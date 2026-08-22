package com.grimidk.formicempire.classes.entities.services.shared;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

public final class SandboxCheatService {
    private SandboxCheatService() {
    }

    public static void applyAssimilateAllIfEnabled(Engine engine) {
        applyEnabledCheats(engine);
    }

    public static void applyEnabledCheats(Engine engine) {
        if (engine == null) {
            return;
        }
        World world = engine.getWorld();
        if (world == null || world.getDynastys() == null) {
            return;
        }
        boolean applied = false;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty == null || !dynasty.isPlayer()) {
                continue;
            }
            if (engine.isInfiniteResearch()) {
                dynasty.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);
                dynasty.unlockUpgrade(GameUnlocks.ABILITY_RESEARCH);
                applied = true;
            }
            if (engine.isInstantBuildings()) {
                dynasty.unlockUpgrade(GameUnlocks.ROLE_BUILDER);
                dynasty.unlockUpgrade(GameUnlocks.ABILITY_BUILD);
                applied = true;
            }
            if (engine.isAssimilateAll()) {
                grantAllAssimilations(dynasty);
                dynasty.unlockUpgrade(GameUnlocks.ABILITY_RESEARCH);
                dynasty.unlockUpgrade(GameUnlocks.ABILITY_ASSIMILATION);
                applied = true;
            }
        }
        if (applied) {
            engine.applySandboxTaintToActiveWorld();
        }
    }

    public static void grantAllAssimilations(Dynasty dynasty) {
        if (dynasty == null) {
            return;
        }
        for (AntSpecies species : GameConstants.getSpecies()) {
            if (species != null) {
                dynasty.absorbSpecies(species.getId());
            }
        }
        for (Assimilation assimilation : GameUnlocks.getAssimilations()) {
            if (assimilation == null) {
                continue;
            }
            dynasty.completeAssimilation(assimilation);
            Upgrade reward = assimilation.getReward();
            if (reward != null) {
                dynasty.unlockUpgrade(reward);
            }
        }
        if (GameUnlocks.ABILITY_ASSIMILATION != null) {
            dynasty.unlockUpgrade(GameUnlocks.ABILITY_ASSIMILATION);
        }
    }

    public static Dynasty findPlayerDynasty(World world) {
        if (world == null) {
            return null;
        }
        if (world.getDynastys() != null) {
            for (Dynasty dynasty : world.getDynastys()) {
                if (dynasty != null && dynasty.isPlayer()) {
                    return dynasty;
                }
            }
        }
        Hex spawn = world.getSpawnHex();
        if (spawn != null && spawn.getColony() != null && spawn.getColony().getDynasty() != null) {
            return spawn.getColony().getDynasty();
        }
        return null;
    }

    public static boolean isForeignColony(World world, Colony colony) {
        if (colony == null || colony.getDynasty() == null) {
            return false;
        }
        Dynasty player = findPlayerDynasty(world);
        if (player == null) {
            return !colony.belongsToPlayerDynasty();
        }
        return colony.getDynasty().getId() != player.getId();
    }

    public static boolean isColonizableEmptyHex(Hex hex) {
        if (hex == null) {
            return false;
        }
        if (hex.getBiome() == GameConstants.BIOME_OCEAN || hex.getBiome() == GameConstants.BIOME_LAKE) {
            return false;
        }
        Colony existing = hex.getColony();
        return existing == null || ColonyStarterService.isReclaimableDeadColony(existing);
    }

    public static boolean canCheatConquerHex(World world, Hex hex) {
        if (hex == null) {
            return false;
        }
        return isForeignColony(world, hex.getColony()) || isColonizableEmptyHex(hex);
    }

    public static boolean cheatConquerColony(Engine engine, Colony target) {
        World world = engine != null ? engine.getWorld() : null;
        return cheatConquerColony(engine, world, target);
    }

    public static boolean cheatConquerHex(Engine engine, World world, Hex hex) {
        if (engine == null || !engine.isEasyConquering() || hex == null || world == null) {
            return false;
        }
        if (isForeignColony(world, hex.getColony())) {
            return cheatConquerColony(engine, world, hex.getColony());
        }
        if (!isColonizableEmptyHex(hex)) {
            return false;
        }
        Dynasty player = findPlayerDynasty(world);
        if (player == null) {
            return false;
        }
        Colony founded = ColonyStarterService.shared().foundColonyOnEmptyHex(world, player, hex);
        if (founded == null || founded.getDynasty() == null || founded.getDynasty().getId() != player.getId()) {
            return false;
        }
        engine.applySandboxTaintToActiveWorld();
        return true;
    }

    public static boolean cheatConquerColony(Engine engine, World world, Colony target) {
        if (engine == null || !engine.isEasyConquering() || target == null || world == null) {
            return false;
        }
        Dynasty player = findPlayerDynasty(world);
        if (player == null) {
            return false;
        }
        Dynasty loser = target.getDynasty();
        if (loser != null && loser.getId() == player.getId()) {
            return false;
        }

        boolean wasLoserCapital = target.isCapital();
        if (loser != null) {
            loser.removeColony(target);
        }
        target.setDynasty(player);
        target.setCapital(false);
        if (loser != null && wasLoserCapital) {
            loser.promoteNewCapital();
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(target);
        ColonyMilitaryService.refreshDynastyMilitaryPower(player);
        if (loser != null) {
            ColonyMilitaryService.refreshDynastyMilitaryPower(loser);
        }
        ColonyStarterService.shared().stabilizeConqueredColony(player, target);
        engine.applySandboxTaintToActiveWorld();
        return target.getDynasty() == player;
    }
}
