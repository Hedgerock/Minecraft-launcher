package com.launcher.publisher;

import com.launcher.api.manifest.signature.Ed25519ManifestSignatureVerifier;
import com.launcher.publisher.support.ManifestSignerFixture;
import com.launcher.publisher.support.Pkcs12KeyStoreFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestPublisherCommandTest {
    private static final String ALIAS = "keystone-smoke";
    private static final char[] PASSWORD = "test-password".toCharArray();

    private final LocalManifestPublicationPreparer preparer = ManifestSignerFixture.PREPARER;
    private final ManifestSignerFixture fixture = new ManifestSignerFixture();

    @Test
    void should_create_matching_signature_and_public_key_when_commands_are_executed(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        Path keyStoreFile = tempDir.resolve("signing.p12");
        Path manifestFile = tempDir.resolve("manifest.json");
        Path signatureFile = tempDir.resolve("manifest.sig");
        Path publicKeyFile = tempDir.resolve("public-key.der");

        Pkcs12KeyStoreFixture.create(
                keyStoreFile,
                ALIAS,
                PASSWORD
        );

        byte[] manifestBytes = fixture.getManifestBytes();

        Files.write(manifestFile, manifestBytes);

        ManifestPublisherCommand signCommand =
                ManifestPublisherCommand.parse(
                        new String[] {
                                "sign",
                                keyStoreFile.toString(),
                                ALIAS,
                                manifestFile.toString(),
                                signatureFile.toString(),
                        }
                );

        ManifestPublisherCommand exportCommand =
                ManifestPublisherCommand.parse(
                        new String[] {
                                "export-public-key",
                                keyStoreFile.toString(),
                                ALIAS,
                                publicKeyFile.toString()
                        }
                );

        //when
        signCommand.execute(preparer, PASSWORD);
        exportCommand.execute(preparer, PASSWORD);

        //then
        assertTrue(Files.isRegularFile(signatureFile));
        assertTrue(Files.isRegularFile(publicKeyFile));
        assertArrayEquals(
                manifestBytes,
                Files.readAllBytes(manifestFile)
        );

        byte[] signatureBytes = Files.readAllBytes(signatureFile);
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile);

        PublicKey restoredPublicKey = KeyFactory
                .getInstance("Ed25519")
                .generatePublic(
                        new X509EncodedKeySpec(publicKeyBytes)
                );

        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(restoredPublicKey);

        assertDoesNotThrow(
                () -> verifier.verify(manifestBytes, signatureBytes)
        );
    }

    @Test
    void should_reject_unknown_command() {
        //given
        String[] args = {
                "unknown",
                "signing.p12",
                ALIAS
        };

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ManifestPublisherCommand.parse(args)
        );

        //then
        assertTrue(
                exception.getMessage().startsWith("Usage:")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentCounts")
    void should_reject_missing_or_extra_arguments(String[] args) {
        //given
        // Arguments supplied by MethodSource

        //when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ManifestPublisherCommand.parse(args)
        );

        //then
        assertTrue(
                exception.getMessage().startsWith("Usage:")
        );
    }

    @ParameterizedTest
    @MethodSource("blankArguments")
    void should_reject_blank_arguments(String[] args) {
        //given
        // Arguments supplied by MethodSource

        //when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ManifestPublisherCommand.parse(args)
        );

        //then
        assertTrue(
                exception.getMessage().endsWith("must be non-blank")
        );
    }

    private static Stream<Arguments> blankArguments() {
        String keyStoreFile = "signing.p12";
        String manifestFile = "manifest.json";
        String signatureFile = "manifest.sig";
        String publicKeyFile = "public-key.der";

        return Stream.of(
                Arguments.of((Object) new String[] {
                        "sign",
                        " ",
                        ALIAS,
                        manifestFile,
                        signatureFile,
                }),
                Arguments.of((Object) new String[] {
                        "sign",
                        keyStoreFile,
                        " ",
                        manifestFile,
                        signatureFile,
                }),
                Arguments.of((Object) new String[] {
                        "sign",
                        keyStoreFile,
                        ALIAS,
                        " ",
                        signatureFile,
                }),
                Arguments.of((Object) new String[] {
                        "sign",
                        keyStoreFile,
                        ALIAS,
                        manifestFile,
                        " ",
                }),
                Arguments.of((Object) new String[] {
                        "export-public-key",
                        " ",
                        ALIAS,
                        publicKeyFile
                }),
                Arguments.of((Object) new String[] {
                        "export-public-key",
                        keyStoreFile,
                        " ",
                        publicKeyFile
                }),
                Arguments.of((Object) new String[] {
                        "export-public-key",
                        keyStoreFile,
                        ALIAS,
                        " "
                })
        );
    }

    private static Stream<Arguments> invalidArgumentCounts() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{
                        "sign"
                }),
                Arguments.of((Object) new String[]{
                        "sign",
                        "signing.p12",
                        ALIAS,
                        "manifest.json"
                }),
                Arguments.of((Object) new String[]{
                        "sign",
                        "signing.p12",
                        ALIAS,
                        "manifest.json",
                        "manifest.sig",
                        "password-in-command-line"
                }),
                Arguments.of((Object) new String[]{
                        "export-public-key"
                }),
                Arguments.of((Object) new String[]{
                        "export-public-key",
                        "signing.p12",
                        ALIAS
                }),
                Arguments.of((Object) new String[]{
                        "export-public-key",
                        "signing.p12",
                        ALIAS,
                        "public-key.der",
                        "password-in-command-line"
                })
        );
    }
}
