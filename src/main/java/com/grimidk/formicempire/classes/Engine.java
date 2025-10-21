/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grimidk.formicempire.classes;

import java.util.concurrent.Semaphore;
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

    public Engine() {
        this.delay = 1000;
        this.semaphore = new Semaphore(1);
        this.killSwitch = false;
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
    
    public void loadConstants(){
        
        System.out.println("Loading biomes...");
        Biome plainsBiome = new Biome(1, "Plains", 25, 2);
        this.biomes.add(plainsBiome);
        Biome forestBiome = new Biome(2, "Forest", 20, 3);
        this.biomes.add(forestBiome);
        Biome jungleBiome = new Biome(3, "Jungle", 30, 4);
        this.biomes.add(jungleBiome);
        Biome swampBiome = new Biome(4, "Swamp", 25, 5);
        this.biomes.add(swampBiome);
        Biome urbanBiome = new Biome(5, "Urban", 30, 1);
        this.biomes.add(urbanBiome);
        Biome tundraBiome = new Biome(6, "Tundra", 5, 2);
        this.biomes.add(tundraBiome);
        Biome taigaBiome = new Biome(7, "Taiga", 10, 1);
        this.biomes.add(taigaBiome);
        Biome dessertBiome = new Biome(8, "Dessert", 50, 0);
        this.biomes.add(dessertBiome);
        
        System.out.println("Loading resources...");
        Resource plantResource = new Resource(1, "Plant Matter", true, false);
        this.resources.add(plantResource);
        Resource fungiResource = new Resource(2, "Fungi Matter", true, false);
        this.resources.add(fungiResource);
        Resource meatResource = new Resource(3, "Animal Matter", true, false);
        this.resources.add(meatResource);
        Resource waterResource = new Resource(4, "Water", true, true);
        this.resources.add(waterResource);
        Resource syrupResource = new Resource(5, "Syrup", true, true);
        this.resources.add(syrupResource);
        Resource resinResource = new Resource(6, "Resin", false, true);
        this.resources.add(resinResource);
        Resource rockResource = new Resource(7, "Mineral", false, false);
        this.resources.add(rockResource);
    
        System.out.println("Loading times...");
        TimeOfDay dayTime = new TimeOfDay(1, "Daytime", 1);
        this.timesOfDay.add(dayTime);
        TimeOfDay duskTime = new TimeOfDay(2, "Dusk", 5/8);
        this.timesOfDay.add(duskTime);
        TimeOfDay nightTime = new TimeOfDay(3, "Nightime", 3/4);
        this.timesOfDay.add(nightTime);
        TimeOfDay dawnTime = new TimeOfDay(4, "Dawn", 5/8);
        this.timesOfDay.add(dawnTime);
        
        System.out.println("Loading weather...");
        Weather clearWeather = new Weather(1, "Clear", 1, 0);
        this.weathers.add(clearWeather);
        Weather rainWeather = new Weather(2, "Rain", 1, 1);
        this.weathers.add(rainWeather);
        Weather snowWeather = new Weather(3, "Snow", 1, 1);
        this.weathers.add(snowWeather);
        Weather heavyRainWeather = new Weather(4, "Heavy Rain", 1, 3);
        this.weathers.add(heavyRainWeather);
        Weather thunderWeather = new Weather(5, "Thunder Storm", 1, 2);
        this.weathers.add(thunderWeather);
        Weather heavySnowWeather = new Weather(6, "Snow Storm", 1, 2);
        this.weathers.add(heavySnowWeather);
        Weather windWeather = new Weather(7, "Heavy Wind", 1, -1);
        this.weathers.add(windWeather);
        Weather heatWeather = new Weather(8, "Heat Wave", 2, -2);
        this.weathers.add(heatWeather);
        Weather frogWeather = new Weather(9, "Frog Rain", 1, 0);
        this.weathers.add(frogWeather);
        
        System.out.println("Loading status...");
        AntStatus StatusAlive = new AntStatus(1, "Alive");
        this.antstatuses.add(StatusAlive);
        AntStatus StatusDead = new AntStatus(2, "Dead");
        this.antstatuses.add(StatusDead);
        AntStatus StatusZombified = new AntStatus(3, "Zombified");
        this.antstatuses.add(StatusZombified);
        
        System.out.println("Loading ant types...");
        AntType TypeEgg = new AntType(1, "Egg", 1, 1, 0, 1, 0, 1, 0, 0, 0, 1/4);
        this.antTypes.add(TypeEgg);
        AntType TypeLarva = new AntType(2, "Larva", 1, 1, 1/2, 1, 1/2, 1, 1, 1/2, 1/2, 1/2);
        this.antTypes.add(TypeLarva);
        AntType TypePupa = new AntType(3, "Pupa", 1, 1, 0, 1, 1, 1, 0, 1/2, 0, 1);
        this.antTypes.add(TypePupa);
        AntType TypeWorker = new AntType(4, "Worker", 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        this.antTypes.add(TypeWorker);
        AntType TypeSoldier = new AntType(5, "Soldier", 3, 2, 3, 1, 3, 2, 3, 5, 3, 2);
        this.antTypes.add(TypeSoldier);
        AntType TypeMajor = new AntType(6, "Major", 10, 10, 15, 5, 20, 5, 2, 50, 2, 5);
        this.antTypes.add(TypeMajor);
        AntType TypeDrone = new AntType(7, "Drone", 1, 1, 1, 1, 1, 1, 1, 1, 1, 2);
        this.antTypes.add(TypeDrone);
        AntType TypePrincess = new AntType(8, "Princess", 1, 1, 1, 1, 1, 1, 1, 1, 1, 2);
        this.antTypes.add(TypePrincess);
        AntType TypeQueen = new AntType(9, "Queen", 50, 50, 2, 1000, 50, 10, 1/2, 50, 1/4, 5);
        this.antTypes.add(TypeQueen);
        
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
    public void start(){
        
    }
}
