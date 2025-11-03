package com.grimidk.formicempire.classes.entities;

import com.grimidk.formicempire.classes.constants.AntRole;
import com.grimidk.formicempire.classes.constants.AntStatus;
import com.grimidk.formicempire.classes.constants.AntSubType;
import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;

public class Ant {
    private AntType type;
    private AntSubType subType;
    private AntRole role;
    private AntStatus status;
    private float health;
    private int maxHealth;
    private int age;
    private int maxAge;
    private float temp;
    private float tempRes;
    private float regen;
    private float consumption;
    private float attack;
    private float attackSpeed;
    private float defense;
    private float speed;
    private float size;

    public Ant(Colony colony, AntType type) {
        this.type = type;
        this.subType = null;
        this.role = null;
        this.status = GameConstants.STATUS_ALIVE;
        this.maxHealth = (int)(colony.getBaseHealth() * type.getHealtMult());
        this.health = this.maxHealth;
        this.maxAge = (int)(colony.getBaseAge() * type.getAgeMult());
        this.age = 0;
        this.tempRes = colony.getBaseTempRes();
        this.regen = colony.getBaseRegen() * type.getRegenMult();
        this.consumption = colony.getBaseConsumption() * type.getConsumptionMult();
        this.attack = colony.getBaseAttack() * type.getAttackMult();
        this.attackSpeed = colony.getBaseAttackSpeed() * type.getAttackSpeedMult();
        this.defense = colony.getBaseDefense() * type.getDefenseMult();
        this.speed = colony.getBaseSpeed() * type.getSpeedMult();
        this.size = colony.getBaseSize() * type.getSizeMult();
    }

    public AntType getType() {
        return type;
    }

    public void setType(AntType type) {
        this.type = type;
    }

    public AntSubType getSubType() {
        return subType;
    }

    public void setSubType(AntSubType subType) {
        this.subType = subType;
    }

    public AntRole getRole() {
        return role;
    }

    public void setRole(AntRole role) {
        this.role = role;
    }

    public AntStatus getStatus() {
        return status;
    }

    public void setStatus(AntStatus status) {
        this.status = status;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getTempRes() {
        return tempRes;
    }

    public void setTempRes(float tempRes) {
        this.tempRes = tempRes;
    }

    public float getRegen() {
        return regen;
    }

    public void setRegen(int regen) {
        this.regen = regen;
    }

    public float getConsumption() {
        return consumption;
    }

    public void setConsumption(float consumption) {
        this.consumption = consumption;
    }

    public float getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(int attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public float getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public float getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void goDie() {
        this.type = GameConstants.TYPE_DEAD;
        this.status = GameConstants.STATUS_ALIVE;
        this.maxHealth = 0;
        this.health = 0;
        this.age = 0;
        this.regen = 0;
        this.consumption = 0;
        this.attack = 0;
        this.attackSpeed = 0;
        this.defense = 0;
        this.speed = 0;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void transform(Colony colony, AntType newType) {
        this.type = newType;
        this.maxHealth = (int)(colony.getBaseHealth() * newType.getHealtMult());
        this.health = this.maxHealth;
        this.maxAge = (int)(colony.getBaseAge() * newType.getAgeMult());
        this.age = 0;
        this.regen = colony.getBaseRegen() * newType.getRegenMult();
        this.consumption = colony.getBaseConsumption() * newType.getConsumptionMult();
        this.attack = colony.getBaseAttack() * newType.getAttackMult();
        this.attackSpeed = colony.getBaseAttackSpeed() * newType.getAttackSpeedMult();
        this.defense = colony.getBaseDefense() * newType.getDefenseMult();
        this.speed = colony.getBaseSpeed() * newType.getSpeedMult();
        this.size = colony.getBaseSize() * newType.getSizeMult();
    }
}
