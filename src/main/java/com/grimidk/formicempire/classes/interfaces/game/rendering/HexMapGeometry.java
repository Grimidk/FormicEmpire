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
}
