package com.grimidk.formicempire.classes.entities.services.colony;

import java.util.LinkedList;
import java.util.Queue;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.spatial.Dimension;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.entities.spatial.Room;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

public class ColonyPathfindingService {

    private final ColonySpatialService spatialService;

    public ColonyPathfindingService(ColonySpatialService spatialService) {
        this.spatialService = spatialService;
    }

    public Queue<NeoPoint> calculateRoute(Colony colony, Room from, Room to, Ant ant) {
        Queue<NeoPoint> route = new LinkedList<>();

        if (to == null) {
            return route;
        }

        Dimension startDim = (from != null) ? from.getDimension() : ant.getDimension();
        Dimension endDim = to.getDimension();
        int startId = startDim.getId();
        int endId = endDim.getId();
        NeoPoint colEnt = spatialService.getColonyEntrance(colony);
        NeoPoint colExit = spatialService.getColonyExit(colony);

        if (startId == 0 && endId == 0) {
            boolean useLanes = spatialService.isYard(from) || spatialService.isYard(to);

            if (from != null) {
                route.add(spatialService.getResolvedPoint(colony, from, "CENTER"));
                if (from.getExitPoint() != null) {
                    route.add(spatialService.getResolvedPoint(colony, from, "EXIT"));
                }
            }

            if (useLanes) {
                int startY = (from != null && from.getExitPoint() != null)
                        ? spatialService.getResolvedPoint(colony, from, "EXIT").y
                        : ant.getY();
                NeoPoint destEntry = spatialService.getResolvedPoint(colony, to, "ENTRY");
                NeoPoint destCenter = spatialService.getResolvedPoint(colony, to, "CENTER");
                int endY = (destEntry != null) ? destEntry.y : destCenter.y;
                boolean goingDown = (startY < endY);
                int laneX = spatialService.getLaneCenter(colony, goingDown);

                route.add(new NeoPoint(laneX, startY, startDim));
                route.add(new NeoPoint(laneX, endY, startDim));

                if (destEntry != null) {
                    route.add(destEntry);
                }
                route.add(destCenter);
            } else {
                NeoPoint destEntry = spatialService.getResolvedPoint(colony, to, "ENTRY");
                if (destEntry != null) {
                    route.add(destEntry);
                }
                route.add(spatialService.getResolvedPoint(colony, to, "CENTER"));
            }
        } else if (startId == 1 && endId == 1) {
            if (from != null) {
                route.add(spatialService.getResolvedPoint(colony, from, "CENTER"));
                route.add(spatialService.getResolvedPoint(colony, from, "EXIT"));
            }

            int startY = (from != null) ? spatialService.getResolvedPoint(colony, from, "EXIT").y : ant.getY();
            NeoPoint destEntry = spatialService.getResolvedPoint(colony, to, "ENTRY");
            int endY = destEntry.y;
            boolean goingDown = (startY < endY);
            int laneX = spatialService.getLaneCenter(colony, goingDown);

            route.add(new NeoPoint(laneX, startY, startDim));
            route.add(new NeoPoint(laneX, endY, startDim));
            route.add(destEntry);
            route.add(spatialService.getResolvedPoint(colony, to, "CENTER"));
        } else if (startId == 0 && endId == 1) {
            if (from != null) {
                route.add(spatialService.getResolvedPoint(colony, from, "CENTER"));
                if (from.getExitPoint() != null) {
                    route.add(spatialService.getResolvedPoint(colony, from, "EXIT"));
                }
            }

            if (spatialService.isYard(from)) {
                int startY = (from != null && from.getExitPoint() != null)
                        ? spatialService.getResolvedPoint(colony, from, "EXIT").y
                        : ant.getY();
                int endY = colEnt.y;
                boolean goingDown = (startY < endY);
                int laneX = spatialService.getLaneCenter(colony, goingDown);

                route.add(new NeoPoint(laneX, startY, startDim));
                route.add(new NeoPoint(laneX, endY, startDim));
            }

            route.add(colEnt);
            route.add(colExit);

            int uwLaneX = spatialService.getLaneCenter(colony, true);
            NeoPoint destEntry = spatialService.getResolvedPoint(colony, to, "ENTRY");
            int targetY = destEntry.y;

            route.add(new NeoPoint(uwLaneX, colExit.y, WorldSpaces.UNDERWORLD));
            route.add(new NeoPoint(uwLaneX, targetY, WorldSpaces.UNDERWORLD));
            route.add(destEntry);
            route.add(spatialService.getResolvedPoint(colony, to, "CENTER"));
        } else if (startId == 1 && endId == 0) {
            if (from != null) {
                route.add(spatialService.getResolvedPoint(colony, from, "CENTER"));
                route.add(spatialService.getResolvedPoint(colony, from, "EXIT"));
            }

            int startY = (from != null) ? spatialService.getResolvedPoint(colony, from, "EXIT").y : ant.getY();
            int uwLaneX = spatialService.getLaneCenter(colony, false);

            route.add(new NeoPoint(uwLaneX, startY, WorldSpaces.UNDERWORLD));
            route.add(new NeoPoint(uwLaneX, colExit.y, WorldSpaces.UNDERWORLD));
            route.add(colExit);
            route.add(colEnt);

            if (spatialService.isYard(to)) {
                int owStartY = colEnt.y;
                NeoPoint destEntry = spatialService.getResolvedPoint(colony, to, "ENTRY");
                NeoPoint destCenter = spatialService.getResolvedPoint(colony, to, "CENTER");
                int owEndY = (destEntry != null) ? destEntry.y : destCenter.y;

                boolean goingDown = (owStartY < owEndY);
                int owLaneX = spatialService.getLaneCenter(colony, goingDown);

                route.add(new NeoPoint(owLaneX, owStartY, endDim));
                route.add(new NeoPoint(owLaneX, owEndY, endDim));
            }

            if (to.getEntryPoint() != null) {
                route.add(spatialService.getResolvedPoint(colony, to, "ENTRY"));
            }
            route.add(spatialService.getResolvedPoint(colony, to, "CENTER"));
        } else if (endId == 2) {
            if (startId == 1) {
                route.addAll(calculateRoute(colony, from,
                        new Room(999, "Temp", WorldSpaces.OVERWORLD, 0, 0, false, null, colEnt, colEnt, colEnt, null, null, null), ant));
            }
            int startY = ant.getY();
            int endY = spatialService.getWorldExit(colony).y;
            int laneX = spatialService.getLaneCenter(colony, startY < endY);

            route.add(new NeoPoint(laneX, startY, WorldSpaces.OVERWORLD));
            route.add(new NeoPoint(laneX, endY, WorldSpaces.OVERWORLD));
            route.add(spatialService.getWorldExit(colony));
        }

        return route;
    }
}
