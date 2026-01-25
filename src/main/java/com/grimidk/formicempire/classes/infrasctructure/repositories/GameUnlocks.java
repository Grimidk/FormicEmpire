package com.grimidk.formicempire.classes.infrasctructure.repositories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;

public final class GameUnlocks {
    private GameUnlocks() {}
    // --- Lists ---
    private static final List<Upgrade> upgrades = new ArrayList<>();
    private static final List<Building> buildings = new ArrayList<>();
    private static final List<Synergy> synergies = new ArrayList<>();

    // --- Upgrades ---
    // -- Types --
    public static final Upgrade TYPE_EGG = new Upgrade(1, "Egg, Larva and Pupa Type", "Brand New Ants", "Unlocks the juvenile ant types, allowing new ants to be born. Each stage takes 4 days to grow out of.", null, 0 );
    static { upgrades.add(TYPE_EGG); }
    public static final Upgrade TYPE_WORKER = new Upgrade(2, "Worker Type", "Means of Production", "Unlocks the worker type, so your colony can sustain itself.", TYPE_EGG, 0 );
    static { upgrades.add(TYPE_WORKER); }
    public static final Upgrade TYPE_SOLDIER = new Upgrade(3, "Soldier Type", "The Red Army", "Unlocks the soldier type, you can now defend your colony.", TYPE_EGG, 100 );
    static { upgrades.add(TYPE_SOLDIER); }  
    public static final Upgrade TYPE_MAJOR = new Upgrade(4, "Major Type", "Ant Tanks", "Unlocks the major type, the bulkiest ants around.", TYPE_SOLDIER, 0 );
    static { upgrades.add(TYPE_MAJOR); }
    public static final Upgrade TYPE_PRINCESS = new Upgrade(5, "Princess and Drone Types", "Winged Ants", "Unlocks the princess and drone types, so new colonies can exist or you can get more queens.", TYPE_EGG,3000 );
    static { upgrades.add(TYPE_PRINCESS); }
    public static final Upgrade TYPE_QUEEN = new Upgrade(6, "Queen Type", "Ant Royalty", "Unlocks the queen type, the leader of the colony.", TYPE_PRINCESS, 0 );
    static { upgrades.add(TYPE_QUEEN); }
    // -- Roles --
    public static final Upgrade ROLE_FORAGER = new Upgrade(7, "Forager Role", "Nature's Bounty", "Allows workers to collect plants and mushrooms. 1 ant can collect 1 resource per hour.", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_FORAGER); }
    public static final Upgrade ROLE_NURSE = new Upgrade(8, "Nurse Role", "Maternity Leave", "Allows workers to take care of juvenile ants. 1 ant can take care of 10 babies, excess babies can perish overnight.", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_NURSE); }
    public static final Upgrade ROLE_FARMER = new Upgrade(9, "Farmer Role", "Grandpa's Farm", "Allows workers to convert plant and animal matter into mushroom. 1 ant can convert 0.1 plant and 0.1 protein per minute, in a 1:1 and 1:2 ratio respectively.", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_FARMER); }
    public static final Upgrade ROLE_GRAVER = new Upgrade(10, "Grave-Keeper Role", "Grim Reaper", "Allows workers to bury dead ants. 1 ant can take care of 5 dead ants, too many dead ants can attract disease.", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_GRAVER); }
    public static final Upgrade ROLE_HUNTER = new Upgrade(11, "Hunter Role", "Hunter Instinct", "Allows soldiers to gather animal matter.  1 ant can collect 1 resource per hour.", TYPE_SOLDIER, 0 );
    static { upgrades.add(ROLE_HUNTER); }
    public static final Upgrade ROLE_LAYER = new Upgrade(12, "Egg Layer Role", "Ant Factory", "Allows queens to lay new eggs. 1 ant can lay 1 egg per hour.", TYPE_QUEEN, 0 );
    static { upgrades.add(ROLE_LAYER); }
    public static final Upgrade ROLE_RANCHER = new Upgrade(13, "Rancher Role", "Aphid Rancher", "Allows workers to ranch aphids. 1 ant can handle 10 aphids and herd 1 additional aphid per day. Each aphid produces 1 hoenydew (syrup).", ROLE_FORAGER, 500 );
    static { upgrades.add(ROLE_RANCHER); }
    public static final Upgrade ROLE_RESEARCHER = new Upgrade(14, "Research Role", "Ant Science", "Allows queens to generate reserach points. 1 ant research 1 point per hour.", TYPE_QUEEN, 0 );
    static { upgrades.add(ROLE_RESEARCHER); }
    public static final Upgrade ROLE_BUILDER = new Upgrade(15, "Builder Role", "Base Building", "Allows workers to build new rooms in the colony.", TYPE_WORKER, 250 );
    static { upgrades.add(ROLE_BUILDER); }
    public static final Upgrade ROLE_SCOUT = new Upgrade(16, "Scout Role", "Adventure's Call", "Allows workers to go find new sources of resources, each scout has 10% chance each day to encounter a plant source.", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_SCOUT); }
    public static final Upgrade ROLE_MINER = new Upgrade(17, "Miner Role", "Yearn For The Mines", "", TYPE_WORKER, 0 );     
    public static final Upgrade ROLE_COURIER = new Upgrade(18, "Courier Role", "Logistic Network", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_POTTER = new Upgrade(19, "Portable-Feeder Role", "A Helping Hand", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_GUARD = new Upgrade(20, "Guard Role", "Ant Bouncers", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_WARRIOR = new Upgrade(21, "Warrior Role", "Phalanx Formation", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_DEFENDER = new Upgrade(22, "Defender Role", "Royal Shield", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_POLICE = new Upgrade(23, "Parasite-Police Role", "Police Brutality", "Allows soldiers to detect parasitic bugs disguising as your ants, each one has a 10% chance to detect parasites per day.", TYPE_SOLDIER, 0 ); 
    static { upgrades.add(ROLE_POLICE); }
    public static final Upgrade ROLE_BOMBER = new Upgrade(24, "Bomber Role", "Explosive Finish", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_BRUTE = new Upgrade(25, "Brute Role", "Heavy Trooper", "Allows majors to become massive menaces in battle.", TYPE_MAJOR, 0 );
    static { upgrades.add(ROLE_BRUTE); }
    public static final Upgrade ROLE_CARRIER = new Upgrade(26, "Carrier Role", "Troop Transport", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_ARTILLERY = new Upgrade(27, "Artillery Role", "Long Range Artillery", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_SIEGE = new Upgrade(28, "Siege-Machine Role", "Siege Technology", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_BORER = new Upgrade(29, "Tunel-Borer Role", "Ant Excavator", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_BREEDER = new Upgrade(30,"Breeder Roles", "Nuptial Flights", "Allows Princesses and Drones to go to nuptial flights and get more queens or spread to new places.", TYPE_PRINCESS, 0 );
    static { upgrades.add(ROLE_BREEDER); }
    public static final Upgrade ROLE_DIPLOMAT = new Upgrade(31, "Diplomat Role", "Mighter Than The Sword", "", TYPE_PRINCESS, 0 );
    public static final Upgrade ROLE_MILITIA = new Upgrade(32, "Militia Role", "Worker Militia Auxiliary", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_CATCHER = new Upgrade(33, "Catcher Role", "Bug Catching", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_CRANE = new Upgrade(34, "Construction Crane Role", "Heavy Duty", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_TRANSPORT = new Upgrade(35, "Resource Transport Role", "Heavy Lifting", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_ASSISTANT = new Upgrade(36, "Lab Assistant Role", "Lab Internship", "Allows princesses to help research with 1/5 the efficency of queens.", TYPE_PRINCESS, 1500 );
    static { upgrades.add(ROLE_ASSISTANT); }
    public static final Upgrade ROLE_ESCORT = new Upgrade(37, "Convoy Escort Role", "Protective Detail", "", TYPE_SOLDIER, 0 );
    // -- Stats -- 
    public static final Upgrade STAT_SKELETON = new Upgrade(101, "Basic Skeletons", "Basic Exoskeletons", "The basic defense stats for all the ants in your colony before multipliers. 100 health points, 5 defense points and 1 health point recovered per second.", TYPE_EGG, 0 );
    static { upgrades.add(STAT_SKELETON); }
    public static final Upgrade STAT_ACID = new Upgrade(102, "Acid Spiting", "Acidic Spit", "The basic offensive stats for all the ants in your colony before multipliers. 10 attack points, 1 attack per second and 1 speed point.", TYPE_EGG, 0 );
    static { upgrades.add(STAT_ACID); }
    public static final Upgrade STAT_LONGEVITY = new Upgrade(103, "Basic Longevity", "Standard Lifespan", "The basic life stats for all the ants in your colony before multipliers. 180 day lifespan, 25% temperature resistance, 20% thirst resistance, 1 unit of size and consumes 1 food per day.", TYPE_EGG, 0 );
    static { upgrades.add(STAT_LONGEVITY); }
    public static final Upgrade STAT_RESEARCH_1 = new Upgrade(104, "Improved Research Efficiency", "Micro Scopes", "Increases research speed by 1 point per hour.", ROLE_RESEARCHER, 800 );
    static { upgrades.add(STAT_RESEARCH_1); }
    public static final Upgrade STAT_RESEARCH_2 = new Upgrade(105, "Advanced Research Efficiency", "Miniature Laboratories", "Increases research speed by 2 more points per hour.", STAT_RESEARCH_1, 2500 );
    static { upgrades.add(STAT_RESEARCH_2); }
    public static final Upgrade STAT_RESEARCH_3 = new Upgrade(106, "Perfected Research Efficiency", "Nano Technology", "Increases research speed by 4 more points per hour.", STAT_RESEARCH_2, 12000 );
    static { upgrades.add(STAT_RESEARCH_3); }
    public static final Upgrade STAT_GROWTH_1 = new Upgrade(107, "Improved Growth", "Egg Incubating", "Decreases juvenile growth time by 1 day.", TYPE_EGG, 3500 );
    static { upgrades.add(STAT_GROWTH_1); }
    public static final Upgrade STAT_GROWTH_2 = new Upgrade(108, "Advanced Growth", "Larval Education", "Decreases juvenile growth time by 1 more day.", STAT_GROWTH_1, 9000 );
    static { upgrades.add(STAT_GROWTH_2); }
    public static final Upgrade STAT_GROWTH_3 = new Upgrade(109, "Perfected Growth", "Pupal Bedding", "Decreases juvenile growth time by 1 more day.", STAT_GROWTH_2, 20000 );
    static { upgrades.add(STAT_GROWTH_3); }
    public static final Upgrade STAT_THIRST_1 = new Upgrade(110, "Improved Metabolism", "Water Retention", "Increases the colony's thirst resistance to 40%.", STAT_LONGEVITY, 1500 );
    static { upgrades.add(STAT_THIRST_1); }
    public static final Upgrade STAT_THIRST_2 = new Upgrade(111, "Advanced Metabolism", "Camel Ants", "Increases the colony's thirst resistance to 60%.", STAT_THIRST_1, 5000 );
    static { upgrades.add(STAT_THIRST_2); }
    public static final Upgrade STAT_THIRST_3 = new Upgrade(112, "Perfected Metabolism", "Hydro-Lock Shells", "Increases the colony's thirst resistance to 80%.", STAT_THIRST_2, 15000 );
    static { upgrades.add(STAT_THIRST_3); }
    public static final Upgrade STAT_LOGISTICS_1 = new Upgrade(113, "Improved Logistics", "Ant Spreadsheets", "Allows your colony to manage 5 sources of each resource.", ROLE_SCOUT, 600);
    static { upgrades.add(STAT_LOGISTICS_1); }
    public static final Upgrade STAT_PASSIVE_1 = new Upgrade(114, "Passive Worker Efficiency", "Active Passiveness", "Increases the efficiency of passive worker buildings by another 1.", STAT_RESEARCH_3, 25000);
    static { upgrades.add(STAT_PASSIVE_1); }
    public static final Upgrade STAT_LAYING_1 = new Upgrade(115, "Improved Egg Laying", "Double Yolks", "Increases egg laying rate by 1 egg per hour.", ROLE_LAYER, 5000);
    static { upgrades.add(STAT_LAYING_1); }
    public static final Upgrade STAT_LAYING_2 = new Upgrade(116, "Advanced Egg Laying", "Quadruplet Eggs", "Increases egg laying rate by 2 more eggs per hour.", STAT_LAYING_1, 15000);
    static { upgrades.add(STAT_LAYING_2); }
    public static final Upgrade STAT_LAYING_3 = new Upgrade(117, "Perfected Egg Laying", "Half a dozen", "Increases egg laying rate by anoter 2 more eggs per hour.", STAT_LAYING_2, 80000);
    static { upgrades.add(STAT_LAYING_3); }
    public static final Upgrade STAT_SCOUTING_1 = new Upgrade(118, "Improved Scouting", "Ant Maps", "Increases scouting rate by 0.1 chance per day.", ROLE_SCOUT, 4000);
    static { upgrades.add(STAT_SCOUTING_1); }
    public static final Upgrade STAT_SCOUTING_2 = new Upgrade(119, "Advanced Scouting", "Scouting Kits", "Increases scouting rate by another 0.2 chance per day.", STAT_SCOUTING_1, 15000);
    static { upgrades.add(STAT_SCOUTING_2); }
    public static final Upgrade STAT_SCOUTING_3 = new Upgrade(120, "Perfected Scouting", "Exploration Drones", "Increases scouting rate by another 0.3 chance per day.", STAT_SCOUTING_2, 35000);
    static { upgrades.add(STAT_SCOUTING_3); }
    public static final Upgrade STAT_FARMING_1 = new Upgrade(121, "Improved Farming", "Fertilizers", "Increases farming conversion rate by 0.1 per minute.", ROLE_FARMER, 4500);
    static { upgrades.add(STAT_FARMING_1); }
    public static final Upgrade STAT_FARMING_2 = new Upgrade(122, "Advanced Farming", "Pesticides", "Increases farming conversion rate by another 0.2 per minute.", STAT_FARMING_1, 16000);
    static { upgrades.add(STAT_FARMING_2); }
    public static final Upgrade STAT_FARMING_3 = new Upgrade(123, "Perfected Farming", "Genetic Modification", "Increases farming conversion rate by another 0.4 per minute.", STAT_FARMING_2, 60000);
    static { upgrades.add(STAT_FARMING_3); }
    public static final Upgrade STAT_CONTAMINATION_1 = new Upgrade(124, "Improved Hygiene", "Body Disposal", "Reduces contamination death rate by 20%.", ROLE_GRAVER, 2000);
    static { upgrades.add(STAT_CONTAMINATION_1); }
    public static final Upgrade STAT_CONTAMINATION_2 = new Upgrade(125, "Advanced Hygiene", "Clean Environment", "Reduces contamination death rate by 40%.", STAT_CONTAMINATION_1, 8000);
    static { upgrades.add(STAT_CONTAMINATION_2); }
    public static final Upgrade STAT_CONTAMINATION_3 = new Upgrade(126, "Perfected Hygiene", "Isolation Protocols", "Reduces contamination death rate by 60%.", STAT_CONTAMINATION_2, 25000);
    static { upgrades.add(STAT_CONTAMINATION_3); }
    public static final Upgrade STAT_GRAVING_1 = new Upgrade(127, "Improved Graving", "Shallow Graves", "Increases graving rate by 5 per day.", ROLE_GRAVER, 1500);
    static { upgrades.add(STAT_GRAVING_1); }
    public static final Upgrade STAT_GRAVING_2 = new Upgrade(128, "Advanced Graving", "Mass Graves", "Increases graving rate by anoter 6 per day.", STAT_GRAVING_1, 6500);
    static { upgrades.add(STAT_GRAVING_2); }
    public static final Upgrade STAT_GRAVING_3 = new Upgrade(129, "Perfected Graving", "Structured Graveyard", "Increases graving rate by anoter 8 per day.", STAT_GRAVING_2, 40000);
    static { upgrades.add(STAT_GRAVING_3); }
    public static final Upgrade STAT_POLICING_1 = new Upgrade(130, "Improved Policing", "Profiling", "Increases policing rate to 20% per day.", ROLE_POLICE, 3000);
    static { upgrades.add(STAT_POLICING_1); }
    public static final Upgrade STAT_POLICING_2 = new Upgrade(131, "Advanced Policing", "Police Database", "Increases policing rate to 35% per day.", STAT_POLICING_1, 14000);
    static { upgrades.add(STAT_POLICING_2); }
    public static final Upgrade STAT_POLICING_3 = new Upgrade(132, "Perfected Policing", "Surveillance Drones", "Increases policing rate to 55% per day.", STAT_POLICING_2, 42000);
    static { upgrades.add(STAT_POLICING_3); }
    // -- Abilities --
    public static final Upgrade ABILITY_RESEARCH = new Upgrade(201, "Research", "Micro Microscopes", "Allows you to research more ant technology by using research points, go to the research menu (Y).", ROLE_RESEARCHER, 0 );
    static { upgrades.add(ABILITY_RESEARCH); }
    public static final Upgrade ABILITY_BUILD = new Upgrade(202, "Building", "Brick And Mortar", "Allows you to build parts of your colony, you can build in the build menu (U).", ROLE_BUILDER, 0 );
    static { upgrades.add(ABILITY_BUILD); }
    public static final Upgrade ABILITY_SPREAD = new Upgrade(203, "Spreading", "Colony Colonization", "Allows you to build satellite colonies, you see them in world map (I).", ROLE_BREEDER, 0 );
    static { upgrades.add(ABILITY_SPREAD); }
    public static final Upgrade ABILITY_RESIN = new Upgrade(204, "Resin Harvest", "Resin Resonation", "Allows your to foragers to harvest resin while foraging plants as a bonus, there is 1% chance each foraging run.", ROLE_FORAGER, 5000 );
    static { upgrades.add(ABILITY_RESIN); }
    public static final Upgrade ABILITY_FORCED_FLIGHT = new Upgrade(505, "Forced Nuptial Flights", "Royal Decree", "Allows you to force a nuptial flight by spending 1000 research points.", ROLE_BREEDER, 9000);
    static { upgrades.add(ABILITY_FORCED_FLIGHT); }

    // --- Buildings ---
    // -- Tier 0 --
    public static final Building ROYAL_CHAMBER_0 = new Building(1, "Basic Royal Chamber", 0, "The base queen chamber for the colony, holds 1 queen.", null, 0, 0, 0);
    static { buildings.add(ROYAL_CHAMBER_0); }
    public static final Building EGG_CHAMBER_0 = new Building(2, "Basic Egg Chamber", 0, "The base egg chamber for the colony, holds 50 juvenile ants of each type.", null, 0, 0, 0);
    static { buildings.add(EGG_CHAMBER_0); }
    public static final Building MUSHROOM_CHAMBER_0 = new Building(3, "Basic Mushroom Chamber", 0, "The base mushroom chamber for the colony, holds 8000 mushroom matter.", null, 0, 0, 0);
    static { buildings.add(MUSHROOM_CHAMBER_0); }
    public static final Building PLANT_CHAMBER_0 = new Building(4, "Basic Plant Chamber", 0, "The base plant chamber for the colony, holds 4000 plant matter.", null, 0, 0, 0);
    static { buildings.add(PLANT_CHAMBER_0); }
    public static final Building WATER_RESERVOIR_0 = new Building(5, "Basic Water Reservoir", 0, "The base water reservoir for the colony, holds 1000 water drops.", null, 0, 0, 0);
    static { buildings.add(WATER_RESERVOIR_0); }
    public static final Building MEAT_CHAMBER_0 = new Building(6, "Basic Protein Chamber", 0, "The base protein chamber for the colony, holds 2000 animal matter.", null,0, 0, 12);
    static { buildings.add(MEAT_CHAMBER_0); }
    public static final Building SYRUP_RESERVOIR_0 = new Building(7, "Basic Syrups Reservoir", 0, "The base syrup reservoir for the colony, holds 500 syrup drops.", null, 0, 0, 24);
    static { buildings.add(SYRUP_RESERVOIR_0); }
    public static final Building ROCK_WAREHOUSE_0 = new Building(8, "Basic Mineral Warehouse", 0, "The base mineral warehouse for the colony, holds 100 mineral rocks.", null, 0, 0, 50);
    static { buildings.add(ROCK_WAREHOUSE_0); }
    public static final Building RESIN_RESERVOIR_0 = new Building(9, "Basic Resin Reservoir", 0, "The base resin reservoir for the colony, holds 200 resin drops.", null, 0, 0, 50);
    static { buildings.add(RESIN_RESERVOIR_0); }
    // -- Tier 1 --
    public static final Building ROYAL_CHAMBER_1 = new Building(10, "Expanded Royal Chamber", 1, "The upgraded queen chamber for the colony, holds 2 queens.", ROYAL_CHAMBER_0, 0, 0, 1000);
    static { buildings.add(ROYAL_CHAMBER_1); }
    public static final Building EGG_CHAMBER_1 = new Building(11, "Expanded Egg Chamber", 1, "The upgraded egg chamber for the colony, holds 80 juvenile ants of each type.", EGG_CHAMBER_0, 0, 0, 100);
    static { buildings.add(EGG_CHAMBER_1); }
    public static final Building MUSHROOM_CHAMBER_1 = new Building(12, "Expanded Mushroom Chamber", 1, "The upgraded mushroom chamber for the colony, holds 15000 mushroom matter.", MUSHROOM_CHAMBER_0, 0, 0, 150);
    static { buildings.add(MUSHROOM_CHAMBER_1); }
    public static final Building PLANT_CHAMBER_1 = new Building(13, "Expanded Plant Chamber", 1, "The upgraded plant chamber for the colony, holds 10000 plant matter.", PLANT_CHAMBER_0, 0, 0, 180);
    static { buildings.add(PLANT_CHAMBER_1); }
    public static final Building WATER_RESERVOIR_1 = new Building(14, "Expanded Water Reservoir", 1, "The upgraded water reservoir for the colony, holds 2500 water drops.", WATER_RESERVOIR_0, 0, 0, 250);
    static { buildings.add(WATER_RESERVOIR_1); }
    public static final Building MEAT_CHAMBER_1 = new Building(15, "Expanded Protein Chamber", 1, "The upgraded protein chamber for the colony, holds 5000 animal matter.", MEAT_CHAMBER_0, 0, 0, 200);
    static { buildings.add(MEAT_CHAMBER_1); }
    public static final Building SYRUP_RESERVOIR_1 = new Building(16, "Expanded Syrups Reservoir", 1, "The upgraded syrup reservoir for the colony, holds 1200 syrup drops.", SYRUP_RESERVOIR_0, 0, 0, 250);
    static { buildings.add(SYRUP_RESERVOIR_1); }
    public static final Building ROCK_WAREHOUSE_1 = new Building(17, "Expanded Mineral Warehouse", 1, "The upgraded mineral warehouse for the colony, holds 250 mineral rocks.", ROCK_WAREHOUSE_0, 0, 0, 500);
    static { buildings.add(ROCK_WAREHOUSE_1); }
    public static final Building RESIN_RESERVOIR_1 = new Building(18, "Expanded Resin Reservoir", 1, "The upgraded resin reservoir for the colony, holds 500 resin drops.", RESIN_RESERVOIR_0, 0, 0, 400);
    static { buildings.add(RESIN_RESERVOIR_1); }
    // -- Tier 2 --
    public static final Building ROYAL_CHAMBER_2 = new Building(19, "Reinforced Royal Chamber", 2, "The resin-reinforced queen chamber for the colony, holds 4 queens.", ROYAL_CHAMBER_1, 500, 0, 3000);
    static { buildings.add(ROYAL_CHAMBER_2); }
    public static final Building EGG_CHAMBER_2 = new Building(20, "Reinforced Egg Chamber", 2, "The resin-reinforced egg chamber for the colony, holds 150 juvenile ants of each type.", EGG_CHAMBER_1, 200, 0, 1000);
    static { buildings.add(EGG_CHAMBER_2); }
    public static final Building MUSHROOM_CHAMBER_2 = new Building(21, "Reinforced Mushroom Chamber", 2, "The resin-reinforced mushroom chamber for the colony, holds 40000 mushroom matter.", MUSHROOM_CHAMBER_1, 250, 0, 800);
    static { buildings.add(MUSHROOM_CHAMBER_2); }
    public static final Building PLANT_CHAMBER_2 = new Building(22, "Reinforced Plant Chamber", 2, "The resin-reinforced plant chamber for the colony, holds 25000 plant matter.", PLANT_CHAMBER_1, 200, 0, 1000);
    static { buildings.add(PLANT_CHAMBER_2); }
    public static final Building WATER_RESERVOIR_2 = new Building(23, "Reinforced Water Reservoir", 2, "The resin-reinforced water reservoir for the colony, holds 10000 water drops.", WATER_RESERVOIR_1, 300, 0, 1200);
    static { buildings.add(WATER_RESERVOIR_2); }
    public static final Building MEAT_CHAMBER_2 = new Building(24, "Reinforced Protein Chamber", 2, "The resin-reinforced protein chamber for the colony, holds 15000 animal matter.", MEAT_CHAMBER_1, 350, 0, 1500);
    static { buildings.add(MEAT_CHAMBER_2); }
    public static final Building SYRUP_RESERVOIR_2 = new Building(25, "Reinforced Syrups Reservoir", 2, "The resin-reinforced syrup reservoir for the colony, holds 3500 syrup drops.", SYRUP_RESERVOIR_1, 350, 0, 1500);
    static { buildings.add(SYRUP_RESERVOIR_2); }
    public static final Building ROCK_WAREHOUSE_2 = new Building(26, "Reinforced Mineral Warehouse", 2, "The resin-reinforced mineral warehouse for the colony, holds 750 mineral rocks.", ROCK_WAREHOUSE_1, 450, 0, 2500);
    static { buildings.add(ROCK_WAREHOUSE_2); }
    public static final Building RESIN_RESERVOIR_2 = new Building(27, "Reinforced Resin Reservoir", 2, "The resin-reinforced resin reservoir for the colony, holds 1200 resin drops.", RESIN_RESERVOIR_1, 400, 0, 2000);
    static { buildings.add(RESIN_RESERVOIR_2); }
    // -- Tier 3 --
    // -- Tier 4 --
    // -- Tier 5 --
    // -- Tier Misc. --
    public static final Building PASSIVE_LAB = new Building(101, "Passive Experiments", 0, "Enables passive research gain even if there are no ants assigned, simulates 1 researcher.", ROYAL_CHAMBER_1, 150, 0, 800);
    static { buildings.add(PASSIVE_LAB); }
    public static final Building PASSIVE_WATER = new Building(102, "Passive Water Collector", 0, "Enables passive water collection even if there are no ants assigned, at 10% your water capacity.", WATER_RESERVOIR_1, 100, 0, 500);
    static { buildings.add(PASSIVE_WATER); }
    public static final Building PASSIVE_APHID = new Building(103, "Passive Aphid Ranch", 0, "Enables your colony to hold more aphids that will never escape, simulates 1 rancher.", SYRUP_RESERVOIR_1, 200, 0, 1000);
    static { buildings.add(PASSIVE_APHID); }
    public static final Building PASSIVE_NURSE = new Building(104, "Passive Nursery", 0, "Enables passive juvenile care even if there are no ants assigned, simulates 1 nurse.", EGG_CHAMBER_1, 150, 0, 700);
    static { buildings.add(PASSIVE_NURSE); }
    public static final Building PASSIVE_FARM = new Building(105, "Passive Farm", 0, "Enables passive farming even if there are no ants assigned, simulates 1 farmer.", MUSHROOM_CHAMBER_1, 200, 0, 900);
    static { buildings.add(PASSIVE_FARM); }
    public static final Building PASSIVE_GRAVE = new Building(106, "Passive Graveyard", 0, "Enables passive grave keeping even if there are no ants assigned, simulates 1 graver.", MEAT_CHAMBER_1, 150, 0, 600);
    static { buildings.add(PASSIVE_GRAVE); }
    public static final Building BUILDING_COMPOSTER = new Building(107, "Composter", 0, "Turns dead bodies into mushrooms daily. 1 body to 4 mushrooms matter.", PASSIVE_GRAVE, 500, 0, 1000);
    static { buildings.add(BUILDING_COMPOSTER); }

    // --- Assimilations ---

    // --- Synergies ---

    // --- Getters ---
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