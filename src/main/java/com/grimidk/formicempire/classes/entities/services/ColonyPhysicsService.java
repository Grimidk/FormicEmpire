package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Bug;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.NeoPoint;
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

    // --- Main Physics Loop ---
    public void runPhysics(Colony colony, Dimension activeDimension) {
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
                if (ant.getDimension() == WorldSpaces.UNDERWORLD || shouldBeInColony(ant)) {
                    ant.setDimension(WorldSpaces.UNDERWORLD);
                    Rectangle targetRoom = getTargetRoomForAnt(colony, ant, virtualWidth);
                    ant.setPosition(getRandomPointInRoom(colony, targetRoom, virtualWidth));
                } else {
                    ant.setDimension(WorldSpaces.OVERWORLD);
                    
                    Rectangle yard = getOverworldJobBounds(colony, ant);
                    if (yard != null) {
                        ant.setPosition(getRandomPointInRoom(colony, yard, virtualWidth));
                    } else {
                        ant.setPosition(getRandomOverworldPosition(colony, sprite));
                    }
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
        if (ant.getDimension() == WorldSpaces.OVERWORLD) {
            handleOverworldAnt(colony, ant);
        } else {
            handleUnderworldAnt(colony, ant);
        }
    }

    private void updateBugLogic(Colony colony, Bug bug) {
        if (bug.getDimension() == WorldSpaces.OVERWORLD && bug.getBugType() == GameConstants.TYPE_APHID) {
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
            NeoPoint entrance = colony.getLocationService().getColonyEntrance(colony);
            
            if (dist(ant.getX(), ant.getY(), entrance.x, entrance.y) < 30) {
                ant.setCarrying(null);
                ant.setCarryingSec(null);
                ant.setDimension(WorldSpaces.UNDERWORLD);
                ant.setPosition(new Point(colony.getGameAreaWidth()/2, 50));
            } else {
                ant.moveTo(entrance);
            }
        } else {
            Rectangle yard = getOverworldJobBounds(colony, ant);

            if (yard != null) {
                if (isPointInSafeBounds(colony, yard, ant.getX(), ant.getY())) {
                    if (Math.random() < 0.05) {
                        ant.moveTo(getRandomPointInRoom(colony, yard, colony.getGameAreaWidth()));
                    }
                } else {
                    ant.moveTo(new Point((int)yard.getCenterX(), (int)yard.getCenterY()));
                }
            } else {
                // Scouts / Wanderers
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

        // Eggs/Pupae don't move unless stuck in wall
        if (ant.getAntType() == GameConstants.TYPE_EGG || ant.getAntType() == GameConstants.TYPE_PUPA) {
            if (!isPointInSafeBounds(colony, myRoom, ant.getX(), ant.getY())) {
                ant.setPosition(getRandomPointInRoom(colony, myRoom, virtualWidth));
            }
            return;
        }

        // Exit Logic
        if (!shouldBeInColony(ant)) {
            NeoPoint exit = colony.getLocationService().getColonyExit(colony);
            
            if (dist(ant.getX(), ant.getY(), exit.x, exit.y) < 30) {
                ant.setDimension(WorldSpaces.OVERWORLD);
                ant.setPosition(colony.getLocationService().getColonyEntrance(colony));
            } else {
                navigateUnderworld(colony, ant, exit);
            }
            return;
        }

        // Job Logic
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
        ColonyLocationService locations = colony.getLocationService();
        int hallCenterX = locations.getHallwayCenterX(colony);
        
        int currentX = ant.getX();
        int currentY = ant.getY();  
        
        // Lane Logic: Left=Up, Right=Down
        boolean goingDown = finalDest.y > currentY;
        int targetLaneX = locations.getLaneCenter(colony, goingDown);

        boolean insideRoom = Math.abs(currentX - hallCenterX) > (ColonyLocationService.HALL_WIDTH / 2 + 10);

        if (insideRoom) {
            int roomRowStart = (currentY / ColonyLocationService.ROOM_SIZE) * ColonyLocationService.ROOM_SIZE;
            int exitDoorY = roomRowStart + (ColonyLocationService.ROOM_SIZE / 2);

            if (Math.abs(currentY - exitDoorY) > 10) {
                // Align to door Y
                ant.moveTo(new Point(currentX, exitDoorY));
            } else {
                // Exit room to target lane
                ant.moveTo(new Point(targetLaneX, currentY));
            }
        } else {
            // In Hallway
            boolean destIsRoom = Math.abs(finalDest.x - hallCenterX) > (ColonyLocationService.HALL_WIDTH / 2);
            int targetY = finalDest.y;

            if (destIsRoom) {
                int destRowStart = (finalDest.y / ColonyLocationService.ROOM_SIZE) * ColonyLocationService.ROOM_SIZE;
                targetY = destRowStart + (ColonyLocationService.ROOM_SIZE / 2);
            }

            // Get in Lane
            if (Math.abs(currentX - targetLaneX) > 5) {
                ant.moveTo(new Point(targetLaneX, currentY));
            } else {
                // Follow Lane
                if (Math.abs(currentY - targetY) > 10) {
                     ant.moveTo(new Point(targetLaneX, targetY));
                } else {
                     // Turn into destination
                     ant.moveTo(finalDest);
                }
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
    
    private Rectangle getOverworldJobBounds(Colony colony, Ant ant) {
        if (ant.getRole() == GameConstants.ROLE_RANCHER) {
            return getRoomBounds(colony, WorldSpaces.RANCHER_YARD);
        } 
        if (ant.getRole() == GameConstants.ROLE_GRAVER) {
            return getRoomBounds(colony, WorldSpaces.GRAVEYARD);
        }
        return null;
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
        if (room == WorldSpaces.RANCHER_YARD) {
            if (colony.getRancherBounds() != null) return colony.getRancherBounds();
            return new Rectangle(10, 10, room.getWidth(), room.getHeight());
        }
        
        if (room == WorldSpaces.GRAVEYARD) {
            if (colony.getGraverBounds() != null) return colony.getGraverBounds();
            int x = Math.max(0, colony.getGameAreaWidth() - room.getWidth() - 10);
            int y = Math.max(0, colony.getGameAreaHeight() - room.getHeight() - 10);
            return new Rectangle(x, y, room.getWidth(), room.getHeight());
        }

        int x = room.getFloorPoint().x;
        int y = room.getFloorPoint().y;
        
        if (room.getDimension() == WorldSpaces.UNDERWORLD) {
            int cx = colony.getLocationService().getHallwayCenterX(colony);
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
        boolean isUnderworld = (r.y < 0) || (r.y > 2000) || (Math.abs(r.x - gameWidth/2) < 500); 
        int hallCenterX = colony.getLocationService().getHallwayCenterX(colony);
        
        boolean isRightSide = r.getCenterX() > hallCenterX;
        int pLeft = ColonyLocationService.PAD_WALL;
        int pRight = ColonyLocationService.PAD_WALL;
        
        if (Math.abs(r.getCenterX() - hallCenterX) < (r.getWidth() + 200)) {
             if (isRightSide) pLeft = ColonyLocationService.PAD_DOOR; 
             else pRight = ColonyLocationService.PAD_DOOR;        
        }

        int minX = r.x + pLeft;
        int maxX = r.x + r.width - pRight - ColonyLocationService.ANT_SIZE;
        
        int minY = r.y + ColonyLocationService.PAD_TOP;
        int maxY = Math.max(minY, r.y + r.height - ColonyLocationService.PAD_BOTTOM - ColonyLocationService.ANT_SIZE);
        
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