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
    public static final Dimension VERTICAL_SIZE = new Dimension(FlatScrollBarUI.VERTICAL_BAR_WIDTH, 96);
    private static final int TRACK_BREADTH = FlatScrollBarUI.VERTICAL_BAR_WIDTH;

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
    }

    @Override
    protected int getTickLength() {
        return 0;
    }

    @Override
    public Dimension getPreferredVerticalSize() {
        return new Dimension(VERTICAL_SIZE);
    }

    @Override
    public Dimension getPreferredHorizontalSize() {
        Dimension size = super.getPreferredHorizontalSize();
        size.height = Math.max(size.height, TRACK_BREADTH + UiControlChrome.THUMB_BREADTH);
        return size;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
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
        if (spacing <= 0) {
            return;
        }
        g.setColor(AssetStyles.UI_BORDER_COLOR);
        int min = slider.getMinimum();
        int max = slider.getMaximum();
        for (int value = min; value <= max; value += spacing) {
            if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
                int x = xPositionForValue(value);
                g.drawLine(x, trackRect.y + 1, x, trackRect.y + trackRect.height - 2);
            } else {
                int y = yPositionForValue(value);
                g.drawLine(trackRect.x + 1, y, trackRect.x + trackRect.width - 2, y);
            }
        }
    }

    @Override
    protected Dimension getThumbSize() {
        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            return new Dimension(FlatScrollBarUI.MIN_THUMB_LENGTH, UiControlChrome.THUMB_BREADTH);
        }
        return new Dimension(UiControlChrome.THUMB_BREADTH, UiControlChrome.THUMB_BREADTH);
    }
}
