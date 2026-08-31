package com.grimidk.formicempire.classes.infrasctructure.assets;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

public final class CritterSpriteCompositor {
    private static final String ROOT = "sprites/critters/";
    private static final int DEFAULT_LEG_FRAME = 1;
    private static final int DEFAULT_ANTENNA_FRAME = 1;

    private static final LruCache<String, ImageIcon> COMPOSITE_CACHE =
            new LruCache<>(GameNumbers.SPRITE_COMPOSITE_CACHE_MAX_ENTRIES);
    private static final LruCache<String, BufferedImage> LAYER_CACHE =
            new LruCache<>(GameNumbers.SPRITE_LAYER_CACHE_MAX_ENTRIES);

    private CritterSpriteCompositor() {
    }

    public static int compositeCacheSize() {
        return COMPOSITE_CACHE.size();
    }

    public static int layerCacheSize() {
        return LAYER_CACHE.size();
    }

    public static ImageIcon getSprite(String folder, String bodyFile, boolean animated) {
        return getSprite(folder, bodyFile, animated, DEFAULT_LEG_FRAME);
    }

    public static ImageIcon getSprite(Species species) {
        return getSprite(species, DEFAULT_LEG_FRAME);
    }

    public static ImageIcon getSprite(Species species, int legFrame) {
        return getSprite(species, legFrame, DEFAULT_ANTENNA_FRAME);
    }

    public static ImageIcon getSprite(Species species, int legFrame, int antennaFrame) {
        if (species == null || !species.hasComposedSprite()) {
            return species != null ? species.getSprite() : null;
        }
        return getSprite(
                species.getSpriteFolder(),
                species.getBodySpriteFile(),
                species.hasLegWalkCycle(),
                legFrame,
                species.getLegFrameCount(),
                antennaFrame,
                species.getAntennaFrameCount());
    }

    public static ImageIcon getSprite(String folder, String bodyFile, boolean animated, int legFrame) {
        return getSprite(folder, bodyFile, animated, legFrame,
                animated ? GameNumbers.ANT_LEG_FRAME_COUNT : 1,
                DEFAULT_ANTENNA_FRAME, 0);
    }

    public static ImageIcon getSprite(
            String folder,
            String bodyFile,
            boolean animated,
            int legFrame,
            int legFrameCount,
            int antennaFrame,
            int antennaFrameCount) {
        if (folder == null || folder.isEmpty() || bodyFile == null || bodyFile.isEmpty()) {
            return null;
        }
        int safeLeg = animated
                ? clampFrame(legFrame, 1, Math.max(1, legFrameCount))
                : DEFAULT_LEG_FRAME;
        int safeAntenna = antennaFrameCount > 1
                ? clampFrame(antennaFrame, 1, antennaFrameCount)
                : DEFAULT_ANTENNA_FRAME;
        String cacheKey = folder + "|L" + safeLeg + "|A" + safeAntenna;
        return COMPOSITE_CACHE.get(cacheKey,
                k -> buildIcon(folder, bodyFile, animated, safeLeg, safeAntenna, antennaFrameCount));
    }

    public static boolean hasLegWalkCycle(Species species) {
        return species != null && species.hasLegWalkCycle();
    }

    private static ImageIcon buildIcon(
            String folder,
            String bodyFile,
            boolean animated,
            int legFrame,
            int antennaFrame,
            int antennaFrameCount) {
        BufferedImage composed = compose(folder, bodyFile, animated, legFrame, antennaFrame, antennaFrameCount);
        if (composed == null) {
            return null;
        }
        return new ImageIcon(CompatibleImages.copyToCompatible(composed));
    }

    private static BufferedImage compose(
            String folder,
            String bodyFile,
            boolean animated,
            int legFrame,
            int antennaFrame,
            int antennaFrameCount) {
        BufferedImage body = loadLayer(folder + "/" + bodyFile);
        BufferedImage legs = null;
        if (animated) {
            legs = loadLayer(folder + "/Leg" + legFrame + ".png");
        }
        BufferedImage antenna = null;
        if (antennaFrameCount > 0) {
            antenna = loadLayer(folder + "/Antenna" + antennaFrame + ".png");
        }
        if (body == null && legs == null && antenna == null) {
            return null;
        }
        int width = firstWidth(legs, body, antenna);
        int height = firstHeight(legs, body, antenna);
        if (width <= 0 || height <= 0) {
            return null;
        }
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            if (legs != null) {
                g.drawImage(legs, 0, 0, null);
            }
            if (body != null) {
                g.drawImage(body, 0, 0, null);
            }
            if (antenna != null) {
                g.drawImage(antenna, 0, 0, null);
            }
        } finally {
            g.dispose();
        }
        return out;
    }

    private static BufferedImage loadLayer(String relativePath) {
        return LAYER_CACHE.get(relativePath, CritterSpriteCompositor::readLayer);
    }

    private static BufferedImage readLayer(String relativePath) {
        String path = ROOT + relativePath;
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            return null;
        }
        try {
            BufferedImage img = ImageIO.read(url);
            if (img == null) {
                return null;
            }
            BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = copy.createGraphics();
            try {
                g.drawImage(img, 0, 0, null);
            } finally {
                g.dispose();
            }
            return copy;
        } catch (IOException e) {
            System.err.println("Failed to load critter layer: " + path);
            return null;
        }
    }

    private static int clampFrame(int frame, int min, int max) {
        if (frame < min) {
            return min;
        }
        if (frame > max) {
            return max;
        }
        return frame;
    }

    private static int firstWidth(BufferedImage... images) {
        for (BufferedImage image : images) {
            if (image != null) {
                return image.getWidth();
            }
        }
        return 0;
    }

    private static int firstHeight(BufferedImage... images) {
        for (BufferedImage image : images) {
            if (image != null) {
                return image.getHeight();
            }
        }
        return 0;
    }
}
