package org.hamcrest;

public interface Description {
   Description appendText(String var1);

   Description appendDescriptionOf(SelfDescribing var1);

   Description appendValue(Object var1);

   <T> Description appendValueList(String var1, String var2, String var3, T... var4);

   <T> Description appendValueList(String var1, String var2, String var3, Iterable<T> var4);

   Description appendList(String var1, String var2, String var3, Iterable<? extends SelfDescribing> var4);
}
