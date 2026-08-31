package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.constants.unlocks.*;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Tier;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.assets.GameSpritePreloader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

public final class GameUnlocks {
        private GameUnlocks() {}

        private static final Map<String, ImageIcon> iconCache = new HashMap<>();

        private static ImageIcon loadIconOrNull(String path) {
                if (iconCache.containsKey(path)) {
                        return iconCache.get(path);
                }
                URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(path);
                if (resourceUrl == null) {
                        return null;
                }
                ImageIcon icon = new ImageIcon(resourceUrl);
                GameSpritePreloader.ensureLoaded(icon);
                iconCache.put(path, icon);
                return icon;
        }

        private static final ImageIcon ICON_TODO = loadIconOrNull("icons/misc/Unknown.png");

        private static ImageIcon loadIcon(String path) {
                ImageIcon icon = loadIconOrNull(path);
                if (icon != null) {
                        return icon;
                }
                System.err.println("CRITICAL ERROR: Resource not found: " + path);
                return ICON_TODO;
        }

        // --- Lists ---
        private static final List<Upgrade> upgrades = new ArrayList<>();
        private static final List<Building> buildings = new ArrayList<>();
        private static final List<Synergy> synergies = new ArrayList<>();
        private static final List<Assimilation> assimilations = new ArrayList<>();

        // --- Upgrades ---
        // -- Types --
        public static final Upgrade TYPE_EGG = new Upgrade(1, "TYPE_EGG", "TYPE_EGG_FLAVOR", "TYPE_EGG_DESC", null, 0, loadIcon("icons/upgrades/TypeEgg.png"), 0);
        static { upgrades.add(TYPE_EGG); }
        public static final Upgrade TYPE_WORKER = new Upgrade(2, "TYPE_WORKER", "TYPE_WORKER_FLAVOR", "TYPE_WORKER_DESC", TYPE_EGG, 0, loadIcon("icons/upgrades/TypeWorker.png"), 0);
        static { upgrades.add(TYPE_WORKER); }
        public static final Upgrade TYPE_SOLDIER = new Upgrade(3, "TYPE_SOLDIER", "TYPE_SOLDIER_FLAVOR", "TYPE_SOLDIER_DESC", TYPE_EGG, 100, loadIcon("icons/upgrades/TypeSoldier.png"), 1);
        static { upgrades.add(TYPE_SOLDIER); }  
        public static final Upgrade TYPE_MAJOR = new Upgrade(4, "TYPE_MAJOR", "TYPE_MAJOR_FLAVOR", "TYPE_MAJOR_DESC", TYPE_SOLDIER, 0, loadIcon("icons/upgrades/TypeMajor.png"), 0);
        static { upgrades.add(TYPE_MAJOR); }
        public static final Upgrade TYPE_PRINCESS = new Upgrade(5, "TYPE_PRINCESS_DRONE", "TYPE_PRINCESS_FLAVOR", "TYPE_PRINCESS_DESC", TYPE_EGG, 1000, loadIcon("icons/upgrades/TypePrincess.png"), 1);
        static { upgrades.add(TYPE_PRINCESS); }
        public static final Upgrade TYPE_QUEEN = new Upgrade(6, "TYPE_QUEEN", "TYPE_QUEEN_FLAVOR", "TYPE_QUEEN_DESC", TYPE_PRINCESS, 0, loadIcon("icons/upgrades/TypeQueen.png"), 1);
        static { upgrades.add(TYPE_QUEEN); }
        
        // -- Roles --
        public static final Upgrade ROLE_FORAGER = new Upgrade(7, "ROLE_FORAGER_UPGRADE", "ROLE_FORAGER_FLAVOR", "ROLE_FORAGER_DESC", TYPE_WORKER, 0, loadIcon("icons/upgrades/RoleForager.png"), 0);
        static { upgrades.add(ROLE_FORAGER); }
        public static final Upgrade ROLE_NURSE = new Upgrade(8, "ROLE_NURSE_UPGRADE", "ROLE_NURSE_FLAVOR", "ROLE_NURSE_DESC", TYPE_WORKER, 0, loadIcon("icons/upgrades/RoleNurse.png"), 0);
        static { upgrades.add(ROLE_NURSE); }
        public static final Upgrade ROLE_FARMER = new Upgrade(9, "ROLE_FARMER_UPGRADE", "ROLE_FARMER_FLAVOR", "ROLE_FARMER_DESC", TYPE_WORKER, 0, loadIcon("icons/upgrades/RoleFarmer.png"), 0);
        static { upgrades.add(ROLE_FARMER); }
        public static final Upgrade ROLE_GRAVER = new Upgrade(10, "ROLE_GRAVER_UPGRADE", "ROLE_GRAVER_FLAVOR", "ROLE_GRAVER_DESC", TYPE_WORKER, 0, loadIcon("icons/upgrades/RoleGraver.png"), 0);
        static { upgrades.add(ROLE_GRAVER); }
        public static final Upgrade ROLE_HUNTER = new Upgrade(11, "ROLE_HUNTER_UPGRADE", "ROLE_HUNTER_FLAVOR", "ROLE_HUNTER_DESC", TYPE_SOLDIER, 0, loadIcon("icons/upgrades/RoleHunter.png"), 0);
        static { upgrades.add(ROLE_HUNTER); }
        public static final Upgrade ROLE_LAYER = new Upgrade(12, "ROLE_LAYER_UPGRADE", "ROLE_LAYER_FLAVOR", "ROLE_LAYER_DESC", TYPE_QUEEN, 0, loadIcon("icons/upgrades/RoleLayer.png"), 0);
        static { upgrades.add(ROLE_LAYER); }
        public static final Upgrade ROLE_RANCHER = new Upgrade(13, "ROLE_RANCHER_UPGRADE", "ROLE_RANCHER_FLAVOR", "ROLE_RANCHER_DESC", ROLE_FORAGER, 250, loadIcon("icons/upgrades/RoleRancher.png"), 1);
        static { upgrades.add(ROLE_RANCHER); }
        public static final Upgrade ROLE_RESEARCHER = new Upgrade(14, "ROLE_RESEARCHER_UPGRADE", "ROLE_RESEARCHER_FLAVOR", "ROLE_RESEARCHER_DESC", TYPE_QUEEN, 0, loadIcon("icons/upgrades/RoleResearcher.png"), 0);
        static { upgrades.add(ROLE_RESEARCHER); }
        public static final Upgrade ROLE_COMMANDER = new Upgrade(40, "ROLE_COMMANDER_UPGRADE", "ROLE_COMMANDER_FLAVOR", "ROLE_COMMANDER_DESC", TYPE_QUEEN, 0, loadIcon("icons/upgrades/RoleCommander.png"), 0);
        static { upgrades.add(ROLE_COMMANDER); }
        public static final Upgrade ROLE_CAPTAIN = new Upgrade(41, "ROLE_CAPTAIN_UPGRADE", "ROLE_CAPTAIN_FLAVOR", "ROLE_CAPTAIN_DESC", TYPE_PRINCESS, 0, loadIcon("icons/upgrades/RoleCaptain.png"), 1);
        static { upgrades.add(ROLE_CAPTAIN); }
        public static final Upgrade ROLE_AIR_SUPPORT = new Upgrade(42, "ROLE_AIR_SUPPORT_UPGRADE", "ROLE_AIR_SUPPORT_FLAVOR", "ROLE_AIR_SUPPORT_DESC", TYPE_PRINCESS, 0, loadIcon("icons/upgrades/RoleAirSupport.png"), 1);
        static { upgrades.add(ROLE_AIR_SUPPORT); }
        public static final Upgrade ROLE_AIR_BOMBER = new Upgrade(43, "ROLE_AIR_BOMBER_UPGRADE", "ROLE_AIR_BOMBER_FLAVOR", "ROLE_AIR_BOMBER_DESC", TYPE_PRINCESS, 0, loadIcon("icons/upgrades/RoleAirBomber.png"), 1);
        static { upgrades.add(ROLE_AIR_BOMBER); }
        public static final Upgrade ROLE_BUILDER = new Upgrade(15, "ROLE_BUILDER_UPGRADE", "ROLE_BUILDER_FLAVOR", "ROLE_BUILDER_DESC", TYPE_WORKER, 150, loadIcon("icons/upgrades/RoleBuilder.png"), 1);
        static { upgrades.add(ROLE_BUILDER); }
        public static final Upgrade ROLE_SCOUT = new Upgrade(16, "ROLE_SCOUT_UPGRADE", "ROLE_SCOUT_FLAVOR", "ROLE_SCOUT_DESC", TYPE_WORKER, 0, loadIcon("icons/upgrades/RoleScout.png"), 0);
        static { upgrades.add(ROLE_SCOUT); }
        public static final Upgrade ROLE_MINER = new Upgrade(17, "ROLE_MINER_UPGRADE", "ROLE_MINER_FLAVOR", "ROLE_MINER_DESC", TYPE_SOLDIER, 0, loadIcon("icons/upgrades/RoleMiner.png"), 0);
        static { upgrades.add(ROLE_MINER); }
        public static final Upgrade ROLE_POTTER = new Upgrade(19, "ROLE_POTTER_UPGRADE", "ROLE_POTTER_FLAVOR", "ROLE_POTTER_DESC", TYPE_WORKER, 0, loadIcon("icons/upgrades/RolePotter.png"), 0);
        static { upgrades.add(ROLE_POTTER); }
        public static final Upgrade ROLE_WARRIOR = new Upgrade(21, "ROLE_WARRIOR_UPGRADE", "ROLE_WARRIOR_FLAVOR", "ROLE_WARRIOR_DESC", TYPE_SOLDIER, 0, loadIcon("icons/upgrades/RoleWarrior.png"), 0);
        static { upgrades.add(ROLE_WARRIOR); }
        public static final Upgrade ROLE_DEFENDER = new Upgrade(22, "ROLE_DEFENDER_UPGRADE", "ROLE_DEFENDER_FLAVOR", "ROLE_DEFENDER_DESC", TYPE_SOLDIER, 0, loadIcon("icons/upgrades/RoleDefender.png"), 0);
        static { upgrades.add(ROLE_DEFENDER); }
        public static final Upgrade ROLE_POLICE = new Upgrade(23, "ROLE_POLICE_UPGRADE", "ROLE_POLICE_FLAVOR", "ROLE_POLICE_DESC", TYPE_SOLDIER, 0, loadIcon("icons/upgrades/RolePolice.png"), 0); 
        static { upgrades.add(ROLE_POLICE); }
        public static final Upgrade ROLE_BOMBER = new Upgrade(24, "ROLE_BOMBER_UPGRADE", "ROLE_BOMBER_FLAVOR", "ROLE_BOMBER_DESC", TYPE_SOLDIER, 0, loadIcon("icons/upgrades/RoleBomber.png"), 0);
        static { upgrades.add(ROLE_BOMBER); }
        public static final Upgrade ROLE_BRUTE = new Upgrade(25, "ROLE_BRUTE_UPGRADE", "ROLE_BRUTE_FLAVOR", "ROLE_BRUTE_DESC", TYPE_MAJOR, 0, loadIcon("icons/upgrades/RoleBrute.png"), 0);
        static { upgrades.add(ROLE_BRUTE); }
        public static final Upgrade ROLE_CARRIER = new Upgrade(26, "ROLE_CARRIER_UPGRADE", "ROLE_CARRIER_FLAVOR", "ROLE_CARRIER_DESC", TYPE_MAJOR, 0, loadIcon("icons/upgrades/RoleCarrier.png"), 0);
        static { upgrades.add(ROLE_CARRIER); }
        public static final Upgrade ROLE_ARTILLERY = new Upgrade(27, "ROLE_ARTILLERY_UPGRADE", "ROLE_ARTILLERY_FLAVOR", "ROLE_ARTILLERY_DESC", TYPE_MAJOR, 0, loadIcon("icons/upgrades/RoleArtillery.png"), 0);
        static { upgrades.add(ROLE_ARTILLERY); }
        public static final Upgrade ROLE_SIEGE = new Upgrade(28, "ROLE_SIEGE_UPGRADE", "ROLE_SIEGE_FLAVOR", "ROLE_SIEGE_DESC", TYPE_MAJOR, 200000, loadIcon("icons/upgrades/RoleSiege.png"), 4);
        static { upgrades.add(ROLE_SIEGE); }
        public static final Upgrade ROLE_BREEDER = new Upgrade(30, "ROLE_BREEDER_UPGRADE", "ROLE_BREEDER_FLAVOR", "ROLE_BREEDER_DESC", TYPE_PRINCESS, 0, loadIcon("icons/upgrades/RoleBreeder.png"), 1);
        static { upgrades.add(ROLE_BREEDER); }
        public static final Upgrade ROLE_DIPLOMAT = new Upgrade(31, "ROLE_DIPLOMAT_UPGRADE", "ROLE_DIPLOMAT_FLAVOR", "ROLE_DIPLOMAT_DESC", TYPE_PRINCESS, 1500, loadIcon("icons/upgrades/RoleDiplomat.png"), 3);
        static { upgrades.add(ROLE_DIPLOMAT); }
        public static final Upgrade ROLE_SPY = new Upgrade(44, "ROLE_SPY_UPGRADE", "ROLE_SPY_FLAVOR", "ROLE_SPY_DESC", TYPE_PRINCESS, 0, loadIcon("icons/upgrades/RoleSpy.png"), 0);
        static { upgrades.add(ROLE_SPY); }
        public static final Upgrade ROLE_MILITIA = new Upgrade(32, "ROLE_MILITIA_UPGRADE", "ROLE_MILITIA_FLAVOR", "ROLE_MILITIA_DESC", TYPE_WORKER, 0, loadIcon("icons/upgrades/RoleMilitia.png"), 0);
        static { upgrades.add(ROLE_MILITIA); }
        public static final Upgrade ROLE_CATCHER = new Upgrade(33, "ROLE_CATCHER_UPGRADE", "ROLE_CATCHER_FLAVOR", "ROLE_CATCHER_DESC", ROLE_HUNTER, 500, loadIcon("icons/upgrades/RoleCatcher.png"), 1);
        static { upgrades.add(ROLE_CATCHER); }
        public static final Upgrade ROLE_CRANE = new Upgrade(34, "ROLE_CRANE_UPGRADE", "ROLE_CRANE_FLAVOR", "ROLE_CRANE_DESC", TYPE_MAJOR, 0, loadIcon("icons/upgrades/RoleCrane.png"), 0);
        static { upgrades.add(ROLE_CRANE); }
        public static final Upgrade ROLE_ASSISTANT = new Upgrade(36, "ROLE_ASSISTANT_UPGRADE", "ROLE_ASSISTANT_FLAVOR", "ROLE_ASSISTANT_DESC", TYPE_PRINCESS, 1200, loadIcon("icons/upgrades/RoleAssistant.png"), 2);
        static { upgrades.add(ROLE_ASSISTANT); }

