package com.grimidk.formicempire.classes.interfaces.game.rendering;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.net.URL;
import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class OverworldAtmosphereRenderer {

    private static final int PARTICLE_COUNT = 90;
    private static final int HEAVY_PARTICLE_COUNT = 160;
    private static final int SNOW_COUNT = 75;
    private static final int SAND_COUNT = 85;

    private static Image overworldCloudSprite;
    private static long accumulatedAnimTimeMs = 0L;
    private static long lastRealTimeMs = 0L;

    private OverworldAtmosphereRenderer() {
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

