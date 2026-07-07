package com.grimidk.formicempire.classes.infrasctructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;

import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;

class LoadSlotWarsIntegrationTest {

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void loadSlotOneManualSavePreservesHistoricWarForPlayer() throws Exception {
        SaveManager saveManager = new SaveManager();
        Savefile savefile = saveManager.loadSlot(1);
        if (savefile == null) {
            return;
        }

        List<Savefile.SavedWar> rawWars = savefile.getWars();
        assertNotNull(rawWars);
        long validRaw = rawWars.stream()
                .filter(w -> w != null && War.isValidRecord(w.dynastyIdA, w.dynastyIdB))
                .count();
        assertTrue(validRaw >= 1, "expected at least one valid war in loaded savefile, got " + validRaw
                + " of " + rawWars.size());

        Engine engine = new Engine();
        engine.startUp(savefile);
        World world = engine.getWorld();
        assertNotNull(world);

        Dynasty player = world.getDynastys().stream().filter(Dynasty::isPlayer).findFirst().orElse(null);
        assertNotNull(player);

        List<War> historic = world.getWarService().getHistoricWarsForDynasty(player.getId());
        assertEquals(1, historic.size(), "player should have one historic war after load");
        assertFalse(historic.get(0).isActive());
        assertEquals("First Grim - Vine War", historic.get(0).getDisplayName());
        assertEquals("Grim Dynasty", world.getWarService().resolveWinnerDisplayName(historic.get(0), player));
    }
}
