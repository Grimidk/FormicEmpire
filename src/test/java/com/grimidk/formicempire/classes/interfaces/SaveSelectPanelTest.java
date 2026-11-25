package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveSelectPanelTest {

    @Test
    void resolveStarterBiomeIdHandlesRandomAndSpecificSelection() {
        Set<Integer> validIds = GameConstants.getStartingBiomes().stream()
                .map(Biome::getId)
                .collect(Collectors.toSet());
        assertFalse(validIds.isEmpty());

        for (int i = 0; i < 20; i++) {
            int randomId = SaveSelectPanel.resolveStarterBiomeId(null);
            assertTrue(validIds.contains(randomId));
        }

        assertEquals(GameConstants.BIOME_DESERT.getId(),
                SaveSelectPanel.resolveStarterBiomeId(GameConstants.BIOME_DESERT));
    }

    @Test
    void pickRandomDynastyBaseNameReturnsValidNonBlankName() {
        for (int i = 0; i < 10; i++) {
            String picked = SaveSelectPanel.pickRandomDynastyBaseName("Solari");
            assertNotNull(picked);
            assertFalse(picked.isBlank());
        }
    }

    @Test
    void visibleSlotsViewportHeightFitsThreeSlotsLessThanTenSlots() {
        int threeSlots = SaveSelectPanel.computeVisibleSlotsViewportHeight(44, 3);
        int tenSlots = SaveSelectPanel.computeVisibleSlotsViewportHeight(44, SaveManager.MAX_SAVE_SLOTS);
        assertTrue(threeSlots > 0);
        assertTrue(tenSlots > threeSlots);
    }

    @Test
    void computeWheelDeltaSupportsPreciseTrackpadAndDiscreteWheel() {
        assertEquals(9.0, SaveSelectPanel.computeWheelDelta(0.25, 0), 0.001);
        assertEquals(36.0, SaveSelectPanel.computeWheelDelta(0.0, 1), 0.001);
        assertEquals(-36.0, SaveSelectPanel.computeWheelDelta(-1.0, -1), 0.001);
    }

    @Test
    void stepSmoothScrollInterpolatesClampsAndSnaps() {
        double stepped = SaveSelectPanel.stepSmoothScroll(0.0, 100.0, 600);
        assertTrue(stepped > 0.0 && stepped < 100.0);

        double clamped = SaveSelectPanel.stepSmoothScroll(590.0, 900.0, 600);
        assertTrue(clamped > 590.0 && clamped <= 600.0);

        double snapped = SaveSelectPanel.stepSmoothScroll(99.5, 100.0, 600);
        assertEquals(100.0, snapped, 0.001);

        assertEquals(0.0, SaveSelectPanel.stepSmoothScroll(20.0, 100.0, 0), 0.001);
    }

    @Test
    void scrollIndicatorFlagsReflectTopMiddleBottomAndEmpty() {
        assertFalse(SaveSelectPanel.canScrollUp(0, 600));
        assertTrue(SaveSelectPanel.canScrollDown(0, 600));

        assertTrue(SaveSelectPanel.canScrollUp(120, 600));
        assertTrue(SaveSelectPanel.canScrollDown(120, 600));

        assertTrue(SaveSelectPanel.canScrollUp(600, 600));
        assertFalse(SaveSelectPanel.canScrollDown(600, 600));

        assertFalse(SaveSelectPanel.canScrollUp(0, 0));
        assertFalse(SaveSelectPanel.canScrollDown(0, 0));
    }
}
