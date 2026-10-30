package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.dynasty.Rank;
import com.grimidk.formicempire.classes.constants.dynasty.DiplomaticReputation;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.world.Continent;
import com.grimidk.formicempire.classes.entities.world.FaultLine;
import com.grimidk.formicempire.classes.entities.world.GeographicFeature;
import com.grimidk.formicempire.classes.entities.services.shared.SandboxCheatService;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEvent;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.game.rendering.HexGridMesh;
import com.grimidk.formicempire.classes.interfaces.game.rendering.HexMapGeometry;
import com.grimidk.formicempire.classes.interfaces.game.rendering.HexMapOverlays;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.DynastyColorSwatch;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatChevronButton;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiScrollBarStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiTableStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.EdgeTriggeredKeyBindings;
import com.grimidk.formicempire.classes.interfaces.ui.util.GameControlKeyBindings;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapDialog extends ZeroDialog {

    private static final Dimension SPEED_ICON_SLOT_SIZE = new Dimension(32, 32);

    private final Engine engine;
    private World world;
    private final HexMapPanel mapPanel;
    private final LegendPanel legendPanel;
    private final JLabel dateTimeLabel;
    private final JButton speedDownButton;
    private final JButton speedUpButton;
    private final JLabel tickLabel;
    private final JButton playPauseButton;
    private final JButton homeButton;
    private final JButton zoomOutButton;
    private final JButton zoomInButton;
    private final JSlider zoomSlider;
    private final JPanel zoomRail;
    private final JButton layersButton;
    private final JButton modesButton;
    private final JButton conquerButton;
    private final JButton closeButton;
    private final Runnable onHexChange;
    private final Runnable onOpenWarDialog;
    private final Runnable onBackToMenu;
    private boolean showBorders = true;
    private boolean showRivers = true;
    private boolean showMeridian = true;
    private boolean showEquator = true;
    private boolean showBiomeIcons = true;
    private boolean showColonyRanks = true;
    private boolean showTrades = true;
    private boolean showTunnels = true;
    public enum MapMode {
        BIOME, HEIGHT, TECTONIC_PLATES
    }
    private MapMode mapMode = MapMode.BIOME;
    public static final Color FAULT_CONVERGENT_COLOR = new Color(175, 65, 230);
    public static final Color FAULT_DIVERGENT_COLOR = new Color(245, 215, 30);
    public static final Color FAULT_TRANSFORM_COLOR = new Color(50, 150, 255);
    private static final Color[] PLATE_COLORS = {
        new Color(185, 85, 85),
        new Color(65, 145, 185),
        new Color(75, 165, 105),
        new Color(185, 145, 65),
        new Color(145, 95, 185),
        new Color(75, 175, 165),
        new Color(195, 105, 145),
        new Color(135, 165, 65),
        new Color(165, 115, 85),
        new Color(105, 125, 195),
        new Color(165, 165, 75),
        new Color(135, 95, 145),
        new Color(95, 165, 145),
        new Color(195, 135, 105),
        new Color(115, 145, 115),
        new Color(155, 105, 115)
    };
    private static final Color[] HEIGHT_COLORS = {
        new Color(110, 15, 20),
        new Color(160, 25, 25),
        new Color(200, 45, 30),
        new Color(225, 75, 30),
        new Color(240, 115, 35),
        new Color(250, 155, 40),
        new Color(252, 190, 45),
        new Color(252, 220, 60),
        new Color(254, 242, 120),
        new Color(255, 250, 190),
        new Color(255, 255, 255)
    };
    private boolean showBattles = true;
    private boolean showGeographicNames = true;
    private boolean showCapitals = true;
    private boolean showLabelContinents = true;
    private boolean showLabelIceCaps = true;
    private boolean showLabelArchipelagos = true;
    private boolean showLabelOceans = true;
    private boolean showLabelTrenches = true;
    private boolean showLabelMountains = true;
    private boolean showLabelValleys = true;
    private boolean showLabelLakes = true;
    private boolean showLabelRivers = true;
    private boolean showLabelPeninsulas = true;
    private boolean syncingZoomUi;
    private boolean holdingLeftA;
    private boolean holdingLeftArrow;
    private boolean holdingRightD;
    private boolean holdingRightArrow;
    private boolean holdingZoomInW;
    private boolean holdingZoomInArrow;
    private boolean holdingZoomOutS;
    private boolean holdingZoomOutArrow;
    private Timer navHoldTimer;
    private int navZoomHoldTicks;

    public MapDialog(JFrame owner, Engine engine, World world, Runnable onHexChange, Runnable onOpenWarDialog) {
        this((Window) owner, engine, world, onHexChange, onOpenWarDialog, null);
    }

    public MapDialog(JFrame owner, Engine engine, World world, Runnable onHexChange, Runnable onOpenWarDialog, Runnable onBackToMenu) {
        this((Window) owner, engine, world, onHexChange, onOpenWarDialog, onBackToMenu);
    }

    public MapDialog(Window owner, Engine engine, World world, Runnable onHexChange, Runnable onOpenWarDialog) {
        this(owner, engine, world, onHexChange, onOpenWarDialog, null);
    }

    public MapDialog(Window owner, Engine engine, World world, Runnable onHexChange, Runnable onOpenWarDialog, Runnable onBackToMenu) {
        super(owner, LanguageStrings.DIALOG_MAP_TITLE, owner != null ? owner.getSize() : AssetStyles.MAP_DIALOG_SIZE,
                owner instanceof JDialog ? ModalityType.DOCUMENT_MODAL : ModalityType.APPLICATION_MODAL);
        this.engine = engine;
        this.world = world;
        if (this.world != null) {
            this.world.ensureGeographicFeaturesReconstructed();
        }
        this.onHexChange = onHexChange;
        this.onOpenWarDialog = onOpenWarDialog;
        this.onBackToMenu = onBackToMenu;
        loadLayerSettingsFromEngine();

        this.mapPanel = new HexMapPanel();
        this.legendPanel = new LegendPanel();

        dateTimeLabel = new JLabel("00:00 01/01/0000");
        dateTimeLabel.setFont(AssetStyles.FONT_NORMAL);
        dateTimeLabel.setForeground(AssetStyles.FONT_COLOR);
        dateTimeLabel.setBorder(new EmptyBorder(0, 0, 0, 8));

        speedDownButton = new JButton();
        speedDownButton.setFocusable(false);
        configureIconControlButton(speedDownButton, GameConstants.ICON_SPEED_DOWN);
        speedDownButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_CONTROL_SPEED_DOWN_TT));
        speedDownButton.addActionListener(e -> changeSpeedFromMap(-1));

        speedUpButton = new JButton();
        speedUpButton.setFocusable(false);
        configureIconControlButton(speedUpButton, GameConstants.ICON_SPEED_UP);
        speedUpButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_CONTROL_SPEED_UP_TT));
        speedUpButton.addActionListener(e -> changeSpeedFromMap(1));

        tickLabel = new JLabel();
        tickLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tickLabel.setVerticalAlignment(SwingConstants.CENTER);
        tickLabel.setIconTextGap(0);
        tickLabel.setText("");
        tickLabel.setPreferredSize(SPEED_ICON_SLOT_SIZE);
        tickLabel.setMinimumSize(SPEED_ICON_SLOT_SIZE);
        tickLabel.setMaximumSize(SPEED_ICON_SLOT_SIZE);

        playPauseButton = new JButton();
        playPauseButton.setFocusable(false);
        configureIconControlButton(playPauseButton, GameConstants.ICON_SPEED_PAUSE);
        playPauseButton.addActionListener(e -> togglePauseFromMap());

        homeButton = new JButton(LanguageStrings.get(LanguageStrings.MAP_HOME_BUTTON));
        homeButton.setFocusable(false);
        AssetStyles.styleButton(homeButton);
        homeButton.addActionListener(e -> travelToHomeHex());

        zoomInButton = createZoomStepButton("+", LanguageStrings.MAP_ZOOM_IN_TT, 1.0);
        zoomOutButton = createZoomStepButton("\u2212", LanguageStrings.MAP_ZOOM_OUT_TT, -1.0);

        zoomSlider = new JSlider(
                JSlider.VERTICAL,
                0,
                GameNumbers.MAP_ZOOM_STEPS.length - 1,
                GameNumbers.MAP_ZOOM_DEFAULT_STEP_INDEX);
        zoomSlider.setFocusable(false);
        zoomSlider.setPaintTicks(false);
        zoomSlider.setPaintLabels(false);
        zoomSlider.setSnapToTicks(true);
        zoomSlider.setMajorTickSpacing(1);
        AssetStyles.styleSlider(zoomSlider);
        Dimension zoomTrack = new Dimension(
                AssetStyles.MIN_CONTROL_HIT_SIZE,
                GameNumbers.MAP_ZOOM_SLIDER_TRACK_HEIGHT);
        zoomSlider.setPreferredSize(zoomTrack);
        zoomSlider.setMinimumSize(zoomTrack);
        zoomSlider.setMaximumSize(zoomTrack);
        zoomSlider.addChangeListener(e -> {
            if (syncingZoomUi || mapPanel == null) {
                return;
            }
            mapPanel.setZoomStepIndex(zoomSlider.getValue());
        });

        zoomRail = new JPanel();
        zoomRail.setOpaque(false);
        zoomRail.setLayout(new BoxLayout(zoomRail, BoxLayout.Y_AXIS));
        zoomRail.setBorder(new EmptyBorder(8, 4, 8, 8));
        zoomInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoomSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoomOutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoomRail.add(zoomInButton);
        zoomRail.add(Box.createVerticalStrut(4));
        zoomRail.add(zoomSlider);
        zoomRail.add(Box.createVerticalStrut(4));
        zoomRail.add(zoomOutButton);

        layersButton = new JButton(LanguageStrings.get(LanguageStrings.MAP_LAYERS));
        layersButton.setFocusable(false);
        layersButton.setToolTipText(LanguageStrings.get(LanguageStrings.MAP_LAYERS_TT));
        layersButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        AssetStyles.styleCompactButton(layersButton);
        Dimension layersBtnSize = AssetStyles.minControlHitSize();
        layersButton.setPreferredSize(layersBtnSize);
        layersButton.setMinimumSize(layersBtnSize);
        layersButton.setMaximumSize(layersBtnSize);
        layersButton.setMargin(new Insets(0, 0, 0, 0));
        layersButton.addActionListener(e -> showLayersMenu());
        zoomRail.add(Box.createVerticalStrut(8));
        zoomRail.add(layersButton);

        modesButton = new JButton(LanguageStrings.get(LanguageStrings.MAP_MODE_BUTTON));
        modesButton.setFocusable(false);
        modesButton.setToolTipText(LanguageStrings.get(LanguageStrings.MAP_MODE_BUTTON_TT));
        modesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        AssetStyles.styleCompactButton(modesButton);
        modesButton.setPreferredSize(layersBtnSize);
        modesButton.setMinimumSize(layersBtnSize);
        modesButton.setMaximumSize(layersBtnSize);
        modesButton.setMargin(new Insets(0, 0, 0, 0));
        modesButton.addActionListener(e -> showModesMenu());
        zoomRail.add(Box.createVerticalStrut(4));
        zoomRail.add(modesButton);

        conquerButton = new JButton(LanguageStrings.get(LanguageStrings.MAP_CONQUER_COLONY));
        conquerButton.setFocusable(false);
        AssetStyles.styleButton(conquerButton);
        conquerButton.setToolTipText(LanguageStrings.get(LanguageStrings.MAP_CONQUER_COLONY_TT));
        conquerButton.addActionListener(e -> conquerActiveHex());
        updateConquerButton();

        closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        closeButton.setFocusable(false);
        AssetStyles.styleButton(closeButton);
        closeButton.addActionListener(e -> handleCloseButton());

        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        bottomPanel.add(dateTimeLabel);
        bottomPanel.add(speedDownButton);
        bottomPanel.add(speedUpButton);
        bottomPanel.add(tickLabel);
        bottomPanel.add(playPauseButton);
        bottomPanel.add(homeButton);
        bottomPanel.add(conquerButton);
        bottomPanel.add(closeButton);

        add(legendPanel, BorderLayout.WEST);
        add(mapPanel, BorderLayout.CENTER);
        add(zoomRail, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                stopNavigationHolding();
            }
        });

        registerCloseKey(KeyEvent.VK_M);
        registerNavigationKeys();
        updateZoomControls();
        updateSpeedAndTimeControls();
    }

    private void registerNavigationKeys() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        InputMap ancestorMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getRootPane().getActionMap();

        bindNavKey(inputMap, ancestorMap, actionMap, KeyEvent.VK_A, "mapPanLeft", "mapPanLeftRelease", () -> {
            holdingLeftA = true;
            if (mapPanel != null) {
                mapPanel.panContinuous(-1);
            }
            ensureNavTimer();
        }, () -> {
            holdingLeftA = false;
            checkStopNavTimer();
        });
        bindNavKey(inputMap, ancestorMap, actionMap, KeyEvent.VK_LEFT, "mapPanLeftArrow", "mapPanLeftArrowRelease", () -> {
            holdingLeftArrow = true;
            if (mapPanel != null) {
                mapPanel.panContinuous(-1);
            }
            ensureNavTimer();
        }, () -> {
            holdingLeftArrow = false;
            checkStopNavTimer();
        });
        bindNavKey(inputMap, ancestorMap, actionMap, KeyEvent.VK_D, "mapPanRight", "mapPanRightRelease", () -> {
            holdingRightD = true;
            if (mapPanel != null) {
                mapPanel.panContinuous(1);
            }
            ensureNavTimer();
        }, () -> {
            holdingRightD = false;
            checkStopNavTimer();
        });
        bindNavKey(inputMap, ancestorMap, actionMap, KeyEvent.VK_RIGHT, "mapPanRightArrow", "mapPanRightArrowRelease", () -> {
            holdingRightArrow = true;
            if (mapPanel != null) {
                mapPanel.panContinuous(1);
            }
            ensureNavTimer();
        }, () -> {
            holdingRightArrow = false;
            checkStopNavTimer();
        });
        bindNavKey(inputMap, ancestorMap, actionMap, KeyEvent.VK_W, "mapZoomIn", "mapZoomInRelease", () -> {
            holdingZoomInW = true;
            if (mapPanel != null) {
                mapPanel.zoomStep(1);
            }
            ensureNavTimer();
        }, () -> {
            holdingZoomInW = false;
            checkStopNavTimer();
        });
        bindNavKey(inputMap, ancestorMap, actionMap, KeyEvent.VK_UP, "mapZoomInArrow", "mapZoomInArrowRelease", () -> {
            holdingZoomInArrow = true;
            if (mapPanel != null) {
                mapPanel.zoomStep(1);
            }
            ensureNavTimer();
        }, () -> {
            holdingZoomInArrow = false;
            checkStopNavTimer();
        });
        bindNavKey(inputMap, ancestorMap, actionMap, KeyEvent.VK_S, "mapZoomOut", "mapZoomOutRelease", () -> {
            holdingZoomOutS = true;
            if (mapPanel != null) {
                mapPanel.zoomStep(-1);
            }
            ensureNavTimer();
        }, () -> {
            holdingZoomOutS = false;
            checkStopNavTimer();
        });
        bindNavKey(inputMap, ancestorMap, actionMap, KeyEvent.VK_DOWN, "mapZoomOutArrow", "mapZoomOutArrowRelease", () -> {
            holdingZoomOutArrow = true;
            if (mapPanel != null) {
                mapPanel.zoomStep(-1);
            }
            ensureNavTimer();
        }, () -> {
            holdingZoomOutArrow = false;
            checkStopNavTimer();
        });

        GameControlKeyBindings.register(
                inputMap,
                actionMap,
                this::togglePauseFromMap,
                () -> changeSpeedFromMap(1),
                () -> changeSpeedFromMap(-1));
    }

    private static void configureIconControlButton(JButton button, ImageIcon icon) {
        AssetStyles.styleIconButton(button);
        button.setIcon(icon);
        button.setText("");
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setPreferredSize(SPEED_ICON_SLOT_SIZE);
        button.setMinimumSize(SPEED_ICON_SLOT_SIZE);
        button.setMaximumSize(SPEED_ICON_SLOT_SIZE);
    }

    private boolean isSpectatorMode() {
        World w = currentWorld();
        return w != null && w.isSpectatorMode();
    }

    private boolean hasPlayerDynasty() {
        return !isSpectatorMode() && findPlayerDynasty() != null;
    }

    private void handleCloseButton() {
        dispose();
    }

    private void togglePauseFromMap() {
        Engine eng = resolveEngine();
        if (eng == null) {
            return;
        }
        eng.togglePause();
        if (getOwner() instanceof MainFrame mf && mf.getGamePanel() != null) {
            mf.getGamePanel().onGameSpeedChanged();
            mf.getGamePanel().updateSpeedLabel();
            mf.getGamePanel().updateStatusIndicator(eng.isPaused());
        }
        updateSpeedAndTimeControls();
    }

    private void changeSpeedFromMap(int direction) {
        Engine eng = resolveEngine();
        if (eng == null) {
            return;
        }
        eng.adjustSpeedStep(direction);
        if (getOwner() instanceof MainFrame mf && mf.getGamePanel() != null) {
            mf.getGamePanel().onGameSpeedChanged();
            mf.getGamePanel().updateSpeedLabel();
            mf.getGamePanel().updateStatusIndicator(eng.isPaused());
        }
        updateSpeedAndTimeControls();
    }

    private void updateSpeedAndTimeControls() {
        World w = currentWorld();
        Engine eng = resolveEngine();
        boolean spectator = w != null && w.isSpectatorMode();
        if (homeButton != null) {
            homeButton.setVisible(!spectator);
        }
        if (closeButton != null) {
            closeButton.setText(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        }
        if (dateTimeLabel != null && w != null) {
            Hex activeHex = w.getActiveHex();
            int hour = activeHex != null ? w.getLocalHour(activeHex) : w.getHour();
            int day = activeHex != null ? w.getLocalDay(activeHex) : w.getDay();
            int month = activeHex != null ? w.getLocalMonth(activeHex) : w.getMonth();
            int year = activeHex != null ? w.getLocalYear(activeHex) : w.getYear();
            dateTimeLabel.setText(String.format("%02d:%02d %02d/%02d/%04d",
                    hour, w.getMinute(), day, month, year));
        }
        if (tickLabel != null) {
            if (eng == null) {
                tickLabel.setIcon(null);
                tickLabel.setToolTipText(null);
            } else if (eng.isPaused()) {
                tickLabel.setIcon(GameConstants.ICON_SPEED_ZERO);
                tickLabel.setToolTipText(LanguageStrings.get(LanguageStrings.UI_CONTROL_PAUSED_TT));
            } else {
                GameSpeed speed = eng.getSpeed();
                if (speed != null && speed.getIcon() != null) {
                    tickLabel.setIcon(speed.getIcon());
                    tickLabel.setToolTipText(LanguageStrings.format(
                            LanguageStrings.UI_CONTROL_SPEED_TT, speed.getName(), speed.getDelayMs()));
                } else {
                    tickLabel.setIcon(null);
                    tickLabel.setToolTipText(eng.getSpeedLabel());
                }
            }
        }
        if (playPauseButton != null && eng != null) {
            boolean paused = eng.isPaused();
            playPauseButton.setIcon(paused ? GameConstants.ICON_SPEED_PLAY : GameConstants.ICON_SPEED_PAUSE);
            playPauseButton.setText("");
            playPauseButton.setToolTipText(LanguageStrings.get(
                    paused ? LanguageStrings.UI_CONTROL_PLAY_TT : LanguageStrings.UI_CONTROL_PAUSE_TT));
        }
    }

    private void bindNavKey(
            InputMap inputMap,
            InputMap ancestorMap,
            ActionMap actionMap,
            int keyCode,
            String pressActionId,
            String releaseActionId,
            Runnable onPress,
            Runnable onRelease) {
        EdgeTriggeredKeyBindings.bind(inputMap, ancestorMap, actionMap, keyCode, pressActionId, onPress);
        KeyStroke release = KeyStroke.getKeyStroke(keyCode, 0, true);
        if (inputMap != null) {
            inputMap.put(release, releaseActionId);
        }
        if (ancestorMap != null) {
            ancestorMap.put(release, releaseActionId);
        }
        actionMap.put(releaseActionId, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRelease.run();
            }
        });
    }

    private void ensureNavTimer() {
        if (navHoldTimer == null) {
            navHoldTimer = new Timer(25, e -> onNavTick());
            navHoldTimer.setRepeats(true);
        }
        if (!navHoldTimer.isRunning()) {
            navHoldTimer.start();
        }
    }

    private void stopNavigationHolding() {
        holdingLeftA = false;
        holdingLeftArrow = false;
        holdingRightD = false;
        holdingRightArrow = false;
        holdingZoomInW = false;
        holdingZoomInArrow = false;
        holdingZoomOutS = false;
        holdingZoomOutArrow = false;
        navZoomHoldTicks = 0;
        if (navHoldTimer != null && navHoldTimer.isRunning()) {
            navHoldTimer.stop();
        }
    }

    private void checkStopNavTimer() {
        if (!holdingLeftA && !holdingLeftArrow
                && !holdingRightD && !holdingRightArrow
                && !holdingZoomInW && !holdingZoomInArrow
                && !holdingZoomOutS && !holdingZoomOutArrow) {
            navZoomHoldTicks = 0;
            if (navHoldTimer != null && navHoldTimer.isRunning()) {
                navHoldTimer.stop();
            }
        }
    }

    private void onNavTick() {
        if (mapPanel == null) {
            return;
        }
        int panDir = 0;
        if (holdingLeftA || holdingLeftArrow) {
            panDir -= 1;
        }
        if (holdingRightD || holdingRightArrow) {
            panDir += 1;
        }
        if (panDir != 0) {
            mapPanel.panContinuous(panDir);
        }

        int zoomStepDir = 0;
        if (holdingZoomInW || holdingZoomInArrow) {
            zoomStepDir += 1;
        }
        if (holdingZoomOutS || holdingZoomOutArrow) {
            zoomStepDir -= 1;
        }
        if (zoomStepDir != 0) {
            navZoomHoldTicks++;
            if (navZoomHoldTicks >= 8) {
                navZoomHoldTicks = 0;
                mapPanel.zoomStep(zoomStepDir);
            }
        } else {
            navZoomHoldTicks = 0;
        }
    }

    private JButton createZoomStepButton(String label, String tooltipKey, double notches) {
        JButton button = new JButton(label);
        button.setFocusable(false);
        AssetStyles.styleCompactButton(button);
        Dimension size = AssetStyles.minControlHitSize();
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setMargin(new Insets(0, 0, 0, 0));
        applyZoomStepButtonColors(button);
        button.setToolTipText(LanguageStrings.get(tooltipKey));
        button.addActionListener(e -> mapPanel.zoomStep(notches > 0 ? 1 : -1));
        return button;
    }

    private static void applyZoomStepButtonColors(JButton button) {
        button.setForeground(AssetStyles.FONT_COLOR_BRIGHT);
    }

    public World getWorld() {
        return world;
    }

    public void bindWorld(World world) {
        this.world = world;
        updateConquerButton();
        if (mapPanel != null) {
            mapPanel.repaint();
        }
        if (legendPanel != null) {
            legendPanel.updateLegend();
        }
    }

    public void setLegendVisible(boolean visible) {
        if (legendPanel != null) {
            legendPanel.setVisible(visible);
            revalidate();
            repaint();
        }
    }

    public boolean isLegendVisible() {
        return legendPanel != null && legendPanel.isVisible();
    }

    private Engine resolveEngine() {
        if (engine != null) {
            return engine;
        }
        World current = world;
        if (current != null && current.getEngine() != null) {
            return current.getEngine();
        }
        return null;
    }

    private World currentWorld() {
        if (world != null) {
            return world;
        }
        Engine eng = resolveEngine();
        if (eng != null && eng.getWorld() != null) {
            return eng.getWorld();
        }
        return null;
    }

    private void syncWorldFromEngine() {
        World current = currentWorld();
        if (current != null) {
            this.world = current;
            this.world.ensureGeographicFeaturesReconstructed();
        }
    }

    private void loadLayerSettingsFromEngine() {
        Engine eng = resolveEngine();
        if (eng == null) {
            return;
        }
        showBorders = eng.isMapLayerBorders();
        showRivers = eng.isMapLayerRivers();
        showMeridian = eng.isMapLayerMeridian();
        showEquator = eng.isMapLayerEquator();
        showBiomeIcons = eng.isMapLayerBiomeIcons();
        showCapitals = eng.isMapLayerCapitals();
        showColonyRanks = eng.isMapLayerColonyRanks();
        showTrades = eng.isMapLayerTrades();
        showTunnels = eng.isMapLayerTunnels();
        showBattles = eng.isMapLayerBattles();
        showGeographicNames = eng.isMapLayerGeographicNames();
        showLabelContinents = eng.isMapLayerLabelContinents();
        showLabelIceCaps = eng.isMapLayerLabelIceCaps();
        showLabelArchipelagos = eng.isMapLayerLabelArchipelagos();
        showLabelOceans = eng.isMapLayerLabelOceans();
        showLabelTrenches = eng.isMapLayerLabelTrenches();
        showLabelMountains = eng.isMapLayerLabelMountains();
        showLabelValleys = eng.isMapLayerLabelValleys();
        showLabelLakes = eng.isMapLayerLabelLakes();
        showLabelRivers = eng.isMapLayerLabelRivers();
        showLabelPeninsulas = eng.isMapLayerLabelPeninsulas();
    }

    private void persistLayerSettings() {
        Engine eng = resolveEngine();
        if (eng == null) {
            return;
        }
        eng.setMapLayerBorders(showBorders);
        eng.setMapLayerRivers(showRivers);
        eng.setMapLayerMeridian(showMeridian);
        eng.setMapLayerEquator(showEquator);
        eng.setMapLayerBiomeIcons(showBiomeIcons);
        eng.setMapLayerCapitals(showCapitals);
        eng.setMapLayerColonyRanks(showColonyRanks);
        eng.setMapLayerTrades(showTrades);
        eng.setMapLayerTunnels(showTunnels);
        eng.setMapLayerBattles(showBattles);
        eng.setMapLayerGeographicNames(showGeographicNames);
        eng.setMapLayerLabelContinents(showLabelContinents);
        eng.setMapLayerLabelIceCaps(showLabelIceCaps);
        eng.setMapLayerLabelArchipelagos(showLabelArchipelagos);
        eng.setMapLayerLabelOceans(showLabelOceans);
        eng.setMapLayerLabelTrenches(showLabelTrenches);
        eng.setMapLayerLabelMountains(showLabelMountains);
        eng.setMapLayerLabelValleys(showLabelValleys);
        eng.setMapLayerLabelLakes(showLabelLakes);
        eng.setMapLayerLabelRivers(showLabelRivers);
        eng.setMapLayerLabelPeninsulas(showLabelPeninsulas);
        eng.saveGlobalSettings();
    }

    private static class LayerItemEntry {
        final Layer layer;
        final String label;
        final boolean initialSelected;
        JCheckBox checkBox;

        LayerItemEntry(Layer layer, String label, boolean initialSelected) {
            this.layer = layer;
            this.label = label;
            this.initialSelected = initialSelected;
        }
    }

    private static class LayerGroupPanel {
        final JPanel panel;
        final JCheckBox groupCheckBox;
        final List<LayerItemEntry> items;

        LayerGroupPanel(JPanel panel, JCheckBox groupCheckBox, List<LayerItemEntry> items) {
            this.panel = panel;
            this.groupCheckBox = groupCheckBox;
            this.items = items;
        }

        boolean areAllSelected() {
            for (LayerItemEntry item : items) {
                if (item.checkBox != null && !item.checkBox.isSelected()) {
                    return false;
                }
            }
            return !items.isEmpty();
        }

        void updateGroupCheckState() {
            if (groupCheckBox != null) {
                groupCheckBox.setSelected(areAllSelected());
            }
        }
    }

    void showLayersMenu() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                AssetStyles.BUTTON_BORDER,
                new EmptyBorder(10, 14, 10, 14)));

        List<LayerItemEntry> dynasticItems = List.of(
                new LayerItemEntry(Layer.BORDERS, LanguageStrings.get(LanguageStrings.MAP_LAYER_BORDERS), showBorders),
                new LayerItemEntry(Layer.TRADES, LanguageStrings.get(LanguageStrings.MAP_LAYER_TRADES), showTrades),
                new LayerItemEntry(Layer.TUNNELS, LanguageStrings.get(LanguageStrings.MAP_LAYER_TUNNELS), showTunnels),
                new LayerItemEntry(Layer.CAPITALS, LanguageStrings.get(LanguageStrings.MAP_LAYER_CAPITALS), showCapitals),
                new LayerItemEntry(Layer.COLONY_RANKS, LanguageStrings.get(LanguageStrings.MAP_LAYER_COLONY_RANKS), showColonyRanks),
                new LayerItemEntry(Layer.BATTLES, LanguageStrings.get(LanguageStrings.MAP_LAYER_BATTLES), showBattles)
        );

        List<LayerItemEntry> geographicalItems = List.of(
                new LayerItemEntry(Layer.MERIDIAN, LanguageStrings.get(LanguageStrings.MAP_LAYER_MERIDIAN), showMeridian),
                new LayerItemEntry(Layer.EQUATOR, LanguageStrings.get(LanguageStrings.MAP_LAYER_EQUATOR), showEquator),
                new LayerItemEntry(Layer.RIVERS, LanguageStrings.get(LanguageStrings.MAP_LAYER_RIVERS), showRivers),
                new LayerItemEntry(Layer.BIOME_ICONS, LanguageStrings.get(LanguageStrings.MAP_LAYER_BIOME_ICONS), showBiomeIcons)
        );

        List<LayerItemEntry> labelItems = List.of(
                new LayerItemEntry(Layer.LABEL_CONTINENTS, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_CONTINENTS), showLabelContinents),
                new LayerItemEntry(Layer.LABEL_ICE_CAPS, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_ICE_CAPS), showLabelIceCaps),
                new LayerItemEntry(Layer.LABEL_ARCHIPELAGOS, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_ARCHIPELAGOS), showLabelArchipelagos),
                new LayerItemEntry(Layer.LABEL_OCEANS, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_OCEANS), showLabelOceans),
                new LayerItemEntry(Layer.LABEL_TRENCHES, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_TRENCHES), showLabelTrenches),
                new LayerItemEntry(Layer.LABEL_MOUNTAINS, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_MOUNTAINS), showLabelMountains),
                new LayerItemEntry(Layer.LABEL_VALLEYS, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_VALLEYS), showLabelValleys),
                new LayerItemEntry(Layer.LABEL_LAKES, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_LAKES), showLabelLakes),
                new LayerItemEntry(Layer.LABEL_RIVERS, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_RIVERS), showLabelRivers),
                new LayerItemEntry(Layer.LABEL_PENINSULAS, LanguageStrings.get(LanguageStrings.MAP_LAYER_LABEL_PENINSULAS), showLabelPeninsulas)
        );

        JCheckBox allLayersCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.MAP_LAYER_ALL).toUpperCase(java.util.Locale.ROOT));
        AssetStyles.styleCheckBox(allLayersCheck);
        allLayersCheck.setFont(AssetStyles.FONT_BOLD.deriveFont(12f));
        allLayersCheck.setForeground(AssetStyles.FONT_COLOR_HEADER);

        List<LayerGroupPanel> groupPanels = new ArrayList<>();
        Runnable updateAllLayersCheckState = () -> {
            boolean allSelected = true;
            for (LayerGroupPanel lgp : groupPanels) {
                if (!lgp.areAllSelected()) {
                    allSelected = false;
                    break;
                }
            }
            allLayersCheck.setSelected(allSelected);
        };

        LayerGroupPanel dynasticPanel = createLayerGroupPanel(
                LanguageStrings.get(LanguageStrings.MAP_LAYER_GROUP_DYNASTIC),
                updateAllLayersCheckState,
                dynasticItems);
        LayerGroupPanel geoPanel = createLayerGroupPanel(
                LanguageStrings.get(LanguageStrings.MAP_LAYER_GROUP_GEOGRAPHICAL),
                updateAllLayersCheckState,
                geographicalItems);
        LayerGroupPanel labelPanel = createLayerGroupPanel(
                LanguageStrings.get(LanguageStrings.MAP_LAYER_GROUP_LABELS),
                updateAllLayersCheckState,
                labelItems);

        groupPanels.add(dynasticPanel);
        groupPanels.add(geoPanel);
        groupPanels.add(labelPanel);

        allLayersCheck.addActionListener(e -> {
            boolean target = allLayersCheck.isSelected();
            List<Layer> allLayers = new ArrayList<>();
            for (LayerGroupPanel lgp : groupPanels) {
                lgp.groupCheckBox.setSelected(target);
                for (LayerItemEntry item : lgp.items) {
                    if (item.checkBox != null) {
                        item.checkBox.setSelected(target);
                    }
                    allLayers.add(item.layer);
                }
            }
            setLayersVisible(allLayers, target);
        });

        updateAllLayersCheckState.run();

        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.setBackground(AssetStyles.BACKGROUND_COLOR);
        allLayersCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        topBar.add(allLayersCheck);
        topBar.add(Box.createVerticalStrut(6));
        JSeparator topSep = AssetStyles.createInternalSeparator();
        topSep.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        topBar.add(topSep);

        panel.add(topBar, BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 3, 16, 0));
        columns.setBackground(AssetStyles.BACKGROUND_COLOR);
        columns.add(dynasticPanel.panel);
        columns.add(geoPanel.panel);
        columns.add(labelPanel.panel);
        panel.add(columns, BorderLayout.CENTER);

        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createEmptyBorder());
        menu.setBackground(AssetStyles.BACKGROUND_COLOR);
        menu.add(panel);
        Dimension menuSize = menu.getPreferredSize();
        if (layersButton != null && layersButton.isShowing()) {
            menu.show(layersButton, -menuSize.width - 4, 0);
        }
    }

    private LayerGroupPanel createLayerGroupPanel(String title, Runnable onAnyChange, List<LayerItemEntry> items) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBackground(AssetStyles.BACKGROUND_COLOR);

        JCheckBox groupCheck = new JCheckBox(title.toUpperCase(java.util.Locale.ROOT));
        AssetStyles.styleCheckBox(groupCheck);
        groupCheck.setFont(AssetStyles.FONT_BOLD.deriveFont(11f));
        groupCheck.setForeground(AssetStyles.FONT_COLOR_HEADER);
        groupCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(groupCheck);
        group.add(Box.createVerticalStrut(4));

        JSeparator sep = AssetStyles.createInternalSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        group.add(sep);
        group.add(Box.createVerticalStrut(6));

        LayerGroupPanel lgp = new LayerGroupPanel(group, groupCheck, items);

        groupCheck.addActionListener(e -> {
            boolean target = groupCheck.isSelected();
            List<Layer> layers = new ArrayList<>();
            for (LayerItemEntry item : items) {
                if (item.checkBox != null) {
                    item.checkBox.setSelected(target);
                }
                layers.add(item.layer);
            }
            setLayersVisible(layers, target);
            if (onAnyChange != null) {
                onAnyChange.run();
            }
        });

        for (LayerItemEntry item : items) {
            JCheckBox box = new JCheckBox(item.label, item.initialSelected);
            item.checkBox = box;
            AssetStyles.styleCheckBox(box);
            box.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.addActionListener(e -> {
                setLayerVisible(item.layer, box.isSelected());
                lgp.updateGroupCheckState();
                if (onAnyChange != null) {
                    onAnyChange.run();
                }
            });
            group.add(box);
            group.add(Box.createVerticalStrut(4));
        }

        lgp.updateGroupCheckState();
        return lgp;
    }

    private void showModesMenu() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AssetStyles.BORDER_COLOR, AssetStyles.BORDER_THICKNESS_BUTTON),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JCheckBox biomeBox = new JCheckBox(LanguageStrings.get(LanguageStrings.MAP_MODE_BIOME), mapMode == MapMode.BIOME);
        AssetStyles.styleCheckBox(biomeBox);

        JCheckBox heightBox = new JCheckBox(LanguageStrings.get(LanguageStrings.MAP_MODE_HEIGHT), mapMode == MapMode.HEIGHT);
        AssetStyles.styleCheckBox(heightBox);

        JCheckBox tectonicBox = new JCheckBox(LanguageStrings.get(LanguageStrings.MAP_MODE_TECTONIC_PLATES), mapMode == MapMode.TECTONIC_PLATES);
        AssetStyles.styleCheckBox(tectonicBox);

        biomeBox.addActionListener(e -> {
            setMapMode(MapMode.BIOME);
            biomeBox.setSelected(true);
            heightBox.setSelected(false);
            tectonicBox.setSelected(false);
        });
        panel.add(biomeBox);

        panel.add(Box.createVerticalStrut(6));

        heightBox.addActionListener(e -> {
            setMapMode(MapMode.HEIGHT);
            heightBox.setSelected(true);
            biomeBox.setSelected(false);
            tectonicBox.setSelected(false);
        });
        panel.add(heightBox);

        panel.add(Box.createVerticalStrut(6));

        tectonicBox.addActionListener(e -> {
            setMapMode(MapMode.TECTONIC_PLATES);
            tectonicBox.setSelected(true);
            biomeBox.setSelected(false);
            heightBox.setSelected(false);
        });
        panel.add(tectonicBox);

        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createEmptyBorder());
        menu.setBackground(AssetStyles.BACKGROUND_COLOR);
        menu.add(panel);
        Dimension menuSize = menu.getPreferredSize();
        if (modesButton != null && modesButton.isShowing()) {
            menu.show(modesButton, -menuSize.width - 4, 0);
        }
    }

    public MapMode getMapMode() {
        return mapMode;
    }

    public void setMapMode(MapMode mapMode) {
        if (this.mapMode != mapMode) {
            this.mapMode = mapMode;
            if (mapPanel != null) {
                mapPanel.invalidateBakedImage();
                mapPanel.repaint();
            }
        }
    }

    String getHexToolTip(MouseEvent e) {
        return (mapPanel != null) ? mapPanel.getToolTipText(e) : null;
    }

    public boolean isShowGeographicNames() {
        return showGeographicNames;
    }

    public void setShowGeographicNames(boolean showGeographicNames) {
        setLayerVisible(Layer.GEOGRAPHIC_NAMES, showGeographicNames);
    }

    public boolean isShowCapitals() {
        return showCapitals;
    }

    public void setShowCapitals(boolean showCapitals) {
        setLayerVisible(Layer.CAPITALS, showCapitals);
    }

    private enum Layer {
        BORDERS, RIVERS, MERIDIAN, EQUATOR, BIOME_ICONS, COLONY_RANKS, TRADES, TUNNELS, BATTLES, GEOGRAPHIC_NAMES,
        CAPITALS, LABEL_CONTINENTS, LABEL_ICE_CAPS, LABEL_ARCHIPELAGOS, LABEL_OCEANS, LABEL_TRENCHES,
        LABEL_MOUNTAINS, LABEL_VALLEYS, LABEL_LAKES, LABEL_RIVERS, LABEL_PENINSULAS
    }

    private boolean isOverlayLayer(Layer layer) {
        return layer == Layer.TRADES || layer == Layer.TUNNELS || layer == Layer.BATTLES
                || layer == Layer.GEOGRAPHIC_NAMES || layer == Layer.LABEL_CONTINENTS
                || layer == Layer.LABEL_ICE_CAPS || layer == Layer.LABEL_ARCHIPELAGOS
                || layer == Layer.LABEL_OCEANS || layer == Layer.LABEL_TRENCHES
                || layer == Layer.LABEL_MOUNTAINS || layer == Layer.LABEL_VALLEYS
                || layer == Layer.LABEL_LAKES || layer == Layer.LABEL_RIVERS
                || layer == Layer.LABEL_PENINSULAS;
    }

    private void applyLayerState(Layer layer, boolean visible) {
        switch (layer) {
            case BORDERS -> showBorders = visible;
            case RIVERS -> showRivers = visible;
            case MERIDIAN -> showMeridian = visible;
            case EQUATOR -> showEquator = visible;
            case BIOME_ICONS -> showBiomeIcons = visible;
            case CAPITALS -> showCapitals = visible;
            case COLONY_RANKS -> showColonyRanks = visible;
            case TRADES -> showTrades = visible;
            case TUNNELS -> showTunnels = visible;
            case BATTLES -> showBattles = visible;
            case GEOGRAPHIC_NAMES -> {
                showGeographicNames = visible;
                showLabelContinents = visible;
                showLabelIceCaps = visible;
                showLabelArchipelagos = visible;
                showLabelOceans = visible;
                showLabelTrenches = visible;
                showLabelMountains = visible;
                showLabelValleys = visible;
                showLabelLakes = visible;
                showLabelRivers = visible;
                showLabelPeninsulas = visible;
            }
            case LABEL_CONTINENTS -> showLabelContinents = visible;
            case LABEL_ICE_CAPS -> showLabelIceCaps = visible;
            case LABEL_ARCHIPELAGOS -> showLabelArchipelagos = visible;
            case LABEL_OCEANS -> showLabelOceans = visible;
            case LABEL_TRENCHES -> showLabelTrenches = visible;
            case LABEL_MOUNTAINS -> showLabelMountains = visible;
            case LABEL_VALLEYS -> showLabelValleys = visible;
            case LABEL_LAKES -> showLabelLakes = visible;
            case LABEL_RIVERS -> showLabelRivers = visible;
            case LABEL_PENINSULAS -> showLabelPeninsulas = visible;
        }
    }

    private void setLayerVisible(Layer layer, boolean visible) {
        applyLayerState(layer, visible);
        boolean isOverlay = isOverlayLayer(layer);
        if (!isOverlay) {
            if (mapPanel != null) {
                mapPanel.invalidateBakedImage();
            }
        }
        persistLayerSettings();
        mapPanel.repaint();
    }

    private void setLayersVisible(Collection<Layer> layers, boolean visible) {
        boolean hasNonOverlay = false;
        for (Layer layer : layers) {
            applyLayerState(layer, visible);
            if (!isOverlayLayer(layer)) {
                hasNonOverlay = true;
            }
        }
        if (hasNonOverlay && mapPanel != null) {
            mapPanel.invalidateBakedImage();
        }
        persistLayerSettings();
        if (mapPanel != null) {
            mapPanel.repaint();
        }
    }


    private void travelToHomeHex() {
        syncWorldFromEngine();
        if (world == null) return;
        Hex homeHex = world.getSpawnHex();
        if (homeHex != null) {
            mapPanel.centerOn(homeHex);
            changeHex(homeHex, !keepMapOpenForSandbox());
        }
    }

    private boolean keepMapOpenForSandbox() {
        Engine eng = resolveEngine();
        return eng != null && eng.isEasyConquering() && hasPlayerDynasty();
    }

    private Hex selectedHex() {
        syncWorldFromEngine();
        return world != null ? world.getActiveHex() : null;
    }

    private void updateConquerButton() {
        if (conquerButton == null) {
            return;
        }
        Engine eng = resolveEngine();
        boolean sandbox = eng != null && eng.isEasyConquering() && hasPlayerDynasty();
        boolean canConquer = sandbox && SandboxCheatService.canCheatConquerHex(world, selectedHex());
        conquerButton.setVisible(sandbox);
        conquerButton.setEnabled(sandbox);
        if (sandbox) {
            conquerButton.setToolTipText(canConquer
                    ? LanguageStrings.get(LanguageStrings.MAP_CONQUER_COLONY_TT)
                    : LanguageStrings.get(LanguageStrings.MAP_CONQUER_COLONY_SELECT_TT));
        }
        if (bottomPanel != null) {
            bottomPanel.revalidate();
            bottomPanel.repaint();
        }
    }

    private void updateZoomControls() {
        if (zoomInButton == null || zoomOutButton == null || zoomSlider == null || mapPanel == null) {
            return;
        }
        zoomOutButton.setEnabled(mapPanel.canZoomOut());
        zoomInButton.setEnabled(mapPanel.canZoomIn());
        int sliderValue = mapPanel.getZoomStepIndex();
        if (zoomSlider.getValue() != sliderValue) {
            syncingZoomUi = true;
            zoomSlider.setValue(sliderValue);
            syncingZoomUi = false;
        }
        zoomSlider.setEnabled(mapPanel.canZoomIn() || mapPanel.canZoomOut());
    }

    private static int zoomToSliderValue(double zoom) {
        int bestIdx = 0;
        double bestDiff = Double.MAX_VALUE;
        for (int i = 0; i < GameNumbers.MAP_ZOOM_STEPS.length; i++) {
            double diff = Math.abs(GameNumbers.MAP_ZOOM_STEPS[i] - zoom);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private static double sliderValueToZoom(int sliderValue) {
        int idx = Math.max(0, Math.min(GameNumbers.MAP_ZOOM_STEPS.length - 1, sliderValue));
        return GameNumbers.MAP_ZOOM_STEPS[idx];
    }

    private void conquerActiveHex() {
        Engine eng = resolveEngine();
        syncWorldFromEngine();
        if (eng == null || !eng.isEasyConquering() || world == null || !hasPlayerDynasty()) {
            return;
        }
        Hex hex = selectedHex();
        if (!SandboxCheatService.canCheatConquerHex(world, hex)) {
            UiOptionPane.showMessageDialog(
                    this,
                    LanguageStrings.get(LanguageStrings.MAP_CONQUER_COLONY_SELECT_TT));
            return;
        }
        if (SandboxCheatService.cheatConquerHex(eng, world, hex)) {
            if (onHexChange != null) {
                onHexChange.run();
            }
            if (mapPanel != null) {
                mapPanel.invalidateBakedImage();
                mapPanel.repaint();
            }
            if (legendPanel != null) {
                legendPanel.updateLegend();
            }
            updateConquerButton();
        }
    }

    private void changeHex(Hex newHex, boolean closeDialog) {
        syncWorldFromEngine();
        if (newHex == null || world == null) {
            return;
        }
        world.changeActiveHex(newHex);

        if (onHexChange != null) {
            onHexChange.run();
        }
        if (mapPanel != null) {
            mapPanel.invalidateBakedImage();
            mapPanel.repaint();
        }
        if (closeDialog) {
            dispose();
        } else {
            updateConquerButton();
        }
    }

    @Override
    public void dispose() {
        stopNavigationHolding();
        Engine eng = resolveEngine();
        if (eng != null) {
            eng.setMapViewport(0, 0, false);
        }
        super.dispose();
    }

    private Dynasty findPlayerDynasty() {
        return SandboxCheatService.findPlayerDynasty(currentWorld());
    }

    @Override
    public void showDialog() {
        if (getOwner() != null && getOwner().isVisible()) {
            setBounds(getOwner().getBounds());
            setPreferredSize(getOwner().getSize());
        }
        refreshDialog();
        normalizeSouthPanelButtons();
        disableFocusTraversal(this);
        if (getOwner() != null && getOwner().isVisible()) {
            setBounds(getOwner().getBounds());
        } else {
            Dimension size = getPreferredSize();
            if (size == null || size.width < AssetStyles.DEFAULT_DIALOG_SIZE.width) {
                size = AssetStyles.DEFAULT_DIALOG_SIZE;
            }
            setSize(size);
            setLocationRelativeTo(null);
        }
        if (getOwner() instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }
        setVisible(true);
    }

    @Override
    protected void refreshDialog() {
        syncWorldFromEngine();
        if (mapPanel != null) {
            mapPanel.repaint();
        }
        if (legendPanel != null) {
            legendPanel.updateLegend();
        }
        updateConquerButton();
        if (zoomOutButton != null) {
            zoomOutButton.setToolTipText(LanguageStrings.get(LanguageStrings.MAP_ZOOM_OUT_TT));
        }
        if (zoomInButton != null) {
            zoomInButton.setToolTipText(LanguageStrings.get(LanguageStrings.MAP_ZOOM_IN_TT));
        }
        updateZoomControls();
        updateSpeedAndTimeControls();
    }

    public void liveUpdate() {
        refreshDialog();
    }

    public void updateTimeDisplay() {
        syncWorldFromEngine();
        updateSpeedAndTimeControls();
    }

    @Override
    public void refreshTheme() {
        super.refreshTheme();
        dateTimeLabel.setForeground(AssetStyles.FONT_COLOR);
        configureIconControlButton(speedDownButton, GameConstants.ICON_SPEED_DOWN);
        configureIconControlButton(speedUpButton, GameConstants.ICON_SPEED_UP);
        Engine eng = resolveEngine();
        configureIconControlButton(playPauseButton,
                eng != null && eng.isPaused() ? GameConstants.ICON_SPEED_PLAY : GameConstants.ICON_SPEED_PAUSE);
        AssetStyles.styleButton(homeButton);
        AssetStyles.styleCompactButton(zoomOutButton);
        AssetStyles.styleCompactButton(zoomInButton);
        applyZoomStepButtonColors(zoomOutButton);
        applyZoomStepButtonColors(zoomInButton);
        AssetStyles.styleSlider(zoomSlider);
        Dimension zoomTrack = new Dimension(
                AssetStyles.MIN_CONTROL_HIT_SIZE,
                GameNumbers.MAP_ZOOM_SLIDER_TRACK_HEIGHT);
        zoomSlider.setPreferredSize(zoomTrack);
        zoomSlider.setMinimumSize(zoomTrack);
        zoomSlider.setMaximumSize(zoomTrack);
        AssetStyles.styleCompactButton(layersButton);
        Dimension layersBtnSize = AssetStyles.minControlHitSize();
        layersButton.setPreferredSize(layersBtnSize);
        layersButton.setMinimumSize(layersBtnSize);
        layersButton.setMaximumSize(layersBtnSize);
        layersButton.setMargin(new Insets(0, 0, 0, 0));
        AssetStyles.styleCompactButton(modesButton);
        modesButton.setPreferredSize(layersBtnSize);
        modesButton.setMinimumSize(layersBtnSize);
        modesButton.setMaximumSize(layersBtnSize);
        modesButton.setMargin(new Insets(0, 0, 0, 0));
        AssetStyles.styleButton(conquerButton);
        AssetStyles.styleButton(closeButton);
        bottomPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        if (mapPanel != null) {
            mapPanel.onThemeChanged();
        }
        if (legendPanel != null) {
            legendPanel.onThemeChanged();
        }
    }

    private final JPanel bottomPanel;

    private class LegendPanel extends JPanel {
        private static final int LEGEND_EXPANDED_WIDTH = 420;
        private static final int LEGEND_COLLAPSED_WIDTH = 32;
        private static final int METRICS_COLUMN_WIDTH = 52;
        private static final int SORT_POPULATION = 0;
        private static final int SORT_DIPLOMACY = 1;
        private static final int SORT_MILITARY = 2;

        private final JPanel legendBody;
        private final JPanel content;
        private final JScrollPane legendScroll;
        private final JButton sortButton;
        private final FlatChevronButton collapseControl;
        private int metricsColumnWidth = METRICS_COLUMN_WIDTH;
        private int sortMode = SORT_POPULATION;
        private boolean expanded = true;

        private final JPanel activeWarsSection;
        private final JPanel activeWarsContent;
        private final FlatChevronButton activeWarsToggle;
        private final JLabel activeWarsTitle;
        private boolean activeWarsExpanded = true;

        private static final int MAX_WORLD_LOG_ENTRIES = 60;
        private final JPanel worldLogsSection;
        private final JPanel worldLogsContent;
        private final JScrollPane worldLogsScroll;
        private final FlatChevronButton worldLogsToggle;
        private final JLabel worldLogsTitle;
        private boolean worldLogsExpanded = true;

        public LegendPanel() {
            setLayout(new BorderLayout());
            setBackground(AssetStyles.BACKGROUND_COLOR);
            setPreferredSize(new Dimension(LEGEND_EXPANDED_WIDTH, 0));
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AssetStyles.BORDER_COLOR));

            collapseControl = new FlatChevronButton();
            collapseControl.setPointsLeft(true);
            collapseControl.setOpaque(true);
            collapseControl.setToolTipText(LanguageStrings.get(LanguageStrings.MAP_LEGEND_HIDE));
            collapseControl.addActionListener(e -> setExpanded(!expanded));
            collapseControl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    collapseControl.setBackground(AssetStyles.BACKGROUND_SECONDARY);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    collapseControl.setBackground(AssetStyles.BACKGROUND_COLOR);
                }
            });

            JPanel collapseWrap = new JPanel(new BorderLayout());
            collapseWrap.setBackground(AssetStyles.BACKGROUND_COLOR);
            collapseWrap.setPreferredSize(new Dimension(LEGEND_COLLAPSED_WIDTH, 0));
            collapseWrap.add(collapseControl, BorderLayout.NORTH);
            add(collapseWrap, BorderLayout.EAST);

            legendBody = new JPanel(new BorderLayout());
            legendBody.setBackground(AssetStyles.BACKGROUND_COLOR);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(10, 5, 5, 5));

            JLabel title = new JLabel(LanguageStrings.get(LanguageStrings.MAP_LEGEND_TITLE), SwingConstants.CENTER);
            title.setFont(AssetStyles.FONT_BOLD);
            title.setForeground(AssetStyles.FONT_COLOR_HEADER);
            header.add(title, BorderLayout.NORTH);

            sortButton = new JButton(LanguageStrings.get(LanguageStrings.MAP_SORT_BY_POPULATION));
            sortButton.setFocusable(false);
            AssetStyles.styleCompactButton(sortButton);
            sortButton.addActionListener(e -> {
                sortMode = (sortMode + 1) % 3;
                updateSortButtonLabel();
                updateLegend();
            });
            JPanel sortWrap = new JPanel(new BorderLayout());
            sortWrap.setOpaque(false);
            sortWrap.setBorder(new EmptyBorder(0, 4, 0, 4));
            sortWrap.add(sortButton, BorderLayout.CENTER);
            header.add(sortWrap, BorderLayout.SOUTH);
            legendBody.add(header, BorderLayout.NORTH);

            activeWarsContent = new JPanel();
            activeWarsContent.setLayout(new BoxLayout(activeWarsContent, BoxLayout.Y_AXIS));
            activeWarsContent.setOpaque(false);
            activeWarsContent.setBorder(new EmptyBorder(0, 12, 4, 8));

            activeWarsToggle = new FlatChevronButton();
            activeWarsToggle.setChevronDirection(FlatChevronButton.ChevronDirection.UP);
            activeWarsToggle.setOpaque(false);
            activeWarsToggle.addActionListener(e -> setActiveWarsExpanded(!activeWarsExpanded));

            activeWarsTitle = new JLabel();
            activeWarsTitle.setFont(AssetStyles.FONT_BOLD.deriveFont(10f));
            activeWarsTitle.setForeground(AssetStyles.FONT_COLOR_HEADER);
            activeWarsTitle.setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
            activeWarsTitle.setIconTextGap(4);

            JPanel activeWarsHeader = new JPanel(new BorderLayout(4, 0));
            activeWarsHeader.setOpaque(false);
            activeWarsHeader.setBorder(new EmptyBorder(0, 4, 4, 4));
            activeWarsHeader.add(activeWarsToggle, BorderLayout.WEST);
            activeWarsHeader.add(activeWarsTitle, BorderLayout.CENTER);
            AssetStyles.markClickable(activeWarsHeader);
            activeWarsHeader.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setActiveWarsExpanded(!activeWarsExpanded);
                }
            });

            activeWarsSection = new JPanel();
            activeWarsSection.setLayout(new BoxLayout(activeWarsSection, BoxLayout.Y_AXIS));
            activeWarsSection.setOpaque(false);
            activeWarsSection.setVisible(false);
            activeWarsSection.add(activeWarsHeader);
            activeWarsSection.add(activeWarsContent);

            worldLogsContent = new JPanel();
            worldLogsContent.setLayout(new BoxLayout(worldLogsContent, BoxLayout.Y_AXIS));
            worldLogsContent.setOpaque(false);
            worldLogsContent.setBorder(new EmptyBorder(0, 8, 4, 8));

            worldLogsScroll = new JScrollPane(worldLogsContent);
            worldLogsScroll.setBorder(null);
            worldLogsScroll.setOpaque(false);
            worldLogsScroll.getViewport().setOpaque(false);
            worldLogsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            worldLogsScroll.getVerticalScrollBar().setUnitIncrement(16);
            UiScrollBarStyles.hide(worldLogsScroll);
            worldLogsScroll.setPreferredSize(new Dimension(0, 130));
            worldLogsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

            worldLogsToggle = new FlatChevronButton();
            worldLogsToggle.setChevronDirection(FlatChevronButton.ChevronDirection.UP);
            worldLogsToggle.setOpaque(false);
            worldLogsToggle.addActionListener(e -> setWorldLogsExpanded(!worldLogsExpanded));

            worldLogsTitle = new JLabel();
            worldLogsTitle.setFont(AssetStyles.FONT_BOLD.deriveFont(10f));
            worldLogsTitle.setForeground(AssetStyles.FONT_COLOR_HEADER);
            worldLogsTitle.setIcon(GameConstants.ICON_STAT_INTELLIGENCE);
            worldLogsTitle.setIconTextGap(4);

            JPanel worldLogsHeader = new JPanel(new BorderLayout(4, 0));
            worldLogsHeader.setOpaque(false);
            worldLogsHeader.setBorder(new EmptyBorder(0, 4, 4, 4));
            worldLogsHeader.add(worldLogsToggle, BorderLayout.WEST);
            worldLogsHeader.add(worldLogsTitle, BorderLayout.CENTER);
            AssetStyles.markClickable(worldLogsHeader);
            worldLogsHeader.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setWorldLogsExpanded(!worldLogsExpanded);
                }
            });

            worldLogsSection = new JPanel();
            worldLogsSection.setLayout(new BoxLayout(worldLogsSection, BoxLayout.Y_AXIS));
            worldLogsSection.setOpaque(false);
            worldLogsSection.setVisible(false);
            worldLogsSection.add(worldLogsHeader);
            worldLogsSection.add(worldLogsScroll);

            JPanel columnHeader = new JPanel(new BorderLayout(4, 0));
            columnHeader.setOpaque(false);
            columnHeader.setBorder(new EmptyBorder(0, 10, 4, 8));
            JLabel dynastyHeader = new JLabel(LanguageStrings.get(LanguageStrings.STAT_DYNASTY));
            dynastyHeader.setFont(AssetStyles.FONT_BOLD.deriveFont(10f));
            dynastyHeader.setForeground(AssetStyles.FONT_COLOR);

            JPanel metricsHeader = new JPanel(new GridLayout(1, 2, 6, 0));
            metricsHeader.setOpaque(false);
            JLabel popHeader = new JLabel(LanguageStrings.get(LanguageStrings.PANEL_POPULATION), SwingConstants.RIGHT);
            popHeader.setFont(AssetStyles.FONT_BOLD.deriveFont(10f));
            popHeader.setForeground(AssetStyles.FONT_COLOR);
            popHeader.setIcon(GameConstants.ICON_STAT_POPULATION);
            popHeader.setHorizontalTextPosition(SwingConstants.LEFT);
            popHeader.setIconTextGap(2);
            JLabel militaryHeader = new JLabel(LanguageStrings.get(LanguageStrings.MAP_LEGEND_MILITARY), SwingConstants.RIGHT);
            militaryHeader.setFont(AssetStyles.FONT_BOLD.deriveFont(10f));
            militaryHeader.setForeground(AssetStyles.FONT_COLOR);
            militaryHeader.setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
            militaryHeader.setHorizontalTextPosition(SwingConstants.LEFT);
            militaryHeader.setIconTextGap(2);
            metricsColumnWidth = Math.max(METRICS_COLUMN_WIDTH,
                    Math.max(popHeader.getPreferredSize().width, militaryHeader.getPreferredSize().width) + 4);
            popHeader.setPreferredSize(new Dimension(metricsColumnWidth, popHeader.getPreferredSize().height));
            militaryHeader.setPreferredSize(new Dimension(metricsColumnWidth, militaryHeader.getPreferredSize().height));
            metricsHeader.add(popHeader);
            metricsHeader.add(militaryHeader);
            metricsHeader.setPreferredSize(new Dimension(metricsColumnWidth * 2 + 6, metricsHeader.getPreferredSize().height));

            columnHeader.add(dynastyHeader, BorderLayout.CENTER);
            columnHeader.add(metricsHeader, BorderLayout.EAST);

            content = new LegendScrollContent();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(AssetStyles.BACKGROUND_COLOR);
            content.setBorder(new EmptyBorder(0, 5, 5, 8));

            JPanel listPanel = new JPanel(new BorderLayout());
            listPanel.setOpaque(false);
            listPanel.add(columnHeader, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(content);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            UiScrollBarStyles.hide(scroll);
            legendScroll = scroll;
            attachLegendWheelScroll(scroll, this, legendBody, listPanel, content, header, columnHeader, activeWarsSection);
            listPanel.add(scroll, BorderLayout.CENTER);

            JPanel centerStack = new JPanel();
            centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
            centerStack.setOpaque(false);
            centerStack.add(activeWarsSection);
            centerStack.add(worldLogsSection);
            centerStack.add(listPanel);
            legendBody.add(centerStack, BorderLayout.CENTER);

            add(legendBody, BorderLayout.CENTER);
            updateLegend();
        }

        private void setActiveWarsExpanded(boolean expanded) {
            activeWarsExpanded = expanded;
            activeWarsContent.setVisible(expanded);
            activeWarsToggle.setChevronDirection(expanded
                    ? FlatChevronButton.ChevronDirection.UP
                    : FlatChevronButton.ChevronDirection.DOWN);
            activeWarsToggle.setToolTipText(LanguageStrings.get(
                    expanded ? LanguageStrings.MAP_ACTIVE_WARS_HIDE : LanguageStrings.MAP_ACTIVE_WARS_SHOW));
            activeWarsSection.revalidate();
            activeWarsSection.repaint();
        }

        private void updateActiveWarsSection() {
            List<War> wars = world != null ? world.getWarService().getActiveWars() : List.of();
            activeWarsSection.setVisible(!wars.isEmpty());
            activeWarsTitle.setText(LanguageStrings.format(
                    LanguageStrings.MAP_ACTIVE_WARS_TITLE_FMT, wars.size()));
            activeWarsContent.removeAll();
            boolean playerCanManageWars = canPlayerManageWars();
            for (War war : wars) {
                String label = world.getWarService().formatWarNameForDisplay(war, null);
                if (label == null || label.isEmpty()) {
                    Dynasty dynastyA = world.findDynastyById(war.getDynastyIdA());
                    Dynasty dynastyB = world.findDynastyById(war.getDynastyIdB());
                    String nameA = dynastyA != null ? dynastyA.getName() : "?";
                    String nameB = dynastyB != null ? dynastyB.getName() : "?";
                    label = LanguageStrings.format(LanguageStrings.MAP_ACTIVE_WAR_PAIR_FMT, nameA, nameB);
                }
                JLabel warLabel = new JLabel(label);
                warLabel.setFont(AssetStyles.FONT_SMALL);
                warLabel.setForeground(AssetStyles.FONT_COLOR_WARNING);
                warLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                warLabel.setBorder(new EmptyBorder(0, 8, 2, 0));
                if (playerCanManageWars && onOpenWarDialog != null) {
                    AssetStyles.markClickable(warLabel);
                    warLabel.setToolTipText(LanguageStrings.get(LanguageStrings.MAP_ACTIVE_WAR_OPEN));
                    warLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            onOpenWarDialog.run();
                        }
                    });
                }
                activeWarsContent.add(warLabel);
            }
            activeWarsContent.setVisible(activeWarsExpanded && !wars.isEmpty());
            activeWarsToggle.setChevronDirection(activeWarsExpanded
                    ? FlatChevronButton.ChevronDirection.UP
                    : FlatChevronButton.ChevronDirection.DOWN);
            activeWarsToggle.setToolTipText(LanguageStrings.get(
                    activeWarsExpanded ? LanguageStrings.MAP_ACTIVE_WARS_HIDE : LanguageStrings.MAP_ACTIVE_WARS_SHOW));
            activeWarsSection.revalidate();
            activeWarsSection.repaint();
        }

        private void setWorldLogsExpanded(boolean expanded) {
            worldLogsExpanded = expanded;
            worldLogsScroll.setVisible(expanded);
            worldLogsToggle.setChevronDirection(expanded
                    ? FlatChevronButton.ChevronDirection.UP
                    : FlatChevronButton.ChevronDirection.DOWN);
            worldLogsToggle.setToolTipText(LanguageStrings.get(
                    expanded ? LanguageStrings.MAP_WORLD_LOGS_HIDE : LanguageStrings.MAP_WORLD_LOGS_SHOW));
            worldLogsSection.revalidate();
            worldLogsSection.repaint();
        }

        private void updateWorldLogsSection() {
            boolean showLogs = isSpectatorMode() && world != null && world.getHistoryService() != null;
            if (!showLogs) {
                worldLogsSection.setVisible(false);
                worldLogsContent.removeAll();
                return;
            }
            List<WorldHistoryEvent> events = world.getHistoryService().getEvents();
            worldLogsSection.setVisible(true);
            worldLogsTitle.setText(LanguageStrings.format(
                    LanguageStrings.MAP_WORLD_LOGS_TITLE_FMT, events.size()));
            worldLogsContent.removeAll();
            int limit = Math.min(events.size(), MAX_WORLD_LOG_ENTRIES);
            for (int i = 0; i < limit; i++) {
                WorldHistoryEvent event = events.get(i);
                String msg = event.formatMessage(world);
                if (msg == null || msg.isEmpty()) {
                    continue;
                }
                String line = "[" + event.formatDateTime() + "] " + msg;
                JLabel logLabel = new JLabel(line);
                logLabel.setFont(AssetStyles.FONT_SMALL);
                logLabel.setForeground(AssetStyles.FONT_COLOR);
                logLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                logLabel.setBorder(new EmptyBorder(0, 4, 2, 0));
                logLabel.setToolTipText(line);
                worldLogsContent.add(logLabel);
            }
            worldLogsScroll.setVisible(worldLogsExpanded && !events.isEmpty());
            worldLogsToggle.setChevronDirection(worldLogsExpanded
                    ? FlatChevronButton.ChevronDirection.UP
                    : FlatChevronButton.ChevronDirection.DOWN);
            worldLogsToggle.setToolTipText(LanguageStrings.get(
                    worldLogsExpanded ? LanguageStrings.MAP_WORLD_LOGS_HIDE : LanguageStrings.MAP_WORLD_LOGS_SHOW));
            worldLogsSection.revalidate();
            worldLogsSection.repaint();
        }

        private boolean canPlayerManageWars() {
            if (world == null || world.getDynastys() == null) {
                return false;
            }
            for (Dynasty dynasty : world.getDynastys()) {
                if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                    return dynasty.isAtWar() || world.getWarService().hasWarHistoryForDynasty(dynasty.getId());
                }
            }
            return false;
        }

        private boolean isPlayerDynastyAtWar() {
            if (world == null || world.getDynastys() == null) {
                return false;
            }
            for (Dynasty dynasty : world.getDynastys()) {
                if (dynasty.isPlayer() && !dynasty.isDefeated() && dynasty.isAtWar()) {
                    return true;
                }
            }
            return false;
        }

        private void attachLegendWheelScroll(JScrollPane scroll, JComponent... targets) {
            MouseWheelListener wheelListener = this::handleLegendWheel;
            scroll.setWheelScrollingEnabled(true);
            scroll.addMouseWheelListener(wheelListener);
            scroll.getViewport().addMouseWheelListener(wheelListener);
            for (JComponent target : targets) {
                target.addMouseWheelListener(wheelListener);
            }
        }

        private void handleLegendWheel(MouseWheelEvent e) {
            if (!expanded || legendScroll == null) {
                return;
            }
            JScrollBar bar = legendScroll.getVerticalScrollBar();
            int next = bar.getValue() + e.getUnitsToScroll() * bar.getUnitIncrement();
            int max = Math.max(bar.getMinimum(), bar.getMaximum() - bar.getVisibleAmount() + 1);
            bar.setValue(Math.max(bar.getMinimum(), Math.min(max, next)));
            e.consume();
        }

        private void setExpanded(boolean expanded) {
            this.expanded = expanded;
            legendBody.setVisible(expanded);
            collapseControl.setPointsLeft(expanded);
            collapseControl.setToolTipText(LanguageStrings.get(
                    expanded ? LanguageStrings.MAP_LEGEND_HIDE : LanguageStrings.MAP_LEGEND_SHOW));
            int width = expanded ? LEGEND_EXPANDED_WIDTH : LEGEND_COLLAPSED_WIDTH;
            setPreferredSize(new Dimension(width, 0));
            setMinimumSize(new Dimension(width, 0));
            setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
            revalidate();
            Container parent = getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
            if (mapPanel != null) {
                mapPanel.revalidate();
                mapPanel.repaint();
            }
        }

        private void updateSortButtonLabel() {
            String labelKey = switch (sortMode) {
                case SORT_DIPLOMACY -> LanguageStrings.MAP_SORT_BY_DIPLOMACY;
                case SORT_MILITARY -> LanguageStrings.MAP_SORT_BY_MILITARY;
                default -> LanguageStrings.MAP_SORT_BY_POPULATION;
            };
            String label = LanguageStrings.get(labelKey);
            sortButton.setText(label);
            sortButton.setToolTipText(label);
            AssetStyles.styleCompactButton(sortButton);
            var fm = sortButton.getFontMetrics(sortButton.getFont());
            var insets = AssetStyles.BUTTON_COMPACT_MARGIN_INSETS;
            int width = insets.left + insets.right + fm.stringWidth(label) + 4;
            int height = insets.top + insets.bottom + fm.getHeight();
            sortButton.setPreferredSize(new Dimension(width, height));
            sortButton.revalidate();
        }

        private void onThemeChanged() {
            setBackground(AssetStyles.BACKGROUND_COLOR);
            legendBody.setBackground(AssetStyles.BACKGROUND_COLOR);
            content.setBackground(AssetStyles.BACKGROUND_COLOR);
            collapseControl.setBackground(AssetStyles.BACKGROUND_COLOR);
            collapseControl.setForeground(AssetStyles.FONT_COLOR);
            collapseControl.repaint();
            AssetStyles.styleCompactButton(sortButton);
            updateSortButtonLabel();
            updateLegend();
            revalidate();
            repaint();
        }

        public void updateLegend() {
            syncWorldFromEngine();
            updateActiveWarsSection();
            updateWorldLogsSection();
            content.removeAll();
            if (world == null || world.getHexes() == null) return;

            Map<Integer, Dynasty> activeDynastiesMap = new HashMap<>();
            for (Hex h : world.getHexes()) {
                if (h.getColony() != null && h.getColony().getDynasty() != null) {
                    Dynasty d = h.getColony().getDynasty();
                    if (d.isPlayer() || d.isDefeated() || d.isActiveForDiplomacy()) {
                        activeDynastiesMap.put(d.getId(), d);
                    }
                }
            }
            if (world.getDynastys() != null) {
                for (Dynasty d : world.getDynastys()) {
                    if (d.isDefeated() || world.getWarService().hasWarHistoryForDynasty(d.getId())) {
                        activeDynastiesMap.putIfAbsent(d.getId(), d);
                    }
                }
            }

            Dynasty playerDynasty = findPlayerDynasty();
            List<Dynasty> sortedDynasties = new ArrayList<>(activeDynastiesMap.values());
            if (sortMode == SORT_DIPLOMACY && playerDynasty != null && playerDynasty.getDiplomacyService() != null) {
                sortedDynasties.sort(Comparator
                        .comparingInt((Dynasty d) -> d.isPlayer() ? Integer.MIN_VALUE
                                : playerDynasty.getDiplomacyService().getEffectiveDiplomaticReputation(d, world))
                        .thenComparing(Dynasty::getName, String.CASE_INSENSITIVE_ORDER));
            } else if (sortMode == SORT_MILITARY) {
                sortedDynasties.sort(Comparator
                        .comparingInt((Dynasty d) -> d.isPlayer() ? Integer.MAX_VALUE : d.getMilitaryPower())
                        .reversed()
                        .thenComparing(Dynasty::getName, String.CASE_INSENSITIVE_ORDER));
            } else {
                sortedDynasties.sort((d1, d2) -> {
                    int p1 = d1.getStatService().getTotalPopulation(d1);
                    int p2 = d2.getStatService().getTotalPopulation(d2);
                    return Integer.compare(p2, p1);
                });
            }

            for (Dynasty d : sortedDynasties) {
                JPanel item = new JPanel(new BorderLayout(4, 0));
                item.setOpaque(false);
                item.setAlignmentX(Component.LEFT_ALIGNMENT);
                item.setMaximumSize(new Dimension(Integer.MAX_VALUE, DynastyColorSwatch.HITBOX_SIZE + 8));

                JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
                badges.setOpaque(false);

                DynastyColorSwatch colorSwatch = new DynastyColorSwatch(d.getColor());
                badges.add(colorSwatch);

                if (d.getSpecies() != null && d.getSpecies().getIcon() != null) {
                    badges.add(new JLabel(d.getSpecies().getIcon()));
                }

                boolean defeated = d.isDefeated();
                if (defeated && GameConstants.CLASS_DEAD.getIcon() != null) {
                    badges.add(new JLabel(GameConstants.CLASS_DEAD.getIcon()));
                }

                JLabel stanceIcon = null;
                if (!defeated && !d.isPlayer() && playerDynasty != null && playerDynasty.getDiplomacyService() != null) {
                    int rep = playerDynasty.getDiplomacyService().getEffectiveDiplomaticReputation(d, world);
                    DiplomaticReputation stance = GameConstants.getDiplomaticReputationLevel(rep);
                    if (stance.getIcon() != null) {
                        stanceIcon = new JLabel(stance.getIcon());
                        stanceIcon.setToolTipText(playerDynasty.getDiplomacyService().buildStanceIconTooltip(d, world));
                        badges.add(stanceIcon);
                    }
                }

                String nameStr = d.getName();
                String clickTooltip = LanguageStrings.format(LanguageStrings.MAP_CLICK_VIEW_CAPITAL, d.getName());
                if (defeated) {
                    nameStr += " (" + GameConstants.CLASS_DEAD.getName() + ")";
                    clickTooltip = clickTooltip + " — " + GameConstants.CLASS_DEAD.getName();
                } else if (d.isPlayer()) {
                    nameStr += LanguageStrings.get(LanguageStrings.MAP_YOU_PLAYER);
                }

                Colony capital = d.getCapital() != null ? d.getCapital()
                        : (d.getColonies() != null && !d.getColonies().isEmpty() ? d.getColonies().get(0) : null);
                Hex capitalHex = capital != null ? world.getHexOfColony(capital) : null;
                if (capitalHex != null) {
                    Runnable goToCapital = () -> {
                        if (isSpectatorMode() && mapPanel != null) {
                            mapPanel.centerOn(capitalHex);
                        }
                        changeHex(capitalHex, !keepMapOpenForSandbox());
                    };
                    colorSwatch.setClickAction(goToCapital, clickTooltip);
                } else {
                    colorSwatch.clearClickAction();
                }

                JLabel name = new JLabel(nameStr);
                name.setFont(d.isPlayer() ? AssetStyles.FONT_BOLD.deriveFont(10f) : AssetStyles.FONT_SMALL);
                name.setForeground(defeated ? AssetStyles.FONT_COLOR_ERROR : AssetStyles.FONT_COLOR);
                name.setToolTipText(buildDynastyLegendInfoTooltip(d, playerDynasty));
                if (capitalHex != null) {
                    AssetStyles.markClickable(name);
                    name.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if (SwingUtilities.isLeftMouseButton(e)) {
                                if (isSpectatorMode() && mapPanel != null) {
                                    mapPanel.centerOn(capitalHex);
                                }
                                changeHex(capitalHex, !keepMapOpenForSandbox());
                            }
                        }
                    });
                }

                int pop = d.getStatService().getTotalPopulation(d);
                int military = d.getMilitaryPower();

                JPanel metricsPanel = new JPanel(new GridLayout(1, 2, 6, 0));
                metricsPanel.setOpaque(false);
                JLabel popLabel = new JLabel(AssetStyles.formatNumber(pop), SwingConstants.RIGHT);
                popLabel.setFont(AssetStyles.FONT_SMALL);
                popLabel.setForeground(AssetStyles.FONT_COLOR);
                popLabel.setPreferredSize(new Dimension(metricsColumnWidth, popLabel.getPreferredSize().height));
                JLabel militaryLabel = new JLabel(AssetStyles.formatNumber(military), SwingConstants.RIGHT);
                militaryLabel.setFont(AssetStyles.FONT_SMALL);
                militaryLabel.setForeground(AssetStyles.FONT_COLOR);
                militaryLabel.setPreferredSize(new Dimension(metricsColumnWidth, militaryLabel.getPreferredSize().height));
                metricsPanel.add(popLabel);
                metricsPanel.add(militaryLabel);
                metricsPanel.setPreferredSize(new Dimension(metricsColumnWidth * 2 + 6, metricsPanel.getPreferredSize().height));

                item.add(badges, BorderLayout.WEST);
                item.add(name, BorderLayout.CENTER);
                item.add(metricsPanel, BorderLayout.EAST);

                content.add(item);
                content.add(Box.createRigidArea(new Dimension(0, 2)));
            }

            content.revalidate();
            content.repaint();
        }

        private String buildDynastyLegendInfoTooltip(Dynasty dynasty, Dynasty playerDynasty) {
            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<b>").append(dynasty.getName()).append("</b>");
            if (dynasty.isDefeated()) {
                sb.append(" (").append(GameConstants.CLASS_DEAD.getName()).append(")");
            }
            if (dynasty.getSpecies() != null) {
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_SPECIES)).append(dynasty.getSpecies().getName());
            }
            if (dynasty.getRank() != null) {
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY_RANK)).append(dynasty.getRank().getName());
            }
            sb.append(LanguageStrings.format(LanguageStrings.MAP_POPULATION_FORMAT,
                    AssetStyles.formatNumber(dynasty.getStatService().getTotalPopulation(dynasty))));
            sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY_MILITARY_POWER))
                    .append(AssetStyles.formatNumber(dynasty.getMilitaryPower()));
            Colony capital = dynasty.getCapital();
            if (capital != null && capital.getName() != null) {
                sb.append(LanguageStrings.format(LanguageStrings.MAP_TOOLTIP_DYNASTY_CAPITAL, capital.getName()));
            }
            if (!dynasty.isDefeated() && !dynasty.isPlayer() && playerDynasty != null
                    && playerDynasty.getDiplomacyService() != null) {
                int rep = playerDynasty.getDiplomacyService().getEffectiveDiplomaticReputation(dynasty, world);
                DiplomaticReputation stance = GameConstants.getDiplomaticReputationLevel(rep);
                sb.append("<br><b>")
                        .append(LanguageStrings.get(LanguageStrings.DIPLOMATIC_REPUTATION))
                        .append(":</b> ")
                        .append(stance.getName())
                        .append(" (")
                        .append(rep)
                        .append(")");
            }
            sb.append("</html>");
            return sb.toString();
        }

        private final class LegendScrollContent extends JPanel implements Scrollable {
            LegendScrollContent() {
                setOpaque(true);
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }

            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 16;
            }

            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                return Math.max(visibleRect.height - 16, 16);
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }

            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }
    }

    private class HexMapPanel extends JPanel {
        private static final int CLICK_SLOP_PX = 6;

        private static final class BakedStepCacheEntry {
            final BufferedImage image;
            final double hexSize;
            final int minX;
            final int minY;

            BakedStepCacheEntry(BufferedImage image, double hexSize, int minX, int minY) {
                this.image = image;
                this.hexSize = hexSize;
                this.minX = minX;
                this.minY = minY;
            }
        }

        private double hexSize = 26;
        private int zoomStepIndex = GameNumbers.MAP_ZOOM_DEFAULT_STEP_INDEX;
        private double zoom = GameNumbers.MAP_ZOOM_STEPS[GameNumbers.MAP_ZOOM_DEFAULT_STEP_INDEX];
        private double panX;
        private double panY;
        private final Map<Integer, Color> biomeColorCache = new HashMap<>();
        private final Map<Integer, Color> biomeFadedColorCache = new HashMap<>();
        private final Map<Long, Hex> hexLookup = new HashMap<>();
        private final Map<Integer, Image> scaledBiomeIconCache = new HashMap<>();
        private final Map<Integer, Image> scaledRankIconCache = new HashMap<>();
        private Image scaledCapitalIconCache;
        private int cachedIconSize = -1;
        private final Map<Integer, BakedStepCacheEntry> bakedStepCache = new LinkedHashMap<Integer, BakedStepCacheEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, BakedStepCacheEntry> eldest) {
                return size() > 5;
            }
        };
        private World cachedBakedWorld;
        private Hex cachedBakedActiveHex;
        private MapMode cachedBakedMapMode;
        private boolean cachedBakedBorders;
        private boolean cachedBakedRivers;
        private boolean cachedBakedBiomeIcons;
        private boolean cachedBakedColonyRanks;
        private boolean cachedBakedCapitals;
        private boolean cachedBakedMeridian;
        private boolean cachedBakedEquator;
        private int cachedBakedColonyStamp;
        private HexGridMesh cachedMesh;
        private World cachedMeshWorld;
        private double cachedMeshHexSize = Double.NaN;
        private int cachedHexCount;
        private Point pressPoint;
        private Point lastDragPoint;
        private boolean dragging;

        public HexMapPanel() {
            setBackground(AssetStyles.BACKGROUND_COLOR);
            ToolTipManager.sharedInstance().registerComponent(this);
            AssetStyles.markClickable(this);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    pressPoint = e.getPoint();
                    lastDragPoint = pressPoint;
                    dragging = false;
                    ToolTipManager.sharedInstance().mousePressed(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e) || pressPoint == null) {
                        return;
                    }
                    boolean wasDragging = dragging;
                    Point click = pressPoint;
                    pressPoint = null;
                    lastDragPoint = null;
                    dragging = false;
                    if (wasDragging) {
                        return;
                    }
                    Point release = e.getPoint();
                    int dx = release.x - click.x;
                    int dy = release.y - click.y;
                    if (dx * dx + dy * dy > CLICK_SLOP_PX * CLICK_SLOP_PX) {
                        return;
                    }
                    handleMouseClick(click);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (dragging) {
                        return;
                    }
                    pressPoint = null;
                    lastDragPoint = null;
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e) || pressPoint == null) {
                        return;
                    }
                    Point now = e.getPoint();
                    if (!dragging) {
                        int dx = now.x - pressPoint.x;
                        int dy = now.y - pressPoint.y;
                        if (dx * dx + dy * dy <= CLICK_SLOP_PX * CLICK_SLOP_PX) {
                            return;
                        }
                        dragging = true;
                        lastDragPoint = pressPoint;
                    }
                    if (lastDragPoint != null) {
                        panX += now.x - lastDragPoint.x;
                        panY += now.y - lastDragPoint.y;
                        clampCamera();
                        lastDragPoint = now;
                        repaint();
                    }
                }
            });
            addMouseWheelListener(e -> {
                double rot = e.getPreciseWheelRotation();
                if (rot < 0) {
                    zoomStep(1, e.getPoint());
                } else if (rot > 0) {
                    zoomStep(-1, e.getPoint());
                }
                e.consume();
            });
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    refreshHexSize();
                    clampCamera();
                    updateZoomControls();
                    invalidateBakedImage();
                    repaint();
                }
            });
        }

        double getZoom() {
            return zoom;
        }

        int getZoomStepIndex() {
            return zoomStepIndex;
        }

        boolean canZoomIn() {
            return zoomStepIndex < GameNumbers.MAP_ZOOM_STEPS.length - 1;
        }

        boolean canZoomOut() {
            return zoomStepIndex > 0;
        }

        void zoomStep(int delta) {
            zoomStep(delta, new Point(Math.max(0, getWidth() / 2), Math.max(0, getHeight() / 2)));
        }

        void zoomStep(int delta, Point focus) {
            if (focus == null || world == null || delta == 0) {
                return;
            }
            int nextIndex = Math.max(0, Math.min(GameNumbers.MAP_ZOOM_STEPS.length - 1, zoomStepIndex + delta));
            setZoomStepIndex(nextIndex, focus);
        }

        void zoomBy(double notches) {
            if (notches > 0) {
                zoomStep(1);
            } else if (notches < 0) {
                zoomStep(-1);
            }
        }

        void zoomContinuous(double direction) {
            if (direction > 0) {
                zoomStep(1);
            } else if (direction < 0) {
                zoomStep(-1);
            }
        }

        void setZoomLevel(double targetZoom) {
            int bestIdx = 0;
            double bestDiff = Double.MAX_VALUE;
            for (int i = 0; i < GameNumbers.MAP_ZOOM_STEPS.length; i++) {
                double diff = Math.abs(GameNumbers.MAP_ZOOM_STEPS[i] - targetZoom);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    bestIdx = i;
                }
            }
            setZoomStepIndex(bestIdx, new Point(Math.max(0, getWidth() / 2), Math.max(0, getHeight() / 2)));
        }

        void setZoomStepIndex(int targetIndex) {
            setZoomStepIndex(targetIndex, new Point(Math.max(0, getWidth() / 2), Math.max(0, getHeight() / 2)));
        }

        void setZoomStepIndex(int targetIndex, Point focus) {
            if (focus == null || world == null) {
                return;
            }
            double fit = fittedHexSize();
            if (!(fit > 0)) {
                return;
            }
            targetIndex = Math.max(0, Math.min(GameNumbers.MAP_ZOOM_STEPS.length - 1, targetIndex));
            if (targetIndex == zoomStepIndex && Math.abs(zoom - GameNumbers.MAP_ZOOM_STEPS[targetIndex]) < 1e-9) {
                updateZoomControls();
                return;
            }
            double oldZoom = zoom;
            zoomStepIndex = targetIndex;
            zoom = GameNumbers.MAP_ZOOM_STEPS[zoomStepIndex];
            double oldSize = fit * oldZoom;
            double newSize = fit * zoom;
            double centerX = getWidth() / 2.0;
            double centerY = getHeight() / 2.0;
            panX = HexMapGeometry.panAfterZoom(panX, focus.x - centerX, oldSize, newSize);
            panY = HexMapGeometry.panAfterZoom(panY, focus.y - centerY, oldSize, newSize);
            hexSize = newSize;
            clampCamera();
            updateZoomControls();
            repaint();
        }

        void panBy(int horizontalDirection) {
            refreshHexSize();
            double step = Math.max(50.0, hexSize * 2.0);
            if (horizontalDirection < 0) {
                panX += step;
            } else {
                panX -= step;
            }
            clampCamera();
            repaint();
        }

        void panContinuous(int horizontalDirection) {
            refreshHexSize();
            double step = Math.max(10.0, hexSize * 0.4);
            if (horizontalDirection < 0) {
                panX += step;
            } else {
                panX -= step;
            }
            clampCamera();
            repaint();
        }

        void centerOn(Hex hex) {
            if (hex == null) {
                return;
            }
            refreshHexSize();
            panX = -HexMapGeometry.flatTopCenterX(hex.getQ(), hex.getR(), hexSize);
            panY = -HexMapGeometry.flatTopCenterY(hex.getQ(), hex.getR(), hexSize);
            clampCamera();
            updateZoomControls();
            repaint();
        }

        void onThemeChanged() {
            biomeColorCache.clear();
            biomeFadedColorCache.clear();
            invalidateMeshCache();
            setBackground(AssetStyles.BACKGROUND_COLOR);
            repaint();
        }

        private static long hexKey(int q, int r) {
            return (((long) q) << 32) | (r & 0xffffffffL);
        }

        private void invalidateMeshCache() {
            cachedMesh = null;
            cachedMeshWorld = null;
            cachedMeshHexSize = Double.NaN;
            cachedHexCount = -1;
            biomeFadedColorCache.clear();
            invalidateBakedImage();
        }

        void invalidateBakedImage() {
            bakedStepCache.clear();
            cachedBakedWorld = null;
            cachedBakedActiveHex = null;
            cachedBakedMapMode = null;
            scaledBiomeIconCache.clear();
            scaledRankIconCache.clear();
            scaledCapitalIconCache = null;
            cachedIconSize = -1;
        }

        private int computeColonyBakeStamp() {
            if (world == null || world.getHexes() == null) {
                return 0;
            }
            int hash = 1;
            for (Hex h : world.getHexes()) {
                if (h == null) {
                    continue;
                }
                Colony c = h.getColony();
                if (c != null) {
                    int dynId = c.getDynasty() != null ? c.getDynasty().getId() : 0;
                    int rankId = c.getRank() != null ? c.getRank().getId() : 0;
                    int cap = c.isCapital() ? 1 : 0;
                    hash = 31 * hash + h.getQ();
                    hash = 31 * hash + h.getR();
                    hash = 31 * hash + dynId;
                    hash = 31 * hash + rankId;
                    hash = 31 * hash + cap;
                }
            }
            return hash;
        }

        private boolean isBakedImageValid(Hex activeHex) {
            BakedStepCacheEntry entry = bakedStepCache.get(zoomStepIndex);
            return entry != null
                    && entry.image != null
                    && cachedBakedWorld == world
                    && Math.abs(entry.hexSize - hexSize) < 1e-6
                    && cachedBakedActiveHex == activeHex
                    && cachedBakedMapMode == mapMode
                    && cachedBakedBorders == showBorders
                    && cachedBakedRivers == showRivers
                    && cachedBakedBiomeIcons == showBiomeIcons
                    && cachedBakedColonyRanks == showColonyRanks
                    && cachedBakedCapitals == showCapitals
                    && cachedBakedMeridian == showMeridian
                    && cachedBakedEquator == showEquator
                    && cachedBakedColonyStamp == computeColonyBakeStamp();
        }

        private HexGridMesh ensureMeshCache(List<Hex> hexes) {
            int hexCount = hexes != null ? hexes.size() : 0;
            if (cachedMesh != null && cachedMeshWorld == world && cachedMeshHexSize == hexSize
                    && cachedHexCount == hexCount) {
                return cachedMesh;
            }
            hexLookup.clear();
            if (hexes != null) {
                for (Hex hex : hexes) {
                    if (hex != null) {
                        hexLookup.put(hexKey(hex.getQ(), hex.getR()), hex);
                    }
                }
            }
            cachedMesh = HexGridMesh.build(
                    hexes,
                    0,
                    0,
                    hexSize,
                    (q, r) -> hexLookup.get(hexKey(q, r)));
            cachedMeshWorld = world;
            cachedMeshHexSize = hexSize;
            cachedHexCount = hexCount;
            return cachedMesh;
        }

        private double getWorldWidthPx() {
            if (world == null) return 0;
            if (world.getWorldWidth() > 0) {
                return HexMapGeometry.rectangularWorldWidth(world.getWorldWidth(), hexSize);
            }
            return HexMapGeometry.worldWidth(world.getWorldRadius(), hexSize);
        }

        private double getWorldHeightPx() {
            if (world == null) return 0;
            if (world.getWorldHeight() > 0) {
                return HexMapGeometry.rectangularWorldHeight(world.getWorldHeight(), hexSize);
            }
            return HexMapGeometry.worldHeight(world.getWorldRadius(), hexSize);
        }

        private double fittedHexSize() {
            if (world == null) {
                return GameNumbers.MAP_HEX_FIT_MIN;
            }
            if (world.getWorldWidth() > 0 && world.getWorldHeight() > 0) {
                int panelH = getHeight();
                if (panelH > 0) {
                    int minR = -world.getWorldHeight() / 2;
                    int maxR = world.getWorldHeight() / 2 - 1;
                    double spanH = (maxR - minR) * 1.5 + 2.0;
                    double sizeH = (panelH - 36.0) / spanH;
                    return HexMapGeometry.clamp(sizeH, GameNumbers.MAP_HEX_FIT_MIN, GameNumbers.MAP_HEX_FIT_MAX);
                }
                return HexMapGeometry.fittedRectangularHexSize(
                        world.getWorldWidth(),
                        world.getWorldHeight(),
                        getWidth(),
                        getHeight(),
                        GameNumbers.MAP_HEX_FIT_MIN,
                        GameNumbers.MAP_HEX_FIT_MAX);
            }
            return HexMapGeometry.fittedHexSize(
                    world.getWorldRadius(),
                    getWidth(),
                    getHeight(),
                    GameNumbers.MAP_HEX_FIT_MIN,
                    GameNumbers.MAP_HEX_FIT_MAX);
        }

        private void refreshHexSize() {
            if (world == null) {
                return;
            }
            int panelW = getWidth();
            int panelH = getHeight();
            if (panelW <= 0 || panelH <= 0) {
                return;
            }
            hexSize = fittedHexSize() * zoom;
        }

        private void clampCamera() {
            if (world == null) {
                panX = 0;
                panY = 0;
                return;
            }
            refreshHexSize();
            double worldW = getWorldWidthPx();
            if (worldW > 0) {
                int panelW = getWidth();
                if (panelW > 0 && worldW <= panelW) {
                    panX = 0;
                } else {
                    while (panX > worldW / 2.0) {
                        panX -= worldW;
                    }
                    while (panX < -worldW / 2.0) {
                        panX += worldW;
                    }
                }
            }
            panY = HexMapGeometry.clampPan(panY, getWorldHeightPx(), getHeight());
            updateViewportInEngine();
        }

        private void updateViewportInEngine() {
            Engine eng = resolveEngine();
            if (eng != null && world != null && world.getWorldWidth() > 0) {
                double worldW_px = getWorldWidthPx();
                if (worldW_px > 0) {
                    double spanCols = (getWidth() / worldW_px) * world.getWorldWidth();
                    double centerCol = ((-panX / worldW_px) * world.getWorldWidth());
                    eng.setMapViewport(centerCol, spanCols, isShowing());
                }
            }
        }

        private void calculateHexSize() {
            refreshHexSize();
            clampCamera();
        }

        private Hex findHexAtLocal(double relX, double relY) {
            Point axial = HexMapGeometry.flatTopAxialRound(relX, relY, hexSize);
            HexGridMesh mesh = cachedMesh;
            if (mesh != null) {
                HexGridMesh.Face face = mesh.faceAt(axial.x, axial.y);
                if (face != null && face.toPolygon().contains(relX, relY)) {
                    return face.hex;
                }
                for (int[] offset : HexMapGeometry.FLAT_TOP_NEIGHBOR_OFFSETS) {
                    HexGridMesh.Face nFace = mesh.faceAt(axial.x + offset[0], axial.y + offset[1]);
                    if (nFace != null && nFace.toPolygon().contains(relX, relY)) {
                        return nFace.hex;
                    }
                }
            }
            return null;
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            syncWorldFromEngine();
            if (world == null || world.getHexes() == null) return null;

            calculateHexSize();
            Point p = e.getPoint();
            if (p == null) return null;
            Point centerOffset = getCenterOffset();

            double worldW = (world.getWorldWidth() > 0) ? getWorldWidthPx() : 0;
            double relX = p.x - centerOffset.x;
            if (worldW > 0) {
                int panelW = getWidth();
                if (panelW > 0 && worldW <= panelW) {
                    if (relX > worldW / 2.0 || relX < -worldW / 2.0) {
                        return null;
                    }
                } else {
                    while (relX > worldW / 2.0) relX -= worldW;
                    while (relX < -worldW / 2.0) relX += worldW;
                }
            }
            double relY = p.y - centerOffset.y;

            Hex hex = findHexAtLocal(relX, relY);
            if (hex == null) return null;

            StringBuilder sb = new StringBuilder("<html>");

            Colony c = hex.getColony();
            boolean hasColonyName = c != null && c.getName() != null;
            if (hasColonyName) {
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_COLONY_NAME))
                        .append(c.getName());
            }

            if (hex.getBiome() != null) {
                if (hasColonyName) {
                    sb.append("<br>");
                }
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_BIOME))
                        .append(hex.getBiome().getName());
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_HEIGHT))
                        .append(hex.getElevation());
            } else if (hasColonyName) {
                sb.append("<br>").append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_BIOME))
                        .append(LanguageStrings.get(LanguageStrings.STAT_UNKNOWN));
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_HEIGHT))
                        .append(hex.getElevation());
            } else {
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_BIOME))
                        .append(LanguageStrings.get(LanguageStrings.STAT_UNKNOWN));
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_HEIGHT))
                        .append(hex.getElevation());
            }
            if (mapMode == MapMode.TECTONIC_PLATES && hex.getPlateId() > 0) {
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_PLATE))
                        .append(hex.getPlateId());
            }

            Continent cont = world.getContinentForHex(hex);
            if (cont != null && cont.getName() != null) {
                String labelKey = cont.isIceCap() ? LanguageStrings.ICE_CAP_LABEL
                        : (cont.isArchipelago() ? LanguageStrings.ARCHIPELAGO_LABEL : LanguageStrings.CONTINENT_LABEL);
                sb.append("<br>")
                        .append(LanguageStrings.get(labelKey))
                        .append(": ")
                        .append(cont.getName());
            }

            GeographicFeature gf = world.getFeatureForHex(hex);
            if (gf != null && gf.getName() != null && isFeatureLabelVisible(gf.getType())) {
                sb.append("<br>")
                        .append(LanguageStrings.get(gf.getType().getLabelKey()))
                        .append(": ")
                        .append(gf.getName());
            }
            GeographicFeature rf = world.getRiverFeatureForHex(hex);
            if (rf != null && rf.getName() != null && rf != gf && isFeatureLabelVisible(rf.getType())) {
                sb.append("<br>")
                        .append(LanguageStrings.get(rf.getType().getLabelKey()))
                        .append(": ")
                        .append(rf.getName());
            }

            if (c != null) {
                if (c.getRank() != null) {
                    sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_RANK)).append(c.getRank().getName());
                }

                if (c.getSpecies() != null) {
                    sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_SPECIES)).append(c.getSpecies().getName());
                } else {
                    sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_SPECIES)).append(LanguageStrings.get(LanguageStrings.STAT_UNKNOWN));
                }

                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_MILITARY_POWER))
                        .append(AssetStyles.formatNumber(c.getMilitaryPower()));

                Dynasty dynasty = c.getDynasty();
                if (dynasty != null) {
                    sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY)).append(dynasty.getName());
                    if (dynasty.isDefeated()) {
                        sb.append(" (").append(GameConstants.CLASS_DEAD.getName()).append(")");
                    }
                    if (dynasty.getRank() != null) {
                        sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY_RANK)).append(dynasty.getRank().getName());
                    }
                    sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY_MILITARY_POWER))
                            .append(AssetStyles.formatNumber(dynasty.getMilitaryPower()));
                    Dynasty playerDynasty = findPlayerDynasty();
                    if (!dynasty.isDefeated() && playerDynasty != null && !dynasty.isPlayer()
                            && playerDynasty.getDiplomacyService() != null) {
                        int rep = playerDynasty.getDiplomacyService().getEffectiveDiplomaticReputation(dynasty, world);
                        DiplomaticReputation stance = GameConstants.getDiplomaticReputationLevel(rep);
                        sb.append("<br><b>")
                                .append(LanguageStrings.get(LanguageStrings.DYNASTY_REPUTATION))
                                .append(":</b> ")
                                .append(stance.getName())
                                .append(" (")
                                .append(rep)
                                .append(")");
                    }
                }
            } else {
                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_EMPTY));
            }

            sb.append("</html>");
            return sb.toString();
        }

        private void handleMouseClick(Point p) {
            syncWorldFromEngine();
            if (world == null || world.getHexes() == null || p == null) {
                return;
            }

            calculateHexSize();
            Point centerOffset = getCenterOffset();

            double worldW = (world.getWorldWidth() > 0) ? getWorldWidthPx() : 0;
            double relX = p.x - centerOffset.x;
            if (worldW > 0) {
                int panelW = getWidth();
                if (panelW > 0 && worldW <= panelW) {
                    if (relX > worldW / 2.0 || relX < -worldW / 2.0) {
                        return;
                    }
                } else {
                    while (relX > worldW / 2.0) relX -= worldW;
                    while (relX < -worldW / 2.0) relX += worldW;
                }
            }
            double relY = p.y - centerOffset.y;

            Hex hex = findHexAtLocal(relX, relY);
            if (hex != null) {
                changeHex(hex, !keepMapOpenForSandbox());
            }
        }

        private void bakeBaseImage(HexGridMesh mesh, Hex activeHex) {
            if (mesh == null || world == null) {
                return;
            }
            int pad = (int) Math.ceil(hexSize * 2.0);
            int minX = mesh.boundsMinX() - pad;
            int maxX = mesh.boundsMaxX() + pad;
            int minY = mesh.boundsMinY() - pad;
            int maxY = mesh.boundsMaxY() + pad;
            int imgW = maxX - minX + 1;
            int imgH = maxY - minY + 1;
            if (imgW <= 0 || imgH <= 0 || (long) imgW * imgH > 30_000_000L) {
                return;
            }
            BufferedImage image;
            try {
                image = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
            } catch (Throwable t) {
                return;
            }
            Graphics2D bg = image.createGraphics();
            bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            bg.translate(-minX, -minY);

            mesh.paintFills(bg, face -> resolveFillColor(face.hex));
            mesh.paintDividers(
                    bg,
                    showBorders,
                    face -> resolveFillColor(face.hex),
                    AssetStyles.BORDER_COLOR);
            if (mapMode == MapMode.TECTONIC_PLATES) {
                mesh.paintFaultBoundariesWithStyles(bg, this::resolveFaultStyle);
            }
            if (showRivers) {
                mesh.paintRivers(bg);
            }
            if (showBiomeIcons || showColonyRanks || showCapitals) {
                for (HexGridMesh.Face face : mesh.faces()) {
                    drawHexIcons(bg, face);
                }
            }
            if (world.getWorldWidth() > 0) {
                double worldW = getWorldWidthPx();
                double topY = -(world.getWorldHeight() * 1.5 * hexSize) / 2.0 - 0.75 * hexSize;
                double botY = +(world.getWorldHeight() * 1.5 * hexSize) / 2.0 - 0.75 * hexSize;
                float[] dash = {6f, 4f};
                BasicStroke lineStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f);
                if (showMeridian) {
                    bg.setColor(new Color(255, 255, 255, 130));
                    bg.setStroke(lineStroke);
                    bg.drawLine(0, (int) Math.round(topY), 0, (int) Math.round(botY));
                }
                if (showEquator) {
                    bg.setColor(new Color(255, 255, 255, 130));
                    bg.setStroke(lineStroke);
                    double eqY = -0.75 * hexSize;
                    double leftX = -worldW / 2.0;
                    double rightX = worldW / 2.0;
                    bg.drawLine((int) Math.round(leftX), (int) Math.round(eqY), (int) Math.round(rightX), (int) Math.round(eqY));
                }
            }
            bg.dispose();

            bakedStepCache.put(zoomStepIndex, new BakedStepCacheEntry(image, hexSize, minX, minY));
            cachedBakedWorld = world;
            cachedBakedActiveHex = activeHex;
            cachedBakedMapMode = mapMode;
            cachedBakedBorders = showBorders;
            cachedBakedRivers = showRivers;
            cachedBakedBiomeIcons = showBiomeIcons;
            cachedBakedColonyRanks = showColonyRanks;
            cachedBakedCapitals = showCapitals;
            cachedBakedMeridian = showMeridian;
            cachedBakedEquator = showEquator;
            cachedBakedColonyStamp = computeColonyBakeStamp();
        }

        private Image getScaledBiomeIcon(Biome biome, int size) {
            if (biome == null || biome.getIcon() == null || biome.getIcon().getImage() == null || size <= 0) {
                return null;
            }
            if (size != cachedIconSize) {
                scaledBiomeIconCache.clear();
                scaledRankIconCache.clear();
                scaledCapitalIconCache = null;
                cachedIconSize = size;
            }
            Image cached = scaledBiomeIconCache.get(biome.getId());
            if (cached != null) {
                return cached;
            }
            try {
                BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D ig = img.createGraphics();
                ig.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                ig.drawImage(biome.getIcon().getImage(), 0, 0, size, size, null);
                ig.dispose();
                scaledBiomeIconCache.put(biome.getId(), img);
                return img;
            } catch (Throwable t) {
                return biome.getIcon().getImage();
            }
        }

        private Image getScaledRankIcon(Rank rank, int size) {
            if (rank == null || rank.getIcon() == null || rank.getIcon().getImage() == null || size <= 0) {
                return null;
            }
            if (size != cachedIconSize) {
                scaledBiomeIconCache.clear();
                scaledRankIconCache.clear();
                scaledCapitalIconCache = null;
                cachedIconSize = size;
            }
            Image cached = scaledRankIconCache.get(rank.getId());
            if (cached != null) {
                return cached;
            }
            try {
                BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D ig = img.createGraphics();
                ig.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                ig.drawImage(rank.getIcon().getImage(), 0, 0, size, size, null);
                ig.dispose();
                scaledRankIconCache.put(rank.getId(), img);
                return img;
            } catch (Throwable t) {
                return rank.getIcon().getImage();
            }
        }

        private Image getScaledCapitalIcon(int size) {
            ImageIcon baseIcon = GameConstants.ROLE_COMMANDER != null ? GameConstants.ROLE_COMMANDER.getIcon() : null;
            if (baseIcon == null || baseIcon.getImage() == null || size <= 0) {
                return null;
            }
            if (size != cachedIconSize) {
                scaledBiomeIconCache.clear();
                scaledRankIconCache.clear();
                scaledCapitalIconCache = null;
                cachedIconSize = size;
            }
            if (scaledCapitalIconCache != null) {
                return scaledCapitalIconCache;
            }
            try {
                BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D ig = img.createGraphics();
                ig.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                ig.drawImage(baseIcon.getImage(), 0, 0, size, size, null);
                ig.dispose();
                scaledCapitalIconCache = img;
                return img;
            } catch (Throwable t) {
                return baseIcon.getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            syncWorldFromEngine();
            if (world == null || world.getHexes() == null) return;

            calculateHexSize();

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            Point centerOffset = getCenterOffset();
            Hex activeHex = world.getActiveHex();
            List<Hex> hexes = world.getHexes();

            HexGridMesh mesh = ensureMeshCache(hexes);
            if (!isBakedImageValid(activeHex)) {
                bakeBaseImage(mesh, activeHex);
            }

            double worldW = (world.getWorldWidth() > 0) ? getWorldWidthPx() : 0;
            List<Double> horizontalShifts = new ArrayList<>();
            if (worldW > 0) {
                int panelW = getWidth();
                if (panelW > 0 && worldW <= panelW) {
                    horizontalShifts.add(0.0);
                } else {
                    double leftBound = centerOffset.x - worldW / 2.0;
                    int minK = (int) Math.floor(-(leftBound + worldW) / worldW);
                    int maxK = (int) Math.ceil((panelW - leftBound) / worldW);
                    for (int k = minK; k <= maxK; k++) {
                        double s = k * worldW;
                        if (leftBound + s + worldW >= 0 && leftBound + s <= panelW) {
                            horizontalShifts.add(s);
                        }
                    }
                    if (horizontalShifts.isEmpty()) {
                        horizontalShifts.add(0.0);
                    }
                }
            } else {
                horizontalShifts.add(0.0);
            }
            BakedStepCacheEntry bakedEntry = bakedStepCache.get(zoomStepIndex);
            for (double shiftX : horizontalShifts) {
                Graphics2D sg = (Graphics2D) g2d.create();
                sg.translate(centerOffset.x + shiftX, centerOffset.y);
                if (bakedEntry != null && bakedEntry.image != null) {
                    sg.drawImage(bakedEntry.image, bakedEntry.minX, bakedEntry.minY, null);
                } else {
                    mesh.paintFills(sg, face -> resolveFillColor(face.hex));
                    mesh.paintDividers(
                            sg,
                            showBorders,
                            face -> resolveFillColor(face.hex),
                            AssetStyles.BORDER_COLOR);
                    if (mapMode == MapMode.TECTONIC_PLATES) {
                        mesh.paintFaultBoundariesWithStyles(sg, this::resolveFaultStyle);
                    }
                    if (showRivers) {
                        mesh.paintRivers(sg);
                    }
                    if (showBiomeIcons || showColonyRanks || showCapitals) {
                        Rectangle localClip = sg.getClipBounds();
                        for (HexGridMesh.Face face : mesh.faces()) {
                            if (localClip == null || face.intersects(localClip)) {
                                drawHexIcons(sg, face);
                            }
                        }
                    }
                    if (world.getWorldWidth() > 0) {
                        double topY = -(world.getWorldHeight() * 1.5 * hexSize) / 2.0 - 0.75 * hexSize;
                        double botY = +(world.getWorldHeight() * 1.5 * hexSize) / 2.0 - 0.75 * hexSize;
                        float[] dash = {6f, 4f};
                        BasicStroke lineStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f);
                        if (showMeridian) {
                            sg.setColor(new Color(255, 255, 255, 130));
                            sg.setStroke(lineStroke);
                            sg.drawLine(0, (int) Math.round(topY), 0, (int) Math.round(botY));
                        }
                        if (showEquator) {
                            sg.setColor(new Color(255, 255, 255, 130));
                            sg.setStroke(lineStroke);
                            double eqY = -0.75 * hexSize;
                            double leftX = -worldW / 2.0;
                            double rightX = worldW / 2.0;
                            sg.drawLine((int) Math.round(leftX), (int) Math.round(eqY), (int) Math.round(rightX), (int) Math.round(eqY));
                        }
                    }
                }
                if (showTunnels) {
                    HexMapOverlays.paintTunnels(sg, world, mesh);
                }
                if (showTrades) {
                    HexMapOverlays.paintTrades(sg, world, mesh);
                }
                if (showBattles) {
                    HexMapOverlays.paintBattles(sg, world, mesh);
                }
                if (activeHex != null) {
                    HexGridMesh.Face activeFace = mesh.faceAt(activeHex.getQ(), activeHex.getR());
                    if (activeFace != null) {
                        sg.setColor(AssetStyles.FONT_COLOR_ERROR);
                        sg.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                        sg.drawPolygon(activeFace.toPolygon());
                    }
                }
                sg.dispose();
            }

            if (hasAnyGeographicLabelVisible()) {
                for (double shiftX : horizontalShifts) {
                    Graphics2D sg = (Graphics2D) g2d.create();
                    sg.translate(centerOffset.x + shiftX, centerOffset.y);
                    paintGeographicNames(sg, world, mesh);
                    sg.dispose();
                }
            }
        }

        private boolean isFeatureLabelVisible(GeographicFeature.FeatureType type) {
            if (!showGeographicNames || type == null) return false;
            return switch (type) {
                case CONTINENT -> showLabelContinents;
                case ICE_CAP -> showLabelIceCaps;
                case OCEAN -> showLabelOceans;
                case ARCHIPELAGO -> showLabelArchipelagos;
                case TRENCH -> showLabelTrenches;
                case PENINSULA -> showLabelPeninsulas;
                case RIVER -> showLabelRivers;
                case MOUNTAIN_RANGE -> showLabelMountains;
                case VALLEY -> showLabelValleys;
                case LAKE_BASIN -> showLabelLakes;
            };
        }

        private boolean hasAnyGeographicLabelVisible() {
            return showGeographicNames && (showLabelContinents || showLabelIceCaps || showLabelArchipelagos || showLabelOceans
                    || showLabelTrenches || showLabelMountains || showLabelValleys || showLabelLakes
                    || showLabelRivers || showLabelPeninsulas);
        }

        private int getFeaturePriority(GeographicFeature.FeatureType type) {
            return switch (type) {
                case CONTINENT, ICE_CAP -> 0;
                case OCEAN, ARCHIPELAGO -> 1;
                case MOUNTAIN_RANGE, RIVER, TRENCH -> 2;
                case PENINSULA, VALLEY, LAKE_BASIN -> 3;
            };
        }

        private void paintGeographicNames(Graphics2D g2, World world, HexGridMesh mesh) {
            if (world == null || mesh == null) return;
            List<GeographicFeature> features = world.getGeographicFeatures();
            if (features == null || features.isEmpty()) return;

            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            List<GeographicFeature> sortedFeatures = new ArrayList<>(features);
            sortedFeatures.sort((a, b) -> {
                int pA = getFeaturePriority(a.getType());
                int pB = getFeaturePriority(b.getType());
                if (pA != pB) {
                    return Integer.compare(pA, pB);
                }
                int sA = (a.getHexes() != null) ? a.getHexes().size() : 0;
                int sB = (b.getHexes() != null) ? b.getHexes().size() : 0;
                return Integer.compare(sB, sA);
            });

            Font fontBig = AssetStyles.FONT_BOLD.deriveFont(15f);
            Font fontMid = AssetStyles.FONT_BOLD.deriveFont(13f);
            Font fontNorm = AssetStyles.FONT_BOLD.deriveFont(11f);

            FontMetrics fmBig = g2.getFontMetrics(fontBig);
            FontMetrics fmMid = g2.getFontMetrics(fontMid);
            FontMetrics fmNorm = g2.getFontMetrics(fontNorm);

            List<Rectangle> placedBoxes = new ArrayList<>();

            for (GeographicFeature feature : sortedFeatures) {
                if (feature == null) continue;
                if (!isFeatureLabelVisible(feature.getType())) continue;
                String name = feature.getName();
                if (name == null || name.isEmpty()) continue;

                Font font = fontNorm;
                FontMetrics fm = fmNorm;
                int padW = 16;
                int padH = 5;
                int arc = 6;
                float strokeWidth = 1.0f;
                Color borderColor = new Color(215, 195, 100, 220);
                Color textColor = Color.WHITE;

                switch (feature.getType()) {
                    case CONTINENT -> {
                        font = fontBig;
                        fm = fmBig;
                        padW = 24;
                        padH = 8;
                        arc = 8;
                        strokeWidth = 1.8f;
                        borderColor = new Color(245, 185, 75, 235);
                        textColor = new Color(255, 248, 235);
                    }
                    case ICE_CAP -> {
                        font = fontBig;
                        fm = fmBig;
                        padW = 24;
                        padH = 8;
                        arc = 8;
                        strokeWidth = 1.8f;
                        borderColor = new Color(220, 240, 255, 240);
                        textColor = new Color(245, 250, 255);
                    }
                    case OCEAN -> {
                        font = fontMid;
                        fm = fmMid;
                        padW = 20;
                        padH = 6;
                        arc = 7;
                        strokeWidth = 1.3f;
                        borderColor = new Color(75, 160, 245, 225);
                        textColor = new Color(230, 245, 255);
                    }
                    case ARCHIPELAGO -> {
                        font = fontMid;
                        fm = fmMid;
                        padW = 20;
                        padH = 6;
                        arc = 7;
                        strokeWidth = 1.3f;
                        borderColor = new Color(90, 215, 185, 225);
                        textColor = new Color(230, 255, 250);
                    }
                    case TRENCH -> {
                        font = fontNorm;
                        fm = fmNorm;
                        padW = 16;
                        padH = 5;
                        arc = 6;
                        strokeWidth = 1.2f;
                        borderColor = new Color(85, 115, 235, 230);
                        textColor = new Color(215, 235, 255);
                    }
                    case PENINSULA -> {
                        font = fontNorm;
                        fm = fmNorm;
                        padW = 16;
                        padH = 5;
                        arc = 6;
                        strokeWidth = 1.0f;
                        borderColor = new Color(215, 195, 100, 220);
                        textColor = Color.WHITE;
                    }
                    case RIVER -> {
                        font = fontNorm;
                        fm = fmNorm;
                        padW = 16;
                        padH = 5;
                        arc = 6;
                        strokeWidth = 1.0f;
                        borderColor = new Color(70, 150, 240, 220);
                        textColor = Color.WHITE;
                    }
                    case MOUNTAIN_RANGE -> {
                        font = fontNorm;
                        fm = fmNorm;
                        padW = 16;
                        padH = 5;
                        arc = 6;
                        strokeWidth = 1.0f;
                        borderColor = new Color(230, 175, 80, 220);
                        textColor = Color.WHITE;
                    }
                    case VALLEY -> {
                        font = fontNorm;
                        fm = fmNorm;
                        padW = 16;
                        padH = 5;
                        arc = 6;
                        strokeWidth = 1.0f;
                        borderColor = new Color(90, 200, 110, 220);
                        textColor = Color.WHITE;
                    }
                    case LAKE_BASIN -> {
                        font = fontNorm;
                        fm = fmNorm;
                        padW = 16;
                        padH = 5;
                        arc = 6;
                        strokeWidth = 1.0f;
                        borderColor = new Color(80, 210, 230, 220);
                        textColor = Color.WHITE;
                    }
                }

                int tw = fm.stringWidth(name);
                int th = fm.getHeight();
                int pw = tw + padW;
                int ph = th + padH;

                List<Hex> hexes = feature.getHexes();
                Hex centroid = feature.getCentroidHex();
                if (centroid == null && (hexes == null || hexes.isEmpty())) continue;

                double cQ = feature.getCentroidQ();
                double cR = feature.getCentroidR();

                List<Hex> candidates = new ArrayList<>();
                if (centroid != null) {
                    candidates.add(centroid);
                }
                if (hexes != null) {
                    List<Hex> otherHexes = new ArrayList<>(hexes);
                    otherHexes.sort(Comparator.comparingDouble(h -> {
                        double dq = h.getQ() - cQ;
                        double dr = h.getR() - cR;
                        return dq * dq + dr * dr + dq * dr;
                    }));
                    for (Hex h : otherHexes) {
                        if (h != centroid && !candidates.contains(h)) {
                            candidates.add(h);
                            if (candidates.size() >= 12) break;
                        }
                    }
                }

                Rectangle chosenRect = null;
                HexGridMesh.Face chosenFace = null;

                for (Hex cand : candidates) {
                    HexGridMesh.Face face = mesh.faceAt(cand.getQ(), cand.getR());
                    if (face == null) continue;

                    int cx = (int) Math.round(face.centerX);
                    int cy = (int) Math.round(face.centerY);
                    int px = cx - pw / 2;
                    int py = cy - ph / 2;

                    Rectangle testRect = new Rectangle(px - 3, py - 2, pw + 6, ph + 4);
                    boolean collides = false;
                    for (Rectangle placed : placedBoxes) {
                        if (testRect.intersects(placed)) {
                            collides = true;
                            break;
                        }
                    }

                    if (!collides) {
                        chosenRect = new Rectangle(px, py, pw, ph);
                        chosenFace = face;
                        break;
                    }
                }

                if (chosenRect == null) {
                    if (getFeaturePriority(feature.getType()) == 0 && centroid != null) {
                        HexGridMesh.Face face = mesh.faceAt(centroid.getQ(), centroid.getR());
                        if (face != null) {
                            int cx = (int) Math.round(face.centerX);
                            int cy = (int) Math.round(face.centerY);
                            chosenRect = new Rectangle(cx - pw / 2, cy - ph / 2, pw, ph);
                            chosenFace = face;
                        }
                    }
                }

                if (chosenRect == null || chosenFace == null) {
                    continue;
                }

                placedBoxes.add(chosenRect);

                g2.setFont(font);
                g2.setColor(new Color(18, 22, 28, 215));
                g2.fillRoundRect(chosenRect.x, chosenRect.y, chosenRect.width, chosenRect.height, arc, arc);

                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(strokeWidth));
                g2.drawRoundRect(chosenRect.x, chosenRect.y, chosenRect.width, chosenRect.height, arc, arc);

                g2.setColor(textColor);
                int textX = chosenRect.x + (chosenRect.width - tw) / 2;
                int textY = chosenRect.y + (chosenRect.height - th) / 2 + fm.getAscent();
                g2.drawString(name, textX, textY);
            }
        }

        private Point getCenterOffset() {
            double vertAdjust = (world != null && world.getWorldWidth() > 0) ? (0.75 * hexSize) : 0.0;
            return new Point(
                    (int) Math.round(getWidth() / 2.0 + panX),
                    (int) Math.round(getHeight() / 2.0 + vertAdjust + panY));
        }

        private HexGridMesh.FaultStyle resolveFaultStyle(HexGridMesh.Edge edge) {
            if (world == null || edge == null || edge.faceA == null || edge.faceB == null) {
                return null;
            }
            Hex hA = edge.faceA.hex;
            Hex hB = edge.faceB.hex;
            if (hA == null || hB == null) {
                return null;
            }
            int pA = hA.getPlateId();
            int pB = hB.getPlateId();
            if (pA <= 0 || pB <= 0 || pA == pB) {
                return null;
            }
            FaultLine.FaultType type = world.getFaultTypeBetweenPlates(pA, pB);
            Color color = null;
            if (type == FaultLine.FaultType.CONVERGENT) {
                color = FAULT_CONVERGENT_COLOR;
            } else if (type == FaultLine.FaultType.DIVERGENT) {
                color = FAULT_DIVERGENT_COLOR;
            } else if (type == FaultLine.FaultType.TRANSFORM) {
                color = FAULT_TRANSFORM_COLOR;
            }
            if (color == null) {
                return null;
            }
            int intensity = world.getFaultIntensityBetweenPlates(pA, pB);
            return new HexGridMesh.FaultStyle(color, intensity);
        }

        private Color resolveFaultBorderColor(HexGridMesh.Edge edge) {
            HexGridMesh.FaultStyle style = resolveFaultStyle(edge);
            return style != null ? style.color : null;
        }

        private Color resolveFillColor(Hex hex) {
            if (mapMode == MapMode.HEIGHT) {
                int elev = Math.max(0, Math.min(10, hex.getElevation()));
                Color base = HEIGHT_COLORS[elev];
                if (hex == world.getActiveHex()) {
                    return base;
                }
                float fade = AssetStyles.mapInactiveHexFade();
                return AssetStyles.fadeTowardBackground(base, fade);
            }
            if (mapMode == MapMode.TECTONIC_PLATES) {
                int plateId = hex.getPlateId();
                if (plateId > 0) {
                    Color base = PLATE_COLORS[(plateId - 1) % PLATE_COLORS.length];
                    if (hex == world.getActiveHex()) {
                        return base;
                    }
                    float fade = AssetStyles.mapInactiveHexFade();
                    return AssetStyles.fadeTowardBackground(base, fade);
                }
            }
            Biome biome = hex.getBiome();
            if (hex == world.getActiveHex()) {
                return biome != null ? getBiomeColor(biome) : AssetStyles.BACKGROUND_COLOR;
            }
            if (biome == null) {
                return AssetStyles.BACKGROUND_COLOR;
            }
            Color faded = biomeFadedColorCache.get(biome.getId());
            if (faded == null) {
                Color base = getBiomeColor(biome);
                float fade = AssetStyles.mapInactiveHexFade();
                faded = AssetStyles.fadeTowardBackground(base, fade);
                biomeFadedColorCache.put(biome.getId(), faded);
            }
            return faded;
        }

        private void drawHexIcons(Graphics2D g2d, HexGridMesh.Face face) {
            Rectangle clip = g2d.getClipBounds();
            if (clip != null && !clip.intersects(face.centerX - hexSize, face.centerY - hexSize, hexSize * 2.0, hexSize * 2.0)) {
                return;
            }
            Hex hex = face.hex;
            int iconSize = Math.max(1, (int) Math.round(hexSize * 0.72));

            if (showBiomeIcons) {
                Biome biome = hex.getBiome();
                if (biome != null) {
                    Image icon = getScaledBiomeIcon(biome, iconSize);
                    if (icon != null) {
                        int iconX = HexMapGeometry.centeredIconOrigin(face.centerX, iconSize);
                        int iconY = HexMapGeometry.centeredIconOrigin(face.centerY, iconSize);
                        g2d.drawImage(icon, iconX, iconY, null);
                    }
                }
            }

            if (showColonyRanks && hex.getColony() != null) {
                Rank rank = hex.getColony().getRank();
                if (rank != null) {
                    Image rankImg = getScaledRankIcon(rank, iconSize);
                    if (rankImg != null) {
                        int rankX = HexMapGeometry.centeredIconOrigin(face.centerX, iconSize);
                        int rankY = HexMapGeometry.centeredIconOrigin(face.centerY - hexSize * 0.78, iconSize);
                        g2d.drawImage(rankImg, rankX, rankY, null);
                    }
                }
            }

            if (showCapitals && hex.getColony() != null && hex.getColony().isCapital()) {
                Image capImg = getScaledCapitalIcon(iconSize);
                if (capImg != null) {
                    int capX = HexMapGeometry.centeredIconOrigin(face.centerX, iconSize);
                    int capY = HexMapGeometry.centeredIconOrigin(face.centerY + hexSize * 0.78, iconSize);
                    g2d.drawImage(capImg, capX, capY, null);
                }
            }
        }

        private Polygon getHexPolygon(Hex hex, int offsetX, int offsetY) {
            return HexMapGeometry.flatTopPolygon(hex.getQ(), hex.getR(), offsetX, offsetY, hexSize);
        }

        private Color getBiomeColor(Biome biome) {
            if (biomeColorCache.containsKey(biome.getId())) {
                return biomeColorCache.get(biome.getId());
            }

            Color mapColor = biome.getMapColor();
            if (mapColor == null) {
                return AssetStyles.BACKGROUND_COLOR;
            }

            biomeColorCache.put(biome.getId(), mapColor);
            return mapColor;
        }
    }
}
