package com.grimidk.formicempire.classes.entities.services.colony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.hunt.HuntExpedition;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;

public final class ColonyUnassignedAntService {

    private ColonyUnassignedAntService() {
    }

    public static Engine resolveEngine(Colony colony) {
        if (colony == null) {
            return null;
        }
        Dynasty dynasty = colony.getDynasty();
        World world = dynasty != null ? dynasty.getOwningWorld() : null;
        return world != null ? world.getEngine() : null;
    }

    public static boolean isUnassigned(Ant ant, Colony colony, Engine engine) {
        if (ant == null || colony == null || !ant.isAlive() || ant.isOnTrade()) {
            return false;
        }
        Engine resolved = engine != null ? engine : resolveEngine(colony);
        AntType type = ant.getAntType();
        AntRole defaultRole = Engine.resolveDefaultRoleForAntType(type, resolved);
        AntRole role = ant.getRole();
        return defaultRole != null && defaultRole.equals(role);
    }

    public static boolean isAvailableForCritterParty(Ant ant, Colony colony, Engine engine) {
        if (!isUnassigned(ant, colony, engine)) {
            return false;
        }
        for (HuntExpedition hunt : colony.getActiveHuntExpeditions()) {
            if (hunt.getParty().contains(ant)) {
                return false;
            }
        }
        for (com.grimidk.formicempire.classes.entities.invasion.InvasionDefense defense
                : colony.getActiveInvasionDefenses()) {
            if (defense.getParty().contains(ant)) {
                return false;
            }
        }
        return true;
    }

    public static int countUnassignedInList(Colony colony, List<Ant> source, Engine engine) {
        if (source == null || source.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Ant ant : source) {
            if (isAvailableForCritterParty(ant, colony, engine)) {
                count++;
            }
        }
        return count;
    }

    public static Map<AntType, Integer> countUnassignedByTypes(Colony colony, List<AntType> types, Engine engine) {
        Map<AntType, Integer> counts = new HashMap<>();
        if (colony == null || types == null) {
            return counts;
        }
        for (AntType type : types) {
            if (type == null) {
                continue;
            }
            counts.put(type, countUnassignedInList(colony, colony.getAntsByType(type), engine));
        }
        return counts;
    }

    public static List<Ant> buildPartyFromCounts(Colony colony, Map<AntType, Integer> partyCounts, Engine engine) {
        List<Ant> party = new ArrayList<>();
        if (colony == null || partyCounts == null) {
            return party;
        }
        for (Map.Entry<AntType, Integer> entry : partyCounts.entrySet()) {
            AntType type = entry.getKey();
            int wanted = entry.getValue() != null ? entry.getValue() : 0;
            if (type == null || wanted <= 0) {
                continue;
            }
            appendFromList(colony, colony.getAntsByType(type), wanted, party, engine);
        }
        return party;
    }

    private static void appendFromList(Colony colony, List<Ant> source, int wanted, List<Ant> picked, Engine engine) {
        if (source == null || wanted <= 0 || picked == null) {
            return;
        }
        int added = 0;
        for (Ant ant : source) {
            if (added >= wanted) {
                break;
            }
            if (!isAvailableForCritterParty(ant, colony, engine)) {
                continue;
            }
            picked.add(ant);
            added++;
        }
    }

    public static int computeUnassignedPercent(int totalAnts, int totalAssigned) {
        if (totalAnts <= 0) {
            return 0;
        }
        int unassigned = Math.max(0, totalAnts - totalAssigned);
        return Math.round(100f * unassigned / totalAnts);
    }
}
