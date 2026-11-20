package com.grimidk.formicempire.classes.interfaces.ui.util;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;

public final class UiIconTooltips {
    public static final int TOOLTIP_INITIAL_DELAY_MS = 200;
    public static final int TOOLTIP_RESHOW_DELAY_MS = 200;

    private static final String AUTO_TOOLTIP_KEY = "formicempire.autoIconTooltip";
    private static final String UPDATING_TOOLTIP_KEY = "formicempire.updatingAutoIconTooltip";
    private static final String AUTO_TABS_KEY = "formicempire.autoIconTooltipTabs";

    private static final Map<Image, Supplier<String>> DYNAMIC_BY_IMAGE = new WeakHashMap<>();

    public static final PropertyChangeListener BUTTON_PROPERTY_LISTENER = evt -> {
        if (!(evt.getSource() instanceof AbstractButton button)) {
            return;
        }
        String name = evt.getPropertyName();
        if ("icon".equals(name)) {
            syncComponentTooltip(button, button.getIcon());
        } else if (JComponent.TOOL_TIP_TEXT_KEY.equals(name)) {
            handleToolTipPropertyChange(button);
        }
    };

    private UiIconTooltips() {
    }

    public static void registerResourceIcon(Icon icon, String path) {
        if (!(icon instanceof ImageIcon imageIcon) || imageIcon.getImage() == null || path == null) {
            return;
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        Supplier<String> supplier = Registry.BY_PATH.get(normalized);
        if (supplier != null) {
            synchronized (DYNAMIC_BY_IMAGE) {
                DYNAMIC_BY_IMAGE.put(imageIcon.getImage(), supplier);
            }
        }
    }

    public static String resolveTooltip(Icon icon) {
        if (!(icon instanceof ImageIcon imageIcon)) {
            return null;
        }
        Image image = imageIcon.getImage();
        if (image == null) {
            return null;
        }
        Supplier<String> supplier = Registry.BY_IMAGE.get(image);
        if (supplier == null) {
            synchronized (DYNAMIC_BY_IMAGE) {
                supplier = DYNAMIC_BY_IMAGE.get(image);
            }
        }
        if (supplier == null) {
            return null;
        }
        String text = supplier.get();
        return (text != null && !text.isBlank()) ? text : null;
    }

    public static void handleToolTipPropertyChange(JComponent comp) {
        if (comp == null) {
            return;
        }
        if (!Boolean.TRUE.equals(comp.getClientProperty(UPDATING_TOOLTIP_KEY))) {
            comp.putClientProperty(AUTO_TOOLTIP_KEY, null);
        }
    }

    public static void syncComponentTooltip(JComponent comp, Icon icon) {
        if (comp == null
                || comp instanceof TableCellRenderer
                || comp instanceof ListCellRenderer
                || comp instanceof JCheckBox
                || comp instanceof JRadioButton) {
            return;
        }
        boolean isAuto = Boolean.TRUE.equals(comp.getClientProperty(AUTO_TOOLTIP_KEY));
        String existing = comp.getToolTipText();
        if (existing != null && !isAuto) {
            return;
        }
        String resolved = resolveTooltip(icon);
        if (resolved != null) {
            if (isAuto && resolved.equals(existing)) {
                return;
            }
            comp.putClientProperty(AUTO_TOOLTIP_KEY, Boolean.TRUE);
            setToolTipInternal(comp, resolved);
        } else if (isAuto) {
            comp.putClientProperty(AUTO_TOOLTIP_KEY, null);
            if (existing != null) {
                setToolTipInternal(comp, null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void syncTabbedPane(JTabbedPane pane) {
        if (pane == null) {
            return;
        }
        Set<Integer> autoTabs = (Set<Integer>) pane.getClientProperty(AUTO_TABS_KEY);
        if (autoTabs == null) {
            autoTabs = new HashSet<>();
            pane.putClientProperty(AUTO_TABS_KEY, autoTabs);
        }
        int count = pane.getTabCount();
        autoTabs.removeIf(idx -> idx < 0 || idx >= count);
        for (int i = 0; i < count; i++) {
            String existing = pane.getToolTipTextAt(i);
            boolean isAuto = autoTabs.contains(i);
            if (existing != null && !isAuto) {
                continue;
            }
            String resolved = resolveTooltip(pane.getIconAt(i));
            if (resolved != null) {
                autoTabs.add(i);
                if (!resolved.equals(existing)) {
                    pane.setToolTipTextAt(i, resolved);
                }
            } else if (isAuto) {
                autoTabs.remove(i);
                if (existing != null) {
                    pane.setToolTipTextAt(i, null);
                }
            }
        }
    }

    public static void refreshTree(Component root) {
        if (root == null) {
            return;
        }
        if (root instanceof JLabel label) {
            syncComponentTooltip(label, label.getIcon());
        } else if (root instanceof AbstractButton button) {
            syncComponentTooltip(button, button.getIcon());
        } else if (root instanceof JTabbedPane pane) {
            syncTabbedPane(pane);
        }
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                refreshTree(child);
            }
        }
    }

    private static void setToolTipInternal(JComponent comp, String text) {
        comp.putClientProperty(UPDATING_TOOLTIP_KEY, Boolean.TRUE);
        try {
            comp.setToolTipText(text);
        } finally {
            comp.putClientProperty(UPDATING_TOOLTIP_KEY, null);
        }
    }

    private static final class Registry {
        private static final IdentityHashMap<Image, Supplier<String>> BY_IMAGE = new IdentityHashMap<>();
        private static final Map<String, Supplier<String>> BY_PATH = new HashMap<>();

        static {
            registerIconKey(GameConstants.ICON_SOCIALISM, LanguageStrings.ICON_SOCIALISM);
            registerIconKey(GameConstants.ICON_APHID, LanguageStrings.BUG_APHID);
            registerIconKey(GameConstants.ICON_SYMBIOTIC_MITE, LanguageStrings.BUG_SYMBIOTIC_MITE);
            registerIconKey(GameConstants.ICON_DERMESTID, LanguageStrings.BUG_DERMESTID);
            registerIconKey(GameConstants.ICON_PARASITIC_MITE, LanguageStrings.BUG_PARASITIC_MITE);
            registerIconKey(GameConstants.ICON_RESEARCH, LanguageStrings.TAB_RESEARCH);
            registerIconKey(GameConstants.ICON_TRADE, LanguageStrings.PANEL_TRADE);
            registerIconKey(GameConstants.ICON_AUTOMATION, LanguageStrings.STAT_AUTOMATION);
            registerIconKey(GameConstants.ICON_ATTACK, LanguageStrings.UNIT_STAT_ATTACK);
            registerIconKey(GameConstants.ICON_DEFENSE, LanguageStrings.UNIT_STAT_DEFENSE);
            registerIconKey(GameConstants.ICON_HEALTH, LanguageStrings.UNIT_STAT_HEALTH);
            registerIconKey(GameConstants.ICON_GROWTH, LanguageStrings.ICON_GROWTH);
            registerIconKey(GameConstants.ICON_RAFT, LanguageStrings.ASSIMILATION_RAFTING);
            registerIconKey(GameConstants.ICON_UNKNOWN, LanguageStrings.STAT_UNKNOWN);
            registerIconKey(GameConstants.SPRITE_UNKNOWN, LanguageStrings.STAT_UNKNOWN);
            registerIconKey(GameConstants.ICON_STAT_LOYALTY, LanguageStrings.STAT_LOYALTY);
            registerIconKey(GameConstants.ICON_STAT_REPUTATION, LanguageStrings.DYNASTY_REPUTATION);
            registerIconKey(GameConstants.ICON_STAT_INTELLIGENCE, LanguageStrings.DIPLO_ACTION_INTELLIGENCE);
            registerIconKey(GameConstants.ICON_STAT_COUNTER_INTELLIGENCE, LanguageStrings.ICON_COUNTER_INTELLIGENCE);
            registerIconKey(GameConstants.ICON_STAT_GENETIC_INTEGRITY, LanguageStrings.STAT_GENETIC_INTEGRITY);
            registerIconKey(GameConstants.ICON_STAT_MILITARY_POWER, LanguageStrings.STAT_MILITARY_POWER);
            registerIconKey(GameConstants.ICON_STAT_COMBAT_CAPACITY, LanguageStrings.STAT_COMBAT_CAPACITY);
            registerIconKey(GameConstants.ICON_STAT_POPULATION, LanguageStrings.PANEL_POPULATION);
            registerIconKey(GameConstants.ICON_STAT_FOOD_CONSUMPTION, LanguageStrings.ICON_FOOD_CONSUMPTION);
            registerIconKey(GameConstants.ICON_STAT_FOOD_PRODUCTION, LanguageStrings.ICON_FOOD_PRODUCTION);
            registerIconKey(GameConstants.ICON_STAT_NET_FOOD, LanguageStrings.ICON_NET_FOOD);
            registerIconKey(GameConstants.ICON_SPEED_ZERO, LanguageStrings.UI_PAUSED_TICK);
            registerIconKey(GameConstants.ICON_SPEED_UP, LanguageStrings.UI_SPEED_UP);
            registerIconKey(GameConstants.ICON_SPEED_DOWN, LanguageStrings.UI_SPEED_DOWN);
            registerIconKey(GameConstants.ICON_SPEED_PLAY, LanguageStrings.UI_PLAY);
            registerIconKey(GameConstants.ICON_SPEED_PAUSE, LanguageStrings.UI_PAUSE);
            registerIconKey(GameConstants.ICON_SOCIAL_DISCORD, LanguageStrings.UI_DISCORD);
            registerIconKey(GameConstants.ICON_SOCIAL_INSTAGRAM, LanguageStrings.UI_INSTAGRAM);
            registerIconKey(GameConstants.ICON_SOCIAL_HELP, LanguageStrings.UI_HELP);
            registerIconKey(GameConstants.ICON_SOCIAL_FEEDBACK, LanguageStrings.UI_FEEDBACK);
            registerIconKey(GameConstants.ICON_SOCIAL_SETTINGS, LanguageStrings.UI_SETTINGS);
            registerIconKey(GameConstants.ICON_MUSIC_PREV, LanguageStrings.MUSIC_PREV_TT);
            registerIconKey(GameConstants.ICON_MUSIC_PLAY, LanguageStrings.MUSIC_PLAY_TT);
            registerIconKey(GameConstants.ICON_MUSIC_PAUSE, LanguageStrings.MUSIC_PAUSE_TT);
            registerIconKey(GameConstants.ICON_MUSIC_NEXT, LanguageStrings.MUSIC_NEXT_TT);
            registerIconKey(GameConstants.ICON_MUSIC_SHUFFLE, LanguageStrings.MUSIC_SHUFFLE_TT);
            registerIconKey(GameConstants.ICON_MUSIC_MUTE, LanguageStrings.MUSIC_MUTE_TT);
            registerIconKey(GameConstants.ICON_MUSIC_UNMUTE, LanguageStrings.MUSIC_UNMUTE_TT);

            Image unknownImage = GameConstants.ICON_UNKNOWN != null ? GameConstants.ICON_UNKNOWN.getImage() : null;
            Image spriteUnknownImage = GameConstants.SPRITE_UNKNOWN != null ? GameConstants.SPRITE_UNKNOWN.getImage() : null;

            registerConstants(GameConstants.getResources(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getAntClasses(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getAntRoles(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getAntMods(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getSpecies(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getCritterSpecies(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getCritterClasses(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getSpeciesDiets(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getBiomes(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getDisasterTypes(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getWeathers(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getSeasons(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getMoonPhases(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getTimesOfDay(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getTemperature(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getHumidity(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getColonyRanks(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getColonyLoyalties(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getDiplomaticReputations(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getTradeMethods(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getTiers(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getGameSpeeds(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getBattleLines(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getWarStagePhases(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getWarStandings(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getSkills(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getAntStatuses(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getMoveStatuses(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getDynastyTitles(), unknownImage, spriteUnknownImage);
            registerConstants(GameConstants.getCityTitles(), unknownImage, spriteUnknownImage);

            for (Building b : GameUnlocks.getBuildings()) {
                if (b != null) {
                    registerImage(b.getIcon(), b::getDisplayName, unknownImage, spriteUnknownImage);
                }
            }
            for (Upgrade u : GameUnlocks.getUpgrades()) {
                if (u != null) {
                    registerImage(u.getIcon(), u::getDisplayName, unknownImage, spriteUnknownImage);
                }
            }
            for (Assimilation a : GameUnlocks.getAssimilations()) {
                if (a != null) {
                    registerImage(a.getIcon(), a::getName, unknownImage, spriteUnknownImage);
                }
            }
            for (Synergy s : GameUnlocks.getSynergies()) {
                if (s != null) {
                    registerImage(s.getIcon(), s::getName, unknownImage, spriteUnknownImage);
                }
            }

            registerPathKey("icons/ui/Close.png", LanguageStrings.UI_CLOSE);
            registerPathKey("icons/ui/Conquer.png", LanguageStrings.MAP_CONQUER_COLONY);
            registerPathKey("icons/ui/GoHome.png", LanguageStrings.MAP_HOME_BUTTON);
            registerPathKey("icons/ui/Layers.png", LanguageStrings.MAP_LAYERS);
            registerPathKey("icons/tools/ToolBrush.png", LanguageStrings.UI_WORLD_TOOL_BRUSH);
            registerPathKey("icons/tools/ToolLine.png", LanguageStrings.UI_WORLD_TOOL_LINE);
            registerPathKey("icons/tools/ToolFill.png", LanguageStrings.UI_WORLD_TOOL_FILL);
            registerPathKey("icons/tools/ToolEraser.png", LanguageStrings.UI_WORLD_TOOL_ERASER);
            registerPathKey("icons/tools/ToolPicker.png", LanguageStrings.UI_WORLD_TOOL_PICKER);
            registerPathKey("icons/tools/ToolUndo.png", LanguageStrings.UI_UNDO);
            registerPathKey("icons/tools/MinimapOverview.png", LanguageStrings.MAP_MODE_BUTTON);
            registerPathKey("icons/tools/FaultConvergent.png", LanguageStrings.UI_FAULT_CONVERGENT);
            registerPathKey("icons/tools/FaultDivergent.png", LanguageStrings.UI_FAULT_DIVERGENT);
            registerPathKey("icons/tools/FaultTransform.png", LanguageStrings.UI_FAULT_TRANSFORM);
            registerPathKey("icons/tools/FaultIntensity1.png", LanguageStrings.UI_INTENSITY);
            registerPathKey("icons/tools/FaultIntensity2.png", LanguageStrings.UI_INTENSITY);
            registerPathKey("icons/tools/FaultIntensity3.png", LanguageStrings.UI_INTENSITY);
            registerPathKey("icons/misc/Automation.png", LanguageStrings.STAT_AUTOMATION);
            registerPathKey("icons/misc/MilitaryPower.png", LanguageStrings.STAT_MILITARY_POWER);
            registerPathKey("icons/misc/Time.png", LanguageStrings.PANEL_TIME);
            BY_PATH.put("icons/weather/Fog.png", GameConstants.WEATHER_FOG::getName);
            BY_PATH.put("icons/disasters/Meteor.png", GameConstants.DISASTER_METEOR::getName);
            BY_PATH.put("icons/roles/Commander.png", GameConstants.ROLE_COMMANDER::getName);
        }

        private static void registerIconKey(ImageIcon icon, String key) {
            if (icon != null && icon.getImage() != null && key != null) {
                BY_IMAGE.putIfAbsent(icon.getImage(), () -> LanguageStrings.get(key));
            }
        }

        private static void registerPathKey(String path, String key) {
            BY_PATH.put(path, () -> LanguageStrings.get(key));
        }

        private static void registerConstants(List<? extends Constant> constants, Image unknownImage, Image spriteUnknownImage) {
            if (constants == null) {
                return;
            }
            for (Constant c : constants) {
                if (c != null) {
                    registerImage(c.getIcon(), c::getName, unknownImage, spriteUnknownImage);
                }
            }
        }

        private static void registerImage(ImageIcon icon, Supplier<String> supplier, Image unknownImage, Image spriteUnknownImage) {
            if (icon == null || supplier == null) {
                return;
            }
            Image img = icon.getImage();
            if (img == null || img == unknownImage || img == spriteUnknownImage) {
                return;
            }
            BY_IMAGE.putIfAbsent(img, supplier);
        }
    }
}
