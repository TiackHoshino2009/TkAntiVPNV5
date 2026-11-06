package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/** @deprecated */
@Deprecated
public class JUnit4ClassRunner extends Runner implements Filterable, Sortable {
   private final List<Method> fTestMethods;
   private TestClass fTestClass;

   public JUnit4ClassRunner(Class<?> klass) throws InitializationError {
      this.fTestClass = new TestClass(klass);
      this.fTestMethods = this.getTestMethods();
      this.validate();
   }

   protected List<Method> getTestMethods() {
      return this.fTestClass.getTestMethods();
   }

   protected void validate() throws InitializationError {
      MethodValidator methodValidator = new MethodValidator(this.fTestClass);
      methodValidator.validateMethodsForDefaultRunner();
      methodValidator.assertValid();
   }

   public void run(final RunNotifier notifier) {
      (new ClassRoadie(notifier, this.fTestClass, this.getDescription(), new Runnable() {
         public void run() {
            JUnit4ClassRunner.this.runMethods(notifier);
         }
      })).runProtected();
   }

   protected void runMethods(RunNotifier notifier) {
      for(Method method : this.fTestMethods) {
         this.invokeTestMethod(method, notifier);
      }

   }

   public Description getDescription() {
      Description spec = Description.createSuiteDescription(this.getName(), this.classAnnotations());

      for(Method method : this.fTestMethods) {
         spec.addChild(this.methodDescription(method));
      }

      return spec;
   }

   protected Annotation[] classAnnotations() {
      return this.fTestClass.getJavaClass().getAnnotations();
   }

   protected String getName() {
      return this.getTestClass().getName();
   }

   protected Object createTest() throws Exception {
      return this.getTestClass().getConstructor().newInstance();
   }

   protected void invokeTestMethod(Method method, RunNotifier notifier) {
      Description description = this.methodDescription(method);

      Object test;
      try {
         test = this.createTest();
      } catch (InvocationTargetException e) {
         this.testAborted(notifier, description, e.getCause());
         return;
      } catch (Exception e) {
         this.testAborted(notifier, description, e);
         return;
      }

      TestMethod testMethod = this.wrapMethod(method);
      (new MethodRoadie(test, testMethod, notifier, description)).run();
   }

   private void testAborted(RunNotifier notifier, Description description, Throwable e) {
      notifier.fireTestStarted(description);
      notifier.fireTestFailure(new Failure(description, e));
      notifier.fireTestFinished(description);
   }

   protected TestMethod wrapMethod(Method method) {
      return new TestMethod(method, this.fTestClass);
   }

   protected String testName(Method method) {
      return method.getName();
   }

   protected Description methodDescription(Method method) {
      return Description.createTestDescription(this.getTestClass().getJavaClass(), this.testName(method), this.testAnnotations(method));
   }

   protected Annotation[] testAnnotations(Method method) {
      return method.getAnnotations();
   }

   public void filter(Filter filter) throws NoTestsRemainException {
      Iterator<Method> iter = this.fTestMethods.iterator();

      while(iter.hasNext()) {
         Method method = (Method)iter.next();
         if (!filter.shouldRun(this.methodDescription(method))) {
            iter.remove();
         }
      }

      if (this.fTestMethods.isEmpty()) {
         throw new NoTestsRemainException();
      }
   }

   public void sort(final Sorter sorter) {
      Collections.sort(this.fTestMethods, new Comparator<Method>() {
         public int compare(Method o1, Method o2) {
            return sorter.compare(JUnit4ClassRunner.this.methodDescription(o1), JUnit4ClassRunner.this.methodDescription(o2));
         }
      });
   }

   protected TestClass getTestClass() {
      return this.fTestClass;
   }
}
