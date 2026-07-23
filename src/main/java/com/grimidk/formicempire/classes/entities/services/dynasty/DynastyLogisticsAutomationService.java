package com.grimidk.formicempire.classes.entities.services.dynasty;

import java.util.HashMap;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.dynasty.TradeMethod;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class DynastyLogisticsAutomationService {

    public void runDailyLogistics(Dynasty dynasty, World world, TradeManager tradeManager) {
        if (dynasty.isPlayer()) return;
        if (dynasty.isDefeated()) return;
        if (dynasty.getColonies().size() < 2) return;
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) return;
        if (!dynasty.hasUpgrade(GameUnlocks.ROLE_COURIER)) return;
        if (world == null || tradeManager == null) return;

        dynasty.bindTradeManager(tradeManager);
        DynastyTradeService tradeService = dynasty.getTradeService();
        if (tradeService == null) return;

        for (Colony origin : dynasty.getColonies()) {
            if (origin.getAge() < 7 || !origin.isAutomationEnabled()) continue;

            Hex originHex = world.getHexOfColony(origin);
            if (originHex == null) continue;

            for (Colony neighbor : tradeService.getNeighborColonies(world, origin)) {
                if (neighbor.getDynasty() != dynasty) continue;
                if (neighbor.getAge() < 7) continue;
                if (tradeService.findTrade(origin, neighbor) != null) continue;

                Map<ResourceType, Double> load = DynastyTradeAutomation.computeOutboundLoad(origin, neighbor);
                if (load.isEmpty()) continue;

                Map<AntType, Integer> transport = DynastyTradeAutomation.buildTransport(origin);
                if (transport.isEmpty()) continue;

                Hex targetHex = tradeService.getNeighborHex(world, origin, neighbor);
                if (targetHex == null) continue;

                TradeMethod method = DynastyTradeAutomation.pickTradeMethod(dynasty, originHex, targetHex);
                Trade trade = new Trade(originHex, targetHex, load, null, transport, true, false, method);
                if (trade.startTrip()) {
                    tradeManager.addTrade(trade);
                    origin.logEvent(ColonyLogPrefixes.AUTOMATION + " "
                            + LanguageStrings.format(LanguageStrings.LOG_AUTOMATION_TRADE_FMT, neighbor.getName()));
                }
                return;
            }
        }
    }
}
