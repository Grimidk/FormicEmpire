/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grimidk.formicempire.classes;

/**
 *
 * @author juanmendezl
 */
public class Ant {
    private AntType type;
    private AntSubType subType;
    private AntStatus status;
    private float health;
    private int maxHealth;
    private float hunger;
    private int maxHunger;
    private float age;
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

    public Ant(AntType type, AntSubType subType) {
        this.type = type;
        this.subType = subType;
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

    public float getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
    }

    public int getMaxHunger() {
        return maxHunger;
    }

    public void setMaxHunger(int maxHunger) {
        this.maxHunger = maxHunger;
    }

    public float getAge() {
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
    
    
}
