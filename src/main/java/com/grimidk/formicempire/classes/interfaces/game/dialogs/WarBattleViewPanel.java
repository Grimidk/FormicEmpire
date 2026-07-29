package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.constants.dynasty.WarStagePhase;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleScene;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleSceneBuilder;
import com.grimidk.formicempire.classes.entities.services.world.WarService;
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
import java.util.Random;

public class WarBattleViewPanel extends JPanel {

    private static final int HEADER_HEIGHT = 72;
    private static final int FIELD_MARGIN = 0;
    private static final int ANT_SPRITE_SCALE = 2;
    private static final int ANIMATION_FRAME_MS = 50;
    private static final float WOBBLE_SPEED = 5.5f;
    private static final float WOBBLE_AMPLITUDE_PX = 2f;
    private static final int LINE_JITTER_PX = 10;
    private static final int RESERVE_WALK_PX_PER_SEC = 28;
    private static final float AIR_SUPPORT_FLY_CYCLE_SEC = 14.0f;
    private static final float AIR_SUPPORT_FLY_PORTION = 0.36f;

    private final War war;
    private final Engine engine;
    private final JLabel statusLabel = new JLabel();
    private final Map<Integer, Image> biomeTileCache = new HashMap<>();
    private WarBattleScene scene;
    private List<BattleAnt> attackerAnts = List.of();
    private List<BattleAnt> defenderAnts = List.of();
    private float contactLineRatio = 0.5f;
    private float targetContactLineRatio = 0.5f;
    private Timer animationTimer;
    private float animationSeconds;
    private long lastTickNanos;

