package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.constants.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColonyMilitaryServiceTest {

    @Test
    void typePointsUseConfiguredWeights() {
        int points = ColonyMilitaryService.computeTypePoints(100, 10, 4, 2, 1);
        assertEquals(100 + 50 + 60 + 20 + 50, points);
    }

    @Test
    void statMultiplierIsOneWithFullCombatUpgrades() {
        assertEquals(1f, ColonyMilitaryService.computeStatMultiplier(true, true), 0.0001f);
    }

    @Test
    void statMultiplierScalesDownWithoutUpgrades() {
        assertEquals(0f, ColonyMilitaryService.computeStatMultiplier(false, false), 0.0001f);
    }

    @Test
    void colonyPowerIsTypePointsTimesStatMultiplier() {
        Colony colony = new Colony(1, "Test", true);
        Dynasty dynasty = new Dynasty(1, "Dynasty", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        colony.setDynasty(dynasty);

        for (int i = 0; i < 20; i++) {
            colony.getWorkers().add(new com.grimidk.formicempire.classes.entities.Ant(colony, GameConstants.TYPE_WORKER));
        }
        for (int i = 0; i < 4; i++) {
            colony.getSoldiers().add(new com.grimidk.formicempire.classes.entities.Ant(colony, GameConstants.TYPE_SOLDIER));
        }

        int expected = Math.round(ColonyMilitaryService.computeTypePoints(20, 4, 0, 0, 0) * 1f);
        assertEquals(expected, ColonyMilitaryService.computeMilitaryPower(colony));
    }

    @Test
    void savedColonyMatchesLiveFormula() {
        Savefile.SavedColony saved = new Savefile.SavedColony();
        saved.workers = 50;
        saved.soldiers = 10;
        saved.majors = 3;
        saved.princesses = 1;
        saved.queens = 1;

        Dynasty dynasty = new Dynasty(2, "Rival", false, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);

        int typePoints = 50 + 50 + 45 + 10 + 50;
        assertEquals(typePoints, ColonyMilitaryService.computeFromSavedColony(saved, dynasty));
    }

    @Test
    void stingerSoldiersIncreaseMilitaryPower() {
        Colony colony = new Colony(3, "Test", true);
        Dynasty dynasty = new Dynasty(3, "Dynasty", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        colony.setDynasty(dynasty);

        com.grimidk.formicempire.classes.entities.Ant standard = new com.grimidk.formicempire.classes.entities.Ant(
                colony, GameConstants.TYPE_SOLDIER);
        colony.getSoldiers().add(standard);

        com.grimidk.formicempire.classes.entities.Ant stinger = new com.grimidk.formicempire.classes.entities.Ant(
                colony, GameConstants.TYPE_SOLDIER);
        stinger.setSubtypeProfile(AntSubtypeProfile.of(1, 1, 2, 1));
        AntSubtypeService.applySubtypeStats(stinger, colony);

        int standardPower = ColonyMilitaryService.computeMilitaryPowerFromPopulation(colony);
        colony.getSoldiers().clear();
        colony.getSoldiers().add(stinger);
        int stingerPower = ColonyMilitaryService.computeMilitaryPowerFromPopulation(colony);

        assertTrue(stingerPower > standardPower);
    }

    @Test
    void savedColonySubtypeCountsAffectMilitaryPower() {
        Savefile.SavedColony saved = new Savefile.SavedColony();
        saved.soldiers = 0;
        saved.soldierSubtypes = Map.of("1121", 4);

        Dynasty dynasty = new Dynasty(4, "Rival", false, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);

        Savefile.SavedColony standardOnly = new Savefile.SavedColony();
        standardOnly.soldiers = 4;

        assertTrue(ColonyMilitaryService.computeFromSavedColony(saved, dynasty)
                > ColonyMilitaryService.computeFromSavedColony(standardOnly, dynasty));
    }

    @Test
    void militaryStrengthDeltaScalesToMaxAtElevenToOne() {
        assertEquals(10, ColonyMilitaryService.getMilitaryStrengthDelta(110, 10));
        assertEquals(0, ColonyMilitaryService.getMilitaryStrengthDelta(10, 10));
        assertEquals(1, ColonyMilitaryService.getMilitaryStrengthDelta(20, 10));
    }

    @Test
    void militaryReputationAdjustmentIsAsymmetric() {
        assertEquals(-10, ColonyMilitaryService.getMilitaryReputationAdjustment(110, 10));
        assertEquals(10, ColonyMilitaryService.getMilitaryReputationAdjustment(10, 110));
    }

    @Test
    void militaryLoyaltyAdjustmentVsCapital() {
        assertEquals(10, ColonyMilitaryService.getMilitaryLoyaltyAdjustment(10, 110, false));
        assertEquals(-10, ColonyMilitaryService.getMilitaryLoyaltyAdjustment(110, 10, false));
        assertEquals(0, ColonyMilitaryService.getMilitaryLoyaltyAdjustment(100, 50, true));
    }

    @Test
    void aiWarDeclarationChanceBlocksMuchStrongerTargets() {
        assertEquals(0, ColonyMilitaryService.computeAiWarDeclarationChance(100, 201, 0));
        assertTrue(ColonyMilitaryService.computeAiWarDeclarationChance(100, 200, 0) > 0);
    }

    @Test
    void aiWarDeclarationChanceRisesWithRelativeWeaknessEvenAtNeutralReputation() {
        double equalNeutral = ColonyMilitaryService.computeAiWarDeclarationChance(
                100, 100, GameConstants.REPUTATION_NEUTRAL.getMinScore());
        double weakNeutral = ColonyMilitaryService.computeAiWarDeclarationChance(
                100, 200, GameConstants.REPUTATION_NEUTRAL.getMinScore());
        assertEquals(0, equalNeutral, 0.0001);
        assertTrue(weakNeutral > 0);
    }

    @Test
    void aiWarDeclarationChanceRisesWithHostilityWhenStrengthIsSimilar() {
        double hostile = ColonyMilitaryService.computeAiWarDeclarationChance(100, 100, 0);
        double neutral = ColonyMilitaryService.computeAiWarDeclarationChance(
                100, 100, GameConstants.REPUTATION_NEUTRAL.getMinScore());
        assertTrue(hostile > neutral);
    }
}
