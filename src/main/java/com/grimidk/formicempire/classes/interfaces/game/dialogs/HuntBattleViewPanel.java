package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.HuntBattleFallenBody;
import com.grimidk.formicempire.classes.entities.services.colony.HuntBattleParticipant;
import com.grimidk.formicempire.classes.entities.services.colony.HuntBattleState;
import com.grimidk.formicempire.classes.entities.services.colony.HuntCreatureCombatService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RouteViewVisuals;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HuntBattleViewPanel extends JPanel {

    public enum BattleKind {
        HUNT,
        INVASION
    }

    private static final int HEADER_HEIGHT = 72;
    private static final int ANT_SPRITE_SCALE = 2;
    private static final int CRITTER_SPRITE_SCALE = 1;
    private static final int ANIMATION_FRAME_MS = 50;
    private static final float ORBIT_RADIUS_MIN = 32f;
    private static final float ORBIT_RADIUS_MAX = 68f;

    private final Colony colony;
    private final Engine engine;
    private final int battleId;
    private final BattleKind battleKind;
    private final JLabel statusLabel = new JLabel();
    private final Map<Integer, Image> biomeTileCache = new HashMap<>();
    private HuntBattleState state;
    private Timer animationTimer;
    private float animationSeconds;
    private long lastTickNanos;

    public HuntBattleViewPanel(Colony colony, Engine engine, int battleId) {
        this(colony, engine, battleId, BattleKind.HUNT);
    }

    public HuntBattleViewPanel(Colony colony, Engine engine, int battleId, BattleKind battleKind) {
        super(new BorderLayout());
        this.colony = colony;
        this.engine = engine;
        this.battleId = battleId;
        this.battleKind = battleKind != null ? battleKind : BattleKind.HUNT;
        setOpaque(true);
        setBackground(AssetStyles.BACKGROUND_COLOR);

        statusLabel.setFont(AssetStyles.FONT_NORMAL);
        statusLabel.setForeground(AssetStyles.FONT_COLOR);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        add(statusLabel, BorderLayout.NORTH);

        loadBiomeTiles();
        refreshScene();
        startAnimation();
    }

    public Colony getColony() {
        return colony;
    }

    public int getBattleId() {
        return battleId;
    }

    public BattleKind getBattleKind() {
        return battleKind;
    }

    public int getTargetId() {
        return battleId;
    }

    public void refreshScene() {
        state = resolveBattleState();
        updateStatusLabel();
    }

    private HuntBattleState resolveBattleState() {
        if (colony == null) {
            return null;
        }
        if (battleKind == BattleKind.INVASION) {
            return HuntCreatureCombatService.getInvasionState(colony, battleId);
        }
        return HuntCreatureCombatService.getState(colony, battleId);
    }

    private void updateStatusLabel() {
        if (state == null || state.getSpecies() == null) {
            statusLabel.setText(LanguageStrings.get(battleKind == BattleKind.INVASION
                    ? LanguageStrings.INVASION_BATTLE_UNAVAILABLE
                    : LanguageStrings.HUNT_BATTLE_UNAVAILABLE));
            return;
        }
        String bugName = state.getSpecies().getName();
        int bugPct = Math.round(state.getBugHealthRatio() * 100f);
        int fighters = state.livingHunters().size();
        if (battleKind == BattleKind.INVASION) {
            statusLabel.setText(LanguageStrings.format(
                    LanguageStrings.INVASION_BATTLE_STATUS_FMT, bugName, bugPct, fighters));
        } else {
            statusLabel.setText(LanguageStrings.format(
                    LanguageStrings.HUNT_BATTLE_STATUS_FMT, bugName, bugPct, fighters));
        }
    }

    public void startAnimation() {
        if (animationTimer != null) {
            animationTimer.start();
            return;
        }
        lastTickNanos = System.nanoTime();
        animationTimer = new Timer(ANIMATION_FRAME_MS, e -> {
            long now = System.nanoTime();
            animationSeconds += (now - lastTickNanos) / 1_000_000_000f;
            lastTickNanos = now;
            repaint();
        });
        animationTimer.start();
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    @Override
    public void removeNotify() {
        stopAnimation();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int fieldY = HEADER_HEIGHT;
        int fieldH = Math.max(120, getHeight() - fieldY);
        int fieldW = getWidth();
        Biome biome = resolveBiome();
        drawFullBiomeField(g2d, 0, fieldY, fieldW, fieldH, biome);

        int centerX = fieldW / 2;
        int centerY = fieldY + fieldH / 2;
        float radius = orbitRadius(fieldH);
        int slotCount = Math.max(1, state.getOrbitSlotCount());
        drawFallenHunters(g2d, centerX, centerY, radius, slotCount);
        List<HuntBattleParticipant> living = state.livingHunters();
        drawOrbitingHunters(g2d, living, centerX, centerY, radius, slotCount);
        drawCenterCritter(g2d, centerX, centerY, radius, slotCount);
        g2d.dispose();
    }

    private float orbitRadius(int fieldH) {
        return Math.max(ORBIT_RADIUS_MIN, Math.min(fieldH * 0.18f, ORBIT_RADIUS_MAX));
    }

    private void drawFallenHunters(Graphics2D g2d, int centerX, int centerY, float radius, int slotCount) {
        AntSpecies species = colony.getDynasty() != null ? colony.getDynasty().getSpecies() : GameConstants.SPECIES_OMNI;
        for (HuntBattleFallenBody body : state.getFallenBodies()) {
            double angle = slotAngle(body.getOrbitSlot(), slotCount);
            int drawX = centerX + (int) Math.round(Math.cos(angle) * radius);
            int drawY = centerY + (int) Math.round(Math.sin(angle) * radius);
            ImageIcon icon = GameConstants.getAntSprite(
                    GameConstants.TYPE_DEAD,
                    species,
                    body.getSubtypeProfile(),
                    1,
                    1,
                    1,
                    1,
                    false);
            drawScaledSprite(g2d, icon, drawX, drawY, ANT_SPRITE_SCALE, body.getBodyRotationDegrees());
        }
    }

    private void drawOrbitingHunters(Graphics2D g2d, List<HuntBattleParticipant> hunters, int centerX, int centerY,
            float radius, int slotCount) {
        if (hunters.isEmpty()) {
            return;
        }
        for (HuntBattleParticipant hunter : hunters) {
            Ant ant = hunter.getAnt();
            if (ant == null) {
                continue;
            }
            double angle = slotAngle(hunter.getOrbitSlot(), slotCount);
            int drawX = centerX + (int) Math.round(Math.cos(angle) * radius);
            int drawY = centerY + (int) Math.round(Math.sin(angle) * radius);
            AntType type = ant.getAntType();
            AntSpecies species = colony.getDynasty() != null ? colony.getDynasty().getSpecies() : GameConstants.SPECIES_OMNI;
            int slot = hunter.getOrbitSlot();
            int legFrame = RouteViewVisuals.resolveLegFrame(type, false, true, slot * 0.7f, animationSeconds, 1f);
            int jawFrame = RouteViewVisuals.resolveJawFrame(type, false, slot * 0.7f, animationSeconds, 1f);
            int wingFrame = 1;
            int antennaFrame = RouteViewVisuals.resolveAntennaFrame(type, slot * 0.7f, animationSeconds, 1f);
            ImageIcon icon = GameConstants.getAntSprite(type, species, ant.getSubtypeProfile(), legFrame, jawFrame,
                    wingFrame, antennaFrame, false);
            float faceDegrees = (float) Math.toDegrees(Math.atan2(centerY - drawY, centerX - drawX)) + 90f;
            drawScaledSprite(g2d, icon, drawX, drawY, ANT_SPRITE_SCALE, faceDegrees);
        }
    }

    private double slotAngle(int slot, int slotCount) {
        return Math.PI * 2.0 * slot / Math.max(1, slotCount);
    }

    private void drawCenterCritter(Graphics2D g2d, int centerX, int centerY, float radius, int slotCount) {
        Species species = state.getSpecies();
        if (species == null) {
            return;
        }
        float faceAngle = 0f;
        if (slotCount > 0 && !state.livingHunters().isEmpty()) {
            int focusSlot = state.getFocusOrbitIndex();
            double angle = slotAngle(focusSlot, slotCount);
            int targetX = centerX + (int) Math.round(Math.cos(angle) * radius);
            int targetY = centerY + (int) Math.round(Math.sin(angle) * radius);
            faceAngle = (float) Math.toDegrees(Math.atan2(targetY - centerY, targetX - centerX)) + 90f;
        }
        int legFrame = RouteViewVisuals.resolveCritterLegFrame(species, true, animationSeconds, animationSeconds, 1f);
        int antennaFrame = RouteViewVisuals.resolveCritterAntennaFrame(species, animationSeconds, animationSeconds,
                1f);
        ImageIcon icon = GameConstants.getCritterSprite(species, legFrame, antennaFrame);
        drawScaledSprite(g2d, icon, centerX, centerY, CRITTER_SPRITE_SCALE, faceAngle);
    }

    private void drawScaledSprite(Graphics2D g2d, ImageIcon icon, int centerX, int centerY, int scale,
            float angleDegrees) {
        if (icon == null) {
            return;
        }
        Image image = icon.getImage();
        int w = Math.max(8, icon.getIconWidth() / scale);
        int h = Math.max(8, icon.getIconHeight() / scale);
        AffineTransform old = g2d.getTransform();
        g2d.translate(centerX, centerY);
        g2d.rotate(Math.toRadians(angleDegrees));
        g2d.drawImage(image, -w / 2, -h / 2, w, h, this);
        g2d.setTransform(old);
    }

    private Biome resolveBiome() {
        World world = engine != null ? engine.getWorld() : null;
        if (world == null || colony == null) {
            return GameConstants.BIOME_PLAINS;
        }
        com.grimidk.formicempire.classes.entities.Hex hex = world.getHexOfColony(colony);
        return hex != null && hex.getBiome() != null ? hex.getBiome() : GameConstants.BIOME_PLAINS;
    }

    private void drawFullBiomeField(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH, Biome biome) {
        Image tile = tileForBiome(biome);
        int tileW = tile != null && tile.getWidth(this) > 0 ? tile.getWidth(this) : 64;
        int tileH = tile != null && tile.getHeight(this) > 0 ? tile.getHeight(this) : 64;
        if (tile == null) {
            g2d.setColor(AssetStyles.UI_BG_SECONDARY);
            g2d.fillRect(fieldX, fieldY, fieldW, fieldH);
            return;
        }
        for (int y = fieldY; y < fieldY + fieldH; y += tileH) {
            for (int x = fieldX; x < fieldX + fieldW; x += tileW) {
                g2d.drawImage(tile, x, y, this);
            }
        }
    }

    private void loadBiomeTiles() {
        putBiomeTile(GameConstants.BIOME_PLAINS, "backgrounds/biomes/PlainsTile.png");
        putBiomeTile(GameConstants.BIOME_FOREST, "backgrounds/biomes/ForestTile.png");
        putBiomeTile(GameConstants.BIOME_JUNGLE, "backgrounds/biomes/JungleTile.png");
        putBiomeTile(GameConstants.BIOME_SWAMP, "backgrounds/biomes/SwampTile.png");
        putBiomeTile(GameConstants.BIOME_TAIGA, "backgrounds/biomes/TaigaTile.png");
        putBiomeTile(GameConstants.BIOME_URBAN, "backgrounds/biomes/UrbanTile.png");
    }

    private void putBiomeTile(Biome biome, String path) {
        Image image = UiResourceLoader.loadImage(HuntBattleViewPanel.class, "/" + path);
        if (image == null) {
            image = UiResourceLoader.loadImage(HuntBattleViewPanel.class, path);
        }
        if (image != null) {
            biomeTileCache.put(biome.getId(), image);
        }
    }

    private Image tileForBiome(Biome biome) {
        if (biome == null) {
            return biomeTileCache.get(GameConstants.BIOME_PLAINS.getId());
        }
        Image tile = biomeTileCache.get(biome.getId());
        return tile != null ? tile : biomeTileCache.get(GameConstants.BIOME_PLAINS.getId());
    }
}
