package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import com.grimidk.formicempire.classes.constants.AntRole;
import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.ColonyRank;
import com.grimidk.formicempire.classes.constants.Species;
import com.grimidk.formicempire.classes.constants.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.GameUpgrades;
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

    // Resources
    private int plants;
    private int plantsCapacity;
    private int mushrooms;
    private int mushroomsCapacity;
    private int protein;
    private int proteinCapacity;
    private int water;
    private int waterCapacity; 
    private int syrups;
    private int syrupsCapacity;
    private int resins;
    private int resinsCapacity;
    private int minerals;
    private int mineralsCapacity;

    // Capacities
    private int eggsCapacity;
    private int queensCapacity;
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
    private int baseAge;
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

    private void initializeLists() {
        this.antGroups.put(GameConstants.TYPE_EGG, new ArrayList<>());
        this.antGroups.put(GameConstants.TYPE_LARVA, new ArrayList<>());
        this.antGroups.put(GameConstants.TYPE_PUPA, new ArrayList<>());
        this.antGroups.put(GameConstants.TYPE_WORKER, new ArrayList<>());
        this.antGroups.put(GameConstants.TYPE_SOLDIER, new ArrayList<>());
        this.antGroups.put(GameConstants.TYPE_MAJOR, new ArrayList<>());
        this.antGroups.put(GameConstants.TYPE_DRONE, new ArrayList<>());
        this.antGroups.put(GameConstants.TYPE_PRINCESS, new ArrayList<>());
        this.antGroups.put(GameConstants.TYPE_QUEEN, new ArrayList<>());
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
        this.conversionRate = 1.0f;
        this.nursingRate = 10f;
        this.gravingRate = 5f;
        this.collectingRate = 1f;
        this.parasiteDetection = 10;

        this.baseHealth = 100;
        this.baseAge = 180;
        this.baseTempRes = 25;
        this.baseRegen = 1;
        this.baseConsumption = 1;
        this.baseAttack = 10;
        this.baseAttackSpeed = 1;
        this.baseDefense = 5;
        this.baseSpeed = 1;
        this.baseSize = 1;

        this.plants = 0;
        this.plantsCapacity = 4000;
        this.mushrooms = 0;
        this.mushroomsCapacity = 8000;
        this.protein = 0;           
        this.proteinCapacity = 2000;
        this.water = 0;
        this.waterCapacity = 1000;
        this.syrups = 0;
        this.syrupsCapacity = 500;
        this.resins = 0;
        this.resinsCapacity = 200;
        this.minerals = 0;
        this.mineralsCapacity = 100;

        this.eggsCapacity = 50;
        this.queensCapacity = 1;
        this.aphidCapacity = 10;
        this.aphids = 0;

        this.hatchRateWorker = 100.0f;
        this.hatchRateSoldier = 0.0f;
        this.hatchRateMajor = 0.0f;
        this.hatchRateDrone = 0.0f;
        this.hatchRatePrincess = 0.0f;
    }

    private void initializeUpgrades() {
        this.upgrades.add(GameUpgrades.TYPE_EGG);
        this.upgrades.add(GameUpgrades.TYPE_QUEEN);
        this.upgrades.add(GameUpgrades.TYPE_WORKER);
        this.upgrades.add(GameUpgrades.ROLE_FORAGER);
        this.upgrades.add(GameUpgrades.ROLE_FARMER);
        this.upgrades.add(GameUpgrades.ROLE_NURSE);
        this.upgrades.add(GameUpgrades.ROLE_LAYER);
        this.upgrades.add(GameUpgrades.STAT_SKELETON);
        this.upgrades.add(GameUpgrades.STAT_ACID);
        this.upgrades.add(GameUpgrades.STAT_LONGEVITY);
    }

    private void loadUpgrades(Savefile savefile) {
        List<Integer> unlockedIds = savefile.getUnlockedUpgradeIds(); 

        if (unlockedIds == null || unlockedIds.isEmpty()) {
            initializeUpgrades();
            return;
        }

        Map<Integer, Upgrade> allUpgrades = new HashMap<>();
        for (Upgrade up : GameUpgrades.getUpgrades()) {
            allUpgrades.put(up.getId(), up);
        }

        for (Integer id : unlockedIds) {
            Upgrade upgradeToUnlock = allUpgrades.get(id);
            if (upgradeToUnlock != null) {
                this.upgrades.add(upgradeToUnlock);
            }
        }
    }

    public Colony(int id, String name, boolean isPlayer) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<>();
        this.deadAnts = new ArrayList<>();
        this.upgrades = new HashSet<>();
        
        initializeLists();
        initializeDefaults();
        initializeUpgrades();
        initializeAssignedRoles();
    }

    public Colony(Savefile savefile) {
        this.id = savefile.getColonyId() > 0 ? savefile.getColonyId() : savefile.getId();
        this.name = savefile.getColonyName() != null && !savefile.getColonyName().isEmpty() ? savefile.getColonyName() : savefile.getName();
        this.isPlayer = true;
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<>();
        this.deadAnts = new ArrayList<>();
        this.upgrades = new HashSet<>();

        initializeLists();
        initializeDefaults(); 
        loadUpgrades(savefile);
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
        
        // Resource Capacities
        this.plantsCapacity = savefile.getPlantsCapacity();
        this.mushroomsCapacity = savefile.getMushroomsCapacity();
        this.proteinCapacity = savefile.getProteinCapacity();
        this.waterCapacity = savefile.getWaterCapacity();
        this.syrupsCapacity = savefile.getSyrupsCapacity();
        this.resinsCapacity = savefile.getResinsCapacity();
        this.mineralsCapacity = savefile.getMineralsCapacity();

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
        this.eggsCapacity = savefile.getEggsCapacity();
        this.queensCapacity = savefile.getQueensCapacity();

        runRoleAssignment(); 
    }

    private void populateAntList(List<Ant> list, int count, AntType type) {
        for (int i = 0; i < count; i++) {
            list.add(new Ant(this, type));
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
        return antGroups.getOrDefault(type, new ArrayList<>());
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
    public int getPlantsCapacity() { return plantsCapacity; }
    public void setPlantsCapacity(int plantsCapacity) { this.plantsCapacity = plantsCapacity; }
    public int getMushrooms() { return mushrooms; }
    public void setMushrooms(int mushrooms) { this.mushrooms = mushrooms; }
    public int getMushroomsCapacity() { return mushroomsCapacity; }
    public void setMushroomsCapacity(int mushroomsCapacity) { this.mushroomsCapacity = mushroomsCapacity; }
    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }
    public int getProteinCapacity() { return proteinCapacity; }
    public void setProteinCapacity(int proteinCapacity) { this.proteinCapacity = proteinCapacity; }
    public int getWater() { return water; }
    public void setWater(int water) { this.water = water; }
    public int getWaterCapacity() { return waterCapacity; }
    public void setWaterCapacity(int waterCapacity) { this.waterCapacity = waterCapacity; }
    public int getSyrups() { return syrups; }
    public void setSyrups(int syrups) { this.syrups = syrups; }
    public int getSyrupsCapacity() { return syrupsCapacity; }
    public void setSyrupsCapacity(int syrupsCapacity) { this.syrupsCapacity = syrupsCapacity; }
    public int getResins() { return resins; }
    public void setResins(int resins) { this.resins = resins; }
    public int getResinsCapacity() { return resinsCapacity; }
    public void setResinsCapacity(int resinsCapacity) { this.resinsCapacity = resinsCapacity; }
    public int getMinerals() { return minerals; }
    public void setMinerals(int minerals) { this.minerals = minerals; }
    public int getMineralsCapacity() { return mineralsCapacity; }
    public void setMineralsCapacity(int mineralsCapacity) { this.mineralsCapacity = mineralsCapacity; }

    // Other Capacities
    public int getEggsCapacity() { return eggsCapacity; }
    public void setEggsCapacity(int eggsCapacity) { this.eggsCapacity = eggsCapacity; }
    public int getQueensCapacity() { return queensCapacity; }
    public void setQueensCapacity(int queensCapacity) { this.queensCapacity = queensCapacity; }
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
    public int getBaseAge() { return baseAge; }
    public void setBaseAge(int baseAge) { this.baseAge = baseAge; }
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
            workerList.add(new Ant(this, GameConstants.TYPE_WORKER));
        }
        getQueens().add(new Ant(this, GameConstants.TYPE_QUEEN));

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

        if (hasUpgrade(GameUpgrades.TYPE_SOLDIER)) {
            cumulative += this.hatchRateSoldier;
            if (rand < cumulative) {
                return GameConstants.TYPE_SOLDIER;
            }
        }
        
        if (hasUpgrade(GameUpgrades.TYPE_MAJOR)) {
            cumulative += this.hatchRateMajor;
            if (rand < cumulative) {
                return GameConstants.TYPE_MAJOR;
            }
        }

        if (hasUpgrade(GameUpgrades.TYPE_PRINCESS)) {
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
        Iterator<Ant> iterator = sourceList.iterator();
        while (iterator.hasNext()) {
            Ant ant = iterator.next();
            if (ant.getAge() >= this.getGrowthTime()) { 
                ant.transform(this, newType);
                destList.add(ant);
                iterator.remove(); 
            }
        }
    }

    private void hatchPupae() {
        Iterator<Ant> iterator = getPupae().iterator();
        while (iterator.hasNext()) {
            Ant pupa = iterator.next();
            if (pupa.getAge() < this.getGrowthTime()) {
                continue;
            }

            AntType newType = determineHatchType();
            pupa.transform(this, newType);
            
            antGroups.get(newType).add(pupa);
            iterator.remove();
        }
    }

    // --- Routine Colony Activities ---
    public void runHatching(){    
        hatchPupae();
        evolveAnts(getLarvae(), getPupae(), GameConstants.TYPE_PUPA);
        evolveAnts(getEggs(), getLarvae(), GameConstants.TYPE_LARVA);
    }

    public void runEating(){
        //Water
        List<Ant> thirstyAnts = new ArrayList<>();
        int waterAvailable = this.getWater();
        List<AntType> eatOrder = Arrays.asList(
             GameConstants.TYPE_QUEEN,
            GameConstants.TYPE_WORKER,
            GameConstants.TYPE_LARVA,
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_PRINCESS,
            GameConstants.TYPE_DRONE
        );
        
        for (AntType type : eatOrder) {
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

        //Food
        int mushroomsAvailable = this.getMushrooms();
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

        //Syrup
        Set<Ant> antsInNeed = new HashSet<>(thirstyAnts);
        antsInNeed.addAll(hungryAnts);
        
        int syrupAvailable = hasUpgrade(GameUpgrades.ROLE_RANCHER) ? this.getSyrups() : 0;
        
        Iterator<Ant> needIterator = antsInNeed.iterator();
        while (needIterator.hasNext() && syrupAvailable > 0) {
            Ant ant = needIterator.next();
            syrupAvailable -= 1;
            
            needIterator.remove(); 
            thirstyAnts.remove(ant);
            hungryAnts.remove(ant);
        }
        this.setSyrups(syrupAvailable);
        
        //Death
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
            if (ant.getStatus() == GameConstants.STATUS_ALIVE ) { 
                ant.goDie();
                this.deadAnts.add(ant);
                antGroups.get(ant.getType()).remove(ant);
            }
        }
    }

    public void runAging(){
        for (List<Ant> antList : antGroups.values()) {
            Iterator<Ant> iterator = antList.iterator();
            while (iterator.hasNext()) {
                Ant ant = iterator.next();
                ant.setAge(ant.getAge() + 1);
                
                if (ant.getAge() >= ant.getMaxAge()) {
                    ant.goDie();
                    this.deadAnts.add(ant);
                    iterator.remove(); 
                }
            }
        }
    }

    public void runNuptial(){
        List<Ant> princesses = getPrincesses();
        List<Ant> drones = getDrones();
        List<Ant> queens = getQueens();

        if (!hasUpgrade(GameUpgrades.TYPE_PRINCESS)) return;

        Iterator<Ant> iterator = princesses.iterator();
        while (iterator.hasNext()) {
            if (queens.size() >= this.getQueensCapacity() || drones.isEmpty()) {
                break;
            }
            
            Ant princess = iterator.next();
            princess.transform(this, GameConstants.TYPE_QUEEN);
            queens.add(princess);
            iterator.remove(); 
            Ant deadDrone = drones.remove(drones.size() - 1); 
            deadDrone.goDie();
            this.deadAnts.add(deadDrone);
        }
    }

    public void runSpreading() { }
    public void runInfection() { }

    // --- Ant Jobs --- //
    public void runLaying() {
        int layerCount = countAntsByRole(getQueens(), GameConstants.ROLE_LAYER);
        List<Ant> eggList = getEggs();
        int spaceAvailable = this.getEggsCapacity() - eggList.size();
        if (spaceAvailable <= 0) return;   
        
        int toLay = Math.min(layerCount * this.getLayingRate(), spaceAvailable);
        for (int i = 0; i < toLay; i++) {
            eggList.add(new Ant(this, GameConstants.TYPE_EGG));
        }
    }

    public void runResearch() {
        if (!hasUpgrade(GameUpgrades.ROLE_RESEARCHER)) return;
        int researcherCount = countAntsByRole(getQueens(), GameConstants.ROLE_RESEARCHER);
        this.researchPoints += researcherCount * researchSpeed;
    }

    public void runGraveKeeping() {
        int graverCount = countAntsByRole(getWorkers(), GameConstants.ROLE_GRAVER);
        if (graverCount == 0 || getDeadAnts().isEmpty()) return;

        int canClean = graverCount * (int) gravingRate;
        Iterator<Ant> iterator = getDeadAnts().iterator();
        while (canClean > 0 && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            canClean--;
        }
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
            List<Ant> list = antGroups.get(typeToKill);
            for (int i = list.size() - 1; i >= 0; i--) {
                if (deficit <= 0) break; 

                if (Math.random() > 0.5) { 
                    Ant antToCull = list.get(i);
                    list.remove(i); 
                    antToCull.goDie(); 
                    this.deadAnts.add(antToCull);
                    deficit--;
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

        this.setPlants(Math.min(this.getPlants() + effectivePlantGain, this.getPlantsCapacity()));
        this.setWater(Math.min(this.getWater() + effectiveWaterGain, this.getWaterCapacity()));

        if (hasUpgrade(GameUpgrades.ROLE_HUNTER)) {
            int hunterCount = countAntsByRole(getSoldiers(), GameConstants.ROLE_HUNTER);
            int proteinGain = (int) (hunterCount * collectingRate);
            this.setProtein(Math.min(this.getProtein() + proteinGain, this.getProteinCapacity()));
        }
    }

    public void runConverting() {
        int farmerCount = countAntsByRole(getWorkers(), GameConstants.ROLE_FARMER);
        if (this.getMushrooms() >= this.getMushroomsCapacity()) {
            return;
        }

        int conversionAmount = ((int) this.getConversionRate()) * farmerCount;
        
        if (this.getPlants() >= conversionAmount) {
            this.setPlants(this.getPlants() - conversionAmount);
            this.setMushrooms(Math.min(this.getMushrooms() + conversionAmount, this.getMushroomsCapacity()));
        }

        if (this.getMushrooms() >= this.getMushroomsCapacity()) return;

        if (this.getProtein() >= conversionAmount) {
            this.setProtein(this.getProtein() - conversionAmount);
            int mushroomGain = conversionAmount * 2;
            this.setMushrooms(Math.min(this.getMushrooms() + mushroomGain, this.getMushroomsCapacity()));
        }
    }

    public void runRanching() {
        if (!hasUpgrade(GameUpgrades.ROLE_RANCHER)) return;
        int syrupGain = (int) (aphids); 
        this.setSyrups(Math.min(this.getSyrups() + syrupGain, this.getSyrupsCapacity()));
    }

    public void runHerding() {
         if (!hasUpgrade(GameUpgrades.ROLE_RANCHER)) return;
        int rancherCount = countAntsByRole(getWorkers(), GameConstants.ROLE_RANCHER);
        int maxSustainableAphids = aphidCapacity * rancherCount;
        
        if (aphids < maxSustainableAphids) {
            this.setAphids(Math.min(this.getAphids() + rancherCount, maxSustainableAphids));
        } else if (aphids > maxSustainableAphids) {
            this.aphids = maxSustainableAphids;
        }
    }

    // --- Job Packer --- //
    public void runMinutelyJobs() {
        this.runConverting();
    }

    public void runHourlyJobs() {
        this.runRoleAssignment();
        this.runCollecting();;
        this.runLaying();
        this.runResearch();
        this.runRanching();
    }

    public void runDailyJobs() {
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