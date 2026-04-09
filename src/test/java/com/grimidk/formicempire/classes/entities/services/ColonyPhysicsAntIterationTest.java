package com.grimidk.formicempire.classes.entities.services;

import java.awt.Point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

/**
 * Regression: physics must not use indexed access on {@link java.util.concurrent.CopyOnWriteArrayList}
 * ant lists while {@code updateAntLogic} can remove ants (e.g. nuptial flight).
 */
class ColonyPhysicsAntIterationTest {

    private ColonyPhysicsService physicsService;
    private Colony colony;
    private Dynasty dynasty;

    @BeforeEach
    void setUp() {
        physicsService = new ColonyPhysicsService();
        dynasty = new Dynasty(1, "T", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "C", true);
        colony.setDynasty(dynasty);
        colony.setActive(true);
        colony.setGameAreaDimensions(2000, 2000);
    }

    @Test
    void runPhysics_manySteps_doesNotThrowWithPopulation() {
        for (int a = 0; a < 40; a++) {
            Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
            ant.setDimension(WorldSpaces.OVERWORLD);
            ant.setPosition(new Point(100 + a * 3, 200));
            colony.getWorkers().add(ant);
        }

        for (int step = 0; step < 2000; step++) {
            physicsService.runPhysics(colony, WorldSpaces.OVERWORLD, null, step);
        }
    }
}
