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
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_CATCH_DERMESTID);
        dynasty.unlockUpgrade(GameUnlocks.STAT_DERMESTID_1);
        colony = new Colony(1, "Test", true);
        dynasty.addColony(colony);
        colony.setAssignedRoleCount(GameConstants.ROLE_CATCHER, 5);
        colony.setAssignedRoleCount(GameConstants.ROLE_RANCHER, 2);
        colony.setAssignedRoleCount(GameConstants.ROLE_GRAVER, 1);
        service = colony.getBugHandlingService();
    }

    @Test
    void aphidsRequireRancherUnlockToCatch() {
        Dynasty dynasty = new Dynasty(2, "Gate", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_CATCHER);
        Colony gated = new Colony(2, "Gate", true);
        dynasty.addColony(gated);
        gated.setAssignedRoleCount(GameConstants.ROLE_CATCHER, 5);

        assertFalse(service.canCatchPetBug(gated, GameConstants.TYPE_APHID));

        dynasty.unlockUpgrade(GameUnlocks.ROLE_RANCHER);
        assertTrue(service.canCatchPetBug(gated, GameConstants.TYPE_APHID));
    }

    @Test
    void symbioticMitesRequireCatchAbility() {
        Dynasty dynasty = new Dynasty(3, "Soil", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_CATCHER);
        Colony gated = new Colony(3, "Soil", true);
        dynasty.addColony(gated);

        assertFalse(service.canCatchPetBug(gated, GameConstants.TYPE_SYMBIOTIC_MITE));

        dynasty.unlockUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
        assertTrue(service.canCatchPetBug(gated, GameConstants.TYPE_SYMBIOTIC_MITE));
    }

    @Test
    void dermestidsRequirePurchasedCatchAbility() {
        Dynasty dynasty = new Dynasty(4, "Derm", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_CATCHER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_GRAVER);
        Colony gated = new Colony(4, "Derm", true);
        dynasty.addColony(gated);

        assertFalse(service.canCatchPetBug(gated, GameConstants.TYPE_DERMESTID));

        dynasty.unlockUpgrade(GameUnlocks.ABILITY_CATCH_DERMESTID);
        assertTrue(service.canCatchPetBug(gated, GameConstants.TYPE_DERMESTID));
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
        assertTrue(GameConstants.BIOME_PLAINS.getNativeBugs().contains(GameConstants.TYPE_SYMBIOTIC_MITE));
        assertTrue(GameConstants.BIOME_PLAINS.getNativeBugs().contains(GameConstants.TYPE_DERMESTID));
    }

    @Test
    void dryBiomesExcludeAphids() {
        assertEquals(2, GameConstants.BIOME_DESERT.getNativeBugs().size());
        assertFalse(GameConstants.BIOME_DESERT.getNativeBugs().contains(GameConstants.TYPE_APHID));
        assertTrue(GameConstants.BIOME_DESERT.getNativeBugs().contains(GameConstants.TYPE_SYMBIOTIC_MITE));
        assertTrue(GameConstants.BIOME_DESERT.getNativeBugs().contains(GameConstants.TYPE_DERMESTID));
    }

    @Test
    void coldBiomesExcludeDermestids() {
        assertEquals(2, GameConstants.BIOME_TUNDRA.getNativeBugs().size());
        assertTrue(GameConstants.BIOME_TUNDRA.getNativeBugs().contains(GameConstants.TYPE_APHID));
        assertTrue(GameConstants.BIOME_TUNDRA.getNativeBugs().contains(GameConstants.TYPE_SYMBIOTIC_MITE));
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
        service.setCount(colony, GameConstants.TYPE_SYMBIOTIC_MITE, 5);
        assertEquals(8, colony.getAphids());
        assertEquals(2, colony.getSymbioticMites());
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
        service.runMonthlyParasiticMites(colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER);
        assertEquals(0, colony.getParasiticMites());
    }

    @Test
    void parasiticMitesDoNotSpawnAtFiveThousandResources() {
        colony.setMushrooms(5_000);
        service.runMonthlyParasiticMites(colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER);
        assertEquals(0, colony.getParasiticMites());
    }

    @Test
    void parasiticMitesDoNotSpawnOutsideColdBiomes() {
        colony.setMushrooms(10_000);
        service.runMonthlyParasiticMites(colony, GameConstants.BIOME_PLAINS, GameConstants.SEASON_WINTER);
        assertEquals(0, colony.getParasiticMites());
    }

    @Test
    void parasiticMitesDoNotSpawnOutsideFallOrWinter() {
        colony.setMushrooms(10_000);
        service.runMonthlyParasiticMites(colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_SUMMER);
        assertEquals(0, colony.getParasiticMites());
    }

    @Test
    void parasiticMitesCanSpawnMonthlyWithHighResources() {
        colony.setMushrooms(10_000);
        boolean spawned = false;
        for (int i = 0; i < 40 && !spawned; i++) {
            service.runMonthlyParasiticMites(colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER);
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
        for (int i = 0; i < 80; i++) {
            service.runMonthlyParasiticMites(colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER);
        }
        assertTrue(colony.getParasiticMites() <= 100 * GameConstants.PARASITIC_MITES_PER_SLOWED_ANT);
    }

    @Test
    void hotBiomesHostParasiticAntsNotMites() {
        assertTrue(GameConstants.BIOME_DESERT.hasNativeParasite(GameConstants.TYPE_PARASITE_ANT));
        assertFalse(GameConstants.BIOME_DESERT.hasNativeParasite(GameConstants.TYPE_PARASITIC_MITE));
    }

    @Test
    void coldBiomesHostParasiticMitesNotAnts() {
        assertTrue(GameConstants.BIOME_TUNDRA.hasNativeParasite(GameConstants.TYPE_PARASITIC_MITE));
        assertFalse(GameConstants.BIOME_TUNDRA.hasNativeParasite(GameConstants.TYPE_PARASITE_ANT));
    }

    @Test
    void symbioticMitesEliminateParasiticMitesDaily() {
        service.setCount(colony, GameConstants.TYPE_SYMBIOTIC_MITE, 1);
        service.setParasiticMiteCount(colony, 20);
        service.runDaily(colony, null);
        assertEquals(15, colony.getParasiticMites());
        assertEquals(5, service.getSymbioticMiteParasiticMiteKillPerDay(colony));
    }

    @Test
    void petBugEntitiesCapAtVisibleSpriteLimitWhileCountsStayFull() {
        for (int i = 0; i < 600; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        service.restorePetCountsFromSave(colony, 5_000, 5_000, 5_000);
        assertEquals(5_000, colony.getAphids());
        assertEquals(5_000, colony.getSymbioticMites());
        assertEquals(5_000, colony.getDermestids());
        assertEquals(GameConstants.MAX_VISIBLE_SPRITE_COUNT,
                colony.getBugs().stream().filter(b -> b.getBugType() == GameConstants.TYPE_APHID).count());
        assertEquals(GameConstants.MAX_VISIBLE_SPRITE_COUNT,
                colony.getBugs().stream().filter(b -> b.getBugType() == GameConstants.TYPE_SYMBIOTIC_MITE).count());
        assertEquals(GameConstants.MAX_VISIBLE_SPRITE_COUNT,
                colony.getBugs().stream().filter(b -> b.getBugType() == GameConstants.TYPE_DERMESTID).count());
    }

    @Test
    void symbioticMiteUpgradeKillsMoreParasiticMites() {
        colony.getDynasty().unlockUpgrade(GameUnlocks.STAT_SYMBIOTIC_MITE_1);
        service.setCount(colony, GameConstants.TYPE_SYMBIOTIC_MITE, 1);
        service.setParasiticMiteCount(colony, 50);
        service.runDaily(colony, null);
        assertEquals(38, colony.getParasiticMites());
        assertEquals(12, service.getSymbioticMiteParasiticMiteKillPerDay(colony));
    }
}
