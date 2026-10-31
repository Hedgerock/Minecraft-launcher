package com.launcher.preparer.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

public final class CloseTrackingInputStream extends ByteArrayInputStream {

    private final IOException closeFailure;

    private boolean closeCalled;
    private boolean closeSuccessful;

    public CloseTrackingInputStream(byte[] content) {
        this(content, null);
    }

    public CloseTrackingInputStream(
            byte[] content,
            IOException closeFailure
    ) {
        super(Objects.requireNonNull(content, "content"));
        this.closeFailure = closeFailure;
    }

    public boolean wasCloseCalled() {
        return closeCalled;
    }

    public boolean wasCloseSuccessful() {
        return closeSuccessful;
    }

    @Override
    public void close() throws IOException {
        closeCalled = true;

        if (closeFailure != null) {
            throw closeFailure;
        }

        super.close();
        closeSuccessful = true;
    }
}
