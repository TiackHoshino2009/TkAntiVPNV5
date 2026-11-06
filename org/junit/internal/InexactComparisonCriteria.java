package org.junit.internal;

import org.junit.Assert;

public class InexactComparisonCriteria extends ComparisonCriteria {
   public double fDelta;

   public InexactComparisonCriteria(double delta) {
      this.fDelta = delta;
   }

   protected void assertElementsEqual(Object expected, Object actual) {
      if (expected instanceof Double) {
         Assert.assertEquals((Double)expected, (Double)actual, this.fDelta);
      } else {
         Assert.assertEquals((double)(Float)expected, (double)(Float)actual, this.fDelta);
      }

   }
}
