package com.grimidk.formicempire.classes.infrasctructure.repositories;

import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.Room;

import java.awt.Point;
import java.util.List;

public final class WorldSpaces {
    private WorldSpaces() {}

    // --- Dimensions ---
    public static final Dimension OVERWORLD = new Dimension(0, "Overworld", true);
    public static final Dimension UNDERWORLD = new Dimension(1, "Underworld", true);
    public static final Dimension MAINWORLD = new Dimension(2, "Mainworld", false);

    private static final int ROOM_SIZE = 256;
    private static final int HALL_WIDTH = 128;

    private static final int REL_X_LEFT = -(HALL_WIDTH / 2) - ROOM_SIZE; 
    private static final int REL_X_RIGHT = (HALL_WIDTH / 2);
    
    // --- Colony Rooms ---
    public static final Room STORAGE = new Room(
        100, 
        "Storage", 
        UNDERWORLD, 
        ROOM_SIZE, 
        ROOM_SIZE, 
        true,
        new Point(REL_X_LEFT, 0),
        new Point(REL_X_LEFT + 128, 128), 
        new Point(REL_X_LEFT + 256, 128),
        new Point(REL_X_LEFT + 256, 128), 
        List.of(GameConstants.ROLE_LAYER), 
        List.of(), 
        List.of()
    );

    public static final Room NURSERY = new Room(
        101, 
        "Nursery", 
        UNDERWORLD, 
        ROOM_SIZE, 
        ROOM_SIZE, 
        true, 
        new Point(REL_X_LEFT, ROOM_SIZE), 
        new Point(REL_X_LEFT + 128, ROOM_SIZE + 128), 
        new Point(REL_X_LEFT + 256, ROOM_SIZE + 128),
        new Point(REL_X_LEFT + 256, ROOM_SIZE + 128),
        List.of(GameConstants.ROLE_NURSE), 
        List.of(GameConstants.TYPE_EGG, GameConstants.TYPE_LARVA, GameConstants.TYPE_PUPA),
        List.of()
    );

    public static final Room FARM = new Room(
        102, 
        "Fungal Farm", 
        UNDERWORLD, 
        ROOM_SIZE, 
        ROOM_SIZE, 
        false, 
        new Point(REL_X_RIGHT, 0), 
        new Point(REL_X_RIGHT + 128, 128), 
        new Point(REL_X_RIGHT, 128), 
        new Point(REL_X_RIGHT, 128),
        List.of(GameConstants.ROLE_FARMER), 
        List.of(),
        List.of()
    );

    public static final Room ROYAL_CHAMBER = new Room(
        103, 
        "Royal Chamber", 
        UNDERWORLD, 
        ROOM_SIZE, 
        ROOM_SIZE, 
        false, 
        new Point(REL_X_RIGHT, ROOM_SIZE), 
        new Point(REL_X_RIGHT + 128, ROOM_SIZE + 128), 
        new Point(REL_X_RIGHT, ROOM_SIZE + 128),
        new Point(REL_X_RIGHT, ROOM_SIZE + 128),
        List.of(),
        List.of(GameConstants.TYPE_QUEEN),
        List.of()
    );

    public static final Room RANCHER_YARD = new Room(
        200, 
        "Aphid Pen", 
        OVERWORLD, 
        ROOM_SIZE, 
        ROOM_SIZE, 
        true, 
        new Point(10, 10), 
        new Point(10 + 128, 10 + 128), 
        new Point(10, 10), 
        new Point(10, 10),
        List.of(GameConstants.ROLE_RANCHER), 
        List.of(),
        List.of(GameConstants.TYPE_APHID)
    );

    public static final Room GRAVEYARD = new Room(
        201, 
        "Graveyard", 
        OVERWORLD, 
        ROOM_SIZE, 
        ROOM_SIZE, 
        false, 
        new Point(2000, 2000),
        new Point(2000 + 128, 2000 + 128), 
        new Point(2000, 2000), 
        new Point(2000, 2000),
        List.of(GameConstants.ROLE_GRAVER), 
        List.of(GameConstants.TYPE_DEAD),
        List.of()
    );

    public static final Room BREEDER_CHAMBER = new Room(
        300, 
        "Breeder Chamber", 
        UNDERWORLD, 
        ROOM_SIZE, 
        ROOM_SIZE, 
        true, 
        new Point(REL_X_LEFT, 512), 
        new Point(REL_X_LEFT + 128, 512 + 128), 
        new Point(REL_X_LEFT + 256, 512 + 128),
        new Point(REL_X_LEFT + 256, 512 + 128),
        List.of(GameConstants.ROLE_BREEDER), 
        List.of(GameConstants.TYPE_DRONE),
        List.of()
    );

    // Dynamic Construction Site (Default coords are placeholders)
    public static final Room CONSTRUCTION_SITE = new Room(
        999, 
        "Construction Site", 
        UNDERWORLD, 
        100, 
        100, 
        false, 
        new Point(0, 0), 
        new Point(0, 0), 
        new Point(0, 0), 
        new Point(0, 0),
        List.of(GameConstants.ROLE_BUILDER), 
        List.of(),
        List.of()
    );
}