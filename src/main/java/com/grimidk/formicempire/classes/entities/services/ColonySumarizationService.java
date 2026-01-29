package com.grimidk.formicempire.classes.entities.services;

import java.util.List;
import java.util.Random;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

public class ColonySumarizationService {

    private final Random random = new Random();

    public void runHourlyLite(Colony colony) {
        simulateProduction(colony);
        simulateResearch(colony);
        simulateBuilding(colony); 
        simulateLaying(colony);
    }

    public void runDailyLite(Colony colony) {
        colony.rankUp();
        simulateNursing(colony);
        simulateEating(colony);
        colony.getPopulationService().runHatching(colony);
        colony.getPopulationService().runAging(colony);
        simulateGraveKeeping(colony);
        
        if (colony.isPlayer()) {
            colony.getPopulationService().runContamination(colony);
        }
    }

    private void simulateProduction(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService loc = colony.getLocationService();
        
        int plantGain = probabilisticRound(stats.getPlantProductionHourly(colony));
        int waterGain = probabilisticRound(stats.getWaterProductionHourly(colony));
        int meatGain = probabilisticRound(stats.getProteinProductionHourly(colony));
        int rockGain = probabilisticRound(stats.getMineralProductionHourly(colony));

        if (loc.getTotalQuantityAvailable(GameConstants.RESOURCE_PLANT) <= 0) plantGain = 0;

        if (loc.getTotalQuantityAvailable(GameConstants.RESOURCE_WATER) <= 0) {
            double passiveOnly = 0; 
             if (colony.hasBuilding(GameUnlocks.PASSIVE_WATER)) {
                double dailyPct = colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1) ? 0.20 : 0.10;
                passiveOnly = (stats.getWaterCapacity(colony) * dailyPct) / 24.0;
            }
            waterGain = probabilisticRound(passiveOnly);
        }
        if (loc.getTotalQuantityAvailable(GameConstants.RESOURCE_MEAT) <= 0) meatGain = 0;
        if (loc.getTotalQuantityAvailable(GameConstants.RESOURCE_ROCK) <= 0) rockGain = 0;

        addResource(colony, colony.getPlants(), plantGain, stats.getPlantsCapacity(colony), GameConstants.RESOURCE_PLANT);
        addResource(colony, colony.getWater(), waterGain, stats.getWaterCapacity(colony), GameConstants.RESOURCE_WATER);
        addResource(colony, colony.getProtein(), meatGain, stats.getProteinCapacity(colony), GameConstants.RESOURCE_MEAT);
        addResource(colony, colony.getMinerals(), rockGain, stats.getMineralsCapacity(colony), GameConstants.RESOURCE_ROCK);

