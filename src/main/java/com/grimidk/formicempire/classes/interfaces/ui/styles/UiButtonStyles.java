package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.infrasctructure.audio.SfxService;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.IconButtonUI;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.PanelBorderButtonUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public final class UiButtonStyles {
    private static final Insets NO_MARGIN = new Insets(0, 0, 0, 0);
    private static final String MENU_CLICK_SFX_KEY = "formicempire.menuClickSfx";

    private UiButtonStyles() {
    }

    public static void style(AbstractButton button) {
        if (!(button.getUI() instanceof PanelBorderButtonUI)) {
            button.setUI(PanelBorderButtonUI.INSTANCE);
        }
        button.setFont(AssetStyles.FONT_BOLD);
        button.setForeground(AssetStyles.FONT_COLOR);
        button.setBackground(AssetStyles.BACKGROUND_COLOR);
        button.setCursor(AssetStyles.cursorClickable());
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setOpaque(false);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMargin(NO_MARGIN);
        button.setBorder(AssetStyles.buttonPaddingBorder());
        installMenuClickSound(button);
    }

    public static void styleCompact(AbstractButton button) {
        style(button);
        button.setFont(AssetStyles.FONT_NORMAL);
        button.setBorder(AssetStyles.buttonCompactPaddingBorder());
    }

    public static void styleIcon(AbstractButton button) {
        if (!(button.getUI() instanceof IconButtonUI)) {
            button.setUI(IconButtonUI.INSTANCE);
        }
        button.setFont(AssetStyles.FONT_NORMAL);
        button.setForeground(AssetStyles.FONT_COLOR);
        button.setBackground(AssetStyles.BACKGROUND_COLOR);
        button.setCursor(AssetStyles.cursorClickable());
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMargin(NO_MARGIN);
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.putClientProperty(AssetStyles.ICON_BUTTON_CLIENT_KEY, Boolean.TRUE);
    }

    public static void styleMenu(AbstractButton button) {
        style(button);
        button.putClientProperty(AssetStyles.MENU_BUTTON_CLIENT_KEY, Boolean.TRUE);
        Color bg = AssetStyles.BACKGROUND_COLOR;
        button.setBackground(AssetStyles.colorFromRgba(
                bg.getRed(), bg.getGreen(), bg.getBlue(), AssetStyles.MENU_BUTTON_ALPHA));
        FontMetrics metrics = button.getFontMetrics(button.getFont());
        Insets insets = AssetStyles.BUTTON_MARGIN_INSETS;
        int height = insets.top + insets.bottom + metrics.getHeight()
                + AssetStyles.BORDER_THICKNESS_BUTTON * 2;
        Dimension size = new Dimension(
                AssetStyles.MENU_BUTTON_WIDTH,
                Math.max(height, AssetStyles.MIN_CONTROL_HIT_SIZE));
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
    }

    public static void styleSectionTab(AbstractButton button) {
        if (!(button.getUI() instanceof PanelBorderButtonUI)) {
            button.setUI(PanelBorderButtonUI.INSTANCE);
        }
        button.setFont(AssetStyles.FONT_BOLD);
        button.setForeground(AssetStyles.FONT_COLOR);
        button.setBackground(AssetStyles.TAB_UNSELECTED_BG);
        button.setCursor(AssetStyles.cursorClickable());
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setOpaque(false);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        Insets insets = AssetStyles.TAB_MARGIN_INSETS;
        button.setBorder(new EmptyBorder(insets.top, insets.left, insets.bottom, Math.max(0, insets.right - 1)));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, AssetStyles.TAB_STRIP_HEIGHT));
        button.setMinimumSize(new Dimension(AssetStyles.TAB_STRIP_WIDTH, AssetStyles.TAB_STRIP_HEIGHT));
        installMenuClickSound(button);
    }

    public static void applyTabSelection(AbstractButton button, boolean selected) {
        if (button == null) {
            return;
        }
        if (selected) {
            button.setBackground(AssetStyles.TAB_SELECTED_BG);
            button.setForeground(AssetStyles.FONT_COLOR_HEADER);
        } else {
            button.setBackground(AssetStyles.TAB_UNSELECTED_BG);
            button.setForeground(AssetStyles.FONT_COLOR);
        }
        resizeSectionTab(button);
    }

    public static void resizeSectionTab(AbstractButton button) {
        if (button == null) {
            return;
        }
        FontMetrics metrics = button.getFontMetrics(button.getFont());
        Insets insets = AssetStyles.TAB_MARGIN_INSETS;
        int width = Math.max(
                AssetStyles.TAB_STRIP_WIDTH,
                insets.left + insets.right + metrics.stringWidth(button.getText()));
        Dimension size = new Dimension(width, AssetStyles.TAB_STRIP_HEIGHT);
        button.setPreferredSize(size);
        button.setMinimumSize(new Dimension(Math.min(width, AssetStyles.TAB_STRIP_WIDTH), AssetStyles.TAB_STRIP_HEIGHT));
    }

    private static void installMenuClickSound(AbstractButton button) {
        if (button == null || Boolean.TRUE.equals(button.getClientProperty(MENU_CLICK_SFX_KEY))) {
            return;
        }
        button.putClientProperty(MENU_CLICK_SFX_KEY, Boolean.TRUE);
        button.addActionListener(e -> SfxService.play(SoundEffects.MENU_CLICK));
    }
}
