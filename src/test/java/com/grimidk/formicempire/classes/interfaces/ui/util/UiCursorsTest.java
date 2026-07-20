package com.grimidk.formicempire.classes.interfaces.ui.util;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiCursorsTest {

    @BeforeAll
    static void installCursors() {
        AssetStyles.installCursors();
    }

    @Test
    void loadsAllCustomCursors() {
        assertNotNull(AssetStyles.cursorNormal());
        assertNotNull(AssetStyles.cursorClick());
        assertNotNull(AssetStyles.cursorClickable());
        assertNotNull(AssetStyles.cursorWriteable());
        assertTrue(UiCursors.isHoverCursor(AssetStyles.cursorClickable()));
        assertTrue(UiCursors.isHoverCursor(AssetStyles.cursorWriteable()));
    }

    @Test
    void minControlHitMatchesCursorPixels() {
        assertTrue(UiCursors.pixelSize() >= AssetStyles.MIN_CONTROL_HIT_SIZE);
        Dimension hit = AssetStyles.minControlHitSize();
        assertEquals(UiCursors.pixelSize(), hit.width);
        assertEquals(UiCursors.pixelSize(), hit.height);
        Dimension spinner = AssetStyles.preferredSpinnerSize(80);
        assertTrue(spinner.width >= 80);
        assertTrue(spinner.height >= AssetStyles.MIN_SPINNER_HEIGHT);
        assertTrue(spinner.height >= UiCursors.pixelSize() * 2);
    }
}
