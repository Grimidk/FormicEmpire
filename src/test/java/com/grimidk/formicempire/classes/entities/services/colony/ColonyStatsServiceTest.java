package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
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
            colony.getWorkers().add(new Ant(colony, GameConstants.CLASS_WORKER));
        }

        int expected = 8;
        assertEquals(expected, statsService.getWaterConsumption(colony));
    }

    @Test
    public void testWaterConsumptionWithUpgrade() {
        for (int i = 0; i < 10; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.CLASS_WORKER));
        }

        dynasty.unlockUpgrade(GameUnlocks.STAT_THIRST_1);

        int expected = 6;
        assertEquals(expected, statsService.getWaterConsumption(colony));
    }

    @Test
    public void heatresistAssimilationGivesThirstThreeResistance() {
        assertEquals(0, statsService.getBaseEvasionChance(colony));
        for (int i = 0; i < 10; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.CLASS_WORKER));
        }

        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HEATRESIST);

        assertEquals(2, statsService.getWaterConsumption(colony));
        assertEquals(80, statsService.getThirstResistance(colony, null));
        assertEquals(0, statsService.getBaseDefense(colony));
        assertEquals(15, statsService.getBaseEvasionChance(colony));
        assertEquals(1.4f, statsService.getHeatresistSpeedMultiplier(colony), 0.0001f);

        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        assertEquals(15f, worker.getEvasionChance(), 0.0001f);

        assertEquals(0.85f, GameNumbers.applyEvasionToHitChance(1.0f, 15f), 0.0001f);
        assertEquals(0.05f, GameNumbers.applyEvasionToHitChance(0.10f, 15f), 0.0001f);
        assertEquals(0.05f, GameNumbers.applyEvasionToHitChance(0.50f, 60f), 0.0001f);
        assertEquals(0.02f, GameNumbers.applyEvasionToHitChance(0.02f, 15f), 0.0001f);
        assertEquals(0.80f, GameNumbers.applyEvasionToHitChance(0.80f, 0f), 0.0001f);
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
            colony.getWorkers().add(new Ant(colony, GameConstants.CLASS_WORKER));
        }
        
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);
        
        assertEquals(10, statsService.getTotalConsumption(colony));
    }

    @Test
    public void testLifespanBaseAndUpgrades() {
        assertEquals(GameNumbers.LIFESPAN_NO_UPGRADE_DAYS, statsService.getBaseLifespan(colony));
        assertEquals(90, statsService.getAntLifespan(colony, GameConstants.CLASS_WORKER));
        assertEquals(90, statsService.getAntLifespan(colony, GameConstants.CLASS_SOLDIER));
        assertEquals(90, statsService.getAntLifespan(colony, GameConstants.CLASS_MAJOR));
        assertEquals(30, statsService.getAntLifespan(colony, GameConstants.CLASS_DRONE));
        assertEquals(180, statsService.getAntLifespan(colony, GameConstants.CLASS_PRINCESS));
        assertEquals(1800, statsService.getAntLifespan(colony, GameConstants.CLASS_QUEEN));

        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);

        assertEquals(GameNumbers.LIFESPAN_BASE_DAYS, statsService.getBaseLifespan(colony));
        assertEquals(180, statsService.getAntLifespan(colony, GameConstants.CLASS_WORKER));
        assertEquals(180, statsService.getAntLifespan(colony, GameConstants.CLASS_SOLDIER));
        assertEquals(180, statsService.getAntLifespan(colony, GameConstants.CLASS_MAJOR));
        assertEquals(60, statsService.getAntLifespan(colony, GameConstants.CLASS_DRONE));
        assertEquals(360, statsService.getAntLifespan(colony, GameConstants.CLASS_PRINCESS));
        assertEquals(3600, statsService.getAntLifespan(colony, GameConstants.CLASS_QUEEN));

        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY_2);

        assertEquals(GameNumbers.LIFESPAN_UPGRADE_2_DAYS, statsService.getBaseLifespan(colony));
        assertEquals(360, statsService.getAntLifespan(colony, GameConstants.CLASS_WORKER));
        assertEquals(360, statsService.getAntLifespan(colony, GameConstants.CLASS_SOLDIER));
        assertEquals(360, statsService.getAntLifespan(colony, GameConstants.CLASS_MAJOR));
        assertEquals(120, statsService.getAntLifespan(colony, GameConstants.CLASS_DRONE));
        assertEquals(720, statsService.getAntLifespan(colony, GameConstants.CLASS_PRINCESS));
        assertEquals(7200, statsService.getAntLifespan(colony, GameConstants.CLASS_QUEEN));

        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY_3);

        assertEquals(GameNumbers.LIFESPAN_UPGRADE_3_DAYS, statsService.getBaseLifespan(colony));
        assertEquals(720, statsService.getAntLifespan(colony, GameConstants.CLASS_WORKER));
        assertEquals(720, statsService.getAntLifespan(colony, GameConstants.CLASS_SOLDIER));
        assertEquals(720, statsService.getAntLifespan(colony, GameConstants.CLASS_MAJOR));
        assertEquals(240, statsService.getAntLifespan(colony, GameConstants.CLASS_DRONE));
        assertEquals(1440, statsService.getAntLifespan(colony, GameConstants.CLASS_PRINCESS));
        assertEquals(14400, statsService.getAntLifespan(colony, GameConstants.CLASS_QUEEN));
    }

    @Test
    public void testAverageAntAge() {
        assertEquals(0.0, statsService.getAverageAntAge(colony, GameConstants.CLASS_WORKER));
        assertEquals(0.0, statsService.getAverageAntAge(colony));
        assertEquals(0.0, dynasty.getAverageAntAge(GameConstants.CLASS_WORKER));
        assertEquals(0.0, dynasty.getAverageAntAge());

        Ant w1 = new Ant(colony, GameConstants.CLASS_WORKER);
        w1.setAge(10);
        Ant w2 = new Ant(colony, GameConstants.CLASS_WORKER);
        w2.setAge(20);
        colony.getWorkers().add(w1);
        colony.getWorkers().add(w2);

        Ant s1 = new Ant(colony, GameConstants.CLASS_SOLDIER);
        s1.setAge(60);
        colony.getSoldiers().add(s1);

        assertEquals(15.0, statsService.getAverageAntAge(colony, GameConstants.CLASS_WORKER), 0.0001);
        assertEquals(60.0, statsService.getAverageAntAge(colony, GameConstants.CLASS_SOLDIER), 0.0001);
        assertEquals(0.0, statsService.getAverageAntAge(colony, GameConstants.CLASS_MAJOR), 0.0001);
        assertEquals(30.0, statsService.getAverageAntAge(colony), 0.0001);

        Colony colony2 = new Colony(2, "Second Colony", true);
        dynasty.addColony(colony2);
        Ant w3 = new Ant(colony2, GameConstants.CLASS_WORKER);
        w3.setAge(30);
        colony2.getWorkers().add(w3);

        assertEquals(20.0, dynasty.getAverageAntAge(GameConstants.CLASS_WORKER), 0.0001);
        assertEquals(30.0, dynasty.getAverageAntAge(), 0.0001);
    }
}