        // -- Abilities --
        public static final Upgrade ABILITY_RESEARCH = new Upgrade(201, "ABILITY_RESEARCH", "ABILITY_RESEARCH_FLAVOR", "ABILITY_RESEARCH_DESC", ROLE_RESEARCHER, 0, loadIcon("icons/upgrades/AbilityResearch.png"), 0);
        static { upgrades.add(ABILITY_RESEARCH); }
        public static final Upgrade ABILITY_BUILD = new Upgrade(202, "ABILITY_BUILD", "ABILITY_BUILD_FLAVOR", "ABILITY_BUILD_DESC", ROLE_BUILDER, 0, loadIcon("icons/upgrades/AbilityBuild.png"), 0);
        static { upgrades.add(ABILITY_BUILD); }
        public static final Upgrade ABILITY_SPREAD = new Upgrade(203, "ABILITY_SPREAD", "ABILITY_SPREAD_FLAVOR", "ABILITY_SPREAD_DESC", ROLE_BREEDER, 0, loadIcon("icons/upgrades/AbilitySpread.png"), 0);
        static { upgrades.add(ABILITY_SPREAD); }
        public static final Upgrade ABILITY_RESIN = new Upgrade(204, "ABILITY_RESIN", "ABILITY_RESIN_FLAVOR", "ABILITY_RESIN_DESC", ROLE_FORAGER, 5000, loadIcon("icons/upgrades/AbilityResin.png"), 2);
        static { upgrades.add(ABILITY_RESIN); }
        public static final Upgrade ABILITY_SYNERGY = new Upgrade(205, "ABILITY_SYNERGY", "ABILITY_SYNERGY_FLAVOR", "ABILITY_SYNERGY_DESC", ABILITY_RESEARCH, 0, loadIcon("icons/upgrades/AbilitySynergy.png"), 0);
        static { upgrades.add(ABILITY_SYNERGY); }
        public static final Upgrade ABILITY_ASSIMILATION = new Upgrade(206, "ABILITY_ASSIMILATION", "ABILITY_ASSIMILATION_FLAVOR", "ABILITY_ASSIMILATION_DESC", ABILITY_RESEARCH, 0, loadIcon("icons/upgrades/AbilityAssimilation.png"), 0);
        static { upgrades.add(ABILITY_ASSIMILATION); }
        public static final Upgrade ABILITY_FORCED_FLIGHT = new Upgrade(505, "ABILITY_FORCED_FLIGHT", "ABILITY_FORCED_FLIGHT_FLAVOR", "ABILITY_FORCED_FLIGHT_DESC", ROLE_BREEDER, 9000, loadIcon("icons/upgrades/AbilityForcedFlight.png"), 2);
        static { upgrades.add(ABILITY_FORCED_FLIGHT); }
        public static final Upgrade ABILITY_DYNASTY = new Upgrade(506, "ABILITY_DYNASTY", "ABILITY_DYNASTY_FLAVOR", "ABILITY_DYNASTY_DESC", ABILITY_SPREAD, 0, loadIcon("icons/upgrades/AbilityDynasty.png"), 0);
        static { upgrades.add(ABILITY_DYNASTY); }
        public static final Upgrade ABILITY_TRADE = new Upgrade(507, "ABILITY_TRADE", "ABILITY_TRADE_FLAVOR", "ABILITY_TRADE_DESC", ABILITY_DYNASTY, 0, loadIcon("icons/upgrades/AbilityTrade.png"), 0);
        static { upgrades.add(ABILITY_TRADE); }
        public static final Upgrade ABILITY_SPREAD_2 = new Upgrade(508, "ABILITY_SPREAD_2", "ABILITY_SPREAD_2_FLAVOR", "ABILITY_SPREAD_2_DESC", ABILITY_SPREAD, 0, loadIcon("icons/upgrades/AbilitySpread2.png"), 0);
        static { upgrades.add(ABILITY_SPREAD_2); }
        public static final Upgrade ABILITY_AUTOMATION = new Upgrade(509, "ABILITY_AUTOMATION", "ABILITY_AUTOMATION_FLAVOR", "ABILITY_AUTOMATION_DESC", ABILITY_DYNASTY, 0, loadIcon("icons/upgrades/AbilityAutomation.png"), 0);
        static { upgrades.add(ABILITY_AUTOMATION); }
        public static final Upgrade ABILITY_TUNNELS = new Upgrade(510, "ABILITY_TUNNELS", "ABILITY_TUNNELS_FLAVOR", "ABILITY_TUNNELS_DESC", ABILITY_TRADE, 100000, loadIcon("icons/upgrades/AbilityTunnels.png"), 4);
        static { upgrades.add(ABILITY_TUNNELS); }
        public static final Upgrade ABILITY_BILATERAL_TRADE = new Upgrade(513, "ABILITY_BILATERAL_TRADE", "ABILITY_BILATERAL_TRADE_FLAVOR", "ABILITY_BILATERAL_TRADE_DESC", ABILITY_TRADE, 0, loadIcon("icons/upgrades/AbilityBilateralTrade.png"), 0);
        static { upgrades.add(ABILITY_BILATERAL_TRADE); }
        public static final Upgrade ABILITY_MANAGEMENT = new Upgrade(511, "ABILITY_MANAGEMENT", "ABILITY_MANAGEMENT_FLAVOR", "ABILITY_MANAGEMENT_DESC", ABILITY_DYNASTY, 0, loadIcon("icons/upgrades/AbilityManagement.png"), 0);
        static { upgrades.add(ABILITY_MANAGEMENT); }
        public static final Upgrade ABILITY_MASS_FLIGHT = new Upgrade(512, "ABILITY_MASS_FLIGHT", "ABILITY_MASS_FLIGHT_FLAVOR", "ABILITY_MASS_FLIGHT_DESC", ABILITY_FORCED_FLIGHT, 0, loadIcon("icons/upgrades/AbilityMassFlight.png"), 1);
        static { upgrades.add(ABILITY_MASS_FLIGHT); }
        public static final Upgrade ABILITY_CLONING = new Upgrade(514, "ABILITY_CLONING", "ABILITY_CLONING_FLAVOR", "ABILITY_CLONING_DESC", ABILITY_RESEARCH, 0, loadIcon("icons/upgrades/AbilityCloning.png"), 0);
        static { upgrades.add(ABILITY_CLONING); }
        public static final Upgrade ABILITY_ABILITY = new Upgrade(900, "ABILITY_ABILITY", "ABILITY_ABILITY_FLAVOR", "ABILITY_ABILITY_DESC", TYPE_QUEEN, 0, loadIcon("icons/upgrades/AbilityAbility.png"), 0);
        static { upgrades.add(ABILITY_ABILITY); }
        public static final Upgrade ABILITY_PARASITIC_MITE_ALERT = new Upgrade(515, "ABILITY_PARASITIC_MITE_ALERT", "ABILITY_PARASITIC_MITE_ALERT_FLAVOR", "ABILITY_PARASITIC_MITE_ALERT_DESC", ABILITY_ABILITY, 0, loadIcon("icons/upgrades/AbilityParasiticMiteAlert.png"), 0);
        static { upgrades.add(ABILITY_PARASITIC_MITE_ALERT); }
        public static final Upgrade ABILITY_CATCH_SYMBIOTIC_MITE = new Upgrade(516, "ABILITY_CATCH_SYMBIOTIC_MITE", "ABILITY_CATCH_SYMBIOTIC_MITE_FLAVOR", "ABILITY_CATCH_SYMBIOTIC_MITE_DESC", ABILITY_ABILITY, 0, loadIcon("icons/upgrades/AbilityCatchSymbioticMite.png"), 0);
        static { upgrades.add(ABILITY_CATCH_SYMBIOTIC_MITE); }
        public static final Upgrade ABILITY_CATCH_DERMESTID = new Upgrade(517, "ABILITY_CATCH_DERMESTID", "ABILITY_CATCH_DERMESTID_FLAVOR", "ABILITY_CATCH_DERMESTID_DESC", ROLE_GRAVER, 2000, loadIcon("icons/upgrades/AbilityCatchDermestid.png"), 2);
        static { upgrades.add(ABILITY_CATCH_DERMESTID); }
        public static final Upgrade ABILITY_DIPLOMAT_PRESSURE_2 = new Upgrade(518, "ABILITY_DIPLOMAT_PRESSURE_2", "ABILITY_DIPLOMAT_PRESSURE_2_FLAVOR", "ABILITY_DIPLOMAT_PRESSURE_2_DESC", ROLE_DIPLOMAT, 2500, loadIcon("icons/upgrades/AbilityDiplomatPressure2.png"), 4);
        static { upgrades.add(ABILITY_DIPLOMAT_PRESSURE_2); }
        public static final Upgrade ABILITY_DIPLOMAT_PRESSURE_3 = new Upgrade(519, "ABILITY_DIPLOMAT_PRESSURE_3", "ABILITY_DIPLOMAT_PRESSURE_3_FLAVOR", "ABILITY_DIPLOMAT_PRESSURE_3_DESC", ABILITY_DIPLOMAT_PRESSURE_2, 10000, loadIcon("icons/upgrades/AbilityDiplomatPressure3.png"), 5);
        static { upgrades.add(ABILITY_DIPLOMAT_PRESSURE_3); }
        public static final Upgrade ABILITY_PHEROMONE_STORM = new Upgrade(520, "ABILITY_PHEROMONE_STORM", "ABILITY_PHEROMONE_STORM_FLAVOR", "ABILITY_PHEROMONE_STORM_DESC", ABILITY_ABILITY, 4000, loadIcon("icons/upgrades/AbilityPheromoneStorm.png"), 2);
        static { upgrades.add(ABILITY_PHEROMONE_STORM); }
        public static final Upgrade ABILITY_CREATINE_DIET = new Upgrade(521, "ABILITY_CREATINE_DIET", "ABILITY_CREATINE_DIET_FLAVOR", "ABILITY_CREATINE_DIET_DESC", ABILITY_ABILITY, 4000, loadIcon("icons/upgrades/AbilityCreatineDiet.png"), 2);
        static { upgrades.add(ABILITY_CREATINE_DIET); }
        public static final Upgrade ABILITY_AUTO_TUNNELS = new Upgrade(522, "ABILITY_AUTO_TUNNELS", "ABILITY_AUTO_TUNNELS_FLAVOR", "ABILITY_AUTO_TUNNELS_DESC", ABILITY_AUTOMATION, 0, loadIcon("icons/upgrades/AbilityAutoTunnels.png"), 0);
        static { upgrades.add(ABILITY_AUTO_TUNNELS); }
        public static final Upgrade ABILITY_AUTO_DIPLOMACY = new Upgrade(523, "ABILITY_AUTO_DIPLOMACY", "ABILITY_AUTO_DIPLOMACY_FLAVOR", "ABILITY_AUTO_DIPLOMACY_DESC", ABILITY_AUTOMATION, 0, loadIcon("icons/upgrades/AbilityAutoDiplomacy.png"), 0);
        static { upgrades.add(ABILITY_AUTO_DIPLOMACY); }
        public static final Upgrade ABILITY_AUTO_LOGISTICS = new Upgrade(525, "ABILITY_AUTO_LOGISTICS", "ABILITY_AUTO_LOGISTICS_FLAVOR", "ABILITY_AUTO_LOGISTICS_DESC", ABILITY_AUTOMATION, 0, loadIcon("icons/upgrades/AbilityAutoLogistics.png"), 0);
        static { upgrades.add(ABILITY_AUTO_LOGISTICS); }
        public static final Upgrade ABILITY_JUMPING = new Upgrade(526, "ABILITY_JUMPING", "ABILITY_JUMPING_FLAVOR", "ABILITY_JUMPING_DESC", ABILITY_ABILITY, 0, loadIcon("icons/upgrades/AssimilatedJumping.png"), 0);
        static { upgrades.add(ABILITY_JUMPING); }
        public static final Upgrade ABILITY_SWARMING = new Upgrade(527, "ABILITY_SWARMING", "ABILITY_SWARMING_FLAVOR", "ABILITY_SWARMING_DESC", ABILITY_ABILITY, 0, loadIcon("icons/upgrades/AssimilatedSwarming.png"), 0);
        static { upgrades.add(ABILITY_SWARMING); }
        public static final Upgrade ABILITY_HUNTS = new Upgrade(530, "ABILITY_HUNTS", "ABILITY_HUNTS_FLAVOR", "ABILITY_HUNTS_DESC", ROLE_SCOUT, 0, loadIcon("icons/upgrades/RoleScout.png"), 0);
        static { upgrades.add(ABILITY_HUNTS); }

