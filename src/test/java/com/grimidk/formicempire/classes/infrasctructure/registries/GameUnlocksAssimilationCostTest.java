package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameUnlocksAssimilationCostTest {

    @Test
    void assimilationCostEscalatesByTwentyFivePercentPerCompletion() {
        assertEquals(10_000, GameUnlocks.getAssimilationTargetCostForCompletedCount(0));
        assertEquals(12_500, GameUnlocks.getAssimilationTargetCostForCompletedCount(1));
        assertEquals(15_000, GameUnlocks.getAssimilationTargetCostForCompletedCount(2));
        assertEquals(17_500, GameUnlocks.getAssimilationTargetCostForCompletedCount(3));
        assertEquals(20_000, GameUnlocks.getAssimilationTargetCostForCompletedCount(4));
    }

    @Test
    void dynastyUsesCompletedAssimilationCountForTargetCost() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        assertEquals(10_000, dynasty.getAssimilationTargetCost());

        dynasty.completeAssimilation(GameUnlocks.ASSIMILATION_LEAFCUTTER);
        assertEquals(12_500, dynasty.getAssimilationTargetCost());

        dynasty.completeAssimilation(GameUnlocks.ASSIMILATION_PHARAOH);
        dynasty.completeAssimilation(GameUnlocks.ASSIMILATION_MARAUDER);
        dynasty.completeAssimilation(GameUnlocks.ASSIMILATION_TRAPJAW);
        assertEquals(20_000, dynasty.getAssimilationTargetCost());
    }
}
