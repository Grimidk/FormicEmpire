package com.grimidk.formicempire.classes.constants.unlocks;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Upgrade extends Constant {
    private final String flavorName;
    private final String description;
    private final Upgrade requirement;
    private final int cost;

    public Upgrade(int id, String name, String flavorName, String description, Upgrade requirement, int cost, ImageIcon icon) {
        super(id, name, icon);
        this.flavorName = flavorName;
        this.description = description;
        this.requirement = requirement;
        this.cost = cost;
    }

    // (no icon)
    public Upgrade(int id, String name, String flavorName, String description, Upgrade requirement, int cost) {
        this(id, name, flavorName, description, requirement, cost, null);
    }

    public String getFlavorName() {
        return flavorName;
    }

    public String getDescription() {
        return description;
    }

    public Upgrade getRequirement() {
        return requirement;
    }

    public int getCost() {
        return cost;
    }
}