package com.grimidk.formicempire.classes.infrasctructure.audio;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SfxServiceNonBlockingTest {

    private SfxService sfxService;

    @AfterEach
    void tearDown() {
        if (sfxService != null) {
            sfxService.shutdown();
            sfxService = null;
        }
    }

    @Test
    void playEffectReturnsWithoutWaitingForAudioThread() {
        Engine engine = new Engine();
        engine.setMasterVolume(100);
        engine.setSfxVolume(100);
        sfxService = engine.getSfxService();

        long startNs = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            sfxService.playEffect(SoundEffects.MENU_CLICK);
        }
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;

        assertTrue(elapsedMs < 200,
                "playEffect must not join previous SFX threads on the caller (took " + elapsedMs + " ms)");
    }
}
