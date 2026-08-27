package com.grimidk.formicempire.classes.entities.dynasty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class ColonyActiveRoleCountCacheTest {

    private Colony colony;

    @BeforeEach
    void setUp() {
        colony = new Colony(1, "Cache Test", true);
        colony.setActive(true);
        Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
        worker.setRole(GameConstants.ROLE_FORAGER);
        colony.getWorkers().add(worker);
    }

    @Test
    void roleCountCache_survivesMinutelyJobsUntilRoleChanges() {
        colony.runMinutelyJobs();
        colony.runMinutelyJobs();
        assertEquals(1, colony.getActiveRoleCount(GameConstants.ROLE_FORAGER));

        colony.getWorkers().get(0).setRole(GameConstants.ROLE_FARMER);
        assertEquals(0, colony.getActiveRoleCount(GameConstants.ROLE_FORAGER));
        assertEquals(1, colony.getActiveRoleCount(GameConstants.ROLE_FARMER));
    }

    @Test
    void tradeToggleInvalidatesRoleCountCache() {
        Ant worker = colony.getWorkers().get(0);
        worker.setOnTrade(true);
        assertEquals(0, colony.getActiveRoleCount(GameConstants.ROLE_FORAGER));

        worker.setOnTrade(false);
        assertEquals(1, colony.getActiveRoleCount(GameConstants.ROLE_FORAGER));
    }
}
