package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonyJobRulesTest {

    private Colony colony;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Test", false);
        dynasty.addColony(colony);
        colony.setActive(false);
        colony.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);
    }

    @Test
    void hourlyLiteCanRunWithoutException() {
        ColonyJobRules.runHourlyLite(colony, GameConstants.BIOME_PLAINS);
        assertTrue(colony.getAge() >= 0);
    }

    @Test
    void dailyLiteCanRunWithoutException() {
        colony.setAge(7);
        ColonyJobRules.runDailyLite(colony, GameConstants.TEMP_WARM, GameConstants.BIOME_PLAINS);
        assertTrue(colony.getRank() != null);
    }

    @Test
    void dailyLiteCatchesAndBreedsPetBugs() {
        Dynasty dynasty = colony.getDynasty();
        dynasty.unlockUpgrade(GameUnlocks.ROLE_CATCHER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_RANCHER);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
        colony.setAssignedRoleCount(GameConstants.ROLE_CATCHER, 20);
        colony.setAssignedRoleCount(GameConstants.ROLE_RANCHER, 2);
        colony.setAge(7);

        boolean caught = false;
        for (int i = 0; i < 80 && !caught; i++) {
            ColonyJobRules.runDailyLite(colony, GameConstants.TEMP_WARM, GameConstants.BIOME_PLAINS);
            caught = colony.getAphids() > 0 || colony.getSymbioticMites() > 0;
        }
        assertTrue(caught);

        colony.applyPetBugCount(GameConstants.TYPE_APHID, 10);
        ColonyJobRules.runDailyLite(colony, GameConstants.TEMP_WARM, null);
        assertEquals(11, colony.getAphids());
    }

    @Test
    void dailyLiteScoutsWithAssignedScouts() {
        Dynasty dynasty = colony.getDynasty();
        dynasty.unlockUpgrade(GameUnlocks.ROLE_SCOUT);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        colony.setAssignedRoleCount(GameConstants.ROLE_SCOUT, 20);
        colony.setAge(7);

        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_PLAINS);

        int before = colony.getLocationService().getDiscoveredSources().size();
        boolean found = false;
        for (int i = 0; i < 40 && !found; i++) {
            ColonyJobRules.runDailyLite(colony, GameConstants.TEMP_WARM, GameConstants.BIOME_PLAINS, hex);
            found = colony.getLocationService().getDiscoveredSources().size() > before;
        }
        assertTrue(found);
        assertTrue(hex.getNonWaterResourceSourcesGenerated() > 0
                || colony.getLocationService().getSourcesByType(GameConstants.RESOURCE_WATER).size() > 0);
    }
}
