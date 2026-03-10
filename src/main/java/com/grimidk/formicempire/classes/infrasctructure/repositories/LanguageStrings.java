package com.grimidk.formicempire.classes.infrasctructure.repositories;

public final class LanguageStrings {
    private LanguageStrings() {}

    // --- UI General ---
    public static final String UI_APP_TITLE = "Formic Empire";
    public static final String UI_BACK_TO_GAME = "Back to Game";
    public static final String UI_BACK_TO_MENU = "Quit to Main Menu";
    public static final String UI_MENU = "Menu";
    public static final String UI_SETTINGS = "Settings";
    public static final String UI_TUTORIAL = "Show Tutorial";
    public static final String UI_PAUSE = "Pause";
    public static final String UI_PLAY = "Play";
    public static final String UI_SPEED_UP = "Speed+";
    public static final String UI_SPEED_DOWN = "Speed-";
    public static final String UI_TICK_PREFIX = "Tick: ";
    public static final String UI_MS_SUFFIX = "ms";
    public static final String UI_PAUSED_TICK = "PAUSED";
    public static final String UI_NOT_STARTED = "Game not started";
    public static final String UI_STARTING = "Starting game...";
    public static final String UI_RUNNING = "Running";
    public static final String UI_ERROR_LOADING = "Error loading game!";
    public static final String UI_LOADING_WAIT = "Loading game, please wait...";
    public static final String UI_CLOSE = "Close";
    public static final String UI_SAVE = "Save";
    public static final String UI_DELETE = "Delete";
    public static final String UI_CANCEL = "Cancel";
    public static final String UI_CONFIRM = "Confirm";
    public static final String UI_LEVEL = "Level";
    public static final String UI_TOTAL = "Total";
    public static final String UI_UPGRADE = "Upgrade";
    public static final String UI_LOCKED = "Locked";
    public static final String UI_UNLOCKED = "Unlocked";
    public static final String UI_COST = "Cost";
    public static final String UI_REQUIREMENTS = "Requirements";
    public static final String UI_BUY = "Buy";
    public static final String UI_BUILD = "Build";
    public static final String UI_BEGIN = "Begin";
    public static final String UI_TRIGGER = "Trigger";
    public static final String UI_ENABLED = "ENABLED";
    public static final String UI_DISABLED = "Disabled";
    public static final String UI_HEALTHY = "Healthy";
    public static final String UI_MISSING = "MISSING";
    public static final String UI_COMBINED = "Combined";
    public static final String UI_SUMMARY = "Summary";
    public static final String UI_MAX = "Max";
    public static final String UI_TRANSIT = "Transit";
    public static final String UI_RETURNING = "Returning";
    public static final String UI_MODIFIED = " [MODIFIED]";
    public static final String UI_BILATERAL = "(Bilateral)";
    public static final String UI_ESTABLISH = "Establish";
    public static final String UI_MANAGE = "Manage";
    public static final String UI_EDIT = "Edit";
    public static final String UI_VIEW = "View";
    public static final String UI_MATURING = "Maturing...";
    public static final String UI_BACK = "Back";
    public static final String UI_CREATE = "Create";
    public static final String UI_LOAD = "Load";
    public static final String UI_HELP = "Help";
    public static final String UI_QUIT = "Quit";
    public static final String UI_ERROR = "Error";

    // --- Panels ---
    public static final String PANEL_ALERTS = "Alerts";
    public static final String PANEL_NO_ALERTS = "No alerts";
    public static final String PANEL_TIME = "Time";
    public static final String PANEL_WORLD = "World";
    public static final String PANEL_COLONY = "Colony";
    public static final String PANEL_RESOURCES = "Resources";
    public static final String PANEL_ANTS = "Ants";
    public static final String PANEL_POPULATION = "Population";
    public static final String PANEL_STATS = "Statistics";
    public static final String PANEL_COLONY_STATS = "Colony Stats";
    public static final String PANEL_NO_COLONY = "No Colony Detected";
    public static final String PANEL_TRADE = "Trade";

    // --- Menu Items ---
    public static final String MENU_WORLD_MAP = "World Map (M)";
    public static final String MENU_STATS = "Colony Statistics (X)";
    public static final String MENU_ROLES = "Manage Roles (Q/W/E/R/T)";
    public static final String MENU_HATCH_RATES = "Manage Hatch Rates (P)";
    public static final String MENU_RESEARCH = "Research (Y)";
    public static final String MENU_BUILD = "Build (U)";
    public static final String MENU_ASSIMILATION = "Assimilation (I)";
    public static final String MENU_SYNERGY = "Synergies (O)";
    public static final String MENU_ABILITIES = "Abilities (C)";
    public static final String MENU_DYNASTY = "Dynasty (A)";
    public static final String MENU_TRADE = "Trade Routes (S)";
    public static final String MENU_TOGGLE_VIEW = "Toggle View (Z)";

