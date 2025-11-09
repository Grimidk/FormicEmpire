package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class AntType {
    private final int id;
    private final String name;
    private final float healtMult;
    private final float attackMult;
    private final float ageMult;
    private final float regenMult;
    private final float consumptionMult;
    private final float attackSpeedMult;
    private final float defenseMult;
    private final float speedMult;
    private final float sizeMult; 
    private final ImageIcon icon;
    private final ImageIcon sprite;

    public AntType(int id, String name, float healtMult, float attackMult, float ageMult, float regenMult, float consumptionMult, float attackSpeedMult, float defenseMult, float speedMult, float sizeMult, ImageIcon icon, ImageIcon sprite) {
        this.id = id;
        this.name = name;
        this.healtMult = healtMult;
        this.attackMult = attackMult;
        this.ageMult = ageMult;
        this.regenMult = regenMult;
        this.consumptionMult = consumptionMult;
        this.attackSpeedMult = attackSpeedMult;
        this.defenseMult = defenseMult;
        this.speedMult = speedMult;
        this.sizeMult = sizeMult;
        this.icon = icon;
        this.sprite = sprite;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getHealtMult() {
        return healtMult;
    }

    public float getAttackMult() {
        return attackMult;
    }

    public float getAgeMult() {
        return ageMult;
    }

    public float getRegenMult() {
        return regenMult;
    }

    public float getConsumptionMult() {
        return consumptionMult;
    }

    public float getAttackSpeedMult() {
        return attackSpeedMult;
    }

    public float getDefenseMult() {
        return defenseMult;
    }

    public float getSpeedMult() {
        return speedMult;
    }

    public float getSizeMult() {
        return sizeMult;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public ImageIcon getSprite() {
        return sprite;
    }
}
