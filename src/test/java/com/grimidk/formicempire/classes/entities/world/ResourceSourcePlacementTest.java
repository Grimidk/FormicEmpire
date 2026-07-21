package com.grimidk.formicempire.classes.entities.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

class ResourceSourcePlacementTest {

    @Test
    void spawnIsOutsideDefaultViewport() {
        int viewportW = 800;
        int viewportH = 600;
        int entranceX = 275;
        int entranceY = 230;
        int displayPx = GameNumbers.SOURCE_DISPLAY_PX_HUGE;
        NeoPoint entrance = new NeoPoint(entranceX, entranceY, WorldSpaces.OVERWORLD);

        for (int i = 0; i < 40; i++) {
            Point center = ResourceSourcePlacement.pickSpawnCenter(
                    entrance, viewportW, viewportH, displayPx, 120);
            Point topLeft = ResourceSourcePlacement.topLeftFromCenter(center.x, center.y, displayPx);
            assertTrue(
                    ResourceSourcePlacement.isFullyOutsideViewport(topLeft, displayPx, viewportW, viewportH),
                    "spawn at " + topLeft + " should be outside " + viewportW + "x" + viewportH);
            double dist = Math.hypot(center.x - entranceX, center.y - entranceY);
            int minDist = ResourceSourcePlacement.minSpawnDistanceFromEntrance(
                    entranceX, entranceY, viewportW, viewportH, displayPx);
            assertTrue(dist >= minDist, "distance " + dist + " < min " + minDist);
        }
    }

    @Test
    void displaySizeShrinksWithDepletionTier() {
        assertEquals(GameNumbers.SOURCE_DISPLAY_PX_HUGE,
                GameConstants.RESOURCE_PLANT.getDisplaySizeForSourceQuantity(ResourceType.SOURCE_QTY_HUGE));
        assertEquals(GameNumbers.SOURCE_DISPLAY_PX_SMALL,
                GameConstants.RESOURCE_PLANT.getDisplaySizeForSourceQuantity(ResourceType.SOURCE_QTY_SMALL - 1));
    }

    @Test
    void viewportIntersectionDetectsOverlap() {
        Point inside = new Point(100, 100);
        assertFalse(ResourceSourcePlacement.isFullyOutsideViewport(inside, 48, 800, 600));
        Point outside = new Point(-80, 300);
        assertTrue(ResourceSourcePlacement.isFullyOutsideViewport(outside, 48, 800, 600));
    }
}
