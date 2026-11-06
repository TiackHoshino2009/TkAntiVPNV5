package org.hamcrest.core;

import java.util.Arrays;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class AllOf<T> extends BaseMatcher<T> {
   private final Iterable<Matcher<? extends T>> matchers;

   public AllOf(Iterable<Matcher<? extends T>> matchers) {
      this.matchers = matchers;
   }

   public boolean matches(Object o) {
      for(Matcher<? extends T> matcher : this.matchers) {
         if (!matcher.matches(o)) {
            return false;
         }
      }

      return true;
   }

   public void describeTo(Description description) {
      description.appendList("(", " and ", ")", this.matchers);
   }

   @Factory
   public static <T> Matcher<T> allOf(Matcher<? extends T>... matchers) {
      return allOf(Arrays.asList(matchers));
   }

   @Factory
   public static <T> Matcher<T> allOf(Iterable<Matcher<? extends T>> matchers) {
      return new AllOf<T>(matchers);
   }
}
