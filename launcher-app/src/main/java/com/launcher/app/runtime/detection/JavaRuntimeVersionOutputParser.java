package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaRuntimeVersion;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JavaRuntimeVersionOutputParser {
    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "\\bversion\\s+\"([^\"]+)\""
    );

    private static final Pattern MAJOR_VERSION_PATTERN = Pattern.compile(
            "^(?:1\\.)?(\\d+)"
    );

    public JavaRuntimeVersion parse(String output) {
        Objects.requireNonNull(output, "output");

        if (output.isBlank()) {
            throw new JavaRuntimeVersionParsingException(
                    "Output cannot be blank"
            );
        }

        Matcher matcher = VERSION_PATTERN.matcher(output);

        if (!matcher.find()) {
            throw new JavaRuntimeVersionParsingException(
                    "Java version cannot be parsed"
            );
        }

        int majorVersion = parseMajorVersion(matcher.group(1));

        return new JavaRuntimeVersion(majorVersion);
    }

    private int parseMajorVersion(String version) {
        Matcher matcher = MAJOR_VERSION_PATTERN.matcher(version.strip());

        if (!matcher.find()) {
            throw new JavaRuntimeVersionParsingException("Java version cannot be parsed");
        }

        return Integer.parseInt(matcher.group(1));
    }

}
