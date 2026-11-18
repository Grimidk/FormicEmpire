package com.grimidk.formicempire.classes.entities.services.shared;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        return world.findPlayerDynasty();
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
        if (HexWaterCrossing.isWaterBiome(hex.getBiome())) {
            return false;
        }
        Colony existing = hex.getColony();
        return existing == null || ColonyStarterService.isReclaimableDeadColony(existing);
    }

    public static boolean canCheatConquerHex(World world, Hex hex) {
        if (hex == null || world == null || world.isSpectatorMode() || findPlayerDynasty(world) == null) {
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
        player.discoverHexAndAdjacent(hex);
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
        Hex conqueredHex = world.getHexOfColony(target);
        if (conqueredHex != null) {
            player.discoverHexAndAdjacent(conqueredHex);
        }
        engine.applySandboxTaintToActiveWorld();
        return target.getDynasty() == player;
    }

    public static boolean canPossessHex(World world, Hex hex) {
        if (world == null || !world.isSpectatorMode() || hex == null) {
            return false;
        }
        Colony colony = hex.getColony();
        if (colony == null || ColonyStarterService.isReclaimableDeadColony(colony)) {
            return false;
        }
        Dynasty dynasty = colony.getDynasty();
        return dynasty != null && !dynasty.isDefeated();
    }

    public static boolean possessHex(Engine engine, World world, Hex hex) {
        if (!canPossessHex(world, hex)) {
            return false;
        }
        Colony target = hex.getColony();
        Dynasty dynasty = target.getDynasty();
        if (world.getDynastys() != null) {
            for (Dynasty other : world.getDynastys()) {
                if (other == null || other == dynasty) {
                    continue;
                }
                other.setPlayer(false);
                if (other.getColonies() != null) {
                    for (Colony otherColony : other.getColonies()) {
                        if (otherColony != null) {
                            otherColony.setIsPlayer(false);
                        }
                    }
                }
            }
        }
        world.setSpectatorMode(false);
        dynasty.setPlayer(true);
        if (dynasty.getColonies() != null) {
            for (Colony colony : dynasty.getColonies()) {
                if (colony == null) {
                    continue;
                }
                colony.setIsPlayer(true);
                Hex colonyHex = world.getHexOfColony(colony);
                if (colonyHex != null) {
                    dynasty.discoverHexAndAdjacent(colonyHex);
                }
            }
        }
        target.setIsPlayer(true);
        dynasty.discoverHexAndAdjacent(hex);
        world.changeActiveHex(hex);
        if (engine != null) {
            engine.clampSpeedToCurrentRules();
        }
        return true;
    }

    public static boolean canDepossessToSpectator(World world) {
        return world != null && !world.isSpectatorMode() && findPlayerDynasty(world) != null;
    }

    public static boolean depossessToSpectator(Engine engine, World world) {
        if (!canDepossessToSpectator(world)) {
            return false;
        }
        Dynasty player = findPlayerDynasty(world);
        if (player != null) {
            player.setPlayer(false);
            if (player.getColonies() != null) {
                for (Colony colony : player.getColonies()) {
                    if (colony == null) {
                        continue;
                    }
                    colony.setIsPlayer(false);
                    colony.setAutomationEnabled(true);
                }
            }
        }
        if (world.getDynastys() != null) {
            for (Dynasty dynasty : world.getDynastys()) {
                if (dynasty == null) {
                    continue;
                }
                dynasty.setPlayer(false);
                if (dynasty.getColonies() != null) {
                    for (Colony colony : dynasty.getColonies()) {
                        if (colony != null) {
                            colony.setIsPlayer(false);
                        }
                    }
                }
            }
        }
        world.setSpectatorMode(true);
        if (world.getActiveHex() != null && world.getActiveHex().getColony() != null) {
            Colony activeColony = world.getActiveHex().getColony();
            activeColony.invalidateActiveRoleCountCache();
            activeColony.runRoleAssignment(engine);
        }
        return true;
    }

    public static War triggerRandomBorderWar(World world) {
        if (world == null || world.getWarService() == null || world.getHexes() == null) {
            return null;
        }
        List<Dynasty[]> nonPactedPairs = new ArrayList<>();
        List<Dynasty[]> allBorderPairs = new ArrayList<>();
        Set<Long> seenPairs = new LinkedHashSet<>();
        for (Hex hex : world.getHexes()) {
            if (hex == null || hex.getColony() == null) {
                continue;
            }
            Dynasty d1 = hex.getColony().getDynasty();
            if (d1 == null || d1.isDefeated() || d1.getDiplomacyService() == null) {
                continue;
            }
            for (Hex neighbor : hex.getAdjacentNeighbors()) {
                if (neighbor == null || neighbor.getColony() == null) {
                    continue;
                }
                Dynasty d2 = neighbor.getColony().getDynasty();
                if (d2 == null || d2 == d1 || d2.isDefeated() || d2.getDiplomacyService() == null) {
                    continue;
                }
                int low = Math.min(d1.getId(), d2.getId());
                int high = Math.max(d1.getId(), d2.getId());
                long key = (((long) low) << 32) | (high & 0xffffffffL);
                if (!seenPairs.add(key)) {
                    continue;
                }
                if (world.getWarService().findActiveWar(low, high) != null) {
                    continue;
                }
                Dynasty[] pair = new Dynasty[] { d1, d2 };
                allBorderPairs.add(pair);
                if (!d1.getDiplomacyService().hasNonAggressionPact(d2)) {
                    nonPactedPairs.add(pair);
                }
            }
        }
        List<Dynasty[]> pool = !nonPactedPairs.isEmpty() ? nonPactedPairs : allBorderPairs;
        if (pool.isEmpty()) {
            return null;
        }
        Dynasty[] chosen = pool.get(GameRandom.nextInt(pool.size()));
        if (chosen[0].getDiplomacyService().hasNonAggressionPact(chosen[1])) {
            chosen[0].getDiplomacyService().breakNonAggressionPact(chosen[1], world);
        }
        return world.getWarService().beginWar(chosen[0], chosen[1]);
    }
}
