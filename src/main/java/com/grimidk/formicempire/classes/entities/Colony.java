package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.awt.Point;
import javax.swing.ImageIcon;
import java.util.concurrent.CopyOnWriteArrayList; 

import com.grimidk.formicempire.classes.constants.AntRole;
import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.Building;
import com.grimidk.formicempire.classes.constants.ColonyRank;
import com.grimidk.formicempire.classes.constants.Species;
import com.grimidk.formicempire.classes.constants.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;

public class Colony {
    private final int id;
    private String name;
    private Species species;
    private boolean isPlayer;
    private ColonyRank rank;
    
    private final Map<AntType, List<Ant>> antGroups;
    private final List<Ant> deadAnts; 
    private Map<AntRole, Integer> assignedRoleCounts;
    private final Set<Upgrade> upgrades;
    private final Set<Building> buildings;

    // Resources
    private int plants;
    private int mushrooms;
    private int protein;
    private int water;
    private int syrups;
    private int resins;
    private int minerals;

    // Capacities
    private int aphidCapacity;
    private int aphids;

    // Research
    private int researchSpeed;
    private int researchPoints;

    // Rates
    private int growthTime;
    private int layingRate;
    private float conversionRate;
    private float nursingRate;
    private float gravingRate;
    private float collectingRate;
    private int parasiteDetection;

    // Base Stats
    private int baseHealth;
    private int baseTempRes;
    private int baseRegen;
    private int baseConsumption;
    private int baseAttack;
    private int baseAttackSpeed;
    private int baseDefense;
    private int baseSpeed;  
    private int baseSize;

    // Hatch Rates
    private float hatchRateWorker;
    private float hatchRateSoldier;
    private float hatchRateMajor;
    private float hatchRateDrone;
    private float hatchRatePrincess;

    private int gameAreaWidth = 1;
    private int gameAreaHeight = 1;
    
    // Building Project
    private Building currentBuildingProject = null;
    private double buildingProgressHours = 0.0;

    private void initializeLists() {
        this.antGroups.put(GameConstants.TYPE_EGG, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_LARVA, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_PUPA, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_WORKER, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_SOLDIER, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_MAJOR, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_DRONE, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_PRINCESS, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_QUEEN, new CopyOnWriteArrayList<>());
    }

    private void initializeAssignedRoles() {
        this.assignedRoleCounts = new HashMap<>();
        for (AntRole role : GameConstants.getAntRoles()) {
            this.assignedRoleCounts.put(role, 0);
        }
    }

    private void initializeDefaults() {
        this.researchSpeed = 1;
        this.researchPoints = 0;

        this.growthTime = 4;
        this.layingRate = 1;
        this.conversionRate = 0.1f;
        this.nursingRate = 10f;
        this.gravingRate = 5f;
        this.collectingRate = 1f;
        this.parasiteDetection = 10;

        this.baseHealth = 100;
        this.baseTempRes = 25;
        this.baseRegen = 1;
        this.baseConsumption = 1;
        this.baseAttack = 10;
        this.baseAttackSpeed = 1;
        this.baseDefense = 5;
        this.baseSpeed = 1;
        this.baseSize = 1;

        this.plants = 0;
        this.mushrooms = 0;
        this.protein = 0;           
        this.water = 0;
        this.syrups = 0;
        this.resins = 0;
        this.minerals = 0;

        this.aphidCapacity = 10;
        this.aphids = 0;

        this.hatchRateWorker = 100.0f;
        this.hatchRateSoldier = 0.0f;
        this.hatchRateMajor = 0.0f;
        this.hatchRateDrone = 0.0f;
        this.hatchRatePrincess = 0.0f;
    }

    private void initializeUpgrades() {
        this.upgrades.add(GameUnlocks.TYPE_EGG);
        this.upgrades.add(GameUnlocks.TYPE_QUEEN);
        this.upgrades.add(GameUnlocks.TYPE_WORKER);
        this.upgrades.add(GameUnlocks.ROLE_FORAGER);
        this.upgrades.add(GameUnlocks.ROLE_FARMER);
        this.upgrades.add(GameUnlocks.ROLE_NURSE);
        this.upgrades.add(GameUnlocks.ROLE_LAYER);
        this.upgrades.add(GameUnlocks.STAT_SKELETON);
        this.upgrades.add(GameUnlocks.STAT_ACID);
        this.upgrades.add(GameUnlocks.STAT_LONGEVITY);
    }

    private void loadUpgrades(Savefile savefile) {
        List<Integer> unlockedIds = savefile.getUnlockedUpgradeIds(); 

        if (unlockedIds == null || unlockedIds.isEmpty()) {
            initializeUpgrades();
            return;
        }

        Map<Integer, Upgrade> allUpgrades = new HashMap<>();
        for (Upgrade up : GameUnlocks.getUpgrades()) {
            allUpgrades.put(up.getId(), up);
        }

        for (Integer id : unlockedIds) {
            Upgrade upgradeToUnlock = allUpgrades.get(id);
            if (upgradeToUnlock != null) {
                this.upgrades.add(upgradeToUnlock);
            }
        }
    }

    private void initializeBuildings() {
        this.buildings.add(GameUnlocks.ROYAL_CHAMBER_0);
        this.buildings.add(GameUnlocks.EGG_CHAMBER_0);
        this.buildings.add(GameUnlocks.MUSHROOM_CHAMBER_0);
        this.buildings.add(GameUnlocks.PLANT_CHAMBER_0);
        this.buildings.add(GameUnlocks.WATER_RESERVOIR_0);
    }

