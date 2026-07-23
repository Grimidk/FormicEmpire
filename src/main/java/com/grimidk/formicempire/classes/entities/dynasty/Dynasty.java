package com.grimidk.formicempire.classes.entities.dynasty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.Color;

import com.grimidk.formicempire.classes.constants.dynasty.Rank;
import com.grimidk.formicempire.classes.constants.dynasty.colony.CityTitle;
import com.grimidk.formicempire.classes.constants.dynasty.DiplomaticReputationModifier;
import com.grimidk.formicempire.classes.constants.dynasty.DynastyTitle;
import com.grimidk.formicempire.classes.constants.dynasty.PactRequestIncomingPolicy;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.GeneticIntegrityModifier;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyAiService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyAutomationService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyLogisticsAutomationService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyStarterService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyStatService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyTradeService;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEvent;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEventType;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public class Dynasty {

    private final int id;
    private String name;
    private String themeBase;
    private String titleKey;
    private boolean isPlayer;
    private boolean wildDynasty;
    private AntSpecies species;
    private int researchPoints;
    private int totalNuptialFlights;
    private int diplomatsSentTotal;
    private Rank rank;
    private Color color;
    
    // --- State Flags ---
    private boolean isDefeated; 
    
    // Global Data
    private boolean defaultAutomationEnabled;
    private boolean defaultAutoBuildEnabled;
    private boolean autoDiplomacyEnabled;
    private boolean defaultAutoTunnelsEnabled;
    private PactRequestIncomingPolicy pactRequestIncomingPolicy;
    private int lastIncomingPactRequestWorldDay;
    private transient boolean pactRequestPromptOpen;
    private final Set<Upgrade> unlockedUpgrades;
    private final Set<Integer> announcedRankIds;
    private final transient List<Synergy> pendingSynergyAlerts = new ArrayList<>();
    private final List<Colony> colonies;
    private final List<Tunnel> tunnels;
    private final Map<String, Integer> globalDeathStatistics;
    private final List<Integer> absorbedDynastyIds;
    private final List<Integer> defeatedSpeciesIds;
    private final Set<Assimilation> completedAssimilations;
    private Assimilation currentAssimilation;
    private double assimilationProgress;
    private Colony capital;
    private int militaryPower;
    private int activeMilitaryPower;
    private int reserveMilitaryPower;
    private final Map<Integer, Integer> diplomaticReputations;
    private final Map<Integer, Map<String, Integer>> diplomaticModifierRemainingDays;
    private final List<Integer> crossDynastyTradeRepGrantedIds;
    private final Set<Integer> pendingPactRequestFromIds;
    private final Set<Integer> pendingWarDeclarationFromIds;
    private final List<PendingNpcWarAlert> pendingNpcWarAlerts;
    private final List<PendingIntegrationVassalWarAlert> pendingIntegrationVassalWarAlerts;
    private final List<PendingWarStageResultAlert> pendingWarStageResultAlerts;
    private final List<Integer> pendingIntegrationCompletedTargetIds;
    private final Map<Integer, Integer> pactBrokenAtWorldMonth;
    private final Map<Integer, Integer> pactRequestDeclinedAtWorldMonth;
    private final Map<Integer, Integer> tradeRequestDeclinedAtWorldMonth;
    private final Map<Integer, Integer> wasAtWarPeacedAtWorldMonth;
    private final Map<Integer, Integer> geneticExchangeGrantedAtWorldMonth;
    private boolean legacyTimedModifiersMigrated;
    private final List<CrossDynastyTradeProposal> pendingTradeProposals;
    private int forcedFlightCooldownDays;
    private final Map<Integer, Integer> diplomatSupportToDynasty = new HashMap<>();
    private int originDynastyId;
    private int activeRebellionDynastyId;
    private int pendingRebellionResponseFromId;
    private int integrationTargetDynastyId;
    private double integrationProgressDays;
    private boolean integrationDiplomatsManual;

    // Services
    private transient DynastyAutomationService automationService;
    private transient DynastyAiService aiService;
    private transient DynastyLogisticsAutomationService logisticsAutomationService;
    private transient DynastyStarterService starterService;
    private transient DynastyStatService statService;
    private transient DynastyTradeService tradeService;
    private transient DynastyDiplomacyService diplomacyService;
    private transient World owningWorld;

    public Dynasty(int id, String name, boolean isPlayer, AntSpecies species) {
        this(id, name, LanguageStrings.DYNASTY_TITLE_DYNASTY, isPlayer, species);
    }

    public Dynasty(int id, String name, String titleKey, boolean isPlayer, AntSpecies species) {
        this.id = id;
        this.name = name;
        this.titleKey = titleKey != null ? titleKey : LanguageStrings.DYNASTY_TITLE_DYNASTY;
        this.isPlayer = isPlayer;
        this.species = species;
        this.colonies = new ArrayList<>();
        this.unlockedUpgrades = new HashSet<>();
        this.announcedRankIds = new LinkedHashSet<>();
        seedStartingAnnouncedRanks();
        this.tunnels = new ArrayList<>();
        this.globalDeathStatistics = new ConcurrentHashMap<>();
        this.absorbedDynastyIds = new ArrayList<>();
        this.defeatedSpeciesIds = new ArrayList<>();
        this.completedAssimilations = new HashSet<>();
        this.researchPoints = 0;
        this.totalNuptialFlights = 0;
        this.diplomatsSentTotal = 0;
        this.rank = GameConstants.RANK_ANT;
        this.isDefeated = false;
        this.currentAssimilation = null;
        this.assimilationProgress = 0;
        this.defaultAutomationEnabled = false;
        this.defaultAutoBuildEnabled = false;
        this.autoDiplomacyEnabled = false;
        this.defaultAutoTunnelsEnabled = false;
        this.pactRequestIncomingPolicy = PactRequestIncomingPolicy.MANUAL;
        this.lastIncomingPactRequestWorldDay = -1;
        this.pactRequestPromptOpen = false;
        this.diplomaticReputations = new HashMap<>();
        this.diplomaticModifierRemainingDays = new HashMap<>();
        this.crossDynastyTradeRepGrantedIds = new ArrayList<>();
        this.pendingPactRequestFromIds = new LinkedHashSet<>();
        this.pendingWarDeclarationFromIds = new LinkedHashSet<>();
        this.pendingNpcWarAlerts = new ArrayList<>();
        this.pendingIntegrationVassalWarAlerts = new ArrayList<>();
        this.pendingWarStageResultAlerts = new ArrayList<>();
        this.pendingIntegrationCompletedTargetIds = new ArrayList<>();
        this.pactBrokenAtWorldMonth = new HashMap<>();
        this.pactRequestDeclinedAtWorldMonth = new HashMap<>();
        this.tradeRequestDeclinedAtWorldMonth = new HashMap<>();
        this.wasAtWarPeacedAtWorldMonth = new HashMap<>();
        this.geneticExchangeGrantedAtWorldMonth = new HashMap<>();
        this.pendingTradeProposals = new ArrayList<>();
        this.forcedFlightCooldownDays = 0;
        this.originDynastyId = 0;
        this.activeRebellionDynastyId = 0;
        this.pendingRebellionResponseFromId = 0;
        this.integrationTargetDynastyId = 0;
        this.integrationProgressDays = 0;
        this.integrationDiplomatsManual = false;
        
        initializeColor();
        initializeServices();
    }
    
    public Dynasty(Savefile.SavedDynasty savedDynasty) {
        this.id = savedDynasty.id;
        if (savedDynasty.titleId > 0) {
            this.titleKey = GameConstants.getDynastyTitleById(savedDynasty.titleId).getNameKey();
        } else {
            this.titleKey = savedDynasty.titleKey != null && !savedDynasty.titleKey.isEmpty()
                    ? savedDynasty.titleKey
                    : LanguageStrings.DYNASTY_TITLE_DYNASTY;
        }
        if (savedDynasty.themeBase != null && !savedDynasty.themeBase.isEmpty()) {
            this.themeBase = savedDynasty.themeBase;
            this.name = savedDynasty.themeBase;
        } else {
            this.name = savedDynasty.name;
            if (savedDynasty.isPlayer) {
                this.themeBase = LanguageStrings.dynastyThemeBase(savedDynasty.name, this.titleKey);
            }
        }
        this.isPlayer = savedDynasty.isPlayer;
        this.wildDynasty = savedDynasty.wildDynasty || inferLegacyWildDynasty(savedDynasty);
        this.researchPoints = savedDynasty.researchPoints;
        this.totalNuptialFlights = savedDynasty.totalNuptialFlights;
        this.diplomatsSentTotal = savedDynasty.diplomatsSentTotal;
        this.isDefeated = savedDynasty.isDefeated;
        this.assimilationProgress = savedDynasty.assimilationProgress;
        this.defaultAutomationEnabled = savedDynasty.defaultAutomationEnabled;
        this.defaultAutoBuildEnabled = savedDynasty.defaultAutoBuildEnabled;
        this.autoDiplomacyEnabled = savedDynasty.autoDiplomacyEnabled;
        this.defaultAutoTunnelsEnabled = savedDynasty.defaultAutoTunnelsEnabled;
        this.pactRequestIncomingPolicy = PactRequestIncomingPolicy.fromPersistenceKey(
                savedDynasty.pactRequestIncomingPolicy);
        this.lastIncomingPactRequestWorldDay = savedDynasty.lastIncomingPactRequestWorldDay;
        this.pactRequestPromptOpen = false;
        this.militaryPower = savedDynasty.militaryPower;
        
        this.species = GameConstants.SPECIES_OMNI; 
        for(AntSpecies s : GameConstants.getSpecies()) {
            if (s.getId() == savedDynasty.speciesId) {
                this.species = s;
                break;
            }
        }

        this.colonies = new ArrayList<>();
        this.unlockedUpgrades = new HashSet<>();
        this.announcedRankIds = new LinkedHashSet<>();
        this.tunnels = new ArrayList<>();
        this.globalDeathStatistics = new ConcurrentHashMap<>();
        this.completedAssimilations = new HashSet<>();
        this.diplomaticReputations = new HashMap<>();
        this.diplomaticModifierRemainingDays = new HashMap<>();
        this.crossDynastyTradeRepGrantedIds = new ArrayList<>();
        this.pendingPactRequestFromIds = new LinkedHashSet<>();
        this.pendingWarDeclarationFromIds = new LinkedHashSet<>();
        this.pendingNpcWarAlerts = new ArrayList<>();
        this.pendingIntegrationVassalWarAlerts = new ArrayList<>();
        this.pendingWarStageResultAlerts = new ArrayList<>();
        this.pendingIntegrationCompletedTargetIds = new ArrayList<>();
        this.pactBrokenAtWorldMonth = new HashMap<>();
        this.pactRequestDeclinedAtWorldMonth = new HashMap<>();
        this.tradeRequestDeclinedAtWorldMonth = new HashMap<>();
        this.wasAtWarPeacedAtWorldMonth = new HashMap<>();
        this.geneticExchangeGrantedAtWorldMonth = new HashMap<>();
        this.pendingTradeProposals = new ArrayList<>();
        this.forcedFlightCooldownDays = 0;
        this.originDynastyId = 0;
        this.activeRebellionDynastyId = 0;
        this.pendingRebellionResponseFromId = 0;
        this.integrationTargetDynastyId = savedDynasty.integrationTargetDynastyId;
        this.integrationProgressDays = savedDynasty.integrationProgressDays;
        this.integrationDiplomatsManual = savedDynasty.integrationDiplomatsManual;
        
        this.absorbedDynastyIds = new ArrayList<>();
        if (savedDynasty.absorbedDynastyIds != null) {
            this.absorbedDynastyIds.addAll(savedDynasty.absorbedDynastyIds);
        }

        this.defeatedSpeciesIds = new ArrayList<>();
        if (savedDynasty.defeatedSpeciesIds != null) {
            this.defeatedSpeciesIds.addAll(savedDynasty.defeatedSpeciesIds);
        }

        this.currentAssimilation = null;
        if (savedDynasty.currentAssimilationId != -1) {
            for (Assimilation a : GameUnlocks.getAssimilations()) {
                if (a.getId() == savedDynasty.currentAssimilationId) {
                    this.currentAssimilation = a;
                    break;
                }
            }
        }

        if (savedDynasty.completedAssimilationIds != null) {
            for (Integer assId : savedDynasty.completedAssimilationIds) {
                for (Assimilation a : GameUnlocks.getAssimilations()) {
                    if (a.getId() == assId) {
                        this.completedAssimilations.add(a);
                        break;
                    }
                }
            }
        }
        
        if (savedDynasty.deathStatistics != null) {
            Map<String, Integer> migrated = DeathCause.migrateStatistics(savedDynasty.deathStatistics);
            if (migrated != null) {
                this.globalDeathStatistics.putAll(migrated);
            }
        }

        if (savedDynasty.diplomaticReputations != null) {
            for (Map.Entry<String, Integer> entry : savedDynasty.diplomaticReputations.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        diplomaticReputations.put(otherDynastyId,
                                GameNumbers.clampDiplomaticReputation(entry.getValue()));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (savedDynasty.diplomaticModifierRemainingDays != null) {
            for (Map.Entry<String, Map<String, Integer>> entry : savedDynasty.diplomaticModifierRemainingDays.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        for (Map.Entry<String, Integer> modifierEntry : entry.getValue().entrySet()) {
                            putDiplomaticModifierRemainingDays(
                                    otherDynastyId, modifierEntry.getKey(), modifierEntry.getValue());
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (savedDynasty.diplomaticModifierKeySets != null) {
            for (Map.Entry<String, List<String>> entry : savedDynasty.diplomaticModifierKeySets.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        for (String modifierKey : entry.getValue()) {
                            addDiplomaticModifierKey(otherDynastyId, modifierKey);
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (savedDynasty.diplomaticModifierKeys != null) {
            for (Map.Entry<String, String> entry : savedDynasty.diplomaticModifierKeys.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        addDiplomaticModifierKey(otherDynastyId, entry.getValue());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (savedDynasty.crossDynastyTradeRepGrantedIds != null) {
            this.crossDynastyTradeRepGrantedIds.addAll(savedDynasty.crossDynastyTradeRepGrantedIds);
            for (int otherDynastyId : savedDynasty.crossDynastyTradeRepGrantedIds) {
                addDiplomaticModifierKey(otherDynastyId, GameConstants.DIPLO_MODIFIER_TRADE.getNameKey());
            }
        }

        if (savedDynasty.pendingPactRequestFromIds != null) {
            this.pendingPactRequestFromIds.addAll(savedDynasty.pendingPactRequestFromIds);
        }
        if (savedDynasty.pendingWarDeclarationFromIds != null) {
            this.pendingWarDeclarationFromIds.addAll(savedDynasty.pendingWarDeclarationFromIds);
        }
        if (savedDynasty.pactBrokenAtWorldMonth != null) {
            for (Map.Entry<String, Integer> entry : savedDynasty.pactBrokenAtWorldMonth.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        pactBrokenAtWorldMonth.put(otherDynastyId, entry.getValue());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (savedDynasty.pactRequestDeclinedAtWorldMonth != null) {
            for (Map.Entry<String, Integer> entry : savedDynasty.pactRequestDeclinedAtWorldMonth.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        pactRequestDeclinedAtWorldMonth.put(otherDynastyId, entry.getValue());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (savedDynasty.tradeRequestDeclinedAtWorldMonth != null) {
            for (Map.Entry<String, Integer> entry : savedDynasty.tradeRequestDeclinedAtWorldMonth.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        tradeRequestDeclinedAtWorldMonth.put(otherDynastyId, entry.getValue());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (savedDynasty.wasAtWarPeacedAtWorldMonth != null) {
            for (Map.Entry<String, Integer> entry : savedDynasty.wasAtWarPeacedAtWorldMonth.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        wasAtWarPeacedAtWorldMonth.put(otherDynastyId, entry.getValue());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (savedDynasty.geneticExchangeGrantedAtWorldMonth != null) {
            for (Map.Entry<String, Integer> entry : savedDynasty.geneticExchangeGrantedAtWorldMonth.entrySet()) {
                try {
                    int otherDynastyId = Integer.parseInt(entry.getKey());
                    if (otherDynastyId != this.id && entry.getValue() != null) {
                        geneticExchangeGrantedAtWorldMonth.put(otherDynastyId, entry.getValue());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (savedDynasty.activeWarDynastyIds != null) {
            for (Integer otherId : savedDynasty.activeWarDynastyIds) {
                if (otherId != null && otherId != this.id
                        && !GameConstants.DIPLO_MODIFIER_WAR.getNameKey()
                                .equals(getDiplomaticModifierKey(otherId))) {
                    setDiplomaticModifierKey(otherId, GameConstants.DIPLO_MODIFIER_WAR.getNameKey());
                }
            }
        }
        if (savedDynasty.pendingTradeProposals != null) {
            for (Savefile.SavedCrossDynastyTradeProposal saved : savedDynasty.pendingTradeProposals) {
                CrossDynastyTradeProposal.Kind kind = saved.request
                        ? CrossDynastyTradeProposal.Kind.REQUEST
                        : CrossDynastyTradeProposal.Kind.OFFER;
                Map<Integer, Double> load = new HashMap<>();
                if (saved.loadByResourceId != null) {
                    for (Map.Entry<String, Double> entry : saved.loadByResourceId.entrySet()) {
                        try {
                            load.put(Integer.parseInt(entry.getKey()), entry.getValue());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                this.pendingTradeProposals.add(new CrossDynastyTradeProposal(
                        saved.fromDynastyId,
                        saved.originColonyId,
                        saved.destinationColonyId,
                        kind,
                        load,
                        true));
            }
        }
        this.forcedFlightCooldownDays = savedDynasty.forcedFlightCooldownDays;
        this.originDynastyId = savedDynasty.originDynastyId;
        this.activeRebellionDynastyId = savedDynasty.activeRebellionDynastyId;
        this.pendingRebellionResponseFromId = savedDynasty.pendingRebellionResponseFromId;
        if (savedDynasty.diplomatSupportToDynasty != null) {
            for (Map.Entry<String, Integer> entry : savedDynasty.diplomatSupportToDynasty.entrySet()) {
                diplomatSupportToDynasty.put(Integer.parseInt(entry.getKey()), entry.getValue());
            }
        }

        if (savedDynasty.unlockedUpgradeIds != null) {
            Map<Integer, Upgrade> allUpgrades = new HashMap<>();
            for (Upgrade u : GameUnlocks.getUpgrades()) {
                allUpgrades.put(u.getId(), u);
            }
            for (Integer upId : savedDynasty.unlockedUpgradeIds) {
                Upgrade u = allUpgrades.get(upId);
                if (u != null) {
                    this.unlockedUpgrades.add(u);
                }
            }
        }

        if (savedDynasty.announcedRankIds != null) {
            this.announcedRankIds.addAll(savedDynasty.announcedRankIds);
        } else {
            seedAnnouncedRanksThrough(GameConstants.getColonyRankByKey(savedDynasty.rankName));
        }
        
        initializeColor();
        initializeServices();

        applyLocalizedName();
        rankUp();
    }

    private void initializeServices() {
        this.automationService = new DynastyAutomationService();
        this.aiService = new DynastyAiService();
        this.logisticsAutomationService = new DynastyLogisticsAutomationService();
        this.starterService = new DynastyStarterService();
        this.statService = new DynastyStatService();
        this.diplomacyService = new DynastyDiplomacyService(this);
    }
    
    private void initializeColor() {
        if (this.isPlayer) {
            this.color = AssetStyles.COLOR_LIGHT_BLUE; 
        } else {
            float hue = (this.id * 0.618033988749895f) % 1.0f;
            this.color = Color.getHSBColor(hue, 0.75f, 0.95f);
        }
    }

    public String generateNextColonyName() {
        return generateColonyName(colonies.size());
    }

    public String generateColonyName(int colonyIndex) {
        String baseName = themeBase != null && !themeBase.isEmpty()
                ? themeBase
                : LanguageStrings.dynastyThemeBase(this.name, this.titleKey);
        if (colonyIndex <= 0) {
            return LanguageStrings.formatCityName(baseName, GameConstants.CITY_TITLE_PRIME);
        }
        Set<String> usedCityTitleKeys = collectUsedCityTitleKeys();
        List<CityTitle> available = new ArrayList<>();
        for (CityTitle title : GameConstants.getSatelliteCityTitles()) {
            if (!usedCityTitleKeys.contains(title.getNameKey())) {
                available.add(title);
            }
        }
        if (!available.isEmpty()) {
            CityTitle cityTitle = available.get(GameRandom.nextInt(available.size()));
            return LanguageStrings.formatCityName(baseName, cityTitle);
        }
        return LanguageStrings.formatProceduralColonyName(baseName, colonyIndex);
    }

    private Set<String> collectUsedCityTitleKeys() {
        Set<String> used = new HashSet<>();
        for (Colony colony : colonies) {
            if (colony == null || colony.getName() == null) {
                continue;
            }
            for (CityTitle title : GameConstants.getCityTitles()) {
                String formatted = LanguageStrings.formatCityName(
                        LanguageStrings.resolveDynastyThemeDisplay(themeBase != null ? themeBase : ""), title);
                if (colony.getName().equals(formatted)) {
                    used.add(title.getNameKey());
                    break;
                }
                String stripped = LanguageStrings.stripCityTitleAffix(colony.getName(), title.getNameKey());
                if (stripped != null && !stripped.equals(colony.getName().trim())) {
                    used.add(title.getNameKey());
                    break;
                }
            }
        }
        return used;
    }

    // --- Logic ---
    public void runDailyJobs() {
        runDailyJobs(null, null);
    }

    public void runDailyJobs(World world, TradeManager tradeManager) {
        if (this.isDefeated) return;
        if (forcedFlightCooldownDays > 0) {
            forcedFlightCooldownDays--;
        }
        this.automationService.runDailyAutomation(this);
        if (world != null) {
            this.aiService.runDailyAi(this, world, tradeManager);
        }
        if (world != null) {
            migrateLegacyTimedDiplomaticModifiers(world);
            tickDiplomaticModifierDays();
        }
        if (world != null && tradeManager != null) {
            bindTradeManager(tradeManager);
            this.logisticsAutomationService.runDailyLogistics(this, world, tradeManager);
            if (diplomacyService != null) {
                diplomacyService.validateAllDiplomatDeployments(world);
            }
            if (hasUpgrade(GameUnlocks.ABILITY_AUTO_DIPLOMACY) && isAutoDiplomacyEnabled()) {
                this.diplomacyService.runAutomatedColonyLoyalty(this, world, tradeManager);
            }
        }
        this.rankUp();
    }
    
    private void rankUp() {
        Rank previous = this.rank;
        int total = this.statService.getTotalPopulation(this);
                
        if (total >= GameConstants.RANK_GIGA.getPopulation()) this.rank = GameConstants.RANK_GIGA;
        else if (total >= GameConstants.RANK_SUPREME.getPopulation()) this.rank = GameConstants.RANK_SUPREME;
        else if (total >= GameConstants.RANK_ULTIMATE.getPopulation()) this.rank = GameConstants.RANK_ULTIMATE;
        else if (total >= GameConstants.RANK_MEGA.getPopulation()) this.rank = GameConstants.RANK_MEGA;
        else if (total >= GameConstants.RANK_HYPER.getPopulation()) this.rank = GameConstants.RANK_HYPER;
        else if (total >= GameConstants.RANK_ULTRA.getPopulation()) this.rank = GameConstants.RANK_ULTRA;
        else if (total >= GameConstants.RANK_SUPER.getPopulation()) this.rank = GameConstants.RANK_SUPER;
        else if (total >= GameConstants.RANK_EMPIRE.getPopulation()) this.rank = GameConstants.RANK_EMPIRE;
        else if (total >= GameConstants.RANK_KINGDOM.getPopulation()) this.rank = GameConstants.RANK_KINGDOM;
        else if (total >= GameConstants.RANK_DUCHY.getPopulation()) this.rank = GameConstants.RANK_DUCHY;
        else if (total >= GameConstants.RANK_COUNTY.getPopulation()) this.rank = GameConstants.RANK_COUNTY;
        else if (total >= GameConstants.RANK_COLONY.getPopulation()) this.rank = GameConstants.RANK_COLONY;
        else this.rank = GameConstants.RANK_ANT;

        if (previous != null && this.rank != null && previous != this.rank
                && this.rank.getPopulation() > previous.getPopulation()) {
            recordHistory(WorldHistoryEventType.DYNASTY_RANK_UP, LanguageStrings.HISTORY_DYNASTY_RANK_UP_FMT,
                    id, -1, -1,
                    WorldHistoryEvent.dynastyArg(id),
                    WorldHistoryEvent.rankArg(previous),
                    WorldHistoryEvent.rankArg(this.rank));
        }
    }
    
    public void recordDeath(String cause) {
        this.globalDeathStatistics.merge(DeathCause.normalize(cause), 1, Integer::sum);
    }

    public void addColony(Colony colony) {
        if (!colonies.contains(colony)) {
            colonies.add(colony);
            colony.setDynasty(this); 
            rankUp();
        }
    }

    public void removeColony(Colony colony) {
        colonies.remove(colony);
        if (capital == colony) {
            capital = null;
        }
        rankUp();
    }
    
    public void addAbsorbedDynasty(int dynastyId) {
        if (!absorbedDynastyIds.contains(dynastyId)) {
            absorbedDynastyIds.add(dynastyId);
            recordHistory(WorldHistoryEventType.DYNASTY_ABSORBED, LanguageStrings.HISTORY_DYNASTY_ABSORBED_FMT,
                    id, -1, -1,
                    WorldHistoryEvent.dynastyArg(id),
                    WorldHistoryEvent.dynastyArg(dynastyId));
        }
    }

    public void absorbSpecies(int speciesId) {
        if (!defeatedSpeciesIds.contains(speciesId)) {
            defeatedSpeciesIds.add(speciesId);
        }
    }

    public int inheritAssimilationsFrom(Dynasty defeated) {
        if (defeated == null || defeated == this) {
            return 0;
        }
        int inherited = 0;

        AntSpecies species = defeated.getSpecies();
        if (species != null) {
            int before = defeatedSpeciesIds.size();
            absorbSpecies(species.getId());
            if (defeatedSpeciesIds.size() > before) {
                inherited++;
            }
        }

        for (int speciesId : new ArrayList<>(defeated.getDefeatedSpeciesIds())) {
            int before = defeatedSpeciesIds.size();
            absorbSpecies(speciesId);
            if (defeatedSpeciesIds.size() > before) {
                inherited++;
            }
        }

        for (Assimilation assimilation : new ArrayList<>(defeated.getCompletedAssimilations())) {
            if (isAssimilationCompleted(assimilation)) {
                continue;
            }
            if (assimilation.getReward() != null) {
                unlockUpgrade(assimilation.getReward());
            }
            completedAssimilations.add(assimilation);
            if (currentAssimilation == assimilation) {
                currentAssimilation = null;
                assimilationProgress = 0;
            }
            inherited++;
        }

        addAbsorbedDynasty(defeated.getId());
        return inherited;
    }

    public void inheritProgressFrom(Dynasty parent) {
        if (parent == null || parent == this) {
            return;
        }
        for (Upgrade upgrade : new ArrayList<>(parent.getUnlockedUpgrades())) {
            unlockUpgrade(upgrade);
        }
        for (Assimilation assimilation : new ArrayList<>(parent.getCompletedAssimilations())) {
            if (!isAssimilationCompleted(assimilation)) {
                if (assimilation.getReward() != null) {
                    unlockUpgrade(assimilation.getReward());
                }
                completedAssimilations.add(assimilation);
            }
        }
        for (int speciesId : new ArrayList<>(parent.getDefeatedSpeciesIds())) {
            absorbSpecies(speciesId);
        }
        setDefaultAutomationEnabled(parent.isDefaultAutomationEnabled());
        setDefaultAutoBuildEnabled(parent.isDefaultAutoBuildEnabled());
        setAutoDiplomacyEnabled(parent.isAutoDiplomacyEnabled());
        setDefaultAutoTunnelsEnabled(parent.isDefaultAutoTunnelsEnabled());
    }
    
    public void incrementNuptialFlights() {
        this.totalNuptialFlights++;

        if (this.totalNuptialFlights >= 10 && !hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT)) {
            unlockUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT);
        }
    }
    
    public int getMassNuptialFlightCost() {
        if (colonies.isEmpty()) return 10000;
        
        long baseCost = (long) colonies.get(0).getNuptialFlightCost();
        long scaledCost = baseCost * 10L;
        
        if (scaledCost > Integer.MAX_VALUE - 100000) {
            return Integer.MAX_VALUE - 100000;
        }
        
        return (int) scaledCost;
    }
    
    public void runMassNuptialFlight(World world) {
        if (!hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT)) return;
        
        int cost = getMassNuptialFlightCost();
        if (getResearchPoints() < cost) return;
        
        addResearchPoints(-cost);
        
        for (Colony colony : new ArrayList<>(colonies)) {
            Hex hex = null;
            for (Hex h : world.getHexes()) {
                if (h.getColony() == colony) {
                    hex = h;
                    break;
                }
            }
            
            if (hex != null) {
                boolean hasDrones = !colony.getDrones().isEmpty();
                boolean hasBreeders = colony.getPrincesses().stream().anyMatch(p -> p.getRole() == GameConstants.ROLE_BREEDER);
                
                if (hasDrones && hasBreeders) {
                    colony.getLabourService().runNuptial(colony, world, hex);
                }
            }
        }
    }

    public List<Tunnel> getTunnels() { return tunnels; }
    public void addTunnel(Tunnel tunnel) {
        if (!tunnels.contains(tunnel)) {
            tunnels.add(tunnel);
        }
    }

    public Tunnel getTunnelBetween(Hex a, Hex b) {
        return tunnels.stream()
            .filter(t -> t.connects(a, b))
            .findFirst()
            .orElse(null);
    }

    public boolean hasIncompleteTunnelAt(Hex hex) {
        if (hex == null) {
            return false;
        }
        for (Tunnel tunnel : tunnels) {
            if (!tunnel.isComplete() && (tunnel.getHexA() == hex || tunnel.getHexB() == hex)) {
                return true;
            }
        }
        return false;
    }

    public Colony getCapital() {
        if (capital == null) {
            resolveCapitalFromColonies();
        }
        return capital;
    }

    public void resolveCapitalFromColonies() {
        if (colonies == null || colonies.isEmpty()) {
            capital = null;
            return;
        }
        Colony current = capital;
        if (current != null && colonies.contains(current) && current.isCapital()
                && colonyMatchesDynastyTheme(current)) {
            setCapital(current);
            return;
        }
        List<Colony> flagged = new ArrayList<>();
        for (Colony c : colonies) {
            if (c.isCapital()) {
                flagged.add(c);
            }
        }
        if (flagged.size() == 1 && colonyMatchesDynastyTheme(flagged.get(0))) {
            setCapital(flagged.get(0));
            return;
        }
        if (current != null && colonies.contains(current) && colonyMatchesDynastyTheme(current)) {
            setCapital(current);
            return;
        }
        colonies.stream()
                .filter(this::colonyMatchesDynastyTheme)
                .min(Comparator.comparingInt(Colony::getId))
                .or(() -> colonies.stream().min(Comparator.comparingInt(Colony::getId)))
                .ifPresent(this::setCapital);
    }

    private boolean colonyMatchesDynastyTheme(Colony colony) {
        if (colony == null) {
            return false;
        }
        String dynastyBase = themeBase != null && !themeBase.isEmpty()
                ? LanguageStrings.resolveDynastyThemeDisplay(themeBase)
                : LanguageStrings.dynastyThemeBase(name, titleKey);
        if (dynastyBase == null || dynastyBase.isEmpty()) {
            return true;
        }
        String colonyBase = LanguageStrings.dynastyThemeBase(colony.getName(), null);
        return dynastyBase.equalsIgnoreCase(colonyBase);
    }

    public void setCapital(Colony colony) {
        Colony previous = this.capital;
        if (colony != null && !colonies.contains(colony)) {
            addColony(colony);
        }
        this.capital = colony;
        for (Colony c : colonies) {
            c.setCapital(c == colony);
        }
        if (colony != null && previous != null && previous != colony) {
            recordHistory(WorldHistoryEventType.CAPITAL_MOVED, LanguageStrings.HISTORY_CAPITAL_MOVED_FMT,
                    id, colony.getId(), -1,
                    WorldHistoryEvent.dynastyArg(id),
                    WorldHistoryEvent.colonyArg(colony.getId()));
        }
    }

    public void promoteNewCapital() {
        if (colonies == null || colonies.isEmpty()) {
            capital = null;
            return;
        }
        Colony newCapital = null;
        int maxAnts = -1;
        for (Colony c : colonies) {
            if (c.getAntTotal() > maxAnts) {
                maxAnts = c.getAntTotal();
                newCapital = c;
            }
        }
        if (newCapital != null) {
            setCapital(newCapital);
        }
    }

    public int getDiplomaticReputation(int otherDynastyId) {
        if (otherDynastyId == id) {
            return GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION;
        }
        return GameNumbers.clampDiplomaticReputation(
                diplomaticReputations.getOrDefault(otherDynastyId, GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION));
    }

    public void setDiplomaticReputation(int otherDynastyId, int score) {
        if (otherDynastyId == id) {
            return;
        }
        diplomaticReputations.put(otherDynastyId, GameNumbers.clampDiplomaticReputation(score));
    }

    public void adjustDiplomaticReputation(int otherDynastyId, int delta) {
        setDiplomaticReputation(otherDynastyId, getDiplomaticReputation(otherDynastyId) + delta);
    }

    public Map<Integer, Integer> copyDiplomaticReputations() {
        return new HashMap<>(diplomaticReputations);
    }

    public Set<String> getDiplomaticModifierKeys(int otherDynastyId) {
        if (otherDynastyId == id) {
            return Collections.emptySet();
        }
        Map<String, Integer> modifiers = diplomaticModifierRemainingDays.get(otherDynastyId);
        return modifiers == null ? Collections.emptySet() : Collections.unmodifiableSet(modifiers.keySet());
    }

    public int getDiplomaticModifierRemainingDays(int otherDynastyId, String modifierKey) {
        if (otherDynastyId == id || modifierKey == null) {
            return 0;
        }
        Map<String, Integer> modifiers = diplomaticModifierRemainingDays.get(otherDynastyId);
        if (modifiers == null) {
            return 0;
        }
        return modifiers.getOrDefault(modifierKey, 0);
    }

    public String getDiplomaticModifierKey(int otherDynastyId) {
        if (otherDynastyId == id) {
            return null;
        }
        Map<String, Integer> modifiers = diplomaticModifierRemainingDays.get(otherDynastyId);
        if (modifiers == null || modifiers.isEmpty()) {
            return null;
        }
        for (String key : modifiers.keySet()) {
            DiplomaticReputationModifier modifier = GameConstants.getDiplomaticReputationModifierByKey(key);
            if (modifier != null && GameConstants.DIPLO_EXCLUSIVE_PACT.equals(modifier.getExclusiveGroupKey())) {
                return key;
            }
        }
        return modifiers.keySet().iterator().next();
    }

    public boolean hasDiplomaticModifierKey(int otherDynastyId, String modifierKey) {
        if (otherDynastyId == id || modifierKey == null) {
            return false;
        }
        Map<String, Integer> modifiers = diplomaticModifierRemainingDays.get(otherDynastyId);
        return modifiers != null && modifiers.containsKey(modifierKey);
    }

    public void addDiplomaticModifierKey(int otherDynastyId, String modifierKey) {
        if (otherDynastyId == id || modifierKey == null) {
            return;
        }
        DiplomaticReputationModifier modifier = GameConstants.getDiplomaticReputationModifierByKey(modifierKey);
        if (modifier == null) {
            return;
        }
        putDiplomaticModifierRemainingDays(
                otherDynastyId, modifierKey, GameNumbers.initialModifierRemainingDays(modifier));
    }

    public void putDiplomaticModifierRemainingDays(int otherDynastyId, String modifierKey, int remainingDays) {
        if (otherDynastyId == id || modifierKey == null) {
            return;
        }
        if (GameConstants.getDiplomaticReputationModifierByKey(modifierKey) == null) {
            return;
        }
        diplomaticModifierRemainingDays
                .computeIfAbsent(otherDynastyId, ignored -> new LinkedHashMap<>())
                .put(modifierKey, remainingDays);
    }

    public void removeDiplomaticModifierKey(int otherDynastyId, String modifierKey) {
        if (otherDynastyId == id || modifierKey == null) {
            return;
        }
        Map<String, Integer> modifiers = diplomaticModifierRemainingDays.get(otherDynastyId);
        if (modifiers == null) {
            return;
        }
        modifiers.remove(modifierKey);
        if (modifiers.isEmpty()) {
            diplomaticModifierRemainingDays.remove(otherDynastyId);
        }
    }

    public void setDiplomaticModifierKey(int otherDynastyId, String modifierKey) {
        if (otherDynastyId == id) {
            return;
        }
        if (modifierKey == null) {
            diplomaticModifierRemainingDays.remove(otherDynastyId);
            return;
        }
        DiplomaticReputationModifier modifier = GameConstants.getDiplomaticReputationModifierByKey(modifierKey);
        if (modifier != null && modifier.getExclusiveGroupKey() != null) {
            removeExclusiveGroupKeys(otherDynastyId, modifier.getExclusiveGroupKey());
        }
        addDiplomaticModifierKey(otherDynastyId, modifierKey);
    }

    public void clearDiplomaticModifierKey(int otherDynastyId) {
        diplomaticModifierRemainingDays.remove(otherDynastyId);
    }

    private void removeExclusiveGroupKeys(int otherDynastyId, String exclusiveGroupKey) {
        Map<String, Integer> modifiers = diplomaticModifierRemainingDays.get(otherDynastyId);
        if (modifiers == null || exclusiveGroupKey == null) {
            return;
        }
        modifiers.keySet().removeIf(key -> {
            DiplomaticReputationModifier modifier = GameConstants.getDiplomaticReputationModifierByKey(key);
            return modifier != null && exclusiveGroupKey.equals(modifier.getExclusiveGroupKey());
        });
        if (modifiers.isEmpty()) {
            diplomaticModifierRemainingDays.remove(otherDynastyId);
        }
    }

    public void tickDiplomaticModifierDays() {
        List<String> expiredKeys = new ArrayList<>();
        List<Integer> expiredOtherIds = new ArrayList<>();
        List<Integer> emptyOtherIds = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, Integer>> pairEntry : diplomaticModifierRemainingDays.entrySet()) {
            int otherDynastyId = pairEntry.getKey();
            Map<String, Integer> modifiers = pairEntry.getValue();
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, Integer> modifierEntry : modifiers.entrySet()) {
                int remaining = modifierEntry.getValue();
                if (remaining == GameNumbers.MODIFIER_PERMANENT) {
                    continue;
                }
                if (remaining <= 1) {
                    toRemove.add(modifierEntry.getKey());
                } else {
                    modifierEntry.setValue(remaining - 1);
                }
            }
            for (String key : toRemove) {
                modifiers.remove(key);
                expiredOtherIds.add(otherDynastyId);
                expiredKeys.add(key);
            }
            if (modifiers.isEmpty()) {
                emptyOtherIds.add(otherDynastyId);
            }
        }
        for (Integer otherDynastyId : emptyOtherIds) {
            diplomaticModifierRemainingDays.remove(otherDynastyId);
        }
        for (int i = 0; i < expiredKeys.size(); i++) {
            expireDiplomaticModifier(expiredOtherIds.get(i), expiredKeys.get(i));
        }
    }

    private void expireDiplomaticModifier(int otherDynastyId, String modifierKey) {
        DiplomaticReputationModifier modifier = GameConstants.getDiplomaticReputationModifierByKey(modifierKey);
        if (modifier != null && modifier.getReputationDelta() != 0) {
            adjustDiplomaticReputation(otherDynastyId, -modifier.getReputationDelta());
        }
    }

    public void migrateLegacyTimedDiplomaticModifiers(World world) {
        if (legacyTimedModifiersMigrated || world == null) {
            return;
        }
        legacyTimedModifiersMigrated = true;
        int worldMonth = DynastyDiplomacyService.worldMonthIndex(world);
        migrateLegacyTimedEntry(
                pactRequestDeclinedAtWorldMonth,
                GameConstants.DIPLO_MODIFIER_DECLINED_PACT,
                worldMonth,
                GameNumbers.monthsToDays(GameNumbers.DIPLO_DECLINED_REQUEST_COOLDOWN_MONTHS));
        migrateLegacyTimedEntry(
                tradeRequestDeclinedAtWorldMonth,
                GameConstants.DIPLO_MODIFIER_TRADE_REQUEST,
                worldMonth,
                GameNumbers.monthsToDays(GameNumbers.DIPLO_DECLINED_REQUEST_COOLDOWN_MONTHS));
        migrateLegacyTimedEntry(
                wasAtWarPeacedAtWorldMonth,
                GameConstants.DIPLO_MODIFIER_WAS_AT_WAR,
                worldMonth,
                GameNumbers.monthsToDays(GameNumbers.WAS_AT_WAR_MODIFIER_MONTHS));
        migrateLegacyTimedEntry(
                geneticExchangeGrantedAtWorldMonth,
                GameConstants.DIPLO_MODIFIER_GENETIC_EXCHANGE,
                worldMonth,
                GameNumbers.monthsToDays(6));
        pactRequestDeclinedAtWorldMonth.clear();
        tradeRequestDeclinedAtWorldMonth.clear();
        wasAtWarPeacedAtWorldMonth.clear();
        geneticExchangeGrantedAtWorldMonth.clear();
    }

    private void migrateLegacyTimedEntry(
            Map<Integer, Integer> legacyStarts,
            DiplomaticReputationModifier modifier,
            int worldMonth,
            int totalDays) {
        if (legacyStarts.isEmpty() || modifier == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> entry : new HashMap<>(legacyStarts).entrySet()) {
            int otherDynastyId = entry.getKey();
            Integer startedAt = entry.getValue();
            if (startedAt == null) {
                continue;
            }
            int elapsedDays = Math.max(0, (worldMonth - startedAt) * GameNumbers.DAYS_PER_MONTH);
            int remaining = Math.max(0, totalDays - elapsedDays);
            if (remaining <= 0) {
                if (hasDiplomaticModifierKey(otherDynastyId, modifier.getNameKey())) {
                    removeDiplomaticModifierKey(otherDynastyId, modifier.getNameKey());
                    expireDiplomaticModifier(otherDynastyId, modifier.getNameKey());
                }
                continue;
            }
            if (!hasDiplomaticModifierKey(otherDynastyId, modifier.getNameKey())) {
                addDiplomaticModifierKey(otherDynastyId, modifier.getNameKey());
            }
            putDiplomaticModifierRemainingDays(otherDynastyId, modifier.getNameKey(), remaining);
        }
    }

    public Map<String, List<String>> copyDiplomaticModifierKeySets() {
        Map<String, List<String>> copy = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Integer>> entry : diplomaticModifierRemainingDays.entrySet()) {
            copy.put(String.valueOf(entry.getKey()), new ArrayList<>(entry.getValue().keySet()));
        }
        return copy;
    }

    public Map<String, Map<String, Integer>> copyDiplomaticModifierRemainingDays() {
        Map<String, Map<String, Integer>> copy = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Integer>> entry : diplomaticModifierRemainingDays.entrySet()) {
            copy.put(String.valueOf(entry.getKey()), new HashMap<>(entry.getValue()));
        }
        return copy;
    }

    public boolean hasCrossDynastyTradeRepBonus(int otherDynastyId) {
        return crossDynastyTradeRepGrantedIds.contains(otherDynastyId);
    }

    public void markCrossDynastyTradeRepBonus(int otherDynastyId) {
        if (otherDynastyId != id && !crossDynastyTradeRepGrantedIds.contains(otherDynastyId)) {
            crossDynastyTradeRepGrantedIds.add(otherDynastyId);
        }
    }

    public List<Integer> copyCrossDynastyTradeRepGrantedIds() {
        return new ArrayList<>(crossDynastyTradeRepGrantedIds);
    }

    public boolean hasPendingPactRequestFrom(int fromDynastyId) {
        return pendingPactRequestFromIds.contains(fromDynastyId);
    }

    public void addPendingPactRequest(int fromDynastyId) {
        if (fromDynastyId != id) {
            pendingPactRequestFromIds.add(fromDynastyId);
        }
    }

    public void removePendingPactRequest(int fromDynastyId) {
        pendingPactRequestFromIds.remove(fromDynastyId);
    }

    public List<Integer> copyPendingPactRequestFromIds() {
        return new ArrayList<>(pendingPactRequestFromIds);
    }

    public boolean isAtWar() {
        for (Map<String, Integer> modifiers : diplomaticModifierRemainingDays.values()) {
            if (modifiers.containsKey(GameConstants.DIPLO_MODIFIER_WAR.getNameKey())) {
                return true;
            }
        }
        return false;
    }

    public boolean isAtWarWith(int otherDynastyId) {
        if (otherDynastyId == id) {
            return false;
        }
        return hasDiplomaticModifierKey(otherDynastyId, GameConstants.DIPLO_MODIFIER_WAR.getNameKey());
    }

    public List<Integer> copyActiveWarDynastyIds() {
        List<Integer> ids = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, Integer>> entry : diplomaticModifierRemainingDays.entrySet()) {
            if (entry.getValue().containsKey(GameConstants.DIPLO_MODIFIER_WAR.getNameKey())) {
                ids.add(entry.getKey());
            }
        }
        return ids;
    }

    public Map<String, Integer> copyPactBrokenAtWorldMonth() {
        Map<String, Integer> copy = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : pactBrokenAtWorldMonth.entrySet()) {
            copy.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return copy;
    }

    public Integer getPactBrokenAtWorldMonth(int otherDynastyId) {
        return pactBrokenAtWorldMonth.get(otherDynastyId);
    }

    public void setPactBrokenAtWorldMonth(int otherDynastyId, int worldMonthIndex) {
        if (otherDynastyId != id) {
            pactBrokenAtWorldMonth.put(otherDynastyId, worldMonthIndex);
        }
    }

    public Map<String, Integer> copyPactRequestDeclinedAtWorldMonth() {
        Map<String, Integer> copy = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : pactRequestDeclinedAtWorldMonth.entrySet()) {
            copy.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return copy;
    }

    public Integer getPactRequestDeclinedAtWorldMonth(int otherDynastyId) {
        return pactRequestDeclinedAtWorldMonth.get(otherDynastyId);
    }

    public void setPactRequestDeclinedAtWorldMonth(int otherDynastyId, int worldMonthIndex) {
        if (otherDynastyId != id) {
            pactRequestDeclinedAtWorldMonth.put(otherDynastyId, worldMonthIndex);
        }
    }

    public void removePactRequestDeclinedAtWorldMonth(int otherDynastyId) {
        pactRequestDeclinedAtWorldMonth.remove(otherDynastyId);
    }

    public Map<String, Integer> copyTradeRequestDeclinedAtWorldMonth() {
        Map<String, Integer> copy = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : tradeRequestDeclinedAtWorldMonth.entrySet()) {
            copy.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return copy;
    }

    public Integer getTradeRequestDeclinedAtWorldMonth(int otherDynastyId) {
        return tradeRequestDeclinedAtWorldMonth.get(otherDynastyId);
    }

    public void setTradeRequestDeclinedAtWorldMonth(int otherDynastyId, int worldMonthIndex) {
        if (otherDynastyId != id) {
            tradeRequestDeclinedAtWorldMonth.put(otherDynastyId, worldMonthIndex);
        }
    }

    public void removeTradeRequestDeclinedAtWorldMonth(int otherDynastyId) {
        tradeRequestDeclinedAtWorldMonth.remove(otherDynastyId);
    }

    public Map<String, Integer> copyWasAtWarPeacedAtWorldMonth() {
        Map<String, Integer> copy = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : wasAtWarPeacedAtWorldMonth.entrySet()) {
            copy.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return copy;
    }

    public Integer getWasAtWarPeacedAtWorldMonth(int otherDynastyId) {
        return wasAtWarPeacedAtWorldMonth.get(otherDynastyId);
    }

    public void setWasAtWarPeacedAtWorldMonth(int otherDynastyId, int worldMonthIndex) {
        if (otherDynastyId != id) {
            wasAtWarPeacedAtWorldMonth.put(otherDynastyId, worldMonthIndex);
        }
    }

    public void removeWasAtWarPeacedAtWorldMonth(int otherDynastyId) {
        wasAtWarPeacedAtWorldMonth.remove(otherDynastyId);
    }

    public Map<String, Integer> copyGeneticExchangeGrantedAtWorldMonth() {
        Map<String, Integer> copy = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : geneticExchangeGrantedAtWorldMonth.entrySet()) {
            copy.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return copy;
    }

    public Integer getGeneticExchangeGrantedAtWorldMonth(int otherDynastyId) {
        return geneticExchangeGrantedAtWorldMonth.get(otherDynastyId);
    }

    public void setGeneticExchangeGrantedAtWorldMonth(int otherDynastyId, int worldMonthIndex) {
        if (otherDynastyId != id) {
            geneticExchangeGrantedAtWorldMonth.put(otherDynastyId, worldMonthIndex);
        }
    }

    public void removeGeneticExchangeGrantedAtWorldMonth(int otherDynastyId) {
        geneticExchangeGrantedAtWorldMonth.remove(otherDynastyId);
    }

    public boolean hasPendingWarDeclarationFrom(int fromDynastyId) {
        return pendingWarDeclarationFromIds.contains(fromDynastyId);
    }

    public void addPendingWarDeclarationFrom(int fromDynastyId) {
        if (fromDynastyId != id) {
            pendingWarDeclarationFromIds.add(fromDynastyId);
        }
    }

    public void removePendingWarDeclarationFrom(int fromDynastyId) {
        pendingWarDeclarationFromIds.remove(fromDynastyId);
    }

    public List<Integer> copyPendingWarDeclarationFromIds() {
        return new ArrayList<>(pendingWarDeclarationFromIds);
    }

    public static final class PendingNpcWarAlert {
        public final int attackerId;
        public final int defenderId;

        public PendingNpcWarAlert(int attackerId, int defenderId) {
            this.attackerId = attackerId;
            this.defenderId = defenderId;
        }
    }

    public void addPendingNpcWarAlert(int attackerId, int defenderId) {
        if (attackerId != id && defenderId != id && attackerId != defenderId) {
            pendingNpcWarAlerts.add(new PendingNpcWarAlert(attackerId, defenderId));
        }
    }

    public void removePendingNpcWarAlert(int attackerId, int defenderId) {
        pendingNpcWarAlerts.removeIf(alert -> alert.attackerId == attackerId && alert.defenderId == defenderId);
    }

    public List<PendingNpcWarAlert> copyPendingNpcWarAlerts() {
        return new ArrayList<>(pendingNpcWarAlerts);
    }

    public static final class PendingIntegrationVassalWarAlert {
        public final int attackerId;
        public final int vassalId;

        public PendingIntegrationVassalWarAlert(int attackerId, int vassalId) {
            this.attackerId = attackerId;
            this.vassalId = vassalId;
        }
    }

    public void addPendingIntegrationVassalWarAlert(int attackerId, int vassalId) {
        if (attackerId != id && vassalId != id && attackerId != vassalId) {
            pendingIntegrationVassalWarAlerts.add(new PendingIntegrationVassalWarAlert(attackerId, vassalId));
        }
    }

    public void removePendingIntegrationVassalWarAlert(int attackerId, int vassalId) {
        pendingIntegrationVassalWarAlerts.removeIf(alert ->
                alert.attackerId == attackerId && alert.vassalId == vassalId);
    }

    public List<PendingIntegrationVassalWarAlert> copyPendingIntegrationVassalWarAlerts() {
        return new ArrayList<>(pendingIntegrationVassalWarAlerts);
    }

    public static final class PendingWarStageResultAlert {
        public final String title;
        public final String message;

        public PendingWarStageResultAlert(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }

    public void addPendingWarStageResultAlert(String title, String message) {
        if (title == null || message == null || title.isBlank() || message.isBlank()) {
            return;
        }
        pendingWarStageResultAlerts.add(new PendingWarStageResultAlert(title, message));
    }

    public List<PendingWarStageResultAlert> copyPendingWarStageResultAlerts() {
        return new ArrayList<>(pendingWarStageResultAlerts);
    }

    public void removePendingWarStageResultAlert(PendingWarStageResultAlert alert) {
        pendingWarStageResultAlerts.remove(alert);
    }

    public void addPendingIntegrationCompletedAlert(int targetDynastyId) {
        if (targetDynastyId != id && targetDynastyId > 0
                && !pendingIntegrationCompletedTargetIds.contains(targetDynastyId)) {
            pendingIntegrationCompletedTargetIds.add(targetDynastyId);
        }
    }

    public void removePendingIntegrationCompletedAlert(int targetDynastyId) {
        pendingIntegrationCompletedTargetIds.remove(Integer.valueOf(targetDynastyId));
    }

    public List<Integer> copyPendingIntegrationCompletedTargetIds() {
        return new ArrayList<>(pendingIntegrationCompletedTargetIds);
    }

    public void addPendingTradeProposal(CrossDynastyTradeProposal proposal) {
        if (proposal == null || proposal.isEmpty()) {
            return;
        }
        pendingTradeProposals.removeIf(existing -> existing.getFromDynastyId() == proposal.getFromDynastyId()
                && existing.getOriginColonyId() == proposal.getOriginColonyId()
                && existing.getDestinationColonyId() == proposal.getDestinationColonyId());
        pendingTradeProposals.add(proposal);
    }

    public void removePendingTradeProposal(CrossDynastyTradeProposal proposal) {
        if (proposal == null) {
            return;
        }
        pendingTradeProposals.removeIf(existing -> existing.getFromDynastyId() == proposal.getFromDynastyId()
                && existing.getOriginColonyId() == proposal.getOriginColonyId()
                && existing.getDestinationColonyId() == proposal.getDestinationColonyId());
    }

    public boolean hasPendingTradeProposalFrom(int fromDynastyId) {
        for (CrossDynastyTradeProposal proposal : pendingTradeProposals) {
            if (proposal.getFromDynastyId() == fromDynastyId) {
                return true;
            }
        }
        return false;
    }

    public List<CrossDynastyTradeProposal> copyPendingTradeProposals() {
        return new ArrayList<>(pendingTradeProposals);
    }

    public void removePendingTradeProposalsFrom(int fromDynastyId) {
        pendingTradeProposals.removeIf(proposal -> proposal.getFromDynastyId() == fromDynastyId);
    }

    public int getOriginDynastyId() {
        return originDynastyId;
    }

    public void setOriginDynastyId(int originDynastyId) {
        this.originDynastyId = Math.max(0, originDynastyId);
    }

    public int getActiveRebellionDynastyId() {
        return activeRebellionDynastyId;
    }

    public void setActiveRebellionDynastyId(int activeRebellionDynastyId) {
        this.activeRebellionDynastyId = Math.max(0, activeRebellionDynastyId);
    }

    public int getPendingRebellionResponseFromId() {
        return pendingRebellionResponseFromId;
    }

    public void setPendingRebellionResponseFromId(int rebellionDynastyId) {
        this.pendingRebellionResponseFromId = Math.max(0, rebellionDynastyId);
    }

    public void clearPendingRebellionResponse() {
        this.pendingRebellionResponseFromId = 0;
    }

    public int getIntegrationTargetDynastyId() {
        return integrationTargetDynastyId;
    }

    public void setIntegrationTargetDynastyId(int integrationTargetDynastyId) {
        this.integrationTargetDynastyId = Math.max(0, integrationTargetDynastyId);
    }

    public double getIntegrationProgressDays() {
        return integrationProgressDays;
    }

    public void setIntegrationProgressDays(double integrationProgressDays) {
        this.integrationProgressDays = Math.max(0, integrationProgressDays);
    }

    public void addIntegrationProgressDays(double days) {
        this.integrationProgressDays = Math.max(0, this.integrationProgressDays + days);
    }

    public boolean hasActiveIntegration() {
        return integrationTargetDynastyId > 0;
    }

    public boolean isIntegratingDynasty(Dynasty target) {
        return target != null && integrationTargetDynastyId == target.getId();
    }

    public void clearIntegration() {
        this.integrationTargetDynastyId = 0;
        this.integrationProgressDays = 0;
        this.integrationDiplomatsManual = false;
    }

    public boolean isIntegrationDiplomatsManual() {
        return integrationDiplomatsManual;
    }

    public void setIntegrationDiplomatsManual(boolean integrationDiplomatsManual) {
        this.integrationDiplomatsManual = integrationDiplomatsManual;
    }

    public int getForcedFlightCooldownDays() {
        return forcedFlightCooldownDays;
    }

    public void setForcedFlightCooldownDays(int days) {
        this.forcedFlightCooldownDays = Math.max(0, days);
    }

    // --- Getters & Setters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getThemeBase() { return themeBase; }
    public void setThemeBase(String themeBase) { this.themeBase = themeBase; }
    public String getTitleKey() { return titleKey; }
    public DynastyTitle getTitle() { return GameConstants.getDynastyTitleByKey(titleKey); }
    public int getTitleId() { return getTitle().getId(); }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey != null ? titleKey : LanguageStrings.DYNASTY_TITLE_DYNASTY;
    }

    public void applyLocalizedName() {
        if (wildDynasty) {
            this.name = LanguageStrings.formatWildDynastyName(this.titleKey);
            return;
        }
        String theme = this.themeBase;
        if (theme == null || theme.isEmpty()) {
            theme = LanguageStrings.stripDynastyNameSuffix(this.name, this.titleKey);
            if (theme == null || theme.isEmpty()) {
                theme = LanguageStrings.stripDynastyNameSuffix(this.name);
            }
        }
        if (theme != null && !theme.isEmpty()) {
            this.name = LanguageStrings.formatDynastyName(theme, this.titleKey);
            if (this.themeBase == null || this.themeBase.isEmpty()) {
                String key = LanguageStrings.findDynastyThemeKey(theme);
                this.themeBase = key != null ? key : theme;
            }
        }
    }

    private static boolean inferLegacyWildDynasty(Savefile.SavedDynasty savedDynasty) {
        if (savedDynasty.isPlayer) {
            return false;
        }
        if (savedDynasty.themeBase != null && !savedDynasty.themeBase.isEmpty()) {
            return false;
        }
        String savedName = savedDynasty.name;
        if (savedName == null || savedName.isEmpty()) {
            return false;
        }
        if (savedName.equals(LanguageStrings.get(LanguageStrings.DYNASTY_WILD_NAME))) {
            return true;
        }
        for (DynastyTitle title : GameConstants.getDynastyTitles()) {
            if (savedName.equals(LanguageStrings.formatWildDynastyName(title))) {
                return true;
            }
        }
        return false;
    }

    public boolean isWildDynasty() { return wildDynasty; }
    public void setWildDynasty(boolean wildDynasty) { this.wildDynasty = wildDynasty; }

    public void setName(String name) { this.name = name; }
    public boolean isPlayer() { return isPlayer; }
    public void setPlayer(boolean player) { 
        isPlayer = player; 
        initializeColor(); 
    }
    public AntSpecies getSpecies() { return species; }
    public void setSpecies(AntSpecies species) { this.species = species; }
    
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    
    public Rank getRank() { return rank; }
    public void setRank(Rank rank) { this.rank = rank; }

    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) {
        this.researchPoints = researchPoints;
        invalidateAffordableAlertCaches();
    }

    public void addResearchPoints(int amount) {
        this.researchPoints += amount;
        invalidateAffordableAlertCaches();
    }
    
    public int getTotalNuptialFlights() { return totalNuptialFlights; }

    public int getDiplomatsSentTotal() { return diplomatsSentTotal; }

    public void recordDiplomatsSent(int count) {
        if (count > 0) {
            diplomatsSentTotal += count;
        }
    }

    public int countCompleteTunnels() {
        int count = 0;
        for (Tunnel tunnel : tunnels) {
            if (tunnel.isComplete()) {
                count++;
            }
        }
        return count;
    }

    public boolean meetsAutoTunnelsPrerequisites() {
        return countCompleteTunnels() >= GameNumbers.AUTO_UPGRADE_MIN_COMPLETE_TUNNELS;
    }

    public boolean meetsAutoDiplomacyPrerequisites() {
        return diplomatsSentTotal >= GameNumbers.AUTO_UPGRADE_MIN_DIPLOMATS_SENT;
    }

    public boolean isDefeated() { return isDefeated; }
    public void setDefeated(boolean isDefeated) { this.isDefeated = isDefeated; }

    public boolean hasLivingPopulation() {
        return statService.getTotalPopulation(this) > 0;
    }

    public boolean isActiveForDiplomacy() {
        return !isDefeated && hasLivingPopulation();
    }

    public boolean isDefaultAutomationEnabled() { return defaultAutomationEnabled; }
    public void setDefaultAutomationEnabled(boolean enabled) { this.defaultAutomationEnabled = enabled; }

    public boolean isDefaultAutoBuildEnabled() { return defaultAutoBuildEnabled; }
    public void setDefaultAutoBuildEnabled(boolean enabled) { this.defaultAutoBuildEnabled = enabled; }

    public boolean isAutoDiplomacyEnabled() { return autoDiplomacyEnabled; }
    public void setAutoDiplomacyEnabled(boolean enabled) { this.autoDiplomacyEnabled = enabled; }

    public PactRequestIncomingPolicy getPactRequestIncomingPolicy() {
        return pactRequestIncomingPolicy != null ? pactRequestIncomingPolicy : PactRequestIncomingPolicy.MANUAL;
    }

    public void setPactRequestIncomingPolicy(PactRequestIncomingPolicy policy) {
        this.pactRequestIncomingPolicy = policy != null ? policy : PactRequestIncomingPolicy.MANUAL;
    }

    public int getLastIncomingPactRequestWorldDay() {
        return lastIncomingPactRequestWorldDay;
    }

    public void setLastIncomingPactRequestWorldDay(int worldDay) {
        this.lastIncomingPactRequestWorldDay = worldDay;
    }

    public boolean isPactRequestPromptOpen() {
        return pactRequestPromptOpen;
    }

    public void setPactRequestPromptOpen(boolean open) {
        this.pactRequestPromptOpen = open;
    }

    public boolean isDefaultAutoTunnelsEnabled() { return defaultAutoTunnelsEnabled; }
    public void setDefaultAutoTunnelsEnabled(boolean enabled) { this.defaultAutoTunnelsEnabled = enabled; }

    public int getDiplomatSupportTo(int otherDynastyId) {
        return diplomatSupportToDynasty.getOrDefault(otherDynastyId, 0);
    }

    public void addDiplomatSupportTo(int otherDynastyId, int count) {
        if (count <= 0) {
            return;
        }
        diplomatSupportToDynasty.merge(otherDynastyId, count, Integer::sum);
    }

    public void removeDiplomatSupportTo(int otherDynastyId, int count) {
        if (count <= 0) {
            return;
        }
        int current = diplomatSupportToDynasty.getOrDefault(otherDynastyId, 0);
        int next = Math.max(0, current - count);
        if (next == 0) {
            diplomatSupportToDynasty.remove(otherDynastyId);
        } else {
            diplomatSupportToDynasty.put(otherDynastyId, next);
        }
    }

    public Map<Integer, Integer> copyDiplomatSupportToDynasty() {
        return new HashMap<>(diplomatSupportToDynasty);
    }

    public void restoreDiplomatSupportToDynasty(Map<Integer, Integer> support) {
        diplomatSupportToDynasty.clear();
        if (support != null) {
            diplomatSupportToDynasty.putAll(support);
        }
    }

    public Set<Upgrade> getUnlockedUpgrades() { return unlockedUpgrades; }

    public boolean hasAnnouncedRank(Rank rank) {
        return rank != null && announcedRankIds.contains(rank.getId());
    }

    public void markRankAnnounced(Rank rank) {
        if (rank != null) {
            announcedRankIds.add(rank.getId());
        }
    }

    public List<Integer> copyAnnouncedRankIds() {
        return new ArrayList<>(announcedRankIds);
    }

    private void seedStartingAnnouncedRanks() {
        announcedRankIds.add(GameConstants.RANK_ANT.getId());
        announcedRankIds.add(GameConstants.RANK_COLONY.getId());
    }

    private void seedAnnouncedRanksThrough(Rank through) {
        seedStartingAnnouncedRanks();
        if (through == null) {
            return;
        }
        for (Rank rank : GameConstants.getColonyRanks()) {
            if (rank.getId() <= through.getId()) {
                announcedRankIds.add(rank.getId());
            }
        }
    }
    public boolean hasUpgrade(Upgrade upgrade) { return unlockedUpgrades.contains(upgrade); }
    public void unlockUpgrade(Upgrade upgrade) {
        if (upgrade == null || unlockedUpgrades.contains(upgrade)) {
            return;
        }
        unlockedUpgrades.add(upgrade);
        if (upgrade == GameUnlocks.TYPE_SOLDIER) {
            if (!hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
                unlockedUpgrades.add(GameUnlocks.ROLE_HUNTER);
            }
            if (!hasUpgrade(GameUnlocks.ROLE_WARRIOR)) {
                unlockedUpgrades.add(GameUnlocks.ROLE_WARRIOR);
            }
            if (!hasUpgrade(GameUnlocks.ROLE_MILITIA)) {
                unlockedUpgrades.add(GameUnlocks.ROLE_MILITIA);
            }
        }
        DynastySynergyService.refreshUnlocked(this);
        invalidateAffordableAlertCaches();
    }

    public void revokeUpgrade(Upgrade upgrade) {
        if (upgrade == null) {
            return;
        }
        if (unlockedUpgrades.remove(upgrade)) {
            invalidateAffordableAlertCaches();
            DynastySynergyService.refreshUnlocked(this);
        }
    }

    public void applySynergyReward(Upgrade reward, Synergy synergy) {
        if (reward == null || unlockedUpgrades.contains(reward)) {
            return;
        }
        unlockedUpgrades.add(reward);
        invalidateAffordableAlertCaches();
        enqueueSynergyAlert(synergy);
    }

    private void enqueueSynergyAlert(Synergy synergy) {
        if (synergy != null && isPlayer) {
            pendingSynergyAlerts.add(synergy);
        }
    }

    public List<Synergy> drainPendingSynergyAlerts() {
        if (pendingSynergyAlerts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Synergy> alerts = new ArrayList<>(pendingSynergyAlerts);
        pendingSynergyAlerts.clear();
        return alerts;
    }

    private void invalidateAffordableAlertCaches() {
        for (Colony colony : colonies) {
            colony.invalidateAffordableAlertCache();
        }
    }

    public List<Colony> getColonies() { return colonies; }
    public List<Integer> getAbsorbedDynastyIds() { return absorbedDynastyIds; }
    public List<Integer> getDefeatedSpeciesIds() { return defeatedSpeciesIds; }

    public Set<Assimilation> getCompletedAssimilations() { return completedAssimilations; }
    public boolean isAssimilationCompleted(Assimilation a) { return completedAssimilations.contains(a); }
    
    public void completeAssimilation(Assimilation a) { 
        if (a != null && !completedAssimilations.contains(a)) {
            completedAssimilations.add(a);
            recordHistory(WorldHistoryEventType.ASSIMILATION_COMPLETED, LanguageStrings.HISTORY_ASSIMILATION_COMPLETED_FMT,
                    id, -1, -1,
                    WorldHistoryEvent.dynastyArg(id),
                    WorldHistoryEvent.assimilationArg(a.getId()));
        }
    }

    public void setOwningWorld(World world) {
        this.owningWorld = world;
    }

    public World getOwningWorld() {
        return owningWorld;
    }

    private void recordHistory(WorldHistoryEventType type, String messageKey,
            int relatedDynastyId, int relatedColonyId, int relatedWarId, String... args) {
        if (owningWorld == null || owningWorld.getHistoryService() == null) {
            return;
        }
        owningWorld.getHistoryService().record(type, messageKey, relatedDynastyId, relatedColonyId, relatedWarId, args);
    }

    public Assimilation getCurrentAssimilation() { return currentAssimilation; }
    public void setCurrentAssimilation(Assimilation a) { this.currentAssimilation = a; }
    public double getAssimilationProgress() { return assimilationProgress; }
    public void setAssimilationProgress(double progress) { this.assimilationProgress = progress; }
    public void addAssimilationProgress(double amount) { this.assimilationProgress += amount; }

    public int getAssimilationTargetCost() {
        return GameUnlocks.getAssimilationTargetCost(this);
    }
    
    public Map<String, Integer> getGlobalDeathStatistics() { return globalDeathStatistics; }
    
    public void bindTradeManager(TradeManager tradeManager) {
        if (tradeManager == null) {
            this.tradeService = null;
            return;
        }
        if (this.tradeService != null && this.tradeService.getTradeManager() == tradeManager) {
            return;
        }
        this.tradeService = new DynastyTradeService(this, tradeManager);
    }

    public DynastyStarterService getStarterService() { return starterService; }
    public DynastyStatService getStatService() { return statService; }
    public DynastyTradeService getTradeService() { return tradeService; }
    public DynastyDiplomacyService getDiplomacyService() { return diplomacyService; }

    public double getDiplomaticGeneticIntegrityBonus() {
        double bonus = 0.0;
        for (Map<String, Integer> modifiers : diplomaticModifierRemainingDays.values()) {
            for (String modifierKey : modifiers.keySet()) {
                GeneticIntegrityModifier modifier = GameConstants.getGeneticIntegrityModifierForDiplomaticKey(modifierKey);
                if (modifier != null) {
                    bonus += modifier.getIntegrityDelta();
                }
            }
        }
        return bonus;
    }

    public int countSatelliteColonies() {
        if (colonies.size() <= 1) {
            return 0;
        }
        int satellites = 0;
        for (Colony colony : colonies) {
            if (!colony.isCapital()) {
                satellites++;
            }
        }
        return satellites;
    }

    public double getSatelliteColonyIntegrityPenalty() {
        return countSatelliteColonies() * GameNumbers.GENETIC_INTEGRITY_SATELLITE_PENALTY;
    }

    public double getBaseGeneticIntegrity() {
        return GameNumbers.GENETIC_INTEGRITY_START - getSatelliteColonyIntegrityPenalty();
    }

    public double getMinGeneticIntegrity() {
        if (!hasUpgrade(GameUnlocks.ABILITY_CLONING)) {
            return 0.0;
        }
        return Math.min(100.0, completedAssimilations.size() * GameNumbers.GENETIC_INTEGRITY_ASSIMILATION_FLOOR_STEP);
    }

    public double getGeneticIntegrity() {
        double raw = getBaseGeneticIntegrity() + getDiplomaticGeneticIntegrityBonus();
        return Math.min(100.0, Math.max(getMinGeneticIntegrity(), raw));
    }

    public String buildGeneticIntegrityTooltip() {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append(LanguageStrings.get(LanguageStrings.GI_TOOLTIP_START))
                .append(": ")
                .append(String.format("%.1f%%", GameNumbers.GENETIC_INTEGRITY_START))
                .append("<br>");

        int satelliteCount = countSatelliteColonies();
        if (satelliteCount > 0) {
            double penalty = getSatelliteColonyIntegrityPenalty();
            sb.append(LanguageStrings.format(
                    LanguageStrings.GI_MODIFIER_SATELLITE_COLONIES,
                    LanguageStrings.formatNumber(satelliteCount),
                    formatGeneticIntegrityDelta(-penalty))).append("<br>");
        }

        for (Map<String, Integer> modifiers : diplomaticModifierRemainingDays.values()) {
            for (String modifierKey : modifiers.keySet()) {
                GeneticIntegrityModifier modifier = GameConstants.getGeneticIntegrityModifierForDiplomaticKey(modifierKey);
                if (modifier != null) {
                    sb.append(LanguageStrings.format(
                            LanguageStrings.LOYALTY_MODIFIER_LINE,
                            modifier.getName(),
                            formatGeneticIntegrityDelta(modifier.getIntegrityDelta()))).append("<br>");
                }
            }
        }

        double minIntegrity = getMinGeneticIntegrity();
        if (minIntegrity > 0.0) {
            sb.append(LanguageStrings.format(
                    LanguageStrings.GI_TOOLTIP_ASSIMILATION_FLOOR,
                    LanguageStrings.formatNumber(completedAssimilations.size()),
                    String.format("%.1f", minIntegrity))).append("<br>");
        }

        sb.append(LanguageStrings.get(LanguageStrings.LOYALTY_TOOLTIP_EFFECTIVE))
                .append(": ")
                .append(String.format("%.1f%%", getGeneticIntegrity()));
        sb.append("</html>");
        return sb.toString();
    }

    private static String formatGeneticIntegrityDelta(double delta) {
        if (Math.rint(delta) == delta) {
            return LanguageStrings.formatSigned((int) delta) + "%";
        }
        return String.format("%+.1f%%", delta);
    }

    public int getMilitaryPower() {
        return militaryPower;
    }

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

}