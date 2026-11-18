package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.shared.SandboxCheatService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.WarBattleViewPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
            defenderColony.getQueens().add(new Ant(defenderColony, GameConstants.CLASS_QUEEN));
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
            defenderColony.getSoldiers().add(new Ant(defenderColony, GameConstants.CLASS_SOLDIER));
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

    @Test
    void tickAlternatesPhasesAndSkipsMissingLines() {
        WarCreatureCombatService.clear(war);
        attackerColony.getSoldiers().clear();
        attackerColony.getMajors().clear();
        attackerColony.getPrincesses().clear();
        attackerColony.getWarAssignedRoleCounts().clear();
        defenderColony.getSoldiers().clear();
        defenderColony.getMajors().clear();
        defenderColony.getPrincesses().clear();
        defenderColony.getWarAssignedRoleCounts().clear();

        for (int i = 0; i < 20; i++) {
            Ant atkInf = new Ant(attackerColony, GameConstants.CLASS_SOLDIER);
            attackerColony.getSoldiers().add(atkInf);

            Ant defInf = new Ant(defenderColony, GameConstants.CLASS_SOLDIER);
            defenderColony.getSoldiers().add(defInf);
        }
        for (int i = 0; i < 4; i++) {
            Ant atkAir = new Ant(attackerColony, GameConstants.CLASS_PRINCESS);
            attackerColony.getPrincesses().add(atkAir);

            Ant defArt = new Ant(defenderColony, GameConstants.CLASS_MAJOR);
            defenderColony.getMajors().add(defArt);
        }
        attackerColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 20);
        attackerColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_AIR_SUPPORT, 4);
        defenderColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 20);
        defenderColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_ARTILLERY, 4);
        ColonyMilitaryService.refreshColonyMilitaryPower(attackerColony);
        ColonyMilitaryService.refreshColonyMilitaryPower(defenderColony);

        WarBattleState state = WarCreatureCombatService.startBorderBattle(war, attacker, defender);
        assertNotNull(state);

        WarCreatureCombatService.tick(war);
        assertTrue(state.isCurrentPhaseAttacker());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, state.getCurrentPhaseLine());

        WarCreatureCombatService.tick(war);
        assertFalse(state.isCurrentPhaseAttacker());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, state.getCurrentPhaseLine());

        WarCreatureCombatService.tick(war);
        assertFalse(state.isCurrentPhaseAttacker());
        assertEquals(GameConstants.BATTLE_LINE_ARTILLERY, state.getCurrentPhaseLine());

        WarCreatureCombatService.tick(war);
        assertTrue(state.isCurrentPhaseAttacker());
        assertEquals(GameConstants.BATTLE_LINE_AIR_SUPPORT, state.getCurrentPhaseLine());

        WarCreatureCombatService.tick(war);
        assertTrue(state.isCurrentPhaseAttacker());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, state.getCurrentPhaseLine());

        WarCreatureCombatService.clear(war);
    }

    @Test
    void warBattleViewPanelRendersPhaseIndicatorWithoutException() {
        WarBattleViewPanel panel = new WarBattleViewPanel(war, null);
        panel.setSize(1200, 800);
        panel.doLayout();
        WarCreatureCombatService.tick(war);
        panel.refreshScene();
        BufferedImage img = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        assertDoesNotThrow(() -> panel.paint(g2d));
        g2d.dispose();
        panel.stopAnimation();
    }

    @Test
    void tickEndsBattleImmediatelyWhenActiveFrontlineHasNoLivingAntsEvenWithReserves() {
        WarCreatureCombatService.clear(war);
        WarBattleState state = WarCreatureCombatService.startBorderBattle(war, attacker, defender);
        assertNotNull(state);
        state.getDefender().getReserve(GameConstants.BATTLE_LINE_INFANTRY)
                .add(new WarBattleParticipant(new Ant(defenderColony, GameConstants.CLASS_SOLDIER),
                        defenderColony, GameConstants.BATTLE_LINE_INFANTRY, GameConstants.ROLE_WARRIOR, false));
        for (var line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : state.getDefender().getActive(line)) {
                p.applyBattleDamage(99999f);
            }
            state.getDefender().getActive(line).clear();
        }
        assertEquals(0, state.getDefender().livingActiveSize());
        assertTrue(state.getDefender().livingArmySize() > 0);
        assertEquals(WarCreatureCombatService.TickOutcome.ATTACKER_WINS, WarCreatureCombatService.tick(war));
        assertEquals(0, state.getDefender().livingActiveSize());
        assertTrue(state.getDefender().livingArmySize() > 0);
        WarCreatureCombatService.clear(war);
    }

    @Test
    void warBattleViewPanelInvokesOnBattleEndedWhenBattleStateCleared() throws Exception {
        java.util.concurrent.atomic.AtomicBoolean ended = new java.util.concurrent.atomic.AtomicBoolean(false);
        WarBattleViewPanel panel = new WarBattleViewPanel(war, null, () -> ended.set(true));
        assertFalse(ended.get());
        WarCreatureCombatService.clear(war);
        assertTrue(war.isActive());
        panel.refreshScene();
        javax.swing.SwingUtilities.invokeAndWait(() -> { });
        assertTrue(ended.get());
        panel.stopAnimation();
    }

    @Test
    void stageTransitionReplacesNumericFeedWithFreshInstanceAndSequence() {
        WarCreatureCombatService.TickOutcome tickOutcome = WarCreatureCombatService.TickOutcome.CONTINUE;
        WarBattleState firstState = WarCreatureCombatService.getState(war);
        assertNotNull(firstState);
        for (int i = 0; i < 10 && firstState.getNumericFeed().getLatestSequence() == 0; i++) {
            tickOutcome = WarCreatureCombatService.tick(war);
        }
        long firstSeq = firstState.getNumericFeed().getLatestSequence();
        assertTrue(firstSeq > 0);

        WarCreatureCombatService.clear(war);
        WarBattleState secondState = WarCreatureCombatService.startHexBattle(war, attacker, defenderColony);
        assertNotNull(secondState);
        assertTrue(secondState.getNumericFeed() != firstState.getNumericFeed());
        assertEquals(0L, secondState.getNumericFeed().getLatestSequence());
        WarCreatureCombatService.clear(war);
    }

    @Test
    void tickSkipsMissingLineAndOverrunsEmptyOpposingFrontline() {
        configureBattleLineMask(attackerColony, attacker, 2);
        configureBattleLineMask(defenderColony, defender, 2);

        WarCreatureCombatService.clear(war);
        WarBattleState missingLineState = WarCreatureCombatService.startBorderBattle(war, attacker, defender);
        assertNotNull(missingLineState);
        WarCreatureCombatService.tick(war);
        assertTrue(missingLineState.isCurrentPhaseAttacker());
        assertEquals(GameConstants.BATTLE_LINE_ARTILLERY, missingLineState.getCurrentPhaseLine());
        WarCreatureCombatService.clear(war);

        configureBattleLineMask(attackerColony, attacker, 1);
        configureBattleLineMask(defenderColony, defender, 2);
        WarBattleState overrunState = WarCreatureCombatService.startBorderBattle(war, attacker, defender);
        assertNotNull(overrunState);
        WarCreatureCombatService.tick(war);
        assertTrue(overrunState.isCurrentPhaseAttacker());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, overrunState.getCurrentPhaseLine());
        assertTrue(overrunState.getNumericFeed().getLatestSequence() > 0);
        WarCreatureCombatService.clear(war);
    }

    @Test
    void allBattleLinePresenceCombinationsProgressAndResolveWithoutSticking() {
        for (int atkMask = 1; atkMask <= 7; atkMask++) {
            for (int defMask = 1; defMask <= 7; defMask++) {
                WarCreatureCombatService.clear(war);
                configureBattleLineMask(attackerColony, attacker, atkMask);
                configureBattleLineMask(defenderColony, defender, defMask);

                WarBattleState state = WarCreatureCombatService.startBorderBattle(war, attacker, defender);
                assertNotNull(state);

                WarCreatureCombatService.TickOutcome outcome = WarCreatureCombatService.TickOutcome.CONTINUE;
                for (int i = 0; i < 400 && outcome == WarCreatureCombatService.TickOutcome.CONTINUE; i++) {
                    outcome = WarCreatureCombatService.tick(war);
                }
                assertTrue(state.getTickIndex() > 0);
                assertTrue(state.getNumericFeed().getLatestSequence() > 0,
                        "Expected dice events for atkMask=" + atkMask + " defMask=" + defMask);
                assertTrue(outcome == WarCreatureCombatService.TickOutcome.ATTACKER_WINS
                        || outcome == WarCreatureCombatService.TickOutcome.DEFENDER_WINS,
                        "Expected resolution within 400 ticks for atkMask=" + atkMask + " defMask=" + defMask);
            }
        }
        WarCreatureCombatService.clear(war);
    }

    @Test
    void randomBorderWarCheatProgressesHourlyThroughStagesWhenObserved() {
        Dynasty cheatA = new Dynasty(101, "CheatA", false, GameConstants.SPECIES_OMNI);
        Dynasty cheatB = new Dynasty(102, "CheatB", false, GameConstants.SPECIES_OMNI);
        cheatA.unlockUpgrade(GameUnlocks.STAT_ACID);
        cheatA.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        cheatB.unlockUpgrade(GameUnlocks.STAT_ACID);
        cheatB.unlockUpgrade(GameUnlocks.STAT_SKELETON);

        World cheatWorld = new World();
        Hex hexA = new Hex();
        hexA.setQ(0);
        hexA.setR(0);
        Hex hexB = new Hex();
        hexB.setQ(1);
        hexB.setR(0);
        hexA.setNorthEast(hexB);
        hexB.setSouthWest(hexA);

        Colony colA = new Colony(201, "CheatA Cap", false);
        Colony colB = new Colony(202, "CheatB Cap", false);
        cheatA.addColony(colA);
        cheatB.addColony(colB);
        cheatA.setCapital(colA);
        cheatB.setCapital(colB);
        colA.setDynasty(cheatA);
        colB.setDynasty(cheatB);
        hexA.setColony(colA);
        hexB.setColony(colB);
        seedPopulation(cheatA, colA);
        seedPopulation(cheatB, colB);
        cheatWorld.getHexes().add(hexA);
        cheatWorld.getHexes().add(hexB);
        cheatWorld.getDynastys().add(cheatA);
        cheatWorld.getDynastys().add(cheatB);

        War cheatWar = SandboxCheatService.triggerRandomBorderWar(cheatWorld);
        assertNotNull(cheatWar);
        assertTrue(cheatA.getDiplomacyService().isAtWarWith(cheatB));
        assertTrue(cheatB.getDiplomacyService().isAtWarWith(cheatA));

        WarBattleViewPanel panel = new WarBattleViewPanel(cheatWar, null);
        panel.setSize(800, 600);
        panel.doLayout();

        WarBattleState initial = WarCreatureCombatService.getState(cheatWar);
        assertNotNull(initial);
        assertEquals(0, initial.getTickIndex());

        cheatWorld.getWarService().tickWarProgressHourly();
        panel.refreshScene();
        assertEquals(1, initial.getTickIndex());
        assertTrue(initial.getNumericFeed().getLatestSequence() > 0);

        String initialPhase = cheatWar.getStagePhase().getNameKey();
        for (int h = 0; h < 600 && cheatWar.isActive() && initialPhase.equals(cheatWar.getStagePhase().getNameKey()); h++) {
            cheatWorld.getWarService().tickWarProgressHourly();
            panel.refreshScene();
        }
        assertTrue(!cheatWar.isActive() || !initialPhase.equals(cheatWar.getStagePhase().getNameKey()),
                "Border war triggered by cheat should advance stage or conclude cleanly while observed");
        panel.stopAnimation();
        WarCreatureCombatService.clear(cheatWar);
    }

    private static void configureBattleLineMask(Colony colony, Dynasty dynasty, int mask) {
        colony.getSoldiers().clear();
        colony.getMajors().clear();
        colony.getPrincesses().clear();
        colony.getWarAssignedRoleCounts().clear();
        if ((mask & 1) != 0) {
            for (int i = 0; i < 4; i++) {
                colony.getSoldiers().add(new Ant(colony, GameConstants.CLASS_SOLDIER));
            }
            colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 4);
        }
        if ((mask & 2) != 0) {
            for (int i = 0; i < 4; i++) {
                colony.getMajors().add(new Ant(colony, GameConstants.CLASS_MAJOR));
            }
            colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_ARTILLERY, 4);
        }
        if ((mask & 4) != 0) {
            for (int i = 0; i < 4; i++) {
                colony.getPrincesses().add(new Ant(colony, GameConstants.CLASS_PRINCESS));
            }
            colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_AIR_SUPPORT, 4);
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
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
            colony.getWorkers().add(new Ant(colony, GameConstants.CLASS_WORKER));
        }
        for (int i = 0; i < 40; i++) {
            colony.getSoldiers().add(new Ant(colony, GameConstants.CLASS_SOLDIER));
        }
        if (colony.getQueens().isEmpty()) {
            colony.getQueens().add(new Ant(colony, GameConstants.CLASS_QUEEN));
        }
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 20);
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
    }
}
