package com.launcher.api.manifest.service;

import com.launcher.api.manifest.support.fixture.ManifestLoadResultFixture;
import com.launcher.model.manifest.ManifestLoadResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManagedResourceUriValidatorTest {
    private final ManagedResourceUriValidator validator = new ManagedResourceUriValidator();
    private final ManifestLoadResultFixture manifestLoadResultFixture = new ManifestLoadResultFixture();

    @Test
    void should_reject_invalid_uri() {
        //given
        ManifestLoadResult manifestLoadResult =
                manifestLoadResultFixture.loadManifestWithFailingAssetsIndex();

        //when & then
        ManagedResourceUriValidationException exception = assertThrows(
                ManagedResourceUriValidationException.class,
                () -> validator.validate(manifestLoadResult)
        );

        assertEquals(
                "Resource URI is invalid",
                exception.getMessage()
        );

        assertEquals(
                "asset-entry/org/value.jar",
                exception.getResourcePath()
        );
    }

    @Test
    void should_reject_fragment() {
        //given
        ManifestLoadResult manifestLoadResult =
                manifestLoadResultFixture.loadManifestWithFragment();

        //when & then
        ManagedResourceUriValidationException exception = assertThrows(
                ManagedResourceUriValidationException.class,
                () -> validator.validate(manifestLoadResult)
        );


        assertEquals(
                "Resource URI must not contain a fragment",
                exception.getMessage()
        );

        assertEquals(
                "natives/org/example/example.jar",
                exception.getResourcePath()
        );
    }

    @Test
    void should_reject_raw_user_info() {
        //given
        ManifestLoadResult manifestLoadResult =
                manifestLoadResultFixture.loadManifestWithRawUserInfo();

        //when & then
        ManagedResourceUriValidationException exception = assertThrows(
                ManagedResourceUriValidationException.class,
                () -> validator.validate(manifestLoadResult)
        );


        assertEquals(
                "Resource URI must not contain user info",
                exception.getMessage()
        );

        assertEquals(
                "test-path",
                exception.getResourcePath()
        );
    }

    @Test
    void should_reject_when_host_is_missing() {
        //given
        ManifestLoadResult manifestLoadResult =
                manifestLoadResultFixture.loadManifestWithoutHost();

        //when & then
        ManagedResourceUriValidationException exception = assertThrows(
                ManagedResourceUriValidationException.class,
                () -> validator.validate(manifestLoadResult)
        );


        assertEquals(
                "Resource URI must contain a host",
                exception.getMessage()
        );

        assertEquals(
                "libraries/org/example/example.jar",
                exception.getResourcePath()
        );
    }

    @Test
    void should_reject_uri_non_absolute_path() {
        //given
        ManifestLoadResult manifestLoadResult =
                manifestLoadResultFixture.loadManifestWithNonAbsoluteUri();

        //when & then
        ManagedResourceUriValidationException exception = assertThrows(
                ManagedResourceUriValidationException.class,
                () -> validator.validate(manifestLoadResult)
        );


        assertEquals(
                "Resource URI must be absolute",
                exception.getMessage()
        );

        assertEquals(
                "natives/org/example/example.jar",
                exception.getResourcePath()
        );
    }

    @Test
    void should_reject_missing_uri_scheme() {
        //given
        ManifestLoadResult manifestLoadResult =
                manifestLoadResultFixture.loadManifestWithoutUriScheme();

        //when & then
        ManagedResourceUriValidationException exception = assertThrows(
                ManagedResourceUriValidationException.class,
                () -> validator.validate(manifestLoadResult)
        );


        assertEquals(
                "Resource URI must be absolute",
                exception.getMessage()
        );

        assertEquals(
                "test-path",
                exception.getResourcePath()
        );
    }

    @Test
    void should_reject_uri_with_invalid_scheme_and_ignore_sensitive_information() {
        //given
        ManifestLoadResult manifestLoadResult =
                manifestLoadResultFixture.loadManifestWithNonHttpsScheme();

        //when & then
        ManagedResourceUriValidationException exception = assertThrows(
                ManagedResourceUriValidationException.class,
                () -> validator.validate(manifestLoadResult)
        );

        assertEquals(
                "Resource URI must use HTTPS",
                exception.getMessage()
        );

        assertEquals(
                "libraries/org/example/example.jar",
                exception.getResourcePath()
        );
    }

    @Test
    void should_not_throw_for_valid_manifest_load_result() {
        //when & then
        assertDoesNotThrow(() ->
                validator.validate(manifestLoadResultFixture.loadManifest())
        );
    }

    @Test
    void should_reject_null_manifest_load_result() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> validator.validate(null)
        );

        assertEquals(
                "manifestLoadResult",
                exception.getMessage()
        );
    }
}
