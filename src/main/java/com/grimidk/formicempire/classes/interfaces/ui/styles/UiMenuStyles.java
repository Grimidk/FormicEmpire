package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.infrasctructure.audio.SfxService;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JMenuItem;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

public final class UiMenuStyles {
    private static final String MENU_CLICK_SFX_KEY = "formicempire.menuClickSfx";

    private UiMenuStyles() {
    }

    public static void style(JMenuItem menuItem) {
        menuItem.setForeground(AssetStyles.FONT_COLOR);
        menuItem.setBackground(AssetStyles.BACKGROUND_COLOR);
        menuItem.setOpaque(true);
        menuItem.setCursor(AssetStyles.cursorClickable());
        if (!(menuItem.getUI() instanceof BasicMenuItemUI)) {
            menuItem.setUI((MenuItemUI) BasicMenuItemUI.createUI(menuItem));
        }
        installMenuClickSound(menuItem);
    }

    private static void installMenuClickSound(JMenuItem menuItem) {
        if (menuItem == null || Boolean.TRUE.equals(menuItem.getClientProperty(MENU_CLICK_SFX_KEY))) {
            return;
        }
        menuItem.putClientProperty(MENU_CLICK_SFX_KEY, Boolean.TRUE);
        menuItem.addActionListener(e -> SfxService.play(SoundEffects.MENU_CLICK));
    }
}
