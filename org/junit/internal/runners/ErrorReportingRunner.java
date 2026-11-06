package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class ErrorReportingRunner extends Runner {
   private final List<Throwable> fCauses;
   private final Class<?> fTestClass;

   public ErrorReportingRunner(Class<?> testClass, Throwable cause) {
      this.fTestClass = testClass;
      this.fCauses = this.getCauses(cause);
   }

   public Description getDescription() {
      Description description = Description.createSuiteDescription(this.fTestClass);

      for(Throwable each : this.fCauses) {
         description.addChild(this.describeCause(each));
      }

      return description;
   }

   public void run(RunNotifier notifier) {
      for(Throwable each : this.fCauses) {
         this.runCause(each, notifier);
      }

   }

   private List<Throwable> getCauses(Throwable cause) {
      if (cause instanceof InvocationTargetException) {
         return this.getCauses(cause.getCause());
      } else if (cause instanceof org.junit.runners.model.InitializationError) {
         return ((org.junit.runners.model.InitializationError)cause).getCauses();
      } else {
         return cause instanceof InitializationError ? ((InitializationError)cause).getCauses() : Arrays.asList(cause);
      }
   }

   private Description describeCause(Throwable child) {
      return Description.createTestDescription(this.fTestClass, "initializationError");
   }

   private void runCause(Throwable child, RunNotifier notifier) {
      Description description = this.describeCause(child);
      notifier.fireTestStarted(description);
      notifier.fireTestFailure(new Failure(description, child));
      notifier.fireTestFinished(description);
   }
}
