package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

/** Paints {@link FlatChevronButton} with a flat border and rotated triangle glyph. */
public final class FlatChevronButtonUI extends BasicButtonUI {
    private static final String ARROW_GLYPH = "\u25B2";

    public static ComponentUI createUI(JComponent c) {
        return new FlatChevronButtonUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        int bw = AssetStyles.BORDER_THICKNESS_BUTTON;
        int fillW = b.getWidth() - bw * 2;
        int fillH = b.getHeight() - bw * 2;
        if (b.isContentAreaFilled() && fillW > 0 && fillH > 0) {
            g.setColor(b.getBackground());
            g.fillRect(bw, bw, fillW, fillH);
        }

        g.setColor(AssetStyles.UI_BORDER_COLOR);
        g.drawRect(0, 0, b.getWidth() - 1, b.getHeight() - 1);

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setFont(b.getFont());
            g2d.setColor(b.getModel().isEnabled() ? AssetStyles.FONT_COLOR : AssetStyles.COLOR_LIGHT_GRAY);
            FontMetrics fm = g2d.getFontMetrics();
            int textW = fm.stringWidth(ARROW_GLYPH);
            int textH = fm.getAscent();
            double rotation = (c instanceof FlatChevronButton chevronButton && chevronButton.isPointsLeft())
                    ? -Math.PI / 2.0
                    : Math.PI / 2.0;
            AffineTransform old = g2d.getTransform();
            g2d.rotate(rotation, b.getWidth() / 2.0, b.getHeight() / 2.0);
            int textX = (b.getWidth() - textW) / 2;
            int textY = (b.getHeight() + textH) / 2 - fm.getDescent();
            g2d.drawString(ARROW_GLYPH, textX, textY);
            g2d.setTransform(old);
        } finally {
            g2d.dispose();
        }
    }
}
