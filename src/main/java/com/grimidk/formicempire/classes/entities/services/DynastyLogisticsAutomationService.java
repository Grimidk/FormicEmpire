package com.grimidk.formicempire.classes.entities.services;

import java.util.HashMap;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.repositories.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class DynastyLogisticsAutomationService {

    private static final double SURPLUS_RATIO = 0.50;
    private static final double DEFICIT_RATIO = 0.25;

    public void runDailyLogistics(Dynasty dynasty, World world, TradeManager tradeManager) {
        if (dynasty.isPlayer()) return;
        if (dynasty.isDefeated()) return;
        if (dynasty.getColonies().size() < 2) return;
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) return;
        if (!dynasty.hasUpgrade(GameUnlocks.ROLE_COURIER)) return;
        if (world == null || tradeManager == null) return;

        dynasty.bindTradeManager(tradeManager);
        DynastyTradeService tradeService = dynasty.getTradeService();
        if (tradeService == null) return;

        for (Colony origin : dynasty.getColonies()) {
            if (origin.getAge() < 7 || !origin.isAutomationEnabled()) continue;

            Hex originHex = world.getHexOfColony(origin);
            if (originHex == null) continue;

            for (Colony neighbor : tradeService.getNeighborColonies(world, origin)) {
                if (neighbor.getDynasty() != dynasty) continue;
                if (neighbor.getAge() < 7) continue;
                if (tradeService.findTrade(origin, neighbor) != null) continue;

                Map<ResourceType, Double> load = computeOutboundLoad(origin, neighbor);
                if (load.isEmpty()) continue;

                Map<AntType, Integer> transport = buildTransport(origin);
                if (transport.isEmpty()) continue;

                Hex targetHex = tradeService.getNeighborHex(world, origin, neighbor);
                if (targetHex == null) continue;

                TradeMethod method = pickMethod(dynasty, originHex, targetHex);
                Trade trade = new Trade(originHex, targetHex, load, null, transport, true, false, method);
                if (trade.startTrip()) {
                    tradeManager.addTrade(trade);
                    origin.logEvent(ColonyLogPrefixes.AUTOMATION + " "
                        + String.format(LanguageStrings.get(LanguageStrings.LOG_AUTOMATION_TRADE_FMT), neighbor.getName()));
                }
                return;
            }
        }
    }

    private Map<AntType, Integer> buildTransport(Colony origin) {
        Map<AntType, Integer> transport = new HashMap<>();

        int couriers = origin.getAssignedRoleCount(GameConstants.ROLE_COURIER);
        if (couriers <= 0) return transport;

        transport.put(GameConstants.TYPE_WORKER, Math.min(couriers, 3));

        int transports = origin.getAssignedRoleCount(GameConstants.ROLE_TRANSPORT);
        if (transports > 0 && origin.hasUpgrade(GameUnlocks.ROLE_TRANSPORT)) {
            transport.put(GameConstants.TYPE_MAJOR, Math.min(transports, 1));
        }

        int escorts = origin.getAssignedRoleCount(GameConstants.ROLE_ESCORT);
        if (escorts > 0 && origin.hasUpgrade(GameUnlocks.ROLE_ESCORT)) {
            transport.put(GameConstants.TYPE_SOLDIER, 1);
        }

        return transport;
    }

    private TradeMethod pickMethod(Dynasty dynasty, Hex originHex, Hex targetHex) {
        Tunnel tunnel = dynasty.getTunnelBetween(originHex, targetHex);
        if (tunnel != null && tunnel.isComplete()) {
            return GameConstants.METHOD_TUNNEL;
        }
        return GameConstants.METHOD_LAND;
    }

    private Map<ResourceType, Double> computeOutboundLoad(Colony origin, Colony destination) {
        Map<ResourceType, Double> load = new HashMap<>();
        ColonyStatsService stats = origin.getStatsService();

        addIfSurplus(load, GameConstants.RESOURCE_FUNGI, origin.getMushroomsPrecise(),
                stats.getMushroomsCapacity(origin), destination.getMushroomsPrecise(),
                stats.getMushroomsCapacity(destination));
        addIfSurplus(load, GameConstants.RESOURCE_WATER, origin.getWaterPrecise(),
                stats.getWaterCapacity(origin), destination.getWaterPrecise(),
                stats.getWaterCapacity(destination));
        addIfSurplus(load, GameConstants.RESOURCE_PLANT, origin.getPlantsPrecise(),
                stats.getPlantsCapacity(origin), destination.getPlantsPrecise(),
                stats.getPlantsCapacity(destination));

        return load;
    }

    private void addIfSurplus(Map<ResourceType, Double> load, ResourceType type,
            double originAmount, int originCap, double destAmount, int destCap) {
        if (originCap <= 0 || destCap <= 0) return;

        double originRatio = originAmount / originCap;
        double destRatio = destAmount / destCap;

        if (originRatio < SURPLUS_RATIO || destRatio > DEFICIT_RATIO) return;

        double surplus = originAmount - (originCap * DEFICIT_RATIO);
        double destRoom = (destCap * SURPLUS_RATIO) - destAmount;
        double amount = Math.min(surplus, destRoom);

        if (amount >= 1.0) {
            load.put(type, Math.floor(amount));
        }
    }
}
