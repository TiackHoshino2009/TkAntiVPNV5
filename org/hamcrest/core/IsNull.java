package org.hamcrest.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class IsNull<T> extends BaseMatcher<T> {
   public boolean matches(Object o) {
      return o == null;
   }

   public void describeTo(Description description) {
      description.appendText("null");
   }

   @Factory
   public static <T> Matcher<T> nullValue() {
      return new IsNull<T>();
   }

   @Factory
   public static <T> Matcher<T> notNullValue() {
      return IsNot.not(nullValue());
   }

   @Factory
   public static <T> Matcher<T> nullValue(Class<T> type) {
      return nullValue();
   }

   @Factory
   public static <T> Matcher<T> notNullValue(Class<T> type) {
      return notNullValue();
   }
}
