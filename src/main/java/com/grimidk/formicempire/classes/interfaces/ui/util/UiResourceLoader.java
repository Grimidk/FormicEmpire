package com.grimidk.formicempire.classes.interfaces.ui.util;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.Icon;

public final class UiResourceLoader {
    private UiResourceLoader() {
    }

    public static Cursor loadCustomCursor(Class<?> anchor, String path, String name) {
        try {
            URL url = anchor.getResource(path);
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                int hotspot = Math.max(0, img.getWidth(null) / 2);
                return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(hotspot, hotspot), name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }

    public static Image loadImage(Class<?> anchor, String path) {
        try {
            URL url = anchor.getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
            System.err.println("Resource not found: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Icon loadDialogIcon(Class<?> anchor, String path, int sizePx) {
        Image image = loadImage(anchor, path);
        if (image == null) {
            return null;
        }
        if (image.getWidth(null) != sizePx || image.getHeight(null) != sizePx) {
            image = image.getScaledInstance(sizePx, sizePx, Image.SCALE_SMOOTH);
        }
        return new ImageIcon(image);
    }
}
