package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.constants.dynasty.colony.ColonyLoyaltyModifier;
import com.grimidk.formicempire.classes.constants.dynasty.DiplomaticReputationModifier;

public final class GameNumbers {
    private GameNumbers() {}

    // --- Movement / gathering ---
    public static final int PARASITIC_MITES_ON_ANT_SPRITE = 5;
    public static final double ANT_JAW_SNAP_CHANCE_PER_HOUR = 0.08;
    public static final double ANT_ANTENNA_TWITCH_CHANCE_PER_HOUR = 0.10;
    public static final double ANT_WING_FLICK_CHANCE_PER_HOUR = 0.06;
    public static final int ANT_SPRITE_SNAP_MINUTES = 30;
    public static final int ANT_CARRY_SPRITE_NATIVE_PX = 40;
    public static final int ANT_CARRY_ICON_PX = 16;
    public static final int ANT_CARRY_JAW_OFFSET_Y = -17;
    public static final int ANT_CARRY_DUAL_OFFSET_X = 5;
    public static final int ANT_LEG_FRAME_COUNT = 4;
    public static final int ANT_LEG_FRAME_FLYING = 0;
    public static final float BASE_SPRITE_SPEED = 2.5f;
    public static final int MAX_PHYSICS_STEPS_PER_GUI_DRAIN = 2;
    public static final int MAX_PENDING_MINUTE_GUI_STEPS = 2;
    public static final int SPRITE_COMPOSITE_CACHE_MAX_ENTRIES = 2048;
    public static final int SPRITE_LAYER_CACHE_MAX_ENTRIES = 768;
    public static final int SPRITE_MERGE_ANT_THRESHOLD = 1000;
    public static final int SPRITE_MERGE_ANT_LARGE_THRESHOLD = 10000;
    public static final int SPRITE_MERGE_GROUP_SIZE = 25;
    public static final int SPRITE_MERGE_LARGE_GROUP_SIZE = 250;
    public static final int SPRITE_MERGE_POSITION_TOLERANCE_PX = 10;
    public static final int SPRITE_MERGE_POSITION_CELL_PX = SPRITE_MERGE_POSITION_TOLERANCE_PX * 2;
    public static final int SPRITE_MERGE_ZONE_THRESHOLD = 100;
    public static final double SPRITE_MERGE_ZONE_MIN_VISIBLE_FRACTION = 0.10;
    public static final int COLLECTING_AGGREGATE_RESIN_WORKER_THRESHOLD = 100;
    public static final int EATING_AGGREGATE_ANT_THRESHOLD = SPRITE_MERGE_ANT_THRESHOLD;
    public static final int GUI_HOUR_MINIMAP_REFRESH_INTERVAL = 6;
    public static final int GUI_HOUR_CONTROL_PANEL_REFRESH_INTERVAL = 6;
    public static final int NPC_DAILY_STAGGER_DAYS = 7;

    public static boolean runsNpcDailyWorkToday(int worldDay, int entityId) {
        return Math.floorMod(worldDay, NPC_DAILY_STAGGER_DAYS) == Math.floorMod(entityId, NPC_DAILY_STAGGER_DAYS);
    }
    public static final float GATHER_FULL_EFFICIENCY_RADIUS_BASE = 500f;
    public static final double GATHER_MIN_EFFICIENCY = 0.01;
    public static final float GATHER_COLONY_SPEED_RADIUS_MULT = 1.25f;

    // --- World generation ---
    public static final int WORLD_DEFAULT_CONTINENT_CORE_RADIUS = 7;
    public static final int WORLD_COASTAL_RING_COUNT = 5;
    public static final int WORLD_OUTER_OCEAN_RING_COUNT = 1;
    public static final int[] WORLD_COASTAL_LAND_CHANCE_PERCENT = { 80, 40, 20, 10, 5 };
    public static final int WORLD_MIN_ISLAND_COUNT = 6;
    public static final int WORLD_MIN_HEXES_PER_BIOME = 2;

