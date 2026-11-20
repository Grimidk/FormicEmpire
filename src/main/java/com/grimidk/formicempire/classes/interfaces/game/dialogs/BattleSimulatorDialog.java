package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntMod;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.AntModService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleState;
import com.grimidk.formicempire.classes.entities.services.world.WarCreatureCombatService;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.audio.SfxService;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.DiceRollResource;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class BattleSimulatorDialog extends ZeroDialog {

    public enum BattleMode {
        BORDER(LanguageStrings.BATTLE_SIM_MODE_BORDER),
        HEX(LanguageStrings.BATTLE_SIM_MODE_HEX),
        FULL_STAGE(LanguageStrings.BATTLE_SIM_MODE_FULL_STAGE);

        private final String labelKey;

        BattleMode(String labelKey) {
            this.labelKey = labelKey;
        }

        public String getLabel() {
            return LanguageStrings.get(labelKey);
        }

        @Override
        public String toString() {
            return getLabel();
        }
    }

    public record UnitSpec(
            AntRole role,
            AntClass antClass,
            Upgrade roleUpgrade,
            BattleLine battleLine,
            Constant labelSource,
            int defaultCount,
            int maxCount) {
        public String displayName() {
            return labelSource != null ? labelSource.getName() : role.getName();
        }

        public ImageIcon icon() {
            return labelSource != null ? labelSource.getIcon() : role.getIcon();
        }
    }

    public record SideConfig(
            AntSpecies species,
            Biome biome,
            Map<AntRole, Integer> unitCounts,
            Set<Upgrade> upgrades,
            AntMod headMod,
            AntMod abdomenMod) {
        public SideConfig(
                AntSpecies species,
                Biome biome,
                Map<AntRole, Integer> unitCounts,
                Set<Upgrade> upgrades) {
            this(species, biome, unitCounts, upgrades, GameConstants.MOD_HEAD_NONE, GameConstants.MOD_ABDOMEN_NONE);
        }
    }

    public record SimulationConfig(
            BattleMode mode,
            boolean riverCrossing,
            SideConfig attacker,
            SideConfig defender) {
    }

    public record BuiltSimulation(
            World world,
            Dynasty attacker,
            Dynasty defender,
            Colony attackerColony,
            Colony defenderColony,
            War war) {
    }

    record BattleRunResult(boolean completed, GameSpeed endingSpeed) {
    }

    private static final Biome[] SIMULATOR_BIOMES = new Biome[] {
            GameConstants.BIOME_PLAINS,
            GameConstants.BIOME_FOREST,
            GameConstants.BIOME_JUNGLE,
            GameConstants.BIOME_SWAMP,
            GameConstants.BIOME_DESERT,
            GameConstants.BIOME_TUNDRA,
            GameConstants.BIOME_TAIGA,
            GameConstants.BIOME_MOUNTAIN,
            GameConstants.BIOME_VOLCANIC,
            GameConstants.BIOME_URBAN,
            GameConstants.BIOME_BEACH,
            GameConstants.BIOME_CLIFFS,
            GameConstants.BIOME_WETLANDS,
            GameConstants.BIOME_MANGROVE,
            GameConstants.BIOME_PRAIRIE
    };

    private static final int DEFAULT_ROLE_MAX_COUNT = 5000;

    private static final List<UnitSpec> UNIT_SPECS = List.of(
            new UnitSpec(GameConstants.ROLE_WARRIOR, GameConstants.CLASS_SOLDIER, GameUnlocks.ROLE_WARRIOR,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.ROLE_WARRIOR, 20, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_DEFENDER, GameConstants.CLASS_SOLDIER, GameUnlocks.ROLE_DEFENDER,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.ROLE_DEFENDER, 10, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_BOMBER, GameConstants.CLASS_SOLDIER, GameUnlocks.ROLE_BOMBER,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.ROLE_BOMBER, 0, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_BRUTE, GameConstants.CLASS_MAJOR, GameUnlocks.ROLE_BRUTE,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.ROLE_BRUTE, 0, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_SIEGE, GameConstants.CLASS_MAJOR, GameUnlocks.ROLE_SIEGE,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.ROLE_SIEGE, 0, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_CAPTAIN, GameConstants.CLASS_PRINCESS, GameUnlocks.ROLE_CAPTAIN,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.ROLE_CAPTAIN, 0, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_MILITIA, GameConstants.CLASS_WORKER, GameUnlocks.ROLE_MILITIA,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.ROLE_MILITIA, 0, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_FORAGER, GameConstants.CLASS_WORKER, GameUnlocks.ROLE_FORAGER,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.CLASS_WORKER, 0, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_LAYER, GameConstants.CLASS_QUEEN, GameUnlocks.ROLE_LAYER,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.CLASS_QUEEN, 1, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_ARTILLERY, GameConstants.CLASS_MAJOR, GameUnlocks.ROLE_ARTILLERY,
                    GameConstants.BATTLE_LINE_ARTILLERY, GameConstants.ROLE_ARTILLERY, 5, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_POTTER, GameConstants.CLASS_WORKER, GameUnlocks.ROLE_POTTER,
                    GameConstants.BATTLE_LINE_ARTILLERY, GameConstants.ROLE_POTTER, 0, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_COMMANDER, GameConstants.CLASS_QUEEN, GameUnlocks.ROLE_COMMANDER,
                    GameConstants.BATTLE_LINE_ARTILLERY, GameConstants.ROLE_COMMANDER, 0, GameNumbers.COMMANDER_MAX_PER_COLONY),
            new UnitSpec(GameConstants.ROLE_AIR_SUPPORT, GameConstants.CLASS_PRINCESS, GameUnlocks.ROLE_AIR_SUPPORT,
                    GameConstants.BATTLE_LINE_AIR_SUPPORT, GameConstants.ROLE_AIR_SUPPORT, 0, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_AIR_BOMBER, GameConstants.CLASS_PRINCESS, GameUnlocks.ROLE_AIR_BOMBER,
                    GameConstants.BATTLE_LINE_AIR_SUPPORT, GameConstants.ROLE_AIR_BOMBER, 0, DEFAULT_ROLE_MAX_COUNT),
            new UnitSpec(GameConstants.ROLE_CARRIER, GameConstants.CLASS_MAJOR, GameUnlocks.ROLE_CARRIER,
                    GameConstants.BATTLE_LINE_INFANTRY, GameConstants.ROLE_CARRIER, 0, DEFAULT_ROLE_MAX_COUNT)
    );

    private static final List<Upgrade> COMBAT_UPGRADES = List.of(
            GameUnlocks.STAT_HEALTH_1,
            GameUnlocks.STAT_HEALTH_2,
            GameUnlocks.STAT_ATTACK_1,
            GameUnlocks.STAT_ATTACK_2,
            GameUnlocks.STAT_DEFENSE_1,
            GameUnlocks.STAT_DEFENSE_2,
            GameUnlocks.STAT_ATTACK_SPEED_1,
            GameUnlocks.STAT_RAFTING_1,
            GameUnlocks.STAT_RAFTING_2,
            GameUnlocks.ASSIMILATED_FIREVENOM,
            GameUnlocks.ASSIMILATED_DEADLYVENOM,
            GameUnlocks.SYNERGY_SUPER_VENOM,
            GameUnlocks.ASSIMILATED_FASTBITE,
            GameUnlocks.ASSIMILATED_JUMPING,
            GameUnlocks.ASSIMILATED_SWARMING,
            GameUnlocks.ASSIMILATED_HEATRESIST,
            GameUnlocks.ASSIMILATED_ACIDSPIT,
            GameUnlocks.ASSIMILATED_SELFDESTRUCT,
            GameUnlocks.SYNERGY_CORROSIVE_BOMBS,
            GameUnlocks.ASSIMILATED_RAFTING
    );

    static List<Upgrade> getCombatUpgrades() {
        return COMBAT_UPGRADES;
    }

    static int getRoleMaxCount(AntRole role) {
        if (role == null) {
            return DEFAULT_ROLE_MAX_COUNT;
        }
        for (UnitSpec spec : UNIT_SPECS) {
            if (spec.role() == role) {
                return spec.maxCount();
            }
        }
        return DEFAULT_ROLE_MAX_COUNT;
    }

    private final MainFrame mainFrame;
    private final Engine engine;
    private final AntSpecies defaultAttackerSpecies;
    private final AntSpecies defaultDefenderSpecies;

    private final JComboBox<BattleMode> modeCombo = new JComboBox<>(BattleMode.values());
    private final JCheckBox riverCheckBox = new JCheckBox(LanguageStrings.get(LanguageStrings.BATTLE_SIM_RIVER));
    private final JComboBox<Biome> attackerBiomeCombo = new JComboBox<>(SIMULATOR_BIOMES);
    private final JComboBox<Biome> defenderBiomeCombo = new JComboBox<>(SIMULATOR_BIOMES);
    private final SideConfigPanel attackerPanel;
    private final SideConfigPanel defenderPanel;
    private final JButton resetDefaultsButton = new JButton(LanguageStrings.get(LanguageStrings.BATTLE_SIM_RESET_DEFAULTS));
    private final JButton copyAttackerToDefenderButton = new JButton(LanguageStrings.get(LanguageStrings.BATTLE_SIM_COPY_ATK_TO_DEF));
    private final JButton randomizeArmiesButton = new JButton(LanguageStrings.get(LanguageStrings.BATTLE_SIM_RANDOMIZE));
    private final JButton battleRushButton = new JButton(LanguageStrings.get(LanguageStrings.BATTLE_SIM_RUSH));
    private final JButton startBattleButton = new JButton(LanguageStrings.get(LanguageStrings.BATTLE_SIM_START));
    private final JButton closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));

    public BattleSimulatorDialog(Window owner, Engine engine) {
        super(owner, LanguageStrings.UI_BATTLE_SIMULATOR, new Dimension(1180, 780));
        this.mainFrame = owner instanceof MainFrame mf ? mf : null;
        this.engine = engine;
        if (this.mainFrame != null && !MainFrame.CARD_GAME.equals(this.mainFrame.getCurrentCard())) {
            this.mainFrame.setMainMenuLoaded(false);
        }

        List<AntSpecies> allSpecies = GameConstants.getSpecies();
        this.defaultAttackerSpecies = GameConstants.SPECIES_OMNI;
        this.defaultDefenderSpecies = allSpecies.size() > 1 ? allSpecies.get(1) : this.defaultAttackerSpecies;
        attackerPanel = new SideConfigPanel(
                LanguageStrings.get(LanguageStrings.BATTLE_SIM_ATTACKER), this.defaultAttackerSpecies);
        defenderPanel = new SideConfigPanel(
                LanguageStrings.get(LanguageStrings.BATTLE_SIM_DEFENDER), this.defaultDefenderSpecies);

        buildLayout();
        southPanel.removeAll();
        southPanel.setLayout(new BorderLayout(10, 0));
        southPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        randomizeArmiesButton.setIcon(DiceRollResource.getFaceIcon(DiceRollResource.faceAtStep(0), 16));
        randomizeArmiesButton.setIconTextGap(6);

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionButtonsPanel.setOpaque(false);
        AssetStyles.styleButton(resetDefaultsButton);
        AssetStyles.styleButton(copyAttackerToDefenderButton);
        AssetStyles.styleButton(randomizeArmiesButton);
        AssetStyles.styleButton(battleRushButton);
        AssetStyles.styleButton(startBattleButton);
        actionButtonsPanel.add(resetDefaultsButton);
        actionButtonsPanel.add(copyAttackerToDefenderButton);
        actionButtonsPanel.add(randomizeArmiesButton);
        actionButtonsPanel.add(battleRushButton);
        actionButtonsPanel.add(startBattleButton);

        JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeButtonPanel.setOpaque(false);
        AssetStyles.styleButton(closeButton);
        closeButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_BACK_TO_MENU));
        closeButton.addActionListener(e -> requestClose());
        closeButtonPanel.add(closeButton);

        southPanel.add(actionButtonsPanel, BorderLayout.CENTER);
        southPanel.add(closeButtonPanel, BorderLayout.EAST);

        resetDefaultsButton.addActionListener(e -> resetToDefaults());
        copyAttackerToDefenderButton.addActionListener(e -> copyAttackerToDefender());
        randomizeArmiesButton.addActionListener(e -> randomizeArmies());
        battleRushButton.addActionListener(e -> launchBattleRush());
        startBattleButton.addActionListener(e -> launchBattle());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (mainFrame != null && !MainFrame.CARD_GAME.equals(mainFrame.getCurrentCard())) {
                    mainFrame.setMainMenuLoaded(true);
                }
            }
        });

        UiDialogUtils.prepareDialog(this, owner);
    }

    JButton getCloseButton() {
        return closeButton;
    }

    @Override
    protected boolean shouldDelegateEscapeToGamePanel() {
        return false;
    }

    @Override
    public void showDialog() {
        refreshDialog();
        disableFocusTraversal(this);
        Window owner = getOwner();
        Rectangle bounds = (owner != null && owner.isShowing())
                ? owner.getBounds()
                : GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(bounds);
        if (owner instanceof MainFrame mf) {
            mf.applyGameCursors(this);
        }
        setVisible(true);
    }

    @Override
    protected void refreshDialog() {
        riverCheckBox.setText(LanguageStrings.get(LanguageStrings.BATTLE_SIM_RIVER));
        resetDefaultsButton.setText(LanguageStrings.get(LanguageStrings.BATTLE_SIM_RESET_DEFAULTS));
        copyAttackerToDefenderButton.setText(LanguageStrings.get(LanguageStrings.BATTLE_SIM_COPY_ATK_TO_DEF));
        randomizeArmiesButton.setText(LanguageStrings.get(LanguageStrings.BATTLE_SIM_RANDOMIZE));
        battleRushButton.setText(LanguageStrings.get(LanguageStrings.BATTLE_SIM_RUSH));
        startBattleButton.setText(LanguageStrings.get(LanguageStrings.BATTLE_SIM_START));
        closeButton.setText(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        closeButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_BACK_TO_MENU));
        attackerPanel.setTitle(LanguageStrings.get(LanguageStrings.BATTLE_SIM_ATTACKER));
        defenderPanel.setTitle(LanguageStrings.get(LanguageStrings.BATTLE_SIM_DEFENDER));
    }

    void randomizeArmies() {
        BattleMode[] modes = BattleMode.values();
        modeCombo.setSelectedItem(modes[GameRandom.nextInt(modes.length)]);
        riverCheckBox.setSelected(GameRandom.nextBoolean());
        attackerBiomeCombo.setSelectedItem(SIMULATOR_BIOMES[GameRandom.nextInt(SIMULATOR_BIOMES.length)]);
        defenderBiomeCombo.setSelectedItem(SIMULATOR_BIOMES[GameRandom.nextInt(SIMULATOR_BIOMES.length)]);
        List<AntSpecies> allSpecies = GameConstants.getSpecies();
        attackerPanel.randomize(allSpecies);
        defenderPanel.randomize(allSpecies);
        DiceRollResource.triggerRollAnimation(randomizeArmiesButton, 16);
    }

    void resetToDefaults() {
        modeCombo.setSelectedItem(BattleMode.FULL_STAGE);
        riverCheckBox.setSelected(false);
        attackerBiomeCombo.setSelectedItem(GameConstants.BIOME_PLAINS);
        defenderBiomeCombo.setSelectedItem(GameConstants.BIOME_PLAINS);
        attackerPanel.resetToDefaults();
        defenderPanel.resetToDefaults();
    }

    void copyAttackerToDefender() {
        Biome atkBiome = (Biome) attackerBiomeCombo.getSelectedItem();
        defenderBiomeCombo.setSelectedItem(atkBiome);
        defenderPanel.applyConfig(attackerPanel.readSideConfig(atkBiome));
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(AssetStyles.UI_BG_PRIMARY);
        root.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel conditionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        conditionsPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        conditionsPanel.setBorder(BorderFactory.createCompoundBorder(
                AssetStyles.INTERNAL_BORDER,
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel modeLabel = new JLabel(LanguageStrings.get(LanguageStrings.BATTLE_SIM_MODE) + ":");
        modeLabel.setFont(AssetStyles.FONT_BOLD);
        modeLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        AssetStyles.styleComboBox(modeCombo);
        modeCombo.setSelectedItem(BattleMode.FULL_STAGE);

        AssetStyles.styleCheckBox(riverCheckBox);

        JLabel atkBiomeLabel = new JLabel(LanguageStrings.get(LanguageStrings.BATTLE_SIM_ATTACKER)
                + " " + LanguageStrings.get(LanguageStrings.BATTLE_SIM_BIOME) + ":");
        atkBiomeLabel.setFont(AssetStyles.FONT_BOLD);
        atkBiomeLabel.setForeground(AssetStyles.FONT_COLOR);
        configureBiomeCombo(attackerBiomeCombo);

        JLabel defBiomeLabel = new JLabel(LanguageStrings.get(LanguageStrings.BATTLE_SIM_DEFENDER)
                + " " + LanguageStrings.get(LanguageStrings.BATTLE_SIM_BIOME) + ":");
        defBiomeLabel.setFont(AssetStyles.FONT_BOLD);
        defBiomeLabel.setForeground(AssetStyles.FONT_COLOR);
        configureBiomeCombo(defenderBiomeCombo);

        conditionsPanel.add(modeLabel);
        conditionsPanel.add(modeCombo);
        conditionsPanel.add(riverCheckBox);
        conditionsPanel.add(Box.createHorizontalStrut(8));
        conditionsPanel.add(atkBiomeLabel);
        conditionsPanel.add(attackerBiomeCombo);
        conditionsPanel.add(defBiomeLabel);
        conditionsPanel.add(defenderBiomeCombo);

        JPanel sidesPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        sidesPanel.setOpaque(false);
        sidesPanel.add(attackerPanel);
        sidesPanel.add(defenderPanel);

        root.add(conditionsPanel, BorderLayout.NORTH);
        root.add(sidesPanel, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private void launchBattle() {
        runSingleBattle(true, null, true);
    }

    private void launchBattleRush() {
        runBattleRush(engine, this::isDisplayable, startingSpeed -> {
            randomizeArmies();
            return runSingleBattle(false, startingSpeed, false);
        });
    }

    static void runBattleRush(
            Engine engine,
            BooleanSupplier keepGoing,
            Function<GameSpeed, BattleRunResult> battleRunner) {
        GameSpeed initialSpeed = engine != null ? engine.getSpeed() : null;
        GameSpeed currentSpeed = initialSpeed;
        try {
            while (keepGoing != null && keepGoing.getAsBoolean()) {
                BattleRunResult result = battleRunner.apply(currentSpeed);
                if (result != null && result.endingSpeed() != null) {
                    currentSpeed = result.endingSpeed();
                }
                if (result == null || !result.completed()) {
                    break;
                }
            }
        } finally {
            if (engine != null && initialSpeed != null) {
                engine.setSpeed(initialSpeed);
            }
        }
    }

    private BattleRunResult runSingleBattle(
            boolean showOutcomeFanfare,
            GameSpeed startingSpeed,
            boolean restoreSpeedAfterBattle) {
        SimulationConfig config = buildConfigFromUi();
        BuiltSimulation sim = buildSimulation(config);

        World previousWorld = engine != null ? engine.getWorld() : null;
        boolean previousPaused = engine == null || engine.isPaused();
        GameSpeed previousSpeed = engine != null ? engine.getSpeed() : GameConstants.SPEED_NORMAL;
        GameSpeed endingSpeed = startingSpeed != null ? startingSpeed : previousSpeed;

        if (engine != null) {
            engine.pauseEngine();
            engine.getSema().acquireUninterruptibly();
            try {
                engine.setWorld(sim.world());
                if (startingSpeed != null) {
                    engine.setSpeed(startingSpeed);
                }
            } finally {
                engine.getSema().release();
            }
        }

        WarBattleDialog battleDialog = new WarBattleDialog(this, sim.war(), engine);
        Runnable simTickListener = () -> {
            if (config.mode() == BattleMode.BORDER
                    && sim.war().isActive()
                    && sim.war().getStagePhase() != GameConstants.WAR_STAGE_ACTIVE_CLASH) {
                int winnerId = sim.war().getStageAttackerDynastyId();
                WarCreatureCombatService.clear(sim.war());
                sim.world().getWarService().concludeWar(sim.war(), winnerId, LanguageStrings.WAR_CONCLUSION_DEFEAT);
            }
            SwingUtilities.invokeLater(() -> {
                if (battleDialog.isDisplayable()) {
                    battleDialog.liveUpdate();
                }
            });
        };

        if (engine != null) {
            engine.addTickListener(simTickListener);
            if (engine.getState() == Thread.State.NEW) {
                engine.start();
            }
            engine.resumeEngine();
        }

        boolean battleCompleted = false;
        try {
            battleDialog.showDialog();
            battleCompleted = !sim.war().isActive();
        } finally {
            if (battleDialog.isDisplayable()) {
                battleDialog.dispose();
            }
            if (engine != null) {
                engine.pauseEngine();
                endingSpeed = engine.getSpeed();
                engine.removeTickListener(simTickListener);
                engine.getSema().acquireUninterruptibly();
                try {
                    WarCreatureCombatService.clear(sim.war());
                    engine.setWorld(previousWorld);
                } finally {
                    engine.getSema().release();
                }
                if (restoreSpeedAfterBattle && previousSpeed != null) {
                    engine.setSpeed(previousSpeed);
                }
                if (!previousPaused && previousWorld != null) {
                    engine.resumeEngine();
                }
            } else {
                WarCreatureCombatService.clear(sim.war());
            }
        }
        if (battleCompleted && showOutcomeFanfare) {
            showBattleOutcomeFanfare(sim);
        }
        return new BattleRunResult(battleCompleted, endingSpeed);
    }

    private void showBattleOutcomeFanfare(BuiltSimulation sim) {
        if (sim == null || sim.war() == null) {
            return;
        }
        SfxService.play(SoundEffects.ASSIMILATION);
        int winnerId = sim.war().getWinnerDynastyId();
        String headline;
        ImageIcon winnerIcon = null;
        if (winnerId == sim.attacker().getId()) {
            String sideLabel = LanguageStrings.get(LanguageStrings.BATTLE_SIM_ATTACKER)
                    + " (" + sim.attacker().getSpecies().getName() + ")";
            headline = LanguageStrings.format(LanguageStrings.BATTLE_SIM_VICTORY_FMT, sideLabel);
            winnerIcon = sim.attacker().getSpecies().getIcon();
        } else if (winnerId == sim.defender().getId()) {
            String sideLabel = LanguageStrings.get(LanguageStrings.BATTLE_SIM_DEFENDER)
                    + " (" + sim.defender().getSpecies().getName() + ")";
            headline = LanguageStrings.format(LanguageStrings.BATTLE_SIM_VICTORY_FMT, sideLabel);
            winnerIcon = sim.defender().getSpecies().getIcon();
        } else {
            headline = LanguageStrings.get(LanguageStrings.BATTLE_SIM_DRAW);
        }

        JLabel messageLabel = new JLabel(headline);
        messageLabel.setFont(AssetStyles.FONT_BOLD);
        messageLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        UiOptionPane.showOptionDialog(
                this,
                messageLabel,
                LanguageStrings.get(LanguageStrings.BATTLE_SIM_VICTORY_TITLE),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                winnerIcon,
                new Object[] { LanguageStrings.get(LanguageStrings.UI_OK) },
                LanguageStrings.get(LanguageStrings.UI_OK));
    }

    SimulationConfig buildConfigFromUi() {
        BattleMode mode = (BattleMode) modeCombo.getSelectedItem();
        if (mode == null) {
            mode = BattleMode.FULL_STAGE;
        }
        SideConfig atk = attackerPanel.readSideConfig((Biome) attackerBiomeCombo.getSelectedItem());
        SideConfig def = defenderPanel.readSideConfig((Biome) defenderBiomeCombo.getSelectedItem());
        return new SimulationConfig(mode, riverCheckBox.isSelected(), atk, def);
    }

    public static BuiltSimulation buildSimulation(SimulationConfig config) {
        BattleMode mode = config.mode() != null ? config.mode() : BattleMode.FULL_STAGE;
        SideConfig atkCfg = config.attacker();
        SideConfig defCfg = config.defender();

        World simWorld = new World();
        simWorld.initializeBlankGrid(4, 4, false, 1.0);
        simWorld.setSpectatorMode(true);

        Hex attackerHex = simWorld.getHexAt(0, 0);
        Hex defenderHex = simWorld.getHexAt(1, 0);
        if (attackerHex == null || defenderHex == null) {
            attackerHex = new Hex();
            attackerHex.setQ(0);
            attackerHex.setR(0);
            defenderHex = new Hex();
            defenderHex.setQ(1);
            defenderHex.setR(0);
            attackerHex.setNorthEast(defenderHex);
            defenderHex.setSouthWest(attackerHex);
            simWorld.getHexes().add(attackerHex);
            simWorld.getHexes().add(defenderHex);
        }

        Biome atkBiome = atkCfg != null && atkCfg.biome() != null ? atkCfg.biome() : GameConstants.BIOME_PLAINS;
        Biome defBiome = defCfg != null && defCfg.biome() != null ? defCfg.biome() : GameConstants.BIOME_PLAINS;
        configureSimHex(attackerHex, atkBiome);
        configureSimHex(defenderHex, defBiome);

        if (config.riverCrossing()) {
            int dirToDef = attackerHex.directionTo(defenderHex);
            if (dirToDef >= 0) {
                attackerHex.setRiverEdge(dirToDef, true);
            }
            int dirToAtk = defenderHex.directionTo(attackerHex);
            if (dirToAtk >= 0) {
                defenderHex.setRiverEdge(dirToAtk, true);
            }
        }

        AntSpecies atkSpecies = atkCfg != null && atkCfg.species() != null ? atkCfg.species() : GameConstants.SPECIES_OMNI;
        AntSpecies defSpecies = defCfg != null && defCfg.species() != null ? defCfg.species() : GameConstants.SPECIES_LEAFCUTTER;

        Dynasty attackerDynasty = new Dynasty(simWorld.allocateDynastyId(),
                LanguageStrings.get(LanguageStrings.BATTLE_SIM_ATTACKER) + " " + atkSpecies.getName(),
                true, atkSpecies);
        Dynasty defenderDynasty = new Dynasty(simWorld.allocateDynastyId(),
                LanguageStrings.get(LanguageStrings.BATTLE_SIM_DEFENDER) + " " + defSpecies.getName(),
                true, defSpecies);

        Colony attackerColony = new Colony(simWorld.getNextColonyId(),
                LanguageStrings.get(LanguageStrings.BATTLE_SIM_ATTACKER), false);
        Colony defenderColony = new Colony(simWorld.getNextColonyId(),
                LanguageStrings.get(LanguageStrings.BATTLE_SIM_DEFENDER), false);

        attachSimColony(attackerDynasty, attackerColony, attackerHex);
        attachSimColony(defenderDynasty, defenderColony, defenderHex);
        simWorld.getDynastys().add(attackerDynasty);
        simWorld.getDynastys().add(defenderDynasty);
        simWorld.markColonizedHexIndexDirty();

        boolean needsAttackerQueen = mode == BattleMode.FULL_STAGE;
        boolean needsDefenderQueen = mode == BattleMode.HEX || mode == BattleMode.FULL_STAGE;
        boolean requireBorderForce = mode != BattleMode.HEX;

        populateSide(attackerDynasty, attackerColony, atkCfg, needsAttackerQueen, requireBorderForce);
        populateSide(defenderDynasty, defenderColony, defCfg, needsDefenderQueen, requireBorderForce);

        War war = simWorld.getWarService().beginWar(attackerDynasty, defenderDynasty);
        if (war != null) {
            war.setTotalStages(1);
            if (mode == BattleMode.HEX) {
                war.setAggressorStagesCaptured(0);
                war.setDefenderStagesCaptured(0);
                war.setStageProgress(0f);
                war.setStagePhase(GameConstants.WAR_STAGE_RESERVE_ASSAULT);
                war.setRedeployHoursRemaining(0);
                war.setStageAttackerDynastyId(attackerDynasty.getId());
                war.setContestedColonyId(defenderColony.getId());
                war.setDeployedActiveDefender(0);
                WarCreatureCombatService.clear(war);
                WarBattleState hexState = WarCreatureCombatService.startHexBattle(
                        war, attackerDynasty, defenderColony, Math.max(1, simWorld.getDay()));
                if (hexState != null) {
                    hexState.setRiverBattle(config.riverCrossing());
                }
            } else {
                WarBattleState battleState = WarCreatureCombatService.getState(war);
                if (battleState != null) {
                    battleState.setRiverBattle(config.riverCrossing());
                }
            }
        }

        simWorld.changeActiveHex(defenderHex);
        return new BuiltSimulation(simWorld, attackerDynasty, defenderDynasty, attackerColony, defenderColony, war);
    }

    private static void configureSimHex(Hex hex, Biome biome) {
        hex.setBiome(biome);
        hex.setElevation(1);
        hex.setContinentId(1);
        hex.setIsland(false);
    }

    private static void attachSimColony(Dynasty dynasty, Colony colony, Hex hex) {
        colony.setAutomationEnabled(false);
        colony.setAutoBuildEnabled(false);
        colony.setAge(10);
        colony.setPlants(1_000_000);
        colony.setMushrooms(1_000_000);
        colony.setProtein(1_000_000);
        colony.setWater(1_000_000);
        dynasty.addColony(colony);
        dynasty.setCapital(colony);
        colony.setDynasty(dynasty);
        hex.setColony(colony);
    }

    private static void populateSide(Dynasty dynasty, Colony colony, SideConfig cfg,
            boolean ensureQueen, boolean requireBorderForce) {
        dynasty.unlockUpgrade(GameUnlocks.TYPE_EGG);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_MAJOR);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_QUEEN);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_SYNERGY);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_ASSIMILATION);

        Set<Upgrade> upgrades = cfg != null && cfg.upgrades() != null
                ? new LinkedHashSet<>(cfg.upgrades())
                : new LinkedHashSet<>();
        if (upgrades.contains(GameUnlocks.SYNERGY_SUPER_VENOM)) {
            upgrades.add(GameUnlocks.ASSIMILATED_FIREVENOM);
            upgrades.add(GameUnlocks.ASSIMILATED_DEADLYVENOM);
        }
        if (upgrades.contains(GameUnlocks.SYNERGY_CORROSIVE_BOMBS)) {
            upgrades.add(GameUnlocks.ASSIMILATED_SELFDESTRUCT);
            upgrades.add(GameUnlocks.ASSIMILATED_ACIDSPIT);
        }
        for (Upgrade upgrade : upgrades) {
            dynasty.unlockUpgrade(upgrade);
        }

        AntMod headMod = cfg != null && cfg.headMod() != null ? cfg.headMod() : GameConstants.MOD_HEAD_NONE;
        AntMod abdomenMod = cfg != null && cfg.abdomenMod() != null ? cfg.abdomenMod() : GameConstants.MOD_ABDOMEN_NONE;
        if (headMod.getRequiredUpgrade() != null) {
            dynasty.unlockUpgrade(headMod.getRequiredUpgrade());
        }
        if (abdomenMod.getRequiredUpgrade() != null) {
            dynasty.unlockUpgrade(abdomenMod.getRequiredUpgrade());
        }

        Map<AntRole, Integer> counts = cfg != null && cfg.unitCounts() != null
                ? new LinkedHashMap<>(cfg.unitCounts())
                : new LinkedHashMap<>();

        int totalUnits = 0;
        int borderUnits = 0;
        for (UnitSpec spec : UNIT_SPECS) {
            int count = Math.min(spec.maxCount(), Math.max(0, counts.getOrDefault(spec.role(), 0)));
            counts.put(spec.role(), count);
            totalUnits += count;
            if (count > 0 && GameConstants.isBorderBattleRole(spec.role())) {
                borderUnits += count;
            }
        }
        if (totalUnits == 0 || (requireBorderForce && borderUnits == 0)) {
            counts.merge(GameConstants.ROLE_WARRIOR, 1, Integer::sum);
        }
        if (counts.getOrDefault(GameConstants.ROLE_COMMANDER, 0) > 0
                && counts.getOrDefault(GameConstants.ROLE_LAYER, 0) < 1) {
            counts.put(GameConstants.ROLE_LAYER, 1);
        }
        if (ensureQueen && counts.getOrDefault(GameConstants.ROLE_LAYER, 0) <= 0
                && counts.getOrDefault(GameConstants.ROLE_COMMANDER, 0) <= 0) {
            counts.put(GameConstants.ROLE_LAYER, 1);
        }

        for (UnitSpec spec : UNIT_SPECS) {
            int count = Math.min(spec.maxCount(), Math.max(0, counts.getOrDefault(spec.role(), 0)));
            if (count <= 0) {
                continue;
            }
            if (spec.roleUpgrade() != null) {
                dynasty.unlockUpgrade(spec.roleUpgrade());
            }
            colony.getWarAssignedRoleCounts().put(spec.role(), count);
            if (!GameConstants.isWarEconomyExclusiveRole(spec.role())) {
                colony.getPeaceAssignedRoleCounts().put(spec.role(), count);
            }
            for (int i = 0; i < count; i++) {
                Ant ant = new Ant(colony, spec.antClass());
                ant.setRole(spec.role());
                applySimulationMods(ant, colony, spec.role(), upgrades, headMod, abdomenMod);
                colony.getAntsByClass(spec.antClass()).add(ant);
            }
        }

        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
    }

    private static void applySimulationMods(Ant ant, Colony colony, AntRole role, Set<Upgrade> upgrades,
            AntMod headMod, AntMod abdomenMod) {
        if (!AntModService.isEligibleClass(ant.getAntClass())) {
            return;
        }
        int head = headMod != null && !headMod.isNone() ? headMod.getDigit() : AntMod.DIGIT_NONE;
        int abdomen = abdomenMod != null && !abdomenMod.isNone() ? abdomenMod.getDigit() : AntMod.DIGIT_NONE;

        if (role == GameConstants.ROLE_DEFENDER) {
            head = GameConstants.MOD_HEAD_DOORHEAD.getDigit();
        } else if (head == AntMod.DIGIT_NONE) {
            if (upgrades.contains(GameUnlocks.ASSIMILATED_TRAPJAW)) {
                head = GameConstants.MOD_HEAD_TRAPJAW.getDigit();
            } else if (upgrades.contains(GameUnlocks.ASSIMILATED_FARSIGHT)) {
                head = GameConstants.MOD_HEAD_FARSIGHT.getDigit();
            } else if (upgrades.contains(GameUnlocks.ASSIMILATED_FARMING)) {
                head = GameConstants.MOD_HEAD_LEAFCUTTER.getDigit();
            }
        }

        if (role == GameConstants.ROLE_POTTER) {
            abdomen = GameConstants.MOD_ABDOMEN_HONEYPOT.getDigit();
        } else if (abdomen == AntMod.DIGIT_NONE && upgrades.contains(GameUnlocks.ASSIMILATED_STINGING)) {
            abdomen = GameConstants.MOD_ABDOMEN_STINGER.getDigit();
        }

        if (head != AntMod.DIGIT_NONE || abdomen != AntMod.DIGIT_NONE) {
            ant.setModProfile(AntModProfile.of(head, AntMod.DIGIT_NONE, abdomen, AntMod.DIGIT_NONE));
        }
        AntModService.applyModStats(ant, colony);
        ant.setHealth(ant.getMaxHealth());
    }

    private static void configureBiomeCombo(JComboBox<Biome> combo) {
        AssetStyles.styleComboBox(combo);
        combo.setSelectedItem(GameConstants.BIOME_PLAINS);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Biome biome) {
                    label.setText(biome.getName());
                    label.setIcon(biome.getIcon());
                    label.setIconTextGap(6);
                }
                return label;
            }
        });
    }

    private static final class SideConfigPanel extends JPanel {
        private final AntSpecies defaultSpecies;
        private final JLabel titleLabel;
        private final JComboBox<AntSpecies> speciesCombo;
        private final JComboBox<AntMod> headModCombo = new JComboBox<>(
                GameConstants.getModsForSlot(AntModSlot.HEAD).toArray(new AntMod[0]));
        private final JComboBox<AntMod> abdomenModCombo = new JComboBox<>(
                GameConstants.getModsForSlot(AntModSlot.ABDOMEN).toArray(new AntMod[0]));
        private final Map<AntRole, JSpinner> unitSpinners = new LinkedHashMap<>();
        private final Map<Upgrade, JCheckBox> upgradeCheckBoxes = new LinkedHashMap<>();
        private final Set<Upgrade> lastAutoSpeciesTraits = new LinkedHashSet<>();
        private AntMod lastAutoHeadMod = GameConstants.MOD_HEAD_NONE;
        private AntMod lastAutoAbdomenMod = GameConstants.MOD_ABDOMEN_NONE;
        private boolean suppressSpeciesSync = false;

        SideConfigPanel(String title, AntSpecies defaultSpecies) {
            super(new BorderLayout(8, 8));
            this.defaultSpecies = defaultSpecies;
            setBackground(AssetStyles.UI_BG_SECONDARY);
            setBorder(BorderFactory.createCompoundBorder(
                    AssetStyles.INTERNAL_BORDER,
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));

            JPanel topPanel = new JPanel(new BorderLayout(8, 6));
            topPanel.setOpaque(false);

            titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setFont(AssetStyles.FONT_BOLD.deriveFont(16f));
            titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
            topPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel speciesRow = new JPanel(new BorderLayout(8, 0));
            speciesRow.setOpaque(false);
            JLabel speciesLabel = new JLabel(LanguageStrings.get(LanguageStrings.BATTLE_SIM_SPECIES) + ":");
            speciesLabel.setFont(AssetStyles.FONT_BOLD);
            speciesLabel.setForeground(AssetStyles.FONT_COLOR);

            List<AntSpecies> speciesList = GameConstants.getSpecies();
            speciesCombo = new JComboBox<>(speciesList.toArray(new AntSpecies[0]));
            AssetStyles.styleComboBox(speciesCombo);
            speciesCombo.setSelectedItem(defaultSpecies);
            speciesCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    if (value instanceof AntSpecies sp) {
                        label.setText(sp.getName());
                        label.setIcon(sp.getIcon());
                        label.setIconTextGap(6);
                    }
                    return label;
                }
            });
            speciesCombo.addActionListener(e -> syncSpeciesDefaults());

            speciesRow.add(speciesLabel, BorderLayout.WEST);
            speciesRow.add(speciesCombo, BorderLayout.CENTER);
            topPanel.add(speciesRow, BorderLayout.CENTER);

            JPanel centerContent = new JPanel();
            centerContent.setOpaque(false);
            centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));

            centerContent.add(buildUnitsSection());
            centerContent.add(Box.createVerticalStrut(10));
            centerContent.add(buildModificationsSection());
            centerContent.add(Box.createVerticalStrut(10));
            centerContent.add(buildUpgradesSection());

            JScrollPane scrollPane = new JScrollPane(centerContent);
            AssetStyles.styleScrollPane(scrollPane);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            add(topPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);

            syncSpeciesDefaults();
        }

        void setTitle(String title) {
            titleLabel.setText(title);
        }

        void resetToDefaults() {
            suppressSpeciesSync = true;
            try {
                lastAutoSpeciesTraits.clear();
                lastAutoHeadMod = GameConstants.MOD_HEAD_NONE;
                lastAutoAbdomenMod = GameConstants.MOD_ABDOMEN_NONE;
                speciesCombo.setSelectedItem(defaultSpecies);
                for (UnitSpec spec : UNIT_SPECS) {
                    JSpinner spinner = unitSpinners.get(spec.role());
                    if (spinner != null) {
                        spinner.setValue(spec.defaultCount());
                    }
                }
                headModCombo.setSelectedItem(GameConstants.MOD_HEAD_NONE);
                abdomenModCombo.setSelectedItem(GameConstants.MOD_ABDOMEN_NONE);
                setAllUpgrades(false);
            } finally {
                suppressSpeciesSync = false;
            }
            syncSpeciesDefaults();
        }

        void randomize(List<AntSpecies> allSpecies) {
            AntSpecies chosenSpecies = (allSpecies != null && !allSpecies.isEmpty())
                    ? allSpecies.get(GameRandom.nextInt(allSpecies.size()))
                    : defaultSpecies;

            Map<AntRole, Integer> counts = new LinkedHashMap<>();
            int total = 0;
            for (UnitSpec spec : UNIT_SPECS) {
                int count;
                if (spec.maxCount() <= 5) {
                    count = GameRandom.nextInt(spec.maxCount() + 1);
                } else if (GameRandom.nextInt(100) < 45) {
                    count = 0;
                } else {
                    count = Math.min(spec.maxCount(), 4 + GameRandom.nextInt(157));
                }
                counts.put(spec.role(), count);
                total += count;
            }
            if (total == 0) {
                counts.put(GameConstants.ROLE_WARRIOR, 40 + GameRandom.nextInt(121));
            }

            Set<Upgrade> selectedUpgrades = new LinkedHashSet<>();
            for (Upgrade upgrade : COMBAT_UPGRADES) {
                if (GameRandom.nextInt(10) < 3) {
                    selectedUpgrades.add(upgrade);
                }
            }

            List<AntMod> headMods = GameConstants.getModsForSlot(AntModSlot.HEAD);
            AntMod headMod = headMods.isEmpty()
                    ? GameConstants.MOD_HEAD_NONE
                    : headMods.get(GameRandom.nextInt(headMods.size()));
            List<AntMod> abdomenMods = GameConstants.getModsForSlot(AntModSlot.ABDOMEN);
            AntMod abdomenMod = abdomenMods.isEmpty()
                    ? GameConstants.MOD_ABDOMEN_NONE
                    : abdomenMods.get(GameRandom.nextInt(abdomenMods.size()));

            applyConfig(new SideConfig(chosenSpecies, null, counts, selectedUpgrades, headMod, abdomenMod));
        }

        void applyConfig(SideConfig config) {
            if (config == null) {
                return;
            }
            suppressSpeciesSync = true;
            try {
                lastAutoSpeciesTraits.clear();
                lastAutoHeadMod = GameConstants.MOD_HEAD_NONE;
                lastAutoAbdomenMod = GameConstants.MOD_ABDOMEN_NONE;
                if (config.species() != null) {
                    speciesCombo.setSelectedItem(config.species());
                }
                Map<AntRole, Integer> counts = config.unitCounts() != null ? config.unitCounts() : Map.of();
                for (UnitSpec spec : UNIT_SPECS) {
                    JSpinner spinner = unitSpinners.get(spec.role());
                    if (spinner != null) {
                        int val = Math.min(spec.maxCount(), Math.max(0, counts.getOrDefault(spec.role(), 0)));
                        spinner.setValue(val);
                    }
                }
                headModCombo.setSelectedItem(config.headMod() != null ? config.headMod() : GameConstants.MOD_HEAD_NONE);
                abdomenModCombo.setSelectedItem(config.abdomenMod() != null ? config.abdomenMod() : GameConstants.MOD_ABDOMEN_NONE);
                Set<Upgrade> selectedUpgrades = config.upgrades() != null ? config.upgrades() : Set.of();
                for (Map.Entry<Upgrade, JCheckBox> entry : upgradeCheckBoxes.entrySet()) {
                    entry.getValue().setSelected(selectedUpgrades.contains(entry.getKey()));
                }
            } finally {
                suppressSpeciesSync = false;
            }
        }

        private JComponent buildUnitsSection() {
            JPanel section = new JPanel(new BorderLayout(4, 4));
            section.setOpaque(false);
            section.setBorder(AssetStyles.INTERNAL_BORDER);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            JLabel heading = new JLabel(LanguageStrings.get(LanguageStrings.BATTLE_SIM_UNITS));
            heading.setFont(AssetStyles.FONT_BOLD);
            heading.setForeground(AssetStyles.FONT_COLOR_HEADER);
            JButton clearUnitsBtn = new JButton(LanguageStrings.get(LanguageStrings.BATTLE_SIM_CLEAR_ALL));
            AssetStyles.styleCompactButton(clearUnitsBtn);
            clearUnitsBtn.addActionListener(e -> {
                for (JSpinner spinner : unitSpinners.values()) {
                    spinner.setValue(0);
                }
            });
            header.add(heading, BorderLayout.WEST);
            header.add(clearUnitsBtn, BorderLayout.EAST);

            JPanel rowsPanel = new JPanel(new GridBagLayout());
            rowsPanel.setOpaque(false);
            rowsPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 4, 2, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            int row = 0;

            for (BattleLine line : GameConstants.getBattleLines()) {
                JLabel lineHeader = new JLabel(line.getName(), line.getIcon(), SwingConstants.LEFT);
                lineHeader.setFont(AssetStyles.FONT_BOLD);
                lineHeader.setForeground(AssetStyles.FONT_COLOR_HEADER);
                lineHeader.setIconTextGap(6);
                gbc.gridx = 0;
                gbc.gridy = row++;
                gbc.gridwidth = 2;
                rowsPanel.add(lineHeader, gbc);
                gbc.gridwidth = 1;

                for (UnitSpec spec : UNIT_SPECS) {
                    if (spec.battleLine() != line) {
                        continue;
                    }
                    JLabel unitLabel = new JLabel(spec.displayName(), spec.icon(), SwingConstants.LEFT);
                    unitLabel.setFont(AssetStyles.FONT_NORMAL);
                    unitLabel.setForeground(AssetStyles.FONT_COLOR);
                    unitLabel.setIconTextGap(6);

                    int step = spec.maxCount() <= 5 ? 1 : 5;
                    JSpinner spinner = new JSpinner(new SpinnerNumberModel(spec.defaultCount(), 0, spec.maxCount(), step));
                    AssetStyles.styleSpinner(spinner);
                    spinner.setPreferredSize(AssetStyles.preferredSpinnerSize(82));
                    unitSpinners.put(spec.role(), spinner);

                    gbc.gridx = 0;
                    gbc.gridy = row;
                    gbc.weightx = 1.0;
                    rowsPanel.add(unitLabel, gbc);

                    gbc.gridx = 1;
                    gbc.weightx = 0.0;
                    rowsPanel.add(spinner, gbc);
                    row++;
                }
            }

            section.add(header, BorderLayout.NORTH);
            section.add(rowsPanel, BorderLayout.CENTER);
            return section;
        }

        private JComponent buildModificationsSection() {
            JPanel section = new JPanel(new BorderLayout(4, 4));
            section.setOpaque(false);
            section.setBorder(AssetStyles.INTERNAL_BORDER);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            JLabel heading = new JLabel(LanguageStrings.get(LanguageStrings.BATTLE_SIM_MODIFICATIONS));
            heading.setFont(AssetStyles.FONT_BOLD);
            heading.setForeground(AssetStyles.FONT_COLOR_HEADER);
            header.add(heading, BorderLayout.WEST);

            JPanel body = new JPanel(new GridBagLayout());
            body.setOpaque(false);
            body.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 4, 2, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            configureModCombo(headModCombo, GameConstants.MOD_HEAD_NONE);
            configureModCombo(abdomenModCombo, GameConstants.MOD_ABDOMEN_NONE);

            JLabel headLabel = new JLabel(LanguageStrings.get(LanguageStrings.HATCH_SUBTYPE_HEAD_SECTION) + ":");
            headLabel.setFont(AssetStyles.FONT_NORMAL);
            headLabel.setForeground(AssetStyles.FONT_COLOR);
            JLabel abdomenLabel = new JLabel(LanguageStrings.get(LanguageStrings.HATCH_SUBTYPE_ABDOMEN_SECTION) + ":");
            abdomenLabel.setFont(AssetStyles.FONT_NORMAL);
            abdomenLabel.setForeground(AssetStyles.FONT_COLOR);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            body.add(headLabel, gbc);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            body.add(headModCombo, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0.0;
            body.add(abdomenLabel, gbc);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            body.add(abdomenModCombo, gbc);

            section.add(header, BorderLayout.NORTH);
            section.add(body, BorderLayout.CENTER);
            return section;
        }

        private static void configureModCombo(JComboBox<AntMod> combo, AntMod defaultMod) {
            AssetStyles.styleComboBox(combo);
            combo.setSelectedItem(defaultMod);
            combo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    if (value instanceof AntMod mod) {
                        label.setText(mod.getName());
                        label.setIcon(mod.getIcon());
                        label.setIconTextGap(6);
                        label.setToolTipText(mod.getDesc());
                    }
                    return label;
                }
            });
        }

        private JComponent buildUpgradesSection() {
            JPanel section = new JPanel(new BorderLayout(4, 4));
            section.setOpaque(false);
            section.setBorder(AssetStyles.INTERNAL_BORDER);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            JLabel heading = new JLabel(LanguageStrings.get(LanguageStrings.BATTLE_SIM_UPGRADES));
            heading.setFont(AssetStyles.FONT_BOLD);
            heading.setForeground(AssetStyles.FONT_COLOR_HEADER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            actions.setOpaque(false);
            JButton selectAllBtn = new JButton(LanguageStrings.get(LanguageStrings.BATTLE_SIM_SELECT_ALL));
            AssetStyles.styleCompactButton(selectAllBtn);
            selectAllBtn.addActionListener(e -> setAllUpgrades(true));
            JButton clearAllBtn = new JButton(LanguageStrings.get(LanguageStrings.BATTLE_SIM_CLEAR_ALL));
            AssetStyles.styleCompactButton(clearAllBtn);
            clearAllBtn.addActionListener(e -> setAllUpgrades(false));
            actions.add(selectAllBtn);
            actions.add(clearAllBtn);

            header.add(heading, BorderLayout.WEST);
            header.add(actions, BorderLayout.EAST);

            JPanel grid = new JPanel(new GridLayout(0, 2, 4, 2));
            grid.setOpaque(false);
            grid.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));

            for (Upgrade upgrade : COMBAT_UPGRADES) {
                JCheckBox box = new JCheckBox();
                AssetStyles.styleCheckBox(box);
                box.setToolTipText(upgrade.getDescription());
                box.setSelected(false);
                upgradeCheckBoxes.put(upgrade, box);

                ImageIcon rawIcon = upgrade.getIcon();
                ImageIcon scaledIcon = null;
                if (rawIcon != null && rawIcon.getImage() != null) {
                    scaledIcon = new ImageIcon(rawIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                }
                JLabel label = new JLabel(upgrade.getTitleName(), scaledIcon, SwingConstants.LEFT);
                label.setFont(AssetStyles.FONT_NORMAL);
                label.setForeground(AssetStyles.FONT_COLOR);
                label.setIconTextGap(6);
                label.setToolTipText(upgrade.getDescription());
                AssetStyles.markClickable(label);
                label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (box.isEnabled()) {
                            box.doClick();
                        }
                    }
                });

                JPanel row = new JPanel(new BorderLayout(4, 0));
                row.setOpaque(false);
                row.add(box, BorderLayout.WEST);
                row.add(label, BorderLayout.CENTER);
                grid.add(row);
            }

            section.add(header, BorderLayout.NORTH);
            section.add(grid, BorderLayout.CENTER);
            return section;
        }

        private void setAllUpgrades(boolean selected) {
            for (JCheckBox box : upgradeCheckBoxes.values()) {
                box.setSelected(selected);
            }
        }

        private void syncSpeciesDefaults() {
            if (suppressSpeciesSync) {
                return;
            }
            for (Upgrade prev : lastAutoSpeciesTraits) {
                JCheckBox box = upgradeCheckBoxes.get(prev);
                if (box != null) {
                    box.setSelected(false);
                }
            }
            lastAutoSpeciesTraits.clear();

            if (headModCombo.getSelectedItem() == lastAutoHeadMod) {
                headModCombo.setSelectedItem(GameConstants.MOD_HEAD_NONE);
            }
            if (abdomenModCombo.getSelectedItem() == lastAutoAbdomenMod) {
                abdomenModCombo.setSelectedItem(GameConstants.MOD_ABDOMEN_NONE);
            }
            lastAutoHeadMod = GameConstants.MOD_HEAD_NONE;
            lastAutoAbdomenMod = GameConstants.MOD_ABDOMEN_NONE;

            AntSpecies sp = (AntSpecies) speciesCombo.getSelectedItem();
            if (sp == null) {
                return;
            }
            for (Upgrade u : sp.getBaseUpgrades()) {
                JCheckBox box = upgradeCheckBoxes.get(u);
                if (box != null) {
                    box.setSelected(true);
                    lastAutoSpeciesTraits.add(u);
                }
            }
            for (AntMod mod : GameConstants.getModsForSlot(AntModSlot.HEAD)) {
                if (!mod.isNone() && mod.getRequiredUpgrade() != null
                        && sp.getBaseUpgrades().contains(mod.getRequiredUpgrade())) {
                    headModCombo.setSelectedItem(mod);
                    lastAutoHeadMod = mod;
                    break;
                }
            }
            for (AntMod mod : GameConstants.getModsForSlot(AntModSlot.ABDOMEN)) {
                if (!mod.isNone() && mod.getRequiredUpgrade() != null
                        && sp.getBaseUpgrades().contains(mod.getRequiredUpgrade())) {
                    abdomenModCombo.setSelectedItem(mod);
                    lastAutoAbdomenMod = mod;
                    break;
                }
            }
        }

        SideConfig readSideConfig(Biome biome) {
            AntSpecies species = (AntSpecies) speciesCombo.getSelectedItem();
            Map<AntRole, Integer> counts = new LinkedHashMap<>();
            for (Map.Entry<AntRole, JSpinner> entry : unitSpinners.entrySet()) {
                counts.put(entry.getKey(), ((Number) entry.getValue().getValue()).intValue());
            }
            Set<Upgrade> selectedUpgrades = new LinkedHashSet<>();
            for (Map.Entry<Upgrade, JCheckBox> entry : upgradeCheckBoxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedUpgrades.add(entry.getKey());
                }
            }
            AntMod headMod = (AntMod) headModCombo.getSelectedItem();
            AntMod abdomenMod = (AntMod) abdomenModCombo.getSelectedItem();
            return new SideConfig(species, biome, counts, selectedUpgrades, headMod, abdomenMod);
        }
    }
}
