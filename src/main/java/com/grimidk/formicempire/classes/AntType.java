package com.grimidk.formicempire.classes;

public class AntType {

    private final int id;
    private String name;
    private float healtMult;
    private float hungerMult;
    private float attackMult;
    private float ageMult;
    private float regenMult;
    private float consumptionMult;
    private float attackSpeedMult;
    private float defenseMult;
    private float speedMult;
    private float sizeMult; 

    public AntType(int id, String name, float healtMult, float hungerMult, float attackMult, float ageMult, float regenMult, float consumptionMult, float attackSpeedMult, float defenseMult, float speedMult, float sizeMult) {
        this.id = id;
        this.name = name;
        this.healtMult = healtMult;
        this.hungerMult = hungerMult;
        this.attackMult = attackMult;
        this.ageMult = ageMult;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getHealtMult() {
        return healtMult;
    }

    public void setHealtMult(float healtMult) {
        this.healtMult = healtMult;
    }

    public float getHungerMult() {
        return hungerMult;
    }

    public void setHungerMult(float hungerMult) {
        this.hungerMult = hungerMult;
    }

    public float getAttackMult() {
        return attackMult;
    }

    public void setAttackMult(float attackMult) {
        this.attackMult = attackMult;
    }

    public float getAgeMult() {
        return ageMult;
    }

    public void setAgeMult(float ageMult) {
        this.ageMult = ageMult;
    }

    public float getRegenMult() {
        return regenMult;
    }

    public void setRegenMult(float regenMult) {
        this.regenMult = regenMult;
    }

    public float getConsumptionMult() {
        return consumptionMult;
    }

    public void setConsumptionMult(float consumptionMult) {
        this.consumptionMult = consumptionMult;
    }

    public float getAttackSpeedMult() {
        return attackSpeedMult;
    }

    public void setAttackSpeedMult(float attackSpeedMult) {
        this.attackSpeedMult = attackSpeedMult;
    }

    public float getDefenseMult() {
        return defenseMult;
    }

    public void setDefenseMult(float defenseMult) {
        this.defenseMult = defenseMult;
    }

    public float getSpeedMult() {
        return speedMult;
    }

    public void setSpeedMult(float speedMult) {
        this.speedMult = speedMult;
    }

    public float getSizeMult() {
        return sizeMult;
    }

    public void setSizeMult(float sizeMult) {
        this.sizeMult = sizeMult;
    }
    
    
}
