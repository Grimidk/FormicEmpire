package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.TradeMethod;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.entities.services.colony.ConvoyScene;
import com.grimidk.formicempire.classes.entities.services.colony.ConvoySceneBuilder;
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

public class ConvoyViewPanel extends JPanel {

    private static final int HEADER_HEIGHT = 72;
    private static final int FIELD_MARGIN = 0;
    private static final int ANT_SPRITE_SCALE = 2;
    private static final int ANIMATION_FRAME_MS = 50;
    private static final float SCROLL_SPEED_PX = 48f;

    private final Trade trade;
    private final Engine engine;
    private final JLabel statusLabel = new JLabel();
    private final Map<Integer, Image> biomeTileCache = new HashMap<>();
    private ConvoyScene scene;
    private List<ConvoyAnt> convoyAnts = List.of();
    private Timer animationTimer;
    private float animationSeconds;

    public ConvoyViewPanel(Trade trade, Engine engine) {
        super(new BorderLayout());
        this.trade = trade;
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
        scene = ConvoySceneBuilder.build(world, trade);
        if (scene.isAvailable()) {
            convoyAnts = buildAntPool(scene);
            statusLabel.setText(buildHeaderText(scene));
        } else {
            convoyAnts = List.of();
            statusLabel.setText(LanguageStrings.get(LanguageStrings.CONVOY_NOT_AVAILABLE));
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
            String msg = LanguageStrings.get(LanguageStrings.CONVOY_NOT_AVAILABLE);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, fieldY + fieldH / 2);
            g2d.dispose();
            return;
        }

        boolean travelingRight = !scene.isReturning();
        int scrollOffset = computeScrollOffset(fieldW, travelingRight);
        drawScrollingField(g2d, fieldX, fieldY, fieldW, fieldH, scrollOffset, scene);

        Rectangle field = new Rectangle(fieldX, fieldY, fieldW, fieldH);
        drawConvoyFormation(g2d, convoyAnts, field, travelingRight);

