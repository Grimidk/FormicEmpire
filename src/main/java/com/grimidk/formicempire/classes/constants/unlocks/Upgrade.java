package com.grimidk.formicempire.classes.constants.unlocks;

import java.util.List;

import javax.swing.ImageIcon;
import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.misc.Tier;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public class Upgrade extends Constant {
    private final String flavorNameKey;
    private final String descriptionKey;
    private final Upgrade requirement;
    private final int cost;
    private final int tierIndex;
    private int gridX;
    private int gridY;

    public Upgrade(int id, String nameKey, String flavorNameKey, String descriptionKey, Upgrade requirement, int cost, ImageIcon icon, int tierIndex, int gridX, int gridY) {
        super(id, nameKey, icon);
        this.flavorNameKey = flavorNameKey;
        this.descriptionKey = descriptionKey;
        this.requirement = requirement;
        this.cost = cost;
        this.tierIndex = Math.max(0, tierIndex);
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public Upgrade(int id, String nameKey, String flavorNameKey, String descriptionKey, Upgrade requirement, int cost, ImageIcon icon, int gridX, int gridY) {
        this(id, nameKey, flavorNameKey, descriptionKey, requirement, cost, icon, 0, gridX, gridY);
    }

    // (no icon)
    public Upgrade(int id, String nameKey, String flavorNameKey, String descriptionKey, Upgrade requirement, int cost, int gridX, int gridY) {
        this(id, nameKey, flavorNameKey, descriptionKey, requirement, cost, null, 0, gridX, gridY);
    }

    public String getFlavorName() {
        return LanguageStrings.get(flavorNameKey);
    }

    public String getDescription() {
        return LanguageStrings.get(descriptionKey);
    }

    public String getTitleName() {
        String name = getName();
        return name.equals(getNameKey()) ? getFlavorName() : name;
    }

    public boolean hasTranslatedName() {
        return !getName().equals(getNameKey());
    }

    public Upgrade getRequirement() {
        return requirement;
    }

    public int getCost() {
        return cost;
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
        return getFlavorName();
    }

    public ImageIcon getTierIcon() {
        return getTier().getIcon();
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }
}
