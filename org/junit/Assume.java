package org.junit;

import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.matchers.Each;

public class Assume {
   public static void assumeTrue(boolean b) {
      assumeThat(b, CoreMatchers.is(true));
   }

   public static void assumeNotNull(Object... objects) {
      assumeThat(Arrays.asList(objects), Each.each(CoreMatchers.notNullValue()));
   }

   public static <T> void assumeThat(T actual, Matcher<T> matcher) {
      if (!matcher.matches(actual)) {
         throw new AssumptionViolatedException(actual, matcher);
      }
   }

   public static void assumeNoException(Throwable t) {
      assumeThat(t, CoreMatchers.nullValue());
   }
}
