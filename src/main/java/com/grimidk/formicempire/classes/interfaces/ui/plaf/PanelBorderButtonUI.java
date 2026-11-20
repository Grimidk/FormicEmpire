package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiIconTooltips;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

public final class PanelBorderButtonUI extends BasicButtonUI {
    public static final PanelBorderButtonUI INSTANCE = new PanelBorderButtonUI();

    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }

    private PanelBorderButtonUI() {
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof AbstractButton b) {
            UiIconTooltips.syncComponentTooltip(b, b.getIcon());
        }
    }

    @Override
    protected void installListeners(AbstractButton b) {
        super.installListeners(b);
        b.addPropertyChangeListener(UiIconTooltips.BUTTON_PROPERTY_LISTENER);
    }

    @Override
    protected void uninstallListeners(AbstractButton b) {
        b.removePropertyChangeListener(UiIconTooltips.BUTTON_PROPERTY_LISTENER);
        super.uninstallListeners(b);
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

        Icon icon = b.getIcon();
        String fullText = b.getText();
        boolean hasText = fullText != null && !fullText.isEmpty();

        Rectangle textRect = new Rectangle();
        Rectangle iconRect = new Rectangle();
        if (icon != null && hasText) {
            int gap = Math.max(4, b.getIconTextGap());
            int totalW = icon.getIconWidth() + gap + fm.stringWidth(fullText);
            iconRect.x = b.getHorizontalAlignment() == SwingConstants.LEFT
                    ? insets.left
                    : Math.max(insets.left, (b.getWidth() - totalW) / 2);
            iconRect.y = (b.getHeight() - icon.getIconHeight()) / 2;
            iconRect.width = icon.getIconWidth();
            iconRect.height = icon.getIconHeight();
            textRect.x = iconRect.x + icon.getIconWidth() + gap;
            textRect.y = 0;
            textRect.width = fm.stringWidth(fullText);
            textRect.height = b.getHeight();
            paintIcon(g, b, iconRect);
            paintText(g, b, textRect, fullText);
        } else if (icon != null) {
            iconRect.x = (b.getWidth() - icon.getIconWidth()) / 2;
            iconRect.y = (b.getHeight() - icon.getIconHeight()) / 2;
            iconRect.width = icon.getIconWidth();
            iconRect.height = icon.getIconHeight();
            paintIcon(g, b, iconRect);
        } else if (hasText) {
            textRect.x = 0;
            textRect.y = 0;
            textRect.width = b.getWidth();
            textRect.height = b.getHeight();
            paintText(g, b, textRect, fullText);
        }
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        FontMetrics fm = g.getFontMetrics(b.getFont());
        g.setFont(b.getFont());
        g.setColor(buttonTextColor(b));
        int textX = (b.getIcon() != null)
                ? textRect.x
                : (b.getWidth() - fm.stringWidth(text)) / 2;
        int textY = (b.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, textX, textY);
    }

    private static Color buttonTextColor(AbstractButton b) {
        if (!b.getModel().isEnabled()) {
            return AssetStyles.COLOR_LIGHT_GRAY;
        }
        return AssetStyles.FONT_COLOR;
    }
}
