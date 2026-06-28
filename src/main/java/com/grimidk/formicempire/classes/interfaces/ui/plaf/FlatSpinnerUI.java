package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

/** Flat spinner with bordered step buttons. */
public final class FlatSpinnerUI extends BasicSpinnerUI {
    public static ComponentUI createUI(JComponent c) {
        return new FlatSpinnerUI();
    }

    @Override
    protected Component createPreviousButton() {
        return createStepButton(true);
    }

    @Override
    protected Component createNextButton() {
        return createStepButton(false);
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

    private static JButton createStepButton(boolean previous) {
        JButton button = new JButton(previous ? "\u25B2" : "\u25BC") {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(AssetStyles.UI_BORDER_COLOR);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                super.paintComponent(g);
            }
        };
        button.setFont(AssetStyles.FONT_SMALL);
        button.setFocusable(false);
        button.setRequestFocusEnabled(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        button.setForeground(AssetStyles.FONT_COLOR);
        Dimension size = new Dimension(18, 14);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        return button;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JSpinner spinner) {
            spinner.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            spinner.setForeground(AssetStyles.FONT_COLOR);
            spinner.setBorder(AssetStyles.INTERNAL_BORDER);
        }
    }
}
