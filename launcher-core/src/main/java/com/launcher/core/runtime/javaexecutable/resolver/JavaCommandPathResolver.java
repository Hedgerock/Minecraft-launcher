package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.model.runtime.JavaExecutableReference;

public interface JavaCommandPathResolver {

    JavaExecutableReference resolve(JavaExecutableReference javaExecutableReference);

}
