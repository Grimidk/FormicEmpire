package com.grimidk.formicempire.classes.interfaces.ui.icons;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/** Flat checkbox glyphs: hollow square (off) or filled square (on). Colors read from {@link AssetStyles} at paint time. */
public final class SquareCheckIcons {
    public static final int ICON_SIZE = 12;

    public static final Icon UNCHECKED = new SquareIcon(false);
    public static final Icon CHECKED = new SquareIcon(true);
    public static final Icon UNCHECKED_DISABLED = new SquareIcon(false, true);
    public static final Icon CHECKED_DISABLED = new SquareIcon(true, true);

    private SquareCheckIcons() {
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
                if (selected) {
                    g2.setColor(disabled ? AssetStyles.COLOR_LIGHT_GRAY : AssetStyles.FONT_COLOR);
                    g2.fillRect(x, y, size, size);
                } else {
                    g2.setColor(disabled ? AssetStyles.COLOR_LIGHT_GRAY : AssetStyles.UI_BORDER_COLOR);
                    g2.drawRect(x, y, size - 1, size - 1);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
