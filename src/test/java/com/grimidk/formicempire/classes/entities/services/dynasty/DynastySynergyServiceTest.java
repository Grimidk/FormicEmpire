package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.entities.Dynasty;
import org.junit.jupiter.api.Test;

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
    }

    @Test
    void superVenomRevokedWhenRequirementLost() {
        Dynasty dynasty = new Dynasty(4, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DEADLYVENOM);
        assertTrue(dynasty.hasUpgrade(GameUnlocks.SYNERGY_SUPER_VENOM));

        dynasty.revokeUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM);
        DynastySynergyService.refreshUnlocked(dynasty);

        assertFalse(dynasty.hasUpgrade(GameUnlocks.SYNERGY_SUPER_VENOM));
        assertFalse(DynastySynergyService.isUnlocked(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY));
    }
}