    // --- Colony Labels ---
    public static final String COLONY_RANK = "Colony Rank";
    public static final String COLONY_TOTAL_RESOURCES = "Total resources: %d";
    public static final String COLONY_TOTAL_ANTS = "Total ants: %d";
    public static final String COLONY_QUEENS = "Queens: %d / %d";
    public static final String COLONY_CONSUMPTION = "Consumption: %d/day";
    public static final String COLONY_MAX_FOOD_PROD = "Max Food Prod: %d/day";
    public static final String COLONY_NET_FOOD = "Net Food: %d/day";
    public static final String COLONY_LAYING_RATE = "Laying Rate: %d/day";
    public static final String COLONY_NURSE_COVERAGE = "Nurse Coverage: %d/%d";
    public static final String COLONY_GRAVE_CLEANING = "Grave Cleaning: %d/%d";
    public static final String COLONY_APHIDS = "Aphids: %d/%d";
    public static final String COLONY_PARASITES = "Parasites: %s";
    public static final String COLONY_POLICING = "Policing: %d";
    public static final String COLONY_DETECTION_RATE = "Detection rate: ~%d/day";
    public static final String COLONY_RESEARCH = "Research: %d";
    public static final String COLONY_RESEARCH_RATE = "Research Rate: %d/day";
    public static final String COLONY_JUVENILE_ANTS = "Juvenile Ants: %d";
    public static final String COLONY_ADULT_ANTS = "Adult Ants: %d";
    
    public static final String TOOLTIP_APHIDS = "Aphids";
    public static final String TOOLTIP_PARASITES = "Parasites in Colony";
    public static final String TOOLTIP_POLICING = "Policing Efficency";
    public static final String TOOLTIP_RESEARCH_POINTS = "Research Points";

    // --- Upgrade Dialog ---
    public static final String DIALOG_UPGRADES_TITLE = "Colony Upgrades";
    public static final String TAB_RESEARCH = "Research";
    public static final String TAB_CONSTRUCTION = "Construction";
    public static final String TAB_ASSIMILATIONS = "Assimilations";
    public static final String TAB_SYNERGIES = "Synergies";
    
    public static final String UPGRADE_RESEARCH_AVAILABLE = "Available Research Points: %d";
    public static final String UPGRADE_NO_RESEARCH = "  No new research available at this time.";
    public static final String UPGRADE_COST_RP = "%d RP";
    public static final String UPGRADE_NOT_ENOUGH_RP = "Not enough Research Points";
    
    public static final String BUILD_NO_CONSTRUCTIONS = "  No new constructions available at this time.";
    public static final String BUILD_UNDER_CONSTRUCTION = "Under Construction: %s";
    public static final String BUILD_PROGRESS_HOURS = "%.1f / %.1f Hours";
    public static final String BUILD_STATUS_BUILDERS = "%d Builders";
    public static final String BUILD_STATUS_CRANES = "%d Cranes";
    public static final String BUILD_STATUS_SPEED = "%s (%.0f%% speed)";
    public static final String BUILD_REQUIREMENT_ERROR = "You need at least 1 Builder or Crane assigned.";
    public static final String BUILD_RESOURCES_ERROR = "Not enough resources.";
    public static final String BUILD_COST_FORMAT = "<html>%d Minerals<br>%d Resin<br>%d Hours (base)</html>";
    
    public static final String ASSIMILATION_CURRENT = "Current Assimilation: %s";
    public static final String ASSIMILATION_NONE = "None";
    public static final String ASSIMILATION_NO_GENOMES = "  No genetic genomes available for assimilation. Defeat other species to unlock.";
    public static final String ASSIMILATION_TARGET = "Target: %d";
    public static final String ASSIMILATION_ACTIVE = "Assimilating: %s";
    public static final String ASSIMILATION_PROGRESS = "%.1f / %d ( %d%% )";
    public static final String ASSIMILATION_INFO = "Assign Researchers to contribute to genetic assimilation.";
    
    public static final String SYNERGY_COMING_SOON = "Synergies - Coming Soon";

