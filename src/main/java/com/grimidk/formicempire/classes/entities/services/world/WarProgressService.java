package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.entities.services.shared.WarCombatSkillService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.List;
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
            if (war.getStagePhase() == GameConstants.WAR_STAGE_REDEPLOYING) {
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
        war.setStagePhase(GameConstants.WAR_STAGE_ACTIVE_CLASH);
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
        if (war.getStagePhase() == GameConstants.WAR_STAGE_REDEPLOYING) {
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
            completeStage(world, warService, war, aggressor, defender, stageAttacker, stageDefender, contested,
                    StageOutcome.CAPTURE, true);
            return true;
        }
        if (forfeitier.getId() == stageAttacker.getId()) {
            completeStage(world, warService, war, aggressor, defender, stageAttacker, stageDefender, contested,
                    StageOutcome.ATTACKER_RETREAT, true);
            return true;
        }
        return false;
    }

    /**
     * Contested-colony owner withdraws from border clash into hex defense without army losses.
     * Hex assault attacker/defender are already tracked via {@link War#getStageAttackerDynastyId()}
     * and the contested colony owner; border clash has no attacker/defender distinction beyond pools.
     */
    public static boolean canWithdrawToHexDefense(World world, War war, Dynasty withdrawer) {
        if (world == null || war == null || withdrawer == null || !war.isActive() || !war.isCampaignInitialized()) {
            return false;
        }
        if (war.getStagePhase() != GameConstants.WAR_STAGE_ACTIVE_CLASH) {
            return false;
        }
        if (!war.involves(withdrawer.getId())) {
            return false;
        }
        Colony contested = findColonyById(world, war.getContestedColonyId());
        if (contested == null || contested.getDynasty() == null) {
            return false;
        }
        // Only the hex owner can bait: pull border forces home and fight with hex-defense boosts.
        return contested.getDynasty().getId() == withdrawer.getId();
    }

    public static boolean withdrawToHexDefense(World world, War war, Dynasty withdrawer) {
        if (!canWithdrawToHexDefense(world, war, withdrawer)) {
            return false;
        }
        Colony contested = findColonyById(world, war.getContestedColonyId());
        Dynasty stageAttacker = world.findDynastyById(war.getStageAttackerDynastyId());
        Dynasty stageDefender = contested != null ? contested.getDynasty() : null;
        if (contested == null || stageAttacker == null || stageDefender == null) {
            return false;
        }

        // Preserve armies: contested owner clears their border pool and opens hex defense
        // on the same contested hex (original stage attacker assaults into the bait).
        war.setDeployedActiveDefender(0);
        WarCreatureCombatService.clear(war);
        beginDirectReserveAssault(world, war, stageAttacker, stageDefender, contested);
        notifyPlayerWarHexBait(world, war, withdrawer, contested);
        return war.getStagePhase() == GameConstants.WAR_STAGE_RESERVE_ASSAULT;
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

        if (war.getStagePhase() == GameConstants.WAR_STAGE_ACTIVE_CLASH) {
            considerAiHexBait(world, war, aggressor, defender, stageAttacker, stageDefender, contested);
            if (war.getStagePhase() != GameConstants.WAR_STAGE_ACTIVE_CLASH) {
                return;
            }
            resolveActiveClashHour(world, war, stageAttacker, stageDefender);
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
        if (GameRandom.nextDouble() < GameNumbers.AI_WAR_FALLBACK_CHANCE) {
            forfeitStage(world, warService, war, ai);
        }
    }

    static boolean canAiFallback(World world, War war, Dynasty ai, Dynasty opponent) {
        if (world == null || war == null || ai == null || opponent == null || !war.isActive()) {
            return false;
        }
        if (war.getStagePhase() != GameConstants.WAR_STAGE_REDEPLOYING) {
            return false;
        }
        int aiActive = ColonyMilitaryService.powerForWarStanding(ai);
        int opponentActive = ColonyMilitaryService.powerForWarStanding(opponent);
        if (aiActive > opponentActive * GameNumbers.WAR_AI_FALLBACK_MAX_POWER_RATIO) {
            return false;
        }
        Colony contested = findColonyById(world, war.getContestedColonyId());
        if (contested == null || contested.isCapital()) {
            return false;
        }
        if (ai.getColonies().size() < GameNumbers.WAR_AI_FALLBACK_MIN_SPARE_COLONIES) {
            return false;
        }
        if (aiActive < GameNumbers.WAR_AI_FALLBACK_MIN_ACTIVE) {
            return false;
        }
        int stageStartActive = war.getStageStartActiveFor(ai.getId());
        if (stageStartActive > 0
                && aiActive < stageStartActive * GameNumbers.WAR_AI_FALLBACK_RECOVERY_RATIO) {
            return false;
        }
        Dynasty stageDefender = contested.getDynasty();
        Dynasty stageAttacker = world.findDynastyById(war.getStageAttackerDynastyId());
        if (stageDefender == null || stageAttacker == null) {
            return false;
        }
        return ai.getId() == stageDefender.getId() || ai.getId() == stageAttacker.getId();
    }

    private static void considerAiHexBait(World world, War war, Dynasty aggressor, Dynasty defender,
            Dynasty stageAttacker, Dynasty stageDefender, Colony contested) {
        tryAiHexBait(world, war, aggressor, stageAttacker, contested);
        if (war.getStagePhase() != GameConstants.WAR_STAGE_ACTIVE_CLASH) {
            return;
        }
        tryAiHexBait(world, war, defender, stageAttacker, contested);
    }

    private static void tryAiHexBait(World world, War war, Dynasty ai, Dynasty stageAttacker, Colony contested) {
        if (ai == null || ai.isPlayer() || ai.isDefeated()) {
            return;
        }
        if (!canAiHexBait(world, war, ai, stageAttacker, contested)) {
            return;
        }
        if (GameRandom.nextDouble() < GameNumbers.AI_WAR_HEX_BAIT_CHANCE) {
            withdrawToHexDefense(world, war, ai);
        }
    }

    static boolean canAiHexBait(World world, War war, Dynasty ai, Dynasty stageAttacker, Colony contested) {
        if (!canWithdrawToHexDefense(world, war, ai)) {
            return false;
        }
        if (stageAttacker == null || contested == null || contested.getDynasty() != ai) {
            return false;
        }
        int borderAi = Math.max(0, war.getDeployedActiveDefender());
        int borderEnemy = Math.max(0, war.getDeployedActiveAttacker());
        if (borderAi <= 0 && borderEnemy <= 0) {
            return false;
        }
        float borderOdds = borderAi / (float) Math.max(1, borderEnemy);

        ColonyMilitaryService.refreshColonyMilitaryPower(contested);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageAttacker);
        ColonyMilitaryService.refreshDynastyMilitaryPower(ai);
        int hexDefense = ColonyMilitaryService.computeHexDefenseEffectivePower(contested);
        int hexAssault = ColonyMilitaryService.computeHexAssaultEffectiveAttackerPower(stageAttacker);
        if (hexDefense <= 0 || hexAssault <= 0) {
            return false;
        }
        float hexOdds = hexDefense / (float) hexAssault;
        if (hexOdds < GameNumbers.WAR_AI_HEX_BAIT_MIN_HEX_ODDS) {
            return false;
        }
        if (hexOdds < borderOdds * GameNumbers.WAR_AI_HEX_BAIT_ODDS_IMPROVEMENT) {
            return false;
        }
        // Bait only when a follow-up counterattack into the assaulting dynasty looks viable.
        return counterattackOddsLookFavorable(world, ai, stageAttacker);
    }

    /**
     * After holding the contested hex, {@code ai} becomes stage attacker and pushes into
     * {@code enemy}'s adjacent territory. Require a reachable target and decent assault odds.
     */
    static boolean counterattackOddsLookFavorable(World world, Dynasty ai, Dynasty enemy) {
        if (world == null || ai == null || enemy == null || ai == enemy) {
            return false;
        }
        Colony counterTarget = findBorderColonyFacing(world, enemy, ai);
        if (counterTarget == null) {
            counterTarget = enemy.getCapital();
        }
        if (counterTarget == null) {
            return false;
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(counterTarget);
        ColonyMilitaryService.refreshDynastyMilitaryPower(ai);
        int counterDefense = ColonyMilitaryService.computeHexDefenseEffectivePower(counterTarget);
        int counterAssault = ColonyMilitaryService.computeHexAssaultEffectiveAttackerPower(ai);
        if (counterAssault <= 0) {
            return false;
        }
        float counterOdds = counterAssault / (float) Math.max(1, counterDefense);
        return counterOdds >= GameNumbers.WAR_AI_HEX_BAIT_MIN_COUNTER_ODDS;
    }

    private static void resolveActiveClashHour(World world, War war, Dynasty stageAttacker, Dynasty stageDefender) {
        if (!WarCreatureCombatService.hasLivingCombatants(war)) {
            WarCreatureCombatService.clear(war);
            WarCreatureCombatService.startBorderBattle(war, stageAttacker, stageDefender);
        }
        WarCreatureCombatService.TickOutcome outcome = WarCreatureCombatService.tick(war);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageAttacker);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageDefender);
        if (outcome == WarCreatureCombatService.TickOutcome.ATTACKER_WINS) {
            // Border winner assaults the loser's hex.
            war.setDeployedActiveDefender(0);
            beginReserveAssault(world, war, stageAttacker, stageDefender);
        } else if (outcome == WarCreatureCombatService.TickOutcome.DEFENDER_WINS) {
            war.setDeployedActiveAttacker(0);
            beginReserveAssault(world, war, stageAttacker, stageDefender);
        }
    }

    private static void beginReserveAssault(World world, War war, Dynasty stageAttacker, Dynasty stageDefender) {
        Dynasty activeWinner;
        Dynasty activeLoser;
        if (war.getDeployedActiveAttacker() > 0 && war.getDeployedActiveDefender() <= 0) {
            activeWinner = stageAttacker;
            activeLoser = stageDefender;
        } else if (war.getDeployedActiveDefender() > 0 && war.getDeployedActiveAttacker() <= 0) {
            activeWinner = stageDefender;
            activeLoser = stageAttacker;
        } else {
            activeWinner = war.getDeployedActiveAttacker() >= war.getDeployedActiveDefender()
                    ? stageAttacker : stageDefender;
            activeLoser = activeWinner == stageAttacker ? stageDefender : stageAttacker;
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
        war.setStagePhase(GameConstants.WAR_STAGE_RESERVE_ASSAULT);
        WarCreatureCombatService.clear(war);
        WarCreatureCombatService.startHexBattle(war, activeWinner, reserveTarget);
    }

    private static void resolveReserveAssaultHour(World world, WarService warService, War war,
            Dynasty aggressor, Dynasty defender, Dynasty stageAttacker, Dynasty stageDefender, Colony contested) {
        if (!WarCreatureCombatService.hasLivingCombatants(war)) {
            WarCreatureCombatService.clear(war);
            WarCreatureCombatService.startHexBattle(war, stageAttacker, contested);
        }
        WarCreatureCombatService.TickOutcome outcome = WarCreatureCombatService.tick(war);
        ColonyMilitaryService.refreshColonyMilitaryPower(contested);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageAttacker);
        ColonyMilitaryService.refreshDynastyMilitaryPower(stageDefender);

        if (outcome == WarCreatureCombatService.TickOutcome.ATTACKER_WINS) {
            WarCreatureCombatService.clear(war);
            completeStage(world, warService, war, aggressor, defender, stageAttacker,
                    contested.getDynasty(), contested, StageOutcome.CAPTURE, true);
        } else if (outcome == WarCreatureCombatService.TickOutcome.DEFENDER_WINS) {
            // Holding the hex awards the stage to the defender and flips stageAttacker
            // so the next redeploy is a counterattack into the failed assaulter's territory.
            WarCreatureCombatService.clear(war);
            completeStage(world, warService, war, aggressor, defender, stageAttacker,
                    contested.getDynasty(), contested, StageOutcome.DEFENDER_HOLD, true);
        }
    }

    private static boolean isStageReadyToResolve(War war) {
        return war != null && war.getStageProgress() >= 1f - 0.0001f;
    }

    /** How a stage ends — drives victor, capture, and player-facing copy. */
    enum StageOutcome {
        CAPTURE,
        ATTACKER_RETREAT,
        DEFENDER_HOLD
    }

    private static void completeStage(World world, WarService warService, War war,
            Dynasty aggressor, Dynasty defender, Dynasty stageAttacker, Dynasty stageDefender,
            Colony contested, StageOutcome outcome, boolean forceImmediate) {
        if (!forceImmediate && !isStageReadyToResolve(war)) {
            return;
        }
        if (outcome == null) {
            outcome = StageOutcome.CAPTURE;
        }
        Dynasty victor = outcome == StageOutcome.CAPTURE ? stageAttacker : stageDefender;
        Dynasty hexOwner = contested.getDynasty();
        WarCreatureCombatService.clear(war);
        boolean captured = outcome == StageOutcome.CAPTURE
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

        notifyPlayerWarStageComplete(world, war, victor, contested, outcome);

        String battleKey = captured
                ? LanguageStrings.HISTORY_BATTLE_CAPTURE_FMT
                : LanguageStrings.HISTORY_BATTLE_FMT;
        world.getHistoryService().record(WorldHistoryEventType.BATTLE, battleKey,
                victor.getId(), contested.getId(), war.getId(),
                WorldHistoryEvent.dynastyArg(victor.getId()),
                WorldHistoryEvent.colonyArg(contested.getId()),
                WorldHistoryEvent.plainArg(war.getId()));

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

        // Victor becomes stageAttacker and advances toward the loser's capital (counterattack).
        Dynasty loser = victor.getId() == aggressor.getId() ? defender : aggressor;
        setupNextStage(world, war, aggressor, defender, victor, loser);
        enterRedeploying(world, war, aggressor, defender);
    }

    /**
     * Resolves the current hex assault as a successful hold by the contested owner.
     * Awards the stage, flips {@code stageAttacker} for counterattack, and enters redeploy.
     */
    static boolean resolveHexDefenseHold(World world, WarService warService, War war) {
        if (world == null || war == null || !war.isActive() || !war.isCampaignInitialized()) {
            return false;
        }
        if (war.getStagePhase() != GameConstants.WAR_STAGE_RESERVE_ASSAULT) {
            return false;
        }
        Dynasty aggressor = world.findDynastyById(war.getAggressorDynastyId());
        Dynasty defender = world.findDynastyById(war.getDefenderDynastyId());
        Dynasty stageAttacker = world.findDynastyById(war.getStageAttackerDynastyId());
        Colony contested = findColonyById(world, war.getContestedColonyId());
        if (aggressor == null || defender == null || stageAttacker == null || contested == null
                || contested.getDynasty() == null) {
            return false;
        }
        if (warService == null) {
            warService = world.getWarService();
        }
        completeStage(world, warService, war, aggressor, defender, stageAttacker,
                contested.getDynasty(), contested, StageOutcome.DEFENDER_HOLD, true);
        return war.getStagePhase() == GameConstants.WAR_STAGE_REDEPLOYING
                || !war.isActive();
    }

    private static void enterRedeploying(World world, War war, Dynasty aggressor, Dynasty defender) {
        WarCreatureCombatService.clear(war);
        war.setStagePhase(GameConstants.WAR_STAGE_REDEPLOYING);
        war.setRedeployHoursRemaining(GameNumbers.WAR_REDEPLOY_HOURS);
        war.setDeployedActiveAttacker(0);
        war.setDeployedActiveDefender(0);
        war.setDeployedReserveDefender(0);
        WarCombatSkillService.applyRedeployRegen(aggressor);
        WarCombatSkillService.applyRedeployRegen(defender);
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
        war.setStagePhase(GameConstants.WAR_STAGE_ACTIVE_CLASH);
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

        if (countAssignedBorderBattleRoles(stageDefender) <= 0) {
            beginDirectReserveAssault(world, war, stageAttacker, stageDefender, contested);
            return;
        }

        war.setStagePhase(GameConstants.WAR_STAGE_ACTIVE_CLASH);
        war.setRedeployHoursRemaining(0);
        war.setStageStartActiveAggressor(ColonyMilitaryService.powerForWarStanding(aggressor));
        war.setStageStartActiveDefender(ColonyMilitaryService.powerForWarStanding(defender));
        WarCreatureCombatService.clear(war);
        WarCreatureCombatService.startBorderBattle(war, stageAttacker, stageDefender);
    }

    private static void beginDirectReserveAssault(World world, War war, Dynasty stageAttacker,
            Dynasty stageDefender, Colony contested) {
        if (contested == null) {
            return;
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(contested);
        war.setStagePhase(GameConstants.WAR_STAGE_RESERVE_ASSAULT);
        war.setRedeployHoursRemaining(0);
        war.setStageAttackerDynastyId(stageAttacker.getId());
        war.setContestedColonyId(contested.getId());
        war.setStageStartActiveAggressor(ColonyMilitaryService.powerForWarStanding(
                world.findDynastyById(war.getAggressorDynastyId())));
        war.setStageStartActiveDefender(ColonyMilitaryService.powerForWarStanding(
                world.findDynastyById(war.getDefenderDynastyId())));
        WarCreatureCombatService.clear(war);
        WarCreatureCombatService.startHexBattle(war, stageAttacker, contested);
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
                && ColonyMilitaryService.computeHexDefenseMilitaryPower(colony) > 0;
    }

    private static int countAssignedBorderBattleRoles(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        int assigned = 0;
        for (Colony colony : dynasty.getColonies()) {
            Map<AntRole, Integer> counts = colony.getWarAssignedRoleCounts();
            for (AntRole role : GameConstants.getBorderBattleRoles()) {
                assigned += counts.getOrDefault(role, 0);
            }
        }
        return assigned;
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
        float winChance = GameNumbers.warBattleWinChance(ratio);
        boolean sideOneIsStronger = sideOnePower >= sideTwoPower;
        boolean strongerWins = GameRandom.nextDouble() < winChance;
        boolean sideOneWins = sideOneIsStronger == strongerWins;

        float lossFraction = GameNumbers.warBattleLoserLossFraction(ratio);
        float winnerLossFraction = GameNumbers.warBattleWinnerLossFraction(ratio);
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

    /** Hex-assault attacker losses hit border roles and Siege proportionally by base power. */
    private static void applyHexAssaultAttackerLoss(Dynasty dynasty, int powerLoss) {
        if (dynasty == null || powerLoss <= 0) {
            return;
        }
        int siegePower = ColonyMilitaryService.computeSiegeAssaultPower(dynasty);
        int borderPower = ColonyMilitaryService.powerForWarStanding(dynasty);
        int total = siegePower + borderPower;
        if (total <= 0) {
            return;
        }
        int siegeLoss = Math.min(siegePower,
                (int) Math.round(powerLoss * (siegePower / (double) total)));
        int borderLoss = Math.max(0, powerLoss - siegeLoss);
        if (borderLoss > 0) {
            applyActivePoolLoss(dynasty, borderLoss);
        }
        if (siegeLoss > 0) {
            reduceSiegeAssaultPower(dynasty, siegeLoss);
        }
    }

    private static void reduceSiegeAssaultPower(Dynasty dynasty, int powerLoss) {
        if (dynasty == null || powerLoss <= 0) {
            return;
        }
        int remaining = powerLoss;
        for (Colony colony : dynasty.getColonies()) {
            if (remaining <= 0) {
                break;
            }
            int siegePower = ColonyMilitaryService.computeAssignedRolePower(colony, GameConstants.ROLE_SIEGE);
            if (siegePower <= 0) {
                continue;
            }
            int colonyLoss = Math.min(siegePower, remaining);
            float mult = Math.max(0.01f, ColonyMilitaryService.computeStatMultiplier(colony));
            int pointBudget = Math.round(colonyLoss / mult);
            int roleWeight = GameConstants.getActiveMilitaryRoleWeight(GameConstants.ROLE_SIEGE);
            if (roleWeight <= 0) {
                roleWeight = GameConstants.getMilitaryWeightForAntType(GameConstants.TYPE_MAJOR);
            }
            int count = colony.getWarAssignedRoleCount(GameConstants.ROLE_SIEGE);
            int remove = Math.min(count, (pointBudget + roleWeight - 1) / Math.max(1, roleWeight));
            colony.setWarAssignedRoleCount(GameConstants.ROLE_SIEGE, Math.max(0, count - remove));
            remaining -= Math.max(1, remove * roleWeight);
            ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        }
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
    }

    private static void applyReservePoolLoss(Colony colony, int powerLoss) {
        if (colony == null || powerLoss <= 0) {
            return;
        }
        int hexDefense = ColonyMilitaryService.computeHexDefenseMilitaryPower(colony);
        if (hexDefense <= 0) {
            eliminateColonyQueens(colony);
            return;
        }
        int remaining = Math.max(0, hexDefense - powerLoss);
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
        for (AntRole role : GameConstants.getBorderBattleRoles()) {
            totalPoints += counts.getOrDefault(role, 0) * GameConstants.getActiveMilitaryRoleWeight(role);
        }
        if (totalPoints <= 0) {
            return;
        }
        int remaining = pointBudget;
        AntRole[] roles = GameConstants.getBorderBattleRoles();
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
        int hexDefense = ColonyMilitaryService.computeHexDefenseMilitaryPower(colony);
        if (hexDefense <= powerLoss) {
            eliminateColonyQueens(colony);
        }
        float mult = Math.max(0.01f, ColonyMilitaryService.computeStatMultiplier(colony));
        int pointBudget = Math.round(powerLoss / mult);
        int remaining = pointBudget;

        int shieldWeight = GameConstants.getActiveMilitaryRoleWeight(GameConstants.ROLE_DEFENDER);
        if (shieldWeight <= 0) {
            shieldWeight = GameConstants.getMilitaryWeightForAntType(GameConstants.TYPE_SOLDIER);
        }
        int shieldsToSacrifice = Math.min(
                WarCombatSkillService.countShieldingDefenders(colony),
                (remaining + shieldWeight - 1) / Math.max(1, shieldWeight));
        int sacrificed = WarCombatSkillService.sacrificeShieldingDefenders(colony, shieldsToSacrifice);
        remaining -= sacrificed * shieldWeight;

        if (remaining > 0) {
            remaining -= reduceHexDefenseOnlyRoleCounts(colony, remaining);
        }
        if (remaining > 0) {
            remaining -= reduceAntList(colony.getSoldiers(), remaining,
                    GameConstants.TYPE_SOLDIER.getMilitaryWeight());
            remaining -= reduceAntList(colony.getMajors(), remaining,
                    GameConstants.TYPE_MAJOR.getMilitaryWeight());
            remaining -= reduceAntList(colony.getWorkers(), remaining,
                    GameConstants.TYPE_WORKER.getMilitaryWeight());
        }
        if (remaining > 0) {
            eliminateColonyQueens(colony);
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
    }

    private static int reduceHexDefenseOnlyRoleCounts(Colony colony, int pointBudget) {
        if (colony == null || pointBudget <= 0) {
            return 0;
        }
        Map<AntRole, Integer> counts = colony.getWarAssignedRoleCounts();
        int spent = 0;
        for (AntRole role : GameConstants.getHexDefenseOnlyRoles()) {
            if (spent >= pointBudget) {
                break;
            }
            int count = counts.getOrDefault(role, 0);
            if (count <= 0) {
                continue;
            }
            int roleWeight = GameConstants.getActiveMilitaryRoleWeight(role);
            int remove = Math.min(count, (pointBudget - spent + roleWeight - 1) / roleWeight);
            counts.put(role, count - remove);
            spent += remove * roleWeight;
        }
        return spent;
    }

    private static int reduceAntList(List<Ant> ants,
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
        WarCombatSkillService.sacrificeShieldingDefenders(colony, Integer.MAX_VALUE);
        if (WarCombatSkillService.countShieldingDefenders(colony) > 0) {
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
            Colony contested, StageOutcome outcome) {
        if (world == null || war == null || victor == null || contested == null) {
            return;
        }
        String progress = formatWarProgressPercent(war.getProgressPercent());
        String message;
        if (outcome == StageOutcome.ATTACKER_RETREAT) {
            message = LanguageStrings.format(
                    LanguageStrings.WAR_STAGE_FORFEITED_FMT,
                    victor.getName(),
                    contested.getName(),
                    progress);
        } else if (outcome == StageOutcome.DEFENDER_HOLD) {
            message = LanguageStrings.format(
                    LanguageStrings.WAR_STAGE_DEFENDER_HELD_FMT,
                    victor.getName(),
                    contested.getName(),
                    progress);
        } else {
            message = LanguageStrings.format(
                    LanguageStrings.WAR_STAGE_CAPTURED_FMT,
                    victor.getName(),
                    contested.getName(),
                    progress);
        }
        Dynasty player = findPlayerDynasty(world);
        if (player != null && war.involves(player.getId())) {
            boolean playerWon = victor.getId() == player.getId();
            String title = playerWon
                    ? LanguageStrings.get(LanguageStrings.WAR_STAGE_WON_TITLE)
                    : LanguageStrings.get(LanguageStrings.WAR_STAGE_LOST_TITLE);
            player.addPendingWarStageResultAlert(title, message);
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
        String hours = String.valueOf(GameNumbers.WAR_REDEPLOY_HOURS);
        String message = LanguageStrings.format(
                LanguageStrings.WAR_STAGE_REDEPLOY_FMT,
                hours,
                next.getName());
        notifyPlayerWarEvent(world, war, message);
    }

    private static void notifyPlayerWarHexBait(World world, War war, Dynasty withdrawer, Colony contested) {
        if (world == null || war == null || withdrawer == null || contested == null) {
            return;
        }
        String message = LanguageStrings.format(
                LanguageStrings.WAR_STAGE_HEX_BAIT_FMT,
                withdrawer.getName(),
                contested.getName());
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
