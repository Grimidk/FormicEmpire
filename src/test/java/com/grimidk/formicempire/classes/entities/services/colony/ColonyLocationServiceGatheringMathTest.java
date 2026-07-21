package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class ColonyLocationServiceGatheringMathTest {

    @Test
    void fullEfficiencyInsideRadius() {
        double e = ColonySourceService.GatheringMath.gatheringEfficiency(100, 100, 100, 100, 500f);
        assertEquals(1.0, e, 1e-9);
    }

    @Test
    void atRadiusEdgeStillFull() {
        double e = ColonySourceService.GatheringMath.gatheringEfficiency(0, 0, 300, 400, 500f);
        assertEquals(1.0, e, 1e-9);
    }

    @Test
    void beyondRadiusScalesDownWithFloor() {
        double e = ColonySourceService.GatheringMath.gatheringEfficiency(0, 0, 0, 50_000, 500f);
        assertEquals(GameNumbers.GATHER_MIN_EFFICIENCY, e, 1e-9);
    }

    @Test
    void inverseDistanceBeyondRadius() {
        double e = ColonySourceService.GatheringMath.gatheringEfficiency(0, 0, 1000, 0, 500f);
        assertEquals(0.5, e, 1e-9);
    }
}
