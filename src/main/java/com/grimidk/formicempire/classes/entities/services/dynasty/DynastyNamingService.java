package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.dynasty.DynastyTitle;
import com.grimidk.formicempire.classes.constants.dynasty.colony.CityTitle;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DynastyNamingService {
    private final Set<String> usedThemeKeys = new HashSet<>();

    public DynastyTitle pickRandomTitle() {
        List<DynastyTitle> titles = GameConstants.getDynastyTitles();
        return titles.get(GameRandom.nextInt(titles.size()));
    }

    public CityTitle pickRandomCapitalCityTitle() {
        List<CityTitle> titles = GameConstants.getCapitalCityTitles();
        if (titles.isEmpty()) {
            return GameConstants.CITY_TITLE_PRIME;
        }
        return titles.get(GameRandom.nextInt(titles.size()));
    }

    public CityTitle pickRandomSatelliteCityTitle() {
        return pickUnusedSatelliteCityTitle(Set.of());
    }

    public CityTitle pickUnusedSatelliteCityTitle(Set<String> usedTitleKeys) {
        List<CityTitle> pool = new ArrayList<>();
        for (CityTitle title : GameConstants.getSatelliteCityTitles()) {
            if (usedTitleKeys == null || !usedTitleKeys.contains(title.getNameKey())) {
                pool.add(title);
            }
        }
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(GameRandom.nextInt(pool.size()));
    }

    public String claimThemeKey(AntSpecies species) {
        String key = pickThemeKey(species);
        usedThemeKeys.add(key);
        return key;
    }

    public String generateDynastyName(AntSpecies species) {
        return generateDynastyName(species, pickRandomTitle());
    }

    public String generateDynastyName(AntSpecies species, DynastyTitle title) {
        String themeKey = claimThemeKey(species);
        return LanguageStrings.formatDynastyName(LanguageStrings.resolveDynastyThemeDisplay(themeKey), title);
    }

    public String generateCapitalName(String themeBase) {
        return LanguageStrings.formatCityName(themeBase, pickRandomCapitalCityTitle());
    }

    public String generateSatelliteColonyName(String themeBase, Set<String> usedCityTitleKeys) {
        CityTitle cityTitle = pickUnusedSatelliteCityTitle(usedCityTitleKeys);
        if (cityTitle != null) {
            return LanguageStrings.formatCityName(themeBase, cityTitle);
        }
        int fallbackIndex = usedCityTitleKeys == null ? 1 : usedCityTitleKeys.size() + 1;
        return LanguageStrings.formatProceduralColonyName(themeBase, fallbackIndex);
    }

    private String pickThemeKey(AntSpecies species) {
        List<String> pool = new ArrayList<>();

        if (species != null) {
            for (String key : species.getPreferredNameKeys()) {
                if (key != null && !key.isEmpty() && !usedThemeKeys.contains(key)) {
                    pool.add(key);
                }
            }
        }

        if (pool.isEmpty()) {
            for (String key : GameConstants.getGenericDynastyThemeKeys()) {
                if (key != null && !key.isEmpty() && !usedThemeKeys.contains(key)) {
                    pool.add(key);
                }
            }
        }

        if (pool.isEmpty()) {
            List<String> generics = GameConstants.getGenericDynastyThemeKeys();
            if (!generics.isEmpty()) {
                return generics.get(GameRandom.nextInt(generics.size()));
            }
            return LanguageStrings.DYNASTY_THEME_ANT;
        }

        return pool.get(GameRandom.nextInt(pool.size()));
    }

    public void registerUsedName(String dynastyName) {
        String theme = LanguageStrings.stripDynastyNameSuffix(dynastyName);
        if (theme == null || theme.isEmpty()) {
            return;
        }
        String key = LanguageStrings.findDynastyThemeKey(theme);
        usedThemeKeys.add(key != null ? key : theme);
    }

    public void registerUsedThemeKey(String themeKeyOrDisplay) {
        if (themeKeyOrDisplay == null || themeKeyOrDisplay.isEmpty()) {
            return;
        }
        if (LanguageStrings.isDynastyThemeKey(themeKeyOrDisplay)) {
            usedThemeKeys.add(themeKeyOrDisplay);
            return;
        }
        String key = LanguageStrings.findDynastyThemeKey(themeKeyOrDisplay);
        usedThemeKeys.add(key != null ? key : themeKeyOrDisplay);
    }
}
