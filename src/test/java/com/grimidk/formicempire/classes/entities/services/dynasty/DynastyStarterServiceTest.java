package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DynastyStarterServiceTest {

    @Test
    public void testInitializeDynastyWithOmniSpecies() {
        // Setup
        DynastyStarterService starterService = new DynastyStarterService();
        AntSpecies omni = GameConstants.SPECIES_OMNI;
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
    public void testInitializeDynastyWithLeafcutterSpecies() {
        // Setup
        DynastyStarterService starterService = new DynastyStarterService();
        AntSpecies leafcutter = GameConstants.SPECIES_LEAFCUTTER;
        Dynasty dynasty = new Dynasty(2, "Leafcutter Dynasty", false, leafcutter);

        // Action
        starterService.initializeDynasty(dynasty);

        // Verification
        assertNotNull(leafcutter.getBaseUpgrades(), "Leafcutter species should have base upgrades");
        for (Upgrade expected : leafcutter.getBaseUpgrades()) {
            assertTrue(dynasty.hasUpgrade(expected), 
                "Dynasty should have unlocked upgrade: " + expected.getName());
        }
    }
}