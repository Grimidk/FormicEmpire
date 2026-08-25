package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarJumpingCombatTest {

    private Dynasty attacker;
    private Dynasty defender;
    private Colony defenderColony;
    private World world;
    private War war;

    @BeforeEach
    void setUp() {
        attacker = new Dynasty(1, "Attacker", true, GameConstants.SPECIES_OMNI);
        defender = new Dynasty(2, "Defender", false, GameConstants.SPECIES_OMNI);
        unlockCombatBasics(attacker);
        unlockCombatBasics(defender);
        defender.unlockUpgrade(GameUnlocks.TYPE_MAJOR);
        defender.unlockUpgrade(GameUnlocks.ROLE_ARTILLERY);

        world = buildBorderWorld();
        war = world.getWarService().beginWar(attacker, defender);
        assertNotNull(war);
        defenderColony = defender.getColonies().get(0);
    }

    @Test
    void assimilatedJumpingUnlocksJumpingAbility() {
        attacker.unlockUpgrade(GameUnlocks.ASSIMILATED_JUMPING);
        assertTrue(attacker.hasUpgrade(GameUnlocks.ABILITY_JUMPING));
    }

    @Test
    void jumpingAbilityLetsInfantryMeleeTargetArtillery() {
        attacker.unlockUpgrade(GameUnlocks.ASSIMILATED_JUMPING);
        WarBattleSideState defenderSide = buildDefenderSide();

        List<WarBattleParticipant> withoutAbility = WarCreatureCombatService.eligibleTargetsForSkill(
                GameConstants.BATTLE_LINE_INFANTRY, defenderSide, defender, GameConstants.SKILL_BASIC_BITE);
        List<WarBattleParticipant> withAbility = WarCreatureCombatService.eligibleTargetsForSkill(
                GameConstants.BATTLE_LINE_INFANTRY, defenderSide, attacker, GameConstants.SKILL_BASIC_BITE);

        assertTrue(withoutAbility.isEmpty());
        assertFalse(withAbility.isEmpty());
        assertTrue(withAbility.stream().allMatch(
                p -> p.getBattleLine() == GameConstants.BATTLE_LINE_ARTILLERY));
    }

    @Test
    void jumpingAbilityUsesOnePointTwoMeleeDamageMultiplier() {
        assertEquals(1.2f, GameNumbers.ASSIMILATED_JUMPING_MELEE_DAMAGE_MULT, 0.0001f);
    }

    private WarBattleSideState buildDefenderSide() {
        WarCreatureCombatService.clear(war);
        configureArtilleryOnlyDefender();
        WarCreatureCombatService.startBorderBattle(war, attacker, defender);
        WarBattleState state = WarCreatureCombatService.getState(war);
        assertNotNull(state);
        return state.getDefender();
    }

    private void configureArtilleryOnlyDefender() {
        defenderColony.getSoldiers().clear();
        defenderColony.getMajors().clear();
        defenderColony.getWarAssignedRoleCounts().clear();
        defenderColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_ARTILLERY, 5);
        for (int i = 0; i < 5; i++) {
            Ant major = new Ant(defenderColony, GameConstants.TYPE_MAJOR);
            major.setRole(GameConstants.ROLE_ARTILLERY);
            defenderColony.getMajors().add(major);
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(defenderColony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(defender);
    }

    private static void unlockCombatBasics(Dynasty dynasty) {
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
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
