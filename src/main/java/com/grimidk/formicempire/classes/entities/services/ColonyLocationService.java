package com.grimidk.formicempire.classes.entities.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.Room;

public class ColonyLocationService {
    
    private final List<ResourceSource> discoveredSources;

    public ColonyLocationService() {
        this.discoveredSources = new ArrayList<>();
    }

    // --- Resource Source Management ---
    public List<ResourceSource> getDiscoveredSources() {
        return Collections.unmodifiableList(discoveredSources);
    }

    public List<ResourceSource> getSourcesByType(ResourceType type) {
        return discoveredSources.stream()
                .filter(source -> source.getResourceType() == type)
                .collect(Collectors.toList());
    }
    
    public int getTotalQuantityAvailable(ResourceType type) {
        return discoveredSources.stream()
                .filter(source -> source.getResourceType() == type)
                .mapToInt(ResourceSource::getQuantity)
                .sum();
    }
    
    public boolean isSourceFull(Colony colony, ResourceType type) {
        int capacity = colony.getStatsService().getSourceCapacity(colony);
        long count = discoveredSources.stream()
                .filter(s -> s.getResourceType() == type)
                .count();
        return count >= capacity;
    }

    public void addSource(Colony colony, ResourceSource source) {
        int capacity = colony.getStatsService().getSourceCapacity(colony);
        
        long count = discoveredSources.stream()
                .filter(s -> s.getResourceType() == source.getResourceType())
                .count();

        if (count < capacity) {
            this.discoveredSources.add(source);
            colony.logEvent("SCOUT: Found new " + source.getResourceType().getName() + " source (" + source.getQuantity() + ")");
        } else {
            colony.logEvent("SCOUT: Found " + source.getResourceType().getName() + " but capacity is full.");
        }
    }

    public void removeSource(Colony colony, ResourceSource source) {
        if (this.discoveredSources.remove(source)) {
            colony.logEvent("DEPLETED: A " + source.getResourceType().getName() + " source has been exhausted.");
        }
    }
    
    public int gatherFromSource(Colony colony, ResourceSource source, int amountWanted) {
        if (!discoveredSources.contains(source)) return 0;

        int available = source.getQuantity();
        int gathered = Math.min(available, amountWanted);
        
        source.decreaseQuantity(gathered);

        if (source.getQuantity() <= 0) {
            removeSource(colony, source);
        }
        
        return gathered;
    }

    // --- Routing Management ---
    public void calculateRoute(Room from, Room to) {
        int leftLane = 0;
        int rightLane = 1;
        int colonyEntance = 0;
        int colonyExit = 1;
        int worldExit = 1;
        
        if (from.getDimension().getId() == 0 && to.getDimension().getId() == 0) {
           
        } else  if (from.getDimension().getId() == 0 && to.getDimension().getId() == 1) {

        } else if (from.getDimension().getId() == 1 && to.getDimension().getId() == 0) {
            
        } else if (from.getDimension().getId() == 1 && to.getDimension().getId() == 1) {
            
        } else {
            
        }
    }
}