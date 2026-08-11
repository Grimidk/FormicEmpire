package com.grimidk.formicempire.classes.infrasctructure.registries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameConstantsWarProgressTest {

    @Test
    void warStageProgressPerHourIsThirteenPercent() {
        assertEquals(0.13f, GameNumbers.WAR_STAGE_PROGRESS_PER_HOUR, 0.0001f);
    }

    @Test
    void fourHoursOfStageProgressCoversJustOverHalfTheBar() {
        float afterFourHours = GameNumbers.WAR_STAGE_PROGRESS_PER_HOUR * 4f;
        assertEquals(0.52f, afterFourHours, 0.0001f);
    }

    @Test
    void hexDefenseAppliesRoleAwareStatMultipliers() {
        assertEquals(150, GameNumbers.warHexDefenseEffectiveDefenderPower(100, 0));
        assertEquals(300, GameNumbers.warHexDefenseEffectiveDefenderPower(0, 100));
        assertEquals(450, GameNumbers.warHexDefenseEffectiveDefenderPower(100, 100));
        assertEquals(100, GameNumbers.warHexAssaultEffectiveAttackerPower(100, 0));
        assertEquals(300, GameNumbers.warHexAssaultEffectiveAttackerPower(0, 100));
        assertEquals(400, GameNumbers.warHexAssaultEffectiveAttackerPower(100, 100));
        assertEquals(150, GameNumbers.warHexDefenseEffectivePower(100));
        assertEquals(67, GameNumbers.warHexDefenseEffectiveLossToActual(100));
        assertEquals(50, GameNumbers.warHexDefenseEffectiveLossToActual(150, 100, 300));
    }

    @Test
    void hexDefenseAttackAndDefenseRespectMultipliersAndDefenseCap() {
        assertEquals(30f, GameNumbers.applyHexDefenseAttack(20f, GameNumbers.WAR_HEX_DEFENDING_STAT_MULT), 0.0001f);
        assertEquals(60f, GameNumbers.applyHexDefenseAttack(20f, GameNumbers.WAR_HEX_DEFENDER_ROLE_STAT_MULT), 0.0001f);
        assertEquals(30f, GameNumbers.applyHexDefenseDefense(20f, GameNumbers.WAR_HEX_DEFENDING_STAT_MULT), 0.0001f);
        assertEquals(60f, GameNumbers.applyHexDefenseDefense(20f, GameNumbers.WAR_HEX_DEFENDER_ROLE_STAT_MULT), 0.0001f);
        assertEquals(100f, GameNumbers.applyHexDefenseDefense(50f, GameNumbers.WAR_HEX_DEFENDER_ROLE_STAT_MULT), 0.0001f);
    }
}
