package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class ColonyPhysicsService {

    public Point getRandomPosition(Colony colony, ImageIcon sprite) {
        int w = (sprite != null) ? sprite.getIconWidth() : 0;
        int h = (sprite != null) ? sprite.getIconHeight() : 0;
        int boundX = Math.max(1, colony.getGameAreaWidth() - w);
        int boundY = Math.max(1, colony.getGameAreaHeight() - h);
        int x = (int) (Math.random() * boundX);
        int y = (int) (Math.random() * boundY);
        return new Point(x, y);
    }

    public void randomizeAllAntPositions(Colony colony) {
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            AntType type = entry.getKey();
            if (type == GameConstants.TYPE_DEAD) continue;
            ImageIcon sprite = type.getSprite();
            List<Ant> ants = entry.getValue();
            for (Ant ant : ants) {
                ant.setPosition(getRandomPosition(colony, sprite));
            }
        }
    }

    public void runPhysics(Colony colony, int activeDimension) {
        for (List<Ant> antList : colony.getAntGroups().values()) {
            for (Ant ant : antList) {
                if (ant.isAlive() && ant.getDimension() == activeDimension) {
                    ant.updatePosition();
                }
            }
        }
    }

    public void assignRandomMovements(Colony colony) {
        List<AntType> mobileTypes = Arrays.asList(
            GameConstants.TYPE_WORKER, 
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_QUEEN,
            GameConstants.TYPE_PRINCESS,
            GameConstants.TYPE_DRONE
        );

        for (AntType type : mobileTypes) {
            List<Ant> ants = colony.getAntsByType(type);
            for (Ant ant : ants) {
               if (ant.getMoveStatus() == GameConstants.MOVE_STATIC && !ant.isMoving()) {
                    if (Math.random() < 0.001) { 
                        Point randomDest = getRandomPosition(colony, type.getSprite());
                        ant.moveTo(randomDest);
                    }
                }
            }
        }
    }
}