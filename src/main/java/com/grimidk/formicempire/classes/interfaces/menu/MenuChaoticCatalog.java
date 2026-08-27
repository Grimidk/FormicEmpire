package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.entities.services.colony.ConvoyScene;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MenuChaoticCatalog {

    public static final int MAX_ANTS = 500;
    public static final int SCENARIO_DURATION_MS = 20_000;
    public static final int ANIMATION_FRAME_MS = 16;
    public static final float OVERWORLD_SPEED_SCALE = 1.4f;
    public static final float COLONY_SPEED_SCALE = 1.4f;
    public static final float BATTLE_LINE_DRIFT_PER_SEC = 0.01f;
    public static final int BATTLE_RESERVE_WALK_PX_PER_SEC = 40;

    private static final List<MenuChaoticDefinition> SCENARIOS = buildScenarios();

    private MenuChaoticCatalog() {
    }

    public static List<MenuChaoticDefinition> getScenarios() {
        return SCENARIOS;
    }

    private static List<MenuChaoticDefinition> buildScenarios() {
        List<MenuChaoticDefinition> scenarios = new ArrayList<>(12);

        scenarios.add(new MenuChaoticDefinition(
                0,
                MenuChaoticKind.OVERWORLD,
                GameConstants.BIOME_PLAINS,
                null,
                null,
                true,
                List.of(
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_WORKER, GameConstants.SPECIES_OMNI, 1111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_WORKER, GameConstants.SPECIES_LEAFCUTTER, 2111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_SOLDIER, GameConstants.SPECIES_TRAPJAW, 2111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_WORKER, GameConstants.SPECIES_HONEYPOT, 1171),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_DRONE, GameConstants.SPECIES_OMNI, 1111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_MAJOR, GameConstants.SPECIES_MARAUDER, 1161)),
                500,
                List.of(
                        new MenuChaoticCritterEntry(GameConstants.TYPE_APHID),
                        new MenuChaoticCritterEntry(GameConstants.TYPE_SYMBIOTIC_MITE)),
                40,
                0x4A11_0001L));

        scenarios.add(new MenuChaoticDefinition(
                1,
                MenuChaoticKind.OVERWORLD,
                GameConstants.BIOME_JUNGLE,
                null,
                null,
                true,
                List.of(
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_WORKER, GameConstants.SPECIES_JET, 1111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_WORKER, GameConstants.SPECIES_WEAVER, 1191),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_SOLDIER, GameConstants.SPECIES_FIRE, 1161),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_WORKER, GameConstants.SPECIES_PHARAOH, 1111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_PRINCESS, GameConstants.SPECIES_JET, 1111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_MAJOR, GameConstants.SPECIES_TRAPJAW, 2111)),
                480,
                List.of(
                        new MenuChaoticCritterEntry(GameConstants.TYPE_APHID),
                        new MenuChaoticCritterEntry(GameConstants.TYPE_DERMESTID)),
                35,
                0x4A11_0002L));

        scenarios.add(new MenuChaoticDefinition(
                2,
                MenuChaoticKind.OVERWORLD,
                GameConstants.BIOME_FOREST,
                null,
                null,
                true,
                List.of(
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_WORKER, GameConstants.SPECIES_CARPENTER, 1111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_WORKER, GameConstants.SPECIES_TURTLE, 3111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_SOLDIER, GameConstants.SPECIES_LEAFCUTTER, 1111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_WORKER, GameConstants.SPECIES_FLOODPLAIN, 1171),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_DRONE, GameConstants.SPECIES_WEAVER, 1111),
                        MenuChaoticAntEntry.overworld(GameConstants.TYPE_MAJOR, GameConstants.SPECIES_CARPENTER, 1161)),
                460,
                List.of(
                        new MenuChaoticCritterEntry(GameConstants.TYPE_PARASITE_ANT),
                        new MenuChaoticCritterEntry(GameConstants.TYPE_SYMBIOTIC_MITE)),
                30,
                0x4A11_0003L));

        scenarios.add(new MenuChaoticDefinition(
                10,
                MenuChaoticKind.COLONY,
                GameConstants.BIOME_TAIGA,
                null,
                null,
                true,
                List.of(
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_WORKER, GameConstants.SPECIES_TURTLE, 1111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_WORKER, GameConstants.SPECIES_LEAFCUTTER, 2111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_SOLDIER, GameConstants.SPECIES_TURTLE, 2111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_WORKER, GameConstants.SPECIES_HONEYPOT, 1171),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_MAJOR, GameConstants.SPECIES_TURTLE, 1161)),
                500,
                List.of(),
                0,
                0x4A11_000AL));

        scenarios.add(new MenuChaoticDefinition(
                11,
                MenuChaoticKind.COLONY,
                GameConstants.BIOME_SWAMP,
                null,
                null,
                true,
                List.of(
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_WORKER, GameConstants.SPECIES_FLOODPLAIN, 1111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_WORKER, GameConstants.SPECIES_PHARAOH, 1111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_SOLDIER, GameConstants.SPECIES_FLOODPLAIN, 1161),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_DRONE, GameConstants.SPECIES_FLOODPLAIN, 1111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_MAJOR, GameConstants.SPECIES_MARAUDER, 2111)),
                480,
                List.of(),
                0,
                0x4A11_000BL));

        scenarios.add(new MenuChaoticDefinition(
                12,
                MenuChaoticKind.COLONY,
                GameConstants.BIOME_URBAN,
                null,
                null,
                true,
                List.of(
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_WORKER, GameConstants.SPECIES_CARPENTER, 1111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_WORKER, GameConstants.SPECIES_OMNI, 1111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_SOLDIER, GameConstants.SPECIES_CARPENTER, 2111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_WORKER, GameConstants.SPECIES_TRAPJAW, 2111),
                        MenuChaoticAntEntry.colony(GameConstants.TYPE_PRINCESS, GameConstants.SPECIES_JET, 1111)),
                500,
                List.of(),
                0,
                0x4A11_000CL));

        scenarios.add(new MenuChaoticDefinition(
                3,
                MenuChaoticKind.BATTLE,
                GameConstants.BIOME_DESERT,
                GameConstants.BIOME_TUNDRA,
                null,
                true,
                buildBattleTemplate(
                        GameConstants.SPECIES_FIRE,
                        GameConstants.SPECIES_TURTLE),
                500,
                List.of(),
                0,
                0x4A11_0004L));

        scenarios.add(new MenuChaoticDefinition(
                4,
                MenuChaoticKind.BATTLE,
                GameConstants.BIOME_MOUNTAIN,
                GameConstants.BIOME_SWAMP,
                null,
                true,
                buildBattleTemplate(
                        GameConstants.SPECIES_MARAUDER,
                        GameConstants.SPECIES_FLOODPLAIN),
                500,
                List.of(),
                0,
                0x4A11_0005L));

        scenarios.add(new MenuChaoticDefinition(
                5,
                MenuChaoticKind.BATTLE,
                GameConstants.BIOME_VOLCANIC,
                GameConstants.BIOME_URBAN,
                null,
                true,
                buildBattleTemplate(
                        GameConstants.SPECIES_PHARAOH,
                        GameConstants.SPECIES_CARPENTER),
                480,
                List.of(),
                0,
                0x4A11_0006L));

        scenarios.add(new MenuChaoticDefinition(
                6,
                MenuChaoticKind.CONVOY,
                GameConstants.BIOME_TAIGA,
                null,
                ConvoyScene.BackgroundKind.LAND_BIOME,
                true,
                List.of(
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_WORKER, GameConstants.SPECIES_LEAFCUTTER, 1111),
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_SOLDIER, GameConstants.SPECIES_LEAFCUTTER, 2111),
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_MAJOR, GameConstants.SPECIES_LEAFCUTTER, 1161),
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_WORKER, GameConstants.SPECIES_TURTLE, 1111)),
                320,
                List.of(),
                0,
                0x4A11_0007L));

        scenarios.add(new MenuChaoticDefinition(
                7,
                MenuChaoticKind.CONVOY,
                GameConstants.BIOME_OCEAN,
                null,
                ConvoyScene.BackgroundKind.SEA,
                false,
                List.of(
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_WORKER, GameConstants.SPECIES_OMNI, 1111),
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_SOLDIER, GameConstants.SPECIES_OMNI, 2111),
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_WORKER, GameConstants.SPECIES_FLOODPLAIN, 1171),
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_MAJOR, GameConstants.SPECIES_OMNI, 1161)),
                280,
                List.of(),
                0,
                0x4A11_0008L));

        scenarios.add(new MenuChaoticDefinition(
                8,
                MenuChaoticKind.CONVOY,
                GameConstants.BIOME_LAKE,
                null,
                ConvoyScene.BackgroundKind.SKY,
                false,
                List.of(
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_PRINCESS, GameConstants.SPECIES_JET, 1111),
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_DRONE, GameConstants.SPECIES_JET, 1111),
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_PRINCESS, GameConstants.SPECIES_OMNI, 1111),
                        MenuChaoticAntEntry.convoy(GameConstants.TYPE_DRONE, GameConstants.SPECIES_OMNI, 1111)),
                260,
                List.of(),
                0,
                0x4A11_0009L));

        return Collections.unmodifiableList(scenarios);
    }

    private static List<MenuChaoticAntEntry> buildBattleTemplate(
            com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies attackerSpecies,
            com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies defenderSpecies) {
        return List.of(
                MenuChaoticAntEntry.battle(GameConstants.TYPE_SOLDIER, attackerSpecies, 2111, true,
                        GameConstants.BATTLE_LINE_INFANTRY, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_WORKER, attackerSpecies, 1111, true,
                        GameConstants.BATTLE_LINE_INFANTRY, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_MAJOR, attackerSpecies, 1161, true,
                        GameConstants.BATTLE_LINE_ARTILLERY, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_PRINCESS, attackerSpecies, 1111, true,
                        GameConstants.BATTLE_LINE_AIR_SUPPORT, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_DRONE, attackerSpecies, 1111, true,
                        GameConstants.BATTLE_LINE_AIR_SUPPORT, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_WORKER, attackerSpecies, 1171, true,
                        GameConstants.BATTLE_LINE_INFANTRY, true),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_SOLDIER, defenderSpecies, 2111, false,
                        GameConstants.BATTLE_LINE_INFANTRY, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_WORKER, defenderSpecies, 1111, false,
                        GameConstants.BATTLE_LINE_INFANTRY, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_MAJOR, defenderSpecies, 3111, false,
                        GameConstants.BATTLE_LINE_ARTILLERY, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_PRINCESS, defenderSpecies, 1111, false,
                        GameConstants.BATTLE_LINE_AIR_SUPPORT, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_DRONE, defenderSpecies, 1111, false,
                        GameConstants.BATTLE_LINE_AIR_SUPPORT, false),
                MenuChaoticAntEntry.battle(GameConstants.TYPE_SOLDIER, defenderSpecies, 1161, false,
                        GameConstants.BATTLE_LINE_INFANTRY, true));
    }
}
