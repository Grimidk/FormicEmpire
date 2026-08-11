package com.grimidk.formicempire.classes.constants.unlocks;

import java.util.List;

import javax.swing.ImageIcon;
import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.misc.Tier;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public class Building extends Constant {
    private final int level;
    private final String descriptionKey;
    private final Building requirement;
    private final int resinCost;
    private final int mineralCost;
    private final int buildTime;
    private final ImageIcon sprite;
    private final int tierIndex;

    public Building(int id, String nameKey, int level, String descriptionKey, Building requirement, int resinCost, int mineralCost, int buildTime, ImageIcon icon, ImageIcon sprite, int tierIndex) {
        super(id, nameKey, icon);
        this.level = level;
        this.descriptionKey = descriptionKey;
        this.requirement = requirement;
        this.resinCost = resinCost;
        this.mineralCost = mineralCost;
        this.buildTime = buildTime;
        this.sprite = sprite;
        this.tierIndex = Math.max(0, tierIndex);
    }

    public Building(int id, String nameKey, int level, String descriptionKey, Building requirement, int resinCost, int mineralCost, int buildTime, ImageIcon icon, ImageIcon sprite) {
        this(id, nameKey, level, descriptionKey, requirement, resinCost, mineralCost, buildTime, icon, sprite, 0);
    }

    public Building(int id, String nameKey, int level, String descriptionKey, Building requirement, int resinCost, int mineralCost, int buildTime, ImageIcon roomArt) {
        this(id, nameKey, level, descriptionKey, requirement, resinCost, mineralCost, buildTime, roomArt, roomArt, 0);
    }

    public Building(int id, String nameKey, int level, String descriptionKey, Building requirement, int resinCost, int mineralCost, int buildTime, ImageIcon roomArt, int tierIndex) {
        this(id, nameKey, level, descriptionKey, requirement, resinCost, mineralCost, buildTime, roomArt, roomArt, tierIndex);
    }

    //(no icon/sprite)
    public Building(int id, String nameKey, int level, String descriptionKey, Building requirement, int resinCost, int mineralCost, int buildTime) {
        this(id, nameKey, level, descriptionKey, requirement, resinCost, mineralCost, buildTime, null, null, 0);
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return LanguageStrings.get(descriptionKey);
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

    public Tier getTier() {
        List<Tier> tiers = GameConstants.getTiers();
        int index = Math.min(tierIndex, tiers.size() - 1);
        return tiers.get(Math.max(0, index));
    }

    public boolean isAvailableFor(Dynasty dynasty) {
        return getTier().isUnlocked(dynasty);
    }

    public String getDisplayName() {
        return getName();
    }

    public ImageIcon getTierIcon() {
        return getTier().getIcon();
    }
}
