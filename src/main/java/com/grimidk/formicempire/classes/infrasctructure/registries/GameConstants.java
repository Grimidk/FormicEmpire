package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.infrasctructure.assets.AntSpriteCompositor;
import com.grimidk.formicempire.classes.infrasctructure.assets.CritterSpriteCompositor;
import com.grimidk.formicempire.classes.infrasctructure.assets.GameSpritePreloader;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.constants.critter.CritterClass;
import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpeciesPalette;
import com.grimidk.formicempire.classes.constants.critter.ant.AntStatus;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.ant.MoveStatus;
import com.grimidk.formicempire.classes.constants.dynasty.colony.CityTitle;
import com.grimidk.formicempire.classes.constants.dynasty.colony.ColonyLoyalty;
import com.grimidk.formicempire.classes.constants.dynasty.colony.ColonyLoyaltyModifier;
import com.grimidk.formicempire.classes.constants.dynasty.Rank;
import com.grimidk.formicempire.classes.constants.dynasty.DynastyTitle;
import com.grimidk.formicempire.classes.constants.dynasty.DiplomaticReputation;
import com.grimidk.formicempire.classes.constants.dynasty.DiplomaticReputationModifier;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.constants.dynasty.WarStagePhase;
import com.grimidk.formicempire.classes.constants.dynasty.WarStanding;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.constants.dynasty.GeneticIntegrityModifier;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.misc.Tier;
import com.grimidk.formicempire.classes.constants.dynasty.TradeMethod;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Humidity;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;

public final class GameConstants {
    private GameConstants() {}

    private static final Map<String, ImageIcon> iconCache = new HashMap<>();

