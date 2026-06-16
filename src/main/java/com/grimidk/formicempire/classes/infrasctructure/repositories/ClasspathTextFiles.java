package com.grimidk.formicempire.classes.infrasctructure.repositories;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class ClasspathTextFiles {
    private ClasspathTextFiles() {}

    public static String loadRoadmapText() {
        return load("/roadmap.txt", LanguageStrings.ROADMAP_UNAVAILABLE);
    }

    public static String loadCreditsText() {
        return load("/credits.txt", LanguageStrings.CREDITS_UNAVAILABLE);
    }

    private static String load(String resourcePath, String unavailableKey) {
        try (InputStream in = ClasspathTextFiles.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return LanguageStrings.get(unavailableKey);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            return LanguageStrings.get(unavailableKey);
        }
    }
}
