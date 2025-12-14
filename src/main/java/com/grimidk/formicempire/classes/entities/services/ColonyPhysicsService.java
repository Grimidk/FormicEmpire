package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Bug;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
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
import java.util.Queue;
import javax.swing.ImageIcon;

public class ColonyPhysicsService {

    // --- Main Physics Loop ---
    public void runPhysics(Colony colony, Dimension activeDimension) {
        // Ants
        for (List<Ant> antList : colony.getAntGroups().values()) {
            for (Ant ant : antList) {
                if (!ant.isAlive()) continue;

                boolean isActiveDim = (ant.getDimension() == activeDimension);
                boolean shouldRunAI = isActiveDim || (Math.random() < 0.05);

                if (!ant.isMoving() && ant.hasRoute()) {
                    processNextRoutePoint(ant);
                }

                if (!ant.isMoving() && !ant.hasRoute() && shouldRunAI) {
                    updateAntLogic(colony, ant);
                }
                
                ant.updatePosition();
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
    
    private void processNextRoutePoint(Ant ant) {
        NeoPoint next = ant.getNextRoutePoint();
        if (next != null) {
            if (next.getDimension() != ant.getDimension()) {
                ant.setDimension(next.getDimension());
                ant.setPosition(next); 
            } else {
                ant.moveTo(next);
            }
        }
    }

    public void randomizeAllAntPositions(Colony colony) {
        int virtualWidth = Math.max(colony.getGameAreaWidth(), 2000);

        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            if (entry.getKey() == GameConstants.TYPE_DEAD) continue;
            
            ImageIcon sprite = entry.getKey().getSprite();
            for (Ant ant : entry.getValue()) {
                ant.clearRoute(); 
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
        if (isGatherer(ant)) {
            handleGathererLogic(colony, ant);
            return;
        }

        if (ant.getDimension() == WorldSpaces.OVERWORLD) {
            handleOverworldAnt(colony, ant);
        } else {
            handleUnderworldAnt(colony, ant);
        }
    }
    
    private boolean isGatherer(Ant ant) {
        AntRole r = ant.getRole();
        return r == GameConstants.ROLE_FORAGER || r == GameConstants.ROLE_HUNTER || r == GameConstants.ROLE_MINER;
    }

    private void handleGathererLogic(Colony colony, Ant ant) {
        if (ant.getCarrying() != null || ant.getCarryingAnt() != null) {
            Room storage = WorldSpaces.STORAGE;
            
            if (isAntInRoom(colony, ant, storage)) {
                ant.clearLoad();
            } else {
                Room current = getRoomContainingAnt(colony, ant);
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, storage, ant);
                ant.setRoute(route);
            }
            return;
        }

        ResourceSource target = colony.getLocationService().findNearestRelevantSource(colony, ant);
        
        if (target != null) {
            double d = dist(ant.getX(), ant.getY(), target.getX(), target.getY());
            
            if (d < 50 && ant.getDimension() == WorldSpaces.OVERWORLD) {
                ant.setCarrying(target.getResourceType());
            } else {
                ant.clearLoad();
                
                Room current = getRoomContainingAnt(colony, ant);
                Room sourceRoom = colony.getLocationService().createTempRoomAtPoint(
                    new Point(target.getX(), target.getY()), 
                    WorldSpaces.OVERWORLD
                );
                
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, sourceRoom, ant);
                ant.setRoute(route);
            }
        } else {
            if (ant.getDimension() == WorldSpaces.OVERWORLD) {
                 if (Math.random() < 0.01) {
                    if (ant.getRole() == GameConstants.ROLE_SCOUT || isGatherer(ant)) {
                         ant.moveTo(getRandomScoutPosition(colony));
                    } else {
                         ant.moveTo(getRandomOverworldPosition(colony, ant.getAntType().getSprite()));
                    }
                }
            } else {
                 handleUnderworldAnt(colony, ant); 
            }
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
                ant.clearLoad();
                ant.setDimension(WorldSpaces.UNDERWORLD);
                ant.setPosition(new Point(colony.getGameAreaWidth()/2, 50));
            } else {
                Room target = findRoomForAnt(colony, ant);
                Room current = getRoomContainingAnt(colony, ant); 
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, target, ant);
                ant.setRoute(route);
            }
        } else {
            Rectangle yard = getOverworldJobBounds(colony, ant);

            if (yard != null) {
                if (isPointInSafeBounds(colony, yard, ant.getX(), ant.getY())) {
                    if (Math.random() < 0.05) {
                        ant.moveTo(getRandomPointInRoom(colony, yard, colony.getGameAreaWidth()));
                    }
                } else {
                    Room targetRoom = (ant.getRole() == GameConstants.ROLE_GRAVER) ? WorldSpaces.GRAVEYARD : WorldSpaces.RANCHER_YARD;
                    Room current = getRoomContainingAnt(colony, ant); 
                    Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, targetRoom, ant);
                    ant.setRoute(route);
                }
            } else {
                if (Math.random() < 0.01) {
                    if (ant.getRole() == GameConstants.ROLE_SCOUT) {
                        ant.moveTo(getRandomScoutPosition(colony));
                    } else {
                        ant.moveTo(getRandomOverworldPosition(colony, ant.getAntType().getSprite()));
                    }
                }
            }
        }
    }

    private void handleUnderworldAnt(Colony colony, Ant ant) {
        int virtualWidth = Math.max(colony.getGameAreaWidth(), 1280);
        Rectangle myRoom = getTargetRoomForAnt(colony, ant, virtualWidth);

        if (ant.getX() == 0 && ant.getY() == 0) {
            ant.setPosition(new Point((int)myRoom.getCenterX(), (int)myRoom.getCenterY()));
            return;
        }

        if (ant.getAntType() == GameConstants.TYPE_EGG || ant.getAntType() == GameConstants.TYPE_PUPA) {
            if (!isPointInSafeBounds(colony, myRoom, ant.getX(), ant.getY())) {
                ant.setPosition(getRandomPointInRoom(colony, myRoom, virtualWidth));
            }
            return;
        }

        if (!shouldBeInColony(ant)) {
            Room targetRoom = null;
            if (ant.getRole() == GameConstants.ROLE_RANCHER) targetRoom = WorldSpaces.RANCHER_YARD;
            else if (ant.getRole() == GameConstants.ROLE_GRAVER) targetRoom = WorldSpaces.GRAVEYARD;
            
            Room current = getRoomContainingAnt(colony, ant); 

            if (targetRoom == null) {
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, new Room(999, "Surface", WorldSpaces.OVERWORLD, 0,0,false,null,colony.getLocationService().getColonyEntrance(colony),null,null,null,null,null), ant);
                ant.setRoute(route);
            } else {
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, targetRoom, ant);
                ant.setRoute(route);
            }
            return;
        }

        if (isPointInSafeBounds(colony, myRoom, ant.getX(), ant.getY())) {
            if (Math.random() < 0.10) {
                ant.moveTo(getRandomPointInRoom(colony, myRoom, virtualWidth));
            }
        } else {
            Room targetRoom = findRoomForAnt(colony, ant);
            Room current = getRoomContainingAnt(colony, ant); 
            
            Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, targetRoom, ant);
            ant.setRoute(route);
        }
    }
    
    private Room getRoomContainingAnt(Colony colony, Ant ant) {
        if (ant.getDimension() == WorldSpaces.UNDERWORLD) {
            if (getRoomBounds(colony, WorldSpaces.STORAGE).contains(ant.getX(), ant.getY())) return WorldSpaces.STORAGE;
            if (getRoomBounds(colony, WorldSpaces.NURSERY).contains(ant.getX(), ant.getY())) return WorldSpaces.NURSERY;
            if (getRoomBounds(colony, WorldSpaces.FARM).contains(ant.getX(), ant.getY())) return WorldSpaces.FARM;
            if (getRoomBounds(colony, WorldSpaces.ROYAL_CHAMBER).contains(ant.getX(), ant.getY())) return WorldSpaces.ROYAL_CHAMBER;
            
            // Check Breeder Room
            if (colony.getBreederBounds() != null && colony.getBreederBounds().contains(ant.getX(), ant.getY())) {
                return WorldSpaces.BREEDER_CHAMBER;
            }
        } 
        else if (ant.getDimension() == WorldSpaces.OVERWORLD) {
            if (getRoomBounds(colony, WorldSpaces.RANCHER_YARD).contains(ant.getX(), ant.getY())) return WorldSpaces.RANCHER_YARD;
            if (getRoomBounds(colony, WorldSpaces.GRAVEYARD).contains(ant.getX(), ant.getY())) return WorldSpaces.GRAVEYARD;
        }
        return null; 
    }
    
    private boolean isAntInRoom(Colony colony, Ant ant, Room room) {
        return ant.getDimension() == room.getDimension() 
            && getRoomBounds(colony, room).contains(ant.getX(), ant.getY());
    }

    // --- Room Logic & Estimates ---
    public Rectangle getTargetRoomForAnt(Colony colony, Ant ant, int gameWidth) {
        if (ant.getRole() == GameConstants.ROLE_BUILDER && colony.getCurrentBuildingProject() != null) {
            return getRoomBounds(colony, WorldSpaces.CONSTRUCTION_SITE);
        }

        Rectangle defined = colony.getTargetRoomForAnt(ant);
        if (defined != null) return defined;
        
        Room room = findRoomForAnt(colony, ant);
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

    private Room findRoomForAnt(Colony colony, Ant ant) {
        if (ant.getRole() == GameConstants.ROLE_BUILDER && colony.getCurrentBuildingProject() != null) {
            return WorldSpaces.CONSTRUCTION_SITE;
        }
        
        if (ant.getAntType() == GameConstants.TYPE_DRONE || ant.getRole() == GameConstants.ROLE_BREEDER) {
            if (colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
                return WorldSpaces.BREEDER_CHAMBER;
            }
        }

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
        
        if (room == WorldSpaces.BREEDER_CHAMBER) {
            if (colony.getBreederBounds() != null) return colony.getBreederBounds();
            int cx = colony.getLocationService().getHallwayCenterX(colony);
            return new Rectangle(cx - 256, 512, 256, 256);
        }

        if (room == WorldSpaces.CONSTRUCTION_SITE) {
            int cx = colony.getLocationService().getHallwayCenterX(colony);
            int bottomY = 512; 
            if (colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
                bottomY = 768;
            }
            int size = 100;
            return new Rectangle(cx - (size/2), bottomY, size, size);
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

    private Point getRandomOverworldPosition(Colony colony, ImageIcon sprite) {
        int w = (sprite != null) ? sprite.getIconWidth() : 0;
        int h = (sprite != null) ? sprite.getIconHeight() : 0;
        int width = Math.max(colony.getGameAreaWidth(), 2000);
        int height = Math.max(colony.getGameAreaHeight(), 2000);
        
        int maxX = Math.max(1, width - w);
        int maxY = Math.max(1, height - h);
        return new Point((int)(Math.random() * maxX), (int)(Math.random() * maxY));
    }

    private Point getRandomScoutPosition(Colony colony) {
        int width = Math.max(colony.getGameAreaWidth(), 2000);
        int height = Math.max(colony.getGameAreaHeight(), 2000);
        
        int minX = -500;
        int maxX = width + 500;
        int minY = -500;
        int maxY = height + 500;
        
        int x = minX + (int)(Math.random() * (maxX - minX));
        int y = minY + (int)(Math.random() * (maxY - minY));
        return new Point(x, y);
    }
    
    private boolean shouldBeInColony(Ant ant) {
        if (isAllowedInRoom(WorldSpaces.NURSERY, ant)) return true;
        if (isAllowedInRoom(WorldSpaces.FARM, ant)) return true;
        if (isAllowedInRoom(WorldSpaces.ROYAL_CHAMBER, ant)) return true;
        if (isAllowedInRoom(WorldSpaces.STORAGE, ant)) return true; 
        
        if (ant.getRole() == GameConstants.ROLE_BUILDER) return true;
        if (ant.getRole() == GameConstants.ROLE_BREEDER || ant.getAntType() == GameConstants.TYPE_DRONE) return true;
        
        return false;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }
    
    public Point getSpecificRoomPoint(Colony c, Rectangle r) { 
        return getRandomPointInRoom(c, r, c.getGameAreaWidth()); 
    }
}