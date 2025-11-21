package com.grimidk.formicempire.classes.interfaces.game.panels;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameAreaPanel extends JPanel {

    private Image backgroundImage;
    private final Map<String, Image> biomeTextureCache = new HashMap<>();
    
    private Image basicRoomImg;
    private Image firstHallwayImg;
    private Image middleHallwayImg;
    
    private Colony colony;
    
    private int currentDimension = 0; 
    private String currentBiomeName = "Plains";

    public Rectangle entranceBounds;
    public Rectangle middleHallwayBounds;
    public Rectangle room1Bounds; 
    public Rectangle room2Bounds; 
    public Rectangle room3Bounds; 
    public Rectangle room4Bounds; 

    public GameAreaPanel() {
        loadImages();
        
        this.backgroundImage = biomeTextureCache.getOrDefault("Plains", null);
        
        setOpaque(true);
    }

    private void loadImages() {
        biomeTextureCache.put("Plains", loadImage("/backgrounds/biomes/PlainsTile.png"));
        biomeTextureCache.put("Forest", loadImage("/backgrounds/biomes/ForestTile.png"));
        biomeTextureCache.put("Jungle", loadImage("/backgrounds/biomes/JungleTile.png"));
        biomeTextureCache.put("Swamp", loadImage("/backgrounds/biomes/SwampTile.png"));
        biomeTextureCache.put("Tundra", loadImage("/backgrounds/biomes/TundraTile.png"));
        biomeTextureCache.put("Taiga", loadImage("/backgrounds/biomes/TaigaTile.png"));
        biomeTextureCache.put("Dessert", loadImage("/backgrounds/biomes/DessertTile.png")); 
        biomeTextureCache.put("Urban", loadImage("/backgrounds/biomes/UrbanTile.png"));
        biomeTextureCache.put("Underground", loadImage("/backgrounds/colony/UndergroundTile.png"));
        
        basicRoomImg = loadImage("/sprites/buildings/basicRoom.png");
        firstHallwayImg = loadImage("/sprites/buildings/firstHallway.png");
        middleHallwayImg = loadImage("/sprites/buildings/middleHallway.png");
    }

    private Image loadImage(String path) {
        try {
            URL imgUrl = getClass().getResource(path);
            if (imgUrl != null) {
                return new ImageIcon(imgUrl).getImage();
            } else {
                System.err.println("CRITICAL ERROR: Resource not found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void setColony(Colony colony) {
        this.colony = colony;
    }
    
    public void toggleDimension() {
        if (currentDimension == 0) {
            currentDimension = 1;
        } else {
            currentDimension = 0;
        }
        updateBackground();
        repaint();
    }
    
    public int getCurrentDimension() {
        return currentDimension;
    }

    public void setBackgroundByBiome(String biomeName) {
        this.currentBiomeName = biomeName;
        updateBackground();
    }
    
    private void updateBackground() {
        if (currentDimension == 1) {
            this.backgroundImage = biomeTextureCache.get("Underground");
        } else {
            this.backgroundImage = biomeTextureCache.getOrDefault(currentBiomeName, biomeTextureCache.get("Plains"));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            int tileWidth = backgroundImage.getWidth(this);
            int tileHeight = backgroundImage.getHeight(this);
            
            if (tileWidth > 0 && tileHeight > 0) { 
                int panelWidth = getWidth();
                int panelHeight = getHeight();

                for (int x = 0; x < panelWidth; x += tileWidth) {
                    for (int y = 0; y < panelHeight; y += tileHeight) {
                        g.drawImage(backgroundImage, x, y, this);
                    }
                }
            }
        }
        
        if (currentDimension == 1) {
            drawUnderworldStructure(g2d);
            if (colony != null && entranceBounds != null) {
                // Mapping as requested:
                // Room 1 (Top Left): Storage
                // Room 2 (Top Right): Farm
                // Room 3 (Bottom Left): Nursery
                // Room 4 (Bottom Right): Royal Chamber
                colony.setRoomBounds(entranceBounds, room1Bounds, room2Bounds, room3Bounds, room4Bounds);
            }
        }
        
        drawAnts(g2d);
    }

    private void drawUnderworldStructure(Graphics2D g2d) {
        if (firstHallwayImg == null || basicRoomImg == null) return;

        int panelWidth = getWidth();
        int topMargin = 0; 
        
        int hallW = firstHallwayImg.getWidth(this);
        int hallH = firstHallwayImg.getHeight(this);
        int centerX = panelWidth / 2;
        int hallX = centerX - (hallW / 2);
        int hallY = topMargin;
        
        g2d.drawImage(firstHallwayImg, hallX, hallY, this);
        entranceBounds = new Rectangle(hallX, hallY, hallW, hallH);
        
        int roomW = basicRoomImg.getWidth(this);
        int roomH = basicRoomImg.getHeight(this);
        
        // --- ROW 1 ---
        int roomY = hallY;
        
        int leftRoomX = hallX - roomW;
        g2d.drawImage(basicRoomImg, leftRoomX, roomY, this);
        room1Bounds = new Rectangle(leftRoomX, roomY, roomW, roomH);
        
        int rightRoomX = hallX + hallW;
        
        AffineTransform old = g2d.getTransform();
        
        double rotateCenterX = rightRoomX + (roomW / 2.0);
        double rotateCenterY = roomY + (roomH / 2.0);
        
        g2d.rotate(Math.toRadians(180), rotateCenterX, rotateCenterY);
        g2d.drawImage(basicRoomImg, rightRoomX, roomY, this);
        room2Bounds = new Rectangle(rightRoomX, roomY, roomW, roomH);
        
        g2d.setTransform(old);

        // --- ROW 2 ---
        int secondRowYOffset = 256; 
        int roomY2 = hallY + secondRowYOffset;

        g2d.drawImage(basicRoomImg, leftRoomX, roomY2, this);
        room3Bounds = new Rectangle(leftRoomX, roomY2, roomW, roomH);

        AffineTransform old2 = g2d.getTransform();
        
        double rotateCenter2X = rightRoomX + (roomW / 2.0);
        double rotateCenter2Y = roomY2 + (roomH / 2.0);
        
        g2d.rotate(Math.toRadians(180), rotateCenter2X, rotateCenter2Y);
        g2d.drawImage(basicRoomImg, rightRoomX, roomY2, this);
        room4Bounds = new Rectangle(rightRoomX, roomY2, roomW, roomH);
        
        g2d.setTransform(old2);
    }

    private void drawAnts(Graphics2D g2d) {
        if (colony == null) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        for (AntType type : GameConstants.getAntTypes()) {
            if (type == GameConstants.TYPE_DEAD) continue; 

            ImageIcon spriteIcon = type.getSprite();
            if (spriteIcon == null) continue; 
            
            Image sprite = spriteIcon.getImage();
            int w = spriteIcon.getIconWidth();
            int h = spriteIcon.getIconHeight();

            List<Ant> ants = colony.getAntsByType(type);
            for (Ant ant : ants) {
                if (ant.getDimension() != currentDimension) continue;
                
                AffineTransform oldTransform = g2d.getTransform();
                
                double centerX = ant.getX() + (w / 2.0);
                double centerY = ant.getY() + (h / 2.0);
                g2d.translate(centerX, centerY);
                
                g2d.rotate(Math.toRadians(ant.getR()));
                
                g2d.drawImage(sprite, -w / 2, -h / 2, this);
                
                g2d.setTransform(oldTransform);
            }
        }
    }
}