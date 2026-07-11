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
        assertTrue(historic.size() >= 1, "player should have at least one historic war after load");
        War vineWar = historic.stream()
                .filter(w -> "First Grim - Vine War".equals(w.getDisplayName()))
                .findFirst()
                .orElse(null);
        assertNotNull(vineWar, "expected First Grim - Vine War in historic wars");
        assertFalse(vineWar.isActive());
        assertEquals("Grim Dynasty", world.getWarService().resolveWinnerDisplayName(vineWar, player));
    }
}
