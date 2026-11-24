package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColonyAgingTest {

    private Colony colony;
    private Dynasty dynasty;
    private ColonyPopulationService populationService;

    @BeforeEach
    public void setup() {
        populationService = new ColonyPopulationService();
        dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Test Colony", true);
        dynasty.addColony(colony);
    }

    @AfterEach
    public void tearDown() {
        GameRandom.clearTestDoubles();
    }

    @Test
    public void juvenileStagesDoNotDieOfOldAge() {
        Ant egg = new Ant(colony, GameConstants.CLASS_EGG);
        Ant larva = new Ant(colony, GameConstants.CLASS_LARVA);
        Ant pupa = new Ant(colony, GameConstants.CLASS_PUPA);

        egg.setAge(200);
        larva.setAge(200);
        pupa.setAge(200);

        colony.getEggs().add(egg);
        colony.getLarvae().add(larva);
        colony.getPupae().add(pupa);

        populationService.runAging(colony);

        assertEquals(201, egg.getAge());
        assertEquals(201, larva.getAge());
        assertEquals(201, pupa.getAge());

        assertTrue(egg.isAlive());
        assertTrue(larva.isAlive());
        assertTrue(pupa.isAlive());

        assertEquals(1, colony.getEggs().size());
        assertEquals(1, colony.getLarvae().size());
        assertEquals(1, colony.getPupae().size());
        assertEquals(0, colony.getDeadAnts().size());
    }

    @Test
    public void oldAgeDeathChanceScalesLinearlyInDiscreteStepsFromLifespanToDoubleLifespan() {
        assertEquals(0, GameNumbers.oldAgeDeathChancePercent(-1, 90));
        assertEquals(0, GameNumbers.oldAgeDeathChancePercent(0, 90));
        assertEquals(0, GameNumbers.oldAgeDeathChancePercent(89, 90));
        assertEquals(1, GameNumbers.oldAgeDeathChancePercent(90, 90));
        assertEquals(50, GameNumbers.oldAgeDeathChancePercent(135, 90));
        assertEquals(98, GameNumbers.oldAgeDeathChancePercent(179, 90));
        assertEquals(100, GameNumbers.oldAgeDeathChancePercent(180, 90));
        assertEquals(100, GameNumbers.oldAgeDeathChancePercent(250, 90));

        assertEquals(1, GameNumbers.oldAgeDeathChancePercent(1800, 1800));
        assertEquals(50, GameNumbers.oldAgeDeathChancePercent(2700, 1800));
        assertEquals(99, GameNumbers.oldAgeDeathChancePercent(3599, 1800));
        assertEquals(100, GameNumbers.oldAgeDeathChancePercent(3600, 1800));

        assertEquals(100, GameNumbers.oldAgeDeathChancePercent(0, 0));
        assertEquals(0, GameNumbers.oldAgeDeathChancePercent(-1, 0));

        for (int lifespan : new int[] { 30, 90, 180, 1800 }) {
            int prev = 0;
            for (int age = lifespan; age < lifespan * 2; age++) {
                int chance = GameNumbers.oldAgeDeathChancePercent(age, lifespan);
                assertTrue(chance >= 1 && chance <= 99);
                assertTrue(chance >= prev);
                prev = chance;
            }
            assertEquals(100, GameNumbers.oldAgeDeathChancePercent(lifespan * 2, lifespan));
        }

        assertFalse(GameNumbers.shouldCheckOldAgeDeath(89, 90, false));
        for (int age = 90; age <= 185; age++) {
            assertTrue(GameNumbers.shouldCheckOldAgeDeath(age, 90, false));
        }

        assertFalse(GameNumbers.shouldCheckOldAgeDeath(1799, 1800, true));
        assertTrue(GameNumbers.shouldCheckOldAgeDeath(1800, 1800, true));
        for (int age = 1801; age <= 1829; age++) {
            assertFalse(GameNumbers.shouldCheckOldAgeDeath(age, 1800, true));
        }
        assertTrue(GameNumbers.shouldCheckOldAgeDeath(1830, 1800, true));
        assertTrue(GameNumbers.shouldCheckOldAgeDeath(2700, 1800, true));
        assertFalse(GameNumbers.shouldCheckOldAgeDeath(3599, 1800, true));
        assertTrue(GameNumbers.shouldCheckOldAgeDeath(3600, 1800, true));
        assertTrue(GameNumbers.shouldCheckOldAgeDeath(3605, 1800, true));
    }

    @Test
    public void workerAgesAndDiesAtLifespanWithoutUpgrade() {
        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setAge(88);
        colony.getWorkers().add(worker);

        GameRandom.enqueueTestDoubles(0.0);
        populationService.runAging(colony);

        assertEquals(89, worker.getAge());
        assertTrue(worker.isAlive());
        assertEquals(1, colony.getWorkers().size());
        assertEquals(0, colony.getDeadAnts().size());
        GameRandom.clearTestDoubles();

        GameRandom.enqueueTestDoubles(0.01);
        populationService.runAging(colony);

        assertEquals(90, worker.getAge());
        assertTrue(worker.isAlive());
        assertEquals(1, colony.getWorkers().size());
        assertEquals(0, colony.getDeadAnts().size());

        worker.setAge(89);
        GameRandom.enqueueTestDoubles(0.005);
        populationService.runAging(colony);

        assertEquals(90, worker.getAge());
        assertFalse(worker.isAlive());
        assertEquals(0, colony.getWorkers().size());
        assertEquals(1, colony.getDeadAnts().size());
        assertEquals(1, colony.getDeathService().getDeathStatistics().getOrDefault(DeathCause.OLD_AGE, 0));

        Ant midSenior = new Ant(colony, GameConstants.CLASS_WORKER);
        midSenior.setAge(134);
        colony.getWorkers().add(midSenior);
        GameRandom.enqueueTestDoubles(0.50);
        populationService.runAging(colony);

        assertEquals(135, midSenior.getAge());
        assertTrue(midSenior.isAlive());

        midSenior.setAge(134);
        GameRandom.enqueueTestDoubles(0.49);
        populationService.runAging(colony);

        assertEquals(135, midSenior.getAge());
        assertFalse(midSenior.isAlive());

        Ant maxAgeWorker = new Ant(colony, GameConstants.CLASS_WORKER);
        maxAgeWorker.setAge(179);
        colony.getWorkers().add(maxAgeWorker);
        GameRandom.enqueueTestDoubles(0.9999);
        populationService.runAging(colony);

        assertEquals(180, maxAgeWorker.getAge());
        assertFalse(maxAgeWorker.isAlive());
        GameRandom.clearTestDoubles();
    }

    @Test
    public void workerLifespanExtendedWithLongevityUpgrade() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);

        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setAge(89);
        colony.getWorkers().add(worker);

        GameRandom.enqueueTestDoubles(0.0);
        populationService.runAging(colony);

        assertEquals(90, worker.getAge());
        assertTrue(worker.isAlive());
        assertEquals(1, colony.getWorkers().size());
        GameRandom.clearTestDoubles();

        worker.setAge(179);
        GameRandom.enqueueTestDoubles(0.01);
        populationService.runAging(colony);

        assertEquals(180, worker.getAge());
        assertTrue(worker.isAlive());
        assertEquals(1, colony.getWorkers().size());

        worker.setAge(179);
        GameRandom.enqueueTestDoubles(0.005);
        populationService.runAging(colony);

        assertEquals(180, worker.getAge());
        assertFalse(worker.isAlive());

        Ant maxWorker = new Ant(colony, GameConstants.CLASS_WORKER);
        maxWorker.setAge(359);
        colony.getWorkers().add(maxWorker);
        GameRandom.enqueueTestDoubles(0.9999);
        populationService.runAging(colony);

        assertEquals(360, maxWorker.getAge());
        assertFalse(maxWorker.isAlive());
        GameRandom.clearTestDoubles();
    }

    @Test
    public void queenSurvivesPastWorkerLifespan() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);

        Ant queen = new Ant(colony, GameConstants.CLASS_QUEEN);
        queen.setAge(179);
        colony.getQueens().add(queen);

        GameRandom.enqueueTestDoubles(0.0);
        populationService.runAging(colony);

        assertEquals(180, queen.getAge());
        assertTrue(queen.isAlive());
        assertEquals(1, colony.getQueens().size());
        GameRandom.clearTestDoubles();

        queen.setAge(3599);
        GameRandom.enqueueTestDoubles(0.01);
        populationService.runAging(colony);

        assertEquals(3600, queen.getAge());
        assertTrue(queen.isAlive());

        GameRandom.enqueueTestDoubles(0.0);
        populationService.runAging(colony);

        assertEquals(3601, queen.getAge());
        assertTrue(queen.isAlive());

        queen.setAge(3628);
        populationService.runAging(colony);

        assertEquals(3629, queen.getAge());
        assertTrue(queen.isAlive());
        GameRandom.clearTestDoubles();

        queen.setAge(5399);
        GameRandom.enqueueTestDoubles(0.50);
        populationService.runAging(colony);

        assertEquals(5400, queen.getAge());
        assertTrue(queen.isAlive());

        queen.setAge(5399);
        GameRandom.enqueueTestDoubles(0.49);
        populationService.runAging(colony);

        assertEquals(5400, queen.getAge());
        assertFalse(queen.isAlive());

        Ant elderQueen = new Ant(colony, GameConstants.CLASS_QUEEN);
        elderQueen.setAge(3599);
        colony.getQueens().add(elderQueen);
        GameRandom.enqueueTestDoubles(0.005);
        populationService.runAging(colony);

        assertEquals(3600, elderQueen.getAge());
        assertFalse(elderQueen.isAlive());

        Ant maxQueen = new Ant(colony, GameConstants.CLASS_QUEEN);
        maxQueen.setAge(7199);
        colony.getQueens().add(maxQueen);
        GameRandom.enqueueTestDoubles(0.9999);
        populationService.runAging(colony);

        assertEquals(7200, maxQueen.getAge());
        assertFalse(maxQueen.isAlive());
        GameRandom.clearTestDoubles();
    }

    @Test
    public void workerLifespanExtendedWithLongevity2And3() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY_2);

        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setAge(179);
        colony.getWorkers().add(worker);

        GameRandom.enqueueTestDoubles(0.0);
        populationService.runAging(colony);

        assertEquals(180, worker.getAge());
        assertTrue(worker.isAlive());
        GameRandom.clearTestDoubles();

        worker.setAge(359);
        GameRandom.enqueueTestDoubles(0.01);
        populationService.runAging(colony);

        assertEquals(360, worker.getAge());
        assertTrue(worker.isAlive());

        worker.setAge(719);
        GameRandom.enqueueTestDoubles(0.9999);
        populationService.runAging(colony);

        assertEquals(720, worker.getAge());
        assertFalse(worker.isAlive());
        GameRandom.clearTestDoubles();

        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY_3);
        Ant worker2 = new Ant(colony, GameConstants.CLASS_WORKER);
        worker2.setAge(359);
        colony.getWorkers().add(worker2);

        GameRandom.enqueueTestDoubles(0.0);
        populationService.runAging(colony);

        assertEquals(360, worker2.getAge());
        assertTrue(worker2.isAlive());
        GameRandom.clearTestDoubles();

        worker2.setAge(719);
        GameRandom.enqueueTestDoubles(0.01);
        populationService.runAging(colony);

        assertEquals(720, worker2.getAge());
        assertTrue(worker2.isAlive());

        worker2.setAge(1439);
        GameRandom.enqueueTestDoubles(0.9999);
        populationService.runAging(colony);

        assertEquals(1440, worker2.getAge());
        assertFalse(worker2.isAlive());
        GameRandom.clearTestDoubles();
    }
}
