package com.grimidk.formicempire.classes.interfaces.game.rendering;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;

import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;

public final class HexMapGeometry {
    public static final int[][] FLAT_TOP_NEIGHBOR_OFFSETS = {
            {1, 0}, {0, 1}, {-1, 1}, {-1, 0}, {0, -1}, {1, -1}
    };

    private HexMapGeometry() {
    }

    public static double flatTopCenterX(int q, int r, double layoutSize) {
        return layoutSize * (Math.sqrt(3.0) * q + Math.sqrt(3.0) / 2.0 * r);
    }

    public static double flatTopCenterY(int q, int r, double layoutSize) {
        return layoutSize * (1.5 * r);
    }

    public static Point flatTopCenterPixel(int q, int r, double originX, double originY, double layoutSize) {
        double cx = originX + flatTopCenterX(q, r, layoutSize);
        double cy = originY + flatTopCenterY(q, r, layoutSize);
        return new Point((int) Math.round(cx), (int) Math.round(cy));
    }

    public static int centeredIconOrigin(double center, int iconSize) {
        return (int) Math.round(center - iconSize / 2.0);
    }

    public static double flatTopCenterXAbs(int q, int r, double originX, double layoutSize) {
        return originX + flatTopCenterX(q, r, layoutSize);
    }

    public static double flatTopCenterYAbs(int q, int r, double originY, double layoutSize) {
        return originY + flatTopCenterY(q, r, layoutSize);
    }

    public static Polygon flatTopPolygon(int q, int r, double originX, double originY, double layoutSize) {
        double cx = originX + flatTopCenterX(q, r, layoutSize);
        double cy = originY + flatTopCenterY(q, r, layoutSize);
        Polygon poly = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angleRad = Math.PI / 180.0 * (60 * i - 30);
            int px = (int) Math.round(cx + layoutSize * Math.cos(angleRad));
            int py = (int) Math.round(cy + layoutSize * Math.sin(angleRad));
            poly.addPoint(px, py);
        }
        return poly;
    }

    public static boolean ownsSharedEdge(int q, int r, Integer neighborQ, Integer neighborR) {
        if (neighborQ == null || neighborR == null) {
            return true;
        }
        if (q != neighborQ) {
            return q < neighborQ;
        }
        return r < neighborR;
    }

    public static boolean ownsSharedEdge(Hex current, Hex neighbor) {
        if (current == null) {
            return false;
        }
        if (neighbor == null) {
            return ownsSharedEdge(current.getQ(), current.getR(), null, null);
        }
        return ownsSharedEdge(current.getQ(), current.getR(), neighbor.getQ(), neighbor.getR());
    }

    public static Dynasty dynastyOf(Hex hex) {
        if (hex == null || hex.getColony() == null) {
            return null;
        }
        return hex.getColony().getDynasty();
    }

    public static Color dynastyPaintColor(Dynasty dynasty, Color fallback) {
        if (dynasty != null && dynasty.getColor() != null) {
            return dynasty.getColor();
        }
        return fallback;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double hexesAcross(int worldRadius) {
        return (Math.max(0, worldRadius) * 2 + 1) + 1.5;
    }

    public static double fittedHexSize(
            int worldRadius,
            int panelW,
            int panelH,
            double minSize,
            double maxSize) {
        if (panelW <= 0 || panelH <= 0) {
            return minSize;
        }
        double hexes = hexesAcross(worldRadius);
        double maxRadiusW = panelW / (hexes * Math.sqrt(3.0));
        double maxRadiusH = panelH / (hexes * 1.5);
        double size = Math.min(maxRadiusW, maxRadiusH);
        size = Math.min(size, maxSize);
        if (size < minSize) {
            size = minSize;
        }
        return size;
    }

    public static double worldWidth(int worldRadius, double hexSize) {
        return hexesAcross(worldRadius) * Math.sqrt(3.0) * hexSize;
    }

    public static double worldHeight(int worldRadius, double hexSize) {
        return hexesAcross(worldRadius) * 1.5 * hexSize;
    }

    public static double nextZoom(double zoom, double factor, double min, double max) {
        if (!(factor > 0) || Double.isNaN(factor) || Double.isInfinite(factor)) {
            return clamp(zoom, min, max);
        }
        return clamp(zoom * factor, min, max);
    }

    public static double panAfterZoom(double pan, double mouseFromCenter, double oldSize, double newSize) {
        if (!(oldSize > 0) || !(newSize > 0)) {
            return pan;
        }
        double scale = newSize / oldSize;
        return mouseFromCenter * (1.0 - scale) + pan * scale;
    }

    public static double clampPan(double pan, double worldSpan, double panelSpan) {
        double extra = Math.max(0.0, worldSpan - panelSpan);
        double max = extra / 2.0;
        return clamp(pan, -max, max);
    }
}
