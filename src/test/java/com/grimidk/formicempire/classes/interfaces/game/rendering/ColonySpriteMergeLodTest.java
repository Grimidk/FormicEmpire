package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.Point;
import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.critter.Critter;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

class ColonySpriteMergeLodTest {

    @Test
    void isMergeActive_respectsThreshold() {
        assertFalse(ColonySpriteMergeLod.isMergeActive(GameNumbers.SPRITE_MERGE_ANT_THRESHOLD));
        assertFalse(ColonySpriteMergeLod.isMergeActive(500));
        assertTrue(ColonySpriteMergeLod.isMergeActive(GameNumbers.SPRITE_MERGE_ANT_THRESHOLD + 1));
    }

    @Test
    void maxGroupSize_scalesAtLargePopulation() {
        assertEquals(GameNumbers.SPRITE_MERGE_GROUP_SIZE,
                ColonySpriteMergeLod.maxGroupSize(GameNumbers.SPRITE_MERGE_ANT_THRESHOLD + 1));
        assertEquals(GameNumbers.SPRITE_MERGE_LARGE_GROUP_SIZE,
                ColonySpriteMergeLod.maxGroupSize(GameNumbers.SPRITE_MERGE_ANT_LARGE_THRESHOLD + 1));
    }

    @Test
    void spatialCell_groupsWithinTolerance() {
        int cellA = ColonySpriteMergeLod.spatialCell(100);
        int cellB = ColonySpriteMergeLod.spatialCell(110);
        int cellC = ColonySpriteMergeLod.spatialCell(118);
        assertEquals(cellA, cellB);
        assertEquals(cellA, cellC);
        assertEquals(ColonySpriteMergeLod.spatialCell(120), cellA + 1);
    }

    @Test
    void antBucket_splitsRoleSubtypeAndPosition() {
        Colony colony = new Colony(1, "c", true);
        Dynasty dynasty = new Dynasty(1, "d", true, GameConstants.SPECIES_OMNI);
        colony.setDynasty(dynasty);

        Ant workerA = new Ant(colony, GameConstants.TYPE_WORKER);
        workerA.setPosition(new Point(100, 100));
        workerA.setRole(GameConstants.ROLE_FORAGER);
        workerA.setSubtypeProfile(AntSubtypeProfile.standard());

        Ant workerB = new Ant(colony, GameConstants.TYPE_WORKER);
        workerB.setPosition(new Point(102, 101));
        workerB.setRole(GameConstants.ROLE_FORAGER);
        workerB.setSubtypeProfile(AntSubtypeProfile.standard());

        Ant workerC = new Ant(colony, GameConstants.TYPE_WORKER);
        workerC.setPosition(new Point(102, 101));
        workerC.setRole(GameConstants.ROLE_NURSE);
        workerC.setSubtypeProfile(AntSubtypeProfile.standard());

        assertEquals(
                ColonySpriteMergeLod.antBucket(workerA, GameConstants.TYPE_WORKER),
                ColonySpriteMergeLod.antBucket(workerB, GameConstants.TYPE_WORKER));
        assertFalse(ColonySpriteMergeLod.antBucket(workerA, GameConstants.TYPE_WORKER)
                .equals(ColonySpriteMergeLod.antBucket(workerC, GameConstants.TYPE_WORKER)));
    }

    @Test
    void forEachAntGroup_chunksByMaxGroupSize() {
        Colony colony = new Colony(1, "c", true);
        Dynasty dynasty = new Dynasty(1, "d", true, GameConstants.SPECIES_OMNI);
        colony.setDynasty(dynasty);

        List<Ant> ants = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
            ant.setPosition(new Point(100 + i, 100));
            ants.add(ant);
        }

        ColonySpriteMergeLod.AntMergeBucket bucket = ColonySpriteMergeLod.antBucket(ants.get(0), GameConstants.TYPE_WORKER);
        Map<ColonySpriteMergeLod.AntMergeBucket, List<Ant>> buckets = new HashMap<>();
        buckets.put(bucket, ants);

        List<Integer> groupSizes = new ArrayList<>();
        ColonySpriteMergeLod.forEachAntGroup(buckets, 25, (key, group) -> groupSizes.add(group.size()));

