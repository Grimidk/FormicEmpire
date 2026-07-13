package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JButton;

public final class FlatChevronButton extends JButton {

    public enum ChevronDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private ChevronDirection direction = ChevronDirection.LEFT;

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
        return direction == ChevronDirection.LEFT;
    }

    public ChevronDirection getChevronDirection() {
        return direction;
    }

    public void setChevronDirection(ChevronDirection direction) {
        this.direction = direction != null ? direction : ChevronDirection.LEFT;
        repaint();
    }

    public void setPointsLeft(boolean pointsLeft) {
        setChevronDirection(pointsLeft ? ChevronDirection.LEFT : ChevronDirection.RIGHT);
    }
}
