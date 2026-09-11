package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaExecutableReference;

interface JavaRuntimeVersionCommandRunner {

    JavaRuntimeVersionCommandResult run(JavaExecutableReference javaExecutableReference);
}
