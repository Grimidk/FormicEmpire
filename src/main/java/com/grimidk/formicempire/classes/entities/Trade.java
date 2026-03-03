package com.grimidk.formicempire.classes.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.entities.services.ColonyResourceService;

public class Trade {

    private final Hex origin;
    private final Hex destination;
    private final Map<ResourceType, Double> load;
    private final Map<AntType, Integer> transport;
    private final boolean isRecurrent;
    private final TradeMethod method;
    private boolean isActive;
    
    private int totalHours;
    private int remainingHours;
    private boolean isReturning;

    public Trade(Hex origin, Hex destination, Map<ResourceType, Double> load, Map<AntType, Integer> transport, boolean isRecurrent, TradeMethod method) {
        this.origin = origin;
        this.destination = destination;
        this.load = new HashMap<>(load);
        this.transport = new HashMap<>(transport);
        this.isRecurrent = isRecurrent;
        this.method = method;
        this.isActive = true;
        this.isReturning = false;
        calculateHours();
    }

    private void calculateHours() {
        this.totalHours = Math.max(1, Math.round(12f / method.getSpeedMult()));
        this.remainingHours = this.totalHours;
    }

    public void startTrip() {
        Colony originColony = origin.getColony();
        if (originColony == null) {
            isActive = false;
            return;
        }

        // Subtract Resources
        ColonyResourceService resService = originColony.getResourceService();
        for (Map.Entry<ResourceType, Double> entry : load.entrySet()) {
            resService.consumeResource(originColony, entry.getKey(), entry.getValue());
        }

        // Subtract Ants
        for (Map.Entry<AntType, Integer> entry : transport.entrySet()) {
            List<Ant> colonyAnts = originColony.getAntsByType(entry.getKey());
            int toRemove = entry.getValue();
            int removed = 0;
            // Safer to use iterator or clear from end to avoid issues, 
            // but CopyOnWriteArrayList is safe for remove(0).
            while (removed < toRemove && !colonyAnts.isEmpty()) {
                colonyAnts.remove(0);
                removed++;
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
                completeReturn();
                if (isRecurrent) {
                    startTrip(); // Subtract resources again for the next loop
                } else {
                    isActive = false;
                }
            }
        }
    }

    public void cancel() {
        if (isActive) {
            completeReturn();
            isActive = false;
        }
    }

    private void deliverLoad() {
        Colony dest = destination.getColony();
        if (dest != null) {
            for (Map.Entry<ResourceType, Double> entry : load.entrySet()) {
                dest.getResourceService().addResource(dest, entry.getKey(), entry.getValue());
            }
        }
    }

    private void completeReturn() {
        Colony originColony = origin.getColony();
        if (originColony != null) {
            // Add Ants back
            for (Map.Entry<AntType, Integer> entry : transport.entrySet()) {
                List<Ant> colonyAnts = originColony.getAntsByType(entry.getKey());
                int toAdd = entry.getValue();
                for (int i = 0; i < toAdd; i++) {
                    colonyAnts.add(new Ant(originColony, entry.getKey()));
                }
            }
        }
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
}
