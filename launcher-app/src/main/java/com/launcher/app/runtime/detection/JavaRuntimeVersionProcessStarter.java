package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaExecutableReference;

import java.io.IOException;

@FunctionalInterface
interface JavaRuntimeVersionProcessStarter {

    Process start(JavaExecutableReference javaExecutableReference) throws IOException;
}
