package com.grimidk.formicempire.classes.entities.services.world;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class WorldWeatherServiceTest {

    @Test
    void isPrecipitationIdentifiesRainAndSnow() {
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_RAIN));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_HEAVY_RAIN));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_THUNDER));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_ACID_RAIN));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_SNOW));
        assertTrue(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_HEAVY_SNOW));

        assertFalse(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_CLEAR));
        assertFalse(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_FOG));
        assertFalse(WorldWeatherService.isPrecipitation(GameConstants.WEATHER_SAND_STORM));
        assertFalse(WorldWeatherService.isPrecipitation(null));
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
