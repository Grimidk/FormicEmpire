package com.grimidk.formicempire.classes.entities.dynasty;

import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.services.colony.AntModService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyJobRules;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLabourService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyPopulationService;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColonyPerformanceCacheTest {

    @Test
    void totalConsumptionCacheUpdatesOnCasteSwapAndRefreshAntStats() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);

        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        colony.getWorkers().add(worker);
        int baseConsumption = colony.getTotalConsumption();
        assertEquals(colony.getStatsService().getTotalConsumption(colony), baseConsumption);

        colony.getWorkers().clear();
        colony.getMajors().add(new Ant(colony, GameConstants.CLASS_MAJOR));
        assertEquals(colony.getStatsService().getTotalConsumption(colony), colony.getTotalConsumption());

        colony.unlockBuilding(GameUnlocks.PASSIVE_FARM);
        colony.refreshAntStats();
        assertEquals(colony.getStatsService().getTotalConsumption(colony), colony.getTotalConsumption());
    }

    @Test
    void activeStateSwitchesManagedListsBetweenArrayListAndCopyOnWriteArrayList() {
        Colony colony = new Colony(1, "Nest", false);
        assertFalse(colony.getWorkers() instanceof CopyOnWriteArrayList<?>);
        assertFalse(colony.getDeadAnts() instanceof CopyOnWriteArrayList<?>);
        assertFalse(colony.getCritters() instanceof CopyOnWriteArrayList<?>);

        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        colony.getWorkers().add(worker);
        colony.setActive(true);

        assertTrue(colony.getWorkers() instanceof CopyOnWriteArrayList<?>);
        assertTrue(colony.getDeadAnts() instanceof CopyOnWriteArrayList<?>);
        assertTrue(colony.getCritters() instanceof CopyOnWriteArrayList<?>);
        assertEquals(1, colony.getWorkers().size());
        assertSame(worker, colony.getWorkers().get(0));

        colony.setActive(false);
        assertFalse(colony.getWorkers() instanceof CopyOnWriteArrayList<?>);
        assertFalse(colony.getDeadAnts() instanceof CopyOnWriteArrayList<?>);
        assertFalse(colony.getCritters() instanceof CopyOnWriteArrayList<?>);
        assertEquals(1, colony.getWorkers().size());
    }

    @Test
    void batchLifecycleMutationsPreserveCountsAcrossInactiveAndActiveColonies() {
        Dynasty dynasty = new Dynasty(1, "Test", false, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(1, "BatchNest", false);
        colony.setDynasty(dynasty);
        colony.setActive(false);

        Ant queen = new Ant(colony, GameConstants.CLASS_QUEEN);
        queen.setRole(GameConstants.ROLE_LAYER);
        colony.getQueens().add(queen);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_LAYER, 1);

        for (int i = 0; i < 6; i++) {
            Ant pupa = new Ant(colony, GameConstants.CLASS_PUPA);
            pupa.setAge(10_000);
            colony.getPupae().add(pupa);
        }
        for (int i = 0; i < 4; i++) {
            Ant dead = new Ant(colony, GameConstants.CLASS_WORKER);
            dead.goDie(colony, "test");
            colony.getDeadAnts().add(dead);
        }
        ColonyPopulationService pop = colony.getPopulationService();
        colony.setHatchRate(GameConstants.CLASS_WORKER, 100f);
        colony.setHatchRate(GameConstants.CLASS_SOLDIER, 0f);
        colony.setHatchRate(GameConstants.CLASS_MAJOR, 0f);
        colony.setHatchRate(GameConstants.CLASS_DRONE, 0f);
        colony.setHatchRate(GameConstants.CLASS_PRINCESS, 0f);
        pop.runHatching(colony);

        assertEquals(0, colony.getPupae().size());
        assertEquals(6, colony.getWorkers().size());

        dynasty.unlockUpgrade(GameUnlocks.ROLE_GRAVER);
        colony.unlockBuilding(GameUnlocks.BUILDING_COMPOSTER);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_GRAVER, 2);
        ColonyJobRules.applyDailyComposting(colony);
        assertEquals(0, colony.getDeadAnts().size());
        assertTrue(colony.getMushrooms() > 0);

        List<Ant> workers = colony.getWorkers();
        for (Ant w : workers) {
            w.setAge(100_000);
        }
        int beforeDeaths = colony.getTotalDeaths();
        pop.runAging(colony);
        assertEquals(0, colony.getWorkers().size());
        assertEquals(beforeDeaths + 6, colony.getTotalDeaths());
        assertEquals(6, colony.getDeadAnts().size());
    }

    @Test
    void intKeyedModCountsAndSinglePassMilitaryPowerMatchStringKeyedApis() {
        Dynasty dynasty = new Dynasty(1, "ModDyn", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_DEFENDER);
        dynasty.putDiplomaticModifierRemainingDays(2, GameConstants.DIPLO_MODIFIER_WAR.getNameKey(),
                GameNumbers.MODIFIER_PERMANENT);

        Colony colony = new Colony(1, "ModNest", true);
        colony.setDynasty(dynasty);
        colony.getQueens().add(new Ant(colony, GameConstants.CLASS_QUEEN));

        for (int i = 0; i < 10; i++) {
            Ant s = AntModService.createAnt(colony, GameConstants.CLASS_SOLDIER,
                    i < 6 ? AntModProfile.standard() : AntModProfile.fromCode(2121));
            colony.getSoldiers().add(s);
        }
        colony.setWarAssignedRoleCount(GameConstants.ROLE_WARRIOR, 8);
        colony.setWarAssignedRoleCount(GameConstants.ROLE_DEFENDER, 2);

        Map<String, Integer> strCounts = AntModService.aggregateModCounts(colony.getSoldiers());
        Map<Integer, Integer> intCounts = AntModService.aggregateModCountsByCode(colony.getSoldiers());
        assertEquals(strCounts.size(), intCounts.size());
        for (Map.Entry<String, Integer> entry : strCounts.entrySet()) {
            assertEquals(entry.getValue(), intCounts.get(Integer.parseInt(entry.getKey())));
        }

        float expectedFactor = AntModService.weightedModCombatFactor(colony, GameConstants.CLASS_SOLDIER, strCounts);
        assertEquals(expectedFactor, AntModService.weightedModCombatFactor(colony, GameConstants.CLASS_SOLDIER, colony.getSoldiers()), 1e-6f);
        assertEquals(expectedFactor, AntModService.weightedModCombatFactorByCode(colony, GameConstants.CLASS_SOLDIER, intCounts), 1e-6f);

        assertEquals(
                AntModService.sumMilitaryPointsForModCounts(colony, GameConstants.CLASS_SOLDIER, strCounts),
                AntModService.sumMilitaryPointsForAnts(colony, GameConstants.CLASS_SOLDIER, colony.getSoldiers()));

        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        assertEquals(colony.getMilitaryPower(), ColonyMilitaryService.computeMilitaryPower(colony));
        assertEquals(colony.getActiveMilitaryPower(), ColonyMilitaryService.computeActiveMilitaryPower(colony));
        assertEquals(colony.getReserveMilitaryPower(), ColonyMilitaryService.computeReserveMilitaryPower(colony));
    }

    @Test
    void standardModProfileReturnsSingletonInstance() {
        assertSame(AntModProfile.standard(), AntModProfile.standard());
        assertSame(AntModProfile.standard(), AntModProfile.fromCode(AntModProfile.STANDARD_CODE));
        assertSame(AntModProfile.standard(), AntModProfile.fromCode(-1));
    }

    @Test
    void eventLogCapsAtMaxSizePreservingNewestAndRetainingNpcEvents() {
        Colony npcColony = new Colony(1, "NpcNest", false);
        for (int i = 0; i < 210; i++) {
            npcColony.logEvent("evt-" + i);
        }
        List<String> consumed = npcColony.consumeEvents();
        assertEquals(200, consumed.size());
        assertEquals("evt-10", consumed.get(0));
        assertEquals("evt-209", consumed.get(199));
        assertTrue(npcColony.consumeEvents().isEmpty());
    }

    @Test
    void inactiveNuptialRemovesFlownDronesAndPrincessesWhileActiveRetainsThem() {
        Dynasty inactiveDynasty = new Dynasty(1, "InactiveDyn", false, GameConstants.SPECIES_OMNI);
        Colony inactive = new Colony(1, "InactiveNest", false);
        inactive.setDynasty(inactiveDynasty);
        inactive.setActive(false);
        inactive.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        inactive.unlockUpgrade(GameUnlocks.ROLE_BREEDER);

        Ant queen = new Ant(inactive, GameConstants.CLASS_QUEEN);
        inactive.getQueens().add(queen);
        for (int i = 0; i < 3; i++) {
            inactive.getDrones().add(new Ant(inactive, GameConstants.CLASS_DRONE));
            Ant princess = new Ant(inactive, GameConstants.CLASS_PRINCESS);
            princess.setRole(GameConstants.ROLE_BREEDER);
            inactive.getPrincesses().add(princess);
        }

        World world = new World();
        Hex hex = new Hex(GameConstants.BIOME_PLAINS, inactive, null, null, null, null, null, null);
        ColonyLabourService labour = new ColonyLabourService();
        labour.runNuptial(inactive, world, hex);

        assertEquals(0, inactive.getDrones().size());
        assertEquals(0, inactive.getPrincesses().size());

        Dynasty activeDynasty = new Dynasty(2, "ActiveDyn", true, GameConstants.SPECIES_OMNI);
        Colony active = new Colony(2, "ActiveNest", true);
        active.setDynasty(activeDynasty);
        active.setActive(true);
        active.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        active.unlockUpgrade(GameUnlocks.ROLE_BREEDER);
        active.getQueens().add(new Ant(active, GameConstants.CLASS_QUEEN));
        for (int i = 0; i < 3; i++) {
            active.getDrones().add(new Ant(active, GameConstants.CLASS_DRONE));
            Ant princess = new Ant(active, GameConstants.CLASS_PRINCESS);
            princess.setRole(GameConstants.ROLE_BREEDER);
            active.getPrincesses().add(princess);
        }
        Hex activeHex = new Hex(GameConstants.BIOME_PLAINS, active, null, null, null, null, null, null);
        labour.runNuptial(active, world, activeHex);

        assertEquals(3, active.getDrones().size());
        assertEquals(3, active.getPrincesses().size());
        assertTrue(active.getDrones().stream().allMatch(Ant::isNuptial));
        assertTrue(active.getPrincesses().stream().allMatch(Ant::isNuptial));
    }
}
