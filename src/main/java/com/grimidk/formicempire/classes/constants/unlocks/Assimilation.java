package com.grimidk.formicempire.classes.constants.unlocks;

import javax.swing.ImageIcon;
import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class Assimilation extends Constant{
    private final String descriptionKey;
    private final Upgrade reward;
    private final int cost;

    public Assimilation(int id, String nameKey, String descriptionKey, Upgrade reward, int cost, ImageIcon icon) {
        super(id, nameKey, icon);
        this.descriptionKey = descriptionKey;
        this.reward = reward;
        this.cost = cost;
    }

    // (no icon)
    public Assimilation(int id, String nameKey, String descriptionKey, Upgrade reward, int cost) {
        this(id, nameKey, descriptionKey, reward, cost, null);
    }

    public String getDescription() {
        return LanguageStrings.get(descriptionKey);
    }

    public Upgrade getReward() {
        return reward;
    }

    public int getCost() {
        return cost;
    }
}