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
    void huntersCanDefeatCockroachWithLargeParty() {
        List<Ant> party = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Ant soldier = new Ant(colony, GameConstants.TYPE_SOLDIER);
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
}
