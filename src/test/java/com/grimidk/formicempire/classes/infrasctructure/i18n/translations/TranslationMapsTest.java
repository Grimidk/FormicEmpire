package com.grimidk.formicempire.classes.infrasctructure.i18n.translations;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationMapsTest {

    @Test
    void allLanguagesHaveIdenticalKeySets() {
        Map<String, String> en = new EnglishTranslation().getStrings();
        Map<String, String> es = new SpanishTranslation().getStrings();
        Map<String, String> fr = new FrenchTranslation().getStrings();
        Map<String, String> pt = new PortugueseTranslation().getStrings();

        Set<String> enKeys = en.keySet();

        assertKeySetsEqual(enKeys, es.keySet(), "es");
        assertKeySetsEqual(enKeys, fr.keySet(), "fr");
        assertKeySetsEqual(enKeys, pt.keySet(), "pt");
    }

    @Test
    void translationSheetHasUniqueTags() throws IOException {
        Path csv = resolveTranslationSheet();
        assertTrue(Files.isRegularFile(csv), "missing " + csv);
        List<String> tags = readTags(csv);
        assertFalse(tags.isEmpty(), "translation_sheet.csv has no tags");
        Set<String> unique = new HashSet<>();
        List<String> dups = new ArrayList<>();
        for (String tag : tags) {
            if (!unique.add(tag)) {
                dups.add(tag);
            }
        }
        assertTrue(dups.isEmpty(), () -> "duplicate tags in translation_sheet.csv: " + dups);
    }

    @Test
    void generatedMapsMatchSheetTagCount() throws IOException {
        int sheetTags = readTags(resolveTranslationSheet()).size();
        assertEquals(sheetTags, new EnglishTranslation().getStrings().size(),
                "English map size should match translation_sheet.csv tag count");
    }

    private static Path resolveTranslationSheet() {
        Path cwd = Path.of("").toAbsolutePath();
        Path relative = Path.of("src/main/resources/texts/translation_sheet.csv");
        Path candidate = cwd.resolve(relative);
        if (Files.isRegularFile(candidate)) {
            return candidate;
        }
        candidate = cwd.resolve("FormicEmpire").resolve(relative);
        assertTrue(Files.isRegularFile(candidate), "cannot find translation_sheet.csv from " + cwd);
        return candidate;
    }

    private static List<String> readTags(Path csv) throws IOException {
        List<String> tags = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String header = br.readLine();
            assertTrue(header != null && header.toLowerCase().startsWith("tag"),
                    "CSV must start with tag column");
            String line;
            StringBuilder pending = null;
            int quoteCount = 0;
            while ((line = br.readLine()) != null) {
                if (pending != null) {
                    pending.append('\n').append(line);
                    quoteCount += countQuotes(line);
                    if (quoteCount % 2 == 0) {
                        addTagFromRecord(pending.toString(), tags);
                        pending = null;
                        quoteCount = 0;
                    }
                    continue;
                }
                quoteCount = countQuotes(line);
                if (quoteCount % 2 != 0) {
                    pending = new StringBuilder(line);
                    continue;
                }
                addTagFromRecord(line, tags);
            }
            assertTrue(pending == null, "Unterminated quoted CSV record");
        }
        return tags;
    }

    private static void addTagFromRecord(String record, List<String> tags) {
        if (record.isBlank()) {
            return;
        }
        String tag;
        if (record.startsWith("\"")) {
            int end = 1;
            StringBuilder sb = new StringBuilder();
            while (end < record.length()) {
                char c = record.charAt(end);
                if (c == '"') {
                    if (end + 1 < record.length() && record.charAt(end + 1) == '"') {
                        sb.append('"');
                        end += 2;
                        continue;
                    }
                    break;
                }
                sb.append(c);
                end++;
            }
            tag = sb.toString();
        } else {
            int comma = record.indexOf(',');
            tag = comma < 0 ? record : record.substring(0, comma);
        }
        tag = tag.trim();
        if (!tag.isEmpty()) {
            tags.add(tag);
        }
    }

    private static int countQuotes(String s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '"') {
                n++;
            }
        }
        return n;
    }

    private static void assertKeySetsEqual(Set<String> expected, Set<String> actual, String lang) {
        assertEquals(expected.size(), actual.size(), lang + " key count");
        Set<String> missing = new HashSet<>(expected);
        missing.removeAll(actual);
        Set<String> extra = new HashSet<>(actual);
        extra.removeAll(expected);
        assertTrue(missing.isEmpty(), () -> lang + " missing keys: " + missing);
        assertTrue(extra.isEmpty(), () -> lang + " extra keys: " + extra);
    }
}
