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
    private boolean colonyDeathFired = false;

    public TriggerManager(World world, Colony colony, Engine engine) {
        this.world = world;
        this.colony = colony;
        this.engine = engine;
    }

    public void registerListeners() {
        engine.addMonthTickListener(this::checkMonthlyTriggers);
        engine.addDayTickListener(this::checkDailyTriggers);
        engine.addHourTickListener(this::checkHourlyTriggers); 
    }

    public interface TriggerListener {
        void onUpgradeTriggered(Upgrade unlockedUpgrade, String title, String message);
        void onColonyDeath();
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
    
    private void fireColonyDeath() {
        for (TriggerListener listener : listeners) {
            SwingUtilities.invokeLater(() -> {
                listener.onColonyDeath();
            });
        }
    }

    // --- Trigger Check Methods ---
    private void checkMonthlyTriggers() {
        checkResearchRoleUnlock();
    }
    
    private void checkDailyTriggers() {
        checkGraveKeeperUnlock();
        checkColonyDeath();
    }

    private void checkHourlyTriggers() {
        checkResearchAbilityUnlock();
        checkBuildAbilityUnlock();
        checkHunterRoleUnlock();
        checkBreederRoleUnlock();
        checkBruteRoleUnlock();
        checkSpreadAbilityUnlock();
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
        
        if (colony.getDeadAnts().size() >= 100) { 
            fireTrigger(GameUpgrades.ROLE_GRAVER, 
                        "A Smelly Problem", 
                        "The bodies are piling up! Your workers have developed the Grave-Keeper role to clean the colony and prevent disease.");
        }
    }
    
    private void checkResearchAbilityUnlock() {
        if (colony.hasUpgrade(GameUpgrades.ABILITY_RESEARCH)) {
            return;
        }
        
        if (colony.getResearchPoints() >= 100) {
            fireTrigger(GameUpgrades.ABILITY_RESEARCH, 
                        "Scientific Breakthrough", 
                        "Your colony has accumulated 100 Research Points! You can now access the Research panel (Y) from the game menu to purchase new upgrades.");
        }
    }
    
    private void checkBuildAbilityUnlock() {
        if (colony.hasUpgrade(GameUpgrades.ABILITY_BUILD)) {
            return;
        }
        
        if (colony.hasUpgrade(GameUpgrades.ROLE_BUILDER)) {
            fireTrigger(GameUpgrades.ABILITY_BUILD, 
                        "Construction Unlocked", 
                        "Your ants have learned the basics of construction! You can now access the Build panel (U) from the game menu.");
        }
    }
    
    private void checkHunterRoleUnlock() {
        if (colony.hasUpgrade(GameUpgrades.ROLE_HUNTER)) {
            return;
        }
        
        if (colony.hasUpgrade(GameUpgrades.TYPE_SOLDIER)) {
            fireTrigger(GameUpgrades.ROLE_HUNTER,
                        "Hunter Instinct",
                        "Unlocking the Soldier ant type has automatically unlocked the 'Hunter' role for them.");
        }
    }
    
    private void checkBreederRoleUnlock() {
        if (colony.hasUpgrade(GameUpgrades.ROLE_BREEDER)) {
            return;
        }
        
        if (colony.hasUpgrade(GameUpgrades.TYPE_PRINCESS)) {
            fireTrigger(GameUpgrades.ROLE_BREEDER,
                        "Nuptial Flights",
                        "Unlocking the Princess and Drone ant types has automatically unlocked the 'Breeder' role.");
        }
    }
    
    private void checkBruteRoleUnlock() {
        if (colony.hasUpgrade(GameUpgrades.ROLE_BRUTE)) {
            return;
        }
        
        if (colony.hasUpgrade(GameUpgrades.TYPE_MAJOR)) {
            fireTrigger(GameUpgrades.ROLE_BRUTE,
                        "Heavy Trooper",
                        "Unlocking the Major ant type has automatically unlocked the 'Brute' role for them.");
        }
    }
    
    private void checkSpreadAbilityUnlock() {
        if (colony.hasUpgrade(GameUpgrades.ABILITY_SPREAD)) {
            return;
        }
        
        if (colony.hasUpgrade(GameUpgrades.ROLE_BREEDER)) {
            fireTrigger(GameUpgrades.ABILITY_SPREAD,
                        "Colony Colonization",
                        "With the ability to breed new queens, your colony now understands how to spread. You can found new colonies from the world map (I).");
        }
    }
    
    private void checkColonyDeath() {
        if (colonyDeathFired || !colony.hasUpgrade(GameUpgrades.TYPE_QUEEN)) {
            return;
        }
        
        if (colony.getQueens() != null && colony.getQueens().size() <= 0) {
            colonyDeathFired = true;
            fireColonyDeath();
        }
    }
}