package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/** Flat combo field with a bordered arrow button. */
public final class FlatComboBoxUI extends BasicComboBoxUI {
    private static final String ARROW_GLYPH = "\u25BC";

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

                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setFont(getFont());
                    g2.setColor(isEnabled() ? AssetStyles.FONT_COLOR : AssetStyles.COLOR_LIGHT_GRAY);
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(ARROW_GLYPH)) / 2;
                    int textY = (getHeight() + fm.getAscent()) / 2 - fm.getDescent();
                    g2.drawString(ARROW_GLYPH, textX, textY);
                } finally {
                    g2.dispose();
                }
            }
        };
        button.setFont(AssetStyles.FONT_BOLD.deriveFont(9f));
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
