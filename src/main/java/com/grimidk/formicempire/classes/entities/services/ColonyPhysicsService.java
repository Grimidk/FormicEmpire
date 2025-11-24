package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class ColonyPhysicsService {
    int padTop = 48;
    int padBottom = 48;
    int padBack = 64; 
    int padHall = 16;    
    int antSize = 40;     
    final int ROOM_SIZE = 256;
    final int HALL_WIDTH = 128;

    public void randomizeAllAntPositions(Colony colony) {
        int virtualWidth = colony.getGameAreaWidth() > 100 ? colony.getGameAreaWidth() : 1280;

        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            AntType type = entry.getKey();
            if (type == GameConstants.TYPE_DEAD) continue;
            
            ImageIcon sprite = type.getSprite();
            List<Ant> ants = entry.getValue();
            
            for (Ant ant : ants) {
                boolean belongsInUnderworld = ant.getDimension() == 1 || shouldBeInColony(ant);

                if (belongsInUnderworld) {
                    if (ant.getDimension() != 1) {
                        ant.setDimension(1);
                    }

                    Rectangle targetRoom = colony.getTargetRoomForAnt(ant);                    
                    if (targetRoom == null) {
                        targetRoom = estimateRoomBounds(colony, ant, virtualWidth);
                    }                    
                    Point safeSpot = getSpecificRoomPoint(colony, targetRoom, virtualWidth);
                    ant.setPosition(safeSpot);
                } else {
                    ant.setPosition(getRandomOverworldPosition(colony, sprite));
                }
            }
        }
    }
    
    private Rectangle estimateRoomBounds(Colony colony, Ant ant, int gameWidth) {
        int centerX = gameWidth / 2;
        int hallX = centerX - (HALL_WIDTH / 2);
        
        int roomIndex;
        
        AntType type = ant.getType();
        AntRole role = ant.getRole();

        if (type == GameConstants.TYPE_QUEEN) {
            roomIndex = 4; // Royal
        } else if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_LARVA || type == GameConstants.TYPE_PUPA) {
            roomIndex = 3; // Nursery
        } else if (role == GameConstants.ROLE_NURSE) {
            roomIndex = 3; // Nursery
        } else if (role == GameConstants.ROLE_FARMER) {
            roomIndex = 2; // Farm
        } else {
            roomIndex = 1; // Storage
        }
        
        int x = 0;
        int y = 0;        
        if (roomIndex == 1) { // Top Left
            x = hallX - ROOM_SIZE;
            y = 0;
        } else if (roomIndex == 2) { // Top Right
            x = hallX + HALL_WIDTH;
            y = 0;
        } else if (roomIndex == 3) { // Bottom Left
            x = hallX - ROOM_SIZE;
            y = ROOM_SIZE;
        } else if (roomIndex == 4) { // Bottom Right
            x = hallX + HALL_WIDTH;
            y = ROOM_SIZE;
        }
        
        return new Rectangle(x, y, ROOM_SIZE, ROOM_SIZE);
    }
    
    private Point getRandomOverworldPosition(Colony colony, ImageIcon sprite) {
        int w = (sprite != null) ? sprite.getIconWidth() : 0;
        int h = (sprite != null) ? sprite.getIconHeight() : 0;
        int boundX = Math.max(1, colony.getGameAreaWidth() - w);
        int boundY = Math.max(1, colony.getGameAreaHeight() - h);
        int x = (int) (Math.random() * boundX);
        int y = (int) (Math.random() * boundY);
        return new Point(x, y);
    }

    // --- Main Physics Loop ---
    public void runPhysics(Colony colony, int activeDimension) {
        for (List<Ant> antList : colony.getAntGroups().values()) {
            for (Ant ant : antList) {
                if (ant.isAlive() && ant.getDimension() == activeDimension) {
                    updateAntLogic(colony, ant);
                    ant.updatePosition();
                }
            }
        }
    }

    private void updateAntLogic(Colony colony, Ant ant) {
        if (ant.isMoving()) return; 

        int dim = ant.getDimension(); 
        Point overworldEntrance = new Point(colony.getGameAreaWidth() / 2, colony.getGameAreaHeight() / 2);
        boolean shouldBeInside = shouldBeInColony(ant);

        // --- OVERWORLD LOGIC ---
        if (dim == 0) {
            if (shouldBeInside) {
                double dist = dist(ant.getX(), ant.getY(), overworldEntrance.x, overworldEntrance.y);
                if (dist < 15) {
                    ant.setDimension(1);
                    Rectangle entranceRect = colony.getEntranceBounds();
                    if (entranceRect != null) {
                        ant.setPosition(new Point((int)entranceRect.getCenterX(), entranceRect.y + 10));
                    }
                    ant.moveTo(null); 
                } else {
                    ant.moveTo(overworldEntrance);
                }
            } else {
                if (Math.random() < 0.01) {
                    ant.moveTo(getRandomOverworldPosition(colony, ant.getType().getSprite()));
                }
            }
        }
        
        // --- UNDERWORLD LOGIC ---
        else if (dim == 1) {
            if (ant.getX() == 0 && ant.getY() == 0) {
                 int virtualWidth = colony.getGameAreaWidth() > 100 ? colony.getGameAreaWidth() : 1280;
                 Rectangle targetRoom = colony.getTargetRoomForAnt(ant);
                 if (targetRoom == null) {
                     targetRoom = estimateRoomBounds(colony, ant, virtualWidth);
                 }
                 Point safeSpot = getSpecificRoomPoint(colony, targetRoom, virtualWidth);
                 ant.setPosition(safeSpot);
                 ant.moveTo(null);
                 return;
            }
            
            Rectangle targetRoom = colony.getTargetRoomForAnt(ant);            
            if (targetRoom == null) {
                int virtualWidth = colony.getGameAreaWidth() > 100 ? colony.getGameAreaWidth() : 1280;
                targetRoom = estimateRoomBounds(colony, ant, virtualWidth);
            }
            
            if (!shouldBeInside) {
                Rectangle entranceRect = colony.getEntranceBounds();                
                int centerX = colony.getGameAreaWidth() / 2;
                Point exitPoint;
                if (entranceRect != null) {
                    exitPoint = new Point((int)entranceRect.getCenterX(), entranceRect.y);
                } else {
                    exitPoint = new Point(centerX, 0);
                }
                 
                double dist = dist(ant.getX(), ant.getY(), exitPoint.x, exitPoint.y);
                if (dist < 15) {
                    ant.setDimension(0);
                    ant.setPosition(overworldEntrance);
                    ant.moveTo(null);
                } else {
                    navigateToPointInUnderworld(colony, ant, exitPoint);
                }
            } else {
                // Static ants
                if (ant.getType() == GameConstants.TYPE_EGG || ant.getType() == GameConstants.TYPE_PUPA) {
                    if (!isPointInSpecificBounds(colony, targetRoom, ant.getX(), ant.getY())) {
                        Point safeSpot = getSpecificRoomPoint(colony, targetRoom, colony.getGameAreaWidth());
                        ant.setPosition(safeSpot);
                        ant.moveTo(null);
                    } else {
                        if (ant.getType() == GameConstants.TYPE_LARVA && Math.random() < 0.01) {
                            ant.moveTo(getSpecificRoomPoint(colony, targetRoom, colony.getGameAreaWidth()));
                        }
                    }
                    return; 
                }

                // Active ants 
                if (isPointInSpecificBounds(colony, targetRoom, ant.getX(), ant.getY())) {
                    if (Math.random() < 0.02) {
                         Point wanderDest = getSpecificRoomPoint(colony, targetRoom, colony.getGameAreaWidth());
                         ant.moveTo(wanderDest);
                    }
                } else {
                    Point roomCenter = new Point((int)targetRoom.getCenterX(), (int)targetRoom.getCenterY());
                    navigateToPointInUnderworld(colony, ant, roomCenter);
                }
            }
        }
    }
    
    public Point getSpecificRoomPoint(Colony colony, Rectangle r) {
        return getSpecificRoomPoint(colony, r, colony.getGameAreaWidth());
    }

    public Point getSpecificRoomPoint(Colony colony, Rectangle r, int gameWidth) {
        Rectangle entrance = colony.getEntranceBounds();
        int hallCenterX;
        
        if (entrance != null) {
            hallCenterX = (int)entrance.getCenterX();
        } else {
            hallCenterX = (gameWidth > 100 ? gameWidth : 1280) / 2;
        }
        
        int minY = r.y + padTop;
        int maxY = r.y + r.height - padBottom;        
        int validMaxY = maxY - antSize;
        if (validMaxY < minY) validMaxY = minY;

        int minX, maxX;        
        if (r.getCenterX() < hallCenterX) {
            minX = r.x + padBack;
            maxX = r.x + r.width - padHall;
        } else {
            minX = r.x + padHall;
            maxX = r.x + r.width - padBack;
        }
        
        int validMaxX = maxX - antSize;
        if (validMaxX < minX) validMaxX = minX;
        int tx = minX + (int)(Math.random() * (validMaxX - minX + 1));
        int ty = minY + (int)(Math.random() * (validMaxY - minY + 1));
        
        return new Point(tx, ty);
    }
    
    private boolean isPointInSpecificBounds(Colony colony, Rectangle r, int x, int y) {
        Rectangle entrance = colony.getEntranceBounds();
        int hallCenterX = (entrance != null) ? (int)entrance.getCenterX() : (colony.getGameAreaWidth()/2);
        
        int minY = r.y + padTop;
        int maxY = r.y + r.height - padBottom - antSize; 

        int minX, maxX;

        if (r.getCenterX() < hallCenterX) {
            minX = r.x + padBack;
            maxX = r.x + r.width - padHall - antSize;
        } else {
            minX = r.x + padHall;
            maxX = r.x + r.width - padBack - antSize;
        }        
        if (maxX < minX) maxX = minX;
        if (maxY < minY) maxY = minY;
        
        return (x >= minX && x <= maxX && y >= minY && y <= maxY);
    }

    private boolean shouldBeInColony(Ant ant) {
        AntType type = ant.getType();
        
        if (type == GameConstants.TYPE_EGG || 
            type == GameConstants.TYPE_LARVA || 
            type == GameConstants.TYPE_PUPA || 
            type == GameConstants.TYPE_QUEEN) {
            return true;
        }
        
        AntRole role = ant.getRole();
        if (role == GameConstants.ROLE_FARMER || role == GameConstants.ROLE_NURSE) {
            return true;
        }
        
        return false;
    }

    private void navigateToPointInUnderworld(Colony colony, Ant ant, Point finalDest) {
        Rectangle entranceRect = colony.getEntranceBounds();
        int hallCenterX;
        int hallWidth;
        
        if (entranceRect != null) {
            hallCenterX = (int)entranceRect.getCenterX();
            hallWidth = entranceRect.width;
        } else {
            hallCenterX = colony.getGameAreaWidth() / 2;
            hallWidth = HALL_WIDTH;
        }

        int currentX = ant.getX();
        int currentY = ant.getY();
        boolean goingDown = finalDest.y > currentY;
        int laneX = goingDown ? (hallCenterX - 30) : (hallCenterX - 10);
        
        // --- Navigation Logic ---
        boolean insideRoom = Math.abs(currentX - hallCenterX) > (hallWidth / 2 + 10);
        
        if (insideRoom) {
            //Exit Room
            int roomHeight = 256;
            int roomRowStart = (currentY / roomHeight) * roomHeight;
            int exitDoorY = roomRowStart + (roomHeight / 2) - 30;
    
            if (Math.abs(currentY - exitDoorY) > 10) {
                ant.moveTo(new Point(currentX, exitDoorY));
            } else {
                ant.moveTo(new Point(laneX, currentY));
            }
        } else {
            //Hallway Travel
            boolean targetIsRoom = Math.abs(finalDest.x - hallCenterX) > (hallWidth / 2);
            int targetY = finalDest.y;

            if (targetIsRoom) {
                int roomHeight = 256;
                int destRowStart = (finalDest.y / roomHeight) * roomHeight;
                targetY = destRowStart + (roomHeight / 2) - 10;
            }
            
            if (Math.abs(currentY - targetY) > 10) {
                if (Math.abs(currentX - laneX) > 5) {
                    ant.moveTo(new Point(laneX, currentY));
                } else {
                    ant.moveTo(new Point(laneX, targetY));
                }
            } else {
                ant.moveTo(finalDest);
            }
        }
    }

    private double dist(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }
}