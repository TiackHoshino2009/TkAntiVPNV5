package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/** @deprecated */
@Deprecated
public class TestMethod {
   private final Method fMethod;
   private TestClass fTestClass;

   public TestMethod(Method method, TestClass testClass) {
      this.fMethod = method;
      this.fTestClass = testClass;
   }

   public boolean isIgnored() {
      return this.fMethod.getAnnotation(Ignore.class) != null;
   }

   public long getTimeout() {
      Test annotation = (Test)this.fMethod.getAnnotation(Test.class);
      if (annotation == null) {
         return 0L;
      } else {
         long timeout = annotation.timeout();
         return timeout;
      }
   }

   protected Class<? extends Throwable> getExpectedException() {
      Test annotation = (Test)this.fMethod.getAnnotation(Test.class);
      return annotation != null && annotation.expected() != Test.None.class ? annotation.expected() : null;
   }

   boolean isUnexpected(Throwable exception) {
      return !this.getExpectedException().isAssignableFrom(exception.getClass());
   }

   boolean expectsException() {
      return this.getExpectedException() != null;
   }

   List<Method> getBefores() {
      return this.fTestClass.getAnnotatedMethods(Before.class);
   }

   List<Method> getAfters() {
      return this.fTestClass.getAnnotatedMethods(After.class);
   }

   public void invoke(Object test) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      this.fMethod.invoke(test);
   }
}
