package com.grimidk.formicempire.classes.infrasctructure.assets;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpeciesPalette;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

public final class AntSpriteCompositor {
    public static final int MARKER_HEAD = 0xFF0000;
    public static final int MARKER_TORSO = 0x00FF00;
    public static final int MARKER_ABDOMEN = 0x0000FF;
    public static final int MARKER_WING_PRIMARY = 0xFFFF00;
    public static final int MARKER_WING_SECONDARY = 0x00FFFF;
    public static final int MARKER_DRONE = 0xFF00FF;
    public static final int MARKER_DRONE_WING_PRIMARY = 0xFF8000;
    public static final int MARKER_DRONE_WING_SECONDARY = 0x8000FF;
    public static final int MARKER_HONEYPOT = 0xFF0080;
    public static final int UNIVERSAL_BLACK = 0x000000;

    private static final String SHARED_ROOT = "sprites/ants/shared/";
    private static final String PARASITIC_MITE_PATH = "sprites/critters/arachnid/parasiticMite/ParasiticMite.png";
    private static final int[][] PARASITIC_MITE_OFFSETS = {{-4, -5}, {4, -4}, {-5, 2}, {5, 3}, {0, 5}};
    private static final int DEFAULT_LEG_FRAME = 1;
    private static final int DEFAULT_JAW_FRAME = 1;
    private static final int DEFAULT_WING_FRAME = 1;
    private static final int DEFAULT_ANTENNA_FRAME = 1;

    private static final LruCache<String, ImageIcon> COMPOSITE_CACHE =
            new LruCache<>(GameNumbers.SPRITE_COMPOSITE_CACHE_MAX_ENTRIES);
    private static final LruCache<String, BufferedImage> LAYER_CACHE =
            new LruCache<>(GameNumbers.SPRITE_LAYER_CACHE_MAX_ENTRIES);

    private AntSpriteCompositor() {
    }

    public static int compositeCacheSize() {
        return COMPOSITE_CACHE.size();
    }

    public static int layerCacheSize() {
        return LAYER_CACHE.size();
    }

    public static ImageIcon getSprite(AntType type, AntSpecies species, AntSubtypeProfile profile) {
        return getSprite(type, species, profile, DEFAULT_LEG_FRAME, DEFAULT_JAW_FRAME, DEFAULT_WING_FRAME,
                DEFAULT_ANTENNA_FRAME, false);
    }

    public static ImageIcon getSprite(
            AntType type,
            AntSpecies species,
            AntSubtypeProfile profile,
            int legFrame,
            int jawFrame,
            int wingFrame) {
        return getSprite(type, species, profile, legFrame, jawFrame, wingFrame, DEFAULT_ANTENNA_FRAME, false);
    }

    public static ImageIcon getSprite(
            AntType type,
            AntSpecies species,
            AntSubtypeProfile profile,
            int legFrame,
            int jawFrame,
            int wingFrame,
            int antennaFrame,
            boolean parasiticMites) {
        if (type == null || species == null || species.getPalette() == null) {
            return null;
        }
        if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_LARVA || type == GameConstants.TYPE_PUPA
                || type == GameConstants.TYPE_DEAD || type == GameConstants.TYPE_ZOMBIE) {
            return null;
        }

