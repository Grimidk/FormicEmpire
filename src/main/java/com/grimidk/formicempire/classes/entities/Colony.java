package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
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
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

public class Colony {
    
    // --- Basic Data ---
    private final int id;
    private Civilization civilization;
    private String name;
    private boolean isPlayer;
    private ColonyRank rank;
    private boolean isActive;
    private boolean automationEnabled = false; 
    private boolean isPrimary = false;
    private int age;
    
    // --- Population Data ---
    private final Map<AntType, List<Ant>> antGroups;
    private final List<Ant> deadAnts;
    private final List<Bug> bugs; 
    
    private Map<AntRole, Integer> assignedRoleCounts;
    private final Set<Building> buildings;

    // --- Resource Data ---
    private double plants;
    private double mushrooms;
    private double protein;
    private double water;
    private double syrups;
    private double resins;
    private double minerals;
    
    private int aphids; 
    private int parasites;

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
    private transient ColonyAutomationService automationService;

    // --- Service Initializer ---
    private void initializeServices() {
        this.statsService = new ColonyStatsService();
        this.labourService = new ColonyLabourService();
        this.populationService = new ColonyPopulationService();
        this.physicsService = new ColonyPhysicsService();
        this.locationService = new ColonyLocationService();
        this.sumarizationService = new ColonySumarizationService();
        this.automationService = new ColonyAutomationService(); 
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
        this.automationEnabled = !isPlayer;
        this.totalDeaths = 0;
        this.age = 0;
        this.antGroups = new HashMap<>();
        this.deadAnts = new CopyOnWriteArrayList<>(); 
        this.bugs = new CopyOnWriteArrayList<>();
        this.buildings = new HashSet<>();
        
        initializeLists();
        initializeDefaults();
        initializeBuildings();
        initializeAssignedRoles();
        initializeServices(); 
    }

