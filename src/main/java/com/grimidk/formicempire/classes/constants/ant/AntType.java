package com.grimidk.formicempire.classes.constants.ant;

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
    private final String spriteName;

    public AntType(int id, String name, float healtMult, float attackMult, float regenMult, float consumptionMult, float attackSpeedMult, float defenseMult, float speedMult, ImageIcon icon, String spriteName) {
        super(id, name, icon);
        this.healtMult = healtMult;
        this.attackMult = attackMult;
        this.regenMult = regenMult;
        this.consumptionMult = consumptionMult;
        this.attackSpeedMult = attackSpeedMult;
        this.defenseMult = defenseMult;
        this.speedMult = speedMult;
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

    public String getSpriteName() {
        return spriteName;
    }
}
