package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.constants.Building;
import com.grimidk.formicempire.classes.constants.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.interfaces.game.AlertPanel;
import com.grimidk.formicempire.classes.interfaces.game.AlertPanel.Alert;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;

public class AlertManager {
    private final Colony colony;
    private final AlertPanel panel;
    private final List<Alert> activeAlerts = new ArrayList<>();
    
    private final Color COL_RED = new Color(180, 0, 0);
    private final Color COL_GREEN = new Color(0, 120, 0);
    private final Color COL_ORANGE = new Color(200, 100, 0);
    private final Color COL_BLUE = new Color(0, 0, 180);

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
                addAlert("CRIT", msg.replace("CRITICAL: ", ""), COL_RED, 30000);
            } else if (msg.startsWith("SUCCESS")) {
                addAlert("SUCC", msg.replace("SUCCESS: ", ""), COL_GREEN, 15000);
            } else if (msg.startsWith("WARNING")) {
                addAlert("WARN", msg.replace("WARNING: ", ""), COL_ORANGE, 20000);
            } else {
                addAlert("INFO", msg, Color.BLACK, 10000);
            }
        }

        checkResourceWarning();
        checkAvailableResearch();
        checkAvailableBuildings();

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
             addAlert("STARVE", "Starvation Risk", COL_RED, 5000);
        }
    }

    private void checkUnassignedAnts() {
    }
    
    private void checkAvailableResearch() {
        if (!colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) return;
        
        int points = colony.getResearchPoints();
        
        for (Upgrade u : GameUnlocks.getUpgrades()) {
            if (!colony.hasUpgrade(u)) {
                if (points >= u.getCost()) {
                     addAlert("RESEARCH", "Research Available", COL_BLUE, 5000);
                     return;
                }
            }
        }
    }
    
    private void checkAvailableBuildings() {
        if (!colony.hasUpgrade(GameUnlocks.ABILITY_BUILD)) return;
        if (colony.getCurrentBuildingProject() != null) return; 

        for (Building b : GameUnlocks.getBuildings()) {
            if (!colony.hasBuilding(b)) {
                if (colony.getMinerals() >= b.getMineralCost() && colony.getResins() >= b.getResinCost()) {
                     addAlert("BUILD", "Can Build: " + b.getName(), COL_GREEN, 5000);
                     return; 
                }
            }
        }
    }
}