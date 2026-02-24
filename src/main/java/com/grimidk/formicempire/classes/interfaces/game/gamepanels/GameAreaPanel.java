package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Bug;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameAreaPanel extends ZeroGamePanel {

    private Image backgroundImage;
    private final Map<String, Image> biomeTextureCache = new HashMap<>();
    
    // --- Images ---
    private Image basicRoomImg;
    private Image firstHallwayImg;
    private Image middleHallwayImg;    
    private Image antHillImg;
    private Image basicYardImg;    
    private Image deadBodyImg;
    
    private Colony colony;
    private Dimension currentDimension = WorldSpaces.OVERWORLD; 
    private String currentBiomeName = "Plains";

    // --- Fixed Layout Anchors ---
    // These ensure that structural components never move when the window resizes,
    // keeping them perfectly synced with absolute ant coordinates.
    private final int ANCHOR_WIDTH = 550;
    private final int ANCHOR_HEIGHT = 500;

    // --- Bounds ---
    public Rectangle entranceBounds;
    public Rectangle room1Bounds; 
    public Rectangle room2Bounds; 
    public Rectangle room3Bounds; 
    public Rectangle room4Bounds; 
    public Rectangle rancherYardBounds;
    public Rectangle graverYardBounds;
    public Rectangle breederRoomBounds;

    public GameAreaPanel() {
        super(null); 
        setOpaque(true);
        initComponents();
        initLayout();
    }
    
    @Override
    protected void initComponents() {
        loadImages();
        this.backgroundImage = biomeTextureCache.getOrDefault("Plains", null);
    }
    
    @Override
    protected void initLayout() {
    }

    private void loadImages() {
        biomeTextureCache.put("Plains", loadImage("/backgrounds/biomes/PlainsTile.png"));
        biomeTextureCache.put("Forest", loadImage("/backgrounds/biomes/ForestTile.png"));
        biomeTextureCache.put("Jungle", loadImage("/backgrounds/biomes/JungleTile.png"));
        biomeTextureCache.put("Swamp", loadImage("/backgrounds/biomes/SwampTile.png"));
        biomeTextureCache.put("Tundra", loadImage("/backgrounds/biomes/TundraTile.png"));
        biomeTextureCache.put("Taiga", loadImage("/backgrounds/biomes/TaigaTile.png"));
        biomeTextureCache.put("Desert", loadImage("/backgrounds/biomes/DessertTile.png")); 
        biomeTextureCache.put("Urban", loadImage("/backgrounds/biomes/UrbanTile.png"));
        biomeTextureCache.put("Mountain", loadImage("/backgrounds/biomes/MountainTile.png"));
        biomeTextureCache.put("Volcanic", loadImage("/backgrounds/biomes/VolcanicTile.png"));
        biomeTextureCache.put("Lake", loadImage("/backgrounds/biomes/LakeTile.png"));
        biomeTextureCache.put("Ocean", loadImage("/backgrounds/biomes/OceanTile.png"));
        
        biomeTextureCache.put("Underground", loadImage("/backgrounds/colony/UndergroundTile.png"));
        
        basicRoomImg = loadImage("/sprites/buildings/basicRoom.png");
        firstHallwayImg = loadImage("/sprites/buildings/firstHallway.png");
        middleHallwayImg = loadImage("/sprites/buildings/middleHallway.png");  
        antHillImg = loadImage("/sprites/buildings/antHill.png");
        basicYardImg = loadImage("/sprites/buildings/basicYard.png");
    
        deadBodyImg = loadImage("/sprites/ants/dead.png");
    }

    private Image loadImage(String path) {
        try {
            URL imgUrl = getClass().getResource(path);
            if (imgUrl != null) {
                return new ImageIcon(imgUrl).getImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void setColony(Colony colony) {
        this.colony = colony;
    }
    
    public void resetView() {
        this.currentDimension = WorldSpaces.OVERWORLD;
        this.currentBiomeName = "Plains";
        this.colony = null;
        this.backgroundImage = biomeTextureCache.get("Plains");
        repaint();
    }
    
    public void toggleDimension() {
        currentDimension = (currentDimension == WorldSpaces.OVERWORLD) ? WorldSpaces.UNDERWORLD : WorldSpaces.OVERWORLD;
        updateBackground();
        repaint();
    }
    
    public Dimension getCurrentDimension() {
        return currentDimension;
    }

    public void setBackgroundByBiome(String biomeName) {
        this.currentBiomeName = biomeName;
        updateBackground();
    }
    
    private void updateBackground() {
        if (currentDimension == WorldSpaces.UNDERWORLD) {
            this.backgroundImage = biomeTextureCache.get("Underground");
        } else {
            this.backgroundImage = biomeTextureCache.getOrDefault(currentBiomeName, biomeTextureCache.get("Plains"));
        }
    }
    
    public void refreshSize(int viewportWidth, int viewportHeight) {
        if (currentDimension == WorldSpaces.OVERWORLD) {
            if (getWidth() != viewportWidth || getHeight() != viewportHeight) {
                setPreferredSize(new java.awt.Dimension(viewportWidth, viewportHeight));
                revalidate();
            }
        } else {
            int requiredHeight = calculateUnderworldHeight();
            int finalHeight = Math.max(requiredHeight, viewportHeight);
            
            if (getWidth() != viewportWidth || getHeight() != finalHeight) {
                setPreferredSize(new java.awt.Dimension(viewportWidth, finalHeight));
                revalidate();
            }
        }
    }

    private int calculateUnderworldHeight() {
        int maxY = 512; 
        
        int roomHeight = (basicRoomImg != null) ? basicRoomImg.getHeight(this) : 200;
        
        if (colony != null && colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            maxY = 512 + 256;
        }
        
        return maxY + roomHeight + 50;
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
        
        if (colony != null) {
            if (currentDimension == WorldSpaces.UNDERWORLD) {
                drawUnderworldStructure(g2d);
            } else {
                drawOverworldStructure(g2d);
            }
            
            colony.setRoomBounds(entranceBounds, room1Bounds, room2Bounds, room3Bounds, room4Bounds, rancherYardBounds, graverYardBounds, breederRoomBounds);
        }
        
        drawAnts(g2d);
        drawBugs(g2d);
    }
    
    private void drawOverworldStructure(Graphics2D g2d) {
        // --- Entrance ---
        if (antHillImg != null) {
            int w = antHillImg.getWidth(this);
            int h = antHillImg.getHeight(this);
            int x = (ANCHOR_WIDTH / 2) - (w / 2);
            int y = (ANCHOR_HEIGHT / 2) - (h / 2);
            
            g2d.drawImage(antHillImg, x, y, this);            
            entranceBounds = new Rectangle(x, y, w, h);
        }
        
        // --- Rancher Yard ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_RANCHER) && basicYardImg != null) {
            int w = basicYardImg.getWidth(this);
            int h = basicYardImg.getHeight(this);
            int x = 10; 
            int y = 10; 
            
            g2d.drawImage(basicYardImg, x, y, this);
            rancherYardBounds = new Rectangle(x, y, w, h);
                        
        } else {
            rancherYardBounds = null;
        }

        // --- Graver Yard ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_GRAVER) && basicYardImg != null) {
            int w = basicYardImg.getWidth(this);
            int h = basicYardImg.getHeight(this);
            int x = ANCHOR_WIDTH - w - 10;
            int y = ANCHOR_HEIGHT - h - 10;
            
            AffineTransform old = g2d.getTransform();
            double rotateCenterX = x + (w / 2.0);
            double rotateCenterY = y + (h / 2.0);
            
            g2d.rotate(Math.toRadians(180), rotateCenterX, rotateCenterY);
            g2d.drawImage(basicYardImg, x, y, this);
            
            graverYardBounds = new Rectangle(x, y, w, h);
            
            if (deadBodyImg != null && colony.getDeadAnts() != null && !colony.getDeadAnts().isEmpty()) {
                int padBack = 64; 
                int padHall = 16;
                int padTop = 48;
                int padBottom = 48;
                
                int safeX = x + padBack;
                int safeY = y + padTop;
                int safeW = w - padBack - padHall;
                int safeH = h - padTop - padBottom;
                
                drawStaticItemsLocal(g2d, deadBodyImg, safeX, safeY, safeW, safeH, colony.getDeadAnts().size());
            }

            g2d.setTransform(old);
            
        } else {
            graverYardBounds = null;
        }
    }
    
    private void drawStaticItemsLocal(Graphics2D g2d, Image img, int rx, int ry, int rw, int rh, int count) {
        int imgW = img.getWidth(this);
        int imgH = img.getHeight(this);
        
        int areaW = Math.max(1, rw - imgW);
        int areaH = Math.max(1, rh - imgH);

        for (int i = 0; i < count; i++) {
            long seed = i * 999999L;
            Random rng = new Random(seed);
            
            int dx = rng.nextInt(areaW);
            int dy = rng.nextInt(areaH);
            
            float angle = rng.nextFloat() * 360.0f; 
            AffineTransform old = g2d.getTransform();
            
            double centerX = rx + dx + (imgW / 2.0);
            double centerY = ry + dy + (imgH / 2.0);
            
            g2d.translate(centerX, centerY);
            g2d.rotate(Math.toRadians(angle));            
            g2d.drawImage(img, -imgW / 2, -imgH / 2, this);
            g2d.setTransform(old);
        }
    }

    private void drawUnderworldStructure(Graphics2D g2d) {
        if (firstHallwayImg == null || basicRoomImg == null || middleHallwayImg == null) return;

        int topMargin = 0; 
        
        // --- ROW 1 (Floor 1) ---
        int hallW = firstHallwayImg.getWidth(this);
        int hallH = firstHallwayImg.getHeight(this);
        int centerX = ANCHOR_WIDTH / 2;
        int hallX = centerX - (hallW / 2);
        int hallY = topMargin;
        
        g2d.drawImage(firstHallwayImg, hallX, hallY, this);
        entranceBounds = new Rectangle(hallX, hallY, hallW, hallH);
        
        int roomW = basicRoomImg.getWidth(this);
        int roomH = basicRoomImg.getHeight(this);
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

        // --- ROW 2 (Floor 2) ---
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
        
        // --- ROW 3 (Floor 3 ) ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            int thirdRowYOffset = 512;
            int roomY3 = hallY + thirdRowYOffset;
            
            g2d.drawImage(middleHallwayImg, hallX, roomY3, this);            
            g2d.drawImage(basicRoomImg, leftRoomX, roomY3, this);
            breederRoomBounds = new Rectangle(leftRoomX, roomY3, roomW, roomH);
        } else {
            breederRoomBounds = null;
        }
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
                
                ResourceType carried = ant.getCarrying();
                if (carried != null && carried.getIcon() != null) {
                    g2d.rotate(Math.toRadians(-ant.getR())); 
                    Image resourceIcon = carried.getIcon().getImage();
                    
                    if (resourceIcon != null) {
                        int iconW = 20; 
                        int iconH = 20;
                        
                        g2d.drawImage(resourceIcon, -iconW/2, -h/2 - iconH, iconW, iconH, this);
                        
                        ResourceType carriedSec = ant.getCarryingSec();
                        if (carriedSec != null && carriedSec.getIcon() != null) {
                            Image secIcon = carriedSec.getIcon().getImage();
                            if (secIcon != null) {
                                g2d.drawImage(secIcon, -iconW/2 + 10, -h/2 - iconH + 5, iconW, iconH, this);
                            }
                        }
                    }
                } 
                
                if (ant.getCarryingAnt() != null) {
                    if (ant.getCarrying() == null) {
                        g2d.rotate(Math.toRadians(-ant.getR())); 
                    }
                    
                    ImageIcon carriedSprite = ant.getCarryingAnt().getSprite();
                    if (carriedSprite != null) {
                        Image cSprite = carriedSprite.getImage();
                        int cW = (int)(w * 0.7);
                        int cH = (int)(h * 0.7);
                        g2d.drawImage(cSprite, -cW/2, -h/2 - cH + 5, cW, cH, this);
                    }
                }
                
                g2d.setTransform(oldTransform);
            }
        }
    }

    private void drawBugs(Graphics2D g2d) {
        if (colony == null) return;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        for (Bug bug : colony.getBugs()) {
            if (bug.getDimension() != currentDimension) continue;
            
            ImageIcon spriteIcon = bug.getBugType().getSprite();
            if (spriteIcon == null) continue;
            
            Image sprite = spriteIcon.getImage();
            int w = spriteIcon.getIconWidth();
            int h = spriteIcon.getIconHeight();
            
            AffineTransform oldTransform = g2d.getTransform();
            
            double centerX = bug.getX() + (w / 2.0);
            double centerY = bug.getY() + (h / 2.0);
            g2d.translate(centerX, centerY);
            g2d.rotate(Math.toRadians(bug.getR()));
            g2d.drawImage(sprite, -w / 2, -h / 2, this);
            g2d.setTransform(oldTransform);
        }
    }
}