    public static int worldRadiusForContinentCore(int continentCoreRadius) {
        return Math.max(0, continentCoreRadius)
                + WORLD_COASTAL_RING_COUNT
                + WORLD_OUTER_OCEAN_RING_COUNT;
    }

    public static final double MAP_ZOOM_MIN = 1.0;
    public static final double MAP_ZOOM_MAX = 4.0;
    public static final double MAP_ZOOM_STEP = 1.15;
    public static final int MAP_ZOOM_SLIDER_MAX = 100;
    public static final int MAP_ZOOM_SLIDER_TRACK_HEIGHT = 100;
    public static final double MAP_HEX_FIT_MIN = 10.0;
    public static final double MAP_HEX_FIT_MAX = 55.0;

    // --- Hex resources / spawn ---
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

    // --- Pets / pens ---
    public static final int PET_POOL_PER_CATCHER = 10;
    public static final int PET_CAPACITY_PER_TENDER = 10;
    public static final int PET_BREED_MIN_COUNT = 2;
    public static final int PET_BREED_DIVISOR = 10;
    public static final int PET_COUNT_SAVE_ABS_MAX = 10_000;
    public static final int MAX_PEN_NON_ANT_SPRITES = 500;
    public static final float CATCH_BASE_CHANCE_PER_CATCHER = 0.12f;

    // --- Convoy ---
    public static final int CONVOY_TUNNEL_LEG_DISTANCE = 10_000;
    public static final int CONVOY_PORTAL_APPROACH_PX = 36;

    // --- Rafting (Floodplain assimilation) ---
    public static final int RAFTING_WATER_CROSS_RANGE_1 = 1;
    public static final int RAFTING_WATER_CROSS_RANGE_2 = 2;
    public static final int RAFTING_WATER_CROSS_RANGE_3 = 3;

    // --- Woodburrow / Silkweave / Hivebuild / Web ---
    public static final double WOODBURROW_TIME_MULT = 0.8;
    public static final int SILKWEAVE_PLANT_COST_MULT = 10;
    public static final double HIVEBUILD_CAPACITY_BONUS_PER_MOUND = 0.10;
    public static final double HIVEBUILD_STORAGE_BONUS_PER_MOUND = 0.25;
    public static final double WEB_BUILDING_PROTEIN_DAILY_FRACTION = 0.10;

    // --- Mites / parasites ---
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

    // --- Ant subtypes (shared rules, not per-subtype attrs) ---
    public static final float SUBTYPE_FOOD_CONSUMPTION_ADD_PER_TRAIT = 0.5f;

    // --- Diplomacy / reputation ---
    public static final int DIPLOMATIC_REPUTATION_MIN = 0;
    public static final int DIPLOMATIC_REPUTATION_MAX = 100;
    public static final int DIPLOMATIC_REPUTATION_TIER_STEP = 20;
    public static final int DEFAULT_DIPLOMATIC_REPUTATION = 50;
    public static final int MODIFIER_PERMANENT = -1;
    public static final int CROSS_DYNASTY_TRADE_RECEIVER_REP = 20;
    public static final int CROSS_DYNASTY_TRADE_OFFER_SENDER_REP = 5;
    public static final int GENETIC_EXCHANGE_DRONE_COST = 25;

    // --- Rebellion ---
    public static final double REBELLION_MONTHLY_CHANCE_MIN = 0.05;
    public static final double REBELLION_MONTHLY_CHANCE_MAX = 0.30;
    public static final double REBELLION_JOIN_BASE_CHANCE = 0.15;
    public static final double REBELLION_JOIN_DISTANCE_WEIGHT = 0.40;
    public static final double REBELLION_JOIN_LOYALTY_WEIGHT = 0.35;
    public static final double REBELLION_NPC_FIGHT_CHANCE_MIN = 0.20;
    public static final double REBELLION_NPC_FIGHT_CHANCE_MAX = 0.80;
    public static final double REBELLION_NPC_FIGHT_RATIO_LOW = 0.85;
    public static final double REBELLION_NPC_FIGHT_RATIO_HIGH = 1.15;

