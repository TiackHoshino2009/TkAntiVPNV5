package org.junit.internal.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class CombinableMatcher<T> extends BaseMatcher<T> {
   private final Matcher<? extends T> fMatcher;

   public CombinableMatcher(Matcher<? extends T> matcher) {
      this.fMatcher = matcher;
   }

   public boolean matches(Object item) {
      return this.fMatcher.matches(item);
   }

   public void describeTo(Description description) {
      description.appendDescriptionOf(this.fMatcher);
   }

   public CombinableMatcher<T> and(Matcher<? extends T> matcher) {
      return new CombinableMatcher<T>(CoreMatchers.allOf(matcher, this.fMatcher));
   }

   public CombinableMatcher<T> or(Matcher<? extends T> matcher) {
      return new CombinableMatcher<T>(CoreMatchers.anyOf(matcher, this.fMatcher));
   }
}
