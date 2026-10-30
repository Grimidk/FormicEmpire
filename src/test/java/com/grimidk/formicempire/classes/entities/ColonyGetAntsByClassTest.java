package com.grimidk.formicempire.classes.entities;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;

class ColonyGetAntsByClassTest {

    @Test
    void getAntsByClass_dead_isDeadAntsList() {
        Colony colony = new Colony(1, "T", true);
        assertSame(colony.getDeadAnts(), colony.getAntsByClass(GameConstants.CLASS_DEAD));
    }

    @Test
    void getAntsByClass_unknownType_returnsEmptyNotAllocatingEachCall() {
        Colony colony = new Colony(1, "T", true);
        assertTrue(colony.getAntsByClass(GameConstants.CLASS_ZOMBIE).isEmpty());
        assertSame(colony.getAntsByClass(GameConstants.CLASS_ZOMBIE), colony.getAntsByClass(GameConstants.CLASS_ZOMBIE));
    }
}
