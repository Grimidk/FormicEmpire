package com.grimidk.formicempire.classes.infrasctructure.registries;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GameConstantsRoomSpritesTest {

    @Test
    void roomSpritesLoadFromClasspath() {
        Assertions.assertNotNull(GameConstants.ROOM_ROYAL_L2);
        Assertions.assertNotNull(GameConstants.ROOM_WATER_L0);
        Assertions.assertNotNull(GameConstants.ROOM_PASSIVE_LAB);
        assertPositiveSize(GameConstants.ROOM_ROYAL_L2);
        assertPositiveSize(GameConstants.ROOM_WATER_L0);
        assertPositiveSize(GameConstants.ROOM_PASSIVE_LAB);
    }

    private static void assertPositiveSize(ImageIcon icon) {
        Assertions.assertTrue(icon.getIconWidth() > 0);
        Assertions.assertTrue(icon.getIconHeight() > 0);
    }
}
