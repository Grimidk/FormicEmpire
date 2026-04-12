package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Bug;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.entities.services.ViewportPhysicsLod;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class GameAreaPanel extends ZeroGamePanel {

    private Image backgroundImage;
    private final Map<String, Image> biomeTextureCache = new HashMap<>();
    
    // --- Images ---
    private Image basicRoomImg;
    private Image doubleRoomImg;
    private Image firstHallwayImg;
    private Image middleHallwayImg;    
    private Image antHillImg;
    private Image basicYardImg;    
    private Image deadBodyImg;
    
    private Colony colony;
    private Engine engine;
    private Dimension currentDimension = WorldSpaces.OVERWORLD; 
    private String currentBiomeName = "Plains";
    private Rectangle paintViewportRect;
    private Rectangle lodViewportRect;

    private final int ANCHOR_WIDTH = 550;
    private final int ANCHOR_HEIGHT = 500;

    private static final int OVERWORLD_PAN_OUTSET = 1400;

    private int overworldLayoutOffsetX;
    private int overworldLayoutOffsetY;

    // --- Bounds ---
    public Rectangle entranceBounds;
    public Rectangle room1Bounds; 
    public Rectangle room2Bounds; 
    public Rectangle room3Bounds; 
    public Rectangle room4Bounds; 
    public Rectangle rancherYardBounds;
    public Rectangle graverYardBounds;
    public Rectangle breederRoomBounds;
    public Rectangle transitRoomBounds;

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
        doubleRoomImg = loadImage("/sprites/buildings/doubleRoom.png");
        firstHallwayImg = loadImage("/sprites/buildings/firstHallway.png");
        middleHallwayImg = loadImage("/sprites/buildings/middleHallway.png");
        antHillImg = loadImage("/sprites/buildings/antHill.png");
        basicYardImg = loadImage("/sprites/buildings/basicYard.png");

        deadBodyImg = loadImage("/sprites/ants/dead.png");
    }

    private Image loadImage(String path) {        try {
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

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setPaintViewportRect(Rectangle viewRect) {
        this.paintViewportRect = viewRect != null ? new Rectangle(viewRect) : null;
        this.lodViewportRect = computeLodViewportRect();
    }

    public Rectangle getLodViewportRect() {
        return lodViewportRect != null ? new Rectangle(lodViewportRect) : null;
    }

    private Rectangle computeLodViewportRect() {
        if (paintViewportRect == null) {
            return null;
        }
        if (currentDimension != WorldSpaces.OVERWORLD) {
            return new Rectangle(paintViewportRect);
        }
        int pw = getWidth();
        int ph = getHeight();
        if (pw <= 0 || ph <= 0) {
            return null;
        }
        int padX = Math.max(0, (pw - paintViewportRect.width) / 2);
        int padY = Math.max(0, (ph - paintViewportRect.height) / 2);
        return new Rectangle(
                paintViewportRect.x - padX,
                paintViewportRect.y - padY,
                paintViewportRect.width,
                paintViewportRect.height);
    }
    
    public void resetView() {
        this.currentDimension = WorldSpaces.OVERWORLD;
        this.currentBiomeName = "Plains";
        this.colony = null;
        this.paintViewportRect = null;
        this.lodViewportRect = null;
        this.overworldLayoutOffsetX = 0;
        this.overworldLayoutOffsetY = 0;
        this.backgroundImage = biomeTextureCache.get("Plains");
        repaint();
    }

    public int getOverworldLayoutOffsetX() {
        return overworldLayoutOffsetX;
    }

    public int getOverworldLayoutOffsetY() {
        return overworldLayoutOffsetY;
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
            int pad = Math.max(OVERWORLD_PAN_OUTSET, Math.max(viewportWidth, viewportHeight) / 2);
            int vw = viewportWidth + 2 * pad;
            int vh = viewportHeight + 2 * pad;
            if (getWidth() != vw || getHeight() != vh) {
                setPreferredSize(new java.awt.Dimension(vw, vh));
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
        
        boolean hasBreeder = colony != null && colony.hasUpgrade(GameUnlocks.ROLE_BREEDER);
        boolean hasTunnels = colony != null && colony.getDynasty() != null && !colony.getDynasty().getTunnels().isEmpty();

        if (hasBreeder || hasTunnels) {
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
        
        int contentPadX = 0;
        int contentPadY = 0;
        if (colony != null && currentDimension == WorldSpaces.OVERWORLD && paintViewportRect != null) {
            int vpw = paintViewportRect.width;
            int vph = paintViewportRect.height;
            contentPadX = Math.max(0, (getWidth() - vpw) / 2);
            contentPadY = Math.max(0, (getHeight() - vph) / 2);
        }
        overworldLayoutOffsetX = contentPadX;
        overworldLayoutOffsetY = contentPadY;

        if (colony != null) {
            if (currentDimension == WorldSpaces.UNDERWORLD) {
                drawUnderworldStructure(g2d);
                drawAnts(g2d);
                drawBugs(g2d);
            } else {
                g2d.translate(contentPadX, contentPadY);
                drawOverworldStructure(g2d);
                drawResourceSources(g2d);
                drawAnts(g2d);
                drawBugs(g2d);
                g2d.translate(-contentPadX, -contentPadY);
            }

            colony.setRoomBounds(entranceBounds, room1Bounds, room2Bounds, room3Bounds, room4Bounds, rancherYardBounds, graverYardBounds, breederRoomBounds, transitRoomBounds);
        }

        if (currentDimension == WorldSpaces.OVERWORLD && engine != null && engine.getWorld() != null) {
            drawEnvironmentalOverlays(g2d);
        }
    }

    private void drawResourceSources(Graphics2D g2d) {
        if (colony == null || colony.getLocationService() == null) {
            return;
        }
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        for (ResourceSource src : colony.getLocationService().getDiscoveredSources()) {
            if (src.getQuantity() <= 0) {
                continue;
            }
            ImageIcon icon = src.getIconForDisplay();
            if (icon == null) {
                continue;
            }
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            int sx = src.getX();
            int sy = src.getY();
            int cx = sx + w / 2;
            int cy = sy + h / 2;
            int side = Math.max(1, (int) Math.ceil(Math.hypot(w, h)));
            int bx = cx - (side + 1) / 2;
            int by = cy - (side + 1) / 2;
            if (!ViewportPhysicsLod.antIntersectsViewport(lodViewportRect, bx, by, side, side)) {
                continue;
            }
            AffineTransform oldTx = g2d.getTransform();
            double deg = resourceSourceRotationDegrees(src);
            g2d.translate(cx, cy);
            g2d.rotate(Math.toRadians(deg));
            g2d.drawImage(icon.getImage(), -w / 2, -h / 2, w, h, this);
            g2d.setTransform(oldTx);
        }
    }

    private static double resourceSourceRotationDegrees(ResourceSource src) {
        int h = Objects.hash(src.getX(), src.getY(), src.getResourceType());
        return Math.floorMod(h, 360);
    }

    private void drawEnvironmentalOverlays(Graphics2D g2d) {
        if (engine == null) return;
        boolean dayOn = engine.isDaylightColorOverlayEnabled();
        boolean weatherOn = engine.isWeatherColorOverlayEnabled();
        if (!dayOn && !weatherOn) return;

        World world = engine.getWorld();
        if (world == null) return;

        if (dayOn && world.getTimeOfDay() != null && world.getTimeOfDay().getOverlayColor() != null) {
            g2d.setColor(world.getTimeOfDay().getOverlayColor());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        if (weatherOn) {
            Weather w = world.getWeather();
            if (world.getActiveHex() != null && world.getActiveHex().getLocalWeather() != null) {
                w = world.getActiveHex().getLocalWeather();
            }

            if (w != null && w.getOverlayColor() != null) {
                g2d.setColor(w.getOverlayColor());
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
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

                Rectangle deadDrawArea = new Rectangle(safeX, safeY, Math.max(1, safeW), Math.max(1, safeH));
                boolean drawDeadPile = !ViewportPhysicsLod.isLodActive(lodViewportRect)
                    || ViewportPhysicsLod.expandViewport(lodViewportRect, ViewportPhysicsLod.MARGIN_PX).intersects(deadDrawArea);

                if (drawDeadPile) {
                    drawStaticItemsLocal(g2d, deadBodyImg, safeX, safeY, safeW, safeH, colony.getDeadAnts().size());
                }
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
        if (firstHallwayImg == null || basicRoomImg == null || middleHallwayImg == null || doubleRoomImg == null) return;

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
        
        // --- ROW 3 (Floor 3) ---
        boolean hasBreeder = colony.hasUpgrade(GameUnlocks.ROLE_BREEDER);
        boolean hasTunnels = colony.getDynasty() != null && !colony.getDynasty().getTunnels().isEmpty();

        if (hasBreeder || hasTunnels) {
            int thirdRowYOffset = 512;
            int roomY3 = hallY + thirdRowYOffset;
            
            g2d.drawImage(middleHallwayImg, hallX, roomY3, this);            
            
            if (hasBreeder) {
                g2d.drawImage(basicRoomImg, leftRoomX, roomY3, this);
                breederRoomBounds = new Rectangle(leftRoomX, roomY3, roomW, roomH);
            } else {
                breederRoomBounds = null;
            }

            if (hasTunnels) {
                int dRoomW = doubleRoomImg.getWidth(this);
                int dRoomH = doubleRoomImg.getHeight(this);
                
                AffineTransform old3 = g2d.getTransform();
                double rotateCenter3X = rightRoomX + (dRoomW / 2.0);
                double rotateCenter3Y = roomY3 + (dRoomH / 2.0);
                
                g2d.rotate(Math.toRadians(180), rotateCenter3X, rotateCenter3Y);
                g2d.drawImage(doubleRoomImg, rightRoomX, roomY3, this);
                transitRoomBounds = new Rectangle(rightRoomX, roomY3, dRoomW, dRoomH);
                g2d.setTransform(old3);
            } else {
                transitRoomBounds = null;
            }
        } else {
            breederRoomBounds = null;
            transitRoomBounds = null;
        }
    }

    private void drawAnts(Graphics2D g2d) {
        if (colony == null) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        for (AntType type : GameConstants.getAntTypes()) {
            if (type == GameConstants.TYPE_DEAD) continue; 

            ImageIcon spriteIcon = GameConstants.getAntSprite(type, colony.getSpecies());
            if (spriteIcon == null) continue; 
            
            Image sprite = spriteIcon.getImage();
            int w = spriteIcon.getIconWidth();
            int h = spriteIcon.getIconHeight();
            
            List<Species> assimilatedSpecies = new ArrayList<>();
            if (type == GameConstants.TYPE_DRONE && colony.getDynasty() != null) {
                for (Species s : GameConstants.getSpecies()) {
                    if (s == colony.getSpecies()) continue;
                    if (s.getAssimilation() != null && colony.getDynasty().isAssimilationCompleted(s.getAssimilation())) {
                        assimilatedSpecies.add(s);
                    }
                }
            }

            List<Ant> ants = colony.getAntsByType(type);
            for (Ant ant : ants) {
                if (ant.getDimension() != currentDimension) continue;

                if (!ViewportPhysicsLod.antIntersectsViewport(lodViewportRect, ant.getX(), ant.getY(), w, h)) {
                    continue;
                }
                
                Image currentSprite = sprite;
                if (!assimilatedSpecies.isEmpty()) {
                    int seed = System.identityHashCode(ant);
                    if (Math.abs(seed) % 10 < 3) {
                        int index = (Math.abs(seed) / 10) % assimilatedSpecies.size();
                        Species as = assimilatedSpecies.get(index);
                        ImageIcon asIcon = GameConstants.getAntSprite(type, as);
                        if (asIcon != null) {
                            currentSprite = asIcon.getImage();
                        }
                    }
                }
                
                AffineTransform oldTransform = g2d.getTransform();
                double centerX = ant.getX() + (w / 2.0);
                double centerY = ant.getY() + (h / 2.0);
                g2d.translate(centerX, centerY);       
                g2d.rotate(Math.toRadians(ant.getR()));
                g2d.drawImage(currentSprite, -w / 2, -h / 2, this);

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

            if (!ViewportPhysicsLod.antIntersectsViewport(lodViewportRect, bug.getX(), bug.getY(), w, h)) {
                continue;
            }
            
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
