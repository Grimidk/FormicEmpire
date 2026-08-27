package com.grimidk.formicempire.classes.entities.dynasty;

import com.grimidk.formicempire.classes.entities.spatial.Room;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList; 
import java.awt.Rectangle;

import com.grimidk.formicempire.classes.entities.services.colony.*;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyIntelligenceService;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.dynasty.colony.ColonyLoyaltyModifier;
import com.grimidk.formicempire.classes.constants.dynasty.Rank;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.critter.Critter;
import com.grimidk.formicempire.classes.entities.spatial.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.audio.SfxService;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;

public class Colony {
    
    // --- Basic Data ---
    private final int id;
    private Dynasty dynasty;
    private String name;
    private boolean isPlayer;
    private Rank rank;
    private boolean isActive;
    private boolean automationEnabled = false; 
    private boolean autoBuildEnabled = false;
    private boolean autoTunnelsEnabled = false;
    private boolean autoLogisticsEnabled = false;
    private boolean isCapital = false;
    private int age;
    private int daysWithoutQueen;
    private int loyalty = GameNumbers.DEFAULT_COLONY_LOYALTY;
    private int militaryPower;
    private int activeMilitaryPower;
    private int reserveMilitaryPower;
    
    // --- Population Data ---
    private final Map<AntType, List<Ant>> antGroups;
    private final List<Ant> deadAnts;
    private final List<Critter> critters; 
    
    private final Map<AntRole, Integer> peaceAssignedRoleCounts = createEmptyRoleCountMap();
    private final Map<AntRole, Integer> warAssignedRoleCounts = createEmptyRoleCountMap();
    private final Map<AntRole, Set<Integer>> peaceRoleDisallowedSubtypes = new HashMap<>();
    private final Map<AntRole, Set<Integer>> warRoleDisallowedSubtypes = new HashMap<>();
    private Map<AntRole, Integer> activeRoleCountCache;
    private boolean roleAssignmentDirty = true;
    private int lastRoleAssignmentPopKey = Integer.MIN_VALUE;
    private final Map<Integer, Integer> outgoingColonyDiplomatMissions = new HashMap<>();
    private final Map<Integer, Integer> incomingColonyDiplomatSupport = new HashMap<>();
    private final Map<Integer, Integer> outgoingDynastyDiplomatMissions = new HashMap<>();
    private final Map<Integer, Integer> outgoingDynastySpyMissions = new HashMap<>();
    private int integrationDiplomatsDeployed;
    private int nativeSpeciesId;
    private Boolean affordableResearchCached;
    private Building affordableBuildingCached;
    private boolean affordableBuildingCacheValid;
    private final Set<Building> buildings;

    // --- Resource Data ---
    private double plants;
    private double mushrooms;
    private double protein;
    private double water;
    private double syrups;
    private double resins;
    private double minerals;
    
    private int aphids; 
    private int symbioticMites;
    private int dermestids;
    private int parasiteAnts;
    private int parasiticMites;

    private int pheromoneStormMonthsRemaining;
    private int recentlyConqueredMonthsRemaining;
    private int recentlyIntegratedMonthsRemaining;
    private final Map<String, Integer> loyaltyModifierRemainingDays = new HashMap<>();
    private int creatineDietMonthsRemaining;

    // --- Hatch Rate Data ---
    private float hatchRateWorker;
    private float hatchRateSoldier;
    private float hatchRateMajor;
    private float hatchRateDrone;
    private float hatchRatePrincess;
    private Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> subtypeHatchRates =
            AntSubtypeService.defaultSubtypeRates();

    // --- Misc. Data ---
    private int totalDeaths;
    private int gameAreaWidth = 1;
    private int gameAreaHeight = 1;
    private long physicsStepSequence;
    private Rectangle lastPhysicsViewport;
    private Dimension lastPhysicsDimension;
    private Building currentBuildingProject = null;
    private Tunnel currentTunnelProject = null;
    private double buildingProgressHours = 0.0;
    private final List<String> eventLog = new ArrayList<>();

    // --- Room Bounds ---
    private Rectangle entranceBounds;
    private Rectangle storageBounds; 
    private Rectangle farmBounds;    
    private Rectangle nurseryBounds; 
    private Rectangle royalBounds;   
    private Rectangle rancherBounds; 
    private Rectangle graverBounds;
    private Rectangle breederBounds; 
    private Rectangle transitBounds; 
    private Rectangle insectPenBounds;

    // --- Service Dependencies ---
    private transient ColonyStatsService statsService;
    private transient ColonySpatialService spatialService;
    private transient ColonySourceService sourceService;
    private transient ColonyPathfindingService pathfindingService;
    private transient ColonyLabourService labourService;
    private transient ColonyPopulationService populationService;
    private transient ColonyDeathService deathService;
    private transient ColonyPhysicsService physicsService;
    private transient ColonyLocationService locationService;
    private transient ColonyAutomationService automationService;
    private transient ColonyResourceService resourceService;
    private transient ColonyStarterService starterService;
    private transient ColonyCritterHandlingService bugHandlingService;
    private transient ColonyConvoyTransitService convoyTransitService;

    // --- Service Initializer ---
    private void initializeServices() {
        this.statsService = new ColonyStatsService();
        this.spatialService = new ColonySpatialService();
        this.sourceService = new ColonySourceService(spatialService);
        this.pathfindingService = new ColonyPathfindingService(spatialService);
        this.locationService = new ColonyLocationService(spatialService, sourceService, pathfindingService);
        this.labourService = new ColonyLabourService();
        this.populationService = new ColonyPopulationService();
        this.deathService = new ColonyDeathService();
        this.physicsService = new ColonyPhysicsService();
        this.automationService = new ColonyAutomationService(); 
        this.resourceService = new ColonyResourceService();
        this.starterService = ColonyStarterService.shared();
        this.bugHandlingService = new ColonyCritterHandlingService();
        this.convoyTransitService = new ColonyConvoyTransitService();
    }

    // --- Initialization Methods ---
    private void initializeLists() {
        this.antGroups.put(GameConstants.TYPE_EGG, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_LARVA, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_PUPA, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_WORKER, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_SOLDIER, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_MAJOR, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_DRONE, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_PRINCESS, new CopyOnWriteArrayList<>());
        this.antGroups.put(GameConstants.TYPE_QUEEN, new CopyOnWriteArrayList<>());
    }

    private static Map<AntRole, Integer> createEmptyRoleCountMap() {
        Map<AntRole, Integer> map = new HashMap<>();
        for (AntRole role : GameConstants.getAntRoles()) {
            map.put(role, 0);
        }
        return map;
    }

    private void initializeAssignedRoles() {
        for (AntRole role : GameConstants.getAntRoles()) {
            peaceAssignedRoleCounts.put(role, 0);
            warAssignedRoleCounts.put(role, 0);
        }
    }

    private boolean usesWarEconomyRoles() {
        Dynasty owner = getDynasty();
        return owner != null && owner.isAtWar();
    }

    private Map<AntRole, Integer> activeAssignedRoleCounts() {
        return usesWarEconomyRoles() ? warAssignedRoleCounts : peaceAssignedRoleCounts;
    }

    private static void applySavedRoleCounts(Map<AntRole, Integer> target, Map<String, Integer> saved) {
        if (saved == null || saved.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Integer> entry : saved.entrySet()) {
            AntRole role = GameConstants.getAntRoleByPersistenceKey(entry.getKey());
            if (role != null && entry.getValue() != null) {
                target.put(role, entry.getValue());
            }
        }
    }

    private static void copyRoleCounts(Map<AntRole, Integer> source, Map<AntRole, Integer> destination) {
        for (AntRole role : GameConstants.getAntRoles()) {
            destination.put(role, source.getOrDefault(role, 0));
        }
    }

