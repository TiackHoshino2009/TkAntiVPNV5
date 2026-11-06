package org.junit.runners;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.rules.RuleFieldValidator;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public abstract class ParentRunner<T> extends Runner implements Filterable, Sortable {
   private final TestClass fTestClass;
   private Sorter fSorter;
   private List<T> fFilteredChildren;
   private RunnerScheduler fScheduler;

   protected ParentRunner(Class<?> testClass) throws InitializationError {
      // $FF: Couldn't be decompiled
   }

   protected abstract List<T> getChildren();

   protected abstract Description describeChild(T var1);

   protected abstract void runChild(T var1, RunNotifier var2);

   protected void collectInitializationErrors(List<Throwable> errors) {
      this.validatePublicVoidNoArgMethods(BeforeClass.class, true, errors);
      this.validatePublicVoidNoArgMethods(AfterClass.class, true, errors);
      this.validateClassRules(errors);
   }

   protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors) {
      for(FrameworkMethod eachTestMethod : this.getTestClass().getAnnotatedMethods(annotation)) {
         eachTestMethod.validatePublicVoidNoArg(isStatic, errors);
      }

   }

   private void validateClassRules(List<Throwable> errors) {
      RuleFieldValidator.CLASS_RULE_VALIDATOR.validate(this.getTestClass(), errors);
   }

   protected Statement classBlock(RunNotifier notifier) {
      Statement statement = this.childrenInvoker(notifier);
      statement = this.withBeforeClasses(statement);
      statement = this.withAfterClasses(statement);
      statement = this.withClassRules(statement);
      return statement;
   }

   protected Statement withBeforeClasses(Statement statement) {
      List<FrameworkMethod> befores = this.fTestClass.getAnnotatedMethods(BeforeClass.class);
      return (Statement)(befores.isEmpty() ? statement : new RunBefores(statement, befores, (Object)null));
   }

   protected Statement withAfterClasses(Statement statement) {
      List<FrameworkMethod> afters = this.fTestClass.getAnnotatedMethods(AfterClass.class);
      return (Statement)(afters.isEmpty() ? statement : new RunAfters(statement, afters, (Object)null));
   }

   private Statement withClassRules(Statement statement) {
      List<TestRule> classRules = this.classRules();
      return (Statement)(classRules.isEmpty() ? statement : new RunRules(statement, classRules, this.getDescription()));
   }

   protected List<TestRule> classRules() {
      return this.fTestClass.<TestRule>getAnnotatedFieldValues((Object)null, ClassRule.class, TestRule.class);
   }

   protected Statement childrenInvoker(RunNotifier notifier) {
      // $FF: Couldn't be decompiled
   }

   private void runChildren(RunNotifier notifier) {
      // $FF: Couldn't be decompiled
   }

   protected String getName() {
      return this.fTestClass.getName();
   }

   public final TestClass getTestClass() {
      return this.fTestClass;
   }

   protected final void runLeaf(Statement statement, Description description, RunNotifier notifier) {
      EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
      eachNotifier.fireTestStarted();

      try {
         statement.evaluate();
      } catch (AssumptionViolatedException e) {
         eachNotifier.addFailedAssumption(e);
      } catch (Throwable e) {
         eachNotifier.addFailure(e);
      } finally {
         eachNotifier.fireTestFinished();
      }

   }

   protected Annotation[] getRunnerAnnotations() {
      return this.fTestClass.getAnnotations();
   }

   public Description getDescription() {
      Description description = Description.createSuiteDescription(this.getName(), this.getRunnerAnnotations());

      for(T child : this.getFilteredChildren()) {
         description.addChild(this.describeChild(child));
      }

      return description;
   }

   public void run(RunNotifier notifier) {
      EachTestNotifier testNotifier = new EachTestNotifier(notifier, this.getDescription());

      try {
         Statement statement = this.classBlock(notifier);
         statement.evaluate();
      } catch (AssumptionViolatedException var4) {
         testNotifier.fireTestIgnored();
      } catch (StoppedByUserException e) {
         throw e;
      } catch (Throwable e) {
         testNotifier.addFailure(e);
      }

   }

   public void filter(Filter filter) throws NoTestsRemainException {
      Iterator<T> iter = this.getFilteredChildren().iterator();

      while(iter.hasNext()) {
         T each = (T)iter.next();
         if (this.shouldRun(filter, each)) {
            try {
               filter.apply(each);
            } catch (NoTestsRemainException var5) {
               iter.remove();
            }
         } else {
            iter.remove();
         }
      }

      if (this.getFilteredChildren().isEmpty()) {
         throw new NoTestsRemainException();
      }
   }

   public void sort(Sorter sorter) {
      this.fSorter = sorter;

      for(T each : this.getFilteredChildren()) {
         this.sortChild(each);
      }

      Collections.sort(this.getFilteredChildren(), this.comparator());
   }

   private void validate() throws InitializationError {
      List<Throwable> errors = new ArrayList();
      this.collectInitializationErrors(errors);
      if (!errors.isEmpty()) {
         throw new InitializationError(errors);
      }
   }

   private List<T> getFilteredChildren() {
      if (this.fFilteredChildren == null) {
         this.fFilteredChildren = new ArrayList(this.getChildren());
      }

      return this.fFilteredChildren;
   }

   private void sortChild(T child) {
      this.fSorter.apply(child);
   }

   private boolean shouldRun(Filter filter, T each) {
      return filter.shouldRun(this.describeChild(each));
   }

   private Comparator<? super T> comparator() {
      // $FF: Couldn't be decompiled
   }

   public void setScheduler(RunnerScheduler scheduler) {
      this.fScheduler = scheduler;
   }

   // $FF: synthetic method
   static void access$000(ParentRunner x0, RunNotifier x1) {
      x0.runChildren(x1);
   }

   // $FF: synthetic method
   static Sorter access$100(ParentRunner x0) {
      return x0.fSorter;
   }
}
