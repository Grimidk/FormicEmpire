package com.grimidk.formicempire.classes.entities.services.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public final class WorldWeatherService {

    private WorldWeatherService() {
    }

    public static boolean hasVisualWeather(Weather w) {
        return w != null && w != GameConstants.WEATHER_CLEAR;
    }

    public static int randomizeWorldWeather(World world) {
        if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
            return 0;
        }
        List<Hex> validHexes = new ArrayList<>(world.getHexes().size());
        for (Hex h : world.getHexes()) {
            if (h != null) {
                validHexes.add(h);
            }
        }
        int total = validHexes.size();
        if (total == 0) {
            return 0;
        }

        List<Weather> nonClearWeathers = new ArrayList<>();
        for (Weather w : GameConstants.getWeathers()) {
            if (hasVisualWeather(w)) {
                nonClearWeathers.add(w);
            }
        }
        if (nonClearWeathers.isEmpty()) {
            return 0;
        }
        Collections.shuffle(nonClearWeathers, GameRandom.getShuffleRandom());

        int frontCount = Math.max(nonClearWeathers.size(), Math.min(48, Math.max(nonClearWeathers.size(), total / 120)));
        int baseRadius = total >= 500 ? 2 : 1;
        int radiusBound = total >= 500 ? 3 : 2;
        Set<Hex> covered = new HashSet<>();

        for (int i = 0; i < frontCount; i++) {
            Weather chosen = i < nonClearWeathers.size()
                    ? nonClearWeathers.get(i)
                    : nonClearWeathers.get(GameRandom.nextInt(nonClearWeathers.size()));
            Hex seed = pickSeedForWeather(chosen, validHexes, covered);
            if (seed == null) {
                continue;
            }
            int radius = baseRadius + GameRandom.nextInt(radiusBound);
            Set<Hex> patch = collectPatch(seed, radius);
            boolean stampedAny = false;
            for (Hex h : patch) {
                if (h != null && covered.add(h)) {
                    h.setLocalWeather(chosen);
                    stampedAny = true;
                }
            }
            if (!stampedAny) {
                seed.setLocalWeather(chosen);
                covered.add(seed);
            }
        }

        for (Hex h : validHexes) {
            if (!covered.contains(h)) {
                h.setLocalWeather(GameConstants.WEATHER_CLEAR);
            }
        }
        return covered.size();
    }

    private static Hex pickSeedForWeather(Weather weather, List<Hex> validHexes, Set<Hex> covered) {
        List<Hex> biomeMatches = new ArrayList<>();
        List<Hex> uncovered = new ArrayList<>();
        for (Hex h : validHexes) {
            if (covered.contains(h)) {
                continue;
            }
            uncovered.add(h);
            if (matchesPreferredBiome(weather, h.getBiome())) {
                biomeMatches.add(h);
            }
        }
        if (!biomeMatches.isEmpty()) {
            return biomeMatches.get(GameRandom.nextInt(biomeMatches.size()));
        }
        if (!uncovered.isEmpty()) {
            return uncovered.get(GameRandom.nextInt(uncovered.size()));
        }
        return validHexes.get(GameRandom.nextInt(validHexes.size()));
    }

    private static boolean matchesPreferredBiome(Weather weather, Biome biome) {
        if (weather == null || biome == null) {
            return false;
        }
        if (weather == GameConstants.WEATHER_SNOW || weather == GameConstants.WEATHER_HEAVY_SNOW) {
            return biome == GameConstants.BIOME_ICE_FIELD
                    || biome == GameConstants.BIOME_TUNDRA
                    || biome == GameConstants.BIOME_PEAK
                    || biome == GameConstants.BIOME_TAIGA;
        }
        if (weather == GameConstants.WEATHER_SAND_STORM || weather == GameConstants.WEATHER_HEAT) {
            return biome == GameConstants.BIOME_DESERT || biome == GameConstants.BIOME_BARREN;
        }
        if (weather == GameConstants.WEATHER_PYROCLASTIC_FOG) {
            return biome == GameConstants.BIOME_VOLCANIC;
        }
        if (weather == GameConstants.WEATHER_ACID_RAIN) {
            return biome == GameConstants.BIOME_URBAN;
        }
        if (weather == GameConstants.WEATHER_FOG) {
            return biome == GameConstants.BIOME_SWAMP
                    || biome == GameConstants.BIOME_WETLANDS
                    || biome == GameConstants.BIOME_MANGROVE
                    || biome == GameConstants.BIOME_LAKE
                    || biome == GameConstants.BIOME_SHALLOW_OCEAN;
        }
        return false;
    }

    public static void applyCoherentWeatherPatches(World world) {
        if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
            return;
        }

        List<Hex> allHexes = world.getHexes();
        int total = allHexes.size();
        int frontCount = Math.max(2, Math.min(8, total / 24));

        Set<Hex> covered = new HashSet<>();

        for (int i = 0; i < frontCount; i++) {
            Hex seed = allHexes.get(GameRandom.nextInt(total));
            if (seed == null || covered.contains(seed)) {
                continue;
            }

            Weather frontWeather = pickFrontWeather(seed.getBiome(), world);
            if (frontWeather == null) {
                continue;
            }

            Set<Hex> patch = collectPatch(seed, 1 + GameRandom.nextInt(2));
            for (Hex h : patch) {
                if (h != null) {
                    h.setLocalWeather(frontWeather);
                    covered.add(h);
                }
            }
        }

        for (Hex h : allHexes) {
            if (h != null && !covered.contains(h)) {
                h.setLocalWeather(resolveBaselineWeather(h.getBiome(), world));
            }
        }
    }

    public static void tickWeatherDrift(World world) {
        if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
            return;
        }

        if (GameRandom.nextInt(100) < 30) {
            applyCoherentWeatherPatches(world);
            return;
        }

        List<Hex> allHexes = world.getHexes();
        for (Hex h : allHexes) {
            if (h != null && isPrecipitation(h.getLocalWeather())) {
                if (GameRandom.nextInt(100) < 15) {
                    Hex east = h.getNorthEast() != null ? h.getNorthEast() : h.getSouthEast();
                    if (east != null && !isPrecipitation(east.getLocalWeather())) {
                        east.setLocalWeather(h.getLocalWeather());
                    }
                    if (GameRandom.nextInt(100) < 40) {
                        h.setLocalWeather(resolveBaselineWeather(h.getBiome(), world));
                    }
                }
            }
        }
    }

    private static Set<Hex> collectPatch(Hex center, int radius) {
        Set<Hex> patch = new HashSet<>();
        if (center == null) {
            return patch;
        }
        patch.add(center);
        Set<Hex> currentRing = new HashSet<>();
        currentRing.add(center);

        for (int r = 0; r < radius; r++) {
            Set<Hex> nextRing = new HashSet<>();
            for (Hex h : currentRing) {
                for (Hex n : h.getAdjacentNeighbors()) {
                    if (n != null && !patch.contains(n)) {
                        if (GameRandom.nextFloat() < 0.85f) {
                            patch.add(n);
                            nextRing.add(n);
                        }
                    }
                }
            }
            currentRing = nextRing;
        }
        return patch;
    }

    private static Weather pickFrontWeather(Biome biome, World world) {
        if (biome == GameConstants.BIOME_DESERT) {
            return GameRandom.nextInt(100) < 25 ? GameConstants.WEATHER_SAND_STORM : GameConstants.WEATHER_HEAT;
        }
        if (biome == GameConstants.BIOME_VOLCANIC) {
            return GameConstants.WEATHER_PYROCLASTIC_FOG;
        }
        if (biome == GameConstants.BIOME_ICE_FIELD || biome == GameConstants.BIOME_TUNDRA || biome == GameConstants.BIOME_PEAK) {
            return GameRandom.nextBoolean() ? GameConstants.WEATHER_SNOW : GameConstants.WEATHER_HEAVY_SNOW;
        }
        if (biome == GameConstants.BIOME_URBAN && GameRandom.nextInt(100) < 30) {
            return GameConstants.WEATHER_ACID_RAIN;
        }
        if ((biome == GameConstants.BIOME_SWAMP
                || biome == GameConstants.BIOME_WETLANDS
                || biome == GameConstants.BIOME_MANGROVE
                || biome == GameConstants.BIOME_LAKE
                || biome == GameConstants.BIOME_BEACH
                || biome == GameConstants.BIOME_SHALLOW_OCEAN)
                && GameRandom.nextInt(100) < 40) {
            return GameConstants.WEATHER_FOG;
        }

        int roll = GameRandom.nextInt(100);
        if (roll < 40) {
            return GameConstants.WEATHER_RAIN;
        } else if (roll < 68) {
            return GameConstants.WEATHER_HEAVY_RAIN;
        } else if (roll < 82) {
            return GameConstants.WEATHER_THUNDER;
        } else if (roll < 92) {
            return GameConstants.WEATHER_FOG;
        } else {
            return GameConstants.WEATHER_WIND;
        }
    }

    private static Weather resolveBaselineWeather(Biome biome, World world) {
        if (biome == GameConstants.BIOME_ICE_FIELD || biome == GameConstants.BIOME_TUNDRA) {
            return GameConstants.WEATHER_CLEAR;
        }
        if (biome == GameConstants.BIOME_DESERT) {
            return GameConstants.WEATHER_HEAT;
        }
        return GameConstants.WEATHER_CLEAR;
    }

    public static boolean isPrecipitation(Weather w) {
        return w == GameConstants.WEATHER_RAIN
                || w == GameConstants.WEATHER_HEAVY_RAIN
                || w == GameConstants.WEATHER_THUNDER
                || w == GameConstants.WEATHER_ACID_RAIN
                || w == GameConstants.WEATHER_BLOOD
                || w == GameConstants.WEATHER_FROG
                || w == GameConstants.WEATHER_SNOW
                || w == GameConstants.WEATHER_HEAVY_SNOW;
    }
}