    // --- Genetic integrity ---
    public static final double GENETIC_INTEGRITY_START = 100.0;
    public static final double GENETIC_INTEGRITY_SATELLITE_PENALTY = 1.0;
    public static final double GENETIC_INTEGRITY_ASSIMILATION_FLOOR_STEP = 5.0;

    // --- Colony loyalty ---
    public static final int COLONY_LOYALTY_MIN = 0;
    public static final int COLONY_LOYALTY_MAX = 100;
    public static final int COLONY_LOYALTY_TIER_STEP = 20;
    public static final int DEFAULT_COLONY_LOYALTY = 50;
    public static final int LOYALTY_CAPITAL_DISTANCE_NEUTRAL = 1;
    public static final int LOYALTY_CAPITAL_DISTANCE_MAX = 10;
    public static final int LOYALTY_CAPITAL_DISTANCE_PENALTY_MAX = 10;
    public static final int PHEROMONE_STORM_SYRUP_COST = 500;
    public static final int PHEROMONE_STORM_DURATION_MONTHS = 12;
    public static final int RECENTLY_CONQUERED_LOYALTY_MONTHS = 6;
    public static final int RECENTLY_INTEGRATED_LOYALTY_MONTHS = 6;

    // --- Military ---
    public static final int MILITARY_BASELINE_HEALTH = 100;
    public static final int MILITARY_BASELINE_ATTACK = 10;
    public static final int MILITARY_BASELINE_DEFENSE = 0;
    public static final float DEFENSE_PERCENT_MIN = 0f;
    public static final float DEFENSE_PERCENT_MAX = 100f;
    public static final int ANT_REGEN_PERCENT_BASE = 10;
    public static final float BOOST_REGEN_NEXT_REDEPLOY_MULT = 2f;
    public static final int MILITARY_BASELINE_ATTACK_SPEED = 1;
    public static final float STAT_HEALTH_1_BONUS = 0.50f;
    public static final float STAT_HEALTH_2_BONUS = 0.80f;
    public static final float STAT_ATTACK_1_BONUS = 0.30f;
    public static final float STAT_ATTACK_2_BONUS = 0.50f;
    public static final int STAT_DEFENSE_FLAT_BONUS = 5;
    public static final int STAT_ATTACK_SPEED_1_FLAT = 1;
    public static final float ASSIMILATED_DAMAGE_ADD_FIRE = 0.5f;
    public static final float ASSIMILATED_DAMAGE_ADD_DEADLY = 0.5f;
    public static final float ASSIMILATED_DAMAGE_SYNERGY_FIRE_DEADLY = 3f;
    public static final float ASSIMILATED_ATTACK_SPEED_MULT_FASTBITE = 2f;
    public static final float ASSIMILATED_JUMPING_MELEE_DAMAGE_MULT = 1.2f;
    public static final float ASSIMILATED_SWARMING_COMBAT_CAPACITY_MULT = 1.5f;
    public static final float ASSIMILATED_LOCSENSE_SPEED_MULT = 1.5f;
    public static final double ASSIMILATED_LOCSENSE_CONVOY_SECURITY_FLAT = 20.0;
    public static final float MILITARY_STRENGTH_RATIO_MAX = 11f;
    public static final int MILITARY_STRENGTH_DELTA_MAX = 10;

    // --- Integration / calendar ---
    public static final float INTEGRATION_MILITARY_RATIO_REQUIRED = 5f;
    public static final int INTEGRATION_MONTHS_PER_COLONY_SLOW = 30;
    public static final int INTEGRATION_MIN_DIPLOMATS = 1;
    public static final int DAYS_PER_MONTH = 30;
    public static final int AI_FORCED_FLIGHT_COOLDOWN_DAYS = 30;
    public static final int FORCED_FLIGHT_BASE_COST = 200;
    public static final int MASS_FLIGHT_COST_MULTIPLIER = 5;

