package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.assets.GameSpritePreloader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadSlotSaveIntegrationTest {

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void loadSlotOneCompletes() {
        SaveManager saveManager = new SaveManager();
        Savefile savefile = saveManager.loadSlot(1);
        if (savefile == null) {
            return;
        }

        Engine engine = new Engine();
        engine.startUp(savefile);

        World world = engine.getWorld();
        assertNotNull(world);
        assertTrue(world.getHexes().size() > 0);

        Colony colony = world.getActiveHex() != null ? world.getActiveHex().getColony() : null;
        GameSpritePreloader.warmSession(colony);
    }
}
