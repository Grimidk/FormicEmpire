package com.grimidk.formicempire.classes.interfaces.game.rendering;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.services.ColonySpatialLayout;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

class RoomDecorationRendererTest {

    @Test
    void highestUnlockedPicksGreatestTierPresent() {
        Colony colony = new Colony(1, "T", true);
        Building[] chain = GameUnlocks.BUILDING_CHAIN_ROYAL;
        Assertions.assertSame(GameUnlocks.ROYAL_CHAMBER_0, RoomDecorationRenderer.highestUnlocked(colony, chain));

        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        Assertions.assertSame(GameUnlocks.ROYAL_CHAMBER_1, RoomDecorationRenderer.highestUnlocked(colony, chain));

        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_3);
        Assertions.assertSame(GameUnlocks.ROYAL_CHAMBER_3, RoomDecorationRenderer.highestUnlocked(colony, chain));
    }

    @Test
    void decorationInteriorRect_matchesPhysicsDoorAndWallPadding() {
        int rw = ColonySpatialLayout.ROOM_SIZE;
        int rh = ColonySpatialLayout.ROOM_SIZE;
        int leftRx = 50;
        Rectangle inLeft = RoomDecorationRenderer.decorationInteriorRect(leftRx, 0, rw, rh);
        Assertions.assertEquals(leftRx + ColonySpatialLayout.PAD_WALL, inLeft.x);
        Assertions.assertEquals(
                leftRx + rw - ColonySpatialLayout.PAD_DOOR,
                inLeft.x + inLeft.width);

        int rightRx = 350;
        Rectangle inRight = RoomDecorationRenderer.decorationInteriorRect(rightRx, 0, rw, rh);
        Assertions.assertEquals(rightRx + ColonySpatialLayout.PAD_DOOR, inRight.x);
        Assertions.assertEquals(
                rightRx + rw - ColonySpatialLayout.PAD_WALL,
                inRight.x + inRight.width);
        Assertions.assertEquals(ColonySpatialLayout.PAD_TOP, inLeft.y);
        Assertions.assertEquals(
                rh - ColonySpatialLayout.PAD_TOP - ColonySpatialLayout.PAD_BOTTOM,
                inLeft.height);
    }
}
