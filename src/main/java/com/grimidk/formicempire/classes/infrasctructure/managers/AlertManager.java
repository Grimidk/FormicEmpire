package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.AlertPanel;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.AlertPanel.Alert;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;

public class AlertManager {
    private final Colony colony;
    private final AlertPanel panel;
    private final List<Alert> activeAlerts = new ArrayList<>();
    
    private final int durationDefault = 10000;

    public AlertManager(Colony colony, AlertPanel panel) {
        this.colony = colony;
        this.panel = panel;
    }

    private static String stripPrefix(String msg, String prefix) {
        if (!msg.startsWith(prefix)) {
            return msg;
        }
        String rest = msg.substring(prefix.length());
        if (rest.startsWith(" ")) {
            return rest.substring(1);
        }
        return rest;
    }

    public void checkStatus() {
        Iterator<Alert> it = activeAlerts.iterator();
        while (it.hasNext()) {
            if (it.next().isExpired()) {
                it.remove();
            }
        }

        List<String> events = colony.consumeEvents();
        for (String msg : events) {
            if (msg.startsWith(ColonyLogPrefixes.DEATH)) {
                addAlert("DEATH", stripPrefix(msg, ColonyLogPrefixes.DEATH), AssetStyles.FONT_COLOR_ERROR, durationDefault); 
            } 
            else if (msg.startsWith(ColonyLogPrefixes.WARNING)) {
                addAlert("WARN", stripPrefix(msg, ColonyLogPrefixes.WARNING), AssetStyles.FONT_COLOR_WARNING, durationDefault);
            } 
            else if (msg.startsWith(ColonyLogPrefixes.SUCCESS)) {
                String body = stripPrefix(msg, ColonyLogPrefixes.SUCCESS);
                addAlert("SUCC", LanguageStrings.get(LanguageStrings.ALERT_BUILT_PREFIX) + body, AssetStyles.FONT_COLOR_SUCCESS, durationDefault);
            } 
            else if (msg.startsWith(ColonyLogPrefixes.COMPOST)) {
                addAlert("COMP", stripPrefix(msg, ColonyLogPrefixes.COMPOST), AssetStyles.FONT_COLOR_HIGHLIGHT, durationDefault);
            } 
            else if (msg.startsWith(ColonyLogPrefixes.NUPTIAL)) {
                addAlert("NUPTIAL", stripPrefix(msg, ColonyLogPrefixes.NUPTIAL), AssetStyles.FONT_COLOR_HEADER, durationDefault);
            }
            else if (msg.startsWith(ColonyLogPrefixes.TRADE)) {
                addAlert("TRADE", stripPrefix(msg, ColonyLogPrefixes.TRADE), AssetStyles.FONT_COLOR, durationDefault);
            }
            else if (msg.startsWith(ColonyLogPrefixes.DYNASTY)) {
                addAlert("DYN", stripPrefix(msg, ColonyLogPrefixes.DYNASTY), AssetStyles.FONT_COLOR, durationDefault);
            }
            else if (msg.startsWith(ColonyLogPrefixes.FAILURE)) {
                addAlert("FAIL", stripPrefix(msg, ColonyLogPrefixes.FAILURE), AssetStyles.FONT_COLOR_ERROR, durationDefault);
            }
            else if (msg.startsWith(ColonyLogPrefixes.AUTOMATION)) {
                addAlert("AUTO", stripPrefix(msg, ColonyLogPrefixes.AUTOMATION), AssetStyles.FONT_COLOR, durationDefault);
            }
            else if (msg.startsWith(ColonyLogPrefixes.PROMOTION)) {
                addAlert("PROMO", stripPrefix(msg, ColonyLogPrefixes.PROMOTION), AssetStyles.FONT_COLOR_HIGHLIGHT, durationDefault);
            }
            else if (msg.startsWith(ColonyLogPrefixes.INFO)) {
                addAlert("INFO", stripPrefix(msg, ColonyLogPrefixes.INFO), AssetStyles.FONT_COLOR, durationDefault);
            }
            else {
                addAlert("INFO", msg, AssetStyles.FONT_COLOR, durationDefault);
            }
        }

        checkResourceWarning();
        checkAvailableResearch();
        checkAvailableBuildings();
        checkBodyPile();

        SwingUtilities.invokeLater(() -> panel.updateAlerts(new ArrayList<>(activeAlerts)));
    }

    private void addAlert(String key, String msg, Color color, long duration) {
        activeAlerts.removeIf(a -> a.key.equals(key));
        activeAlerts.add(0, new Alert(key, msg, color, duration));
    }

    private void checkResourceWarning() {
        int production = colony.getTotalProduction();
        int consumption = colony.getTotalConsumption();
        if (consumption > production && colony.getMushrooms() < consumption * 24) {
            addAlert("STARVE", LanguageStrings.get(LanguageStrings.ALERT_STARVATION_RISK), AssetStyles.FONT_COLOR_ERROR, durationDefault);
        }
    }

    private void checkAvailableResearch() {
        if (!colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) return;
        for (Upgrade u : GameUnlocks.getUpgrades()) {
            if (!colony.hasUpgrade(u) && u.getCost() > 0 && 
               (u.getRequirement() == null || colony.hasUpgrade(u.getRequirement())) && 
               colony.getResearchPoints() >= u.getCost()) {
                addAlert("RESEARCH", LanguageStrings.get(LanguageStrings.ALERT_NEW_RESEARCH), AssetStyles.FONT_COLOR_HIGHLIGHT, durationDefault);
                return; 
            }
        }
    }
    
    private void checkAvailableBuildings() {
        if (!colony.hasUpgrade(GameUnlocks.ABILITY_BUILD)) return;
        if (colony.getCurrentBuildingProject() != null) return; 

        for (Building b : GameUnlocks.getBuildings()) {
            if (!colony.hasBuilding(b) && colony.getMinerals() >= b.getMineralCost() && 
                colony.getResins() >= b.getResinCost() && colony.hasBuilding(b.getRequirement())) {
                addAlert("BUILD", String.format(LanguageStrings.get(LanguageStrings.ALERT_CAN_BUILD_FMT), b.getName()), AssetStyles.FONT_COLOR_VALUE, durationDefault);
                return; 
            }
        }
    }

    private void checkBodyPile() {
        int deadCount = colony.getDeadAnts().size();
        if (deadCount >= 500) {
            addAlert("DEAD", String.format(LanguageStrings.get(LanguageStrings.ALERT_BODY_PILE_FMT), deadCount), AssetStyles.FONT_COLOR_ERROR, durationDefault);
        }
    }
}
