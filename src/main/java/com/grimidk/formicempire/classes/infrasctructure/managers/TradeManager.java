package com.grimidk.formicempire.classes.infrasctructure.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.grimidk.formicempire.classes.entities.dynasty.Trade;

public class TradeManager implements Runnable {

    private final List<Trade> activeTrades;

    public TradeManager() {
        this.activeTrades = new CopyOnWriteArrayList<>();
    }

    public void addTrade(Trade trade) {
        if (trade != null && !activeTrades.contains(trade)) {
            activeTrades.add(trade);
        }
    }

    public void removeTrade(Trade trade) {
        activeTrades.remove(trade);
    }

    public List<Trade> getActiveTrades() {
        return new ArrayList<>(activeTrades);
    }

    public void clearActiveTrades() {
        activeTrades.clear();
    }

    @Override
    public void run() {
        updateTrades();
    }

    private void updateTrades() {
        List<Trade> toRemove = new ArrayList<>();
        for (Trade trade : activeTrades) {
            if (trade.isActive()) {
                trade.tick();
                if (!trade.isActive()) {
                    toRemove.add(trade);
                }
            } else {
                toRemove.add(trade);
            }
        }
        activeTrades.removeAll(toRemove);
    }
}
