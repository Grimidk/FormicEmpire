package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.dynasty.Rank;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TriggerManager {

    private final World world;
    private final Colony playerColony;
    private final Engine engine;
    
    private final List<TriggerListener> listeners = new ArrayList<>();
    private boolean colonyDeathFired = false;
    private int listenerGeneration = 0;

    private final Runnable monthlyRunnable = this::checkMonthlyTriggers;
    private final Runnable dailyRunnable = this::checkDailyTriggers;
    private final Runnable hourlyRunnable = this::checkHourlyTriggers;

    public TriggerManager(World world, Colony colony, Engine engine) {
        this.world = world;
        this.playerColony = colony;
        this.engine = engine;
    }

    public void registerListeners() {
        unregisterTickListeners();
        engine.addMonthTickListener(monthlyRunnable);
        engine.addDayTickListener(dailyRunnable);
        engine.addHourTickListener(hourlyRunnable);
    }

    public void unregisterListeners() {
        unregisterTickListeners();
        listeners.clear();
        listenerGeneration++;
    }

    private void unregisterTickListeners() {
        engine.removeMonthTickListener(monthlyRunnable);
        engine.removeDayTickListener(dailyRunnable);
        engine.removeHourTickListener(hourlyRunnable);
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

        int generation = listenerGeneration;
        List<TriggerListener> snapshot = new ArrayList<>(listeners);
        for (TriggerListener listener : snapshot) {
            SwingUtilities.invokeLater(() -> {
                if (generation != listenerGeneration) {
                    return;
                }
                listener.onUpgradeTriggered(upgrade, title, message);
            });
        }
    }

    private void fireLocalizedTrigger(Upgrade upgrade, String titleKey, String messageKey) {
        fireTrigger(upgrade, LanguageStrings.get(titleKey), LanguageStrings.get(messageKey));
    }

    private void fireLocalizedTrigger(Upgrade upgrade, String titleKey, String messageKey,
            Object... messageArgs) {
        fireTrigger(upgrade, LanguageStrings.get(titleKey), LanguageStrings.format(messageKey, messageArgs));
    }

    private void fireInfoPopup(String titleKey, String messageKey) {
        String title = LanguageStrings.get(titleKey);
        String message = LanguageStrings.get(messageKey);
        int generation = listenerGeneration;
        List<TriggerListener> snapshot = new ArrayList<>(listeners);
        for (TriggerListener listener : snapshot) {
            SwingUtilities.invokeLater(() -> {
                if (generation != listenerGeneration) {
                    return;
                }
                listener.onUpgradeTriggered(null, title, message);
            });
        }
    }

    private void fireSynergyUnlocked(Synergy synergy) {
        if (synergy == null) {
            return;
        }
        String title = LanguageStrings.get(synergy.getTriggerTitleKey());
        String message = LanguageStrings.format(
                synergy.getTriggerMessageKey(),
                synergy.getName(),
                synergy.formatRequirementFlavorNames());
        int generation = listenerGeneration;
        List<TriggerListener> snapshot = new ArrayList<>(listeners);
        for (TriggerListener listener : snapshot) {
            SwingUtilities.invokeLater(() -> {
                if (generation != listenerGeneration) {
                    return;
                }
                listener.onUpgradeTriggered(synergy.getReward(), title, message);
            });
        }
    }
    
    private void fireColonyDeath() {
        int generation = listenerGeneration;
        List<TriggerListener> snapshot = new ArrayList<>(listeners);
        for (TriggerListener listener : snapshot) {
            SwingUtilities.invokeLater(() -> {
                if (generation != listenerGeneration) {
                    return;
                }
                listener.onColonyDeath();
            });
        }
    }

    // --- Schedule Checks ---
    private void checkMonthlyTriggers() {
        checkResearchRoleUnlock();
        checkParasiticMiteOutbreak();
    }
    
    private void checkDailyTriggers() {
        checkGraveKeeperUnlock();
        checkColonyDeath();
        checkAllNPCTriggers();
        checkMassFlightUnlock();
        checkDynastyRankPopups();
        checkAutoTunnelsUnlock();
        checkAutoDiplomacyUnlock();
        checkAutoLogisticsUnlock();
    }

    private void checkHourlyTriggers() {
        checkResearchAbilityUnlock();
        checkBuildAbilityUnlock();
        checkHunterRoleUnlock();
        checkMilitiaRoleRetrofit();
        checkBreederRoleUnlock();
        checkBruteRoleUnlock();
        checkCommanderRoleUnlock();
        checkSpreadAbilityUnlock();
        checkScoutRoleUnlock();
        checkMinerRoleUnlock();
        checkDynastyTriggers();
        checkTradeRoleTriggers();
        checkTunnelRoleUnlock();
        checkAssimilationAbilityUnlock();
        checkSubtypeHatchUnlock();
        checkSynergyUnlocks();
        checkAbilityMenuHint();
    }

    private void checkSynergyUnlocks() {
        Dynasty dynasty = playerColony.getDynasty();
        if (dynasty == null) {
            return;
        }
        for (Synergy synergy : dynasty.drainPendingSynergyAlerts()) {
            fireSynergyUnlocked(synergy);
        }
    }

    private void checkAllNPCTriggers() {
        if (world == null || world.getHexes() == null) return;

        for (Hex hex : world.getHexes()) {
            Colony npc = hex.getColony();
            if (npc == null || npc.isPlayer()) continue;

            checkNPCResearcher(npc);
            checkNPCGraver(npc);
            checkNPCScout(npc);
            checkNPCMiner(npc);
            checkNPCPolice(npc);
            checkNPCAirSupport(npc);
            checkNPCUnitRoles(npc);
            checkNPCAbilities(npc);
            checkNPCCloning(npc);
            checkNPCParasiticMites(npc);
        }
    }
    
    private void checkNPCCloning(Colony npc) {
        applyCloningUnlockIfEligible(npc);
    }

    private void checkNPCParasiticMites(Colony npc) {
        applyParasiticMiteUnlockIfEligible(npc, false);
    }

    private void applyCloningUnlockIfEligible(Colony colony) {
        if (colony.getDynasty() == null || colony.hasUpgrade(GameUnlocks.ABILITY_CLONING)) {
            return;
        }
        Rank required = GameConstants.RANK_EMPIRE;
        if (colony.getDynasty().getRank() != null && colony.getDynasty().getRank().meetsOrExceeds(required)) {
            colony.unlockUpgrade(GameUnlocks.ABILITY_CLONING);
        }
    }

    private void checkDynastyRankPopups() {
        Dynasty dynasty = playerColony.getDynasty();
        if (dynasty == null || !dynasty.isPlayer() || dynasty.getRank() == null) {
            return;
        }
        Rank current = dynasty.getRank();
        for (Rank rank : GameConstants.getColonyRanks()) {
            if (rank.getId() <= GameConstants.RANK_COLONY.getId()) {
                continue;
            }
            if (rank.getId() > current.getId()) {
                break;
            }

            Upgrade unlock = rank.getUnlockOnAnnounce();
            boolean needsUnlock = unlock != null && !playerColony.hasUpgrade(unlock);
            boolean needsAnnounce = !dynasty.hasAnnouncedRank(rank);
            if (!needsAnnounce && !needsUnlock) {
                continue;
            }
            if (!rank.hasTriggerPopup()) {
                if (needsUnlock) {
                    playerColony.unlockUpgrade(unlock);
                }
                dynasty.markRankAnnounced(rank);
                continue;
            }

            dynasty.markRankAnnounced(rank);
            if (needsUnlock) {
                fireLocalizedTrigger(unlock, rank.getTriggerTitleKey(), rank.getTriggerMessageKey());
            } else {
                fireInfoPopup(rank.getTriggerTitleKey(), rank.getTriggerMessageKey());
            }
        }
    }

    private void checkNPCResearcher(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;
        if (npc.getAntTotal() > GameNumbers.TRIGGER_NPC_RESEARCHER_MIN_ANTS) {
            npc.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);
        }
    }

    private void checkNPCGraver(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_GRAVER)) return;
        if (npc.getDeadAnts().size() >= GameNumbers.TRIGGER_NPC_GRAVER_DEAD_ANTS) {
            npc.unlockUpgrade(GameUnlocks.ROLE_GRAVER);
        }
    }

    private void checkNPCScout(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_SCOUT)) return;
        boolean lowFood = npc.getPlants() < (npc.getStatsService().getPlantsCapacity(npc) * GameNumbers.TRIGGER_NPC_SCOUT_FOOD_RATIO);
        boolean highPop = npc.getAntTotal() > GameNumbers.TRIGGER_NPC_SCOUT_MIN_ANTS;
        if (lowFood || highPop) {
            npc.unlockUpgrade(GameUnlocks.ROLE_SCOUT);
        }
    }

    private void checkNPCMiner(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_MINER)) return;
        if (!npc.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) return;
        Dynasty dynasty = npc.getDynasty();
        if (GameUnlocks.countDynastyBuildingsOfTier(dynasty, GameConstants.TIER_3)
                >= GameNumbers.TRIGGER_MINER_TIER3_BUILDINGS) {
            npc.unlockUpgrade(GameUnlocks.ROLE_MINER);
        }
    }

    private void checkNPCPolice(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_POLICE)) return;
        Rank rank = npc.getRank();
        if (rank != null && rank.meetsOrExceeds(GameConstants.RANK_DUCHY)) {
            npc.unlockUpgrade(GameUnlocks.ROLE_POLICE);
        }
    }

    private void checkNPCAirSupport(Colony npc) {
        if (npc.hasUpgrade(GameUnlocks.ROLE_AIR_SUPPORT)) {
            return;
        }
        Dynasty dynasty = npc.getDynasty();
        Rank rank = dynasty != null ? dynasty.getRank() : npc.getRank();
        if (rank != null && rank.meetsOrExceeds(GameConstants.RANK_KINGDOM)) {
            npc.unlockUpgrade(GameUnlocks.ROLE_AIR_SUPPORT);
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
        if (!npc.hasUpgrade(GameUnlocks.ABILITY_RESEARCH) && npc.getResearchPoints() >= GameNumbers.TRIGGER_RESEARCH_MIN_RP) {
            npc.unlockUpgrade(GameUnlocks.ABILITY_RESEARCH);
        }
        if (!npc.hasUpgrade(GameUnlocks.ABILITY_BUILD) && npc.hasUpgrade(GameUnlocks.ROLE_BUILDER)) {
            npc.unlockUpgrade(GameUnlocks.ABILITY_BUILD);
        }
        if (!npc.hasUpgrade(GameUnlocks.ABILITY_SPREAD) && npc.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            npc.unlockUpgrade(GameUnlocks.ABILITY_SPREAD);
        }
        if (npc.getDynasty() != null) {
            int colonies = npc.getDynasty().getColonies().size();
            if (!npc.hasUpgrade(GameUnlocks.ABILITY_DYNASTY) && colonies >= GameNumbers.TRIGGER_DYNASTY_MIN_COLONIES) {
                npc.unlockUpgrade(GameUnlocks.ABILITY_DYNASTY);
            }
            if (!npc.hasUpgrade(GameUnlocks.ABILITY_TRADE) && colonies >= GameNumbers.TRIGGER_TRADE_MIN_COLONIES) {
                npc.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
            }
            if (!npc.hasUpgrade(GameUnlocks.ABILITY_ABILITY) && npc.getResearchPoints() >= GameNumbers.TRIGGER_NPC_ABILITY_MENU_MIN_RP) {
                npc.unlockUpgrade(GameUnlocks.ABILITY_ABILITY);
            }
        }
    }

    private void checkResearchRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;

        boolean timeMet = (world.getYear() * 12 + world.getMonth()) >= GameNumbers.TRIGGER_RESEARCHER_MIN_MONTHS;
        if (timeMet) {
            fireLocalizedTrigger(GameUnlocks.ROLE_RESEARCHER,
                LanguageStrings.TRIGGER_RESEARCHER_ROLE_TITLE,
                LanguageStrings.TRIGGER_RESEARCHER_ROLE_MSG);
        }
    }
    
    private void checkGraveKeeperUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_GRAVER)) return;
        
        if (playerColony.getDeadAnts().size() >= GameNumbers.TRIGGER_GRAVER_DEAD_ANTS) { 
            fireLocalizedTrigger(GameUnlocks.ROLE_GRAVER,
                LanguageStrings.TRIGGER_GRAVER_ROLE_TITLE,
                LanguageStrings.TRIGGER_GRAVER_ROLE_MSG);
        }
    }
    
    private void checkResearchAbilityUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) return;
        
        if (playerColony.getResearchPoints() >= GameNumbers.TRIGGER_RESEARCH_MIN_RP) {
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

    private void checkMilitiaRoleRetrofit() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_MILITIA)) {
            return;
        }
        if (playerColony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            playerColony.unlockUpgrade(GameUnlocks.ROLE_MILITIA);
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

    private void checkCommanderRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_COMMANDER)) {
            return;
        }
        Dynasty dynasty = playerColony.getDynasty();
        if (dynasty == null || world == null || world.getWarService() == null) {
            return;
        }
        if (dynasty.getStatService() == null || !dynasty.getStatService().hasMultiQueenColony(dynasty)) {
            return;
        }
        int wars = world.getWarService().countWarsForDynasty(dynasty.getId());
        if (wars >= GameNumbers.TRIGGER_COMMANDER_MIN_WARS) {
            fireLocalizedTrigger(GameUnlocks.ROLE_COMMANDER,
                    LanguageStrings.TRIGGER_COMMANDER_ROLE_TITLE,
                    LanguageStrings.TRIGGER_COMMANDER_ROLE_MSG);
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
        
        if (plantHarvestProgress(playerColony) >= GameNumbers.TRIGGER_SCOUT_PLANT_COLLECTED) {
            fireLocalizedTrigger(GameUnlocks.ROLE_SCOUT,
                LanguageStrings.TRIGGER_SCOUT_ROLE_TITLE,
                LanguageStrings.TRIGGER_SCOUT_ROLE_MSG);
        }
    }

    private void checkMinerRoleUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ROLE_MINER)) return;
        if (!playerColony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) return;

        Dynasty dynasty = playerColony.getDynasty();
        if (GameUnlocks.countDynastyBuildingsOfTier(dynasty, GameConstants.TIER_3)
                >= GameNumbers.TRIGGER_MINER_TIER3_BUILDINGS) {
            fireLocalizedTrigger(GameUnlocks.ROLE_MINER,
                LanguageStrings.TRIGGER_MINER_ROLE_TITLE,
                LanguageStrings.TRIGGER_MINER_ROLE_MSG);
        }
    }

    private static int plantHarvestProgress(Colony colony) {
        if (colony.getLocationService() == null || colony.getLocationService().getDiscoveredSources() == null) {
            return 0;
        }
        int best = 0;
        for (ResourceSource source : colony.getLocationService().getDiscoveredSources()) {
            if (source.getResourceType() != GameConstants.RESOURCE_PLANT) {
                continue;
            }
            int collected = Math.max(0, source.getInitialQuantity() - source.getQuantity());
            if (collected > best) {
                best = collected;
            }
        }
        return best;
    }

    private void checkParasiticMiteOutbreak() {
        applyParasiticMiteUnlockIfEligible(playerColony, true);
    }

    private void applyParasiticMiteUnlockIfEligible(Colony colony, boolean notifyPlayer) {
        if (colony == null || colony.getDynasty() == null) {
            return;
        }
        boolean needsAlert = !colony.hasUpgrade(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT);
        boolean needsSymbioticMiteCatch = !colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
        if (!needsAlert && !needsSymbioticMiteCatch) {
            return;
        }
        if (!dynastyHasAnyParasiticMites(colony.getDynasty())) {
            return;
        }

        if (notifyPlayer && needsAlert) {
            colony.unlockUpgrade(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT);
            if (needsSymbioticMiteCatch) {
                colony.unlockUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
            }
            fireLocalizedTrigger(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT,
                    LanguageStrings.TRIGGER_PARASITIC_MITE_TITLE,
                    LanguageStrings.TRIGGER_PARASITIC_MITE_MSG);
            return;
        }

        if (needsAlert) {
            colony.unlockUpgrade(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT);
        }
        if (needsSymbioticMiteCatch) {
            colony.unlockUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
        }
        if (!notifyPlayer
                && colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)
                && !colony.hasUpgrade(GameUnlocks.ROLE_CATCHER)) {
            colony.unlockUpgrade(GameUnlocks.ROLE_CATCHER);
        }
    }

    private static boolean dynastyHasAnyParasiticMites(Dynasty dynasty) {
        if (dynasty == null || dynasty.getColonies() == null) {
            return false;
        }
        for (Colony member : dynasty.getColonies()) {
            if (member != null && member.getParasiticMites() > 0) {
                return true;
            }
        }
        return false;
    }

    private void checkMassFlightUnlock() {
        if (playerColony.getDynasty() == null) return;
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT)) return;

        if (playerColony.getDynasty().getTotalNuptialFlights() >= GameNumbers.TRIGGER_MASS_FLIGHT_MIN_NUPTIALS) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_MASS_FLIGHT,
                LanguageStrings.TRIGGER_MASS_FLIGHT_TITLE,
                LanguageStrings.TRIGGER_MASS_FLIGHT_MSG);
        }
    }

    private void checkAutoTunnelsUnlock() {
        if (playerColony.getDynasty() == null) return;
        if (!playerColony.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION)) return;
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_AUTO_TUNNELS)) return;

        Dynasty dynasty = playerColony.getDynasty();
        if (!dynasty.meetsAutoTunnelsPrerequisites()) return;

        fireLocalizedTrigger(GameUnlocks.ABILITY_AUTO_TUNNELS,
                LanguageStrings.TRIGGER_AUTO_TUNNELS_TITLE,
                LanguageStrings.TRIGGER_AUTO_TUNNELS_MSG,
                GameNumbers.AUTO_UPGRADE_MIN_COMPLETE_TUNNELS);
    }

    private void checkAutoDiplomacyUnlock() {
        if (playerColony.getDynasty() == null) return;
        if (!playerColony.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION)) return;
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_AUTO_DIPLOMACY)) return;

        Dynasty dynasty = playerColony.getDynasty();
        if (!dynasty.meetsAutoDiplomacyPrerequisites()) return;

        fireLocalizedTrigger(GameUnlocks.ABILITY_AUTO_DIPLOMACY,
                LanguageStrings.TRIGGER_AUTO_DIPLOMACY_TITLE,
                LanguageStrings.TRIGGER_AUTO_DIPLOMACY_MSG,
                GameNumbers.AUTO_UPGRADE_MIN_DIPLOMATS_SENT);
    }

    private void checkAutoLogisticsUnlock() {
        if (playerColony.getDynasty() == null) return;
        if (!playerColony.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION)) return;
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_AUTO_LOGISTICS)) return;

        Dynasty dynasty = playerColony.getDynasty();
        dynasty.bindTradeManager(engine.getTradeManager());
        if (!dynasty.meetsAutoLogisticsPrerequisites()) return;

        fireLocalizedTrigger(GameUnlocks.ABILITY_AUTO_LOGISTICS,
                LanguageStrings.TRIGGER_AUTO_LOGISTICS_TITLE,
                LanguageStrings.TRIGGER_AUTO_LOGISTICS_MSG,
                GameNumbers.AUTO_UPGRADE_MIN_RECURRENT_ROUTES);
    }
    
    private void checkDynastyTriggers() {
        if (playerColony.getDynasty() == null) return;
        
        int colonyCount = playerColony.getDynasty().getColonies().size();

        if (colonyCount >= GameNumbers.TRIGGER_DYNASTY_MIN_COLONIES && !playerColony.hasUpgrade(GameUnlocks.ABILITY_DYNASTY)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_DYNASTY,
                LanguageStrings.TRIGGER_DYNASTY_ABILITY_TITLE,
                LanguageStrings.TRIGGER_DYNASTY_ABILITY_MSG);
        }
        
        if (colonyCount >= GameNumbers.TRIGGER_TRADE_MIN_COLONIES && !playerColony.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_TRADE,
                LanguageStrings.TRIGGER_TRADE_ABILITY_TITLE,
                LanguageStrings.TRIGGER_TRADE_ABILITY_MSG);
        }

        if (colonyCount >= GameNumbers.TRIGGER_MANAGEMENT_MIN_COLONIES && !playerColony.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_MANAGEMENT,
                LanguageStrings.TRIGGER_MANAGEMENT_ABILITY_TITLE,
                LanguageStrings.TRIGGER_MANAGEMENT_ABILITY_MSG);
        }

        if (colonyCount >= GameNumbers.TRIGGER_SPREAD_2_MIN_COLONIES && !playerColony.hasUpgrade(GameUnlocks.ABILITY_SPREAD_2)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_SPREAD_2,
                LanguageStrings.TRIGGER_SPREAD_2_ABILITY_TITLE,
                LanguageStrings.TRIGGER_SPREAD_2_ABILITY_MSG);
        }
        
        if (colonyCount >= GameNumbers.TRIGGER_AUTOMATION_MIN_COLONIES && !playerColony.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION)) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_AUTOMATION,
                LanguageStrings.TRIGGER_AUTOMATION_ABILITY_TITLE,
                LanguageStrings.TRIGGER_AUTOMATION_ABILITY_MSG);
        }

        if (engine.getTradeManager() != null
                && engine.getTradeManager().getActiveTrades().size() >= GameNumbers.TRIGGER_BILATERAL_MIN_TRADES) {
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

        if (playerColony.getDynasty().getAbsorbedDynastyIds().size() >= GameNumbers.TRIGGER_ASSIMILATION_MIN_ABSORBED) {
            fireLocalizedTrigger(GameUnlocks.ABILITY_ASSIMILATION,
                LanguageStrings.TRIGGER_ASSIMILATION_ABILITY_TITLE,
                LanguageStrings.TRIGGER_ASSIMILATION_ABILITY_MSG);
        }
    }

    private void checkSubtypeHatchUnlock() {
        if (playerColony.hasUpgrade(GameUnlocks.ABILITY_SUBTYPE_HATCH)) {
            return;
        }
        if (!AntSubtypeService.hasSubtypeAssimilation(playerColony)) {
            return;
        }
        fireLocalizedTrigger(GameUnlocks.ABILITY_SUBTYPE_HATCH,
                LanguageStrings.TRIGGER_SUBTYPE_HATCH_TITLE,
                LanguageStrings.TRIGGER_SUBTYPE_HATCH_MSG);
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