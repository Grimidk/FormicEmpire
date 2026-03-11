package com.grimidk.formicempire.classes.constants.unlocks;

import javax.swing.ImageIcon;
import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class Upgrade extends Constant {
    private final String flavorNameKey;
    private final String descriptionKey;
    private final Upgrade requirement;
    private final int cost;

    public Upgrade(int id, String nameKey, String flavorNameKey, String descriptionKey, Upgrade requirement, int cost, ImageIcon icon) {
        super(id, nameKey, icon);
        this.flavorNameKey = flavorNameKey;
        this.descriptionKey = descriptionKey;
        this.requirement = requirement;
        this.cost = cost;
    }

    // (no icon)
    public Upgrade(int id, String nameKey, String flavorNameKey, String descriptionKey, Upgrade requirement, int cost) {
        this(id, nameKey, flavorNameKey, descriptionKey, requirement, cost, null);
    }

    public String getFlavorName() {
        return LanguageStrings.get(flavorNameKey);
    }

    public String getDescription() {
        return LanguageStrings.get(descriptionKey);
    }

    public Upgrade getRequirement() {
        return requirement;
    }

    public int getCost() {
        return cost;
    }
}