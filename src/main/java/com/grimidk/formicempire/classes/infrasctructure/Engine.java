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
    private final CopyOnWriteArrayList<Runnable> tickListeners = new CopyOnWriteArrayList<>();

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

    public void addTickListener(Runnable r) {
        if (r != null) tickListeners.add(r);
    }

    public void removeTickListener(Runnable r) {
        if (r != null) tickListeners.remove(r);
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

        if (savefile != null) {
            this.setLanguage(savefile.getLanguage());
            this.setAllowTurboMode(savefile.isAllowTurboMode());
            this.setScreenSize(savefile.getScreenSize());
            this.setFullScreen(savefile.isFullScreen());
            this.setAutosaveFrequency(savefile.getAutosaveFrequency());
        }

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
                Thread.sleep((long) delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!paused) {
                try {
                    semaphore.acquire();
                    if (this.world != null) this.world.runMinute();
                    
                    for (Runnable r : tickListeners) {
                        try {
                            r.run();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
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