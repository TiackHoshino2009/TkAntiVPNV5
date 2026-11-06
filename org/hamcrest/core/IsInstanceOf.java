package org.hamcrest.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class IsInstanceOf extends BaseMatcher<Object> {
   private final Class<?> theClass;

   public IsInstanceOf(Class<?> theClass) {
      this.theClass = theClass;
   }

   public boolean matches(Object item) {
      return this.theClass.isInstance(item);
   }

   public void describeTo(Description description) {
      description.appendText("an instance of ").appendText(this.theClass.getName());
   }

   @Factory
   public static Matcher<Object> instanceOf(Class<?> type) {
      return new IsInstanceOf(type);
   }
}
