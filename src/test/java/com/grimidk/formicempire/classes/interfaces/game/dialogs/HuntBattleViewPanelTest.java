package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.interfaces.game.dialogs.HuntBattleViewPanel.SlotPosition;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntBattleViewPanelTest {

    @Test
    void multiRingPlacementDistributesLargePartiesAcrossConcentricRings() {
        float startRadius = 48f;
        float maxRadius = 210f;

        Set<Integer> rings100 = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            SlotPosition pos = HuntBattleViewPanel.computeRingPosition(i, 100, startRadius, maxRadius, 22f, 22f);
            assertTrue(pos.radius() >= startRadius * 0.85f && pos.radius() <= maxRadius + 4.01f);
            rings100.add(Math.round(pos.radius() / 16f));
        }
        assertTrue(rings100.size() >= 3, "100 ants should occupy multiple concentric rings: " + rings100);

        Set<Integer> rings250 = new HashSet<>();
        for (int i = 0; i < 250; i++) {
            SlotPosition pos = HuntBattleViewPanel.computeRingPosition(i, 250, startRadius, maxRadius, 22f, 22f);
            assertTrue(pos.radius() <= maxRadius + 4.01f);
            rings250.add(Math.round(pos.radius() / 14f));
        }
        assertTrue(rings250.size() >= 5, "250 ants should occupy even more concentric rings: " + rings250);

        float infOuter = HuntBattleViewPanel.outerRingRadius(100, startRadius, 165f, 22f, 22f);
        float artStart = infOuter + 28f;
        SlotPosition artPos = HuntBattleViewPanel.computeRingPosition(0, 29, artStart, 280f, 22f, 22f);
        assertTrue(artPos.radius() > infOuter + 15f,
                "Artillery ring (" + artPos.radius() + ") must sit outside infantry outer ring (" + infOuter + ")");
    }

    @Test
    void critterFocusRotationFollowsShortestArcAndWrapsCleanly() {
        assertEquals(20f, HuntBattleViewPanel.wrapDegrees180(380f), 0.001f);
        assertEquals(-20f, HuntBattleViewPanel.wrapDegrees180(340f), 0.001f);
        assertEquals(180f, HuntBattleViewPanel.wrapDegrees180(180f), 0.001f);
        assertEquals(180f, HuntBattleViewPanel.wrapDegrees180(-180f), 0.001f);

        float cw = HuntBattleViewPanel.stepAngleToward(350f, 10f, 15f);
        assertEquals(5f, cw, 0.001f);

        float ccw = HuntBattleViewPanel.stepAngleToward(10f, 350f, 15f);
        assertEquals(355f, ccw, 0.001f);

        float snap = HuntBattleViewPanel.stepAngleToward(5f, 10f, 25f);
        assertEquals(10f, snap, 0.001f);
    }
}
