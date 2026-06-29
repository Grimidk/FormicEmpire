package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatSpinnerUI;

import javax.swing.JSpinner;
import javax.swing.text.JTextComponent;

public final class UiSpinnerStyles {
    private UiSpinnerStyles() {
    }

    public static void style(JSpinner spinner) {
        if (spinner == null) {
            return;
        }
        spinner.setFont(AssetStyles.FONT_NORMAL);
        spinner.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        spinner.setForeground(AssetStyles.FONT_COLOR);
        spinner.setBorder(AssetStyles.INTERNAL_BORDER);
        if (spinner.getEditor() instanceof JTextComponent editor) {
            UiTextFieldStyles.style(editor);
        }
        spinner.updateUI();
        FlatSpinnerUI.styleStepButtons(spinner);
    }
}
