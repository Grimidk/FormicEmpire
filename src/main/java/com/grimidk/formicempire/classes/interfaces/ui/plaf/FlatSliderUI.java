package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;

public final class FlatSliderUI extends BasicSliderUI {
    public static final Dimension VERTICAL_SIZE = new Dimension(AssetStyles.MIN_CONTROL_HIT_SIZE, 96);
    private static final int TICK_LENGTH = 5;

    public FlatSliderUI(JSlider slider) {
        super(slider);
    }

    public static ComponentUI createUI(JComponent c) {
        return new FlatSliderUI((JSlider) c);
    }

    @Override
    protected void installDefaults(JSlider slider) {
        super.installDefaults(slider);
        focusInsets = new Insets(0, 0, 0, 0);
        slider.setOpaque(true);
    }

    @Override
    protected int getTickLength() {
        return slider != null && slider.getPaintTicks() ? TICK_LENGTH : 0;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        g.setColor(c.getBackground());
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        super.paint(g, c);
    }

    @Override
    public Dimension getPreferredVerticalSize() {
        return new Dimension(VERTICAL_SIZE);
    }

    @Override
    public Dimension getPreferredHorizontalSize() {
        Dimension size = super.getPreferredHorizontalSize();
        int min = AssetStyles.MIN_CONTROL_HIT_SIZE;
        int ticks = getTickLength();
        size.height = Math.max(size.height, min + ticks);
        return size;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        recalculateIfInsetsChanged();
        recalculateIfOrientationChanged();
        if (slider.getOrientation() == JSlider.VERTICAL) {
            return getPreferredVerticalSize();
        }
        return getPreferredHorizontalSize();
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        if (slider.getOrientation() == JSlider.VERTICAL) {
            return getMinimumVerticalSize();
        }
        return getPreferredHorizontalSize();
    }

    @Override
    public Dimension getMinimumVerticalSize() {
        return new Dimension(VERTICAL_SIZE);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        if (slider.getOrientation() == JSlider.VERTICAL) {
            return new Dimension(VERTICAL_SIZE.width, Integer.MAX_VALUE);
        }
        return new Dimension(Integer.MAX_VALUE, getPreferredHorizontalSize().height);
    }

    @Override
    public void paintTrack(Graphics g) {
        UiControlChrome.paintSliderTrack(g, trackRect);
    }

    @Override
    public void paintThumb(Graphics g) {
        UiControlChrome.paintThumb(g, thumbRect);
    }

    @Override
    public void paintTicks(Graphics g) {
        if (!slider.getPaintTicks()) {
            return;
        }
        int spacing = slider.getMajorTickSpacing();
        if (spacing <= 0 || tickRect == null || tickRect.isEmpty()) {
            return;
        }
        g.setColor(AssetStyles.UI_BORDER_COLOR);
        int min = slider.getMinimum();
        int max = slider.getMaximum();
        for (int value = min; value <= max; value += spacing) {
            if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
                int x = xPositionForValue(value);
                g.drawLine(x, tickRect.y, x, tickRect.y + tickRect.height - 1);
            } else {
                int y = yPositionForValue(value);
                g.drawLine(tickRect.x, y, tickRect.x + tickRect.width - 1, y);
            }
        }
    }

    @Override
    protected Dimension getThumbSize() {
        int hit = AssetStyles.MIN_CONTROL_HIT_SIZE;
        return new Dimension(hit, hit);
    }
}
