package com.launcher.api.manifest.service;

import com.launcher.api.manifest.mapper.ManifestMapper;
import com.launcher.api.manifest.signature.ManifestSignatureVerificationException;
import com.launcher.api.manifest.signature.ManifestSignatureVerifier;
import com.launcher.api.manifest.support.FailingManifestClient;
import com.launcher.api.manifest.support.FailingManifestSignatureVerifier;
import com.launcher.api.manifest.support.FailingManifestSignatureClient;
import com.launcher.api.manifest.support.ManifestJsonProvider;
import com.launcher.api.manifest.support.RecordingManifestClient;
import com.launcher.api.manifest.support.RecordingManifestMapper;
import com.launcher.api.manifest.support.RecordingManifestSignatureClient;
import com.launcher.api.manifest.support.RecordingManifestSignatureVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignedHttpManifestServiceTest {
    private RecordingManifestClient manifestClient;
    private RecordingManifestSignatureClient manifestSignatureClient;
    private RecordingManifestSignatureVerifier manifestSignatureVerifier;
    private RecordingManifestMapper manifestMapper;

    @BeforeEach
    void setUp() {
        manifestClient = new RecordingManifestClient(
                getManifestBytes("1.12.2")
        );
        manifestSignatureClient = new RecordingManifestSignatureClient(new byte[]{1});
        manifestSignatureVerifier = new RecordingManifestSignatureVerifier();
        manifestMapper = new RecordingManifestMapper();
    }

    @Test
    void should_map_manifest_only_after_signature_verification_passed() {
        //given
        AtomicBoolean verified = new AtomicBoolean();
        AtomicBoolean mapped = new AtomicBoolean();

        ManifestSignatureVerifier signatureVerifier =
                ((manifestBytes, signatureBytes) -> verified.set(true));

        ManifestMapper mapper = manifestJson -> {
            assertTrue(
                    verified.get(),
                    "Manifest must be verified before mapping"
            );

            mapped.set(true);
            return null;
        };

        SignedHttpManifestService service =
                new SignedHttpManifestService(
                        manifestClient,
                        manifestSignatureClient,
                        signatureVerifier,
                        mapper
                );

        //when
        service.loadManifest();

        //then
        assertTrue(mapped.get());
    }

    @Test
    void should_map_manifest_when_signature_verification_passed() {
        //given
        SignedHttpManifestService signedHttpManifestService = new SignedHttpManifestService(
                manifestClient,
                manifestSignatureClient,
                manifestSignatureVerifier,
                manifestMapper
        );

        //when
        signedHttpManifestService.loadManifest();

        //then
        assertArrayEquals(
                manifestClient.downloadBytes(),
                manifestSignatureVerifier.getManifestBytes()
        );

        assertArrayEquals(
                manifestSignatureClient.download(),
                manifestSignatureVerifier.getSignatureBytes()
        );

        assertEquals(
                ManifestJsonProvider.getMinimumValidManifestJson("1.12.2"),
                manifestMapper.getJson()
        );

        assertTrue(manifestMapper.wasCalled());
    }

    @Test
    void should_not_map_manifest_when_signature_verification_failed() {
        //given
        ManifestSignatureVerificationException failure =
                new ManifestSignatureVerificationException(
                        "Manifest signature is invalid"
                );

        FailingManifestSignatureVerifier signatureVerifier =
                new FailingManifestSignatureVerifier(failure);

        SignedHttpManifestService service = new SignedHttpManifestService(
                new RecordingManifestClient(
                        getManifestBytes("1.7.10")
                ),
                manifestSignatureClient,
                signatureVerifier,
                manifestMapper
        );

        //when & then
        ManifestSignatureVerificationException exception = assertThrows(
                ManifestSignatureVerificationException.class,
                service::loadManifest
        );

        assertSame(failure, exception);
        assertFalse(manifestMapper.wasCalled());
    }

    @Test
    void should_stop_loading_when_signature_download_failed() {
        //given
        RuntimeException failure =
                new RuntimeException("Signature download failed");

        SignedHttpManifestService manifestService =
                new SignedHttpManifestService(
                        manifestClient,
                        new FailingManifestSignatureClient(failure),
                        manifestSignatureVerifier,
                        manifestMapper
                );

        //when & then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                manifestService::loadManifest
        );

        //then
        assertSame(failure, exception);
        assertFalse(manifestSignatureVerifier.wasCalled());
        assertFalse(manifestMapper.wasCalled());
    }

    @Test
    void should_stop_loading_verifying_and_mapping_when_manifest_client_failed() {
        //given
        RuntimeException failure = new RuntimeException("Something went wrong");

        FailingManifestClient failingManifestClient =
                new FailingManifestClient(failure);


        SignedHttpManifestService signedHttpManifestService = new SignedHttpManifestService(
                failingManifestClient,
                manifestSignatureClient,
                manifestSignatureVerifier,
                manifestMapper
        );

        //when & then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                signedHttpManifestService::loadManifest
        );

        assertSame(failure, exception);

        assertFalse(manifestSignatureClient.wasCalled());
        assertFalse(manifestSignatureVerifier.wasCalled());
        assertFalse(manifestMapper.wasCalled());
    }

    @Test
    void should_fail_without_mapping_when_manifest_contains_invalid_utf8() {
        //given
        byte[] invalidManifestBytes = {
                (byte) 0xC3,
                (byte) 0x28
        };

        SignedHttpManifestService signedHttpManifestService = new SignedHttpManifestService(
                new RecordingManifestClient(invalidManifestBytes),
                manifestSignatureClient,
                manifestSignatureVerifier,
                manifestMapper
        );

        //when & then
        ManifestDecodingException exception = assertThrows(
                ManifestDecodingException.class,
                signedHttpManifestService::loadManifest
        );

        assertEquals(
                "Failed to decode manifest",
                exception.getMessage()
        );

        assertInstanceOf(
                CharacterCodingException.class,
                exception.getCause()
        );

        assertFalse(manifestMapper.wasCalled());
    }

    @Test
    void should_reject_null_manifest_mapper() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SignedHttpManifestService(
                        manifestClient,
                        manifestSignatureClient,
                        manifestSignatureVerifier,
                        null
                )
        );

        assertEquals(
                "manifestMapper",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_manifest_signature_verifier() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SignedHttpManifestService(
                        manifestClient,
                        manifestSignatureClient,
                        null,
                        manifestMapper
                )
        );

        assertEquals(
                "signatureVerifier",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_manifest_signature_client() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SignedHttpManifestService(
                        manifestClient,
                        null,
                        manifestSignatureVerifier,
                        manifestMapper
                )
        );

        assertEquals(
                "signatureClient",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_manifest_client() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SignedHttpManifestService(
                        null,
                        manifestSignatureClient,
                        manifestSignatureVerifier,
                        manifestMapper
                )
        );

        assertEquals(
                "manifestClient",
                exception.getMessage()
        );
    }

    private byte[] getManifestBytes(String version) {
        return ManifestJsonProvider.getMinimumValidManifestJson(version)
                .getBytes(StandardCharsets.UTF_8);
    }
}