    private static ImageIcon loadIcon(String path) {
        if (iconCache.containsKey(path)) return iconCache.get(path);
        
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resourceUrl == null) {
            System.err.println("CRITICAL ERROR: Resource not found: " + path);
            URL fallback = Thread.currentThread().getContextClassLoader().getResource("icons/misc/Unknown.png");
            if (fallback == null) {
                return null;
            }
            ImageIcon todo = new ImageIcon(fallback);
            GameSpritePreloader.ensureLoaded(todo);
            iconCache.put(path, todo);
            return todo;
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
    // TODO asset: sprites/buildings/rooms/PassiveWeb.png (placeholder)
    public static final ImageIcon ROOM_PASSIVE_WEB = loadIcon("sprites/buildings/rooms/PassiveWeb.png");

    // --- Convoy view tiles ---
    public static final ImageIcon CONVOY_TILE_SEA = loadIcon("backgrounds/convoy/SeaConvoyTile.png");
    public static final ImageIcon CONVOY_TILE_UNDERGROUND = loadIcon("backgrounds/convoy/UndergroundConvoyTile.png");
    public static final ImageIcon CONVOY_TILE_SKY = loadIcon("backgrounds/convoy/SkyConvoyTile.png");

    public static final ImageIcon TUNNEL_SPRITE = loadIcon("sprites/buildings/TunnelSprite.png");

    public static ImageIcon getCritterSprite(Species species) {
        return getCritterSprite(species, 1);
    }

    public static ImageIcon getCritterSprite(Species species, int legFrame) {
        if (species == null) {
            return null;
        }
        if (species.hasComposedSprite()) {
            return CritterSpriteCompositor.getSprite(species, legFrame);
        }
        return species.getSprite();
    }

    public static ImageIcon getAntSprite(AntType type, AntSpecies species) {
        return getAntSprite(type, species, AntSubtypeProfile.standard());
    }

    public static ImageIcon getAntSprite(AntType type, AntSpecies species, AntSubtypeProfile profile) {
        return getAntSprite(type, species, profile, 1, 1, 1, 1, false);
    }

    public static ImageIcon getAntSprite(
            AntType type,
            AntSpecies species,
            AntSubtypeProfile profile,
            int legFrame,
            int jawFrame,
            int wingFrame) {
        return getAntSprite(type, species, profile, legFrame, jawFrame, wingFrame, 1, false);
    }

    public static ImageIcon getAntSprite(
            AntType type,
            AntSpecies species,
            AntSubtypeProfile profile,
            int legFrame,
            int jawFrame,
            int wingFrame,
            int antennaFrame,
            boolean parasiticMites) {
        if (type == null) {
            return null;
        }
        if (type == TYPE_EGG || type == TYPE_LARVA || type == TYPE_PUPA || type == TYPE_DEAD || type == TYPE_ZOMBIE) {
            return loadIcon("sprites/ants/" + type.getSpriteName());
        }
        AntSpecies resolveSpecies = species != null ? species : SPECIES_OMNI;
        if (resolveSpecies.getPalette() != null) {
            return AntSpriteCompositor.getSprite(
                    type, resolveSpecies, profile, legFrame, jawFrame, wingFrame, antennaFrame, parasiticMites);
        }
        return null;
    }

    private static AntSpeciesPalette palette(
            String headColor,
            String torsoColor,
            String abdomenColor,
            String wingPrimaryColor,
            String wingSecondaryColor,
            String droneColor,
            String droneWingPrimaryColor,
            String droneWingSecondaryColor,
            String honeypotColor) {
        return new AntSpeciesPalette(
                headColor, torsoColor, abdomenColor,
                wingPrimaryColor, wingSecondaryColor,
                droneColor, droneWingPrimaryColor, droneWingSecondaryColor,
                honeypotColor);
    }

    public static ImageIcon getRepresentativeAntSprite(AntSpecies species) {
        AntType type = TYPE_WORKER;
        AntSubtypeProfile profile = AntSubtypeProfile.standard();

        if (species != null && species.getBaseUpgrades().contains(GameUnlocks.TYPE_MAJOR)) {
            type = TYPE_MAJOR;
        }

        AntSubtype traitSubtype = findSpeciesTraitSubtype(species);
        if (traitSubtype != null) {
            profile = profileWithSubtype(traitSubtype);
            if (traitSubtype.getAttackMult() > 1f) {
                type = TYPE_SOLDIER;
            } else {
                type = TYPE_WORKER;
            }
        }

        return getAntSprite(type, species, profile);
    }

    private static AntSubtype findSpeciesTraitSubtype(AntSpecies species) {
        if (species == null) {
            return null;
        }
        for (AntSubtype subtype : antSubtypes) {
            if (subtype == null || subtype.isNone() || subtype.getRequiredUpgrade() == null) {
                continue;
            }
            if (species.getBaseUpgrades().contains(subtype.getRequiredUpgrade())) {
                return subtype;
            }
        }
        return null;
    }

    private static AntSubtypeProfile profileWithSubtype(AntSubtype subtype) {
        int head = AntSubtype.DIGIT_NONE;
        int torso = AntSubtype.DIGIT_NONE;
        int abdomen = AntSubtype.DIGIT_NONE;
        int other = AntSubtype.DIGIT_NONE;
        switch (subtype.getSlot()) {
            case HEAD -> head = subtype.getDigit();
            case TORSO -> torso = subtype.getDigit();
            case ABDOMEN -> abdomen = subtype.getDigit();
            case OTHER -> other = subtype.getDigit();
        }
        return AntSubtypeProfile.of(head, torso, abdomen, other);
    }

    public static ImageIcon getAssimilatedDroneSprite(AntSpecies species) {
        if (species == null || !hasAssimilatedDroneSprite(species)) {
            return null;
        }
        return loadIcon("sprites/ants/zero-drones/" + species.getZeroDroneSpriteFileName());
    }

    public static boolean hasAssimilatedDroneSprite(AntSpecies species) {
        if (species == null) {
            return false;
        }
        String path = "sprites/ants/zero-drones/" + species.getZeroDroneSpriteFileName();
        return Thread.currentThread().getContextClassLoader().getResource(path) != null;
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
    private static final List<CritterClass> critterClasses = new ArrayList<>();
    private static final List<Skill> skills = new ArrayList<>();
    private static final List<Species> critterSpecies = new ArrayList<>();
    private static final List<AntType> antTypes = new ArrayList<>();
    private static final List<AntSubtype> antSubtypes = new ArrayList<>();
    private static final List<AntRole> antRoles = new ArrayList<>();
    private static final List<Rank> colonyRanks = new ArrayList<>();
    private static final List<GameSpeed> gameSpeeds = new ArrayList<>();
    private static final List<DiplomaticReputation> diplomaticReputations = new ArrayList<>();
    private static final List<DiplomaticReputationModifier> diplomaticReputationModifiers = new ArrayList<>();
    private static final List<GeneticIntegrityModifier> geneticIntegrityModifiers = new ArrayList<>();
    private static final List<ColonyLoyalty> colonyLoyalties = new ArrayList<>();
    private static final List<ColonyLoyaltyModifier> colonyLoyaltyModifiers = new ArrayList<>();
    private static final List<DynastyTitle> dynastyTitles = new ArrayList<>();
    private static final List<CityTitle> cityTitles = new ArrayList<>();
    private static final List<AntSpecies> species = new ArrayList<>();
    private static final List<TradeMethod> tradeMethods = new ArrayList<>();
    private static final List<WarStagePhase> warStagePhases = new ArrayList<>();
    private static final List<WarStanding> warStandings = new ArrayList<>();
    private static final List<BattleLine> battleLines = new ArrayList<>();
    private static final List<Humidity> humidity = new ArrayList<>();
    private static final List<Temperature> temperature = new ArrayList<>();
    private static final List<ImageIcon> misc = new ArrayList<>();
    private static final List<Tier> tiers = new ArrayList<>();

    // --- Misc Icons ---
    public static final ImageIcon ICON_SOCIALISM = loadIcon("icons/misc/Socialism.png");
    static { misc.add(ICON_SOCIALISM); }
    public static final ImageIcon ICON_APHID = loadIcon("icons/critters/Aphid.png");
    static { misc.add(ICON_APHID); }
    public static final ImageIcon ICON_SYMBIOTIC_MITE = loadIcon("icons/critters/SoilMite.png");
    static { misc.add(ICON_SYMBIOTIC_MITE); }
    public static final ImageIcon ICON_DERMESTID = loadIcon("icons/critters/Dermestid.png");
    static { misc.add(ICON_DERMESTID); }
    public static final ImageIcon ICON_PARASITIC_MITE = loadIcon("icons/critters/ParasiticMite.png");
    static { misc.add(ICON_PARASITIC_MITE); }
    public static final ImageIcon ICON_RESEARCH = loadIcon("icons/misc/Research.png");
    static { misc.add(ICON_RESEARCH); }
    public static final ImageIcon ICON_TRADE = loadIcon("icons/misc/Trade.png");
    static { misc.add(ICON_TRADE); }
    public static final ImageIcon ICON_AUTOMATION = loadIcon("icons/misc/Automation.png");
    static { misc.add(ICON_AUTOMATION); }
    public static final ImageIcon ICON_ATTACK = loadIcon("icons/misc/Attack.png");
    static { misc.add(ICON_ATTACK); }
    public static final ImageIcon ICON_DEFENSE = loadIcon("icons/misc/Defense.png");
    static { misc.add(ICON_DEFENSE); }
    public static final ImageIcon ICON_UNKNOWN = loadIcon("icons/misc/Unknown.png");
    static { misc.add(ICON_UNKNOWN); }
    public static final ImageIcon ICON_STAT_LOYALTY = loadIcon("icons/misc/Loyalty.png");
    static { misc.add(ICON_STAT_LOYALTY); }
    public static final ImageIcon ICON_STAT_REPUTATION = loadIcon("icons/misc/Reputation.png");
    static { misc.add(ICON_STAT_REPUTATION); }
    public static final ImageIcon ICON_STAT_INTELLIGENCE = loadIcon("icons/misc/Intelligence.png");
    static { misc.add(ICON_STAT_INTELLIGENCE); }
    public static final ImageIcon ICON_STAT_COUNTER_INTELLIGENCE = loadIcon("icons/misc/CounterIntelligence.png");
    static { misc.add(ICON_STAT_COUNTER_INTELLIGENCE); }
    public static final ImageIcon ICON_STAT_GENETIC_INTEGRITY = loadIcon("icons/misc/GeneticIntegrity.png");
    static { misc.add(ICON_STAT_GENETIC_INTEGRITY); }
    public static final ImageIcon ICON_STAT_MILITARY_POWER = loadIcon("icons/misc/MilitaryPower.png");
    static { misc.add(ICON_STAT_MILITARY_POWER); }
    public static final ImageIcon ICON_STAT_COMBAT_CAPACITY = loadIcon("icons/misc/CombatCapacity.png");
    static { misc.add(ICON_STAT_COMBAT_CAPACITY); }
    public static final ImageIcon ICON_STAT_POPULATION = loadIcon("icons/misc/Population.png");
    static { misc.add(ICON_STAT_POPULATION); }
    public static final ImageIcon ICON_STAT_FOOD_CONSUMPTION = loadIcon("icons/misc/FoodConsumption.png");
    static { misc.add(ICON_STAT_FOOD_CONSUMPTION); }
    public static final ImageIcon ICON_STAT_FOOD_PRODUCTION = loadIcon("icons/misc/FoodProduction.png");
    static { misc.add(ICON_STAT_FOOD_PRODUCTION); }
    public static final ImageIcon ICON_STAT_NET_FOOD = loadIcon("icons/misc/NetFood.png");
    static { misc.add(ICON_STAT_NET_FOOD); }

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
    public static final Biome BIOME_PLAINS = new Biome(1, LanguageStrings.BIOME_PLAINS, 25, 2, 1f, 1f, 0f, 0f, 0f, 1,
        "#A8C76A", loadIcon("icons/biomes/Plains.png"), null);
    static { biomes.add(BIOME_PLAINS); }
    public static final Biome BIOME_FOREST = new Biome(2, LanguageStrings.BIOME_FOREST, 22, 3, 2f, 2f, 0.1f, 2f, 0.1f, 1,
        "#3D7A3D", loadIcon("icons/biomes/Forest.png"), null);
    static { biomes.add(BIOME_FOREST); }
    public static final Biome BIOME_JUNGLE = new Biome(3, LanguageStrings.BIOME_JUNGLE, 30, 4, 2.5f, 2.5f, 0.2f, 2.5f, 0.15f, 3,
        "#1E6B3C", loadIcon("icons/biomes/Jungle.png"), null);
    static { biomes.add(BIOME_JUNGLE); }
    public static final Biome BIOME_SWAMP = new Biome(4, LanguageStrings.BIOME_SWAMP, 26, 5, 1.5f, 1.5f, 0f, 1.5f, 0.08f, 2,
        "#5A6B3A", loadIcon("icons/biomes/Swamp.png"), null);
    static { biomes.add(BIOME_SWAMP); }
    public static final Biome BIOME_URBAN = new Biome(5, LanguageStrings.BIOME_URBAN, 28, 1, 0.5f, 0.5f, 0.5f, 0f, 0f, 2,
        "#8A8A8A", loadIcon("icons/biomes/Urban.png"), null);
    static { biomes.add(BIOME_URBAN); }
    public static final Biome BIOME_TUNDRA = new Biome(6, LanguageStrings.BIOME_TUNDRA, 5, 2, 0.2f, 0.2f, 0.3f, 0f, 0f, 4,
        "#D8E0E8", loadIcon("icons/biomes/Tundra.png"), null);
    static { biomes.add(BIOME_TUNDRA); }
    public static final Biome BIOME_TAIGA = new Biome(7, LanguageStrings.BIOME_TAIGA, 12, 1, 0.4f, 0.3f, 0.5f, 1f, 0.05f, 3,
        "#4A6B5A", loadIcon("icons/biomes/Taiga.png"), null);
    static { biomes.add(BIOME_TAIGA); }
    public static final Biome BIOME_DESERT = new Biome(8, LanguageStrings.BIOME_DESERT, 45, 0, 0.05f, 0.02f, 1f, 0f, 0f, 4,
        "#E0C070", loadIcon("icons/biomes/Dessert.png"), null);
    static { biomes.add(BIOME_DESERT); }
    public static final Biome BIOME_MOUNTAIN = new Biome(9, LanguageStrings.BIOME_MOUNTAIN, 12, 1, 0.1f, 0.3f, 2f, 0f, 0f, 3,
        "#8B7D6B", loadIcon("icons/biomes/Mountain.png"), null);
    static { biomes.add(BIOME_MOUNTAIN); }
    public static final Biome BIOME_VOLCANIC = new Biome(10, LanguageStrings.BIOME_VOLCANIC, 60, 0, 0.01f, 0.01f, 5f, 0f, 0f, 5,
        "#5A3030", loadIcon("icons/biomes/Volcanic.png"), null);
    static { biomes.add(BIOME_VOLCANIC); }
    public static final Biome BIOME_LAKE = new Biome(11, LanguageStrings.BIOME_LAKE, 25, 5, 0.5f, 0.5f, 0f, 0f, 0f, 1,
        "#4A90A8", loadIcon("icons/biomes/Lake.png"), null);
    static { biomes.add(BIOME_LAKE); }
    public static final Biome BIOME_OCEAN = new Biome(12, LanguageStrings.BIOME_OCEAN, 20, 5, 0.2f, 0.2f, 0f, 0f, 0f, 2,
        "#1E4A6E", loadIcon("icons/biomes/Ocean.png"), null);
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
    public static final MoveStatus MOVE_WANDER = new MoveStatus(2, LanguageStrings.MOVE_WANDER, 1f, loadIcon("icons/moves/Wander.png"));
    static { moveStatuses.add(MOVE_WANDER); }
    public static final MoveStatus MOVE_MARCH = new MoveStatus(3, LanguageStrings.MOVE_MARCH, 2f, loadIcon("icons/moves/March.png"));
    static { moveStatuses.add(MOVE_MARCH); }
    public static final MoveStatus MOVE_SPEED = new MoveStatus(4, LanguageStrings.MOVE_SPEED, 3f, loadIcon("icons/moves/Speed.png"));
    static { moveStatuses.add(MOVE_SPEED); }
    public static final MoveStatus MOVE_FLY = new MoveStatus(5, LanguageStrings.MOVE_FLY, 6f, loadIcon("icons/moves/Fly.png"));
    static { moveStatuses.add(MOVE_FLY); }
    public static final MoveStatus MOVE_PATROL = new MoveStatus(6, LanguageStrings.MOVE_PATROL, 2f, loadIcon("icons/moves/Patrol.png"));
    static { moveStatuses.add(MOVE_PATROL); }

    // --- Critter Class ---
    public static final CritterClass CRITTER_CLASS_ANT = new CritterClass(1, LanguageStrings.CRITTER_CLASS_ANT,
        loadIcon("icons/critterclasses/Ant.png"));
    static { critterClasses.add(CRITTER_CLASS_ANT); }
    public static final CritterClass CRITTER_CLASS_INSECT = new CritterClass(2, LanguageStrings.CRITTER_CLASS_INSECT,
        loadIcon("icons/critterclasses/Insect.png"));
    static { critterClasses.add(CRITTER_CLASS_INSECT); }
    public static final CritterClass CRITTER_CLASS_ARACHNID = new CritterClass(3, LanguageStrings.CRITTER_CLASS_ARACHNID,
        loadIcon("icons/critterclasses/Arachnid.png"));
    static { critterClasses.add(CRITTER_CLASS_ARACHNID); }
    public static final CritterClass CRITTER_CLASS_REPTILE = new CritterClass(4, LanguageStrings.CRITTER_CLASS_REPTILE,
        loadIcon("icons/critterclasses/Reptile.png"));
    static { critterClasses.add(CRITTER_CLASS_REPTILE); }
    public static final CritterClass CRITTER_CLASS_AMPHIBIAN = new CritterClass(5, LanguageStrings.CRITTER_CLASS_AMPHIBIAN,
        loadIcon("icons/critterclasses/Amphibian.png"));
    static { critterClasses.add(CRITTER_CLASS_AMPHIBIAN); }
    public static final CritterClass CRITTER_CLASS_BIRD = new CritterClass(6, LanguageStrings.CRITTER_CLASS_BIRD,
        loadIcon("icons/critterclasses/Bird.png"));
    static { critterClasses.add(CRITTER_CLASS_BIRD); }
    public static final CritterClass CRITTER_CLASS_MAMMAL = new CritterClass(5, LanguageStrings.CRITTER_CLASS_MAMMAL,
        loadIcon("icons/critterclasses/Mammal.png"));
    static { critterClasses.add(CRITTER_CLASS_MAMMAL); }
    public static final CritterClass CRITTER_CLASS_FUNGI = new CritterClass(6, LanguageStrings.CRITTER_CLASS_FUNGI,
        loadIcon("icons/critterclasses/Fungi.png"));
    static { critterClasses.add(CRITTER_CLASS_FUNGI); }
    public static final CritterClass CRITTER_CLASS_PLANT = new CritterClass(7, LanguageStrings.CRITTER_CLASS_PLANT,
        loadIcon("icons/critterclasses/Plant.png"));
    static { critterClasses.add(CRITTER_CLASS_PLANT); }
    public static final CritterClass CRITTER_CLASS_XENO = new CritterClass(8, LanguageStrings.CRITTER_CLASS_XENO,
        loadIcon("icons/critterclasses/Xeno.png"));
    static { critterClasses.add(CRITTER_CLASS_XENO); }

    // --- Skills ---
    public static final Skill SKILL_BASIC_BITE = new Skill(1, LanguageStrings.SKILL_BASIC_BITE, 1f, 1f, 1, true,
            loadIcon("icons/skills/BasicBite.png"));
    static { skills.add(SKILL_BASIC_BITE); }
    public static final Skill SKILL_POWERFUL_BITE = new Skill(2, LanguageStrings.SKILL_POWERFUL_BITE, 1f, 2f, 1, true,
            loadIcon("icons/skills/PowerfulBite.png"));
    static { skills.add(SKILL_POWERFUL_BITE); }
    public static final Skill SKILL_STINGING = new Skill(3, LanguageStrings.SKILL_STINGING, 0.8f, 2.5f, 1, true,
            loadIcon("icons/skills/Stinging.png"));
    static { skills.add(SKILL_STINGING); }
    public static final Skill SKILL_SHIELDING = new Skill(4, LanguageStrings.SKILL_SHIELDING, 1f, 0f, -1, false,
            loadIcon("icons/skills/Shielding.png"));
    static { skills.add(SKILL_SHIELDING); }
    public static final Skill SKILL_BOOST_REGEN = new Skill(5, LanguageStrings.SKILL_BOOST_REGEN, 1f, 0f, -1, false,
            loadIcon("icons/skills/BoostRegen.png"));
    static { skills.add(SKILL_BOOST_REGEN); }
    public static final Skill SKILL_ACID_SPITTING = new Skill(6, LanguageStrings.SKILL_ACID_SPITTING, 0.75f, 2f, 1, true,
            loadIcon("icons/skills/AcidSpitting.png"));
    static { skills.add(SKILL_ACID_SPITTING); }
    public static final Skill SKILL_ACID_ARTILLERY = new Skill(7, LanguageStrings.SKILL_ACID_ARTILLERY, 0.5f, 5f, 1, true,
            loadIcon("icons/skills/AcidArtillery.png"));
    static { skills.add(SKILL_ACID_ARTILLERY); }
    public static final Skill SKILL_SELFDESTRUCT = new Skill(8, LanguageStrings.SKILL_SELFDESTRUCT, 0.9f, 5f, 5, true, 
            loadIcon("icons/skills/Selfdestruct.png"));
    static { skills.add(SKILL_SELFDESTRUCT); }
    public static final Skill SKILL_ACIDIC_SELFDESTRUCT = new Skill(9, LanguageStrings.SKILL_ACIDIC_SELFDESTRUCT, 0.9f, 10f, 7, true,
            loadIcon("icons/skills/AcidicSelfdestruct.png"));
    static { skills.add(SKILL_ACIDIC_SELFDESTRUCT); }
    public static final Skill SKILL_ARTILLERY_LEADER = new Skill(10, LanguageStrings.SKILL_ARTILLERY_LEADER, 1f, 0f, -1, false,
            true, GameNumbers.COMMANDER_ARTILLERY_DAMAGE_BONUS, null,
            loadIcon("icons/skills/PreciseCommands.png"));
    static { skills.add(SKILL_ARTILLERY_LEADER); }
    public static final Skill SKILL_INFANTRY_LEADER = new Skill(11, LanguageStrings.SKILL_INFANTRY_LEADER, 1f, 0f, -1, false,
            true, GameNumbers.CAPTAIN_INFANTRY_DAMAGE_BONUS, null,
            loadIcon("icons/skills/InfantryCommand.png"));
    static { skills.add(SKILL_INFANTRY_LEADER); }
    public static final Skill SKILL_CLOSE_ANT_SUPPORT = new Skill(12, LanguageStrings.SKILL_CLOSE_ANT_SUPPORT, 0.9f, 10f, 1, true,
            loadIcon("icons/skills/CloseAntSupport.png"));
    static { skills.add(SKILL_CLOSE_ANT_SUPPORT); }
    public static final Skill SKILL_AIR_BOMBING = new Skill(13, LanguageStrings.SKILL_AIR_BOMBING, 0.9f, 20f, 7, true,
            loadIcon("icons/skills/AirBombing.png"));
    static { skills.add(SKILL_AIR_BOMBING); }

    // --- Species ---
    public static final Species TYPE_ANT = new Species(1, LanguageStrings.BUG_ANT, CRITTER_CLASS_ANT, LanguageStrings.BUG_ANT_SCIENTIFIC, 1, 1, 1, 1, 1, 1,
        loadIcon("icons/ants/omni/Worker.png"), loadIcon("icons/ants/omni/Worker.png"), false, List.of(SKILL_BASIC_BITE));
    static { critterSpecies.add(TYPE_ANT); }
    public static final Species TYPE_APHID = new Species(2, LanguageStrings.BUG_APHID, CRITTER_CLASS_INSECT, LanguageStrings.BUG_APHID_SCIENTIFIC, 1, 1, 0, 0, 0, 0.5f,
        loadIcon("icons/critters/Aphid.png"),
        CritterSpriteCompositor.getSprite("aphid", "Aphid.png", true),
        "aphid", "Aphid.png", true, true);
    static { critterSpecies.add(TYPE_APHID); }
    public static final Species TYPE_PARASITE_ANT = new Species(3, LanguageStrings.BUG_PARASITE_ANT, CRITTER_CLASS_ANT, LanguageStrings.BUG_PARASITE_ANT_SCIENTIFIC, 1, 1, 0, 0, 1, 1,
        loadIcon("icons/critters/ParasiticAnt.png"),
        CritterSpriteCompositor.getSprite("parasiticAnt", "ParasiticAnt.png", true),
        "parasiticAnt", "ParasiticAnt.png", true, false);
    static { critterSpecies.add(TYPE_PARASITE_ANT); }
    public static final Species TYPE_SYMBIOTIC_MITE = new Species(4, LanguageStrings.BUG_SYMBIOTIC_MITE, CRITTER_CLASS_ARACHNID, LanguageStrings.BUG_SYMBIOTIC_MITE_SCIENTIFIC, 1, 0, 0, 0, 0, 0.4f,
        loadIcon("icons/critters/SoilMite.png"),
        CritterSpriteCompositor.getSprite("soilMite", "SoilMite.png", true),
        "soilMite", "SoilMite.png", true, true);
    static { critterSpecies.add(TYPE_SYMBIOTIC_MITE); }
    public static final Species TYPE_DERMESTID = new Species(5, LanguageStrings.BUG_DERMESTID, CRITTER_CLASS_INSECT, LanguageStrings.BUG_DERMESTID_SCIENTIFIC, 1, 0, 0, 0, 0, 0.35f,
        loadIcon("icons/critters/Dermestid.png"),
        CritterSpriteCompositor.getSprite("dermestid", "Dermestid.png", true),
        "dermestid", "Dermestid.png", true, true);
    static { critterSpecies.add(TYPE_DERMESTID); }
    public static final Species TYPE_PARASITIC_MITE = new Species(6, LanguageStrings.BUG_PARASITIC_MITE, CRITTER_CLASS_ARACHNID, LanguageStrings.BUG_PARASITIC_MITE_SCIENTIFIC, 1, 0, 0, 0, 1, 0.25f,
        loadIcon("icons/critters/ParasiticMite.png"),
        CritterSpriteCompositor.getSprite("parasiticMite", "ParasiticMite.png", false),
        "parasiticMite", "ParasiticMite.png", false, false);
    static { critterSpecies.add(TYPE_PARASITIC_MITE); }

    static {
        for (Biome biome : biomes) {
            biome.setNativeBugs(buildNativeBugsForBiome(biome));
            biome.setNativeParasites(buildNativeParasitesForBiome(biome));
        }
    }

    private static List<Species> buildNativeBugsForBiome(Biome biome) {
        List<Species> natives = new ArrayList<>();
        if (!biome.isDry()) {
            natives.add(TYPE_APHID);
        }
        natives.add(TYPE_SYMBIOTIC_MITE);
        if (!biome.isCold()) {
            natives.add(TYPE_DERMESTID);
        }
        return List.copyOf(natives);
    }

    private static List<Species> buildNativeParasitesForBiome(Biome biome) {
        List<Species> natives = new ArrayList<>();
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
    public static final AntType TYPE_EGG = new AntType(1, LanguageStrings.TYPE_EGG, 0.01f, 0f, 0f, 0f, 0f, 0f, 0f,
        loadIcon("icons/ants/Egg.png"), "Egg.png");
    static { antTypes.add(TYPE_EGG); }    
    public static final AntType TYPE_LARVA = new AntType(2, LanguageStrings.TYPE_LARVA, 0.01f, 0f, 0f, 1f, 0f, 0f, 0.5f,
        loadIcon("icons/ants/Larva.png"), "Larva.png");
    static { antTypes.add(TYPE_LARVA); }
    public static final AntType TYPE_PUPA = new AntType(3, LanguageStrings.TYPE_PUPA, 0.01f, 0f, 0f, 0f, 0f, 0f, 0f,
        loadIcon("icons/ants/Pupa.png"), "Pupa.png");
    static { antTypes.add(TYPE_PUPA); }
    public static final AntType TYPE_WORKER = new AntType(4, LanguageStrings.TYPE_WORKER, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1,
        loadIcon("icons/ants/omni/Worker.png"), "Worker.png");
    static { antTypes.add(TYPE_WORKER); }
    public static final AntType TYPE_SOLDIER = new AntType(5, LanguageStrings.TYPE_SOLDIER, 3f, 3f, 1f, 2f, 2f, 0f, 3f, 5,
        loadIcon("icons/ants/omni/Soldier.png"), "Soldier.png");
    static { antTypes.add(TYPE_SOLDIER); }
    public static final AntType TYPE_MAJOR = new AntType(6, LanguageStrings.TYPE_MAJOR, 10f, 15f, 1f, 5f, 2f, 20f, 2f, 15,
        loadIcon("icons/ants/omni/Major.png"), "Major.png");
    static { antTypes.add(TYPE_MAJOR); }
    public static final AntType TYPE_DRONE = new AntType(7, LanguageStrings.TYPE_DRONE, 0.01f, 0f, 0f, 1f, 0f, 0f, 1f, 
        loadIcon("icons/ants/omni/Drone.png"), "Drone.png");
    static { antTypes.add(TYPE_DRONE); }
    public static final AntType TYPE_PRINCESS = new AntType(8, LanguageStrings.TYPE_PRINCESS, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 10,
        loadIcon("icons/ants/omni/Princess.png"), "Princess.png");
    static { antTypes.add(TYPE_PRINCESS); }
    public static final AntType TYPE_QUEEN = new AntType(9, LanguageStrings.TYPE_QUEEN, 50f, 2f, 1f, 10f, 1f, 20f, 1/4f, 50,
        loadIcon("icons/ants/omni/Queen.png"), "Queen.png");
    static { antTypes.add(TYPE_QUEEN); }
    public static final AntType TYPE_DEAD = new AntType(10, LanguageStrings.TYPE_DEAD, 0, 0, 0, 0, 0, 0, 0,
        loadIcon("icons/ants/Dead.png"), "Dead.png");
    static { antTypes.add(TYPE_DEAD); }
    public static final AntType TYPE_ZOMBIE = new AntType(11, LanguageStrings.TYPE_ZOMBIE,  1f, 1f, 1f, 1f, 1f, 0f, 1f, 
        loadIcon("icons/ants/Zombie.png"), "Zombie.png");
    static { antTypes.add(TYPE_ZOMBIE); }

    // --- Ant Subtypes ---
    private static final ImageIcon SUBTYPE_ICON_NOTHING = loadIcon("icons/species/Omni.png");

    public static final AntSubtype SUBTYPE_HEAD_NONE = new AntSubtype(1, LanguageStrings.SUBTYPE_NOTHING, AntSubtypeSlot.HEAD,
            AntSubtype.DIGIT_NONE, null, null, null, 1f, false, 1f, 1f, 1f, LanguageStrings.SUBTYPE_NOTHING_DESC, SUBTYPE_ICON_NOTHING);
    static { antSubtypes.add(SUBTYPE_HEAD_NONE); }
    public static final AntSubtype SUBTYPE_HEAD_TRAPJAW = new AntSubtype(2, LanguageStrings.SUBTYPE_HEAD_TRAPJAW, AntSubtypeSlot.HEAD,
            2, GameUnlocks.ASSIMILATED_TRAPJAW, "trapjaw/", "trapjaw",
            1.5f, true, 1f, 1f, 1f,
            SKILL_POWERFUL_BITE, SKILL_BASIC_BITE, loadIcon("icons/subtypes/trapjaw.png"));
    static { antSubtypes.add(SUBTYPE_HEAD_TRAPJAW); }
    public static final AntSubtype SUBTYPE_HEAD_DOORHEAD = new AntSubtype(3, LanguageStrings.SUBTYPE_HEAD_DOORHEAD, AntSubtypeSlot.HEAD,
            3, GameUnlocks.ASSIMILATED_DOORHEAD, "turtle/", "doorhead",
            1f, false, 20f, 1f, 1f,
            SKILL_SHIELDING, null, loadIcon("icons/subtypes/doorhead.png"));
    static { antSubtypes.add(SUBTYPE_HEAD_DOORHEAD); }
    public static final AntSubtype SUBTYPE_HEAD_FARSIGHT = new AntSubtype(9, LanguageStrings.SUBTYPE_HEAD_FARSIGHT, AntSubtypeSlot.HEAD,
            4, GameUnlocks.ASSIMILATED_FARSIGHT, "bulldog/", "farsight",
            1f, false, 1f, 1f, 1f,
            1f, 0.15f, LanguageStrings.SUBTYPE_HEAD_FARSIGHT_DESC, null, null, loadIcon("icons/subtypes/farsight.png"));
    static { antSubtypes.add(SUBTYPE_HEAD_FARSIGHT); }

    public static final AntSubtype SUBTYPE_TORSO_NONE = new AntSubtype(4, LanguageStrings.SUBTYPE_NOTHING, AntSubtypeSlot.TORSO,
            AntSubtype.DIGIT_NONE, null, null, null, 1f, false, 1f, 1f, 1f, LanguageStrings.SUBTYPE_NOTHING_DESC, SUBTYPE_ICON_NOTHING);
    static { antSubtypes.add(SUBTYPE_TORSO_NONE); }

    public static final AntSubtype SUBTYPE_ABDOMEN_NONE = new AntSubtype(5, LanguageStrings.SUBTYPE_NOTHING, AntSubtypeSlot.ABDOMEN,
            AntSubtype.DIGIT_NONE, null, null, null, 1f, false, 1f, 1f, 1f, LanguageStrings.SUBTYPE_NOTHING_DESC, SUBTYPE_ICON_NOTHING);
    static { antSubtypes.add(SUBTYPE_ABDOMEN_NONE); }
    public static final AntSubtype SUBTYPE_ABDOMEN_STINGER = new AntSubtype(6, LanguageStrings.SUBTYPE_ABDOMEN_STINGER, AntSubtypeSlot.ABDOMEN,
            2, GameUnlocks.ASSIMILATED_STINGING, "bullet/", "bullet",
            1.5f, false, 1f, 1f, 1f,
            SKILL_STINGING, null, loadIcon("icons/subtypes/bullet.png"));
    static { antSubtypes.add(SUBTYPE_ABDOMEN_STINGER); }
    public static final AntSubtype SUBTYPE_ABDOMEN_HONEYPOT = new AntSubtype(7, LanguageStrings.SUBTYPE_ABDOMEN_HONEYPOT, AntSubtypeSlot.ABDOMEN,
            3, GameUnlocks.ASSIMILATED_HONEYPOT, "honeypot/", "honeypot",
            1f, false, 1f, 0.75f, 4f,
            1.15f, SKILL_BOOST_REGEN, null, loadIcon("icons/subtypes/honeypot.png"));
    static { antSubtypes.add(SUBTYPE_ABDOMEN_HONEYPOT); }
    public static final AntSubtype SUBTYPE_OTHER_NONE = new AntSubtype(8, LanguageStrings.SUBTYPE_NOTHING, AntSubtypeSlot.OTHER,
            AntSubtype.DIGIT_NONE, null, null, null, 1f, false, 1f, 1f, 1f, LanguageStrings.SUBTYPE_NOTHING_DESC, SUBTYPE_ICON_NOTHING);
    static { antSubtypes.add(SUBTYPE_OTHER_NONE); }

    static {
        SKILL_POWERFUL_BITE.setRequiredSubtype(SUBTYPE_HEAD_TRAPJAW);
        SKILL_STINGING.setRequiredSubtype(SUBTYPE_ABDOMEN_STINGER);
        SKILL_SHIELDING.setRequiredSubtype(SUBTYPE_HEAD_DOORHEAD);
        SKILL_BOOST_REGEN.setRequiredSubtype(SUBTYPE_ABDOMEN_HONEYPOT);
    }

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
    public static final AntRole ROLE_MINER = new AntRole(8, TYPE_SOLDIER, LanguageStrings.ROLE_MINER, loadIcon("icons/roles/Miner.png"));
    static { antRoles.add(ROLE_MINER); }
    public static final AntRole ROLE_COURIER = new AntRole(9, TYPE_WORKER, LanguageStrings.ROLE_COURIER, loadIcon("icons/roles/Courier.png"));
    static { antRoles.add(ROLE_COURIER); }
    public static final AntRole ROLE_POTTER = new AntRole(10, TYPE_WORKER, LanguageStrings.ROLE_POTTER, loadIcon("icons/roles/Potter.png"), Set.of(SUBTYPE_ABDOMEN_HONEYPOT), Set.of(SUBTYPE_ABDOMEN_HONEYPOT), true);
    static { antRoles.add(ROLE_POTTER); }
    static { SKILL_BOOST_REGEN.setRequiredRole(ROLE_POTTER); }
    public static final AntRole ROLE_WARRIOR = new AntRole(12, TYPE_SOLDIER, LanguageStrings.ROLE_WARRIOR, loadIcon("icons/roles/Warrior.png"), true);
    static { antRoles.add(ROLE_WARRIOR); }
    public static final AntRole ROLE_DEFENDER = new AntRole(13, TYPE_SOLDIER, LanguageStrings.ROLE_DEFENDER, loadIcon("icons/roles/Defender.png"), Set.of(SUBTYPE_HEAD_DOORHEAD), Set.of(SUBTYPE_HEAD_DOORHEAD), true, true);
    static { antRoles.add(ROLE_DEFENDER); }
    static { SKILL_SHIELDING.setRequiredRole(ROLE_DEFENDER); }
    public static final AntRole ROLE_POLICE = new AntRole(14, TYPE_SOLDIER, LanguageStrings.ROLE_POLICE, loadIcon("icons/roles/Police.png"));
    static { antRoles.add(ROLE_POLICE); }
    public static final AntRole ROLE_BOMBER = new AntRole(15, TYPE_SOLDIER, LanguageStrings.ROLE_BOMBER, loadIcon("icons/roles/Bomber.png"), true);
    static { antRoles.add(ROLE_BOMBER); }
    static { SKILL_SELFDESTRUCT.setRequiredRole(ROLE_BOMBER); }
    static { SKILL_ACIDIC_SELFDESTRUCT.setRequiredRole(ROLE_BOMBER); }
    public static final AntRole ROLE_HUNTER = new AntRole(16, TYPE_SOLDIER, LanguageStrings.ROLE_HUNTER, loadIcon("icons/roles/Hunter.png"));
    static { antRoles.add(ROLE_HUNTER); }
    public static final AntRole ROLE_BRUTE = new AntRole(17, TYPE_MAJOR, LanguageStrings.ROLE_BRUTE,loadIcon("icons/roles/Brute.png"), true);
    static { antRoles.add(ROLE_BRUTE); }
    public static final AntRole ROLE_CARRIER = new AntRole(18, TYPE_MAJOR, LanguageStrings.ROLE_CARRIER, loadIcon("icons/roles/Carrier.png"));
    static { antRoles.add(ROLE_CARRIER); }
    public static final AntRole ROLE_ARTILLERY = new AntRole(19, TYPE_MAJOR, LanguageStrings.ROLE_ARTILLERY,loadIcon("icons/roles/Artillery.png"), Set.of(), Set.of(), true);
    static { antRoles.add(ROLE_ARTILLERY); }
    static { SKILL_ACID_ARTILLERY.setRequiredRole(ROLE_ARTILLERY); }
    public static final AntRole ROLE_SIEGE = new AntRole(20, TYPE_MAJOR, LanguageStrings.ROLE_SIEGE, loadIcon("icons/roles/Siege.png"), Set.of(), Set.of(), true, true);
    static { antRoles.add(ROLE_SIEGE); }
    public static final AntRole ROLE_BORER = new AntRole(21, TYPE_MAJOR, LanguageStrings.ROLE_BORER, loadIcon("icons/roles/Borer.png"));
    static { antRoles.add(ROLE_BORER); }
    public static final AntRole ROLE_DRONE = new AntRole(22, TYPE_DRONE, LanguageStrings.ROLE_DRONE, loadIcon("icons/roles/Drone.png"));
    static { antRoles.add(ROLE_DRONE); }
    public static final AntRole ROLE_BREEDER = new AntRole(23, TYPE_PRINCESS, LanguageStrings.ROLE_BREEDER, loadIcon("icons/roles/Breeder.png"));
    static { antRoles.add(ROLE_BREEDER); }
    public static final AntRole ROLE_DIPLOMAT = new AntRole(24, TYPE_PRINCESS, LanguageStrings.ROLE_DIPLOMAT, loadIcon("icons/roles/Diplomat.png"));
    static { antRoles.add(ROLE_DIPLOMAT); }
    public static final AntRole ROLE_SPY = new AntRole(39, TYPE_PRINCESS, LanguageStrings.ROLE_SPY, loadIcon("icons/roles/Spy.png"));
    static { antRoles.add(ROLE_SPY); }
    public static final AntRole ROLE_LAYER = new AntRole(25, TYPE_QUEEN, LanguageStrings.ROLE_LAYER, loadIcon("icons/roles/Layer.png"));
    static { antRoles.add(ROLE_LAYER); }
    public static final AntRole ROLE_RESEARCHER = new AntRole(26, TYPE_QUEEN, LanguageStrings.ROLE_RESEARCHER, loadIcon("icons/roles/Researcher.png"));
    static { antRoles.add(ROLE_RESEARCHER); }
    public static final AntRole ROLE_COMMANDER = new AntRole(35, TYPE_QUEEN, LanguageStrings.ROLE_COMMANDER, loadIcon("icons/roles/Commander.png"), true);
    static { antRoles.add(ROLE_COMMANDER); }
    static { SKILL_ARTILLERY_LEADER.setRequiredRole(ROLE_COMMANDER); }
    public static final AntRole ROLE_CAPTAIN = new AntRole(36, TYPE_PRINCESS, LanguageStrings.ROLE_CAPTAIN, loadIcon("icons/roles/Captain.png"), true);
    static { antRoles.add(ROLE_CAPTAIN); }
    static { SKILL_INFANTRY_LEADER.setRequiredRole(ROLE_CAPTAIN); }
    public static final AntRole ROLE_AIR_SUPPORT = new AntRole(37, TYPE_PRINCESS, LanguageStrings.ROLE_AIR_SUPPORT, loadIcon("icons/roles/AirSupport.png"), true);
    static { antRoles.add(ROLE_AIR_SUPPORT); }
    static { SKILL_CLOSE_ANT_SUPPORT.setRequiredRole(ROLE_AIR_SUPPORT); }
    public static final AntRole ROLE_AIR_BOMBER = new AntRole(38, TYPE_PRINCESS, LanguageStrings.ROLE_AIR_BOMBER, loadIcon("icons/roles/AirBomber.png"), true);
    static { antRoles.add(ROLE_AIR_BOMBER); }
    static { SKILL_AIR_BOMBING.setRequiredRole(ROLE_AIR_BOMBER); }
    public static final AntRole ROLE_MILITIA = new AntRole(27, TYPE_WORKER, LanguageStrings.ROLE_MILITIA, loadIcon("icons/roles/Militia.png"), true);
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
    public static final Rank RANK_ANT = new Rank(1, LanguageStrings.RANK_ANT, 1L,
        loadIcon("icons/ranks/Ant.png"));
    static { colonyRanks.add(RANK_ANT); }
    public static final Rank RANK_COLONY = new Rank(2, LanguageStrings.RANK_COLONY, 10L,
        loadIcon("icons/ranks/Colony.png"));
    static { colonyRanks.add(RANK_COLONY); }
    public static final Rank RANK_COUNTY = new Rank(3, LanguageStrings.RANK_COUNTY, 100L,
        LanguageStrings.TRIGGER_RANK_COUNTY_TITLE, LanguageStrings.TRIGGER_RANK_COUNTY_MSG,
        loadIcon("icons/ranks/County.png"));
    static { colonyRanks.add(RANK_COUNTY); }
    public static final Rank RANK_DUCHY = new Rank(4, LanguageStrings.RANK_DUCHY, 1000L,
        LanguageStrings.TRIGGER_RANK_DUCHY_TITLE, LanguageStrings.TRIGGER_RANK_DUCHY_MSG,
        loadIcon("icons/ranks/Duchy.png"));
    static { colonyRanks.add(RANK_DUCHY); }
    public static final Rank RANK_KINGDOM = new Rank(5, LanguageStrings.RANK_KINGDOM, 10000L,
        LanguageStrings.TRIGGER_RANK_KINGDOM_TITLE, LanguageStrings.TRIGGER_RANK_KINGDOM_MSG,
        loadIcon("icons/ranks/Kingdom.png"));
    static { colonyRanks.add(RANK_KINGDOM); }
    public static final Rank RANK_EMPIRE = new Rank(6, LanguageStrings.RANK_EMPIRE, 100000L,
        LanguageStrings.TRIGGER_CLONING_TITLE, LanguageStrings.TRIGGER_CLONING_MSG,
        loadIcon("icons/ranks/Empire.png"));
    static { colonyRanks.add(RANK_EMPIRE); }
    public static final Rank RANK_SUPER = new Rank(7, LanguageStrings.RANK_SUPER, 1000000L,
        LanguageStrings.TRIGGER_RANK_SUPER_TITLE, LanguageStrings.TRIGGER_RANK_SUPER_MSG,
        loadIcon("icons/ranks/Super.png"));
    static { colonyRanks.add(RANK_SUPER); }
    public static final Rank RANK_ULTRA = new Rank(8, LanguageStrings.RANK_ULTRA, 10000000L,
        LanguageStrings.TRIGGER_RANK_ULTRA_TITLE, LanguageStrings.TRIGGER_RANK_ULTRA_MSG,
        loadIcon("icons/ranks/Ultra.png"));
    static { colonyRanks.add(RANK_ULTRA); }
    public static final Rank RANK_HYPER = new Rank(9, LanguageStrings.RANK_HYPER, 100000000L,
        LanguageStrings.TRIGGER_RANK_HYPER_TITLE, LanguageStrings.TRIGGER_RANK_HYPER_MSG,
        loadIcon("icons/ranks/Hyper.png"));
    static { colonyRanks.add(RANK_HYPER); }
    public static final Rank RANK_MEGA = new Rank(10, LanguageStrings.RANK_MEGA, 1000000000L,
        LanguageStrings.TRIGGER_RANK_MEGA_TITLE, LanguageStrings.TRIGGER_RANK_MEGA_MSG,
        loadIcon("icons/ranks/Mega.png"));
    static { colonyRanks.add(RANK_MEGA); }
    public static final Rank RANK_ULTIMATE = new Rank(11, LanguageStrings.RANK_ULTIMATE, 10000000000L,
        LanguageStrings.TRIGGER_RANK_ULTIMATE_TITLE, LanguageStrings.TRIGGER_RANK_ULTIMATE_MSG,
        loadIcon("icons/ranks/Ultimate.png"));
    static { colonyRanks.add(RANK_ULTIMATE); }

    public static final Rank RANK_SUPREME = new Rank(12, LanguageStrings.RANK_SUPREME, 100000000000L,
        LanguageStrings.TRIGGER_RANK_SUPREME_TITLE, LanguageStrings.TRIGGER_RANK_SUPREME_MSG,
        loadIcon("icons/ranks/Supreme.png"));
    static { colonyRanks.add(RANK_SUPREME); }
    public static final Rank RANK_GIGA = new Rank(13, LanguageStrings.RANK_GIGA, 1000000000000L,
        LanguageStrings.TRIGGER_RANK_GIGA_TITLE, LanguageStrings.TRIGGER_RANK_GIGA_MSG,
        loadIcon("icons/ranks/Giga.png"));
    static { colonyRanks.add(RANK_GIGA); }

    static {
        RANK_DUCHY.setUnlockOnAnnounce(GameUnlocks.ROLE_POLICE);
        RANK_KINGDOM.setUnlockOnAnnounce(GameUnlocks.ROLE_AIR_SUPPORT);
        RANK_EMPIRE.setUnlockOnAnnounce(GameUnlocks.ABILITY_CLONING);
    }

    // --- Tiers ---
    public static final Tier TIER_0 = new Tier(1, LanguageStrings.TIER_0, RANK_COLONY, 10L, 0, loadIcon("icons/tiers/Tier0.png"));
    static { tiers.add(TIER_0); }
    public static final Tier TIER_1 = new Tier(2, LanguageStrings.TIER_1, RANK_COUNTY, 100L, 0, loadIcon("icons/tiers/Tier1.png"));
    static { tiers.add(TIER_1); }
    public static final Tier TIER_2 = new Tier(3, LanguageStrings.TIER_2, RANK_DUCHY, 1_000L, 200, loadIcon("icons/tiers/Tier2.png"));
    static { tiers.add(TIER_2); }
    public static final Tier TIER_3 = new Tier(4, LanguageStrings.TIER_3, RANK_KINGDOM, 10_000L, 500, loadIcon("icons/tiers/Tier3.png"));
    static { tiers.add(TIER_3); }
    public static final Tier TIER_4 = new Tier(5, LanguageStrings.TIER_4, RANK_EMPIRE, 100_000L, 1_000, loadIcon("icons/tiers/Tier4.png"));
    static { tiers.add(TIER_4); }
    public static final Tier TIER_5 = new Tier(6, LanguageStrings.TIER_5, RANK_SUPER, 1_000_000L, 2_500, loadIcon("icons/tiers/Tier5.png"));
    static { tiers.add(TIER_5); }
    public static final Tier TIER_6 = new Tier(7, LanguageStrings.TIER_6, RANK_ULTRA, 10_000_000L, 5_000, loadIcon("icons/tiers/Tier6.png"));
    static { tiers.add(TIER_6); }
    public static final Tier TIER_7 = new Tier(8, LanguageStrings.TIER_7, RANK_HYPER, 100_000_000L, 10_000, loadIcon("icons/tiers/Tier7.png"));
    static { tiers.add(TIER_7); }
    public static final Tier TIER_8 = new Tier(9, LanguageStrings.TIER_8, RANK_MEGA, 1_000_000_000L, 25_000, loadIcon("icons/tiers/Tier8.png"));
    static { tiers.add(TIER_8); }
    public static final Tier TIER_9 = new Tier(10, LanguageStrings.TIER_9, RANK_ULTIMATE, 10_000_000_000L, 50_000, loadIcon("icons/tiers/Tier9.png"));
    static { tiers.add(TIER_9); }
    public static final Tier TIER_10 = new Tier(11, LanguageStrings.TIER_10, RANK_SUPREME, 100_000_000_000L, 100_000, loadIcon("icons/tiers/Tier10.png"));
    static { tiers.add(TIER_10); }
    public static final Tier TIER_11 = new Tier(12, LanguageStrings.TIER_11, RANK_GIGA, 1_000_000_000_000L, 250_000, loadIcon("icons/tiers/Tier11.png"));
    static { tiers.add(TIER_11); }  

    // --- Game speeds ---
    public static final GameSpeed SPEED_VERY_SLOW = new GameSpeed(
            GameSpeed.ID_VERY_SLOW, LanguageStrings.UI_SPEED_VERY_SLOW, 50,
            loadIcon("icons/speed/VerySlow.png"));
    static { gameSpeeds.add(SPEED_VERY_SLOW); }
    public static final GameSpeed SPEED_SLOW = new GameSpeed(
            GameSpeed.ID_SLOW, LanguageStrings.UI_SPEED_SLOW, 25,
            loadIcon("icons/speed/Slow.png"));
    static { gameSpeeds.add(SPEED_SLOW); }
    public static final GameSpeed SPEED_NORMAL = new GameSpeed(
            GameSpeed.ID_NORMAL, LanguageStrings.UI_SPEED_NORMAL, 10,
            loadIcon("icons/speed/Normal.png"));
    static { gameSpeeds.add(SPEED_NORMAL); }
    public static final GameSpeed SPEED_FAST = new GameSpeed(
            GameSpeed.ID_FAST, LanguageStrings.UI_SPEED_FAST, 5,
            loadIcon("icons/speed/Fast.png"));
    static { gameSpeeds.add(SPEED_FAST); }
    public static final GameSpeed SPEED_VERY_FAST = new GameSpeed(
            GameSpeed.ID_VERY_FAST, LanguageStrings.UI_SPEED_VERY_FAST, 2,
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
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_WARMONGER = new DiplomaticReputationModifier(
        12, LanguageStrings.DIPLO_MODIFIER_WARMONGER, -20, 0, null);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_WARMONGER); }
    public static final DiplomaticReputationModifier DIPLO_MODIFIER_CAUGHT_SPYING = new DiplomaticReputationModifier(
        13, LanguageStrings.DIPLO_MODIFIER_CAUGHT_SPYING, -15, 0, null, GameNumbers.CAUGHT_SPYING_DURATION_DAYS);
    static { diplomaticReputationModifiers.add(DIPLO_MODIFIER_CAUGHT_SPYING); }

    public static final GeneticIntegrityModifier GI_MODIFIER_PACT = new GeneticIntegrityModifier(
        1, LanguageStrings.GI_MODIFIER_PACT, 10.0, DIPLO_MODIFIER_PACT.getNameKey());
    static { geneticIntegrityModifiers.add(GI_MODIFIER_PACT); }
    public static final GeneticIntegrityModifier GI_MODIFIER_GENETIC_EXCHANGE = new GeneticIntegrityModifier(
        2, LanguageStrings.GI_MODIFIER_GENETIC_EXCHANGE, 10.0, DIPLO_MODIFIER_GENETIC_EXCHANGE.getNameKey());
    static { geneticIntegrityModifiers.add(GI_MODIFIER_GENETIC_EXCHANGE); }

    private static final Set<AntRole> UNOBTAINABLE_ROLES = Set.of(
            ROLE_CARRIER);

    private static final AntRole[] ACTIVE_MILITARY_ROLES = antRoles.stream()
            .filter(AntRole::isActiveMilitary)
            .toArray(AntRole[]::new);

    private static final AntRole[] BORDER_BATTLE_ROLES = antRoles.stream()
            .filter(AntRole::participatesInBorderBattle)
            .toArray(AntRole[]::new);

    private static final AntRole[] HEX_DEFENSE_ONLY_ROLES = antRoles.stream()
            .filter(AntRole::isHexDefenseOnly)
            .toArray(AntRole[]::new);

    public static boolean isWarEconomyExclusiveRole(AntRole role) {
        return role != null && role.isActiveMilitary();
    }

    public static boolean isObtainableRole(AntRole role) {
        return role != null && !UNOBTAINABLE_ROLES.contains(role);
    }

    public static boolean isEligibleDefaultHatchRole(AntRole role) {
        return isObtainableRole(role) && !isWarEconomyExclusiveRole(role);
    }

    public static List<AntRole> eligibleDefaultHatchRoles(AntType type) {
        List<AntRole> out = new ArrayList<>();
        if (type == null) {
            return out;
        }
        for (AntRole role : antRoles) {
            if (role.getAntType() == type && isEligibleDefaultHatchRole(role)) {
                out.add(role);
            }
        }
        return out;
    }

    public static boolean isActiveMilitaryRole(AntRole role) {
        return role != null && role.isActiveMilitary();
    }

    public static boolean isBorderBattleRole(AntRole role) {
        return role != null && role.participatesInBorderBattle();
    }

    public static boolean isHexDefenseOnlyRole(AntRole role) {
        return role != null && role.isHexDefenseOnly();
    }

    public static AntRole[] getActiveMilitaryRoles() {
        return ACTIVE_MILITARY_ROLES.clone();
    }

    public static AntRole[] getBorderBattleRoles() {
        return BORDER_BATTLE_ROLES.clone();
    }

    public static AntRole[] getHexDefenseOnlyRoles() {
        return HEX_DEFENSE_ONLY_ROLES.clone();
    }

    public static int getMilitaryWeightForAntType(AntType type) {
        return type != null ? type.getMilitaryWeight() : 0;
    }

    public static int getActiveMilitaryRoleWeight(AntRole role) {
        return role != null ? getMilitaryWeightForAntType(role.getAntType()) : 0;
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

    // --- City titles ---
    public static final CityTitle CITY_TITLE_PRIME = new CityTitle(
            1, LanguageStrings.CITY_TITLE_PRIME, CityTitle.Affix.SUFFIX, CityTitle.ColonyRole.CAPITAL);
    static { cityTitles.add(CITY_TITLE_PRIME); }

    public static final CityTitle CITY_TITLE_NEW = new CityTitle(
            2, LanguageStrings.CITY_TITLE_NEW, CityTitle.Affix.PREFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_NEW); }

    public static final CityTitle CITY_TITLE_CITY = new CityTitle(
            3, LanguageStrings.CITY_TITLE_CITY, CityTitle.Affix.SUFFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_CITY); }

    public static final CityTitle CITY_TITLE_BERG = new CityTitle(
            4, LanguageStrings.CITY_TITLE_BERG, CityTitle.Affix.SUFFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_BERG); }

