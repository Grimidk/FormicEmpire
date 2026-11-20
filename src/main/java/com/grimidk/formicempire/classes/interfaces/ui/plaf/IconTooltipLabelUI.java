package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.interfaces.ui.util.UiIconTooltips;

import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;

public final class IconTooltipLabelUI extends BasicLabelUI {
    public static final IconTooltipLabelUI INSTANCE = new IconTooltipLabelUI();

    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }

    private IconTooltipLabelUI() {
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLabel label) {
            UiIconTooltips.syncComponentTooltip(label, label.getIcon());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (!(e.getSource() instanceof JLabel label)) {
            return;
        }
        String name = e.getPropertyName();
        if ("icon".equals(name)) {
            UiIconTooltips.syncComponentTooltip(label, label.getIcon());
        } else if (JComponent.TOOL_TIP_TEXT_KEY.equals(name)) {
            UiIconTooltips.handleToolTipPropertyChange(label);
        }
    }
}
