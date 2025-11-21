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

    public void randomizeAllAntPositions(Colony colony) {
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            AntType type = entry.getKey();
            if (type == GameConstants.TYPE_DEAD) continue;
            
            ImageIcon sprite = type.getSprite();
            List<Ant> ants = entry.getValue();
            
            for (Ant ant : ants) {
                if (ant.getDimension() == 1) {
                    Rectangle targetRoom = colony.getTargetRoomForAnt(ant);
                    if (targetRoom != null) {
                        Point safeSpot = getSpecificRoomPoint(colony, targetRoom);
                        ant.setPosition(safeSpot);
                    } else {
                        ant.setPosition(new Point(0,0)); 
                    }
                } else {
                    ant.setPosition(getRandomOverworldPosition(colony, sprite));
                }
            }
        }
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
                if (ant.isAlive()) {
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
            Rectangle targetRoom = colony.getTargetRoomForAnt(ant);
            
            if (!shouldBeInside) {
                 Rectangle entranceRect = colony.getEntranceBounds();
                 if (entranceRect == null) return; 
                 
                 Point exitPoint = new Point((int)entranceRect.getCenterX(), entranceRect.y);
                 
                 double dist = dist(ant.getX(), ant.getY(), exitPoint.x, exitPoint.y);
                 if (dist < 15) {
                     ant.setDimension(0);
                     ant.setPosition(overworldEntrance);
                     ant.moveTo(null);
                 } else {
                     navigateToPointInUnderworld(colony, ant, exitPoint);
                 }
            } else {
                if (targetRoom == null) return;

                if (ant.getType() == GameConstants.TYPE_EGG || ant.getType() == GameConstants.TYPE_PUPA) {
                     if (!isPointInSpecificBounds(colony, targetRoom, ant.getX(), ant.getY())) {
                         Point safeSpot = getSpecificRoomPoint(colony, targetRoom);
                         ant.setPosition(safeSpot);
                         ant.moveTo(null);
                     } else {
                         if (ant.getType() == GameConstants.TYPE_LARVA && Math.random() < 0.01) {
                             ant.moveTo(getSpecificRoomPoint(colony, targetRoom));
                         }
                     }
                     return; 
                }

                if (targetRoom.contains(ant.getX(), ant.getY())) {
                    if (Math.random() < 0.02) {
                         Point wanderDest = getSpecificRoomPoint(colony, targetRoom);
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
        Rectangle entrance = colony.getEntranceBounds();
        if (entrance == null) return new Point(r.x, r.y); 
        
        int padTop = 64;
        int padBottom = 64;
        int padHallSide = 32;
        int padAwaySide = 96;
        
        int minX, maxX, minY, maxY;
        
        minY = r.y + padTop;
        maxY = r.y + r.height - padBottom;

        if (r.getCenterX() < entrance.getCenterX()) {
            minX = r.x + padAwaySide;
            maxX = r.x + r.width - padHallSide;
        } else {
            minX = r.x + padHallSide;
            maxX = r.x + r.width - padAwaySide;
        }
        
        if (maxX <= minX) { minX = r.x; maxX = r.x + r.width; }
        if (maxY <= minY) { minY = r.y; maxY = r.y + r.height; }

        int tx = minX + (int)(Math.random() * (maxX - minX));
        int ty = minY + (int)(Math.random() * (maxY - minY));
        
        return new Point(tx, ty);
    }
    
    private boolean isPointInSpecificBounds(Colony colony, Rectangle r, int x, int y) {
        Rectangle entrance = colony.getEntranceBounds();
        if (entrance == null) return r.contains(x, y);
        
        int padTop = 64;
        int padBottom = 64;
        int padHallSide = 32;
        int padAwaySide = 96;
        
        int minX, maxX, minY, maxY;
        
        minY = r.y + padTop;
        maxY = r.y + r.height - padBottom;

        if (r.getCenterX() < entrance.getCenterX()) {
            minX = r.x + padAwaySide;
            maxX = r.x + r.width - padHallSide;
        } else {
            minX = r.x + padHallSide;
            maxX = r.x + r.width - padAwaySide;
        }
        
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
        if (entranceRect == null) return;

        int currentX = ant.getX();
        int currentY = ant.getY();
        int centerX = (int)entranceRect.getCenterX();
        
        if (Math.abs(currentX - centerX) > 20) {
            ant.moveTo(new Point(centerX, currentY));
            return;
        }

        if (Math.abs(currentY - finalDest.y) > 20) {
            ant.moveTo(new Point(centerX, finalDest.y));
            return;
        }
        
        ant.moveTo(finalDest);
    }

    private double dist(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }

    public void assignRandomMovements(Colony colony) {
       // Logic delegated to updateAntLogic
    }
}