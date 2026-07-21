package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class FlatProgressBarUI extends BasicProgressBarUI {
    public static ComponentUI createUI(JComponent c) {
        return new FlatProgressBarUI();
    }

    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        Insets insets = progressBar.getInsets();
        int width = progressBar.getWidth() - (insets.left + insets.right);
        int height = progressBar.getHeight() - (insets.top + insets.bottom);
        if (width <= 0 || height <= 0) {
            return;
        }
        int x = insets.left;
        int y = insets.top;

        g.setColor(AssetStyles.BACKGROUND_DARK);
        g.fillRect(x, y, width, height);
        g.setColor(AssetStyles.UI_BORDER_COLOR);
        g.drawRect(x, y, width - 1, height - 1);

        int range = progressBar.getMaximum() - progressBar.getMinimum();
        int fillWidth = 0;
        if (range > 0) {
            fillWidth = (int) ((long) width * (progressBar.getValue() - progressBar.getMinimum()) / range);
        }
        if (fillWidth > 0) {
            g.setColor(progressBar.getForeground());
            int fillW = Math.max(0, fillWidth - 2);
            int fillH = Math.max(0, height - 2);
            if (fillW > 0 && fillH > 0) {
                g.fillRect(x + 1, y + 1, fillW, fillH);
            }
        }

        if (progressBar.isStringPainted()) {
            paintString(g, x, y, width, height, fillWidth, insets);
        }
    }
}
