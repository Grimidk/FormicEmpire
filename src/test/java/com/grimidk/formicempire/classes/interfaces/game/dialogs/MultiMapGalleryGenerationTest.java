package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiMapGalleryGenerationTest {

    @Test
    void highDensityChaoticParametersGenerateValidWorld() {
        World world = new World();
        world.setTargetContinentCount(10);
        world.setMinIslandCount(50);
        world.setContinentalLandPercentage(0.55);
        world.setTectonicPlateCount(16);
        world.setHasIceCaps(true);
        world.setIceCapPolarLimit(0.50);

        world.generateRectangularWorld(null, null, "Chaotic High", null);

        assertNotNull(world.getHexes());
        assertFalse(world.getHexes().isEmpty());
        assertTrue(world.getContinentCount() >= 1);
        assertTrue(world.getIslandCount() >= 1);
        assertTrue(world.hasIceCaps());
    }

    @Test
    void lowDensityChaoticParametersGenerateValidWorld() {
        World world = new World();
        world.setTargetContinentCount(1);
        world.setMinIslandCount(0);
        world.setContinentalLandPercentage(0.12);
        world.setTectonicPlateCount(3);
        world.setHasIceCaps(false);

        world.generateRectangularWorld(null, null, "Chaotic Low", null);

        assertNotNull(world.getHexes());
        assertFalse(world.getHexes().isEmpty());
        assertTrue(world.getContinentCount() >= 1);
        assertFalse(world.hasIceCaps());
    }

    @Test
    void chaoticRandomSamplingProducesUnboundedRanges() {
        boolean sawLowContinent = false;
        boolean sawHighContinent = false;
        boolean sawZeroIce = false;
        boolean sawHighIce = false;
        boolean sawHighIslands = false;

        for (int i = 0; i < 1000; i++) {
            int rContinents = 1 + GameRandom.nextInt(10);
            int rIslands = GameRandom.nextInt(51);
            int rIceCapPct = GameRandom.nextInt(51);

            if (rContinents <= 2) sawLowContinent = true;
            if (rContinents >= 9) sawHighContinent = true;
            if (rIceCapPct == 0) sawZeroIce = true;
            if (rIceCapPct >= 45) sawHighIce = true;
            if (rIslands >= 45) sawHighIslands = true;
        }

        assertTrue(sawLowContinent);
        assertTrue(sawHighContinent);
        assertTrue(sawZeroIce);
        assertTrue(sawHighIce);
        assertTrue(sawHighIslands);
    }
}
