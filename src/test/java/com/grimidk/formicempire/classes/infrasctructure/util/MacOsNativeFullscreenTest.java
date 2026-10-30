package com.grimidk.formicempire.classes.infrasctructure.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MacOsNativeFullscreenTest {

    @Test
    void macDetectionMatchesOsName() {
        boolean mac = System.getProperty("os.name", "").toLowerCase().contains("mac");
        assertTrue(mac == MacOsNativeFullscreen.isMac());
    }

    @Test
    void requestForegroundIsSafeWhenEawtUnavailable() {
        if (!MacOsNativeFullscreen.isEawtAvailable()) {
            assertFalse(MacOsNativeFullscreen.requestForeground());
        }
    }
}