        if (colony.getMushrooms() < stats.getMushroomsCapacity(colony)) {
            int farmerCount = stats.getEffectiveFarmerCount(colony);
            float convertRate = stats.getConversionRate(colony);
            
            int maxConvert = probabilisticRound(farmerCount * convertRate * 60);
            
            if (maxConvert > 0) {
                int actualConverted = 0;

                int plantConvert = Math.min(maxConvert, colony.getPlants());
                if (plantConvert > 0) {
                    colony.setPlants(colony.getPlants() - plantConvert);
                    actualConverted += plantConvert;
                    maxConvert -= plantConvert;
                }

                if (maxConvert > 0) {
                    int meatConvert = Math.min(maxConvert, colony.getProtein());
                    if (meatConvert > 0) {
                        colony.setProtein(colony.getProtein() - meatConvert);
                        actualConverted += (meatConvert * 2); 
                    }
                }
                
                if (actualConverted > 0) {
                    addResource(colony, colony.getMushrooms(), actualConverted, stats.getMushroomsCapacity(colony), GameConstants.RESOURCE_FUNGI);
                }
            }
        }
    }

    private int probabilisticRound(double value) {
        int floor = (int) value;
        return (random.nextDouble() < (value - floor)) ? floor + 1 : floor;
    }

    private void addResource(Colony colony, int current, int gain, int max, ResourceType type) {
        if (gain <= 0) return;
        int newValue = Math.min(current + gain, max);
        
        if (type == GameConstants.RESOURCE_PLANT) colony.setPlants(newValue);
        else if (type == GameConstants.RESOURCE_WATER) colony.setWater(newValue);
        else if (type == GameConstants.RESOURCE_MEAT) colony.setProtein(newValue);
        else if (type == GameConstants.RESOURCE_ROCK) colony.setMinerals(newValue);
        else if (type == GameConstants.RESOURCE_FUNGI) colony.setMushrooms(newValue);
    }

    private void simulateLaying(Colony colony) {
        int layerCount = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
        if (layerCount <= 0) return;

        int spaceAvailable = colony.getStatsService().getEggsCapacity(colony) - colony.getEggs().size();
        if (spaceAvailable <= 0) return;

        int toLay = probabilisticRound(layerCount * colony.getStatsService().getLayingRate(colony));
        toLay = Math.min(toLay, spaceAvailable);
        
        List<Ant> eggs = colony.getEggs();
        for (int i = 0; i < toLay; i++) {
            Ant newEgg = new Ant(colony, GameConstants.TYPE_EGG);
            newEgg.setDimension(WorldSpaces.UNDERWORLD);
            newEgg.setPosition(new java.awt.Point(0, 0));
            eggs.add(newEgg);
        }
    }

    private void simulateEating(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        int totalConsumption = stats.getTotalConsumption(colony);
        totalConsumption += colony.getParasites();

        int mushrooms = colony.getMushrooms();
        
        if (mushrooms >= totalConsumption) {
            colony.setMushrooms(mushrooms - totalConsumption);
        } else {
            colony.setMushrooms(0);
            handleStarvation(colony, totalConsumption - mushrooms, "Starvation");
        }
        
        int waterDemand = stats.getWaterConsumption(colony);
        int water = colony.getWater();
        
        if (water >= waterDemand) {
            colony.setWater(water - waterDemand);
        } else {
            colony.setWater(0);
            handleStarvation(colony, (waterDemand - water) / 2, "Dehydration");
        }
    }

    private void handleStarvation(Colony colony, int deficit, String cause) {
        int deaths = deficit / 5; 
        
        if (deaths <= 0 && deficit > 0 && random.nextFloat() < 0.2) deaths = 1; 

        if (deaths <= 0) return;

        List<Ant> workers = colony.getWorkers();
        int killed = 0;
        
        for (int i = workers.size() - 1; i >= 0; i--) {
            if (killed >= deaths) break;
            Ant victim = workers.remove(i);
            colony.recordAntDeath(victim, cause);
            killed++;
        }
        
        if (killed > 5) {
            colony.logEvent("Background Simulation: " + killed + " ants died of " + cause + ".");
        }
    }

    private void simulateNursing(Colony colony) {
        int nurseCount = colony.getAssignedRoleCount(GameConstants.ROLE_NURSE);
        if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) nurseCount += 2;
        else if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) nurseCount += 1; 

        float nursingRate = colony.getStatsService().getNursingRate(colony);
        int capacity = (int) (nurseCount * nursingRate);
        
        int totalBrood = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
        
        if (totalBrood > capacity) {
            int toCull = totalBrood - capacity;
            
            int killedEggs = performCulling(colony, colony.getEggs(), toCull);
            toCull -= killedEggs;
            
            if (toCull > 0) {
                int killedLarvae = performCulling(colony, colony.getLarvae(), toCull);
                toCull -= killedLarvae;
            }
            
            if (toCull > 0) {
                performCulling(colony, colony.getPupae(), toCull);
            }
        }
    }

    private int performCulling(Colony colony, List<Ant> broodList, int amount) {
        int removed = 0;
        for (int i = broodList.size() - 1; i >= 0; i--) {
            if (removed >= amount) break;
            Ant victim = broodList.remove(i);
            colony.recordAntDeath(victim, "Lack of Care");
            removed++;
        }
        return removed;
    }
    
    private void simulateGraveKeeping(Colony colony) {
        int graverCount = colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
        
        if (colony.hasBuilding(GameUnlocks.PASSIVE_GRAVE)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) graverCount += 2;
            else graverCount += 1;
        }

        if (graverCount <= 0) return;
        
        float gravingRate = colony.getStatsService().getGravingRate(colony);
        int cleanCapacity = (int) (graverCount * gravingRate);
        
        if (cleanCapacity <= 0) return;

        List<Ant> deadAnts = colony.getDeadAnts();
        int removed = 0;
        
        for (int i = deadAnts.size() - 1; i >= 0; i--) {
            if (removed >= cleanCapacity) break;
            deadAnts.remove(i);
            removed++;
        }
    }

    private void simulateResearch(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;
        int researcherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER);
        
        if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) researcherCount += 2;
            else researcherCount += 1;
        }

        int speed = colony.getStatsService().getResearchSpeed(colony);
        colony.setResearchPoints(colony.getResearchPoints() + (researcherCount * speed));
    }

    private void simulateBuilding(Colony colony) {
        if (colony.getCurrentBuildingProject() == null) return;
        int builderCount = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
        if (builderCount <= 0) return;
        
        double efficiency = builderCount / 100.0;
        colony.setBuildingProgressHours(colony.getBuildingProgressHours() + 1.0);
        
        double required = colony.getCurrentBuildingProject().getBuildTime() / efficiency;
        if (colony.getBuildingProgressHours() >= required) {
            colony.unlockBuilding(colony.getCurrentBuildingProject());
            colony.setCurrentBuildingProject(null);
            colony.setBuildingProgressHours(0.0);
        }
    }
}