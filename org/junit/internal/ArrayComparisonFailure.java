package org.junit.internal;

import java.util.ArrayList;
import java.util.List;

public class ArrayComparisonFailure extends AssertionError {
   private static final long serialVersionUID = 1L;
   private List<Integer> fIndices = new ArrayList();
   private final String fMessage;
   private final AssertionError fCause;

   public ArrayComparisonFailure(String message, AssertionError cause, int index) {
      this.fMessage = message;
      this.fCause = cause;
      this.addDimension(index);
   }

   public void addDimension(int index) {
      this.fIndices.add(0, index);
   }

   public String getMessage() {
      StringBuilder builder = new StringBuilder();
      if (this.fMessage != null) {
         builder.append(this.fMessage);
      }

      builder.append("arrays first differed at element ");

      for(int each : this.fIndices) {
         builder.append("[");
         builder.append(each);
         builder.append("]");
      }

      builder.append("; ");
      builder.append(this.fCause.getMessage());
      return builder.toString();
   }

   public String toString() {
      return this.getMessage();
   }
}
