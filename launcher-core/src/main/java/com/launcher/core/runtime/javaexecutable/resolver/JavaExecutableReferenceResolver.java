package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.model.runtime.JavaExecutableReference;

public interface JavaExecutableReferenceResolver {

    JavaExecutableReference resolve(String javaExecutable);

}
