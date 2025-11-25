package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList; 
import java.awt.Rectangle; 

import com.grimidk.formicempire.classes.entities.services.ColonyStatsService;
import com.grimidk.formicempire.classes.entities.services.ColonyLabourService;
import com.grimidk.formicempire.classes.entities.services.ColonyPopulationService;
import com.grimidk.formicempire.classes.entities.services.ColonyPhysicsService;
import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;


public class Colony {
    // --- Basic Data ---
    private final int id;
    private String name;
    private Species species;
    private boolean isPlayer;
    private ColonyRank rank;
    
    // --- Population Data ---
    private final Map<AntType, List<Ant>> antGroups;
    private final List<Ant> deadAnts;
    // New list for general Bugs (Aphids, etc)
    private final List<Bug> bugs; 
    
    private Map<AntRole, Integer> assignedRoleCounts;
    private final Set<Upgrade> upgrades;
    private final Set<Building> buildings;

    // --- Resource Data ---
    private int plants;
    private int mushrooms;
    private int protein;
    private int water;
    private int syrups;
    private int resins;
    private int minerals;
    private int aphids; // Kept for count/save compatibility, but logic moves to 'bugs' list
    private int researchPoints;

    // --- Hatch Rate Data ---
    private float hatchRateWorker;
    private float hatchRateSoldier;
    private float hatchRateMajor;
    private float hatchRateDrone;
    private float hatchRatePrincess;

    // --- Misc. Data ---
    private int totalDeaths;
    private int gameAreaWidth = 1;
    private int gameAreaHeight = 1;
    private Building currentBuildingProject = null;
    private double buildingProgressHours = 0.0;
    private final List<String> eventLog = new ArrayList<>();

    // --- Room Bounds ---
    private Rectangle entranceBounds;
    private Rectangle storageBounds; 
    private Rectangle farmBounds;    
    private Rectangle nurseryBounds; 
    private Rectangle royalBounds;   
    private Rectangle rancherBounds; 
    private Rectangle graverBounds;  

    // --- Service Dependencies ---
    private transient ColonyStatsService statsService;
    private transient ColonyLabourService labourService;
    private transient ColonyPopulationService populationService;
    private transient ColonyPhysicsService physicsService;

    // --- Service Initializer ---
    private void initializeServices() {
        this.statsService = new ColonyStatsService();
        this.labourService = new ColonyLabourService();
        this.populationService = new ColonyPopulationService();
        this.physicsService = new ColonyPhysicsService();
    }

