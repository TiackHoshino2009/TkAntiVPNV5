package org.junit.internal.builders;

import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class AnnotatedBuilder extends RunnerBuilder {
   private static final String CONSTRUCTOR_ERROR_FORMAT = "Custom runner class %s should have a public constructor with signature %s(Class testClass)";
   private RunnerBuilder fSuiteBuilder;

   public AnnotatedBuilder(RunnerBuilder suiteBuilder) {
      this.fSuiteBuilder = suiteBuilder;
   }

   public Runner runnerForClass(Class<?> testClass) throws Exception {
      RunWith annotation = (RunWith)testClass.getAnnotation(RunWith.class);
      return annotation != null ? this.buildRunner(annotation.value(), testClass) : null;
   }

   public Runner buildRunner(Class<? extends Runner> runnerClass, Class<?> testClass) throws Exception {
      try {
         return (Runner)runnerClass.getConstructor(Class.class).newInstance(testClass);
      } catch (NoSuchMethodException var7) {
         try {
            return (Runner)runnerClass.getConstructor(Class.class, RunnerBuilder.class).newInstance(testClass, this.fSuiteBuilder);
         } catch (NoSuchMethodException var6) {
            String simpleName = runnerClass.getSimpleName();
            throw new InitializationError(String.format("Custom runner class %s should have a public constructor with signature %s(Class testClass)", simpleName, simpleName));
         }
      }
   }
}
