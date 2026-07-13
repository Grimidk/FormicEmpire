package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class FlatTabbedPaneUI extends BasicTabbedPaneUI {
    private static final int SELECTED_LIFT_PX = 2;

    public static ComponentUI createUI(JComponent c) {
        return new FlatTabbedPaneUI();
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        String title = tabPane.getTitleAt(tabIndex);
        if (title == null) {
            title = "";
        }
        Insets insets = tabInsets != null ? tabInsets : new Insets(0, 0, 0, 0);
        int textWidth = metrics.stringWidth(title);
        return Math.max(AssetStyles.TAB_STRIP_WIDTH, insets.left + insets.right + textWidth);
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return AssetStyles.TAB_STRIP_HEIGHT + SELECTED_LIFT_PX;
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = AssetStyles.TAB_MARGIN_INSETS;
        tabAreaInsets = new Insets(0, 0, 0, 0);
        contentBorderInsets = new Insets(0, 0, 0, 0);
        selectedTabPadInsets = new Insets(0, 0, SELECTED_LIFT_PX, 0);
        tabRunOverlay = -1;
    }

    @Override
    protected int getTabRunOverlay(int tabPlacement) {
        return -1;
    }

    @Override
    protected boolean shouldPadTabRun(int tabPlacement, int run) {
        return false;
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        int paintY = isSelected ? y - SELECTED_LIFT_PX : y;
        int paintH = isSelected ? h + SELECTED_LIFT_PX : h;
        g.setColor(isSelected ? AssetStyles.TAB_SELECTED_BG : AssetStyles.TAB_UNSELECTED_BG);
        g.fillRect(x, paintY, w, paintH);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        int paintY = isSelected ? y - SELECTED_LIFT_PX : y;
        int paintH = isSelected ? h + SELECTED_LIFT_PX : h;
        g.setColor(AssetStyles.UI_BORDER_COLOR);
        g.drawLine(x, paintY, x, paintY + paintH - 1);
        g.drawLine(x, paintY, x + w - 1, paintY);
        g.drawLine(x + w - 1, paintY, x + w - 1, paintY + paintH - 1);
        if (!isSelected) {
            g.drawLine(x, paintY + paintH - 1, x + w - 1, paintY + paintH - 1);
        }
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
        g.setColor(isSelected ? AssetStyles.FONT_COLOR_HEADER : AssetStyles.FONT_COLOR);
        super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
    }
}
