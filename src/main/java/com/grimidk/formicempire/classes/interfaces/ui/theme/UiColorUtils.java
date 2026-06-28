package com.grimidk.formicempire.classes.interfaces.ui.theme;

import java.awt.Color;

public final class UiColorUtils {
    private UiColorUtils() {
    }

    public static Color colorFromRgb(int r, int g, int b) {
        return new Color(clampByte(r), clampByte(g), clampByte(b));
    }

    public static Color colorFromRgba(int r, int g, int b, int a) {
        return new Color(clampByte(r), clampByte(g), clampByte(b), clampByte(a));
    }

    public static Color colorFromAveragedRgb(long sumR, long sumG, long sumB, long count, Color fallback) {
        if (count <= 0) {
            return fallback;
        }
        return colorFromRgb((int) (sumR / count), (int) (sumG / count), (int) (sumB / count));
    }

    public static Color lightenTowardBackground(Color c, float amount, Color background) {
        int r = Math.min(255, (int) (c.getRed() + (background.getRed() - c.getRed()) * amount));
        int g = Math.min(255, (int) (c.getGreen() + (background.getGreen() - c.getGreen()) * amount));
        int b = Math.min(255, (int) (c.getBlue() + (background.getBlue() - c.getBlue()) * amount));
        return colorFromRgba(r, g, b, c.getAlpha());
    }

    public static Color fadeTowardBackground(Color c, float factor, Color background) {
        int r = (int) (c.getRed() * (1 - factor) + background.getRed() * factor);
        int g = (int) (c.getGreen() * (1 - factor) + background.getGreen() * factor);
        int b = (int) (c.getBlue() * (1 - factor) + background.getBlue() * factor);
        return colorFromRgb(r, g, b);
    }

    private static int clampByte(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
