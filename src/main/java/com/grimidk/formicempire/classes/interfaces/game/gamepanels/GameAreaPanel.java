package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.critter.Critter;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.ResourceSource;
import com.grimidk.formicempire.classes.entities.services.colony.ColonySpatialLayout;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RoomDecorationRenderer;
import com.grimidk.formicempire.classes.entities.services.shared.ViewportPhysicsLod;
import com.grimidk.formicempire.classes.entities.spatial.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.assets.GameSpritePreloader;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.game.rendering.CoastalTideRenderer;
import com.grimidk.formicempire.classes.interfaces.game.rendering.ColonySpriteMergeLod;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RouteViewVisuals;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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
    private Image webYardImg;
    private Image deadBodyImg;
    private Image tunnelSpriteImg;
    
    private Colony colony;
    private Engine engine;
    private Dimension currentDimension = WorldSpaces.OVERWORLD;
    private int currentBiomeId = GameConstants.BIOME_PLAINS.getId();
    private Rectangle paintViewportRect;
    private Rectangle lodViewportRect;

    private static final int OVERWORLD_PAN_OUTSET = 1400;
    private static final float SPRITE_ROTATION_EPSILON_DEG = 0.5f;

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
    public Rectangle webYardBounds;

    private int overworldDeadBodySpritesRemaining;
    private int lastPetPenBoundsSyncKey = Integer.MIN_VALUE;
    private List<AntSpecies> cachedAssimilatedDroneSpecies = List.of();
    private int cachedAssimilatedDroneSignature = Integer.MIN_VALUE;
    private BufferedImage cachedBackgroundLayer;
    private int cachedBackgroundW;
    private int cachedBackgroundH;
    private int cachedBackgroundBiomeId = -1;
    private Dimension cachedBackgroundDimension;
    private int cachedBackgroundWaterBordersKey = Integer.MIN_VALUE;

    private BufferedImage cachedUnderworldStructureImage;
    private int cachedUnderworldW = -1;
    private int cachedUnderworldH = -1;
    private boolean cachedHasBreeder = false;
    private boolean cachedShowsLogistics = false;
    private boolean cachedHasTunnel = false;
    private int cachedStructureColonyId = -1;

    private BufferedImage cachedUnderworldDecorationsImage;
    private int cachedDecorationsW = -1;
    private int cachedDecorationsH = -1;
    private int cachedDecorationsSignature = -1;

    private BufferedImage cachedDeadBodyPileImage;
    private int cachedDeadBodyCount = -1;
    private int cachedDeadBodyW = -1;
    private int cachedDeadBodyH = -1;

    public GameAreaPanel() {
        super(null);
        setOpaque(true);
        initComponents();
        initLayout();
        ToolTipManager.sharedInstance().registerComponent(this);
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
        for (Biome b : GameConstants.getBiomes()) {
            if (b != null && b.getTile() != null) {
                biomeTextureCache.put(b.getId(), b.getTile().getImage());
            }
        }
        undergroundTexture = loadImage("backgrounds/colony/UndergroundTile.png");

        basicRoomImg = loadImage("sprites/buildings/BasicRoom.png");
        doubleRoomImg = loadImage("sprites/buildings/DoubleRoom.png");
        firstHallwayImg = loadImage("sprites/buildings/FirstHallway.png");
        middleHallwayImg = loadImage("sprites/buildings/MiddleHallway.png");
        antHillImg = loadImage("sprites/buildings/AntHill.png");
        basicYardImg = loadImage("sprites/buildings/BasicYard.png");
        webYardImg = loadImage("sprites/buildings/WebYard.png");
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
        if (this.colony != colony) {
            invalidateStructuralCaches();
            invalidateBackgroundLayerCache();
        }
        this.colony = colony;
        this.lastPetPenBoundsSyncKey = Integer.MIN_VALUE;
        this.cachedAssimilatedDroneSignature = Integer.MIN_VALUE;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setPaintViewportRect(Rectangle viewRect) {
        if (this.paintViewportRect == null && viewRect != null) {
            invalidateBackgroundLayerCache();
        } else if (this.paintViewportRect != null && viewRect != null) {
            if (this.paintViewportRect.width != viewRect.width || this.paintViewportRect.height != viewRect.height) {
                invalidateBackgroundLayerCache();
            }
        }
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
        this.webYardBounds = null;
        RoomDecorationRenderer.clearWebYardCache();
        invalidateBackgroundLayerCache();
        invalidateStructuralCaches();
        repaint();
    }

    public int getOverworldLayoutOffsetX() {
        return overworldLayoutOffsetX;
    }

    public int getOverworldLayoutOffsetY() {
        return overworldLayoutOffsetY;
    }

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
        invalidateBackgroundLayerCache();
    }

    private void invalidateBackgroundLayerCache() {
        cachedBackgroundLayer = null;
        cachedBackgroundBiomeId = -1;
        cachedBackgroundWaterBordersKey = Integer.MIN_VALUE;
    }

    private Hex resolveCurrentHex() {
        if (engine != null && engine.getWorld() != null) {
            World world = engine.getWorld();
            if (colony != null) {
                Hex colonyHex = world.findHexForColony(colony);
                if (colonyHex != null) {
                    return colonyHex;
                }
            }
            Hex active = world.getActiveHex();
            if (active != null) {
                return active;
            }
            return world.getSpawnHex();
        }
        return null;
    }

    private void invalidateStructuralCaches() {
        cachedUnderworldStructureImage = null;
        cachedUnderworldW = -1;
        cachedUnderworldH = -1;
        cachedUnderworldDecorationsImage = null;
        cachedDecorationsW = -1;
        cachedDecorationsH = -1;
        cachedDeadBodyPileImage = null;
        cachedDeadBodyCount = -1;
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
        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW <= 0 || panelH <= 0) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            renderGameScene(g2d);
        } finally {
            g2d.dispose();
        }
    }

    private void renderGameScene(Graphics2D g2d) {
        Shape previousClip = g2d.getClip();
        Rectangle renderClip = resolveRenderClip(g2d);
        if (renderClip != null) {
            g2d.clip(renderClip);
        }
        try {
            renderGameSceneContents(g2d);
        } finally {
            g2d.setClip(previousClip);
        }
    }

    private Rectangle resolveRenderClip(Graphics2D g2d) {
        Rectangle clip = g2d.getClipBounds();
        if (currentDimension != WorldSpaces.OVERWORLD || paintViewportRect == null) {
            return clip;
        }
        int margin = 128;
        Rectangle viewportClip = new Rectangle(paintViewportRect);
        viewportClip.grow(margin, margin);
        if (clip == null) {
            return viewportClip;
        }
        return clip.intersection(viewportClip);
    }

    private void renderGameSceneContents(Graphics2D g2d) {
        paintBackgroundLayer(g2d);

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
                ColonySpriteMergeLod.ZoneMergeContext spriteMergeZoneContext = buildSpriteMergeZoneContext();
                drawAnts(g2d, spriteMergeZoneContext);
                drawBugs(g2d, spriteMergeZoneContext);
                drawUnderworldRoomDecorationsOverlay(g2d);
            } else {
                g2d.translate(contentPadX, contentPadY);
                overworldDeadBodySpritesRemaining = GameNumbers.MAX_PEN_NON_ANT_SPRITES;
                drawOverworldStructure(g2d);
                drawResourceSources(g2d);
                ColonySpriteMergeLod.ZoneMergeContext spriteMergeZoneContext = buildSpriteMergeZoneContext();
                drawAnts(g2d, spriteMergeZoneContext);
                drawBugs(g2d, spriteMergeZoneContext);
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

    private void paintBackgroundLayer(Graphics2D g2d) {
        if (backgroundImage == null) {
            return;
        }
        int panelW = getWidth();
        int panelH = getHeight();
        Hex currentHex = resolveCurrentHex();
        int bordersKey = CoastalTideRenderer.getWaterBordersKey(currentHex, currentBiomeId);
        if (cachedBackgroundLayer != null && cachedBackgroundW == panelW && cachedBackgroundH == panelH
                && cachedBackgroundBiomeId == currentBiomeId
                && cachedBackgroundDimension == currentDimension
                && cachedBackgroundWaterBordersKey == bordersKey) {
            g2d.drawImage(cachedBackgroundLayer, 0, 0, null);
            return;
        }
        int tileWidth = backgroundImage.getWidth(this);
        int tileHeight = backgroundImage.getHeight(this);
        if (tileWidth <= 0 || tileHeight <= 0) {
            return;
        }
        BufferedImage layer = new BufferedImage(panelW, panelH, BufferedImage.TYPE_INT_RGB);
        Graphics2D layerGraphics = layer.createGraphics();
        try {
            for (int x = 0; x < panelW; x += tileWidth) {
                for (int y = 0; y < panelH; y += tileHeight) {
                    layerGraphics.drawImage(backgroundImage, x, y, this);
                }
            }
            if (currentDimension == WorldSpaces.OVERWORLD) {
                int contentPadX = (paintViewportRect != null) ? Math.max(0, (panelW - paintViewportRect.width) / 2) : 0;
                int contentPadY = (paintViewportRect != null) ? Math.max(0, (panelH - paintViewportRect.height) / 2) : 0;
                CoastalTideRenderer.bakeCoastalBackground(layerGraphics, currentBiomeId, currentHex, contentPadX, contentPadY, panelW, panelH, paintViewportRect);
            }
        } finally {
            layerGraphics.dispose();
        }
        cachedBackgroundLayer = layer;
        cachedBackgroundW = panelW;
        cachedBackgroundH = panelH;
        cachedBackgroundBiomeId = currentBiomeId;
        cachedBackgroundDimension = currentDimension;
        cachedBackgroundWaterBordersKey = bordersKey;
        g2d.drawImage(cachedBackgroundLayer, 0, 0, null);
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

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event == null || currentDimension != WorldSpaces.OVERWORLD || colony == null
                || colony.getLocationService() == null) {
            return null;
        }
        int localX = event.getX() - overworldLayoutOffsetX;
        int localY = event.getY() - overworldLayoutOffsetY;
        ResourceSource hit = findResourceSourceAt(localX, localY);
        if (hit == null || hit.getResourceType() == null) {
            return null;
        }
        String sizeLabel = LanguageStrings.get(hit.getResourceType().getSourceSizeLabelKey(hit.getQuantity()));
        return LanguageStrings.format(
                LanguageStrings.TOOLTIP_RESOURCE_SOURCE_FMT,
                hit.getResourceType().getName(),
                sizeLabel,
                AssetStyles.formatNumber(hit.getQuantity()));
    }

    private ResourceSource findResourceSourceAt(int localX, int localY) {
        ResourceSource best = null;
        double bestDistSq = Double.POSITIVE_INFINITY;
        for (ResourceSource src : colony.getLocationService().getDiscoveredSources()) {
            if (src.getQuantity() <= 0) {
                continue;
            }
            int displayPx = src.getDisplaySizePx();
            int half = Math.max(1, displayPx / 2);
            int cx = src.getCenterX();
            int cy = src.getCenterY();
            if (localX < cx - half || localX > cx + half || localY < cy - half || localY > cy + half) {
                continue;
            }
            double dx = localX - cx;
            double dy = localY - cy;
            double distSq = dx * dx + dy * dy;
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = src;
            }
        }
        return best;
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

        TimeOfDay tod = (world.getActiveHex() != null) ? world.getTimeOfDay(world.getActiveHex()) : world.getTimeOfDay();
        if (dayOn && tod != null && tod.getOverlayColor() != null) {
            g2d.setColor(tod.getOverlayColor());
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
            RoomDecorationRenderer.drawRancherYardDecorations(g2d, colony, x, y, w, h, this);
                        
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
                            GameNumbers.capPenNonAntSprites(colony.getDeadAnts().size()),
                            overworldDeadBodySpritesRemaining);
                    drawCachedDeadAntPile(g2d, safeX, safeY, safeW, safeH, deadSprites);
                    overworldDeadBodySpritesRemaining -= deadSprites;
                }
            }

            g2d.setTransform(old);
            RoomDecorationRenderer.drawGraverYardDecorations(g2d, colony, x, y, w, h, this);
            
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

        if ((colony.hasBuilding(GameUnlocks.PASSIVE_WATER) || colony.hasBuilding(GameUnlocks.PASSIVE_PROTEIN)) && webYardImg != null) {
            int w = webYardImg.getWidth(this);
            int h = webYardImg.getHeight(this);
            int x = ColonySpatialLayout.ANCHOR_WIDTH - w - 10;
            int y = 10;

            AffineTransform old = g2d.getTransform();
            g2d.translate(x + w, y);
            g2d.scale(-1, 1);
            g2d.drawImage(webYardImg, 0, 0, this);
            g2d.setTransform(old);

            webYardBounds = new Rectangle(x, y, w, h);
            RoomDecorationRenderer.drawWebYardDecorations(g2d, colony, x, y, w, h, this);
        } else {
            webYardBounds = null;
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

    private void drawCachedDeadAntPile(Graphics2D g2d, int safeX, int safeY, int safeW, int safeH, int count) {
        if (count <= 0 || deadBodyImg == null) {
            return;
        }
        if (cachedDeadBodyPileImage == null || cachedDeadBodyCount != count
                || cachedDeadBodyW != safeW || cachedDeadBodyH != safeH) {
            BufferedImage baked = new BufferedImage(safeW, safeH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg2d = baked.createGraphics();
            try {
                drawStaticItemsLocal(bg2d, deadBodyImg, 0, 0, safeW, safeH, count);
            } finally {
                bg2d.dispose();
            }
            cachedDeadBodyPileImage = baked;
            cachedDeadBodyCount = count;
            cachedDeadBodyW = safeW;
            cachedDeadBodyH = safeH;
        }
        g2d.drawImage(cachedDeadBodyPileImage, safeX, safeY, this);
    }

    private void drawUnderworldRoomDecorationsOverlay(Graphics2D g2d) {
        if (colony == null) {
            return;
        }
        UnderworldRoomLayout L = resolveUnderworldRoomLayout();
        if (L == null) {
            return;
        }

        int panelW = getWidth();
        int panelH = getHeight();
        int sig = computeUnderworldDecorationsSignature(colony);

        if (cachedUnderworldDecorationsImage == null || cachedDecorationsW != panelW || cachedDecorationsH != panelH
                || cachedDecorationsSignature != sig) {
            BufferedImage baked = new BufferedImage(panelW, panelH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg2d = baked.createGraphics();
            try {
                RoomDecorationRenderer.drawStorageRoomDecorations(bg2d, colony, L.leftRoomX, L.roomYRow1, L.roomW, L.roomH, this);
                RoomDecorationRenderer.drawFarmRoomDecorations(bg2d, colony, L.rightRoomX, L.roomYRow1, L.roomW, L.roomH, this);
                RoomDecorationRenderer.drawNurseryRoomDecorations(bg2d, colony, L.leftRoomX, L.roomYRow2, L.roomW, L.roomH, this);
                RoomDecorationRenderer.drawRoyalRoomDecorations(bg2d, colony, L.rightRoomX, L.roomYRow2, L.roomW, L.roomH, this);
            } finally {
                bg2d.dispose();
            }
            cachedUnderworldDecorationsImage = baked;
            cachedDecorationsW = panelW;
            cachedDecorationsH = panelH;
            cachedDecorationsSignature = sig;
        }

        g2d.drawImage(cachedUnderworldDecorationsImage, 0, 0, null);
    }

    private int computeUnderworldDecorationsSignature(Colony c) {
        if (c == null) {
            return 0;
        }
        int h = c.getId();
        Building[][] chains = {
                GameUnlocks.BUILDING_CHAIN_WATER, GameUnlocks.BUILDING_CHAIN_SYRUP,
                GameUnlocks.BUILDING_CHAIN_ROCK, GameUnlocks.BUILDING_CHAIN_RESIN,
                GameUnlocks.BUILDING_CHAIN_MUSHROOM, GameUnlocks.BUILDING_CHAIN_PLANT,
                GameUnlocks.BUILDING_CHAIN_MEAT, GameUnlocks.BUILDING_CHAIN_EGG,
                GameUnlocks.BUILDING_CHAIN_ROYAL
        };
        for (Building[] chain : chains) {
            Building b = RoomDecorationRenderer.highestUnlocked(c, chain);
            h = 31 * h + (b != null ? b.getId() : 0);
        }
        Building[] passives = {
                GameUnlocks.PASSIVE_FARM, GameUnlocks.PASSIVE_NURSE, GameUnlocks.PASSIVE_LAB
        };
        for (Building b : passives) {
            h = 31 * h + (c.hasBuilding(b) ? b.getId() : 0);
        }
        return h;
    }

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
        int roomY2 = L.roomYRow2;

        entranceBounds = new Rectangle(hallX, hallY, hallW, hallH);
        room1Bounds = new Rectangle(leftRoomX, roomY, roomW, roomH);
        room2Bounds = new Rectangle(rightRoomX, roomY, roomW, roomH);
        room3Bounds = new Rectangle(leftRoomX, roomY2, roomW, roomH);
        room4Bounds = new Rectangle(rightRoomX, roomY2, roomW, roomH);

        boolean hasBreeder = colony.hasUpgrade(GameUnlocks.ROLE_BREEDER);
        boolean showLogisticsChamber = showsLogisticsChamber();
        World world = engine != null ? engine.getWorld() : null;
        boolean hasCompleteTunnel = world != null && colony.hasCompleteTunnel(world);

        int panelW = getWidth();
        int panelH = getHeight();

        if (hasBreeder || showLogisticsChamber) {
            int thirdRowYOffset = 512;
            int roomY3 = hallY + thirdRowYOffset;
            if (hasBreeder) {
                breederRoomBounds = new Rectangle(leftRoomX, roomY3, roomW, roomH);
            } else {
                breederRoomBounds = null;
            }
            if (showLogisticsChamber && doubleRoomImg != null) {
                int dRoomW = doubleRoomImg.getWidth(this);
                int dRoomH = doubleRoomImg.getHeight(this);
                transitRoomBounds = new Rectangle(rightRoomX, roomY3, dRoomW, dRoomH);
            } else {
                transitRoomBounds = null;
            }
        } else {
            breederRoomBounds = null;
            transitRoomBounds = null;
        }

        if (cachedUnderworldStructureImage == null || cachedUnderworldW != panelW || cachedUnderworldH != panelH
                || cachedHasBreeder != hasBreeder || cachedShowsLogistics != showLogisticsChamber
                || cachedHasTunnel != hasCompleteTunnel || cachedStructureColonyId != colony.getId()) {
            BufferedImage baked = new BufferedImage(panelW, panelH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg2d = baked.createGraphics();
            try {
                bg2d.drawImage(firstHallwayImg, hallX, hallY, this);
                bg2d.drawImage(basicRoomImg, leftRoomX, roomY, this);

                AffineTransform old = bg2d.getTransform();
                double rotateCenterX = rightRoomX + (roomW / 2.0);
                double rotateCenterY = roomY + (roomH / 2.0);
                bg2d.rotate(Math.toRadians(180), rotateCenterX, rotateCenterY);
                bg2d.drawImage(basicRoomImg, rightRoomX, roomY, this);
                bg2d.setTransform(old);

                bg2d.drawImage(basicRoomImg, leftRoomX, roomY2, this);

                AffineTransform old2 = bg2d.getTransform();
                double rotateCenter2X = rightRoomX + (roomW / 2.0);
                double rotateCenter2Y = roomY2 + (roomH / 2.0);
                bg2d.rotate(Math.toRadians(180), rotateCenter2X, rotateCenter2Y);
                bg2d.drawImage(basicRoomImg, rightRoomX, roomY2, this);
                bg2d.setTransform(old2);

                if (hasBreeder || showLogisticsChamber) {
                    int thirdRowYOffset = 512;
                    int roomY3 = hallY + thirdRowYOffset;
                    bg2d.drawImage(middleHallwayImg, hallX, roomY3, this);

                    if (hasBreeder) {
                        bg2d.drawImage(basicRoomImg, leftRoomX, roomY3, this);
                    }

                    if (showLogisticsChamber && doubleRoomImg != null) {
                        int dRoomW = doubleRoomImg.getWidth(this);
                        int dRoomH = doubleRoomImg.getHeight(this);

                        AffineTransform old3 = bg2d.getTransform();
                        double rotateCenter3X = rightRoomX + (dRoomW / 2.0);
                        double rotateCenter3Y = roomY3 + (dRoomH / 2.0);
                        bg2d.rotate(Math.toRadians(180), rotateCenter3X, rotateCenter3Y);
                        bg2d.drawImage(doubleRoomImg, rightRoomX, roomY3, this);
                        bg2d.setTransform(old3);
                        drawTunnelBesideLogisticsRoom(bg2d, transitRoomBounds);
                    }
                }
            } finally {
                bg2d.dispose();
            }

            cachedUnderworldStructureImage = baked;
            cachedUnderworldW = panelW;
            cachedUnderworldH = panelH;
            cachedHasBreeder = hasBreeder;
            cachedShowsLogistics = showLogisticsChamber;
            cachedHasTunnel = hasCompleteTunnel;
            cachedStructureColonyId = colony.getId();
        }

        g2d.drawImage(cachedUnderworldStructureImage, 0, 0, null);
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

    private ColonySpriteMergeLod.ZoneMergeContext buildSpriteMergeZoneContext() {
        if (colony == null) {
            return ColonySpriteMergeLod.buildZoneMergeContext(
                    null,
                    currentDimension,
                    currentSpriteMergeZoneLayout());
        }
        return ColonySpriteMergeLod.buildZoneMergeContext(
                colony,
                currentDimension,
                currentSpriteMergeZoneLayout());
    }

    private ColonySpriteMergeLod.MergeZoneLayout currentSpriteMergeZoneLayout() {
        return new ColonySpriteMergeLod.MergeZoneLayout(
                entranceBounds,
                room1Bounds,
                room2Bounds,
                room3Bounds,
                room4Bounds,
                rancherYardBounds,
                graverYardBounds,
                insectPenBounds,
                breederRoomBounds,
                transitRoomBounds);
    }

    private void drawAnts(Graphics2D g2d, ColonySpriteMergeLod.ZoneMergeContext zoneMergeContext) {
        if (colony == null) return;

        applyFastSpriteHints(g2d);

        int colonyAntTotal = colony.getAntTotal();
        boolean colonyMergeActive = ColonySpriteMergeLod.isMergeActive(colonyAntTotal);
        int maxGroupSize = ColonySpriteMergeLod.maxGroupSize(colonyAntTotal);

        for (AntClass type : GameConstants.getAntClasses()) {
            if (type == GameConstants.CLASS_DEAD) continue;

            List<Ant> ants = colony.getAntsByClass(type);
            if (ants.isEmpty()) {
                continue;
            }

            ImageIcon typeSpriteIcon = GameConstants.getAntSprite(type, colony.getSpecies());
            int typeW = typeSpriteIcon != null ? typeSpriteIcon.getIconWidth() : 16;
            int typeH = typeSpriteIcon != null ? typeSpriteIcon.getIconHeight() : 16;

            List<AntSpecies> assimilatedSpecies = type == GameConstants.CLASS_DRONE
                    ? assimilatedDroneSpeciesForPaint()
                    : List.of();

            Map<ColonySpriteMergeLod.AntMergeBucket, List<Ant>> buckets = new HashMap<>();
            List<Ant> individuals = new ArrayList<>();

            for (Ant ant : ants) {
                if (!isAntVisibleForPaint(ant, typeW, typeH)) {
                    continue;
                }
                boolean mergeAnt = colonyMergeActive
                        || (zoneMergeContext != null && zoneMergeContext.shouldMergeAnt(ant));
                if (mergeAnt) {
                    buckets.computeIfAbsent(ColonySpriteMergeLod.antBucket(ant, type), k -> new ArrayList<>()).add(ant);
                } else {
                    individuals.add(ant);
                }
            }

            for (Ant ant : individuals) {
                paintAntSprite(g2d, ant, type, assimilatedSpecies);
            }

            ColonySpriteMergeLod.forEachAntGroup(buckets, group -> {
                if (zoneMergeContext == null || group.isEmpty()) {
                    return maxGroupSize;
                }
                return zoneMergeContext.effectiveMaxGroupSizeForAnt(group.get(0), maxGroupSize);
            }, (bucket, group) -> {
                Ant representative = group.get(0);
                paintAntSpriteAt(
                        g2d,
                        representative,
                        type,
                        ColonySpriteMergeLod.mergeCentroidX(group),
                        ColonySpriteMergeLod.mergeCentroidY(group),
                        assimilatedSpecies);
            });
        }
    }

    private boolean isAntVisibleForPaint(Ant ant, int typeW, int typeH) {
        if (ant.getDimension() != currentDimension) {
            return false;
        }
        if (ant.getDimension() == WorldSpaces.TUNNEL_WORLD) {
            return false;
        }
        return ViewportPhysicsLod.antIntersectsViewport(lodViewportRect, ant.getX(), ant.getY(), typeW, typeH);
    }

    private void paintAntSprite(Graphics2D g2d, Ant ant, AntClass type, List<AntSpecies> assimilatedSpecies) {
        paintAntSpriteAt(g2d, ant, type, ant.getX(), ant.getY(), assimilatedSpecies);
    }

    private void paintAntSpriteAt(
            Graphics2D g2d,
            Ant ant,
            AntClass type,
            int drawX,
            int drawY,
            List<AntSpecies> assimilatedSpecies) {
        boolean showCarry = RouteViewVisuals.showsGathererCarry(ant);
        ImageIcon antSpriteIcon = GameConstants.getAntSprite(
                type,
                colony.getSpecies(),
                ant.getModProfile(),
                ant.getLegFrame(),
                RouteViewVisuals.jawFrameForCarry(ant.getJawFrame(), showCarry),
                ant.getWingFrame(),
                ant.getAntennaFrame(),
                ant.isParasiticMiteInfected());
        if (antSpriteIcon == null) {
            return;
        }
        Image sprite = antSpriteIcon.getImage();
        int w = antSpriteIcon.getIconWidth();
        int h = antSpriteIcon.getIconHeight();

        Image currentSprite = sprite;
        if (!assimilatedSpecies.isEmpty()) {
            int seed = System.identityHashCode(ant);
            if (Math.abs(seed) % 10 < 3) {
                int index = (Math.abs(seed) / 10) % assimilatedSpecies.size();
                AntSpecies as = assimilatedSpecies.get(index);
                ImageIcon asIcon = GameConstants.getAssimilatedDroneSprite(as);
                if (asIcon != null) {
                    currentSprite = asIcon.getImage();
                }
            }
        }

        drawSpriteOriented(g2d, currentSprite, drawX, drawY, w, h, ant.getR(), showCarry
                ? () -> RouteViewVisuals.paintJawCarryIcons(
                g2d, RouteViewVisuals.gathererCarryIcons(ant), w, h, this)
                : null);
    }

    private List<AntSpecies> assimilatedDroneSpeciesForPaint() {
        if (colony == null) {
            cachedAssimilatedDroneSignature = Integer.MIN_VALUE;
            cachedAssimilatedDroneSpecies = List.of();
            return cachedAssimilatedDroneSpecies;
        }
        int signature = assimilatedDronePaintSignature(colony);
        if (signature == cachedAssimilatedDroneSignature) {
            return cachedAssimilatedDroneSpecies;
        }
        cachedAssimilatedDroneSignature = signature;
        if (signature == 0) {
            cachedAssimilatedDroneSpecies = List.of();
            return cachedAssimilatedDroneSpecies;
        }
        List<AntSpecies> species = new ArrayList<>();
        Dynasty dynasty = colony.getDynasty();
        for (AntSpecies s : GameConstants.getSpecies()) {
            if (s == colony.getSpecies()) {
                continue;
            }
            if (s.getAssimilation() != null && dynasty.isAssimilationCompleted(s.getAssimilation())
                    && GameConstants.hasAssimilatedDroneSprite(s)) {
                species.add(s);
            }
        }
        cachedAssimilatedDroneSpecies = List.copyOf(species);
        return cachedAssimilatedDroneSpecies;
    }

    private static int assimilatedDronePaintSignature(Colony colony) {
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || !dynasty.hasUpgrade(GameUnlocks.ABILITY_CLONING)) {
            return 0;
        }
        int signature = colony.getId();
        for (AntSpecies s : GameConstants.getSpecies()) {
            if (s == colony.getSpecies() || s.getAssimilation() == null) {
                continue;
            }
            if (dynasty.isAssimilationCompleted(s.getAssimilation())
                    && GameConstants.hasAssimilatedDroneSprite(s)) {
                signature = 31 * signature + s.getId();
            }
        }
        return signature;
    }

    private void drawBugs(Graphics2D g2d, ColonySpriteMergeLod.ZoneMergeContext zoneMergeContext) {
        if (colony == null) {
            return;
        }

        applyFastSpriteHints(g2d);

        boolean overworld = currentDimension == WorldSpaces.OVERWORLD;
        int[] aphidSprites = {0};
        int[] symbioticMiteSprites = {0};
        int[] dermestidSprites = {0};
        final int typeSpriteCap = GameNumbers.MAX_PEN_NON_ANT_SPRITES;
        boolean colonyMergeActive = ColonySpriteMergeLod.isMergeActive(colony.getAntTotal());
        int maxGroupSize = ColonySpriteMergeLod.maxGroupSize(colony.getAntTotal());
        Map<ColonySpriteMergeLod.CritterMergeBucket, List<Critter>> buckets = new HashMap<>();

        for (Critter bug : colony.getCritters()) {
            if (bug.getDimension() != currentDimension) {
                continue;
            }

            ImageIcon spriteIcon = GameConstants.getCritterSprite(
                    bug.getSpecies(), bug.getLegFrame(), bug.getAntennaFrame());
            if (spriteIcon == null) {
                continue;
            }

            int w = spriteIcon.getIconWidth();
            int h = spriteIcon.getIconHeight();

            if (!ViewportPhysicsLod.antIntersectsViewport(lodViewportRect, bug.getX(), bug.getY(), w, h)) {
                continue;
            }

            Species type = bug.getSpecies();
            if (overworld) {
                if (!isBugInTypePen(bug, type, w, h)) {
                    continue;
                }
                if (type == GameConstants.TYPE_APHID && aphidSprites[0] >= typeSpriteCap) {
                    continue;
                }
                if (type == GameConstants.TYPE_SYMBIOTIC_MITE && symbioticMiteSprites[0] >= typeSpriteCap) {
                    continue;
                }
                if (type == GameConstants.TYPE_DERMESTID && dermestidSprites[0] >= typeSpriteCap) {
                    continue;
                }
            } else {
                if (type == GameConstants.TYPE_APHID && aphidSprites[0] >= typeSpriteCap) {
                    continue;
                }
                if (type == GameConstants.TYPE_SYMBIOTIC_MITE && symbioticMiteSprites[0] >= typeSpriteCap) {
                    continue;
                }
                if (type == GameConstants.TYPE_DERMESTID && dermestidSprites[0] >= typeSpriteCap) {
                    continue;
                }
            }

            boolean mergeBug = colonyMergeActive
                    || (zoneMergeContext != null && zoneMergeContext.shouldMergeCritter(bug, w, h));
            if (mergeBug) {
                buckets.computeIfAbsent(ColonySpriteMergeLod.critterBucket(bug), k -> new ArrayList<>()).add(bug);
                continue;
            }

            paintCritterSprite(g2d, bug, spriteIcon, w, h);

            if (type == GameConstants.TYPE_APHID) {
                aphidSprites[0]++;
            } else if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
                symbioticMiteSprites[0]++;
            } else if (type == GameConstants.TYPE_DERMESTID) {
                dermestidSprites[0]++;
            }
        }

        ColonySpriteMergeLod.forEachCritterGroup(buckets, group -> {
            if (zoneMergeContext == null || group.isEmpty()) {
                return maxGroupSize;
            }
            Critter representative = group.get(0);
            ImageIcon groupSprite = GameConstants.getCritterSprite(
                    representative.getSpecies(), representative.getLegFrame(), representative.getAntennaFrame());
            int groupW = groupSprite != null ? groupSprite.getIconWidth() : 16;
            int groupH = groupSprite != null ? groupSprite.getIconHeight() : 16;
            return zoneMergeContext.effectiveMaxGroupSizeForCritter(representative, groupW, groupH, maxGroupSize);
        }, (bucket, group) -> {
            Critter representative = group.get(0);
            ImageIcon spriteIcon = GameConstants.getCritterSprite(
                    representative.getSpecies(), representative.getLegFrame(), representative.getAntennaFrame());
            if (spriteIcon == null) {
                return;
            }
            Species type = representative.getSpecies();
            if (type == GameConstants.TYPE_APHID && aphidSprites[0] >= typeSpriteCap) {
                return;
            }
            if (type == GameConstants.TYPE_SYMBIOTIC_MITE && symbioticMiteSprites[0] >= typeSpriteCap) {
                return;
            }
            if (type == GameConstants.TYPE_DERMESTID && dermestidSprites[0] >= typeSpriteCap) {
                return;
            }

            paintCritterSpriteAt(
                    g2d,
                    spriteIcon.getImage(),
                    ColonySpriteMergeLod.mergeCentroidX(group),
                    ColonySpriteMergeLod.mergeCentroidY(group),
                    spriteIcon.getIconWidth(),
                    spriteIcon.getIconHeight(),
                    representative.getR());

            if (type == GameConstants.TYPE_APHID) {
                aphidSprites[0]++;
            } else if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
                symbioticMiteSprites[0]++;
            } else if (type == GameConstants.TYPE_DERMESTID) {
                dermestidSprites[0]++;
            }
        });
    }

    private void paintCritterSprite(Graphics2D g2d, Critter bug, ImageIcon spriteIcon, int w, int h) {
        paintCritterSpriteAt(g2d, spriteIcon.getImage(), bug.getX(), bug.getY(), w, h, bug.getR());
    }

    private void paintCritterSpriteAt(
            Graphics2D g2d,
            Image sprite,
            int drawX,
            int drawY,
            int w,
            int h,
            int rotationDegrees) {
        drawSpriteOriented(g2d, sprite, drawX, drawY, w, h, rotationDegrees, null);
    }

    private static void applyFastSpriteHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }

    private void drawSpriteOriented(
            Graphics2D g2d,
            Image sprite,
            int x,
            int y,
            int w,
            int h,
            int rotationDegrees,
            Runnable overlayInLocalSpace) {
        float normalized = normalizeDegrees(rotationDegrees);
        boolean needsRotate = Math.abs(normalized) >= SPRITE_ROTATION_EPSILON_DEG;
        if (!needsRotate && overlayInLocalSpace == null) {
            g2d.drawImage(sprite, Math.round(x), Math.round(y), this);
            return;
        }
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(x + (w / 2.0), y + (h / 2.0));
        if (needsRotate) {
            g2d.rotate(Math.toRadians(normalized));
        }
        g2d.drawImage(sprite, -w / 2, -h / 2, this);
        if (overlayInLocalSpace != null) {
            overlayInLocalSpace.run();
        }
        g2d.setTransform(oldTransform);
    }

    private static float normalizeDegrees(float degrees) {
        float r = degrees % 360f;
        if (r > 180f) {
            r -= 360f;
        } else if (r < -180f) {
            r += 360f;
        }
        return r;
    }

    private boolean isBugInTypePen(Critter bug, Species type, int w, int h) {
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

    private boolean isBugInOverworldPen(Critter bug, int w, int h) {
        return isBugCenterInRect(bug, w, h, rancherYardBounds)
                || isBugCenterInRect(bug, w, h, graverYardBounds)
                || isBugCenterInRect(bug, w, h, insectPenBounds);
    }

    private static boolean isBugCenterInRect(Critter bug, int w, int h, Rectangle rect) {
        if (rect == null) {
            return false;
        }
        int cx = bug.getX() + w / 2;
        int cy = bug.getY() + h / 2;
        return rect.contains(cx, cy);
    }
}