    public WarBattleViewPanel(War war, Engine engine) {
        super(new BorderLayout());
        this.war = war;
        this.engine = engine;
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

    public void refreshScene() {
        World world = engine != null ? engine.getWorld() : null;
        WarService warService = world != null ? world.getWarService() : null;
        scene = WarBattleSceneBuilder.build(world, war, warService);
        if (scene.isAvailable()) {
            targetContactLineRatio = scene.getFrontlineRatio();
            attackerAnts = buildAntPool(scene.getAttacker(), true);
            defenderAnts = buildAntPool(scene.getDefender(), false);
            statusLabel.setText(buildHeaderText(scene));
        } else {
            attackerAnts = List.of();
            defenderAnts = List.of();
            statusLabel.setText(LanguageStrings.get(LanguageStrings.BATTLE_NOT_AVAILABLE));
        }
        repaint();
    }

    public void startAnimation() {
        if (animationTimer != null) {
            return;
        }
        lastTickNanos = System.nanoTime();
        int intervalMs = engine != null ? engine.getVisualFrameIntervalMs() : ANIMATION_FRAME_MS;
        animationTimer = new Timer(intervalMs, e -> {
            long now = System.nanoTime();
            float deltaSec = (now - lastTickNanos) / 1_000_000_000f;
            lastTickNanos = now;
            if (deltaSec <= 0f || deltaSec > 0.25f) {
                int fallbackMs = animationTimer != null ? animationTimer.getDelay() : intervalMs;
                deltaSec = Math.max(1, fallbackMs) / 1000f;
            }
            animationSeconds += deltaSec;
            if (animationSeconds > 10_000f) {
                animationSeconds = 0f;
            }
            float delta = targetContactLineRatio - contactLineRatio;
            if (Math.abs(delta) > 0.0005f) {
                float step = 1f - (float) Math.exp(-1.237f * deltaSec);
                contactLineRatio += delta * step;
            } else {
                contactLineRatio = targetContactLineRatio;
            }
            repaint();
        });
        animationTimer.setCoalesce(true);
        animationTimer.start();
    }

    public void applyVisualFrameRate() {
        if (animationTimer != null && engine != null) {
            animationTimer.setDelay(engine.getVisualFrameIntervalMs());
        }
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
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
        if (scene == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int fieldY = HEADER_HEIGHT;
        int fieldH = Math.max(120, getHeight() - fieldY);
        int fieldW = getWidth();
        int fieldX = FIELD_MARGIN;

        if (!scene.isAvailable()) {
            g2d.setColor(AssetStyles.FONT_COLOR);
            g2d.setFont(AssetStyles.FONT_NORMAL);
            String msg = LanguageStrings.get(LanguageStrings.BATTLE_NOT_AVAILABLE);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, fieldY + fieldH / 2);
            g2d.dispose();
            return;
        }

        int contactLineX = fieldX + Math.round(fieldW * contactLineRatio);
        if (scene.isDefenseOnlyBiome()) {
            drawFullBiomeField(g2d, fieldX, fieldY, fieldW, fieldH, scene.getDefenderBiome());
        } else {
            int biomeSplitX = fieldX + (fieldW / 2);
            drawBattlefieldBiomes(g2d, fieldX, fieldY, fieldW, fieldH, biomeSplitX,
                    scene.getAttackerBiome(), scene.getDefenderBiome());
        }

        Rectangle field = new Rectangle(fieldX, fieldY, fieldW, fieldH);
        drawAntColumn(g2d, attackerAnts, field, contactLineX, true);
        drawAntColumn(g2d, defenderAnts, field, contactLineX, false);

        g2d.dispose();
    }

    private void drawFullBiomeField(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH, Biome biome) {
        Image tile = tileForBiome(biome);
        int tileW = tileWidth(tile);
        int tileH = tileHeight(tile);
        tileRegion(g2d, tile, fieldX, fieldY, fieldW, fieldH, tileW, tileH);
    }

    private void drawBattlefieldBiomes(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH,
            int splitX, Biome leftBiome, Biome rightBiome) {
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

    private void tileRegion(Graphics2D g2d, Image tile, int originX, int originY, int regionW, int regionH,
            int tileW, int tileH) {
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

    private void drawAntColumn(Graphics2D g2d, List<BattleAnt> ants, Rectangle field, int contactLineX,
            boolean attackerSide) {
        if (ants.isEmpty() || field.width <= 4 || field.height <= 4) {
            return;
        }
        int edgePad = 4;
        int fieldRight = field.x + field.width;
        for (BattleAnt ant : ants) {
            boolean winged = RouteViewVisuals.isWinged(ant.type);
            boolean airSupport = !ant.reserve && ant.battleLine == GameConstants.BATTLE_LINE_AIR_SUPPORT;
            int jawFrame = RouteViewVisuals.resolveJawFrame(ant.type, ant.reserve, ant.wobblePhase, animationSeconds,
                    ant.motionRate);
            int wingFrame = RouteViewVisuals.resolveWingFrame(ant.type, airSupport && winged, ant.wobblePhase,
                    animationSeconds, ant.motionRate);

            ImageIcon icon = GameConstants.getAntSprite(
                    ant.type, ant.species, ant.profile, 1, jawFrame, wingFrame);
            Image image = icon != null ? icon.getImage() : null;
            int w = icon != null ? Math.max(8, icon.getIconWidth() / ANT_SPRITE_SCALE) : 12;
            int h = icon != null ? Math.max(8, icon.getIconHeight() / ANT_SPRITE_SCALE) : 12;

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
                int lineOffset = lineOffsetPx(ant.battleLine, ant.reserve);
                if (ant.reserve) {
                    lineOffset = Math.max(WarBattleScene.ARTILLERY_LINE_OFFSET_PX,
                            lineOffset - Math.round(animationSeconds * RESERVE_WALK_PX_PER_SEC * ant.motionRate));
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

            AffineTransform old = g2d.getTransform();
            double cx = drawX + w / 2.0;
            double cy = drawY + h / 2.0;
            g2d.translate(cx, cy);
            float faceAngle = RouteViewVisuals.movementFacingDegrees(attackerSide ? 1f : -1f, 0f);
            g2d.rotate(Math.toRadians(faceAngle));
            if (image != null) {
                g2d.drawImage(image, -w / 2, -h / 2, w, h, this);
            } else {
                g2d.setColor(attackerSide ? AssetStyles.FONT_COLOR : AssetStyles.FONT_COLOR_VALUE);
                g2d.fillRect(-w / 2, -h / 2, w, h);
            }
            g2d.setTransform(old);
        }
    }

    private static int lineOffsetPx(BattleLine line, boolean reserve) {
        if (reserve) {
            return WarBattleScene.RESERVE_LINE_OFFSET_PX;
        }
        if (line == GameConstants.BATTLE_LINE_ARTILLERY) {
            return WarBattleScene.ARTILLERY_LINE_OFFSET_PX;
        }
        if (line == GameConstants.BATTLE_LINE_AIR_SUPPORT) {
            return WarBattleScene.AIR_SUPPORT_LINE_OFFSET_PX;
        }
        return 0;
    }

    private List<BattleAnt> buildAntPool(WarBattleScene.Side side, boolean attackerSide) {
        if (side == null) {
            return List.of();
        }
        Dynasty dynasty = resolveDynasty(side.dynastyId());
        Random random = new Random(side.dynastyId() ^ (attackerSide ? 17 : 31));
        List<BattleAnt> ants = new ArrayList<>();

        appendLineAnts(ants, side.activeByLine(), dynasty, side.species(), random, false);
        Map<BattleLine, Map<AntType, Integer>> reserveVisual =
                allocateReserveVisual(side.reserveByLine(), WarBattleScene.MAX_VISUAL_RESERVE_ANTS_PER_SIDE);
        appendLineAnts(ants, reserveVisual, dynasty, side.species(), random, true);
        return ants;
    }

    private static void appendLineAnts(List<BattleAnt> ants, Map<BattleLine, Map<AntType, Integer>> byLine,
            Dynasty dynasty, AntSpecies species, Random random, boolean reserve) {
        if (byLine == null || byLine.isEmpty()) {
            return;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            Map<AntType, Integer> typeCounts = byLine.get(line);
            if (typeCounts == null || typeCounts.isEmpty()) {
                continue;
            }
            int lineTotal = 0;
            for (Integer c : typeCounts.values()) {
                if (c != null) {
                    lineTotal += c;
                }
            }
            if (lineTotal <= 0) {
                continue;
            }
            int indexInLine = 0;
            for (Map.Entry<AntType, Integer> entry : typeCounts.entrySet()) {
                AntType type = entry.getKey();
                int count = entry.getValue() != null ? entry.getValue() : 0;
                if (type == null || type == GameConstants.TYPE_DEAD || count <= 0) {
                    continue;
                }
                if (line == GameConstants.BATTLE_LINE_AIR_SUPPORT) {
                    if (!RouteViewVisuals.isWinged(type)) {
                        continue;
                    }
                } else if (type == GameConstants.TYPE_DRONE) {
                    continue;
                }
                for (int i = 0; i < count; i++) {
                    float laneY = lineTotal <= 1 ? 0.5f : indexInLine / (float) (lineTotal - 1);
                    laneY = Math.max(0f, Math.min(1f, laneY + (random.nextFloat() - 0.5f) * 0.02f));
                    AntSubtypeProfile profile = dynasty != null
                            ? AntSubtypeService.sampleProfileFromDynasty(dynasty, type)
                            : AntSubtypeProfile.standard();
                    ants.add(new BattleAnt(type, species, profile, line, laneY, random.nextFloat(),
                            random.nextFloat() * (float) (Math.PI * 2),
                            0.85f + random.nextFloat() * 0.3f,
                            random.nextFloat(),
                            reserve));
                    indexInLine++;
                }
            }
        }
    }

    private static Map<BattleLine, Map<AntType, Integer>> allocateReserveVisual(
            Map<BattleLine, Map<AntType, Integer>> reserveByLine, int visualCap) {
        if (reserveByLine == null || reserveByLine.isEmpty() || visualCap <= 0) {
            return Map.of();
        }
        int total = 0;
        for (Map<AntType, Integer> counts : reserveByLine.values()) {
            if (counts == null) {
                continue;
            }
            for (Integer c : counts.values()) {
                if (c != null) {
                    total += c;
                }
            }
        }
        if (total <= 0) {
            return Map.of();
        }
        if (total <= visualCap) {
            return reserveByLine;
        }
        float scale = visualCap / (float) total;
        Map<BattleLine, Map<AntType, Integer>> scaled = new HashMap<>();
        int assigned = 0;
        for (Map.Entry<BattleLine, Map<AntType, Integer>> lineEntry : reserveByLine.entrySet()) {
            Map<AntType, Integer> out = new HashMap<>();
            if (lineEntry.getValue() != null) {
                for (Map.Entry<AntType, Integer> e : lineEntry.getValue().entrySet()) {
                    int count = e.getValue() != null ? e.getValue() : 0;
                    int visual = Math.max(0, Math.round(count * scale));
                    if (visual > 0 && e.getKey() != null) {
                        out.put(e.getKey(), visual);
                        assigned += visual;
                    }
                }
            }
            if (!out.isEmpty()) {
                scaled.put(lineEntry.getKey(), out);
            }
        }
        if (assigned == 0) {
            for (Map.Entry<BattleLine, Map<AntType, Integer>> lineEntry : reserveByLine.entrySet()) {
                if (lineEntry.getValue() == null) {
                    continue;
                }
                for (Map.Entry<AntType, Integer> e : lineEntry.getValue().entrySet()) {
                    if (e.getKey() != null && e.getValue() != null && e.getValue() > 0) {
                        scaled.put(lineEntry.getKey(), Map.of(e.getKey(), 1));
                        return scaled;
                    }
                }
            }
        }
        return scaled;
    }

    private Dynasty resolveDynasty(int dynastyId) {
        if (dynastyId <= 0 || engine == null || engine.getWorld() == null) {
            return null;
        }
        return engine.getWorld().findDynastyById(dynastyId);
    }

    private String buildHeaderText(WarBattleScene scene) {
        int attackerAnts = sumTypeCounts(scene.getAttacker().typeCounts());
        int defenderAnts = sumTypeCounts(scene.getDefender().typeCounts());
        String phase = formatPhase(scene.getPhase());
        String location = LanguageStrings.format(LanguageStrings.BATTLE_LOCATION_FMT, scene.getLocationName());
        String attackerPower = LanguageStrings.format(
                LanguageStrings.BATTLE_POWER_FMT,
                scene.getAttacker().dynastyName(),
                AssetStyles.formatNumber(scene.getAttacker().livingActive()));
        String defenderPower = LanguageStrings.format(
                LanguageStrings.BATTLE_POWER_FMT,
                scene.getDefender().dynastyName(),
                AssetStyles.formatNumber(scene.getDefender().livingActive()));
        String progress = LanguageStrings.format(
                LanguageStrings.WAR_PROGRESS_FMT,
                Math.round(scene.getWarProgressPercent()));
        String forces = LanguageStrings.format(
                LanguageStrings.BATTLE_FORCES_FMT,
                AssetStyles.formatNumber(attackerAnts),
                AssetStyles.formatNumber(defenderAnts));
        if (scene.getPhase() == GameConstants.WAR_STAGE_REDEPLOYING) {
            String redeploy = LanguageStrings.format(
                    LanguageStrings.BATTLE_REDEPLOY_FMT,
                    String.valueOf(scene.getRedeployHoursRemaining()));
            return scene.getWarName() + "  |  " + location + "  |  " + redeploy + "  |  " + forces + "  |  " + progress;
        }
        return scene.getWarName() + "  |  " + location + "  |  " + phase
                + "  |  " + forces + "  |  " + attackerPower + " vs " + defenderPower + "  |  " + progress;
    }

    private static int sumTypeCounts(Map<AntType, Integer> typeCounts) {
        if (typeCounts == null || typeCounts.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (Integer count : typeCounts.values()) {
            if (count != null) {
                sum += count;
            }
        }
        return sum;
    }

    private static String formatPhase(WarStagePhase phase) {
        if (phase == null) {
            return GameConstants.WAR_STAGE_ACTIVE_CLASH.getName();
        }
        return phase.getName();
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
        Image image = UiResourceLoader.loadImage(WarBattleViewPanel.class, "/" + path);
        if (image == null) {
            image = UiResourceLoader.loadImage(WarBattleViewPanel.class, path);
        }
        if (image != null) {
            biomeTileCache.put(biome.getId(), image);
        }
    }

    private static final class BattleAnt {
        private final AntType type;
        private final AntSpecies species;
        private final AntSubtypeProfile profile;
        private final BattleLine battleLine;
        private final float laneY;
        private final float laneJitter;
        private final float wobblePhase;
        private final float motionRate;
        private final float flyPhase;
        private final boolean reserve;

        private BattleAnt(AntType type, AntSpecies species, AntSubtypeProfile profile, BattleLine battleLine,
                float laneY, float laneJitter, float wobblePhase, float motionRate, float flyPhase, boolean reserve) {
            this.type = type;
            this.species = species;
            this.profile = profile != null ? profile : AntSubtypeProfile.standard();
            this.battleLine = battleLine != null ? battleLine : GameConstants.BATTLE_LINE_INFANTRY;
            this.laneY = laneY;
            this.laneJitter = laneJitter;
            this.wobblePhase = wobblePhase;
            this.motionRate = motionRate;
            this.flyPhase = flyPhase;
            this.reserve = reserve;
        }
    }
}
