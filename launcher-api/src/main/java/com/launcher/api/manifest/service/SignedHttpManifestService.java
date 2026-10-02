package com.launcher.api.manifest.service;

import com.launcher.api.manifest.client.ManifestClient;
import com.launcher.api.manifest.client.ManifestSignatureClient;
import com.launcher.api.manifest.mapper.ManifestMapper;
import com.launcher.api.manifest.signature.ManifestSignatureVerifier;
import com.launcher.core.manifest.ManifestService;
import com.launcher.model.manifest.ManifestLoadResult;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class SignedHttpManifestService implements ManifestService {
    private final ManifestClient manifestClient;
    private final ManifestSignatureClient signatureClient;
    private final ManifestSignatureVerifier signatureVerifier;
    private final ManifestMapper manifestMapper;

    public SignedHttpManifestService(
            ManifestClient manifestClient,
            ManifestSignatureClient signatureClient,
            ManifestSignatureVerifier signatureVerifier,
            ManifestMapper manifestMapper
    ) {
        this.manifestClient = Objects.requireNonNull(
                manifestClient,
                "manifestClient"
        );
        this.signatureClient = Objects.requireNonNull(
                signatureClient,
                "signatureClient"
        );
        this.signatureVerifier = Objects.requireNonNull(
                signatureVerifier,
                "signatureVerifier"
        );
        this.manifestMapper = Objects.requireNonNull(
                manifestMapper,
                "manifestMapper"
        );
    }

    @Override
    public ManifestLoadResult loadManifest() {
        byte[] manifestBytes = manifestClient.downloadBytes();
        byte[] signatureBytes = signatureClient.download();

        signatureVerifier.verify(manifestBytes, signatureBytes);

        CharsetDecoder decoder = StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try {
            String manifestJson = decoder
                    .decode(ByteBuffer.wrap(manifestBytes))
                    .toString();

            return manifestMapper.map(manifestJson);
        } catch (CharacterCodingException e) {
            throw new ManifestDecodingException(
                    "Failed to decode manifest",
                    e
            );
        }
    }
}
