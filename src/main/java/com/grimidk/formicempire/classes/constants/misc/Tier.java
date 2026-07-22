package com.grimidk.formicempire.classes.constants.misc;

import javax.swing.ImageIcon;
import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.entities.Dynasty;

public class Tier extends Constant {
    private final Rank rankRequirement;
    private final long minimumRp;
    private final int minimumConstructionCost;

    public Tier(int id, String nameKey, Rank rankRequirement, long minimumRp, int minimumConstructionCost, ImageIcon icon) {
        super(id, nameKey, icon);
        this.rankRequirement = rankRequirement;
        this.minimumRp = minimumRp;
        this.minimumConstructionCost = minimumConstructionCost;
    }

    public Rank getRankRequirement() {
        return rankRequirement;
    }

    public long getMinimumRp() {
        return minimumRp;
    }

    public int getMinimumConstructionCost() {
        return minimumConstructionCost;
    }

    public boolean isUnlocked(Dynasty dynasty) {
        if (dynasty == null || dynasty.getRank() == null || rankRequirement == null) {
            return false;
        }
        return dynasty.getRank().getId() >= rankRequirement.getId();
    }

}
