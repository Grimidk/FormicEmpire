package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
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
    public void heatresistAssimilationGivesThirstThreeResistance() {
        for (int i = 0; i < 10; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }

        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HEATRESIST);

        assertEquals(2, statsService.getWaterConsumption(colony));
        assertEquals(80, statsService.getThirstResistance(colony, null));
    }

    @Test
    public void locsenseBoostsSpeedAndConvoySecurityFlat() {
        assertEquals(1f, statsService.getLocsenseSpeedMultiplier(colony), 0.0001f);
        double baseMitigation = statsService.getConvoySecurityMitigationPercent(colony, 5.0, 1.0);
        assertEquals(8.333, baseMitigation, 0.01);

        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_LOCSENSE);

        assertEquals(1.5f, statsService.getLocsenseSpeedMultiplier(colony), 0.0001f);
        assertEquals(baseMitigation + 20.0,
                statsService.getConvoySecurityMitigationPercent(colony, 5.0, 1.0), 0.01);
        assertEquals(100.0, statsService.getConvoySecurityMitigationPercent(colony, 100.0, 1.0), 0.01);
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