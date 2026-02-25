package com.grimidk.formicempire.classes.constants.unlocks;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Synergy extends Constant {
    private final String description;
    private final Upgrade requirement1;
    private final Upgrade requirement2;
    private final Upgrade reward;

    public Synergy(int id, String name, String description, Upgrade requirement1, Upgrade requirement2, Upgrade reward, ImageIcon icon) {
        super(id, name, icon);
        this.description = description;
        this.requirement1 = requirement1;
        this.requirement2 = requirement2;
        this.reward = reward;
    }

    // (no icon)
    public Synergy(int id, String name, String description, Upgrade requirement1, Upgrade requirement2, Upgrade reward) {
        this(id, name, description, requirement1, requirement2, reward, null);
    }

    public String getDescription() {
        return description;
    }

    public Upgrade getRequirement1() {
        return requirement1;
    }   

    public Upgrade getRequirement2() {
        return requirement2;
    }
    
    public Upgrade getReward() {
        return reward;
    }
}
