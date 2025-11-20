package com.grimidk.formicempire.classes.constants.unlocks;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Building extends Constant {
    private final int level;
    private final String description;
    private final Building requirement;
    private final int resinCost;
    private final int mineralCost;
    private final int buildTime;
    private final ImageIcon sprite;

    public Building(int id, String name,int level, String description, Building requirement, int resinCost, int mineralCost, int buildTime, ImageIcon icon, ImageIcon sprite) {
        super(id, name, icon);
        this.level = level;
        this.description = description;
        this.requirement = requirement;
        this.resinCost = resinCost;
        this.mineralCost = mineralCost;
        this.buildTime = buildTime;
        this.sprite = sprite;
    }

    //(no icon/sprite)
    public Building(int id, String name,int level, String description, Building requirement, int resinCost, int mineralCost, int buildTime) {
        this(id, name, level, description, requirement, resinCost, mineralCost, buildTime, null, null);
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

    public ImageIcon getSprite() {
        return sprite;
    }
}
