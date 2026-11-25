package com.grimidk.formicempire.classes.entities.services.world;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class WorldWeatherServiceTest {

    @Test
    void isPrecipitationAndHasVisualWeatherIdentifyAllWeatherTypes() {
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_RAIN));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_HEAVY_RAIN));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_THUNDER));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_ACID_RAIN));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_BLOOD));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_FROG));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_SNOW));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_HEAVY_SNOW));

        assertFalse(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_CLEAR));
        assertFalse(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_FOG));
        assertFalse(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_SAND_STORM));
        assertFalse(WorldWeatherService.isPrecipitation(null));

        assertFalse(WorldWeatherService.hasVisualWeather(null));
        assertFalse(WorldWeatherService.hasVisualWeather(GameConstants.WEATHER_CLEAR));
        for (Weather w : GameConstants.getWeathers()) {
            if (w != GameConstants.WEATHER_CLEAR) {
                assertTrue(WorldWeatherService.hasVisualWeather(w), "Expected visual weather for " + w.getNameKey());
            }
        }
    }

    @Test
    void randomizeWorldWeatherCoversAllNonClearTypesAcrossWorld() {
        assertEquals(0, WorldWeatherService.randomizeWorldWeather(null));
        assertEquals(0, WorldWeatherService.randomizeWorldWeather(new World()));

        World world = new World();
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                null,
                "Spectator",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        int affected = world.randomizeAllWeather();
        assertTrue(affected > 0);

        Set<Weather> seen = new HashSet<>();
        for (Hex h : world.getHexes()) {
            if (h != null) {
                assertNotNull(h.getLocalWeather());
                seen.add(h.getLocalWeather());
            }
        }
        for (Weather w : GameConstants.getWeathers()) {
            if (WorldWeatherService.hasVisualWeather(w)) {
                assertTrue(seen.contains(w), "Expected randomized weather to include " + w.getNameKey());
            }
        }
    }

    @Test
    void applyCoherentWeatherPatchesAssignsWeatherToHexes() {
        World world = new World();
        ArrayList<Hex> hexes = new ArrayList<>();
        for (int q = -2; q <= 2; q++) {
            for (int r = -2; r <= 2; r++) {
                Hex hex = new Hex();
                hex.setQ(q);
                hex.setR(r);
                hex.setBiome(GameConstants.BIOME_PLAINS);
                hexes.add(hex);
            }
        }
        world.setHexes(hexes);

        WorldWeatherService.applyCoherentWeatherPatches(world);

        boolean anyNonNull = false;
        for (Hex h : world.getHexes()) {
            if (h.getLocalWeather() != null) {
                anyNonNull = true;
                break;
            }
        }
        assertTrue(anyNonNull);
    }

    @Test
    void tickWeatherDriftHandlesNullGracefully() {
        assertDoesNotThrow(() -> WorldWeatherService.tickWeatherDrift(null));
        World world = new World();
        assertDoesNotThrow(() -> WorldWeatherService.tickWeatherDrift(world));
    }
}
