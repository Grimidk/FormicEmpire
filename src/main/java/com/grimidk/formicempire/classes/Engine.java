package com.grimidk.formicempire.classes;

import java.util.concurrent.Semaphore;

import com.grimidk.formicempire.classes.constants.AntRole;
import com.grimidk.formicempire.classes.constants.AntStatus;
import com.grimidk.formicempire.classes.constants.AntSubType;
import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.Biome;
import com.grimidk.formicempire.classes.constants.ColonyRank;
import com.grimidk.formicempire.classes.constants.MoonPhase;
import com.grimidk.formicempire.classes.constants.ResourceType;
import com.grimidk.formicempire.classes.constants.Season;
import com.grimidk.formicempire.classes.constants.Species;
import com.grimidk.formicempire.classes.constants.TimeOfDay;
import com.grimidk.formicempire.classes.constants.Weather;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;

public class Engine extends Thread{
    
    private World world;
    private ArrayList<AntStatus> antstatuses;
    private ArrayList<AntType> antTypes;
    private ArrayList<AntSubType> antSubTypes;
    private ArrayList<AntRole> antRoles;
    private ArrayList<Biome> biomes;
    private ArrayList<ColonyRank> colonyRanks;
    private ArrayList<ResourceType> resources;
    private ArrayList<Species> species;
    private ArrayList<TimeOfDay> timesOfDay;
    private ArrayList<MoonPhase> moonPhases;
    private ArrayList<Season> seasons;
    private ArrayList<Weather> weathers;
    private ArrayList<Upgrade> upgrades;    
    
    private float delay;
    private final Semaphore semaphore;
    private boolean killSwitch;
    private volatile boolean paused;
    private final CopyOnWriteArrayList<Runnable> tickListeners = new CopyOnWriteArrayList<>();

