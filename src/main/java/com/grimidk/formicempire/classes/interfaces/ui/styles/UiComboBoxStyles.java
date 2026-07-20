package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JComboBox;

public final class UiComboBoxStyles {
    private UiComboBoxStyles() {
    }

    public static void style(JComboBox<?> box) {
        box.setFont(AssetStyles.FONT_NORMAL);
        box.setOpaque(true);
        applyComboColors(box);
        box.setBorder(AssetStyles.INTERNAL_BORDER);
        box.setCursor(AssetStyles.cursorClickable());
    }

    public static void applyComboColors(JComboBox<?> box) {
        if (box.isEnabled()) {
            box.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            box.setForeground(AssetStyles.FONT_COLOR);
        } else {
            box.setBackground(AssetStyles.BACKGROUND_DARK);
            box.setForeground(AssetStyles.COLOR_LIGHT_GRAY);
        }
    }
}
