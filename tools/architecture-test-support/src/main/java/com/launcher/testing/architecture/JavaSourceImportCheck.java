package com.launcher.testing.architecture;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface JavaSourceImportCheck {

    List<String> findForbiddenImports(String source) throws IOException;
}
