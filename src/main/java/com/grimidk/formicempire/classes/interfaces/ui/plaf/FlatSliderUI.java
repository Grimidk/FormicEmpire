package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;

public final class FlatSliderUI extends BasicSliderUI {
    public static final Dimension VERTICAL_SIZE = new Dimension(AssetStyles.MIN_CONTROL_HIT_SIZE, 96);
    private static final int TICK_LENGTH = 5;
    private static final int TRACK_BREADTH = Math.max(4, UiControlChrome.THUMB_BREADTH);

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
    public void setThumbLocation(int x, int y) {
        thumbRect.setLocation(x, y);
        slider.repaint();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        recalculateIfInsetsChanged();
        recalculateIfOrientationChanged();
        g.setColor(c.getBackground() != null ? c.getBackground() : AssetStyles.BACKGROUND_COLOR);
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        if (slider.getPaintTrack()) {
            paintTrack(g);
        }
        if (slider.getPaintTicks()) {
            paintTicks(g);
        }
        if (slider.getPaintLabels()) {
            paintLabels(g);
        }
        paintThumb(g);
    }

    @Override
    public void paintFocus(Graphics g) {
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
    protected void calculateTrackRect() {
        if (slider.getOrientation() != JSlider.HORIZONTAL) {
            super.calculateTrackRect();
            trackRect.x = contentRect.x;
            trackRect.width = Math.max(contentRect.width, getThumbSize().width);
            return;
        }
        int tickSpace = getTickLength();
        int labelSpace = slider.getPaintLabels() ? getHeightOfTallestLabel() : 0;
        int thumbH = thumbRect.height;
        int block = thumbH + tickSpace + labelSpace;
        int top = contentRect.y + Math.max(0, (contentRect.height - block - 1) / 2);
        trackRect.x = contentRect.x + trackBuffer;
        trackRect.width = Math.max(0, contentRect.width - (trackBuffer * 2));
        trackRect.height = TRACK_BREADTH;
        trackRect.y = top + Math.max(0, (thumbH - TRACK_BREADTH) / 2);
    }

    @Override
    protected void calculateTickRect() {
        if (slider.getOrientation() != JSlider.HORIZONTAL) {
            super.calculateTickRect();
            return;
        }
        int tickSpace = getTickLength();
        int labelSpace = slider.getPaintLabels() ? getHeightOfTallestLabel() : 0;
        int thumbH = thumbRect.height;
        int block = thumbH + tickSpace + labelSpace;
        int top = contentRect.y + Math.max(0, (contentRect.height - block - 1) / 2);
        tickRect.x = trackRect.x;
        tickRect.width = trackRect.width;
        tickRect.y = top + thumbH;
        tickRect.height = tickSpace;
    }

    @Override
    public void paintTrack(Graphics g) {
        if (slider.getOrientation() == JSlider.VERTICAL) {
            int cx = trackRect.x + trackRect.width / 2;
            int x = cx - TRACK_BREADTH / 2;
            UiControlChrome.paintSliderTrack(
                    g,
                    new Rectangle(x, trackRect.y, TRACK_BREADTH, trackRect.height));
            return;
        }
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
