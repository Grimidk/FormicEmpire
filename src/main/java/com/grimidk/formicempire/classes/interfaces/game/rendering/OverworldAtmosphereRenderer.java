package com.grimidk.formicempire.classes.interfaces.game.rendering;

import com.grimidk.formicempire.classes.constants.world.DisasterType;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.services.colony.ColonySpatialLayout;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.assets.GameSpritePreloader;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.ImageIcon;

public final class OverworldAtmosphereRenderer {

    private static final int PARTICLE_COUNT = 90;
    private static final int HEAVY_PARTICLE_COUNT = 160;
    private static final int SNOW_COUNT = 75;
    private static final int SAND_COUNT = 85;

    private static final int[][] EARTHQUAKE_OFFSETS = {
        { 50, 95 },
        { 365, 115 },
        { 75, 365 },
        { 350, 345 }
    };

    private static final int[][] VOLCANO_OFFSETS = {
        { 90, 145 },
        { 330, 235 }
    };

    private static final int[][] WILDFIRE_OFFSETS = {
        { 70, 85 },
        { 400, 75 },
        { 130, 265 },
        { 360, 285 },
        { 230, 390 },
        { 430, 200 }
    };

    private static final int[][] ICE_COLLAPSE_OFFSETS = {
        { 45, 75 },
        { 370, 65 },
        { 60, 315 },
        { 360, 320 }
    };

    private static final int[][] OCEAN_FREEZE_OFFSETS = {
        { 55, 85 },
        { 365, 80 },
        { 70, 305 },
        { 355, 315 }
    };

    private static Image overworldCloudSprite;
    private static Image helpPlaceholderSprite;
    private static final Map<String, Image> disasterSprites = new ConcurrentHashMap<>();
    private static final Map<Image, Boolean> visiblePixelsCache = new ConcurrentHashMap<>();
    private static long accumulatedAnimTimeMs = 0L;
    private static long lastRealTimeMs = 0L;

    private OverworldAtmosphereRenderer() {
    }

    public static Image getHelpPlaceholderSprite() {
        if (helpPlaceholderSprite == null) {
            try {
                URL url = OverworldAtmosphereRenderer.class.getResource("/icons/social/Help.png");
                if (url != null) {
                    helpPlaceholderSprite = new ImageIcon(url).getImage();
                    GameSpritePreloader.ensureLoaded(helpPlaceholderSprite);
                }
            } catch (Throwable ignore) {
            }
        }
        return helpPlaceholderSprite;
    }

    private static Image getOverworldCloudSprite() {
        if (overworldCloudSprite == null) {
            try {
                URL url = OverworldAtmosphereRenderer.class.getResource("/sprites/environment/OverworldCloud.png");
                if (url != null) {
                    overworldCloudSprite = new ImageIcon(url).getImage();
                }
            } catch (Throwable ignore) {
            }
        }
        return overworldCloudSprite;
    }

    private static synchronized long advanceTime(boolean isPaused) {
        long now = System.currentTimeMillis();
        if (lastRealTimeMs == 0L) {
            lastRealTimeMs = now;
        }
        long delta = now - lastRealTimeMs;
        lastRealTimeMs = now;
        if (!isPaused && delta > 0 && delta < 500) {
            accumulatedAnimTimeMs += delta;
        }
        return accumulatedAnimTimeMs;
    }

    public static void render(Graphics2D g2d, int viewWidth, int viewHeight, World world, Hex activeHex, boolean daylightEnabled, boolean weatherEnabled) {
        render(g2d, viewWidth, viewHeight, world, activeHex, daylightEnabled, weatherEnabled, false);
    }

