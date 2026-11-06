package org.junit.runner.manipulation;

import org.junit.runner.Description;

public abstract class Filter {
   public static Filter ALL = new Filter() {
      public boolean shouldRun(Description description) {
         return true;
      }

      public String describe() {
         return "all tests";
      }

      public void apply(Object child) throws NoTestsRemainException {
      }

      public Filter intersect(Filter second) {
         return second;
      }
   };

   public static Filter matchMethodDescription(final Description desiredDescription) {
      return new Filter() {
         public boolean shouldRun(Description description) {
            if (description.isTest()) {
               return desiredDescription.equals(description);
            } else {
               for(Description each : description.getChildren()) {
                  if (this.shouldRun(each)) {
                     return true;
                  }
               }

               return false;
            }
         }

         public String describe() {
            return String.format("Method %s", desiredDescription.getDisplayName());
         }
      };
   }

   public abstract boolean shouldRun(Description var1);

   public abstract String describe();

   public void apply(Object child) throws NoTestsRemainException {
      if (child instanceof Filterable) {
         Filterable filterable = (Filterable)child;
         filterable.filter(this);
      }
   }

   public Filter intersect(final Filter second) {
      return second != this && second != ALL ? new Filter() {
         public boolean shouldRun(Description description) {
            return Filter.this.shouldRun(description) && second.shouldRun(description);
         }

         public String describe() {
            return Filter.this.describe() + " and " + second.describe();
         }
      } : this;
   }
}