    public Colony(Savefile.SavedColony savedColony) {
        this.id = savedColony.id;
        this.name = savedColony.name;
        this.isPlayer = savedColony.isPlayer;
        
        this.isPrimary = savedColony.isPrimary;
        this.automationEnabled = savedColony.isAutomated;
        this.age = savedColony.age;
        this.totalDeaths = savedColony.totalDeaths;
        
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<>();
        this.deadAnts = new CopyOnWriteArrayList<>();
        this.bugs = new CopyOnWriteArrayList<>();
        this.buildings = new HashSet<>();

        initializeLists();
        initializeDefaults(); 
        loadBuildings(savedColony);
        initializeAssignedRoles(); 
        initializeServices(); 
        
        if (this.populationService != null && savedColony.localDeathStatistics != null) {
            this.populationService.loadDeathStatistics(savedColony.localDeathStatistics);
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
        rankUp();
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
        if (populationService != null) {
            populationService.recordDeath(cause, this);
        }
    }

    // --- Getters/Setters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean isPrimary) { this.isPrimary = isPrimary; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public Civilization getCivilization() { return civilization; }
    public void setCivilization(Civilization civilization) { 
        this.civilization = civilization; 
        if (civilization != null) {
            if (!civilization.getColonies().contains(this)) {
                civilization.addColony(this); 
            }
            refreshAntStats();
        }
    }

    // Delegates to Civilization
    public Species getSpecies() { 
        return civilization != null ? civilization.getSpecies() : GameConstants.SPECIES_OMNI; 
    }
    public void setSpecies(Species species) { 
        if (civilization != null) civilization.setSpecies(species); 
    }
    
    public int getResearchPoints() { 
        return civilization != null ? civilization.getResearchPoints() : 0; 
    }
    public void setResearchPoints(int points) { 
        if (civilization != null) civilization.setResearchPoints(points); 
    }
    public void addResearchPoints(int amount) {
        if (civilization != null) civilization.addResearchPoints(amount);
    }
    
    public boolean hasUpgrade(Upgrade upgrade) {
        return civilization != null && civilization.hasUpgrade(upgrade);
    }
    public void unlockUpgrade(Upgrade upgrade) {
        if (civilization != null) civilization.unlockUpgrade(upgrade);
    }
    public Set<Upgrade> getUnlockedUpgrades() {
        return civilization != null ? civilization.getUnlockedUpgrades() : new HashSet<>();
    }
    
    public boolean isPlayer() { return isPlayer; }
    public void setIsPlayer(boolean isPlayer) { this.isPlayer = isPlayer; }
    public ColonyRank getRank() { return rank; }
    public void setRank(ColonyRank rank) { this.rank = rank; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public boolean isAutomationEnabled() { return automationEnabled; }
    public void setAutomationEnabled(boolean automationEnabled) { this.automationEnabled = automationEnabled; }

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
    
    // --- Resource Getters/Setters ---
    public int getPlants() { return (int) plants; }
    public int getMushrooms() { return (int) mushrooms; }
    public int getProtein() { return (int) protein; }
    public int getWater() { return (int) water; }
    public int getSyrups() { return (int) syrups; }
    public int getResins() { return (int) resins; }
    public int getMinerals() { return (int) minerals; }

    public double getPlantsPrecise() { return plants; }
    public double getMushroomsPrecise() { return mushrooms; }
    public double getProteinPrecise() { return protein; }
    public double getWaterPrecise() { return water; }
    public double getSyrupsPrecise() { return syrups; }
    public double getResinsPrecise() { return resins; }
    public double getMineralsPrecise() { return minerals; }
    
    public void setPlants(double plants) { 
        this.plants = Math.max(0, Math.min(plants, (double)getPlantsCapacity())); 
    }
    public void setMushrooms(double mushrooms) { 
        this.mushrooms = Math.max(0, Math.min(mushrooms, (double)getMushroomsCapacity())); 
    }
    public void setProtein(double protein) { 
        this.protein = Math.max(0, Math.min(protein, (double)getProteinCapacity())); 
    }
    public void setWater(double water) { 
        this.water = Math.max(0, Math.min(water, (double)getWaterCapacity())); 
    }
    public void setSyrups(double syrups) { 
        this.syrups = Math.max(0, Math.min(syrups, (double)getSyrupsCapacity())); 
    }
    public void setResins(double resins) { 
        this.resins = Math.max(0, Math.min(resins, (double)getResinsCapacity())); 
    }
    public void setMinerals(double minerals) { 
        this.minerals = Math.max(0, Math.min(minerals, (double)getMineralsCapacity())); 
    }

    public int getAphids() { return aphids; }
    public void setAphids(int count) { 
        int rancherCount = getAssignedRoleCount(GameConstants.ROLE_RANCHER);
        int maxAphids = Integer.MAX_VALUE;
        if (statsService != null) {
            maxAphids = statsService.getAphidCapacity(this) * rancherCount;
        }
        
        this.aphids = Math.max(0, Math.min(count, maxAphids)); 
        
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
        } else if (currentAphids > this.aphids) {
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
    public ColonyAutomationService getAutomationService() { return this.automationService; }

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
    public void runNuptial(World world, Hex currentHex) { labourService.runNuptial(this, world, currentHex); }
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
    
    public int getNuptialFlightCost() {
        int base = 1000;
        int multiplier = 1;
        if (civilization != null) {
            multiplier = (int) Math.pow(2, civilization.getColonies().size());
        }
        if (multiplier < 1) multiplier = 1;
        return base * multiplier;
    }

    public void forceNuptialFlight(World world, Hex currentHex) {
        if (!hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT)) return;
        
        int cost = getNuptialFlightCost();
        if (getResearchPoints() < cost) return;
        
        boolean hasDrones = !getDrones().isEmpty();
        boolean hasBreeders = getPrincesses().stream().anyMatch(p -> p.getRole() == GameConstants.ROLE_BREEDER);
        
        if (!hasDrones || !hasBreeders) {
            logEvent("Cannot force flight. Missing Drones or Breeder Princesses.");
            return;
        }

        addResearchPoints(-cost);
        this.labourService.runNuptial(this, world, currentHex);
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
        if (this.age < 7) {
            return;
        }

        if (this.isActive) {
            if (this.automationEnabled) {
                this.automationService.runAutomation(this);
            }
            this.runRoleAssignment();
            this.runLaying();
            this.runResearch();
            this.runRanching();
            this.runBuilding();
            this.runCollecting(); 
        } else {
            if (this.automationEnabled) {
                this.automationService.runAutomation(this);
            }
            this.populationService.runRoleAssignment(this); 
            this.sumarizationService.runHourlyLite(this);
        }
    }

    public void runDailyJobs(Temperature currentTemp, Biome biome) {
        if (this.age < 7) {
            this.age++;
            if (this.age >= 7) {
                matureColony();
            }
            return;
        }

        if (this.automationEnabled) {
            this.automationService.runDailyAutomation(this);
        }

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

        this.age++;
    }

    public void matureColony() {
        System.out.println("[Colony] Maturation complete. Spawning workforce for " + this.getName());
        Random random = new Random();

        List<Ant> workerList = this.getWorkers();
        for (int i = 0; i < 9; i++) {
            Ant worker = new Ant(this, GameConstants.TYPE_WORKER);
            workerList.add(worker);
        }

        configureWorker(0, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        configureWorker(1, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        configureWorker(2, GameConstants.ROLE_FARMER, WorldSpaces.UNDERWORLD);
        configureWorker(3, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        configureWorker(4, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        configureWorker(5, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        configureWorker(6, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        configureWorker(7, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        configureWorker(8, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);

        this.setAssignedRoleCount(GameConstants.ROLE_LAYER, 1);
        this.setAssignedRoleCount(GameConstants.ROLE_NURSE, 3);
        this.setAssignedRoleCount(GameConstants.ROLE_FARMER, 1);
        this.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);

        if (this.locationService != null) {
            if (this.locationService.getDiscoveredSources().isEmpty()) {
                int range = 300;
                
                int centerX = ColonyLocationService.ANCHOR_CENTER_X; 
                int centerY = ColonyLocationService.ANCHOR_HEIGHT / 2;
                
                int pX = centerX + random.nextInt((range * 2) + 1) - range;
                int pY = centerY + random.nextInt((range * 2) + 1) - range;
                pX = Math.max(50, pX);
                pY = Math.max(50, pY);
                ResourceSource initialPlant = new ResourceSource(GameConstants.RESOURCE_PLANT, 10000, pX, pY);
                
                int wX = centerX + random.nextInt((range * 2) + 1) - range;
                int wY = centerY + random.nextInt((range * 2) + 1) - range;
                wX = Math.max(50, wX);
                wY = Math.max(50, wY);
                ResourceSource initialWater = new ResourceSource(GameConstants.RESOURCE_WATER, 10000, wX, wY);
                
                this.locationService.addSource(this, initialPlant);
                this.locationService.addSource(this, initialWater);
            }
        }
        
        if (this.physicsService != null) {
            this.physicsService.randomizeAllAntPositions(this);
        }

        this.logEvent("Colony Maturation Complete: Workforce deployed.");
    }
    
    private void configureWorker(int index, AntRole role, Dimension dim) {
        if (this.getWorkers() != null && index < this.getWorkers().size()) {
            Ant worker = this.getWorkers().get(index);
            worker.setRole(role);
            worker.setDimension(dim);
        }
    }

    public void runMonthlyJobs() { 
        if (this.isActive || this.isPlayer) {
            this.runParasitation();
        }
    }

    public void runYearlyJobs(World world, Hex currentHex) {
        this.runNuptial(world, currentHex);
    }
    
    public void refreshAntStats() {
        for (List<Ant> list : antGroups.values()) {
            for (Ant a : list) {
                a.updateStatsFromColony(this);
            }
        }
    }
}