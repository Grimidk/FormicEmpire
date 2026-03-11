package com.grimidk.formicempire.classes.infrasctructure.repositories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.constants.unlocks.*;

public final class GameUnlocks {
    private GameUnlocks() {}
    // --- Lists ---
    private static final List<Upgrade> upgrades = new ArrayList<>();
    private static final List<Building> buildings = new ArrayList<>();
    private static final List<Synergy> synergies = new ArrayList<>();
    private static final List<Assimilation> assimilations = new ArrayList<>();

    // --- Upgrades ---
    // -- Types --
    public static final Upgrade TYPE_EGG = new Upgrade(1, "TYPE_EGG", "TYPE_EGG_FLAVOR", "TYPE_EGG_DESC", null, 0 );
    static { upgrades.add(TYPE_EGG); }
    public static final Upgrade TYPE_WORKER = new Upgrade(2, "TYPE_WORKER", "TYPE_WORKER_FLAVOR", "TYPE_WORKER_DESC", TYPE_EGG, 0 );
    static { upgrades.add(TYPE_WORKER); }
    public static final Upgrade TYPE_SOLDIER = new Upgrade(3, "TYPE_SOLDIER", "TYPE_SOLDIER_FLAVOR", "TYPE_SOLDIER_DESC", TYPE_EGG, 100 );
    static { upgrades.add(TYPE_SOLDIER); }  
    public static final Upgrade TYPE_MAJOR = new Upgrade(4, "TYPE_MAJOR", "TYPE_MAJOR_FLAVOR", "TYPE_MAJOR_DESC", TYPE_SOLDIER, 0 );
    static { upgrades.add(TYPE_MAJOR); }
    public static final Upgrade TYPE_PRINCESS = new Upgrade(5, "TYPE_PRINCESS_DRONE", "TYPE_PRINCESS_FLAVOR", "TYPE_PRINCESS_DESC", TYPE_EGG, 3000 );
    static { upgrades.add(TYPE_PRINCESS); }
    public static final Upgrade TYPE_QUEEN = new Upgrade(6, "TYPE_QUEEN", "TYPE_QUEEN_FLAVOR", "TYPE_QUEEN_DESC", TYPE_PRINCESS, 0 );
    static { upgrades.add(TYPE_QUEEN); }
    
