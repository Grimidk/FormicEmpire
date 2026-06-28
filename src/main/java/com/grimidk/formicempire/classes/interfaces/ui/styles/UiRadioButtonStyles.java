package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.icons.SquareRadioIcons;

import javax.swing.JRadioButton;

public final class UiRadioButtonStyles {
    public static final int ICON_TEXT_GAP = 8;

    private UiRadioButtonStyles() {
    }

    public static void style(JRadioButton button) {
        button.setFont(AssetStyles.FONT_NORMAL);
        button.setForeground(AssetStyles.FONT_COLOR);
        button.setBackground(AssetStyles.BACKGROUND_COLOR);
        button.setFocusPainted(false);
        button.setIcon(SquareRadioIcons.UNSELECTED);
        button.setSelectedIcon(SquareRadioIcons.SELECTED);
        button.setDisabledIcon(SquareRadioIcons.UNSELECTED_DISABLED);
        button.setDisabledSelectedIcon(SquareRadioIcons.SELECTED_DISABLED);
        button.setIconTextGap(ICON_TEXT_GAP);
    }
}
