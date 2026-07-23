package com.grimidk.formicempire.classes.constants.dynasty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class BattleLineTest {

    @Test
    void accuraciesMatchDesignBaselines() {
        assertEquals(100f, GameConstants.BATTLE_LINE_INFANTRY.getBaseAccuracyPercent(), 0.0001f);
        assertEquals(50f, GameConstants.BATTLE_LINE_ARTILLERY.getBaseAccuracyPercent(), 0.0001f);
        assertEquals(25f, GameConstants.BATTLE_LINE_AIR_SUPPORT.getBaseAccuracyPercent(), 0.0001f);
    }

    @Test
    void roleLookupMapsToExpectedLine() {
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY,
                GameConstants.getBattleLineForRole(GameConstants.ROLE_WARRIOR));
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY,
                GameConstants.getBattleLineForRole(GameConstants.ROLE_BOMBER));
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY,
                GameConstants.getBattleLineForRole(GameConstants.ROLE_POTTER));
        assertEquals(GameConstants.BATTLE_LINE_ARTILLERY,
                GameConstants.getBattleLineForRole(GameConstants.ROLE_ARTILLERY));
        assertTrue(GameConstants.BATTLE_LINE_INFANTRY.allowsRole(GameConstants.ROLE_MILITIA));
        assertTrue(GameConstants.ROLE_POTTER.isActiveMilitary());
        assertTrue(GameConstants.BATTLE_LINE_AIR_SUPPORT.getAllowedRoles().isEmpty());
    }
}
