package com.grimidk.formicempire.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap; 
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.ColonyRank;
import com.grimidk.formicempire.classes.constants.Species;

public class Colony {
    private final int id;
    private String name;
    private Species species;
    private boolean isPlayer;
    private ColonyRank rank;
    
    private final Map<AntType, List<Ant>> antGroups;
    private final List<Ant> deadAnts;

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
    private int growthTime;
    private int layingRate;
    private float conversionRate;
    private int parasiteDetection;

    private int baseHealth;
    private int baseAge;
    private int baseTempRes;
    private int baseRegen;
    private int baseConsumption;
    private int baseAttack;
    private int baseAttackSpeed;
    private int baseDefense;
    private int baseSpeed;  
    private int baseSize;

    private void initializeLists() {
        this.antGroups.put(Engine.TYPE_EGG, new ArrayList<Ant>());
        this.antGroups.put(Engine.TYPE_LARVA, new ArrayList<Ant>());
        this.antGroups.put(Engine.TYPE_PUPA, new ArrayList<Ant>());
        this.antGroups.put(Engine.TYPE_WORKER, new ArrayList<Ant>());
        this.antGroups.put(Engine.TYPE_SOLDIER, new ArrayList<Ant>());
        this.antGroups.put(Engine.TYPE_MAJOR, new ArrayList<Ant>());
        this.antGroups.put(Engine.TYPE_DRONE, new ArrayList<Ant>());
        this.antGroups.put(Engine.TYPE_PRINCESS, new ArrayList<Ant>());
        this.antGroups.put(Engine.TYPE_QUEEN, new ArrayList<Ant>());
    }

    private void initializeDefaults() {
        this.researchSpeed = 100;
        this.growthTime = 5;
        this.layingRate = 1;
        this.conversionRate = 1.0f;
        this.parasiteDetection = 10;
        this.baseHealth = 100;
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
        this.plantsCapacity = 2000;
        this.mushrooms = 0;
        this.mushroomsCapacity = 5000;
        this.protein = 0;           
        this.proteinCapacity = 1000;
        this.water = 0;
        this.waterCapacity = 1000;
        this.syrups = 0;
        this.syrupsCapacity = 500;
        this.resins = 0;
        this.resinsCapacity = 200;
        this.minerals = 0;
        this.mineralsCapacity = 100;
        this.eggsCapacity = 50;
        this.queensCapacity = 2;
    }

    public Colony(int id, String name, boolean isPlayer) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.rank = Engine.RANK_COLONY;
        this.antGroups = new HashMap<AntType, List<Ant>>();
        this.deadAnts = new ArrayList<Ant>();
        
