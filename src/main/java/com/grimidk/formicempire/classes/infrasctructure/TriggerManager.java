package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.World;
import com.grimidk.formicempire.classes.constants.Upgrade;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class TriggerManager {

    private final World world;
    private final Colony colony;
    private final Engine engine;
    
    private final List<TriggerListener> listeners = new ArrayList<>();

    public TriggerManager(World world, Colony colony, Engine engine) {
        this.world = world;
        this.colony = colony;
        this.engine = engine;
    }

    public void registerListeners() {
        engine.addMonthTickListener(this::checkMonthlyTriggers);
        engine.addDayTickListener(this::checkDailyTriggers);
    }

    // --- Listener Interface for Popups ---
    public interface TriggerListener {
        void onUpgradeTriggered(Upgrade unlockedUpgrade, String title, String message);
    }
    
    public void addListener(TriggerListener listener) {
        listeners.add(listener);
    }
    
    private void fireTrigger(Upgrade upgrade, String title, String message) {
        colony.unlockUpgrade(upgrade);
        
        for (TriggerListener listener : listeners) {
            SwingUtilities.invokeLater(() -> {
                listener.onUpgradeTriggered(upgrade, title, message);
            });
        }
    }

    // --- Trigger Check Methods ---
    private void checkMonthlyTriggers() {
        checkResearchRoleUnlock();
    }
    
    private void checkDailyTriggers() {
        checkGraveKeeperUnlock();
    }

    // --- Specific Trigger Logic ---
    private void checkResearchRoleUnlock() {
        if (colony.hasUpgrade(GameUpgrades.ROLE_RESEARCHER)) {
            return;
        }

        boolean timeMet = world.getYear() > 0 || world.getMonth() > 1;

        if (timeMet) {
            fireTrigger(GameUpgrades.ROLE_RESEARCHER, 
                        "New Ideas", 
                        "A month has passed. Your Queen has grown wise and can now dedicate time to Research, unlocking the Researcher role!");
        }
    }
    
    private void checkGraveKeeperUnlock() {
        if (colony.hasUpgrade(GameUpgrades.ROLE_GRAVER)) {
            return;
        }
        
        if (colony.getDeadAnts().size() >= 30) {
            fireTrigger(GameUpgrades.ROLE_GRAVER, 
                        "A Smelly Problem", 
                        "The bodies are piling up! Your workers have developed the Grave-Keeper role to clean the colony and prevent disease.");
        }
    }
}