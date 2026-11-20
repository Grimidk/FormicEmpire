package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.SpeciesDiet;
import com.grimidk.formicempire.classes.constants.dynasty.DynastyTitle;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyNamingService;
import com.grimidk.formicempire.classes.entities.world.FaultLine;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.TectonicPlate;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.maps.SavedMapData;
import com.grimidk.formicempire.classes.infrasctructure.maps.SavedMapService;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.game.rendering.HexGridMesh;
import com.grimidk.formicempire.classes.interfaces.game.rendering.HexMapGeometry;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.EdgeTriggeredKeyBindings;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiResourceLoader;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WorldBuilderDialog extends JDialog {

    private enum Tool {
        BRUSH, LINE, FILL, ERASER, PICKER
    }

    private static final int STAGE_SETUP = 0;
    private static final int STAGE_PLATES = 1;
    private static final int STAGE_LAND = 2;
    private static final int STAGE_BIOMES = 3;
    private static final int STAGE_DYNASTIES = 4;
    private static final int TOOLBAR_BTN_HEIGHT = 28;
    private static final Dimension ICON_BTN_SIZE = new Dimension(32, TOOLBAR_BTN_HEIGHT);

    private static final Color[] PLATE_COLORS = new Color[] {
            new Color(225, 75, 75), new Color(75, 140, 235), new Color(85, 205, 85),
            new Color(235, 180, 50), new Color(175, 85, 235), new Color(50, 210, 210),
            new Color(235, 120, 50), new Color(130, 185, 85), new Color(215, 85, 160),
            new Color(140, 140, 160), new Color(195, 195, 80), new Color(105, 205, 155)
    };

    private static final String[] BIOME_CATEGORY_NAMES = new String[] {
            "Aquatic & Coastal",
            "Temperate & Lush",
            "Cold & Highland",
            "Arid & Extreme"
    };

    private static final Biome[][] BIOME_CATEGORIES = new Biome[][] {
            {
                    GameConstants.BIOME_ABYSS,
                    GameConstants.BIOME_DEEP_OCEAN,
                    GameConstants.BIOME_OCEAN,
                    GameConstants.BIOME_SHALLOW_OCEAN,
                    GameConstants.BIOME_LAKE,
                    GameConstants.BIOME_BEACH,
                    GameConstants.BIOME_CLIFFS,
                    GameConstants.BIOME_SWAMP,
                    GameConstants.BIOME_WETLANDS,
                    GameConstants.BIOME_MANGROVE
            },
            {
                    GameConstants.BIOME_PLAINS,
                    GameConstants.BIOME_PRAIRIE,
                    GameConstants.BIOME_FOREST,
                    GameConstants.BIOME_JUNGLE,
                    GameConstants.BIOME_URBAN
            },
            {
                    GameConstants.BIOME_TAIGA,
                    GameConstants.BIOME_TUNDRA,
                    GameConstants.BIOME_ICE_FIELD,
                    GameConstants.BIOME_MOUNTAIN,
                    GameConstants.BIOME_PEAK
            },
            {
                    GameConstants.BIOME_DESERT,
                    GameConstants.BIOME_BARREN,
                    GameConstants.BIOME_BURNT_FOREST,
                    GameConstants.BIOME_VOLCANIC
            }
    };

    private static class WorldSnapshot {
        final int stage;
        final int[] plateIds;
        final int[] elevations;
        final Biome[] biomes;
        final Colony[] colonies;
        final FaultLine.FaultType[] faultTypes;
        final int[] faultIntensities;
        final int[] continentIds;
        final boolean[] islands;
        final int[] waterDistances;
        final boolean[] coastals;
        final boolean[][] riverEdges;
        final List<Dynasty> dynasties;
        final Map<Dynasty, List<Colony>> dynastyColonies;
        final Map<Dynasty, Colony> dynastyCapitals;
        final List<TectonicPlate> plates;
        final List<FaultLine> faults;

        WorldSnapshot(int stage, World world) {
            this.stage = stage;
            List<Hex> hexList = world.getHexes();
            int n = hexList != null ? hexList.size() : 0;
            this.plateIds = new int[n];
            this.elevations = new int[n];
            this.biomes = new Biome[n];
            this.colonies = new Colony[n];
            this.faultTypes = new FaultLine.FaultType[n];
            this.faultIntensities = new int[n];
            this.continentIds = new int[n];
            this.islands = new boolean[n];
            this.waterDistances = new int[n];
            this.coastals = new boolean[n];
            this.riverEdges = new boolean[n][6];
            for (int i = 0; i < n; i++) {
                Hex h = hexList.get(i);
                if (h != null) {
                    this.plateIds[i] = h.getPlateId();
                    this.elevations[i] = h.getElevation();
                    this.biomes[i] = h.getBiome();
                    this.colonies[i] = h.getColony();
                    this.faultTypes[i] = h.getFaultType();
                    this.faultIntensities[i] = h.getFaultIntensity();
                    this.continentIds[i] = h.getContinentId();
                    this.islands[i] = h.isIsland();
                    this.waterDistances[i] = h.getWaterDistance();
                    this.coastals[i] = h.isCoastal();
                    for (int d = 0; d < 6; d++) {
                        this.riverEdges[i][d] = h.hasRiverEdge(d);
                    }
                }
            }
            this.dynasties = new ArrayList<>(world.getDynastys());
            this.dynastyColonies = new LinkedHashMap<>();
            this.dynastyCapitals = new LinkedHashMap<>();
            for (Dynasty d : this.dynasties) {
                if (d != null) {
                    this.dynastyColonies.put(d, new ArrayList<>(d.getColonies()));
                    this.dynastyCapitals.put(d, d.getCapital());
                }
            }
            this.plates = new ArrayList<>(world.getTectonicPlates());
            this.faults = new ArrayList<>(world.getFaultLines());
        }

        void restore(World world) {
            List<Hex> hexList = world.getHexes();
            int n = Math.min(hexList.size(), plateIds.length);
            Map<Integer, TectonicPlate> rebuiltPlates = new LinkedHashMap<>();
            for (TectonicPlate p : plates) {
                if (p != null) {
                    rebuiltPlates.put(p.getId(), new TectonicPlate(
                            p.getId(), p.getCenterQ(), p.getCenterR(),
                            p.getDriftAngle(), p.getDriftSpeed(), p.isOceanic()));
                }
            }
            for (int i = 0; i < n; i++) {
                Hex h = hexList.get(i);
                if (h != null) {
                    h.setPlateId(plateIds[i]);
                    h.setElevation(elevations[i]);
                    h.setBiome(biomes[i]);
                    h.setColony(colonies[i]);
                    h.setFaultType(faultTypes[i]);
                    h.setFaultIntensity(faultIntensities[i]);
                    h.setContinentId(continentIds[i]);
                    h.setIsland(islands[i]);
                    h.setWaterDistance(waterDistances[i]);
                    h.setCoastal(coastals[i]);
                    for (int d = 0; d < 6; d++) {
                        h.setRiverEdge(d, riverEdges[i][d]);
                    }
                    TectonicPlate tp = rebuiltPlates.get(plateIds[i]);
                    if (tp != null) {
                        tp.addHex(h);
                    }
                }
            }
            world.getDynastys().clear();
            for (Dynasty d : dynasties) {
                if (d != null) {
                    for (Colony existingCol : new ArrayList<>(d.getColonies())) {
                        d.removeColony(existingCol);
                    }
                    List<Colony> cols = dynastyColonies.get(d);
                    if (cols != null) {
                        for (Colony col : cols) {
                            d.addColony(col);
                        }
                    }
                    d.setCapital(dynastyCapitals.get(d));
                    world.registerDynasty(d);
                }
            }
            world.clearTectonicPlatesAndFaults();
            for (TectonicPlate tp : rebuiltPlates.values()) {
                world.addTectonicPlate(tp);
            }
            for (FaultLine fl : faults) {
                if (fl != null) {
                    world.addFaultLine(fl);
                }
            }
            world.markColonizedHexIndexDirty();
            world.classifyLandmasses();
        }
    }

    private final MainFrame mainFrame;
    private final Engine engine;

    private World world;
    private String mapName = "Custom World";
    private int currentStage = STAGE_SETUP;

    private Tool currentTool = Tool.BRUSH;
    private int brushRadius = 1;

    private FaultLine.FaultType selectedFaultType = FaultLine.FaultType.CONVERGENT;
    private int selectedFaultIntensity = 2;

    private boolean raiseElevation = true;
    private Integer selectedExactElevation = null;
    private Biome selectedBiome = GameConstants.BIOME_PLAINS;
    private int selectedBiomeCategory = 1;

    private SpeciesDiet selectedDynastyDiet = GameConstants.DIET_FUNGIVOROUS;
    private AntSpecies selectedDynastySpecies = GameConstants.SPECIES_OMNI;
    private int dynastyAction = 0;
    private final DynastyNamingService dynastyNamingService = new DynastyNamingService();

    private final Deque<WorldSnapshot> undoStack = new ArrayDeque<>();
    private final WorldSnapshot[] stageBaselineSnapshots = new WorldSnapshot[5];
    private final Set<Hex> currentStrokeHexes = new HashSet<>();
    private boolean currentStrokeSnapshotPushed = false;
    private boolean strokeModifiedPlates = false;
    private Hex lineStartHex = null;
    private Hex lineEndHex = null;

    private final JPanel centerCardPanel = new JPanel(new CardLayout());
    private final JPanel setupPanel = new JPanel(new GridBagLayout());
    private final WorldCanvas canvas = new WorldCanvas();

    private final JButton[] stageNavButtons = new JButton[5];
    private final JToggleButton[] toolButtons = new JToggleButton[5];
    private final JButton[] brushSizeButtons = new JButton[3];
    private final JButton backButton = new JButton();
    private final JButton nextButton = new JButton();
    private final JButton saveButton = new JButton();
    private final JButton playButton = new JButton();
    private JButton undoBtn;
    private JButton autoGenBtn;
    private JButton resetStepButton;

    private final JPanel dynamicToolbarPanel = new JPanel(new BorderLayout());
    private final JPanel dynamicPalettePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));

    private final JTextField nameField = new JTextField(16);
    private final JTextField heightField = new JTextField(String.valueOf(GameNumbers.WORLD_RECT_HEIGHT), 16);
    private final JLabel dimensionsDisplay = new JLabel("Dimensions: 128 x 64 (8,192 hexes)");
    private final JTextField iceCapMarginField = new JTextField("18", 16);
    private final JLabel statusMessageLabel = new JLabel(" ");

    public WorldBuilderDialog(MainFrame owner, Engine engine) {
        this(owner, engine, null);
    }

    public WorldBuilderDialog(MainFrame owner, Engine engine, SavedMapData initialMapData) {
        super(owner, LanguageStrings.get(LanguageStrings.UI_WORLD_BUILDER), true);
        this.mainFrame = owner;
        this.engine = engine;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                requestClose();
            }
        });

        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(maxBounds);
        setResizable(true);

        int initialStage = STAGE_SETUP;
        if (initialMapData != null) {
            this.world = SavedMapService.buildWorldFromMapData(initialMapData);
            if (initialMapData.mapName != null && !initialMapData.mapName.trim().isEmpty()) {
                this.mapName = initialMapData.mapName.trim();
            }
            heightField.setText(String.valueOf(Math.max(6, this.world.getWorldHeight())));
            int icePct = this.world.hasIceCaps()
                    ? (int) Math.round((1.0 - this.world.getIceCapPolarLimit()) * 100.0)
                    : 0;
            iceCapMarginField.setText(String.valueOf(Math.max(0, Math.min(50, icePct))));
            initialStage = SavedMapService.inferLastStage(initialMapData, this.world);
            WorldSnapshot loadedSnap = new WorldSnapshot(initialStage, this.world);
            int cap = Math.max(STAGE_PLATES, initialStage);
            for (int s = STAGE_PLATES; s <= cap; s++) {
                stageBaselineSnapshots[s] = loadedSnap;
            }
        } else {
            this.world = new World();
            this.world.initializeBlankGrid(
                    GameNumbers.WORLD_RECT_WIDTH,
                    GameNumbers.WORLD_RECT_HEIGHT,
                    GameNumbers.WORLD_DEFAULT_HAS_ICE_CAPS,
                    GameNumbers.WORLD_DEFAULT_ICECAP_POLAR_LIMIT);
            recalculatePlatesFromFaults();
            stageBaselineSnapshots[STAGE_PLATES] = new WorldSnapshot(STAGE_PLATES, this.world);
        }

        initUI();
        UiDialogUtils.prepareDialog(this, owner);
        applyToolbarStyles();
        setStage(initialStage);
        installKeyBindings();
    }

    private void requestClose() {
        int choice = UiOptionPane.showConfirmDialog(
                this,
                LanguageStrings.get(LanguageStrings.UI_WORLD_CONFIRM_SAVE_CLOSE),
                LanguageStrings.get(LanguageStrings.UI_WORLD_SAVE_MAP),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            if (saveCurrentWorld(false)) {
                dispose();
            }
        } else if (choice == JOptionPane.NO_OPTION) {
            dispose();
        }
    }

    private void installKeyBindings() {
        EdgeTriggeredKeyBindings.bind(
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
                getRootPane().getActionMap(),
                KeyEvent.VK_ESCAPE,
                "world_builder_close",
                this::requestClose);

        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();
        int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutMask), "builder_undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "builder_undo_ctrl");
        am.put("builder_undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performUndo();
            }
        });
        am.put("builder_undo_ctrl", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performUndo();
            }
        });
    }

    private void pushUndoSnapshot() {
        if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
            return;
        }
        for (int s = currentStage + 1; s < stageBaselineSnapshots.length; s++) {
            stageBaselineSnapshots[s] = null;
        }
        if (undoStack.size() >= 10) {
            undoStack.removeFirst();
        }
        undoStack.addLast(new WorldSnapshot(currentStage, world));
    }

    private void performUndo() {
        if (undoStack.isEmpty()) {
            statusMessageLabel.setText("No actions to undo.");
            return;
        }
        WorldSnapshot snapshot = undoStack.removeLast();
        snapshot.restore(world);
        if (currentStage == STAGE_PLATES) {
            recalculatePlatesFromFaults();
        }
        canvas.invalidateStageImages();
        canvas.repaint();
        statusMessageLabel.setText("Undid action. (" + undoStack.size() + " undo steps remaining)");
    }

    private void resetCurrentStageToBaseline() {
        if (currentStage <= STAGE_SETUP || currentStage >= stageBaselineSnapshots.length) {
            return;
        }
        WorldSnapshot baseline = stageBaselineSnapshots[currentStage];
        if (baseline == null) {
            statusMessageLabel.setText("No baseline state recorded for this step.");
            return;
        }
        pushUndoSnapshot();
        for (int s = currentStage + 1; s < stageBaselineSnapshots.length; s++) {
            stageBaselineSnapshots[s] = null;
        }
        baseline.restore(world);
        lineStartHex = null;
        lineEndHex = null;
        if (currentStage == STAGE_PLATES) {
            recalculatePlatesFromFaults();
        }
        canvas.invalidateStageImages();
        canvas.repaint();
        statusMessageLabel.setText("Reset current step to baseline state.");
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AssetStyles.UI_BG_PRIMARY);

        JPanel navHeader = new JPanel(new GridLayout(1, 5, -1, 0));
        navHeader.setBackground(AssetStyles.BACKGROUND_DARK);

        String[] stageLabels = new String[] {
                LanguageStrings.get(LanguageStrings.UI_WORLD_STAGE_SETUP),
                LanguageStrings.get(LanguageStrings.UI_WORLD_STAGE_FAULTS),
                LanguageStrings.get(LanguageStrings.UI_WORLD_STAGE_LAND),
                LanguageStrings.get(LanguageStrings.UI_WORLD_STAGE_BIOMES),
                LanguageStrings.get(LanguageStrings.UI_WORLD_STAGE_DYNASTIES)
        };

        for (int i = 0; i < 5; i++) {
            final int s = i;
            JButton btn = new JButton(stageLabels[i]);
            AssetStyles.styleSectionTabButton(btn);
            btn.addActionListener(e -> setStage(s));
            stageNavButtons[i] = btn;
            navHeader.add(btn);
        }

        buildSetupPanel();

        centerCardPanel.setOpaque(false);
        centerCardPanel.add(setupPanel, "SETUP");

        JPanel canvasContainer = new JPanel(new BorderLayout());
        canvasContainer.setOpaque(false);

        dynamicToolbarPanel.setOpaque(false);
        dynamicToolbarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AssetStyles.UI_BORDER_COLOR));
        buildGlobalToolbar();
        dynamicPalettePanel.setOpaque(false);
        dynamicToolbarPanel.add(dynamicPalettePanel, BorderLayout.SOUTH);

        canvasContainer.add(dynamicToolbarPanel, BorderLayout.NORTH);
        canvasContainer.add(canvas, BorderLayout.CENTER);

        centerCardPanel.add(canvasContainer, "CANVAS");

        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(AssetStyles.UI_BG_SECONDARY);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AssetStyles.UI_BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBottom.setOpaque(false);

        backButton.setIcon(null);
        backButton.setText(LanguageStrings.get(LanguageStrings.UI_BACK));
        AssetStyles.styleButton(backButton);
        backButton.addActionListener(e -> setStage(currentStage - 1));

        nextButton.setIcon(null);
        nextButton.setText(LanguageStrings.get(LanguageStrings.UI_NEXT));
        AssetStyles.styleButton(nextButton);
        nextButton.addActionListener(e -> setStage(currentStage + 1));

        leftBottom.add(backButton);
        leftBottom.add(nextButton);

        statusMessageLabel.setFont(AssetStyles.FONT_SMALL);
        statusMessageLabel.setForeground(AssetStyles.FONT_COLOR_HIGHLIGHT);
        leftBottom.add(Box.createHorizontalStrut(12));
        leftBottom.add(statusMessageLabel);

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBottom.setOpaque(false);

        saveButton.setIcon(null);
        saveButton.setText(LanguageStrings.get(LanguageStrings.UI_WORLD_SAVE_MAP));
        AssetStyles.styleButton(saveButton);
        saveButton.addActionListener(e -> saveCurrentWorld(true));

        playButton.setIcon(null);
        playButton.setText(LanguageStrings.get(LanguageStrings.UI_PLAY));
        AssetStyles.styleButton(playButton);
        playButton.addActionListener(e -> playCurrentWorld());

        JButton closeBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        AssetStyles.styleButton(closeBtn);
        closeBtn.addActionListener(e -> requestClose());

        rightBottom.add(saveButton);
        rightBottom.add(playButton);
        rightBottom.add(closeBtn);

        bottomBar.add(leftBottom, BorderLayout.WEST);
        bottomBar.add(rightBottom, BorderLayout.EAST);

        root.add(navHeader, BorderLayout.NORTH);
        root.add(centerCardPanel, BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void buildSetupPanel() {
        setupPanel.setOpaque(false);
        setupPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        Dimension boxSize = new Dimension(220, Math.max(AssetStyles.MIN_CONTROL_HIT_SIZE + 6, nameField.getPreferredSize().height));
        nameField.setPreferredSize(boxSize);
        nameField.setMinimumSize(boxSize);
        nameField.setMaximumSize(boxSize);
        heightField.setPreferredSize(boxSize);
        heightField.setMinimumSize(boxSize);
        heightField.setMaximumSize(boxSize);
        iceCapMarginField.setPreferredSize(boxSize);
        iceCapMarginField.setMinimumSize(boxSize);
        iceCapMarginField.setMaximumSize(boxSize);
        dimensionsDisplay.setPreferredSize(new Dimension(280, boxSize.height));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        JLabel nameLbl = new JLabel(LanguageStrings.get(LanguageStrings.UI_WORLD_NAME) + ":");
        nameLbl.setFont(AssetStyles.FONT_BOLD);
        nameLbl.setForeground(AssetStyles.FONT_COLOR_HEADER);
        setupPanel.add(nameLbl, c);

        c.gridx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        nameField.setText(mapName);
        nameField.setFont(AssetStyles.FONT_NORMAL);
        setupPanel.add(nameField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        JLabel sizeLbl = new JLabel(LanguageStrings.get(LanguageStrings.UI_WORLD_SIZE) + ":");
        sizeLbl.setFont(AssetStyles.FONT_BOLD);
        sizeLbl.setForeground(AssetStyles.FONT_COLOR_HEADER);
        setupPanel.add(sizeLbl, c);

        c.gridx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        heightField.setFont(AssetStyles.FONT_NORMAL);
        heightField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateDimensionsPreview(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateDimensionsPreview(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateDimensionsPreview(); }
        });
        dimensionsDisplay.setFont(AssetStyles.FONT_NORMAL);
        dimensionsDisplay.setForeground(AssetStyles.FONT_COLOR);
        JPanel heightRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        heightRow.setOpaque(false);
        heightRow.add(heightField);
        heightRow.add(Box.createHorizontalStrut(12));
        heightRow.add(dimensionsDisplay);
        setupPanel.add(heightRow, c);

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        JPanel presetsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        presetsRow.setOpaque(false);
        addPresetButton(presetsRow, "16 (Tiny, 32x16)", 16);
        addPresetButton(presetsRow, "32 (Small, 64x32)", 32);
        addPresetButton(presetsRow, "48 (Medium, 96x48)", 48);
        addPresetButton(presetsRow, "64 (Large, 128x64)", 64);
        setupPanel.add(presetsRow, c);

        c.gridx = 0;
        c.gridy = 3;
        c.fill = GridBagConstraints.NONE;
        JLabel capsLbl = new JLabel(LanguageStrings.get(LanguageStrings.UI_WORLD_ICE_CAPS) + " (%):");
        capsLbl.setFont(AssetStyles.FONT_BOLD);
        capsLbl.setForeground(AssetStyles.FONT_COLOR_HEADER);
        setupPanel.add(capsLbl, c);

        c.gridx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        iceCapMarginField.setFont(AssetStyles.FONT_NORMAL);
        iceCapMarginField.setToolTipText("0 - 50%");
        setupPanel.add(iceCapMarginField, c);

        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        c.insets = new Insets(18, 10, 8, 10);
        c.fill = GridBagConstraints.NONE;
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionsRow.setOpaque(false);
        JButton initGridBtn = new JButton("Apply Dimensions & Generate Grid");
        AssetStyles.styleButton(initGridBtn);
        initGridBtn.addActionListener(e -> applyGridSetup());
        actionsRow.add(initGridBtn);
        actionsRow.add(Box.createHorizontalStrut(10));

        JButton resetSetupBtn = new JButton(LanguageStrings.get(LanguageStrings.MULTI_MAP_PARAM_RESET));
        AssetStyles.styleButton(resetSetupBtn);
        resetSetupBtn.addActionListener(e -> {
            heightField.setText(String.valueOf(GameNumbers.WORLD_RECT_HEIGHT));
            iceCapMarginField.setText("18");
            updateDimensionsPreview();
        });
        actionsRow.add(resetSetupBtn);

        setupPanel.add(actionsRow, c);
        updateDimensionsPreview();
    }

    private void updateDimensionsPreview() {
        int h = parseBoundedInt(heightField.getText(), GameNumbers.WORLD_RECT_HEIGHT, 6, 128);
        int w = 2 * h;
        dimensionsDisplay.setText("Dimensions: " + w + " x " + h + " (" + AssetStyles.formatNumber(w * h) + " hexes)");
    }

    private void addPresetButton(JPanel parent, String title, int h) {
        if (parent.getComponentCount() > 0) {
            parent.add(Box.createHorizontalStrut(6));
        }
        JButton b = new JButton(title);
        AssetStyles.styleCompactButton(b);
        b.addActionListener(e -> {
            heightField.setText(String.valueOf(h));
            updateDimensionsPreview();
        });
        parent.add(b);
    }

    private int parseBoundedInt(String text, int fallback, int min, int max) {
        try {
            int val = Integer.parseInt(text.trim());
            return Math.max(min, Math.min(max, val));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void applyGridSetup() {
        int h = parseBoundedInt(heightField.getText(), GameNumbers.WORLD_RECT_HEIGHT, 6, 128);
        int w = 2 * h;
        int icePct = parseBoundedInt(iceCapMarginField.getText(), 18, 0, 50);
        boolean caps = icePct > 0;
        double capMargin = caps ? (1.0 - (icePct / 100.0)) : 1.0;
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            mapName = name;
        }
        undoStack.clear();
        Arrays.fill(stageBaselineSnapshots, null);
        lineStartHex = null;
        lineEndHex = null;
        world.initializeBlankGrid(w, h, caps, capMargin);
        recalculatePlatesFromFaults();
        stageBaselineSnapshots[STAGE_PLATES] = new WorldSnapshot(STAGE_PLATES, world);
        canvas.invalidateCaches();
        canvas.resetCamera();
        statusMessageLabel.setText("Initialized " + w + "x" + h + " world grid.");
        setStage(STAGE_PLATES);
    }

    private void buildGlobalToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setOpaque(false);

        ButtonGroup toolGroup = new ButtonGroup();
        Icon brushIcon = scaleIcon(UiResourceLoader.loadDialogIcon(getClass(), "/icons/tools/ToolBrush.png", 16), 16);
        Icon lineIcon = scaleIcon(UiResourceLoader.loadDialogIcon(getClass(), "/icons/tools/ToolLine.png", 16), 16);
        Icon fillIcon = scaleIcon(UiResourceLoader.loadDialogIcon(getClass(), "/icons/tools/ToolFill.png", 16), 16);
        Icon eraserIcon = scaleIcon(UiResourceLoader.loadDialogIcon(getClass(), "/icons/tools/ToolEraser.png", 16), 16);
        Icon pickerIcon = scaleIcon(UiResourceLoader.loadDialogIcon(getClass(), "/icons/tools/ToolPicker.png", 16), 16);
        Icon undoIcon = scaleIcon(UiResourceLoader.loadDialogIcon(getClass(), "/icons/tools/ToolUndo.png", 16), 16);

        JToggleButton brushBtn = new JToggleButton(brushIcon, true);
        JToggleButton lineBtn = lineIcon != null ? new JToggleButton(lineIcon, false) : new JToggleButton("/", false);
        JToggleButton fillBtn = new JToggleButton(fillIcon, false);
        JToggleButton eraserBtn = new JToggleButton(eraserIcon, false);
        JToggleButton pickerBtn = new JToggleButton(pickerIcon, false);

        JToggleButton[] createdToolButtons = {brushBtn, lineBtn, fillBtn, eraserBtn, pickerBtn};
        String[] toolTips = {
                LanguageStrings.get(LanguageStrings.UI_WORLD_TOOL_BRUSH),
                LanguageStrings.get(LanguageStrings.UI_WORLD_TOOL_LINE),
                LanguageStrings.get(LanguageStrings.UI_WORLD_TOOL_FILL),
                LanguageStrings.get(LanguageStrings.UI_WORLD_TOOL_ERASER),
                LanguageStrings.get(LanguageStrings.UI_WORLD_TOOL_PICKER)
        };
        Tool[] toolValues = {Tool.BRUSH, Tool.LINE, Tool.FILL, Tool.ERASER, Tool.PICKER};

        for (int i = 0; i < createdToolButtons.length; i++) {
            JToggleButton tb = createdToolButtons[i];
            Tool toolVal = toolValues[i];
            tb.setToolTipText(toolTips[i]);
            tb.addChangeListener(e -> tb.setBackground(tb.isSelected() ? AssetStyles.TAB_SELECTED_BG : AssetStyles.BACKGROUND_COLOR));
            tb.addActionListener(e -> {
                currentTool = toolVal;
                lineStartHex = null;
                lineEndHex = null;
                canvas.repaint();
            });
            toolButtons[i] = tb;
            toolGroup.add(tb);
            bar.add(tb);
        }

        bar.add(Box.createHorizontalStrut(8));
        bar.add(new JLabel(LanguageStrings.get(LanguageStrings.UI_WORLD_BRUSH_SIZE) + ":"));
        for (int r = 1; r <= 3; r++) {
            final int rad = r;
            JButton rBtn = new JButton(String.valueOf(r));
            rBtn.addActionListener(e -> {
                brushRadius = rad;
                updateBrushSizeButtons();
            });
            brushSizeButtons[r - 1] = rBtn;
            bar.add(rBtn);
        }

        bar.add(Box.createHorizontalStrut(8));
        undoBtn = new JButton(undoIcon);
        undoBtn.setToolTipText(LanguageStrings.get(LanguageStrings.UI_UNDO) + " (Ctrl+Z / Cmd+Z)");
        undoBtn.addActionListener(e -> performUndo());
        bar.add(undoBtn);

        bar.add(Box.createHorizontalStrut(12));
        Icon autoIcon = scaleIcon(UiResourceLoader.loadDialogIcon(getClass(), "/icons/misc/Automation.png", 16), 16);
        autoGenBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_WORLD_AUTO_GENERATE), autoIcon);
        autoGenBtn.addActionListener(e -> autoGenerateCurrentStage());
        bar.add(autoGenBtn);

        bar.add(Box.createHorizontalStrut(8));
        resetStepButton = new JButton(LanguageStrings.get(LanguageStrings.UI_WORLD_RESET_STEP), undoIcon);
        resetStepButton.addActionListener(e -> resetCurrentStageToBaseline());
        bar.add(resetStepButton);

        applyToolbarStyles();
        dynamicToolbarPanel.add(bar, BorderLayout.NORTH);
    }

    private void applyToolbarStyles() {
        for (JToggleButton tb : toolButtons) {
            if (tb != null) {
                AssetStyles.styleCompactButton(tb);
                applyUniformIconButtonSize(tb);
                tb.setBackground(tb.isSelected() ? AssetStyles.TAB_SELECTED_BG : AssetStyles.BACKGROUND_COLOR);
            }
        }
        for (JButton rBtn : brushSizeButtons) {
            if (rBtn != null) {
                AssetStyles.styleCompactButton(rBtn);
                applyUniformIconButtonSize(rBtn);
            }
        }
        updateBrushSizeButtons();
        if (undoBtn != null) {
            AssetStyles.styleCompactButton(undoBtn);
            applyUniformIconButtonSize(undoBtn);
        }
        if (autoGenBtn != null) {
            AssetStyles.styleCompactButton(autoGenBtn);
            applyUniformTextButtonHeight(autoGenBtn);
        }
        if (resetStepButton != null) {
            AssetStyles.styleCompactButton(resetStepButton);
            applyUniformTextButtonHeight(resetStepButton);
        }
    }

    private void updateBrushSizeButtons() {
        for (int i = 0; i < brushSizeButtons.length; i++) {
            JButton b = brushSizeButtons[i];
            if (b == null) {
                continue;
            }
            boolean selected = (i + 1) == brushRadius;
            b.setBackground(selected ? AssetStyles.TAB_SELECTED_BG : AssetStyles.BACKGROUND_COLOR);
            b.setForeground(selected ? AssetStyles.FONT_COLOR_HEADER : AssetStyles.FONT_COLOR);
        }
    }

    private void updateStageView() {
        for (int i = 0; i < 5; i++) {
            AssetStyles.styleSectionTabButton(stageNavButtons[i]);
            AssetStyles.applyTabSelection(stageNavButtons[i], i == currentStage);
        }
        backButton.setEnabled(currentStage > STAGE_SETUP);
        nextButton.setVisible(currentStage < STAGE_DYNASTIES);
        nextButton.setText(LanguageStrings.get(LanguageStrings.UI_NEXT));

        CardLayout cl = (CardLayout) centerCardPanel.getLayout();
        if (currentStage == STAGE_SETUP) {
            cl.show(centerCardPanel, "SETUP");
        } else {
            cl.show(centerCardPanel, "CANVAS");
            refreshPalette();
            canvas.invalidateStageImages();
            canvas.repaint();
        }
    }

    private void refreshPalette() {
        dynamicPalettePanel.removeAll();
        if (currentStage == STAGE_PLATES) {
            JLabel faultLbl = new JLabel("Fault Type: ");
            faultLbl.setFont(AssetStyles.FONT_BOLD);
            dynamicPalettePanel.add(faultLbl);

            FaultLine.FaultType[] types = new FaultLine.FaultType[] {
                    FaultLine.FaultType.CONVERGENT,
                    FaultLine.FaultType.DIVERGENT,
                    FaultLine.FaultType.TRANSFORM
            };
            String[] typeNames = new String[] {
                    LanguageStrings.get(LanguageStrings.UI_FAULT_CONVERGENT),
                    LanguageStrings.get(LanguageStrings.UI_FAULT_DIVERGENT),
                    LanguageStrings.get(LanguageStrings.UI_FAULT_TRANSFORM)
            };
            String[] typeIcons = new String[] {
                    "/icons/tools/FaultConvergent.png",
                    "/icons/tools/FaultDivergent.png",
                    "/icons/tools/FaultTransform.png"
            };

            for (int i = 0; i < types.length; i++) {
                final FaultLine.FaultType ft = types[i];
                Icon fIcon = scaleIcon(UiResourceLoader.loadDialogIcon(getClass(), typeIcons[i], 16), 16);
                JButton btn = new JButton(fIcon);
                btn.setToolTipText(typeNames[i]);
                AssetStyles.styleCompactButton(btn);
                applyUniformIconButtonSize(btn);
                if (ft == selectedFaultType) {
                    btn.setBackground(AssetStyles.TAB_SELECTED_BG);
                    btn.setForeground(AssetStyles.FONT_COLOR_HEADER);
                }
                btn.addActionListener(e -> {
                    selectedFaultType = ft;
                    refreshPalette();
                });
                dynamicPalettePanel.add(btn);
            }

            dynamicPalettePanel.add(Box.createHorizontalStrut(8));
            JLabel intLbl = new JLabel(LanguageStrings.get(LanguageStrings.UI_INTENSITY) + ": ");
            intLbl.setFont(AssetStyles.FONT_BOLD);
            dynamicPalettePanel.add(intLbl);

            for (int intens = 1; intens <= 3; intens++) {
                final int curInt = intens;
                Icon iIcon = scaleIcon(UiResourceLoader.loadDialogIcon(getClass(), "/icons/tools/FaultIntensity" + intens + ".png", 16), 16);
                JButton iBtn = new JButton(iIcon);
                iBtn.setToolTipText(LanguageStrings.get(LanguageStrings.UI_INTENSITY) + " " + intens);
                AssetStyles.styleCompactButton(iBtn);
                applyUniformIconButtonSize(iBtn);
                if (curInt == selectedFaultIntensity) {
                    iBtn.setBackground(AssetStyles.TAB_SELECTED_BG);
                    iBtn.setForeground(AssetStyles.FONT_COLOR_HEADER);
                }
                iBtn.addActionListener(e -> {
                    selectedFaultIntensity = curInt;
                    refreshPalette();
                });
                dynamicPalettePanel.add(iBtn);
            }

            dynamicPalettePanel.add(Box.createHorizontalStrut(10));
            JButton clearFaultsBtn = new JButton("Clear Faults");
            AssetStyles.styleCompactButton(clearFaultsBtn);
            applyUniformTextButtonHeight(clearFaultsBtn);
            clearFaultsBtn.addActionListener(e -> {
                pushUndoSnapshot();
                for (Hex h : world.getHexes()) {
                    if (h != null) {
                        h.setFaultType(null);
                        h.setFaultIntensity(0);
                    }
                }
                recalculatePlatesFromFaults();
                canvas.invalidateStageImages();
                canvas.repaint();
            });
            dynamicPalettePanel.add(clearFaultsBtn);

        } else if (currentStage == STAGE_LAND) {
            JLabel elevLbl = new JLabel(LanguageStrings.get(LanguageStrings.UI_WORLD_STAGE_LAND).replaceFirst("^\\d+\\.\\s*", "") + ": ");
            elevLbl.setFont(AssetStyles.FONT_BOLD);
            dynamicPalettePanel.add(elevLbl);

            JButton raiseBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_ELEVATION_RAISE));
            AssetStyles.styleCompactButton(raiseBtn);
            applyUniformTextButtonHeight(raiseBtn);
            if (selectedExactElevation == null && raiseElevation) {
                raiseBtn.setBackground(AssetStyles.TAB_SELECTED_BG);
                raiseBtn.setForeground(AssetStyles.FONT_COLOR_HEADER);
            }
            raiseBtn.addActionListener(e -> {
                raiseElevation = true;
                selectedExactElevation = null;
                refreshPalette();
            });

            JButton lowerBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_ELEVATION_LOWER));
            AssetStyles.styleCompactButton(lowerBtn);
            applyUniformTextButtonHeight(lowerBtn);
            if (selectedExactElevation == null && !raiseElevation) {
                lowerBtn.setBackground(AssetStyles.TAB_SELECTED_BG);
                lowerBtn.setForeground(AssetStyles.FONT_COLOR_HEADER);
            }
            lowerBtn.addActionListener(e -> {
                raiseElevation = false;
                selectedExactElevation = null;
                refreshPalette();
            });

            dynamicPalettePanel.add(raiseBtn);
            dynamicPalettePanel.add(lowerBtn);
            dynamicPalettePanel.add(Box.createHorizontalStrut(12));

            JPanel scaleBar = new JPanel(new GridLayout(1, MapDialog.HEIGHT_COLORS.length, 2, 0));
            scaleBar.setOpaque(false);
            for (int lvl = 0; lvl < MapDialog.HEIGHT_COLORS.length; lvl++) {
                final int elevationLevel = lvl;
                boolean exactSelected = Integer.valueOf(lvl).equals(selectedExactElevation);
                JButton swatch = new JButton(String.valueOf(lvl));
                swatch.setFont(AssetStyles.FONT_SMALL);
                swatch.setFocusPainted(false);
                swatch.setOpaque(true);
                swatch.setContentAreaFilled(true);
                swatch.setMargin(new Insets(0, 0, 0, 0));
                Dimension swatchSize = new Dimension(28, TOOLBAR_BTN_HEIGHT);
                swatch.setPreferredSize(swatchSize);
                swatch.setMinimumSize(swatchSize);
                swatch.setMaximumSize(swatchSize);
                swatch.setBackground(MapDialog.HEIGHT_COLORS[lvl]);
                swatch.setForeground(contrastForeground(MapDialog.HEIGHT_COLORS[lvl]));
                swatch.setBorder(BorderFactory.createLineBorder(
                        exactSelected ? AssetStyles.FONT_COLOR_HIGHLIGHT : AssetStyles.BORDER_COLOR,
                        exactSelected ? 2 : 1));
                swatch.setToolTipText(LanguageStrings.get(LanguageStrings.MAP_MODE_HEIGHT) + ": " + lvl);
                swatch.addActionListener(e -> {
                    selectedExactElevation = elevationLevel;
                    refreshPalette();
                });
                scaleBar.add(swatch);
            }
            dynamicPalettePanel.add(scaleBar);

        } else if (currentStage == STAGE_BIOMES) {
            JPanel biomeContainer = new JPanel();
            biomeContainer.setLayout(new BoxLayout(biomeContainer, BoxLayout.Y_AXIS));
            biomeContainer.setOpaque(false);

            JPanel catRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            catRow.setOpaque(false);
            catRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel biomeLbl = new JLabel("Biomes: ");
            biomeLbl.setFont(AssetStyles.FONT_BOLD);
            catRow.add(biomeLbl);

            for (int cIdx = 0; cIdx < BIOME_CATEGORY_NAMES.length; cIdx++) {
                final int categoryIdx = cIdx;
                JButton catBtn = new JButton(BIOME_CATEGORY_NAMES[cIdx]);
                AssetStyles.styleCompactButton(catBtn);
                applyUniformTextButtonHeight(catBtn);
                if (cIdx == selectedBiomeCategory) {
                    catBtn.setBackground(AssetStyles.TAB_SELECTED_BG);
                    catBtn.setForeground(AssetStyles.FONT_COLOR_HEADER);
                }
                catBtn.addActionListener(e -> {
                    selectedBiomeCategory = categoryIdx;
                    refreshPalette();
                });
                catRow.add(catBtn);
            }
            biomeContainer.add(catRow);

            Biome[] groupBiomes = BIOME_CATEGORIES[Math.max(0, Math.min(BIOME_CATEGORIES.length - 1, selectedBiomeCategory))];
            int cols = Math.max(1, Math.min(7, groupBiomes.length));
            JPanel biomesGrid = new JPanel(new GridLayout(0, cols, 6, 4));
            biomesGrid.setOpaque(false);
            for (Biome b : groupBiomes) {
                JButton bBtn = createUniformPaletteItemButton(b.getName(), b.getIcon(), b.getMapColor(), b == selectedBiome);
                bBtn.addActionListener(e -> {
                    selectedBiome = b;
                    refreshPalette();
                });
                biomesGrid.add(bBtn);
            }
            JPanel biomesGridWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            biomesGridWrap.setOpaque(false);
            biomesGridWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            biomesGridWrap.add(biomesGrid);
            biomeContainer.add(biomesGridWrap);

            JPanel riverRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            riverRow.setOpaque(false);
            riverRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JButton refreshRiversBtn = new JButton("Refresh Rivers");
            AssetStyles.styleCompactButton(refreshRiversBtn);
            applyUniformTextButtonHeight(refreshRiversBtn);
            refreshRiversBtn.setToolTipText("Generate or refresh algorithmic rivers and geographic features from current biomes");
            refreshRiversBtn.addActionListener(e -> refreshRiversAndFeatures());
            riverRow.add(refreshRiversBtn);
            biomeContainer.add(riverRow);

            dynamicPalettePanel.add(biomeContainer);

        } else if (currentStage == STAGE_DYNASTIES) {
            SpeciesDiet[] diets = new SpeciesDiet[] {
                    GameConstants.DIET_FUNGIVOROUS,
                    GameConstants.DIET_CARNIVOROUS
            };
            List<AntSpecies> dietSpecies = new ArrayList<>();
            if (selectedDynastyDiet == GameConstants.DIET_FUNGIVOROUS && GameConstants.SPECIES_OMNI != null) {
                dietSpecies.add(GameConstants.SPECIES_OMNI);
            }
            for (AntSpecies s : GameConstants.getSpecies()) {
                if (s != null && s.getDiet() == selectedDynastyDiet && s != GameConstants.SPECIES_OMNI) {
                    dietSpecies.add(s);
                }
            }
            if (selectedDynastySpecies == null || selectedDynastySpecies.getDiet() != selectedDynastyDiet) {
                selectedDynastySpecies = dietSpecies.isEmpty() ? GameConstants.SPECIES_OMNI : dietSpecies.get(0);
            }

            JPanel dynContainer = new JPanel();
            dynContainer.setLayout(new BoxLayout(dynContainer, BoxLayout.Y_AXIS));
            dynContainer.setOpaque(false);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            topRow.setOpaque(false);
            topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel dynLbl = new JLabel("Dynasties: ");
            dynLbl.setFont(AssetStyles.FONT_BOLD);
            topRow.add(dynLbl);

            for (SpeciesDiet d : diets) {
                if (d == null) continue;
                JButton dietBtn = new JButton(d.getName(), scaleIcon(d.getIcon(), 16));
                AssetStyles.styleCompactButton(dietBtn);
                applyUniformTextButtonHeight(dietBtn);
                if (d == selectedDynastyDiet) {
                    dietBtn.setBackground(AssetStyles.TAB_SELECTED_BG);
                    dietBtn.setForeground(AssetStyles.FONT_COLOR_HEADER);
                }
                dietBtn.addActionListener(e -> {
                    selectedDynastyDiet = d;
                    selectedDynastySpecies = null;
                    if (selectedDynastyDiet == GameConstants.DIET_FUNGIVOROUS && GameConstants.SPECIES_OMNI != null) {
                        selectedDynastySpecies = GameConstants.SPECIES_OMNI;
                    } else {
                        for (AntSpecies s : GameConstants.getSpecies()) {
                            if (s != null && s.getDiet() == selectedDynastyDiet) {
                                selectedDynastySpecies = s;
                                break;
                            }
                        }
                    }
                    refreshPalette();
                });
                topRow.add(dietBtn);
            }

            topRow.add(Box.createHorizontalStrut(10));

            Icon capIcon = scaleIcon(
                    GameConstants.ROLE_COMMANDER != null && GameConstants.ROLE_COMMANDER.getIcon() != null
                            ? GameConstants.ROLE_COMMANDER.getIcon()
                            : UiResourceLoader.loadDialogIcon(getClass(), "/icons/roles/Commander.png", 16),
                    16);
            JButton placeCapBtn = new JButton("Place Capital", capIcon);
            AssetStyles.styleCompactButton(placeCapBtn);
            applyUniformTextButtonHeight(placeCapBtn);
            if (dynastyAction == 0) {
                placeCapBtn.setBackground(AssetStyles.TAB_SELECTED_BG);
                placeCapBtn.setForeground(AssetStyles.FONT_COLOR_HEADER);
            }
            placeCapBtn.addActionListener(e -> {
                dynastyAction = 0;
                refreshPalette();
            });

            JButton claimBtn = new JButton("Claim Territory");
            AssetStyles.styleCompactButton(claimBtn);
            applyUniformTextButtonHeight(claimBtn);
            if (dynastyAction == 1) {
                claimBtn.setBackground(AssetStyles.TAB_SELECTED_BG);
                claimBtn.setForeground(AssetStyles.FONT_COLOR_HEADER);
            }
            claimBtn.addActionListener(e -> {
                dynastyAction = 1;
                refreshPalette();
            });

            JButton removeBtn = new JButton("Remove");
            AssetStyles.styleCompactButton(removeBtn);
            applyUniformTextButtonHeight(removeBtn);
            if (dynastyAction == 2) {
                removeBtn.setBackground(AssetStyles.TAB_SELECTED_BG);
                removeBtn.setForeground(AssetStyles.FONT_COLOR_HEADER);
            }
            removeBtn.addActionListener(e -> {
                dynastyAction = 2;
                refreshPalette();
            });

            topRow.add(placeCapBtn);
            topRow.add(claimBtn);
            topRow.add(removeBtn);
            dynContainer.add(topRow);

            int cols = Math.max(1, Math.min(7, dietSpecies.size()));
            JPanel speciesGrid = new JPanel(new GridLayout(0, cols, 6, 4));
            speciesGrid.setOpaque(false);
            for (AntSpecies s : dietSpecies) {
                Color bg = s.getPalette() != null
                        ? new Color(s.getPalette().getHeadColor())
                        : AssetStyles.BACKGROUND_COLOR;
                JButton sBtn = createUniformPaletteItemButton(s.getName(), s.getIcon(), bg, s == selectedDynastySpecies);
                sBtn.addActionListener(e -> {
                    selectedDynastySpecies = s;
                    refreshPalette();
                });
                speciesGrid.add(sBtn);
            }
            JPanel speciesGridWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            speciesGridWrap.setOpaque(false);
            speciesGridWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            speciesGridWrap.add(speciesGrid);
            dynContainer.add(speciesGridWrap);

            dynamicPalettePanel.add(dynContainer);
        }
        dynamicPalettePanel.revalidate();
        dynamicPalettePanel.repaint();
    }

    private static void applyUniformIconButtonSize(AbstractButton btn) {
        btn.setBorder(BorderFactory.createCompoundBorder(
                AssetStyles.BUTTON_BORDER,
                BorderFactory.createEmptyBorder(3, 4, 3, 4)));
        btn.setPreferredSize(ICON_BTN_SIZE);
        btn.setMinimumSize(ICON_BTN_SIZE);
        btn.setMaximumSize(ICON_BTN_SIZE);
        btn.setMargin(new Insets(0, 0, 0, 0));
    }

    private static void applyUniformTextButtonHeight(AbstractButton btn) {
        btn.setHorizontalTextPosition(SwingConstants.TRAILING);
        btn.setIconTextGap(6);
        FontMetrics fm = btn.getFontMetrics(btn.getFont());
        Insets ins = btn.getInsets();
        int textW = btn.getText() != null ? fm.stringWidth(btn.getText()) : 0;
        int iconW = btn.getIcon() != null ? btn.getIcon().getIconWidth() + (textW > 0 ? 6 : 0) : 0;
        int w = Math.max(ICON_BTN_SIZE.width, ins.left + ins.right + iconW + textW + 8);
        Dimension d = new Dimension(w, TOOLBAR_BTN_HEIGHT);
        btn.setPreferredSize(d);
        btn.setMinimumSize(d);
        btn.setMaximumSize(d);
    }

    private static Icon scaleIcon(Icon icon, int size) {
        if (icon == null) {
            return null;
        }
        if (icon.getIconWidth() == size && icon.getIconHeight() == size) {
            return icon;
        }
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (icon instanceof ImageIcon ii && ii.getImage() != null) {
            g2.drawImage(ii.getImage(), 0, 0, size, size, null);
        } else {
            double sx = icon.getIconWidth() > 0 ? (double) size / icon.getIconWidth() : 1.0;
            double sy = icon.getIconHeight() > 0 ? (double) size / icon.getIconHeight() : 1.0;
            g2.scale(sx, sy);
            icon.paintIcon(null, g2, 0, 0);
        }
        g2.dispose();
        return new ImageIcon(img);
    }

    private static JButton createUniformPaletteItemButton(String text, Icon icon, Color bg, boolean selected) {
        JButton btn = new JButton(text, scaleIcon(icon, 16));
        Dimension size = new Dimension(132, TOOLBAR_BTN_HEIGHT);
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        btn.setFont(AssetStyles.FONT_SMALL);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(4);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(bg);
        btn.setForeground(contrastForeground(bg));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? AssetStyles.FONT_COLOR_HIGHLIGHT : AssetStyles.BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        btn.setToolTipText(text);
        return btn;
    }

    private static Color contrastForeground(Color bg) {
        if (bg == null) return AssetStyles.FONT_COLOR;
        double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    private void setStage(int newStage) {
        if (newStage < STAGE_SETUP || newStage > STAGE_DYNASTIES) {
            return;
        }
        lineStartHex = null;
        lineEndHex = null;
        if (strokeModifiedPlates) {
            recalculatePlatesFromFaults();
            strokeModifiedPlates = false;
        }
        if (newStage > STAGE_SETUP && stageBaselineSnapshots[newStage] == null) {
            stageBaselineSnapshots[newStage] = new WorldSnapshot(newStage, world);
        }
        currentStage = newStage;
        updateStageView();
    }

    public void recalculatePlatesFromFaults() {
        if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
            return;
        }

        world.clearTectonicPlatesAndFaults();
        for (Hex h : world.getHexes()) {
            if (h != null) {
                h.setPlateId(0);
            }
        }

        Set<Hex> visited = new HashSet<>();
        List<List<Hex>> components = new ArrayList<>();

        for (Hex h : world.getHexes()) {
            if (h == null || h.getFaultType() != null || visited.contains(h)) {
                continue;
            }
            List<Hex> comp = new ArrayList<>();
            Queue<Hex> q = new ArrayDeque<>();
            q.add(h);
            visited.add(h);

            while (!q.isEmpty()) {
                Hex curr = q.poll();
                comp.add(curr);
                for (Hex n : curr.getAdjacentNeighbors()) {
                    if (n != null && n.getFaultType() == null && !visited.contains(n)) {
                        visited.add(n);
                        q.add(n);
                    }
                }
            }
            components.add(comp);
        }

        if (components.isEmpty()) {
            List<Hex> all = new ArrayList<>();
            for (Hex h : world.getHexes()) {
                if (h != null) {
                    all.add(h);
                }
            }
            if (!all.isEmpty()) {
                components.add(all);
            }
        }

        components.sort((a, b) -> Integer.compare(b.size(), a.size()));

        for (int i = 0; i < components.size(); i++) {
            int pid = i + 1;
            List<Hex> comp = components.get(i);
            Hex center = comp.get(0);
            double driftAngle = GameRandom.nextDouble() * 2.0 * Math.PI;
            double driftSpeed = 0.5 + GameRandom.nextDouble() * 1.5;
            boolean oceanic = (components.size() > 2)
                    ? ((i % 2 == 0) || (GameRandom.nextInt(100) < 55))
                    : (i == 0);
            TectonicPlate plate = new TectonicPlate(pid, center.getQ(), center.getR(), driftAngle, driftSpeed, oceanic);
            for (Hex h : comp) {
                plate.addHex(h);
            }
            world.addTectonicPlate(plate);
        }

        ArrayDeque<Hex> faultFillQueue = new ArrayDeque<>();
        for (Hex h : world.getHexes()) {
            if (h != null && h.getPlateId() > 0) {
                for (Hex n : h.getAdjacentNeighbors()) {
                    if (n != null && n.getPlateId() == 0) {
                        faultFillQueue.add(h);
                        break;
                    }
                }
            }
        }
        while (!faultFillQueue.isEmpty()) {
            Hex cur = faultFillQueue.poll();
            TectonicPlate plate = world.getPlateById(cur.getPlateId());
            if (plate == null) {
                continue;
            }
            for (Hex n : cur.getAdjacentNeighbors()) {
                if (n != null && n.getPlateId() == 0) {
                    plate.addHex(n);
                    faultFillQueue.add(n);
                }
            }
        }

        Map<Long, FaultLine> faultMap = new HashMap<>();
        for (Hex h : world.getHexes()) {
            if (h == null || h.getPlateId() <= 0) continue;
            int idA = h.getPlateId();
            for (Hex n : h.getAdjacentNeighbors()) {
                if (n == null || n.getPlateId() <= 0) continue;
                int idB = n.getPlateId();
                if (idA == idB) continue;
                long key = (((long) Math.min(idA, idB)) << 32) | (Math.max(idA, idB) & 0xffffffffL);
                FaultLine fault = faultMap.get(key);
                if (fault == null) {
                    FaultLine.FaultType type = h.getFaultType() != null ? h.getFaultType()
                            : (n.getFaultType() != null ? n.getFaultType() : FaultLine.FaultType.TRANSFORM);
                    int intensity = Math.max(1, Math.min(2,
                            h.getFaultType() != null ? h.getFaultIntensity() : n.getFaultIntensity()));
                    fault = new FaultLine(Math.min(idA, idB), Math.max(idA, idB), type, intensity);
                    faultMap.put(key, fault);
                    world.addFaultLine(fault);
                }
                fault.addBoundaryPair(h, n);
            }
        }
    }

    private void autoGenerateCurrentStage() {
        if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
            return;
        }
        strokeModifiedPlates = false;
        pushUndoSnapshot();

        if (currentStage == STAGE_PLATES) {
            world.generateTectonicPlates();
            world.generateFaultLinesFromPlates();
            statusMessageLabel.setText("Auto-generated tectonic plates & fault lines.");
        } else if (currentStage == STAGE_LAND) {
            if (world.getTectonicPlates().isEmpty()) {
                world.generateTectonicPlates();
                world.generateFaultLinesFromPlates();
            }
            world.generateAlgorithmicLandmasses();
            statusMessageLabel.setText("Auto-generated landmasses & elevation.");
        } else if (currentStage == STAGE_BIOMES) {
            world.generateAlgorithmicBiomesAndFeatures();
            statusMessageLabel.setText("Auto-assigned biomes, rivers & features.");
        } else if (currentStage == STAGE_DYNASTIES) {
            world.getDynastys().clear();
            for (Hex h : world.getHexes()) {
                if (h != null) {
                    h.setColony(null);
                }
            }
            world.markColonizedHexIndexDirty();
            world.classifyLandmasses();
            DynastyTitle playerTitle = dynastyNamingService.pickRandomTitle();
            world.populateDynasties(GameConstants.BIOME_PLAINS, null, "Player", playerTitle.getNameKey());
            statusMessageLabel.setText("Auto-generated dynasties on landmasses.");
        }
        canvas.invalidateStageImages();
        canvas.repaint();
    }

    private void refreshRiversAndFeatures() {
        if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
            return;
        }
        pushUndoSnapshot();
        world.generateAlgorithmicRiversAndFeatures();
        statusMessageLabel.setText("Generated / refreshed rivers & geographic features.");
        canvas.invalidateStageImages();
        canvas.repaint();
    }

    private boolean saveCurrentWorld(boolean showDialog) {
        if (strokeModifiedPlates) {
            recalculatePlatesFromFaults();
            strokeModifiedPlates = false;
            canvas.invalidateStageImages();
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            name = mapName;
        }
        try {
            File savedFile = SavedMapService.saveMap(world, name, currentStage);
            statusMessageLabel.setText("Saved to " + savedFile.getName());
            if (showDialog) {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.UI_WORLD_SAVE_SUCCESS) + "\n" + savedFile.getAbsolutePath(),
                        LanguageStrings.get(LanguageStrings.UI_WORLD_SAVE_MAP),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } catch (Exception ex) {
            UiOptionPane.showMessageDialog(this,
                    "Failed to save map: " + ex.getMessage(),
                    LanguageStrings.get(LanguageStrings.UI_ERROR),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void playCurrentWorld() {
        saveCurrentWorld(false);
        try {
            SavedMapData data = SavedMapService.createSavedMapData(world, mapName, currentStage);
            Savefile save = new Savefile(1, mapName);
            int playerTitleId = GameConstants.DYNASTY_TITLE_DYNASTY.getId();
            for (Dynasty d : world.getDynastys()) {
                if (d != null && d.isPlayer()) {
                    playerTitleId = d.getTitleId();
                    break;
                }
            }
            save.setPlayerDynastyTitleId(playerTitleId);
            save.setStarterBiomeId(1);
            save.setStarterDietId(1);
            save.setSpectatorMode(false);
            save.setVersion(SaveManager.getAppVersion());
            SavedMapService.populateSavefileFromMapData(save, data);
            dispose();
            mainFrame.openGameWithSave(save);
        } catch (Exception ex) {
            UiOptionPane.showMessageDialog(this,
                    "Error launching game: " + ex.getMessage(),
                    LanguageStrings.get(LanguageStrings.UI_ERROR),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyToolAt(Hex centerHex) {
        if (centerHex == null) {
            return;
        }
        if (currentTool == Tool.PICKER) {
            sampleHex(centerHex);
            return;
        }

        if (currentTool == Tool.FILL) {
            if (!currentStrokeSnapshotPushed) {
                pushUndoSnapshot();
                currentStrokeSnapshotPushed = true;
            }
            applyBucketFill(centerHex);
            if (currentStage == STAGE_PLATES) {
                recalculatePlatesFromFaults();
            }
            canvas.invalidateStageImages();
            canvas.repaint();
            return;
        }
        List<Hex> targets = getHexesInRadius(centerHex, brushRadius);
        List<Hex> editedHexes = new ArrayList<>();
        for (Hex h : targets) {
            if (!currentStrokeHexes.add(h)) {
                continue;
            }
            if (!currentStrokeSnapshotPushed) {
                pushUndoSnapshot();
                currentStrokeSnapshotPushed = true;
            }
            if (currentTool == Tool.ERASER) {
                applyEraser(h);
            } else {
                applyPaint(h);
            }
            editedHexes.add(h);
        }
        if (editedHexes.isEmpty()) {
            return;
        }
        if (currentStage == STAGE_PLATES) {
            strokeModifiedPlates = true;
        }
        if (currentStage == STAGE_DYNASTIES && dynastyAction == 0) {
            canvas.invalidateStageImages();
        } else {
            canvas.patchDirtyHexes(editedHexes);
        }
        canvas.repaint();
    }

    private void sampleHex(Hex hex) {
        if (hex == null) return;
        if (currentStage == STAGE_PLATES && hex.getFaultType() != null) {
            selectedFaultType = hex.getFaultType();
            selectedFaultIntensity = Math.max(1, hex.getFaultIntensity());
        } else if (currentStage == STAGE_LAND) {
            selectedExactElevation = hex.getElevation();
            raiseElevation = hex.getElevation() >= 4;
        } else if (currentStage == STAGE_BIOMES && hex.getBiome() != null) {
            selectedBiome = hex.getBiome();
            for (int cIdx = 0; cIdx < BIOME_CATEGORIES.length; cIdx++) {
                for (Biome b : BIOME_CATEGORIES[cIdx]) {
                    if (b == selectedBiome) {
                        selectedBiomeCategory = cIdx;
                        break;
                    }
                }
            }
        } else if (currentStage == STAGE_DYNASTIES && hex.getColony() != null && hex.getColony().getDynasty() != null) {
            AntSpecies sampled = hex.getColony().getDynasty().getSpecies();
            if (sampled != null) {
                selectedDynastySpecies = sampled;
                if (sampled.getDiet() == GameConstants.DIET_FUNGIVOROUS
                        || sampled.getDiet() == GameConstants.DIET_CARNIVOROUS) {
                    selectedDynastyDiet = sampled.getDiet();
                }
            }
        }
        currentTool = Tool.BRUSH;
        refreshPalette();
    }

    private void applyPaint(Hex hex) {
        if (hex == null) return;
        if (currentStage == STAGE_PLATES) {
            hex.setFaultType(selectedFaultType);
            hex.setFaultIntensity(selectedFaultIntensity);
        } else if (currentStage == STAGE_LAND) {
            int nextElev = selectedExactElevation != null
                    ? Math.max(0, Math.min(10, selectedExactElevation))
                    : Math.max(0, Math.min(10, hex.getElevation() + (raiseElevation ? 1 : -1)));
            hex.setElevation(nextElev);
            updateHexBiomeForElevation(hex, nextElev);
        } else if (currentStage == STAGE_BIOMES) {
            hex.setBiome(selectedBiome);
        } else if (currentStage == STAGE_DYNASTIES) {
            applyDynastyAction(hex);
        }
    }

    private void applyEraser(Hex hex) {
        if (hex == null) return;
        if (currentStage == STAGE_PLATES) {
            hex.setFaultType(null);
            hex.setFaultIntensity(0);
        } else if (currentStage == STAGE_LAND) {
            hex.setElevation(0);
            updateHexBiomeForElevation(hex, 0);
        } else if (currentStage == STAGE_BIOMES) {
            hex.setBiome(GameConstants.BIOME_PLAINS);
        } else if (currentStage == STAGE_DYNASTIES) {
            Colony oldCol = hex.getColony();
            if (oldCol != null) {
                if (oldCol.getDynasty() != null) {
                    oldCol.getDynasty().removeColony(oldCol);
                }
                hex.setColony(null);
                world.markColonizedHexIndexDirty();
            }
        }
    }

    private void applyBucketFill(Hex startHex) {
        if (startHex == null) return;
        if (currentStage == STAGE_PLATES) {
            FaultLine.FaultType origType = startHex.getFaultType();
            if (origType == selectedFaultType) return;
            floodFill(startHex, h -> h.getFaultType() == origType, h -> {
                h.setFaultType(selectedFaultType);
                h.setFaultIntensity(selectedFaultIntensity);
            });
        } else if (currentStage == STAGE_LAND) {
            int originalElev = startHex.getElevation();
            int nextElev = selectedExactElevation != null
                    ? Math.max(0, Math.min(10, selectedExactElevation))
                    : Math.max(0, Math.min(10, originalElev + (raiseElevation ? 1 : -1)));
            if (nextElev == originalElev) return;
            floodFill(startHex, h -> h.getElevation() == originalElev, h -> {
                h.setElevation(nextElev);
                updateHexBiomeForElevation(h, nextElev);
            });
        } else if (currentStage == STAGE_BIOMES) {
            Biome originalBiome = startHex.getBiome();
            if (originalBiome == selectedBiome) return;
            floodFill(startHex, h -> h.getBiome() == originalBiome, h -> h.setBiome(selectedBiome));
        }
    }

    private void updateHexBiomeForElevation(Hex hex, int elev) {
        if (elev <= 0) {
            hex.setBiome(GameConstants.BIOME_ABYSS);
        } else if (elev == 1) {
            hex.setBiome(GameConstants.BIOME_DEEP_OCEAN);
        } else if (elev == 2) {
            hex.setBiome(GameConstants.BIOME_OCEAN);
        } else if (elev == 3) {
            hex.setBiome(GameConstants.BIOME_SHALLOW_OCEAN);
        } else if (hex.getBiome() == null || hex.getBiome() == GameConstants.BIOME_OCEAN
                || hex.getBiome() == GameConstants.BIOME_DEEP_OCEAN
                || hex.getBiome() == GameConstants.BIOME_SHALLOW_OCEAN
                || hex.getBiome() == GameConstants.BIOME_ABYSS) {
            hex.setBiome(GameConstants.BIOME_PLAINS);
        }
    }

    private void applyDynastyAction(Hex hex) {
        if (hex == null) return;
        if (dynastyAction == 2) {
            Colony oldCol = hex.getColony();
            if (oldCol != null) {
                if (oldCol.getDynasty() != null) {
                    oldCol.getDynasty().removeColony(oldCol);
                }
                hex.setColony(null);
                world.markColonizedHexIndexDirty();
            }
            return;
        }
        AntSpecies species = selectedDynastySpecies != null ? selectedDynastySpecies : GameConstants.SPECIES_OMNI;
        Dynasty targetDyn = getOrCreateDynasty(species);
        if (targetDyn == null) {
            return;
        }
        if (dynastyAction == 0) {
            Colony oldCol = hex.getColony();
            if (oldCol != null && oldCol.getDynasty() != null) {
                oldCol.getDynasty().removeColony(oldCol);
            }
            Colony cap = new Colony(world.getNextColonyId(), targetDyn.getName() + " Capital", targetDyn.isPlayer());
            cap.setDynasty(targetDyn);
            targetDyn.addColony(cap);
            ColonyStarterService.shared().initializeNewColony(cap);
            targetDyn.setCapital(cap);
            hex.setColony(cap);
            world.markColonizedHexIndexDirty();
        } else if (dynastyAction == 1) {
            if (hex.getColony() == null) {
                Colony outpost = new Colony(world.getNextColonyId(), targetDyn.getName() + " Outpost", targetDyn.isPlayer());
                outpost.setDynasty(targetDyn);
                targetDyn.addColony(outpost);
                ColonyStarterService.shared().initializeNewColony(outpost);
                hex.setColony(outpost);
                world.markColonizedHexIndexDirty();
            }
        }
    }

    private Dynasty getOrCreateDynasty(AntSpecies species) {
        if (species == null) {
            return null;
        }
        for (Dynasty d : world.getDynastys()) {
            if (d != null && d.getSpecies() != null && d.getSpecies().getId() == species.getId()) {
                return d;
            }
        }
        boolean hasPlayer = false;
        for (Dynasty d : world.getDynastys()) {
            if (d != null && d.isPlayer()) {
                hasPlayer = true;
                break;
            }
        }
        boolean isPlayer = !hasPlayer && species.getId() == GameConstants.SPECIES_OMNI.getId();
        for (Dynasty d : world.getDynastys()) {
            if (d != null) {
                dynastyNamingService.registerUsedThemeKey(d.getThemeBase());
                dynastyNamingService.registerUsedName(d.getName());
            }
        }
        Dynasty dyn = createEditorDynasty(world, species, isPlayer, dynastyNamingService);
        world.registerDynasty(dyn);
        return dyn;
    }

    public static Dynasty createEditorDynasty(
            World world,
            AntSpecies species,
            boolean isPlayer,
            DynastyNamingService namingService) {
        DynastyTitle title = namingService.pickRandomTitle();
        String themeBase = isPlayer ? "Player" : namingService.claimThemeKey(species);
        String dynName = LanguageStrings.formatDynastyName(themeBase, title);
        Dynasty dyn = new Dynasty(world.allocateDynastyId(), dynName, title.getNameKey(), isPlayer, species);
        dyn.setThemeBase(themeBase);
        return dyn;
    }

    private List<Hex> computeHexLine(Hex start, Hex end) {
        if (world == null || start == null || end == null) {
            return Collections.emptyList();
        }
        int q1 = start.getQ();
        int r1 = start.getR();
        int s1 = -q1 - r1;
        int q2 = end.getQ();
        int r2 = end.getR();
        int s2 = -q2 - r2;

        int steps = Math.max(Math.abs(q1 - q2), Math.max(Math.abs(r1 - r2), Math.abs(s1 - s2)));
        if (steps == 0) {
            return List.of(start);
        }
        List<Hex> line = new ArrayList<>(steps + 1);
        Set<Hex> seen = new LinkedHashSet<>();
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double fq = q1 + (q2 - q1) * t + 1e-6;
            double fr = r1 + (r2 - r1) * t + 1e-6;
            double fs = s1 + (s2 - s1) * t - 2e-6;

            long rq = Math.round(fq);
            long rr = Math.round(fr);
            long rs = Math.round(fs);
            double dq = Math.abs(rq - fq);
            double dr = Math.abs(rr - fr);
            double ds = Math.abs(rs - fs);
            if (dq > dr && dq > ds) {
                rq = -rr - rs;
            } else if (dr > ds) {
                rr = -rq - rs;
            }

            Hex h = world.findHex((int) rq, (int) rr);
            if (h != null && seen.add(h)) {
                line.add(h);
            }
        }
        return line;
    }

    private void applyLineTool(Hex start, Hex end) {
        List<Hex> lineHexes = computeHexLine(start, end);
        if (lineHexes.isEmpty()) {
            return;
        }
        Set<Hex> targets = new LinkedHashSet<>();
        for (Hex lineHex : lineHexes) {
            targets.addAll(getHexesInRadius(lineHex, brushRadius));
        }
        if (targets.isEmpty()) {
            return;
        }
        pushUndoSnapshot();
        for (Hex h : targets) {
            applyPaint(h);
        }
        if (currentStage == STAGE_PLATES) {
            recalculatePlatesFromFaults();
            canvas.invalidateStageImages();
        } else if (currentStage == STAGE_DYNASTIES && dynastyAction == 0) {
            canvas.invalidateStageImages();
        } else {
            canvas.patchDirtyHexes(new ArrayList<>(targets));
        }
        canvas.repaint();
    }

    private void floodFill(Hex start, Predicate<Hex> condition, Consumer<Hex> action) {
        Set<Hex> visited = new HashSet<>();
        Queue<Hex> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            Hex curr = queue.poll();
            action.accept(curr);
            for (Hex n : curr.getAdjacentNeighbors()) {
                if (n != null && !visited.contains(n) && condition.test(n)) {
                    visited.add(n);
                    queue.add(n);
                }
            }
        }
    }

    private List<Hex> getHexesInRadius(Hex center, int radius) {
        if (center == null) {
            return Collections.emptyList();
        }
        if (radius <= 1) {
            return List.of(center);
        }
        int maxDist = radius - 1;
        List<Hex> list = new ArrayList<>();
        Set<Hex> visited = new HashSet<>();
        ArrayDeque<Hex> queue = new ArrayDeque<>();
        Map<Hex, Integer> distMap = new HashMap<>();
        visited.add(center);
        queue.add(center);
        distMap.put(center, 0);

        while (!queue.isEmpty()) {
            Hex curr = queue.poll();
            list.add(curr);
            int d = distMap.get(curr);
            if (d >= maxDist) {
                continue;
            }
            for (Hex n : curr.getAdjacentNeighbors()) {
                if (n != null && visited.add(n)) {
                    distMap.put(n, d + 1);
                    queue.add(n);
                }
            }
        }
        return list;
    }

    private class WorldCanvas extends JPanel {
        private static final double BASE_BAKE_HEX_SIZE = 14.0;
        private static final Color HEX_BORDER_COLOR = new Color(0, 0, 0, 30);
        private static final BasicStroke HEX_BORDER_STROKE = new BasicStroke(1f);

        private double hexSize = 22.0;
        private double panX = 0;
        private double panY = 0;
        private boolean cameraFitted = false;
        private Point dragOrigin = null;

        private final int miniW = 220;
        private final int miniH = 120;
        private final int miniMargin = 16;
        private final Icon overviewIcon = UiResourceLoader.loadDialogIcon(getClass(), "/icons/tools/MinimapOverview.png", 12);
        private HexGridMesh cachedMesh;
        private int cachedMeshHexCount = -1;
        private BufferedImage cachedMapImage;
        private int cachedMapMinX;
        private int cachedMapMinY;
        private int cachedMapStage = -1;
        private BufferedImage cachedMinimap;
        private int cachedMinimapStage = -1;

        public WorldCanvas() {
            setBackground(new Color(15, 18, 24));
            setOpaque(true);

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isClickInsideMinimap(e.getX(), e.getY())) {
                        handleMinimapClick(e.getX(), e.getY());
                        return;
                    }
                    if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                        dragOrigin = e.getPoint();
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (currentTool == Tool.LINE) {
                            lineStartHex = pickHex(e.getX(), e.getY());
                            lineEndHex = lineStartHex;
                            repaint();
                        } else {
                            currentStrokeHexes.clear();
                            currentStrokeSnapshotPushed = false;
                            strokeModifiedPlates = false;
                            Hex hex = pickHex(e.getX(), e.getY());
                            applyToolAt(hex);
                        }
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isClickInsideMinimap(e.getX(), e.getY())) {
                        handleMinimapClick(e.getX(), e.getY());
                        return;
                    }
                    if (dragOrigin != null && (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e))) {
                        panX += (e.getX() - dragOrigin.x);
                        panY += (e.getY() - dragOrigin.y);
                        dragOrigin = e.getPoint();
                        repaint();
                    } else if (SwingUtilities.isLeftMouseButton(e) && currentTool == Tool.LINE) {
                        Hex hovered = pickHex(e.getX(), e.getY());
                        if (hovered != null) {
                            lineEndHex = hovered;
                            repaint();
                        }
                    } else if (SwingUtilities.isLeftMouseButton(e) && currentTool != Tool.FILL) {
                        Hex hex = pickHex(e.getX(), e.getY());
                        applyToolAt(hex);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                        dragOrigin = null;
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (currentTool == Tool.LINE) {
                            if (lineStartHex != null && lineEndHex != null) {
                                applyLineTool(lineStartHex, lineEndHex);
                            }
                            lineStartHex = null;
                            lineEndHex = null;
                            repaint();
                        } else {
                            currentStrokeHexes.clear();
                            currentStrokeSnapshotPushed = false;
                            if (strokeModifiedPlates) {
                                strokeModifiedPlates = false;
                                recalculatePlatesFromFaults();
                                invalidateStageImages();
                                repaint();
                            }
                        }
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double zoomFactor = (e.getWheelRotation() < 0) ? 1.15 : 0.85;
                    double newSize = Math.max(minHexSize(), Math.min(80.0, hexSize * zoomFactor));
                    if (newSize != hexSize) {
                        double centerX = getWidth() / 2.0;
                        double centerY = getHeight() / 2.0;
                        panX = HexMapGeometry.panAfterZoom(panX, e.getX() - centerX, hexSize, newSize);
                        panY = HexMapGeometry.panAfterZoom(panY, e.getY() - centerY, hexSize, newSize);
                        hexSize = newSize;
                        repaint();
                    }
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);
            addMouseWheelListener(ma);
        }

        void invalidateCaches() {
            cachedMesh = null;
            cachedMeshHexCount = -1;
            cachedMapImage = null;
            cachedMinimap = null;
            cameraFitted = false;
        }

        void invalidateStageImages() {
            cachedMesh = null;
            cachedMapImage = null;
            cachedMinimap = null;
        }

        void patchDirtyHexes(List<Hex> targets) {
            if (targets == null || targets.isEmpty() || world == null) {
                return;
            }
            if (cachedMesh != null && cachedMapImage != null && cachedMapStage == currentStage) {
                Graphics2D mg = cachedMapImage.createGraphics();
                try {
                    mg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    mg.translate(-cachedMapMinX, -cachedMapMinY);
                    for (Hex h : targets) {
                        if (h == null) continue;
                        HexGridMesh.Face face = cachedMesh.faceAt(h.getQ(), h.getR());
                        if (face != null) {
                            paintFace(mg, face);
                        }
                    }
                } finally {
                    mg.dispose();
                }
            }
            if (cachedMinimap != null && cachedMinimapStage == currentStage) {
                int contentW = miniW - 16;
                int contentH = miniH - 24;
                int wW = Math.max(1, world.getWorldWidth());
                int wH = Math.max(1, world.getWorldHeight());
                double cellW = (double) contentW / wW;
                double cellH = (double) contentH / wH;
                int minR = -wH / 2;
                int minCol = -wW / 2;
                int fillW = Math.max(1, (int) Math.ceil(cellW));
                int fillH = Math.max(1, (int) Math.ceil(cellH));
                Graphics2D mmg = cachedMinimap.createGraphics();
                try {
                    for (Hex h : targets) {
                        if (h == null) continue;
                        int col = h.getQ() + (h.getR() >> 1);
                        int px = (int) Math.round((col - minCol) * cellW);
                        int py = (int) Math.round((h.getR() - minR) * cellH);
                        mmg.setColor(getMinimapCellColor(h));
                        mmg.fillRect(px, py, fillW, fillH);
                    }
                } finally {
                    mmg.dispose();
                }
            }
        }

        private HexGridMesh ensureMesh() {
            List<Hex> hexes = world.getHexes();
            int count = hexes != null ? hexes.size() : 0;
            if (cachedMesh != null && cachedMeshHexCount == count) {
                return cachedMesh;
            }
            cachedMesh = HexGridMesh.build(hexes, 0.0, 0.0, BASE_BAKE_HEX_SIZE, world::getHexAt);
            cachedMeshHexCount = count;
            cachedMapImage = null;
            return cachedMesh;
        }

        private void ensureBakedMapImage(HexGridMesh mesh) {
            if (cachedMapImage != null && cachedMapStage == currentStage) {
                return;
            }
            int minX = mesh.boundsMinX();
            int minY = mesh.boundsMinY();
            int mapW = mesh.boundsMaxX() - minX + 1;
            int mapH = mesh.boundsMaxY() - minY + 1;
            if (mapW <= 0 || mapH <= 0 || mapW > 4096 || mapH > 4096) {
                cachedMapImage = null;
                return;
            }
            BufferedImage img = new BufferedImage(mapW, mapH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D mg = img.createGraphics();
            try {
                mg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                mg.translate(-minX, -minY);
                for (HexGridMesh.Face face : mesh.faces()) {
                    paintFace(mg, face);
                }
            } finally {
                mg.dispose();
            }
            cachedMapImage = img;
            cachedMapMinX = minX;
            cachedMapMinY = minY;
            cachedMapStage = currentStage;
        }

        private void paintFace(Graphics2D g2d, HexGridMesh.Face face) {
            Hex h = face.hex;
            if (h == null) return;
            paintHexShape(
                    g2d,
                    h,
                    face.toPolygon(),
                    (int) Math.round(face.centerX),
                    (int) Math.round(face.centerY),
                    BASE_BAKE_HEX_SIZE);
        }

        private void paintHexShape(Graphics2D g2d, Hex h, Polygon poly, int cx, int cy, double renderHexSize) {
            g2d.setColor(getCellColor(h));
            g2d.fillPolygon(poly);

            g2d.setColor(HEX_BORDER_COLOR);
            g2d.setStroke(HEX_BORDER_STROKE);
            g2d.drawPolygon(poly);

            if (currentStage == STAGE_PLATES && h.getFaultType() != null) {
                paintFaultIndicator(g2d, cx, cy, h.getFaultType(), h.getFaultIntensity(), renderHexSize);
            }

            if (currentStage == STAGE_DYNASTIES && h.getColony() != null) {
                ImageIcon badge = h.getColony().isCapital()
                        ? (GameConstants.ROLE_COMMANDER != null ? GameConstants.ROLE_COMMANDER.getIcon() : null)
                        : (GameConstants.RANK_COLONY != null ? GameConstants.RANK_COLONY.getIcon() : null);
                int badgeSize = Math.max(10, (int) Math.round(renderHexSize * 0.72));
                if (badge != null && badge.getImage() != null) {
                    g2d.drawImage(
                            badge.getImage(),
                            cx - badgeSize / 2,
                            cy - badgeSize / 2,
                            badgeSize,
                            badgeSize,
                            null);
                } else {
                    g2d.setColor(h.getColony().isPlayer() ? Color.YELLOW : Color.CYAN);
                    g2d.fillOval(cx - 5, cy - 5, 10, 10);
                }
            }
        }

        private boolean isClickInsideMinimap(int mx, int my) {
            int rx = getWidth() - miniW - miniMargin;
            int ry = getHeight() - miniH - miniMargin;
            return mx >= rx && mx <= rx + miniW && my >= ry && my <= ry + miniH;
        }

        private void handleMinimapClick(int mx, int my) {
            if (world == null || world.getWorldWidth() <= 0 || world.getWorldHeight() <= 0) {
                return;
            }
            int rx = getWidth() - miniW - miniMargin;
            int ry = getHeight() - miniH - miniMargin;
            double normX = (double) (mx - rx) / miniW;
            double normY = (double) (my - ry) / miniH;

            int targetCol = (int) Math.round((normX - 0.5) * world.getWorldWidth());
            int targetR = (int) Math.round((normY - 0.5) * world.getWorldHeight());
            int targetQ = targetCol - (targetR >> 1);

            double targetCenterX = HexMapGeometry.flatTopCenterX(targetQ, targetR, hexSize);
            double targetCenterY = HexMapGeometry.flatTopCenterY(targetQ, targetR, hexSize);

            panX = -targetCenterX;
            panY = -targetCenterY;
            repaint();
        }

        private double minHexSize() {
            if (world == null || world.getWorldWidth() <= 0 || world.getWorldHeight() <= 0) {
                return 4.0;
            }
            double fit = HexMapGeometry.fittedRectangularHexSize(
                    world.getWorldWidth(),
                    world.getWorldHeight(),
                    Math.max(100, getWidth() - 40),
                    Math.max(100, getHeight() - 40),
                    1.0,
                    36.0);
            return Math.min(6.0, fit);
        }

        public void resetCamera() {
            panX = 0;
            panY = 0;
            if (world != null && world.getWorldWidth() > 0 && world.getWorldHeight() > 0) {
                hexSize = HexMapGeometry.fittedRectangularHexSize(
                        world.getWorldWidth(),
                        world.getWorldHeight(),
                        Math.max(100, getWidth() - 40),
                        Math.max(100, getHeight() - 40),
                        1.0,
                        36.0);
                cameraFitted = getWidth() > 0 && getHeight() > 0;
            } else {
                hexSize = 22.0;
                cameraFitted = false;
            }
            repaint();
        }

        private Hex pickHex(int screenX, int screenY) {
            if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
                return null;
            }
            double originX = getWidth() / 2.0 + panX;
            double originY = getHeight() / 2.0 + panY;
            Point axial = HexMapGeometry.flatTopAxialRound(screenX - originX, screenY - originY, hexSize);
            return world.findHex(axial.x, axial.y);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
                return;
            }
            if (!cameraFitted && getWidth() > 0 && getHeight() > 0 && world.getWorldWidth() > 0 && world.getWorldHeight() > 0) {
                panX = 0;
                panY = 0;
                hexSize = HexMapGeometry.fittedRectangularHexSize(
                        world.getWorldWidth(),
                        world.getWorldHeight(),
                        Math.max(100, getWidth() - 40),
                        Math.max(100, getHeight() - 40),
                        1.0,
                        36.0);
                cameraFitted = true;
            }
            Graphics2D g2d = (Graphics2D) g.create();
            double originX = getWidth() / 2.0 + panX;
            double originY = getHeight() / 2.0 + panY;

            HexGridMesh mesh = ensureMesh();
            ensureBakedMapImage(mesh);

            if (cachedMapImage != null && hexSize <= 18.0) {
                double scale = hexSize / BASE_BAKE_HEX_SIZE;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(
                        cachedMapImage,
                        (int) Math.round(originX + cachedMapMinX * scale),
                        (int) Math.round(originY + cachedMapMinY * scale),
                        (int) Math.round(cachedMapImage.getWidth() * scale),
                        (int) Math.round(cachedMapImage.getHeight() * scale),
                        null);
            } else {
                Graphics2D hg = (Graphics2D) g2d.create();
                hg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                hg.translate(originX, originY);
                double minVisX = -originX - hexSize * 2.0;
                double maxVisX = -originX + getWidth() + hexSize * 2.0;
                double minVisY = -originY - hexSize * 2.0;
                double maxVisY = -originY + getHeight() + hexSize * 2.0;
                for (Hex h : world.getHexes()) {
                    if (h == null) continue;
                    double cx = HexMapGeometry.flatTopCenterX(h.getQ(), h.getR(), hexSize);
                    double cy = HexMapGeometry.flatTopCenterY(h.getQ(), h.getR(), hexSize);
                    if (cx < minVisX || cx > maxVisX || cy < minVisY || cy > maxVisY) {
                        continue;
                    }
                    Polygon poly = HexMapGeometry.flatTopPolygon(h.getQ(), h.getR(), 0.0, 0.0, hexSize);
                    paintHexShape(hg, h, poly, (int) Math.round(cx), (int) Math.round(cy), hexSize);
                }
                hg.dispose();
            }

            Graphics2D og = (Graphics2D) g2d.create();
            og.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            og.translate(originX, originY);
            if (mesh != null) {
                double scale = hexSize / BASE_BAKE_HEX_SIZE;
                Graphics2D rg = (Graphics2D) og.create();
                rg.scale(scale, scale);
                mesh.paintRivers(rg);
                rg.dispose();
            }
            if (world.getWorldWidth() > 0) {
                double worldW = world.getWorldWidth() * 1.5 * hexSize;
                double topY = -(world.getWorldHeight() * 1.5 * hexSize) / 2.0 - 0.75 * hexSize;
                double botY = +(world.getWorldHeight() * 1.5 * hexSize) / 2.0 - 0.75 * hexSize;
                double eqY = -0.75 * hexSize;
                float[] dash = {6f, 4f};
                og.setColor(new Color(255, 255, 255, 130));
                og.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
                og.drawLine(0, (int) Math.round(topY), 0, (int) Math.round(botY));
                og.drawLine((int) Math.round(-worldW / 2.0), (int) Math.round(eqY), (int) Math.round(worldW / 2.0), (int) Math.round(eqY));
            }
            og.dispose();

            if (currentTool == Tool.LINE && lineStartHex != null && lineEndHex != null) {
                Graphics2D lg = (Graphics2D) g2d.create();
                lg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                lg.translate(originX, originY);
                List<Hex> previewLine = computeHexLine(lineStartHex, lineEndHex);
                Set<Hex> previewTargets = new LinkedHashSet<>();
                for (Hex h : previewLine) {
                    previewTargets.addAll(getHexesInRadius(h, brushRadius));
                }
                lg.setColor(new Color(255, 255, 180, 95));
                for (Hex h : previewTargets) {
                    lg.fillPolygon(HexMapGeometry.flatTopPolygon(h.getQ(), h.getR(), 0.0, 0.0, hexSize));
                }
                lg.setColor(new Color(255, 235, 110, 210));
                lg.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, new float[] {6f, 4f}, 0f));
                double sx = HexMapGeometry.flatTopCenterX(lineStartHex.getQ(), lineStartHex.getR(), hexSize);
                double sy = HexMapGeometry.flatTopCenterY(lineStartHex.getQ(), lineStartHex.getR(), hexSize);
                double ex = HexMapGeometry.flatTopCenterX(lineEndHex.getQ(), lineEndHex.getR(), hexSize);
                double ey = HexMapGeometry.flatTopCenterY(lineEndHex.getQ(), lineEndHex.getR(), hexSize);
                lg.drawLine((int) Math.round(sx), (int) Math.round(sy), (int) Math.round(ex), (int) Math.round(ey));
                lg.dispose();
            }

            paintMinimap(g2d);
            g2d.dispose();
        }

        private void paintFaultIndicator(Graphics2D g2d, int cx, int cy, FaultLine.FaultType type, int intensity, double renderHexSize) {
            Color faultColor;
            if (type == FaultLine.FaultType.CONVERGENT) {
                faultColor = new Color(230, 45, 45);
            } else if (type == FaultLine.FaultType.DIVERGENT) {
                faultColor = new Color(30, 160, 230);
            } else {
                faultColor = new Color(235, 185, 30);
            }
            g2d.setColor(faultColor);
            int maxSafeRadius = Math.max(2, (int) Math.floor(renderHexSize * 0.65) - 4);
            int dotRadius = Math.max(2, Math.min(Math.min(6, maxSafeRadius), (int) Math.round(renderHexSize * 0.22)));
            g2d.fillOval(cx - dotRadius, cy - dotRadius, dotRadius * 2, dotRadius * 2);

            if (intensity >= 2) {
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawOval(cx - dotRadius - 2, cy - dotRadius - 2, (dotRadius + 2) * 2, (dotRadius + 2) * 2);
            }
            if (intensity >= 3) {
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawOval(cx - dotRadius - 4, cy - dotRadius - 4, (dotRadius + 4) * 2, (dotRadius + 4) * 2);
            }
        }

        private void ensureBakedMinimap() {
            int contentW = miniW - 16;
            int contentH = miniH - 24;
            if (cachedMinimap != null && cachedMinimapStage == currentStage
                    && cachedMinimap.getWidth() == contentW && cachedMinimap.getHeight() == contentH) {
                return;
            }
            BufferedImage img = new BufferedImage(contentW, contentH, BufferedImage.TYPE_INT_RGB);
            Graphics2D mmg = img.createGraphics();
            try {
                mmg.setColor(new Color(12, 16, 22));
                mmg.fillRect(0, 0, contentW, contentH);
                int wW = Math.max(1, world.getWorldWidth());
                int wH = Math.max(1, world.getWorldHeight());
                double cellW = (double) contentW / wW;
                double cellH = (double) contentH / wH;
                int minR = -wH / 2;
                int minCol = -wW / 2;
                int fillW = Math.max(1, (int) Math.ceil(cellW));
                int fillH = Math.max(1, (int) Math.ceil(cellH));

                for (Hex h : world.getHexes()) {
                    if (h == null) continue;
                    int col = h.getQ() + (h.getR() >> 1);
                    int px = (int) Math.round((col - minCol) * cellW);
                    int py = (int) Math.round((h.getR() - minR) * cellH);
                    mmg.setColor(getMinimapCellColor(h));
                    mmg.fillRect(px, py, fillW, fillH);
                }
            } finally {
                mmg.dispose();
            }
            cachedMinimap = img;
            cachedMinimapStage = currentStage;
        }

        private Color getMinimapCellColor(Hex h) {
            if (currentStage == STAGE_PLATES && h.getFaultType() == null && h.getPlateId() <= 0) {
                if (h.getBiome() != null && h.getBiome() != GameConstants.BIOME_OCEAN) {
                    return h.getBiome().getMapColor();
                }
                if (h.getElevation() >= 4) {
                    return MapDialog.HEIGHT_COLORS[Math.max(0, Math.min(10, h.getElevation()))];
                }
            }
            return getCellColor(h);
        }

        private void paintMinimap(Graphics2D g2d) {
            int rx = getWidth() - miniW - miniMargin;
            int ry = getHeight() - miniH - miniMargin;

            g2d.setColor(new Color(12, 16, 22, 230));
            g2d.fillRoundRect(rx, ry, miniW, miniH, 8, 8);
            g2d.setColor(AssetStyles.UI_BORDER_COLOR);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(rx, ry, miniW, miniH, 8, 8);

            g2d.setColor(AssetStyles.FONT_COLOR_HEADER);
            g2d.setFont(AssetStyles.FONT_SMALL);
            if (overviewIcon != null) {
                overviewIcon.paintIcon(this, g2d, rx + 6, ry + 4);
                g2d.drawString("OVERVIEW", rx + 22, ry + 14);
            } else {
                g2d.drawString("OVERVIEW", rx + 8, ry + 14);
            }

            ensureBakedMinimap();
            if (cachedMinimap != null) {
                g2d.drawImage(cachedMinimap, rx + 8, ry + 20, null);
            }

            int wW = Math.max(1, world.getWorldWidth());
            int wH = Math.max(1, world.getWorldHeight());
            double viewNormW = (double) getWidth() / (wW * hexSize * Math.sqrt(3.0));
            double viewNormH = (double) getHeight() / (wH * hexSize * 1.5);
            double viewNormCenterX = 0.5 - (panX / (wW * hexSize * Math.sqrt(3.0)));
            double viewNormCenterY = 0.5 - (panY / (wH * hexSize * 1.5));

            int vW = (int) Math.round(viewNormW * (miniW - 16));
            int vH = (int) Math.round(viewNormH * (miniH - 24));
            int vx = rx + 8 + (int) Math.round((viewNormCenterX - viewNormW / 2.0) * (miniW - 16));
            int vy = ry + 20 + (int) Math.round((viewNormCenterY - viewNormH / 2.0) * (miniH - 24));

            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRect(Math.max(rx + 8, vx), Math.max(ry + 20, vy), Math.min(miniW - 16, Math.max(4, vW)), Math.min(miniH - 24, Math.max(4, vH)));
        }

        private Color getCellColor(Hex h) {
            if (currentStage == STAGE_PLATES) {
                if (h.getFaultType() != null) {
                    if (h.getFaultType() == FaultLine.FaultType.CONVERGENT) return new Color(225, 45, 45);
                    if (h.getFaultType() == FaultLine.FaultType.DIVERGENT) return new Color(30, 160, 230);
                    return new Color(235, 185, 30);
                }
                int pid = h.getPlateId();
                if (pid <= 0) return new Color(40, 50, 65);
                return PLATE_COLORS[(pid - 1) % PLATE_COLORS.length];
            }
            if (currentStage == STAGE_LAND) {
                int elev = Math.max(0, Math.min(10, h.getElevation()));
                return MapDialog.HEIGHT_COLORS[elev];
            }
            if (currentStage == STAGE_BIOMES) {
                return (h.getBiome() != null) ? h.getBiome().getMapColor() : new Color(33, 75, 112);
            }
            if (currentStage == STAGE_DYNASTIES) {
                if (h.getColony() != null && h.getColony().getDynasty() != null) {
                    Color dynColor = h.getColony().getDynasty().getColor();
                    if (dynColor != null) {
                        return dynColor;
                    }
                    return h.getColony().isPlayer() ? new Color(117, 23, 23) : new Color(50, 95, 160);
                }
                return (h.getBiome() != null) ? h.getBiome().getMapColor() : new Color(33, 75, 112);
            }
            return new Color(33, 75, 112);
        }
    }
}
