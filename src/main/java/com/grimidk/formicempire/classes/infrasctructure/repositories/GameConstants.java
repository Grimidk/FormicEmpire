package com.grimidk.formicempire.classes.infrasctructure.repositories;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntStatus;
import com.grimidk.formicempire.classes.constants.ant.AntSubType;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.ant.MoveStatus;
import com.grimidk.formicempire.classes.constants.misc.BugType;
import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Humidity;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import java.net.URL; 
import java.util.HashMap;
import java.util.Map;

public final class GameConstants {
    private GameConstants() {}

    private static final Map<String, ImageIcon> iconCache = new HashMap<>();

    private static ImageIcon loadIcon(String path) {
        if (iconCache.containsKey(path)) return iconCache.get(path);
        
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resourceUrl == null) {
            System.err.println("CRITICAL ERROR: Resource not found: " + path);
            return null; 
        }
        ImageIcon icon = new ImageIcon(resourceUrl);
        iconCache.put(path, icon);
        return icon;
    }

    /** Expected pixel size of room PNGs under {@code sprites/buildings/rooms/}; keep in sync with {@link BuildingSpriteResources#ROOM_ICON_PX}. */
    public static final int BUILDING_ROOM_ICON_SIZE_PX = BuildingSpriteResources.ROOM_ICON_PX;

    /**
     * Room / underground decoration sprite for a main building chain (classpath PNGs; see {@link BuildingSpriteResources}).
     */
    public static ImageIcon buildingRoomSprite(String chainKey, int level) {
        return BuildingSpriteResources.roomTier(chainKey, level);
    }

    /**
     * Passive colony building overlay (see {@link BuildingSpriteResources#passiveRoom(String)}).
     */
    public static ImageIcon passiveBuildingRoomSprite(String passiveKey) {
        return BuildingSpriteResources.passiveRoom(passiveKey);
    }

    public static ImageIcon getAntSprite(AntType type, Species species) {
        if (type == null) return null;
        
        String path;
        if (type == TYPE_EGG || type == TYPE_LARVA || type == TYPE_PUPA || type == TYPE_DEAD || type == TYPE_ZOMBIE) {
            path = "sprites/ants/" + type.getSpriteName();
        } else {
            String dir = (species != null) ? species.getDirectory() : "omni/";
            path = "sprites/ants/" + dir + type.getSpriteName();
        }
        
        return loadIcon(path);
    }

    // --- Lists ---
    private static final List<Biome> biomes = new ArrayList<>();
    private static final List<ResourceType> resources = new ArrayList<>();
    private static final List<TimeOfDay> timesOfDay = new ArrayList<>();
    private static final List<MoonPhase> moonPhases = new ArrayList<>();
    private static final List<Season> seasons = new ArrayList<>();
    private static final List<Weather> weathers = new ArrayList<>();
    private static final List<AntStatus> antStatuses = new ArrayList<>();
    private static final List<MoveStatus> moveStatuses = new ArrayList<>();
    private static final List<BugType> bugTypes = new ArrayList<>();
    private static final List<AntType> antTypes = new ArrayList<>();
    private static final List<AntSubType> antSubTypes = new ArrayList<>();
    private static final List<AntRole> antRoles = new ArrayList<>();
    private static final List<ColonyRank> colonyRanks = new ArrayList<>();
    private static final List<Species> species = new ArrayList<>();
    private static final List<TradeMethod> tradeMethods = new ArrayList<>();
    private static final List<Humidity> humidity = new ArrayList<>();
    private static final List<Temperature> temperature = new ArrayList<>();
    private static final List<ImageIcon> misc = new ArrayList<>();

    // --- Misc Icons ---
    public static final ImageIcon ICON_APHID = loadIcon("icons/bugs/aphid.png");
    static { misc.add(ICON_APHID); }
    public static final ImageIcon ICON_SOIL_MITE = loadIcon("icons/bugs/soilMite.png");
    static { misc.add(ICON_SOIL_MITE); }
    public static final ImageIcon ICON_DERMESTID = loadIcon("icons/bugs/dermestid.png");
    static { misc.add(ICON_DERMESTID); }
    public static final ImageIcon ICON_PARASITIC_MITE = loadIcon("icons/bugs/parasiticMite.png");
    static { misc.add(ICON_PARASITIC_MITE); }
    public static final ImageIcon ICON_RESEARCH = loadIcon("icons/misc/research.png");
    static { misc.add(ICON_RESEARCH); }

    // --- Base Stats ---
    public static final float BASE_SPRITE_SPEED = 2.5f;
    public static final int PARASITIC_MITES_ON_ANT_SPRITE = 5;

    public static final float GATHER_FULL_EFFICIENCY_RADIUS_BASE = 500f;
    public static final double GATHER_MIN_EFFICIENCY = 0.01;
    public static final float GATHER_COLONY_SPEED_RADIUS_MULT = 1.25f;

    public static final int HEX_RESOURCE_DEPLETION_SOURCES_PER_PERCENT = 20;
    public static final int HEX_DEPLETION_SPAWN_BUFFER_EXTRA_MAX = 500;
    
    public static final int RESOURCE_SPAWN_VIEWPORT_MARGIN = 64;
    public static final int RESOURCE_SPAWN_EXTRA_DISTANCE_MIN = 80;
    public static final int RESOURCE_SPAWN_EXTRA_DISTANCE_MAX = 320;
    public static final int RESOURCE_SPAWN_BUFFER_EXTRA_CAP = 400;
    public static final int SOURCE_DISPLAY_PX_SMALL = 36;
    public static final int SOURCE_DISPLAY_PX_MEDIUM = 52;
    public static final int SOURCE_DISPLAY_PX_BIG = 72;
    public static final int SOURCE_DISPLAY_PX_HUGE = 96;
    public static final int HEX_SUSTAIN_MAX_DEPLETION_PCT = 80;

    public static final int BIOME_COLD_TEMP_MAX = 15;
    public static final int BIOME_DRY_HUMIDITY_MAX = 1;

    public static final int PET_POOL_PER_CATCHER = 10;
    public static final int PET_CAPACITY_PER_TENDER = 10;
    public static final int PET_BREED_MIN_COUNT = 2;
    public static final float CATCH_BASE_CHANCE_PER_CATCHER = 0.12f;
    public static final int SOIL_MITE_PARASITIC_MITE_KILL_PER_DAY = 5;
    public static final int SOIL_MITE_PARASITIC_MITE_KILL_UPGRADED = 12;

    public static final int PARASITIC_MITE_RESOURCE_THRESHOLD = 5_000;
    public static final float PARASITIC_MITE_MONTHLY_SPAWN_CHANCE = 0.65f;
    public static final int PARASITIC_MITE_MIN_MONTHLY_SPAWN = 1_000;
    public static final int PARASITIC_MITE_PER_ANT = 50;
    public static final float PARASITIC_MITE_SPREAD_FACTOR = 0.50f;
    public static final int PARASITIC_MITES_PER_SLOWED_ANT = 10;
    public static final float PARASITIC_MITE_SPEED_MULTIPLIER = 0.5f;

    // --- Temperatures ---
    public static final Temperature TEMP_FREEZING = new Temperature(1, LanguageStrings.TEMP_FREEZING, 5,
        loadIcon("icons/temp/freezing.png"));
    static { temperature.add(TEMP_FREEZING); }
    public static final Temperature TEMP_COLD = new Temperature(2, LanguageStrings.TEMP_COLD, 15,
        loadIcon("icons/temp/cold.png"));
    static { temperature.add(TEMP_COLD); }
    public static final Temperature TEMP_CHILLY = new Temperature(3, LanguageStrings.TEMP_CHILLY, 20,
        loadIcon("icons/temp/chilly.png"));
    static { temperature.add(TEMP_CHILLY); }
    public static final Temperature TEMP_GOOD = new Temperature(4, LanguageStrings.TEMP_GOOD, 28,
        loadIcon("icons/temp/good.png"));
    static { temperature.add(TEMP_GOOD); }
    public static final Temperature TEMP_WARM = new Temperature(5, LanguageStrings.TEMP_WARM, 35,
        loadIcon("icons/temp/warm.png"));
    static { temperature.add(TEMP_WARM); }
    public static final Temperature TEMP_HOT = new Temperature(6, LanguageStrings.TEMP_HOT, 45,
        loadIcon("icons/temp/hot.png"));
    static { temperature.add(TEMP_HOT); }    
    public static final Temperature TEMP_BURNING = new Temperature(7, LanguageStrings.TEMP_BURNING, 100,
        loadIcon("icons/temp/burning.png"));
    static { temperature.add(TEMP_BURNING); }    

    // --- Humidity
    public static final Humidity HUMID_0 = new Humidity(1, LanguageStrings.HUMID_ARID, 0,
        loadIcon("icons/humid/humid0.png"));
    static { humidity.add(HUMID_0); }    
    public static final Humidity HUMID_1 = new Humidity(2, LanguageStrings.HUMID_DRY, 1, 
        loadIcon("icons/humid/humid1.png"));
    static { humidity.add(HUMID_1); }
    public static final Humidity HUMID_2 = new Humidity(3, LanguageStrings.HUMID_NORMAL, 2, 
        loadIcon("icons/humid/humid2.png"));
    static { humidity.add(HUMID_2); }
    public static final Humidity HUMID_3 = new Humidity(4, LanguageStrings.HUMID_HUMID, 3, 
        loadIcon("icons/humid/humid3.png"));
    static { humidity.add(HUMID_3); }
    public static final Humidity HUMID_4 = new Humidity(5, LanguageStrings.HUMID_MOIST, 4, 
        loadIcon("icons/humid/humid4.png"));
    static { humidity.add(HUMID_4); }
    public static final Humidity HUMID_5 = new Humidity(6, LanguageStrings.HUMID_SATURATED, 5, 
        loadIcon("icons/humid/humid5.png"));
    static { humidity.add(HUMID_5); }

    // --- Biomes ---
    public static final Biome BIOME_PLAINS = new Biome(1, LanguageStrings.BIOME_PLAINS, 25, 2, 1f, 1f, 0f,
        loadIcon("icons/biomes/plains.png"), null);
    static { biomes.add(BIOME_PLAINS); }
    public static final Biome BIOME_FOREST = new Biome(2, LanguageStrings.BIOME_FOREST, 22, 3, 2f, 2f, 0.1f,
        loadIcon("icons/biomes/forest.png"), null);
    static { biomes.add(BIOME_FOREST); }
    public static final Biome BIOME_JUNGLE = new Biome(3, LanguageStrings.BIOME_JUNGLE, 30, 4, 2.5f, 2.5f, 0.2f,
        loadIcon("icons/biomes/jungle.png"), null);
    static { biomes.add(BIOME_JUNGLE); }
    public static final Biome BIOME_SWAMP = new Biome(4, LanguageStrings.BIOME_SWAMP, 26, 5, 1.5f, 1.5f, 0f,
        loadIcon("icons/biomes/swamp.png"), null);
    static { biomes.add(BIOME_SWAMP); }
    public static final Biome BIOME_URBAN = new Biome(5, LanguageStrings.BIOME_URBAN, 28, 1, 0.5f, 0.5f, 0.5f,
        loadIcon("icons/biomes/urban.png"), null);
    static { biomes.add(BIOME_URBAN); }
    public static final Biome BIOME_TUNDRA = new Biome(6, LanguageStrings.BIOME_TUNDRA, 5, 2, 0.2f, 0.2f, 0.3f,
        loadIcon("icons/biomes/tundra.png"), null);
    static { biomes.add(BIOME_TUNDRA); }
    public static final Biome BIOME_TAIGA = new Biome(7, LanguageStrings.BIOME_TAIGA, 12, 1, 0.4f, 0.3f, 0.5f,
        loadIcon("icons/biomes/taiga.png"), null);
    static { biomes.add(BIOME_TAIGA); }
    public static final Biome BIOME_DESERT = new Biome(8, LanguageStrings.BIOME_DESERT, 45, 0, 0.05f, 0.02f, 1f,
        loadIcon("icons/biomes/dessert.png"), null);
    static { biomes.add(BIOME_DESERT); }
    public static final Biome BIOME_MOUNTAIN = new Biome(9, LanguageStrings.BIOME_MOUNTAIN, 12, 1, 0.1f, 0.3f, 2f,
        loadIcon("icons/biomes/mountain.png"), null);
    static { biomes.add(BIOME_MOUNTAIN); }
    public static final Biome BIOME_VOLCANIC = new Biome(10, LanguageStrings.BIOME_VOLCANIC, 60, 0, 0.01f, 0.01f, 5f,
        loadIcon("icons/biomes/volcanic.png"), null);
    static { biomes.add(BIOME_VOLCANIC); }
    public static final Biome BIOME_LAKE = new Biome(11, LanguageStrings.BIOME_LAKE, 25, 5, 0.5f, 0.5f, 0f,
        loadIcon("icons/biomes/lake.png"), null);
    static { biomes.add(BIOME_LAKE); }
    public static final Biome BIOME_OCEAN = new Biome(12, LanguageStrings.BIOME_OCEAN, 20, 5, 0.2f, 0.2f, 0f,
        loadIcon("icons/biomes/ocean.png"), null);
    static { biomes.add(BIOME_OCEAN); }

    // --- Resources ---
    public static final ResourceType RESOURCE_PLANT = new ResourceType(1, LanguageStrings.RESOURCE_PLANT, true, false,
        loadIcon("icons/resources/plant.png"),
        loadIcon("sprites/sources/plant_small.png"),
        loadIcon("sprites/sources/plant_medium.png"),
        loadIcon("sprites/sources/plant_big.png"),
        loadIcon("sprites/sources/plant_huge.png"));
    static { resources.add(RESOURCE_PLANT); }
    public static final ResourceType RESOURCE_FUNGI = new ResourceType(2, LanguageStrings.RESOURCE_FUNGI, true, false,
        loadIcon("icons/resources/mushroom.png"),
        loadIcon("sprites/sources/mushroom_small.png"),
        loadIcon("sprites/sources/mushroom_medium.png"),
        loadIcon("sprites/sources/mushroom_big.png"),
        loadIcon("sprites/sources/mushroom_huge.png"));
    static { resources.add(RESOURCE_FUNGI); }
    public static final ResourceType RESOURCE_MEAT = new ResourceType(3, LanguageStrings.RESOURCE_MEAT, true, false,
        loadIcon("icons/resources/protein.png"),
        loadIcon("sprites/sources/protein_small.png"),
        loadIcon("sprites/sources/protein_medium.png"),
        loadIcon("sprites/sources/protein_big.png"),
        loadIcon("sprites/sources/protein_huge.png"));
    static { resources.add(RESOURCE_MEAT); }
    public static final ResourceType RESOURCE_WATER = new ResourceType(4, LanguageStrings.RESOURCE_WATER, true, true,
        loadIcon("icons/resources/water.png"),
        loadIcon("sprites/sources/water_small.png"),
        loadIcon("sprites/sources/water_medium.png"),
        loadIcon("sprites/sources/water_big.png"),
        loadIcon("sprites/sources/water_huge.png"));
    static { resources.add(RESOURCE_WATER); }
    public static final ResourceType RESOURCE_SYRUP = new ResourceType(5, LanguageStrings.RESOURCE_SYRUP, true, true,
        loadIcon("icons/resources/syrup.png"),
        loadIcon("sprites/sources/syrup_small.png"),
        loadIcon("sprites/sources/syrup_medium.png"),
        loadIcon("sprites/sources/syrup_big.png"),
        loadIcon("sprites/sources/syrup_huge.png"));
    static { resources.add(RESOURCE_SYRUP); }
    public static final ResourceType RESOURCE_RESIN = new ResourceType(6, LanguageStrings.RESOURCE_RESIN, false, true,
        loadIcon("icons/resources/resin.png"),
        loadIcon("sprites/sources/resin_small.png"),
        loadIcon("sprites/sources/resin_medium.png"),
        loadIcon("sprites/sources/resin_big.png"),
        loadIcon("sprites/sources/resin_huge.png"));
    static { resources.add(RESOURCE_RESIN); }
    public static final ResourceType RESOURCE_ROCK = new ResourceType(7, LanguageStrings.RESOURCE_ROCK, false, false,
        loadIcon("icons/resources/mineral.png"),
        loadIcon("sprites/sources/mineral_small.png"),
        loadIcon("sprites/sources/mineral_medium.png"),
        loadIcon("sprites/sources/mineral_big.png"),
        loadIcon("sprites/sources/mineral_huge.png"));
    static { resources.add(RESOURCE_ROCK); }

    // --- Times of Day ---
    public static final TimeOfDay TIME_DAY = new TimeOfDay(1, LanguageStrings.TIME_DAY, 1.05f, AssetStyles.OVERLAY_DAY,
        loadIcon("icons/times/day.png"));
    static { timesOfDay.add(TIME_DAY); }
    public static final TimeOfDay TIME_DUSK = new TimeOfDay(2, LanguageStrings.TIME_DUSK, 0.95f, AssetStyles.OVERLAY_DUSK,
        loadIcon("icons/times/dusk.png"));
    static { timesOfDay.add(TIME_DUSK); }
    public static final TimeOfDay TIME_NIGHT = new TimeOfDay(3, LanguageStrings.TIME_NIGHT, 0.85f, AssetStyles.OVERLAY_NIGHT,
        loadIcon("icons/times/night.png"));
    static { timesOfDay.add(TIME_NIGHT); }
    public static final TimeOfDay TIME_DAWN = new TimeOfDay(4, LanguageStrings.TIME_DAWN, 0.90f, AssetStyles.OVERLAY_DAWN,
        loadIcon("icons/times/dawn.png"));
    static { timesOfDay.add(TIME_DAWN); }
    public static final TimeOfDay TIME_SOLAR_ECLIPSE = new TimeOfDay(5, LanguageStrings.TIME_SOLAR_ECLIPSE, 0.7f, AssetStyles.OVERLAY_SOLAR_ECLIPSE,
        loadIcon("icons/times/solar-eclipse.png"));
    static { timesOfDay.add(TIME_SOLAR_ECLIPSE); }
    public static final TimeOfDay TIME_LUNAR_ECLIPSE = new TimeOfDay(6, LanguageStrings.TIME_LUNAR_ECLIPSE, 0.85f, AssetStyles.OVERLAY_LUNAR_ECLIPSE,
        loadIcon("icons/times/lunar-eclipse.png"));
    static { timesOfDay.add(TIME_LUNAR_ECLIPSE); }

    // --- Moon Phases ---
    public static final MoonPhase PHASE_NEW_MOON = new MoonPhase(1, LanguageStrings.MOON_NEW, 1f, 
        loadIcon("icons/moon/new-moon.png"));
    static { moonPhases.add(PHASE_NEW_MOON); }
    public static final MoonPhase PHASE_WAXING_CRESCENT = new MoonPhase(2, LanguageStrings.MOON_WAXING_CRESCENT, 3/4f,
        loadIcon("icons/moon/waxing-crescent.png"));
    static { moonPhases.add(PHASE_WAXING_CRESCENT); }
    public static final MoonPhase PHASE_FIRST_QUARTER = new MoonPhase(3, LanguageStrings.MOON_FIRST_QUARTER, 1/2f, 
        loadIcon("icons/moon/first-quarter.png"));
    static { moonPhases.add(PHASE_FIRST_QUARTER); }
    public static final MoonPhase PHASE_WAXING_GIBBOUS = new MoonPhase(4, LanguageStrings.MOON_WAXING_GIBBOUS, 1/4f, 
        loadIcon("icons/moon/waxing-gibbous.png"));
    static { moonPhases.add(PHASE_WAXING_GIBBOUS); }
    public static final MoonPhase PHASE_FULL_MOON = new MoonPhase(5, LanguageStrings.MOON_FULL, 0f, 
        loadIcon("icons/moon/full-moon.png"));
    static { moonPhases.add(PHASE_FULL_MOON); }
    public static final MoonPhase PHASE_WANING_GIBBOUS = new MoonPhase(6, LanguageStrings.MOON_WANING_GIBBOUS, 1/4f, 
        loadIcon("icons/moon/waning-gibbous.png"));
    static { moonPhases.add(PHASE_WANING_GIBBOUS); }
    public static final MoonPhase PHASE_LAST_QUARTER = new MoonPhase(7, LanguageStrings.MOON_LAST_QUARTER, 1/2f, 
        loadIcon("icons/moon/third-quarter.png"));
    static { moonPhases.add(PHASE_LAST_QUARTER); }
    public static final MoonPhase PHASE_WANING_CRESCENT = new MoonPhase(8, LanguageStrings.MOON_WANING_CRESCENT, 3/4f, 
        loadIcon("icons/moon/waning-crescent.png"));
    static { moonPhases.add(PHASE_WANING_CRESCENT); }

    // --- Seasons ---
    public static final Season SEASON_SPRING = new Season(1, LanguageStrings.SEASON_SPRING, 1.0f, 1.0f, 
        loadIcon("icons/seasons/spring.png"));
    static { seasons.add(SEASON_SPRING); }
    public static final Season SEASON_SUMMER = new Season(2, LanguageStrings.SEASON_SUMMER, 1.15f, 0.8f, 
        loadIcon("icons/seasons/summer.png"));
    static { seasons.add(SEASON_SUMMER); }
    public static final Season SEASON_AUTUMN = new Season(3, LanguageStrings.SEASON_AUTUMN, 0.95f, 1.1f, 
        loadIcon("icons/seasons/autumn.png"));
    static { seasons.add(SEASON_AUTUMN); }
    public static final Season SEASON_WINTER = new Season(4, LanguageStrings.SEASON_WINTER, 0.7f, 1.2f, 
        loadIcon("icons/seasons/winter.png"));
    static { seasons.add(SEASON_WINTER); }

    // --- Weather ---
    public static final Weather WEATHER_CLEAR = new Weather(1, LanguageStrings.WEATHER_CLEAR, 0, 1.0f, AssetStyles.OVERLAY_CLEAR,
        loadIcon("icons/weather/clear.png"));
    static { weathers.add(WEATHER_CLEAR); }
    public static final Weather WEATHER_RAIN = new Weather(2, LanguageStrings.WEATHER_RAIN, 1, 0.95f, AssetStyles.OVERLAY_RAIN,
        loadIcon("icons/weather/rain.png"));
    static { weathers.add(WEATHER_RAIN); }
    public static final Weather WEATHER_SNOW = new Weather(3, LanguageStrings.WEATHER_SNOW, 1, 0.8f, AssetStyles.OVERLAY_SNOW,
        loadIcon("icons/weather/snow.png"));
    static { weathers.add(WEATHER_SNOW); }
    public static final Weather WEATHER_HEAVY_RAIN = new Weather(4, LanguageStrings.WEATHER_HEAVY_RAIN, 2, 0.9f, AssetStyles.OVERLAY_HEAVY_RAIN,
        loadIcon("icons/weather/heavy-rain.png"));
    static { weathers.add(WEATHER_HEAVY_RAIN); }
    public static final Weather WEATHER_THUNDER = new Weather(5, LanguageStrings.WEATHER_THUNDER, 2, 0.9f, AssetStyles.OVERLAY_THUNDER,
        loadIcon("icons/weather/thunder.png"));
    static { weathers.add(WEATHER_THUNDER); }
    public static final Weather WEATHER_HEAVY_SNOW = new Weather(6, LanguageStrings.WEATHER_HEAVY_SNOW, 2, 0.7f, AssetStyles.OVERLAY_HEAVY_SNOW,
        loadIcon("icons/weather/heavy-snow.png"));
    static { weathers.add(WEATHER_HEAVY_SNOW); }
    public static final Weather WEATHER_WIND = new Weather(7, LanguageStrings.WEATHER_WIND, -1, 0.95f, AssetStyles.OVERLAY_WIND,
        loadIcon("icons/weather/heavy-wind.png"));
        static { weathers.add(WEATHER_WIND); }
    public static final Weather WEATHER_HEAT = new Weather(8, LanguageStrings.WEATHER_HEAT, -2, 1.2f, AssetStyles.OVERLAY_HEAT,
        loadIcon("icons/weather/heat-wave.png"));
    static { weathers.add(WEATHER_HEAT); }
    public static final Weather WEATHER_FOG = new Weather(9, LanguageStrings.WEATHER_FOG, 0, 0.9f, AssetStyles.OVERLAY_FOG,
        loadIcon("icons/weather/fog.png"));
    static { weathers.add(WEATHER_FOG); }
    public static final Weather WEATHER_FROG = new Weather(10, LanguageStrings.WEATHER_FROG, 3, 1.0f, AssetStyles.OVERLAY_FROG,
        loadIcon("icons/weather/frog-rain.png"));
    static { weathers.add(WEATHER_FROG); }
    public static final Weather WEATHER_BLOOD = new Weather(11, LanguageStrings.WEATHER_BLOOD, 2, 0.85f, AssetStyles.OVERLAY_BLOOD,
        loadIcon("icons/weather/blood-rain.png"));
    static { weathers.add(WEATHER_BLOOD); }
    public static final Weather WEATHER_SAND_STORM = new Weather(12, LanguageStrings.WEATHER_SAND_STORM, -1, 0.8f, AssetStyles.OVERLAY_SANDSTORM,
        loadIcon("icons/weather/sand-storm.png"));
    static { weathers.add(WEATHER_SAND_STORM); }
    public static final Weather WEATHER_PYROCLASTIC_FOG = new Weather(13, LanguageStrings.WEATHER_PYROCLASTIC_FOG, 0, 0.5f, AssetStyles.OVERLAY_PYROCLASTIC,
        loadIcon("icons/weather/pyro-fog.png"));
    static { weathers.add(WEATHER_PYROCLASTIC_FOG); }
    public static final Weather WEATHER_ACID_RAIN = new Weather(14, LanguageStrings.WEATHER_ACID_RAIN, 1, 0.75f, AssetStyles.OVERLAY_ACID_RAIN,
        loadIcon("icons/weather/acid-rain.png"));
    static { weathers.add(WEATHER_ACID_RAIN); }

    // --- Ant Status ---
    public static final AntStatus STATUS_ALIVE = new AntStatus(1, LanguageStrings.STATUS_ALIVE, 
        loadIcon("icons/status/alive.png"));
    static { antStatuses.add(STATUS_ALIVE); }
    public static final AntStatus STATUS_DEAD = new AntStatus(2, LanguageStrings.STATUS_DEAD, 
        loadIcon("icons/status/dead.png"));
    static { antStatuses.add(STATUS_DEAD); }
    public static final AntStatus STATUS_ZOMBIFIED = new AntStatus(3, LanguageStrings.STATUS_ZOMBIFIED, 
        loadIcon("icons/status/zombified.png"));
    static { antStatuses.add(STATUS_ZOMBIFIED); }

    // --- Move Status ---
    public static final MoveStatus MOVE_STATIC = new MoveStatus(1, LanguageStrings.MOVE_STATIC, 0, null);
    static { moveStatuses.add(MOVE_STATIC); }
    public static final MoveStatus MOVE_WANDER = new MoveStatus(2, LanguageStrings.MOVE_WANDER, 1/2f, null);
    static { moveStatuses.add(MOVE_WANDER); }
    public static final MoveStatus MOVE_MARCH = new MoveStatus(3, LanguageStrings.MOVE_MARCH, 1f, null);
    static { moveStatuses.add(MOVE_MARCH); }
    public static final MoveStatus MOVE_SPEED = new MoveStatus(4, LanguageStrings.MOVE_SPEED, 3/2f, null);
    static { moveStatuses.add(MOVE_SPEED); }
    public static final MoveStatus MOVE_FLY = new MoveStatus(5, LanguageStrings.MOVE_FLY, 3f, null);
    static { moveStatuses.add(MOVE_FLY); }
    public static final MoveStatus MOVE_PATROL = new MoveStatus(6, LanguageStrings.MOVE_PATROL, 1f, null);
    static { moveStatuses.add(MOVE_PATROL); }

    // --- Bug Types ---
    public static final BugType TYPE_ANT = new BugType(1, LanguageStrings.BUG_ANT, LanguageStrings.BUG_ANT_SCIENTIFIC, 1, 1, 1, 1, 1, 1,
        loadIcon("icons/ants/omni/worker.png"), loadIcon("sprites/ants/omni/worker.png"));
    static { bugTypes.add(TYPE_ANT); }
    public static final BugType TYPE_APHID = new BugType(2, LanguageStrings.BUG_APHID, LanguageStrings.BUG_APHID_SCIENTIFIC, 1, 1, 0, 0, 5, 0.5f,
        loadIcon("icons/bugs/aphid.png") , loadIcon("sprites/bugs/aphid.png"));
    static { bugTypes.add(TYPE_APHID); }
    public static final BugType TYPE_PARASITE = new BugType(3, LanguageStrings.BUG_PARASITE, LanguageStrings.BUG_PARASITE_SCIENTIFIC, 1, 1, 0, 0, 1, 1,
        loadIcon("icons/bugs/parasite.png") , loadIcon("sprites/bugs/parasite.png"));
    static { bugTypes.add(TYPE_PARASITE); }
    public static final BugType TYPE_SOIL_MITE = new BugType(4, LanguageStrings.BUG_SOIL_MITE, LanguageStrings.BUG_SOIL_MITE_SCIENTIFIC,
            1, 0, 0, 0, 2, 0.4f, loadIcon("icons/bugs/soilMite.png"), loadIcon("sprites/bugs/soilMite.png"));
    static { bugTypes.add(TYPE_SOIL_MITE); }
    public static final BugType TYPE_DERMESTID = new BugType(5, LanguageStrings.BUG_DERMESTID, LanguageStrings.BUG_DERMESTID_SCIENTIFIC,
            1, 0, 0, 0, 3, 0.35f, loadIcon("icons/bugs/dermestid.png"), loadIcon("sprites/bugs/dermestid.png"));
    static { bugTypes.add(TYPE_DERMESTID); }
    public static final BugType TYPE_PARASITIC_MITE = new BugType(6, LanguageStrings.BUG_PARASITIC_MITE,
            LanguageStrings.BUG_PARASITIC_MITE_SCIENTIFIC, 1, 0, 0, 0, 1, 0.25f,
            loadIcon("icons/bugs/parasiticMite.png"), loadIcon("sprites/bugs/parasiticMite.png"));
    static { bugTypes.add(TYPE_PARASITIC_MITE); }

    static {
        for (Biome biome : biomes) {
            biome.setNativeBugs(buildNativeBugsForBiome(biome));
        }
    }

    private static List<BugType> buildNativeBugsForBiome(Biome biome) {
        List<BugType> natives = new ArrayList<>();
        if (!biome.isDry()) {
            natives.add(TYPE_APHID);
        }
        natives.add(TYPE_SOIL_MITE);
        if (!biome.isCold()) {
            natives.add(TYPE_DERMESTID);
        }
        return List.copyOf(natives);
    }

    // --- Ant Types ---
    public static final AntType TYPE_EGG = new AntType(1, LanguageStrings.TYPE_EGG, 1f, 0f, 0f, 0f, 0f, 0f, 0f,
        loadIcon("icons/ants/egg.png"), "egg.png");
    static { antTypes.add(TYPE_EGG); }    
    public static final AntType TYPE_LARVA = new AntType(2, LanguageStrings.TYPE_LARVA, 1f, 1/2f, 1/2f, 1f, 1f, 1/2f, 1/2f,
        loadIcon("icons/ants/larva.png"), "larva.png");
    static { antTypes.add(TYPE_LARVA); }
    public static final AntType TYPE_PUPA = new AntType(3, LanguageStrings.TYPE_PUPA, 1f, 0f, 1f, 0f, 0f, 1/2f, 0f,
        loadIcon("icons/ants/pupa.png"), "pupa.png");
    static { antTypes.add(TYPE_PUPA); }
    public static final AntType TYPE_WORKER = new AntType(4, LanguageStrings.TYPE_WORKER, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/omni/worker.png"), "worker.png");
    static { antTypes.add(TYPE_WORKER); }
    public static final AntType TYPE_SOLDIER = new AntType(5, LanguageStrings.TYPE_SOLDIER, 3f, 3f, 3f, 2f, 3f, 5f, 3f, 
        loadIcon("icons/ants/omni/soldier.png"), "soldier.png");
    static { antTypes.add(TYPE_SOLDIER); }
    public static final AntType TYPE_MAJOR = new AntType(6, LanguageStrings.TYPE_MAJOR, 10f, 15f, 20f, 5f, 2f, 50f, 2f, 
        loadIcon("icons/ants/omni/major.png"), "major.png");
    static { antTypes.add(TYPE_MAJOR); }
    public static final AntType TYPE_DRONE = new AntType(7, LanguageStrings.TYPE_DRONE, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/omni/drone.png"), "drone.png");
    static { antTypes.add(TYPE_DRONE); }
    public static final AntType TYPE_PRINCESS = new AntType(8, LanguageStrings.TYPE_PRINCESS, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/omni/princess.png"), "princess.png");
    static { antTypes.add(TYPE_PRINCESS); }
    public static final AntType TYPE_QUEEN = new AntType(9, LanguageStrings.TYPE_QUEEN, 50f, 2f, 50f, 10f, 1/2f, 50f, 1/4f, 
        loadIcon("icons/ants/omni/queen.png"), "queen.png");
    static { antTypes.add(TYPE_QUEEN); }
    public static final AntType TYPE_DEAD = new AntType(10, LanguageStrings.TYPE_DEAD, 0, 0, 0, 0, 0, 0, 0,
        loadIcon("icons/ants/dead.png"), "dead.png");
    static { antTypes.add(TYPE_DEAD); }
    public static final AntType TYPE_ZOMBIE = new AntType(11, LanguageStrings.TYPE_ZOMBIE,  1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/zombie.png"), "zombie.png");
    static { antTypes.add(TYPE_ZOMBIE); }

    // --- Ant Subtypes ---

    // --- Ant Roles ---
    public static final AntRole ROLE_FORAGER = new AntRole(1, TYPE_WORKER, LanguageStrings.ROLE_FORAGER, null);
    static { antRoles.add(ROLE_FORAGER); }
    public static final AntRole ROLE_NURSE = new AntRole(2, TYPE_WORKER, LanguageStrings.ROLE_NURSE, null);
    static { antRoles.add(ROLE_NURSE); }
    public static final AntRole ROLE_BUILDER = new AntRole(3, TYPE_WORKER, LanguageStrings.ROLE_BUILDER, null);
    static { antRoles.add(ROLE_BUILDER); }
    public static final AntRole ROLE_SCOUT = new AntRole(4, TYPE_WORKER, LanguageStrings.ROLE_SCOUT, null);
    static { antRoles.add(ROLE_SCOUT); }
    public static final AntRole ROLE_FARMER = new AntRole(5, TYPE_WORKER, LanguageStrings.ROLE_FARMER, null);
    static { antRoles.add(ROLE_FARMER); }
    public static final AntRole ROLE_RANCHER = new AntRole(6, TYPE_WORKER, LanguageStrings.ROLE_RANCHER, null);
    static { antRoles.add(ROLE_RANCHER); }
    public static final AntRole ROLE_GRAVER = new AntRole(7, TYPE_WORKER, LanguageStrings.ROLE_GRAVER, null);
    static { antRoles.add(ROLE_GRAVER); }
    public static final AntRole ROLE_MINER = new AntRole(8, TYPE_WORKER, LanguageStrings.ROLE_MINER, null);
    static { antRoles.add(ROLE_MINER); }
    public static final AntRole ROLE_COURIER = new AntRole(9, TYPE_WORKER, LanguageStrings.ROLE_COURIER, null);
    static { antRoles.add(ROLE_COURIER); }
    public static final AntRole ROLE_POTTER = new AntRole(10, TYPE_WORKER, LanguageStrings.ROLE_POTTER, null);
    static { antRoles.add(ROLE_POTTER); }
    public static final AntRole ROLE_GUARD = new AntRole(11, TYPE_SOLDIER, LanguageStrings.ROLE_GUARD, null);
    static { antRoles.add(ROLE_GUARD); }
    public static final AntRole ROLE_WARRIOR = new AntRole(12, TYPE_SOLDIER, LanguageStrings.ROLE_WARRIOR, null);
    static { antRoles.add(ROLE_WARRIOR); }
    public static final AntRole ROLE_DEFENDER = new AntRole(13, TYPE_SOLDIER, LanguageStrings.ROLE_DEFENDER, null);
    static { antRoles.add(ROLE_DEFENDER); }
    public static final AntRole ROLE_POLICE = new AntRole(14, TYPE_SOLDIER, LanguageStrings.ROLE_POLICE, null);
    static { antRoles.add(ROLE_POLICE); }
    public static final AntRole ROLE_BOMBER = new AntRole(15, TYPE_SOLDIER, LanguageStrings.ROLE_BOMBER, null);
    static { antRoles.add(ROLE_BOMBER); }
    public static final AntRole ROLE_HUNTER = new AntRole(16, TYPE_SOLDIER, LanguageStrings.ROLE_HUNTER, null);
    static { antRoles.add(ROLE_HUNTER); }
    public static final AntRole ROLE_BRUTE = new AntRole(17, TYPE_MAJOR, LanguageStrings.ROLE_BRUTE, null);
    static { antRoles.add(ROLE_BRUTE); }
    public static final AntRole ROLE_CARRIER = new AntRole(18, TYPE_MAJOR, LanguageStrings.ROLE_CARRIER, null);
    static { antRoles.add(ROLE_CARRIER); }
    public static final AntRole ROLE_ARTILLERY = new AntRole(19, TYPE_MAJOR, LanguageStrings.ROLE_ARTILLERY, null);
    static { antRoles.add(ROLE_ARTILLERY); }
    public static final AntRole ROLE_SIEGE = new AntRole(20, TYPE_MAJOR, LanguageStrings.ROLE_SIEGE, null);
    static { antRoles.add(ROLE_SIEGE); }
    public static final AntRole ROLE_BORER = new AntRole(21, TYPE_MAJOR, LanguageStrings.ROLE_BORER, null);
    static { antRoles.add(ROLE_BORER); }
    public static final AntRole ROLE_DRONE = new AntRole(22, TYPE_DRONE, LanguageStrings.ROLE_DRONE, null);
    static { antRoles.add(ROLE_DRONE); }
    public static final AntRole ROLE_BREEDER = new AntRole(23, TYPE_PRINCESS, LanguageStrings.ROLE_BREEDER, null);
    static { antRoles.add(ROLE_BREEDER); }
    public static final AntRole ROLE_DIPLOMAT = new AntRole(24, TYPE_PRINCESS, LanguageStrings.ROLE_DIPLOMAT, null);
    static { antRoles.add(ROLE_DIPLOMAT); }
    public static final AntRole ROLE_LAYER = new AntRole(25, TYPE_QUEEN, LanguageStrings.ROLE_LAYER, null);
    static { antRoles.add(ROLE_LAYER); }
    public static final AntRole ROLE_RESEARCHER = new AntRole(26, TYPE_QUEEN, LanguageStrings.ROLE_RESEARCHER, null);
    static { antRoles.add(ROLE_RESEARCHER); }
    public static final AntRole ROLE_MILITIA = new AntRole(27, TYPE_WORKER, LanguageStrings.ROLE_MILITIA, null);
    static { antRoles.add(ROLE_MILITIA); }
    public static final AntRole ROLE_CATCHER = new AntRole(28, TYPE_SOLDIER, LanguageStrings.ROLE_CATCHER, null);
    static { antRoles.add(ROLE_CATCHER); }
    public static final AntRole ROLE_CRANE = new AntRole(29, TYPE_MAJOR, LanguageStrings.ROLE_CRANE, null);
    static { antRoles.add(ROLE_CRANE); }
    public static final AntRole ROLE_TRANSPORT = new AntRole(30, TYPE_MAJOR, LanguageStrings.ROLE_TRANSPORT, null);
    static { antRoles.add(ROLE_TRANSPORT); }
    public static final AntRole ROLE_ASSISTANT = new AntRole(31, TYPE_PRINCESS, LanguageStrings.ROLE_ASSISTANT, null);
    static { antRoles.add(ROLE_ASSISTANT); }
    public static final AntRole ROLE_ESCORT = new AntRole(32, TYPE_SOLDIER, LanguageStrings.ROLE_ESCORT, null);
    static { antRoles.add(ROLE_ESCORT); }
    public static final AntRole ROLE_ENGINEER = new AntRole(33, TYPE_WORKER, LanguageStrings.ROLE_ENGINEER, null);
    static { antRoles.add(ROLE_ENGINEER); }
    public static final AntRole ROLE_SKYTRANS = new AntRole(34, TYPE_PRINCESS, LanguageStrings.ROLE_SKYTRANS, null);
    static { antRoles.add(ROLE_SKYTRANS); }

    // --- Colony Ranks ---
    public static final ColonyRank RANK_ANT = new ColonyRank(1, LanguageStrings.RANK_ANT, 1l, 
        loadIcon("icons/ranks/ant.png"));
    static { colonyRanks.add(RANK_ANT); }
    public static final ColonyRank RANK_COLONY = new ColonyRank(2, LanguageStrings.RANK_COLONY, 10l, 
        loadIcon("icons/ranks/colony.png"));
    static { colonyRanks.add(RANK_COLONY); }
    public static final ColonyRank RANK_COUNTY = new ColonyRank(3, LanguageStrings.RANK_COUNTY, 100l, 
        loadIcon("icons/ranks/county.png"));
    static { colonyRanks.add(RANK_COUNTY); }
    public static final ColonyRank RANK_DUCHY = new ColonyRank(4, LanguageStrings.RANK_DUCHY, 1000l, 
        loadIcon("icons/ranks/duchy.png"));
    static { colonyRanks.add(RANK_DUCHY); }
    public static final ColonyRank RANK_KINGDOM = new ColonyRank(5, LanguageStrings.RANK_KINGDOM, 10000l, 
        loadIcon("icons/ranks/kingdom.png"));
    static { colonyRanks.add(RANK_KINGDOM); }
    public static final ColonyRank RANK_EMPIRE = new ColonyRank(6, LanguageStrings.RANK_EMPIRE, 100000l, 
        loadIcon("icons/ranks/empire.png"));
    static { colonyRanks.add(RANK_EMPIRE); }
    public static final ColonyRank RANK_SUPER = new ColonyRank(7, LanguageStrings.RANK_SUPER, 1000000l, 
        loadIcon("icons/ranks/super.png"));
    static { colonyRanks.add(RANK_SUPER); }
    public static final ColonyRank RANK_ULTRA = new ColonyRank(8, LanguageStrings.RANK_ULTRA, 10000000l, 
        loadIcon("icons/ranks/ultra.png"));
    static { colonyRanks.add(RANK_ULTRA); }
    public static final ColonyRank RANK_HYPER = new ColonyRank(9, LanguageStrings.RANK_HYPER, 100000000l, 
        loadIcon("icons/ranks/hyper.png"));
    static { colonyRanks.add(RANK_HYPER); }
    public static final ColonyRank RANK_MEGA = new ColonyRank(10, LanguageStrings.RANK_MEGA, 1000000000l, 
        loadIcon("icons/ranks/mega.png"));
    static { colonyRanks.add(RANK_MEGA); }
    public static final ColonyRank RANK_ULTIMATE = new ColonyRank(11, LanguageStrings.RANK_ULTIMATE, 10000000000l, 
        loadIcon("icons/ranks/ultimate.png"));
    static { colonyRanks.add(RANK_ULTIMATE); }
    public static final ColonyRank RANK_SUPREME = new ColonyRank(12, LanguageStrings.RANK_SUPREME, 100000000000l, 
        loadIcon("icons/ranks/supreme.png"));
    static { colonyRanks.add(RANK_SUPREME); }
    public static final ColonyRank RANK_GIGA = new ColonyRank(13, LanguageStrings.RANK_GIGA, 1000000000000l, 
        loadIcon("icons/ranks/giga.png"));
    static { colonyRanks.add(RANK_GIGA); }
    
    // --- Species ---
    public static final Species SPECIES_OMNI = new Species(1, LanguageStrings.SPECIES_OMNI, LanguageStrings.SPECIES_OMNI_SCIENTIFIC,  "omni/", null, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, GameUnlocks.ROLE_FORAGER, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY), loadIcon("icons/species/omni.png"));
    static { species.add(SPECIES_OMNI); }
    
    public static final Species SPECIES_LEAF = new Species(2, LanguageStrings.SPECIES_LEAF, LanguageStrings.SPECIES_LEAF_SCIENTIFIC, "leaf/", GameUnlocks.ASSIMILATION_LEAFCUTTER, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, GameUnlocks.ROLE_FORAGER, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY,
            GameUnlocks.ASSIMILATED_FARMING), loadIcon("icons/species/leaf.png"));
    static { species.add(SPECIES_LEAF); }
    
    public static final Species SPECIES_PHARAOH = new Species(3, LanguageStrings.SPECIES_PHARAOH, LanguageStrings.SPECIES_PHARAOH_SCIENTIFIC, "pharaoh/", GameUnlocks.ASSIMILATION_PHARAOH, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, LanguageStrings.get(LanguageStrings.SPECIES_PHARAOH_SCIENTIFIC).length() > 0 ? GameUnlocks.ROLE_FORAGER : null, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY,
            GameUnlocks.ASSIMILATED_MULTIQUEEN) ,loadIcon("icons/species/pharaoh.png"));
    static { species.add(SPECIES_PHARAOH); }
    
    public static final Species SPECIES_MARAUDER = new Species(4, LanguageStrings.SPECIES_MARAUDER, LanguageStrings.SPECIES_MARAUDER_SCIENTIFIC, "marauder/", GameUnlocks.ASSIMILATION_MARAUDER, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, GameUnlocks.ROLE_FORAGER, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY,
            GameUnlocks.TYPE_MAJOR) ,loadIcon("icons/species/marauder.png"));
    static { species.add(SPECIES_MARAUDER); }

    // --- Trade Methods ---
    public static final TradeMethod METHOD_LAND = new TradeMethod(1, LanguageStrings.METHOD_LAND, 1.0f, 1.0f, 0.35f, null);
    static { tradeMethods.add(METHOD_LAND); }
    public static final TradeMethod METHOD_AIR = new TradeMethod(2, LanguageStrings.METHOD_AIR, 5.0f, 0.5f, 0.25f, null);
    static { tradeMethods.add(METHOD_AIR); }
    public static final TradeMethod METHOD_SEA = new TradeMethod(3, LanguageStrings.METHOD_SEA, 3.0f, 5.0f, 0.15f, null);
    static { tradeMethods.add(METHOD_SEA); }
    public static final TradeMethod METHOD_TUNNEL = new TradeMethod(4, LanguageStrings.METHOD_TUNNEL, 2.0f, 2.0f, 0.05f, null);
    static { tradeMethods.add(METHOD_TUNNEL); }
    
    // --- Construction Costs ---
    public static final double TUNNEL_WORK_REQUIRED = 5000000.0;
    
    // --- Getters ---
    public static List<Biome> getBiomes() { return Collections.unmodifiableList(biomes); }

    public static List<ResourceType> getResources() { return Collections.unmodifiableList(resources); }

    public static List<TimeOfDay> getTimesOfDay() { return Collections.unmodifiableList(timesOfDay); }  

    public static List<MoonPhase> getMoonPhases() { return Collections.unmodifiableList(moonPhases); }

    public static List<Season> getSeasons() { return Collections.unmodifiableList(seasons); }

    public static List<Weather> getWeathers() { return Collections.unmodifiableList(weathers); }

    public static List<AntStatus> getAntStatuses() { return Collections.unmodifiableList(antStatuses); }

    public static List<MoveStatus> getMoveStatuses() { return Collections.unmodifiableList(moveStatuses); }

    public static List<AntType> getAntTypes() { return Collections.unmodifiableList(antTypes); }

    public static List<AntSubType> getAntSubTypes() {  return Collections.unmodifiableList(antSubTypes); }

    public static List<AntRole> getAntRoles() { return Collections.unmodifiableList(antRoles); }

    public static AntRole getAntRoleById(int id) {
        for (AntRole r : antRoles) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public static List<ColonyRank> getColonyRanks() { return Collections.unmodifiableList(colonyRanks); }

    public static List<Species> getSpecies() { return Collections.unmodifiableList(species); }

    public static List<TradeMethod> getTradeMethods() { return Collections.unmodifiableList(tradeMethods); }

    public static List<Humidity> getHumidity() { return Collections.unmodifiableList(humidity); }

    public static List<Temperature> getTemperature() { return Collections.unmodifiableList(temperature); }

    public static List<BugType> getBugTypes() { return Collections.unmodifiableList(bugTypes); }

    public static List<ImageIcon> getMisc() { return Collections.unmodifiableList(misc); }
}
