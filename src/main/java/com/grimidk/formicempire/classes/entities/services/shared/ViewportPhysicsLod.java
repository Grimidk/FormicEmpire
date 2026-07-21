package com.grimidk.formicempire.classes.entities.services.shared;

import java.awt.Rectangle;

import com.grimidk.formicempire.classes.entities.Ant;

public final class ViewportPhysicsLod {

    public static final int MARGIN_PX = 64;
    public static final int OFF_VIEWPORT_AI_PERIOD = 16;
    public static final int OFF_VIEWPORT_MOVE_PERIOD = 4;

    private ViewportPhysicsLod() {
    }

    public static boolean isLodActive(Rectangle viewportBounds) {
        return viewportBounds != null && viewportBounds.width > 0 && viewportBounds.height > 0;
    }

    public static Rectangle expandViewport(Rectangle viewport, int marginPx) {
        Rectangle v = new Rectangle(viewport);
        v.x -= marginPx;
        v.y -= marginPx;
        v.width += 2 * marginPx;
        v.height += 2 * marginPx;
        return v;
    }

    public static boolean antIntersectsViewport(Rectangle viewportBounds, int antX, int antY, int spriteW, int spriteH) {
        if (!isLodActive(viewportBounds)) {
            return true;
        }
        int m = MARGIN_PX;
        int ax2 = antX + Math.max(1, spriteW);
        int ay2 = antY + Math.max(1, spriteH);
        int vx1 = viewportBounds.x - m;
        int vy1 = viewportBounds.y - m;
        int vx2 = viewportBounds.x + viewportBounds.width + m;
        int vy2 = viewportBounds.y + viewportBounds.height + m;
        return antX < vx2 && ax2 > vx1 && antY < vy2 && ay2 > vy1;
    }

    public static boolean shouldRunOffViewportAi(long physicsStepIndex, Ant ant) {
        int h = System.identityHashCode(ant);
        return ((physicsStepIndex + h) & Integer.MAX_VALUE) % OFF_VIEWPORT_AI_PERIOD == 0;
    }

    public static boolean shouldRunOffViewportPosition(long physicsStepIndex, Ant ant) {
        int h = System.identityHashCode(ant);
        return ((physicsStepIndex + h) & Integer.MAX_VALUE) % OFF_VIEWPORT_MOVE_PERIOD == 0;
    }

    public static float compensatedMoveSpeed(float baseSpeed) {
        return baseSpeed * OFF_VIEWPORT_MOVE_PERIOD;
    }

    public static boolean shouldRunOffViewportBugAi(long physicsStepIndex, int bugIdentityHash) {
        return ((physicsStepIndex + bugIdentityHash) & Integer.MAX_VALUE) % OFF_VIEWPORT_AI_PERIOD == 0;
    }

    public static boolean shouldRunOffViewportBugMove(long physicsStepIndex, int bugIdentityHash) {
        return ((physicsStepIndex + bugIdentityHash) & Integer.MAX_VALUE) % OFF_VIEWPORT_MOVE_PERIOD == 0;
    }
}
