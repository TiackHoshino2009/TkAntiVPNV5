package org.junit.rules;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.matchers.JUnitMatchers;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ExpectedException implements TestRule {
   private Matcher<Object> fMatcher = null;

   public static ExpectedException none() {
      return new ExpectedException();
   }

   private ExpectedException() {
   }

   public Statement apply(Statement base, Description description) {
      return new ExpectedExceptionStatement(base);
   }

   public void expect(Matcher<?> matcher) {
      if (this.fMatcher == null) {
         this.fMatcher = matcher;
      } else {
         this.fMatcher = JUnitMatchers.<Object>both(this.fMatcher).and(matcher);
      }

   }

   public void expect(Class<? extends Throwable> type) {
      this.expect(CoreMatchers.instanceOf(type));
   }

   public void expectMessage(String substring) {
      this.expectMessage(JUnitMatchers.containsString(substring));
   }

   public void expectMessage(Matcher<String> matcher) {
      this.expect(this.hasMessage(matcher));
   }

   private Matcher<Throwable> hasMessage(final Matcher<String> matcher) {
      return new TypeSafeMatcher<Throwable>() {
         public void describeTo(org.hamcrest.Description description) {
            description.appendText("exception with message ");
            description.appendDescriptionOf(matcher);
         }

         public boolean matchesSafely(Throwable item) {
            return matcher.matches(item.getMessage());
         }
      };
   }

   private class ExpectedExceptionStatement extends Statement {
      private final Statement fNext;

      public ExpectedExceptionStatement(Statement base) {
         this.fNext = base;
      }

      public void evaluate() throws Throwable {
         try {
            this.fNext.evaluate();
         } catch (Throwable e) {
            if (ExpectedException.this.fMatcher == null) {
               throw e;
            }

            Assert.assertThat(e, ExpectedException.this.fMatcher);
            return;
         }

         if (ExpectedException.this.fMatcher != null) {
            throw new AssertionError("Expected test to throw " + StringDescription.toString(ExpectedException.this.fMatcher));
         }
      }
   }
}
