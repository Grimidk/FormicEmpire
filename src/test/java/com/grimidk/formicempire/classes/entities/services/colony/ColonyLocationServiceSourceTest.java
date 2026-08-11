package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonyLocationServiceSourceTest {

    private Colony colony;
    private ColonyLocationService locationService;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        colony = new Colony(1, "Test", true);
        dynasty.addColony(colony);
        colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 1);
        locationService = colony.getLocationService();
    }

    @Test
    void depletedSourceIsNotActive() {
        ResourceSource source = new ResourceSource(
                GameConstants.RESOURCE_PLANT, ResourceType.SOURCE_QTY_SMALL, 10, 20);
        locationService.addSource(colony, source);
        assertTrue(locationService.isActiveSource(source));

        source.decreaseQuantity(ResourceType.SOURCE_QTY_SMALL);
        locationService.removeSource(colony, source);
        assertFalse(locationService.isActiveSource(source));
    }

    @Test
    void findNearestIgnoresDepletedSources() {
        ResourceSource source = new ResourceSource(
                GameConstants.RESOURCE_PLANT, ResourceType.SOURCE_QTY_SMALL, 10, 20);
        locationService.addSource(colony, source);
        source.decreaseQuantity(ResourceType.SOURCE_QTY_SMALL);
        locationService.removeSource(colony, source);

        Ant forager = new Ant(colony, GameConstants.TYPE_WORKER);
        forager.setRole(GameConstants.ROLE_FORAGER);
        assertNull(locationService.findNearestRelevantSource(colony, forager));
    }
}