        AntSubtypeProfile safeProfile = profile != null ? profile : AntSubtypeProfile.standard();
        boolean flyingLegs = legFrame == GameNumbers.ANT_LEG_FRAME_FLYING
                && (type == GameConstants.TYPE_DRONE || type == GameConstants.TYPE_PRINCESS);
        int safeLeg = flyingLegs ? GameNumbers.ANT_LEG_FRAME_FLYING : clampFrame(legFrame, 1, 4);
        int safeJaw = clampFrame(jawFrame, 1, 2);
        int safeWing = clampFrame(wingFrame, 1, 2);
        int safeAntenna = clampFrame(antennaFrame, 1, 2);
        String cacheKey = species.getId() + "|" + type.getId() + "|" + safeProfile.getCode()
                + "|L" + safeLeg + "|J" + safeJaw + "|W" + safeWing + "|A" + safeAntenna
                + "|M" + (parasiticMites ? 1 : 0);
        return COMPOSITE_CACHE.get(cacheKey,
                k -> buildIcon(type, species, safeProfile, safeLeg, safeJaw, safeWing, safeAntenna, parasiticMites));
    }

    public static boolean canCompose(AntType type) {
        return type != null
                && type != GameConstants.TYPE_EGG
                && type != GameConstants.TYPE_LARVA
                && type != GameConstants.TYPE_PUPA
                && type != GameConstants.TYPE_DEAD
                && type != GameConstants.TYPE_ZOMBIE;
    }

    private static ImageIcon buildIcon(
            AntType type,
            AntSpecies species,
            AntSubtypeProfile profile,
            int legFrame,
            int jawFrame,
            int wingFrame,
            int antennaFrame,
            boolean parasiticMites) {
        BufferedImage composed = compose(type, species, profile, legFrame, jawFrame, wingFrame, antennaFrame,
                parasiticMites);
        if (composed == null) {
            return null;
        }
        return new ImageIcon(CompatibleImages.copyToCompatible(composed));
    }

    private static BufferedImage compose(
            AntType type,
            AntSpecies species,
            AntSubtypeProfile profile,
            int legFrame,
            int jawFrame,
            int wingFrame,
            int antennaFrame,
            boolean parasiticMites) {
        String typeName = typeFolderName(type);
        if (typeName == null) {
            return null;
        }

        boolean drone = type == GameConstants.TYPE_DRONE;
        String headVariant = headVariantName(profile, drone);
        String abdomenVariant = abdomenVariantName(profile, drone);
        String jawVariant = headVariant;
        String antennaVariant = headVariant;

        BufferedImage legs;
        if (legFrame == GameNumbers.ANT_LEG_FRAME_FLYING) {
            legs = loadLayer("legs/" + typeName + "/LegFlying.png");
            if (legs == null) {
                legs = loadLayer("legs/" + typeName + "/Leg1.png");
            }
        } else {
            legs = loadLayer("legs/" + typeName + "/Leg" + legFrame + ".png");
        }
        BufferedImage abdomen = loadLayer("abdomen/" + typeName + "/Abdomen" + abdomenVariant + ".png");
        BufferedImage torso = loadLayer("torso/" + typeName + "/TorsoNone.png");
        BufferedImage head = loadLayer("head/" + typeName + "/Head" + headVariant + ".png");
        BufferedImage jaws = loadLayer("jaws/" + typeName + "/Jaw" + jawVariant + jawFrame + ".png");
        BufferedImage antennae = loadLayer(
                "antennae/" + typeName + "/Antenna" + antennaVariant + antennaFrame + ".png");

        BufferedImage wings = null;
        if (type == GameConstants.TYPE_DRONE || type == GameConstants.TYPE_PRINCESS) {
            wings = loadLayer("wings/" + typeName + "/Wing" + wingFrame + ".png");
        }

        if (legs == null && abdomen == null && torso == null && head == null) {
            return null;
        }

        int width = firstWidth(legs, abdomen, torso, head, jaws, antennae, wings);
        int height = firstHeight(legs, abdomen, torso, head, jaws, antennae, wings);
        if (width <= 0 || height <= 0) {
            return null;
        }

        AntSpeciesPalette palette = species.getPalette();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            drawRecolored(g, legs, palette, drone);
            drawRecolored(g, abdomen, palette, drone);
            drawRecolored(g, torso, palette, drone);
            drawRecolored(g, head, palette, drone);
            drawRecolored(g, jaws, palette, drone);
            drawRecolored(g, antennae, palette, drone);
            drawRecolored(g, wings, palette, drone);
            if (parasiticMites) {
                drawParasiticMites(g, width, height);
            }
        } finally {
            g.dispose();
        }
        return out;
    }

    private static void drawParasiticMites(Graphics2D g, int antWidth, int antHeight) {
        BufferedImage mite = loadAbsoluteLayer(PARASITIC_MITE_PATH);
        if (mite == null) {
            return;
        }
        int miteW = mite.getWidth();
        int miteH = mite.getHeight();
        int halfW = miteW / 2;
        int halfH = miteH / 2;
        int centerX = antWidth / 2;
        int centerY = antHeight / 2;
        int count = Math.min(GameNumbers.PARASITIC_MITES_ON_ANT_SPRITE, PARASITIC_MITE_OFFSETS.length);
        for (int i = 0; i < count; i++) {
            int x = centerX + PARASITIC_MITE_OFFSETS[i][0] - halfW;
            int y = centerY + PARASITIC_MITE_OFFSETS[i][1] - halfH;
            g.drawImage(mite, x, y, null);
        }
    }

    private static void drawRecolored(Graphics2D g, BufferedImage src, AntSpeciesPalette palette, boolean drone) {
        if (src == null) {
            return;
        }
        g.drawImage(recolor(src, palette, drone), 0, 0, null);
    }

    static BufferedImage recolor(BufferedImage src, AntSpeciesPalette palette, boolean drone) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int rgb = argb & 0xFFFFFF;
                if (rgb == UNIVERSAL_BLACK) {
                    out.setRGB(x, y, argb);
                    continue;
                }
                int replacement = mapMarker(rgb, palette, drone);
                if (replacement < 0) {
                    out.setRGB(x, y, argb);
                } else {
                    out.setRGB(x, y, (a << 24) | (replacement & 0xFFFFFF));
                }
            }
        }
        return out;
    }

    static int mapMarker(int rgb, AntSpeciesPalette palette, boolean drone) {
        return switch (rgb) {
            case MARKER_HEAD -> drone ? palette.getDroneColor() : palette.getHeadColor();
            case MARKER_TORSO -> drone ? palette.getDroneColor() : palette.getTorsoColor();
            case MARKER_ABDOMEN -> drone ? palette.getDroneColor() : palette.getAbdomenColor();
            case MARKER_DRONE -> palette.getDroneColor();
            case MARKER_WING_PRIMARY -> palette.getWingPrimaryColor();
            case MARKER_WING_SECONDARY -> palette.getWingSecondaryColor();
            case MARKER_DRONE_WING_PRIMARY -> palette.getDroneWingPrimaryColor();
            case MARKER_DRONE_WING_SECONDARY -> palette.getDroneWingSecondaryColor();
            case MARKER_HONEYPOT -> palette.getHoneypotColor();
            default -> -1;
        };
    }

    private static BufferedImage loadLayer(String relativePath) {
        return LAYER_CACHE.get(SHARED_ROOT + relativePath, AntSpriteCompositor::readLayer);
    }

    private static BufferedImage loadAbsoluteLayer(String classpathPath) {
        return LAYER_CACHE.get(classpathPath, AntSpriteCompositor::readLayer);
    }

    private static BufferedImage readLayer(String path) {
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
            System.err.println("Failed to load ant layer: " + path);
            return null;
        }
    }

    private static String typeFolderName(AntType type) {
        if (type == GameConstants.TYPE_WORKER) {
            return "Worker";
        }
        if (type == GameConstants.TYPE_SOLDIER) {
            return "Soldier";
        }
        if (type == GameConstants.TYPE_MAJOR) {
            return "Major";
        }
        if (type == GameConstants.TYPE_DRONE) {
            return "Drone";
        }
        if (type == GameConstants.TYPE_PRINCESS) {
            return "Princess";
        }
        if (type == GameConstants.TYPE_QUEEN) {
            return "Queen";
        }
        return null;
    }

    private static String headVariantName(AntSubtypeProfile profile, boolean drone) {
        if (drone) {
            return "None";
        }
        AntSubtype subtype = profile.getSubtype(AntSubtypeSlot.HEAD);
        return subtypeVariantPascal(subtype, "None");
    }

    private static String abdomenVariantName(AntSubtypeProfile profile, boolean drone) {
        if (drone) {
            return "None";
        }
        AntSubtype subtype = profile.getSubtype(AntSubtypeSlot.ABDOMEN);
        return subtypeVariantPascal(subtype, "None");
    }

    private static String subtypeVariantPascal(AntSubtype subtype, String fallback) {
        if (subtype == null || subtype.isNone() || !subtype.hasSprite()) {
            return fallback;
        }
        String folder = subtype.getSpriteFolder();
        if (folder == null || folder.isEmpty()) {
            return fallback;
        }
        return Character.toUpperCase(folder.charAt(0)) + folder.substring(1);
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
