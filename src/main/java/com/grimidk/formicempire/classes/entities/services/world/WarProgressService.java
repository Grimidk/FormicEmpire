package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.Map;

public final class WarProgressService {

    private WarProgressService() {
    }

    public static void tickActiveWarsHourly(World world, WarService warService) {
        if (world == null || warService == null) {
            return;
        }
        for (War war : warService.getActiveWars()) {
            if (!war.isActive() || !war.isCampaignInitialized()) {
                continue;
            }
            Dynasty aggressor = world.findDynastyById(war.getAggressorDynastyId());
            Dynasty defender = world.findDynastyById(war.getDefenderDynastyId());
            if (aggressor == null || defender == null || aggressor.isDefeated() || defender.isDefeated()) {
                continue;
            }
            if (!aggressor.getDiplomacyService().isAtWarWith(defender)) {
                continue;
            }
            if (war.getStagePhase() == WarStagePhase.REDEPLOYING) {
                tickRedeployHour(world, warService, war, aggressor, defender);
                continue;
            }
            tickWarHour(world, warService, war, aggressor, defender);
        }
    }

    public static void tickActiveWarsDaily(World world, WarService warService) {
        if (world == null || warService == null) {
            return;
        }
        for (War war : warService.getActiveWars()) {
            if (!war.isActive()) {
                continue;
            }
            Dynasty aggressor = world.findDynastyById(war.getAggressorDynastyId());
            Dynasty defender = world.findDynastyById(war.getDefenderDynastyId());
            if (aggressor == null || defender == null || aggressor.isDefeated() || defender.isDefeated()) {
                continue;
            }
            if (!aggressor.getDiplomacyService().isAtWarWith(defender)) {
                continue;
            }
            if (!war.isCampaignInitialized()) {
                initializeCampaign(world, war, aggressor, defender);
            }
        }
    }

    public static void tickActiveWars(World world, WarService warService) {
        tickActiveWarsHourly(world, warService);
        tickActiveWarsDaily(world, warService);
    }

    public static void initializeCampaign(World world, War war, Dynasty aggressor, Dynasty defender) {
        if (world == null || war == null || aggressor == null || defender == null) {
            return;
        }
        Colony aggressorCapital = aggressor.getCapital();
        Colony defenderCapital = defender.getCapital();
        if (aggressorCapital == null || defenderCapital == null) {
            return;
        }

        int totalStages = Math.max(1, world.colonyHexDistance(aggressorCapital, defenderCapital));
        Colony contested = findInitialContestedColony(world, aggressor, defender, aggressorCapital);
        if (contested == null) {
            contested = defenderCapital;
        }

        war.setTotalStages(totalStages);
        war.setAggressorStagesCaptured(0);
        war.setDefenderStagesCaptured(0);
        war.setStageProgress(0f);
        war.setStagePhase(WarStagePhase.ACTIVE_CLASH);
        war.setContestedColonyId(contested.getId());
        war.setStageAttackerDynastyId(aggressor.getId());
        war.setAggressorCapitalColonyId(aggressorCapital.getId());
        war.setDefenderCapitalColonyId(defenderCapital.getId());
        beginActiveClash(world, war, aggressor, defender);
        war.recomputeProgressPercent();
    }

    public static boolean canForfeitStage(World world, War war, Dynasty forfeitier) {
        if (world == null || war == null || forfeitier == null || !war.isActive() || !war.isCampaignInitialized()) {
            return false;
        }
        if (war.getStagePhase() == WarStagePhase.REDEPLOYING) {
            return false;
        }
        if (!war.involves(forfeitier.getId())) {
            return false;
        }
        Colony contested = findColonyById(world, war.getContestedColonyId());
        if (contested == null || contested.isCapital()) {
            return false;
        }
        Dynasty stageDefender = contested.getDynasty();
        Dynasty stageAttacker = world.findDynastyById(war.getStageAttackerDynastyId());
        if (stageDefender == null || stageAttacker == null) {
            return false;
        }
        return forfeitier.getId() == stageDefender.getId() || forfeitier.getId() == stageAttacker.getId();
    }

