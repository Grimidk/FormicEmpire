package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TriggerManager {

    private final World world;
    private final Colony playerColony;
    private final Engine engine;
    
    private final List<TriggerListener> listeners = new ArrayList<>();
    private boolean colonyDeathFired = false;

    private final Runnable monthlyRunnable = this::checkMonthlyTriggers;
    private final Runnable dailyRunnable = this::checkDailyTriggers;
    private final Runnable hourlyRunnable = this::checkHourlyTriggers;

    public TriggerManager(World world, Colony colony, Engine engine) {
        this.world = world;
        this.playerColony = colony;
        this.engine = engine;
    }

    public void registerListeners() {
        unregisterListeners();
        engine.addMonthTickListener(monthlyRunnable);
        engine.addDayTickListener(dailyRunnable);
        engine.addHourTickListener(hourlyRunnable);
    }

    public void unregisterListeners() {
        engine.removeMonthTickListener(monthlyRunnable);
        engine.removeDayTickListener(dailyRunnable);
        engine.removeHourTickListener(hourlyRunnable);
        listeners.clear();
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

    private void fireLocalizedTrigger(Upgrade upgrade, String titleKey, String messageKey) {
        fireTrigger(upgrade, LanguageStrings.get(titleKey), LanguageStrings.get(messageKey));
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
        checkParasiticMiteOutbreak();
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
             
            fireLocalizedTrigger(GameUnlocks.ABILITY_CLONING,
                LanguageStrings.TRIGGER_CLONING_TITLE,
                LanguageStrings.TRIGGER_CLONING_MSG);
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
            fireLocalizedTrigger(GameUnlocks.ROLE_RESEARCHER,
                LanguageStrings.TRIGGER_RESEARCHER_ROLE_TITLE,
                LanguageStrings.TRIGGER_RESEARCHER_ROLE_MSG);
        }
    }
    
    private void checkGraveKeeperUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_GRAVER)) return;
        
        if (playerColony.getDeadAnts().size() >= 100) { 
            fireLocalizedTrigger(GameUnlocks.ROLE_GRAVER,
                LanguageStrings.TRIGGER_GRAVER_ROLE_TITLE,
                LanguageStrings.TRIGGER_GRAVER_ROLE_MSG);
        }
    }
    
    private void checkResearchAbilityUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) return;
        
        if (playerColony.getResearchPoints() >= 100) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_RESEARCH,
                LanguageStrings.TRIGGER_RESEARCH_ABILITY_TITLE,
                LanguageStrings.TRIGGER_RESEARCH_ABILITY_MSG);
        }
    }
    
    private void checkBuildAbilityUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_BUILD)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_BUILDER)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_BUILD,
                LanguageStrings.TRIGGER_BUILD_ABILITY_TITLE,
                LanguageStrings.TRIGGER_BUILD_ABILITY_MSG);
        }
    }
    
    private void checkHunterRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            fireLocalizedTrigger(GameUnlocks.ROLE_HUNTER,
                LanguageStrings.TRIGGER_HUNTER_ROLE_TITLE,
                LanguageStrings.TRIGGER_HUNTER_ROLE_MSG);
        }
    }
    
    private void checkBreederRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            fireLocalizedTrigger(GameUnlocks.ROLE_BREEDER,
                LanguageStrings.TRIGGER_BREEDER_ROLE_TITLE,
                LanguageStrings.TRIGGER_BREEDER_ROLE_MSG);
        }
    }
    
    private void checkBruteRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_BRUTE)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
            fireLocalizedTrigger(GameUnlocks.ROLE_BRUTE,
                LanguageStrings.TRIGGER_BRUTE_ROLE_TITLE,
                LanguageStrings.TRIGGER_BRUTE_ROLE_MSG);
        }
    }
    
    private void checkSpreadAbilityUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_SPREAD)) return;
        
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_SPREAD,
                LanguageStrings.TRIGGER_SPREAD_ABILITY_TITLE,
                LanguageStrings.TRIGGER_SPREAD_ABILITY_MSG);
        }
    }
    
    private void checkScoutRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) return;
        
        if (playerColony.getLocationService() != null && playerColony.getLocationService().getDiscoveredSources() != null) {
            for (ResourceSource source : playerColony.getLocationService().getDiscoveredSources()) {
                if (source.getResourceType() == GameConstants.RESOURCE_PLANT && source.getInitialQuantity() == 10000) {
                    int collected = source.getInitialQuantity() - source.getQuantity();
                    if (collected >= 6000) {
                        fireLocalizedTrigger(GameUnlocks.ROLE_SCOUT,
                            LanguageStrings.TRIGGER_SCOUT_ROLE_TITLE,
                            LanguageStrings.TRIGGER_SCOUT_ROLE_MSG);
                    }
                    break; 
                }
            }
        }
    }

    private void checkPoliceRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_POLICE)) return;
        
        if (playerColony.getRank().getPopulation() >= 1000) {
            fireLocalizedTrigger(GameUnlocks.ROLE_POLICE,
                LanguageStrings.TRIGGER_POLICE_ROLE_TITLE,
                LanguageStrings.TRIGGER_POLICE_ROLE_MSG);
        }
    }

    private void checkParasiticMiteOutbreak() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT)) return;
        if (playerColony.getParasiticMites() <= 0) return;

        fireLocalizedTrigger(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT,
            LanguageStrings.TRIGGER_PARASITIC_MITE_TITLE,
            LanguageStrings.TRIGGER_PARASITIC_MITE_MSG);
    }

    private void checkMassFlightUnlock() {
        if (playerColony.getDynasty() == null) return;
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT)) return;

        if (playerColony.getDynasty().getTotalNuptialFlights() >= 10) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_MASS_FLIGHT,
                LanguageStrings.TRIGGER_MASS_FLIGHT_TITLE,
                LanguageStrings.TRIGGER_MASS_FLIGHT_MSG);
        }
    }
    
    private void checkDynastyTriggers() {
        if (playerColony.getDynasty() == null) return;
        
        int colonyCount = playerColony.getDynasty().getColonies().size();

        if (colonyCount >= 2 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_DYNASTY)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_DYNASTY,
                LanguageStrings.TRIGGER_DYNASTY_ABILITY_TITLE,
                LanguageStrings.TRIGGER_DYNASTY_ABILITY_MSG);
        }
        
        if (colonyCount >= 3 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_TRADE,
                LanguageStrings.TRIGGER_TRADE_ABILITY_TITLE,
                LanguageStrings.TRIGGER_TRADE_ABILITY_MSG);
        }

        if (colonyCount >= 4 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_MANAGEMENT,
                LanguageStrings.TRIGGER_MANAGEMENT_ABILITY_TITLE,
                LanguageStrings.TRIGGER_MANAGEMENT_ABILITY_MSG);
        }

        if (colonyCount >= 5 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_SPREAD_2)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_SPREAD_2,
                LanguageStrings.TRIGGER_SPREAD_2_ABILITY_TITLE,
                LanguageStrings.TRIGGER_SPREAD_2_ABILITY_MSG);
        }
        
        if (colonyCount >= 7 && !playerColony.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_AUTOMATION,
                LanguageStrings.TRIGGER_AUTOMATION_ABILITY_TITLE,
                LanguageStrings.TRIGGER_AUTOMATION_ABILITY_MSG);
        }

        if (engine.getTradeManager() != null && engine.getTradeManager().getActiveTrades().size() >= 5) {
            if (!playerColony.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE)) {
                fireLocalizedTrigger(GameUnlocks.ABILITY_BILATERAL_TRADE,
                    LanguageStrings.TRIGGER_BILATERAL_TRADE_TITLE,
                    LanguageStrings.TRIGGER_BILATERAL_TRADE_MSG);
            }
        }
    }

    private void checkTradeRoleTriggers() {
        if (!playerColony.hasUpgrade(GameUnlocks.ABILITY_TRADE)) return;

        if (!playerColony.hasUpgrade(GameUnlocks.ROLE_COURIER)) {
            fireLocalizedTrigger(GameUnlocks.ROLE_COURIER,
                LanguageStrings.TRIGGER_COURIER_ROLE_TITLE,
                LanguageStrings.TRIGGER_COURIER_ROLE_MSG);
        }
    }

    private void checkTunnelRoleUnlock() {
        if (!playerColony.hasUpgrade(GameUnlocks.ABILITY_TUNNELS)) return;

        if (!playerColony.hasUpgrade(GameUnlocks.ROLE_BORER)) {
            fireLocalizedTrigger(GameUnlocks.ROLE_BORER,
                LanguageStrings.TRIGGER_BORER_ROLE_TITLE,
                LanguageStrings.TRIGGER_BORER_ROLE_MSG);
        }
    }

    private void checkAssimilationAbilityUnlock() {
        if (playerColony.getDynasty() == null) return;
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_ASSIMILATION)) return;

        if (playerColony.getDynasty().getAbsorbedDynastyIds().size() > 0) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_ASSIMILATION,
                LanguageStrings.TRIGGER_ASSIMILATION_ABILITY_TITLE,
                LanguageStrings.TRIGGER_ASSIMILATION_ABILITY_MSG);
        }
    }

    private void checkAbilityMenuHint() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_ABILITY)) return;

        boolean hasActionAbilities = playerColony.hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT)
                || playerColony.hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT);

        if (hasActionAbilities) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_ABILITY,
                LanguageStrings.TRIGGER_OPERATIONS_ABILITY_TITLE,
                LanguageStrings.TRIGGER_OPERATIONS_ABILITY_MSG);
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