    public static final CityTitle CITY_TITLE_GRAD = new CityTitle(
            5, LanguageStrings.CITY_TITLE_GRAD, CityTitle.Affix.SUFFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_GRAD); }

    public static final CityTitle CITY_TITLE_BURG = new CityTitle(
            6, LanguageStrings.CITY_TITLE_BURG, CityTitle.Affix.SUFFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_BURG); }

    public static final CityTitle CITY_TITLE_HAVEN = new CityTitle(
            7, LanguageStrings.CITY_TITLE_HAVEN, CityTitle.Affix.SUFFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_HAVEN); }

    public static final CityTitle CITY_TITLE_NEST = new CityTitle(
            8, LanguageStrings.CITY_TITLE_NEST, CityTitle.Affix.SUFFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_NEST); }

    public static final CityTitle CITY_TITLE_HOLD = new CityTitle(
            9, LanguageStrings.CITY_TITLE_HOLD, CityTitle.Affix.SUFFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_HOLD); }

    public static final CityTitle CITY_TITLE_FORD = new CityTitle(
            10, LanguageStrings.CITY_TITLE_FORD, CityTitle.Affix.SUFFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_FORD); }

    public static final CityTitle CITY_TITLE_FORT = new CityTitle(
            11, LanguageStrings.CITY_TITLE_FORT, CityTitle.Affix.PREFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_FORT); }

    public static final CityTitle CITY_TITLE_CASTLE = new CityTitle(
            12, LanguageStrings.CITY_TITLE_CASTLE, CityTitle.Affix.PREFIX, CityTitle.ColonyRole.SATELLITE);
    static { cityTitles.add(CITY_TITLE_CASTLE); }

    private static final List<String> genericDynastyThemeKeys = List.of(
            LanguageStrings.DYNASTY_THEME_LEAFCUTTER,
            LanguageStrings.DYNASTY_THEME_SAND,
            LanguageStrings.DYNASTY_THEME_ROCK,
            LanguageStrings.DYNASTY_THEME_IRON,
            LanguageStrings.DYNASTY_THEME_MUSHROOM,
            LanguageStrings.DYNASTY_THEME_MEAT,
            LanguageStrings.DYNASTY_THEME_WATER,
            LanguageStrings.DYNASTY_THEME_FIRE,
            LanguageStrings.DYNASTY_THEME_WIND,
            LanguageStrings.DYNASTY_THEME_DIRT,
            LanguageStrings.DYNASTY_THEME_WOOD,
            LanguageStrings.DYNASTY_THEME_GOLD,
            LanguageStrings.DYNASTY_THEME_BUG,
            LanguageStrings.DYNASTY_THEME_STONE,
            LanguageStrings.DYNASTY_THEME_CLAY,
            LanguageStrings.DYNASTY_THEME_STEEL,
            LanguageStrings.DYNASTY_THEME_ICE,
            LanguageStrings.DYNASTY_THEME_FLAME,
            LanguageStrings.DYNASTY_THEME_STORM,
            LanguageStrings.DYNASTY_THEME_DUST,
            LanguageStrings.DYNASTY_THEME_VINE,
            LanguageStrings.DYNASTY_THEME_ROOT,
            LanguageStrings.DYNASTY_THEME_SEED,
            LanguageStrings.DYNASTY_THEME_POLLEN,
            LanguageStrings.DYNASTY_THEME_HONEY,
            LanguageStrings.DYNASTY_THEME_SILK,
            LanguageStrings.DYNASTY_THEME_WEB,
            LanguageStrings.DYNASTY_THEME_SHADOW,
            LanguageStrings.DYNASTY_THEME_NIGHT,
            LanguageStrings.DYNASTY_THEME_DAY,
            LanguageStrings.DYNASTY_THEME_SILVER,
            LanguageStrings.DYNASTY_THEME_COPPER,
            LanguageStrings.DYNASTY_THEME_BRONZE,
            LanguageStrings.DYNASTY_THEME_EMERALD,
            LanguageStrings.DYNASTY_THEME_RUBY,
            LanguageStrings.DYNASTY_THEME_SAPPHIRE,
            LanguageStrings.DYNASTY_THEME_QUARTZ,
            LanguageStrings.DYNASTY_THEME_GRANITE,
            LanguageStrings.DYNASTY_THEME_SWAMP,
            LanguageStrings.DYNASTY_THEME_MARSH,
            LanguageStrings.DYNASTY_THEME_OCEAN,
            LanguageStrings.DYNASTY_THEME_RIVER,
            LanguageStrings.DYNASTY_THEME_PEAK,
            LanguageStrings.DYNASTY_THEME_VALLEY,
            LanguageStrings.DYNASTY_THEME_CAVE,
            LanguageStrings.DYNASTY_THEME_FOREST,
            LanguageStrings.DYNASTY_THEME_JUNGLE
    );

    // --- AntSpecies ---
    // palette(head, torso, abdomen, wingPrimary, wingSecondary, drone, droneWingPrimary, droneWingSecondary, honeypot)
    public static final AntSpecies SPECIES_OMNI = new AntSpecies(1, LanguageStrings.SPECIES_OMNI, LanguageStrings.SPECIES_OMNI_SCIENTIFIC,  "omni/", GameUnlocks.ASSIMILATION_OMNI, 
        Set.of(GameUnlocks.TYPE_EGG, GameUnlocks.TYPE_QUEEN, GameUnlocks.TYPE_WORKER, GameUnlocks.ROLE_FORAGER, 
            GameUnlocks.ROLE_FARMER, GameUnlocks.ROLE_NURSE, GameUnlocks.ROLE_LAYER, 
            GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_ACID, GameUnlocks.STAT_LONGEVITY),
        Set.of(LanguageStrings.DYNASTY_THEME_PLAYER, LanguageStrings.DYNASTY_THEME_OMNI, LanguageStrings.DYNASTY_THEME_ANT,
                LanguageStrings.DYNASTY_THEME_IRON, LanguageStrings.DYNASTY_THEME_GOLD, LanguageStrings.DYNASTY_THEME_STONE,
                LanguageStrings.DYNASTY_THEME_BUG, LanguageStrings.DYNASTY_THEME_SILVER, LanguageStrings.DYNASTY_THEME_COPPER,
                LanguageStrings.DYNASTY_THEME_BRONZE, LanguageStrings.DYNASTY_THEME_STEEL),
        palette("751717", "751717", "751717", "eb8931", "a46422", "1b2632", "31a2f2", "005784", "eb8931"),
        loadIcon("icons/species/Omni.png"));
    static { species.add(SPECIES_OMNI); }
    
    public static final AntSpecies SPECIES_LEAFCUTTER = new AntSpecies(2, LanguageStrings.SPECIES_LEAFCUTTER, LanguageStrings.SPECIES_LEAFCUTTER_SCIENTIFIC, "leafcutter/", GameUnlocks.ASSIMILATION_LEAFCUTTER, 
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_FARMING),
        Set.of(LanguageStrings.DYNASTY_THEME_LEAF, LanguageStrings.DYNASTY_THEME_PLANT, LanguageStrings.DYNASTY_THEME_SEED,
                LanguageStrings.DYNASTY_THEME_LEAFCUTTER, LanguageStrings.DYNASTY_THEME_WOOD, LanguageStrings.DYNASTY_THEME_VINE,
                LanguageStrings.DYNASTY_THEME_ROOT, LanguageStrings.DYNASTY_THEME_FOREST, LanguageStrings.DYNASTY_THEME_JUNGLE,
                LanguageStrings.DYNASTY_THEME_MUSHROOM),
        palette("be2633", "be2633", "be2633", "eb8931", "a46422", "676767", "31a2f2", "005784", "eb8931"),
        loadIcon("icons/species/Leafcutter.png"));
    static { species.add(SPECIES_LEAFCUTTER); }
    
    public static final AntSpecies SPECIES_PHARAOH = new AntSpecies(3, LanguageStrings.SPECIES_PHARAOH, LanguageStrings.SPECIES_PHARAOH_SCIENTIFIC, "pharaoh/", GameUnlocks.ASSIMILATION_PHARAOH, 
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_MULTIQUEEN),
        Set.of(LanguageStrings.DYNASTY_THEME_PHARAOH, LanguageStrings.DYNASTY_THEME_RUBY, LanguageStrings.DYNASTY_THEME_TOPAZ,
                LanguageStrings.DYNASTY_THEME_SAND, LanguageStrings.DYNASTY_THEME_DUST, LanguageStrings.DYNASTY_THEME_EMERALD,
                LanguageStrings.DYNASTY_THEME_SAPPHIRE, LanguageStrings.DYNASTY_THEME_GOLD, LanguageStrings.DYNASTY_THEME_SILVER,
                LanguageStrings.DYNASTY_THEME_SILK),
        palette("f7e26b", "f7e26b", "f7e26b", "a46422", "eb8931", "751717", "e06f8b", "be2633", "eb8931"),
        loadIcon("icons/species/Pharaoh.png"));
    static { species.add(SPECIES_PHARAOH); }
    
    public static final AntSpecies SPECIES_MARAUDER = new AntSpecies(4, LanguageStrings.SPECIES_MARAUDER, LanguageStrings.SPECIES_MARAUDER_SCIENTIFIC, "marauder/", GameUnlocks.ASSIMILATION_MARAUDER, 
        defaultSpeciesUpgrades(GameUnlocks.TYPE_MAJOR),
        Set.of(LanguageStrings.DYNASTY_THEME_MARAUDER, LanguageStrings.DYNASTY_THEME_SCORPION, LanguageStrings.DYNASTY_THEME_COCKROACH,
                LanguageStrings.DYNASTY_THEME_MEAT, LanguageStrings.DYNASTY_THEME_FIRE, LanguageStrings.DYNASTY_THEME_FLAME,
                LanguageStrings.DYNASTY_THEME_STORM, LanguageStrings.DYNASTY_THEME_SHADOW, LanguageStrings.DYNASTY_THEME_NIGHT,
                LanguageStrings.DYNASTY_THEME_MARSH, LanguageStrings.DYNASTY_THEME_SWAMP),
        palette("eb8931", "eb8931", "eb8931", "f7e26b", "a46422", "676767", "9d9d9d", "434343", "f7e26b"),
        loadIcon("icons/species/Marauder.png"));
    static { species.add(SPECIES_MARAUDER); }

    public static final AntSpecies SPECIES_TRAPJAW = new AntSpecies(5, LanguageStrings.SPECIES_TRAPJAW, LanguageStrings.SPECIES_TRAPJAW_SCIENTIFIC, "trapjaw/", GameUnlocks.ASSIMILATION_TRAPJAW,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_TRAPJAW),
        Set.of(LanguageStrings.DYNASTY_THEME_TRAPJAW, LanguageStrings.DYNASTY_THEME_TRAP, LanguageStrings.DYNASTY_THEME_JAW),
        palette("493c2b", "493c2b", "493c2b", "751717", "32100a", "a46422", "32100a", "751717", "eb8931"),
        loadIcon("icons/species/Trapjaw.png"));
    static { species.add(SPECIES_TRAPJAW); }

    public static final AntSpecies SPECIES_HONEYPOT = new AntSpecies(6, LanguageStrings.SPECIES_HONEYPOT, LanguageStrings.SPECIES_HONEYPOT_SCIENTIFIC, "honeypot/", GameUnlocks.ASSIMILATION_HONEYPOT,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_HONEYPOT),
        Set.of(LanguageStrings.DYNASTY_THEME_HONEYPOT, LanguageStrings.DYNASTY_THEME_HONEY, LanguageStrings.DYNASTY_THEME_BEE),
        palette("a46422", "a46422", "a46422", "32100a", "751717", "a46422", "f7e26b", "eb8931", "eb8931"),
        loadIcon("icons/species/Honeypot.png"));
    static { species.add(SPECIES_HONEYPOT); }

    public static final AntSpecies SPECIES_TURTLE = new AntSpecies(7, LanguageStrings.SPECIES_TURTLE, LanguageStrings.SPECIES_TURTLE_SCIENTIFIC, "turtle/", GameUnlocks.ASSIMILATION_DOORHEAD,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_DOORHEAD),
        Set.of(LanguageStrings.DYNASTY_THEME_TURTLE, LanguageStrings.DYNASTY_THEME_SHELL, LanguageStrings.DYNASTY_THEME_TORTOISE),
        palette("434343", "434343", "434343", "751717", "32100a", "32100a", "a3ce27", "676767", "eb8931"),
        loadIcon("icons/species/Turtle.png"));
    static { species.add(SPECIES_TURTLE); }

    public static final AntSpecies SPECIES_CARPENTER = new AntSpecies(8, LanguageStrings.SPECIES_CARPENTER, LanguageStrings.SPECIES_CARPENTER_SCIENTIFIC, "carpenter/", GameUnlocks.ASSIMILATION_WOODBURROW,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_WOODBURROW),
        Set.of(LanguageStrings.DYNASTY_THEME_CARPENTER, LanguageStrings.DYNASTY_THEME_WOOD, LanguageStrings.DYNASTY_THEME_TREE),
        palette("a3ce27", "a3ce27", "eb8931", "d24f14", "32100a", "32100a", "a46422", "d24f14", "eb8931"),
        loadIcon("icons/species/Carpenter.png"));
    static { species.add(SPECIES_CARPENTER); }

    public static final AntSpecies SPECIES_WEAVER = new AntSpecies(9, LanguageStrings.SPECIES_WEAVER, LanguageStrings.SPECIES_WEAVER_SCIENTIFIC, "weaver/", GameUnlocks.ASSIMILATION_SILKWEAVE,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_SILKWEAVE),
        Set.of(LanguageStrings.DYNASTY_THEME_WEAVER, LanguageStrings.DYNASTY_THEME_SILK, LanguageStrings.DYNASTY_THEME_SPIDER),
        palette("493c2b", "493c2b", "be2633", "9427c4", "ae7ee5", "ae7ee5", "493c2b", "be2633", "be2633"),
        loadIcon("icons/species/Weaver.png"));
    static { species.add(SPECIES_WEAVER); }

    public static final AntSpecies SPECIES_FLOODPLAIN = new AntSpecies(10, LanguageStrings.SPECIES_FLOODPLAIN, LanguageStrings.SPECIES_FLOODPLAIN_SCIENTIFIC, "floodplain/", GameUnlocks.ASSIMILATION_RAFTING,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_RAFTING),
        Set.of(LanguageStrings.DYNASTY_THEME_FLOODPLAIN, LanguageStrings.DYNASTY_THEME_RIVER, LanguageStrings.DYNASTY_THEME_WATER),
        palette("be2633", "d24f14", "be2633", "b2dcef", "005784", "005784", "32100a", "d24f14", "be2633"),
        loadIcon("icons/species/Floodplain.png"));
    static { species.add(SPECIES_FLOODPLAIN); }

    public static final AntSpecies SPECIES_FIRE = new AntSpecies(11, LanguageStrings.SPECIES_FIRE, LanguageStrings.SPECIES_FIRE_SCIENTIFIC, "fire/", GameUnlocks.ASSIMILATION_FIREVENOM,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_FIREVENOM),
        Set.of(LanguageStrings.DYNASTY_THEME_FIRE, LanguageStrings.DYNASTY_THEME_FLAME, LanguageStrings.DYNASTY_THEME_BURN),
        palette("d24f14", "d24f14", "d24f14", "a3ce27", "eb8931", "f7e26b", "eb8931", "d24f14", "f7e26b"),
        loadIcon("icons/species/Fire.png"));
    static { species.add(SPECIES_FIRE); }

    public static final AntSpecies SPECIES_JET = new AntSpecies(12, LanguageStrings.SPECIES_JET, LanguageStrings.SPECIES_JET_SCIENTIFIC, "jet/", GameUnlocks.ASSIMILATION_JUMPING,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_JUMPING),
        Set.of(LanguageStrings.DYNASTY_THEME_JET, LanguageStrings.DYNASTY_THEME_TORNADO, LanguageStrings.DYNASTY_THEME_WIND),
        palette("0e141a", "0e141a", "0e141a", "32100a", "1b2632", "a46422", "0e141a", "44891a", "a46422"),
        loadIcon("icons/species/Jet.png"));
    static { species.add(SPECIES_JET); }

    public static final AntSpecies SPECIES_BULLET = new AntSpecies(14, LanguageStrings.SPECIES_BULLET, LanguageStrings.SPECIES_BULLET_SCIENTIFIC, "bullet/", GameUnlocks.ASSIMILATION_STINGING,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_STINGING),
        Set.of(LanguageStrings.DYNASTY_THEME_BULLET, LanguageStrings.DYNASTY_THEME_STING, LanguageStrings.DYNASTY_THEME_PUNCH),
        palette("32100a", "32100a", "32100a", "be2633", "751717", "a3ce27", "9d9d9d", "676767", "eb8931"),
        loadIcon("icons/species/Bullet.png"));
    static { species.add(SPECIES_BULLET); }

    public static final AntSpecies SPECIES_ARMY = new AntSpecies(15, LanguageStrings.SPECIES_ARMY, LanguageStrings.SPECIES_ARMY_SCIENTIFIC, "army/", GameUnlocks.ASSIMILATION_SWARMING,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_SWARMING),
        Set.of(LanguageStrings.DYNASTY_THEME_ARMY, LanguageStrings.DYNASTY_THEME_SOLDIER, LanguageStrings.DYNASTY_THEME_WARRIOR),
        palette("d24f14", "eb8931", "eb8931", "eb8931", "31a2f2", "31a2f2", "d24f14", "f7e26b", "f7e26b"),
        loadIcon("icons/species/Army.png"));
    static { species.add(SPECIES_ARMY); }

    // Ghost palette: white body, grey wings; dark brown drone with white/dark wings
    public static final AntSpecies SPECIES_GHOST = new AntSpecies(16, LanguageStrings.SPECIES_GHOST, LanguageStrings.SPECIES_GHOST_SCIENTIFIC, "ghost/", GameUnlocks.ASSIMILATION_STEALTH,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_STEALTH),
        Set.of(LanguageStrings.DYNASTY_THEME_GHOST, LanguageStrings.DYNASTY_THEME_SHADOW, LanguageStrings.DYNASTY_THEME_PHANTOM),
        palette("ffffff", "ffffff", "ffffff", "676767", "434343", "32100a", "ffffff", "0e141a", "ffffff"),
        loadIcon("icons/species/Ghost.png"));
    static { species.add(SPECIES_GHOST); }

    public static final AntSpecies SPECIES_DRACULA = new AntSpecies(17, LanguageStrings.SPECIES_DRACULA, LanguageStrings.SPECIES_DRACULA_SCIENTIFIC, "dracula/", GameUnlocks.ASSIMILATION_FASTBITE,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_FASTBITE),
        Set.of(LanguageStrings.DYNASTY_THEME_DRACULA, LanguageStrings.DYNASTY_THEME_VAMPIRE, LanguageStrings.DYNASTY_THEME_BLOOD),
        palette("751717", "a46422", "a46422", "b2dcef", "31a2f2", "d24f14", "b2dcef", "31a2f2", "eb8931"),
        loadIcon("icons/species/Dracula.png"));
    static { species.add(SPECIES_DRACULA); }

    public static final AntSpecies SPECIES_SILVER = new AntSpecies(18, LanguageStrings.SPECIES_SILVER, LanguageStrings.SPECIES_SILVER_SCIENTIFIC, "silver/", GameUnlocks.ASSIMILATION_HEATRESIST,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_HEATRESIST),
        Set.of(LanguageStrings.DYNASTY_THEME_SILVER, LanguageStrings.DYNASTY_THEME_METAL, LanguageStrings.DYNASTY_THEME_GOLD),
        palette("9d9d9d", "9d9d9d", "9d9d9d", "b2dcef", "ae7ee5", "44891a", "751717", "ae7ee5", "44891a"),
        loadIcon("icons/species/Silver.png"));
    static { species.add(SPECIES_SILVER); }

    public static final AntSpecies SPECIES_MARICOPA = new AntSpecies(19, LanguageStrings.SPECIES_MARICOPA, LanguageStrings.SPECIES_MARICOPA_SCIENTIFIC, "maricopa/", GameUnlocks.ASSIMILATION_DEADLYVENOM,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_DEADLYVENOM),
        Set.of(LanguageStrings.DYNASTY_THEME_MARICOPA, LanguageStrings.DYNASTY_THEME_VENOM, LanguageStrings.DYNASTY_THEME_POISON),
        palette("e06f8b", "e06f8b", "e06f8b", "d24f14", "be2633", "9427c4", "be2633", "751717", "f7e26b"),
        loadIcon("icons/species/Maricopa.png"));
    static { species.add(SPECIES_MARICOPA); }

    public static final AntSpecies SPECIES_EXPLODING = new AntSpecies(20, LanguageStrings.SPECIES_EXPLODING, LanguageStrings.SPECIES_EXPLODING_SCIENTIFIC, "exploding/", GameUnlocks.ASSIMILATION_SELFDESTRUCT,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_SELFDESTRUCT),
        Set.of(LanguageStrings.DYNASTY_THEME_EXPLODING, LanguageStrings.DYNASTY_THEME_BOMB, LanguageStrings.DYNASTY_THEME_EXPLOSION),
        palette("be2633", "be2633", "434343", "d24f14", "eb8931", "be2633", "005784", "31a2f2", "eb8931"),
        loadIcon("icons/species/Exploding.png"));
    static { species.add(SPECIES_EXPLODING); }

    public static final AntSpecies SPECIES_BULLDOG = new AntSpecies(21, LanguageStrings.SPECIES_BULLDOG, LanguageStrings.SPECIES_BULLDOG_SCIENTIFIC, "bulldog/", GameUnlocks.ASSIMILATION_FARSIGHT,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_FARSIGHT),
        Set.of(LanguageStrings.DYNASTY_THEME_BULLDOG, LanguageStrings.DYNASTY_THEME_DOG, LanguageStrings.DYNASTY_THEME_HOUND),
        palette("d24f14", "d24f14", "32100a", "be2633", "751717", "1b2632", "9427c4", "be2633", "eb8931"),
        loadIcon("icons/species/Bulldog.png"));
    static { species.add(SPECIES_BULLDOG); }

    public static final AntSpecies SPECIES_SHININGBLACK = new AntSpecies(22, LanguageStrings.SPECIES_SHININGBLACK, LanguageStrings.SPECIES_SHININGBLACK_SCIENTIFIC, "shiningblack/", GameUnlocks.ASSIMILATION_HIVEBUILD,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_HIVEBUILD),
        Set.of(LanguageStrings.DYNASTY_THEME_SHINING, LanguageStrings.DYNASTY_THEME_BLACK, LanguageStrings.DYNASTY_THEME_DARK),
        palette("0e141a", "0e141a", "434343", "0e141a", "434343", "ffffff", "0e141a", "676767", "434343"),
        loadIcon("icons/species/ShiningBlack.png"));
    static { species.add(SPECIES_SHININGBLACK); }

    public static final AntSpecies SPECIES_DESERT = new AntSpecies(23, LanguageStrings.SPECIES_DESERT, LanguageStrings.SPECIES_DESERT_SCIENTIFIC, "desert/", GameUnlocks.ASSIMILATION_LOCSENSE,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_LOCSENSE),
        Set.of(LanguageStrings.DYNASTY_THEME_DESERT, LanguageStrings.DYNASTY_THEME_SAND, LanguageStrings.DYNASTY_THEME_DIRT),
        palette("be2633", "751717", "751717", "f7e26b", "eb8931", "f7e26b", "a46422", "493c2b", "a46422"),
        loadIcon("icons/species/Desert.png"));
    static { species.add(SPECIES_DESERT); }

    public static final AntSpecies SPECIES_GREEN = new AntSpecies(24, LanguageStrings.SPECIES_GREEN, LanguageStrings.SPECIES_GREEN_SCIENTIFIC, "green/", GameUnlocks.ASSIMILATION_ACIDSPIT,
        defaultSpeciesUpgrades(GameUnlocks.ASSIMILATED_ACIDSPIT),
        Set.of(LanguageStrings.DYNASTY_THEME_GREEN, LanguageStrings.DYNASTY_THEME_ACID, LanguageStrings.DYNASTY_THEME_EMERALD),
        palette("a3ce27", "a3ce27", "a3ce27", "31a2f2", "005784", "d24f14", "751717", "32100a", "eb8931"),
        loadIcon("icons/species/Green.png"));
    static { species.add(SPECIES_GREEN); }

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

    // --- War stage phases / standing ---
    public static final WarStagePhase WAR_STAGE_ACTIVE_CLASH = new WarStagePhase(
            1, LanguageStrings.BATTLE_PHASE_CLASH, "ACTIVE_CLASH", loadIcon("icons/war/BorderClash.png"));
    static { warStagePhases.add(WAR_STAGE_ACTIVE_CLASH); }
    public static final WarStagePhase WAR_STAGE_RESERVE_ASSAULT = new WarStagePhase(
            2, LanguageStrings.BATTLE_PHASE_RESERVE, "RESERVE_ASSAULT", loadIcon("icons/war/ColonySiege.png"));
    static { warStagePhases.add(WAR_STAGE_RESERVE_ASSAULT); }
    public static final WarStagePhase WAR_STAGE_REDEPLOYING = new WarStagePhase(
            3, LanguageStrings.BATTLE_PHASE_REDEPLOY, "REDEPLOYING", loadIcon("icons/war/Redeployment.png"));
    static { warStagePhases.add(WAR_STAGE_REDEPLOYING); }

    public static final WarStanding WAR_STANDING_WINNING = new WarStanding(
            1, LanguageStrings.WAR_STANDING_WINNING, loadIcon("icons/war/Winning.png"));
    static { warStandings.add(WAR_STANDING_WINNING); }
    public static final WarStanding WAR_STANDING_LOSING = new WarStanding(
            2, LanguageStrings.WAR_STANDING_LOSING, loadIcon("icons/war/Losing.png"));
    static { warStandings.add(WAR_STANDING_LOSING); }
    public static final WarStanding WAR_STANDING_EVEN = new WarStanding(
            3, LanguageStrings.WAR_STANDING_EVEN, loadIcon("icons/war/Even.png"));
    static { warStandings.add(WAR_STANDING_EVEN); }

    // --- Battle lines ---
    public static final BattleLine BATTLE_LINE_INFANTRY = new BattleLine(
            1, LanguageStrings.BATTLE_LINE_INFANTRY, 100f,
            Set.of(ROLE_MILITIA, ROLE_WARRIOR, ROLE_DEFENDER, ROLE_BRUTE, ROLE_SIEGE, ROLE_BOMBER, ROLE_CAPTAIN),
            loadIcon("icons/battleLines/Infantry.png"));
    static { battleLines.add(BATTLE_LINE_INFANTRY); }
    public static final BattleLine BATTLE_LINE_ARTILLERY = new BattleLine(
            2, LanguageStrings.BATTLE_LINE_ARTILLERY, 50f,
            Set.of(ROLE_ARTILLERY, ROLE_POTTER, ROLE_COMMANDER),
            loadIcon("icons/battleLines/Artillery.png"));
    static { battleLines.add(BATTLE_LINE_ARTILLERY); }
    public static final BattleLine BATTLE_LINE_AIR_SUPPORT = new BattleLine(
            3, LanguageStrings.BATTLE_LINE_AIR_SUPPORT, 100f,
            Set.of(ROLE_AIR_SUPPORT, ROLE_AIR_BOMBER), loadIcon("icons/battleLines/AirSupport.png"));
    static { battleLines.add(BATTLE_LINE_AIR_SUPPORT); }

    static {
        SKILL_BASIC_BITE.setBattleLine(BATTLE_LINE_INFANTRY);
        SKILL_POWERFUL_BITE.setBattleLine(BATTLE_LINE_INFANTRY);
        SKILL_STINGING.setBattleLine(BATTLE_LINE_INFANTRY);
        SKILL_SHIELDING.setBattleLine(BATTLE_LINE_INFANTRY);
        SKILL_BOOST_REGEN.setBattleLine(BATTLE_LINE_ARTILLERY);
        SKILL_ACID_SPITTING.setBattleLine(BATTLE_LINE_INFANTRY);
        SKILL_ACID_ARTILLERY.setBattleLine(BATTLE_LINE_ARTILLERY);
        SKILL_SELFDESTRUCT.setBattleLine(BATTLE_LINE_INFANTRY);
        SKILL_ACIDIC_SELFDESTRUCT.setBattleLine(BATTLE_LINE_INFANTRY);
        SKILL_ARTILLERY_LEADER.setBattleLine(BATTLE_LINE_ARTILLERY);
        SKILL_INFANTRY_LEADER.setBattleLine(BATTLE_LINE_INFANTRY);
        SKILL_CLOSE_ANT_SUPPORT.setBattleLine(BATTLE_LINE_AIR_SUPPORT);
        SKILL_AIR_BOMBING.setBattleLine(BATTLE_LINE_AIR_SUPPORT);
        SKILL_SELFDESTRUCT.setSacrificesSelf(true);
        SKILL_ACIDIC_SELFDESTRUCT.setSacrificesSelf(true);
        SKILL_AIR_BOMBING.setSacrificesSelf(true);
        SKILL_ACIDIC_SELFDESTRUCT.setReplacesSkill(SKILL_SELFDESTRUCT);
    }

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

    public static List<Skill> getSkills() { return Collections.unmodifiableList(skills); }

    public static Skill getSkillById(int id) {
        for (Skill skill : skills) {
            if (skill.getId() == id) {
                return skill;
            }
        }
        return null;
    }

    public static List<WarStagePhase> getWarStagePhases() { return Collections.unmodifiableList(warStagePhases); }

    public static WarStagePhase getWarStagePhaseByPersistenceKey(String key) {
        if (key == null || key.isEmpty()) {
            return WAR_STAGE_ACTIVE_CLASH;
        }
        for (WarStagePhase phase : warStagePhases) {
            if (phase.getPersistenceKey().equals(key) || phase.getNameKey().equals(key)) {
                return phase;
            }
        }
        return WAR_STAGE_ACTIVE_CLASH;
    }

    public static List<WarStanding> getWarStandings() { return Collections.unmodifiableList(warStandings); }

    public static List<BattleLine> getBattleLines() { return Collections.unmodifiableList(battleLines); }

    public static BattleLine getBattleLineById(int id) {
        for (BattleLine line : battleLines) {
            if (line.getId() == id) {
                return line;
            }
        }
        return null;
    }

    public static BattleLine getBattleLineForRole(AntRole role) {
        if (role == null) {
            return null;
        }
        for (BattleLine line : battleLines) {
            if (line.allowsRole(role)) {
                return line;
            }
        }
        return null;
    }

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

    public static AntSubtype getAntSubtypeById(int id) {
        for (AntSubtype subtype : antSubtypes) {
            if (subtype.getId() == id) {
                return subtype;
            }
        }
        return null;
    }

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

    public static List<Rank> getColonyRanks() { return Collections.unmodifiableList(colonyRanks); }
    public static List<Tier> getTiers() { return Collections.unmodifiableList(tiers); }

    public static Tier getTierForRank(Rank rank) {
        if (rank == null) {
            return null;
        }
        for (Tier tier : tiers) {
            if (tier.getRankRequirement() == rank) {
                return tier;
            }
        }
        return null;
    }

    public static Tier getHighestUnlockedTier(Rank rank) {
        if (rank == null) {
            return null;
        }
        Tier highest = null;
        for (Tier tier : tiers) {
            Rank requirement = tier.getRankRequirement();
            if (requirement != null && rank.getId() >= requirement.getId()) {
                highest = tier;
            }
        }
        return highest;
    }

    public static long getTierMaximumRpExclusive(Tier tier) {
        if (tier == null) {
            return Long.MAX_VALUE;
        }
        int index = tiers.indexOf(tier);
        if (index < 0 || index >= tiers.size() - 1) {
            return Long.MAX_VALUE;
        }
        return tiers.get(index + 1).getMinimumRp();
    }

    public static Rank getColonyRankByKey(String key) {
        if (key == null || key.isEmpty()) {
            return RANK_ANT;
        }
        for (Rank rank : colonyRanks) {
            if (rank.getNameKey().equals(key)) {
                return rank;
            }
        }
        return RANK_ANT;
    }

    public static List<DynastyTitle> getDynastyTitles() { return Collections.unmodifiableList(dynastyTitles); }
    public static List<CityTitle> getCityTitles() { return Collections.unmodifiableList(cityTitles); }

    public static List<CityTitle> getCapitalCityTitles() {
        List<CityTitle> capitals = new ArrayList<>();
        for (CityTitle title : cityTitles) {
            if (title.isCapital()) {
                capitals.add(title);
            }
        }
        return Collections.unmodifiableList(capitals);
    }

    public static List<CityTitle> getSatelliteCityTitles() {
        List<CityTitle> satellites = new ArrayList<>();
        for (CityTitle title : cityTitles) {
            if (title.isSatellite()) {
                satellites.add(title);
            }
        }
        return Collections.unmodifiableList(satellites);
    }

    public static List<String> getGenericDynastyThemeKeys() {
        return genericDynastyThemeKeys;
    }

    public static List<String> getAllDynastyThemeKeys() {
        LinkedHashSet<String> keys = new LinkedHashSet<>(genericDynastyThemeKeys);
        for (AntSpecies s : species) {
            keys.addAll(s.getPreferredNameKeys());
        }
        return List.copyOf(keys);
    }

    public static DynastyTitle getDynastyTitleByKey(String key) {
        DynastyTitle title = findDynastyTitleByKey(dynastyTitles, key);
        return title != null ? title : DYNASTY_TITLE_DYNASTY;
    }

    public static DynastyTitle getDynastyTitleById(int id) {
        for (DynastyTitle title : dynastyTitles) {
            if (title.getId() == id) {
                return title;
            }
        }
        return DYNASTY_TITLE_DYNASTY;
    }

    public static CityTitle getCityTitleByKey(String key) {
        CityTitle title = findCityTitleByKey(key);
        return title != null ? title : CITY_TITLE_CITY;
    }

    public static CityTitle getCityTitleById(int id) {
        for (CityTitle title : cityTitles) {
            if (title.getId() == id) {
                return title;
            }
        }
        return CITY_TITLE_PRIME;
    }

    private static DynastyTitle findDynastyTitleByKey(List<DynastyTitle> titles, String key) {
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

    private static CityTitle findCityTitleByKey(String key) {
        if (key == null) {
            return null;
        }
        for (CityTitle title : cityTitles) {
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

    public static DiplomaticReputation getDiplomaticReputationLevel(int score) {
        score = GameNumbers.clampDiplomaticReputation(score);
        DiplomaticReputation level = REPUTATION_AGGRESSIVE;
        for (DiplomaticReputation candidate : diplomaticReputations) {
            if (score >= candidate.getMinScore()) {
                level = candidate;
            }
        }
        return level;
    }

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

    public static ColonyLoyalty getColonyLoyaltyLevel(int score) {
        score = GameNumbers.clampColonyLoyalty(score);
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

    public static List<AntSpecies> getSpecies() { return Collections.unmodifiableList(species); }

    public static AntSpecies getSpeciesById(int id) {
        for (AntSpecies s : species) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    public static List<AntSpecies> getNonOmniSpecies() {
        List<AntSpecies> result = new ArrayList<>();
        for (AntSpecies s : species) {
            if (s.getId() != SPECIES_OMNI.getId()) {
                result.add(s);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static List<AntSpecies> getWorldSpawnableNpcSpecies() {
        return getNonOmniSpecies();
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

    public static List<Species> getCritterSpecies() { return Collections.unmodifiableList(critterSpecies); }

    public static List<CritterClass> getCritterClasses() { return Collections.unmodifiableList(critterClasses); }

    public static List<ImageIcon> getMisc() { return Collections.unmodifiableList(misc); }
}
