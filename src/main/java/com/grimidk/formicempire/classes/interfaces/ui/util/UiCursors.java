package com.grimidk.formicempire.classes.interfaces.ui.util;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
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

    private static CursorLoaded load(Class<?> anchor, String path, String name) {
        try {
            URL url = anchor.getResource(path);
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                int width = Math.max(1, img.getWidth(null));
                int height = Math.max(1, img.getHeight(null));
                int size = Math.max(width, height);
                int hotspot = Math.max(0, Math.min(width, height) / 2);
                Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                        img, new Point(hotspot, hotspot), name);
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
