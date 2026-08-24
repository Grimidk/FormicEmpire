package com.grimidk.formicempire.classes.infrasctructure.managers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.util.GamePaths;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class SaveManager {
    private static final ExecutorService SHARED_EXECUTOR =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "save-worker"));

    public static void shutdownSharedExecutor() {
        SHARED_EXECUTOR.shutdown();
    }

    private final File savesDir;

    public SaveManager() {
        this.savesDir = GamePaths.resolveSavesDirectory();
        System.out.println("[SaveManager] Saves directory: " + savesDir.getAbsolutePath());
    }

    public File getSlotFile(int slotId, String type) {
        String fileName;
        switch (type) {
            case "manual":
                fileName = "slot" + slotId + ".manual.json";
                break;
            case "autosave":
                fileName = (slotId > 0) ? "slot" + slotId + ".autosave.json" : "autosave.json";
                break;
            default:
                fileName = "slot" + slotId + "." + type + ".json";
        }
        return new File(savesDir, fileName);
    }

    private File getSettingsFile() {
        return new File(savesDir, "settings.json");
    }

    private void writeSaveToFile(Savefile save, File targetFile) throws IOException {
        File tmp = new File(targetFile.getAbsolutePath() + ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeSaveToWriter(save, bw);
            bw.flush();
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            throw e; 
        }

        try {
            Files.move(tmp.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException amnse) {
            Files.move(tmp.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            throw e; 
        }
    }

    private void writeManualSave(Savefile save) throws IOException {
        File f = getSlotFile(save.getId(), "manual");
        if (save.getPlayTime() == 0) {
            save.setPlayTime(computePlayTimeFromSave(save));
        }
        save.setTimestamp(System.currentTimeMillis());
        
        writeSaveToFile(save, f); 
        System.out.println("[SaveManager] Wrote manual save for slot " + save.getId() + " -> " + f.getName());
    }

    public void saveAutosave(World w, Engine e) {
        if (w == null) return;
        int slotId = 0;
        try {
            slotId = w.getSaveSlotId();
        } catch (Exception ignore) {
            slotId = 0; 
        }
        saveWorldToSlot(w, slotId);
    }

    public void saveUserSlot(Savefile save) throws IOException {
        writeManualSave(save);
    }

    public void saveUserSlotAsync(Savefile save, Consumer<Boolean> onDone) {
        SHARED_EXECUTOR.submit(() -> {
            boolean success = false;
            try {
                writeManualSave(save);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onDone != null) {
                boolean ok = success;
                SwingUtilities.invokeLater(() -> onDone.accept(ok));
            }
        });
    }

    public boolean deleteSlot(int slotId) {
        boolean removed = false;
        File[] filesToDelete = {
            getSlotFile(slotId, "manual"),
            getSlotFile(slotId, "autosave"),
        };
        
        for (File f : filesToDelete) {
            if (f.exists()) {
                try {
                    f.delete();
                    removed = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return removed;
    }

    public void saveWorldToSlot(World w, int slotId) {
        if (w == null) return;
        
        if (slotId == 0) {
            System.out.println("[SaveManager] Skipping autosave for new game (Slot ID 0).");
            return;
        }

        String nameToUse = LanguageStrings.get(LanguageStrings.SAVE_AUTOSAVE_NAME);
        File manualFile = getSlotFile(slotId, "manual");
        if (manualFile.exists() && manualFile.length() > 0) {
            try (BufferedReader r = Files.newBufferedReader(manualFile.toPath(), StandardCharsets.UTF_8)) {
                Savefile existingManual = readSaveFromReader(r);
                if (existingManual != null && existingManual.getName() != null && !existingManual.getName().trim().isEmpty()) {
                    nameToUse = existingManual.getName(); 
                }
            } catch (Exception ignore) {}
        }

        File f = getSlotFile(slotId, "autosave");
        Savefile save = new Savefile(slotId, nameToUse);
        populateSavefileFromGame(save, w, w.getEngine());

        try {
            if (slotId > 0) {
                File manual = getSlotFile(slotId, "manual");
                if (manual.exists() && manual.length() > 0) {
                    try (BufferedReader r = Files.newBufferedReader(manual.toPath(), StandardCharsets.UTF_8)) {
                        Savefile existing = readSaveFromReader(r);
                        if (existing != null) {
                            int existingPlay = existing.getPlayTime() != 0 ? existing.getPlayTime() : computePlayTimeFromSave(existing);
                            if (existingPlay > save.getPlayTime()) {
                                System.out.println("[SaveManager] Manual save for slot " + slotId + " has more playtime than autosave - skipping autosave");
                                return;
                            }
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
            
            writeSaveToFile(save, f);
            
            System.out.println("[SaveManager] Autosaved world to " + f.getAbsolutePath());
        
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public Savefile loadAutosaveForSlot(int slotId) {
        if (slotId <= 0) return null;

        File autos = getSlotFile(slotId, "autosave");
        if (!autos.exists() || autos.length() == 0) {
            return null; 
        }

        try (BufferedReader r = Files.newBufferedReader(autos.toPath(), StandardCharsets.UTF_8)) {
            return readSaveFromReader(r);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Savefile loadSlot(int slotId) {
        File manualFile = getSlotFile(slotId, "manual");
        File autosaveFile = getSlotFile(slotId, "autosave");

        Savefile manualSave = null;
        Savefile autosave = null;

        if (manualFile.exists() && manualFile.length() > 0) {
            try (BufferedReader r = Files.newBufferedReader(manualFile.toPath(), StandardCharsets.UTF_8)) {
                manualSave = readSaveFromReader(r);
            } catch (Exception e) {
                System.err.println("[SaveManager] Failed to read manual save for slot " + slotId);
                e.printStackTrace();
            }
        }

        if (autosaveFile.exists() && autosaveFile.length() > 0) {
            try (BufferedReader r = Files.newBufferedReader(autosaveFile.toPath(), StandardCharsets.UTF_8)) {
                autosave = readSaveFromReader(r);
            } catch (Exception e) {
                System.err.println("[SaveManager] Failed to read autosave for slot " + slotId);
                e.printStackTrace();
            }
        }

        if (manualSave != null && autosave != null) {
            if (autosave.getTimestamp() > manualSave.getTimestamp()) {
                return autosave;
            }
            return manualSave;
        } else if (manualSave != null) {
            return manualSave;
        } else if (autosave != null) {
            return autosave;
        }

        return null;
    }

    public void saveWorldToSlotUser(World w, int slotId) throws IOException {
        if (w == null) return;
        
        String slotName = String.format(LanguageStrings.get(LanguageStrings.SAVE_DEFAULT_NAME_FMT), slotId);
        try {
            Savefile existing = loadSlot(slotId);
            if (existing != null && existing.getName() != null && !existing.getName().isEmpty()) {
                slotName = existing.getName();
            }
        } catch (Exception ignore) {
        }
        
        saveWorldToSlotUser(w, slotId, slotName);
    }
    
    public void saveWorldToSlotUser(World w, int slotId, String name) throws IOException {
        if (w == null) return;
        
        String saveName = (name != null && !name.isEmpty()) ? name : String.format(LanguageStrings.get(LanguageStrings.SAVE_DEFAULT_NAME_FMT), slotId);
        Savefile save = new Savefile(slotId, saveName);
        populateSavefileFromGame(save, w, w.getEngine());
        writeManualSave(save);
    }

    public void saveWorldToSlotUserAsync(World w, Engine e, int slotId, Consumer<Boolean> onDone) {
        saveWorldToSlotUserAsync(w, e, slotId, null, onDone);
    }

    public void saveWorldToSlotUserAsync(World w, Engine e, int slotId, String name, Consumer<Boolean> onDone) {
        SHARED_EXECUTOR.submit(() -> {
            boolean success = false;
            try {
                saveWorldToSlotUser(w, slotId, name);
                success = true;
            } catch (Exception err) {
                err.printStackTrace();
            }
            if (onDone != null) {
                boolean ok = success;
                SwingUtilities.invokeLater(() -> onDone.accept(ok));
            }
        });
    }

    private int computePlayTime(int minute, int hour, int day, int month, int year) {
        int days = (day - 1) + ((month - 1) * 30) + (year * 12 * 30);
        return (days * 24 * 60) + (hour * 60) + minute;
    }

    private int computePlayTime(World w) {
        if (w == null) return 0;
        return computePlayTime(w.getMinute(), w.getHour(), w.getDay(), w.getMonth(), w.getYear());
    }
    
    private int computePlayTimeFromSave(Savefile s) {
        if (s == null) return 0;
        return computePlayTime(s.getMinute(), s.getHour(), s.getDay(), s.getMonth(), s.getYear());
    }

    private void populateSavefileFromGame(Savefile save, World w, Engine engine) {
        save.setTimestamp(System.currentTimeMillis());
        save.setMinute(w.getMinute());
        save.setHour(w.getHour());
        save.setDay(w.getDay());
        save.setMonth(w.getMonth());
        save.setYear(w.getYear());
        save.setPlayTime(computePlayTime(w));
        save.setWorldRadius(w.getWorldRadius());
        save.setContinentCoreRadius(w.getContinentCoreRadius());
        if (engine != null && engine.hasAchievementTaintingSandbox()) {
            w.disableAchievements();
        }
        if (!w.allowsAchievements()) {
            save.disableAchievements();
        } else {
            save.setAllowsAchievements(true);
        }

        if (w.getDynastys() != null) {
            for (Dynasty dynasty : w.getDynastys()) {
                if (dynasty.isPlayer()) {
                    save.setPlayerDynastyTitleId(dynasty.getTitleId());
                    save.setPlayerDynastyTitleKey(dynasty.getTitleKey());
                    break;
                }
            }
        }
        
        List<Savefile.SavedHex> hexList = new ArrayList<>();
        List<Savefile.SavedColony> colonyList = new ArrayList<>();
        List<Savefile.SavedDynasty> dynastyList = new ArrayList<>();

        // Save Dynastys
        if (w.getDynastys() != null) {
            for (Dynasty dynasty : w.getDynastys()) {
                Savefile.SavedDynasty sc = new Savefile.SavedDynasty();
                sc.id = dynasty.getId();
                sc.titleId = dynasty.getTitleId();
                sc.titleKey = dynasty.getTitleKey();
                if (dynasty.getThemeBase() != null && !dynasty.getThemeBase().isEmpty()) {
                    sc.themeBase = dynasty.getThemeBase();
                    sc.name = sc.themeBase;
                } else {
                    sc.name = dynasty.getName();
                }
                sc.isPlayer = dynasty.isPlayer();
                sc.wildDynasty = dynasty.isWildDynasty();
                sc.isDefeated = dynasty.isDefeated();
                sc.rankName = dynasty.getRank() != null ? dynasty.getRank().getNameKey() : LanguageStrings.RANK_ANT;
                sc.researchPoints = dynasty.getResearchPoints();
                sc.totalNuptialFlights = dynasty.getTotalNuptialFlights();
                sc.diplomatsSentTotal = dynasty.getDiplomatsSentTotal();
                sc.speciesId = dynasty.getSpecies() != null ? dynasty.getSpecies().getId() : 1;
                sc.defaultAutomationEnabled = dynasty.isDefaultAutomationEnabled();
                sc.defaultAutoBuildEnabled = dynasty.isDefaultAutoBuildEnabled();
                sc.autoDiplomacyEnabled = dynasty.isAutoDiplomacyEnabled();
                sc.defaultAutoTunnelsEnabled = dynasty.isDefaultAutoTunnelsEnabled();
                sc.defaultAutoLogisticsEnabled = dynasty.isDefaultAutoLogisticsEnabled();
                sc.pactRequestIncomingPolicy = dynasty.getPactRequestIncomingPolicy().name();
                sc.lastIncomingPactRequestWorldDay = dynasty.getLastIncomingPactRequestWorldDay();
                sc.diplomatSupportToDynasty = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : dynasty.copyDiplomatSupportToDynasty().entrySet()) {
                    sc.diplomatSupportToDynasty.put(String.valueOf(entry.getKey()), entry.getValue());
                }
                sc.defeatedSpeciesIds = (dynasty.getDefeatedSpeciesIds() != null) ? new ArrayList<>(dynasty.getDefeatedSpeciesIds()) : new ArrayList<>();
                sc.completedAssimilationIds = new ArrayList<>();
                if (dynasty.getCompletedAssimilations() != null) {
                    for (Assimilation a : dynasty.getCompletedAssimilations()) {
                        sc.completedAssimilationIds.add(a.getId());
                    }
                }
                sc.currentAssimilationId = (dynasty.getCurrentAssimilation() != null) ? dynasty.getCurrentAssimilation().getId() : -1;
                sc.assimilationProgress = dynasty.getAssimilationProgress();
                sc.capitalColonyId = (dynasty.getCapital() != null) ? dynasty.getCapital().getId() : -1;
                sc.geneticIntegrity = dynasty.getGeneticIntegrity();
                sc.militaryPower = dynasty.getMilitaryPower();
                
                sc.diplomaticReputations = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : dynasty.copyDiplomaticReputations().entrySet()) {
                    sc.diplomaticReputations.put(String.valueOf(entry.getKey()), entry.getValue());
                }
                sc.diplomaticModifierRemainingDays = dynasty.copyDiplomaticModifierRemainingDays();
                sc.crossDynastyTradeRepGrantedIds = dynasty.copyCrossDynastyTradeRepGrantedIds();
                sc.pendingPactRequestFromIds = dynasty.copyPendingPactRequestFromIds();
                sc.pendingWarDeclarationFromIds = dynasty.copyPendingWarDeclarationFromIds();
                sc.activeWarDynastyIds = dynasty.copyActiveWarDynastyIds();
                sc.pactBrokenAtWorldMonth = dynasty.copyPactBrokenAtWorldMonth();
                sc.pendingTradeProposals = new ArrayList<>();
                for (CrossDynastyTradeProposal proposal : dynasty.copyPendingTradeProposals()) {
                    Savefile.SavedCrossDynastyTradeProposal saved = new Savefile.SavedCrossDynastyTradeProposal();
                    saved.fromDynastyId = proposal.getFromDynastyId();
                    saved.originColonyId = proposal.getOriginColonyId();
                    saved.destinationColonyId = proposal.getDestinationColonyId();
                    saved.request = proposal.getKind() == CrossDynastyTradeProposal.Kind.REQUEST;
                    for (Map.Entry<Integer, Double> entry : proposal.copyLoadByResourceId().entrySet()) {
                        saved.loadByResourceId.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    sc.pendingTradeProposals.add(saved);
                }
                sc.forcedFlightCooldownDays = dynasty.getForcedFlightCooldownDays();
                sc.originDynastyId = dynasty.getOriginDynastyId();
                sc.activeRebellionDynastyId = dynasty.getActiveRebellionDynastyId();
                sc.pendingRebellionResponseFromId = dynasty.getPendingRebellionResponseFromId();
                sc.integrationTargetDynastyId = dynasty.getIntegrationTargetDynastyId();
                sc.integrationProgressDays = dynasty.getIntegrationProgressDays();
                sc.integrationDiplomatsManual = dynasty.isIntegrationDiplomatsManual();
                
                sc.unlockedUpgradeIds = new ArrayList<>();
                if (dynasty.getUnlockedUpgrades() != null) {
                    for(Upgrade u : dynasty.getUnlockedUpgrades()) {
                        sc.unlockedUpgradeIds.add(u.getId());
                    }
                }
                sc.unlockedSkillIds = dynasty.copyUnlockedSkillIds();
                sc.announcedRankIds = dynasty.copyAnnouncedRankIds();
                sc.absorbedDynastyIds = (dynasty.getAbsorbedDynastyIds() != null) ? new ArrayList<>(dynasty.getAbsorbedDynastyIds()) : new ArrayList<>();
                
                sc.deathStatistics = (dynasty.getGlobalDeathStatistics() != null) ? new HashMap<>(dynasty.getGlobalDeathStatistics()) : new HashMap<>();
                
                // Save Tunnels
                if (dynasty.getTunnels() != null) {
                    for (Tunnel t : dynasty.getTunnels()) {
                        Savefile.SavedTunnel st = new Savefile.SavedTunnel();
                        st.qA = t.getHexA().getQ();
                        st.rA = t.getHexA().getR();
                        st.qB = t.getHexB().getQ();
                        st.rB = t.getHexB().getR();
                        st.progress = t.getProgress();
                        st.totalCost = t.getTotalCost();
                        st.isComplete = t.isComplete();
                        sc.tunnels.add(st);
                    }
                }

                dynastyList.add(sc);
            }
        }
        save.setDynastys(dynastyList);

        // Save Trades
        List<Savefile.SavedTrade> savedTrades = new ArrayList<>();
        if (engine != null && engine.getTradeManager() != null) {
            for (Trade t : engine.getTradeManager().getActiveTrades()) {
                Savefile.SavedTrade st = new Savefile.SavedTrade();
                st.qOrigin = t.getOrigin().getQ();
                st.rOrigin = t.getOrigin().getR();
                st.qDest = t.getDestination().getQ();
                st.rDest = t.getDestination().getR();
                st.isRecurrent = t.isRecurrent();
                st.isBilateral = t.isBilateral();
                st.methodId = t.getMethod().getId();
                st.isActive = t.isActive();
                st.totalHours = t.getTotalHours();
                st.remainingHours = t.getRemainingHours();
                st.isReturning = t.isReturning();
                
                for (Map.Entry<ResourceType, Double> e : t.getLoad().entrySet()) {
                    st.load.put(e.getKey().getId(), e.getValue());
                }
                for (Map.Entry<ResourceType, Double> e : t.getReturnLoad().entrySet()) {
                    st.returnLoad.put(e.getKey().getId(), e.getValue());
                }
                for (Map.Entry<AntType, Integer> e : t.getTransport().entrySet()) {
                    st.transport.put(e.getKey().getId(), e.getValue());
                }

                // Save Pending
                st.hasPendingUpdate = t.hasPendingUpdate();
                if (st.hasPendingUpdate) {
                    st.pendingRecurrent = t.isPendingRecurrent();
                    st.pendingIsBilateral = t.isPendingBilateral();
                    st.pendingMethodId = t.getPendingMethod().getId();
                    if (t.getPendingLoad() != null) {
                        for (Map.Entry<ResourceType, Double> e : t.getPendingLoad().entrySet()) {
                            st.pendingLoad.put(e.getKey().getId(), e.getValue());
                        }
                    }
                    if (t.getPendingReturnLoad() != null) {
                        for (Map.Entry<ResourceType, Double> e : t.getPendingReturnLoad().entrySet()) {
                            st.pendingReturnLoad.put(e.getKey().getId(), e.getValue());
                        }
                    }
                    if (t.getPendingTransport() != null) {
                        for (Map.Entry<AntType, Integer> e : t.getPendingTransport().entrySet()) {
                            st.pendingTransport.put(e.getKey().getId(), e.getValue());
                        }
                    }
                }
                
                savedTrades.add(st);
            }
        }
        save.setTrades(savedTrades);
        save.setWars(w.getWarService().toSavedWars());
        save.setWorldHistory(w.getHistoryService().toSaved());

        // Save Hexes & Colonies
        if (w.getHexes() != null) {
            for (Hex h : w.getHexes()) {
                boolean hasColony = (h.getColony() != null);
                int biomeId = (h.getBiome() != null) ? h.getBiome().getId() : 1;
                int weatherId = (h.getLocalWeather() != null) ? h.getLocalWeather().getId() : 1;
                Savefile.SavedHex sh = new Savefile.SavedHex(h.getQ(), h.getR(), biomeId, hasColony, h.getTimeOffset(), weatherId);
                sh.nonWaterResourceSourcesGenerated = h.getNonWaterResourceSourcesGenerated();
                hexList.add(sh);
                
                if (hasColony) {
                    Colony c = h.getColony();
                    Savefile.SavedColony sc = new Savefile.SavedColony();
                    
                    // ID & Location
                    sc.id = c.getId();
                    sc.dynastyId = (c.getDynasty() != null) ? c.getDynasty().getId() : 0;
                    sc.name = c.getName();
                    sc.rankName = c.getRank() != null ? c.getRank().getNameKey() : LanguageStrings.RANK_COLONY;
                    sc.isPlayer = c.isPlayer();
                    sc.isCapital = c.isCapital();
                    sc.isAutomated = c.isAutomationEnabled();
                    sc.autoBuildEnabled = c.isAutoBuildEnabled();
                    sc.autoTunnelsEnabled = c.isAutoTunnelsEnabled();
                    sc.autoLogisticsEnabled = c.isAutoLogisticsEnabled();
                    sc.age = c.getAge();
                    sc.daysWithoutQueen = c.getDaysWithoutQueen();
                    sc.loyalty = c.getLoyalty();
                    sc.q = h.getQ();
                    sc.r = h.getR();
                    
                    // Counts
                    sc.totalAnts = c.getAntTotal();
                    sc.deadAnts = c.getDeadAnts().size();
                    sc.eggs = c.getEggs().size();
                    sc.larvae = c.getLarvae().size();
                    sc.pupae = c.getPupae().size();
                    sc.workers = c.getWorkers().size();
                    sc.soldiers = c.getSoldiers().size();
                    sc.majors = c.getMajors().size();
                    sc.drones = c.getDrones().size();
                    sc.princesses = c.getPrincesses().size();
                    sc.queens = c.getQueens().size();
                    
                    // Resources
                    sc.mushrooms = c.getMushrooms();
                    sc.plants = c.getPlants();
                    sc.protein = c.getProtein();
                    sc.water = c.getWater();
                    sc.syrups = c.getSyrups();
                    sc.resins = c.getResins();
                    sc.minerals = c.getMinerals();
                    
                    // Rates
                    sc.hatchRateWorker = c.getHatchRateWorker();
                    sc.hatchRateSoldier = c.getHatchRateSoldier();
                    sc.hatchRateMajor = c.getHatchRateMajor();
                    sc.hatchRateDrone = c.getHatchRateDrone();
                    sc.hatchRatePrincess = c.getHatchRatePrincess();
                    sc.subtypeHatchRatesFlat = AntSubtypeService.flattenSubtypeRates(c.getSubtypeHatchRates());
                    sc.workerSubtypes = AntSubtypeService.aggregateSubtypeCounts(c.getWorkers());
                    sc.soldierSubtypes = AntSubtypeService.aggregateSubtypeCounts(c.getSoldiers());
                    sc.majorSubtypes = AntSubtypeService.aggregateSubtypeCounts(c.getMajors());
                    sc.princessSubtypes = AntSubtypeService.aggregateSubtypeCounts(c.getPrincesses());
                    sc.queenSubtypes = AntSubtypeService.aggregateSubtypeCounts(c.getQueens());
                    
                    // Stats
                    sc.aphids = c.getBugHandlingService().resolvePetCountForSave(c, GameConstants.TYPE_APHID);
                    sc.symbioticMites = c.getBugHandlingService().resolvePetCountForSave(c, GameConstants.TYPE_SYMBIOTIC_MITE);
                    sc.dermestids = c.getBugHandlingService().resolvePetCountForSave(c, GameConstants.TYPE_DERMESTID);
                    sc.parasiteAnts = c.getParasiteAnts();
                    sc.parasiticMites = c.getParasiticMites();
                    sc.loyaltyModifierRemainingDays = c.copyLoyaltyModifierRemainingDays();
                    sc.integrationDiplomatsDeployed = c.getIntegrationDiplomatsDeployed();
                    sc.nativeSpeciesId = c.getNativeSpeciesId();
                    sc.creatineDietMonthsRemaining = c.getCreatineDietMonthsRemaining();
                    sc.totalDeaths = c.getTotalDeaths();
                    sc.militaryPower = c.getMilitaryPower();
                    
                    // Maps/Lists
                    for (Map.Entry<AntRole, Integer> entry : c.getPeaceAssignedRoleCounts().entrySet()) {
                        sc.assignedRoleCounts.put(String.valueOf(entry.getKey().getId()), entry.getValue());
                    }
                    for (Map.Entry<AntRole, Integer> entry : c.getWarAssignedRoleCounts().entrySet()) {
                        sc.warAssignedRoleCounts.put(String.valueOf(entry.getKey().getId()), entry.getValue());
                    }
                    sc.roleDisallowedSubtypesFlat = c.flattenPeaceRoleDisallowedSubtypes();
                    sc.warRoleDisallowedSubtypesFlat = c.flattenWarRoleDisallowedSubtypes();
                    for (Map.Entry<Integer, Integer> entry : c.getOutgoingColonyDiplomatMissions().entrySet()) {
                        sc.outgoingColonyDiplomatMissions.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    for (Map.Entry<Integer, Integer> entry : c.getIncomingColonyDiplomatSupport().entrySet()) {
                        sc.incomingColonyDiplomatSupport.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    for (Map.Entry<Integer, Integer> entry : c.getOutgoingDynastyDiplomatMissions().entrySet()) {
                        sc.outgoingDynastyDiplomatMissions.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    
                    if (c.getPopulationService() != null) {
                        sc.localDeathStatistics = new HashMap<>(c.getDeathService().getDeathStatistics());
                    }

                    for (Building b : c.getUnlockedBuildings()) sc.unlockedBuildingIds.add(b.getId());
                    
                    if (c.getLocationService() != null) {
                        for (ResourceSource rs : c.getLocationService().getDiscoveredSources()) {
                             sc.savedResourceSources.add(new Savefile.SavedResourceSource(
                                 rs.getResourceType().getId(), rs.getQuantity(), rs.getInitialQuantity(), rs.getX(), rs.getY()
                             ));
                        }
                    }
                    colonyList.add(sc);
                    
                    if (c.isPlayer()) {
                        save.setColonyId(c.getId());
                        save.setColonyName(c.getName());
                        save.setTotalAnts(c.getAntTotal());
                        save.setWorkers(c.getWorkers().size());
                        save.setQueens(c.getQueens().size());
                        save.setDeadAnts(c.getDeadAnts().size());
                        
                        save.setMushrooms(c.getMushrooms());
                        save.setPlants(c.getPlants());
                        save.setProtein(c.getProtein());
                        save.setWater(c.getWater());
                        save.setSyrups(c.getSyrups());
                        save.setResins(c.getResins());
                        save.setMinerals(c.getMinerals());
                    }
                }
            }
        }
        
        save.setWorldHexes(hexList);
        save.setColonies(colonyList);
    }

    private void writeSaveToWriter(Savefile s, BufferedWriter w) throws IOException {
        w.write("{");
        w.newLine();

        // - Global -
        writeJsonLine(w, "id", s.getId(), false);
        writeJsonLine(w, "name", s.getName() != null ? s.getName() : "", false);
        writeJsonLine(w, "timestamp", s.getTimestamp(), false);
        writeJsonLine(w, "playTime", s.getPlayTime(), false);
        writeJsonLine(w, "minute", s.getMinute(), false);
        writeJsonLine(w, "hour", s.getHour(), false);
        writeJsonLine(w, "day", s.getDay(), false);
        writeJsonLine(w, "month", s.getMonth(), false);
        writeJsonLine(w, "year", s.getYear(), false);
        writeJsonLine(w, "worldRadius", s.getWorldRadius(), false);
        writeJsonLine(w, "continentCoreRadius", s.getContinentCoreRadius(), false);
        writeJsonLine(w, "playerDynastyTitleId", s.resolvePlayerDynastyTitleId(), false);
        writeJsonLine(w, "playerDynastyTitleKey", s.getPlayerDynastyTitleKey() != null ? s.getPlayerDynastyTitleKey() : LanguageStrings.DYNASTY_TITLE_DYNASTY, false);
        writeJsonLine(w, "allowsAchievements", s.allowsAchievements(), false);
        
        // - Root Summary Data - 
        writeJsonLine(w, "colonyId", s.getColonyId(), false);
        writeJsonLine(w, "colonyName", s.getColonyName(), false);
        writeJsonLine(w, "totalAnts", s.getTotalAnts(), false);
        writeJsonLine(w, "workers", s.getWorkers(), false);
        writeJsonLine(w, "queens", s.getQueens(), false);
        writeJsonLine(w, "deadAnts", s.getDeadAnts(), false);
        
        // - Hexes -
        w.write("  \"worldHexes\": ");
        w.write(serializeHexesToJson(s.getWorldHexes()));
        w.write(","); 
        w.newLine();
        
        // - Dynastys -
        w.write("  \"dynastys\": [");
        w.newLine();
        List<Savefile.SavedDynasty> dynastys = s.getDynastys();
        if (dynastys != null) {
             for(int i=0; i<dynastys.size(); i++) {
                writeSavedDynasty(w, dynastys.get(i), (i == dynastys.size() - 1));
            }
        }
        w.write("  ],");
        w.newLine();

        // - Trades -
        w.write("  \"trades\": " + serializeTradesToJson(s.getTrades()) + ",");
        w.newLine();
        w.write("  \"wars\": " + serializeWarsToJson(s.getWars()) + ",");
        w.newLine();
        w.write("  \"worldHistory\": " + serializeWorldHistoryToJson(s.getWorldHistory()) + ",");
        w.newLine();

        // - Colonies -
        w.write("  \"colonies\": [");
        w.newLine();
        
        List<Savefile.SavedColony> cols = s.getColonies();
        if (cols != null) {
            for(int i=0; i<cols.size(); i++) {
                writeSavedColony(w, cols.get(i), (i == cols.size() - 1));
            }
        }
        w.write("  ]");
        
        w.newLine();
        w.write("}");
        w.newLine();
    }
    
    private void writeSavedDynasty(BufferedWriter w, Savefile.SavedDynasty sc, boolean isLast) throws IOException {
        w.write("    {");
        w.newLine();
        writeJsonLine(w, "id", sc.id, false);
        writeJsonLine(w, "themeBase", sc.themeBase != null ? sc.themeBase : "", false);
        writeJsonLine(w, "titleId", sc.titleId, false);
        writeJsonLine(w, "name", sc.name != null ? sc.name : "", false);
        writeJsonLine(w, "titleKey", sc.titleKey != null ? sc.titleKey : LanguageStrings.DYNASTY_TITLE_DYNASTY, false);
        writeJsonLine(w, "isPlayer", sc.isPlayer, false);
        writeJsonLine(w, "wildDynasty", sc.wildDynasty, false);
        writeJsonLine(w, "isDefeated", sc.isDefeated, false);
        writeJsonLine(w, "rank", sc.rankName, false);
        writeJsonLine(w, "speciesId", sc.speciesId, false);
        writeJsonLine(w, "researchPoints", sc.researchPoints, false);
        writeJsonLine(w, "totalNuptialFlights", sc.totalNuptialFlights, false);
        writeJsonLine(w, "diplomatsSentTotal", sc.diplomatsSentTotal, false);
        writeJsonLine(w, "defaultAutomationEnabled", sc.defaultAutomationEnabled, false);
        writeJsonLine(w, "defaultAutoBuildEnabled", sc.defaultAutoBuildEnabled, false);
        writeJsonLine(w, "autoDiplomacyEnabled", sc.autoDiplomacyEnabled, false);
        writeJsonLine(w, "defaultAutoTunnelsEnabled", sc.defaultAutoTunnelsEnabled, false);
        writeJsonLine(w, "defaultAutoLogisticsEnabled", sc.defaultAutoLogisticsEnabled, false);
        writeJsonLine(w, "pactRequestIncomingPolicy", sc.pactRequestIncomingPolicy, false);
        writeJsonLine(w, "lastIncomingPactRequestWorldDay", sc.lastIncomingPactRequestWorldDay, false);
        writeJsonLine(w, "currentAssimilationId", sc.currentAssimilationId, false);
        writeJsonLine(w, "assimilationProgress", sc.assimilationProgress, false);
        writeJsonLine(w, "capitalColonyId", sc.capitalColonyId, false);
        writeJsonLine(w, "geneticIntegrity", sc.geneticIntegrity, false);
        writeJsonLine(w, "militaryPower", sc.militaryPower, false);
        w.write("      \"diplomatSupportToDynasty\": " + serializeMapToJson(sc.diplomatSupportToDynasty) + ","); w.newLine();
        w.write("      \"diplomaticReputations\": " + serializeMapToJson(sc.diplomaticReputations) + ","); w.newLine();
        w.write("      \"diplomaticModifierRemainingDays\": " + serializeNestedIntegerMapToJson(sc.diplomaticModifierRemainingDays) + ","); w.newLine();
        w.write("      \"crossDynastyTradeRepGrantedIds\": " + serializeListToJson(sc.crossDynastyTradeRepGrantedIds) + ","); w.newLine();
        w.write("      \"pendingPactRequestFromIds\": " + serializeListToJson(sc.pendingPactRequestFromIds) + ","); w.newLine();
        w.write("      \"pendingWarDeclarationFromIds\": " + serializeListToJson(sc.pendingWarDeclarationFromIds) + ","); w.newLine();
        w.write("      \"activeWarDynastyIds\": " + serializeListToJson(sc.activeWarDynastyIds) + ","); w.newLine();
        w.write("      \"pactBrokenAtWorldMonth\": " + serializeMapToJson(sc.pactBrokenAtWorldMonth) + ","); w.newLine();
        w.write("      \"pendingTradeProposals\": " + serializeTradeProposalsToJson(sc.pendingTradeProposals) + ","); w.newLine();
        w.write("      \"forcedFlightCooldownDays\": " + sc.forcedFlightCooldownDays + ","); w.newLine();
        w.write("      \"originDynastyId\": " + sc.originDynastyId + ","); w.newLine();
        w.write("      \"activeRebellionDynastyId\": " + sc.activeRebellionDynastyId + ","); w.newLine();
        w.write("      \"pendingRebellionResponseFromId\": " + sc.pendingRebellionResponseFromId + ","); w.newLine();
        w.write("      \"integrationTargetDynastyId\": " + sc.integrationTargetDynastyId + ","); w.newLine();
        w.write("      \"integrationProgressDays\": " + sc.integrationProgressDays + ","); w.newLine();
        w.write("      \"integrationDiplomatsManual\": " + sc.integrationDiplomatsManual + ","); w.newLine();
        w.write("      \"unlockedUpgradeIds\": " + serializeListToJson(sc.unlockedUpgradeIds) + ","); w.newLine();
        w.write("      \"unlockedSkillIds\": " + serializeListToJson(sc.unlockedSkillIds) + ","); w.newLine();
        w.write("      \"announcedRankIds\": " + serializeListToJson(sc.announcedRankIds) + ","); w.newLine();
        w.write("      \"absorbedDynastyIds\": " + serializeListToJson(sc.absorbedDynastyIds) + ","); w.newLine();
        w.write("      \"defeatedSpeciesIds\": " + serializeListToJson(sc.defeatedSpeciesIds) + ","); w.newLine();
        w.write("      \"completedAssimilationIds\": " + serializeListToJson(sc.completedAssimilationIds) + ","); w.newLine();
        w.write("      \"deathStatistics\": " + serializeMapToJson(sc.deathStatistics) + ","); w.newLine();
        w.write("      \"tunnels\": " + serializeTunnelsToJson(sc.tunnels)); w.newLine();
        w.write("    }");
        if (!isLast) w.write(",");
        w.newLine();
    }
    
    private void writeSavedColony(BufferedWriter w, Savefile.SavedColony sc, boolean isLast) throws IOException {
        w.write("    {");
        w.newLine();
        // ID & Loc
        writeJsonLine(w, "id", sc.id, false);
        writeJsonLine(w, "dynastyId", sc.dynastyId, false);
        writeJsonLine(w, "name", sc.name, false);
        writeJsonLine(w, "rank", sc.rankName, false);
        writeJsonLine(w, "isPlayer", sc.isPlayer, false);
        writeJsonLine(w, "isCapital", sc.isCapital, false);
        writeJsonLine(w, "isAutomated", sc.isAutomated, false);
        writeJsonLine(w, "autoBuildEnabled", sc.autoBuildEnabled, false);
        writeJsonLine(w, "autoTunnelsEnabled", sc.autoTunnelsEnabled, false);
        writeJsonLine(w, "autoLogisticsEnabled", sc.autoLogisticsEnabled, false);
        writeJsonLine(w, "age", sc.age, false);
        writeJsonLine(w, "daysWithoutQueen", sc.daysWithoutQueen, false);
        writeJsonLine(w, "loyalty", sc.loyalty, false);
        writeJsonLine(w, "militaryPower", sc.militaryPower, false);
        writeJsonLine(w, "q", sc.q, false);
        writeJsonLine(w, "r", sc.r, false);
        
        // Stats
        writeJsonLine(w, "totalAnts", sc.totalAnts, false);
        writeJsonLine(w, "workers", sc.workers, false);
        writeJsonLine(w, "eggs", sc.eggs, false);
        writeJsonLine(w, "larvae", sc.larvae, false);
        writeJsonLine(w, "pupae", sc.pupae, false);
        writeJsonLine(w, "soldiers", sc.soldiers, false);
        writeJsonLine(w, "majors", sc.majors, false);
        writeJsonLine(w, "drones", sc.drones, false);
        writeJsonLine(w, "princesses", sc.princesses, false);
        writeJsonLine(w, "queens", sc.queens, false);
        writeJsonLine(w, "deadAnts", sc.deadAnts, false);
        
        writeJsonLine(w, "mushrooms", sc.mushrooms, false);
        writeJsonLine(w, "plants", sc.plants, false);
        writeJsonLine(w, "protein", sc.protein, false);
        writeJsonLine(w, "water", sc.water, false);
        writeJsonLine(w, "syrups", sc.syrups, false);
        writeJsonLine(w, "resins", sc.resins, false);
        writeJsonLine(w, "minerals", sc.minerals, false);
        
        // Hatch Rates
        writeJsonLine(w, "hatchRateWorker", sc.hatchRateWorker, false);
        writeJsonLine(w, "hatchRateSoldier", sc.hatchRateSoldier, false);
        writeJsonLine(w, "hatchRateMajor", sc.hatchRateMajor, false);
        writeJsonLine(w, "hatchRateDrone", sc.hatchRateDrone, false);
        writeJsonLine(w, "hatchRatePrincess", sc.hatchRatePrincess, false);
        
        w.write("      \"subtypeHatchRatesFlat\": " + serializeDoubleMapToJson(sc.subtypeHatchRatesFlat) + ","); w.newLine();
        w.write("      \"workerSubtypes\": " + serializeMapToJson(sc.workerSubtypes) + ","); w.newLine();
        w.write("      \"soldierSubtypes\": " + serializeMapToJson(sc.soldierSubtypes) + ","); w.newLine();
        w.write("      \"majorSubtypes\": " + serializeMapToJson(sc.majorSubtypes) + ","); w.newLine();
        w.write("      \"princessSubtypes\": " + serializeMapToJson(sc.princessSubtypes) + ","); w.newLine();
        w.write("      \"queenSubtypes\": " + serializeMapToJson(sc.queenSubtypes) + ","); w.newLine();
        
        writeJsonLine(w, "aphids", sc.aphids, false);
        writeJsonLine(w, "symbioticMites", sc.symbioticMites, false);
        writeJsonLine(w, "dermestids", sc.dermestids, false);
        writeJsonLine(w, "parasiteAnts", sc.parasiteAnts, false);
        writeJsonLine(w, "parasiticMites", sc.parasiticMites, false);
        w.write("      \"loyaltyModifierRemainingDays\": " + serializeMapToJson(sc.loyaltyModifierRemainingDays) + ","); w.newLine();
        writeJsonLine(w, "integrationDiplomatsDeployed", sc.integrationDiplomatsDeployed, false);
        writeJsonLine(w, "nativeSpeciesId", sc.nativeSpeciesId, false);
        writeJsonLine(w, "creatineDietMonthsRemaining", sc.creatineDietMonthsRemaining, false);
        writeJsonLine(w, "totalDeaths", sc.totalDeaths, false);

        // Serialized Lists within Colony
        w.write("      \"assignedRoleCounts\": " + serializeMapToJson(sc.assignedRoleCounts) + ","); w.newLine();
        w.write("      \"warAssignedRoleCounts\": " + serializeMapToJson(sc.warAssignedRoleCounts) + ","); w.newLine();
        w.write("      \"roleDisallowedSubtypesFlat\": " + serializeMapToJson(sc.roleDisallowedSubtypesFlat) + ","); w.newLine();
        w.write("      \"warRoleDisallowedSubtypesFlat\": " + serializeMapToJson(sc.warRoleDisallowedSubtypesFlat) + ","); w.newLine();
        w.write("      \"outgoingColonyDiplomatMissions\": " + serializeMapToJson(sc.outgoingColonyDiplomatMissions) + ","); w.newLine();
        w.write("      \"incomingColonyDiplomatSupport\": " + serializeMapToJson(sc.incomingColonyDiplomatSupport) + ","); w.newLine();
        w.write("      \"outgoingDynastyDiplomatMissions\": " + serializeMapToJson(sc.outgoingDynastyDiplomatMissions) + ","); w.newLine();
        w.write("      \"localDeathStatistics\": " + serializeMapToJson(sc.localDeathStatistics) + ","); w.newLine();
        w.write("      \"unlockedBuildingIds\": " + serializeListToJson(sc.unlockedBuildingIds) + ","); w.newLine();
        w.write("      \"savedResourceSources\": " + serializeSourcesToJson(sc.savedResourceSources)); w.newLine(); 
        
        w.write("    }");
        if (!isLast) w.write(",");
        w.newLine();
    }

    private void writeJsonLine(BufferedWriter w, String key, Object value, boolean last) throws IOException {
        w.write("    \"");
        w.write(escapeJsonString(key));
        w.write("\": ");
        if (value instanceof String) {
            w.write("\"" + escapeJsonString((String)value) + "\"");
        } else {
            w.write(String.valueOf(value));
        }
        if (!last) w.write(",");
        w.newLine();
    }

    private Savefile readSaveFromReader(BufferedReader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = r.readLine()) != null) sb.append(line);
        String json = sb.toString();
        
        Map<String, String> rootMap = parseTopLevelJson(json);
        
        int id = Integer.parseInt(rootMap.getOrDefault("id", "0"));
        String name = rootMap.getOrDefault("name", "");
        Savefile s = new Savefile(id, name);
        
        s.setTimestamp(Long.parseLong(rootMap.getOrDefault("timestamp", "0")));
        s.setPlayTime(Integer.parseInt(rootMap.getOrDefault("playTime", "0")));
        s.setMinute(Integer.parseInt(rootMap.getOrDefault("minute", "0")));
        s.setHour(Integer.parseInt(rootMap.getOrDefault("hour", "0")));
        s.setDay(Integer.parseInt(rootMap.getOrDefault("day", "1")));
        s.setMonth(Integer.parseInt(rootMap.getOrDefault("month", "1")));
        s.setYear(Integer.parseInt(rootMap.getOrDefault("year", "0")));
        s.setWorldRadius(Integer.parseInt(rootMap.getOrDefault("worldRadius", "8")));
        s.setContinentCoreRadius(Integer.parseInt(rootMap.getOrDefault("continentCoreRadius", "0")));
        s.setPlayerDynastyTitleId(Integer.parseInt(rootMap.getOrDefault("playerDynastyTitleId", "0")));
        s.setPlayerDynastyTitleKey(rootMap.getOrDefault("playerDynastyTitleKey", LanguageStrings.DYNASTY_TITLE_DYNASTY));
        if (!Boolean.parseBoolean(rootMap.getOrDefault("allowsAchievements", "true"))) {
            s.disableAchievements();
        }
        
        // --- Populate Root Summary Data ---
        s.setColonyId(Integer.parseInt(rootMap.getOrDefault("colonyId", "0")));
        s.setColonyName(rootMap.getOrDefault("colonyName", ""));
        s.setTotalAnts(Integer.parseInt(rootMap.getOrDefault("totalAnts", "0")));
        s.setWorkers(Integer.parseInt(rootMap.getOrDefault("workers", "0")));
        s.setQueens(Integer.parseInt(rootMap.getOrDefault("queens", "0")));
        s.setDeadAnts(Integer.parseInt(rootMap.getOrDefault("deadAnts", "0")));

        s.setWorldHexes(deserializeJsonToHexes(rootMap.get("worldHexes")));
        
        List<Savefile.SavedDynasty> dynastys = deserializeJsonToDynastys(rootMap.get("dynastys"));
        s.setDynastys(dynastys);
        
        List<Savefile.SavedColony> colonies = deserializeJsonToColonies(rootMap.get("colonies"));
        s.setColonies(colonies);

        s.setTrades(deserializeJsonToTrades(rootMap.get("trades")));
        s.setWars(deserializeJsonToWars(rootMap.get("wars")));
        s.setWorldHistory(deserializeJsonToWorldHistory(rootMap.get("worldHistory")));
        
        return s;
    }

    // --- Manual Parser Helpers ---
    
    private Map<String, String> parseTopLevelJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        json = json.trim();
        if(json.startsWith("{")) json = json.substring(1);
        if(json.endsWith("}")) json = json.substring(0, json.length()-1);
        
        boolean inQuote = false;
        int braceDepth = 0;
        int bracketDepth = 0;
        int start = 0;
        
        for(int i=0; i<json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i==0 || json.charAt(i-1) != '\\')) inQuote = !inQuote;
            if (!inQuote) {
                if (c == '{') braceDepth++;
                if (c == '}') braceDepth--;
                if (c == '[') bracketDepth++;
                if (c == ']') bracketDepth--;
                
                if (c == ',' && braceDepth == 0 && bracketDepth == 0) {
                    parsePair(json.substring(start, i), map);
                    start = i+1;
                }
            }
        }
        if (start < json.length()) parsePair(json.substring(start), map);
        
        return map;
    }
    
    private void parsePair(String pair, Map<String, String> map) {
        int idx = pair.indexOf(':');
        if (idx > 0) {
            String k = pair.substring(0, idx).trim().replace("\"", "");
            String v = pair.substring(idx+1).trim();
            if (v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length()-1);
            map.put(k, v);
        }
    }
    
    private List<Savefile.SavedDynasty> deserializeJsonToDynastys(String jsonArray) {
        List<Savefile.SavedDynasty> list = new ArrayList<>();
        if (jsonArray == null || !jsonArray.startsWith("[")) return list;
        
        int lastIdx = jsonArray.lastIndexOf("]");
        if (lastIdx <= 1) return list;
        
        String content = jsonArray.substring(1, lastIdx);        
        int braceDepth = 0;
        int start = 0;
        for(int i=0; i<content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') braceDepth++;
            if (c == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    String dynastyJson = content.substring(start, i+1);
                    if (!dynastyJson.trim().isEmpty()) {
                        list.add(parseDynastyObject(dynastyJson));
                    }
                    while(i+1 < content.length() && (content.charAt(i+1) == ',' || Character.isWhitespace(content.charAt(i+1)))) i++;
                    start = i+1;
                }
            }
        }
        return list;
    }
    
    private Savefile.SavedDynasty parseDynastyObject(String json) {
        Savefile.SavedDynasty sc = new Savefile.SavedDynasty();
        Map<String, String> map = parseTopLevelJson(json);
        sc.id = Integer.parseInt(map.getOrDefault("id", "0"));
        sc.themeBase = map.getOrDefault("themeBase", "");
        sc.titleId = Integer.parseInt(map.getOrDefault("titleId", "0"));
        sc.name = map.getOrDefault("name", "");
        sc.titleKey = map.getOrDefault("titleKey", LanguageStrings.DYNASTY_TITLE_DYNASTY);
        if (sc.titleId <= 0) {
            sc.titleId = GameConstants.getDynastyTitleByKey(sc.titleKey).getId();
        }
        sc.isPlayer = Boolean.parseBoolean(map.getOrDefault("isPlayer", "false"));
        sc.wildDynasty = Boolean.parseBoolean(map.getOrDefault("wildDynasty", "false"));
        sc.isDefeated = Boolean.parseBoolean(map.getOrDefault("isDefeated", "false"));
        sc.rankName = map.getOrDefault("rank", LanguageStrings.RANK_ANT);
        sc.speciesId = Integer.parseInt(map.getOrDefault("speciesId", "1"));
        sc.researchPoints = Integer.parseInt(map.getOrDefault("researchPoints", "0"));
        sc.totalNuptialFlights = Integer.parseInt(map.getOrDefault("totalNuptialFlights", "0"));
        sc.diplomatsSentTotal = Integer.parseInt(map.getOrDefault("diplomatsSentTotal", "0"));
        sc.defaultAutomationEnabled = Boolean.parseBoolean(map.getOrDefault("defaultAutomationEnabled", "false"));
        sc.defaultAutoBuildEnabled = Boolean.parseBoolean(map.getOrDefault("defaultAutoBuildEnabled", "false"));
        sc.autoDiplomacyEnabled = Boolean.parseBoolean(map.getOrDefault("autoDiplomacyEnabled", "false"));
        sc.defaultAutoTunnelsEnabled = Boolean.parseBoolean(map.getOrDefault("defaultAutoTunnelsEnabled", "false"));
        sc.defaultAutoLogisticsEnabled = Boolean.parseBoolean(map.getOrDefault("defaultAutoLogisticsEnabled", "false"));
        sc.pactRequestIncomingPolicy = map.getOrDefault("pactRequestIncomingPolicy", "MANUAL");
        sc.lastIncomingPactRequestWorldDay = Integer.parseInt(
                map.getOrDefault("lastIncomingPactRequestWorldDay", "-1"));
        sc.currentAssimilationId = Integer.parseInt(map.getOrDefault("currentAssimilationId", "-1"));
        sc.assimilationProgress = Double.parseDouble(map.getOrDefault("assimilationProgress", "0.0"));
        sc.capitalColonyId = Integer.parseInt(map.getOrDefault("capitalColonyId", "-1"));
        sc.geneticIntegrity = Double.parseDouble(map.getOrDefault("geneticIntegrity", "100.0"));
        sc.militaryPower = Integer.parseInt(map.getOrDefault("militaryPower", "0"));
        sc.diplomatSupportToDynasty = deserializeJsonToMap(map.get("diplomatSupportToDynasty"));
        sc.diplomaticReputations = deserializeJsonToMap(map.get("diplomaticReputations"));
        sc.diplomaticModifierRemainingDays = deserializeJsonToNestedIntegerMap(map.get("diplomaticModifierRemainingDays"));
        if (sc.diplomaticModifierRemainingDays.isEmpty()) {
            sc.diplomaticModifierKeySets = deserializeJsonToStringListMap(map.get("diplomaticModifierKeySets"));
            if (sc.diplomaticModifierKeySets.isEmpty()) {
                sc.diplomaticModifierKeys = deserializeJsonToStringMap(map.get("diplomaticModifierKeys"));
            }
        }
        sc.crossDynastyTradeRepGrantedIds = deserializeJsonToList(map.get("crossDynastyTradeRepGrantedIds"));
        sc.pendingPactRequestFromIds = deserializeJsonToList(map.get("pendingPactRequestFromIds"));
        sc.pendingWarDeclarationFromIds = deserializeJsonToList(map.get("pendingWarDeclarationFromIds"));
        sc.activeWarDynastyIds = deserializeJsonToList(map.get("activeWarDynastyIds"));
        sc.pactBrokenAtWorldMonth = deserializeJsonToMap(map.get("pactBrokenAtWorldMonth"));
        sc.pactRequestDeclinedAtWorldMonth = deserializeJsonToMap(map.get("pactRequestDeclinedAtWorldMonth"));
        sc.tradeRequestDeclinedAtWorldMonth = deserializeJsonToMap(map.get("tradeRequestDeclinedAtWorldMonth"));
        sc.wasAtWarPeacedAtWorldMonth = deserializeJsonToMap(map.get("wasAtWarPeacedAtWorldMonth"));
        sc.geneticExchangeGrantedAtWorldMonth = deserializeJsonToMap(map.get("geneticExchangeGrantedAtWorldMonth"));
        sc.pendingTradeProposals = deserializeJsonToTradeProposals(map.get("pendingTradeProposals"));
        sc.forcedFlightCooldownDays = Integer.parseInt(map.getOrDefault("forcedFlightCooldownDays", "0"));
        sc.originDynastyId = Integer.parseInt(map.getOrDefault("originDynastyId", "0"));
        sc.activeRebellionDynastyId = Integer.parseInt(map.getOrDefault("activeRebellionDynastyId", "0"));
        sc.pendingRebellionResponseFromId = Integer.parseInt(map.getOrDefault("pendingRebellionResponseFromId", "0"));
        sc.integrationTargetDynastyId = Integer.parseInt(map.getOrDefault("integrationTargetDynastyId", "0"));
        if (map.containsKey("integrationProgressDays")) {
            sc.integrationProgressDays = Double.parseDouble(map.getOrDefault("integrationProgressDays", "0"));
        } else {
            sc.integrationProgressMonths = Double.parseDouble(map.getOrDefault("integrationProgressMonths", "0"));
            sc.integrationProgressDays = sc.integrationProgressMonths * GameNumbers.DAYS_PER_MONTH;
        }
        sc.integrationDiplomatsManual = Boolean.parseBoolean(map.getOrDefault("integrationDiplomatsManual", "false"));
        sc.unlockedUpgradeIds = deserializeJsonToList(map.get("unlockedUpgradeIds"));
        if (map.containsKey("unlockedSkillIds")) {
            sc.unlockedSkillIds = deserializeJsonToList(map.get("unlockedSkillIds"));
        } else {
            sc.unlockedSkillIds = null;
        }
        if (map.containsKey("announcedRankIds")) {
            sc.announcedRankIds = deserializeJsonToList(map.get("announcedRankIds"));
        } else {
            sc.announcedRankIds = null;
        }
        sc.absorbedDynastyIds = deserializeJsonToList(map.get("absorbedDynastyIds"));
        sc.defeatedSpeciesIds = deserializeJsonToList(map.get("defeatedSpeciesIds"));
        sc.completedAssimilationIds = deserializeJsonToList(map.get("completedAssimilationIds"));
        sc.deathStatistics = deserializeJsonToMap(map.get("deathStatistics"));
        sc.tunnels = deserializeJsonToTunnels(map.get("tunnels"));
        return sc;
    }

    private List<Savefile.SavedColony> deserializeJsonToColonies(String jsonArray) {
        List<Savefile.SavedColony> list = new ArrayList<>();
        if (jsonArray == null || !jsonArray.startsWith("[")) return list;
        
        int lastIdx = jsonArray.lastIndexOf("]");
        if (lastIdx <= 1) return list;
        
        String content = jsonArray.substring(1, lastIdx);        
        int braceDepth = 0;
        int start = 0;
        for(int i=0; i<content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') braceDepth++;
            if (c == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    String colJson = content.substring(start, i+1);
                    if (!colJson.trim().isEmpty()) {
                        list.add(parseColonyObject(colJson));
                    }
                    while(i+1 < content.length() && (content.charAt(i+1) == ',' || Character.isWhitespace(content.charAt(i+1)))) i++;
                    start = i+1;
                }
            }
        }
        return list;
    }
    
    private Savefile.SavedColony parseColonyObject(String json) {
        Savefile.SavedColony sc = new Savefile.SavedColony();
        Map<String, String> map = parseTopLevelJson(json); 
        
        sc.id = Integer.parseInt(map.getOrDefault("id", "0"));
        sc.dynastyId = Integer.parseInt(map.getOrDefault("dynastyId", "0"));
        sc.name = map.getOrDefault("name", "");
        sc.rankName = map.getOrDefault("rank", LanguageStrings.RANK_COLONY);
        sc.isPlayer = Boolean.parseBoolean(map.getOrDefault("isPlayer", "false"));
        sc.isCapital = Boolean.parseBoolean(map.getOrDefault("isCapital", "false"));
        sc.isAutomated = Boolean.parseBoolean(map.getOrDefault("isAutomated", "false"));
        sc.autoBuildEnabled = Boolean.parseBoolean(map.getOrDefault("autoBuildEnabled", "false"));
        sc.autoTunnelsEnabled = Boolean.parseBoolean(map.getOrDefault("autoTunnelsEnabled", "false"));
        sc.autoLogisticsEnabled = Boolean.parseBoolean(map.getOrDefault("autoLogisticsEnabled", "false"));
        sc.age = Integer.parseInt(map.getOrDefault("age", "0"));
        sc.daysWithoutQueen = Integer.parseInt(map.getOrDefault("daysWithoutQueen", "0"));
        sc.loyalty = Integer.parseInt(map.getOrDefault("loyalty", String.valueOf(GameNumbers.DEFAULT_COLONY_LOYALTY)));
        sc.militaryPower = Integer.parseInt(map.getOrDefault("militaryPower", "0"));
        sc.q = Integer.parseInt(map.getOrDefault("q", "0"));
        sc.r = Integer.parseInt(map.getOrDefault("r", "0"));
        sc.totalAnts = Integer.parseInt(map.getOrDefault("totalAnts", "0"));
        sc.workers = Integer.parseInt(map.getOrDefault("workers", "0"));
        sc.eggs = Integer.parseInt(map.getOrDefault("eggs", "0"));
        sc.larvae = Integer.parseInt(map.getOrDefault("larvae", "0"));
        sc.pupae = Integer.parseInt(map.getOrDefault("pupae", "0"));
        sc.soldiers = Integer.parseInt(map.getOrDefault("soldiers", "0"));
        sc.majors = Integer.parseInt(map.getOrDefault("majors", "0"));
        sc.drones = Integer.parseInt(map.getOrDefault("drones", "0"));
        sc.princesses = Integer.parseInt(map.getOrDefault("princesses", "0"));
        sc.queens = Integer.parseInt(map.getOrDefault("queens", "0"));
        sc.deadAnts = Integer.parseInt(map.getOrDefault("deadAnts", "0"));
        
        sc.plants = Integer.parseInt(map.getOrDefault("plants", "0"));
        sc.mushrooms = Integer.parseInt(map.getOrDefault("mushrooms", "0"));
        sc.protein = Integer.parseInt(map.getOrDefault("protein", "0"));
        sc.water = Integer.parseInt(map.getOrDefault("water", "0"));
        sc.syrups = Integer.parseInt(map.getOrDefault("syrups", "0"));
        sc.resins = Integer.parseInt(map.getOrDefault("resins", "0"));
        sc.minerals = Integer.parseInt(map.getOrDefault("minerals", "0"));

        // Hatch Rates
        sc.hatchRateWorker = Float.parseFloat(map.getOrDefault("hatchRateWorker", "0.0"));
        sc.hatchRateSoldier = Float.parseFloat(map.getOrDefault("hatchRateSoldier", "0.0"));
        sc.hatchRateMajor = Float.parseFloat(map.getOrDefault("hatchRateMajor", "0.0"));
        sc.hatchRateDrone = Float.parseFloat(map.getOrDefault("hatchRateDrone", "0.0"));
        sc.hatchRatePrincess = Float.parseFloat(map.getOrDefault("hatchRatePrincess", "0.0"));
        sc.subtypeHatchRatesFlat = deserializeJsonToDoubleMap(map.get("subtypeHatchRatesFlat"));
        sc.workerSubtypes = deserializeJsonToMap(map.get("workerSubtypes"));
        sc.soldierSubtypes = deserializeJsonToMap(map.get("soldierSubtypes"));
        sc.majorSubtypes = deserializeJsonToMap(map.get("majorSubtypes"));
        sc.princessSubtypes = deserializeJsonToMap(map.get("princessSubtypes"));
        sc.queenSubtypes = deserializeJsonToMap(map.get("queenSubtypes"));
        
        sc.aphids = Integer.parseInt(map.getOrDefault("aphids", "0"));
        sc.symbioticMites = Integer.parseInt(map.getOrDefault("symbioticMites",
                map.getOrDefault("soilMites", map.getOrDefault("bullMites", "0"))));
        sc.dermestids = Integer.parseInt(map.getOrDefault("dermestids", "0"));
        sc.parasiteAnts = Integer.parseInt(map.getOrDefault("parasiteAnts", map.getOrDefault("parasites", "0")));
        sc.parasiticMites = Integer.parseInt(map.getOrDefault("parasiticMites", "0"));
        sc.loyaltyModifierRemainingDays = deserializeJsonToMap(map.get("loyaltyModifierRemainingDays"));
        sc.pheromoneStormMonthsRemaining = Integer.parseInt(map.getOrDefault("pheromoneStormMonthsRemaining", "0"));
        sc.recentlyConqueredMonthsRemaining = Integer.parseInt(
                map.getOrDefault("recentlyConqueredMonthsRemaining", "0"));
        sc.recentlyIntegratedMonthsRemaining = Integer.parseInt(
                map.getOrDefault("recentlyIntegratedMonthsRemaining", "0"));
        sc.integrationDiplomatsDeployed = Integer.parseInt(
                map.getOrDefault("integrationDiplomatsDeployed", "0"));
        sc.nativeSpeciesId = Integer.parseInt(map.getOrDefault("nativeSpeciesId", "0"));
        sc.creatineDietMonthsRemaining = Integer.parseInt(map.getOrDefault("creatineDietMonthsRemaining", "0"));
        sc.totalDeaths = Integer.parseInt(map.getOrDefault("totalDeaths", "0"));
        
        // Nested structures
        sc.assignedRoleCounts = deserializeJsonToMap(map.get("assignedRoleCounts"));
        sc.warAssignedRoleCounts = deserializeJsonToMap(map.get("warAssignedRoleCounts"));
        sc.roleDisallowedSubtypesFlat = deserializeJsonToMap(map.get("roleDisallowedSubtypesFlat"));
        sc.warRoleDisallowedSubtypesFlat = deserializeJsonToMap(map.get("warRoleDisallowedSubtypesFlat"));
        sc.outgoingColonyDiplomatMissions = deserializeJsonToMap(map.get("outgoingColonyDiplomatMissions"));
        sc.incomingColonyDiplomatSupport = deserializeJsonToMap(map.get("incomingColonyDiplomatSupport"));
        sc.outgoingDynastyDiplomatMissions = deserializeJsonToMap(map.get("outgoingDynastyDiplomatMissions"));
        sc.localDeathStatistics = deserializeJsonToMap(map.get("localDeathStatistics"));
        sc.unlockedBuildingIds = deserializeJsonToList(map.get("unlockedBuildingIds"));
        sc.savedResourceSources = deserializeJsonToSources(map.get("savedResourceSources"));
        
        return sc;
    }

    private String serializeListToJson(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeMapToJson(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            sb.append("\"");
            sb.append(escapeJsonString(entry.getKey()));
            sb.append("\":");
            sb.append(entry.getValue());
            if (i < map.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private String serializeNestedIntegerMapToJson(Map<String, Map<String, Integer>> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : map.entrySet()) {
            sb.append("\"");
            sb.append(escapeJsonString(entry.getKey()));
            sb.append("\":");
            sb.append(serializeMapToJson(entry.getValue()));
            if (i < map.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private Map<String, Map<String, Integer>> deserializeJsonToNestedIntegerMap(String json) {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        if (json == null || json.length() <= 2) {
            return map;
        }
        Pattern entryPattern = Pattern.compile("\"([^\"]*)\":(\\{[^}]*\\})");
        Matcher matcher = entryPattern.matcher(json);
        while (matcher.find()) {
            map.put(unescapeJsonString(matcher.group(1)), deserializeJsonToMap(matcher.group(2)));
        }
        return map;
    }

    private String serializeStringListMapToJson(Map<String, List<String>> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            sb.append("\"");
            sb.append(escapeJsonString(entry.getKey()));
            sb.append("\":[");
            List<String> values = entry.getValue();
            for (int j = 0; j < values.size(); j++) {
                sb.append("\"").append(escapeJsonString(values.get(j))).append("\"");
                if (j < values.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            if (i < map.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private Map<String, List<String>> deserializeJsonToStringListMap(String json) {
        Map<String, List<String>> map = new HashMap<>();
        if (json == null || json.length() <= 2) {
            return map;
        }
        Pattern entryPattern = Pattern.compile("\"([^\"]*)\":\\[([^\\]]*)]");
        Matcher matcher = entryPattern.matcher(json);
        while (matcher.find()) {
            String dynastyId = unescapeJsonString(matcher.group(1));
            String listBody = matcher.group(2).trim();
            List<String> keys = new ArrayList<>();
            if (!listBody.isEmpty()) {
                Matcher keyMatcher = Pattern.compile("\"([^\"]*)\"").matcher(listBody);
                while (keyMatcher.find()) {
                    keys.add(unescapeJsonString(keyMatcher.group(1)));
                }
            }
            map.put(dynastyId, keys);
        }
        return map;
    }

    private String serializeStringMapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("\"");
            sb.append(escapeJsonString(entry.getKey()));
            sb.append("\":\"");
            sb.append(escapeJsonString(entry.getValue()));
            sb.append("\"");
            if (i < map.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private String serializeDoubleMapToJson(Map<String, Double> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            sb.append("\"").append(escapeJsonString(entry.getKey())).append("\":").append(entry.getValue());
            if (i < map.size() - 1) sb.append(",");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }
    
    private String serializeSourcesToJson(List<Savefile.SavedResourceSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < sources.size(); i++) {
            Savefile.SavedResourceSource s = sources.get(i);
            sb.append("{");
            sb.append("\"typeId\":").append(s.typeId).append(","); 
            sb.append("\"qty\":").append(s.currentQuantity).append(",");
            sb.append("\"init\":").append(s.initialQuantity).append(",");
            sb.append("\"x\":").append(s.x).append(",");
            sb.append("\"y\":").append(s.y);
            sb.append("}");
            if (i < sources.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String serializeHexesToJson(List<Savefile.SavedHex> hexes) {
        if (hexes == null || hexes.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < hexes.size(); i++) {
            Savefile.SavedHex h = hexes.get(i);
            sb.append("{");
            sb.append("\"q\":").append(h.q).append(",");
            sb.append("\"r\":").append(h.r).append(",");
            sb.append("\"b\":").append(h.biomeId).append(",");
            sb.append("\"c\":").append(h.hasColony).append(",");
            sb.append("\"t\":").append(h.timeOffset).append(","); 
            sb.append("\"w\":").append(h.weatherId).append(",");
            sb.append("\"nw\":").append(h.nonWaterResourceSourcesGenerated);
            sb.append("}");
            if (i < hexes.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeTunnelsToJson(List<Savefile.SavedTunnel> tunnels) {
        if (tunnels == null || tunnels.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < tunnels.size(); i++) {
            Savefile.SavedTunnel t = tunnels.get(i);
            sb.append("{");
            sb.append("\"qA\":").append(t.qA).append(",");
            sb.append("\"rA\":").append(t.rA).append(",");
            sb.append("\"qB\":").append(t.qB).append(",");
            sb.append("\"rB\":").append(t.rB).append(",");
            sb.append("\"p\":").append(t.progress).append(",");
            sb.append("\"tc\":").append(t.totalCost).append(",");
            sb.append("\"ic\":").append(t.isComplete);
            sb.append("}");
            if (i < tunnels.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeTradeProposalsToJson(List<Savefile.SavedCrossDynastyTradeProposal> proposals) {
        if (proposals == null || proposals.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < proposals.size(); i++) {
            Savefile.SavedCrossDynastyTradeProposal p = proposals.get(i);
            sb.append("{");
            sb.append("\"fromDynastyId\":").append(p.fromDynastyId).append(",");
            sb.append("\"originColonyId\":").append(p.originColonyId).append(",");
            sb.append("\"destinationColonyId\":").append(p.destinationColonyId).append(",");
            sb.append("\"request\":").append(p.request).append(",");
            sb.append("\"load\":").append(serializeDoubleMapToJson(p.loadByResourceId));
            sb.append("}");
            if (i < proposals.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeTradesToJson(List<Savefile.SavedTrade> trades) {
        if (trades == null || trades.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < trades.size(); i++) {
            Savefile.SavedTrade t = trades.get(i);
            sb.append("{");
            sb.append("\"qo\":").append(t.qOrigin).append(",");
            sb.append("\"ro\":").append(t.rOrigin).append(",");
            sb.append("\"qd\":").append(t.qDest).append(",");
            sb.append("\"rd\":").append(t.rDest).append(",");
            sb.append("\"ir\":").append(t.isRecurrent).append(",");
            sb.append("\"ib\":").append(t.isBilateral).append(",");
            sb.append("\"mid\":").append(t.methodId).append(",");
            sb.append("\"ia\":").append(t.isActive).append(",");
            sb.append("\"th\":").append(t.totalHours).append(",");
            sb.append("\"rh\":").append(t.remainingHours).append(",");
            sb.append("\"ret\":").append(t.isReturning).append(",");
            
            Map<String, Double> loadStrMap = new HashMap<>();
            for (Map.Entry<Integer, Double> e : t.load.entrySet()) loadStrMap.put(String.valueOf(e.getKey()), e.getValue());
            sb.append("\"load\":").append(serializeDoubleMapToJson(loadStrMap)).append(",");

            Map<String, Double> returnLoadStrMap = new HashMap<>();
            for (Map.Entry<Integer, Double> e : t.returnLoad.entrySet()) returnLoadStrMap.put(String.valueOf(e.getKey()), e.getValue());
            sb.append("\"rload\":").append(serializeDoubleMapToJson(returnLoadStrMap)).append(",");
            
            Map<String, Integer> transportStrMap = new HashMap<>();
            for (Map.Entry<Integer, Integer> e : t.transport.entrySet()) transportStrMap.put(String.valueOf(e.getKey()), e.getValue());
            sb.append("\"trans\":").append(serializeMapToJson(transportStrMap)).append(",");

            // Pending Updates
            sb.append("\"hpu\":").append(t.hasPendingUpdate).append(",");
            if (t.hasPendingUpdate) {
                sb.append("\"pir\":").append(t.pendingRecurrent).append(",");
                sb.append("\"pib\":").append(t.pendingIsBilateral).append(",");
                sb.append("\"pmid\":").append(t.pendingMethodId).append(",");
                
                Map<String, Double> pLoadStrMap = new HashMap<>();
                for (Map.Entry<Integer, Double> entry : t.pendingLoad.entrySet()) pLoadStrMap.put(String.valueOf(entry.getKey()), entry.getValue());
                sb.append("\"pload\":").append(serializeDoubleMapToJson(pLoadStrMap)).append(",");

                Map<String, Double> pReturnLoadStrMap = new HashMap<>();
                for (Map.Entry<Integer, Double> entry : t.pendingReturnLoad.entrySet()) pReturnLoadStrMap.put(String.valueOf(entry.getKey()), entry.getValue());
                sb.append("\"prload\":").append(serializeDoubleMapToJson(pReturnLoadStrMap)).append(",");

                Map<String, Integer> pTransportStrMap = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : t.pendingTransport.entrySet()) pTransportStrMap.put(String.valueOf(entry.getKey()), entry.getValue());
                sb.append("\"ptrans\":").append(serializeMapToJson(pTransportStrMap));
            } else {
                sb.setLength(sb.length() - 1);
            }
            
            sb.append("}");
            if (i < trades.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static final Pattern JSON_PAIR_PATTERN = Pattern.compile("\"([^\"]*)\":([0-9.]+)");

    private Map<String, Integer> deserializeJsonToMap(String json) {
        Map<String, Integer> map = new HashMap<>();
        if (json == null || json.length() <= 2) {
            return map;
        }
        Matcher m = JSON_PAIR_PATTERN.matcher(json);
        while (m.find()) {
            try {
                String key = unescapeJsonString(m.group(1));
                int value = (int) Double.parseDouble(m.group(2));
                map.put(key, value);
            } catch (Exception e) {
                System.err.println("Error parsing map pair: " + m.group(0));
            }
        }
        return map;
    }

    private static final Pattern JSON_STRING_PAIR_PATTERN = Pattern.compile("\"([^\"]*)\":\"([^\"]*)\"");

    private Map<String, String> deserializeJsonToStringMap(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.length() <= 2) {
            return map;
        }
        Matcher m = JSON_STRING_PAIR_PATTERN.matcher(json);
        while (m.find()) {
            map.put(unescapeJsonString(m.group(1)), unescapeJsonString(m.group(2)));
        }
        return map;
    }

    private Map<String, Double> deserializeJsonToDoubleMap(String json) {
        Map<String, Double> map = new HashMap<>();
        if (json == null || json.length() <= 2) {
            return map;
        }
        Matcher m = JSON_PAIR_PATTERN.matcher(json);
        while (m.find()) {
            try {
                String key = unescapeJsonString(m.group(1));
                double value = Double.parseDouble(m.group(2));
                map.put(key, value);
            } catch (Exception e) {
                System.err.println("Error parsing double map pair: " + m.group(0));
            }
        }
        return map;
    }
    
    private List<Integer> deserializeJsonToList(String json) {
        List<Integer> list = new ArrayList<>();
        if (json == null || json.length() <= 2) {
            return list;
        }
        
        String content = json.substring(1, json.length() - 1);
        if (content.isEmpty()) {
            return list;
        }
        
        String[] parts = content.split(",");
        for (String part : parts) {
            try {
                list.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                System.err.println("Error parsing list ID: " + part);
            }
        }
        return list;
    }
    
    private List<Savefile.SavedResourceSource> deserializeJsonToSources(String json) {
        List<Savefile.SavedResourceSource> list = new ArrayList<>();
        if (json == null || json.length() <= 2) return list;
        
        String inner = json.substring(1, json.length() - 1);
        if (inner.isEmpty()) return list;

        String[] objects = inner.split("\\},");
        
        for (String objStr : objects) {
            if (!objStr.endsWith("}")) objStr += "}";
            
            int typeId = 0; 
            int qty = 0;
            int init = 0;
            int x = 0;
            int y = 0;
            
            try {
                String clean = objStr.replace("{", "").replace("}", "");
                String[] fields = clean.split(",");
                for (String f : fields) {
                    String[] kv = f.split(":");
                    if (kv.length == 2) {
                        String k = kv[0].replace("\"", "").trim();
                        String v = kv[1].replace("\"", "").trim();
                        
                        if (k.equals("typeId")) typeId = Integer.parseInt(v); 
                        else if (k.equals("qty")) qty = Integer.parseInt(v);
                        else if (k.equals("init")) init = Integer.parseInt(v);
                        else if (k.equals("x")) x = Integer.parseInt(v);
                        else if (k.equals("y")) y = Integer.parseInt(v);
                    }
                }
                list.add(new Savefile.SavedResourceSource(typeId, qty, init, x, y));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return list;
    }
    
    private List<Savefile.SavedHex> deserializeJsonToHexes(String json) {
        List<Savefile.SavedHex> list = new ArrayList<>();
        if (json == null || json.length() <= 2) return list;
        
        String inner = json.substring(1, json.length() - 1);
        if (inner.isEmpty()) return list;
        
        String[] objects = inner.split("\\},");
        
        for (String objStr : objects) {
            if (!objStr.endsWith("}")) objStr += "}";
            
            int q = 0;
            int r = 0;
            int b = 0;
            boolean c = false;
            int t = 0; 
            int w = 1;
            int nw = 0;
            
            try {
                String clean = objStr.replace("{", "").replace("}", "");
                String[] fields = clean.split(",");
                for (String f : fields) {
                    String[] kv = f.split(":");
                    if (kv.length == 2) {
                        String k = kv[0].replace("\"", "").trim();
                        String v = kv[1].replace("\"", "").trim();
                        
                        if (k.equals("q")) q = Integer.parseInt(v);
                        else if (k.equals("r")) r = Integer.parseInt(v);
                        else if (k.equals("b")) b = Integer.parseInt(v);
                        else if (k.equals("c")) c = Boolean.parseBoolean(v);
                        else if (k.equals("t")) t = Integer.parseInt(v);
                        else if (k.equals("w")) w = Integer.parseInt(v);
                        else if (k.equals("nw")) nw = Integer.parseInt(v);
                    }
                }
                Savefile.SavedHex saved = new Savefile.SavedHex(q, r, b, c, t, w);
                saved.nonWaterResourceSourcesGenerated = nw;
                list.add(saved);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private List<Savefile.SavedTunnel> deserializeJsonToTunnels(String jsonArray) {
        List<Savefile.SavedTunnel> list = new ArrayList<>();
        if (jsonArray == null || !jsonArray.startsWith("[")) return list;
        
        int lastIdx = jsonArray.lastIndexOf("]");
        if (lastIdx <= 1) return list;
        
        String content = jsonArray.substring(1, lastIdx);
        int braceDepth = 0;
        int start = 0;
        for(int i=0; i<content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') braceDepth++;
            if (c == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    String tunnelJson = content.substring(start, i+1);
                    if (!tunnelJson.trim().isEmpty()) {
                        Savefile.SavedTunnel st = new Savefile.SavedTunnel();
                        Map<String, String> m = parseTopLevelJson(tunnelJson);
                        st.qA = Integer.parseInt(m.getOrDefault("qA", "0"));
                        st.rA = Integer.parseInt(m.getOrDefault("rA", "0"));
                        st.qB = Integer.parseInt(m.getOrDefault("qB", "0"));
                        st.rB = Integer.parseInt(m.getOrDefault("rB", "0"));
                        st.progress = Double.parseDouble(m.getOrDefault("p", "0"));
                        st.totalCost = Double.parseDouble(m.getOrDefault("tc", "0"));
                        st.isComplete = Boolean.parseBoolean(m.getOrDefault("ic", "false"));
                        list.add(st);
                    }
                    while(i+1 < content.length() && (content.charAt(i+1) == ',' || Character.isWhitespace(content.charAt(i+1)))) i++;
                    start = i+1;
                }
            }
        }
        return list;
    }

    private List<Savefile.SavedCrossDynastyTradeProposal> deserializeJsonToTradeProposals(String jsonArray) {
        List<Savefile.SavedCrossDynastyTradeProposal> list = new ArrayList<>();
        if (jsonArray == null || !jsonArray.startsWith("[")) {
            return list;
        }

        int lastIdx = jsonArray.lastIndexOf("]");
        if (lastIdx <= 1) {
            return list;
        }

        String content = jsonArray.substring(1, lastIdx);
        int braceDepth = 0;
        int start = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                braceDepth++;
            }
            if (c == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    String proposalJson = content.substring(start, i + 1);
                    if (!proposalJson.trim().isEmpty()) {
                        Savefile.SavedCrossDynastyTradeProposal saved = new Savefile.SavedCrossDynastyTradeProposal();
                        Map<String, String> map = parseTopLevelJson(proposalJson);
                        saved.fromDynastyId = Integer.parseInt(map.getOrDefault("fromDynastyId", "0"));
                        saved.originColonyId = Integer.parseInt(map.getOrDefault("originColonyId", "0"));
                        saved.destinationColonyId = Integer.parseInt(map.getOrDefault("destinationColonyId", "0"));
                        saved.request = Boolean.parseBoolean(map.getOrDefault("request", "false"));
                        saved.loadByResourceId = deserializeJsonToDoubleMap(map.get("load"));
                        list.add(saved);
                    }
                    while (i + 1 < content.length()
                            && (content.charAt(i + 1) == ',' || Character.isWhitespace(content.charAt(i + 1)))) {
                        i++;
                    }
                    start = i + 1;
                }
            }
        }
        return list;
    }

    private List<Savefile.SavedTrade> deserializeJsonToTrades(String jsonArray) {
        List<Savefile.SavedTrade> list = new ArrayList<>();
        if (jsonArray == null || !jsonArray.startsWith("[")) return list;
        
        int lastIdx = jsonArray.lastIndexOf("]");
        if (lastIdx <= 1) return list;
        
        String content = jsonArray.substring(1, lastIdx);
        int braceDepth = 0;
        int start = 0;
        for(int i=0; i<content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') braceDepth++;
            if (c == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    String tradeJson = content.substring(start, i+1);
                    if (!tradeJson.trim().isEmpty()) {
                        Savefile.SavedTrade st = new Savefile.SavedTrade();
                        Map<String, String> m = parseTopLevelJson(tradeJson);
                        st.qOrigin = Integer.parseInt(m.getOrDefault("qo", "0"));
                        st.rOrigin = Integer.parseInt(m.getOrDefault("ro", "0"));
                        st.qDest = Integer.parseInt(m.getOrDefault("qd", "0"));
                        st.rDest = Integer.parseInt(m.getOrDefault("rd", "0"));
                        st.isRecurrent = Boolean.parseBoolean(m.getOrDefault("ir", "false"));
                        st.isBilateral = Boolean.parseBoolean(m.getOrDefault("ib", "false"));
                        st.methodId = Integer.parseInt(m.getOrDefault("mid", "1"));
                        st.isActive = Boolean.parseBoolean(m.getOrDefault("ia", "false"));
                        st.totalHours = Integer.parseInt(m.getOrDefault("th", "0"));
                        st.remainingHours = Integer.parseInt(m.getOrDefault("rh", "0"));
                        st.isReturning = Boolean.parseBoolean(m.getOrDefault("ret", "false"));
                        
                        Map<String, Double> loadMap = deserializeJsonToDoubleMap(m.get("load"));
                        for (Map.Entry<String, Double> entry : loadMap.entrySet()) st.load.put(Integer.parseInt(entry.getKey()), entry.getValue());

                        Map<String, Double> rLoadMap = deserializeJsonToDoubleMap(m.get("rload"));
                        for (Map.Entry<String, Double> entry : rLoadMap.entrySet()) st.returnLoad.put(Integer.parseInt(entry.getKey()), entry.getValue());
                        
                        Map<String, Integer> transMap = deserializeJsonToMap(m.get("trans"));
                        for (Map.Entry<String, Integer> entry : transMap.entrySet()) st.transport.put(Integer.parseInt(entry.getKey()), entry.getValue());

                        // Pending
                        st.hasPendingUpdate = Boolean.parseBoolean(m.getOrDefault("hpu", "false"));
                        if (st.hasPendingUpdate) {
                            st.pendingRecurrent = Boolean.parseBoolean(m.getOrDefault("pir", "false"));
                            st.pendingIsBilateral = Boolean.parseBoolean(m.getOrDefault("pib", "false"));
                            st.pendingMethodId = Integer.parseInt(m.getOrDefault("pmid", "1"));

                            Map<String, Double> pLoadMap = deserializeJsonToDoubleMap(m.get("pload"));
                            for (Map.Entry<String, Double> entry : pLoadMap.entrySet()) st.pendingLoad.put(Integer.parseInt(entry.getKey()), entry.getValue());

                            Map<String, Double> pReturnLoadMap = deserializeJsonToDoubleMap(m.get("prload"));
                            for (Map.Entry<String, Double> entry : pReturnLoadMap.entrySet()) st.pendingReturnLoad.put(Integer.parseInt(entry.getKey()), entry.getValue());

                            Map<String, Integer> pTransMap = deserializeJsonToMap(m.get("ptrans"));
                            for (Map.Entry<String, Integer> entry : pTransMap.entrySet()) st.pendingTransport.put(Integer.parseInt(entry.getKey()), entry.getValue());
                        }
                        
                        list.add(st);
                    }
                    while(i+1 < content.length() && (content.charAt(i+1) == ',' || Character.isWhitespace(content.charAt(i+1)))) i++;
                    start = i+1;
                }
            }
        }
        return list;
    }

    private String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String unescapeJsonString(String str) {
        if (str == null) return null;
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\");
    }

    // --- Settings Management ---

    public void saveSettings(Engine engine) {
        File f = getSettingsFile();
        File tmp = new File(f.getAbsolutePath() + ".tmp");

        try (BufferedWriter w = Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("{");
            w.newLine();
            writeJsonLine(w, "language", engine.getLanguage(), false);
            writeJsonLine(w, "allowTurboMode", engine.isAllowTurboMode(), false);
            writeJsonLine(w, "screenSize", engine.getScreenSize(), false);
            writeJsonLine(w, "fullScreen", engine.isFullScreen(), false);
            writeJsonLine(w, "autosaveFrequency", engine.getAutosaveFrequency(), false);
            writeJsonLine(w, "daylightColorOverlayEnabled", engine.isDaylightColorOverlayEnabled(), false);
            writeJsonLine(w, "weatherColorOverlayEnabled", engine.isWeatherColorOverlayEnabled(), false);
            writeJsonLine(w, "arachnophobiaMode", engine.isArachnophobiaMode(), false);
            writeJsonLine(w, "masterVolume", engine.getMasterVolume(), false);
            writeJsonLine(w, "musicVolume", engine.getMusicVolume(), false);
            writeJsonLine(w, "sfxVolume", engine.getSfxVolume(), false);
            writeJsonLine(w, "musicMuted", engine.isMusicMuted(), false);
            writeJsonLine(w, "musicShuffle", engine.isMusicShuffle(), false);
            writeJsonLine(w, "pauseOnFocusLoss", engine.isPauseOnFocusLoss(), false);
            writeJsonLine(w, "confirmOnQuit", engine.isConfirmOnQuit(), false);
            writeJsonLine(w, "escapeKeyGameActions", engine.isEscapeKeyGameActions(), false);
            writeJsonLine(w, "showTooltips", engine.isShowTooltips(), false);
            writeJsonLine(w, "fuzzParasiteAnts", engine.isFuzzParasiteAnts(), false);
            writeJsonLine(w, "showAuditMenu", engine.isShowAuditMenu(), false);
            writeJsonLine(w, "overworldAutoRecenter", engine.isOverworldAutoRecenter(), false);
            writeJsonLine(w, "darkMode", engine.isDarkMode(), false);
            writeJsonLine(w, "frameRateCap", engine.getFrameRateCap(), false);
            writeJsonLine(w, "freeAbilities", engine.isFreeAbilities(), false);
            writeJsonLine(w, "infiniteResearch", engine.isInfiniteResearch(), false);
            writeJsonLine(w, "instantBuildings", engine.isInstantBuildings(), false);
            writeJsonLine(w, "assimilateAll", engine.isAssimilateAll(), false);
            writeJsonLine(w, "easyConquering", engine.isEasyConquering(), false);
            writeJsonLine(w, "instantIntegration", engine.isInstantIntegration(), false);
            writeJsonLine(w, "defaultRoleWorker", engine.getDefaultRoleWorker(), false);
            writeJsonLine(w, "defaultRoleSoldier", engine.getDefaultRoleSoldier(), false);
            writeJsonLine(w, "defaultRoleMajor", engine.getDefaultRoleMajor(), false);
            writeJsonLine(w, "defaultRolePrincess", engine.getDefaultRolePrincess(), false);
            writeJsonLine(w, "defaultRoleQueen", engine.getDefaultRoleQueen(), true);
            w.write("}");
            w.newLine();
            w.flush();
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            e.printStackTrace();
            return;
        }

        try {
            Files.move(tmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException amnse) {
            try {
                Files.move(tmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            e.printStackTrace();
        }
        System.out.println("[SaveManager] Saved global settings.");
    }

    public void loadSettings(Engine engine) {
        File f = getSettingsFile();
        if (!f.exists() || f.length() == 0) {
            System.out.println("[SaveManager] settings.json not found, using defaults.");
            return;
        }

        Map<String,String> m = new HashMap<>();
        try (BufferedReader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.equals("{") || line.equals("}") || line.isEmpty()) continue;

                int idx = line.indexOf(':');
                if (idx <= 0) continue;
                
                String k = line.substring(0, idx).trim();
                if (k.startsWith("\"")) k = k.substring(1);
                if (k.endsWith("\"")) k = k.substring(0, k.length() - 1);

                String v = line.substring(idx + 1).trim();
                if (v.endsWith(",")) v = v.substring(0, v.length() - 1);
                
                if (v.startsWith("\"")) {
                    v = v.substring(1);
                    if (v.endsWith("\"")) v = v.substring(0, v.length() - 1);
                } else if (v.equals("null")) {
                    v = ""; 
                }
                m.put(k, v);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            engine.setLanguage(m.getOrDefault("language", engine.getLanguage()));
            engine.setAllowTurboMode(Boolean.parseBoolean(m.getOrDefault("allowTurboMode", String.valueOf(engine.isAllowTurboMode()))));
            engine.setScreenSize(m.getOrDefault("screenSize", engine.getScreenSize()));
            engine.setFullScreen(Boolean.parseBoolean(m.getOrDefault("fullScreen", String.valueOf(engine.isFullScreen()))));
            engine.setAutosaveFrequency(Integer.parseInt(m.getOrDefault("autosaveFrequency", String.valueOf(engine.getAutosaveFrequency()))));
            boolean legacyVisualFilters = m.containsKey("visualFiltersEnabled")
                    && Boolean.parseBoolean(m.get("visualFiltersEnabled"));
            if (m.containsKey("daylightColorOverlayEnabled")) {
                engine.setDaylightColorOverlayEnabled(Boolean.parseBoolean(m.get("daylightColorOverlayEnabled")));
            } else if (m.containsKey("visualFiltersEnabled")) {
                engine.setDaylightColorOverlayEnabled(legacyVisualFilters);
            }
            if (m.containsKey("weatherColorOverlayEnabled")) {
                engine.setWeatherColorOverlayEnabled(Boolean.parseBoolean(m.get("weatherColorOverlayEnabled")));
            } else if (m.containsKey("visualFiltersEnabled")) {
                engine.setWeatherColorOverlayEnabled(legacyVisualFilters);
            }
            engine.setArachnophobiaMode(Boolean.parseBoolean(m.getOrDefault("arachnophobiaMode", String.valueOf(engine.isArachnophobiaMode()))));
            engine.setMasterVolume(Integer.parseInt(m.getOrDefault("masterVolume", String.valueOf(engine.getMasterVolume()))));
            engine.setMusicVolume(Integer.parseInt(m.getOrDefault("musicVolume", String.valueOf(engine.getMusicVolume()))));
            engine.setSfxVolume(Integer.parseInt(m.getOrDefault("sfxVolume", String.valueOf(engine.getSfxVolume()))));
            engine.setMusicMuted(Boolean.parseBoolean(m.getOrDefault("musicMuted", String.valueOf(engine.isMusicMuted()))));
            engine.setMusicShuffle(Boolean.parseBoolean(m.getOrDefault("musicShuffle", String.valueOf(engine.isMusicShuffle()))));
            engine.setPauseOnFocusLoss(Boolean.parseBoolean(m.getOrDefault("pauseOnFocusLoss", String.valueOf(engine.isPauseOnFocusLoss()))));
            engine.setConfirmOnQuit(Boolean.parseBoolean(m.getOrDefault("confirmOnQuit", String.valueOf(engine.isConfirmOnQuit()))));
            engine.setEscapeKeyGameActions(Boolean.parseBoolean(m.getOrDefault("escapeKeyGameActions",
                    String.valueOf(engine.isEscapeKeyGameActions()))));
            engine.setShowTooltips(Boolean.parseBoolean(m.getOrDefault("showTooltips", String.valueOf(engine.isShowTooltips()))));
            engine.setFuzzParasiteAnts(Boolean.parseBoolean(m.getOrDefault("fuzzParasiteAnts",
                    m.getOrDefault("fuzzParasites", String.valueOf(engine.isFuzzParasiteAnts())))));
            engine.setShowAuditMenu(Boolean.parseBoolean(m.getOrDefault("showAuditMenu",
                    String.valueOf(engine.isShowAuditMenu()))));
            engine.setOverworldAutoRecenter(Boolean.parseBoolean(m.getOrDefault("overworldAutoRecenter",
                    String.valueOf(engine.isOverworldAutoRecenter()))));
            engine.setDarkMode(Boolean.parseBoolean(m.getOrDefault("darkMode", String.valueOf(engine.isDarkMode()))));
            engine.setFrameRateCap(Integer.parseInt(m.getOrDefault("frameRateCap", String.valueOf(engine.getFrameRateCap()))));
            engine.setFreeAbilities(Boolean.parseBoolean(m.getOrDefault("freeAbilities", "false")));
            engine.setInfiniteResearch(Boolean.parseBoolean(m.getOrDefault("infiniteResearch", "false")));
            engine.setInstantBuildings(Boolean.parseBoolean(m.getOrDefault("instantBuildings", "false")));
            engine.setAssimilateAll(Boolean.parseBoolean(m.getOrDefault("assimilateAll", "false")));
            engine.setEasyConquering(Boolean.parseBoolean(m.getOrDefault("easyConquering", "false")));
            engine.setInstantIntegration(Boolean.parseBoolean(m.getOrDefault("instantIntegration", "false")));
            engine.setDefaultRoleWorker(Engine.sanitizeDefaultRoleId(
                    GameConstants.TYPE_WORKER,
                    Integer.parseInt(m.getOrDefault("defaultRoleWorker", String.valueOf(engine.getDefaultRoleWorker()))),
                    Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_WORKER).getId()));
            engine.setDefaultRoleSoldier(Engine.sanitizeDefaultRoleId(
                    GameConstants.TYPE_SOLDIER,
                    Integer.parseInt(m.getOrDefault("defaultRoleSoldier", String.valueOf(engine.getDefaultRoleSoldier()))),
                    Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_SOLDIER).getId()));
            engine.setDefaultRoleMajor(Engine.sanitizeDefaultRoleId(
                    GameConstants.TYPE_MAJOR,
                    Integer.parseInt(m.getOrDefault("defaultRoleMajor", String.valueOf(engine.getDefaultRoleMajor()))),
                    Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_MAJOR).getId()));
            engine.setDefaultRolePrincess(Engine.sanitizeDefaultRoleId(
                    GameConstants.TYPE_PRINCESS,
                    Integer.parseInt(m.getOrDefault("defaultRolePrincess", String.valueOf(engine.getDefaultRolePrincess()))),
                    Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_PRINCESS).getId()));
            engine.setDefaultRoleQueen(Engine.sanitizeDefaultRoleId(
                    GameConstants.TYPE_QUEEN,
                    Integer.parseInt(m.getOrDefault("defaultRoleQueen", String.valueOf(engine.getDefaultRoleQueen()))),
                    Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_QUEEN).getId()));

            System.out.println("[SaveManager] Global settings loaded.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[SaveManager] Error parsing settings.json, using defaults.");
        }
    }

    private String serializeWarsToJson(List<Savefile.SavedWar> wars) {
        if (wars == null || wars.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < wars.size(); i++) {
            Savefile.SavedWar war = wars.get(i);
            sb.append("{");
            sb.append("\"id\":").append(war.id).append(",");
            sb.append("\"dynastyIdA\":").append(war.dynastyIdA).append(",");
            sb.append("\"dynastyIdB\":").append(war.dynastyIdB).append(",");
            sb.append("\"startedWorldMonth\":").append(war.startedWorldMonth).append(",");
            sb.append("\"declaredByDynastyId\":").append(war.declaredByDynastyId).append(",");
            sb.append("\"displayName\":\"").append(escapeJsonString(war.displayName != null ? war.displayName : "")).append("\",");
            sb.append("\"militaryPowerAtStartA\":").append(war.militaryPowerAtStartA).append(",");
            sb.append("\"militaryPowerAtStartB\":").append(war.militaryPowerAtStartB).append(",");
            sb.append("\"endedWorldMonth\":").append(war.endedWorldMonth).append(",");
            sb.append("\"winnerDynastyId\":").append(war.winnerDynastyId).append(",");
            sb.append("\"conclusionKey\":\"").append(escapeJsonString(war.conclusionKey != null ? war.conclusionKey : "")).append("\",");
            sb.append("\"pendingPeaceOfferFromDynastyId\":").append(war.pendingPeaceOfferFromDynastyId).append(",");
            sb.append("\"progressPercent\":").append(war.progressPercent).append(",");
            sb.append("\"totalStages\":").append(war.totalStages).append(",");
            sb.append("\"aggressorStagesCaptured\":").append(war.aggressorStagesCaptured).append(",");
            sb.append("\"defenderStagesCaptured\":").append(war.defenderStagesCaptured).append(",");
            sb.append("\"stageProgress\":").append(war.stageProgress).append(",");
            sb.append("\"stagePhaseKey\":\"").append(escapeJsonString(
                    war.stagePhaseKey != null ? war.stagePhaseKey : GameConstants.WAR_STAGE_ACTIVE_CLASH.getPersistenceKey())).append("\",");
            sb.append("\"contestedColonyId\":").append(war.contestedColonyId).append(",");
            sb.append("\"stageAttackerDynastyId\":").append(war.stageAttackerDynastyId).append(",");
            sb.append("\"deployedActiveAttacker\":").append(war.deployedActiveAttacker).append(",");
            sb.append("\"deployedActiveDefender\":").append(war.deployedActiveDefender).append(",");
            sb.append("\"deployedReserveDefender\":").append(war.deployedReserveDefender).append(",");
            sb.append("\"aggressorCapitalColonyId\":").append(war.aggressorCapitalColonyId).append(",");
            sb.append("\"defenderCapitalColonyId\":").append(war.defenderCapitalColonyId).append(",");
            sb.append("\"redeployHoursRemaining\":").append(war.redeployHoursRemaining).append(",");
            sb.append("\"stageStartActiveAggressor\":").append(war.stageStartActiveAggressor).append(",");
            sb.append("\"stageStartActiveDefender\":").append(war.stageStartActiveDefender).append(",");
            sb.append("\"capturedColonyIds\":\"").append(escapeJsonString(
                    war.capturedColonyIds != null ? war.capturedColonyIds : "")).append("\",");
            sb.append("\"capturedByDynastyIds\":\"").append(escapeJsonString(
                    war.capturedByDynastyIds != null ? war.capturedByDynastyIds : "")).append("\",");
            sb.append("\"rebellionWar\":").append(war.rebellionWar);
            sb.append("}");
            if (i < wars.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private List<Savefile.SavedWar> deserializeJsonToWars(String jsonArray) {
        List<Savefile.SavedWar> list = new ArrayList<>();
        if (jsonArray == null || !jsonArray.startsWith("[")) {
            return list;
        }
        int lastIdx = jsonArray.lastIndexOf("]");
        if (lastIdx <= 1) {
            return list;
        }
        String content = jsonArray.substring(1, lastIdx);
        int braceDepth = 0;
        int start = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                braceDepth++;
            }
            if (c == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    String warJson = content.substring(start, i + 1).trim();
                    if (warJson.startsWith(",")) {
                        warJson = warJson.substring(1).trim();
                    }
                    if (!warJson.isEmpty() && warJson.startsWith("{")) {
                        Map<String, String> map = parseTopLevelJson(warJson);
                        Savefile.SavedWar war = new Savefile.SavedWar();
                        war.id = Integer.parseInt(map.getOrDefault("id", "0"));
                        war.dynastyIdA = Integer.parseInt(map.getOrDefault("dynastyIdA", "0"));
                        war.dynastyIdB = Integer.parseInt(map.getOrDefault("dynastyIdB", "0"));
                        war.startedWorldMonth = Integer.parseInt(map.getOrDefault("startedWorldMonth", "0"));
                        war.declaredByDynastyId = Integer.parseInt(map.getOrDefault("declaredByDynastyId", "0"));
                        war.displayName = map.getOrDefault("displayName", "");
                        war.militaryPowerAtStartA = Integer.parseInt(map.getOrDefault("militaryPowerAtStartA", "0"));
                        war.militaryPowerAtStartB = Integer.parseInt(map.getOrDefault("militaryPowerAtStartB", "0"));
                        war.endedWorldMonth = Integer.parseInt(map.getOrDefault("endedWorldMonth", String.valueOf(War.ACTIVE_END_MONTH)));
                        war.winnerDynastyId = Integer.parseInt(map.getOrDefault("winnerDynastyId", "0"));
                        war.conclusionKey = map.getOrDefault("conclusionKey", null);
                        if (war.conclusionKey != null && war.conclusionKey.isEmpty()) {
                            war.conclusionKey = null;
                        }
                        war.pendingPeaceOfferFromDynastyId = Integer.parseInt(map.getOrDefault("pendingPeaceOfferFromDynastyId", "0"));
                        war.progressPercent = parseFloat(map.get("progressPercent"), 50f);
                        war.totalStages = Integer.parseInt(map.getOrDefault("totalStages", "0"));
                        war.aggressorStagesCaptured = Integer.parseInt(map.getOrDefault("aggressorStagesCaptured", "0"));
                        war.defenderStagesCaptured = Integer.parseInt(map.getOrDefault("defenderStagesCaptured", "0"));
                        war.stageProgress = parseFloat(map.get("stageProgress"), 0f);
                        war.stagePhaseKey = map.getOrDefault("stagePhaseKey", GameConstants.WAR_STAGE_ACTIVE_CLASH.getPersistenceKey());
                        war.contestedColonyId = Integer.parseInt(map.getOrDefault("contestedColonyId", "0"));
                        war.stageAttackerDynastyId = Integer.parseInt(map.getOrDefault("stageAttackerDynastyId", "0"));
                        war.deployedActiveAttacker = Integer.parseInt(map.getOrDefault("deployedActiveAttacker", "0"));
                        war.deployedActiveDefender = Integer.parseInt(map.getOrDefault("deployedActiveDefender", "0"));
                        war.deployedReserveDefender = Integer.parseInt(map.getOrDefault("deployedReserveDefender", "0"));
                        war.aggressorCapitalColonyId = Integer.parseInt(map.getOrDefault("aggressorCapitalColonyId", "0"));
                        war.defenderCapitalColonyId = Integer.parseInt(map.getOrDefault("defenderCapitalColonyId", "0"));
                        war.redeployHoursRemaining = Integer.parseInt(map.getOrDefault("redeployHoursRemaining",
                                map.getOrDefault("redeployDaysRemaining", "0")));
                        war.stageStartActiveAggressor = Integer.parseInt(map.getOrDefault("stageStartActiveAggressor", "0"));
                        war.stageStartActiveDefender = Integer.parseInt(map.getOrDefault("stageStartActiveDefender", "0"));
                        war.capturedColonyIds = map.getOrDefault("capturedColonyIds", "");
                        war.capturedByDynastyIds = map.getOrDefault("capturedByDynastyIds", "");
                        war.dynastyNameA = map.getOrDefault("dynastyNameA", null);
                        war.dynastyNameB = map.getOrDefault("dynastyNameB", null);
                        war.winnerDynastyName = map.getOrDefault("winnerDynastyName", null);
                        war.rebellionWar = Boolean.parseBoolean(map.getOrDefault("rebellionWar", "false"));
                        if (War.isValidRecord(war.dynastyIdA, war.dynastyIdB)) {
                            list.add(war);
                        }
                    }
                    start = i + 1;
                }
            }
        }
        return list;
    }

    private String serializeWorldHistoryToJson(List<Savefile.SavedWorldHistoryEvent> events) {
        if (events == null || events.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < events.size(); i++) {
            Savefile.SavedWorldHistoryEvent event = events.get(i);
            sb.append("{");
            sb.append("\"year\":").append(event.year).append(",");
            sb.append("\"month\":").append(event.month).append(",");
            sb.append("\"day\":").append(event.day).append(",");
            sb.append("\"hour\":").append(event.hour).append(",");
            sb.append("\"minute\":").append(event.minute).append(",");
            sb.append("\"type\":\"").append(escapeJsonString(event.type != null ? event.type : "")).append("\",");
            sb.append("\"messageKey\":\"").append(escapeJsonString(event.messageKey != null ? event.messageKey : "")).append("\",");
            sb.append("\"args\":").append(serializeStringListToJson(event.args)).append(",");
            sb.append("\"relatedDynastyId\":").append(event.relatedDynastyId).append(",");
            sb.append("\"relatedColonyId\":").append(event.relatedColonyId).append(",");
            sb.append("\"relatedWarId\":").append(event.relatedWarId);
            sb.append("}");
            if (i < events.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeStringListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(escapeJsonString(list.get(i) != null ? list.get(i) : "")).append("\"");
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private List<Savefile.SavedWorldHistoryEvent> deserializeJsonToWorldHistory(String jsonArray) {
        List<Savefile.SavedWorldHistoryEvent> list = new ArrayList<>();
        if (jsonArray == null || !jsonArray.startsWith("[")) {
            return list;
        }
        int lastIdx = jsonArray.lastIndexOf("]");
        if (lastIdx <= 1) {
            return list;
        }
        String content = jsonArray.substring(1, lastIdx);
        int braceDepth = 0;
        int start = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                braceDepth++;
            }
            if (c == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    String eventJson = content.substring(start, i + 1).trim();
                    if (eventJson.startsWith(",")) {
                        eventJson = eventJson.substring(1).trim();
                    }
                    if (!eventJson.isEmpty() && eventJson.startsWith("{")) {
                        Map<String, String> map = parseTopLevelJson(eventJson);
                        Savefile.SavedWorldHistoryEvent event = new Savefile.SavedWorldHistoryEvent();
                        event.year = Integer.parseInt(map.getOrDefault("year", "0"));
                        event.month = Integer.parseInt(map.getOrDefault("month", "1"));
                        event.day = Integer.parseInt(map.getOrDefault("day", "1"));
                        event.hour = Integer.parseInt(map.getOrDefault("hour", "0"));
                        event.minute = Integer.parseInt(map.getOrDefault("minute", "0"));
                        event.type = map.getOrDefault("type", "");
                        event.messageKey = map.getOrDefault("messageKey", "");
                        event.args = deserializeJsonToStringList(map.get("args"));
                        event.relatedDynastyId = Integer.parseInt(map.getOrDefault("relatedDynastyId", "-1"));
                        event.relatedColonyId = Integer.parseInt(map.getOrDefault("relatedColonyId", "-1"));
                        event.relatedWarId = Integer.parseInt(map.getOrDefault("relatedWarId", "-1"));
                        list.add(event);
                    }
                    start = i + 1;
                }
            }
        }
        return list;
    }

    private List<String> deserializeJsonToStringList(String json) {
        List<String> list = new ArrayList<>();
        if (json == null || json.length() <= 2) {
            return list;
        }
        Matcher keyMatcher = Pattern.compile("\"([^\"]*)\"").matcher(json);
        while (keyMatcher.find()) {
            list.add(unescapeJsonString(keyMatcher.group(1)));
        }
        return list;
    }

    private static float parseFloat(String value, float defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
