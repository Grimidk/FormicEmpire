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
    public static final Upgrade TYPE_EGG = new Upgrade(1, "Egg, Larva and Pupa Type", "Brand New Ants", "Unlocks the baby ant types, allowing new ants to be born. The colony can hold 50 of each type of baby ant.", null );
    public static final Upgrade TYPE_WORKER = new Upgrade(2, "Worker Type", "Means of Production", "Unlocks the worker type, so your colony can sustain itself.", TYPE_EGG );
    public static final Upgrade TYPE_SOLDIER = new Upgrade(3, "Soldier Type", "The Red Army", "Unlocks the soldier type, you can now defend your colony.", TYPE_EGG );
    public static final Upgrade TYPE_MAJOR = new Upgrade(4, "Major Type", "Ant Tanks", "Unlocks the major type, the bulkiest ants around.", TYPE_SOLDIER );
    public static final Upgrade TYPE_PRINCESS = new Upgrade(5, "Princess and Drone Types", "Winged Ants", "Unlocks the princess and drone types, so new colonies can exist.", TYPE_EGG );
    public static final Upgrade TYPE_QUEEN = new Upgrade(6, "Queen Type", "Ant Royalty", "Unlocks the queen type, the leader of the colony. Your colony can currently handle 1 queen.", TYPE_PRINCESS );
    // -- Roles --
    public static final Upgrade ROLE_FORAGER = new Upgrade(7, "Forager Role", "Nature's Bounty", "Allows workers to collect plants and mushrooms. 1 ant can collect 1 resource per hour. Your colony can hold 8000 mushrooms and 4000 plants.", TYPE_WORKER );
    public static final Upgrade ROLE_NURSE = new Upgrade(8, "Nurse Role", "Maternity Leave", "Allows workers to take care of baby ants. 1 ant can take care of 10 babies, excess babies can perish overnight.", TYPE_WORKER );
    public static final Upgrade ROLE_FARMER = new Upgrade(9, "Farmer Role", "Grandpa's Farm", "Allows workers to convert plant and animal matter into mushroom. 1 ant can convert 1 plant and 1 protein per minute, in a 1:1 and 1:2 ratio respectively.", TYPE_WORKER );
    public static final Upgrade ROLE_GRAVER = new Upgrade(10, "Grave-Keeper Role", "Grim Reaper", "Allows workers to bury dead ants. 1 ant can take care of 5 dead ants, too many dead ants can attract disease.", TYPE_WORKER );
    public static final Upgrade ROLE_HUNTER = new Upgrade(11, "Hunter Role", "Hunter Instinct", "Allows soldiers to gather animal matter.  1 ant can collect 1 resource per hour. Your colony can hold 2000 proteins", TYPE_SOLDIER );
    public static final Upgrade ROLE_LAYER = new Upgrade(12, "Egg Layer Role", "Ant Factory", "Allows queens to lay new eggs. 1 ant can lay 1 egg per hour.", TYPE_QUEEN );
    public static final Upgrade ROLE_RANCHER = new Upgrade(13, "Rancher Role", "Aphid Rancher", "Allows workers to ranch aphids. 1 ant can handle 10 aphids and herd 1 additional aphid per day. Each aphid produces 1 hoenydew (syrup) and your colony has a capacity of 500.", ROLE_FORAGER );
    public static final Upgrade ROLE_RESEARCHER = new Upgrade(14, "Research Role", "Ant Science", "Allows queens to generate reserach points. 1 ant research 1 point per hour.", TYPE_QUEEN );


    // public static final AntRole ROLE_BUILDER = new AntRole(3, TYPE_WORKER, "Builder");
    // public static final AntRole ROLE_SCOUT = new AntRole(4, TYPE_WORKER, "Scout");
    // public static final AntRole ROLE_MINER = new AntRole(8, TYPE_WORKER, "Miner");
    // public static final AntRole ROLE_COURIER = new AntRole(9, TYPE_WORKER, "Courier");
    // public static final AntRole ROLE_POTTER = new AntRole(10, TYPE_WORKER, "Portable-Feeder");

    // public static final AntRole ROLE_GUARD = new AntRole(11, TYPE_SOLDIER, "Guard");
    // public static final AntRole ROLE_WARRIOR = new AntRole(12, TYPE_SOLDIER, "Warrior");
    // public static final AntRole ROLE_DEFENDER = new AntRole(13, TYPE_SOLDIER, "Defender");
    // public static final AntRole ROLE_POLICE = new AntRole(14, TYPE_SOLDIER, "Parasite-Police");
    // public static final AntRole ROLE_BOMBER = new AntRole(15, TYPE_SOLDIER, "Bomber");

    // public static final AntRole ROLE_BRUTE = new AntRole(17, TYPE_MAJOR, "Brute");
    // public static final AntRole ROLE_CARRIER = new AntRole(18, TYPE_MAJOR, "Troop-Carrier");
    // public static final AntRole ROLE_ARTILLERY = new AntRole(19, TYPE_MAJOR, "Artillery");
    // public static final AntRole ROLE_SIEGE = new AntRole(20, TYPE_MAJOR, "Siege-Engine");
    // public static final AntRole ROLE_BORER = new AntRole(21, TYPE_MAJOR, "Boring-Machine");

    // public static final AntRole ROLE_DRONE = new AntRole(22, TYPE_DRONE, "Drone");
    // public static final AntRole ROLE_BREEDER = new AntRole(23, TYPE_PRINCESS, "Breeder");
    // public static final AntRole ROLE_DIPLOMAT = new AntRole(24, TYPE_PRINCESS, "Diplomat");

        // this.growthTime = 4;
        // this.parasiteDetection = 10;

        // this.baseHealth = 100;
        // this.baseAge = 180;
        // this.baseTempRes = 25;
        // this.baseRegen = 1;
        // this.baseConsumption = 1;
        // this.baseAttack = 10;
        // this.baseAttackSpeed = 1;
        // this.baseDefense = 5;
        // this.baseSpeed = 1;
        // this.baseSize = 1;

        // this.waterCapacity = 1000;
        // this.resinsCapacity = 200;
        // this.mineralsCapacity = 100;

    // -- Stats -- 

    // -- Powers --

    // -- Abilities --
    
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

        synergies.add(null);
    }

    public static List<Upgrade> getUpgrades() {
        return Collections.unmodifiableList(upgrades);
    }

    public static List<Synergy> getSynergies() {
        return Collections.unmodifiableList(synergies);
    }
}
