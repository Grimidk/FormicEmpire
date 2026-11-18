package com.grimidk.formicempire.classes.entities.services.shared;

import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.world.Bridge;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BridgeTest {

    @Test
    void bridgeRequiresRaftingParentAndProgressesToCompletion() {
        assertEquals(GameUnlocks.ASSIMILATED_RAFTING, GameUnlocks.ABILITY_BRIDGES.getRequirement());

        Hex landA = new Hex();
        Hex landB = new Hex();
        Bridge bridge = new Bridge(landA, landB, 100.0);

        assertFalse(bridge.isComplete());
        assertTrue(bridge.connects(landB, landA));

        bridge.addProgress(60.0);
        assertFalse(bridge.isComplete());
        assertEquals(60.0, bridge.getProgress());

        bridge.addProgress(50.0);
        assertTrue(bridge.isComplete());
        assertEquals(100.0, bridge.getProgress());
    }

    @Test
    void bridgedLandHexesAreReachableAcrossWaterWhenComplete() {
        Hex landA = new Hex();
        Hex water = new Hex();
        Hex landB = new Hex();
        landA.setBiome(GameConstants.BIOME_PLAINS);
        water.setBiome(GameConstants.BIOME_OCEAN);
        landB.setBiome(GameConstants.BIOME_PLAINS);

        landA.setSouth(water);
        water.setNorth(landA);
        water.setSouth(landB);
        landB.setNorth(water);

        Dynasty dynasty = new Dynasty(1, "Test", false, GameConstants.SPECIES_OMNI);
        assertTrue(HexWaterCrossing.bridgeableLandHexes(landA, dynasty).isEmpty());

        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_RAFTING);
        assertTrue(HexWaterCrossing.bridgeableLandHexes(landA, dynasty).isEmpty());

        dynasty.unlockUpgrade(GameUnlocks.ABILITY_BRIDGES);
        List<Hex> candidates = HexWaterCrossing.bridgeableLandHexes(landA, dynasty);
        assertEquals(List.of(landB), candidates);

        Bridge bridge = new Bridge(landA, landB, 100.0);
        dynasty.addBridge(bridge);
        assertFalse(HexWaterCrossing.bridgedLandHexes(landA, dynasty).contains(landB));

        bridge.addProgress(100.0);
        assertTrue(HexWaterCrossing.bridgedLandHexes(landA, dynasty).contains(landB));
        assertTrue(HexWaterCrossing.colonizableLandHexes(landA, 0, dynasty).contains(landB));
        assertTrue(HexWaterCrossing.tradeReachableLandHexes(landA, 0, dynasty).contains(landB));
    }
}
