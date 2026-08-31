package com.grimidk.formicempire.classes.entities.dynasty;

import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyUpgradeLoadTest {

    @Test
    void upgradeIdsAreUnique() {
        Set<Integer> seen = new HashSet<>();
        for (var upgrade : GameUnlocks.getUpgrades()) {
            assertTrue(seen.add(upgrade.getId()), "Duplicate upgrade id: " + upgrade.getId());
        }
    }

    @Test
    void loadMigratesCollision528HuntsUnlockForScout() {
        Savefile.SavedDynasty saved = new Savefile.SavedDynasty();
        saved.id = 1;
        saved.name = "Test";
        saved.isPlayer = true;
        saved.speciesId = GameConstants.SPECIES_OMNI.getId();
        saved.unlockedUpgradeIds = new ArrayList<>(List.of(
                GameUnlocks.ROLE_SCOUT.getId(),
                528));

        Dynasty dynasty = new Dynasty(saved);

        assertTrue(dynasty.hasUpgrade(GameUnlocks.ABILITY_HUNTS));
        assertFalse(dynasty.hasUpgrade(GameUnlocks.ABILITY_RAFTING_2));
    }

    @Test
    void loadBackfillsHuntsWhenScoutUnlockedWithoutHuntsId() {
        Savefile.SavedDynasty saved = new Savefile.SavedDynasty();
        saved.id = 2;
        saved.name = "ScoutOnly";
        saved.isPlayer = true;
        saved.speciesId = GameConstants.SPECIES_OMNI.getId();
        saved.unlockedUpgradeIds = new ArrayList<>(List.of(GameUnlocks.ROLE_SCOUT.getId()));

        Dynasty dynasty = new Dynasty(saved);

        assertTrue(dynasty.hasUpgrade(GameUnlocks.ABILITY_HUNTS));
    }

    @Test
    void loadKeepsRafting2WhenAssimilationPrerequisiteMet() {
        Savefile.SavedDynasty saved = new Savefile.SavedDynasty();
        saved.id = 3;
        saved.name = "Rafting";
        saved.isPlayer = true;
        saved.speciesId = GameConstants.SPECIES_OMNI.getId();
        saved.unlockedUpgradeIds = new ArrayList<>(List.of(
                GameUnlocks.ASSIMILATED_RAFTING.getId(),
                528));

        Dynasty dynasty = new Dynasty(saved);

        assertTrue(dynasty.hasUpgrade(GameUnlocks.ABILITY_RAFTING_2));
        assertFalse(dynasty.hasUpgrade(GameUnlocks.ABILITY_HUNTS));
    }
}
