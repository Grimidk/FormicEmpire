package com.grimidk.formicempire.classes.infrasctructure.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GamePathsTest {

    @TempDir
    Path tempDir;

    private String previousSavesDir;
    private String previousUserDataDir;

    @BeforeEach
    void captureProperties() {
        previousSavesDir = System.getProperty(GamePaths.SAVES_DIR_PROPERTY);
        previousUserDataDir = System.getProperty(GamePaths.USER_DATA_DIR_PROPERTY);
    }

    @AfterEach
    void restoreProperties() {
        restoreProperty(GamePaths.SAVES_DIR_PROPERTY, previousSavesDir);
        restoreProperty(GamePaths.USER_DATA_DIR_PROPERTY, previousUserDataDir);
    }

    @Test
    void savesDirPropertyOverrideWins() {
        File override = tempDir.resolve("custom-saves").toFile();
        System.setProperty(GamePaths.SAVES_DIR_PROPERTY, override.getAbsolutePath());

        File resolved = GamePaths.resolveSavesDirectory();

        assertEquals(override.getAbsoluteFile(), resolved.getAbsoluteFile());
        assertTrue(resolved.isDirectory());
    }

    @Test
    void userDataDirectoryUsesOverride() {
        File override = tempDir.resolve("userdata").toFile();
        System.setProperty(GamePaths.USER_DATA_DIR_PROPERTY, override.getAbsolutePath());

        File resolved = GamePaths.resolveUserDataDirectory();

        assertEquals(override.getAbsoluteFile(), resolved.getAbsoluteFile());
        assertTrue(resolved.isDirectory());
    }

    @Test
    void userDataDirectoryDefaultsToVendorAppFolder() {
        System.clearProperty(GamePaths.USER_DATA_DIR_PROPERTY);

        File resolved = GamePaths.resolveUserDataDirectory();
        File expectedTail = new File(new File("GrimIDK"), "FormicEmpire");

        assertTrue(resolved.getAbsolutePath().replace('\\', '/').endsWith(expectedTail.getPath().replace('\\', '/')),
                () -> "expected …/GrimIDK/FormicEmpire but was " + resolved.getAbsolutePath());
        assertTrue(resolved.isDirectory());
    }

    @Test
    void migrateCopiesMissingFilesWithoutOverwriting() throws Exception {
        File destination = tempDir.resolve("app-support-saves").toFile();
        assertTrue(destination.mkdirs());
        File keep = new File(destination, "settings.json");
        Files.writeString(keep.toPath(), "{\"keep\":true}", StandardCharsets.UTF_8);

        File legacy = tempDir.resolve("legacy-saves").toFile();
        assertTrue(legacy.mkdirs());
        Files.writeString(new File(legacy, "slot_1_manual.save").toPath(), "day-77", StandardCharsets.UTF_8);
        Files.writeString(new File(legacy, "settings.json").toPath(), "{\"legacy\":true}", StandardCharsets.UTF_8);

        GamePaths.copyMissingSaveFiles(legacy, destination);

        assertEquals("{\"keep\":true}", Files.readString(keep.toPath()));
        assertEquals("day-77", Files.readString(new File(destination, "slot_1_manual.save").toPath()));
    }

    private static void restoreProperty(String key, String previous) {
        if (previous == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previous);
        }
    }
}
