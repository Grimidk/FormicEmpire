package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEventType;
import com.grimidk.formicempire.classes.entities.world.FaultLine;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapDialogModeTest {

    @Test
    void toggleMapModesAndPaint() throws Exception {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, null, world, null, null);
            try {
                assertEquals(MapDialog.MapMode.BIOME, dialog.getMapMode());

                dialog.setSize(800, 600);
                dialog.doLayout();
                BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                dialog.paint(g);

                dialog.setMapMode(MapDialog.MapMode.HEIGHT);
                assertEquals(MapDialog.MapMode.HEIGHT, dialog.getMapMode());
                dialog.paint(g);

                dialog.setMapMode(MapDialog.MapMode.TECTONIC_PLATES);
                assertEquals(MapDialog.MapMode.TECTONIC_PLATES, dialog.getMapMode());
                dialog.paint(g);

                dialog.setMapMode(MapDialog.MapMode.BIOME);
                assertEquals(MapDialog.MapMode.BIOME, dialog.getMapMode());
                dialog.paint(g);

                g.dispose();
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void tectonicPlatesModeRendersFaultBorders() throws Exception {
        assertEquals(new Color(175, 65, 230), MapDialog.FAULT_CONVERGENT_COLOR);
        assertEquals(new Color(245, 215, 30), MapDialog.FAULT_DIVERGENT_COLOR);
        assertEquals(new Color(50, 150, 255), MapDialog.FAULT_TRANSFORM_COLOR);

        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_PLAINS);
                h.setElevation(3);
                h.setPlateId(q < 0 ? 1 : 2);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        FaultLine fault = new FaultLine(1, 2, FaultLine.FaultType.CONVERGENT);
        fault.addBoundaryPair(hexMap.get("-1,0"), hexMap.get("0,0"));
        world.addFaultLine(fault);

        assertEquals(FaultLine.FaultType.CONVERGENT, world.getFaultTypeBetweenPlates(1, 2));

        FaultLine divFault = new FaultLine(2, 3, FaultLine.FaultType.DIVERGENT, 2);
        world.addFaultLine(divFault);
        assertEquals(FaultLine.FaultType.DIVERGENT, world.getFaultTypeBetweenPlates(2, 3));
        assertEquals(2, world.getFaultIntensityBetweenPlates(2, 3));

        FaultLine transFault = new FaultLine(3, 4, FaultLine.FaultType.TRANSFORM, 1);
        world.addFaultLine(transFault);
        assertEquals(FaultLine.FaultType.TRANSFORM, world.getFaultTypeBetweenPlates(3, 4));
        assertEquals(1, world.getFaultIntensityBetweenPlates(3, 4));

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, null, world, null, null);
            try {
                dialog.setSize(800, 600);
                dialog.doLayout();
                dialog.setMapMode(MapDialog.MapMode.TECTONIC_PLATES);
                assertEquals(MapDialog.MapMode.TECTONIC_PLATES, dialog.getMapMode());
                BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                dialog.paint(g);
                g.dispose();
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void geographicNamesLayerToggleAndRendering() throws Exception {
        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_PLAINS);
                h.setElevation(2);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        Hex m1 = hexMap.get("0,0");
        Hex m2 = hexMap.get("1,0");
        m1.setBiome(GameConstants.BIOME_MOUNTAIN);
        m1.setElevation(8);
        m2.setBiome(GameConstants.BIOME_PEAK);
        m2.setElevation(9);
        world.generateGeographicFeatures();

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, null, world, null, null);
            try {
                dialog.setSize(800, 600);
                dialog.doLayout();

                assertTrue(dialog.isShowGeographicNames());

                BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                dialog.paint(g);

                dialog.setShowGeographicNames(false);
                assertFalse(dialog.isShowGeographicNames());
                dialog.paint(g);

                dialog.setShowGeographicNames(true);
                assertTrue(dialog.isShowGeographicNames());
                dialog.paint(g);
                g.dispose();
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void testMapDialogLayersMenuAndGroupToggles() throws Exception {
        World world = new World();
        world.generateRectangularWorld(GameConstants.BIOME_PLAINS, new Colony(1, "Capital", true), "Player", LanguageStrings.DYNASTY_TITLE_DYNASTY);
        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, null, world, null, null);
            try {
                dialog.setSize(800, 600);
                dialog.doLayout();
                dialog.showLayersMenu();
                assertTrue(dialog.isShowCapitals());
                dialog.setShowCapitals(false);
                assertFalse(dialog.isShowCapitals());
                dialog.setShowCapitals(true);
                assertTrue(dialog.isShowCapitals());
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void spectatorRandomDisasterCheatButtonRespectsSandboxSettingAndTriggersDisaster() throws Exception {
        World world = new World();
        world.setSpectatorMode(true);
        world.generateRectangularWorld(GameConstants.BIOME_PLAINS, null, "Spectator", LanguageStrings.DYNASTY_TITLE_DYNASTY);

        Engine engine = new Engine();
        engine.setWorld(world);
        engine.setCheatRandomDisaster(false);

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, engine, world, null, null);
            try {
                assertFalse(dialog.getDisasterCheatButton().isVisible());
                assertFalse(dialog.getDisasterCheatButton().isEnabled());

                engine.setCheatRandomDisaster(true);
                dialog.liveUpdate();

                assertTrue(dialog.getDisasterCheatButton().isVisible());
                assertTrue(dialog.getDisasterCheatButton().isEnabled());

                dialog.getDisasterCheatButton().doClick();

                boolean anyHexMarked = world.getHexes().stream().anyMatch(h -> h != null && h.getDisasterType() != null);
                boolean anyHistory = world.getHistoryService().getEvents().stream()
                        .anyMatch(e -> e.getType() == WorldHistoryEventType.DISASTER);
                assertTrue(anyHexMarked);
                assertTrue(anyHistory);
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void timezoneMapModeColorsBoundariesAndPaintsCleanly() throws Exception {
        assertEquals("UTC", MapDialog.formatTimezoneBandLabel(0));
        assertEquals("+1", MapDialog.formatTimezoneBandLabel(1));
        assertEquals("+12", MapDialog.formatTimezoneBandLabel(12));
        assertEquals("-1", MapDialog.formatTimezoneBandLabel(-1));
        assertEquals("-12", MapDialog.formatTimezoneBandLabel(-12));
        for (int offset = -12; offset <= 12; offset++) {
            String label = MapDialog.formatTimezoneBandLabel(offset);
            if (offset != 0) {
                assertFalse(label.contains("UTC"));
            }
        }

        for (int offset = -12; offset < 12; offset++) {
            Hex a = new Hex();
            a.setBiome(GameConstants.BIOME_PLAINS);
            a.setTimeOffset(offset);
            Hex b = new Hex();
            b.setBiome(GameConstants.BIOME_PLAINS);
            b.setTimeOffset(offset + 1);
            assertNotEquals(
                    MapDialog.resolveTimezoneColorForHex(a),
                    MapDialog.resolveTimezoneColorForHex(b));
        }

        Hex negTwelve = new Hex();
        negTwelve.setBiome(GameConstants.BIOME_PLAINS);
        negTwelve.setTimeOffset(-12);
        Hex posTwelve = new Hex();
        posTwelve.setBiome(GameConstants.BIOME_PLAINS);
        posTwelve.setTimeOffset(12);
        Hex posEleven = new Hex();
        posEleven.setBiome(GameConstants.BIOME_PLAINS);
        posEleven.setTimeOffset(11);
        assertNotEquals(
                MapDialog.resolveTimezoneColorForHex(negTwelve),
                MapDialog.resolveTimezoneColorForHex(posTwelve));
        assertNotEquals(
                MapDialog.resolveTimezoneColorForHex(negTwelve),
                MapDialog.resolveTimezoneColorForHex(posEleven));

        Hex waterZero = new Hex();
        waterZero.setBiome(GameConstants.BIOME_OCEAN);
        waterZero.setTimeOffset(0);
        Hex landZero = new Hex();
        landZero.setBiome(GameConstants.BIOME_PLAINS);
        landZero.setTimeOffset(0);
        assertNotEquals(
                MapDialog.resolveTimezoneColorForHex(waterZero),
                MapDialog.resolveTimezoneColorForHex(landZero));

        assertNull(MapDialog.resolveTimezoneBoundaryColor(waterZero, landZero));
        Hex landOne = new Hex();
        landOne.setBiome(GameConstants.BIOME_PLAINS);
        landOne.setTimeOffset(1);
        assertEquals(MapDialog.TIMEZONE_BORDER_COLOR,
                MapDialog.resolveTimezoneBoundaryColor(landZero, landOne));
        assertEquals(MapDialog.INTERNATIONAL_DATE_LINE_COLOR,
                MapDialog.resolveTimezoneBoundaryColor(negTwelve, posTwelve));

        World world = new World();
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                new Colony(1, "Capital", true),
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);
        Engine engine = new Engine();
        engine.setWorld(world);

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, engine, world, null, null);
            try {
                dialog.setSize(800, 600);
                dialog.doLayout();
                dialog.setMapMode(MapDialog.MapMode.TIMEZONES);
                assertEquals(MapDialog.MapMode.TIMEZONES, dialog.getMapMode());

                BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                dialog.paint(g);

                engine.setCheatRevealMap(true);
                dialog.paint(g);
                g.dispose();
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void randomWeatherCheatButtonRespectsSettingAndRandomizesWorldWeather() throws Exception {
        World world = new World();
        world.setSpectatorMode(false);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                new Colony(1, "Capital", true),
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        Engine engine = new Engine();
        engine.setFreeAbilities(false);
        engine.setInfiniteResearch(false);
        engine.setInstantBuildings(false);
        engine.setAssimilateAll(false);
        engine.setEasyConquering(false);
        engine.setInstantIntegration(false);
        engine.setCheatFogOfWarToggle(false);
        engine.setCheatRandomWeather(false);
        engine.setWorld(world);

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, engine, world, null, null);
            try {
                assertFalse(dialog.getWeatherCheatButton().isVisible());
                assertFalse(dialog.getWeatherCheatButton().isEnabled());
                assertTrue(world.allowsAchievements());

                engine.setCheatRandomWeather(true);
                dialog.liveUpdate();

                assertTrue(dialog.getWeatherCheatButton().isVisible());
                assertTrue(dialog.getWeatherCheatButton().isEnabled());

                dialog.getWeatherCheatButton().doClick();

                Set<Weather> seen = new HashSet<>();
                for (Hex h : world.getHexes()) {
                    if (h != null && h.getLocalWeather() != null) {
                        seen.add(h.getLocalWeather());
                    }
                }
                assertTrue(seen.size() > 3);
                assertFalse(world.allowsAchievements());
            } finally {
                dialog.dispose();
            }
        });
    }
}