    // --- Hatch Rate Dialog ---
    public static final String DIALOG_HATCH_RATES_TITLE = "Manage Pupa Hatch Rates";
    public static final String HATCH_DESC = "Set hatch chance for new ants:";
    public static final String HATCH_TOTAL = "Total: %.1f%%";
    public static final String HATCH_WARNING_TOTAL = "Warning: Total is not 100%%.";
    public static final String HATCH_TOTAL_OK = "Total is 100%";

    // --- Abilities Dialog ---
    public static final String DIALOG_ABILITIES_TITLE = "Colony Operations";
    public static final String ABILITY_RP_LABEL = "Research Points: %d";
    public static final String ABILITY_NO_ABILITIES = "No active abilities unlocked yet.";
    public static final String ABILITY_FORCED_FLIGHT = "Forced Nuptial Flight";
    public static final String ABILITY_FORCED_FLIGHT_DESC = "Spend %d RP to immediately trigger a nuptial flight.\nRequires Drones and Breeder Princesses.";
    public static final String ABILITY_MASS_FLIGHT = "Mass Nuptial Flights";
    public static final String ABILITY_MASS_FLIGHT_DESC = "Spend %d RP to trigger nuptial flights in ALL capable colonies across your dynasty.";
    public static final String ABILITY_ERROR_NOT_ENOUGH_RP = "Not enough Research Points (%d needed).";
    public static final String ABILITY_ERROR_NO_DRONES = "No Drones available in the colony.";
    public static final String ABILITY_ERROR_NO_BREEDERS = "No Breeder Princesses available.";
    public static final String ABILITY_ERROR_LOCATE_COLONY = "Error: Could not locate colony on the world map.";

    // --- Stats Dialog ---
    public static final String DIALOG_STATS_TITLE = "Statistics";
    public static final String STATS_DYNASTY_MODE = "Dynasty Mode";
    public static final String STATS_TAB_GENERAL = "General & World";
    public static final String STATS_TAB_DYNASTY = "Dynasty Overview";
    public static final String STATS_TAB_ECONOMY = "Economy";
    public static final String STATS_TAB_POPULATION = "Population";
    public static final String STATS_TAB_LOCAL_HEX = "Local Hex";
    public static final String STATS_TAB_RATES = "Rates & Jobs";
    public static final String STATS_TAB_UNIT_STATS = "Unit Stats";
    public static final String STATS_TAB_MORTALITY = "Mortality";
    
    public static final String COL_CATEGORY = "Category";
    public static final String COL_PROPERTY = "Property";
    public static final String COL_VALUE = "Value";
    public static final String COL_SCOPE = "Scope";
    public static final String COL_METRIC = "Metric";
    public static final String COL_RESOURCE = "Resource";
    public static final String COL_CURRENT = "Current";
    public static final String COL_CAPACITY = "Capacity";
    public static final String COL_SOURCES = "Sources";
    public static final String COL_PROD_DAY = "Prod/Day";
    public static final String COL_CONS_DAY = "Cons/Day";
    public static final String COL_NET = "Net";
    public static final String COL_TYPE = "Type";
    public static final String COL_ROLE = "Role";
    public static final String COL_COUNT = "Count";
    public static final String COL_ACTIVITY = "Activity";
    public static final String COL_ASSIGNED = "Assigned";
    public static final String COL_RATE_CAP = "Rate/Capacity";
    public static final String COL_COV_OUT = "Coverage/Output";
    public static final String COL_STAT = "Stat";
    public static final String COL_BASE_VAL = "Base Value";
    public static final String COL_DESCRIPTION = "Description";
    public static final String COL_CAUSE = "Cause";
    public static final String COL_TOTAL = "Total";
    
