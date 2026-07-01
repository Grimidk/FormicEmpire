package com.grimidk.formicempire.classes.entities.services;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

public class ColonyConvoyTransitService {

    public void beginConvoyTransit(Colony colony, Trade trade) {
        if (colony == null || trade == null) {
            return;
        }
        trade.initializeTunnelBearing(false);
        for (Ant ant : trade.getAntsOnTrip()) {
            prepareAntForOutbound(colony, ant, trade);
        }
    }

    public void onReturnLegStarted(Colony colony, Trade trade) {
        if (colony == null || trade == null) {
            return;
        }
        trade.initializeTunnelBearing(true);
        for (Ant ant : trade.getAntsOnTrip()) {
            if (ant.getDimension() == WorldSpaces.TUNNEL_WORLD) {
                ant.setPosition(new Point(0, 0));
                aimTunnelTravel(ant, trade);
            } else {
                prepareAntForOutbound(colony, ant, trade);
            }
        }
    }

    public void restoreConvoyAnts(Colony colony, Trade trade) {
        if (colony == null || trade == null) {
            return;
        }
        NeoPoint staging = colony.getSpatialService().getTransitStagingPoint(colony);
        int index = 0;
        for (Ant ant : trade.getAntsOnTrip()) {
            ant.clearRoute();
            ant.clearLoad();
            ant.setDimension(WorldSpaces.UNDERWORLD);
            ant.setPosition(staggerStagingPoint(staging, index++));
        }
    }

    public void runConvoyPhysics(Colony colony) {
        if (colony == null || !colony.isActive()) {
            return;
        }
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            List<Ant> antList = entry.getValue();
            synchronized (antList) {
                for (Ant ant : antList) {
                    if (!ant.isAlive() || !ant.isOnTrade()) {
                        continue;
                    }
                    Trade trade = findTradeForAnt(colony, ant);
                    if (trade == null) {
                        continue;
                    }
                    updateConvoyAnt(colony, ant, trade);
                    float moveSpeed = GameConstants.BASE_SPRITE_SPEED;
                    if (colony.isCreatineDietActive()) {
                        moveSpeed *= GameConstants.CREATINE_DIET_SPEED_MULTIPLIER;
                    }
                    if (entry.getKey() == GameConstants.TYPE_WORKER
                            && colony.hasUpgrade(GameUnlocks.STAT_WORKER_SPEED_2)) {
                        moveSpeed *= 2f;
                    }
                    ant.updatePosition(moveSpeed);
                }
            }
        }
    }

    private void updateConvoyAnt(Colony colony, Ant ant, Trade trade) {
        if (ant.getDimension() == WorldSpaces.TUNNEL_WORLD) {
            aimTunnelTravel(ant, trade);
            return;
        }
        if (ant.getDimension() == WorldSpaces.OVERWORLD) {
            ant.setDimension(WorldSpaces.UNDERWORLD);
            ant.setPosition(colony.getSpatialService().getTransitStagingPoint(colony));
            ant.clearRoute();
        }
        if (ant.getDimension() == WorldSpaces.UNDERWORLD) {
            approachTransitPortal(colony, ant, trade);
        }
    }

    private void prepareAntForOutbound(Colony colony, Ant ant, Trade trade) {
        ant.clearRoute();
        ant.clearLoad();
        if (ant.getDimension() == WorldSpaces.TUNNEL_WORLD) {
            ant.setPosition(new Point(0, 0));
            aimTunnelTravel(ant, trade);
            return;
        }
        if (ant.getDimension() == WorldSpaces.OVERWORLD) {
            ant.setDimension(WorldSpaces.UNDERWORLD);
        }
        ant.setPosition(colony.getSpatialService().getTransitStagingPoint(colony));
    }

    private void approachTransitPortal(Colony colony, Ant ant, Trade trade) {
        NeoPoint portal = colony.getSpatialService().getTransitPortal(colony);
        if (distance(ant.getX(), ant.getY(), portal.x, portal.y) <= GameConstants.CONVOY_PORTAL_APPROACH_PX) {
            enterTunnelWorld(ant, trade);
            return;
        }
        if (!ant.isMoving() && !ant.hasRoute()) {
            ant.moveTo(new Point(portal.x, portal.y));
        }
    }

    private void enterTunnelWorld(Ant ant, Trade trade) {
        ant.clearRoute();
        ant.setDimension(WorldSpaces.TUNNEL_WORLD);
        ant.setPosition(new Point(0, 0));
        aimTunnelTravel(ant, trade);
    }

    private void aimTunnelTravel(Ant ant, Trade trade) {
        double bearing = trade.getTunnelBearingRadians();
        int farX = (int) (ant.getX() + Math.cos(bearing) * GameConstants.CONVOY_TUNNEL_LEG_DISTANCE);
        int farY = (int) (ant.getY() + Math.sin(bearing) * GameConstants.CONVOY_TUNNEL_LEG_DISTANCE);
        if (!ant.isMoving()) {
            ant.moveTo(new Point(farX, farY));
        }
    }

    private Trade findTradeForAnt(Colony colony, Ant ant) {
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || dynasty.getTradeService() == null) {
            return null;
        }
        for (Trade trade : dynasty.getTradeService().getDynastyTrades()) {
            if (trade.getOrigin().getColony() == colony && trade.containsAnt(ant)) {
                return trade;
            }
        }
        return null;
    }

    private static Point staggerStagingPoint(NeoPoint staging, int index) {
        int spread = index % 5;
        return new Point(staging.x + (spread * 8), staging.y + ((index / 5) * 6));
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    public static double bearingFromHexes(Hex origin, Hex destination, boolean returning) {
        if (origin == null || destination == null) {
            return 0.0;
        }
        double dq = destination.getQ() - origin.getQ();
        double dr = destination.getR() - origin.getR();
        double bearing = Math.atan2(dr, dq);
        if (returning) {
            bearing += Math.PI;
        }
        return bearing;
    }
}
