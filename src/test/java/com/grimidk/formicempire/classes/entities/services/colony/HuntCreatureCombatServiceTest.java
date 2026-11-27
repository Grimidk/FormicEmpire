package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.hunt.KnownHuntTarget;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class HuntCreatureCombatServiceTest {

    private Colony colony;
    private KnownHuntTarget target;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
        target = new KnownHuntTarget(1, GameConstants.TYPE_COCKROACH.getId(), 100, 200, 0, 30);
    }

    @AfterEach
    void tearDown() {
        HuntCreatureCombatService.clearAll();
    }

    @Test
    void cockroachUsesConfiguredHuntStats() {
        assertEquals(GameNumbers.HUNT_COCKROACH_HP, HuntCreatureCombatService.resolveBugMaxHealth(GameConstants.TYPE_COCKROACH),
                0.0001f);
        assertEquals(GameNumbers.HUNT_COCKROACH_ATTACK,
                HuntCreatureCombatService.resolveBugAttack(GameConstants.TYPE_COCKROACH), 0.0001f);
        assertEquals(GameNumbers.HUNT_COCKROACH_DEFENSE,
                HuntCreatureCombatService.resolveBugDefense(GameConstants.TYPE_COCKROACH), 0.0001f);
        assertEquals(GameNumbers.HUNT_COCKROACH_ATTACK_SPEED,
                HuntCreatureCombatService.resolveBugAttackSpeed(GameConstants.TYPE_COCKROACH));
    }

    @Test
    void antLionUsesInvasionCombatStats() {
        assertEquals(GameNumbers.INVASION_ANT_LION_HP,
                HuntCreatureCombatService.resolveBugMaxHealth(GameConstants.TYPE_ANT_LION), 0.0001f);
        assertEquals(GameNumbers.INVASION_ANT_LION_ATTACK,
                HuntCreatureCombatService.resolveBugAttack(GameConstants.TYPE_ANT_LION), 0.0001f);
        assertEquals(GameNumbers.INVASION_ANT_LION_DEFENSE,
                HuntCreatureCombatService.resolveBugDefense(GameConstants.TYPE_ANT_LION), 0.0001f);
        assertEquals(GameNumbers.INVASION_ANT_LION_ATTACK_SPEED,
                HuntCreatureCombatService.resolveBugAttackSpeed(GameConstants.TYPE_ANT_LION));
    }

    @Test
    void spiderUsesConfiguredHuntStats() {
        assertEquals(GameNumbers.HUNT_SPIDER_HP,
                HuntCreatureCombatService.resolveBugMaxHealth(GameConstants.TYPE_SPIDER), 0.0001f);
        assertEquals(GameNumbers.HUNT_SPIDER_ATTACK,
                HuntCreatureCombatService.resolveBugAttack(GameConstants.TYPE_SPIDER), 0.0001f);
        assertEquals(GameNumbers.HUNT_SPIDER_DEFENSE,
                HuntCreatureCombatService.resolveBugDefense(GameConstants.TYPE_SPIDER), 0.0001f);
        assertEquals(GameNumbers.HUNT_SPIDER_ATTACK_SPEED,
                HuntCreatureCombatService.resolveBugAttackSpeed(GameConstants.TYPE_SPIDER));
    }

    @Test
    void huntersCanDefeatCockroachWithLargeParty() {
        List<Ant> party = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Ant soldier = new Ant(colony, GameConstants.CLASS_SOLDIER);
            soldier.setRole(GameConstants.ROLE_WARRIOR);
            party.add(soldier);
            colony.getSoldiers().add(soldier);
        }
        HuntBattleState state = HuntCreatureCombatService.startBattle(colony, target, party);
        assertNotNull(state);
        HuntCreatureCombatService.TickOutcome outcome = HuntCreatureCombatService.TickOutcome.CONTINUE;
        for (int i = 0; i < 5000
                && outcome == HuntCreatureCombatService.TickOutcome.CONTINUE; i++) {
            outcome = HuntCreatureCombatService.tick(colony, target.getId());
        }
        assertTrue(outcome == HuntCreatureCombatService.TickOutcome.HUNTERS_WIN
                || state.getBugHealth() < GameNumbers.HUNT_COCKROACH_HP);
    }

    @Test
    void artilleryFiresBehindFrontlineInfantryAndPreservesBattleLinesOnFallenBodies() {
        Dynasty dynasty = colony.getDynasty();
        dynasty.unlockUpgrade(GameUnlocks.ROLE_WARRIOR);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_ARTILLERY);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_AIR_SUPPORT);

        List<Ant> party = new ArrayList<>();
        List<Ant> infantry = new ArrayList<>();
        List<Ant> artillery = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Ant warrior = new Ant(colony, GameConstants.CLASS_SOLDIER);
            warrior.setRole(GameConstants.ROLE_WARRIOR);
            infantry.add(warrior);
            party.add(warrior);
            colony.getSoldiers().add(warrior);
        }
        for (int i = 0; i < 2; i++) {
            Ant art = new Ant(colony, GameConstants.CLASS_MAJOR);
            art.setRole(GameConstants.ROLE_ARTILLERY);
            artillery.add(art);
            party.add(art);
            colony.getMajors().add(art);
        }
        Ant air = new Ant(colony, GameConstants.CLASS_PRINCESS);
        air.setRole(GameConstants.ROLE_AIR_SUPPORT);
        party.add(air);
        colony.getPrincesses().add(air);

        HuntBattleState state = HuntCreatureCombatService.startBattle(colony, target, party);
        assertNotNull(state);
        assertEquals(50, state.getInfantrySlotCount());
        assertEquals(2, state.getArtillerySlotCount());
        assertEquals(1, state.getAirSupportSlotCount());

        for (HuntBattleParticipant p : state.getHunters()) {
            if (p.getAnt().getRole() == GameConstants.ROLE_ARTILLERY) {
                assertEquals(GameConstants.BATTLE_LINE_ARTILLERY, p.getBattleLine());
            } else if (p.getAnt().getRole() == GameConstants.ROLE_AIR_SUPPORT) {
                assertEquals(GameConstants.BATTLE_LINE_AIR_SUPPORT, p.getBattleLine());
            } else {
                assertEquals(GameConstants.BATTLE_LINE_INFANTRY, p.getBattleLine());
            }
        }

        boolean sawAllArtilleryFullWhileInfantryLived = false;
        for (int tick = 0; tick < 500; tick++) {
            boolean anyInfantryAliveBefore = infantry.stream().anyMatch(Ant::isAlive);
            HuntCreatureCombatService.TickOutcome outcome = HuntCreatureCombatService.tick(colony, target.getId());
            boolean anyInfantryAliveAfter = infantry.stream().anyMatch(Ant::isAlive);
            boolean allArtilleryFull = artillery.stream().allMatch(a -> a.getHealth() == a.getMaxHealth());
            if (anyInfantryAliveBefore && anyInfantryAliveAfter) {
                assertTrue(allArtilleryFull, "Artillery should be shielded while Infantry remain alive");
                sawAllArtilleryFullWhileInfantryLived = true;
            }
            if (outcome != HuntCreatureCombatService.TickOutcome.CONTINUE) {
                break;
            }
        }
        assertTrue(sawAllArtilleryFullWhileInfantryLived);
        assertTrue(state.getBugHealth() < GameNumbers.HUNT_COCKROACH_HP);
        if (!state.getFallenBodies().isEmpty()) {
            assertEquals(GameConstants.BATTLE_LINE_INFANTRY, state.getFallenBodies().get(0).getBattleLine());
        }
        HuntCreatureCombatService.clear(colony, target.getId());

        KnownHuntTarget target2 = new KnownHuntTarget(2, GameConstants.TYPE_COCKROACH.getId(), 100, 200, 0, 30);
        Ant soleWarrior = new Ant(colony, GameConstants.CLASS_SOLDIER);
        soleWarrior.setRole(GameConstants.ROLE_WARRIOR);
        Ant exposedArt = new Ant(colony, GameConstants.CLASS_MAJOR);
        exposedArt.setRole(GameConstants.ROLE_ARTILLERY);
        colony.getSoldiers().add(soleWarrior);
        colony.getMajors().add(exposedArt);
        List<Ant> smallParty = List.of(soleWarrior, exposedArt);
        assertNotNull(HuntCreatureCombatService.startBattle(colony, target2, smallParty));
        HuntCreatureCombatService.TickOutcome smallOutcome = HuntCreatureCombatService.TickOutcome.CONTINUE;
        for (int tick = 0; tick < 500 && smallOutcome == HuntCreatureCombatService.TickOutcome.CONTINUE; tick++) {
            smallOutcome = HuntCreatureCombatService.tick(colony, target2.getId());
        }
        assertEquals(HuntCreatureCombatService.TickOutcome.BUG_WINS, smallOutcome);
    }
}
