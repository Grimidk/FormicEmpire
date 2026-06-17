package com.grimidk.formicempire.classes.entities.services;

import java.awt.Point;
import java.util.List;
import java.util.Queue;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.Room;

public class ColonyLocationService {

    private final ColonySpatialService spatialService;
    private final ColonySourceService sourceService;
    private final ColonyPathfindingService pathfindingService;

    public ColonyLocationService(
            ColonySpatialService spatialService,
            ColonySourceService sourceService,
            ColonyPathfindingService pathfindingService) {
        this.spatialService = spatialService;
        this.sourceService = sourceService;
        this.pathfindingService = pathfindingService;
    }

    public ColonySpatialService getSpatialService() {
        return spatialService;
    }

    public ColonySourceService getSourceService() {
        return sourceService;
    }

    public ColonyPathfindingService getPathfindingService() {
        return pathfindingService;
    }

    public List<ResourceSource> getDiscoveredSources() {
        return sourceService.getDiscoveredSources();
    }

    public List<ResourceSource> getSourcesByType(ResourceType type) {
        return sourceService.getSourcesByType(type);
    }

    public int getTotalQuantityAvailable(ResourceType type) {
        return sourceService.getTotalQuantityAvailable(type);
    }

    public boolean isSourceFull(Colony colony, ResourceType type) {
        return sourceService.isSourceFull(colony, type);
    }

    public boolean addSource(Colony colony, ResourceSource source) {
        return sourceService.addSource(colony, source);
    }

    public void removeSource(Colony colony, ResourceSource source) {
        sourceService.removeSource(colony, source);
    }

    public int gatherFromSource(Colony colony, ResourceSource source, int amountWanted) {
        return sourceService.gatherFromSource(colony, source, amountWanted);
    }

    public double computeGatherEfficiency(Colony colony, ResourceSource source, List<Ant> workers) {
        return sourceService.computeGatherEfficiency(colony, source, workers);
    }

    public float computeFullEfficiencyRadius(Colony colony, List<Ant> workers) {
        return sourceService.computeFullEfficiencyRadius(colony, workers);
    }

    public boolean isActiveSource(ResourceSource source) {
        return sourceService.isActiveSource(source);
    }

    public ResourceSource findNearestRelevantSource(Colony colony, Ant ant) {
        return sourceService.findNearestRelevantSource(colony, ant);
    }

    public Room createTempRoomAtPoint(Point p, Dimension dim) {
        return spatialService.createTempRoomAtPoint(p, dim);
    }

    public int getHallwayCenterX(Colony colony) {
        return spatialService.getHallwayCenterX(colony);
    }

    public int getLaneCenter(Colony colony, boolean goingDown) {
        return spatialService.getLaneCenter(colony, goingDown);
    }

    public NeoPoint getColonyEntrance(Colony colony) {
        return spatialService.getColonyEntrance(colony);
    }

    public NeoPoint getColonyExit(Colony colony) {
        return spatialService.getColonyExit(colony);
    }

    public NeoPoint getWorldExit(Colony colony) {
        return spatialService.getWorldExit(colony);
    }

    public Queue<NeoPoint> calculateRoute(Colony colony, Room from, Room to, Ant ant) {
        return pathfindingService.calculateRoute(colony, from, to, ant);
    }

    public static final class GatheringMath {

        private GatheringMath() {
        }

        public static double gatheringEfficiency(
                double nestX, double nestY, int sourceX, int sourceY, float fullEfficiencyRadius) {
            return ColonySourceService.GatheringMath.gatheringEfficiency(
                    nestX, nestY, sourceX, sourceY, fullEfficiencyRadius);
        }

        public static float computeFullEfficiencyRadius(Colony colony, List<Ant> workers) {
            return ColonySourceService.GatheringMath.computeFullEfficiencyRadius(colony, workers);
        }
    }
}
