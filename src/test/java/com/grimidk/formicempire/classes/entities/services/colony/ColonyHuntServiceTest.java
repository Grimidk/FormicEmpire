package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.hunt.KnownHuntTarget;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonyHuntServiceTest {

    private Colony colony;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
        colony.unlockUpgrade(GameUnlocks.ROLE_SCOUT);
        colony.unlockUpgrade(GameUnlocks.ABILITY_HUNTS);
    }

    @Test
    void knownTargetCapacityMatchesSourceCapacity() {
        assertEquals(1, ColonyHuntService.getKnownTargetCapacity(colony));
        colony.unlockUpgrade(GameUnlocks.STAT_LOGISTICS_1);
        assertEquals(5, ColonyHuntService.getKnownTargetCapacity(colony));
    }

    @Test
    void discoveryStopsWhenKnownBugListIsFull() {
        colony.unlockUpgrade(GameUnlocks.STAT_LOGISTICS_1);
        for (int i = 0; i < 5; i++) {
            colony.getKnownHuntTargets().add(new KnownHuntTarget(
                    i + 1, GameConstants.TYPE_COCKROACH.getId(), 10, 20, 0, 30));
        }
        assertTrue(ColonyHuntService.isKnownTargetCapacityFull(colony));
        ColonyHuntService.registerDiscoveredBug(
                colony, GameConstants.BIOME_URBAN, null, 1);
        assertEquals(5, colony.getKnownHuntTargets().size());
    }

    @Test
    void dispatchUsesSelectedAntCountsByType() {
        colony.getKnownHuntTargets().add(new KnownHuntTarget(
                1, GameConstants.TYPE_COCKROACH.getId(), 100, 200, 0, 30));
        Ant soldier = new Ant(colony, GameConstants.CLASS_SOLDIER);
        colony.getSoldiers().add(soldier);
        Ant major = new Ant(colony, GameConstants.CLASS_MAJOR);
        colony.getMajors().add(major);
        colony.runRoleAssignment(null);

        Map<AntClass, Integer> counts = new HashMap<>();
        counts.put(GameConstants.CLASS_SOLDIER, 1);
        counts.put(GameConstants.CLASS_MAJOR, 1);
        assertTrue(ColonyHuntService.dispatchHunt(colony, 1, counts));
        assertEquals(2, colony.getHuntExpeditionForTarget(1).getParty().size());
    }

    @Test
    void canDispatchMultipleTargetsInParallel() {
        colony.getKnownHuntTargets().add(new KnownHuntTarget(
                1, GameConstants.TYPE_COCKROACH.getId(), 100, 200, 0, 30));
        colony.getKnownHuntTargets().add(new KnownHuntTarget(
                2, GameConstants.TYPE_COCKROACH.getId(), 120, 220, 0, 30));
        for (int i = 0; i < 4; i++) {
            Ant soldier = new Ant(colony, GameConstants.CLASS_SOLDIER);
            colony.getSoldiers().add(soldier);
        }
        colony.runRoleAssignment(null);

        Map<AntClass, Integer> first = new HashMap<>();
        first.put(GameConstants.CLASS_SOLDIER, 1);
        Map<AntClass, Integer> second = new HashMap<>();
        second.put(GameConstants.CLASS_SOLDIER, 1);

        assertTrue(ColonyHuntService.dispatchHunt(colony, 1, first));
        assertTrue(ColonyHuntService.dispatchHunt(colony, 2, second));
        assertEquals(2, colony.getActiveHuntExpeditions().size());
        assertFalse(ColonyHuntService.canDispatchHunt(colony, 1));
        assertFalse(ColonyHuntService.canDispatchHunt(colony, 2));
    }

    @Test
    void winChanceIncreasesWithLargerParty() {
        KnownHuntTarget target = new KnownHuntTarget(
                1, GameConstants.TYPE_COCKROACH.getId(), 100, 200, 0, 30);
        Map<AntClass, Integer> small = new HashMap<>();
        small.put(GameConstants.CLASS_SOLDIER, 1);
        ColonyHuntService.HuntDispatchPreview smallPreview =
                ColonyHuntService.previewDispatch(colony, target, small);

        for (int i = 0; i < 20; i++) {
            Ant soldier = new Ant(colony, GameConstants.CLASS_SOLDIER);
            colony.getSoldiers().add(soldier);
        }
        colony.runRoleAssignment(null);
        Map<AntClass, Integer> large = new HashMap<>();
        large.put(GameConstants.CLASS_SOLDIER, 20);
        ColonyHuntService.HuntDispatchPreview largePreview =
                ColonyHuntService.previewDispatch(colony, target, large);

        assertTrue(largePreview.winChance > smallPreview.winChance);
        assertTrue(smallPreview.winChance < 0.25f, "Single soldier should have low win chance without 50% floor");
        assertEquals(0f, ColonyHuntService.estimateWinChance(GameConstants.TYPE_COCKROACH, List.of()));
        assertEquals(GameNumbers.HUNT_COCKROACH_REWARD_PROTEIN, largePreview.rewardProtein);
        assertFalse(largePreview.travelHours <= 0f);
    }

    @Test
    void partyCapacityScalesAsCombatCapacityDividedByTenAndWinRateGrowsWithRank() {
        assertEquals(GameNumbers.COMBAT_CAPACITY_BASE / GameNumbers.HUNT_MAX_PARTY_FROM_CAPACITY_DIVISOR,
                ColonyHuntService.maxPartyCapacity(colony));
        assertEquals(100, ColonyHuntService.maxPartyCapacity(colony));

        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_COMMANDER, 1);
        assertEquals(250, ColonyHuntService.maxPartyCapacity(colony));

        colony.getDynasty().unlockUpgrade(GameUnlocks.STAT_SKELETON);
        colony.getDynasty().unlockUpgrade(GameUnlocks.STAT_ACID);

        List<Ant> duchy = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            duchy.add(new Ant(colony, GameConstants.CLASS_SOLDIER));
        }
        for (int i = 0; i < 20; i++) {
            duchy.add(new Ant(colony, GameConstants.CLASS_MAJOR));
        }
        float duchyChance = ColonyHuntService.estimateWinChance(GameConstants.TYPE_BOMBARDIER, duchy, colony);

        colony.getDynasty().unlockUpgrade(GameUnlocks.STAT_HEALTH_1);
        colony.getDynasty().unlockUpgrade(GameUnlocks.STAT_ATTACK_1);
        colony.getDynasty().unlockUpgrade(GameUnlocks.STAT_DEFENSE_1);
        List<Ant> kingdom = new ArrayList<>();
        for (int i = 0; i < 70; i++) {
            kingdom.add(new Ant(colony, GameConstants.CLASS_SOLDIER));
        }
        for (int i = 0; i < 30; i++) {
            kingdom.add(new Ant(colony, GameConstants.CLASS_MAJOR));
        }
        float kingdomChance = ColonyHuntService.estimateWinChance(GameConstants.TYPE_BOMBARDIER, kingdom, colony);

        assertTrue(duchyChance < 0.35f, "Duchy should struggle vs Bombardier: " + duchyChance);
        assertTrue(kingdomChance > 0.55f, "Kingdom should be favorable vs Bombardier: " + kingdomChance);
        assertTrue(kingdomChance > duchyChance);
    }
}
