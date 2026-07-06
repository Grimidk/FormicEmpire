package com.grimidk.formicempire.classes.infrasctructure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class Engine extends Thread {
    private World world;
    private GameSpeed speed = GameConstants.SPEED_NORMAL;
    private final Semaphore semaphore;
    private boolean killSwitch;
    private volatile boolean paused;
    
    // --- Managers ---
    private final TradeManager tradeManager;

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
    
    private int masterVolume = 50;
    private int musicVolume = 50;
    private int sfxVolume = 50;
    
    private boolean pauseOnFocusLoss = true;
    private boolean confirmOnQuit = true;
    private boolean escapeKeyGameActions = true;
    private boolean showTooltips = true;
    
    private boolean fuzzParasiteAnts = true;
    private boolean overworldAutoRecenter = true;
    private boolean darkMode = false;
    private int defaultRoleWorker = 1; // ROLE_FORAGER
    private int defaultRoleSoldier = 16; // ROLE_HUNTER
    private int defaultRoleMajor = 17; // ROLE_BRUTE
    private int defaultRolePrincess = 23; // ROLE_BREEDER
    private int defaultRoleQueen = 25; // ROLE_LAYER

    /** Ignores duplicate speed input within one Swing key sequence (VK + typed char). */
    private long lastSpeedDownStepMs;
    private long lastSpeedUpStepMs;
    private static final long SPEED_STEP_COALESCE_MS = 120;

    public Engine() {
        this.semaphore = new Semaphore(1);
        this.killSwitch = false;
        this.paused = true;
        this.tradeManager = new TradeManager();
        this.addHourTickListener(this.tradeManager);
        this.settingsSaveManager = new SaveManager();
        this.loadGlobalSettings();
    }

    public TradeManager getTradeManager() {
        return tradeManager;
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

    /** Unpauses if needed, then steps speed up or down (one tier per call). */
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
        for (Runnable r : minuteTickListeners) {
            try {
                r.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
        LanguageStrings.setLanguage(this.language);
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
        this.masterVolume = masterVolume;
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int musicVolume) {
        this.musicVolume = musicVolume;
    }

    public int getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(int sfxVolume) {
        this.sfxVolume = sfxVolume;
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

    public boolean isFuzzParasiteAnts() {
        return fuzzParasiteAnts;
    }

    public void setFuzzParasiteAnts(boolean fuzzParasiteAnts) {
        this.fuzzParasiteAnts = fuzzParasiteAnts;
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
                return legacyDefaultRoleForAntType(type);
            }
            AntRole chosen = GameConstants.getAntRoleById(roleId);
            if (chosen != null && chosen.getAntType() == type) {
                return chosen;
            }
        }
        return legacyDefaultRoleForAntType(type);
    }

    private static AntRole legacyDefaultRoleForAntType(AntType type) {
        if (type == GameConstants.TYPE_WORKER) {
            return GameConstants.ROLE_FORAGER;
        }
        if (type == GameConstants.TYPE_SOLDIER) {
            return GameConstants.ROLE_HUNTER;
        }
        if (type == GameConstants.TYPE_MAJOR) {
            return GameConstants.ROLE_BRUTE;
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
        if (r != null && r.getAntType() == type) {
            return desiredRoleId;
        }
        return fallbackRoleId;
    }

    public static int defaultRoleIdForAntType(AntType type, Engine engine) {
        AntRole r = resolveDefaultRoleForAntType(type, engine);
        return r != null ? r.getId() : -1;
    }
}
