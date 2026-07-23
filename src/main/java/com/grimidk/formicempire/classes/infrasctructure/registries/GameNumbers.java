package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.constants.dynasty.colony.ColonyLoyaltyModifier;
import com.grimidk.formicempire.classes.constants.dynasty.DiplomaticReputationModifier;

public final class GameNumbers {
    private GameNumbers() {}

    // --- Movement / gathering ---
    public static final float BASE_SPRITE_SPEED = 2.5f;
    public static final int PARASITIC_MITES_ON_ANT_SPRITE = 5;
    public static final float GATHER_FULL_EFFICIENCY_RADIUS_BASE = 500f;
    public static final double GATHER_MIN_EFFICIENCY = 0.01;
    public static final float GATHER_COLONY_SPEED_RADIUS_MULT = 1.25f;

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

    // --- Ant subtypes ---
    public static final int SUBTYPE_DIGIT_NONE = 1;
    public static final float SUBTYPE_DAMAGE_MULT_STINGER = 1.5f;
    public static final float SUBTYPE_ATTACK_MULT_TRAPJAW = 1.5f;
    /** Doorhead: flat extra defense percent (damage reduction). */
    public static final float SUBTYPE_DEFENSE_ADD_DOORHEAD = 20f;
    public static final float SUBTYPE_FORAGE_MULT_HONEYPOT = 4f;
    public static final float SUBTYPE_SPEED_MULT_HONEYPOT = 0.75f;
    /** Honeypot abdomen: regen is this × colony/type regen (15% more). */
    public static final float SUBTYPE_REGEN_MULT_HONEYPOT = 1.15f;
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
    public static final int MILITARY_WEIGHT_WORKER = 1;
    public static final int MILITARY_WEIGHT_SOLDIER = 5;
    public static final int MILITARY_WEIGHT_MAJOR = 15;
    public static final int MILITARY_WEIGHT_PRINCESS = 10;
    public static final int MILITARY_WEIGHT_QUEEN = 50;
    public static final int MILITARY_BASELINE_HEALTH = 100;
    public static final int MILITARY_BASELINE_ATTACK = 10;
    /** Colony-wide defense baseline (ants use type absolute % instead). */
    public static final int MILITARY_BASELINE_DEFENSE = 0;
    /** Defense is percent damage reduction; always clamp to this range. */
    public static final float DEFENSE_PERCENT_MIN = 0f;
    public static final float DEFENSE_PERCENT_MAX = 100f;
    /** Major / queen innate defense percent. */
    public static final float ANT_DEFENSE_PERCENT_MAJOR = 20f;
    public static final float ANT_DEFENSE_PERCENT_QUEEN = 20f;
    /** Divisor for military-power defense factor from ant defense %. */
    public static final float MILITARY_DEFENSE_FACTOR_BASELINE = ANT_DEFENSE_PERCENT_MAJOR;
    /** Colony skeleton regen baseline: percent of max HP recovered per tick. */
    public static final int ANT_REGEN_PERCENT_BASE = 10;
    public static final int MILITARY_BASELINE_ATTACK_SPEED = 1;
    /** Each venom assimilation adds this fraction to colony attack (0.5 = +50%). */
    public static final float ASSIMILATED_DAMAGE_ADD_FIRE = 0.5f;
    public static final float ASSIMILATED_DAMAGE_ADD_DEADLY = 0.5f;
    /** Super Venom absolute attack mult: replaces both +50% bonuses with a +200% bonus (3×). */
    public static final float ASSIMILATED_DAMAGE_SYNERGY_FIRE_DEADLY = 3f;
    public static final float ASSIMILATED_ATTACK_SPEED_MULT_FASTBITE = 2f;
    public static final float MILITARY_STRENGTH_RATIO_MAX = 11f;
    public static final int MILITARY_STRENGTH_DELTA_MAX = 10;

