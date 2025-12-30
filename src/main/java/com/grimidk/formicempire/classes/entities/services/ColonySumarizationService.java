package com.grimidk.formicempire.classes.entities.services;

import java.util.List;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

public class ColonySumarizationService {

    public void runHourlyLite(Colony colony) {
        simulateProduction(colony);
        simulateResearch(colony);
        simulateBuilding(colony); 
        simulateLaying(colony);
    }

    public void runDailyLite(Colony colony) {
        colony.rankUp();
        simulateEating(colony);
        colony.getPopulationService().runHatching(colony);
        colony.getPopulationService().runAging(colony);
        simulateNursing(colony);
        
        if (colony.isPlayer()) {
            colony.getPopulationService().runContamination(colony);
        }
    }

    // --- Production Simulation ---
    private void simulateProduction(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        
        int foragerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
        int hunterCount = colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER);
        int minerCount = colony.getAssignedRoleCount(GameConstants.ROLE_MINER);
        int farmerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);
        float collectionRate = stats.getCollectingRate(colony);        
        int plantGain = (int) (foragerCount * 0.5 * collectionRate);
        int waterGain = (int) (foragerCount * 0.5 * collectionRate);
        int meatGain = (int) (hunterCount * collectionRate);
        int rockGain = (int) (minerCount * collectionRate);

        if (colony.isPlayer()) {
            ColonyLocationService loc = colony.getLocationService();
            if (loc.getTotalQuantityAvailable(GameConstants.PLANT_RESOURCE) <= 0) plantGain = 0;
            if (loc.getTotalQuantityAvailable(GameConstants.WATER_RESOURCE) <= 0) waterGain = 0;
            if (loc.getTotalQuantityAvailable(GameConstants.MEAT_RESOURCE) <= 0) meatGain = 0;
            if (loc.getTotalQuantityAvailable(GameConstants.ROCK_RESOURCE) <= 0) rockGain = 0;
        }

        addResource(colony, colony.getPlants(), plantGain, stats.getPlantsCapacity(colony), GameConstants.PLANT_RESOURCE);
        addResource(colony, colony.getWater(), waterGain, stats.getWaterCapacity(colony), GameConstants.WATER_RESOURCE);
        addResource(colony, colony.getProtein(), meatGain, stats.getProteinCapacity(colony), GameConstants.MEAT_RESOURCE);
        addResource(colony, colony.getMinerals(), rockGain, stats.getMineralsCapacity(colony), GameConstants.ROCK_RESOURCE);

        if (colony.getMushrooms() < stats.getMushroomsCapacity(colony)) {
            float convertRate = stats.getConversionRate(colony);
            int maxConvert = (int) (farmerCount * convertRate * 10); 
            if (maxConvert > 0 && colony.getPlants() >= maxConvert) {
                colony.setPlants(colony.getPlants() - maxConvert);
                addResource(colony, colony.getMushrooms(), maxConvert, stats.getMushroomsCapacity(colony), GameConstants.FUNGI_RESOURCE);
            }
        }
    }

    private void addResource(Colony colony, int current, int gain, int max, ResourceType type) {
        if (gain <= 0) return;
        int newValue = Math.min(current + gain, max);
        
        if (type == GameConstants.PLANT_RESOURCE) colony.setPlants(newValue);
        else if (type == GameConstants.WATER_RESOURCE) colony.setWater(newValue);
        else if (type == GameConstants.MEAT_RESOURCE) colony.setProtein(newValue);
        else if (type == GameConstants.ROCK_RESOURCE) colony.setMinerals(newValue);
        else if (type == GameConstants.FUNGI_RESOURCE) colony.setMushrooms(newValue);
    }


    // --- Life Cycle Simulation ---
    private void simulateLaying(Colony colony) {
        int layerCount = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
        if (layerCount <= 0) return;

        int spaceAvailable = colony.getStatsService().getEggsCapacity(colony) - colony.getEggs().size();
        if (spaceAvailable <= 0) return;

        int toLay = Math.min(layerCount * (int) colony.getStatsService().getLayingRate(colony), spaceAvailable);
        
        List<Ant> eggs = colony.getEggs();
        for (int i = 0; i < toLay; i++) {
            Ant newEgg = new Ant(colony, GameConstants.TYPE_EGG);
            newEgg.setDimension(WorldSpaces.UNDERWORLD);
            newEgg.setPosition(new java.awt.Point(0, 0));
            eggs.add(newEgg);
        }
    }

    private void simulateEating(Colony colony) {
        int totalConsumption = colony.getStatsService().getTotalConsumption(colony);
        int mushrooms = colony.getMushrooms();
        
        if (mushrooms >= totalConsumption) {
            colony.setMushrooms(mushrooms - totalConsumption);
        } else {
            colony.setMushrooms(0);
            handleStarvation(colony, totalConsumption - mushrooms);
        }
        
        int totalAnts = colony.getAntTotal();
        int water = colony.getWater();
        int waterDemand = totalAnts; 
        
        if (water >= waterDemand) {
            colony.setWater(water - waterDemand);
        } else {
            colony.setWater(0);
        }
    }

    private void handleStarvation(Colony colony, int deficit) {
        int deaths = deficit / 10; 
        if (deaths <= 0) return;

        List<Ant> workers = colony.getWorkers();
        int killed = 0;
        
        for (int i = workers.size() - 1; i >= 0; i--) {
            if (killed >= deaths) break;
            workers.remove(i);
            colony.setTotalDeaths(colony.getTotalDeaths() + 1);
            killed++;
        }
        
        if (killed > 0) {
            colony.logEvent("Background Simulation: " + killed + " ants starved.");
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
            performCulling(colony, colony.getEggs(), toCull);
            toCull -= colony.getEggs().size(); 
            if (toCull > 0) performCulling(colony, colony.getLarvae(), toCull);
        }
    }

    private void performCulling(Colony colony, List<Ant> broodList, int amount) {
        int removed = 0;
        for (int i = broodList.size() - 1; i >= 0; i--) {
            if (removed >= amount) break;
            broodList.remove(i);
            colony.setTotalDeaths(colony.getTotalDeaths() + 1);
            removed++;
        }
    }

    // --- Misc Simulation ---
    private void simulateResearch(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;
        int researcherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER);
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