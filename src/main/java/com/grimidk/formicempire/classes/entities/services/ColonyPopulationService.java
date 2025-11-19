package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColonyPopulationService {

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

    public void runHatching(Colony colony){    
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
        
        for (Ant ant : antsToKill) {
             if (ant.isAlive()) { 
                AntType originalType = ant.getType(); 
                ant.goDie();
                colony.getDeadAnts().add(ant);
                List<Ant> antList = colony.getAntsByType(originalType);
                if (antList != null) antList.remove(ant);
            }
        }
    }
    
    public void runEating(Colony colony){
        ColonyStatsService stats = colony.getStatsService();
        
        // --- 1. Water Consumption ---
        List<Ant> thirstyAnts = new ArrayList<>();
        int waterAvailable = colony.getWater();
        List<AntType> adultDrinkOrder = Arrays.asList(
            GameConstants.TYPE_QUEEN, GameConstants.TYPE_WORKER, GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR, GameConstants.TYPE_PRINCESS, GameConstants.TYPE_DRONE
        );
        for (AntType type : adultDrinkOrder) {
            List<Ant> list = colony.getAntsByType(type);
            for (Ant ant : list) {
                if (waterAvailable >= 1) {
                    waterAvailable -= 1;
                } else {
                    thirstyAnts.add(ant);
                }
            }
        }
        colony.setWater(waterAvailable);
        
        // --- 2. Food Consumption ---
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
        colony.setMushrooms(mushroomsAvailable);
        
        // --- 3. Syrup Phase ---
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
        
        // --- 4. Death Phase ---
        Set<Ant> antsToKill = new HashSet<>();
        for (Ant ant : thirstyAnts) {
            if (Math.random() < 0.25) antsToKill.add(ant);
        }
        for (Ant ant : hungryAnts) {
            antsToKill.add(ant); 
        }
        
        for (Ant ant : antsToKill) {
            if (ant.isAlive()) { 
                AntType originalType = ant.getType(); 
                ant.goDie();
                colony.setTotalDeaths(colony.getTotalDeaths() + 1);
                colony.getDeadAnts().add(ant);
                List<Ant> antList = colony.getAntsByType(originalType);
                if (antList != null) antList.remove(ant);
            }
        }

        if (antsToKill.size() > 0) {
            colony.logEvent("WARNING: " + antsToKill.size() + " Ants Died (Starvation/Dehydration)");
        }
    }
    
    public void runInfection(Colony colony) { 
        if (colony.getDeadAnts().size() < 100) {
            return;
        }
        // Placeholder for infection logic
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