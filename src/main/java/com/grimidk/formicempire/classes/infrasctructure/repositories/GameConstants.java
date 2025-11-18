package com.grimidk.formicempire.classes.infrasctructure.repositories;

import com.grimidk.formicempire.classes.constants.ColonyRank;
import com.grimidk.formicempire.classes.constants.ResourceType;
import com.grimidk.formicempire.classes.constants.Species;
import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntStatus;
import com.grimidk.formicempire.classes.constants.ant.AntSubType;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.ant.MoveStatus;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import java.net.URL; 

public final class GameConstants {
    private GameConstants() {}

    private static ImageIcon loadIcon(String path) {
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resourceUrl == null) {
            System.err.println("CRITICAL ERROR: Resource not found: " + path);
            return null; 
        }
        return new ImageIcon(resourceUrl);
    }

    // --- Misc Icons ---
    public static final ImageIcon ICON_APHID = loadIcon("icons/misc/aphid.png");
    public static final ImageIcon ICON_RESEARCH = loadIcon("icons/misc/research.png");
    
    // --- Temperatures ---
    public static final ImageIcon ICON_TEMP_FREEZING = loadIcon("icons/misc/temp/freezing.png");
    public static final ImageIcon ICON_TEMP_COLD = loadIcon("icons/misc/temp/cold.png");
    public static final ImageIcon ICON_TEMP_CHILLY = loadIcon("icons/misc/temp/chilly.png");
    public static final ImageIcon ICON_TEMP_GOOD = loadIcon("icons/misc/temp/good.png");
    public static final ImageIcon ICON_TEMP_WARM = loadIcon("icons/misc/temp/warm.png");
    public static final ImageIcon ICON_TEMP_HOT = loadIcon("icons/misc/temp/hot.png");

    // --- Humidity
    public static final ImageIcon ICON_HUMID_0 =loadIcon("icons/misc/humid/humid0.png");
    public static final ImageIcon ICON_HUMID_1 =loadIcon("icons/misc/humid/humid1.png");
    public static final ImageIcon ICON_HUMID_2 =loadIcon("icons/misc/humid/humid2.png");
    public static final ImageIcon ICON_HUMID_3 =loadIcon("icons/misc/humid/humid3.png");
    public static final ImageIcon ICON_HUMID_4 =loadIcon("icons/misc/humid/humid4.png");
    public static final ImageIcon ICON_HUMID_5 =loadIcon("icons/misc/humid/humid5.png");

    // --- Biomes ---
    public static final Biome PLAINS_BIOME = new Biome(1, "Plains", 25, 2, 
        loadIcon("icons/biomes/plains.png"));
    public static final Biome FOREST_BIOME = new Biome(2, "Forest", 20, 3, 
        loadIcon("icons/biomes/forest.png"));
    public static final Biome JUNGLE_BIOME = new Biome(3, "Jungle", 30, 4, 
        loadIcon("icons/biomes/jungle.png"));
    public static final Biome SWAMP_BIOME = new Biome(4, "Swamp", 25, 5, 
        loadIcon("icons/biomes/swamp.png"));
    public static final Biome URBAN_BIOME = new Biome(5, "Urban", 30, 1, 
        loadIcon("icons/biomes/urban.png"));
    public static final Biome TUNDRA_BIOME = new Biome(6, "Tundra", 5, 2, 
        loadIcon("icons/biomes/tundra.png"));
    public static final Biome TAIGA_BIOME = new Biome(7, "Taiga", 10, 1, 
        loadIcon("icons/biomes/taiga.png"));
    public static final Biome DESSERT_BIOME = new Biome(8, "Dessert", 50, 0, 
        loadIcon("icons/biomes/dessert.png"));

    // --- Resources ---
    public static final ResourceType PLANT_RESOURCE = new ResourceType(1, "Plant Matter", true, false, 
        loadIcon("icons/resources/plant.png"));
    public static final ResourceType FUNGI_RESOURCE = new ResourceType(2, "Fungi Matter", true, false, 
        loadIcon("icons/resources/mushroom.png"));
    public static final ResourceType MEAT_RESOURCE = new ResourceType(3, "Animal Matter", true, false, 
        loadIcon("icons/resources/protein.png"));
    public static final ResourceType WATER_RESOURCE = new ResourceType(4, "Water", true, true, 
        loadIcon("icons/resources/water.png"));
    public static final ResourceType SYRUP_RESOURCE = new ResourceType(5, "Syrup", true, true, 
        loadIcon("icons/resources/syrup.png"));
    public static final ResourceType RESIN_RESOURCE = new ResourceType(6, "Resin", false, true, 
        loadIcon("icons/resources/resin.png"));
    public static final ResourceType ROCK_RESOURCE = new ResourceType(7, "Mineral", false, false, 
        loadIcon("icons/resources/mineral.png"));

    // --- Times of Day ---
    public static final TimeOfDay DAY_TIME = new TimeOfDay(1, "Daytime", 1f,
        loadIcon("icons/times/day.png"));
    public static final TimeOfDay DUSK_TIME = new TimeOfDay(2, "Dusk", 5/8f,
        loadIcon("icons/times/dusk.png"));
    public static final TimeOfDay NIGHT_TIME = new TimeOfDay(3, "Nightime", 3/4f,
        loadIcon("icons/times/night.png"));
    public static final TimeOfDay DAWN_TIME = new TimeOfDay(4, "Dawn", 5/8f,
        loadIcon("icons/times/dawn.png"));
    public static final TimeOfDay SOLAR_ECLIPSE_TIME = new TimeOfDay(5, "Solar Eclipse", 3/2f,
        loadIcon("icons/times/solar-eclipse.png"));
    public static final TimeOfDay LUNAR_ECLIPSE_TIME = new TimeOfDay(6, "Lunar Eclipse", 3/2f,
        loadIcon("icons/times/lunar-eclipse.png"));

    // --- Moon Phases ---
    public static final MoonPhase NEW_MOON_PHASE = new MoonPhase(1, "New Moon", 1f, 
        loadIcon("icons/moon/new-moon.png"));
    public static final MoonPhase WAXING_CRESCENT_PHASE = new MoonPhase(2, "Waxing Crescent", 3/4f,
        loadIcon("icons/moon/waxing-crescent.png"));
    public static final MoonPhase FIRST_QUARTER_PHASE = new MoonPhase(3, "First Quarter", 1/2f, 
        loadIcon("icons/moon/first-quarter.png"));
    public static final MoonPhase WAXING_GIBBOUS_PHASE = new MoonPhase(4, "Waxing Gibbous", 1/4f, 
        loadIcon("icons/moon/waxing-gibbous.png"));
    public static final MoonPhase FULL_MOON_PHASE = new MoonPhase(5, "Full Moon", 0f, 
        loadIcon("icons/moon/full-moon.png"));
    public static final MoonPhase WANING_GIBBOUS_PHASE = new MoonPhase(6, "Waning Gibbous", 1/4f, 
        loadIcon("icons/moon/waning-gibbous.png"));
    public static final MoonPhase LAST_QUARTER_PHASE = new MoonPhase(7, "Last Quarter", 1/2f, 
        loadIcon("icons/moon/third-quarter.png"));
    public static final MoonPhase WANING_CRESCENT_PHASE = new MoonPhase(8, "Waning Crescent", 3/4f, 
        loadIcon("icons/moon/waning-crescent.png"));

    // --- Seasons ---
    public static final Season SPRING_SEASON = new Season(1, "Spring", 1f, 3, 
        loadIcon("icons/seasons/spring.png"));
    public static final Season SUMMER_SEASON = new Season(2, "Summer", 2f, 1, 
        loadIcon("icons/seasons/summer.png"));
    public static final Season AUTUMN_SEASON = new Season(3, "Autumn", 1f, 2, 
        loadIcon("icons/seasons/autumn.png"));
    public static final Season WINTER_SEASON = new Season(4, "Winter", 1/2f, 1, 
        loadIcon("icons/seasons/winter.png"));

    // --- Weather ---
    public static final Weather CLEAR_WEATHER = new Weather(1, "Clear", 1, 0, 
        loadIcon("icons/weather/clear.png"));
    public static final Weather RAIN_WEATHER = new Weather(2, "Rain", 1, 1, 
        loadIcon("icons/weather/rain.png"));
    public static final Weather SNOW_WEATHER = new Weather(3, "Snow", 1, 1, 
        loadIcon("icons/weather/snow.png"));
    public static final Weather HEAVY_RAIN_WEATHER = new Weather(4, "Heavy Rain", 1, 3, 
        loadIcon("icons/weather/heavy-rain.png"));
    public static final Weather THUNDER_WEATHER = new Weather(5, "Thunder Storm", 1, 2, 
        loadIcon("icons/weather/thunder.png"));
    public static final Weather HEAVY_SNOW_WEATHER = new Weather(6, "Snow Storm", 1, 2, 
        loadIcon("icons/weather/heavy-snow.png"));
    public static final Weather WIND_WEATHER = new Weather(7, "Heavy Wind", 1, -1, 
        loadIcon("icons/weather/heavy-wind.png"));
    public static final Weather HEAT_WEATHER = new Weather(8, "Heat Wave", 2, -2, 
        loadIcon("icons/weather/heat-wave.png"));
    public static final Weather FROG_WEATHER = new Weather(9, "Frog Rain", 1, 0, 
        loadIcon("icons/weather/frog-rain.png"));

    // --- Ant Status ---
    public static final AntStatus STATUS_ALIVE = new AntStatus(1, "Alive", 
        loadIcon("icons/status/alive.png"));
    public static final AntStatus STATUS_DEAD = new AntStatus(2, "Dead", 
        loadIcon("icons/status/dead.png"));
    public static final AntStatus STATUS_ZOMBIFIED = new AntStatus(3, "Zombified", 
        loadIcon("icons/status/zombified.png"));

    // --- Move Status ---
    public static final MoveStatus MOVE_STATIC = new MoveStatus(1, "Static", 0);
    public static final MoveStatus MOVE_WANDER = new MoveStatus(2, "Wandering", 1/2f);
    public static final MoveStatus MOVE_MARCH = new MoveStatus(3, "Marching", 1f);
    public static final MoveStatus MOVE_SPEED = new MoveStatus(4, "Speed Marching", 3/2f);
    public static final MoveStatus MOVE_FLY = new MoveStatus(5, "Flying", 3f);

    // --- Ant Types ---
    public static final AntType TYPE_EGG = new AntType(1, "Egg", 1f, 0f, 0f, 0f, 0f, 0f, 0f, 1/4f, 
        loadIcon("icons/ants/egg.png"), loadIcon("sprites/ants/egg.png"));
    public static final AntType TYPE_LARVA = new AntType(2, "Larva", 1f, 1/2f, 1/2f, 1f, 1f, 1/2f, 1/2f, 1/2f, 
        loadIcon("icons/ants/larva.png"), loadIcon("sprites/ants/larva.png"));
    public static final AntType TYPE_PUPA = new AntType(3, "Pupa", 1f, 0f, 1f, 0f, 0f, 1/2f, 0f, 1f, 
        loadIcon("icons/ants/pupa.png"), loadIcon("sprites/ants/pupa.png"));
    public static final AntType TYPE_WORKER = new AntType(4, "Worker", 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/worker.png"), loadIcon("sprites/ants/worker.png"));
    public static final AntType TYPE_SOLDIER = new AntType(5, "Soldier", 3f, 3f, 3f, 2f, 3f, 5f, 3f, 2f, 
        loadIcon("icons/ants/soldier.png"), loadIcon("sprites/ants/soldier.png"));
    public static final AntType TYPE_MAJOR = new AntType(6, "Major", 10f, 15f, 20f, 5f, 2f, 50f, 2f, 5f, 
        loadIcon("icons/ants/major.png"), loadIcon("sprites/ants/major.png"));
    public static final AntType TYPE_DRONE = new AntType(7, "Drone", 1f, 1f, 1f, 1f, 1f, 1f, 1f, 2f, 
        loadIcon("icons/ants/drone.png"), loadIcon("sprites/ants/drone.png"));
    public static final AntType TYPE_PRINCESS = new AntType(8, "Princess", 1f, 1f, 1f, 1f, 1f, 1f, 1f, 2f, 
        loadIcon("icons/ants/princess.png"), loadIcon("sprites/ants/princess.png"));
    public static final AntType TYPE_QUEEN = new AntType(9, "Queen", 50f, 2f, 50f, 10f, 1/2f, 50f, 1/4f, 5f, 
        loadIcon("icons/ants/queen.png"), loadIcon("sprites/ants/queen.png"));
    public static final AntType TYPE_DEAD = new AntType(10, "Dead", 0, 0, 0, 0, 0, 0, 0, 0,  
        loadIcon("icons/ants/dead.png"), loadIcon("sprites/ants/dead.png"));
    public static final AntType TYPE_ZOMBIE = new AntType(11, "Zombie",  1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/zombie.png"), loadIcon("sprites/ants/zombie.png"));

    // --- Ant Subtypes ---
    // (Add definitions here when you have them)

    // --- Ant Roles ---
    public static final AntRole ROLE_FORAGER = new AntRole(1, TYPE_WORKER, "Forager");
    public static final AntRole ROLE_NURSE = new AntRole(2, TYPE_WORKER, "Nurse");
    public static final AntRole ROLE_BUILDER = new AntRole(3, TYPE_WORKER, "Builder");
    public static final AntRole ROLE_SCOUT = new AntRole(4, TYPE_WORKER, "Scout");
    public static final AntRole ROLE_FARMER = new AntRole(5, TYPE_WORKER, "Farmer");
    public static final AntRole ROLE_RANCHER = new AntRole(6, TYPE_WORKER, "Rancher");
    public static final AntRole ROLE_GRAVER = new AntRole(7, TYPE_WORKER, "Grave-Keeper");
    public static final AntRole ROLE_MINER = new AntRole(8, TYPE_WORKER, "Miner");
    public static final AntRole ROLE_COURIER = new AntRole(9, TYPE_WORKER, "Courier");
    public static final AntRole ROLE_POTTER = new AntRole(10, TYPE_WORKER, "Portable-Feeder");
    public static final AntRole ROLE_GUARD = new AntRole(11, TYPE_SOLDIER, "Guard");
    public static final AntRole ROLE_WARRIOR = new AntRole(12, TYPE_SOLDIER, "Warrior");
    public static final AntRole ROLE_DEFENDER = new AntRole(13, TYPE_SOLDIER, "Defender");
    public static final AntRole ROLE_POLICE = new AntRole(14, TYPE_SOLDIER, "Parasite-Police");
    public static final AntRole ROLE_BOMBER = new AntRole(15, TYPE_SOLDIER, "Bomber");
    public static final AntRole ROLE_HUNTER = new AntRole(16, TYPE_SOLDIER, "Hunter");
    public static final AntRole ROLE_BRUTE = new AntRole(17, TYPE_MAJOR, "Brute");
    public static final AntRole ROLE_CARRIER = new AntRole(18, TYPE_MAJOR, "Troop-Carrier");
    public static final AntRole ROLE_ARTILLERY = new AntRole(19, TYPE_MAJOR, "Artillery");
    public static final AntRole ROLE_SIEGE = new AntRole(20, TYPE_MAJOR, "Siege-Engine");
    public static final AntRole ROLE_BORER = new AntRole(21, TYPE_MAJOR, "Boring-Machine");
    public static final AntRole ROLE_DRONE = new AntRole(22, TYPE_DRONE, "Drone");
    public static final AntRole ROLE_BREEDER = new AntRole(23, TYPE_PRINCESS, "Breeder");
    public static final AntRole ROLE_DIPLOMAT = new AntRole(24, TYPE_PRINCESS, "Diplomat");
    public static final AntRole ROLE_LAYER = new AntRole(25, TYPE_QUEEN, "Egg-Layer");
    public static final AntRole ROLE_RESEARCHER = new AntRole(26, TYPE_QUEEN, "Researcher");
    public static final AntRole ROLE_MILITIA = new AntRole(27, TYPE_WORKER, "Militia Auxiliary");
    public static final AntRole ROLE_CATCHER = new AntRole(28, TYPE_SOLDIER, "Catcher");
    public static final AntRole ROLE_CRANE = new AntRole(29, TYPE_MAJOR, "Construction Crane");

    // --- Colony Ranks ---
    public static final ColonyRank RANK_ANT = new ColonyRank(1, "Ant", 1l, 
        loadIcon("icons/ranks/ant.png"));
    public static final ColonyRank RANK_COLONY = new ColonyRank(2, "Ant Colony", 10l, 
        loadIcon("icons/ranks/colony.png"));
    public static final ColonyRank RANK_COUNTY = new ColonyRank(3, "Ant County", 100l, 
        loadIcon("icons/ranks/county.png"));
    public static final ColonyRank RANK_DUCHY = new ColonyRank(4, "Ant Duchy", 1000l, 
        loadIcon("icons/ranks/duchy.png"));
    public static final ColonyRank RANK_KINGDOM = new ColonyRank(5, "Ant Queendom", 10000l, 
        loadIcon("icons/ranks/kingdom.png"));
    public static final ColonyRank RANK_EMPIRE = new ColonyRank(6, "Ant Empire", 100000l, 
        loadIcon("icons/ranks/empire.png"));
    public static final ColonyRank RANK_SUPER = new ColonyRank(7, "Ant Super Colony", 1000000l, 
        loadIcon("icons/ranks/super.png"));
    public static final ColonyRank RANK_ULTRA = new ColonyRank(8, "Ant Ultra Colony", 10000000l, 
        loadIcon("icons/ranks/ultra.png"));
    public static final ColonyRank RANK_HYPER = new ColonyRank(9, "Ant Hyper Colony", 100000000l, 
        loadIcon("icons/ranks/hyper.png"));
    public static final ColonyRank RANK_MEGA = new ColonyRank(10, "Ant Mega Colony", 1000000000l, 
        loadIcon("icons/ranks/mega.png"));
    public static final ColonyRank RANK_ULTIMATE = new ColonyRank(11, "Ant Ultimate Colony", 10000000000l, 
        loadIcon("icons/ranks/ultimate.png"));
    public static final ColonyRank RANK_SUPREME = new ColonyRank(12, "Ant Supreme Colony", 100000000000l, 
        loadIcon("icons/ranks/supreme.png"));
    public static final ColonyRank RANK_GIGA = new ColonyRank(13, "Ant Giga Colony", 1000000000000l, 
        loadIcon("icons/ranks/giga.png"));
    
    // --- Species ---
    // (Add definitions here when you have them)

    // --- Lists to hold the constants ---
    private static final List<Biome> biomes = new ArrayList<>();
    private static final List<ResourceType> resources = new ArrayList<>();
    private static final List<TimeOfDay> timesOfDay = new ArrayList<>();
    private static final List<MoonPhase> moonPhases = new ArrayList<>();
    private static final List<Season> seasons = new ArrayList<>();
    private static final List<Weather> weathers = new ArrayList<>();
    private static final List<AntStatus> antStatuses = new ArrayList<>();
    private static final List<MoveStatus> moveStatuses = new ArrayList<>();
    private static final List<AntType> antTypes = new ArrayList<>();
    private static final List<AntSubType> antSubTypes = new ArrayList<>();
    private static final List<AntRole> antRoles = new ArrayList<>();
    private static final List<ColonyRank> colonyRanks = new ArrayList<>();
    private static final List<Species> species = new ArrayList<>();
    public static final List<ImageIcon> humidity = new ArrayList<>();
    private static final List<ImageIcon> temperature = new ArrayList<>();
    private static final List<ImageIcon> misc = new ArrayList<>();
    
    // --- Static initializer block ---
    static {
        biomes.add(PLAINS_BIOME);
        biomes.add(FOREST_BIOME);
        biomes.add(JUNGLE_BIOME);
        biomes.add(SWAMP_BIOME);
        biomes.add(URBAN_BIOME);
        biomes.add(TUNDRA_BIOME);
        biomes.add(TAIGA_BIOME);
        biomes.add(DESSERT_BIOME);

        resources.add(PLANT_RESOURCE);
        resources.add(FUNGI_RESOURCE);
        resources.add(MEAT_RESOURCE);
        resources.add(WATER_RESOURCE);
        resources.add(SYRUP_RESOURCE);
        resources.add(RESIN_RESOURCE);
        resources.add(ROCK_RESOURCE);

        timesOfDay.add(DAY_TIME);
        timesOfDay.add(DUSK_TIME);
        timesOfDay.add(NIGHT_TIME);
        timesOfDay.add(DAWN_TIME);
        timesOfDay.add(SOLAR_ECLIPSE_TIME);
        timesOfDay.add(LUNAR_ECLIPSE_TIME);

        moonPhases.add(NEW_MOON_PHASE);
        moonPhases.add(WAXING_CRESCENT_PHASE);
        moonPhases.add(FIRST_QUARTER_PHASE);
        moonPhases.add(WAXING_GIBBOUS_PHASE);
        moonPhases.add(FULL_MOON_PHASE);
        moonPhases.add(WANING_GIBBOUS_PHASE);
        moonPhases.add(LAST_QUARTER_PHASE);
        moonPhases.add(WANING_CRESCENT_PHASE);

        seasons.add(SPRING_SEASON);
        seasons.add(SUMMER_SEASON);
        seasons.add(AUTUMN_SEASON);
        seasons.add(WINTER_SEASON);

        weathers.add(CLEAR_WEATHER);
        weathers.add(RAIN_WEATHER);
        weathers.add(SNOW_WEATHER);
        weathers.add(HEAVY_RAIN_WEATHER);
        weathers.add(THUNDER_WEATHER);
        weathers.add(HEAVY_SNOW_WEATHER);
        weathers.add(WIND_WEATHER);
        weathers.add(HEAT_WEATHER);
        weathers.add(FROG_WEATHER);

        antStatuses.add(STATUS_ALIVE);
        antStatuses.add(STATUS_DEAD);
        antStatuses.add(STATUS_ZOMBIFIED);

        moveStatuses.add(MOVE_STATIC);
        moveStatuses.add(MOVE_WANDER);
        moveStatuses.add(MOVE_MARCH);
        moveStatuses.add(MOVE_SPEED);
        moveStatuses.add(MOVE_FLY);

        antTypes.add(TYPE_EGG);
        antTypes.add(TYPE_LARVA);
        antTypes.add(TYPE_PUPA);
        antTypes.add(TYPE_WORKER);
        antTypes.add(TYPE_SOLDIER);
        antTypes.add(TYPE_MAJOR);
        antTypes.add(TYPE_DRONE);
        antTypes.add(TYPE_PRINCESS);
        antTypes.add(TYPE_QUEEN);
        antTypes.add(TYPE_DEAD);
        antTypes.add(TYPE_ZOMBIE);

        // (Add antSubTypes here when you have them)

        antRoles.add(ROLE_FORAGER);
        antRoles.add(ROLE_NURSE);
        antRoles.add(ROLE_BUILDER);
        antRoles.add(ROLE_SCOUT);
        antRoles.add(ROLE_FARMER);
        antRoles.add(ROLE_RANCHER);
        antRoles.add(ROLE_GRAVER);
        antRoles.add(ROLE_MINER);
        antRoles.add(ROLE_COURIER);
        antRoles.add(ROLE_POTTER);
        antRoles.add(ROLE_GUARD);
        antRoles.add(ROLE_WARRIOR);
        antRoles.add(ROLE_DEFENDER);
        antRoles.add(ROLE_POLICE);
        antRoles.add(ROLE_BOMBER);
        antRoles.add(ROLE_HUNTER);
        antRoles.add(ROLE_BRUTE);
        antRoles.add(ROLE_CARRIER);
        antRoles.add(ROLE_ARTILLERY);
        antRoles.add(ROLE_SIEGE);
        antRoles.add(ROLE_BORER);
        antRoles.add(ROLE_DRONE);
        antRoles.add(ROLE_BREEDER);
        antRoles.add(ROLE_DIPLOMAT);
        antRoles.add(ROLE_LAYER);
        antRoles.add(ROLE_RESEARCHER);
        antRoles.add(ROLE_MILITIA);
        antRoles.add(ROLE_CATCHER);
        antRoles.add(ROLE_CRANE);

        colonyRanks.add(RANK_ANT);
        colonyRanks.add(RANK_COLONY);
        colonyRanks.add(RANK_COUNTY);
        colonyRanks.add(RANK_DUCHY);
        colonyRanks.add(RANK_KINGDOM);
        colonyRanks.add(RANK_EMPIRE);
        colonyRanks.add(RANK_SUPER);
        colonyRanks.add(RANK_ULTRA);
        colonyRanks.add(RANK_HYPER);
        colonyRanks.add(RANK_MEGA);
        colonyRanks.add(RANK_ULTIMATE);
        colonyRanks.add(RANK_SUPREME);
        colonyRanks.add(RANK_GIGA);

        temperature.add(ICON_TEMP_FREEZING);
        temperature.add(ICON_TEMP_COLD);
        temperature.add(ICON_TEMP_CHILLY);
        temperature.add(ICON_TEMP_GOOD);
        temperature.add(ICON_TEMP_WARM);
        temperature.add(ICON_TEMP_HOT);

        humidity.add(ICON_HUMID_0);
        humidity.add(ICON_HUMID_1);
        humidity.add(ICON_HUMID_2);
        humidity.add(ICON_HUMID_3);
        humidity.add(ICON_HUMID_4);
        humidity.add(ICON_HUMID_5);

        misc.add(ICON_APHID);
        misc.add(ICON_RESEARCH);
        
        // (Add species here when you have them)
    }
    
    // --- Public Static Getters ---
    
    public static List<Biome> getBiomes() {
        return Collections.unmodifiableList(biomes);
    }

    public static List<ResourceType> getResources() {
        return Collections.unmodifiableList(resources);
    }

    public static List<TimeOfDay> getTimesOfDay() {
        return Collections.unmodifiableList(timesOfDay);
    }

    public static List<MoonPhase> getMoonPhases() {
        return Collections.unmodifiableList(moonPhases);
    }

    public static List<Season> getSeasons() {
        return Collections.unmodifiableList(seasons);
    }

    public static List<Weather> getWeathers() {
        return Collections.unmodifiableList(weathers);
    }

    public static List<AntStatus> getAntStatuses() {
        return Collections.unmodifiableList(antStatuses);
    }

    public static List<AntType> getAntTypes() {
        return Collections.unmodifiableList(antTypes);
    }

    public static List<AntSubType> getAntSubTypes() {
        return Collections.unmodifiableList(antSubTypes);
    }

    public static List<AntRole> getAntRoles() {
        return Collections.unmodifiableList(antRoles);
    }

    public static List<ColonyRank> getColonyRanks() {
        return Collections.unmodifiableList(colonyRanks);
    }

    public static List<Species> getSpecies() {
        return Collections.unmodifiableList(species);
    }
}