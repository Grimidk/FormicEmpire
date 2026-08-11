package com.grimidk.formicempire.classes.infrasctructure.registries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;

class GameUnlocksBuildingGateTest {

    private Dynasty dynasty;
    private Colony colony;

    @BeforeEach
    void setUp() {
        dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
    }

    @Test
    void rockWarehousesRequireMiner() {
        assertFalse(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.ROCK_WAREHOUSE_0));
        colony.unlockUpgrade(GameUnlocks.ROLE_MINER);
        assertTrue(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.ROCK_WAREHOUSE_0));
        assertTrue(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.ROCK_WAREHOUSE_5));
    }

    @Test
    void resinReservoirsRequireResinAbility() {
        assertFalse(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.RESIN_RESERVOIR_0));
        colony.unlockUpgrade(GameUnlocks.ABILITY_RESIN);
        assertTrue(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.RESIN_RESERVOIR_0));
        assertTrue(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.RESIN_RESERVOIR_5));
    }

    @Test
    void otherBuildingsDoNotNeedSpecialUnlock() {
        assertTrue(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.PLANT_CHAMBER_0));
        assertTrue(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.ROYAL_CHAMBER_5));
    }

    @Test
    void blockingMaterialIsMineralForRockWarehousesWithoutMiner() {
        assertEquals(GameConstants.RESOURCE_ROCK,
                GameUnlocks.getBlockingBuildingMaterial(colony, GameUnlocks.ROCK_WAREHOUSE_0));
        colony.unlockUpgrade(GameUnlocks.ROLE_MINER);
        assertNull(GameUnlocks.getBlockingBuildingMaterial(colony, GameUnlocks.ROCK_WAREHOUSE_0));
    }

    @Test
    void blockingMaterialIsResinForResinReservoirsWithoutAbility() {
        assertEquals(GameConstants.RESOURCE_RESIN,
                GameUnlocks.getBlockingBuildingMaterial(colony, GameUnlocks.RESIN_RESERVOIR_0));
        colony.unlockUpgrade(GameUnlocks.ABILITY_RESIN);
        assertNull(GameUnlocks.getBlockingBuildingMaterial(colony, GameUnlocks.RESIN_RESERVOIR_0));
    }

    @Test
    void blockingMaterialFollowsMineralOrResinCostWithoutMatchingUnlock() {
        assertEquals(GameConstants.RESOURCE_RESIN,
                GameUnlocks.getBlockingBuildingMaterial(colony, GameUnlocks.PLANT_CHAMBER_2));
        colony.unlockUpgrade(GameUnlocks.ABILITY_RESIN);
        assertNull(GameUnlocks.getBlockingBuildingMaterial(colony, GameUnlocks.PLANT_CHAMBER_2));

        assertEquals(GameConstants.RESOURCE_ROCK,
                GameUnlocks.getBlockingBuildingMaterial(colony, GameUnlocks.PLANT_CHAMBER_4));
        colony.unlockUpgrade(GameUnlocks.ROLE_MINER);
        assertNull(GameUnlocks.getBlockingBuildingMaterial(colony, GameUnlocks.PLANT_CHAMBER_4));
    }
}
