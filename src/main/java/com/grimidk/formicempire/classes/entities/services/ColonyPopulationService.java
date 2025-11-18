package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    public void runLaying(Colony colony) {
        int layerCount = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
        List<Ant> eggList = colony.getEggs();
        
        int spaceAvailable = colony.getStatsService().getEggsCapacity(colony) - eggList.size();
        if (spaceAvailable <= 0) return;   
        
        int toLay = Math.min(layerCount * (int) colony.getStatsService().getLayingRate(colony), spaceAvailable);
        for (int i = 0; i < toLay; i++) {
            Ant newEgg = new Ant(colony, GameConstants.TYPE_EGG);
            eggList.add(newEgg);
        }
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

    public void runNursing(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        int nurseCount = colony.getAssignedRoleCount(GameConstants.ROLE_NURSE);
        int babyAntTotal = colony.getEggs().size() +  colony.getLarvae().size() +  colony.getPupae().size();
        float nursingRate = stats.getNursingRate(colony);

        if (babyAntTotal <= nurseCount * nursingRate) {
            return;
        }

        int deficit = babyAntTotal - (nurseCount * (int) nursingRate);
        List<AntType> killOrder = List.of(GameConstants.TYPE_LARVA, GameConstants.TYPE_EGG, GameConstants.TYPE_PUPA);
        int deathCount = 0;

        for (AntType typeToKill : killOrder) {
            if (deficit <= 0) break;
            List<Ant> list = colony.getAntsByType(typeToKill);
            List<Ant> antsToCull = new ArrayList<>();
            
            for (Ant ant : list) {
                if (deficit <= 0) break;
                if (Math.random() > 0.5) { 
                    antsToCull.add(ant);
                    deficit--;
                } 
            }
            
            for (Ant antToCull : antsToCull) {
                if (antToCull.isAlive()) {
                    antToCull.goDie(); 
                    colony.getDeadAnts().add(antToCull);
                    list.remove(antToCull);
                    deathCount++;
                }
            }
        }
        
        if (deathCount > 0) {
            colony.logEvent("WARNING: " + deathCount + " Juveniles Died (Nursing)");
        }
    }
    
    public void runSpreading(Colony colony, List<Ant> princesses) { 
        // Placeholder
    }

    public void runPolicing(Colony colony) {
        // Placeholder
    }

    public void runNuptial(Colony colony) {
        List<Ant> princesses = colony.getPrincesses();
        List<Ant> drones = colony.getDrones();

        if (!colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) return;

        List<Ant> princessesToEvolve = new ArrayList<>();
        boolean flightOccurred = false;
        int queenCapacity = colony.getStatsService().getQueensCapacity(colony);
        
        for (Ant princess : princesses) {
            boolean hasQueenSpace = (colony.getQueens().size() + princessesToEvolve.size()) < queenCapacity;
            boolean hasDrones = !drones.isEmpty();     
            
            if (hasQueenSpace && hasDrones) {
                princessesToEvolve.add(princess);
                Ant deadDrone = drones.remove(drones.size() - 1); 
                deadDrone.goDie();
                colony.getDeadAnts().add(deadDrone);
                flightOccurred = true;
            } 
        }

        for (Ant princess : princessesToEvolve) {
            princess.transform(colony, GameConstants.TYPE_QUEEN);
            colony.getQueens().add(princess);
        }
        
        princesses.removeAll(princessesToEvolve);
        runSpreading(colony, princesses); 
        
        if (flightOccurred) {
            colony.logEvent("CRITICAL: Nuptial Flight Occurred");
        }
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