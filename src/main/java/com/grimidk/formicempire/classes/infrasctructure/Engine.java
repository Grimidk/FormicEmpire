package com.grimidk.formicempire.classes.infrasctructure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.shared.SandboxCheatService;
import com.grimidk.formicempire.classes.infrasctructure.audio.MusicService;
import com.grimidk.formicempire.classes.infrasctructure.audio.SfxService;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.diagnostics.SimulationDiagnostics;
import com.grimidk.formicempire.classes.infrasctructure.diagnostics.SimulationDiagnostics.Scope;

public class Engine extends Thread {
    private World world;
    private GameSpeed speed = GameConstants.SPEED_NORMAL;
    private final Semaphore semaphore;
    private boolean killSwitch;
    private volatile boolean paused;
    
    // --- Managers ---
    private final TradeManager tradeManager;
    private final MusicService musicService;
    private final SfxService sfxService;

    // --- Listeners ---
    private final CopyOnWriteArrayList<Runnable> minuteTickListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> hourTickListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> dayTickListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> monthTickListeners = new CopyOnWriteArrayList<>();
    
    private final SaveManager settingsSaveManager;

    // --- Settings ---
    private String language = "en";
    private boolean allowTurboMode = false;
    private String screenSize = "1000x700";
    private boolean fullScreen = true;
    private int autosaveFrequency = 1; // 1 = every month
    
    private boolean daylightColorOverlayEnabled = true;
    private boolean weatherColorOverlayEnabled = true;
    private boolean arachnophobiaMode = false;
    
    private int masterVolume = GameNumbers.VOLUME_DEFAULT_PERCENT;
    private int musicVolume = GameNumbers.VOLUME_DEFAULT_PERCENT;
    private int sfxVolume = GameNumbers.VOLUME_DEFAULT_PERCENT;
    private boolean musicMuted = false;
    private boolean musicShuffle = true;
    
    private boolean pauseOnFocusLoss = true;
    private boolean confirmOnQuit = true;
    private boolean escapeKeyGameActions = true;
    private boolean showTooltips = true;
    private boolean disablePopups = false;
    
    private boolean fuzzParasiteAnts = true;
    private boolean showAuditMenu = false;
    private boolean showFpsCounter = false;
    private boolean overworldAutoRecenter = true;
    private boolean darkMode = false;
    private int frameRateCap = 60;
    private boolean freeAbilities = false;
    private boolean infiniteResearch = false;
    private boolean instantBuildings = false;
    private boolean assimilateAll = false;
    private boolean easyConquering = false;
    private boolean instantIntegration = false;
    private int defaultRoleWorker = GameConstants.ROLE_FORAGER.getId();
    private int defaultRoleSoldier = GameConstants.ROLE_HUNTER.getId();
    private int defaultRoleMajor = GameConstants.ROLE_CRANE.getId();
    private int defaultRolePrincess = GameConstants.ROLE_BREEDER.getId();
    private int defaultRoleQueen = GameConstants.ROLE_LAYER.getId();
    private boolean mapLayerBorders = true;
    private boolean mapLayerBiomeIcons = true;
    private boolean mapLayerColonyRanks = true;
    private boolean mapLayerTrades = true;
    private boolean mapLayerTunnels = true;
    private boolean mapLayerBattles = true;

    private long lastSpeedDownStepMs;
    private long lastSpeedUpStepMs;
    private static final long SPEED_STEP_COALESCE_MS = 40;

