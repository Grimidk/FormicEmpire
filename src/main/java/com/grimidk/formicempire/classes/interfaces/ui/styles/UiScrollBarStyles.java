package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatScrollBarUI;

import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public final class UiScrollBarStyles {
    public static final String HIDDEN_SCROLLBAR_KEY = "formicempire.hiddenScrollbar";
    public static final int DEFAULT_UNIT_INCREMENT = 32;
    public static final int DEFAULT_BLOCK_INCREMENT = 128;

    private UiScrollBarStyles() {
    }

    public static boolean isHidden(JScrollPane scrollPane) {
        return Boolean.TRUE.equals(scrollPane.getClientProperty(HIDDEN_SCROLLBAR_KEY));
    }

    public static boolean isHidden(JScrollBar scrollBar) {
        Container parent = scrollBar.getParent();
        if (parent instanceof JScrollPane scrollPane) {
            return isHidden(scrollPane);
        }
        return false;
    }

    public static void hide(JScrollPane scrollPane) {
        scrollPane.putClientProperty(HIDDEN_SCROLLBAR_KEY, Boolean.TRUE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public static void style(JScrollPane scrollPane) {
        if (scrollPane == null) {
            return;
        }
        style(scrollPane.getVerticalScrollBar());
        style(scrollPane.getHorizontalScrollBar());
    }

    public static void style(JScrollBar scrollBar) {
        if (scrollBar == null) {
            return;
        }
        scrollBar.setBackground(AssetStyles.BACKGROUND_DARK);
        scrollBar.setForeground(AssetStyles.BACKGROUND_LIGHT);
        scrollBar.setBorder(null);
        scrollBar.setUnitIncrement(DEFAULT_UNIT_INCREMENT);
        scrollBar.setBlockIncrement(DEFAULT_BLOCK_INCREMENT);
        if (scrollBar.getOrientation() == JScrollBar.VERTICAL) {
            Dimension size = new Dimension(FlatScrollBarUI.VERTICAL_BAR_WIDTH, 48);
            scrollBar.setPreferredSize(size);
            scrollBar.setMinimumSize(new Dimension(FlatScrollBarUI.VERTICAL_BAR_WIDTH, FlatScrollBarUI.MIN_THUMB_LENGTH));
        } else {
            Dimension zero = new Dimension(0, 0);
            scrollBar.setPreferredSize(zero);
            scrollBar.setMinimumSize(zero);
            scrollBar.setMaximumSize(zero);
        }
        scrollBar.updateUI();
    }
}
