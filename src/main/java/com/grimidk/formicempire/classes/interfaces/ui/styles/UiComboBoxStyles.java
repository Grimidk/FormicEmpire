package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JComboBox;

public final class UiComboBoxStyles {
    private UiComboBoxStyles() {
    }

    public static void style(JComboBox<?> box) {
        box.setFont(AssetStyles.FONT_NORMAL);
        box.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        box.setForeground(AssetStyles.FONT_COLOR);
        box.setBorder(AssetStyles.INTERNAL_BORDER);
    }
}
