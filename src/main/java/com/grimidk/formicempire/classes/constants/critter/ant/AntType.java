package com.grimidk.formicempire.classes.constants.critter.ant;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class AntType extends Constant {
    private final float healtMult;
    private final float attackMult;
    private final float regenMult;
    private final float consumptionMult;
    private final float attackSpeedMult;
    private final float defenseMult;
    private final float speedMult;
    private final int militaryWeight;
    private final String spriteName;

    public AntType(int id, String name, float healtMult, float attackMult, float regenMult, float consumptionMult,
            float attackSpeedMult, float defenseMult, float speedMult, ImageIcon icon, String spriteName) {
        this(id, name, healtMult, attackMult, regenMult, consumptionMult, attackSpeedMult, defenseMult, speedMult,
                0, icon, spriteName);
    }

    public AntType(int id, String name, float healtMult, float attackMult, float regenMult, float consumptionMult,
            float attackSpeedMult, float defenseMult, float speedMult, int militaryWeight, ImageIcon icon,
            String spriteName) {
        super(id, name, icon);
        this.healtMult = healtMult;
        this.attackMult = attackMult;
        this.regenMult = regenMult;
        this.consumptionMult = consumptionMult;
        this.attackSpeedMult = attackSpeedMult;
        this.defenseMult = defenseMult;
        this.speedMult = speedMult;
        this.militaryWeight = Math.max(0, militaryWeight);
        this.spriteName = spriteName;
    }

    public float getHealtMult() {
        return healtMult;
    }

    public float getAttackMult() {
        return attackMult;
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

    public int getMilitaryWeight() {
        return militaryWeight;
    }

    public String getSpriteName() {
        return spriteName;
    }
}
