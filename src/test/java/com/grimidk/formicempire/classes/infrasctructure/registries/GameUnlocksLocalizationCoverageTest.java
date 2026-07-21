package com.grimidk.formicempire.classes.infrasctructure.registries;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

class GameUnlocksLocalizationCoverageTest {

    @Test
    void upgradesResolveDisplayStrings() {
        LanguageStrings.setLanguage("en");
        assertFalse(GameUnlocks.getUpgrades().isEmpty());
        for (Upgrade upgrade : GameUnlocks.getUpgrades()) {
            assertNotEquals(upgrade.getNameKey(), upgrade.getFlavorName(), upgrade.getNameKey());
            assertNotEquals(upgrade.getNameKey() + "_DESC", upgrade.getDescription(), upgrade.getNameKey());
            assertFalse(upgrade.getFlavorName().isBlank(), upgrade.getNameKey());
            assertFalse(upgrade.getDescription().isBlank(), upgrade.getNameKey());
        }
    }

    @Test
    void buildingsAndAssimilationsResolveDisplayStrings() {
        LanguageStrings.setLanguage("en");
        assertFalse(GameUnlocks.getBuildings().isEmpty());
        for (Building building : GameUnlocks.getBuildings()) {
            assertNotEquals(building.getNameKey(), building.getName(), building.getNameKey());
            assertFalse(building.getName().isBlank(), building.getNameKey());
            assertFalse(building.getDescription().isBlank(), building.getNameKey());
        }
        assertFalse(GameUnlocks.getAssimilations().isEmpty());
        for (Assimilation assimilation : GameUnlocks.getAssimilations()) {
            assertNotEquals(assimilation.getNameKey(), assimilation.getName(), assimilation.getNameKey());
            assertFalse(assimilation.getName().isBlank(), assimilation.getNameKey());
            assertFalse(assimilation.getDescription().isBlank(), assimilation.getNameKey());
        }
    }

    @Test
    void synergiesResolveDisplayStringsAndRequirements() {
        LanguageStrings.setLanguage("en");
        assertFalse(GameUnlocks.getSynergies().isEmpty());
        for (Synergy synergy : GameUnlocks.getSynergies()) {
            assertNotEquals(synergy.getNameKey(), synergy.getName(), synergy.getNameKey());
            assertFalse(synergy.getName().isBlank(), synergy.getNameKey());
            assertFalse(synergy.getDescription().isBlank(), synergy.getNameKey());
            assertTrue(synergy.getRequirementCount() >= 2, synergy.getNameKey());
            assertFalse(synergy.formatRequirementFlavorNames().isBlank(), synergy.getNameKey());
        }
    }
}
