package com.grimidk.formicempire.classes.infrasctructure.i18n;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LocalizationTest {

    @Test
    public void testSpeciesScientificNameLocalization() {
        // Test Omni AntSpecies
        AntSpecies omni = GameConstants.SPECIES_OMNI;
        assertNotNull(omni);
        String scientificName = omni.getScientific();
        assertNotEquals("SPECIES_OMNI_SCIENTIFIC", scientificName, "Scientific name should be localized, not return the key");
        assertEquals("Omniformica Grimunknowni", scientificName);

        // Test Leafcutter AntSpecies
        AntSpecies leafcutter = GameConstants.SPECIES_LEAFCUTTER;
        assertNotNull(leafcutter);
        assertEquals("Atta Cephalotes", leafcutter.getScientific());

        // Test Pharaoh AntSpecies
        AntSpecies pharaoh = GameConstants.SPECIES_PHARAOH;
        assertNotNull(pharaoh);
        assertEquals("Monomorium Pharaonis", pharaoh.getScientific());

        // Test Marauder AntSpecies
        AntSpecies marauder = GameConstants.SPECIES_MARAUDER;
        assertNotNull(marauder);
        assertEquals("Carebara Diversa", marauder.getScientific());
    }

    @Test
    void stripDynastyNameSuffixStripsAnyLocale() {
        LanguageStrings.setLanguage("en");
        assertEquals("Crystal", LanguageStrings.stripDynastyNameSuffix("Crystal Dinastía", LanguageStrings.DYNASTY_TITLE_DYNASTY));
        assertEquals("Crystal", LanguageStrings.stripDynastyNameSuffix("Dinastía Crystal", LanguageStrings.DYNASTY_TITLE_DYNASTY));
        assertEquals("Crystal", LanguageStrings.stripDynastyNameSuffix("Crystal Dynasty", LanguageStrings.DYNASTY_TITLE_DYNASTY));
        assertEquals("Crystal Dynasty", LanguageStrings.formatSaveSlotDisplayName("Crystal", GameConstants.DYNASTY_TITLE_DYNASTY.getId(), 1));
        assertEquals("Crystal Dynasty", LanguageStrings.formatSaveSlotDisplayName("Crystal Dinastía", GameConstants.DYNASTY_TITLE_DYNASTY.getId(), 1));
        assertEquals("Crystal Dynasty", LanguageStrings.formatSaveSlotDisplayName("Dinastía Crystal", GameConstants.DYNASTY_TITLE_DYNASTY.getId(), 1));
    }

    @Test
    void romanceLanguagesPutDynastyTitleBeforeTheme() {
        LanguageStrings.setLanguage("es");
        assertEquals("Unión Oro", LanguageStrings.formatDynastyName(
                LanguageStrings.DYNASTY_THEME_GOLD, GameConstants.DYNASTY_TITLE_UNION));
        assertEquals("Dinastía Crystal", LanguageStrings.formatDynastyName("Crystal", GameConstants.DYNASTY_TITLE_DYNASTY));

        LanguageStrings.setLanguage("fr");
        assertEquals("Union Or", LanguageStrings.formatDynastyName(
                LanguageStrings.DYNASTY_THEME_GOLD, GameConstants.DYNASTY_TITLE_UNION));
        assertEquals("Dynastie Crystal", LanguageStrings.formatDynastyName("Crystal", GameConstants.DYNASTY_TITLE_DYNASTY));

        LanguageStrings.setLanguage("pt");
        assertEquals("União Ouro", LanguageStrings.formatDynastyName(
                LanguageStrings.DYNASTY_THEME_GOLD, GameConstants.DYNASTY_TITLE_UNION));
        assertEquals("Dinastia Crystal", LanguageStrings.formatDynastyName("Crystal", GameConstants.DYNASTY_TITLE_DYNASTY));

        LanguageStrings.setLanguage("en");
        assertEquals("Gold Union", LanguageStrings.formatDynastyName(
                LanguageStrings.DYNASTY_THEME_GOLD, GameConstants.DYNASTY_TITLE_UNION));
        assertEquals("Crystal Dynasty", LanguageStrings.formatDynastyName("Crystal", GameConstants.DYNASTY_TITLE_DYNASTY));
    }

    @Test
    void formicEmpireBrandNameIsNeverTranslated() {
        String brand = LanguageStrings.APP_DISPLAY_NAME;
        assertEquals("Formic Empire", brand);

        for (String lang : new String[] {"en", "es", "fr", "pt"}) {
            LanguageStrings.setLanguage(lang);
            assertEquals(brand, LanguageStrings.get(LanguageStrings.UI_APP_TITLE), lang);
            assertTrue(LanguageStrings.withAppDisplayName(LanguageStrings.HELP_TUTORIAL_TITLE).contains(brand), lang);
            assertTrue(LanguageStrings.withAppDisplayName(LanguageStrings.INTRO_WARNING).contains(brand), lang);
            assertFalse(LanguageStrings.withAppDisplayName(LanguageStrings.HELP_TUTORIAL_TITLE).contains("Imperio Fórmico"), lang);
            assertFalse(LanguageStrings.withAppDisplayName(LanguageStrings.HELP_TUTORIAL_TITLE).contains("Empire Formique"), lang);
            assertFalse(LanguageStrings.withAppDisplayName(LanguageStrings.HELP_TUTORIAL_TITLE).contains("Império Fôrmico"), lang);
        }
        LanguageStrings.setLanguage("en");
    }
}
