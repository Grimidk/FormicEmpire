package com.grimidk.formicempire.classes.interfaces.ui.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UiNameSearchBarTest {

    @Test
    void normalizeQueryTrimsAndLowercases() {
        assertEquals("", UiNameSearchBar.normalizeQuery(null));
        assertEquals("", UiNameSearchBar.normalizeQuery("   "));
        assertEquals("leaf", UiNameSearchBar.normalizeQuery("  Leaf  "));
    }

    @Test
    void emptyQueryMatchesEverything() {
        assertTrue(UiNameSearchBar.matches("", "Anything"));
        assertTrue(UiNameSearchBar.matches(null, "Anything"));
        assertTrue(UiNameSearchBar.matches("", List.of("a", "b")));
    }

    @Test
    void matchesSubstringCaseInsensitive() {
        assertTrue(UiNameSearchBar.matches("leaf", "Leafcutter Prime"));
        assertTrue(UiNameSearchBar.matches("prime", "Leafcutter Prime", "Other"));
        assertFalse(UiNameSearchBar.matches("zzz", "Leafcutter Prime"));
        assertTrue(UiNameSearchBar.matches("dyn", List.of("My Dynasty", "Nest")));
        assertFalse(UiNameSearchBar.matches("xyz", List.of("My Dynasty", "Nest")));
    }
}
