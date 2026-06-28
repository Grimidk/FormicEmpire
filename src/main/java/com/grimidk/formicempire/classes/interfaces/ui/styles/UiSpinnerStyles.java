package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JSpinner;

public final class UiSpinnerStyles {
    private UiSpinnerStyles() {
    }

    public static void style(JSpinner spinner) {
        spinner.setFont(AssetStyles.FONT_NORMAL);
        spinner.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        spinner.setForeground(AssetStyles.FONT_COLOR);
        spinner.setBorder(AssetStyles.INTERNAL_BORDER);
    }
}