    public static final String STAT_STATUS_NO_HEX = "No Active Hex";
    public static final String STAT_COORDS = "Coordinates (Q, R)";
    public static final String STAT_LOCAL_WEATHER = "Local Weather";
    public static final String STAT_USING_GLOBAL = "Using Global";
    public static final String STAT_BASE_TEMP = "Base Temp";
    public static final String STAT_HUMIDITY_LEVEL = "Humidity Level";
    public static final String STAT_ABUNDANCES = "Abundances";
    public static final String STAT_NEIGHBORS = "Neighbors";
    public static final String STAT_EDGE_WORLD = "Edge of World";
    public static final String STAT_UNKNOWN = "Unknown";
    public static final String STAT_DYNASTY = "Dynasty";
    public static final String STAT_GLOBAL_POP = "Global Population";
    public static final String STAT_GLOBAL_QUEENS = "Global Queens";
    public static final String STAT_SPECIES_SCIENTIFIC = "Species (Scientific)";
    public static final String STAT_AGE = "Age";
    public static final String STAT_DAYS_SUFFIX = " days";
    public static final String STAT_QUEEN_STATUS = "Queen Status";
    public static final String STAT_AUTOMATION = "Automation";
    public static final String STAT_AUTO_BUILD = "Auto-Build";
    public static final String STAT_DATE_TIME = "Date & Time";
    public static final String STAT_TIME_DAY = "Time of Day";
    public static final String STAT_MOON_PHASE = "Moon Phase";
    public static final String STAT_ENVIRONMENT = "Environment";
    public static final String STAT_NO_DYNASTY = "No Dynasty Linked";
    public static final String STAT_CAPITAL = "Capital";
    public static final String STAT_TOTAL_COLONIES = "Total Colonies";
    public static final String STAT_NUPTIAL_FLIGHTS = "Total Nuptial Flights";
    public static final String STAT_DEFEATED_SPECIES = "Defeated Species";
    public static final String STAT_UPGRADES_RES = "Upgrades Researched";
    public static final String STAT_ASSIM_COMP = "Completed Assimilations";
    public static final String STAT_BUILDINGS_BUILT = "Total Buildings Built";
    public static final String STAT_STORED_POINTS = "Stored Points";
    public static final String STAT_GLOBAL_RATE = "Global Rate";
    public static final String STAT_PTS_DAY_FORMAT = "+%d pts/day";
    public static final String STAT_MORTALITY = "Mortality";
    public static final String STAT_GLOBAL_DEATHS = "Global Deaths";
    public static final String STAT_TOTAL_FOOD = "Total Food";
    public static final String STAT_ADULTS = "Total Adults";
    public static final String STAT_JUVENILES = "Total Juveniles";
    public static final String STAT_COLONY_TOTAL = "Colony Total";
    public static final String STAT_DYNASTY_TOTAL = "Dynasty Total";
    public static final String STAT_NO_DEATHS = "No deaths recorded";

    // --- Unit Stats ---
    public static final String UNIT_STAT_HEALTH = "Base Health";
    public static final String UNIT_STAT_HEALTH_DESC = "Hitpoints per worker";
    public static final String UNIT_STAT_DEFENSE = "Base Defense";
    public static final String UNIT_STAT_DEFENSE_DESC = "Percent damage reduction";
    public static final String UNIT_STAT_ATTACK = "Base Attack";
    public static final String UNIT_STAT_ATTACK_DESC = "Damage per hit";
    public static final String UNIT_STAT_SPEED = "Movement Speed";
    public static final String UNIT_STAT_SPEED_DESC = "Pixels per tick";
    public static final String UNIT_STAT_REGEN = "Regeneration";
    public static final String UNIT_STAT_REGEN_DESC = "Health recovered per tick";
    public static final String UNIT_STAT_TEMP_RES = "Temp. Resist";
    public static final String UNIT_STAT_TEMP_RES_DESC = "Degrees tolerance";
    public static final String UNIT_STAT_CONSUMPTION = "Base Consumption";
    public static final String UNIT_STAT_CONSUMPTION_DESC = "Food units per day per ant";
    public static final String UNIT_STAT_DETECTION = "Parasite Detection";
    public static final String UNIT_STAT_DETECTION_DESC = "Chance to find parasites";
    public static final String UNIT_STAT_IMMUNITY = "Immunity";
    public static final String UNIT_STAT_IMMUNITY_DESC = "Contamination mitigation";

    // --- Role Management Dialog ---
    public static final String DIALOG_ROLES_TITLE = "Manage Ant Roles";
    public static final String ROLE_TOTAL_PREFIX = "Total %ss: %d";
    public static final String ROLE_ASSIGNED_PREFIX = "Total Assigned: %d";
    public static final String ROLE_UNASSIGNED_PREFIX = "Unassigned: %d";
    public static final String ROLE_ERROR_OVER_ASSIGNED = "You have assigned more roles than you have ants.";

    // --- Map Dialog ---
    public static final String DIALOG_MAP_TITLE = "World Map";
    public static final String MAP_HOME_BUTTON = "Center on Home";
    public static final String MAP_LEGEND_TITLE = "Dynasty Legend";
    public static final String MAP_CLICK_VIEW_CAPITAL = "Click to view %s Capital";
    public static final String MAP_YOU_PLAYER = " (You)";
    public static final String MAP_POPULATION_FORMAT = "Population: %d";
    public static final String MAP_TOOLTIP_BIOME = "<b>Biome:</b> ";
    public static final String MAP_TOOLTIP_RANK = "<br><b>Rank:</b> ";
    public static final String MAP_TOOLTIP_SPECIES = "<br><b>Species:</b> ";
    public static final String MAP_TOOLTIP_DYNASTY = "<br><b>Dynasty:</b> ";
    public static final String MAP_TOOLTIP_DYNASTY_RANK = "<br><b>Dynasty Rank:</b> ";
    public static final String MAP_TOOLTIP_EMPTY = "<br><i>Empty</i>";

