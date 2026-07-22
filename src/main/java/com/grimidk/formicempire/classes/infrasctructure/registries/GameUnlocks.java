package com.grimidk.formicempire.classes.infrasctructure.registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.constants.unlocks.*;
import com.grimidk.formicempire.classes.entities.Dynasty;

public final class GameUnlocks {
        private GameUnlocks() {}
        // --- Lists ---
        private static final List<Upgrade> upgrades = new ArrayList<>();
        private static final List<Building> buildings = new ArrayList<>();
        private static final List<Synergy> synergies = new ArrayList<>();
        private static final List<Assimilation> assimilations = new ArrayList<>();

        // --- Upgrades ---
        // -- Types --
        // TODO asset: icons/types/Egg.png (upgrade tree icon)
        public static final Upgrade TYPE_EGG = new Upgrade(1, "TYPE_EGG", "TYPE_EGG_FLAVOR", "TYPE_EGG_DESC", null, 0, null, 0);
        static { upgrades.add(TYPE_EGG); }
        // TODO asset: icons/types/Worker.png (upgrade tree icon)
        public static final Upgrade TYPE_WORKER = new Upgrade(2, "TYPE_WORKER", "TYPE_WORKER_FLAVOR", "TYPE_WORKER_DESC", TYPE_EGG, 0, null, 0);
        static { upgrades.add(TYPE_WORKER); }
        // TODO asset: icons/types/Soldier.png (upgrade tree icon)
        public static final Upgrade TYPE_SOLDIER = new Upgrade(3, "TYPE_SOLDIER", "TYPE_SOLDIER_FLAVOR", "TYPE_SOLDIER_DESC", TYPE_EGG, 100, null, 1);
        static { upgrades.add(TYPE_SOLDIER); }  
        // TODO asset: icons/species/Marauder.png (upgrade tree icon)
        public static final Upgrade TYPE_MAJOR = new Upgrade(4, "TYPE_MAJOR", "TYPE_MAJOR_FLAVOR", "TYPE_MAJOR_DESC", TYPE_SOLDIER, 0, null, 0);
        static { upgrades.add(TYPE_MAJOR); }
        // TODO asset: icons/types/Princess.png (upgrade tree icon)
        public static final Upgrade TYPE_PRINCESS = new Upgrade(5, "TYPE_PRINCESS_DRONE", "TYPE_PRINCESS_FLAVOR", "TYPE_PRINCESS_DESC", TYPE_EGG, 1000, null, 2);
        static { upgrades.add(TYPE_PRINCESS); }
        // TODO asset: icons/types/Queen.png (upgrade tree icon)
        public static final Upgrade TYPE_QUEEN = new Upgrade(6, "TYPE_QUEEN", "TYPE_QUEEN_FLAVOR", "TYPE_QUEEN_DESC", TYPE_PRINCESS, 0, null, 0);
        static { upgrades.add(TYPE_QUEEN); }
        
        // -- Roles --
        // TODO asset: icons/roles/Forager.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_FORAGER = new Upgrade(7, "ROLE_FORAGER_UPGRADE", "ROLE_FORAGER_FLAVOR", "ROLE_FORAGER_DESC", TYPE_WORKER, 0, null, 0);
        static { upgrades.add(ROLE_FORAGER); }
        // TODO asset: icons/roles/Nurse.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_NURSE = new Upgrade(8, "ROLE_NURSE_UPGRADE", "ROLE_NURSE_FLAVOR", "ROLE_NURSE_DESC", TYPE_WORKER, 0, null, 0);
        static { upgrades.add(ROLE_NURSE); }
        // TODO asset: icons/roles/Farmer.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_FARMER = new Upgrade(9, "ROLE_FARMER_UPGRADE", "ROLE_FARMER_FLAVOR", "ROLE_FARMER_DESC", TYPE_WORKER, 0, null, 0);
        static { upgrades.add(ROLE_FARMER); }
        // TODO asset: icons/roles/Graver.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_GRAVER = new Upgrade(10, "ROLE_GRAVER_UPGRADE", "ROLE_GRAVER_FLAVOR", "ROLE_GRAVER_DESC", TYPE_WORKER, 0, null, 0);
        static { upgrades.add(ROLE_GRAVER); }
        // TODO asset: icons/roles/Hunter.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_HUNTER = new Upgrade(11, "ROLE_HUNTER_UPGRADE", "ROLE_HUNTER_FLAVOR", "ROLE_HUNTER_DESC", TYPE_SOLDIER, 0, null, 0);
        static { upgrades.add(ROLE_HUNTER); }
        // TODO asset: icons/roles/Layer.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_LAYER = new Upgrade(12, "ROLE_LAYER_UPGRADE", "ROLE_LAYER_FLAVOR", "ROLE_LAYER_DESC", TYPE_QUEEN, 0, null, 0);
        static { upgrades.add(ROLE_LAYER); }
        // TODO asset: icons/roles/Rancher.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_RANCHER = new Upgrade(13, "ROLE_RANCHER_UPGRADE", "ROLE_RANCHER_FLAVOR", "ROLE_RANCHER_DESC", ROLE_FORAGER, 250, null, 1);
        static { upgrades.add(ROLE_RANCHER); }
        // TODO asset: icons/roles/Researcher.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_RESEARCHER = new Upgrade(14, "ROLE_RESEARCHER_UPGRADE", "ROLE_RESEARCHER_FLAVOR", "ROLE_RESEARCHER_DESC", TYPE_QUEEN, 0, null, 0);
        static { upgrades.add(ROLE_RESEARCHER); }
        // TODO asset: icons/roles/Builder.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_BUILDER = new Upgrade(15, "ROLE_BUILDER_UPGRADE", "ROLE_BUILDER_FLAVOR", "ROLE_BUILDER_DESC", TYPE_WORKER, 150, null, 1);
        static { upgrades.add(ROLE_BUILDER); }
        // TODO asset: icons/roles/Scout.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_SCOUT = new Upgrade(16, "ROLE_SCOUT_UPGRADE", "ROLE_SCOUT_FLAVOR", "ROLE_SCOUT_DESC", TYPE_WORKER, 0, null, 0);
        static { upgrades.add(ROLE_SCOUT); }
        // TODO asset: icons/roles/Miner.png (upgrade tree icon; may diverge from role icon)
        // Not purchasable until minerals / miner unlock path is implemented (cost 0 is never offered in Research)
        public static final Upgrade ROLE_MINER = new Upgrade(17, "ROLE_MINER_UPGRADE", "ROLE_MINER_FLAVOR", "ROLE_MINER_DESC", TYPE_WORKER, 0, null, 0);
        // TODO asset: icons/roles/Potter.png (upgrade tree icon; may diverge from role icon)
        // Not purchasable until implemented
        public static final Upgrade ROLE_POTTER = new Upgrade(19, "ROLE_POTTER_UPGRADE", "ROLE_POTTER_FLAVOR", "ROLE_POTTER_DESC", TYPE_WORKER, 0, null, 0);
        // TODO asset: icons/roles/Warrior.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_WARRIOR = new Upgrade(21, "ROLE_WARRIOR_UPGRADE", "ROLE_WARRIOR_FLAVOR", "ROLE_WARRIOR_DESC", TYPE_SOLDIER, 0, null, 0);
        static { upgrades.add(ROLE_WARRIOR); }
        // TODO asset: icons/roles/Defender.png (upgrade tree icon; may diverge from role icon)
        // Not purchasable until implemented
        public static final Upgrade ROLE_DEFENDER = new Upgrade(22, "ROLE_DEFENDER_UPGRADE", "ROLE_DEFENDER_FLAVOR", "ROLE_DEFENDER_DESC", TYPE_SOLDIER, 0, null, 0);
        // TODO asset: icons/roles/Police.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_POLICE = new Upgrade(23, "ROLE_POLICE_UPGRADE", "ROLE_POLICE_FLAVOR", "ROLE_POLICE_DESC", TYPE_SOLDIER, 0, null, 0); 
        static { upgrades.add(ROLE_POLICE); }
        // TODO asset: icons/roles/Bomber.png (upgrade tree icon; may diverge from role icon)
        // Not purchasable until implemented
        public static final Upgrade ROLE_BOMBER = new Upgrade(24, "ROLE_BOMBER_UPGRADE", "ROLE_BOMBER_FLAVOR", "ROLE_BOMBER_DESC", TYPE_SOLDIER, 0, null, 0);
        // TODO asset: icons/roles/Brute.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_BRUTE = new Upgrade(25, "ROLE_BRUTE_UPGRADE", "ROLE_BRUTE_FLAVOR", "ROLE_BRUTE_DESC", TYPE_MAJOR, 0, null, 0);
        static { upgrades.add(ROLE_BRUTE); }
        // TODO asset: icons/roles/Carrier.png (upgrade tree icon; may diverge from role icon)
        // Not purchasable until implemented
        public static final Upgrade ROLE_CARRIER = new Upgrade(26, "ROLE_CARRIER_UPGRADE", "ROLE_CARRIER_FLAVOR", "ROLE_CARRIER_DESC", TYPE_MAJOR, 0, null, 0);
        // TODO asset: icons/roles/Artillery.png (upgrade tree icon; may diverge from role icon)
        // Not purchasable until implemented
        public static final Upgrade ROLE_ARTILLERY = new Upgrade(27, "ROLE_ARTILLERY_UPGRADE", "ROLE_ARTILLERY_FLAVOR", "ROLE_ARTILLERY_DESC", TYPE_MAJOR, 0, null, 0);
        // TODO asset: icons/roles/Siege.png (upgrade tree icon; may diverge from role icon)
        // Not purchasable until implemented
        public static final Upgrade ROLE_SIEGE = new Upgrade(28, "ROLE_SIEGE_UPGRADE", "ROLE_SIEGE_FLAVOR", "ROLE_SIEGE_DESC", TYPE_MAJOR, 0, null, 0);
        // TODO asset: icons/roles/Breeder.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_BREEDER = new Upgrade(30, "ROLE_BREEDER_UPGRADE", "ROLE_BREEDER_FLAVOR", "ROLE_BREEDER_DESC", TYPE_PRINCESS, 0, null, 0);
        static { upgrades.add(ROLE_BREEDER); }
        // TODO asset: icons/roles/Diplomat.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_DIPLOMAT = new Upgrade(31, "ROLE_DIPLOMAT_UPGRADE", "ROLE_DIPLOMAT_FLAVOR", "ROLE_DIPLOMAT_DESC", TYPE_PRINCESS, 1500, null, 3);
        static { upgrades.add(ROLE_DIPLOMAT); }
        // TODO asset: icons/roles/Militia.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_MILITIA = new Upgrade(32, "ROLE_MILITIA_UPGRADE", "ROLE_MILITIA_FLAVOR", "ROLE_MILITIA_DESC", TYPE_WORKER, 0, null, 0);
        static { upgrades.add(ROLE_MILITIA); }
        // TODO asset: icons/roles/Catcher.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_CATCHER = new Upgrade(33, "ROLE_CATCHER_UPGRADE", "ROLE_CATCHER_FLAVOR", "ROLE_CATCHER_DESC", ROLE_HUNTER, 500, null, 1);
        static { upgrades.add(ROLE_CATCHER); }
        // TODO asset: icons/roles/Crane.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_CRANE = new Upgrade(34, "ROLE_CRANE_UPGRADE", "ROLE_CRANE_FLAVOR", "ROLE_CRANE_DESC", TYPE_MAJOR, 250000, null, 4);
        static { upgrades.add(ROLE_CRANE); }
        // TODO asset: icons/roles/Assistant.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_ASSISTANT = new Upgrade(36, "ROLE_ASSISTANT_UPGRADE", "ROLE_ASSISTANT_FLAVOR", "ROLE_ASSISTANT_DESC", TYPE_PRINCESS, 1200, null, 3);
        static { upgrades.add(ROLE_ASSISTANT); }

