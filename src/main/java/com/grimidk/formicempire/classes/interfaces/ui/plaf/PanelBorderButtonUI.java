package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

/** Paints flat buttons with a 1px outline and centered label. */
public final class PanelBorderButtonUI extends BasicButtonUI {
    public static final PanelBorderButtonUI INSTANCE = new PanelBorderButtonUI();

    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }

    private PanelBorderButtonUI() {
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        FontMetrics fm = g.getFontMetrics(b.getFont());
        Insets insets = b.getInsets();

        int bw = AssetStyles.BORDER_THICKNESS_BUTTON;
        int fillW = b.getWidth() - bw * 2;
        int fillH = b.getHeight() - bw * 2;
        if (b.isContentAreaFilled() && fillW > 0 && fillH > 0) {
            g.setColor(b.getBackground());
            g.fillRect(bw, bw, fillW, fillH);
        }

        Border border = b.getBorder();
        if (border != null) {
            border.paintBorder(b, g, 0, 0, b.getWidth(), b.getHeight());
        }

        Rectangle viewRect = new Rectangle(
                insets.left,
                insets.top,
                b.getWidth() - (insets.left + insets.right),
                b.getHeight() - (insets.top + insets.bottom));

        Rectangle textRect = new Rectangle();
        Rectangle iconRect = new Rectangle();
        String text = SwingUtilities.layoutCompoundLabel(
                b, fm, b.getText(), b.getIcon(),
                b.getVerticalAlignment(), b.getHorizontalAlignment(),
                b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                viewRect, iconRect, textRect,
                b.getText() == null ? 0 : b.getIconTextGap());

        paintIcon(g, b, iconRect);
        paintText(g, b, textRect, b.getText());
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        FontMetrics fm = g.getFontMetrics(b.getFont());
        g.setFont(b.getFont());
        g.setColor(buttonTextColor(b));
        int textX = textRect.x + (textRect.width - fm.stringWidth(text)) / 2;
        int textY = textRect.y + fm.getAscent() + (textRect.height - fm.getHeight()) / 2;
        g.drawString(text, textX, textY);
    }

    private static Color buttonTextColor(AbstractButton b) {
        // Model flag only — isEnabled() also walks parents; table cell renderers disable the row panel.
        if (!b.getModel().isEnabled()) {
            return AssetStyles.COLOR_LIGHT_GRAY;
        }
        return AssetStyles.FONT_COLOR;
    }
}
