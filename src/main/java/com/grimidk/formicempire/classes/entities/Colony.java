package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList; 
import java.awt.Rectangle;
import java.awt.Point; 

import com.grimidk.formicempire.classes.entities.services.*; 
import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;


public class Colony {
    
    // --- Basic Data ---
    private final int id;
    private String name;
    private Species species;
    private boolean isPlayer;
    private ColonyRank rank;
    private boolean isActive;
    
    // --- Population Data ---
    private final Map<AntType, List<Ant>> antGroups;
    private final List<Ant> deadAnts;
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
    private int aphids; 
    private int parasites;
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
    private Rectangle breederBounds; 

    // --- Service Dependencies ---
    private transient ColonyStatsService statsService;
    private transient ColonyLabourService labourService;
    private transient ColonyPopulationService populationService;
    private transient ColonyPhysicsService physicsService;
    private transient ColonyLocationService locationService;
    private transient ColonySumarizationService sumarizationService;
    private transient ColonyStatTrackingService trackingService;

    // --- Service Initializer ---
    private void initializeServices() {
        this.statsService = new ColonyStatsService();
        this.labourService = new ColonyLabourService();
        this.populationService = new ColonyPopulationService();
        this.physicsService = new ColonyPhysicsService();
        this.locationService = new ColonyLocationService();
        this.sumarizationService = new ColonySumarizationService();
        this.trackingService = new ColonyStatTrackingService(); 
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
        this.totalDeaths = 0;
        this.plants = 0;
        this.mushrooms = 0;
        this.protein = 0;           
        this.water = 0;
        this.syrups = 0;
        this.resins = 0;
        this.minerals = 0;
        this.aphids = 0;
        this.parasites = 0;
        this.hatchRateWorker = 100.0f;
        this.hatchRateSoldier = 0.0f;
        this.hatchRateMajor = 0.0f;
        this.hatchRateDrone = 0.0f;
        this.hatchRatePrincess = 0.0f;
        this.isActive = false;
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
    
    private void loadUpgrades(Savefile.SavedColony savedColony) {
        List<Integer> unlockedIds = savedColony.unlockedUpgradeIds; 
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
    
    private void loadBuildings(Savefile.SavedColony savedColony) {
        List<Integer> unlockedBuildingIds = savedColony.unlockedBuildingIds; 
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

    public Colony(Savefile.SavedColony savedColony) {
        this.id = savedColony.id;
        this.name = savedColony.name;
        this.isPlayer = savedColony.isPlayer;
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<>();
        this.deadAnts = new CopyOnWriteArrayList<>();
        this.bugs = new CopyOnWriteArrayList<>();
        this.upgrades = new HashSet<>();
        this.buildings = new HashSet<>();

        initializeLists();
        initializeDefaults(); 
        loadUpgrades(savedColony);
        loadBuildings(savedColony);
        initializeAssignedRoles(); 
        initializeServices(); 
        
        if (this.trackingService != null && savedColony.deathStatistics != null) {
            this.trackingService.loadStatistics(savedColony.deathStatistics);
        }
        
        Map<String, Integer> savedRoles = savedColony.assignedRoleCounts;
        if (savedRoles != null && !savedRoles.isEmpty()) {
            for (AntRole role : GameConstants.getAntRoles()) {
                Integer count = savedRoles.get(role.getName());
                if (count != null) {
                    this.assignedRoleCounts.put(role, count);
                }
            }
        }
        
        this.hatchRateWorker = savedColony.hatchRateWorker;
        this.hatchRateSoldier = savedColony.hatchRateSoldier;
        this.hatchRateMajor = savedColony.hatchRateMajor;
        this.hatchRateDrone = savedColony.hatchRateDrone;
        this.hatchRatePrincess = savedColony.hatchRatePrincess;

        populateAntList(getEggs(), savedColony.eggs, GameConstants.TYPE_EGG);
        populateAntList(getLarvae(), savedColony.larvae, GameConstants.TYPE_LARVA);
        populateAntList(getPupae(), savedColony.pupae, GameConstants.TYPE_PUPA);
        populateAntList(getWorkers(), savedColony.workers, GameConstants.TYPE_WORKER);
        populateAntList(getSoldiers(), savedColony.soldiers, GameConstants.TYPE_SOLDIER);
        populateAntList(getMajors(), savedColony.majors, GameConstants.TYPE_MAJOR);
        populateAntList(getDrones(), savedColony.drones, GameConstants.TYPE_DRONE);
        populateAntList(getPrincesses(), savedColony.princesses, GameConstants.TYPE_PRINCESS);
        populateAntList(getQueens(), savedColony.queens, GameConstants.TYPE_QUEEN);
        populateAntList(deadAnts, savedColony.deadAnts, GameConstants.TYPE_DEAD);

        this.plants = savedColony.plants;
        this.mushrooms = savedColony.mushrooms;
        this.protein = savedColony.protein;
        this.water = savedColony.water;
        this.syrups = savedColony.syrups;
        this.resins = savedColony.resins;
        this.minerals = savedColony.minerals;

        this.aphids = savedColony.aphids; 
        for(int i=0; i<this.aphids; i++) {
            this.bugs.add(new Bug(GameConstants.TYPE_APHID));
        }

        this.parasites = savedColony.parasites;
        for(int i=0; i<this.parasites; i++) {
            Bug p = new Bug(GameConstants.TYPE_PARASITE);
            p.setDimension(WorldSpaces.UNDERWORLD);
            this.bugs.add(p);
        }
        
        this.researchPoints = savedColony.researchPoints;
        this.totalDeaths = savedColony.totalDeaths;
        
        if (savedColony.savedResourceSources != null && this.locationService != null) {
            List<ResourceType> allTypes = GameConstants.getResources();
            
            for (Savefile.SavedResourceSource s : savedColony.savedResourceSources) {
                ResourceType type = null;
                for (ResourceType rt : allTypes) {
                    if (rt.getId() == s.typeId) {
                        type = rt;
                        break;
                    }
                }
                
                if (type != null) {
                    ResourceSource rs = new ResourceSource(
                        type,
                        s.currentQuantity,
                        s.initialQuantity,
                        s.x,
                        s.y
                    );
                    this.locationService.addSource(this, rs);
                }
            }
        }

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
                newAnt.setDimension(WorldSpaces.UNDERWORLD);
            } 
            else {
                newAnt.setDimension(WorldSpaces.OVERWORLD); 
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

    // --- Death Tracking Wrapper ---
    public void recordAntDeath(Ant ant, String cause) {
        if (ant == null) return;
        this.totalDeaths++;
        this.deadAnts.add(ant);
        if (trackingService != null) {
            trackingService.recordDeath(cause);
        }
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
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

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
    
    // --- Resource Getters/Setters with Clamping ---
    public int getPlants() { return plants; }
    public void setPlants(int plants) { 
        this.plants = Math.max(0, Math.min(plants, getPlantsCapacity())); 
    }
    public int getMushrooms() { return mushrooms; }
    public void setMushrooms(int mushrooms) { 
        this.mushrooms = Math.max(0, Math.min(mushrooms, getMushroomsCapacity())); 
    }
    public int getProtein() { return protein; }
    public void setProtein(int protein) { 
        this.protein = Math.max(0, Math.min(protein, getProteinCapacity())); 
    }
    public int getWater() { return water; }
    public void setWater(int water) { 
        this.water = Math.max(0, Math.min(water, getWaterCapacity())); 
    }
    public int getSyrups() { return syrups; }
    public void setSyrups(int syrups) { 
        this.syrups = Math.max(0, Math.min(syrups, getSyrupsCapacity())); 
    }
    public int getResins() { return resins; }
    public void setResins(int resins) { 
        this.resins = Math.max(0, Math.min(resins, getResinsCapacity())); 
    }
    public int getMinerals() { return minerals; }
    public void setMinerals(int minerals) { 
        this.minerals = Math.max(0, Math.min(minerals, getMineralsCapacity())); 
    }

    public int getAphids() { return aphids; }
    public void setAphids(int count) { 
        this.aphids = Math.max(0, count); 
        
        if (this.bugs.stream().filter(b -> b.getBugType() == GameConstants.TYPE_APHID).count() != this.aphids) {
            long currentAphids = this.bugs.stream().filter(b -> b.getBugType() == GameConstants.TYPE_APHID).count();
            if (currentAphids < this.aphids) {
                 int diff = this.aphids - (int)currentAphids;
                 Rectangle yard = getRancherBounds();
                 if (yard == null) yard = new Rectangle(10, 10, 256, 256); 
                 for(int i=0; i<diff; i++) {
                    Bug newBug = new Bug(GameConstants.TYPE_APHID);
                    if (physicsService != null) {
                        Point spawnPos = physicsService.getSpecificRoomPoint(this, yard);
                        newBug.setPosition(spawnPos);
                    }
                    this.bugs.add(newBug);
                }
            } else {
                int diff = (int)currentAphids - this.aphids;
                for(int i=0; i<diff; i++) {
                    for(Bug b : this.bugs) {
                        if (b.getBugType() == GameConstants.TYPE_APHID) {
                            this.bugs.remove(b);
                            break;
                        }
                    }
                }
            }
        }
    }

    public int getParasites() { return parasites; }
    public void setParasites(int count) { 
        this.parasites = Math.max(0, count);
        
        long currentParasites = this.bugs.stream().filter(b -> b.getBugType() == GameConstants.TYPE_PARASITE).count();
        if (currentParasites < this.parasites) {
             int diff = this.parasites - (int)currentParasites;
             Rectangle spawnRoom = getStorageBounds();
             if (spawnRoom == null) spawnRoom = new Rectangle(0, 0, 256, 256);

             for(int i=0; i<diff; i++) {
                Bug newBug = new Bug(GameConstants.TYPE_PARASITE);
                newBug.setDimension(WorldSpaces.UNDERWORLD);
                if (physicsService != null) {
                    Point spawnPos = physicsService.getSpecificRoomPoint(this, spawnRoom);
                    newBug.setPosition(spawnPos);
                }
                this.bugs.add(newBug);
            }
        } else if (currentParasites > this.parasites) {
            int diff = (int)currentParasites - this.parasites;
            for(int i=0; i<diff; i++) {
                for(Bug b : this.bugs) {
                    if (b.getBugType() == GameConstants.TYPE_PARASITE) {
                        this.bugs.remove(b);
                        break;
                    }
                }
            }
        }
    }

    public String getParasiteCountDisplay() {
        if (!hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            return "???";
        }
        int actual = getParasites();
        if (actual == 0) return "~0";
        
        double fuzz = Math.random() * 0.2; 
        boolean up = Math.random() > 0.5;
        
        int display = actual;
        if (up) display += (int)(actual * fuzz);
        else display -= (int)(actual * fuzz);
        
        if (display < 0) display = 0;
        
        return "~" + display;
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
    public void setRoomBounds(Rectangle entrance, Rectangle storage, Rectangle farm, Rectangle nursery, Rectangle royal, Rectangle rancher, Rectangle graver, Rectangle breeder) {
        this.entranceBounds = entrance;
        this.storageBounds = storage;
        this.farmBounds = farm;
        this.nurseryBounds = nursery;
        this.royalBounds = royal;
        this.rancherBounds = rancher;
        this.graverBounds = graver;
        this.breederBounds = breeder;
    }
    
    public void setRoomBounds(Rectangle entrance, Rectangle storage, Rectangle farm, Rectangle nursery, Rectangle royal, Rectangle rancher, Rectangle graver) {
        setRoomBounds(entrance, storage, farm, nursery, royal, rancher, graver, null);
    }
    
    public Rectangle getEntranceBounds() { return entranceBounds; }
    public Rectangle getStorageBounds() { return storageBounds; }
    public Rectangle getFarmBounds() { return farmBounds; }
    public Rectangle getNurseryBounds() { return nurseryBounds; }
    public Rectangle getRoyalBounds() { return royalBounds; }
    public Rectangle getRancherBounds() { return rancherBounds; }
    public Rectangle getGraverBounds() { return graverBounds; }
    public Rectangle getBreederBounds() { return breederBounds; }
    
    public Rectangle getTargetRoomForAnt(Ant ant) {
        AntRole role = ant.getRole();
        if (ant.getAntType() == GameConstants.TYPE_QUEEN) return royalBounds;
        if (ant.getAntType() == GameConstants.TYPE_EGG || ant.getAntType() == GameConstants.TYPE_LARVA || ant.getAntType() == GameConstants.TYPE_PUPA) return nurseryBounds;        
        if (ant.getAntType() == GameConstants.TYPE_DRONE) return breederBounds;
        if (role == GameConstants.ROLE_NURSE) return nurseryBounds;
        if (role == GameConstants.ROLE_FARMER) return farmBounds;
        if (role == GameConstants.ROLE_RANCHER && rancherBounds != null) return rancherBounds;
        if (role == GameConstants.ROLE_GRAVER && graverBounds != null) return graverBounds;
        if (role == GameConstants.ROLE_BREEDER && breederBounds != null) return breederBounds;
        if (role == GameConstants.ROLE_ASSISTANT && royalBounds != null) return royalBounds;
        
        return null; // Default - Outside
    }

    // --- Public getters for services ---
    public ColonyStatsService getStatsService() { return this.statsService; }
    public ColonyLabourService getLabourService() { return this.labourService; }
    public ColonyPopulationService getPopulationService() { return this.populationService; }
    public ColonyPhysicsService getPhysicsService() { return this.physicsService; }
    public ColonyLocationService getLocationService() { return this.locationService; }
    public ColonySumarizationService getSumarizationService() { return this.sumarizationService; }
    public ColonyStatTrackingService getTrackingService() { return this.trackingService; }

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
    public int getSourceCapacity() { return statsService.getSourceCapacity(this); }


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
    public void runContamination() { populationService.runContamination(this); }
    public void runCollecting() { labourService.runCollecting(this); }
    public void runConverting() { labourService.runConverting(this); }
    public void runRanching() { labourService.runRanching(this); }
    public void runHerding(Biome biome) { labourService.runHerding(this, biome); }
    public void runScoutting(Biome biome) { labourService.runScoutting(this, biome); }
    public void runComposting() { labourService.runComposting(this); }
    public void runParasitation() { populationService.runParasitation(this); }
    public void runPolicing() { labourService.runPolicing(this); }

    public void forceNuptialFlight() {
        if (!hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT)) return;
        if (this.researchPoints < 1000) return;
        boolean hasDrones = !getDrones().isEmpty();
        boolean hasBreeders = getPrincesses().stream().anyMatch(p -> p.getRole() == GameConstants.ROLE_BREEDER);
        
        if (!hasDrones || !hasBreeders) {
            logEvent("Cannot force flight. Missing Drones or Breeder Princesses.");
            return;
        }

        this.researchPoints -= 1000;
        this.labourService.runNuptial(this);
    }
    
    public void runPhysics(Dimension activeDimension) { 
        if (this.isActive) {
            physicsService.runPhysics(this, activeDimension); 
        }
    }

    // --- Job Schedulers ---
    public void runMinutelyJobs() {
        if (this.isActive) {
            this.runConverting();
        }
    }

    public void runHourlyJobs() {
        if (this.isActive) {
            this.runRoleAssignment();
            this.runCollecting();
            this.runLaying();
            this.runResearch();
            this.runRanching();
            this.runBuilding();
        } else {
            this.populationService.runRoleAssignment(this); 
            this.sumarizationService.runHourlyLite(this);
        }
    }

    public void runDailyJobs(Temperature currentTemp, Biome biome) {
        if (this.isActive) {
            this.rankUp();
            this.runEating(currentTemp);
            this.runHatching();
            this.runAging();
            this.runNursing();
            this.runGraveKeeping();
            this.runHerding(biome); 
            this.runScoutting(biome);
            this.runContamination(); 
            this.runComposting();
            this.runPolicing(); 
        } else {
            this.sumarizationService.runDailyLite(this);
        }
    }

    public void runMonthlyJobs() { 
        if (this.isActive || this.isPlayer) {
             this.runParasitation();
        }
    }

    public void runYearlyJobs() {
        this.runNuptial();
    }
}