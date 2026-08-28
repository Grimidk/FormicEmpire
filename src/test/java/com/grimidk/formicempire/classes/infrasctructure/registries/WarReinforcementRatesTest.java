package com.grimidk.formicempire.classes.infrasctructure.registries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarReinforcementRatesTest {

    @Test
    void carrierBonusUsesFloorLog2PlusOne() {
        assertEquals(0, GameNumbers.warCarrierReinforcementBonusPercent(0));
        assertEquals(1, GameNumbers.warCarrierReinforcementBonusPercent(1));
        assertEquals(2, GameNumbers.warCarrierReinforcementBonusPercent(2));
        assertEquals(2, GameNumbers.warCarrierReinforcementBonusPercent(3));
        assertEquals(3, GameNumbers.warCarrierReinforcementBonusPercent(4));
        assertEquals(4, GameNumbers.warCarrierReinforcementBonusPercent(8));
        assertEquals(15, GameNumbers.warCarrierReinforcementBonusPercent(1 << 14));
        assertEquals(15, GameNumbers.warCarrierReinforcementBonusPercent((1 << 14) + 500));
    }

    @Test
    void dailyRateCapsAtTwentyFivePercent() {
        assertEquals(0.10f, GameNumbers.warDailyReinforcementRate(0), 0.0001f);
        assertEquals(0.11f, GameNumbers.warDailyReinforcementRate(1), 0.0001f);
        assertEquals(0.25f, GameNumbers.warDailyReinforcementRate(1 << 14), 0.0001f);
    }

    @Test
    void allowanceScalesWithCombatCapacity() {
        assertEquals(100, GameNumbers.warDailyReinforcementAllowancePerLine(1000, 0));
        assertEquals(110, GameNumbers.warDailyReinforcementAllowancePerLine(1000, 1));
        assertEquals(250, GameNumbers.warDailyReinforcementAllowancePerLine(1000, 1 << 14));
    }
}
