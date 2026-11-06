package org.junit.rules;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/** @deprecated */
@Deprecated
public interface MethodRule {
   Statement apply(Statement var1, FrameworkMethod var2, Object var3);
}
