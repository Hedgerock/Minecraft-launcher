package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoaderInfoTest {
    @Test
    void should_reject_blank_version() {
        //given
        String type = "test-type";
        String version = " ";

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LoaderInfo(type, version)
        );

        assertTrue(exception.getMessage().contains("version must not be blank"));
    }

    @Test
    void should_reject_null_version() {
        //given
        String type = "test-type";

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LoaderInfo(type, null)
        );

        assertTrue(exception.getMessage().contains("version"));
    }

    @Test
    void should_reject_blank_type() {
        //given
        String type = " ";
        String version = "1.7.10";

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LoaderInfo(type, version)
        );

        assertTrue(exception.getMessage().contains("type must not be blank"));
    }

    @Test
    void should_reject_null_type() {
        //given
        String version = "1.7.10";

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LoaderInfo(null, version)
        );

        assertTrue(exception.getMessage().contains("type"));
    }

}
