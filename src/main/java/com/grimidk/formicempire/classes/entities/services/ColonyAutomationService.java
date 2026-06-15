package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColonyAutomationService {

    private static final int MIN_NURSES = 4;
    private static final int MIN_FORAGERS = 4;
    private static final int MIN_FARMERS = 1;

    public void runAutomation(Colony colony) {
        if (!colony.isAutomationEnabled()) return;

        Map<AntRole, Integer> roleQuotas = calculateNeedsBasedQuotas(colony);
        applyQuotas(colony, roleQuotas);
    }

    public void runDailyAutomation(Colony colony) {
        if (!colony.isAutomationEnabled()) return;

        checkAndConstructBuildings(colony);
    }
    
    public void runAutoBuild(Colony colony) {
        checkAndConstructBuildings(colony);
    }

    public void checkAndConstructBuildings(Colony colony) {
        if (colony.getCurrentBuildingProject() != null) return;

        List<Building> candidates = new ArrayList<>();
        for (Building b : GameUnlocks.getBuildings()) {
            boolean notOwned = !colony.hasBuilding(b);
            boolean reqMet = (b.getRequirement() == null || colony.hasBuilding(b.getRequirement()));
            boolean canAfford = colony.getMinerals() >= b.getMineralCost() && colony.getResins() >= b.getResinCost();

            if (notOwned && reqMet && canAfford) {
                candidates.add(b);
            }
        }

        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingInt(b -> b.getMineralCost() + b.getResinCost()));
            Building target = candidates.get(0);
            
            colony.startBuildingProject(target);
            colony.logEvent(ColonyLogPrefixes.AUTOMATION + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_AUTOMATION_BUILD_FMT), target.getName()));
        }
    }

    private Map<AntRole, Integer> calculateNeedsBasedQuotas(Colony colony) {
        Map<AntRole, Integer> targets = new HashMap<>();
        for (AntRole role : GameConstants.getAntRoles()) targets.put(role, 0);

        calculateWorkerQuotas(colony, targets);
        calculateSoldierQuotas(colony, targets);
        calculateMajorQuotas(colony, targets);
        calculatePrincessQuotas(colony, targets);
        calculateQueenQuotas(colony, targets);

        return targets;
    }

    private void calculateWorkerQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalWorkers = colony.getWorkers().size();
        int remaining = totalWorkers;
        if (remaining == 0) return;

        ColonyStatsService stats = colony.getStatsService();

        int farmerTarget = Math.max(MIN_FARMERS, (int) (totalWorkers * 0.05));
        int farmers = assignMinimum(remaining, farmerTarget);
        remaining -= farmers;
        
        int foragers = assignMinimum(remaining, MIN_FORAGERS);
        remaining -= foragers;

        float nursingRate = stats.getNursingRate(colony);
        int maxBrood = stats.getEggsCapacity(colony) * 3; 
        int nurseTarget = MIN_NURSES;
        if (nursingRate > 0) {
            nurseTarget = Math.max(MIN_NURSES, (int) Math.ceil(maxBrood / nursingRate));
        }
        int nurses = assignMinimum(remaining, nurseTarget);
        remaining -= nurses;

        int graverTarget = (int) (totalWorkers * 0.05);
        int gravers = assignMinimum(remaining, graverTarget);
        remaining -= gravers;

        int totalGraversNeeded = calculateGraverNeeds(colony, remaining + gravers, stats);
        if (totalGraversNeeded > gravers) {
            int extraGravers = Math.min(remaining, totalGraversNeeded - gravers);
            gravers += extraGravers;
            remaining -= extraGravers;
        }
        targets.put(GameConstants.ROLE_GRAVER, gravers);

        int extraNurses = calculateExtraNurseNeeds(colony, remaining, nurses, stats);
        nurses += extraNurses;
        remaining -= extraNurses;
        targets.put(GameConstants.ROLE_NURSE, nurses);

        int extraFarmers = calculateExtraFarmerNeeds(colony, remaining, farmers, stats);
        farmers += extraFarmers;
        remaining -= extraFarmers;
        targets.put(GameConstants.ROLE_FARMER, farmers);

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER) && colony.getAphids() > 0) {
            int rancherTarget = Math.max(1, (int) (totalWorkers * 0.05));
            int toAdd = Math.min(rancherTarget, remaining);
            targets.put(GameConstants.ROLE_RANCHER, toAdd);
            remaining -= toAdd;
        }

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) {
            int scoutTarget = Math.max(1, (int) (totalWorkers * 0.02));
            int toAdd = Math.min(scoutTarget, remaining);
            targets.put(GameConstants.ROLE_SCOUT, toAdd);
            remaining -= toAdd;
        }

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_MINER)) {
            int minerTarget = (int) (totalWorkers * 0.15);
            int toAdd = Math.min(remaining, minerTarget);
            
            if (colony.getMinerals() >= colony.getMineralsCapacity()) {
                toAdd = Math.min(remaining, Math.max(1, (int) (minerTarget * 0.10))); 
            }
            
            targets.put(GameConstants.ROLE_MINER, toAdd);
            remaining -= toAdd;
        }

        if (remaining > 0) {
            int foragerMin = (int) (totalWorkers * 0.10);
            if (foragers < foragerMin) {
                int toAdd = Math.min(remaining, foragerMin - foragers);
                foragers += toAdd;
                remaining -= toAdd;
            }
        }

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_BUILDER)) {
            if (colony.getCurrentBuildingProject() != null) {
                targets.put(GameConstants.ROLE_BUILDER, remaining);
                targets.put(GameConstants.ROLE_FORAGER, foragers);
            } else {
                targets.put(GameConstants.ROLE_BUILDER, 0);
                targets.put(GameConstants.ROLE_FORAGER, foragers + remaining);
            }
        } else {
            targets.put(GameConstants.ROLE_BUILDER, 0);
            targets.put(GameConstants.ROLE_FORAGER, foragers + remaining);
        }
    }
    
    private int assignMinimum(int available, int min) {
        return Math.min(min, available);
    }

    private int calculateGraverNeeds(Colony colony, int available, ColonyStatsService stats) {
        if (available <= 0) return 0;
        int deadBodies = colony.getDeadAnts().size();
        if (deadBodies == 0) return 0;

        float dailyCleaningRate = stats.getGravingRate(colony) * 24.0f;
        if (dailyCleaningRate <= 0) return 0;

        float daysToClear = 5.0f;
        if (deadBodies >= 400) daysToClear = 1.0f; 
        if (deadBodies >= 1400) daysToClear = 0.5f;

        int needed = (int) Math.ceil(deadBodies / (dailyCleaningRate * daysToClear));                    
        int doubleNeeded = needed * 2;
        
        return Math.min(doubleNeeded, available);
    }

    private int calculateExtraNurseNeeds(Colony colony, int available, int currentNurses, ColonyStatsService stats) {
        if (available <= 0) return 0;
        int totalBrood = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
        float nursingRate = stats.getNursingRate(colony);
        if (nursingRate <= 0) return 0;

        int currentCapacity = (int) (currentNurses * nursingRate);
        if (currentCapacity < totalBrood) {
            int deficit = totalBrood - currentCapacity;
            int extraNeeded = (int) Math.ceil(deficit / nursingRate);
            return Math.min(extraNeeded, available);
        }
        return 0;
    }

    private int calculateExtraFarmerNeeds(Colony colony, int available, int currentFarmers, ColonyStatsService stats) {
        if (available <= 0) return 0;
        int totalConsumption = stats.getTotalConsumption(colony);
        float conversionRate = stats.getConversionRate(colony);
        double productionPerFarmer = conversionRate * 1440.0;
        
        if (productionPerFarmer <= 0) return 0;

        int currentProduction = (int) (currentFarmers * productionPerFarmer);
        boolean needsFood = currentProduction < totalConsumption || colony.getMushrooms() < stats.getMushroomsCapacity(colony) * 0.2;
        
        if (needsFood) {
            int deficit = totalConsumption - currentProduction;
            int baseNeeded = (int) Math.ceil(deficit / productionPerFarmer);
            if (colony.getMushrooms() < stats.getMushroomsCapacity(colony) * 0.1) baseNeeded++; 
            
            int extraNeeded = baseNeeded * 3;
            return Math.min(extraNeeded, available);
        }
        return 0;
    }

    private void calculateSoldierQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int remainingSoldiers = colony.getSoldiers().size();
        if (remainingSoldiers == 0) return;

        int assignedPolice = 0;
        if (colony.getParasites() > 0 && colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            int maxPolice = (int) (colony.getSoldiers().size() * 0.20);
            assignedPolice = Math.min(maxPolice, remainingSoldiers);
            
            if (assignedPolice == 0 && maxPolice > 0 && remainingSoldiers > 0) assignedPolice = 1;
        }
        targets.put(GameConstants.ROLE_POLICE, assignedPolice);
        remainingSoldiers -= assignedPolice;

        if (remainingSoldiers > 0 && colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            targets.put(GameConstants.ROLE_HUNTER, remainingSoldiers);
        }
    }

    private void calculateMajorQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalMajors = colony.getMajors().size();
        if (totalMajors == 0) return;
        
        if (colony.getCurrentBuildingProject() != null && colony.hasUpgrade(GameUnlocks.ROLE_CRANE)) {
            targets.put(GameConstants.ROLE_CRANE, totalMajors);
            targets.put(GameConstants.ROLE_BRUTE, 0);
        } else {
            targets.put(GameConstants.ROLE_CRANE, 0);
            targets.put(GameConstants.ROLE_BRUTE, totalMajors);
        }
    }

    private void calculatePrincessQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalPrincesses = colony.getPrincesses().size();
        if (totalPrincesses == 0) return;
        
        int assistantCount = (int) (totalPrincesses * 0.80);
        targets.put(GameConstants.ROLE_ASSISTANT, assistantCount);
        
        int breederCount = totalPrincesses - assistantCount;
        targets.put(GameConstants.ROLE_BREEDER, breederCount);
    }

    private void calculateQueenQuotas(Colony colony, Map<AntRole, Integer> targets) {
        List<Ant> queens = colony.getQueens();
        int totalQueens = queens.size();
        if (totalQueens == 0) return;

        int waterCapacity = colony.getStatsService().getWaterCapacity(colony);
        int totalAnts = colony.getAntTotal();

        if (totalAnts >= waterCapacity && colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER) && colony.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT) <= 25) {
            if (totalQueens == 1) {
                targets.put(GameConstants.ROLE_RESEARCHER, 1);
            } else {
                int half = totalQueens / 2;
                int researchers = half;
                targets.put(GameConstants.ROLE_RESEARCHER, researchers);
                targets.put(GameConstants.ROLE_LAYER, totalQueens - researchers);
            }
        } else {
            targets.put(GameConstants.ROLE_LAYER, totalQueens);
        }
    }

    private void applyQuotas(Colony colony, Map<AntRole, Integer> quotas) {
        for (Map.Entry<AntRole, Integer> entry : quotas.entrySet()) {
            colony.setAssignedRoleCount(entry.getKey(), entry.getValue());
        }
    }
}