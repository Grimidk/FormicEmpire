package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.world.WarStagePhase;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Savefile implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // --- Global Save Data ---
    private final int id; 
    private final String name; 
    private long timestamp;
    private int playTime;
    private int minute;
    private int hour;
    private int day;
    private int month;
    private int year;
    private int worldRadius;
    private String playerDynastyTitleKey;
    private int playerDynastyTitleId;
    
    // --- Root Summary Data ---
    private int colonyId;
    private String colonyName;
    private int totalAnts;
    private int deadAnts;
    private int workers;
    private int queens;
    
    // --- Resource Summary Data ---
    private int plants;
    private int mushrooms;
    private int protein;
    private int water;
    private int syrups;
    private int resins;
    private int minerals;

    // --- Collections ---
    private List<SavedHex> worldHexes;
    private List<SavedColony> colonies;
    private List<SavedDynasty> dynastys;
    private List<SavedTrade> trades;
    private List<SavedWar> wars;
    private List<SavedWorldHistoryEvent> worldHistory;

    public Savefile(int id, String name) {
        this.id = id;
        this.name = name;
        this.timestamp = System.currentTimeMillis();
        this.worldHexes = new ArrayList<>();
        this.colonies = new ArrayList<>();
        this.dynastys = new ArrayList<>();
        this.trades = new ArrayList<>();
        this.wars = new ArrayList<>();
        this.worldHistory = new ArrayList<>();
        this.minute = 0;
        this.hour = 0;
        this.day = 1;
        this.month = 1;
        this.year = 0; 
        this.worldRadius = 8;
        this.playerDynastyTitleKey = LanguageStrings.DYNASTY_TITLE_DYNASTY;
    }

    public static class SavedDynasty implements Serializable {
        private static final long serialVersionUID = 1L;
        public int id;
        public String name;
        public String themeBase;
        public int titleId;
        public String titleKey;
        public boolean isPlayer;
        public boolean wildDynasty;
        public boolean isDefeated;
        public String rankName;
        public int speciesId;
        public int researchPoints;
        public int totalNuptialFlights;
        public int diplomatsSentTotal;
        public boolean defaultAutomationEnabled;
        public boolean defaultAutoBuildEnabled;
        public boolean autoDiplomacyEnabled;
        public boolean defaultAutoTunnelsEnabled;
        public String pactRequestIncomingPolicy = "MANUAL";
        public int lastIncomingPactRequestWorldDay = -1;
        public Map<String, Integer> diplomatSupportToDynasty = new HashMap<>();
        public List<Integer> unlockedUpgradeIds = new ArrayList<>();
        public List<Integer> absorbedDynastyIds = new ArrayList<>();
        public List<Integer> defeatedSpeciesIds = new ArrayList<>();
        public List<Integer> completedAssimilationIds = new ArrayList<>();
        public int currentAssimilationId = -1;
        public double assimilationProgress = 0;
        public int capitalColonyId = -1;
        public double geneticIntegrity = 100.0;
        public int militaryPower;
        public Map<String, Integer> deathStatistics = new HashMap<>();
        public Map<String, Integer> diplomaticReputations = new HashMap<>();
        public Map<String, String> diplomaticModifierKeys = new HashMap<>();
        public Map<String, List<String>> diplomaticModifierKeySets = new HashMap<>();
        public Map<String, Map<String, Integer>> diplomaticModifierRemainingDays = new HashMap<>();
        public List<Integer> crossDynastyTradeRepGrantedIds = new ArrayList<>();
        public List<Integer> pendingPactRequestFromIds = new ArrayList<>();
        public List<Integer> pendingWarDeclarationFromIds = new ArrayList<>();
        public List<Integer> activeWarDynastyIds = new ArrayList<>();
        public Map<String, Integer> pactBrokenAtWorldMonth = new HashMap<>();
        public Map<String, Integer> pactRequestDeclinedAtWorldMonth = new HashMap<>();
        public Map<String, Integer> tradeRequestDeclinedAtWorldMonth = new HashMap<>();
        public Map<String, Integer> wasAtWarPeacedAtWorldMonth = new HashMap<>();
        public Map<String, Integer> geneticExchangeGrantedAtWorldMonth = new HashMap<>();
        public List<SavedCrossDynastyTradeProposal> pendingTradeProposals = new ArrayList<>();
        public int forcedFlightCooldownDays;
        public List<SavedTunnel> tunnels = new ArrayList<>();
        public int originDynastyId;
        public int activeRebellionDynastyId;
        public int pendingRebellionResponseFromId;
        public int integrationTargetDynastyId;
        public double integrationProgressDays;
        public double integrationProgressMonths;
        public boolean integrationDiplomatsManual;
    }

    public static class SavedCrossDynastyTradeProposal implements Serializable {
        private static final long serialVersionUID = 1L;

        public int fromDynastyId;
        public int originColonyId;
        public int destinationColonyId;
        public boolean request;
        public Map<String, Double> loadByResourceId = new HashMap<>();
    }

    public static class SavedColony implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public int id;
        public int dynastyId;
        public String name;
        public String rankName;
        public boolean isPlayer;
        public boolean isCapital;
        public boolean isAutomated;
        public boolean autoBuildEnabled;
        public boolean autoTunnelsEnabled;
        public Map<String, Integer> outgoingColonyDiplomatMissions = new HashMap<>();
        public Map<String, Integer> incomingColonyDiplomatSupport = new HashMap<>();
        public Map<String, Integer> outgoingDynastyDiplomatMissions = new HashMap<>();
        public int age;
        public int daysWithoutQueen;
        public int q; 
        public int r;        
        public float progress;
        public int totalAnts;
        public int deadAnts;
        public int eggs, pupae, larvae, workers, soldiers, majors, drones, princesses, queens;        
        public int plants, mushrooms, protein, water, syrups, resins, minerals;        
        public float hatchRateWorker, hatchRateSoldier, hatchRateMajor, hatchRateDrone, hatchRatePrincess;
        public Map<String, Double> subtypeHatchRatesFlat = new HashMap<>();
        public Map<String, Integer> workerSubtypes = new HashMap<>();
        public Map<String, Integer> soldierSubtypes = new HashMap<>();
        public Map<String, Integer> majorSubtypes = new HashMap<>();
        public Map<String, Integer> princessSubtypes = new HashMap<>();
        public Map<String, Integer> queenSubtypes = new HashMap<>();
        public int aphids, symbioticMites, dermestids, parasiteAnts, parasiticMites;
        public int pheromoneStormMonthsRemaining;
        public int recentlyConqueredMonthsRemaining;
        public int recentlyIntegratedMonthsRemaining;
        public Map<String, Integer> loyaltyModifierRemainingDays = new HashMap<>();
        public int integrationDiplomatsDeployed;
        public int nativeSpeciesId;
        public int creatineDietMonthsRemaining;
        public int totalDeaths;
        public int loyalty = GameNumbers.DEFAULT_COLONY_LOYALTY;
        public int militaryPower;
        public Map<String, Integer> assignedRoleCounts = new HashMap<>();
        public Map<String, Integer> warAssignedRoleCounts = new HashMap<>();
        public Map<String, Integer> localDeathStatistics = new HashMap<>();
        public List<Integer> unlockedBuildingIds = new ArrayList<>();
        public List<SavedResourceSource> savedResourceSources = new ArrayList<>();
    }

    public static class SavedResourceSource implements Serializable {
        private static final long serialVersionUID = 1L;
        public int typeId;
        public int currentQuantity;
        public int initialQuantity;
        public int x;
        public int y;

        public SavedResourceSource(int typeId, int currentQuantity, int initialQuantity, int x, int y) {
            this.typeId = typeId;
            this.currentQuantity = currentQuantity;
            this.initialQuantity = initialQuantity;
            this.x = x;
            this.y = y;
        }
    }
    
    public static class SavedHex implements Serializable {
        private static final long serialVersionUID = 1L;
        public int q;
        public int r;
        public int biomeId;
        public boolean hasColony;
        public int timeOffset;
        public int weatherId;
        public int nonWaterResourceSourcesGenerated;

        public SavedHex(int q, int r, int biomeId, boolean hasColony, int timeOffset, int weatherId) {
            this.q = q;
            this.r = r;
            this.biomeId = biomeId;
            this.hasColony = hasColony;
            this.timeOffset = timeOffset;
            this.weatherId = weatherId;
        }
    }

    public static class SavedTunnel implements Serializable {
        private static final long serialVersionUID = 1L;
        public int qA, rA;
        public int qB, rB;
        public double progress;
        public double totalCost;
        public boolean isComplete;
    }

    public static class SavedWar implements Serializable {
        private static final long serialVersionUID = 1L;
        public int id;
        public int dynastyIdA;
        public int dynastyIdB;
        public int startedWorldMonth;
        public int declaredByDynastyId;
        public String displayName;
        public int militaryPowerAtStartA;
        public int militaryPowerAtStartB;
        public int endedWorldMonth = War.ACTIVE_END_MONTH;
        public int winnerDynastyId;
        public String conclusionKey;
        public int pendingPeaceOfferFromDynastyId;
        public float progressPercent = 50f;
        public int totalStages;
        public int aggressorStagesCaptured;
        public int defenderStagesCaptured;
        public float stageProgress;
        public String stagePhaseKey = WarStagePhase.ACTIVE_CLASH.name();
        public int contestedColonyId;
        public int stageAttackerDynastyId;
        public int deployedActiveAttacker;
        public int deployedActiveDefender;
        public int deployedReserveDefender;
        public int aggressorCapitalColonyId;
        public int defenderCapitalColonyId;
        public int redeployHoursRemaining;
        public int stageStartActiveAggressor;
        public int stageStartActiveDefender;
        public String capturedColonyIds = "";
        public String capturedByDynastyIds = "";
        public String dynastyNameA;
        public String dynastyNameB;
        public String winnerDynastyName;
        public boolean rebellionWar;
    }

    public static class SavedWorldHistoryEvent implements Serializable {
        private static final long serialVersionUID = 1L;
        public int year;
        public int month;
        public int day;
        public int hour;
        public int minute;
        public String type;
        public String messageKey;
        public List<String> args = new ArrayList<>();
        public int relatedDynastyId = -1;
        public int relatedColonyId = -1;
        public int relatedWarId = -1;
    }

    public static class SavedTrade implements Serializable {
        private static final long serialVersionUID = 1L;
        public int qOrigin, rOrigin;
        public int qDest, rDest;
        public Map<Integer, Double> load = new HashMap<>();
        public Map<Integer, Double> returnLoad = new HashMap<>();
        public Map<Integer, Integer> transport = new HashMap<>();
        public boolean isRecurrent;
        public boolean isBilateral;
        public int methodId;
        public boolean isActive;
        public int totalHours;
        public int remainingHours;
        public boolean isReturning;
        
        public boolean hasPendingUpdate;
        public Map<Integer, Double> pendingLoad = new HashMap<>();
        public Map<Integer, Double> pendingReturnLoad = new HashMap<>();
        public Map<Integer, Integer> pendingTransport = new HashMap<>();
        public boolean pendingRecurrent;
        public boolean pendingIsBilateral;
        public int pendingMethodId;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public String getName() { return name; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getPlayTime() { return playTime; }
    public void setPlayTime(int playTime) { this.playTime = playTime; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getWorldRadius() { return worldRadius; }
    public void setWorldRadius(int worldRadius) { this.worldRadius = worldRadius; }

    public String getPlayerDynastyTitleKey() { return playerDynastyTitleKey; }
    public void setPlayerDynastyTitleKey(String playerDynastyTitleKey) {
        this.playerDynastyTitleKey = playerDynastyTitleKey;
    }

    public int getPlayerDynastyTitleId() { return playerDynastyTitleId; }
    public void setPlayerDynastyTitleId(int playerDynastyTitleId) {
        this.playerDynastyTitleId = playerDynastyTitleId;
    }

    public int resolvePlayerDynastyTitleId() {
        if (playerDynastyTitleId > 0) {
            return playerDynastyTitleId;
        }
        return GameConstants.getDynastyTitleByKey(
                playerDynastyTitleKey != null ? playerDynastyTitleKey : LanguageStrings.DYNASTY_TITLE_DYNASTY
        ).getId();
    }
    
    public int getColonyId() { return colonyId; }
    public void setColonyId(int colonyId) { this.colonyId = colonyId; }
    
    public String getColonyName() { return colonyName; }
    public void setColonyName(String colonyName) { this.colonyName = colonyName; }
    
    public int getTotalAnts() { return totalAnts; }
    public void setTotalAnts(int totalAnts) { this.totalAnts = totalAnts; }
    
    public int getDeadAnts() { return deadAnts; }
    public void setDeadAnts(int deadAnts) { this.deadAnts = deadAnts; }
    
    public int getWorkers() { return workers; }
    public void setWorkers(int workers) { this.workers = workers; }
    
    public int getQueens() { return queens; }
    public void setQueens(int queens) { this.queens = queens; }
    
    public int getPlants() { return plants; }
    public void setPlants(int plants) { this.plants = plants; }

    public int getMushrooms() { return mushrooms; }   
    public void setMushrooms(int mushrooms) { this.mushrooms = mushrooms; }   

    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }

    public int getWater() { return water; }
    public void setWater(int water) { this.water = water; }

    public int getSyrups() { return syrups; }
    public void setSyrups(int syrups) { this.syrups = syrups; }

    public int getResins() { return resins; }
    public void setResins(int resins) { this.resins = resins; }

    public int getMinerals() { return minerals; }
    public void setMinerals(int minerals) { this.minerals = minerals; }

    public List<SavedHex> getWorldHexes() { return worldHexes; }
    public void setWorldHexes(List<SavedHex> worldHexes) { this.worldHexes = worldHexes; }

    public List<SavedColony> getColonies() { return colonies; }
    public void setColonies(List<SavedColony> colonies) { this.colonies = colonies; }

    public List<SavedDynasty> getDynastys() { return dynastys; }
    public void setDynastys(List<SavedDynasty> dynastys) { this.dynastys = dynastys; }

    public List<SavedTrade> getTrades() { return trades; }
    public void setTrades(List<SavedTrade> trades) { this.trades = trades; }

    public List<SavedWar> getWars() { return wars; }
    public void setWars(List<SavedWar> wars) { this.wars = wars; }

    public List<SavedWorldHistoryEvent> getWorldHistory() { return worldHistory; }
    public void setWorldHistory(List<SavedWorldHistoryEvent> worldHistory) { this.worldHistory = worldHistory; }
}