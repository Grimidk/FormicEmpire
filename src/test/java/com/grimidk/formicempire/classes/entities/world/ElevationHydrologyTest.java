package com.grimidk.formicempire.classes.entities.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class ElevationHydrologyTest {

    @Test
    void hexElevationClamping() {
        Hex hex = new Hex();
        hex.setElevation(5);
        assertEquals(5, hex.getElevation());

        hex.setElevation(-3);
        assertEquals(0, hex.getElevation());

        hex.setElevation(15);
        assertEquals(10, hex.getElevation());
    }

    @Test
    void oceanBiomeElevationRanges() {
        Biome deepOcean = GameConstants.BIOME_DEEP_OCEAN;
        Biome ocean = GameConstants.BIOME_OCEAN;
        Biome shallowOcean = GameConstants.BIOME_SHALLOW_OCEAN;
        Biome peak = GameConstants.BIOME_PEAK;

        assertEquals(0, deepOcean.getMinElevation());
        assertEquals(1, deepOcean.getMaxElevation());
        assertTrue(deepOcean.supportsElevation(0));
        assertTrue(deepOcean.supportsElevation(1));
        assertFalse(deepOcean.supportsElevation(2));

        assertEquals(1, ocean.getMinElevation());
        assertEquals(2, ocean.getMaxElevation());
        assertTrue(ocean.supportsElevation(1));
        assertTrue(ocean.supportsElevation(2));
        assertFalse(ocean.supportsElevation(3));

        assertEquals(2, shallowOcean.getMinElevation());
        assertEquals(3, shallowOcean.getMaxElevation());
        assertTrue(shallowOcean.supportsElevation(2));
        assertTrue(shallowOcean.supportsElevation(3));
        assertFalse(shallowOcean.supportsElevation(4));

        assertEquals(9, peak.getMinElevation());
        assertEquals(10, peak.getMaxElevation());
        assertTrue(peak.supportsElevation(9));
        assertTrue(peak.supportsElevation(10));
        assertFalse(peak.supportsElevation(8));
    }
}
