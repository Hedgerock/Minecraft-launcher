package com.launcher.model.manifest.natives;

import com.launcher.model.runtime.OperatingSystem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryNativesMetadataTest {

    @Test
    void should_reject_null_operating_system_when_resolving_classifier() {
        //given
        Map<OperatingSystem, String> classifiers = Map.of(
                OperatingSystem.LINUX, "classifier-for-linux"
        );

        LibraryNativesMetadata libraryNativesMetadata = new LibraryNativesMetadata(classifiers);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> libraryNativesMetadata.classifierFor(null)
        );

        assertTrue(exception.getMessage().contains("operatingSystem"));
    }

    @Test
    void should_return_empty_classifier_when_operating_system_is_not_mapped() {
        //given
        Map<OperatingSystem, String> classifiers = Map.of(
                OperatingSystem.LINUX, "classifier-for-linux"
        );

        LibraryNativesMetadata libraryNativesMetadata = new LibraryNativesMetadata(classifiers);

        //when
        Optional<String> result = libraryNativesMetadata.classifierFor(OperatingSystem.WINDOWS);

        //then
        assertFalse(result.isPresent());
    }

    @Test
    void should_return_classifier_for_operating_system() {
        //given
        Map<OperatingSystem, String> classifiers = Map.of(
                OperatingSystem.WINDOWS, "classifier-for-windows",
                OperatingSystem.LINUX, "classifier-for-linux"
        );

        LibraryNativesMetadata libraryNativesMetadata = new LibraryNativesMetadata(classifiers);

        //when
        Optional<String> result = libraryNativesMetadata.classifierFor(OperatingSystem.WINDOWS);

        //then
        assertTrue(result.isPresent());
        assertEquals("classifier-for-windows", result.get());
    }

    @Test
    void should_reject_mutation_of_classifiers_from_accessor() {
        //given
        Map<OperatingSystem, String> classifiers = new HashMap<>();

        classifiers.put(OperatingSystem.WINDOWS, "classifier-for-windows");

        LibraryNativesMetadata libraryNativesMetadata = new LibraryNativesMetadata(classifiers);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> libraryNativesMetadata.classifiers().put(OperatingSystem.LINUX, "classifier-for-linux")
        );
    }

    @Test
    void should_create_immutable_classifiers() {
        //given
        Map<OperatingSystem, String> classifiers = new HashMap<>();

        classifiers.put(OperatingSystem.WINDOWS, "classifier-for-windows");

        LibraryNativesMetadata libraryNativesMetadata = new LibraryNativesMetadata(classifiers);

        //when
        classifiers.put(OperatingSystem.LINUX, "classifier-for-linux");

        //then
        assertEquals(
                Map.of(OperatingSystem.WINDOWS, "classifier-for-windows"),
                libraryNativesMetadata.classifiers()
        );
    }

    @Test
    void should_reject_blank_classifier_name() {
        //given
        Map<OperatingSystem, String> classifiers = new HashMap<>();

        classifiers.put(OperatingSystem.WINDOWS, " ");

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LibraryNativesMetadata(classifiers)
        );

        assertTrue(exception.getMessage().contains("classifierName must not be blank"));
    }

    @Test
    void should_reject_null_classifier_name() {
        //given
        Map<OperatingSystem, String> classifiers = new HashMap<>();

        classifiers.put(OperatingSystem.WINDOWS, null);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LibraryNativesMetadata(classifiers)
        );

        assertTrue(exception.getMessage().contains("classifierName"));
    }

    @Test
    void should_reject_null_operating_system() {
        //given
        Map<OperatingSystem, String> classifiers = new HashMap<>();

        classifiers.put(null, "classifier");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LibraryNativesMetadata(classifiers)
        );

        assertTrue(exception.getMessage().contains("operatingSystem"));
    }

    @Test
    void should_allow_empty_classifiers() {
        //given & when
        LibraryNativesMetadata libraryNativesMetadata = new LibraryNativesMetadata(Map.of());

        //then
        assertTrue(libraryNativesMetadata.isEmpty());
    }

    @Test
    void should_reject_null_classifiers() {

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LibraryNativesMetadata(null)
        );

        assertTrue(exception.getMessage().contains("classifiers"));

    }

}
