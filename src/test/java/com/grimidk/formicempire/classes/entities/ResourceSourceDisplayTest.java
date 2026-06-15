package com.grimidk.formicempire.classes.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

class ResourceSourceDisplayTest {

    @Test
    void iconTierFollowsCurrentQuantity() {
        ResourceSource source = new ResourceSource(
                GameConstants.RESOURCE_PLANT,
                ResourceType.SOURCE_QTY_HUGE,
                ResourceType.SOURCE_QTY_HUGE,
                100,
                200);
        assertEquals(GameConstants.SOURCE_DISPLAY_PX_HUGE, source.getDisplaySizePx());

        source.decreaseQuantity(ResourceType.SOURCE_QTY_HUGE - ResourceType.SOURCE_QTY_MEDIUM);
        assertEquals(GameConstants.SOURCE_DISPLAY_PX_MEDIUM, source.getDisplaySizePx());
        assertNotSame(
                GameConstants.RESOURCE_PLANT.getSourceSpriteHuge(),
                source.getIconForDisplay());
    }

    @Test
    void centerUsesScaledDisplaySize() {
        ResourceSource source = new ResourceSource(GameConstants.RESOURCE_WATER, 500, 40, 60);
        int half = source.getDisplaySizePx() / 2;
        assertEquals(40 + half, source.getCenterX());
        assertEquals(60 + half, source.getCenterY());
    }
}
