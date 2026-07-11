package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.infrasctructure.assets.GameSpritePreloader;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntStatus;
import com.grimidk.formicempire.classes.constants.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.ant.MoveStatus;
import com.grimidk.formicempire.classes.constants.misc.BugType;
import com.grimidk.formicempire.classes.constants.misc.ColonyLoyalty;
import com.grimidk.formicempire.classes.constants.misc.ColonyLoyaltyModifier;
import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.misc.DynastyTitle;
import com.grimidk.formicempire.classes.constants.misc.DiplomaticReputation;
import com.grimidk.formicempire.classes.constants.misc.DiplomaticReputationModifier;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.constants.misc.GeneticIntegrityModifier;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Humidity;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

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
        GameSpritePreloader.ensureLoaded(icon);
        iconCache.put(path, icon);
        return icon;
    }

    // --- Building room overlay sprites ---
    public static final ImageIcon ROOM_ROYAL_L0 = loadIcon("sprites/buildings/rooms/RoyalL0.png");
    public static final ImageIcon ROOM_ROYAL_L1 = loadIcon("sprites/buildings/rooms/RoyalL1.png");
    public static final ImageIcon ROOM_ROYAL_L2 = loadIcon("sprites/buildings/rooms/RoyalL2.png");
    public static final ImageIcon ROOM_ROYAL_L3 = loadIcon("sprites/buildings/rooms/RoyalL3.png");
    public static final ImageIcon ROOM_EGG_L0 = loadIcon("sprites/buildings/rooms/EggL0.png");
    public static final ImageIcon ROOM_EGG_L1 = loadIcon("sprites/buildings/rooms/EggL1.png");
    public static final ImageIcon ROOM_EGG_L2 = loadIcon("sprites/buildings/rooms/EggL2.png");
    public static final ImageIcon ROOM_EGG_L3 = loadIcon("sprites/buildings/rooms/EggL3.png");
    public static final ImageIcon ROOM_MUSHROOM_L0 = loadIcon("sprites/buildings/rooms/MushroomL0.png");
    public static final ImageIcon ROOM_MUSHROOM_L1 = loadIcon("sprites/buildings/rooms/MushroomL1.png");
    public static final ImageIcon ROOM_MUSHROOM_L2 = loadIcon("sprites/buildings/rooms/MushroomL2.png");
    public static final ImageIcon ROOM_MUSHROOM_L3 = loadIcon("sprites/buildings/rooms/MushroomL3.png");
    public static final ImageIcon ROOM_PLANT_L0 = loadIcon("sprites/buildings/rooms/PlantL0.png");
    public static final ImageIcon ROOM_PLANT_L1 = loadIcon("sprites/buildings/rooms/PlantL1.png");
    public static final ImageIcon ROOM_PLANT_L2 = loadIcon("sprites/buildings/rooms/PlantL2.png");
    public static final ImageIcon ROOM_PLANT_L3 = loadIcon("sprites/buildings/rooms/PlantL3.png");
    public static final ImageIcon ROOM_WATER_L0 = loadIcon("sprites/buildings/rooms/WaterL0.png");
    public static final ImageIcon ROOM_WATER_L1 = loadIcon("sprites/buildings/rooms/WaterL1.png");
    public static final ImageIcon ROOM_WATER_L2 = loadIcon("sprites/buildings/rooms/WaterL2.png");
    public static final ImageIcon ROOM_WATER_L3 = loadIcon("sprites/buildings/rooms/WaterL3.png");
    public static final ImageIcon ROOM_MEAT_L0 = loadIcon("sprites/buildings/rooms/MeatL0.png");
    public static final ImageIcon ROOM_MEAT_L1 = loadIcon("sprites/buildings/rooms/MeatL1.png");
    public static final ImageIcon ROOM_MEAT_L2 = loadIcon("sprites/buildings/rooms/MeatL2.png");
    public static final ImageIcon ROOM_MEAT_L3 = loadIcon("sprites/buildings/rooms/MeatL3.png");
    public static final ImageIcon ROOM_SYRUP_L0 = loadIcon("sprites/buildings/rooms/SyrupL0.png");
    public static final ImageIcon ROOM_SYRUP_L1 = loadIcon("sprites/buildings/rooms/SyrupL1.png");
    public static final ImageIcon ROOM_SYRUP_L2 = loadIcon("sprites/buildings/rooms/SyrupL2.png");
    public static final ImageIcon ROOM_SYRUP_L3 = loadIcon("sprites/buildings/rooms/SyrupL3.png");
    public static final ImageIcon ROOM_ROCK_L0 = loadIcon("sprites/buildings/rooms/RockL0.png");
    public static final ImageIcon ROOM_ROCK_L1 = loadIcon("sprites/buildings/rooms/RockL1.png");
    public static final ImageIcon ROOM_ROCK_L2 = loadIcon("sprites/buildings/rooms/RockL2.png");
    public static final ImageIcon ROOM_ROCK_L3 = loadIcon("sprites/buildings/rooms/RockL3.png");
    public static final ImageIcon ROOM_RESIN_L0 = loadIcon("sprites/buildings/rooms/ResinL0.png");
    public static final ImageIcon ROOM_RESIN_L1 = loadIcon("sprites/buildings/rooms/ResinL1.png");
    public static final ImageIcon ROOM_RESIN_L2 = loadIcon("sprites/buildings/rooms/ResinL2.png");
    public static final ImageIcon ROOM_RESIN_L3 = loadIcon("sprites/buildings/rooms/ResinL3.png");
    public static final ImageIcon ROOM_PASSIVE_LAB = loadIcon("sprites/buildings/rooms/PassiveLab.png");
    public static final ImageIcon ROOM_PASSIVE_WATER = loadIcon("sprites/buildings/rooms/PassiveWater.png");
    public static final ImageIcon ROOM_PASSIVE_APHID = loadIcon("sprites/buildings/rooms/PassiveAphid.png");
    public static final ImageIcon ROOM_PASSIVE_NURSE = loadIcon("sprites/buildings/rooms/PassiveNurse.png");
    public static final ImageIcon ROOM_PASSIVE_FARM = loadIcon("sprites/buildings/rooms/PassiveFarm.png");
    public static final ImageIcon ROOM_PASSIVE_GRAVE = loadIcon("sprites/buildings/rooms/PassiveGrave.png");
    public static final ImageIcon ROOM_PASSIVE_COMPOSTER = loadIcon("sprites/buildings/rooms/PassiveComposter.png");

    // --- Convoy view tiles ---
    public static final ImageIcon CONVOY_TILE_SEA = loadIcon("backgrounds/convoy/SeaConvoyTile.png");
    public static final ImageIcon CONVOY_TILE_UNDERGROUND = loadIcon("backgrounds/convoy/UndergroundConvoyTile.png");
    public static final ImageIcon CONVOY_TILE_SKY = loadIcon("backgrounds/convoy/SkyConvoyTile.png");

    public static final ImageIcon TUNNEL_SPRITE = loadIcon("sprites/buildings/TunnelSprite.png");

    public static ImageIcon getAntSprite(AntType type, Species species) {
        return getAntSprite(type, species, AntSubtypeProfile.standard());
    }

    public static ImageIcon getAntSprite(AntType type, Species species, AntSubtypeProfile profile) {
        if (type == null) {
            return null;
        }
        if (type != TYPE_EGG && type != TYPE_LARVA && type != TYPE_PUPA
                && type != TYPE_DEAD && type != TYPE_ZOMBIE) {
            ImageIcon subtypeSprite = resolveSubtypeSprite(type, species, profile);
            if (subtypeSprite != null) {
                return subtypeSprite;
            }
        }

        String path;
        if (type == TYPE_EGG || type == TYPE_LARVA || type == TYPE_PUPA || type == TYPE_DEAD || type == TYPE_ZOMBIE) {
            path = "sprites/ants/" + type.getSpriteName();
        } else {
            String dir = (species != null) ? species.getDirectory() : "omni/";
            path = "sprites/ants/" + dir + type.getSpriteName();
        }

        return loadIcon(path);
    }

    private static ImageIcon resolveSubtypeSprite(AntType type, Species species, AntSubtypeProfile profile) {
        if (profile == null || profile.isStandard()) {
            return null;
        }
        String folder = profile.buildSpriteFolder();
        if (folder == null) {
            return null;
        }
        String speciesDir = resolveSubtypeSpriteSpeciesDir(species, profile);
        String path = "sprites/ants/" + speciesDir + "subtypes/" + folder + "/" + type.getSpriteName();
        return loadIcon(path);
    }

    private static String resolveSubtypeSpriteSpeciesDir(Species species, AntSubtypeProfile profile) {
        if (species != null && species.getId() == SPECIES_OMNI.getId()) {
            return "omni/";
        }
        if (profile.countActiveSubtypes() > 1) {
            return "omni/";
        }
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype != null && !subtype.isNone() && subtype.hasSprite()) {
                return subtype.getSpriteSpeciesDir();
            }
        }
        return "omni/";
    }

    public static ImageIcon getAssimilatedDroneSprite(Species species) {
        if (species == null || !hasAssimilatedDroneSprite(species)) {
            return null;
        }
        return loadIcon("sprites/ants/zero-drones/" + species.getZeroDroneSpriteFileName());
    }

    /** True when {@code sprites/ants/zero-drones/} has art for this species (fully implemented assimilations only). */
    public static boolean hasAssimilatedDroneSprite(Species species) {
        return species != null && WORLD_SPAWNABLE_NPC_SPECIES_IDS.contains(species.getId());
    }

    private static Set<Upgrade> defaultSpeciesUpgrades(Upgrade speciesTrait) {
        return Set.of(
                GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER,
                GameUnlocks.ROLE_FORAGER, GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE,
                GameUnlocks.ROLE_LAYER, GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID,
                GameUnlocks.STAT_LONGEVITY, speciesTrait);
    }

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
    private static final List<AntSubtype> antSubtypes = new ArrayList<>();
    private static final List<AntRole> antRoles = new ArrayList<>();
    private static final List<ColonyRank> colonyRanks = new ArrayList<>();
    private static final List<GameSpeed> gameSpeeds = new ArrayList<>();
    private static final List<DiplomaticReputation> diplomaticReputations = new ArrayList<>();
    private static final List<DiplomaticReputationModifier> diplomaticReputationModifiers = new ArrayList<>();
    private static final List<GeneticIntegrityModifier> geneticIntegrityModifiers = new ArrayList<>();
    private static final List<ColonyLoyalty> colonyLoyalties = new ArrayList<>();
    private static final List<ColonyLoyaltyModifier> colonyLoyaltyModifiers = new ArrayList<>();
    private static final List<DynastyTitle> dynastyTitles = new ArrayList<>();
    private static final List<DynastyTitle> cityTitles = new ArrayList<>();
    private static final List<Species> species = new ArrayList<>();
    private static final List<TradeMethod> tradeMethods = new ArrayList<>();
    private static final List<Humidity> humidity = new ArrayList<>();
    private static final List<Temperature> temperature = new ArrayList<>();
    private static final List<ImageIcon> misc = new ArrayList<>();

    // --- Misc Icons ---
    public static final ImageIcon ICON_SOCIALISM = loadIcon("icons/misc/Socialism.png");
    static { misc.add(ICON_SOCIALISM); }
    public static final ImageIcon ICON_APHID = loadIcon("icons/bugs/Aphid.png");
    static { misc.add(ICON_APHID); }
    public static final ImageIcon ICON_SYMBIOTIC_MITE = loadIcon("icons/bugs/SoilMite.png");
    static { misc.add(ICON_SYMBIOTIC_MITE); }
    public static final ImageIcon ICON_DERMESTID = loadIcon("icons/bugs/Dermestid.png");
    static { misc.add(ICON_DERMESTID); }
    public static final ImageIcon ICON_PARASITIC_MITE = loadIcon("icons/bugs/ParasiticMite.png");
    static { misc.add(ICON_PARASITIC_MITE); }
    public static final ImageIcon ICON_RESEARCH = loadIcon("icons/misc/Research.png");
    static { misc.add(ICON_RESEARCH); }
    public static final ImageIcon ICON_STAT_LOYALTY = loadIcon("icons/misc/Loyalty.png");
    static { misc.add(ICON_STAT_LOYALTY); }
    public static final ImageIcon ICON_STAT_REPUTATION = loadIcon("icons/misc/Reputation.png");
    static { misc.add(ICON_STAT_REPUTATION); }
    public static final ImageIcon ICON_STAT_GENETIC_INTEGRITY = loadIcon("icons/misc/GeneticIntegrity.png");
    static { misc.add(ICON_STAT_GENETIC_INTEGRITY); }
    public static final ImageIcon ICON_STAT_MILITARY_POWER = loadIcon("icons/misc/MilitaryPower.png");
    static { misc.add(ICON_STAT_MILITARY_POWER); }
    public static final ImageIcon ICON_STAT_POPULATION = loadIcon("icons/misc/Population.png");
    static { misc.add(ICON_STAT_POPULATION); }
    public static final ImageIcon ICON_STAT_FOOD_CONSUMPTION = loadIcon("icons/misc/FoodConsumption.png");
    static { misc.add(ICON_STAT_FOOD_CONSUMPTION); }
    public static final ImageIcon ICON_STAT_FOOD_PRODUCTION = loadIcon("icons/misc/FoodProduction.png");
    static { misc.add(ICON_STAT_FOOD_PRODUCTION); }
    public static final ImageIcon ICON_STAT_NET_FOOD = loadIcon("icons/misc/NetFood.png");
    static { misc.add(ICON_STAT_NET_FOOD); }

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
    public static final int PET_COUNT_SAVE_ABS_MAX = 10_000;
    public static final int MAX_PEN_NON_ANT_SPRITES = 500;
    public static final float CATCH_BASE_CHANCE_PER_CATCHER = 0.12f;

    public static int capPenNonAntSprites(int count) {
        return Math.min(Math.max(0, count), MAX_PEN_NON_ANT_SPRITES);
    }

    public static final int CONVOY_TUNNEL_LEG_DISTANCE = 10_000;
    public static final int CONVOY_PORTAL_APPROACH_PX = 36;
    public static final int SYMBIOTIC_MITE_PARASITIC_MITE_KILL_PER_DAY = 5;
    public static final int SYMBIOTIC_MITE_PARASITIC_MITE_KILL_UPGRADED = 12;

    public static final int PARASITIC_MITE_RESOURCE_THRESHOLD = 10_000;
    public static final float PARASITE_OUTBREAK_CHANCE = 0.25f;
    public static final int PARASITE_OUTBREAK_PREVENTION_MULTIPLIER = 2;
    public static final int PARASITIC_MITE_MIN_MONTHLY_SPAWN = 1_000;
    public static final int PARASITIC_MITE_PER_ANT = 1;
    public static final float PARASITIC_MITE_SPREAD_FACTOR = 0.10f;
    public static final int PARASITIC_MITES_PER_SLOWED_ANT = 10;
    public static final float PARASITIC_MITE_SPEED_MULTIPLIER = 0.5f;

    // --- Temperatures ---
    public static final Temperature TEMP_FREEZING = new Temperature(1, LanguageStrings.TEMP_FREEZING, 5,
        loadIcon("icons/temp/Freezing.png"));
    static { temperature.add(TEMP_FREEZING); }
    public static final Temperature TEMP_COLD = new Temperature(2, LanguageStrings.TEMP_COLD, 15,
        loadIcon("icons/temp/Cold.png"));
    static { temperature.add(TEMP_COLD); }
    public static final Temperature TEMP_CHILLY = new Temperature(3, LanguageStrings.TEMP_CHILLY, 20,
        loadIcon("icons/temp/Chilly.png"));
    static { temperature.add(TEMP_CHILLY); }
    public static final Temperature TEMP_GOOD = new Temperature(4, LanguageStrings.TEMP_GOOD, 28,
        loadIcon("icons/temp/Good.png"));
    static { temperature.add(TEMP_GOOD); }
    public static final Temperature TEMP_WARM = new Temperature(5, LanguageStrings.TEMP_WARM, 35,
        loadIcon("icons/temp/Warm.png"));
    static { temperature.add(TEMP_WARM); }
    public static final Temperature TEMP_HOT = new Temperature(6, LanguageStrings.TEMP_HOT, 45,
        loadIcon("icons/temp/Hot.png"));
    static { temperature.add(TEMP_HOT); }    
    public static final Temperature TEMP_BURNING = new Temperature(7, LanguageStrings.TEMP_BURNING, 100,
        loadIcon("icons/temp/Burning.png"));
    static { temperature.add(TEMP_BURNING); }    

    // --- Humidity
    public static final Humidity HUMID_0 = new Humidity(1, LanguageStrings.HUMID_ARID, 0,
        loadIcon("icons/humid/Humid0.png"));
    static { humidity.add(HUMID_0); }    
    public static final Humidity HUMID_1 = new Humidity(2, LanguageStrings.HUMID_DRY, 1, 
        loadIcon("icons/humid/Humid1.png"));
    static { humidity.add(HUMID_1); }
    public static final Humidity HUMID_2 = new Humidity(3, LanguageStrings.HUMID_NORMAL, 2, 
        loadIcon("icons/humid/Humid2.png"));
    static { humidity.add(HUMID_2); }
    public static final Humidity HUMID_3 = new Humidity(4, LanguageStrings.HUMID_HUMID, 3, 
        loadIcon("icons/humid/Humid3.png"));
    static { humidity.add(HUMID_3); }
    public static final Humidity HUMID_4 = new Humidity(5, LanguageStrings.HUMID_MOIST, 4, 
        loadIcon("icons/humid/Humid4.png"));
    static { humidity.add(HUMID_4); }
    public static final Humidity HUMID_5 = new Humidity(6, LanguageStrings.HUMID_SATURATED, 5, 
        loadIcon("icons/humid/Humid5.png"));
    static { humidity.add(HUMID_5); }

    // --- Biomes ---
    public static final Biome BIOME_PLAINS = new Biome(1, LanguageStrings.BIOME_PLAINS, 25, 2, 1f, 1f, 0f, 1,
        loadIcon("icons/biomes/Plains.png"), null);
    static { biomes.add(BIOME_PLAINS); }
    public static final Biome BIOME_FOREST = new Biome(2, LanguageStrings.BIOME_FOREST, 22, 3, 2f, 2f, 0.1f, 1,
        loadIcon("icons/biomes/Forest.png"), null);
    static { biomes.add(BIOME_FOREST); }
    public static final Biome BIOME_JUNGLE = new Biome(3, LanguageStrings.BIOME_JUNGLE, 30, 4, 2.5f, 2.5f, 0.2f, 3,
        loadIcon("icons/biomes/Jungle.png"), null);
    static { biomes.add(BIOME_JUNGLE); }
    public static final Biome BIOME_SWAMP = new Biome(4, LanguageStrings.BIOME_SWAMP, 26, 5, 1.5f, 1.5f, 0f, 2,
        loadIcon("icons/biomes/Swamp.png"), null);
    static { biomes.add(BIOME_SWAMP); }
    public static final Biome BIOME_URBAN = new Biome(5, LanguageStrings.BIOME_URBAN, 28, 1, 0.5f, 0.5f, 0.5f, 2,
        loadIcon("icons/biomes/Urban.png"), null);
    static { biomes.add(BIOME_URBAN); }
    public static final Biome BIOME_TUNDRA = new Biome(6, LanguageStrings.BIOME_TUNDRA, 5, 2, 0.2f, 0.2f, 0.3f, 4,
        loadIcon("icons/biomes/Tundra.png"), null);
    static { biomes.add(BIOME_TUNDRA); }
    public static final Biome BIOME_TAIGA = new Biome(7, LanguageStrings.BIOME_TAIGA, 12, 1, 0.4f, 0.3f, 0.5f, 3,
        loadIcon("icons/biomes/Taiga.png"), null);
    static { biomes.add(BIOME_TAIGA); }
    public static final Biome BIOME_DESERT = new Biome(8, LanguageStrings.BIOME_DESERT, 45, 0, 0.05f, 0.02f, 1f, 4,
        loadIcon("icons/biomes/Dessert.png"), null);
    static { biomes.add(BIOME_DESERT); }
    public static final Biome BIOME_MOUNTAIN = new Biome(9, LanguageStrings.BIOME_MOUNTAIN, 12, 1, 0.1f, 0.3f, 2f, 3,
        loadIcon("icons/biomes/Mountain.png"), null);
    static { biomes.add(BIOME_MOUNTAIN); }
    public static final Biome BIOME_VOLCANIC = new Biome(10, LanguageStrings.BIOME_VOLCANIC, 60, 0, 0.01f, 0.01f, 5f, 5,
        loadIcon("icons/biomes/Volcanic.png"), null);
    static { biomes.add(BIOME_VOLCANIC); }
    public static final Biome BIOME_LAKE = new Biome(11, LanguageStrings.BIOME_LAKE, 25, 5, 0.5f, 0.5f, 0f, 1,
        loadIcon("icons/biomes/Lake.png"), null);
    static { biomes.add(BIOME_LAKE); }
    public static final Biome BIOME_OCEAN = new Biome(12, LanguageStrings.BIOME_OCEAN, 20, 5, 0.2f, 0.2f, 0f, 2,
        loadIcon("icons/biomes/Ocean.png"), null);
    static { biomes.add(BIOME_OCEAN); }

    // --- Resources ---
    public static final ResourceType RESOURCE_PLANT = new ResourceType(1, LanguageStrings.RESOURCE_PLANT, true, false,
        loadIcon("icons/resources/Plant.png"),
        loadIcon("sprites/sources/PlantSmall.png"),
        loadIcon("sprites/sources/PlantMedium.png"),
        loadIcon("sprites/sources/PlantBig.png"),
        loadIcon("sprites/sources/PlantHuge.png"));
    static { resources.add(RESOURCE_PLANT); }
    public static final ResourceType RESOURCE_FUNGI = new ResourceType(2, LanguageStrings.RESOURCE_FUNGI, true, false,
        loadIcon("icons/resources/Mushroom.png"),
        loadIcon("sprites/sources/MushroomSmall.png"),
        loadIcon("sprites/sources/MushroomMedium.png"),
        loadIcon("sprites/sources/MushroomBig.png"),
        loadIcon("sprites/sources/MushroomHuge.png"));
    static { resources.add(RESOURCE_FUNGI); }
    public static final ResourceType RESOURCE_MEAT = new ResourceType(3, LanguageStrings.RESOURCE_MEAT, true, false,
        loadIcon("icons/resources/Protein.png"),
        loadIcon("sprites/sources/ProteinSmall.png"),
        loadIcon("sprites/sources/ProteinMedium.png"),
        loadIcon("sprites/sources/ProteinBig.png"),
        loadIcon("sprites/sources/ProteinHuge.png"));
    static { resources.add(RESOURCE_MEAT); }
    public static final ResourceType RESOURCE_WATER = new ResourceType(4, LanguageStrings.RESOURCE_WATER, true, true,
        loadIcon("icons/resources/Water.png"),
        loadIcon("sprites/sources/WaterSmall.png"),
        loadIcon("sprites/sources/WaterMedium.png"),
        loadIcon("sprites/sources/WaterBig.png"),
        loadIcon("sprites/sources/WaterHuge.png"));
    static { resources.add(RESOURCE_WATER); }
    public static final ResourceType RESOURCE_SYRUP = new ResourceType(5, LanguageStrings.RESOURCE_SYRUP, true, true,
        loadIcon("icons/resources/Syrup.png"),
        loadIcon("sprites/sources/SyrupSmall.png"),
        loadIcon("sprites/sources/SyrupMedium.png"),
        loadIcon("sprites/sources/SyrupBig.png"),
        loadIcon("sprites/sources/SyrupHuge.png"));
    static { resources.add(RESOURCE_SYRUP); }
    public static final ResourceType RESOURCE_RESIN = new ResourceType(6, LanguageStrings.RESOURCE_RESIN, false, true,
        loadIcon("icons/resources/Resin.png"),
        loadIcon("sprites/sources/ResinSmall.png"),
        loadIcon("sprites/sources/ResinMedium.png"),
        loadIcon("sprites/sources/ResinBig.png"),
        loadIcon("sprites/sources/ResinHuge.png"));
    static { resources.add(RESOURCE_RESIN); }
    public static final ResourceType RESOURCE_ROCK = new ResourceType(7, LanguageStrings.RESOURCE_ROCK, false, false,
        loadIcon("icons/resources/Mineral.png"),
        loadIcon("sprites/sources/MineralSmall.png"),
        loadIcon("sprites/sources/MineralMedium.png"),
        loadIcon("sprites/sources/MineralBig.png"),
        loadIcon("sprites/sources/MineralHuge.png"));
    static { resources.add(RESOURCE_ROCK); }

    // --- Times of Day ---
    public static final TimeOfDay TIME_DAY = new TimeOfDay(1, LanguageStrings.TIME_DAY, 1.05f, AssetStyles.OVERLAY_DAY,
        loadIcon("icons/times/Day.png"));
    static { timesOfDay.add(TIME_DAY); }
    public static final TimeOfDay TIME_DUSK = new TimeOfDay(2, LanguageStrings.TIME_DUSK, 0.95f, AssetStyles.OVERLAY_DUSK,
        loadIcon("icons/times/Dusk.png"));
    static { timesOfDay.add(TIME_DUSK); }
    public static final TimeOfDay TIME_NIGHT = new TimeOfDay(3, LanguageStrings.TIME_NIGHT, 0.85f, AssetStyles.OVERLAY_NIGHT,
        loadIcon("icons/times/Night.png"));
    static { timesOfDay.add(TIME_NIGHT); }
    public static final TimeOfDay TIME_DAWN = new TimeOfDay(4, LanguageStrings.TIME_DAWN, 0.90f, AssetStyles.OVERLAY_DAWN,
        loadIcon("icons/times/Dawn.png"));
    static { timesOfDay.add(TIME_DAWN); }
    public static final TimeOfDay TIME_SOLAR_ECLIPSE = new TimeOfDay(5, LanguageStrings.TIME_SOLAR_ECLIPSE, 0.7f, AssetStyles.OVERLAY_SOLAR_ECLIPSE,
        loadIcon("icons/times/SolarEclipse.png"));
    static { timesOfDay.add(TIME_SOLAR_ECLIPSE); }
    public static final TimeOfDay TIME_LUNAR_ECLIPSE = new TimeOfDay(6, LanguageStrings.TIME_LUNAR_ECLIPSE, 0.85f, AssetStyles.OVERLAY_LUNAR_ECLIPSE,
        loadIcon("icons/times/LunarEclipse.png"));
    static { timesOfDay.add(TIME_LUNAR_ECLIPSE); }

    // --- Moon Phases ---
    public static final MoonPhase PHASE_NEW_MOON = new MoonPhase(1, LanguageStrings.MOON_NEW, 1f, 
        loadIcon("icons/moon/NewMoon.png"));
    static { moonPhases.add(PHASE_NEW_MOON); }
    public static final MoonPhase PHASE_WAXING_CRESCENT = new MoonPhase(2, LanguageStrings.MOON_WAXING_CRESCENT, 3/4f,
        loadIcon("icons/moon/WaxingCrescent.png"));
    static { moonPhases.add(PHASE_WAXING_CRESCENT); }
    public static final MoonPhase PHASE_FIRST_QUARTER = new MoonPhase(3, LanguageStrings.MOON_FIRST_QUARTER, 1/2f, 
        loadIcon("icons/moon/FirstQuarter.png"));
    static { moonPhases.add(PHASE_FIRST_QUARTER); }
    public static final MoonPhase PHASE_WAXING_GIBBOUS = new MoonPhase(4, LanguageStrings.MOON_WAXING_GIBBOUS, 1/4f, 
        loadIcon("icons/moon/WaxingGibbous.png"));
    static { moonPhases.add(PHASE_WAXING_GIBBOUS); }
    public static final MoonPhase PHASE_FULL_MOON = new MoonPhase(5, LanguageStrings.MOON_FULL, 0f, 
        loadIcon("icons/moon/FullMoon.png"));
    static { moonPhases.add(PHASE_FULL_MOON); }
    public static final MoonPhase PHASE_WANING_GIBBOUS = new MoonPhase(6, LanguageStrings.MOON_WANING_GIBBOUS, 1/4f, 
        loadIcon("icons/moon/WaningGibbous.png"));
    static { moonPhases.add(PHASE_WANING_GIBBOUS); }
    public static final MoonPhase PHASE_LAST_QUARTER = new MoonPhase(7, LanguageStrings.MOON_LAST_QUARTER, 1/2f, 
        loadIcon("icons/moon/ThirdQuarter.png"));
    static { moonPhases.add(PHASE_LAST_QUARTER); }
    public static final MoonPhase PHASE_WANING_CRESCENT = new MoonPhase(8, LanguageStrings.MOON_WANING_CRESCENT, 3/4f, 
        loadIcon("icons/moon/WaningCrescent.png"));
    static { moonPhases.add(PHASE_WANING_CRESCENT); }

    // --- Seasons ---
    public static final Season SEASON_SPRING = new Season(1, LanguageStrings.SEASON_SPRING, 1.0f, 1.0f, 
        loadIcon("icons/seasons/Spring.png"));
    static { seasons.add(SEASON_SPRING); }
    public static final Season SEASON_SUMMER = new Season(2, LanguageStrings.SEASON_SUMMER, 1.15f, 0.8f, 
        loadIcon("icons/seasons/Summer.png"));
    static { seasons.add(SEASON_SUMMER); }
    public static final Season SEASON_AUTUMN = new Season(3, LanguageStrings.SEASON_AUTUMN, 0.95f, 1.1f, 
        loadIcon("icons/seasons/Autumn.png"));
    static { seasons.add(SEASON_AUTUMN); }
    public static final Season SEASON_WINTER = new Season(4, LanguageStrings.SEASON_WINTER, 0.7f, 1.2f, 
        loadIcon("icons/seasons/Winter.png"));
    static { seasons.add(SEASON_WINTER); }

    // --- Weather ---
    public static final Weather WEATHER_CLEAR = new Weather(1, LanguageStrings.WEATHER_CLEAR, 0, 1.0f, AssetStyles.OVERLAY_CLEAR,
        loadIcon("icons/weather/Clear.png"));
    static { weathers.add(WEATHER_CLEAR); }
    public static final Weather WEATHER_RAIN = new Weather(2, LanguageStrings.WEATHER_RAIN, 1, 0.95f, AssetStyles.OVERLAY_RAIN,
        loadIcon("icons/weather/Rain.png"));
    static { weathers.add(WEATHER_RAIN); }
    public static final Weather WEATHER_SNOW = new Weather(3, LanguageStrings.WEATHER_SNOW, 1, 0.8f, AssetStyles.OVERLAY_SNOW,
        loadIcon("icons/weather/Snow.png"));
    static { weathers.add(WEATHER_SNOW); }
    public static final Weather WEATHER_HEAVY_RAIN = new Weather(4, LanguageStrings.WEATHER_HEAVY_RAIN, 2, 0.9f, AssetStyles.OVERLAY_HEAVY_RAIN,
        loadIcon("icons/weather/HeavyRain.png"));
    static { weathers.add(WEATHER_HEAVY_RAIN); }
    public static final Weather WEATHER_THUNDER = new Weather(5, LanguageStrings.WEATHER_THUNDER, 2, 0.9f, AssetStyles.OVERLAY_THUNDER,
        loadIcon("icons/weather/Thunder.png"));
    static { weathers.add(WEATHER_THUNDER); }
    public static final Weather WEATHER_HEAVY_SNOW = new Weather(6, LanguageStrings.WEATHER_HEAVY_SNOW, 2, 0.7f, AssetStyles.OVERLAY_HEAVY_SNOW,
        loadIcon("icons/weather/HeavySnow.png"));
    static { weathers.add(WEATHER_HEAVY_SNOW); }
    public static final Weather WEATHER_WIND = new Weather(7, LanguageStrings.WEATHER_WIND, -1, 0.95f, AssetStyles.OVERLAY_WIND,
        loadIcon("icons/weather/HeavyWind.png"));
        static { weathers.add(WEATHER_WIND); }
    public static final Weather WEATHER_HEAT = new Weather(8, LanguageStrings.WEATHER_HEAT, -2, 1.2f, AssetStyles.OVERLAY_HEAT,
        loadIcon("icons/weather/HeatWave.png"));
    static { weathers.add(WEATHER_HEAT); }
    public static final Weather WEATHER_FOG = new Weather(9, LanguageStrings.WEATHER_FOG, 0, 0.9f, AssetStyles.OVERLAY_FOG,
        loadIcon("icons/weather/Fog.png"));
    static { weathers.add(WEATHER_FOG); }
    public static final Weather WEATHER_FROG = new Weather(10, LanguageStrings.WEATHER_FROG, 3, 1.0f, AssetStyles.OVERLAY_FROG,
        loadIcon("icons/weather/FrogRain.png"));
    static { weathers.add(WEATHER_FROG); }
    public static final Weather WEATHER_BLOOD = new Weather(11, LanguageStrings.WEATHER_BLOOD, 2, 0.85f, AssetStyles.OVERLAY_BLOOD,
        loadIcon("icons/weather/BloodRain.png"));
    static { weathers.add(WEATHER_BLOOD); }
    public static final Weather WEATHER_SAND_STORM = new Weather(12, LanguageStrings.WEATHER_SAND_STORM, -1, 0.8f, AssetStyles.OVERLAY_SANDSTORM,
        loadIcon("icons/weather/SandStorm.png"));
    static { weathers.add(WEATHER_SAND_STORM); }
    public static final Weather WEATHER_PYROCLASTIC_FOG = new Weather(13, LanguageStrings.WEATHER_PYROCLASTIC_FOG, 0, 0.5f, AssetStyles.OVERLAY_PYROCLASTIC,
        loadIcon("icons/weather/PyroFog.png"));
    static { weathers.add(WEATHER_PYROCLASTIC_FOG); }
    public static final Weather WEATHER_ACID_RAIN = new Weather(14, LanguageStrings.WEATHER_ACID_RAIN, 1, 0.75f, AssetStyles.OVERLAY_ACID_RAIN,
        loadIcon("icons/weather/AcidRain.png"));
    static { weathers.add(WEATHER_ACID_RAIN); }

    // --- Ant Status ---
    public static final AntStatus STATUS_ALIVE = new AntStatus(1, LanguageStrings.STATUS_ALIVE, 
        loadIcon("icons/status/Alive.png"));
    static { antStatuses.add(STATUS_ALIVE); }
    public static final AntStatus STATUS_DEAD = new AntStatus(2, LanguageStrings.STATUS_DEAD, 
        loadIcon("icons/status/Dead.png"));
    static { antStatuses.add(STATUS_DEAD); }
    public static final AntStatus STATUS_ZOMBIFIED = new AntStatus(3, LanguageStrings.STATUS_ZOMBIFIED, 
        loadIcon("icons/status/Zombified.png"));
    static { antStatuses.add(STATUS_ZOMBIFIED); }

    // --- Move Status ---
    public static final MoveStatus MOVE_STATIC = new MoveStatus(1, LanguageStrings.MOVE_STATIC, 0, loadIcon("icons/moves/Static.png"));
    static { moveStatuses.add(MOVE_STATIC); }
    public static final MoveStatus MOVE_WANDER = new MoveStatus(2, LanguageStrings.MOVE_WANDER, 1/2f, loadIcon("icons/moves/Wander.png"));
    static { moveStatuses.add(MOVE_WANDER); }
    public static final MoveStatus MOVE_MARCH = new MoveStatus(3, LanguageStrings.MOVE_MARCH, 1f, loadIcon("icons/moves/March.png"));
    static { moveStatuses.add(MOVE_MARCH); }
    public static final MoveStatus MOVE_SPEED = new MoveStatus(4, LanguageStrings.MOVE_SPEED, 3/2f, loadIcon("icons/moves/Speed.png"));
    static { moveStatuses.add(MOVE_SPEED); }
    public static final MoveStatus MOVE_FLY = new MoveStatus(5, LanguageStrings.MOVE_FLY, 3f, loadIcon("icons/moves/Fly.png"));
    static { moveStatuses.add(MOVE_FLY); }
    public static final MoveStatus MOVE_PATROL = new MoveStatus(6, LanguageStrings.MOVE_PATROL, 1f, loadIcon("icons/moves/Patrol.png"));
    static { moveStatuses.add(MOVE_PATROL); }

    // --- Bug Types ---
    public static final BugType TYPE_ANT = new BugType(1, LanguageStrings.BUG_ANT, LanguageStrings.BUG_ANT_SCIENTIFIC, 1, 1, 1, 1, 1, 1,
        loadIcon("icons/ants/omni/Worker.png"), loadIcon("sprites/ants/omni/Worker.png"));
    static { bugTypes.add(TYPE_ANT); }
    public static final BugType TYPE_APHID = new BugType(2, LanguageStrings.BUG_APHID, LanguageStrings.BUG_APHID_SCIENTIFIC, 1, 1, 0, 0, 5, 0.5f,
        loadIcon("icons/bugs/Aphid.png") , loadIcon("sprites/bugs/Aphid.png"));
    static { bugTypes.add(TYPE_APHID); }
    public static final BugType TYPE_PARASITE_ANT = new BugType(3, LanguageStrings.BUG_PARASITE_ANT, LanguageStrings.BUG_PARASITE_ANT_SCIENTIFIC, 1, 1, 0, 0, 1, 1,
        loadIcon("icons/bugs/Parasite.png") , loadIcon("sprites/bugs/Parasite.png"));
    static { bugTypes.add(TYPE_PARASITE_ANT); }
    public static final BugType TYPE_SYMBIOTIC_MITE = new BugType(4, LanguageStrings.BUG_SYMBIOTIC_MITE, LanguageStrings.BUG_SYMBIOTIC_MITE_SCIENTIFIC, 1, 0, 0, 0, 2, 0.4f, 
        loadIcon("icons/bugs/SoilMite.png"), loadIcon("sprites/bugs/SoilMite.png"));
    static { bugTypes.add(TYPE_SYMBIOTIC_MITE); }
    public static final BugType TYPE_DERMESTID = new BugType(5, LanguageStrings.BUG_DERMESTID, LanguageStrings.BUG_DERMESTID_SCIENTIFIC, 1, 0, 0, 0, 3, 0.35f,
            loadIcon("icons/bugs/Dermestid.png"), loadIcon("sprites/bugs/Dermestid.png"));
    static { bugTypes.add(TYPE_DERMESTID); }
    public static final BugType TYPE_PARASITIC_MITE = new BugType(6, LanguageStrings.BUG_PARASITIC_MITE, LanguageStrings.BUG_PARASITIC_MITE_SCIENTIFIC, 1, 0, 0, 0, 1, 0.25f,
            loadIcon("icons/bugs/ParasiticMite.png"), loadIcon("sprites/bugs/ParasiticMite.png"));
    static { bugTypes.add(TYPE_PARASITIC_MITE); }

    static {
        for (Biome biome : biomes) {
            biome.setNativeBugs(buildNativeBugsForBiome(biome));
            biome.setNativeParasites(buildNativeParasitesForBiome(biome));
        }
    }

    private static List<BugType> buildNativeBugsForBiome(Biome biome) {
        List<BugType> natives = new ArrayList<>();
        if (!biome.isDry()) {
            natives.add(TYPE_APHID);
        }
        natives.add(TYPE_SYMBIOTIC_MITE);
        if (!biome.isCold()) {
            natives.add(TYPE_DERMESTID);
        }
        return List.copyOf(natives);
    }

    private static List<BugType> buildNativeParasitesForBiome(Biome biome) {
        List<BugType> natives = new ArrayList<>();
        if (biome.isHot()) {
            natives.add(TYPE_PARASITE_ANT);
        }
        if (biome.isCold()) {
            natives.add(TYPE_PARASITIC_MITE);
        }
        return List.copyOf(natives);
    }

    public static Season seasonForMonth(int month) {
        if (month >= 1 && month < 4) {
            return SEASON_SPRING;
        }
        if (month >= 4 && month < 7) {
            return SEASON_SUMMER;
        }
        if (month >= 7 && month < 10) {
            return SEASON_AUTUMN;
        }
        if (month >= 10 && month < 13) {
            return SEASON_WINTER;
        }
        return SEASON_SPRING;
    }

    public static boolean isParasiticAntSeason(Season season) {
        return season == SEASON_SPRING || season == SEASON_SUMMER;
    }

    public static boolean isParasiticMiteSeason(Season season) {
        return season == SEASON_AUTUMN || season == SEASON_WINTER;
    }

    // --- Ant Types ---
    public static final AntType TYPE_EGG = new AntType(1, LanguageStrings.TYPE_EGG, 1f, 0f, 0f, 0f, 0f, 0f, 0f,
        loadIcon("icons/ants/Egg.png"), "Egg.png");
    static { antTypes.add(TYPE_EGG); }    
    public static final AntType TYPE_LARVA = new AntType(2, LanguageStrings.TYPE_LARVA, 1f, 1/2f, 1/2f, 1f, 1f, 1/2f, 1/2f,
        loadIcon("icons/ants/Larva.png"), "Larva.png");
    static { antTypes.add(TYPE_LARVA); }
    public static final AntType TYPE_PUPA = new AntType(3, LanguageStrings.TYPE_PUPA, 1f, 0f, 1f, 0f, 0f, 1/2f, 0f,
        loadIcon("icons/ants/Pupa.png"), "Pupa.png");
    static { antTypes.add(TYPE_PUPA); }
    public static final AntType TYPE_WORKER = new AntType(4, LanguageStrings.TYPE_WORKER, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/omni/Worker.png"), "Worker.png");
    static { antTypes.add(TYPE_WORKER); }
    public static final AntType TYPE_SOLDIER = new AntType(5, LanguageStrings.TYPE_SOLDIER, 3f, 3f, 3f, 2f, 3f, 5f, 3f, 
        loadIcon("icons/ants/omni/Soldier.png"), "Soldier.png");
    static { antTypes.add(TYPE_SOLDIER); }
    public static final AntType TYPE_MAJOR = new AntType(6, LanguageStrings.TYPE_MAJOR, 10f, 15f, 20f, 5f, 2f, 50f, 2f, 
        loadIcon("icons/ants/omni/Major.png"), "Major.png");
    static { antTypes.add(TYPE_MAJOR); }
    public static final AntType TYPE_DRONE = new AntType(7, LanguageStrings.TYPE_DRONE, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/omni/Drone.png"), "Drone.png");
    static { antTypes.add(TYPE_DRONE); }
    public static final AntType TYPE_PRINCESS = new AntType(8, LanguageStrings.TYPE_PRINCESS, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/omni/Princess.png"), "Princess.png");
    static { antTypes.add(TYPE_PRINCESS); }
    public static final AntType TYPE_QUEEN = new AntType(9, LanguageStrings.TYPE_QUEEN, 50f, 2f, 50f, 10f, 1/2f, 50f, 1/4f, 
        loadIcon("icons/ants/omni/Queen.png"), "Queen.png");
    static { antTypes.add(TYPE_QUEEN); }
    public static final AntType TYPE_DEAD = new AntType(10, LanguageStrings.TYPE_DEAD, 0, 0, 0, 0, 0, 0, 0,
        loadIcon("icons/ants/Dead.png"), "Dead.png");
    static { antTypes.add(TYPE_DEAD); }
    public static final AntType TYPE_ZOMBIE = new AntType(11, LanguageStrings.TYPE_ZOMBIE,  1f, 1f, 1f, 1f, 1f, 1f, 1f, 
        loadIcon("icons/ants/Zombie.png"), "Zombie.png");
    static { antTypes.add(TYPE_ZOMBIE); }

    // --- Ant Subtypes ---
    public static final int SUBTYPE_DIGIT_NONE = 1;
    public static final float SUBTYPE_DAMAGE_MULT_STINGER = 4f;
    public static final float SUBTYPE_ATTACK_MULT_TRAPJAW = 3f;
    public static final float SUBTYPE_DEFENSE_MULT_DOORHEAD = 5f;
    public static final float SUBTYPE_FORAGE_MULT_HONEYPOT = 4f;
    public static final float SUBTYPE_SPEED_MULT_HONEYPOT = 0.75f;

    private static final ImageIcon SUBTYPE_ICON_NOTHING = loadIcon("icons/species/Omni.png");

    public static final AntSubtype SUBTYPE_HEAD_NONE = new AntSubtype(1, LanguageStrings.SUBTYPE_NOTHING, AntSubtypeSlot.HEAD,
            SUBTYPE_DIGIT_NONE, null, null, null, 1f, false, 1f, 1f, 1f, null, SUBTYPE_ICON_NOTHING);
    static { antSubtypes.add(SUBTYPE_HEAD_NONE); }
    public static final AntSubtype SUBTYPE_HEAD_TRAPJAW = new AntSubtype(2, LanguageStrings.SUBTYPE_HEAD_TRAPJAW, AntSubtypeSlot.HEAD,
            2, GameUnlocks.ASSIMILATED_TRAPJAW, "trapjaw/", "trapjaw",
            SUBTYPE_ATTACK_MULT_TRAPJAW, true, 1f, 1f, 1f, loadIcon("icons/subtypes/trapjaw.png"));
    static { antSubtypes.add(SUBTYPE_HEAD_TRAPJAW); }
    public static final AntSubtype SUBTYPE_HEAD_DOORHEAD = new AntSubtype(3, LanguageStrings.SUBTYPE_HEAD_DOORHEAD, AntSubtypeSlot.HEAD,
            3, GameUnlocks.ASSIMILATED_DOORHEAD, "turtle/", "doorhead",
            1f, false, SUBTYPE_DEFENSE_MULT_DOORHEAD, 1f, 1f, loadIcon("icons/subtypes/doorhead.png"));
    static { antSubtypes.add(SUBTYPE_HEAD_DOORHEAD); }

    public static final AntSubtype SUBTYPE_TORSO_NONE = new AntSubtype(4, LanguageStrings.SUBTYPE_NOTHING, AntSubtypeSlot.TORSO,
            SUBTYPE_DIGIT_NONE, null, null, null, 1f, false, 1f, 1f, 1f, null, SUBTYPE_ICON_NOTHING);
    static { antSubtypes.add(SUBTYPE_TORSO_NONE); }

    public static final AntSubtype SUBTYPE_ABDOMEN_NONE = new AntSubtype(5, LanguageStrings.SUBTYPE_NOTHING, AntSubtypeSlot.ABDOMEN,
            SUBTYPE_DIGIT_NONE, null, null, null, 1f, false, 1f, 1f, 1f, null, SUBTYPE_ICON_NOTHING);
    static { antSubtypes.add(SUBTYPE_ABDOMEN_NONE); }
    public static final AntSubtype SUBTYPE_ABDOMEN_STINGER = new AntSubtype(6, LanguageStrings.SUBTYPE_ABDOMEN_STINGER, AntSubtypeSlot.ABDOMEN,
            2, GameUnlocks.ASSIMILATED_STINGING, "bullet/", "bullet",
            SUBTYPE_DAMAGE_MULT_STINGER, false, 1f, 1f, 1f, loadIcon("icons/subtypes/bullet.png"));
    static { antSubtypes.add(SUBTYPE_ABDOMEN_STINGER); }
    public static final AntSubtype SUBTYPE_ABDOMEN_HONEYPOT = new AntSubtype(7, LanguageStrings.SUBTYPE_ABDOMEN_HONEYPOT, AntSubtypeSlot.ABDOMEN,
            3, GameUnlocks.ASSIMILATED_HONEYPOT, "honeypot/", "honeypot",
            1f, false, 1f, SUBTYPE_SPEED_MULT_HONEYPOT, SUBTYPE_FORAGE_MULT_HONEYPOT, loadIcon("icons/subtypes/honeypot.png"));
    static { antSubtypes.add(SUBTYPE_ABDOMEN_HONEYPOT); }

    public static final AntSubtype SUBTYPE_OTHER_NONE = new AntSubtype(8, LanguageStrings.SUBTYPE_NOTHING, AntSubtypeSlot.OTHER,
            SUBTYPE_DIGIT_NONE, null, null, null, 1f, false, 1f, 1f, 1f, null, SUBTYPE_ICON_NOTHING);
    static { antSubtypes.add(SUBTYPE_OTHER_NONE); }

    // --- Ant Roles ---
    public static final AntRole ROLE_FORAGER = new AntRole(1, TYPE_WORKER, LanguageStrings.ROLE_FORAGER, loadIcon("icons/roles/Forager.png"));
    static { antRoles.add(ROLE_FORAGER); }
    public static final AntRole ROLE_NURSE = new AntRole(2, TYPE_WORKER, LanguageStrings.ROLE_NURSE, loadIcon("icons/roles/Nurse.png"));
    static { antRoles.add(ROLE_NURSE); }
    public static final AntRole ROLE_BUILDER = new AntRole(3, TYPE_WORKER, LanguageStrings.ROLE_BUILDER, loadIcon("icons/roles/Builder.png"));
    static { antRoles.add(ROLE_BUILDER); }
    public static final AntRole ROLE_SCOUT = new AntRole(4, TYPE_WORKER, LanguageStrings.ROLE_SCOUT, loadIcon("icons/roles/Scout.png"));
    static { antRoles.add(ROLE_SCOUT); }
    public static final AntRole ROLE_FARMER = new AntRole(5, TYPE_WORKER, LanguageStrings.ROLE_FARMER, loadIcon("icons/roles/Farmer.png"));
    static { antRoles.add(ROLE_FARMER); }
    public static final AntRole ROLE_RANCHER = new AntRole(6, TYPE_WORKER, LanguageStrings.ROLE_RANCHER, loadIcon("icons/roles/Rancher.png"));
    static { antRoles.add(ROLE_RANCHER); }
    public static final AntRole ROLE_GRAVER = new AntRole(7, TYPE_WORKER, LanguageStrings.ROLE_GRAVER, loadIcon("icons/roles/Graver.png"));
    static { antRoles.add(ROLE_GRAVER); }
    public static final AntRole ROLE_MINER = new AntRole(8, TYPE_WORKER, LanguageStrings.ROLE_MINER, loadIcon("icons/roles/Miner.png"));
    static { antRoles.add(ROLE_MINER); }
    public static final AntRole ROLE_COURIER = new AntRole(9, TYPE_WORKER, LanguageStrings.ROLE_COURIER, loadIcon("icons/roles/Courier.png"));
    static { antRoles.add(ROLE_COURIER); }
    public static final AntRole ROLE_POTTER = new AntRole(10, TYPE_WORKER, LanguageStrings.ROLE_POTTER, loadIcon("icons/roles/Potter.png"));
    static { antRoles.add(ROLE_POTTER); }
    public static final AntRole ROLE_WARRIOR = new AntRole(12, TYPE_SOLDIER, LanguageStrings.ROLE_WARRIOR, loadIcon("icons/roles/Warrior.png"));
    static { antRoles.add(ROLE_WARRIOR); }
    public static final AntRole ROLE_DEFENDER = new AntRole(13, TYPE_SOLDIER, LanguageStrings.ROLE_DEFENDER, loadIcon("icons/roles/Defender.png"));
    static { antRoles.add(ROLE_DEFENDER); }
    public static final AntRole ROLE_POLICE = new AntRole(14, TYPE_SOLDIER, LanguageStrings.ROLE_POLICE, loadIcon("icons/roles/Police.png"));
    static { antRoles.add(ROLE_POLICE); }
    public static final AntRole ROLE_BOMBER = new AntRole(15, TYPE_SOLDIER, LanguageStrings.ROLE_BOMBER, loadIcon("icons/roles/Bomber.png"));
    static { antRoles.add(ROLE_BOMBER); }
    public static final AntRole ROLE_HUNTER = new AntRole(16, TYPE_SOLDIER, LanguageStrings.ROLE_HUNTER, loadIcon("icons/roles/Hunter.png"));
    static { antRoles.add(ROLE_HUNTER); }
    public static final AntRole ROLE_BRUTE = new AntRole(17, TYPE_MAJOR, LanguageStrings.ROLE_BRUTE, loadIcon("icons/roles/Brute.png"));
    static { antRoles.add(ROLE_BRUTE); }
    public static final AntRole ROLE_CARRIER = new AntRole(18, TYPE_MAJOR, LanguageStrings.ROLE_CARRIER, loadIcon("icons/roles/Carrier.png"));
    static { antRoles.add(ROLE_CARRIER); }
    public static final AntRole ROLE_ARTILLERY = new AntRole(19, TYPE_MAJOR, LanguageStrings.ROLE_ARTILLERY, loadIcon("icons/roles/Artillery.png"));
    static { antRoles.add(ROLE_ARTILLERY); }
    public static final AntRole ROLE_SIEGE = new AntRole(20, TYPE_MAJOR, LanguageStrings.ROLE_SIEGE, loadIcon("icons/roles/Siege.png"));
    static { antRoles.add(ROLE_SIEGE); }
    public static final AntRole ROLE_BORER = new AntRole(21, TYPE_MAJOR, LanguageStrings.ROLE_BORER, loadIcon("icons/roles/Borer.png"));
    static { antRoles.add(ROLE_BORER); }
    public static final AntRole ROLE_DRONE = new AntRole(22, TYPE_DRONE, LanguageStrings.ROLE_DRONE, loadIcon("icons/roles/Drone.png"));
    static { antRoles.add(ROLE_DRONE); }
    public static final AntRole ROLE_BREEDER = new AntRole(23, TYPE_PRINCESS, LanguageStrings.ROLE_BREEDER, loadIcon("icons/roles/Breeder.png"));
    static { antRoles.add(ROLE_BREEDER); }
    public static final AntRole ROLE_DIPLOMAT = new AntRole(24, TYPE_PRINCESS, LanguageStrings.ROLE_DIPLOMAT, loadIcon("icons/roles/Diplomat.png"));
    static { antRoles.add(ROLE_DIPLOMAT); }
    public static final AntRole ROLE_LAYER = new AntRole(25, TYPE_QUEEN, LanguageStrings.ROLE_LAYER, loadIcon("icons/roles/Layer.png"));
    static { antRoles.add(ROLE_LAYER); }
    public static final AntRole ROLE_RESEARCHER = new AntRole(26, TYPE_QUEEN, LanguageStrings.ROLE_RESEARCHER, loadIcon("icons/roles/Researcher.png"));
    static { antRoles.add(ROLE_RESEARCHER); }
    public static final AntRole ROLE_MILITIA = new AntRole(27, TYPE_WORKER, LanguageStrings.ROLE_MILITIA, loadIcon("icons/roles/Militia.png"));
    static { antRoles.add(ROLE_MILITIA); }
    public static final AntRole ROLE_CATCHER = new AntRole(28, TYPE_SOLDIER, LanguageStrings.ROLE_CATCHER, loadIcon("icons/roles/Catcher.png"));
    static { antRoles.add(ROLE_CATCHER); }
    public static final AntRole ROLE_CRANE = new AntRole(29, TYPE_MAJOR, LanguageStrings.ROLE_CRANE, loadIcon("icons/roles/Crane.png"));
    static { antRoles.add(ROLE_CRANE); }
    public static final AntRole ROLE_TRANSPORT = new AntRole(30, TYPE_MAJOR, LanguageStrings.ROLE_TRANSPORT, loadIcon("icons/roles/Transport.png"));
    static { antRoles.add(ROLE_TRANSPORT); }
    public static final AntRole ROLE_ASSISTANT = new AntRole(31, TYPE_PRINCESS, LanguageStrings.ROLE_ASSISTANT, loadIcon("icons/roles/Assistant.png"));
    static { antRoles.add(ROLE_ASSISTANT); }
    public static final AntRole ROLE_ESCORT = new AntRole(32, TYPE_SOLDIER, LanguageStrings.ROLE_ESCORT, loadIcon("icons/roles/Escort.png"));
    static { antRoles.add(ROLE_ESCORT); }
    public static final AntRole ROLE_ENGINEER = new AntRole(33, TYPE_WORKER, LanguageStrings.ROLE_ENGINEER, loadIcon("icons/roles/Engineer.png"));
    static { antRoles.add(ROLE_ENGINEER); }
    public static final AntRole ROLE_SKYTRANS = new AntRole(34, TYPE_PRINCESS, LanguageStrings.ROLE_SKYTRANS, loadIcon("icons/roles/Skytrans.png"));
    static { antRoles.add(ROLE_SKYTRANS); }

    // --- Colony Ranks ---
    public static final ColonyRank RANK_ANT = new ColonyRank(1, LanguageStrings.RANK_ANT, 1l, 
        loadIcon("icons/ranks/Ant.png"));
    static { colonyRanks.add(RANK_ANT); }
    public static final ColonyRank RANK_COLONY = new ColonyRank(2, LanguageStrings.RANK_COLONY, 10l, 
        loadIcon("icons/ranks/Colony.png"));
    static { colonyRanks.add(RANK_COLONY); }
    public static final ColonyRank RANK_COUNTY = new ColonyRank(3, LanguageStrings.RANK_COUNTY, 100l, 
        loadIcon("icons/ranks/County.png"));
    static { colonyRanks.add(RANK_COUNTY); }
    public static final ColonyRank RANK_DUCHY = new ColonyRank(4, LanguageStrings.RANK_DUCHY, 1000l, 
        loadIcon("icons/ranks/Duchy.png"));
    static { colonyRanks.add(RANK_DUCHY); }
    public static final ColonyRank RANK_KINGDOM = new ColonyRank(5, LanguageStrings.RANK_KINGDOM, 10000l, 
        loadIcon("icons/ranks/Kingdom.png"));
    static { colonyRanks.add(RANK_KINGDOM); }
    public static final ColonyRank RANK_EMPIRE = new ColonyRank(6, LanguageStrings.RANK_EMPIRE, 100000l, 
        loadIcon("icons/ranks/Empire.png"));
    static { colonyRanks.add(RANK_EMPIRE); }
    public static final ColonyRank RANK_SUPER = new ColonyRank(7, LanguageStrings.RANK_SUPER, 1000000l, 
        loadIcon("icons/ranks/Super.png"));
    static { colonyRanks.add(RANK_SUPER); }
    public static final ColonyRank RANK_ULTRA = new ColonyRank(8, LanguageStrings.RANK_ULTRA, 10000000l, 
        loadIcon("icons/ranks/Ultra.png"));
    static { colonyRanks.add(RANK_ULTRA); }
    public static final ColonyRank RANK_HYPER = new ColonyRank(9, LanguageStrings.RANK_HYPER, 100000000l, 
        loadIcon("icons/ranks/Hyper.png"));
    static { colonyRanks.add(RANK_HYPER); }
    public static final ColonyRank RANK_MEGA = new ColonyRank(10, LanguageStrings.RANK_MEGA, 1000000000l, 
        loadIcon("icons/ranks/Mega.png"));
    static { colonyRanks.add(RANK_MEGA); }
    public static final ColonyRank RANK_ULTIMATE = new ColonyRank(11, LanguageStrings.RANK_ULTIMATE, 10000000000l, 
        loadIcon("icons/ranks/Ultimate.png"));
    static { colonyRanks.add(RANK_ULTIMATE); }
    public static final ColonyRank RANK_SUPREME = new ColonyRank(12, LanguageStrings.RANK_SUPREME, 100000000000l, 
        loadIcon("icons/ranks/Supreme.png"));
    static { colonyRanks.add(RANK_SUPREME); }
    public static final ColonyRank RANK_GIGA = new ColonyRank(13, LanguageStrings.RANK_GIGA, 1000000000000l, 
        loadIcon("icons/ranks/Giga.png"));
    static { colonyRanks.add(RANK_GIGA); }

    // --- Game speeds (tick delay ms; icons under icons/speed/) ---
    public static final GameSpeed SPEED_VERY_SLOW = new GameSpeed(
            GameSpeed.ID_VERY_SLOW, LanguageStrings.UI_SPEED_VERY_SLOW, 1000,
            loadIcon("icons/speed/VerySlow.png"));
    static { gameSpeeds.add(SPEED_VERY_SLOW); }
    public static final GameSpeed SPEED_SLOW = new GameSpeed(
            GameSpeed.ID_SLOW, LanguageStrings.UI_SPEED_SLOW, 500,
            loadIcon("icons/speed/Slow.png"));
    static { gameSpeeds.add(SPEED_SLOW); }
    public static final GameSpeed SPEED_NORMAL = new GameSpeed(
            GameSpeed.ID_NORMAL, LanguageStrings.UI_SPEED_NORMAL, 250,
            loadIcon("icons/speed/Normal.png"));
    static { gameSpeeds.add(SPEED_NORMAL); }
    public static final GameSpeed SPEED_FAST = new GameSpeed(
            GameSpeed.ID_FAST, LanguageStrings.UI_SPEED_FAST, 100,
            loadIcon("icons/speed/Fast.png"));
    static { gameSpeeds.add(SPEED_FAST); }
    public static final GameSpeed SPEED_VERY_FAST = new GameSpeed(
            GameSpeed.ID_VERY_FAST, LanguageStrings.UI_SPEED_VERY_FAST, 50,
            loadIcon("icons/speed/VeryFast.png"));
    static { gameSpeeds.add(SPEED_VERY_FAST); }
    public static final GameSpeed SPEED_TURBO = new GameSpeed(
            GameSpeed.ID_TURBO, LanguageStrings.UI_SPEED_TURBO, 1,
            loadIcon("icons/speed/Turbo.png"));
    static { gameSpeeds.add(SPEED_TURBO); }
    public static final ImageIcon ICON_SPEED_ZERO = loadIcon("icons/speed/Zero.png");
    public static final ImageIcon ICON_SPEED_UP = loadIcon("icons/speed/SpeedUp.png");
    public static final ImageIcon ICON_SPEED_DOWN = loadIcon("icons/speed/SpeedDown.png");
    public static final ImageIcon ICON_SPEED_PLAY = loadIcon("icons/speed/Play.png");
    public static final ImageIcon ICON_SPEED_PAUSE = loadIcon("icons/speed/Pause.png");

    public static final int DIPLOMATIC_REPUTATION_MIN = 0;
    public static final int DIPLOMATIC_REPUTATION_MAX = 100;
    public static final int DIPLOMATIC_REPUTATION_TIER_STEP = 20;
    public static final int DEFAULT_DIPLOMATIC_REPUTATION = 50;

    // --- Diplomatic Reputation ---
    public static final DiplomaticReputation REPUTATION_AGGRESSIVE = new DiplomaticReputation(
        1, LanguageStrings.REPUTATION_AGGRESSIVE, 0, loadIcon("icons/diplomacy/Aggressive.png"));
    static { diplomaticReputations.add(REPUTATION_AGGRESSIVE); }
    public static final DiplomaticReputation REPUTATION_WARY = new DiplomaticReputation(
        2, LanguageStrings.REPUTATION_WARY, 20, loadIcon("icons/diplomacy/Wary.png"));
    static { diplomaticReputations.add(REPUTATION_WARY); }
    public static final DiplomaticReputation REPUTATION_NEUTRAL = new DiplomaticReputation(
        3, LanguageStrings.REPUTATION_NEUTRAL, 40, loadIcon("icons/diplomacy/Neutral.png"));
    static { diplomaticReputations.add(REPUTATION_NEUTRAL); }
    public static final DiplomaticReputation REPUTATION_CORDIAL = new DiplomaticReputation(
        4, LanguageStrings.REPUTATION_CORDIAL, 60, loadIcon("icons/diplomacy/Cordial.png"));
    static { diplomaticReputations.add(REPUTATION_CORDIAL); }
    public static final DiplomaticReputation REPUTATION_FRIENDLY = new DiplomaticReputation(
        5, LanguageStrings.REPUTATION_FRIENDLY, 80, loadIcon("icons/diplomacy/Friendly.png"));
    static { diplomaticReputations.add(REPUTATION_FRIENDLY); }

    public static final String DIPLO_EXCLUSIVE_PACT = "pact";

    /** Stored remaining-days sentinel: modifier does not expire on a timer. */
    public static final int MODIFIER_PERMANENT = -1;

    // --- Diplomatic reputation modifiers ---
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_PACT = new DiplomaticReputationModifier(
        1, LanguageStrings.DIPLO_MODIFIER_PACT, 20, REPUTATION_CORDIAL.getMinScore(), DIPLO_EXCLUSIVE_PACT);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_PACT); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_BROKEN_PACT = new DiplomaticReputationModifier(
        2, LanguageStrings.DIPLO_MODIFIER_BROKEN_PACT, -30, 0, DIPLO_EXCLUSIVE_PACT);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_BROKEN_PACT); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_DECLINED_PACT = new DiplomaticReputationModifier(
        7, LanguageStrings.DIPLO_MODIFIER_DECLINED_PACT, -10, 0, DIPLO_EXCLUSIVE_PACT, 30);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_DECLINED_PACT); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_WAR = new DiplomaticReputationModifier(
        3, LanguageStrings.DIPLO_MODIFIER_WAR, -200, 0, DIPLO_EXCLUSIVE_PACT);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_WAR); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_TRADE = new DiplomaticReputationModifier(
        4, LanguageStrings.DIPLO_MODIFIER_TRADE, 20, REPUTATION_NEUTRAL.getMinScore(), null);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_TRADE); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_TRADE_REQUEST = new DiplomaticReputationModifier(
        5, LanguageStrings.DIPLO_MODIFIER_TRADE_REQUEST, -5, REPUTATION_NEUTRAL.getMinScore(), null, 30);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_TRADE_REQUEST); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_TRADE_OFFER = new DiplomaticReputationModifier(
        10, LanguageStrings.DIPLO_MODIFIER_TRADE_OFFER, 5, REPUTATION_NEUTRAL.getMinScore(), null);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_TRADE_OFFER); }
    public static final int CROSS_DYNASTY_TRADE_RECEIVER_REP = 20;
    public static final int CROSS_DYNASTY_TRADE_OFFER_SENDER_REP = 5;
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_BORDER_FRICTION = new DiplomaticReputationModifier(
        6, LanguageStrings.DIPLO_MODIFIER_BORDER_FRICTION, -10, 0, null);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_BORDER_FRICTION); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_GRANTED_INDEPENDENCE = new DiplomaticReputationModifier(
        8, LanguageStrings.DIPLO_MODIFIER_GRANTED_INDEPENDENCE, 25, 0, null);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_GRANTED_INDEPENDENCE); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_WAS_AT_WAR = new DiplomaticReputationModifier(
        9, LanguageStrings.DIPLO_MODIFIER_WAS_AT_WAR, -20, 0, null, 360);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_WAS_AT_WAR); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_GENETIC_EXCHANGE = new DiplomaticReputationModifier(
        11, LanguageStrings.DIPLO_MODIFIER_GENETIC_EXCHANGE, 10, 0, null, 180);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_GENETIC_EXCHANGE); }

    public static final int GENETIC_EXCHANGE_DRONE_COST = 25;

    public static final double REBELLION_MONTHLY_CHANCE_MIN = 0.05;
    public static final double REBELLION_MONTHLY_CHANCE_MAX = 0.30;
    public static final double REBELLION_JOIN_BASE_CHANCE = 0.15;
    public static final double REBELLION_JOIN_DISTANCE_WEIGHT = 0.40;
    public static final double REBELLION_JOIN_LOYALTY_WEIGHT = 0.35;
    public static final double REBELLION_NPC_FIGHT_CHANCE_MIN = 0.20;
    public static final double REBELLION_NPC_FIGHT_CHANCE_MAX = 0.80;
    public static final double REBELLION_NPC_FIGHT_RATIO_LOW = 0.85;
    public static final double REBELLION_NPC_FIGHT_RATIO_HIGH = 1.15;

    // --- Genetic integrity modifiers ---
    public static final double GENETIC_INTEGRITY_START = 100.0;
    public static final double GENETIC_INTEGRITY_SATELLITE_PENALTY = 1.0;
    public static final double GENETIC_INTEGRITY_ASSIMILATION_FLOOR_STEP = 5.0;
    public static final GeneticIntegrityModifier GI_MODIFIER_PACT = new GeneticIntegrityModifier(
        1, LanguageStrings.GI_MODIFIER_PACT, 10.0, DIPLO_MODIFIER_PACT.getNameKey());
    static { geneticIntegrityModifiers.add(GI_MODIFIER_PACT); }
    public static final GeneticIntegrityModifier GI_MODIFIER_GENETIC_EXCHANGE = new GeneticIntegrityModifier(
        2, LanguageStrings.GI_MODIFIER_GENETIC_EXCHANGE, 10.0, DIPLO_MODIFIER_GENETIC_EXCHANGE.getNameKey());
    static { geneticIntegrityModifiers.add(GI_MODIFIER_GENETIC_EXCHANGE); }

    public static final int COLONY_LOYALTY_MIN = 0;
    public static final int COLONY_LOYALTY_MAX = 100;
    public static final int COLONY_LOYALTY_TIER_STEP = 20;
    public static final int DEFAULT_COLONY_LOYALTY = 50;

    public static final int MILITARY_WEIGHT_WORKER = 1;
    public static final int MILITARY_WEIGHT_SOLDIER = 5;
    public static final int MILITARY_WEIGHT_MAJOR = 15;
    public static final int MILITARY_WEIGHT_PRINCESS = 10;
    public static final int MILITARY_WEIGHT_QUEEN = 50;

    private static final AntRole[] ACTIVE_MILITARY_ROLES = {
            ROLE_WARRIOR, ROLE_DEFENDER,
            ROLE_MILITIA,
            ROLE_BRUTE, ROLE_ARTILLERY, ROLE_SIEGE
    };

    private static final java.util.Set<AntRole> WAR_ECONOMY_EXCLUSIVE_ROLES =
            java.util.Set.copyOf(java.util.Arrays.asList(ACTIVE_MILITARY_ROLES));

    public static boolean isWarEconomyExclusiveRole(AntRole role) {
        return role != null && WAR_ECONOMY_EXCLUSIVE_ROLES.contains(role);
    }

    public static boolean isActiveMilitaryRole(AntRole role) {
        if (role == null) {
            return false;
        }
        for (AntRole activeRole : ACTIVE_MILITARY_ROLES) {
            if (activeRole == role) {
                return true;
            }
        }
        return false;
    }

    public static AntRole[] getActiveMilitaryRoles() {
        return ACTIVE_MILITARY_ROLES.clone();
    }

    public static int getMilitaryWeightForAntType(AntType type) {
        if (type == TYPE_WORKER) {
            return MILITARY_WEIGHT_WORKER;
        }
        if (type == TYPE_SOLDIER) {
            return MILITARY_WEIGHT_SOLDIER;
        }
        if (type == TYPE_MAJOR) {
            return MILITARY_WEIGHT_MAJOR;
        }
        if (type == TYPE_PRINCESS) {
            return MILITARY_WEIGHT_PRINCESS;
        }
        if (type == TYPE_QUEEN) {
            return MILITARY_WEIGHT_QUEEN;
        }
        return 0;
    }

    public static int getActiveMilitaryRoleWeight(AntRole role) {
        return role != null ? getMilitaryWeightForAntType(role.getAntType()) : 0;
    }

    public static final int MILITARY_BASELINE_HEALTH = 100;
    public static final int MILITARY_BASELINE_ATTACK = 10;
    public static final int MILITARY_BASELINE_DEFENSE = 5;
    public static final int MILITARY_BASELINE_ATTACK_SPEED = 1;
    public static final float ASSIMILATED_DAMAGE_MULT_FIRE = 4f;
    public static final float ASSIMILATED_DAMAGE_ADD_DEADLY = 4f;
    public static final float ASSIMILATED_DAMAGE_SYNERGY_FIRE_DEADLY = 16f;
    public static final float ASSIMILATED_ATTACK_SPEED_MULT_FASTBITE = 3f;
    public static final float MILITARY_STRENGTH_RATIO_MAX = 11f;
    public static final int MILITARY_STRENGTH_DELTA_MAX = 10;
    public static final int LOYALTY_CAPITAL_DISTANCE_NEUTRAL = 1;
    public static final int LOYALTY_CAPITAL_DISTANCE_MAX = 10;
    public static final int LOYALTY_CAPITAL_DISTANCE_PENALTY_MAX = 10;

    public static int getCapitalDistanceLoyaltyPenalty(int hexDistance) {
        if (hexDistance <= LOYALTY_CAPITAL_DISTANCE_NEUTRAL) {
            return 0;
        }
        if (hexDistance >= LOYALTY_CAPITAL_DISTANCE_MAX) {
            return -LOYALTY_CAPITAL_DISTANCE_PENALTY_MAX;
        }
        float normalized = (hexDistance - LOYALTY_CAPITAL_DISTANCE_NEUTRAL)
                / (float) (LOYALTY_CAPITAL_DISTANCE_MAX - LOYALTY_CAPITAL_DISTANCE_NEUTRAL);
        return -Math.round(normalized * LOYALTY_CAPITAL_DISTANCE_PENALTY_MAX);
    }

    public static int axialHexDistance(int q1, int r1, int q2, int r2) {
        int dq = q1 - q2;
        int dr = r1 - r2;
        return (Math.abs(dq) + Math.abs(dr) + Math.abs(dq + dr)) / 2;
    }

    // --- Colony Loyalty ---
    public static final ColonyLoyalty LOYALTY_REBELLIOUS = new ColonyLoyalty(
        1, LanguageStrings.LOYALTY_REBELLIOUS, 0, loadIcon("icons/loyalty/Rebellious.png"));
    static { colonyLoyalties.add(LOYALTY_REBELLIOUS); }
    public static final ColonyLoyalty LOYALTY_DISLOYAL = new ColonyLoyalty(
        2, LanguageStrings.LOYALTY_DISLOYAL, 20, loadIcon("icons/loyalty/Disloyal.png"));
    static { colonyLoyalties.add(LOYALTY_DISLOYAL); }
    public static final ColonyLoyalty LOYALTY_COMPLACENT = new ColonyLoyalty(
        3, LanguageStrings.LOYALTY_COMPLACENT, 40, loadIcon("icons/loyalty/Complacent.png"));
    static { colonyLoyalties.add(LOYALTY_COMPLACENT); }
    public static final ColonyLoyalty LOYALTY_LOYAL = new ColonyLoyalty(
        4, LanguageStrings.LOYALTY_LOYAL, 60, loadIcon("icons/loyalty/Loyal.png"));
    static { colonyLoyalties.add(LOYALTY_LOYAL); }
    public static final ColonyLoyalty LOYALTY_MILITANT = new ColonyLoyalty(
        5, LanguageStrings.LOYALTY_MILITANT, 80, loadIcon("icons/loyalty/Militant.png"));
    static { colonyLoyalties.add(LOYALTY_MILITANT); }

    // --- Colony loyalty modifiers ---
    public static final ColonyLoyaltyModifier LOYALTY_MODIFIER_TRADE = new ColonyLoyaltyModifier(
        1, LanguageStrings.LOYALTY_MODIFIER_TRADE, 10, 0, null);
    static { colonyLoyaltyModifiers.add(LOYALTY_MODIFIER_TRADE); }
    public static final ColonyLoyaltyModifier LOYALTY_MODIFIER_TUNNEL = new ColonyLoyaltyModifier(
        2, LanguageStrings.LOYALTY_MODIFIER_TUNNEL, 10, 0, null);
    static { colonyLoyaltyModifiers.add(LOYALTY_MODIFIER_TUNNEL); }
    public static final ColonyLoyaltyModifier LOYALTY_MODIFIER_CAPITAL = new ColonyLoyaltyModifier(
        3, LanguageStrings.LOYALTY_MODIFIER_CAPITAL, 200, 0, null);
    static { colonyLoyaltyModifiers.add(LOYALTY_MODIFIER_CAPITAL); }
    public static final ColonyLoyaltyModifier LOYALTY_MODIFIER_PHEROMONE_STORM = new ColonyLoyaltyModifier(
        4, LanguageStrings.LOYALTY_MODIFIER_PHEROMONE_STORM, 10, 0, null, 360);
    static { colonyLoyaltyModifiers.add(LOYALTY_MODIFIER_PHEROMONE_STORM); }
    public static final ColonyLoyaltyModifier LOYALTY_MODIFIER_RECENTLY_CONQUERED = new ColonyLoyaltyModifier(
        5, LanguageStrings.LOYALTY_MODIFIER_RECENTLY_CONQUERED, -5, 0, null, 180);
    static { colonyLoyaltyModifiers.add(LOYALTY_MODIFIER_RECENTLY_CONQUERED); }
    public static final ColonyLoyaltyModifier LOYALTY_MODIFIER_RECENTLY_INTEGRATED = new ColonyLoyaltyModifier(
        6, LanguageStrings.LOYALTY_MODIFIER_RECENTLY_INTEGRATED, 25, 0, null, 180);
    static { colonyLoyaltyModifiers.add(LOYALTY_MODIFIER_RECENTLY_INTEGRATED); }

    public static final int PHEROMONE_STORM_SYRUP_COST = 500;
    public static final int PHEROMONE_STORM_DURATION_MONTHS = 12;
    public static final int RECENTLY_CONQUERED_LOYALTY_MONTHS = 6;
    public static final int RECENTLY_INTEGRATED_LOYALTY_MONTHS = 6;

    public static final float INTEGRATION_MILITARY_RATIO_REQUIRED = 5f;
    public static final int INTEGRATION_MONTHS_PER_COLONY_SLOW = 30;
    public static final int INTEGRATION_MIN_DIPLOMATS = 1;
    public static final int DAYS_PER_MONTH = 30;
    public static final int AI_FORCED_FLIGHT_COOLDOWN_DAYS = 30;

    public static int monthsToDays(int months) {
        return Math.max(0, months) * DAYS_PER_MONTH;
    }

    public static int daysToMonthsCeil(int days) {
        if (days <= 0) {
            return 0;
        }
        return (days + DAYS_PER_MONTH - 1) / DAYS_PER_MONTH;
    }

    public static int initialModifierRemainingDays(DiplomaticReputationModifier modifier) {
        if (modifier == null || !modifier.hasExpiration()) {
            return MODIFIER_PERMANENT;
        }
        return modifier.getDurationDays();
    }

    public static int initialModifierRemainingDays(ColonyLoyaltyModifier modifier) {
        if (modifier == null || !modifier.hasExpiration()) {
            return MODIFIER_PERMANENT;
        }
        return modifier.getDurationDays();
    }
    public static final int WAR_PACT_BREAK_COOLDOWN_MONTHS = 6;
    public static final int DIPLO_DECLINED_REQUEST_COOLDOWN_MONTHS = 1;
    public static final int WAS_AT_WAR_MODIFIER_MONTHS = 12;
    public static final int WAR_DECLARATION_MIN_POPULATION = 1000;
    public static final float WAR_STANDING_MILITARY_RATIO = 1.15f;
    public static final double AI_ACCEPT_PEACE_CHANCE = 0.85;
    public static final double AI_DECLARE_WAR_CHANCE = 0.12;
    public static final float AI_DECLARE_WAR_MAX_TARGET_STRENGTH_RATIO = 2f;
    public static final float WAR_BATTLE_RATIO_MAX = 10f;
    public static final float WAR_BATTLE_WIN_CHANCE_AT_PARITY = 0.5f;
    public static final float WAR_BATTLE_LOSS_FRACTION_AT_PARITY = 0.006f;
    public static final float WAR_BATTLE_WINNER_LOSS_FRACTION_MAX = 0.035f;
    public static final float WAR_STAGE_PROGRESS_PER_DAY = 0.035f;
    public static final float WAR_STAGE_PROGRESS_PER_HOUR = WAR_STAGE_PROGRESS_PER_DAY / 24f;
    public static final int WAR_REDEPLOY_HOURS = 24;
    public static final float WAR_AI_FALLBACK_MAX_POWER_RATIO = 1f;
    public static final float WAR_AI_FALLBACK_RECOVERY_RATIO = 0.75f;
    public static final int WAR_AI_FALLBACK_MIN_ACTIVE = 500;
    public static final int WAR_AI_FALLBACK_MIN_SPARE_COLONIES = 3;
    public static final double AI_WAR_FALLBACK_CHANCE = 0.06;

    public static float warBattleWinChance(float strongerOverWeakerRatio) {
        float ratio = Math.max(1f, Math.min(WAR_BATTLE_RATIO_MAX, strongerOverWeakerRatio));
        return WAR_BATTLE_WIN_CHANCE_AT_PARITY
                + (1f - WAR_BATTLE_WIN_CHANCE_AT_PARITY) * (ratio - 1f) / (WAR_BATTLE_RATIO_MAX - 1f);
    }

    public static float warBattleLoserLossFraction(float strongerOverWeakerRatio) {
        float ratio = Math.max(1f, Math.min(WAR_BATTLE_RATIO_MAX, strongerOverWeakerRatio));
        return WAR_BATTLE_LOSS_FRACTION_AT_PARITY
                + (1f - WAR_BATTLE_LOSS_FRACTION_AT_PARITY) * (ratio - 1f) / (WAR_BATTLE_RATIO_MAX - 1f);
    }

    public static float warBattleWinnerLossFraction(float strongerOverWeakerRatio) {
        float loserFraction = warBattleLoserLossFraction(strongerOverWeakerRatio);
        return Math.min(WAR_BATTLE_WINNER_LOSS_FRACTION_MAX, loserFraction * 0.1f);
    }

    public static final int AI_EXPANSION_COLONY_TARGET = 6;
    public static final double AI_CREATINE_FOOD_STRESS_RATIO = 0.35;
    public static final double TRADE_AUTOMATION_SURPLUS_RATIO = 0.50;
    public static final double TRADE_AUTOMATION_DEFICIT_RATIO = 0.25;
    public static final int CREATINE_DIET_PROTEIN_COST = 300;
    public static final int CREATINE_DIET_DURATION_MONTHS = 6;
    public static final float CREATINE_DIET_SPEED_MULTIPLIER = 2f;
    public static final int DIPLOMAT_MAX_PER_DYNASTY_MISSION = 5;
    public static final int DIPLOMAT_MAX_PER_COLONY_MISSION = 3;
    public static final int AUTO_UPGRADE_MIN_COMPLETE_TUNNELS = 5;
    public static final int AUTO_UPGRADE_MIN_DIPLOMATS_SENT = 10;
    public static final int DIPLOMAT_STABILITY_GAIN_BASE = 1;
    public static final int DIPLOMAT_STABILITY_GAIN_PRESSURE_2 = 3;
    public static final int DIPLOMAT_STABILITY_GAIN_PRESSURE_3 = 5;
    
    // --- Dynasty titles ---
    public static final DynastyTitle DYNASTY_TITLE_DYNASTY = new DynastyTitle(
            1, LanguageStrings.DYNASTY_TITLE_DYNASTY, LanguageStrings.DYNASTY_TITLE_FMT_DYNASTY);
    static { dynastyTitles.add(DYNASTY_TITLE_DYNASTY); }

    public static final DynastyTitle DYNASTY_TITLE_CONGLOMERATE = new DynastyTitle(
            2, LanguageStrings.DYNASTY_TITLE_CONGLOMERATE, LanguageStrings.DYNASTY_TITLE_FMT_CONGLOMERATE);
    static { dynastyTitles.add(DYNASTY_TITLE_CONGLOMERATE); }

    public static final DynastyTitle DYNASTY_TITLE_UNION = new DynastyTitle(
            3, LanguageStrings.DYNASTY_TITLE_UNION, LanguageStrings.DYNASTY_TITLE_FMT_UNION);
    static { dynastyTitles.add(DYNASTY_TITLE_UNION); }

    public static final DynastyTitle DYNASTY_TITLE_SYNDICATE = new DynastyTitle(
            4, LanguageStrings.DYNASTY_TITLE_SYNDICATE, LanguageStrings.DYNASTY_TITLE_FMT_SYNDICATE);
    static { dynastyTitles.add(DYNASTY_TITLE_SYNDICATE); }

    public static final DynastyTitle DYNASTY_TITLE_TECHNOCRACY = new DynastyTitle(
            5, LanguageStrings.DYNASTY_TITLE_TECHNOCRACY, LanguageStrings.DYNASTY_TITLE_FMT_TECHNOCRACY);
    static { dynastyTitles.add(DYNASTY_TITLE_TECHNOCRACY); }

    public static final DynastyTitle DYNASTY_TITLE_EMPIRE = new DynastyTitle(
            6, LanguageStrings.DYNASTY_TITLE_EMPIRE, LanguageStrings.DYNASTY_TITLE_FMT_EMPIRE);
    static { dynastyTitles.add(DYNASTY_TITLE_EMPIRE); }

    public static final DynastyTitle DYNASTY_TITLE_DOMINION = new DynastyTitle(
            7, LanguageStrings.DYNASTY_TITLE_DOMINION, LanguageStrings.DYNASTY_TITLE_FMT_DOMINION);
    static { dynastyTitles.add(DYNASTY_TITLE_DOMINION); }

    public static final DynastyTitle DYNASTY_TITLE_NATION = new DynastyTitle(
            11, LanguageStrings.DYNASTY_TITLE_NATION, LanguageStrings.DYNASTY_TITLE_FMT_NATION);
    static { dynastyTitles.add(DYNASTY_TITLE_NATION); }

    public static final DynastyTitle DYNASTY_TITLE_REPUBLIC = new DynastyTitle(
            12, LanguageStrings.DYNASTY_TITLE_REPUBLIC, LanguageStrings.DYNASTY_TITLE_FMT_REPUBLIC);
    static { dynastyTitles.add(DYNASTY_TITLE_REPUBLIC); }

    public static final DynastyTitle CITY_TITLE_CITY = new DynastyTitle(
            8, LanguageStrings.DYNASTY_TITLE_CITY, LanguageStrings.DYNASTY_TITLE_FMT_CITY);
    static { cityTitles.add(CITY_TITLE_CITY); }

    public static final DynastyTitle CITY_TITLE_BERG = new DynastyTitle(
            9, LanguageStrings.DYNASTY_TITLE_BERG, LanguageStrings.DYNASTY_TITLE_FMT_BERG);
    static { cityTitles.add(CITY_TITLE_BERG); }

    public static final DynastyTitle CITY_TITLE_GRAD = new DynastyTitle(
            10, LanguageStrings.DYNASTY_TITLE_GRAD, LanguageStrings.DYNASTY_TITLE_FMT_GRAD);
    static { cityTitles.add(CITY_TITLE_GRAD); }

    // --- Species ---
    public static final Species SPECIES_OMNI = new Species(1, LanguageStrings.SPECIES_OMNI, LanguageStrings.SPECIES_OMNI_SCIENTIFIC,  "omni/", null, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, GameUnlocks.ROLE_FORAGER, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY), loadIcon("icons/species/Omni.png"));
    static { species.add(SPECIES_OMNI); }
    
    public static final Species SPECIES_LEAFCUTTER = new Species(2, LanguageStrings.SPECIES_LEAFCUTTER, LanguageStrings.SPECIES_LEAFCUTTER_SCIENTIFIC, "leafcutter/", GameUnlocks.ASSIMILATION_LEAFCUTTER, 
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_FARMING), loadIcon("icons/species/Leafcutter.png"));
    static { species.add(SPECIES_LEAFCUTTER); }
    
    public static final Species SPECIES_PHARAOH = new Species(3, LanguageStrings.SPECIES_PHARAOH, LanguageStrings.SPECIES_PHARAOH_SCIENTIFIC, "pharaoh/", GameUnlocks.ASSIMILATION_PHARAOH, 
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_MULTIQUEEN), loadIcon("icons/species/Pharaoh.png"));
    static { species.add(SPECIES_PHARAOH); }
    
    public static final Species SPECIES_MARAUDER = new Species(4, LanguageStrings.SPECIES_MARAUDER, LanguageStrings.SPECIES_MARAUDER_SCIENTIFIC, "marauder/", GameUnlocks.ASSIMILATION_MARAUDER, 
        defaultSpeciesUpgrades(GameUnlocks.TYPE_MAJOR), loadIcon("icons/species/Marauder.png"));
    static { species.add(SPECIES_MARAUDER); }

    public static final Species SPECIES_TRAPJAW = new Species(5, LanguageStrings.SPECIES_TRAPJAW, LanguageStrings.SPECIES_TRAPJAW_SCIENTIFIC, "trapjaw/", GameUnlocks.ASSIMILATION_TRAPJAW,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_TRAPJAW), loadIcon("icons/species/Trapjaw.png"));
    static { species.add(SPECIES_TRAPJAW); }

    public static final Species SPECIES_HONEYPOT = new Species(6, LanguageStrings.SPECIES_HONEYPOT, LanguageStrings.SPECIES_HONEYPOT_SCIENTIFIC, "honeypot/", GameUnlocks.ASSIMILATION_HONEYPOT,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_HONEYPOT), loadIcon("icons/species/Honeypot.png"));
    static { species.add(SPECIES_HONEYPOT); }

    public static final Species SPECIES_TURTLE = new Species(7, LanguageStrings.SPECIES_TURTLE, LanguageStrings.SPECIES_TURTLE_SCIENTIFIC, "turtle/", GameUnlocks.ASSIMILATION_DOORHEAD,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_DOORHEAD), loadIcon("icons/species/Turtle.png"));
    static { species.add(SPECIES_TURTLE); }

    // TODO asset: icons/species/Carpenter.png; sprites/ants/carpenter/*.png (placeholder — replace final art)
    public static final Species SPECIES_CARPENTER = new Species(8, LanguageStrings.SPECIES_CARPENTER, LanguageStrings.SPECIES_CARPENTER_SCIENTIFIC, "carpenter/", GameUnlocks.ASSIMILATION_WOODBURROW,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_WOODBURROW), loadIcon("icons/species/Carpenter.png"));
    static { species.add(SPECIES_CARPENTER); }

    // TODO asset: icons/species/Weaver.png; sprites/ants/weaver/*.png (placeholder — replace final art)
    public static final Species SPECIES_WEAVER = new Species(9, LanguageStrings.SPECIES_WEAVER, LanguageStrings.SPECIES_WEAVER_SCIENTIFIC, "weaver/", GameUnlocks.ASSIMILATION_SILKWEAVE,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_SILKWEAVE), loadIcon("icons/species/Weaver.png"));
    static { species.add(SPECIES_WEAVER); }

    // TODO asset: icons/species/Floodplain.png; sprites/ants/floodplain/*.png (placeholder — replace final art)
    public static final Species SPECIES_FLOODPLAIN = new Species(10, LanguageStrings.SPECIES_FLOODPLAIN, LanguageStrings.SPECIES_FLOODPLAIN_SCIENTIFIC, "floodplain/", GameUnlocks.ASSIMILATION_RAFTING,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_RAFTING), loadIcon("icons/species/Floodplain.png"));
    static { species.add(SPECIES_FLOODPLAIN); }

    public static final Species SPECIES_FIRE = new Species(11, LanguageStrings.SPECIES_FIRE, LanguageStrings.SPECIES_FIRE_SCIENTIFIC, "fire/", GameUnlocks.ASSIMILATION_FIREVENOM,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_FIREVENOM), loadIcon("icons/species/Fire.png"));
    static { species.add(SPECIES_FIRE); }

    // TODO asset: icons/species/Jet.png; sprites/ants/jet/*.png (placeholder — replace final art)
    public static final Species SPECIES_JET = new Species(12, LanguageStrings.SPECIES_JET, LanguageStrings.SPECIES_JET_SCIENTIFIC, "jet/", GameUnlocks.ASSIMILATION_JUMPING,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_JUMPING), loadIcon("icons/species/Jet.png"));
    static { species.add(SPECIES_JET); }

    // TODO asset: icons/species/Gliding.png; sprites/ants/gliding/*.png (placeholder — replace final art)
    public static final Species SPECIES_GLIDING = new Species(13, LanguageStrings.SPECIES_GLIDING, LanguageStrings.SPECIES_GLIDING_SCIENTIFIC, "gliding/", GameUnlocks.ASSIMILATION_GLIDING,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_GLIDING), loadIcon("icons/species/Gliding.png"));
    static { species.add(SPECIES_GLIDING); }

    public static final Species SPECIES_BULLET = new Species(14, LanguageStrings.SPECIES_BULLET, LanguageStrings.SPECIES_BULLET_SCIENTIFIC, "bullet/", GameUnlocks.ASSIMILATION_STINGING,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_STINGING), loadIcon("icons/species/Bullet.png"));
    static { species.add(SPECIES_BULLET); }

    // TODO asset: icons/species/Army.png; sprites/ants/army/*.png (placeholder — replace final art)
    public static final Species SPECIES_ARMY = new Species(15, LanguageStrings.SPECIES_ARMY, LanguageStrings.SPECIES_ARMY_SCIENTIFIC, "army/", GameUnlocks.ASSIMILATION_SWARMING,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_SWARMING), loadIcon("icons/species/Army.png"));
    static { species.add(SPECIES_ARMY); }

    // TODO asset: icons/species/Ghost.png; sprites/ants/ghost/*.png (placeholder — replace final art)
    public static final Species SPECIES_GHOST = new Species(16, LanguageStrings.SPECIES_GHOST, LanguageStrings.SPECIES_GHOST_SCIENTIFIC, "ghost/", GameUnlocks.ASSIMILATION_STEALTH,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_STEALTH), loadIcon("icons/species/Ghost.png"));
    static { species.add(SPECIES_GHOST); }

    // TODO asset: icons/species/Dracula.png; sprites/ants/dracula/*.png (placeholder — replace final art)
    public static final Species SPECIES_DRACULA = new Species(17, LanguageStrings.SPECIES_DRACULA, LanguageStrings.SPECIES_DRACULA_SCIENTIFIC, "dracula/", GameUnlocks.ASSIMILATION_FASTBITE,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_FASTBITE), loadIcon("icons/species/Dracula.png"));
    static { species.add(SPECIES_DRACULA); }

    // TODO asset: icons/species/Silver.png; sprites/ants/silver/*.png (placeholder — replace final art)
    public static final Species SPECIES_SILVER = new Species(18, LanguageStrings.SPECIES_SILVER, LanguageStrings.SPECIES_SILVER_SCIENTIFIC, "silver/", GameUnlocks.ASSIMILATION_HEATRESIST,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_HEATRESIST), loadIcon("icons/species/Silver.png"));
    static { species.add(SPECIES_SILVER); }

    public static final Species SPECIES_MARICOPA = new Species(19, LanguageStrings.SPECIES_MARICOPA, LanguageStrings.SPECIES_MARICOPA_SCIENTIFIC, "maricopa/", GameUnlocks.ASSIMILATION_DEADLYVENOM,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_DEADLYVENOM), loadIcon("icons/species/Maricopa.png"));
    static { species.add(SPECIES_MARICOPA); }

    // TODO asset: icons/species/Exploding.png; sprites/ants/exploding/*.png (placeholder — replace final art)
    public static final Species SPECIES_EXPLODING = new Species(20, LanguageStrings.SPECIES_EXPLODING, LanguageStrings.SPECIES_EXPLODING_SCIENTIFIC, "exploding/", GameUnlocks.ASSIMILATION_SELFDESTRUCT,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_SELFDESTRUCT), loadIcon("icons/species/Exploding.png"));
    static { species.add(SPECIES_EXPLODING); }

    // TODO asset: icons/species/Bulldog.png; sprites/ants/bulldog/*.png (placeholder — replace final art)
    public static final Species SPECIES_BULLDOG = new Species(21, LanguageStrings.SPECIES_BULLDOG, LanguageStrings.SPECIES_BULLDOG_SCIENTIFIC, "bulldog/", GameUnlocks.ASSIMILATION_FARSIGHT,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_FARSIGHT), loadIcon("icons/species/Bulldog.png"));
    static { species.add(SPECIES_BULLDOG); }

    // TODO asset: icons/species/ShiningBlack.png; sprites/ants/shiningblack/*.png (placeholder — replace final art)
    public static final Species SPECIES_SHININGBLACK = new Species(22, LanguageStrings.SPECIES_SHININGBLACK, LanguageStrings.SPECIES_SHININGBLACK_SCIENTIFIC, "shiningblack/", GameUnlocks.ASSIMILATION_HIVEBUILD,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_HIVEBUILD), loadIcon("icons/species/ShiningBlack.png"));
    static { species.add(SPECIES_SHININGBLACK); }

    // TODO asset: icons/species/Desert.png; sprites/ants/desert/*.png (placeholder — replace final art)
    public static final Species SPECIES_DESERT = new Species(23, LanguageStrings.SPECIES_DESERT, LanguageStrings.SPECIES_DESERT_SCIENTIFIC, "desert/", GameUnlocks.ASSIMILATION_LOCSENSE,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_LOCSENSE), loadIcon("icons/species/Desert.png"));
    static { species.add(SPECIES_DESERT); }

    // --- Trade Methods ---
    public static final TradeMethod METHOD_LAND = new TradeMethod(1, LanguageStrings.METHOD_LAND, 1.0f, 1.0f, 0.35f,
        loadIcon("icons/convoy/LandConvoy.png"));
    static { tradeMethods.add(METHOD_LAND); }
    public static final TradeMethod METHOD_AIR = new TradeMethod(2, LanguageStrings.METHOD_AIR, 5.0f, 0.5f, 0.25f,
        loadIcon("icons/convoy/SkyConvoy.png"));
    static { tradeMethods.add(METHOD_AIR); }
    public static final TradeMethod METHOD_SEA = new TradeMethod(3, LanguageStrings.METHOD_SEA, 3.0f, 5.0f, 0.15f,
        loadIcon("icons/convoy/SeaConvoy.png"));
    static { tradeMethods.add(METHOD_SEA); }
    public static final TradeMethod METHOD_TUNNEL = new TradeMethod(4, LanguageStrings.METHOD_TUNNEL, 2.0f, 2.0f, 0.05f,
        loadIcon("icons/convoy/TunnelConvoy.png"));
    static { tradeMethods.add(METHOD_TUNNEL); }
    
    // --- Construction Costs ---
    public static final double TUNNEL_WORK_REQUIRED = 5000000.0;
    
    // --- Getters ---
    public static List<Biome> getBiomes() { return Collections.unmodifiableList(biomes); }

    public static List<ResourceType> getResources() { return Collections.unmodifiableList(resources); }

    public static ResourceType getResourceById(int id) {
        for (ResourceType r : resources) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public static List<TimeOfDay> getTimesOfDay() { return Collections.unmodifiableList(timesOfDay); }  

    public static List<MoonPhase> getMoonPhases() { return Collections.unmodifiableList(moonPhases); }

    public static List<Season> getSeasons() { return Collections.unmodifiableList(seasons); }

    public static List<Weather> getWeathers() { return Collections.unmodifiableList(weathers); }

    public static List<AntStatus> getAntStatuses() { return Collections.unmodifiableList(antStatuses); }

    public static List<MoveStatus> getMoveStatuses() { return Collections.unmodifiableList(moveStatuses); }

    public static List<AntType> getAntTypes() { return Collections.unmodifiableList(antTypes); }

    public static AntType getAntTypeById(int id) {
        for (AntType t : antTypes) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public static List<AntSubtype> getAntSubtypes() { return Collections.unmodifiableList(antSubtypes); }

    public static List<AntSubtypeSlot> getConfigurableSubtypeSlots() {
        return List.of(AntSubtypeSlot.HEAD, AntSubtypeSlot.ABDOMEN);
    }

    public static AntSubtype getAntSubtypeBySlotAndDigit(AntSubtypeSlot slot, int digit) {
        for (AntSubtype subtype : antSubtypes) {
            if (subtype.getSlot() == slot && subtype.getDigit() == digit) {
                return subtype;
            }
        }
        return getDefaultSubtypeForSlot(slot);
    }

    public static AntSubtype getDefaultSubtypeForSlot(AntSubtypeSlot slot) {
        return switch (slot) {
            case HEAD -> SUBTYPE_HEAD_NONE;
            case TORSO -> SUBTYPE_TORSO_NONE;
            case ABDOMEN -> SUBTYPE_ABDOMEN_NONE;
            case OTHER -> SUBTYPE_OTHER_NONE;
        };
    }

    public static List<AntSubtype> getSubtypesForSlot(AntSubtypeSlot slot) {
        List<AntSubtype> result = new ArrayList<>();
        for (AntSubtype subtype : antSubtypes) {
            if (subtype.getSlot() == slot) {
                result.add(subtype);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static List<AntRole> getAntRoles() { return Collections.unmodifiableList(antRoles); }

    public static AntRole getAntRoleById(int id) {
        for (AntRole r : antRoles) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public static AntRole getAntRoleByPersistenceKey(String key) {
        if (key == null) {
            return null;
        }
        for (AntRole role : antRoles) {
            if (String.valueOf(role.getId()).equals(key) || role.getNameKey().equals(key)) {
                return role;
            }
        }
        return null;
    }

    public static List<ColonyRank> getColonyRanks() { return Collections.unmodifiableList(colonyRanks); }
    public static List<DynastyTitle> getDynastyTitles() { return Collections.unmodifiableList(dynastyTitles); }
    public static List<DynastyTitle> getCityTitles() { return Collections.unmodifiableList(cityTitles); }

    public static DynastyTitle getDynastyTitleByKey(String key) {
        DynastyTitle title = findTitleByKey(dynastyTitles, key);
        if (title != null) {
            return title;
        }
        title = findTitleByKey(cityTitles, key);
        return title != null ? title : DYNASTY_TITLE_DYNASTY;
    }

    public static DynastyTitle getDynastyTitleById(int id) {
        for (DynastyTitle title : dynastyTitles) {
            if (title.getId() == id) {
                return title;
            }
        }
        for (DynastyTitle title : cityTitles) {
            if (title.getId() == id) {
                return title;
            }
        }
        return DYNASTY_TITLE_DYNASTY;
    }

    public static DynastyTitle getCityTitleByKey(String key) {
        DynastyTitle title = findTitleByKey(cityTitles, key);
        return title != null ? title : CITY_TITLE_CITY;
    }

    private static DynastyTitle findTitleByKey(List<DynastyTitle> titles, String key) {
        if (key == null) {
            return null;
        }
        for (DynastyTitle title : titles) {
            if (title.getNameKey().equals(key)) {
                return title;
            }
        }
        return null;
    }

    public static List<GameSpeed> getGameSpeeds() { return Collections.unmodifiableList(gameSpeeds); }

    public static GameSpeed getGameSpeedById(int id) {
        for (GameSpeed speed : gameSpeeds) {
            if (speed.getId() == id) {
                return speed;
            }
        }
        return null;
    }

    public static int clampDiplomaticReputation(int score) {
        return Math.max(DIPLOMATIC_REPUTATION_MIN, Math.min(DIPLOMATIC_REPUTATION_MAX, score));
    }

    public static DiplomaticReputation getDiplomaticReputationLevel(int score) {
        score = clampDiplomaticReputation(score);
        DiplomaticReputation level = REPUTATION_AGGRESSIVE;
        for (DiplomaticReputation candidate : diplomaticReputations) {
            if (score >= candidate.getMinScore()) {
                level = candidate;
            }
        }
        return level;
    }

    /** True when effective reputation is Wary or better (not Aggressive). */
    public static boolean allowsDiplomatMissionToDynasty(int effectiveReputation) {
        return getDiplomaticReputationLevel(effectiveReputation) != REPUTATION_AGGRESSIVE;
    }

    public static DiplomaticReputation getDiplomaticReputationById(int id) {
        for (DiplomaticReputation level : diplomaticReputations) {
            if (level.getId() == id) {
                return level;
            }
        }
        return null;
    }

    public static List<DiplomaticReputation> getDiplomaticReputations() {
        return Collections.unmodifiableList(diplomaticReputations);
    }

    public static DiplomaticReputationModifier getDiplomaticReputationModifierById(int id) {
        for (DiplomaticReputationModifier modifier : diplomaticReputationModifiers) {
            if (modifier.getId() == id) {
                return modifier;
            }
        }
        return null;
    }

    public static DiplomaticReputationModifier getDiplomaticReputationModifierByKey(String key) {
        if (key == null) {
            return null;
        }
        for (DiplomaticReputationModifier modifier : diplomaticReputationModifiers) {
            if (modifier.getNameKey().equals(key)) {
                return modifier;
            }
        }
        return null;
    }

    public static List<DiplomaticReputationModifier> getDiplomaticReputationModifiers() {
        return Collections.unmodifiableList(diplomaticReputationModifiers);
    }

    public static GeneticIntegrityModifier getGeneticIntegrityModifierForDiplomaticKey(String diplomaticModifierKey) {
        if (diplomaticModifierKey == null) {
            return null;
        }
        for (GeneticIntegrityModifier modifier : geneticIntegrityModifiers) {
            if (diplomaticModifierKey.equals(modifier.getLinkedDiplomaticModifierKey())) {
                return modifier;
            }
        }
        return null;
    }

    public static List<GeneticIntegrityModifier> getGeneticIntegrityModifiers() {
        return Collections.unmodifiableList(geneticIntegrityModifiers);
    }

    public static ColonyLoyaltyModifier getColonyLoyaltyModifierById(int id) {
        for (ColonyLoyaltyModifier modifier : colonyLoyaltyModifiers) {
            if (modifier.getId() == id) {
                return modifier;
            }
        }
        return null;
    }

    public static ColonyLoyaltyModifier getColonyLoyaltyModifierByKey(String key) {
        if (key == null) {
            return null;
        }
        for (ColonyLoyaltyModifier modifier : colonyLoyaltyModifiers) {
            if (modifier.getNameKey().equals(key)) {
                return modifier;
            }
        }
        return null;
    }

    public static List<ColonyLoyaltyModifier> getColonyLoyaltyModifiers() {
        return Collections.unmodifiableList(colonyLoyaltyModifiers);
    }

    public static int clampColonyLoyalty(int score) {
        return Math.max(COLONY_LOYALTY_MIN, Math.min(COLONY_LOYALTY_MAX, score));
    }

    public static ColonyLoyalty getColonyLoyaltyLevel(int score) {
        score = clampColonyLoyalty(score);
        ColonyLoyalty level = LOYALTY_REBELLIOUS;
        for (ColonyLoyalty candidate : colonyLoyalties) {
            if (score >= candidate.getMinScore()) {
                level = candidate;
            }
        }
        return level;
    }

    public static boolean allowsDiplomatMissionToColony(int effectiveLoyalty) {
        return getColonyLoyaltyLevel(effectiveLoyalty) != LOYALTY_REBELLIOUS;
    }

    public static ColonyLoyalty getColonyLoyaltyById(int id) {
        for (ColonyLoyalty level : colonyLoyalties) {
            if (level.getId() == id) {
                return level;
            }
        }
        return null;
    }

    public static List<ColonyLoyalty> getColonyLoyalties() {
        return Collections.unmodifiableList(colonyLoyalties);
    }

    public static List<Species> getSpecies() { return Collections.unmodifiableList(species); }

    public static Species getSpeciesById(int id) {
        for (Species s : species) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    public static double computeIntegrationMonthsPerColony(double diplomatsPerColony) {
        if (diplomatsPerColony <= 0.0) {
            return INTEGRATION_MONTHS_PER_COLONY_SLOW;
        }
        return INTEGRATION_MONTHS_PER_COLONY_SLOW / Math.sqrt(diplomatsPerColony);
    }

    public static List<Species> getNonOmniSpecies() {
        List<Species> result = new ArrayList<>();
        for (Species s : species) {
            if (s.getId() != SPECIES_OMNI.getId()) {
                result.add(s);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** NPC dynasties at world gen — only species with art in {@code sprites/ants/zero-drones/}. */
    private static final Set<Integer> WORLD_SPAWNABLE_NPC_SPECIES_IDS = Set.of(
            SPECIES_LEAFCUTTER.getId(),
            SPECIES_PHARAOH.getId(),
            SPECIES_MARAUDER.getId(),
            SPECIES_TRAPJAW.getId(),
            SPECIES_HONEYPOT.getId(),
            SPECIES_TURTLE.getId(),
            SPECIES_BULLET.getId());

    public static List<Species> getWorldSpawnableNpcSpecies() {
        List<Species> result = new ArrayList<>();
        for (Species s : species) {
            if (WORLD_SPAWNABLE_NPC_SPECIES_IDS.contains(s.getId())) {
                result.add(s);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static List<TradeMethod> getTradeMethods() { return Collections.unmodifiableList(tradeMethods); }

    public static TradeMethod getTradeMethodById(int id) {
        for (TradeMethod m : tradeMethods) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    public static List<Humidity> getHumidity() { return Collections.unmodifiableList(humidity); }

    public static List<Temperature> getTemperature() { return Collections.unmodifiableList(temperature); }

    public static List<BugType> getBugTypes() { return Collections.unmodifiableList(bugTypes); }

    public static List<ImageIcon> getMisc() { return Collections.unmodifiableList(misc); }
}
