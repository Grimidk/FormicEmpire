package com.grimidk.formicempire.classes.infrasctructure;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.World;

public class Engine extends Thread {
    private World world;
    private float delay;
    private final Semaphore semaphore;
    private boolean killSwitch;
    private volatile boolean paused;
    
    // --- Listeners ---
    private final CopyOnWriteArrayList<Runnable> minuteTickListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> hourTickListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> dayTickListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> monthTickListeners = new CopyOnWriteArrayList<>();
    
    private final SaveManager settingsSaveManager;

    // Settings
    private String language = "en";
    private boolean allowTurboMode = false;
    private String screenSize = "1000x700";
    private boolean fullScreen = false;
    private int autosaveFrequency = 1; // 1 = every month

    public Engine() {
        this.delay = 250;
        this.semaphore = new Semaphore(1);
        this.killSwitch = false;
        this.paused = true;
        this.settingsSaveManager = new SaveManager();
        this.loadGlobalSettings();
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
        Colony colony;
        if (savefile != null) {
            colony = new Colony(savefile);
        } else {
            colony = new Colony(1, "Grim Colony", true);
        }
        try {
            if (savefile != null && this.world != null) {
                this.world.setMinute(savefile.getMinute());
                this.world.setHour(savefile.getHour());
                this.world.setDay(savefile.getDay());
                this.world.setMonth(savefile.getMonth());
                this.world.setYear(savefile.getYear());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Generating new world...");
        this.world.startWorld(GameConstants.getBiomes().get(0), colony);
    }

    public void startUp(Savefile savefile) {
        System.out.println("Loading new world...");
        World world = new World();
        this.setWorld(world);

        this.loadFile(savefile);
        try {
            if (savefile != null && this.world != null) {
                this.world.setSaveSlotId(savefile.getId());
            }
        } catch (Exception ignore) {
        }

        if (this.world.getSpawnHex() != null && this.world.getSpawnHex().getColony() != null) {
            Colony colony = this.world.getSpawnHex().getColony();
            
            TriggerManager triggerManager = new TriggerManager(this.world, colony, this);
            
            triggerManager.registerListeners();
        }
    }

    @Override
    public void run() {
        while (!killSwitch) {
            try {
                Thread.sleep((long) delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    // Settings Getters and Setters

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = (language != null) ? language : "en";
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
        this.autosaveFrequency = (autosaveFrequency > 0) ? autosaveFrequency : 1;
    }
}