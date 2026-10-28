package com.grimidk.formicempire.classes.entities.services.shared;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

public final class HexWaterCrossing {
    private HexWaterCrossing() {}

    public static boolean isWaterBiome(Biome biome) {
        return biome == GameConstants.BIOME_OCEAN
                || biome == GameConstants.BIOME_DEEP_OCEAN
                || biome == GameConstants.BIOME_SHALLOW_OCEAN
                || biome == GameConstants.BIOME_LAKE;
    }

    public static boolean isWaterHex(Hex hex) {
        return hex != null && isWaterBiome(hex.getBiome());
    }

    public static boolean isLandHex(Hex hex) {
        return hex != null && !isWaterBiome(hex.getBiome());
    }

    public static int waterCrossRange(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        if (dynasty.hasUpgrade(GameUnlocks.STAT_RAFTING_2)) {
            return GameNumbers.RAFTING_WATER_CROSS_RANGE_3;
        }
        if (dynasty.hasUpgrade(GameUnlocks.STAT_RAFTING_1)) {
            return GameNumbers.RAFTING_WATER_CROSS_RANGE_2;
        }
        if (dynasty.hasUpgrade(GameUnlocks.ASSIMILATED_RAFTING)) {
            return GameNumbers.RAFTING_WATER_CROSS_RANGE_1;
        }
        return 0;
    }

    public static boolean canCrossBetween(Hex a, Hex b, Dynasty dynasty) {
        if (a == null || b == null) {
            return false;
        }
        if (!a.hasRiverTo(b) && !b.hasRiverTo(a)) {
            return true;
        }
        if (dynasty == null) {
            return false;
        }
        if (waterCrossRange(dynasty) > 0) {
            return true;
        }
        var tunnel = dynasty.getTunnelBetween(a, b);
        return tunnel != null && tunnel.isComplete();
    }

    public static List<Hex> adjacentColonizableLandHexes(Hex origin, Dynasty dynasty) {
        List<Hex> result = new ArrayList<>();
        if (origin == null) {
            return result;
        }
        for (Hex neighbor : origin.getAdjacentNeighbors()) {
            if (!isLandHex(neighbor) || neighbor.isIsland()) {
                continue;
            }
            if (origin.hasRiverTo(neighbor) && !canCrossBetween(origin, neighbor, dynasty)) {
                continue;
            }
            result.add(neighbor);
        }
        return result;
    }

    public static List<Hex> adjacentColonizableLandHexes(Hex origin) {
        Dynasty d = origin != null && origin.getColony() != null ? origin.getColony().getDynasty() : null;
        return adjacentColonizableLandHexes(origin, d);
    }

    public static List<Hex> waterCrossLandHexes(Hex origin, int waterCrossRange) {
        List<Hex> result = new ArrayList<>();
        if (origin == null || waterCrossRange <= 0) {
            return result;
        }
        Set<Hex> found = new HashSet<>();
        Map<Hex, Integer> waterDepth = new HashMap<>();
        Queue<Hex> queue = new ArrayDeque<>();
        for (Hex adjacent : origin.getAdjacentNeighbors()) {
            if (!isWaterHex(adjacent)) {
                continue;
            }
            waterDepth.put(adjacent, 1);
            queue.add(adjacent);
        }
        while (!queue.isEmpty()) {
            Hex water = queue.poll();
            int depth = waterDepth.get(water);
            for (Hex neighbor : water.getAdjacentNeighbors()) {
                if (neighbor == null || neighbor == origin) {
                    continue;
                }
                if (isLandHex(neighbor)) {
                    if (found.add(neighbor)) {
                        result.add(neighbor);
                    }
                    continue;
                }
                if (!isWaterHex(neighbor) || depth >= waterCrossRange || waterDepth.containsKey(neighbor)) {
                    continue;
                }
                waterDepth.put(neighbor, depth + 1);
                queue.add(neighbor);
            }
        }
        return result;
    }

    public static List<Hex> colonizableLandHexes(Hex origin, int waterCrossRange, Dynasty dynasty) {
        List<Hex> result = new ArrayList<>(adjacentColonizableLandHexes(origin, dynasty));
        Set<Hex> seen = new HashSet<>(result);
        for (Hex across : waterCrossLandHexes(origin, waterCrossRange)) {
            if (seen.add(across)) {
                result.add(across);
            }
        }
        return result;
    }

    public static List<Hex> colonizableLandHexes(Hex origin, int waterCrossRange) {
        Dynasty d = origin != null && origin.getColony() != null ? origin.getColony().getDynasty() : null;
        return colonizableLandHexes(origin, waterCrossRange, d);
    }

    public static List<Hex> tradeReachableLandHexes(Hex origin, int waterCrossRange, Dynasty dynasty) {
        List<Hex> result = new ArrayList<>();
        if (origin == null) {
            return result;
        }
        Set<Hex> seen = new HashSet<>();
        for (Hex neighbor : origin.getAdjacentNeighbors()) {
            if (!isLandHex(neighbor) || !seen.add(neighbor)) {
                continue;
            }
            if (origin.hasRiverTo(neighbor) && !canCrossBetween(origin, neighbor, dynasty)) {
                continue;
            }
            result.add(neighbor);
        }
        for (Hex across : waterCrossLandHexes(origin, waterCrossRange)) {
            if (seen.add(across)) {
                result.add(across);
            }
        }
        return result;
    }

    public static List<Hex> tradeReachableLandHexes(Hex origin, int waterCrossRange) {
        Dynasty d = origin != null && origin.getColony() != null ? origin.getColony().getDynasty() : null;
        return tradeReachableLandHexes(origin, waterCrossRange, d);
    }
}
