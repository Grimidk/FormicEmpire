package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JMenuItem;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

public final class UiMenuStyles {
    private UiMenuStyles() {
    }

    public static void style(JMenuItem menuItem) {
        menuItem.setForeground(AssetStyles.FONT_COLOR);
        menuItem.setBackground(AssetStyles.BACKGROUND_COLOR);
        menuItem.setOpaque(true);
        menuItem.setCursor(AssetStyles.cursorClickable());
        if (!(menuItem.getUI() instanceof BasicMenuItemUI)) {
            menuItem.setUI((MenuItemUI) BasicMenuItemUI.createUI(menuItem));
        }
    }
}
