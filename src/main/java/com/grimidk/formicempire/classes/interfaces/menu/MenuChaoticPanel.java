package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.services.colony.ConvoyScene;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleScene;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RouteViewVisuals;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RouteViewVisuals.ConvoyResourceProp;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MenuChaoticPanel extends JPanel {

    private static final int SPRITE_SCALE = 2;
    private static final float WOBBLE_SPEED = 5.5f;
    private static final float WOBBLE_AMPLITUDE_PX = 2f;
    private static final int LINE_JITTER_PX = 10;
    private static final float AIR_SUPPORT_FLY_CYCLE_SEC = 14.0f;
    private static final float AIR_SUPPORT_FLY_PORTION = 0.36f;
    private static final float CONVOY_SCROLL_SPEED_PX = 120f;
    private static final int MENU_DIM_ALPHA = 96;

    private final List<MenuChaoticWorld> worlds;
    private final List<Integer> playOrder = new ArrayList<>();
    private final Map<Integer, Image> biomeTileCache = new HashMap<>();
    private Image antHillImage;
    private int playOrderIndex;
    private int scenarioElapsedMs;
    private float animationSeconds;
    private Timer animationTimer;
    private boolean active;

    public MenuChaoticPanel() {
        setOpaque(true);
        setBackground(AssetStyles.COLOR_ABSOLUTE_BLACK);
        worlds = new ArrayList<>();
        for (MenuChaoticDefinition definition : MenuChaoticCatalog.getScenarios()) {
            worlds.add(MenuChaoticWorld.fromDefinition(definition));
        }
        reshufflePlayOrder(new Random(0xC4A0_7C1C));
        prepareCurrentScenario(new Random(0xC04D_1211L));
        loadBiomeTiles();
        loadAntHillImage();
    }

    public void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        if (active) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    private void reshufflePlayOrder(Random random) {
        playOrder.clear();
        for (int i = 0; i < worlds.size(); i++) {
            playOrder.add(i);
        }
        Collections.shuffle(playOrder, random);
        playOrderIndex = 0;
    }

    private void startAnimation() {
        if (animationTimer != null) {
            return;
        }
        animationTimer = new Timer(MenuChaoticCatalog.ANIMATION_FRAME_MS, e -> tick());
        animationTimer.setCoalesce(true);
        animationTimer.start();
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

    private void tick() {
        if (!active || worlds.isEmpty() || playOrder.isEmpty()) {
            return;
        }
        float delta = MenuChaoticCatalog.ANIMATION_FRAME_MS / 1000f;
        animationSeconds += delta;
        if (animationSeconds > 10_000f) {
            animationSeconds = 0f;
        }
        scenarioElapsedMs += MenuChaoticCatalog.ANIMATION_FRAME_MS;
        if (scenarioElapsedMs >= MenuChaoticCatalog.SCENARIO_DURATION_MS) {
            scenarioElapsedMs = 0;
            playOrderIndex++;
            if (playOrderIndex >= playOrder.size()) {
                playOrderIndex = 0;
                reshufflePlayOrder(new Random(System.nanoTime()));
            }
            prepareCurrentScenario(new Random(System.nanoTime()));
        }
        currentWorld().update(delta, getWidth(), getHeight());
        repaint();
    }

    private void prepareCurrentScenario(Random random) {
        if (playOrder.isEmpty() || playOrderIndex < 0 || playOrderIndex >= playOrder.size()) {
            return;
        }
        MenuChaoticWorld world = currentWorld();
        if (world.getKind() == MenuChaoticKind.CONVOY) {
            world.randomizeConvoyTravelDirection(random);
        }
    }

    private MenuChaoticWorld currentWorld() {
        return worlds.get(playOrder.get(playOrderIndex));
    }

    @Override
    public void removeNotify() {
        stopAnimation();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (worlds.isEmpty() || playOrder.isEmpty()) {
            return;
        }
        MenuChaoticWorld world = currentWorld();
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int fieldW = getWidth();
        int fieldH = getHeight();
        if (fieldW <= 0 || fieldH <= 0) {
            g2d.dispose();
            return;
        }

        switch (world.getKind()) {
            case OVERWORLD -> paintOverworld(g2d, world, fieldW, fieldH);
            case COLONY -> paintColony(g2d, world, fieldW, fieldH);
            case BATTLE -> paintBattle(g2d, world, fieldW, fieldH);
            case CONVOY -> paintConvoy(g2d, world, fieldW, fieldH);
        }

        g2d.setColor(new Color(0, 0, 0, MENU_DIM_ALPHA));
        g2d.fillRect(0, 0, fieldW, fieldH);
        g2d.dispose();
    }

    private void paintOverworld(Graphics2D g2d, MenuChaoticWorld world, int fieldW, int fieldH) {
        drawFullBiomeField(g2d, 0, 0, fieldW, fieldH, world.getPrimaryBiome());
        for (MenuChaoticWorld.ShowcaseCritter critter : world.getCritters()) {
            drawCritter(g2d, critter, fieldW, fieldH);
        }
        for (MenuChaoticWorld.ShowcaseAnt ant : world.getAnts()) {
            drawWanderingAnt(g2d, ant, fieldW, fieldH);
        }
    }

    private void paintColony(Graphics2D g2d, MenuChaoticWorld world, int fieldW, int fieldH) {
        drawFullBiomeField(g2d, 0, 0, fieldW, fieldH, world.getPrimaryBiome());
        int entranceX = Math.round(MenuChaoticWorld.colonyEntranceXNorm() * fieldW);
        int entranceY = Math.round(MenuChaoticWorld.colonyEntranceYNorm() * fieldH);
        if (antHillImage != null) {
            int hillW = antHillImage.getWidth(this);
            int hillH = antHillImage.getHeight(this);
            if (hillW > 0 && hillH > 0) {
                g2d.drawImage(antHillImage, entranceX - hillW / 2, entranceY - hillH / 2, this);
            }
        }
        for (MenuChaoticWorld.ShowcaseAnt ant : world.getAnts()) {
            drawWanderingAnt(g2d, ant, fieldW, fieldH);
        }
    }

    private void paintBattle(Graphics2D g2d, MenuChaoticWorld world, int fieldW, int fieldH) {
        int contactLineX = Math.round(fieldW * world.getContactLineRatio());
        drawBattlefieldBiomes(g2d, 0, 0, fieldW, fieldH, fieldW / 2,
                world.getPrimaryBiome(), world.getSecondaryBiome());
        Rectangle field = new Rectangle(0, 0, fieldW, fieldH);
        drawBattleColumn(g2d, world.getAnts(), field, contactLineX, true);
        drawBattleColumn(g2d, world.getAnts(), field, contactLineX, false);
    }

    private void paintConvoy(Graphics2D g2d, MenuChaoticWorld world, int fieldW, int fieldH) {
        boolean travelingRight = world.isConvoyTravelingRight();
        int scrollOffset = computeConvoyScrollOffset(fieldW, world, travelingRight);
        drawConvoyField(g2d, 0, 0, fieldW, fieldH, scrollOffset, world);
        drawConvoyResources(g2d, world.getConvoyResources(), 0, 0, fieldW, fieldH, scrollOffset);
        Rectangle field = new Rectangle(0, 0, fieldW, fieldH);
        drawConvoyFormation(g2d, world.getAnts(), field, travelingRight);
    }

    private void drawWanderingAnt(Graphics2D g2d, MenuChaoticWorld.ShowcaseAnt ant, int fieldW, int fieldH) {
        int jawFrame = RouteViewVisuals.resolveJawFrame(ant.type, false, ant.wobblePhase, animationSeconds,
                ant.motionRate);
        int wingFrame = RouteViewVisuals.resolveWingFrame(ant.type, RouteViewVisuals.isWinged(ant.type),
                ant.wobblePhase, animationSeconds, ant.motionRate);
        float angle = RouteViewVisuals.movementFacingDegrees(ant.vx, ant.vy);
        drawAntSprite(g2d, ant.type, ant.species, ant.profile, jawFrame, wingFrame,
                Math.round(ant.xNorm * fieldW), Math.round(ant.yNorm * fieldH), angle);
    }

    private void drawCritter(Graphics2D g2d, MenuChaoticWorld.ShowcaseCritter critter, int fieldW, int fieldH) {
        if (critter.species == null || critter.species.getSprite() == null) {
            return;
        }
        ImageIcon icon = critter.species.getSprite();
        Image sprite = icon.getImage();
        int w = Math.max(6, icon.getIconWidth() / SPRITE_SCALE);
        int h = Math.max(6, icon.getIconHeight() / SPRITE_SCALE);
        if (w <= 0 || h <= 0) {
            return;
        }
        float wobble = (float) Math.sin(critter.wobblePhase + animationSeconds * WOBBLE_SPEED * critter.motionRate)
                * WOBBLE_AMPLITUDE_PX;
        int drawX = Math.round(critter.xNorm * fieldW);
        int drawY = Math.round(critter.yNorm * fieldH + wobble);
        float angle = RouteViewVisuals.movementFacingDegrees(critter.vx, critter.vy);
        AffineTransform old = g2d.getTransform();
        g2d.translate(drawX, drawY);
        g2d.rotate(Math.toRadians(angle));
        g2d.drawImage(sprite, -w / 2, -h / 2, w, h, this);
        g2d.setTransform(old);
    }

    private void drawBattleColumn(Graphics2D g2d, List<MenuChaoticWorld.ShowcaseAnt> ants, Rectangle field,
            int contactLineX, boolean attackerSide) {
        int edgePad = 4;
        int fieldRight = field.x + field.width;
        for (MenuChaoticWorld.ShowcaseAnt ant : ants) {
            if (ant.attackerSide != attackerSide) {
                continue;
            }
            boolean winged = RouteViewVisuals.isWinged(ant.type);
            boolean airSupport = !ant.reserve && ant.battleLine == GameConstants.BATTLE_LINE_AIR_SUPPORT;
            int jawFrame = RouteViewVisuals.resolveJawFrame(ant.type, ant.reserve, ant.wobblePhase, animationSeconds,
                    ant.motionRate);
            int wingFrame = RouteViewVisuals.resolveWingFrame(ant.type, airSupport && winged, ant.wobblePhase,
                    animationSeconds, ant.motionRate);

            ImageIcon icon = GameConstants.getAntSprite(ant.type, ant.species, ant.profile, 1, jawFrame, wingFrame);
            int w = icon != null ? Math.max(8, icon.getIconWidth() / SPRITE_SCALE) : 12;
            int h = icon != null ? Math.max(8, icon.getIconHeight() / SPRITE_SCALE) : 12;

            float wobble = (float) Math.sin(ant.wobblePhase + animationSeconds * WOBBLE_SPEED * ant.motionRate)
                    * WOBBLE_AMPLITUDE_PX;
            int drawY = field.y + edgePad
                    + Math.round(ant.laneY * (field.height - h - edgePad * 2f))
                    + Math.round(wobble);

            int drawX;
            if (airSupport) {
                float cycle = (animationSeconds / AIR_SUPPORT_FLY_CYCLE_SEC) + ant.flyPhase;
                cycle = cycle - (float) Math.floor(cycle);
                if (cycle > AIR_SUPPORT_FLY_PORTION) {
                    continue;
                }
                float t = cycle / AIR_SUPPORT_FLY_PORTION;
                int travel = field.width + w * 2;
                if (attackerSide) {
                    drawX = field.x - w + Math.round(t * travel);
                } else {
                    drawX = fieldRight - Math.round(t * travel);
                }
            } else {
                int lineOffset = MenuChaoticWorld.battleLineOffsetPx(ant.battleLine, ant.reserve);
                if (ant.reserve) {
                    lineOffset = Math.max(WarBattleScene.ARTILLERY_LINE_OFFSET_PX,
                            lineOffset - Math.round(animationSeconds * MenuChaoticCatalog.BATTLE_RESERVE_WALK_PX_PER_SEC
                                    * ant.motionRate));
                }
                int jitter = Math.round((ant.laneJitter - 0.5f) * 2f * LINE_JITTER_PX);
                if (attackerSide) {
                    drawX = contactLineX - w - edgePad - lineOffset + jitter;
                    drawX = Math.max(field.x + edgePad, drawX);
                } else {
                    drawX = contactLineX + edgePad + lineOffset + jitter;
                    drawX = Math.min(fieldRight - w - edgePad, drawX);
                }
            }

            float faceAngle = RouteViewVisuals.movementFacingDegrees(attackerSide ? 1f : -1f, 0f);

            Image image = icon != null ? icon.getImage() : null;
            AffineTransform old = g2d.getTransform();
            double cx = drawX + w / 2.0;
            double cy = drawY + h / 2.0;
            g2d.translate(cx, cy);
            g2d.rotate(Math.toRadians(faceAngle));
            if (image != null) {
                g2d.drawImage(image, -w / 2, -h / 2, w, h, this);
            }
            g2d.setTransform(old);
        }
    }

    private void drawConvoyFormation(Graphics2D g2d, List<MenuChaoticWorld.ShowcaseAnt> ants, Rectangle field,
            boolean travelingRight) {
        if (ants.isEmpty() || field.width <= 4 || field.height <= 4) {
            return;
        }
        int centerX = field.x + field.width / 2;
        int centerY = field.y + field.height / 2;
        float radiusX = Math.min(field.width * 0.2f, 56f + ants.size() * 0.5f);
        float radiusY = Math.min(field.height * 0.34f, 44f + ants.size() * 0.38f);
        float faceAngle = RouteViewVisuals.convoyFacingDegrees(travelingRight);
        for (MenuChaoticWorld.ShowcaseAnt ant : ants) {
            int jawFrame = RouteViewVisuals.resolveJawFrame(ant.type, false, ant.wobblePhase, animationSeconds,
                    ant.motionRate);
            int wingFrame = RouteViewVisuals.resolveWingFrame(ant.type, RouteViewVisuals.isWinged(ant.type),
                    ant.wobblePhase, animationSeconds, ant.motionRate);
            ImageIcon icon = GameConstants.getAntSprite(ant.type, ant.species, ant.profile, 1, jawFrame, wingFrame);
            if (icon == null) {
                continue;
            }
            Image image = icon.getImage();
            int w = Math.max(8, icon.getIconWidth() / SPRITE_SCALE);
            int h = Math.max(8, icon.getIconHeight() / SPRITE_SCALE);
            int drawX = centerX + Math.round(ant.offsetX * radiusX);
            int drawY = centerY + Math.round(ant.offsetY * radiusY);
            AffineTransform old = g2d.getTransform();
            double cx = drawX + w / 2.0;
            double cy = drawY + h / 2.0;
            g2d.translate(cx, cy);
            g2d.rotate(Math.toRadians(faceAngle));
            g2d.drawImage(image, -w / 2, -h / 2, w, h, this);
            g2d.setTransform(old);
        }
    }

    private void drawConvoyResources(Graphics2D g2d, List<ConvoyResourceProp> props, int fieldX, int fieldY, int fieldW,
            int fieldH, int scrollOffset) {
        if (props == null || props.isEmpty()) {
            return;
        }
        int spacing = Math.max(220, fieldW / 2);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        for (ConvoyResourceProp prop : props) {
            if (prop.resourceType() == null) {
                continue;
            }
            ImageIcon icon = prop.resourceType().getIconForSourceQuantity(prop.quantity());
            if (icon == null) {
                continue;
            }
            int displayPx = prop.resourceType().getDisplaySizeForSourceQuantity(prop.quantity());
            int phasePx = Math.round(prop.xPhase() * spacing);
            int anchorX = fieldX + Math.floorMod(phasePx + scrollOffset, Math.max(1, spacing));
            for (int x = anchorX - spacing * 2; x < fieldX + fieldW + spacing; x += spacing) {
                if (x + displayPx < fieldX || x > fieldX + fieldW) {
                    continue;
                }
                int drawY = fieldY + Math.round(prop.yNorm() * fieldH) - displayPx / 2;
                AffineTransform old = g2d.getTransform();
                g2d.translate(x, drawY + displayPx / 2.0);
                g2d.rotate(Math.toRadians(prop.rotationDegrees()));
                g2d.drawImage(icon.getImage(), -displayPx / 2, -displayPx / 2, displayPx, displayPx, this);
                g2d.setTransform(old);
            }
        }
    }

    private int computeConvoyScrollOffset(int fieldW, MenuChaoticWorld world, boolean travelingRight) {
        Image tile = tileForConvoy(world);
        int tileW = tileWidth(tile);
        if (tileW <= 0) {
            tileW = 64;
        }
        int raw = (int) (animationSeconds * CONVOY_SCROLL_SPEED_PX) % tileW;
        if (travelingRight) {
            return tileW - raw;
        }
        return raw;
    }

    private void drawConvoyField(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH, int scrollOffset,
            MenuChaoticWorld world) {
        Image tile = tileForConvoy(world);
        tileScrollRegion(g2d, tile, fieldX, fieldY, fieldW, fieldH, scrollOffset);
    }

    private Image tileForConvoy(MenuChaoticWorld world) {
        ConvoyScene.BackgroundKind kind = world.getConvoyBackground();
        if (kind == null) {
            return tileForBiome(world.getPrimaryBiome());
        }
        return switch (kind) {
            case SEA -> iconImage(GameConstants.CONVOY_TILE_SEA);
            case SKY -> iconImage(GameConstants.CONVOY_TILE_SKY);
            case TUNNEL -> iconImage(GameConstants.CONVOY_TILE_UNDERGROUND);
            case LAND_BIOME -> tileForBiome(world.getPrimaryBiome());
        };
    }

    private static Image iconImage(javax.swing.ImageIcon icon) {
        return icon != null ? icon.getImage() : null;
    }

    private void drawFullBiomeField(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH, Biome biome) {
        tileRegion(g2d, tileForBiome(biome), fieldX, fieldY, fieldW, fieldH);
    }

    private void drawBattlefieldBiomes(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH, int splitX,
            Biome leftBiome, Biome rightBiome) {
        Image leftTile = tileForBiome(leftBiome);
        Image rightTile = tileForBiome(rightBiome);
        int tileW = tileWidth(leftTile);
        int tileH = tileHeight(leftTile);
        Shape oldClip = g2d.getClip();
        int leftW = splitX - fieldX;
        int rightW = fieldX + fieldW - splitX;
        if (leftW > 0) {
            g2d.setClip(fieldX, fieldY, leftW, fieldH);
            tileRegion(g2d, leftTile, fieldX, fieldY, fieldW, fieldH, tileW, tileH);
        }
        if (rightW > 0) {
            g2d.setClip(splitX, fieldY, rightW, fieldH);
            tileRegion(g2d, rightTile, fieldX, fieldY, fieldW, fieldH, tileW, tileH);
        }
        g2d.setClip(oldClip);
    }

    private void tileRegion(Graphics2D g2d, Image tile, int originX, int originY, int regionW, int regionH) {
        int tileW = tileWidth(tile);
        int tileH = tileHeight(tile);
        tileRegion(g2d, tile, originX, originY, regionW, regionH, tileW, tileH);
    }

    private void tileRegion(Graphics2D g2d, Image tile, int originX, int originY, int regionW, int regionH, int tileW,
            int tileH) {
        if (tile == null || tileW <= 0 || tileH <= 0) {
            g2d.setColor(AssetStyles.UI_BG_SECONDARY);
            g2d.fillRect(originX, originY, regionW, regionH);
            return;
        }
        for (int y = originY; y < originY + regionH; y += tileH) {
            for (int x = originX; x < originX + regionW; x += tileW) {
                g2d.drawImage(tile, x, y, this);
            }
        }
    }

    private void tileScrollRegion(Graphics2D g2d, Image tile, int fieldX, int fieldY, int fieldW, int fieldH,
            int scrollOffset) {
        int tileW = tileWidth(tile);
        int tileH = tileHeight(tile);
        if (tile == null || tileW <= 0 || tileH <= 0) {
            g2d.setColor(AssetStyles.UI_BG_SECONDARY);
            g2d.fillRect(fieldX, fieldY, fieldW, fieldH);
            return;
        }
        int startX = fieldX - tileW + (scrollOffset % tileW);
        if (startX > fieldX) {
            startX -= tileW;
        }
        for (int y = fieldY; y < fieldY + fieldH; y += tileH) {
            for (int x = startX; x < fieldX + fieldW; x += tileW) {
                if (x + tileW < fieldX || x > fieldX + fieldW) {
                    continue;
                }
                g2d.drawImage(tile, x, y, this);
            }
        }
    }

    private void drawAntSprite(Graphics2D g2d, AntType type,
            com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies species,
            com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile profile,
            int jawFrame, int wingFrame, int drawX, int drawY, float angleDegrees) {
        ImageIcon icon = GameConstants.getAntSprite(type, species, profile, 1, jawFrame, wingFrame);
        if (icon == null) {
            return;
        }
        Image image = icon.getImage();
        int w = Math.max(6, icon.getIconWidth() / SPRITE_SCALE);
        int h = Math.max(6, icon.getIconHeight() / SPRITE_SCALE);
        AffineTransform old = g2d.getTransform();
        g2d.translate(drawX, drawY);
        g2d.rotate(Math.toRadians(angleDegrees));
        g2d.drawImage(image, -w / 2, -h / 2, w, h, this);
        g2d.setTransform(old);
    }

    private Image tileForBiome(Biome biome) {
        if (biome == null) {
            return biomeTileCache.get(GameConstants.BIOME_PLAINS.getId());
        }
        Image tile = biomeTileCache.get(biome.getId());
        if (tile != null) {
            return tile;
        }
        return biomeTileCache.get(GameConstants.BIOME_PLAINS.getId());
    }

    private int tileWidth(Image tile) {
        if (tile == null) {
            return 64;
        }
        int w = tile.getWidth(this);
        return w > 0 ? w : 64;
    }

    private int tileHeight(Image tile) {
        if (tile == null) {
            return 64;
        }
        int h = tile.getHeight(this);
        return h > 0 ? h : 64;
    }

    private void loadAntHillImage() {
        antHillImage = UiResourceLoader.loadImage(MenuChaoticPanel.class, "/sprites/buildings/AntHill.png");
        if (antHillImage == null) {
            antHillImage = UiResourceLoader.loadImage(MenuChaoticPanel.class, "sprites/buildings/AntHill.png");
        }
    }

    private void loadBiomeTiles() {
        putBiomeTile(GameConstants.BIOME_PLAINS, "backgrounds/biomes/PlainsTile.png");
        putBiomeTile(GameConstants.BIOME_FOREST, "backgrounds/biomes/ForestTile.png");
        putBiomeTile(GameConstants.BIOME_JUNGLE, "backgrounds/biomes/JungleTile.png");
        putBiomeTile(GameConstants.BIOME_SWAMP, "backgrounds/biomes/SwampTile.png");
        putBiomeTile(GameConstants.BIOME_TUNDRA, "backgrounds/biomes/TundraTile.png");
        putBiomeTile(GameConstants.BIOME_TAIGA, "backgrounds/biomes/TaigaTile.png");
        putBiomeTile(GameConstants.BIOME_DESERT, "backgrounds/biomes/DesertTile.png");
        putBiomeTile(GameConstants.BIOME_URBAN, "backgrounds/biomes/UrbanTile.png");
        putBiomeTile(GameConstants.BIOME_MOUNTAIN, "backgrounds/biomes/MountainTile.png");
        putBiomeTile(GameConstants.BIOME_VOLCANIC, "backgrounds/biomes/VolcanicTile.png");
        putBiomeTile(GameConstants.BIOME_LAKE, "backgrounds/biomes/LakeTile.png");
        putBiomeTile(GameConstants.BIOME_OCEAN, "backgrounds/biomes/OceanTile.png");
    }

    private void putBiomeTile(Biome biome, String path) {
        Image image = UiResourceLoader.loadImage(MenuChaoticPanel.class, "/" + path);
        if (image == null) {
            image = UiResourceLoader.loadImage(MenuChaoticPanel.class, path);
        }
        if (image != null) {
            biomeTileCache.put(biome.getId(), image);
        }
    }
}
