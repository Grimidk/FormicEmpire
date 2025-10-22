/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grimidk.formicempire.classes;

import java.util.concurrent.Semaphore;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;

/**
 *
 * @author juanmendezl
 */
public class Engine extends Thread{
    
    private World world;
    private ArrayList<AntStatus> antstatuses;
    private ArrayList<AntSubType> antSubTypes;
    private ArrayList<AntType> antTypes;
    private ArrayList<Biome> biomes;
    private ArrayList<Resource> resources;
    private ArrayList<Species> species;
    private ArrayList<TimeOfDay> timesOfDay;
    private ArrayList<Upgrade> upgrades;
    private ArrayList<Weather> weathers;
    
    private float delay;
    private final Semaphore semaphore;
    private boolean killSwitch;
    private volatile boolean paused;
    private final CopyOnWriteArrayList<Runnable> tickListeners = new CopyOnWriteArrayList<>();

    public Engine() {
        this.delay = 1000;
        this.semaphore = new Semaphore(1);
        this.killSwitch = false;
        this.paused = false;
        this.antstatuses = new ArrayList<>();
        this.antSubTypes = new ArrayList<>();
        this.antTypes = new ArrayList<>();
        this.biomes = new ArrayList<>();
        this.resources = new ArrayList<>();
        this.species = new ArrayList<>();
        this.timesOfDay = new ArrayList<>();
        this.upgrades = new ArrayList<>();
        this.weathers = new ArrayList<>();
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public float getDelay() {
        return delay;
    }

    public void setDelay(float delay) {
        this.delay = delay;
    }

    public ArrayList<AntStatus> getAntstatuses() {
        return antstatuses;
    }

    public void setAntstatuses(ArrayList<AntStatus> antstatuses) {
        this.antstatuses = antstatuses;
    }

    public ArrayList<AntSubType> getAntSubTypes() {
        return antSubTypes;
    }

    public void setAntSubTypes(ArrayList<AntSubType> antSubTypes) {
        this.antSubTypes = antSubTypes;
    }

    public ArrayList<AntType> getAntTypes() {
        return antTypes;
    }

    public void setAntTypes(ArrayList<AntType> antTypes) {
        this.antTypes = antTypes;
    }

    public ArrayList<Biome> getBiomes() {
        return biomes;
    }

    public void setBiomes(ArrayList<Biome> biomes) {
        this.biomes = biomes;
    }

    public ArrayList<Resource> getResources() {
        return resources;
    }

    public void setResources(ArrayList<Resource> resources) {
        this.resources = resources;
    }

    public ArrayList<Species> getSpecies() {
        return species;
    }

    public void setSpecies(ArrayList<Species> species) {
        this.species = species;
    }

    public ArrayList<TimeOfDay> getTimesOfDay() {
        return timesOfDay;
    }

    public void setTimesOfDay(ArrayList<TimeOfDay> timesOfDay) {
        this.timesOfDay = timesOfDay;
    }

    public ArrayList<Upgrade> getUpgrades() {
        return upgrades;
    }

    public void setUpgrades(ArrayList<Upgrade> upgrades) {
        this.upgrades = upgrades;
    }

    public ArrayList<Weather> getWeathers() {
        return weathers;
    }

    public void setWeathers(ArrayList<Weather> weathers) {
        this.weathers = weathers;
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
    
        // Biomes
        public static final Biome PLAINS_BIOME = new Biome(1, "Plains", 25, 2);
        public static final Biome FOREST_BIOME = new Biome(2, "Forest", 20, 3);
        public static final Biome JUNGLE_BIOME = new Biome(3, "Jungle", 30, 4);
        public static final Biome SWAMP_BIOME = new Biome(4, "Swamp", 25, 5);
        public static final Biome URBAN_BIOME = new Biome(5, "Urban", 30, 1);
        public static final Biome TUNDRA_BIOME = new Biome(6, "Tundra", 5, 2);
        public static final Biome TAIGA_BIOME = new Biome(7, "Taiga", 10, 1);
        public static final Biome DESSERT_BIOME = new Biome(8, "Dessert", 50, 0);

        // Resources
        public static final Resource PLANT_RESOURCE = new Resource(1, "Plant Matter", true, false);
        public static final Resource FUNGI_RESOURCE = new Resource(2, "Fungi Matter", true, false);
        public static final Resource MEAT_RESOURCE = new Resource(3, "Animal Matter", true, false);
        public static final Resource WATER_RESOURCE = new Resource(4, "Water", true, true);
        public static final Resource SYRUP_RESOURCE = new Resource(5, "Syrup", true, true);
        public static final Resource RESIN_RESOURCE = new Resource(6, "Resin", false, true);
        public static final Resource ROCK_RESOURCE = new Resource(7, "Mineral", false, false);

        // Times of Day
        public static final TimeOfDay DAY_TIME = new TimeOfDay(1, "Daytime", 1);
        public static final TimeOfDay DUSK_TIME = new TimeOfDay(2, "Dusk", 5/8);
        public static final TimeOfDay NIGHT_TIME = new TimeOfDay(3, "Nightime", 3/4);
        public static final TimeOfDay DAWN_TIME = new TimeOfDay(4, "Dawn", 5/8);

        // Weather
        public static final Weather CLEAR_WEATHER = new Weather(1, "Clear", 1, 0);
        public static final Weather RAIN_WEATHER = new Weather(2, "Rain", 1, 1);
        public static final Weather SNOW_WEATHER = new Weather(3, "Snow", 1, 1);
        public static final Weather HEAVY_RAIN_WEATHER = new Weather(4, "Heavy Rain", 1, 3);
        public static final Weather THUNDER_WEATHER = new Weather(5, "Thunder Storm", 1, 2);
        public static final Weather HEAVY_SNOW_WEATHER = new Weather(6, "Snow Storm", 1, 2);
        public static final Weather WIND_WEATHER = new Weather(7, "Heavy Wind", 1, -1);
        public static final Weather HEAT_WEATHER = new Weather(8, "Heat Wave", 2, -2);
        public static final Weather FROG_WEATHER = new Weather(9, "Frog Rain", 1, 0);

        // Ant Status
        public static final AntStatus STATUS_ALIVE = new AntStatus(1, "Alive");
        public static final AntStatus STATUS_DEAD = new AntStatus(2, "Dead");
        public static final AntStatus STATUS_ZOMBIFIED = new AntStatus(3, "Zombified");

        // Ant Types
        public static final AntType TYPE_EGG = new AntType(1, "Egg", 1, 1, 0, 1, 0, 1, 0, 0, 0, 1/4);
        public static final AntType TYPE_LARVA = new AntType(2, "Larva", 1, 1, 1/2, 1, 1/2, 1, 1, 1/2, 1/2, 1/2);
        public static final AntType TYPE_PUPA = new AntType(3, "Pupa", 1, 1, 0, 1, 1, 1, 0, 1/2, 0, 1);
        public static final AntType TYPE_WORKER = new AntType(4, "Worker", 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        public static final AntType TYPE_SOLDIER = new AntType(5, "Soldier", 3, 2, 3, 1, 3, 2, 3, 5, 3, 2);
        public static final AntType TYPE_MAJOR = new AntType(6, "Major", 10, 10, 15, 5, 20, 5, 2, 50, 2, 5);
        public static final AntType TYPE_DRONE = new AntType(7, "Drone", 1, 1, 1, 1, 1, 1, 1, 1, 1, 2);
        public static final AntType TYPE_PRINCESS = new AntType(8, "Princess", 1, 1, 1, 1, 1, 1, 1, 1, 1, 2);
        public static final AntType TYPE_QUEEN = new AntType(9, "Queen", 50, 50, 2, 1000, 50, 10, 1/2, 50, 1/4, 5);

        public void loadConstants(){
            System.out.println("Loading biomes...");
            this.biomes.add(PLAINS_BIOME);
            this.biomes.add(FOREST_BIOME);
            this.biomes.add(JUNGLE_BIOME);
            this.biomes.add(SWAMP_BIOME);
            this.biomes.add(URBAN_BIOME);
            this.biomes.add(TUNDRA_BIOME);
            this.biomes.add(TAIGA_BIOME);
            this.biomes.add(DESSERT_BIOME);

            System.out.println("Loading resources...");
            this.resources.add(PLANT_RESOURCE);
            this.resources.add(FUNGI_RESOURCE);
            this.resources.add(MEAT_RESOURCE);
            this.resources.add(WATER_RESOURCE);
            this.resources.add(SYRUP_RESOURCE);
            this.resources.add(RESIN_RESOURCE);
            this.resources.add(ROCK_RESOURCE);

            System.out.println("Loading times...");
            this.timesOfDay.add(DAY_TIME);
            this.timesOfDay.add(DUSK_TIME);
            this.timesOfDay.add(NIGHT_TIME);
            this.timesOfDay.add(DAWN_TIME);

            System.out.println("Loading weather...");
            this.weathers.add(CLEAR_WEATHER);
            this.weathers.add(RAIN_WEATHER);
            this.weathers.add(SNOW_WEATHER);
            this.weathers.add(HEAVY_RAIN_WEATHER);
            this.weathers.add(THUNDER_WEATHER);
            this.weathers.add(HEAVY_SNOW_WEATHER);
            this.weathers.add(WIND_WEATHER);
            this.weathers.add(HEAT_WEATHER);
            this.weathers.add(FROG_WEATHER);

            System.out.println("Loading status...");
            this.antstatuses.add(STATUS_ALIVE);
            this.antstatuses.add(STATUS_DEAD);
            this.antstatuses.add(STATUS_ZOMBIFIED);

            System.out.println("Loading ant types...");
            this.antTypes.add(TYPE_EGG);
            this.antTypes.add(TYPE_LARVA);
            this.antTypes.add(TYPE_PUPA);
            this.antTypes.add(TYPE_WORKER);
            this.antTypes.add(TYPE_SOLDIER);
            this.antTypes.add(TYPE_MAJOR);
            this.antTypes.add(TYPE_DRONE);
            this.antTypes.add(TYPE_PRINCESS);
            this.antTypes.add(TYPE_QUEEN);

            System.out.println("Loading ant subtypes...");
            System.out.println("Loading upgrades...");
            System.out.println("Loading synergies...");
            System.out.println("Loading bugs...");
        }
    
    public void loadFile(Savefile savefile){ 
        Colony colony = new Colony(1, "Grim Colony", true);
        System.out.println("Generating new world...");
        this.world.startWorld(this.biomes.get(0), colony); 
    }
    
    public void startUp(Savefile savefile){
        
        System.out.println("Loading new world...");
        World world = new World();
        this.setWorld(world);
        this.loadConstants();
        this.loadFile(savefile);
    }
    
    @Override
    public void run(){
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
                for (Runnable r : tickListeners) {
                    try {
                        r.run();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
