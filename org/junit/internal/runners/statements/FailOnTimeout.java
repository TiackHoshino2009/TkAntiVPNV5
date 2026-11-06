package org.junit.internal.runners.statements;

import org.junit.runners.model.Statement;

public class FailOnTimeout extends Statement {
   private final Statement fOriginalStatement;
   private final long fTimeout;

   public FailOnTimeout(Statement originalStatement, long timeout) {
      this.fOriginalStatement = originalStatement;
      this.fTimeout = timeout;
   }

   public void evaluate() throws Throwable {
      StatementThread thread = this.evaluateStatement();
      if (!thread.fFinished) {
         this.throwExceptionForUnfinishedThread(thread);
      }

   }

   private StatementThread evaluateStatement() throws InterruptedException {
      StatementThread thread = new StatementThread(this.fOriginalStatement);
      thread.start();
      thread.join(this.fTimeout);
      thread.interrupt();
      return thread;
   }

   private void throwExceptionForUnfinishedThread(StatementThread thread) throws Throwable {
      if (thread.fExceptionThrownByOriginalStatement != null) {
         throw thread.fExceptionThrownByOriginalStatement;
      } else {
         this.throwTimeoutException(thread);
      }
   }

   private void throwTimeoutException(StatementThread thread) throws Exception {
      Exception exception = new Exception(String.format("test timed out after %d milliseconds", this.fTimeout));
      exception.setStackTrace(thread.getStackTrace());
      throw exception;
   }

   private static class StatementThread extends Thread {
      private final Statement fStatement;
      private boolean fFinished = false;
      private Throwable fExceptionThrownByOriginalStatement = null;

      public StatementThread(Statement statement) {
         this.fStatement = statement;
      }

      public void run() {
         try {
            this.fStatement.evaluate();
            this.fFinished = true;
         } catch (InterruptedException var2) {
         } catch (Throwable e) {
            this.fExceptionThrownByOriginalStatement = e;
         }

      }
   }
}
