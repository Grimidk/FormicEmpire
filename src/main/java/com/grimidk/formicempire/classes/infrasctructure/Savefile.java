package com.grimidk.formicempire.classes.infrasctructure;

import java.io.Serializable;
import java.util.HashMap;
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

    private Map<String, Integer> assignedRoleCounts;

    public Savefile(int id, String name) {
        this.id = id;
        this.name = name;
        this.assignedRoleCounts = new HashMap<>();
        this.minute = 0;
        this.hour = 0;
        this.day = 1;
        this.month = 1;
        this.year = 0; 
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

    public int getPlantsCapacity() {
        return plantsCapacity;
    }

    public void setPlantsCapacity(int plantsCapacity) {
        this.plantsCapacity = plantsCapacity;
    }

    public int getMushrooms() {
        return mushrooms;
    }   

    public void setMushrooms(int mushrooms) {
        this.mushrooms = mushrooms;
    }   

    public int getMushroomsCapacity() {
        return mushroomsCapacity;
    }   

    public void setMushroomsCapacity(int mushroomsCapacity) {
        this.mushroomsCapacity = mushroomsCapacity;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getProteinCapacity() {
        return proteinCapacity;
    }

    public void setProteinCapacity(int proteinCapacity) {
        this.proteinCapacity = proteinCapacity;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getWaterCapacity() {
        return waterCapacity;
    }

    public void setWaterCapacity(int waterCapacity) {
        this.waterCapacity = waterCapacity;
    }

    public int getSyrups() {
        return syrups;
    }

    public void setSyrups(int syrups) {
        this.syrups = syrups;
    }

    public int getSyrupsCapacity() {
        return syrupsCapacity;
    }

    public void setSyrupsCapacity(int syrupsCapacity) {
        this.syrupsCapacity = syrupsCapacity;
    }

    public int getResins() {
        return resins;
    }

    public void setResins(int resins) {
        this.resins = resins;
    }

    public int getResinsCapacity() {
        return resinsCapacity;
    }

    public void setResinsCapacity(int resinsCapacity) {
        this.resinsCapacity = resinsCapacity;
    }

    public int getMinerals() {
        return minerals;
    }

    public void setMinerals(int minerals) {
        this.minerals = minerals;
    }

    public int getMineralsCapacity() {
        return mineralsCapacity;
    }

    public void setMineralsCapacity(int mineralsCapacity) {
        this.mineralsCapacity = mineralsCapacity;
    }

    public Map<String, Integer> getAssignedRoleCounts() {
        return assignedRoleCounts;
    }

    public void setAssignedRoleCounts(Map<String, Integer> assignedRoleCounts) {
        this.assignedRoleCounts = assignedRoleCounts;
    }
}