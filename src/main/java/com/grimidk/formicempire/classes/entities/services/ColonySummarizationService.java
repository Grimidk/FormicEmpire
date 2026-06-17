package com.grimidk.formicempire.classes.entities.services;

import java.util.Random;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Colony;

public class ColonySummarizationService {

    private final Random random = new Random();

    public void runHourlyLite(Colony colony, Biome biome) {
        ColonyJobRules.runHourlyLite(colony, biome, random);
    }

    public void runDailyLite(Colony colony) {
        ColonyJobRules.runDailyLite(colony, random);
    }
}
