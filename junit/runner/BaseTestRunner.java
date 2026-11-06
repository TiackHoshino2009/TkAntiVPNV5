package junit.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.Properties;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestSuite;

public abstract class BaseTestRunner implements TestListener {
   public static final String SUITE_METHODNAME = "suite";
   private static Properties fPreferences;
   static int fgMaxMessageLength = 500;
   static boolean fgFilterStack = true;
   boolean fLoading = true;

   public synchronized void startTest(Test test) {
      this.testStarted(test.toString());
   }

   protected static void setPreferences(Properties preferences) {
      fPreferences = preferences;
   }

   protected static Properties getPreferences() {
      if (fPreferences == null) {
         fPreferences = new Properties();
         fPreferences.put("loading", "true");
         fPreferences.put("filterstack", "true");
         readPreferences();
      }

      return fPreferences;
   }

   public static void savePreferences() throws IOException {
      FileOutputStream fos = new FileOutputStream(getPreferencesFile());

      try {
         getPreferences().store(fos, "");
      } finally {
         fos.close();
      }

   }

   public static void setPreference(String key, String value) {
      getPreferences().put(key, value);
   }

   public synchronized void endTest(Test test) {
      this.testEnded(test.toString());
   }

   public synchronized void addError(Test test, Throwable t) {
      this.testFailed(1, test, t);
   }

   public synchronized void addFailure(Test test, AssertionFailedError t) {
      this.testFailed(2, test, t);
   }

   public abstract void testStarted(String var1);

   public abstract void testEnded(String var1);

   public abstract void testFailed(int var1, Test var2, Throwable var3);

   public Test getTest(String suiteClassName) {
      if (suiteClassName.length() <= 0) {
         this.clearStatus();
         return null;
      } else {
         Class<?> testClass = null;

         try {
            testClass = this.loadSuiteClass(suiteClassName);
         } catch (ClassNotFoundException e) {
            String clazz = e.getMessage();
            if (clazz == null) {
               clazz = suiteClassName;
            }

            this.runFailed("Class not found \"" + clazz + "\"");
            return null;
         } catch (Exception e) {
            this.runFailed("Error: " + e.toString());
            return null;
         }

         Method suiteMethod = null;

         try {
            suiteMethod = testClass.getMethod("suite");
         } catch (Exception var6) {
            this.clearStatus();
            return new TestSuite(testClass);
         }

         if (!Modifier.isStatic(suiteMethod.getModifiers())) {
            this.runFailed("Suite() method must be static");
            return null;
         } else {
            Test test = null;

            try {
               test = (Test)suiteMethod.invoke((Object)null, new Class[0]);
               if (test == null) {
                  return test;
               }
            } catch (InvocationTargetException e) {
               this.runFailed("Failed to invoke suite():" + e.getTargetException().toString());
               return null;
            } catch (IllegalAccessException e) {
               this.runFailed("Failed to invoke suite():" + e.toString());
               return null;
            }

            this.clearStatus();
            return test;
         }
      }
   }

   public String elapsedTimeAsString(long runTime) {
      return NumberFormat.getInstance().format((double)runTime / (double)1000.0F);
   }

   protected String processArguments(String[] args) {
      String suiteName = null;

      for(int i = 0; i < args.length; ++i) {
         if (args[i].equals("-noloading")) {
            this.setLoading(false);
         } else if (args[i].equals("-nofilterstack")) {
            fgFilterStack = false;
         } else if (args[i].equals("-c")) {
            if (args.length > i + 1) {
               suiteName = this.extractClassName(args[i + 1]);
            } else {
               System.out.println("Missing Test class name");
            }

            ++i;
         } else {
            suiteName = args[i];
         }
      }

      return suiteName;
   }

   public void setLoading(boolean enable) {
      this.fLoading = enable;
   }

   public String extractClassName(String className) {
      return className.startsWith("Default package for") ? className.substring(className.lastIndexOf(".") + 1) : className;
   }

   public static String truncate(String s) {
      if (fgMaxMessageLength != -1 && s.length() > fgMaxMessageLength) {
         s = s.substring(0, fgMaxMessageLength) + "...";
      }

      return s;
   }

   protected abstract void runFailed(String var1);

   protected Class<?> loadSuiteClass(String suiteClassName) throws ClassNotFoundException {
      return Class.forName(suiteClassName);
   }

   protected void clearStatus() {
   }

   protected boolean useReloadingTestSuiteLoader() {
      return getPreference("loading").equals("true") && this.fLoading;
   }

   private static File getPreferencesFile() {
      String home = System.getProperty("user.home");
      return new File(home, "junit.properties");
   }

   private static void readPreferences() {
      InputStream is = null;

      try {
         InputStream var5 = new FileInputStream(getPreferencesFile());
         setPreferences(new Properties(getPreferences()));
         getPreferences().load(var5);
      } catch (IOException var4) {
         try {
            if (is != null) {
               is.close();
            }
         } catch (IOException var3) {
         }
      }

   }

   public static String getPreference(String key) {
      return getPreferences().getProperty(key);
   }

   public static int getPreference(String key, int dflt) {
      String value = getPreference(key);
      int intValue = dflt;
      if (value == null) {
         return dflt;
      } else {
         try {
            intValue = Integer.parseInt(value);
         } catch (NumberFormatException var5) {
         }

         return intValue;
      }
   }

   public static String getFilteredTrace(Throwable t) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(stringWriter);
      t.printStackTrace(writer);
      StringBuffer buffer = stringWriter.getBuffer();
      String trace = buffer.toString();
      return getFilteredTrace(trace);
   }

   public static String getFilteredTrace(String stack) {
      if (showStackRaw()) {
         return stack;
      } else {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         StringReader sr = new StringReader(stack);
         BufferedReader br = new BufferedReader(sr);

         String line;
         try {
            while((line = br.readLine()) != null) {
               if (!filterLine(line)) {
                  pw.println(line);
               }
            }
         } catch (Exception var7) {
            return stack;
         }

         return sw.toString();
      }
   }

   protected static boolean showStackRaw() {
      return !getPreference("filterstack").equals("true") || !fgFilterStack;
   }

   static boolean filterLine(String line) {
      String[] patterns = new String[]{"junit.framework.TestCase", "junit.framework.TestResult", "junit.framework.TestSuite", "junit.framework.Assert.", "junit.swingui.TestRunner", "junit.awtui.TestRunner", "junit.textui.TestRunner", "java.lang.reflect.Method.invoke("};

      for(int i = 0; i < patterns.length; ++i) {
         if (line.indexOf(patterns[i]) > 0) {
            return true;
         }
      }

      return false;
   }

   static {
      fgMaxMessageLength = getPreference("maxmessage", fgMaxMessageLength);
   }
}
