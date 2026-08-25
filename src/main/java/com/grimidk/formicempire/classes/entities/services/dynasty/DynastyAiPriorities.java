package com.grimidk.formicempire.classes.entities.services.dynasty;

import java.util.ArrayList;
import java.util.List;

import com.grimidk.formicempire.classes.constants.dynasty.AiPersonality;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

public final class DynastyAiPriorities {

    private DynastyAiPriorities() {
    }

    public static List<Upgrade> researchPriority(Dynasty dynasty) {
        AiPersonality personality = dynasty != null ? dynasty.getAiPersonality() : null;
        List<Upgrade> list = new ArrayList<>();
        list.add(GameUnlocks.ROLE_BUILDER);
        list.add(GameUnlocks.TYPE_EGG);
        list.add(GameUnlocks.TYPE_SOLDIER);
        list.add(GameUnlocks.ROLE_RANCHER);
        list.add(GameUnlocks.STAT_SCOUTING_1);
        list.add(GameUnlocks.TYPE_PRINCESS);
        list.add(GameUnlocks.ABILITY_FORCED_FLIGHT);
        if (personality == AiPersonality.MILITARIST) {
            list.add(GameUnlocks.STAT_ATTACK_1);
            list.add(GameUnlocks.STAT_HEALTH_1);
            list.add(GameUnlocks.STAT_DEFENSE_1);
            list.add(GameUnlocks.ROLE_DIPLOMAT);
        } else {
            list.add(GameUnlocks.ROLE_DIPLOMAT);
            list.add(GameUnlocks.STAT_RESEARCH_1);
        }
        list.add(GameUnlocks.ABILITY_RESIN);
        list.add(GameUnlocks.STAT_WORKER_SPEED_2);
        list.add(GameUnlocks.ABILITY_TUNNELS);
        list.add(GameUnlocks.ROLE_TRANSPORT);
        list.add(GameUnlocks.ROLE_ESCORT);
        list.add(GameUnlocks.STAT_ATTACK_2);
        list.add(GameUnlocks.STAT_HEALTH_2);
        list.add(GameUnlocks.STAT_DEFENSE_2);
        list.add(GameUnlocks.STAT_GROWTH_1);
        list.add(GameUnlocks.ROLE_SIEGE);
        return list;
    }

    public static Upgrade tryPurchaseNextUpgrade(Dynasty dynasty) {
        if (dynasty == null || dynasty.isPlayer()) {
            return null;
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)
                && dynasty.getResearchPoints() < 100) {
            return null;
        }
        for (Upgrade upgrade : researchPriority(dynasty)) {
            if (upgrade == null || dynasty.hasUpgrade(upgrade)) {
                continue;
            }
            if (upgrade.getRequirement() != null && !dynasty.hasUpgrade(upgrade.getRequirement())) {
                continue;
            }
            if (!GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, upgrade)) {
                continue;
            }
            if (!upgrade.isAvailableFor(dynasty)) {
                continue;
            }
            int cost = upgrade.getCost();
            if (cost <= 0) {
                continue;
            }
            if (dynasty.getResearchPoints() < cost) {
                continue;
            }
            dynasty.addResearchPoints(-cost);
            dynasty.unlockUpgrade(upgrade);
            unlockFreeCompanions(dynasty, upgrade);
            return upgrade;
        }
        return null;
    }

    private static void unlockFreeCompanions(Dynasty dynasty, Upgrade purchased) {
        if (purchased == GameUnlocks.TYPE_SOLDIER) {
            dynasty.unlockUpgrade(GameUnlocks.TYPE_MAJOR);
        }
        if (purchased == GameUnlocks.TYPE_PRINCESS) {
            dynasty.unlockUpgrade(GameUnlocks.TYPE_QUEEN);
        }
        if (dynasty.getAiPersonality() == AiPersonality.MILITARIST
                && (purchased == GameUnlocks.STAT_ATTACK_1
                || purchased == GameUnlocks.STAT_HEALTH_1
                || purchased == GameUnlocks.STAT_DEFENSE_1)) {
            dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
            dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        }
    }

    public static int buildingPriorityScore(Building building) {
        if (building == null) {
            return 1000;
        }
        String key = building.getNameKey();
        if (key == null) {
            return 500;
        }
        if (key.startsWith("MUSHROOM_CHAMBER_") || key.startsWith("PLANT_CHAMBER_")) {
            return 10 + buildingTier(building);
        }
        if (key.startsWith("WATER_RESERVOIR_") || key.startsWith("MEAT_CHAMBER_")) {
            return 20 + buildingTier(building);
        }
        if (key.startsWith("EGG_CHAMBER_")) {
            return 40 + buildingTier(building);
        }
        if (key.startsWith("ROYAL_CHAMBER_")) {
            return 55 + buildingTier(building);
        }
        if (key.startsWith("ROCK_WAREHOUSE_") || key.startsWith("RESIN_RESERVOIR_")) {
            return 70 + buildingTier(building);
        }
        if (key.startsWith("PASSIVE_")) {
            return 80;
        }
        return 100 + building.getMineralCost() + building.getResinCost();
    }

    private static int buildingTier(Building building) {
        return Math.max(0, building.getLevel());
    }
}
