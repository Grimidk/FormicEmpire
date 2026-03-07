package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
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

    public void checkStatus() {
        Iterator<Alert> it = activeAlerts.iterator();
        while (it.hasNext()) {
            if (it.next().isExpired()) {
                it.remove();
            }
        }

        List<String> events = colony.consumeEvents();
        for (String msg : events) {
            if (msg.startsWith("CRITICAL")) {
                String shortMsg = msg.replace("CRITICAL: ", "").replace(" new Queens joined", " Queens In");
                addAlert("CRIT", shortMsg, AssetStyles.FONT_COLOR_ERROR, durationDefault); 
            } 
            else if (msg.startsWith("DEATH:")) {
                String cleanMsg = msg.replace("DEATH: ", "");
                addAlert("DEATH", cleanMsg, AssetStyles.FONT_COLOR_ERROR, durationDefault);
            }
            else if (msg.startsWith("WARNING")) {
                String shortWarn = msg.replace("WARNING: ", "").replace(" Juveniles Died (Nursing)", " Juveniles Lost");
                addAlert("WARN", shortWarn, AssetStyles.FONT_COLOR_WARNING, durationDefault);
            } 
            else if (msg.startsWith("SUCCESS")) {
                addAlert("SUCC", msg.replace("SUCCESS: ", "Built: "), AssetStyles.FONT_COLOR_SUCCESS, durationDefault);
            } 
            else if (msg.startsWith("COMPOST")) {
                String display = msg.replace("COMPOST: Recycled ", "Recycled ").replace(" bodies into mushroom matter.", " Bodies");
                addAlert("COMP", display, AssetStyles.FONT_COLOR_HIGHLIGHT, durationDefault);
            } 
            else if (msg.contains("Nuptial Flight")) {
                addAlert("NUPTIAL", "Nuptial Flight Occurred", AssetStyles.FONT_COLOR_HEADER, durationDefault);
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
            addAlert("STARVE", "Starvation Risk!", AssetStyles.FONT_COLOR_ERROR, durationDefault);
        }
    }

    private void checkAvailableResearch() {
        if (!colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) return;
        for (Upgrade u : GameUnlocks.getUpgrades()) {
            if (!colony.hasUpgrade(u) && u.getCost() > 0 && 
               (u.getRequirement() == null || colony.hasUpgrade(u.getRequirement())) && 
               colony.getResearchPoints() >= u.getCost()) {
                addAlert("RESEARCH", "New Research Available", AssetStyles.FONT_COLOR_HIGHLIGHT, durationDefault);
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
                addAlert("BUILD", "Can Build: " + b.getName(), AssetStyles.FONT_COLOR_VALUE, durationDefault);
                return; 
            }
        }
    }

    private void checkBodyPile() {
        int deadCount = colony.getDeadAnts().size();
        if (deadCount >= 500) {
            addAlert("DEAD", "Body Pile High: " + deadCount, AssetStyles.FONT_COLOR_ERROR, durationDefault);
        }
    }
}