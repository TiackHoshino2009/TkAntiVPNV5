package org.junit.internal.builders;

import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class SuiteMethodBuilder extends RunnerBuilder {
   public Runner runnerForClass(Class<?> each) throws Throwable {
      return this.hasSuiteMethod(each) ? new SuiteMethod(each) : null;
   }

   public boolean hasSuiteMethod(Class<?> testClass) {
      try {
         testClass.getMethod("suite");
         return true;
      } catch (NoSuchMethodException var3) {
         return false;
      }
   }
}
