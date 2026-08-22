package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class HexMapGeometryTest {

    @Test
    void fittedHexSizeMatchesWidthLimitedPanel() {
        int radius = 4;
        int panelW = 800;
        int panelH = 2000;
        double hexes = HexMapGeometry.hexesAcross(radius);
        double expected = panelW / (hexes * Math.sqrt(3.0));
        expected = Math.min(expected, GameNumbers.MAP_HEX_FIT_MAX);
        if (expected < GameNumbers.MAP_HEX_FIT_MIN) {
            expected = GameNumbers.MAP_HEX_FIT_MIN;
        }
        assertEquals(
                expected,
                HexMapGeometry.fittedHexSize(
                        radius,
                        panelW,
                        panelH,
                        GameNumbers.MAP_HEX_FIT_MIN,
                        GameNumbers.MAP_HEX_FIT_MAX),
                1e-9);
    }

    @Test
    void panAfterZoomKeepsWorldPointUnderMouse() {
        double pan = 10;
        double mouseFromCenter = 40;
        double oldSize = 20;
        double newSize = 40;
        double newPan = HexMapGeometry.panAfterZoom(pan, mouseFromCenter, oldSize, newSize);
        double layoutBefore = (mouseFromCenter - pan) / oldSize;
        double layoutAfter = (mouseFromCenter - newPan) / newSize;
        assertEquals(layoutBefore, layoutAfter, 1e-9);
    }

    @Test
    void clampPanIsZeroWhenMapFitsPanel() {
        assertEquals(0.0, HexMapGeometry.clampPan(12, 400, 400), 1e-9);
        assertEquals(0.0, HexMapGeometry.clampPan(-8, 300, 500), 1e-9);
    }

    @Test
    void clampPanAllowsHalfOverflow() {
        assertEquals(50.0, HexMapGeometry.clampPan(80, 500, 400), 1e-9);
        assertEquals(-50.0, HexMapGeometry.clampPan(-80, 500, 400), 1e-9);
    }

    @Test
    void nextZoomClampsToRange() {
        assertEquals(
                GameNumbers.MAP_ZOOM_MIN,
                HexMapGeometry.nextZoom(1.0, 0.5, GameNumbers.MAP_ZOOM_MIN, GameNumbers.MAP_ZOOM_MAX),
                1e-9);
        assertEquals(
                GameNumbers.MAP_ZOOM_MAX,
                HexMapGeometry.nextZoom(3.9, 2.0, GameNumbers.MAP_ZOOM_MIN, GameNumbers.MAP_ZOOM_MAX),
                1e-9);
        double stepped = HexMapGeometry.nextZoom(
                1.0,
                GameNumbers.MAP_ZOOM_STEP,
                GameNumbers.MAP_ZOOM_MIN,
                GameNumbers.MAP_ZOOM_MAX);
        assertTrue(stepped > 1.0);
        assertTrue(stepped < GameNumbers.MAP_ZOOM_MAX);
    }

    @Test
    void worldSpanGrowsWithHexSize() {
        double small = HexMapGeometry.worldWidth(5, 10);
        double large = HexMapGeometry.worldWidth(5, 40);
        assertEquals(4.0, large / small, 1e-9);
        assertEquals(
                HexMapGeometry.worldHeight(5, 12),
                HexMapGeometry.hexesAcross(5) * 1.5 * 12,
                1e-9);
    }
}
