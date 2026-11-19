package com.grimidk.formicempire.classes.infrasctructure.managers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.hunt.KnownHuntTarget;
import com.grimidk.formicempire.classes.entities.invasion.InvasionAlert;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyHuntService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyInvasionService;
import com.grimidk.formicempire.classes.entities.world.Bridge;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.ResourceSource;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.world.Tunnel;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.AntModService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyPopulationService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.util.GamePaths;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.assets.ClasspathTextFiles;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.ResearchTreeGraph;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

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
    private final AtomicBoolean autosaveInFlight = new AtomicBoolean(false);

    public SaveManager() {
        this.savesDir = GamePaths.resolveSavesDirectory();
        System.out.println("[SaveManager] Saves directory: " + savesDir.getAbsolutePath());
    }

    public SaveManager(File savesDir) {
        this.savesDir = savesDir;
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

    public static String getAppVersion() {
        return ClasspathTextFiles.load(AssetStyles.META_VERSION, LanguageStrings.VERSION_UNAVAILABLE).trim();
    }

    private void writeSaveToFile(Savefile save, File targetFile) throws IOException {
        if (save != null) {
            save.setVersion(getAppVersion());
        }
        SaveCodec.writeCompressed(save, targetFile);
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
        if (isPlayerDynastyAboutToDieWithoutQueen(w)) {
            System.out.println("[SaveManager] Skipping autosave: player dynasty is about to die without a queen.");
            return;
        }
        int slotId = 0;
        try {
            slotId = w.getSaveSlotId();
        } catch (Exception ignore) {
            slotId = 0;
        }
        if (slotId == 0) {
            System.out.println("[SaveManager] Skipping autosave for new game (Slot ID 0).");
            return;
        }
        if (!autosaveInFlight.compareAndSet(false, true)) {
            return;
        }
        final int targetSlot = slotId;
        final Savefile save = new Savefile(targetSlot, LanguageStrings.get(LanguageStrings.SAVE_AUTOSAVE_NAME));
        try {
            populateSavefileFromGame(save, w, e != null ? e : w.getEngine());
        } catch (RuntimeException ex) {
            autosaveInFlight.set(false);
            throw ex;
        }
        try {
            SHARED_EXECUTOR.submit(() -> {
                try {
                    if (applyManualMetadataAndShouldSkip(save, targetSlot)) {
                        return;
                    }
                    File autosaveFile = getSlotFile(targetSlot, "autosave");
                    writeSaveToFile(save, autosaveFile);
                    System.out.println("[SaveManager] Autosaved world to " + autosaveFile.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    autosaveInFlight.set(false);
                }
            });
        } catch (RuntimeException ex) {
            autosaveInFlight.set(false);
            throw ex;
        }
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
            File metaFile = SaveCodec.getMetadataFile(f);
            if (metaFile.exists()) {
                try {
                    metaFile.delete();
                } catch (Exception ignore) {}
            }
        }
        return removed;
    }

    public static boolean isPlayerDynastyAboutToDieWithoutQueen(World w) {
        if (w == null || w.getDynastys() == null) {
            return false;
        }
        for (Dynasty d : w.getDynastys()) {
            if (d == null || d.isDefeated() || !d.isPlayer()) {
                continue;
            }
            if (isDynastyAboutToDieWithoutQueen(d)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDynastyAboutToDieWithoutQueen(Dynasty dynasty) {
        if (dynasty == null || dynasty.isDefeated() || dynasty.getColonies() == null) {
            return false;
        }
        boolean anyQueenInDynasty = false;
        for (Colony c : dynasty.getColonies()) {
            if (c != null && !c.getQueens().isEmpty()) {
                anyQueenInDynasty = true;
                break;
            }
        }
        if (!anyQueenInDynasty) {
            return true;
        }
        for (Colony c : dynasty.getColonies()) {
            if (c != null && c.getQueens().isEmpty() && c.getDaysWithoutQueen() >= 5) {
                return true;
            }
        }
        return false;
    }

    private boolean applyManualMetadataAndShouldSkip(Savefile save, int slotId) {
        if (save == null || slotId <= 0) {
            return false;
        }
        File manualFile = getSlotFile(slotId, "manual");
        if (!manualFile.exists() || manualFile.length() == 0) {
            return false;
        }
        try {
            Savefile existing = loadMetadataOrFile(manualFile);
            if (existing == null) {
                return false;
            }
            if (existing.getName() != null && !existing.getName().trim().isEmpty()) {
                save.setName(existing.getName());
            }
            int existingPlay = existing.getPlayTime() != 0 ? existing.getPlayTime() : computePlayTimeFromSave(existing);
            if (existingPlay > save.getPlayTime()) {
                System.out.println("[SaveManager] Manual save for slot " + slotId + " has more playtime than autosave - skipping autosave");
                return true;
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    public void saveWorldToSlot(World w, int slotId) {
        if (w == null) return;
        if (isPlayerDynastyAboutToDieWithoutQueen(w)) {
            System.out.println("[SaveManager] Skipping save: player dynasty is about to die without a queen.");
            return;
        }
        
        if (slotId == 0) {
            System.out.println("[SaveManager] Skipping autosave for new game (Slot ID 0).");
            return;
        }

        File f = getSlotFile(slotId, "autosave");
        Savefile save = new Savefile(slotId, LanguageStrings.get(LanguageStrings.SAVE_AUTOSAVE_NAME));
        populateSavefileFromGame(save, w, w.getEngine());

        try {
            if (applyManualMetadataAndShouldSkip(save, slotId)) {
                return;
            }
            
            writeSaveToFile(save, f);
            
            System.out.println("[SaveManager] Autosaved world to " + f.getAbsolutePath());
        
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private Savefile loadMetadataOrFile(File file) throws IOException {
        Savefile meta = SaveCodec.readMetadata(file);
        if (meta != null) {
            return meta;
        }
        Savefile full = SaveCodec.read(file);
        if (full != null) {
            SaveCodec.writeMetadataSidecar(full, file);
        }
        return full;
    }

    public Savefile loadSlotMetadata(int slotId) {
        File manualFile = getSlotFile(slotId, "manual");
        File autosaveFile = getSlotFile(slotId, "autosave");
        Savefile manualSave = null;
        Savefile autosave = null;

        if (manualFile.exists() && manualFile.length() > 0) {
            try {
                manualSave = loadMetadataOrFile(manualFile);
            } catch (Exception e) {
                System.err.println("[SaveManager] Failed to read manual save metadata for slot " + slotId);
            }
        }
        if (autosaveFile.exists() && autosaveFile.length() > 0) {
            try {
                autosave = loadMetadataOrFile(autosaveFile);
            } catch (Exception e) {
                System.err.println("[SaveManager] Failed to read autosave metadata for slot " + slotId);
            }
        }
        if (manualSave != null && autosave != null) {
            return autosave.getTimestamp() > manualSave.getTimestamp() ? autosave : manualSave;
        }
        return manualSave != null ? manualSave : autosave;
    }

    public Savefile loadAutosaveForSlot(int slotId) {
        if (slotId <= 0) return null;

        File autos = getSlotFile(slotId, "autosave");
        if (!autos.exists() || autos.length() == 0) {
            return null; 
        }

        try {
            return SaveCodec.read(autos);
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
            try {
                manualSave = SaveCodec.read(manualFile);
            } catch (Exception e) {
                System.err.println("[SaveManager] Failed to read manual save for slot " + slotId);
                e.printStackTrace();
            }
        }

        if (autosaveFile.exists() && autosaveFile.length() > 0) {
            try {
                autosave = SaveCodec.read(autosaveFile);
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
        if (isPlayerDynastyAboutToDieWithoutQueen(w)) {
            System.out.println("[SaveManager] Skipping manual save: player dynasty is about to die without a queen.");
            return;
        }
        
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
        save.setVersion(getAppVersion());
        save.setTimestamp(System.currentTimeMillis());
        save.setMinute(w.getMinute());
        save.setHour(w.getHour());
        save.setDay(w.getDay());
        save.setMonth(w.getMonth());
        save.setYear(w.getYear());
        save.setPlayTime(computePlayTime(w));
        save.setWorldRadius(w.getWorldRadius());
        save.setContinentCoreRadius(w.getContinentCoreRadius());
        save.setWorldWidth(w.getWorldWidth());
        save.setWorldHeight(w.getWorldHeight());
        if (engine != null && engine.hasAchievementTaintingSandbox()) {
            w.disableAchievements();
        }
        if (!w.allowsAchievements()) {
            save.disableAchievements();
        } else {
            save.setAllowsAchievements(true);
        }
        save.setSpectatorMode(w.isSpectatorMode());
        Hex spawnHex = w.getSpawnHex();
        if (spawnHex != null && spawnHex.getBiome() != null) {
            save.setStarterBiomeId(spawnHex.getBiome().getId());
        }

        if (w.getDynastys() != null) {
            for (Dynasty dynasty : w.getDynastys()) {
                if (dynasty.isPlayer()) {
                    save.setPlayerDynastyTitleId(dynasty.getTitleId());
                    save.setPlayerDynastyTitleKey(dynasty.getTitleKey());
                    if (dynasty.getStartingDiet() != null) {
                        save.setStarterDietId(dynasty.getStartingDiet().getId());
                    }
                    break;
                }
            }
        }
        
        List<Savefile.SavedHex> hexList = new ArrayList<>();
        List<Savefile.SavedColony> colonyList = new ArrayList<>();
        List<Savefile.SavedDynasty> dynastyList = new ArrayList<>();
        Colony playerSummaryColony = null;
        Colony playerCapitalColony = null;

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
                sc.aiPersonality = dynasty.getAiPersonality() != null
                        ? dynasty.getAiPersonality().toPersistenceKey()
                        : null;
                sc.isDefeated = dynasty.isDefeated();
                sc.rankName = dynasty.getRank() != null ? dynasty.getRank().getNameKey() : LanguageStrings.RANK_ANT;
                sc.researchPoints = dynasty.getResearchPoints();
                sc.totalNuptialFlights = dynasty.getTotalNuptialFlights();
                sc.diplomatsSentTotal = dynasty.getDiplomatsSentTotal();
                sc.speciesId = dynasty.getSpecies() != null ? dynasty.getSpecies().getId() : 1;
                sc.startingDietId = dynasty.getStartingDiet() != null ? dynasty.getStartingDiet().getId() : 1;
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
                sc.spySupportToDynasty = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : dynasty.copySpySupportToDynasty().entrySet()) {
                    sc.spySupportToDynasty.put(String.valueOf(entry.getKey()), entry.getValue());
                }
                sc.intelligenceToward = new HashMap<>();
                for (Map.Entry<Integer, Double> entry : dynasty.copyIntelligenceToward().entrySet()) {
                    sc.intelligenceToward.put(String.valueOf(entry.getKey()), entry.getValue());
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
                sc.knownHexKeys = new ArrayList<>(dynasty.getKnownHexKeys());
                sc.exploredHexCount = dynasty.getExploredHexCount();
                
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
                if (dynasty.getBridges() != null) {
                    for (Bridge b : dynasty.getBridges()) {
                        Savefile.SavedTunnel sb = new Savefile.SavedTunnel();
                        sb.qA = b.getHexA().getQ();
                        sb.rA = b.getHexA().getR();
                        sb.qB = b.getHexB().getQ();
                        sb.rB = b.getHexB().getR();
                        sb.progress = b.getProgress();
                        sb.totalCost = b.getTotalCost();
                        sb.isComplete = b.isComplete();
                        sc.bridges.add(sb);
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
                for (Map.Entry<AntClass, Integer> e : t.getTransport().entrySet()) {
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
                        for (Map.Entry<AntClass, Integer> e : t.getPendingTransport().entrySet()) {
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
                if (h.getRiverEdges() != null) {
                    System.arraycopy(h.getRiverEdges(), 0, sh.riverEdges, 0, Math.min(6, h.getRiverEdges().length));
                }
                sh.continentId = h.getContinentId();
                sh.waterDistance = h.getWaterDistance();
                sh.coastal = h.isCoastal();
                sh.elevation = h.getElevation();
                sh.hydrology = h.getHydrology();
                sh.island = h.isIsland();
                sh.plateId = h.getPlateId();
                sh.disasterTypeId = h.getDisasterType() != null ? h.getDisasterType().getId() : 0;
                sh.disasterAgeHours = h.getDisasterAgeHours();
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
                    sc.modHatchRatesFlat = AntModService.flattenModRates(c.getModHatchRates());
                    sc.workerMods = AntModService.aggregateModCounts(c.getWorkers());
                    sc.soldierMods = AntModService.aggregateModCounts(c.getSoldiers());
                    sc.majorMods = AntModService.aggregateModCounts(c.getMajors());
                    sc.princessMods = AntModService.aggregateModCounts(c.getPrincesses());
                    sc.queenMods = AntModService.aggregateModCounts(c.getQueens());
                    sc.eggAges = ColonyPopulationService.aggregateAgeCounts(c.getEggs());
                    sc.larvaAges = ColonyPopulationService.aggregateAgeCounts(c.getLarvae());
                    sc.pupaAges = ColonyPopulationService.aggregateAgeCounts(c.getPupae());
                    sc.workerAges = ColonyPopulationService.aggregateAgeCounts(c.getWorkers());
                    sc.soldierAges = ColonyPopulationService.aggregateAgeCounts(c.getSoldiers());
                    sc.majorAges = ColonyPopulationService.aggregateAgeCounts(c.getMajors());
                    sc.droneAges = ColonyPopulationService.aggregateAgeCounts(c.getDrones());
                    sc.princessAges = ColonyPopulationService.aggregateAgeCounts(c.getPrincesses());
                    sc.queenAges = ColonyPopulationService.aggregateAgeCounts(c.getQueens());
                    
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
                    sc.roleDisallowedModsFlat = c.flattenPeaceRoleDisallowedMods();
                    sc.warRoleDisallowedModsFlat = c.flattenWarRoleDisallowedMods();
                    for (Map.Entry<Integer, Integer> entry : c.getOutgoingColonyDiplomatMissions().entrySet()) {
                        sc.outgoingColonyDiplomatMissions.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    for (Map.Entry<Integer, Integer> entry : c.getIncomingColonyDiplomatSupport().entrySet()) {
                        sc.incomingColonyDiplomatSupport.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    for (Map.Entry<Integer, Integer> entry : c.getOutgoingDynastyDiplomatMissions().entrySet()) {
                        sc.outgoingDynastyDiplomatMissions.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    for (Map.Entry<Integer, Integer> entry : c.getOutgoingDynastySpyMissions().entrySet()) {
                        sc.outgoingDynastySpyMissions.put(String.valueOf(entry.getKey()), entry.getValue());
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
                    sc.nextHuntTargetId = c.getNextHuntTargetIdSeed();
                    for (KnownHuntTarget target : c.getKnownHuntTargets()) {
                        sc.knownHuntTargets.add(new Savefile.SavedHuntTarget(
                                target.getId(),
                                target.getSpeciesId(),
                                target.getOverworldX(),
                                target.getOverworldY(),
                                target.getDiscoveredWorldDay(),
                                target.getEscapeWorldDay()));
                    }
                    sc.activeHuntExpeditions = ColonyHuntService.toSavedExpeditions(c);
                    sc.activeHuntExpedition = sc.activeHuntExpeditions.size() == 1
                            ? sc.activeHuntExpeditions.get(0)
                            : null;
                    sc.nextInvasionAlertId = c.getNextInvasionAlertIdSeed();
                    sc.invasionAlerts = ColonyInvasionService.toSavedAlerts(c);
                    sc.activeInvasionDefenses = ColonyInvasionService.toSavedDefenses(c);
                    colonyList.add(sc);
                    
                    if (c.isPlayer()) {
                        if (c.isActive()) {
                            playerSummaryColony = c;
                        } else if (c.isCapital()) {
                            playerCapitalColony = c;
                        }
                    }
                }
            }
        }

        Colony summaryColony = playerSummaryColony != null ? playerSummaryColony : playerCapitalColony;
        if (summaryColony != null) {
            applyPlayerColonySummary(save, summaryColony);
        }
        
        save.setWorldHexes(hexList);
        save.setColonies(colonyList);
    }

    private static void applyPlayerColonySummary(Savefile save, Colony c) {
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
            writeJsonLine(w, "disablePopups", engine.isDisablePopups(), false);
            writeJsonLine(w, "fuzzParasiteAnts", engine.isFuzzParasiteAnts(), false);
            writeJsonLine(w, "showAuditMenu", engine.isShowAuditMenu(), false);
            writeJsonLine(w, "showFpsCounter", engine.isShowFpsCounter(), false);
            writeJsonLine(w, "overworldAutoRecenter", engine.isOverworldAutoRecenter(), false);
            writeJsonLine(w, "darkMode", engine.isDarkMode(), false);
            writeJsonLine(w, "frameRateCap", engine.getFrameRateCap(), false);
            writeJsonLine(w, "freeAbilities", engine.isFreeAbilities(), false);
            writeJsonLine(w, "infiniteResearch", engine.isInfiniteResearch(), false);
            writeJsonLine(w, "instantBuildings", engine.isInstantBuildings(), false);
            writeJsonLine(w, "assimilateAll", engine.isAssimilateAll(), false);
            writeJsonLine(w, "mapEditorEnabled", engine.isMapEditorEnabled(), false);
            writeJsonLine(w, "easyConquering", engine.isEasyConquering(), false);
            writeJsonLine(w, "instantIntegration", engine.isInstantIntegration(), false);
            writeJsonLine(w, "cheatFogOfWarToggle", engine.isCheatFogOfWarToggle(), false);
            writeJsonLine(w, "cheatRevealMap", engine.isCheatRevealMap(), false);
            writeJsonLine(w, "cheatRandomDisaster", engine.isCheatRandomDisaster(), false);
            writeJsonLine(w, "cheatRandomWar", engine.isCheatRandomWar(), false);
            writeJsonLine(w, "animateWorldGen", engine.isAnimateWorldGen(), false);
            writeJsonLine(w, "defaultRoleWorker", engine.getDefaultRoleWorker(), false);
            writeJsonLine(w, "defaultRoleSoldier", engine.getDefaultRoleSoldier(), false);
            writeJsonLine(w, "defaultRoleMajor", engine.getDefaultRoleMajor(), false);
            writeJsonLine(w, "defaultRolePrincess", engine.getDefaultRolePrincess(), false);
            writeJsonLine(w, "defaultRoleQueen", engine.getDefaultRoleQueen(), false);
            writeJsonLine(w, "mapLayerBorders", engine.isMapLayerBorders(), false);
            writeJsonLine(w, "mapLayerRivers", engine.isMapLayerRivers(), false);
            writeJsonLine(w, "mapLayerMeridian", engine.isMapLayerMeridian(), false);
            writeJsonLine(w, "mapLayerEquator", engine.isMapLayerEquator(), false);
            writeJsonLine(w, "mapLayerBiomeIcons", engine.isMapLayerBiomeIcons(), false);
            writeJsonLine(w, "mapLayerColonyRanks", engine.isMapLayerColonyRanks(), false);
            writeJsonLine(w, "mapLayerTrades", engine.isMapLayerTrades(), false);
            writeJsonLine(w, "mapLayerTunnels", engine.isMapLayerTunnels(), false);
            writeJsonLine(w, "mapLayerBattles", engine.isMapLayerBattles(), false);
            writeJsonLine(w, "mapLayerGeographicNames", engine.isMapLayerGeographicNames(), false);
            writeJsonLine(w, "mapLayerCapitals", engine.isMapLayerCapitals(), false);
            writeJsonLine(w, "mapLayerLabelContinents", engine.isMapLayerLabelContinents(), false);
            writeJsonLine(w, "mapLayerLabelIceCaps", engine.isMapLayerLabelIceCaps(), false);
            writeJsonLine(w, "mapLayerLabelArchipelagos", engine.isMapLayerLabelArchipelagos(), false);
            writeJsonLine(w, "mapLayerLabelOceans", engine.isMapLayerLabelOceans(), false);
            writeJsonLine(w, "mapLayerLabelTrenches", engine.isMapLayerLabelTrenches(), false);
            writeJsonLine(w, "mapLayerLabelMountains", engine.isMapLayerLabelMountains(), false);
            writeJsonLine(w, "mapLayerLabelValleys", engine.isMapLayerLabelValleys(), false);
            writeJsonLine(w, "mapLayerLabelLakes", engine.isMapLayerLabelLakes(), false);
            writeJsonLine(w, "mapLayerLabelRivers", engine.isMapLayerLabelRivers(), false);
            writeJsonLine(w, "mapLayerLabelPeninsulas", engine.isMapLayerLabelPeninsulas(), false);
            writeJsonLine(w, "mapLayerDayNight", engine.isMapLayerDayNight(), false);
            writeJsonLine(w, "mapLayerWeather", engine.isMapLayerWeather(), false);
            writeJsonLine(w, "mapLayerDisasters", engine.isMapLayerDisasters(), false);
            
            // Serialize Upgrade Coordinates
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (com.grimidk.formicempire.classes.constants.unlocks.Upgrade u : com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks.getUpgrades()) {
                if (u.getGridX() != u.getDefaultGridX() || u.getGridY() != u.getDefaultGridY()) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(u.getId()).append("\":\"").append(u.getGridX()).append(",").append(u.getGridY()).append("\"");
                    first = false;
                }
            }
            sb.append("}");
            writeJsonLine(w, "upgradePositions", sb.toString(), true);

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
        writeUpgradePositionsFile();
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
            engine.setDisablePopups(Boolean.parseBoolean(m.getOrDefault("disablePopups", String.valueOf(engine.isDisablePopups()))));
            engine.setFuzzParasiteAnts(Boolean.parseBoolean(m.getOrDefault("fuzzParasiteAnts",
                    m.getOrDefault("fuzzParasites", String.valueOf(engine.isFuzzParasiteAnts())))));
            engine.setShowAuditMenu(Boolean.parseBoolean(m.getOrDefault("showAuditMenu",
                    String.valueOf(engine.isShowAuditMenu()))));
            engine.setShowFpsCounter(Boolean.parseBoolean(m.getOrDefault("showFpsCounter",
                    String.valueOf(engine.isShowFpsCounter()))));
            engine.setOverworldAutoRecenter(Boolean.parseBoolean(m.getOrDefault("overworldAutoRecenter",
                    String.valueOf(engine.isOverworldAutoRecenter()))));
            engine.setDarkMode(Boolean.parseBoolean(m.getOrDefault("darkMode", String.valueOf(engine.isDarkMode()))));
            engine.setFrameRateCap(Integer.parseInt(m.getOrDefault("frameRateCap", String.valueOf(engine.getFrameRateCap()))));
            engine.setFreeAbilities(Boolean.parseBoolean(m.getOrDefault("freeAbilities", "false")));
            engine.setInfiniteResearch(Boolean.parseBoolean(m.getOrDefault("infiniteResearch", "false")));
            engine.setInstantBuildings(Boolean.parseBoolean(m.getOrDefault("instantBuildings", "false")));
            engine.setAssimilateAll(Boolean.parseBoolean(m.getOrDefault("assimilateAll", "false")));
            engine.setMapEditorEnabled(Boolean.parseBoolean(m.getOrDefault("mapEditorEnabled", "false")));
            engine.setEasyConquering(Boolean.parseBoolean(m.getOrDefault("easyConquering", "false")));
            engine.setInstantIntegration(Boolean.parseBoolean(m.getOrDefault("instantIntegration", "false")));
            engine.setCheatFogOfWarToggle(Boolean.parseBoolean(m.getOrDefault("cheatFogOfWarToggle", "false")));
            engine.setCheatRevealMap(Boolean.parseBoolean(m.getOrDefault("cheatRevealMap", "false")));
            engine.setCheatRandomDisaster(Boolean.parseBoolean(m.getOrDefault("cheatRandomDisaster", "false")));
            engine.setCheatRandomWar(Boolean.parseBoolean(m.getOrDefault("cheatRandomWar", "false")));
            engine.setAnimateWorldGen(Boolean.parseBoolean(m.getOrDefault("animateWorldGen", "true")));
            engine.setDefaultRoleWorker(Engine.sanitizeDefaultRoleId(
                    GameConstants.CLASS_WORKER,
                    Integer.parseInt(m.getOrDefault("defaultRoleWorker", String.valueOf(engine.getDefaultRoleWorker()))),
                    Engine.builtinDefaultRoleForAntClass(GameConstants.CLASS_WORKER).getId()));
            engine.setDefaultRoleSoldier(Engine.sanitizeDefaultRoleId(
                    GameConstants.CLASS_SOLDIER,
                    Integer.parseInt(m.getOrDefault("defaultRoleSoldier", String.valueOf(engine.getDefaultRoleSoldier()))),
                    Engine.builtinDefaultRoleForAntClass(GameConstants.CLASS_SOLDIER).getId()));
            engine.setDefaultRoleMajor(Engine.sanitizeDefaultRoleId(
                    GameConstants.CLASS_MAJOR,
                    Integer.parseInt(m.getOrDefault("defaultRoleMajor", String.valueOf(engine.getDefaultRoleMajor()))),
                    Engine.builtinDefaultRoleForAntClass(GameConstants.CLASS_MAJOR).getId()));
            engine.setDefaultRolePrincess(Engine.sanitizeDefaultRoleId(
                    GameConstants.CLASS_PRINCESS,
                    Integer.parseInt(m.getOrDefault("defaultRolePrincess", String.valueOf(engine.getDefaultRolePrincess()))),
                    Engine.builtinDefaultRoleForAntClass(GameConstants.CLASS_PRINCESS).getId()));
            engine.setDefaultRoleQueen(Engine.sanitizeDefaultRoleId(
                    GameConstants.CLASS_QUEEN,
                    Integer.parseInt(m.getOrDefault("defaultRoleQueen", String.valueOf(engine.getDefaultRoleQueen()))),
                    Engine.builtinDefaultRoleForAntClass(GameConstants.CLASS_QUEEN).getId()));
            engine.setMapLayerBorders(Boolean.parseBoolean(m.getOrDefault("mapLayerBorders",
                    String.valueOf(engine.isMapLayerBorders()))));
            engine.setMapLayerRivers(Boolean.parseBoolean(m.getOrDefault("mapLayerRivers",
                    String.valueOf(engine.isMapLayerRivers()))));
            engine.setMapLayerMeridian(Boolean.parseBoolean(m.getOrDefault("mapLayerMeridian",
                    String.valueOf(engine.isMapLayerMeridian()))));
            engine.setMapLayerEquator(Boolean.parseBoolean(m.getOrDefault("mapLayerEquator",
                    String.valueOf(engine.isMapLayerEquator()))));
            engine.setMapLayerBiomeIcons(Boolean.parseBoolean(m.getOrDefault("mapLayerBiomeIcons",
                    String.valueOf(engine.isMapLayerBiomeIcons()))));
            engine.setMapLayerColonyRanks(Boolean.parseBoolean(m.getOrDefault("mapLayerColonyRanks",
                    String.valueOf(engine.isMapLayerColonyRanks()))));
            engine.setMapLayerTrades(Boolean.parseBoolean(m.getOrDefault("mapLayerTrades",
                    String.valueOf(engine.isMapLayerTrades()))));
            engine.setMapLayerTunnels(Boolean.parseBoolean(m.getOrDefault("mapLayerTunnels",
                    String.valueOf(engine.isMapLayerTunnels()))));
            engine.setMapLayerBattles(Boolean.parseBoolean(m.getOrDefault("mapLayerBattles",
                    String.valueOf(engine.isMapLayerBattles()))));
            engine.setMapLayerGeographicNames(Boolean.parseBoolean(m.getOrDefault("mapLayerGeographicNames",
                    String.valueOf(engine.isMapLayerGeographicNames()))));
            engine.setMapLayerCapitals(Boolean.parseBoolean(m.getOrDefault("mapLayerCapitals",
                    String.valueOf(engine.isMapLayerCapitals()))));
            engine.setMapLayerLabelContinents(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelContinents",
                    String.valueOf(engine.isMapLayerLabelContinents()))));
            engine.setMapLayerLabelIceCaps(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelIceCaps",
                    String.valueOf(engine.isMapLayerLabelIceCaps()))));
            engine.setMapLayerLabelArchipelagos(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelArchipelagos",
                    String.valueOf(engine.isMapLayerLabelArchipelagos()))));
            engine.setMapLayerLabelOceans(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelOceans",
                    String.valueOf(engine.isMapLayerLabelOceans()))));
            engine.setMapLayerLabelTrenches(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelTrenches",
                    String.valueOf(engine.isMapLayerLabelTrenches()))));
            engine.setMapLayerLabelMountains(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelMountains",
                    String.valueOf(engine.isMapLayerLabelMountains()))));
            engine.setMapLayerLabelValleys(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelValleys",
                    String.valueOf(engine.isMapLayerLabelValleys()))));
            engine.setMapLayerLabelLakes(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelLakes",
                    String.valueOf(engine.isMapLayerLabelLakes()))));
            engine.setMapLayerLabelRivers(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelRivers",
                    String.valueOf(engine.isMapLayerLabelRivers()))));
            engine.setMapLayerLabelPeninsulas(Boolean.parseBoolean(m.getOrDefault("mapLayerLabelPeninsulas",
                    String.valueOf(engine.isMapLayerLabelPeninsulas()))));
            engine.setMapLayerDayNight(Boolean.parseBoolean(m.getOrDefault("mapLayerDayNight",
                    String.valueOf(engine.isMapLayerDayNight()))));
            engine.setMapLayerWeather(Boolean.parseBoolean(m.getOrDefault("mapLayerWeather",
                    String.valueOf(engine.isMapLayerWeather()))));
            engine.setMapLayerDisasters(Boolean.parseBoolean(m.getOrDefault("mapLayerDisasters",
                    String.valueOf(engine.isMapLayerDisasters()))));

            loadUpgradePositionsFromFile();
            if (m.containsKey("upgradePositions")) {
                loadUpgradePositionsFromSettings(m.get("upgradePositions"));
            }

            System.out.println("[SaveManager] Global settings loaded.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[SaveManager] Error parsing settings.json, using defaults.");
        }
    }

    private void writeUpgradePositionsFile() {
        try {
            File dest = new File(GamePaths.resolveSavesDirectory(), "upgrade_positions.json");
            try (BufferedWriter pw = Files.newBufferedWriter(dest.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                pw.write("{\n");
                boolean first = true;
                for (Upgrade u : GameUnlocks.getUpgrades()) {
                    if (!first) pw.write(",\n");
                    pw.write("  \"" + escapeJsonString(u.getNameKey()) + "\": { ");
                    pw.write("\"defaultX\": " + u.getDefaultGridX() + ", ");
                    pw.write("\"defaultY\": " + u.getDefaultGridY() + ", ");
                    pw.write("\"setX\": " + u.getGridX() + ", ");
                    pw.write("\"setY\": " + u.getGridY());
                    pw.write(" }");
                    first = false;
                }
                pw.write("\n}\n");
            }
        } catch (Exception ignore) {}
    }

    private void loadUpgradePositionsFromFile() {
        File file = new File(GamePaths.resolveSavesDirectory(), "upgrade_positions.json");
        if (!file.exists()) {
            return;
        }
        boolean loadedAny = false;
        try (BufferedReader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                int nameKeyEnd = line.indexOf("\":");
                if (nameKeyEnd <= 0) continue;
                String nameKey = line.substring(0, nameKeyEnd).trim();
                if (nameKey.startsWith("\"")) nameKey = nameKey.substring(1);

                int setXIdx = line.indexOf("\"setX\":");
                int setYIdx = line.indexOf("\"setY\":");
                if (setXIdx < 0 || setYIdx < 0) continue;

                int commaAfterX = line.indexOf(',', setXIdx);
                int braceAfterY = line.indexOf('}', setYIdx);
                if (commaAfterX < 0 || braceAfterY < 0) continue;

                String strX = line.substring(setXIdx + 7, commaAfterX).trim();
                String strY = line.substring(setYIdx + 7, braceAfterY).trim();

                try {
                    int gx = Integer.parseInt(strX);
                    int gy = Integer.parseInt(strY);
                    Upgrade u = GameUnlocks.getUpgradeByNameKey(nameKey);
                    if (u != null) {
                        u.setGridX(gx);
                        u.setGridY(gy);
                        loadedAny = true;
                    }
                } catch (NumberFormatException ignore) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (loadedAny) {
            ResearchTreeGraph.invalidateLayoutCache();
        }
    }

    private void loadUpgradePositionsFromSettings(String serialized) {
        if (serialized == null || serialized.isBlank() || serialized.equals("{}")) {
            return;
        }
        boolean loadedAny = false;
        String unescaped = unescapeJsonString(serialized);
        if (unescaped.startsWith("{")) unescaped = unescaped.substring(1);
        if (unescaped.endsWith("}")) unescaped = unescaped.substring(0, unescaped.length() - 1);
        String[] pairs = unescaped.split(",");
        for (String pair : pairs) {
            int col = pair.indexOf(':');
            if (col <= 0) continue;
            String key = pair.substring(0, col).trim().replace("\"", "");
            String val = pair.substring(col + 1).trim().replace("\"", "");
            String[] coords = val.split(",");
            if (coords.length == 2) {
                try {
                    int id = Integer.parseInt(key);
                    int gx = Integer.parseInt(coords[0].trim());
                    int gy = Integer.parseInt(coords[1].trim());
                    Upgrade u = GameUnlocks.getUpgradeById(id);
                    if (u != null) {
                        u.setGridX(gx);
                        u.setGridY(gy);
                        loadedAny = true;
                    }
                } catch (NumberFormatException ignore) {}
            }
        }
        if (loadedAny) {
            ResearchTreeGraph.invalidateLayoutCache();
        }
    }

}
