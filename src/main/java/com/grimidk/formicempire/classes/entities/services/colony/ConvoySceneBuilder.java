package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.TradeMethod;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.util.HashMap;
import java.util.Map;

public final class ConvoySceneBuilder {

    private ConvoySceneBuilder() {
    }

    public static ConvoyScene build(World world, Trade trade) {
        if (world == null || trade == null || !isInTransit(trade)) {
            return unavailable(trade);
        }

        Colony originColony = trade.getOrigin().getColony();
        Colony destColony = trade.getDestination().getColony();
        if (originColony == null || destColony == null) {
            return unavailable(trade);
        }

        TradeMethod method = trade.hasPendingUpdate() ? trade.getPendingMethod() : trade.getMethod();
        if (method == null) {
            method = GameConstants.METHOD_LAND;
        }

        boolean returning = trade.isReturning();
        Hex legDestination = returning ? trade.getOrigin() : trade.getDestination();
        Biome landBiome = biomeAtHex(legDestination);
        ConvoyScene.BackgroundKind backgroundKind = resolveBackgroundKind(method);

        Dynasty dynasty = originColony.getDynasty();
        AntSpecies species = dynasty != null ? dynasty.getSpecies() : GameConstants.SPECIES_OMNI;
        Map<AntType, Integer> typeCounts = aggregateAntTypes(trade, method);

        int totalHours = Math.max(1, trade.getTotalHours());
        int remainingHours = Math.max(0, trade.getRemainingHours());
        float legProgress = 1f - (remainingHours / (float) totalHours);

        return new ConvoyScene(
                originColony.getName(),
                destColony.getName(),
                method,
                backgroundKind,
                landBiome,
                species,
                typeCounts,
                returning,
                legProgress,
                remainingHours,
                totalHours,
                true);
    }

    public static boolean isInTransit(Trade trade) {
        if (trade == null || !trade.isActive()) {
            return false;
        }
        if (!trade.getAntsOnTrip().isEmpty()) {
            return true;
        }
        return trade.isReturning() || trade.getRemainingHours() < trade.getTotalHours();
    }

    private static ConvoyScene unavailable(Trade trade) {
        String origin = "";
        String destination = "";
        TradeMethod method = GameConstants.METHOD_LAND;
        if (trade != null) {
            if (trade.getOrigin() != null && trade.getOrigin().getColony() != null) {
                origin = trade.getOrigin().getColony().getName();
            }
            if (trade.getDestination() != null && trade.getDestination().getColony() != null) {
                destination = trade.getDestination().getColony().getName();
            }
            TradeMethod resolved = trade.hasPendingUpdate() ? trade.getPendingMethod() : trade.getMethod();
            if (resolved != null) {
                method = resolved;
            }
        }
        return new ConvoyScene(
                origin,
                destination,
                method,
                ConvoyScene.BackgroundKind.LAND_BIOME,
                GameConstants.BIOME_PLAINS,
                GameConstants.SPECIES_OMNI,
                Map.of(),
                false,
                0f,
                0,
                1,
                false);
    }

    private static ConvoyScene.BackgroundKind resolveBackgroundKind(TradeMethod method) {
        if (method == GameConstants.METHOD_SEA) {
            return ConvoyScene.BackgroundKind.SEA;
        }
        if (method == GameConstants.METHOD_AIR) {
            return ConvoyScene.BackgroundKind.SKY;
        }
        if (method == GameConstants.METHOD_TUNNEL) {
            return ConvoyScene.BackgroundKind.TUNNEL;
        }
        return ConvoyScene.BackgroundKind.LAND_BIOME;
    }

    private static Biome biomeAtHex(Hex hex) {
        if (hex != null && hex.getBiome() != null) {
            return hex.getBiome();
        }
        return GameConstants.BIOME_PLAINS;
    }

    private static Map<AntType, Integer> aggregateAntTypes(Trade trade, TradeMethod method) {
        Map<AntType, Integer> byType = new HashMap<>();
        boolean sky = method == GameConstants.METHOD_AIR;
        for (Ant ant : trade.getAntsOnTrip()) {
            if (ant == null || !ant.isAlive() || ant.getAntType() == null) {
                continue;
            }
            AntType type = ant.getAntType();
            if (!includeConvoyType(type, sky)) {
                continue;
            }
            byType.merge(type, 1, Integer::sum);
        }
        if (byType.isEmpty()) {
            Map<AntType, Integer> transport = trade.getTransport();
            if (transport != null) {
                for (Map.Entry<AntType, Integer> entry : transport.entrySet()) {
                    AntType type = entry.getKey();
                    if (type == null || entry.getValue() == null || entry.getValue() <= 0) {
                        continue;
                    }
                    if (!includeConvoyType(type, sky)) {
                        continue;
                    }
                    byType.put(type, entry.getValue());
                }
            }
        }
        return byType;
    }

    private static boolean includeConvoyType(AntType type, boolean sky) {
        if (type == null || type == GameConstants.TYPE_DEAD) {
            return false;
        }
        if (sky) {
            return type == GameConstants.TYPE_DRONE || type == GameConstants.TYPE_PRINCESS;
        }
        return type != GameConstants.TYPE_DRONE;
    }
}
