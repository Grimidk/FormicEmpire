package com.grimidk.formicempire.classes.infrasctructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Savefile implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // --- Global Save Data ---
    private final int id; 
    private final String name; 
    private long timestamp;
    private int playTime;
    private int minute;
    private int hour;
    private int day;
    private int month;
    private int year;
    private int worldRadius;
    
    // --- Root Summary Data ---
    private int colonyId;
    private String colonyName;
    private int totalAnts;
    private int deadAnts;
    private int workers;
    private int queens;
    
    // --- Resource Summary Data ---
    private int plants;
    private int mushrooms;
    private int protein;
    private int water;
    private int syrups;
    private int resins;
    private int minerals;

    // --- Collections ---
    private List<SavedHex> worldHexes;
    private List<SavedColony> colonies;
    private List<SavedDynasty> dynastys;

    public Savefile(int id, String name) {
        this.id = id;
        this.name = name;
        this.timestamp = System.currentTimeMillis();
        this.worldHexes = new ArrayList<>();
        this.colonies = new ArrayList<>();
        this.dynastys = new ArrayList<>();
        this.minute = 0;
        this.hour = 0;
        this.day = 1;
        this.month = 1;
        this.year = 0; 
        this.worldRadius = 8;
    }

    public static class SavedDynasty implements Serializable {
        private static final long serialVersionUID = 1L;
        public int id;
        public String name;
        public boolean isPlayer;
        public String rankName;
        public int speciesId;
        public int researchPoints;
        public List<Integer> unlockedUpgradeIds = new ArrayList<>();
        public Map<String, Integer> deathStatistics = new HashMap<>();
    }

    public static class SavedColony implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public int id;
        public int dynastyId;
        public String name;
        public String rankName;
        public boolean isPlayer;
        public boolean isCapital;
        public boolean isAutomated;
        public boolean autoBuildEnabled;
        public int age;
        public int q; 
        public int r;        
        public float progress;
        public int totalAnts;
        public int deadAnts;
        public int eggs, pupae, larvae, workers, soldiers, majors, drones, princesses, queens;        
        public int plants, mushrooms, protein, water, syrups, resins, minerals;        
        public float hatchRateWorker, hatchRateSoldier, hatchRateMajor, hatchRateDrone, hatchRatePrincess;
        public int aphids, parasites;
        public int totalDeaths;
        public Map<String, Integer> assignedRoleCounts = new HashMap<>();
        public Map<String, Integer> localDeathStatistics = new HashMap<>();
        public List<Integer> unlockedBuildingIds = new ArrayList<>();
        public List<SavedResourceSource> savedResourceSources = new ArrayList<>();
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

    // --- Getters & Setters ---

    public int getId() { return id; }
    public String getName() { return name; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getPlayTime() { return playTime; }
    public void setPlayTime(int playTime) { this.playTime = playTime; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getWorldRadius() { return worldRadius; }
    public void setWorldRadius(int worldRadius) { this.worldRadius = worldRadius; }
    
    public int getColonyId() { return colonyId; }
    public void setColonyId(int colonyId) { this.colonyId = colonyId; }
    
    public String getColonyName() { return colonyName; }
    public void setColonyName(String colonyName) { this.colonyName = colonyName; }
    
    public int getTotalAnts() { return totalAnts; }
    public void setTotalAnts(int totalAnts) { this.totalAnts = totalAnts; }
    
    public int getDeadAnts() { return deadAnts; }
    public void setDeadAnts(int deadAnts) { this.deadAnts = deadAnts; }
    
    public int getWorkers() { return workers; }
    public void setWorkers(int workers) { this.workers = workers; }
    
    public int getQueens() { return queens; }
    public void setQueens(int queens) { this.queens = queens; }
    
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

    public List<SavedHex> getWorldHexes() { return worldHexes; }
    public void setWorldHexes(List<SavedHex> worldHexes) { this.worldHexes = worldHexes; }

    public List<SavedColony> getColonies() { return colonies; }
    public void setColonies(List<SavedColony> colonies) { this.colonies = colonies; }

    public List<SavedDynasty> getDynastys() { return dynastys; }
    public void setDynastys(List<SavedDynasty> dynastys) { this.dynastys = dynastys; }
}