package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import java.awt.Graphics;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

/** Icon-only control with no border or fill — for toolbar glyphs. */
public final class IconButtonUI extends BasicButtonUI {
    public static final IconButtonUI INSTANCE = new IconButtonUI();

    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }

    private IconButtonUI() {
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton button = (AbstractButton) c;
        Icon icon = button.getIcon();
        if (icon == null) {
            return;
        }
        if (!button.getModel().isEnabled()) {
            Icon disabled = button.getDisabledIcon();
            if (disabled != null) {
                icon = disabled;
            }
        }
        int x = (button.getWidth() - icon.getIconWidth()) / 2;
        int y = (button.getHeight() - icon.getIconHeight()) / 2;
        icon.paintIcon(button, g, x, y);
    }
}
