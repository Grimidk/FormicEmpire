package com.grimidk.formicempire.classes.entities.services.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.grimidk.formicempire.classes.entities.world.GeographicFeature;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public class GeographicFeatureNamingService {
    private final Set<String> usedKeys = new HashSet<>();
    private final Set<String> usedDisplayNames = new HashSet<>();

    public void reset() {
        usedKeys.clear();
        usedDisplayNames.clear();
    }

    public String claimNameKey(GeographicFeature.FeatureType type, int id) {
        if (type == null) {
            return null;
        }
        String[] pool = switch (type) {
            case RIVER -> LanguageStrings.RIVER_NAMES;
            case MOUNTAIN_RANGE -> LanguageStrings.MOUNTAIN_RANGE_NAMES;
            case VALLEY -> LanguageStrings.VALLEY_NAMES;
            case LAKE_BASIN -> LanguageStrings.LAKE_BASIN_NAMES;
            case OCEAN -> LanguageStrings.OCEAN_NAMES;
            case PENINSULA -> LanguageStrings.PENINSULA_NAMES;
            case TRENCH -> LanguageStrings.TRENCH_NAMES;
            case ARCHIPELAGO -> LanguageStrings.ARCHIPELAGO_NAMES;
            case CONTINENT -> LanguageStrings.CONTINENT_NAMES;
            default -> null;
        };

        if (pool == null || pool.length == 0) {
            return null;
        }

        List<String> available = new ArrayList<>();
        for (String key : pool) {
            if (!usedKeys.contains(key)) {
                available.add(key);
            }
        }

        if (!available.isEmpty()) {
            int idx = GameRandom.nextInt(available.size());
            String chosen = available.get(idx);
            usedKeys.add(chosen);
            usedDisplayNames.add(LanguageStrings.get(chosen));
            return chosen;
        }

        int idx = (Math.max(1, id) - 1) % pool.length;
        String baseKey = pool[idx];
        usedKeys.add(baseKey);
        return baseKey;
    }

    public String formatDisplayName(String nameKey, int id) {
        if (nameKey == null || nameKey.isEmpty()) {
            return "";
        }
        String baseName = LanguageStrings.get(nameKey);
        if (!usedDisplayNames.contains(baseName)) {
            usedDisplayNames.add(baseName);
            return baseName;
        }
        int num = 2;
        String candidate = baseName + " " + toRomanNumeral(num);
        while (usedDisplayNames.contains(candidate)) {
            num++;
            candidate = baseName + " " + toRomanNumeral(num);
        }
        usedDisplayNames.add(candidate);
        return candidate;
    }

    private static String toRomanNumeral(int n) {
        String[] romans = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV"};
        if (n >= 1 && n < romans.length) return romans[n];
        return String.valueOf(n);
    }
}
