package com.grimidk.formicempire.classes.interfaces.ui.util;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

public final class UiCursors {
    public static final String CLICKABLE_CLIENT_KEY = "formicempire.clickable";

    private static Cursor normal = Cursor.getDefaultCursor();
    private static Cursor click = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static Cursor clickable = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static Cursor writeable = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    private static int pixelSize = 20;
    private static boolean installed;

    private UiCursors() {
    }

    public static void install(Class<?> anchor, String normalPath, String clickPath,
            String clickablePath, String writeablePath) {
        if (installed) {
            return;
        }
        CursorLoaded loadedNormal = load(anchor, normalPath, "AntCursorNormal");
        CursorLoaded loadedClick = load(anchor, clickPath, "AntCursorClick");
        CursorLoaded loadedClickable = load(anchor, clickablePath, "AntCursorClickable");
        CursorLoaded loadedWriteable = load(anchor, writeablePath, "AntCursorWriteable");

        normal = loadedNormal.cursor;
        click = loadedClick.cursor;
        clickable = loadedClickable.cursor;
        writeable = loadedWriteable.cursor;

        pixelSize = Math.max(1, Math.max(
                loadedNormal.pixelSize,
                Math.max(loadedClick.pixelSize,
                        Math.max(loadedClickable.pixelSize, loadedWriteable.pixelSize))));
        installed = true;
    }

    public static boolean isInstalled() {
        return installed;
    }

    public static int pixelSize() {
        return pixelSize;
    }

    public static Cursor normal() {
        return normal;
    }

    public static Cursor click() {
        return click;
    }

    public static Cursor clickable() {
        return clickable;
    }

    public static Cursor writeable() {
        return writeable;
    }

    public static boolean isHoverCursor(Cursor cursor) {
        return cursor != null && (cursor == clickable || cursor == writeable);
    }

    public static void markClickable(JComponent component) {
        if (component == null) {
            return;
        }
        component.putClientProperty(CLICKABLE_CLIENT_KEY, Boolean.TRUE);
        component.setCursor(clickable);
    }

    public static void clearClickable(JComponent component) {
        if (component == null) {
            return;
        }
        component.putClientProperty(CLICKABLE_CLIENT_KEY, null);
        component.setCursor(null);
    }

    public static Cursor createBestCursor(Image img, int width, int height, int hotX, int hotY, String name) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension best = tk.getBestCursorSize(width, height);
        if (best.width <= 0 || best.height <= 0) {
            return Cursor.getDefaultCursor();
        }
        if (best.width == width && best.height == height) {
            return tk.createCustomCursor(img, new Point(hotX, hotY), name);
        }
        BufferedImage canvas = new BufferedImage(best.width, best.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        int targetHotX;
        int targetHotY;
        try {
            if (best.width >= width && best.height >= height) {
                int dx = (best.width - width) / 2;
                int dy = (best.height - height) / 2;
                g2.drawImage(img, dx, dy, null);
                targetHotX = dx + hotX;
                targetHotY = dy + hotY;
            } else {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(img, 0, 0, best.width, best.height, null);
                targetHotX = Math.round((float) hotX * best.width / width);
                targetHotY = Math.round((float) hotY * best.height / height);
            }
        } finally {
            g2.dispose();
        }
        targetHotX = Math.max(0, Math.min(best.width - 1, targetHotX));
        targetHotY = Math.max(0, Math.min(best.height - 1, targetHotY));
        return tk.createCustomCursor(canvas, new Point(targetHotX, targetHotY), name);
    }

    private static CursorLoaded load(Class<?> anchor, String path, String name) {
        try {
            URL url = anchor.getResource(path);
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                int width = Math.max(1, img.getWidth(null));
                int height = Math.max(1, img.getHeight(null));
                int size = Math.max(width, height);
                int hotspot = Math.max(0, Math.min(width, height) / 2);
                Cursor cursor = createBestCursor(img, width, height, hotspot, hotspot, name);
                return new CursorLoaded(cursor, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CursorLoaded(Cursor.getDefaultCursor(), 20);
    }

    private record CursorLoaded(Cursor cursor, int pixelSize) {
    }
}
