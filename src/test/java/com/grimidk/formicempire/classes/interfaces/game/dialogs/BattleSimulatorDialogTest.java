package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.services.shared.CritterSkillService;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleScene;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleSceneBuilder;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleState;
import com.grimidk.formicempire.classes.entities.services.world.WarCreatureCombatService;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.BattleMode;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.BuiltSimulation;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.SideConfig;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog.SimulationConfig;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
