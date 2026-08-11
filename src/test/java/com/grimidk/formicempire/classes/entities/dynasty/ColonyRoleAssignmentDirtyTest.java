package com.grimidk.formicempire.classes.entities.dynasty;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class ColonyRoleAssignmentDirtyTest {

    private Colony colony;
    private Dynasty dynasty;

    @BeforeEach
    void setUp() {
        dynasty = new Dynasty(1, "DirtyDynasty", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "DirtyColony", true);
        colony.setDynasty(dynasty);
        colony.setActive(true);
        for (int i = 0; i < 10; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 4);
        colony.runRoleAssignment(null);
    }

    @Test
    void runRoleAssignmentClearsDirtyFlag() {
        assertFalse(colony.isRoleAssignmentDirty());
    }

    @Test
    void runRoleAssignmentIfNeededSkipsWhenClean() {
        assertFalse(colony.isRoleAssignmentDirty());
        colony.runRoleAssignmentIfNeeded(null);
        assertFalse(colony.isRoleAssignmentDirty());
    }

    @Test
    void changingAssignedRoleCountMarksDirtyAndReassigns() {
        colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 7);
        assertTrue(colony.isRoleAssignmentDirty());
        colony.runRoleAssignmentIfNeeded(null);
        assertFalse(colony.isRoleAssignmentDirty());
    }

    @Test
    void sameAssignedRoleCountDoesNotMarkDirty() {
        colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 4);
        assertFalse(colony.isRoleAssignmentDirty());
    }

    @Test
    void populationChangeTriggersReassignmentViaPopKey() {
        assertFalse(colony.isRoleAssignmentDirty());
        colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        colony.runRoleAssignmentIfNeeded(null);
        assertFalse(colony.isRoleAssignmentDirty());
    }
}