    // --- War ---
    public static final int WAR_PACT_BREAK_COOLDOWN_MONTHS = 6;
    public static final int DIPLO_DECLINED_REQUEST_COOLDOWN_MONTHS = 1;
    public static final int DIPLO_PENDING_PACT_REQUEST_QUEUE_MAX = 1;
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
    public static final float WAR_STAGE_PROGRESS_PER_HOUR = 0.13f;
    public static final float WAR_STAGE_PROGRESS_PER_DAY = WAR_STAGE_PROGRESS_PER_HOUR * 24f;
    public static final int WAR_REDEPLOY_HOURS = 24;
    public static final float WAR_HEX_DEFENDING_STAT_MULT = 1.5f;
    public static final float WAR_HEX_DEFENDER_ROLE_STAT_MULT = 3f;
    public static final float WAR_HEX_SIEGE_ATTACKER_STAT_MULT = 3f;
    public static final float WAR_HEX_ATTACKER_STAT_MULT = 1f;
    public static final float WAR_HEX_DEFENSE_POWER_MULTIPLIER = WAR_HEX_DEFENDING_STAT_MULT;
    public static final float WAR_AI_FALLBACK_MAX_POWER_RATIO = 1f;
    public static final float WAR_AI_FALLBACK_RECOVERY_RATIO = 0.75f;
    public static final int WAR_AI_FALLBACK_MIN_ACTIVE = 500;
    public static final int WAR_AI_FALLBACK_MIN_SPARE_COLONIES = 3;
    public static final double AI_WAR_FALLBACK_CHANCE = 0.06;
    public static final double AI_WAR_HEX_BAIT_CHANCE = 0.14;
    public static final float WAR_AI_HEX_BAIT_MIN_HEX_ODDS = 1.05f;
    public static final float WAR_AI_HEX_BAIT_ODDS_IMPROVEMENT = 1.2f;
    public static final float WAR_AI_HEX_BAIT_MIN_COUNTER_ODDS = 0.9f;
    public static final float WAR_BATTLE_ARMY_DEFEAT_RATIO = 0.9f;

    public static final int COMBAT_CAPACITY_BASE = 1000;
    public static final int COMBAT_CAPACITY_WITH_COMMANDER = 2500;  
    public static final int COMMANDER_MAX_PER_COLONY = 1;
    public static final int TRIGGER_COMMANDER_MIN_WARS = 3;
    public static final int TRIGGER_COMMANDER_MIN_QUEENS_IN_COLONY = 2;
    public static final float COMMANDER_ARTILLERY_DAMAGE_BONUS = 0.5f;
    public static final float CAPTAIN_INFANTRY_DAMAGE_BONUS = 0.25f;
    public static final int WARMONGER_DECLARED_WARS_THRESHOLD = 5;

    // --- AI / trade automation ---
    public static final int AI_EXPANSION_COLONY_TARGET = 6;
    public static final int AI_MILITARIST_EXPANSION_COLONY_TARGET = 8;
    public static final int AI_PACIFIST_EXPANSION_COLONY_TARGET = 5;
    public static final double AI_MILITARIST_WAR_CHANCE_MULT = 2.25;
    public static final double AI_PACIFIST_WAR_CHANCE_MULT = 0.35;
    public static final int AI_MILITARIST_REPUTATION_DELTA = -12;
    public static final int AI_PACIFIST_REPUTATION_DELTA = 10;
    public static final int AI_MIN_BREEDERS_FOR_FLIGHT = 1;
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
    public static final int AUTO_UPGRADE_MIN_RECURRENT_ROUTES = 10;