        g2d.dispose();
    }

    private int computeScrollOffset(int fieldW, boolean travelingRight) {
        Image tile = tileForScene(scene);
        int tileW = tileWidth(tile);
        if (tileW <= 0) {
            tileW = 64;
        }
        int raw = (int) (animationSeconds * SCROLL_SPEED_PX) % tileW;
        if (travelingRight) {
            return tileW - raw;
        }
        return raw;
    }

    private void drawScrollingField(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH,
            int scrollOffset, ConvoyScene scene) {
        Image tile = tileForScene(scene);
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

    private Image tileForScene(ConvoyScene scene) {
        return switch (scene.getBackgroundKind()) {
            case SEA -> iconImage(GameConstants.CONVOY_TILE_SEA);
            case SKY -> iconImage(GameConstants.CONVOY_TILE_SKY);
            case TUNNEL -> iconImage(GameConstants.CONVOY_TILE_UNDERGROUND);
            case LAND_BIOME -> tileForBiome(scene.getLandBiome());
        };
    }

    private static Image iconImage(javax.swing.ImageIcon icon) {
        return icon != null ? icon.getImage() : null;
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

    private void drawConvoyFormation(Graphics2D g2d, List<ConvoyAnt> ants, Rectangle field, boolean travelingRight) {
        if (ants.isEmpty() || field.width <= 4 || field.height <= 4) {
            return;
        }
        int centerX = field.x + field.width / 2;
        int centerY = field.y + field.height / 2;
        float radiusX = Math.min(field.width * 0.2f, 56f + ants.size() * 0.5f);
        float radiusY = Math.min(field.height * 0.34f, 44f + ants.size() * 0.38f);
        double faceRadians = travelingRight ? Math.toRadians(90) : Math.toRadians(270);

        for (ConvoyAnt ant : ants) {
            ImageIcon icon = GameConstants.getAntSprite(ant.type, ant.species, ant.profile);
            if (icon == null) {
                continue;
            }
            Image image = icon.getImage();
            int w = Math.max(8, icon.getIconWidth() / ANT_SPRITE_SCALE);
            int h = Math.max(8, icon.getIconHeight() / ANT_SPRITE_SCALE);

            int drawX = centerX + Math.round(ant.offsetX * radiusX);
            int drawY = centerY + Math.round(ant.offsetY * radiusY);

            AffineTransform old = g2d.getTransform();
            double cx = drawX + w / 2.0;
            double cy = drawY + h / 2.0;
            g2d.translate(cx, cy);
            g2d.rotate(faceRadians);
            g2d.drawImage(image, -w / 2, -h / 2, w, h, this);
            g2d.setTransform(old);
        }
    }

    private List<ConvoyAnt> buildAntPool(ConvoyScene scene) {
        List<Ant> tripAnts = new ArrayList<>();
        if (trade != null && trade.getAntsOnTrip() != null) {
            for (Ant ant : trade.getAntsOnTrip()) {
                if (ant == null || !ant.isAlive() || ant.getAntType() == null) {
                    continue;
                }
                AntType type = ant.getAntType();
                if (type == GameConstants.TYPE_DEAD || type == GameConstants.TYPE_DRONE) {
                    continue;
                }
                tripAnts.add(ant);
            }
        }

        if (!tripAnts.isEmpty()) {
            int visualTotal = Math.min(tripAnts.size(), ConvoyScene.MAX_VISUAL_ANTS);
            List<ConvoyAnt> ants = new ArrayList<>(visualTotal);
            Random random = new Random(scene.getOriginName().hashCode() ^ scene.getDestinationName().hashCode());
            for (int i = 0; i < visualTotal; i++) {
                Ant source = tripAnts.get(i % tripAnts.size());
                float angle = random.nextFloat() * (float) (Math.PI * 2);
                float dist = (float) Math.sqrt(random.nextFloat());
                AntSubtypeProfile profile = source.getSubtypeProfile() != null
                        ? source.getSubtypeProfile()
                        : AntSubtypeProfile.standard();
                ants.add(new ConvoyAnt(source.getAntType(), scene.getSpecies(), profile,
                        (float) Math.cos(angle) * dist,
                        (float) Math.sin(angle) * dist));
            }
            return ants;
        }

        if (scene.typeCounts().isEmpty()) {
            return List.of();
        }
        int total = scene.typeCounts().values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) {
            return List.of();
        }

        int visualCap = ConvoyScene.MAX_VISUAL_ANTS;
        int visualTotal = Math.min(total, visualCap);
        Map<AntType, Integer> allocated = allocateVisualCounts(scene.typeCounts(), total, visualTotal);
        Colony originColony = trade != null && trade.getOrigin() != null ? trade.getOrigin().getColony() : null;

        List<ConvoyAnt> ants = new ArrayList<>(visualTotal);
        Random random = new Random(scene.getOriginName().hashCode() ^ scene.getDestinationName().hashCode());
        for (Map.Entry<AntType, Integer> entry : allocated.entrySet()) {
            AntType type = entry.getKey();
            if (type == null || type == GameConstants.TYPE_DEAD || type == GameConstants.TYPE_DRONE) {
                continue;
            }
            for (int i = 0; i < entry.getValue(); i++) {
                float angle = random.nextFloat() * (float) (Math.PI * 2);
                float dist = (float) Math.sqrt(random.nextFloat());
                AntSubtypeProfile profile = originColony != null
                        ? AntSubtypeService.sampleProfileFromColony(originColony, type)
                        : AntSubtypeProfile.standard();
                ants.add(new ConvoyAnt(type, scene.getSpecies(), profile,
                        (float) Math.cos(angle) * dist,
                        (float) Math.sin(angle) * dist));
            }
        }
        return ants;
    }

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

    private String buildHeaderText(ConvoyScene scene) {
        String leg = scene.isReturning()
                ? LanguageStrings.get(LanguageStrings.UI_RETURNING)
                : LanguageStrings.get(LanguageStrings.UI_TRANSIT);
        String route = LanguageStrings.format(
                LanguageStrings.CONVOY_ROUTE_FMT,
                scene.getOriginName(),
                scene.getDestinationName());
        TradeMethod method = scene.getMethod();
        String methodName = method != null ? method.getName() : "";
        String progress = LanguageStrings.format(
                LanguageStrings.CONVOY_PROGRESS_FMT,
                leg,
                AssetStyles.formatNumber(scene.getRemainingHours()),
                AssetStyles.formatNumber(scene.getTotalHours()),
                Math.round(scene.getLegProgress() * 100f));
        String forces = LanguageStrings.format(
                LanguageStrings.CONVOY_FORCES_FMT,
                AssetStyles.formatNumber(sumTypeCounts(scene.typeCounts())));
        return route + "  |  " + methodName + "  |  " + progress + "  |  " + forces;
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
        Image image = UiResourceLoader.loadImage(ConvoyViewPanel.class, "/" + path);
        if (image == null) {
            image = UiResourceLoader.loadImage(ConvoyViewPanel.class, path);
        }
        if (image != null) {
            biomeTileCache.put(biome.getId(), image);
        }
    }

    private static final class ConvoyAnt {
        private final AntType type;
        private final AntSpecies species;
        private final AntSubtypeProfile profile;
        private final float offsetX;
        private final float offsetY;

        private ConvoyAnt(AntType type, AntSpecies species, AntSubtypeProfile profile, float offsetX, float offsetY) {
            this.type = type;
            this.species = species;
            this.profile = profile != null ? profile : AntSubtypeProfile.standard();
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }
}
