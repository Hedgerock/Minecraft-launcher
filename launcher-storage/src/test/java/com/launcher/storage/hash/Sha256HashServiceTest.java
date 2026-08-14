package com.launcher.storage.hash;

import com.launcher.core.storage.exception.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class Sha256HashServiceTest {

    private final Sha256HashService hashService = new Sha256HashService();

    @Test
    void should_throw_storage_exception_when_file_does_not_exist(@TempDir Path tempDir) {
        //given
        Path file = tempDir.resolve("non-existing-file.txt");

        //when
        StorageException exception = assertThrows(
                StorageException.class,
                () -> hashService.sha256(file)
        );

        //then
        assertTrue(exception.getMessage().contains("Unable to calculate SHA-256 hash for file: " + file));

    }

    @Test
    void should_return_different_hash_for_different_content(@TempDir Path tempDir) throws IOException {
        //given
        Path firstFile = tempDir.resolve("first-file.txt");
        Path secondFile = tempDir.resolve("second-file.txt");

        Files.writeString(firstFile, "Hello test!");
        Files.writeString(secondFile, "Hello not for test!");

        //when
        String firstHash = hashService.sha256(firstFile);
        String secondHash = hashService.sha256(secondFile);

        //then
        assertNotEquals(firstHash, secondHash);

    }

    @Test
    void should_return_same_hash_for_same_content(@TempDir Path tempDir) throws IOException {
        //given
        Path firstFile = tempDir.resolve("first-file.txt");
        Path secondFile = tempDir.resolve("second-file.txt");

        Files.writeString(firstFile, "Hello test!");
        Files.writeString(secondFile, "Hello test!");

        //when
        String firstHash = hashService.sha256(firstFile);
        String secondHash = hashService.sha256(secondFile);

        //then
        assertEquals(firstHash, secondHash);
    }

    @Test
    void should_calculate_sha256_hash() {
        //given
        Path file = Path.of("src/test/resources/test-file.txt");

        //when
        String hash = hashService.sha256(file);

        //then
        assertEquals(
                "5189f11bf923a205cbbfa764fe12656474279aa04c61bec612c0d4e8daf6e4a7",
                hash
        );

    }


}
