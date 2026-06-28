package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/** Flat combo field with a bordered arrow button. */
public final class FlatComboBoxUI extends BasicComboBoxUI {
    public static ComponentUI createUI(JComponent c) {
        return new FlatComboBoxUI();
    }

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                if (isEnabled()) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                g.setColor(AssetStyles.UI_BORDER_COLOR);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g.setColor(AssetStyles.FONT_COLOR);
                g.fillRect(cx - 3, cy - 1, 7, 2);
                g.fillRect(cx - 2, cy, 5, 2);
                g.fillRect(cx - 1, cy + 1, 3, 2);
            }
        };
        button.setFocusable(false);
        button.setRequestFocusEnabled(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        button.setForeground(AssetStyles.FONT_COLOR);
        return button;
    }

    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        g.setColor(comboBox.getBackground());
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(AssetStyles.UI_BORDER_COLOR);
        g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }

    @Override
    protected ComboPopup createPopup() {
        return new BasicComboPopup(comboBox) {
            @Override
            protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                Rectangle bounds = super.computePopupBounds(px, py, pw, ph);
                return bounds;
            }
        };
    }

    @Override
    protected Insets getInsets() {
        return new Insets(2, 4, 2, 2);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        if (comboBox != null) {
            comboBox.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            comboBox.setForeground(AssetStyles.FONT_COLOR);
        }
    }
}
