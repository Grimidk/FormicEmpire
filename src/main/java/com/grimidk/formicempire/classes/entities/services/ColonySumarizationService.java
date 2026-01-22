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
        
        if (colony.isPlayer()) {
            colony.getPopulationService().runContamination(colony);
        }
    }

    private void simulateProduction(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService loc = colony.getLocationService();
        
        int foragerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
        int hunterCount = colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER);
        int minerCount = colony.getAssignedRoleCount(GameConstants.ROLE_MINER);
        int farmerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);
        float collectionRate = stats.getCollectingRate(colony);        

        float plantSplit = 0.5f;
        float waterSplit = 0.5f;
        
        float convertRate = stats.getConversionRate(colony);
        int plantsNeededPerHour = (int)(farmerCount * convertRate * 60); 
        
        boolean lowPlants = colony.getPlants() < (plantsNeededPerHour * 24);
        boolean waterFull = colony.getWater() >= stats.getWaterCapacity(colony);
        boolean plantsFull = colony.getPlants() >= stats.getPlantsCapacity(colony);

        if ((lowPlants && !plantsFull) || (!plantsFull && waterFull)) {
            plantSplit = 1.0f;
            waterSplit = 0.0f;
        } else if (plantsFull && !waterFull) {
            plantSplit = 0.0f;
            waterSplit = 1.0f;
        }

        float rawPlantGain = foragerCount * plantSplit * collectionRate;
        int plantGain = (int) rawPlantGain;
        if (random.nextFloat() < (rawPlantGain - plantGain)) plantGain++;

        float rawWaterGain = foragerCount * waterSplit * collectionRate;
        int waterGain = (int) rawWaterGain;
        if (random.nextFloat() < (rawWaterGain - waterGain)) waterGain++;

        float rawMeatGain = hunterCount * collectionRate;
        int meatGain = (int) rawMeatGain;
        if (random.nextFloat() < (rawMeatGain - meatGain)) meatGain++;
        
        float rawRockGain = minerCount * collectionRate;
        int rockGain = (int) rawRockGain;
        if (random.nextFloat() < (rawRockGain - rockGain)) rockGain++;

        if (colony.isPlayer()) {
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
            float rawConvert = farmerCount * convertRate * 60;
            int maxConvert = (int) rawConvert;
            if (random.nextFloat() < (rawConvert - maxConvert)) maxConvert++;
            
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


    private void simulateLaying(Colony colony) {
        if (colony.getMushrooms() <= 0 && colony.getAntTotal() > 5) return;

        int layerCount = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
        if (layerCount <= 0) return;

        int spaceAvailable = colony.getStatsService().getEggsCapacity(colony) - colony.getEggs().size();
        if (spaceAvailable <= 0) return;

        float rawLay = layerCount * colony.getStatsService().getLayingRate(colony);
        int toLay = (int) rawLay;
        if (random.nextFloat() < (rawLay - toLay)) toLay++;
        
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
        int deaths = deficit / 20; 
        if (deaths <= 0 && deficit > 0 && random.nextFloat() < 0.1) deaths = 1; 

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