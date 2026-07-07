package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.services.colony.ColonyStatsService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DynastyAiService {

    private static final int LOYALTY_MISSION_THRESHOLD = GameConstants.REPUTATION_CORDIAL.getMinScore();
    private static final int REPUTATION_MISSION_TARGET = GameConstants.REPUTATION_FRIENDLY.getMinScore();

    public void runDailyAi(Dynasty dynasty, World world, TradeManager tradeManager) {
        if (dynasty == null || dynasty.isPlayer() || dynasty.isDefeated()) {
            return;
        }
        if (world == null) {
            return;
        }

        ensureDiplomatStaff(dynasty);
        runDiplomaticMissions(dynasty, world, tradeManager);
        runWarConsideration(dynasty, world, tradeManager);
        runCrossDynastyTrade(dynasty, world, tradeManager);
        runAbilities(dynasty, world);
    }

    private void ensureDiplomatStaff(Dynasty dynasty) {
        if (!dynasty.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
            return;
        }
        Colony capital = dynasty.getCapital();
        for (Colony colony : dynasty.getColonies()) {
            if (colony.getAge() < 7 || colony.getPrincesses().isEmpty()) {
                continue;
            }
            int desired = colony == capital ? Math.min(2, colony.getPrincesses().size()) : 1;
            if (colony.getAssignedRoleCount(GameConstants.ROLE_DIPLOMAT) < desired) {
                colony.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, desired);
            }
        }
    }

    private void runDiplomaticMissions(Dynasty dynasty, World world, TradeManager tradeManager) {
        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        if (diplo == null) {
            return;
        }

        for (Colony colony : dynasty.getColonies()) {
            if (colony.getAge() < 7) {
                continue;
            }
            int loyalty = colony.getEffectiveLoyalty(tradeManager, world);
            if (loyalty >= LOYALTY_MISSION_THRESHOLD) {
                continue;
            }
            if (!diplo.canSendDiplomatsToColony(colony, tradeManager, world)) {
                continue;
            }
            int max = Math.min(
                    diplo.countAvailableDiplomats(colony),
                    diplo.getMaxDiplomatsForColonyMission());
            int sent = diplo.sendDiplomatsToColony(colony, max, tradeManager, world);
            if (sent > 0) {
                int gain = sent * diplo.getDiplomatStabilityGainPerAnt();
                colony.logEvent(ColonyLogPrefixes.DYNASTY + " "
                        + LanguageStrings.format(LanguageStrings.LOG_AI_DIPLOMAT_COLONY_FMT,
                                sent, colony.getName(), gain));
            }
        }

        List<Dynasty> others = new ArrayList<>(world.getDynastys());
        others.remove(dynasty);
        others.sort(Comparator
                .comparingInt((Dynasty other) -> diplo.sharesBorderWith(other, world) ? 0 : 1)
                .thenComparingInt(other -> diplo.getEffectiveDiplomaticReputation(other, world)));

        for (Dynasty other : others) {
            if (!other.isActiveForDiplomacy()) {
                continue;
            }
            if (diplo.canRequestNonAggressionPact(other, world)) {
                diplo.requestNonAggressionPact(other, world);
                if (diplo.hasNonAggressionPact(other)) {
                    logDynastyEvent(dynasty, LanguageStrings.format(LanguageStrings.LOG_AI_PACT_FMT, other.getName()));
                }
                continue;
            }
            int effectiveRep = diplo.getEffectiveDiplomaticReputation(other, world);
            if (effectiveRep >= REPUTATION_MISSION_TARGET) {
                continue;
            }
            Colony from = pickDiplomatSourceColony(dynasty, diplo);
            if (from == null || !diplo.canSendDiplomatsToDynasty(from, other, world)) {
                continue;
            }
            int max = Math.min(
                    diplo.countAvailableDiplomats(from),
                    diplo.getMaxDiplomatsForDynastyMission());
            int sent = diplo.sendDiplomatsToDynasty(from, other, max, world);
            if (sent > 0) {
                int gain = sent * diplo.getDiplomatStabilityGainPerAnt();
                logDynastyEvent(dynasty, LanguageStrings.format(LanguageStrings.LOG_AI_DIPLOMAT_DYNASTY_FMT,
                        sent, other.getName(), gain));
            }
        }
    }

    private void runWarConsideration(Dynasty dynasty, World world, TradeManager tradeManager) {
        if (dynasty.isAtWar()) {
            return;
        }
        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        if (diplo == null || hasColonizableExpansionSpace(dynasty, world)) {
            return;
        }

        List<Dynasty> candidates = new ArrayList<>();
        for (Dynasty other : world.getDynastys()) {
            if (other == dynasty || !other.isActiveForDiplomacy()) {
                continue;
            }
            if (!diplo.sharesBorderWith(other, world)) {
                continue;
            }
            if (!diplo.canDeclareWar(other, world)) {
                continue;
            }
            candidates.add(other);
        }
        if (candidates.isEmpty()) {
            return;
        }

        Dynasty target = null;
        double bestChance = 0;
        for (Dynasty other : candidates) {
            double chance = ColonyMilitaryService.computeAiWarDeclarationChance(
                    dynasty.getMilitaryPower(),
                    other.getMilitaryPower(),
                    diplo.getEffectiveDiplomaticReputation(other, world));
            if (chance > bestChance) {
                bestChance = chance;
                target = other;
            }
        }
        if (target == null || bestChance <= 0 || GameRandom.nextDouble() >= bestChance) {
            return;
        }

        diplo.declareWar(target, world, tradeManager);
        logDynastyEvent(dynasty, LanguageStrings.format(LanguageStrings.LOG_AI_DECLARE_WAR_FMT, target.getName()));
    }

    private boolean hasColonizableExpansionSpace(Dynasty dynasty, World world) {
        for (Colony colony : dynasty.getColonies()) {
            Hex hex = world.getHexOfColony(colony);
            if (hex == null) {
                continue;
            }
            for (Hex neighbor : hex.getAdjacentNeighbors()) {
                if (neighbor == null) {
                    continue;
                }
                if (neighbor.getBiome() == GameConstants.BIOME_OCEAN
                        || neighbor.getBiome() == GameConstants.BIOME_LAKE) {
                    continue;
                }
                Colony existing = neighbor.getColony();
                if (existing == null) {
                    return true;
                }
                if (existing.getAntTotal() == 0 && existing.getAge() >= 7) {
                    return true;
                }
            }
        }
        return false;
    }

    private void runCrossDynastyTrade(Dynasty dynasty, World world, TradeManager tradeManager) {
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE) || !dynasty.hasUpgrade(GameUnlocks.ROLE_COURIER)) {
            return;
        }
        if (world == null || tradeManager == null) {
            return;
        }

        dynasty.bindTradeManager(tradeManager);
        DynastyTradeService tradeService = dynasty.getTradeService();
        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        if (tradeService == null || diplo == null) {
            return;
        }

        diplo.processIncomingTradeProposals(world, tradeManager);

        for (Colony localColony : dynasty.getColonies()) {
            if (localColony.getAge() < 7 || !localColony.isAutomationEnabled()) {
                continue;
            }
            Hex localHex = world.getHexOfColony(localColony);
            if (localHex == null) {
                continue;
            }

            for (Colony neighbor : tradeService.getNeighborColonies(world, localColony)) {
                Dynasty otherDynasty = neighbor.getDynasty();
                if (otherDynasty == null || otherDynasty == dynasty || !DynastyDiplomacyService.isDiplomaticallyContactable(otherDynasty)) {
                    continue;
                }
                if (tradeService.findTrade(localColony, neighbor) != null
                        || tradeService.findTrade(neighbor, localColony) != null) {
                    continue;
                }
                if (!diplo.canParticipateInCrossDynastyTrade(otherDynasty, localColony, world)) {
                    continue;
                }

                Map<ResourceType, Double> offerLoad = DynastyTradeAutomation.computeOutboundLoad(localColony, neighbor);
                if (!offerLoad.isEmpty()
                        && diplo.canNpcProposeTradeOffer(otherDynasty, world)
                        && diplo.proposeCrossDynastyTrade(
                                localColony, neighbor, CrossDynastyTradeProposal.Kind.OFFER, world, tradeManager)
                                != DynastyDiplomacyService.TradeProposalResult.FAILED) {
                    return;
                }

                Map<ResourceType, Double> requestLoad = DynastyTradeAutomation.computeOutboundLoad(neighbor, localColony);
                if (!requestLoad.isEmpty()
                        && diplo.canNpcProposeTradeRequest(otherDynasty, world)
                        && diplo.proposeCrossDynastyTrade(
                                localColony, neighbor, CrossDynastyTradeProposal.Kind.REQUEST, world, tradeManager)
                                != DynastyDiplomacyService.TradeProposalResult.FAILED) {
                    return;
                }
            }
        }
    }

    private Colony pickDiplomatSourceColony(Dynasty dynasty, DynastyDiplomacyService diplo) {
        Colony capital = dynasty.getCapital();
        if (capital != null && diplo.countAvailableDiplomats(capital) > 0) {
            return capital;
        }
        return dynasty.getColonies().stream()
                .filter(c -> diplo.countAvailableDiplomats(c) > 0)
                .max(Comparator.comparingInt(c -> diplo.countAvailableDiplomats(c)))
                .orElse(null);
    }

    private void runAbilities(Dynasty dynasty, World world) {
        int colonyCount = dynasty.getColonies().size();
        boolean expanding = colonyCount < GameConstants.AI_EXPANSION_COLONY_TARGET;

        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT) && expanding) {
            int massCost = dynasty.getMassNuptialFlightCost();
            if (dynasty.getResearchPoints() >= massCost * 2L && colonyCount >= 3) {
                dynasty.runMassNuptialFlight(world);
                return;
            }
        }

        Colony capital = dynasty.getCapital();
        if (capital == null || capital.getAge() < 7) {
            return;
        }

        Hex capitalHex = world.getHexOfColony(capital);
        if (capitalHex == null) {
            return;
        }

        runCreatineDietIfNeeded(dynasty);

        if (capital.hasUpgrade(GameUnlocks.ABILITY_PHEROMONE_STORM)
                && !capital.isPheromoneStormActive()
                && capital.getEffectiveLoyalty(null, world) < LOYALTY_MISSION_THRESHOLD) {
            capital.activatePheromoneStorm();
        }

        if (expanding
                && capital.hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT)
                && dynasty.getForcedFlightCooldownDays() <= 0
                && capital.getResearchPoints() >= capital.getNuptialFlightCost() * 2L) {
            int costBefore = capital.getResearchPoints();
            capital.forceNuptialFlight(world, capitalHex);
            if (capital.getResearchPoints() < costBefore) {
                dynasty.setForcedFlightCooldownDays(GameConstants.AI_FORCED_FLIGHT_COOLDOWN_DAYS);
            }
        }
    }

    private void runCreatineDietIfNeeded(Dynasty dynasty) {
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_CREATINE_DIET)) {
            return;
        }
        int proteinCost = GameConstants.CREATINE_DIET_PROTEIN_COST;
        for (Colony colony : dynasty.getColonies()) {
            if (colony.getAge() < 7 || colony.isCreatineDietActive()) {
                continue;
            }
            if (colony.getProtein() < proteinCost + 50) {
                continue;
            }
            if (!isUnderFoodStress(colony)) {
                continue;
            }
            if (colony.activateCreatineDiet()) {
                return;
            }
        }
    }

    private boolean isUnderFoodStress(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        int mushroomCap = stats.getMushroomsCapacity(colony);
        if (mushroomCap <= 0) {
            return false;
        }
        return colony.getMushroomsPrecise() / mushroomCap < GameConstants.AI_CREATINE_FOOD_STRESS_RATIO;
    }

    private static void logDynastyEvent(Dynasty dynasty, String message) {
        Colony logColony = dynasty.getCapital();
        if (logColony == null && !dynasty.getColonies().isEmpty()) {
            logColony = dynasty.getColonies().get(0);
        }
        if (logColony != null) {
            logColony.logEvent(ColonyLogPrefixes.DYNASTY + " " + message);
        }
    }
}
