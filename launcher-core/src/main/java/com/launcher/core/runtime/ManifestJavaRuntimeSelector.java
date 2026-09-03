package com.launcher.core.runtime;

import com.launcher.core.runtime.javaexecutable.resolver.JavaExecutableReferenceResolver;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.runtime.JavaExecutableReference;

import java.util.Objects;

public final class ManifestJavaRuntimeSelector implements JavaRuntimeSelector {
    private final JavaExecutableReferenceResolver javaExecutableReferenceResolver;

    public ManifestJavaRuntimeSelector(
            JavaExecutableReferenceResolver javaExecutableReferenceResolver
    ) {
        this.javaExecutableReferenceResolver =
                Objects.requireNonNull(javaExecutableReferenceResolver, "javaExecutableReferenceResolver");
    }

    @Override
    public JavaExecutableReference selectJavaExecutable(LaunchInfo launchInfo) {
        Objects.requireNonNull(launchInfo, "launchInfo");

        return javaExecutableReferenceResolver.resolve(launchInfo.javaExecutable());
    }
}
