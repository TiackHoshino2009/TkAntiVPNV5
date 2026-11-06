package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/** @deprecated */
@Deprecated
public class ClassRoadie {
   private RunNotifier fNotifier;
   private TestClass fTestClass;
   private Description fDescription;
   private final Runnable fRunnable;

   public ClassRoadie(RunNotifier notifier, TestClass testClass, Description description, Runnable runnable) {
      this.fNotifier = notifier;
      this.fTestClass = testClass;
      this.fDescription = description;
      this.fRunnable = runnable;
   }

   protected void runUnprotected() {
      this.fRunnable.run();
   }

   protected void addFailure(Throwable targetException) {
      this.fNotifier.fireTestFailure(new Failure(this.fDescription, targetException));
   }

   public void runProtected() {
      try {
         this.runBefores();
         this.runUnprotected();
      } catch (FailedBefore var5) {
      } finally {
         this.runAfters();
      }

   }

   private void runBefores() throws FailedBefore {
      try {
         try {
            for(Method before : this.fTestClass.getBefores()) {
               before.invoke((Object)null);
            }

         } catch (InvocationTargetException e) {
            throw e.getTargetException();
         }
      } catch (AssumptionViolatedException var5) {
         throw new FailedBefore();
      } catch (Throwable e) {
         this.addFailure(e);
         throw new FailedBefore();
      }
   }

   private void runAfters() {
      for(Method after : this.fTestClass.getAfters()) {
         try {
            after.invoke((Object)null);
         } catch (InvocationTargetException e) {
            this.addFailure(e.getTargetException());
         } catch (Throwable e) {
            this.addFailure(e);
         }
      }

   }
}
