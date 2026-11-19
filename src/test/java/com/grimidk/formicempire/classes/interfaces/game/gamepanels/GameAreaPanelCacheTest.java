package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameAreaPanelCacheTest {

    @Test
    void setColonyPreservesCachedSignaturesWhenUnchangedAndResetsWhenChanged() throws Exception {
        GameAreaPanel panel = new GameAreaPanel();
        Colony c1 = new Colony(1, "C1", true);
        Colony c2 = new Colony(2, "C2", false);

        Field syncKeyField = GameAreaPanel.class.getDeclaredField("lastPetPenBoundsSyncKey");
        syncKeyField.setAccessible(true);
        Field droneSigField = GameAreaPanel.class.getDeclaredField("cachedAssimilatedDroneSignature");
        droneSigField.setAccessible(true);

        panel.setColony(c1);
        syncKeyField.setInt(panel, 12345);
        droneSigField.setInt(panel, 67890);

        panel.setColony(c1);
        assertEquals(12345, syncKeyField.getInt(panel));
        assertEquals(67890, droneSigField.getInt(panel));

        panel.setColony(c2);
        assertEquals(Integer.MIN_VALUE, syncKeyField.getInt(panel));
        assertEquals(Integer.MIN_VALUE, droneSigField.getInt(panel));
    }
}
