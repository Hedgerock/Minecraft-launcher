package com.launcher.game.support;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class GameProcessTestApplication {

    private GameProcessTestApplication() {
    }

    public static void main(String[] args) throws IOException {
        switch (args[0]) {
            case "stdout" -> writeOutput(System.out);
            case "stderr" -> writeOutput(System.err);
            case "marker" -> Files.writeString(
                    Path.of(args[1]),
                    "started",
                    StandardCharsets.UTF_8
            );
            default -> throw new IllegalStateException(
                    "Unknown test mode: " + args[0]
            );
        }
    }

    private static void writeOutput(OutputStream outputStream) throws IOException {
        byte[] block = new byte[1024];
        Arrays.fill(block, (byte) 'x');
        block[block.length - 1] = (byte) '\n';

        for (int index = 0; index < 256; index++) {
            outputStream.write(block);
        }

        outputStream.flush();
    }
}
