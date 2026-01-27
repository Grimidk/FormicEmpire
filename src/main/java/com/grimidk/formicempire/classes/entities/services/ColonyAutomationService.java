package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

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
        if (colony.isPlayer()) return;
        if (!colony.isAutomationEnabled()) return;

        Map<AntRole, Integer> roleQuotas = calculateNeedsBasedQuotas(colony);
        applyQuotas(colony, roleQuotas);
    }

    public void runDailyAutomation(Colony colony) {
        if (colony.isPlayer()) return;
        if (!colony.isAutomationEnabled()) return;

        checkAndBuyUpgrades(colony);
        checkAndConstructBuildings(colony);
    }

    private void checkAndBuyUpgrades(Colony colony) {
        List<Upgrade> candidates = new ArrayList<>();
        for (Upgrade u : GameUnlocks.getUpgrades()) {
            boolean notOwned = !colony.hasUpgrade(u);
            boolean reqMet = (u.getRequirement() == null || colony.hasUpgrade(u.getRequirement()));
            boolean validCost = u.getCost() > 0;
            boolean canAfford = colony.getResearchPoints() >= u.getCost();

            if (notOwned && reqMet && validCost && canAfford) {
                candidates.add(u);
            }
        }
        
        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingInt(Upgrade::getCost));
            Upgrade target = candidates.get(0);
            
            colony.setResearchPoints(colony.getResearchPoints() - target.getCost());
            colony.unlockUpgrade(target);
            colony.logEvent("AUTOMATION: Researched " + target.getName());
        }
    }

    private void checkAndConstructBuildings(Colony colony) {
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
            colony.logEvent("AUTOMATION: Started construction of " + target.getName());
        }
    }

    private Map<AntRole, Integer> calculateNeedsBasedQuotas(Colony colony) {
        Map<AntRole, Integer> targets = new HashMap<>();
        for (AntRole role : GameConstants.getAntRoles()) targets.put(role, 0);

        calculateWorkerQuotas(colony, targets);
        calculateSoldierQuotas(colony, targets);
        calculatePrincessQuotas(colony, targets);
        calculateQueenQuotas(colony, targets);

        return targets;
    }

    private void calculateWorkerQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int remaining = colony.getWorkers().size();
        if (remaining == 0) return;

        // 1. Minimum Essentials
        int farmers = assignMinimum(remaining, MIN_FARMERS);
        remaining -= farmers;
        
        int foragers = assignMinimum(remaining, MIN_FORAGERS);
        remaining -= foragers;

        int nurses = assignMinimum(remaining, MIN_NURSES);
        remaining -= nurses;

        ColonyStatsService stats = colony.getStatsService();

        // 2. Critical Graving
        int gravers = calculateGraverNeeds(colony, remaining, stats);
        remaining -= gravers;
        targets.put(GameConstants.ROLE_GRAVER, gravers);

        // 3. Nurse Scaling
        int extraNurses = calculateExtraNurseNeeds(colony, remaining, nurses, stats);
        nurses += extraNurses;
        remaining -= extraNurses;
        targets.put(GameConstants.ROLE_NURSE, nurses);

        // 4. Farmer Scaling
        int extraFarmers = calculateExtraFarmerNeeds(colony, remaining, farmers, stats);
        farmers += extraFarmers;
        remaining -= extraFarmers;
        targets.put(GameConstants.ROLE_FARMER, farmers);

        // 5. Specialized Roles
        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
            int toAdd = Math.min(4, remaining);
            targets.put(GameConstants.ROLE_RANCHER, toAdd);
            remaining -= toAdd;
        }

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) {
            int toAdd = Math.min(farmers, remaining);
            targets.put(GameConstants.ROLE_SCOUT, toAdd);
            remaining -= toAdd;
        }

        if (remaining > 0 && colony.getCurrentBuildingProject() != null) {
            boolean secureResources = colony.getPlants() > 100 && colony.getWater() > 100;
            if (secureResources) {
                int builderTarget = (int) (colony.getWorkers().size() * 0.3);
                int toAdd = Math.min(remaining, builderTarget); 
                targets.put(GameConstants.ROLE_BUILDER, toAdd);
                remaining -= toAdd;
            }
        }

        // 6. Remaining Foragers
        if (remaining > 0) {
            foragers += remaining;
        }
        targets.put(GameConstants.ROLE_FORAGER, foragers);
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

        // 1. Police
        int assignedPolice = 0;
        if (colony.getParasites() > 0 && colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            int maxPolice = (int) (colony.getSoldiers().size() * 0.20);
            assignedPolice = Math.min(maxPolice, remainingSoldiers);
            
            if (assignedPolice == 0 && maxPolice > 0 && remainingSoldiers > 0) assignedPolice = 1;
        }
        targets.put(GameConstants.ROLE_POLICE, assignedPolice);
        remainingSoldiers -= assignedPolice;

        // 2. Hunters
        boolean proteinNeeded = colony.getProtein() < colony.getStatsService().getProteinCapacity(colony);
        if (proteinNeeded && colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            targets.put(GameConstants.ROLE_HUNTER, remainingSoldiers);
        }
    }

    private void calculatePrincessQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalPrincesses = colony.getPrincesses().size();
        if (totalPrincesses == 0) return;
        
        // 1. Assistants
        int assistantCount = (int) (totalPrincesses * 0.10);
        targets.put(GameConstants.ROLE_ASSISTANT, assistantCount);
        
        // 2. Breeders
        int breederCount = totalPrincesses - assistantCount;
        targets.put(GameConstants.ROLE_BREEDER, breederCount);
    }

    private void calculateQueenQuotas(Colony colony, Map<AntRole, Integer> targets) {
        List<Ant> queens = colony.getQueens();
        int totalQueens = queens.size();
        if (totalQueens == 0) return;

        int waterCapacity = colony.getStatsService().getWaterCapacity(colony);
        int totalAnts = colony.getAntTotal();

        if (totalAnts >= waterCapacity && colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) {
            if (totalQueens == 1) {
                targets.put(GameConstants.ROLE_RESEARCHER, 1);
            } else {
                int half = totalQueens / 2;
                int researchers = half;
                targets.put(GameConstants.ROLE_RESEARCHER, researchers);
                targets.put(GameConstants.ROLE_LAYER, researchers);
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