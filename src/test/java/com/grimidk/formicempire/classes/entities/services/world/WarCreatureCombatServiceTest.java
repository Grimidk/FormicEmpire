package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarCreatureCombatServiceTest {

    private Dynasty attacker;
    private Dynasty defender;
    private Colony attackerColony;
    private Colony defenderColony;
    private World world;
    private War war;

    @BeforeEach
    void setUp() {
        attacker = new Dynasty(1, "Attacker", true, GameConstants.SPECIES_OMNI);
        defender = new Dynasty(2, "Defender", false, GameConstants.SPECIES_OMNI);
        attacker.unlockUpgrade(GameUnlocks.STAT_ACID);
        attacker.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        defender.unlockUpgrade(GameUnlocks.STAT_ACID);
        defender.unlockUpgrade(GameUnlocks.STAT_SKELETON);

        world = buildBorderWorld();
        war = world.getWarService().beginWar(attacker, defender);
        assertNotNull(war);
        attackerColony = attacker.getColonies().get(0);
        defenderColony = defender.getColonies().get(0);
    }

    @Test
    void borderBattleSeatsForcesAndSyncsDeployedCounts() {
        WarBattleState state = WarCreatureCombatService.getState(war);
        assertNotNull(state);
        assertFalse(state.isHexAssault());
        assertTrue(state.getAttacker().livingArmySize() >= state.getAttacker().livingActiveSize());
        assertTrue(war.getDeployedActiveAttacker() > 0);
        assertTrue(war.getDeployedActiveDefender() > 0);
    }

    @Test
    void hexDefensePutsDefendersAndQueensOnActiveLine() {
        WarCreatureCombatService.clear(war);
        defenderColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 5);
        if (defenderColony.getQueens().isEmpty()) {
            defenderColony.getQueens().add(new Ant(defenderColony, GameConstants.TYPE_QUEEN));
        }

        WarBattleState state = WarCreatureCombatService.startHexBattle(war, attacker, defenderColony);
        assertNotNull(state);
        assertTrue(state.isHexAssault());
        assertTrue(state.getDefender().hasLivingQueen());
        assertTrue(state.getDefender().hasLivingDefenders());
        assertTrue(war.getDeployedReserveDefender() > 0);
    }

    @Test
    void borderBattleCanResolveByCasualtyRatioWithoutProgressCap() {
        WarCreatureCombatService.clear(war);
        defenderColony.getSoldiers().clear();
        defenderColony.getWarAssignedRoleCounts().clear();
        defenderColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 5);
        for (int i = 0; i < 5; i++) {
            defenderColony.getSoldiers().add(new Ant(defenderColony, GameConstants.TYPE_SOLDIER));
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(defenderColony);

        WarCreatureCombatService.startBorderBattle(war, attacker, defender);
        WarCreatureCombatService.TickOutcome outcome = WarCreatureCombatService.TickOutcome.CONTINUE;
        for (int i = 0; i < 500 && outcome == WarCreatureCombatService.TickOutcome.CONTINUE; i++) {
            outcome = WarCreatureCombatService.tick(war);
        }
        assertTrue(outcome == WarCreatureCombatService.TickOutcome.ATTACKER_WINS
                || outcome == WarCreatureCombatService.TickOutcome.DEFENDER_WINS);
        WarBattleState state = WarCreatureCombatService.getState(war);
        assertNotNull(state);
        if (outcome == WarCreatureCombatService.TickOutcome.ATTACKER_WINS) {
            assertTrue(state.getDefender().getDeadCount()
                    >= Math.ceil(state.getDefender().getStartingArmySize() * GameNumbers.WAR_BATTLE_ARMY_DEFEAT_RATIO)
                    || state.getDefender().livingArmySize() <= 0);
        }
    }

    @Test
    void clearRestoresTemporarilyAssignedRoles() {
        WarCreatureCombatService.clear(war);
        Ant soldier = defenderColony.getSoldiers().get(0);
        soldier.setRole(null);
        defenderColony.getWarAssignedRoleCounts().clear();
        defenderColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 1);

        WarCreatureCombatService.startBorderBattle(war, attacker, defender);
        assertEquals(GameConstants.ROLE_WARRIOR, soldier.getRole());
        WarCreatureCombatService.clear(war);
        assertEquals(null, soldier.getRole());
    }

    @Test
    void concurrentWarsDoNotShareTheSameAntInstances() {
        Dynasty secondEnemy = new Dynasty(3, "Second Enemy", false, GameConstants.SPECIES_OMNI);
        secondEnemy.unlockUpgrade(GameUnlocks.STAT_ACID);
        secondEnemy.unlockUpgrade(GameUnlocks.STAT_SKELETON);

        Hex secondHex = new Hex();
        secondHex.setQ(0);
        secondHex.setR(1);
        Hex attackerHex = world.getHexes().get(0);
        attackerHex.setSouth(secondHex);
        secondHex.setNorth(attackerHex);

        Colony secondColony = new Colony(12, "Second Capital", false);
        secondEnemy.addColony(secondColony);
        secondEnemy.setCapital(secondColony);
        secondColony.setDynasty(secondEnemy);
        secondHex.setColony(secondColony);
        seedPopulation(secondEnemy, secondColony);
        world.getHexes().add(secondHex);
        world.getDynastys().add(secondEnemy);

        WarCreatureCombatService.clear(war);
        WarCreatureCombatService.startBorderBattle(war, attacker, defender);
        War second = world.getWarService().beginWar(attacker, secondEnemy);
        assertNotNull(second);
        assertEquals(2, world.getWarService().getActiveWars().size());

        WarBattleState firstState = WarCreatureCombatService.getState(war);
        WarBattleState secondState = WarCreatureCombatService.getState(second);
        assertNotNull(firstState);
        assertNotNull(secondState);

        java.util.IdentityHashMap<Ant, Boolean> firstAnts = collectLivingAnts(firstState);
        java.util.IdentityHashMap<Ant, Boolean> secondAnts = collectLivingAnts(secondState);
        assertFalse(firstAnts.isEmpty());
        assertFalse(secondAnts.isEmpty());
        for (Ant ant : firstAnts.keySet()) {
            assertFalse(secondAnts.containsKey(ant),
                    "ant claimed by both wars would desync health/roles and break combat");
        }

        int firstAttacker = countLivingOnSide(firstState.getAttacker());
        int secondAttacker = countLivingOnSide(secondState.getAttacker());
        assertTrue(Math.abs(firstAttacker - secondAttacker) <= 1,
                "attacker fronts should split evenly, got " + firstAttacker + " vs " + secondAttacker);

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 40; i++) {
                WarCreatureCombatService.tick(war);
                WarCreatureCombatService.tick(second);
            }
        });

        WarCreatureCombatService.clear(war);
        WarCreatureCombatService.clear(second);
    }

    @Test
    void multiFrontWarsSplitActiveMilitaryEvenlyAcrossAttackerFronts() {
        Dynasty secondEnemy = new Dynasty(3, "Second Enemy", false, GameConstants.SPECIES_OMNI);
        secondEnemy.unlockUpgrade(GameUnlocks.STAT_ACID);
        secondEnemy.unlockUpgrade(GameUnlocks.STAT_SKELETON);

        Hex secondHex = new Hex();
        secondHex.setQ(0);
        secondHex.setR(1);
        Hex attackerHex = world.getHexes().get(0);
        attackerHex.setSouth(secondHex);
        secondHex.setNorth(attackerHex);

        Colony secondColony = new Colony(12, "Second Capital", false);
        secondEnemy.addColony(secondColony);
        secondEnemy.setCapital(secondColony);
        secondColony.setDynasty(secondEnemy);
        secondHex.setColony(secondColony);
        seedPopulation(secondEnemy, secondColony);
        world.getHexes().add(secondHex);
        world.getDynastys().add(secondEnemy);

        int warriorQuota = attackerColony.getWarAssignedRoleCount(GameConstants.ROLE_WARRIOR);
        assertEquals(20, warriorQuota);

        WarCreatureCombatService.clear(war);
        WarCreatureCombatService.startBorderBattle(war, attacker, defender);
        assertEquals(warriorQuota,
                countRoleOnSide(WarCreatureCombatService.getState(war).getAttacker(), GameConstants.ROLE_WARRIOR));

        War second = world.getWarService().beginWar(attacker, secondEnemy);
        assertNotNull(second);

        int firstWarriors = countRoleOnSide(WarCreatureCombatService.getState(war).getAttacker(),
                GameConstants.ROLE_WARRIOR);
        int secondWarriors = countRoleOnSide(WarCreatureCombatService.getState(second).getAttacker(),
                GameConstants.ROLE_WARRIOR);
        int expectedShare = (warriorQuota + 1) / 2;
        assertEquals(expectedShare, firstWarriors);
        assertEquals(expectedShare, secondWarriors);

        WarCreatureCombatService.clear(war);
        WarCreatureCombatService.clear(second);
    }

    private static int countLivingOnSide(WarBattleSideState side) {
        return side == null ? 0 : side.livingArmySize();
    }

    private static int countRoleOnSide(WarBattleSideState side, AntRole role) {
        int count = 0;
        if (side == null || role == null) {
            return 0;
        }
        for (var line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : side.getActive(line)) {
                if (p != null && p.isAlive() && p.getAnt() != null && p.getAnt().getRole() == role) {
                    count++;
                }
            }
            for (WarBattleParticipant p : side.getReserve(line)) {
                if (p != null && p.isAlive() && p.getAnt() != null && p.getAnt().getRole() == role) {
                    count++;
                }
            }
        }
        return count;
    }

    private static java.util.IdentityHashMap<Ant, Boolean> collectLivingAnts(WarBattleState state) {
        java.util.IdentityHashMap<Ant, Boolean> ants = new java.util.IdentityHashMap<>();
        collectSideAnts(state.getAttacker(), ants);
        collectSideAnts(state.getDefender(), ants);
        return ants;
    }

    private static void collectSideAnts(WarBattleSideState side, java.util.IdentityHashMap<Ant, Boolean> ants) {
        for (var line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : side.getActive(line)) {
                if (p != null && p.isAlive() && p.getAnt() != null) {
                    ants.put(p.getAnt(), Boolean.TRUE);
                }
            }
            for (WarBattleParticipant p : side.getReserve(line)) {
                if (p != null && p.isAlive() && p.getAnt() != null) {
                    ants.put(p.getAnt(), Boolean.TRUE);
                }
            }
        }
    }

    private World buildBorderWorld() {
        World built = new World();
        Hex aggressorHex = new Hex();
        Hex defenderHex = new Hex();
        aggressorHex.setQ(0);
        aggressorHex.setR(0);
        defenderHex.setQ(1);
        defenderHex.setR(0);
        aggressorHex.setNorthEast(defenderHex);
        defenderHex.setSouthWest(aggressorHex);

        Colony atk = new Colony(10, "Attacker Capital", true);
        Colony def = new Colony(11, "Defender Border", false);
        attacker.addColony(atk);
        defender.addColony(def);
        attacker.setCapital(atk);
        defender.setCapital(def);
        atk.setDynasty(attacker);
        def.setDynasty(defender);
        aggressorHex.setColony(atk);
        defenderHex.setColony(def);
        seedPopulation(attacker, atk);
        seedPopulation(defender, def);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(aggressorHex);
        hexes.add(defenderHex);
        built.setHexes(hexes);
        built.getDynastys().add(attacker);
        built.getDynastys().add(defender);
        return built;
    }

    private static void seedPopulation(Dynasty dynasty, Colony colony) {
        int needed = GameNumbers.WAR_DECLARATION_MIN_POPULATION
                - dynasty.getStatService().getTotalPopulation(dynasty);
        for (int i = 0; i < Math.max(0, needed); i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        for (int i = 0; i < 40; i++) {
            colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        }
        if (colony.getQueens().isEmpty()) {
            colony.getQueens().add(new Ant(colony, GameConstants.TYPE_QUEEN));
        }
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 20);
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
    }
}
