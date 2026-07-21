package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.services.colony.ColonyStatsService;
import java.util.HashMap;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

public final class DynastyTradeAutomation {

    private DynastyTradeAutomation() {
    }

    public static Map<AntType, Integer> buildTransport(Colony origin) {
        Map<AntType, Integer> transport = new HashMap<>();

        int couriers = origin.getAssignedRoleCount(GameConstants.ROLE_COURIER);
        if (couriers <= 0) {
            return transport;
        }

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

    public static TradeMethod pickTradeMethod(Dynasty dynasty, Hex originHex, Hex targetHex) {
        Tunnel tunnel = dynasty.getTunnelBetween(originHex, targetHex);
        if (tunnel != null && tunnel.isComplete()) {
            return GameConstants.METHOD_TUNNEL;
        }
        return GameConstants.METHOD_LAND;
    }

    public static Map<ResourceType, Double> computeOutboundLoad(Colony origin, Colony destination) {
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

    private static void addIfSurplus(Map<ResourceType, Double> load, ResourceType type,
            double originAmount, int originCap, double destAmount, int destCap) {
        if (originCap <= 0 || destCap <= 0) {
            return;
        }

        double originRatio = originAmount / originCap;
        double destRatio = destAmount / destCap;

        if (originRatio < GameNumbers.TRADE_AUTOMATION_SURPLUS_RATIO
                || destRatio > GameNumbers.TRADE_AUTOMATION_DEFICIT_RATIO) {
            return;
        }

        double surplus = originAmount - (originCap * GameNumbers.TRADE_AUTOMATION_DEFICIT_RATIO);
        double destRoom = (destCap * GameNumbers.TRADE_AUTOMATION_SURPLUS_RATIO) - destAmount;
        double amount = Math.min(surplus, destRoom);

        if (amount >= 1.0) {
            load.put(type, Math.floor(amount));
        }
    }
}
