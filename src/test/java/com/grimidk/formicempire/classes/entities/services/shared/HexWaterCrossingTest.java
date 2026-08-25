package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class HexWaterCrossingTest {

    private Hex landA;
    private Hex water1;
    private Hex water2;
    private Hex water3;
    private Hex landB;
    private Hex adjacentLand;
    private Hex nearShore;
    private Dynasty dynasty;

    @BeforeEach
    void setup() {
        landA = landHex();
        water1 = waterHex();
        water2 = waterHex();
        water3 = waterHex();
        landB = landHex();
        adjacentLand = landHex();
        nearShore = landHex();

        landA.setNorth(adjacentLand);
        adjacentLand.setSouth(landA);

        landA.setNorthEast(water1);
        water1.setSouthWest(landA);

        water1.setNorthEast(water2);
        water2.setSouthWest(water1);

        water2.setNorthEast(water3);
        water3.setSouthWest(water2);

        water3.setNorthEast(landB);
        landB.setSouthWest(water3);

        water1.setNorth(nearShore);
        nearShore.setSouth(water1);
        nearShore.setIsland(true);
        landB.setIsland(true);

        dynasty = new Dynasty(1, "Raft Dynasty", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_RAFTING);
    }

    @Test
    void waterCrossRange_scalesWithUpgrades() {
        Dynasty bare = new Dynasty(2, "Bare", true, GameConstants.SPECIES_OMNI);
        assertEquals(0, HexWaterCrossing.waterCrossRange(bare));

        assertEquals(GameNumbers.RAFTING_WATER_CROSS_RANGE_1, HexWaterCrossing.waterCrossRange(dynasty));

        dynasty.unlockUpgrade(GameUnlocks.ABILITY_RAFTING_2);
        assertEquals(GameNumbers.RAFTING_WATER_CROSS_RANGE_2, HexWaterCrossing.waterCrossRange(dynasty));

        dynasty.unlockUpgrade(GameUnlocks.ABILITY_RAFTING_3);
        assertEquals(GameNumbers.RAFTING_WATER_CROSS_RANGE_3, HexWaterCrossing.waterCrossRange(dynasty));
    }

    @Test
    void range1_reachesAcrossOneWaterButNotThree() {
        List<Hex> across1 = HexWaterCrossing.waterCrossLandHexes(landA, 1);
        assertTrue(across1.contains(nearShore));
        assertFalse(across1.contains(landB));
    }

    @Test
    void range3_reachesIslandAcrossThreeWater() {
        List<Hex> across3 = HexWaterCrossing.waterCrossLandHexes(landA, 3);
        assertTrue(across3.contains(landB));
        assertTrue(across3.contains(nearShore));

        List<Hex> colonizable = HexWaterCrossing.colonizableLandHexes(landA, 3);
        assertTrue(colonizable.contains(adjacentLand));
        assertTrue(colonizable.contains(landB));
    }

    @Test
    void withoutRafting_colonizableIsAdjacentLandOnly() {
        List<Hex> colonizable = HexWaterCrossing.colonizableLandHexes(landA, 0);
        assertTrue(colonizable.contains(adjacentLand));
        assertFalse(colonizable.contains(landB));
        assertFalse(colonizable.contains(nearShore));
    }

    private static Hex landHex() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_PLAINS);
        return hex;
    }

    private static Hex waterHex() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_OCEAN);
        return hex;
    }
}
