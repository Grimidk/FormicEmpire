package com.grimidk.formicempire.classes.infrasctructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;

class LoadSlotWarsIntegrationTest {

    private static final String FIXTURE_WAR_DISPLAY_NAME = "First Grim - Vine War";

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void loadSlotOneManualSavePreservesHistoricWarForPlayer() throws Exception {
        SaveManager saveManager = new SaveManager();
        Savefile savefile = saveManager.loadSlot(1);
        if (savefile == null) {
            return;
        }

        List<Savefile.SavedWar> rawWars = savefile.getWars();
        if (rawWars == null || rawWars.isEmpty()) {
            return;
        }
        long validRaw = rawWars.stream()
                .filter(w -> w != null && War.isValidRecord(w.dynastyIdA, w.dynastyIdB))
                .count();
        if (validRaw < 1) {
            return;
        }

        boolean hasFixtureWar = rawWars.stream()
                .anyMatch(w -> w != null && FIXTURE_WAR_DISPLAY_NAME.equals(w.displayName));
        if (!hasFixtureWar) {
            return;
        }

        Engine engine = new Engine();
        engine.startUp(savefile);
        World world = engine.getWorld();
        assertNotNull(world);

        Dynasty player = world.getDynastys().stream().filter(Dynasty::isPlayer).findFirst().orElse(null);
        assertNotNull(player);

        List<War> historic = world.getWarService().getHistoricWarsForDynasty(player.getId());
        assertTrue(historic.size() >= 1, "player should have at least one historic war after load");
        War vineWar = historic.stream()
                .filter(w -> FIXTURE_WAR_DISPLAY_NAME.equals(w.getDisplayName()))
                .findFirst()
                .orElse(null);
        assertNotNull(vineWar, "expected " + FIXTURE_WAR_DISPLAY_NAME + " in historic wars");
        assertFalse(vineWar.isActive());
        assertEquals(player.getId(), vineWar.getWinnerDynastyId());
        assertEquals(player.getName(), world.getWarService().resolveWinnerDisplayName(vineWar, player));
    }
}
