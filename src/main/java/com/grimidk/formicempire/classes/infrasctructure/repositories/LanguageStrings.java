package com.grimidk.formicempire.classes.infrasctructure.repositories;

import com.grimidk.formicempire.classes.infrasctructure.repositories.translations.EnglishTranslation;
import com.grimidk.formicempire.classes.infrasctructure.repositories.translations.FrenchTranslation;
import com.grimidk.formicempire.classes.infrasctructure.repositories.translations.PortugueseTranslation;
import com.grimidk.formicempire.classes.infrasctructure.repositories.translations.SpanishTranslation;
import com.grimidk.formicempire.classes.infrasctructure.repositories.translations.Translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LanguageStrings {
    private LanguageStrings() {}

    private static String currentLanguage = "en";
    private static final Map<String, Map<String, String>> translations = new HashMap<>();
    private static final Map<String, String> languageNames = new HashMap<>();
    
    private static final List<Runnable> listeners = new ArrayList<>();

    static {
        registerLanguage("en", new EnglishTranslation());
        registerLanguage("es", new SpanishTranslation());
        registerLanguage("fr", new FrenchTranslation());
        registerLanguage("pt", new PortugueseTranslation());
    }

    public static void registerLanguage(String code, Translation translation) {
        translations.put(code, translation.getStrings());
        languageNames.put(code, translation.getLanguageName());
    }

    public static void setLanguage(String lang) {
        if (translations.containsKey(lang)) {
            currentLanguage = lang;
            notifyListeners();
        }
    }
    
    public static String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public static List<String> getAvailableLanguageCodes() {
        return new ArrayList<>(translations.keySet());
    }
    
    public static String getLanguageName(String code) {
        return languageNames.getOrDefault(code, code);
    }

    public static String get(String key) {
        Map<String, String> langMap = translations.get(currentLanguage);
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        // Fallback to English
        langMap = translations.get("en");
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        return key;
    }

    public static String formatPlayerDynastyName(String baseName) {
        return String.format(get(DYNASTY_PLAYER_NAME_FMT), baseName);
    }

    public static String stripDynastyNameSuffix(String dynastyName) {
        if (dynastyName == null) {
            return null;
        }
        String localizedSuffix = String.format(get(DYNASTY_PLAYER_NAME_FMT), "");
        if (dynastyName.endsWith(localizedSuffix)) {
            return dynastyName.substring(0, dynastyName.length() - localizedSuffix.length());
        }
        String legacySuffix = " Dynasty";
        if (dynastyName.endsWith(legacySuffix)) {
            return dynastyName.substring(0, dynastyName.length() - legacySuffix.length());
        }
        return dynastyName;
    }

    public static boolean isAutosaveSaveName(String name) {
        return "Autosave".equals(name) || get(SAVE_AUTOSAVE_NAME).equals(name);
    }

    /** True for default slot labels (not a user-chosen colony/world name). */
    public static boolean isGenericSaveName(String name, int slotId) {
        if (name == null || name.trim().isEmpty()) {
            return true;
        }
        String trimmed = name.trim();
        if (isAutosaveSaveName(trimmed)) {
            return true;
        }
        if (("Save " + slotId).equals(trimmed)) {
            return true;
        }
        return String.format(get(SAVE_DEFAULT_NAME_FMT), slotId).equals(trimmed);
    }
    
    public static void addListener(Runnable listener) {
        listeners.add(listener);
    }
    
    public static void removeListener(Runnable listener) {
        listeners.remove(listener);
    }
    
    private static void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    // --- Keys ---
    public static final String UI_APP_TITLE = "UI_APP_TITLE";
    public static final String UI_BACK_TO_GAME = "UI_BACK_TO_GAME";
    public static final String UI_BACK_TO_MENU = "UI_BACK_TO_MENU";
    public static final String UI_MENU = "UI_MENU";
    public static final String UI_SETTINGS = "UI_SETTINGS";
    public static final String UI_TUTORIAL = "UI_TUTORIAL";
    public static final String UI_PAUSE = "UI_PAUSE";
    public static final String UI_PLAY = "UI_PLAY";
    public static final String UI_SPEED_UP = "UI_SPEED_UP";
    public static final String UI_SPEED_DOWN = "UI_SPEED_DOWN";
    public static final String UI_SPEED_VERY_SLOW = "UI_SPEED_VERY_SLOW";
    public static final String UI_SPEED_SLOW = "UI_SPEED_SLOW";
    public static final String UI_SPEED_NORMAL = "UI_SPEED_NORMAL";
    public static final String UI_SPEED_FAST = "UI_SPEED_FAST";
    public static final String UI_SPEED_VERY_FAST = "UI_SPEED_VERY_FAST";
    public static final String UI_SPEED_TURBO = "UI_SPEED_TURBO";
    public static final String UI_TICK_PREFIX = "UI_TICK_PREFIX";
    public static final String UI_MS_SUFFIX = "UI_MS_SUFFIX";
    public static final String UI_PAUSED_TICK = "UI_PAUSED_TICK";
    public static final String UI_NOT_STARTED = "UI_NOT_STARTED";
    public static final String UI_STARTING = "UI_STARTING";
    public static final String UI_RUNNING = "UI_RUNNING";
    public static final String UI_ERROR_LOADING = "UI_ERROR_LOADING";
    public static final String UI_LOADING_WAIT = "UI_LOADING_WAIT";
    public static final String UI_CLOSE = "UI_CLOSE";
    public static final String UI_SAVE = "UI_SAVE";
    public static final String UI_DELETE = "UI_DELETE";
    public static final String UI_CANCEL = "UI_CANCEL";
    public static final String UI_CONFIRM = "UI_CONFIRM";
    public static final String UI_LEVEL = "UI_LEVEL";
    public static final String UI_TOTAL = "UI_TOTAL";
    public static final String UI_UPGRADE = "UI_UPGRADE";
    public static final String UI_LOCKED = "UI_LOCKED";
    public static final String UI_UNLOCKED = "UI_UNLOCKED";
    public static final String UI_COST = "UI_COST";
    public static final String UI_REQUIREMENTS = "UI_REQUIREMENTS";
    public static final String UI_BUY = "UI_BUY";
    public static final String UI_BUILD = "UI_BUILD";
    public static final String UI_BEGIN = "UI_BEGIN";
    public static final String UI_TRIGGER = "UI_TRIGGER";
    public static final String UI_ENABLED = "UI_ENABLED";
    public static final String UI_DISABLED = "UI_DISABLED";
    public static final String UI_HEALTHY = "UI_HEALTHY";
    public static final String UI_MISSING = "UI_MISSING";
    public static final String UI_COMBINED = "UI_COMBINED";
    public static final String UI_SUMMARY = "UI_SUMMARY";
    public static final String UI_MAX = "UI_MAX";
    public static final String UI_TRANSIT = "UI_TRANSIT";
    public static final String UI_RETURNING = "UI_RETURNING";
    public static final String UI_MODIFIED = "UI_MODIFIED";
    public static final String UI_BILATERAL = "UI_BILATERAL";
    public static final String UI_ESTABLISH = "UI_ESTABLISH";
    public static final String UI_MANAGE = "UI_MANAGE";
    public static final String UI_EDIT = "UI_EDIT";
    public static final String UI_VIEW = "UI_VIEW";
    public static final String UI_MATURING = "UI_MATURING";
    public static final String UI_BACK = "UI_BACK";
    public static final String UI_CREATE = "UI_CREATE";
    public static final String UI_LOAD = "UI_LOAD";
    public static final String UI_HELP = "UI_HELP";
    public static final String UI_ROADMAP = "UI_ROADMAP";
    public static final String UI_QUIT = "UI_QUIT";
    public static final String INTRO_WARNING = "INTRO_WARNING";
    public static final String ROADMAP_TITLE = "ROADMAP_TITLE";
    public static final String ROADMAP_UNAVAILABLE = "ROADMAP_UNAVAILABLE";
    public static final String UI_CREDITS = "UI_CREDITS";
    public static final String CREDITS_TITLE = "CREDITS_TITLE";
    public static final String CREDITS_UNAVAILABLE = "CREDITS_UNAVAILABLE";
    public static final String UI_ERROR = "UI_ERROR";
    public static final String UI_NEXT = "UI_NEXT";
    public static final String UI_FINISH = "UI_FINISH";
    
    public static final String UI_CONFIRM_EXIT_TITLE = "UI_CONFIRM_EXIT_TITLE";
    public static final String UI_CONFIRM_EXIT_MSG = "UI_CONFIRM_EXIT_MSG";

    public static final String PANEL_ALERTS = "PANEL_ALERTS";
    public static final String PANEL_NO_ALERTS = "PANEL_NO_ALERTS";
    public static final String PANEL_TIME = "PANEL_TIME";
    public static final String PANEL_WORLD = "PANEL_WORLD";
    public static final String PANEL_COLONY = "PANEL_COLONY";
    public static final String PANEL_RESOURCES = "PANEL_RESOURCES";
    public static final String PANEL_ANTS = "PANEL_ANTS";
    public static final String PANEL_POPULATION = "PANEL_POPULATION";
    public static final String PANEL_STATS = "PANEL_STATS";
    public static final String PANEL_COLONY_STATS = "PANEL_COLONY_STATS";
    public static final String PANEL_NO_COLONY = "PANEL_NO_COLONY";
    public static final String PANEL_TRADE = "PANEL_TRADE";

    public static final String MENU_WORLD_MAP = "MENU_WORLD_MAP";
    public static final String MENU_STATS = "MENU_STATS";
    public static final String MENU_ROLES = "MENU_ROLES";
    public static final String MENU_HATCH_RATES = "MENU_HATCH_RATES";
    public static final String MENU_RESEARCH = "MENU_RESEARCH";
    public static final String MENU_BUILD = "MENU_BUILD";
    public static final String MENU_ASSIMILATION = "MENU_ASSIMILATION";
    public static final String MENU_SYNERGY = "MENU_SYNERGY";
    public static final String MENU_ABILITIES = "MENU_ABILITIES";
    public static final String MENU_DYNASTY = "MENU_DYNASTY";
    public static final String MENU_TRADE = "MENU_TRADE";
    public static final String MENU_TOGGLE_VIEW = "MENU_TOGGLE_VIEW";

    public static final String COLONY_RANK = "COLONY_RANK";
    public static final String COLONY_TOTAL_RESOURCES = "COLONY_TOTAL_RESOURCES";
    public static final String COLONY_TOTAL_ANTS = "COLONY_TOTAL_ANTS";
    public static final String COLONY_QUEENS = "COLONY_QUEENS";
    public static final String COLONY_CONSUMPTION = "COLONY_CONSUMPTION";
    public static final String COLONY_MAX_FOOD_PROD = "COLONY_MAX_FOOD_PROD";
    public static final String COLONY_NET_FOOD = "COLONY_NET_FOOD";
    public static final String COLONY_LAYING_RATE = "COLONY_LAYING_RATE";
    public static final String COLONY_NURSE_COVERAGE = "COLONY_NURSE_COVERAGE";
    public static final String COLONY_GRAVE_CLEANING = "COLONY_GRAVE_CLEANING";
    public static final String COLONY_PET_INSECTS = "COLONY_PET_INSECTS";
    public static final String COLONY_PARASITIC_MITES = "COLONY_PARASITIC_MITES";
    public static final String COLONY_PARASITE_ANTS = "COLONY_PARASITE_ANTS";
    public static final String COLONY_POLICING = "COLONY_POLICING";
    public static final String COLONY_DETECTION_RATE = "COLONY_DETECTION_RATE";
    public static final String COLONY_RESEARCH = "COLONY_RESEARCH";
    public static final String COLONY_RESEARCH_RATE = "COLONY_RESEARCH_RATE";
    public static final String COLONY_JUVENILE_ANTS = "COLONY_JUVENILE_ANTS";
    public static final String COLONY_ADULT_ANTS = "COLONY_ADULT_ANTS";
    public static final String COLONY_MILITARY_POWER = "COLONY_MILITARY_POWER";
    
    public static final String TOOLTIP_PET_INSECTS = "TOOLTIP_PET_INSECTS";
    public static final String TOOLTIP_PARASITIC_MITES = "TOOLTIP_PARASITIC_MITES";
    public static final String TOOLTIP_PARASITE_ANTS = "TOOLTIP_PARASITE_ANTS";
    public static final String TOOLTIP_POLICING = "TOOLTIP_POLICING";
    public static final String TOOLTIP_RESEARCH_POINTS = "TOOLTIP_RESEARCH_POINTS";

    public static final String DIALOG_UPGRADES_TITLE = "DIALOG_UPGRADES_TITLE";
    public static final String TAB_RESEARCH = "TAB_RESEARCH";
    public static final String TAB_CONSTRUCTION = "TAB_CONSTRUCTION";
    public static final String TAB_ASSIMILATIONS = "TAB_ASSIMILATIONS";
    public static final String TAB_SYNERGIES = "TAB_SYNERGIES";
    public static final String UPGRADE_RESEARCH_AVAILABLE = "UPGRADE_RESEARCH_AVAILABLE";
    public static final String UPGRADE_NO_RESEARCH = "UPGRADE_NO_RESEARCH";
    public static final String UPGRADE_COST_RP = "UPGRADE_COST_RP";
    public static final String UPGRADE_NOT_ENOUGH_RP = "UPGRADE_NOT_ENOUGH_RP";
    public static final String BUILD_NO_CONSTRUCTIONS = "BUILD_NO_CONSTRUCTIONS";
    public static final String BUILD_UNDER_CONSTRUCTION = "BUILD_UNDER_CONSTRUCTION";
    public static final String BUILD_PROGRESS_HOURS = "BUILD_PROGRESS_HOURS";
    public static final String BUILD_STATUS_BUILDERS = "BUILD_STATUS_BUILDERS";
    public static final String BUILD_STATUS_CRANES = "BUILD_STATUS_CRANES";
    public static final String BUILD_STATUS_SPEED = "BUILD_STATUS_SPEED";
    public static final String BUILD_REQUIREMENT_ERROR = "BUILD_REQUIREMENT_ERROR";
    public static final String BUILD_RESOURCES_ERROR = "BUILD_RESOURCES_ERROR";
    public static final String BUILD_COST_FORMAT = "BUILD_COST_FORMAT";
    public static final String ASSIMILATION_CURRENT = "ASSIMILATION_CURRENT";
    public static final String ASSIMILATION_NONE = "ASSIMILATION_NONE";
    public static final String ASSIMILATION_NO_GENOMES = "ASSIMILATION_NO_GENOMES";
    public static final String ASSIMILATION_TARGET = "ASSIMILATION_TARGET";
    public static final String ASSIMILATION_ACTIVE = "ASSIMILATION_ACTIVE";
    public static final String ASSIMILATION_PROGRESS = "ASSIMILATION_PROGRESS";
    public static final String ASSIMILATION_INFO = "ASSIMILATION_INFO";
    public static final String SYNERGY_COMING_SOON = "SYNERGY_COMING_SOON";

    public static final String DIALOG_HATCH_RATES_TITLE = "DIALOG_HATCH_RATES_TITLE";
    public static final String HATCH_DESC = "HATCH_DESC";
    public static final String HATCH_TOTAL = "HATCH_TOTAL";
    public static final String HATCH_WARNING_TOTAL = "HATCH_WARNING_TOTAL";
    public static final String HATCH_TOTAL_OK = "HATCH_TOTAL_OK";

    public static final String DIALOG_ABILITIES_TITLE = "DIALOG_ABILITIES_TITLE";
    public static final String ABILITY_RP_LABEL = "ABILITY_RP_LABEL";
    public static final String ABILITY_NO_ABILITIES = "ABILITY_NO_ABILITIES";
    public static final String ABILITY_FORCED_FLIGHT = "ABILITY_FORCED_FLIGHT";
    public static final String ABILITY_FORCED_FLIGHT_DESC = "ABILITY_FORCED_FLIGHT_DESC";
    public static final String ABILITY_MASS_FLIGHT = "ABILITY_MASS_FLIGHT";
    public static final String ABILITY_MASS_FLIGHT_DESC = "ABILITY_MASS_FLIGHT_DESC";
    public static final String ABILITY_ERROR_NOT_ENOUGH_RP = "ABILITY_ERROR_NOT_ENOUGH_RP";
    public static final String ABILITY_ERROR_NO_DRONES = "ABILITY_ERROR_NO_DRONES";
    public static final String ABILITY_ERROR_NO_BREEDERS = "ABILITY_ERROR_NO_BREEDERS";
    public static final String ABILITY_ERROR_LOCATE_COLONY = "ABILITY_ERROR_LOCATE_COLONY";
    public static final String ABILITY_ERROR_ALREADY_ACTIVE = "ABILITY_ERROR_ALREADY_ACTIVE";
    public static final String ABILITY_ERROR_NOT_ENOUGH_RESOURCE = "ABILITY_ERROR_NOT_ENOUGH_RESOURCE";
    public static final String ABILITY_COST_RESOURCE_FMT = "ABILITY_COST_RESOURCE_FMT";
    public static final String ABILITY_PHEROMONE_STORM = "ABILITY_PHEROMONE_STORM";
    public static final String ABILITY_PHEROMONE_STORM_FLAVOR = "ABILITY_PHEROMONE_STORM_FLAVOR";
    public static final String ABILITY_PHEROMONE_STORM_DESC = "ABILITY_PHEROMONE_STORM_DESC";
    public static final String ABILITY_CREATINE_DIET = "ABILITY_CREATINE_DIET";
    public static final String ABILITY_CREATINE_DIET_FLAVOR = "ABILITY_CREATINE_DIET_FLAVOR";
    public static final String ABILITY_CREATINE_DIET_DESC = "ABILITY_CREATINE_DIET_DESC";

    public static final String DIALOG_STATS_TITLE = "DIALOG_STATS_TITLE";
    public static final String STATS_DYNASTY_MODE = "STATS_DYNASTY_MODE";
    public static final String STATS_TAB_GENERAL = "STATS_TAB_GENERAL";
    public static final String STATS_TAB_DYNASTY = "STATS_TAB_DYNASTY";
    public static final String STATS_TAB_ECONOMY = "STATS_TAB_ECONOMY";
    public static final String STATS_TAB_POPULATION = "STATS_TAB_POPULATION";
    public static final String STATS_TAB_LOCAL_HEX = "STATS_TAB_LOCAL_HEX";
    public static final String STATS_TAB_RATES = "STATS_TAB_RATES";
    public static final String STATS_TAB_INSECTS = "STATS_TAB_INSECTS";
    public static final String STATS_TAB_UNIT_STATS = "STATS_TAB_UNIT_STATS";
    public static final String STATS_TAB_MORTALITY = "STATS_TAB_MORTALITY";
    public static final String COL_CATEGORY = "COL_CATEGORY";
    public static final String COL_PROPERTY = "COL_PROPERTY";
    public static final String COL_VALUE = "COL_VALUE";
    public static final String COL_SCOPE = "COL_SCOPE";
    public static final String COL_METRIC = "COL_METRIC";
    public static final String COL_RESOURCE = "COL_RESOURCE";
    public static final String COL_CURRENT = "COL_CURRENT";
    public static final String COL_CAPACITY = "COL_CAPACITY";
    public static final String COL_SOURCES = "COL_SOURCES";
    public static final String COL_PROD_DAY = "COL_PROD_DAY";
    public static final String COL_CONS_DAY = "COL_CONS_DAY";
    public static final String COL_NET = "COL_NET";
    public static final String COL_TYPE = "COL_TYPE";
    public static final String COL_ROLE = "COL_ROLE";
    public static final String COL_COUNT = "COL_COUNT";
    public static final String COL_ACTIVITY = "COL_ACTIVITY";
    public static final String COL_ASSIGNED = "COL_ASSIGNED";
    public static final String COL_RATE_CAP = "COL_RATE_CAP";
    public static final String COL_COV_OUT = "COL_COV_OUT";
    public static final String COL_STAT = "COL_STAT";
    public static final String COL_BASE_VAL = "COL_BASE_VAL";
    public static final String COL_DESCRIPTION = "COL_DESCRIPTION";
    public static final String COL_CAUSE = "COL_CAUSE";
    public static final String COL_TOTAL = "COL_TOTAL";
    public static final String COL_CARETAKERS = "COL_CARETAKERS";
    public static final String STAT_STATUS_NO_HEX = "STAT_STATUS_NO_HEX";
    public static final String STAT_COORDS = "STAT_COORDS";
    public static final String STAT_LOCAL_WEATHER = "STAT_LOCAL_WEATHER";
    public static final String STAT_USING_GLOBAL = "STAT_USING_GLOBAL";
    public static final String STAT_BASE_TEMP = "STAT_BASE_TEMP";
    public static final String STAT_HUMIDITY_LEVEL = "STAT_HUMIDITY_LEVEL";
    public static final String STAT_ABUNDANCES = "STAT_ABUNDANCES";
    public static final String STAT_NEIGHBORS = "STAT_NEIGHBORS";
    public static final String STAT_EDGE_WORLD = "STAT_EDGE_WORLD";
    public static final String STAT_UNKNOWN = "STAT_UNKNOWN";
    public static final String STAT_DYNASTY = "STAT_DYNASTY";
    public static final String STAT_GLOBAL_POP = "STAT_GLOBAL_POP";
    public static final String STAT_GLOBAL_QUEENS = "STAT_GLOBAL_QUEENS";
    public static final String STAT_SPECIES_SCIENTIFIC = "STAT_SPECIES_SCIENTIFIC";
    public static final String STAT_AGE = "STAT_AGE";
    public static final String STAT_DAYS_SUFFIX = "STAT_DAYS_SUFFIX";
    public static final String STAT_QUEEN_STATUS = "STAT_QUEEN_STATUS";
    public static final String STAT_AUTOMATION = "STAT_AUTOMATION";
    public static final String STAT_AUTO_BUILD = "STAT_AUTO_BUILD";
    public static final String STAT_DATE_TIME = "STAT_DATE_TIME";
    public static final String STAT_TIME_DAY = "STAT_TIME_DAY";
    public static final String STAT_MOON_PHASE = "STAT_MOON_PHASE";
    public static final String STAT_ENVIRONMENT = "STAT_ENVIRONMENT";
    public static final String STAT_NO_DYNASTY = "STAT_NO_DYNASTY";
    public static final String STAT_CAPITAL = "STAT_CAPITAL";
    public static final String STAT_TOTAL_COLONIES = "STAT_TOTAL_COLONIES";
    public static final String STAT_NUPTIAL_FLIGHTS = "STAT_NUPTIAL_FLIGHTS";
    public static final String STAT_DEFEATED_SPECIES = "STAT_DEFEATED_SPECIES";
    public static final String STAT_UPGRADES_RES = "STAT_UPGRADES_RES";
    public static final String STAT_ASSIM_COMP = "STAT_ASSIM_COMP";
    public static final String STAT_BUILDINGS_BUILT = "STAT_BUILDINGS_BUILT";
    public static final String STAT_STORED_POINTS = "STAT_STORED_POINTS";
    public static final String STAT_GLOBAL_RATE = "STAT_GLOBAL_RATE";
    public static final String STAT_PTS_DAY_FORMAT = "STAT_PTS_DAY_FORMAT";
    public static final String STAT_MORTALITY = "STAT_MORTALITY";
    public static final String STAT_GLOBAL_DEATHS = "STAT_GLOBAL_DEATHS";
    public static final String STAT_TOTAL_FOOD = "STAT_TOTAL_FOOD";
    public static final String STAT_ADULTS = "STAT_ADULTS";
    public static final String STAT_JUVENILES = "STAT_JUVENILES";
    public static final String STAT_COLONY_TOTAL = "STAT_COLONY_TOTAL";
    public static final String STAT_DYNASTY_TOTAL = "STAT_DYNASTY_TOTAL";
    public static final String STAT_NO_DEATHS = "STAT_NO_DEATHS";
    public static final String STAT_GENETIC_INTEGRITY = "STAT_GENETIC_INTEGRITY";

    public static final String UNIT_STAT_HEALTH = "UNIT_STAT_HEALTH";
    public static final String UNIT_STAT_HEALTH_DESC = "UNIT_STAT_HEALTH_DESC";
    public static final String UNIT_STAT_DEFENSE = "UNIT_STAT_DEFENSE";
    public static final String UNIT_STAT_DEFENSE_DESC = "UNIT_STAT_DEFENSE_DESC";
    public static final String UNIT_STAT_ATTACK = "UNIT_STAT_ATTACK";
    public static final String UNIT_STAT_ATTACK_DESC = "UNIT_STAT_ATTACK_DESC";
    public static final String UNIT_STAT_SPEED = "UNIT_STAT_SPEED";
    public static final String UNIT_STAT_SPEED_DESC = "UNIT_STAT_SPEED_DESC";
    public static final String UNIT_STAT_REGEN = "UNIT_STAT_REGEN";
    public static final String UNIT_STAT_REGEN_DESC = "UNIT_STAT_REGEN_DESC";
    public static final String UNIT_STAT_TEMP_RES = "UNIT_STAT_TEMP_RES";
    public static final String UNIT_STAT_TEMP_RES_DESC = "UNIT_STAT_TEMP_RES_DESC";
    public static final String UNIT_STAT_CONSUMPTION = "UNIT_STAT_CONSUMPTION";
    public static final String UNIT_STAT_CONSUMPTION_DESC = "UNIT_STAT_CONSUMPTION_DESC";
    public static final String UNIT_STAT_DETECTION = "UNIT_STAT_DETECTION";
    public static final String UNIT_STAT_DETECTION_DESC = "UNIT_STAT_DETECTION_DESC";
    public static final String UNIT_STAT_IMMUNITY = "UNIT_STAT_IMMUNITY";
    public static final String UNIT_STAT_IMMUNITY_DESC = "UNIT_STAT_IMMUNITY_DESC";

    public static final String DIALOG_ROLES_TITLE = "DIALOG_ROLES_TITLE";
    public static final String ROLE_TOTAL_PREFIX = "ROLE_TOTAL_PREFIX";
    public static final String ROLE_ASSIGNED_PREFIX = "ROLE_ASSIGNED_PREFIX";
    public static final String ROLE_UNASSIGNED_PREFIX = "ROLE_UNASSIGNED_PREFIX";
    public static final String ROLE_ERROR_OVER_ASSIGNED = "ROLE_ERROR_OVER_ASSIGNED";

    public static final String DIALOG_MAP_TITLE = "DIALOG_MAP_TITLE";
    public static final String MAP_HOME_BUTTON = "MAP_HOME_BUTTON";
    public static final String MAP_LEGEND_TITLE = "MAP_LEGEND_TITLE";
    public static final String MAP_CLICK_VIEW_CAPITAL = "MAP_CLICK_VIEW_CAPITAL";
    public static final String MAP_YOU_PLAYER = "MAP_YOU_PLAYER";
    public static final String MAP_POPULATION_FORMAT = "MAP_POPULATION_FORMAT";
    public static final String MAP_TOOLTIP_BIOME = "MAP_TOOLTIP_BIOME";
    public static final String MAP_TOOLTIP_RANK = "MAP_TOOLTIP_RANK";
    public static final String MAP_TOOLTIP_SPECIES = "MAP_TOOLTIP_SPECIES";
    public static final String MAP_TOOLTIP_DYNASTY = "MAP_TOOLTIP_DYNASTY";
    public static final String MAP_TOOLTIP_DYNASTY_RANK = "MAP_TOOLTIP_DYNASTY_RANK";
    public static final String MAP_TOOLTIP_EMPTY = "MAP_TOOLTIP_EMPTY";
    public static final String MAP_DYNASTY_DIPLO_FORMAT = "MAP_DYNASTY_DIPLO_FORMAT";
    public static final String MAP_SORT_BY_POPULATION = "MAP_SORT_BY_POPULATION";
    public static final String MAP_SORT_BY_DIPLOMACY = "MAP_SORT_BY_DIPLOMACY";
    public static final String MAP_SORT_BY_MILITARY = "MAP_SORT_BY_MILITARY";
    public static final String MAP_LEGEND_SHOW = "MAP_LEGEND_SHOW";
    public static final String MAP_LEGEND_HIDE = "MAP_LEGEND_HIDE";

    public static final String DIALOG_DYNASTY_TITLE = "DIALOG_DYNASTY_TITLE";
    public static final String TAB_OVERVIEW = "TAB_OVERVIEW";
    public static final String TAB_DIPLOMACY = "TAB_DIPLOMACY";
    public static final String TAB_LOGISTICS = "TAB_LOGISTICS";
    public static final String DYNASTY_MANAGE_LOGISTICS = "DYNASTY_MANAGE_LOGISTICS";
    public static final String DYNASTY_DIRECTION = "DYNASTY_DIRECTION";
    public static final String DYNASTY_NEIGHBOR = "DYNASTY_NEIGHBOR";
    public static final String DYNASTY_TUNNEL_STATUS = "DYNASTY_TUNNEL_STATUS";
    public static final String DYNASTY_OUTGOING = "DYNASTY_OUTGOING";
    public static final String DYNASTY_INCOMING = "DYNASTY_INCOMING";
    public static final String DYNASTY_ACTIONS = "DYNASTY_ACTIONS";
    public static final String DYNASTY_TRANSIT_FORMAT = "DYNASTY_TRANSIT_FORMAT";
    public static final String DYNASTY_BUILD_TUNNEL = "DYNASTY_BUILD_TUNNEL";
    public static final String DYNASTY_TUNNEL_SPONSORING = "DYNASTY_TUNNEL_SPONSORING";
    public static final String DYNASTY_BUILT = "DYNASTY_BUILT";
    public static final String DYNASTY_PROGRESS_PERCENT = "DYNASTY_PROGRESS_PERCENT";
    public static final String DYNASTY_ERROR_NO_ENGINEERS = "DYNASTY_ERROR_NO_ENGINEERS";
    public static final String DYNASTY_ERROR_LABOR_REQUIRED = "DYNASTY_ERROR_LABOR_REQUIRED";
    public static final String DYNASTY_ERROR_ASSIGN_BORERS = "DYNASTY_ERROR_ASSIGN_BORERS";
    public static final String DYNASTY_MANAGE_TRADE_TITLE = "DYNASTY_MANAGE_TRADE_TITLE";
    public static final String DYNASTY_MANAGE_TRADE_MSG = "DYNASTY_MANAGE_TRADE_MSG";
    public static final String DYNASTY_MODIFY = "DYNASTY_MODIFY";
    public static final String DYNASTY_CANCEL_ROUTE = "DYNASTY_CANCEL_ROUTE";
    public static final String DYNASTY_ERROR_LOCATE = "DYNASTY_ERROR_LOCATE";
    public static final String DYNASTY_SORT_BY = "DYNASTY_SORT_BY";
    public static final String DYNASTY_DEFAULT_AUTO_BUILD = "DYNASTY_DEFAULT_AUTO_BUILD";
    public static final String DYNASTY_DEFAULT_AUTO_BUILD_TOOLTIP = "DYNASTY_DEFAULT_AUTO_BUILD_TOOLTIP";
    public static final String DYNASTY_DEFAULT_AUTOMATION = "DYNASTY_DEFAULT_AUTOMATION";
    public static final String DYNASTY_DEFAULT_AUTOMATION_TOOLTIP = "DYNASTY_DEFAULT_AUTOMATION_TOOLTIP";
    public static final String DYNASTY_SATELLITE = "DYNASTY_SATELLITE";
    public static final String STAT_LOYALTY = "STAT_LOYALTY";
    public static final String STAT_MILITARY_POWER = "STAT_MILITARY_POWER";
    public static final String STAT_MILITARY_POWER_DESC = "STAT_MILITARY_POWER_DESC";
    public static final String DYNASTY_SORT_MILITARY_HIGH = "DYNASTY_SORT_MILITARY_HIGH";
    public static final String DYNASTY_SORT_MILITARY_LOW = "DYNASTY_SORT_MILITARY_LOW";
    public static final String MAP_LEGEND_MILITARY = "MAP_LEGEND_MILITARY";
    public static final String MAP_TOOLTIP_MILITARY_POWER = "MAP_TOOLTIP_MILITARY_POWER";
    public static final String MAP_TOOLTIP_DYNASTY_MILITARY_POWER = "MAP_TOOLTIP_DYNASTY_MILITARY_POWER";
    public static final String HELP_EMPIRE_MILITARY_POWER = "HELP_EMPIRE_MILITARY_POWER";
    public static final String HELP_MILITARY_POWER_BODY = "HELP_MILITARY_POWER_BODY";
    public static final String DYNASTY_REPUTATION = "DYNASTY_REPUTATION";
    public static final String DYNASTY_REPUTATION_STANCE = "DYNASTY_REPUTATION_STANCE";
    public static final String SCORE_TIER_FORMAT = "SCORE_TIER_FORMAT";
    public static final String DYNASTY_STATUS = "DYNASTY_STATUS";
    public static final String DYNASTY_7_DAYS = "DYNASTY_7_DAYS";
    public static final String DYNASTY_RENAME_TITLE = "DYNASTY_RENAME_TITLE";

    public static final String TRADE_ESTABLISH_TITLE = "TRADE_ESTABLISH_TITLE";
    public static final String TRADE_MODIFY_TITLE = "TRADE_MODIFY_TITLE";
    public static final String TRADE_CAPACITY_FORMAT = "TRADE_CAPACITY_FORMAT";
    public static final String TRADE_SPEED_FORMAT = "TRADE_SPEED_FORMAT";
    public static final String TRADE_TIME_FORMAT = "TRADE_TIME_FORMAT";
    public static final String TRADE_SECURITY_FORMAT = "TRADE_SECURITY_FORMAT";
    public static final String TRADE_ROUTE_PREFIX = "TRADE_ROUTE_PREFIX";
    public static final String TRADE_CARGO_LOAD = "TRADE_CARGO_LOAD";
    public static final String TRADE_CARGO_RETURN = "TRADE_CARGO_RETURN";
    public static final String TRADE_PERSONNEL = "TRADE_PERSONNEL";
    public static final String TRADE_COURIERS = "TRADE_COURIERS";
    public static final String TRADE_TRANSPORTS = "TRADE_TRANSPORTS";
    public static final String TRADE_ESCORTS = "TRADE_ESCORTS";
    public static final String TRADE_FLYERS = "TRADE_FLYERS";
    public static final String TRADE_METHOD = "TRADE_METHOD";
    public static final String TRADE_RECURRENT = "TRADE_RECURRENT";
    public static final String TRADE_BILATERAL = "TRADE_BILATERAL";
    public static final String TRADE_CONFIRM = "TRADE_CONFIRM";
    public static final String TRADE_UPDATE = "TRADE_UPDATE";
    public static final String TRADE_OPTIMIZE = "TRADE_OPTIMIZE";
    public static final String TRADE_MAKE_TWO_WAY = "TRADE_MAKE_TWO_WAY";
    public static final String TRADE_ERROR_OVER_CAP = "TRADE_ERROR_OVER_CAP";
    public static final String TRADE_ERROR_NO_ANTS = "TRADE_ERROR_NO_ANTS";
    public static final String TRADE_ERROR_NO_LOAD = "TRADE_ERROR_NO_LOAD";
    public static final String TRADE_OPTIMIZE_MSG = "TRADE_OPTIMIZE_MSG";
    public static final String TRADE_ERROR_EMPTY = "TRADE_ERROR_EMPTY";
    public static final String TRADE_ERROR_NO_PERSONNEL = "TRADE_ERROR_NO_PERSONNEL";
    public static final String TRADE_ERROR_NO_METHOD = "TRADE_ERROR_NO_METHOD";
    public static final String TRADE_QUEUED_MSG = "TRADE_QUEUED_MSG";
    public static final String TRADE_ERROR_START = "TRADE_ERROR_START";

    public static final String ALERT_STARVATION_RISK = "ALERT_STARVATION_RISK";
    public static final String ALERT_NEW_RESEARCH = "ALERT_NEW_RESEARCH";
    public static final String ALERT_CAN_BUILD_FMT = "ALERT_CAN_BUILD_FMT";
    public static final String ALERT_BODY_PILE_FMT = "ALERT_BODY_PILE_FMT";
    public static final String ALERT_NUPTIAL_FLIGHT = "ALERT_NUPTIAL_FLIGHT";
    public static final String ALERT_BUILT_PREFIX = "ALERT_BUILT_PREFIX";
    public static final String ALERT_COMPOST_RECYCLED_PREFIX = "ALERT_COMPOST_RECYCLED_PREFIX";
    public static final String ALERT_COMPOST_BODIES_SUFFIX = "ALERT_COMPOST_BODIES_SUFFIX";

    public static final String ASSIMILATION_DIALOG_SUCCESS_TITLE = "ASSIMILATION_DIALOG_SUCCESS_TITLE";
    public static final String ASSIMILATION_DIALOG_SUCCESS_BODY = "ASSIMILATION_DIALOG_SUCCESS_BODY";

    public static final String UI_DIALOG_LOADING_TITLE = "UI_DIALOG_LOADING_TITLE";
    public static final String SAVE_DEFAULT_NAME_FMT = "SAVE_DEFAULT_NAME_FMT";
    public static final String SAVE_AUTOSAVE_NAME = "SAVE_AUTOSAVE_NAME";
    public static final String SAVE_ERROR_WRITE = "SAVE_ERROR_WRITE";
    public static final String SAVE_ERROR_WRITE_TITLE = "SAVE_ERROR_WRITE_TITLE";
    public static final String DYNASTY_PLAYER_NAME_FMT = "DYNASTY_PLAYER_NAME_FMT";
    public static final String DYNASTY_WILD_NAME = "DYNASTY_WILD_NAME";

    public static final String STAT_NEIGHBOR_NORTH = "STAT_NEIGHBOR_NORTH";
    public static final String STAT_NEIGHBOR_NORTH_WEST = "STAT_NEIGHBOR_NORTH_WEST";
    public static final String STAT_NEIGHBOR_NORTH_EAST = "STAT_NEIGHBOR_NORTH_EAST";
    public static final String STAT_NEIGHBOR_SOUTH = "STAT_NEIGHBOR_SOUTH";
    public static final String STAT_NEIGHBOR_SOUTH_WEST = "STAT_NEIGHBOR_SOUTH_WEST";
    public static final String STAT_NEIGHBOR_SOUTH_EAST = "STAT_NEIGHBOR_SOUTH_EAST";
    public static final String STAT_TABLE_SEPARATOR = "STAT_TABLE_SEPARATOR";
    public static final String STAT_HEX_COLONY_FMT = "STAT_HEX_COLONY_FMT";
    public static final String STAT_LABEL_ID = "STAT_LABEL_ID";
    public static final String STAT_LABEL_ERROR = "STAT_LABEL_ERROR";
    public static final String STAT_LABEL_PROGRESS = "STAT_LABEL_PROGRESS";
    public static final String STAT_GLOBAL_RESOURCE_FMT = "STAT_GLOBAL_RESOURCE_FMT";
    public static final String STAT_DEFEATED_UNKNOWN_FMT = "STAT_DEFEATED_UNKNOWN_FMT";
    public static final String STAT_JOB_FARMING = "STAT_JOB_FARMING";
    public static final String STAT_JOB_FARMERS_EFF_FMT = "STAT_JOB_FARMERS_EFF_FMT";
    public static final String STAT_RATE_CONVERT_DAY = "STAT_RATE_CONVERT_DAY";
    public static final String STAT_RATE_PWR_DAY = "STAT_RATE_PWR_DAY";
    public static final String STAT_RATE_RES_DAY = "STAT_RATE_RES_DAY";
    public static final String STAT_RATE_SCOUT_STATUS = "STAT_RATE_SCOUT_STATUS";
    public static final String STAT_RATE_RESEARCH_ASST_FMT = "STAT_RATE_RESEARCH_ASST_FMT";
    public static final String STAT_RATE_PTS_DAY_FMT = "STAT_RATE_PTS_DAY_FMT";
    public static final String STAT_JOB_EGG_LAYING = "STAT_JOB_EGG_LAYING";
    public static final String STAT_RATE_LAYERS_FMT = "STAT_RATE_LAYERS_FMT";
    public static final String STAT_RATE_EGGS_DAY = "STAT_RATE_EGGS_DAY";
    public static final String STAT_RATE_NURSES_FMT = "STAT_RATE_NURSES_FMT";
    public static final String STAT_RATE_CAP_SHORT = "STAT_RATE_CAP_SHORT";
    public static final String STAT_RATE_LOAD_FMT = "STAT_RATE_LOAD_FMT";
    public static final String STAT_RATE_GRAVERS_FMT = "STAT_RATE_GRAVERS_FMT";
    public static final String STAT_RATE_RANCHERS_FMT = "STAT_RATE_RANCHERS_FMT";
    public static final String STAT_RATE_APHIDS_FMT = "STAT_RATE_APHIDS_FMT";
    public static final String STAT_RATE_POLICE_FMT = "STAT_RATE_POLICE_FMT";
    public static final String STAT_RATE_PARASITE_ANTS_FMT = "STAT_RATE_PARASITE_ANTS_FMT";
    public static final String STAT_RATE_DET_DAY = "STAT_RATE_DET_DAY";
    public static final String STAT_NO_INSECTS = "STAT_NO_INSECTS";
    public static final String STAT_INSECT_CATCHERS_FMT = "STAT_INSECT_CATCHERS_FMT";
    public static final String STAT_INSECT_SLOWED_FMT = "STAT_INSECT_SLOWED_FMT";
    public static final String STAT_INSECT_POOL = "STAT_INSECT_POOL";
    public static final String STAT_INSECT_PARASITIC_CAP_FMT = "STAT_INSECT_PARASITIC_CAP_FMT";
    public static final String STAT_INSECT_PARASITIC_KILL_FMT = "STAT_INSECT_PARASITIC_KILL_FMT";
    public static final String STAT_OUTBREAK_PREV_FMT = "STAT_OUTBREAK_PREV_FMT";
    public static final String STAT_OUTBREAK_PREV_BLOCKED = "STAT_OUTBREAK_PREV_BLOCKED";
    public static final String STAT_OUTBREAK_PREV_PROJECTED_FMT = "STAT_OUTBREAK_PREV_PROJECTED_FMT";
    public static final String TOOLTIP_OUTBREAK_PREV_POLICE = "TOOLTIP_OUTBREAK_PREV_POLICE";
    public static final String TOOLTIP_OUTBREAK_PREV_SYMBIOTIC = "TOOLTIP_OUTBREAK_PREV_SYMBIOTIC";

    public static final String LOG_DEATH_OLD_AGE_FMT = "LOG_DEATH_OLD_AGE_FMT";
    public static final String LOG_DEATH_JUVENILES_LACK_CARE_FMT = "LOG_DEATH_JUVENILES_LACK_CARE_FMT";
    public static final String LOG_DEATH_COUNT_CAUSE_FMT = "LOG_DEATH_COUNT_CAUSE_FMT";
    public static final String LOG_CAUSE_OLD_AGE = "LOG_CAUSE_OLD_AGE";
    public static final String LOG_CAUSE_LACK_OF_CARE = "LOG_CAUSE_LACK_OF_CARE";
    public static final String LOG_CAUSE_DEHYDRATION = "LOG_CAUSE_DEHYDRATION";
    public static final String LOG_CAUSE_STARVATION = "LOG_CAUSE_STARVATION";
    public static final String LOG_CAUSE_CONTAMINATION = "LOG_CAUSE_CONTAMINATION";
    public static final String LOG_CAUSE_CONFLICT = "LOG_CAUSE_CONFLICT";
    public static final String LOG_CAUSE_ILLNESS = "LOG_CAUSE_ILLNESS";
    public static final String LOG_CAUSE_OTHER = "LOG_CAUSE_OTHER";
    public static final String LOG_CONTAMINATION_LEVEL_FMT = "LOG_CONTAMINATION_LEVEL_FMT";
    public static final String LOG_CONTAM_LEVEL_SMALL = "LOG_CONTAM_LEVEL_SMALL";
    public static final String LOG_CONTAM_LEVEL_MEDIUM = "LOG_CONTAM_LEVEL_MEDIUM";
    public static final String LOG_CONTAM_LEVEL_MASSIVE = "LOG_CONTAM_LEVEL_MASSIVE";
    public static final String LOG_PARASITE_ANT_SPREAD_FMT = "LOG_PARASITE_ANT_SPREAD_FMT";
    public static final String LOG_PARASITIC_MITE_SPREAD_FMT = "LOG_PARASITIC_MITE_SPREAD_FMT";
    public static final String LOG_DYNASTY_ABSORBED_FMT = "LOG_DYNASTY_ABSORBED_FMT";
    public static final String LOG_FAILURE_SATELLITE = "LOG_FAILURE_SATELLITE";
    public static final String LOG_SATELLITE_AT_FMT = "LOG_SATELLITE_AT_FMT";
    public static final String LOG_SATELLITES_ESTABLISHED_FMT = "LOG_SATELLITES_ESTABLISHED_FMT";
    public static final String LOG_SPREADING_FAILED = "LOG_SPREADING_FAILED";
    public static final String LOG_PARASITE_ANTS_ELIMINATED_FMT = "LOG_PARASITE_ANTS_ELIMINATED_FMT";
    public static final String LOG_CAUGHT_BUGS_SUMMARY_FMT = "LOG_CAUGHT_BUGS_SUMMARY_FMT";
    public static final String LOG_CAUGHT_BUG_BRED_FMT = "LOG_CAUGHT_BUG_BRED_FMT";
    public static final String LOG_SYMBIOTIC_MITES_PREDATION_FMT = "LOG_SYMBIOTIC_MITES_PREDATION_FMT";
    public static final String LOG_NUPTIAL_QUEENS_FMT = "LOG_NUPTIAL_QUEENS_FMT";
    public static final String LOG_SUCCESS_ASSIMILATION_FMT = "LOG_SUCCESS_ASSIMILATION_FMT";
    public static final String LOG_SUCCESS_TUNNEL = "LOG_SUCCESS_TUNNEL";
    public static final String LOG_FOUND_NEW_SOURCE_FMT = "LOG_FOUND_NEW_SOURCE_FMT";
    public static final String LOG_FOUND_SOURCE_FULL_FMT = "LOG_FOUND_SOURCE_FULL_FMT";
    public static final String LOG_SOURCE_EXHAUSTED_FMT = "LOG_SOURCE_EXHAUSTED_FMT";
    public static final String LOG_FORCE_FLIGHT_BLOCKED = "LOG_FORCE_FLIGHT_BLOCKED";
    public static final String LOG_PHEROMONE_STORM_STARTED_FMT = "LOG_PHEROMONE_STORM_STARTED_FMT";
    public static final String LOG_PHEROMONE_STORM_ENDED = "LOG_PHEROMONE_STORM_ENDED";
    public static final String LOG_CREATINE_DIET_STARTED_FMT = "LOG_CREATINE_DIET_STARTED_FMT";
    public static final String LOG_CREATINE_DIET_ENDED = "LOG_CREATINE_DIET_ENDED";
    public static final String LOG_WARNING_NO_QUEEN_FMT = "LOG_WARNING_NO_QUEEN_FMT";
    public static final String LOG_MATURATION_COMPLETE = "LOG_MATURATION_COMPLETE";
    public static final String LOG_TRADE_CANCELLED_FMT = "LOG_TRADE_CANCELLED_FMT";
    public static final String LOG_TRADE_ARRIVED_FMT = "LOG_TRADE_ARRIVED_FMT";
    public static final String LOG_TRADE_CONVOY_RETURN_FMT = "LOG_TRADE_CONVOY_RETURN_FMT";
    public static final String LOG_TRADE_BILATERAL_RETURN_FMT = "LOG_TRADE_BILATERAL_RETURN_FMT";
    public static final String LOG_TRADE_ROUTE_CANCELLED_FMT = "LOG_TRADE_ROUTE_CANCELLED_FMT";
    public static final String LOG_AUTOMATION_BUILD_FMT = "LOG_AUTOMATION_BUILD_FMT";
    public static final String LOG_AUTOMATION_TUNNEL_FMT = "LOG_AUTOMATION_TUNNEL_FMT";
    public static final String LOG_AUTOMATION_TRADE_FMT = "LOG_AUTOMATION_TRADE_FMT";
    public static final String LOG_LAST_COLONY_FALLEN = "LOG_LAST_COLONY_FALLEN";
    public static final String LOG_PROMOTION_CAPITAL_FMT = "LOG_PROMOTION_CAPITAL_FMT";
    public static final String LOG_DYNASTY_RESEARCHED_FMT = "LOG_DYNASTY_RESEARCHED_FMT";
    public static final String LOG_COMPOST_RECYCLED_FMT = "LOG_COMPOST_RECYCLED_FMT";

    public static final String DYNASTY_SORT_POP_HIGH = "DYNASTY_SORT_POP_HIGH";
    public static final String DYNASTY_SORT_POP_LOW = "DYNASTY_SORT_POP_LOW";
    public static final String DYNASTY_SORT_AGE_OLD = "DYNASTY_SORT_AGE_OLD";
    public static final String DYNASTY_SORT_AGE_NEW = "DYNASTY_SORT_AGE_NEW";
    public static final String DYNASTY_SORT_LOYALTY_HIGH = "DYNASTY_SORT_LOYALTY_HIGH";
    public static final String DYNASTY_SORT_LOYALTY_LOW = "DYNASTY_SORT_LOYALTY_LOW";

    public static final String DEATH_OPTIONS_RELOAD = "DEATH_OPTIONS_RELOAD";
    public static final String DEATH_OPTIONS_MENU = "DEATH_OPTIONS_MENU";
    public static final String DEATH_TITLE = "DEATH_TITLE";
    public static final String DEATH_MESSAGE = "DEATH_MESSAGE";
    public static final String DEATH_LOAD_FAILED_NEW_GAME = "DEATH_LOAD_FAILED_NEW_GAME";
    public static final String DEATH_LOAD_FAILED_TITLE = "DEATH_LOAD_FAILED_TITLE";
    public static final String DEATH_LOAD_FAILED_NO_AUTOSAVE = "DEATH_LOAD_FAILED_NO_AUTOSAVE";

    public static final String SAVE_EMPTY_SLOT = "SAVE_EMPTY_SLOT";
    public static final String SAVE_DAYS_FORMAT = "SAVE_DAYS_FORMAT";
    public static final String SAVE_ENTER_NAME = "SAVE_ENTER_NAME";
    public static final String SAVE_CREATE_TITLE = "SAVE_CREATE_TITLE";
    public static final String SAVE_ERROR_CREATE = "SAVE_ERROR_CREATE";
    public static final String SAVE_DELETE_CONFIRM = "SAVE_DELETE_CONFIRM";
    public static final String SAVE_DELETE_TITLE = "SAVE_DELETE_TITLE";
    public static final String SAVE_DELETE_ERROR = "SAVE_DELETE_ERROR";

    public static final String SETTINGS_LANGUAGE = "SETTINGS_LANGUAGE";
    public static final String SETTINGS_SCREEN_SIZE = "SETTINGS_SCREEN_SIZE";
    public static final String SETTINGS_FULLSCREEN = "SETTINGS_FULLSCREEN";
    public static final String SETTINGS_AUTOSAVE = "SETTINGS_AUTOSAVE";
    public static final String SETTINGS_TURBO = "SETTINGS_TURBO";
    public static final String SETTINGS_EVERY_MONTH = "SETTINGS_EVERY_MONTH";
    public static final String SETTINGS_EVERY_3_MONTHS = "SETTINGS_EVERY_3_MONTHS";
    public static final String SETTINGS_EVERY_6_MONTHS = "SETTINGS_EVERY_6_MONTHS";
    public static final String SETTINGS_EVERY_YEAR = "SETTINGS_EVERY_YEAR";
    public static final String SETTINGS_SAVE_APPLY = "SETTINGS_SAVE_APPLY";
    public static final String SETTINGS_RESET_TAB = "SETTINGS_RESET_TAB";
    public static final String SETTINGS_SAVED_MSG = "SETTINGS_SAVED_MSG";
    
    public static final String SETTINGS_TAB_GENERAL = "SETTINGS_TAB_GENERAL";
    public static final String SETTINGS_TAB_VIDEO = "SETTINGS_TAB_VIDEO";
    public static final String SETTINGS_TAB_AUDIO = "SETTINGS_TAB_AUDIO";
    public static final String SETTINGS_TAB_ROLES = "SETTINGS_TAB_ROLES";
    
    public static final String SETTINGS_DAYLIGHT_COLOR_OVERLAY = "SETTINGS_DAYLIGHT_COLOR_OVERLAY";
    public static final String SETTINGS_WEATHER_COLOR_OVERLAY = "SETTINGS_WEATHER_COLOR_OVERLAY";
    public static final String SETTINGS_ARACHNOPHOBIA = "SETTINGS_ARACHNOPHOBIA";
    
    public static final String SETTINGS_PAUSE_FOCUS = "SETTINGS_PAUSE_FOCUS";
    public static final String SETTINGS_CONFIRM_QUIT = "SETTINGS_CONFIRM_QUIT";
    public static final String SETTINGS_ESCAPE_KEY_GAME_ACTIONS = "SETTINGS_ESCAPE_KEY_GAME_ACTIONS";
    public static final String SETTINGS_SHOW_TOOLTIPS = "SETTINGS_SHOW_TOOLTIPS";
    
    public static final String SETTINGS_FUZZ_PARASITE_ANTS = "SETTINGS_FUZZ_PARASITE_ANTS";
    public static final String SETTINGS_OVERWORLD_AUTO_RECENTER = "SETTINGS_OVERWORLD_AUTO_RECENTER";
    public static final String SETTINGS_DARK_MODE = "SETTINGS_DARK_MODE";
    public static final String SETTINGS_DEFAULT_ROLE_WORKER = "SETTINGS_DEFAULT_ROLE_WORKER";
    public static final String SETTINGS_DEFAULT_ROLE_SOLDIER = "SETTINGS_DEFAULT_ROLE_SOLDIER";
    public static final String SETTINGS_DEFAULT_ROLE_MAJOR = "SETTINGS_DEFAULT_ROLE_MAJOR";
    public static final String SETTINGS_DEFAULT_ROLE_PRINCESS = "SETTINGS_DEFAULT_ROLE_PRINCESS";
    public static final String SETTINGS_DEFAULT_ROLE_QUEEN = "SETTINGS_DEFAULT_ROLE_QUEEN";
    
    public static final String SETTINGS_MASTER_VOL = "SETTINGS_MASTER_VOL";
    public static final String SETTINGS_MUSIC_VOL = "SETTINGS_MUSIC_VOL";
    public static final String SETTINGS_SFX_VOL = "SETTINGS_SFX_VOL";
    
    public static final String WORLD_BIOME_PREFIX = "WORLD_BIOME_PREFIX";
    public static final String WORLD_TEMP_PREFIX = "WORLD_TEMP_PREFIX";
    public static final String WORLD_HUMIDITY_PREFIX = "WORLD_HUMIDITY_PREFIX";
    public static final String WORLD_NA = "WORLD_NA";

    public static final String BIOME_PLAINS = "BIOME_PLAINS";
    public static final String BIOME_FOREST = "BIOME_FOREST";
    public static final String BIOME_JUNGLE = "BIOME_JUNGLE";
    public static final String BIOME_SWAMP = "BIOME_SWAMP";
    public static final String BIOME_URBAN = "BIOME_URBAN";
    public static final String BIOME_TUNDRA = "BIOME_TUNDRA";
    public static final String BIOME_TAIGA = "BIOME_TAIGA";
    public static final String BIOME_DESERT = "BIOME_DESERT";
    public static final String BIOME_MOUNTAIN = "BIOME_MOUNTAIN";
    public static final String BIOME_VOLCANIC = "BIOME_VOLCANIC";
    public static final String BIOME_LAKE = "BIOME_LAKE";
    public static final String BIOME_OCEAN = "BIOME_OCEAN";

    public static final String TEMP_FREEZING = "TEMP_FREEZING";
    public static final String TEMP_COLD = "TEMP_COLD";
    public static final String TEMP_CHILLY = "TEMP_CHILLY";
    public static final String TEMP_GOOD = "TEMP_GOOD";
    public static final String TEMP_WARM = "TEMP_WARM";
    public static final String TEMP_HOT = "TEMP_HOT";
    public static final String TEMP_BURNING = "TEMP_BURNING";

    public static final String HUMID_ARID = "HUMID_ARID";
    public static final String HUMID_DRY = "HUMID_DRY";
    public static final String HUMID_NORMAL = "HUMID_NORMAL";
    public static final String HUMID_HUMID = "HUMID_HUMID";
    public static final String HUMID_MOIST = "HUMID_MOIST";
    public static final String HUMID_SATURATED = "HUMID_SATURATED";

    public static final String RESOURCE_PLANT = "RESOURCE_PLANT";
    public static final String RESOURCE_FUNGI = "RESOURCE_FUNGI";
    public static final String RESOURCE_MEAT = "RESOURCE_MEAT";
    public static final String RESOURCE_WATER = "RESOURCE_WATER";
    public static final String RESOURCE_SYRUP = "RESOURCE_SYRUP";
    public static final String RESOURCE_RESIN = "RESOURCE_RESIN";
    public static final String RESOURCE_ROCK = "RESOURCE_ROCK";

    public static final String TIME_DAY = "TIME_DAY";
    public static final String TIME_DUSK = "TIME_DUSK";
    public static final String TIME_NIGHT = "TIME_NIGHT";
    public static final String TIME_DAWN = "TIME_DAWN";
    public static final String TIME_SOLAR_ECLIPSE = "TIME_SOLAR_ECLIPSE";
    public static final String TIME_LUNAR_ECLIPSE = "TIME_LUNAR_ECLIPSE";

    public static final String MOON_NEW = "MOON_NEW";
    public static final String MOON_WAXING_CRESCENT = "MOON_WAXING_CRESCENT";
    public static final String MOON_FIRST_QUARTER = "MOON_FIRST_QUARTER";
    public static final String MOON_WAXING_GIBBOUS = "MOON_WAXING_GIBBOUS";
    public static final String MOON_FULL = "MOON_FULL";
    public static final String MOON_WANING_GIBBOUS = "MOON_WANING_GIBBOUS";
    public static final String MOON_LAST_QUARTER = "MOON_LAST_QUARTER";
    public static final String MOON_WANING_CRESCENT = "MOON_WANING_CRESCENT";

    public static final String SEASON_SPRING = "SEASON_SPRING";
    public static final String SEASON_SUMMER = "SEASON_SUMMER";
    public static final String SEASON_AUTUMN = "SEASON_AUTUMN";
    public static final String SEASON_WINTER = "SEASON_WINTER";

    public static final String WEATHER_CLEAR = "WEATHER_CLEAR";
    public static final String WEATHER_RAIN = "WEATHER_RAIN";
    public static final String WEATHER_SNOW = "WEATHER_SNOW";
    public static final String WEATHER_HEAVY_RAIN = "WEATHER_HEAVY_RAIN";
    public static final String WEATHER_THUNDER = "WEATHER_THUNDER";
    public static final String WEATHER_HEAVY_SNOW = "WEATHER_HEAVY_SNOW";
    public static final String WEATHER_WIND = "WEATHER_WIND";
    public static final String WEATHER_HEAT = "WEATHER_HEAT";
    public static final String WEATHER_FOG = "WEATHER_FOG";
    public static final String WEATHER_FROG = "WEATHER_FROG";
    public static final String WEATHER_BLOOD = "WEATHER_BLOOD";
    public static final String WEATHER_SAND_STORM = "WEATHER_SAND_STORM";
    public static final String WEATHER_PYROCLASTIC_FOG = "WEATHER_PYROCLASTIC_FOG";
    public static final String WEATHER_ACID_RAIN = "WEATHER_ACID_RAIN";

    public static final String STATUS_ALIVE = "STATUS_ALIVE";
    public static final String STATUS_DEAD = "STATUS_DEAD";
    public static final String STATUS_ZOMBIFIED = "STATUS_ZOMBIFIED";

    public static final String MOVE_STATIC = "MOVE_STATIC";
    public static final String MOVE_WANDER = "MOVE_WANDER";
    public static final String MOVE_MARCH = "MOVE_MARCH";
    public static final String MOVE_SPEED = "MOVE_SPEED";
    public static final String MOVE_FLY = "MOVE_FLY";
    public static final String MOVE_PATROL = "MOVE_PATROL";

    public static final String BUG_ANT = "BUG_ANT";
    public static final String BUG_APHID = "BUG_APHID";
    public static final String BUG_PARASITE_ANT = "BUG_PARASITE_ANT";
    public static final String BUG_SYMBIOTIC_MITE = "BUG_SYMBIOTIC_MITE";
    public static final String BUG_DERMESTID = "BUG_DERMESTID";
    public static final String BUG_ANT_SCIENTIFIC = "BUG_ANT_SCIENTIFIC";
    public static final String BUG_APHID_SCIENTIFIC = "BUG_APHID_SCIENTIFIC";
    public static final String BUG_PARASITE_ANT_SCIENTIFIC = "BUG_PARASITE_ANT_SCIENTIFIC";
    public static final String BUG_SYMBIOTIC_MITE_SCIENTIFIC = "BUG_SYMBIOTIC_MITE_SCIENTIFIC";
    public static final String BUG_DERMESTID_SCIENTIFIC = "BUG_DERMESTID_SCIENTIFIC";
    public static final String BUG_PARASITIC_MITE = "BUG_PARASITIC_MITE";
    public static final String BUG_PARASITIC_MITE_SCIENTIFIC = "BUG_PARASITIC_MITE_SCIENTIFIC";

    public static final String TYPE_EGG = "TYPE_EGG";
    public static final String TYPE_LARVA = "TYPE_LARVA";
    public static final String TYPE_PUPA = "TYPE_PUPA";
    public static final String TYPE_WORKER = "TYPE_WORKER";
    public static final String TYPE_SOLDIER = "TYPE_SOLDIER";
    public static final String TYPE_MAJOR = "TYPE_MAJOR";
    public static final String TYPE_DRONE = "TYPE_DRONE";
    public static final String TYPE_PRINCESS = "TYPE_PRINCESS";
    public static final String TYPE_QUEEN = "TYPE_QUEEN";
    public static final String TYPE_DEAD = "TYPE_DEAD";
    public static final String TYPE_ZOMBIE = "TYPE_ZOMBIE";

    public static final String ROLE_FORAGER = "ROLE_FORAGER";
    public static final String ROLE_NURSE = "ROLE_NURSE";
    public static final String ROLE_BUILDER = "ROLE_BUILDER";
    public static final String ROLE_SCOUT = "ROLE_SCOUT";
    public static final String ROLE_FARMER = "ROLE_FARMER";
    public static final String ROLE_RANCHER = "ROLE_RANCHER";
    public static final String ROLE_GRAVER = "ROLE_GRAVER";
    public static final String ROLE_MINER = "ROLE_MINER";
    public static final String ROLE_COURIER = "ROLE_COURIER";
    public static final String ROLE_POTTER = "ROLE_POTTER";
    public static final String ROLE_GUARD = "ROLE_GUARD";
    public static final String ROLE_WARRIOR = "ROLE_WARRIOR";
    public static final String ROLE_DEFENDER = "ROLE_DEFENDER";
    public static final String ROLE_POLICE = "ROLE_POLICE";
    public static final String ROLE_BOMBER = "ROLE_BOMBER";
    public static final String ROLE_HUNTER = "ROLE_HUNTER";
    public static final String ROLE_BRUTE = "ROLE_BRUTE";
    public static final String ROLE_CARRIER = "ROLE_CARRIER";
    public static final String ROLE_ARTILLERY = "ROLE_ARTILLERY";
    public static final String ROLE_SIEGE = "ROLE_SIEGE";
    public static final String ROLE_BORER = "ROLE_BORER";
    public static final String ROLE_DRONE = "ROLE_DRONE";
    public static final String ROLE_BREEDER = "ROLE_BREEDER";
    public static final String ROLE_DIPLOMAT = "ROLE_DIPLOMAT";
    public static final String ROLE_LAYER = "ROLE_LAYER";
    public static final String ROLE_RESEARCHER = "ROLE_RESEARCHER";
    public static final String ROLE_MILITIA = "ROLE_MILITIA";
    public static final String ROLE_CATCHER = "ROLE_CATCHER";
    public static final String ROLE_CRANE = "ROLE_CRANE";
    public static final String ROLE_TRANSPORT = "ROLE_TRANSPORT";
    public static final String ROLE_ASSISTANT = "ROLE_ASSISTANT";
    public static final String ROLE_ESCORT = "ROLE_ESCORT";
    public static final String ROLE_ENGINEER = "ROLE_ENGINEER";
    public static final String ROLE_SKYTRANS = "ROLE_SKYTRANS";

    public static final String RANK_ANT = "RANK_ANT";
    public static final String RANK_COLONY = "RANK_COLONY";
    public static final String RANK_COUNTY = "RANK_COUNTY";
    public static final String RANK_DUCHY = "RANK_DUCHY";
    public static final String RANK_KINGDOM = "RANK_KINGDOM";
    public static final String RANK_EMPIRE = "RANK_EMPIRE";
    public static final String RANK_SUPER = "RANK_SUPER";
    public static final String RANK_ULTRA = "RANK_ULTRA";
    public static final String RANK_HYPER = "RANK_HYPER";
    public static final String RANK_MEGA = "RANK_MEGA";
    public static final String RANK_ULTIMATE = "RANK_ULTIMATE";
    public static final String RANK_SUPREME = "RANK_SUPREME";
    public static final String RANK_GIGA = "RANK_GIGA";

    public static final String REPUTATION_AGGRESSIVE = "REPUTATION_AGGRESSIVE";
    public static final String REPUTATION_WARY = "REPUTATION_WARY";
    public static final String REPUTATION_NEUTRAL = "REPUTATION_NEUTRAL";
    public static final String REPUTATION_CORDIAL = "REPUTATION_CORDIAL";
    public static final String REPUTATION_FRIENDLY = "REPUTATION_FRIENDLY";

    public static final String LOYALTY_REBELLIOUS = "LOYALTY_REBELLIOUS";
    public static final String LOYALTY_DISLOYAL = "LOYALTY_DISLOYAL";
    public static final String LOYALTY_COMPLACENT = "LOYALTY_COMPLACENT";
    public static final String LOYALTY_LOYAL = "LOYALTY_LOYAL";
    public static final String LOYALTY_MILITANT = "LOYALTY_MILITANT";

    public static final String DIPLO_MODIFIER_PACT = "DIPLO_MODIFIER_PACT";
    public static final String DIPLO_MODIFIER_BROKEN_PACT = "DIPLO_MODIFIER_BROKEN_PACT";
    public static final String DIPLO_MODIFIER_WAR = "DIPLO_MODIFIER_WAR";
    public static final String DIPLO_MODIFIER_TRADE = "DIPLO_MODIFIER_TRADE";
    public static final String DIPLO_MODIFIER_TRADE_REQUEST = "DIPLO_MODIFIER_TRADE_REQUEST";
    public static final String DIPLO_MODIFIER_BORDER_FRICTION = "DIPLO_MODIFIER_BORDER_FRICTION";
    public static final String GI_MODIFIER_PACT = "GI_MODIFIER_PACT";
    public static final String LOYALTY_MODIFIER_TRADE = "LOYALTY_MODIFIER_TRADE";
    public static final String LOYALTY_MODIFIER_TUNNEL = "LOYALTY_MODIFIER_TUNNEL";
    public static final String LOYALTY_MODIFIER_CAPITAL = "LOYALTY_MODIFIER_CAPITAL";
    public static final String LOYALTY_MODIFIER_PHEROMONE_STORM = "LOYALTY_MODIFIER_PHEROMONE_STORM";
    public static final String DIPLO_ACTION_FORM_PACT = "DIPLO_ACTION_FORM_PACT";
    public static final String DIPLO_ACTION_BREAK_PACT = "DIPLO_ACTION_BREAK_PACT";
    public static final String DIPLO_ACTION_TRADE = "DIPLO_ACTION_TRADE";
    public static final String DIPLO_ACTION_REQUEST_TRADE = "DIPLO_ACTION_REQUEST_TRADE";
    public static final String DIPLO_ACTION_SEND_DIPLOMATS = "DIPLO_ACTION_SEND_DIPLOMATS";
    public static final String DIPLO_SEND_DIPLOMATS_TITLE = "DIPLO_SEND_DIPLOMATS_TITLE";
    public static final String DIPLO_SEND_DIPLOMATS_PROMPT = "DIPLO_SEND_DIPLOMATS_PROMPT";
    public static final String DIPLO_SEND_DIPLOMATS_SUCCESS_DYNASTY = "DIPLO_SEND_DIPLOMATS_SUCCESS_DYNASTY";
    public static final String DIPLO_SEND_DIPLOMATS_SUCCESS_COLONY = "DIPLO_SEND_DIPLOMATS_SUCCESS_COLONY";
    public static final String DIPLO_ERROR_NO_DIPLOMATS = "DIPLO_ERROR_NO_DIPLOMATS";
    public static final String DIPLO_ERROR_NO_DIPLOMAT_ROLE = "DIPLO_ERROR_NO_DIPLOMAT_ROLE";
    public static final String DIPLO_ERROR_REPUTATION_STABLE = "DIPLO_ERROR_REPUTATION_STABLE";
    public static final String DIPLO_ERROR_LOYALTY_STABLE = "DIPLO_ERROR_LOYALTY_STABLE";
    public static final String DIPLO_ERROR_CORDIAL_REQUIRED = "DIPLO_ERROR_CORDIAL_REQUIRED";
    public static final String DIPLO_ERROR_CORDIAL_REQUIRED_REQUEST = "DIPLO_ERROR_CORDIAL_REQUIRED_REQUEST";
    public static final String DIPLO_ERROR_NEUTRAL_REQUIRED = "DIPLO_ERROR_NEUTRAL_REQUIRED";
    public static final String DIPLO_ERROR_LOYALTY_REQUIRED = "DIPLO_ERROR_LOYALTY_REQUIRED";
    public static final String DIPLO_ERROR_NO_BORDER = "DIPLO_ERROR_NO_BORDER";
    public static final String DIPLO_ERROR_ACTIVE_COLONY = "DIPLO_ERROR_ACTIVE_COLONY";
    public static final String DIPLO_TRADE_REQUEST_SENT = "DIPLO_TRADE_REQUEST_SENT";
    public static final String DIPLO_TOOLTIP_BASE = "DIPLO_TOOLTIP_BASE";
    public static final String DIPLO_TOOLTIP_EFFECTIVE = "DIPLO_TOOLTIP_EFFECTIVE";
    public static final String DIPLO_MODIFIER_LINE = "DIPLO_MODIFIER_LINE";
    public static final String DIPLO_MILITARY_STRENGTH = "DIPLO_MILITARY_STRENGTH";
    public static final String LOYALTY_TOOLTIP_BASE = "LOYALTY_TOOLTIP_BASE";
    public static final String LOYALTY_TOOLTIP_EFFECTIVE = "LOYALTY_TOOLTIP_EFFECTIVE";
    public static final String LOYALTY_MODIFIER_LINE = "LOYALTY_MODIFIER_LINE";
    public static final String LOYALTY_MODIFIER_MILITARY_VS_CAPITAL = "LOYALTY_MODIFIER_MILITARY_VS_CAPITAL";
    public static final String LOYALTY_MODIFIER_DISTANCE_FROM_CAPITAL = "LOYALTY_MODIFIER_DISTANCE_FROM_CAPITAL";
    public static final String LOYALTY_MODIFIER_DISTANCE_LINE = "LOYALTY_MODIFIER_DISTANCE_LINE";

    public static final String SPECIES_OMNI = "SPECIES_OMNI";
    public static final String SPECIES_OMNI_SCIENTIFIC = "SPECIES_OMNI_SCIENTIFIC";
    public static final String SPECIES_LEAF = "SPECIES_LEAF";
    public static final String SPECIES_LEAF_SCIENTIFIC = "SPECIES_LEAF_SCIENTIFIC";
    public static final String SPECIES_PHARAOH = "SPECIES_PHARAOH";
    public static final String SPECIES_PHARAOH_SCIENTIFIC = "SPECIES_PHARAOH_SCIENTIFIC";
    public static final String SPECIES_MARAUDER = "SPECIES_MARAUDER";
    public static final String SPECIES_MARAUDER_SCIENTIFIC = "SPECIES_MARAUDER_SCIENTIFIC";

    public static final String METHOD_LAND = "METHOD_LAND";
    public static final String METHOD_AIR = "METHOD_AIR";
    public static final String METHOD_SEA = "METHOD_SEA";
    public static final String METHOD_TUNNEL = "METHOD_TUNNEL";

    public static final String EVENT_ECLIPSE_NUPTIAL = "EVENT_ECLIPSE_NUPTIAL";

    // -- GameUnlocks Keys --
    public static final String TYPE_EGG_FLAVOR = "TYPE_EGG_FLAVOR";
    public static final String TYPE_EGG_DESC = "TYPE_EGG_DESC";
    public static final String TYPE_WORKER_FLAVOR = "TYPE_WORKER_FLAVOR";
    public static final String TYPE_WORKER_DESC = "TYPE_WORKER_DESC";
    public static final String TYPE_SOLDIER_FLAVOR = "TYPE_SOLDIER_FLAVOR";
    public static final String TYPE_SOLDIER_DESC = "TYPE_SOLDIER_DESC";
    public static final String TYPE_MAJOR_FLAVOR = "TYPE_MAJOR_FLAVOR";
    public static final String TYPE_MAJOR_DESC = "TYPE_MAJOR_DESC";
    public static final String TYPE_PRINCESS_DRONE = "TYPE_PRINCESS_DRONE";
    public static final String TYPE_PRINCESS_FLAVOR = "TYPE_PRINCESS_FLAVOR";
    public static final String TYPE_PRINCESS_DESC = "TYPE_PRINCESS_DESC";
    public static final String TYPE_QUEEN_FLAVOR = "TYPE_QUEEN_FLAVOR";
    public static final String TYPE_QUEEN_DESC = "TYPE_QUEEN_DESC";

    public static final String ROLE_FORAGER_UPGRADE = "ROLE_FORAGER_UPGRADE";
    public static final String ROLE_FORAGER_FLAVOR = "ROLE_FORAGER_FLAVOR";
    public static final String ROLE_FORAGER_DESC = "ROLE_FORAGER_DESC";
    public static final String ROLE_NURSE_UPGRADE = "ROLE_NURSE_UPGRADE";
    public static final String ROLE_NURSE_FLAVOR = "ROLE_NURSE_FLAVOR";
    public static final String ROLE_NURSE_DESC = "ROLE_NURSE_DESC";
    public static final String ROLE_FARMER_UPGRADE = "ROLE_FARMER_UPGRADE";
    public static final String ROLE_FARMER_FLAVOR = "ROLE_FARMER_FLAVOR";
    public static final String ROLE_FARMER_DESC = "ROLE_FARMER_DESC";
    public static final String ROLE_GRAVER_UPGRADE = "ROLE_GRAVER_UPGRADE";
    public static final String ROLE_GRAVER_FLAVOR = "ROLE_GRAVER_FLAVOR";
    public static final String ROLE_GRAVER_DESC = "ROLE_GRAVER_DESC";
    public static final String ROLE_HUNTER_UPGRADE = "ROLE_HUNTER_UPGRADE";
    public static final String ROLE_HUNTER_FLAVOR = "ROLE_HUNTER_FLAVOR";
    public static final String ROLE_HUNTER_DESC = "ROLE_HUNTER_DESC";
    public static final String ROLE_LAYER_UPGRADE = "ROLE_LAYER_UPGRADE";
    public static final String ROLE_LAYER_FLAVOR = "ROLE_LAYER_FLAVOR";
    public static final String ROLE_LAYER_DESC = "ROLE_LAYER_DESC";
    public static final String ROLE_RANCHER_UPGRADE = "ROLE_RANCHER_UPGRADE";
    public static final String ROLE_RANCHER_FLAVOR = "ROLE_RANCHER_FLAVOR";
    public static final String ROLE_RANCHER_DESC = "ROLE_RANCHER_DESC";
    public static final String ROLE_RESEARCHER_UPGRADE = "ROLE_RESEARCHER_UPGRADE";
    public static final String ROLE_RESEARCHER_FLAVOR = "ROLE_RESEARCHER_FLAVOR";
    public static final String ROLE_RESEARCHER_DESC = "ROLE_RESEARCHER_DESC";
    public static final String ROLE_BUILDER_UPGRADE = "ROLE_BUILDER_UPGRADE";
    public static final String ROLE_BUILDER_FLAVOR = "ROLE_BUILDER_FLAVOR";
    public static final String ROLE_BUILDER_DESC = "ROLE_BUILDER_DESC";
    public static final String ROLE_SCOUT_UPGRADE = "ROLE_SCOUT_UPGRADE";
    public static final String ROLE_SCOUT_FLAVOR = "ROLE_SCOUT_FLAVOR";
    public static final String ROLE_SCOUT_DESC = "ROLE_SCOUT_DESC";
    public static final String ROLE_MINER_UPGRADE = "ROLE_MINER_UPGRADE";
    public static final String ROLE_MINER_FLAVOR = "ROLE_MINER_FLAVOR";
    public static final String ROLE_MINER_DESC = "ROLE_MINER_DESC";
    public static final String ROLE_POTTER_UPGRADE = "ROLE_POTTER_UPGRADE";
    public static final String ROLE_POTTER_FLAVOR = "ROLE_POTTER_FLAVOR";
    public static final String ROLE_POTTER_DESC = "ROLE_POTTER_DESC";
    public static final String ROLE_GUARD_UPGRADE = "ROLE_GUARD_UPGRADE";
    public static final String ROLE_GUARD_FLAVOR = "ROLE_GUARD_FLAVOR";
    public static final String ROLE_GUARD_DESC = "ROLE_GUARD_DESC";
    public static final String ROLE_WARRIOR_UPGRADE = "ROLE_WARRIOR_UPGRADE";
    public static final String ROLE_WARRIOR_FLAVOR = "ROLE_WARRIOR_FLAVOR";
    public static final String ROLE_WARRIOR_DESC = "ROLE_WARRIOR_DESC";
    public static final String ROLE_DEFENDER_UPGRADE = "ROLE_DEFENDER_UPGRADE";
    public static final String ROLE_DEFENDER_FLAVOR = "ROLE_DEFENDER_FLAVOR";
    public static final String ROLE_DEFENDER_DESC = "ROLE_DEFENDER_DESC";
    public static final String ROLE_POLICE_UPGRADE = "ROLE_POLICE_UPGRADE";
    public static final String ROLE_POLICE_FLAVOR = "ROLE_POLICE_FLAVOR";
    public static final String ROLE_POLICE_DESC = "ROLE_POLICE_DESC";
    public static final String ROLE_BOMBER_UPGRADE = "ROLE_BOMBER_UPGRADE";
    public static final String ROLE_BOMBER_FLAVOR = "ROLE_BOMBER_FLAVOR";
    public static final String ROLE_BOMBER_DESC = "ROLE_BOMBER_DESC";
    public static final String ROLE_BRUTE_UPGRADE = "ROLE_BRUTE_UPGRADE";
    public static final String ROLE_BRUTE_FLAVOR = "ROLE_BRUTE_FLAVOR";
    public static final String ROLE_BRUTE_DESC = "ROLE_BRUTE_DESC";
    public static final String ROLE_CARRIER_UPGRADE = "ROLE_CARRIER_UPGRADE";
    public static final String ROLE_CARRIER_FLAVOR = "ROLE_CARRIER_FLAVOR";
    public static final String ROLE_CARRIER_DESC = "ROLE_CARRIER_DESC";
    public static final String ROLE_ARTILLERY_UPGRADE = "ROLE_ARTILLERY_UPGRADE";
    public static final String ROLE_ARTILLERY_FLAVOR = "ROLE_ARTILLERY_FLAVOR";
    public static final String ROLE_ARTILLERY_DESC = "ROLE_ARTILLERY_DESC";
    public static final String ROLE_SIEGE_UPGRADE = "ROLE_SIEGE_UPGRADE";
    public static final String ROLE_SIEGE_FLAVOR = "ROLE_SIEGE_FLAVOR";
    public static final String ROLE_SIEGE_DESC = "ROLE_SIEGE_DESC";
    public static final String ROLE_BREEDER_UPGRADE = "ROLE_BREEDER_UPGRADE";
    public static final String ROLE_BREEDER_FLAVOR = "ROLE_BREEDER_FLAVOR";
    public static final String ROLE_BREEDER_DESC = "ROLE_BREEDER_DESC";
    public static final String ROLE_DIPLOMAT_UPGRADE = "ROLE_DIPLOMAT_UPGRADE";
    public static final String ROLE_DIPLOMAT_FLAVOR = "ROLE_DIPLOMAT_FLAVOR";
    public static final String ROLE_DIPLOMAT_DESC = "ROLE_DIPLOMAT_DESC";
    public static final String ROLE_MILITIA_UPGRADE = "ROLE_MILITIA_UPGRADE";
    public static final String ROLE_MILITIA_FLAVOR = "ROLE_MILITIA_FLAVOR";
    public static final String ROLE_MILITIA_DESC = "ROLE_MILITIA_DESC";
    public static final String ROLE_CATCHER_UPGRADE = "ROLE_CATCHER_UPGRADE";
    public static final String ROLE_CATCHER_FLAVOR = "ROLE_CATCHER_FLAVOR";
    public static final String ROLE_CATCHER_DESC = "ROLE_CATCHER_DESC";
    public static final String ROLE_CRANE_UPGRADE = "ROLE_CRANE_UPGRADE";
    public static final String ROLE_CRANE_FLAVOR = "ROLE_CRANE_FLAVOR";
    public static final String ROLE_CRANE_DESC = "ROLE_CRANE_DESC";
    public static final String ROLE_ASSISTANT_UPGRADE = "ROLE_ASSISTANT_UPGRADE";
    public static final String ROLE_ASSISTANT_FLAVOR = "ROLE_ASSISTANT_FLAVOR";
    public static final String ROLE_ASSISTANT_DESC = "ROLE_ASSISTANT_DESC";
    public static final String ROLE_COURIER_UPGRADE = "ROLE_COURIER_UPGRADE";
    public static final String ROLE_COURIER_FLAVOR = "ROLE_COURIER_FLAVOR";
    public static final String ROLE_COURIER_DESC = "ROLE_COURIER_DESC";
    public static final String ROLE_BORER_UPGRADE = "ROLE_BORER_UPGRADE";
    public static final String ROLE_BORER_FLAVOR = "ROLE_BORER_FLAVOR";
    public static final String ROLE_BORER_DESC = "ROLE_BORER_DESC";
    public static final String ROLE_TRANSPORT_UPGRADE = "ROLE_TRANSPORT_UPGRADE";
    public static final String ROLE_TRANSPORT_FLAVOR = "ROLE_TRANSPORT_FLAVOR";
    public static final String ROLE_TRANSPORT_DESC = "ROLE_TRANSPORT_DESC";
    public static final String ROLE_ESCORT_UPGRADE = "ROLE_ESCORT_UPGRADE";
    public static final String ROLE_ESCORT_FLAVOR = "ROLE_ESCORT_FLAVOR";
    public static final String ROLE_ESCORT_DESC = "ROLE_ESCORT_DESC";
    public static final String ROLE_ENGINEER_UPGRADE = "ROLE_ENGINEER_UPGRADE";
    public static final String ROLE_ENGINEER_FLAVOR = "ROLE_ENGINEER_FLAVOR";
    public static final String ROLE_ENGINEER_DESC = "ROLE_ENGINEER_DESC";
    public static final String ROLE_SKYTRANS_UPGRADE = "ROLE_SKYTRANS_UPGRADE";
    public static final String ROLE_SKYTRANS_FLAVOR = "ROLE_SKYTRANS_FLAVOR";
    public static final String ROLE_SKYTRANS_DESC = "ROLE_SKYTRANS_DESC";

    public static final String ABILITY_RESEARCH_FLAVOR = "ABILITY_RESEARCH_FLAVOR";
    public static final String ABILITY_RESEARCH_DESC = "ABILITY_RESEARCH_DESC";
    public static final String ABILITY_BUILD_FLAVOR = "ABILITY_BUILD_FLAVOR";
    public static final String ABILITY_BUILD_DESC = "ABILITY_BUILD_DESC";
    public static final String ABILITY_SPREAD_FLAVOR = "ABILITY_SPREAD_FLAVOR";
    public static final String ABILITY_SPREAD_DESC = "ABILITY_SPREAD_DESC";
    public static final String ABILITY_RESIN_FLAVOR = "ABILITY_RESIN_FLAVOR";
    public static final String ABILITY_RESIN_DESC = "ABILITY_RESIN_DESC";
    public static final String ABILITY_SYNERGY_FLAVOR = "ABILITY_SYNERGY_FLAVOR";
    public static final String ABILITY_SYNERGY_DESC = "ABILITY_SYNERGY_DESC";
    public static final String ABILITY_ASSIMILATION_FLAVOR = "ABILITY_ASSIMILATION_FLAVOR";
    public static final String ABILITY_ASSIMILATION_DESC = "ABILITY_ASSIMILATION_DESC";
    public static final String ABILITY_FORCED_FLIGHT_FLAVOR = "ABILITY_FORCED_FLIGHT_FLAVOR";
    public static final String ABILITY_DYNASTY_FLAVOR = "ABILITY_DYNASTY_FLAVOR";
    public static final String ABILITY_DYNASTY_DESC = "ABILITY_DYNASTY_DESC";
    public static final String ABILITY_TRADE_FLAVOR = "ABILITY_TRADE_FLAVOR";
    public static final String ABILITY_TRADE_DESC = "ABILITY_TRADE_DESC";
    public static final String ABILITY_SPREAD_2_FLAVOR = "ABILITY_SPREAD_2_FLAVOR";
    public static final String ABILITY_SPREAD_2_DESC = "ABILITY_SPREAD_2_DESC";
    public static final String ABILITY_AUTOMATION_FLAVOR = "ABILITY_AUTOMATION_FLAVOR";
    public static final String ABILITY_AUTOMATION_DESC = "ABILITY_AUTOMATION_DESC";
    public static final String ABILITY_TUNNELS_FLAVOR = "ABILITY_TUNNELS_FLAVOR";
    public static final String ABILITY_TUNNELS_DESC = "ABILITY_TUNNELS_DESC";
    public static final String ABILITY_BILATERAL_TRADE_FLAVOR = "ABILITY_BILATERAL_TRADE_FLAVOR";
    public static final String ABILITY_BILATERAL_TRADE_DESC = "ABILITY_BILATERAL_TRADE_DESC";
    public static final String ABILITY_MANAGEMENT_FLAVOR = "ABILITY_MANAGEMENT_FLAVOR";
    public static final String ABILITY_MANAGEMENT_DESC = "ABILITY_MANAGEMENT_DESC";
    public static final String ABILITY_MASS_FLIGHT_FLAVOR = "ABILITY_MASS_FLIGHT_FLAVOR";
    public static final String ABILITY_CLONING_FLAVOR = "ABILITY_CLONING_FLAVOR";
    public static final String ABILITY_CLONING_DESC = "ABILITY_CLONING_DESC";
    public static final String ABILITY_PARASITIC_MITE_ALERT_FLAVOR = "ABILITY_PARASITIC_MITE_ALERT_FLAVOR";
    public static final String ABILITY_PARASITIC_MITE_ALERT_DESC = "ABILITY_PARASITIC_MITE_ALERT_DESC";
    public static final String ABILITY_CATCH_SYMBIOTIC_MITE_FLAVOR = "ABILITY_CATCH_SYMBIOTIC_MITE_FLAVOR";
    public static final String ABILITY_CATCH_SYMBIOTIC_MITE_DESC = "ABILITY_CATCH_SYMBIOTIC_MITE_DESC";
    public static final String ABILITY_CATCH_DERMESTID_FLAVOR = "ABILITY_CATCH_DERMESTID_FLAVOR";
    public static final String ABILITY_CATCH_DERMESTID_DESC = "ABILITY_CATCH_DERMESTID_DESC";
    public static final String ABILITY_DIPLOMAT_PRESSURE_2 = "ABILITY_DIPLOMAT_PRESSURE_2";
    public static final String ABILITY_DIPLOMAT_PRESSURE_2_FLAVOR = "ABILITY_DIPLOMAT_PRESSURE_2_FLAVOR";
    public static final String ABILITY_DIPLOMAT_PRESSURE_2_DESC = "ABILITY_DIPLOMAT_PRESSURE_2_DESC";
    public static final String ABILITY_DIPLOMAT_PRESSURE_3 = "ABILITY_DIPLOMAT_PRESSURE_3";
    public static final String ABILITY_DIPLOMAT_PRESSURE_3_FLAVOR = "ABILITY_DIPLOMAT_PRESSURE_3_FLAVOR";
    public static final String ABILITY_DIPLOMAT_PRESSURE_3_DESC = "ABILITY_DIPLOMAT_PRESSURE_3_DESC";
    public static final String ABILITY_ABILITY_FLAVOR = "ABILITY_ABILITY_FLAVOR";
    public static final String ABILITY_ABILITY_DESC = "ABILITY_ABILITY_DESC";

    public static final String STAT_SKELETON_FLAVOR = "STAT_SKELETON_FLAVOR";
    public static final String STAT_SKELETON_DESC = "STAT_SKELETON_DESC";
    public static final String STAT_ACID_FLAVOR = "STAT_ACID_FLAVOR";
    public static final String STAT_ACID_DESC = "STAT_ACID_DESC";
    public static final String STAT_LONGEVITY_FLAVOR = "STAT_LONGEVITY_FLAVOR";
    public static final String STAT_LONGEVITY_DESC = "STAT_LONGEVITY_DESC";
    public static final String STAT_RESEARCH_1_FLAVOR = "STAT_RESEARCH_1_FLAVOR";
    public static final String STAT_RESEARCH_1_DESC = "STAT_RESEARCH_1_DESC";
    public static final String STAT_RESEARCH_2_FLAVOR = "STAT_RESEARCH_2_FLAVOR";
    public static final String STAT_RESEARCH_2_DESC = "STAT_RESEARCH_2_DESC";
    public static final String STAT_RESEARCH_3_FLAVOR = "STAT_RESEARCH_3_FLAVOR";
    public static final String STAT_RESEARCH_3_DESC = "STAT_RESEARCH_3_DESC";
    public static final String STAT_GROWTH_1_FLAVOR = "STAT_GROWTH_1_FLAVOR";
    public static final String STAT_GROWTH_1_DESC = "STAT_GROWTH_1_DESC";
    public static final String STAT_GROWTH_2_FLAVOR = "STAT_GROWTH_2_FLAVOR";
    public static final String STAT_GROWTH_2_DESC = "STAT_GROWTH_2_DESC";
    public static final String STAT_GROWTH_3_FLAVOR = "STAT_GROWTH_3_FLAVOR";
    public static final String STAT_GROWTH_3_DESC = "STAT_GROWTH_3_DESC";
    public static final String STAT_THIRST_1_FLAVOR = "STAT_THIRST_1_FLAVOR";
    public static final String STAT_THIRST_1_DESC = "STAT_THIRST_1_DESC";
    public static final String STAT_THIRST_2_FLAVOR = "STAT_THIRST_2_FLAVOR";
    public static final String STAT_THIRST_2_DESC = "STAT_THIRST_2_DESC";
    public static final String STAT_THIRST_3_FLAVOR = "STAT_THIRST_3_FLAVOR";
    public static final String STAT_THIRST_3_DESC = "STAT_THIRST_3_DESC";
    public static final String STAT_LOGISTICS_1_FLAVOR = "STAT_LOGISTICS_1_FLAVOR";
    public static final String STAT_LOGISTICS_1_DESC = "STAT_LOGISTICS_1_DESC";
    public static final String STAT_PASSIVE_1_FLAVOR = "STAT_PASSIVE_1_FLAVOR";
    public static final String STAT_PASSIVE_1_DESC = "STAT_PASSIVE_1_DESC";
    public static final String STAT_LAYING_1_FLAVOR = "STAT_LAYING_1_FLAVOR";
    public static final String STAT_LAYING_1_DESC = "STAT_LAYING_1_DESC";
    public static final String STAT_LAYING_2_FLAVOR = "STAT_LAYING_2_FLAVOR";
    public static final String STAT_LAYING_2_DESC = "STAT_LAYING_2_DESC";
    public static final String STAT_LAYING_3_FLAVOR = "STAT_LAYING_3_FLAVOR";
    public static final String STAT_LAYING_3_DESC = "STAT_LAYING_3_DESC";
    public static final String STAT_SCOUTING_1_FLAVOR = "STAT_SCOUTING_1_FLAVOR";
    public static final String STAT_SCOUTING_1_DESC = "STAT_SCOUTING_1_DESC";
    public static final String STAT_SCOUTING_2_FLAVOR = "STAT_SCOUTING_2_FLAVOR";
    public static final String STAT_SCOUTING_2_DESC = "STAT_SCOUTING_2_DESC";
    public static final String STAT_SCOUTING_3_FLAVOR = "STAT_SCOUTING_3_FLAVOR";
    public static final String STAT_SCOUTING_3_DESC = "STAT_SCOUTING_3_DESC";
    public static final String STAT_FARMING_1_FLAVOR = "STAT_FARMING_1_FLAVOR";
    public static final String STAT_FARMING_1_DESC = "STAT_FARMING_1_DESC";
    public static final String STAT_FARMING_2_FLAVOR = "STAT_FARMING_2_FLAVOR";
    public static final String STAT_FARMING_2_DESC = "STAT_FARMING_2_DESC";
    public static final String STAT_FARMING_3_FLAVOR = "STAT_FARMING_3_FLAVOR";
    public static final String STAT_FARMING_3_DESC = "STAT_FARMING_3_DESC";
    public static final String STAT_CONTAMINATION_1_FLAVOR = "STAT_CONTAMINATION_1_FLAVOR";
    public static final String STAT_CONTAMINATION_1_DESC = "STAT_CONTAMINATION_1_DESC";
    public static final String STAT_CONTAMINATION_2_FLAVOR = "STAT_CONTAMINATION_2_FLAVOR";
    public static final String STAT_CONTAMINATION_2_DESC = "STAT_CONTAMINATION_2_DESC";
    public static final String STAT_CONTAMINATION_3_FLAVOR = "STAT_CONTAMINATION_3_FLAVOR";
    public static final String STAT_CONTAMINATION_3_DESC = "STAT_CONTAMINATION_3_DESC";
    public static final String STAT_SYMBIOTIC_MITE_1_FLAVOR = "STAT_SYMBIOTIC_MITE_1_FLAVOR";
    public static final String STAT_SYMBIOTIC_MITE_1_DESC = "STAT_SYMBIOTIC_MITE_1_DESC";
    public static final String STAT_DERMESTID_1_FLAVOR = "STAT_DERMESTID_1_FLAVOR";
    public static final String STAT_DERMESTID_1_DESC = "STAT_DERMESTID_1_DESC";
    public static final String STAT_GRAVING_1_FLAVOR = "STAT_GRAVING_1_FLAVOR";
    public static final String STAT_GRAVING_1_DESC = "STAT_GRAVING_1_DESC";
    public static final String STAT_GRAVING_2_FLAVOR = "STAT_GRAVING_2_FLAVOR";
    public static final String STAT_GRAVING_2_DESC = "STAT_GRAVING_2_DESC";
    public static final String STAT_GRAVING_3_FLAVOR = "STAT_GRAVING_3_FLAVOR";
    public static final String STAT_GRAVING_3_DESC = "STAT_GRAVING_3_DESC";
    public static final String STAT_POLICING_1_FLAVOR = "STAT_POLICING_1_FLAVOR";
    public static final String STAT_POLICING_1_DESC = "STAT_POLICING_1_DESC";
    public static final String STAT_POLICING_2_FLAVOR = "STAT_POLICING_2_FLAVOR";
    public static final String STAT_POLICING_2_DESC = "STAT_POLICING_2_DESC";
    public static final String STAT_POLICING_3_FLAVOR = "STAT_POLICING_3_FLAVOR";
    public static final String STAT_POLICING_3_DESC = "STAT_POLICING_3_DESC";
    public static final String STAT_HEX_SUSTAIN_FLAVOR = "STAT_HEX_SUSTAIN_FLAVOR";
    public static final String STAT_HEX_SUSTAIN_DESC = "STAT_HEX_SUSTAIN_DESC";
    public static final String STAT_WORKER_SPEED_2_FLAVOR = "STAT_WORKER_SPEED_2_FLAVOR";
    public static final String STAT_WORKER_SPEED_2_DESC = "STAT_WORKER_SPEED_2_DESC";

    public static final String STAT_CAT_OVERWORLD = "STAT_CAT_OVERWORLD";
    public static final String STAT_HEX_DEPLETION = "STAT_HEX_DEPLETION";
    public static final String STAT_HEX_SOURCES_FOUND = "STAT_HEX_SOURCES_FOUND";
    public static final String STAT_HEX_MAX_EFFICIENCY_DISTANCE = "STAT_HEX_MAX_EFFICIENCY_DISTANCE";
    public static final String STAT_LOCAL_HEX_NOTE_CAT = "STAT_LOCAL_HEX_NOTE_CAT";
    public static final String STAT_LOCAL_HEX_DYNASTY_HINT = "STAT_LOCAL_HEX_DYNASTY_HINT";
    public static final String UNIT_STAT_WORKER_MARCH = "UNIT_STAT_WORKER_MARCH";
    public static final String UNIT_STAT_WORKER_MARCH_DESC = "UNIT_STAT_WORKER_MARCH_DESC";

    public static final String ASSIMILATED_FARMING_FLAVOR = "ASSIMILATED_FARMING_FLAVOR";
    public static final String ASSIMILATED_FARMING_DESC = "ASSIMILATED_FARMING_DESC";
    public static final String ASSIMILATED_MULTIQUEEN_FLAVOR = "ASSIMILATED_MULTIQUEEN_FLAVOR";
    public static final String ASSIMILATED_MULTIQUEEN_DESC = "ASSIMILATED_MULTIQUEEN_DESC";

    public static final String ROYAL_CHAMBER_0 = "ROYAL_CHAMBER_0";
    public static final String ROYAL_CHAMBER_0_DESC = "ROYAL_CHAMBER_0_DESC";
    public static final String EGG_CHAMBER_0 = "EGG_CHAMBER_0";
    public static final String EGG_CHAMBER_0_DESC = "EGG_CHAMBER_0_DESC";
    public static final String MUSHROOM_CHAMBER_0 = "MUSHROOM_CHAMBER_0";
    public static final String MUSHROOM_CHAMBER_0_DESC = "MUSHROOM_CHAMBER_0_DESC";
    public static final String PLANT_CHAMBER_0 = "PLANT_CHAMBER_0";
    public static final String PLANT_CHAMBER_0_DESC = "PLANT_CHAMBER_0_DESC";
    public static final String WATER_RESERVOIR_0 = "WATER_RESERVOIR_0";
    public static final String WATER_RESERVOIR_0_DESC = "WATER_RESERVOIR_0_DESC";
    public static final String MEAT_CHAMBER_0 = "MEAT_CHAMBER_0";
    public static final String MEAT_CHAMBER_0_DESC = "MEAT_CHAMBER_0_DESC";
    public static final String SYRUP_RESERVOIR_0 = "SYRUP_RESERVOIR_0";
    public static final String SYRUP_RESERVOIR_0_DESC = "SYRUP_RESERVOIR_0_DESC";
    public static final String ROCK_WAREHOUSE_0 = "ROCK_WAREHOUSE_0";
    public static final String ROCK_WAREHOUSE_0_DESC = "ROCK_WAREHOUSE_0_DESC";
    public static final String RESIN_RESERVOIR_0 = "RESIN_RESERVOIR_0";
    public static final String RESIN_RESERVOIR_0_DESC = "RESIN_RESERVOIR_0_DESC";

    public static final String ROYAL_CHAMBER_1 = "ROYAL_CHAMBER_1";
    public static final String ROYAL_CHAMBER_1_DESC = "ROYAL_CHAMBER_1_DESC";
    public static final String EGG_CHAMBER_1 = "EGG_CHAMBER_1";
    public static final String EGG_CHAMBER_1_DESC = "EGG_CHAMBER_1_DESC";
    public static final String MUSHROOM_CHAMBER_1 = "MUSHROOM_CHAMBER_1";
    public static final String MUSHROOM_CHAMBER_1_DESC = "MUSHROOM_CHAMBER_1_DESC";
    public static final String PLANT_CHAMBER_1 = "PLANT_CHAMBER_1";
    public static final String PLANT_CHAMBER_1_DESC = "PLANT_CHAMBER_1_DESC";
    public static final String WATER_RESERVOIR_1 = "WATER_RESERVOIR_1";
    public static final String WATER_RESERVOIR_1_DESC = "WATER_RESERVOIR_1_DESC";
    public static final String MEAT_CHAMBER_1 = "MEAT_CHAMBER_1";
    public static final String MEAT_CHAMBER_1_DESC = "MEAT_CHAMBER_1_DESC";
    public static final String SYRUP_RESERVOIR_1 = "SYRUP_RESERVOIR_1";
    public static final String SYRUP_RESERVOIR_1_DESC = "SYRUP_RESERVOIR_1_DESC";
    public static final String ROCK_WAREHOUSE_1 = "ROCK_WAREHOUSE_1";
    public static final String ROCK_WAREHOUSE_1_DESC = "ROCK_WAREHOUSE_1_DESC";
    public static final String RESIN_RESERVOIR_1 = "RESIN_RESERVOIR_1";
    public static final String RESIN_RESERVOIR_1_DESC = "RESIN_RESERVOIR_1_DESC";

    public static final String ROYAL_CHAMBER_2 = "ROYAL_CHAMBER_2";
    public static final String ROYAL_CHAMBER_2_DESC = "ROYAL_CHAMBER_2_DESC";
    public static final String EGG_CHAMBER_2 = "EGG_CHAMBER_2";
    public static final String EGG_CHAMBER_2_DESC = "EGG_CHAMBER_2_DESC";
    public static final String MUSHROOM_CHAMBER_2 = "MUSHROOM_CHAMBER_2";
    public static final String MUSHROOM_CHAMBER_2_DESC = "MUSHROOM_CHAMBER_2_DESC";
    public static final String PLANT_CHAMBER_2 = "PLANT_CHAMBER_2";
    public static final String PLANT_CHAMBER_2_DESC = "PLANT_CHAMBER_2_DESC";
    public static final String WATER_RESERVOIR_2 = "WATER_RESERVOIR_2";
    public static final String WATER_RESERVOIR_2_DESC = "WATER_RESERVOIR_2_DESC";
    public static final String MEAT_CHAMBER_2 = "MEAT_CHAMBER_2";
    public static final String MEAT_CHAMBER_2_DESC = "MEAT_CHAMBER_2_DESC";
    public static final String SYRUP_RESERVOIR_2 = "SYRUP_RESERVOIR_2";
    public static final String SYRUP_RESERVOIR_2_DESC = "SYRUP_RESERVOIR_2_DESC";
    public static final String ROCK_WAREHOUSE_2 = "ROCK_WAREHOUSE_2";
    public static final String ROCK_WAREHOUSE_2_DESC = "ROCK_WAREHOUSE_2_DESC";
    public static final String RESIN_RESERVOIR_2 = "RESIN_RESERVOIR_2";
    public static final String RESIN_RESERVOIR_2_DESC = "RESIN_RESERVOIR_2_DESC";

    public static final String ROYAL_CHAMBER_3 = "ROYAL_CHAMBER_3";
    public static final String ROYAL_CHAMBER_3_DESC = "ROYAL_CHAMBER_3_DESC";
    public static final String EGG_CHAMBER_3 = "EGG_CHAMBER_3";
    public static final String EGG_CHAMBER_3_DESC = "EGG_CHAMBER_3_DESC";
    public static final String MUSHROOM_CHAMBER_3 = "MUSHROOM_CHAMBER_3";
    public static final String MUSHROOM_CHAMBER_3_DESC = "MUSHROOM_CHAMBER_3_DESC";
    public static final String PLANT_CHAMBER_3 = "PLANT_CHAMBER_3";
    public static final String PLANT_CHAMBER_3_DESC = "PLANT_CHAMBER_3_DESC";
    public static final String WATER_RESERVOIR_3 = "WATER_RESERVOIR_3";
    public static final String WATER_RESERVOIR_3_DESC = "WATER_RESERVOIR_3_DESC";
    public static final String MEAT_CHAMBER_3 = "MEAT_CHAMBER_3";
    public static final String MEAT_CHAMBER_3_DESC = "MEAT_CHAMBER_3_DESC";
    public static final String SYRUP_RESERVOIR_3 = "SYRUP_RESERVOIR_3";
    public static final String SYRUP_RESERVOIR_3_DESC = "SYRUP_RESERVOIR_3_DESC";
    public static final String ROCK_WAREHOUSE_3 = "ROCK_WAREHOUSE_3";
    public static final String ROCK_WAREHOUSE_3_DESC = "ROCK_WAREHOUSE_3_DESC";
    public static final String RESIN_RESERVOIR_3 = "RESIN_RESERVOIR_3";
    public static final String RESIN_RESERVOIR_3_DESC = "RESIN_RESERVOIR_3_DESC";

    public static final String PASSIVE_LAB = "PASSIVE_LAB";
    public static final String PASSIVE_LAB_DESC = "PASSIVE_LAB_DESC";
    public static final String PASSIVE_WATER = "PASSIVE_WATER";
    public static final String PASSIVE_WATER_DESC = "PASSIVE_WATER_DESC";
    public static final String PASSIVE_APHID = "PASSIVE_APHID";
    public static final String PASSIVE_APHID_DESC = "PASSIVE_APHID_DESC";
    public static final String PASSIVE_NURSE = "PASSIVE_NURSE";
    public static final String PASSIVE_NURSE_DESC = "PASSIVE_NURSE_DESC";
    public static final String PASSIVE_FARM = "PASSIVE_FARM";
    public static final String PASSIVE_FARM_DESC = "PASSIVE_FARM_DESC";
    public static final String PASSIVE_GRAVE = "PASSIVE_GRAVE";
    public static final String PASSIVE_GRAVE_DESC = "PASSIVE_GRAVE_DESC";
    public static final String BUILDING_COMPOSTER = "BUILDING_COMPOSTER";
    public static final String BUILDING_COMPOSTER_DESC = "BUILDING_COMPOSTER_DESC";

    public static final String ASSIMILATION_LEAFCUTTER = "ASSIMILATION_LEAFCUTTER";
    public static final String ASSIMILATION_LEAFCUTTER_DESC = "ASSIMILATION_LEAFCUTTER_DESC";
    public static final String ASSIMILATION_PHARAOH = "ASSIMILATION_PHARAOH";
    public static final String ASSIMILATION_PHARAOH_DESC = "ASSIMILATION_PHARAOH_DESC";
    public static final String ASSIMILATION_MARAUDER = "ASSIMILATION_MARAUDER";
    public static final String ASSIMILATION_MARAUDER_DESC = "ASSIMILATION_MARAUDER_DESC";

    // -- HelpPanel Keys --
    public static final String HELP_TAB_WELCOME = "HELP_TAB_WELCOME";
    public static final String HELP_TAB_STARTED = "HELP_TAB_STARTED";
    public static final String HELP_TAB_DYNASTY = "HELP_TAB_DYNASTY";
    public static final String HELP_TAB_HOTKEYS = "HELP_TAB_HOTKEYS";
    public static final String HELP_TAB_TUTORIALS = "HELP_TAB_TUTORIALS";
    public static final String HELP_TAB_SPECIES = "HELP_TAB_SPECIES";
    public static final String HELP_TAB_TYPES = "HELP_TAB_TYPES";
    public static final String HELP_TAB_BUGS = "HELP_TAB_BUGS";
    public static final String HELP_TAB_ROLES = "HELP_TAB_ROLES";
    public static final String HELP_TAB_ANT_ROLES = "HELP_TAB_ANT_ROLES";
    public static final String HELP_TAB_EMPIRE = "HELP_TAB_EMPIRE";
    public static final String HELP_TAB_UPGRADES = "HELP_TAB_UPGRADES";
    public static final String HELP_TAB_BUILDINGS = "HELP_TAB_BUILDINGS";
    public static final String HELP_TAB_ASSIMILATIONS = "HELP_TAB_ASSIMILATIONS";
    public static final String HELP_TAB_WORLD = "HELP_TAB_WORLD";

    public static final String HELP_WELCOME_STORY = "HELP_WELCOME_STORY";
    public static final String HELP_START_INFO = "HELP_START_INFO";
    public static final String HELP_OVERWORLD_GATHERING = "HELP_OVERWORLD_GATHERING";
    public static final String HELP_DYNASTY_INFO = "HELP_DYNASTY_INFO";
    public static final String HELP_EMPIRE_TRADE = "HELP_EMPIRE_TRADE";
    public static final String HELP_EMPIRE_LOYALTY = "HELP_EMPIRE_LOYALTY";
    public static final String HELP_EMPIRE_REPUTATION = "HELP_EMPIRE_REPUTATION";
    public static final String HELP_EMPIRE_MOVEMENT = "HELP_EMPIRE_MOVEMENT";
    public static final String HELP_TIER_MIN_SCORE = "HELP_TIER_MIN_SCORE";
    public static final String HELP_LOYALTY_MODIFIERS_TITLE = "HELP_LOYALTY_MODIFIERS_TITLE";
    public static final String HELP_LOYALTY_MODIFIER_MILITARY = "HELP_LOYALTY_MODIFIER_MILITARY";
    public static final String HELP_LOYALTY_MODIFIER_DISTANCE = "HELP_LOYALTY_MODIFIER_DISTANCE";
    public static final String HELP_ROLE_ANT_TYPE = "HELP_ROLE_ANT_TYPE";

    public static final String METHOD_LAND_DESC = "METHOD_LAND_DESC";
    public static final String METHOD_AIR_DESC = "METHOD_AIR_DESC";
    public static final String METHOD_SEA_DESC = "METHOD_SEA_DESC";
    public static final String METHOD_TUNNEL_DESC = "METHOD_TUNNEL_DESC";

    public static final String MOVE_STATIC_DESC = "MOVE_STATIC_DESC";
    public static final String MOVE_WANDER_DESC = "MOVE_WANDER_DESC";
    public static final String MOVE_MARCH_DESC = "MOVE_MARCH_DESC";
    public static final String MOVE_SPEED_DESC = "MOVE_SPEED_DESC";
    public static final String MOVE_FLY_DESC = "MOVE_FLY_DESC";
    public static final String MOVE_PATROL_DESC = "MOVE_PATROL_DESC";

    public static final String HELP_SPECIES_SCIENTIFIC = "HELP_SPECIES_SCIENTIFIC";
    public static final String HELP_SPECIES_TRAITS = "HELP_SPECIES_TRAITS";

    public static final String HELP_RESOURCE_SOURCE_SMALL = "HELP_RESOURCE_SOURCE_SMALL";
    public static final String HELP_RESOURCE_SOURCE_MEDIUM = "HELP_RESOURCE_SOURCE_MEDIUM";
    public static final String HELP_RESOURCE_SOURCE_BIG = "HELP_RESOURCE_SOURCE_BIG";
    public static final String HELP_RESOURCE_SOURCE_HUGE = "HELP_RESOURCE_SOURCE_HUGE";

    public static final String HOTKEY_PAUSE = "HOTKEY_PAUSE";
    public static final String HOTKEY_SPEED = "HOTKEY_SPEED";
    public static final String HOTKEY_VIEW = "HOTKEY_VIEW";
    public static final String HOTKEY_ESC = "HOTKEY_ESC";
    public static final String HOTKEY_ROLES = "HOTKEY_ROLES";
    public static final String HOTKEY_P = "HOTKEY_P";
    public static final String HOTKEY_UPGRADES = "HOTKEY_UPGRADES";
    public static final String HOTKEY_Z = "HOTKEY_Z";
    public static final String HOTKEY_DYNASTY = "HOTKEY_DYNASTY";
    public static final String HOTKEY_M = "HOTKEY_M";
    public static final String HOTKEY_X = "HOTKEY_X";

    public static final String HELP_TYPE_EGG_DESC = "HELP_TYPE_EGG_DESC";
    public static final String HELP_TYPE_LARVA_DESC = "HELP_TYPE_LARVA_DESC";
    public static final String HELP_TYPE_PUPA_DESC = "HELP_TYPE_PUPA_DESC";
    public static final String HELP_TYPE_WORKER_DESC = "HELP_TYPE_WORKER_DESC";
    public static final String HELP_TYPE_SOLDIER_DESC = "HELP_TYPE_SOLDIER_DESC";
    public static final String HELP_TYPE_MAJOR_DESC = "HELP_TYPE_MAJOR_DESC";
    public static final String HELP_TYPE_PRINCESS_DESC = "HELP_TYPE_PRINCESS_DESC";
    public static final String HELP_TYPE_DRONE_DESC = "HELP_TYPE_DRONE_DESC";
    public static final String HELP_TYPE_QUEEN_DESC = "HELP_TYPE_QUEEN_DESC";

    public static final String HELP_BUG_ANT_DESC = "HELP_BUG_ANT_DESC";
    public static final String HELP_BUG_APHID_DESC = "HELP_BUG_APHID_DESC";
    public static final String HELP_BUG_SYMBIOTIC_MITE_DESC = "HELP_BUG_SYMBIOTIC_MITE_DESC";
    public static final String HELP_BUG_DERMESTID_DESC = "HELP_BUG_DERMESTID_DESC";
    public static final String HELP_BUG_PARASITE_ANT_DESC = "HELP_BUG_PARASITE_ANT_DESC";
    public static final String HELP_BUG_PARASITIC_MITE_DESC = "HELP_BUG_PARASITIC_MITE_DESC";

    public static final String HELP_BIOMES_TITLE = "HELP_BIOMES_TITLE";
    public static final String HELP_BIOME_TEMP = "HELP_BIOME_TEMP";
    public static final String HELP_BIOME_HUMID = "HELP_BIOME_HUMID";
    public static final String HELP_WORLD_INFO_TITLE = "HELP_WORLD_INFO_TITLE";
    public static final String HELP_SEASONS = "HELP_SEASONS";
    public static final String HELP_WEATHER = "HELP_WEATHER";
    public static final String HELP_TIME_DAY = "HELP_TIME_DAY";
    public static final String HELP_MOON = "HELP_MOON";
    public static final String HELP_TEMP = "HELP_TEMP";
    public static final String HELP_HUMID = "HELP_HUMID";
    public static final String HELP_SELECT_ITEM = "HELP_SELECT_ITEM";
    public static final String HELP_BUILD_BASE_COST = "HELP_BUILD_BASE_COST";

    public static final String HELP_TUTORIAL_TITLE = "HELP_TUTORIAL_TITLE";
    public static final String HELP_TUTORIAL_TIPS = "HELP_TUTORIAL_TIPS";
    public static final String HELP_TUTORIAL_THREATS = "HELP_TUTORIAL_THREATS";
    public static final String HELP_TUTORIAL_DYNASTY = "HELP_TUTORIAL_DYNASTY";
    
    public static final String HOTKEY_PAUSE_LABEL = "HOTKEY_PAUSE_LABEL";
    public static final String HOTKEY_ESC_LABEL = "HOTKEY_ESC_LABEL";
    public static final String HOTKEY_VIEW_LABEL = "HOTKEY_VIEW_LABEL";
    public static final String HOTKEY_ROLES_LABEL = "HOTKEY_ROLES_LABEL";
    public static final String HOTKEY_UPGRADES_LABEL = "HOTKEY_UPGRADES_LABEL";
    public static final String HOTKEY_P_LABEL = "HOTKEY_P_LABEL";
    public static final String HOTKEY_DYNASTY_LABEL = "HOTKEY_DYNASTY_LABEL";
    public static final String HOTKEY_M_LABEL = "HOTKEY_M_LABEL";
    public static final String HOTKEY_X_LABEL = "HOTKEY_X_LABEL";

    public static final String HELP_SKIP_TUTORIAL = "HELP_SKIP_TUTORIAL";
    public static final String HELP_FINISH = "HELP_FINISH";

    // -- Trigger unlock dialogs --
    public static final String TRIGGER_CLONING_TITLE = "TRIGGER_CLONING_TITLE";
    public static final String TRIGGER_CLONING_MSG = "TRIGGER_CLONING_MSG";
    public static final String TRIGGER_RESEARCHER_ROLE_TITLE = "TRIGGER_RESEARCHER_ROLE_TITLE";
    public static final String TRIGGER_RESEARCHER_ROLE_MSG = "TRIGGER_RESEARCHER_ROLE_MSG";
    public static final String TRIGGER_GRAVER_ROLE_TITLE = "TRIGGER_GRAVER_ROLE_TITLE";
    public static final String TRIGGER_GRAVER_ROLE_MSG = "TRIGGER_GRAVER_ROLE_MSG";
    public static final String TRIGGER_RESEARCH_ABILITY_TITLE = "TRIGGER_RESEARCH_ABILITY_TITLE";
    public static final String TRIGGER_RESEARCH_ABILITY_MSG = "TRIGGER_RESEARCH_ABILITY_MSG";
    public static final String TRIGGER_BUILD_ABILITY_TITLE = "TRIGGER_BUILD_ABILITY_TITLE";
    public static final String TRIGGER_BUILD_ABILITY_MSG = "TRIGGER_BUILD_ABILITY_MSG";
    public static final String TRIGGER_HUNTER_ROLE_TITLE = "TRIGGER_HUNTER_ROLE_TITLE";
    public static final String TRIGGER_HUNTER_ROLE_MSG = "TRIGGER_HUNTER_ROLE_MSG";
    public static final String TRIGGER_BREEDER_ROLE_TITLE = "TRIGGER_BREEDER_ROLE_TITLE";
    public static final String TRIGGER_BREEDER_ROLE_MSG = "TRIGGER_BREEDER_ROLE_MSG";
    public static final String TRIGGER_BRUTE_ROLE_TITLE = "TRIGGER_BRUTE_ROLE_TITLE";
    public static final String TRIGGER_BRUTE_ROLE_MSG = "TRIGGER_BRUTE_ROLE_MSG";
    public static final String TRIGGER_SPREAD_ABILITY_TITLE = "TRIGGER_SPREAD_ABILITY_TITLE";
    public static final String TRIGGER_SPREAD_ABILITY_MSG = "TRIGGER_SPREAD_ABILITY_MSG";
    public static final String TRIGGER_SCOUT_ROLE_TITLE = "TRIGGER_SCOUT_ROLE_TITLE";
    public static final String TRIGGER_SCOUT_ROLE_MSG = "TRIGGER_SCOUT_ROLE_MSG";
    public static final String TRIGGER_POLICE_ROLE_TITLE = "TRIGGER_POLICE_ROLE_TITLE";
    public static final String TRIGGER_POLICE_ROLE_MSG = "TRIGGER_POLICE_ROLE_MSG";
    public static final String TRIGGER_PARASITIC_MITE_TITLE = "TRIGGER_PARASITIC_MITE_TITLE";
    public static final String TRIGGER_PARASITIC_MITE_MSG = "TRIGGER_PARASITIC_MITE_MSG";
    public static final String TRIGGER_MASS_FLIGHT_TITLE = "TRIGGER_MASS_FLIGHT_TITLE";
    public static final String TRIGGER_MASS_FLIGHT_MSG = "TRIGGER_MASS_FLIGHT_MSG";
    public static final String TRIGGER_DYNASTY_ABILITY_TITLE = "TRIGGER_DYNASTY_ABILITY_TITLE";
    public static final String TRIGGER_DYNASTY_ABILITY_MSG = "TRIGGER_DYNASTY_ABILITY_MSG";
    public static final String TRIGGER_TRADE_ABILITY_TITLE = "TRIGGER_TRADE_ABILITY_TITLE";
    public static final String TRIGGER_TRADE_ABILITY_MSG = "TRIGGER_TRADE_ABILITY_MSG";
    public static final String TRIGGER_MANAGEMENT_ABILITY_TITLE = "TRIGGER_MANAGEMENT_ABILITY_TITLE";
    public static final String TRIGGER_MANAGEMENT_ABILITY_MSG = "TRIGGER_MANAGEMENT_ABILITY_MSG";
    public static final String TRIGGER_SPREAD_2_ABILITY_TITLE = "TRIGGER_SPREAD_2_ABILITY_TITLE";
    public static final String TRIGGER_SPREAD_2_ABILITY_MSG = "TRIGGER_SPREAD_2_ABILITY_MSG";
    public static final String TRIGGER_AUTOMATION_ABILITY_TITLE = "TRIGGER_AUTOMATION_ABILITY_TITLE";
    public static final String TRIGGER_AUTOMATION_ABILITY_MSG = "TRIGGER_AUTOMATION_ABILITY_MSG";
    public static final String TRIGGER_BILATERAL_TRADE_TITLE = "TRIGGER_BILATERAL_TRADE_TITLE";
    public static final String TRIGGER_BILATERAL_TRADE_MSG = "TRIGGER_BILATERAL_TRADE_MSG";
    public static final String TRIGGER_COURIER_ROLE_TITLE = "TRIGGER_COURIER_ROLE_TITLE";
    public static final String TRIGGER_COURIER_ROLE_MSG = "TRIGGER_COURIER_ROLE_MSG";
    public static final String TRIGGER_BORER_ROLE_TITLE = "TRIGGER_BORER_ROLE_TITLE";
    public static final String TRIGGER_BORER_ROLE_MSG = "TRIGGER_BORER_ROLE_MSG";
    public static final String TRIGGER_ASSIMILATION_ABILITY_TITLE = "TRIGGER_ASSIMILATION_ABILITY_TITLE";
    public static final String TRIGGER_ASSIMILATION_ABILITY_MSG = "TRIGGER_ASSIMILATION_ABILITY_MSG";
    public static final String TRIGGER_OPERATIONS_ABILITY_TITLE = "TRIGGER_OPERATIONS_ABILITY_TITLE";
    public static final String TRIGGER_OPERATIONS_ABILITY_MSG = "TRIGGER_OPERATIONS_ABILITY_MSG";
}
