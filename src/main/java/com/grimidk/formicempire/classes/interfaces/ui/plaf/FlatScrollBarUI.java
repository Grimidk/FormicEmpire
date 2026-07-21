package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiScrollBarStyles;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class FlatScrollBarUI extends BasicScrollBarUI {
    public static final int VERTICAL_BAR_WIDTH = 8;
    public static final int MIN_THUMB_LENGTH = 24;

    public static ComponentUI createUI(JComponent c) {
        return new FlatScrollBarUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        if (scrollbar.getUnitIncrement() <= 1) {
            scrollbar.setUnitIncrement(UiScrollBarStyles.DEFAULT_UNIT_INCREMENT);
        }
        if (scrollbar.getBlockIncrement() <= 10) {
            scrollbar.setBlockIncrement(UiScrollBarStyles.DEFAULT_BLOCK_INCREMENT);
        }
    }

    @Override
    protected void configureScrollBarColors() {
        trackColor = AssetStyles.BACKGROUND_DARK;
        trackHighlightColor = AssetStyles.BACKGROUND_DARK;
        thumbColor = AssetStyles.BACKGROUND_LIGHT;
        thumbHighlightColor = AssetStyles.BACKGROUND_LIGHT;
        thumbLightShadowColor = AssetStyles.BACKGROUND_LIGHT;
        thumbDarkShadowColor = AssetStyles.BACKGROUND_LIGHT;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroSizeButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroSizeButton();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            return new Dimension(VERTICAL_BAR_WIDTH, 48);
        }
        return new Dimension(0, 0);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            return new Dimension(VERTICAL_BAR_WIDTH, MIN_THUMB_LENGTH);
        }
        return new Dimension(0, 0);
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            return new Dimension(UiControlChrome.THUMB_BREADTH, MIN_THUMB_LENGTH);
        }
        return new Dimension(MIN_THUMB_LENGTH, UiControlChrome.THUMB_BREADTH);
    }

    private static JButton createZeroSizeButton() {
        JButton button = new JButton();
        Dimension zero = new Dimension(0, 0);
        button.setPreferredSize(zero);
        button.setMinimumSize(zero);
        button.setMaximumSize(zero);
        return button;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (trackRect.isEmpty()) {
            return;
        }
        UiControlChrome.paintScrollbarTrack(g, trackRect);
        if (!thumbRect.isEmpty() && scrollbar.isEnabled()) {
            UiControlChrome.paintScrollbarThumb(g, thumbRect);
        }
    }

    @Override
    protected void paintDecreaseHighlight(Graphics g) {
    }

    @Override
    protected void paintIncreaseHighlight(Graphics g) {
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        UiControlChrome.paintScrollbarTrack(g, trackBounds);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        UiControlChrome.paintScrollbarThumb(g, thumbBounds);
    }
}
