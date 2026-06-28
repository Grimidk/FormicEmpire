package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatTabbedPaneUI;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.PanelBorderButtonUI;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** Button chrome — padding is a compound border on {@link AssetStyles#buttonPaddingBorder()}. */
public final class UiButtonStyles {
    private static final Insets NO_MARGIN = new Insets(0, 0, 0, 0);

    private UiButtonStyles() {
    }

    public static void style(AbstractButton button) {
        if (!(button.getUI() instanceof PanelBorderButtonUI)) {
            button.setUI(PanelBorderButtonUI.INSTANCE);
        }
        button.setFont(AssetStyles.FONT_BOLD);
        button.setForeground(AssetStyles.FONT_COLOR);
        button.setBackground(AssetStyles.BACKGROUND_COLOR);
        button.setCursor(null);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setOpaque(false);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMargin(NO_MARGIN);
        button.setBorder(AssetStyles.buttonPaddingBorder());
    }

    /** Smaller padding for table cells and inline controls. */
    public static void styleCompact(AbstractButton button) {
        style(button);
        button.setFont(AssetStyles.FONT_NORMAL);
        button.setBorder(AssetStyles.buttonCompactPaddingBorder());
    }

    /** Fixed-size tab strip button — matches {@link FlatTabbedPaneUI} tab dimensions. */
    public static void styleSectionTab(AbstractButton button) {
        if (!(button.getUI() instanceof PanelBorderButtonUI)) {
            button.setUI(PanelBorderButtonUI.INSTANCE);
        }
        button.setFont(AssetStyles.FONT_BOLD);
        button.setForeground(AssetStyles.FONT_COLOR);
        button.setBackground(AssetStyles.BACKGROUND_COLOR);
        button.setCursor(null);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setOpaque(false);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMargin(NO_MARGIN);
        button.setBorder(new EmptyBorder(AssetStyles.TAB_MARGIN_INSETS));
        Dimension size = AssetStyles.tabStripSize();
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
    }
}
