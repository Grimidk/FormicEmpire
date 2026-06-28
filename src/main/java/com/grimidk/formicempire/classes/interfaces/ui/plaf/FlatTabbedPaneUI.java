package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/** Flat tab chrome only — uses default Swing tab layout and hit-testing. */
public final class FlatTabbedPaneUI extends BasicTabbedPaneUI {
    public static ComponentUI createUI(JComponent c) {
        return new FlatTabbedPaneUI();
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return AssetStyles.TAB_STRIP_WIDTH;
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return AssetStyles.TAB_STRIP_HEIGHT;
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = AssetStyles.TAB_MARGIN_INSETS;
        tabAreaInsets = new Insets(0, 0, 0, 0);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        tabRunOverlay = 0;
    }

    @Override
    protected int getTabRunOverlay(int tabPlacement) {
        return 0;
    }

    @Override
    protected boolean shouldPadTabRun(int tabPlacement, int run) {
        return false;
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        g.setColor(isSelected ? AssetStyles.BACKGROUND_COLOR : AssetStyles.BACKGROUND_LIGHT);
        g.fillRect(x, y, w, h);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        g.setColor(AssetStyles.UI_BORDER_COLOR);
        g.drawLine(x, y, x, y + h - 1);
        g.drawLine(x, y, x + w - 1, y);
        g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        int width = tabPane.getWidth();
        int height = tabPane.getHeight();
        Insets insets = tabPane.getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = width - insets.left - insets.right;
        int h = height - insets.top - insets.bottom;
        g.setColor(AssetStyles.UI_BORDER_COLOR);
        g.drawRect(x, y, w - 1, h - 1);
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
            int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
            int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        g.setFont(font);
        g.setColor(AssetStyles.FONT_COLOR);
        super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
    }
}
