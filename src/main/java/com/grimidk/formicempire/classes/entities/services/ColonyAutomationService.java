package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

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

        int remainingWorkforce = totalWorkers;
        ColonyStatsService stats = colony.getStatsService();

        int assignedNurses = MIN_NURSES;
        int totalBrood = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
        float nursingRate = stats.getNursingRate(colony);
        
        if (nursingRate > 0) {
            int currentCapacity = (int) (assignedNurses * nursingRate);
            if (currentCapacity < totalBrood) {
                int deficit = totalBrood - currentCapacity;
                int extraNursesNeeded = (int) Math.ceil(deficit / nursingRate);
                assignedNurses += extraNursesNeeded;
            }
        }
        assignedNurses = Math.min(assignedNurses, remainingWorkforce);
        targets.put(GameConstants.ROLE_NURSE, assignedNurses);
        remainingWorkforce -= assignedNurses;
        if (remainingWorkforce <= 0) return;

        int assignedGravers = 0;
        int deadBodies = colony.getDeadAnts().size();
        if (deadBodies > 0) {
            float dailyCleaningRate = stats.getGravingRate(colony) * 24.0f; 
            if (dailyCleaningRate > 0) {
                float totalCleaningNeeded = deadBodies; 
                float targetDays = 10.0f;
                int needed = (int) Math.ceil(totalCleaningNeeded / (dailyCleaningRate * targetDays));
                assignedGravers = Math.min(needed, remainingWorkforce);
            }
        }
        targets.put(GameConstants.ROLE_GRAVER, assignedGravers);
        remainingWorkforce -= assignedGravers;
        if (remainingWorkforce <= 0) return;

        int assignedForagers = Math.min(MIN_FORAGERS, remainingWorkforce);
        remainingWorkforce -= assignedForagers;
        
        int assignedFarmers = 0;
        if (remainingWorkforce > 0) {
            assignedFarmers = Math.min(MIN_FARMERS, remainingWorkforce);
            remainingWorkforce -= assignedFarmers;
        }

        if (remainingWorkforce > 0) {
            int totalConsumption = stats.getTotalConsumption(colony);
            float conversionRate = stats.getConversionRate(colony); 
            double productionPerFarmer = conversionRate * 1440.0; 

            if (productionPerFarmer > 0) {
                int currentProduction = (int) (assignedFarmers * productionPerFarmer);
                boolean needsFood = currentProduction < totalConsumption || colony.getMushrooms() < stats.getMushroomsCapacity(colony) * 0.1;
                
                if (needsFood) {
                    int deficit = totalConsumption - currentProduction;
                    int extraFarmersNeeded = (int) Math.ceil(deficit / productionPerFarmer);
                    if (colony.getMushrooms() < stats.getMushroomsCapacity(colony) * 0.2) {
                        extraFarmersNeeded += 1;
                    }
                    int extraAssigned = Math.min(extraFarmersNeeded, remainingWorkforce);
                    assignedFarmers += extraAssigned;
                    remainingWorkforce -= extraAssigned;
                }
            }
        }
        targets.put(GameConstants.ROLE_FARMER, assignedFarmers);

        if (remainingWorkforce > 0) {
            double plantDemand = assignedFarmers * stats.getConversionRate(colony) * 1440.0;
            float collectingRate = stats.getCollectingRate(colony);
            double productionPerForager = collectingRate * 24.0 * 0.5;

            if (productionPerForager > 0) {
                int currentPlantProd = (int) (assignedForagers * productionPerForager);
                boolean needsPlants = currentPlantProd < plantDemand || colony.getPlants() < stats.getPlantsCapacity(colony) * 0.1;

                if (needsPlants) {
                    double deficit = plantDemand - currentPlantProd;
                    int extraForagersNeeded = (int) Math.ceil(deficit / productionPerForager);
                     if (colony.getPlants() < stats.getPlantsCapacity(colony) * 0.2) {
                        extraForagersNeeded += 2;
                    }
                    int extraAssigned = Math.min(extraForagersNeeded, remainingWorkforce);
                    assignedForagers += extraAssigned;
                    remainingWorkforce -= extraAssigned;
                }
            }
        }
        targets.put(GameConstants.ROLE_FORAGER, assignedForagers);
        if (remainingWorkforce <= 0) return;

        if (colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
            int syrupCapacity = stats.getSyrupsCapacity(colony);
            int currentSyrup = colony.getSyrups();
            int syrupDeficit = syrupCapacity - currentSyrup;

            if (syrupDeficit > 0) {
                double dailySyrupGoal = syrupDeficit / 10.0;
                
                int aphidCapacityPerRancher = stats.getAphidCapacity(colony);
                if (aphidCapacityPerRancher > 0) {
                     int neededAphids = (int) Math.ceil(dailySyrupGoal);
                     int neededRanchers = (int) Math.ceil((double)neededAphids / aphidCapacityPerRancher);
                     
                     int assignedRanchers = Math.min(neededRanchers, remainingWorkforce);
                     targets.put(GameConstants.ROLE_RANCHER, assignedRanchers);
                     remainingWorkforce -= assignedRanchers;
                }
            }
        }
        if (remainingWorkforce <= 0) return;

        if (colony.getCurrentBuildingProject() != null) {
            boolean plantsFull = colony.getPlants() >= stats.getPlantsCapacity(colony);
            boolean waterFull = colony.getWater() >= stats.getWaterCapacity(colony);

            if (plantsFull && waterFull) {
                int maxBuilders = totalWorkers / 2;
                int assignedBuilders = Math.min(maxBuilders, remainingWorkforce);
                targets.put(GameConstants.ROLE_BUILDER, assignedBuilders);
                remainingWorkforce -= assignedBuilders;
            }
        }

        if (remainingWorkforce > 0) {
            targets.put(GameConstants.ROLE_FORAGER, targets.get(GameConstants.ROLE_FORAGER) + remainingWorkforce);
        }
    }

    private void calculateSoldierQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalSoldiers = colony.getSoldiers().size();
        if (totalSoldiers == 0) return;

        int remainingSoldiers = totalSoldiers;
        ColonyStatsService stats = colony.getStatsService();

        int assignedPolice = 0;
        if (colony.getParasites() > 0 && colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            int maxPolice = (int) (totalSoldiers * 0.20);
            assignedPolice = Math.min(maxPolice, remainingSoldiers);
            
            if (assignedPolice == 0 && maxPolice > 0 && remainingSoldiers > 0) assignedPolice = 1;
        }
        targets.put(GameConstants.ROLE_POLICE, assignedPolice);
        remainingSoldiers -= assignedPolice;

        boolean proteinNeeded = colony.getProtein() < stats.getProteinCapacity(colony);
        
        if (proteinNeeded && colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            targets.put(GameConstants.ROLE_HUNTER, remainingSoldiers);
        }
    }

    private void calculatePrincessQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalPrincesses = colony.getPrincesses().size();
        if (totalPrincesses == 0) return;
        
        int assistantCount = (int) (totalPrincesses * 0.10);
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

        if (totalAnts >= waterCapacity && colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) {
            if (totalQueens == 1) {
                targets.put(GameConstants.ROLE_RESEARCHER, 1);
            } else {
                int half = totalQueens / 2;
                int researchers = half;
                int layers = totalQueens - half;
                
                targets.put(GameConstants.ROLE_RESEARCHER, researchers);
                targets.put(GameConstants.ROLE_LAYER, layers);
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