package org.junit.internal.builders;

import org.junit.Ignore;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class IgnoredBuilder extends RunnerBuilder {
   public Runner runnerForClass(Class<?> testClass) {
      return testClass.getAnnotation(Ignore.class) != null ? new IgnoredClassRunner(testClass) : null;
   }
}
