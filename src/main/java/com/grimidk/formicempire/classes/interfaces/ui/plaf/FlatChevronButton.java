package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JButton;

/** Small flat button that paints a left or right chevron (no text label). */
public final class FlatChevronButton extends JButton {
    private boolean pointsLeft = true;

    public FlatChevronButton() {
        setUI(FlatChevronButtonUI.createUI(this));
        setFocusable(false);
        setRequestFocusEnabled(false);
        setBorderPainted(false);
        setContentAreaFilled(true);
        setMargin(new Insets(0, 0, 0, 0));
        setBackground(AssetStyles.BACKGROUND_COLOR);
        setForeground(AssetStyles.FONT_COLOR);
        setFont(AssetStyles.FONT_BOLD.deriveFont(11f));
        Dimension size = new Dimension(24, 24);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    public boolean isPointsLeft() {
        return pointsLeft;
    }

    public void setPointsLeft(boolean pointsLeft) {
        this.pointsLeft = pointsLeft;
        repaint();
    }
}