    // --- Triggers ---
    public static final int TRIGGER_GRAVER_DEAD_ANTS = 100;
    public static final int TRIGGER_RESEARCH_MIN_RP = 100;
    public static final int TRIGGER_MASS_FLIGHT_MIN_NUPTIALS = 10;
    public static final int TRIGGER_BILATERAL_MIN_TRADES = 5;
    public static final int TRIGGER_SCOUT_PLANT_COLLECTED = 6000;
    public static final int TRIGGER_MINER_TIER3_BUILDINGS = 10;
    public static final double MINING_GATHER_SUCCESS_CHANCE = 0.10;
    public static final double RESIN_FORAGE_BONUS_CHANCE = 0.01;
    public static final int TRIGGER_DYNASTY_MIN_COLONIES = 2;
    public static final int TRIGGER_TRADE_MIN_COLONIES = 3;
    public static final int TRIGGER_MANAGEMENT_MIN_COLONIES = 4;
    public static final int TRIGGER_SPREAD_2_MIN_COLONIES = 5;
    public static final int TRIGGER_AUTOMATION_MIN_COLONIES = 7;
    public static final int TRIGGER_RESEARCHER_MIN_MONTHS = 2;
    public static final int TRIGGER_ASSIMILATION_MIN_ABSORBED = 1;
    public static final int TRIGGER_NPC_RESEARCHER_MIN_ANTS = 20;
    public static final int TRIGGER_NPC_GRAVER_DEAD_ANTS = 20;
    public static final int TRIGGER_NPC_SCOUT_MIN_ANTS = 50;
    public static final double TRIGGER_NPC_SCOUT_FOOD_RATIO = 0.2;
    public static final int TRIGGER_NPC_ABILITY_MENU_MIN_RP = 4000;
    public static final int PARASITE_ANT_OUTBREAK_MIN_POPULATION = 1000;
    public static final int DIPLOMAT_STABILITY_GAIN_BASE = 1;
    public static final int DIPLOMAT_STABILITY_GAIN_PRESSURE_2 = 3;
    public static final int DIPLOMAT_STABILITY_GAIN_PRESSURE_3 = 5;
    public static final int SPY_POWER_BASE = DIPLOMAT_STABILITY_GAIN_BASE;
    public static final int SPY_POWER_PRESSURE_2 = DIPLOMAT_STABILITY_GAIN_PRESSURE_2;
    public static final int SPY_POWER_PRESSURE_3 = DIPLOMAT_STABILITY_GAIN_PRESSURE_3;
    public static final int SPY_MAX_PER_DYNASTY_MISSION = DIPLOMAT_MAX_PER_DYNASTY_MISSION;
    public static final double SPY_INTEL_POWER_DIVISOR = 10.0;
    public static final double SPY_CI_REDUCTION_PER_POINT = 0.1;
    public static final double COUNTER_INTELLIGENCE_START = 5.0;
    public static final double COUNTER_INTELLIGENCE_MAX = 50.0;
    public static final double INTELLIGENCE_MAX = 100.0;
    public static final double SPY_THEFT_FRACTION_MIN = 0.01;
    public static final double SPY_THEFT_FRACTION_MAX = 0.03;
    public static final int CAUGHT_SPYING_DURATION_DAYS = DAYS_PER_MONTH * 12;

    // --- Construction ---
    public static final double TUNNEL_WORK_REQUIRED = 5000000.0;

    // --- Research ---
    public static final int RESEARCH_ASSISTANT_EFFICIENCY_DIVISOR = 50;

    // --- Audio ---
    public static final int VOLUME_MIN_PERCENT = 0;
    public static final int VOLUME_MAX_PERCENT = 100;
    public static final int VOLUME_DEFAULT_PERCENT = 50;
    public static final int VOLUME_STEP_PERCENT = 10;

    // --- Numeric helpers ---

    public static int snapVolumePercent(int volume) {
        int clamped = Math.max(VOLUME_MIN_PERCENT, Math.min(VOLUME_MAX_PERCENT, volume));
        int stepped = ((clamped + VOLUME_STEP_PERCENT / 2) / VOLUME_STEP_PERCENT) * VOLUME_STEP_PERCENT;
        return Math.max(VOLUME_MIN_PERCENT, Math.min(VOLUME_MAX_PERCENT, stepped));
    }

    public static int capPenNonAntSprites(int count) {
        return Math.min(Math.max(0, count), MAX_PEN_NON_ANT_SPRITES);
    }

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

    public static int warHexDefenseEffectivePower(int baseReservePower) {
        return warHexDefenseEffectiveDefenderPower(baseReservePower, 0);
    }

