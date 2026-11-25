package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.services.world.WarCreatureCombatService;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class HexMapOverlaysTest {

    @Test
    void tradeLegProgressMovesFromOriginTowardDestination() {
        Hex origin = new Hex();
        origin.setQ(0);
        origin.setR(0);
        Hex destination = new Hex();
        destination.setQ(1);
        destination.setR(0);
        Trade trade = new Trade(
                origin,
                destination,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                false,
                false,
                GameConstants.METHOD_LAND);
        trade.restoreTripState(10, 10, false);
        assertEquals(0.0, HexMapOverlays.tradeLegProgress(trade), 1e-9);
        trade.restoreTripState(10, 5, false);
        assertEquals(0.5, HexMapOverlays.tradeLegProgress(trade), 1e-9);
        trade.restoreTripState(10, 0, false);
        assertEquals(1.0, HexMapOverlays.tradeLegProgress(trade), 1e-9);
    }

    @Test
    void tradeLegProgressAdvancesWithinCurrentHour() {
        Hex origin = new Hex();
        origin.setQ(0);
        origin.setR(0);
        Hex destination = new Hex();
        destination.setQ(1);
        destination.setR(0);
        Trade trade = new Trade(
                origin,
                destination,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                false,
                false,
                GameConstants.METHOD_LAND);
        trade.restoreTripState(10, 5, false);
        assertEquals(0.5, HexMapOverlays.tradeLegProgress(trade, 0), 1e-9);
        assertEquals(0.55, HexMapOverlays.tradeLegProgress(trade, 30), 1e-9);
        assertEquals(0.5 + (59.0 / 60.0) / 10.0, HexMapOverlays.tradeLegProgress(trade, 59), 1e-9);
    }

    @Test
    void tradeMarkerFacingPointsAlongTravelDirection() {
        assertEquals(Math.PI / 2.0, HexMapOverlays.tradeMarkerFacingRadians(0, 0, 1, 0), 1e-9);
        assertEquals(Math.PI, HexMapOverlays.tradeMarkerFacingRadians(0, 0, 0, 1), 1e-9);
        assertEquals(0.0, HexMapOverlays.tradeMarkerFacingRadians(0, 0, 0, -1), 1e-9);
        assertEquals(3.0 * Math.PI / 2.0, HexMapOverlays.tradeMarkerFacingRadians(0, 0, -1, 0), 1e-9);
    }

    @Test
    void areNeighborsMatchesFlatTopAdjacency() {
        Hex a = new Hex();
        a.setQ(0);
        a.setR(0);
        Hex neighbor = new Hex();
        neighbor.setQ(1);
        neighbor.setR(0);
        Hex far = new Hex();
        far.setQ(2);
        far.setR(0);
        assertTrue(HexMapOverlays.areNeighbors(a, neighbor));
        assertFalse(HexMapOverlays.areNeighbors(a, far));
    }

    @Test
    void paintDayNightAndWeatherCloudsExecuteSafely() {
        World world = new World();
        Hex h1 = new Hex();
        h1.setQ(0);
        h1.setR(0);
        h1.setBiome(GameConstants.BIOME_PLAINS);
        h1.setLocalWeather(GameConstants.WEATHER_RAIN);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(h1);
        world.setHexes(hexes);

        HexGridMesh mesh = HexGridMesh.build(hexes, 0.0, 0.0, 24.0, (q, r) -> h1.getQ() == q && h1.getR() == r ? h1 : null);
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        assertDoesNotThrow(() -> HexMapOverlays.paintDayNightFilter(g2d, world, mesh, hex -> true));
        assertDoesNotThrow(() -> HexMapOverlays.paintWeatherClouds(g2d, world, mesh, hex -> true));
        assertDoesNotThrow(() -> HexMapOverlays.paintWeatherClouds(g2d, world, mesh, hex -> true, true));
        assertDoesNotThrow(() -> HexMapOverlays.paintWeatherClouds(g2d, world, mesh, hex -> true, false));
        assertDoesNotThrow(() -> HexMapOverlays.paintDayNightFilter(null, null, null, null));
        assertDoesNotThrow(() -> HexMapOverlays.paintWeatherClouds(null, null, null, null));
        assertDoesNotThrow(() -> HexMapOverlays.paintWeatherClouds(null, null, null, null, true));

        g2d.dispose();
    }

    @Test
    void paintWeatherCloudsDrawsWeatherIconPerDiscoveredWeatherHex() {
        for (Weather weather : GameConstants.getWeathers()) {
            World world = new World();
            Hex h1 = new Hex();
            h1.setQ(0);
            h1.setR(0);
            h1.setBiome(GameConstants.BIOME_PLAINS);
            h1.setLocalWeather(weather);
            ArrayList<Hex> hexes = new ArrayList<>();
            hexes.add(h1);
            world.setHexes(hexes);

            HexGridMesh mesh = HexGridMesh.build(hexes, 50.0, 50.0, 28.0, (q, r) -> h1.getQ() == q && h1.getR() == r ? h1 : null);
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            HexMapOverlays.paintWeatherClouds(g2d, world, mesh, hex -> true, false);
            g2d.dispose();

            BufferedImage hiddenImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gHidden = hiddenImg.createGraphics();
            HexMapOverlays.paintWeatherClouds(gHidden, world, mesh, hex -> false, false);
            gHidden.dispose();
            assertFalse(hasNonZeroAlphaNear(hiddenImg, 50, 50, 28));

            BufferedImage badgeImg = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gBadge = badgeImg.createGraphics();
            HexMapOverlays.paintWeatherBadge(gBadge, weather, 30.0, 30.0, 18);
            gBadge.dispose();

            boolean hasMapPixels = hasNonZeroAlphaNear(img, 50, 50, 12);
            boolean hasBadgePixels = hasNonZeroAlphaNear(badgeImg, 30, 30, 12);
            if (weather == GameConstants.WEATHER_CLEAR) {
                assertFalse(hasMapPixels, "WEATHER_CLEAR should not paint map weather icon");
                assertFalse(hasBadgePixels, "WEATHER_CLEAR should not paint weather badge");
            } else {
                ImageIcon icon = weather.getIcon();
                assertNotNull(icon, "Missing weather icon for " + weather.getNameKey());
                assertNotEquals(GameConstants.ICON_UNKNOWN, icon, "Unknown icon for " + weather.getNameKey());
                assertNotNull(icon.getImage(), "Null icon image for " + weather.getNameKey());
                assertTrue(hasMapPixels, "Expected visible weather icon on map for " + weather.getNameKey());
                assertTrue(hasBadgePixels, "Expected visible weather badge for " + weather.getNameKey());
            }
        }

        World nullWeatherWorld = new World();
        Hex nullHex = new Hex();
        nullHex.setQ(0);
        nullHex.setR(0);
        nullHex.setBiome(GameConstants.BIOME_PLAINS);
        nullHex.setLocalWeather(null);
        nullWeatherWorld.setWeather(null);
        ArrayList<Hex> nullHexes = new ArrayList<>(List.of(nullHex));
        HexGridMesh nullMesh = HexGridMesh.build(nullHexes, 50.0, 50.0, 28.0, (q, r) -> nullHex);
        BufferedImage nullMapImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gNullMap = nullMapImg.createGraphics();
        HexMapOverlays.paintWeatherClouds(gNullMap, nullWeatherWorld, nullMesh, hex -> true, false);
        gNullMap.dispose();
        assertFalse(hasNonZeroAlphaNear(nullMapImg, 50, 50, 28));

        BufferedImage nullBadgeImg = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gNullBadge = nullBadgeImg.createGraphics();
        HexMapOverlays.paintWeatherBadge(gNullBadge, null, 30.0, 30.0, 18);
        gNullBadge.dispose();
        assertFalse(hasNonZeroAlphaNear(nullBadgeImg, 30, 30, 14));
    }

    @Test
    void paintDisastersExecutesSafelyAndMarksDisasterHexes() {
        World world = new World();
        Hex h1 = new Hex();
        h1.setQ(0);
        h1.setR(0);
        h1.setBiome(GameConstants.BIOME_PLAINS);
        h1.recordDisaster(GameConstants.DISASTER_EARTHQUAKE);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(h1);
        world.setHexes(hexes);

        HexGridMesh mesh = HexGridMesh.build(hexes, 0.0, 0.0, 24.0, (q, r) -> h1.getQ() == q && h1.getR() == r ? h1 : null);
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        assertDoesNotThrow(() -> HexMapOverlays.paintDisasters(g2d, world, mesh, hex -> true));
        assertDoesNotThrow(() -> HexMapOverlays.paintDisasterBadge(g2d, GameConstants.DISASTER_VOLCANO, 50, 50, 18));
        assertDoesNotThrow(() -> HexMapOverlays.paintDisasters(null, null, null, null));

        g2d.dispose();
    }

    @Test
    void paintBattlesSinglePassDrawsOnlyWarringBordersAcrossMultipleWars() {
        World world = new World();
        Dynasty d1 = new Dynasty(1, "D1", true, GameConstants.SPECIES_OMNI);
        Dynasty d2 = new Dynasty(2, "D2", false, GameConstants.SPECIES_OMNI);
        Dynasty d3 = new Dynasty(3, "D3", false, GameConstants.SPECIES_OMNI);

        Colony c1 = new Colony(1, "C1", true);
        Colony c2 = new Colony(2, "C2", false);
        Colony c3 = new Colony(3, "C3", false);
        d1.addColony(c1);
        d2.addColony(c2);
        d3.addColony(c3);
        c1.setDynasty(d1);
        c2.setDynasty(d2);
        c3.setDynasty(d3);

        Hex h1 = new Hex(GameConstants.BIOME_PLAINS, c1, null, null, null, null, null, null);
        h1.setQ(0);
        h1.setR(0);
        Hex h2 = new Hex(GameConstants.BIOME_PLAINS, c2, null, null, null, null, null, null);
        h2.setQ(1);
        h2.setR(0);
        Hex h3 = new Hex(GameConstants.BIOME_PLAINS, c3, null, null, null, null, null, null);
        h3.setQ(0);
        h3.setR(1);

        ArrayList<Hex> hexes = new ArrayList<>(List.of(h1, h2, h3));
        world.setHexes(hexes);
        world.registerDynasty(d1);
        world.registerDynasty(d2);
        world.registerDynasty(d3);
        try {
            world.getWarService().beginWar(d1, d2);
            world.getWarService().beginWar(d1, d3);

            HexGridMesh mesh = HexGridMesh.build(hexes, 200.0, 200.0, 40.0, (q, r) -> {
                for (Hex h : hexes) {
                    if (h.getQ() == q && h.getR() == r) {
                        return h;
                    }
                }
                return null;
            });

            BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            assertDoesNotThrow(() -> HexMapOverlays.paintBattles(g2d, world, mesh, hex -> true));
            g2d.dispose();

            assertEquals(2, world.getWarService().getActiveWars().size());
            int checkedEdges = 0;
            for (HexGridMesh.Edge edge : mesh.edges()) {
                if (edge.faceA == null || edge.faceB == null) {
                    continue;
                }
                checkedEdges++;
                int midX = (int) Math.round((edge.a.x + edge.b.x) * 0.5);
                int midY = (int) Math.round((edge.a.y + edge.b.y) * 0.5);
                int idA = edge.faceA.hex.getColony().getDynasty().getId();
                int idB = edge.faceB.hex.getColony().getDynasty().getId();
                boolean shouldDraw = (idA == 1 || idB == 1);
                boolean hasPaintedPixels = hasNonZeroAlphaNear(img, midX, midY, 10);
                assertEquals(shouldDraw, hasPaintedPixels);
            }
            assertEquals(3, checkedEdges);
        } finally {
            WarCreatureCombatService.clearAll();
        }
    }

    private static boolean hasNonZeroAlphaNear(BufferedImage img, int cx, int cy, int radius) {
        for (int y = Math.max(0, cy - radius); y <= Math.min(img.getHeight() - 1, cy + radius); y++) {
            for (int x = Math.max(0, cx - radius); x <= Math.min(img.getWidth() - 1, cx + radius); x++) {
                if ((img.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
