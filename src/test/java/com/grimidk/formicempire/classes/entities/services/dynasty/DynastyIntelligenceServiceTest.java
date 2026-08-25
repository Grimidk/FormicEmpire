package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.dynasty.IntelFact;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyIntelligenceServiceTest {

    @Test
    void intelFactTiersUnlockAtTenPercentSteps() {
        assertFalse(IntelFact.RANK_COLONIES.isUnlockedBy(9.9));
        assertTrue(IntelFact.RANK_COLONIES.isUnlockedBy(10));
        assertFalse(IntelFact.THEFT.isUnlockedBy(99.9));
        assertTrue(IntelFact.THEFT.isUnlockedBy(100));
        assertEquals(0, IntelFact.tierFor(0));
        assertEquals(5, IntelFact.tierFor(50));
        assertEquals(10, IntelFact.tierFor(100));
    }

    @Test
    void counterIntelligenceIsBasePlusIdleSpiesCapped() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_GHOST);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_SPY);
        DynastyIntelligenceService intel = dynasty.getIntelligenceService();
        assertEquals(GameNumbers.COUNTER_INTELLIGENCE_START, dynasty.getCounterIntelligence(), 0.001);
        assertEquals(GameNumbers.COUNTER_INTELLIGENCE_START, intel.computeCounterIntelligence(), 0.001);
    }

    @Test
    void intelligenceClampsAndCanSeeUsesTier() {
        Dynasty viewer = new Dynasty(1, "Viewer", true, GameConstants.SPECIES_GHOST);
        Dynasty target = new Dynasty(2, "Target", false, GameConstants.SPECIES_OMNI);
        viewer.unlockUpgrade(GameUnlocks.ASSIMILATED_STEALTH);
        assertTrue(viewer.hasUpgrade(GameUnlocks.ROLE_SPY));

        DynastyIntelligenceService intel = viewer.getIntelligenceService();
        assertFalse(intel.canSee(target, IntelFact.POPULATION));
        viewer.setIntelligenceToward(target.getId(), 30);
        assertTrue(intel.canSee(target, IntelFact.POPULATION));
        assertFalse(intel.canSee(target, IntelFact.RESOURCES));

        viewer.addIntelligenceToward(target.getId(), 200);
        assertEquals(GameNumbers.INTELLIGENCE_MAX, viewer.getIntelligenceToward(target.getId()), 0.001);
        assertTrue(intel.canSee(target, IntelFact.THEFT));
    }

    @Test
    void spyPowerMatchesDiplomatPressureTiers() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_GHOST);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_SPY);
        DynastyIntelligenceService intel = dynasty.getIntelligenceService();
        assertEquals(GameNumbers.SPY_POWER_BASE, intel.getSpyPowerPerAnt());

        dynasty.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2);
        assertEquals(GameNumbers.SPY_POWER_PRESSURE_2, intel.getSpyPowerPerAnt());

        dynasty.unlockUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_3);
        assertEquals(GameNumbers.SPY_POWER_PRESSURE_3, intel.getSpyPowerPerAnt());
    }
}
