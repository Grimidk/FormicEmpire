package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.Color;

import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.services.DynastyAutomationService;
import com.grimidk.formicempire.classes.entities.services.DynastyStarterService;
import com.grimidk.formicempire.classes.entities.services.DynastyStatService;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

public class Dynasty {

    private final int id;
    private String name;
    private boolean isPlayer;
    private Species species;
    private int researchPoints;
    private ColonyRank rank;
    private Color color;
    
    // --- State Flags ---
    private boolean isDefeated; 
    
    // Global Data
    private final Set<Upgrade> unlockedUpgrades;
    private final List<Colony> colonies;
    private final Map<String, Integer> globalDeathStatistics;
    private final List<Integer> absorbedDynastyIds;

    // Services
    private transient DynastyAutomationService automationService;
    private transient DynastyStarterService starterService;
    private transient DynastyStatService statService;

    public Dynasty(int id, String name, boolean isPlayer, Species species) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.species = species;
        this.colonies = new ArrayList<>();
        this.unlockedUpgrades = new HashSet<>();
        this.globalDeathStatistics = new ConcurrentHashMap<>();
        this.absorbedDynastyIds = new ArrayList<>();
        this.researchPoints = 0;
        this.rank = GameConstants.RANK_ANT;
        this.isDefeated = false;
        
        initializeColor();
        initializeServices();
    }
    
    public Dynasty(Savefile.SavedDynasty savedDynasty) {
        this.id = savedDynasty.id;
        this.name = savedDynasty.name;
        this.isPlayer = savedDynasty.isPlayer;
        this.researchPoints = savedDynasty.researchPoints;
        this.isDefeated = savedDynasty.isDefeated;
        
        this.species = GameConstants.SPECIES_OMNI; 
        for(Species s : GameConstants.getSpecies()) {
            if (s.getId() == savedDynasty.speciesId) {
                this.species = s;
                break;
            }
        }

        this.colonies = new ArrayList<>();
        this.unlockedUpgrades = new HashSet<>();
        this.globalDeathStatistics = new ConcurrentHashMap<>();
        
        this.absorbedDynastyIds = new ArrayList<>();
        if (savedDynasty.absorbedDynastyIds != null) {
            this.absorbedDynastyIds.addAll(savedDynasty.absorbedDynastyIds);
        }
        
        if (savedDynasty.deathStatistics != null) {
            this.globalDeathStatistics.putAll(savedDynasty.deathStatistics);
        }

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
        this.starterService = new DynastyStarterService();
        this.statService = new DynastyStatService();
    }
    
    private void initializeColor() {
        if (this.isPlayer) {
            this.color = new Color(0, 191, 255); 
        } else {
            float hue = (this.id * 0.618033988749895f) % 1.0f;
            this.color = Color.getHSBColor(hue, 0.75f, 0.95f);
        }
    }

    public String generateNextColonyName() {
        String baseName = this.name;
        if (baseName != null && baseName.endsWith(" Dynasty")) {
            baseName = baseName.substring(0, baseName.length() - 8);
        } else if (baseName == null) {
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
        if (this.isDefeated) return;
        this.automationService.runDailyAutomation(this);
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
        this.globalDeathStatistics.merge(cause, 1, Integer::sum);
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
        rankUp();
    }
    
    public void addAbsorbedDynasty(int dynastyId) {
        if (!absorbedDynastyIds.contains(dynastyId)) {
            absorbedDynastyIds.add(dynastyId);
        }
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
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }
    public void addResearchPoints(int amount) { this.researchPoints += amount; }

    public boolean isDefeated() { return isDefeated; }
    public void setDefeated(boolean isDefeated) { this.isDefeated = isDefeated; }

    public Set<Upgrade> getUnlockedUpgrades() { return unlockedUpgrades; }
    public boolean hasUpgrade(Upgrade upgrade) { return unlockedUpgrades.contains(upgrade); }
    public void unlockUpgrade(Upgrade upgrade) { unlockedUpgrades.add(upgrade); }

    public List<Colony> getColonies() { return colonies; }
    public List<Integer> getAbsorbedDynastyIds() { return absorbedDynastyIds; }
    
    public Map<String, Integer> getGlobalDeathStatistics() { return globalDeathStatistics; }
    
    public DynastyStarterService getStarterService() { return starterService; }
    public DynastyStatService getStatService() { return statService; }

}