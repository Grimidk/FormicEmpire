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
    public static final ImageIcon ICON_RESEARCH = loadIcon("icons/misc/research.png");
    static { misc.add(ICON_RESEARCH); }

    // --- Temperatures ---
    public static final Temperature TEMP_FREEZING = new Temperature(1, "Freezing", 5,
        loadIcon("icons/temp/freezing.png"));
    static { temperature.add(TEMP_FREEZING); }
    public static final Temperature TEMP_COLD = new Temperature(2, "Cold", 15,
        loadIcon("icons/temp/cold.png"));
    static { temperature.add(TEMP_COLD); }
    public static final Temperature TEMP_CHILLY = new Temperature(3, "Chilly", 20,
        loadIcon("icons/temp/chilly.png"));
    static { temperature.add(TEMP_CHILLY); }
    public static final Temperature TEMP_GOOD = new Temperature(4, "Good", 28,
        loadIcon("icons/temp/good.png"));
    static { temperature.add(TEMP_GOOD); }
    public static final Temperature TEMP_WARM = new Temperature(5, "Warm", 35,
        loadIcon("icons/temp/warm.png"));
    static { temperature.add(TEMP_WARM); }
    public static final Temperature TEMP_HOT = new Temperature(6, "Hot", 45,
        loadIcon("icons/temp/hot.png"));
    static { temperature.add(TEMP_HOT); }    
    public static final Temperature TEMP_BURNING = new Temperature(7, "Burning", 100,
        loadIcon("icons/temp/burning.png"));
    static { temperature.add(TEMP_BURNING); }    

    // --- Humidity
    public static final Humidity HUMID_0 = new Humidity(1, "Arid", 0,
        loadIcon("icons/humid/humid0.png"));
    static { humidity.add(HUMID_0); }    
    public static final Humidity HUMID_1 = new Humidity(2, "Dry", 1, 
        loadIcon("icons/humid/humid1.png"));
    static { humidity.add(HUMID_1); }
    public static final Humidity HUMID_2 = new Humidity(3, "Normal", 2, 
        loadIcon("icons/humid/humid2.png"));
    static { humidity.add(HUMID_2); }
    public static final Humidity HUMID_3 = new Humidity(4, "Humid", 3, 
        loadIcon("icons/humid/humid3.png"));
    static { humidity.add(HUMID_3); }
    public static final Humidity HUMID_4 = new Humidity(5, "Moist", 4, 
        loadIcon("icons/humid/humid4.png"));
    static { humidity.add(HUMID_4); }
    public static final Humidity HUMID_5 = new Humidity(6, "Saturated", 5, 
        loadIcon("icons/humid/humid5.png"));
    static { humidity.add(HUMID_5); }

    // --- Biomes ---
    public static final Biome BIOME_PLAINS = new Biome(1, "Plains", 25, 2, 1f, 1f, 0f,
        loadIcon("icons/biomes/plains.png"));
    static { biomes.add(BIOME_PLAINS); }
    public static final Biome BIOME_FOREST = new Biome(2, "Forest", 22, 3, 2f, 2f, 0.1f,
        loadIcon("icons/biomes/forest.png"));
    static { biomes.add(BIOME_FOREST); }
    public static final Biome BIOME_JUNGLE = new Biome(3, "Jungle", 30, 4, 2.5f, 2.5f, 0.2f,
        loadIcon("icons/biomes/jungle.png"));
    static { biomes.add(BIOME_JUNGLE); }
    public static final Biome BIOME_SWAMP = new Biome(4, "Swamp", 26, 5, 1.5f, 1.5f, 0f,
        loadIcon("icons/biomes/swamp.png"));
    static { biomes.add(BIOME_SWAMP); }
    public static final Biome BIOME_URBAN = new Biome(5, "Urban", 28, 1, 0.5f, 0.5f, 0.5f,
        loadIcon("icons/biomes/urban.png"));
    static { biomes.add(BIOME_URBAN); }
    public static final Biome BIOME_TUNDRA = new Biome(6, "Tundra", 5, 2, 0.2f, 0.2f, 0.3f,
        loadIcon("icons/biomes/tundra.png"));
    static { biomes.add(BIOME_TUNDRA); }
    public static final Biome BIOME_TAIGA = new Biome(7, "Taiga", 12, 1, 0.4f, 0.3f, 0.5f,
        loadIcon("icons/biomes/taiga.png"));
    static { biomes.add(BIOME_TAIGA); }
    public static final Biome BIOME_DESERT = new Biome(8, "Desert", 45, 0, 0.05f, 0.02f, 1f,
        loadIcon("icons/biomes/dessert.png"));
    static { biomes.add(BIOME_DESERT); }
    public static final Biome BIOME_MOUNTAIN = new Biome(9, "Mountain", 12, 1, 0.1f, 0.3f, 2f,
        loadIcon("icons/biomes/mountain.png"));
    static { biomes.add(BIOME_MOUNTAIN); }
    public static final Biome BIOME_VOLCANIC = new Biome(10, "Volcanic", 60, 0, 0.01f, 0.01f, 5f,
        loadIcon("icons/biomes/volcanic.png"));
    static { biomes.add(BIOME_VOLCANIC); }
    public static final Biome BIOME_LAKE = new Biome(11, "Lake", 25, 5, 0.5f, 0.5f, 0f,
        loadIcon("icons/biomes/lake.png"));
    static { biomes.add(BIOME_LAKE); }
    public static final Biome BIOME_OCEAN = new Biome(12, "Ocean", 20, 5, 0.2f, 0.2f, 0f,
        loadIcon("icons/biomes/ocean.png"));
    static { biomes.add(BIOME_OCEAN); }

    // --- Resources ---
    public static final ResourceType RESOURCE_PLANT = new ResourceType(1, "Plant Matter", true, false, 
        loadIcon("icons/resources/plant.png"));
    static { resources.add(RESOURCE_PLANT); }
    public static final ResourceType RESOURCE_FUNGI = new ResourceType(2, "Fungi Matter", true, false, 
        loadIcon("icons/resources/mushroom.png"));
    static { resources.add(RESOURCE_FUNGI); }
    public static final ResourceType RESOURCE_MEAT = new ResourceType(3, "Animal Matter", true, false, 
        loadIcon("icons/resources/protein.png"));
    static { resources.add(RESOURCE_MEAT); }
    public static final ResourceType RESOURCE_WATER = new ResourceType(4, "Water", true, true, 
        loadIcon("icons/resources/water.png"));
    static { resources.add(RESOURCE_WATER); }
    public static final ResourceType RESOURCE_SYRUP = new ResourceType(5, "Syrup", true, true, 
        loadIcon("icons/resources/syrup.png"));
    static { resources.add(RESOURCE_SYRUP); }
    public static final ResourceType RESOURCE_RESIN = new ResourceType(6, "Resin", false, true, 
        loadIcon("icons/resources/resin.png"));
    static { resources.add(RESOURCE_RESIN); }
    public static final ResourceType RESOURCE_ROCK = new ResourceType(7, "Mineral", false, false, 
        loadIcon("icons/resources/mineral.png"));
    static { resources.add(RESOURCE_ROCK); }

    // --- Times of Day ---
    public static final TimeOfDay TIME_DAY = new TimeOfDay(1, "Daytime", 1.05f,
        loadIcon("icons/times/day.png"));
    static { timesOfDay.add(TIME_DAY); }
    public static final TimeOfDay TIME_DUSK = new TimeOfDay(2, "Dusk", 0.95f,
        loadIcon("icons/times/dusk.png"));
    static { timesOfDay.add(TIME_DUSK); }
    public static final TimeOfDay TIME_NIGHT = new TimeOfDay(3, "Nightime", 0.85f,
        loadIcon("icons/times/night.png"));
    static { timesOfDay.add(TIME_NIGHT); }
    public static final TimeOfDay TIME_DAWN = new TimeOfDay(4, "Dawn", 0.90f,
        loadIcon("icons/times/dawn.png"));
    static { timesOfDay.add(TIME_DAWN); }
    public static final TimeOfDay TIME_SOLAR_ECLIPSE = new TimeOfDay(5, "Solar Eclipse", 0.7f,
        loadIcon("icons/times/solar-eclipse.png"));
    static { timesOfDay.add(TIME_SOLAR_ECLIPSE); }
    public static final TimeOfDay TIME_LUNAR_ECLIPSE = new TimeOfDay(6, "Lunar Eclipse", 0.85f,
        loadIcon("icons/times/lunar-eclipse.png"));
    static { timesOfDay.add(TIME_LUNAR_ECLIPSE); }

    // --- Moon Phases ---
    public static final MoonPhase PHASE_NEW_MOON = new MoonPhase(1, "New Moon", 1f, 
        loadIcon("icons/moon/new-moon.png"));
    static { moonPhases.add(PHASE_NEW_MOON); }
    public static final MoonPhase PHASE_WAXING_CRESCENT = new MoonPhase(2, "Waxing Crescent", 3/4f,
        loadIcon("icons/moon/waxing-crescent.png"));
    static { moonPhases.add(PHASE_WAXING_CRESCENT); }
    public static final MoonPhase PHASE_FIRST_QUARTER = new MoonPhase(3, "First Quarter", 1/2f, 
        loadIcon("icons/moon/first-quarter.png"));
    static { moonPhases.add(PHASE_FIRST_QUARTER); }
    public static final MoonPhase PHASE_WAXING_GIBBOUS = new MoonPhase(4, "Waxing Gibbous", 1/4f, 
        loadIcon("icons/moon/waxing-gibbous.png"));
    static { moonPhases.add(PHASE_WAXING_GIBBOUS); }
    public static final MoonPhase PHASE_FULL_MOON = new MoonPhase(5, "Full Moon", 0f, 
        loadIcon("icons/moon/full-moon.png"));
    static { moonPhases.add(PHASE_FULL_MOON); }
    public static final MoonPhase PHASE_WANING_GIBBOUS = new MoonPhase(6, "Waning Gibbous", 1/4f, 
        loadIcon("icons/moon/waning-gibbous.png"));
    static { moonPhases.add(PHASE_WANING_GIBBOUS); }
    public static final MoonPhase PHASE_LAST_QUARTER = new MoonPhase(7, "Last Quarter", 1/2f, 
        loadIcon("icons/moon/third-quarter.png"));
    static { moonPhases.add(PHASE_LAST_QUARTER); }
    public static final MoonPhase PHASE_WANING_CRESCENT = new MoonPhase(8, "Waning Crescent", 3/4f, 
        loadIcon("icons/moon/waning-crescent.png"));
    static { moonPhases.add(PHASE_WANING_CRESCENT); }

    // --- Seasons ---
    public static final Season SEASON_SPRING = new Season(1, "Spring", 1.0f, 1.0f, 
        loadIcon("icons/seasons/spring.png"));
    static { seasons.add(SEASON_SPRING); }
    public static final Season SEASON_SUMMER = new Season(2, "Summer", 1.15f, 0.8f, 
        loadIcon("icons/seasons/summer.png"));
    static { seasons.add(SEASON_SUMMER); }
    public static final Season SEASON_AUTUMN = new Season(3, "Autumn", 0.95f, 1.1f, 
        loadIcon("icons/seasons/autumn.png"));
    static { seasons.add(SEASON_AUTUMN); }
    public static final Season SEASON_WINTER = new Season(4, "Winter", 0.7f, 1.2f, 
        loadIcon("icons/seasons/winter.png"));
    static { seasons.add(SEASON_WINTER); }

    // --- Weather ---
    public static final Weather WEATHER_CLEAR = new Weather(1, "Clear", 0, 1.0f, 
        loadIcon("icons/weather/clear.png"));
    static { weathers.add(WEATHER_CLEAR); }
    public static final Weather WEATHER_RAIN = new Weather(2, "Rain", 1, 0.95f, 
        loadIcon("icons/weather/rain.png"));
    static { weathers.add(WEATHER_RAIN); }
    public static final Weather WEATHER_SNOW = new Weather(3, "Snow", 1, 0.8f, 
        loadIcon("icons/weather/snow.png"));
    static { weathers.add(WEATHER_SNOW); }
    public static final Weather WEATHER_HEAVY_RAIN = new Weather(4, "Heavy Rain", 2, 0.9f, 
        loadIcon("icons/weather/heavy-rain.png"));
    static { weathers.add(WEATHER_HEAVY_RAIN); }
    public static final Weather WEATHER_THUNDER = new Weather(5, "Thunder Storm", 2, 0.9f, 
        loadIcon("icons/weather/thunder.png"));
    static { weathers.add(WEATHER_THUNDER); }
    public static final Weather WEATHER_HEAVY_SNOW = new Weather(6, "Snow Storm", 2, 0.7f, 
        loadIcon("icons/weather/heavy-snow.png"));
    static { weathers.add(WEATHER_HEAVY_SNOW); }
    public static final Weather WEATHER_WIND = new Weather(7, "Heavy Wind", -1, 0.95f, 
        loadIcon("icons/weather/heavy-wind.png"));
        static { weathers.add(WEATHER_WIND); }
    public static final Weather WEATHER_HEAT = new Weather(8, "Heat Wave", -2, 1.2f, 
        loadIcon("icons/weather/heat-wave.png"));
    static { weathers.add(WEATHER_HEAT); }
    public static final Weather WEATHER_FOG = new Weather(9, "Fog", 0, 0.9f, 
        loadIcon("icons/weather/fog.png"));
    static { weathers.add(WEATHER_FOG); }
    public static final Weather WEATHER_FROG = new Weather(10, "Frog Rain", 3, 1.0f, 
        loadIcon("icons/weather/frog-rain.png"));
    static { weathers.add(WEATHER_FROG); }
    public static final Weather WEATHER_BLOOD = new Weather(11, "Blood Rain", 2, 0.85f, 
        loadIcon("icons/weather/blood-rain.png"));
    static { weathers.add(WEATHER_BLOOD); }

    // --- Ant Status ---
    public static final AntStatus STATUS_ALIVE = new AntStatus(1, "Alive", 
        loadIcon("icons/status/alive.png"));
    static { antStatuses.add(STATUS_ALIVE); }
    public static final AntStatus STATUS_DEAD = new AntStatus(2, "Dead", 
        loadIcon("icons/status/dead.png"));
    static { antStatuses.add(STATUS_DEAD); }
    public static final AntStatus STATUS_ZOMBIFIED = new AntStatus(3, "Zombified", 
        loadIcon("icons/status/zombified.png"));
    static { antStatuses.add(STATUS_ZOMBIFIED); }

    // --- Move Status ---
    public static final MoveStatus MOVE_STATIC = new MoveStatus(1, "Static", 0);
    static { moveStatuses.add(MOVE_STATIC); }
    public static final MoveStatus MOVE_WANDER = new MoveStatus(2, "Wandering", 1/2f);
    static { moveStatuses.add(MOVE_WANDER); }
    public static final MoveStatus MOVE_MARCH = new MoveStatus(3, "Marching", 1f);
    static { moveStatuses.add(MOVE_MARCH); }
    public static final MoveStatus MOVE_SPEED = new MoveStatus(4, "Speed Marching", 3/2f);
    static { moveStatuses.add(MOVE_SPEED); }
    public static final MoveStatus MOVE_FLY = new MoveStatus(5, "Flying", 3f);
    static { moveStatuses.add(MOVE_FLY); }
    public static final MoveStatus MOVE_PATROL = new MoveStatus(6, "Patrolling", 1f);
    static { moveStatuses.add(MOVE_PATROL); }

    // --- Bug Types ---
    public static final BugType TYPE_ANT = new BugType(1, "Ant", 1, 1, 1, 1, 1, 1,
        loadIcon("icons/ants/omni/worker.png"), loadIcon("sprites/ants/omni/worker.png"));
    static { bugTypes.add(TYPE_ANT); }
    public static final BugType TYPE_APHID = new BugType(2, "Aphid", 1, 1, 0, 0, 5, 0.5f,
        loadIcon("icons/bugs/aphid.png") , loadIcon("sprites/bugs/aphid.png"));
    static { bugTypes.add(TYPE_APHID); }
    public static final BugType TYPE_PARASITE = new BugType(3, "Ant Parasite", 1, 1, 0, 0, 1, 1,
        loadIcon("icons/bugs/parasite.png") , loadIcon("sprites/bugs/parasite.png"));
    static { bugTypes.add(TYPE_PARASITE); }

    // --- Ant Types ---
    public static final AntType TYPE_EGG = new AntType(1, "Egg", 1f, 0f, 0f, 0f, 0f, 0f, 0f,
        loadIcon("icons/ants/egg.png"), "egg.png");
    static { antTypes.add(TYPE_EGG); }    
    public static final AntType TYPE_LARVA = new AntType(2, "Larva", 1f, 1/2f, 1/2f, 1f, 1f, 1/2f, 1/2f,
        loadIcon("icons/ants/larva.png"), "larva.png");
    static { antTypes.add(TYPE_LARVA); }
    public static final AntType TYPE_PUPA = new AntType(3, "Pupa", 1f, 0f, 1f, 0f, 0f, 1/2f, 0f,
        loadIcon("icons/ants/pupa.png"), "pupa.png");
    static { antTypes.add(TYPE_PUPA); }
    public static final AntType TYPE_WORKER = new AntType(4, "Worker", 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/omni/worker.png"), "worker.png");
    static { antTypes.add(TYPE_WORKER); }
    public static final AntType TYPE_SOLDIER = new AntType(5, "Soldier", 3f, 3f, 3f, 2f, 3f, 5f, 3f, 
        loadIcon("icons/ants/omni/soldier.png"), "soldier.png");
    static { antTypes.add(TYPE_SOLDIER); }
    public static final AntType TYPE_MAJOR = new AntType(6, "Major", 10f, 15f, 20f, 5f, 2f, 50f, 2f, 
        loadIcon("icons/ants/omni/major.png"), "major.png");
    static { antTypes.add(TYPE_MAJOR); }
    public static final AntType TYPE_DRONE = new AntType(7, "Drone", 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/omni/drone.png"), "drone.png");
    static { antTypes.add(TYPE_DRONE); }
    public static final AntType TYPE_PRINCESS = new AntType(8, "Princess", 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/omni/princess.png"), "princess.png");
    static { antTypes.add(TYPE_PRINCESS); }
    public static final AntType TYPE_QUEEN = new AntType(9, "Queen", 50f, 2f, 50f, 10f, 1/2f, 50f, 1/4f, 
        loadIcon("icons/ants/omni/queen.png"), "queen.png");
    static { antTypes.add(TYPE_QUEEN); }
    public static final AntType TYPE_DEAD = new AntType(10, "Dead", 0, 0, 0, 0, 0, 0, 0,
        loadIcon("icons/ants/dead.png"), "dead.png");
    static { antTypes.add(TYPE_DEAD); }
    public static final AntType TYPE_ZOMBIE = new AntType(11, "Zombie",  1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/zombie.png"), "zombie.png");
    static { antTypes.add(TYPE_ZOMBIE); }

    // --- Ant Subtypes ---

    // --- Ant Roles ---
    public static final AntRole ROLE_FORAGER = new AntRole(1, TYPE_WORKER, "Forager");
    static { antRoles.add(ROLE_FORAGER); }
    public static final AntRole ROLE_NURSE = new AntRole(2, TYPE_WORKER, "Nurse");
    static { antRoles.add(ROLE_NURSE); }
    public static final AntRole ROLE_BUILDER = new AntRole(3, TYPE_WORKER, "Builder");
    static { antRoles.add(ROLE_BUILDER); }
    public static final AntRole ROLE_SCOUT = new AntRole(4, TYPE_WORKER, "Scout");
    static { antRoles.add(ROLE_SCOUT); }
    public static final AntRole ROLE_FARMER = new AntRole(5, TYPE_WORKER, "Farmer");
    static { antRoles.add(ROLE_FARMER); }
    public static final AntRole ROLE_RANCHER = new AntRole(6, TYPE_WORKER, "Rancher");
    static { antRoles.add(ROLE_RANCHER); }
    public static final AntRole ROLE_GRAVER = new AntRole(7, TYPE_WORKER, "Grave-Keeper");
    static { antRoles.add(ROLE_GRAVER); }
    public static final AntRole ROLE_MINER = new AntRole(8, TYPE_WORKER, "Miner");
    static { antRoles.add(ROLE_MINER); }
    public static final AntRole ROLE_COURIER = new AntRole(9, TYPE_WORKER, "Courier");
    static { antRoles.add(ROLE_COURIER); }
    public static final AntRole ROLE_POTTER = new AntRole(10, TYPE_WORKER, "Portable-Feeder");
    static { antRoles.add(ROLE_POTTER); }
    public static final AntRole ROLE_GUARD = new AntRole(11, TYPE_SOLDIER, "Guard");
    static { antRoles.add(ROLE_GUARD); }
    public static final AntRole ROLE_WARRIOR = new AntRole(12, TYPE_SOLDIER, "Warrior");
    static { antRoles.add(ROLE_WARRIOR); }
    public static final AntRole ROLE_DEFENDER = new AntRole(13, TYPE_SOLDIER, "Defender");
    static { antRoles.add(ROLE_DEFENDER); }
    public static final AntRole ROLE_POLICE = new AntRole(14, TYPE_SOLDIER, "Parasite-Police");
    static { antRoles.add(ROLE_POLICE); }
    public static final AntRole ROLE_BOMBER = new AntRole(15, TYPE_SOLDIER, "Bomber");
    static { antRoles.add(ROLE_BOMBER); }
    public static final AntRole ROLE_HUNTER = new AntRole(16, TYPE_SOLDIER, "Hunter");
    static { antRoles.add(ROLE_HUNTER); }
    public static final AntRole ROLE_BRUTE = new AntRole(17, TYPE_MAJOR, "Brute");
    static { antRoles.add(ROLE_BRUTE); }
    public static final AntRole ROLE_CARRIER = new AntRole(18, TYPE_MAJOR, "Troop-Carrier");
    static { antRoles.add(ROLE_CARRIER); }
    public static final AntRole ROLE_ARTILLERY = new AntRole(19, TYPE_MAJOR, "Artillery");
    static { antRoles.add(ROLE_ARTILLERY); }
    public static final AntRole ROLE_SIEGE = new AntRole(20, TYPE_MAJOR, "Siege-Engine");
    static { antRoles.add(ROLE_SIEGE); }
    public static final AntRole ROLE_BORER = new AntRole(21, TYPE_MAJOR, "Tunnel Borer");
    static { antRoles.add(ROLE_BORER); }
    public static final AntRole ROLE_DRONE = new AntRole(22, TYPE_DRONE, "Drone");
    static { antRoles.add(ROLE_DRONE); }
    public static final AntRole ROLE_BREEDER = new AntRole(23, TYPE_PRINCESS, "Breeder");
    static { antRoles.add(ROLE_BREEDER); }
    public static final AntRole ROLE_DIPLOMAT = new AntRole(24, TYPE_PRINCESS, "Diplomat");
    static { antRoles.add(ROLE_DIPLOMAT); }
    public static final AntRole ROLE_LAYER = new AntRole(25, TYPE_QUEEN, "Egg-Layer");
    static { antRoles.add(ROLE_LAYER); }
    public static final AntRole ROLE_RESEARCHER = new AntRole(26, TYPE_QUEEN, "Researcher");
    static { antRoles.add(ROLE_RESEARCHER); }
    public static final AntRole ROLE_MILITIA = new AntRole(27, TYPE_WORKER, "Militia Auxiliary");
    static { antRoles.add(ROLE_MILITIA); }
    public static final AntRole ROLE_CATCHER = new AntRole(28, TYPE_SOLDIER, "Catcher");
    static { antRoles.add(ROLE_CATCHER); }
    public static final AntRole ROLE_CRANE = new AntRole(29, TYPE_MAJOR, "Construction Crane");
    static { antRoles.add(ROLE_CRANE); }
    public static final AntRole ROLE_TRANSPORT = new AntRole(30, TYPE_MAJOR, "Resource Transport");
    static { antRoles.add(ROLE_TRANSPORT); }
    public static final AntRole ROLE_ASSISTANT = new AntRole(31, TYPE_PRINCESS, "Lab Assistant");
    static { antRoles.add(ROLE_ASSISTANT); }
    public static final AntRole ROLE_ESCORT = new AntRole(32, TYPE_SOLDIER, "Convoy Escort");
    static { antRoles.add(ROLE_ESCORT); }
    public static final AntRole ROLE_ENGINEER = new AntRole(33, TYPE_WORKER, "Tunnel Engineer");
    static { antRoles.add(ROLE_ENGINEER); }
    public static final AntRole ROLE_SKYTRANS = new AntRole(34, TYPE_PRINCESS, "Sky Transport");
    static { antRoles.add(ROLE_SKYTRANS); }

    // --- Colony Ranks ---
    public static final ColonyRank RANK_ANT = new ColonyRank(1, "Ant", 1l, 
        loadIcon("icons/ranks/ant.png"));
    static { colonyRanks.add(RANK_ANT); }
    public static final ColonyRank RANK_COLONY = new ColonyRank(2, "Ant Colony", 10l, 
        loadIcon("icons/ranks/colony.png"));
    static { colonyRanks.add(RANK_COLONY); }
    public static final ColonyRank RANK_COUNTY = new ColonyRank(3, "Ant County", 100l, 
        loadIcon("icons/ranks/county.png"));
    static { colonyRanks.add(RANK_COUNTY); }
    public static final ColonyRank RANK_DUCHY = new ColonyRank(4, "Ant Duchy", 1000l, 
        loadIcon("icons/ranks/duchy.png"));
    static { colonyRanks.add(RANK_DUCHY); }
    public static final ColonyRank RANK_KINGDOM = new ColonyRank(5, "Ant Queendom", 10000l, 
        loadIcon("icons/ranks/kingdom.png"));
    static { colonyRanks.add(RANK_KINGDOM); }
    public static final ColonyRank RANK_EMPIRE = new ColonyRank(6, "Ant Empire", 100000l, 
        loadIcon("icons/ranks/empire.png"));
    static { colonyRanks.add(RANK_EMPIRE); }
    public static final ColonyRank RANK_SUPER = new ColonyRank(7, "Ant Super Colony", 1000000l, 
        loadIcon("icons/ranks/super.png"));
    static { colonyRanks.add(RANK_SUPER); }
    public static final ColonyRank RANK_ULTRA = new ColonyRank(8, "Ant Ultra Colony", 10000000l, 
        loadIcon("icons/ranks/ultra.png"));
    static { colonyRanks.add(RANK_ULTRA); }
    public static final ColonyRank RANK_HYPER = new ColonyRank(9, "Ant Hyper Colony", 100000000l, 
        loadIcon("icons/ranks/hyper.png"));
    static { colonyRanks.add(RANK_HYPER); }
    public static final ColonyRank RANK_MEGA = new ColonyRank(10, "Ant Mega Colony", 1000000000l, 
        loadIcon("icons/ranks/mega.png"));
    static { colonyRanks.add(RANK_MEGA); }
    public static final ColonyRank RANK_ULTIMATE = new ColonyRank(11, "Ant Ultimate Colony", 10000000000l, 
        loadIcon("icons/ranks/ultimate.png"));
    static { colonyRanks.add(RANK_ULTIMATE); }
    public static final ColonyRank RANK_SUPREME = new ColonyRank(12, "Ant Supreme Colony", 100000000000l, 
        loadIcon("icons/ranks/supreme.png"));
    static { colonyRanks.add(RANK_SUPREME); }
    public static final ColonyRank RANK_GIGA = new ColonyRank(13, "Ant Giga Colony", 1000000000000l, 
        loadIcon("icons/ranks/giga.png"));
    static { colonyRanks.add(RANK_GIGA); }
    
    // --- Species ---
    public static final Species SPECIES_OMNI = new Species(1, "Omni Ant", "Omniformica Grimunknowni",  "omni/", null, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, GameUnlocks.ROLE_FORAGER, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY));
    static { species.add(SPECIES_OMNI); }
    
    public static final Species SPECIES_LEAF = new Species(2, "Leaf-Cutter Ant", "Atta Cephalotes", "leaf/", GameUnlocks.ASSIMILATION_LEAFCUTTER, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, GameUnlocks.ROLE_FORAGER, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY,
            GameUnlocks.ASSIMILATED_FARMING));
    static { species.add(SPECIES_LEAF); }
    
    public static final Species SPECIES_PHARAOH = new Species(3, "Pharaoh Ant", "Monomorium Pharaonis", "pharaoh/", GameUnlocks.ASSIMILATION_PHARAOH, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, GameUnlocks.ROLE_FORAGER, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY,
            GameUnlocks.ASSIMILATED_MULTIQUEEN));
    static { species.add(SPECIES_PHARAOH); }
    
    public static final Species SPECIES_MARAUDER = new Species(4, "Marauder Ant", "Carebara Diversa", "marauder/", GameUnlocks.ASSIMILATION_MARAUDER, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, GameUnlocks.ROLE_FORAGER, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY,
            GameUnlocks.TYPE_MAJOR));
    static { species.add(SPECIES_MARAUDER); }

    // --- Trade Methods ---
    public static final TradeMethod METHOD_LAND = new TradeMethod(1, "Land", 1.0f, 1.0f, 0.35f, null);
    static { tradeMethods.add(METHOD_LAND); }
    public static final TradeMethod METHOD_AIR = new TradeMethod(2, "Air", 5.0f, 0.5f, 0.25f, null);
    static { tradeMethods.add(METHOD_AIR); }
    public static final TradeMethod METHOD_SEA = new TradeMethod(3, "Sea", 3.0f, 5.0f, 0.15f, null);
    static { tradeMethods.add(METHOD_SEA); }
    public static final TradeMethod METHOD_TUNNEL = new TradeMethod(4, "Tunnel", 2.0f, 2.0f, 0.05f, null);
    static { tradeMethods.add(METHOD_TUNNEL); }
    
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

    public static List<ColonyRank> getColonyRanks() { return Collections.unmodifiableList(colonyRanks); }

    public static List<Species> getSpecies() { return Collections.unmodifiableList(species); }

    public static List<TradeMethod> getTradeMethods() { return Collections.unmodifiableList(tradeMethods); }

    public static List<Humidity> getHumidity() { return Collections.unmodifiableList(humidity); }

    public static List<Temperature> getTemperature() { return Collections.unmodifiableList(temperature); }

    public static List<BugType> getBugTypes() { return Collections.unmodifiableList(bugTypes); }

    public static List<ImageIcon> getMisc() { return Collections.unmodifiableList(misc); }
}