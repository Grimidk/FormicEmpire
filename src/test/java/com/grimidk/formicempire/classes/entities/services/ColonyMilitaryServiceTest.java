package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
