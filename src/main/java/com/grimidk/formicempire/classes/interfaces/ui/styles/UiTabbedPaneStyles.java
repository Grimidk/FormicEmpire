package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JTabbedPane;

public final class UiTabbedPaneStyles {
    private UiTabbedPaneStyles() {
    }

    public static void style(JTabbedPane tabbedPane) {
        tabbedPane.setFont(AssetStyles.FONT_BOLD);
        tabbedPane.setBackground(AssetStyles.BACKGROUND_DARK);
        tabbedPane.setForeground(AssetStyles.FONT_COLOR);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.updateUI();
    }
}
