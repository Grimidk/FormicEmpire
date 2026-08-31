package com.grimidk.formicempire.classes.entities.critter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class CritterSpriteAnimTest {

    @Test
    void enemyCrittersUseFourLegFrames() {
        assertEquals(GameNumbers.CRITTER_ENEMY_LEG_FRAME_COUNT, GameConstants.TYPE_COCKROACH.getLegFrameCount());
        assertEquals(2, GameConstants.TYPE_COCKROACH.getAntennaFrameCount());
        assertEquals(GameNumbers.CRITTER_ENEMY_LEG_FRAME_COUNT, GameConstants.TYPE_BOMBARDIER_BEETLE.getLegFrameCount());
        assertEquals(GameNumbers.CRITTER_ENEMY_LEG_FRAME_COUNT, GameConstants.TYPE_ANT_LION.getLegFrameCount());
        assertEquals(GameNumbers.CRITTER_ENEMY_LEG_FRAME_COUNT, GameConstants.TYPE_SPIDER.getLegFrameCount());
        assertEquals(GameNumbers.CRITTER_ENEMY_LEG_FRAME_COUNT, GameConstants.TYPE_TARANTULA.getLegFrameCount());
        assertEquals(0, GameConstants.TYPE_SPIDER.getAntennaFrameCount());
    }

    @Test
    void legWalkAdvancesOneFramePerMovementUpdate() {
        Critter critter = new Critter(GameConstants.TYPE_APHID);
        critter.setSpeed(10f);
        critter.setPosition(new Point(0, 0));
        critter.moveTo(new Point(1000, 0));

        assertEquals(1, critter.getLegFrame());

        for (int expected = 2; expected <= GameNumbers.ANT_LEG_FRAME_COUNT; expected++) {
            critter.updatePosition(1f);
            assertEquals(expected, critter.getLegFrame());
        }
        critter.updatePosition(1f);
        assertEquals(1, critter.getLegFrame());
    }

    @Test
    void idleCritterResetsToLegFrameOne() {
        Critter critter = new Critter(GameConstants.TYPE_DERMESTID);
        critter.setSpeed(10f);
        critter.setPosition(new Point(0, 0));
        critter.moveTo(new Point(1000, 0));
        critter.updatePosition(1f);
        assertEquals(2, critter.getLegFrame());

        critter.setPosition(new Point(50, 50));
        assertEquals(1, critter.getLegFrame());

        critter.updatePosition(1f);
        assertEquals(1, critter.getLegFrame());
    }

    @Test
    void parasiticMiteDoesNotAdvanceLegFrame() {
        Critter mite = new Critter(GameConstants.TYPE_PARASITIC_MITE);
        mite.setSpeed(10f);
        mite.setPosition(new Point(0, 0));
        mite.moveTo(new Point(1000, 0));
        assertEquals(1, mite.getLegFrame());
        mite.updatePosition(1f);
        assertEquals(1, mite.getLegFrame());
        mite.updatePosition(1f);
        assertEquals(1, mite.getLegFrame());
    }
}