    // --- Integration / calendar ---
    public static final float INTEGRATION_MILITARY_RATIO_REQUIRED = 5f;
    public static final int INTEGRATION_MONTHS_PER_COLONY_SLOW = 30;
    public static final int INTEGRATION_MIN_DIPLOMATS = 1;
    public static final int DAYS_PER_MONTH = 30;
    public static final int AI_FORCED_FLIGHT_COOLDOWN_DAYS = 30;

    // --- War ---
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
    /** Stage capture progress added each war hour (0–1 scale); ~8 hours per hex at 13%/hour. */
    public static final float WAR_STAGE_PROGRESS_PER_HOUR = 0.13f;
    public static final float WAR_STAGE_PROGRESS_PER_DAY = WAR_STAGE_PROGRESS_PER_HOUR * 24f;
    public static final int WAR_REDEPLOY_HOURS = 24;
    /** Hex defenders fight at +50% effective military power during local reserve/hex defense. */
    public static final float WAR_HEX_DEFENSE_POWER_MULTIPLIER = 1.5f;
    public static final float WAR_AI_FALLBACK_MAX_POWER_RATIO = 1f;
    public static final float WAR_AI_FALLBACK_RECOVERY_RATIO = 0.75f;
    public static final int WAR_AI_FALLBACK_MIN_ACTIVE = 500;
    public static final int WAR_AI_FALLBACK_MIN_SPARE_COLONIES = 3;
    public static final double AI_WAR_FALLBACK_CHANCE = 0.06;

    // --- AI / trade automation ---
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

    // --- Triggers ---
    public static final int TRIGGER_GRAVER_DEAD_ANTS = 100;
    public static final int TRIGGER_RESEARCH_MIN_RP = 100;
    public static final int TRIGGER_MASS_FLIGHT_MIN_NUPTIALS = 10;
    public static final int TRIGGER_BILATERAL_MIN_TRADES = 5;
    public static final int TRIGGER_SCOUT_PLANT_COLLECTED = 6000;
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

    // --- Construction ---
    public static final double TUNNEL_WORK_REQUIRED = 5000000.0;

    // --- Research ---
    /** Lab assistants contribute at 1/N of researcher-queen efficiency. */
    public static final int RESEARCH_ASSISTANT_EFFICIENCY_DIVISOR = 50;

    // --- Numeric helpers ---

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
        if (baseReservePower <= 0) {
            return 0;
        }
        return Math.max(1, Math.round(baseReservePower * WAR_HEX_DEFENSE_POWER_MULTIPLIER));
    }

    public static int warHexDefenseEffectiveLossToActual(int effectiveLoss) {
        if (effectiveLoss <= 0) {
            return 0;
        }
        return Math.max(0, Math.round(effectiveLoss / WAR_HEX_DEFENSE_POWER_MULTIPLIER));
    }

    public static int clampDiplomaticReputation(int score) {
        return Math.max(DIPLOMATIC_REPUTATION_MIN, Math.min(DIPLOMATIC_REPUTATION_MAX, score));
    }

    public static int clampColonyLoyalty(int score) {
        return Math.max(COLONY_LOYALTY_MIN, Math.min(COLONY_LOYALTY_MAX, score));
    }

    /** Defense is percent damage reduction in {@code [0, 100]}. */
    public static float clampDefensePercent(float defensePercent) {
        if (Float.isNaN(defensePercent) || Float.isInfinite(defensePercent)) {
            return DEFENSE_PERCENT_MIN;
        }
        return Math.max(DEFENSE_PERCENT_MIN, Math.min(DEFENSE_PERCENT_MAX, defensePercent));
    }

    public static int clampDefensePercent(int defensePercent) {
        return Math.round(clampDefensePercent((float) defensePercent));
    }

    /** Applies percent damage reduction; {@code defensePercent} is clamped to 0–100. */
    public static float damageAfterDefense(float rawDamage, float defensePercent) {
        if (rawDamage <= 0f) {
            return 0f;
        }
        float reduction = clampDefensePercent(defensePercent) / 100f;
        return rawDamage * (1f - reduction);
    }

    /** {@code regenPercent} is % of max HP recovered; result is HP restored this tick. */
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
