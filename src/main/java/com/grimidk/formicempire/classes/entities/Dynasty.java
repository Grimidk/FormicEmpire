package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.Color;

import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.misc.GeneticIntegrityModifier;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyAiService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyAutomationService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyLogisticsAutomationService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyStarterService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyStatService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyTradeService;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class Dynasty {

    private final int id;
    private String name;
    private boolean isPlayer;
    private Species species;
    private int researchPoints;
    private int totalNuptialFlights;
    private ColonyRank rank;
    private Color color;
    
    // --- State Flags ---
    private boolean isDefeated; 
    
    // Global Data
    private boolean defaultAutomationEnabled;
    private boolean defaultAutoBuildEnabled;
    private final Set<Upgrade> unlockedUpgrades;
    private final List<Colony> colonies;
    private final List<Tunnel> tunnels;
    private final Map<String, Integer> globalDeathStatistics;
    private final List<Integer> absorbedDynastyIds;
    private final List<Integer> defeatedSpeciesIds;
    private final Set<Assimilation> completedAssimilations;
    private Assimilation currentAssimilation;
    private double assimilationProgress;
    private Colony capital;
    private double geneticIntegrity;
    private int militaryPower;
    private final Map<Integer, Integer> diplomaticReputations;
    private final Map<Integer, String> diplomaticModifierKeys;
    private final List<Integer> crossDynastyTradeRepGrantedIds;
    private final Set<Integer> pendingPactRequestFromIds;
    private final List<CrossDynastyTradeProposal> pendingTradeProposals;
    private int forcedFlightCooldownDays;

    // Services
    private transient DynastyAutomationService automationService;
    private transient DynastyAiService aiService;
    private transient DynastyLogisticsAutomationService logisticsAutomationService;
    private transient DynastyStarterService starterService;
    private transient DynastyStatService statService;
    private transient DynastyTradeService tradeService;
    private transient DynastyDiplomacyService diplomacyService;

    public Dynasty(int id, String name, boolean isPlayer, Species species) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.species = species;
        this.colonies = new ArrayList<>();
        this.unlockedUpgrades = new HashSet<>();
        this.tunnels = new ArrayList<>();
        this.globalDeathStatistics = new ConcurrentHashMap<>();
        this.absorbedDynastyIds = new ArrayList<>();
        this.defeatedSpeciesIds = new ArrayList<>();
        this.completedAssimilations = new HashSet<>();
        this.researchPoints = 0;
        this.totalNuptialFlights = 0;
        this.rank = GameConstants.RANK_ANT;
        this.isDefeated = false;
        this.currentAssimilation = null;
        this.assimilationProgress = 0;
        this.defaultAutomationEnabled = false;
        this.defaultAutoBuildEnabled = false;
        this.geneticIntegrity = 100.0;
        this.diplomaticReputations = new HashMap<>();
        this.diplomaticModifierKeys = new HashMap<>();
        this.crossDynastyTradeRepGrantedIds = new ArrayList<>();
        this.pendingPactRequestFromIds = new LinkedHashSet<>();
        this.pendingTradeProposals = new ArrayList<>();
        this.forcedFlightCooldownDays = 0;
        
        initializeColor();
        initializeServices();
    }
    
    public Dynasty(Savefile.SavedDynasty savedDynasty) {
        this.id = savedDynasty.id;
        this.name = savedDynasty.name;
        this.isPlayer = savedDynasty.isPlayer;
        this.researchPoints = savedDynasty.researchPoints;
        this.totalNuptialFlights = savedDynasty.totalNuptialFlights;
        this.isDefeated = savedDynasty.isDefeated;
        this.assimilationProgress = savedDynasty.assimilationProgress;
        this.defaultAutomationEnabled = savedDynasty.defaultAutomationEnabled;
        this.defaultAutoBuildEnabled = savedDynasty.defaultAutoBuildEnabled;
        this.geneticIntegrity = savedDynasty.geneticIntegrity;
        this.militaryPower = savedDynasty.militaryPower;
        
        this.species = GameConstants.SPECIES_OMNI; 
        for(Species s : GameConstants.getSpecies()) {
            if (s.getId() == savedDynasty.speciesId) {
                this.species = s;
                break;
            }
        }

        this.colonies = new ArrayList<>();
        this.unlockedUpgrades = new HashSet<>();
        this.tunnels = new ArrayList<>();
        this.globalDeathStatistics = new ConcurrentHashMap<>();
        this.completedAssimilations = new HashSet<>();
        this.diplomaticReputations = new HashMap<>();
        this.diplomaticModifierKeys = new HashMap<>();
        this.crossDynastyTradeRepGrantedIds = new ArrayList<>();
        this.pendingPactRequestFromIds = new LinkedHashSet<>();
        this.pendingTradeProposals = new ArrayList<>();
        this.forcedFlightCooldownDays = 0;
        
        this.absorbedDynastyIds = new ArrayList<>();
        if (savedDynasty.absorbedDynastyIds != null) {
            this.absorbedDynastyIds.addAll(savedDynasty.absorbedDynastyIds);
        }

        this.defeatedSpeciesIds = new ArrayList<>();
        if (savedDynasty.defeatedSpeciesIds != null) {
            this.defeatedSpeciesIds.addAll(savedDynasty.defeatedSpeciesIds);
        }

        this.currentAssimilation = null;
        if (savedDynasty.currentAssimilationId != -1) {
            for (Assimilation a : GameUnlocks.getAssimilations()) {
                if (a.getId() == savedDynasty.currentAssimilationId) {
                    this.currentAssimilation = a;
                    break;
                }
            }
        }

        if (savedDynasty.completedAssimilationIds != null) {
            for (Integer assId : savedDynasty.completedAssimilationIds) {
                for (Assimilation a : GameUnlocks.getAssimilations()) {
                    if (a.getId() == assId) {
                        this.completedAssimilations.add(a);
                        break;
                    }
                }
            }
        }
        
        if (savedDynasty.deathStatistics != null) {
            Map<String, Integer> migrated = DeathCause.migrateStatistics(savedDynasty.deathStatistics);
            if (migrated != null) {
                this.globalDeathStatistics.putAll(migrated);
            }
        }

        if (savedDynasty.diplomaticReputations != null) {
            for (Map.Entry<String, Integer> entry : savedDynasty.diplomaticReputations.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        diplomaticReputations.put(otherDynastyId,
                                GameConstants.clampDiplomaticReputation(entry.getValue()));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (savedDynasty.diplomaticModifierKeys != null) {
            for (Map.Entry<String, String> entry : savedDynasty.diplomaticModifierKeys.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null
                            && GameConstants.getDiplomaticReputationModifierByKey(entry.getValue()) != null) {
                        diplomaticModifierKeys.put(otherDynastyId, entry.getValue());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (savedDynasty.crossDynastyTradeRepGrantedIds != null) {
            this.crossDynastyTradeRepGrantedIds.addAll(savedDynasty.crossDynastyTradeRepGrantedIds);
        }

        if (savedDynasty.pendingPactRequestFromIds != null) {
            this.pendingPactRequestFromIds.addAll(savedDynasty.pendingPactRequestFromIds);
        }
        if (savedDynasty.pendingTradeProposals != null) {
            for (Savefile.SavedCrossDynastyTradeProposal saved : savedDynasty.pendingTradeProposals) {
                CrossDynastyTradeProposal.Kind kind = saved.request
                        ? CrossDynastyTradeProposal.Kind.REQUEST
                        : CrossDynastyTradeProposal.Kind.OFFER;
                Map<Integer, Double> load = new HashMap<>();
                if (saved.loadByResourceId != null) {
                    for (Map.Entry<String, Double> entry : saved.loadByResourceId.entrySet()) {
                        try {
                            load.put(Integer.parseInt(entry.getKey()), entry.getValue());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                this.pendingTradeProposals.add(new CrossDynastyTradeProposal(
                        saved.fromDynastyId,
                        saved.originColonyId,
                        saved.destinationColonyId,
                        kind,
                        load,
                        true));
            }
        }
        this.forcedFlightCooldownDays = savedDynasty.forcedFlightCooldownDays;

        if (savedDynasty.unlockedUpgradeIds != null) {
            Map<Integer, Upgrade> allUpgrades = new HashMap<>();
            for (Upgrade u : GameUnlocks.getUpgrades()) {
                allUpgrades.put(u.getId(), u);
            }
            for (Integer upId : savedDynasty.unlockedUpgradeIds) {
                Upgrade u = allUpgrades.get(upId);
                if (u != null) {
                    this.unlockedUpgrades.add(u);
                }
            }
        }
        
        initializeColor();
        initializeServices();
        
        rankUp();
    }

    private void initializeServices() {
        this.automationService = new DynastyAutomationService();
        this.aiService = new DynastyAiService();
        this.logisticsAutomationService = new DynastyLogisticsAutomationService();
        this.starterService = new DynastyStarterService();
        this.statService = new DynastyStatService();
        this.diplomacyService = new DynastyDiplomacyService(this);
    }
    
    private void initializeColor() {
        if (this.isPlayer) {
            this.color = AssetStyles.COLOR_LIGHT_BLUE; 
        } else {
            float hue = (this.id * 0.618033988749895f) % 1.0f;
            this.color = Color.getHSBColor(hue, 0.75f, 0.95f);
        }
    }

    public String generateNextColonyName() {
        String baseName = LanguageStrings.stripDynastyNameSuffix(this.name);
        if (baseName == null || baseName.isEmpty()) {
            baseName = "Player";
        }
        
        int count = colonies.size();
        if (count == 0) return baseName + " Prime";
        if (count == 1) return "New " + baseName;
        if (count == 2) return baseName + " Secundus";
        if (count == 3) return baseName + " Tertius";
        if (count == 4) return baseName + " Quartus";
        return baseName + " " + (count + 1);
    }

    // --- Logic ---
    public void runDailyJobs() {
        runDailyJobs(null, null);
    }

    public void runDailyJobs(World world, TradeManager tradeManager) {
        if (this.isDefeated) return;
        if (forcedFlightCooldownDays > 0) {
            forcedFlightCooldownDays--;
        }
        this.automationService.runDailyAutomation(this);
        if (world != null) {
            this.aiService.runDailyAi(this, world, tradeManager);
        }
        if (world != null && tradeManager != null) {
            bindTradeManager(tradeManager);
            this.logisticsAutomationService.runDailyLogistics(this, world, tradeManager);
        }
        this.rankUp();
    }
    
    private void rankUp() {
        int total = this.statService.getTotalPopulation(this);
                
        if (total >= GameConstants.RANK_GIGA.getPopulation()) this.rank = GameConstants.RANK_GIGA;
        else if (total >= GameConstants.RANK_SUPREME.getPopulation()) this.rank = GameConstants.RANK_SUPREME;
        else if (total >= GameConstants.RANK_ULTIMATE.getPopulation()) this.rank = GameConstants.RANK_ULTIMATE;
        else if (total >= GameConstants.RANK_MEGA.getPopulation()) this.rank = GameConstants.RANK_MEGA;
        else if (total >= GameConstants.RANK_HYPER.getPopulation()) this.rank = GameConstants.RANK_HYPER;
        else if (total >= GameConstants.RANK_ULTRA.getPopulation()) this.rank = GameConstants.RANK_ULTRA;
        else if (total >= GameConstants.RANK_SUPER.getPopulation()) this.rank = GameConstants.RANK_SUPER;
        else if (total >= GameConstants.RANK_EMPIRE.getPopulation()) this.rank = GameConstants.RANK_EMPIRE;
        else if (total >= GameConstants.RANK_KINGDOM.getPopulation()) this.rank = GameConstants.RANK_KINGDOM;
        else if (total >= GameConstants.RANK_DUCHY.getPopulation()) this.rank = GameConstants.RANK_DUCHY;
        else if (total >= GameConstants.RANK_COUNTY.getPopulation()) this.rank = GameConstants.RANK_COUNTY;
        else if (total >= GameConstants.RANK_COLONY.getPopulation()) this.rank = GameConstants.RANK_COLONY;
        else this.rank = GameConstants.RANK_ANT;
    }
    
    public void recordDeath(String cause) {
        this.globalDeathStatistics.merge(DeathCause.normalize(cause), 1, Integer::sum);
    }

    public void addColony(Colony colony) {
        if (!colonies.contains(colony)) {
            colonies.add(colony);
            colony.setDynasty(this); 
            rankUp();
        }
    }

    public void removeColony(Colony colony) {
        colonies.remove(colony);
        if (capital == colony) {
            capital = null;
        }
        rankUp();
    }
    
    public void addAbsorbedDynasty(int dynastyId) {
        if (!absorbedDynastyIds.contains(dynastyId)) {
            absorbedDynastyIds.add(dynastyId);
        }
    }

    public void absorbSpecies(int speciesId) {
        if (!defeatedSpeciesIds.contains(speciesId)) {
            defeatedSpeciesIds.add(speciesId);
        }
    }
    
    public void incrementNuptialFlights() {
        this.totalNuptialFlights++;
        
        this.geneticIntegrity = Math.max(0.0, this.geneticIntegrity - 1.0);
        
        if (this.totalNuptialFlights >= 10 && !hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT)) {
            unlockUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT);
        }
    }
    
    public int getMassNuptialFlightCost() {
        if (colonies.isEmpty()) return 10000;
        
        long baseCost = (long) colonies.get(0).getNuptialFlightCost();
        long scaledCost = baseCost * 10L;
        
        if (scaledCost > Integer.MAX_VALUE - 100000) {
            return Integer.MAX_VALUE - 100000;
        }
        
        return (int) scaledCost;
    }
    
    public void runMassNuptialFlight(World world) {
        if (!hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT)) return;
        
        int cost = getMassNuptialFlightCost();
        if (getResearchPoints() < cost) return;
        
        addResearchPoints(-cost);
        
        for (Colony colony : new ArrayList<>(colonies)) {
            Hex hex = null;
            for (Hex h : world.getHexes()) {
                if (h.getColony() == colony) {
                    hex = h;
                    break;
                }
            }
            
            if (hex != null) {
                boolean hasDrones = !colony.getDrones().isEmpty();
                boolean hasBreeders = colony.getPrincesses().stream().anyMatch(p -> p.getRole() == GameConstants.ROLE_BREEDER);
                
                if (hasDrones && hasBreeders) {
                    colony.getLabourService().runNuptial(colony, world, hex);
                }
            }
        }
    }

    public List<Tunnel> getTunnels() { return tunnels; }
    public void addTunnel(Tunnel tunnel) {
        if (!tunnels.contains(tunnel)) {
            tunnels.add(tunnel);
        }
    }

    public Tunnel getTunnelBetween(Hex a, Hex b) {
        return tunnels.stream()
            .filter(t -> t.connects(a, b))
            .findFirst()
            .orElse(null);
    }

    public Colony getCapital() {
        if (capital == null) {
            resolveCapitalFromColonies();
        }
        return capital;
    }

    /** Infer capital when save has no capitalColonyId or it was not wired on load. */
    public void resolveCapitalFromColonies() {
        if (colonies == null || colonies.isEmpty()) {
            return;
        }
        List<Colony> flagged = new ArrayList<>();
        for (Colony c : colonies) {
            if (c.isCapital()) {
                flagged.add(c);
            }
        }
        if (flagged.size() == 1) {
            setCapital(flagged.get(0));
            return;
        }
        if (flagged.size() > 1) {
            flagged.stream()
                    .max(Comparator.comparingInt(Colony::getMilitaryPower)
                            .thenComparingInt(Colony::getId))
                    .ifPresent(this::setCapital);
            return;
        }
        colonies.stream()
                .min(Comparator.comparingInt(Colony::getId))
                .ifPresent(this::setCapital);
    }

    public void setCapital(Colony colony) {
        if (colony != null && !colonies.contains(colony)) {
            addColony(colony);
        }
        this.capital = colony;
        for (Colony c : colonies) {
            c.setCapital(c == colony);
        }
    }

    public int getDiplomaticReputation(int otherDynastyId) {
        if (otherDynastyId == id) {
            return GameConstants.DEFAULT_DIPLOMATIC_REPUTATION;
        }
        return GameConstants.clampDiplomaticReputation(
                diplomaticReputations.getOrDefault(otherDynastyId, GameConstants.DEFAULT_DIPLOMATIC_REPUTATION));
    }

    public void setDiplomaticReputation(int otherDynastyId, int score) {
        if (otherDynastyId == id) {
            return;
        }
        diplomaticReputations.put(otherDynastyId, GameConstants.clampDiplomaticReputation(score));
    }

    public void adjustDiplomaticReputation(int otherDynastyId, int delta) {
        setDiplomaticReputation(otherDynastyId, getDiplomaticReputation(otherDynastyId) + delta);
    }

    public Map<Integer, Integer> copyDiplomaticReputations() {
        return new HashMap<>(diplomaticReputations);
    }

    public String getDiplomaticModifierKey(int otherDynastyId) {
        if (otherDynastyId == id) {
            return null;
        }
        return diplomaticModifierKeys.get(otherDynastyId);
    }

    public void setDiplomaticModifierKey(int otherDynastyId, String modifierKey) {
        if (otherDynastyId == id) {
            return;
        }
        if (modifierKey == null) {
            diplomaticModifierKeys.remove(otherDynastyId);
        } else {
            diplomaticModifierKeys.put(otherDynastyId, modifierKey);
        }
    }

    public void clearDiplomaticModifierKey(int otherDynastyId) {
        setDiplomaticModifierKey(otherDynastyId, null);
    }

    public Map<String, String> copyDiplomaticModifierKeys() {
        Map<String, String> copy = new HashMap<>();
        for (Map.Entry<Integer, String> entry : diplomaticModifierKeys.entrySet()) {
            copy.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return copy;
    }

    public boolean hasCrossDynastyTradeRepBonus(int otherDynastyId) {
        return crossDynastyTradeRepGrantedIds.contains(otherDynastyId);
    }

    public void markCrossDynastyTradeRepBonus(int otherDynastyId) {
        if (otherDynastyId != id && !crossDynastyTradeRepGrantedIds.contains(otherDynastyId)) {
            crossDynastyTradeRepGrantedIds.add(otherDynastyId);
        }
    }

    public List<Integer> copyCrossDynastyTradeRepGrantedIds() {
        return new ArrayList<>(crossDynastyTradeRepGrantedIds);
    }

    public boolean hasPendingPactRequestFrom(int fromDynastyId) {
        return pendingPactRequestFromIds.contains(fromDynastyId);
    }

    public void addPendingPactRequest(int fromDynastyId) {
        if (fromDynastyId != id) {
            pendingPactRequestFromIds.add(fromDynastyId);
        }
    }

    public void removePendingPactRequest(int fromDynastyId) {
        pendingPactRequestFromIds.remove(fromDynastyId);
    }

    public List<Integer> copyPendingPactRequestFromIds() {
        return new ArrayList<>(pendingPactRequestFromIds);
    }

    public void addPendingTradeProposal(CrossDynastyTradeProposal proposal) {
        if (proposal == null || proposal.isEmpty()) {
            return;
        }
        pendingTradeProposals.removeIf(existing -> existing.getFromDynastyId() == proposal.getFromDynastyId()
                && existing.getOriginColonyId() == proposal.getOriginColonyId()
                && existing.getDestinationColonyId() == proposal.getDestinationColonyId());
        pendingTradeProposals.add(proposal);
    }

    public void removePendingTradeProposal(CrossDynastyTradeProposal proposal) {
        if (proposal == null) {
            return;
        }
        pendingTradeProposals.removeIf(existing -> existing.getFromDynastyId() == proposal.getFromDynastyId()
                && existing.getOriginColonyId() == proposal.getOriginColonyId()
                && existing.getDestinationColonyId() == proposal.getDestinationColonyId());
    }

    public boolean hasPendingTradeProposalFrom(int fromDynastyId) {
        for (CrossDynastyTradeProposal proposal : pendingTradeProposals) {
            if (proposal.getFromDynastyId() == fromDynastyId) {
                return true;
            }
        }
        return false;
    }

    public List<CrossDynastyTradeProposal> copyPendingTradeProposals() {
        return new ArrayList<>(pendingTradeProposals);
    }

    public int getForcedFlightCooldownDays() {
        return forcedFlightCooldownDays;
    }

    public void setForcedFlightCooldownDays(int days) {
        this.forcedFlightCooldownDays = Math.max(0, days);
    }

    // --- Getters & Setters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isPlayer() { return isPlayer; }
    public void setPlayer(boolean player) { 
        isPlayer = player; 
        initializeColor(); 
    }
    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }
    
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    
    public ColonyRank getRank() { return rank; }
    public void setRank(ColonyRank rank) { this.rank = rank; }

    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) {
        this.researchPoints = researchPoints;
        invalidateAffordableAlertCaches();
    }

    public void addResearchPoints(int amount) {
        this.researchPoints += amount;
        invalidateAffordableAlertCaches();
    }
    
    public int getTotalNuptialFlights() { return totalNuptialFlights; }

    public boolean isDefeated() { return isDefeated; }
    public void setDefeated(boolean isDefeated) { this.isDefeated = isDefeated; }

    public boolean isDefaultAutomationEnabled() { return defaultAutomationEnabled; }
    public void setDefaultAutomationEnabled(boolean enabled) { this.defaultAutomationEnabled = enabled; }

    public boolean isDefaultAutoBuildEnabled() { return defaultAutoBuildEnabled; }
    public void setDefaultAutoBuildEnabled(boolean enabled) { this.defaultAutoBuildEnabled = enabled; }

    public Set<Upgrade> getUnlockedUpgrades() { return unlockedUpgrades; }
    public boolean hasUpgrade(Upgrade upgrade) { return unlockedUpgrades.contains(upgrade); }
    public void unlockUpgrade(Upgrade upgrade) {
        unlockedUpgrades.add(upgrade);
        invalidateAffordableAlertCaches();
    }

    private void invalidateAffordableAlertCaches() {
        for (Colony colony : colonies) {
            colony.invalidateAffordableAlertCache();
        }
    }

    public List<Colony> getColonies() { return colonies; }
    public List<Integer> getAbsorbedDynastyIds() { return absorbedDynastyIds; }
    public List<Integer> getDefeatedSpeciesIds() { return defeatedSpeciesIds; }

    public Set<Assimilation> getCompletedAssimilations() { return completedAssimilations; }
    public boolean isAssimilationCompleted(Assimilation a) { return completedAssimilations.contains(a); }
    
    public void completeAssimilation(Assimilation a) { 
        if (!completedAssimilations.contains(a)) {
            completedAssimilations.add(a);
            if (hasUpgrade(GameUnlocks.ABILITY_CLONING)) {
                this.geneticIntegrity = Math.min(100.0, this.geneticIntegrity + 5.0);
            }
        }
    }

    public Assimilation getCurrentAssimilation() { return currentAssimilation; }
    public void setCurrentAssimilation(Assimilation a) { this.currentAssimilation = a; }
    public double getAssimilationProgress() { return assimilationProgress; }
    public void setAssimilationProgress(double progress) { this.assimilationProgress = progress; }
    public void addAssimilationProgress(double amount) { this.assimilationProgress += amount; }
    
    public Map<String, Integer> getGlobalDeathStatistics() { return globalDeathStatistics; }
    
    public void bindTradeManager(TradeManager tradeManager) {
        if (tradeManager == null) {
            this.tradeService = null;
            return;
        }
        if (this.tradeService != null && this.tradeService.getTradeManager() == tradeManager) {
            return;
        }
        this.tradeService = new DynastyTradeService(this, tradeManager);
    }

    public DynastyStarterService getStarterService() { return starterService; }
    public DynastyStatService getStatService() { return statService; }
    public DynastyTradeService getTradeService() { return tradeService; }
    public DynastyDiplomacyService getDiplomacyService() { return diplomacyService; }

    public double getDiplomaticGeneticIntegrityBonus() {
        double bonus = 0.0;
        for (String modifierKey : diplomaticModifierKeys.values()) {
            GeneticIntegrityModifier modifier = GameConstants.getGeneticIntegrityModifierForDiplomaticKey(modifierKey);
            if (modifier != null) {
                bonus += modifier.getIntegrityDelta();
            }
        }
        return bonus;
    }

    public double getBaseGeneticIntegrity() {
        return geneticIntegrity;
    }

    public double getMinGeneticIntegrity() {
        if (hasUpgrade(GameUnlocks.ABILITY_CLONING)) {
            return Math.min(100.0, completedAssimilations.size() * 5.0);
        }
        return 0.0;
    }

    public double getGeneticIntegrity() {
        double effective = geneticIntegrity + getDiplomaticGeneticIntegrityBonus();
        return Math.min(100.0, Math.max(getMinGeneticIntegrity(), effective));
    }
    
    public void setGeneticIntegrity(double geneticIntegrity) { 
        this.geneticIntegrity = Math.max(getMinGeneticIntegrity(), geneticIntegrity); 
    }

    public int getMilitaryPower() {
        return militaryPower;
    }

    public void setMilitaryPower(int militaryPower) {
        this.militaryPower = Math.max(0, militaryPower);
    }

}