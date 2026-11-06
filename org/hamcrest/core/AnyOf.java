package org.hamcrest.core;

import java.util.Arrays;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class AnyOf<T> extends BaseMatcher<T> {
   private final Iterable<Matcher<? extends T>> matchers;

   public AnyOf(Iterable<Matcher<? extends T>> matchers) {
      this.matchers = matchers;
   }

   public boolean matches(Object o) {
      for(Matcher<? extends T> matcher : this.matchers) {
         if (matcher.matches(o)) {
            return true;
         }
      }

      return false;
   }

   public void describeTo(Description description) {
      description.appendList("(", " or ", ")", this.matchers);
   }

   @Factory
   public static <T> Matcher<T> anyOf(Matcher<? extends T>... matchers) {
      return anyOf(Arrays.asList(matchers));
   }

   @Factory
   public static <T> Matcher<T> anyOf(Iterable<Matcher<? extends T>> matchers) {
      return new AnyOf<T>(matchers);
   }
}