    public static void render(Graphics2D g2d, int viewWidth, int viewHeight, World world, Hex activeHex, boolean daylightEnabled, boolean weatherEnabled, boolean isPaused) {
        int defaultAnchorX = Math.max(0, (viewWidth - ColonySpatialLayout.ANCHOR_WIDTH) / 2);
        int defaultAnchorY = Math.max(0, (viewHeight - ColonySpatialLayout.ANCHOR_HEIGHT) / 2);
        render(g2d, viewWidth, viewHeight, defaultAnchorX, defaultAnchorY, world, activeHex, daylightEnabled, weatherEnabled, isPaused);
    }

    public static void render(
            Graphics2D g2d,
            int viewWidth,
            int viewHeight,
            int anchorOffsetX,
            int anchorOffsetY,
            World world,
            Hex activeHex,
            boolean daylightEnabled,
            boolean weatherEnabled,
            boolean isPaused) {
        if (g2d == null || viewWidth <= 0 || viewHeight <= 0 || world == null) {
            return;
        }

        Weather weather = activeHex != null && activeHex.getLocalWeather() != null
                ? activeHex.getLocalWeather()
                : world.getWeather();

        long timeMs = advanceTime(isPaused);

        if (daylightEnabled || weatherEnabled) {
            renderCloudShadows(g2d, viewWidth, viewHeight, timeMs, weather);
        }

        if (weatherEnabled && weather != null) {
            renderPrecipitation(g2d, viewWidth, viewHeight, timeMs, weather);
        }

        if (activeHex != null && activeHex.getDisasterType() != null
                && activeHex.getDisasterAgeHours() < GameNumbers.DISASTER_GAME_AREA_EFFECT_HOURS) {
            renderDisasterAtmosphere(g2d, viewWidth, viewHeight, anchorOffsetX, anchorOffsetY, timeMs, activeHex.getDisasterType());
        }
    }

    public static boolean hasVisiblePixels(Image image) {
        if (image == null) {
            return false;
        }
        Boolean cached = visiblePixelsCache.get(image);
        if (cached != null) {
            return cached;
        }
        GameSpritePreloader.ensureLoaded(image);
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);
        if (iw <= 0 || ih <= 0) {
            return false;
        }
        BufferedImage buf = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buf.createGraphics();
        try {
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        boolean visible = false;
        for (int y = 0; y < ih && !visible; y++) {
            for (int x = 0; x < iw; x++) {
                if (((buf.getRGB(x, y) >>> 24) & 0xFF) != 0) {
                    visible = true;
                    break;
                }
            }
        }
        visiblePixelsCache.put(image, visible);
        return visible;
    }

    static Image loadDisasterSprite(String path) {
        if (path == null) {
            return null;
        }
        Image cached = disasterSprites.get(path);
        if (cached != null) {
            return cached;
        }
        URL url = OverworldAtmosphereRenderer.class.getResource(path);
        if (url == null) {
            return null;
        }
        Image img = new ImageIcon(url).getImage();
        if (img == null) {
            return null;
        }
        GameSpritePreloader.ensureLoaded(img);
        disasterSprites.put(path, img);
        return img;
    }

    private static List<Image> collectVisibleSprites(List<String> paths, int fromIndex, int toIndex) {
        if (paths == null || paths.isEmpty()) {
            Image placeholder = getHelpPlaceholderSprite();
            return placeholder != null ? List.of(placeholder) : List.of();
        }
        int end = Math.min(paths.size(), toIndex);
        List<Image> result = new ArrayList<>();
        for (int i = Math.max(0, fromIndex); i < end; i++) {
            Image img = loadDisasterSprite(paths.get(i));
            if (img != null && hasVisiblePixels(img)) {
                result.add(img);
            }
        }
        if (result.isEmpty()) {
            Image placeholder = getHelpPlaceholderSprite();
            if (placeholder != null) {
                result.add(placeholder);
            }
        }
        return result;
    }

    private static void drawUnscaledSprite(Graphics2D g, Image img, int x, int y) {
        if (img == null) {
            return;
        }
        g.drawImage(img, x, y, null);
    }

