package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import junit.framework.Test;

public class SuiteMethod extends JUnit38ClassRunner {
   public SuiteMethod(Class<?> klass) throws Throwable {
      super(testFromSuiteMethod(klass));
   }

   public static Test testFromSuiteMethod(Class<?> klass) throws Throwable {
      Method suiteMethod = null;
      Test suite = null;

      try {
         suiteMethod = klass.getMethod("suite");
         if (!Modifier.isStatic(suiteMethod.getModifiers())) {
            throw new Exception(klass.getName() + ".suite() must be static");
         } else {
            suite = (Test)suiteMethod.invoke((Object)null);
            return suite;
         }
      } catch (InvocationTargetException e) {
         throw e.getCause();
      }
   }
}
