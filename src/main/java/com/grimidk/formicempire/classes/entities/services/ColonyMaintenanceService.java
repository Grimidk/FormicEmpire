package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.GameUnlocks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ColonyMaintenanceService {

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
    }

    public void runGraveKeeping(Colony colony) {
        int graverCount = colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
        
        if (graverCount == 0 || colony.getDeadAnts().isEmpty()) return;
        
        int canClean = graverCount * (int) colony.getStatsService().getGravingRate(colony);
        List<Ant> antsToRemove = new ArrayList<>();
        
        for (Ant deadAnt : colony.getDeadAnts()) {
            if (canClean <= 0) break;
            antsToRemove.add(deadAnt);
            canClean--;
        }
        colony.getDeadAnts().removeAll(antsToRemove);
    }

    public void runResearch(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;
        int researcherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER);
        
        if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) {
            researcherCount += 1; 
        }
        
        colony.setResearchPoints(colony.getResearchPoints() + (researcherCount * colony.getStatsService().getResearchSpeed(colony)));
    }

    public void runBuilding(Colony colony) {
        if (colony.getCurrentBuildingProject() == null) return;
        
        int builderCount = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
        if (builderCount <= 0) return;
        
        double efficiency = builderCount / 100.0;
        if (efficiency <= 0) return;

        colony.setBuildingProgressHours(colony.getBuildingProgressHours() + 1.0);
        double requiredHours = colony.getCurrentBuildingProject().getBuildTime() / efficiency;
        
        if (colony.getBuildingProgressHours() >= requiredHours) {
            colony.unlockBuilding(colony.getCurrentBuildingProject());
            colony.logEvent("SUCCESS: Built " + colony.getCurrentBuildingProject().getName());
            colony.setCurrentBuildingProject(null);
            colony.setBuildingProgressHours(0.0);
        }
    }
    
    public void runInfection(Colony colony) { 
        if (colony.getDeadAnts().size() < 100) {
            return;
        }
        // Placeholder for infection logic
    }
}