package com.grimidk.formicempire.classes.infrasctructure.util;

import com.grimidk.formicempire.FormicEmpire;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.LinkedHashSet;

public final class GamePaths {

    public static final String SAVES_DIR_PROPERTY = "formicempire.savesDir";
    public static final String USER_DATA_DIR_PROPERTY = "formicempire.userDataDir";
    private static final String VENDOR_FOLDER_NAME = "GrimIDK";
    private static final String APP_FOLDER_NAME = "FormicEmpire";

    private GamePaths() {
    }

    public static File resolveSavesDirectory() {
        String override = System.getProperty(SAVES_DIR_PROPERTY);
        if (override != null && !override.trim().isEmpty()) {
            return ensureDirectory(new File(override.trim()));
        }

        File fromCodeSource = directoryFromCodeSource();
        File devProjectRoot = fromCodeSource != null ? resolveDevProjectRoot(fromCodeSource) : null;
        if (devProjectRoot != null) {
            return ensureDirectory(new File(devProjectRoot, "saves"));
        }

        File savesDir = ensureDirectory(new File(resolveUserDataDirectory(), "saves"));
        migrateLegacySavesIfNeeded(savesDir);
        return savesDir;
    }

    public static File resolveUserDataDirectory() {
        String override = System.getProperty(USER_DATA_DIR_PROPERTY);
        if (override != null && !override.trim().isEmpty()) {
            return ensureDirectory(new File(override.trim()));
        }

        return ensureDirectory(new File(resolveOsUserDataRoot(), VENDOR_FOLDER_NAME + File.separator + APP_FOLDER_NAME));
    }

    private static File resolveOsUserDataRoot() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home", ".");
        if (os.contains("mac")) {
            return new File(home, "Library/Application Support");
        }
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.trim().isEmpty()) {
                return new File(appData.trim());
            }
            return new File(home, "AppData/Roaming");
        }
        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.trim().isEmpty()) {
            return new File(xdgDataHome.trim());
        }
        return new File(home, ".local/share");
    }

    public static File resolveApplicationDirectory() {
        File fromCodeSource = directoryFromCodeSource();
        if (fromCodeSource != null) {
            File devProjectRoot = resolveDevProjectRoot(fromCodeSource);
            if (devProjectRoot != null) {
                return devProjectRoot;
            }
            return normalizePackagedAppDirectory(fromCodeSource);
        }
        return new File(System.getProperty("user.dir", "."));
    }

    static void migrateLegacySavesIfNeeded(File destinationSavesDir) {
        if (destinationSavesDir == null) {
            return;
        }
        for (File legacySavesDir : legacySavesCandidates()) {
            copyMissingSaveFiles(legacySavesDir, destinationSavesDir);
        }
    }

    private static File[] legacySavesCandidates() {
        LinkedHashSet<File> candidates = new LinkedHashSet<>();
        File fromCodeSource = directoryFromCodeSource();
        if (fromCodeSource != null) {
            candidates.add(new File(fromCodeSource, "saves"));
            File packagedRoot = normalizePackagedAppDirectory(fromCodeSource);
            if (packagedRoot != null) {
                candidates.add(new File(packagedRoot, "saves"));
            }
        }
        candidates.add(new File(resolveApplicationDirectory(), "saves"));
        candidates.add(new File(System.getProperty("user.dir", "."), "saves"));
        candidates.add(new File(resolveOsUserDataRoot(), APP_FOLDER_NAME + File.separator + "saves"));
        return candidates.toArray(new File[0]);
    }

    static void copyMissingSaveFiles(File legacySavesDir, File destinationSavesDir) {
        if (legacySavesDir == null || !legacySavesDir.isDirectory()) {
            return;
        }
        if (legacySavesDir.getAbsoluteFile().equals(destinationSavesDir.getAbsoluteFile())) {
            return;
        }
        File[] legacyFiles = legacySavesDir.listFiles();
        if (legacyFiles == null || legacyFiles.length == 0) {
            return;
        }
        for (File legacyFile : legacyFiles) {
            if (legacyFile == null || !legacyFile.isFile()) {
                continue;
            }
            File target = new File(destinationSavesDir, legacyFile.getName());
            if (target.exists()) {
                continue;
            }
            try {
                Files.copy(legacyFile.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                System.out.println("[GamePaths] Migrated legacy save: " + legacyFile.getName()
                        + " -> " + target.getAbsolutePath());
            } catch (IOException ex) {
                System.err.println("[GamePaths] Failed to migrate " + legacyFile.getName() + ": " + ex.getMessage());
            }
        }
    }

    private static File ensureDirectory(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("[GamePaths] Could not create directory: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private static File normalizePackagedAppDirectory(File appDir) {
        if (appDir == null) {
            return null;
        }
        if ("lib".equals(appDir.getName())) {
            File imageRoot = appDir.getParentFile();
            if (imageRoot != null && new File(imageRoot, "bin").isDirectory()) {
                return imageRoot;
            }
        }
        return appDir;
    }

    private static File resolveDevProjectRoot(File appDir) {
        File dir = appDir;
        if (dir != null && "classes".equals(dir.getName())) {
            dir = dir.getParentFile();
        }
        if (dir == null || !"target".equals(dir.getName())) {
            return null;
        }
        File projectRoot = dir.getParentFile();
        if (projectRoot != null && new File(projectRoot, "pom.xml").isFile()) {
            return projectRoot;
        }
        return null;
    }

    private static File directoryFromCodeSource() {
        try {
            CodeSource codeSource = FormicEmpire.class.getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) {
                return null;
            }
            File location = new File(codeSource.getLocation().toURI());
            if (location.isFile()) {
                File parent = location.getParentFile();
                if (parent != null) {
                    return parent;
                }
            } else if (location.isDirectory()) {
                return location;
            }
        } catch (URISyntaxException | SecurityException ex) {
            System.err.println("[GamePaths] Failed to resolve application directory: " + ex.getMessage());
        }
        return null;
    }
}
