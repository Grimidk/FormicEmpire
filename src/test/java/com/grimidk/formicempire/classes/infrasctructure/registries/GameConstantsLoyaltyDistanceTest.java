package com.grimidk.formicempire.classes.infrasctructure.registries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameConstantsLoyaltyDistanceTest {

    @Test
    void adjacentCapitalHasNoDistancePenalty() {
        assertEquals(0, GameConstants.getCapitalDistanceLoyaltyPenalty(0));
        assertEquals(0, GameConstants.getCapitalDistanceLoyaltyPenalty(1));
    }

    @Test
    void tenTilesFromCapitalHasMaxPenalty() {
        assertEquals(-10, GameConstants.getCapitalDistanceLoyaltyPenalty(10));
        assertEquals(-10, GameConstants.getCapitalDistanceLoyaltyPenalty(15));
    }

    @Test
    void midRangeDistanceScalesLinearly() {
        assertEquals(-1, GameConstants.getCapitalDistanceLoyaltyPenalty(2));
        assertEquals(-4, GameConstants.getCapitalDistanceLoyaltyPenalty(5));
    }

    @Test
    void axialHexDistanceUsesCubeFormula() {
        assertEquals(0, GameConstants.axialHexDistance(0, 0, 0, 0));
        assertEquals(1, GameConstants.axialHexDistance(0, 0, 1, 0));
        assertEquals(3, GameConstants.axialHexDistance(0, 0, 3, 0));
    }
}
