package com.grimidk.formicempire.classes.interfaces.ui.icons;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

public final class SquareRadioIcons {
    public static final int ICON_SIZE = 12;

    public static final Icon UNSELECTED = new RadioIcon(false);
    public static final Icon SELECTED = new RadioIcon(true);
    public static final Icon UNSELECTED_DISABLED = new RadioIcon(false, true);
    public static final Icon SELECTED_DISABLED = new RadioIcon(true, true);

    private SquareRadioIcons() {
    }

    private static final class RadioIcon implements Icon {
        private final boolean selected;
        private final boolean disabled;

        private RadioIcon(boolean selected) {
            this(selected, false);
        }

        private RadioIcon(boolean selected, boolean disabled) {
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
                int size = ICON_SIZE;
                if (selected) {
                    g2.setColor(disabled ? AssetStyles.COLOR_LIGHT_GRAY : AssetStyles.FONT_COLOR);
                    g2.fillOval(x, y, size - 1, size - 1);
                } else {
                    g2.setColor(disabled ? AssetStyles.COLOR_LIGHT_GRAY : AssetStyles.UI_BORDER_COLOR);
                    g2.drawOval(x, y, size - 1, size - 1);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
