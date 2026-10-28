package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.rendering.HexGridMesh;
import com.grimidk.formicempire.classes.interfaces.game.rendering.HexMapGeometry;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniMapPanel extends ZeroGamePanel {
    private final Engine engine;
    private final Runnable openMapAction;
    private final Map<Integer, Color> biomeColorCache = new HashMap<>();
    private final Map<Long, Hex> hexLookup = new HashMap<>();
    private BufferedImage cachedBakedBaseImage;
    private int cachedBakedW = -1;
    private int cachedBakedH = -1;
    private double cachedHexRadius;
    private double cachedOriginX;
    private double cachedOriginY;
    private double cachedWorldBoxX;
    private double cachedWorldBoxY;
    private double cachedWorldBoxW;
    private double cachedWorldBoxH;
    private World explicitWorld;

    public MiniMapPanel(Engine engine, Runnable openMapAction) {
        this(engine, null, openMapAction);
    }

    public MiniMapPanel(World world) {
        this(null, world, null);
    }

    public MiniMapPanel(Engine engine, World explicitWorld, Runnable openMapAction) {
        super(null);
        this.engine = engine;
        this.explicitWorld = explicitWorld;
        this.openMapAction = openMapAction;
        initComponents();
        initLayout();
    }

    public World getWorld() {
        return explicitWorld != null ? explicitWorld : (engine != null ? engine.getWorld() : null);
    }

    public void setExplicitWorld(World explicitWorld) {
        this.explicitWorld = explicitWorld;
        invalidateMapCache();
        repaint();
    }

    @Override
    protected void initComponents() {
        setPreferredSize(new Dimension(250, 180));
        setMinimumSize(new Dimension(200, 120));
        setTitledBorder(LanguageStrings.PANEL_MINIMAP);
        AssetStyles.markClickable(this);
        addMouseListener(new MouseAdapter() {
            private Point pressPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                pressPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1 || pressPoint == null) {
                    return;
                }
                Point release = e.getPoint();
                int dx = release.x - pressPoint.x;
                int dy = release.y - pressPoint.y;
                pressPoint = null;
                if (dx * dx + dy * dy > 36) {
                    return;
                }
                if (getWorld() == null || openMapAction == null) {
                    return;
                }
                openMapAction.run();
            }
        });
    }

    @Override
    protected void initLayout() {
    }

    public void invalidateMapCache() {
        cachedBakedBaseImage = null;
        cachedBakedW = -1;
        cachedBakedH = -1;
    }

    @Override
    public void refreshTheme() {
        biomeColorCache.clear();
        invalidateMapCache();
        super.refreshTheme();
        repaint();
    }

    public void refreshMap() {
        repaint();
    }

    public void reset() {
        biomeColorCache.clear();
        hexLookup.clear();
        invalidateMapCache();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        World world = getWorld();
        if (world == null) {
            return;
        }

        Insets insets = getInsets();
        int availW = getWidth() - insets.left - insets.right - 8;
        int availH = getHeight() - insets.top - insets.bottom - 8;
        if (availW <= 0 || availH <= 0) {
            return;
        }

        int panelW = getWidth();
        int panelH = getHeight();

        if (cachedBakedBaseImage == null || cachedBakedW != panelW || cachedBakedH != panelH) {
            double hexRadius;
            double originX;
            double originY;
            double worldBoxX = 0;
            double worldBoxY = 0;
            double worldBoxW = 0;
            double worldBoxH = 0;

            if (world.getWorldWidth() > 0 && world.getWorldHeight() > 0) {
                int wW = world.getWorldWidth();
                int wH = world.getWorldHeight();
                double spanX = wW * Math.sqrt(3.0);
                double spanY = (wH - 1) * 1.5 + 2.0;
                hexRadius = Math.min(availW / spanX, availH / spanY);
                if (hexRadius < 0.6) {
                    hexRadius = 0.6;
                }
                originX = insets.left + 4 + availW / 2.0;
                originY = insets.top + 4 + availH / 2.0 + 0.75 * hexRadius;

                worldBoxW = wW * Math.sqrt(3.0) * hexRadius;
                worldBoxH = spanY * hexRadius;
                worldBoxX = originX - worldBoxW / 2.0;
                worldBoxY = insets.top + 4 + (availH - worldBoxH) / 2.0;
            } else {
                int rad = world.getWorldRadius();
                double span = HexMapGeometry.hexesAcross(rad);
                hexRadius = Math.min(availW / span, availH / (span * 0.866));
                if (hexRadius < 1.0) hexRadius = 1.0;
                originX = insets.left + 4 + availW / 2.0;
                originY = insets.top + 4 + availH / 2.0;
            }

            bakeBaseImage(world, panelW, panelH, originX, originY, hexRadius,
                    worldBoxX, worldBoxY, worldBoxW, worldBoxH);
        }

        if (cachedBakedBaseImage == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.drawImage(cachedBakedBaseImage, 0, 0, null);

            double originX = cachedOriginX;
            double originY = cachedOriginY;
            double hexRadius = cachedHexRadius;
            double worldBoxX = cachedWorldBoxX;
            double worldBoxY = cachedWorldBoxY;
            double worldBoxW = cachedWorldBoxW;
            double worldBoxH = cachedWorldBoxH;

            if (world.getWorldWidth() > 0) {
                if (engine != null && engine.isMapViewportActive() && engine.getMapViewportSpanCols() > 0) {
                    double spanCols = Math.min(world.getWorldWidth(), engine.getMapViewportSpanCols());
                    double centerCol = engine.getMapViewportCenterCol();
                    double viewW = spanCols * Math.sqrt(3.0) * hexRadius;
                    double viewCenterX = originX + (centerCol * Math.sqrt(3.0) * hexRadius);
                    double leftX = viewCenterX - viewW / 2.0;

                    while (leftX < worldBoxX) leftX += worldBoxW;
                    while (leftX >= worldBoxX + worldBoxW) leftX -= worldBoxW;

                    g2d.setColor(new Color(255, 255, 255, 40));
                    g2d.fillRect((int) Math.round(leftX), (int) Math.round(worldBoxY), (int) Math.round(viewW), (int) Math.round(worldBoxH));
                    g2d.setColor(new Color(255, 255, 255, 220));
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRect((int) Math.round(leftX), (int) Math.round(worldBoxY), (int) Math.round(viewW), (int) Math.round(worldBoxH));

                    if (leftX + viewW > worldBoxX + worldBoxW) {
                        double wrapW = (leftX + viewW) - (worldBoxX + worldBoxW);
                        g2d.setColor(new Color(255, 255, 255, 40));
                        g2d.fillRect((int) Math.round(worldBoxX), (int) Math.round(worldBoxY), (int) Math.round(wrapW), (int) Math.round(worldBoxH));
                        g2d.setColor(new Color(255, 255, 255, 220));
                        g2d.drawRect((int) Math.round(worldBoxX), (int) Math.round(worldBoxY), (int) Math.round(wrapW), (int) Math.round(worldBoxH));
                    }
                } else {
                    Hex active = world.getActiveHex();
                    if (active != null) {
                        int r = active.getR();
                        int col = active.getQ() + (r >> 1);
                        double hx = originX + col * Math.sqrt(3.0) * hexRadius + ((r & 1) != 0 ? (Math.sqrt(3.0) / 2.0) * hexRadius : 0.0);
                        double hy = originY + (1.5 * r) * hexRadius;
                        int markerSize = Math.max(6, (int) Math.round(hexRadius * 3.0));
                        g2d.setColor(new Color(255, 255, 255, 50));
                        g2d.fillRect((int) Math.round(hx - markerSize / 2.0), (int) Math.round(hy - markerSize / 2.0), markerSize, markerSize);
                        g2d.setColor(AssetStyles.FONT_COLOR_ERROR);
                        g2d.setStroke(new BasicStroke(1.5f));
                        g2d.drawRect((int) Math.round(hx - markerSize / 2.0), (int) Math.round(hy - markerSize / 2.0), markerSize, markerSize);
                    }
                }
            }
        } finally {
            g2d.dispose();
        }
    }

    private void bakeBaseImage(World world, int panelW, int panelH, double originX, double originY,
                               double hexRadius, double worldBoxX, double worldBoxY,
                               double worldBoxW, double worldBoxH) {
        List<Hex> hexes = world.getHexes();
        if (hexes == null || hexes.isEmpty()) {
            cachedBakedBaseImage = null;
            return;
        }

        List<Hex> snapshot;
        try {
            snapshot = new ArrayList<>(hexes);
        } catch (ConcurrentModificationException ignored) {
            return;
        }

        List<Hex> landHexes = new ArrayList<>();
        hexLookup.clear();
        for (Hex hex : snapshot) {
            if (hex == null || isOcean(hex)) {
                continue;
            }
            landHexes.add(hex);
            hexLookup.put(packKey(hex.getQ(), hex.getR()), hex);
        }
        if (landHexes.isEmpty() && world.getWorldWidth() <= 0) {
            cachedBakedBaseImage = null;
            return;
        }

        BufferedImage baked = new BufferedImage(panelW, panelH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bg2d = baked.createGraphics();
        try {
            bg2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (world.getWorldWidth() > 0) {
                bg2d.setColor(GameConstants.BIOME_DEEP_OCEAN.getMapColor());
                bg2d.fillRect((int) Math.round(worldBoxX), (int) Math.round(worldBoxY), (int) Math.round(worldBoxW), (int) Math.round(worldBoxH));
                bg2d.setColor(AssetStyles.BORDER_COLOR);
                bg2d.drawRect((int) Math.round(worldBoxX), (int) Math.round(worldBoxY), (int) Math.round(worldBoxW), (int) Math.round(worldBoxH));
            }

            if (!landHexes.isEmpty()) {
                HexGridMesh mesh = HexGridMesh.build(
                        landHexes,
                        originX,
                        originY,
                        hexRadius,
                        (q, r) -> hexLookup.get(packKey(q, r)));
                mesh.paintFills(bg2d, face -> getBiomeColor(face.hex.getBiome()));
                mesh.paintDividers(
                        bg2d,
                        true,
                        face -> getBiomeColor(face.hex.getBiome()),
                        AssetStyles.BORDER_COLOR);
                mesh.paintRivers(bg2d);
            }
        } finally {
            bg2d.dispose();
        }

        cachedBakedBaseImage = baked;
        cachedBakedW = panelW;
        cachedBakedH = panelH;
        cachedOriginX = originX;
        cachedOriginY = originY;
        cachedHexRadius = hexRadius;
        cachedWorldBoxX = worldBoxX;
        cachedWorldBoxY = worldBoxY;
        cachedWorldBoxW = worldBoxW;
        cachedWorldBoxH = worldBoxH;
    }

    private Color getBiomeColor(Biome biome) {
        if (biome == null) {
            return AssetStyles.BACKGROUND_COLOR;
        }
        Color cached = biomeColorCache.get(biome.getId());
        if (cached != null) {
            return cached;
        }
        Color mapColor = biome.getMapColor();
        if (mapColor == null) {
            return AssetStyles.BACKGROUND_COLOR;
        }
        biomeColorCache.put(biome.getId(), mapColor);
        return mapColor;
    }

    private static boolean isOcean(Hex hex) {
        return hex != null && (hex.getBiome() == GameConstants.BIOME_OCEAN
                || hex.getBiome() == GameConstants.BIOME_DEEP_OCEAN);
    }

    private static double axialX(int q, int r) {
        return Math.sqrt(3.0) * q + Math.sqrt(3.0) / 2.0 * r;
    }

    private static double axialY(int r) {
        return 1.5 * r;
    }

    private static long packKey(int q, int r) {
        return (((long) q) << 32) ^ (r & 0xffffffffL);
    }
}