    public static boolean forfeitStage(World world, WarService warService, War war, Dynasty forfeitier) {
        if (!canForfeitStage(world, war, forfeitier)) {
            return false;
        }
        Dynasty aggressor = world.findDynastyById(war.getAggressorDynastyId());
        Dynasty defender = world.findDynastyById(war.getDefenderDynastyId());
        if (aggressor == null || defender == null) {
            return false;
        }

        Colony contested = findColonyById(world, war.getContestedColonyId());
        if (contested == null) {
            return false;
        }

        Dynasty stageDefender = contested.getDynasty();
        Dynasty stageAttacker = world.findDynastyById(war.getStageAttackerDynastyId());
        if (stageDefender == null || stageAttacker == null) {
            return false;
        }

        if (forfeitier.getId() == stageDefender.getId()) {
            completeStage(world, warService, war, aggressor, defender, stageAttacker, stageDefender, contested, false);
            return true;
        }
        if (forfeitier.getId() == stageAttacker.getId()) {
            completeStage(world, warService, war, aggressor, defender, stageAttacker, stageDefender, contested, true);
            return true;
        }
        return false;
    }

    private static void tickWarHour(World world, WarService warService, War war, Dynasty aggressor, Dynasty defender) {
        if (war.getProgressPercent() >= 100f) {
            warService.concludeWar(war, aggressor.getId(), LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY);
            return;
        }
        if (war.getProgressPercent() <= 0f) {
            warService.concludeWar(war, defender.getId(), LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY);
            return;
        }

        Colony contested = findColonyById(world, war.getContestedColonyId());
        if (contested == null || contested.getDynasty() == null) {
            reassignContestedColony(world, war, aggressor, defender);
            contested = findColonyById(world, war.getContestedColonyId());
            if (contested == null) {
                return;
            }
        }

        Dynasty stageAttacker = world.findDynastyById(war.getStageAttackerDynastyId());
        Dynasty stageDefender = contested.getDynasty();
        if (stageAttacker == null || stageDefender == null || stageAttacker == stageDefender) {
            reassignContestedColony(world, war, aggressor, defender);
            return;
        }

        if (war.getStagePhase() == WarStagePhase.ACTIVE_CLASH) {
            resolveActiveClashHour(world, war, stageAttacker, stageDefender);
            if (war.getDeployedActiveAttacker() <= 0 || war.getDeployedActiveDefender() <= 0) {
                beginReserveAssault(world, war, stageAttacker, stageDefender);
            }
            return;
        }

        resolveReserveAssaultHour(world, warService, war, aggressor, defender, stageAttacker, stageDefender, contested);
    }

    private static void tickRedeployHour(World world, WarService warService, War war,
            Dynasty aggressor, Dynasty defender) {
        int remaining = war.getRedeployHoursRemaining() - 1;
        war.setRedeployHoursRemaining(remaining);
        if (remaining <= 0) {
            Colony contested = findColonyById(world, war.getContestedColonyId());
            if (contested != null) {
                notifyPlayerWarStageClash(world, war, contested);
            }
            beginActiveClash(world, war, aggressor, defender);
        }
    }

    private static void considerAiFallback(World world, WarService warService, War war,
            Dynasty aggressor, Dynasty defender) {
        if (warService == null) {
            warService = world != null ? world.getWarService() : null;
        }
        if (warService == null) {
            return;
        }
        tryAiFallbackForDynasty(world, warService, war, aggressor, defender);
        if (!war.isActive()) {
            return;
        }
        tryAiFallbackForDynasty(world, warService, war, defender, aggressor);
    }

    private static void tryAiFallbackForDynasty(World world, WarService warService, War war,
            Dynasty ai, Dynasty opponent) {
        if (ai == null || opponent == null || ai.isPlayer() || ai.isDefeated()) {
            return;
        }
        if (!canAiFallback(world, war, ai, opponent)) {
            return;
        }
        if (GameRandom.nextDouble() < GameConstants.AI_WAR_FALLBACK_CHANCE) {
            forfeitStage(world, warService, war, ai);
        }
    }

