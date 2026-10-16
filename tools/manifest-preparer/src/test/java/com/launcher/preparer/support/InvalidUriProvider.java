package com.launcher.preparer.support;

import org.junit.jupiter.params.provider.Arguments;

import java.net.URI;
import java.util.stream.Stream;


public class InvalidUriProvider {

    public static Stream<Arguments> invalidUris() {
        return Stream.of(
                Arguments.of(
                        "non absolute uri",
                        getUri("/content/value")
                ),
                Arguments.of(
                        "non https",
                        getUri("http://localhost/content-value.jar")
                ),
                Arguments.of(
                        "without host",
                        getUri("https:/example.org/value.jar")
                ),
                Arguments.of(
                        "with raw user info",
                        getUri("https://admin%40global:p%25ss@example.com")
                ),
                Arguments.of(
                        "with raw fragment",
                        getUri("https://example.com#content%20value")
                )
        );
    }

    private static URI getUri(String value) {
        return URI.create(value);
    }
}