        public static final Upgrade ABILITY_SUBTYPE_HATCH = new Upgrade(524, "ABILITY_SUBTYPE_HATCH", "ABILITY_SUBTYPE_HATCH_FLAVOR", "ABILITY_SUBTYPE_HATCH_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AbilitySubtypeHatch.png"), 0);
        static { upgrades.add(ABILITY_SUBTYPE_HATCH); }

        public static boolean meetsExtraAutomationPrerequisites(Dynasty dynasty, Upgrade upgrade) {
            if (upgrade == ABILITY_AUTO_TUNNELS) {
                return dynasty != null && dynasty.meetsAutoTunnelsPrerequisites();
            }
            if (upgrade == ABILITY_AUTO_DIPLOMACY) {
                return dynasty != null && dynasty.meetsAutoDiplomacyPrerequisites();
            }
            if (upgrade == ABILITY_AUTO_LOGISTICS) {
                return dynasty != null && dynasty.meetsAutoLogisticsPrerequisites();
            }
            return true;
        }

        // -- Advanced Roles --
        public static final Upgrade ROLE_COURIER = new Upgrade(18, "ROLE_COURIER_UPGRADE", "ROLE_COURIER_FLAVOR", "ROLE_COURIER_DESC", ABILITY_TRADE, 0, loadIcon("icons/upgrades/RoleCourier.png"), 0);
        static { upgrades.add(ROLE_COURIER); }
        public static final Upgrade ROLE_BORER = new Upgrade(29, "ROLE_BORER_UPGRADE", "ROLE_BORER_FLAVOR", "ROLE_BORER_DESC", ABILITY_TUNNELS, 0, loadIcon("icons/upgrades/RoleBorer.png"), 3);
        static { upgrades.add(ROLE_BORER); }
        public static final Upgrade ROLE_TRANSPORT = new Upgrade(35, "ROLE_TRANSPORT_UPGRADE", "ROLE_TRANSPORT_FLAVOR", "ROLE_TRANSPORT_DESC", ROLE_COURIER, 100000, loadIcon("icons/upgrades/RoleTransport.png"), 4);
        static { upgrades.add(ROLE_TRANSPORT); }
        public static final Upgrade ROLE_ESCORT = new Upgrade(37, "ROLE_ESCORT_UPGRADE", "ROLE_ESCORT_FLAVOR", "ROLE_ESCORT_DESC", ROLE_COURIER, 80000, loadIcon("icons/upgrades/RoleEscort.png"), 3);
        static { upgrades.add(ROLE_ESCORT); }
        public static final Upgrade ROLE_ENGINEER = new Upgrade(38, "ROLE_ENGINEER_UPGRADE", "ROLE_ENGINEER_FLAVOR", "ROLE_ENGINEER_DESC", ROLE_BORER, 50000, loadIcon("icons/upgrades/RoleEngineer.png"), 3);
        static { upgrades.add(ROLE_ENGINEER); }
        public static final Upgrade ROLE_SKYTRANS = new Upgrade(39, "ROLE_SKYTRANS_UPGRADE", "ROLE_SKYTRANS_FLAVOR", "ROLE_SKYTRANS_DESC", ROLE_COURIER, 150000, loadIcon("icons/upgrades/RoleSkytrans.png"), 4);
        static { upgrades.add(ROLE_SKYTRANS); }

        // -- Stats -- 
        // TODO asset: icons/upgrades/StatSkeleton.png
        public static final Upgrade STAT_SKELETON = new Upgrade(101, "STAT_SKELETON", "STAT_SKELETON_FLAVOR", "STAT_SKELETON_DESC", TYPE_EGG, 0, ICON_TODO, 0);
        static { upgrades.add(STAT_SKELETON); }
        // TODO asset: icons/upgrades/StatAcid.png
        public static final Upgrade STAT_ACID = new Upgrade(102, "STAT_ACID", "STAT_ACID_FLAVOR", "STAT_ACID_DESC", TYPE_EGG, 0, ICON_TODO, 0);
        static { upgrades.add(STAT_ACID); }
        // TODO asset: icons/upgrades/StatLongevity.png
        public static final Upgrade STAT_LONGEVITY = new Upgrade(103, "STAT_LONGEVITY", "STAT_LONGEVITY_FLAVOR", "STAT_LONGEVITY_DESC", TYPE_EGG, 0, ICON_TODO, 0);
        static { upgrades.add(STAT_LONGEVITY); }
        // TODO asset: icons/upgrades/StatResearch1.png
        public static final Upgrade STAT_RESEARCH_1 = new Upgrade(104, "STAT_RESEARCH_1", "STAT_RESEARCH_1_FLAVOR", "STAT_RESEARCH_1_DESC", ROLE_RESEARCHER, 500, ICON_TODO, 1);
        static { upgrades.add(STAT_RESEARCH_1); }
        // TODO asset: icons/upgrades/StatResearch2.png
        public static final Upgrade STAT_RESEARCH_2 = new Upgrade(105, "STAT_RESEARCH_2", "STAT_RESEARCH_2_FLAVOR", "STAT_RESEARCH_2_DESC", STAT_RESEARCH_1, 2000, ICON_TODO, 2);
        static { upgrades.add(STAT_RESEARCH_2); }
        // TODO asset: icons/upgrades/StatResearch3.png
        public static final Upgrade STAT_RESEARCH_3 = new Upgrade(106, "STAT_RESEARCH_3", "STAT_RESEARCH_3_FLAVOR", "STAT_RESEARCH_3_DESC", STAT_RESEARCH_2, 7500, ICON_TODO, 3);
        static { upgrades.add(STAT_RESEARCH_3); }
        // TODO asset: icons/upgrades/StatGrowth1.png
        public static final Upgrade STAT_GROWTH_1 = new Upgrade(107, "STAT_GROWTH_1", "STAT_GROWTH_1_FLAVOR", "STAT_GROWTH_1_DESC", TYPE_EGG, 3500, ICON_TODO, 2);
        static { upgrades.add(STAT_GROWTH_1); }
        // TODO asset: icons/upgrades/StatGrowth2.png
        public static final Upgrade STAT_GROWTH_2 = new Upgrade(108, "STAT_GROWTH_2", "STAT_GROWTH_2_FLAVOR", "STAT_GROWTH_2_DESC", STAT_GROWTH_1, 9000, ICON_TODO, 3);
        static { upgrades.add(STAT_GROWTH_2); }
        // TODO asset: icons/upgrades/StatGrowth3.png
        public static final Upgrade STAT_GROWTH_3 = new Upgrade(109, "STAT_GROWTH_3", "STAT_GROWTH_3_FLAVOR", "STAT_GROWTH_3_DESC", STAT_GROWTH_2, 20000, ICON_TODO, 4);
        static { upgrades.add(STAT_GROWTH_3); }
        // TODO asset: icons/upgrades/StatThirst1.png
        public static final Upgrade STAT_THIRST_1 = new Upgrade(110, "STAT_THIRST_1", "STAT_THIRST_1_FLAVOR", "STAT_THIRST_1_DESC", STAT_LONGEVITY, 1500, ICON_TODO, 2);
        static { upgrades.add(STAT_THIRST_1); }
        // TODO asset: icons/upgrades/StatThirst2.png
        public static final Upgrade STAT_THIRST_2 = new Upgrade(111, "STAT_THIRST_2", "STAT_THIRST_2_FLAVOR", "STAT_THIRST_2_DESC", STAT_THIRST_1, 5000, ICON_TODO, 3);
        static { upgrades.add(STAT_THIRST_2); }
        // TODO asset: icons/upgrades/StatLogistics1.png
        public static final Upgrade STAT_LOGISTICS_1 = new Upgrade(113, "STAT_LOGISTICS_1", "STAT_LOGISTICS_1_FLAVOR", "STAT_LOGISTICS_1_DESC", ROLE_SCOUT, 600, ICON_TODO, 1);
        static { upgrades.add(STAT_LOGISTICS_1); }
        // TODO asset: icons/upgrades/StatPassive1.png
        public static final Upgrade STAT_PASSIVE_1 = new Upgrade(114, "STAT_PASSIVE_1", "STAT_PASSIVE_1_FLAVOR", "STAT_PASSIVE_1_DESC", STAT_RESEARCH_3, 25000, ICON_TODO, 4);
        static { upgrades.add(STAT_PASSIVE_1); }
        // TODO asset: icons/upgrades/StatLaying1.png
        public static final Upgrade STAT_LAYING_1 = new Upgrade(115, "STAT_LAYING_1", "STAT_LAYING_1_FLAVOR", "STAT_LAYING_1_DESC", ROLE_LAYER, 5000, ICON_TODO, 2);
        static { upgrades.add(STAT_LAYING_1); }
        // TODO asset: icons/upgrades/StatLaying2.png
        public static final Upgrade STAT_LAYING_2 = new Upgrade(116, "STAT_LAYING_2", "STAT_LAYING_2_FLAVOR", "STAT_LAYING_2_DESC", STAT_LAYING_1, 15000, ICON_TODO, 3);
        static { upgrades.add(STAT_LAYING_2); }
        // TODO asset: icons/upgrades/StatLaying3.png
        public static final Upgrade STAT_LAYING_3 = new Upgrade(117, "STAT_LAYING_3", "STAT_LAYING_3_FLAVOR", "STAT_LAYING_3_DESC", STAT_LAYING_2, 80000, ICON_TODO, 4);
        static { upgrades.add(STAT_LAYING_3); }
        // TODO asset: icons/upgrades/StatScouting1.png
        public static final Upgrade STAT_SCOUTING_1 = new Upgrade(118, "STAT_SCOUTING_1", "STAT_SCOUTING_1_FLAVOR", "STAT_SCOUTING_1_DESC", ROLE_SCOUT, 4000, ICON_TODO, 2);
        static { upgrades.add(STAT_SCOUTING_1); }
        // TODO asset: icons/upgrades/StatScouting2.png
        public static final Upgrade STAT_SCOUTING_2 = new Upgrade(119, "STAT_SCOUTING_2", "STAT_SCOUTING_2_FLAVOR", "STAT_SCOUTING_2_DESC", STAT_SCOUTING_1, 15000, ICON_TODO, 3);
        static { upgrades.add(STAT_SCOUTING_2); }
        // TODO asset: icons/upgrades/StatScouting3.png
        public static final Upgrade STAT_SCOUTING_3 = new Upgrade(120, "STAT_SCOUTING_3", "STAT_SCOUTING_3_FLAVOR", "STAT_SCOUTING_3_DESC", STAT_SCOUTING_2, 35000, ICON_TODO, 4);
        static { upgrades.add(STAT_SCOUTING_3); }
        // TODO asset: icons/upgrades/StatFarming1.png
        public static final Upgrade STAT_FARMING_1 = new Upgrade(121, "STAT_FARMING_1", "STAT_FARMING_1_FLAVOR", "STAT_FARMING_1_DESC", ROLE_FARMER, 4500, ICON_TODO, 2);
        static { upgrades.add(STAT_FARMING_1); }
        // TODO asset: icons/upgrades/StatFarming2.png
        public static final Upgrade STAT_FARMING_2 = new Upgrade(122, "STAT_FARMING_2", "STAT_FARMING_2_FLAVOR", "STAT_FARMING_2_DESC", STAT_FARMING_1, 16000, ICON_TODO, 3);
        static { upgrades.add(STAT_FARMING_2); }
        // TODO asset: icons/upgrades/StatFarming3.png
        public static final Upgrade STAT_FARMING_3 = new Upgrade(123, "STAT_FARMING_3", "STAT_FARMING_3_FLAVOR", "STAT_FARMING_3_DESC", STAT_FARMING_2, 60000, ICON_TODO, 4);
        static { upgrades.add(STAT_FARMING_3); }
        // TODO asset: icons/upgrades/StatContamination1.png
        public static final Upgrade STAT_CONTAMINATION_1 = new Upgrade(124, "STAT_CONTAMINATION_1", "STAT_CONTAMINATION_1_FLAVOR", "STAT_CONTAMINATION_1_DESC", ROLE_GRAVER, 2000, ICON_TODO, 2);
        static { upgrades.add(STAT_CONTAMINATION_1); }
        // TODO asset: icons/upgrades/StatContamination2.png
        public static final Upgrade STAT_CONTAMINATION_2 = new Upgrade(125, "STAT_CONTAMINATION_2", "STAT_CONTAMINATION_2_FLAVOR", "STAT_CONTAMINATION_2_DESC", STAT_CONTAMINATION_1, 8000, ICON_TODO, 3);
        static { upgrades.add(STAT_CONTAMINATION_2); }
        // TODO asset: icons/upgrades/StatContamination3.png
        public static final Upgrade STAT_CONTAMINATION_3 = new Upgrade(126, "STAT_CONTAMINATION_3", "STAT_CONTAMINATION_3_FLAVOR", "STAT_CONTAMINATION_3_DESC", STAT_CONTAMINATION_2, 25000, ICON_TODO, 4);
        static { upgrades.add(STAT_CONTAMINATION_3); }
        // TODO asset: icons/upgrades/StatSymbioticMite1.png
        public static final Upgrade STAT_SYMBIOTIC_MITE_1 = new Upgrade(135, "STAT_SYMBIOTIC_MITE_1", "STAT_SYMBIOTIC_MITE_1_FLAVOR", "STAT_SYMBIOTIC_MITE_1_DESC", ABILITY_CATCH_SYMBIOTIC_MITE, 2000, ICON_TODO, 2);
        static { upgrades.add(STAT_SYMBIOTIC_MITE_1); }
        // TODO asset: icons/upgrades/StatDermestid1.png
        public static final Upgrade STAT_DERMESTID_1 = new Upgrade(136, "STAT_DERMESTID_1", "STAT_DERMESTID_1_FLAVOR", "STAT_DERMESTID_1_DESC", ABILITY_CATCH_DERMESTID, 2000, ICON_TODO, 3);
        static { upgrades.add(STAT_DERMESTID_1); }
        // TODO asset: icons/upgrades/StatGraving1.png
        public static final Upgrade STAT_GRAVING_1 = new Upgrade(127, "STAT_GRAVING_1", "STAT_GRAVING_1_FLAVOR", "STAT_GRAVING_1_DESC", ROLE_GRAVER, 1500, ICON_TODO, 2);
        static { upgrades.add(STAT_GRAVING_1); }
        // TODO asset: icons/upgrades/StatGraving2.png
        public static final Upgrade STAT_GRAVING_2 = new Upgrade(128, "STAT_GRAVING_2", "STAT_GRAVING_2_FLAVOR", "STAT_GRAVING_2_DESC", STAT_GRAVING_1, 6500, ICON_TODO, 3);
        static { upgrades.add(STAT_GRAVING_2); }
        // TODO asset: icons/upgrades/StatGraving3.png
        public static final Upgrade STAT_GRAVING_3 = new Upgrade(129, "STAT_GRAVING_3", "STAT_GRAVING_3_FLAVOR", "STAT_GRAVING_3_DESC", STAT_GRAVING_2, 40000, ICON_TODO, 4);
        static { upgrades.add(STAT_GRAVING_3); }
        // TODO asset: icons/upgrades/StatPolicing1.png
        public static final Upgrade STAT_POLICING_1 = new Upgrade(130, "STAT_POLICING_1", "STAT_POLICING_1_FLAVOR", "STAT_POLICING_1_DESC", ROLE_POLICE, 3000, ICON_TODO, 2);
        static { upgrades.add(STAT_POLICING_1); }
        // TODO asset: icons/upgrades/StatPolicing2.png
        public static final Upgrade STAT_POLICING_2 = new Upgrade(131, "STAT_POLICING_2", "STAT_POLICING_2_FLAVOR", "STAT_POLICING_2_DESC", STAT_POLICING_1, 14000, ICON_TODO, 3);
        static { upgrades.add(STAT_POLICING_2); }
        // TODO asset: icons/upgrades/StatPolicing3.png
        public static final Upgrade STAT_POLICING_3 = new Upgrade(132, "STAT_POLICING_3", "STAT_POLICING_3_FLAVOR", "STAT_POLICING_3_DESC", STAT_POLICING_2, 42000, ICON_TODO, 4);
        static { upgrades.add(STAT_POLICING_3); }
        // TODO asset: icons/upgrades/StatHexSustain.png
        public static final Upgrade STAT_HEX_SUSTAIN = new Upgrade(133, "STAT_HEX_SUSTAIN", "STAT_HEX_SUSTAIN_FLAVOR", "STAT_HEX_SUSTAIN_DESC", STAT_FARMING_3, 175000, ICON_TODO, 5);
        static { upgrades.add(STAT_HEX_SUSTAIN); }
        // TODO asset: icons/upgrades/StatWorkerSpeed2.png
        public static final Upgrade STAT_WORKER_SPEED_2 = new Upgrade(134, "STAT_WORKER_SPEED_2", "STAT_WORKER_SPEED_2_FLAVOR", "STAT_WORKER_SPEED_2_DESC", STAT_SCOUTING_3, 200000, ICON_TODO, 5);
        static { upgrades.add(STAT_WORKER_SPEED_2); }
        // TODO asset: icons/upgrades/StatHealth1.png
        public static final Upgrade STAT_HEALTH_1 = new Upgrade(137, "STAT_HEALTH_1", "STAT_HEALTH_1_FLAVOR", "STAT_HEALTH_1_DESC", STAT_SKELETON, 12000, ICON_TODO, 3);
        static { upgrades.add(STAT_HEALTH_1); }
        // TODO asset: icons/upgrades/StatAttack1.png
        public static final Upgrade STAT_ATTACK_1 = new Upgrade(138, "STAT_ATTACK_1", "STAT_ATTACK_1_FLAVOR", "STAT_ATTACK_1_DESC", STAT_ACID, 12000, ICON_TODO, 3);
        static { upgrades.add(STAT_ATTACK_1); }
        // TODO asset: icons/upgrades/StatDefense1.png
        public static final Upgrade STAT_DEFENSE_1 = new Upgrade(139, "STAT_DEFENSE_1", "STAT_DEFENSE_1_FLAVOR", "STAT_DEFENSE_1_DESC", STAT_SKELETON, 10000, ICON_TODO, 3);
        static { upgrades.add(STAT_DEFENSE_1); }
        // TODO asset: icons/upgrades/StatHealth2.png
        public static final Upgrade STAT_HEALTH_2 = new Upgrade(140, "STAT_HEALTH_2", "STAT_HEALTH_2_FLAVOR", "STAT_HEALTH_2_DESC", STAT_HEALTH_1, 500000, ICON_TODO, 6);
        static { upgrades.add(STAT_HEALTH_2); }
        // TODO asset: icons/upgrades/StatAttack2.png
        public static final Upgrade STAT_ATTACK_2 = new Upgrade(141, "STAT_ATTACK_2", "STAT_ATTACK_2_FLAVOR", "STAT_ATTACK_2_DESC", STAT_ATTACK_1, 500000, ICON_TODO, 6);
        static { upgrades.add(STAT_ATTACK_2); }
        // TODO asset: icons/upgrades/StatDefense2.png
        public static final Upgrade STAT_DEFENSE_2 = new Upgrade(142, "STAT_DEFENSE_2", "STAT_DEFENSE_2_FLAVOR", "STAT_DEFENSE_2_DESC", STAT_DEFENSE_1, 450000, ICON_TODO, 6);
        static { upgrades.add(STAT_DEFENSE_2); }
        // TODO asset: icons/upgrades/StatAttackSpeed1.png
        public static final Upgrade STAT_ATTACK_SPEED_1 = new Upgrade(143, "STAT_ATTACK_SPEED_1", "STAT_ATTACK_SPEED_1_FLAVOR", "STAT_ATTACK_SPEED_1_DESC", STAT_ATTACK_1, 550000, ICON_TODO, 6);
        static { upgrades.add(STAT_ATTACK_SPEED_1); }
        // -- Assimilated --
        public static final Upgrade ASSIMILATED_FARMING = new Upgrade(1001, "ASSIMILATED_FARMING", "ASSIMILATED_FARMING_FLAVOR", "ASSIMILATED_FARMING_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedFarming.png"), 0);
        static { upgrades.add(ASSIMILATED_FARMING); }
        public static final Upgrade ASSIMILATED_MULTIQUEEN = new Upgrade(1002, "ASSIMILATED_MULTIQUEEN", "ASSIMILATED_MULTIQUEEN_FLAVOR", "ASSIMILATED_MULTIQUEEN_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedMultiqueen.png"), 0);
        static { upgrades.add(ASSIMILATED_MULTIQUEEN); }
        public static final Upgrade ASSIMILATED_TRAPJAW = new Upgrade(1003, "ASSIMILATED_TRAPJAW", "ASSIMILATED_TRAPJAW_FLAVOR", "ASSIMILATED_TRAPJAW_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedTrapjaw.png"), 0);
        static { upgrades.add(ASSIMILATED_TRAPJAW); }
        public static final Upgrade ASSIMILATED_HONEYPOT = new Upgrade(1004, "ASSIMILATED_HONEYPOT", "ASSIMILATED_HONEYPOT_FLAVOR", "ASSIMILATED_HONEYPOT_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedHoneypot.png"), 0);
        static { upgrades.add(ASSIMILATED_HONEYPOT); }
        public static final Upgrade ASSIMILATED_DOORHEAD = new Upgrade(1005, "ASSIMILATED_DOORHEAD", "ASSIMILATED_DOORHEAD_FLAVOR", "ASSIMILATED_DOORHEAD_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedDoorhead.png"), 0);
        static { upgrades.add(ASSIMILATED_DOORHEAD); }
        public static final Upgrade ASSIMILATED_WOODBURROW = new Upgrade(1006, "ASSIMILATED_WOODBURROW", "ASSIMILATED_WOODBURROW_FLAVOR", "ASSIMILATED_WOODBURROW_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedWoodburrow.png"), 0);
        static { upgrades.add(ASSIMILATED_WOODBURROW); }
        public static final Upgrade ASSIMILATED_SILKWEAVE = new Upgrade(1007, "ASSIMILATED_SILKWEAVE", "ASSIMILATED_SILKWEAVE_FLAVOR", "ASSIMILATED_SILKWEAVE_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedSilkweave.png"), 0);
        static { upgrades.add(ASSIMILATED_SILKWEAVE); }
        public static final Upgrade ASSIMILATED_RAFTING = new Upgrade(1008, "ASSIMILATED_RAFTING", "ASSIMILATED_RAFTING_FLAVOR", "ASSIMILATED_RAFTING_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedRafting.png"), 0);
        static { upgrades.add(ASSIMILATED_RAFTING); }
        public static final Upgrade ABILITY_RAFTING_2 = new Upgrade(528, "ABILITY_RAFTING_2", "ABILITY_RAFTING_2_FLAVOR", "ABILITY_RAFTING_2_DESC", ASSIMILATED_RAFTING, 2500, loadIcon("icons/upgrades/AbilityRafting2.png"), 4);
        static { upgrades.add(ABILITY_RAFTING_2); }
        public static final Upgrade ABILITY_RAFTING_3 = new Upgrade(529, "ABILITY_RAFTING_3", "ABILITY_RAFTING_3_FLAVOR", "ABILITY_RAFTING_3_DESC", ABILITY_RAFTING_2, 10000, loadIcon("icons/upgrades/AbilityRafting3.png"), 5);
        static { upgrades.add(ABILITY_RAFTING_3); }
        public static final Upgrade ASSIMILATED_FIREVENOM = new Upgrade(1009, "ASSIMILATED_FIREVENOM", "ASSIMILATED_FIREVENOM_FLAVOR", "ASSIMILATED_FIREVENOM_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedFirevenom.png"), 0);
        static { upgrades.add(ASSIMILATED_FIREVENOM); }
        public static final Upgrade ASSIMILATED_JUMPING = new Upgrade(1010, "ASSIMILATED_JUMPING", "ASSIMILATED_JUMPING_FLAVOR", "ASSIMILATED_JUMPING_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedJumping.png"), 0);
        static { upgrades.add(ASSIMILATED_JUMPING); }
        public static final Upgrade ASSIMILATED_STINGING = new Upgrade(1012, "ASSIMILATED_STINGING", "ASSIMILATED_STINGING_FLAVOR", "ASSIMILATED_STINGING_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedStinging.png"), 0);
        static { upgrades.add(ASSIMILATED_STINGING); }
        public static final Upgrade ASSIMILATED_SWARMING = new Upgrade(1013, "ASSIMILATED_SWARMING", "ASSIMILATED_SWARMING_FLAVOR", "ASSIMILATED_SWARMING_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedSwarming.png"), 0);
        static { upgrades.add(ASSIMILATED_SWARMING); }
        // Ghost stealth unlocks Spies (intelligence / counter-intelligence)
        public static final Upgrade ASSIMILATED_STEALTH = new Upgrade(1014, "ASSIMILATED_STEALTH", "ASSIMILATED_STEALTH_FLAVOR", "ASSIMILATED_STEALTH_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedStealth.png"), 0);
        static { upgrades.add(ASSIMILATED_STEALTH); }
        public static final Upgrade ASSIMILATED_FASTBITE = new Upgrade(1015, "ASSIMILATED_FASTBITE", "ASSIMILATED_FASTBITE_FLAVOR", "ASSIMILATED_FASTBITE_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedFastbite.png"), 0);
        static { upgrades.add(ASSIMILATED_FASTBITE); }
        public static final Upgrade ASSIMILATED_HEATRESIST = new Upgrade(1016, "ASSIMILATED_HEATRESIST", "ASSIMILATED_HEATRESIST_FLAVOR", "ASSIMILATED_HEATRESIST_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedHeatresist.png"), 0);
        static { upgrades.add(ASSIMILATED_HEATRESIST); }
        public static final Upgrade ASSIMILATED_DEADLYVENOM = new Upgrade(1017, "ASSIMILATED_DEADLYVENOM", "ASSIMILATED_DEADLYVENOM_FLAVOR", "ASSIMILATED_DEADLYVENOM_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedDeadlyvenom.png"), 0);
        static { upgrades.add(ASSIMILATED_DEADLYVENOM); }
        public static final Upgrade ASSIMILATED_SELFDESTRUCT = new Upgrade(1018, "ASSIMILATED_SELFDESTRUCT", "ASSIMILATED_SELFDESTRUCT_FLAVOR", "ASSIMILATED_SELFDESTRUCT_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedSelfdestruct.png"), 0);
        static { upgrades.add(ASSIMILATED_SELFDESTRUCT); }
        public static final Upgrade ASSIMILATED_FARSIGHT = new Upgrade(1019, "ASSIMILATED_FARSIGHT", "ASSIMILATED_FARSIGHT_FLAVOR", "ASSIMILATED_FARSIGHT_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedFarsight.png"), 0);
        static { upgrades.add(ASSIMILATED_FARSIGHT); }
        public static final Upgrade ASSIMILATED_HIVEBUILD = new Upgrade(1020, "ASSIMILATED_HIVEBUILD", "ASSIMILATED_HIVEBUILD_FLAVOR", "ASSIMILATED_HIVEBUILD_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedHivebuild.png"), 0);
        static { upgrades.add(ASSIMILATED_HIVEBUILD); }
        public static final Upgrade ASSIMILATED_LOCSENSE = new Upgrade(1021, "ASSIMILATED_LOCSENSE", "ASSIMILATED_LOCSENSE_FLAVOR", "ASSIMILATED_LOCSENSE_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedLocsense.png"), 0);
        static { upgrades.add(ASSIMILATED_LOCSENSE); }
        public static final Upgrade ASSIMILATED_ACIDSPIT = new Upgrade(1022, "ASSIMILATED_ACIDSPIT", "ASSIMILATED_ACIDSPIT_FLAVOR", "ASSIMILATED_ACIDSPIT_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedAcidspit.png"), 0);
        static { upgrades.add(ASSIMILATED_ACIDSPIT); }
        public static final Upgrade ASSIMILATED_ASSIMILATION = new Upgrade(1023, "ASSIMILATED_ASSIMILATION", "ASSIMILATED_ASSIMILATION_FLAVOR", "ASSIMILATED_ASSIMILATION_DESC", ABILITY_ASSIMILATION, 0, loadIcon("icons/upgrades/AssimilatedAssimilation.png"), 0);
        static { upgrades.add(ASSIMILATED_ASSIMILATION); }

        public static final Upgrade SYNERGY_SUPER_VENOM = new Upgrade(10001, "SYNERGY_SUPER_VENOM", "SYNERGY_SUPER_VENOM_FLAVOR", "SYNERGY_SUPER_VENOM_DESC", STAT_ACID, 0, loadIcon("icons/synergies/SuperVenom.png"), 0);
        static { upgrades.add(SYNERGY_SUPER_VENOM); }
        public static final Upgrade SYNERGY_CORROSIVE_BOMBS = new Upgrade(10002, "SYNERGY_CORROSIVE_BOMBS", "SYNERGY_CORROSIVE_BOMBS_FLAVOR", "SYNERGY_CORROSIVE_BOMBS_DESC", ROLE_BOMBER, 0, loadIcon("icons/synergies/CorrosiveBombs.png"), 0);
        static { upgrades.add(SYNERGY_CORROSIVE_BOMBS); }
        public static final Upgrade SYNERGY_WEB_BUILDING = new Upgrade(10003, "SYNERGY_WEB_BUILDING", "SYNERGY_WEB_BUILDING_FLAVOR", "SYNERGY_WEB_BUILDING_DESC", ABILITY_SYNERGY, 0, loadIcon("icons/synergies/WebBuilding.png"), 0);
        static { upgrades.add(SYNERGY_WEB_BUILDING); }

        // --- Buildings ---

        // -- Tier 0 --
        // TODO asset: icons/buildings/RoyalChamber0.png
        public static final Building ROYAL_CHAMBER_0 = new Building(1, "ROYAL_CHAMBER_0", 0, "ROYAL_CHAMBER_0_DESC", null, 0, 0, 0, ICON_TODO, GameConstants.ROOM_ROYAL_L0, 0);
        static { buildings.add(ROYAL_CHAMBER_0); }
        // TODO asset: icons/buildings/EggChamber0.png
        public static final Building EGG_CHAMBER_0 = new Building(2, "EGG_CHAMBER_0", 0, "EGG_CHAMBER_0_DESC", null, 0, 0, 0, ICON_TODO, GameConstants.ROOM_EGG_L0, 0);
        static { buildings.add(EGG_CHAMBER_0); }
        // TODO asset: icons/buildings/MushroomChamber0.png
        public static final Building MUSHROOM_CHAMBER_0 = new Building(3, "MUSHROOM_CHAMBER_0", 0, "MUSHROOM_CHAMBER_0_DESC", null, 0, 0, 0, ICON_TODO, GameConstants.ROOM_MUSHROOM_L0, 0);
        static { buildings.add(MUSHROOM_CHAMBER_0); }
        // TODO asset: icons/buildings/PlantChamber0.png
        public static final Building PLANT_CHAMBER_0 = new Building(4, "PLANT_CHAMBER_0", 0, "PLANT_CHAMBER_0_DESC", null, 0, 0, 0, ICON_TODO, GameConstants.ROOM_PLANT_L0, 0);
        static { buildings.add(PLANT_CHAMBER_0); }
        // TODO asset: icons/buildings/WaterReservoir0.png
        public static final Building WATER_RESERVOIR_0 = new Building(5, "WATER_RESERVOIR_0", 0, "WATER_RESERVOIR_0_DESC", null, 0, 0, 0, ICON_TODO, GameConstants.ROOM_WATER_L0, 0);
        static { buildings.add(WATER_RESERVOIR_0); }
        // TODO asset: icons/buildings/MeatChamber0.png
        public static final Building MEAT_CHAMBER_0 = new Building(6, "MEAT_CHAMBER_0", 0, "MEAT_CHAMBER_0_DESC", null, 0, 0, 12, ICON_TODO, GameConstants.ROOM_MEAT_L0, 0);
        static { buildings.add(MEAT_CHAMBER_0); }
        // TODO asset: icons/buildings/SyrupReservoir0.png
        public static final Building SYRUP_RESERVOIR_0 = new Building(7, "SYRUP_RESERVOIR_0", 0, "SYRUP_RESERVOIR_0_DESC", null, 0, 0, 24, ICON_TODO, GameConstants.ROOM_SYRUP_L0, 0);
        static { buildings.add(SYRUP_RESERVOIR_0); }
        // TODO asset: icons/buildings/RockWarehouse0.png
        public static final Building ROCK_WAREHOUSE_0 = new Building(8, "ROCK_WAREHOUSE_0", 0, "ROCK_WAREHOUSE_0_DESC", null, 0, 0, 50, ICON_TODO, GameConstants.ROOM_ROCK_L0, 0);
        static { buildings.add(ROCK_WAREHOUSE_0); }
        // TODO asset: icons/buildings/ResinReservoir0.png
        public static final Building RESIN_RESERVOIR_0 = new Building(9, "RESIN_RESERVOIR_0", 0, "RESIN_RESERVOIR_0_DESC", null, 0, 0, 50, ICON_TODO, GameConstants.ROOM_RESIN_L0, 0);
        static { buildings.add(RESIN_RESERVOIR_0); }
        // -- Tier 1 --
        // TODO asset: icons/buildings/RoyalChamber1.png
        public static final Building ROYAL_CHAMBER_1 = new Building(10, "ROYAL_CHAMBER_1", 1, "ROYAL_CHAMBER_1_DESC", ROYAL_CHAMBER_0, 0, 0, 1000, ICON_TODO, GameConstants.ROOM_ROYAL_L1, 1);
        static { buildings.add(ROYAL_CHAMBER_1); }
        // TODO asset: icons/buildings/EggChamber1.png
        public static final Building EGG_CHAMBER_1 = new Building(11, "EGG_CHAMBER_1", 1, "EGG_CHAMBER_1_DESC", EGG_CHAMBER_0, 0, 0, 100, ICON_TODO, GameConstants.ROOM_EGG_L1, 1);
        static { buildings.add(EGG_CHAMBER_1); }
        // TODO asset: icons/buildings/MushroomChamber1.png
        public static final Building MUSHROOM_CHAMBER_1 = new Building(12, "MUSHROOM_CHAMBER_1", 1, "MUSHROOM_CHAMBER_1_DESC", MUSHROOM_CHAMBER_0, 0, 0, 150, ICON_TODO, GameConstants.ROOM_MUSHROOM_L1, 1);
        static { buildings.add(MUSHROOM_CHAMBER_1); }
        // TODO asset: icons/buildings/PlantChamber1.png
        public static final Building PLANT_CHAMBER_1 = new Building(13, "PLANT_CHAMBER_1", 1, "PLANT_CHAMBER_1_DESC", PLANT_CHAMBER_0, 0, 0, 180, ICON_TODO, GameConstants.ROOM_PLANT_L1, 1);
        static { buildings.add(PLANT_CHAMBER_1); }
        // TODO asset: icons/buildings/WaterReservoir1.png
        public static final Building WATER_RESERVOIR_1 = new Building(14, "WATER_RESERVOIR_1", 1, "WATER_RESERVOIR_1_DESC", WATER_RESERVOIR_0, 0, 0, 250, ICON_TODO, GameConstants.ROOM_WATER_L1, 1);
        static { buildings.add(WATER_RESERVOIR_1); }
        // TODO asset: icons/buildings/MeatChamber1.png
        public static final Building MEAT_CHAMBER_1 = new Building(15, "MEAT_CHAMBER_1", 1, "MEAT_CHAMBER_1_DESC", MEAT_CHAMBER_0, 0, 0, 200, ICON_TODO, GameConstants.ROOM_MEAT_L1, 1);
        static { buildings.add(MEAT_CHAMBER_1); }
        // TODO asset: icons/buildings/SyrupReservoir1.png
        public static final Building SYRUP_RESERVOIR_1 = new Building(16, "SYRUP_RESERVOIR_1", 1, "SYRUP_RESERVOIR_1_DESC", SYRUP_RESERVOIR_0, 0, 0, 250, ICON_TODO, GameConstants.ROOM_SYRUP_L1, 1);
        static { buildings.add(SYRUP_RESERVOIR_1); }
        // TODO asset: icons/buildings/RockWarehouse1.png
        public static final Building ROCK_WAREHOUSE_1 = new Building(17, "ROCK_WAREHOUSE_1", 1, "ROCK_WAREHOUSE_1_DESC", ROCK_WAREHOUSE_0, 0, 0, 500, ICON_TODO, GameConstants.ROOM_ROCK_L1, 1);
        static { buildings.add(ROCK_WAREHOUSE_1); }
        // TODO asset: icons/buildings/ResinReservoir1.png
        public static final Building RESIN_RESERVOIR_1 = new Building(18, "RESIN_RESERVOIR_1", 1, "RESIN_RESERVOIR_1_DESC", RESIN_RESERVOIR_0, 0, 0, 400, ICON_TODO, GameConstants.ROOM_RESIN_L1, 1);
        static { buildings.add(RESIN_RESERVOIR_1); }
        // -- Tier 2 --
        // TODO asset: icons/buildings/RoyalChamber2.png
        public static final Building ROYAL_CHAMBER_2 = new Building(19, "ROYAL_CHAMBER_2", 2, "ROYAL_CHAMBER_2_DESC", ROYAL_CHAMBER_1, 500, 0, 3000, ICON_TODO, GameConstants.ROOM_ROYAL_L2, 2);
        static { buildings.add(ROYAL_CHAMBER_2); }
        // TODO asset: icons/buildings/EggChamber2.png
        public static final Building EGG_CHAMBER_2 = new Building(20, "EGG_CHAMBER_2", 2, "EGG_CHAMBER_2_DESC", EGG_CHAMBER_1, 200, 0, 1000, ICON_TODO, GameConstants.ROOM_EGG_L2, 2);
        static { buildings.add(EGG_CHAMBER_2); }
        // TODO asset: icons/buildings/MushroomChamber2.png
        public static final Building MUSHROOM_CHAMBER_2 = new Building(21, "MUSHROOM_CHAMBER_2", 2, "MUSHROOM_CHAMBER_2_DESC", MUSHROOM_CHAMBER_1, 250, 0, 800, ICON_TODO, GameConstants.ROOM_MUSHROOM_L2, 2);
        static { buildings.add(MUSHROOM_CHAMBER_2); }
        // TODO asset: icons/buildings/PlantChamber2.png
        public static final Building PLANT_CHAMBER_2 = new Building(22, "PLANT_CHAMBER_2", 2, "PLANT_CHAMBER_2_DESC", PLANT_CHAMBER_1, 200, 0, 1000, ICON_TODO, GameConstants.ROOM_PLANT_L2, 2);
        static { buildings.add(PLANT_CHAMBER_2); }
        // TODO asset: icons/buildings/WaterReservoir2.png
        public static final Building WATER_RESERVOIR_2 = new Building(23, "WATER_RESERVOIR_2", 2, "WATER_RESERVOIR_2_DESC", WATER_RESERVOIR_1, 300, 0, 1200, ICON_TODO, GameConstants.ROOM_WATER_L2, 2);
        static { buildings.add(WATER_RESERVOIR_2); }
        // TODO asset: icons/buildings/MeatChamber2.png
        public static final Building MEAT_CHAMBER_2 = new Building(24, "MEAT_CHAMBER_2", 2, "MEAT_CHAMBER_2_DESC", MEAT_CHAMBER_1, 350, 0, 1500, ICON_TODO, GameConstants.ROOM_MEAT_L2, 2);
        static { buildings.add(MEAT_CHAMBER_2); }
        // TODO asset: icons/buildings/SyrupReservoir2.png
        public static final Building SYRUP_RESERVOIR_2 = new Building(25, "SYRUP_RESERVOIR_2", 2, "SYRUP_RESERVOIR_2_DESC", SYRUP_RESERVOIR_1, 350, 0, 1500, ICON_TODO, GameConstants.ROOM_SYRUP_L2, 2);
        static { buildings.add(SYRUP_RESERVOIR_2); }
        // TODO asset: icons/buildings/RockWarehouse2.png
        public static final Building ROCK_WAREHOUSE_2 = new Building(26, "ROCK_WAREHOUSE_2", 2, "ROCK_WAREHOUSE_2_DESC", ROCK_WAREHOUSE_1, 450, 0, 2500, ICON_TODO, GameConstants.ROOM_ROCK_L2, 2);
        static { buildings.add(ROCK_WAREHOUSE_2); }
        // TODO asset: icons/buildings/ResinReservoir2.png
        public static final Building RESIN_RESERVOIR_2 = new Building(27, "RESIN_RESERVOIR_2", 2, "RESIN_RESERVOIR_2_DESC", RESIN_RESERVOIR_1, 400, 0, 2000, ICON_TODO, GameConstants.ROOM_RESIN_L2, 2);
        static { buildings.add(RESIN_RESERVOIR_2); }
        // -- Tier 3 --
        // TODO asset: icons/buildings/RoyalChamber3.png
        public static final Building ROYAL_CHAMBER_3 = new Building(28, "ROYAL_CHAMBER_3", 3, "ROYAL_CHAMBER_3_DESC", ROYAL_CHAMBER_2, 1000, 0, 15000, ICON_TODO, GameConstants.ROOM_ROYAL_L3, 3);
        static { buildings.add(ROYAL_CHAMBER_3); }
        // TODO asset: icons/buildings/EggChamber3.png
        public static final Building EGG_CHAMBER_3 = new Building(29, "EGG_CHAMBER_3", 3, "EGG_CHAMBER_3_DESC", EGG_CHAMBER_2, 500, 0, 5000, ICON_TODO, GameConstants.ROOM_EGG_L3, 3);
        static { buildings.add(EGG_CHAMBER_3); }
        // TODO asset: icons/buildings/MushroomChamber3.png
        public static final Building MUSHROOM_CHAMBER_3 = new Building(30, "MUSHROOM_CHAMBER_3", 3, "MUSHROOM_CHAMBER_3_DESC", MUSHROOM_CHAMBER_2, 800, 0, 5000, ICON_TODO, GameConstants.ROOM_MUSHROOM_L3, 3);
        static { buildings.add(MUSHROOM_CHAMBER_3); }
        // TODO asset: icons/buildings/PlantChamber3.png
        public static final Building PLANT_CHAMBER_3 = new Building(31, "PLANT_CHAMBER_3", 3, "PLANT_CHAMBER_3_DESC", PLANT_CHAMBER_2, 700, 0, 5000, ICON_TODO, GameConstants.ROOM_PLANT_L3, 3);
        static { buildings.add(PLANT_CHAMBER_3); }
        // TODO asset: icons/buildings/WaterReservoir3.png
        public static final Building WATER_RESERVOIR_3 = new Building(32, "WATER_RESERVOIR_3", 3, "WATER_RESERVOIR_3_DESC", WATER_RESERVOIR_2, 900, 0, 5000, ICON_TODO, GameConstants.ROOM_WATER_L3, 3);
        static { buildings.add(WATER_RESERVOIR_3); }
        // TODO asset: icons/buildings/MeatChamber3.png
        public static final Building MEAT_CHAMBER_3 = new Building(33, "MEAT_CHAMBER_3", 3, "MEAT_CHAMBER_3_DESC", MEAT_CHAMBER_2, 850, 0, 10000, ICON_TODO, GameConstants.ROOM_MEAT_L3, 3);
        static { buildings.add(MEAT_CHAMBER_3); }
        // TODO asset: icons/buildings/SyrupReservoir3.png
        public static final Building SYRUP_RESERVOIR_3 = new Building(34, "SYRUP_RESERVOIR_3", 3, "SYRUP_RESERVOIR_3_DESC", SYRUP_RESERVOIR_2, 800, 0, 10000, ICON_TODO, GameConstants.ROOM_SYRUP_L3, 3);
        static { buildings.add(SYRUP_RESERVOIR_3); }
        // TODO asset: icons/buildings/RockWarehouse3.png
        public static final Building ROCK_WAREHOUSE_3 = new Building(35, "ROCK_WAREHOUSE_3", 3, "ROCK_WAREHOUSE_3_DESC", ROCK_WAREHOUSE_2, 950, 0, 10000, ICON_TODO, GameConstants.ROOM_ROCK_L3, 3);
        static { buildings.add(ROCK_WAREHOUSE_3); }
        // TODO asset: icons/buildings/ResinReservoir3.png
        public static final Building RESIN_RESERVOIR_3 = new Building(36, "RESIN_RESERVOIR_3", 3, "RESIN_RESERVOIR_3_DESC", RESIN_RESERVOIR_2, 900, 0, 10000, ICON_TODO, GameConstants.ROOM_RESIN_L3, 3);
        static { buildings.add(RESIN_RESERVOIR_3); }
        // -- Tier 4 --
        // TODO asset: icons/buildings/RoyalChamber4.png
        public static final Building ROYAL_CHAMBER_4 = new Building(37, "ROYAL_CHAMBER_4", 4, "ROYAL_CHAMBER_4_DESC", ROYAL_CHAMBER_3, 2000, 1000, 37500, ICON_TODO, GameConstants.ROOM_ROYAL_L3, 4);
        static { buildings.add(ROYAL_CHAMBER_4); }
        // TODO asset: icons/buildings/EggChamber4.png
        public static final Building EGG_CHAMBER_4 = new Building(38, "EGG_CHAMBER_4", 4, "EGG_CHAMBER_4_DESC", EGG_CHAMBER_3, 1000, 500, 12500, ICON_TODO, GameConstants.ROOM_EGG_L3, 4);
        static { buildings.add(EGG_CHAMBER_4); }
        // TODO asset: icons/buildings/MushroomChamber4.png
        public static final Building MUSHROOM_CHAMBER_4 = new Building(39, "MUSHROOM_CHAMBER_4", 4, "MUSHROOM_CHAMBER_4_DESC", MUSHROOM_CHAMBER_3, 1600, 800, 12500, ICON_TODO, GameConstants.ROOM_MUSHROOM_L3, 4);
        static { buildings.add(MUSHROOM_CHAMBER_4); }
        // TODO asset: icons/buildings/PlantChamber4.png
        public static final Building PLANT_CHAMBER_4 = new Building(40, "PLANT_CHAMBER_4", 4, "PLANT_CHAMBER_4_DESC", PLANT_CHAMBER_3, 1400, 700, 12500, ICON_TODO, GameConstants.ROOM_PLANT_L3, 4);
        static { buildings.add(PLANT_CHAMBER_4); }
        // TODO asset: icons/buildings/WaterReservoir4.png
        public static final Building WATER_RESERVOIR_4 = new Building(41, "WATER_RESERVOIR_4", 4, "WATER_RESERVOIR_4_DESC", WATER_RESERVOIR_3, 1800, 900, 12500, ICON_TODO, GameConstants.ROOM_WATER_L3, 4);
        static { buildings.add(WATER_RESERVOIR_4); }
        // TODO asset: icons/buildings/MeatChamber4.png
        public static final Building MEAT_CHAMBER_4 = new Building(42, "MEAT_CHAMBER_4", 4, "MEAT_CHAMBER_4_DESC", MEAT_CHAMBER_3, 1700, 850, 25000, ICON_TODO, GameConstants.ROOM_MEAT_L3, 4);
        static { buildings.add(MEAT_CHAMBER_4); }
        // TODO asset: icons/buildings/SyrupReservoir4.png
        public static final Building SYRUP_RESERVOIR_4 = new Building(43, "SYRUP_RESERVOIR_4", 4, "SYRUP_RESERVOIR_4_DESC", SYRUP_RESERVOIR_3, 1600, 800, 25000, ICON_TODO, GameConstants.ROOM_SYRUP_L3, 4);
        static { buildings.add(SYRUP_RESERVOIR_4); }
        // TODO asset: icons/buildings/RockWarehouse4.png
        public static final Building ROCK_WAREHOUSE_4 = new Building(44, "ROCK_WAREHOUSE_4", 4, "ROCK_WAREHOUSE_4_DESC", ROCK_WAREHOUSE_3, 1900, 950, 25000, ICON_TODO, GameConstants.ROOM_ROCK_L3, 4);
        static { buildings.add(ROCK_WAREHOUSE_4); }
        // TODO asset: icons/buildings/ResinReservoir4.png
        public static final Building RESIN_RESERVOIR_4 = new Building(45, "RESIN_RESERVOIR_4", 4, "RESIN_RESERVOIR_4_DESC", RESIN_RESERVOIR_3, 1800, 900, 25000, ICON_TODO, GameConstants.ROOM_RESIN_L3, 4);
        static { buildings.add(RESIN_RESERVOIR_4); }
        // -- Tier 5 --
        // TODO asset: icons/buildings/RoyalChamber5.png
        public static final Building ROYAL_CHAMBER_5 = new Building(46, "ROYAL_CHAMBER_5", 5, "ROYAL_CHAMBER_5_DESC", ROYAL_CHAMBER_4, 4000, 2500, 75000, ICON_TODO, GameConstants.ROOM_ROYAL_L3, 5);
        static { buildings.add(ROYAL_CHAMBER_5); }
        // TODO asset: icons/buildings/EggChamber5.png
        public static final Building EGG_CHAMBER_5 = new Building(47, "EGG_CHAMBER_5", 5, "EGG_CHAMBER_5_DESC", EGG_CHAMBER_4, 2000, 1250, 25000, ICON_TODO, GameConstants.ROOM_EGG_L3, 5);
        static { buildings.add(EGG_CHAMBER_5); }
        // TODO asset: icons/buildings/MushroomChamber5.png
        public static final Building MUSHROOM_CHAMBER_5 = new Building(48, "MUSHROOM_CHAMBER_5", 5, "MUSHROOM_CHAMBER_5_DESC", MUSHROOM_CHAMBER_4, 3200, 2000, 25000, ICON_TODO, GameConstants.ROOM_MUSHROOM_L3, 5);
        static { buildings.add(MUSHROOM_CHAMBER_5); }
        // TODO asset: icons/buildings/PlantChamber5.png
        public static final Building PLANT_CHAMBER_5 = new Building(49, "PLANT_CHAMBER_5", 5, "PLANT_CHAMBER_5_DESC", PLANT_CHAMBER_4, 2800, 1750, 25000, ICON_TODO, GameConstants.ROOM_PLANT_L3, 5);
        static { buildings.add(PLANT_CHAMBER_5); }
        // TODO asset: icons/buildings/WaterReservoir5.png
        public static final Building WATER_RESERVOIR_5 = new Building(50, "WATER_RESERVOIR_5", 5, "WATER_RESERVOIR_5_DESC", WATER_RESERVOIR_4, 3600, 2250, 25000, ICON_TODO, GameConstants.ROOM_WATER_L3, 5);
        static { buildings.add(WATER_RESERVOIR_5); }
        // TODO asset: icons/buildings/MeatChamber5.png
        public static final Building MEAT_CHAMBER_5 = new Building(51, "MEAT_CHAMBER_5", 5, "MEAT_CHAMBER_5_DESC", MEAT_CHAMBER_4, 3400, 2125, 50000, ICON_TODO, GameConstants.ROOM_MEAT_L3, 5);
        static { buildings.add(MEAT_CHAMBER_5); }
        // TODO asset: icons/buildings/SyrupReservoir5.png
        public static final Building SYRUP_RESERVOIR_5 = new Building(52, "SYRUP_RESERVOIR_5", 5, "SYRUP_RESERVOIR_5_DESC", SYRUP_RESERVOIR_4, 3200, 2000, 50000, ICON_TODO, GameConstants.ROOM_SYRUP_L3, 5);
        static { buildings.add(SYRUP_RESERVOIR_5); }
        // TODO asset: icons/buildings/RockWarehouse5.png
        public static final Building ROCK_WAREHOUSE_5 = new Building(53, "ROCK_WAREHOUSE_5", 5, "ROCK_WAREHOUSE_5_DESC", ROCK_WAREHOUSE_4, 3800, 2375, 50000, ICON_TODO, GameConstants.ROOM_ROCK_L3, 5);
        static { buildings.add(ROCK_WAREHOUSE_5); }
        // TODO asset: icons/buildings/ResinReservoir5.png
        public static final Building RESIN_RESERVOIR_5 = new Building(54, "RESIN_RESERVOIR_5", 5, "RESIN_RESERVOIR_5_DESC", RESIN_RESERVOIR_4, 3600, 2250, 50000, ICON_TODO, GameConstants.ROOM_RESIN_L3, 5);
        static { buildings.add(RESIN_RESERVOIR_5); }
        // -- Tier Misc. (passive overlays) --
        // TODO asset: icons/buildings/PassiveLab.png
        public static final Building PASSIVE_LAB = new Building(101, "PASSIVE_LAB", 0, "PASSIVE_LAB_DESC", ROYAL_CHAMBER_1, 150, 0, 800, ICON_TODO, GameConstants.ROOM_PASSIVE_LAB, 2);
        static { buildings.add(PASSIVE_LAB); }
        // TODO asset: icons/buildings/PassiveWater.png
        public static final Building PASSIVE_WATER = new Building(102, "PASSIVE_WATER", 0, "PASSIVE_WATER_DESC", WATER_RESERVOIR_1, 100, 0, 500, ICON_TODO, GameConstants.ROOM_PASSIVE_WATER, 2);
        static { buildings.add(PASSIVE_WATER); }
        // TODO asset: icons/buildings/PassiveAphid.png
        public static final Building PASSIVE_APHID = new Building(103, "PASSIVE_APHID", 0, "PASSIVE_APHID_DESC", SYRUP_RESERVOIR_1, 200, 0, 1000, ICON_TODO, GameConstants.ROOM_PASSIVE_APHID, 2);
        static { buildings.add(PASSIVE_APHID); }
        // TODO asset: icons/buildings/PassiveNurse.png
        public static final Building PASSIVE_NURSE = new Building(104, "PASSIVE_NURSE", 0, "PASSIVE_NURSE_DESC", EGG_CHAMBER_1, 150, 0, 700, ICON_TODO, GameConstants.ROOM_PASSIVE_NURSE, 2);
        static { buildings.add(PASSIVE_NURSE); }
        // TODO asset: icons/buildings/PassiveFarm.png
        public static final Building PASSIVE_FARM = new Building(105, "PASSIVE_FARM", 0, "PASSIVE_FARM_DESC", MUSHROOM_CHAMBER_1, 200, 0, 900, ICON_TODO, GameConstants.ROOM_PASSIVE_FARM, 2);
        static { buildings.add(PASSIVE_FARM); }
        // TODO asset: icons/buildings/PassiveGrave.png
        public static final Building PASSIVE_GRAVE = new Building(106, "PASSIVE_GRAVE", 0, "PASSIVE_GRAVE_DESC", MEAT_CHAMBER_1, 150, 0, 600, ICON_TODO, GameConstants.ROOM_PASSIVE_GRAVE, 2);
        static { buildings.add(PASSIVE_GRAVE); }
        // TODO asset: icons/buildings/PassiveWeb.png
        public static final Building PASSIVE_WEB = new Building(108, "PASSIVE_WEB", 0, "PASSIVE_WEB_DESC", MEAT_CHAMBER_1, 200, 0, 800, loadIcon("icons/misc/PassiveWeb.png"), GameConstants.ROOM_PASSIVE_WEB, 2);
        static { buildings.add(PASSIVE_WEB); }
        // TODO asset: icons/buildings/Composter.png
        public static final Building BUILDING_COMPOSTER = new Building(107, "BUILDING_COMPOSTER", 0, "BUILDING_COMPOSTER_DESC", PASSIVE_GRAVE, 500, 0, 1000, ICON_TODO, GameConstants.ROOM_PASSIVE_COMPOSTER, 3);
        static { buildings.add(BUILDING_COMPOSTER); }

        // Silkweave leaf L3 storages (tandem with resin L3; plant cost = 10× resin of matching L3)
        // TODO asset: icons/buildings + sprites/buildings/rooms for Silkweave L3 storages
        public static final Building EGG_CHAMBER_3_SILK = new Building(201, "EGG_CHAMBER_3_SILK", 3, "EGG_CHAMBER_3_SILK_DESC", EGG_CHAMBER_2, 0, 0, 500 * GameNumbers.SILKWEAVE_PLANT_COST_MULT, 5000, ICON_TODO, GameConstants.ROOM_EGG_L3, 3);
        static { buildings.add(EGG_CHAMBER_3_SILK); }
        public static final Building MUSHROOM_CHAMBER_3_SILK = new Building(202, "MUSHROOM_CHAMBER_3_SILK", 3, "MUSHROOM_CHAMBER_3_SILK_DESC", MUSHROOM_CHAMBER_2, 0, 0, 800 * GameNumbers.SILKWEAVE_PLANT_COST_MULT, 5000, ICON_TODO, GameConstants.ROOM_MUSHROOM_L3, 3);
        static { buildings.add(MUSHROOM_CHAMBER_3_SILK); }
        public static final Building PLANT_CHAMBER_3_SILK = new Building(203, "PLANT_CHAMBER_3_SILK", 3, "PLANT_CHAMBER_3_SILK_DESC", PLANT_CHAMBER_2, 0, 0, 700 * GameNumbers.SILKWEAVE_PLANT_COST_MULT, 5000, ICON_TODO, GameConstants.ROOM_PLANT_L3, 3);
        static { buildings.add(PLANT_CHAMBER_3_SILK); }
        public static final Building WATER_RESERVOIR_3_SILK = new Building(204, "WATER_RESERVOIR_3_SILK", 3, "WATER_RESERVOIR_3_SILK_DESC", WATER_RESERVOIR_2, 0, 0, 900 * GameNumbers.SILKWEAVE_PLANT_COST_MULT, 5000, ICON_TODO, GameConstants.ROOM_WATER_L3, 3);
        static { buildings.add(WATER_RESERVOIR_3_SILK); }
        public static final Building MEAT_CHAMBER_3_SILK = new Building(205, "MEAT_CHAMBER_3_SILK", 3, "MEAT_CHAMBER_3_SILK_DESC", MEAT_CHAMBER_2, 0, 0, 850 * GameNumbers.SILKWEAVE_PLANT_COST_MULT, 10000, ICON_TODO, GameConstants.ROOM_MEAT_L3, 3);
        static { buildings.add(MEAT_CHAMBER_3_SILK); }
        public static final Building SYRUP_RESERVOIR_3_SILK = new Building(206, "SYRUP_RESERVOIR_3_SILK", 3, "SYRUP_RESERVOIR_3_SILK_DESC", SYRUP_RESERVOIR_2, 0, 0, 800 * GameNumbers.SILKWEAVE_PLANT_COST_MULT, 10000, ICON_TODO, GameConstants.ROOM_SYRUP_L3, 3);
        static { buildings.add(SYRUP_RESERVOIR_3_SILK); }
        public static final Building ROCK_WAREHOUSE_3_SILK = new Building(207, "ROCK_WAREHOUSE_3_SILK", 3, "ROCK_WAREHOUSE_3_SILK_DESC", ROCK_WAREHOUSE_2, 0, 0, 950 * GameNumbers.SILKWEAVE_PLANT_COST_MULT, 10000, ICON_TODO, GameConstants.ROOM_ROCK_L3, 3);
        static { buildings.add(ROCK_WAREHOUSE_3_SILK); }
        public static final Building RESIN_RESERVOIR_3_SILK = new Building(208, "RESIN_RESERVOIR_3_SILK", 3, "RESIN_RESERVOIR_3_SILK_DESC", RESIN_RESERVOIR_2, 0, 0, 900 * GameNumbers.SILKWEAVE_PLANT_COST_MULT, 10000, ICON_TODO, GameConstants.ROOM_RESIN_L3, 3);
        static { buildings.add(RESIN_RESERVOIR_3_SILK); }

        // Hivebuild mounds (capacity boosts; room/overworld art TBD)
        // TODO asset: icons/buildings + sprites/buildings/rooms + overworld mound art for HiveMound2/4
        public static final Building HIVE_MOUND_2 = new Building(210, "HIVE_MOUND_2", 2, "HIVE_MOUND_2_DESC", ROYAL_CHAMBER_1, 400, 0, 2000, ICON_TODO, GameConstants.ROOM_PASSIVE_LAB, 2);
        static { buildings.add(HIVE_MOUND_2); }
        public static final Building HIVE_MOUND_4 = new Building(211, "HIVE_MOUND_4", 4, "HIVE_MOUND_4_DESC", HIVE_MOUND_2, 1500, 800, 15000, ICON_TODO, GameConstants.ROOM_PASSIVE_LAB, 4);
        static { buildings.add(HIVE_MOUND_4); }

        public static final Building[] BUILDING_CHAIN_ROYAL = {
                ROYAL_CHAMBER_5, ROYAL_CHAMBER_4, ROYAL_CHAMBER_3, ROYAL_CHAMBER_2, ROYAL_CHAMBER_1, ROYAL_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_EGG = {
                EGG_CHAMBER_5, EGG_CHAMBER_4, EGG_CHAMBER_3, EGG_CHAMBER_2, EGG_CHAMBER_1, EGG_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_MUSHROOM = {
                MUSHROOM_CHAMBER_5, MUSHROOM_CHAMBER_4, MUSHROOM_CHAMBER_3, MUSHROOM_CHAMBER_2, MUSHROOM_CHAMBER_1, MUSHROOM_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_PLANT = {
                PLANT_CHAMBER_5, PLANT_CHAMBER_4, PLANT_CHAMBER_3, PLANT_CHAMBER_2, PLANT_CHAMBER_1, PLANT_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_WATER = {
                WATER_RESERVOIR_5, WATER_RESERVOIR_4, WATER_RESERVOIR_3, WATER_RESERVOIR_2, WATER_RESERVOIR_1, WATER_RESERVOIR_0
        };
        public static final Building[] BUILDING_CHAIN_MEAT = {
                MEAT_CHAMBER_5, MEAT_CHAMBER_4, MEAT_CHAMBER_3, MEAT_CHAMBER_2, MEAT_CHAMBER_1, MEAT_CHAMBER_0
        };
        public static final Building[] BUILDING_CHAIN_SYRUP = {
                SYRUP_RESERVOIR_5, SYRUP_RESERVOIR_4, SYRUP_RESERVOIR_3, SYRUP_RESERVOIR_2, SYRUP_RESERVOIR_1, SYRUP_RESERVOIR_0
        };
        public static final Building[] BUILDING_CHAIN_ROCK = {
                ROCK_WAREHOUSE_5, ROCK_WAREHOUSE_4, ROCK_WAREHOUSE_3, ROCK_WAREHOUSE_2, ROCK_WAREHOUSE_1, ROCK_WAREHOUSE_0
        };
        public static final Building[] BUILDING_CHAIN_RESIN = {
                RESIN_RESERVOIR_5, RESIN_RESERVOIR_4, RESIN_RESERVOIR_3, RESIN_RESERVOIR_2, RESIN_RESERVOIR_1, RESIN_RESERVOIR_0
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

        public static final Assimilation ASSIMILATION_OMNI = new Assimilation(12, "ASSIMILATION_OMNI", "ASSIMILATION_OMNI_DESC", ASSIMILATED_ASSIMILATION, ASSIMILATION_COST, loadIcon("icons/assimilations/Omni.png"));
        static { assimilations.add(ASSIMILATION_OMNI); }

        public static boolean canAssimilateForeignSpecies(Dynasty dynasty) {
                return dynasty != null && (dynasty.hasUpgrade(ASSIMILATED_ASSIMILATION)
                        || dynasty.isAssimilationCompleted(ASSIMILATION_OMNI));
        }

        public static boolean isAssimilationAvailable(Dynasty dynasty, Assimilation assimilation) {
                if (dynasty == null || assimilation == null) {
                        return false;
                }
                if (dynasty.isAssimilationCompleted(assimilation) || dynasty.getCurrentAssimilation() != null) {
                        return false;
                }

                int speciesId = -1;
                for (AntSpecies species : GameConstants.getSpecies()) {
                        if (species.getAssimilation() == assimilation) {
                                speciesId = species.getId();
                                break;
                        }
                }

                boolean isOmniKeystone = assimilation == ASSIMILATION_OMNI;
                boolean defeated = speciesId != -1 && dynasty.getDefeatedSpeciesIds().contains(speciesId);
                boolean omniSelfAvailable = isOmniKeystone && dynasty.getSpecies() == GameConstants.SPECIES_OMNI;
                boolean foreignAllowed = isOmniKeystone || canAssimilateForeignSpecies(dynasty);
                return (defeated || omniSelfAvailable) && foreignAllowed;
        }

        public static boolean hasAssimilationUiContent(Dynasty dynasty) {
                if (dynasty == null) {
                        return false;
                }
                if (dynasty.getCurrentAssimilation() != null) {
                        return true;
                }
                for (Assimilation assimilation : getAssimilations()) {
                        if (isAssimilationAvailable(dynasty, assimilation)) {
                                return true;
                        }
                }
                return false;
        }

        public static boolean shouldShowAssimilationUi(Dynasty dynasty) {
                return dynasty != null
                        && dynasty.hasUpgrade(ABILITY_ASSIMILATION)
                        && (hasAssimilationUiContent(dynasty)
                                || dynasty.getCompletedAssimilations().size() > 2);
        }

        public static final Assimilation ASSIMILATION_LEAFCUTTER = new Assimilation(1, "ASSIMILATION_LEAFCUTTER", "ASSIMILATION_LEAFCUTTER_DESC", ASSIMILATED_FARMING, ASSIMILATION_COST, loadIcon("icons/assimilations/Leafcutter.png"));
        static { assimilations.add(ASSIMILATION_LEAFCUTTER); }
        public static final Assimilation ASSIMILATION_PHARAOH = new Assimilation(2, "ASSIMILATION_PHARAOH", "ASSIMILATION_PHARAOH_DESC", ASSIMILATED_MULTIQUEEN, ASSIMILATION_COST, loadIcon("icons/assimilations/Pharaoh.png"));
        static { assimilations.add(ASSIMILATION_PHARAOH); }
        public static final Assimilation ASSIMILATION_MARAUDER = new Assimilation(3, "ASSIMILATION_MARAUDER", "ASSIMILATION_MARAUDER_DESC", TYPE_MAJOR, ASSIMILATION_COST, loadIcon("icons/assimilations/Marauder.png"));
        static { assimilations.add(ASSIMILATION_MARAUDER); }
        public static final Assimilation ASSIMILATION_TRAPJAW = new Assimilation(4, "ASSIMILATION_TRAPJAW", "ASSIMILATION_TRAPJAW_DESC", ASSIMILATED_TRAPJAW, ASSIMILATION_COST, loadIcon("icons/assimilations/Trapjaw.png"));
        static { assimilations.add(ASSIMILATION_TRAPJAW); }
        public static final Assimilation ASSIMILATION_HONEYPOT = new Assimilation(5, "ASSIMILATION_HONEYPOT", "ASSIMILATION_HONEYPOT_DESC", ASSIMILATED_HONEYPOT, ASSIMILATION_COST, loadIcon("icons/assimilations/Honeypot.png"));
        static { assimilations.add(ASSIMILATION_HONEYPOT); }
        public static final Assimilation ASSIMILATION_DOORHEAD = new Assimilation(6, "ASSIMILATION_DOORHEAD", "ASSIMILATION_DOORHEAD_DESC", ASSIMILATED_DOORHEAD, ASSIMILATION_COST, loadIcon("icons/assimilations/Turtle.png"));
        static { assimilations.add(ASSIMILATION_DOORHEAD); }
        public static final Assimilation ASSIMILATION_WOODBURROW = new Assimilation(7, "ASSIMILATION_WOODBURROW", "ASSIMILATION_WOODBURROW_DESC", ASSIMILATED_WOODBURROW, ASSIMILATION_COST, loadIcon("icons/assimilations/Carpenter.png"));
        static { assimilations.add(ASSIMILATION_WOODBURROW); }
        public static final Assimilation ASSIMILATION_SILKWEAVE = new Assimilation(8, "ASSIMILATION_SILKWEAVE", "ASSIMILATION_SILKWEAVE_DESC", ASSIMILATED_SILKWEAVE, ASSIMILATION_COST, loadIcon("icons/assimilations/Weaver.png"));
        static { assimilations.add(ASSIMILATION_SILKWEAVE); }
        public static final Assimilation ASSIMILATION_RAFTING = new Assimilation(9, "ASSIMILATION_RAFTING", "ASSIMILATION_RAFTING_DESC", ASSIMILATED_RAFTING, ASSIMILATION_COST, loadIcon("icons/assimilations/Floodplain.png"));
        static { assimilations.add(ASSIMILATION_RAFTING); }
        public static final Assimilation ASSIMILATION_FIREVENOM = new Assimilation(10, "ASSIMILATION_FIREVENOM", "ASSIMILATION_FIREVENOM_DESC", ASSIMILATED_FIREVENOM, ASSIMILATION_COST, loadIcon("icons/assimilations/Fire.png"));
        static { assimilations.add(ASSIMILATION_FIREVENOM); }
        public static final Assimilation ASSIMILATION_JUMPING = new Assimilation(11, "ASSIMILATION_JUMPING", "ASSIMILATION_JUMPING_DESC", ASSIMILATED_JUMPING, ASSIMILATION_COST, loadIcon("icons/assimilations/Jet.png"));
        static { assimilations.add(ASSIMILATION_JUMPING); }
        public static final Assimilation ASSIMILATION_STINGING = new Assimilation(13, "ASSIMILATION_STINGING", "ASSIMILATION_STINGING_DESC", ASSIMILATED_STINGING, ASSIMILATION_COST, loadIcon("icons/assimilations/Bullet.png"));
        static { assimilations.add(ASSIMILATION_STINGING); }
        public static final Assimilation ASSIMILATION_SWARMING = new Assimilation(14, "ASSIMILATION_SWARMING", "ASSIMILATION_SWARMING_DESC", ASSIMILATED_SWARMING, ASSIMILATION_COST, loadIcon("icons/assimilations/Army.png"));
        static { assimilations.add(ASSIMILATION_SWARMING); }
        public static final Assimilation ASSIMILATION_STEALTH = new Assimilation(15, "ASSIMILATION_STEALTH", "ASSIMILATION_STEALTH_DESC", ASSIMILATED_STEALTH, ASSIMILATION_COST, loadIcon("icons/assimilations/Ghost.png"));
        static { assimilations.add(ASSIMILATION_STEALTH); }
        public static final Assimilation ASSIMILATION_FASTBITE = new Assimilation(16, "ASSIMILATION_FASTBITE", "ASSIMILATION_FASTBITE_DESC", ASSIMILATED_FASTBITE, ASSIMILATION_COST, loadIcon("icons/assimilations/Dracula.png"));
        static { assimilations.add(ASSIMILATION_FASTBITE); }
        public static final Assimilation ASSIMILATION_HEATRESIST = new Assimilation(17, "ASSIMILATION_HEATRESIST", "ASSIMILATION_HEATRESIST_DESC", ASSIMILATED_HEATRESIST, ASSIMILATION_COST, loadIcon("icons/assimilations/Silver.png"));
        static { assimilations.add(ASSIMILATION_HEATRESIST); }
        public static final Assimilation ASSIMILATION_DEADLYVENOM = new Assimilation(18, "ASSIMILATION_DEADLYVENOM", "ASSIMILATION_DEADLYVENOM_DESC", ASSIMILATED_DEADLYVENOM, ASSIMILATION_COST, loadIcon("icons/assimilations/Maricopa.png"));
        static { assimilations.add(ASSIMILATION_DEADLYVENOM); }
        public static final Assimilation ASSIMILATION_SELFDESTRUCT = new Assimilation(19, "ASSIMILATION_SELFDESTRUCT", "ASSIMILATION_SELFDESTRUCT_DESC", ASSIMILATED_SELFDESTRUCT, ASSIMILATION_COST, loadIcon("icons/assimilations/Exploding.png"));
        static { assimilations.add(ASSIMILATION_SELFDESTRUCT); }
        public static final Assimilation ASSIMILATION_FARSIGHT = new Assimilation(20, "ASSIMILATION_FARSIGHT", "ASSIMILATION_FARSIGHT_DESC", ASSIMILATED_FARSIGHT, ASSIMILATION_COST, loadIcon("icons/assimilations/Bulldog.png"));
        static { assimilations.add(ASSIMILATION_FARSIGHT); }
        public static final Assimilation ASSIMILATION_HIVEBUILD = new Assimilation(21, "ASSIMILATION_HIVEBUILD", "ASSIMILATION_HIVEBUILD_DESC", ASSIMILATED_HIVEBUILD, ASSIMILATION_COST, loadIcon("icons/assimilations/ShiningBlack.png"));
        static { assimilations.add(ASSIMILATION_HIVEBUILD); }
        public static final Assimilation ASSIMILATION_LOCSENSE = new Assimilation(22, "ASSIMILATION_LOCSENSE", "ASSIMILATION_LOCSENSE_DESC", ASSIMILATED_LOCSENSE, ASSIMILATION_COST, loadIcon("icons/assimilations/Desert.png"));
        static { assimilations.add(ASSIMILATION_LOCSENSE); }
        public static final Assimilation ASSIMILATION_ACIDSPIT = new Assimilation(23, "ASSIMILATION_ACIDSPIT", "ASSIMILATION_ACIDSPIT_DESC", ASSIMILATED_ACIDSPIT, ASSIMILATION_COST, loadIcon("icons/assimilations/Green.png"));
        static { assimilations.add(ASSIMILATION_ACIDSPIT); }

        // --- Synergies ---
        public static final Synergy SUPER_VENOM_SYNERGY = new Synergy(1, "SYNERGY_SUPER_VENOM", "SYNERGY_SUPER_VENOM_DESC",
                SYNERGY_SUPER_VENOM, loadIcon("icons/synergies/SuperVenom.png"), ASSIMILATED_FIREVENOM, ASSIMILATED_DEADLYVENOM);
        static { synergies.add(SUPER_VENOM_SYNERGY); }
        public static final Synergy ACID_ARTILLERY_SYNERGY = new Synergy(2, "SYNERGY_ACID_ARTILLERY", "SYNERGY_ACID_ARTILLERY_DESC",
                ROLE_ARTILLERY, loadIcon("icons/synergies/AcidArtillery.png"), ASSIMILATED_ACIDSPIT, TYPE_MAJOR);
        static { synergies.add(ACID_ARTILLERY_SYNERGY); }
        public static final Synergy CORROSIVE_BOMBS_SYNERGY = new Synergy(3, "SYNERGY_CORROSIVE_BOMBS", "SYNERGY_CORROSIVE_BOMBS_DESC",
                SYNERGY_CORROSIVE_BOMBS, loadIcon("icons/synergies/CorrosiveBombs.png"), ASSIMILATED_SELFDESTRUCT, ASSIMILATED_ACIDSPIT);
        static { synergies.add(CORROSIVE_BOMBS_SYNERGY); }
        public static final Synergy AIR_BOMBER_SYNERGY = new Synergy(4, "SYNERGY_AIR_BOMBER", "SYNERGY_AIR_BOMBER_DESC",
                ROLE_AIR_BOMBER, loadIcon("icons/synergies/AirBomber.png"),
                ROLE_AIR_SUPPORT, ROLE_BOMBER, ASSIMILATED_ACIDSPIT);
        static { synergies.add(AIR_BOMBER_SYNERGY); }
        public static final Synergy WEB_BUILDING_SYNERGY = new Synergy(5, "SYNERGY_WEB_BUILDING", "SYNERGY_WEB_BUILDING_DESC",
                SYNERGY_WEB_BUILDING, loadIcon("icons/synergies/WebBuilding.png"), ASSIMILATED_SILKWEAVE, ASSIMILATED_HIVEBUILD);
        static { synergies.add(WEB_BUILDING_SYNERGY); }

        // --- Getters ---
        public static List<Upgrade> getUpgrades() { return Collections.unmodifiableList(upgrades); }

        public static List<Building> getBuildings() { return Collections.unmodifiableList(buildings); }

        public static List<Synergy> getSynergies() { return Collections.unmodifiableList(synergies); }

        public static List<Assimilation> getAssimilations() { return Collections.unmodifiableList(assimilations); }

        public static Assimilation getAssimilationRequiredForSubtypeRoleUpgrade(Upgrade upgrade) {
                if (upgrade == ROLE_POTTER) {
                        return ASSIMILATION_HONEYPOT;
                }
                if (upgrade == ROLE_DEFENDER) {
                        return ASSIMILATION_DOORHEAD;
                }
                if (upgrade == ROLE_SPY) {
                        return ASSIMILATION_STEALTH;
                }
                return null;
        }

        public static boolean meetsSubtypeRoleAssimilationRequirement(Dynasty dynasty, Upgrade upgrade) {
                Assimilation required = getAssimilationRequiredForSubtypeRoleUpgrade(upgrade);
                if (required == null) {
                        return true;
                }
                return dynasty != null && required.getReward() != null && dynasty.hasUpgrade(required.getReward());
        }

        public static boolean meetsBuildingUnlockRequirement(Colony colony, Building building) {
                if (colony == null || building == null) {
                        return false;
                }
                if (isSilkweaveBuilding(building)) {
                        if (!colony.hasUpgrade(ASSIMILATED_SILKWEAVE)) {
                                return false;
                        }
                        if (building == ROCK_WAREHOUSE_3_SILK) {
                                return colony.hasUpgrade(ROLE_MINER);
                        }
                        if (building == RESIN_RESERVOIR_3_SILK) {
                                return colony.hasUpgrade(ABILITY_RESIN);
                        }
                        return true;
                }
                if (isHiveMoundBuilding(building)) {
                        return colony.hasUpgrade(ASSIMILATED_HIVEBUILD);
                }
                if (building == PASSIVE_WEB || building == PASSIVE_WATER) {
                        return colony.hasUpgrade(SYNERGY_WEB_BUILDING);
                }
                if (isInBuildingChain(building, BUILDING_CHAIN_ROCK)) {
                        return colony.hasUpgrade(ROLE_MINER);
                }
                if (isInBuildingChain(building, BUILDING_CHAIN_RESIN)) {
                        return colony.hasUpgrade(ABILITY_RESIN);
                }
                return true;
        }

        public static boolean isSilkweaveBuilding(Building building) {
                return building == EGG_CHAMBER_3_SILK
                        || building == MUSHROOM_CHAMBER_3_SILK
                        || building == PLANT_CHAMBER_3_SILK
                        || building == WATER_RESERVOIR_3_SILK
                        || building == MEAT_CHAMBER_3_SILK
                        || building == SYRUP_RESERVOIR_3_SILK
                        || building == ROCK_WAREHOUSE_3_SILK
                        || building == RESIN_RESERVOIR_3_SILK;
        }

        public static boolean isHiveMoundBuilding(Building building) {
                return building == HIVE_MOUND_2 || building == HIVE_MOUND_4;
        }

        public static boolean isBuildingShownInTree(Colony colony, Building building) {
                if (colony == null || building == null) {
                        return false;
                }
                if (colony.hasBuilding(building)) {
                        return true;
                }
                if (isSilkweaveBuilding(building)) {
                        return colony.hasUpgrade(ASSIMILATED_SILKWEAVE);
                }
                if (isHiveMoundBuilding(building)) {
                        return colony.hasUpgrade(ASSIMILATED_HIVEBUILD);
                }
                if (building == PASSIVE_WEB || building == PASSIVE_WATER) {
                        return colony.hasUpgrade(SYNERGY_WEB_BUILDING);
                }
                return true;
        }

        public static ResourceType getBlockingBuildingMaterial(Colony colony, Building building) {
                if (colony == null || building == null) {
                        return null;
                }
                if (isInBuildingChain(building, BUILDING_CHAIN_ROCK) && !colony.hasUpgrade(ROLE_MINER)) {
                        return GameConstants.RESOURCE_ROCK;
                }
                if (building == ROCK_WAREHOUSE_3_SILK && !colony.hasUpgrade(ROLE_MINER)) {
                        return GameConstants.RESOURCE_ROCK;
                }
                if (isInBuildingChain(building, BUILDING_CHAIN_RESIN) && !colony.hasUpgrade(ABILITY_RESIN)) {
                        return GameConstants.RESOURCE_RESIN;
                }
                if (building == RESIN_RESERVOIR_3_SILK && !colony.hasUpgrade(ABILITY_RESIN)) {
                        return GameConstants.RESOURCE_RESIN;
                }
                if (building.getMineralCost() > 0
                                && colony.getMinerals() < building.getMineralCost()
                                && !colony.hasUpgrade(ROLE_MINER)) {
                        return GameConstants.RESOURCE_ROCK;
                }
                if (building.getResinCost() > 0
                                && colony.getResins() < building.getResinCost()
                                && !colony.hasUpgrade(ABILITY_RESIN)) {
                        return GameConstants.RESOURCE_RESIN;
                }
                if (building.getPlantCost() > 0 && colony.getPlants() < building.getPlantCost()) {
                        return GameConstants.RESOURCE_PLANT;
                }
                return null;
        }

        public static int countDynastyBuildingsOfTier(Dynasty dynasty, Tier tier) {
                if (dynasty == null || tier == null || dynasty.getColonies() == null) {
                        return 0;
                }
                int count = 0;
                for (Colony colony : dynasty.getColonies()) {
                        if (colony == null || colony.getUnlockedBuildings() == null) {
                                continue;
                        }
                        for (Building building : colony.getUnlockedBuildings()) {
                                if (building != null && building.getTier() == tier) {
                                        count++;
                                }
                        }
                }
                return count;
        }

        private static boolean isInBuildingChain(Building building, Building[] chain) {
                for (Building candidate : chain) {
                        if (candidate == building) {
                                return true;
                        }
                }
                return false;
        }
}
