package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.awt.Rectangle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

class ColonyPhysicsAnimLodTest {

    private final ColonyPhysicsService physicsService = new ColonyPhysicsService();

    @AfterEach
    void clearRandomTestDoubles() {
        GameRandom.clearTestDoubles();
    }

    @Test
    void minuteTickSkipsOffViewportAntsWithoutOpenAnim() {
        Colony colony = new Colony(1, "C", true);
        Ant inView = new Ant(colony, GameConstants.TYPE_WORKER);
        Ant outOfView = new Ant(colony, GameConstants.TYPE_WORKER);
        inView.setDimension(WorldSpaces.OVERWORLD);
        outOfView.setDimension(WorldSpaces.OVERWORLD);
        inView.setPosition(new Point(50, 50));
        outOfView.setPosition(new Point(500, 500));
        colony.getWorkers().add(inView);
        colony.getWorkers().add(outOfView);

        GameRandom.enqueueTestDoubles(0.0, 1.0);
        inView.rollHourlySpriteAnim();
        assertEquals(2, inView.getJawFrame());
        assertEquals(1, outOfView.getJawFrame());

        physicsService.tickAntSpriteAnimMinutes(
                colony,
                WorldSpaces.OVERWORLD,
                new Rectangle(0, 0, 120, 120));

        assertEquals(2, inView.getJawFrame());
        assertEquals(1, outOfView.getJawFrame());
    }

    @Test
    void minuteTickStillAdvancesOpenAnimOffViewport() {
        Colony colony = new Colony(1, "C", true);
        Ant outOfView = new Ant(colony, GameConstants.TYPE_WORKER);
        outOfView.setDimension(WorldSpaces.OVERWORLD);
        outOfView.setPosition(new Point(900, 900));
        colony.getWorkers().add(outOfView);

        GameRandom.enqueueTestDoubles(0.0, 1.0);
        outOfView.rollHourlySpriteAnim();
        assertEquals(2, outOfView.getJawFrame());

        for (int minute = 0; minute < GameNumbers.ANT_SPRITE_SNAP_MINUTES; minute++) {
            physicsService.tickAntSpriteAnimMinutes(
                    colony,
                    WorldSpaces.OVERWORLD,
                    new Rectangle(0, 0, 120, 120));
        }

        assertEquals(1, outOfView.getJawFrame());
    }

    @Test
    void hourlyRollSamplesBucketAnimAtHighPopulation() {
        Colony colony = new Colony(1, "C", true);
        for (int i = 0; i < GameNumbers.SPRITE_MERGE_ANT_THRESHOLD + 2; i++) {
            Ant filler = new Ant(colony, GameConstants.TYPE_EGG);
            colony.getEggs().add(filler);
        }

        Ant a = new Ant(colony, GameConstants.TYPE_WORKER);
        Ant b = new Ant(colony, GameConstants.TYPE_WORKER);
        a.setDimension(WorldSpaces.OVERWORLD);
        b.setDimension(WorldSpaces.OVERWORLD);
        a.setPosition(new Point(40, 40));
        b.setPosition(new Point(42, 41));
        colony.getWorkers().add(a);
        colony.getWorkers().add(b);

        GameRandom.enqueueTestDoubles(0.0, 1.0, 1.0, 1.0);
        physicsService.rollAntSpriteAnimHourly(
                colony,
                WorldSpaces.OVERWORLD,
                new Rectangle(0, 0, 200, 200));

        assertEquals(a.getJawFrame(), b.getJawFrame());
        assertEquals(a.getAntennaFrame(), b.getAntennaFrame());
        assertEquals(2, a.getJawFrame());
    }
}
