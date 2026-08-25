package com.grimidk.formicempire.classes.entities.services.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class ColonySourceService {

    private static final long RANK_UPDATE_INTERVAL_MS = 2000;

    private final ColonySpatialService spatialService;
    private final List<ResourceSource> discoveredSources;
    private final Map<ResourceType, List<ResourceSource>> rankedSourcesCache = new HashMap<>();
    private long lastRankUpdate = 0;

    public ColonySourceService(ColonySpatialService spatialService) {
        this.spatialService = spatialService;
        this.discoveredSources = new CopyOnWriteArrayList<>();
    }

    public List<ResourceSource> getDiscoveredSources() {
        return Collections.unmodifiableList(discoveredSources);
    }

    public List<ResourceSource> getSourcesByType(ResourceType type) {
        return discoveredSources.stream()
                .filter(source -> source.getResourceType() == type)
                .collect(Collectors.toList());
    }

    public int getTotalQuantityAvailable(ResourceType type) {
        return discoveredSources.stream()
                .filter(source -> source.getResourceType() == type)
                .mapToInt(ResourceSource::getQuantity)
                .sum();
    }

    public boolean isSourceFull(Colony colony, ResourceType type) {
        int capacity = colony.getStatsService().getSourceCapacity(colony);
        long count = discoveredSources.stream()
                .filter(s -> s.getResourceType() == type)
                .count();
        return count >= capacity;
    }

    public boolean addSource(Colony colony, ResourceSource source) {
        int capacity = colony.getStatsService().getSourceCapacity(colony);

        long count = discoveredSources.stream()
                .filter(s -> s.getResourceType() == source.getResourceType())
                .count();

        if (count < capacity) {
            this.discoveredSources.add(source);
            invalidateCache();
            colony.logEvent(ColonyLogPrefixes.INFO + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_FOUND_NEW_SOURCE_FMT), source.getResourceType().getName()));
            return true;
        }
        colony.logEvent(ColonyLogPrefixes.INFO + " "
            + String.format(LanguageStrings.get(LanguageStrings.LOG_FOUND_SOURCE_FULL_FMT), source.getResourceType().getName()));
        return false;
    }

    public void removeSource(Colony colony, ResourceSource source) {
        if (this.discoveredSources.remove(source)) {
            invalidateCache();
            colony.logEvent(ColonyLogPrefixes.INFO + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_SOURCE_EXHAUSTED_FMT), source.getResourceType().getName()));
        }
    }

    private void invalidateCache() {
        synchronized (rankedSourcesCache) {
            rankedSourcesCache.clear();
            lastRankUpdate = 0;
        }
    }

    public int gatherFromSource(Colony colony, ResourceSource source, int amountWanted) {
        if (!isActiveSource(source)) {
            return 0;
        }

        int available = source.getQuantity();
        int gathered = Math.min(available, amountWanted);

        source.decreaseQuantity(gathered);

        if (source.getQuantity() <= 0) {
            removeSource(colony, source);
        }

        return gathered;
    }

    public double computeGatherEfficiency(Colony colony, ResourceSource source, List<Ant> workers) {
        NeoPoint nest = spatialService.getColonyEntrance(colony);
        float radius = GatheringMath.computeFullEfficiencyRadius(colony, workers);
        return GatheringMath.gatheringEfficiency(
            nest.getX(), nest.getY(), source.getCenterX(), source.getCenterY(), radius);
    }

    public float computeFullEfficiencyRadius(Colony colony, List<Ant> workers) {
        return GatheringMath.computeFullEfficiencyRadius(colony, workers);
    }

    public boolean isActiveSource(ResourceSource source) {
        return source != null && source.getQuantity() > 0 && discoveredSources.contains(source);
    }

    public ResourceSource findNearestRelevantSource(Colony colony, Ant ant) {
        List<ResourceType> targetTypes = new ArrayList<>();
        AntRole role = ant.getRole();

        if (role == GameConstants.ROLE_FORAGER) {
            targetTypes.add(GameConstants.RESOURCE_PLANT);
            targetTypes.add(GameConstants.RESOURCE_WATER);
            if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_2)) {
                targetTypes.add(GameConstants.RESOURCE_FUNGI);
            }
            if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_3) && colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)) {
                targetTypes.add(GameConstants.RESOURCE_RESIN);
            }
        } else if (role == GameConstants.ROLE_HUNTER) {
            targetTypes.add(GameConstants.RESOURCE_MEAT);
        } else if (role == GameConstants.ROLE_MINER) {
            targetTypes.add(GameConstants.RESOURCE_ROCK);
        }

        synchronized (rankedSourcesCache) {
            updateRankedCache(colony);

            for (ResourceType type : targetTypes) {
                List<ResourceSource> ranked = rankedSourcesCache.get(type);
                if (ranked == null || ranked.isEmpty()) {
                    continue;
                }

                List<ResourceSource> active = ranked.stream()
                        .filter(this::isActiveSource)
                        .collect(Collectors.toList());
                if (active.isEmpty()) {
                    continue;
                }

                if (GameRandom.nextDouble() < 0.70 || active.size() == 1) {
                    return active.get(0);
                }
                return active.get(1 + GameRandom.nextInt(active.size() - 1));
            }
        }
        return null;
    }

    private void updateRankedCache(Colony colony) {
        long now = System.currentTimeMillis();
        if (now - lastRankUpdate < RANK_UPDATE_INTERVAL_MS && !rankedSourcesCache.isEmpty()) {
            boolean stale = false;
            outer:
            for (List<ResourceSource> ranked : rankedSourcesCache.values()) {
                for (ResourceSource source : ranked) {
                    if (!isActiveSource(source)) {
                        stale = true;
                        break outer;
                    }
                }
            }
            if (!stale) {
                return;
            }
        }

        rankedSourcesCache.clear();
        NeoPoint nest = spatialService.getColonyEntrance(colony);
        final double nestX = nest.getX();
        final double nestY = nest.getY();

        for (ResourceType type : GameConstants.getResources()) {
            List<ResourceSource> sources = discoveredSources.stream()
                .filter(s -> s.getResourceType() == type && s.getQuantity() > 0)
                .sorted(Comparator.comparingDouble(s -> {
                    double dx = s.getCenterX() - nestX;
                    double dy = s.getCenterY() - nestY;
                    return dx * dx + dy * dy;
                }))
                .collect(Collectors.toList());

            if (!sources.isEmpty()) {
                rankedSourcesCache.put(type, sources);
            }
        }
        lastRankUpdate = now;
    }

    public static final class GatheringMath {

        private GatheringMath() {
        }

        public static double gatheringEfficiency(
                double nestX,
                double nestY,
                int sourceX,
                int sourceY,
                float fullEfficiencyRadius) {
            if (fullEfficiencyRadius <= 0f) {
                return GameNumbers.GATHER_MIN_EFFICIENCY;
            }
            double dx = sourceX - nestX;
            double dy = sourceY - nestY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist <= fullEfficiencyRadius) {
                return 1.0;
            }
            return Math.max(GameNumbers.GATHER_MIN_EFFICIENCY, fullEfficiencyRadius / dist);
        }

        public static float computeFullEfficiencyRadius(Colony colony, List<Ant> workers) {
            float refTravel = GameNumbers.BASE_SPRITE_SPEED * GameConstants.TYPE_WORKER.getSpeedMult();
            if (refTravel <= 1e-6f) {
                return GameNumbers.GATHER_FULL_EFFICIENCY_RADIUS_BASE;
            }
            if (workers == null || workers.isEmpty()) {
                return GameNumbers.GATHER_FULL_EFFICIENCY_RADIUS_BASE;
            }
            float sum = 0f;
            int n = 0;
            for (Ant ant : workers) {
                if (ant == null || !ant.isAlive()) {
                    continue;
                }
                sum += effectiveTravelUnits(colony, ant);
                n++;
            }
            if (n == 0) {
                return GameNumbers.GATHER_FULL_EFFICIENCY_RADIUS_BASE;
            }
            float avg = sum / n;
            float colonyMult = colony.hasUpgrade(GameUnlocks.STAT_ACID)
                    ? GameNumbers.GATHER_COLONY_SPEED_RADIUS_MULT
                    : 1f;
            return GameNumbers.GATHER_FULL_EFFICIENCY_RADIUS_BASE * (avg / refTravel) * colonyMult;
        }

        private static float effectiveTravelUnits(Colony colony, Ant ant) {
            float u = GameNumbers.BASE_SPRITE_SPEED * ant.getAntType().getSpeedMult();
            if (colony.isCreatineDietActive()) {
                u *= GameNumbers.CREATINE_DIET_SPEED_MULTIPLIER;
            }
            u *= colony.getStatsService().getLocsenseSpeedMultiplier(colony);
            if (ant.getAntType() == GameConstants.TYPE_WORKER && colony.hasUpgrade(GameUnlocks.STAT_WORKER_SPEED_2)) {
                u *= 2f;
            }
            return u;
        }
    }
}
