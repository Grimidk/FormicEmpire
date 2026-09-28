package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class ColonyResourceDeathSelectionTest {

    @Test
    void selectsDeathsInPopulationProportions() {
        Colony colony = new Colony(1, "Test", false);
        List<Ant> candidates = new ArrayList<>();
        for (int i = 0; i < 700; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_WORKER));
        }
        for (int i = 0; i < 300; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_SOLDIER));
        }

        List<Ant> victims = ColonyResourceDeathSelection.selectVictims(candidates, 1000, null);

        assertEquals(1000, victims.size());
        assertEquals(700, countType(victims, GameConstants.CLASS_WORKER));
        assertEquals(300, countType(victims, GameConstants.CLASS_SOLDIER));
    }

    @Test
    void sparesQueensWhileOtherTypesRemain() {
        Colony colony = new Colony(2, "Test", false);
        List<Ant> candidates = new ArrayList<>();
        for (int i = 0; i < 70; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_WORKER));
        }
        for (int i = 0; i < 30; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_SOLDIER));
        }
        for (int i = 0; i < 5; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_QUEEN));
        }

        List<Ant> victims = ColonyResourceDeathSelection.selectVictims(candidates, 100, null);

        assertEquals(100, victims.size());
        assertEquals(0, countType(victims, GameConstants.CLASS_QUEEN));
        assertEquals(70, countType(victims, GameConstants.CLASS_WORKER));
        assertEquals(30, countType(victims, GameConstants.CLASS_SOLDIER));
    }

    @Test
    void killsQueensOnlyAfterNonQueensAreExhausted() {
        Colony colony = new Colony(3, "Test", false);
        List<Ant> candidates = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_WORKER));
        }
        for (int i = 0; i < 10; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_SOLDIER));
        }
        for (int i = 0; i < 20; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_QUEEN));
        }

        List<Ant> victims = ColonyResourceDeathSelection.selectVictims(candidates, 60, null);

        assertEquals(60, victims.size());
        assertEquals(40, countType(victims, GameConstants.CLASS_WORKER));
        assertEquals(10, countType(victims, GameConstants.CLASS_SOLDIER));
        assertEquals(10, countType(victims, GameConstants.CLASS_QUEEN));
    }

    @Test
    void randomWithinTypePreservesCountsAcrossRuns() {
        Colony colony = new Colony(4, "Test", false);
        List<Ant> candidates = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_WORKER));
        }
        for (int i = 0; i < 50; i++) {
            candidates.add(new Ant(colony, GameConstants.CLASS_SOLDIER));
        }

        Map<AntClass, Integer> typeTotals = new HashMap<>();
        for (int run = 0; run < 20; run++) {
            List<Ant> victims = ColonyResourceDeathSelection.selectVictims(candidates, 20, null);
            assertEquals(20, victims.size());
            typeTotals.merge(GameConstants.CLASS_WORKER, countType(victims, GameConstants.CLASS_WORKER), Integer::sum);
            typeTotals.merge(GameConstants.CLASS_SOLDIER, countType(victims, GameConstants.CLASS_SOLDIER), Integer::sum);
        }

        assertEquals(200, typeTotals.get(GameConstants.CLASS_WORKER));
        assertEquals(200, typeTotals.get(GameConstants.CLASS_SOLDIER));
        assertTrue(typeTotals.get(GameConstants.CLASS_WORKER) > 0);
        assertTrue(typeTotals.get(GameConstants.CLASS_SOLDIER) > 0);
    }

    private static int countType(List<Ant> ants, AntClass type) {
        int count = 0;
        for (Ant ant : ants) {
            if (ant.getAntClass() == type) {
                count++;
            }
        }
        return count;
    }
}