    static boolean canAiFallback(World world, War war, Dynasty ai, Dynasty opponent) {
        if (world == null || war == null || ai == null || opponent == null || !war.isActive()) {
            return false;
        }
        if (war.getStagePhase() != WarStagePhase.REDEPLOYING) {
            return false;
        }
        int aiActive = ColonyMilitaryService.powerForWarStanding(ai);
        int opponentActive = ColonyMilitaryService.powerForWarStanding(opponent);
        if (aiActive > opponentActive * GameConstants.WAR_AI_FALLBACK_MAX_POWER_RATIO) {
            return false;
        }
        Colony contested = findColonyById(world, war.getContestedColonyId());
        if (contested == null || contested.isCapital()) {
            return false;
        }
        if (ai.getColonies().size() < GameConstants.WAR_AI_FALLBACK_MIN_SPARE_COLONIES) {
            return false;
        }
        if (aiActive < GameConstants.WAR_AI_FALLBACK_MIN_ACTIVE) {
            return false;
        }
        int stageStartActive = war.getStageStartActiveFor(ai.getId());
        if (stageStartActive > 0
                && aiActive < stageStartActive * GameConstants.WAR_AI_FALLBACK_RECOVERY_RATIO) {
            return false;
        }
        Dynasty stageDefender = contested.getDynasty();
        Dynasty stageAttacker = world.findDynastyById(war.getStageAttackerDynastyId());
        if (stageDefender == null || stageAttacker == null) {
            return false;
        }
        return ai.getId() == stageDefender.getId() || ai.getId() == stageAttacker.getId();
    }

