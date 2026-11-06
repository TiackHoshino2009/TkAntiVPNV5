package junit.framework;

public interface TestListener {
   void addError(Test var1, Throwable var2);

   void addFailure(Test var1, AssertionFailedError var2);

   void endTest(Test var1);

   void startTest(Test var1);
}
