package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class WarCombatSkillServiceTest {

    @Test
    void redeployRegenHealsByRegenPercentOfMaxHealth() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        Colony colony = new Colony(1, "C", true);
        colony.setDynasty(dynasty);

        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        AntSubtypeService.applySubtypeStats(ant, colony);
        ant.setHealth(Math.round(ant.getMaxHealth() * 0.5f));
        float before = ant.getHealth();

        WarCombatSkillService.healAntOnRedeploy(ant);

        float expected = Math.min(ant.getMaxHealth(),
                before + GameNumbers.regenAmountFromPercent(ant.getMaxHealth(), ant.getRegen()));
        assertEquals(Math.round(expected), Math.round(ant.getHealth()));
    }

    @Test
    void boostRegenDoublesNextRedeployHeal() {
        Dynasty dynasty = new Dynasty(2, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        Colony colony = new Colony(2, "C", true);
        colony.setDynasty(dynasty);

        Ant potter = new Ant(colony, GameConstants.TYPE_WORKER);
        potter.setSubtypeProfile(AntSubtypeProfile.of(1, 1, 3, 1));
        potter.setRole(GameConstants.ROLE_POTTER);
        AntSubtypeService.applySubtypeStats(potter, colony);
        potter.setHealth(Math.round(potter.getMaxHealth() * 0.4f));
        float before = potter.getHealth();
        float baseHeal = GameNumbers.regenAmountFromPercent(potter.getMaxHealth(), potter.getRegen());

        assertTrue(WarCombatSkillService.useBoostRegen(potter, colony));
        WarCombatSkillService.healAntOnRedeploy(potter);

        float expected = Math.min(potter.getMaxHealth(),
                before + baseHeal * GameNumbers.BOOST_REGEN_NEXT_REDEPLOY_MULT);
        assertEquals(Math.round(expected), Math.round(potter.getHealth()));
        assertFalse(potter.isBoostRegenPending());
    }

    @Test
    void honeypotUnlocksPotterRole() {
        Dynasty dynasty = new Dynasty(3, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        assertTrue(dynasty.hasUpgrade(GameUnlocks.ROLE_POTTER));
        assertTrue(GameConstants.isObtainableRole(GameConstants.ROLE_POTTER));
        assertTrue(GameConstants.ROLE_POTTER.requiresSubtypes());
    }

    @Test
    void doorheadUnlocksDefenderRoleWithSubtypeRequirement() {
        Dynasty dynasty = new Dynasty(4, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD);
        assertTrue(dynasty.hasUpgrade(GameUnlocks.ROLE_DEFENDER));
        assertTrue(GameConstants.isObtainableRole(GameConstants.ROLE_DEFENDER));
        assertTrue(GameConstants.ROLE_DEFENDER.isHexDefenseOnly());
        assertTrue(GameConstants.ROLE_DEFENDER.isSubtypeRequired(GameConstants.SUBTYPE_HEAD_DOORHEAD));
    }

    @Test
    void shieldingDefendersDieBeforeQueens() {
        Dynasty dynasty = new Dynasty(5, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD);
        Colony colony = new Colony(5, "C", true);
        colony.setDynasty(dynasty);

        Ant queen = new Ant(colony, GameConstants.TYPE_QUEEN);
        colony.getQueens().add(queen);

        Ant defender = new Ant(colony, GameConstants.TYPE_SOLDIER);
        defender.setSubtypeProfile(AntSubtypeProfile.of(3, 1, 1, 1));
        defender.setRole(GameConstants.ROLE_DEFENDER);
        AntSubtypeService.applySubtypeStats(defender, colony);
        colony.getSoldiers().add(defender);
        assertTrue(WarCombatSkillService.useShielding(defender, colony));

        assertEquals(1, WarCombatSkillService.sacrificeShieldingDefenders(colony, 1));
        assertTrue(colony.getSoldiers().isEmpty());
        assertEquals(1, colony.getQueens().size());
    }

    @Test
    void hexDefenseStatMultipliersByRoleAndSide() {
        assertEquals(GameNumbers.WAR_HEX_DEFENDING_STAT_MULT,
                WarCombatSkillService.hexDefenseStatMultiplier(GameConstants.ROLE_WARRIOR, false), 0.0001f);
        assertEquals(GameNumbers.WAR_HEX_DEFENDER_ROLE_STAT_MULT,
                WarCombatSkillService.hexDefenseStatMultiplier(GameConstants.ROLE_DEFENDER, false), 0.0001f);
        assertEquals(GameNumbers.WAR_HEX_ATTACKER_STAT_MULT,
                WarCombatSkillService.hexDefenseStatMultiplier(GameConstants.ROLE_WARRIOR, true), 0.0001f);
        assertEquals(GameNumbers.WAR_HEX_SIEGE_ATTACKER_STAT_MULT,
                WarCombatSkillService.hexDefenseStatMultiplier(GameConstants.ROLE_SIEGE, true), 0.0001f);
        assertEquals(GameNumbers.WAR_HEX_DEFENDING_STAT_MULT,
                WarCombatSkillService.hexDefenseStatMultiplier(GameConstants.ROLE_SIEGE, false), 0.0001f);
    }

    @Test
    void hexAssaultUsesDynastyActivePlusSiegeAndFullColonyDefense() {
        Dynasty dynasty = new Dynasty(9, "Attacker", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_MAJOR);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD);
        Colony home = new Colony(9, "Home", true);
        home.setDynasty(dynasty);
        dynasty.addColony(home);
        for (int i = 0; i < 30; i++) {
            home.getSoldiers().add(new Ant(home, GameConstants.TYPE_SOLDIER));
        }
        for (int i = 0; i < 10; i++) {
            home.getMajors().add(new Ant(home, GameConstants.TYPE_MAJOR));
        }

        Dynasty enemy = new Dynasty(10, "Enemy", false, GameConstants.SPECIES_OMNI);
        Colony enemyColony = new Colony(10, "EC", false);
        enemyColony.getWorkers().add(new Ant(enemyColony, GameConstants.TYPE_WORKER));
        enemy.addColony(enemyColony);
        dynasty.getDiplomacyService().applyWar(enemy);
        assertTrue(dynasty.isAtWar());

        home.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 10);
        home.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 5);
        home.getWarAssignedRoleCounts().put(GameConstants.ROLE_SIEGE, 3);
        ColonyMilitaryService.refreshColonyMilitaryPower(home);
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);

        int borderActive = ColonyMilitaryService.powerForWarStanding(dynasty);
        int siege = ColonyMilitaryService.computeSiegeAssaultPower(dynasty);
        int assault = ColonyMilitaryService.computeHexAssaultAttackerPower(dynasty);
        assertEquals(borderActive + siege, assault);
        assertTrue(borderActive > 0);
        assertTrue(siege > 0);
        assertEquals(borderActive, home.getActiveMilitaryPower());

        int fullColony = ColonyMilitaryService.computeHexDefenseMilitaryPower(home);
        int population = ColonyMilitaryService.computeMilitaryPowerFromPopulation(home);
        assertEquals(population, fullColony);
        assertTrue(fullColony > ColonyMilitaryService.computeReserveMilitaryPower(home));
    }

    @Test
    void effectiveHexDefenseDefenseIsCappedAtOneHundred() {
        Dynasty dynasty = new Dynasty(8, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD);
        Colony colony = new Colony(8, "C", true);
        colony.setDynasty(dynasty);

        Ant defender = new Ant(colony, GameConstants.TYPE_SOLDIER);
        defender.setSubtypeProfile(AntSubtypeProfile.of(3, 1, 1, 1));
        defender.setRole(GameConstants.ROLE_DEFENDER);
        AntSubtypeService.applySubtypeStats(defender, colony);
        defender.setDefense(50f);

        assertEquals(100f, WarCombatSkillService.effectiveHexDefenseDefense(defender, false), 0.0001f);
        assertEquals(defender.getAttack() * GameNumbers.WAR_HEX_DEFENDER_ROLE_STAT_MULT,
                WarCombatSkillService.effectiveHexDefenseAttack(defender, false), 0.0001f);
    }

    @Test
    void defenderAndSiegeExcludedFromBorderActivePower() {
        Dynasty dynasty = new Dynasty(6, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD);
        Colony colony = new Colony(6, "C", true);
        colony.setDynasty(dynasty);
        dynasty.addColony(colony);
        for (int i = 0; i < 25; i++) {
            colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        }

        Dynasty enemy = new Dynasty(7, "Enemy", false, GameConstants.SPECIES_OMNI);
        Colony enemyColony = new Colony(7, "EC", false);
        enemyColony.getWorkers().add(new Ant(enemyColony, GameConstants.TYPE_WORKER));
        enemy.addColony(enemyColony);
        dynasty.getDiplomacyService().applyWar(enemy);
        assertTrue(dynasty.isAtWar());

        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 5);
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        int withWarriors = colony.getActiveMilitaryPower();
        assertTrue(withWarriors > 0);

        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 20);
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        assertEquals(withWarriors, colony.getActiveMilitaryPower());
        assertTrue(ColonyMilitaryService.computeHexDefenseOnlyPower(colony) > 0);
        assertTrue(GameConstants.ROLE_SIEGE.isHexDefenseOnly());
        assertFalse(GameConstants.ROLE_DEFENDER.participatesInBorderBattle());
    }
}