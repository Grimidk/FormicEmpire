package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.FormicEmpire;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;

/** Resolves writable paths next to the running application (jar, exe, or .app bundle). */
public final class GamePaths {

    private GamePaths() {
    }

    public static File resolveSavesDirectory() {
        File savesDir = new File(resolveApplicationDirectory(), "saves");
        if (!savesDir.exists() && !savesDir.mkdirs()) {
            System.err.println("[GamePaths] Could not create saves directory: " + savesDir.getAbsolutePath());
        }
        return savesDir;
    }

    public static File resolveApplicationDirectory() {
        File fromCodeSource = directoryFromCodeSource();
        if (fromCodeSource != null) {
            File devProjectRoot = resolveDevProjectRoot(fromCodeSource);
            if (devProjectRoot != null) {
                return devProjectRoot;
            }
            return fromCodeSource;
        }
        return new File(System.getProperty("user.dir", "."));
    }

    /** When running the shaded jar from Maven {@code target/}, keep saves at the repo root. */
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
