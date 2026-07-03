package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonySourceServiceTest {

    private Colony colony;
    private ColonySourceService sourceService;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Test", true);
        dynasty.addColony(colony);
        colony.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        sourceService = colony.getSourceService();
    }

    @Test
    void addSourceRespectsCapacity() {
        ResourceSource first = new ResourceSource(GameConstants.RESOURCE_PLANT, 100, 10, 20);
        ResourceSource second = new ResourceSource(GameConstants.RESOURCE_PLANT, 100, 30, 40);
        assertTrue(sourceService.addSource(colony, first));
        assertFalse(sourceService.addSource(colony, second));
        assertEquals(1, sourceService.getSourcesByType(GameConstants.RESOURCE_PLANT).size());
    }

    @Test
    void gatherDepletesAndRemovesSource() {
        ResourceSource source = new ResourceSource(GameConstants.RESOURCE_WATER, 50, 15, 25);
        sourceService.addSource(colony, source);
        int gathered = sourceService.gatherFromSource(colony, source, 50);
        assertEquals(50, gathered);
        assertFalse(sourceService.isActiveSource(source));
        assertEquals(0, sourceService.getTotalQuantityAvailable(GameConstants.RESOURCE_WATER));
    }

    @Test
    void gatheringEfficiencyFallsOffWithDistance() {
        double near = ColonySourceService.GatheringMath.gatheringEfficiency(0, 0, 10, 10, 100f);
        double far = ColonySourceService.GatheringMath.gatheringEfficiency(0, 0, 500, 500, 100f);
        assertTrue(near > far);
        assertTrue(far >= GameConstants.GATHER_MIN_EFFICIENCY);
    }
}
