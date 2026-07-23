package com.grimidk.formicempire.classes.constants.critter;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class Species extends Constant {
    private final CritterClass critterClass;
    private final String scientificNameKey;
    private final float baseHealth;
    private final float baseRegen;
    private final float baseAttack;
    private final float baseAttackSpeed;
    private final float baseDefense;
    private final float baseSpeed;
    private final ImageIcon sprite;
    private final boolean pet;

    public Species(int id, String name, CritterClass critterClass, String scientificNameKey, float baseHealth,
            float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed,
            ImageIcon icon, ImageIcon sprite) {
        this(id, name, critterClass, scientificNameKey, baseHealth, baseRegen, baseAttack, baseAttackSpeed,
                baseDefense, baseSpeed, icon, sprite, false);
    }

    public Species(int id, String name, CritterClass critterClass, String scientificNameKey, float baseHealth,
            float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed,
            ImageIcon icon, ImageIcon sprite, boolean pet) {
        super(id, name, icon);
        this.critterClass = critterClass;
        this.scientificNameKey = scientificNameKey;
        this.baseHealth = baseHealth;
        this.baseRegen = baseRegen;
        this.baseAttack = baseAttack;
        this.baseAttackSpeed = baseAttackSpeed;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.sprite = sprite;
        this.pet = pet;
    }

    public CritterClass getCritterClass() {
        return critterClass;
    }

    public boolean isPet() {
        return pet;
    }

    public String getScientificName() {
        return LanguageStrings.get(scientificNameKey);
    }

    public float getBaseHealth() {
        return baseHealth;
    }

    public float getBaseRegen() {
        return baseRegen;
    }

    public float getBaseAttack() {
        return baseAttack;
    }

    public float getBaseAttackSpeed() {
        return baseAttackSpeed;
    }

    public float getBaseDefense() {
        return baseDefense;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public ImageIcon getSprite() {
        return sprite;
    }
}
