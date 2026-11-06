package junit.runner;

public interface TestRunListener {
   int STATUS_ERROR = 1;
   int STATUS_FAILURE = 2;

   void testRunStarted(String var1, int var2);

   void testRunEnded(long var1);

   void testRunStopped(long var1);

   void testStarted(String var1);

   void testEnded(String var1);

   void testFailed(int var1, String var2, String var3);
}
