package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ColonyResourceDeathSelection {

    private ColonyResourceDeathSelection() {}

    static List<Ant> selectVictims(List<Ant> candidates, int deathCount, Set<Ant> exclude) {
        if (deathCount <= 0 || candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        Map<AntType, List<Ant>> byType = new LinkedHashMap<>();
        List<Ant> queens = new ArrayList<>();

        for (Ant ant : candidates) {
            if (ant == null || !ant.isAlive()) {
                continue;
            }
            if (exclude != null && exclude.contains(ant)) {
                continue;
            }
            if (ant.getAntType() == GameConstants.TYPE_QUEEN) {
                queens.add(ant);
            } else {
                byType.computeIfAbsent(ant.getAntType(), type -> new ArrayList<>()).add(ant);
            }
        }

        int nonQueenTotal = 0;
        for (List<Ant> ants : byType.values()) {
            nonQueenTotal += ants.size();
        }

        int maxKillable = nonQueenTotal + queens.size();
        if (maxKillable == 0) {
            return List.of();
        }

        int toKill = Math.min(deathCount, maxKillable);
        List<Ant> victims = new ArrayList<>(toKill);

        int fromNonQueens = Math.min(toKill, nonQueenTotal);
        if (fromNonQueens > 0) {
            victims.addAll(pickProportional(byType, fromNonQueens));
        }

        int remaining = toKill - victims.size();
        if (remaining > 0 && !queens.isEmpty()) {
            Collections.shuffle(queens, GameRandom.getShuffleRandom());
            for (int i = 0; i < remaining && i < queens.size(); i++) {
                victims.add(queens.get(i));
            }
        }

        return victims;
    }

    private static List<Ant> pickProportional(Map<AntType, List<Ant>> byType, int deathCount) {
        int total = 0;
        for (List<Ant> ants : byType.values()) {
            total += ants.size();
        }
        if (total <= 0 || deathCount <= 0) {
            return List.of();
        }

        deathCount = Math.min(deathCount, total);
        Map<AntType, Integer> killsByType = allocateProportional(byType, deathCount);

        List<Ant> victims = new ArrayList<>(deathCount);
        for (Map.Entry<AntType, List<Ant>> entry : byType.entrySet()) {
            int n = killsByType.getOrDefault(entry.getKey(), 0);
            if (n <= 0) {
                continue;
            }
            List<Ant> pool = new ArrayList<>(entry.getValue());
            Collections.shuffle(pool, GameRandom.getShuffleRandom());
            for (int i = 0; i < n && i < pool.size(); i++) {
                victims.add(pool.get(i));
            }
        }
        return victims;
    }

    private static Map<AntType, Integer> allocateProportional(Map<AntType, List<Ant>> byType, int deathCount) {
        List<AntType> types = new ArrayList<>(byType.keySet());
        int total = 0;
        for (AntType type : types) {
            total += byType.get(type).size();
        }

        Map<AntType, Integer> allocated = new HashMap<>();
        double[] exact = new double[types.size()];
        int assigned = 0;

        for (int i = 0; i < types.size(); i++) {
            AntType type = types.get(i);
            int count = byType.get(type).size();
            exact[i] = deathCount * (double) count / total;
            int floor = (int) exact[i];
            allocated.put(type, floor);
            assigned += floor;
        }

        int remainder = deathCount - assigned;
        if (remainder > 0) {
            Integer[] order = new Integer[types.size()];
            for (int i = 0; i < types.size(); i++) {
                order[i] = i;
            }
            java.util.Arrays.sort(order, (a, b) -> {
                double fracA = exact[a] - allocated.get(types.get(a));
                double fracB = exact[b] - allocated.get(types.get(b));
                int cmp = Double.compare(fracB, fracA);
                if (cmp != 0) {
                    return cmp;
                }
                return types.get(a).getId() - types.get(b).getId();
            });
            for (int i = 0; i < remainder && i < order.length; i++) {
                AntType type = types.get(order[i]);
                allocated.put(type, allocated.get(type) + 1);
            }
        }

        return allocated;
    }
}
