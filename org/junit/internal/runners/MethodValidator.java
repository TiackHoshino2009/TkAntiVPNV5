package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** @deprecated */
@Deprecated
public class MethodValidator {
   private final List<Throwable> fErrors = new ArrayList();
   private TestClass fTestClass;

   public MethodValidator(TestClass testClass) {
      this.fTestClass = testClass;
   }

   public void validateInstanceMethods() {
      this.validateTestMethods(After.class, false);
      this.validateTestMethods(Before.class, false);
      this.validateTestMethods(Test.class, false);
      List<Method> methods = this.fTestClass.getAnnotatedMethods(Test.class);
      if (methods.size() == 0) {
         this.fErrors.add(new Exception("No runnable methods"));
      }

   }

   public void validateStaticMethods() {
      this.validateTestMethods(BeforeClass.class, true);
      this.validateTestMethods(AfterClass.class, true);
   }

   public List<Throwable> validateMethodsForDefaultRunner() {
      this.validateNoArgConstructor();
      this.validateStaticMethods();
      this.validateInstanceMethods();
      return this.fErrors;
   }

   public void assertValid() throws InitializationError {
      if (!this.fErrors.isEmpty()) {
         throw new InitializationError(this.fErrors);
      }
   }

   public void validateNoArgConstructor() {
      try {
         this.fTestClass.getConstructor();
      } catch (Exception e) {
         this.fErrors.add(new Exception("Test class should have public zero-argument constructor", e));
      }

   }

   private void validateTestMethods(Class<? extends Annotation> annotation, boolean isStatic) {
      for(Method each : this.fTestClass.getAnnotatedMethods(annotation)) {
         if (Modifier.isStatic(each.getModifiers()) != isStatic) {
            String state = isStatic ? "should" : "should not";
            this.fErrors.add(new Exception("Method " + each.getName() + "() " + state + " be static"));
         }

         if (!Modifier.isPublic(each.getDeclaringClass().getModifiers())) {
            this.fErrors.add(new Exception("Class " + each.getDeclaringClass().getName() + " should be public"));
         }

         if (!Modifier.isPublic(each.getModifiers())) {
            this.fErrors.add(new Exception("Method " + each.getName() + " should be public"));
         }

         if (each.getReturnType() != Void.TYPE) {
            this.fErrors.add(new Exception("Method " + each.getName() + " should be void"));
         }

         if (each.getParameterTypes().length != 0) {
            this.fErrors.add(new Exception("Method " + each.getName() + " should have no parameters"));
         }
      }

   }
}
