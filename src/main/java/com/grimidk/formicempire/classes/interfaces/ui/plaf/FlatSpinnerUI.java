package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
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
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(AssetStyles.UI_BORDER_COLOR);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g.setColor(isEnabled() ? AssetStyles.FONT_COLOR : AssetStyles.COLOR_LIGHT_GRAY);
                if (previous) {
                    paintDownArrow(g, cx, cy);
                } else {
                    paintUpArrow(g, cx, cy);
                }
            }
        };
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setMargin(new Insets(0, 0, 0, 0));
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

    private static void paintUpArrow(Graphics g, int cx, int cy) {
        g.fillRect(cx - 3, cy + 1, 7, 2);
        g.fillRect(cx - 2, cy, 5, 2);
        g.fillRect(cx - 1, cy - 1, 3, 2);
    }

    private static void paintDownArrow(Graphics g, int cx, int cy) {
        g.fillRect(cx - 3, cy - 1, 7, 2);
        g.fillRect(cx - 2, cy, 5, 2);
        g.fillRect(cx - 1, cy + 1, 3, 2);
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
