package com.launcher.core.runtime.javaexecutable.resolver.provider;

import java.nio.file.Path;

@FunctionalInterface
interface PathParser {

    Path parse(String path);

}
