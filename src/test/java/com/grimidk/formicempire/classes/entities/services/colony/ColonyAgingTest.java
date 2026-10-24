package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
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
    public void workerAgesAndDiesAtLifespanWithoutUpgrade() {
        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setAge(88);
        colony.getWorkers().add(worker);

        populationService.runAging(colony);

        assertEquals(89, worker.getAge());
        assertTrue(worker.isAlive());
        assertEquals(1, colony.getWorkers().size());
        assertEquals(0, colony.getDeadAnts().size());

        populationService.runAging(colony);

        assertEquals(90, worker.getAge());
        assertFalse(worker.isAlive());
        assertEquals(0, colony.getWorkers().size());
        assertEquals(1, colony.getDeadAnts().size());
        assertEquals(1, colony.getDeathService().getDeathStatistics().getOrDefault(DeathCause.OLD_AGE, 0));
    }

    @Test
    public void workerLifespanExtendedWithLongevityUpgrade() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);

        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setAge(89);
        colony.getWorkers().add(worker);

        populationService.runAging(colony);

        assertEquals(90, worker.getAge());
        assertTrue(worker.isAlive());
        assertEquals(1, colony.getWorkers().size());

        worker.setAge(179);
        populationService.runAging(colony);

        assertEquals(180, worker.getAge());
        assertFalse(worker.isAlive());
        assertEquals(0, colony.getWorkers().size());
        assertEquals(1, colony.getDeadAnts().size());
    }

    @Test
    public void queenSurvivesPastWorkerLifespan() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);

        Ant queen = new Ant(colony, GameConstants.CLASS_QUEEN);
        queen.setAge(179);
        colony.getQueens().add(queen);

        populationService.runAging(colony);

        assertEquals(180, queen.getAge());
        assertTrue(queen.isAlive());
        assertEquals(1, colony.getQueens().size());

        queen.setAge(3599);
        populationService.runAging(colony);

        assertEquals(3600, queen.getAge());
        assertFalse(queen.isAlive());
        assertEquals(0, colony.getQueens().size());
        assertEquals(1, colony.getDeadAnts().size());
    }

    @Test
    public void workerLifespanExtendedWithLongevity2And3() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY_2);

        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setAge(179);
        colony.getWorkers().add(worker);

        populationService.runAging(colony);

        assertEquals(180, worker.getAge());
        assertTrue(worker.isAlive());

        worker.setAge(359);
        populationService.runAging(colony);

        assertEquals(360, worker.getAge());
        assertFalse(worker.isAlive());

        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY_3);
        Ant worker2 = new Ant(colony, GameConstants.CLASS_WORKER);
        worker2.setAge(359);
        colony.getWorkers().add(worker2);

        populationService.runAging(colony);

        assertEquals(360, worker2.getAge());
        assertTrue(worker2.isAlive());

        worker2.setAge(719);
        populationService.runAging(colony);

        assertEquals(720, worker2.getAge());
        assertFalse(worker2.isAlive());
    }
}
