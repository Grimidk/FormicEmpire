package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Bug;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class ColonyPhysicsService {
    private final int padTop = 48;
    private final int padBottom = 48;
    private final int padBack = 64; 
    private final int padHall = 16;    
    private final int antSize = 40;     
    
    private final int ROOM_SIZE = 256;
    private final int HALL_WIDTH = 128;

    // --- Initialization ---
    public void randomizeAllAntPositions(Colony colony) {
        int virtualWidth = Math.max(colony.getGameAreaWidth(), 1280);

        // Randomize Ants
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            if (entry.getKey() == GameConstants.TYPE_DEAD) continue;
            
            ImageIcon sprite = entry.getKey().getSprite();
            for (Ant ant : entry.getValue()) {
                if (ant.getDimension() == 1 || shouldBeInColony(ant)) {
                    ant.setDimension(1);
                    Rectangle room = getRoomForAnt(colony, ant, virtualWidth);
                    ant.setPosition(getRandomPointInRoom(colony, room, virtualWidth));
                } else {
                    ant.setPosition(getRandomOverworldPosition(colony, sprite));
                }
            }
        }
        
        // Randomize Bugs 
        for (Bug bug : colony.getBugs()) {
            if (bug.getBugType() == GameConstants.TYPE_APHID && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
                Rectangle yard = getOrEstimateRancherBounds(colony);
                bug.setPosition(getRandomPointInRoom(colony, yard, virtualWidth));
            } else {
                bug.setPosition(new Point(-1000, -1000));
            }
        }
    }

    // --- Main Loop ---
    public void runPhysics(Colony colony, int activeDimension) {
        // Update Ants
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
        
        // Update Bugs
        for (Bug bug : colony.getBugs()) {
            if (bug.isAlive() && bug.getDimension() == activeDimension) {
                if (!bug.isMoving()) {
                    updateBugLogic(colony, bug);
                }
                bug.updatePosition();
            }
        }
    }

    // --- Logic Updates ---
    private void updateAntLogic(Colony colony, Ant ant) {
        if (ant.getDimension() == 0) {
            handleOverworldAnt(colony, ant);
        } else {
            handleUnderworldAnt(colony, ant);
        }
    }

    private void updateBugLogic(Colony colony, Bug bug) {
        if (bug.getDimension() == 0 && bug.getBugType() == GameConstants.TYPE_APHID) {
            if (colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
                Rectangle yard = getOrEstimateRancherBounds(colony);
                wanderInBoundaries(colony, bug, yard, 0.05);
            } else {
                bug.setPosition(new Point(-1000, -1000));
            }
        }
    }

    private void handleOverworldAnt(Colony colony, Ant ant) {
        if (shouldBeInColony(ant)) {
            // Go home
            Point entrance = getEntrancePoint(colony);
            if (dist(ant.getX(), ant.getY(), entrance.x, entrance.y) < 30) {
                ant.setDimension(1);
                ant.setPosition(new Point(colony.getGameAreaWidth()/2, 50));
            } else {
                ant.moveTo(entrance);
            }
        } else {
            // Work outside
            AntRole role = ant.getRole();
            if (role == GameConstants.ROLE_RANCHER) {
                wanderInBoundaries(colony, ant, getOrEstimateRancherBounds(colony), 0.05);
            } else if (role == GameConstants.ROLE_GRAVER) {
                wanderInBoundaries(colony, ant, getOrEstimateGraverBounds(colony), 0.05);
            } else {
                // Wander freely
                if (Math.random() < 0.01) {
                    ant.moveTo(getRandomOverworldPosition(colony, ant.getAntType().getSprite()));
                }
            }
        }
    }

    private void handleUnderworldAnt(Colony colony, Ant ant) {
        int virtualWidth = Math.max(colony.getGameAreaWidth(), 1280);
        Rectangle room = getRoomForAnt(colony, ant, virtualWidth);

        if (ant.getX() == 0 && ant.getY() == 0) {
            ant.setPosition(getRandomPointInRoom(colony, room, virtualWidth));
            ant.moveTo(null); 
            return;
        }

        if (ant.getAntType() == GameConstants.TYPE_EGG || ant.getAntType() == GameConstants.TYPE_PUPA) {
            if (!isPointInRoom(colony, room, ant.getX(), ant.getY())) {
                ant.setPosition(getRandomPointInRoom(colony, room, virtualWidth));
            }
            return;
        }

        boolean roleBelongsOutside = !shouldBeInColony(ant);
        
        if (roleBelongsOutside) {
            Point exit = new Point(colony.getGameAreaWidth() / 2, 20);
            if (colony.getEntranceBounds() != null) {
                exit = new Point((int)colony.getEntranceBounds().getCenterX(), colony.getEntranceBounds().y + 20);
            }

            if (dist(ant.getX(), ant.getY(), exit.x, exit.y) < 30) {
                ant.setDimension(0);
                ant.setPosition(getEntrancePoint(colony));
            } else {
                navigateUnderworld(colony, ant, exit);
            }
        } else {
            wanderInBoundaries(colony, ant, room, 0.10);
            
            if (!isPointInRoom(colony, room, ant.getX(), ant.getY()) && 
                dist(ant.getX(), ant.getY(), room.getCenterX(), room.getCenterY()) > 300) {
                Point center = new Point((int)room.getCenterX(), (int)room.getCenterY());
                navigateUnderworld(colony, ant, center);
            }
        }
    }

    private void wanderInBoundaries(Colony colony, Bug entity, Rectangle bounds, double chance) {
        if (isPointInRoom(colony, bounds, entity.getX(), entity.getY())) {
            if (Math.random() < chance) {
                entity.moveTo(getRandomPointInRoom(colony, bounds, colony.getGameAreaWidth()));
            }
        } else {
            entity.moveTo(new Point((int)bounds.getCenterX(), (int)bounds.getCenterY()));
        }
    }

    private void navigateUnderworld(Colony colony, Ant ant, Point dest) {
        int hallX = colony.getGameAreaWidth() / 2;
        if (colony.getEntranceBounds() != null) hallX = (int)colony.getEntranceBounds().getCenterX();

        boolean inRoom = Math.abs(ant.getX() - hallX) > (HALL_WIDTH / 2 + 10);
        
        if (inRoom) {
            int roomY = (ant.getY() / 256) * 256 + 128;
            ant.moveTo(new Point(hallX, roomY));
        } else {
            if (Math.abs(ant.getY() - dest.y) > 10) {
                ant.moveTo(new Point(hallX, dest.y));
            } else {
                ant.moveTo(dest);
            }
        }
    }

    // --- Helpers & Bounds ---
    public Point getSpecificRoomPoint(Colony c, Rectangle r) { return getRandomPointInRoom(c, r, c.getGameAreaWidth()); }
    public Point getSpecificRoomPoint(Colony c, Rectangle r, int w) { return getRandomPointInRoom(c, r, w); }

    private Point getRandomPointInRoom(Colony colony, Rectangle r, int gameWidth) {
        Rectangle bounds = getWalkableRoomBounds(colony, r, gameWidth);
        int x = bounds.x + (int)(Math.random() * bounds.width);
        int y = bounds.y + (int)(Math.random() * bounds.height);
        return new Point(x, y);
    }

    private boolean isPointInRoom(Colony colony, Rectangle r, int x, int y) {
        if (r == null) return false;
        return getWalkableRoomBounds(colony, r, colony.getGameAreaWidth()).contains(x, y);
    }

    private Rectangle getWalkableRoomBounds(Colony colony, Rectangle r, int gameWidth) {
        int hallCenterX = (colony.getEntranceBounds() != null) ? (int)colony.getEntranceBounds().getCenterX() : gameWidth/2;
        boolean isRightSide = r.getCenterX() > hallCenterX;

        int minX = isRightSide ? (r.x + padHall) : (r.x + padBack);
        int maxX = isRightSide ? (r.x + r.width - padBack - antSize) : (r.x + r.width - padHall - antSize);
        int minY = r.y + padTop;
        int maxY = Math.max(minY, r.y + r.height - padBottom - antSize);
        
        if (maxX < minX) maxX = minX; 
        
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    private Rectangle getRoomForAnt(Colony colony, Ant ant, int gameWidth) {
        Rectangle defined = colony.getTargetRoomForAnt(ant);
        if (defined != null) return defined;

        return estimateRoomBounds(colony, ant, gameWidth);
    }

    private Rectangle estimateRoomBounds(Colony colony, Ant ant, int gameWidth) {
        if (ant.getRole() == GameConstants.ROLE_RANCHER) return getOrEstimateRancherBounds(colony);
        if (ant.getRole() == GameConstants.ROLE_GRAVER) return getOrEstimateGraverBounds(colony);

        // 1=TL, 2=TR, 3=BL, 4=BR
        int index = 1; // Default Storage
        AntType t = ant.getAntType();
        if (t == GameConstants.TYPE_QUEEN) index = 4;
        else if (t == GameConstants.TYPE_EGG || t == GameConstants.TYPE_LARVA || t == GameConstants.TYPE_PUPA || ant.getRole() == GameConstants.ROLE_NURSE) index = 3;
        else if (ant.getRole() == GameConstants.ROLE_FARMER) index = 2;

        int cx = gameWidth / 2;
        int x = (index % 2 == 0) ? (cx + HALL_WIDTH/2) : (cx - HALL_WIDTH/2 - ROOM_SIZE);
        int y = (index > 2) ? ROOM_SIZE : 0;
        
        return new Rectangle(x, y, ROOM_SIZE, ROOM_SIZE);
    }
    
    private Rectangle getOrEstimateRancherBounds(Colony colony) {
        if (colony.getRancherBounds() != null) return colony.getRancherBounds();
        return new Rectangle(10, 10, 256, 256);
    }

    private Rectangle getOrEstimateGraverBounds(Colony colony) {
        if (colony.getGraverBounds() != null) return colony.getGraverBounds();
        return new Rectangle(colony.getGameAreaWidth() - 270, colony.getGameAreaHeight() - 270, 256, 256);
    }

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
        AntType t = ant.getAntType();
        if (t == GameConstants.TYPE_EGG || t == GameConstants.TYPE_LARVA || t == GameConstants.TYPE_PUPA || t == GameConstants.TYPE_QUEEN) return true;
        AntRole r = ant.getRole();
        return !(r == GameConstants.ROLE_RANCHER || r == GameConstants.ROLE_GRAVER || r == GameConstants.ROLE_FORAGER || r == GameConstants.ROLE_HUNTER);
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }
}