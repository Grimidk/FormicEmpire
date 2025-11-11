package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class Building {
    private final int id;
    private final String name;
    private final int level;
    private final String description;
    private final Building requirement;
    private final int resinCost;
    private final int mineralCost;
    private final int buildTime;
    private ImageIcon icon;
    private ImageIcon sprite;

    public Building(int id, String name,int level, String description, Building requirement, int resinCost, int mineralCost, int buildTime) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.description = description;
        this.requirement = requirement;
        this.resinCost = resinCost;
        this.mineralCost = mineralCost;
        this.buildTime = buildTime;
        this.icon = icon;
        this.sprite = sprite;
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

    public ImageIcon getIcon() {
        return icon;
    }

    public ImageIcon getSprite() {
        return sprite;
    }
}
