package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyConvoyTransitService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyResourceService;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class Trade {

    private final Hex origin;
    private final Hex destination;
    private final Map<ResourceType, Double> load;
    private final Map<ResourceType, Double> returnLoad;
    private final Map<AntType, Integer> transport;
    private final List<Ant> antsOnTrip;
    private boolean isRecurrent;
    private boolean isBilateral;
    private TradeMethod method;
    private boolean isActive;
    
    private int totalHours;
    private int remainingHours;
    private boolean isReturning;

    private double tunnelBearingRadians;

    private Map<ResourceType, Double> pendingLoad;
    private Map<ResourceType, Double> pendingReturnLoad;
    private Map<AntType, Integer> pendingTransport;
    private Boolean pendingRecurrent;
    private Boolean pendingIsBilateral;
    private TradeMethod pendingMethod;
    private boolean hasPendingUpdate;

    public Trade(Hex origin, Hex destination, Map<ResourceType, Double> load, Map<ResourceType, Double> returnLoad, Map<AntType, Integer> transport, boolean isRecurrent, boolean isBilateral, TradeMethod method) {
        this.origin = origin;
        this.destination = destination;
        this.load = new HashMap<>(load);
        this.returnLoad = returnLoad != null ? new HashMap<>(returnLoad) : new HashMap<>();
        this.transport = new HashMap<>(transport);
        this.antsOnTrip = new ArrayList<>();
        this.isRecurrent = isRecurrent;
        this.isBilateral = isBilateral;
        this.method = method;
        this.isActive = true;
        this.isReturning = false;
        this.hasPendingUpdate = false;
        calculateHours();
    }

    private void calculateHours() {
        float baseHours = 168f;

        float methodSpeed = method.getSpeedMult();
        
        float workerSpeed = GameConstants.TYPE_WORKER.getSpeedMult();
        float totalAntSpeed = 0;
        int totalAnts = 0;
        
        for (Map.Entry<AntType, Integer> entry : transport.entrySet()) {
            totalAntSpeed += entry.getKey().getSpeedMult() * entry.getValue();
            totalAnts += entry.getValue();
        }
        
        float avgAntSpeed = (totalAnts > 0) ? (totalAntSpeed / totalAnts) : workerSpeed;
        float speedFactor = methodSpeed * (avgAntSpeed / workerSpeed);
        
        Colony originColony = origin.getColony();
        if (originColony != null && originColony.getDynasty() != null) {
            Tunnel tunnel = originColony.getDynasty().getTunnelBetween(origin, destination);
            if (tunnel != null && tunnel.isComplete()) {
                speedFactor *= 1.5f;
            }
        }

        this.totalHours = Math.max(1, Math.round(baseHours / speedFactor));
        this.remainingHours = this.totalHours;
    }

    public boolean startTrip() {
        Colony originColony = origin.getColony();
        if (originColony == null) {
            isActive = false;
            return false;
        }

        if (antsOnTrip.isEmpty()) {
            for (Map.Entry<AntType, Integer> entry : transport.entrySet()) {
                List<Ant> colonyAnts = originColony.getAntsByType(entry.getKey());
                long availableCount = colonyAnts.stream().filter(a -> !a.isOnTrade() && a.isAlive()).count();
                if (availableCount < entry.getValue()) {
                    originColony.logEvent(ColonyLogPrefixes.TRADE + " "
                        + String.format(LanguageStrings.get(LanguageStrings.LOG_TRADE_CANCELLED_FMT),
                            entry.getKey().getName()));
                    isActive = false;
                    return false;
                }
            }

            for (Map.Entry<AntType, Integer> entry : transport.entrySet()) {
                List<Ant> colonyAnts = originColony.getAntsByType(entry.getKey());
                int needed = entry.getValue();
                int found = 0;
                for (Ant ant : colonyAnts) {
                    if (found >= needed) break;
                    if (!ant.isOnTrade() && ant.isAlive()) {
                        ant.setOnTrade(true);
                        antsOnTrip.add(ant);
                        found++;
                    }
                }
            }
        }

        ColonyResourceService resService = originColony.getResourceService();
        for (Map.Entry<ResourceType, Double> entry : load.entrySet()) {
            resService.consumeResource(originColony, entry.getKey(), entry.getValue());
        }

        originColony.getConvoyTransitService().beginConvoyTransit(originColony, this);
        
        this.isReturning = false;
        this.remainingHours = this.totalHours;
        return true;
    }

    public void tick() {
        if (!isActive) return;

        if (!hasSufficientLiveEscorts()) {
            cancelDueToEscortLoss();
            return;
        }

        remainingHours--;
        if (remainingHours <= 0) {
            if (!isReturning) {
                deliverLoad();
                if (isBilateral) {
                    pickupReturnLoad();
                }
                isReturning = true;
                remainingHours = totalHours;
                Colony originColony = origin.getColony();
                if (originColony != null) {
                    originColony.getConvoyTransitService().onReturnLegStarted(originColony, this);
                }
            } else {
                if (isBilateral) {
                    deliverReturnLoad();
                }

                if (hasPendingUpdate) {
                    releaseAnts();
                    applyPendingUpdate();
                } else {
                    completeReturn();
                }

                if (isRecurrent) {
                    if (!startTrip()) {
                        Colony originColony = origin.getColony();
                        if (originColony != null) {
                            Colony destColony = destination.getColony();
                            String destName = destColony != null ? destColony.getName() : "?";
                            originColony.logEvent(ColonyLogPrefixes.TRADE + " "
                                    + LanguageStrings.format(LanguageStrings.LOG_TRADE_ESCORT_LOSS_FMT, destName));
                        }
                    }
                } else {
                    isActive = false;
                }
            }
        }
    }

    private void deliverLoad() {
        Colony dest = destination.getColony();
        if (dest != null) {
            Map<ResourceType, Double> securedLoad = calculateSecuredLoad(load);
            for (Map.Entry<ResourceType, Double> entry : securedLoad.entrySet()) {
                dest.getResourceService().addResource(dest, entry.getKey(), entry.getValue());
            }
            if (origin.getColony() != null) {
                origin.getColony().logEvent(ColonyLogPrefixes.TRADE + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_TRADE_ARRIVED_FMT), dest.getName()));
            }
        }
    }

    private void pickupReturnLoad() {
        Colony destColony = destination.getColony();
        if (destColony != null && !returnLoad.isEmpty()) {
            ColonyResourceService resService = destColony.getResourceService();
            for (Map.Entry<ResourceType, Double> entry : returnLoad.entrySet()) {
                resService.consumeResource(destColony, entry.getKey(), entry.getValue());
            }
            destColony.logEvent(ColonyLogPrefixes.TRADE + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_TRADE_CONVOY_RETURN_FMT),
                    origin.getColony().getName()));
        }
    }

    private void deliverReturnLoad() {
        Colony originColony = origin.getColony();
        if (originColony != null) {
            Map<ResourceType, Double> securedLoad = calculateSecuredLoad(returnLoad);
            for (Map.Entry<ResourceType, Double> entry : securedLoad.entrySet()) {
                originColony.getResourceService().addResource(originColony, entry.getKey(), entry.getValue());
            }
            originColony.logEvent(ColonyLogPrefixes.TRADE + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_TRADE_BILATERAL_RETURN_FMT), securedLoad.size()));
        }
    }

    private Map<ResourceType, Double> calculateSecuredLoad(Map<ResourceType, Double> cargo) {
        Colony originColony = origin.getColony();
        Map<ResourceType, Double> securedLoad = new HashMap<>(cargo);
        if (originColony == null) return securedLoad;
        
        double baseSec = originColony.getStatsService().getBaseTradeSecurity(originColony);
        double totalSec = 0;
        
        for (Map.Entry<AntType, Integer> entry : transport.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) continue;

            AntType type = entry.getKey();
            if (type == GameConstants.TYPE_WORKER) totalSec += count * baseSec * 1.0;
            else if (type == GameConstants.TYPE_MAJOR) totalSec += count * baseSec * 2.5;
            else if (type == GameConstants.TYPE_SOLDIER) totalSec += count * baseSec * 10.0;
            else if (type == GameConstants.TYPE_PRINCESS) totalSec += count * baseSec * 1.0;
        }

        double dangerFactor = method.getDangerFactor();
        double mitigationPercent = 100.0;
        if (dangerFactor > 0) {
            mitigationPercent = Math.min(100.0, (totalSec / (10.0 + dangerFactor * 50.0)) * 100.0);
        }
        
        double securityFactor = mitigationPercent / 100.0;
        
        for (Map.Entry<ResourceType, Double> entry : securedLoad.entrySet()) {
            entry.setValue(entry.getValue() * securityFactor);
        }
        return securedLoad;
    }

    private void completeReturn() {
        if (!isRecurrent) {
            releaseAnts();
        }
    }

    private void releaseAnts() {
        Colony originColony = origin.getColony();
        if (originColony != null) {
            originColony.getConvoyTransitService().restoreConvoyAnts(originColony, this);
        }
        for (Ant ant : antsOnTrip) {
            ant.setOnTrade(false);
        }
        antsOnTrip.clear();
    }

    public void cancel() {
        if (isActive) {
            if (!isReturning) {
                Colony originColony = origin.getColony();
                if (originColony != null) {
                    for (Map.Entry<ResourceType, Double> entry : load.entrySet()) {
                        originColony.getResourceService().addResource(originColony, entry.getKey(), entry.getValue());
                    }
                    Colony destColony = destination.getColony();
                    originColony.logEvent(ColonyLogPrefixes.TRADE + " "
                        + String.format(LanguageStrings.get(LanguageStrings.LOG_TRADE_ROUTE_CANCELLED_FMT),
                            destColony != null ? destColony.getName() : "?"));
                }
            }
            releaseAnts();
            isActive = false;
        }
    }

    public void purgeDeadEscorts() {
        antsOnTrip.removeIf(ant -> {
            if (ant == null || !ant.isAlive()) {
                if (ant != null) {
                    ant.setOnTrade(false);
                }
                return true;
            }
            return false;
        });
    }

    public boolean hasSufficientLiveEscorts() {
        purgeDeadEscorts();
        if (transport.isEmpty()) {
            return true;
        }
        Map<AntType, Integer> liveCounts = new HashMap<>();
        for (Ant ant : antsOnTrip) {
            if (ant != null && ant.isAlive()) {
                liveCounts.merge(ant.getAntType(), 1, Integer::sum);
            }
        }
        if (antsOnTrip.isEmpty()) {
            Colony originColony = origin.getColony();
            if (originColony == null) {
                return false;
            }
            for (Map.Entry<AntType, Integer> entry : transport.entrySet()) {
                long available = originColony.getAntsByType(entry.getKey()).stream()
                        .filter(a -> a.isAlive() && !a.isOnTrade())
                        .count();
                if (available < entry.getValue()) {
                    return false;
                }
            }
            return true;
        }
        for (Map.Entry<AntType, Integer> entry : transport.entrySet()) {
            if (liveCounts.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void cancelDueToEscortLoss() {
        if (!isActive) {
            return;
        }
        Colony originColony = origin.getColony();
        Colony destColony = destination.getColony();
        if (originColony != null) {
            if (!isReturning) {
                for (Map.Entry<ResourceType, Double> entry : load.entrySet()) {
                    originColony.getResourceService().addResource(originColony, entry.getKey(), entry.getValue());
                }
            }
            String destName = destColony != null ? destColony.getName() : "?";
            originColony.logEvent(ColonyLogPrefixes.TRADE + " "
                    + LanguageStrings.format(LanguageStrings.LOG_TRADE_ESCORT_LOSS_FMT, destName));
        }
        releaseAnts();
        isActive = false;
    }

    public void cancelDueToEscortLoss(TradeManager tradeManager) {
        cancelDueToEscortLoss();
        if (tradeManager != null) {
            tradeManager.removeTrade(this);
        }
    }

    public void setPendingUpdate(Map<ResourceType, Double> load, Map<ResourceType, Double> returnLoad, Map<AntType, Integer> transport, boolean isRecurrent, boolean isBilateral, TradeMethod method) {
        this.pendingLoad = new HashMap<>(load);
        this.pendingReturnLoad = returnLoad != null ? new HashMap<>(returnLoad) : new HashMap<>();
        this.pendingTransport = new HashMap<>(transport);
        this.pendingRecurrent = isRecurrent;
        this.pendingIsBilateral = isBilateral;
        this.pendingMethod = method;
        this.hasPendingUpdate = true;
    }

    private void applyPendingUpdate() {
        this.load.clear();
        this.load.putAll(pendingLoad);
        this.returnLoad.clear();
        this.returnLoad.putAll(pendingReturnLoad);
        this.transport.clear();
        this.transport.putAll(pendingTransport);
        this.isRecurrent = pendingRecurrent;
        this.isBilateral = pendingIsBilateral;
        this.method = pendingMethod;
        this.hasPendingUpdate = false;
        calculateHours();
    }

    public int getRemainingHours() {
        return remainingHours;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public void restoreTripState(int totalHours, int remainingHours, boolean isReturning) {
        this.totalHours = totalHours;
        this.remainingHours = remainingHours;
        this.isReturning = isReturning;
    }

    public boolean isReturning() {
        return isReturning;
    }

    public Hex getOrigin() {
        return origin;
    }

    public Hex getDestination() {
        return destination;
    }

    public Map<ResourceType, Double> getLoad() {
        return load;
    }

    public Map<ResourceType, Double> getReturnLoad() {
        return returnLoad;
    }

    public Map<AntType, Integer> getTransport() {
        return transport;
    }

    public boolean isRecurrent() {
        return isRecurrent;
    }

    public boolean isBilateral() {
        return isBilateral;
    }

    public TradeMethod getMethod() {
        return method;
    }

    public boolean isActive() {
        return isActive;
    }

    public List<Ant> getAntsOnTrip() {
        return antsOnTrip;
    }

    public boolean containsAnt(Ant ant) {
        return ant != null && antsOnTrip.contains(ant);
    }

    public double getTunnelBearingRadians() {
        return tunnelBearingRadians;
    }

    public void initializeTunnelBearing(boolean returning) {
        this.tunnelBearingRadians = ColonyConvoyTransitService.bearingFromHexes(origin, destination, returning);
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean hasPendingUpdate() {
        return hasPendingUpdate;
    }

    public Map<ResourceType, Double> getPendingLoad() {
        return pendingLoad;
    }

    public Map<ResourceType, Double> getPendingReturnLoad() {
        return pendingReturnLoad;
    }

    public Map<AntType, Integer> getPendingTransport() {
        return pendingTransport;
    }

    public boolean isPendingRecurrent() {
        return pendingRecurrent != null ? pendingRecurrent : isRecurrent;
    }

    public boolean isPendingBilateral() {
        return pendingIsBilateral != null ? pendingIsBilateral : isBilateral;
    }

    public TradeMethod getPendingMethod() {
        return pendingMethod != null ? pendingMethod : method;
    }
}
