package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.grimidk.formicempire.classes.entities.critter.Ant;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.dynasty.AiPersonality;
import com.grimidk.formicempire.classes.constants.dynasty.DiplomaticReputation;
import com.grimidk.formicempire.classes.constants.dynasty.DiplomaticReputationModifier;
import com.grimidk.formicempire.classes.constants.dynasty.PactRequestIncomingPolicy;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.dynasty.TradeMethod;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class DynastyDiplomacyService {

    public static boolean isDiplomaticallyContactable(Dynasty dynasty) {
        return dynasty != null && dynasty.isActiveForDiplomacy();
    }

    public static boolean meetsWarDeclarationPopulationRequirement(Dynasty dynasty) {
        if (dynasty == null || dynasty.isDefeated()) {
            return false;
        }
        return dynasty.getStatService().getTotalPopulation(dynasty)
                >= GameNumbers.WAR_DECLARATION_MIN_POPULATION;
    }

    public static boolean meetsWarActiveMilitaryRequirement(Dynasty dynasty) {
        return countPreparedActiveMilitaryRoles(dynasty) > 0;
    }

    public static int countAssignedActiveMilitaryRoles(Dynasty dynasty) {
        if (dynasty == null || dynasty.isDefeated()) {
            return 0;
        }
        ensureSoldierWarRoleUpgrades(dynasty);
        int assigned = 0;
        for (Colony colony : dynasty.getColonies()) {
            assigned += sumActiveMilitaryRoleCounts(colony.getWarAssignedRoleCounts());
        }
        return assigned;
    }

    public static int countPreparedActiveMilitaryRoles(Dynasty dynasty) {
        if (dynasty == null || dynasty.isDefeated()) {
            return 0;
        }
        ensureSoldierWarRoleUpgrades(dynasty);
        int existing = 0;
        for (Colony colony : dynasty.getColonies()) {
            existing += sumActiveMilitaryRoleCounts(colony.getWarAssignedRoleCounts());
        }
        if (existing > 0) {
            return existing;
        }
        int preview = 0;
        for (Colony colony : dynasty.getColonies()) {
            preview += sumActiveMilitaryRoleCounts(colony.getAutomationService().calculateWarEconomyQuotas(colony));
        }
        return preview;
    }

    private static int sumActiveMilitaryRoleCounts(Map<AntRole, Integer> roleCounts) {
        if (roleCounts == null) {
            return 0;
        }
        int total = 0;
        for (AntRole role : GameConstants.getActiveMilitaryRoles()) {
            total += roleCounts.getOrDefault(role, 0);
        }
        return total;
    }

    private static void ensureSoldierWarRoleUpgrades(Dynasty dynasty) {
        if (dynasty == null || !dynasty.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            return;
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            dynasty.unlockUpgrade(GameUnlocks.ROLE_HUNTER);
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ROLE_WARRIOR)) {
            dynasty.unlockUpgrade(GameUnlocks.ROLE_WARRIOR);
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ROLE_MILITIA)) {
            dynasty.unlockUpgrade(GameUnlocks.ROLE_MILITIA);
        }
    }

    public enum TradeProposalResult {
        FAILED,
        QUEUED,
        ACCEPTED,
        DECLINED
    }

    private final Dynasty dynasty;

    public DynastyDiplomacyService(Dynasty dynasty) {
        this.dynasty = dynasty;
    }

    public boolean hasNonAggressionPact(Dynasty other) {
        if (other == null || other == dynasty) {
            return false;
        }
        return dynasty.hasDiplomaticModifierKey(other.getId(), GameConstants.DIPLO_MODIFIER_PACT.getNameKey());
    }

    public boolean canFormNonAggressionPact(Dynasty other, World world) {
        if (other == null || other == dynasty || world == null
                || !isDiplomaticallyContactable(dynasty) || !isDiplomaticallyContactable(other)) {
            return false;
        }
        if (hasNonAggressionPact(other)) {
            return false;
        }
        return GameConstants.DIPLO_MODIFIER_PACT.meetsReputationRequirement(
                getEffectiveDiplomaticReputation(other, world));
    }

    public boolean canBreakNonAggressionPact(Dynasty other) {
        return hasNonAggressionPact(other);
    }

    public void formNonAggressionPact(Dynasty other, World world) {
        if (!canFormNonAggressionPact(other, world)) {
            return;
        }
        applyModifierBothWays(other, GameConstants.DIPLO_MODIFIER_PACT);
    }

    public void applyNonAggressionPact(Dynasty other) {
        if (other == null || other == dynasty || hasNonAggressionPact(other)) {
            return;
        }
        applyModifierBothWays(other, GameConstants.DIPLO_MODIFIER_PACT);
    }

    public boolean canRequestNonAggressionPact(Dynasty other, World world) {
        if (world != null) {
            dynasty.migrateLegacyTimedDiplomaticModifiers(world);
        }
        if (getPactRequestDeclineCooldownMonthsRemaining(other, world) > 0) {
            return false;
        }
        return canFormNonAggressionPact(other, world);
    }

    public static double computePactAcceptanceChance(int effectiveReputation) {
        int minRep = GameConstants.REPUTATION_CORDIAL.getMinScore();
        int maxRep = GameNumbers.DIPLOMATIC_REPUTATION_MAX;
        if (effectiveReputation < minRep) {
            return 0.0;
        }
        if (effectiveReputation >= maxRep) {
            return 1.0;
        }
        return 0.5 + 0.5 * (effectiveReputation - minRep) / (double) (maxRep - minRep);
    }

    public void requestNonAggressionPact(Dynasty other, World world) {
        if (!canRequestNonAggressionPact(other, world)) {
            return;
        }
        if (other.hasPendingPactRequestFrom(dynasty.getId())) {
            return;
        }

        if (other.isPlayer()) {
            other.getDiplomacyService().handleIncomingPactRequest(dynasty, world);
            return;
        }

        int effectiveRep = other.getDiplomacyService().getEffectiveDiplomaticReputation(dynasty, world);
        if (GameRandom.nextDouble() < computePactAcceptanceChance(effectiveRep)) {
            formNonAggressionPact(other, world);
        } else {
            other.getDiplomacyService().declineNonAggressionPact(dynasty, world);
        }
    }

    public void acceptNonAggressionPact(Dynasty requester, World world) {
        if (requester == null || requester == dynasty) {
            return;
        }
        removePendingPactRequestIfAny(requester);
        requester.getDiplomacyService().formNonAggressionPact(dynasty, world);
    }

    public boolean canAcceptIncomingPactRequest(Dynasty requester, World world) {
        if (requester == null || requester == dynasty || world == null) {
            return false;
        }
        if (dynasty.isPactRequestPromptOpen()) {
            return false;
        }
        if (dynasty.copyPendingPactRequestFromIds().size() >= GameNumbers.DIPLO_PENDING_PACT_REQUEST_QUEUE_MAX) {
            return false;
        }
        int today = DynastyIntegrationService.worldDayIndex(world);
        if (dynasty.getLastIncomingPactRequestWorldDay() == today) {
            return false;
        }
        return !dynasty.hasPendingPactRequestFrom(requester.getId());
    }

    public void handleIncomingPactRequest(Dynasty requester, World world) {
        if (!canAcceptIncomingPactRequest(requester, world)) {
            return;
        }
        dynasty.setLastIncomingPactRequestWorldDay(DynastyIntegrationService.worldDayIndex(world));
        PactRequestIncomingPolicy policy = dynasty.getPactRequestIncomingPolicy();
        if (policy == PactRequestIncomingPolicy.AUTO_ACCEPT) {
            acceptNonAggressionPact(requester, world);
        } else if (policy == PactRequestIncomingPolicy.AUTO_DECLINE) {
            declineNonAggressionPact(requester, world);
        } else {
            dynasty.addPendingPactRequest(requester.getId());
        }
    }

    public void applyIncomingPactPolicyToPendingRequests(World world) {
        if (world == null) {
            return;
        }
        PactRequestIncomingPolicy policy = dynasty.getPactRequestIncomingPolicy();
        if (policy == PactRequestIncomingPolicy.MANUAL) {
            return;
        }
        List<Integer> pending = dynasty.copyPendingPactRequestFromIds();
        for (int fromId : pending) {
            Dynasty requester = world.findDynastyById(fromId);
            if (requester == null || requester.isDefeated()) {
                dynasty.removePendingPactRequest(fromId);
                continue;
            }
            if (policy == PactRequestIncomingPolicy.AUTO_ACCEPT) {
                acceptNonAggressionPact(requester, world);
            } else if (policy == PactRequestIncomingPolicy.AUTO_DECLINE) {
                declineNonAggressionPact(requester, world);
            }
        }
    }

    public void declineNonAggressionPact(Dynasty requester, World world) {
        if (requester == null || requester == dynasty) {
            return;
        }
        dynasty.removePendingPactRequest(requester.getId());
        if (hasNonAggressionPact(requester)) {
            return;
        }
        applyModifierBothWays(requester, GameConstants.DIPLO_MODIFIER_DECLINED_PACT);
    }

    private void removePendingPactRequestIfAny(Dynasty requester) {
        dynasty.removePendingPactRequest(requester.getId());
        requester.removePendingPactRequest(dynasty.getId());
    }

    public void breakNonAggressionPact(Dynasty other) {
        breakNonAggressionPact(other, null);
    }

    public void breakNonAggressionPact(Dynasty other, World world) {
        if (!canBreakNonAggressionPact(other)) {
            return;
        }
        recordPactBroken(other, world);
        applyModifierBothWays(other, GameConstants.DIPLO_MODIFIER_BROKEN_PACT);
    }

    public boolean isAtWarWith(Dynasty other) {
        if (other == null || other == dynasty) {
            return false;
        }
        return dynasty.hasDiplomaticModifierKey(other.getId(), GameConstants.DIPLO_MODIFIER_WAR.getNameKey());
    }

    public void applyWar(Dynasty other) {
        TradeManager tradeManager = dynasty.getTradeService() != null
                ? dynasty.getTradeService().getTradeManager()
                : null;
        applyWar(other, tradeManager, null);
    }

    public void applyWar(Dynasty other, TradeManager tradeManager) {
        applyWar(other, tradeManager, null);
    }

    public void applyWar(Dynasty other, TradeManager tradeManager, World world) {
        if (other == null || other == dynasty || !isDiplomaticallyContactable(dynasty) || !isDiplomaticallyContactable(other)) {
            return;
        }
        int integrationVassalId = 0;
        if (world != null) {
            Dynasty overlord = DynastyIntegrationService.findIntegrationOverlord(world, other.getId());
            if (overlord != null && overlord != dynasty) {
                integrationVassalId = other.getId();
                other = overlord;
                if (overlord.isPlayer()) {
                    overlord.addPendingIntegrationVassalWarAlert(dynasty.getId(), integrationVassalId);
                }
            }
        }
        if (other == dynasty || !isDiplomaticallyContactable(other)) {
            return;
        }
        if (isAtWarWith(other)) {
            return;
        }
        cancelCrossDynastyTradesWith(other, tradeManager);
        applyModifierBothWays(other, GameConstants.DIPLO_MODIFIER_WAR);
        applyWarEconomyToDynasty(dynasty);
        applyWarEconomyToDynasty(other);
        if (other.isPlayer() && integrationVassalId <= 0) {
            other.addPendingWarDeclarationFrom(dynasty.getId());
        }
        if (world != null) {
            world.getWarService().beginWar(dynasty, other);
        }
    }

    public void clearWarWith(Dynasty other, TradeManager tradeManager) {
        if (other == null || !isAtWarWith(other)) {
            return;
        }
        clearExclusiveGroupBothWays(other, GameConstants.DIPLO_EXCLUSIVE_PACT);
        applyWarEconomyToDynasty(dynasty);
        applyWarEconomyToDynasty(other);
    }

    public void applyWasAtWarModifier(Dynasty other, World world) {
        if (other == null || other == dynasty) {
            return;
        }
        applyModifierBothWays(other, GameConstants.DIPLO_MODIFIER_WAS_AT_WAR);
    }

    public int getWasAtWarModifierMonthsRemaining(Dynasty other, World world) {
        if (world != null) {
            dynasty.migrateLegacyTimedDiplomaticModifiers(world);
        }
        return GameNumbers.daysToMonthsCeil(dynasty.getDiplomaticModifierRemainingDays(
                other.getId(), GameConstants.DIPLO_MODIFIER_WAS_AT_WAR.getNameKey()));
    }

    public boolean canDeclareWar(Dynasty other, World world) {
        if (other == null || other == dynasty || !isDiplomaticallyContactable(dynasty) || !isDiplomaticallyContactable(other)) {
            return false;
        }
        if (!meetsWarDeclarationPopulationRequirement(dynasty)) {
            return false;
        }
        if (!meetsWarActiveMilitaryRequirement(dynasty)) {
            return false;
        }
        if (!sharesBorderWith(other, world)) {
            return false;
        }
        if (isAtWarWith(other)) {
            return false;
        }
        if (hasNonAggressionPact(other)) {
            return false;
        }
        return getMonthsSincePactBroken(other, world) >= GameNumbers.WAR_PACT_BREAK_COOLDOWN_MONTHS;
    }

    public int getMonthsSincePactBroken(Dynasty other, World world) {
        if (other == null || world == null) {
            return Integer.MAX_VALUE;
        }
        Integer brokenAt = dynasty.getPactBrokenAtWorldMonth(other.getId());
        if (brokenAt == null) {
            return Integer.MAX_VALUE;
        }
        return worldMonthIndex(world) - brokenAt;
    }

    public int getWarDeclarationCooldownMonthsRemaining(Dynasty other, World world) {
        int since = getMonthsSincePactBroken(other, world);
        if (since >= GameNumbers.WAR_PACT_BREAK_COOLDOWN_MONTHS) {
            return 0;
        }
        return GameNumbers.WAR_PACT_BREAK_COOLDOWN_MONTHS - since;
    }

    public int getPactRequestDeclineCooldownMonthsRemaining(Dynasty other, World world) {
        if (other == null) {
            return 0;
        }
        if (world != null) {
            dynasty.migrateLegacyTimedDiplomaticModifiers(world);
        }
        return GameNumbers.daysToMonthsCeil(dynasty.getDiplomaticModifierRemainingDays(
                other.getId(), GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getNameKey()));
    }

    public int getTradeRequestDeclineCooldownMonthsRemaining(Dynasty other, World world) {
        if (other == null) {
            return 0;
        }
        if (world != null) {
            dynasty.migrateLegacyTimedDiplomaticModifiers(world);
        }
        return GameNumbers.daysToMonthsCeil(dynasty.getDiplomaticModifierRemainingDays(
                other.getId(), GameConstants.DIPLO_MODIFIER_TRADE_REQUEST.getNameKey()));
    }

    public void declareWar(Dynasty other, World world, TradeManager tradeManager) {
        if (!canDeclareWar(other, world)) {
            return;
        }
        applyWar(other, tradeManager, world);
        if (world != null && !dynasty.isPlayer() && !other.isPlayer()) {
            queueNpcWarAlertForPlayer(world, dynasty.getId(), other.getId());
        }
    }

    private static void queueNpcWarAlertForPlayer(World world, int attackerId, int defenderId) {
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                dynasty.addPendingNpcWarAlert(attackerId, defenderId);
                return;
            }
        }
    }

    public static int worldMonthIndex(World world) {
        if (world == null) {
            return 0;
        }
        return world.getYear() * 12 + world.getMonth();
    }

    private void recordPactBroken(Dynasty other, World world) {
        if (other == null || world == null) {
            return;
        }
        int monthIndex = worldMonthIndex(world);
        dynasty.setPactBrokenAtWorldMonth(other.getId(), monthIndex);
        other.setPactBrokenAtWorldMonth(dynasty.getId(), monthIndex);
    }

    private void applyWarEconomyToDynasty(Dynasty target) {
        if (target == null) {
            return;
        }
        ensureSoldierWarRoleUpgrades(target);
        for (Colony colony : target.getColonies()) {
            if (!hasConfiguredWarEconomyAssignments(colony)) {
                colony.copyPeaceRolesToWar();
            }
            if (!target.isPlayer() || colony.isAutomationEnabled()) {
                colony.getAutomationService().applyWarEconomyQuotas(colony);
            }
            colony.refreshRoleAssignmentForWarState(null);
        }
    }

    private static boolean hasConfiguredWarEconomyAssignments(Colony colony) {
        return sumActiveMilitaryRoleCounts(colony.getWarAssignedRoleCounts()) > 0;
    }

    public void cancelCrossDynastyTradesWith(Dynasty other, TradeManager tradeManager) {
        if (other == null || tradeManager == null) {
            return;
        }
        List<Trade> toCancel = new ArrayList<>();
        for (Trade trade : tradeManager.getActiveTrades()) {
            if (trade.isActive() && isTradeBetweenDynasties(trade, dynasty, other)) {
                toCancel.add(trade);
            }
        }
        for (Trade trade : toCancel) {
            trade.cancel();
            tradeManager.removeTrade(trade);
        }
    }

    record ReputationModifierLine(String label, int delta) {}

    public String buildReputationModifierTooltip(Dynasty other, World world) {
        if (other == null || other == dynasty) {
            return null;
        }
        StringBuilder sb = new StringBuilder("<html>");
        appendReputationModifierDetails(sb, other, world);
        sb.append("</html>");
        return sb.toString();
    }

    public String buildStanceIconTooltip(Dynasty other, World world) {
        if (other == null || other == dynasty) {
            return null;
        }
        int rep = getEffectiveDiplomaticReputation(other, world);
        DiplomaticReputation stance = GameConstants.getDiplomaticReputationLevel(rep);
        StringBuilder sb = new StringBuilder("<html><b>");
        sb.append(LanguageStrings.get(LanguageStrings.DIPLOMATIC_REPUTATION))
                .append(":</b> ")
                .append(stance.getName())
                .append(" (")
                .append(rep)
                .append(")<br>");
        appendReputationModifierDetails(sb, other, world);
        sb.append("</html>");
        return sb.toString();
    }

    List<ReputationModifierLine> collectVisibleReputationModifiers(Dynasty other, World world) {
        List<ReputationModifierLine> lines = new ArrayList<>();

        for (String modifierKey : dynasty.getDiplomaticModifierKeys(other.getId())) {
            DiplomaticReputationModifier modifier = GameConstants.getDiplomaticReputationModifierByKey(modifierKey);
            if (modifier != null) {
                lines.add(new ReputationModifierLine(modifier.getName(), modifier.getReputationDelta()));
            }
        }

        int friction = getBorderFrictionAdjustment(other, world);
        if (friction != 0) {
            lines.add(new ReputationModifierLine(GameConstants.DIPLO_MODIFIER_BORDER_FRICTION.getName(), friction));
        }

        int warmonger = getWarmongerAdjustment(other, world);
        if (warmonger != 0) {
            lines.add(new ReputationModifierLine(GameConstants.DIPLO_MODIFIER_WARMONGER.getName(), warmonger));
        }

        int militaryAdj = getMilitaryReputationAdjustment(other);
        if (militaryAdj != 0) {
            lines.add(new ReputationModifierLine(
                    LanguageStrings.get(LanguageStrings.DIPLO_MILITARY_STRENGTH), militaryAdj));
        }

        int diplomatAdj = getDiplomatReputationAdjustment(other);
        if (diplomatAdj != 0) {
            lines.add(new ReputationModifierLine(
                    LanguageStrings.get(LanguageStrings.DIPLO_MODIFIER_DIPLOMAT_MISSION), diplomatAdj));
        }

        lines.sort(Comparator.comparingInt(ReputationModifierLine::delta).reversed());
        return lines;
    }

    public int sumVisibleReputationModifierDeltas(Dynasty other, World world) {
        int total = 0;
        for (ReputationModifierLine line : collectVisibleReputationModifiers(other, world)) {
            total += line.delta();
        }
        return total;
    }

    private void appendReputationModifierLine(StringBuilder sb, String label, int delta) {
        sb.append(LanguageStrings.format(
                LanguageStrings.DIPLO_MODIFIER_LINE,
                label,
                LanguageStrings.formatSigned(delta))).append("<br>");
    }

    private void appendReputationModifierDetails(StringBuilder sb, Dynasty other, World world) {
        sb.append(LanguageStrings.get(LanguageStrings.DIPLO_TOOLTIP_BASE))
                .append(": ")
                .append(GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION)
                .append("<br>");

        List<ReputationModifierLine> modifiers = collectVisibleReputationModifiers(other, world);
        for (ReputationModifierLine line : modifiers) {
            appendReputationModifierLine(sb, line.label(), line.delta());
        }

        sb.append(LanguageStrings.get(LanguageStrings.DIPLO_TOOLTIP_EFFECTIVE))
                .append(": ")
                .append(getEffectiveDiplomaticReputation(other, world));
    }

    public boolean sharesBorderWith(Dynasty other, World world) {
        if (other == null || other == dynasty || world == null) {
            return false;
        }
        for (Colony colony : dynasty.getColonies()) {
            Hex hex = world.getHexOfColony(colony);
            if (hex == null) {
                continue;
            }
            for (Hex neighborHex : hex.getAdjacentNeighbors()) {
                if (neighborHex == null || neighborHex.getColony() == null) {
                    continue;
                }
                Dynasty neighborDynasty = neighborHex.getColony().getDynasty();
                if (neighborDynasty == other) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getBorderFrictionAdjustment(Dynasty other, World world) {
        if (!sharesBorderWith(other, world) || hasNonAggressionPact(other) || isAtWarWith(other)) {
            return 0;
        }
        return GameConstants.DIPLO_MODIFIER_BORDER_FRICTION.getReputationDelta();
    }

    public int getWarmongerAdjustment(Dynasty other, World world) {
        if (other == null || other == dynasty || world == null || world.getWarService() == null) {
            return 0;
        }
        if (world.getWarService().isWarmonger(dynasty.getId())
                || world.getWarService().isWarmonger(other.getId())) {
            return GameConstants.DIPLO_MODIFIER_WARMONGER.getReputationDelta();
        }
        return 0;
    }

    public int getEffectiveDiplomaticReputation(Dynasty other, World world) {
        if (other == null || other == dynasty) {
            return GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION;
        }
        int score = dynasty.getDiplomaticReputation(other.getId())
                + getBorderFrictionAdjustment(other, world)
                + getWarmongerAdjustment(other, world)
                + getMilitaryReputationAdjustment(other)
                + getDiplomatReputationAdjustment(other)
                + getAiPersonalityReputationAdjustment(other);
        return GameNumbers.clampDiplomaticReputation(score);
    }

    public int getAiPersonalityReputationAdjustment(Dynasty other) {
        if (other == null || other == dynasty) {
            return 0;
        }
        AiPersonality personality = other.getAiPersonality();
        if (personality == AiPersonality.MILITARIST) {
            return GameNumbers.AI_MILITARIST_REPUTATION_DELTA;
        }
        if (personality == AiPersonality.PACIFIST) {
            return GameNumbers.AI_PACIFIST_REPUTATION_DELTA;
        }
        return 0;
    }

    public int getDiplomatReputationAdjustment(Dynasty other) {
        if (other == null || other == dynasty) {
            return 0;
        }
        int gainPer = getDiplomatStabilityGainPerAnt();
        int ourSupport = Math.min(
                dynasty.getDiplomatSupportTo(other.getId()),
                countLiveDynastyMissionDiplomatsToward(other.getId()));
        int theirSupport = Math.min(
                other.getDiplomatSupportTo(dynasty.getId()),
                other.getDiplomacyService() != null
                        ? other.getDiplomacyService().countLiveDynastyMissionDiplomatsToward(dynasty.getId())
                        : 0);
        return (ourSupport + theirSupport) * gainPer;
    }

    public int countLiveDynastyMissionDiplomatsToward(int targetDynastyId) {
        int live = 0;
        for (Colony colony : dynasty.getColonies()) {
            int deployed = colony.getOutgoingDynastyDiplomatMissions().getOrDefault(targetDynastyId, 0);
            if (deployed <= 0) {
                continue;
            }
            live += Math.min(deployed, countLiveDiplomats(colony));
        }
        return live;
    }

    public int getMilitaryReputationAdjustment(Dynasty other) {
        if (other == null || other == dynasty) {
            return 0;
        }
        return ColonyMilitaryService.getMilitaryReputationAdjustment(
                dynasty.getMilitaryPower(), other.getMilitaryPower());
    }

    public boolean meetsTradeReputationRequirement(Dynasty other, World world) {
        return getEffectiveDiplomaticReputation(other, world) >= GameConstants.REPUTATION_NEUTRAL.getMinScore();
    }

    public boolean meetsTradeOfferReputationRequirement(Dynasty other, World world) {
        return getEffectiveDiplomaticReputation(other, world) >= GameConstants.REPUTATION_WARY.getMinScore();
    }

    public boolean meetsTradeRequestReputationRequirement(Dynasty other, World world) {
        return getEffectiveDiplomaticReputation(other, world) >= GameConstants.REPUTATION_NEUTRAL.getMinScore();
    }

    public boolean canNpcProposeTradeOffer(Dynasty other, World world) {
        return meetsTradeOfferReputationRequirement(other, world)
                && getEffectiveDiplomaticReputation(other, world) >= GameConstants.REPUTATION_CORDIAL.getMinScore();
    }

    public boolean canNpcProposeTradeRequest(Dynasty other, World world) {
        return meetsTradeRequestReputationRequirement(other, world);
    }

    public boolean canRequestTrade(Dynasty other, World world) {
        return canProposeCrossDynastyTrade(other, world, CrossDynastyTradeProposal.Kind.REQUEST);
    }

    public boolean canOfferTrade(Dynasty other, World world) {
        return canProposeCrossDynastyTrade(other, world, CrossDynastyTradeProposal.Kind.OFFER);
    }

    public static double computeTradeAcceptanceChance(int effectiveReputation) {
        int minRep = GameConstants.REPUTATION_NEUTRAL.getMinScore();
        int maxRep = GameNumbers.DIPLOMATIC_REPUTATION_MAX;
        if (effectiveReputation < minRep) {
            return 0.0;
        }
        if (effectiveReputation >= maxRep) {
            return 1.0;
        }
        return 0.5 + 0.5 * (effectiveReputation - minRep) / (double) (maxRep - minRep);
    }

    public static boolean meetsTradeLoyaltyRequirement(Colony colony) {
        if (colony == null) {
            return false;
        }
        return colony.getLoyalty() >= GameConstants.LOYALTY_DISLOYAL.getMinScore();
    }

    public boolean canParticipateInCrossDynastyTrade(Dynasty other, Colony localColony, World world) {
        if (other == null || other == dynasty || !isDiplomaticallyContactable(dynasty) || !isDiplomaticallyContactable(other)) {
            return false;
        }
        if (isAtWarWith(other)) {
            return false;
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            return false;
        }
        if (localColony == null || localColony.getDynasty() != dynasty) {
            return false;
        }
        if (!meetsTradeLoyaltyRequirement(localColony)) {
            return false;
        }
        return findNeighborColony(other, localColony, world) != null;
    }

    public boolean canEstablishCrossDynastyTrade(Dynasty other, Colony playerColony, World world, TradeManager tradeManager) {
        return canParticipateInCrossDynastyTrade(other, playerColony, world)
                && meetsTradeReputationRequirement(other, world);
    }

    public boolean canProposeCrossDynastyTrade(Dynasty other, World world, CrossDynastyTradeProposal.Kind kind) {
        if (other == null || other == dynasty || !isDiplomaticallyContactable(dynasty) || !isDiplomaticallyContactable(other)) {
            return false;
        }
        if (world != null) {
            dynasty.migrateLegacyTimedDiplomaticModifiers(world);
        }
        if (getTradeRequestDeclineCooldownMonthsRemaining(other, world) > 0) {
            return false;
        }
        if (isAtWarWith(other)) {
            return false;
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            return false;
        }
        if (kind == CrossDynastyTradeProposal.Kind.OFFER) {
            return meetsTradeOfferReputationRequirement(other, world);
        }
        return meetsTradeRequestReputationRequirement(other, world);
    }

    public boolean hasActiveCrossDynastyTrade(Dynasty other, TradeManager tradeManager) {
        if (other == null || other == dynasty || tradeManager == null) {
            return false;
        }
        for (Trade trade : tradeManager.getActiveTrades()) {
            if (trade.isActive() && isTradeBetweenDynasties(trade, dynasty, other)) {
                return true;
            }
        }
        return false;
    }

    public Colony findNeighborColony(Dynasty other, Colony playerColony, World world) {
        if (other == null || playerColony == null || world == null) {
            return null;
        }
        Hex playerHex = world.getHexOfColony(playerColony);
        if (playerHex == null) {
            return null;
        }
        for (Hex neighborHex : playerHex.getAdjacentNeighbors()) {
            if (neighborHex == null || neighborHex.getColony() == null) {
                continue;
            }
            Colony neighborColony = neighborHex.getColony();
            if (neighborColony.getDynasty() == other) {
                return neighborColony;
            }
        }
        return null;
    }

    public TradeProposalResult proposeCrossDynastyTrade(
            Colony localColony,
            Colony neighborColony,
            CrossDynastyTradeProposal.Kind kind,
            World world,
            TradeManager tradeManager) {
        if (localColony == null || neighborColony == null || world == null || tradeManager == null || kind == null) {
            return TradeProposalResult.FAILED;
        }
        Dynasty other = neighborColony.getDynasty();
        if (other == null || other == dynasty) {
            return TradeProposalResult.FAILED;
        }
        if (!canProposeCrossDynastyTrade(other, world, kind)) {
            return TradeProposalResult.FAILED;
        }
        if (!canParticipateInCrossDynastyTrade(other, localColony, world)) {
            return TradeProposalResult.FAILED;
        }
        if (!DynastyDiplomacyService.isCrossDynastyTrade(localColony, neighborColony)) {
            return TradeProposalResult.FAILED;
        }

        Colony origin;
        Colony destination;
        if (kind == CrossDynastyTradeProposal.Kind.OFFER) {
            origin = localColony;
            destination = neighborColony;
        } else {
            origin = neighborColony;
            destination = localColony;
        }

        if (!meetsTradeLoyaltyRequirement(origin)) {
            return TradeProposalResult.FAILED;
        }

        dynasty.bindTradeManager(tradeManager);
        DynastyTradeService tradeService = dynasty.getTradeService();
        if (tradeService == null) {
            return TradeProposalResult.FAILED;
        }
        if (tradeService.findTrade(origin, destination) != null) {
            return TradeProposalResult.FAILED;
        }

        Map<ResourceType, Double> load = DynastyTradeAutomation.computeOutboundLoad(origin, destination);
        if (load.isEmpty()) {
            return TradeProposalResult.FAILED;
        }

        CrossDynastyTradeProposal proposal = new CrossDynastyTradeProposal(
                dynasty.getId(),
                origin.getId(),
                destination.getId(),
                kind,
                load);

        return deliverTradeProposal(proposal, other, world, tradeManager);
    }

    public TradeProposalResult requestTrade(Dynasty other, Colony localColony, World world, TradeManager tradeManager) {
        if (other == null || localColony == null || world == null) {
            return TradeProposalResult.FAILED;
        }
        Colony neighbor = findNeighborColony(other, localColony, world);
        if (neighbor == null) {
            return TradeProposalResult.FAILED;
        }
        return proposeCrossDynastyTrade(localColony, neighbor, CrossDynastyTradeProposal.Kind.REQUEST, world, tradeManager);
    }

    public TradeProposalResult offerTrade(Dynasty other, Colony localColony, World world, TradeManager tradeManager) {
        if (other == null || localColony == null || world == null) {
            return TradeProposalResult.FAILED;
        }
        Colony neighbor = findNeighborColony(other, localColony, world);
        if (neighbor == null) {
            return TradeProposalResult.FAILED;
        }
        return proposeCrossDynastyTrade(localColony, neighbor, CrossDynastyTradeProposal.Kind.OFFER, world, tradeManager);
    }

    public void processIncomingTradeProposals(World world, TradeManager tradeManager) {
        if (world == null || tradeManager == null || dynasty.isPlayer()) {
            return;
        }
        List<CrossDynastyTradeProposal> pending = dynasty.copyPendingTradeProposals();
        if (pending.isEmpty()) {
            return;
        }

        CrossDynastyTradeProposal proposal = pending.get(0);
        Dynasty proposer = world.findDynastyById(proposal.getFromDynastyId());
        if (proposer == null || !isDiplomaticallyContactable(proposer)) {
            dynasty.removePendingTradeProposal(proposal);
            return;
        }

        if (!isTradeProposalStillValid(proposal, proposer, world, tradeManager)) {
            dynasty.removePendingTradeProposal(proposal);
            return;
        }

        int effectiveRep = getEffectiveDiplomaticReputation(proposer, world);
        if (GameRandom.nextDouble() < computeTradeAcceptanceChance(effectiveRep)) {
            acceptTradeProposal(proposer, proposal, world, tradeManager);
        } else {
            declineTradeProposal(proposer, proposal, world);
        }
    }

    public boolean acceptTradeProposal(Dynasty proposer, CrossDynastyTradeProposal proposal, World world, TradeManager tradeManager) {
        if (proposer == null || proposal == null || world == null || tradeManager == null) {
            return false;
        }
        if (proposal.getFromDynastyId() != proposer.getId()) {
            return false;
        }
        if (!isTradeProposalStillValid(proposal, proposer, world, tradeManager)) {
            dynasty.removePendingTradeProposal(proposal);
            proposer.removePendingTradeProposal(proposal);
            return false;
        }

        Colony origin = findColonyById(world, proposal.getOriginColonyId());
        Colony destination = findColonyById(world, proposal.getDestinationColonyId());
        if (origin == null || destination == null) {
            dynasty.removePendingTradeProposal(proposal);
            return false;
        }

        Hex originHex = world.getHexOfColony(origin);
        Hex destinationHex = world.getHexOfColony(destination);
        if (originHex == null || destinationHex == null) {
            dynasty.removePendingTradeProposal(proposal);
            return false;
        }

        Map<AntType, Integer> transport = DynastyTradeAutomation.buildTransport(origin);
        if (transport.isEmpty()) {
            dynasty.removePendingTradeProposal(proposal);
            return false;
        }

        Dynasty senderDynasty = origin.getDynasty();
        TradeMethod method = senderDynasty != null
                ? DynastyTradeAutomation.pickTradeMethod(senderDynasty, originHex, destinationHex)
                : GameConstants.METHOD_LAND;
        Map<ResourceType, Double> load = proposal.toResourceLoad();
        Trade trade = new Trade(originHex, destinationHex, load, null, transport, false, false, method);
        if (!trade.startTrip()) {
            dynasty.removePendingTradeProposal(proposal);
            return false;
        }

        tradeManager.addTrade(trade);
        dynasty.removePendingTradeProposal(proposal);
        proposer.removePendingTradeProposal(proposal);
        applyCrossDynastyTradeReputation(origin, destination, proposal.getKind());
        origin.logEvent(ColonyLogPrefixes.DYNASTY + " "
                + LanguageStrings.format(LanguageStrings.LOG_TRADE_PROPOSAL_ACCEPTED_FMT, destination.getName()));
        return true;
    }

    public void declineTradeProposal(Dynasty proposer, CrossDynastyTradeProposal proposal, World world) {
        if (proposer == null || proposal == null) {
            return;
        }
        dynasty.removePendingTradeProposal(proposal);
        proposer.removePendingTradeProposal(proposal);
        applyTradeDeclinePenalty(proposer);
        Colony logColony = proposer.getCapital();
        if (logColony == null && !proposer.getColonies().isEmpty()) {
            logColony = proposer.getColonies().get(0);
        }
        if (logColony != null) {
            logColony.logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + LanguageStrings.format(LanguageStrings.LOG_TRADE_PROPOSAL_DECLINED_FMT, dynasty.getName()));
        }
    }

    public String formatProposalLoadSummary(CrossDynastyTradeProposal proposal) {
        if (proposal == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceType, Double> entry : proposal.toResourceLoad().entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(LanguageStrings.format(LanguageStrings.TRADE_LOAD_ENTRY_FMT,
                    entry.getValue().intValue(),
                    entry.getKey().getName()));
        }
        return sb.toString();
    }

    public void applyCrossDynastyTradeReputation(
            Colony origin,
            Colony destination,
            CrossDynastyTradeProposal.Kind kind) {
        if (origin == null || destination == null || kind == null) {
            return;
        }
        Dynasty sender = origin.getDynasty();
        Dynasty receiver = destination.getDynasty();
        applyCrossDynastyTradeReputation(sender, receiver, kind);
    }

    public void applyCrossDynastyTradeReputation(
            Dynasty sender,
            Dynasty receiver,
            CrossDynastyTradeProposal.Kind kind) {
        if (sender == null || receiver == null || sender == receiver || kind == null) {
            return;
        }

        int receiveDelta = GameNumbers.CROSS_DYNASTY_TRADE_RECEIVER_REP;
        receiver.adjustDiplomaticReputation(sender.getId(), receiveDelta);
        receiver.markCrossDynastyTradeRepBonus(sender.getId());
        receiver.addDiplomaticModifierKey(sender.getId(), GameConstants.DIPLO_MODIFIER_TRADE.getNameKey());

        int senderDelta = kind == CrossDynastyTradeProposal.Kind.OFFER
                ? GameNumbers.CROSS_DYNASTY_TRADE_OFFER_SENDER_REP
                : GameConstants.DIPLO_MODIFIER_TRADE_REQUEST.getReputationDelta();
        sender.adjustDiplomaticReputation(receiver.getId(), senderDelta);
        if (kind == CrossDynastyTradeProposal.Kind.OFFER) {
            sender.addDiplomaticModifierKey(receiver.getId(), GameConstants.DIPLO_MODIFIER_TRADE_OFFER.getNameKey());
        } else {
            sender.addDiplomaticModifierKey(receiver.getId(), GameConstants.DIPLO_MODIFIER_TRADE_REQUEST.getNameKey());
        }
    }

    public void onCrossDynastyTradeEstablished(Dynasty sender, Dynasty receiver, CrossDynastyTradeProposal.Kind kind) {
        applyCrossDynastyTradeReputation(sender, receiver, kind);
    }

    public void onCrossDynastyTradeEstablished(Dynasty other) {
        onCrossDynastyTradeEstablished(dynasty, other, CrossDynastyTradeProposal.Kind.OFFER);
    }

    private TradeProposalResult deliverTradeProposal(
            CrossDynastyTradeProposal proposal,
            Dynasty receiver,
            World world,
            TradeManager tradeManager) {
        if (receiver.isPlayer()) {
            receiver.addPendingTradeProposal(proposal);
            return TradeProposalResult.QUEUED;
        }

        receiver.bindTradeManager(tradeManager);
        int effectiveRep = receiver.getDiplomacyService().getEffectiveDiplomaticReputation(dynasty, world);
        if (GameRandom.nextDouble() < computeTradeAcceptanceChance(effectiveRep)) {
            if (receiver.getDiplomacyService().acceptTradeProposal(dynasty, proposal, world, tradeManager)) {
                return TradeProposalResult.ACCEPTED;
            }
            return TradeProposalResult.FAILED;
        }
        receiver.getDiplomacyService().declineTradeProposal(dynasty, proposal, world);
        return TradeProposalResult.DECLINED;
    }

    private boolean isTradeProposalStillValid(
            CrossDynastyTradeProposal proposal,
            Dynasty proposer,
            World world,
            TradeManager tradeManager) {
        Colony origin = findColonyById(world, proposal.getOriginColonyId());
        Colony destination = findColonyById(world, proposal.getDestinationColonyId());
        if (origin == null || destination == null) {
            return false;
        }
        if (isAtWarWith(proposer)) {
            return false;
        }
        Dynasty originDynasty = origin.getDynasty();
        Dynasty destinationDynasty = destination.getDynasty();
        if (originDynasty == null || destinationDynasty == null) {
            return false;
        }
        if (!isDiplomaticallyContactable(originDynasty) || !isDiplomaticallyContactable(destinationDynasty)) {
            return false;
        }
        if (!meetsTradeLoyaltyRequirement(origin)) {
            return false;
        }
        if (!originDynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            return false;
        }

        proposer.bindTradeManager(tradeManager);
        DynastyTradeService tradeService = proposer.getTradeService();
        if (tradeService == null) {
            return false;
        }
        return tradeService.findTrade(origin, destination) == null
                && !proposal.toResourceLoad().isEmpty()
                && !DynastyTradeAutomation.buildTransport(origin).isEmpty();
    }

    private Colony findColonyById(World world, int colonyId) {
        for (Dynasty dynastyOnWorld : world.getDynastys()) {
            for (Colony colony : dynastyOnWorld.getColonies()) {
                if (colony.getId() == colonyId) {
                    return colony;
                }
            }
        }
        return null;
    }

    private void applyTradeDeclinePenalty(Dynasty other) {
        int delta = GameConstants.DIPLO_MODIFIER_TRADE_REQUEST.getReputationDelta();
        dynasty.adjustDiplomaticReputation(other.getId(), delta);
        other.adjustDiplomaticReputation(dynasty.getId(), delta);
        dynasty.addDiplomaticModifierKey(other.getId(), GameConstants.DIPLO_MODIFIER_TRADE_REQUEST.getNameKey());
        other.addDiplomaticModifierKey(dynasty.getId(), GameConstants.DIPLO_MODIFIER_TRADE_REQUEST.getNameKey());
    }

    public static boolean isCrossDynastyTrade(Colony origin, Colony destination) {
        if (origin == null || destination == null) {
            return false;
        }
        Dynasty originDynasty = origin.getDynasty();
        Dynasty destinationDynasty = destination.getDynasty();
        return originDynasty != null && destinationDynasty != null && originDynasty != destinationDynasty;
    }

    public static boolean isTradeBetweenDynasties(Trade trade, Dynasty first, Dynasty second) {
        if (trade == null || first == null || second == null) {
            return false;
        }
        Colony originColony = trade.getOrigin().getColony();
        Colony destinationColony = trade.getDestination().getColony();
        if (originColony == null || destinationColony == null) {
            return false;
        }
        Dynasty originDynasty = originColony.getDynasty();
        Dynasty destinationDynasty = destinationColony.getDynasty();
        return (originDynasty == first && destinationDynasty == second)
                || (originDynasty == second && destinationDynasty == first);
    }

    private void applyModifierBothWays(Dynasty other, DiplomaticReputationModifier modifier) {
        clearExclusiveGroupBothWays(other, modifier.getExclusiveGroupKey());
        dynasty.adjustDiplomaticReputation(other.getId(), modifier.getReputationDelta());
        other.adjustDiplomaticReputation(dynasty.getId(), modifier.getReputationDelta());
        dynasty.setDiplomaticModifierKey(other.getId(), modifier.getNameKey());
        other.setDiplomaticModifierKey(dynasty.getId(), modifier.getNameKey());
    }

    private void clearExclusiveGroupBothWays(Dynasty other, String exclusiveGroupKey) {
        clearExclusiveGroupOnPair(dynasty, other, exclusiveGroupKey);
        clearExclusiveGroupOnPair(other, dynasty, exclusiveGroupKey);
    }

    private void clearExclusiveGroupOnPair(Dynasty owner, Dynasty other, String exclusiveGroupKey) {
        if (exclusiveGroupKey == null) {
            return;
        }
        Set<String> keys = new LinkedHashSet<>(owner.getDiplomaticModifierKeys(other.getId()));
        for (String currentKey : keys) {
            DiplomaticReputationModifier current = GameConstants.getDiplomaticReputationModifierByKey(currentKey);
            if (current != null && exclusiveGroupKey.equals(current.getExclusiveGroupKey())) {
                owner.removeDiplomaticModifierKey(other.getId(), currentKey);
            }
        }
    }

    public int getMaxDiplomatsForDynastyMission() {
        return GameNumbers.DIPLOMAT_MAX_PER_DYNASTY_MISSION;
    }

    public int getMaxDiplomatsForColonyMission() {
        return GameNumbers.DIPLOMAT_MAX_PER_COLONY_MISSION;
    }

    public int getMaxDiplomatsPerTarget() {
        return GameNumbers.DIPLOMAT_MAX_PER_DYNASTY_MISSION;
    }

    public int getDiplomatStabilityGainPerAnt() {
        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_3)) {
            return GameNumbers.DIPLOMAT_STABILITY_GAIN_PRESSURE_3;
        }
        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2)) {
            return GameNumbers.DIPLOMAT_STABILITY_GAIN_PRESSURE_2;
        }
        return GameNumbers.DIPLOMAT_STABILITY_GAIN_BASE;
    }

    public int countAvailableDiplomats(Colony colony) {
        if (colony == null || !colonyHasDiplomatRole(colony)) {
            return 0;
        }
        return Math.max(0, colony.getAssignedRoleCount(GameConstants.ROLE_DIPLOMAT) - colony.getDeployedDiplomatCount());
    }

    public Colony pickDiplomatSourceColony() {
        return pickDiplomatSourceColony(null);
    }

    public Colony pickDiplomatSourceColony(Colony exclude) {
        Colony capital = dynasty.getCapital();
        if (capital != null && capital != exclude && countAvailableDiplomats(capital) > 0) {
            return capital;
        }
        return dynasty.getColonies().stream()
                .filter(c -> c != exclude && countAvailableDiplomats(c) > 0)
                .max(Comparator.comparingInt(this::countAvailableDiplomats))
                .orElse(null);
    }

    public void reconcileDiplomatDeployments(Colony source, int newAssigned) {
        if (source == null || source.getDynasty() != dynasty) {
            return;
        }
        int deployed = source.getDeployedDiplomatCount();
        if (deployed <= newAssigned) {
            return;
        }
        recallDiplomats(source, deployed - newAssigned);
    }

    private void recallDiplomats(Colony source, int count) {
        int remaining = count;
        while (remaining > 0) {
            Map.Entry<Integer, Integer> colonyMission = largestMissionEntry(source.getOutgoingColonyDiplomatMissions());
            Map.Entry<Integer, Integer> dynastyMission = largestMissionEntry(source.getOutgoingDynastyDiplomatMissions());
            if (colonyMission == null && dynastyMission == null) {
                break;
            }
            if (dynastyMission != null
                    && (colonyMission == null || dynastyMission.getValue() >= colonyMission.getValue())) {
                undeployDynastyMission(source, dynastyMission.getKey(), 1);
            } else {
                undeployColonyMission(source, colonyMission.getKey(), 1);
            }
            remaining--;
        }
    }

    private Map.Entry<Integer, Integer> largestMissionEntry(Map<Integer, Integer> missions) {
        Map.Entry<Integer, Integer> largest = null;
        for (Map.Entry<Integer, Integer> entry : missions.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            if (largest == null || entry.getValue() > largest.getValue()) {
                largest = entry;
            }
        }
        return largest;
    }

    private void deployColonyMission(Colony from, Colony target, int count) {
        from.getOutgoingColonyDiplomatMissions().merge(target.getId(), count, Integer::sum);
        target.getIncomingColonyDiplomatSupport().merge(from.getId(), count, Integer::sum);
    }

    private void undeployColonyMission(Colony from, int targetColonyId, int count) {
        int current = from.getOutgoingColonyDiplomatMissions().getOrDefault(targetColonyId, 0);
        int toRemove = Math.min(count, current);
        if (toRemove <= 0) {
            return;
        }
        int next = current - toRemove;
        if (next == 0) {
            from.getOutgoingColonyDiplomatMissions().remove(targetColonyId);
        } else {
            from.getOutgoingColonyDiplomatMissions().put(targetColonyId, next);
        }
        Colony target = findColonyById(targetColonyId);
        if (target != null) {
            int incoming = target.getIncomingColonyDiplomatSupport().getOrDefault(from.getId(), 0);
            int incomingNext = Math.max(0, incoming - toRemove);
            if (incomingNext == 0) {
                target.getIncomingColonyDiplomatSupport().remove(from.getId());
            } else {
                target.getIncomingColonyDiplomatSupport().put(from.getId(), incomingNext);
            }
        }
    }

    private void deployDynastyMission(Colony from, Dynasty targetDynasty, int count) {
        from.getOutgoingDynastyDiplomatMissions().merge(targetDynasty.getId(), count, Integer::sum);
        dynasty.addDiplomatSupportTo(targetDynasty.getId(), count);
    }

    private void undeployDynastyMission(Colony from, int targetDynastyId, int count) {
        int current = from.getOutgoingDynastyDiplomatMissions().getOrDefault(targetDynastyId, 0);
        int toRemove = Math.min(count, current);
        if (toRemove <= 0) {
            return;
        }
        int next = current - toRemove;
        if (next == 0) {
            from.getOutgoingDynastyDiplomatMissions().remove(targetDynastyId);
        } else {
            from.getOutgoingDynastyDiplomatMissions().put(targetDynastyId, next);
        }
        dynasty.removeDiplomatSupportTo(targetDynastyId, toRemove);
    }

    private Colony findColonyById(int colonyId) {
        for (Colony colony : dynasty.getColonies()) {
            if (colony.getId() == colonyId) {
                return colony;
            }
        }
        return null;
    }

    private boolean colonyHasDiplomatRole(Colony colony) {
        return colony != null && colony.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT);
    }

    public boolean needsDiplomatMissionToDynasty(Dynasty other, World world) {
        if (other == null || other == dynasty) {
            return false;
        }
        return GameConstants.allowsDiplomatMissionToDynasty(getEffectiveDiplomaticReputation(other, world));
    }

    public boolean needsDiplomatMissionToColony(Colony target, TradeManager tradeManager, World world) {
        if (target == null) {
            return false;
        }
        return GameConstants.allowsDiplomatMissionToColony(target.getEffectiveLoyalty(tradeManager, world));
    }

    public boolean canSendDiplomatsToColony(Colony from, Colony target, TradeManager tradeManager, World world) {
        return from != null && target != null && from != target && target.getDynasty() == dynasty
                && from.getDynasty() == dynasty
                && countAvailableDiplomats(from) > 0
                && needsDiplomatMissionToColony(target, tradeManager, world);
    }

    public int sendDiplomatsToColony(Colony from, Colony target, int requestedCount,
            TradeManager tradeManager, World world) {
        if (!canSendDiplomatsToColony(from, target, tradeManager, world) || requestedCount <= 0) {
            return 0;
        }
        int room = Math.max(0, getMaxDiplomatsPerTarget() - countColonyMissionDiplomatsOn(target));
        int toSend = Math.min(requestedCount, Math.min(
                countAvailableDiplomats(from),
                Math.min(getMaxDiplomatsForColonyMission(), room)));
        if (toSend <= 0) {
            return 0;
        }
        deployColonyMission(from, target, toSend);
        dynasty.recordDiplomatsSent(toSend);
        return toSend;
    }

    public void runAutomatedColonyLoyalty(Dynasty owner, World world, TradeManager tradeManager) {
        if (owner == null || owner != dynasty || world == null || tradeManager == null) {
            return;
        }
        if (!owner.hasUpgrade(GameUnlocks.ABILITY_AUTO_DIPLOMACY)
                || !owner.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
            return;
        }

        List<Colony> targets = new ArrayList<>();
        for (Colony colony : owner.getColonies()) {
            if (colony.getAge() < 7) {
                continue;
            }
            int effectiveLoyalty = colony.getEffectiveLoyalty(tradeManager, world);
            if (effectiveLoyalty >= GameConstants.LOYALTY_MILITANT.getMinScore()) {
                continue;
            }
            if (!needsDiplomatMissionToColony(colony, tradeManager, world)) {
                continue;
            }
            targets.add(colony);
        }
        targets.sort(Comparator.comparingInt(c -> c.getEffectiveLoyalty(tradeManager, world)));

        for (Colony target : targets) {
            for (Colony source : pickAutomatedDiplomatSources(owner, tradeManager, world)) {
                if (source == target || !canSendDiplomatsToColony(source, target, tradeManager, world)) {
                    continue;
                }
                int room = Math.max(0, getMaxDiplomatsPerTarget() - countColonyMissionDiplomatsOn(target));
                if (room <= 0) {
                    break;
                }
                int max = Math.min(countAvailableDiplomats(source),
                        Math.min(getMaxDiplomatsForColonyMission(), room));
                int sent = sendDiplomatsToColony(source, target, max, tradeManager, world);
                if (sent > 0) {
                    int gain = sent * getDiplomatStabilityGainPerAnt();
                    target.logEvent(ColonyLogPrefixes.AUTOMATION + " "
                            + LanguageStrings.format(LanguageStrings.LOG_AUTO_DIPLOMAT_COLONY_FMT,
                                    sent, target.getName(), gain));
                }
            }
        }
    }

    public List<Colony> listDiplomatSourceColonies(TradeManager tradeManager, World world) {
        List<Colony> sources = new ArrayList<>();
        Colony capital = dynasty.getCapital();
        if (capital != null && capital.getAge() >= 7) {
            sources.add(capital);
        }
        for (Colony colony : dynasty.getColonies()) {
            if (colony == capital || colony.getAge() < 7) {
                continue;
            }
            int effectiveLoyalty = colony.getEffectiveLoyalty(tradeManager, world);
            if (effectiveLoyalty >= GameConstants.LOYALTY_MILITANT.getMinScore()) {
                sources.add(colony);
            }
        }
        return sources;
    }

    private List<Colony> pickAutomatedDiplomatSources(Dynasty owner, TradeManager tradeManager, World world) {
        if (owner == null || owner != dynasty || tradeManager == null || world == null) {
            return List.of();
        }
        return listDiplomatSourceColonies(tradeManager, world).stream()
                .filter(colony -> countAvailableDiplomats(colony) > 0)
                .sorted(Comparator.comparingInt(this::countAvailableDiplomats).reversed())
                .toList();
    }

    public boolean canSendDiplomatsToDynasty(Colony from, Dynasty other, World world) {
        if (other == null || other == dynasty || from == null || world == null) {
            return false;
        }
        if (from.getDynasty() != dynasty || isAtWarWith(other)) {
            return false;
        }
        return countAvailableDiplomats(from) > 0 && needsDiplomatMissionToDynasty(other, world);
    }

    public int sendDiplomatsToDynasty(Colony from, Dynasty other, int requestedCount, World world) {
        if (!canSendDiplomatsToDynasty(from, other, world) || requestedCount <= 0) {
            return 0;
        }
        int room = Math.max(0, getMaxDiplomatsPerTarget() - countDynastyMissionDiplomatsToward(other));
        int toSend = Math.min(requestedCount, Math.min(
                countAvailableDiplomats(from),
                Math.min(getMaxDiplomatsForDynastyMission(), room)));
        if (toSend <= 0) {
            return 0;
        }
        deployDynastyMission(from, other, toSend);
        dynasty.recordDiplomatsSent(toSend);
        return toSend;
    }

    public boolean hasAnyDiplomatRoleColony() {
        for (Colony colony : dynasty.getColonies()) {
            if (colonyHasDiplomatRole(colony)) {
                return true;
            }
        }
        return false;
    }

    public int countDynastyWideAvailableDiplomats() {
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += countAvailableDiplomats(colony);
        }
        return total;
    }

    public int countDynastyMissionDiplomatsToward(Dynasty other) {
        if (other == null) {
            return 0;
        }
        int deployed = 0;
        for (Colony colony : dynasty.getColonies()) {
            deployed += colony.getOutgoingDynastyDiplomatMissions().getOrDefault(other.getId(), 0);
        }
        return deployed;
    }

    public int countColonyMissionDiplomatsOn(Colony target) {
        if (target == null) {
            return 0;
        }
        return target.getIncomingColonyDiplomatSupport().values().stream().mapToInt(Integer::intValue).sum();
    }

    public int computeMaxDynastyMissionDiplomatsToward(Dynasty other) {
        if (other == null) {
            return 0;
        }
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += maxDynastyMissionFromColony(colony, other.getId());
        }
        return Math.min(getMaxDiplomatsPerTarget(), total);
    }

    public int computeMaxColonyMissionDiplomatsOn(Colony target) {
        if (target == null) {
            return 0;
        }
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            if (colony == target) {
                continue;
            }
            total += maxColonyMissionFromColony(colony, target.getId());
        }
        return Math.min(getMaxDiplomatsPerTarget(), total);
    }

    public boolean canManageDynastyMissionDiplomats(Dynasty other, World world) {
        if (other == null || other == dynasty || world == null || isAtWarWith(other)) {
            return false;
        }
        if (!hasAnyDiplomatRoleColony()) {
            return false;
        }
        if (countDynastyMissionDiplomatsToward(other) > 0) {
            return true;
        }
        return countDynastyWideAvailableDiplomats() > 0 && needsDiplomatMissionToDynasty(other, world);
    }

    public boolean canManageColonyMissionDiplomats(Colony target, TradeManager tradeManager, World world) {
        if (target == null || target.getDynasty() != dynasty) {
            return false;
        }
        if (!hasAnyDiplomatRoleColony()) {
            return false;
        }
        if (countColonyMissionDiplomatsOn(target) > 0) {
            return true;
        }
        return countDynastyWideAvailableDiplomats() > 0
                && needsDiplomatMissionToColony(target, tradeManager, world);
    }

    public void assignDynastyMissionDiplomats(Dynasty other, int targetTotal, World world) {
        if (other == null || other == dynasty || world == null) {
            return;
        }
        int before = countDynastyMissionDiplomatsToward(other);
        int capped = Math.max(0, Math.min(targetTotal, getMaxDiplomatsPerTarget()));
        capped = Math.min(capped, computeMaxDynastyMissionDiplomatsToward(other));
        clearDynastyMissionToward(other.getId());
        int remaining = capped;
        List<Colony> sources = listDiplomatDeploySources(null);
        sources.sort(Comparator.comparingInt((Colony c) -> maxDynastyMissionFromColony(c, other.getId())).reversed());
        for (Colony colony : sources) {
            if (remaining <= 0) {
                break;
            }
            int deploy = Math.min(remaining, maxDynastyMissionFromColony(colony, other.getId()));
            if (deploy > 0) {
                deployDynastyMission(colony, other, deploy);
                remaining -= deploy;
            }
        }
        int after = countDynastyMissionDiplomatsToward(other);
        if (after > before) {
            dynasty.recordDiplomatsSent(after - before);
        }
    }

    public void assignColonyMissionDiplomats(Colony target, int targetTotal, TradeManager tradeManager, World world) {
        if (target == null || target.getDynasty() != dynasty) {
            return;
        }
        int before = countColonyMissionDiplomatsOn(target);
        int capped = Math.max(0, Math.min(targetTotal, getMaxDiplomatsPerTarget()));
        capped = Math.min(capped, computeMaxColonyMissionDiplomatsOn(target));
        clearColonyMissionTo(target.getId());
        int remaining = capped;
        List<Colony> sources = listDiplomatDeploySources(target);
        sources.sort(Comparator.comparingInt((Colony c) -> maxColonyMissionFromColony(c, target.getId())).reversed());
        for (Colony colony : sources) {
            if (remaining <= 0) {
                break;
            }
            int deploy = Math.min(remaining, maxColonyMissionFromColony(colony, target.getId()));
            if (deploy > 0) {
                deployColonyMission(colony, target, deploy);
                remaining -= deploy;
            }
        }
        int after = countColonyMissionDiplomatsOn(target);
        if (after > before) {
            dynasty.recordDiplomatsSent(after - before);
        }
    }

    private int maxDynastyMissionFromColony(Colony colony, int targetDynastyId) {
        if (!colonyHasDiplomatRole(colony)) {
            return 0;
        }
        int assigned = colony.getAssignedRoleCount(GameConstants.ROLE_DIPLOMAT);
        int integration = colony.getIntegrationDiplomatsDeployed();
        int otherMissionTotal = missionDeployedExcludingDynastyTarget(colony, targetDynastyId);
        return Math.min(getMaxDiplomatsForDynastyMission(),
                Math.max(0, assigned - integration - otherMissionTotal));
    }

    private int maxColonyMissionFromColony(Colony colony, int targetColonyId) {
        if (colony == null || colony.getId() == targetColonyId || !colonyHasDiplomatRole(colony)) {
            return 0;
        }
        int assigned = colony.getAssignedRoleCount(GameConstants.ROLE_DIPLOMAT);
        int integration = colony.getIntegrationDiplomatsDeployed();
        int otherMissionTotal = missionDeployedExcludingColonyTarget(colony, targetColonyId);
        return Math.min(getMaxDiplomatsForColonyMission(),
                Math.max(0, assigned - integration - otherMissionTotal));
    }

    private int missionDeployedExcludingDynastyTarget(Colony colony, int targetDynastyId) {
        int total = colony.getOutgoingColonyDiplomatMissions().values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<Integer, Integer> entry : colony.getOutgoingDynastyDiplomatMissions().entrySet()) {
            if (entry.getKey() != targetDynastyId) {
                total += entry.getValue();
            }
        }
        return total;
    }

    private int missionDeployedExcludingColonyTarget(Colony colony, int targetColonyId) {
        int total = colony.getOutgoingDynastyDiplomatMissions().values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<Integer, Integer> entry : colony.getOutgoingColonyDiplomatMissions().entrySet()) {
            if (entry.getKey() != targetColonyId) {
                total += entry.getValue();
            }
        }
        return total;
    }

    private void clearDynastyMissionToward(int targetDynastyId) {
        for (Colony colony : dynasty.getColonies()) {
            int deployed = colony.getOutgoingDynastyDiplomatMissions().getOrDefault(targetDynastyId, 0);
            if (deployed > 0) {
                undeployDynastyMission(colony, targetDynastyId, deployed);
            }
        }
    }

    private void clearColonyMissionTo(int targetColonyId) {
        for (Colony colony : dynasty.getColonies()) {
            int deployed = colony.getOutgoingColonyDiplomatMissions().getOrDefault(targetColonyId, 0);
            if (deployed > 0) {
                undeployColonyMission(colony, targetColonyId, deployed);
            }
        }
    }

    private List<Colony> listDiplomatDeploySources(Colony exclude) {
        List<Colony> sources = new ArrayList<>();
        for (Colony colony : dynasty.getColonies()) {
            if (colony != exclude && colonyHasDiplomatRole(colony)) {
                sources.add(colony);
            }
        }
        sources.sort(Comparator.comparingInt(this::countAvailableDiplomats).reversed());
        return sources;
    }

    public void validateAllDiplomatDeployments(World world) {
        for (Colony colony : dynasty.getColonies()) {
            reconcileDiplomatDeploymentsAfterCasualty(colony, null, world, false);
        }
    }

    public void reconcileDiplomatDeploymentsAfterCasualty(Colony source, AntRole formerRole) {
        reconcileDiplomatDeploymentsAfterCasualty(source, formerRole, null, true);
    }

    private void reconcileDiplomatDeploymentsAfterCasualty(
            Colony source, AntRole formerRole, World world, boolean logCollapses) {
        if (source == null || source.getDynasty() != dynasty) {
            return;
        }
        if (!colonyHasDiplomatRole(source) && source.getDeployedDiplomatCount() == 0) {
            return;
        }

        int liveDiplomats = countLiveDiplomats(source);
        int assigned = source.getAssignedRoleCount(GameConstants.ROLE_DIPLOMAT);
        int integrationDeployed = source.getIntegrationDiplomatsDeployed();
        int missionDeployed = source.getDeployedDiplomatCount() - integrationDeployed;
        int maxMissionSupport = Math.max(0, Math.min(assigned, liveDiplomats) - integrationDeployed);

        if (missionDeployed <= maxMissionSupport) {
            return;
        }

        Map<Integer, Integer> beforeColony = new HashMap<>(source.getOutgoingColonyDiplomatMissions());
        Map<Integer, Integer> beforeDynasty = new HashMap<>(source.getOutgoingDynastyDiplomatMissions());
        recallDiplomats(source, missionDeployed - maxMissionSupport);

        if (!logCollapses) {
            return;
        }

        for (Map.Entry<Integer, Integer> entry : beforeColony.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            int after = source.getOutgoingColonyDiplomatMissions().getOrDefault(entry.getKey(), 0);
            if (after == 0) {
                Colony target = findColonyById(entry.getKey());
                String targetName = target != null ? target.getName() : "?";
                source.logEvent(ColonyLogPrefixes.WARNING + " "
                        + LanguageStrings.format(LanguageStrings.LOG_DIPLOMAT_MISSION_COLLAPSED_COLONY_FMT, targetName));
            }
        }
        for (Map.Entry<Integer, Integer> entry : beforeDynasty.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            int after = source.getOutgoingDynastyDiplomatMissions().getOrDefault(entry.getKey(), 0);
            if (after == 0) {
                String targetName = "?";
                if (world != null) {
                    Dynasty target = world.findDynastyById(entry.getKey());
                    if (target != null) {
                        targetName = target.getName();
                    }
                }
                source.logEvent(ColonyLogPrefixes.WARNING + " "
                        + LanguageStrings.format(LanguageStrings.LOG_DIPLOMAT_MISSION_COLLAPSED_DYNASTY_FMT, targetName));
            }
        }
    }

    private int countLiveDiplomats(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int count = 0;
        for (Ant ant : colony.getPrincesses()) {
            if (ant.isAlive() && ant.getRole() == GameConstants.ROLE_DIPLOMAT) {
                count++;
            }
        }
        return count;
    }

    public int countDynastyLiveDrones() {
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += countLiveDrones(colony);
        }
        return total;
    }

    public boolean canOfferGeneticExchange(Dynasty target, World world) {
        if (target == null || target == dynasty || world == null) {
            return false;
        }
        if (isAtWarWith(target)) {
            return false;
        }
        if (!isDiplomaticallyContactable(dynasty) || !isDiplomaticallyContactable(target)) {
            return false;
        }
        if (getEffectiveDiplomaticReputation(target, world) < GameConstants.REPUTATION_CORDIAL.getMinScore()) {
            return false;
        }
        if (hasActiveGeneticExchangeWith(target)) {
            return false;
        }
        return countDynastyLiveDrones() >= GameNumbers.GENETIC_EXCHANGE_DRONE_COST;
    }

    public boolean offerGeneticExchange(Dynasty target, World world) {
        if (!canOfferGeneticExchange(target, world)) {
            return false;
        }
        Colony logColony = spendDynastyRandomDrones(GameNumbers.GENETIC_EXCHANGE_DRONE_COST);
        if (logColony == null) {
            return false;
        }

        target.adjustDiplomaticReputation(dynasty.getId(), GameConstants.DIPLO_MODIFIER_GENETIC_EXCHANGE.getReputationDelta());
        target.addDiplomaticModifierKey(dynasty.getId(), GameConstants.DIPLO_MODIFIER_GENETIC_EXCHANGE.getNameKey());

        logColony.logEvent(ColonyLogPrefixes.DYNASTY + " "
                + LanguageStrings.format(LanguageStrings.LOG_GENETIC_EXCHANGE_FMT, target.getName()));
        return true;
    }

    public boolean hasActiveGeneticExchangeWith(Dynasty other) {
        if (other == null || other == dynasty) {
            return false;
        }
        return other.hasDiplomaticModifierKey(
                dynasty.getId(), GameConstants.DIPLO_MODIFIER_GENETIC_EXCHANGE.getNameKey());
    }

    private Colony spendDynastyRandomDrones(int count) {
        List<Ant> liveDrones = new ArrayList<>();
        Map<Ant, Colony> owners = new HashMap<>();
        for (Colony colony : dynasty.getColonies()) {
            for (Ant drone : colony.getDrones()) {
                if (drone.isAlive()) {
                    liveDrones.add(drone);
                    owners.put(drone, colony);
                }
            }
        }
        if (liveDrones.size() < count) {
            return null;
        }
        Collections.shuffle(liveDrones, GameRandom.getShuffleRandom());
        Map<Colony, Integer> spentPerColony = new HashMap<>();
        for (int i = 0; i < count; i++) {
            Ant drone = liveDrones.get(i);
            Colony colony = owners.get(drone);
            drone.goDie(colony, DeathCause.OTHER);
            colony.getDrones().remove(drone);
            colony.recordAntDeath(drone, DeathCause.OTHER);
            spentPerColony.merge(colony, 1, Integer::sum);
        }
        Colony logColony = null;
        int mostSpent = 0;
        for (Map.Entry<Colony, Integer> entry : spentPerColony.entrySet()) {
            if (entry.getValue() > mostSpent) {
                mostSpent = entry.getValue();
                logColony = entry.getKey();
            }
        }
        if (logColony == null) {
            logColony = dynasty.getCapital();
        }
        return logColony;
    }

    private int countLiveDrones(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int count = 0;
        for (Ant drone : colony.getDrones()) {
            if (drone.isAlive()) {
                count++;
            }
        }
        return count;
    }
}
