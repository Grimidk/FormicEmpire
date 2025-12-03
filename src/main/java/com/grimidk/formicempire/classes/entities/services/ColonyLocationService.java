package com.grimidk.formicempire.classes.entities.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.Room;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

public class ColonyLocationService {
    
    // --- Routing & Physics Constants ---
    public static final int ROOM_SIZE = 256; 
    public static final int HALL_WIDTH = 128;
    public static final int LANE_OFFSET = 30;
    
    public static final int PAD_WALL = 64; 
    public static final int PAD_DOOR = 20; 
    public static final int PAD_TOP = 48;
    public static final int PAD_BOTTOM = 48;
    public static final int ANT_SIZE = 40;

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

    // --- Routing Coordinates ---
    public int getHallwayCenterX(Colony colony) {
        if (colony.getEntranceBounds() != null) {
            return (int) colony.getEntranceBounds().getCenterX();
        }
        return colony.getGameAreaWidth() / 2;
    }

    public int getLaneCenter(Colony colony, boolean goingDown) {
        int center = getHallwayCenterX(colony);
        return goingDown ? center : (center - LANE_OFFSET);
    }

    public NeoPoint getColonyEntrance(Colony colony) {
        int offset = 20;
        int x = getHallwayCenterX(colony) - offset;  
        int y;
        if (colony.getEntranceBounds() != null) {
            y = (int)colony.getEntranceBounds().getCenterY() - offset;
        } else {
            y = (colony.getGameAreaHeight() / 2) - offset;
        }

        return new NeoPoint(x, y, WorldSpaces.OVERWORLD);
    }

    public NeoPoint getColonyExit(Colony colony) {
        int x = getHallwayCenterX(colony);
        int y = -50;
        return new NeoPoint(x, y, WorldSpaces.UNDERWORLD);
    }

    public NeoPoint getWorldExit(Colony colony) {
        int x = getHallwayCenterX(colony);
        int y = -50;
        return new NeoPoint(x, y, WorldSpaces.OVERWORLD);
    }

    // --- Routing Management ---
    public void calculateRoute(Colony colony, Room from, Room to, Ant ant) {
        boolean lane = false; //false = down, true = up

        if (from == null && ant.getDimension().getId() == 1) {
            // Wandering 
        }

        if (from.getDimension().getId() == 2) {
            // Coming from MAINWORLD
        }

        if (to.getDimension().getId() == 2) {
            // Going to MAINWORLD
            getWorldExit(colony);
        }
        
        if (from.getDimension().getId() == 0 && to.getDimension().getId() == 0) {
            from.getExitPoint();

            to.getEntryPoint();
        } else if (from.getDimension().getId() == 1 && to.getDimension().getId() == 1) {
            from.getExitPoint();
            if (from.getFloorPoint().getY() > to.getFloorPoint().getY()) {
                lane = true;

            } else if (from.getFloorPoint().getY() < to.getFloorPoint().getY()){
                lane = false;

            } else {

            }
            to.getEntryPoint();
        } else if (from.getDimension().getId() == 0 && to.getDimension().getId() == 1) {
            from.getExitPoint();

            to.getEntryPoint();
        } else if (from.getDimension().getId() == 1 && to.getDimension().getId() == 0) {
            from.getExitPoint();

            to.getEntryPoint();
        } else { return; }
    }
}