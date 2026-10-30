package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.infrasctructure.audio.SfxService;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

public final class UiTabbedPaneStyles {
    private static final String MENU_CLICK_SFX_KEY = "formicempire.menuClickSfx";

    private UiTabbedPaneStyles() {
    }

    public static void style(JTabbedPane tabbedPane) {
        tabbedPane.setFont(AssetStyles.FONT_BOLD);
        tabbedPane.setBackground(AssetStyles.TAB_UNSELECTED_BG);
        tabbedPane.setForeground(AssetStyles.FONT_COLOR);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.updateUI();
        installMenuClickSound(tabbedPane);
    }

    private static void installMenuClickSound(JTabbedPane tabbedPane) {
        if (tabbedPane == null || Boolean.TRUE.equals(tabbedPane.getClientProperty(MENU_CLICK_SFX_KEY))) {
            return;
        }
        tabbedPane.putClientProperty(MENU_CLICK_SFX_KEY, Boolean.TRUE);
        ChangeListener listener = e -> SfxService.play(SoundEffects.MENU_CLICK);
        tabbedPane.addChangeListener(listener);
    }
}
