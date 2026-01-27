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
        int totalWorkers = colony.getWorkers().size();
        if (totalWorkers == 0) return;

        int remaining = totalWorkers;
        
        // 1. Farmers
        int farmers = 0;
        if (remaining > 0) {
            int toAdd = Math.min(MIN_FARMERS, remaining);
            farmers += toAdd;
            remaining -= toAdd;
        }

        // 2. Foragers
        int foragers = 0;
        if (remaining > 0) {
            int toAdd = Math.min(MIN_FORAGERS, remaining);
            foragers += toAdd;
            remaining -= toAdd;
        }

        // 3. Nurses 
        int nurses = 0;
        if (remaining > 0) {
            int toAdd = Math.min(MIN_NURSES, remaining);
            nurses += toAdd;
            remaining -= toAdd;
        }

        ColonyStatsService stats = colony.getStatsService();

        // 4. Gravers
        int gravers = 0;
        if (remaining > 0) {
            int deadBodies = colony.getDeadAnts().size();
            if (deadBodies > 0) {
                float dailyCleaningRate = stats.getGravingRate(colony) * 24.0f;
                if (dailyCleaningRate > 0) {
                    float daysToClear = 5.0f;

                    if (deadBodies >= 400) {
                        daysToClear = 1.0f; 
                    }
                    if (deadBodies >= 1400) {
                        daysToClear = 0.5f;
                    }

                    int needed = (int) Math.ceil(deadBodies / (dailyCleaningRate * daysToClear)); 
                    gravers = Math.min(needed, remaining);
                    remaining -= gravers;
                }
            }
        }
        targets.put(GameConstants.ROLE_GRAVER, gravers);

        // 5. Extra Nurses
        if (remaining > 0) {
            int totalBrood = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
            float nursingRate = stats.getNursingRate(colony);
            if (nursingRate > 0) {
                int currentCapacity = (int) (nurses * nursingRate);
                if (currentCapacity < totalBrood) {
                    int deficit = totalBrood - currentCapacity;
                    int extraNeeded = (int) Math.ceil(deficit / nursingRate);
                    int toAdd = Math.min(extraNeeded, remaining);
                    nurses += toAdd;
                    remaining -= toAdd;
                }
            }
        }
        targets.put(GameConstants.ROLE_NURSE, nurses);

        // 6. Extra Farmers
        if (remaining > 0) {
            int totalConsumption = stats.getTotalConsumption(colony);
            float conversionRate = stats.getConversionRate(colony);
            double productionPerFarmer = conversionRate * 1440.0;
            
            if (productionPerFarmer > 0) {
                int currentProduction = (int) (farmers * productionPerFarmer);
                boolean needsFood = currentProduction < totalConsumption || colony.getMushrooms() < stats.getMushroomsCapacity(colony) * 0.2;
                
                if (needsFood) {
                    int deficit = totalConsumption - currentProduction;
                    int extraNeeded = (int) Math.ceil(deficit / productionPerFarmer);
                    if (colony.getMushrooms() < stats.getMushroomsCapacity(colony) * 0.1) extraNeeded++; 
                    
                    int toAdd = Math.min(extraNeeded, remaining);
                    farmers += toAdd;
                    remaining -= toAdd;
                }
            }
        }
        targets.put(GameConstants.ROLE_FARMER, farmers);

        // 7. Ranchers
        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
             int toAdd = Math.min(2, remaining);
             targets.put(GameConstants.ROLE_RANCHER, toAdd);
             remaining -= toAdd;
        }

        // 8. Scouts
        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) {
            int scoutTarget = farmers; 
            int toAdd = Math.min(scoutTarget, remaining);
            targets.put(GameConstants.ROLE_SCOUT, toAdd);
            remaining -= toAdd;
        }

        // 9. Builders
        if (remaining > 0 && colony.getCurrentBuildingProject() != null) {
            boolean secureResources = colony.getPlants() > 100 && colony.getWater() > 100;
            if (secureResources) {
                int builderTarget = (int) (totalWorkers * 0.3);
                int toAdd = Math.min(remaining, builderTarget); 
                targets.put(GameConstants.ROLE_BUILDER, toAdd);
                remaining -= toAdd;
            }
        }

        // 10. Extra Foragers
        if (remaining > 0) {
            foragers += remaining;
        }
        targets.put(GameConstants.ROLE_FORAGER, foragers);
    }

    private void calculateSoldierQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalSoldiers = colony.getSoldiers().size();
        if (totalSoldiers == 0) return;

        int remainingSoldiers = totalSoldiers;
        ColonyStatsService stats = colony.getStatsService();

        // 1. Police
        int assignedPolice = 0;
        if (colony.getParasites() > 0 && colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            int maxPolice = (int) (totalSoldiers * 0.20);
            assignedPolice = Math.min(maxPolice, remainingSoldiers);
            
            if (assignedPolice == 0 && maxPolice > 0 && remainingSoldiers > 0) assignedPolice = 1;
        }
        targets.put(GameConstants.ROLE_POLICE, assignedPolice);

        // 2. Hunters
        remainingSoldiers -= assignedPolice;

        boolean proteinNeeded = colony.getProtein() < stats.getProteinCapacity(colony);
        
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
                // 1. Researchers
                targets.put(GameConstants.ROLE_RESEARCHER, 1);
            } else {
                int half = totalQueens / 2;
                int researchers = half;
                int layers = totalQueens - half;
                
                targets.put(GameConstants.ROLE_RESEARCHER, researchers);
                // 2. Layers
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