    public Engine() {
        this.semaphore = new Semaphore(1);
        this.killSwitch = false;
        this.paused = true;
        this.tradeManager = new TradeManager();
        this.addHourTickListener(this.tradeManager);
        this.settingsSaveManager = new SaveManager();
        this.loadGlobalSettings();
        this.musicService = new MusicService(this);
        this.sfxService = new SfxService(this);
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    public MusicService getMusicService() {
        return musicService;
    }

    public SfxService getSfxService() {
        return sfxService;
    }

    public SaveManager getSaveManager() {
        return settingsSaveManager;
    }

    public void loadGlobalSettings() {
        this.settingsSaveManager.loadSettings(this);
    }

    public void saveGlobalSettings() {
        this.settingsSaveManager.saveSettings(this);
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
        if (this.world != null) {
            this.world.setEngine(this);
        }
    }

    public float getDelay() {
        return speed.getDelayMs();
    }

    public void setDelay(float delay) {
        this.speed = GameSpeed.closestToDelayMs((int) delay);
    }

    public GameSpeed getSpeed() {
        return speed;
    }

    public String getSpeedLabel() {
        if (paused) {
            return LanguageStrings.get(LanguageStrings.UI_PAUSED_TICK);
        }
        return speed.getLabel();
    }

    public void setSpeed(GameSpeed speed) {
        if (speed == null) {
            return;
        }
        GameSpeed resolved = speed;
        if (speed.getId() > GameSpeed.maxPlayableId(allowTurboMode)) {
            resolved = GameSpeed.fromId(GameSpeed.maxPlayableId(allowTurboMode));
        }
        if (this.speed != resolved) {
            this.speed = resolved;
            interrupt();
        }
    }

    public void stepSpeedUp() {
        adjustSpeedStep(1);
    }

    public void stepSpeedDown() {
        adjustSpeedStep(-1);
    }

    public void adjustSpeedStep(int direction) {
        if (direction == 0) {
            return;
        }
        long now = System.currentTimeMillis();
        if (direction < 0) {
            if (now - lastSpeedDownStepMs < SPEED_STEP_COALESCE_MS) {
                return;
            }
            lastSpeedDownStepMs = now;
        } else {
            if (now - lastSpeedUpStepMs < SPEED_STEP_COALESCE_MS) {
                return;
            }
            lastSpeedUpStepMs = now;
        }
        if (direction < 0) {
            if (paused) {
                return;
            }
            if (speed.getId() == GameSpeed.ID_VERY_SLOW) {
                pauseEngine();
                return;
            }
            setSpeed(GameSpeed.step(speed, -1, allowTurboMode));
            return;
        }
        if (paused) {
            resumeEngine();
            return;
        }
        setSpeed(GameSpeed.step(speed, 1, allowTurboMode));
    }

    public void togglePause() {
        if (paused) {
            resumeEngine();
        } else {
            pauseEngine();
        }
    }

    public Semaphore getSema() {
        return semaphore;
    }

    public boolean isKillSwitch() {
        return killSwitch;
    }

    public void setKillSwitch(boolean killSwitch) {
        this.killSwitch = killSwitch;
    }

    public void pauseEngine() {
        this.paused = true;
    }

    public void resumeEngine() {
        this.paused = false;
    }

    public boolean isPaused() {
        return this.paused;
    }

    // --- Minute Listeners ---
    public void addTickListener(Runnable r) {
        if (r != null) minuteTickListeners.add(r);
    }
    public void removeTickListener(Runnable r) {
        if (r != null) minuteTickListeners.remove(r);
    }
    public void notifyMinuteListeners() {
        try (SimulationDiagnostics.TimedSection section = SimulationDiagnostics.start(Scope.ENGINE_MINUTE_NOTIFY)) {
            for (Runnable r : minuteTickListeners) {
                try {
                    r.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public int getMinuteTickListenerCount() {
        return minuteTickListeners.size();
    }

    public int getHourTickListenerCount() {
        return hourTickListeners.size();
    }

    public int getDayTickListenerCount() {
        return dayTickListeners.size();
    }

    public int getMonthTickListenerCount() {
        return monthTickListeners.size();
    }

    // --- Hour Listeners ---
    public void addHourTickListener(Runnable r) {
        if (r != null) hourTickListeners.add(r);
    }
    public void removeHourTickListener(Runnable r) {
        if (r != null) hourTickListeners.remove(r);
    }
    public void notifyHourListeners() {
        for (Runnable r : hourTickListeners) {
            try {
                r.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    // --- Day Listeners ---
    public void addDayTickListener(Runnable r) {
        if (r != null) dayTickListeners.add(r);
    }
    public void removeDayTickListener(Runnable r) {
        if (r != null) dayTickListeners.remove(r);
    }
    public void notifyDayListeners() {
        for (Runnable r : dayTickListeners) {
            try {
                r.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // --- Month Listeners ---
    public void addMonthTickListener(Runnable r) {
        if (r != null) monthTickListeners.add(r);
    }
    public void removeMonthTickListener(Runnable r) {
        if (r != null) monthTickListeners.remove(r);
    }
    public void notifyMonthListeners() {
        for (Runnable r : monthTickListeners) {
            try {
                r.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void loadFile(Savefile savefile) {
        if (this.world == null) {
            System.err.println("[Engine] World object is null in Engine.loadFile");
            return;
        }

        if (savefile != null) {
            System.out.println("[Engine] Loading existing world state from Savefile ID: " + savefile.getId());
            this.world.loadWorld(savefile);
        } else {
            System.out.println("[Engine] Generating new world...");
            String baseName = "Player";
            Colony colony = new Colony(1, baseName + " Prime", true);
            this.world.startWorld(GameConstants.BIOME_PLAINS, colony, baseName);
        }
    }

    public void startUp(Savefile savefile) {
        System.out.println("[Engine] Starting up Engine...");
        tradeManager.clearActiveTrades();
        World world = new World();
        this.setWorld(world);

        this.loadFile(savefile);
        SandboxCheatService.applyEnabledCheats(this);
        
        try {
            if (savefile != null && this.world != null) {
                this.world.setSaveSlotId(savefile.getId());
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public void run() {
        while (!killSwitch) {
            try {
                Thread.sleep(speed.getDelayMs());
            } catch (InterruptedException e) {
                Thread.interrupted();
                continue;
            }

            if (!paused) {
                semaphore.acquireUninterruptibly();
                try {
                    if (this.world != null) {
                        this.world.runMinute();
                        notifyMinuteListeners();
                    }
                } finally {
                    semaphore.release();
                }
            }
        }
    }
    // --- Settings Getters and Setters ---

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = (language != null) ? language : "en";
        if (LanguageStrings.setLanguage(this.language) && this.world != null) {
            this.world.relocalizeDynastyNames();
        }
    }

    public boolean isAllowTurboMode() {
        return allowTurboMode;
    }

    public void setAllowTurboMode(boolean allowTurboMode) {
        this.allowTurboMode = allowTurboMode;
        if (!allowTurboMode && speed.getId() > GameSpeed.maxPlayableId(false)) {
            speed = GameSpeed.fromId(GameSpeed.maxPlayableId(false));
        }
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = (screenSize != null) ? screenSize : "1000x700";
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public int getAutosaveFrequency() {
        return autosaveFrequency;
    }

    public void setAutosaveFrequency(int autosaveFrequency) {
        this.autosaveFrequency = (autosaveFrequency >= 0) ? autosaveFrequency : 1;
    }

    public boolean isDaylightColorOverlayEnabled() {
        return daylightColorOverlayEnabled;
    }

    public void setDaylightColorOverlayEnabled(boolean daylightColorOverlayEnabled) {
        this.daylightColorOverlayEnabled = daylightColorOverlayEnabled;
    }

    public boolean isWeatherColorOverlayEnabled() {
        return weatherColorOverlayEnabled;
    }

    public void setWeatherColorOverlayEnabled(boolean weatherColorOverlayEnabled) {
        this.weatherColorOverlayEnabled = weatherColorOverlayEnabled;
    }

    public boolean isArachnophobiaMode() {
        return arachnophobiaMode;
    }

    public void setArachnophobiaMode(boolean arachnophobiaMode) {
        this.arachnophobiaMode = arachnophobiaMode;
    }

    public int getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(int masterVolume) {
        this.masterVolume = GameNumbers.snapVolumePercent(masterVolume);
        if (musicService != null) {
            musicService.refreshVolume();
        }
        if (sfxService != null) {
            sfxService.refreshVolume();
        }
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int musicVolume) {
        this.musicVolume = GameNumbers.snapVolumePercent(musicVolume);
        if (musicService != null) {
            musicService.refreshVolume();
        }
    }

    public int getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(int sfxVolume) {
        this.sfxVolume = GameNumbers.snapVolumePercent(sfxVolume);
        if (sfxService != null) {
            sfxService.refreshVolume();
        }
    }

    public boolean isMusicMuted() {
        return musicMuted;
    }

    public void setMusicMuted(boolean musicMuted) {
        this.musicMuted = musicMuted;
        if (musicService != null) {
            musicService.refreshVolume();
        }
    }

    public boolean isMusicShuffle() {
        return musicShuffle;
    }

    public void setMusicShuffle(boolean musicShuffle) {
        this.musicShuffle = musicShuffle;
    }

    public boolean isPauseOnFocusLoss() {
        return pauseOnFocusLoss;
    }

    public void setPauseOnFocusLoss(boolean pauseOnFocusLoss) {
        this.pauseOnFocusLoss = pauseOnFocusLoss;
    }

    public boolean isConfirmOnQuit() {
        return confirmOnQuit;
    }

    public void setConfirmOnQuit(boolean confirmOnQuit) {
        this.confirmOnQuit = confirmOnQuit;
    }

    public boolean isEscapeKeyGameActions() {
        return escapeKeyGameActions;
    }

    public void setEscapeKeyGameActions(boolean escapeKeyGameActions) {
        this.escapeKeyGameActions = escapeKeyGameActions;
    }

    public boolean isShowTooltips() {
        return showTooltips;
    }

    public void setShowTooltips(boolean showTooltips) {
        this.showTooltips = showTooltips;
    }

    public boolean isDisablePopups() {
        return disablePopups;
    }

    public void setDisablePopups(boolean disablePopups) {
        this.disablePopups = disablePopups;
    }

    public boolean isFuzzParasiteAnts() {
        return fuzzParasiteAnts;
    }

    public void setFuzzParasiteAnts(boolean fuzzParasiteAnts) {
        this.fuzzParasiteAnts = fuzzParasiteAnts;
    }

    public boolean isShowAuditMenu() {
        return showAuditMenu;
    }

    public void setShowAuditMenu(boolean showAuditMenu) {
        this.showAuditMenu = showAuditMenu;
    }

    public boolean isShowFpsCounter() {
        return showFpsCounter;
    }

    public void setShowFpsCounter(boolean showFpsCounter) {
        this.showFpsCounter = showFpsCounter;
    }

    public boolean isFreeAbilities() {
        return freeAbilities;
    }

    public void setFreeAbilities(boolean freeAbilities) {
        this.freeAbilities = freeAbilities;
    }

    public boolean isInfiniteResearch() {
        return infiniteResearch;
    }

    public void setInfiniteResearch(boolean infiniteResearch) {
        this.infiniteResearch = infiniteResearch;
    }

    public boolean isInstantBuildings() {
        return instantBuildings;
    }

    public void setInstantBuildings(boolean instantBuildings) {
        this.instantBuildings = instantBuildings;
    }

    public boolean isAssimilateAll() {
        return assimilateAll;
    }

    public void setAssimilateAll(boolean assimilateAll) {
        this.assimilateAll = assimilateAll;
    }

    public boolean isEasyConquering() {
        return easyConquering;
    }

    public void setEasyConquering(boolean easyConquering) {
        this.easyConquering = easyConquering;
    }

    public boolean isInstantIntegration() {
        return instantIntegration;
    }

    public void setInstantIntegration(boolean instantIntegration) {
        this.instantIntegration = instantIntegration;
    }

    public boolean hasAchievementTaintingSandbox() {
        return freeAbilities
                || infiniteResearch
                || instantBuildings
                || assimilateAll
                || easyConquering
                || instantIntegration;
    }

    public void applySandboxTaintToActiveWorld() {
        if (world != null && hasAchievementTaintingSandbox()) {
            world.disableAchievements();
        }
    }

    public boolean isOverworldAutoRecenter() {
        return overworldAutoRecenter;
    }

    public void setOverworldAutoRecenter(boolean overworldAutoRecenter) {
        this.overworldAutoRecenter = overworldAutoRecenter;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public int getFrameRateCap() {
        return frameRateCap;
    }

    public void setFrameRateCap(int frameRateCap) {
        this.frameRateCap = sanitizeFrameRateCap(frameRateCap);
    }

    public static int sanitizeFrameRateCap(int hz) {
        if (hz <= 0) {
            return 0;
        }
        if (hz <= 30) {
            return 30;
        }
        if (hz <= 60) {
            return 60;
        }
        return 120;
    }

    public int getVisualFrameIntervalMs() {
        int hz = frameRateCap;
        if (hz <= 0) {
            return 1;
        }
        return Math.max(1, 1000 / hz);
    }

    public int getDefaultRoleWorker() {
        return defaultRoleWorker;
    }

    public void setDefaultRoleWorker(int defaultRoleWorker) {
        this.defaultRoleWorker = defaultRoleWorker;
    }

    public int getDefaultRoleSoldier() {
        return defaultRoleSoldier;
    }

    public void setDefaultRoleSoldier(int defaultRoleSoldier) {
        this.defaultRoleSoldier = defaultRoleSoldier;
    }

    public int getDefaultRoleMajor() {
        return defaultRoleMajor;
    }

    public void setDefaultRoleMajor(int defaultRoleMajor) {
        this.defaultRoleMajor = defaultRoleMajor;
    }

    public int getDefaultRolePrincess() {
        return defaultRolePrincess;
    }

    public void setDefaultRolePrincess(int defaultRolePrincess) {
        this.defaultRolePrincess = defaultRolePrincess;
    }

    public int getDefaultRoleQueen() {
        return defaultRoleQueen;
    }

    public void setDefaultRoleQueen(int defaultRoleQueen) {
        this.defaultRoleQueen = defaultRoleQueen;
    }

    public boolean isMapLayerBorders() {
        return mapLayerBorders;
    }

    public void setMapLayerBorders(boolean mapLayerBorders) {
        this.mapLayerBorders = mapLayerBorders;
    }

    public boolean isMapLayerBiomeIcons() {
        return mapLayerBiomeIcons;
    }

    public void setMapLayerBiomeIcons(boolean mapLayerBiomeIcons) {
        this.mapLayerBiomeIcons = mapLayerBiomeIcons;
    }

    public boolean isMapLayerColonyRanks() {
        return mapLayerColonyRanks;
    }

    public void setMapLayerColonyRanks(boolean mapLayerColonyRanks) {
        this.mapLayerColonyRanks = mapLayerColonyRanks;
    }

    public boolean isMapLayerTrades() {
        return mapLayerTrades;
    }

    public void setMapLayerTrades(boolean mapLayerTrades) {
        this.mapLayerTrades = mapLayerTrades;
    }

    public boolean isMapLayerTunnels() {
        return mapLayerTunnels;
    }

    public void setMapLayerTunnels(boolean mapLayerTunnels) {
        this.mapLayerTunnels = mapLayerTunnels;
    }

    public boolean isMapLayerBattles() {
        return mapLayerBattles;
    }

    public void setMapLayerBattles(boolean mapLayerBattles) {
        this.mapLayerBattles = mapLayerBattles;
    }

    public static List<AntRole> antRolesForAntType(AntType type) {
        List<AntRole> out = new ArrayList<>();
        for (AntRole r : GameConstants.getAntRoles()) {
            if (r.getAntType() == type) {
                out.add(r);
            }
        }
        return out;
    }

    public static AntRole resolveDefaultRoleForAntType(AntType type, Engine engine) {
        if (engine != null) {
            int roleId;
            if (type == GameConstants.TYPE_WORKER) {
                roleId = engine.getDefaultRoleWorker();
            } else if (type == GameConstants.TYPE_SOLDIER) {
                roleId = engine.getDefaultRoleSoldier();
            } else if (type == GameConstants.TYPE_MAJOR) {
                roleId = engine.getDefaultRoleMajor();
            } else if (type == GameConstants.TYPE_PRINCESS) {
                roleId = engine.getDefaultRolePrincess();
            } else if (type == GameConstants.TYPE_QUEEN) {
                roleId = engine.getDefaultRoleQueen();
            } else {
                return builtinDefaultRoleForAntType(type);
            }
            AntRole chosen = GameConstants.getAntRoleById(roleId);
            if (chosen != null && chosen.getAntType() == type
                    && GameConstants.isEligibleDefaultHatchRole(chosen)) {
                return chosen;
            }
        }
        return builtinDefaultRoleForAntType(type);
    }

    public static AntRole builtinDefaultRoleForAntType(AntType type) {
        if (type == GameConstants.TYPE_WORKER) {
            return GameConstants.ROLE_FORAGER;
        }
        if (type == GameConstants.TYPE_SOLDIER) {
            return GameConstants.ROLE_HUNTER;
        }
        if (type == GameConstants.TYPE_MAJOR) {
            return GameConstants.ROLE_CRANE;
        }
        if (type == GameConstants.TYPE_PRINCESS) {
            return GameConstants.ROLE_BREEDER;
        }
        if (type == GameConstants.TYPE_DRONE) {
            return GameConstants.ROLE_DRONE;
        }
        if (type == GameConstants.TYPE_QUEEN) {
            return GameConstants.ROLE_LAYER;
        }
        return null;
    }

    public static int sanitizeDefaultRoleId(AntType type, int desiredRoleId, int fallbackRoleId) {
        AntRole r = GameConstants.getAntRoleById(desiredRoleId);
        if (r != null && r.getAntType() == type && GameConstants.isEligibleDefaultHatchRole(r)) {
            return desiredRoleId;
        }
        AntRole fallback = GameConstants.getAntRoleById(fallbackRoleId);
        if (fallback != null && fallback.getAntType() == type
                && GameConstants.isEligibleDefaultHatchRole(fallback)) {
            return fallbackRoleId;
        }
        AntRole builtin = builtinDefaultRoleForAntType(type);
        return builtin != null ? builtin.getId() : fallbackRoleId;
    }

    public static int defaultRoleIdForAntType(AntType type, Engine engine) {
        AntRole r = resolveDefaultRoleForAntType(type, engine);
        return r != null ? r.getId() : -1;
    }
}
