package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.colony.CityTitle;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyNamingServiceTest {

    @BeforeEach
    void setLanguage() {
        LanguageStrings.setLanguage("en");
    }

    @Test
    void preferredThemeKeysComeFromSpeciesTags() {
        AntSpecies leafcutter = GameConstants.SPECIES_LEAFCUTTER;
        assertTrue(leafcutter.getPreferredNameKeys().contains(LanguageStrings.DYNASTY_THEME_LEAF));
        assertFalse(leafcutter.getPreferredNameKeys().contains("Leaf"));
    }

    @Test
    void claimThemeKeyPrefersSpeciesThenAvoidsRepeats() {
        DynastyNamingService naming = new DynastyNamingService();
        AntSpecies species = GameConstants.SPECIES_FIRE;
        Set<String> claimed = new HashSet<>();
        for (int i = 0; i < species.getPreferredNameKeys().size(); i++) {
            String key = naming.claimThemeKey(species);
            assertTrue(species.getPreferredNameKeys().contains(key));
            assertTrue(claimed.add(key), "theme key repeated: " + key);
        }
        String fallback = naming.claimThemeKey(species);
        assertTrue(GameConstants.getGenericDynastyThemeKeys().contains(fallback));
        assertFalse(claimed.contains(fallback));
    }

    @Test
    void satelliteColonyNamesUseSatelliteCityTitles() {
        Dynasty dynasty = new Dynasty(1, "Fire Dynasty", LanguageStrings.DYNASTY_TITLE_DYNASTY, false,
                GameConstants.SPECIES_FIRE);
        dynasty.setThemeBase(LanguageStrings.DYNASTY_THEME_FIRE);
        Colony capital = new Colony(1, dynasty.generateColonyName(0), false);
        dynasty.addColony(capital);

        String satellite = dynasty.generateColonyName(1);
        boolean matchedCityTitle = false;
        for (CityTitle title : GameConstants.getSatelliteCityTitles()) {
            if (satellite.equals(LanguageStrings.formatCityName(LanguageStrings.DYNASTY_THEME_FIRE, title))) {
                matchedCityTitle = true;
                assertTrue(title.isSatellite());
                break;
            }
        }
        assertTrue(matchedCityTitle, "expected satellite city title name, got: " + satellite);
    }

    @Test
    void capitalUsesPrimeCityTitle() {
        assertTrue(GameConstants.CITY_TITLE_PRIME.isCapital());
        assertTrue(GameConstants.CITY_TITLE_PRIME.isSuffix());
        assertEquals("Fire Prime", LanguageStrings.formatCityName(
                LanguageStrings.DYNASTY_THEME_FIRE, GameConstants.CITY_TITLE_PRIME));
    }

    @Test
    void newIsPrefixSatellite() {
        assertTrue(GameConstants.CITY_TITLE_NEW.isPrefix());
        assertTrue(GameConstants.CITY_TITLE_NEW.isSatellite());
        assertEquals("New Fire", LanguageStrings.formatCityName(
                LanguageStrings.DYNASTY_THEME_FIRE, GameConstants.CITY_TITLE_NEW));
    }
}