        initializeLists();
        initializeDefaults();
    }

    private void populateAntList(List<Ant> list, int count, AntType type) {
        for (int i = 0; i < count; i++) {
            list.add(new Ant(this, type));
        }
    }

    public Colony(Savefile savefile) {
        this.id = savefile.getColonyId();
        this.name = savefile.getColonyName();
        this.isPlayer = true;
        this.rank = Engine.RANK_COLONY;
        this.antGroups = new HashMap<AntType, List<Ant>>();
        this.deadAnts = new ArrayList<Ant>();

        initializeLists();
        initializeDefaults(); 

        populateAntList(getEggs(), savefile.getEggs(), Engine.TYPE_EGG);
        populateAntList(getLarvae(), savefile.getLarvae(), Engine.TYPE_LARVA);
        populateAntList(getPupae(), savefile.getPupae(), Engine.TYPE_PUPA);
        populateAntList(getWorkers(), savefile.getWorkers(), Engine.TYPE_WORKER);
        populateAntList(getSoldiers(), savefile.getSoldiers(), Engine.TYPE_SOLDIER);
        populateAntList(getMajors(), savefile.getMajors(), Engine.TYPE_MAJOR);
        populateAntList(getDrones(), savefile.getDrones(), Engine.TYPE_DRONE);
        populateAntList(getPrincesses(), savefile.getPrincesses(), Engine.TYPE_PRINCESS);
        populateAntList(getQueens(), savefile.getQueens(), Engine.TYPE_QUEEN);

        this.plants = savefile.getPlants();
        this.mushrooms = savefile.getMushrooms();
        this.protein = savefile.getProtein();
        this.water = savefile.getWater();
        this.syrups = savefile.getSyrups();
        this.resins = savefile.getResins();
        this.minerals = savefile.getMinerals();
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

    public boolean isPlayer() {
        return isPlayer;
    }

    public void setIsPlayer(boolean isPlayer) {
        this.isPlayer = isPlayer;
    }

    public ColonyRank getRank() {
        return rank;
    }

    public void setRank(ColonyRank rank) {
        this.rank = rank;
    }

    public ArrayList<Ant> getEggs() {
        return (ArrayList<Ant>) antGroups.get(Engine.TYPE_EGG);
    }

    public void setEggs(ArrayList<Ant> eggs) {
        antGroups.put(Engine.TYPE_EGG, eggs);
    }

    public ArrayList<Ant> getLarvae() {
        return (ArrayList<Ant>) antGroups.get(Engine.TYPE_LARVA);
    }

    public void setLarvae(ArrayList<Ant> larvae) {
        antGroups.put(Engine.TYPE_LARVA, larvae);
    }

    public ArrayList<Ant> getPupae() {
        return (ArrayList<Ant>) antGroups.get(Engine.TYPE_PUPA);
    }

    public void setPupae(ArrayList<Ant> pupae) {
        antGroups.put(Engine.TYPE_PUPA, pupae);
    }

    public ArrayList<Ant> getWorkers() {
        return (ArrayList<Ant>) antGroups.get(Engine.TYPE_WORKER);
    }

    public void setWorkers(ArrayList<Ant> workers) {
        antGroups.put(Engine.TYPE_WORKER, workers);
    }

    public ArrayList<Ant> getSoldiers() {
        return (ArrayList<Ant>) antGroups.get(Engine.TYPE_SOLDIER);
    }

    public void setSoldiers(ArrayList<Ant> soldiers) {
        antGroups.put(Engine.TYPE_SOLDIER, soldiers);
    }

    public ArrayList<Ant> getMajors() {
        return (ArrayList<Ant>) antGroups.get(Engine.TYPE_MAJOR);
    }

    public void setMajors(ArrayList<Ant> majors) {
        antGroups.put(Engine.TYPE_MAJOR, majors);
    }

    public ArrayList<Ant> getDrones() {
        return (ArrayList<Ant>) antGroups.get(Engine.TYPE_DRONE);
    }

    public void setDrones(ArrayList<Ant> drones) {
        antGroups.put(Engine.TYPE_DRONE, drones);
    }

    public ArrayList<Ant> getPrincesses() {
        return (ArrayList<Ant>) antGroups.get(Engine.TYPE_PRINCESS);
    }

    public void setPrincesses(ArrayList<Ant> princesses) {
        antGroups.put(Engine.TYPE_PRINCESS, princesses);
    }

    public ArrayList<Ant> getQueens() {
        return (ArrayList<Ant>) antGroups.get(Engine.TYPE_QUEEN);
    }

    public void setQueens(ArrayList<Ant> queens) {
        antGroups.put(Engine.TYPE_QUEEN, queens);
    }

    public ArrayList<Ant> getDeadAnts() {
        return (ArrayList<Ant>) deadAnts;
    }

    public void setDeadAnts(ArrayList<Ant> deadAnts) {
        this.deadAnts.clear();
        this.deadAnts.addAll(deadAnts);
    }
    
    public int getAntTotal() {
        int total = 0;
        for (List<Ant> list : antGroups.values()) {
            total += list.size();
        }
        return total;
    }

    public int getTotalConsumption(){
        double totalConsumption = 0;
        for (Map.Entry<AntType, List<Ant>> entry : antGroups.entrySet()) {
            AntType type = entry.getKey();
            List<Ant> list = entry.getValue();
            totalConsumption += (double) list.size() * type.getConsumptionMult() * this.getBaseConsumption();
        }
        return (int) totalConsumption;
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

    public int getGrowthTime() {
        return growthTime;
    }

    public void setGrowthTime(int growthTime) {
        this.growthTime = growthTime;
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
        List<Ant> workerList = getWorkers();
        for (int i = 0; i < 9; i++) {
            workerList.add(new Ant(this, Engine.TYPE_WORKER));
        }
        getQueens().add(new Ant(this, Engine.TYPE_QUEEN));
    }

    public void runLaying(){
        List<Ant> eggList = getEggs();
        if (eggList.size() >= this.getEggsCapacity()) {
            return;
        }   
        
        int toLay = this.getQueens().size() * this.getLayingRate();
        for (int i = 0; i < toLay; i++) {
            if (eggList.size() >= this.getEggsCapacity()) {
                break; 
            }
            eggList.add(new Ant(this, Engine.TYPE_EGG));
        }
    }

    private AntType determineHatchType() {
        double rand = Math.random();
        if (rand < 0.8) {
            return Engine.TYPE_WORKER;
        } else if (rand < 0.90) {
            return Engine.TYPE_SOLDIER;
        } else if (rand < 0.97) {
            // if () {
            //     return Engine.TYPE_MAJOR;
            // } else {
            //     return Engine.TYPE_SOLDIER;
            // }
            return Engine.TYPE_SOLDIER;
        } else if (rand < 0.99) {
            // if () {
            //     return Engine.TYPE_DRONE;
            // } else {
            //     return Engine.TYPE_SOLDIER;
            // }
            return Engine.TYPE_SOLDIER;
        } else {
            // if () {
            //     return Engine.TYPE_PRINCESS;
            // } else {
            //     return Engine.TYPE_SOLDIER;
            // }
            return Engine.TYPE_SOLDIER;
        }
    }

    private void evolveAnts(List<Ant> sourceList, List<Ant> destList, AntType newType) {
        Iterator<Ant> iterator = sourceList.iterator();
        while (iterator.hasNext()) {
            Ant ant = iterator.next();
            if (ant.getAge() >= this.getGrowthTime()) { 
                ant.transform(this, newType);
                destList.add(ant);
                iterator.remove(); 
            }
        }
    }

    private void hatchPupae() {
        Iterator<Ant> iterator = getPupae().iterator();
        while (iterator.hasNext()) {
            Ant pupa = iterator.next();
            if (pupa.getAge() < this.getGrowthTime()) {
                continue;
            }

            AntType newType = determineHatchType();
            pupa.transform(this, newType);
            
            antGroups.get(newType).add(pupa);
            iterator.remove();
        }
    }

    public void runHatching(){    
        hatchPupae();

        evolveAnts(getLarvae(), getPupae(), Engine.TYPE_PUPA);

        evolveAnts(getEggs(), getLarvae(), Engine.TYPE_LARVA);
    }

    public void runCollecting(){
        int plantGain = (int) ((getWorkers().size() * getBaseAttackSpeed() * Engine.TYPE_WORKER.getAttackSpeedMult()));
        this.setPlants(Math.min(this.getPlants() + plantGain, this.getPlantsCapacity()));

        int proteinGain = (int) ((getSoldiers().size() * getBaseAttackSpeed() * Engine.TYPE_SOLDIER.getAttackSpeedMult()));
        this.setProtein(Math.min(this.getProtein() + proteinGain, this.getProteinCapacity()));
    }

    public void runConverting(){
        if (this.getMushrooms() >= this.getMushroomsCapacity()) {
            return;
        }

        int conversionAmount = (int) this.getConversionRate();
        
        if (this.getPlants() >= conversionAmount) {
            this.setPlants(this.getPlants() - conversionAmount);
            this.setMushrooms(Math.min(this.getMushrooms() + conversionAmount, this.getMushroomsCapacity()));
        }

        if (this.getMushrooms() >= this.getMushroomsCapacity()) {
            return;
        }

        if (this.getProtein() >= conversionAmount) {
            this.setProtein(this.getProtein() - conversionAmount);
            int mushroomGain = conversionAmount * 3;
            this.setMushrooms(Math.min(this.getMushrooms() + mushroomGain, this.getMushroomsCapacity()));
        }
    }

    public void runEating(){
        int totalConsumption = this.getTotalConsumption();
        if (this.getMushrooms() >= totalConsumption) {
            this.setMushrooms(this.getMushrooms() - totalConsumption);
            return;
        }

        int deficit = totalConsumption - this.getMushrooms();
        this.setMushrooms(0);

        List<AntType> killOrder = Arrays.asList(
            Engine.TYPE_DRONE,
            Engine.TYPE_PRINCESS,
            Engine.TYPE_MAJOR,
            Engine.TYPE_SOLDIER,
            Engine.TYPE_LARVA,
            Engine.TYPE_WORKER,
            Engine.TYPE_QUEEN
        );

        for (AntType typeToKill : killOrder) {
            List<Ant> list = antGroups.get(typeToKill);
            int antConsumption = (int) (typeToKill.getConsumptionMult() * this.getBaseConsumption());
            if (antConsumption <= 0) antConsumption = 1; 

            while (deficit > 0 && !list.isEmpty()) {
                Ant dead = list.remove(list.size() - 1); 
                this.deadAnts.add(dead);
                deficit -= antConsumption;
            }

            if (deficit <= 0) {
                break; 
            }
        }
    }

    public void runAging(){
        for (List<Ant> antList : antGroups.values()) {
            Iterator<Ant> iterator = antList.iterator();
            while (iterator.hasNext()) {
                Ant ant = iterator.next();
                ant.setAge(ant.getAge() + 1);
                
                if (ant.getAge() >= ant.getMaxAge()) {
                    this.deadAnts.add(ant);
                    iterator.remove(); 
                }
            }
        }
    }

    public void runNuptial(){
        List<Ant> princesses = getPrincesses();
        List<Ant> drones = getDrones();
        List<Ant> queens = getQueens();

        Iterator<Ant> iterator = princesses.iterator();
        while (iterator.hasNext()) {
            if (queens.size() >= this.getQueensCapacity() || drones.isEmpty()) {
                break;
            }
            
            Ant princess = iterator.next();
            princess.transform(this, Engine.TYPE_QUEEN);
            queens.add(princess);
            iterator.remove(); 
            Ant deadDrone = drones.remove(drones.size() - 1); 
            this.deadAnts.add(deadDrone);
        }
    }

    public void runSpreading(){
    }
}