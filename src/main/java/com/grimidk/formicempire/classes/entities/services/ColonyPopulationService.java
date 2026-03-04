package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.world.Temperature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ColonyPopulationService {
    
    private final Random random = new Random();
    
    // --- Death Statistics ---
    private final Map<String, Integer> deathCauses = new ConcurrentHashMap<>();

    public ColonyPopulationService() {
        initializeDeathCauses();
    }

    private void initializeDeathCauses() {
        deathCauses.put("Old Age", 0);
        deathCauses.put("Starvation", 0);
        deathCauses.put("Dehydration", 0);
        deathCauses.put("Contamination", 0);
        deathCauses.put("Conflict", 0);
        deathCauses.put("Illness", 0);
        deathCauses.put("Lack of Care", 0);
        deathCauses.put("Other", 0);
    }

    public void recordDeath(String cause) {
        recordDeath(cause, null);
    }

    public void recordDeath(String cause, Colony colony) {
        String key = "Other";
        if (cause != null) {
            if (deathCauses.containsKey(cause)) key = cause;
            else if (cause.equalsIgnoreCase("Combat")) key = "Conflict";
        }
        deathCauses.merge(key, 1, Integer::sum);
        
        if (colony != null && colony.getDynasty() != null) {
            colony.getDynasty().recordDeath(key);
        }
    }

    public Map<String, Integer> getDeathStatistics() {
        return Collections.unmodifiableMap(deathCauses);
    }
    
    public void loadDeathStatistics(Map<String, Integer> savedStats) {
        if (savedStats != null) {
            this.deathCauses.putAll(savedStats);
        }
    }

    public void resetDeathStatistics() {
        deathCauses.clear();
        initializeDeathCauses();
    }

    // --- Role Management ---
    private AntRole getDefaultRoleForType(AntType type) {
        if (type == GameConstants.TYPE_WORKER) return GameConstants.ROLE_FORAGER;
        else if (type == GameConstants.TYPE_SOLDIER) return GameConstants.ROLE_HUNTER;
        else if (type == GameConstants.TYPE_MAJOR) return GameConstants.ROLE_BRUTE; 
        else if (type == GameConstants.TYPE_PRINCESS) return GameConstants.ROLE_BREEDER;
        else if (type == GameConstants.TYPE_DRONE) return GameConstants.ROLE_DRONE;
        else if (type == GameConstants.TYPE_QUEEN) return GameConstants.ROLE_LAYER;
        return null; 
    }

    private void assignRolesForType(Colony colony, List<Ant> ants, AntType type) {
        AntRole defaultRole = getDefaultRoleForType(type);
        if (defaultRole == null) return; 
        
        List<Ant> tradeAnts = new ArrayList<>();
        List<Ant> availableAnts = new ArrayList<>();
        
        for (Ant ant : ants) {
            if (ant.isOnTrade()) {
                tradeAnts.add(ant);
            } else {
                ant.setRole(defaultRole);
                availableAnts.add(ant);
            }
        }
        
        Map<AntRole, Integer> assignedRoleCounts = colony.getAssignedRoleCounts();
        
        for (Map.Entry<AntRole, Integer> entry : assignedRoleCounts.entrySet()) {
            AntRole role = entry.getKey();
            if (role.getAntType() != type) continue;
            if (role.equals(defaultRole)) continue; 
            
            int desiredCount = entry.getValue();
            
            int currentlyOnTradeWithThisRole = 0;
            for (Ant ta : tradeAnts) {
                if (ta.getRole() != null && ta.getRole().equals(role)) {
                    currentlyOnTradeWithThisRole++;
                }
            }
            
            int neededCount = Math.max(0, desiredCount - currentlyOnTradeWithThisRole);
            int assignedCount = 0;
            Iterator<Ant> antIterator = availableAnts.iterator();
            
            while (assignedCount < neededCount && antIterator.hasNext()) {
                Ant antToAssign = antIterator.next();
                antToAssign.setRole(role); 
                antIterator.remove();
                assignedCount++;
            }
        }
    }

    public void runRoleAssignment(Colony colony) {
        assignRolesForType(colony, colony.getWorkers(), GameConstants.TYPE_WORKER);
        assignRolesForType(colony, colony.getSoldiers(), GameConstants.TYPE_SOLDIER);
        assignRolesForType(colony, colony.getMajors(), GameConstants.TYPE_MAJOR);
        assignRolesForType(colony, colony.getPrincesses(), GameConstants.TYPE_PRINCESS);
        assignRolesForType(colony, colony.getQueens(), GameConstants.TYPE_QUEEN);
    }

    // --- Hatching & Lifecycle ---
    private AntType determineHatchType(Colony colony) {
        double rand = random.nextDouble() * 100.0;
        double cumulative = 0.0;
        
        cumulative += colony.getHatchRateWorker();
        if (rand < cumulative) return GameConstants.TYPE_WORKER;
        
        if (colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            cumulative += colony.getHatchRateSoldier();
            if (rand < cumulative) return GameConstants.TYPE_SOLDIER;
        }
        if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
            cumulative += colony.getHatchRateMajor();
            if (rand < cumulative) return GameConstants.TYPE_MAJOR;
        }
        if (colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            cumulative += colony.getHatchRateDrone();
            if (rand < cumulative) return GameConstants.TYPE_DRONE;
            cumulative += colony.getHatchRatePrincess();
            if (rand < cumulative) return GameConstants.TYPE_PRINCESS;
        }
        return GameConstants.TYPE_WORKER;
    }

    private void evolveAnts(Colony colony, List<Ant> sourceList, List<Ant> destList, AntType newType) {
        List<Ant> antsToEvolve = new ArrayList<>();
        int growthTime = colony.getStatsService().getGrowthTime(colony);
        
        for (Ant ant : sourceList) {
            if (ant.getAge() >= growthTime) {
                antsToEvolve.add(ant);
            }
        }
        for (Ant ant : antsToEvolve) {
            ant.transform(colony, newType); 
            destList.add(ant);
        }
        sourceList.removeAll(antsToEvolve);
    }

    private void hatchPupae(Colony colony) {
        List<Ant> pupaeToHatch = new ArrayList<>();
        int growthTime = colony.getStatsService().getGrowthTime(colony);
        
        for (Ant pupa : colony.getPupae()) {
            if (pupa.getAge() >= growthTime) {
                pupaeToHatch.add(pupa);
            }
        }
        for (Ant pupa : pupaeToHatch) {
            AntType newType = determineHatchType(colony);
            pupa.transform(colony, newType);
            colony.getAntsByType(newType).add(pupa);
        }
        colony.getPupae().removeAll(pupaeToHatch);
    }

    private void applyAutomatedHatchRates(Colony colony) {
        float s = colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER) ? 15.0f : 0f;
        float m = colony.hasUpgrade(GameUnlocks.TYPE_MAJOR) ? 5.0f : 0f;
        float p = 0f;
        float d = 0f;
        
        if (colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            p = 4.0f;
            d = 1.0f;
        }

        float w = 100.0f - (s + m + p + d);

        colony.setHatchRate(GameConstants.TYPE_WORKER, w);
        colony.setHatchRate(GameConstants.TYPE_SOLDIER, s);
        colony.setHatchRate(GameConstants.TYPE_MAJOR, m);
        colony.setHatchRate(GameConstants.TYPE_PRINCESS, p);
        colony.setHatchRate(GameConstants.TYPE_DRONE, d);
    }

    public void runHatching(Colony colony){
        if (colony.isAutomationEnabled() || (!colony.isPlayer() && colony.isCapital())) {
            applyAutomatedHatchRates(colony);
        }

        hatchPupae(colony);
        evolveAnts(colony, colony.getLarvae(), colony.getPupae(), GameConstants.TYPE_PUPA);
        evolveAnts(colony, colony.getEggs(), colony.getLarvae(), GameConstants.TYPE_LARVA);
    }

    public void runAging(Colony colony){
        List<Ant> antsToKill = new ArrayList<>();

        for (List<Ant> antList : colony.getAntGroups().values()) {
            for (Ant ant : antList) {
                ant.setAge(ant.getAge() + 1);
            }
        }
        
        int agedDeaths = 0;
        for (Ant ant : antsToKill) {
             if (ant.isAlive()) { 
                AntType originalType = ant.getAntType(); 
                ant.goDie(colony, "Old Age");
                colony.recordAntDeath(ant, "Old Age");
                
                List<Ant> antList = colony.getAntsByType(originalType);
                if (antList != null) antList.remove(ant);
                agedDeaths++;
            }
        }
        
        if (agedDeaths > 0) {
            colony.logEvent("DEATH: " + agedDeaths + " Ants died of Old Age");
        }
    }
    
    public void runEating(Colony colony, Temperature currentTemp) {
        ColonyStatsService stats = colony.getStatsService();
        ColonyResourceService resources = colony.getResourceService();
        
        List<AntType> adultDrinkOrder = Arrays.asList(
            GameConstants.TYPE_DRONE, GameConstants.TYPE_PRINCESS, GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_SOLDIER, GameConstants.TYPE_WORKER, GameConstants.TYPE_QUEEN
        );
        
        int resistanceChance = stats.getThirstResistance(colony, currentTemp);
        List<Ant> thirstyCandidates = new ArrayList<>();
        
        for (AntType type : adultDrinkOrder) {
            List<Ant> list = colony.getAntsByType(type);
            for (Ant ant : list) {
                if (random.nextInt(100) >= resistanceChance) {
                    thirstyCandidates.add(ant);
                }
            }
        }
        
        int waterNeeded = thirstyCandidates.size();
        double consumedWater = resources.consumeResource(colony, GameConstants.RESOURCE_WATER, waterNeeded);
        double waterDeficit = waterNeeded - consumedWater;
        
        if (waterDeficit > 0 && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
            double syrupConsumed = resources.consumeResource(colony, GameConstants.RESOURCE_SYRUP, waterDeficit);
            waterDeficit -= syrupConsumed;
        }

        List<Ant> doomedThirsty = new ArrayList<>();
        for (int i = 0; i < (int) waterDeficit; i++) {
            if (i < thirstyCandidates.size()) {
                doomedThirsty.add(thirstyCandidates.get(i));
            }
        }

        List<AntType> eatOrder = Arrays.asList(
            GameConstants.TYPE_DRONE, GameConstants.TYPE_PRINCESS, GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_SOLDIER, GameConstants.TYPE_LARVA, GameConstants.TYPE_WORKER, GameConstants.TYPE_QUEEN
        );
        
        List<Ant> hungryCandidates = new ArrayList<>();
        int foodNeeded = 0;
        int baseConsumption = stats.getBaseConsumption(colony);

        for (AntType type : eatOrder) {
            int consumptionPerAnt = (int) (type.getConsumptionMult() * baseConsumption);
            if (consumptionPerAnt <= 0) consumptionPerAnt = 1; 
            if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_PUPA) continue;
            
            List<Ant> list = colony.getAntsByType(type);
            for (Ant ant : list) {
                foodNeeded += consumptionPerAnt;
                hungryCandidates.add(ant); 
            }
        }

        int parasiteCount = colony.getParasites();
        if (parasiteCount > 0) {
            foodNeeded += parasiteCount; 
        }

        double consumedFood = resources.consumeResource(colony, GameConstants.RESOURCE_FUNGI, foodNeeded);
        double foodDeficit = foodNeeded - consumedFood;

        if (foodDeficit > 0 && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
            double syrupConsumed = resources.consumeResource(colony, GameConstants.RESOURCE_SYRUP, foodDeficit);
            foodDeficit -= syrupConsumed;
        }

        List<Ant> doomedHungry = new ArrayList<>();
        int approxAntsToKill = (int) (foodDeficit / (baseConsumption <= 0 ? 1 : baseConsumption));
        
        int added = 0;
        for (int i = 0; i < hungryCandidates.size() && added < approxAntsToKill; i++) {
            Ant potentialVictim = hungryCandidates.get(i);
            if (!doomedThirsty.contains(potentialVictim)) {
                doomedHungry.add(potentialVictim);
                added++;
            }
        }

        processDeaths(colony, doomedThirsty, "Dehydration");
        processDeaths(colony, doomedHungry, "Starvation");
    }

    private void processDeaths(Colony colony, List<Ant> ants, String cause) {
        int count = 0;
        for (Ant ant : ants) {
            if (ant.isAlive()) {
                AntType originalType = ant.getAntType();
                ant.goDie(colony, cause);
                colony.recordAntDeath(ant, cause);
                
                List<Ant> antList = colony.getAntsByType(originalType);
                if (antList != null) antList.remove(ant);
                count++;
            }
        }
        if (count > 0) {
            colony.logEvent("DEATH: " + count + " Ants died of " + cause);
        }
    }
    
    public void runContamination(Colony colony) { 
        int deadBodyCount = colony.getDeadAnts().size();
        int baseDeaths = 0;
        String contaminationLevel = "";

        if (deadBodyCount >= 5000) {
            baseDeaths = 100;
            contaminationLevel = "Massive";
        } else if (deadBodyCount >= 1500) {
            baseDeaths = 25;
            contaminationLevel = "Medium";
        } else if (deadBodyCount >= 500) {
            baseDeaths = 5;
            contaminationLevel = "Small";
        } else {
            return;
        }

        float mitigation = colony.getStatsService().getContaminationMitigation(colony);
        int finalDeaths = (int) (baseDeaths * mitigation);
        
        if (finalDeaths <= 0) return;

        List<Ant> victims = new ArrayList<>();
        List<AntType> killableTypes = Arrays.asList(GameConstants.TYPE_WORKER, GameConstants.TYPE_SOLDIER, GameConstants.TYPE_MAJOR);
        
        int killed = 0;
        for (AntType type : killableTypes) {
            if (killed >= finalDeaths) break;
            List<Ant> population = colony.getAntsByType(type);
            
            for (Ant ant : population) {
                if (killed >= finalDeaths) break;
                if (random.nextFloat() < 0.1) {
                    victims.add(ant);
                    killed++;
                }
            }
        }

        processDeaths(colony, victims, "Contamination");
        if (killed > 0) {
            colony.logEvent("Contamination Level: " + contaminationLevel);
        }
    }

    public void runParasitation(Colony colony) {
        if (colony.getAntTotal() < 1000) return;
        
        int spawnAmount = Math.max(10, (int)(colony.getAntTotal() * 0.01));
        int existingParasites = colony.getParasites();

        if (existingParasites > 0) {
            spawnAmount += (int)(existingParasites * 0.50);
        }
        
        if (spawnAmount <= 0) return;
        
        colony.setParasites(colony.getParasites() + spawnAmount);
        colony.logEvent("A parasitic infestation has spread! " + spawnAmount + " new parasites detected.");
    }

    public void rankUp(Colony colony) {
        int total = colony.getAntTotal();
        if (total >= GameConstants.RANK_GIGA.getPopulation()) colony.setRank(GameConstants.RANK_GIGA);
        else if (total >= GameConstants.RANK_SUPREME.getPopulation()) colony.setRank(GameConstants.RANK_SUPREME);
        else if (total >= GameConstants.RANK_ULTIMATE.getPopulation()) colony.setRank(GameConstants.RANK_ULTIMATE);
        else if (total >= GameConstants.RANK_MEGA.getPopulation()) colony.setRank(GameConstants.RANK_MEGA);
        else if (total >= GameConstants.RANK_HYPER.getPopulation()) colony.setRank(GameConstants.RANK_HYPER);
        else if (total >= GameConstants.RANK_ULTRA.getPopulation()) colony.setRank(GameConstants.RANK_ULTRA);
        else if (total >= GameConstants.RANK_SUPER.getPopulation()) colony.setRank(GameConstants.RANK_SUPER);
        else if (total >= GameConstants.RANK_EMPIRE.getPopulation()) colony.setRank(GameConstants.RANK_EMPIRE);
        else if (total >= GameConstants.RANK_KINGDOM.getPopulation()) colony.setRank(GameConstants.RANK_KINGDOM);
        else if (total >= GameConstants.RANK_DUCHY.getPopulation()) colony.setRank(GameConstants.RANK_DUCHY);
        else if (total >= GameConstants.RANK_COUNTY.getPopulation()) colony.setRank(GameConstants.RANK_COUNTY);
        else if (total >= GameConstants.RANK_COLONY.getPopulation()) colony.setRank(GameConstants.RANK_COLONY);
        else colony.setRank(GameConstants.RANK_ANT);
    }
}