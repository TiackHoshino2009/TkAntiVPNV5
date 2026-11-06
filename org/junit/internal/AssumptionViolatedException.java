package org.junit.internal;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.StringDescription;

public class AssumptionViolatedException extends RuntimeException implements SelfDescribing {
   private static final long serialVersionUID = 1L;
   private final Object fValue;
   private final Matcher<?> fMatcher;

   public AssumptionViolatedException(Object value, Matcher<?> matcher) {
      super(value instanceof Throwable ? (Throwable)value : null);
      this.fValue = value;
      this.fMatcher = matcher;
   }

   public AssumptionViolatedException(String assumption) {
      this(assumption, (Matcher)null);
   }

   public String getMessage() {
      return StringDescription.asString(this);
   }

   public void describeTo(Description description) {
      if (this.fMatcher != null) {
         description.appendText("got: ");
         description.appendValue(this.fValue);
         description.appendText(", expected: ");
         description.appendDescriptionOf(this.fMatcher);
      } else {
         description.appendText("failed assumption: " + this.fValue);
      }

   }
}
