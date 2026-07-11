package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.BugType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Bug;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.entities.services.colony.ColonySpatialLayout;
// import com.grimidk.formicempire.classes.interfaces.game.rendering.RoomDecorationRenderer;
import com.grimidk.formicempire.classes.entities.services.shared.ViewportPhysicsLod;
import com.grimidk.formicempire.classes.entities.spatial.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.assets.GameSpritePreloader;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public class GameAreaPanel extends ZeroGamePanel {

    private Image backgroundImage;
    private final Map<Integer, Image> biomeTextureCache = new HashMap<>();
    private Image undergroundTexture;
    private Image basicRoomImg;
    private Image doubleRoomImg;
    private Image firstHallwayImg;
    private Image middleHallwayImg;    
    private Image antHillImg;
    private Image basicYardImg;    
    private Image deadBodyImg;
    private Image tunnelSpriteImg;
    
    private Colony colony;
    private Engine engine;
    private Dimension currentDimension = WorldSpaces.OVERWORLD;
    private int currentBiomeId = GameConstants.BIOME_PLAINS.getId();
    private Rectangle paintViewportRect;
    private Rectangle lodViewportRect;

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
    public Rectangle insectPenBounds;

    /** Overworld corpse draw budget in graver yard (reset each frame). */
    private int overworldDeadBodySpritesRemaining;
    private int lastPetPenBoundsSyncKey = Integer.MIN_VALUE;

    public GameAreaPanel() {
        super(null);
        setOpaque(true);
        initComponents();
        initLayout();
    }

    @Override
    protected void initComponents() {
        loadImages();
        updateBackground();
    }

    @Override
    protected void initLayout() {
    }

    private void loadImages() {
        biomeTextureCache.put(GameConstants.BIOME_PLAINS.getId(), loadImage("backgrounds/biomes/PlainsTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_FOREST.getId(), loadImage("backgrounds/biomes/ForestTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_JUNGLE.getId(), loadImage("backgrounds/biomes/JungleTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_SWAMP.getId(), loadImage("backgrounds/biomes/SwampTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_TUNDRA.getId(), loadImage("backgrounds/biomes/TundraTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_TAIGA.getId(), loadImage("backgrounds/biomes/TaigaTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_DESERT.getId(), loadImage("backgrounds/biomes/DesertTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_URBAN.getId(), loadImage("backgrounds/biomes/UrbanTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_MOUNTAIN.getId(), loadImage("backgrounds/biomes/MountainTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_VOLCANIC.getId(), loadImage("backgrounds/biomes/VolcanicTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_LAKE.getId(), loadImage("backgrounds/biomes/LakeTile.png"));
        biomeTextureCache.put(GameConstants.BIOME_OCEAN.getId(), loadImage("backgrounds/biomes/OceanTile.png"));
        undergroundTexture = loadImage("backgrounds/colony/UndergroundTile.png");

        basicRoomImg = loadImage("sprites/buildings/BasicRoom.png");
        doubleRoomImg = loadImage("sprites/buildings/DoubleRoom.png");
        firstHallwayImg = loadImage("sprites/buildings/FirstHallway.png");
        middleHallwayImg = loadImage("sprites/buildings/MiddleHallway.png");
        antHillImg = loadImage("sprites/buildings/AntHill.png");
        basicYardImg = loadImage("sprites/buildings/BasicYard.png");
        tunnelSpriteImg = loadImage("sprites/buildings/TunnelSprite.png");

        deadBodyImg = loadImage("sprites/ants/Dead.png");
    }

    private Image loadImage(String classpathRelativePath) {
        try {
            URL imgUrl = resolveResourceUrl(classpathRelativePath);
            if (imgUrl != null) {
                Image image = new ImageIcon(imgUrl).getImage();
                GameSpritePreloader.ensureLoaded(image);
                return image;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static URL resolveResourceUrl(String classpathRelativePath) {
        URL u = tryResourceUrl(classpathRelativePath);
        if (u != null) {
            return u;
        }
        String alt = alternateFilenameFirstLetterCase(classpathRelativePath);
        if (alt != null && !alt.equals(classpathRelativePath)) {
            u = tryResourceUrl(alt);
        }
        return u;
    }

    private static URL tryResourceUrl(String classpathRelativePath) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            URL u = cl.getResource(classpathRelativePath);
            if (u != null) {
                return u;
            }
        }
        return GameAreaPanel.class.getResource("/" + classpathRelativePath);
    }

    private static String alternateFilenameFirstLetterCase(String path) {
        int sep = path.lastIndexOf('/');
        int nameStart = sep + 1;
        if (nameStart >= path.length()) {
            return null;
        }
        char c = path.charAt(nameStart);
        if (!Character.isLetter(c)) {
            return null;
        }
        char toggled = Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c);
        return path.substring(0, nameStart) + toggled + path.substring(nameStart + 1);
    }
    
    public void setColony(Colony colony) {
        this.colony = colony;
        this.lastPetPenBoundsSyncKey = Integer.MIN_VALUE;
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
        this.currentBiomeId = GameConstants.BIOME_PLAINS.getId();
        this.colony = null;
        this.paintViewportRect = null;
        this.lodViewportRect = null;
        this.overworldLayoutOffsetX = 0;
        this.overworldLayoutOffsetY = 0;
        this.backgroundImage = resolveBackgroundImage();
        repaint();
    }

    public int getOverworldLayoutOffsetX() {
        return overworldLayoutOffsetX;
    }

    public int getOverworldLayoutOffsetY() {
        return overworldLayoutOffsetY;
    }

    /**
     * Colony entrance in {@link #paintComponent} panel coordinates for the current overworld layout.
     */
    public Point computeOverworldColonyCenterPanelPixels(Colony sessionColony, int viewportWidth, int viewportHeight) {
        int pad = Math.max(OVERWORLD_PAN_OUTSET, Math.max(viewportWidth, viewportHeight) / 2);
        int contentPadX = pad;
        int contentPadY = pad;
        if (sessionColony != null && sessionColony.getLocationService() != null) {
            NeoPoint entrance = sessionColony.getLocationService().getColonyEntrance(sessionColony);
            return new Point(contentPadX + (int) entrance.getX(), contentPadY + (int) entrance.getY());
        }
        return new Point(
                contentPadX + ColonySpatialLayout.ANCHOR_CENTER_X,
                contentPadY + ColonySpatialLayout.ANCHOR_HEIGHT / 2);
    }
    
    public void toggleDimension() {
        currentDimension = (currentDimension == WorldSpaces.OVERWORLD) ? WorldSpaces.UNDERWORLD : WorldSpaces.OVERWORLD;
        updateBackground();
        repaint();
    }
    
    public Dimension getCurrentDimension() {
        return currentDimension;
    }

    public void setBackgroundBiome(Biome biome) {
        if (biome == null) {
            return;
        }
        this.currentBiomeId = biome.getId();
        updateBackground();
    }

    private Image resolveBackgroundImage() {
        if (currentDimension == WorldSpaces.UNDERWORLD) {
            return undergroundTexture;
        }
        Image tile = biomeTextureCache.get(currentBiomeId);
        if (tile == null) {
            tile = biomeTextureCache.get(GameConstants.BIOME_PLAINS.getId());
        }
        return tile;
    }
    
    private void updateBackground() {
        this.backgroundImage = resolveBackgroundImage();
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
            java.awt.Dimension next = new java.awt.Dimension(viewportWidth, finalHeight);
            if (!next.equals(getPreferredSize())) {
                setPreferredSize(next);
                revalidate();
            }
        }
    }

    private boolean showsLogisticsChamber() {
        return colony != null && colony.hasUpgrade(GameUnlocks.ABILITY_TRADE);
    }

    private boolean showsUnderworldThirdRow() {
        if (colony == null) {
            return false;
        }
        return colony.hasUpgrade(GameUnlocks.ROLE_BREEDER) || showsLogisticsChamber();
    }

    private int calculateUnderworldHeight() {
        UnderworldRoomLayout layout = resolveUnderworldRoomLayout();
        if (layout == null) {
            return ColonySpatialLayout.ANCHOR_HEIGHT;
        }

        int bottom = layout.roomYRow2 + layout.roomH;
        if (showsUnderworldThirdRow()) {
            int row3Y = layout.hallY + 512;
            int row3H = layout.roomH;
            if (middleHallwayImg != null) {
                row3H = Math.max(row3H, middleHallwayImg.getHeight(this));
            }
            if (showsLogisticsChamber() && doubleRoomImg != null) {
                row3H = Math.max(row3H, doubleRoomImg.getHeight(this));
            }
            bottom = row3Y + row3H;
        }
        return bottom + 50;
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
                // drawUnderworldRoomDecorationsOverlay(g2d); // disabled — see roadmap: in-room sprites rework
            } else {
                g2d.translate(contentPadX, contentPadY);
                overworldDeadBodySpritesRemaining = GameConstants.MAX_PEN_NON_ANT_SPRITES;
                drawOverworldStructure(g2d);
                drawResourceSources(g2d);
                drawAnts(g2d);
                drawBugs(g2d);
                g2d.translate(-contentPadX, -contentPadY);
            }

            colony.setRoomBounds(entranceBounds, room1Bounds, room2Bounds, room3Bounds, room4Bounds, rancherYardBounds, graverYardBounds, breederRoomBounds, transitRoomBounds);
            colony.setInsectPenBounds(insectPenBounds);
            int penBoundsKey = Objects.hash(
                    colony.getId(),
                    rancherYardBounds,
                    graverYardBounds,
                    insectPenBounds);
            if (penBoundsKey != lastPetPenBoundsSyncKey) {
                colony.getBugHandlingService().syncPetPenPositionsFromBounds(colony);
                lastPetPenBoundsSyncKey = penBoundsKey;
            }
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
            int displayPx = src.getDisplaySizePx();
            int cx = src.getCenterX();
            int cy = src.getCenterY();
            int side = Math.max(1, displayPx);
            int bx = cx - (side + 1) / 2;
            int by = cy - (side + 1) / 2;
            if (!ViewportPhysicsLod.antIntersectsViewport(lodViewportRect, bx, by, side, side)) {
                continue;
            }
            AffineTransform oldTx = g2d.getTransform();
            double deg = resourceSourceRotationDegrees(src);
            g2d.translate(cx, cy);
            g2d.rotate(Math.toRadians(deg));
            g2d.drawImage(icon.getImage(), -displayPx / 2, -displayPx / 2, displayPx, displayPx, this);
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
            int x = (ColonySpatialLayout.ANCHOR_WIDTH / 2) - (w / 2);
            int y = (ColonySpatialLayout.ANCHOR_HEIGHT / 2) - (h / 2);
            
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
            int x = ColonySpatialLayout.ANCHOR_WIDTH - w - 10;
            int y = ColonySpatialLayout.ANCHOR_HEIGHT - h - 10;
            
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

                if (drawDeadPile && overworldDeadBodySpritesRemaining > 0) {
                    int deadSprites = Math.min(
                            GameConstants.capPenNonAntSprites(colony.getDeadAnts().size()),
                            overworldDeadBodySpritesRemaining);
                    drawStaticItemsLocal(g2d, deadBodyImg, safeX, safeY, safeW, safeH, deadSprites);
                    overworldDeadBodySpritesRemaining -= deadSprites;
                }
            }

            g2d.setTransform(old);
            
        } else {
            graverYardBounds = null;
        }

        // --- Insect Pen ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_CATCHER) && basicYardImg != null) {
            int w = basicYardImg.getWidth(this);
            int h = basicYardImg.getHeight(this);
            int x = 10;
            int y = ColonySpatialLayout.ANCHOR_HEIGHT - h - 10;

            g2d.drawImage(basicYardImg, x, y, this);
            insectPenBounds = new Rectangle(x, y, w, h);
        } else {
            insectPenBounds = null;
        }
    }
    
    private void drawStaticItemsLocal(Graphics2D g2d, Image img, int rx, int ry, int rw, int rh, int count) {
        int imgW = img.getWidth(this);
        int imgH = img.getHeight(this);
        
        int areaW = Math.max(1, rw - imgW);
        int areaH = Math.max(1, rh - imgH);

        for (int i = 0; i < count; i++) {
            long seed = i * 999999L;
            int dx = GameRandom.seededNextInt(seed, areaW);
            int dy = GameRandom.seededNextInt(seed + 1, areaH);

            float angle = GameRandom.seededNextFloat(seed + 2) * 360.0f;
            AffineTransform old = g2d.getTransform();
            
            double centerX = rx + dx + (imgW / 2.0);
            double centerY = ry + dy + (imgH / 2.0);
            
            g2d.translate(centerX, centerY);
            g2d.rotate(Math.toRadians(angle));            
            g2d.drawImage(img, -imgW / 2, -imgH / 2, this);
            g2d.setTransform(old);
        }
    }

    private record UnderworldRoomLayout(
            int hallX,
            int hallY,
            int hallW,
            int hallH,
            int leftRoomX,
            int rightRoomX,
            int roomW,
            int roomH,
            int roomYRow1,
            int roomYRow2) {
    }

    private UnderworldRoomLayout resolveUnderworldRoomLayout() {
        if (firstHallwayImg == null || basicRoomImg == null || middleHallwayImg == null) {
            return null;
        }
        int topMargin = 0;
        int hallW = firstHallwayImg.getWidth(this);
        int hallH = firstHallwayImg.getHeight(this);
        int centerX = ColonySpatialLayout.ANCHOR_WIDTH / 2;
        int hallX = centerX - (hallW / 2);
        int hallY = topMargin;
        int roomW = basicRoomImg.getWidth(this);
        int roomH = basicRoomImg.getHeight(this);
        int leftRoomX = hallX - roomW;
        int rightRoomX = hallX + hallW;
        int secondRowYOffset = 256;
        int roomY2 = hallY + secondRowYOffset;
        return new UnderworldRoomLayout(hallX, hallY, hallW, hallH, leftRoomX, rightRoomX, roomW, roomH, hallY, roomY2);
    }

    /*
     * In-room building decoration overlay — disabled for now (see roadmap).
     * Re-enable by uncommenting the draw call in paint and restoring RoomDecorationRenderer import.
     *
    private void drawUnderworldRoomDecorationsOverlay(Graphics2D g2d) {
        if (colony == null) {
            return;
        }
        UnderworldRoomLayout L = resolveUnderworldRoomLayout();
        if (L == null) {
            return;
        }

        RoomDecorationRenderer.drawStorageRoomDecorations(g2d, colony, L.leftRoomX, L.roomYRow1, L.roomW, L.roomH, this);

        AffineTransform old = g2d.getTransform();
        double rotateCenterX = L.rightRoomX + (L.roomW / 2.0);
        double rotateCenterY = L.roomYRow1 + (L.roomH / 2.0);
        g2d.rotate(Math.toRadians(180), rotateCenterX, rotateCenterY);
        RoomDecorationRenderer.drawFarmRoomDecorations(g2d, colony, L.rightRoomX, L.roomYRow1, L.roomW, L.roomH, this);
        g2d.setTransform(old);

        RoomDecorationRenderer.drawNurseryRoomDecorations(g2d, colony, L.leftRoomX, L.roomYRow2, L.roomW, L.roomH, this);

        AffineTransform old2 = g2d.getTransform();
        double rotateCenter2X = L.rightRoomX + (L.roomW / 2.0);
        double rotateCenter2Y = L.roomYRow2 + (L.roomH / 2.0);
        g2d.rotate(Math.toRadians(180), rotateCenter2X, rotateCenter2Y);
        RoomDecorationRenderer.drawRoyalRoomDecorations(g2d, colony, L.rightRoomX, L.roomYRow2, L.roomW, L.roomH, this);
        g2d.setTransform(old2);
    }
    */

    private void drawUnderworldStructure(Graphics2D g2d) {
        if (firstHallwayImg == null || basicRoomImg == null || middleHallwayImg == null) {
            return;
        }

        UnderworldRoomLayout L = resolveUnderworldRoomLayout();
        if (L == null) {
            return;
        }

        int hallX = L.hallX;
        int hallY = L.hallY;
        int hallW = L.hallW;
        int hallH = L.hallH;
        int roomW = L.roomW;
        int roomH = L.roomH;
        int leftRoomX = L.leftRoomX;
        int rightRoomX = L.rightRoomX;
        int roomY = L.roomYRow1;
        
        g2d.drawImage(firstHallwayImg, hallX, hallY, this);
        entranceBounds = new Rectangle(hallX, hallY, hallW, hallH);
        
        g2d.drawImage(basicRoomImg, leftRoomX, roomY, this);
        room1Bounds = new Rectangle(leftRoomX, roomY, roomW, roomH);
        
        AffineTransform old = g2d.getTransform();
        
        double rotateCenterX = rightRoomX + (roomW / 2.0);
        double rotateCenterY = roomY + (roomH / 2.0);
        
        g2d.rotate(Math.toRadians(180), rotateCenterX, rotateCenterY);
        g2d.drawImage(basicRoomImg, rightRoomX, roomY, this);
        room2Bounds = new Rectangle(rightRoomX, roomY, roomW, roomH);
        
        g2d.setTransform(old);

        // --- ROW 2 (Floor 2) ---
        int roomY2 = L.roomYRow2;

        g2d.drawImage(basicRoomImg, leftRoomX, roomY2, this);
        room3Bounds = new Rectangle(leftRoomX, roomY2, roomW, roomH);

        AffineTransform old2 = g2d.getTransform();
        
        double rotateCenter2X = rightRoomX + (roomW / 2.0);
        double rotateCenter2Y = roomY2 + (roomH / 2.0);
        
        g2d.rotate(Math.toRadians(180), rotateCenter2X, rotateCenter2Y);
        g2d.drawImage(basicRoomImg, rightRoomX, roomY2, this);
        room4Bounds = new Rectangle(rightRoomX, roomY2, roomW, roomH);
        
        g2d.setTransform(old2);
        
        // --- ROW 3 (Floor 3) — breeder + logistics portal row ---
        boolean hasBreeder = colony.hasUpgrade(GameUnlocks.ROLE_BREEDER);
        boolean showLogisticsChamber = showsLogisticsChamber();

        if (hasBreeder || showLogisticsChamber) {
            int thirdRowYOffset = 512;
            int roomY3 = hallY + thirdRowYOffset;
            
            g2d.drawImage(middleHallwayImg, hallX, roomY3, this);            
            
            if (hasBreeder) {
                g2d.drawImage(basicRoomImg, leftRoomX, roomY3, this);
                breederRoomBounds = new Rectangle(leftRoomX, roomY3, roomW, roomH);
            } else {
                breederRoomBounds = null;
            }

            if (showLogisticsChamber && doubleRoomImg != null) {
                int dRoomW = doubleRoomImg.getWidth(this);
                int dRoomH = doubleRoomImg.getHeight(this);
                
                AffineTransform old3 = g2d.getTransform();
                double rotateCenter3X = rightRoomX + (dRoomW / 2.0);
                double rotateCenter3Y = roomY3 + (dRoomH / 2.0);
                
                g2d.rotate(Math.toRadians(180), rotateCenter3X, rotateCenter3Y);
                g2d.drawImage(doubleRoomImg, rightRoomX, roomY3, this);
                transitRoomBounds = new Rectangle(rightRoomX, roomY3, dRoomW, dRoomH);
                g2d.setTransform(old3);
                drawTunnelBesideLogisticsRoom(g2d, transitRoomBounds);
            } else {
                transitRoomBounds = null;
            }
        } else {
            breederRoomBounds = null;
            transitRoomBounds = null;
        }
    }

    private void drawTunnelBesideLogisticsRoom(Graphics2D g2d, Rectangle logisticsBounds) {
        if (tunnelSpriteImg == null || logisticsBounds == null || colony == null) {
            return;
        }
        World world = engine != null ? engine.getWorld() : null;
        if (world == null || !colony.hasCompleteTunnel(world)) {
            return;
        }
        int tunnelW = tunnelSpriteImg.getWidth(this);
        int tunnelH = tunnelSpriteImg.getHeight(this);
        if (tunnelW <= 0 || tunnelH <= 0) {
            return;
        }
        int gap = 8;
        int tunnelX = logisticsBounds.x + logisticsBounds.width + gap - 56;
        int tunnelY = logisticsBounds.y + (logisticsBounds.height - tunnelH) / 2;
        g2d.drawImage(tunnelSpriteImg, tunnelX, tunnelY, this);
    }

    private void drawAnts(Graphics2D g2d) {
        if (colony == null) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        for (AntType type : GameConstants.getAntTypes()) {
            if (type == GameConstants.TYPE_DEAD) continue; 

            List<Ant> ants = colony.getAntsByType(type);
            if (ants.isEmpty()) {
                continue;
            }

            List<Species> assimilatedSpecies = new ArrayList<>();
            if (type == GameConstants.TYPE_DRONE && colony.getDynasty() != null) {
                for (Species s : GameConstants.getSpecies()) {
                    if (s == colony.getSpecies()) continue;
                    if (s.getAssimilation() != null && colony.getDynasty().isAssimilationCompleted(s.getAssimilation())
                            && GameConstants.hasAssimilatedDroneSprite(s)) {
                        assimilatedSpecies.add(s);
                    }
                }
            }

            for (Ant ant : ants) {
                if (ant.getDimension() != currentDimension) continue;
                if (ant.getDimension() == WorldSpaces.TUNNEL_WORLD) continue;

                ImageIcon antSpriteIcon = GameConstants.getAntSprite(type, colony.getSpecies(), ant.getSubtypeProfile());
                if (antSpriteIcon == null) continue;
                Image sprite = antSpriteIcon.getImage();
                int w = antSpriteIcon.getIconWidth();
                int h = antSpriteIcon.getIconHeight();

                if (!ViewportPhysicsLod.antIntersectsViewport(lodViewportRect, ant.getX(), ant.getY(), w, h)) {
                    continue;
                }
                
                Image currentSprite = sprite;
                if (!assimilatedSpecies.isEmpty()) {
                    int seed = System.identityHashCode(ant);
                    if (Math.abs(seed) % 10 < 3) {
                        int index = (Math.abs(seed) / 10) % assimilatedSpecies.size();
                        Species as = assimilatedSpecies.get(index);
                        ImageIcon asIcon = GameConstants.getAssimilatedDroneSprite(as);
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
                if (ant.isParasiticMiteInfected()) {
                    drawParasiticMiteOverlay(g2d);
                }

                g2d.setTransform(oldTransform);
            }
        }
    }

    private void drawParasiticMiteOverlay(Graphics2D g2d) {
        ImageIcon miteIcon = GameConstants.TYPE_PARASITIC_MITE.getSprite();
        if (miteIcon == null) {
            miteIcon = GameConstants.ICON_PARASITIC_MITE;
        }
        if (miteIcon == null) {
            return;
        }
        Image mite = miteIcon.getImage();
        int miteW = miteIcon.getIconWidth();
        int miteH = miteIcon.getIconHeight();
        int halfW = miteW / 2;
        int halfH = miteH / 2;
        int[][] offsets = {{-4, -5}, {4, -4}, {-5, 2}, {5, 3}, {0, 5}};
        int count = Math.min(GameConstants.PARASITIC_MITES_ON_ANT_SPRITE, offsets.length);
        for (int i = 0; i < count; i++) {
            g2d.drawImage(mite, offsets[i][0] - halfW, offsets[i][1] - halfH, this);
        }
    }

    private void drawBugs(Graphics2D g2d) {
        if (colony == null) {
            return;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        boolean overworld = currentDimension == WorldSpaces.OVERWORLD;
        int aphidSprites = 0;
        int symbioticMiteSprites = 0;
        int dermestidSprites = 0;
        final int typeSpriteCap = GameConstants.MAX_PEN_NON_ANT_SPRITES;

        for (Bug bug : colony.getBugs()) {
            if (bug.getDimension() != currentDimension) {
                continue;
            }

            ImageIcon spriteIcon = bug.getBugType().getSprite();
            if (spriteIcon == null) {
                continue;
            }

            Image sprite = spriteIcon.getImage();
            int w = spriteIcon.getIconWidth();
            int h = spriteIcon.getIconHeight();

            if (!ViewportPhysicsLod.antIntersectsViewport(lodViewportRect, bug.getX(), bug.getY(), w, h)) {
                continue;
            }

            BugType type = bug.getBugType();
            if (overworld) {
                if (!isBugInTypePen(bug, type, w, h)) {
                    continue;
                }
                if (type == GameConstants.TYPE_APHID && aphidSprites >= typeSpriteCap) {
                    continue;
                }
                if (type == GameConstants.TYPE_SYMBIOTIC_MITE && symbioticMiteSprites >= typeSpriteCap) {
                    continue;
                }
                if (type == GameConstants.TYPE_DERMESTID && dermestidSprites >= typeSpriteCap) {
                    continue;
                }
            } else {
                if (type == GameConstants.TYPE_APHID && aphidSprites >= typeSpriteCap) {
                    continue;
                }
                if (type == GameConstants.TYPE_SYMBIOTIC_MITE && symbioticMiteSprites >= typeSpriteCap) {
                    continue;
                }
                if (type == GameConstants.TYPE_DERMESTID && dermestidSprites >= typeSpriteCap) {
                    continue;
                }
            }

            AffineTransform oldTransform = g2d.getTransform();

            double centerX = bug.getX() + (w / 2.0);
            double centerY = bug.getY() + (h / 2.0);
            g2d.translate(centerX, centerY);
            g2d.rotate(Math.toRadians(bug.getR()));
            g2d.drawImage(sprite, -w / 2, -h / 2, this);
            g2d.setTransform(oldTransform);

            if (type == GameConstants.TYPE_APHID) {
                aphidSprites++;
            } else if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
                symbioticMiteSprites++;
            } else if (type == GameConstants.TYPE_DERMESTID) {
                dermestidSprites++;
            }
        }
    }

    private boolean isBugInTypePen(Bug bug, BugType type, int w, int h) {
        if (type == GameConstants.TYPE_APHID) {
            return isBugCenterInRect(bug, w, h, rancherYardBounds);
        }
        if (type == GameConstants.TYPE_DERMESTID) {
            return isBugCenterInRect(bug, w, h, graverYardBounds);
        }
        if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
            return isBugCenterInRect(bug, w, h, insectPenBounds);
        }
        return isBugInOverworldPen(bug, w, h);
    }

    private boolean isBugInOverworldPen(Bug bug, int w, int h) {
        return isBugCenterInRect(bug, w, h, rancherYardBounds)
                || isBugCenterInRect(bug, w, h, graverYardBounds)
                || isBugCenterInRect(bug, w, h, insectPenBounds);
    }

    private static boolean isBugCenterInRect(Bug bug, int w, int h, Rectangle rect) {
        if (rect == null) {
            return false;
        }
        int cx = bug.getX() + w / 2;
        int cy = bug.getY() + h / 2;
        return rect.contains(cx, cy);
    }
}
