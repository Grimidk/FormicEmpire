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
        
        // --- Overworld Estimation ---
        if (ant.getRole() == GameConstants.ROLE_RANCHER) {
             return new Rectangle(0, 0, ROOM_SIZE, ROOM_SIZE); 
        }
        if (ant.getRole() == GameConstants.ROLE_GRAVER) {
             int h = colony.getGameAreaHeight() > 100 ? colony.getGameAreaHeight() : 720;
             return new Rectangle(gameWidth - ROOM_SIZE, h - ROOM_SIZE, ROOM_SIZE, ROOM_SIZE);
        }

        // --- Underworld Estimation ---
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
        
        for (int i = 0; i < 10; i++) {
            int x = (int) (Math.random() * boundX);
            int y = (int) (Math.random() * boundY);
            Point p = new Point(x, y);
            
            if (isOverworldPointValid(colony, p)) {
                return p;
            }
        }
        return new Point(boundX/2, boundY/2);
    }
    
    private boolean isOverworldPointValid(Colony colony, Point p) {
        if (colony.getEntranceBounds() != null && colony.getEntranceBounds().contains(p)) return false; // AntHill
        if (colony.getRancherBounds() != null && colony.getRancherBounds().contains(p)) return false; // Rancher Yard
        if (colony.getGraverBounds() != null && colony.getGraverBounds().contains(p)) return false;   // Graver Yard
        return true;
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
        
        // --- OVERWORLD LOGIC ---
        if (dim == 0) {
            AntRole role = ant.getRole();
            boolean isRancher = role == GameConstants.ROLE_RANCHER;
            boolean isGraver = role == GameConstants.ROLE_GRAVER;
            boolean shouldBeInside = shouldBeInColony(ant) && !isRancher && !isGraver;
            
            Point entrancePoint = new Point(colony.getGameAreaWidth() / 2, colony.getGameAreaHeight() / 2);
            if (colony.getEntranceBounds() != null) {
                Rectangle ent = colony.getEntranceBounds(); 
                entrancePoint = new Point((int)ent.getCenterX(), (int)ent.getCenterY());
            }

            if (shouldBeInside) {
                double dist = dist(ant.getX(), ant.getY(), entrancePoint.x, entrancePoint.y);
                if (dist < 30) {
                    ant.setDimension(1);
                    ant.setPosition(new Point(colony.getGameAreaWidth()/2, 50));
                    ant.moveTo(null); 
                } else {
                    ant.moveTo(entrancePoint);
                }
            } else {
                if (isRancher || isGraver) {
                    Rectangle yard = colony.getTargetRoomForAnt(ant);
                    if (yard != null) {
                        if (isPointInSpecificBounds(colony, yard, ant.getX(), ant.getY())) {
                             if (Math.random() < 0.02) {
                                 Point wanderDest = getSpecificRoomPoint(colony, yard, colony.getGameAreaWidth());
                                 ant.moveTo(wanderDest);
                            }
                        } else {
                            Point yardCenter = new Point((int)yard.getCenterX(), (int)yard.getCenterY());
                            ant.moveTo(yardCenter);
                        }
                    } else {
                        wanderOverworldCarefully(colony, ant);
                    }
                } else {
                    wanderOverworldCarefully(colony, ant);
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
            
            boolean roleBelongsOutside = (ant.getRole() == GameConstants.ROLE_FORAGER || 
                                          ant.getRole() == GameConstants.ROLE_HUNTER ||
                                          ant.getRole() == GameConstants.ROLE_RANCHER ||
                                          ant.getRole() == GameConstants.ROLE_GRAVER);

            if (roleBelongsOutside) {
                Rectangle entranceRect = colony.getEntranceBounds(); 
                Point exitPoint;
                if (entranceRect != null) {
                    exitPoint = new Point((int)entranceRect.getCenterX(), entranceRect.y + 20);
                } else {
                    exitPoint = new Point(colony.getGameAreaWidth() / 2, 20);
                }
                 
                double dist = dist(ant.getX(), ant.getY(), exitPoint.x, exitPoint.y);
                if (dist < 30) {
                    ant.setDimension(0); 
                    ant.setPosition(new Point(colony.getGameAreaWidth()/2, colony.getGameAreaHeight()/2));
                    ant.moveTo(null);
                } else {
                    navigateToPointInUnderworld(colony, ant, exitPoint);
                }
            } else {
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

    private void wanderOverworldCarefully(Colony colony, Ant ant) {
        if (Math.random() < 0.01) {
            Point p = getRandomOverworldPosition(colony, ant.getType().getSprite());
            ant.moveTo(p);
        }
    }
    
    public Point getSpecificRoomPoint(Colony colony, Rectangle r) {
        return getSpecificRoomPoint(colony, r, colony.getGameAreaWidth());
    }

    public Point getSpecificRoomPoint(Colony colony, Rectangle r, int gameWidth) {
        int minY = r.y + padTop;
        int maxY = r.y + r.height - padBottom - antSize;        
        if (maxY < minY) maxY = minY;
        boolean isRightSide = r.getCenterX() > (gameWidth / 2);
        int minX, maxX;
        
        if (isRightSide) {
             minX = r.x + padHall; 
             maxX = r.x + r.width - padBack - antSize;
        } else {
             minX = r.x + padBack;
             maxX = r.x + r.width - padHall - antSize;
        }

        if (maxX < minX) {
            int center = r.x + (r.width/2) - (antSize/2);
            minX = center;
            maxX = center;
        }
        
        int tx = minX + (int)(Math.random() * (maxX - minX + 1));
        int ty = minY + (int)(Math.random() * (maxY - minY + 1));
        
        return new Point(tx, ty);
    }
    
    private boolean isPointInSpecificBounds(Colony colony, Rectangle r, int x, int y) {
        if (r == null) return false;
        
        int gameWidth = colony.getGameAreaWidth();
        boolean isRightSide = r.getCenterX() > (gameWidth / 2);

        int minY = r.y + padTop;
        int maxY = r.y + r.height - padBottom - antSize; 
        if (maxY < minY) maxY = minY;

        int minX, maxX;
        if (isRightSide) {
             minX = r.x + padHall; 
             maxX = r.x + r.width - padBack - antSize;
        } else {
             minX = r.x + padBack;
             maxX = r.x + r.width - padHall - antSize;
        }
        
        if (maxX < minX) maxX = minX; 

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
        
        if (role == GameConstants.ROLE_RANCHER || 
            role == GameConstants.ROLE_GRAVER || 
            role == GameConstants.ROLE_FORAGER || 
            role == GameConstants.ROLE_HUNTER) {
            return false;
        }
        
        return true;
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