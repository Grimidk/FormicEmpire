package com.grimidk.formicempire.classes.entities.services.dynasty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;

public class DynastyTradeService {

    private final Dynasty dynasty;
    private final TradeManager tradeManager;

    public DynastyTradeService(Dynasty dynasty, TradeManager tradeManager) {
        this.dynasty = dynasty;
        this.tradeManager = tradeManager;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    public List<Trade> getDynastyTrades() {
        return tradeManager.getActiveTrades().stream()
            .filter(this::isTradeInDynasty)
            .collect(Collectors.toList());
    }

    public int countActiveRecurrentRoutes() {
        return (int) tradeManager.getActiveTrades().stream()
                .filter(t -> t.isActive() && t.isRecurrent() && isTradeInDynasty(t))
                .count();
    }

    private boolean isTradeInDynasty(Trade trade) {
        Colony originColony = trade.getOrigin().getColony();
        return originColony != null && originColony.getDynasty() == dynasty;
    }

    public List<Trade> getColonyTrades(Colony colony) {
        return tradeManager.getActiveTrades().stream()
            .filter(t -> t.getOrigin().getColony() == colony || t.getDestination().getColony() == colony)
            .collect(Collectors.toList());
    }

    public Trade findTrade(Colony origin, Colony destination) {
        if (origin == null || destination == null || tradeManager == null) {
            return null;
        }
        for (Trade trade : tradeManager.getActiveTrades()) {
            if (!trade.isActive()) {
                continue;
            }
            Colony tradeOrigin = trade.getOrigin().getColony();
            Colony tradeDest = trade.getDestination().getColony();
            if (tradeOrigin == origin && tradeDest == destination) {
                return trade;
            }
            if (tradeOrigin != null && tradeDest != null
                    && tradeOrigin.getId() == origin.getId() && tradeDest.getId() == destination.getId()) {
                return trade;
            }
        }
        return null;
    }

    public List<Colony> getNeighborColonies(World world, Colony colony) {
        List<Colony> neighbors = new ArrayList<>();
        Hex center = world.getHexOfColony(colony);
        if (center == null) {
            return neighbors;
        }
        for (Hex hex : center.getAdjacentNeighbors()) {
            if (hex != null && hex.getColony() != null && hex.getColony() != colony) {
                neighbors.add(hex.getColony());
            }
        }
        return neighbors;
    }

    public Hex getNeighborHex(World world, Colony colony, Colony neighbor) {
        Hex center = world.getHexOfColony(colony);
        if (center == null) {
            return null;
        }
        for (Hex hex : center.getAdjacentNeighbors()) {
            if (hex != null && hex.getColony() == neighbor) {
                return hex;
            }
        }
        return null;
    }
}
