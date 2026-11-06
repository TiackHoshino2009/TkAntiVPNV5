package org.junit;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.internal.ExactComparisonCriteria;
import org.junit.internal.InexactComparisonCriteria;

public class Assert {
   protected Assert() {
   }

   public static void assertTrue(String message, boolean condition) {
      if (!condition) {
         fail(message);
      }

   }

   public static void assertTrue(boolean condition) {
      assertTrue((String)null, condition);
   }

   public static void assertFalse(String message, boolean condition) {
      assertTrue(message, !condition);
   }

   public static void assertFalse(boolean condition) {
      assertFalse((String)null, condition);
   }

   public static void fail(String message) {
      if (message == null) {
         throw new AssertionError();
      } else {
         throw new AssertionError(message);
      }
   }

   public static void fail() {
      fail((String)null);
   }

   public static void assertEquals(String message, Object expected, Object actual) {
      if (expected != null || actual != null) {
         if (expected == null || !isEquals(expected, actual)) {
            if (expected instanceof String && actual instanceof String) {
               String cleanMessage = message == null ? "" : message;
               throw new ComparisonFailure(cleanMessage, (String)expected, (String)actual);
            } else {
               failNotEquals(message, expected, actual);
            }
         }
      }
   }

   private static boolean isEquals(Object expected, Object actual) {
      return expected.equals(actual);
   }

   public static void assertEquals(Object expected, Object actual) {
      assertEquals((String)null, (Object)expected, (Object)actual);
   }

   public static void assertArrayEquals(String message, Object[] expecteds, Object[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
   }

   public static void assertArrayEquals(Object[] expecteds, Object[] actuals) {
      assertArrayEquals((String)null, (Object[])expecteds, (Object[])actuals);
   }

   public static void assertArrayEquals(String message, byte[] expecteds, byte[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
   }

   public static void assertArrayEquals(byte[] expecteds, byte[] actuals) {
      assertArrayEquals((String)null, (byte[])expecteds, (byte[])actuals);
   }

   public static void assertArrayEquals(String message, char[] expecteds, char[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
   }

   public static void assertArrayEquals(char[] expecteds, char[] actuals) {
      assertArrayEquals((String)null, (char[])expecteds, (char[])actuals);
   }

   public static void assertArrayEquals(String message, short[] expecteds, short[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
   }

   public static void assertArrayEquals(short[] expecteds, short[] actuals) {
      assertArrayEquals((String)null, (short[])expecteds, (short[])actuals);
   }

   public static void assertArrayEquals(String message, int[] expecteds, int[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
   }

   public static void assertArrayEquals(int[] expecteds, int[] actuals) {
      assertArrayEquals((String)null, (int[])expecteds, (int[])actuals);
   }

   public static void assertArrayEquals(String message, long[] expecteds, long[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
   }

   public static void assertArrayEquals(long[] expecteds, long[] actuals) {
      assertArrayEquals((String)null, (long[])expecteds, (long[])actuals);
   }

   public static void assertArrayEquals(String message, double[] expecteds, double[] actuals, double delta) throws ArrayComparisonFailure {
      (new InexactComparisonCriteria(delta)).arrayEquals(message, expecteds, actuals);
   }

   public static void assertArrayEquals(double[] expecteds, double[] actuals, double delta) {
      assertArrayEquals((String)null, expecteds, actuals, delta);
   }

   public static void assertArrayEquals(String message, float[] expecteds, float[] actuals, float delta) throws ArrayComparisonFailure {
      (new InexactComparisonCriteria((double)delta)).arrayEquals(message, expecteds, actuals);
   }

   public static void assertArrayEquals(float[] expecteds, float[] actuals, float delta) {
      assertArrayEquals((String)null, expecteds, actuals, delta);
   }

   private static void internalArrayEquals(String message, Object expecteds, Object actuals) throws ArrayComparisonFailure {
      (new ExactComparisonCriteria()).arrayEquals(message, expecteds, actuals);
   }

   public static void assertEquals(String message, double expected, double actual, double delta) {
      if (Double.compare(expected, actual) != 0) {
         if (!(Math.abs(expected - actual) <= delta)) {
            failNotEquals(message, new Double(expected), new Double(actual));
         }

      }
   }

   public static void assertEquals(long expected, long actual) {
      assertEquals((String)null, expected, actual);
   }

   public static void assertEquals(String message, long expected, long actual) {
      assertEquals(message, (Object)expected, (Object)actual);
   }

   /** @deprecated */
   @Deprecated
   public static void assertEquals(double expected, double actual) {
      assertEquals((String)null, expected, actual);
   }

   /** @deprecated */
   @Deprecated
   public static void assertEquals(String message, double expected, double actual) {
      fail("Use assertEquals(expected, actual, delta) to compare floating-point numbers");
   }

   public static void assertEquals(double expected, double actual, double delta) {
      assertEquals((String)null, expected, actual, delta);
   }

   public static void assertNotNull(String message, Object object) {
      assertTrue(message, object != null);
   }

   public static void assertNotNull(Object object) {
      assertNotNull((String)null, object);
   }

   public static void assertNull(String message, Object object) {
      assertTrue(message, object == null);
   }

   public static void assertNull(Object object) {
      assertNull((String)null, object);
   }

   public static void assertSame(String message, Object expected, Object actual) {
      if (expected != actual) {
         failNotSame(message, expected, actual);
      }
   }

   public static void assertSame(Object expected, Object actual) {
      assertSame((String)null, expected, actual);
   }

   public static void assertNotSame(String message, Object unexpected, Object actual) {
      if (unexpected == actual) {
         failSame(message);
      }

   }

   public static void assertNotSame(Object unexpected, Object actual) {
      assertNotSame((String)null, unexpected, actual);
   }

   private static void failSame(String message) {
      String formatted = "";
      if (message != null) {
         formatted = message + " ";
      }

      fail(formatted + "expected not same");
   }

   private static void failNotSame(String message, Object expected, Object actual) {
      String formatted = "";
      if (message != null) {
         formatted = message + " ";
      }

      fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
   }

   private static void failNotEquals(String message, Object expected, Object actual) {
      fail(format(message, expected, actual));
   }

   static String format(String message, Object expected, Object actual) {
      String formatted = "";
      if (message != null && !message.equals("")) {
         formatted = message + " ";
      }

      String expectedString = String.valueOf(expected);
      String actualString = String.valueOf(actual);
      return expectedString.equals(actualString) ? formatted + "expected: " + formatClassAndValue(expected, expectedString) + " but was: " + formatClassAndValue(actual, actualString) : formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">";
   }

   private static String formatClassAndValue(Object value, String valueString) {
      String className = value == null ? "null" : value.getClass().getName();
      return className + "<" + valueString + ">";
   }

   /** @deprecated */
   @Deprecated
   public static void assertEquals(String message, Object[] expecteds, Object[] actuals) {
      assertArrayEquals(message, expecteds, actuals);
   }

   /** @deprecated */
   @Deprecated
   public static void assertEquals(Object[] expecteds, Object[] actuals) {
      assertArrayEquals(expecteds, actuals);
   }

   public static <T> void assertThat(T actual, Matcher<T> matcher) {
      assertThat("", actual, matcher);
   }

   public static <T> void assertThat(String reason, T actual, Matcher<T> matcher) {
      if (!matcher.matches(actual)) {
         Description description = new StringDescription();
         description.appendText(reason);
         description.appendText("\nExpected: ");
         description.appendDescriptionOf(matcher);
         description.appendText("\n     got: ");
         description.appendValue(actual);
         description.appendText("\n");
         throw new AssertionError(description.toString());
      }
   }
}
