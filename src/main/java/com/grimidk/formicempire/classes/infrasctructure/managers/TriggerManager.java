package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import javax.swing.*;
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
        checkMassFlightUnlock();
        checkCloningAbilityUnlock();
    }

    private void checkHourlyTriggers() {
        checkResearchAbilityUnlock();
        checkBuildAbilityUnlock();
        checkHunterRoleUnlock();
        checkBreederRoleUnlock();
        checkBruteRoleUnlock();
        checkSpreadAbilityUnlock();
        checkScoutRoleUnlock();
        checkDynastyTriggers();
        checkTradeRoleTriggers();
        checkTunnelRoleUnlock();
        checkAssimilationAbilityUnlock();
        checkAbilityMenuHint();
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
            checkNPCCloning(npc);
        }
    }
    
    private void checkCloningAbilityUnlock() {
        if (playerColony.getDynasty() == null) return;
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_CLONING)) return;

        if (playerColony.getDynasty().getRank().getId() >= GameConstants.RANK_ULTRA.getId()) {
            double bonus = playerColony.getDynasty().getCompletedAssimilations().size() * 5.0;
            playerColony.getDynasty().setGeneticIntegrity(playerColony.getDynasty().getGeneticIntegrity() + bonus);
             
            fireTrigger(GameUnlocks.ABILITY_CLONING,
                "Cloning Vats",
                "Your colony has reached the Ultra rank! You have unlocked Cloning, securing your genetic future.");
        }
    }
    
    private void checkNPCCloning(Colony npc) {
        if (npc.getDynasty() == null) return;
        if (npc.hasUpgrade(GameUnlocks.ABILITY_CLONING)) return;
        if (npc.getDynasty().getRank().getId() >= GameConstants.RANK_ULTRA.getId()) {
            double bonus = npc.getDynasty().getCompletedAssimilations().size() * 5.0;
            npc.getDynasty().setGeneticIntegrity(npc.getDynasty().getGeneticIntegrity() + bonus);
            npc.unlockUpgrade(GameUnlocks.ABILITY_CLONING);
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
                "The colony has become so prosperous that parasitic bugs may infiltrate it!");
        }
    }

    private void checkMassFlightUnlock() {
        if (playerColony.getDynasty() == null) return;
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT)) return;

        if (playerColony.getDynasty().getTotalNuptialFlights() >= 10) {
            fireTrigger(GameUnlocks.ABILITY_MASS_FLIGHT,
                "Imperial Decree",
                "Your dynasty has performed 10 nuptial flights! You have unlocked the 'Mass Nuptial Flights' ability in the Colony Operations menu (Z).");
        }
    }
    
    private void checkDynastyTriggers() {
        if (playerColony.getDynasty() == null) return;
        
        int colonyCount = playerColony.getDynasty().getColonies().size();

        if (colonyCount >= 2 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_DYNASTY)) {
            fireTrigger(GameUnlocks.ABILITY_DYNASTY,
                "Ant Dynasty",
                "Your dynasty grows! With a second colony established, you can now manage your entire Dynasty. Press (S) to open the Dynasty menu.");
        }
        
        if (colonyCount >= 3 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            fireTrigger(GameUnlocks.ABILITY_TRADE,
                "Trade Networks",
                "With three colonies, your ants have learned to transport resources efficiently between nests. Trade Routes unlocked!");
        }

        if (colonyCount >= 4 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT)) {
            fireTrigger(GameUnlocks.ABILITY_MANAGEMENT,
                "Middle Management",
                "Your dynasty has so many colonies that you need help managing them! You can now let your colonies build by themselves.");
        }

        if (colonyCount >= 5 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_SPREAD_2)) {
            fireTrigger(GameUnlocks.ABILITY_SPREAD_2,
                "Mass Colonization",
                "Your dynasty is expanding rapidly! The limit on new colonies has been removed.");
        }
        
        if (colonyCount >= 7 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION)) {
            fireTrigger(GameUnlocks.ABILITY_AUTOMATION,
                "Automation Era",
                "Your dynasty is vast. You can now completely automate colony management.");
        }

        if (engine.getTradeManager() != null && engine.getTradeManager().getActiveTrades().size() >= 5) {
            if (!playerColony.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE)) {
                fireTrigger(GameUnlocks.ABILITY_BILATERAL_TRADE,
                    "Two-Way Logistics",
                    "Your trade network is so busy that your ants have learned to bring resources back on their return trips! Bilateral Trade unlocked.");
            }
        }
    }

    private void checkTradeRoleTriggers() {
        if (!playerColony.hasUpgrade(GameUnlocks.ABILITY_TRADE)) return;

        if (!playerColony.hasUpgrade(GameUnlocks.ROLE_COURIER)) {
            fireTrigger(GameUnlocks.ROLE_COURIER, "Logistic Network", "Trade routes require couriers! Workers can now be assigned to transport goods.");
        }
    }

    private void checkTunnelRoleUnlock() {
        if (!playerColony.hasUpgrade(GameUnlocks.ABILITY_TUNNELS)) return;

        if (!playerColony.hasUpgrade(GameUnlocks.ROLE_BORER)) {
            fireTrigger(GameUnlocks.ROLE_BORER, "Boring Job", "Trade routes can be dangerous! Majors can now be assigned to dig tunnels for faster, safer trade routes.");
        }
    }

    private void checkAssimilationAbilityUnlock() {
        if (playerColony.getDynasty() == null) return;
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_ASSIMILATION)) return;

        if (playerColony.getDynasty().getAbsorbedDynastyIds().size() > 0) {
            fireTrigger(GameUnlocks.ABILITY_ASSIMILATION,
                "Genetic Assimilation",
                "By absorbing the remnants of a defeated dynasty, your ants have learned that genetic traits can be harvested! Genetic Assimilation unlocked in the Upgrades menu (Y).");
        }
    }

    private void checkAbilityMenuHint() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_ABILITY)) return;
        
        boolean hasActionAbilities = playerColony.hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT);
        
        if (hasActionAbilities) {
            fireTrigger(GameUnlocks.ABILITY_ABILITY, 
                "Colony Operations", 
                "You have gained a special active ability! You can now access the Colony Operations menu by pressing (Z).");
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