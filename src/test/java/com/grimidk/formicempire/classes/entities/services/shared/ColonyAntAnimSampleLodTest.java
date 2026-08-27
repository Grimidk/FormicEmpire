package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class ColonyAntAnimSampleLodTest {

    @Test
    void sampleActiveAboveMergeThresholdOnly() {
        assertFalse(ColonyAntAnimSampleLod.isSampleActive(GameNumbers.SPRITE_MERGE_ANT_THRESHOLD));
        assertTrue(ColonyAntAnimSampleLod.isSampleActive(GameNumbers.SPRITE_MERGE_ANT_THRESHOLD + 1));
    }

    @Test
    void hourlyAnimSampleKeyGroupsNearbySameTypeAnts() {
        Colony colony = new Colony(1, "C", true);
        Ant a = new Ant(colony, GameConstants.TYPE_WORKER);
        Ant b = new Ant(colony, GameConstants.TYPE_WORKER);
        a.setPosition(new Point(10, 10));
        b.setPosition(new Point(12, 11));

        long keyA = ColonyAntAnimSampleLod.hourlyAnimSampleKey(a, GameConstants.TYPE_WORKER);
        long keyB = ColonyAntAnimSampleLod.hourlyAnimSampleKey(b, GameConstants.TYPE_WORKER);
        assertEquals(keyA, keyB);
    }

    @Test
    void hourlyAnimSampleKeySeparatesDistantAnts() {
        Colony colony = new Colony(1, "C", true);
        Ant near = new Ant(colony, GameConstants.TYPE_WORKER);
        Ant far = new Ant(colony, GameConstants.TYPE_WORKER);
        near.setPosition(new Point(10, 10));
        far.setPosition(new Point(10 + GameNumbers.SPRITE_MERGE_POSITION_CELL_PX, 10));

        long nearKey = ColonyAntAnimSampleLod.hourlyAnimSampleKey(near, GameConstants.TYPE_WORKER);
        long farKey = ColonyAntAnimSampleLod.hourlyAnimSampleKey(far, GameConstants.TYPE_WORKER);
        assertNotEquals(nearKey, farKey);
    }
}
