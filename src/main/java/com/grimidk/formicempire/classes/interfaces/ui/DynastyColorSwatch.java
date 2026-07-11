package com.grimidk.formicempire.classes.interfaces.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Dynasty color indicator with a fixed outer hitbox and a smaller centered swatch.
 */
public class DynastyColorSwatch extends JPanel {

    public static final int HITBOX_SIZE = 18;
    public static final int SWATCH_SIZE = 12;

    private Color dynastyColor = Color.GRAY;
    private boolean hovered;
    private boolean clickable;
    private Runnable onClick;

    public DynastyColorSwatch(Color dynastyColor) {
        setDynastyColor(dynastyColor);
        setOpaque(false);
        applyFixedSize();
        installMouseHandlers();
    }

    public DynastyColorSwatch(Color dynastyColor, Runnable onClick, String tooltip) {
        this(dynastyColor);
        setClickAction(onClick, tooltip);
    }

    public static int hitboxWidth() {
        return HITBOX_SIZE;
    }

    public void setDynastyColor(Color dynastyColor) {
        this.dynastyColor = dynastyColor != null ? dynastyColor : Color.GRAY;
        repaint();
    }

    public void setClickAction(Runnable onClick, String tooltip) {
        this.onClick = onClick;
        this.clickable = onClick != null;
        setToolTipText(tooltip);
        setCursor(clickable
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getDefaultCursor());
    }

    public void clearClickAction() {
        onClick = null;
        clickable = false;
        setToolTipText(null);
        setCursor(Cursor.getDefaultCursor());
        hovered = false;
        repaint();
    }

    private void applyFixedSize() {
        Dimension size = new Dimension(HITBOX_SIZE, HITBOX_SIZE);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    private void installMouseHandlers() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickable && onClick != null && isEnabled()) {
                    onClick.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (clickable) {
                    hovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hovered) {
                    hovered = false;
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        int x = (getWidth() - SWATCH_SIZE) / 2;
        int y = (getHeight() - SWATCH_SIZE) / 2;
        g.setColor(dynastyColor);
        g.fillRect(x, y, SWATCH_SIZE, SWATCH_SIZE);
        g.setColor(hovered && clickable ? AssetStyles.SELECTION_BACKGROUND : AssetStyles.COLOR_ABSOLUTE_BLACK);
        g.drawRect(x, y, SWATCH_SIZE - 1, SWATCH_SIZE - 1);
    }
}
