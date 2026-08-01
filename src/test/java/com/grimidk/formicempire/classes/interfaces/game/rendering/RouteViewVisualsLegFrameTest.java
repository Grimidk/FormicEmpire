package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class RouteViewVisualsLegFrameTest {

    @Test
    void flyingWingedAntUsesFlyingLegFrame() {
        assertEquals(GameNumbers.ANT_LEG_FRAME_FLYING,
                RouteViewVisuals.resolveLegFrame(GameConstants.TYPE_DRONE, true, true, 0f, 1.2f, 1f));
        assertEquals(GameNumbers.ANT_LEG_FRAME_FLYING,
                RouteViewVisuals.resolveLegFrame(GameConstants.TYPE_PRINCESS, true, false, 0f, 0.5f, 1f));
    }

    @Test
    void groundedMovingAntCyclesWalkFrames() {
        int a = RouteViewVisuals.resolveLegFrame(GameConstants.TYPE_WORKER, false, true, 0f, 0f, 1f);
        int b = RouteViewVisuals.resolveLegFrame(GameConstants.TYPE_WORKER, false, true, 0f, 0.2f, 1f);
        assertEquals(1, a);
        assertTrue(b >= 1 && b <= GameNumbers.ANT_LEG_FRAME_COUNT);
    }

    @Test
    void idleAntStaysOnLegOne() {
        assertEquals(1, RouteViewVisuals.resolveLegFrame(GameConstants.TYPE_SOLDIER, false, false, 1.5f, 9f, 1f));
    }
}
