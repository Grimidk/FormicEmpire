package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.TriggerManager;

public class DynastyAutomationService {

    public void runDailyAutomation(Dynasty dynasty) {
        if (dynasty.isPlayer()) return;

        for (Colony colony : dynasty.getColonies()) {
            TriggerManager.evaluateNpcColonyTriggers(colony);
        }
        checkAndBuyUpgrades(dynasty);
        for (Colony colony : dynasty.getColonies()) {
            TriggerManager.evaluateNpcColonyTriggers(colony);
        }
    }

    private void checkAndBuyUpgrades(Dynasty dynasty) {
        Upgrade purchased = DynastyAiPriorities.tryPurchaseNextUpgrade(dynasty);
        if (purchased == null) {
            return;
        }
        if (!dynasty.getColonies().isEmpty()) {
            dynasty.getColonies().get(0).logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + LanguageStrings.format(LanguageStrings.LOG_DYNASTY_RESEARCHED_FMT, purchased.getName()));
        }
    }
}
