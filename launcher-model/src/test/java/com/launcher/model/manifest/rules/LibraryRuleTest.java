package com.launcher.model.manifest.rules;

import com.launcher.model.runtime.OperatingSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryRuleTest {

    @Test
    void should_reject_null_operating_system() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LibraryRule(LibraryRuleAction.ALLOW, null)
        );

        assertTrue(exception.getMessage().contains("operatingSystem"));
    }

    @Test
    void should_reject_null_action() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LibraryRule(null, OperatingSystem.WINDOWS)
        );

        assertTrue(exception.getMessage().contains("action"));
    }

    @Test
    void should_create_valid_rule() {
        //given & when
        LibraryRule rule =
                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS);

        //then
        assertEquals(
                LibraryRuleAction.ALLOW,
                rule.action()
        );

        assertEquals(
                OperatingSystem.WINDOWS,
                rule.operatingSystem()
        );
    }

}
