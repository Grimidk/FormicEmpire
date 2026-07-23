package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStatsService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastySynergyServiceTest {

    @Test
    void superVenomUnlocksWhenSynergyResearchedAndBothVenomsOwned() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DEADLYVENOM);

        assertTrue(DynastySynergyService.isUnlocked(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY));
        assertTrue(DynastySynergyService.hasAnyUnlocked(dynasty));
        assertTrue(dynasty.hasUpgrade(GameUnlocks.SYNERGY_SUPER_VENOM));
    }

    @Test
    void superVenomDoesNotUnlockWithoutSynergyAbility() {
        Dynasty dynasty = new Dynasty(2, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DEADLYVENOM);

        DynastySynergyService.refreshUnlocked(dynasty);

        assertFalse(DynastySynergyService.isUnlocked(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY));
        assertFalse(DynastySynergyService.hasAnyUnlocked(dynasty));
    }

    @Test
    void superVenomDoesNotUnlockWithOnlyOneRequirement() {
        Dynasty dynasty = new Dynasty(3, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);

        DynastySynergyService.refreshUnlocked(dynasty);

        assertFalse(DynastySynergyService.isUnlocked(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY));
        assertTrue(DynastySynergyService.isPartiallyComplete(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY));
        assertTrue(DynastySynergyService.isVisibleInPanel(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY));
        assertEquals(1, DynastySynergyService.countRequirementsMet(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY));
    }

    @Test
    void superVenomInactiveWhenRequirementLostButRewardPersists() {
        Dynasty dynasty = new Dynasty(4, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DEADLYVENOM);
        assertTrue(dynasty.hasUpgrade(GameUnlocks.SYNERGY_SUPER_VENOM));

        dynasty.revokeUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        DynastySynergyService.refreshUnlocked(dynasty);

        assertTrue(dynasty.hasUpgrade(GameUnlocks.SYNERGY_SUPER_VENOM));
        assertFalse(DynastySynergyService.isActive(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY));
        assertTrue(DynastySynergyService.hasAchieved(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY));
    }

    @Test
    void activeSynergySupersedesRequirementUpgrades() {
        Dynasty dynasty = new Dynasty(5, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DEADLYVENOM);

        assertTrue(DynastySynergyService.isUpgradeSuperseded(dynasty, GameUnlocks.ASSIMILATED_FIREVENOM));
        assertTrue(DynastySynergyService.isUpgradeSuperseded(dynasty, GameUnlocks.ASSIMILATED_DEADLYVENOM));
        assertFalse(DynastySynergyService.isUpgradeSuperseded(dynasty, GameUnlocks.ASSIMILATED_STINGING));
    }

    @Test
    void playerDynastyQueuesSynergyAlertWhenUnlocked() {
        Dynasty dynasty = new Dynasty(6, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DEADLYVENOM);

        assertEquals(1, dynasty.drainPendingSynergyAlerts().size());
        assertTrue(dynasty.drainPendingSynergyAlerts().isEmpty());
    }

    @Test
    void npcDynastyDoesNotQueueSynergyAlert() {
        Dynasty dynasty = new Dynasty(7, "NPC", false, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DEADLYVENOM);

        assertTrue(dynasty.drainPendingSynergyAlerts().isEmpty());
    }

    @Test
    void superVenomDamageUsesRuntimeActiveStateNotAchievedMarker() {
        Dynasty dynasty = new Dynasty(8, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DEADLYVENOM);
        dynasty.revokeUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);

        assertTrue(dynasty.hasUpgrade(GameUnlocks.SYNERGY_SUPER_VENOM));
        assertEquals(5f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
    }
}
