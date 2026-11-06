package org.junit.runner.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Result;

public class RunNotifier {
   private final List<RunListener> fListeners = Collections.synchronizedList(new ArrayList());
   private boolean fPleaseStop = false;

   public void addListener(RunListener listener) {
      this.fListeners.add(listener);
   }

   public void removeListener(RunListener listener) {
      this.fListeners.remove(listener);
   }

   public void fireTestRunStarted(final Description description) {
      (new SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testRunStarted(description);
         }
      }).run();
   }

   public void fireTestRunFinished(final Result result) {
      (new SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testRunFinished(result);
         }
      }).run();
   }

   public void fireTestStarted(final Description description) throws StoppedByUserException {
      if (this.fPleaseStop) {
         throw new StoppedByUserException();
      } else {
         (new SafeNotifier() {
            protected void notifyListener(RunListener each) throws Exception {
               each.testStarted(description);
            }
         }).run();
      }
   }

   public void fireTestFailure(final Failure failure) {
      (new SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testFailure(failure);
         }
      }).run();
   }

   public void fireTestAssumptionFailed(final Failure failure) {
      (new SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testAssumptionFailure(failure);
         }
      }).run();
   }

   public void fireTestIgnored(final Description description) {
      (new SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testIgnored(description);
         }
      }).run();
   }

   public void fireTestFinished(final Description description) {
      (new SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testFinished(description);
         }
      }).run();
   }

   public void pleaseStop() {
      this.fPleaseStop = true;
   }

   public void addFirstListener(RunListener listener) {
      this.fListeners.add(0, listener);
   }

   private abstract class SafeNotifier {
      private SafeNotifier() {
      }

      void run() {
         synchronized(RunNotifier.this.fListeners) {
            Iterator<RunListener> all = RunNotifier.this.fListeners.iterator();

            while(all.hasNext()) {
               try {
                  this.notifyListener((RunListener)all.next());
               } catch (Exception e) {
                  all.remove();
                  RunNotifier.this.fireTestFailure(new Failure(Description.TEST_MECHANISM, e));
               }
            }

         }
      }

      protected abstract void notifyListener(RunListener var1) throws Exception;
   }
}
