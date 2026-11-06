package org.hamcrest.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class IsAnything<T> extends BaseMatcher<T> {
   private final String description;

   public IsAnything() {
      this("ANYTHING");
   }

   public IsAnything(String description) {
      this.description = description;
   }

   public boolean matches(Object o) {
      return true;
   }

   public void describeTo(Description description) {
      description.appendText(this.description);
   }

   @Factory
   public static <T> Matcher<T> anything() {
      return new IsAnything<T>();
   }

   @Factory
   public static <T> Matcher<T> anything(String description) {
      return new IsAnything<T>(description);
   }

   @Factory
   public static <T> Matcher<T> any(Class<T> type) {
      return new IsAnything<T>();
   }
}