    // --- Dynasty Management ---
    public static final String DIALOG_DYNASTY_TITLE = "Dynasty Management";
    public static final String TAB_OVERVIEW = "Overview";
    public static final String TAB_LOGISTICS = "Logistics";
    public static final String DYNASTY_MANAGE_LOGISTICS = "Managing Logistics for: ";
    public static final String DYNASTY_DIRECTION = "Direction";
    public static final String DYNASTY_NEIGHBOR = "Neighbor Colony";
    public static final String DYNASTY_TUNNEL_STATUS = "Tunnel Status";
    public static final String DYNASTY_OUTGOING = "Outgoing Route";
    public static final String DYNASTY_INCOMING = "Incoming Route";
    public static final String DYNASTY_ACTIONS = "Actions";
    public static final String DYNASTY_TRANSIT_FORMAT = "%s (%dh)%s";
    public static final String DYNASTY_BUILD_TUNNEL = "Build Tunnel";
    public static final String DYNASTY_TUNNEL_Sponsoring = "This colony is already sponsoring a tunnel project.";
    public static final String DYNASTY_BUILT = "Built";
    public static final String DYNASTY_PROGRESS_PERCENT = "%.0f%%";
    public static final String DYNASTY_ERROR_NO_ENGINEERS = "No Engineers or Borers assigned to progress construction!";
    public static final String DYNASTY_ERROR_LABOR_REQUIRED = "Labor Required";
    public static final String DYNASTY_ERROR_ASSIGN_BORERS = "You must assign at least one Engineer or Tunnel Borer to this colony to start tunnel construction.";
    public static final String DYNASTY_MANAGE_TRADE_TITLE = "Trade Management";
    public static final String DYNASTY_MANAGE_TRADE_MSG = "Manage trade route to %s";
    public static final String DYNASTY_MODIFY = "Modify";
    public static final String DYNASTY_CANCEL_ROUTE = "Cancel Route";
    public static final String DYNASTY_ERROR_LOCATE = "Internal error: could not locate colonies on map.";
    public static final String DYNASTY_SORT_BY = "Sort by:";
    public static final String DYNASTY_DEFAULT_AUTO_BUILD = "Default Auto-Build";
    public static final String DYNASTY_DEFAULT_AUTO_BUILD_TOOLTIP = "Automatically enable Auto-Build for all new colonies established by this dynasty.";
    public static final String DYNASTY_DEFAULT_AUTOMATION = "Default Automation";
    public static final String DYNASTY_DEFAULT_AUTOMATION_TOOLTIP = "Automatically enable Automation for all new colonies established by this dynasty.";
    public static final String DYNASTY_SATELLITE = "Satellite";
    public static final String DYNASTY_STATUS = "Actions/Status";
    public static final String DYNASTY_7_DAYS = "%d / 7 Days";
    public static final String DYNASTY_RENAME_TITLE = "Enter new name for %s";

