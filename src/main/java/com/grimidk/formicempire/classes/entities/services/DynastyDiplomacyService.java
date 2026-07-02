package com.grimidk.formicempire.classes.entities.services;

import java.util.ArrayList;
import java.util.List;

import com.grimidk.formicempire.classes.constants.misc.DiplomaticReputationModifier;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class DynastyDiplomacyService {

    private final Dynasty dynasty;

    public DynastyDiplomacyService(Dynasty dynasty) {
        this.dynasty = dynasty;
    }

    public boolean hasNonAggressionPact(Dynasty other) {
        if (other == null || other == dynasty) {
            return false;
        }
        return GameConstants.DIPLO_MODIFIER_PACT.getNameKey()
                .equals(dynasty.getDiplomaticModifierKey(other.getId()));
    }

    public boolean canFormNonAggressionPact(Dynasty other) {
        if (other == null || other == dynasty || dynasty.isDefeated() || other.isDefeated()) {
            return false;
        }
        if (hasNonAggressionPact(other)) {
            return false;
        }
        return GameConstants.DIPLO_MODIFIER_PACT
                .meetsReputationRequirement(dynasty.getDiplomaticReputation(other.getId()));
    }

    public boolean canBreakNonAggressionPact(Dynasty other) {
        return hasNonAggressionPact(other);
    }

    public void formNonAggressionPact(Dynasty other) {
        if (!canFormNonAggressionPact(other)) {
            return;
        }
        applyModifierBothWays(other, GameConstants.DIPLO_MODIFIER_PACT);
    }

    public void breakNonAggressionPact(Dynasty other) {
        if (!canBreakNonAggressionPact(other)) {
            return;
        }
        applyModifierBothWays(other, GameConstants.DIPLO_MODIFIER_BROKEN_PACT);
    }

    public boolean isAtWarWith(Dynasty other) {
        if (other == null || other == dynasty) {
            return false;
        }
        return GameConstants.DIPLO_MODIFIER_WAR.getNameKey()
                .equals(dynasty.getDiplomaticModifierKey(other.getId()));
    }

    public void applyWar(Dynasty other) {
        TradeManager tradeManager = dynasty.getTradeService() != null
                ? dynasty.getTradeService().getTradeManager()
                : null;
        applyWar(other, tradeManager);
    }

    public void applyWar(Dynasty other, TradeManager tradeManager) {
        if (other == null || other == dynasty || dynasty.isDefeated() || other.isDefeated()) {
            return;
        }
        if (isAtWarWith(other)) {
            return;
        }
        cancelCrossDynastyTradesWith(other, tradeManager);
        applyModifierBothWays(other, GameConstants.DIPLO_MODIFIER_WAR);
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

    public String buildReputationModifierTooltip(Dynasty other, World world) {
        if (other == null || other == dynasty) {
            return null;
        }
        StringBuilder sb = new StringBuilder("<html>");
        int base = dynasty.getDiplomaticReputation(other.getId());
        sb.append(LanguageStrings.get(LanguageStrings.DIPLO_TOOLTIP_BASE))
                .append(": ")
                .append(base)
                .append("<br>");

        String modifierKey = dynasty.getDiplomaticModifierKey(other.getId());
        if (modifierKey != null) {
            DiplomaticReputationModifier modifier = GameConstants.getDiplomaticReputationModifierByKey(modifierKey);
            if (modifier != null) {
                sb.append(modifier.getName()).append("<br>");
            }
        }

        int friction = getBorderFrictionAdjustment(other, world);
        if (friction != 0) {
            sb.append(String.format(
                    LanguageStrings.get(LanguageStrings.DIPLO_MODIFIER_LINE),
                    GameConstants.DIPLO_MODIFIER_BORDER_FRICTION.getName(),
                    friction)).append("<br>");
        }

        int militaryAdj = getMilitaryReputationAdjustment(other);
        if (militaryAdj != 0) {
            sb.append(String.format(
                    LanguageStrings.get(LanguageStrings.DIPLO_MODIFIER_LINE),
                    LanguageStrings.get(LanguageStrings.DIPLO_MILITARY_STRENGTH),
                    militaryAdj)).append("<br>");
        }

        sb.append(LanguageStrings.get(LanguageStrings.DIPLO_TOOLTIP_EFFECTIVE))
                .append(": ")
                .append(getEffectiveDiplomaticReputation(other, world));
        sb.append("</html>");
        return sb.toString();
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
            for (Hex neighborHex : adjacentHexes(hex)) {
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

    public int getEffectiveDiplomaticReputation(Dynasty other, World world) {
        if (other == null || other == dynasty) {
            return GameConstants.DEFAULT_DIPLOMATIC_REPUTATION;
        }
        int score = dynasty.getDiplomaticReputation(other.getId())
                + getBorderFrictionAdjustment(other, world)
                + getMilitaryReputationAdjustment(other);
        return GameConstants.clampDiplomaticReputation(score);
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

    public boolean meetsTradeRequestReputationRequirement(Dynasty other, World world) {
        return GameConstants.DIPLO_MODIFIER_TRADE_REQUEST
                .meetsReputationRequirement(getEffectiveDiplomaticReputation(other, world));
    }

    public boolean canRequestTrade(Dynasty other, World world) {
        if (other == null || other == dynasty || dynasty.isDefeated() || other.isDefeated()) {
            return false;
        }
        if (isAtWarWith(other)) {
            return false;
        }
        return meetsTradeRequestReputationRequirement(other, world);
    }

    public static boolean meetsTradeLoyaltyRequirement(Colony colony) {
        if (colony == null) {
            return false;
        }
        return colony.getLoyalty() >= GameConstants.LOYALTY_DISLOYAL.getMinScore();
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

    public boolean canEstablishCrossDynastyTrade(Dynasty other, Colony playerColony, World world, TradeManager tradeManager) {
        if (other == null || other == dynasty || dynasty.isDefeated() || other.isDefeated()) {
            return false;
        }
        if (isAtWarWith(other)) {
            return false;
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            return false;
        }
        if (playerColony == null || playerColony.getDynasty() != dynasty) {
            return false;
        }
        if (!meetsTradeLoyaltyRequirement(playerColony)) {
            return false;
        }
        if (!meetsTradeReputationRequirement(other, world)) {
            return false;
        }
        return findNeighborColony(other, playerColony, world) != null;
    }

    public Colony findNeighborColony(Dynasty other, Colony playerColony, World world) {
        if (other == null || playerColony == null || world == null) {
            return null;
        }
        Hex playerHex = world.getHexOfColony(playerColony);
        if (playerHex == null) {
            return null;
        }
        for (Hex neighborHex : adjacentHexes(playerHex)) {
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

    public void requestTrade(Dynasty other, World world) {
        if (!canRequestTrade(other, world)) {
            return;
        }
        int delta = GameConstants.DIPLO_MODIFIER_TRADE_REQUEST.getReputationDelta();
        dynasty.adjustDiplomaticReputation(other.getId(), delta);
        other.adjustDiplomaticReputation(dynasty.getId(), delta);
    }

    public void onCrossDynastyTradeEstablished(Dynasty other) {
        if (other == null || other == dynasty) {
            return;
        }
        if (!dynasty.hasCrossDynastyTradeRepBonus(other.getId())) {
            int delta = GameConstants.DIPLO_MODIFIER_TRADE.getReputationDelta();
            dynasty.adjustDiplomaticReputation(other.getId(), delta);
            other.adjustDiplomaticReputation(dynasty.getId(), delta);
            dynasty.markCrossDynastyTradeRepBonus(other.getId());
            other.markCrossDynastyTradeRepBonus(dynasty.getId());
        }
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
        String currentKey = owner.getDiplomaticModifierKey(other.getId());
        if (currentKey == null) {
            return;
        }
        DiplomaticReputationModifier current = GameConstants.getDiplomaticReputationModifierByKey(currentKey);
        if (current != null && exclusiveGroupKey.equals(current.getExclusiveGroupKey())) {
            owner.clearDiplomaticModifierKey(other.getId());
        }
    }

    private static Hex[] adjacentHexes(Hex center) {
        return new Hex[] {
            center.getNorth(), center.getNorthEast(), center.getSouthEast(),
            center.getSouth(), center.getSouthWest(), center.getNorthWest()
        };
    }

    public int getMaxDiplomatsForDynastyMission() {
        return GameConstants.DIPLOMAT_MAX_PER_DYNASTY_MISSION;
    }

    public int getMaxDiplomatsForColonyMission() {
        return GameConstants.DIPLOMAT_MAX_PER_COLONY_MISSION;
    }

    public int getDiplomatStabilityGainPerAnt() {
        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_3)) {
            return GameConstants.DIPLOMAT_STABILITY_GAIN_PRESSURE_3;
        }
        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2)) {
            return GameConstants.DIPLOMAT_STABILITY_GAIN_PRESSURE_2;
        }
        return GameConstants.DIPLOMAT_STABILITY_GAIN_BASE;
    }

    public int countAvailableDiplomats(Colony colony) {
        if (colony == null || !colonyHasDiplomatRole(colony)) {
            return 0;
        }
        return colony.getAssignedRoleCount(GameConstants.ROLE_DIPLOMAT);
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

    public boolean canSendDiplomatsToColony(Colony target, TradeManager tradeManager, World world) {
        return target != null && target.getDynasty() == dynasty
                && countAvailableDiplomats(target) > 0
                && needsDiplomatMissionToColony(target, tradeManager, world);
    }

    public int sendDiplomatsToColony(Colony target, int requestedCount, TradeManager tradeManager, World world) {
        if (!canSendDiplomatsToColony(target, tradeManager, world) || requestedCount <= 0) {
            return 0;
        }
        int toSend = Math.min(requestedCount, Math.min(
                countAvailableDiplomats(target), getMaxDiplomatsForColonyMission()));
        if (toSend <= 0) {
            return 0;
        }
        int gain = toSend * getDiplomatStabilityGainPerAnt();
        target.setLoyalty(target.getLoyalty() + gain);
        return toSend;
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
        int toSend = Math.min(requestedCount, Math.min(
                countAvailableDiplomats(from), getMaxDiplomatsForDynastyMission()));
        if (toSend <= 0) {
            return 0;
        }
        int gain = toSend * getDiplomatStabilityGainPerAnt();
        dynasty.adjustDiplomaticReputation(other.getId(), gain);
        other.adjustDiplomaticReputation(dynasty.getId(), gain);
        return toSend;
    }
}
