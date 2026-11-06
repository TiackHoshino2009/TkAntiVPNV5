package org.junit.experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.runner.Computer;
import org.junit.runner.Runner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.RunnerScheduler;

public class ParallelComputer extends Computer {
   private final boolean fClasses;
   private final boolean fMethods;

   public ParallelComputer(boolean classes, boolean methods) {
      this.fClasses = classes;
      this.fMethods = methods;
   }

   public static Computer classes() {
      return new ParallelComputer(true, false);
   }

   public static Computer methods() {
      return new ParallelComputer(false, true);
   }

   private static <T> Runner parallelize(Runner runner) {
      if (runner instanceof ParentRunner) {
         ((ParentRunner)runner).setScheduler(new RunnerScheduler() {
            private final List<Future<Object>> fResults = new ArrayList();
            private final ExecutorService fService = Executors.newCachedThreadPool();

            public void schedule(final Runnable childStatement) {
               this.fResults.add(this.fService.submit(new Callable<Object>() {
                  public Object call() throws Exception {
                     childStatement.run();
                     return null;
                  }
               }));
            }

            public void finished() {
               for(Future<Object> each : this.fResults) {
                  try {
                     each.get();
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               }

            }
         });
      }

      return runner;
   }

   public Runner getSuite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
      Runner suite = super.getSuite(builder, classes);
      return this.fClasses ? parallelize(suite) : suite;
   }

   protected Runner getRunner(RunnerBuilder builder, Class<?> testClass) throws Throwable {
      Runner runner = super.getRunner(builder, testClass);
      return this.fMethods ? parallelize(runner) : runner;
   }
}