    // --- Trade Creation ---
    public static final String TRADE_ESTABLISH_TITLE = "Establish Trade Route";
    public static final String TRADE_MODIFY_TITLE = "Modify Trade Route";
    public static final String TRADE_CAPACITY_FORMAT = "Capacity: %.1f / %.1f";
    public static final String TRADE_SPEED_FORMAT = "Transit Speed: %.2fx";
    public static final String TRADE_TIME_FORMAT = "Travel Time: %dh";
    public static final String TRADE_SECURITY_FORMAT = "Security: %.1f%%";
    public static final String TRADE_ROUTE_PREFIX = "Route: %s -> %s";
    public static final String TRADE_CARGO_LOAD = "Cargo (Load)";
    public static final String TRADE_CARGO_RETURN = "Cargo (Return Load)";
    public static final String TRADE_PERSONNEL = "Personnel (Assigned Logistics Roles)";
    public static final String TRADE_COURIERS = "Couriers";
    public static final String TRADE_TRANSPORTS = "Transports";
    public static final String TRADE_ESCORTS = "Escorts";
    public static final String TRADE_FLYERS = "Flyers";
    public static final String TRADE_METHOD = "Method:";
    public static final String TRADE_RECURRENT = "Recurrent Route";
    public static final String TRADE_BILATERAL = "Bilateral Trade";
    public static final String TRADE_CONFIRM = "Confirm Trade Route";
    public static final String TRADE_UPDATE = "Update Trade Route";
    public static final String TRADE_OPTIMIZE = "Bilateral Optimization";
    public static final String TRADE_ERROR_OVER_CAP = "Cargo exceeds transport capacity!";
    public static final String TRADE_ERROR_NO_ANTS = "No ants assigned to transport!";
    public static final String TRADE_ERROR_NO_LOAD = "No resources selected for trade!";
    public static final String TRADE_OPTIMIZE_MSG = "Merge incoming route from %s into this bilateral convoy?\nThe other convoy will be cancelled and this one will handle both ways.";
    public static final String TRADE_ERROR_EMPTY = "Cargo cannot be empty.";
    public static final String TRADE_ERROR_NO_PERSONNEL = "Must assign at least one ant for transport.";
    public static final String TRADE_ERROR_NO_METHOD = "No valid logistics method available.";
    public static final String TRADE_QUEUED_MSG = "Modifications queued. They will apply once the convoy returns to home base.";
    public static final String TRADE_ERROR_START = "Failed to start trade trip. Check colony logs for details.";

    // --- Game States & Deaths ---
    public static final String DEATH_OPTIONS_RELOAD = "Reload Last Save";
    public static final String DEATH_OPTIONS_MENU = "Go to Main Menu";
    public static final String DEATH_TITLE = "Your Colony Has Perished";
    public static final String DEATH_MESSAGE = "Your last queen has died, and the colony cannot continue.\nWhat would you like to do?";
    public static final String DEATH_LOAD_FAILED_NEW_GAME = "This was a new game with no save file. Returning to main menu.";
    public static final String DEATH_LOAD_FAILED_TITLE = "Load Failed";
    public static final String DEATH_LOAD_FAILED_NO_AUTOSAVE = "No autosave found for this slot. Returning to main menu.";

    // --- Save Select ---
    public static final String SAVE_EMPTY_SLOT = "Empty slot";
    public static final String SAVE_DAYS_FORMAT = "%s — %d days";
    public static final String SAVE_ENTER_NAME = "Enter save name:";
    public static final String SAVE_CREATE_TITLE = "Create Save";
    public static final String SAVE_ERROR_CREATE = "Failed to create new save file.";
    public static final String SAVE_DELETE_CONFIRM = "Delete save in slot %d?";
    public static final String SAVE_DELETE_TITLE = "Delete Save";
    public static final String SAVE_DELETE_ERROR = "Failed to delete save (file may not exist).";

    // --- Settings ---
    public static final String SETTINGS_LANGUAGE = "Language:";
    public static final String SETTINGS_SCREEN_SIZE = "Screen Size:";
    public static final String SETTINGS_FULLSCREEN = "Full Screen:";
    public static final String SETTINGS_AUTOSAVE = "Autosave Frequency:";
    public static final String SETTINGS_TURBO = "Allow Turbo Mode:";
    public static final String SETTINGS_EVERY_MONTH = "Every Month";
    public static final String SETTINGS_EVERY_3_MONTHS = "Every 3 Months";
    public static final String SETTINGS_EVERY_6_MONTHS = "Every 6 Months";
    public static final String SETTINGS_EVERY_YEAR = "Every Year (12 Months)";
    public static final String SETTINGS_SAVE_APPLY = "Save & Apply";
    public static final String SETTINGS_SAVED_MSG = "Settings saved and applied.";

    // --- World & Environment ---
    public static final String WORLD_BIOME_PREFIX = "Biome: ";
    public static final String WORLD_TEMP_PREFIX = "Temp: ";
    public static final String WORLD_HUMIDITY_PREFIX = "Humidity: ";
    public static final String WORLD_NA = "N/A";

    // --- Biomes ---
    public static final String BIOME_PLAINS = "Plains";
    public static final String BIOME_FOREST = "Forest";
    public static final String BIOME_JUNGLE = "Jungle";
    public static final String BIOME_SWAMP = "Swamp";
    public static final String BIOME_URBAN = "Urban";
    public static final String BIOME_TUNDRA = "Tundra";
    public static final String BIOME_TAIGA = "Taiga";
    public static final String BIOME_DESERT = "Desert";
    public static final String BIOME_MOUNTAIN = "Mountain";
    public static final String BIOME_VOLCANIC = "Volcanic";
    public static final String BIOME_LAKE = "Lake";
    public static final String BIOME_OCEAN = "Ocean";

