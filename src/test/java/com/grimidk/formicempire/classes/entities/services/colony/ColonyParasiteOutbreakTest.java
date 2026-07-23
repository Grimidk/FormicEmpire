package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonyParasiteOutbreakTest {

    private Colony colony;
    private ColonyPopulationService populationService;
    private ColonyCritterHandlingService bugService;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_POLICE);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
        colony = new Colony(1, "Test", true);
        dynasty.addColony(colony);
        populationService = colony.getPopulationService();
        bugService = colony.getBugHandlingService();
    }

    private void addWorkers(int count) {
        for (int i = 0; i < count; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
    }

    @Test
    void projectsParasiteAntSpawnFromPopulation() {
        addWorkers(5_000);
        int projected = populationService.projectParasiteAntMonthlySpawn(
                colony, GameConstants.BIOME_DESERT, GameConstants.SEASON_SUMMER);
        assertEquals(50, projected);
        assertEquals(100, populationService.requiredPoliceToPreventParasiteAntOutbreak(
                colony, GameConstants.BIOME_DESERT, GameConstants.SEASON_SUMMER));
    }

    @Test
    void parasiteAntSpawnIncludesExistingInfestation() {
        addWorkers(5_000);
        colony.setParasiteAnts(40);
        int projected = populationService.projectParasiteAntMonthlySpawn(
                colony, GameConstants.BIOME_DESERT, GameConstants.SEASON_SPRING);
        assertEquals(70, projected);
    }

    @Test
    void parasiteAntOutbreakNotEligibleOutsideHotBiomeOrSeason() {
        addWorkers(5_000);
        assertEquals(0, populationService.projectParasiteAntMonthlySpawn(
                colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_SUMMER));
        assertEquals(0, populationService.projectParasiteAntMonthlySpawn(
                colony, GameConstants.BIOME_DESERT, GameConstants.SEASON_WINTER));
    }

    @Test
    void sufficientPolicePreventsParasiteAntOutbreak() {
        addWorkers(5_000);
        colony.setAssignedRoleCount(GameConstants.ROLE_POLICE, 100);
        assertTrue(populationService.isParasiteAntOutbreakPrevented(
                colony, GameConstants.BIOME_DESERT, GameConstants.SEASON_SUMMER));

        for (int i = 0; i < 80; i++) {
            populationService.runParasitation(colony, GameConstants.BIOME_DESERT, GameConstants.SEASON_SUMMER);
        }
        assertEquals(0, colony.getParasiteAnts());
    }

    @Test
    void insufficientPoliceAllowsParasiteAntOutbreak() {
        addWorkers(5_000);
        colony.setAssignedRoleCount(GameConstants.ROLE_POLICE, 10);
        assertFalse(populationService.isParasiteAntOutbreakPrevented(
                colony, GameConstants.BIOME_DESERT, GameConstants.SEASON_SUMMER));

        boolean spawned = false;
        for (int i = 0; i < 60 && !spawned; i++) {
            populationService.runParasitation(colony, GameConstants.BIOME_DESERT, GameConstants.SEASON_SUMMER);
            spawned = colony.getParasiteAnts() > 0;
        }
        assertTrue(spawned);
    }

    @Test
    void projectsParasiticMiteSpawnFromPopulationAndResources() {
        addWorkers(100);
        colony.setMushrooms(10_000);
        int projected = bugService.projectParasiticMiteMonthlySpawn(
                colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER);
        assertEquals(GameNumbers.PARASITIC_MITE_MIN_MONTHLY_SPAWN, projected);
        assertEquals(2_000, bugService.requiredSymbioticMitesToPreventOutbreak(
                colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER));
    }

    @Test
    void sufficientSymbioticMitesPreventParasiticMiteOutbreak() {
        addWorkers(200);
        colony.setMushrooms(10_000);
        bugService.restorePetCountsFromSave(colony, 0, 2_000, 0);
        assertEquals(2_000, colony.getSymbioticMites());
        assertTrue(bugService.isParasiticMiteOutbreakPrevented(
                colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER));

        for (int i = 0; i < 80; i++) {
            bugService.runMonthlyParasiticMites(colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER);
        }
        assertEquals(0, colony.getParasiticMites());
    }

    @Test
    void insufficientSymbioticMitesAllowParasiticMiteOutbreak() {
        addWorkers(100);
        colony.setMushrooms(10_000);
        bugService.setCount(colony, GameConstants.TYPE_SYMBIOTIC_MITE, 10);
        assertFalse(bugService.isParasiticMiteOutbreakPrevented(
                colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER));

        boolean spawned = false;
        for (int i = 0; i < 60 && !spawned; i++) {
            bugService.runMonthlyParasiticMites(colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER);
            spawned = colony.getParasiticMites() > 0;
        }
        assertTrue(spawned);
    }
}
