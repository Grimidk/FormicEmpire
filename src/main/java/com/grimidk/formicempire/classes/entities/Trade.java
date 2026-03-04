package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.entities.services.ColonyResourceService;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

public class Trade {

    private final Hex origin;
    private final Hex destination;
    private final Map<ResourceType, Double> load;
    private final Map<AntType, Integer> transport;
    private final List<Ant> antsOnTrip;
    private boolean isRecurrent;
    private TradeMethod method;
    private boolean isActive;
    
    private int totalHours;
    private int remainingHours;
    private boolean isReturning;

    private Map<ResourceType, Double> pendingLoad;
    private Map<AntType, Integer> pendingTransport;
    private Boolean pendingRecurrent;
    private TradeMethod pendingMethod;
    private boolean hasPendingUpdate;

    public Trade(Hex origin, Hex destination, Map<ResourceType, Double> load, Map<AntType, Integer> transport, boolean isRecurrent, TradeMethod method) {
        this.origin = origin;
        this.destination = destination;
        this.load = new HashMap<>(load);
        this.transport = new HashMap<>(transport);
        this.antsOnTrip = new ArrayList<>();
        this.isRecurrent = isRecurrent;
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

    public void startTrip() {
        Colony originColony = origin.getColony();
        if (originColony == null) {
            isActive = false;
            return;
        }

        Colony destColony = destination.getColony();
        if (destColony != null) {
            ColonyResourceService destResService = destColony.getResourceService();
            for (ResourceType rt : load.keySet()) {
                if (!destResService.hasCapacity(destColony, rt)) {
                    originColony.logEvent("TRADE: Cancelled. " + destColony.getName() + " is full.");
                    isActive = false;
                    releaseAnts();
                    return;
                }
            }
        }

        ColonyResourceService resService = originColony.getResourceService();
        for (Map.Entry<ResourceType, Double> entry : load.entrySet()) {
            resService.consumeResource(originColony, entry.getKey(), entry.getValue());
        }

        if (antsOnTrip.isEmpty()) {
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
        
        this.isReturning = false;
        this.remainingHours = this.totalHours;
    }

    public void tick() {
        if (!isActive) return;
        
        remainingHours--;
        if (remainingHours <= 0) {
            if (!isReturning) {
                deliverLoad();
                isReturning = true;
                remainingHours = totalHours;
            } else {
                if (hasPendingUpdate) {
                    releaseAnts();
                    applyPendingUpdate();
                } else {
                    completeReturn();
                }

                if (isRecurrent) {
                    startTrip();
                } else {
                    isActive = false;
                }
            }
        }
    }

    private void applySecurityLoss() {
        Colony originColony = origin.getColony();
        if (originColony == null) return;
        
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
        
        for (Map.Entry<ResourceType, Double> entry : load.entrySet()) {
            load.put(entry.getKey(), entry.getValue() * securityFactor);
        }
    }

    private void deliverLoad() {
        Colony dest = destination.getColony();
        if (dest != null) {
            applySecurityLoss();
            for (Map.Entry<ResourceType, Double> entry : load.entrySet()) {
                dest.getResourceService().addResource(dest, entry.getKey(), entry.getValue());
            }
            if (origin.getColony() != null) {
                origin.getColony().logEvent("TRADE: Trade arrived at " + dest.getName() + " successfully.");
            }
        }
    }

    private void completeReturn() {
        if (!isRecurrent) {
            releaseAnts();
        }
    }

    private void releaseAnts() {
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
                    originColony.logEvent("TRADE: Route to " + destination.getColony().getName() + " cancelled. Resources refunded.");
                }
            }
            releaseAnts();
            isActive = false;
        }
    }

    public void setPendingUpdate(Map<ResourceType, Double> load, Map<AntType, Integer> transport, boolean isRecurrent, TradeMethod method) {
        this.pendingLoad = new HashMap<>(load);
        this.pendingTransport = new HashMap<>(transport);
        this.pendingRecurrent = isRecurrent;
        this.pendingMethod = method;
        this.hasPendingUpdate = true;
    }

    private void applyPendingUpdate() {
        this.load.clear();
        this.load.putAll(pendingLoad);
        this.transport.clear();
        this.transport.putAll(pendingTransport);
        this.isRecurrent = pendingRecurrent;
        this.method = pendingMethod;
        this.hasPendingUpdate = false;
        calculateHours();
    }

    public int getRemainingHours() {
        return remainingHours;
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

    public Map<AntType, Integer> getTransport() {
        return transport;
    }

    public boolean isRecurrent() {
        return isRecurrent;
    }

    public TradeMethod getMethod() {
        return method;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean hasPendingUpdate() {
        return hasPendingUpdate;
    }
}
