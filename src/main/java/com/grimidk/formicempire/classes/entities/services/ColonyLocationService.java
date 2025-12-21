package com.grimidk.formicempire.classes.entities.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.awt.Point;
import java.awt.Rectangle;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.Room;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
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
        this.discoveredSources = new CopyOnWriteArrayList<>();
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
            colony.logEvent("Found new " + source.getResourceType().getName() + " source.");
        } else {
            colony.logEvent("Found " + source.getResourceType().getName() + " but capacity is full.");
        }
    }

    public void removeSource(Colony colony, ResourceSource source) {
        if (this.discoveredSources.remove(source)) {
            colony.logEvent("A " + source.getResourceType().getName() + " source has been exhausted.");
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

    public ResourceSource findNearestRelevantSource(Colony colony, Ant ant) {
        List<ResourceType> targetTypes = new ArrayList<>();
        AntRole role = ant.getRole();

        if (role == GameConstants.ROLE_FORAGER) {
            targetTypes.add(GameConstants.PLANT_RESOURCE);
            targetTypes.add(GameConstants.WATER_RESOURCE);
        } else if (role == GameConstants.ROLE_HUNTER) {
            targetTypes.add(GameConstants.MEAT_RESOURCE);
        } else if (role == GameConstants.ROLE_MINER) {
            targetTypes.add(GameConstants.ROCK_RESOURCE);
        }

        ResourceSource nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (ResourceSource source : discoveredSources) {
            if (source == null || source.getQuantity() <= 0) continue;

            if (targetTypes.contains(source.getResourceType())) {
                double dist = Math.pow(source.getX() - ant.getX(), 2) + Math.pow(source.getY() - ant.getY(), 2);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = source;
                }
            }
        }
        return nearest;
    }
    
    public Room createTempRoomAtPoint(Point p, Dimension dim) {
        return new Room(9999, "TempTarget", dim, 10, 10, false, p, p, p, p, null, null, null);
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
    
    // --- Helpers Points ---
        private NeoPoint getResolvedPoint(Colony colony, Room room, String pointType) {
        if (room == null) return null;
        
        Rectangle dynamicBounds = null;
        if (room.getId() == 201) dynamicBounds = colony.getGraverBounds(); // Graveyard
        else if (room.getId() == 200) dynamicBounds = colony.getRancherBounds(); // Rancher
        else if (room.getId() == 104) dynamicBounds = colony.getBreederBounds(); // Breeder
        else if (room.getId() == 999) dynamicBounds = colony.getPhysicsService().getRoomBounds(colony, WorldSpaces.CONSTRUCTION_SITE); // Construction

        if (dynamicBounds != null) {
            return new NeoPoint((int)dynamicBounds.getCenterX(), (int)dynamicBounds.getCenterY(), room.getDimension());
        }

        Point p = null;
        switch (pointType) {
            case "CENTER": p = room.getCenterPoint(); break;
            case "ENTRY": p = room.getEntryPoint(); break;
            case "EXIT": p = room.getExitPoint(); break;
        }
        
        if (p == null) return null;

        if (room.getDimension().getId() == 1) { 
            return new NeoPoint(p.x + getHallwayCenterX(colony), p.y, room.getDimension());
        }

        return new NeoPoint(p.x, p.y, room.getDimension());
    }

    private boolean isYard(Room room) {
        if (room == null) return false;
        return room.getId() == 200 || room.getId() == 201;
    }

    // --- Routing Management ---
    public Queue<NeoPoint> calculateRoute(Colony colony, Room from, Room to, Ant ant) {
        Queue<NeoPoint> route = new LinkedList<>();

        if (to == null) return route;

        Dimension startDim = (from != null) ? from.getDimension() : ant.getDimension();
        Dimension endDim = to.getDimension();     
        int startId = startDim.getId();
        int endId = endDim.getId();
        NeoPoint colEnt = getColonyEntrance(colony); 
        NeoPoint colExit = getColonyExit(colony);    

        if (startId == 0 && endId == 0) {
            boolean useLanes = isYard(from) || isYard(to);

            if (from != null) {
                route.add(getResolvedPoint(colony, from, "CENTER"));
                if (from.getExitPoint() != null) {
                    route.add(getResolvedPoint(colony, from, "EXIT"));
                }
            }

            if (useLanes) {
                int startY = (from != null && from.getExitPoint() != null) ? getResolvedPoint(colony, from, "EXIT").y : ant.getY();
                NeoPoint destEntry = getResolvedPoint(colony, to, "ENTRY");
                NeoPoint destCenter = getResolvedPoint(colony, to, "CENTER");
                int endY = (destEntry != null) ? destEntry.y : destCenter.y;
                boolean goingDown = (startY < endY);
                int laneX = getLaneCenter(colony, goingDown);

                route.add(new NeoPoint(laneX, startY, startDim));
                route.add(new NeoPoint(laneX, endY, startDim));

                if (destEntry != null) route.add(destEntry);
                route.add(destCenter);
            } else {
                NeoPoint destEntry = getResolvedPoint(colony, to, "ENTRY");
                if (destEntry != null) route.add(destEntry);
                route.add(getResolvedPoint(colony, to, "CENTER"));
            }
        }

        else if (startId == 1 && endId == 1) {
            if (from != null) {
                route.add(getResolvedPoint(colony, from, "CENTER"));
                route.add(getResolvedPoint(colony, from, "EXIT"));
            }

            int startY = (from != null) ? getResolvedPoint(colony, from, "EXIT").y : ant.getY();
            NeoPoint destEntry = getResolvedPoint(colony, to, "ENTRY");
            int endY = destEntry.y;
            boolean goingDown = (startY < endY); 
            int laneX = getLaneCenter(colony, goingDown);

            route.add(new NeoPoint(laneX, startY, startDim));
            route.add(new NeoPoint(laneX, endY, startDim));
            route.add(destEntry);
            route.add(getResolvedPoint(colony, to, "CENTER"));
        }

        else if (startId == 0 && endId == 1) {
            if (from != null) {
                route.add(getResolvedPoint(colony, from, "CENTER"));
                if (from.getExitPoint() != null) {
                    route.add(getResolvedPoint(colony, from, "EXIT"));
                }
            }

            if (isYard(from)) {
                int startY = (from != null && from.getExitPoint() != null) ? getResolvedPoint(colony, from, "EXIT").y : ant.getY();
                int endY = colEnt.y;
                boolean goingDown = (startY < endY);
                int laneX = getLaneCenter(colony, goingDown);

                route.add(new NeoPoint(laneX, startY, startDim)); 
                route.add(new NeoPoint(laneX, endY, startDim));   
            } 
            
            route.add(colEnt); 
            route.add(colExit); 

            int uwLaneX = getLaneCenter(colony, false); 
            NeoPoint destEntry = getResolvedPoint(colony, to, "ENTRY");
            int targetY = destEntry.y;

            route.add(new NeoPoint(uwLaneX, colExit.y, WorldSpaces.UNDERWORLD));
            route.add(new NeoPoint(uwLaneX, targetY, WorldSpaces.UNDERWORLD));
            route.add(destEntry);
            route.add(getResolvedPoint(colony, to, "CENTER"));
        }

        else if (startId == 1 && endId == 0) {
            if (from != null) {
                route.add(getResolvedPoint(colony, from, "CENTER"));
                route.add(getResolvedPoint(colony, from, "EXIT"));
            }

            int startY = (from != null) ? getResolvedPoint(colony, from, "EXIT").y : ant.getY();
            int uwLaneX = getLaneCenter(colony, true);

            route.add(new NeoPoint(uwLaneX, startY, WorldSpaces.UNDERWORLD));
            route.add(new NeoPoint(uwLaneX, colExit.y, WorldSpaces.UNDERWORLD));
            route.add(colExit); 
            route.add(colEnt);  

            if (isYard(to)) {
                int owStartY = colEnt.y;
                NeoPoint destEntry = getResolvedPoint(colony, to, "ENTRY");
                NeoPoint destCenter = getResolvedPoint(colony, to, "CENTER");
                int owEndY = (destEntry != null) ? destEntry.y : destCenter.y;
                
                boolean goingDown = (owStartY < owEndY);
                int owLaneX = getLaneCenter(colony, goingDown);

                route.add(new NeoPoint(owLaneX, owStartY, endDim));
                route.add(new NeoPoint(owLaneX, owEndY, endDim));
            }

            if (to.getEntryPoint() != null) {
                route.add(getResolvedPoint(colony, to, "ENTRY"));
            }
            route.add(getResolvedPoint(colony, to, "CENTER"));
        }

        else if (endId == 2) {
            if (startId == 1) {
                route.addAll(calculateRoute(colony, from, new Room(999, "Temp", WorldSpaces.OVERWORLD, 0,0,false,null,colEnt,colEnt,colEnt,null,null,null), ant));
            }
            int startY = ant.getY();
            int endY = getWorldExit(colony).y;
            int laneX = getLaneCenter(colony, startY < endY);
            
            route.add(new NeoPoint(laneX, startY, WorldSpaces.OVERWORLD));
            route.add(new NeoPoint(laneX, endY, WorldSpaces.OVERWORLD));
            route.add(getWorldExit(colony));
        }

        return route;
    }
}