package com.grimidk.formicempire.classes.interfaces.ui.icons;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

public final class SquareCheckIcons {
    public static final int ICON_SIZE = AssetStyles.MIN_CONTROL_HIT_SIZE;
    private static final String CHECK_MARK = "\u2716";

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

    private static void paintMark(Graphics2D g2, int x, int y, int size, Color color) {
        g2.setColor(color);
        g2.setFont(AssetStyles.FONT_BOLD.deriveFont(14f));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (size - fm.stringWidth(CHECK_MARK)) / 2;
        int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(CHECK_MARK, textX, textY);
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int size = ICON_SIZE;
                Color border = borderColor(disabled);
                if (selected) {
                    g2.setColor(fillColor(disabled));
                    g2.fillRect(x + 1, y + 1, size - 2, size - 2);
                    paintMark(g2, x, y, size, markColor(disabled));
                }
                g2.setColor(border);
                g2.drawRect(x, y, size - 1, size - 1);
            } finally {
                g2.dispose();
            }
        }
    }
}
