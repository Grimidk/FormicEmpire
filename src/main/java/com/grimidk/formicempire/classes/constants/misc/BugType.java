package com.grimidk.formicempire.classes.constants.misc;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class BugType extends Constant{
    private float baseHealth;
    private float baseRegen; 
    private float baseAttack;
    private float baseAttackSpeed;
    private float baseDefense;
    private float baseSpeed;
    private float baseSize;

    public BugType(int id, String name, float baseHealth, float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed, float baseSize, ImageIcon icon) {
        super(id, name, icon);
        this.baseHealth = baseHealth;
        this.baseRegen = baseRegen;
        this.baseAttack = baseAttack;
        this.baseAttackSpeed = baseAttackSpeed;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.baseSize = baseSize;
    }

    //no icon
    public BugType(int id, String name, float baseHealth, float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed, float baseSize) {
        this(id, name, baseHealth, baseRegen, baseAttack, baseAttackSpeed, baseDefense, baseSpeed, baseSize, null);
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

    public float getBaseSize() {
        return baseSize;
    }
}
