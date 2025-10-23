package com.grimidk.formicempire.classes;

import java.util.ArrayList;

public class Colony {
    
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

    private int plants;
    private int plantsCapacity;
    private int mushrooms;
    private int mushroomsCapacity;
    private int protein;
    private int proteinCapacity;
    private int water;
    private int waterCapacity; 
    private int syrups;
    private int syrupsCapacity;
    private int resins;
    private int resinsCapacity;
    private int minerals;
    private int mineralsCapacity;

    private int eggsCapacity;
    private int queensCapacity;

    private int researchSpeed;
    private int growthSpeed;
    private int layingRate;
    private int parasiteDetection;

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
        this.eggs = new ArrayList<>();
        this.larvae = new ArrayList<>();
        this.pupae = new ArrayList<>();
        this.workers = new ArrayList<>();
        this.soldiers = new ArrayList<>();  
        this.majors = new ArrayList<>();
        this.drones = new ArrayList<>();
        this.princesses = new ArrayList<>();
        this.queens = new ArrayList<>();
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

    public Colony(Savefile savefile) {
        this.id = savefile.getColonyId();
        this.name = savefile.getColonyName();
        this.isPlayer = true;
        this.eggs = new ArrayList<>();
        this.larvae = new ArrayList<>();
        this.pupae = new ArrayList<>();
        this.workers = new ArrayList<>();
        this.soldiers = new ArrayList<>();  
        this.majors = new ArrayList<>();
        this.drones = new ArrayList<>();
        this.princesses = new ArrayList<>();
        this.queens = new ArrayList<>();

        try {
            int workersCount = savefile.getWorkers();
            int soldiersCount = savefile.getSoldiers();
            int queensCount = savefile.getQueens();
            for (int i = 0; i < workersCount; i++) {
                Ant a = new Ant(this, Engine.TYPE_WORKER);
                this.workers.add(a);
            }
            for (int i = 0; i < soldiersCount; i++) {
                Ant a = new Ant(this, Engine.TYPE_SOLDIER);
                this.soldiers.add(a);
            }
            for (int i = 0; i < queensCount; i++) {
                Ant a = new Ant(this, Engine.TYPE_QUEEN);
                this.queens.add(a);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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

    public int getId() {
        return id;
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

    public int getPlants() {
        return plants;
    }

    public void setPlants(int plants) {
        this.plants = plants;
    }

    public int getPlantsCapacity() {
        return plantsCapacity;
    }

    public void setPlantsCapacity(int plantsCapacity) {
        this.plantsCapacity = plantsCapacity;
    }

    public int getMushrooms() {
        return mushrooms;
    }

    public void setMushrooms(int mushrooms) {
        this.mushrooms = mushrooms;
    }

    public int getMushroomsCapacity() {
        return mushroomsCapacity;
    }

    public void setMushroomsCapacity(int mushroomsCapacity) {
        this.mushroomsCapacity = mushroomsCapacity;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getProteinCapacity() {
        return proteinCapacity;
    }

    public void setProteinCapacity(int proteinCapacity) {
        this.proteinCapacity = proteinCapacity;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getWaterCapacity() {
        return waterCapacity;
    }

    public void setWaterCapacity(int waterCapacity) {
        this.waterCapacity = waterCapacity;
    }

    public int getSyrups() {
        return syrups;
    }

    public void setSyrups(int syrups) {
        this.syrups = syrups;
    }

    public int getSyrupsCapacity() {
        return syrupsCapacity;
    }

    public void setSyrupsCapacity(int syrupsCapacity) {
        this.syrupsCapacity = syrupsCapacity;
    }

    public int getResins() {
        return resins;
    }

    public void setResins(int resins) {
        this.resins = resins;
    }

    public int getResinsCapacity() {
        return resinsCapacity;
    }

    public void setResinsCapacity(int resinsCapacity) {
        this.resinsCapacity = resinsCapacity;
    }

    public int getMinerals() {
        return minerals;
    }

    public void setMinerals(int minerals) {
        this.minerals = minerals;
    }

    public int getMineralsCapacity() {
        return mineralsCapacity;
    }

    public void setMineralsCapacity(int mineralsCapacity) {
        this.mineralsCapacity = mineralsCapacity;
    }

    public int getEggsCapacity() {
        return eggsCapacity;
    }

    public void setEggsCapacity(int eggsCapacity) {
        this.eggsCapacity = eggsCapacity;
    }

    public int getQueensCapacity() {
        return queensCapacity;
    }

    public void setQueensCapacity(int queensCapacity) {
        this.queensCapacity = queensCapacity;
    }

    public int getResearchSpeed() {
        return researchSpeed;
    }

    public void setResearchSpeed(int researchSpeed) {
        this.researchSpeed = researchSpeed;
    }

    public int getGrowthSpeed() {
        return growthSpeed;
    }

    public void setGrowthSpeed(int growthSpeed) {
        this.growthSpeed = growthSpeed;
    }

    public int getLayingRate() {
        return layingRate;
    }

    public void setLayingRate(int layingRate) {
        this.layingRate = layingRate;
    }

    public int getParasiteDetection() {
        return parasiteDetection;
    }

    public void setParasiteDetection(int parasiteDetection) {
        this.parasiteDetection = parasiteDetection;
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
        for (int i = 1; i <= 9; i++) {
            Ant ant = new Ant(this, Engine.TYPE_WORKER);
            this.workers.add(ant);
            System.out.println("Spawned worker ant");
        }
        Ant queen = new Ant(this, Engine.TYPE_QUEEN);
        this.queens.add(queen);
        System.out.println("Spawned queen ant");
    }

    public void loadColony(Savefile savefile){
        
    }

    public void runLaying(){

    }

    public void runHatching(){    
        for (int i = 0; i < this.getQueens().size(); i++) {
            double r = Math.random(); 
            if (r < 0.80) {
                Ant ant = new Ant(this, Engine.TYPE_WORKER);
                this.workers.add(ant);
                System.out.println("Spawned worker ant");
            } else if (r < 0.99) {
                Ant ant = new Ant(this, Engine.TYPE_SOLDIER);
                this.soldiers.add(ant);
                System.out.println("Spawned soldier ant");
            } else {
                Ant ant = new Ant(this, Engine.TYPE_QUEEN);
                this.queens.add(ant);
                System.out.println("Spawned queen ant");
            }
        }
    }
    
    public void runEating(){

    }
}