        // -- Abilities --
        // TODO asset: icons/upgrades/AbilityResearch.png
        public static final Upgrade ABILITY_RESEARCH = new Upgrade(201, "ABILITY_RESEARCH", "ABILITY_RESEARCH_FLAVOR", "ABILITY_RESEARCH_DESC", ROLE_RESEARCHER, 0, null, 0);
        static { upgrades.add(ABILITY_RESEARCH); }
        // TODO asset: icons/upgrades/AbilityBuild.png
        public static final Upgrade ABILITY_BUILD = new Upgrade(202, "ABILITY_BUILD", "ABILITY_BUILD_FLAVOR", "ABILITY_BUILD_DESC", ROLE_BUILDER, 0, null, 0);
        static { upgrades.add(ABILITY_BUILD); }
        // TODO asset: icons/upgrades/AbilitySpread.png
        public static final Upgrade ABILITY_SPREAD = new Upgrade(203, "ABILITY_SPREAD", "ABILITY_SPREAD_FLAVOR", "ABILITY_SPREAD_DESC", ROLE_BREEDER, 0, null, 0);
        static { upgrades.add(ABILITY_SPREAD); }
        // TODO asset: icons/upgrades/AbilityResin.png
        public static final Upgrade ABILITY_RESIN = new Upgrade(204, "ABILITY_RESIN", "ABILITY_RESIN_FLAVOR", "ABILITY_RESIN_DESC", ROLE_FORAGER, 5000, null, 2);
        static { upgrades.add(ABILITY_RESIN); }
        // TODO asset: icons/upgrades/AbilitySynergy.png
        public static final Upgrade ABILITY_SYNERGY = new Upgrade(205, "ABILITY_SYNERGY", "ABILITY_SYNERGY_FLAVOR", "ABILITY_SYNERGY_DESC", ABILITY_RESEARCH, 0, null, 0);
        static { upgrades.add(ABILITY_SYNERGY); }
        // TODO asset: icons/upgrades/AbilityAssimilation.png
        public static final Upgrade ABILITY_ASSIMILATION = new Upgrade(206, "ABILITY_ASSIMILATION", "ABILITY_ASSIMILATION_FLAVOR", "ABILITY_ASSIMILATION_DESC", ABILITY_RESEARCH, 0, null, 0);
        static { upgrades.add(ABILITY_ASSIMILATION); }
        // TODO asset: icons/upgrades/AbilityForcedFlight.png
        public static final Upgrade ABILITY_FORCED_FLIGHT = new Upgrade(505, "ABILITY_FORCED_FLIGHT", "ABILITY_FORCED_FLIGHT_FLAVOR", "ABILITY_FORCED_FLIGHT_DESC", ROLE_BREEDER, 9000, null, 2);
        static { upgrades.add(ABILITY_FORCED_FLIGHT); }
        // TODO asset: icons/upgrades/AbilityDynasty.png
        public static final Upgrade ABILITY_DYNASTY = new Upgrade(506, "ABILITY_DYNASTY", "ABILITY_DYNASTY_FLAVOR", "ABILITY_DYNASTY_DESC", ABILITY_SPREAD, 0, null, 0);
        static { upgrades.add(ABILITY_DYNASTY); }
        // TODO asset: icons/upgrades/AbilityTrade.png
        public static final Upgrade ABILITY_TRADE = new Upgrade(507, "ABILITY_TRADE", "ABILITY_TRADE_FLAVOR", "ABILITY_TRADE_DESC", ABILITY_DYNASTY, 0, null, 0);
        static { upgrades.add(ABILITY_TRADE); }
        // TODO asset: icons/upgrades/AbilitySpread2.png
        public static final Upgrade ABILITY_SPREAD_2 = new Upgrade(508, "ABILITY_SPREAD_2", "ABILITY_SPREAD_2_FLAVOR", "ABILITY_SPREAD_2_DESC", ABILITY_SPREAD, 0, null, 0);
        static { upgrades.add(ABILITY_SPREAD_2); }
        // TODO asset: icons/upgrades/AbilityAutomation.png
        public static final Upgrade ABILITY_AUTOMATION = new Upgrade(509, "ABILITY_AUTOMATION", "ABILITY_AUTOMATION_FLAVOR", "ABILITY_AUTOMATION_DESC", ABILITY_DYNASTY, 0, null, 0);
        static { upgrades.add(ABILITY_AUTOMATION); }
        // TODO asset: icons/upgrades/AbilityTunnels.png
        public static final Upgrade ABILITY_TUNNELS = new Upgrade(510, "ABILITY_TUNNELS", "ABILITY_TUNNELS_FLAVOR", "ABILITY_TUNNELS_DESC", ABILITY_TRADE, 100000, null, 4);
        static { upgrades.add(ABILITY_TUNNELS); }
        // TODO asset: icons/upgrades/AbilityBilateralTrade.png
        public static final Upgrade ABILITY_BILATERAL_TRADE = new Upgrade(513, "ABILITY_BILATERAL_TRADE", "ABILITY_BILATERAL_TRADE_FLAVOR", "ABILITY_BILATERAL_TRADE_DESC", ABILITY_TRADE, 0, null, 0);
        static { upgrades.add(ABILITY_BILATERAL_TRADE); }
        // TODO asset: icons/upgrades/AbilityManagement.png
        public static final Upgrade ABILITY_MANAGEMENT = new Upgrade(511, "ABILITY_MANAGEMENT", "ABILITY_MANAGEMENT_FLAVOR", "ABILITY_MANAGEMENT_DESC", ABILITY_DYNASTY, 0, null, 0);
        static { upgrades.add(ABILITY_MANAGEMENT); }
        // TODO asset: icons/upgrades/AbilityMassFlight.png
        public static final Upgrade ABILITY_MASS_FLIGHT = new Upgrade(512, "ABILITY_MASS_FLIGHT", "ABILITY_MASS_FLIGHT_FLAVOR", "ABILITY_MASS_FLIGHT_DESC", ABILITY_FORCED_FLIGHT, 0, null, 0);
        static { upgrades.add(ABILITY_MASS_FLIGHT); }
        // TODO asset: icons/upgrades/AbilityCloning.png
        public static final Upgrade ABILITY_CLONING = new Upgrade(514, "ABILITY_CLONING", "ABILITY_CLONING_FLAVOR", "ABILITY_CLONING_DESC", ABILITY_RESEARCH, 0, null, 0);
        static { upgrades.add(ABILITY_CLONING); }
        // TODO asset: icons/upgrades/AbilityParasiticMiteAlert.png
        public static final Upgrade ABILITY_PARASITIC_MITE_ALERT = new Upgrade(515, "ABILITY_PARASITIC_MITE_ALERT", "ABILITY_PARASITIC_MITE_ALERT_FLAVOR", "ABILITY_PARASITIC_MITE_ALERT_DESC", null, 0, null, 0);
        static { upgrades.add(ABILITY_PARASITIC_MITE_ALERT); }
        // TODO asset: icons/upgrades/AbilityCatchSymbioticMite.png
        public static final Upgrade ABILITY_CATCH_SYMBIOTIC_MITE = new Upgrade(516, "ABILITY_CATCH_SYMBIOTIC_MITE", "ABILITY_CATCH_SYMBIOTIC_MITE_FLAVOR", "ABILITY_CATCH_SYMBIOTIC_MITE_DESC", null, 0, null, 0);
        static { upgrades.add(ABILITY_CATCH_SYMBIOTIC_MITE); }
        // TODO asset: icons/upgrades/AbilityCatchDermestid.png
        public static final Upgrade ABILITY_CATCH_DERMESTID = new Upgrade(517, "ABILITY_CATCH_DERMESTID", "ABILITY_CATCH_DERMESTID_FLAVOR", "ABILITY_CATCH_DERMESTID_DESC", ROLE_GRAVER, 2000, null, 2);
        static { upgrades.add(ABILITY_CATCH_DERMESTID); }
        // TODO asset: icons/upgrades/AbilityDiplomatPressure2.png
        public static final Upgrade ABILITY_DIPLOMAT_PRESSURE_2 = new Upgrade(518, "ABILITY_DIPLOMAT_PRESSURE_2", "ABILITY_DIPLOMAT_PRESSURE_2_FLAVOR", "ABILITY_DIPLOMAT_PRESSURE_2_DESC", ROLE_DIPLOMAT, 2500, null, 4);
        static { upgrades.add(ABILITY_DIPLOMAT_PRESSURE_2); }
        // TODO asset: icons/upgrades/AbilityDiplomatPressure3.png
        public static final Upgrade ABILITY_DIPLOMAT_PRESSURE_3 = new Upgrade(519, "ABILITY_DIPLOMAT_PRESSURE_3", "ABILITY_DIPLOMAT_PRESSURE_3_FLAVOR", "ABILITY_DIPLOMAT_PRESSURE_3_DESC", ABILITY_DIPLOMAT_PRESSURE_2, 10000, null, 5);
        static { upgrades.add(ABILITY_DIPLOMAT_PRESSURE_3); }
        // TODO asset: icons/upgrades/AbilityAbility.png
        public static final Upgrade ABILITY_ABILITY = new Upgrade(900, "ABILITY_ABILITY", "ABILITY_ABILITY_FLAVOR", "ABILITY_ABILITY_DESC", null, 0, null, 0);
        static { upgrades.add(ABILITY_ABILITY); }
        // TODO asset: icons/upgrades/AbilityPheromoneStorm.png
        public static final Upgrade ABILITY_PHEROMONE_STORM = new Upgrade(520, "ABILITY_PHEROMONE_STORM", "ABILITY_PHEROMONE_STORM_FLAVOR", "ABILITY_PHEROMONE_STORM_DESC", ABILITY_ABILITY, 4000, null, 2);
        static { upgrades.add(ABILITY_PHEROMONE_STORM); }
        // TODO asset: icons/upgrades/AbilityCreatineDiet.png
        public static final Upgrade ABILITY_CREATINE_DIET = new Upgrade(521, "ABILITY_CREATINE_DIET", "ABILITY_CREATINE_DIET_FLAVOR", "ABILITY_CREATINE_DIET_DESC", ABILITY_ABILITY, 4000, null, 2);
        static { upgrades.add(ABILITY_CREATINE_DIET); }
        // TODO asset: icons/upgrades/AbilityAutoTunnels.png
        public static final Upgrade ABILITY_AUTO_TUNNELS = new Upgrade(522, "ABILITY_AUTO_TUNNELS", "ABILITY_AUTO_TUNNELS_FLAVOR", "ABILITY_AUTO_TUNNELS_DESC", ABILITY_AUTOMATION, 0, null, 0);
        static { upgrades.add(ABILITY_AUTO_TUNNELS); }
        // TODO asset: icons/upgrades/AbilityAutoDiplomacy.png
        public static final Upgrade ABILITY_AUTO_DIPLOMACY = new Upgrade(523, "ABILITY_AUTO_DIPLOMACY", "ABILITY_AUTO_DIPLOMACY_FLAVOR", "ABILITY_AUTO_DIPLOMACY_DESC", ABILITY_AUTOMATION, 0, null, 0);
        static { upgrades.add(ABILITY_AUTO_DIPLOMACY); }

