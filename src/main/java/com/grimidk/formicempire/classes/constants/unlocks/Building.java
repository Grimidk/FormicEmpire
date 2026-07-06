package com.grimidk.formicempire.classes.constants.unlocks;

import javax.swing.ImageIcon;
import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class Building extends Constant {
    private final int level;
    private final String descriptionKey;
    private final Building requirement;
    private final int resinCost;
    private final int mineralCost;
    private final int buildTime;
    private final ImageIcon sprite;

    public Building(int id, String nameKey, int level, String descriptionKey, Building requirement, int resinCost, int mineralCost, int buildTime, ImageIcon icon, ImageIcon sprite) {
        super(id, nameKey, icon);
        this.level = level;
        this.descriptionKey = descriptionKey;
        this.requirement = requirement;
        this.resinCost = resinCost;
        this.mineralCost = mineralCost;
        this.buildTime = buildTime;
        this.sprite = sprite;
    }

    public Building(int id, String nameKey, int level, String descriptionKey, Building requirement, int resinCost, int mineralCost, int buildTime, ImageIcon roomArt) {
        this(id, nameKey, level, descriptionKey, requirement, resinCost, mineralCost, buildTime, roomArt, roomArt);
    }

    //(no icon/sprite)
    public Building(int id, String nameKey, int level, String descriptionKey, Building requirement, int resinCost, int mineralCost, int buildTime) {
        this(id, nameKey, level, descriptionKey, requirement, resinCost, mineralCost, buildTime, null, null);
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
}
