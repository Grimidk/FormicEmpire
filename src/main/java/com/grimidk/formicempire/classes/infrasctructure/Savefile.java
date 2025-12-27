package com.grimidk.formicempire.classes.infrasctructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Savefile implements Serializable {

    private static final long serialVersionUID = 1L;
    private final int id;
    private final String name;
    private int colonyId;
    private String colonyName;
    private float progress;
    private int playTime;
    private int minute;
    private int hour;
    private int day;
    private int month;
    private int year;
    
    private int totalAnts;
    private int deadAnts;
    private int eggs;
    private int pupae;
    private int larvae;
    private int workers;
    private int soldiers;
    private int majors;
    private int drones;
    private int princesses;
    private int queens;

    private int plants;
    private int mushrooms;
    private int protein;
    private int water;
    private int syrups;
    private int resins;
    private int minerals;

    private float hatchRateWorker;
    private float hatchRateSoldier;
    private float hatchRateMajor;
    private float hatchRateDrone;
    private float hatchRatePrincess;

    private Map<String, Integer> assignedRoleCounts;
    private List<Integer> unlockedUpgradeIds;
    private List<Integer> unlockedBuildingIds;
    private List<SavedResourceSource> savedResourceSources;

    private int aphids;
    private int parasites;
    private int researchPoints;
    private int totalDeaths;
    
    private int worldRadius;
    private List<SavedHex> worldHexes;

    public Savefile(int id, String name) {
        this.id = id;
        this.name = name;
        this.assignedRoleCounts = new HashMap<>();
        this.unlockedUpgradeIds = new ArrayList<>(); 
        this.unlockedBuildingIds = new ArrayList<>(); 
        this.savedResourceSources = new ArrayList<>();
        this.worldHexes = new ArrayList<>();
        this.minute = 0;
        this.hour = 0;
        this.day = 1;
        this.month = 1;
        this.year = 0; 
        this.worldRadius = 8;

        this.hatchRateWorker = 100.0f;
        this.hatchRateSoldier = 0.0f;
        this.hatchRateMajor = 0.0f;
        this.hatchRateDrone = 0.0f;
        this.hatchRatePrincess = 0.0f;

        this.aphids = 0;
        this.parasites = 0;
        this.researchPoints = 0;
        this.totalDeaths = 0;
    }

    public static class SavedResourceSource implements Serializable {
        private static final long serialVersionUID = 1L;
        public int typeId;
        public int currentQuantity;
        public int initialQuantity;
        public int x;
        public int y;

        public SavedResourceSource(int typeId, int currentQuantity, int initialQuantity, int x, int y) {
            this.typeId = typeId;
            this.currentQuantity = currentQuantity;
            this.initialQuantity = initialQuantity;
            this.x = x;
            this.y = y;
        }
    }
    
    public static class SavedHex implements Serializable {
        private static final long serialVersionUID = 1L;
        public int q;
        public int r;
        public int biomeId;
        public boolean hasColony;
        public int timeOffset;
        public int weatherId;

        public SavedHex(int q, int r, int biomeId, boolean hasColony, int timeOffset, int weatherId) {
            this.q = q;
            this.r = r;
            this.biomeId = biomeId;
            this.hasColony = hasColony;
            this.timeOffset = timeOffset;
            this.weatherId = weatherId;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
     public int getColonyId() {
        return colonyId;
    }

    public void setColonyId(int colonyId) {
        this.colonyId = colonyId;
    }

    public String getColonyName() {
        return colonyName;
    }

    public void setColonyName(String colonyName) {
        this.colonyName = colonyName;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public int getPlayTime() {
        return playTime;
    }

    public void setPlayTime(int playTime) {
        this.playTime = playTime;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getTotalAnts() {
        return totalAnts;
    }

    public void setTotalAnts(int totalAnts) {
        this.totalAnts = totalAnts;
    }

    public int getDeadAnts() {
        return deadAnts;
    }
    
    public void setDeadAnts(int deadAnts) {
        this.deadAnts = deadAnts;
    }

    public int getEggs() {
        return eggs;
    }

    public void setEggs(int eggs) {
        this.eggs = eggs;
    }

    public int getPupae() {
        return pupae;
    }

    public void setPupae(int pupae) {
        this.pupae = pupae;
    }

    public int getLarvae() {
        return larvae;
    }

    public void setLarvae(int larvae) {
        this.larvae = larvae;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public int getSoldiers() {
        return soldiers;
    }

    public void setSoldiers(int soldiers) {
        this.soldiers = soldiers;
    }

    public int getMajors() {
        return majors;
    }

    public void setMajors(int majors) {
        this.majors = majors;
    }

    public int getDrones() {
        return drones;
    }

    public void setDrones(int drones) {
        this.drones = drones;
    }

    public int getPrincesses() {
        return princesses;
    }

    public void setPrincesses(int princesses) {
        this.princesses = princesses;
    }

    public int getQueens() {
        return queens;
    }

    public void setQueens(int queens) {
        this.queens = queens;
    }

    public int getPlants() {
        return plants;
    }

    public void setPlants(int plants) {
        this.plants = plants;
    }

    public int getMushrooms() {
        return mushrooms;
    }   

    public void setMushrooms(int mushrooms) {
        this.mushrooms = mushrooms;
    }   

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getSyrups() {
        return syrups;
    }

    public void setSyrups(int syrups) {
        this.syrups = syrups;
    }

    public int getResins() {
        return resins;
    }

    public void setResins(int resins) {
        this.resins = resins;
    }

    public int getMinerals() {
        return minerals;
    }

    public void setMinerals(int minerals) {
        this.minerals = minerals;
    }

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

    public Map<String, Integer> getAssignedRoleCounts() {
        return assignedRoleCounts;
    }

    public void setAssignedRoleCounts(Map<String, Integer> assignedRoleCounts) {
        this.assignedRoleCounts = assignedRoleCounts;
    }
    
    public List<Integer> getUnlockedUpgradeIds() {
        return unlockedUpgradeIds;
    }

    public void setUnlockedUpgradeIds(List<Integer> unlockedUpgradeIds) {
        this.unlockedUpgradeIds = unlockedUpgradeIds;
    }
        
    public List<Integer> getUnlockedBuildingIds() {
        return unlockedBuildingIds;
    }

    public void setUnlockedBuildingIds(List<Integer> unlockedBuildingIds) {
        this.unlockedBuildingIds = unlockedBuildingIds;
    }
    
    public List<SavedResourceSource> getSavedResourceSources() {
        return savedResourceSources;
    }

    public void setSavedResourceSources(List<SavedResourceSource> savedResourceSources) {
        this.savedResourceSources = savedResourceSources;
    }

    public int getAphids() { return aphids; }
    public void setAphids(int aphids) { this.aphids = aphids; }

    public int getParasites() { return parasites; }
    public void setParasites(int parasites) { this.parasites = parasites; }

    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }

    public int getTotalDeaths() { return totalDeaths; }
    public void setTotalDeaths(int totalDeaths) { this.totalDeaths = totalDeaths; }
    
    public int getWorldRadius() {
        return worldRadius;
    }
    
    public void setWorldRadius(int worldRadius) {
        this.worldRadius = worldRadius;
    }
    
    public List<SavedHex> getWorldHexes() {
        return worldHexes;
    }
    
    public void setWorldHexes(List<SavedHex> worldHexes) {
        this.worldHexes = worldHexes;
    }
}