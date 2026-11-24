package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.maps.SavedMapData;
import com.grimidk.formicempire.classes.infrasctructure.maps.SavedMapService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimezoneCalculationTest {

    @Test
    void calculateTimeZoneOffsetAtMeridianIsZero() {
        assertEquals(0, GameNumbers.calculateTimeZoneOffset(0, 128));
        assertEquals(0, GameNumbers.calculateTimeZoneOffset(0, 24));
        assertEquals(0, GameNumbers.calculateTimeZoneOffset(0, 72));
        assertEquals(0, GameNumbers.calculateTimeZoneOffset(0, 0));
        assertEquals(0, GameNumbers.calculateTimeZoneOffset(5, -1));
    }

    @Test
    void calculateTimeZoneOffsetIncrementsByOneHour() {
        int worldWidth = 24;
        for (int col = -12; col <= 11; col++) {
            assertEquals(col, GameNumbers.calculateTimeZoneOffset(col, worldWidth));
        }
    }

    @Test
    void calculateTimeZoneOffsetWrapsProperly() {
        int worldWidth = 128;
        assertEquals(0, GameNumbers.calculateTimeZoneOffset(0, worldWidth));
        int eastZone = GameNumbers.calculateTimeZoneOffset(32, worldWidth);
        int westZone = GameNumbers.calculateTimeZoneOffset(-32, worldWidth);
        assertEquals(6, eastZone);
        assertEquals(-6, westZone);
    }

    @Test
    void getTimeOfDayForHourMapsSolarCycleCorrectly() {
        assertSame(GameConstants.TIME_NIGHT, GameConstants.getTimeOfDayForHour(0));
        assertSame(GameConstants.TIME_NIGHT, GameConstants.getTimeOfDayForHour(4));
        assertSame(GameConstants.TIME_DAWN, GameConstants.getTimeOfDayForHour(5));
        assertSame(GameConstants.TIME_DAWN, GameConstants.getTimeOfDayForHour(6));
        assertSame(GameConstants.TIME_DAY, GameConstants.getTimeOfDayForHour(7));
        assertSame(GameConstants.TIME_DAY, GameConstants.getTimeOfDayForHour(12));
        assertSame(GameConstants.TIME_DAY, GameConstants.getTimeOfDayForHour(17));
        assertSame(GameConstants.TIME_DUSK, GameConstants.getTimeOfDayForHour(18));
        assertSame(GameConstants.TIME_DUSK, GameConstants.getTimeOfDayForHour(19));
        assertSame(GameConstants.TIME_NIGHT, GameConstants.getTimeOfDayForHour(20));
        assertSame(GameConstants.TIME_NIGHT, GameConstants.getTimeOfDayForHour(23));
    }

    @Test
    void worldLocalHourAndTimeOfDayReflectHexOffset() {
        World world = new World();
        world.setWorldWidth(128);
        world.setWorldHeight(64);
        world.setHour(12);

        Hex meridianHex = new Hex();
        meridianHex.setTimeOffset(0);

        Hex eastHex = new Hex();
        eastHex.setTimeOffset(8);

        Hex westHex = new Hex();
        westHex.setTimeOffset(-8);

        assertEquals(12, world.getLocalHour(meridianHex));
        assertSame(GameConstants.TIME_DAY, world.getTimeOfDay(meridianHex));

        assertEquals(20, world.getLocalHour(eastHex));
        assertSame(GameConstants.TIME_NIGHT, world.getTimeOfDay(eastHex));

        assertEquals(4, world.getLocalHour(westHex));
        assertSame(GameConstants.TIME_NIGHT, world.getTimeOfDay(westHex));

        assertEquals(12, meridianHex.getLocalHour(world));
        assertEquals(20, eastHex.getLocalHour(world));
        assertSame(GameConstants.TIME_DAY, meridianHex.getLocalTimeOfDay(world));
        assertSame(GameConstants.TIME_NIGHT, eastHex.getLocalTimeOfDay(world));
    }

    @Test
    void localDateRolloverForwardAcrossMidnight() {
        World world = new World();
        world.setWorldWidth(128);
        world.setWorldHeight(64);
        world.setHour(23);
        world.setDay(30);
        world.setMonth(12);
        world.setYear(1);

        Hex eastHex = new Hex();
        eastHex.setTimeOffset(2);

        assertEquals(1, world.getLocalHour(eastHex));
        assertEquals(1, world.getLocalDay(eastHex));
        assertEquals(1, world.getLocalMonth(eastHex));
        assertEquals(2, world.getLocalYear(eastHex));
    }

    @Test
    void localDateRollbackBackwardAcrossMidnight() {
        World world = new World();
        world.setWorldWidth(128);
        world.setWorldHeight(64);
        world.setHour(1);
        world.setDay(1);
        world.setMonth(1);
        world.setYear(2);

        Hex westHex = new Hex();
        westHex.setTimeOffset(-3);

        assertEquals(22, world.getLocalHour(westHex));
        assertEquals(30, world.getLocalDay(westHex));
        assertEquals(12, world.getLocalMonth(westHex));
        assertEquals(1, world.getLocalYear(westHex));
    }

    @Test
    void calculateTimeZoneOffsetHandlesOddWidthAndOutOfRangeColumns() {
        for (int col = -200; col <= 200; col++) {
            int offset = GameNumbers.calculateTimeZoneOffset(col, 25);
            assertTrue(offset >= -12 && offset <= 12);
        }
    }

    @Test
    void formatTimeZoneOffsetFormatsSignedUtcOffsets() {
        assertEquals("UTC-12", GameNumbers.formatTimeZoneOffset(-12));
        assertEquals("UTC-5", GameNumbers.formatTimeZoneOffset(-5));
        assertEquals("UTC+0", GameNumbers.formatTimeZoneOffset(0));
        assertEquals("UTC+6", GameNumbers.formatTimeZoneOffset(6));
        assertEquals("UTC+12", GameNumbers.formatTimeZoneOffset(12));
    }

    @Test
    void localDateDoesNotUnderflowYearZeroAndHandlesMidTickOverflow() {
        World world = new World();
        world.setWorldWidth(128);
        world.setWorldHeight(64);
        world.setHour(0);
        world.setDay(1);
        world.setMonth(1);
        world.setYear(0);

        Hex westHex = new Hex();
        westHex.setTimeOffset(-6);

        assertEquals(18, world.getLocalHour(westHex));
        assertEquals(1, world.getLocalDay(westHex));
        assertEquals(1, world.getLocalMonth(westHex));
        assertEquals(0, world.getLocalYear(westHex));

        world.setHour(24);
        world.setDay(30);
        world.setMonth(12);
        world.setYear(1);

        Hex meridianHex = new Hex();
        meridianHex.setTimeOffset(0);
        assertEquals(0, world.getLocalHour(meridianHex));
        assertEquals(1, world.getLocalDay(meridianHex));
        assertEquals(1, world.getLocalMonth(meridianHex));
        assertEquals(2, world.getLocalYear(meridianHex));
    }

    @Test
    void worldEnvironmentUsesActiveHexLocalTimeOfDay() {
        World world = new World();
        world.setWorldWidth(128);
        world.setWorldHeight(64);
        world.setHour(0);

        Hex utcZeroHex = new Hex();
        utcZeroHex.setQ(0);
        utcZeroHex.setR(0);
        utcZeroHex.setBiome(GameConstants.BIOME_PLAINS);
        utcZeroHex.setLocalWeather(GameConstants.WEATHER_CLEAR);
        utcZeroHex.setTimeOffset(0);

        Hex noonHex = new Hex();
        noonHex.setQ(64);
        noonHex.setR(0);
        noonHex.setBiome(GameConstants.BIOME_PLAINS);
        noonHex.setLocalWeather(GameConstants.WEATHER_CLEAR);
        noonHex.setTimeOffset(12);

        world.setHexes(new java.util.ArrayList<>(List.of(utcZeroHex, noonHex)));

        world.changeActiveHex(noonHex);
        int noonTemp = world.getTemperature();

        world.changeActiveHex(utcZeroHex);
        int nightTemp = world.getTemperature();

        assertEquals(Math.round(GameConstants.BIOME_PLAINS.getTemperature() * GameConstants.TIME_DAY.getTempMult()), noonTemp);
        assertEquals(Math.round(GameConstants.BIOME_PLAINS.getTemperature() * GameConstants.TIME_NIGHT.getTempMult()), nightTemp);
        assertNotEquals(noonTemp, nightTemp);
    }

    @Test
    void savedMapServiceComputesCanonicalTimeOffsetOnBuild() {
        SavedMapData data = new SavedMapData();
        data.worldWidth = 128;
        data.worldHeight = 64;
        SavedMapData.SavedMapHex eastHex = new SavedMapData.SavedMapHex();
        eastHex.q = 32;
        eastHex.r = 0;
        eastHex.biomeId = GameConstants.BIOME_PLAINS.getId();
        eastHex.timeOffset = 0;
        data.hexes = List.of(eastHex);

        World built = SavedMapService.buildWorldFromMapData(data);
        Hex resolved = built.getHexAt(32, 0);
        assertEquals(6, resolved.getTimeOffset());
    }
}
