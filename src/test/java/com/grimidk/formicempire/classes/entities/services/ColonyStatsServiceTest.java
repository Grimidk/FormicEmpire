package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColonyStatsServiceTest {

    private ColonyStatsService statsService;
    private Colony colony;
    private Dynasty dynasty;

    @BeforeEach
    public void setup() {
        statsService = new ColonyStatsService();
        dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Test Colony", true);
        dynasty.addColony(colony);
    }

    @Test
    public void testWaterConsumptionBase() {
        for (int i = 0; i < 10; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }

        int expected = 8;
        assertEquals(expected, statsService.getWaterConsumption(colony));
    }

    @Test
    public void testWaterConsumptionWithUpgrade() {
        for (int i = 0; i < 10; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }

        dynasty.unlockUpgrade(GameUnlocks.STAT_THIRST_1);

        int expected = 6;
        assertEquals(expected, statsService.getWaterConsumption(colony));
    }

    @Test
    public void testTotalMushroomConsumption() {
        for (int i = 0; i < 10; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);
        
        assertEquals(10, statsService.getTotalConsumption(colony));
    }
}