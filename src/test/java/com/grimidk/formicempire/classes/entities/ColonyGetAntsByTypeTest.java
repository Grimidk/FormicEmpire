package com.grimidk.formicempire.classes.entities;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;

class ColonyGetAntsByTypeTest {

    @Test
    void getAntsByType_dead_isDeadAntsList() {
        Colony colony = new Colony(1, "T", true);
        assertSame(colony.getDeadAnts(), colony.getAntsByType(GameConstants.TYPE_DEAD));
    }

    @Test
    void getAntsByType_unknownType_returnsEmptyNotAllocatingEachCall() {
        Colony colony = new Colony(1, "T", true);
        assertTrue(colony.getAntsByType(GameConstants.TYPE_ZOMBIE).isEmpty());
        assertSame(colony.getAntsByType(GameConstants.TYPE_ZOMBIE), colony.getAntsByType(GameConstants.TYPE_ZOMBIE));
    }
}
