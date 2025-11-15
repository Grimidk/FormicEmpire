package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.constants.Building;
import com.grimidk.formicempire.classes.constants.Synergy;
import com.grimidk.formicempire.classes.constants.Upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameUnlocks {
    private GameUnlocks() {}

    // --- Upgrades ---
    // -- Types --
    public static final Upgrade TYPE_EGG = new Upgrade(1, "Egg, Larva and Pupa Type", "Brand New Ants", "Unlocks the juvenile ant types, allowing new ants to be born. Each stage takes 4 days to grow out of.", null, 0 );
    public static final Upgrade TYPE_WORKER = new Upgrade(2, "Worker Type", "Means of Production", "Unlocks the worker type, so your colony can sustain itself.", TYPE_EGG, 0 );
    public static final Upgrade TYPE_SOLDIER = new Upgrade(3, "Soldier Type", "The Red Army", "Unlocks the soldier type, you can now defend your colony.", TYPE_EGG, 100 );
    public static final Upgrade TYPE_MAJOR = new Upgrade(4, "Major Type", "Ant Tanks", "Unlocks the major type, the bulkiest ants around.", TYPE_SOLDIER, 0 );
    public static final Upgrade TYPE_PRINCESS = new Upgrade(5, "Princess and Drone Types", "Winged Ants", "Unlocks the princess and drone types, so new colonies can exist or you can get more queens.", TYPE_EGG,3000 );
    public static final Upgrade TYPE_QUEEN = new Upgrade(6, "Queen Type", "Ant Royalty", "Unlocks the queen type, the leader of the colony.", TYPE_PRINCESS, 0 );
    // -- Roles --
    public static final Upgrade ROLE_FORAGER = new Upgrade(7, "Forager Role", "Nature's Bounty", "Allows workers to collect plants and mushrooms. 1 ant can collect 1 resource per hour.", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_NURSE = new Upgrade(8, "Nurse Role", "Maternity Leave", "Allows workers to take care of juvenile ants. 1 ant can take care of 10 babies, excess babies can perish overnight.", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_FARMER = new Upgrade(9, "Farmer Role", "Grandpa's Farm", "Allows workers to convert plant and animal matter into mushroom. 1 ant can convert 1 plant and 1 protein per minute, in a 1:1 and 1:2 ratio respectively.", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_GRAVER = new Upgrade(10, "Grave-Keeper Role", "Grim Reaper", "Allows workers to bury dead ants. 1 ant can take care of 5 dead ants, too many dead ants can attract disease.", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_HUNTER = new Upgrade(11, "Hunter Role", "Hunter Instinct", "Allows soldiers to gather animal matter.  1 ant can collect 1 resource per hour.", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_LAYER = new Upgrade(12, "Egg Layer Role", "Ant Factory", "Allows queens to lay new eggs. 1 ant can lay 1 egg per hour.", TYPE_QUEEN, 0 );
    public static final Upgrade ROLE_RANCHER = new Upgrade(13, "Rancher Role", "Aphid Rancher", "Allows workers to ranch aphids. 1 ant can handle 10 aphids and herd 1 additional aphid per day. Each aphid produces 1 hoenydew (syrup).", ROLE_FORAGER, 500 );
    public static final Upgrade ROLE_RESEARCHER = new Upgrade(14, "Research Role", "Ant Science", "Allows queens to generate reserach points. 1 ant research 1 point per hour.", TYPE_QUEEN, 0 );
    public static final Upgrade ROLE_BUILDER = new Upgrade(15, "Builder Role", "Base Building", "Allows workers to build new rooms in the colony.", TYPE_WORKER, 250 );
    public static final Upgrade ROLE_SCOUT = new Upgrade(16, "Scout Role", "Adventure's Call", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_MINER = new Upgrade(17, "Miner Role", "Yearn For The Mines", "", TYPE_WORKER, 0 );     
    public static final Upgrade ROLE_COURIER = new Upgrade(18, "Courier Role", "Logistic Network", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_POTTER = new Upgrade(19, "Portable-Feeder Role", "A Helping Hand", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_GUARD = new Upgrade(20, "Guard Role", "Ant Bouncers", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_WARRIOR = new Upgrade(21, "Warrior Role", "Phalanx Formation", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_DEFENDER = new Upgrade(22, "Defender Role", "Royal Shield", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_POLICE = new Upgrade(23, "Parasite-Police Role", "Police Brutality", "", TYPE_SOLDIER, 0 );  // this.parasiteDetection = 10;
    public static final Upgrade ROLE_BOMBER = new Upgrade(24, "Bomber Role", "Explosive Finish", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_BRUTE = new Upgrade(25, "Brute Role", "Heavy Trooper", "Allows majors to become massive menaces in battle.", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_CARRIER = new Upgrade(26, "Carrier Role", "Troop Transport", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_ARTILLERY = new Upgrade(27, "Artillery Role", "Long Range Artillery", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_SIEGE = new Upgrade(28, "Siege-Machine Role", "Siege Technology", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_BORER = new Upgrade(29, "Tunel-Borer Role", "Ant Excavator", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_BREEDER = new Upgrade(30,"Breeder Roles", "Nuptial Flights", "Allows Princesses and Drones to go to nuptial flights and get more queens", TYPE_PRINCESS, 0 );
    public static final Upgrade ROLE_DIPLOMAT = new Upgrade(31, "Diplomat Role", "Mighter Than The Sword", "", TYPE_PRINCESS, 0 );
    public static final Upgrade ROLE_MILITIA = new Upgrade(32, "Militia Role", "Worker Militia Auxiliary", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_CATCHER = new Upgrade(33, "Catcher Role", "Bug Catching", "", TYPE_SOLDIER, 0 );
    // -- Stats -- 
    public static final Upgrade STAT_SKELETON = new Upgrade(34, "Basic Skeletons", "Basic Exoskeletons", "The basic defense stats for all the ants in your colony before multipliers. 100 health points, 5 defense points and 1 health point recovered per second.", TYPE_EGG, 0 );
    public static final Upgrade STAT_ACID = new Upgrade(35, "Acid Spiting", "Acidic Spit", "The basic offensive stats for all the ants in your colony before multipliers. 10 attack points, 1 attack per second and 1 speed point.", TYPE_EGG, 0 );
    public static final Upgrade STAT_LONGEVITY = new Upgrade(36, "Basic Longevity", "Standard Lifespan", "The basic life stats for all the ants in your colony before multipliers. 180 day lifespan, 25% temperature resistance and 4 days to grow per infant stage.", TYPE_EGG, 0 );
    // -- Abilities --
    public static final Upgrade ABILITY_RESEARCH = new Upgrade(37, "Research", "Micro Microscopes", "Allows you to research more ant technology by using research points, go to the research menu (Y).", ROLE_RESEARCHER, 0 );
    public static final Upgrade ABILITY_BUILD = new Upgrade(38, "Building", "Brick And Mortar", "Allows you to build parts of your colony, you can build in the build menu (U).", ROLE_BUILDER, 0 );
    public static final Upgrade ABILITY_SPREAD = new Upgrade(39, "Spreading", "Colony Colonization", "Allows you to build satellite colonies, you see them in world map (I).", ROLE_BREEDER, 0 );
    public static final Upgrade ABILITY_RESIN = new Upgrade(40, "Resin Harvest", "Resin Resonation", "Allows your to foragers to harvest resin while foraging plants as a bonus, there is 1% chance each foraging run.", ROLE_FORAGER, 5000 );
    // --- Buildings ---
    // -- Tier 0 --
    public static final Building ROYAL_CHAMBER_0 = new Building(1, "Basic Royal Chamber", 0, "The base queen chamber for the colony, holds 1 queen.", null, 0, 0, 0);
    public static final Building EGG_CHAMBER_0 = new Building(2, "Basic Egg Chamber", 0, "The base egg chamber for the colony, holds 50 juvenile ants of each type.", null, 0, 0, 0);
    public static final Building MUSHROOM_CHAMBER_0 = new Building(3, "Basic Mushroom Chamber", 0, "The base mushroom chamber for the colony, holds 8000 mushroom matter.", null, 0, 0, 0);
    public static final Building PLANT_CHAMBER_0 = new Building(4, "Basic Plant Chamber", 0, "The base plant chamber for the colony, holds 4000 plant matter.", null, 0, 0, 0);
    public static final Building WATER_RESERVOIR_0 = new Building(5, "Basic Water Reservoir", 0, "The base water reservoir for the colony, holds 1000 water drops.", null, 0, 0, 0);
    public static final Building MEAT_CHAMBER_0 = new Building(6, "Basic Protein Chamber", 0, "The base protein chamber for the colony, holds 2000 animal matter.", null,0, 0, 12);
    public static final Building SYRUP_RESERVOIR_0 = new Building(7, "Basic Syrups Reservoir", 0, "The base syrup reservoir for the colony, holds 500 syrup drops.", null, 0, 0, 24);
    public static final Building ROCK_WAREHOUSE_0 = new Building(8, "Basic Mineral Warehouse", 0, "The base mineral warehouse for the colony, holds 100 mineral rocks.", null, 0, 0, 50);
    public static final Building RESIN_RESERVOIR_0 = new Building(9, "Basic Resin Reservoir", 0, "The base resin reservoir for the colony, holds 200 resin drops.", null, 0, 0, 50);
    // -- Tier 1 --
    public static final Building ROYAL_CHAMBER_1 = new Building(10, "Expanded Royal Chamber", 1, "The upgraded queen chamber for the colony, holds 2 queens.", ROYAL_CHAMBER_0, 0, 0, 1000);
    public static final Building EGG_CHAMBER_1 = new Building(11, "Expanded Egg Chamber", 1, "The upgraded egg chamber for the colony, holds 80 juvenile ants of each type.", EGG_CHAMBER_0, 0, 0, 100);
    public static final Building MUSHROOM_CHAMBER_1 = new Building(12, "Expanded Mushroom Chamber", 1, "The upgraded mushroom chamber for the colony, holds 15000 mushroom matter.", MUSHROOM_CHAMBER_0, 0, 0, 150);
    public static final Building PLANT_CHAMBER_1 = new Building(13, "Expanded Plant Chamber", 1, "The upgraded plant chamber for the colony, holds 10000 plant matter.", PLANT_CHAMBER_0, 0, 0, 180);
    public static final Building WATER_RESERVOIR_1 = new Building(14, "Expanded Water Reservoir", 1, "The upgraded water reservoir for the colony, holds 2500 water drops.", WATER_RESERVOIR_0, 0, 0, 250);
    public static final Building MEAT_CHAMBER_1 = new Building(15, "Expanded Protein Chamber", 1, "The upgraded protein chamber for the colony, holds 5000 animal matter.", MEAT_CHAMBER_0, 0, 0, 200);
    public static final Building SYRUP_RESERVOIR_1 = new Building(16, "Expanded Syrups Reservoir", 1, "The upgraded syrup reservoir for the colony, holds 1200 syrup drops.", SYRUP_RESERVOIR_0, 0, 0, 250);
    public static final Building ROCK_WAREHOUSE_1 = new Building(17, "Expanded Mineral Warehouse", 1, "The upgraded mineral warehouse for the colony, holds 250 mineral rocks.", ROCK_WAREHOUSE_0, 0, 0, 500);
    public static final Building RESIN_RESERVOIR_1 = new Building(18, "Expanded Resin Reservoir", 1, "The upgraded resin reservoir for the colony, holds 500 resin drops.", RESIN_RESERVOIR_0, 0, 0, 400);
    // -- Tier 2 --
    public static final Building ROYAL_CHAMBER_2 = new Building(19, "Reinforced Royal Chamber", 2, "The resin-reinforced queen chamber for the colony, holds 4 queens.", ROYAL_CHAMBER_1, 500, 0, 3000);
    public static final Building EGG_CHAMBER_2 = new Building(20, "Reinforced Egg Chamber", 2, "The resin-reinforced egg chamber for the colony, holds 150 juvenile ants of each type.", EGG_CHAMBER_1, 200, 0, 1000);
    public static final Building MUSHROOM_CHAMBER_2 = new Building(21, "Reinforced Mushroom Chamber", 2, "The resin-reinforced mushroom chamber for the colony, holds 40000 mushroom matter.", MUSHROOM_CHAMBER_1, 250, 0, 800);
    public static final Building PLANT_CHAMBER_2 = new Building(22, "Reinforced Plant Chamber", 2, "The resin-reinforced plant chamber for the colony, holds 25000 plant matter.", PLANT_CHAMBER_1, 200, 0, 1000);
    public static final Building WATER_RESERVOIR_2 = new Building(23, "Reinforced Water Reservoir", 2, "The resin-reinforced water reservoir for the colony, holds 10000 water drops.", WATER_RESERVOIR_1, 300, 0, 1200);
    public static final Building MEAT_CHAMBER_2 = new Building(24, "Reinforced Protein Chamber", 2, "The resin-reinforced protein chamber for the colony, holds 15000 animal matter.", MEAT_CHAMBER_1, 350, 0, 1500);
    public static final Building SYRUP_RESERVOIR_2 = new Building(25, "Reinforced Syrups Reservoir", 2, "The resin-reinforced syrup reservoir for the colony, holds 3500 syrup drops.", SYRUP_RESERVOIR_1, 350, 0, 1500);
    public static final Building ROCK_WAREHOUSE_2 = new Building(26, "Reinforced Mineral Warehouse", 2, "The resin-reinforced mineral warehouse for the colony, holds 750 mineral rocks.", ROCK_WAREHOUSE_1, 450, 0, 2500);
    public static final Building RESIN_RESERVOIR_2 = new Building(27, "Reinforced Resin Reservoir", 2, "The resin-reinforced resin reservoir for the colony, holds 1200 resin drops.", RESIN_RESERVOIR_1, 400, 0, 2000);
    // -- Tier 3 --
    // -- Tier 4 --
    // -- Tier 5 --
    // -- Tier Misc. --

    // --- Synergies ---


    // --- Lists ---
    private static final List<Upgrade> upgrades = new ArrayList<>();
    private static final List<Building> buildings = new ArrayList<>();
    private static final List<Synergy> synergies = new ArrayList<>();

    static {
        upgrades.add(TYPE_EGG);
        upgrades.add(TYPE_WORKER);
        upgrades.add(TYPE_SOLDIER);
        upgrades.add(TYPE_MAJOR);
        upgrades.add(TYPE_PRINCESS);
        upgrades.add(TYPE_QUEEN);
        upgrades.add(ROLE_FORAGER);
        upgrades.add(ROLE_NURSE);
        upgrades.add(ROLE_FARMER);
        upgrades.add(ROLE_GRAVER);
        upgrades.add(ROLE_HUNTER);
        upgrades.add(ROLE_LAYER);
        upgrades.add(ROLE_RANCHER);
        upgrades.add(ROLE_BUILDER);
        upgrades.add(ROLE_BRUTE);
        upgrades.add(ROLE_BREEDER);
        upgrades.add(ROLE_RESEARCHER);
        upgrades.add(STAT_SKELETON);
        upgrades.add(STAT_ACID);
        upgrades.add(STAT_LONGEVITY);
        upgrades.add(ABILITY_RESEARCH);
        upgrades.add(ABILITY_BUILD);
        upgrades.add(ABILITY_SPREAD);
        upgrades.add(ABILITY_RESIN);

        buildings.add(ROYAL_CHAMBER_0);
        buildings.add(EGG_CHAMBER_0);
        buildings.add(MUSHROOM_CHAMBER_0);
        buildings.add(PLANT_CHAMBER_0);
        buildings.add(WATER_RESERVOIR_0);
        buildings.add(MEAT_CHAMBER_0);
        buildings.add(SYRUP_RESERVOIR_0);
        buildings.add(ROCK_WAREHOUSE_0);
        buildings.add(RESIN_RESERVOIR_0);
        buildings.add(ROYAL_CHAMBER_1);
        buildings.add(EGG_CHAMBER_1);
        buildings.add(MUSHROOM_CHAMBER_1);
        buildings.add(PLANT_CHAMBER_1);
        buildings.add(WATER_RESERVOIR_1);
        buildings.add(MEAT_CHAMBER_1);
        buildings.add(SYRUP_RESERVOIR_1);
        buildings.add(ROCK_WAREHOUSE_1);
        buildings.add(RESIN_RESERVOIR_1);
        buildings.add(ROYAL_CHAMBER_2);
        buildings.add(EGG_CHAMBER_2);
        buildings.add(MUSHROOM_CHAMBER_2);
        buildings.add(PLANT_CHAMBER_2);
        buildings.add(WATER_RESERVOIR_2);
        buildings.add(MEAT_CHAMBER_2);
        buildings.add(SYRUP_RESERVOIR_2);
        buildings.add(ROCK_WAREHOUSE_2);
        buildings.add(RESIN_RESERVOIR_2);
    }

    public static List<Upgrade> getUpgrades() {
        return Collections.unmodifiableList(upgrades);
    }

    public static List<Building> getBuildings() {
        return Collections.unmodifiableList(buildings);
    }

    public static List<Synergy> getSynergies() {
        return Collections.unmodifiableList(synergies);
    }
}