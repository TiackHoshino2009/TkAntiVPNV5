package org.junit.experimental.theories;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public class Theories extends BlockJUnit4ClassRunner {
   public Theories(Class<?> klass) throws InitializationError {
      super(klass);
   }

   protected void collectInitializationErrors(List<Throwable> errors) {
      super.collectInitializationErrors(errors);
      this.validateDataPointFields(errors);
   }

   private void validateDataPointFields(List<Throwable> errors) {
      Field[] fields = this.getTestClass().getJavaClass().getDeclaredFields();

      for(Field each : fields) {
         if (each.getAnnotation(DataPoint.class) != null && !Modifier.isStatic(each.getModifiers())) {
            errors.add(new Error("DataPoint field " + each.getName() + " must be static"));
         }
      }

   }

   protected void validateConstructor(List<Throwable> errors) {
      this.validateOnlyOneConstructor(errors);
   }

   protected void validateTestMethods(List<Throwable> errors) {
      for(FrameworkMethod each : this.computeTestMethods()) {
         if (each.getAnnotation(Theory.class) != null) {
            each.validatePublicVoid(false, errors);
         } else {
            each.validatePublicVoidNoArg(false, errors);
         }
      }

   }

   protected List<FrameworkMethod> computeTestMethods() {
      List<FrameworkMethod> testMethods = super.computeTestMethods();
      List<FrameworkMethod> theoryMethods = this.getTestClass().getAnnotatedMethods(Theory.class);
      testMethods.removeAll(theoryMethods);
      testMethods.addAll(theoryMethods);
      return testMethods;
   }

   public Statement methodBlock(FrameworkMethod method) {
      return new TheoryAnchor(method, this.getTestClass());
   }

   public static class TheoryAnchor extends Statement {
      private int successes = 0;
      private FrameworkMethod fTestMethod;
      private TestClass fTestClass;
      private List<AssumptionViolatedException> fInvalidParameters = new ArrayList();

      public TheoryAnchor(FrameworkMethod method, TestClass testClass) {
         this.fTestMethod = method;
         this.fTestClass = testClass;
      }

      private TestClass getTestClass() {
         return this.fTestClass;
      }

      public void evaluate() throws Throwable {
         this.runWithAssignment(Assignments.allUnassigned(this.fTestMethod.getMethod(), this.getTestClass()));
         if (this.successes == 0) {
            Assert.fail("Never found parameters that satisfied method assumptions.  Violated assumptions: " + this.fInvalidParameters);
         }

      }

      protected void runWithAssignment(Assignments parameterAssignment) throws Throwable {
         if (!parameterAssignment.isComplete()) {
            this.runWithIncompleteAssignment(parameterAssignment);
         } else {
            this.runWithCompleteAssignment(parameterAssignment);
         }

      }

      protected void runWithIncompleteAssignment(Assignments incomplete) throws InstantiationException, IllegalAccessException, Throwable {
         for(PotentialAssignment source : incomplete.potentialsForNextUnassigned()) {
            this.runWithAssignment(incomplete.assignNext(source));
         }

      }

      protected void runWithCompleteAssignment(final Assignments complete) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, Throwable {
         (new BlockJUnit4ClassRunner(this.getTestClass().getJavaClass()) {
            protected void collectInitializationErrors(List<Throwable> errors) {
            }

            public Statement methodBlock(FrameworkMethod method) {
               final Statement statement = super.methodBlock(method);
               return new Statement() {
                  public void evaluate() throws Throwable {
                     try {
                        statement.evaluate();
                        TheoryAnchor.this.handleDataPointSuccess();
                     } catch (AssumptionViolatedException e) {
                        TheoryAnchor.this.handleAssumptionViolation(e);
                     } catch (Throwable e) {
                        TheoryAnchor.this.reportParameterizedError(e, complete.getArgumentStrings(TheoryAnchor.this.nullsOk()));
                     }

                  }
               };
            }

            protected Statement methodInvoker(FrameworkMethod method, Object test) {
               return TheoryAnchor.this.methodCompletesWithParameters(method, complete, test);
            }

            public Object createTest() throws Exception {
               return this.getTestClass().getOnlyConstructor().newInstance(complete.getConstructorArguments(TheoryAnchor.this.nullsOk()));
            }
         }).methodBlock(this.fTestMethod).evaluate();
      }

      private Statement methodCompletesWithParameters(final FrameworkMethod method, final Assignments complete, final Object freshInstance) {
         return new Statement() {
            public void evaluate() throws Throwable {
               try {
                  Object[] values = complete.getMethodArguments(TheoryAnchor.this.nullsOk());
                  method.invokeExplosively(freshInstance, values);
               } catch (PotentialAssignment.CouldNotGenerateValueException var2) {
               }

            }
         };
      }

      protected void handleAssumptionViolation(AssumptionViolatedException e) {
         this.fInvalidParameters.add(e);
      }

      protected void reportParameterizedError(Throwable e, Object... params) throws Throwable {
         if (params.length == 0) {
            throw e;
         } else {
            throw new ParameterizedAssertionError(e, this.fTestMethod.getName(), params);
         }
      }

      private boolean nullsOk() {
         Theory annotation = (Theory)this.fTestMethod.getMethod().getAnnotation(Theory.class);
         return annotation == null ? false : annotation.nullsAccepted();
      }

      protected void handleDataPointSuccess() {
         ++this.successes;
      }
   }
}
