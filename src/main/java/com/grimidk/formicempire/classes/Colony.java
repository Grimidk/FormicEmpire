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
    private ArrayList<Ant> deadAnts;

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
    private float conversionRate;
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
        this.deadAnts = new ArrayList<>();
        this.eggs = new ArrayList<>();
        this.larvae = new ArrayList<>();
        this.pupae = new ArrayList<>();
        this.workers = new ArrayList<>();
        this.soldiers = new ArrayList<>();  
        this.majors = new ArrayList<>();
        this.drones = new ArrayList<>();
        this.princesses = new ArrayList<>();
        this.queens = new ArrayList<>();

        this.researchSpeed = 100;
        this.growthSpeed = 10;
        this.layingRate = 1;
        this.conversionRate = 1.0f;
        this.parasiteDetection = 10;
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

        this.plants = 0;
        this.plantsCapacity = 5000;
        this.mushrooms = 0;
        this.mushroomsCapacity = 10000;
        this.protein = 0;           
        this.proteinCapacity = 2000;
        this.water = 0;
        this.waterCapacity = 2000;
        this.syrups = 0;
        this.syrupsCapacity = 1000;
        this.resins = 0;
        this.resinsCapacity = 1000;
        this.minerals = 0;
        this.mineralsCapacity = 500;
        this.eggsCapacity = 100;
        this.queensCapacity = 1;
    }

    public Colony(Savefile savefile) {
        this.id = savefile.getColonyId();
        this.name = savefile.getColonyName();
        this.isPlayer = true;
        this.deadAnts = new ArrayList<>();
        this.eggs = new ArrayList<>();
        this.larvae = new ArrayList<>();
        this.pupae = new ArrayList<>();
        this.workers = new ArrayList<>();
        this.soldiers = new ArrayList<>();  
        this.majors = new ArrayList<>();
        this.drones = new ArrayList<>();
        this.princesses = new ArrayList<>();
        this.queens = new ArrayList<>();

        int eggCount = savefile.getEggs();
        int larvaeCount = savefile.getLarvae();
        int pupaeCount = savefile.getPupae();
        int workersCount = savefile.getWorkers();
        int soldiersCount = savefile.getSoldiers();
        int majorsCount = savefile.getMajors();
        int dronesCount = savefile.getDrones();
        int princessesCount = savefile.getPrincesses();
        int queensCount = savefile.getQueens();

        for (int i = 0; i < eggCount; i++) {
            Ant a = new Ant(this, Engine.TYPE_EGG);
            this.eggs.add(a);
        }
        for (int i = 0; i < larvaeCount; i++) {
            Ant a = new Ant(this, Engine.TYPE_LARVA);
            this.larvae.add(a);
        }
        for (int i = 0; i < pupaeCount; i++) {
            Ant a = new Ant(this, Engine.TYPE_PUPA);
            this.pupae.add(a);
        }
        for (int i = 0; i < workersCount; i++) {
            Ant a = new Ant(this, Engine.TYPE_WORKER);
            this.workers.add(a);
        }
        for (int i = 0; i < soldiersCount; i++) {
            Ant a = new Ant(this, Engine.TYPE_SOLDIER);
            this.soldiers.add(a);
        }
        for (int i = 0; i < majorsCount; i++) {
            Ant a = new Ant(this, Engine.TYPE_MAJOR);
            this.majors.add(a);
        }
        for (int i = 0; i < dronesCount; i++) {
            Ant a = new Ant(this, Engine.TYPE_DRONE);
            this.drones.add(a);
        }
        for (int i = 0; i < princessesCount; i++) {
            Ant a = new Ant(this, Engine.TYPE_PRINCESS);
            this.princesses.add(a);
        }
        for (int i = 0; i < queensCount; i++) {
            Ant a = new Ant(this, Engine.TYPE_QUEEN);
            this.queens.add(a);
        }

        this.plants = savefile.getPlants();
        this.mushrooms = savefile.getMushrooms();
        this.protein = savefile.getProtein();
        this.water = savefile.getWater();
        this.syrups = savefile.getSyrups();
        this.resins = savefile.getResins();
        this.minerals = savefile.getMinerals();

        this.researchSpeed = 100;
        this.growthSpeed = 10;
        this.layingRate = 1;
        this.conversionRate = 1.0f;
        this.parasiteDetection = 10;
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

        this.plantsCapacity = 5000;
        this.mushroomsCapacity = 10000;        
        this.proteinCapacity = 2000;
        this.waterCapacity = 2000;
        this.syrupsCapacity = 1000;
        this.resinsCapacity = 1000;
        this.mineralsCapacity = 500;
        this.eggsCapacity = 100;
        this.queensCapacity = 1;
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

    public ArrayList<Ant> getDeadAnts() {
        return deadAnts;
    }

    public void setDeadAnts(ArrayList<Ant> deadAnts) {
        this.deadAnts = deadAnts;
    }
    
    public int getAntTotal() {
        return eggs.size() + larvae.size() + pupae.size() + workers.size() + soldiers.size() + majors.size() + drones.size() + princesses.size() + queens.size();
    }

    public int getTotalConsumption(){
        System.out.println();
        return 
        (int) (eggs.size() * Engine.TYPE_EGG.getConsumptionMult() * this.getBaseConsumption()) + 
        (int) (larvae.size() * Engine.TYPE_LARVA.getConsumptionMult() * this.getBaseConsumption()) + 
        (int) (pupae.size() * Engine.TYPE_PUPA.getConsumptionMult() * this.getBaseConsumption()) + 
        (int) (workers.size() * Engine.TYPE_WORKER.getConsumptionMult() * this.getBaseConsumption()) + 
        (int) (soldiers.size() * Engine.TYPE_SOLDIER.getConsumptionMult() * this.getBaseConsumption()) +
        (int) (majors.size() * Engine.TYPE_MAJOR.getConsumptionMult() * this.getBaseConsumption()) + 
        (int) (drones.size() * Engine.TYPE_DRONE.getConsumptionMult() * this.getBaseConsumption()) + 
        (int) (princesses.size() * Engine.TYPE_PRINCESS.getConsumptionMult() * this.getBaseConsumption()) + 
        (int) (queens.size() * Engine.TYPE_QUEEN.getConsumptionMult() * this.getBaseConsumption());
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
    
    public float getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(float conversionRate) {
        this.conversionRate = conversionRate;
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
        }
        Ant queen = new Ant(this, Engine.TYPE_QUEEN);
        this.queens.add(queen);
    }

    public void runLaying(){
        if (this.eggs.size() >= this.getEggsCapacity()) {
            return;
        }   
        for (int i = 0; i < (this.getQueens().size() * this.getLayingRate()); i++) {
            Ant ant = new Ant(this, Engine.TYPE_EGG);
            this.eggs.add(ant);
        }
    }

    public void runHatching(){    
        for (int i = 0; i < this.getEggs().size(); i++) {
            double r = Math.random(); 
            if (r < 0.80) {
                Ant ant = new Ant(this, Engine.TYPE_WORKER);
                this.workers.add(ant);
            } else if (r < 0.99) {
                Ant ant = new Ant(this, Engine.TYPE_SOLDIER);
                this.soldiers.add(ant);
            } else {
                if (this.getQueens().size() >= this.getQueensCapacity()) {
                    Ant ant = new Ant(this, Engine.TYPE_WORKER);
                    this.workers.add(ant);
                } else {
                    Ant ant = new Ant(this, Engine.TYPE_QUEEN);
                    this.queens.add(ant);
                }
            }
        }
        this.setEggs(new ArrayList<>());
    }

    public void runCollecting(){
        int plant = this.getPlants() + (int) ((this.getWorkers().size() * this.getBaseAttackSpeed() * Engine.TYPE_WORKER.getAttackSpeedMult()));
        if (plant > this.getPlantsCapacity()) {
            plant = this.getPlantsCapacity();
        }
        this.setPlants(plant);
        int protein = this.getProtein() + (int) ((this.getSoldiers().size() * this.getBaseAttackSpeed() * Engine.TYPE_SOLDIER.getAttackSpeedMult()));
        if (protein > this.getProteinCapacity()) {
            protein = this.getProteinCapacity();
        }
        this.setProtein(protein);
    }

    public void runConverting(){
        if (this.getMushrooms() >= this.getMushroomsCapacity()) {
            return;
        }
        if (this.getPlants() >= this.getConversionRate()) {
            this.setPlants(this.getPlants() - (int) this.getConversionRate());
            if (this.getMushrooms() + ((int) this.getConversionRate()) <= this.getMushroomsCapacity()) {
                this.setMushrooms(this.getMushrooms() + ((int) this.getConversionRate()));
            } else {
                this.setMushrooms(this.getMushroomsCapacity());
            }
        }
        if (this.getProtein() >= this.getConversionRate()) {
            this.setProtein(this.getProtein() - (int) this.getConversionRate());
            if (this.getMushrooms() + ((int) this.getConversionRate() * 3) <= this.getMushroomsCapacity()) {
                this.setMushrooms(this.getMushrooms() + ((int) this.getConversionRate() * 3));
            } else {
                this.setMushrooms(this.getMushroomsCapacity());
            }
        }
    }

    public void runEating(){
        if (this.getMushrooms() < this.getTotalConsumption()) {
            int mush = this.getTotalConsumption() - this.getMushrooms();
            while (mush > 0) {
                if (this.getDrones().size() > 0) {
                    this.deadAnts.add(this.getDrones().get(0));
                    this.drones.remove(0);
                    mush -= (int) (Engine.TYPE_DRONE.getConsumptionMult() * this.getBaseConsumption());
                } else if (this.getPrincesses().size() > 0) {
                    this.deadAnts.add(this.getPrincesses().get(0));
                    this.princesses.remove(0);
                    mush -= (int) (Engine.TYPE_PRINCESS.getConsumptionMult() * this.getBaseConsumption());
                } else if (this.getMajors().size() > 0) {
                    this.deadAnts.add(this.getMajors().get(0));
                    this.majors.remove(0);
                    mush -= (int) (Engine.TYPE_MAJOR.getConsumptionMult() * this.getBaseConsumption());
                } else if (this.getSoldiers().size() > 0) {
                    this.deadAnts.add(this.getSoldiers().get(0));
                    this.soldiers.remove(0);
                    mush -= (int) (Engine.TYPE_SOLDIER.getConsumptionMult() * this.getBaseConsumption());
                } else if (this.getLarvae().size() > 0) {
                    this.deadAnts.add(this.getLarvae().get(0));
                    this.larvae.remove(0);
                    mush -= (int) (Engine.TYPE_EGG.getConsumptionMult() * this.getBaseConsumption());
                } else if (this.getWorkers().size() > 0) {
                    this.deadAnts.add(this.getWorkers().get(0));
                    this.workers.remove(0);
                    mush -= (int) (Engine.TYPE_WORKER.getConsumptionMult() * this.getBaseConsumption());
                } else if (this.getQueens().size() > 0) {
                    this.deadAnts.add(this.getQueens().get(0));
                    this.queens.remove(0);
                    mush -= (int) (Engine.TYPE_QUEEN.getConsumptionMult() * this.getBaseConsumption());
                } else {
                    break;
                }
            }
            this.setMushrooms(0);
        } else {
            this.setMushrooms(this.getMushrooms() - this.getTotalConsumption());
        }
    }

    public void runAging(){
        for (Ant ant : this.getWorkers()) {
            ant.setAge(ant.getAge() + 1);
            if (ant.getAge() >= ant.getMaxAge()) {
                this.deadAnts.add(ant);
                this.workers.remove(ant);
            }
        }
        for (Ant ant : this.getSoldiers()) {
            ant.setAge(ant.getAge() + 1);  
            if (ant.getAge() >= ant.getMaxAge()) {
                this.deadAnts.add(ant);
                this.soldiers.remove(ant);
            }
        } 
        for (Ant ant : this.getMajors()) {
            ant.setAge(ant.getAge() + 1);  
            if (ant.getAge() >= ant.getMaxAge()) {
                this.deadAnts.add(ant);
                this.majors.remove(ant);
            }
        }
        for (Ant ant : this.getDrones()) {
            ant.setAge(ant.getAge() + 1);   
            if (ant.getAge() >= ant.getMaxAge()) {
                this.deadAnts.add(ant);
                this.drones.remove(ant);
            }
        } 
        for (Ant ant : this.getPrincesses()) {
            ant.setAge(ant.getAge() + 1);   
            if (ant.getAge() >= ant.getMaxAge()) {
                this.deadAnts.add(ant);
                this.princesses.remove(ant);
            }
        }
        for (Ant ant : this.getQueens()) {
            ant.setAge(ant.getAge() + 1);
            if (ant.getAge() >= ant.getMaxAge()) {
                this.deadAnts.add(ant);
                this.queens.remove(ant);
            }
        }

    }

    public void runEvolving(){

    }

    public void runNuptial(){

    }
}
