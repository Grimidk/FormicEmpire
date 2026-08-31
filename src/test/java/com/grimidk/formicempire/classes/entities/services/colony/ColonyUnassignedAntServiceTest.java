package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class ColonyUnassignedAntServiceTest {

    private Colony colony;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
    }

    @Test
    void defaultRoleAntsCountAsUnassigned() {
        Ant soldier = new Ant(colony, GameConstants.TYPE_SOLDIER);
        colony.getSoldiers().add(soldier);
        colony.runRoleAssignment(null);
        assertTrue(ColonyUnassignedAntService.isUnassigned(soldier, colony, null));
    }

    @Test
    void assignedRoleAntsAreNotUnassigned() {
        Ant soldier = new Ant(colony, GameConstants.TYPE_SOLDIER);
        colony.getSoldiers().add(soldier);
        colony.setAssignedRoleCount(GameConstants.ROLE_WARRIOR, 1);
        colony.runRoleAssignment(null);
        assertFalse(ColonyUnassignedAntService.isUnassigned(soldier, colony, null));
    }

    @Test
    void computeUnassignedPercentMatchesRoleDialogLogic() {
        assertEquals(0, ColonyUnassignedAntService.computeUnassignedPercent(0, 0));
        assertEquals(100, ColonyUnassignedAntService.computeUnassignedPercent(10, 0));
        assertEquals(20, ColonyUnassignedAntService.computeUnassignedPercent(10, 8));
        assertTrue(ColonyUnassignedAntService.computeUnassignedPercent(100, 96)
                < GameNumbers.EXHAUSTION_UNASSIGNED_WARN_PCT);
    }
}
