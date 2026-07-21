package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.awt.Rectangle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIf;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.spatial.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

class ColonyPhysicsViewportPerformanceTest {

    private ColonyPhysicsService physicsService;
    private Colony colony;
    private Dynasty dynasty;

    @BeforeEach
    void setUp() {
        physicsService = new ColonyPhysicsService();
        dynasty = new Dynasty(1, "BenchDynasty", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "BenchColony", true);
        colony.setDynasty(dynasty);
        colony.setActive(true);
        colony.setGameAreaDimensions(4000, 4000);
    }

    private void spawnWorkers(int count, int baseY) {
        for (int i = 0; i < count; i++) {
            Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
            ant.setDimension(WorldSpaces.OVERWORLD);
            int x = 20 + (i % 2000);
            int y = baseY + (i / 2000);
            ant.setPosition(new Point(x, y));
            colony.getWorkers().add(ant);
        }
    }

    private long timePhysicsMs(Dimension dim, Rectangle viewport, long steps, int iterations) {
        long t0 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            for (long s = 0; s < steps; s++) {
                physicsService.runPhysics(colony, dim, viewport, s + i * steps);
            }
        }
        return (System.nanoTime() - t0) / 1_000_000L;
    }

    @Test
    @Timeout(120)
    void physics_10k_workers_reportsViewportVsNoViewportTimings() {
        final int n = 10_000;
        spawnWorkers(n, 3000);

        Rectangle smallVp = new Rectangle(0, 0, 800, 600);

        timePhysicsMs(WorldSpaces.OVERWORLD, smallVp, 1, 2);
        timePhysicsMs(WorldSpaces.OVERWORLD, null, 1, 2);

        long tLod = timePhysicsMs(WorldSpaces.OVERWORLD, smallVp, 1, 24);
        long tNoLod = timePhysicsMs(WorldSpaces.OVERWORLD, null, 1, 24);

        System.out.println("[ColonyPhysicsViewportPerformance] 10k ants (mostly off-screen for smallVp), 24 physics calls each: "
            + "viewportLod(small)=" + tLod + "ms noLod(null)=" + tNoLod + "ms");

        assertTrue(tLod < 90_000 && tNoLod < 90_000, "Smoke: both modes complete in reasonable time");
    }

    @Test
    @Timeout(120)
    @EnabledIf("run100kEnabled")
    void physics_100k_workers_reportsTiming() {
        final int n = 100_000;
        spawnWorkers(n, 5000);

        Rectangle smallVp = new Rectangle(0, 0, 800, 600);
        long tSmall = timePhysicsMs(WorldSpaces.OVERWORLD, smallVp, 1, 2);
        System.out.println("[ColonyPhysicsViewportPerformance] 100k ants, 2 physics calls, smallVp=" + tSmall + "ms");

        assertTrue(tSmall < 120_000L, "Sanity: completes under 120s with timeout guard");
    }

    static boolean run100kEnabled() {
        return Boolean.parseBoolean(System.getProperty("formic.run100k", "false"));
    }
}
