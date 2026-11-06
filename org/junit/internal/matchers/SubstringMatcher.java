package org.junit.internal.matchers;

import org.hamcrest.Description;

public abstract class SubstringMatcher extends TypeSafeMatcher<String> {
   protected final String substring;

   protected SubstringMatcher(String substring) {
      this.substring = substring;
   }

   public boolean matchesSafely(String item) {
      return this.evalSubstringOf(item);
   }

   public void describeTo(Description description) {
      description.appendText("a string ").appendText(this.relationship()).appendText(" ").appendValue(this.substring);
   }

   protected abstract boolean evalSubstringOf(String var1);

   protected abstract String relationship();
}
