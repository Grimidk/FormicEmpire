package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.icons.SquareCheckIcons;

import javax.swing.JCheckBox;

public final class UiCheckBoxStyles {
    public static final int ICON_TEXT_GAP = 8;

    private UiCheckBoxStyles() {
    }

    public static void style(JCheckBox box) {
        box.setFont(AssetStyles.FONT_NORMAL);
        box.setForeground(AssetStyles.FONT_COLOR);
        box.setBackground(AssetStyles.BACKGROUND_COLOR);
        box.setFocusPainted(false);
        box.setCursor(AssetStyles.cursorClickable());
        box.setIcon(SquareCheckIcons.UNCHECKED);
        box.setSelectedIcon(SquareCheckIcons.CHECKED);
        box.setDisabledIcon(SquareCheckIcons.UNCHECKED_DISABLED);
        box.setDisabledSelectedIcon(SquareCheckIcons.CHECKED_DISABLED);
        box.setIconTextGap(ICON_TEXT_GAP);
        box.repaint();
    }
}