    // -- Roles --
    public static final Upgrade ROLE_FORAGER = new Upgrade(7, "ROLE_FORAGER_UPGRADE", "ROLE_FORAGER_FLAVOR", "ROLE_FORAGER_DESC", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_FORAGER); }
    public static final Upgrade ROLE_NURSE = new Upgrade(8, "ROLE_NURSE_UPGRADE", "ROLE_NURSE_FLAVOR", "ROLE_NURSE_DESC", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_NURSE); }
    public static final Upgrade ROLE_FARMER = new Upgrade(9, "ROLE_FARMER_UPGRADE", "ROLE_FARMER_FLAVOR", "ROLE_FARMER_DESC", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_FARMER); }
    public static final Upgrade ROLE_GRAVER = new Upgrade(10, "ROLE_GRAVER_UPGRADE", "ROLE_GRAVER_FLAVOR", "ROLE_GRAVER_DESC", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_GRAVER); }
    public static final Upgrade ROLE_HUNTER = new Upgrade(11, "ROLE_HUNTER_UPGRADE", "ROLE_HUNTER_FLAVOR", "ROLE_HUNTER_DESC", TYPE_SOLDIER, 0 );
    static { upgrades.add(ROLE_HUNTER); }
    public static final Upgrade ROLE_LAYER = new Upgrade(12, "ROLE_LAYER_UPGRADE", "ROLE_LAYER_FLAVOR", "ROLE_LAYER_DESC", TYPE_QUEEN, 0 );
    static { upgrades.add(ROLE_LAYER); }
    public static final Upgrade ROLE_RANCHER = new Upgrade(13, "ROLE_RANCHER_UPGRADE", "ROLE_RANCHER_FLAVOR", "ROLE_RANCHER_DESC", ROLE_FORAGER, 500 );
    static { upgrades.add(ROLE_RANCHER); }
    public static final Upgrade ROLE_RESEARCHER = new Upgrade(14, "ROLE_RESEARCHER_UPGRADE", "ROLE_RESEARCHER_FLAVOR", "ROLE_RESEARCHER_DESC", TYPE_QUEEN, 0 );
    static { upgrades.add(ROLE_RESEARCHER); }
    public static final Upgrade ROLE_BUILDER = new Upgrade(15, "ROLE_BUILDER_UPGRADE", "ROLE_BUILDER_FLAVOR", "ROLE_BUILDER_DESC", TYPE_WORKER, 250 );
    static { upgrades.add(ROLE_BUILDER); }
    public static final Upgrade ROLE_SCOUT = new Upgrade(16, "ROLE_SCOUT_UPGRADE", "ROLE_SCOUT_FLAVOR", "ROLE_SCOUT_DESC", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_SCOUT); }
    public static final Upgrade ROLE_MINER = new Upgrade(17, "ROLE_MINER_UPGRADE", "ROLE_MINER_FLAVOR", "ROLE_MINER_DESC", TYPE_WORKER, 0 );     
    static { upgrades.add(ROLE_MINER); }
    public static final Upgrade ROLE_POTTER = new Upgrade(19, "ROLE_POTTER_UPGRADE", "ROLE_POTTER_FLAVOR", "ROLE_POTTER_DESC", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_POTTER); }
    public static final Upgrade ROLE_GUARD = new Upgrade(20, "ROLE_GUARD_UPGRADE", "ROLE_GUARD_FLAVOR", "ROLE_GUARD_DESC", TYPE_SOLDIER, 0 );
    static { upgrades.add(ROLE_GUARD); }
    public static final Upgrade ROLE_WARRIOR = new Upgrade(21, "ROLE_WARRIOR_UPGRADE", "ROLE_WARRIOR_FLAVOR", "ROLE_WARRIOR_DESC", TYPE_SOLDIER, 0 );
    static { upgrades.add(ROLE_WARRIOR); }
    public static final Upgrade ROLE_DEFENDER = new Upgrade(22, "ROLE_DEFENDER_UPGRADE", "ROLE_DEFENDER_FLAVOR", "ROLE_DEFENDER_DESC", TYPE_SOLDIER, 0 );
    static { upgrades.add(ROLE_DEFENDER); }
    public static final Upgrade ROLE_POLICE = new Upgrade(23, "ROLE_POLICE_UPGRADE", "ROLE_POLICE_FLAVOR", "ROLE_POLICE_DESC", TYPE_SOLDIER, 0 ); 
    static { upgrades.add(ROLE_POLICE); }
    public static final Upgrade ROLE_BOMBER = new Upgrade(24, "ROLE_BOMBER_UPGRADE", "ROLE_BOMBER_FLAVOR", "ROLE_BOMBER_DESC", TYPE_SOLDIER, 0 );
    static { upgrades.add(ROLE_BOMBER); }
    public static final Upgrade ROLE_BRUTE = new Upgrade(25, "ROLE_BRUTE_UPGRADE", "ROLE_BRUTE_FLAVOR", "ROLE_BRUTE_DESC", TYPE_MAJOR, 0 );
    static { upgrades.add(ROLE_BRUTE); }
    public static final Upgrade ROLE_CARRIER = new Upgrade(26, "ROLE_CARRIER_UPGRADE", "ROLE_CARRIER_FLAVOR", "ROLE_CARRIER_DESC", TYPE_MAJOR, 0 );
    static { upgrades.add(ROLE_CARRIER); }
    public static final Upgrade ROLE_ARTILLERY = new Upgrade(27, "ROLE_ARTILLERY_UPGRADE", "ROLE_ARTILLERY_FLAVOR", "ROLE_ARTILLERY_DESC", TYPE_MAJOR, 0 );
    static { upgrades.add(ROLE_ARTILLERY); }
    public static final Upgrade ROLE_SIEGE = new Upgrade(28, "ROLE_SIEGE_UPGRADE", "ROLE_SIEGE_FLAVOR", "ROLE_SIEGE_DESC", TYPE_MAJOR, 0 );
    static { upgrades.add(ROLE_SIEGE); }
    public static final Upgrade ROLE_BREEDER = new Upgrade(30,"ROLE_BREEDER_UPGRADE", "ROLE_BREEDER_FLAVOR", "ROLE_BREEDER_DESC", TYPE_PRINCESS, 0 );
    static { upgrades.add(ROLE_BREEDER); }
    public static final Upgrade ROLE_DIPLOMAT = new Upgrade(31, "ROLE_DIPLOMAT_UPGRADE", "ROLE_DIPLOMAT_FLAVOR", "ROLE_DIPLOMAT_DESC", TYPE_PRINCESS, 0 );
    static { upgrades.add(ROLE_DIPLOMAT); }
    public static final Upgrade ROLE_MILITIA = new Upgrade(32, "ROLE_MILITIA_UPGRADE", "ROLE_MILITIA_FLAVOR", "ROLE_MILITIA_DESC", TYPE_WORKER, 0 );
    static { upgrades.add(ROLE_MILITIA); }
    public static final Upgrade ROLE_CATCHER = new Upgrade(33, "ROLE_CATCHER_UPGRADE", "ROLE_CATCHER_FLAVOR", "ROLE_CATCHER_DESC", TYPE_SOLDIER, 0 );
    static { upgrades.add(ROLE_CATCHER); }
    public static final Upgrade ROLE_CRANE = new Upgrade(34, "ROLE_CRANE_UPGRADE", "ROLE_CRANE_FLAVOR", "ROLE_CRANE_DESC", TYPE_MAJOR, 250000 );
    static { upgrades.add(ROLE_CRANE); }
    public static final Upgrade ROLE_ASSISTANT = new Upgrade(36, "ROLE_ASSISTANT_UPGRADE", "ROLE_ASSISTANT_FLAVOR", "ROLE_ASSISTANT_DESC", TYPE_PRINCESS, 1500 );
    static { upgrades.add(ROLE_ASSISTANT); }

    // -- Abilities --
    public static final Upgrade ABILITY_RESEARCH = new Upgrade(201, "ABILITY_RESEARCH", "ABILITY_RESEARCH_FLAVOR", "ABILITY_RESEARCH_DESC", ROLE_RESEARCHER, 0 );
    static { upgrades.add(ABILITY_RESEARCH); }
    public static final Upgrade ABILITY_BUILD = new Upgrade(202, "ABILITY_BUILD", "ABILITY_BUILD_FLAVOR", "ABILITY_BUILD_DESC", ROLE_BUILDER, 0 );
    static { upgrades.add(ABILITY_BUILD); }
    public static final Upgrade ABILITY_SPREAD = new Upgrade(203, "ABILITY_SPREAD", "ABILITY_SPREAD_FLAVOR", "ABILITY_SPREAD_DESC", ROLE_BREEDER, 0 );
    static { upgrades.add(ABILITY_SPREAD); }
    public static final Upgrade ABILITY_RESIN = new Upgrade(204, "ABILITY_RESIN", "ABILITY_RESIN_FLAVOR", "ABILITY_RESIN_DESC", ROLE_FORAGER, 5000 );
    static { upgrades.add(ABILITY_RESIN); }
    public static final Upgrade ABILITY_SYNERGY = new Upgrade(205, "ABILITY_SYNERGY", "ABILITY_SYNERGY_FLAVOR", "ABILITY_SYNERGY_DESC", ABILITY_RESEARCH, 0);
    static { upgrades.add(ABILITY_SYNERGY); }
    public static final Upgrade ABILITY_ASSIMILATION = new Upgrade(206, "ABILITY_ASSIMILATION", "ABILITY_ASSIMILATION_FLAVOR", "ABILITY_ASSIMILATION_DESC", ABILITY_RESEARCH, 0);
    static { upgrades.add(ABILITY_ASSIMILATION); }
    public static final Upgrade ABILITY_FORCED_FLIGHT = new Upgrade(505, "ABILITY_FORCED_FLIGHT", "ABILITY_FORCED_FLIGHT_FLAVOR", "ABILITY_FORCED_FLIGHT_DESC", ROLE_BREEDER, 9000);
    static { upgrades.add(ABILITY_FORCED_FLIGHT); }
    public static final Upgrade ABILITY_DYNASTY = new Upgrade(506, "ABILITY_DYNASTY", "ABILITY_DYNASTY_FLAVOR", "ABILITY_DYNASTY_DESC", ABILITY_SPREAD, 0);
    static { upgrades.add(ABILITY_DYNASTY); }
    public static final Upgrade ABILITY_TRADE = new Upgrade(507, "ABILITY_TRADE", "ABILITY_TRADE_FLAVOR", "ABILITY_TRADE_DESC", ABILITY_DYNASTY, 0);
    static { upgrades.add(ABILITY_TRADE); }
    public static final Upgrade ABILITY_SPREAD_2 = new Upgrade(508, "ABILITY_SPREAD_2", "ABILITY_SPREAD_2_FLAVOR", "ABILITY_SPREAD_2_DESC", ABILITY_SPREAD, 0);
    static { upgrades.add(ABILITY_SPREAD_2); }
    public static final Upgrade ABILITY_AUTOMATION = new Upgrade(509, "ABILITY_AUTOMATION", "ABILITY_AUTOMATION_FLAVOR", "ABILITY_AUTOMATION_DESC", ABILITY_DYNASTY, 0);
    static { upgrades.add(ABILITY_AUTOMATION); }
    public static final Upgrade ABILITY_TUNNELS =  new Upgrade(510, "ABILITY_TUNNELS", "ABILITY_TUNNELS_FLAVOR", "ABILITY_TUNNELS_DESC", ABILITY_TRADE, 100000);    
    static { upgrades.add(ABILITY_TUNNELS); }
    public static final Upgrade ABILITY_BILATERAL_TRADE = new Upgrade(513, "ABILITY_BILATERAL_TRADE", "ABILITY_BILATERAL_TRADE_FLAVOR", "ABILITY_BILATERAL_TRADE_DESC", ABILITY_TRADE, 0);
    static { upgrades.add(ABILITY_BILATERAL_TRADE); }
    public static final Upgrade ABILITY_MANAGEMENT = new Upgrade(511, "ABILITY_MANAGEMENT", "ABILITY_MANAGEMENT_FLAVOR", "ABILITY_MANAGEMENT_DESC", ABILITY_DYNASTY, 0);
    static { upgrades.add(ABILITY_MANAGEMENT); }
    public static final Upgrade ABILITY_MASS_FLIGHT = new Upgrade(512, "ABILITY_MASS_FLIGHT", "ABILITY_MASS_FLIGHT_FLAVOR", "ABILITY_MASS_FLIGHT_DESC", ABILITY_FORCED_FLIGHT, 0);
    static { upgrades.add(ABILITY_MASS_FLIGHT); }
    public static final Upgrade ABILITY_ABILITY = new Upgrade(900, "ABILITY_ABILITY", "ABILITY_ABILITY_FLAVOR", "ABILITY_ABILITY_DESC", null, 0);
    static { upgrades.add(ABILITY_ABILITY); }

    // -- Advanced Roles --
    public static final Upgrade ROLE_COURIER = new Upgrade(18, "ROLE_COURIER_UPGRADE", "ROLE_COURIER_FLAVOR", "ROLE_COURIER_DESC", ABILITY_TRADE, 0 );
    static { upgrades.add(ROLE_COURIER); }
    public static final Upgrade ROLE_BORER = new Upgrade(29, "ROLE_BORER_UPGRADE", "ROLE_BORER_FLAVOR", "ROLE_BORER_DESC", ABILITY_TUNNELS, 0 );
    static { upgrades.add(ROLE_BORER); }
    public static final Upgrade ROLE_TRANSPORT = new Upgrade(35, "ROLE_TRANSPORT_UPGRADE", "ROLE_TRANSPORT_FLAVOR", "ROLE_TRANSPORT_DESC", ROLE_COURIER, 100000 );
    static { upgrades.add(ROLE_TRANSPORT); }
    public static final Upgrade ROLE_ESCORT = new Upgrade(37, "ROLE_ESCORT_UPGRADE", "ROLE_ESCORT_FLAVOR", "ROLE_ESCORT_DESC", ROLE_COURIER, 80000 );
    static { upgrades.add(ROLE_ESCORT); }
    public static final Upgrade ROLE_ENGINEER = new Upgrade(38, "ROLE_ENGINEER_UPGRADE", "ROLE_ENGINEER_FLAVOR", "ROLE_ENGINEER_DESC", ROLE_BORER, 50000 );
    static { upgrades.add(ROLE_ENGINEER); }
    public static final Upgrade ROLE_SKYTRANS = new Upgrade(39, "ROLE_SKYTRANS_UPGRADE", "ROLE_SKYTRANS_FLAVOR", "ROLE_SKYTRANS_DESC", ROLE_COURIER, 150000 );
    static { upgrades.add(ROLE_SKYTRANS); }

    // -- Stats -- 
    public static final Upgrade STAT_SKELETON = new Upgrade(101, "STAT_SKELETON", "STAT_SKELETON_FLAVOR", "STAT_SKELETON_DESC", TYPE_EGG, 0 );
    static { upgrades.add(STAT_SKELETON); }
    public static final Upgrade STAT_ACID = new Upgrade(102, "STAT_ACID", "STAT_ACID_FLAVOR", "STAT_ACID_DESC", TYPE_EGG, 0 );
    static { upgrades.add(STAT_ACID); }
    public static final Upgrade STAT_LONGEVITY = new Upgrade(103, "STAT_LONGEVITY", "STAT_LONGEVITY_FLAVOR", "STAT_LONGEVITY_DESC", TYPE_EGG, 0 );
    static { upgrades.add(STAT_LONGEVITY); }
    public static final Upgrade STAT_RESEARCH_1 = new Upgrade(104, "STAT_RESEARCH_1", "STAT_RESEARCH_1_FLAVOR", "STAT_RESEARCH_1_DESC", ROLE_RESEARCHER, 800 );
    static { upgrades.add(STAT_RESEARCH_1); }
    public static final Upgrade STAT_RESEARCH_2 = new Upgrade(105, "STAT_RESEARCH_2", "STAT_RESEARCH_2_FLAVOR", "STAT_RESEARCH_2_DESC", STAT_RESEARCH_1, 2500 );
    static { upgrades.add(STAT_RESEARCH_2); }
    public static final Upgrade STAT_RESEARCH_3 = new Upgrade(106, "STAT_RESEARCH_3", "STAT_RESEARCH_3_FLAVOR", "STAT_RESEARCH_3_DESC", STAT_RESEARCH_2, 12000 );
    static { upgrades.add(STAT_RESEARCH_3); }
    public static final Upgrade STAT_GROWTH_1 = new Upgrade(107, "STAT_GROWTH_1", "STAT_GROWTH_1_FLAVOR", "STAT_GROWTH_1_DESC", TYPE_EGG, 3500 );
    static { upgrades.add(STAT_GROWTH_1); }
    public static final Upgrade STAT_GROWTH_2 = new Upgrade(108, "STAT_GROWTH_2", "STAT_GROWTH_2_FLAVOR", "STAT_GROWTH_2_DESC", STAT_GROWTH_1, 9000 );
    static { upgrades.add(STAT_GROWTH_2); }
    public static final Upgrade STAT_GROWTH_3 = new Upgrade(109, "STAT_GROWTH_3", "STAT_GROWTH_3_FLAVOR", "STAT_GROWTH_3_DESC", STAT_GROWTH_2, 20000 );
    static { upgrades.add(STAT_GROWTH_3); }
    public static final Upgrade STAT_THIRST_1 = new Upgrade(110, "STAT_THIRST_1", "STAT_THIRST_1_FLAVOR", "STAT_THIRST_1_DESC", STAT_LONGEVITY, 1500 );
    static { upgrades.add(STAT_THIRST_1); }
    public static final Upgrade STAT_THIRST_2 = new Upgrade(111, "STAT_THIRST_2", "STAT_THIRST_2_FLAVOR", "STAT_THIRST_2_DESC", STAT_THIRST_1, 5000 );
    static { upgrades.add(STAT_THIRST_2); }
    public static final Upgrade STAT_THIRST_3 = new Upgrade(112, "STAT_THIRST_3", "STAT_THIRST_3_FLAVOR", "STAT_THIRST_3_DESC", STAT_THIRST_2, 15000 );
    static { upgrades.add(STAT_THIRST_3); }
    public static final Upgrade STAT_LOGISTICS_1 = new Upgrade(113, "STAT_LOGISTICS_1", "STAT_LOGISTICS_1_FLAVOR", "STAT_LOGISTICS_1_DESC", ROLE_SCOUT, 600);
    static { upgrades.add(STAT_LOGISTICS_1); }
    public static final Upgrade STAT_PASSIVE_1 = new Upgrade(114, "STAT_PASSIVE_1", "STAT_PASSIVE_1_FLAVOR", "STAT_PASSIVE_1_DESC", STAT_RESEARCH_3, 25000);
    static { upgrades.add(STAT_PASSIVE_1); }
    public static final Upgrade STAT_LAYING_1 = new Upgrade(115, "STAT_LAYING_1", "STAT_LAYING_1_FLAVOR", "STAT_LAYING_1_DESC", ROLE_LAYER, 5000);
    static { upgrades.add(STAT_LAYING_1); }
    public static final Upgrade STAT_LAYING_2 = new Upgrade(116, "STAT_LAYING_2", "STAT_LAYING_2_FLAVOR", "STAT_LAYING_2_DESC", STAT_LAYING_1, 15000);
    static { upgrades.add(STAT_LAYING_2); }
    public static final Upgrade STAT_LAYING_3 = new Upgrade(117, "STAT_LAYING_3", "STAT_LAYING_3_FLAVOR", "STAT_LAYING_3_DESC", STAT_LAYING_2, 80000);
    static { upgrades.add(STAT_LAYING_3); }
    public static final Upgrade STAT_SCOUTING_1 = new Upgrade(118, "STAT_SCOUTING_1", "STAT_SCOUTING_1_FLAVOR", "STAT_SCOUTING_1_DESC", ROLE_SCOUT, 4000);
    static { upgrades.add(STAT_SCOUTING_1); }
    public static final Upgrade STAT_SCOUTING_2 = new Upgrade(119, "STAT_SCOUTING_2", "STAT_SCOUTING_2_FLAVOR", "STAT_SCOUTING_2_DESC", STAT_SCOUTING_1, 15000);
    static { upgrades.add(STAT_SCOUTING_2); }
    public static final Upgrade STAT_SCOUTING_3 = new Upgrade(120, "STAT_SCOUTING_3", "STAT_SCOUTING_3_FLAVOR", "STAT_SCOUTING_3_DESC", STAT_SCOUTING_2, 35000);
    static { upgrades.add(STAT_SCOUTING_3); }
    public static final Upgrade STAT_FARMING_1 = new Upgrade(121, "STAT_FARMING_1", "STAT_FARMING_1_FLAVOR", "STAT_FARMING_1_DESC", ROLE_FARMER, 4500);
    static { upgrades.add(STAT_FARMING_1); }
    public static final Upgrade STAT_FARMING_2 = new Upgrade(122, "STAT_FARMING_2", "STAT_FARMING_2_FLAVOR", "STAT_FARMING_2_DESC", STAT_FARMING_1, 16000);
    static { upgrades.add(STAT_FARMING_2); }
    public static final Upgrade STAT_FARMING_3 = new Upgrade(123, "STAT_FARMING_3", "STAT_FARMING_3_FLAVOR", "STAT_FARMING_3_DESC", STAT_FARMING_2, 60000);
    static { upgrades.add(STAT_FARMING_3); }
    public static final Upgrade STAT_CONTAMINATION_1 = new Upgrade(124, "STAT_CONTAMINATION_1", "STAT_CONTAMINATION_1_FLAVOR", "STAT_CONTAMINATION_1_DESC", ROLE_GRAVER, 2000);
    static { upgrades.add(STAT_CONTAMINATION_1); }
    public static final Upgrade STAT_CONTAMINATION_2 = new Upgrade(125, "STAT_CONTAMINATION_2", "STAT_CONTAMINATION_2_FLAVOR", "STAT_CONTAMINATION_2_DESC", STAT_CONTAMINATION_1, 8000);
    static { upgrades.add(STAT_CONTAMINATION_2); }
    public static final Upgrade STAT_CONTAMINATION_3 = new Upgrade(126, "STAT_CONTAMINATION_3", "STAT_CONTAMINATION_3_FLAVOR", "STAT_CONTAMINATION_3_DESC", STAT_CONTAMINATION_2, 25000);
    static { upgrades.add(STAT_CONTAMINATION_3); }
    public static final Upgrade STAT_GRAVING_1 = new Upgrade(127, "STAT_GRAVING_1", "STAT_GRAVING_1_FLAVOR", "STAT_GRAVING_1_DESC", ROLE_GRAVER, 1500);
    static { upgrades.add(STAT_GRAVING_1); }
    public static final Upgrade STAT_GRAVING_2 = new Upgrade(128, "STAT_GRAVING_2", "STAT_GRAVING_2_FLAVOR", "STAT_GRAVING_2_DESC", STAT_GRAVING_1, 6500);
    static { upgrades.add(STAT_GRAVING_2); }
    public static final Upgrade STAT_GRAVING_3 = new Upgrade(129, "STAT_GRAVING_3", "STAT_GRAVING_3_FLAVOR", "STAT_GRAVING_3_DESC", STAT_GRAVING_2, 40000);
    static { upgrades.add(STAT_GRAVING_3); }
    public static final Upgrade STAT_POLICING_1 = new Upgrade(130, "STAT_POLICING_1", "STAT_POLICING_1_FLAVOR", "STAT_POLICING_1_DESC", ROLE_POLICE, 3000);
    static { upgrades.add(STAT_POLICING_1); }
    public static final Upgrade STAT_POLICING_2 = new Upgrade(131, "STAT_POLICING_2", "STAT_POLICING_2_FLAVOR", "STAT_POLICING_2_DESC", STAT_POLICING_1, 14000);
    static { upgrades.add(STAT_POLICING_2); }
    public static final Upgrade STAT_POLICING_3 = new Upgrade(132, "STAT_POLICING_3", "STAT_POLICING_3_FLAVOR", "STAT_POLICING_3_DESC", STAT_POLICING_2, 42000);
    static { upgrades.add(STAT_POLICING_3); }
    // -- Assimilated --
    public static final Upgrade ASSIMILATED_FARMING = new Upgrade(1001, "ASSIMILATED_FARMING", "ASSIMILATED_FARMING_FLAVOR", "ASSIMILATED_FARMING_DESC", ABILITY_ASSIMILATION, 0 );
    static { upgrades.add(ASSIMILATED_FARMING); }
    public static final Upgrade ASSIMILATED_MULTIQUEEN = new Upgrade(1002, "ASSIMILATED_MULTIQUEEN", "ASSIMILATED_MULTIQUEEN_FLAVOR", "ASSIMILATED_MULTIQUEEN_DESC", ABILITY_ASSIMILATION, 0 );
    static { upgrades.add(ASSIMILATED_MULTIQUEEN); }

    // --- Buildings ---
    // -- Tier 0 --
    public static final Building ROYAL_CHAMBER_0 = new Building(1, "ROYAL_CHAMBER_0", 0, "ROYAL_CHAMBER_0_DESC", null, 0, 0, 0);
    static { buildings.add(ROYAL_CHAMBER_0); }
    public static final Building EGG_CHAMBER_0 = new Building(2, "EGG_CHAMBER_0", 0, "EGG_CHAMBER_0_DESC", null, 0, 0, 0);
    static { buildings.add(EGG_CHAMBER_0); }
    public static final Building MUSHROOM_CHAMBER_0 = new Building(3, "MUSHROOM_CHAMBER_0", 0, "MUSHROOM_CHAMBER_0_DESC", null, 0, 0, 0);
    static { buildings.add(MUSHROOM_CHAMBER_0); }
    public static final Building PLANT_CHAMBER_0 = new Building(4, "PLANT_CHAMBER_0", 0, "PLANT_CHAMBER_0_DESC", null, 0, 0, 0);
    static { buildings.add(PLANT_CHAMBER_0); }
    public static final Building WATER_RESERVOIR_0 = new Building(5, "WATER_RESERVOIR_0", 0, "WATER_RESERVOIR_0_DESC", null, 0, 0, 0);
    static { buildings.add(WATER_RESERVOIR_0); }
    public static final Building MEAT_CHAMBER_0 = new Building(6, "MEAT_CHAMBER_0", 0, "MEAT_CHAMBER_0_DESC", null,0, 0, 12);
    static { buildings.add(MEAT_CHAMBER_0); }
    public static final Building SYRUP_RESERVOIR_0 = new Building(7, "SYRUP_RESERVOIR_0", 0, "SYRUP_RESERVOIR_0_DESC", null, 0, 0, 24);
    static { buildings.add(SYRUP_RESERVOIR_0); }
    public static final Building ROCK_WAREHOUSE_0 = new Building(8, "ROCK_WAREHOUSE_0", 0, "ROCK_WAREHOUSE_0_DESC", null, 0, 0, 50);
    static { buildings.add(ROCK_WAREHOUSE_0); }
    public static final Building RESIN_RESERVOIR_0 = new Building(9, "RESIN_RESERVOIR_0", 0, "RESIN_RESERVOIR_0_DESC", null, 0, 0, 50);
    static { buildings.add(RESIN_RESERVOIR_0); }
    // -- Tier 1 --
    public static final Building ROYAL_CHAMBER_1 = new Building(10, "ROYAL_CHAMBER_1", 1, "ROYAL_CHAMBER_1_DESC", ROYAL_CHAMBER_0, 0, 0, 1000);
    static { buildings.add(ROYAL_CHAMBER_1); }
    public static final Building EGG_CHAMBER_1 = new Building(11, "EGG_CHAMBER_1", 1, "EGG_CHAMBER_1_DESC", EGG_CHAMBER_0, 0, 0, 100);
    static { buildings.add(EGG_CHAMBER_1); }
    public static final Building MUSHROOM_CHAMBER_1 = new Building(12, "MUSHROOM_CHAMBER_1", 1, "MUSHROOM_CHAMBER_1_DESC", MUSHROOM_CHAMBER_0, 0, 0, 150);
    static { buildings.add(MUSHROOM_CHAMBER_1); }
    public static final Building PLANT_CHAMBER_1 = new Building(13, "PLANT_CHAMBER_1", 1, "PLANT_CHAMBER_1_DESC", PLANT_CHAMBER_0, 0, 0, 180);
    static { buildings.add(PLANT_CHAMBER_1); }
    public static final Building WATER_RESERVOIR_1 = new Building(14, "WATER_RESERVOIR_1", 1, "WATER_RESERVOIR_1_DESC", WATER_RESERVOIR_0, 0, 0, 250);
    static { buildings.add(WATER_RESERVOIR_1); }
    public static final Building MEAT_CHAMBER_1 = new Building(15, "MEAT_CHAMBER_1", 1, "MEAT_CHAMBER_1_DESC", MEAT_CHAMBER_0, 0, 0, 200);
    static { buildings.add(MEAT_CHAMBER_1); }
    public static final Building SYRUP_RESERVOIR_1 = new Building(16, "SYRUP_RESERVOIR_1", 1, "SYRUP_RESERVOIR_1_DESC", SYRUP_RESERVOIR_0, 0, 0, 250);
    static { buildings.add(SYRUP_RESERVOIR_1); }
    public static final Building ROCK_WAREHOUSE_1 = new Building(17, "ROCK_WAREHOUSE_1", 1, "ROCK_WAREHOUSE_1_DESC", ROCK_WAREHOUSE_0, 0, 0, 500);
    static { buildings.add(ROCK_WAREHOUSE_1); }
    public static final Building RESIN_RESERVOIR_1 = new Building(18, "RESIN_RESERVOIR_1", 1, "RESIN_RESERVOIR_1_DESC", RESIN_RESERVOIR_0, 0, 0, 400);
    static { buildings.add(RESIN_RESERVOIR_1); }
    // -- Tier 2 --
    public static final Building ROYAL_CHAMBER_2 = new Building(19, "ROYAL_CHAMBER_2", 2, "ROYAL_CHAMBER_2_DESC", ROYAL_CHAMBER_1, 500, 0, 3000);
    static { buildings.add(ROYAL_CHAMBER_2); }
    public static final Building EGG_CHAMBER_2 = new Building(20, "EGG_CHAMBER_2", 2, "EGG_CHAMBER_2_DESC", EGG_CHAMBER_1, 200, 0, 1000);
    static { buildings.add(EGG_CHAMBER_2); }
    public static final Building MUSHROOM_CHAMBER_2 = new Building(21, "MUSHROOM_CHAMBER_2", 2, "MUSHROOM_CHAMBER_2_DESC", MUSHROOM_CHAMBER_1, 250, 0, 800);
    static { buildings.add(MUSHROOM_CHAMBER_2); }
    public static final Building PLANT_CHAMBER_2 = new Building(22, "PLANT_CHAMBER_2", 2, "PLANT_CHAMBER_2_DESC", PLANT_CHAMBER_1, 200, 0, 1000);
    static { buildings.add(PLANT_CHAMBER_2); }
    public static final Building WATER_RESERVOIR_2 = new Building(23, "WATER_RESERVOIR_2", 2, "WATER_RESERVOIR_2_DESC", WATER_RESERVOIR_1, 300, 0, 1200);
    static { buildings.add(WATER_RESERVOIR_2); }
    public static final Building MEAT_CHAMBER_2 = new Building(24, "MEAT_CHAMBER_2", 2, "MEAT_CHAMBER_2_DESC", MEAT_CHAMBER_1, 350, 0, 1500);
    static { buildings.add(MEAT_CHAMBER_2); }
    public static final Building SYRUP_RESERVOIR_2 = new Building(25, "SYRUP_RESERVOIR_2", 2, "SYRUP_RESERVOIR_2_DESC", SYRUP_RESERVOIR_1, 350, 0, 1500);
    static { buildings.add(SYRUP_RESERVOIR_2); }
    public static final Building ROCK_WAREHOUSE_2 = new Building(26, "ROCK_WAREHOUSE_2", 2, "ROCK_WAREHOUSE_2_DESC", ROCK_WAREHOUSE_1, 450, 0, 2500);
    static { buildings.add(ROCK_WAREHOUSE_2); }
    public static final Building RESIN_RESERVOIR_2 = new Building(27, "RESIN_RESERVOIR_2", 2, "RESIN_RESERVOIR_2_DESC", RESIN_RESERVOIR_1, 400, 0, 2000);
    static { buildings.add(RESIN_RESERVOIR_2); }
    // -- Tier 3 --
    public static final Building ROYAL_CHAMBER_3 = new Building(28, "ROYAL_CHAMBER_3", 3, "ROYAL_CHAMBER_3_DESC", ROYAL_CHAMBER_2, 1000, 0, 15000);
    static { buildings.add(ROYAL_CHAMBER_3); }
    public static final Building EGG_CHAMBER_3 = new Building(29, "EGG_CHAMBER_3", 3, "EGG_CHAMBER_3_DESC", EGG_CHAMBER_2, 500, 0, 5000);
    static { buildings.add(EGG_CHAMBER_3); }
    public static final Building MUSHROOM_CHAMBER_3 = new Building(30, "MUSHROOM_CHAMBER_3", 3, "MUSHROOM_CHAMBER_3_DESC", MUSHROOM_CHAMBER_2, 800, 0, 5000);
    static { buildings.add(MUSHROOM_CHAMBER_3); }   
    public static final Building PLANT_CHAMBER_3 = new Building(31, "PLANT_CHAMBER_3", 3, "PLANT_CHAMBER_3_DESC", PLANT_CHAMBER_2, 700, 0, 5000);
    static { buildings.add(PLANT_CHAMBER_3); }
    public static final Building WATER_RESERVOIR_3 = new Building(32, "WATER_RESERVOIR_3", 3, "WATER_RESERVOIR_3_DESC", WATER_RESERVOIR_2, 900, 0, 5000);
    static { buildings.add(WATER_RESERVOIR_3); }
    public static final Building MEAT_CHAMBER_3 = new Building(33, "MEAT_CHAMBER_3", 3, "MEAT_CHAMBER_3_DESC", MEAT_CHAMBER_2, 850, 0, 10000);
    static { buildings.add(MEAT_CHAMBER_3); }
    public static final Building SYRUP_RESERVOIR_3 = new Building(34, "SYRUP_RESERVOIR_3", 3, "SYRUP_RESERVOIR_3_DESC", SYRUP_RESERVOIR_2, 800, 0, 10000);
    static { buildings.add(SYRUP_RESERVOIR_3); }
    public static final Building ROCK_WAREHOUSE_3 = new Building(35, "ROCK_WAREHOUSE_3", 3, "ROCK_WAREHOUSE_3_DESC", ROCK_WAREHOUSE_2, 950, 0, 10000);
    static { buildings.add(ROCK_WAREHOUSE_3); }
    public static final Building RESIN_RESERVOIR_3 = new Building(36, "RESIN_RESERVOIR_3", 3, "RESIN_RESERVOIR_3_DESC", RESIN_RESERVOIR_2, 900, 0, 10000);
    static { buildings.add(RESIN_RESERVOIR_3); }
    // -- Tier 4 --
    // -- Tier 5 --
    // -- Tier Misc. --
    public static final Building PASSIVE_LAB = new Building(101, "PASSIVE_LAB", 0, "PASSIVE_LAB_DESC", ROYAL_CHAMBER_1, 150, 0, 800);
    static { buildings.add(PASSIVE_LAB); }
    public static final Building PASSIVE_WATER = new Building(102, "PASSIVE_WATER", 0, "PASSIVE_WATER_DESC", WATER_RESERVOIR_1, 100, 0, 500);
    static { buildings.add(PASSIVE_WATER); }
    public static final Building PASSIVE_APHID = new Building(103, "PASSIVE_APHID", 0, "PASSIVE_APHID_DESC", SYRUP_RESERVOIR_1, 200, 0, 1000);
    static { buildings.add(PASSIVE_APHID); }
    public static final Building PASSIVE_NURSE = new Building(104, "PASSIVE_NURSE", 0, "PASSIVE_NURSE_DESC", EGG_CHAMBER_1, 150, 0, 700);
    static { buildings.add(PASSIVE_NURSE); }
    public static final Building PASSIVE_FARM = new Building(105, "PASSIVE_FARM", 0, "PASSIVE_FARM_DESC", MUSHROOM_CHAMBER_1, 200, 0, 900);
    static { buildings.add(PASSIVE_FARM); }
    public static final Building PASSIVE_GRAVE = new Building(106, "PASSIVE_GRAVE", 0, "PASSIVE_GRAVE_DESC", MEAT_CHAMBER_1, 150, 0, 600);
    static { buildings.add(PASSIVE_GRAVE); }
    public static final Building BUILDING_COMPOSTER = new Building(107, "BUILDING_COMPOSTER", 0, "BUILDING_COMPOSTER_DESC", PASSIVE_GRAVE, 500, 0, 1000);
    static { buildings.add(BUILDING_COMPOSTER); }

    // --- Assimilations ---
    public static final Assimilation ASSIMILATION_LEAFCUTTER = new Assimilation(1, "ASSIMILATION_LEAFCUTTER", "ASSIMILATION_LEAFCUTTER_DESC", ASSIMILATED_FARMING, 5000);
    static { assimilations.add(ASSIMILATION_LEAFCUTTER); }
    public static final Assimilation ASSIMILATION_PHARAOH = new Assimilation(2, "ASSIMILATION_PHARAOH", "ASSIMILATION_PHARAOH_DESC", ASSIMILATED_MULTIQUEEN, 10000);
    static { assimilations.add(ASSIMILATION_PHARAOH); }
    public static final Assimilation ASSIMILATION_MARAUDER = new Assimilation(3, "ASSIMILATION_MARAUDER", "ASSIMILATION_MARAUDER_DESC", TYPE_MAJOR, 15000);
    static { assimilations.add(ASSIMILATION_MARAUDER); }

    // --- Synergies ---

    // --- Getters ---
    public static List<Upgrade> getUpgrades() { return Collections.unmodifiableList(upgrades); }

    public static List<Building> getBuildings() { return Collections.unmodifiableList(buildings); }

    public static List<Synergy> getSynergies() { return Collections.unmodifiableList(synergies); }

    public static List<Assimilation> getAssimilations() { return Collections.unmodifiableList(assimilations); }
}