    private static void renderDisasterAtmosphere(
            Graphics2D g2d,
            int w,
            int h,
            int anchorX,
            int anchorY,
            long timeMs,
            DisasterType disaster) {
        Graphics2D g = (Graphics2D) g2d.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            List<String> paths = disaster.getSpritePaths();
            int playW = ColonySpatialLayout.ANCHOR_WIDTH;
            int playH = ColonySpatialLayout.ANCHOR_HEIGHT;

            if (disaster == GameConstants.DISASTER_EARTHQUAKE) {
                int shake = (int) Math.round(Math.sin(timeMs * 0.04) * 3.0);
                List<Image> cracks = collectVisibleSprites(paths, 0, paths.size());
                if (!cracks.isEmpty()) {
                    for (int i = 0; i < EARTHQUAKE_OFFSETS.length; i++) {
                        Image spr = cracks.get(i % cracks.size());
                        drawUnscaledSprite(g, spr, anchorX + EARTHQUAKE_OFFSETS[i][0] + shake, anchorY + EARTHQUAKE_OFFSETS[i][1]);
                    }
                } else {
                    g.setColor(new Color(110, 85, 55, 110));
                    g.setStroke(new BasicStroke(2.2f));
                    double proceduralShake = Math.sin(timeMs * 0.03) * 6.0;
                    for (int i = 0; i < 12; i++) {
                        double sx = ((i * 173) % w) + proceduralShake;
                        double sy = ((i * 119) % h);
                        g.draw(new Line2D.Double(sx, sy, sx + 35, sy + 12));
                        g.draw(new Line2D.Double(sx + 35, sy + 12, sx + 60, sy - 6));
                    }
                }
            } else if (disaster == GameConstants.DISASTER_TSUNAMI) {
                g.setColor(new Color(35, 145, 225, 65));
                g.fillRect(0, 0, w, h);
                List<Image> waves = collectVisibleSprites(paths, 0, paths.size());
                if (!waves.isEmpty()) {
                    Image wave = waves.get((int) ((timeMs / 250L) % waves.size()));
                    int waveW = Math.max(1, wave.getWidth(null));
                    int waveH = Math.max(1, wave.getHeight(null));
                    int sweepSpan = playW + waveW * 2;
                    int waveX = anchorX - waveW + (int) Math.round((timeMs * 0.14) % sweepSpan);
                    for (int wy = anchorY - waveH / 2; wy < anchorY + playH + waveH / 2; wy += waveH) {
                        drawUnscaledSprite(g, wave, waveX, wy);
                    }
                } else {
                    g.setColor(new Color(35, 145, 225, 90));
                    double waveX = (timeMs * 0.12) % (w + 260) - 260;
                    for (int i = 0; i < 4; i++) {
                        g.fill(new Ellipse2D.Double(waveX + i * 90, h * 0.55 - i * 40, 260, 110));
                    }
                }
            } else if (disaster == GameConstants.DISASTER_TORNADO) {
                List<Image> frames = collectVisibleSprites(paths, 0, paths.size());
                double normX = 0.5 + Math.sin(timeMs * 0.0014) * 0.36;
                double normY = 0.5 + Math.sin(timeMs * 0.0023) * 0.28;
                if (!frames.isEmpty()) {
                    Image frame = frames.get((int) ((timeMs / 110L) % frames.size()));
                    int fw = Math.max(1, frame.getWidth(null));
                    int fh = Math.max(1, frame.getHeight(null));
                    int tx = anchorX + (int) Math.round(normX * Math.max(0, playW - fw));
                    int ty = anchorY + (int) Math.round(normY * Math.max(0, playH - fh));
                    drawUnscaledSprite(g, frame, tx, ty);
                } else {
                    g.setColor(new Color(145, 150, 160, 120));
                    double cx = anchorX + normX * playW;
                    double cy = anchorY + normY * playH;
                    for (int i = 0; i < 6; i++) {
                        double radius = 18 + i * 16;
                        double offset = Math.sin((timeMs * 0.01) + i) * 12;
                        g.fill(new Ellipse2D.Double(cx - radius + offset, cy - i * 18, radius * 2, radius * 0.7));
                    }
                }
            } else if (disaster == GameConstants.DISASTER_METEOR) {
                g.setColor(new Color(210, 75, 30, 60));
                g.fillRect(0, 0, w, h);
                Image crater = paths.isEmpty() ? null : loadDisasterSprite(paths.get(0));
                List<Image> smokeFrames = collectVisibleSprites(paths, 1, paths.size());
                int craterX = anchorX + 350;
                int craterY = anchorY + 130;
                if (crater != null && hasVisiblePixels(crater)) {
                    drawUnscaledSprite(g, crater, craterX, craterY);
                }
                if (!smokeFrames.isEmpty()) {
                    Image smoke = smokeFrames.get((int) ((timeMs / 240L) % smokeFrames.size()));
                    drawUnscaledSprite(g, smoke, craterX, craterY);
                }
                g.setColor(new Color(255, 165, 50, 170));
                for (int i = 0; i < 45; i++) {
                    double ex = (i * 97 + timeMs * 0.02) % w;
                    double ey = (i * 61 - timeMs * 0.03 + h * 10) % h;
                    g.fill(new Ellipse2D.Double(ex, ey, 3.5, 3.5));
                }
            } else if (disaster == GameConstants.DISASTER_VOLCANO) {
                g.setColor(new Color(240, 95, 25, 65));
                g.fillRect(0, 0, w, h);
                Image fissure = paths.isEmpty() ? null : loadDisasterSprite(paths.get(0));
                boolean showFissure = fissure != null && hasVisiblePixels(fissure);
                List<Image> eruptionFrames = collectVisibleSprites(paths, 1, paths.size());
                for (int i = 0; i < VOLCANO_OFFSETS.length; i++) {
                    int vx = anchorX + VOLCANO_OFFSETS[i][0];
                    int vy = anchorY + VOLCANO_OFFSETS[i][1];
                    if (showFissure) {
                        drawUnscaledSprite(g, fissure, vx, vy);
                    }
                    if (!eruptionFrames.isEmpty()) {
                        Image plume = eruptionFrames.get((int) (((timeMs / 220L) + i) % eruptionFrames.size()));
                        drawUnscaledSprite(g, plume, vx, vy);
                    }
                }
                g.setColor(new Color(255, 175, 45, 190));
                for (int i = 0; i < 65; i++) {
                    double ex = (i * 83 + Math.sin(timeMs * 0.004 + i) * 20) % w;
                    double ey = (h - ((i * 59 + timeMs * 0.06) % h));
                    g.fill(new Ellipse2D.Double(ex, ey, 4, 4));
                }
            } else if (disaster == GameConstants.DISASTER_WILDFIRE) {
                g.setColor(new Color(240, 95, 25, 65));
                g.fillRect(0, 0, w, h);
                List<Image> flameFrames = collectVisibleSprites(paths, 0, paths.size());
                if (!flameFrames.isEmpty()) {
                    for (int i = 0; i < WILDFIRE_OFFSETS.length; i++) {
                        Image flame = flameFrames.get((int) (((timeMs / 130L) + i) % flameFrames.size()));
                        drawUnscaledSprite(g, flame, anchorX + WILDFIRE_OFFSETS[i][0], anchorY + WILDFIRE_OFFSETS[i][1]);
                    }
                }
                g.setColor(new Color(255, 175, 45, 190));
                for (int i = 0; i < 65; i++) {
                    double ex = (i * 83 + Math.sin(timeMs * 0.004 + i) * 20) % w;
                    double ey = (h - ((i * 59 + timeMs * 0.06) % h));
                    g.fill(new Ellipse2D.Double(ex, ey, 4, 4));
                }
            } else if (disaster == GameConstants.DISASTER_ICE_COLLAPSE) {
                g.setColor(new Color(175, 225, 255, 75));
                g.fillRect(0, 0, w, h);
                List<Image> decals = collectVisibleSprites(paths, 0, paths.size());
                if (!decals.isEmpty()) {
                    for (int i = 0; i < ICE_COLLAPSE_OFFSETS.length; i++) {
                        Image spr = decals.get(i % decals.size());
                        drawUnscaledSprite(g, spr, anchorX + ICE_COLLAPSE_OFFSETS[i][0], anchorY + ICE_COLLAPSE_OFFSETS[i][1]);
                    }
                }
            } else {
                g.setColor(new Color(195, 235, 255, 80));
                g.fillRect(0, 0, w, h);
                List<Image> decals = collectVisibleSprites(paths, 0, paths.size());
                if (!decals.isEmpty()) {
                    for (int i = 0; i < OCEAN_FREEZE_OFFSETS.length; i++) {
                        Image spr = decals.get(i % decals.size());
                        drawUnscaledSprite(g, spr, anchorX + OCEAN_FREEZE_OFFSETS[i][0], anchorY + OCEAN_FREEZE_OFFSETS[i][1]);
                    }
                }
            }
        } finally {
            g.dispose();
        }
    }

    private static void renderCloudShadows(Graphics2D g2d, int w, int h, long timeMs, Weather weather) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isRaining = isRain(weather);
        boolean isStormy = weather == GameConstants.WEATHER_THUNDER || weather == GameConstants.WEATHER_HEAVY_RAIN;
        float baseAlpha = isStormy ? 0.35f : (isRaining ? 0.25f : 0.14f);

        int period1 = 120000;
        double progress1 = (timeMs % period1) / (double) period1;
        double driftX1 = progress1 * (w * 2) - w * 0.5;
        double driftY1 = (progress1 * 0.4) * h;

        Composite original = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, baseAlpha));
        g.setColor(new Color(20, 25, 45));

        Image sprite = getOverworldCloudSprite();
        int cloudW1 = Math.max(300, (int) (w * 0.45));
        int cloudH1 = Math.max(160, (int) (h * 0.30));

        for (int i = 0; i < 4; i++) {
            double cx = (driftX1 + i * (cloudW1 * 1.35)) % (w + cloudW1) - cloudW1 * 0.5;
            double cy = (driftY1 + ((i * 137) % h)) % (h + cloudH1) - cloudH1 * 0.5;
            if (sprite != null) {
                g.drawImage(sprite, (int) cx, (int) cy, cloudW1, cloudH1, null);
            } else {
                g.fill(new Ellipse2D.Double(cx, cy, cloudW1, cloudH1));
                g.fill(new Ellipse2D.Double(cx + cloudW1 * 0.25, cy - cloudH1 * 0.2, cloudW1 * 0.7, cloudH1 * 0.8));
            }
        }

        int period2 = 80000;
        double progress2 = ((timeMs + 40000) % period2) / (double) period2;
        double driftX2 = progress2 * (w * 2) - w * 0.5;
        double driftY2 = (progress2 * 0.3) * h;

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, baseAlpha * 0.75f));
        int cloudW2 = Math.max(240, (int) (w * 0.35));
        int cloudH2 = Math.max(130, (int) (h * 0.22));

        for (int i = 0; i < 5; i++) {
            double cx = (driftX2 + i * (cloudW2 * 1.45)) % (w + cloudW2) - cloudW2 * 0.5;
            double cy = (driftY2 + ((i * 211) % h)) % (h + cloudH2) - cloudH2 * 0.5;
            if (sprite != null) {
                g.drawImage(sprite, (int) cx, (int) cy, cloudW2, cloudH2, null);
            } else {
                g.fill(new Ellipse2D.Double(cx, cy, cloudW2, cloudH2));
            }
        }

        g.setComposite(original);
        g.dispose();
    }

    private static void renderPrecipitation(Graphics2D g2d, int w, int h, long timeMs, Weather weather) {
        if (isRain(weather)) {
            renderRain(g2d, w, h, timeMs, weather);
        } else if (isSnow(weather)) {
            renderSnow(g2d, w, h, timeMs, weather);
        } else if (weather == GameConstants.WEATHER_SAND_STORM) {
            renderSand(g2d, w, h, timeMs);
        }
    }

    private static void renderRain(Graphics2D g2d, int w, int h, long timeMs, Weather weather) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean heavy = weather == GameConstants.WEATHER_HEAVY_RAIN || weather == GameConstants.WEATHER_THUNDER;
        int count = heavy ? HEAVY_PARTICLE_COUNT : PARTICLE_COUNT;
        float strokeWidth = heavy ? 1.6f : 1.2f;
        int dropLength = heavy ? 22 : 16;
        double angleDx = 4.0;

        Color dropColor;
        if (weather == GameConstants.WEATHER_ACID_RAIN) {
            dropColor = new Color(175, 235, 60, 160);
        } else if (weather == GameConstants.WEATHER_THUNDER) {
            dropColor = new Color(205, 225, 255, 175);
        } else {
            dropColor = new Color(185, 215, 255, 145);
        }

        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setColor(dropColor);

        double speed = heavy ? 1.4 : 1.0;
        for (int i = 0; i < count; i++) {
            int seedX = (int) ((i * 47) % w);
            int seedY = (int) ((i * 89) % h);
            int fallRate = 18 + (i % 12);
            double fallY = (seedY + (timeMs * 0.05 * speed * fallRate)) % (h + dropLength);
            double fallX = (seedX + (fallY / (double) h) * angleDx * 30.0) % w;

            g.draw(new Line2D.Double(fallX, fallY, fallX + angleDx, fallY + dropLength));
        }

        g.dispose();
    }

    private static void renderSnow(Graphics2D g2d, int w, int h, long timeMs, Weather weather) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean heavy = weather == GameConstants.WEATHER_HEAVY_SNOW;
        int count = heavy ? (int) (SNOW_COUNT * 1.6) : SNOW_COUNT;
        Color flakeColor = new Color(255, 255, 255, heavy ? 200 : 165);
        g.setColor(flakeColor);

        for (int i = 0; i < count; i++) {
            int seedX = (int) ((i * 53) % w);
            int seedY = (int) ((i * 79) % h);
            int size = 2 + (i % 4);
            double fallRate = 4.0 + (i % 6);
            double fallY = (seedY + (timeMs * 0.01 * fallRate)) % (h + size);
            double sway = Math.sin((timeMs * 0.003) + i) * 12.0;
            double fallX = (seedX + sway + w) % w;

            g.fill(new Ellipse2D.Double(fallX, fallY, size, size));
        }

        g.dispose();
    }

    private static void renderSand(Graphics2D g2d, int w, int h, long timeMs) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setColor(new Color(215, 165, 85, 125));

        for (int i = 0; i < SAND_COUNT; i++) {
            int seedX = (int) ((i * 61) % w);
            int seedY = (int) ((i * 71) % h);
            int length = 25 + (i % 25);
            double fallX = (seedX + (timeMs * 0.08 * (12 + (i % 10)))) % (w + length);
            double fallY = (seedY + Math.sin(fallX * 0.02) * 8.0) % h;

            g.draw(new Line2D.Double(fallX, fallY, fallX + length, fallY + 3));
        }

        g.dispose();
    }

    public static boolean isRain(Weather weather) {
        return weather == GameConstants.WEATHER_RAIN
                || weather == GameConstants.WEATHER_HEAVY_RAIN
                || weather == GameConstants.WEATHER_THUNDER
                || weather == GameConstants.WEATHER_ACID_RAIN;
    }

    public static boolean isSnow(Weather weather) {
        return weather == GameConstants.WEATHER_SNOW
                || weather == GameConstants.WEATHER_HEAVY_SNOW;
    }
}

