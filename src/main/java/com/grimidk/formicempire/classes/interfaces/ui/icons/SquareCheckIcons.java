package com.grimidk.formicempire.classes.interfaces.ui.icons;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/** Flat checkbox glyphs: hollow square (off) or filled square with X (on). */
public final class SquareCheckIcons {
    public static final int ICON_SIZE = 12;

    public static final Icon UNCHECKED = new SquareIcon(false);
    public static final Icon CHECKED = new SquareIcon(true);
    public static final Icon UNCHECKED_DISABLED = new SquareIcon(false, true);
    public static final Icon CHECKED_DISABLED = new SquareIcon(true, true);

    private SquareCheckIcons() {
    }

    private static Color borderColor(boolean disabled) {
        if (disabled) {
            return AssetStyles.COLOR_LIGHT_GRAY;
        }
        return AssetStyles.isDarkMode() ? AssetStyles.COLOR_ABSOLUTE_WHITE : AssetStyles.COLOR_ABSOLUTE_BLACK;
    }

    private static Color fillColor(boolean disabled) {
        if (disabled) {
            return AssetStyles.COLOR_LIGHT_GRAY;
        }
        return AssetStyles.isDarkMode() ? AssetStyles.COLOR_ABSOLUTE_BLACK : AssetStyles.COLOR_ABSOLUTE_WHITE;
    }

    private static Color markColor(boolean disabled) {
        if (disabled) {
            return AssetStyles.COLOR_MEDIUM_GRAY;
        }
        return AssetStyles.isDarkMode() ? AssetStyles.COLOR_ABSOLUTE_WHITE : AssetStyles.COLOR_ABSOLUTE_BLACK;
    }

    private static void paintX(Graphics2D g2, int x, int y, int size, Color color) {
        g2.setColor(color);
        int pad = 2;
        int span = size - pad * 2;
        for (int i = 0; i < span; i++) {
            int px = x + pad + i;
            g2.fillRect(px, y + pad + i, 2, 2);
            g2.fillRect(px, y + size - pad - i - 2, 2, 2);
        }
    }

    private static final class SquareIcon implements Icon {
        private final boolean selected;
        private final boolean disabled;

        private SquareIcon(boolean selected) {
            this(selected, false);
        }

        private SquareIcon(boolean selected, boolean disabled) {
            this.selected = selected;
            this.disabled = disabled;
        }

        @Override
        public int getIconWidth() {
            return ICON_SIZE;
        }

        @Override
        public int getIconHeight() {
            return ICON_SIZE;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                int size = ICON_SIZE;
                Color border = borderColor(disabled);
                if (selected) {
                    g2.setColor(fillColor(disabled));
                    g2.fillRect(x + 1, y + 1, size - 2, size - 2);
                    paintX(g2, x, y, size, markColor(disabled));
                }
                g2.setColor(border);
                g2.drawRect(x, y, size - 1, size - 1);
            } finally {
                g2.dispose();
            }
        }
    }
}
