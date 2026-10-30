package com.grimidk.formicempire.classes.infrasctructure.registries;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameNumbersStaggerTest {

    @Test
    void npcDailyWorkRotatesAcrossStaggerWindow() {
        int entityId = 3;
        boolean sawActive = false;
        boolean sawInactive = false;
        for (int day = 0; day < GameNumbers.NPC_DAILY_STAGGER_DAYS; day++) {
            if (GameNumbers.runsNpcDailyWorkToday(day, entityId)) {
                sawActive = true;
            } else {
                sawInactive = true;
            }
        }
        assertTrue(sawActive);
        assertTrue(sawInactive);
    }

    @Test
    void sameEntityRunsOnRepeatingDaySlot() {
        int entityId = 5;
        int slot = Math.floorMod(entityId, GameNumbers.NPC_DAILY_STAGGER_DAYS);
        assertTrue(GameNumbers.runsNpcDailyWorkToday(slot, entityId));
        assertFalse(GameNumbers.runsNpcDailyWorkToday(slot + 1, entityId));
    }
}