    // --- Temperatures ---
    public static final String TEMP_FREEZING = "Freezing";
    public static final String TEMP_COLD = "Cold";
    public static final String TEMP_CHILLY = "Chilly";
    public static final String TEMP_GOOD = "Good";
    public static final String TEMP_WARM = "Warm";
    public static final String TEMP_HOT = "Hot";
    public static final String TEMP_BURNING = "Burning";

    // --- Humidities ---
    public static final String HUMID_ARID = "Arid";
    public static final String HUMID_DRY = "Dry";
    public static final String HUMID_NORMAL = "Normal";
    public static final String HUMID_HUMID = "Humid";
    public static final String HUMID_MOIST = "Moist";
    public static final String HUMID_SATURATED = "Saturated";

    // --- Resources ---
    public static final String RESOURCE_PLANT = "Plant Matter";
    public static final String RESOURCE_FUNGI = "Fungi Matter";
    public static final String RESOURCE_MEAT = "Animal Matter";
    public static final String RESOURCE_WATER = "Water";
    public static final String RESOURCE_SYRUP = "Syrup";
    public static final String RESOURCE_RESIN = "Resin";
    public static final String RESOURCE_ROCK = "Mineral";

    // --- Times of Day ---
    public static final String TIME_DAY = "Daytime";
    public static final String TIME_DUSK = "Dusk";
    public static final String TIME_NIGHT = "Nightime";
    public static final String TIME_DAWN = "Dawn";
    public static final String TIME_SOLAR_ECLIPSE = "Solar Eclipse";
    public static final String TIME_LUNAR_ECLIPSE = "Lunar Eclipse";

    // --- Moon Phases ---
    public static final String MOON_NEW = "New Moon";
    public static final String MOON_WAXING_CRESCENT = "Waxing Crescent";
    public static final String MOON_FIRST_QUARTER = "First Quarter";
    public static final String MOON_WAXING_GIBBOUS = "Waxing Gibbous";
    public static final String MOON_FULL = "Full Moon";
    public static final String MOON_WANING_GIBBOUS = "Waning Gibbous";
    public static final String MOON_LAST_QUARTER = "Last Quarter";
    public static final String MOON_WANING_CRESCENT = "Waning Crescent";

    // --- Seasons ---
    public static final String SEASON_SPRING = "Spring";
    public static final String SEASON_SUMMER = "Summer";
    public static final String SEASON_AUTUMN = "Autumn";
    public static final String SEASON_WINTER = "Winter";

    // --- Weather ---
    public static final String WEATHER_CLEAR = "Clear";
    public static final String WEATHER_RAIN = "Rain";
    public static final String WEATHER_SNOW = "Snow";
    public static final String WEATHER_HEAVY_RAIN = "Heavy Rain";
    public static final String WEATHER_THUNDER = "Thunder Storm";
    public static final String WEATHER_HEAVY_SNOW = "Snow Storm";
    public static final String WEATHER_WIND = "Heavy Wind";
    public static final String WEATHER_HEAT = "Heat Wave";
    public static final String WEATHER_FOG = "Fog";
    public static final String WEATHER_FROG = "Frog Rain";
    public static final String WEATHER_BLOOD = "Blood Rain";
    public static final String WEATHER_SAND_STORM = "Sand Storm";
    public static final String WEATHER_PYROCLASTIC_FOG = "Pyroclastic Fog";
    public static final String WEATHER_ACID_RAIN = "Acid Rain";

    // --- Ant Status ---
    public static final String STATUS_ALIVE = "Alive";
    public static final String STATUS_DEAD = "Dead";
    public static final String STATUS_ZOMBIFIED = "Zombified";

    // --- Move Status ---
    public static final String MOVE_STATIC = "Static";
    public static final String MOVE_WANDER = "Wandering";
    public static final String MOVE_MARCH = "Marching";
    public static final String MOVE_SPEED = "Speed Marching";
    public static final String MOVE_FLY = "Flying";
    public static final String MOVE_PATROL = "Patrolling";

    // --- Bug Types ---
    public static final String BUG_ANT = "Ant";
    public static final String BUG_APHID = "Aphid";
    public static final String BUG_PARASITE = "Ant Parasite";

