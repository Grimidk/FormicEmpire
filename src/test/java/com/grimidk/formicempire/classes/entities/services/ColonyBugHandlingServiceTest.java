package com.grimidk.formicempire.classes.entities.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

class ColonyBugHandlingServiceTest {

    private Colony colony;
    private ColonyBugHandlingService service;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_CATCHER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_RANCHER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_GRAVER);
        dynasty.unlockUpgrade(GameUnlocks.STAT_DERMESTID_1);
        colony = new Colony(1, "Test", true);
        dynasty.addColony(colony);
        colony.setAssignedRoleCount(GameConstants.ROLE_CATCHER, 5);
        colony.setAssignedRoleCount(GameConstants.ROLE_RANCHER, 2);
        colony.setAssignedRoleCount(GameConstants.ROLE_GRAVER, 1);
        service = colony.getBugHandlingService();
    }

    @Test
    void breedingAddsHalfColonyWhenAtLeastTwo() {
        service.setCount(colony, GameConstants.TYPE_APHID, 4);
        service.runDaily(colony, null);
        assertEquals(6, colony.getAphids());
    }

    @Test
    void plainsHasAllNativePetBugs() {
        assertEquals(3, GameConstants.BIOME_PLAINS.getNativeBugs().size());
        assertTrue(GameConstants.BIOME_PLAINS.getNativeBugs().contains(GameConstants.TYPE_APHID));
        assertTrue(GameConstants.BIOME_PLAINS.getNativeBugs().contains(GameConstants.TYPE_SOIL_MITE));
        assertTrue(GameConstants.BIOME_PLAINS.getNativeBugs().contains(GameConstants.TYPE_DERMESTID));
    }

    @Test
    void dryBiomesExcludeAphids() {
        assertEquals(2, GameConstants.BIOME_DESERT.getNativeBugs().size());
        assertFalse(GameConstants.BIOME_DESERT.getNativeBugs().contains(GameConstants.TYPE_APHID));
        assertTrue(GameConstants.BIOME_DESERT.getNativeBugs().contains(GameConstants.TYPE_SOIL_MITE));
        assertTrue(GameConstants.BIOME_DESERT.getNativeBugs().contains(GameConstants.TYPE_DERMESTID));
    }

    @Test
    void coldBiomesExcludeDermestids() {
        assertEquals(2, GameConstants.BIOME_TUNDRA.getNativeBugs().size());
        assertTrue(GameConstants.BIOME_TUNDRA.getNativeBugs().contains(GameConstants.TYPE_APHID));
        assertTrue(GameConstants.BIOME_TUNDRA.getNativeBugs().contains(GameConstants.TYPE_SOIL_MITE));
        assertFalse(GameConstants.BIOME_TUNDRA.getNativeBugs().contains(GameConstants.TYPE_DERMESTID));
    }

    @Test
    void passiveGraveDoesNotRaiseDermestidCapacity() {
        colony.unlockBuilding(GameUnlocks.PASSIVE_GRAVE);
        colony.getDynasty().unlockUpgrade(GameUnlocks.STAT_PASSIVE_1);
        assertEquals(10, service.getMaxCapacity(colony, GameConstants.TYPE_DERMESTID));
    }

    @Test
    void dermestidsAddGraveBonusWhenUpgraded() {
        service.setCount(colony, GameConstants.TYPE_DERMESTID, 5);
        assertEquals(5, service.getDermestidGraveBonus(colony));
    }

    @Test
    void sharedCatcherPoolLimitsTotalPets() {
        colony.setAssignedRoleCount(GameConstants.ROLE_CATCHER, 1);
        service.setCount(colony, GameConstants.TYPE_APHID, 8);
        service.setCount(colony, GameConstants.TYPE_SOIL_MITE, 5);
        assertEquals(8, colony.getAphids());
        assertEquals(2, colony.getSoilMites());
    }

    @Test
    void parasiticMitesSlowOneAntPerTenMites() {
        for (int i = 0; i < 5; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        service.setParasiticMiteCount(colony, 25);
        assertEquals(2, service.getParasiticMiteSlowedAntCount(colony));
        long infected = colony.getWorkers().stream().filter(Ant::isParasiticMiteInfected).count();
        assertEquals(2, infected);
    }

    @Test
    void parasiticMitesDoNotSpawnBelowResourceThreshold() {
        colony.setMushrooms(100);
        service.runMonthlyParasiticMites(colony);
        assertEquals(0, colony.getParasiticMites());
    }

    @Test
    void parasiticMitesDoNotSpawnAtFiveThousandResources() {
        colony.setMushrooms(5_000);
        service.runMonthlyParasiticMites(colony);
        assertEquals(0, colony.getParasiticMites());
    }

    @Test
    void parasiticMitesCanSpawnMonthlyWithHighResources() {
        colony.setMushrooms(10_000);
        boolean spawned = false;
        for (int i = 0; i < 30 && !spawned; i++) {
            service.runMonthlyParasiticMites(colony);
            spawned = colony.getParasiticMites() > 0;
        }
        assertTrue(spawned);
        assertTrue(colony.getParasiticMites() >= GameConstants.PARASITIC_MITE_MIN_MONTHLY_SPAWN);
    }

    @Test
    void parasiticMitesCapAtTenPerAnt() {
        for (int i = 0; i < 100; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        colony.setMushrooms(10_000);
        for (int i = 0; i < 50; i++) {
            service.runMonthlyParasiticMites(colony);
        }
        assertTrue(colony.getParasiticMites() <= 100 * GameConstants.PARASITIC_MITES_PER_SLOWED_ANT);
    }

    @Test
    void soilMitesEliminateParasiticMitesDaily() {
        service.setCount(colony, GameConstants.TYPE_SOIL_MITE, 1);
        service.setParasiticMiteCount(colony, 20);
        service.runDaily(colony, null);
        assertEquals(15, colony.getParasiticMites());
        assertEquals(5, service.getSoilMiteParasiticMiteKillPerDay(colony));
    }

    @Test
    void petBugEntitiesCapAtVisibleSpriteLimitWhileCountsStayFull() {
        for (int i = 0; i < 600; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        service.restorePetCountsFromSave(colony, 5_000, 5_000, 5_000);
        assertEquals(5_000, colony.getAphids());
        assertEquals(5_000, colony.getSoilMites());
        assertEquals(5_000, colony.getDermestids());
        assertEquals(GameConstants.MAX_VISIBLE_SPRITE_COUNT,
                colony.getBugs().stream().filter(b -> b.getBugType() == GameConstants.TYPE_APHID).count());
        assertEquals(GameConstants.MAX_VISIBLE_SPRITE_COUNT,
                colony.getBugs().stream().filter(b -> b.getBugType() == GameConstants.TYPE_SOIL_MITE).count());
        assertEquals(GameConstants.MAX_VISIBLE_SPRITE_COUNT,
                colony.getBugs().stream().filter(b -> b.getBugType() == GameConstants.TYPE_DERMESTID).count());
    }

    @Test
    void soilMiteUpgradeKillsMoreParasiticMites() {
        colony.getDynasty().unlockUpgrade(GameUnlocks.STAT_SOIL_MITE_1);
        service.setCount(colony, GameConstants.TYPE_SOIL_MITE, 1);
        service.setParasiticMiteCount(colony, 50);
        service.runDaily(colony, null);
        assertEquals(38, colony.getParasiticMites());
        assertEquals(12, service.getSoilMiteParasiticMiteKillPerDay(colony));
    }
}
