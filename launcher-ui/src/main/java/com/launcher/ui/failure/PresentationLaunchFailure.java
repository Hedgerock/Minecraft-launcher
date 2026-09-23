package com.launcher.ui.failure;

import java.util.Objects;

public record PresentationLaunchFailure(
        String message
) {

    public PresentationLaunchFailure {
        Objects.requireNonNull(message, "message");

        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
