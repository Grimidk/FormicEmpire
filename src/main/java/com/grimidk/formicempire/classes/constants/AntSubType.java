package com.grimidk.formicempire.classes.constants;

public class AntSubType {

    private final int id;
    private final AntType antType;
    private final String name;
    private final float healtMult;
    private final float attackMult;
    private final float regenMult;
    private final float consumptionMult;
    private final float attackSpeedMult;
    private final float defenseMult;
    private final float speedMult;
    private final float sizeMult; 

    public AntSubType(int id, AntType antType, String name, float healtMult, float attackMult, float regenMult, float consumptionMult, float attackSpeedMult, float defenseMult, float speedMult, float sizeMult) {
        this.id = id;
        this.antType = antType;
        this.name = name;
        this.healtMult = healtMult;
        this.attackMult = attackMult;
        this.regenMult = regenMult;
        this.consumptionMult = consumptionMult;
        this.attackSpeedMult = attackSpeedMult;
        this.defenseMult = defenseMult;
        this.speedMult = speedMult;
        this.sizeMult = sizeMult;
    }

    public int getId() {
        return id;
    }

    public AntType getAntType() {
        return antType;
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
