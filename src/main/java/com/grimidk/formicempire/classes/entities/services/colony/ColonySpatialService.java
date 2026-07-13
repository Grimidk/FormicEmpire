package com.grimidk.formicempire.classes.entities.services.colony;

import java.awt.Point;
import java.awt.Rectangle;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.spatial.Dimension;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.entities.spatial.Room;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

public class ColonySpatialService {

    public Room createTempRoomAtPoint(Point p, Dimension dim) {
        return new Room(9999, "TempTarget", dim, 10, 10, false, p, p, p, p, null, null, null);
    }

    public int getHallwayCenterX(Colony colony) {
        if (colony.getEntranceBounds() != null) {
            return (int) colony.getEntranceBounds().getCenterX();
        }
        return ColonySpatialLayout.ANCHOR_CENTER_X;
    }

    public int getLaneCenter(Colony colony, boolean goingDown) {
        int center = getHallwayCenterX(colony);
        return goingDown ? (center - ColonySpatialLayout.LANE_OFFSET) : (center - ColonySpatialLayout.LANE_OFFSET / 6);
    }

    public NeoPoint getColonyEntrance(Colony colony) {
        int offset = 20;
        int x = getHallwayCenterX(colony) - offset;
        int y;
        if (colony.getEntranceBounds() != null) {
            y = (int) colony.getEntranceBounds().getCenterY() - offset;
        } else {
            y = (ColonySpatialLayout.ANCHOR_HEIGHT / 2) - offset;
        }
        return new NeoPoint(x, y, WorldSpaces.OVERWORLD);
    }

    public NeoPoint getColonyExit(Colony colony) {
        int x = getHallwayCenterX(colony);
        int y = -50;
        return new NeoPoint(x, y, WorldSpaces.UNDERWORLD);
    }

    public NeoPoint getWorldExit(Colony colony) {
        int x = getHallwayCenterX(colony);
        int y = -50;
        return new NeoPoint(x, y, WorldSpaces.OVERWORLD);
    }

    public NeoPoint getTransitPortal(Colony colony) {
        java.awt.Rectangle transit = colony.getTransitBounds();
        if (transit != null) {
            return new NeoPoint(transit.x + transit.width + 64, (int) transit.getCenterY(), WorldSpaces.UNDERWORLD);
        }
        int hallX = getHallwayCenterX(colony) + (ColonySpatialLayout.HALL_WIDTH / 2);
        int rowY = 512;
        return new NeoPoint(
                hallX + ColonySpatialLayout.ROOM_SIZE + 64,
                rowY + (ColonySpatialLayout.ROOM_SIZE / 2),
                WorldSpaces.UNDERWORLD);
    }

    public NeoPoint getTransitStagingPoint(Colony colony) {
        java.awt.Rectangle transit = colony.getTransitBounds();
        if (transit != null) {
            return new NeoPoint((int) transit.getCenterX(), (int) transit.getCenterY(), WorldSpaces.UNDERWORLD);
        }
        NeoPoint portal = getTransitPortal(colony);
        return new NeoPoint(portal.x - 96, portal.y, WorldSpaces.UNDERWORLD);
    }

    public NeoPoint getResolvedPoint(Colony colony, Room room, String pointType) {
        if (room == null) {
            return null;
        }

        if (room.getId() == WorldSpaces.SURFACE.getId()) {
            return getColonyEntrance(colony);
        }

        Rectangle dynamicBounds = null;
        if (room.getId() == 201) {
            dynamicBounds = colony.getGraverBounds();
        } else if (room.getId() == 200) {
            dynamicBounds = colony.getRancherBounds();
        } else if (room.getId() == 104) {
            dynamicBounds = colony.getBreederBounds();
        } else if (room.getId() == 105) {
            dynamicBounds = colony.getTransitBounds();
        } else if (room.getId() == 999) {
            dynamicBounds = colony.getPhysicsService().getRoomBounds(colony, WorldSpaces.CONSTRUCTION_SITE);
        }

        if (dynamicBounds != null) {
            return new NeoPoint((int) dynamicBounds.getCenterX(), (int) dynamicBounds.getCenterY(), room.getDimension());
        }

        Point p = null;
        switch (pointType) {
            case "CENTER":
                p = room.getCenterPoint();
                break;
            case "ENTRY":
                p = room.getEntryPoint();
                break;
            case "EXIT":
                p = room.getExitPoint();
                break;
            default:
                break;
        }

        if (p == null) {
            return null;
        }

        if (room.getDimension().getId() == 1) {
            return new NeoPoint(p.x + getHallwayCenterX(colony), p.y, room.getDimension());
        }

        return new NeoPoint(p.x, p.y, room.getDimension());
    }

    public boolean isYard(Room room) {
        if (room == null) {
            return false;
        }
        return room.getId() == 200 || room.getId() == 201;
    }
}
