package com.grimidk.formicempire.classes.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class DynastyTickDiplomaticModifierTest {

    @Test
    void tickDiplomaticModifierDaysCanRemoveEmptyPairsWithoutConcurrentModification() {
        Dynasty dynasty = new Dynasty(1, "Alpha", true, GameConstants.SPECIES_OMNI);
        dynasty.putDiplomaticModifierRemainingDays(
                2,
                GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getNameKey(),
                1);

        assertDoesNotThrow(dynasty::tickDiplomaticModifierDays);
        assertTrue(dynasty.copyDiplomaticModifierRemainingDays().isEmpty());
    }
}
