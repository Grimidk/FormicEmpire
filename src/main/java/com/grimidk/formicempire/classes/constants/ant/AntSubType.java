package com.grimidk.formicempire.classes.constants.ant;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class AntSubType extends Constant {

    private final AntType antType;
    private final float healtMult;
    private final float attackMult;
    private final float regenMult;
    private final float consumptionMult;
    private final float attackSpeedMult;
    private final float defenseMult;
    private final float speedMult;
    private final float sizeMult; 

    public AntSubType(int id, AntType antType, String name, float healtMult, float attackMult, float regenMult, float consumptionMult, float attackSpeedMult, float defenseMult, float speedMult, float sizeMult, ImageIcon icon) {
        super(id, name, icon);
        this.antType = antType;
        this.healtMult = healtMult;
        this.attackMult = attackMult;
        this.regenMult = regenMult;
        this.consumptionMult = consumptionMult;
        this.attackSpeedMult = attackSpeedMult;
        this.defenseMult = defenseMult;
        this.speedMult = speedMult;
        this.sizeMult = sizeMult;
    }

    //(no icon)
    public AntSubType(int id, AntType antType, String name, float healtMult, float attackMult, float regenMult, float consumptionMult, float attackSpeedMult, float defenseMult, float speedMult, float sizeMult) {
        this(id, antType, name, healtMult, attackMult, regenMult, consumptionMult, attackSpeedMult, defenseMult, speedMult, sizeMult, null);
    }

    public AntType getAntType() {
        return antType;
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

    public float getSizeMult() {
        return sizeMult;
    }
}