    public static int warHexDefenseEffectiveDefenderPower(int standardDefendingPower, int defenderRolePower) {
        int standard = Math.max(0, standardDefendingPower);
        int defenders = Math.max(0, defenderRolePower);
        long effective = Math.round(standard * (double) WAR_HEX_DEFENDING_STAT_MULT)
                + Math.round(defenders * (double) WAR_HEX_DEFENDER_ROLE_STAT_MULT);
        if (effective <= 0 && (standard > 0 || defenders > 0)) {
            return 1;
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, effective));
    }

    public static int warHexAssaultEffectiveAttackerPower(int nonSiegePower, int siegePower) {
        int nonSiege = Math.max(0, nonSiegePower);
        int siege = Math.max(0, siegePower);
        long effective = Math.round(nonSiege * (double) WAR_HEX_ATTACKER_STAT_MULT)
                + Math.round(siege * (double) WAR_HEX_SIEGE_ATTACKER_STAT_MULT);
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, effective));
    }

    public static int warHexDefenseEffectiveLossToActual(int effectiveLoss) {
        return warHexDefenseEffectiveLossToActual(effectiveLoss, WAR_HEX_DEFENDING_STAT_MULT);
    }

    public static int warHexDefenseEffectiveLossToActual(int effectiveLoss, float averageMultiplier) {
        if (effectiveLoss <= 0 || averageMultiplier <= 0f) {
            return 0;
        }
        return Math.max(0, Math.round(effectiveLoss / averageMultiplier));
    }

    public static int warHexDefenseEffectiveLossToActual(int effectiveLoss, int basePower, int effectivePower) {
        if (effectiveLoss <= 0 || basePower <= 0 || effectivePower <= 0) {
            return 0;
        }
        return Math.max(0, Math.round(effectiveLoss * (basePower / (float) effectivePower)));
    }

    public static float applyHexDefenseAttack(float baseAttack, float statMult) {
        if (baseAttack <= 0f || statMult <= 0f) {
            return 0f;
        }
        return baseAttack * statMult;
    }

    public static float applyHexDefenseDefense(float baseDefensePercent, float statMult) {
        if (statMult <= 0f) {
            return clampDefensePercent(0f);
        }
        return clampDefensePercent(baseDefensePercent * statMult);
    }

    public static int clampDiplomaticReputation(int score) {
        return Math.max(DIPLOMATIC_REPUTATION_MIN, Math.min(DIPLOMATIC_REPUTATION_MAX, score));
    }

    public static int clampColonyLoyalty(int score) {
        return Math.max(COLONY_LOYALTY_MIN, Math.min(COLONY_LOYALTY_MAX, score));
    }

    public static float clampDefensePercent(float defensePercent) {
        if (Float.isNaN(defensePercent) || Float.isInfinite(defensePercent)) {
            return DEFENSE_PERCENT_MIN;
        }
        return Math.max(DEFENSE_PERCENT_MIN, Math.min(DEFENSE_PERCENT_MAX, defensePercent));
    }

    public static int clampDefensePercent(int defensePercent) {
        return Math.round(clampDefensePercent((float) defensePercent));
    }

    public static float damageAfterDefense(float rawDamage, float defensePercent) {
        if (rawDamage <= 0f) {
            return 0f;
        }
        float reduction = clampDefensePercent(defensePercent) / 100f;
        return rawDamage * (1f - reduction);
    }

    public static float regenAmountFromPercent(float maxHealth, float regenPercent) {
        if (maxHealth <= 0f || regenPercent <= 0f) {
            return 0f;
        }
        return maxHealth * (regenPercent / 100f);
    }

    public static double computeIntegrationMonthsPerColony(double diplomatsPerColony) {
        if (diplomatsPerColony <= 0) {
            return INTEGRATION_MONTHS_PER_COLONY_SLOW;
        }
        return INTEGRATION_MONTHS_PER_COLONY_SLOW / Math.sqrt(diplomatsPerColony);
    }
}
