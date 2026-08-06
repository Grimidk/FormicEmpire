package com.grimidk.formicempire.classes.constants.unlocks;

import javax.swing.ImageIcon;
import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

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

    public String getDescription() {
        if (reward != null) {
            String rewardDescription = reward.getDescription();
            if (rewardDescription != null && !rewardDescription.isBlank()) {
                return rewardDescription;
            }
        }
        return LanguageStrings.get(descriptionKey);
    }

    public Upgrade getReward() {
        return reward;
    }

    public int getCost() {
        return cost;
    }
}