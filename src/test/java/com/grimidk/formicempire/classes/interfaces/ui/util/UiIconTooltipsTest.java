package com.grimidk.formicempire.classes.interfaces.ui.util;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.icons.SquareCheckIcons;
import com.grimidk.formicempire.classes.interfaces.ui.icons.SquareRadioIcons;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiTableStyles;
import com.grimidk.formicempire.classes.interfaces.ui.theme.UiLookAndFeel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UiIconTooltipsTest {

    @AfterEach
    void restoreLanguage() {
        LanguageStrings.setLanguage("en");
    }

    @Test
    void lookAndFeelSetsTwoHundredMillisecondTooltipDelay() {
        UiLookAndFeel.applyGlobalStyles();
        assertEquals(UiIconTooltips.TOOLTIP_INITIAL_DELAY_MS, ToolTipManager.sharedInstance().getInitialDelay());
        assertEquals(UiIconTooltips.TOOLTIP_RESHOW_DELAY_MS, ToolTipManager.sharedInstance().getReshowDelay());
    }

    @Test
    void resolvesTooltipsForAllCustomIconsAndSkipsControlIcons() {
        for (ImageIcon icon : GameConstants.getMisc()) {
            String tip = UiIconTooltips.resolveTooltip(icon);
            assertNotNull(tip);
            assertFalse(tip.isBlank());
        }

        ImageIcon[] extraIcons = {
                GameConstants.ICON_SPEED_ZERO,
                GameConstants.ICON_SPEED_UP,
                GameConstants.ICON_SPEED_DOWN,
                GameConstants.ICON_SPEED_PLAY,
                GameConstants.ICON_SPEED_PAUSE,
                GameConstants.ICON_SOCIAL_DISCORD,
                GameConstants.ICON_SOCIAL_INSTAGRAM,
                GameConstants.ICON_SOCIAL_HELP,
                GameConstants.ICON_SOCIAL_FEEDBACK,
                GameConstants.ICON_SOCIAL_SETTINGS,
                GameConstants.ICON_MUSIC_PREV,
                GameConstants.ICON_MUSIC_PLAY,
                GameConstants.ICON_MUSIC_PAUSE,
                GameConstants.ICON_MUSIC_NEXT,
                GameConstants.ICON_MUSIC_SHUFFLE,
                GameConstants.ICON_MUSIC_MUTE,
                GameConstants.ICON_MUSIC_UNMUTE,
                GameConstants.RESOURCE_FUNGI.getIcon(),
                GameConstants.CLASS_WORKER.getIcon(),
                GameConstants.ROLE_LAYER.getIcon(),
                GameConstants.BIOME_PLAINS.getIcon(),
                GameConstants.WEATHER_FOG.getIcon(),
                GameConstants.RANK_COLONY.getIcon(),
                GameConstants.TIER_1.getIcon(),
                GameConstants.DISASTER_METEOR.getIcon(),
                GameUnlocks.getBuildings().get(0).getIcon(),
                GameUnlocks.getUpgrades().get(0).getIcon()
        };
        for (ImageIcon icon : extraIcons) {
            String tip = UiIconTooltips.resolveTooltip(icon);
            assertNotNull(tip);
            assertFalse(tip.isBlank());
        }

        Icon brushIcon = UiResourceLoader.loadDialogIcon(UiIconTooltipsTest.class, "/icons/tools/ToolBrush.png", 16);
        assertEquals(LanguageStrings.get(LanguageStrings.UI_WORLD_TOOL_BRUSH), UiIconTooltips.resolveTooltip(brushIcon));

        assertNull(UiIconTooltips.resolveTooltip(SquareCheckIcons.UNCHECKED));
        assertNull(UiIconTooltips.resolveTooltip(SquareRadioIcons.UNSELECTED));
    }

    @Test
    void labelsButtonsTabsAndTablesAutoPopulateAndPreserveExplicitTooltips() {
        UiLookAndFeel.applyGlobalStyles();

        JLabel autoLabel = new JLabel(GameConstants.ROLE_LAYER.getIcon());
        assertEquals(GameConstants.ROLE_LAYER.getName(), autoLabel.getToolTipText());

        LanguageStrings.setLanguage("es");
        UiIconTooltips.refreshTree(autoLabel);
        assertEquals(GameConstants.ROLE_LAYER.getName(), autoLabel.getToolTipText());

        JLabel explicitLabel = new JLabel(GameConstants.ROLE_LAYER.getIcon());
        explicitLabel.setToolTipText("Custom");
        explicitLabel.setIcon(GameConstants.CLASS_WORKER.getIcon());
        assertEquals("Custom", explicitLabel.getToolTipText());

        JButton borderButton = new JButton(GameConstants.ICON_RESEARCH);
        AssetStyles.styleButton(borderButton);
        assertEquals(LanguageStrings.get(LanguageStrings.TAB_RESEARCH), borderButton.getToolTipText());
        borderButton.setToolTipText("CustomButton");
        borderButton.setIcon(GameConstants.ICON_TRADE);
        assertEquals("CustomButton", borderButton.getToolTipText());

        JButton iconButton = new JButton(GameConstants.ICON_SPEED_PLAY);
        AssetStyles.styleIconButton(iconButton);
        assertEquals(LanguageStrings.get(LanguageStrings.UI_PLAY), iconButton.getToolTipText());
        iconButton.setToolTipText("CustomIconButton");
        iconButton.setIcon(GameConstants.ICON_SPEED_PAUSE);
        assertEquals("CustomIconButton", iconButton.getToolTipText());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Tab", GameConstants.ICON_TRADE, new JLabel());
        UiIconTooltips.syncTabbedPane(tabbedPane);
        assertEquals(LanguageStrings.get(LanguageStrings.PANEL_TRADE), tabbedPane.getToolTipTextAt(0));

        DefaultTableCellRenderer iconRenderer = UiTableStyles.createIconCellRenderer();
        JTable table = new JTable(1, 1);
        Component cell = iconRenderer.getTableCellRendererComponent(
                table, GameConstants.ICON_STAT_MILITARY_POWER, false, false, 0, 0);
        assertEquals(LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER), ((DefaultTableCellRenderer) cell).getToolTipText());
    }
}