    // --- Ant Types ---
    public static final String TYPE_EGG = "Egg";
    public static final String TYPE_LARVA = "Larva";
    public static final String TYPE_PUPA = "Pupa";
    public static final String TYPE_WORKER = "Worker";
    public static final String TYPE_SOLDIER = "Soldier";
    public static final String TYPE_MAJOR = "Major";
    public static final String TYPE_DRONE = "Drone";
    public static final String TYPE_PRINCESS = "Princess";
    public static final String TYPE_QUEEN = "Queen";
    public static final String TYPE_DEAD = "Dead";
    public static final String TYPE_ZOMBIE = "Zombie";

    // --- Ant Roles ---
    public static final String ROLE_FORAGER = "Forager";
    public static final String ROLE_NURSE = "Nurse";
    public static final String ROLE_BUILDER = "Builder";
    public static final String ROLE_SCOUT = "Scout";
    public static final String ROLE_FARMER = "Farmer";
    public static final String ROLE_RANCHER = "Rancher";
    public static final String ROLE_GRAVER = "Grave-Keeper";
    public static final String ROLE_MINER = "Miner";
    public static final String ROLE_COURIER = "Courier";
    public static final String ROLE_POTTER = "Portable-Feeder";
    public static final String ROLE_GUARD = "Guard";
    public static final String ROLE_WARRIOR = "Warrior";
    public static final String ROLE_DEFENDER = "Defender";
    public static final String ROLE_POLICE = "Parasite-Police";
    public static final String ROLE_BOMBER = "Bomber";
    public static final String ROLE_HUNTER = "Hunter";
    public static final String ROLE_BRUTE = "Brute";
    public static final String ROLE_CARRIER = "Troop-Carrier";
    public static final String ROLE_ARTILLERY = "Artillery";
    public static final String ROLE_SIEGE = "Siege-Engine";
    public static final String ROLE_BORER = "Tunnel Borer";
    public static final String ROLE_DRONE = "Drone";
    public static final String ROLE_BREEDER = "Breeder";
    public static final String ROLE_DIPLOMAT = "Diplomat";
    public static final String ROLE_LAYER = "Egg-Layer";
    public static final String ROLE_RESEARCHER = "Researcher";
    public static final String ROLE_MILITIA = "Militia Auxiliary";
    public static final String ROLE_CATCHER = "Catcher";
    public static final String ROLE_CRANE = "Construction Crane";
    public static final String ROLE_TRANSPORT = "Resource Transport";
    public static final String ROLE_ASSISTANT = "Lab Assistant";
    public static final String ROLE_ESCORT = "Convoy Escort";
    public static final String ROLE_ENGINEER = "Tunnel Engineer";
    public static final String ROLE_SKYTRANS = "Sky Transport";

    // --- Colony Ranks ---
    public static final String RANK_ANT = "Ant";
    public static final String RANK_COLONY = "Ant Colony";
    public static final String RANK_COUNTY = "Ant County";
    public static final String RANK_DUCHY = "Ant Duchy";
    public static final String RANK_KINGDOM = "Ant Queendom";
    public static final String RANK_EMPIRE = "Ant Empire";
    public static final String RANK_SUPER = "Ant Super Colony";
    public static final String RANK_ULTRA = "Ant Ultra Colony";
    public static final String RANK_HYPER = "Ant Hyper Colony";
    public static final String RANK_MEGA = "Ant Mega Colony";
    public static final String RANK_ULTIMATE = "Ant Ultimate Colony";
    public static final String RANK_SUPREME = "Ant Supreme Colony";
    public static final String RANK_GIGA = "Ant Giga Colony";

    // --- Species ---
    public static final String SPECIES_OMNI = "Omni Ant";
    public static final String SPECIES_OMNI_SCIENTIFIC = "Omniformica Grimunknowni";
    public static final String SPECIES_LEAF = "Leaf-Cutter Ant";
    public static final String SPECIES_LEAF_SCIENTIFIC = "Atta Cephalotes";
    public static final String SPECIES_PHARAOH = "Pharaoh Ant";
    public static final String SPECIES_PHARAOH_SCIENTIFIC = "Monomorium Pharaonis";
    public static final String SPECIES_MARAUDER = "Marauder Ant";
    public static final String SPECIES_MARAUDER_SCIENTIFIC = "Carebara Diversa";

    // --- Trade Methods ---
    public static final String METHOD_LAND = "Land";
    public static final String METHOD_AIR = "Air";
    public static final String METHOD_SEA = "Sea";
    public static final String METHOD_TUNNEL = "Tunnel";

    // --- Event Messages ---
    public static final String EVENT_ECLIPSE_NUPTIAL = "The Eclipse has triggered a spontaneous Nuptial Flight!";
}
