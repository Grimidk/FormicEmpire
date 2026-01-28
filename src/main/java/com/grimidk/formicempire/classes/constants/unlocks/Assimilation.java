package com.grimidk.formicempire.classes.constants.unlocks;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Assimilation extends Constant{
    private final String description;
    private final Upgrade reward;
    private final int cost;

    public Assimilation(int id, String name, String description, Upgrade reward, int cost, ImageIcon icon) {
        super(id, name, icon);
        this.description = description;
        this.reward = reward;
        this.cost = cost;
    }

    // (no icon)
    public Assimilation(int id, String name, String description, Upgrade reward, int cost) {
        this(id, name, description, reward, cost, null);
    }

    public String getDescription() {
        return description;
    }

    public Upgrade getReward() {
        return reward;
    }

    public int getCost() {
        return cost;
    }
}