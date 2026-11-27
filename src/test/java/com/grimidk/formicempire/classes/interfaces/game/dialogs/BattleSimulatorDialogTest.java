package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.services.colony.HuntBattleState;
import com.grimidk.formicempire.classes.entities.services.colony.HuntCreatureCombatService.TickOutcome;
import com.grimidk.formicempire.classes.entities.services.shared.CritterSkillService;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleScene;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleSceneBuilder;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleState;
import com.grimidk.formicempire.classes.entities.services.world.WarCreatureCombatService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.BattleMode;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.BattleRunResult;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.BuiltCritterSimulation;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.BuiltSimulation;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.CritterSimulationConfig;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.RankPreset;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.SideConfig;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.SimulationConfig;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BattleSimulatorDialogTest {

    @Test
    void borderSimulationConfiguresRiverUpgradesModsAndResolves() {
        Map<AntRole, Integer> atkUnits = Map.of(
                GameConstants.ROLE_WARRIOR, 12,
                GameConstants.ROLE_ARTILLERY, 3);
        Map<AntRole, Integer> defUnits = Map.of(
                GameConstants.ROLE_WARRIOR, 8,
                GameConstants.ROLE_DEFENDER, 4);
        Set<Upgrade> atkUpgrades = Set.of(
                GameUnlocks.STAT_SKELETON,
                GameUnlocks.STAT_ACID,
                GameUnlocks.STAT_ATTACK_1,
                GameUnlocks.ASSIMILATED_TRAPJAW,
                GameUnlocks.ASSIMILATED_FIREVENOM);
        Set<Upgrade> defUpgrades = Set.of(
                GameUnlocks.STAT_SKELETON,
                GameUnlocks.STAT_ACID,
                GameUnlocks.ASSIMILATED_DOORHEAD);

        SimulationConfig config = new SimulationConfig(
                BattleMode.BORDER,
                true,
                new SideConfig(GameConstants.SPECIES_FIRE, GameConstants.BIOME_VOLCANIC, atkUnits, atkUpgrades),
                new SideConfig(GameConstants.SPECIES_TURTLE, GameConstants.BIOME_FOREST, defUnits, defUpgrades));

        BuiltSimulation sim = BattleSimulatorDialog.buildSimulation(config);
        try {
            assertNotNull(sim.war());
            assertTrue(sim.war().isActive());
            assertEquals(GameConstants.WAR_STAGE_ACTIVE_CLASH, sim.war().getStagePhase());

            WarBattleState state = WarCreatureCombatService.getState(sim.war());
            assertNotNull(state);
            assertFalse(state.isHexAssault());
            assertTrue(state.isRiverBattle());
            assertEquals(15, state.getAttacker().livingArmySize());
            assertEquals(8, state.getDefender().livingArmySize());

            WarBattleScene scene = WarBattleSceneBuilder.build(sim.world(), sim.war(), sim.world().getWarService());
            assertTrue(scene.isAvailable());
            assertEquals(GameConstants.BIOME_VOLCANIC, scene.getAttackerBiome());
            assertEquals(GameConstants.BIOME_FOREST, scene.getDefenderBiome());

            Ant trapjawWarrior = sim.attackerColony().getSoldiers().get(0);
            assertEquals(GameConstants.MOD_HEAD_TRAPJAW,
                    trapjawWarrior.getModProfile().getMod(AntModSlot.HEAD));
            assertTrue(CritterSkillService.resolveAvailableSkills(trapjawWarrior, sim.attackerColony())
                    .contains(GameConstants.SKILL_POWERFUL_BITE));

            for (int h = 0; h < 200 && sim.war().isActive()
                    && sim.war().getStagePhase() == GameConstants.WAR_STAGE_ACTIVE_CLASH; h++) {
                sim.world().runHour();
            }
            assertTrue(!sim.war().isActive() || sim.war().getStagePhase() != GameConstants.WAR_STAGE_ACTIVE_CLASH);
        } finally {
            WarCreatureCombatService.clear(sim.war());
        }
    }

    @Test
    void hexAssaultAndFullStageStartInExpectedPhaseAndResolve() {
        Map<AntRole, Integer> units = Map.of(
                GameConstants.ROLE_WARRIOR, 10,
                GameConstants.ROLE_DEFENDER, 5);
        Set<Upgrade> upgrades = Set.of(
                GameUnlocks.STAT_SKELETON,
                GameUnlocks.STAT_ACID,
                GameUnlocks.ASSIMILATED_DOORHEAD);

        SimulationConfig hexConfig = new SimulationConfig(
                BattleMode.HEX,
                false,
                new SideConfig(GameConstants.SPECIES_OMNI, GameConstants.BIOME_PLAINS, units, upgrades),
                new SideConfig(GameConstants.SPECIES_LEAFCUTTER, GameConstants.BIOME_JUNGLE, units, upgrades));

        BuiltSimulation hexSim = BattleSimulatorDialog.buildSimulation(hexConfig);
        try {
            assertEquals(GameConstants.WAR_STAGE_RESERVE_ASSAULT, hexSim.war().getStagePhase());
            WarBattleState hexState = WarCreatureCombatService.getState(hexSim.war());
            assertNotNull(hexState);
            assertTrue(hexState.isHexAssault());
            assertFalse(hexState.isRiverBattle());
            assertTrue(hexState.getDefender().hasLivingQueen());
            assertEquals(10, hexState.getAttacker().livingArmySize());
            assertEquals(16, hexState.getDefender().livingArmySize());

            for (int h = 0; h < 250 && hexSim.war().isActive(); h++) {
                hexSim.world().runHour();
            }
            assertFalse(hexSim.war().isActive());
        } finally {
            WarCreatureCombatService.clear(hexSim.war());
        }

        SimulationConfig fullConfig = new SimulationConfig(
                BattleMode.FULL_STAGE,
                false,
                new SideConfig(GameConstants.SPECIES_ARMY, GameConstants.BIOME_PLAINS, units, upgrades),
                new SideConfig(GameConstants.SPECIES_PHARAOH, GameConstants.BIOME_DESERT, units, upgrades));

        BuiltSimulation fullSim = BattleSimulatorDialog.buildSimulation(fullConfig);
        try {
            assertEquals(GameConstants.WAR_STAGE_ACTIVE_CLASH, fullSim.war().getStagePhase());
            for (int h = 0; h < 400 && fullSim.war().isActive(); h++) {
                fullSim.world().runHour();
            }
            assertFalse(fullSim.war().isActive());
        } finally {
            WarCreatureCombatService.clear(fullSim.war());
        }
    }

    @Test
    void combatUpgradesFilterCommanderCapModsAndDialogCopyResetWork() throws Exception {
        List<Upgrade> combatUpgrades = BattleSimulatorDialog.getCombatUpgrades();
        assertFalse(combatUpgrades.contains(GameUnlocks.STAT_SKELETON));
        assertFalse(combatUpgrades.contains(GameUnlocks.STAT_ACID));
        assertFalse(combatUpgrades.contains(GameUnlocks.STAT_LONGEVITY));
        assertFalse(combatUpgrades.contains(GameUnlocks.ASSIMILATED_TRAPJAW));
        assertFalse(combatUpgrades.contains(GameUnlocks.ASSIMILATED_DOORHEAD));
        assertTrue(combatUpgrades.contains(GameUnlocks.STAT_ATTACK_1));
        assertTrue(combatUpgrades.contains(GameUnlocks.ASSIMILATED_FIREVENOM));
        assertTrue(combatUpgrades.contains(GameUnlocks.SYNERGY_SUPER_VENOM));

        assertEquals(GameNumbers.COMMANDER_MAX_PER_COLONY,
                BattleSimulatorDialog.getRoleMaxCount(GameConstants.ROLE_COMMANDER));

        Map<AntRole, Integer> overCapAtk = Map.of(
                GameConstants.ROLE_WARRIOR, 5,
                GameConstants.ROLE_COMMANDER, 10);
        Map<AntRole, Integer> def = Map.of(GameConstants.ROLE_WARRIOR, 5);
        SimulationConfig modConfig = new SimulationConfig(
                BattleMode.BORDER,
                false,
                new SideConfig(GameConstants.SPECIES_OMNI, GameConstants.BIOME_PLAINS, overCapAtk,
                        Set.of(GameUnlocks.STAT_ATTACK_1),
                        GameConstants.MOD_HEAD_TRAPJAW, GameConstants.MOD_ABDOMEN_STINGER),
                new SideConfig(GameConstants.SPECIES_OMNI, GameConstants.BIOME_PLAINS, def, Set.of()));

        BuiltSimulation modSim = BattleSimulatorDialog.buildSimulation(modConfig);
        try {
            assertEquals(GameNumbers.COMMANDER_MAX_PER_COLONY,
                    modSim.attackerColony().getWarAssignedRoleCount(GameConstants.ROLE_COMMANDER));
            assertTrue(modSim.attackerColony().getQueens().size() >= 2);
            Ant moddedSoldier = modSim.attackerColony().getSoldiers().get(0);
            assertEquals(GameConstants.MOD_HEAD_TRAPJAW, moddedSoldier.getModProfile().getMod(AntModSlot.HEAD));
            assertEquals(GameConstants.MOD_ABDOMEN_STINGER, moddedSoldier.getModProfile().getMod(AntModSlot.ABDOMEN));
            List<Skill> skills =
                    CritterSkillService.resolveAvailableSkills(moddedSoldier, modSim.attackerColony());
            assertTrue(skills.contains(GameConstants.SKILL_POWERFUL_BITE));
            assertTrue(skills.contains(GameConstants.SKILL_STINGING));
        } finally {
            WarCreatureCombatService.clear(modSim.war());
        }

        SwingUtilities.invokeAndWait(() -> {
            BattleSimulatorDialog dialog = new BattleSimulatorDialog(null, null);
            try {
                SimulationConfig initial = dialog.buildConfigFromUi();
                assertEquals(BattleMode.FULL_STAGE, initial.mode());
                assertFalse(initial.riverCrossing());
                assertEquals(GameConstants.SPECIES_OMNI, initial.attacker().species());
                assertEquals(GameConstants.SPECIES_LEAFCUTTER, initial.defender().species());

                dialog.copyAttackerToDefender();
                SimulationConfig copied = dialog.buildConfigFromUi();
                assertEquals(copied.attacker().species(), copied.defender().species());
                assertEquals(copied.attacker().biome(), copied.defender().biome());
                assertEquals(copied.attacker().unitCounts(), copied.defender().unitCounts());
                assertEquals(copied.attacker().upgrades(), copied.defender().upgrades());
                assertEquals(copied.attacker().headMod(), copied.defender().headMod());
                assertEquals(copied.attacker().abdomenMod(), copied.defender().abdomenMod());

                dialog.resetToDefaults();
                SimulationConfig reset = dialog.buildConfigFromUi();
                assertEquals(BattleMode.FULL_STAGE, reset.mode());
                assertFalse(reset.riverCrossing());
                assertEquals(GameConstants.SPECIES_OMNI, reset.attacker().species());
                assertEquals(GameConstants.SPECIES_LEAFCUTTER, reset.defender().species());

                dialog.randomizeArmies();
                SimulationConfig randomized = dialog.buildConfigFromUi();
                int atkTotal = randomized.attacker().unitCounts().values().stream().mapToInt(Integer::intValue).sum();
                int defTotal = randomized.defender().unitCounts().values().stream().mapToInt(Integer::intValue).sum();
                assertTrue(atkTotal > 0);
                assertTrue(defTotal > 0);
                assertTrue(randomized.attacker().unitCounts().getOrDefault(GameConstants.ROLE_COMMANDER, 0)
                        <= GameNumbers.COMMANDER_MAX_PER_COLONY);
                assertTrue(randomized.defender().unitCounts().getOrDefault(GameConstants.ROLE_COMMANDER, 0)
                        <= GameNumbers.COMMANDER_MAX_PER_COLONY);
                BuiltSimulation randSim = BattleSimulatorDialog.buildSimulation(randomized);
                try {
                    assertTrue(randSim.war().isActive());
                } finally {
                    WarCreatureCombatService.clear(randSim.war());
                }
            } finally {
                dialog.dispose();
            }
        });

        String formattedVictory = LanguageStrings.format(LanguageStrings.BATTLE_SIM_VICTORY_FMT, "Attacker");
        assertTrue(formattedVictory.contains("Attacker"));
        assertFalse(formattedVictory.contains("{0}"));

        SimulationConfig riverConfig = new SimulationConfig(
                BattleMode.BORDER,
                true,
                new SideConfig(GameConstants.SPECIES_OMNI, GameConstants.BIOME_PLAINS,
                        Map.of(GameConstants.ROLE_WARRIOR, 5), Set.of()),
                new SideConfig(GameConstants.SPECIES_OMNI, GameConstants.BIOME_PLAINS,
                        Map.of(GameConstants.ROLE_WARRIOR, 5), Set.of()));
        BuiltSimulation riverSim = BattleSimulatorDialog.buildSimulation(riverConfig);
        try {
            WarBattleScene scene = WarBattleSceneBuilder.build(riverSim.world(), riverSim.war(), null);
            assertTrue(scene.isRiverCrossing());
        } finally {
            WarCreatureCombatService.clear(riverSim.war());
        }
    }

    @Test
    void creatorDialogHasCloseButtonAndBattleRushKeepsSpeedAcrossBattles() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BattleSimulatorDialog dialog = new BattleSimulatorDialog(null, null);
            try {
                dialog.pack();
                JButton closeButton = dialog.getCloseButton();
                assertNotNull(closeButton);
                assertTrue(closeButton.isVisible());
                assertEquals(LanguageStrings.get(LanguageStrings.UI_CLOSE), closeButton.getText());
                assertTrue(dialog.southPanel.getPreferredSize().width <= 1180,
                        "South panel buttons should fit within dialog width without clipping");
                assertTrue(dialog.isDisplayable());
                closeButton.doClick();
                assertFalse(dialog.isDisplayable());
            } finally {
                if (dialog.isDisplayable()) {
                    dialog.dispose();
                }
            }
        });

        Engine engine = new Engine();
        engine.setSpeed(GameConstants.SPEED_NORMAL);
        List<GameSpeed> seenStartingSpeeds = new ArrayList<>();
        AtomicInteger runs = new AtomicInteger(0);

        BattleSimulatorDialog.runBattleRush(
                engine,
                () -> runs.get() < 3,
                startingSpeed -> {
                    int idx = runs.getAndIncrement();
                    seenStartingSpeeds.add(startingSpeed);
                    if (idx == 0) {
                        engine.setSpeed(GameConstants.SPEED_VERY_FAST);
                        return new BattleRunResult(true, engine.getSpeed());
                    }
                    if (idx == 1) {
                        return new BattleRunResult(true, engine.getSpeed());
                    }
                    return new BattleRunResult(false, engine.getSpeed());
                });

        assertEquals(
                List.of(GameConstants.SPEED_NORMAL, GameConstants.SPEED_VERY_FAST, GameConstants.SPEED_VERY_FAST),
                seenStartingSpeeds);
        assertEquals(GameConstants.SPEED_NORMAL, engine.getSpeed());
    }

    @Test
    void critterTabBuildsAndResolvesHuntAndInvasionBattlesAndTogglesCopyButton() throws Exception {
        List<Species> combatCritters = GameConstants.getCombatCritterSpecies();
        assertTrue(combatCritters.contains(GameConstants.TYPE_COCKROACH));
        assertTrue(combatCritters.contains(GameConstants.TYPE_ANT_LION));
        assertFalse(combatCritters.contains(GameConstants.TYPE_APHID));
        assertFalse(combatCritters.contains(GameConstants.TYPE_PARASITIC_MITE));

        Map<AntRole, Integer> armyUnits = Map.of(
                GameConstants.ROLE_WARRIOR, 35,
                GameConstants.ROLE_BRUTE, 10);
        Set<Upgrade> armyUpgrades = Set.of(
                GameUnlocks.STAT_HEALTH_1,
                GameUnlocks.STAT_ATTACK_1,
                GameUnlocks.ASSIMILATED_TRAPJAW);

        CritterSimulationConfig huntConfig = new CritterSimulationConfig(
                GameConstants.BIOME_JUNGLE,
                new SideConfig(GameConstants.SPECIES_OMNI, GameConstants.BIOME_JUNGLE, armyUnits, armyUpgrades),
                GameConstants.TYPE_COCKROACH);
        BuiltCritterSimulation huntSim = BattleSimulatorDialog.buildCritterSimulation(huntConfig);
        try {
            assertEquals(HuntBattleViewPanel.BattleKind.HUNT, huntSim.battleKind());
            assertEquals(GameConstants.BIOME_JUNGLE, huntSim.world().getActiveHex().getBiome());
            HuntBattleState state = huntSim.getBattleState();
            assertNotNull(state);
            assertEquals(45, state.getHunters().size());

            for (int h = 0; h < 200 && !huntSim.isFinished(); h++) {
                huntSim.tickHour();
            }
            assertTrue(huntSim.isFinished());
            assertTrue(huntSim.getFinalOutcome() == TickOutcome.HUNTERS_WIN
                    || huntSim.getFinalOutcome() == TickOutcome.BUG_WINS);
            assertEquals(null, huntSim.getBattleState());
        } finally {
            huntSim.clear();
        }

        CritterSimulationConfig invasionConfig = new CritterSimulationConfig(
                GameConstants.BIOME_DESERT,
                new SideConfig(GameConstants.SPECIES_FIRE, GameConstants.BIOME_DESERT,
                        Map.of(GameConstants.ROLE_WARRIOR, 20), Set.of()),
                GameConstants.TYPE_ANT_LION);
        BuiltCritterSimulation invasionSim = BattleSimulatorDialog.buildCritterSimulation(invasionConfig);
        try {
            assertEquals(HuntBattleViewPanel.BattleKind.INVASION, invasionSim.battleKind());
            assertNotNull(invasionSim.getBattleState());
            for (int h = 0; h < 200 && !invasionSim.isFinished(); h++) {
                invasionSim.tickHour();
            }
            assertTrue(invasionSim.isFinished());
        } finally {
            invasionSim.clear();
        }

        SwingUtilities.invokeAndWait(() -> {
            BattleSimulatorDialog dialog = new BattleSimulatorDialog(null, null);
            try {
                assertEquals(BattleSimulatorDialog.TAB_ARMY_VS_ARMY, dialog.getSelectedTabIndex());
                assertTrue(dialog.getCopyAttackerToDefenderButton().isVisible());

                dialog.selectTab(BattleSimulatorDialog.TAB_ARMY_VS_CRITTER);
                assertEquals(BattleSimulatorDialog.TAB_ARMY_VS_CRITTER, dialog.getSelectedTabIndex());
                assertFalse(dialog.getCopyAttackerToDefenderButton().isVisible());
                assertTrue(dialog.southPanel.getPreferredSize().width <= 1180);

                dialog.resetCritterToDefaults();
                CritterSimulationConfig resetCfg = dialog.buildCritterConfigFromUi();
                assertEquals(GameConstants.BIOME_PLAINS, resetCfg.biome());
                assertEquals(GameConstants.TYPE_COCKROACH, resetCfg.critter());
                assertEquals(GameConstants.SPECIES_OMNI, resetCfg.army().species());

                dialog.randomizeCritterBattle();
                CritterSimulationConfig randCfg = dialog.buildCritterConfigFromUi();
                assertNotNull(randCfg.biome());
                assertNotNull(randCfg.critter());
                assertTrue(combatCritters.contains(randCfg.critter()));
                int total = randCfg.army().unitCounts().values().stream().mapToInt(Integer::intValue).sum();
                assertTrue(total > 0);

                BuiltCritterSimulation built = BattleSimulatorDialog.buildCritterSimulation(randCfg);
                try {
                    assertNotNull(built.getBattleState());
                } finally {
                    built.clear();
                }

                dialog.selectTab(BattleSimulatorDialog.TAB_ARMY_VS_ARMY);
                dialog.applyAttackerRankPreset(RankPreset.EMPIRE);
                dialog.applyDefenderRankPreset(RankPreset.DUCHY);
                assertTrue(dialog.getWarWinChanceLabel().getText().contains("%"));

                dialog.selectTab(BattleSimulatorDialog.TAB_ARMY_VS_CRITTER);
                dialog.applyCritterArmyRankPreset(RankPreset.EMPIRE);
                assertTrue(dialog.getCritterPartyLimitLabel().getText().contains("250"));
                assertTrue(dialog.getCritterWinChanceLabel().getText().contains("%"));
            } finally {
                dialog.dispose();
            }
        });

        SideConfig duchy = BattleSimulatorDialog.buildPresetConfig(
                RankPreset.DUCHY, GameConstants.SPECIES_OMNI, GameConstants.BIOME_PLAINS);
        SideConfig kingdom = BattleSimulatorDialog.buildPresetConfig(
                RankPreset.KINGDOM, GameConstants.SPECIES_OMNI, GameConstants.BIOME_PLAINS);
        SideConfig empire = BattleSimulatorDialog.buildPresetConfig(
                RankPreset.EMPIRE, GameConstants.SPECIES_OMNI, GameConstants.BIOME_PLAINS);

        assertEquals(100, BattleSimulatorDialog.countConfiguredUnits(duchy));
        assertEquals(100, BattleSimulatorDialog.countConfiguredUnits(kingdom));
        assertEquals(250, BattleSimulatorDialog.countConfiguredUnits(empire));
        assertEquals(100, BattleSimulatorDialog.computeCritterPartyCap(duchy));
        assertEquals(250, BattleSimulatorDialog.computeCritterPartyCap(empire));

        assertEquals(0.5f, BattleSimulatorDialog.estimateArmyWinChance(
                new SimulationConfig(BattleMode.FULL_STAGE, false, duchy, duchy)), 0.01f);
        assertTrue(BattleSimulatorDialog.estimateArmyWinChance(
                new SimulationConfig(BattleMode.FULL_STAGE, false, empire, duchy)) > 0.75f);
        assertTrue(BattleSimulatorDialog.estimateArmyWinChance(
                new SimulationConfig(BattleMode.FULL_STAGE, false, duchy, empire)) < 0.25f);

        for (Species critter : GameConstants.getCombatCritterSpecies()) {
            float duchyWin = BattleSimulatorDialog.estimateCritterWinChance(
                    new CritterSimulationConfig(GameConstants.BIOME_PLAINS, duchy, critter));
            float kingdomWin = BattleSimulatorDialog.estimateCritterWinChance(
                    new CritterSimulationConfig(GameConstants.BIOME_PLAINS, kingdom, critter));
            float empireWin = BattleSimulatorDialog.estimateCritterWinChance(
                    new CritterSimulationConfig(GameConstants.BIOME_PLAINS, empire, critter));
            assertTrue(kingdomWin >= duchyWin, critter.getName() + " kingdom < duchy");
            assertTrue(empireWin > 0.90f, critter.getName() + " should be a breeze for Empire: " + empireWin);
        }
        assertTrue(BattleSimulatorDialog.estimateCritterWinChance(
                new CritterSimulationConfig(GameConstants.BIOME_PLAINS, duchy, GameConstants.TYPE_TARANTULA)) < 0.20f);
    }

    @Test
    void allTwelveRankPresetsUseIconOnlyButtonsAndGigaPresetWorksAcrossTabs() throws Exception {
        RankPreset[] allPresets = RankPreset.values();
        assertEquals(12, allPresets.length);
        assertEquals(RankPreset.COLONY, allPresets[0]);
        assertEquals(RankPreset.GIGA, allPresets[allPresets.length - 1]);

        SideConfig giga = BattleSimulatorDialog.buildPresetConfig(
                RankPreset.GIGA, GameConstants.SPECIES_OMNI, GameConstants.BIOME_PLAINS);
        assertTrue(BattleSimulatorDialog.countConfiguredUnits(giga) > 3000);
        assertTrue(BattleSimulatorDialog.estimateCritterWinChance(
                new CritterSimulationConfig(GameConstants.BIOME_PLAINS, giga, GameConstants.TYPE_TARANTULA)) > 0.95f);

        SwingUtilities.invokeAndWait(() -> {
            BattleSimulatorDialog dialog = new BattleSimulatorDialog(null, null);
            try {
                dialog.pack();
                assertTrue(dialog.getWidth() <= 1180 || dialog.southPanel.getPreferredSize().width <= 1180);

                Map<RankPreset, JButton> buttons = dialog.getAttackerRankPresetButtons();
                assertEquals(12, buttons.size());
                for (RankPreset preset : allPresets) {
                    JButton btn = buttons.get(preset);
                    assertNotNull(btn, "Missing button for " + preset);
                    assertEquals("", btn.getText());
                    assertNotNull(btn.getIcon(), "Missing icon for " + preset);
                    assertEquals(preset.getLabel(), btn.getToolTipText());
                    assertTrue(btn.getPreferredSize().width >= AssetStyles.MIN_CONTROL_HIT_SIZE);
                    assertTrue(btn.getPreferredSize().height >= AssetStyles.MIN_CONTROL_HIT_SIZE);
                }

                LanguageStrings.setLanguage("es");
                dialog.refreshDialog();
                for (RankPreset preset : allPresets) {
                    JButton btn = buttons.get(preset);
                    assertEquals("", btn.getText());
                    assertEquals(preset.getLabel(), btn.getToolTipText());
                }

                dialog.selectTab(BattleSimulatorDialog.TAB_ARMY_VS_ARMY);
                dialog.applyAttackerRankPreset(RankPreset.GIGA);
                dialog.applyDefenderRankPreset(RankPreset.COLONY);
                assertTrue(dialog.getWarWinChanceLabel().getText().contains("%"));

                dialog.selectTab(BattleSimulatorDialog.TAB_ARMY_VS_CRITTER);
                dialog.applyCritterArmyRankPreset(RankPreset.GIGA);
                assertTrue(dialog.getCritterWinChanceLabel().getText().contains("%"));
            } finally {
                LanguageStrings.setLanguage("en");
                dialog.dispose();
            }
        });
    }
}
