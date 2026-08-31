package com.launcher.model.manifest.natives;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeExtractionRulesTest {

    @Test
    void should_accept_empty_excludes() {
        //given & when
        NativeExtractionRules rules = new NativeExtractionRules(List.of());

        //then
        assertTrue(rules.isEmpty());
    }

    @Test
    void should_reject_excludes_mutation_accessor() {
        //given
        List<String> excludes = new ArrayList<>();
        excludes.add("test-exclude");

        NativeExtractionRules rules = new NativeExtractionRules(excludes);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> rules.excludes().add("new-exclude")
        );
    }

    @Test
    void should_create_immutable_excludes() {
        //given
        List<String> excludes = new ArrayList<>();
        excludes.add("test-exclude");

        NativeExtractionRules rules = new NativeExtractionRules(excludes);

        //when
        excludes.add("new-exclude");

        //then
        assertEquals(
                List.of("test-exclude"),
                rules.excludes()
        );
    }

    @Test
    void should_reject_null_exclude_in_excludes() {
        //given
        List<String> excludes = new ArrayList<>();
        excludes.add(null);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new NativeExtractionRules(excludes)
        );

        assertTrue(exception.getMessage().contains("exclude"));
    }

    @Test
    void should_reject_null_excludes() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new NativeExtractionRules(null)
        );

        assertTrue(exception.getMessage().contains("excludes"));
    }

}
