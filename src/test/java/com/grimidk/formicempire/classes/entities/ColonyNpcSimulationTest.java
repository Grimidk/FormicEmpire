package com.grimidk.formicempire.classes.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class ColonyNpcSimulationTest {

    @Test
    void focusedNpcColonyDoesNotRunFullSimulation() {
        Colony npc = new Colony(1, "NPC", false);
        npc.setActive(true);
        assertFalse(npc.runsFullSimulation());
    }

    @Test
    void focusedPlayerColonyRunsFullSimulation() {
        Colony player = new Colony(2, "Player", true);
        player.setActive(true);
        assertTrue(player.runsFullSimulation());
    }

    @Test
    void focusedNpcColonyDailyJobsUseLiteEatingNotPerAntMassacre() {
        Colony npc = new Colony(3, "NPC", false);
        npc.setAge(10);
        npc.setActive(true);
        npc.setMushrooms(0);
        npc.setWater(0);

        for (int i = 0; i < 50; i++) {
            npc.getWorkers().add(new Ant(npc, GameConstants.TYPE_WORKER));
        }

        int workersBefore = npc.getWorkers().size();
        npc.runDailyJobs(
                GameConstants.TEMP_GOOD,
                GameConstants.BIOME_PLAINS,
                null);

        int workersAfter = npc.getWorkers().size();
        int deaths = workersBefore - workersAfter;

        assertTrue(deaths < workersBefore / 2,
                "lite eating should not wipe the colony on zero food/water, got " + deaths + " deaths");
    }
}
