package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.AntRole;
import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.ColonyRank;
import com.grimidk.formicempire.classes.constants.Species;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;

public class Colony {
    private final int id;
    private String name;
    private Species species;
    private boolean isPlayer;
    private ColonyRank rank;
    
    private final Map<AntType, List<Ant>> antGroups;
    private final List<Ant> deadAnts;
    private Map<AntRole, Integer> assignedRoleCounts;

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
    private int aphidCapacity;

    private int researchSpeed;
    private int growthTime;
    private int layingRate;
    private float conversionRate;
    private float nursingRate;
    private float gravingRate;
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
        this.antGroups.put(GameConstants.TYPE_EGG, new ArrayList<Ant>());
        this.antGroups.put(GameConstants.TYPE_LARVA, new ArrayList<Ant>());
        this.antGroups.put(GameConstants.TYPE_PUPA, new ArrayList<Ant>());
        this.antGroups.put(GameConstants.TYPE_WORKER, new ArrayList<Ant>());
        this.antGroups.put(GameConstants.TYPE_SOLDIER, new ArrayList<Ant>());
        this.antGroups.put(GameConstants.TYPE_MAJOR, new ArrayList<Ant>());
        this.antGroups.put(GameConstants.TYPE_DRONE, new ArrayList<Ant>());
        this.antGroups.put(GameConstants.TYPE_PRINCESS, new ArrayList<Ant>());
        this.antGroups.put(GameConstants.TYPE_QUEEN, new ArrayList<Ant>());
    }

    private void initializeAssignedRoles() {
        this.assignedRoleCounts = new HashMap<>();
        for (AntRole role : GameConstants.getAntRoles()) {
            this.assignedRoleCounts.put(role, 0);
        }
    }

    private void initializeDefaults() {
        this.researchSpeed = 100;
        this.growthTime = 4;
        this.layingRate = 1;
        this.conversionRate = 1.0f;
        this.nursingRate = 10f;
        this.gravingRate = 5f;
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
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<AntType, List<Ant>>();
        this.deadAnts = new ArrayList<Ant>();
        
        initializeLists();
        initializeDefaults();
        initializeAssignedRoles();
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
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<AntType, List<Ant>>();
        this.deadAnts = new ArrayList<Ant>();

        initializeLists();
        initializeDefaults(); 
        initializeAssignedRoles(); 

        Map<String, Integer> savedRoles = savefile.getAssignedRoleCounts();
        if (savedRoles != null && !savedRoles.isEmpty()) {
            for (AntRole role : GameConstants.getAntRoles()) {
                Integer count = savedRoles.get(role.getName());
                if (count != null) {
                    this.assignedRoleCounts.put(role, count);
                }
            }
        }

        populateAntList(getEggs(), savefile.getEggs(), GameConstants.TYPE_EGG);
        populateAntList(getLarvae(), savefile.getLarvae(), GameConstants.TYPE_LARVA);
        populateAntList(getPupae(), savefile.getPupae(), GameConstants.TYPE_PUPA);
        populateAntList(getWorkers(), savefile.getWorkers(), GameConstants.TYPE_WORKER);
        populateAntList(getSoldiers(), savefile.getSoldiers(), GameConstants.TYPE_SOLDIER);
        populateAntList(getMajors(), savefile.getMajors(), GameConstants.TYPE_MAJOR);
        populateAntList(getDrones(), savefile.getDrones(), GameConstants.TYPE_DRONE);
        populateAntList(getPrincesses(), savefile.getPrincesses(), GameConstants.TYPE_PRINCESS);
        populateAntList(getQueens(), savefile.getQueens(), GameConstants.TYPE_QUEEN);
        populateAntList(deadAnts, savefile.getDeadAnts(), GameConstants.TYPE_DEAD);

        this.plants = savefile.getPlants();
        this.mushrooms = savefile.getMushrooms();
        this.protein = savefile.getProtein();
        this.water = savefile.getWater();
        this.syrups = savefile.getSyrups();
        this.resins = savefile.getResins();
        this.minerals = savefile.getMinerals();

        runRoleAssignment();
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

    public List<Ant> getAntsByType(AntType type) {
        return antGroups.getOrDefault(type, new ArrayList<>());
    }
    
    public ArrayList<Ant> getEggs() {
        return (ArrayList<Ant>) antGroups.get(GameConstants.TYPE_EGG);
    }

    public void setEggs(ArrayList<Ant> eggs) {
        antGroups.put(GameConstants.TYPE_EGG, eggs);
    }

    public ArrayList<Ant> getLarvae() {
        return (ArrayList<Ant>) antGroups.get(GameConstants.TYPE_LARVA);
    }

    public void setLarvae(ArrayList<Ant> larvae) {
        antGroups.put(GameConstants.TYPE_LARVA, larvae);
    }

    public ArrayList<Ant> getPupae() {
        return (ArrayList<Ant>) antGroups.get(GameConstants.TYPE_PUPA);
    }

    public void setPupae(ArrayList<Ant> pupae) {
        antGroups.put(GameConstants.TYPE_PUPA, pupae);
    }

    public ArrayList<Ant> getWorkers() {
        return (ArrayList<Ant>) antGroups.get(GameConstants.TYPE_WORKER);
    }

    public void setWorkers(ArrayList<Ant> workers) {
        antGroups.put(GameConstants.TYPE_WORKER, workers);
    }

    public ArrayList<Ant> getSoldiers() {
        return (ArrayList<Ant>) antGroups.get(GameConstants.TYPE_SOLDIER);
    }

    public void setSoldiers(ArrayList<Ant> soldiers) {
        antGroups.put(GameConstants.TYPE_SOLDIER, soldiers);
    }

    public ArrayList<Ant> getMajors() {
        return (ArrayList<Ant>) antGroups.get(GameConstants.TYPE_MAJOR);
    }

    public void setMajors(ArrayList<Ant> majors) {
        antGroups.put(GameConstants.TYPE_MAJOR, majors);
    }

    public ArrayList<Ant> getDrones() {
        return (ArrayList<Ant>) antGroups.get(GameConstants.TYPE_DRONE);
    }

    public void setDrones(ArrayList<Ant> drones) {
        antGroups.put(GameConstants.TYPE_DRONE, drones);
    }

    public ArrayList<Ant> getPrincesses() {
        return (ArrayList<Ant>) antGroups.get(GameConstants.TYPE_PRINCESS);
    }

    public void setPrincesses(ArrayList<Ant> princesses) {
        antGroups.put(GameConstants.TYPE_PRINCESS, princesses);
    }

    public ArrayList<Ant> getQueens() {
        return (ArrayList<Ant>) antGroups.get(GameConstants.TYPE_QUEEN);
    }

    public void setQueens(ArrayList<Ant> queens) {
        antGroups.put(GameConstants.TYPE_QUEEN, queens);
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

    public int getAphidCapacity() {
        return aphidCapacity;
    }

    public void setAphidCapacity(int aphidCapacity) {
        this.aphidCapacity = aphidCapacity;
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

    public float getNursingRate() {
        return nursingRate;
    }

    public void setNursingRate(float nursingRate) {
        this.nursingRate = nursingRate;
    }

    public float getGravingRate() {
        return gravingRate;
    }

    public void setGravingRate(float gravingRate) {
        this.gravingRate = gravingRate;
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

    public int getAssignedRoleCount(AntRole role) {
        return assignedRoleCounts.getOrDefault(role, 0);
    }

    public void setAssignedRoleCount(AntRole role, int count) {
        if (count >= 0) {
            assignedRoleCounts.put(role, count);
        }
    }

    public Map<AntRole, Integer> getAssignedRoleCounts() {
        return assignedRoleCounts;
    }
    
    public void startColony() {
        List<Ant> workerList = getWorkers();
        for (int i = 0; i < 9; i++) {
            workerList.add(new Ant(this, GameConstants.TYPE_WORKER));
        }
        getQueens().add(new Ant(this, GameConstants.TYPE_QUEEN));

        setAssignedRoleCount(GameConstants.ROLE_LAYER, 1);
        setAssignedRoleCount(GameConstants.ROLE_NURSE, 2);
        setAssignedRoleCount(GameConstants.ROLE_FARMER, 1);
        setAssignedRoleCount(GameConstants.ROLE_GRAVER, 1);
        setAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);
        
        this.getQueens().get(0).setRole(GameConstants.ROLE_LAYER);
        this.getWorkers().get(0).setRole(GameConstants.ROLE_NURSE);
        this.getWorkers().get(1).setRole(GameConstants.ROLE_NURSE);
        this.getWorkers().get(2).setRole(GameConstants.ROLE_FARMER);
        this.getWorkers().get(3).setRole(GameConstants.ROLE_GRAVER);
        this.getWorkers().get(4).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(5).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(6).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(7).setRole(GameConstants.ROLE_FORAGER);
        this.getWorkers().get(8).setRole(GameConstants.ROLE_FORAGER);
    }

    private void setDefaultRole(Ant ant, AntType type) {
        if (type == GameConstants.TYPE_WORKER) {
            ant.setRole(GameConstants.ROLE_FORAGER);
        } else if (type == GameConstants.TYPE_SOLDIER) {
            ant.setRole(GameConstants.ROLE_HUNTER);
        } else if (type == GameConstants.TYPE_MAJOR) {
            ant.setRole(GameConstants.ROLE_DEFENDER);
        } else if (type == GameConstants.TYPE_PRINCESS) {
            ant.setRole(GameConstants.ROLE_BREEDER);
        } else if (type == GameConstants.TYPE_QUEEN) {
            ant.setRole(GameConstants.ROLE_LAYER);
        } else {
            ant.setRole(null);
        }
    }

    private void assignRolesForType(List<Ant> ants, AntType type) {
        List<Ant> availableAnts = new ArrayList<>();
        for (Ant ant : ants) {
            setDefaultRole(ant, type);
            availableAnts.add(ant);
        }

        for (Map.Entry<AntRole, Integer> entry : assignedRoleCounts.entrySet()) {
            AntRole role = entry.getKey();
            if (role.getAntType() != type) continue; 

            int assigned = entry.getValue();
            int assignedCount = 0;
            Iterator<Ant> iterator = availableAnts.iterator();
            while (assignedCount < assigned && iterator.hasNext()) {
                Ant antToAssign = iterator.next();
                antToAssign.setRole(role);
                iterator.remove();
                assignedCount++;
            }
        }
    }

    public void runRoleAssignment() {
        assignRolesForType(getWorkers(), GameConstants.TYPE_WORKER);
        assignRolesForType(getSoldiers(), GameConstants.TYPE_SOLDIER);
        assignRolesForType(getMajors(), GameConstants.TYPE_MAJOR);
        assignRolesForType(getPrincesses(), GameConstants.TYPE_PRINCESS);
        assignRolesForType(getQueens(), GameConstants.TYPE_QUEEN);
    }

    private int countAntsByRole(List<Ant> antList, AntRole role) {
        int count = 0;
        for (Ant ant : antList) {
            if (role.equals(ant.getRole())) {
                count++;
            }
        }
        return count;
    }

    private AntType determineHatchType() {
        double rand = Math.random();
        if (rand < 0.8) {
            return GameConstants.TYPE_WORKER;
        } else if (rand < 0.90) {
            return GameConstants.TYPE_SOLDIER;
        } else if (rand < 0.97) {
            // if () {
            //     return GameConstants.TYPE_MAJOR;
            // } else {
            //     return GameConstants.TYPE_SOLDIER;
            // }
            return GameConstants.TYPE_SOLDIER;
        } else if (rand < 0.99) {
            // if () {
            //     return GameConstants.TYPE_DRONE;
            // } else {
            //     return GameConstants.TYPE_SOLDIER;
            // }
            return GameConstants.TYPE_SOLDIER;
        } else {
            // if () {
            //     return GameConstants.TYPE_PRINCESS;
            // } else {
            //     return GameConstants.TYPE_SOLDIER;
            // }
            return GameConstants.TYPE_SOLDIER;
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

    // --- Routine Colony Activities (Always happen) --- //

    public void runHatching(){    
        hatchPupae();

        evolveAnts(getLarvae(), getPupae(), GameConstants.TYPE_PUPA);

        evolveAnts(getEggs(), getLarvae(), GameConstants.TYPE_LARVA);
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
            GameConstants.TYPE_DRONE,
            GameConstants.TYPE_PRINCESS,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_LARVA,
            GameConstants.TYPE_WORKER,
            GameConstants.TYPE_QUEEN
        );

        for (AntType typeToKill : killOrder) {
            List<Ant> list = antGroups.get(typeToKill);
            int antConsumption = (int) (typeToKill.getConsumptionMult() * this.getBaseConsumption());
            if (antConsumption <= 0) antConsumption = 1; 

            while (deficit > 0 && !list.isEmpty()) {
                Ant deadAnt = list.remove(list.size() - 1); 
                deadAnt.goDie();
                this.deadAnts.add(deadAnt);
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
                    ant.goDie();
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
            princess.transform(this, GameConstants.TYPE_QUEEN);
            queens.add(princess);
            iterator.remove(); 
            Ant deadDrone = drones.remove(drones.size() - 1); 
            this.deadAnts.add(deadDrone);
        }
    }

    public void runSpreading(){
    }

    // --- Ant Jobs (Role dependant) --- //

    public void runLaying(){
        int layerCount = countAntsByRole(getQueens(), GameConstants.ROLE_LAYER);
        List<Ant> eggList = getEggs();
        if (eggList.size() >= this.getEggsCapacity()) {
            return;
        }   
        
        int toLay = layerCount * this.getLayingRate();
        for (int i = 0; i < toLay; i++) {
            if (eggList.size() >= this.getEggsCapacity()) {
                break; 
            }
            eggList.add(new Ant(this, GameConstants.TYPE_EGG));
        }
    }

    public void runGraveKeeping(){
        int graverCount = countAntsByRole(getWorkers(), GameConstants.ROLE_GRAVER);
        if (graverCount == 0 || getDeadAnts().isEmpty()) return;

        int canClean = graverCount * (int) gravingRate;
        Iterator<Ant> iterator = getDeadAnts().iterator();
        while (canClean > 0 && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            canClean--;
        }
    }

    public void runNursing(){
        int nurseCount = countAntsByRole(getWorkers(), GameConstants.ROLE_NURSE);
        int babyAntTotal = this.getEggs().size() +  this.getLarvae().size() +  this.getPupae().size();

        if (babyAntTotal >= nurseCount * nursingRate) {
            return;
        }

        int deficit = babyAntTotal - (nurseCount * (int) nursingRate);

        List<AntType> killOrder = Arrays.asList(
            GameConstants.TYPE_LARVA,
            GameConstants.TYPE_EGG,
            GameConstants.TYPE_PUPA
        );

        for (AntType typeToKill : killOrder) {
            List<Ant> list = antGroups.get(typeToKill);
            while (deficit > 0 && !list.isEmpty()) {
                Ant deadAnt = list.remove(list.size() - 1); 
                double rand = Math.random();
                if (rand > 0.8) {
                    deadAnt.goDie();
                }
                this.deadAnts.add(deadAnt);
                deficit -= 1;
            }

            if (deficit <= 0) {
                break; 
            }
        }
    }

    public void runCollecting(){
        int foragerCount = countAntsByRole(getWorkers(), GameConstants.ROLE_FORAGER);
        int plantGain = (int) (foragerCount * getBaseAttackSpeed() * GameConstants.TYPE_WORKER.getAttackSpeedMult());
        this.setPlants(Math.min(this.getPlants() + plantGain, this.getPlantsCapacity()));

        int warriorCount = countAntsByRole(getSoldiers(), GameConstants.ROLE_HUNTER);
        int proteinGain = (int) (warriorCount * getBaseAttackSpeed() * GameConstants.TYPE_SOLDIER.getAttackSpeedMult());
        this.setProtein(Math.min(this.getProtein() + proteinGain, this.getProteinCapacity()));
    }

    public void runConverting(){
        int farmerCount = countAntsByRole(getWorkers(), GameConstants.ROLE_FARMER);
        if (this.getMushrooms() >= this.getMushroomsCapacity()) {
            return;
        }

        int conversionAmount = ((int) this.getConversionRate()) * farmerCount;
        
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

    public void runRanching(){
        int farmerCount = countAntsByRole(getWorkers(), GameConstants.ROLE_RANCHER);
    }
}