    // --- Initialization Methods ---
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
        this.researchPoints = 0;
        this.plants = 0;
        this.mushrooms = 0;
        this.protein = 0;           
        this.water = 0;
        this.syrups = 0;
        this.resins = 0;
        this.minerals = 0;
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
        List<Integer> unlockedBuildingIds = savefile.getUnlockedBuildingIds(); 
        if (unlockedBuildingIds == null || unlockedBuildingIds.isEmpty()) {
            initializeBuildings();
            return;
        }
        Map<Integer, Building> allBuildings = new HashMap<>();
        for (Building up : GameUnlocks.getBuildings()) {
            allBuildings.put(up.getId(), up);
        }
        for (Integer id : unlockedBuildingIds) {
            Building buildingToUnlock = allBuildings.get(id);
            if (buildingToUnlock != null) {
                this.buildings.add(buildingToUnlock);
            }
        }
    }

    // --- Constructors ---
    public Colony(int id, String name, boolean isPlayer) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<>();
        this.deadAnts = new CopyOnWriteArrayList<>(); 
        this.bugs = new CopyOnWriteArrayList<>();
        this.upgrades = new HashSet<>();
        this.buildings = new HashSet<>();
        
        initializeLists();
        initializeDefaults();
        initializeUpgrades();
        initializeBuildings();
        initializeAssignedRoles();
        
        initializeServices(); 
    }

    public Colony(Savefile savefile) {
        this.id = savefile.getColonyId() > 0 ? savefile.getColonyId() : savefile.getId();
        this.name = savefile.getColonyName() != null && !savefile.getColonyName().isEmpty() ? savefile.getColonyName() : savefile.getName();
        this.isPlayer = true;
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<>();
        this.deadAnts = new CopyOnWriteArrayList<>();
        this.bugs = new CopyOnWriteArrayList<>();
        this.upgrades = new HashSet<>();
        this.buildings = new HashSet<>();

        initializeLists();
        initializeDefaults(); 
        loadUpgrades(savefile);
        laodBuildings(savefile);
        initializeAssignedRoles(); 

        initializeServices(); 
        
        Map<String, Integer> savedRoles = savefile.getAssignedRoleCounts();
        if (savedRoles != null && !savedRoles.isEmpty()) {
            for (AntRole role : GameConstants.getAntRoles()) {
                Integer count = savedRoles.get(role.getName());
                if (count != null) {
                    this.assignedRoleCounts.put(role, count);
                }
            }
        }
        
        this.hatchRateWorker = savefile.getHatchRateWorker();
        this.hatchRateSoldier = savefile.getHatchRateSoldier();
        this.hatchRateMajor = savefile.getHatchRateMajor();
        this.hatchRateDrone = savefile.getHatchRateDrone();
        this.hatchRatePrincess = savefile.getHatchRatePrincess();

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

        this.plants = savefile.getPlants();
        this.mushrooms = savefile.getMushrooms();
        this.protein = savefile.getProtein();
        this.water = savefile.getWater();
        this.syrups = savefile.getSyrups();
        this.resins = savefile.getResins();
        this.minerals = savefile.getMinerals();

        this.aphids = savefile.getAphids();
        
        // Populate actual Bug objects for Aphids based on save count
        for(int i=0; i<this.aphids; i++) {
            this.bugs.add(new Bug(GameConstants.TYPE_APHID));
        }
        
        this.researchPoints = savefile.getResearchPoints();

        runRoleAssignment();
    }
        
    // --- Population Initializer ---
    private void populateAntList(List<Ant> list, int count, AntType type) {
        for (int i = 0; i < count; i++) {
            Ant newAnt = new Ant(this, type);
            
            if (type == GameConstants.TYPE_EGG || 
                type == GameConstants.TYPE_LARVA || 
                type == GameConstants.TYPE_PUPA || 
                type == GameConstants.TYPE_QUEEN) {
                newAnt.setDimension(1);
            } 
            else {
                newAnt.setDimension(0); 
            }
            list.add(newAnt);
        }
    }
    
    private void randomizeAllAntPositions() {
        if (physicsService == null) initializeServices(); 
        physicsService.randomizeAllAntPositions(this);
    }
    
    // --- Event Log  ---
    public void logEvent(String message) {
        synchronized (eventLog) {
            eventLog.add(message);
        }
    }

    public List<String> consumeEvents() {
        List<String> consumed;
        synchronized (eventLog) {
            consumed = new ArrayList<>(eventLog);
            eventLog.clear();
        }
        return consumed;
    }

    // --- Getters/Setters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }
    public boolean isPlayer() { return isPlayer; }
    public void setIsPlayer(boolean isPlayer) { this.isPlayer = isPlayer; }
    public ColonyRank getRank() { return rank; }
    public void setRank(ColonyRank rank) { this.rank = rank; }

    public Map<AntType, List<Ant>> getAntGroups() { return antGroups; } 
    public List<Ant> getAntsByType(AntType type) { return antGroups.getOrDefault(type, new CopyOnWriteArrayList<>()); }
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
    
    // Getter for general bugs
    public List<Bug> getBugs() { return bugs; }
    
    public int getAntTotal() {
        return antGroups.values().stream().mapToInt(List::size).sum();
    }
    public boolean hasUpgrade(Upgrade upgrade) { return this.upgrades.contains(upgrade); }
    public void unlockUpgrade(Upgrade upgrade) { this.upgrades.add(upgrade); }
    public Set<Upgrade> getUnlockedUpgrades() { return this.upgrades; }
    public boolean hasBuilding(Building building) { return this.buildings.contains(building); }
    public void unlockBuilding(Building building) { this.buildings.add(building); }
    public Set<Building> getUnlockedBuildings() { return this.buildings; }
    public Building getCurrentBuildingProject() { return currentBuildingProject; }
    public void setCurrentBuildingProject(Building b) { this.currentBuildingProject = b; }
    public double getBuildingProgressHours() { return buildingProgressHours; }
    public void setBuildingProgressHours(double d) { this.buildingProgressHours = d; }

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

    public int getAphids() { return aphids; }
    
    // Updates both the count and the bug list to stay in sync
    public void setAphids(int count) { 
        if (count > this.aphids) {
            int diff = count - this.aphids;
            for(int i=0; i<diff; i++) this.bugs.add(new Bug(GameConstants.TYPE_APHID));
        } else if (count < this.aphids) {
            int diff = this.aphids - count;
            // Remove aphids from the bug list
            for(int i=0; i<diff; i++) {
                for(Bug b : this.bugs) {
                    if (b.getBugType() == GameConstants.TYPE_APHID) {
                        this.bugs.remove(b);
                        break;
                    }
                }
            }
        }
        this.aphids = count; 
    }
    
    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }

    public void setGameAreaDimensions(int width, int height) {
        boolean firstTimeUpdate = (this.gameAreaWidth == 1 && this.gameAreaHeight == 1 && width > 1 && height > 1);
        this.gameAreaWidth = width;
        this.gameAreaHeight = height;
        if (firstTimeUpdate) {
            randomizeAllAntPositions();
        }
    }
    public int getGameAreaWidth() { return this.gameAreaWidth; }
    public int getGameAreaHeight() { return this.gameAreaHeight; }

    public int getAssignedRoleCount(AntRole role) { return assignedRoleCounts.getOrDefault(role, 0); }
    public void setAssignedRoleCount(AntRole role, int count) { if (count >= 0) assignedRoleCounts.put(role, count); }
    public Map<AntRole, Integer> getAssignedRoleCounts() { return assignedRoleCounts; }
    
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

    public int getTotalDeaths () { return totalDeaths; }
    public void setTotalDeaths (int totalDeaths) { this.totalDeaths = totalDeaths; }
    
    // --- Room Bounds Getters/Setters ---
    public void setRoomBounds(Rectangle entrance, Rectangle storage, Rectangle farm, Rectangle nursery, Rectangle royal, Rectangle rancher, Rectangle graver) {
        this.entranceBounds = entrance;
        this.storageBounds = storage;
        this.farmBounds = farm;
        this.nurseryBounds = nursery;
        this.royalBounds = royal;
        this.rancherBounds = rancher;
        this.graverBounds = graver;
    }
    
    public Rectangle getEntranceBounds() { return entranceBounds; }
    public Rectangle getStorageBounds() { return storageBounds; }
    public Rectangle getFarmBounds() { return farmBounds; }
    public Rectangle getNurseryBounds() { return nurseryBounds; }
    public Rectangle getRoyalBounds() { return royalBounds; }
    public Rectangle getRancherBounds() { return rancherBounds; }
    public Rectangle getGraverBounds() { return graverBounds; }
    
    public Rectangle getTargetRoomForAnt(Ant ant) {
        if (ant.getAntType() == GameConstants.TYPE_QUEEN) return royalBounds;
        if (ant.getAntType() == GameConstants.TYPE_EGG || ant.getAntType() == GameConstants.TYPE_LARVA || ant.getAntType() == GameConstants.TYPE_PUPA) return nurseryBounds;
        
        AntRole role = ant.getRole();
        if (role == null) return storageBounds; 
        
        if (role == GameConstants.ROLE_NURSE) return nurseryBounds;
        if (role == GameConstants.ROLE_FARMER) return farmBounds;
        if (role == GameConstants.ROLE_FORAGER || role == GameConstants.ROLE_HUNTER) return storageBounds;
        if (role == GameConstants.ROLE_RANCHER && rancherBounds != null) return rancherBounds;
        if (role == GameConstants.ROLE_GRAVER && graverBounds != null) return graverBounds;
        
        return storageBounds; // Default
    }

    // --- Public getters for services ---
    public ColonyStatsService getStatsService() { return this.statsService; }
    public ColonyLabourService getLabourService() { return this.labourService; }
    public ColonyPopulationService getPopulationService() { return this.populationService; }
    public ColonyPhysicsService getPhysicsService() { return this.physicsService; }

    public int getTotalConsumption(){ return statsService.getTotalConsumption(this); }
    public int getTotalProduction(){ return statsService.getTotalProduction(this); }
    public int getPlantsCapacity() { return statsService.getPlantsCapacity(this); }
    public int getMushroomsCapacity() { return statsService.getMushroomsCapacity(this); }
    public int getProteinCapacity() { return statsService.getProteinCapacity(this); }
    public int getWaterCapacity() { return statsService.getWaterCapacity(this); }
    public int getSyrupsCapacity() { return statsService.getSyrupsCapacity(this); }
    public int getResinsCapacity() { return statsService.getResinsCapacity(this); }
    public int getMineralsCapacity() { return statsService.getMineralsCapacity(this); }
    public int getEggsCapacity() { return statsService.getEggsCapacity(this); }
    public int getQueensCapacity() { return statsService.getQueensCapacity(this); }
    public int getAphidCapacity() { return statsService.getAphidCapacity(this); }
    public int getResearchSpeed() { return statsService.getResearchSpeed(this); }
    public int getGrowthTime() { return statsService.getGrowthTime(this); }
    public float getLayingRate() { return statsService.getLayingRate(this); }
    public float getConversionRate() { return statsService.getConversionRate(this); }
    public float getNursingRate() { return statsService.getNursingRate(this); }
    public float getGravingRate() { return statsService.getGravingRate(this); }
    public float getCollectingRate() { return statsService.getCollectingRate(this); }
    public float getParasiteDetection() { return statsService.getParasiteDetection(this); }
    public int getBaseHealth() { return statsService.getBaseHealth(this); }
    public int getBaseTempRes() { return statsService.getBaseTempRes(this); }
    public int getBaseRegen() { return statsService.getBaseRegen(this); }
    public int getBaseConsumption() { return statsService.getBaseConsumption(this); }
    public int getBaseAttack() { return statsService.getBaseAttack(this); }
    public int getBaseAttackSpeed() { return statsService.getBaseAttackSpeed(this); }
    public int getBaseDefense() { return statsService.getBaseDefense(this); }
    public int getBaseSpeed() { return statsService.getBaseSpeed(this); }
    public int getBaseSize(){ return statsService.getBaseSize(this); }

    // --- Colony Setup ---
    public void startColony() {
        List<Ant> workerList = getWorkers();
        for (int i = 0; i < 9; i++) {
            Ant worker = new Ant(this, GameConstants.TYPE_WORKER);
            worker.setDimension(0);
            workerList.add(worker);
        }
        Ant queen = new Ant(this, GameConstants.TYPE_QUEEN);
        queen.setDimension(1); 
        getQueens().add(queen);
        
        setAssignedRoleCount(GameConstants.ROLE_LAYER, 1);
        setAssignedRoleCount(GameConstants.ROLE_NURSE, 3);
        setAssignedRoleCount(GameConstants.ROLE_FARMER, 1);
        setAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);
        this.getQueens().get(0).setRole(GameConstants.ROLE_LAYER);
        
        // Assign roles and dimensions
        this.getWorkers().get(0).setRole(GameConstants.ROLE_NURSE);
        this.getWorkers().get(0).setDimension(1);
        
        this.getWorkers().get(1).setRole(GameConstants.ROLE_NURSE);
        this.getWorkers().get(1).setDimension(1);
        
        this.getWorkers().get(2).setRole(GameConstants.ROLE_FARMER);
        this.getWorkers().get(2).setDimension(1);
        
        this.getWorkers().get(3).setRole(GameConstants.ROLE_NURSE);
        this.getWorkers().get(3).setDimension(1);
        
        this.getWorkers().get(4).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(4).setDimension(0);
        
        this.getWorkers().get(5).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(5).setDimension(0);
        
        this.getWorkers().get(6).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(6).setDimension(0);
        
        this.getWorkers().get(7).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(7).setDimension(0);
        
        this.getWorkers().get(8).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(8).setDimension(0);
    }

    // --- Simulation Logic Methods ---
    public void runRoleAssignment() { populationService.runRoleAssignment(this); }
    public void runHatching(){ populationService.runHatching(this); }
    public void rankUp() { populationService.rankUp(this); }
    public void runLaying() { labourService.runLaying(this); }
    public void runAging(){ populationService.runAging(this); }
    public void runNursing() { labourService.runNursing(this); }
    public void runNuptial() { labourService.runNuptial(this); }
    public void runEating(Temperature currentTemp){ populationService.runEating(this, currentTemp); }
    public void runGraveKeeping() { labourService.runGraveKeeping(this); }
    public void runResearch() { labourService.runResearch(this); }
    public void runBuilding() { labourService.runBuilding(this); }
    public void runInfection() { populationService.runInfection(this); }
    public void runCollecting() { labourService.runCollecting(this); }
    public void runConverting() { labourService.runConverting(this); }
    public void runRanching() { labourService.runRanching(this); }
    public void runHerding() { labourService.runHerding(this); }
    
    public void runPhysics(int activeDimension) { 
        physicsService.runPhysics(this, activeDimension); 
    }

    // --- Job Schedulers ---
    public void runMinutelyJobs() {
        this.runConverting();
    }

    public void runHourlyJobs() {
        this.runRoleAssignment();
        this.runCollecting();
        this.runLaying();
        this.runResearch();
        this.runRanching();
        this.runBuilding();
    }

    public void runDailyJobs(Temperature currentTemp) {
        this.rankUp();
        this.runEating(currentTemp);
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