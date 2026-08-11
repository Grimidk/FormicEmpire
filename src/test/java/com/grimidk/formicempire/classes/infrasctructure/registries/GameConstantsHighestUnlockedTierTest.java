package com.grimidk.formicempire.classes.infrasctructure.registries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameConstantsHighestUnlockedTierTest {

    @Test
    void antRankHasNoUnlockedTier() {
        assertNull(GameConstants.getHighestUnlockedTier(GameConstants.RANK_ANT));
    }

    @Test
    void colonyRankUnlocksTier0() {
        assertEquals(GameConstants.TIER_0,
                GameConstants.getHighestUnlockedTier(GameConstants.RANK_COLONY));
    }

    @Test
    void kingdomRankUnlocksThroughTier3() {
        assertEquals(GameConstants.TIER_3,
                GameConstants.getHighestUnlockedTier(GameConstants.RANK_KINGDOM));
    }

    @Test
    void gigaRankUnlocksHighestTier() {
        assertEquals(GameConstants.TIER_11,
                GameConstants.getHighestUnlockedTier(GameConstants.RANK_GIGA));
    }

    @Test
    void nullRankReturnsNull() {
        assertNull(GameConstants.getHighestUnlockedTier(null));
    }
}
