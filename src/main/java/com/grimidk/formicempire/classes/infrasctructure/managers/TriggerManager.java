package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class TriggerManager {

    private final World world;
    private final Colony playerColony;
    private final Engine engine;
    
    private final List<TriggerListener> listeners = new ArrayList<>();
    private boolean colonyDeathFired = false;

    public TriggerManager(World world, Colony colony, Engine engine) {
        this.world = world;
        this.playerColony = colony;
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
        playerColony.unlockUpgrade(upgrade);
        
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

    // --- Schedule Checks ---
    private void checkMonthlyTriggers() {
        checkResearchRoleUnlock();
        checkPoliceRoleUnlock();
    }
    
    private void checkDailyTriggers() {
        checkGraveKeeperUnlock();
        checkColonyDeath();
        checkAllNPCTriggers();
    }

    private void checkHourlyTriggers() {
        checkResearchAbilityUnlock();
        checkBuildAbilityUnlock();
        checkHunterRoleUnlock();
        checkBreederRoleUnlock();
        checkBruteRoleUnlock();
        checkSpreadAbilityUnlock();
        checkScoutRoleUnlock();
        checkCivilizationAbilityUnlock();
    }

    private void checkAllNPCTriggers() {
        if (world == null || world.getHexes() == null) return;

        for (Hex hex : world.getHexes()) {
            Colony npc = hex.getColony();
            if (npc == null || npc.isPlayer()) continue;

            checkNPCResearcher(npc);
            checkNPCGraver(npc);
            checkNPCScout(npc);
            checkNPCPolice(npc);
            checkNPCUnitRoles(npc);
            checkNPCAbilities(npc);
        }
    }

    private void checkNPCResearcher(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;
        if (npc.getAntTotal() > 20) {
            npc.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);
        }
    }

    private void checkNPCGraver(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_GRAVER)) return;
        if (npc.getDeadAnts().size() >= 20) {
            npc.unlockUpgrade(GameUnlocks.ROLE_GRAVER);
        }
    }

    private void checkNPCScout(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_SCOUT)) return;
        boolean lowFood = npc.getPlants() < (npc.getStatsService().getPlantsCapacity(npc) * 0.2);
        boolean highPop = npc.getAntTotal() > 50;
        if (lowFood || highPop) {
            npc.unlockUpgrade(GameUnlocks.ROLE_SCOUT);
        }
    }

    private void checkNPCPolice(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_POLICE)) return;
        if (npc.getRank().getPopulation() >= 1000) {
            npc.unlockUpgrade(GameUnlocks.ROLE_POLICE);
        }
    }

    private void checkNPCUnitRoles(Colony npc) {
        if (!npc.hasUpgrade(GameUnlocks.ROLE_HUNTER) && npc.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            npc.unlockUpgrade(GameUnlocks.ROLE_HUNTER);
        }
        if (!npc.hasUpgrade(GameUnlocks.ROLE_BREEDER) && npc.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            npc.unlockUpgrade(GameUnlocks.ROLE_BREEDER);
        }
        if (!npc.hasUpgrade(GameUnlocks.ROLE_BRUTE) && npc.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
            npc.unlockUpgrade(GameUnlocks.ROLE_BRUTE);
        }
    }

    private void checkNPCAbilities(Colony npc) {
        if (!npc.hasUpgrade(GameUnlocks.ABILITY_RESEARCH) && npc.getResearchPoints() >= 100) {
            npc.unlockUpgrade(GameUnlocks.ABILITY_RESEARCH);
        }
        if (!npc.hasUpgrade(GameUnlocks.ABILITY_BUILD) && npc.hasUpgrade(GameUnlocks.ROLE_BUILDER)) {
            npc.unlockUpgrade(GameUnlocks.ABILITY_BUILD);
        }
        if (!npc.hasUpgrade(GameUnlocks.ABILITY_SPREAD) && npc.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            npc.unlockUpgrade(GameUnlocks.ABILITY_SPREAD);
        }
    }

    private void checkResearchRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;

        boolean timeMet = world.getYear() > 0 || world.getMonth() > 1;
        if (timeMet) {
            fireTrigger(GameUnlocks.ROLE_RESEARCHER, 
                "New Ideas", 
                "A month has passed. Your Queen has grown wise and can now dedicate time to Research, unlocking the Researcher role!");
        }
    }
    
    private void checkGraveKeeperUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_GRAVER)) return;
        
        if (playerColony.getDeadAnts().size() >= 100) { 
            fireTrigger(GameUnlocks.ROLE_GRAVER, 
                "A Smelly Problem", 
                "The bodies are piling up! Your workers have developed the Grave-Keeper role to clean the colony and prevent disease.");
        }
    }
    
    private void checkResearchAbilityUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) return;
        
        if (playerColony.getResearchPoints() >= 100) {
            fireTrigger(GameUnlocks.ABILITY_RESEARCH, 
                "Scientific Breakthrough", 
                "Your colony has accumulated 100 Research Points! You can now access the Research panel (Y) from the game menu to purchase new upgrades.");
        }
    }
    
    private void checkBuildAbilityUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_BUILD)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_BUILDER)) {
            fireTrigger(GameUnlocks.ABILITY_BUILD, 
                "Construction Unlocked", 
                "Your ants have learned the basics of construction! You can now access the Build panel (U) from the game menu.");
        }
    }
    
    private void checkHunterRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            fireTrigger(GameUnlocks.ROLE_HUNTER,
                "Hunter Instinct",
                "Unlocking the Soldier ant type has automatically unlocked the 'Hunter' role for them.");
        }
    }
    
    private void checkBreederRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            fireTrigger(GameUnlocks.ROLE_BREEDER,
                "Nuptial Flights",
                "Unlocking the Princess and Drone ant types has automatically unlocked the 'Breeder' role.");
        }
    }
    
    private void checkBruteRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_BRUTE)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
            fireTrigger(GameUnlocks.ROLE_BRUTE,
                "Heavy Trooper",
                "Unlocking the Major ant type has automatically unlocked the 'Brute' role for them.");
        }
    }
    
    private void checkSpreadAbilityUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_SPREAD)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            fireTrigger(GameUnlocks.ABILITY_SPREAD,
                "Colony Colonization",
                "With the ability to breed new queens, your colony now understands how to spread. You can found new colonies from the world map (I).");
}
    }
    
    private void checkScoutRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) return;
        
        if (playerColony.getLocationService() != null && playerColony.getLocationService().getDiscoveredSources() != null) {
            for (ResourceSource source : playerColony.getLocationService().getDiscoveredSources()) {
                if (source.getResourceType() == GameConstants.RESOURCE_PLANT && source.getInitialQuantity() == 10000) {
                    int collected = source.getInitialQuantity() - source.getQuantity();
                    if (collected >= 6000) {
                        fireTrigger(GameUnlocks.ROLE_SCOUT, 
                            "Adventure's Call", 
                            "We have depleted more than half of our main plant source! Our workers feel the need to explore for new lands, unlocking the Scout role!");
                    }
                    break; 
                }
            }
        }
    }

    private void checkPoliceRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_POLICE)) return;
        
        if (playerColony.getRank().getPopulation() >= 1000) {
            fireTrigger(GameUnlocks.ROLE_POLICE, 
                "Parasitic Infestation", 
                "The colony has become so prosperous that parasitic ants may infiltrate it!");
        }
    }
    
    private void checkCivilizationAbilityUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_CIVILIZATION)) return;
        
        if (playerColony.getCivilization() != null && playerColony.getCivilization().getColonies().size() >= 2) {
            fireTrigger(GameUnlocks.ABILITY_CIVILIZATION,
                "Ant Civilization",
                "Your civilization grows! With a second colony established, you can now manage your entire Civilization. Press (S) to open the Civilization menu.");
        }
    }
    
    private void checkColonyDeath() {
        if (colonyDeathFired || !playerColony.hasUpgrade(GameUnlocks.TYPE_QUEEN)) {
            return;
        }
        
        if (playerColony.getQueens() != null && playerColony.getQueens().size() <= 0) {
            colonyDeathFired = true;
            fireColonyDeath();
        }
    }
}