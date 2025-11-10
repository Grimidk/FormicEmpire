package com.grimidk.formicempire.classes.constants;

public class Building {
    private final int id;
    private final String name;
    private final int level;
    private final String description;
    private final Building requirement;
    private final int resinCost;
    private final int mineralCost;
    private final int buildTime;

    public Building(int id, String name,int level, String description, Building requirement, int resinCost, int mineralCost, int buildTime) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.description = description;
        this.requirement = requirement;
        this.resinCost = resinCost;
        this.mineralCost = mineralCost;
        this.buildTime = buildTime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public Building getRequirement() {
        return requirement;
    }

    public int getResinCost() {
        return resinCost;
    }

    public int getMineralCost() {
        return mineralCost;
    }

    public int getBuildTime() {
        return buildTime;
    }
}
