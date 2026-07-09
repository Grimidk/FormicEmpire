package com.grimidk.formicempire.classes.infrasctructure.i18n;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.constants.misc.Species;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LocalizationTest {

    @Test
    public void testSpeciesScientificNameLocalization() {
        // Test Omni Species
        Species omni = GameConstants.SPECIES_OMNI;
        assertNotNull(omni);
        String scientificName = omni.getScientific();
        assertNotEquals("SPECIES_OMNI_SCIENTIFIC", scientificName, "Scientific name should be localized, not return the key");
        assertEquals("Omniformica Grimunknowni", scientificName);

        // Test Leaf Species
        Species leaf = GameConstants.SPECIES_LEAF;
        assertNotNull(leaf);
        assertEquals("Atta Cephalotes", leaf.getScientific());

        // Test Pharaoh Species
        Species pharaoh = GameConstants.SPECIES_PHARAOH;
        assertNotNull(pharaoh);
        assertEquals("Monomorium Pharaonis", pharaoh.getScientific());

        // Test Marauder Species
        Species marauder = GameConstants.SPECIES_MARAUDER;
        assertNotNull(marauder);
        assertEquals("Carebara Diversa", marauder.getScientific());
    }

    @Test
    void stripDynastyNameSuffixStripsAnyLocale() {
        LanguageStrings.setLanguage("en");
        assertEquals("Crystal", LanguageStrings.stripDynastyNameSuffix("Crystal Dinastía", LanguageStrings.DYNASTY_TITLE_DYNASTY));
        assertEquals("Crystal", LanguageStrings.stripDynastyNameSuffix("Crystal Dynasty", LanguageStrings.DYNASTY_TITLE_DYNASTY));
        assertEquals("Crystal Dynasty", LanguageStrings.formatSaveSlotDisplayName("Crystal", GameConstants.DYNASTY_TITLE_DYNASTY.getId(), 1));
        assertEquals("Crystal Dynasty", LanguageStrings.formatSaveSlotDisplayName("Crystal Dinastía", GameConstants.DYNASTY_TITLE_DYNASTY.getId(), 1));
    }
}
