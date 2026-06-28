package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JSlider;

public final class UiSliderStyles {
    private UiSliderStyles() {
    }

    public static void style(JSlider slider) {
        slider.setBackground(AssetStyles.BACKGROUND_COLOR);
        slider.setForeground(AssetStyles.FONT_COLOR_HEADER);
        if (slider.getOrientation() == JSlider.VERTICAL) {
            slider.setOpaque(false);
            slider.setFont(AssetStyles.FONT_SMALL);
            slider.setPaintTicks(false);
            slider.setPaintLabels(false);
        }
        slider.updateUI();
    }
}
