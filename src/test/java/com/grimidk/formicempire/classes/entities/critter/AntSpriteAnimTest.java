package com.grimidk.formicempire.classes.entities.critter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

class AntSpriteAnimTest {

    @AfterEach
    void clearRandomTestDoubles() {
        GameRandom.clearTestDoubles();
    }

    @Test
    void legWalkAdvancesOneFramePerMovementUpdate() {
        Colony colony = new Colony(1, "C", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        ant.setSpeed(10f);
        ant.setPosition(new Point(0, 0));
        ant.moveTo(new Point(1000, 0));

        assertEquals(1, ant.getLegFrame());

        for (int expected = 2; expected <= GameNumbers.ANT_LEG_FRAME_COUNT; expected++) {
            ant.updatePosition(1f);
            assertEquals(expected, ant.getLegFrame());
        }
        ant.updatePosition(1f);
        assertEquals(1, ant.getLegFrame());
    }

    @Test
    void idleAntResetsToLegFrameOne() {
        Colony colony = new Colony(1, "C", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        ant.setSpeed(10f);
        ant.setPosition(new Point(0, 0));
        ant.moveTo(new Point(1000, 0));
        ant.updatePosition(1f);
        assertEquals(2, ant.getLegFrame());

        ant.setPosition(new Point(50, 50));
        assertEquals(1, ant.getLegFrame());

        ant.updatePosition(1f);
        assertEquals(1, ant.getLegFrame());
    }

    @Test
    void nuptialDroneUsesFlyingLegFrame() {
        Colony colony = new Colony(1, "C", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_DRONE);
        ant.setNuptial(true);
        assertEquals(GameNumbers.ANT_LEG_FRAME_FLYING, ant.getLegFrame());
        ant.setSpeed(10f);
        ant.moveTo(new Point(1000, 0));
        ant.updatePosition(1f);
        assertEquals(GameNumbers.ANT_LEG_FRAME_FLYING, ant.getLegFrame());
    }

    @Test
    void hourlyRollSnapsJawWhenChanceSucceeds() {
        Colony colony = new Colony(1, "C", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        GameRandom.enqueueTestDoubles(0.0, 1.0);

        ant.rollHourlySpriteAnim();

        assertEquals(2, ant.getJawFrame());
        assertEquals(1, ant.getAntennaFrame());
    }

    @Test
    void hourlyRollTwitchesAntennaWhenChanceSucceeds() {
        Colony colony = new Colony(1, "C", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        GameRandom.enqueueTestDoubles(1.0, 0.0);

        ant.rollHourlySpriteAnim();

        assertEquals(1, ant.getJawFrame());
        assertEquals(2, ant.getAntennaFrame());
    }

    @Test
    void hourlyRollCanSnapJawAndTwitchAntennaIndependently() {
        Colony colony = new Colony(1, "C", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_SOLDIER);
        GameRandom.enqueueTestDoubles(0.0, 0.0, 1.0);

        ant.rollHourlySpriteAnim();

        assertEquals(2, ant.getJawFrame());
        assertEquals(2, ant.getAntennaFrame());
    }

    @Test
    void spriteAnimMinuteClosesJawAndAntennaAfterSnapDuration() {
        Colony colony = new Colony(1, "C", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        GameRandom.enqueueTestDoubles(0.0, 0.0);

        ant.rollHourlySpriteAnim();
        assertEquals(2, ant.getJawFrame());
        assertEquals(2, ant.getAntennaFrame());

        for (int minute = 0; minute < GameNumbers.ANT_SPRITE_SNAP_MINUTES; minute++) {
            ant.tickSpriteAnimMinute();
        }

        assertEquals(1, ant.getJawFrame());
        assertEquals(1, ant.getAntennaFrame());
    }
}