    private void laodBuildings(Savefile savefile) {
        List<Integer> unlockedIds = savefile.getUnlockedBuildingIds(); 

        if (unlockedIds == null || unlockedIds.isEmpty()) {
            initializeBuildings();
            return;
        }

        Map<Integer, Building> allBuildings = new HashMap<>();
        for (Building up : GameUnlocks.getBuildings()) {
            allBuildings.put(up.getId(), up);
        }

        for (Integer id : unlockedIds) {
            Building buildingToUnlock = allBuildings.get(id);
            if (buildingToUnlock != null) {
                this.buildings.add(buildingToUnlock);
            }
        }
    }

    public Colony(int id, String name, boolean isPlayer) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<>();
        this.deadAnts = new CopyOnWriteArrayList<>(); 
        this.upgrades = new HashSet<>();
        this.buildings = new HashSet<>();
        
        initializeLists();
        initializeDefaults();
        initializeUpgrades();
        initializeBuildings();
        initializeAssignedRoles();
    }

    public Colony(Savefile savefile) {
        this.id = savefile.getColonyId() > 0 ? savefile.getColonyId() : savefile.getId();
        this.name = savefile.getColonyName() != null && !savefile.getColonyName().isEmpty() ? savefile.getColonyName() : savefile.getName();
        this.isPlayer = true;
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<>();
        this.deadAnts = new CopyOnWriteArrayList<>(); 
        this.upgrades = new HashSet<>();
        this.buildings = new HashSet<>();

        initializeLists();
        initializeDefaults(); 
        loadUpgrades(savefile);
        laodBuildings(savefile);
        initializeAssignedRoles(); 
        
        Map<String, Integer> savedRoles = savefile.getAssignedRoleCounts();
        if (savedRoles != null && !savedRoles.isEmpty()) {
            for (AntRole role : GameConstants.getAntRoles()) {
                Integer count = savedRoles.get(role.getName());
                if (count != null) {
                    this.assignedRoleCounts.put(role, count);
                }
            }
        }
        
        // Hatch Rates
        this.hatchRateWorker = savefile.getHatchRateWorker();
        this.hatchRateSoldier = savefile.getHatchRateSoldier();
        this.hatchRateMajor = savefile.getHatchRateMajor();
        this.hatchRateDrone = savefile.getHatchRateDrone();
        this.hatchRatePrincess = savefile.getHatchRatePrincess();

        // Ant populations
        populateAntList(getEggs(), savefile.getEggs(), GameConstants.TYPE_EGG);
        populateAntList(getLarvae(), savefile.getLarvae(), GameConstants.TYPE_LARVA);
        populateAntList(getPupae(), savefile.getPupae(), GameConstants.TYPE_PUPA);
        populateAntList(getWorkers(), savefile.getWorkers(), GameConstants.TYPE_WORKER);
        populateAntList(getSoldiers(), savefile.getSoldiers(), GameConstants.TYPE_SOLDIER);
        populateAntList(getMajors(), savefile.getMajors(), GameConstants.TYPE_MAJOR);
        populateAntList(getDrones(), savefile.getDrones(), GameConstants.TYPE_DRONE);
        populateAntList(getPrincesses(), savefile.getPrincesses(), GameConstants.TYPE_PRINCESS);
        populateAntList(getQueens(), savefile.getQueens(), GameConstants.TYPE_QUEEN);
        populateAntList(deadAnts, savefile.getDeadAnts(), GameConstants.TYPE_DEAD);

        // Resources
        this.plants = savefile.getPlants();
        this.mushrooms = savefile.getMushrooms();
        this.protein = savefile.getProtein();
        this.water = savefile.getWater();
        this.syrups = savefile.getSyrups();
        this.resins = savefile.getResins();
        this.minerals = savefile.getMinerals();

        // New Stats
        this.aphids = savefile.getAphids();
        this.researchPoints = savefile.getResearchPoints();
        this.researchSpeed = savefile.getResearchSpeed();
        this.growthTime = savefile.getGrowthTime();
        this.layingRate = savefile.getLayingRate();
        this.conversionRate = savefile.getConversionRate();
        this.nursingRate = savefile.getNursingRate();
        this.gravingRate = savefile.getGravingRate();
        this.collectingRate = savefile.getCollectingRate();
        this.aphidCapacity = savefile.getAphidCapacity();

        runRoleAssignment(); 
    }

    private Point getRandomPosition(ImageIcon sprite) {
        int w = (sprite != null) ? sprite.getIconWidth() : 0;
        int h = (sprite != null) ? sprite.getIconHeight() : 0;
        
        int boundX = Math.max(1, gameAreaWidth - w);
        int boundY = Math.max(1, gameAreaHeight - h);
        
        int x = (int) (Math.random() * boundX);
        int y = (int) (Math.random() * boundY);
        return new Point(x, y);
    }

    private void populateAntList(List<Ant> list, int count, AntType type) {
        for (int i = 0; i < count; i++) {
            Ant newAnt = new Ant(this, type);
            list.add(newAnt);
        }
    }
    
    private void randomizeAllAntPositions() {
        for (Map.Entry<AntType, List<Ant>> entry : antGroups.entrySet()) {
            AntType type = entry.getKey();
            if (type == GameConstants.TYPE_DEAD) continue;
            
            ImageIcon sprite = type.getSprite();
            List<Ant> ants = entry.getValue();
            
            for (Ant ant : ants) {
                ant.setPosition(getRandomPosition(sprite));
            }
        }
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }
    public boolean isPlayer() { return isPlayer; }
    public void setIsPlayer(boolean isPlayer) { this.isPlayer = isPlayer; }
    public ColonyRank getRank() { return rank; }
    public void setRank(ColonyRank rank) { this.rank = rank; }

    public List<Ant> getAntsByType(AntType type) {
        return antGroups.getOrDefault(type, new CopyOnWriteArrayList<>());
    }
    
    public List<Ant> getEggs() { return antGroups.get(GameConstants.TYPE_EGG); }
    public void setEggs(List<Ant> eggs) { antGroups.put(GameConstants.TYPE_EGG, eggs); }

    public List<Ant> getLarvae() { return antGroups.get(GameConstants.TYPE_LARVA); }
    public void setLarvae(List<Ant> larvae) { antGroups.put(GameConstants.TYPE_LARVA, larvae); }

    public List<Ant> getPupae() { return antGroups.get(GameConstants.TYPE_PUPA); }
    public void setPupae(List<Ant> pupae) { antGroups.put(GameConstants.TYPE_PUPA, pupae); }

    public List<Ant> getWorkers() { return antGroups.get(GameConstants.TYPE_WORKER); }
    public void setWorkers(List<Ant> workers) { antGroups.put(GameConstants.TYPE_WORKER, workers); }

    public List<Ant> getSoldiers() { return antGroups.get(GameConstants.TYPE_SOLDIER); }
    public void setSoldiers(List<Ant> soldiers) { antGroups.put(GameConstants.TYPE_SOLDIER, soldiers); }

    public List<Ant> getMajors() { return antGroups.get(GameConstants.TYPE_MAJOR); }
    public void setMajors(List<Ant> majors) { antGroups.put(GameConstants.TYPE_MAJOR, majors); }

    public List<Ant> getDrones() { return antGroups.get(GameConstants.TYPE_DRONE); }
    public void setDrones(List<Ant> drones) { antGroups.put(GameConstants.TYPE_DRONE, drones); }

    public List<Ant> getPrincesses() { return antGroups.get(GameConstants.TYPE_PRINCESS); }
    public void setPrincesses(List<Ant> princesses) { antGroups.put(GameConstants.TYPE_PRINCESS, princesses); }

    public List<Ant> getQueens() { return antGroups.get(GameConstants.TYPE_QUEEN); }
    public void setQueens(List<Ant> queens) { antGroups.put(GameConstants.TYPE_QUEEN, queens); }

    public List<Ant> getDeadAnts() { return deadAnts; }
    public void setDeadAnts(List<Ant> deadAnts) {
        this.deadAnts.clear();
        this.deadAnts.addAll(deadAnts);
    }
    
    public int getAntTotal() {
        return antGroups.values().stream().mapToInt(List::size).sum();
    }

    public boolean hasUpgrade(Upgrade upgrade) {
        return this.upgrades.contains(upgrade);
    }

    public void unlockUpgrade(Upgrade upgrade) {
        this.upgrades.add(upgrade); 
    }

    public Set<Upgrade> getUnlockedUpgrades() {
        return this.upgrades;
    }

    public boolean hasBuilding(Building building) {
        return this.buildings.contains(building);
    }

    public void unlockBuilding(Building building) {
        this.buildings.add(building); 
    }

    public Set<Building> getUnlockedBuildings() {
        return this.buildings;
    }
    
    public Building getCurrentBuildingProject() { return currentBuildingProject; }
    public double getBuildingProgressHours() { return buildingProgressHours; }

    public boolean startBuildingProject(Building building) {
        if (currentBuildingProject != null) return false; 
        
        if (getMinerals() < building.getMineralCost() || getResins() < building.getResinCost()) {
            return false; 
        }
        
        setMinerals(getMinerals() - building.getMineralCost());
        setResins(getResins() - building.getResinCost());
        
        this.currentBuildingProject = building;
        this.buildingProgressHours = 0.0;
        return true;
    }

    public int getTotalConsumption(){
        double totalConsumption = 0;
        for (Map.Entry<AntType, List<Ant>> entry : antGroups.entrySet()) {
            totalConsumption += (double) entry.getValue().size() * entry.getKey().getConsumptionMult() * this.getBaseConsumption();
        }
        return (int) totalConsumption;
    }

    public int getTotalProduction(){
        int foragerCount = this.countAntsByRole(getWorkers(), GameConstants.ROLE_FORAGER);
        int hunterCount = this.countAntsByRole(getSoldiers(), GameConstants.ROLE_HUNTER);
        int farmerCount = this.countAntsByRole(getWorkers(), GameConstants.ROLE_FARMER);
        
        double plantCollection = foragerCount * 0.5 * this.getCollectingRate();
        double proteinCollection = hunterCount * this.getCollectingRate();
        
        int collectionPerHour = (int) (plantCollection + proteinCollection);
        int totalProductionRate = (int) (Math.min((conversionRate * farmerCount) * 60, collectionPerHour)) * 3 * 24; 
        return Math.min(totalProductionRate, this.getMushroomsCapacity());
    }

    // Resource Getters/Setters
    public int getPlants() { return plants; }
    public void setPlants(int plants) { this.plants = plants; }
    public int getMushrooms() { return mushrooms; }
    public void setMushrooms(int mushrooms) { this.mushrooms = mushrooms; }
    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }
    public int getWater() { return water; }
    public void setWater(int water) { this.water = water; }
    public int getSyrups() { return syrups; }
    public void setSyrups(int syrups) { this.syrups = syrups; }
    public int getResins() { return resins; }
    public void setResins(int resins) { this.resins = resins; }
    public int getMinerals() { return minerals; }
    public void setMinerals(int minerals) { this.minerals = minerals; }

    public int getPlantsCapacity() { 
        if (this.hasBuilding(GameUnlocks.PLANT_CHAMBER_2)) {return 25000;
        } else if (this.hasBuilding(GameUnlocks.PLANT_CHAMBER_1)) {return 10000;
        } else if (this.hasBuilding(GameUnlocks.PLANT_CHAMBER_0)) {return 4000; 
        } else {return 0;}
    }
    public int getMushroomsCapacity() { 
        if (this.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_2)) {return 40000;
        } else if (this.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_1)) {return 15000;
        } else if (this.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_0)) {return 8000; 
        } else {return 0;}
    }
    public int getProteinCapacity() { 
        if (this.hasBuilding(GameUnlocks.MEAT_CHAMBER_2)) {return 15000;
        } else if (this.hasBuilding(GameUnlocks.MEAT_CHAMBER_1)) {return 5000;
        } else if (this.hasBuilding(GameUnlocks.MEAT_CHAMBER_0)) {return 2000; 
        } else {return 0;}
    }
    public int getWaterCapacity() {
        if (this.hasBuilding(GameUnlocks.WATER_RESERVOIR_2)) {return 10000;
        } else if (this.hasBuilding(GameUnlocks.WATER_RESERVOIR_1)) {return 2500;
        } else if (this.hasBuilding(GameUnlocks.WATER_RESERVOIR_0)) {return 1000; 
        } else {return 0;}
    }
    public int getSyrupsCapacity() { 
        if (this.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_2)) {return 3500;
        } else if (this.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_1)) {return 1200;
        } else if (this.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_0)) {return 500; 
        } else {return 0;}
    }
    public int getResinsCapacity() { 
        if (this.hasBuilding(GameUnlocks.RESIN_RESERVOIR_2)) {return 1200;
        } else if (this.hasBuilding(GameUnlocks.RESIN_RESERVOIR_1)) {return 500;
        } else if (this.hasBuilding(GameUnlocks.RESIN_RESERVOIR_0)) {return 200; 
        } else {return 0;}
    }
    public int getMineralsCapacity() { 
        if (this.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_2)) {return 750;
        } else if (this.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_1)) {return 250;
        } else if (this.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_0)) {return 100; 
        } else {return 0;}
    }

    // Other Capacities
    public int getEggsCapacity() {
        if (this.hasBuilding(GameUnlocks.EGG_CHAMBER_2)) {return 150;
        } else if (this.hasBuilding(GameUnlocks.EGG_CHAMBER_1)) {return 80;
        } else if (this.hasBuilding(GameUnlocks.EGG_CHAMBER_0)) {return 50; 
        } else {return 0;}
    }
    public int getQueensCapacity() {
        if (this.hasBuilding(GameUnlocks.ROYAL_CHAMBER_2)) {return 4;
        } else if (this.hasBuilding(GameUnlocks.ROYAL_CHAMBER_1)) {return 2;
        } else if (this.hasBuilding(GameUnlocks.ROYAL_CHAMBER_0)) {return 1; 
        } else {return 0;}
    }
    public int getAphidCapacity() { return aphidCapacity; }
    public void setAphidCapacity(int aphidCapacity) { this.aphidCapacity = aphidCapacity; }
    public int getAphids() { return aphids; }
    public void setAphids(int aphids) { this.aphids = aphids; }

    // Research & Rates
    public int getResearchSpeed() { return researchSpeed; }
    public void setResearchSpeed(int researchSpeed) { this.researchSpeed = researchSpeed; }
    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }
    public int getGrowthTime() { return growthTime; }
    public void setGrowthTime(int growthTime) { this.growthTime = growthTime; }
    public int getLayingRate() { return layingRate; }
    public void setLayingRate(int layingRate) { this.layingRate = layingRate; }
    public float getConversionRate() { return conversionRate; }
    public void setConversionRate(float conversionRate) { this.conversionRate = conversionRate; }
    public float getNursingRate() { return nursingRate; }
    public void setNursingRate(float nursingRate) { this.nursingRate = nursingRate; }
    public float getGravingRate() { return gravingRate; }
    public void setGravingRate(float gravingRate) { this.gravingRate = gravingRate; }
    public float getCollectingRate() { return collectingRate; }
    public void setCollectingRate(float collectingRate) { this.collectingRate = collectingRate; }
    public int getParasiteDetection() { return parasiteDetection; }
    public void setParasiteDetection(int parasiteDetection) { this.parasiteDetection = parasiteDetection; }

    // Base Stats
    public int getBaseHealth() { return baseHealth; }
    public void setBaseHealth(int baseHealth) { this.baseHealth = baseHealth; }
    public int getBaseTempRes() { return baseTempRes; }
    public void setBaseTempRes(int baseTempRes) { this.baseTempRes = baseTempRes; }
    public int getBaseRegen() { return baseRegen; }
    public void setBaseRegen(int baseRegen) { this.baseRegen = baseRegen; }
    public int getBaseConsumption() { return baseConsumption; }
    public void setBaseConsumption(int baseConsumption) { this.baseConsumption = baseConsumption; }
    public int getBaseAttack() { return baseAttack; }
    public void setBaseAttack(int baseAttack) { this.baseAttack = baseAttack; }
    public int getBaseAttackSpeed() { return baseAttackSpeed; }
    public void setBaseAttackSpeed(int baseAttackSpeed) { this.baseAttackSpeed = baseAttackSpeed; }
    public int getBaseDefense() { return baseDefense; }
    public void setBaseDefense(int baseDefense) { this.baseDefense = baseDefense; }
    public int getBaseSpeed() { return baseSpeed; }
    public void setBaseSpeed(int baseSpeed) { this.baseSpeed = baseSpeed; }
    public int getBaseSize() { return baseSize; }
    public void setBaseSize(int baseSize) { this.baseSize = baseSize; }

    public void setGameAreaDimensions(int width, int height) {
        boolean firstTimeUpdate = (this.gameAreaWidth == 1 && this.gameAreaHeight == 1 && width > 1 && height > 1);
        
        this.gameAreaWidth = width;
        this.gameAreaHeight = height;

        if (firstTimeUpdate) {
            randomizeAllAntPositions();
        }
    }

    // Role Counts
    public int getAssignedRoleCount(AntRole role) {
        return assignedRoleCounts.getOrDefault(role, 0);
    }

    public void setAssignedRoleCount(AntRole role, int count) {
        if (count >= 0) {
            assignedRoleCounts.put(role, count);
        }
    }

    public Map<AntRole, Integer> getAssignedRoleCounts() {
        return assignedRoleCounts;
    }
    
    // Hatch Rates
    public float getHatchRateWorker() { return hatchRateWorker; }
    public void setHatchRateWorker(float hatchRateWorker) { this.hatchRateWorker = hatchRateWorker; }
    public float getHatchRateSoldier() { return hatchRateSoldier; }
    public void setHatchRateSoldier(float hatchRateSoldier) { this.hatchRateSoldier = hatchRateSoldier; }
    public float getHatchRateMajor() { return hatchRateMajor; }
    public void setHatchRateMajor(float hatchRateMajor) { this.hatchRateMajor = hatchRateMajor; }
    public float getHatchRateDrone() { return hatchRateDrone; }
    public void setHatchRateDrone(float hatchRateDrone) { this.hatchRateDrone = hatchRateDrone; }
    public float getHatchRatePrincess() { return hatchRatePrincess; }
    public void setHatchRatePrincess(float hatchRatePrincess) { this.hatchRatePrincess = hatchRatePrincess; }

    public float getHatchRate(AntType type) {
        if (type == GameConstants.TYPE_WORKER) return hatchRateWorker;
        if (type == GameConstants.TYPE_SOLDIER) return hatchRateSoldier;
        if (type == GameConstants.TYPE_MAJOR) return hatchRateMajor;
        if (type == GameConstants.TYPE_DRONE) return hatchRateDrone;
        if (type == GameConstants.TYPE_PRINCESS) return hatchRatePrincess;
        return 0f;
    }

    public void setHatchRate(AntType type, float rate) {
        if (type == GameConstants.TYPE_WORKER) this.hatchRateWorker = rate;
        else if (type == GameConstants.TYPE_SOLDIER) this.hatchRateSoldier = rate;
        else if (type == GameConstants.TYPE_MAJOR) this.hatchRateMajor = rate;
        else if (type == GameConstants.TYPE_DRONE) this.hatchRateDrone = rate;
        else if (type == GameConstants.TYPE_PRINCESS) this.hatchRatePrincess = rate;
    }

    // --- Starting Colony ---
    public void startColony() {
        List<Ant> workerList = getWorkers();
        for (int i = 0; i < 9; i++) {
            Ant worker = new Ant(this, GameConstants.TYPE_WORKER);
            workerList.add(worker);
        }
        Ant queen = new Ant(this, GameConstants.TYPE_QUEEN);
        getQueens().add(queen);

        setAssignedRoleCount(GameConstants.ROLE_LAYER, 1);
        setAssignedRoleCount(GameConstants.ROLE_NURSE, 3);
        setAssignedRoleCount(GameConstants.ROLE_FARMER, 1);
        setAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);
        
        this.getQueens().get(0).setRole(GameConstants.ROLE_LAYER);
        this.getWorkers().get(0).setRole(GameConstants.ROLE_NURSE);
        this.getWorkers().get(1).setRole(GameConstants.ROLE_NURSE);
        this.getWorkers().get(2).setRole(GameConstants.ROLE_FARMER);
        this.getWorkers().get(3).setRole(GameConstants.ROLE_NURSE);
        this.getWorkers().get(4).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(5).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(6).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(7).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(8).setRole(GameConstants.ROLE_FORAGER);
    }

    private AntRole getDefaultRoleForType(AntType type) {
        if (type == GameConstants.TYPE_WORKER) {
            return GameConstants.ROLE_FORAGER;
        } else if (type == GameConstants.TYPE_SOLDIER) {
            return GameConstants.ROLE_HUNTER;
        } else if (type == GameConstants.TYPE_MAJOR) {
            return GameConstants.ROLE_BRUTE; 
        } else if (type == GameConstants.TYPE_PRINCESS) {
            return GameConstants.ROLE_BREEDER;
        } else if (type == GameConstants.TYPE_DRONE) {
            return GameConstants.ROLE_DRONE;
        } else if (type == GameConstants.TYPE_QUEEN) {
            return GameConstants.ROLE_LAYER;
        }
        return null; 
    }

    private void assignRolesForType(List<Ant> ants, AntType type) {
        AntRole defaultRole = getDefaultRoleForType(type);
        if (defaultRole == null) return; 

        for (Ant ant : ants) {
            ant.setRole(defaultRole);
        }

        List<Ant> availableAnts = new ArrayList<>(ants); 
        
        Iterator<Map.Entry<AntRole, Integer>> roleIterator = assignedRoleCounts.entrySet().iterator();
        while(roleIterator.hasNext()) {
            Map.Entry<AntRole, Integer> entry = roleIterator.next();
            AntRole role = entry.getKey();
            
            if (role.getAntType() != type) continue;
            if (role.equals(defaultRole)) continue; 

            int desiredCount = entry.getValue();
            int assignedCount = 0;
             
            Iterator<Ant> antIterator = availableAnts.iterator();
            while (assignedCount < desiredCount && antIterator.hasNext()) {
                Ant antToAssign = antIterator.next();
                antToAssign.setRole(role); 
                antIterator.remove();
                assignedCount++;
            }
        }
        assignedRoleCounts.put(defaultRole, availableAnts.size());
    }

    public void runRoleAssignment() {
        assignRolesForType(getWorkers(), GameConstants.TYPE_WORKER);
        assignRolesForType(getSoldiers(), GameConstants.TYPE_SOLDIER);
        assignRolesForType(getMajors(), GameConstants.TYPE_MAJOR);
        assignRolesForType(getPrincesses(), GameConstants.TYPE_PRINCESS);
        assignRolesForType(getQueens(), GameConstants.TYPE_QUEEN);
    }

    private int countAntsByRole(List<Ant> antList, AntRole role) {
        int count = 0;
        for (Ant ant : antList) {
            if (role.equals(ant.getRole())) {
                count++;
            }
        }
        return count;
    }

    // --- Hatching Logic ---
    private AntType determineHatchType() {
        double rand = Math.random() * 100.0;
        double cumulative = 0.0;

        cumulative += this.hatchRateWorker;
        if (rand < cumulative) {
            return GameConstants.TYPE_WORKER;
        }

        if (hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            cumulative += this.hatchRateSoldier;
            if (rand < cumulative) {
                return GameConstants.TYPE_SOLDIER;
            }
        }
        
        if (hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
            cumulative += this.hatchRateMajor;
            if (rand < cumulative) {
                return GameConstants.TYPE_MAJOR;
            }
        }

        if (hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            cumulative += this.hatchRateDrone;
            if (rand < cumulative) {
                return GameConstants.TYPE_DRONE;
            }

            cumulative += this.hatchRatePrincess;
            if (rand < cumulative) {
                return GameConstants.TYPE_PRINCESS;
            }
        }
        
        return GameConstants.TYPE_WORKER;
    }

    private void evolveAnts(List<Ant> sourceList, List<Ant> destList, AntType newType) {
        List<Ant> antsToEvolve = new ArrayList<>();
        for (Ant ant : sourceList) {
            if (ant.getAge() >= this.getGrowthTime()) {
                antsToEvolve.add(ant);
            }
        }
        
        for (Ant ant : antsToEvolve) {
            ant.transform(this, newType);
            destList.add(ant);
        }
        
        sourceList.removeAll(antsToEvolve);
    }

    private void hatchPupae() {
        List<Ant> pupaeToHatch = new ArrayList<>();
        for (Ant pupa : getPupae()) {
            if (pupa.getAge() >= this.getGrowthTime()) {
                pupaeToHatch.add(pupa);
            }
        }

        for (Ant pupa : pupaeToHatch) {
            AntType newType = determineHatchType();
            pupa.transform(this, newType);
            
            antGroups.get(newType).add(pupa);
        }
        
        getPupae().removeAll(pupaeToHatch);
    }

    // --- Routine Colony Activities --- 
    public void rankUp() {
        if (getAntTotal() >= GameConstants.RANK_GIGA.getPopulation()) {
            setRank(GameConstants.RANK_GIGA);
        } else if (getAntTotal() >= GameConstants.RANK_SUPREME.getPopulation()) {
            setRank(GameConstants.RANK_SUPREME);
        } else if (getAntTotal() >= GameConstants.RANK_ULTIMATE.getPopulation()) {
            setRank(GameConstants.RANK_ULTIMATE);
        } else if (getAntTotal() >= GameConstants.RANK_MEGA.getPopulation()) {
            setRank(GameConstants.RANK_MEGA);
        } else if (getAntTotal() >= GameConstants.RANK_HYPER.getPopulation()) {
            setRank(GameConstants.RANK_HYPER);
        } else if (getAntTotal() >= GameConstants.RANK_ULTRA.getPopulation()) {
            setRank(GameConstants.RANK_ULTRA);
        } else if (getAntTotal() >= GameConstants.RANK_SUPER.getPopulation()) {
            setRank(GameConstants.RANK_SUPER);
        } else if (getAntTotal() >= GameConstants.RANK_EMPIRE.getPopulation()) {
            setRank(GameConstants.RANK_EMPIRE);
        } else if (getAntTotal() >= GameConstants.RANK_KINGDOM.getPopulation()) {
            setRank(GameConstants.RANK_KINGDOM);
        } else if (getAntTotal() >= GameConstants.RANK_DUCHY.getPopulation()) {
            setRank(GameConstants.RANK_DUCHY);
        } else if (getAntTotal() >= GameConstants.RANK_COUNTY.getPopulation()) {
            setRank(GameConstants.RANK_COUNTY);
        } else if (getAntTotal() >= GameConstants.RANK_COLONY.getPopulation()) {
            setRank(GameConstants.RANK_COLONY);
        } else {
            setRank(GameConstants.RANK_ANT);
        }
    }

    public void runHatching(){    
        hatchPupae();
        evolveAnts(getLarvae(), getPupae(), GameConstants.TYPE_PUPA);
        evolveAnts(getEggs(), getLarvae(), GameConstants.TYPE_LARVA);
    }

    public void runEating(){
        // --- 1. Water Consumption ---
        List<Ant> thirstyAnts = new ArrayList<>();
        int waterAvailable = this.getWater();
        List<AntType> adultDrinkOrder = Arrays.asList(
            GameConstants.TYPE_QUEEN,
            GameConstants.TYPE_WORKER,
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_PRINCESS,
            GameConstants.TYPE_DRONE
        );
        
        for (AntType type : adultDrinkOrder) {
            List<Ant> list = antGroups.get(type);
            for (Ant ant : list) {
                if (waterAvailable >= 1) {
                    waterAvailable -= 1;
                } else {
                    thirstyAnts.add(ant);
                }
            }
        }
        this.setWater(waterAvailable);

        // --- 2. Food Consumption ---
        int mushroomsAvailable = this.getMushrooms();
        List<AntType> eatOrder = Arrays.asList(
            GameConstants.TYPE_QUEEN,
            GameConstants.TYPE_WORKER,
            GameConstants.TYPE_LARVA,
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_PRINCESS,
            GameConstants.TYPE_DRONE
        );
        
        List<Ant> hungryAnts = new ArrayList<>();
        
        for (AntType type : eatOrder) {
            int consumptionPerAnt = (int) (type.getConsumptionMult() * this.getBaseConsumption());
            if (consumptionPerAnt <= 0) consumptionPerAnt = 1; 
            if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_PUPA) continue;

            List<Ant> list = antGroups.get(type);
            for (Ant ant : list) {
                if (mushroomsAvailable >= consumptionPerAnt) {
                    mushroomsAvailable -= consumptionPerAnt;
                } else {
                    hungryAnts.add(ant);
                }
            }
        }
        this.setMushrooms(mushroomsAvailable);

        // --- 3. Syrup Phase ---
        Set<Ant> antsInNeed = new HashSet<>(thirstyAnts);
        antsInNeed.addAll(hungryAnts);
        
        int syrupAvailable = hasUpgrade(GameUnlocks.ROLE_RANCHER) ? this.getSyrups() : 0;
        
        Iterator<Ant> needIterator = antsInNeed.iterator();
        while (needIterator.hasNext() && syrupAvailable > 0) {
            Ant ant = needIterator.next();
            syrupAvailable -= 1;
            
            needIterator.remove(); 
            thirstyAnts.remove(ant);
            hungryAnts.remove(ant);
        }
        this.setSyrups(syrupAvailable);
        
        // --- 4. Death Phase ---
        Set<Ant> antsToKill = new HashSet<>();
        for (Ant ant : thirstyAnts) {
            if (Math.random() < 0.25) {
                antsToKill.add(ant);
            }
        }
        
        for (Ant ant : hungryAnts) {
            antsToKill.add(ant); 
        }

        for (Ant ant : antsToKill) {
            if (ant.isAlive()) { 
                
                AntType originalType = ant.getType(); 
                
                ant.goDie();
                this.deadAnts.add(ant);

                List<Ant> antList = antGroups.get(originalType);
                if (antList != null) {
                    antList.remove(ant);
                }
            }
        }
    }

    public void runAging(){
        List<Ant> antsToKill = new ArrayList<>();
        for (List<Ant> antList : antGroups.values()) {
            for (Ant ant : antList) {
                ant.setAge(ant.getAge() + 1);
                
            }
        }
        
        for (Ant ant : antsToKill) {
             if (ant.isAlive()) { 
                AntType originalType = ant.getType(); 
                ant.goDie();
                this.deadAnts.add(ant);

                List<Ant> antList = antGroups.get(originalType);
                if (antList != null) {
                    antList.remove(ant);
                }
            }
        }
    }

    public void runNuptial() {
        List<Ant> princesses = getPrincesses();
        List<Ant> drones = getDrones();

        if (!hasUpgrade(GameUnlocks.TYPE_PRINCESS)) return;

        List<Ant> princessesToEvolve = new ArrayList<>();
        
        for (Ant princess : princesses) {
            boolean hasQueenSpace = (getQueens().size() + princessesToEvolve.size()) < this.getQueensCapacity();
            
            boolean hasDrones = !drones.isEmpty();            
            if (hasQueenSpace && hasDrones) {
                princessesToEvolve.add(princess);
                Ant deadDrone = drones.remove(drones.size() - 1); 
                deadDrone.goDie();
                this.deadAnts.add(deadDrone);
            } 
        }

        for (Ant princess : princessesToEvolve) {
            princess.transform(this, GameConstants.TYPE_QUEEN);
            getQueens().add(princess);
        }
        
        princesses.removeAll(princessesToEvolve);
        runSpreading(princesses); 
    }

    public void runSpreading(List<Ant> princesses) { 

    }
    
    public void runInfection() { 
        if (this.deadAnts.size() < 100) {
            return;
        } else {

        }
    }

    // --- Ant Jobs ---
    public void runLaying() {
        int layerCount = countAntsByRole(getQueens(), GameConstants.ROLE_LAYER);
        List<Ant> eggList = getEggs();
        int spaceAvailable = this.getEggsCapacity() - eggList.size();
        if (spaceAvailable <= 0) return;   
        
        int toLay = Math.min(layerCount * this.getLayingRate(), spaceAvailable);
        for (int i = 0; i < toLay; i++) {
            Ant newEgg = new Ant(this, GameConstants.TYPE_EGG);
            newEgg.setPosition(getRandomPosition(GameConstants.TYPE_EGG.getSprite()));
            eggList.add(newEgg);
        }
    }

    public void runResearch() {
        if (!hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;
        int researcherCount = countAntsByRole(getQueens(), GameConstants.ROLE_RESEARCHER);
        this.researchPoints += researcherCount * researchSpeed;
    }

    public void runGraveKeeping() {
        int graverCount = countAntsByRole(getWorkers(), GameConstants.ROLE_GRAVER);
        if (graverCount == 0 || getDeadAnts().isEmpty()) return;

        int canClean = graverCount * (int) gravingRate;
        List<Ant> antsToRemove = new ArrayList<>();
        
        for (Ant deadAnt : getDeadAnts()) {
            if (canClean <= 0) break;
            antsToRemove.add(deadAnt);
            canClean--;
        }
        
        getDeadAnts().removeAll(antsToRemove);
    }

    public void runNursing() {
        int nurseCount = countAntsByRole(getWorkers(), GameConstants.ROLE_NURSE);
        int babyAntTotal = this.getEggs().size() +  this.getLarvae().size() +  this.getPupae().size();

        if (babyAntTotal <= nurseCount * nursingRate) {
            return;
        }

        int deficit = babyAntTotal - (nurseCount * (int) nursingRate);

        List<AntType> killOrder = Arrays.asList(
            GameConstants.TYPE_LARVA,
            GameConstants.TYPE_EGG,
            GameConstants.TYPE_PUPA
        );

        for (AntType typeToKill : killOrder) {
            if (deficit <= 0) break;
            
            List<Ant> list = antGroups.get(typeToKill);
            List<Ant> antsToCull = new ArrayList<>();
            
            for (Ant ant : list) {
                if (deficit <= 0) break;
                
                if (Math.random() > 0.5) { 
                    antsToCull.add(ant);
                    deficit--;
                } 
            }
            
            for (Ant antToCull : antsToCull) {
                if (antToCull.isAlive()) {
                    antToCull.goDie(); 
                    this.deadAnts.add(antToCull);
                    list.remove(antToCull);
                }
            }
        }
    }

    public void runCollecting() {
        int foragerCount = countAntsByRole(getWorkers(), GameConstants.ROLE_FORAGER);
        int plantGain = 0;
        int waterGain = 0;
        
        for (int i = 0; i < foragerCount; i++) {
            if (Math.random() < 0.5) {
                waterGain++;
            } else {
                plantGain++;
            }
        }
        
        int effectivePlantGain = (int) (plantGain * collectingRate);
        int effectiveWaterGain = (int) (waterGain * collectingRate);
        int effectiveResinGain = (int) (plantGain * (collectingRate / 100));

        this.setPlants(Math.min(this.getPlants() + effectivePlantGain, this.getPlantsCapacity()));
        this.setWater(Math.min(this.getWater() + effectiveWaterGain, this.getWaterCapacity()));
        if (hasUpgrade(GameUnlocks.ABILITY_RESIN)) this.setResins(Math.min(this.getResins() + effectiveResinGain, this.getResinsCapacity()));

        if (hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            int hunterCount = countAntsByRole(getSoldiers(), GameConstants.ROLE_HUNTER);
            int proteinGain = (int) (hunterCount * collectingRate);
            this.setProtein(Math.min(this.getProtein() + proteinGain, this.getProteinCapacity()));
        }
    }

    public void runConverting() {
        int farmerCount = countAntsByRole(getWorkers(), GameConstants.ROLE_FARMER);
        
        if (this.getMushrooms() >= this.getMushroomsCapacity()) return;

        if (Math.random() <= conversionRate) {
            if (this.getPlants() >= farmerCount) {
                this.setPlants(this.getPlants() - farmerCount);
                this.setMushrooms(Math.min(this.getMushrooms() + farmerCount, this.getMushroomsCapacity()));
            }
        }
        if (this.getMushrooms() >= this.getMushroomsCapacity()) return;

        if (Math.random() <= conversionRate) {
            if (this.getProtein() >= farmerCount) {
                this.setProtein(this.getProtein() - farmerCount);
                int mushroomGain = farmerCount * 2;
                this.setMushrooms(Math.min(this.getMushrooms() + mushroomGain, this.getMushroomsCapacity()));
            }
        }
    }

    public void runRanching() {
        if (!hasUpgrade(GameUnlocks.ROLE_RANCHER)) return;
        int syrupGain = (int) (aphids); 
        this.setSyrups(Math.min(this.getSyrups() + syrupGain, this.getSyrupsCapacity()));
    }

    public void runHerding() {
         if (!hasUpgrade(GameUnlocks.ROLE_RANCHER)) return;
        int rancherCount = countAntsByRole(getWorkers(), GameConstants.ROLE_RANCHER);
        int maxSustainableAphids = aphidCapacity * rancherCount;
        
        if (aphids < maxSustainableAphids) {
            this.setAphids(Math.min(this.getAphids() + rancherCount, maxSustainableAphids));
        } else if (aphids > maxSustainableAphids) {
            this.aphids = maxSustainableAphids;
        }
    }

    public void runBuilding() {
        if (currentBuildingProject == null) {
            return;
        }
        
        int builderCount = getAssignedRoleCount(GameConstants.ROLE_BUILDER);
        if (builderCount <= 0) {
            return;
        }
        
        double efficiency = builderCount / 100.0;
        if (efficiency <= 0) {
            return;
        }

        this.buildingProgressHours += 1.0; 
        
        double requiredHours = currentBuildingProject.getBuildTime() / efficiency;
        
        if (this.buildingProgressHours >= requiredHours) {
            this.buildings.add(currentBuildingProject);
            
            this.currentBuildingProject = null;
            this.buildingProgressHours = 0.0;
        }
    }

    // --- Job Packer --- 
    public void runMinutelyJobs() {
        this.runConverting();
    }

    public void runHourlyJobs() {
        this.runRoleAssignment();
        this.runCollecting();;
        this.runLaying();
        this.runResearch();
        this.runRanching();
        this.runBuilding();
    }

    public void runDailyJobs() {
        this.rankUp();
        this.runEating();
        this.runHatching();
        this.runAging();
        this.runNursing();
        this.runGraveKeeping();
        this.runHerding();
    }

    public void runMonthlyJobs() { 
        this.runInfection();
    }

    public void runYearlyJobs() {
        this.runNuptial();
    }
}