package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

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

    public int getCombatCapacity(Dynasty dynasty) {
        if (dynasty == null) {
            return GameNumbers.COMBAT_CAPACITY_BASE;
        }
        int capacity = hasAssignedCommanders(dynasty)
                ? GameNumbers.COMBAT_CAPACITY_WITH_COMMANDER
                : GameNumbers.COMBAT_CAPACITY_BASE;
        if (hasSwarmingAbility(dynasty)) {
            capacity = Math.round(capacity * GameNumbers.ASSIMILATED_SWARMING_COMBAT_CAPACITY_MULT);
        }
        return capacity;
    }

    public boolean hasSwarmingAbility(Dynasty dynasty) {
        return dynasty != null
                && (dynasty.hasUpgrade(GameUnlocks.ABILITY_SWARMING)
                        || dynasty.hasUpgrade(GameUnlocks.ASSIMILATED_SWARMING));
    }

    public boolean hasAssignedCommanders(Dynasty dynasty) {
        if (dynasty == null || dynasty.getColonies() == null) {
            return false;
        }
        for (Colony colony : dynasty.getColonies()) {
            if (colony != null && colony.getWarAssignedRoleCount(GameConstants.ROLE_COMMANDER) > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMultiQueenColony(Dynasty dynasty) {
        if (dynasty == null || dynasty.getColonies() == null) {
            return false;
        }
        int minQueens = GameNumbers.TRIGGER_COMMANDER_MIN_QUEENS_IN_COLONY;
        for (Colony colony : dynasty.getColonies()) {
            if (colony != null && colony.getQueens() != null && colony.getQueens().size() >= minQueens) {
                return true;
            }
        }
        return false;
    }

    public int getActiveMilitaryPower(Dynasty dynasty) {
        return dynasty != null ? dynasty.getActiveMilitaryPower() : 0;
    }

    public int getReserveMilitaryPower(Dynasty dynasty) {
        return dynasty != null ? dynasty.getReserveMilitaryPower() : 0;
    }

    public int getTotalAssignedCarriers(Dynasty dynasty) {
        if (dynasty == null || dynasty.getColonies() == null) {
            return 0;
        }
        boolean atWar = dynasty.isAtWar();
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            if (colony == null) {
                continue;
            }
            total += atWar
                    ? colony.getWarAssignedRoleCount(GameConstants.ROLE_CARRIER)
                    : colony.getAssignedRoleCount(GameConstants.ROLE_CARRIER);
        }
        return total;
    }

    public int getAssignedCarriers(Colony colony) {
        if (colony == null) {
            return 0;
        }
        Dynasty dynasty = colony.getDynasty();
        boolean atWar = dynasty != null && dynasty.isAtWar();
        return atWar
                ? colony.getWarAssignedRoleCount(GameConstants.ROLE_CARRIER)
                : colony.getAssignedRoleCount(GameConstants.ROLE_CARRIER);
    }

    public int getWarFrontCount(Dynasty dynasty, World world) {
        if (dynasty == null || world == null || !dynasty.isAtWar()) {
            return 1;
        }
        int count = 0;
        for (War war : world.getWarService().getActiveWars()) {
            if (war.involves(dynasty.getId())) {
                count++;
            }
        }
        return Math.max(1, count);
    }

    public int getCarrierSharePerFront(Dynasty dynasty, World world) {
        return fairShare(getTotalAssignedCarriers(dynasty), getWarFrontCount(dynasty, world));
    }

    public float getDailyReinforcementRate(Dynasty dynasty, World world) {
        return GameNumbers.warDailyReinforcementRate(getCarrierSharePerFront(dynasty, world));
    }

    public int getDailyReinforcementAllowancePerLine(Dynasty dynasty, World world) {
        return GameNumbers.warDailyReinforcementAllowancePerLine(
                getCombatCapacity(dynasty),
                getCarrierSharePerFront(dynasty, world));
    }

    private static int fairShare(int totalQuota, int frontCount) {
        int fronts = Math.max(1, frontCount);
        if (totalQuota <= 0) {
            return 0;
        }
        return (totalQuota + fronts - 1) / fronts;
    }
}