package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.services.CivilizationAutomationService;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

public class Civilization {

    private final int id;
    private String name;
    private boolean isPlayer;
    private Species species;
    
    // Global Resources
    private int researchPoints;
    private final Set<Upgrade> unlockedUpgrades;
    
    // Children
    private final List<Colony> colonies;

    // Services
    private transient CivilizationAutomationService automationService;

    public Civilization(int id, String name, boolean isPlayer) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.colonies = new ArrayList<>();
        this.unlockedUpgrades = new HashSet<>();
        this.researchPoints = 0;
        this.automationService = new CivilizationAutomationService();
        
        initializeUpgrades();
    }
    
    // public Civilization(Savefile.SavedCivilization savedCiv) {
    //     this.id = savedCiv.id;
    //     this.name = savedCiv.name;
    //     this.isPlayer = savedCiv.isPlayer;
    //     this.researchPoints = savedCiv.researchPoints;
    //     this.colonies = new ArrayList<>();
    //     this.unlockedUpgrades = new HashSet<>();
    //     this.automationService = new CivilizationAutomationService();

    //     if (savedCiv.unlockedUpgradeIds != null) {
    //         for (Integer upId : savedCiv.unlockedUpgradeIds) {
    //             for (Upgrade u : GameUnlocks.getUpgrades()) {
    //                 if (u.getId() == upId) {
    //                     this.unlockedUpgrades.add(u);
    //                     break;
    //                 }
    //             }
    //         }
    //     } else {
    //         initializeUpgrades();
    //     }
    // }

    private void initializeUpgrades() {
        this.unlockedUpgrades.add(GameUnlocks.TYPE_EGG);
        this.unlockedUpgrades.add(GameUnlocks.TYPE_QUEEN);
        this.unlockedUpgrades.add(GameUnlocks.TYPE_WORKER);
        this.unlockedUpgrades.add(GameUnlocks.ROLE_FORAGER);
        this.unlockedUpgrades.add(GameUnlocks.ROLE_FARMER);
        this.unlockedUpgrades.add(GameUnlocks.ROLE_NURSE);
        this.unlockedUpgrades.add(GameUnlocks.ROLE_LAYER);
        this.unlockedUpgrades.add(GameUnlocks.STAT_SKELETON);
        this.unlockedUpgrades.add(GameUnlocks.STAT_ACID);
        this.unlockedUpgrades.add(GameUnlocks.STAT_LONGEVITY);
    }

    // --- Core Logic ---
    public void runHourlyJobs() {
        for (Colony colony : colonies) {
            colony.runHourlyJobs();
        }
    }

    public void runDailyJobs(Object tempIcon, Object biome) {
        this.automationService.runDailyAutomation(this);
        for (Colony colony : colonies) {
        }
    }

    public void runMonthlyJobs() {
        for (Colony colony : colonies) {
            colony.runMonthlyJobs();
        }
    }

    public void runYearlyJobs() {
        for (Colony colony : colonies) {
            colony.runYearlyJobs();
        }
    }

    public void addColony(Colony colony) {
        if (!colonies.contains(colony)) {
            colonies.add(colony);
            // colony.setCivilization(this); 
        }
    }

    public void removeColony(Colony colony) {
        colonies.remove(colony);
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isPlayer() { return isPlayer; }
    public void setPlayer(boolean player) { isPlayer = player; }
    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }

    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }
    public void addResearchPoints(int amount) { this.researchPoints += amount; }

    public Set<Upgrade> getUnlockedUpgrades() { return unlockedUpgrades; }
    public boolean hasUpgrade(Upgrade upgrade) { return unlockedUpgrades.contains(upgrade); }
    public void unlockUpgrade(Upgrade upgrade) { unlockedUpgrades.add(upgrade); }

    public List<Colony> getColonies() { return colonies; }
}