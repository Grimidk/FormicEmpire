package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.constants.Synergy;
import com.grimidk.formicempire.classes.constants.Upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameUpgrades {
    private GameUpgrades() {}

    // --- Upgrades ---
    // -- Types --
    public static final Upgrade TYPE_EGG = new Upgrade(1, "Egg, Larva and Pupa Type", "Brand New Ants", "Unlocks the baby ant types, allowing new ants to be born. The colony can hold 50 of each type of baby ant. Each stage takes 4 days to grow out of.", null, 0 );
    public static final Upgrade TYPE_WORKER = new Upgrade(2, "Worker Type", "Means of Production", "Unlocks the worker type, so your colony can sustain itself.", TYPE_EGG, 0 );
    public static final Upgrade TYPE_SOLDIER = new Upgrade(3, "Soldier Type", "The Red Army", "Unlocks the soldier type, you can now defend your colony.", TYPE_EGG, 100 );
    public static final Upgrade TYPE_MAJOR = new Upgrade(4, "Major Type", "Ant Tanks", "Unlocks the major type, the bulkiest ants around.", TYPE_SOLDIER, 5000 );
    public static final Upgrade TYPE_PRINCESS = new Upgrade(5, "Princess and Drone Types", "Winged Ants", "Unlocks the princess and drone types, so new colonies can exist or you can get more queens.", TYPE_EGG,3000 );
    public static final Upgrade TYPE_QUEEN = new Upgrade(6, "Queen Type", "Ant Royalty", "Unlocks the queen type, the leader of the colony. Your colony can currently handle 1 queen.", TYPE_PRINCESS, 10000 );
    // -- Roles --
    public static final Upgrade ROLE_FORAGER = new Upgrade(7, "Forager Role", "Nature's Bounty", "Allows workers to collect plants and mushrooms. 1 ant can collect 1 resource per hour. Your colony can hold 8000 mushrooms, 4000 plants and 1000 water.", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_NURSE = new Upgrade(8, "Nurse Role", "Maternity Leave", "Allows workers to take care of baby ants. 1 ant can take care of 10 babies, excess babies can perish overnight.", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_FARMER = new Upgrade(9, "Farmer Role", "Grandpa's Farm", "Allows workers to convert plant and animal matter into mushroom. 1 ant can convert 1 plant and 1 protein per minute, in a 1:1 and 1:2 ratio respectively.", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_GRAVER = new Upgrade(10, "Grave-Keeper Role", "Grim Reaper", "Allows workers to bury dead ants. 1 ant can take care of 5 dead ants, too many dead ants can attract disease.", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_HUNTER = new Upgrade(11, "Hunter Role", "Hunter Instinct", "Allows soldiers to gather animal matter.  1 ant can collect 1 resource per hour. Your colony can hold 2000 proteins", TYPE_SOLDIER, 1 );
    public static final Upgrade ROLE_LAYER = new Upgrade(12, "Egg Layer Role", "Ant Factory", "Allows queens to lay new eggs. 1 ant can lay 1 egg per hour.", TYPE_QUEEN, 0 );
    public static final Upgrade ROLE_RANCHER = new Upgrade(13, "Rancher Role", "Aphid Rancher", "Allows workers to ranch aphids. 1 ant can handle 10 aphids and herd 1 additional aphid per day. Each aphid produces 1 hoenydew (syrup) and your colony has a capacity of 500.", ROLE_FORAGER, 250 );
    public static final Upgrade ROLE_RESEARCHER = new Upgrade(14, "Research Role", "Ant Science", "Allows queens to generate reserach points. 1 ant research 1 point per hour.", TYPE_QUEEN, 0 );
    public static final Upgrade ROLE_BUILDER = new Upgrade(15, "Builde Role", "Base Building", "Allows workers to build new rooms in the colony, you can build in the build menu (U).", TYPE_WORKER, 500 );
    public static final Upgrade ROLE_SCOUT = new Upgrade(16, "Scout Role", "Adventure's Call", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_MINER = new Upgrade(17, "Miner Role", "Yearn For The Mines", "", TYPE_WORKER, 0 );        // this.mineralsCapacity = 100;
    public static final Upgrade ROLE_COURIER = new Upgrade(18, "Courier Role", "Logistic Network", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_POTTER = new Upgrade(19, "Portable-Feeder Role", "A Helping Hand", "", TYPE_WORKER, 0 );
    public static final Upgrade ROLE_GUARD = new Upgrade(20, "Guard Role", "Ant Bouncers", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_WARRIOR = new Upgrade(21, "Warrior Role", "Phalanx Formation", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_DEFENDER = new Upgrade(22, "Defender Role", "Royal Shield", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_POLICE = new Upgrade(23, "Parasite-Police Role", "Police Brutality", "", TYPE_SOLDIER, 0 );  // this.parasiteDetection = 10;
    public static final Upgrade ROLE_BOMBER = new Upgrade(24, "Bomber Role", "Explosive Finish", "", TYPE_SOLDIER, 0 );
    public static final Upgrade ROLE_BRUTE = new Upgrade(25, "Brute Role", "Heavy Trooper", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_CARRIER = new Upgrade(26, "Carrier Role", "Troop Transport", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_ARTILLERY = new Upgrade(27, "Artillery Role", "Long Range Artillery", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_SIEGE = new Upgrade(28, "Siege-Machine Role", "Siege Technology", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_BORER = new Upgrade(29, "Tunel-Borer Role", "Ant Excavator", "", TYPE_MAJOR, 0 );
    public static final Upgrade ROLE_BREEDER = new Upgrade(30,"Breeder Roles", "Nuptial Flights", "Allows Princesses and Drones to go to nuptial flights and get more queens", TYPE_PRINCESS, 1 );
    public static final Upgrade ROLE_DIPLOMAT = new Upgrade(31, "Diplomat Role", "Mighter Than The Sword", "", TYPE_PRINCESS, 0 );
    // -- Stats -- 
    public static final Upgrade STAT_SKELETON = new Upgrade(32, "Basic Skeletons", "Basic Exoskeletons", "The basic defense stats for all the ants in your colony before multipliers. 100 health points, 5 defense points and 1 health point recovered per second.", TYPE_EGG, 0 );
    public static final Upgrade STAT_ACID = new Upgrade(33, "Acid Spiting", "Acidic Spit", "The basic offensive stats for all the ants in your colony before multipliers. 10 attack points, 1 attack per second and 1 speed point.", TYPE_EGG, 0 );
    public static final Upgrade STAT_LONGEVITY = new Upgrade(34, "Basic Longevity", "Standard Lifespan", "The basic life stats for all the ants in your colony before multipliers. 180 day lifespan, 25% temperature resistance and 4 days to grow per infant stage.", TYPE_EGG, 0 );

    // -- Abilities --
    public static final Upgrade ABILITY_RESEARCH = new Upgrade(35, "Research", "Micro Microscopes", "Allows you to research more ant technology by using research points, go to the research menu (Y).", ROLE_RESEARCHER, 0 );

    
    // --- Synergies ---



    // --- Lists ---
    private static final List<Upgrade> upgrades = new ArrayList<>();
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
        upgrades.add(ROLE_BREEDER);
        upgrades.add(ROLE_RESEARCHER);
        upgrades.add(STAT_SKELETON);
        upgrades.add(STAT_ACID);
        upgrades.add(STAT_LONGEVITY);
        upgrades.add(ABILITY_RESEARCH);

        synergies.add(null);
    }

    public static List<Upgrade> getUpgrades() {
        return Collections.unmodifiableList(upgrades);
    }

    public static List<Synergy> getSynergies() {
        return Collections.unmodifiableList(synergies);
    }
}
