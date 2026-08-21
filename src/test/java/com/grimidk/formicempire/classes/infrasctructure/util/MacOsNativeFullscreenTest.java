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
    void eawtRequiresModuleExportOnMac() {
        if (!MacOsNativeFullscreen.isMac()) {
            assertFalse(MacOsNativeFullscreen.isEawtAvailable());
            return;
        }
        Module desktop = ModuleLayer.boot().findModule("java.desktop").orElseThrow();
        Module self = MacOsNativeFullscreen.class.getModule();
        boolean exported = desktop.isExported("com.apple.eawt", self) || desktop.isOpen("com.apple.eawt", self);
        assertTrue(exported == MacOsNativeFullscreen.isEawtAvailable());
    }
}
