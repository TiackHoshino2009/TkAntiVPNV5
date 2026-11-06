package org.junit.runners.model;

import java.lang.annotation.Annotation;
import java.util.List;

abstract class FrameworkMember<T extends FrameworkMember<T>> {
   abstract Annotation[] getAnnotations();

   abstract boolean isShadowedBy(T var1);

   boolean isShadowedBy(List<T> members) {
      for(T each : members) {
         if (this.isShadowedBy(each)) {
            return true;
         }
      }

      return false;
   }
}
