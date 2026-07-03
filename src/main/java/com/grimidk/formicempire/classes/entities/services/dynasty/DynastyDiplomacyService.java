package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.DiplomaticReputationModifier;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class DynastyDiplomacyService {

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

    public boolean canRequestNonAggressionPact(Dynasty other, World world) {
        return canFormNonAggressionPact(other);
    }

    public static double computePactAcceptanceChance(int effectiveReputation) {
        int minRep = GameConstants.REPUTATION_CORDIAL.getMinScore();
        int maxRep = GameConstants.DIPLOMATIC_REPUTATION_MAX;
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
            other.addPendingPactRequest(dynasty.getId());
            return;
        }

        int effectiveRep = other.getDiplomacyService().getEffectiveDiplomaticReputation(dynasty, world);
        if (GameRandom.nextDouble() < computePactAcceptanceChance(effectiveRep)) {
            formNonAggressionPact(other);
        } else {
            other.getDiplomacyService().declineNonAggressionPact(dynasty, world);
        }
    }

    public void acceptNonAggressionPact(Dynasty requester) {
        if (requester == null || requester == dynasty) {
            return;
        }
        removePendingPactRequestIfAny(requester);
        requester.getDiplomacyService().formNonAggressionPact(dynasty);
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
        int maxRep = GameConstants.DIPLOMATIC_REPUTATION_MAX;
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
        if (other == null || other == dynasty || dynasty.isDefeated() || other.isDefeated()) {
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
        if (other == null || other == dynasty || dynasty.isDefeated() || other.isDefeated()) {
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
        if (proposer == null || proposer.isDefeated()) {
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

        int receiveDelta = GameConstants.CROSS_DYNASTY_TRADE_RECEIVER_REP;
        receiver.adjustDiplomaticReputation(sender.getId(), receiveDelta);

        int senderDelta = kind == CrossDynastyTradeProposal.Kind.OFFER
                ? GameConstants.CROSS_DYNASTY_TRADE_OFFER_SENDER_REP
                : GameConstants.DIPLO_MODIFIER_TRADE_REQUEST.getReputationDelta();
        sender.adjustDiplomaticReputation(receiver.getId(), senderDelta);
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
        if (originDynasty.isDefeated() || destinationDynasty.isDefeated()) {
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
