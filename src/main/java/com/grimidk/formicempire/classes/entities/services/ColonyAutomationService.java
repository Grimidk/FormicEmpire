package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import java.util.HashMap;
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

        int totalWorkers = colony.getWorkers().size();
        if (totalWorkers == 0) return targets;

        int assignedNurses = 0;
        int assignedForagers = 0;
        int assignedFarmers = 0;
        int remainingWorkforce = totalWorkers;

        int minimumNeeded = MIN_NURSES + MIN_FORAGERS + MIN_FARMERS;

        if (totalWorkers < minimumNeeded) {

            int toAdd = Math.min(MIN_NURSES, remainingWorkforce);
            assignedNurses += toAdd;
            remainingWorkforce -= toAdd;

            if (remainingWorkforce > 0) {
                toAdd = Math.min(MIN_FORAGERS, remainingWorkforce);
                assignedForagers += toAdd;
                remainingWorkforce -= toAdd;
            }

            if (remainingWorkforce > 0) {
                toAdd = Math.min(MIN_FARMERS, remainingWorkforce);
                assignedFarmers += toAdd;
                remainingWorkforce -= toAdd;
            }

            targets.put(GameConstants.ROLE_NURSE, assignedNurses);
            targets.put(GameConstants.ROLE_FORAGER, assignedForagers);
            targets.put(GameConstants.ROLE_FARMER, assignedFarmers);
            return targets;

        } else {
            assignedNurses = MIN_NURSES;
            assignedForagers = MIN_FORAGERS;
            assignedFarmers = MIN_FARMERS;
            remainingWorkforce -= minimumNeeded;
        }

        ColonyStatsService stats = colony.getStatsService();

        int totalBrood = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
        float nursingRate = stats.getNursingRate(colony);
        
        if (nursingRate > 0) {
            int currentCapacity = (int) (assignedNurses * nursingRate);
            if (currentCapacity < totalBrood) {
                int deficit = totalBrood - currentCapacity;
                int extraNursesNeeded = (int) Math.ceil(deficit / nursingRate);
                
                int extraAssigned = Math.min(extraNursesNeeded, remainingWorkforce);
                
                assignedNurses += extraAssigned;
                remainingWorkforce -= extraAssigned;
            }
        }

        if (remainingWorkforce <= 0) {
            updateTargets(targets, assignedNurses, assignedForagers, assignedFarmers);
            return targets;
        }

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

        if (remainingWorkforce <= 0) {
            updateTargets(targets, assignedNurses, assignedForagers, assignedFarmers);
            return targets;
        }

        double plantDemand = assignedFarmers * conversionRate * 1440.0;
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

        if (remainingWorkforce > 0) {
            
            if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER) && colony.getProtein() < stats.getProteinCapacity(colony) * 0.5) {
                int hunters = Math.min(remainingWorkforce, 2);
                targets.put(GameConstants.ROLE_HUNTER, hunters);
                remainingWorkforce -= hunters;
            }

            if (remainingWorkforce > 0 && colony.getParasites() > 0 && colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
                int police = Math.min(remainingWorkforce, 3);
                targets.put(GameConstants.ROLE_POLICE, police);
                remainingWorkforce -= police;
            }
            
            if (remainingWorkforce > 0 && colony.getDeadAnts().size() > 5) {
                int gravers = Math.min(remainingWorkforce, 1);
                targets.put(GameConstants.ROLE_GRAVER, gravers);
                remainingWorkforce -= gravers;
            }

            if (remainingWorkforce > 0) {
                if (colony.getCurrentBuildingProject() != null) {
                    targets.put(GameConstants.ROLE_BUILDER, remainingWorkforce);
                } else if (colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER) && colony.getResearchPoints() < 2000) {
                    targets.put(GameConstants.ROLE_RESEARCHER, remainingWorkforce);
                } else {
                    assignedForagers += remainingWorkforce;
                }
            }
        }

        updateTargets(targets, assignedNurses, assignedForagers, assignedFarmers);
        return targets;
    }

    private void updateTargets(Map<AntRole, Integer> targets, int nurses, int foragers, int farmers) {
        targets.put(GameConstants.ROLE_NURSE, nurses);
        targets.put(GameConstants.ROLE_FORAGER, foragers);
        targets.put(GameConstants.ROLE_FARMER, farmers);
    }

    private void applyQuotas(Colony colony, Map<AntRole, Integer> quotas) {
        for (Map.Entry<AntRole, Integer> entry : quotas.entrySet()) {
            colony.setAssignedRoleCount(entry.getKey(), entry.getValue());
        }
    }
}