package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

class ColonyRoomDecorationServiceTest {

    @Test
    void highestUnlockedPicksGreatestTierPresent() {
        Colony colony = new Colony(1, "T", true);
        Building[] chain = GameUnlocks.BUILDING_CHAIN_ROYAL;
        Assertions.assertSame(GameUnlocks.ROYAL_CHAMBER_0, ColonyRoomDecorationService.highestUnlocked(colony, chain));

        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        Assertions.assertSame(GameUnlocks.ROYAL_CHAMBER_1, ColonyRoomDecorationService.highestUnlocked(colony, chain));

        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_3);
        Assertions.assertSame(GameUnlocks.ROYAL_CHAMBER_3, ColonyRoomDecorationService.highestUnlocked(colony, chain));
    }

    @Test
    void decorationInteriorRect_matchesPhysicsDoorAndWallPadding() {
        int rw = ColonyLocationService.ROOM_SIZE;
        int rh = ColonyLocationService.ROOM_SIZE;
        int leftRx = 50;
        Rectangle inLeft = ColonyRoomDecorationService.decorationInteriorRect(leftRx, 0, rw, rh);
        Assertions.assertEquals(leftRx + ColonyLocationService.PAD_WALL, inLeft.x);
        Assertions.assertEquals(
                leftRx + rw - ColonyLocationService.PAD_DOOR,
                inLeft.x + inLeft.width);

        int rightRx = 350;
        Rectangle inRight = ColonyRoomDecorationService.decorationInteriorRect(rightRx, 0, rw, rh);
        Assertions.assertEquals(rightRx + ColonyLocationService.PAD_DOOR, inRight.x);
        Assertions.assertEquals(
                rightRx + rw - ColonyLocationService.PAD_WALL,
                inRight.x + inRight.width);
        Assertions.assertEquals(ColonyLocationService.PAD_TOP, inLeft.y);
        Assertions.assertEquals(
                rh - ColonyLocationService.PAD_TOP - ColonyLocationService.PAD_BOTTOM,
                inLeft.height);
    }
}
