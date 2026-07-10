package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.misc.ColonyLoyalty;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.world.WarService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class DynastyRebellionService {

    private DynastyRebellionService() {
    }

    public static void runMonthlyChecks(World world, TradeManager tradeManager) {
        if (world == null) {
            return;
        }
        for (Dynasty dynasty : new ArrayList<>(world.getDynastys())) {
            if (dynasty == null || dynasty.isDefeated() || dynasty.getOriginDynastyId() > 0) {
                continue;
            }
            if (hasBlockingRebellion(world, dynasty)) {
                continue;
            }
            for (Colony colony : new ArrayList<>(dynasty.getColonies())) {
                if (colony == null || colony.isCapital() || colony.getDynasty() != dynasty) {
                    continue;
                }
                int loyalty = colony.getEffectiveLoyalty(tradeManager, world);
                if (GameConstants.getColonyLoyaltyLevel(loyalty) != GameConstants.LOYALTY_REBELLIOUS) {
                    continue;
                }
                double chance = computeMonthlyRebellionChance(loyalty);
                if (GameRandom.nextDouble() < chance) {
                    triggerRebellion(world, tradeManager, dynasty, colony);
                    break;
                }
            }
        }
    }

    public static double computeMonthlyRebellionChance(int effectiveLoyalty) {
        int rebelliousMax = GameConstants.LOYALTY_DISLOYAL.getMinScore() - 1;
        if (effectiveLoyalty > rebelliousMax) {
            return 0.0;
        }
        if (effectiveLoyalty <= 0) {
            return GameConstants.REBELLION_MONTHLY_CHANCE_MAX;
        }
        float t = effectiveLoyalty / (float) rebelliousMax;
        return GameConstants.REBELLION_MONTHLY_CHANCE_MIN
                + (GameConstants.REBELLION_MONTHLY_CHANCE_MAX - GameConstants.REBELLION_MONTHLY_CHANCE_MIN) * (1.0 - t);
    }

    public static boolean isRebellionRisk(Colony colony, TradeManager tradeManager, World world) {
        if (colony == null || colony.isCapital() || world == null) {
            return false;
        }
        int loyalty = colony.getEffectiveLoyalty(tradeManager, world);
        return GameConstants.getColonyLoyaltyLevel(loyalty) == GameConstants.LOYALTY_REBELLIOUS;
    }

    public static boolean canColonyJoinRebellion(Colony colony, TradeManager tradeManager, World world) {
        if (colony == null || colony.isCapital() || world == null) {
            return false;
        }
        int loyalty = colony.getEffectiveLoyalty(tradeManager, world);
        ColonyLoyalty level = GameConstants.getColonyLoyaltyLevel(loyalty);
        return level == GameConstants.LOYALTY_REBELLIOUS || level == GameConstants.LOYALTY_DISLOYAL;
    }

    public static double computeJoinChance(Colony seed, Colony candidate, TradeManager tradeManager, World world) {
        if (seed == null || candidate == null || world == null) {
            return 0.0;
        }
        int distance = Math.max(1, world.colonyHexDistance(seed, candidate));
        float distanceFactor = 1f / distance;
        int loyalty = candidate.getEffectiveLoyalty(tradeManager, world);
        float loyaltyFactor = Math.max(0f, (GameConstants.LOYALTY_COMPLACENT.getMinScore() - 1 - loyalty)
                / (float) (GameConstants.LOYALTY_COMPLACENT.getMinScore() - 1));
        return Math.min(0.95, GameConstants.REBELLION_JOIN_BASE_CHANCE
                + GameConstants.REBELLION_JOIN_DISTANCE_WEIGHT * distanceFactor
                + GameConstants.REBELLION_JOIN_LOYALTY_WEIGHT * loyaltyFactor);
    }

    public static boolean hasBlockingRebellion(World world, Dynasty parent) {
        if (parent == null || world == null) {
            return false;
        }
        if (parent.getPendingRebellionResponseFromId() > 0) {
            return true;
        }
        int rebelId = parent.getActiveRebellionDynastyId();
        if (rebelId <= 0) {
            return false;
        }
        Dynasty rebel = world.findDynastyById(rebelId);
        return rebel != null && !rebel.isDefeated();
    }

    public static void triggerRebellion(World world, TradeManager tradeManager, Dynasty parent, Colony seed) {
        if (world == null || parent == null || seed == null || hasBlockingRebellion(world, parent)) {
            return;
        }
        Set<Colony> rebels = expandRebellion(seed, parent, tradeManager, world);
        if (rebels.isEmpty()) {
            return;
        }
        Dynasty rebellion = formRebellionDynasty(world, parent, rebels, seed, tradeManager);
        if (rebellion == null) {
            return;
        }
        parent.setActiveRebellionDynastyId(rebellion.getId());
        resolveParentResponse(world, tradeManager, parent, rebellion);
    }

    static Set<Colony> expandRebellion(Colony seed, Dynasty parent, TradeManager tradeManager, World world) {
        Set<Colony> joined = new LinkedHashSet<>();
        joined.add(seed);
        Queue<Colony> frontier = new ArrayDeque<>();
        frontier.add(seed);
        DynastyTradeService tradeService = parent.getTradeService();
        if (tradeService == null) {
            tradeService = new DynastyTradeService(parent, tradeManager);
        }

        while (!frontier.isEmpty()) {
            Colony current = frontier.poll();
            List<Colony> neighbors = new ArrayList<>(tradeService.getNeighborColonies(world, current));
            neighbors.sort((a, b) -> {
                int distA = world.colonyHexDistance(seed, a);
                int distB = world.colonyHexDistance(seed, b);
                if (distA != distB) {
                    return Integer.compare(distA, distB);
                }
                return Integer.compare(a.getId(), b.getId());
            });

            for (Colony neighbor : neighbors) {
                if (neighbor == null || neighbor.getDynasty() != parent || joined.contains(neighbor)) {
                    continue;
                }
                if (!canColonyJoinRebellion(neighbor, tradeManager, world)) {
                    continue;
                }
                double chance = computeJoinChance(seed, neighbor, tradeManager, world);
                if (GameRandom.nextDouble() < chance) {
                    joined.add(neighbor);
                    frontier.add(neighbor);
                } else {
                    return joined;
                }
            }
        }
        return joined;
    }

    private static Dynasty formRebellionDynasty(World world, Dynasty parent, Set<Colony> rebels, Colony seed,
            TradeManager tradeManager) {
        String theme = LanguageStrings.dynastyThemeBase(parent.getName(), parent.getTitleKey());
        String rebelName = LanguageStrings.format(LanguageStrings.DYNASTY_REBELLION_NAME_FMT, theme);
        int rebelId = world.allocateDynastyId();
        Dynasty rebellion = new Dynasty(rebelId, rebelName, parent.getTitleKey(), false, parent.getSpecies());
        rebellion.setOriginDynastyId(parent.getId());
        rebellion.inheritProgressFrom(parent);
        rebellion.getStarterService().initializeDynasty(rebellion);
        world.getDynastys().add(rebellion);

        Set<Integer> rebelIds = new HashSet<>();
        for (Colony colony : rebels) {
            rebelIds.add(colony.getId());
        }

        for (Colony colony : rebels) {
            parent.removeColony(colony);
            rebellion.addColony(colony);
            colony.setCapital(false);
            colony.logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + LanguageStrings.format(LanguageStrings.LOG_REBELLION_COLONY_JOINED_FMT, rebelName));
        }
        rebellion.setCapital(seed);

        splitTunnels(parent, rebellion, rebelIds, world);
        splitTrades(parent, rebellion, rebelIds, tradeManager);
        recallDiplomatsTargetingColonies(world, parent, rebelIds);
        cancelCrossDynastyLinks(parent, rebellion, tradeManager);

        ColonyMilitaryService.refreshDynastyMilitaryPower(parent);
        ColonyMilitaryService.refreshDynastyMilitaryPower(rebellion);
        for (Colony colony : rebellion.getColonies()) {
            ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        }

        Colony logColony = parent.getCapital() != null ? parent.getCapital() : seed;
        if (logColony != null) {
            logColony.logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + LanguageStrings.format(LanguageStrings.LOG_REBELLION_FORMED_FMT, rebelName));
        }
        return rebellion;
    }

    private static void splitTunnels(Dynasty parent, Dynasty rebellion, Set<Integer> rebelColonyIds, World world) {
        List<Tunnel> toRebellion = new ArrayList<>();
        List<Tunnel> toRemove = new ArrayList<>();
        for (Tunnel tunnel : new ArrayList<>(parent.getTunnels())) {
            Colony colonyA = hexColony(tunnel.getHexA());
            Colony colonyB = hexColony(tunnel.getHexB());
            boolean aRebel = colonyA != null && rebelColonyIds.contains(colonyA.getId());
            boolean bRebel = colonyB != null && rebelColonyIds.contains(colonyB.getId());
            if (aRebel && bRebel) {
                toRebellion.add(tunnel);
            } else if (aRebel != bRebel) {
                toRemove.add(tunnel);
            }
        }
        for (Tunnel tunnel : toRebellion) {
            parent.getTunnels().remove(tunnel);
            rebellion.addTunnel(tunnel);
        }
        parent.getTunnels().removeAll(toRemove);
    }

    private static Colony hexColony(Hex hex) {
        return hex != null ? hex.getColony() : null;
    }

    private static void splitTrades(Dynasty parent, Dynasty rebellion, Set<Integer> rebelColonyIds, TradeManager tradeManager) {
        if (tradeManager == null) {
            return;
        }
        List<Trade> toCancel = new ArrayList<>();
        for (Trade trade : tradeManager.getActiveTrades()) {
            Colony origin = trade.getOrigin() != null ? trade.getOrigin().getColony() : null;
            Colony destination = trade.getDestination() != null ? trade.getDestination().getColony() : null;
            if (origin == null || destination == null) {
                continue;
            }
            boolean originRebel = rebelColonyIds.contains(origin.getId());
            boolean destRebel = rebelColonyIds.contains(destination.getId());
            if (originRebel != destRebel) {
                toCancel.add(trade);
            }
        }
        for (Trade trade : toCancel) {
            trade.cancel();
            tradeManager.removeTrade(trade);
        }
    }

    private static void cancelCrossDynastyLinks(Dynasty parent, Dynasty rebellion, TradeManager tradeManager) {
        if (tradeManager == null) {
            return;
        }
        DynastyDiplomacyService parentDiplo = parent.getDiplomacyService();
        DynastyDiplomacyService rebelDiplo = rebellion.getDiplomacyService();
        if (parentDiplo != null) {
            parentDiplo.cancelCrossDynastyTradesWith(rebellion, tradeManager);
        }
        if (rebelDiplo != null) {
            rebelDiplo.cancelCrossDynastyTradesWith(parent, tradeManager);
        }
        parent.removePendingTradeProposalsFrom(rebellion.getId());
        rebellion.removePendingTradeProposalsFrom(parent.getId());
    }

    private static void recallDiplomatsTargetingColonies(World world, Dynasty parent, Set<Integer> rebelColonyIds) {
        for (Colony source : parent.getColonies()) {
            List<Map.Entry<Integer, Integer>> missions = new ArrayList<>(source.getOutgoingColonyDiplomatMissions().entrySet());
            for (Map.Entry<Integer, Integer> entry : missions) {
                if (!rebelColonyIds.contains(entry.getKey()) || entry.getValue() <= 0) {
                    continue;
                }
                recallColonyDiplomats(world, source, entry.getKey(), entry.getValue());
            }
        }
    }

    private static void recallColonyDiplomats(World world, Colony from, int targetColonyId, int count) {
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
        Colony target = findColonyById(world, targetColonyId);
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

    private static Colony findColonyById(World world, int colonyId) {
        if (world == null || world.getHexes() == null) {
            return null;
        }
        for (Hex hex : world.getHexes()) {
            Colony colony = hex.getColony();
            if (colony != null && colony.getId() == colonyId) {
                return colony;
            }
        }
        return null;
    }

    private static void resolveParentResponse(World world, TradeManager tradeManager, Dynasty parent, Dynasty rebellion) {
        if (parent.isPlayer()) {
            parent.setPendingRebellionResponseFromId(rebellion.getId());
            return;
        }
        if (npcChoosesFight(parent, rebellion)) {
            beginRebellionWar(world, tradeManager, parent, rebellion);
        } else {
            grantIndependence(world, parent, rebellion);
        }
    }

    public static boolean npcChoosesFight(Dynasty parent, Dynasty rebellion) {
        if (parent == null || rebellion == null) {
            return true;
        }
        int parentPower = parent.getMilitaryPower();
        int rebelPower = rebellion.getMilitaryPower();
        if (parentPower <= 0 && rebelPower <= 0) {
            return GameRandom.nextBoolean();
        }
        double ratio = parentPower / (double) Math.max(1, rebelPower);
        double fightChance = GameConstants.REBELLION_NPC_FIGHT_CHANCE_MIN
                + (GameConstants.REBELLION_NPC_FIGHT_CHANCE_MAX - GameConstants.REBELLION_NPC_FIGHT_CHANCE_MIN)
                * clamp01((ratio - GameConstants.REBELLION_NPC_FIGHT_RATIO_LOW)
                        / (GameConstants.REBELLION_NPC_FIGHT_RATIO_HIGH - GameConstants.REBELLION_NPC_FIGHT_RATIO_LOW));
        return GameRandom.nextDouble() < fightChance;
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public static void respondToRebellion(World world, TradeManager tradeManager, Dynasty parent,
            Dynasty rebellion, boolean fight) {
        if (world == null || parent == null || rebellion == null) {
            return;
        }
        parent.clearPendingRebellionResponse();
        if (fight) {
            beginRebellionWar(world, tradeManager, parent, rebellion);
        } else {
            grantIndependence(world, parent, rebellion);
        }
    }

    public static void grantIndependence(World world, Dynasty parent, Dynasty rebellion) {
        if (parent == null || rebellion == null) {
            return;
        }
        DynastyDiplomacyService parentDiplo = parent.getDiplomacyService();
        if (parentDiplo != null) {
            parentDiplo.applyNonAggressionPact(rebellion);
        }
        rebellion.adjustDiplomaticReputation(parent.getId(),
                GameConstants.DIPLO_MODIFIER_GRANTED_INDEPENDENCE.getReputationDelta());
        rebellion.addDiplomaticModifierKey(parent.getId(),
                GameConstants.DIPLO_MODIFIER_GRANTED_INDEPENDENCE.getNameKey());
        parent.setActiveRebellionDynastyId(0);

        Colony logColony = parent.getCapital();
        if (logColony != null) {
            logColony.logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + LanguageStrings.format(LanguageStrings.LOG_REBELLION_INDEPENDENCE_FMT, rebellion.getName()));
        }
    }

    public static void beginRebellionWar(World world, TradeManager tradeManager, Dynasty parent, Dynasty rebellion) {
        if (world == null || parent == null || rebellion == null) {
            return;
        }
        WarService warService = world.getWarService();
        if (warService == null) {
            return;
        }
        warService.beginRebellionWar(parent, rebellion);
        DynastyDiplomacyService parentDiplo = parent.getDiplomacyService();
        if (parentDiplo != null) {
            parentDiplo.applyWar(rebellion, tradeManager, world);
        }
    }

    public static void onRebellionWarConcluded(World world, War war, int winnerDynastyId) {
        if (world == null || war == null || !war.isRebellionWar() || winnerDynastyId <= 0) {
            return;
        }
        Dynasty rebellion = null;
        Dynasty parent = null;
        for (int dynastyId : new int[] { war.getDynastyIdA(), war.getDynastyIdB() }) {
            Dynasty candidate = world.findDynastyById(dynastyId);
            if (candidate != null && candidate.getOriginDynastyId() > 0) {
                rebellion = candidate;
                parent = world.findDynastyById(candidate.getOriginDynastyId());
                break;
            }
        }
        if (parent == null || rebellion == null) {
            return;
        }
        parent.setActiveRebellionDynastyId(0);
        if (winnerDynastyId == parent.getId()) {
            rebellion.setDefeated(true);
        } else if (winnerDynastyId == rebellion.getId()) {
            parent.setDefeated(true);
        }
    }

    public static String generateRebellionWarName(World world, Dynasty parent) {
        int ordinal = countRebellionWars(world, parent.getId()) + 1;
        String theme = LanguageStrings.dynastyThemeBase(parent.getName(), parent.getTitleKey());
        return LanguageStrings.format(LanguageStrings.REBELLION_WAR_NAME_FMT,
                LanguageStrings.getWarOrdinal(ordinal), theme);
    }

    private static int countRebellionWars(World world, int parentDynastyId) {
        if (world == null || world.getWarService() == null) {
            return 0;
        }
        int count = 0;
        WarService warService = world.getWarService();
        for (War war : warService.getActiveWars()) {
            if (war.isRebellionWar() && war.involves(parentDynastyId)) {
                count++;
            }
        }
        for (War war : warService.getHistoricWars()) {
            if (war.isRebellionWar() && war.involves(parentDynastyId)) {
                count++;
            }
        }
        return count;
    }
}
