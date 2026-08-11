package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonyLabourWorkingAntsTest {

    private Colony colony;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "LabourDynasty", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "LabourColony", true);
        colony.setDynasty(dynasty);
        colony.setActive(true);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_HUNTER);

        for (int i = 0; i < 5; i++) {
            Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
            worker.setRole(GameConstants.ROLE_FORAGER);
            colony.getWorkers().add(worker);
        }
        for (int i = 0; i < 3; i++) {
            Ant soldier = new Ant(colony, GameConstants.TYPE_SOLDIER);
            soldier.setRole(GameConstants.ROLE_HUNTER);
            colony.getSoldiers().add(soldier);
        }
        Ant idle = new Ant(colony, GameConstants.TYPE_WORKER);
        idle.setRole(GameConstants.ROLE_FARMER);
        colony.getWorkers().add(idle);
    }

    @Test
    void collectingUsesRoleScopedWorkersWithoutThrowing() {
        colony.getLabourService().runCollecting(colony);
        assertEquals(5, colony.getActiveRoleCount(GameConstants.ROLE_FORAGER));
        assertEquals(3, colony.getActiveRoleCount(GameConstants.ROLE_HUNTER));
    }
}
