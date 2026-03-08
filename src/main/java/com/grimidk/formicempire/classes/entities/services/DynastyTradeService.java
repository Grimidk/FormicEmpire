package com.grimidk.formicempire.classes.entities.services;

import java.util.List;
import java.util.stream.Collectors;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;

public class DynastyTradeService {

    private final Dynasty dynasty;
    private final TradeManager tradeManager;

    public DynastyTradeService(Dynasty dynasty, TradeManager tradeManager) {
        this.dynasty = dynasty;
        this.tradeManager = tradeManager;
    }

    public List<Trade> getDynastyTrades() {
        return tradeManager.getActiveTrades().stream()
            .filter(t -> isTradeInDynasty(t))
            .collect(Collectors.toList());
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
}
