package com.grimidk.formicempire.classes.infrasctructure;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class Engine extends Thread {
    private World world;
    private float delay;
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
    private boolean fullScreen = false;
    private int autosaveFrequency = 1; // 1 = every month
    
    private boolean visualFiltersEnabled = true;
    private boolean arachnophobiaMode = false;
    
    private int masterVolume = 80;
    private int musicVolume = 70;
    private int sfxVolume = 100;
    
    private boolean pauseOnFocusLoss = true;
    private boolean confirmOnQuit = true;
    private boolean showTooltips = true;

    public Engine() {
        this.delay = 250;
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
        return delay;
    }

    public void setDelay(float delay) {
        this.delay = delay;
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
            if (!allowTurboMode) {
                try {
                    Thread.sleep((long) delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Thread.yield();
            }

            if (!paused) {
                try {
                    semaphore.acquire();
                    if (this.world != null) {
                        this.world.runMinute(); 
                    }
                    notifyMinuteListeners(); 
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

    public boolean isVisualFiltersEnabled() {
        return visualFiltersEnabled;
    }

    public void setVisualFiltersEnabled(boolean visualFiltersEnabled) {
        this.visualFiltersEnabled = visualFiltersEnabled;
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

    public boolean isShowTooltips() {
        return showTooltips;
    }

    public void setShowTooltips(boolean showTooltips) {
        this.showTooltips = showTooltips;
    }
}
