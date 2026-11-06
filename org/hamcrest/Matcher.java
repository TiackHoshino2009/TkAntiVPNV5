package org.hamcrest;

public interface Matcher<T> extends SelfDescribing {
   boolean matches(Object var1);

   void _dont_implement_Matcher___instead_extend_BaseMatcher_();
}
