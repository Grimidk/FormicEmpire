package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleScene;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleSceneBuilder;
import com.grimidk.formicempire.classes.entities.services.world.WarService;
import com.grimidk.formicempire.classes.entities.services.world.WarStagePhase;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
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
    /** How close marching ants sit to the contact line (0 = rear edge, 1 = line). */
    private static final float CONTACT_DEPTH_BASE = 0.97f;
    private static final float CONTACT_DEPTH_SPREAD = 0.025f;
    /** Wall-clock animation; independent of sim speed and pause. */
    private static final int ANIMATION_FRAME_MS = 50;
    private static final float WOBBLE_SPEED = 5.5f;
    private static final float MARCH_SPEED = 2.8f;
    private static final float WOBBLE_AMPLITUDE_PX = 2f;
    private static final float MARCH_DEPTH_AMPLITUDE = 0.018f;

    private final War war;
    private final Engine engine;
    private final JLabel statusLabel = new JLabel();
    private final Map<Integer, Image> biomeTileCache = new HashMap<>();
    private WarBattleScene scene;
    private List<BattleAnt> attackerAnts = List.of();
    private List<BattleAnt> defenderAnts = List.of();
    private float contactLineRatio = 0.5f;
    private Timer animationTimer;
    private float animationSeconds;

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
            contactLineRatio = scene.getFrontlineRatio();
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
        animationTimer = new Timer(ANIMATION_FRAME_MS, e -> {
            animationSeconds += ANIMATION_FRAME_MS / 1000f;
            if (animationSeconds > 10_000f) {
                animationSeconds = 0f;
            }
            repaint();
        });
        animationTimer.setCoalesce(true);
        animationTimer.start();
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

    /** Entire field uses one biome (hex defense on the contested colony). */
    private void drawFullBiomeField(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH, Biome biome) {
        Image tile = tileForBiome(biome);
        int tileW = tileWidth(tile);
        int tileH = tileHeight(tile);
        tileRegion(g2d, tile, fieldX, fieldY, fieldW, fieldH, tileW, tileH);
    }

    /** Fixed 50/50 biome split; tiles at native size like {@link com.grimidk.formicempire.classes.interfaces.game.gamepanels.GameAreaPanel}. */
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
        int edgePad = 1;
        int fieldRight = field.x + field.width;
        double faceRadians = attackerSide ? Math.toRadians(90) : Math.toRadians(270);
        for (BattleAnt ant : ants) {
            ImageIcon icon = GameConstants.getAntSprite(ant.type, ant.species, ant.profile);
            if (icon == null) {
                continue;
            }
            Image image = icon.getImage();
            int w = Math.max(8, icon.getIconWidth() / ANT_SPRITE_SCALE);
            int h = Math.max(8, icon.getIconHeight() / ANT_SPRITE_SCALE);

            float depth = ant.animatedContactDepth(animationSeconds);
            float wobble = (float) Math.sin(ant.wobblePhase + animationSeconds * WOBBLE_SPEED * ant.motionRate)
                    * WOBBLE_AMPLITUDE_PX;
            int drawY = field.y + edgePad + Math.round(ant.laneY * (field.height - h - edgePad * 2)) + Math.round(wobble);

            int drawX;
            if (attackerSide) {
                int marchSpan = Math.max(1, contactLineX - field.x - w - edgePad);
                drawX = field.x + edgePad + Math.round(depth * marchSpan);
            } else {
                int marchSpan = Math.max(1, fieldRight - contactLineX - w - edgePad);
                drawX = contactLineX + edgePad + Math.round((1f - depth) * marchSpan);
            }

            AffineTransform old = g2d.getTransform();
            double cx = drawX + w / 2.0;
            double cy = drawY + h / 2.0;
            g2d.translate(cx, cy);
            g2d.rotate(faceRadians);
            g2d.drawImage(image, -w / 2, -h / 2, w, h, this);
            g2d.setTransform(old);
        }
    }

    private List<BattleAnt> buildAntPool(WarBattleScene.Side side, boolean attackerSide) {
        if (side == null || side.typeCounts().isEmpty()) {
            return List.of();
        }
        int total = side.typeCounts().values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) {
            return List.of();
        }

        Dynasty dynasty = resolveDynasty(side.dynastyId());
        int visualCap = WarBattleScene.MAX_VISUAL_ANTS_PER_SIDE;
        int visualTotal = Math.min(total, visualCap);
        Map<AntType, Integer> allocated = allocateVisualCounts(side.typeCounts(), total, visualTotal);

        List<BattleAnt> ants = new ArrayList<>(visualTotal);
        Random random = new Random(side.dynastyId() ^ (attackerSide ? 17 : 31));
        for (Map.Entry<AntType, Integer> entry : allocated.entrySet()) {
            AntType type = entry.getKey();
            if (type == null || type == GameConstants.TYPE_DEAD || type == GameConstants.TYPE_DRONE) {
                continue;
            }
            for (int i = 0; i < entry.getValue(); i++) {
                float laneY = random.nextFloat();
                float depthSpread = random.nextFloat() * CONTACT_DEPTH_SPREAD;
                AntSubtypeProfile profile = dynasty != null
                        ? AntSubtypeService.sampleProfileFromDynasty(dynasty, type)
                        : AntSubtypeProfile.standard();
                ants.add(new BattleAnt(type, side.species(), profile, laneY,
                        CONTACT_DEPTH_BASE + depthSpread,
                        random.nextFloat() * (float) (Math.PI * 2),
                        0.85f + random.nextFloat() * 0.3f));
            }
        }
        return ants;
    }

    private Dynasty resolveDynasty(int dynastyId) {
        if (dynastyId <= 0 || engine == null || engine.getWorld() == null) {
            return null;
        }
        return engine.getWorld().findDynastyById(dynastyId);
    }

    /** Largest-remainder allocation so on-screen mix matches deployed type counts. */
    private static Map<AntType, Integer> allocateVisualCounts(Map<AntType, Integer> typeCounts, int total,
            int visualTotal) {
        Map<AntType, Integer> allocated = new HashMap<>();
        if (visualTotal <= 0 || total <= 0) {
            return allocated;
        }
        if (visualTotal >= total) {
            for (Map.Entry<AntType, Integer> entry : typeCounts.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                    allocated.put(entry.getKey(), entry.getValue());
                }
            }
            return allocated;
        }

        int assigned = 0;
        List<RemainderSlot> remainders = new ArrayList<>();
        for (Map.Entry<AntType, Integer> entry : typeCounts.entrySet()) {
            AntType type = entry.getKey();
            int count = entry.getValue() != null ? entry.getValue() : 0;
            if (type == null || count <= 0) {
                continue;
            }
            float exact = count * (visualTotal / (float) total);
            int floor = (int) exact;
            allocated.put(type, floor);
            assigned += floor;
            remainders.add(new RemainderSlot(type, exact - floor));
        }
        remainders.sort((a, b) -> Float.compare(b.fraction, a.fraction));
        int slotsLeft = visualTotal - assigned;
        for (int i = 0; i < slotsLeft && i < remainders.size(); i++) {
            AntType type = remainders.get(i).type;
            allocated.merge(type, 1, Integer::sum);
        }
        return allocated;
    }

    private record RemainderSlot(AntType type, float fraction) {}

    private String buildHeaderText(WarBattleScene scene) {
        int attackerAnts = sumTypeCounts(scene.getAttacker().typeCounts());
        int defenderAnts = sumTypeCounts(scene.getDefender().typeCounts());
        String phase = formatPhase(scene.getPhase());
        String location = LanguageStrings.format(LanguageStrings.BATTLE_LOCATION_FMT, scene.getLocationName());
        String attackerPower = LanguageStrings.format(
                LanguageStrings.BATTLE_POWER_FMT,
                scene.getAttacker().dynastyName(),
                AssetStyles.formatNumber(scene.getAttacker().deployedPower()));
        String defenderPower = LanguageStrings.format(
                LanguageStrings.BATTLE_POWER_FMT,
                scene.getDefender().dynastyName(),
                AssetStyles.formatNumber(scene.getDefender().deployedPower()));
        String progress = LanguageStrings.format(
                LanguageStrings.WAR_PROGRESS_FMT,
                Math.round(scene.getWarProgressPercent()));
        String forces = LanguageStrings.format(
                LanguageStrings.BATTLE_FORCES_FMT,
                AssetStyles.formatNumber(attackerAnts),
                AssetStyles.formatNumber(defenderAnts));
        if (scene.getPhase() == WarStagePhase.REDEPLOYING) {
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
        if (phase == WarStagePhase.RESERVE_ASSAULT) {
            return LanguageStrings.get(LanguageStrings.BATTLE_PHASE_RESERVE);
        }
        if (phase == WarStagePhase.REDEPLOYING) {
            return LanguageStrings.get(LanguageStrings.BATTLE_PHASE_REDEPLOY);
        }
        return LanguageStrings.get(LanguageStrings.BATTLE_PHASE_CLASH);
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
        private final Species species;
        private final AntSubtypeProfile profile;
        private final float laneY;
        private final float contactDepth;
        private final float wobblePhase;
        private final float motionRate;

        private BattleAnt(AntType type, Species species, AntSubtypeProfile profile, float laneY, float contactDepth,
                float wobblePhase, float motionRate) {
            this.type = type;
            this.species = species;
            this.profile = profile != null ? profile : AntSubtypeProfile.standard();
            this.laneY = laneY;
            this.contactDepth = Math.min(0.995f, contactDepth);
            this.wobblePhase = wobblePhase;
            this.motionRate = motionRate;
        }

        private float animatedContactDepth(float seconds) {
            float march = (float) Math.sin(wobblePhase + seconds * MARCH_SPEED * motionRate) * MARCH_DEPTH_AMPLITUDE;
            return Math.max(0.05f, Math.min(0.995f, contactDepth + march));
        }
    }
}
