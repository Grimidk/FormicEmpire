package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonyAssimilationBuildingsTest {

    private Dynasty dynasty;
    private Colony colony;
    private ColonyStatsService stats;

    @BeforeEach
    void setup() {
        dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);
        dynasty.addColony(colony);
        colony.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        colony.unlockBuilding(GameUnlocks.PLANT_CHAMBER_1);
        colony.unlockBuilding(GameUnlocks.PLANT_CHAMBER_2);
        colony.unlockBuilding(GameUnlocks.PLANT_CHAMBER_3);
        colony.unlockBuilding(GameUnlocks.MEAT_CHAMBER_0);
        colony.unlockBuilding(GameUnlocks.MEAT_CHAMBER_1);
        colony.unlockBuilding(GameUnlocks.MEAT_CHAMBER_2);
        colony.unlockBuilding(GameUnlocks.MEAT_CHAMBER_3);
        stats = colony.getStatsService();
    }

    @Test
    void woodburrow_reducesBuildAndTunnelTime() {
        assertEquals(1.0, ColonyStatsService.woodburrowTimeMult(dynasty), 1e-9);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_WOODBURROW);
        assertEquals(GameNumbers.WOODBURROW_TIME_MULT, ColonyStatsService.woodburrowTimeMult(dynasty), 1e-9);
        assertEquals(GameNumbers.TUNNEL_WORK_REQUIRED * GameNumbers.WOODBURROW_TIME_MULT,
                ColonyStatsService.getTunnelWorkRequired(dynasty), 1e-6);
        assertEquals(GameUnlocks.PLANT_CHAMBER_3.getBuildTime() * GameNumbers.WOODBURROW_TIME_MULT,
                stats.getEffectiveBuildTime(colony, GameUnlocks.PLANT_CHAMBER_3), 1e-6);
    }

    @Test
    void silkweave_addsL3CapacityInTandem() {
        int before = stats.getPlantsCapacity(colony);
        assertEquals(60000, before);
        colony.unlockBuilding(GameUnlocks.PLANT_CHAMBER_3_SILK);
        assertEquals(120000, stats.getPlantsCapacity(colony));
    }

    @Test
    void hiveMounds_boostStorageNotQueens() {
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_0);
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        int plantsBefore = stats.getPlantsCapacity(colony);
        int queensBefore = stats.getQueensCapacity(colony);

        colony.unlockBuilding(GameUnlocks.HIVE_MOUND_2);
        int plantsAfter = stats.getPlantsCapacity(colony);
        assertEquals(Math.round(plantsBefore * 1.35), plantsAfter);
        assertEquals(queensBefore, stats.getQueensCapacity(colony));

        colony.unlockBuilding(GameUnlocks.HIVE_MOUND_4);
        assertEquals(Math.round(plantsBefore * 1.70), stats.getPlantsCapacity(colony));
    }

    @Test
    void silkPlantCost_isTenTimesResin() {
        assertEquals(0, GameUnlocks.PLANT_CHAMBER_3_SILK.getResinCost());
        assertEquals(GameUnlocks.PLANT_CHAMBER_3.getResinCost() * GameNumbers.SILKWEAVE_PLANT_COST_MULT,
                GameUnlocks.PLANT_CHAMBER_3_SILK.getPlantCost());
    }

    @Test
    void webBuildingSynergy_gatesPassiveWebAndWater() {
        assertTrue(!GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.PASSIVE_WEB));
        assertTrue(!GameUnlocks.isBuildingShownInTree(colony, GameUnlocks.PASSIVE_WEB));

        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_SILKWEAVE);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HIVEBUILD);
        DynastySynergyService.refreshUnlocked(dynasty);
        assertTrue(DynastySynergyService.isActive(dynasty, GameUnlocks.WEB_BUILDING_SYNERGY));
        assertTrue(dynasty.hasUpgrade(GameUnlocks.SYNERGY_WEB_BUILDING));

        colony.unlockBuilding(GameUnlocks.WATER_RESERVOIR_0);
        colony.unlockBuilding(GameUnlocks.WATER_RESERVOIR_1);
        assertTrue(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.PASSIVE_WEB));
        assertTrue(GameUnlocks.meetsBuildingUnlockRequirement(colony, GameUnlocks.PASSIVE_WATER));
        assertTrue(GameUnlocks.isBuildingShownInTree(colony, GameUnlocks.PASSIVE_WEB));
        assertTrue(GameUnlocks.isBuildingShownInTree(colony, GameUnlocks.PASSIVE_WATER));
    }

    @Test
    void passiveWeb_proteinOnlyWhenBuilt() {
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_SILKWEAVE);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HIVEBUILD);
        DynastySynergyService.refreshUnlocked(dynasty);
        assertEquals(0.0, stats.getProteinProductionHourly(colony), 1e-9);
        colony.unlockBuilding(GameUnlocks.PASSIVE_WEB);
        double expected = (stats.getProteinCapacity(colony) * GameNumbers.WEB_BUILDING_PROTEIN_DAILY_FRACTION) / 24.0;
        assertEquals(expected, stats.getProteinProductionHourly(colony), 1e-6);
    }
}
