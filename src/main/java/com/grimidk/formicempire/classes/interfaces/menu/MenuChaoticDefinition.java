package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.services.colony.ConvoyScene;

import java.util.List;

public record MenuChaoticDefinition(
        int id,
        MenuChaoticKind kind,
        Biome primaryBiome,
        Biome secondaryBiome,
        ConvoyScene.BackgroundKind convoyBackground,
        boolean convoyTravelingRight,
        List<MenuChaoticAntEntry> antTemplate,
        int antCount,
        List<MenuChaoticCritterEntry> critterTemplate,
        int critterCount,
        long layoutSeed) {
}