    private static void resolveActiveClashHour(World world, War war, Dynasty stageAttacker, Dynasty stageDefender) {
        int attackerPower = war.getDeployedActiveAttacker();
        int defenderPower = war.getDeployedActiveDefender();
        if (attackerPower <= 0 && defenderPower <= 0) {
            return;
        }

        BattleTickResult result = resolveBattleTick(attackerPower, defenderPower);
        war.setDeployedActiveAttacker(Math.max(0, attackerPower - result.attackerLoss));
        war.setDeployedActiveDefender(Math.max(0, defenderPower - result.defenderLoss));

        applyActivePoolLoss(stageAttacker, result.attackerLoss);
        applyActivePoolLoss(stageDefender, result.defenderLoss);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageAttacker);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageDefender);

        war.setStageProgress(Math.min(1f, war.getStageProgress() + GameConstants.WAR_STAGE_PROGRESS_PER_HOUR));
        war.recomputeProgressPercent();
    }

    private static void beginReserveAssault(World world, War war, Dynasty stageAttacker, Dynasty stageDefender) {
        Dynasty activeWinner;
        Dynasty activeLoser;
        int winnerPower;
        if (war.getDeployedActiveAttacker() > 0 && war.getDeployedActiveDefender() <= 0) {
            activeWinner = stageAttacker;
            activeLoser = stageDefender;
            winnerPower = war.getDeployedActiveAttacker();
        } else if (war.getDeployedActiveDefender() > 0 && war.getDeployedActiveAttacker() <= 0) {
            activeWinner = stageDefender;
            activeLoser = stageAttacker;
            winnerPower = war.getDeployedActiveDefender();
        } else {
            activeWinner = war.getDeployedActiveAttacker() >= war.getDeployedActiveDefender()
                    ? stageAttacker : stageDefender;
            activeLoser = activeWinner == stageAttacker ? stageDefender : stageAttacker;
            winnerPower = Math.max(war.getDeployedActiveAttacker(), war.getDeployedActiveDefender());
        }

        Colony reserveTarget = findBorderColonyFacing(world, activeLoser, activeWinner);
        if (reserveTarget == null) {
            reserveTarget = findColonyById(world, war.getContestedColonyId());
        }
        if (reserveTarget == null) {
            beginActiveClash(world, war,
                    world.findDynastyById(war.getAggressorDynastyId()),
                    world.findDynastyById(war.getDefenderDynastyId()));
            return;
        }

        ColonyMilitaryService.refreshColonyMilitaryPower(reserveTarget);
        war.setContestedColonyId(reserveTarget.getId());
        war.setStageAttackerDynastyId(activeWinner.getId());
        war.setDeployedActiveAttacker(winnerPower);
        war.setDeployedActiveDefender(0);
        war.setDeployedReserveDefender(ColonyMilitaryService.computeReserveMilitaryPower(reserveTarget));
        war.setStagePhase(WarStagePhase.RESERVE_ASSAULT);
    }

    private static void resolveReserveAssaultHour(World world, WarService warService, War war,
            Dynasty aggressor, Dynasty defender, Dynasty stageAttacker, Dynasty stageDefender, Colony contested) {
        int attackerPower = war.getDeployedActiveAttacker();
        int reservePower = war.getDeployedReserveDefender();
        if (attackerPower <= 0 && reservePower <= 0) {
            beginActiveClash(world, war, aggressor, defender);
            return;
        }

        BattleTickResult result = resolveBattleTick(attackerPower, reservePower);
        war.setDeployedActiveAttacker(Math.max(0, attackerPower - result.attackerLoss));
        war.setDeployedReserveDefender(Math.max(0, reservePower - result.defenderLoss));

        applyActivePoolLoss(stageAttacker, result.attackerLoss);
        applyReservePoolLoss(contested, result.defenderLoss);
        ColonyMilitaryService.refreshColonyMilitaryPower(contested);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageAttacker);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageDefender);

        war.setStageProgress(Math.min(1f, war.getStageProgress() + GameConstants.WAR_STAGE_PROGRESS_PER_HOUR));
        war.recomputeProgressPercent();

        if (war.getDeployedReserveDefender() <= 0 || !colonyHasQueenDefense(contested)) {
            completeStage(world, warService, war, aggressor, defender, stageAttacker,
                    contested.getDynasty(), contested, false);
            return;
        }
        if (war.getDeployedActiveAttacker() <= 0) {
            enterRedeploying(world, war, aggressor, defender);
        }
    }

    private static void completeStage(World world, WarService warService, War war,
            Dynasty aggressor, Dynasty defender, Dynasty stageAttacker, Dynasty stageDefender,
            Colony contested, boolean attackerRetreat) {
        Dynasty victor = attackerRetreat ? stageDefender : stageAttacker;
        Dynasty hexOwner = contested.getDynasty();
        boolean captured = !attackerRetreat && victor.getId() == stageAttacker.getId()
                && hexOwner != null && hexOwner != victor;
        boolean contestedWasCapital = captured && contested.isCapital();

        if (captured) {
            captureColony(world, warService, war, contested, victor, hexOwner, true);
        }

        if (victor.getId() == aggressor.getId()) {
            war.setAggressorStagesCaptured(war.getAggressorStagesCaptured() + 1);
        } else if (victor.getId() == defender.getId()) {
            war.setDefenderStagesCaptured(war.getDefenderStagesCaptured() + 1);
        }

        war.setStageProgress(0f);
        war.recomputeProgressPercent();

        notifyPlayerWarStageComplete(world, war, victor, contested, attackerRetreat, captured);

        if (contestedWasCapital && victor.getId() == aggressor.getId()) {
            war.setProgressPercent(100f);
            warService.concludeWar(war, aggressor.getId(), LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY);
            return;
        }
        if (contestedWasCapital && victor.getId() == defender.getId()) {
            war.setProgressPercent(0f);
            warService.concludeWar(war, defender.getId(), LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY);
            return;
        }
        if (war.getProgressPercent() >= 100f) {
            warService.concludeWar(war, aggressor.getId(), LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY);
            return;
        }
        if (war.getProgressPercent() <= 0f) {
            warService.concludeWar(war, defender.getId(), LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY);
            return;
        }

        setupNextStage(world, war, aggressor, defender, victor,
                hexOwner != null ? hexOwner : stageDefender);
        enterRedeploying(world, war, aggressor, defender);
    }

    private static void enterRedeploying(World world, War war, Dynasty aggressor, Dynasty defender) {
        war.setStagePhase(WarStagePhase.REDEPLOYING);
        war.setRedeployHoursRemaining(GameConstants.WAR_REDEPLOY_HOURS);
        war.setDeployedActiveAttacker(0);
        war.setDeployedActiveDefender(0);
        war.setDeployedReserveDefender(0);
        ColonyMilitaryService.refreshDynastyMilitaryPower(aggressor);
        ColonyMilitaryService.refreshDynastyMilitaryPower(defender);
        war.setStageStartActiveAggressor(ColonyMilitaryService.powerForWarStanding(aggressor));
        war.setStageStartActiveDefender(ColonyMilitaryService.powerForWarStanding(defender));
        considerAiFallback(world, world.getWarService(), war, aggressor, defender);
        notifyPlayerWarRedeploy(world, war, aggressor, defender);
    }

    private static void setupNextStage(World world, War war, Dynasty aggressor, Dynasty defender,
            Dynasty victor, Dynasty loser) {
        Colony next;
        if (victor.getId() == aggressor.getId()) {
            next = findNextTargetTowardCapital(world, aggressor, defender, defender.getCapital());
            war.setStageAttackerDynastyId(aggressor.getId());
        } else {
            next = findNextTargetTowardCapital(world, defender, aggressor, aggressor.getCapital());
            war.setStageAttackerDynastyId(defender.getId());
        }
        if (next == null) {
            next = victor.getCapital();
        }
        war.setContestedColonyId(next.getId());
        war.setStagePhase(WarStagePhase.ACTIVE_CLASH);
        war.setDeployedReserveDefender(0);
    }

    private static void beginActiveClash(World world, War war, Dynasty aggressor, Dynasty defender) {
        Dynasty stageAttacker = world.findDynastyById(war.getStageAttackerDynastyId());
        Colony contested = findColonyById(world, war.getContestedColonyId());
        if (stageAttacker == null || contested == null) {
            return;
        }
        Dynasty stageDefender = contested.getDynasty();
        if (stageDefender == null) {
            return;
        }

        ColonyMilitaryService.refreshDynastyMilitaryPower(stageAttacker);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageDefender);

        if (DynastyDiplomacyService.countAssignedActiveMilitaryRoles(stageDefender) <= 0) {
            beginDirectReserveAssault(world, war, stageAttacker, stageDefender, contested);
            return;
        }

        war.setStagePhase(WarStagePhase.ACTIVE_CLASH);
        war.setRedeployHoursRemaining(0);
        war.setDeployedActiveAttacker(ColonyMilitaryService.powerForWarStanding(stageAttacker));
        war.setDeployedActiveDefender(ColonyMilitaryService.powerForWarStanding(stageDefender));
        war.setDeployedReserveDefender(0);
        war.setStageStartActiveAggressor(ColonyMilitaryService.powerForWarStanding(aggressor));
        war.setStageStartActiveDefender(ColonyMilitaryService.powerForWarStanding(defender));
    }

    private static void beginDirectReserveAssault(World world, War war, Dynasty stageAttacker,
            Dynasty stageDefender, Colony contested) {
        if (contested == null) {
            return;
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(contested);
        war.setStagePhase(WarStagePhase.RESERVE_ASSAULT);
        war.setRedeployHoursRemaining(0);
        war.setStageAttackerDynastyId(stageAttacker.getId());
        war.setContestedColonyId(contested.getId());
        war.setDeployedActiveAttacker(Math.max(0, ColonyMilitaryService.powerForWarStanding(stageAttacker)));
        war.setDeployedActiveDefender(0);
        war.setDeployedReserveDefender(ColonyMilitaryService.computeReserveMilitaryPower(contested));
        war.setStageStartActiveAggressor(ColonyMilitaryService.powerForWarStanding(
                world.findDynastyById(war.getAggressorDynastyId())));
        war.setStageStartActiveDefender(ColonyMilitaryService.powerForWarStanding(
                world.findDynastyById(war.getDefenderDynastyId())));
    }

    private static void reassignContestedColony(World world, War war, Dynasty aggressor, Dynasty defender) {
        Colony contested = findInitialContestedColony(world, aggressor, defender, aggressor.getCapital());
        if (contested == null) {
            contested = defender.getCapital();
        }
        if (contested != null) {
            war.setContestedColonyId(contested.getId());
            war.setStageAttackerDynastyId(aggressor.getId());
            beginActiveClash(world, war, aggressor, defender);
        }
    }

    private static Colony findInitialContestedColony(World world, Dynasty aggressor, Dynasty defender,
            Colony aggressorCapital) {
        Colony best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Colony defColony : defender.getColonies()) {
            Hex defHex = world.getHexOfColony(defColony);
            if (defHex == null || !isAdjacentToDynasty(defHex, aggressor)) {
                continue;
            }
            int score = aggressorCapital != null
                    ? world.colonyHexDistance(aggressorCapital, defColony)
                    : defColony.getId();
            if (score < bestScore || (score == bestScore && (best == null || defColony.getId() < best.getId()))) {
                bestScore = score;
                best = defColony;
            }
        }
        return best;
    }

    private static Colony findNextTargetTowardCapital(World world, Dynasty attacker, Dynasty owner, Colony capital) {
        Colony best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Colony colony : owner.getColonies()) {
            Hex hex = world.getHexOfColony(colony);
            if (hex == null || !isAdjacentToDynasty(hex, attacker)) {
                continue;
            }
            int dist = capital != null ? world.colonyHexDistance(colony, capital) : colony.getId();
            if (dist < bestDist || (dist == bestDist && (best == null || colony.getId() < best.getId()))) {
                bestDist = dist;
                best = colony;
            }
        }
        return best;
    }

    static void captureColony(World world, WarService warService, War war,
            Colony colony, Dynasty victor, Dynasty loser, boolean promoteLoserCapital) {
        if (colony == null || victor == null || loser == null || colony.getDynasty() == victor) {
            return;
        }
        boolean wasLoserCapital = colony.isCapital();
        loser.removeColony(colony);
        colony.setDynasty(victor);
        colony.setCapital(false);
        if (war != null) {
            war.recordCapture(victor.getId(), colony.getId());
        }
        if (wasLoserCapital && promoteLoserCapital) {
            loser.promoteNewCapital();
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(victor);
        ColonyMilitaryService.refreshDynastyMilitaryPower(loser);
        ColonyStarterService.shared().stabilizeConqueredColony(victor, colony);
    }

    private static Colony findBorderColonyFacing(World world, Dynasty owner, Dynasty facing) {
        Colony best = null;
        int bestDist = Integer.MAX_VALUE;
        Colony facingCapital = facing.getCapital();
        for (Colony colony : owner.getColonies()) {
            Hex hex = world.getHexOfColony(colony);
            if (hex == null || !isAdjacentToDynasty(hex, facing)) {
                continue;
            }
            int dist = facingCapital != null ? world.colonyHexDistance(colony, facingCapital) : colony.getId();
            if (dist < bestDist || (dist == bestDist && (best == null || colony.getId() < best.getId()))) {
                bestDist = dist;
                best = colony;
            }
        }
        return best;
    }

    private static boolean colonyHasQueenDefense(Colony colony) {
        if (colony == null) {
            return false;
        }
        return !colony.getQueens().isEmpty()
                && ColonyMilitaryService.computeReserveMilitaryPower(colony) > 0;
    }

    private static boolean isAdjacentToDynasty(Hex hex, Dynasty dynasty) {
        if (hex == null || dynasty == null) {
            return false;
        }
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor != null && neighbor.getColony() != null
                    && neighbor.getColony().getDynasty() == dynasty) {
                return true;
            }
        }
        return false;
    }

    private static Colony findColonyById(World world, int colonyId) {
        if (world == null || colonyId <= 0 || world.getHexes() == null) {
            return null;
        }
        for (Hex hex : world.getHexes()) {
            if (hex.getColony() != null && hex.getColony().getId() == colonyId) {
                return hex.getColony();
            }
        }
        return null;
    }

    private static BattleTickResult resolveBattleTick(int sideOnePower, int sideTwoPower) {
        if (sideOnePower <= 0 && sideTwoPower <= 0) {
            return BattleTickResult.none();
        }
        if (sideOnePower <= 0) {
            return new BattleTickResult(0, sideTwoPower);
        }
        if (sideTwoPower <= 0) {
            return new BattleTickResult(sideOnePower, 0);
        }

        int stronger = Math.max(sideOnePower, sideTwoPower);
        int weaker = Math.min(sideOnePower, sideTwoPower);
        float ratio = stronger / (float) weaker;
        float winChance = GameConstants.warBattleWinChance(ratio);
        boolean sideOneIsStronger = sideOnePower >= sideTwoPower;
        boolean strongerWins = GameRandom.nextDouble() < winChance;
        boolean sideOneWins = sideOneIsStronger == strongerWins;

        float lossFraction = GameConstants.warBattleLoserLossFraction(ratio);
        float winnerLossFraction = GameConstants.warBattleWinnerLossFraction(ratio);
        if (sideOneWins) {
            return new BattleTickResult(
                    computeBattleLoss(sideOnePower, winnerLossFraction),
                    computeBattleLoss(sideTwoPower, lossFraction));
        }
        return new BattleTickResult(
                computeBattleLoss(sideOnePower, lossFraction),
                computeBattleLoss(sideTwoPower, winnerLossFraction));
    }

    private static int computeBattleLoss(int power, float fraction) {
        if (power <= 0) {
            return 0;
        }
        int loss = Math.round(power * fraction);
        if (loss < 1 && power >= 30) {
            loss = 1;
        }
        return Math.min(power, Math.max(0, loss));
    }

    private static void applyActivePoolLoss(Dynasty dynasty, int powerLoss) {
        if (dynasty == null || powerLoss <= 0) {
            return;
        }
        int totalActive = ColonyMilitaryService.powerForWarStanding(dynasty);
        if (totalActive <= 0) {
            return;
        }
        for (Colony colony : dynasty.getColonies()) {
            int colonyActive = colony.getActiveMilitaryPower();
            if (colonyActive <= 0) {
                continue;
            }
            int colonyLoss = Math.min(colonyActive,
                    (int) Math.round(powerLoss * (colonyActive / (double) totalActive)));
            reduceColonyActiveMilitary(colony, colonyLoss);
        }
    }

    private static void applyReservePoolLoss(Colony colony, int powerLoss) {
        if (colony == null || powerLoss <= 0) {
            return;
        }
        int reserve = ColonyMilitaryService.computeReserveMilitaryPower(colony);
        if (reserve <= 0) {
            eliminateColonyQueens(colony);
            return;
        }
        int remaining = Math.max(0, reserve - powerLoss);
        if (remaining <= 0) {
            eliminateColonyQueens(colony);
        }
        reduceColonyReserveMilitary(colony, powerLoss);
    }

    private static void reduceColonyActiveMilitary(Colony colony, int powerLoss) {
        if (colony == null || powerLoss <= 0) {
            return;
        }
        Map<AntRole, Integer> counts = colony.getWarAssignedRoleCounts();
        float mult = Math.max(0.01f, ColonyMilitaryService.computeStatMultiplier(colony));
        int pointBudget = Math.round(powerLoss / mult);
        int totalPoints = 0;
        for (AntRole role : GameConstants.getActiveMilitaryRoles()) {
            totalPoints += counts.getOrDefault(role, 0) * GameConstants.getActiveMilitaryRoleWeight(role);
        }
        if (totalPoints <= 0) {
            return;
        }
        int remaining = pointBudget;
        AntRole[] roles = GameConstants.getActiveMilitaryRoles();
        for (int i = 0; i < roles.length && remaining > 0; i++) {
            AntRole role = roles[i];
            int count = counts.getOrDefault(role, 0);
            if (count <= 0) {
                continue;
            }
            int roleWeight = GameConstants.getActiveMilitaryRoleWeight(role);
            int rolePoints = count * roleWeight;
            int slice = i == roles.length - 1
                    ? remaining
                    : Math.min(remaining, Math.round(pointBudget * (rolePoints / (float) totalPoints)));
            int remove = Math.min(count, (slice + roleWeight - 1) / roleWeight);
            counts.put(role, count - remove);
            remaining -= remove * roleWeight;
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
    }

    private static void reduceColonyReserveMilitary(Colony colony, int powerLoss) {
        if (colony == null || powerLoss <= 0) {
            return;
        }
        int reserve = ColonyMilitaryService.computeReserveMilitaryPower(colony);
        if (reserve <= powerLoss) {
            eliminateColonyQueens(colony);
        }
        float mult = Math.max(0.01f, ColonyMilitaryService.computeStatMultiplier(colony));
        int pointBudget = Math.round(powerLoss / mult);
        int remaining = pointBudget;

        remaining -= reduceAntList(colony.getSoldiers(), remaining,
                GameConstants.MILITARY_WEIGHT_SOLDIER);
        remaining -= reduceAntList(colony.getMajors(), remaining,
                GameConstants.MILITARY_WEIGHT_MAJOR);
        remaining -= reduceAntList(colony.getWorkers(), remaining,
                GameConstants.MILITARY_WEIGHT_WORKER);
        if (remaining > 0) {
            eliminateColonyQueens(colony);
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
    }

    private static int reduceAntList(java.util.List<com.grimidk.formicempire.classes.entities.Ant> ants,
            int pointBudget, int weightPerAnt) {
        if (ants == null || ants.isEmpty() || pointBudget <= 0 || weightPerAnt <= 0) {
            return 0;
        }
        int remove = Math.min(ants.size(), (pointBudget + weightPerAnt - 1) / weightPerAnt);
        for (int i = 0; i < remove && !ants.isEmpty(); i++) {
            ants.remove(ants.size() - 1);
        }
        return remove * weightPerAnt;
    }

    private static void eliminateColonyQueens(Colony colony) {
        if (colony == null || colony.getQueens() == null) {
            return;
        }
        colony.getQueens().clear();
    }

    private record BattleTickResult(int attackerLoss, int defenderLoss) {
        static BattleTickResult none() {
            return new BattleTickResult(0, 0);
        }
    }

    private static void notifyPlayerWarStageComplete(World world, War war, Dynasty victor,
            Colony contested, boolean attackerRetreat, boolean captured) {
        if (world == null || war == null || victor == null || contested == null) {
            return;
        }
        String progress = formatWarProgressPercent(war.getProgressPercent());
        String message;
        if (attackerRetreat) {
            message = LanguageStrings.format(
                    LanguageStrings.WAR_STAGE_FORFEITED_FMT,
                    victor.getName(),
                    contested.getName(),
                    progress);
        } else if (captured) {
            message = LanguageStrings.format(
                    LanguageStrings.WAR_STAGE_CAPTURED_FMT,
                    victor.getName(),
                    contested.getName(),
                    progress);
        } else {
            message = LanguageStrings.format(
                    LanguageStrings.WAR_STAGE_DEFENDER_HELD_FMT,
                    victor.getName(),
                    contested.getName(),
                    progress);
        }
        notifyPlayerWarEvent(world, war, message);
    }

    private static void notifyPlayerWarRedeploy(World world, War war, Dynasty aggressor, Dynasty defender) {
        if (world == null || war == null || !war.isActive()) {
            return;
        }
        Colony next = findColonyById(world, war.getContestedColonyId());
        if (next == null) {
            return;
        }
        String hours = String.valueOf(GameConstants.WAR_REDEPLOY_HOURS);
        String message = LanguageStrings.format(
                LanguageStrings.WAR_STAGE_REDEPLOY_FMT,
                hours,
                next.getName());
        notifyPlayerWarEvent(world, war, message);
    }

    private static void notifyPlayerWarStageClash(World world, War war, Colony contested) {
        if (world == null || war == null || contested == null) {
            return;
        }
        String message = LanguageStrings.format(
                LanguageStrings.WAR_STAGE_CLASH_FMT,
                contested.getName());
        notifyPlayerWarEvent(world, war, message);
    }

    private static void notifyPlayerWarEvent(World world, War war, String message) {
        if (world == null || war == null || message == null) {
            return;
        }
        Dynasty player = findPlayerDynasty(world);
        if (player == null || !war.involves(player.getId())) {
            return;
        }
        Colony alertColony = player.getCapital();
        if (alertColony == null && !player.getColonies().isEmpty()) {
            alertColony = player.getColonies().get(0);
        }
        if (alertColony != null) {
            alertColony.logEvent(ColonyLogPrefixes.WAR + " " + message);
        }
    }

    private static Dynasty findPlayerDynasty(World world) {
        if (world == null) {
            return null;
        }
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                return dynasty;
            }
        }
        return null;
    }

    private static String formatWarProgressPercent(float progressPercent) {
        return String.valueOf(Math.round(progressPercent));
    }
}
