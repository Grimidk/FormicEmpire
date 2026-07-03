package com.grimidk.formicempire.classes.infrasctructure.assets;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BuildingSpriteResourcesTest {

    @Test
    void roomAndPassiveIconsAreUniformSquareSize() {
        ImageIcon royal = BuildingSpriteResources.roomTier("royal", 2);
        ImageIcon water = BuildingSpriteResources.roomTier("water", 0);
        ImageIcon passive = BuildingSpriteResources.passiveRoom("lab");
        Assertions.assertNotNull(royal);
        Assertions.assertNotNull(water);
        Assertions.assertNotNull(passive);
        int px = BuildingSpriteResources.ROOM_ICON_PX;
        Assertions.assertEquals(px, royal.getIconWidth());
        Assertions.assertEquals(px, royal.getIconHeight());
        Assertions.assertEquals(px, water.getIconWidth());
        Assertions.assertEquals(px, passive.getIconWidth());
    }
}