        public static final Upgrade ABILITY_SUBTYPE_HATCH = new Upgrade(524, "ABILITY_SUBTYPE_HATCH", "ABILITY_SUBTYPE_HATCH_FLAVOR", "ABILITY_SUBTYPE_HATCH_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ABILITY_SUBTYPE_HATCH); }

        public static boolean meetsExtraAutomationPrerequisites(Dynasty dynasty, Upgrade upgrade) {
            if (upgrade == ABILITY_AUTO_TUNNELS) {
                return dynasty != null && dynasty.meetsAutoTunnelsPrerequisites();
            }
            if (upgrade == ABILITY_AUTO_DIPLOMACY) {
                return dynasty != null && dynasty.meetsAutoDiplomacyPrerequisites();
            }
            return true;
        }

        // -- Advanced Roles --
        // TODO asset: icons/roles/Courier.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_COURIER = new Upgrade(18, "ROLE_COURIER_UPGRADE", "ROLE_COURIER_FLAVOR", "ROLE_COURIER_DESC", ABILITY_TRADE, 0, null, 0);
        static { upgrades.add(ROLE_COURIER); }
        // TODO asset: icons/roles/Borer.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_BORER = new Upgrade(29, "ROLE_BORER_UPGRADE", "ROLE_BORER_FLAVOR", "ROLE_BORER_DESC", ABILITY_TUNNELS, 0, null, 0);
        static { upgrades.add(ROLE_BORER); }
        // TODO asset: icons/roles/Transport.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_TRANSPORT = new Upgrade(35, "ROLE_TRANSPORT_UPGRADE", "ROLE_TRANSPORT_FLAVOR", "ROLE_TRANSPORT_DESC", ROLE_COURIER, 100000, null, 4);
        static { upgrades.add(ROLE_TRANSPORT); }
        // TODO asset: icons/roles/Escort.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_ESCORT = new Upgrade(37, "ROLE_ESCORT_UPGRADE", "ROLE_ESCORT_FLAVOR", "ROLE_ESCORT_DESC", ROLE_COURIER, 80000, null, 3);
        static { upgrades.add(ROLE_ESCORT); }
        // TODO asset: icons/roles/Engineer.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_ENGINEER = new Upgrade(38, "ROLE_ENGINEER_UPGRADE", "ROLE_ENGINEER_FLAVOR", "ROLE_ENGINEER_DESC", ROLE_BORER, 50000, null, 3);
        static { upgrades.add(ROLE_ENGINEER); }
        // TODO asset: icons/roles/Skytrans.png (upgrade tree icon; may diverge from role icon)
        public static final Upgrade ROLE_SKYTRANS = new Upgrade(39, "ROLE_SKYTRANS_UPGRADE", "ROLE_SKYTRANS_FLAVOR", "ROLE_SKYTRANS_DESC", ROLE_COURIER, 150000, null, 4);
        static { upgrades.add(ROLE_SKYTRANS); }

        // -- Stats -- 
        // TODO asset: icons/upgrades/StatSkeleton.png
        public static final Upgrade STAT_SKELETON = new Upgrade(101, "STAT_SKELETON", "STAT_SKELETON_FLAVOR", "STAT_SKELETON_DESC", TYPE_EGG, 0, null, 0);
        static { upgrades.add(STAT_SKELETON); }
        // TODO asset: icons/upgrades/StatAcid.png
        public static final Upgrade STAT_ACID = new Upgrade(102, "STAT_ACID", "STAT_ACID_FLAVOR", "STAT_ACID_DESC", TYPE_EGG, 0, null, 0);
        static { upgrades.add(STAT_ACID); }
        // TODO asset: icons/upgrades/StatLongevity.png
        public static final Upgrade STAT_LONGEVITY = new Upgrade(103, "STAT_LONGEVITY", "STAT_LONGEVITY_FLAVOR", "STAT_LONGEVITY_DESC", TYPE_EGG, 0, null, 0);
        static { upgrades.add(STAT_LONGEVITY); }
        // TODO asset: icons/upgrades/StatResearch1.png
        public static final Upgrade STAT_RESEARCH_1 = new Upgrade(104, "STAT_RESEARCH_1", "STAT_RESEARCH_1_FLAVOR", "STAT_RESEARCH_1_DESC", ROLE_RESEARCHER, 500, null, 1);
        static { upgrades.add(STAT_RESEARCH_1); }
        // TODO asset: icons/upgrades/StatResearch2.png
        public static final Upgrade STAT_RESEARCH_2 = new Upgrade(105, "STAT_RESEARCH_2", "STAT_RESEARCH_2_FLAVOR", "STAT_RESEARCH_2_DESC", STAT_RESEARCH_1, 2000, null, 2);
        static { upgrades.add(STAT_RESEARCH_2); }
        // TODO asset: icons/upgrades/StatResearch3.png
        public static final Upgrade STAT_RESEARCH_3 = new Upgrade(106, "STAT_RESEARCH_3", "STAT_RESEARCH_3_FLAVOR", "STAT_RESEARCH_3_DESC", STAT_RESEARCH_2, 7500, null, 3);
        static { upgrades.add(STAT_RESEARCH_3); }
        // TODO asset: icons/upgrades/StatGrowth1.png
        public static final Upgrade STAT_GROWTH_1 = new Upgrade(107, "STAT_GROWTH_1", "STAT_GROWTH_1_FLAVOR", "STAT_GROWTH_1_DESC", TYPE_EGG, 3500, null, 2);
        static { upgrades.add(STAT_GROWTH_1); }
        // TODO asset: icons/upgrades/StatGrowth2.png
        public static final Upgrade STAT_GROWTH_2 = new Upgrade(108, "STAT_GROWTH_2", "STAT_GROWTH_2_FLAVOR", "STAT_GROWTH_2_DESC", STAT_GROWTH_1, 9000, null, 3);
        static { upgrades.add(STAT_GROWTH_2); }
        // TODO asset: icons/upgrades/StatGrowth3.png
        public static final Upgrade STAT_GROWTH_3 = new Upgrade(109, "STAT_GROWTH_3", "STAT_GROWTH_3_FLAVOR", "STAT_GROWTH_3_DESC", STAT_GROWTH_2, 20000, null, 4);
        static { upgrades.add(STAT_GROWTH_3); }
        // TODO asset: icons/upgrades/StatThirst1.png
        public static final Upgrade STAT_THIRST_1 = new Upgrade(110, "STAT_THIRST_1", "STAT_THIRST_1_FLAVOR", "STAT_THIRST_1_DESC", STAT_LONGEVITY, 1500, null, 2);
        static { upgrades.add(STAT_THIRST_1); }
        // TODO asset: icons/upgrades/StatThirst2.png
        public static final Upgrade STAT_THIRST_2 = new Upgrade(111, "STAT_THIRST_2", "STAT_THIRST_2_FLAVOR", "STAT_THIRST_2_DESC", STAT_THIRST_1, 5000, null, 3);
        static { upgrades.add(STAT_THIRST_2); }
        // TODO asset: icons/upgrades/StatThirst3.png
        public static final Upgrade STAT_THIRST_3 = new Upgrade(112, "STAT_THIRST_3", "STAT_THIRST_3_FLAVOR", "STAT_THIRST_3_DESC", STAT_THIRST_2, 15000, null, 4);
        static { upgrades.add(STAT_THIRST_3); }
        // TODO asset: icons/upgrades/StatLogistics1.png
        public static final Upgrade STAT_LOGISTICS_1 = new Upgrade(113, "STAT_LOGISTICS_1", "STAT_LOGISTICS_1_FLAVOR", "STAT_LOGISTICS_1_DESC", ROLE_SCOUT, 600, null, 1);
        static { upgrades.add(STAT_LOGISTICS_1); }
        // TODO asset: icons/upgrades/StatPassive1.png
        public static final Upgrade STAT_PASSIVE_1 = new Upgrade(114, "STAT_PASSIVE_1", "STAT_PASSIVE_1_FLAVOR", "STAT_PASSIVE_1_DESC", STAT_RESEARCH_3, 25000, null, 4);
        static { upgrades.add(STAT_PASSIVE_1); }
        // TODO asset: icons/upgrades/StatLaying1.png
        public static final Upgrade STAT_LAYING_1 = new Upgrade(115, "STAT_LAYING_1", "STAT_LAYING_1_FLAVOR", "STAT_LAYING_1_DESC", ROLE_LAYER, 5000, null, 2);
        static { upgrades.add(STAT_LAYING_1); }
        // TODO asset: icons/upgrades/StatLaying2.png
        public static final Upgrade STAT_LAYING_2 = new Upgrade(116, "STAT_LAYING_2", "STAT_LAYING_2_FLAVOR", "STAT_LAYING_2_DESC", STAT_LAYING_1, 15000, null, 3);
        static { upgrades.add(STAT_LAYING_2); }
        // TODO asset: icons/upgrades/StatLaying3.png
        public static final Upgrade STAT_LAYING_3 = new Upgrade(117, "STAT_LAYING_3", "STAT_LAYING_3_FLAVOR", "STAT_LAYING_3_DESC", STAT_LAYING_2, 80000, null, 4);
        static { upgrades.add(STAT_LAYING_3); }
        // TODO asset: icons/upgrades/StatScouting1.png
        public static final Upgrade STAT_SCOUTING_1 = new Upgrade(118, "STAT_SCOUTING_1", "STAT_SCOUTING_1_FLAVOR", "STAT_SCOUTING_1_DESC", ROLE_SCOUT, 4000, null, 2);
        static { upgrades.add(STAT_SCOUTING_1); }
        // TODO asset: icons/upgrades/StatScouting2.png
        public static final Upgrade STAT_SCOUTING_2 = new Upgrade(119, "STAT_SCOUTING_2", "STAT_SCOUTING_2_FLAVOR", "STAT_SCOUTING_2_DESC", STAT_SCOUTING_1, 15000, null, 3);
        static { upgrades.add(STAT_SCOUTING_2); }
        // TODO asset: icons/upgrades/StatScouting3.png
        public static final Upgrade STAT_SCOUTING_3 = new Upgrade(120, "STAT_SCOUTING_3", "STAT_SCOUTING_3_FLAVOR", "STAT_SCOUTING_3_DESC", STAT_SCOUTING_2, 35000, null, 4);
        static { upgrades.add(STAT_SCOUTING_3); }
        // TODO asset: icons/upgrades/StatFarming1.png
        public static final Upgrade STAT_FARMING_1 = new Upgrade(121, "STAT_FARMING_1", "STAT_FARMING_1_FLAVOR", "STAT_FARMING_1_DESC", ROLE_FARMER, 4500, null, 2);
        static { upgrades.add(STAT_FARMING_1); }
        // TODO asset: icons/upgrades/StatFarming2.png
        public static final Upgrade STAT_FARMING_2 = new Upgrade(122, "STAT_FARMING_2", "STAT_FARMING_2_FLAVOR", "STAT_FARMING_2_DESC", STAT_FARMING_1, 16000, null, 3);
        static { upgrades.add(STAT_FARMING_2); }
        // TODO asset: icons/upgrades/StatFarming3.png
        public static final Upgrade STAT_FARMING_3 = new Upgrade(123, "STAT_FARMING_3", "STAT_FARMING_3_FLAVOR", "STAT_FARMING_3_DESC", STAT_FARMING_2, 60000, null, 4);
        static { upgrades.add(STAT_FARMING_3); }
        // TODO asset: icons/upgrades/StatContamination1.png
        public static final Upgrade STAT_CONTAMINATION_1 = new Upgrade(124, "STAT_CONTAMINATION_1", "STAT_CONTAMINATION_1_FLAVOR", "STAT_CONTAMINATION_1_DESC", ROLE_GRAVER, 2000, null, 2);
        static { upgrades.add(STAT_CONTAMINATION_1); }
        // TODO asset: icons/upgrades/StatContamination2.png
        public static final Upgrade STAT_CONTAMINATION_2 = new Upgrade(125, "STAT_CONTAMINATION_2", "STAT_CONTAMINATION_2_FLAVOR", "STAT_CONTAMINATION_2_DESC", STAT_CONTAMINATION_1, 8000, null, 3);
        static { upgrades.add(STAT_CONTAMINATION_2); }
        // TODO asset: icons/upgrades/StatContamination3.png
        public static final Upgrade STAT_CONTAMINATION_3 = new Upgrade(126, "STAT_CONTAMINATION_3", "STAT_CONTAMINATION_3_FLAVOR", "STAT_CONTAMINATION_3_DESC", STAT_CONTAMINATION_2, 25000, null, 4);
        static { upgrades.add(STAT_CONTAMINATION_3); }
        // TODO asset: icons/upgrades/StatSymbioticMite1.png
        public static final Upgrade STAT_SYMBIOTIC_MITE_1 = new Upgrade(135, "STAT_SYMBIOTIC_MITE_1", "STAT_SYMBIOTIC_MITE_1_FLAVOR", "STAT_SYMBIOTIC_MITE_1_DESC", ABILITY_CATCH_SYMBIOTIC_MITE, 2000, null, 2);
        static { upgrades.add(STAT_SYMBIOTIC_MITE_1); }
        // TODO asset: icons/upgrades/StatDermestid1.png
        public static final Upgrade STAT_DERMESTID_1 = new Upgrade(136, "STAT_DERMESTID_1", "STAT_DERMESTID_1_FLAVOR", "STAT_DERMESTID_1_DESC", ABILITY_CATCH_DERMESTID, 2000, null, 3);
        static { upgrades.add(STAT_DERMESTID_1); }
        // TODO asset: icons/upgrades/StatGraving1.png
        public static final Upgrade STAT_GRAVING_1 = new Upgrade(127, "STAT_GRAVING_1", "STAT_GRAVING_1_FLAVOR", "STAT_GRAVING_1_DESC", ROLE_GRAVER, 1500, null, 2);
        static { upgrades.add(STAT_GRAVING_1); }
        // TODO asset: icons/upgrades/StatGraving2.png
        public static final Upgrade STAT_GRAVING_2 = new Upgrade(128, "STAT_GRAVING_2", "STAT_GRAVING_2_FLAVOR", "STAT_GRAVING_2_DESC", STAT_GRAVING_1, 6500, null, 3);
        static { upgrades.add(STAT_GRAVING_2); }
        // TODO asset: icons/upgrades/StatGraving3.png
        public static final Upgrade STAT_GRAVING_3 = new Upgrade(129, "STAT_GRAVING_3", "STAT_GRAVING_3_FLAVOR", "STAT_GRAVING_3_DESC", STAT_GRAVING_2, 40000, null, 4);
        static { upgrades.add(STAT_GRAVING_3); }
        // TODO asset: icons/upgrades/StatPolicing1.png
        public static final Upgrade STAT_POLICING_1 = new Upgrade(130, "STAT_POLICING_1", "STAT_POLICING_1_FLAVOR", "STAT_POLICING_1_DESC", ROLE_POLICE, 3000, null, 2);
        static { upgrades.add(STAT_POLICING_1); }
        // TODO asset: icons/upgrades/StatPolicing2.png
        public static final Upgrade STAT_POLICING_2 = new Upgrade(131, "STAT_POLICING_2", "STAT_POLICING_2_FLAVOR", "STAT_POLICING_2_DESC", STAT_POLICING_1, 14000, null, 3);
        static { upgrades.add(STAT_POLICING_2); }
        // TODO asset: icons/upgrades/StatPolicing3.png
        public static final Upgrade STAT_POLICING_3 = new Upgrade(132, "STAT_POLICING_3", "STAT_POLICING_3_FLAVOR", "STAT_POLICING_3_DESC", STAT_POLICING_2, 42000, null, 4);
        static { upgrades.add(STAT_POLICING_3); }
        // TODO asset: icons/upgrades/StatHexSustain.png
        public static final Upgrade STAT_HEX_SUSTAIN = new Upgrade(133, "STAT_HEX_SUSTAIN", "STAT_HEX_SUSTAIN_FLAVOR", "STAT_HEX_SUSTAIN_DESC", STAT_FARMING_3, 175000, null, 5);
        static { upgrades.add(STAT_HEX_SUSTAIN); }
        // TODO asset: icons/upgrades/StatWorkerSpeed2.png
        public static final Upgrade STAT_WORKER_SPEED_2 = new Upgrade(134, "STAT_WORKER_SPEED_2", "STAT_WORKER_SPEED_2_FLAVOR", "STAT_WORKER_SPEED_2_DESC", STAT_SCOUTING_3, 200000, null, 5);
        static { upgrades.add(STAT_WORKER_SPEED_2); }
        // -- Assimilated --
        // TODO asset: icons/upgrades/AssimilatedFarming.png; icons/species/Leafcutter.png (placeholder — replace final art)
        public static final Upgrade ASSIMILATED_FARMING = new Upgrade(1001, "ASSIMILATED_FARMING", "ASSIMILATED_FARMING_FLAVOR", "ASSIMILATED_FARMING_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_FARMING); }
        // TODO asset: icons/upgrades/AssimilatedMultiqueen.png; icons/species/Pharaoh.png (placeholder — replace final art)
        public static final Upgrade ASSIMILATED_MULTIQUEEN = new Upgrade(1002, "ASSIMILATED_MULTIQUEEN", "ASSIMILATED_MULTIQUEEN_FLAVOR", "ASSIMILATED_MULTIQUEEN_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_MULTIQUEEN); }
        // TODO asset: icons/upgrades/AssimilatedTrapjaw.png; icons/species/Trapjaw.png (placeholder — replace final art)
        public static final Upgrade ASSIMILATED_TRAPJAW = new Upgrade(1003, "ASSIMILATED_TRAPJAW", "ASSIMILATED_TRAPJAW_FLAVOR", "ASSIMILATED_TRAPJAW_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_TRAPJAW); }
        // TODO asset: icons/upgrades/AssimilatedHoneypot.png; icons/species/Honeypot.png (placeholder — replace final art)
        public static final Upgrade ASSIMILATED_HONEYPOT = new Upgrade(1004, "ASSIMILATED_HONEYPOT", "ASSIMILATED_HONEYPOT_FLAVOR", "ASSIMILATED_HONEYPOT_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_HONEYPOT); }
        // TODO asset: icons/upgrades/AssimilatedDoorhead.png; icons/species/Turtle.png (placeholder — replace final art)
        public static final Upgrade ASSIMILATED_DOORHEAD = new Upgrade(1005, "ASSIMILATED_DOORHEAD", "ASSIMILATED_DOORHEAD_FLAVOR", "ASSIMILATED_DOORHEAD_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_DOORHEAD); }
        // TODO asset: icons/upgrades/AssimilatedWoodburrow.png; icons/species/Carpenter.png (placeholder — replace final art)
        // TODO mechanic: wood excavation / carpenter nesting — not implemented
        public static final Upgrade ASSIMILATED_WOODBURROW = new Upgrade(1006, "ASSIMILATED_WOODBURROW", "ASSIMILATED_WOODBURROW_FLAVOR", "ASSIMILATED_WOODBURROW_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_WOODBURROW); }
        // TODO asset: icons/upgrades/AssimilatedSilkweave.png; icons/species/Weaver.png (placeholder — replace final art)
        // TODO mechanic: silk nest weaving — not implemented
        public static final Upgrade ASSIMILATED_SILKWEAVE = new Upgrade(1007, "ASSIMILATED_SILKWEAVE", "ASSIMILATED_SILKWEAVE_FLAVOR", "ASSIMILATED_SILKWEAVE_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_SILKWEAVE); }
        // TODO asset: icons/upgrades/AssimilatedRafting.png; icons/species/Floodplain.png (placeholder — replace final art)
        // TODO mechanic: flood rafting — not implemented
        public static final Upgrade ASSIMILATED_RAFTING = new Upgrade(1008, "ASSIMILATED_RAFTING", "ASSIMILATED_RAFTING_FLAVOR", "ASSIMILATED_RAFTING_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_RAFTING); }
        // TODO asset: icons/upgrades/AssimilatedFirevenom.png; icons/species/Fire.png (placeholder — replace final art)
        public static final Upgrade ASSIMILATED_FIREVENOM = new Upgrade(1009, "ASSIMILATED_FIREVENOM", "ASSIMILATED_FIREVENOM_FLAVOR", "ASSIMILATED_FIREVENOM_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_FIREVENOM); }
        // TODO asset: icons/upgrades/AssimilatedJumping.png; icons/species/Jet.png (placeholder — replace final art)
        // TODO mechanic: jump attacks — not implemented
        public static final Upgrade ASSIMILATED_JUMPING = new Upgrade(1010, "ASSIMILATED_JUMPING", "ASSIMILATED_JUMPING_FLAVOR", "ASSIMILATED_JUMPING_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_JUMPING); }
        // TODO asset: icons/upgrades/AssimilatedGliding.png; icons/species/Gliding.png (placeholder — replace final art)
        // TODO mechanic: gliding movement — not implemented
        public static final Upgrade ASSIMILATED_GLIDING = new Upgrade(1011, "ASSIMILATED_GLIDING", "ASSIMILATED_GLIDING_FLAVOR", "ASSIMILATED_GLIDING_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_GLIDING); }
        // TODO asset: icons/upgrades/AssimilatedStinging.png; icons/species/Bullet.png (placeholder — replace final art)
        public static final Upgrade ASSIMILATED_STINGING = new Upgrade(1012, "ASSIMILATED_STINGING", "ASSIMILATED_STINGING_FLAVOR", "ASSIMILATED_STINGING_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_STINGING); }
        // TODO asset: icons/upgrades/AssimilatedSwarming.png; icons/species/Army.png (placeholder — replace final art)
        // TODO mechanic: army-ant raiding swarms — not implemented
        public static final Upgrade ASSIMILATED_SWARMING = new Upgrade(1013, "ASSIMILATED_SWARMING", "ASSIMILATED_SWARMING_FLAVOR", "ASSIMILATED_SWARMING_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_SWARMING); }
        // TODO asset: icons/upgrades/AssimilatedStealth.png; icons/species/Ghost.png (placeholder — replace final art)
        // TODO mechanic: ghost-ant stealth / hostile nesting — not implemented
        public static final Upgrade ASSIMILATED_STEALTH = new Upgrade(1014, "ASSIMILATED_STEALTH", "ASSIMILATED_STEALTH_FLAVOR", "ASSIMILATED_STEALTH_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_STEALTH); }
        // TODO asset: icons/upgrades/AssimilatedFastbite.png; icons/species/Dracula.png (placeholder — replace final art)
        public static final Upgrade ASSIMILATED_FASTBITE = new Upgrade(1015, "ASSIMILATED_FASTBITE", "ASSIMILATED_FASTBITE_FLAVOR", "ASSIMILATED_FASTBITE_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_FASTBITE); }
        // TODO asset: icons/upgrades/AssimilatedHeatresist.png; icons/species/Silver.png (placeholder — replace final art)
        // TODO mechanic: desert heat foraging tolerance — not implemented
        public static final Upgrade ASSIMILATED_HEATRESIST = new Upgrade(1016, "ASSIMILATED_HEATRESIST", "ASSIMILATED_HEATRESIST_FLAVOR", "ASSIMILATED_HEATRESIST_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_HEATRESIST); }
        // TODO asset: icons/upgrades/AssimilatedDeadlyvenom.png; icons/species/Maricopa.png (placeholder — replace final art)
        public static final Upgrade ASSIMILATED_DEADLYVENOM = new Upgrade(1017, "ASSIMILATED_DEADLYVENOM", "ASSIMILATED_DEADLYVENOM_FLAVOR", "ASSIMILATED_DEADLYVENOM_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_DEADLYVENOM); }
        // TODO asset: icons/upgrades/AssimilatedSelfdestruct.png; icons/species/Exploding.png (placeholder — replace final art)
        // TODO mechanic: exploding-ant self-destruct defense — not implemented
        public static final Upgrade ASSIMILATED_SELFDESTRUCT = new Upgrade(1018, "ASSIMILATED_SELFDESTRUCT", "ASSIMILATED_SELFDESTRUCT_FLAVOR", "ASSIMILATED_SELFDESTRUCT_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_SELFDESTRUCT); }
        // TODO asset: icons/upgrades/AssimilatedFarsight.png; icons/species/Bulldog.png (placeholder — replace final art)
        // TODO mechanic: bulldog vision / tracking — not implemented
        public static final Upgrade ASSIMILATED_FARSIGHT = new Upgrade(1019, "ASSIMILATED_FARSIGHT", "ASSIMILATED_FARSIGHT_FLAVOR", "ASSIMILATED_FARSIGHT_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_FARSIGHT); }
        // TODO asset: icons/upgrades/AssimilatedHivebuild.png; icons/species/ShiningBlack.png (placeholder — replace final art)
        // TODO mechanic: carton hive building — not implemented
        public static final Upgrade ASSIMILATED_HIVEBUILD = new Upgrade(1020, "ASSIMILATED_HIVEBUILD", "ASSIMILATED_HIVEBUILD_FLAVOR", "ASSIMILATED_HIVEBUILD_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_HIVEBUILD); }
        // TODO asset: icons/upgrades/AssimilatedLocsense.png; icons/species/Desert.png (placeholder — replace final art)
        // TODO mechanic: desert route navigation — not implemented
        public static final Upgrade ASSIMILATED_LOCSENSE = new Upgrade(1021, "ASSIMILATED_LOCSENSE", "ASSIMILATED_LOCSENSE_FLAVOR", "ASSIMILATED_LOCSENSE_DESC", ABILITY_ASSIMILATION, 0, null, 0);
        static { upgrades.add(ASSIMILATED_LOCSENSE); }

        // TODO asset: icons/upgrades/SynergySuperVenom.png (placeholder — replace final art)
        public static final Upgrade SYNERGY_SUPER_VENOM = new Upgrade(10001, "SYNERGY_SUPER_VENOM", "SYNERGY_SUPER_VENOM_FLAVOR", "SYNERGY_SUPER_VENOM_DESC", null, 0, null, 0);
        static { upgrades.add(SYNERGY_SUPER_VENOM); }

        // --- Buildings ---

        // -- Tier 0 --
        public static final Building ROYAL_CHAMBER_0 = new Building(1, "ROYAL_CHAMBER_0", 0, "ROYAL_CHAMBER_0_DESC", null, 0, 0, 0, GameConstants.ROOM_ROYAL_L0, 0);
        static { buildings.add(ROYAL_CHAMBER_0); }
        public static final Building EGG_CHAMBER_0 = new Building(2, "EGG_CHAMBER_0", 0, "EGG_CHAMBER_0_DESC", null, 0, 0, 0, GameConstants.ROOM_EGG_L0, 0);
        static { buildings.add(EGG_CHAMBER_0); }
        public static final Building MUSHROOM_CHAMBER_0 = new Building(3, "MUSHROOM_CHAMBER_0", 0, "MUSHROOM_CHAMBER_0_DESC", null, 0, 0, 0, GameConstants.ROOM_MUSHROOM_L0, 0);
        static { buildings.add(MUSHROOM_CHAMBER_0); }
        public static final Building PLANT_CHAMBER_0 = new Building(4, "PLANT_CHAMBER_0", 0, "PLANT_CHAMBER_0_DESC", null, 0, 0, 0, GameConstants.ROOM_PLANT_L0, 0);
        static { buildings.add(PLANT_CHAMBER_0); }
        public static final Building WATER_RESERVOIR_0 = new Building(5, "WATER_RESERVOIR_0", 0, "WATER_RESERVOIR_0_DESC", null, 0, 0, 0, GameConstants.ROOM_WATER_L0, 0);
        static { buildings.add(WATER_RESERVOIR_0); }
        public static final Building MEAT_CHAMBER_0 = new Building(6, "MEAT_CHAMBER_0", 0, "MEAT_CHAMBER_0_DESC", null, 0, 0, 12, GameConstants.ROOM_MEAT_L0, 0);
        static { buildings.add(MEAT_CHAMBER_0); }
        public static final Building SYRUP_RESERVOIR_0 = new Building(7, "SYRUP_RESERVOIR_0", 0, "SYRUP_RESERVOIR_0_DESC", null, 0, 0, 24, GameConstants.ROOM_SYRUP_L0, 0);
        static { buildings.add(SYRUP_RESERVOIR_0); }
        public static final Building ROCK_WAREHOUSE_0 = new Building(8, "ROCK_WAREHOUSE_0", 0, "ROCK_WAREHOUSE_0_DESC", null, 0, 0, 50, GameConstants.ROOM_ROCK_L0, 0);
        static { buildings.add(ROCK_WAREHOUSE_0); }
        public static final Building RESIN_RESERVOIR_0 = new Building(9, "RESIN_RESERVOIR_0", 0, "RESIN_RESERVOIR_0_DESC", null, 0, 0, 50, GameConstants.ROOM_RESIN_L0, 0);
        static { buildings.add(RESIN_RESERVOIR_0); }
        // -- Tier 1 --
        public static final Building ROYAL_CHAMBER_1 = new Building(10, "ROYAL_CHAMBER_1", 1, "ROYAL_CHAMBER_1_DESC", ROYAL_CHAMBER_0, 0, 0, 1000, GameConstants.ROOM_ROYAL_L1, 1);
        static { buildings.add(ROYAL_CHAMBER_1); }
        public static final Building EGG_CHAMBER_1 = new Building(11, "EGG_CHAMBER_1", 1, "EGG_CHAMBER_1_DESC", EGG_CHAMBER_0, 0, 0, 100, GameConstants.ROOM_EGG_L1, 1);
        static { buildings.add(EGG_CHAMBER_1); }
        public static final Building MUSHROOM_CHAMBER_1 = new Building(12, "MUSHROOM_CHAMBER_1", 1, "MUSHROOM_CHAMBER_1_DESC", MUSHROOM_CHAMBER_0, 0, 0, 150, GameConstants.ROOM_MUSHROOM_L1, 1);
        static { buildings.add(MUSHROOM_CHAMBER_1); }
        public static final Building PLANT_CHAMBER_1 = new Building(13, "PLANT_CHAMBER_1", 1, "PLANT_CHAMBER_1_DESC", PLANT_CHAMBER_0, 0, 0, 180, GameConstants.ROOM_PLANT_L1, 1);
        static { buildings.add(PLANT_CHAMBER_1); }
        public static final Building WATER_RESERVOIR_1 = new Building(14, "WATER_RESERVOIR_1", 1, "WATER_RESERVOIR_1_DESC", WATER_RESERVOIR_0, 0, 0, 250, GameConstants.ROOM_WATER_L1, 1);
        static { buildings.add(WATER_RESERVOIR_1); }
        public static final Building MEAT_CHAMBER_1 = new Building(15, "MEAT_CHAMBER_1", 1, "MEAT_CHAMBER_1_DESC", MEAT_CHAMBER_0, 0, 0, 200, GameConstants.ROOM_MEAT_L1, 1);
        static { buildings.add(MEAT_CHAMBER_1); }
        public static final Building SYRUP_RESERVOIR_1 = new Building(16, "SYRUP_RESERVOIR_1", 1, "SYRUP_RESERVOIR_1_DESC", SYRUP_RESERVOIR_0, 0, 0, 250, GameConstants.ROOM_SYRUP_L1, 1);
        static { buildings.add(SYRUP_RESERVOIR_1); }
        public static final Building ROCK_WAREHOUSE_1 = new Building(17, "ROCK_WAREHOUSE_1", 1, "ROCK_WAREHOUSE_1_DESC", ROCK_WAREHOUSE_0, 0, 0, 500, GameConstants.ROOM_ROCK_L1, 1);
        static { buildings.add(ROCK_WAREHOUSE_1); }
        public static final Building RESIN_RESERVOIR_1 = new Building(18, "RESIN_RESERVOIR_1", 1, "RESIN_RESERVOIR_1_DESC", RESIN_RESERVOIR_0, 0, 0, 400, GameConstants.ROOM_RESIN_L1, 1);
        static { buildings.add(RESIN_RESERVOIR_1); }
        // -- Tier 2 --
        public static final Building ROYAL_CHAMBER_2 = new Building(19, "ROYAL_CHAMBER_2", 2, "ROYAL_CHAMBER_2_DESC", ROYAL_CHAMBER_1, 500, 0, 3000, GameConstants.ROOM_ROYAL_L2, 2);
        static { buildings.add(ROYAL_CHAMBER_2); }
        public static final Building EGG_CHAMBER_2 = new Building(20, "EGG_CHAMBER_2", 2, "EGG_CHAMBER_2_DESC", EGG_CHAMBER_1, 200, 0, 1000, GameConstants.ROOM_EGG_L2, 2);
        static { buildings.add(EGG_CHAMBER_2); }
        public static final Building MUSHROOM_CHAMBER_2 = new Building(21, "MUSHROOM_CHAMBER_2", 2, "MUSHROOM_CHAMBER_2_DESC", MUSHROOM_CHAMBER_1, 250, 0, 800, GameConstants.ROOM_MUSHROOM_L2, 2);
        static { buildings.add(MUSHROOM_CHAMBER_2); }
        public static final Building PLANT_CHAMBER_2 = new Building(22, "PLANT_CHAMBER_2", 2, "PLANT_CHAMBER_2_DESC", PLANT_CHAMBER_1, 200, 0, 1000, GameConstants.ROOM_PLANT_L2, 2);
        static { buildings.add(PLANT_CHAMBER_2); }
        public static final Building WATER_RESERVOIR_2 = new Building(23, "WATER_RESERVOIR_2", 2, "WATER_RESERVOIR_2_DESC", WATER_RESERVOIR_1, 300, 0, 1200, GameConstants.ROOM_WATER_L2, 2);
        static { buildings.add(WATER_RESERVOIR_2); }
        public static final Building MEAT_CHAMBER_2 = new Building(24, "MEAT_CHAMBER_2", 2, "MEAT_CHAMBER_2_DESC", MEAT_CHAMBER_1, 350, 0, 1500, GameConstants.ROOM_MEAT_L2, 2);
        static { buildings.add(MEAT_CHAMBER_2); }
        public static final Building SYRUP_RESERVOIR_2 = new Building(25, "SYRUP_RESERVOIR_2", 2, "SYRUP_RESERVOIR_2_DESC", SYRUP_RESERVOIR_1, 350, 0, 1500, GameConstants.ROOM_SYRUP_L2, 2);
        static { buildings.add(SYRUP_RESERVOIR_2); }
        public static final Building ROCK_WAREHOUSE_2 = new Building(26, "ROCK_WAREHOUSE_2", 2, "ROCK_WAREHOUSE_2_DESC", ROCK_WAREHOUSE_1, 450, 0, 2500, GameConstants.ROOM_ROCK_L2, 2);
        static { buildings.add(ROCK_WAREHOUSE_2); }
        public static final Building RESIN_RESERVOIR_2 = new Building(27, "RESIN_RESERVOIR_2", 2, "RESIN_RESERVOIR_2_DESC", RESIN_RESERVOIR_1, 400, 0, 2000, GameConstants.ROOM_RESIN_L2, 2);
        static { buildings.add(RESIN_RESERVOIR_2); }
        // -- Tier 3 --
        public static final Building ROYAL_CHAMBER_3 = new Building(28, "ROYAL_CHAMBER_3", 3, "ROYAL_CHAMBER_3_DESC", ROYAL_CHAMBER_2, 1000, 0, 15000, GameConstants.ROOM_ROYAL_L3, 3);
        static { buildings.add(ROYAL_CHAMBER_3); }
        public static final Building EGG_CHAMBER_3 = new Building(29, "EGG_CHAMBER_3", 3, "EGG_CHAMBER_3_DESC", EGG_CHAMBER_2, 500, 0, 5000, GameConstants.ROOM_EGG_L3, 3);
        static { buildings.add(EGG_CHAMBER_3); }
        public static final Building MUSHROOM_CHAMBER_3 = new Building(30, "MUSHROOM_CHAMBER_3", 3, "MUSHROOM_CHAMBER_3_DESC", MUSHROOM_CHAMBER_2, 800, 0, 5000, GameConstants.ROOM_MUSHROOM_L3, 3);
        static { buildings.add(MUSHROOM_CHAMBER_3); }
        public static final Building PLANT_CHAMBER_3 = new Building(31, "PLANT_CHAMBER_3", 3, "PLANT_CHAMBER_3_DESC", PLANT_CHAMBER_2, 700, 0, 5000, GameConstants.ROOM_PLANT_L3, 3);
        static { buildings.add(PLANT_CHAMBER_3); }
        public static final Building WATER_RESERVOIR_3 = new Building(32, "WATER_RESERVOIR_3", 3, "WATER_RESERVOIR_3_DESC", WATER_RESERVOIR_2, 900, 0, 5000, GameConstants.ROOM_WATER_L3, 3);
        static { buildings.add(WATER_RESERVOIR_3); }
        public static final Building MEAT_CHAMBER_3 = new Building(33, "MEAT_CHAMBER_3", 3, "MEAT_CHAMBER_3_DESC", MEAT_CHAMBER_2, 850, 0, 10000, GameConstants.ROOM_MEAT_L3, 3);
        static { buildings.add(MEAT_CHAMBER_3); }
        public static final Building SYRUP_RESERVOIR_3 = new Building(34, "SYRUP_RESERVOIR_3", 3, "SYRUP_RESERVOIR_3_DESC", SYRUP_RESERVOIR_2, 800, 0, 10000, GameConstants.ROOM_SYRUP_L3, 3);
        static { buildings.add(SYRUP_RESERVOIR_3); }
        public static final Building ROCK_WAREHOUSE_3 = new Building(35, "ROCK_WAREHOUSE_3", 3, "ROCK_WAREHOUSE_3_DESC", ROCK_WAREHOUSE_2, 950, 0, 10000, GameConstants.ROOM_ROCK_L3, 3);
        static { buildings.add(ROCK_WAREHOUSE_3); }
        public static final Building RESIN_RESERVOIR_3 = new Building(36, "RESIN_RESERVOIR_3", 3, "RESIN_RESERVOIR_3_DESC", RESIN_RESERVOIR_2, 900, 0, 10000, GameConstants.ROOM_RESIN_L3, 3);
        static { buildings.add(RESIN_RESERVOIR_3); }
        // -- Tier 4 --
        // -- Tier 5 --
        // -- Tier Misc. (passive overlays) --
        public static final Building PASSIVE_LAB = new Building(101, "PASSIVE_LAB", 0, "PASSIVE_LAB_DESC", ROYAL_CHAMBER_1, 150, 0, 800, GameConstants.ROOM_PASSIVE_LAB, 2);
        static { buildings.add(PASSIVE_LAB); }
        public static final Building PASSIVE_WATER = new Building(102, "PASSIVE_WATER", 0, "PASSIVE_WATER_DESC", WATER_RESERVOIR_1, 100, 0, 500, GameConstants.ROOM_PASSIVE_WATER, 2);
        static { buildings.add(PASSIVE_WATER); }
        public static final Building PASSIVE_APHID = new Building(103, "PASSIVE_APHID", 0, "PASSIVE_APHID_DESC", SYRUP_RESERVOIR_1, 200, 0, 1000, GameConstants.ROOM_PASSIVE_APHID, 2);
        static { buildings.add(PASSIVE_APHID); }
        public static final Building PASSIVE_NURSE = new Building(104, "PASSIVE_NURSE", 0, "PASSIVE_NURSE_DESC", EGG_CHAMBER_1, 150, 0, 700, GameConstants.ROOM_PASSIVE_NURSE, 2);
        static { buildings.add(PASSIVE_NURSE); }
        public static final Building PASSIVE_FARM = new Building(105, "PASSIVE_FARM", 0, "PASSIVE_FARM_DESC", MUSHROOM_CHAMBER_1, 200, 0, 900, GameConstants.ROOM_PASSIVE_FARM, 2);
        static { buildings.add(PASSIVE_FARM); }
        public static final Building PASSIVE_GRAVE = new Building(106, "PASSIVE_GRAVE", 0, "PASSIVE_GRAVE_DESC", MEAT_CHAMBER_1, 150, 0, 600, GameConstants.ROOM_PASSIVE_GRAVE, 2);
        static { buildings.add(PASSIVE_GRAVE); }
        public static final Building BUILDING_COMPOSTER = new Building(107, "BUILDING_COMPOSTER", 0, "BUILDING_COMPOSTER_DESC", PASSIVE_GRAVE, 500, 0, 1000, GameConstants.ROOM_PASSIVE_COMPOSTER, 3);
        static { buildings.add(BUILDING_COMPOSTER); }

        public static final Building[] BUILDING_CHAIN_ROYAL = {
                ROYAL_CHAMBER_3, ROYAL_CHAMBER_2, ROYAL_CHAMBER_1, ROYAL_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_EGG = {
                EGG_CHAMBER_3, EGG_CHAMBER_2, EGG_CHAMBER_1, EGG_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_MUSHROOM = {
                MUSHROOM_CHAMBER_3, MUSHROOM_CHAMBER_2, MUSHROOM_CHAMBER_1, MUSHROOM_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_PLANT = {
                PLANT_CHAMBER_3, PLANT_CHAMBER_2, PLANT_CHAMBER_1, PLANT_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_WATER = {
                WATER_RESERVOIR_3, WATER_RESERVOIR_2, WATER_RESERVOIR_1, WATER_RESERVOIR_0
        };
        public static final Building[] BUILDING_CHAIN_MEAT = {
                MEAT_CHAMBER_3, MEAT_CHAMBER_2, MEAT_CHAMBER_1, MEAT_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_SYRUP = {
                SYRUP_RESERVOIR_3, SYRUP_RESERVOIR_2, SYRUP_RESERVOIR_1, SYRUP_RESERVOIR_0
        };
        public static final Building[] BUILDING_CHAIN_ROCK = {
                ROCK_WAREHOUSE_3, ROCK_WAREHOUSE_2, ROCK_WAREHOUSE_1, ROCK_WAREHOUSE_0
        };
        public static final Building[] BUILDING_CHAIN_RESIN = {
                RESIN_RESERVOIR_3, RESIN_RESERVOIR_2, RESIN_RESERVOIR_1, RESIN_RESERVOIR_0
        };

        // --- Assimilations ---
        public static final int ASSIMILATION_COST = 10000;
        public static final double ASSIMILATION_COST_STEP = 0.25;

        public static int getAssimilationTargetCost(Dynasty dynasty) {
                int completed = dynasty == null ? 0 : dynasty.getCompletedAssimilations().size();
                return getAssimilationTargetCostForCompletedCount(completed);
        }

        public static int getAssimilationTargetCostForCompletedCount(int completedAssimilations) {
                double multiplier = 1.0 + ASSIMILATION_COST_STEP * Math.max(0, completedAssimilations);
                return (int) Math.round(ASSIMILATION_COST * multiplier);
        }

        // TODO asset: icons/assimilations/Leafcutter.png; icons/species/Leafcutter.png (placeholder — replace final art)
        public static final Assimilation ASSIMILATION_LEAFCUTTER = new Assimilation(1, "ASSIMILATION_LEAFCUTTER", "ASSIMILATION_LEAFCUTTER_DESC", ASSIMILATED_FARMING, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_LEAFCUTTER); }
        // TODO asset: icons/assimilations/Pharaoh.png; icons/species/Pharaoh.png (placeholder — replace final art)
        public static final Assimilation ASSIMILATION_PHARAOH = new Assimilation(2, "ASSIMILATION_PHARAOH", "ASSIMILATION_PHARAOH_DESC", ASSIMILATED_MULTIQUEEN, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_PHARAOH); }
        // TODO asset: icons/assimilations/Marauder.png; icons/species/Marauder.png (placeholder — replace final art)
        public static final Assimilation ASSIMILATION_MARAUDER = new Assimilation(3, "ASSIMILATION_MARAUDER", "ASSIMILATION_MARAUDER_DESC", TYPE_MAJOR, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_MARAUDER); }
        // TODO asset: icons/assimilations/Trapjaw.png; icons/species/Trapjaw.png (placeholder — replace final art)
        public static final Assimilation ASSIMILATION_TRAPJAW = new Assimilation(4, "ASSIMILATION_TRAPJAW", "ASSIMILATION_TRAPJAW_DESC", ASSIMILATED_TRAPJAW, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_TRAPJAW); }

        public static final Assimilation ASSIMILATION_HONEYPOT = new Assimilation(5, "ASSIMILATION_HONEYPOT", "ASSIMILATION_HONEYPOT_DESC", ASSIMILATED_HONEYPOT, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_HONEYPOT); }

        public static final Assimilation ASSIMILATION_DOORHEAD = new Assimilation(6, "ASSIMILATION_DOORHEAD", "ASSIMILATION_DOORHEAD_DESC", ASSIMILATED_DOORHEAD, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_DOORHEAD); }
        // TODO asset: icons/assimilations/Carpenter.png; icons/species/Carpenter.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_WOODBURROW — not implemented
        public static final Assimilation ASSIMILATION_WOODBURROW = new Assimilation(7, "ASSIMILATION_WOODBURROW", "ASSIMILATION_WOODBURROW_DESC", ASSIMILATED_WOODBURROW, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_WOODBURROW); }
        // TODO asset: icons/assimilations/Weaver.png; icons/species/Weaver.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_SILKWEAVE — not implemented
        public static final Assimilation ASSIMILATION_SILKWEAVE = new Assimilation(8, "ASSIMILATION_SILKWEAVE", "ASSIMILATION_SILKWEAVE_DESC", ASSIMILATED_SILKWEAVE, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_SILKWEAVE); }
        // TODO asset: icons/assimilations/Floodplain.png; icons/species/Floodplain.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_RAFTING — not implemented
        public static final Assimilation ASSIMILATION_RAFTING = new Assimilation(9, "ASSIMILATION_RAFTING", "ASSIMILATION_RAFTING_DESC", ASSIMILATED_RAFTING, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_RAFTING); }
        // TODO asset: icons/assimilations/Fire.png; icons/species/Fire.png (placeholder — replace final art)
        public static final Assimilation ASSIMILATION_FIREVENOM = new Assimilation(10, "ASSIMILATION_FIREVENOM", "ASSIMILATION_FIREVENOM_DESC", ASSIMILATED_FIREVENOM, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_FIREVENOM); }
        // TODO asset: icons/assimilations/Jet.png; icons/species/Jet.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_JUMPING — not implemented
        public static final Assimilation ASSIMILATION_JUMPING = new Assimilation(11, "ASSIMILATION_JUMPING", "ASSIMILATION_JUMPING_DESC", ASSIMILATED_JUMPING, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_JUMPING); }
        // TODO asset: icons/assimilations/Gliding.png; icons/species/Gliding.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_GLIDING — not implemented
        public static final Assimilation ASSIMILATION_GLIDING = new Assimilation(12, "ASSIMILATION_GLIDING", "ASSIMILATION_GLIDING_DESC", ASSIMILATED_GLIDING, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_GLIDING); }
        // TODO asset: icons/assimilations/Bullet.png; icons/species/Bullet.png (placeholder — replace final art)
        public static final Assimilation ASSIMILATION_STINGING = new Assimilation(13, "ASSIMILATION_STINGING", "ASSIMILATION_STINGING_DESC", ASSIMILATED_STINGING, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_STINGING); }
        // TODO asset: icons/assimilations/Army.png; icons/species/Army.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_SWARMING — not implemented
        public static final Assimilation ASSIMILATION_SWARMING = new Assimilation(14, "ASSIMILATION_SWARMING", "ASSIMILATION_SWARMING_DESC", ASSIMILATED_SWARMING, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_SWARMING); }
        // TODO asset: icons/assimilations/Ghost.png; icons/species/Ghost.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_STEALTH — not implemented
        public static final Assimilation ASSIMILATION_STEALTH = new Assimilation(15, "ASSIMILATION_STEALTH", "ASSIMILATION_STEALTH_DESC", ASSIMILATED_STEALTH, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_STEALTH); }
        // TODO asset: icons/assimilations/Dracula.png; icons/species/Dracula.png (placeholder — replace final art)
        public static final Assimilation ASSIMILATION_FASTBITE = new Assimilation(16, "ASSIMILATION_FASTBITE", "ASSIMILATION_FASTBITE_DESC", ASSIMILATED_FASTBITE, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_FASTBITE); }
        // TODO asset: icons/assimilations/Silver.png; icons/species/Silver.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_HEATRESIST — not implemented
        public static final Assimilation ASSIMILATION_HEATRESIST = new Assimilation(17, "ASSIMILATION_HEATRESIST", "ASSIMILATION_HEATRESIST_DESC", ASSIMILATED_HEATRESIST, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_HEATRESIST); }
        // TODO asset: icons/assimilations/Maricopa.png; icons/species/Maricopa.png (placeholder — replace final art)
        public static final Assimilation ASSIMILATION_DEADLYVENOM = new Assimilation(18, "ASSIMILATION_DEADLYVENOM", "ASSIMILATION_DEADLYVENOM_DESC", ASSIMILATED_DEADLYVENOM, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_DEADLYVENOM); }
        // TODO asset: icons/assimilations/Exploding.png; icons/species/Exploding.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_SELFDESTRUCT — not implemented
        public static final Assimilation ASSIMILATION_SELFDESTRUCT = new Assimilation(19, "ASSIMILATION_SELFDESTRUCT", "ASSIMILATION_SELFDESTRUCT_DESC", ASSIMILATED_SELFDESTRUCT, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_SELFDESTRUCT); }
        // TODO asset: icons/assimilations/Bulldog.png; icons/species/Bulldog.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_FARSIGHT — not implemented
        public static final Assimilation ASSIMILATION_FARSIGHT = new Assimilation(20, "ASSIMILATION_FARSIGHT", "ASSIMILATION_FARSIGHT_DESC", ASSIMILATED_FARSIGHT, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_FARSIGHT); }
        // TODO asset: icons/assimilations/ShiningBlack.png; icons/species/ShiningBlack.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_HIVEBUILD — not implemented
        public static final Assimilation ASSIMILATION_HIVEBUILD = new Assimilation(21, "ASSIMILATION_HIVEBUILD", "ASSIMILATION_HIVEBUILD_DESC", ASSIMILATED_HIVEBUILD, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_HIVEBUILD); }
        // TODO asset: icons/assimilations/Desert.png; icons/species/Desert.png (placeholder — replace final art)
        // TODO mechanic: reward ASSIMILATED_LOCSENSE — not implemented
        public static final Assimilation ASSIMILATION_LOCSENSE = new Assimilation(22, "ASSIMILATION_LOCSENSE", "ASSIMILATION_LOCSENSE_DESC", ASSIMILATED_LOCSENSE, ASSIMILATION_COST, null);
        static { assimilations.add(ASSIMILATION_LOCSENSE); }

        // --- Synergies ---
        // TODO asset: icons/synergies/SuperVenom.png
        public static final Synergy SUPER_VENOM_SYNERGY = new Synergy(1, "SYNERGY_SUPER_VENOM", "SYNERGY_SUPER_VENOM_DESC",
                SYNERGY_SUPER_VENOM, ASSIMILATED_FIREVENOM, ASSIMILATED_DEADLYVENOM);
        static { synergies.add(SUPER_VENOM_SYNERGY); }

        // --- Getters ---
        public static List<Upgrade> getUpgrades() { return Collections.unmodifiableList(upgrades); }

        public static List<Building> getBuildings() { return Collections.unmodifiableList(buildings); }

        public static List<Synergy> getSynergies() { return Collections.unmodifiableList(synergies); }

        public static List<Assimilation> getAssimilations() { return Collections.unmodifiableList(assimilations); }
}
