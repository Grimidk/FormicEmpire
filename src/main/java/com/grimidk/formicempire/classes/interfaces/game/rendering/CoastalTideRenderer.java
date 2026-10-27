package com.grimidk.formicempire.classes.interfaces.game.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.services.colony.ColonySpatialLayout;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class CoastalTideRenderer {

    private CoastalTideRenderer() {
    }

    public static final int TIDE_AMPLITUDE_PX = 50;

    private static Image loadRequired(String path) {
        URL url = CoastalTideRenderer.class.getResource(path);
        return url != null ? new ImageIcon(url).getImage() : null;
    }

    private static Image[] resolveFrame(String name) {
        Image img = loadRequired("/backgrounds/biomes/" + name + ".png");
        return img != null ? new Image[] { img } : new Image[0];
    }

    private static final Image[] SHORE_FRAMES = resolveFrame("CoastShore");
    private static final Image[] CLIFF_FRAMES = resolveFrame("CoastCliffs");
    private static final Image[] CORNER_FRAMES = resolveFrame("CoastShoreCorner");
    private static final Image[] CLIFF_CORNER_FRAMES = resolveFrame("CoastCliffsCorner");

    private static Image selectFrame(Image[] frames, long animTimeMs) {
        if (frames == null || frames.length == 0) {
            return null;
        }
        int index = (int) ((animTimeMs / 400L) % frames.length);
        return frames[Math.abs(index)];
    }

    public static double getTideLevel(int hour, int minute) {
        double totalHours = (hour % 24) + (Math.max(0, Math.min(59, minute)) / 60.0);
        double angle = (totalHours * Math.PI) / 6.0 - (Math.PI / 2.0);
        return (Math.sin(angle) + 1.0) / 2.0;
    }

    public static boolean isTideRising(int hour) {
        int h = (hour % 24 + 24) % 24;
        return (h >= 0 && h < 6) || (h >= 12 && h < 18);
    }

    public static boolean isCoastal(Hex hex) {
        if (hex == null) {
            return false;
        }
        if (hex.isCoastal()) {
            return true;
        }
        Biome b = hex.getBiome();
        return b != null && b.isCoastal();
    }

    public static boolean isCoastalBiome(int biomeId) {
        if (GameConstants.BIOME_BEACH != null && biomeId == GameConstants.BIOME_BEACH.getId()) {
            return true;
        }
        if (GameConstants.BIOME_CLIFFS != null && biomeId == GameConstants.BIOME_CLIFFS.getId()) {
            return true;
        }
        if (GameConstants.BIOME_MANGROVE != null && biomeId == GameConstants.BIOME_MANGROVE.getId()) {
            return true;
        }
        for (Biome b : GameConstants.getBiomes()) {
            if (b != null && b.getId() == biomeId && b.isCoastal()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWaterNeighbor(Hex neighbor) {
        if (neighbor == null) {
            return true;
        }
        if (neighbor.getContinentId() == 0) {
            return true;
        }
        Biome b = neighbor.getBiome();
        return b == GameConstants.BIOME_OCEAN || b == GameConstants.BIOME_LAKE;
    }

    public static final class WaterBorders {
        public final boolean north;
        public final boolean south;
        public final boolean east;
        public final boolean west;

        public WaterBorders(boolean north, boolean south, boolean east, boolean west) {
            this.north = north;
            this.south = south;
            this.east = east;
            this.west = west;
        }

        public boolean hasAny() {
            return north || south || east || west;
        }
    }

    public static WaterBorders resolveWaterBorders(Hex hex) {
        if (hex == null) {
            return new WaterBorders(false, true, false, false);
        }
        if (!isCoastal(hex)) {
            return new WaterBorders(false, false, false, false);
        }
        boolean hasAnyNeighbor = hex.getNorth() != null
                || hex.getSouth() != null
                || hex.getNorthEast() != null
                || hex.getSouthEast() != null
                || hex.getNorthWest() != null
                || hex.getSouthWest() != null;
        if (!hasAnyNeighbor) {
            return new WaterBorders(false, true, false, false);
        }
        boolean wN = isWaterNeighbor(hex.getNorth());
        boolean wS = isWaterNeighbor(hex.getSouth());
        boolean wNE = isWaterNeighbor(hex.getNorthEast());
        boolean wSE = isWaterNeighbor(hex.getSouthEast());
        boolean wNW = isWaterNeighbor(hex.getNorthWest());
        boolean wSW = isWaterNeighbor(hex.getSouthWest());

        boolean isIsland = hex.isIsland();
        int waterCount = (wN ? 1 : 0) + (wS ? 1 : 0) + (wNE ? 1 : 0)
                + (wSE ? 1 : 0) + (wNW ? 1 : 0) + (wSW ? 1 : 0);
        if (waterCount == 6) {
            isIsland = true;
        }

        if (isIsland) {
            return new WaterBorders(true, true, true, true);
        }

        if (waterCount == 5) {
            if (!wN) {
                return new WaterBorders(false, true, true, true);
            }
            if (!wS) {
                return new WaterBorders(true, false, true, true);
            }
            if (!wNE || !wSE) {
                return new WaterBorders(true, true, false, true);
            }
            if (!wNW || !wSW) {
                return new WaterBorders(true, true, true, false);
            }
        }

        boolean east = wNE || wSE;
        boolean west = wNW || wSW;

        boolean north = wN || (wNE && wNW);
        boolean south = wS || (wSE && wSW);

        if (!north && !south) {
            if (wSE ^ wSW) {
                if (!wN) {
                    south = true;
                }
            } else if (wNW ^ wNE) {
                if (!wS) {
                    north = true;
                }
            }
        } else if (!north && (wNW ^ wNE)) {
            if (!wS && !south) {
                north = true;
            }
        } else if (!south && (wSE ^ wSW)) {
            if (!wN && !north) {
                south = true;
            }
        }

        if (north && south && east && west) {
            if (!wN) {
                north = false;
            } else if (!wS) {
                south = false;
            } else if (!wNE || !wSE) {
                east = false;
            } else if (!wNW || !wSW) {
                west = false;
            }
        }

        if (!north && !south && !east && !west) {
            south = true;
        }
        return new WaterBorders(north, south, east, west);
    }

    public static int getWaterBordersKey(Hex hex, int biomeId) {
        if (!isCoastalBiome(biomeId) && !isCoastal(hex)) {
            return 0;
        }
        WaterBorders borders = resolveWaterBorders(hex);
        int key = 1;
        if (borders.north) key |= 2;
        if (borders.south) key |= 4;
        if (borders.east) key |= 8;
        if (borders.west) key |= 16;
        return key;
    }

    private static final class CoastalPalette {
        final Color deepWater;
        final Color shallowWater;
        final Color foam;
        final Color wetIntertidal;

        CoastalPalette(Color deepWater, Color shallowWater, Color foam, Color wetIntertidal) {
            this.deepWater = deepWater;
            this.shallowWater = shallowWater;
            this.foam = foam;
            this.wetIntertidal = wetIntertidal;
        }
    }

    private static CoastalPalette resolvePalette(Biome biome) {
        if (biome == GameConstants.BIOME_CLIFFS) {
            return new CoastalPalette(
                    new Color(18, 38, 62, 225),
                    new Color(30, 60, 95, 185),
                    new Color(230, 245, 255, 210),
                    new Color(30, 30, 35, 90));
        }
        if (biome == GameConstants.BIOME_MANGROVE) {
            return new CoastalPalette(
                    new Color(25, 60, 50, 220),
                    new Color(45, 95, 75, 175),
                    new Color(210, 235, 220, 190),
                    new Color(40, 25, 15, 80));
        }
        if (biome == GameConstants.BIOME_BEACH) {
            return new CoastalPalette(
                    new Color(24, 85, 135, 215),
                    new Color(55, 145, 175, 170),
                    new Color(245, 252, 255, 220),
                    new Color(80, 65, 30, 65));
        }
        return new CoastalPalette(
                new Color(28, 80, 125, 215),
                new Color(50, 125, 165, 170),
                new Color(240, 248, 255, 210),
                new Color(30, 30, 20, 60));
    }

    private static Image getOceanTile() {
        if (GameConstants.BIOME_OCEAN != null && GameConstants.BIOME_OCEAN.getTile() != null) {
            return GameConstants.BIOME_OCEAN.getTile().getImage();
        }
        return null;
    }

    private static void tileImageArea(Graphics2D g2d, Image img, int x, int y, int w, int h) {
        if (img == null || w <= 0 || h <= 0) {
            return;
        }
        int tw = img.getWidth(null);
        int th = img.getHeight(null);
        if (tw <= 0 || th <= 0) {
            return;
        }
        Shape oldClip = g2d.getClip();
        g2d.clipRect(x, y, w, h);
        try {
            int startX = x - ((x % tw + tw) % tw);
            int startY = y - ((y % th + th) % th);
            int endX = x + w;
            int endY = y + h;
            for (int px = startX; px < endX; px += tw) {
                for (int py = startY; py < endY; py += th) {
                    g2d.drawImage(img, px, py, null);
                }
            }
        } finally {
            g2d.setClip(oldClip);
        }
    }

    public static void bakeCoastalBackground(
            Graphics2D g2d,
            int biomeId,
            Hex hex,
            int contentPadX,
            int contentPadY,
            int panelW,
            int panelH) {
        bakeCoastalBackground(g2d, biomeId, hex, contentPadX, contentPadY, panelW, panelH, null);
    }

    public static void bakeCoastalBackground(
            Graphics2D g2d,
            int biomeId,
            Hex hex,
            int contentPadX,
            int contentPadY,
            int panelW,
            int panelH,
            Rectangle paintViewportRect) {
        if (!isCoastalBiome(biomeId) && !isCoastal(hex)) {
            return;
        }

        WaterBorders borders = resolveWaterBorders(hex);
        if (!borders.hasAny()) {
            borders = new WaterBorders(false, true, false, false);
        }

        Image shoreFrame;
        if (GameConstants.BIOME_CLIFFS != null && biomeId == GameConstants.BIOME_CLIFFS.getId()) {
            shoreFrame = (CLIFF_FRAMES.length > 0) ? CLIFF_FRAMES[0] : null;
        } else {
            shoreFrame = (SHORE_FRAMES.length > 0) ? SHORE_FRAMES[0] : null;
        }

        Image oceanTile = getOceanTile();
        int fw = (shoreFrame != null) ? Math.max(1, shoreFrame.getWidth(null)) : 64;
        int fh = (shoreFrame != null) ? Math.max(1, shoreFrame.getHeight(null)) : 64;

        int southShoreY = panelH - 3 * fh;
        int northShoreY = 2 * fh;
        int westShoreX = 2 * fw;
        int eastShoreX = panelW - 3 * fw;

        Image cornerFrame;
        if (GameConstants.BIOME_CLIFFS != null && biomeId == GameConstants.BIOME_CLIFFS.getId()) {
            cornerFrame = (CLIFF_CORNER_FRAMES.length > 0) ? CLIFF_CORNER_FRAMES[0] : null;
        } else {
            cornerFrame = (CORNER_FRAMES.length > 0) ? CORNER_FRAMES[0] : null;
        }
        if (cornerFrame == null) {
            cornerFrame = shoreFrame;
        }

        Object oldAntialias = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (borders.south) {
            int oceanTop = southShoreY + fh;
            if (oceanTop < panelH) {
                if (oceanTile != null) {
                    tileImageArea(g2d, oceanTile, 0, oceanTop, panelW, panelH - oceanTop);
                } else {
                    g2d.setColor(new Color(18, 38, 62));
                    g2d.fillRect(0, oceanTop, panelW, panelH - oceanTop);
                }
            }
        }

        if (borders.north) {
            int oceanBottom = northShoreY;
            if (oceanBottom > 0) {
                if (oceanTile != null) {
                    tileImageArea(g2d, oceanTile, 0, 0, panelW, oceanBottom);
                } else {
                    g2d.setColor(new Color(18, 38, 62));
                    g2d.fillRect(0, 0, panelW, oceanBottom);
                }
            }
        }

        if (borders.east) {
            int oceanLeft = eastShoreX + fw;
            if (oceanLeft < panelW) {
                if (oceanTile != null) {
                    tileImageArea(g2d, oceanTile, oceanLeft, 0, panelW - oceanLeft, panelH);
                } else {
                    g2d.setColor(new Color(18, 38, 62));
                    g2d.fillRect(oceanLeft, 0, panelW - oceanLeft, panelH);
                }
            }
        }

        if (borders.west) {
            int oceanRight = westShoreX;
            if (oceanRight > 0) {
                if (oceanTile != null) {
                    tileImageArea(g2d, oceanTile, 0, 0, oceanRight, panelH);
                } else {
                    g2d.setColor(new Color(18, 38, 62));
                    g2d.fillRect(0, 0, oceanRight, panelH);
                }
            }
        }

        if (borders.south) {
            int startX = borders.west ? westShoreX + fw : 0;
            int endX = borders.east ? eastShoreX : panelW;
            if (shoreFrame != null) {
                for (int x = startX; x < endX; x += fw) {
                    g2d.drawImage(shoreFrame, x, southShoreY, null);
                }
            }
        }

        if (borders.north) {
            int startX = borders.west ? westShoreX + fw : 0;
            int endX = borders.east ? eastShoreX : panelW;
            if (shoreFrame != null) {
                AffineTransform oldTx = g2d.getTransform();
                for (int x = startX; x < endX; x += fw) {
                    g2d.translate(x + fw, northShoreY + fh);
                    g2d.rotate(Math.PI);
                    g2d.drawImage(shoreFrame, 0, 0, null);
                    g2d.setTransform(oldTx);
                }
            }
        }

        if (borders.east) {
            int startY = borders.north ? northShoreY + fh : 0;
            int endY = borders.south ? southShoreY : panelH;
            if (shoreFrame != null) {
                AffineTransform oldTx = g2d.getTransform();
                for (int y = startY; y < endY; y += fh) {
                    g2d.translate(eastShoreX, y + fh);
                    g2d.rotate(-Math.PI / 2.0);
                    g2d.drawImage(shoreFrame, 0, 0, null);
                    g2d.setTransform(oldTx);
                }
            }
        }

        if (borders.west) {
            int startY = borders.north ? northShoreY + fh : 0;
            int endY = borders.south ? southShoreY : panelH;
            if (shoreFrame != null) {
                AffineTransform oldTx = g2d.getTransform();
                for (int y = startY; y < endY; y += fh) {
                    g2d.translate(westShoreX + fw, y);
                    g2d.rotate(Math.PI / 2.0);
                    g2d.drawImage(shoreFrame, 0, 0, null);
                    g2d.setTransform(oldTx);
                }
            }
        }

        if (cornerFrame != null) {
            if (borders.south && borders.east) {
                g2d.drawImage(cornerFrame, eastShoreX, southShoreY, null);
            }
            if (borders.south && borders.west) {
                AffineTransform oldTx = g2d.getTransform();
                g2d.translate(westShoreX + fw, southShoreY);
                g2d.rotate(Math.PI / 2.0);
                g2d.drawImage(cornerFrame, 0, 0, null);
                g2d.setTransform(oldTx);
            }
            if (borders.north && borders.west) {
                AffineTransform oldTx = g2d.getTransform();
                g2d.translate(westShoreX + fw, northShoreY + fh);
                g2d.rotate(Math.PI);
                g2d.drawImage(cornerFrame, 0, 0, null);
                g2d.setTransform(oldTx);
            }
            if (borders.north && borders.east) {
                AffineTransform oldTx = g2d.getTransform();
                g2d.translate(eastShoreX, northShoreY + fh);
                g2d.rotate(-Math.PI / 2.0);
                g2d.drawImage(cornerFrame, 0, 0, null);
                g2d.setTransform(oldTx);
            }
        }

        if (oldAntialias != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialias);
        }
    }

    public static void renderCoastalTide(
            Graphics2D g2d,
            World world,
            int contentPadX,
            int contentPadY,
            int panelW,
            int panelH,
            long animTimeMs) {
        renderCoastalTide(g2d, world, null, contentPadX, contentPadY, panelW, panelH, null, animTimeMs);
    }

    public static void renderCoastalTide(
            Graphics2D g2d,
            World world,
            int contentPadX,
            int contentPadY,
            int panelW,
            int panelH,
            Rectangle paintViewportRect,
            long animTimeMs) {
        renderCoastalTide(g2d, world, null, contentPadX, contentPadY, panelW, panelH, paintViewportRect, animTimeMs);
    }

    public static void renderCoastalTide(
            Graphics2D g2d,
            World world,
            Hex hexOverride,
            int contentPadX,
            int contentPadY,
            int panelW,
            int panelH,
            Rectangle paintViewportRect,
            long animTimeMs) {
        Hex hex = hexOverride;
        if (hex == null && world != null) {
            hex = world.getActiveHex();
            if (hex == null) {
                hex = world.getSpawnHex();
            }
        }
        int biomeId = (hex != null && hex.getBiome() != null) ? hex.getBiome().getId() : -1;
        bakeCoastalBackground(g2d, biomeId, hex, contentPadX, contentPadY, panelW, panelH, paintViewportRect);
    }
}
