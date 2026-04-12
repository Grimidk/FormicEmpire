package com.grimidk.formicempire.classes.constants.misc;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class BugType extends Constant{
    private final String scientificNameKey;
    private final float baseHealth;
    private final float baseRegen; 
    private final float baseAttack;
    private final float baseAttackSpeed;
    private final float baseDefense;
    private final float baseSpeed;
    private final ImageIcon sprite;

    public BugType(int id, String name, String scientificNameKey, float baseHealth, float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed, ImageIcon icon, ImageIcon sprite) {
        super(id, name, icon);
        this.scientificNameKey = scientificNameKey;
        this.baseHealth = baseHealth;
        this.baseRegen = baseRegen;
        this.baseAttack = baseAttack;
        this.baseAttackSpeed = baseAttackSpeed;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.sprite = sprite;
    }

    //no icon
    public BugType(int id, String name, String scientificNameKey, float baseHealth, float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed, ImageIcon sprite) {
        this(id, name, scientificNameKey, baseHealth, baseRegen, baseAttack, baseAttackSpeed, baseDefense, baseSpeed, null, sprite);
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