    public Engine() {
        this.delay = 500;
        this.semaphore = new Semaphore(1);
        this.killSwitch = false;
        this.paused = false;
        this.antstatuses = new ArrayList<>();
        this.antSubTypes = new ArrayList<>();
        this.antTypes = new ArrayList<>();
        this.antRoles = new ArrayList<>();
        this.biomes = new ArrayList<>();
        this.colonyRanks = new ArrayList<>();
        this.resources = new ArrayList<>();
        this.species = new ArrayList<>();
        this.timesOfDay = new ArrayList<>();
        this.moonPhases = new ArrayList<>();
        this.seasons = new ArrayList<>();
        this.weathers = new ArrayList<>();
        this.upgrades = new ArrayList<>();
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

    public ArrayList<AntType> getAntTypes() {
        return antTypes;
    }

    public void setAntTypes(ArrayList<AntType> antTypes) {
        this.antTypes = antTypes;
    }

    public ArrayList<AntSubType> getAntSubTypes() {
        return antSubTypes;
    }

    public void setAntSubTypes(ArrayList<AntSubType> antSubTypes) {
        this.antSubTypes = antSubTypes;
    }

    public ArrayList<AntRole> getAntRoles() {
        return antRoles;
    }

    public void setAntRoles(ArrayList<AntRole> antRoles) {
        this.antRoles = antRoles;
    }

    public ArrayList<Biome> getBiomes() {
        return biomes;
    }

    public void setBiomes(ArrayList<Biome> biomes) {
        this.biomes = biomes;
    }

    public ArrayList<ColonyRank> getColonyRanks() {
        return colonyRanks;
    }

    public void setColonyRanks(ArrayList<ColonyRank> colonyRanks) {
        this.colonyRanks = colonyRanks;
    }

    public ArrayList<ResourceType> getResources() {
        return resources;
    }

    public void setResources(ArrayList<ResourceType> resources) {
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

    public ArrayList<MoonPhase> getMoonPhases() {
        return moonPhases;
    }

    public void setMoonPhases(ArrayList<MoonPhase> moonPhases) {
        this.moonPhases = moonPhases;
    }

    public ArrayList<Season> getSeasons() {
        return seasons;
    }

    public void setSeasons(ArrayList<Season> seasons) {
        this.seasons = seasons;
    }

    public ArrayList<Weather> getWeathers() {
        return weathers;
    }

    public void setWeathers(ArrayList<Weather> weathers) {
        this.weathers = weathers;
    }

    public ArrayList<Upgrade> getUpgrades() {
        return upgrades;
    }

    public void setUpgrades(ArrayList<Upgrade> upgrades) {
        this.upgrades = upgrades;
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
        public static final ResourceType PLANT_RESOURCE = new ResourceType(1, "Plant Matter", true, false);
        public static final ResourceType FUNGI_RESOURCE = new ResourceType(2, "Fungi Matter", true, false);
        public static final ResourceType MEAT_RESOURCE = new ResourceType(3, "Animal Matter", true, false);
        public static final ResourceType WATER_RESOURCE = new ResourceType(4, "Water", true, true);
        public static final ResourceType SYRUP_RESOURCE = new ResourceType(5, "Syrup", true, true);
        public static final ResourceType RESIN_RESOURCE = new ResourceType(6, "Resin", false, true);
        public static final ResourceType ROCK_RESOURCE = new ResourceType(7, "Mineral", false, false);

        // Times of Day
        public static final TimeOfDay DAY_TIME = new TimeOfDay(1, "Daytime", 1);
        public static final TimeOfDay DUSK_TIME = new TimeOfDay(2, "Dusk", 5/8);
        public static final TimeOfDay NIGHT_TIME = new TimeOfDay(3, "Nightime", 3/4);
        public static final TimeOfDay DAWN_TIME = new TimeOfDay(4, "Dawn", 5/8);
        public static final TimeOfDay SOLAR_ECLIPSE_TIME = new TimeOfDay(5, "Solar Eclipse", 3/2);
        public static final TimeOfDay LUNAR_ECLIPSE_TIME = new TimeOfDay(6, "Lunar Eclipse", 3/2);

        // Moon Phases
        public static final MoonPhase NEW_MOON_PHASE = new MoonPhase(1, "New Moon", 1);
        public static final MoonPhase WAXING_CRESCENT_PHASE = new MoonPhase(2, "Waxing Crescent", 3/4);   
        public static final MoonPhase FIRST_QUARTER_PHASE = new MoonPhase(3, "First Quarter", 1/2);
        public static final MoonPhase WAXING_GIBBOUS_PHASE = new MoonPhase(4, "Waxing Gibbous", 1/4);
        public static final MoonPhase FULL_MOON_PHASE = new MoonPhase(5, "Full Moon", 0);
        public static final MoonPhase WANING_GIBBOUS_PHASE = new MoonPhase(6, "Waning Gibbous", 1/4);
        public static final MoonPhase LAST_QUARTER_PHASE = new MoonPhase(7, "Last Quarter", 1/2);
        public static final MoonPhase WANING_CRESCENT_PHASE = new MoonPhase(8, "Waning Crescent", 3/4);

        //Seasons
        public static final Season SPRING_SEASON = new Season(1, "Spring", 1, 3);
        public static final Season SUMMER_SEASON = new Season(2, "Summer", 2, 1);
        public static final Season AUTUMN_SEASON = new Season(3, "Autumn", 1, 2);
        public static final Season WINTER_SEASON = new Season(4, "Winter", 1/2, 1);

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
        public static final AntType TYPE_EGG = new AntType(1, "Egg", 1,  0, 1, 0, 0, 0, 0, 0, 1/4);
        public static final AntType TYPE_LARVA = new AntType(2, "Larva", 1,  1/2, 1, 1/2, 1, 1, 1/2, 1/2, 1/2);
        public static final AntType TYPE_PUPA = new AntType(3, "Pupa", 1,  0, 1, 1, 0, 0, 1/2, 0, 1);
        public static final AntType TYPE_WORKER = new AntType(4, "Worker", 1,  1, 1, 1, 1, 1, 1, 1, 1);
        public static final AntType TYPE_SOLDIER = new AntType(5, "Soldier", 3,  3, 1, 3, 2, 3, 5, 3, 2);
        public static final AntType TYPE_MAJOR = new AntType(6, "Major", 10,  15, 5, 20, 5, 2, 50, 2, 5);
        public static final AntType TYPE_DRONE = new AntType(7, "Drone", 1, 1, 1, 1, 1, 1, 1, 1, 2);
        public static final AntType TYPE_PRINCESS = new AntType(8, "Princess", 1, 1, 1, 1, 1, 1, 1, 1, 2);
        public static final AntType TYPE_QUEEN = new AntType(9, "Queen", 50,  2, 50, 50, 10, 1/2, 50, 1/4, 5);

        //Ant Subtypes
        
        //Ant Roles
        public static final AntRole ROLE_FORAGER = new AntRole(1, TYPE_WORKER, "Forager");
        public static final AntRole ROLE_NURSE = new AntRole(2, TYPE_WORKER, "Nurse");
        public static final AntRole ROLE_BUILDER = new AntRole(3, TYPE_WORKER, "Builder");
        public static final AntRole ROLE_SCOUT = new AntRole(4, TYPE_WORKER, "Scout");
        public static final AntRole ROLE_FARMER = new AntRole(5, TYPE_WORKER, "Farmer");
        public static final AntRole ROLE_RANCHER = new AntRole(6, TYPE_WORKER, "Rancher");
        public static final AntRole ROLE_GRAVER = new AntRole(7, TYPE_WORKER, "Grave-Keeper");
        public static final AntRole ROLE_MINER = new AntRole(8, TYPE_WORKER, "Miner");
        public static final AntRole ROLE_COURIER = new AntRole(9, TYPE_WORKER, "Courier");
        public static final AntRole ROLE_POTTER = new AntRole(10, TYPE_WORKER, "Portable-Feeder");
        public static final AntRole ROLE_GUARD = new AntRole(11, TYPE_SOLDIER, "Guard");
        public static final AntRole ROLE_WARRIOR = new AntRole(12, TYPE_SOLDIER, "Warrior");
        public static final AntRole ROLE_DEFENDER = new AntRole(13, TYPE_SOLDIER, "Defender");
        public static final AntRole ROLE_POLICE = new AntRole(14, TYPE_SOLDIER, "Parasite-Police");
        public static final AntRole ROLE_BOMBER = new AntRole(15, TYPE_SOLDIER, "Bomber");
        public static final AntRole ROLE_BRUTE = new AntRole(16, TYPE_MAJOR, "Brute");
        public static final AntRole ROLE_CARRIER = new AntRole(17, TYPE_MAJOR, "Troop-Carrier");
        public static final AntRole ROLE_ARTILLERY = new AntRole(18, TYPE_MAJOR, "Artillery");
        public static final AntRole ROLE_SIEGE = new AntRole(19, TYPE_MAJOR, "Siege-Engine");
        public static final AntRole ROLE_BORER = new AntRole(20, TYPE_MAJOR, "Boring-Machine");
        public static final AntRole ROLE_DRONE = new AntRole(21, TYPE_DRONE, "Drone");
        public static final AntRole ROLE_BREEDER = new AntRole(22, TYPE_PRINCESS, "Breeder");
        public static final AntRole ROLE_DIPLOMAT = new AntRole(23, TYPE_PRINCESS, "Diplomat");
        public static final AntRole ROLE_LAYER = new AntRole(24, TYPE_QUEEN, "Egg-Layer");
        public static final AntRole ROLE_RESEARCHER = new AntRole(25, TYPE_QUEEN, "Researcher");

        //Colony Ranks
        public static final ColonyRank RANK_ANT = new ColonyRank(1, "Ant", 1l);
        public static final ColonyRank RANK_COLONY = new ColonyRank(2, "Ant", 10l);
        public static final ColonyRank RANK_COUNTY = new ColonyRank(3, "Ant", 100l);
        public static final ColonyRank RANK_DUCHY = new ColonyRank(4, "Ant", 1000l);
        public static final ColonyRank RANK_KINGDOM = new ColonyRank(5, "Ant", 10000l);
        public static final ColonyRank RANK_EMPIRE = new ColonyRank(6, "Ant", 100000l);
        public static final ColonyRank RANK_SUPER = new ColonyRank(7, "Ant", 1000000l);
        public static final ColonyRank RANK_ULTRA = new ColonyRank(8, "Ant", 10000000l);
        public static final ColonyRank RANK_HYPER = new ColonyRank(9, "Ant", 100000000l);
        public static final ColonyRank RANK_MEGA = new ColonyRank(10, "Ant", 1000000000l);
        public static final ColonyRank RANK_ULTIMATE = new ColonyRank(11, "Ant", 10000000000l);
        public static final ColonyRank RANK_SUPREME = new ColonyRank(12, "Ant", 100000000000l);
        public static final ColonyRank RANK_GIGA = new ColonyRank(13, "Ant", 1000000000000l);

        // Ant Ultimate-Colony (10.000.000.000)
        // Ant Supreme-Colony (100.000.000.000)
        // Ant Giga-Colony (1.000.000.000.000)	
        
        //Species

        //Upgrades

        //Synergies

        //Bug Types

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
            this.timesOfDay.add(SOLAR_ECLIPSE_TIME);
            this.timesOfDay.add(LUNAR_ECLIPSE_TIME);

            System.out.println("Loading phases...");
            this.moonPhases.add(NEW_MOON_PHASE);
            this.moonPhases.add(WAXING_CRESCENT_PHASE);   
            this.moonPhases.add(FIRST_QUARTER_PHASE);
            this.moonPhases.add(WAXING_GIBBOUS_PHASE);      
            this.moonPhases.add(FULL_MOON_PHASE);
            this.moonPhases.add(WANING_GIBBOUS_PHASE);
            this.moonPhases.add(LAST_QUARTER_PHASE);
            this.moonPhases.add(WANING_CRESCENT_PHASE);

            System.out.println("Loading seasons...");
            this.seasons.add(SPRING_SEASON);;
            this.seasons.add(SUMMER_SEASON);
            this.seasons.add(AUTUMN_SEASON);
            this.seasons.add(WINTER_SEASON);

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

            System.out.println("Loading ant roles...");
            this.antRoles.add(ROLE_FORAGER);
            this.antRoles.add(ROLE_NURSE);
            this.antRoles.add(ROLE_BUILDER);            
            this.antRoles.add(ROLE_SCOUT);
            this.antRoles.add(ROLE_FARMER);
            this.antRoles.add(ROLE_RANCHER);
            this.antRoles.add(ROLE_GRAVER);
            this.antRoles.add(ROLE_MINER);
            this.antRoles.add(ROLE_COURIER);
            this.antRoles.add(ROLE_POTTER);
            this.antRoles.add(ROLE_GUARD);
            this.antRoles.add(ROLE_WARRIOR);
            this.antRoles.add(ROLE_DEFENDER);
            this.antRoles.add(ROLE_POLICE);
            this.antRoles.add(ROLE_BOMBER);
            this.antRoles.add(ROLE_BRUTE);
            this.antRoles.add(ROLE_CARRIER);
            this.antRoles.add(ROLE_ARTILLERY);  
            this.antRoles.add(ROLE_SIEGE);
            this.antRoles.add(ROLE_BORER);
            this.antRoles.add(ROLE_DRONE);
            this.antRoles.add(ROLE_BREEDER);    
            this.antRoles.add(ROLE_DIPLOMAT);
            this.antRoles.add(ROLE_LAYER);
            this.antRoles.add(ROLE_RESEARCHER);

            System.out.println("Loading ranks...");
            this.colonyRanks.add(RANK_ANT);
            this.colonyRanks.add(RANK_COLONY);
            this.colonyRanks.add(RANK_COUNTY);
            this.colonyRanks.add(RANK_DUCHY);
            this.colonyRanks.add(RANK_KINGDOM);
            this.colonyRanks.add(RANK_EMPIRE);
            this.colonyRanks.add(RANK_SUPER);
            this.colonyRanks.add(RANK_ULTRA);
            this.colonyRanks.add(RANK_HYPER);
            this.colonyRanks.add(RANK_MEGA);

            System.out.println("Loading species...");

            System.out.println("Loading upgrades...");

            System.out.println("Loading synergies...");

            System.out.println("Loading bug types...");
        }
    
    public void loadFile(Savefile savefile){ 
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
        this.world.startWorld(this.biomes.get(0), colony);
    }
    
    public void startUp(Savefile savefile){
        System.out.println("Loading new world...");
        World world = new World();
        this.setWorld(world);
        this.loadConstants();
        this.loadFile(savefile);
        try {
            if (savefile != null && this.world != null) {
                this.world.setSaveSlotId(savefile.getId());
            }
        } catch (Exception ignore) {}
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