    private static Map<Integer, Integer> parseIntKeyMap(Map<String, Integer> saved) {
        Map<Integer, Integer> parsed = new HashMap<>();
        if (saved == null) {
            return parsed;
        }
        for (Map.Entry<String, Integer> entry : saved.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                parsed.put(Integer.parseInt(entry.getKey()), entry.getValue());
            }
        }
        return parsed;
    }

    private void loadAssignedRoleCountsFromSave(Savefile.SavedColony savedColony) {
        applySavedRoleCounts(peaceAssignedRoleCounts, savedColony.assignedRoleCounts);
        stripWarEconomyExclusiveFromPeace();
        if (savedColony.warAssignedRoleCounts != null && !savedColony.warAssignedRoleCounts.isEmpty()) {
            applySavedRoleCounts(warAssignedRoleCounts, savedColony.warAssignedRoleCounts);
        } else if (savedColony.assignedRoleCounts != null && !savedColony.assignedRoleCounts.isEmpty()) {
            copyPeaceRolesToWar();
        }
        applySavedRoleDisallowedSubtypes(peaceRoleDisallowedSubtypes, savedColony.roleDisallowedSubtypesFlat);
        if (savedColony.warRoleDisallowedSubtypesFlat != null && !savedColony.warRoleDisallowedSubtypesFlat.isEmpty()) {
            applySavedRoleDisallowedSubtypes(warRoleDisallowedSubtypes, savedColony.warRoleDisallowedSubtypesFlat);
        } else if (savedColony.roleDisallowedSubtypesFlat != null && !savedColony.roleDisallowedSubtypesFlat.isEmpty()) {
            copyPeaceRoleSubtypeAllowsToWar();
        }
    }

    private static void applySavedRoleDisallowedSubtypes(Map<AntRole, Set<Integer>> target,
            Map<String, Integer> flat) {
        target.clear();
        Map<AntRole, Set<Integer>> loaded = AntSubtypeService.unflattenRoleDisallowedSubtypes(flat);
        for (Map.Entry<AntRole, Set<Integer>> entry : loaded.entrySet()) {
            Set<Integer> cleaned = new HashSet<>(entry.getValue());
            for (AntSubtype forced : entry.getKey().getForcedAllowedSubtypes()) {
                cleaned.remove(forced.getId());
            }
            if (!cleaned.isEmpty()) {
                target.put(entry.getKey(), cleaned);
            }
        }
    }

    private void stripWarEconomyExclusiveFromPeace() {
        for (AntRole role : GameConstants.getAntRoles()) {
            if (GameConstants.isWarEconomyExclusiveRole(role)) {
                peaceAssignedRoleCounts.put(role, 0);
            }
        }
    }
    
    private void initializeDefaults() {
        this.plants = 0;
        this.mushrooms = 0;
        this.protein = 0;           
        this.water = 0;
        this.syrups = 0;
        this.resins = 0;
        this.minerals = 0;
        this.aphids = 0;
        this.symbioticMites = 0;
        this.dermestids = 0;
        this.parasiteAnts = 0;
        this.parasiticMites = 0;
        this.hatchRateWorker = 100.0f;
        this.hatchRateSoldier = 0.0f;
        this.hatchRateMajor = 0.0f;
        this.hatchRateDrone = 0.0f;
        this.hatchRatePrincess = 0.0f;
        this.subtypeHatchRates = AntSubtypeService.defaultSubtypeRates();
        this.isActive = false;
        this.autoBuildEnabled = false;
        this.autoTunnelsEnabled = false;
        this.autoLogisticsEnabled = false;
        this.loyalty = GameNumbers.DEFAULT_COLONY_LOYALTY;
    }

    private void initializeBuildings() {
        this.buildings.add(GameUnlocks.ROYAL_CHAMBER_0);
        this.buildings.add(GameUnlocks.EGG_CHAMBER_0);
        this.buildings.add(GameUnlocks.MUSHROOM_CHAMBER_0);
        this.buildings.add(GameUnlocks.PLANT_CHAMBER_0);
        this.buildings.add(GameUnlocks.WATER_RESERVOIR_0);
    }
    
    private void loadBuildings(Savefile.SavedColony savedColony) {
        List<Integer> unlockedBuildingIds = savedColony.unlockedBuildingIds; 
        if (unlockedBuildingIds == null || unlockedBuildingIds.isEmpty()) {
            initializeBuildings();
            return;
        }
        Map<Integer, Building> allBuildings = new HashMap<>();
        for (Building up : GameUnlocks.getBuildings()) {
            allBuildings.put(up.getId(), up);
        }
        for (Integer id : unlockedBuildingIds) {
            Building buildingToUnlock = allBuildings.get(id);
            if (buildingToUnlock != null) {
                this.buildings.add(buildingToUnlock);
            }
        }
    }

    // --- Constructors ---
    public Colony(int id, String name, boolean isPlayer) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.rank = GameConstants.RANK_COLONY;
        this.automationEnabled = !isPlayer;
        this.totalDeaths = 0;
        this.age = 0;
        this.daysWithoutQueen = 0;
        this.antGroups = new HashMap<>();
        this.deadAnts = new CopyOnWriteArrayList<>(); 
        this.critters = new CopyOnWriteArrayList<>();
        this.buildings = new HashSet<>();
        
        initializeLists();
        initializeDefaults();
        initializeBuildings();
        initializeAssignedRoles();
        initializeServices(); 
    }

    public Colony(Savefile.SavedColony savedColony) {
        this.id = savedColony.id;
        this.name = savedColony.name;
        this.isPlayer = savedColony.isPlayer;
        
        this.rank = GameConstants.RANK_COLONY;
        this.antGroups = new HashMap<>();
        this.deadAnts = new CopyOnWriteArrayList<>();
        this.critters = new CopyOnWriteArrayList<>();
        this.buildings = new HashSet<>();

        initializeLists();
        initializeDefaults(); 
        
        this.isCapital = savedColony.isCapital;
        this.automationEnabled = savedColony.isAutomated;
        this.autoBuildEnabled = savedColony.autoBuildEnabled;
        this.autoTunnelsEnabled = savedColony.autoTunnelsEnabled;
        this.autoLogisticsEnabled = savedColony.autoLogisticsEnabled;
        this.age = savedColony.age;
        this.daysWithoutQueen = savedColony.daysWithoutQueen;
        this.loyalty = GameNumbers.clampColonyLoyalty(savedColony.loyalty);
        this.militaryPower = savedColony.militaryPower;
        this.totalDeaths = savedColony.totalDeaths;

        loadBuildings(savedColony);
        initializeAssignedRoles(); 
        initializeServices(); 
        
        if (this.deathService != null && savedColony.localDeathStatistics != null) {
            this.deathService.loadDeathStatistics(savedColony.localDeathStatistics);
        }
        
        loadAssignedRoleCountsFromSave(savedColony);
        copyDiplomatMissionMaps(
                parseIntKeyMap(savedColony.outgoingColonyDiplomatMissions),
                parseIntKeyMap(savedColony.incomingColonyDiplomatSupport),
                parseIntKeyMap(savedColony.outgoingDynastyDiplomatMissions));
        copySpyMissionMaps(parseIntKeyMap(savedColony.outgoingDynastySpyMissions));
        
        this.hatchRateWorker = savedColony.hatchRateWorker;
        this.hatchRateSoldier = savedColony.hatchRateSoldier;
        this.hatchRateMajor = savedColony.hatchRateMajor;
        this.hatchRateDrone = savedColony.hatchRateDrone;
        this.hatchRatePrincess = savedColony.hatchRatePrincess;
        if (savedColony.subtypeHatchRatesFlat != null && !savedColony.subtypeHatchRatesFlat.isEmpty()) {
            this.subtypeHatchRates = AntSubtypeService.unflattenSubtypeRates(savedColony.subtypeHatchRatesFlat);
        }

        populateAntList(getEggs(), savedColony.eggs, GameConstants.TYPE_EGG);
        populateAntList(getLarvae(), savedColony.larvae, GameConstants.TYPE_LARVA);
        populateAntList(getPupae(), savedColony.pupae, GameConstants.TYPE_PUPA);
        AntSubtypeService.populateAntsFromSubtypeCounts(this, getWorkers(), GameConstants.TYPE_WORKER,
                savedColony.workerSubtypes, savedColony.workers);
        AntSubtypeService.populateAntsFromSubtypeCounts(this, getSoldiers(), GameConstants.TYPE_SOLDIER,
                savedColony.soldierSubtypes, savedColony.soldiers);
        AntSubtypeService.populateAntsFromSubtypeCounts(this, getMajors(), GameConstants.TYPE_MAJOR,
                savedColony.majorSubtypes, savedColony.majors);
        populateAntList(getDrones(), savedColony.drones, GameConstants.TYPE_DRONE);
        AntSubtypeService.populateAntsFromSubtypeCounts(this, getPrincesses(), GameConstants.TYPE_PRINCESS,
                savedColony.princessSubtypes, savedColony.princesses);
        AntSubtypeService.populateAntsFromSubtypeCounts(this, getQueens(), GameConstants.TYPE_QUEEN,
                savedColony.queenSubtypes, savedColony.queens);
        populateAntList(deadAnts, savedColony.deadAnts, GameConstants.TYPE_DEAD);

        this.plants = savedColony.plants;
        this.mushrooms = savedColony.mushrooms;
        this.protein = savedColony.protein;
        this.water = savedColony.water;
        this.syrups = savedColony.syrups;
        this.resins = savedColony.resins;
        this.minerals = savedColony.minerals;

        this.aphids = savedColony.aphids;
        this.symbioticMites = savedColony.symbioticMites;
        this.dermestids = savedColony.dermestids;

        this.parasiteAnts = savedColony.parasiteAnts;
        this.parasiticMites = savedColony.parasiticMites;
        this.pheromoneStormMonthsRemaining = savedColony.pheromoneStormMonthsRemaining;
        this.recentlyConqueredMonthsRemaining = savedColony.recentlyConqueredMonthsRemaining;
        this.recentlyIntegratedMonthsRemaining = savedColony.recentlyIntegratedMonthsRemaining;
        if (savedColony.loyaltyModifierRemainingDays != null) {
            loyaltyModifierRemainingDays.putAll(savedColony.loyaltyModifierRemainingDays);
        } else {
            migrateLegacyLoyaltyModifierFields();
        }
        this.creatineDietMonthsRemaining = savedColony.creatineDietMonthsRemaining;
        this.integrationDiplomatsDeployed = savedColony.integrationDiplomatsDeployed;
        this.nativeSpeciesId = savedColony.nativeSpeciesId;
        for (int i = 0; i < this.parasiteAnts; i++) {
            Critter p = new Critter(GameConstants.TYPE_PARASITE_ANT);
            p.setDimension(WorldSpaces.UNDERWORLD);
            this.critters.add(p);
        }

        getBugHandlingService().restorePetCountsFromSave(
                this, savedColony.aphids, savedColony.symbioticMites, savedColony.dermestids);
        getBugHandlingService().setParasiticMiteCount(this, savedColony.parasiticMites);

        this.totalDeaths = savedColony.totalDeaths;
        
        if (savedColony.savedResourceSources != null && this.locationService != null) {
            List<ResourceType> allTypes = GameConstants.getResources();
            
            for (Savefile.SavedResourceSource s : savedColony.savedResourceSources) {
                ResourceType type = null;
                for (ResourceType rt : allTypes) {
                    if (rt.getId() == s.typeId) {
                        type = rt;
                        break;
                    }
                }
                
                if (type != null) {
                    ResourceSource rs = new ResourceSource(
                        type,
                        s.currentQuantity,
                        s.initialQuantity,
                        s.x,
                        s.y
                    );
                    this.locationService.addSource(this, rs);
                }
            }
        }

        rankUp();
    }
        
    // --- Population Initializer ---
    public void addAnts(AntType type, int count) {
        List<Ant> list = getAntsByType(type);
        populateAntList(list, count, type);
    }

    private void populateAntList(List<Ant> list, int count, AntType type) {
        for (int i = 0; i < count; i++) {
            Ant newAnt = new Ant(this, type);
            if (type == GameConstants.TYPE_EGG || 
                type == GameConstants.TYPE_LARVA || 
                type == GameConstants.TYPE_PUPA || 
                type == GameConstants.TYPE_QUEEN) {
                newAnt.setDimension(WorldSpaces.UNDERWORLD);
            } 
            else {
                newAnt.setDimension(WorldSpaces.OVERWORLD); 
            }
            list.add(newAnt);
        }
        if (count > 0) {
            invalidateActiveRoleCountCache();
        }
    }
    
    private void randomizeAllAntPositions() {
        if (physicsService == null) initializeServices(); 
        physicsService.randomizeAllAntPositions(this);
    }
    
    // --- Event Log  ---
    public void logEvent(String message) {
        synchronized (eventLog) {
            eventLog.add(message);
        }
    }

    public List<String> consumeEvents() {
        List<String> consumed;
        synchronized (eventLog) {
            consumed = new ArrayList<>(eventLog);
            eventLog.clear();
        }
        return consumed;
    }

    public List<String> consumeEventsWithPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return List.of();
        }
        List<String> consumed = new ArrayList<>();
        synchronized (eventLog) {
            Iterator<String> it = eventLog.iterator();
            while (it.hasNext()) {
                String msg = it.next();
                if (msg.startsWith(prefix)) {
                    consumed.add(msg);
                    it.remove();
                }
            }
        }
        return consumed;
    }

    // --- Death Tracking Wrapper ---
    public void recordAntDeath(Ant ant, String cause) {
        if (ant == null) return;
        this.totalDeaths++;
        this.deadAnts.add(ant);
        if (deathService != null) {
            deathService.recordDeath(cause, this);
        }
        if (ant.getAntType() == GameConstants.TYPE_QUEEN) {
            clampCommanderWarAssignment();
        }
    }

    public void handleAntCasualtyAftermath(Ant ant, AntType formerType, AntRole formerRole, boolean wasOnTrade) {
        markRoleAssignmentDirty();
        if (wasOnTrade) {
            ant.setOnTrade(false);
            handleConvoyEscortCasualty();
        }
        if (dynasty != null && formerType == GameConstants.TYPE_PRINCESS) {
            DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
            if (diplo != null) {
                diplo.reconcileDiplomatDeploymentsAfterCasualty(this, formerRole);
            }
            DynastyIntelligenceService intel = dynasty.getIntelligenceService();
            if (intel != null && (formerRole == null || formerRole == GameConstants.ROLE_SPY)) {
                intel.reconcileSpyDeploymentsAfterCasualty(this);
            }
        }
    }

    private void handleConvoyEscortCasualty() {
        if (dynasty == null || dynasty.getTradeService() == null) {
            return;
        }
        TradeManager tradeManager = dynasty.getTradeService().getTradeManager();
        if (tradeManager == null) {
            return;
        }
        for (Trade trade : tradeManager.getActiveTrades()) {
            if (!trade.isActive() || trade.getOrigin().getColony() != this) {
                continue;
            }
            if (!trade.hasSufficientLiveEscorts()) {
                trade.cancelDueToEscortLoss(tradeManager);
            }
        }
    }

    // --- Getters/Setters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isCapital() { return isCapital; }
    public void setCapital(boolean isCapital) { this.isCapital = isCapital; }

    private boolean isDynastyCapital() {
        if (dynasty != null) {
            Colony capitalColony = dynasty.getCapital();
            return capitalColony != null && capitalColony == this;
        }
        return isCapital;
    }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public int getDaysWithoutQueen() { return daysWithoutQueen; }
    public void setDaysWithoutQueen(int days) { this.daysWithoutQueen = days; }

    public int getLoyalty() { return loyalty; }

    public int getMilitaryPower() { return militaryPower; }

    public void setMilitaryPower(int militaryPower) {
        this.militaryPower = Math.max(0, militaryPower);
    }

    public int getActiveMilitaryPower() {
        return activeMilitaryPower;
    }

    public void setActiveMilitaryPower(int activeMilitaryPower) {
        this.activeMilitaryPower = Math.max(0, activeMilitaryPower);
    }

    public int getReserveMilitaryPower() {
        return reserveMilitaryPower;
    }

    public void setReserveMilitaryPower(int reserveMilitaryPower) {
        this.reserveMilitaryPower = Math.max(0, reserveMilitaryPower);
    }

    public void setLoyalty(int loyalty) {
        this.loyalty = GameNumbers.clampColonyLoyalty(loyalty);
    }

    public int getLoyaltyModifierBonus(TradeManager tradeManager, World world) {
        int bonus = 0;
        if (tradeManager != null && world != null && participatesInActiveTrade(tradeManager, world)) {
            bonus += GameConstants.LOYALTY_MODIFIER_TRADE.getLoyaltyDelta();
        }
        if (world != null && dynasty != null && hasCompleteTunnel(world)) {
            bonus += GameConstants.LOYALTY_MODIFIER_TUNNEL.getLoyaltyDelta();
        }
        if (isDynastyCapital()) {
            bonus += GameConstants.LOYALTY_MODIFIER_CAPITAL.getLoyaltyDelta();
        }
        for (Map.Entry<String, Integer> entry : loyaltyModifierRemainingDays.entrySet()) {
            ColonyLoyaltyModifier modifier = GameConstants.getColonyLoyaltyModifierByKey(entry.getKey());
            if (modifier != null) {
                bonus += modifier.getLoyaltyDelta();
            }
        }
        bonus += getMilitaryLoyaltyAdjustment();
        bonus += getDistanceFromCapitalLoyaltyAdjustment(world);
        return bonus;
    }

    private int getDistanceFromCapitalLoyaltyAdjustment(World world) {
        if (isDynastyCapital() || dynasty == null || world == null) {
            return 0;
        }
        Colony capital = dynasty.getCapital();
        if (capital == null || capital == this) {
            return 0;
        }
        int tiles = world.colonyHexDistance(this, capital);
        return GameNumbers.getCapitalDistanceLoyaltyPenalty(tiles);
    }

    public int getCapitalHexDistance(World world) {
        if (isDynastyCapital() || dynasty == null || world == null) {
            return 0;
        }
        Colony capital = dynasty.getCapital();
        if (capital == null || capital == this) {
            return -1;
        }
        return world.colonyHexDistance(this, capital);
    }

    private int getMilitaryLoyaltyAdjustment() {
        if (isDynastyCapital() || dynasty == null) {
            return 0;
        }
        Colony capital = dynasty.getCapital();
        if (capital == null || capital == this) {
            return 0;
        }
        return ColonyMilitaryService.getMilitaryLoyaltyAdjustment(
                getMilitaryPower(), capital.getMilitaryPower(), isDynastyCapital());
    }

    public int getEffectiveLoyalty(TradeManager tradeManager, World world) {
        return GameNumbers.clampColonyLoyalty(
                loyalty + getLoyaltyModifierBonus(tradeManager, world) + getDiplomatLoyaltyBonus());
    }

    public int getDiplomatLoyaltyBonus() {
        if (dynasty == null || incomingColonyDiplomatSupport.isEmpty()) {
            return 0;
        }
        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        if (diplo == null) {
            return 0;
        }
        int gainPer = diplo.getDiplomatStabilityGainPerAnt();
        int diplomats = incomingColonyDiplomatSupport.values().stream().mapToInt(Integer::intValue).sum();
        return diplomats * gainPer;
    }

    public String buildLoyaltyModifierTooltip(TradeManager tradeManager, World world) {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append(LanguageStrings.get(LanguageStrings.LOYALTY_TOOLTIP_BASE))
                .append(": ")
                .append(loyalty)
                .append("<br>");

        if (isDynastyCapital()) {
            appendLoyaltyModifierLine(sb, GameConstants.LOYALTY_MODIFIER_CAPITAL);
        }
        if (tradeManager != null && world != null && participatesInActiveTrade(tradeManager, world)) {
            appendLoyaltyModifierLine(sb, GameConstants.LOYALTY_MODIFIER_TRADE);
        }
        if (world != null && dynasty != null && hasCompleteTunnel(world)) {
            appendLoyaltyModifierLine(sb, GameConstants.LOYALTY_MODIFIER_TUNNEL);
        }
        for (Map.Entry<String, Integer> entry : loyaltyModifierRemainingDays.entrySet()) {
            ColonyLoyaltyModifier modifier = GameConstants.getColonyLoyaltyModifierByKey(entry.getKey());
            if (modifier != null) {
                appendLoyaltyModifierLine(sb, modifier);
            }
        }
        int militaryAdj = getMilitaryLoyaltyAdjustment();
        if (militaryAdj != 0) {
            sb.append(LanguageStrings.format(
                    LanguageStrings.LOYALTY_MODIFIER_LINE,
                    LanguageStrings.get(LanguageStrings.LOYALTY_MODIFIER_MILITARY_VS_CAPITAL),
                    LanguageStrings.formatSigned(militaryAdj))).append("<br>");
        }
        int distanceAdj = getDistanceFromCapitalLoyaltyAdjustment(world);
        if (distanceAdj != 0) {
            int tiles = getCapitalHexDistance(world);
            sb.append(LanguageStrings.format(
                    LanguageStrings.LOYALTY_MODIFIER_DISTANCE_LINE,
                    LanguageStrings.get(LanguageStrings.LOYALTY_MODIFIER_DISTANCE_FROM_CAPITAL),
                    tiles,
                    LanguageStrings.formatSigned(distanceAdj))).append("<br>");
        }
        int diplomatAdj = getDiplomatLoyaltyBonus();
        if (diplomatAdj != 0) {
            sb.append(LanguageStrings.format(
                    LanguageStrings.LOYALTY_MODIFIER_LINE,
                    LanguageStrings.get(LanguageStrings.LOYALTY_MODIFIER_DIPLOMAT_MISSION),
                    LanguageStrings.formatSigned(diplomatAdj))).append("<br>");
        }

        sb.append(LanguageStrings.get(LanguageStrings.LOYALTY_TOOLTIP_EFFECTIVE))
                .append(": ")
                .append(LanguageStrings.formatNumber(getEffectiveLoyalty(tradeManager, world)));
        sb.append("</html>");
        return sb.toString();
    }

    private static void appendLoyaltyModifierLine(StringBuilder sb, ColonyLoyaltyModifier modifier) {
        sb.append(LanguageStrings.format(
                LanguageStrings.LOYALTY_MODIFIER_LINE,
                modifier.getName(),
                LanguageStrings.formatSigned(modifier.getLoyaltyDelta()))).append("<br>");
    }

    public boolean participatesInActiveTrade(TradeManager tradeManager, World world) {
        if (tradeManager == null || world == null) {
            return false;
        }
        Hex colonyHex = world.getHexOfColony(this);
        if (colonyHex == null) {
            return false;
        }
        for (Trade trade : tradeManager.getActiveTrades()) {
            if (!trade.isActive()) {
                continue;
            }
            Hex originHex = trade.getOrigin();
            Hex destinationHex = trade.getDestination();
            if (originHex == colonyHex || destinationHex == colonyHex) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCompleteTunnel(World world) {
        if (dynasty == null || world == null) {
            return false;
        }
        Hex colonyHex = world.getHexOfColony(this);
        if (colonyHex == null) {
            return false;
        }
        for (Tunnel tunnel : dynasty.getTunnels()) {
            if (tunnel.isComplete() && (tunnel.getHexA() == colonyHex || tunnel.getHexB() == colonyHex)) {
                return true;
            }
        }
        return false;
    }
    
    public Dynasty getDynasty() { return dynasty; }
    public void setDynasty(Dynasty dynasty) { 
        this.dynasty = dynasty; 
        if (dynasty != null) {
            this.isPlayer = dynasty.isPlayer();
            if (!dynasty.getColonies().contains(this)) {
                dynasty.addColony(this); 
            }
            refreshAntStats();
        }
    }

    public AntSpecies getSpecies() {
        if (nativeSpeciesId > 0) {
            AntSpecies nativeSpecies = GameConstants.getSpeciesById(nativeSpeciesId);
            if (nativeSpecies != null) {
                return nativeSpecies;
            }
        }
        return dynasty != null ? dynasty.getSpecies() : GameConstants.SPECIES_OMNI;
    }

    public void setSpecies(AntSpecies species) {
        if (dynasty != null) {
            dynasty.setSpecies(species);
        }
    }

    public int getNativeSpeciesId() {
        return nativeSpeciesId;
    }

    public void setNativeSpeciesId(int nativeSpeciesId) {
        this.nativeSpeciesId = Math.max(0, nativeSpeciesId);
    }

    public int getIntegrationDiplomatsDeployed() {
        return integrationDiplomatsDeployed;
    }

    public void setIntegrationDiplomatsDeployed(int integrationDiplomatsDeployed) {
        this.integrationDiplomatsDeployed = Math.max(0, integrationDiplomatsDeployed);
    }
    
    public int getResearchPoints() { 
        return dynasty != null ? dynasty.getResearchPoints() : 0; 
    }
    public void setResearchPoints(int points) { 
        if (dynasty != null) dynasty.setResearchPoints(points); 
    }
    public void addResearchPoints(int amount) {
        if (dynasty != null) dynasty.addResearchPoints(amount);
    }
    
    public boolean hasUpgrade(Upgrade upgrade) {
        return dynasty != null && dynasty.hasUpgrade(upgrade);
    }
    public void unlockUpgrade(Upgrade upgrade) {
        if (dynasty != null) {
            dynasty.unlockUpgrade(upgrade);
        }
    }
    public Set<Upgrade> getUnlockedUpgrades() {
        return dynasty != null ? dynasty.getUnlockedUpgrades() : new HashSet<>();
    }
    
    public boolean isPlayer() { return isPlayer; }
    public void setIsPlayer(boolean isPlayer) { this.isPlayer = isPlayer; }

    public boolean belongsToPlayerDynasty() {
        return dynasty != null && dynasty.isPlayer();
    }

    public Rank getRank() { return rank; }
    public void setRank(Rank rank) { this.rank = rank; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public boolean runsFullSimulation() {
        return isActive && isPlayer;
    }

    public boolean isAutomationEnabled() { return automationEnabled; }
    public void setAutomationEnabled(boolean automationEnabled) { this.automationEnabled = automationEnabled; }
    public boolean isAutoBuildEnabled() { return autoBuildEnabled; }
    public void setAutoBuildEnabled(boolean autoBuildEnabled) { this.autoBuildEnabled = autoBuildEnabled; }
    public boolean isAutoTunnelsEnabled() { return autoTunnelsEnabled; }
    public void setAutoTunnelsEnabled(boolean autoTunnelsEnabled) { this.autoTunnelsEnabled = autoTunnelsEnabled; }
    public boolean isAutoLogisticsEnabled() { return autoLogisticsEnabled; }
    public void setAutoLogisticsEnabled(boolean autoLogisticsEnabled) { this.autoLogisticsEnabled = autoLogisticsEnabled; }

    public int getDeployedDiplomatCount() {
        int total = 0;
        for (int count : outgoingColonyDiplomatMissions.values()) {
            total += count;
        }
        for (int count : outgoingDynastyDiplomatMissions.values()) {
            total += count;
        }
        total += integrationDiplomatsDeployed;
        return total;
    }

    public Map<Integer, Integer> getOutgoingColonyDiplomatMissions() {
        return outgoingColonyDiplomatMissions;
    }

    public Map<Integer, Integer> getIncomingColonyDiplomatSupport() {
        return incomingColonyDiplomatSupport;
    }

    public Map<Integer, Integer> getOutgoingDynastyDiplomatMissions() {
        return outgoingDynastyDiplomatMissions;
    }

    public int getDeployedSpyCount() {
        int total = 0;
        for (int count : outgoingDynastySpyMissions.values()) {
            total += count;
        }
        return total;
    }

    public Map<Integer, Integer> getOutgoingDynastySpyMissions() {
        return outgoingDynastySpyMissions;
    }

    public void copySpyMissionMaps(Map<Integer, Integer> outgoingDynasty) {
        outgoingDynastySpyMissions.clear();
        if (outgoingDynasty != null) {
            outgoingDynastySpyMissions.putAll(outgoingDynasty);
        }
    }

    public void copyDiplomatMissionMaps(
            Map<Integer, Integer> outgoingColony,
            Map<Integer, Integer> incomingColony,
            Map<Integer, Integer> outgoingDynasty) {
        outgoingColonyDiplomatMissions.clear();
        incomingColonyDiplomatSupport.clear();
        outgoingDynastyDiplomatMissions.clear();
        if (outgoingColony != null) {
            outgoingColonyDiplomatMissions.putAll(outgoingColony);
        }
        if (incomingColony != null) {
            incomingColonyDiplomatSupport.putAll(incomingColony);
        }
        if (outgoingDynasty != null) {
            outgoingDynastyDiplomatMissions.putAll(outgoingDynasty);
        }
    }

    public Map<AntType, List<Ant>> getAntGroups() { return antGroups; }

    public List<Ant> getAntsByType(AntType type) {
        if (type == GameConstants.TYPE_DEAD) {
            return deadAnts;
        }
        List<Ant> list = antGroups.get(type);
        if (list != null) {
            return list;
        }
        return Collections.emptyList();
    }
    public List<Ant> getEggs() { return antGroups.get(GameConstants.TYPE_EGG); }
    public void setEggs(List<Ant> eggs) { antGroups.put(GameConstants.TYPE_EGG, eggs); }
    public List<Ant> getLarvae() { return antGroups.get(GameConstants.TYPE_LARVA); }
    public void setLarvae(List<Ant> larvae) { antGroups.put(GameConstants.TYPE_LARVA, larvae); }
    public List<Ant> getPupae() { return antGroups.get(GameConstants.TYPE_PUPA); }
    public void setPupae(List<Ant> pupae) { antGroups.put(GameConstants.TYPE_PUPA, pupae); }
    public List<Ant> getWorkers() { return antGroups.get(GameConstants.TYPE_WORKER); }
    public void setWorkers(List<Ant> workers) { antGroups.put(GameConstants.TYPE_WORKER, workers); }
    public List<Ant> getSoldiers() { return antGroups.get(GameConstants.TYPE_SOLDIER); }
    public void setSoldiers(List<Ant> soldiers) { antGroups.put(GameConstants.TYPE_SOLDIER, soldiers); }
    public List<Ant> getMajors() { return antGroups.get(GameConstants.TYPE_MAJOR); }
    public void setMajors(List<Ant> majors) { antGroups.put(GameConstants.TYPE_MAJOR, majors); }
    public List<Ant> getDrones() { return antGroups.get(GameConstants.TYPE_DRONE); }
    public void setDrones(List<Ant> drones) { antGroups.put(GameConstants.TYPE_DRONE, drones); }
    public List<Ant> getPrincesses() { return antGroups.get(GameConstants.TYPE_PRINCESS); }
    public void setPrincesses(List<Ant> princesses) { antGroups.put(GameConstants.TYPE_PRINCESS, princesses); }
    public List<Ant> getQueens() { return antGroups.get(GameConstants.TYPE_QUEEN); }
    public void setQueens(List<Ant> queens) { antGroups.put(GameConstants.TYPE_QUEEN, queens); }
    public List<Ant> getDeadAnts() { return deadAnts; }
    public void setDeadAnts(List<Ant> deadAnts) {
        this.deadAnts.clear();
        this.deadAnts.addAll(deadAnts);
    }
    
    public List<Critter> getCritters() { return critters; }
    
    public int getAntTotal() {
        return antGroups.values().stream().mapToInt(List::size).sum();
    }

    public boolean hasBuilding(Building building) { return this.buildings.contains(building); }
    public void unlockBuilding(Building building) {
        this.buildings.add(building);
        invalidateAffordableAlertCache();
    }
    public Set<Building> getUnlockedBuildings() { return this.buildings; }
    public Building getCurrentBuildingProject() { return currentBuildingProject; }
    public void setCurrentBuildingProject(Building b) {
        this.currentBuildingProject = b;
        invalidateAffordableAlertCache();
    }
    public Tunnel getCurrentTunnelProject() { return currentTunnelProject; }
    public void setCurrentTunnelProject(Tunnel t) { this.currentTunnelProject = t; }
    public double getBuildingProgressHours() { return buildingProgressHours; }
    public void setBuildingProgressHours(double d) { this.buildingProgressHours = d; }

    public boolean startBuildingProject(Building building) {
        if (building == null || currentBuildingProject != null) return false;
        Engine eng = (getDynasty() != null && getDynasty().getOwningWorld() != null)
                ? getDynasty().getOwningWorld().getEngine()
                : null;
        boolean instant = eng != null && eng.isInstantBuildings();
        if (!instant && !building.isAvailableFor(getDynasty())) return false;
        if (!GameUnlocks.meetsBuildingUnlockRequirement(this, building)) return false;
        if (building.getRequirement() != null && !hasBuilding(building.getRequirement())) return false;
        if (!instant) {
            if (getMinerals() < building.getMineralCost()
                    || getResins() < building.getResinCost()
                    || getPlants() < building.getPlantCost()) {
                return false;
            }
            resourceService.consumeResource(this, GameConstants.RESOURCE_ROCK, building.getMineralCost());
            resourceService.consumeResource(this, GameConstants.RESOURCE_RESIN, building.getResinCost());
            resourceService.consumeResource(this, GameConstants.RESOURCE_PLANT, building.getPlantCost());
            setCurrentBuildingProject(building);
            this.buildingProgressHours = 0.0;
            if (isPlayer()) {
                SfxService.play(SoundEffects.BUILDING);
            }
            return true;
        }
        unlockBuilding(building);
        if (isPlayer()) {
            SfxService.play(SoundEffects.BUILDING_END);
        }
        if (eng != null) {
            eng.applySandboxTaintToActiveWorld();
        }
        return true;
    }
    
    // --- Resource Getters/Setters ---
    public int getPlants() { return (int) plants; }
    public int getMushrooms() { return (int) mushrooms; }
    public int getProtein() { return (int) protein; }
    public int getWater() { return (int) water; }
    public int getSyrups() { return (int) syrups; }
    public int getResins() { return (int) resins; }
    public int getMinerals() { return (int) minerals; }

    public double getPlantsPrecise() { return plants; }
    public double getMushroomsPrecise() { return mushrooms; }
    public double getProteinPrecise() { return protein; }
    public double getWaterPrecise() { return water; }
    public double getSyrupsPrecise() { return syrups; }
    public double getResinsPrecise() { return resins; }
    public double getMineralsPrecise() { return minerals; }
    
    public void setPlants(double plants) { 
        this.plants = Math.max(0, plants);
        invalidateAffordableAlertCache();
    }
    public void setMushrooms(double mushrooms) { 
        this.mushrooms = Math.max(0, mushrooms); 
    }
    public void setProtein(double protein) { 
        this.protein = Math.max(0, protein); 
    }
    public void setWater(double water) { 
        this.water = Math.max(0, water); 
    }
    public void setSyrups(double syrups) { 
        this.syrups = Math.max(0, syrups); 
    }
    public void setResins(double resins) {
        this.resins = Math.max(0, resins);
        invalidateAffordableAlertCache();
    }
    public void setMinerals(double minerals) {
        this.minerals = Math.max(0, minerals);
        invalidateAffordableAlertCache();
    }

    public int getAphids() { return aphids; }
    public void setAphids(int count) {
        getBugHandlingService().setCount(this, GameConstants.TYPE_APHID, count);
    }

    public int getSymbioticMites() { return symbioticMites; }
    public void setSymbioticMites(int count) {
        getBugHandlingService().setCount(this, GameConstants.TYPE_SYMBIOTIC_MITE, count);
    }

    public int getDermestids() { return dermestids; }
    public void setDermestids(int count) {
        getBugHandlingService().setCount(this, GameConstants.TYPE_DERMESTID, count);
    }

    public void applyPetBugCount(Species type, int count) {
        if (type == GameConstants.TYPE_APHID) {
            this.aphids = count;
        } else if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
            this.symbioticMites = count;
        } else if (type == GameConstants.TYPE_DERMESTID) {
            this.dermestids = count;
        }
    }

    public void applyParasiteAntCount(int count) {
        this.parasiteAnts = count;
    }

    public ColonyCritterHandlingService getBugHandlingService() {
        return bugHandlingService;
    }

    public ColonyConvoyTransitService getConvoyTransitService() {
        return convoyTransitService;
    }

    public int getParasiteAnts() { return parasiteAnts; }
    public void setParasiteAnts(int count) {
        getBugHandlingService().setParasiteAntCount(this, count);
    }

    public int getParasiticMites() { return parasiticMites; }

    public void setParasiticMites(int count) {
        getBugHandlingService().setParasiticMiteCount(this, count);
    }

    public void applyParasiticMiteCount(int count) {
        this.parasiticMites = Math.max(0, count);
    }

    public int getParasiticMiteSlowedAntCount() {
        return getBugHandlingService().getParasiticMiteSlowedAntCount(this);
    }

    public String getParasiteAntCountDisplay(boolean fuzzEnabled) {
        if (!hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            return "???";
        }
        int actual = getParasiteAnts();
        
        if (!fuzzEnabled) {
            return String.valueOf(actual);
        }

        if (actual == 0) return "~0";
        
        double fuzz = GameRandom.nextDouble() * 0.2; 
        boolean up = GameRandom.nextDouble() > 0.5;
        
        int display = actual;
        if (up) display += (int)(actual * fuzz);
        else display -= (int)(actual * fuzz);
        
        if (display < 0) display = 0;
        
        return "~" + display;
    }

    public void setGameAreaDimensions(int width, int height) {
        boolean firstTimeUpdate = (this.gameAreaWidth == 1 && this.gameAreaHeight == 1 && width > 1 && height > 1);
        this.gameAreaWidth = width;
        this.gameAreaHeight = height;
        if (firstTimeUpdate) {
            randomizeAllAntPositions();
        }
    }
    public int getGameAreaWidth() { return this.gameAreaWidth; }
    public int getGameAreaHeight() { return this.gameAreaHeight; }

    public int getAssignedRoleCount(AntRole role) {
        return activeAssignedRoleCounts().getOrDefault(role, 0);
    }

    public void setAssignedRoleCount(AntRole role, int count) {
        if (count >= 0) {
            int previous = activeAssignedRoleCounts().getOrDefault(role, 0);
            if (previous != count) {
                activeAssignedRoleCounts().put(role, count);
                markRoleAssignmentDirty();
                reconcileDiplomatDeploymentsIfNeeded(role, previous, count, true);
            }
        }
    }

    private void reconcileDiplomatDeploymentsIfNeeded(AntRole role, int previous, int next, boolean activeEconomy) {
        if (!activeEconomy || dynasty == null || next >= previous) {
            return;
        }
        if (role == GameConstants.ROLE_DIPLOMAT) {
            DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
            if (diplo != null) {
                diplo.reconcileDiplomatDeployments(this, next);
            }
        } else if (role == GameConstants.ROLE_SPY) {
            DynastyIntelligenceService intel = dynasty.getIntelligenceService();
            if (intel != null) {
                intel.reconcileSpyDeployments(this, next);
            }
        }
    }

    public Map<AntRole, Integer> getAssignedRoleCounts() {
        return activeAssignedRoleCounts();
    }

    public int getPeaceAssignedRoleCount(AntRole role) {
        return peaceAssignedRoleCounts.getOrDefault(role, 0);
    }

    public void setPeaceAssignedRoleCount(AntRole role, int count) {
        if (GameConstants.isWarEconomyExclusiveRole(role)) {
            count = 0;
        }
        if (count >= 0) {
            int previous = peaceAssignedRoleCounts.getOrDefault(role, 0);
            if (previous != count) {
                peaceAssignedRoleCounts.put(role, count);
                markRoleAssignmentDirty();
                reconcileDiplomatDeploymentsIfNeeded(role, previous, count, !usesWarEconomyRoles());
            }
        }
    }

    public Map<AntRole, Integer> getPeaceAssignedRoleCounts() {
        return peaceAssignedRoleCounts;
    }

    public int getWarAssignedRoleCount(AntRole role) {
        return warAssignedRoleCounts.getOrDefault(role, 0);
    }

    public void setWarAssignedRoleCount(AntRole role, int count) {
        if (count < 0) {
            return;
        }
        if (role == GameConstants.ROLE_COMMANDER) {
            count = Math.min(count, getMaxAssignableCommanders());
        }
        int previous = warAssignedRoleCounts.getOrDefault(role, 0);
        if (previous != count) {
            warAssignedRoleCounts.put(role, count);
            markRoleAssignmentDirty();
            reconcileDiplomatDeploymentsIfNeeded(role, previous, count, usesWarEconomyRoles());
        }
    }

    public int getMaxAssignableCommanders() {
        int livingQueens = 0;
        if (getQueens() != null) {
            for (Ant queen : getQueens()) {
                if (queen != null && queen.isAlive()) {
                    livingQueens++;
                }
            }
        }
        if (livingQueens < GameNumbers.TRIGGER_COMMANDER_MIN_QUEENS_IN_COLONY) {
            return 0;
        }
        return GameNumbers.COMMANDER_MAX_PER_COLONY;
    }

    public void clampCommanderWarAssignment() {
        int max = getMaxAssignableCommanders();
        int current = getWarAssignedRoleCount(GameConstants.ROLE_COMMANDER);
        if (current > max) {
            setWarAssignedRoleCount(GameConstants.ROLE_COMMANDER, max);
        }
    }

    public Map<AntRole, Integer> getWarAssignedRoleCounts() {
        return warAssignedRoleCounts;
    }

    public void copyPeaceRolesToWar() {
        for (AntRole role : GameConstants.getAntRoles()) {
            if (GameConstants.isWarEconomyExclusiveRole(role)) {
                continue;
            }
            warAssignedRoleCounts.put(role, peaceAssignedRoleCounts.getOrDefault(role, 0));
        }
        copyPeaceRoleSubtypeAllowsToWar();
        markRoleAssignmentDirty();
    }

    public void copyPeaceRoleSubtypeAllowsToWar() {
        warRoleDisallowedSubtypes.clear();
        for (Map.Entry<AntRole, Set<Integer>> entry : peaceRoleDisallowedSubtypes.entrySet()) {
            if (GameConstants.isWarEconomyExclusiveRole(entry.getKey())) {
                continue;
            }
            warRoleDisallowedSubtypes.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
    }

    public boolean isRoleSubtypeAllowed(AntRole role, AntSubtype subtype) {
        return isRoleSubtypeAllowed(role, subtype, usesWarEconomyRoles());
    }

    public boolean isPeaceRoleSubtypeAllowed(AntRole role, AntSubtype subtype) {
        return isRoleSubtypeAllowed(role, subtype, false);
    }

    public boolean isWarRoleSubtypeAllowed(AntRole role, AntSubtype subtype) {
        return isRoleSubtypeAllowed(role, subtype, true);
    }

    private boolean isRoleSubtypeAllowed(AntRole role, AntSubtype subtype, boolean war) {
        if (role == null || subtype == null || subtype.isNone()) {
            return true;
        }
        if (role.isSubtypeForcedAllowed(subtype)) {
            return true;
        }
        Map<AntRole, Set<Integer>> disallowed = war ? warRoleDisallowedSubtypes : peaceRoleDisallowedSubtypes;
        Set<Integer> blocked = disallowed.get(role);
        return blocked == null || !blocked.contains(subtype.getId());
    }

    public void setRoleSubtypeAllowed(AntRole role, AntSubtype subtype, boolean allowed) {
        if (usesWarEconomyRoles()) {
            setWarRoleSubtypeAllowed(role, subtype, allowed);
        } else {
            setPeaceRoleSubtypeAllowed(role, subtype, allowed);
        }
    }

    public void setPeaceRoleSubtypeAllowed(AntRole role, AntSubtype subtype, boolean allowed) {
        setRoleSubtypeAllowed(role, subtype, allowed, peaceRoleDisallowedSubtypes);
    }

    public void setWarRoleSubtypeAllowed(AntRole role, AntSubtype subtype, boolean allowed) {
        setRoleSubtypeAllowed(role, subtype, allowed, warRoleDisallowedSubtypes);
    }

    private void setRoleSubtypeAllowed(AntRole role, AntSubtype subtype, boolean allowed,
            Map<AntRole, Set<Integer>> disallowedByRole) {
        if (role == null || subtype == null || subtype.isNone()) {
            return;
        }
        if (role.isSubtypeForcedAllowed(subtype)) {
            allowed = true;
        }
        Set<Integer> blocked = disallowedByRole.computeIfAbsent(role, ignored -> new HashSet<>());
        boolean changed;
        if (allowed) {
            changed = blocked.remove(subtype.getId());
            if (blocked.isEmpty()) {
                disallowedByRole.remove(role);
            }
        } else {
            changed = blocked.add(subtype.getId());
        }
        if (changed) {
            markRoleAssignmentDirty();
        }
    }

    public Set<Integer> getAllowedSpecialSubtypeIdsForRole(AntRole role) {
        return getAllowedSpecialSubtypeIdsForRole(role, usesWarEconomyRoles());
    }

    public Set<Integer> getPeaceAllowedSpecialSubtypeIdsForRole(AntRole role) {
        return getAllowedSpecialSubtypeIdsForRole(role, false);
    }

    public Set<Integer> getWarAllowedSpecialSubtypeIdsForRole(AntRole role) {
        return getAllowedSpecialSubtypeIdsForRole(role, true);
    }

    private Set<Integer> getAllowedSpecialSubtypeIdsForRole(AntRole role, boolean war) {
        Set<Integer> allowed = new HashSet<>();
        for (AntSubtype subtype : GameConstants.getAntSubtypes()) {
            if (subtype.isNone()) {
                continue;
            }
            if (isRoleSubtypeAllowed(role, subtype, war)) {
                allowed.add(subtype.getId());
            }
        }
        return AntSubtypeService.effectiveAllowedSubtypeIds(role, allowed);
    }

    public Map<String, Integer> flattenPeaceRoleDisallowedSubtypes() {
        return AntSubtypeService.flattenRoleDisallowedSubtypes(peaceRoleDisallowedSubtypes);
    }

    public Map<String, Integer> flattenWarRoleDisallowedSubtypes() {
        return AntSubtypeService.flattenRoleDisallowedSubtypes(warRoleDisallowedSubtypes);
    }

    public void refreshRoleAssignmentForWarState(Engine engine) {
        invalidateActiveRoleCountCache();
        runRoleAssignment(engine);
        ColonyMilitaryService.refreshColonyMilitaryPower(this);
        Dynasty owner = getDynasty();
        if (owner != null) {
            ColonyMilitaryService.refreshDynastyMilitaryPower(owner);
        }
    }

    public void invalidateActiveRoleCountCache() {
        activeRoleCountCache = null;
    }

    public void markRoleAssignmentDirty() {
        roleAssignmentDirty = true;
    }

    public boolean isRoleAssignmentDirty() {
        return roleAssignmentDirty;
    }

    private int roleBearingPopulationKey() {
        return listSize(getWorkers())
                + 31 * listSize(getSoldiers())
                + 961 * listSize(getMajors())
                + 29791 * listSize(getPrincesses())
                + 923521 * listSize(getQueens());
    }

    private static int listSize(List<Ant> list) {
        return list != null ? list.size() : 0;
    }

    private boolean needsRoleAssignment() {
        return roleAssignmentDirty || roleBearingPopulationKey() != lastRoleAssignmentPopKey;
    }

    private void clearRoleAssignmentDirty() {
        roleAssignmentDirty = false;
        lastRoleAssignmentPopKey = roleBearingPopulationKey();
    }

    public void invalidateAffordableAlertCache() {
        affordableResearchCached = null;
        affordableBuildingCacheValid = false;
        affordableBuildingCached = null;
    }

    public boolean hasAffordableResearch() {
        if (affordableResearchCached == null) {
            affordableResearchCached = computeHasAffordableResearch();
        }
        return affordableResearchCached;
    }

    public Building getAffordableBuildingForAlert() {
        if (!affordableBuildingCacheValid) {
            affordableBuildingCached = computeAffordableBuildingForAlert();
            affordableBuildingCacheValid = true;
        }
        return affordableBuildingCached;
    }

    private boolean computeHasAffordableResearch() {
        if (!hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) {
            return false;
        }
        int researchPoints = getResearchPoints();
        for (Upgrade upgrade : GameUnlocks.getUpgrades()) {
            if (!hasUpgrade(upgrade) && upgrade.getCost() > 0
                    && (upgrade.getRequirement() == null || hasUpgrade(upgrade.getRequirement()))
                    && GameUnlocks.meetsExtraAutomationPrerequisites(getDynasty(), upgrade)
                    && upgrade.isAvailableFor(getDynasty())
                    && researchPoints >= upgrade.getCost()) {
                return true;
            }
        }
        return false;
    }

    private Building computeAffordableBuildingForAlert() {
        if (!hasUpgrade(GameUnlocks.ABILITY_BUILD) || currentBuildingProject != null) {
            return null;
        }
        int minerals = getMinerals();
        int resins = getResins();
        int plants = getPlants();
        for (Building building : GameUnlocks.getBuildings()) {
            if (!hasBuilding(building) && minerals >= building.getMineralCost()
                    && resins >= building.getResinCost()
                    && plants >= building.getPlantCost()
                    && (building.getRequirement() == null || hasBuilding(building.getRequirement()))
                    && building.isAvailableFor(getDynasty())
                    && GameUnlocks.meetsBuildingUnlockRequirement(this, building)) {
                return building;
            }
        }
        return null;
    }

    public int getActiveRoleCount(AntRole role) {
        if (activeRoleCountCache == null) {
            rebuildActiveRoleCountCache();
        }
        return activeRoleCountCache.getOrDefault(role, 0);
    }

    private void rebuildActiveRoleCountCache() {
        Map<AntRole, Integer> counts = new HashMap<>();
        for (List<Ant> group : antGroups.values()) {
            for (Ant ant : group) {
                if (ant.isAlive() && !ant.isOnTrade() && ant.getRole() != null) {
                    counts.merge(ant.getRole(), 1, Integer::sum);
                }
            }
        }
        activeRoleCountCache = counts;
    }
    
    public float getHatchRateWorker() { return hatchRateWorker; }
    public void setHatchRateWorker(float hatchRateWorker) { this.hatchRateWorker = hatchRateWorker; }
    public float getHatchRateSoldier() { return hatchRateSoldier; }
    public void setHatchRateSoldier(float hatchRateSoldier) { this.hatchRateSoldier = hatchRateSoldier; }
    public float getHatchRateMajor() { return hatchRateMajor; }
    public void setHatchRateMajor(float hatchRateMajor) { this.hatchRateMajor = hatchRateMajor; }
    public float getHatchRateDrone() { return hatchRateDrone; }
    public void setHatchRateDrone(float hatchRateDrone) { this.hatchRateDrone = hatchRateDrone; }
    public float getHatchRatePrincess() { return hatchRatePrincess; }
    public void setHatchRatePrincess(float hatchRatePrincess) { this.hatchRatePrincess = hatchRatePrincess; }
    
    public float getHatchRate(AntType type) {
        if (type == GameConstants.TYPE_WORKER) return hatchRateWorker;
        if (type == GameConstants.TYPE_SOLDIER) return hatchRateSoldier;
        if (type == GameConstants.TYPE_MAJOR) return hatchRateMajor;
        if (type == GameConstants.TYPE_DRONE) return hatchRateDrone;
        if (type == GameConstants.TYPE_PRINCESS) return hatchRatePrincess;
        return 0f;
    }
    public void setHatchRate(AntType type, float rate) {
        if (type == GameConstants.TYPE_WORKER) this.hatchRateWorker = rate;
        else if (type == GameConstants.TYPE_SOLDIER) this.hatchRateSoldier = rate;
        else if (type == GameConstants.TYPE_MAJOR) this.hatchRateMajor = rate;
        else if (type == GameConstants.TYPE_DRONE) this.hatchRateDrone = rate;
        else if (type == GameConstants.TYPE_PRINCESS) this.hatchRatePrincess = rate;
    }

    public Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> getSubtypeHatchRates() {
        return subtypeHatchRates;
    }

    public void setSubtypeHatchRates(Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> subtypeHatchRates) {
        this.subtypeHatchRates = AntSubtypeService.deepCopyRates(subtypeHatchRates);
    }

    public float getSubtypeHatchRate(AntType type, AntSubtypeSlot slot, int digit) {
        if (type == null) {
            return digit == AntSubtype.DIGIT_NONE ? 100f : 0f;
        }
        Map<AntSubtypeSlot, Map<Integer, Float>> typeRates = subtypeHatchRates.get(type);
        if (typeRates == null) {
            return digit == AntSubtype.DIGIT_NONE ? 100f : 0f;
        }
        Map<Integer, Float> slotRates = typeRates.get(slot);
        if (slotRates == null) {
            return digit == AntSubtype.DIGIT_NONE ? 100f : 0f;
        }
        return slotRates.getOrDefault(digit, 0f);
    }

    public void setSubtypeHatchRate(AntType type, AntSubtypeSlot slot, int digit, float rate) {
        if (type == null) {
            return;
        }
        subtypeHatchRates.computeIfAbsent(type, ignored -> new EnumMap<>(AntSubtypeService.defaultRatesForType()))
                .computeIfAbsent(slot, ignored -> new HashMap<>())
                .put(digit, rate);
    }

    public int getTotalDeaths () { return totalDeaths; }
    public void setTotalDeaths (int totalDeaths) { this.totalDeaths = totalDeaths; }
    
    // --- Room Bounds Getters/Setters ---
    public void setRoomBounds(Rectangle entrance, Rectangle storage, Rectangle farm, Rectangle nursery, Rectangle royal, Rectangle rancher, Rectangle graver, Rectangle breeder, Rectangle transit) {
        this.entranceBounds = entrance;
        this.storageBounds = storage;
        this.farmBounds = farm;
        this.nurseryBounds = nursery;
        this.royalBounds = royal;
        this.rancherBounds = rancher;
        this.graverBounds = graver;
        this.breederBounds = breeder;
        this.transitBounds = transit;
    }

    public void setRoomBounds(Rectangle entrance, Rectangle storage, Rectangle farm, Rectangle nursery, Rectangle royal, Rectangle rancher, Rectangle graver) {
        setRoomBounds(entrance, storage, farm, nursery, royal, rancher, graver, null, null);
    }

    public Rectangle getEntranceBounds() { return entranceBounds; }
    public Rectangle getStorageBounds() { return storageBounds; }
    public Rectangle getFarmBounds() { return farmBounds; }
    public Rectangle getNurseryBounds() { return nurseryBounds; }
    public Rectangle getRoyalBounds() { return royalBounds; }
    public Rectangle getRancherBounds() { return rancherBounds; }
    public Rectangle getGraverBounds() { return graverBounds; }
    public Rectangle getInsectPenBounds() { return insectPenBounds; }
    public void setInsectPenBounds(Rectangle insectPenBounds) { this.insectPenBounds = insectPenBounds; }
    public Rectangle getBreederBounds() { return breederBounds; }
    public Rectangle getTransitBounds() { return transitBounds; }    
    public Rectangle getTargetRoomForAnt(Ant ant) {
        AntRole role = ant.getRole();
        if (ant.getAntType() == GameConstants.TYPE_QUEEN) return royalBounds;
        if (ant.getAntType() == GameConstants.TYPE_EGG || ant.getAntType() == GameConstants.TYPE_LARVA || ant.getAntType() == GameConstants.TYPE_PUPA) return nurseryBounds;        
        if (ant.getAntType() == GameConstants.TYPE_DRONE) return breederBounds;
        if (role == GameConstants.ROLE_NURSE) return nurseryBounds;
        if (role == GameConstants.ROLE_FARMER) return farmBounds;
        if (role == GameConstants.ROLE_RANCHER && rancherBounds != null) return rancherBounds;
        if (role == GameConstants.ROLE_GRAVER && graverBounds != null) return graverBounds;
        if (role == GameConstants.ROLE_BREEDER && breederBounds != null) return breederBounds;
        if (role == GameConstants.ROLE_ASSISTANT && royalBounds != null) return royalBounds;
        if (role == GameConstants.ROLE_CATCHER && insectPenBounds != null) return insectPenBounds;
        return null;
    }

    // --- Public getters for services ---
    public ColonyStatsService getStatsService() { return this.statsService; }
    public ColonySpatialService getSpatialService() { return this.spatialService; }
    public ColonySourceService getSourceService() { return this.sourceService; }
    public ColonyPathfindingService getPathfindingService() { return this.pathfindingService; }
    public ColonyLabourService getLabourService() { return this.labourService; }
    public ColonyPopulationService getPopulationService() { return this.populationService; }
    public ColonyDeathService getDeathService() { return this.deathService; }
    public ColonyPhysicsService getPhysicsService() { return this.physicsService; }
    public ColonyLocationService getLocationService() { return this.locationService; }
    public ColonyAutomationService getAutomationService() { return this.automationService; }
    public ColonyResourceService getResourceService() { return this.resourceService; }
    public ColonyStarterService getStarterService() { return this.starterService; }

    public int getTotalConsumption(){ return statsService.getTotalConsumption(this); }
    public int getTotalProduction(){ return statsService.getTotalProduction(this); }
    public int getPlantsCapacity() { return statsService.getPlantsCapacity(this); }
    public int getMushroomsCapacity() { return statsService.getMushroomsCapacity(this); }
    public int getProteinCapacity() { return statsService.getProteinCapacity(this); }
    public int getWaterCapacity() { return statsService.getWaterCapacity(this); }
    public int getSyrupsCapacity() { return statsService.getSyrupsCapacity(this); }
    public int getResinsCapacity() { return statsService.getResinsCapacity(this); }
    public int getMineralsCapacity() { return statsService.getMineralsCapacity(this); }
    public int getEggsCapacity() { return statsService.getEggsCapacity(this); }
    public int getQueensCapacity() { return statsService.getQueensCapacity(this); }
    public int getAphidCapacity() { return statsService.getAphidCapacity(this); }
    public int getResearchSpeed() { return statsService.getResearchSpeed(this); }
    public int getGrowthTime() { return statsService.getGrowthTime(this); }
    public float getLayingRate() { return statsService.getLayingRate(this); }
    public float getConversionRate() { return statsService.getConversionRate(this); }
    public float getNursingRate() { return statsService.getNursingRate(this); }
    public float getGravingRate() { return statsService.getGravingRate(this); }
    public float getCollectingRate() { return statsService.getCollectingRate(this); }
    public float getParasiteDetection() { return statsService.getParasiteDetection(this); }
    public int getBaseHealth() { return statsService.getBaseHealth(this); }
    public int getBaseTempRes() { return statsService.getBaseTempRes(this); }
    public int getBaseRegen() { return statsService.getBaseRegen(this); }
    public int getBaseConsumption() { return statsService.getBaseConsumption(this); }
    public int getBaseAttack() { return statsService.getBaseAttack(this); }
    public int getBaseAttackSpeed() { return statsService.getBaseAttackSpeed(this); }
    public int getBaseDefense() { return statsService.getBaseDefense(this); }
    public int getBaseSpeed() { return statsService.getBaseSpeed(this); }
    public int getSourceCapacity() { return statsService.getSourceCapacity(this); }
    public double getConstructionEfficiency() { return statsService.getConstructionEfficiency(this); }


    // --- Simulation Logic Methods ---
    public void runRoleAssignment() {
        runRoleAssignment(null);
    }

    public void runRoleAssignment(Engine engine) {
        populationService.runRoleAssignment(this, engine);
        clearRoleAssignmentDirty();
    }

    public void runRoleAssignmentIfNeeded(Engine engine) {
        if (needsRoleAssignment()) {
            runRoleAssignment(engine);
        }
    }
    public void runHatching(){ populationService.runHatching(this); }
    public void rankUp() { populationService.rankUp(this); }
    public void runLaying() { labourService.runLaying(this); }
    public void runAging(){ populationService.runAging(this); }
    public void runNursing() { labourService.runNursing(this); }
    public void runNuptial(World world, Hex currentHex) { labourService.runNuptial(this, world, currentHex); }
    public void runEating(Temperature currentTemp){ populationService.runEating(this, currentTemp); }
    public void runGraveKeeping() { labourService.runGraveKeeping(this); }
    public void runResearch() { labourService.runResearch(this); }
    public void runBuilding() { labourService.runBuilding(this); }
    public void runContamination() { populationService.runContamination(this); }
    public void runCollecting() { labourService.runCollecting(this); }
    public void runConverting() { labourService.runConverting(this); }
    public void runRanching() { labourService.runRanching(this); }
    public void runHerding(Biome biome) {
        labourService.runCaughtBugs(this, biome);
    }
    public void runScoutting(Biome biome, Hex currentHex) { labourService.runScoutting(this, biome, currentHex); }
    public void runComposting() { labourService.runComposting(this); }
    public void runParasitation(Biome biome, Season season) {
        populationService.runParasitation(this, biome, season);
    }
    public void runPolicing() { labourService.runPolicing(this); }
    
    public int getNuptialFlightCost() {
        int base = GameNumbers.FORCED_FLIGHT_BASE_COST;
        int colonyCount = (dynasty != null) ? dynasty.getColonies().size() : 1;
        long scaledCost = (long) base * (1L + (long) colonyCount * colonyCount);
        
        if (scaledCost > Integer.MAX_VALUE - 100000) {
            return Integer.MAX_VALUE - 100000;
        }
        
        return (int) scaledCost;
    }

    public void forceNuptialFlight(World world, Hex currentHex) {
        if (!hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT)) return;
        
        int cost = getNuptialFlightCost();
        Engine eng = (getDynasty() != null && getDynasty().getOwningWorld() != null)
                ? getDynasty().getOwningWorld().getEngine()
                : (world != null ? world.getEngine() : null);
        boolean free = eng != null && eng.isFreeAbilities();
        if (!free && getResearchPoints() < cost) return;

        if (!ColonyLabourService.meetsNuptialRequirements(this)) {
            logEvent(ColonyLogPrefixes.INFO + " " + LanguageStrings.get(LanguageStrings.LOG_FORCE_FLIGHT_BLOCKED));
            return;
        }

        if (!free) {
            addResearchPoints(-cost);
        } else if (eng != null) {
            eng.applySandboxTaintToActiveWorld();
        }
        this.labourService.runNuptial(this, world, currentHex);
    }

    public boolean isPheromoneStormActive() {
        return hasActiveLoyaltyModifier(GameConstants.LOYALTY_MODIFIER_PHEROMONE_STORM.getNameKey());
    }

    public int getPheromoneStormMonthsRemaining() {
        return GameNumbers.daysToMonthsCeil(
                getLoyaltyModifierRemainingDays(GameConstants.LOYALTY_MODIFIER_PHEROMONE_STORM.getNameKey()));
    }

    public boolean isRecentlyConquered() {
        return hasActiveLoyaltyModifier(GameConstants.LOYALTY_MODIFIER_RECENTLY_CONQUERED.getNameKey());
    }

    public int getRecentlyConqueredMonthsRemaining() {
        return GameNumbers.daysToMonthsCeil(
                getLoyaltyModifierRemainingDays(GameConstants.LOYALTY_MODIFIER_RECENTLY_CONQUERED.getNameKey()));
    }

    public void setRecentlyConqueredMonthsRemaining(int months) {
        applyLoyaltyModifier(GameConstants.LOYALTY_MODIFIER_RECENTLY_CONQUERED, GameNumbers.monthsToDays(months));
    }

    public boolean isRecentlyIntegrated() {
        return hasActiveLoyaltyModifier(GameConstants.LOYALTY_MODIFIER_RECENTLY_INTEGRATED.getNameKey());
    }

    public int getRecentlyIntegratedMonthsRemaining() {
        return GameNumbers.daysToMonthsCeil(
                getLoyaltyModifierRemainingDays(GameConstants.LOYALTY_MODIFIER_RECENTLY_INTEGRATED.getNameKey()));
    }

    public void setRecentlyIntegratedMonthsRemaining(int months) {
        applyLoyaltyModifier(GameConstants.LOYALTY_MODIFIER_RECENTLY_INTEGRATED, GameNumbers.monthsToDays(months));
    }

    public boolean hasActiveLoyaltyModifier(String modifierKey) {
        if (modifierKey == null) {
            return false;
        }
        Integer remaining = loyaltyModifierRemainingDays.get(modifierKey);
        return remaining != null
                && (remaining == GameNumbers.MODIFIER_PERMANENT || remaining > 0);
    }

    public int getLoyaltyModifierRemainingDays(String modifierKey) {
        return loyaltyModifierRemainingDays.getOrDefault(modifierKey, 0);
    }

    public void applyLoyaltyModifier(ColonyLoyaltyModifier modifier) {
        if (modifier == null) {
            return;
        }
        applyLoyaltyModifier(modifier, GameNumbers.initialModifierRemainingDays(modifier));
    }

    public void applyLoyaltyModifier(ColonyLoyaltyModifier modifier, int remainingDays) {
        if (modifier == null || remainingDays == 0) {
            return;
        }
        loyaltyModifierRemainingDays.put(modifier.getNameKey(), remainingDays);
    }

    public Map<String, Integer> copyLoyaltyModifierRemainingDays() {
        return new HashMap<>(loyaltyModifierRemainingDays);
    }

    private void migrateLegacyLoyaltyModifierFields() {
        if (pheromoneStormMonthsRemaining > 0) {
            applyLoyaltyModifier(
                    GameConstants.LOYALTY_MODIFIER_PHEROMONE_STORM,
                    GameNumbers.monthsToDays(pheromoneStormMonthsRemaining));
        }
        if (recentlyConqueredMonthsRemaining > 0) {
            applyLoyaltyModifier(
                    GameConstants.LOYALTY_MODIFIER_RECENTLY_CONQUERED,
                    GameNumbers.monthsToDays(recentlyConqueredMonthsRemaining));
        }
        if (recentlyIntegratedMonthsRemaining > 0) {
            applyLoyaltyModifier(
                    GameConstants.LOYALTY_MODIFIER_RECENTLY_INTEGRATED,
                    GameNumbers.monthsToDays(recentlyIntegratedMonthsRemaining));
        }
        pheromoneStormMonthsRemaining = 0;
        recentlyConqueredMonthsRemaining = 0;
        recentlyIntegratedMonthsRemaining = 0;
    }

    public void tickLoyaltyModifierDays() {
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : loyaltyModifierRemainingDays.entrySet()) {
            int remaining = entry.getValue();
            if (remaining == GameNumbers.MODIFIER_PERMANENT) {
                continue;
            }
            if (remaining <= 1) {
                expired.add(entry.getKey());
            } else {
                entry.setValue(remaining - 1);
            }
        }
        for (String key : expired) {
            loyaltyModifierRemainingDays.remove(key);
            if (GameConstants.LOYALTY_MODIFIER_PHEROMONE_STORM.getNameKey().equals(key)) {
                logEvent(ColonyLogPrefixes.INFO + " "
                        + LanguageStrings.get(LanguageStrings.LOG_PHEROMONE_STORM_ENDED));
            }
        }
    }

    public boolean isCreatineDietActive() {
        return creatineDietMonthsRemaining > 0;
    }

    public int getCreatineDietMonthsRemaining() {
        return creatineDietMonthsRemaining;
    }

    public boolean activatePheromoneStorm() {
        if (!hasUpgrade(GameUnlocks.ABILITY_PHEROMONE_STORM) || isPheromoneStormActive()) {
            return false;
        }
        Engine eng = (getDynasty() != null && getDynasty().getOwningWorld() != null)
                ? getDynasty().getOwningWorld().getEngine()
                : null;
        boolean free = eng != null && eng.isFreeAbilities();
        int cost = GameNumbers.PHEROMONE_STORM_SYRUP_COST;
        if (!free && getSyrups() < cost) {
            return false;
        }
        if (!free) {
            setSyrups(getSyrupsPrecise() - cost);
        } else {
            eng.applySandboxTaintToActiveWorld();
        }
        applyLoyaltyModifier(GameConstants.LOYALTY_MODIFIER_PHEROMONE_STORM);
        logEvent(ColonyLogPrefixes.INFO + " "
                + LanguageStrings.format(LanguageStrings.LOG_PHEROMONE_STORM_STARTED_FMT,
                        GameConstants.LOYALTY_MODIFIER_PHEROMONE_STORM.getLoyaltyDelta(),
                        GameNumbers.PHEROMONE_STORM_DURATION_MONTHS));
        return true;
    }

    public boolean activateCreatineDiet() {
        if (!hasUpgrade(GameUnlocks.ABILITY_CREATINE_DIET) || isCreatineDietActive()) {
            return false;
        }
        Engine eng = (getDynasty() != null && getDynasty().getOwningWorld() != null)
                ? getDynasty().getOwningWorld().getEngine()
                : null;
        boolean free = eng != null && eng.isFreeAbilities();
        int cost = GameNumbers.CREATINE_DIET_PROTEIN_COST;
        if (!free && getProtein() < cost) {
            return false;
        }
        if (!free) {
            setProtein(getProteinPrecise() - cost);
        } else {
            eng.applySandboxTaintToActiveWorld();
        }
        creatineDietMonthsRemaining = GameNumbers.CREATINE_DIET_DURATION_MONTHS;
        logEvent(ColonyLogPrefixes.INFO + " "
                + LanguageStrings.format(LanguageStrings.LOG_CREATINE_DIET_STARTED_FMT,
                        GameNumbers.CREATINE_DIET_DURATION_MONTHS));
        return true;
    }

    private void tickAbilityDurations() {
        if (creatineDietMonthsRemaining > 0) {
            creatineDietMonthsRemaining--;
            if (creatineDietMonthsRemaining == 0) {
                logEvent(ColonyLogPrefixes.INFO + " " + LanguageStrings.get(LanguageStrings.LOG_CREATINE_DIET_ENDED));
            }
        }
    }
    
    public void runPhysics(Dimension activeDimension) {
        runPhysics(activeDimension, null);
    }

    public void runPhysics(Dimension activeDimension, Rectangle viewportPanelBounds) {
        if (!this.isActive) {
            return;
        }
        physicsStepSequence++;
        this.lastPhysicsViewport = viewportPanelBounds;
        this.lastPhysicsDimension = activeDimension;
        convoyTransitService.runConvoyPhysics(this);
        physicsService.runPhysics(this, activeDimension, viewportPanelBounds, physicsStepSequence);
    }

    // --- Job Schedulers ---
    public void runMinutelyJobs() {
        if (this.runsFullSimulation()) {
            this.runConverting();
            physicsService.tickAntSpriteAnimMinutes(this);
        }
    }

    public void runHourlyJobs(Biome biome, Engine engine) {
        if (this.age < 7) {
            return;
        }

        Season season = engine != null && engine.getWorld() != null ? engine.getWorld().getSeason() : null;

        if (this.runsFullSimulation()) {
            if (this.automationEnabled) {
                this.automationService.runAutomation(this, biome, season);
            }
            this.runRoleAssignmentIfNeeded(engine);
            this.runLaying();
            this.runResearch();
            this.runRanching();
            this.runBuilding();
            this.labourService.runTunnelConstruction(this);
            this.runCollecting();
            physicsService.rollAntSpriteAnimHourly(this, lastPhysicsDimension, lastPhysicsViewport);
        } else {
            if (this.automationEnabled) {
                this.automationService.runAutomation(this, biome, season);
            }
            this.runRoleAssignmentIfNeeded(engine);
            this.labourService.runTunnelConstruction(this);
            ColonyJobRules.runHourlyLite(this, biome);
        }
    }

    public void runDailyJobs(Temperature currentTemp, Biome biome, Hex currentHex) {
        if (this.age < 7) {
            this.age++;
            if (this.age >= 7) {
                matureColony();
            }
            ColonyMilitaryService.refreshColonyMilitaryPower(this);
            return;
        }

        if (this.getQueens().isEmpty()) {
            this.daysWithoutQueen++;
            if (this.daysWithoutQueen == 1 || this.daysWithoutQueen == 6) {
                this.logEvent(ColonyLogPrefixes.WARNING + " "
                    + LanguageStrings.format(LanguageStrings.LOG_WARNING_NO_QUEEN_FMT, this.daysWithoutQueen));
            }
        } else {
            this.daysWithoutQueen = 0;
        }

        if (this.automationEnabled) {
            this.automationService.runDailyAutomation(this, currentHex);
        } else if (this.autoBuildEnabled) {
            this.automationService.runAutoBuild(this);
        }

        tickLoyaltyModifierDays();

        if (this.runsFullSimulation()) {
            this.rankUp();
            this.runEating(currentTemp); 
            this.runHatching();
            this.runAging();
            this.runNursing();
            this.runComposting();
            this.runGraveKeeping();
            this.runHerding(biome); 
            this.runScoutting(biome, currentHex);
            this.runContamination(); 
            this.runPolicing(); 
        } else {
            ColonyJobRules.runDailyLite(this, currentTemp, biome, currentHex);
        }

        this.age++;
        ColonyMilitaryService.refreshColonyMilitaryPower(this);
    }

    public void matureColony() {
        starterService.matureColony(this);
    }
    
    public void configureWorker(int index, AntRole role, Dimension dim) {
        if (this.getWorkers() != null && index < this.getWorkers().size()) {
            Ant worker = this.getWorkers().get(index);
            worker.setRole(role);
            worker.setDimension(dim);
        }
    }

    public void runMonthlyJobs(Season season, Biome biome) {
        tickAbilityDurations();
        if (this.runsFullSimulation()) {
            this.runParasitation(biome, season);
            getBugHandlingService().runMonthlyParasiticMites(this, biome, season);
        } else {
            ColonyJobRules.runMonthlyLite(this, biome, season);
        }
    }

    public void runYearlyJobs(World world, Hex currentHex) {
        this.runNuptial(world, currentHex);
    }
    
    public void refreshAntStats() {
        for (List<Ant> list : antGroups.values()) {
            for (Ant a : list) {
                a.updateStatsFromColony(this);
            }
        }
    }
}
