package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DynastyStarterServiceTest {

    @Test
    public void testInitializeDynastyWithOmniSpecies() {
        // Setup
        DynastyStarterService starterService = new DynastyStarterService();
        Species omni = GameConstants.SPECIES_OMNI;
        Dynasty dynasty = new Dynasty(1, "Test Dynasty", true, omni);

        // Action
        starterService.initializeDynasty(dynasty);

        // Verification
        assertNotNull(omni.getBaseUpgrades(), "Omni species should have base upgrades");
        for (Upgrade expected : omni.getBaseUpgrades()) {
            assertTrue(dynasty.hasUpgrade(expected), 
                "Dynasty should have unlocked upgrade: " + expected.getName());
        }
    }

    @Test
    public void testInitializeDynastyWithLeafSpecies() {
        // Setup
        DynastyStarterService starterService = new DynastyStarterService();
        Species leaf = GameConstants.SPECIES_LEAF;
        Dynasty dynasty = new Dynasty(2, "Leafy Dynasty", false, leaf);

        // Action
        starterService.initializeDynasty(dynasty);

        // Verification
        assertNotNull(leaf.getBaseUpgrades(), "Leaf species should have base upgrades");
        for (Upgrade expected : leaf.getBaseUpgrades()) {
            assertTrue(dynasty.hasUpgrade(expected), 
                "Dynasty should have unlocked upgrade: " + expected.getName());
        }
    }
}