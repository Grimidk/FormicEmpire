package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.rendering.HexGridMesh;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    public MiniMapPanel(Engine engine, Runnable openMapAction) {
        super(null);
        this.engine = engine;
        this.openMapAction = openMapAction;
        initComponents();
        initLayout();
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
                if (engine == null || engine.getWorld() == null || openMapAction == null) {
                    return;
                }
                openMapAction.run();
            }
        });
    }

    @Override
    protected void initLayout() {
    }

    @Override
    public void refreshTheme() {
        biomeColorCache.clear();
        super.refreshTheme();
        repaint();
    }

    public void refreshMap() {
        repaint();
    }

    public void reset() {
        biomeColorCache.clear();
        hexLookup.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        World world = engine != null ? engine.getWorld() : null;
        if (world == null) {
            return;
        }
        List<Hex> hexes = world.getHexes();
        if (hexes == null || hexes.isEmpty()) {
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
        if (landHexes.isEmpty()) {
            return;
        }

        Insets insets = getInsets();
        int availW = getWidth() - insets.left - insets.right - 4;
        int availH = getHeight() - insets.top - insets.bottom - 4;
        if (availW <= 0 || availH <= 0) {
            return;
        }

        double minCx = Double.POSITIVE_INFINITY;
        double maxCx = Double.NEGATIVE_INFINITY;
        double minCy = Double.POSITIVE_INFINITY;
        double maxCy = Double.NEGATIVE_INFINITY;
        for (Hex hex : landHexes) {
            double cx = axialX(hex.getQ(), hex.getR());
            double cy = axialY(hex.getR());
            if (cx < minCx) {
                minCx = cx;
            }
            if (cx > maxCx) {
                maxCx = cx;
            }
            if (cy < minCy) {
                minCy = cy;
            }
            if (cy > maxCy) {
                maxCy = cy;
            }
        }

        double spanX = Math.max(0.001, maxCx - minCx);
        double spanY = Math.max(0.001, maxCy - minCy);
        double hexRadius = Math.min(availW / (spanX + 2.0), availH / (spanY + 2.0));
        if (hexRadius < 1.0) {
            hexRadius = 1.0;
        }

        double midCx = (minCx + maxCx) * 0.5;
        double midCy = (minCy + maxCy) * 0.5;
        double originX = insets.left + 2 + availW / 2.0 - midCx * hexRadius;
        double originY = insets.top + 2 + availH / 2.0 - midCy * hexRadius;

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            HexGridMesh mesh = HexGridMesh.build(
                    landHexes,
                    originX,
                    originY,
                    hexRadius,
                    (q, r) -> hexLookup.get(packKey(q, r)));
            mesh.paintFills(g2d, face -> getBiomeColor(face.hex.getBiome()));
            mesh.paintDividers(
                    g2d,
                    true,
                    face -> getBiomeColor(face.hex.getBiome()),
                    AssetStyles.BORDER_COLOR);
        } finally {
            g2d.dispose();
        }
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
        return hex.getBiome() == GameConstants.BIOME_OCEAN;
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
