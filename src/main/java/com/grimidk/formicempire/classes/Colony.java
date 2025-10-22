/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grimidk.formicempire.classes;

import java.util.ArrayList;

/**
 *
 * @author juanmendezl
 */
public class Colony {
    
    @SuppressWarnings("unused")
    private final int id;
    private String name;
    private Species species;
    private boolean isPlayer;
    private ArrayList<Ant> eggs;
    private ArrayList<Ant> larvae;
    private ArrayList<Ant> pupae;
    private ArrayList<Ant> workers;
    private ArrayList<Ant> soldiers;
    private ArrayList<Ant> majors;
    private ArrayList<Ant> drones;
    private ArrayList<Ant> princesses;
    private ArrayList<Ant> queens;

    private Resource plants;
    private Resource mushrooms;
    private Resource protein;
    private Resource water;
    private Resource syrups;
    private Resource resins;
    private Resource minerals;

    private int baseHealth;
    private int baseHunger;
    private int baseAge;
    private int baseTempRes;
    private int baseRegen;
    private int baseConsumption;
    private int baseAttack;
    private int baseAttackSpeed;
    private int baseDefense;
    private int baseSpeed;  
    private int baseSize;

    public Colony(int id, String name, boolean isPlayer) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.baseHealth = 100;
        this.baseHunger = 100;
        this.baseAge = 180;
        this.baseTempRes = 25;
        this.baseRegen = 1;
        this.baseConsumption = 1;
        this.baseAttack = 10;
        this.baseAttackSpeed = 1;
        this.baseDefense = 5;
        this.baseSpeed = 1;
        this.baseSize = 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public boolean isIsPlayer() {
        return isPlayer;
    }

    public void setIsPlayer(boolean isPlayer) {
        this.isPlayer = isPlayer;
    }

    public ArrayList<Ant> getEggs() {
        return eggs;
    }

    public void setEggs(ArrayList<Ant> eggs) {
        this.eggs = eggs;
    }

    public ArrayList<Ant> getLarvae() {
        return larvae;
    }

    public void setLarvae(ArrayList<Ant> larvae) {
        this.larvae = larvae;
    }

    public ArrayList<Ant> getPupae() {
        return pupae;
    }

    public void setPupae(ArrayList<Ant> pupae) {
        this.pupae = pupae;
    }

    public ArrayList<Ant> getWorkers() {
        return workers;
    }

    public void setWorkers(ArrayList<Ant> workers) {
        this.workers = workers;
    }

    public ArrayList<Ant> getSoldiers() {
        return soldiers;
    }

    public void setSoldiers(ArrayList<Ant> soliders) {
        this.soldiers = soliders;
    }

    public ArrayList<Ant> getMajors() {
        return majors;
    }

    public void setMajors(ArrayList<Ant> majors) {
        this.majors = majors;
    }

    public ArrayList<Ant> getDrones() {
        return drones;
    }

    public void setDrones(ArrayList<Ant> drones) {
        this.drones = drones;
    }

    public ArrayList<Ant> getPrincesses() {
        return princesses;
    }

    public void setPrincesses(ArrayList<Ant> princesses) {
        this.princesses = princesses;
    }

    public ArrayList<Ant> getQueens() {
        return queens;
    }

    public void setQueens(ArrayList<Ant> queens) {
        this.queens = queens;
    }
    
    public int getAntTotal() {
        return eggs.size() + larvae.size() + pupae.size() + workers.size() + soldiers.size() + majors.size() + drones.size() + princesses.size() + queens.size();
    }

    public Resource getPlants() {
        return plants;
    }

    public void setPlants(Resource plants) {
        this.plants = plants;
    }

    public Resource getMushrooms() {
        return mushrooms;
    }

    public void setMushrooms(Resource mushrooms) {
        this.mushrooms = mushrooms;
    }

    public Resource getProtein() {
        return protein;
    }

    public void setProtein(Resource protein) {
        this.protein = protein;
    }

    public Resource getWater() {
        return water;
    }

    public void setWater(Resource water) {
        this.water = water;
    }

    public Resource getSyrups() {
        return syrups;
    }

    public void setSyrups(Resource syrups) {
        this.syrups = syrups;
    }

    public Resource getResins() {
        return resins;
    }

    public void setResins(Resource resins) {
        this.resins = resins;
    }

    public Resource getMinerals() {
        return minerals;
    }

    public void setMinerals(Resource minerals) {
        this.minerals = minerals;
    }

    public int getBaseHealth() {
    return baseHealth;
    }

    public void setBaseHealth(int baseHealth) {
        this.baseHealth = baseHealth;
    }

    public int getBaseHunger() {
        return baseHunger;
    }

    public void setBaseHunger(int baseHunger) {
        this.baseHunger = baseHunger;
    }

    public int getBaseAge() {
        return baseAge;
    }

    public void setBaseAge(int baseAge) {
        this.baseAge = baseAge;
    }

    public int getBaseTempRes() {
        return baseTempRes;
    }

    public void setBaseTempRes(int baseTempRes) {
        this.baseTempRes = baseTempRes;
    }

    public int getBaseRegen() {
        return baseRegen;
    }

    public void setBaseRegen(int baseRegen) {
        this.baseRegen = baseRegen;
    }

    public int getBaseConsumption() {
        return baseConsumption;
    }

    public void setBaseConsumption(int baseConsumption) {
        this.baseConsumption = baseConsumption;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public void setBaseAttack(int baseAttack) {
        this.baseAttack = baseAttack;
    }

    public int getBaseAttackSpeed() {
        return baseAttackSpeed;
    }

    public void setBaseAttackSpeed(int baseAttackSpeed) {
        this.baseAttackSpeed = baseAttackSpeed;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public void setBaseDefense(int baseDefense) {
        this.baseDefense = baseDefense;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(int baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public int getBaseSize() {
        return baseSize;
    }

    public void setBaseSize(int baseSize) {
        this.baseSize = baseSize;
    }
    
    public void startColony() {
        System.out.println("Generating new colony...");
        for (int i = 1; i <= 5; i++) {
            // Ant ant = new Ant(this, );
        }
    }
    
}
