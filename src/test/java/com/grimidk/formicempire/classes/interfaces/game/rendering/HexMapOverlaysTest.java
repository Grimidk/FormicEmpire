package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
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
}
