package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Colony;

public class ColonySummarizationService {

    public void runHourlyLite(Colony colony, Biome biome) {
        ColonyJobRules.runHourlyLite(colony, biome);
    }

    public void runDailyLite(Colony colony) {
        ColonyJobRules.runDailyLite(colony);
    }
}
