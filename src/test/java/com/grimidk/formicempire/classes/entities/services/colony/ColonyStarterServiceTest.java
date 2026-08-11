package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColonyStarterServiceTest {

    private ColonyStarterService starterService;
    private Dynasty dynasty;

    @BeforeEach
    public void setup() {
        starterService = new ColonyStarterService();
        dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_OMNI);
    }

    @Test
    public void testInitializeFirstColony() {
        Colony colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);

        // Action
        starterService.initializeNewColony(colony);

        // Verification
        assertTrue(colony.isCapital(), "First colony should be capital");
        assertEquals(7, colony.getAge(), "First colony should start at age 7");
        assertFalse(colony.getQueens().isEmpty(), "Colony should have at least one queen");
        assertEquals(30, colony.getWorkers().size(), "First colony should have 30 workers after maturation");
        assertEquals(1, colony.getAssignedRoleCount(GameConstants.ROLE_LAYER), "Should have 1 layer");
        assertEquals(15, colony.getAssignedRoleCount(GameConstants.ROLE_NURSE), "Should have 15 nurses");
        assertEquals(3, colony.getAssignedRoleCount(GameConstants.ROLE_FARMER), "Should have 3 farmers");
        assertEquals(12, colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER), "Should have 12 foragers");
        assertEquals(15, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_NURSE));
        assertEquals(3, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_FARMER));
        assertEquals(12, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_FORAGER));
    }

    @Test
    public void testInitializeSatelliteColony() {
        // Add a capital first
        Colony capital = new Colony(1, "Prime", true);
        dynasty.addColony(capital);
        starterService.initializeNewColony(capital);

        // Add a second colony
        Colony satellite = new Colony(2, "Satellite", true);
        dynasty.addColony(satellite);

        // Action
        starterService.initializeNewColony(satellite);

        // Verification
        assertFalse(satellite.isCapital(), "Second colony should not be capital");
        assertEquals(0, satellite.getAge(), "Satellite should start at age 0");
        assertFalse(satellite.getQueens().isEmpty(), "Satellite should have a queen");
        assertEquals(0, satellite.getWorkers().size(), "Satellite should not have workers yet");
    }
}