package com.grimidk.formicempire.classes.entities.services;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

import com.grimidk.formicempire.classes.infrasctructure.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

/**
 * Overworld resource-node placement: keeps nodes outside the default viewport and away from the nest entrance.
 */
public final class ResourceSourcePlacement {

    private ResourceSourcePlacement() {
    }

    /**
     * Minimum center-to-entrance distance so a square sprite of {@code displaySizePx} does not intersect the
     * viewport rectangle {@code [0, viewportW) x [0, viewportH)} when the scroll view is centered on the nest.
     */
    public static int minSpawnDistanceFromEntrance(
            int entranceX, int entranceY, int viewportW, int viewportH, int displaySizePx) {
        int half = Math.max(1, displaySizePx / 2);
        int margin = GameConstants.RESOURCE_SPAWN_VIEWPORT_MARGIN;
        int farthestCorner = 0;
        int[][] corners = {{0, 0}, {viewportW, 0}, {0, viewportH}, {viewportW, viewportH}};
        for (int[] corner : corners) {
            double dx = corner[0] - entranceX;
            double dy = corner[1] - entranceY;
            farthestCorner = Math.max(farthestCorner, (int) Math.ceil(Math.hypot(dx, dy)));
        }
        return farthestCorner + half + margin;
    }

    /**
     * Picks a spawn center at least {@link #minSpawnDistanceFromEntrance} plus {@code extraDistance} from the entrance.
     */
    public static Point pickSpawnCenter(
            NeoPoint entrance, int viewportW, int viewportH, int displaySizePx, int extraDistance, Random random) {
        int minDist = minSpawnDistanceFromEntrance(
                entrance.x, entrance.y, viewportW, viewportH, displaySizePx);
        int dist = minDist + Math.max(0, extraDistance);
        double angle = random.nextDouble() * 2.0 * Math.PI;
        int cx = (int) Math.round(entrance.x + Math.cos(angle) * dist);
        int cy = (int) Math.round(entrance.y + Math.sin(angle) * dist);
        return new Point(cx, cy);
    }

    /** Top-left of the drawn sprite given center and square display size. */
    public static Point topLeftFromCenter(int centerX, int centerY, int displaySizePx) {
        int half = displaySizePx / 2;
        return new Point(centerX - half, centerY - half);
    }

    /**
     * Returns true when the axis-aligned square of {@code displaySizePx} at {@code topLeft} is fully outside
     * the viewport content rect.
     */
    public static boolean isFullyOutsideViewport(Point topLeft, int displaySizePx, int viewportW, int viewportH) {
        Rectangle viewport = new Rectangle(0, 0, Math.max(1, viewportW), Math.max(1, viewportH));
        Rectangle sprite = new Rectangle(topLeft.x, topLeft.y, displaySizePx, displaySizePx);
        return !viewport.intersects(sprite);
    }
}
