package junit.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class TestSuite implements Test {
   private String fName;
   private Vector<Test> fTests;

   public static Test createTest(Class<?> theClass, String name) {
      Constructor<?> constructor;
      try {
         constructor = getTestConstructor(theClass);
      } catch (NoSuchMethodException var8) {
         return warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()");
      }

      Object test;
      try {
         if (constructor.getParameterTypes().length == 0) {
            test = constructor.newInstance();
            if (test instanceof TestCase) {
               ((TestCase)test).setName(name);
            }
         } else {
            test = constructor.newInstance(name);
         }
      } catch (InstantiationException e) {
         return warning("Cannot instantiate test case: " + name + " (" + exceptionToString(e) + ")");
      } catch (InvocationTargetException e) {
         return warning("Exception in constructor: " + name + " (" + exceptionToString(e.getTargetException()) + ")");
      } catch (IllegalAccessException e) {
         return warning("Cannot access test case: " + name + " (" + exceptionToString(e) + ")");
      }

      return (Test)test;
   }

   public static Constructor<?> getTestConstructor(Class<?> theClass) throws NoSuchMethodException {
      try {
         return theClass.getConstructor(String.class);
      } catch (NoSuchMethodException var2) {
         return theClass.getConstructor();
      }
   }

   public static Test warning(final String message) {
      return new TestCase("warning") {
         protected void runTest() {
            fail(message);
         }
      };
   }

   private static String exceptionToString(Throwable t) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(stringWriter);
      t.printStackTrace(writer);
      return stringWriter.toString();
   }

   public TestSuite() {
      this.fTests = new Vector(10);
   }

   public TestSuite(Class<?> theClass) {
      this.fTests = new Vector(10);
      this.addTestsFromTestCase(theClass);
   }

   private void addTestsFromTestCase(Class<?> theClass) {
      this.fName = theClass.getName();

      try {
         getTestConstructor(theClass);
      } catch (NoSuchMethodException var8) {
         this.addTest(warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()"));
         return;
      }

      if (!Modifier.isPublic(theClass.getModifiers())) {
         this.addTest(warning("Class " + theClass.getName() + " is not public"));
      } else {
         Class<?> superClass = theClass;

         for(List<String> names = new ArrayList(); Test.class.isAssignableFrom(superClass); superClass = superClass.getSuperclass()) {
            for(Method each : superClass.getDeclaredMethods()) {
               this.addTestMethod(each, names, theClass);
            }
         }

         if (this.fTests.size() == 0) {
            this.addTest(warning("No tests found in " + theClass.getName()));
         }

      }
   }

   public TestSuite(Class<? extends TestCase> theClass, String name) {
      this(theClass);
      this.setName(name);
   }

   public TestSuite(String name) {
      this.fTests = new Vector(10);
      this.setName(name);
   }

   public TestSuite(Class<?>... classes) {
      this.fTests = new Vector(10);

      for(Class<?> each : classes) {
         this.addTest(this.testCaseForClass(each));
      }

   }

   private Test testCaseForClass(Class<?> each) {
      return (Test)(TestCase.class.isAssignableFrom(each) ? new TestSuite(each.asSubclass(TestCase.class)) : warning(each.getCanonicalName() + " does not extend TestCase"));
   }

   public TestSuite(Class<? extends TestCase>[] classes, String name) {
      this(classes);
      this.setName(name);
   }

   public void addTest(Test test) {
      this.fTests.add(test);
   }

   public void addTestSuite(Class<? extends TestCase> testClass) {
      this.addTest(new TestSuite(testClass));
   }

   public int countTestCases() {
      int count = 0;

      for(Test each : this.fTests) {
         count += each.countTestCases();
      }

      return count;
   }

   public String getName() {
      return this.fName;
   }

   public void run(TestResult result) {
      for(Test each : this.fTests) {
         if (result.shouldStop()) {
            break;
         }

         this.runTest(each, result);
      }

   }

   public void runTest(Test test, TestResult result) {
      test.run(result);
   }

   public void setName(String name) {
      this.fName = name;
   }

   public Test testAt(int index) {
      return (Test)this.fTests.get(index);
   }

   public int testCount() {
      return this.fTests.size();
   }

   public Enumeration<Test> tests() {
      return this.fTests.elements();
   }

   public String toString() {
      return this.getName() != null ? this.getName() : super.toString();
   }

   private void addTestMethod(Method m, List<String> names, Class<?> theClass) {
      String name = m.getName();
      if (!names.contains(name)) {
         if (!this.isPublicTestMethod(m)) {
            if (this.isTestMethod(m)) {
               this.addTest(warning("Test method isn't public: " + m.getName() + "(" + theClass.getCanonicalName() + ")"));
            }

         } else {
            names.add(name);
            this.addTest(createTest(theClass, name));
         }
      }
   }

   private boolean isPublicTestMethod(Method m) {
      return this.isTestMethod(m) && Modifier.isPublic(m.getModifiers());
   }

   private boolean isTestMethod(Method m) {
      return m.getParameterTypes().length == 0 && m.getName().startsWith("test") && m.getReturnType().equals(Void.TYPE);
   }
}
