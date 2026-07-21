package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynastyStatService {

    public int getTotalPopulation(Dynasty dynasty) {
        if (dynasty == null) return 0;
        int total = 0;
        for (Colony c : dynasty.getColonies()) {
            total += c.getAntTotal();
        }
        return total;
    }

    public Map<AntType, Integer> getGlobalPopulationByType(Dynasty dynasty) {
        Map<AntType, Integer> totals = new HashMap<>();
        if (dynasty == null) return totals;

        for (Colony c : dynasty.getColonies()) {
            for (Map.Entry<AntType, List<Ant>> entry : c.getAntGroups().entrySet()) {
                totals.merge(entry.getKey(), entry.getValue().size(), Integer::sum);
            }
        }
        return totals;
    }

    public Map<ResourceType, Integer> getGlobalResources(Dynasty dynasty) {
        Map<ResourceType, Integer> resources = new HashMap<>();
        resources.put(GameConstants.RESOURCE_PLANT, 0);
        resources.put(GameConstants.RESOURCE_FUNGI, 0);
        resources.put(GameConstants.RESOURCE_MEAT, 0);
        resources.put(GameConstants.RESOURCE_WATER, 0);
        resources.put(GameConstants.RESOURCE_SYRUP, 0);
        resources.put(GameConstants.RESOURCE_RESIN, 0);
        resources.put(GameConstants.RESOURCE_ROCK, 0);

        if (dynasty == null) return resources;

        for (Colony c : dynasty.getColonies()) {
            resources.merge(GameConstants.RESOURCE_PLANT, (int) c.getPlants(), Integer::sum);
            resources.merge(GameConstants.RESOURCE_FUNGI, (int) c.getMushrooms(), Integer::sum);
            resources.merge(GameConstants.RESOURCE_MEAT, (int) c.getProtein(), Integer::sum);
            resources.merge(GameConstants.RESOURCE_WATER, (int) c.getWater(), Integer::sum);
            resources.merge(GameConstants.RESOURCE_SYRUP, (int) c.getSyrups(), Integer::sum);
            resources.merge(GameConstants.RESOURCE_RESIN, (int) c.getResins(), Integer::sum);
            resources.merge(GameConstants.RESOURCE_ROCK, (int) c.getMinerals(), Integer::sum);
        }
        return resources;
    }

    public int getGlobalResearchRateDaily(Dynasty dynasty) {
        if (dynasty == null) return 0;
        int totalDaily = 0;
        for (Colony c : dynasty.getColonies()) {
            totalDaily += c.getStatsService().getDailyResearchPoints(c);
        }
        return totalDaily;
    }
    
    public int getTotalColonies(Dynasty dynasty) {
        return dynasty != null ? dynasty.getColonies().size() : 0;
    }

    public int getTotalQueens(Dynasty dynasty) {
        if (dynasty == null) return 0;
        int total = 0;
        for (Colony c : dynasty.getColonies()) {
            total += c.getQueens().size();
        }
        return total;
    }

    public int getGlobalBirthRateDaily(Dynasty dynasty) {
        if (dynasty == null) return 0;
        int totalDaily = 0;
        for (Colony c : dynasty.getColonies()) {
            int layers = c.getAssignedRoleCount(GameConstants.ROLE_LAYER);
            totalDaily += (int) (layers * c.getStatsService().getLayingRate(c) * 24);
        }
        return totalDaily;
    }

    public int getMilitaryPower(Dynasty dynasty) {
        return dynasty != null ? dynasty.getMilitaryPower() : 0;
    }
}