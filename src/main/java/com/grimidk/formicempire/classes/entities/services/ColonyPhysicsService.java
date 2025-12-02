package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Bug;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Room;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class ColonyPhysicsService {
    // --- Constants ---
    private final int ROOM_SIZE = 256; 
    private final int HALL_WIDTH = 128;
    private final int PAD_WALL = 64; 
    private final int PAD_DOOR = 20; 
    private final int PAD_TOP = 48;
    private final int PAD_BOTTOM = 48;
    private final int ANT_SIZE = 40;

    // --- Main Physics Loop ---
    public void runPhysics(Colony colony, int activeDimension) {
        // Ants
        for (List<Ant> antList : colony.getAntGroups().values()) {
            for (Ant ant : antList) {
                if (ant.isAlive() && ant.getDimension() == activeDimension) {
                    if (!ant.isMoving()) {
                        updateAntLogic(colony, ant);
                    }
                    ant.updatePosition();
                }
            }
        }
        
        // Bugs
        for (Bug bug : colony.getBugs()) {
            if (bug.isAlive() && bug.getDimension() == activeDimension) {
                if (!bug.isMoving()) {
                    updateBugLogic(colony, bug);
                }
                bug.updatePosition();
            }
        }
    }

    // --- Initialization ---
    public void randomizeAllAntPositions(Colony colony) {
        if (colony.getGameAreaWidth() <= 100) return;

        int virtualWidth = colony.getGameAreaWidth();

        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            if (entry.getKey() == GameConstants.TYPE_DEAD) continue;
            
            ImageIcon sprite = entry.getKey().getSprite();
            for (Ant ant : entry.getValue()) {
                if (ant.getDimension() == WorldSpaces.UNDERWORLD.getId() || shouldBeInColony(ant)) {
                    ant.setDimension(WorldSpaces.UNDERWORLD.getId());
                    Rectangle targetRoom = getTargetRoomForAnt(colony, ant, virtualWidth);
                    ant.setPosition(getRandomPointInRoom(colony, targetRoom, virtualWidth));
                } else {
                    ant.setDimension(WorldSpaces.OVERWORLD.getId());
                    ant.setPosition(getRandomOverworldPosition(colony, sprite));
                }
            }
        }
        
        for (Bug bug : colony.getBugs()) {
            if (bug.getBugType() == GameConstants.TYPE_APHID && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
                Rectangle yard = getRoomBounds(colony, WorldSpaces.RANCHER_YARD);
                bug.setPosition(getRandomPointInRoom(colony, yard, virtualWidth));
            } else {
                bug.setPosition(new Point(-1000, -1000));
            }
        }
    }

    // --- AI Logic ---
    private void updateAntLogic(Colony colony, Ant ant) {
        if (ant.getDimension() == WorldSpaces.OVERWORLD.getId()) {
            handleOverworldAnt(colony, ant);
        } else {
            handleUnderworldAnt(colony, ant);
        }
    }

    private void updateBugLogic(Colony colony, Bug bug) {
        if (bug.getDimension() == WorldSpaces.OVERWORLD.getId() && bug.getBugType() == GameConstants.TYPE_APHID) {
            if (colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
                Rectangle yard = getRoomBounds(colony, WorldSpaces.RANCHER_YARD);
                wanderInBoundaries(colony, bug, yard, 0.05);
            } else {
                bug.setPosition(new Point(-1000, -1000));
            }
        }
    }

    private void handleOverworldAnt(Colony colony, Ant ant) {
        if (shouldBeInColony(ant)) {
            // Go Home
            Point entrance = getEntrancePoint(colony);
            if (dist(ant.getX(), ant.getY(), entrance.x, entrance.y) < 30) {
                ant.setDimension(WorldSpaces.UNDERWORLD.getId());
                ant.setPosition(new Point(colony.getGameAreaWidth()/2, 50));
            } else {
                ant.moveTo(entrance);
            }
        } else {
            // Wander Outside
            Rectangle yard = null;
            if (ant.getRole() == GameConstants.ROLE_RANCHER) {
                yard = getRoomBounds(colony, WorldSpaces.RANCHER_YARD);
            } else if (ant.getRole() == GameConstants.ROLE_GRAVER) {
                yard = getRoomBounds(colony, WorldSpaces.GRAVEYARD);
            }

            if (yard != null) {
                wanderInBoundaries(colony, ant, yard, 0.05);
            } else {
                if (Math.random() < 0.01) {
                    ant.moveTo(getRandomOverworldPosition(colony, ant.getAntType().getSprite()));
                }
            }
        }
    }

    private void handleUnderworldAnt(Colony colony, Ant ant) {
        int virtualWidth = Math.max(colony.getGameAreaWidth(), 1280);
        Rectangle myRoom = getTargetRoomForAnt(colony, ant, virtualWidth);

        if (ant.getX() == 0 && ant.getY() == 0) {
            ant.setPosition(new Point((int)myRoom.getCenterX(), (int)myRoom.getCenterY()));
            ant.moveTo(null); 
            return;
        }

        if (ant.getAntType() == GameConstants.TYPE_EGG || ant.getAntType() == GameConstants.TYPE_PUPA) {
            if (!isPointInSafeBounds(colony, myRoom, ant.getX(), ant.getY())) {
                ant.setPosition(getRandomPointInRoom(colony, myRoom, virtualWidth));
            }
            return;
        }

        if (!shouldBeInColony(ant)) {
            Point exit = new Point(colony.getGameAreaWidth() / 2, -50);
            if (colony.getEntranceBounds() != null) {
                exit = new Point((int)colony.getEntranceBounds().getCenterX(), -50);
            }
            
            if (dist(ant.getX(), ant.getY(), exit.x, exit.y) < 30) {
                ant.setDimension(WorldSpaces.OVERWORLD.getId());
                ant.setPosition(getEntrancePoint(colony));
            } else {
                navigateUnderworld(colony, ant, exit);
            }
            return;
        }

        if (isPointInSafeBounds(colony, myRoom, ant.getX(), ant.getY())) {
            if (Math.random() < 0.10) {
                ant.moveTo(getRandomPointInRoom(colony, myRoom, virtualWidth));
            }
        } else {
            Point roomCenter = new Point((int)myRoom.getCenterX(), (int)myRoom.getCenterY());
            navigateUnderworld(colony, ant, roomCenter);
        }
    }

    // --- Navigation System ---
    private void navigateUnderworld(Colony colony, Ant ant, Point finalDest) {
        int hallCenterX = colony.getGameAreaWidth() / 2;
        if (colony.getEntranceBounds() != null) hallCenterX = (int)colony.getEntranceBounds().getCenterX();
        
        int currentX = ant.getX();
        int currentY = ant.getY();
        
        boolean insideRoom = Math.abs(currentX - hallCenterX) > (HALL_WIDTH / 2 + 10);

        if (insideRoom) {
            int roomRowStart = (currentY / ROOM_SIZE) * ROOM_SIZE;
            int exitDoorY = roomRowStart + (ROOM_SIZE / 2);

            if (Math.abs(currentY - exitDoorY) > 10) {
                ant.moveTo(new Point(currentX, exitDoorY));
            } else {
                int laneX = (currentX < hallCenterX) ? (hallCenterX - 30) : (hallCenterX + 30);
                ant.moveTo(new Point(laneX, currentY));
            }
        } else {
            boolean destIsRoom = Math.abs(finalDest.x - hallCenterX) > (HALL_WIDTH / 2);
            int targetY = finalDest.y;

            if (destIsRoom) {
                int destRowStart = (finalDest.y / ROOM_SIZE) * ROOM_SIZE;
                targetY = destRowStart + (ROOM_SIZE / 2);
            }

            if (Math.abs(currentY - targetY) > 10) {
                int laneX = (ant.getX() < hallCenterX) ? (hallCenterX - 30) : (hallCenterX + 30);
                
                if (Math.abs(currentX - laneX) > 10) {
                     ant.moveTo(new Point(laneX, currentY));
                } else {
                     ant.moveTo(new Point(laneX, targetY));
                }
            } else {
                ant.moveTo(finalDest);
            }
        }
    }

    // --- Room Logic & Estimates ---
    public Rectangle getTargetRoomForAnt(Colony colony, Ant ant, int gameWidth) {
        Rectangle defined = colony.getTargetRoomForAnt(ant);
        if (defined != null) return defined;
        
        Room room = findRoomForAnt(ant);
        return getRoomBounds(colony, room);
    }

    private Room findRoomForAnt(Ant ant) {
        if (isAllowedInRoom(WorldSpaces.NURSERY, ant)) return WorldSpaces.NURSERY;
        if (isAllowedInRoom(WorldSpaces.FARM, ant)) return WorldSpaces.FARM;
        if (isAllowedInRoom(WorldSpaces.ROYAL_CHAMBER, ant)) return WorldSpaces.ROYAL_CHAMBER;
        
        return WorldSpaces.STORAGE;
    }
    
    private boolean isAllowedInRoom(Room room, Ant ant) {
        if (room == null || ant == null) return false;

        AntType type = ant.getAntType();
        if (type != null && room.getAllowedAntTypes().contains(type)) {
            return true;
        }
        
        AntRole role = ant.getRole();
        if (role != null && room.getAllowedAntRoles().contains(role)) {
            return true;
        }

        return false;
    }

    public Rectangle getRoomBounds(Colony colony, Room room) {
        int x = room.getFloorPoint().x;
        int y = room.getFloorPoint().y;
        
        if (room.getDimension() == WorldSpaces.UNDERWORLD) {
            int cx = colony.getGameAreaWidth() / 2;
            x += cx;
        }
        
        return new Rectangle(x, y, room.getWidth(), room.getHeight());
    }

    private void wanderInBoundaries(Colony colony, Bug entity, Rectangle bounds, double chance) {
        if (isPointInSafeBounds(colony, bounds, entity.getX(), entity.getY())) {
            if (Math.random() < chance) {
                entity.moveTo(getRandomPointInRoom(colony, bounds, colony.getGameAreaWidth()));
            }
        } else {
            entity.moveTo(new Point((int)bounds.getCenterX(), (int)bounds.getCenterY()));
        }
    }

    private Point getRandomPointInRoom(Colony colony, Rectangle r, int gameWidth) {
        Rectangle safe = getSafeWalkableBounds(colony, r, gameWidth);
        int x = safe.x + (int)(Math.random() * safe.width);
        int y = safe.y + (int)(Math.random() * safe.height);
        return new Point(x, y);
    }

    private Rectangle getSafeWalkableBounds(Colony colony, Rectangle r, int gameWidth) {
        int hallCenterX = (colony.getEntranceBounds() != null) ? (int)colony.getEntranceBounds().getCenterX() : gameWidth/2;
        boolean isRightSide = r.getCenterX() > hallCenterX;

        int minX = isRightSide ? (r.x + PAD_DOOR) : (r.x + PAD_WALL);
        int maxX = isRightSide ? (r.x + r.width - PAD_WALL - ANT_SIZE) : (r.x + r.width - PAD_DOOR - ANT_SIZE);
        
        int minY = r.y + PAD_TOP;
        int maxY = Math.max(minY, r.y + r.height - PAD_BOTTOM - ANT_SIZE);
        
        if (maxX < minX) maxX = minX; 
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    private boolean isPointInSafeBounds(Colony colony, Rectangle r, int x, int y) {
        if (r == null) return false;
        return getSafeWalkableBounds(colony, r, colony.getGameAreaWidth()).contains(x, y);
    }

    // --- Overworld Helpers ---
    private Point getRandomOverworldPosition(Colony colony, ImageIcon sprite) {
        int w = (sprite != null) ? sprite.getIconWidth() : 0;
        int h = (sprite != null) ? sprite.getIconHeight() : 0;
        int maxX = Math.max(1, colony.getGameAreaWidth() - w);
        int maxY = Math.max(1, colony.getGameAreaHeight() - h);
        return new Point((int)(Math.random() * maxX), (int)(Math.random() * maxY));
    }
    
    private Point getEntrancePoint(Colony colony) {
        if (colony.getEntranceBounds() != null) 
            return new Point((int)colony.getEntranceBounds().getCenterX(), (int)colony.getEntranceBounds().getCenterY());
        return new Point(colony.getGameAreaWidth()/2, colony.getGameAreaHeight()/2);
    }
    
    private boolean shouldBeInColony(Ant ant) {
        if (isAllowedInRoom(WorldSpaces.NURSERY, ant)) return true;
        if (isAllowedInRoom(WorldSpaces.FARM, ant)) return true;
        if (isAllowedInRoom(WorldSpaces.ROYAL_CHAMBER, ant)) return true;
        if (isAllowedInRoom(WorldSpaces.STORAGE, ant)) return true; 
        
        return false;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }
    
    public Point getSpecificRoomPoint(Colony c, Rectangle r) { 
        return getRandomPointInRoom(c, r, c.getGameAreaWidth()); 
    }
}