        assertEquals(List.of(25), groupSizes);
    }

    @Test
    void antBucket_splitsCarryLoad() {
        Colony colony = new Colony(1, "c", true);
        Dynasty dynasty = new Dynasty(1, "d", true, GameConstants.SPECIES_OMNI);
        colony.setDynasty(dynasty);

        Ant carryingPlant = new Ant(colony, GameConstants.TYPE_WORKER);
        carryingPlant.setPosition(new Point(100, 100));
        carryingPlant.setRole(GameConstants.ROLE_FORAGER);
        carryingPlant.setCarrying(GameConstants.RESOURCE_PLANT);

        Ant sameLoad = new Ant(colony, GameConstants.TYPE_WORKER);
        sameLoad.setPosition(new Point(101, 102));
        sameLoad.setRole(GameConstants.ROLE_FORAGER);
        sameLoad.setCarrying(GameConstants.RESOURCE_PLANT);

        Ant carryingMeat = new Ant(colony, GameConstants.TYPE_WORKER);
        carryingMeat.setPosition(new Point(100, 100));
        carryingMeat.setRole(GameConstants.ROLE_FORAGER);
        carryingMeat.setCarrying(GameConstants.RESOURCE_MEAT);

        assertEquals(
                ColonySpriteMergeLod.antBucket(carryingPlant, GameConstants.TYPE_WORKER),
                ColonySpriteMergeLod.antBucket(sameLoad, GameConstants.TYPE_WORKER));
        assertFalse(ColonySpriteMergeLod.antBucket(carryingPlant, GameConstants.TYPE_WORKER)
                .equals(ColonySpriteMergeLod.antBucket(carryingMeat, GameConstants.TYPE_WORKER)));
    }

    @Test
    void zoneCappedMaxGroupSize_preservesMinimumVisibleFraction() {
        assertEquals(10, ColonySpriteMergeLod.zoneCappedMaxGroupSize(100, GameNumbers.SPRITE_MERGE_LARGE_GROUP_SIZE));
        assertEquals(10, ColonySpriteMergeLod.minimumVisibleSpritesForZone(100));
        assertEquals(100, ColonySpriteMergeLod.minimumVisibleSpritesForZone(1000));

        int capped = ColonySpriteMergeLod.zoneCappedMaxGroupSize(1000, 250);
        assertEquals(10, capped);
        assertTrue((int) Math.ceil(1000 / (double) capped) >= ColonySpriteMergeLod.minimumVisibleSpritesForZone(1000));
    }

    @Test
    void zoneMergeContext_activatesWhenRoomExceedsThreshold() {
        Colony colony = new Colony(1, "c", true);
        Dynasty dynasty = new Dynasty(1, "d", true, GameConstants.SPECIES_OMNI);
        colony.setDynasty(dynasty);

        Rectangle nursery = new Rectangle(0, 0, 256, 256);
        ColonySpriteMergeLod.MergeZoneLayout layout = new ColonySpriteMergeLod.MergeZoneLayout(
                null,
                null,
                null,
                nursery,
                null,
                null,
                null,
                null,
                null,
                null);

        for (int i = 0; i < GameNumbers.SPRITE_MERGE_ZONE_THRESHOLD + 1; i++) {
            Ant ant = new Ant(colony, GameConstants.TYPE_LARVA);
            ant.setDimension(WorldSpaces.UNDERWORLD);
            ant.setPosition(new Point(10 + (i % 20), 10 + (i / 20)));
            colony.getLarvae().add(ant);
        }

        ColonySpriteMergeLod.ZoneMergeContext context = ColonySpriteMergeLod.buildZoneMergeContext(
                colony,
                WorldSpaces.UNDERWORLD,
                layout);

        Ant inside = colony.getLarvae().get(0);
        assertTrue(context.shouldMergeAnt(inside));
        assertTrue(context.isZoneMergeEnabled(ColonySpriteMergeLod.ZONE_NURSERY));
    }

    @Test
    void mergeCentroid_averagesPositions() {
        Colony colony = new Colony(1, "c", true);
        Dynasty dynasty = new Dynasty(1, "d", true, GameConstants.SPECIES_OMNI);
        colony.setDynasty(dynasty);

        Ant a = new Ant(colony, GameConstants.TYPE_WORKER);
        a.setPosition(new Point(100, 200));
        Ant b = new Ant(colony, GameConstants.TYPE_WORKER);
        b.setPosition(new Point(104, 208));

        List<Critter> group = List.of(a, b);
        assertEquals(102, ColonySpriteMergeLod.mergeCentroidX(group));
        assertEquals(204, ColonySpriteMergeLod.mergeCentroidY(group));
    }
}
