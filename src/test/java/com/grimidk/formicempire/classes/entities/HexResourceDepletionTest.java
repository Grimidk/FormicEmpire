package com.grimidk.formicempire.classes.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class HexResourceDepletionTest {

    @Test
    void cappedDepletionRespectsMax() {
        Hex h = new Hex();
        h.setNonWaterResourceSourcesGenerated(400 * GameConstants.HEX_RESOURCE_DEPLETION_SOURCES_PER_PERCENT);
        assertEquals(100, h.getResourceDepletionPercent());
        assertEquals(80, h.getResourceDepletionPercentCapped(GameConstants.HEX_SUSTAIN_MAX_DEPLETION_PCT));
        assertEquals(100, h.getResourceDepletionPercentCapped(100));
    }
}
