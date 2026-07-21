package com.grimidk.formicempire.classes.infrasctructure.i18n.translations;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationMapsTest {

    @Test
    void allLanguagesHaveIdenticalKeySets() {
        Map<String, String> en = new EnglishTranslation().getStrings();
        Map<String, String> es = new SpanishTranslation().getStrings();
        Map<String, String> fr = new FrenchTranslation().getStrings();
        Map<String, String> pt = new PortugueseTranslation().getStrings();

        Set<String> enKeys = en.keySet();
        assertEquals(enKeys.size(), en.size(), "English map has duplicate keys");

        assertKeySetsEqual(enKeys, es.keySet(), "es");
        assertKeySetsEqual(enKeys, fr.keySet(), "fr");
        assertKeySetsEqual(enKeys, pt.keySet(), "pt");
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
