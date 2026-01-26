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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

public class ColonyPopulationService {
    
    private Random random = new Random();

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
        for (Ant ant : ants) {
            ant.setRole(defaultRole);
        }
        
        List<Ant> availableAnts = new ArrayList<>(ants); 
        Map<AntRole, Integer> assignedRoleCounts = colony.getAssignedRoleCounts();
        Iterator<Map.Entry<AntRole, Integer>> roleIterator = assignedRoleCounts.entrySet().iterator();
        
        while(roleIterator.hasNext()) {
            Map.Entry<AntRole, Integer> entry = roleIterator.next();
            AntRole role = entry.getKey();
            if (role.getAntType() != type) continue;
            if (role.equals(defaultRole)) continue; 
            
            int desiredCount = entry.getValue();
            int assignedCount = 0;
            Iterator<Ant> antIterator = availableAnts.iterator();
            
            while (assignedCount < desiredCount && antIterator.hasNext()) {
                Ant antToAssign = antIterator.next();
                antToAssign.setRole(role); 
                antIterator.remove();
                assignedCount++;
            }
        }
        assignedRoleCounts.put(defaultRole, availableAnts.size());
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
        double rand = Math.random() * 100.0;
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

    private void adjustNPCHatchRates(Colony colony) {
        float s = 0f;
        float m = 0f;
        float p = 0f;
        float d = 0f;

        if (colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            s = 15.0f;
        }
        if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
            m = 5.0f;
        }
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
        if (!colony.isPlayer()) {
            adjustNPCHatchRates(colony);
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
    
    public void runEating(Colony colony, Temperature currentTemp){
        ColonyStatsService stats = colony.getStatsService();
        
        // --- Water Consumption ---
        List<Ant> thirstyAnts = new ArrayList<>();
        int waterAvailable = colony.getWater();
        List<AntType> adultDrinkOrder = Arrays.asList(
            GameConstants.TYPE_QUEEN, GameConstants.TYPE_WORKER, GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR, GameConstants.TYPE_PRINCESS, GameConstants.TYPE_DRONE
        );
        
        int resistanceChance = stats.getThirstResistance(colony, currentTemp);

        for (AntType type : adultDrinkOrder) {
            List<Ant> list = colony.getAntsByType(type);
            for (Ant ant : list) {
                if (random.nextInt(100) < resistanceChance) {
                    continue; 
                }

                if (waterAvailable >= 1) {
                    waterAvailable -= 1;
                } else {
                    thirstyAnts.add(ant);
                }
            }
        }
        colony.setWater(waterAvailable);
        
        // --- Food Consumption ---
        int mushroomsAvailable = colony.getMushrooms();
        List<AntType> eatOrder = Arrays.asList(
            GameConstants.TYPE_QUEEN, GameConstants.TYPE_WORKER, GameConstants.TYPE_LARVA,
            GameConstants.TYPE_SOLDIER, GameConstants.TYPE_MAJOR, GameConstants.TYPE_PRINCESS, GameConstants.TYPE_DRONE
        );
        List<Ant> hungryAnts = new ArrayList<>();
        for (AntType type : eatOrder) {
            int consumptionPerAnt = (int) (type.getConsumptionMult() * stats.getBaseConsumption(colony));
            if (consumptionPerAnt <= 0) consumptionPerAnt = 1; 
            if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_PUPA) continue;
            
            List<Ant> list = colony.getAntsByType(type);
            for (Ant ant : list) {
                if (mushroomsAvailable >= consumptionPerAnt) {
                    mushroomsAvailable -= consumptionPerAnt;
                } else {
                    hungryAnts.add(ant);
                }
            }
        }

        // --- Parasite Consumption ---
        int parasiteCount = colony.getParasites();
        if (parasiteCount > 0) {
            int parasiteConsumption = parasiteCount * 1; 
            if (mushroomsAvailable >= parasiteConsumption) {
                mushroomsAvailable -= parasiteConsumption;
            } else {
                mushroomsAvailable = 0;
            }
        }

        colony.setMushrooms(mushroomsAvailable);
        
        // --- Syrup Phase ---
        Set<Ant> antsInNeed = new HashSet<>(thirstyAnts);
        antsInNeed.addAll(hungryAnts);
        int syrupAvailable = colony.hasUpgrade(GameUnlocks.ROLE_RANCHER) ? colony.getSyrups() : 0;
        
        Iterator<Ant> needIterator = antsInNeed.iterator();
        while (needIterator.hasNext() && syrupAvailable > 0) {
            Ant ant = needIterator.next();
            syrupAvailable -= 1;
            needIterator.remove(); 
            thirstyAnts.remove(ant);
            hungryAnts.remove(ant);
        }
        colony.setSyrups(syrupAvailable);
        
        // --- Death Phase ---        
        int starvationCount = 0;
        int dehydrationCount = 0;
        
        for (Ant ant : thirstyAnts) {
            if (ant.isAlive()) {
                AntType originalType = ant.getAntType();
                ant.goDie(colony, "Dehydration");
                colony.recordAntDeath(ant, "Dehydration");
                
                List<Ant> antList = colony.getAntsByType(originalType);
                if (antList != null) antList.remove(ant);
                
                dehydrationCount++;
            }
        }
        
        for (Ant ant : hungryAnts) {
            if (ant.isAlive()) {
                AntType originalType = ant.getAntType();
                ant.goDie(colony, "Starvation");
                colony.recordAntDeath(ant, "Starvation");
                
                List<Ant> antList = colony.getAntsByType(originalType);
                if (antList != null) antList.remove(ant);
                
                starvationCount++;
            }
        }

        if (starvationCount > 0) {
            colony.logEvent("DEATH: " + starvationCount + " Ants died of Starvation");
        }
        if (dehydrationCount > 0) {
            colony.logEvent("DEATH: " + dehydrationCount + " Ants died of Dehydration");
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

        for (Ant victim : victims) {
            if (victim.isAlive()) {
                AntType originalType = victim.getAntType();
                victim.goDie(colony, "Contamination");
                colony.recordAntDeath(victim, "Contamination");

                List<Ant> antList = colony.getAntsByType(originalType);
                if (antList != null) antList.remove(victim);
            }
        }
        
        if (killed > 0) {
            colony.logEvent("DEATH: " + killed + " Ants died from " + contaminationLevel + " Contamination");
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