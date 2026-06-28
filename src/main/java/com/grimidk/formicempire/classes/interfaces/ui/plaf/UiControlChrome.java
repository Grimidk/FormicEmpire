package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Graphics;
import java.awt.Rectangle;

/** Shared flat track/thumb painting for scrollbars and sliders. */
final class UiControlChrome {
    static final int THUMB_BREADTH = FlatScrollBarUI.VERTICAL_BAR_WIDTH - 2;

    private UiControlChrome() {
    }

    static void paintScrollbarTrack(Graphics g, Rectangle bounds) {
        g.setColor(AssetStyles.BACKGROUND_DARK);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    static void paintSliderTrack(Graphics g, Rectangle bounds) {
        g.setColor(AssetStyles.BACKGROUND_DARK);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(AssetStyles.UI_BORDER_COLOR);
        g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }

    static void paintScrollbarThumb(Graphics g, Rectangle bounds) {
        if (bounds.isEmpty()) {
            return;
        }
        g.setColor(AssetStyles.BACKGROUND_LIGHT);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    static void paintThumb(Graphics g, Rectangle bounds) {
        if (bounds.isEmpty()) {
            return;
        }
        g.setColor(AssetStyles.BACKGROUND_LIGHT);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(AssetStyles.UI_BORDER_COLOR);
        g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }
}
