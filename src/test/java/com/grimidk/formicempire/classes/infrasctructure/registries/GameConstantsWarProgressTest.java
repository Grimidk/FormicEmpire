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
    void hexDefenseAppliesFiftyPercentLocalPowerBonus() {
        assertEquals(150, GameNumbers.warHexDefenseEffectivePower(100));
        assertEquals(67, GameNumbers.warHexDefenseEffectiveLossToActual(100));
    }
}
