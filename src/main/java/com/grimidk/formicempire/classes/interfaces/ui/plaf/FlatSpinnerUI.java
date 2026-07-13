package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiButtonStyles;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

public final class FlatSpinnerUI extends BasicSpinnerUI {
    private static final Dimension STEP_BUTTON_SIZE = new Dimension(26, 22);

    public static ComponentUI createUI(JComponent c) {
        return new FlatSpinnerUI();
    }

    @Override
    protected Component createPreviousButton() {
        Component button = createStepButton("\u25BC");
        installPreviousButtonListeners(button);
        return button;
    }

    @Override
    protected Component createNextButton() {
        Component button = createStepButton("\u25B2");
        installNextButtonListeners(button);
        return button;
    }

    @Override
    protected LayoutManager createLayout() {
        return super.createLayout();
    }

    @Override
    protected JComponent createEditor() {
        JComponent editor = super.createEditor();
        if (editor != null) {
            editor.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            editor.setForeground(AssetStyles.FONT_COLOR);
        }
        return editor;
    }

    private static JButton createStepButton(String label) {
        JButton button = new JButton(label);
        UiButtonStyles.styleCompact(button);
        button.setFont(AssetStyles.FONT_BOLD.deriveFont(11f));
        button.setFocusable(false);
        button.setRequestFocusEnabled(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        button.setPreferredSize(STEP_BUTTON_SIZE);
        button.setMinimumSize(STEP_BUTTON_SIZE);
        return button;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JSpinner spinner) {
            spinner.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            spinner.setForeground(AssetStyles.FONT_COLOR);
            spinner.setBorder(AssetStyles.INTERNAL_BORDER);
            styleStepButtons(spinner);
        }
    }

    public static void styleStepButtons(JSpinner spinner) {
        if (spinner == null) {
            return;
        }
        for (Component child : spinner.getComponents()) {
            if (child instanceof JButton button) {
                UiButtonStyles.styleCompact(button);
                button.setFont(AssetStyles.FONT_BOLD.deriveFont(11f));
                button.setPreferredSize(STEP_BUTTON_SIZE);
                button.setMinimumSize(STEP_BUTTON_SIZE);
                button.setOpaque(true);
                button.setContentAreaFilled(true);
                button.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            }
        }
    }
}
