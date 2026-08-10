package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JSlider;

public final class UiSliderStyles {
    private UiSliderStyles() {
    }

    public static void style(JSlider slider) {
        slider.setBackground(AssetStyles.BACKGROUND_COLOR);
        slider.setForeground(AssetStyles.FONT_COLOR_HEADER);
        slider.setCursor(AssetStyles.cursorClickable());
        if (slider.getOrientation() == JSlider.VERTICAL) {
            slider.setOpaque(false);
            slider.setFont(AssetStyles.FONT_SMALL);
            slider.setPaintTicks(false);
            slider.setPaintLabels(false);
        } else {
            slider.setOpaque(true);
            applyPercentTickStyle(slider);
        }
        slider.updateUI();
    }

    private static void applyPercentTickStyle(JSlider slider) {
        slider.setPaintTicks(true);
        if (slider.getMajorTickSpacing() <= 0) {
            slider.setMajorTickSpacing(GameNumbers.VOLUME_STEP_PERCENT);
        }
        slider.setSnapToTicks(true);
        slider.setValue(GameNumbers.snapVolumePercent(slider.getValue()));
    }
}
