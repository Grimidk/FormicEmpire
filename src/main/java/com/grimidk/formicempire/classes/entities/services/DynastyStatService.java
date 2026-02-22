package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

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

    public Map<String, Integer> getGlobalResources(Dynasty dynasty) {
        Map<String, Integer> resources = new HashMap<>();
        resources.put("Plants", 0);
        resources.put("Mushrooms", 0);
        resources.put("Protein", 0);
        resources.put("Water", 0);
        resources.put("Syrups", 0);
        resources.put("Resins", 0);
        resources.put("Minerals", 0);

        if (dynasty == null) return resources;

        for (Colony c : dynasty.getColonies()) {
            resources.merge("Plants", c.getPlants(), Integer::sum);
            resources.merge("Mushrooms", c.getMushrooms(), Integer::sum);
            resources.merge("Protein", c.getProtein(), Integer::sum);
            resources.merge("Water", c.getWater(), Integer::sum);
            resources.merge("Syrups", c.getSyrups(), Integer::sum);
            resources.merge("Resins", c.getResins(), Integer::sum);
            resources.merge("Minerals", c.getMinerals(), Integer::sum);
        }
        return resources;
    }

    public int getGlobalResearchRateDaily(Dynasty dynasty) {
        if (dynasty == null) return 0;
        int totalDaily = 0;

        for (Colony c : dynasty.getColonies()) {
            int researchers = c.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER);
            int assistants = c.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT);            
            int baseSpeed = c.getStatsService().getResearchSpeed(c);
            int hourlyQueen = researchers * baseSpeed;
            int hourlyAssistant = (int) (assistants * (baseSpeed / 5.0));
            
            totalDaily += (hourlyQueen + hourlyAssistant) * 24;
        }
        return totalDaily;
    }
    
    public int getTotalColonies(Dynasty dynasty) {
        return dynasty != null ? dynasty.getColonies().size() : 0;
    }
}