package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatSpinnerUI;

import javax.swing.JComponent;
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
        spinner.updateUI();
        styleEditor(spinner);
        FlatSpinnerUI.styleStepButtons(spinner);
        FlatSpinnerUI.applySpinnerMinimumSize(spinner);
    }

    private static void styleEditor(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            UiTextFieldStyles.style(defaultEditor.getTextField());
            return;
        }
        if (editor instanceof JTextComponent text) {
            UiTextFieldStyles.style(text);
        }
    }
}
