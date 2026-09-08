package com.launcher.core.runtime;

import com.launcher.core.runtime.javaexecutable.resolver.JavaExecutableReferenceResolver;
import com.launcher.core.runtime.model.JavaRuntimeSelectionRequest;
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
    public JavaExecutableReference selectJavaExecutable(JavaRuntimeSelectionRequest request) {
        Objects.requireNonNull(request, "request");

        return javaExecutableReferenceResolver.resolve(
                request.launchInfo().javaExecutable()
        );
    }
}
