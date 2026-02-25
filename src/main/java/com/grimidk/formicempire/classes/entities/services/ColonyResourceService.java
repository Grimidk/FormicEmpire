package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

public class ColonyResourceService {

    public synchronized double addResource(Colony colony, ResourceType type, double amount) {
        if (amount <= 0) return 0;
        
        ColonyStatsService stats = colony.getStatsService();
        double current = 0;
        double capacity = 0;

        if (type == GameConstants.RESOURCE_PLANT) { current = colony.getPlantsPrecise(); capacity = stats.getPlantsCapacity(colony); }
        else if (type == GameConstants.RESOURCE_WATER) { current = colony.getWaterPrecise(); capacity = stats.getWaterCapacity(colony); }
        else if (type == GameConstants.RESOURCE_MEAT) { current = colony.getProteinPrecise(); capacity = stats.getProteinCapacity(colony); }
        else if (type == GameConstants.RESOURCE_ROCK) { current = colony.getMineralsPrecise(); capacity = stats.getMineralsCapacity(colony); }
        else if (type == GameConstants.RESOURCE_FUNGI) { current = colony.getMushroomsPrecise(); capacity = stats.getMushroomsCapacity(colony); }
        else if (type == GameConstants.RESOURCE_SYRUP) { current = colony.getSyrupsPrecise(); capacity = stats.getSyrupsCapacity(colony); }
        else if (type == GameConstants.RESOURCE_RESIN) { current = colony.getResinsPrecise(); capacity = stats.getResinsCapacity(colony); }

        double space = capacity - current;
        double actualAdd = Math.min(amount, space);
        if (actualAdd <= 0) return 0;

        double newValue = current + actualAdd;

        if (type == GameConstants.RESOURCE_PLANT) colony.setPlants(newValue);
        else if (type == GameConstants.RESOURCE_WATER) colony.setWater(newValue);
        else if (type == GameConstants.RESOURCE_MEAT) colony.setProtein(newValue);
        else if (type == GameConstants.RESOURCE_ROCK) colony.setMinerals(newValue);
        else if (type == GameConstants.RESOURCE_FUNGI) colony.setMushrooms(newValue);
        else if (type == GameConstants.RESOURCE_SYRUP) colony.setSyrups(newValue);
        else if (type == GameConstants.RESOURCE_RESIN) colony.setResins(newValue);

        return actualAdd;
    }

    public synchronized double consumeResource(Colony colony, ResourceType type, double amount) {
        if (amount <= 0) return 0;
        double current = 0;

        if (type == GameConstants.RESOURCE_PLANT) current = colony.getPlantsPrecise();
        else if (type == GameConstants.RESOURCE_WATER) current = colony.getWaterPrecise();
        else if (type == GameConstants.RESOURCE_MEAT) current = colony.getProteinPrecise();
        else if (type == GameConstants.RESOURCE_ROCK) current = colony.getMineralsPrecise();
        else if (type == GameConstants.RESOURCE_FUNGI) current = colony.getMushroomsPrecise();
        else if (type == GameConstants.RESOURCE_SYRUP) current = colony.getSyrupsPrecise();
        else if (type == GameConstants.RESOURCE_RESIN) current = colony.getResinsPrecise();

        double actualConsume = Math.min(amount, current);
        if (actualConsume <= 0) return 0;

        double newValue = current - actualConsume;

        if (type == GameConstants.RESOURCE_PLANT) colony.setPlants(newValue);
        else if (type == GameConstants.RESOURCE_WATER) colony.setWater(newValue);
        else if (type == GameConstants.RESOURCE_MEAT) colony.setProtein(newValue);
        else if (type == GameConstants.RESOURCE_ROCK) colony.setMinerals(newValue);
        else if (type == GameConstants.RESOURCE_FUNGI) colony.setMushrooms(newValue);
        else if (type == GameConstants.RESOURCE_SYRUP) colony.setSyrups(newValue);
        else if (type == GameConstants.RESOURCE_RESIN) colony.setResins(newValue);

        return actualConsume;
    }

    public synchronized boolean hasCapacity(Colony colony, ResourceType type) {
        ColonyStatsService stats = colony.getStatsService();
        if (type == GameConstants.RESOURCE_PLANT) return colony.getPlantsPrecise() < stats.getPlantsCapacity(colony);
        if (type == GameConstants.RESOURCE_WATER) return colony.getWaterPrecise() < stats.getWaterCapacity(colony);
        if (type == GameConstants.RESOURCE_MEAT) return colony.getProteinPrecise() < stats.getProteinCapacity(colony);
        if (type == GameConstants.RESOURCE_ROCK) return colony.getMineralsPrecise() < stats.getMineralsCapacity(colony);
        if (type == GameConstants.RESOURCE_FUNGI) return colony.getMushroomsPrecise() < stats.getMushroomsCapacity(colony);
        if (type == GameConstants.RESOURCE_SYRUP) return colony.getSyrupsPrecise() < stats.getSyrupsCapacity(colony);
        if (type == GameConstants.RESOURCE_RESIN) return colony.getResinsPrecise() < stats.getResinsCapacity(colony);
        return false;
    }
}