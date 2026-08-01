package com.grimidk.formicempire.classes.infrasctructure.assets;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

public final class CritterSpriteCompositor {
    private static final String ROOT = "sprites/critters/";
    private static final int DEFAULT_LEG_FRAME = 1;

    private static final Map<String, ImageIcon> COMPOSITE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, BufferedImage> LAYER_CACHE = new ConcurrentHashMap<>();

    private CritterSpriteCompositor() {
    }

    public static ImageIcon getSprite(String folder, String bodyFile, boolean animated) {
        return getSprite(folder, bodyFile, animated, DEFAULT_LEG_FRAME);
    }

    public static ImageIcon getSprite(Species species) {
        return getSprite(species, DEFAULT_LEG_FRAME);
    }

    public static ImageIcon getSprite(Species species, int legFrame) {
        if (species == null || !species.hasComposedSprite()) {
            return species != null ? species.getSprite() : null;
        }
        return getSprite(
                species.getSpriteFolder(),
                species.getBodySpriteFile(),
                species.hasLegWalkCycle(),
                legFrame);
    }

    public static ImageIcon getSprite(String folder, String bodyFile, boolean animated, int legFrame) {
        if (folder == null || folder.isEmpty() || bodyFile == null || bodyFile.isEmpty()) {
            return null;
        }
        int safeLeg = animated
                ? clampFrame(legFrame, 1, GameNumbers.ANT_LEG_FRAME_COUNT)
                : DEFAULT_LEG_FRAME;
        String cacheKey = folder + "|L" + safeLeg;
        return COMPOSITE_CACHE.computeIfAbsent(cacheKey, k -> buildIcon(folder, bodyFile, animated, safeLeg));
    }

    public static boolean hasLegWalkCycle(Species species) {
        return species != null && species.hasLegWalkCycle();
    }

    private static ImageIcon buildIcon(String folder, String bodyFile, boolean animated, int legFrame) {
        BufferedImage composed = compose(folder, bodyFile, animated, legFrame);
        if (composed == null) {
            return null;
        }
        return new ImageIcon(composed);
    }

    private static BufferedImage compose(String folder, String bodyFile, boolean animated, int legFrame) {
        BufferedImage body = loadLayer(folder + "/" + bodyFile);
        BufferedImage legs = null;
        if (animated) {
            legs = loadLayer(folder + "/Leg" + legFrame + ".png");
        }
        if (body == null && legs == null) {
            return null;
        }
        int width = firstWidth(legs, body);
        int height = firstHeight(legs, body);
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
        } finally {
            g.dispose();
        }
        return out;
    }

    private static BufferedImage loadLayer(String relativePath) {
        return LAYER_CACHE.computeIfAbsent(relativePath, CritterSpriteCompositor::readLayer);
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
