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
    
    private Colony colony;

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

    public void setBackgroundByBiome(String biomeName) {
        Image newBg = biomeTextureCache.get(biomeName);
        
        if (newBg == null) {
            newBg = biomeTextureCache.get("Plains");
        }
        
        if (this.backgroundImage != newBg) {
            this.backgroundImage = newBg;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            int tileWidth = backgroundImage.getWidth(this);
            int tileHeight = backgroundImage.getHeight(this);
            
            if (tileWidth <= 0 || tileHeight <= 0) return; 

            int panelWidth = getWidth();
            int panelHeight = getHeight();

            for (int x = 0; x < panelWidth; x += tileWidth) {
                for (int y = 0; y < panelHeight; y += tileHeight) {
                    g.drawImage(backgroundImage, x, y, this);
                }
            }
        }
        
        if (colony != null) {
            Graphics2D g2d = (Graphics2D) g;
            
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
}