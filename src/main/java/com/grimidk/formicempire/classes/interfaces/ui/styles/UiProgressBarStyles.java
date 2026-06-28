package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JProgressBar;

public final class UiProgressBarStyles {
    private UiProgressBarStyles() {
    }

    public static void style(JProgressBar bar) {
        bar.setBackground(AssetStyles.BACKGROUND_DARK);
        bar.setForeground(AssetStyles.FONT_COLOR_SUCCESS);
        bar.setFont(AssetStyles.FONT_SMALL);
        bar.setBorderPainted(false);
        bar.setOpaque(true);
    }
}
