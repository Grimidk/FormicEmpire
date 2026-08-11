package com.grimidk.formicempire.classes.infrasctructure.registries;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Critter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameNumbersDefenseTest {

    @Test
    void snapVolumePercentUsesTenPercentSteps() {
        assertEquals(0, GameNumbers.snapVolumePercent(-5));
        assertEquals(0, GameNumbers.snapVolumePercent(4));
        assertEquals(10, GameNumbers.snapVolumePercent(5));
        assertEquals(50, GameNumbers.snapVolumePercent(50));
        assertEquals(50, GameNumbers.snapVolumePercent(54));
        assertEquals(60, GameNumbers.snapVolumePercent(55));
        assertEquals(100, GameNumbers.snapVolumePercent(100));
        assertEquals(100, GameNumbers.snapVolumePercent(150));
    }

    @Test
    void clampDefensePercentStaysWithinZeroToOneHundred() {
        assertEquals(0f, GameNumbers.clampDefensePercent(-10f), 0.0001f);
        assertEquals(0f, GameNumbers.clampDefensePercent(Float.NaN), 0.0001f);
        assertEquals(100f, GameNumbers.clampDefensePercent(250f), 0.0001f);
        assertEquals(5f, GameNumbers.clampDefensePercent(5f), 0.0001f);
    }

    @Test
    void damageAfterDefenseAppliesPercentReduction() {
        assertEquals(75f, GameNumbers.damageAfterDefense(100f, 25f), 0.0001f);
        assertEquals(0f, GameNumbers.damageAfterDefense(100f, 100f), 0.0001f);
        assertEquals(100f, GameNumbers.damageAfterDefense(100f, 0f), 0.0001f);
    }

    @Test
    void petSpeciesHaveOneHealthZeroAttackZeroDefense() {
        assertEquals(1f, GameConstants.TYPE_APHID.getBaseHealth(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_APHID.getBaseAttack(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_APHID.getBaseDefense(), 0.0001f);

        assertEquals(1f, GameConstants.TYPE_SYMBIOTIC_MITE.getBaseHealth(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_SYMBIOTIC_MITE.getBaseAttack(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_SYMBIOTIC_MITE.getBaseDefense(), 0.0001f);

        assertEquals(1f, GameConstants.TYPE_DERMESTID.getBaseHealth(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_DERMESTID.getBaseAttack(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_DERMESTID.getBaseDefense(), 0.0001f);

        Critter aphid = new Critter(GameConstants.TYPE_APHID);
        assertEquals(1, aphid.getMaxHealth());
        assertEquals(0f, aphid.getAttack(), 0.0001f);
        assertEquals(0f, aphid.getDefense(), 0.0001f);
    }

    @Test
    void critterDefenseIsClampedToPercentRange() {
        Critter critter = new Critter(GameConstants.TYPE_APHID);
        critter.setDefense(250);
        assertEquals(100f, critter.getDefense(), 0.0001f);
        critter.setDefense(-5);
        assertEquals(0f, critter.getDefense(), 0.0001f);
    }

    @Test
    void regenAmountFromPercentIsShareOfMaxHealth() {
        assertEquals(10f, GameNumbers.regenAmountFromPercent(100f, 10f), 0.0001f);
        assertEquals(50f, GameNumbers.regenAmountFromPercent(500f, 10f), 0.0001f);
        assertEquals(0f, GameNumbers.regenAmountFromPercent(100f, 0f), 0.0001f);
    }

    @Test
    void juvenileAndDroneTypesAreNonCombat() {
        assertEquals(0.01f, GameConstants.TYPE_EGG.getHealtMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_EGG.getAttackMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_EGG.getRegenMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_EGG.getAttackSpeedMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_EGG.getDefenseMult(), 0.0001f);

        assertEquals(0.01f, GameConstants.TYPE_LARVA.getHealtMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_LARVA.getAttackMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_LARVA.getRegenMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_LARVA.getAttackSpeedMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_LARVA.getDefenseMult(), 0.0001f);

        assertEquals(0.01f, GameConstants.TYPE_PUPA.getHealtMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_PUPA.getAttackMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_PUPA.getRegenMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_PUPA.getAttackSpeedMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_PUPA.getDefenseMult(), 0.0001f);

        assertEquals(0.01f, GameConstants.TYPE_DRONE.getHealtMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_DRONE.getAttackMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_DRONE.getRegenMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_DRONE.getAttackSpeedMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_DRONE.getDefenseMult(), 0.0001f);
    }

    @Test
    void combatTypesUseTenPercentRegenAndTypeDefense() {
        assertEquals(1f, GameConstants.TYPE_WORKER.getRegenMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_WORKER.getDefenseMult(), 0.0001f);
        assertEquals(1f, GameConstants.TYPE_SOLDIER.getRegenMult(), 0.0001f);
        assertEquals(0f, GameConstants.TYPE_SOLDIER.getDefenseMult(), 0.0001f);
        assertEquals(1f, GameConstants.TYPE_MAJOR.getRegenMult(), 0.0001f);
        assertEquals(20f, GameConstants.TYPE_MAJOR.getDefenseMult(), 0.0001f);
        assertEquals(1f, GameConstants.TYPE_QUEEN.getRegenMult(), 0.0001f);
        assertEquals(20f, GameConstants.TYPE_QUEEN.getDefenseMult(), 0.0001f);
        assertEquals(10, GameNumbers.ANT_REGEN_PERCENT_BASE);
    }
}
