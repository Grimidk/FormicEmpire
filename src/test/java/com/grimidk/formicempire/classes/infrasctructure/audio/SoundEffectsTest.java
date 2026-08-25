package com.grimidk.formicempire.classes.infrasctructure.audio;

import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundEffectsTest {

    @Test
    void registryIncludesAllEffectFiles() {
        assertEquals(9, SoundEffects.getEffects().size());
        assertFalse(SoundEffects.getPlayableEffects().isEmpty());

        SoundEffect popup = SoundEffects.getById("popup");
        assertNotNull(popup);
        assertEquals("Popup", popup.getDisplayName());
        assertTrue(popup.isResourcePresent());
        assertEquals("/audio/effects/popup.mp3", popup.getResourcePath());

        assertTrue(SoundEffects.SPIN_UP.isResourcePresent());
        assertTrue(SoundEffects.SPIN_DOWN.isResourcePresent());
        assertTrue(SoundEffects.BATTLE.isResourcePresent());
        assertTrue(SoundEffects.BUILDING.isResourcePresent());
        assertTrue(SoundEffects.BUILDING_END.isResourcePresent());
        assertTrue(SoundEffects.CLOSE_WINDOW.isResourcePresent());
        assertTrue(SoundEffects.RESEARCH.isResourcePresent());
        assertTrue(SoundEffects.ASSIMILATION.isResourcePresent());
    }

    @Test
    void soundEffectBuildsClasspathPathWithoutAuthor() {
        SoundEffect effect = new SoundEffect("click", "Click");
        assertEquals("click", effect.getId());
        assertEquals("Click", effect.getDisplayName());
        assertEquals("click.mp3", effect.getFileName());
        assertEquals("/audio/effects/click.mp3", effect.getResourcePath());
        assertFalse(effect.isResourcePresent());
    }

    @Test
    void soundEffectRejectsBlankFields() {
        assertThrows(IllegalArgumentException.class, () -> new SoundEffect(" ", "Name"));
        assertThrows(IllegalArgumentException.class, () -> new SoundEffect("Id", " "));
    }

    @Test
    void linearGain_respectsMasterAndSfxVolumes() {
        assertEquals(0f, SfxService.linearGain(0, 100));
        assertEquals(0f, SfxService.linearGain(100, 0));
        assertEquals(1f, SfxService.linearGain(100, 100));
        assertEquals(0.25f, SfxService.linearGain(50, 50), 0.0001f);
    }
}
