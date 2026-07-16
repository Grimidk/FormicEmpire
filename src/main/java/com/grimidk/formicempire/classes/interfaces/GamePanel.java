package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.constants.misc.PactRequestIncomingPolicy;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyRebellionService;
import com.grimidk.formicempire.classes.entities.services.world.WarService;
import com.grimidk.formicempire.classes.entities.services.world.WarStanding;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.interfaces.game.managers.AlertManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.TriggerManager;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.GameControlKeyBindings;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.infrasctructure.assets.GameSpritePreloader;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.*;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.*;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiScrollBarStyles;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.util.List;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class GamePanel extends ZeroGamePanel {
    private final MainFrame frame;
    
    private JLabel statusLabel;
    private JLabel statusIndicator;
    
    private ColonyPanel colonyPanel; 
    private WorldPanel worldPanel;  
    private AlertPanel alertPanel; 
    private ControlPanel controlPanel;
    private GameAreaPanel gameAreaPanel;
    private JScrollPane gameScrollPane;
    
    private HatchRateDialog hatchDialog;
    private RoleManagementDialog roleDialog;    
    private UpgradeDialog upgradeDialog; 
    private AbilitiesDialog abilitiesDialog;
    private MapDialog mapDialog;
    private StatsDialog statsDialog; 
    private DynastyManagementDialog dynastyDialog;
    private WarBattleDialog warBattleDialog;
    private ConvoyDialog convoyDialog;
    private SettingsPanel.SettingsDialog settingsDialog;

    private AlertManager alertManager;
    private TriggerManager triggerManager; 

    private Runnable minuteTickListener;
    private Runnable hourTickListener;
    private Runnable dayTickListener;
    private Runnable monthTickListener;

    private final AtomicInteger pendingMinuteGuiSteps = new AtomicInteger(0);
    private Timer minuteDrainTimer;

    private boolean overworldPanDragging;
    private Point overworldPanLastScreen;
    private Timer overworldSpringTimer;
    private Point overworldSpringStartPos;
    private Point overworldSpringTargetPos;
    private long overworldSpringStartMs;

    private volatile boolean engineStarted = false;
    private SwingWorker<Void, Void> loadWorker;
    private KeyEventDispatcher plusSpeedKeyDispatcher;
    private int lastPeaceOfferPromptWarId = -1;

    private static final int OVERWORLD_SPRING_DURATION_MS = 240;
    private static final int OVERWORLD_IDLE_RECENTER_MS = 1000;
    private static final int OVERWORLD_IDLE_RECENTER_POLL_MS = 200;
    private static final double OVERWORLD_CENTERED_EPS_PX = 2.5;

    private long overworldLastUserPanMs = System.currentTimeMillis();
    private Timer overworldIdleRecenterTimer;
    private AdjustmentListener overworldScrollbarPanListener;
    
    public GamePanel(MainFrame frame) {
        super(new BorderLayout()); 
        this.frame = frame;
        
        initComponents();
        initControlPanelCallbacks();
        initGameControlKeyBindings();
        initLayout();        
        updateStatusIndicator(false);
    }

    @Override
    protected void initComponents() {
        statusLabel = new JLabel(LanguageStrings.get(LanguageStrings.UI_NOT_STARTED));
        statusIndicator = new JLabel();
        
        colonyPanel = new ColonyPanel();
        colonyPanel.setEngine(frame.getEngine());
        worldPanel = new WorldPanel();
        alertPanel = new AlertPanel();
        gameAreaPanel = new GameAreaPanel();
        gameAreaPanel.setEngine(frame.getEngine());
        
        gameScrollPane = new JScrollPane(gameAreaPanel);
        gameScrollPane.setBorder(null);
        gameScrollPane.getViewport().setOpaque(false);
        gameScrollPane.setOpaque(false);
        UiScrollBarStyles.hide(gameScrollPane);
        gameScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        gameScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateGameAreaSize();
                if (engineStarted && gameAreaPanel.getCurrentDimension() == WorldSpaces.OVERWORLD) {
                    centerOverworldScroll();
                }
            }
        });
        gameScrollPane.getViewport().addChangeListener(e -> {
            if (!engineStarted || gameAreaPanel == null) {
                return;
            }
            Rectangle vr = gameScrollPane.getViewport().getViewRect();
            gameAreaPanel.setPaintViewportRect(vr);
            gameAreaPanel.repaint();
        });

        setupOverworldPanAndSpring();
        setupGameAreaWheelScroll();
        setupOverworldIdleRecenter();
    }

    private void noteOverworldUserPan() {
        overworldLastUserPanMs = System.currentTimeMillis();
    }

    private boolean isOverworldAutoRecenterEnabled() {
        Engine engine = frame.getEngine();
        return engine == null || engine.isOverworldAutoRecenter();
    }

    public void applyOverworldRecenterSetting() {
        if (!isOverworldAutoRecenterEnabled()) {
            stopOverworldSpring();
            stopOverworldIdleRecenter();
            return;
        }
        if (engineStarted) {
            startOverworldIdleRecenter();
        }
    }

    public void refreshAuditMenuOption() {
        if (controlPanel != null) {
            controlPanel.updateAuditMenu(frame.getEngine().isShowAuditMenu());
        }
    }

    private boolean isOverworldViewCentered() {
        if (gameScrollPane == null) {
            return true;
        }
        Point cur = gameScrollPane.getViewport().getViewPosition();
        return cur.distance(computeOverworldCenterViewPosition()) < OVERWORLD_CENTERED_EPS_PX;
    }

    private void setupOverworldIdleRecenter() {
        overworldIdleRecenterTimer = new Timer(OVERWORLD_IDLE_RECENTER_POLL_MS, e -> {
            if (!isOverworldAutoRecenterEnabled()) {
                return;
            }
            if (!engineStarted || gameScrollPane == null || gameAreaPanel == null) {
                return;
            }
            if (gameAreaPanel.getCurrentDimension() != WorldSpaces.OVERWORLD) {
                return;
            }
            if (overworldPanDragging) {
                return;
            }
            if (overworldSpringTimer != null && overworldSpringTimer.isRunning()) {
                return;
            }
            if (System.currentTimeMillis() - overworldLastUserPanMs < OVERWORLD_IDLE_RECENTER_MS) {
                return;
            }
            if (isOverworldViewCentered()) {
                return;
            }
            startOverworldSpringToCenter();
        });
        overworldIdleRecenterTimer.setRepeats(true);
    }

    private void startOverworldIdleRecenter() {
        if (!isOverworldAutoRecenterEnabled()) {
            stopOverworldIdleRecenter();
            return;
        }
        if (overworldIdleRecenterTimer == null) {
            setupOverworldIdleRecenter();
        }
        overworldIdleRecenterTimer.start();
    }

    private void stopOverworldIdleRecenter() {
        if (overworldIdleRecenterTimer != null) {
            overworldIdleRecenterTimer.stop();
        }
    }

    private void setupOverworldScrollbarPanTracking() {
        if (overworldScrollbarPanListener != null) {
            return;
        }
        overworldScrollbarPanListener = e -> {
            if (e.getValueIsAdjusting() && engineStarted && gameAreaPanel != null
                    && gameAreaPanel.getCurrentDimension() == WorldSpaces.OVERWORLD) {
                noteOverworldUserPan();
            }
        };
        gameScrollPane.getVerticalScrollBar().addAdjustmentListener(overworldScrollbarPanListener);
        gameScrollPane.getHorizontalScrollBar().addAdjustmentListener(overworldScrollbarPanListener);
    }

    private void removeOverworldScrollbarPanTracking() {
        if (overworldScrollbarPanListener != null && gameScrollPane != null) {
            gameScrollPane.getVerticalScrollBar().removeAdjustmentListener(overworldScrollbarPanListener);
            gameScrollPane.getHorizontalScrollBar().removeAdjustmentListener(overworldScrollbarPanListener);
            overworldScrollbarPanListener = null;
        }
    }

    private void setupGameAreaWheelScroll() {
        gameScrollPane.setWheelScrollingEnabled(false);
        MouseWheelListener wheelListener = this::handleGameAreaWheel;
        gameAreaPanel.addMouseWheelListener(wheelListener);
    }

    private void handleGameAreaWheel(MouseWheelEvent e) {
        if (!engineStarted || gameAreaPanel == null || gameScrollPane == null) {
            return;
        }
        if (gameAreaPanel.getCurrentDimension() == WorldSpaces.OVERWORLD) {
            noteOverworldUserPan();
            if (e.isShiftDown()) {
                JViewport vp = gameScrollPane.getViewport();
                Point p = vp.getViewPosition();
                int increment = gameScrollPane.getHorizontalScrollBar().getUnitIncrement();
                p.x += e.getWheelRotation() * increment;
                clampOverworldViewPosition(vp, p);
                vp.setViewPosition(p);
                e.consume();
                return;
            }
        }
        JScrollBar verticalBar = gameScrollPane.getVerticalScrollBar();
        int next = verticalBar.getValue() + e.getUnitsToScroll() * verticalBar.getUnitIncrement();
        int max = Math.max(verticalBar.getMinimum(),
                verticalBar.getMaximum() - verticalBar.getVisibleAmount() + 1);
        verticalBar.setValue(Math.max(verticalBar.getMinimum(), Math.min(max, next)));
        e.consume();
    }

    private void setupOverworldPanAndSpring() {
        gameAreaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!engineStarted || gameAreaPanel.getCurrentDimension() != WorldSpaces.OVERWORLD) {
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                stopOverworldSpring();
                noteOverworldUserPan();
                overworldPanDragging = true;
                overworldPanLastScreen = e.getLocationOnScreen();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!overworldPanDragging) {
                    return;
                }
                overworldPanDragging = false;
                overworldPanLastScreen = null;
                noteOverworldUserPan();
            }
        });
        gameAreaPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!overworldPanDragging || overworldPanLastScreen == null
                        || gameAreaPanel.getCurrentDimension() != WorldSpaces.OVERWORLD) {
                    return;
                }
                noteOverworldUserPan();
                Point screen = e.getLocationOnScreen();
                int dx = screen.x - overworldPanLastScreen.x;
                int dy = screen.y - overworldPanLastScreen.y;
                overworldPanLastScreen = screen;
                JViewport vp = gameScrollPane.getViewport();
                Point p = vp.getViewPosition();
                p.x -= dx;
                p.y -= dy;
                clampOverworldViewPosition(vp, p);
                vp.setViewPosition(p);
            }
        });
    }

    private void clampOverworldViewPosition(JViewport vp, Point p) {
        Dimension ext = vp.getExtentSize();
        Dimension vs = vp.getView().getSize();
        int maxX = Math.max(0, vs.width - ext.width);
        int maxY = Math.max(0, vs.height - ext.height);
        p.x = Math.max(0, Math.min(maxX, p.x));
        p.y = Math.max(0, Math.min(maxY, p.y));
    }

    private Point computeOverworldCenterViewPosition() {
        JViewport vp = gameScrollPane.getViewport();
        Dimension ext = vp.getExtentSize();
        Dimension vs = vp.getView().getSize();
        int maxX = Math.max(0, vs.width - ext.width);
        int maxY = Math.max(0, vs.height - ext.height);

        Colony colony = getColonyFromEngine(frame.getEngine());
        if (gameAreaPanel != null && colony != null) {
            Point colonyCenter = gameAreaPanel.computeOverworldColonyCenterPanelPixels(
                    colony, ext.width, ext.height);
            Point centered = new Point(colonyCenter.x - ext.width / 2, colonyCenter.y - ext.height / 2);
            centered.x = Math.max(0, Math.min(maxX, centered.x));
            centered.y = Math.max(0, Math.min(maxY, centered.y));
            return centered;
        }

        return new Point(maxX / 2, maxY / 2);
    }

    void centerOverworldScroll() {
        if (gameScrollPane == null || gameAreaPanel == null) {
            return;
        }
        if (gameAreaPanel.getCurrentDimension() != WorldSpaces.OVERWORLD) {
            return;
        }
        stopOverworldSpring();
        SwingUtilities.invokeLater(() -> {
            JViewport vp = gameScrollPane.getViewport();
            vp.setViewPosition(computeOverworldCenterViewPosition());
            gameAreaPanel.repaint();
        });
    }

    private void stopOverworldSpring() {
        if (overworldSpringTimer != null && overworldSpringTimer.isRunning()) {
            overworldSpringTimer.stop();
        }
        overworldSpringTimer = null;
    }

    private void startOverworldSpringToCenter() {
        if (gameScrollPane == null || gameAreaPanel == null) {
            return;
        }
        if (gameAreaPanel.getCurrentDimension() != WorldSpaces.OVERWORLD) {
            return;
        }
        JViewport vp = gameScrollPane.getViewport();
        Point target = computeOverworldCenterViewPosition();
        Point start = vp.getViewPosition();
        if (start.distance(target) < 1.5) {
            vp.setViewPosition(target);
            return;
        }
        stopOverworldSpring();
        overworldSpringStartPos = new Point(start);
        overworldSpringTargetPos = new Point(target);
        overworldSpringStartMs = System.currentTimeMillis();
        overworldSpringTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - overworldSpringStartMs;
            float t = Math.min(1f, elapsed / (float) OVERWORLD_SPRING_DURATION_MS);
            float ease = 1f - (1f - t) * (1f - t);
            int x = (int) (overworldSpringStartPos.x + (overworldSpringTargetPos.x - overworldSpringStartPos.x) * ease);
            int y = (int) (overworldSpringStartPos.y + (overworldSpringTargetPos.y - overworldSpringStartPos.y) * ease);
            if (t >= 1f) {
                vp.setViewPosition(overworldSpringTargetPos);
                stopOverworldSpring();
                return;
            }
            vp.setViewPosition(new Point(x, y));
            gameAreaPanel.repaint();
        });
        overworldSpringTimer.start();
    }
    
    @Override
    public void refreshTranslations() {
        updateStatusIndicator(frame.getEngine().isPaused());
        
        // Refresh subpanels
        worldPanel.refreshTranslations();
        colonyPanel.refreshTranslations();
        alertPanel.refreshTranslations();
        controlPanel.refreshTranslations();
        
        // Refresh open dialogs
        if (hatchDialog != null && hatchDialog.isShowing()) hatchDialog.refreshTranslations();
        if (roleDialog != null && roleDialog.isShowing()) roleDialog.refreshTranslations();
        if (upgradeDialog != null && upgradeDialog.isShowing()) upgradeDialog.refreshTranslations();
        if (abilitiesDialog != null && abilitiesDialog.isShowing()) abilitiesDialog.refreshTranslations();
        if (mapDialog != null && mapDialog.isShowing()) mapDialog.refreshTranslations();
        if (statsDialog != null && statsDialog.isShowing()) statsDialog.refreshTranslations();
        if (dynastyDialog != null && dynastyDialog.isShowing()) dynastyDialog.refreshTranslations();
        if (warBattleDialog != null && warBattleDialog.isShowing()) warBattleDialog.refreshTranslations();
        if (convoyDialog != null && convoyDialog.isShowing()) convoyDialog.refreshTranslations();
        if (settingsDialog != null && settingsDialog.isShowing()) settingsDialog.refreshDialog();
    }

    @Override
    public void refreshTheme() {
        super.refreshTheme();
        updateStatusIndicator(frame.getEngine().isPaused());
        worldPanel.refreshTheme();
        colonyPanel.refreshTheme();
        alertPanel.refreshTheme();
        controlPanel.refreshTheme();
        refreshOpenDialogThemes();
    }

    private void refreshOpenDialogThemes() {
        if (hatchDialog != null && hatchDialog.isShowing()) hatchDialog.refreshTheme();
        if (roleDialog != null && roleDialog.isShowing()) roleDialog.refreshTheme();
        if (upgradeDialog != null && upgradeDialog.isShowing()) upgradeDialog.refreshTheme();
        if (abilitiesDialog != null && abilitiesDialog.isShowing()) abilitiesDialog.refreshTheme();
        if (mapDialog != null && mapDialog.isShowing()) mapDialog.refreshTheme();
        if (statsDialog != null && statsDialog.isShowing()) statsDialog.refreshTheme();
        if (dynastyDialog != null && dynastyDialog.isShowing()) dynastyDialog.refreshTheme();
        if (warBattleDialog != null && warBattleDialog.isShowing()) warBattleDialog.refreshTheme();
        if (convoyDialog != null && convoyDialog.isShowing()) convoyDialog.refreshTheme();
        if (settingsDialog != null && settingsDialog.isShowing()) settingsDialog.refreshTheme();
    }
    
    private void initControlPanelCallbacks() {
        Runnable handleBackButtonCallback = this::handleBackButton;
        Runnable showHatchRateDialogCallback = this::showHatchRateDialog;        
        Runnable showResearchDialogCallback = this::showResearchDialog;
        Runnable showBuildDialogCallback = this::showBuildDialog;
        Runnable showAssimilationDialogCallback = this::showAssimilationDialog;
        Runnable showSynergyDialogCallback = this::showSynergyDialog;
        Runnable showAbilitiesDialogCallback = this::showAbilitiesDialog;
        Runnable showMapDialogCallback = this::showMapDialog; 
        Runnable showStatsDialogCallback = this::showStatsDialog;
        Runnable showDynastyDialogCallback = this::showDynastyDialog;
        Runnable showTradeDialogCallback = this::showTradeDialog;
        Runnable showDiplomacyDialogCallback = this::showDiplomacyDialog;
        Runnable showWarDialogCallback = this::showWarDialog;
        Runnable showSettingsDialogCallback = this::showSettingsDialog;
        ControlPanel.RoleManagementCallback showRoleManagementDialogCallback = this::showRoleManagementDialog;
        
        Runnable toggleViewCallback = () -> {
            if (gameAreaPanel != null) {
                gameAreaPanel.toggleDimension();
                
                updateGameAreaSize();

                if (gameAreaPanel.getCurrentDimension() == WorldSpaces.OVERWORLD) {
                    centerOverworldScroll();
                    noteOverworldUserPan();
                    SwingUtilities.invokeLater(() -> {
                        if (gameScrollPane != null && gameAreaPanel != null) {
                            gameAreaPanel.setPaintViewportRect(gameScrollPane.getViewport().getViewRect());
                            gameAreaPanel.repaint();
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        updateGameAreaSize();
                        if (gameScrollPane != null && gameAreaPanel != null) {
                            gameScrollPane.getVerticalScrollBar().setValue(0);
                            gameAreaPanel.setPaintViewportRect(gameScrollPane.getViewport().getViewRect());
                            gameAreaPanel.repaint();
                        }
                    });
                }
            }
        };
        
        controlPanel = new ControlPanel(frame, 
            handleBackButtonCallback, 
            showHatchRateDialogCallback, 
            showResearchDialogCallback,
            showBuildDialogCallback,
            showAssimilationDialogCallback,
            showSynergyDialogCallback,
            showRoleManagementDialogCallback,
            showAbilitiesDialogCallback,
            showStatsDialogCallback,
            toggleViewCallback,
            showMapDialogCallback,
            showDynastyDialogCallback,
            showTradeDialogCallback,
            showDiplomacyDialogCallback,
            showWarDialogCallback,
            showSettingsDialogCallback,
            () -> dynastyDialog != null && dynastyDialog.isShowing());
    }

    private void initGameControlKeyBindings() {
        JRootPane root = frame.getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();
        GameControlKeyBindings.register(
                inputMap,
                actionMap,
                () -> {
                    if (!engineStarted || controlPanel == null) {
                        return;
                    }
                    controlPanel.togglePauseFromInput();
                },
                () -> {
                    if (!engineStarted || controlPanel == null) {
                        return;
                    }
                    controlPanel.stepSpeedFromInput(1);
                },
                () -> {
                    if (!engineStarted || controlPanel == null) {
                        return;
                    }
                    controlPanel.stepSpeedFromInput(-1);
                });
    }

    private void installPlusSpeedKeyDispatcher() {
        if (plusSpeedKeyDispatcher != null) {
            return;
        }
        plusSpeedKeyDispatcher = e -> {
            if (!engineStarted || controlPanel == null || e.getID() != KeyEvent.KEY_TYPED) {
                return false;
            }
            Component source = e.getComponent();
            if (source == null) {
                return false;
            }
            Window sourceWindow = SwingUtilities.getWindowAncestor(source);
            if (sourceWindow != frame) {
                return false;
            }
            if (e.getKeyChar() == '+') {
                controlPanel.stepSpeedFromInput(1);
                return true;
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(plusSpeedKeyDispatcher);
    }

    private void uninstallPlusSpeedKeyDispatcher() {
        if (plusSpeedKeyDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(plusSpeedKeyDispatcher);
            plusSpeedKeyDispatcher = null;
        }
    }
    
    private void updateGameAreaSize() {
        if (gameAreaPanel != null && gameScrollPane != null) {
            java.awt.Dimension viewportSize = gameScrollPane.getViewport().getSize();
            gameAreaPanel.refreshSize(viewportSize.width, viewportSize.height);
        }
    }

    public void onWindowGeometryChanged() {
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
            if (engineStarted) {
                updateGameAreaSize();
            }
        });
    }

    @Override
    protected void initLayout() {
        add(createNorthPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER); 
        if (controlPanel != null) {
            add(controlPanel, BorderLayout.SOUTH);
        }
    }

    private JPanel createNorthPanel() {
        statusIndicator.setOpaque(true);
        statusIndicator.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        statusIndicator.setPreferredSize(new Dimension(12, 12));
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(statusIndicator);
        north.add(statusLabel);
        return north;
    }

   private JPanel createCenterPanel() {
        JPanel center = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;
        gbc.weighty = 1.0; 
        gbc.fill = GridBagConstraints.BOTH;

        // --- Colony Panel (Left) ---
        colonyPanel.setPreferredSize(new Dimension(200, 0));
        colonyPanel.setMinimumSize(new Dimension(200, 0));
        gbc.gridx = 0;
        gbc.weightx = 0.0; 
        center.add(colonyPanel, gbc);

        // --- Game Area (Middle) ---
        gameScrollPane.setPreferredSize(new Dimension(100, 0));
        gbc.gridx = 1;
        gbc.weightx = 0.6; 
        center.add(gameScrollPane, gbc); 

        // --- Right Panel Container (World + Alert) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(230, 0));
        rightPanel.setMinimumSize(new Dimension(230, 0));
        rightPanel.add(worldPanel, BorderLayout.NORTH);
        rightPanel.add(alertPanel, BorderLayout.CENTER); 

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        center.add(rightPanel, gbc);
        
        return center;
    }

    // --- Dialog Methods ---
    private void showHatchRateDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (hatchDialog != null && hatchDialog.isShowing()) {
            hatchDialog.dispose();
            return;
        }
        
        if (hatchDialog != null) {
            hatchDialog.dispose();
        }
        hatchDialog = new HatchRateDialog(frame, colony);
        hatchDialog.showDialog();
    }

    private void showRoleManagementDialog(int tabType) {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (roleDialog != null && roleDialog.getColony() != colony) {
            roleDialog.dispose();
            roleDialog = null;
        }

        if (roleDialog != null && roleDialog.isTabOpen(tabType)) {
            roleDialog.dispose();
            return;
        }

        if (roleDialog != null && roleDialog.isShowing()) {
            roleDialog.selectTab(tabType);
            roleDialog.requestFocus();
            return;
        }
        
        if (roleDialog != null) {
            roleDialog.dispose();
        }
        roleDialog = new RoleManagementDialog(frame, colony);
        roleDialog.showDialog(tabType);
    }

    private void showResearchDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (upgradeDialog != null && upgradeDialog.isTabOpen(UpgradeDialog.TAB_RESEARCH)) {
            upgradeDialog.dispose();
            return;
        }

        if (upgradeDialog != null && upgradeDialog.isShowing()) {
            upgradeDialog.setTab(UpgradeDialog.TAB_RESEARCH);
            upgradeDialog.requestFocus();
            return;
        }
        
        if (upgradeDialog != null) {
            upgradeDialog.dispose();
        }
        upgradeDialog = new UpgradeDialog(frame, colony, engine);
        upgradeDialog.showDialog(UpgradeDialog.TAB_RESEARCH);
    }
    
    private void showBuildDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (upgradeDialog != null && upgradeDialog.isTabOpen(UpgradeDialog.TAB_BUILD)) {
            upgradeDialog.dispose();
            return;
        }

        if (upgradeDialog != null && upgradeDialog.isShowing()) {
            upgradeDialog.setTab(UpgradeDialog.TAB_BUILD);
            upgradeDialog.requestFocus();
            return;
        }
        
        if (upgradeDialog != null) {
            upgradeDialog.dispose();
        }
        upgradeDialog = new UpgradeDialog(frame, colony, engine);
        upgradeDialog.showDialog(UpgradeDialog.TAB_BUILD);
    }

    private void showAssimilationDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (upgradeDialog != null && upgradeDialog.isTabOpen(UpgradeDialog.TAB_ASSIMILATION)) {
            upgradeDialog.dispose();
            return;
        }

        if (upgradeDialog != null && upgradeDialog.isShowing()) {
            upgradeDialog.setTab(UpgradeDialog.TAB_ASSIMILATION);
            upgradeDialog.requestFocus();
            return;
        }
        
        if (upgradeDialog != null) {
            upgradeDialog.dispose();
        }
        upgradeDialog = new UpgradeDialog(frame, colony, engine);
        upgradeDialog.showDialog(UpgradeDialog.TAB_ASSIMILATION);
    }

    private void showSynergyDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;

        if (upgradeDialog != null && upgradeDialog.isTabOpen(UpgradeDialog.TAB_SYNERGY)) {
            upgradeDialog.dispose();
            return;
        }

        if (upgradeDialog != null && upgradeDialog.isShowing()) {
            upgradeDialog.setTab(UpgradeDialog.TAB_SYNERGY);
            upgradeDialog.requestFocus();
            return;
        }

        if (upgradeDialog != null) {
            upgradeDialog.dispose();
        }
        upgradeDialog = new UpgradeDialog(frame, colony, engine);
        upgradeDialog.showDialog(UpgradeDialog.TAB_SYNERGY);
    }

    private void showAbilitiesDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (abilitiesDialog != null && abilitiesDialog.isShowing()) {
            abilitiesDialog.dispose();
            return;
        }
        
        if (abilitiesDialog != null) {
            abilitiesDialog.dispose();
        }
        abilitiesDialog = new AbilitiesDialog(frame, colony);
        abilitiesDialog.showDialog();
    }

    private void showMapDialog() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        if (world == null) return;
        
        if (mapDialog != null && mapDialog.isShowing()) {
            mapDialog.dispose();
            return;
        }
        
        if (mapDialog == null || mapDialog.getOwner() != frame) {
            if (mapDialog != null) mapDialog.dispose();
            mapDialog = new MapDialog(frame, world, this::refreshAllGUIData, this::showWarDialog);
        }
        mapDialog.showDialog();
    }

    private void showStatsDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || colony.getDynasty() == null) return;

        if (statsDialog != null && statsDialog.isShowing()) {
            statsDialog.dispose();
            return;
        }

        if (statsDialog != null) {
            statsDialog.dispose();
        }
        
        statsDialog = new StatsDialog(frame, colony, engine);
        statsDialog.showDialog();
    }

    private void showDynastyDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || colony.getDynasty() == null) return;

        if (dynastyDialog != null && dynastyDialog.isTabOpen(DynastyManagementDialog.TAB_OVERVIEW)) {
            dynastyDialog.dispose();
            return;
        }

        if (dynastyDialog != null && dynastyDialog.isShowing()) {
            dynastyDialog.setTab(DynastyManagementDialog.TAB_OVERVIEW);
            dynastyDialog.requestFocus();
            return;
        }

        if (dynastyDialog != null) {
            dynastyDialog.dispose();
        }

        dynastyDialog = new DynastyManagementDialog(frame, colony.getDynasty(), engine, this::handleGoToColony,
                this::showWarRolesFromManagement, this::showWarBattle, this::showConvoyView);
        dynastyDialog.showDialog(DynastyManagementDialog.TAB_OVERVIEW);
    }

    private void showWarDialog() {
        showDynastyWarsTab();
    }

    private void showDynastyWarsTab() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || colony.getDynasty() == null
                || !colony.hasUpgrade(GameUnlocks.ABILITY_DYNASTY)) {
            return;
        }

        if (dynastyDialog != null && dynastyDialog.isTabOpen(DynastyManagementDialog.TAB_WARS)) {
            dynastyDialog.dispose();
            return;
        }

        if (dynastyDialog != null && dynastyDialog.isShowing()) {
            dynastyDialog.setTab(DynastyManagementDialog.TAB_WARS);
            dynastyDialog.requestFocus();
            return;
        }

        if (dynastyDialog != null) {
            dynastyDialog.dispose();
        }

        dynastyDialog = new DynastyManagementDialog(frame, colony.getDynasty(), engine, this::handleGoToColony,
                this::showWarRolesFromManagement, this::showWarBattle, this::showConvoyView);
        dynastyDialog.showDialog(DynastyManagementDialog.TAB_WARS);
    }

    private void showWarRolesFromManagement() {
        showRoleManagementDialog(RoleManagementDialog.TAB_WORKER);
    }

    private void showWarBattle(War war) {
        if (war == null || !war.isActive()) {
            return;
        }
        Engine engine = frame.getEngine();
        if (engine == null || engine.getWorld() == null) {
            return;
        }
        if (warBattleDialog != null && warBattleDialog.isShowing()) {
            warBattleDialog.dispose();
        }
        if (warBattleDialog != null) {
            warBattleDialog.dispose();
        }
        warBattleDialog = new WarBattleDialog(frame, war, engine);
        warBattleDialog.showDialog();
    }

    private void showConvoyView(Trade trade) {
        if (trade == null) {
            return;
        }
        Engine engine = frame.getEngine();
        if (engine == null || engine.getWorld() == null) {
            return;
        }
        if (convoyDialog != null && convoyDialog.isShowing()) {
            convoyDialog.dispose();
        }
        if (convoyDialog != null) {
            convoyDialog.dispose();
        }
        convoyDialog = new ConvoyDialog(frame, trade, engine);
        convoyDialog.showDialog();
    }

    private void showDynastyDialogTab(int tabIndex) {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || colony.getDynasty() == null) return;

        if (dynastyDialog != null && dynastyDialog.isTabOpen(tabIndex)) {
            dynastyDialog.requestFocus();
            return;
        }

        if (dynastyDialog != null && dynastyDialog.isShowing()) {
            dynastyDialog.setTab(tabIndex);
            dynastyDialog.requestFocus();
            return;
        }

        if (dynastyDialog != null) {
            dynastyDialog.dispose();
        }

        dynastyDialog = new DynastyManagementDialog(frame, colony.getDynasty(), engine, this::handleGoToColony,
                this::showWarRolesFromManagement, this::showWarBattle, this::showConvoyView);
        dynastyDialog.showDialog(tabIndex);
    }

    private void showTradeDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || colony.getDynasty() == null) return;

        if (dynastyDialog != null && dynastyDialog.isTabOpen(DynastyManagementDialog.TAB_TRADE)) {
            dynastyDialog.dispose();
            return;
        }

        if (dynastyDialog != null && dynastyDialog.isShowing()) {
            dynastyDialog.setTab(DynastyManagementDialog.TAB_TRADE);
            dynastyDialog.requestFocus();
            return;
        }

        if (dynastyDialog != null) {
            dynastyDialog.dispose();
        }

        dynastyDialog = new DynastyManagementDialog(frame, colony.getDynasty(), engine, this::handleGoToColony,
                this::showWarRolesFromManagement, this::showWarBattle, this::showConvoyView);
        dynastyDialog.showDialog(DynastyManagementDialog.TAB_TRADE);
    }

    private void showDiplomacyDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || colony.getDynasty() == null) {
            return;
        }

        if (dynastyDialog != null && dynastyDialog.isTabOpen(DynastyManagementDialog.TAB_DIPLOMACY)) {
            dynastyDialog.dispose();
            return;
        }

        if (dynastyDialog != null && dynastyDialog.isShowing()) {
            dynastyDialog.setTab(DynastyManagementDialog.TAB_DIPLOMACY);
            dynastyDialog.requestFocus();
            return;
        }

        if (dynastyDialog != null) {
            dynastyDialog.dispose();
        }

        dynastyDialog = new DynastyManagementDialog(frame, colony.getDynasty(), engine, this::handleGoToColony,
                this::showWarRolesFromManagement, this::showWarBattle, this::showConvoyView);
        dynastyDialog.showDialog(DynastyManagementDialog.TAB_DIPLOMACY);
    }

    private void showSettingsDialog() {
        Engine engine = frame.getEngine();
        if (engine == null) return;

        if (settingsDialog != null && settingsDialog.isShowing()) {
            settingsDialog.dispose();
            return;
        }

        if (settingsDialog != null) {
            settingsDialog.dispose();
        }

        settingsDialog = new SettingsPanel.SettingsDialog(frame, engine);
        settingsDialog.showDialog();
    }

    private void handleGoToColony(Colony target) {
        Engine engine = frame.getEngine();
        if (engine == null || engine.getWorld() == null) return;
        
        World world = engine.getWorld();
        if (world.getHexes() != null) {
            for (Hex hex : world.getHexes()) {
                if (hex.getColony() == target) {
                    world.changeActiveHex(hex);
                    
                    if (gameAreaPanel != null) {
                        gameAreaPanel.setColony(target);
                        gameAreaPanel.resetView(); 
                    }
                    
                    refreshAllGUIData();
                    updateGameAreaSize();
                    centerOverworldScroll();

                    if (triggerManager != null) {
                    }
                    break;
                }
            }
        }
    }
    
    private Colony getColonyFromEngine(Engine engine) {
        return engine != null && engine.getWorld() != null && engine.getWorld().getActiveHex() != null ? engine.getWorld().getActiveHex().getColony() : null;
    }
    
    public boolean isEngineStarted() { return engineStarted; }

    // --- Cleanup Methods ---
    public boolean handleEscapeKey() {
        Engine engine = frame.getEngine();
        if (engine == null || !engine.isEscapeKeyGameActions() || !engineStarted) {
            return false;
        }
        if (closeVisibleDialogs()) {
            return true;
        }
        if (controlPanel != null) {
            controlPanel.toggleGameMenu();
            return true;
        }
        return false;
    }

    private boolean closeVisibleDialogs() {
        boolean closed = false;
        if (convoyDialog != null && convoyDialog.isShowing()) {
            convoyDialog.dispose();
            convoyDialog = null;
            closed = true;
        }
        if (warBattleDialog != null && warBattleDialog.isShowing()) {
            warBattleDialog.dispose();
            warBattleDialog = null;
            closed = true;
        }
        if (closed) {
            return true;
        }
        if (hatchDialog != null && hatchDialog.isShowing()) {
            hatchDialog.dispose();
            hatchDialog = null;
            closed = true;
        }
        if (roleDialog != null && roleDialog.isShowing()) {
            roleDialog.dispose();
            roleDialog = null;
            closed = true;
        }
        if (upgradeDialog != null && upgradeDialog.isShowing()) {
            upgradeDialog.dispose();
            upgradeDialog = null;
            closed = true;
        }
        if (abilitiesDialog != null && abilitiesDialog.isShowing()) {
            abilitiesDialog.dispose();
            abilitiesDialog = null;
            closed = true;
        }
        if (mapDialog != null && mapDialog.isShowing()) {
            mapDialog.dispose();
            mapDialog = null;
            closed = true;
        }
        if (statsDialog != null && statsDialog.isShowing()) {
            statsDialog.dispose();
            statsDialog = null;
            closed = true;
        }
        if (dynastyDialog != null && dynastyDialog.isShowing()) {
            dynastyDialog.dispose();
            dynastyDialog = null;
            closed = true;
        }
        if (settingsDialog != null && settingsDialog.isShowing()) {
            settingsDialog.dispose();
            settingsDialog = null;
            closed = true;
        }
        return closed;
    }

    private void disposeAllDialogs() {
        if (hatchDialog != null) { hatchDialog.dispose(); hatchDialog = null; }
        if (roleDialog != null) { roleDialog.dispose(); roleDialog = null; }
        if (upgradeDialog != null) { upgradeDialog.dispose(); upgradeDialog = null; }
        if (abilitiesDialog != null) { abilitiesDialog.dispose(); abilitiesDialog = null; }
        if (mapDialog != null) { mapDialog.dispose(); mapDialog = null; }
        if (statsDialog != null) { statsDialog.dispose(); statsDialog = null; }
        if (dynastyDialog != null) { dynastyDialog.dispose(); dynastyDialog = null; }
        if (warBattleDialog != null) { warBattleDialog.dispose(); warBattleDialog = null; }
        if (convoyDialog != null) { convoyDialog.dispose(); convoyDialog = null; }
        if (settingsDialog != null) { settingsDialog.dispose(); settingsDialog = null; }
    }

    public void endSession() {
        cleanupSession();
    }

    private void cleanupSession() {
        cancelLoadWorker();
        uninstallPlusSpeedKeyDispatcher();
        disposeAllDialogs();
        unregisterTickListeners();
        if (triggerManager != null) {
            triggerManager.unregisterListeners();
            triggerManager = null;
        }
        alertManager = null;
        lastPeaceOfferPromptWarId = -1;

        if (gameAreaPanel != null) {
            stopOverworldSpring();
            overworldPanDragging = false;
            overworldPanLastScreen = null;
            gameAreaPanel.resetView();
        }
        if (colonyPanel != null) colonyPanel.reset();
        if (worldPanel != null) worldPanel.reset();
        if (alertPanel != null) alertPanel.updateAlerts(new ArrayList<>());

        Engine eng = frame.getEngine();
        if (eng != null) {
            eng.pauseEngine();
            eng.setWorld(null);
        }
        updateStatusIndicator(true);
        if (controlPanel != null) {
            controlPanel.setPlayPauseButtonText(true);
        }

        this.engineStarted = false;
        frame.clearFocusPauseState();
        statusLabel.setText(LanguageStrings.get(LanguageStrings.UI_NOT_STARTED));
    }

    private void handleBackButton() {
        Engine engine = frame.getEngine();
        if (engine != null) {
            engine.pauseEngine();
        }

        World worldToSave = null;
        int slotId = 0;
        String saveName = null;
        SaveManager sm = engine != null ? engine.getSaveManager() : null;

        try {
            if (engine != null && engine.getWorld() != null) {
                World w = engine.getWorld();
                slotId = w.getSaveSlotId();
                if (slotId > 0 && sm != null) {
                    worldToSave = w;
                    Savefile existing = sm.loadSlot(slotId);
                    saveName = (existing != null && existing.getName() != null && !existing.getName().trim().isEmpty())
                            ? existing.getName()
                            : LanguageStrings.format(LanguageStrings.SAVE_DEFAULT_NAME_FMT, slotId);
                } else if (sm != null) {
                    sm.saveWorldToSlot(w, 0);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        cleanupSession();
        frame.showCard(MainFrame.CARD_SAVE);

        if (worldToSave != null && sm != null && engine != null) {
            World capturedWorld = worldToSave;
            int capturedSlot = slotId;
            String capturedName = saveName;
            sm.saveWorldToSlotUserAsync(capturedWorld, engine, capturedSlot, capturedName, success -> {
                if (!success) {
                    UiOptionPane.showMessageDialog(frame,
                            LanguageStrings.get(LanguageStrings.SAVE_ERROR_WRITE),
                            LanguageStrings.get(LanguageStrings.SAVE_ERROR_WRITE_TITLE),
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
    
    public void quitToMenuWithoutSaving() {
        cleanupSession();
        frame.showCard(MainFrame.CARD_SAVE);
    }

    public void enterWithSavefile(Savefile savefile) {
        cleanupSession();
        cancelLoadWorker();
        statusLabel.setText(LanguageStrings.get(LanguageStrings.UI_STARTING));
        Engine engine = frame.getEngine();
        
        JDialog loadingDialog = new JDialog(frame, LanguageStrings.get(LanguageStrings.UI_DIALOG_LOADING_TITLE), true);
        loadingDialog.setUndecorated(true);
        loadingDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(AssetStyles.BORDER_COLOR, AssetStyles.BORDER_THICKNESS_EXTERNAL));
        panel.setBackground(AssetStyles.BACKGROUND_DARK);

        JLabel label = new JLabel(LanguageStrings.get(LanguageStrings.UI_LOADING_WAIT), SwingConstants.CENTER);
        label.setForeground(AssetStyles.FONT_COLOR_BRIGHT);
        label.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        panel.add(label, BorderLayout.CENTER);
        loadingDialog.add(panel);
        UiDialogUtils.prepareDialog(loadingDialog, frame);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(frame);

        loadWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (isCancelled()) {
                    return null;
                }
                engine.startUp(savefile);
                if (isCancelled()) {
                    return null;
                }
                Colony colony = null;
                World world = engine.getWorld();
                if (world != null && world.getActiveHex() != null) {
                    colony = world.getActiveHex().getColony();
                }
                GameSpritePreloader.warmSession(colony, this::isCancelled);
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                if (isCancelled()) {
                    return;
                }
                try {
                    get();

                    World world = engine.getWorld();
                    Colony colony = null;
                    if (world != null && world.getActiveHex() != null) {
                        colony = world.getActiveHex().getColony();
                    }
                    if (colony == null || !colony.isPlayer()) {
                        cleanupSession();
                        UiOptionPane.showMessageDialog(frame,
                                LanguageStrings.get(LanguageStrings.UI_ERROR_LOADING),
                                LanguageStrings.get(LanguageStrings.UI_ERROR),
                                JOptionPane.ERROR_MESSAGE);
                        frame.showCard(MainFrame.CARD_SAVE);
                        return;
                    }

                    registerTickListeners();

                    gameAreaPanel.setColony(colony);

                    alertManager = new AlertManager(colony, alertPanel);

                    triggerManager = new TriggerManager(world, colony, engine);
                    if (frame instanceof TriggerManager.TriggerListener) {
                        triggerManager.addListener((TriggerManager.TriggerListener) frame);
                    }
                    triggerManager.registerListeners();

                    updateStaticWorldInfo();
                    if (!engineStarted) {
                        engineStarted = true;
                        if (!engine.isAlive()) {
                            engine.start();
                        }
                        installPlusSpeedKeyDispatcher();
                    }
                    engine.pauseEngine();
                    refreshAllGUIData();
                    updateGameAreaSize();
                    centerOverworldScroll();
                    noteOverworldUserPan();

                    controlPanel.setPlayPauseButtonText(engine.isPaused());
                    updateStatusIndicator(engine.isPaused());
                    paintGameAreaWhilePaused();
                } catch (Exception e) {
                    e.printStackTrace();
                    cleanupSession();
                    UiOptionPane.showMessageDialog(frame,
                            LanguageStrings.get(LanguageStrings.UI_ERROR_LOADING),
                            LanguageStrings.get(LanguageStrings.UI_ERROR),
                            JOptionPane.ERROR_MESSAGE);
                    frame.showCard(MainFrame.CARD_SAVE);
                }
            }
        };
        loadWorker.execute();
        loadingDialog.setVisible(true);
    }

    private void cancelLoadWorker() {
        if (loadWorker != null) {
            loadWorker.cancel(true);
            loadWorker = null;
        }
    }

    private void registerTickListeners() {
        unregisterTickListeners();
        Engine engine = frame.getEngine();
        if (engine == null) return;

        pendingMinuteGuiSteps.set(0);
        minuteTickListener = () -> pendingMinuteGuiSteps.incrementAndGet();
        hourTickListener = () -> SwingUtilities.invokeLater(this::updateHourGUI);
        dayTickListener = () -> SwingUtilities.invokeLater(this::updateDayGUI);
        monthTickListener = () -> SwingUtilities.invokeLater(this::updateMonthGUI);

        engine.addTickListener(minuteTickListener);
        engine.addHourTickListener(hourTickListener);
        engine.addDayTickListener(dayTickListener);
        engine.addMonthTickListener(monthTickListener);

        if (minuteDrainTimer == null) {
            minuteDrainTimer = new Timer(16, e -> drainPendingMinuteGuiSteps());
            minuteDrainTimer.setRepeats(true);
        }
        minuteDrainTimer.start();
        setupOverworldScrollbarPanTracking();
        startOverworldIdleRecenter();

        controlPanel.updateTickLabel(engine);
        updateStatusIndicator(engine.isPaused());
    }

    private void unregisterTickListeners() {
        Engine engine = frame.getEngine();
        if (engine != null) {
            if (minuteTickListener != null) engine.removeTickListener(minuteTickListener);
            if (hourTickListener != null) engine.removeHourTickListener(hourTickListener);
            if (dayTickListener != null) engine.removeDayTickListener(dayTickListener);
            if (monthTickListener != null) engine.removeMonthTickListener(monthTickListener);
        }
        minuteTickListener = null;
        hourTickListener = null;
        dayTickListener = null;
        monthTickListener = null;

        if (minuteDrainTimer != null) {
            minuteDrainTimer.stop();
        }
        stopOverworldIdleRecenter();
        removeOverworldScrollbarPanTracking();
        pendingMinuteGuiSteps.set(0);
    }

    private void drainPendingMinuteGuiSteps() {
        if (!engineStarted) {
            return;
        }
        Engine engine = frame.getEngine();
        if (engine == null || engine.isPaused()) {
            return;
        }
        int steps = pendingMinuteGuiSteps.getAndSet(0);
        if (steps <= 0) {
            return;
        }
        performMinuteGuiUpdate(steps);
    }

    public void updateStatusIndicator(boolean paused) {
        if (!engineStarted) {
            statusIndicator.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            statusLabel.setText(LanguageStrings.get(LanguageStrings.UI_NOT_STARTED));
            return;
        }
        if (paused) {
            statusIndicator.setBackground(AssetStyles.FONT_COLOR_WARNING);
            statusLabel.setText(LanguageStrings.get(LanguageStrings.UI_PAUSED_TICK));
        } else {
            statusIndicator.setBackground(AssetStyles.FONT_COLOR_SUCCESS);
            statusLabel.setText(LanguageStrings.get(LanguageStrings.UI_RUNNING));
        }
    }

    public void updateSpeedLabel() {
        if (controlPanel != null) {
            Engine eng = frame.getEngine();
            controlPanel.updateTickLabel(eng);
            if (eng != null) {
                controlPanel.setPlayPauseButtonText(eng.isPaused());
            }
        }
    }

    public void refreshAllGUIData() {
        updateMinuteGUI();
        updateHourGUI();
        updateDayGUI();
        updateMonthGUI();
    }

    private void updateStaticWorldInfo() {
        World world = frame.getEngine().getWorld();
        if (world == null) return;
        worldPanel.updateStaticData(world);
        if (world.getActiveHex() != null && world.getActiveHex().getBiome() != null) {
            gameAreaPanel.setBackgroundBiome(world.getActiveHex().getBiome());
        }
    }

    private void paintGameAreaWhilePaused() {
        if (gameScrollPane == null || gameAreaPanel == null) {
            return;
        }
        Rectangle viewportRect = gameScrollPane.getViewport().getViewRect();
        gameAreaPanel.setPaintViewportRect(viewportRect);
        gameAreaPanel.revalidate();
        gameAreaPanel.repaint();
        if (gameAreaPanel.getWidth() > 0 && gameAreaPanel.getHeight() > 0) {
            gameAreaPanel.paintImmediately(gameAreaPanel.getVisibleRect());
        }
    }

    private void updateMinuteGUI() {
        pendingMinuteGuiSteps.set(0);
        performMinuteGuiUpdate(1);
    }

    private void performMinuteGuiUpdate(int physicsSteps) {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        Colony colony = world != null && world.getActiveHex() != null ? world.getActiveHex().getColony() : null;

        gameAreaPanel.setColony(colony);
        if (alertManager != null && colony != null) {
            alertManager.setColony(colony);
        }
        if (colony != null && gameAreaPanel.getCurrentDimension() == WorldSpaces.UNDERWORLD) {
            updateGameAreaSize();
        }
        updateStaticWorldInfo();

        if (world == null) {
            return;
        }

        Rectangle viewportRect = gameScrollPane.getViewport().getViewRect();
        gameAreaPanel.setPaintViewportRect(viewportRect);

        if (colony != null) {
            int w = gameAreaPanel.getWidth();
            int h = gameAreaPanel.getHeight();
            if (w > 1 && h > 1) {
                if (gameAreaPanel.getCurrentDimension() == WorldSpaces.OVERWORLD) {
                    colony.setGameAreaDimensions(viewportRect.width, viewportRect.height);
                } else {
                    colony.setGameAreaDimensions(w, h);
                }
            }
            Rectangle lodRect = gameAreaPanel.getLodViewportRect();
            for (int i = 0; i < physicsSteps; i++) {
                colony.runPhysics(gameAreaPanel.getCurrentDimension(), lodRect);
            }
        }

        worldPanel.updateMinuteData(world);
        colonyPanel.updateMinuteData(colony);
        gameAreaPanel.repaint();
    }

    private void updateHourGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        Colony colony = world != null && world.getActiveHex() != null ? world.getActiveHex().getColony() : null;
        if (world == null) return;

        worldPanel.updateHourData(world);
        colonyPanel.updateHourData(colony);
        
        if (colony != null && colony.isPlayer()) {
            updateGameAreaSize();
        }

        if (colony != null && colony.isPlayer()) {
            if (upgradeDialog != null && upgradeDialog.isShowing()) {
                upgradeDialog.liveUpdate();
            }
            if (abilitiesDialog != null && abilitiesDialog.isShowing()) {
                abilitiesDialog.liveUpdate();
            }
            if (statsDialog != null && statsDialog.isShowing()) {
                statsDialog.liveUpdate();
            }
            if (dynastyDialog != null && dynastyDialog.isShowing()) {
                dynastyDialog.liveUpdate();
            }
            if (warBattleDialog != null && warBattleDialog.isShowing()) {
                warBattleDialog.liveUpdate();
            }
            if (convoyDialog != null && convoyDialog.isShowing()) {
                convoyDialog.liveUpdate();
            }
            if (controlPanel != null) {
                controlPanel.updateResearchMenu(colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH));
                controlPanel.updateBuildMenu(colony.hasUpgrade(GameUnlocks.ABILITY_BUILD));
                controlPanel.updateAssimilationMenu(colony.hasUpgrade(GameUnlocks.ABILITY_ASSIMILATION));
                controlPanel.updateSynergyMenu(colony.hasUpgrade(GameUnlocks.ABILITY_SYNERGY));
                controlPanel.updateAbilitiesMenu(colony.hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT));
                controlPanel.updateDynastyMenu(colony.hasUpgrade(GameUnlocks.ABILITY_DYNASTY));
                controlPanel.updateTradeMenu(colony.hasUpgrade(GameUnlocks.ABILITY_TRADE));
                controlPanel.updateWarsMenu(colony.hasUpgrade(GameUnlocks.ABILITY_DYNASTY));
                controlPanel.updateAuditMenu(frame.getEngine().isShowAuditMenu());
            }
        } else {
             if (controlPanel != null) {
                controlPanel.updateResearchMenu(false);
                controlPanel.updateBuildMenu(false);
                controlPanel.updateAssimilationMenu(false);
                controlPanel.updateSynergyMenu(false);
                controlPanel.updateAbilitiesMenu(false);
                controlPanel.updateDynastyMenu(false);
                controlPanel.updateTradeMenu(false);
                controlPanel.updateWarsMenu(false);
                controlPanel.updateAuditMenu(frame.getEngine().isShowAuditMenu());
            }
        }

        flushPlayerWarAlerts(world);
    }

    private void flushPlayerWarAlerts(World world) {
        if (world == null || alertManager == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null) {
            return;
        }
        for (Colony playerColony : playerDynasty.getColonies()) {
            for (String msg : playerColony.consumeEventsWithPrefix(ColonyLogPrefixes.WAR)) {
                alertManager.ingestLogEvent(msg);
            }
        }
        alertManager.checkStatus();
    }

    private void updateDayGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        Colony colony = world != null && world.getActiveHex() != null ? world.getActiveHex().getColony() : null;
        if (world == null) return;

        worldPanel.updateDayData(world);
        colonyPanel.updateDayData(colony);

        processPendingDiplomacyNotifications(world);
        processPendingNpcWarAlerts(world);

        if (alertManager != null) {
            TradeManager tradeManager = engine != null ? engine.getTradeManager() : null;
            alertManager.checkRebellionRisk(world, tradeManager);
            alertManager.checkStatus();
        }

        if (roleDialog != null && roleDialog.isShowing()) {
            roleDialog.liveUpdate();
        }
    }

    private void processPendingDiplomacyNotifications(World world) {
        processPendingRebellionResponse(world);
        processPendingPactRequests(world);
        processPendingWarDeclarations(world);
        processPendingIntegrationVassalWarAlerts(world);
        processPendingIntegrationCompletedAlerts(world);
        processPendingWarStageResultAlerts(world);
        processPendingPeaceOffers(world);
        processPendingTradeProposals(world);
    }

    private void processPendingNpcWarAlerts(World world) {
        if (world == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null) {
            return;
        }

        Colony alertColony = playerDynasty.getCapital();
        if (alertColony == null && !playerDynasty.getColonies().isEmpty()) {
            alertColony = playerDynasty.getColonies().get(0);
        }
        if (alertColony == null) {
            return;
        }

        for (Dynasty.PendingNpcWarAlert pending : playerDynasty.copyPendingNpcWarAlerts()) {
            playerDynasty.removePendingNpcWarAlert(pending.attackerId, pending.defenderId);
            Dynasty attacker = world.findDynastyById(pending.attackerId);
            Dynasty defender = world.findDynastyById(pending.defenderId);
            if (attacker == null || defender == null || attacker.isDefeated() || defender.isDefeated()) {
                continue;
            }
            alertColony.logEvent(ColonyLogPrefixes.WAR + " "
                    + LanguageStrings.format(
                            LanguageStrings.DIPLO_NPC_WAR_ALERT_FMT, attacker.getName(), defender.getName()));
        }
    }

    private void updateMonthGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        if (world == null) return;
        worldPanel.updateMonthData(world);
    }

    private void processPendingRebellionResponse(World world) {
        if (world == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null) {
            return;
        }
        int rebellionId = playerDynasty.getPendingRebellionResponseFromId();
        if (rebellionId <= 0) {
            return;
        }
        Dynasty rebellion = world.findDynastyById(rebellionId);
        if (rebellion == null || rebellion.isDefeated()) {
            playerDynasty.clearPendingRebellionResponse();
            return;
        }

        String message = LanguageStrings.format(
                LanguageStrings.REBELLION_RESPONSE_MSG_FMT,
                rebellion.getName(),
                AssetStyles.formatNumber(rebellion.getColonies().size()));
        String[] options = {
                LanguageStrings.get(LanguageStrings.REBELLION_ACTION_FIGHT),
                LanguageStrings.get(LanguageStrings.REBELLION_ACTION_INDEPENDENCE)
        };
        int choice = UiOptionPane.showOptionDialog(this, message,
                LanguageStrings.get(LanguageStrings.REBELLION_RESPONSE_TITLE),
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[1]);

        TradeManager tradeManager = frame.getEngine() != null ? frame.getEngine().getTradeManager() : null;
        boolean fight = choice == JOptionPane.YES_OPTION || choice == 0;
        DynastyRebellionService.respondToRebellion(world, tradeManager, playerDynasty, rebellion, fight);
    }

    private void processPendingPactRequests(World world) {
        if (world == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null || playerDynasty.getDiplomacyService() == null) {
            return;
        }

        List<Integer> pending = playerDynasty.copyPendingPactRequestFromIds();
        if (pending.isEmpty()) {
            return;
        }

        int fromId = pending.get(0);
        Dynasty requester = world.findDynastyById(fromId);
        if (requester == null || requester.isDefeated()) {
            playerDynasty.removePendingPactRequest(fromId);
            return;
        }

        PactRequestIncomingPolicy policy = playerDynasty.getPactRequestIncomingPolicy();
        if (policy == PactRequestIncomingPolicy.AUTO_ACCEPT) {
            playerDynasty.getDiplomacyService().acceptNonAggressionPact(requester, world);
            return;
        }
        if (policy == PactRequestIncomingPolicy.AUTO_DECLINE) {
            playerDynasty.getDiplomacyService().declineNonAggressionPact(requester, world);
            return;
        }

        String message = LanguageStrings.format(
                LanguageStrings.DIPLO_PACT_REQUEST_MSG_FMT, requester.getName());
        String[] options = {
                LanguageStrings.get(LanguageStrings.DIPLO_PACT_REQUEST_ACCEPT),
                LanguageStrings.get(LanguageStrings.DIPLO_PACT_REQUEST_DECLINE)
        };
        playerDynasty.setPactRequestPromptOpen(true);
        try {
            int choice = UiOptionPane.showOptionDialog(this, message,
                    LanguageStrings.get(LanguageStrings.DIPLO_PACT_REQUEST_TITLE),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            playerDynasty.removePendingPactRequest(fromId);
            if (choice == JOptionPane.YES_OPTION || choice == 0) {
                playerDynasty.getDiplomacyService().acceptNonAggressionPact(requester, world);
            } else {
                playerDynasty.getDiplomacyService().declineNonAggressionPact(requester, world);
            }
        } finally {
            playerDynasty.setPactRequestPromptOpen(false);
        }
    }

    private void processPendingPeaceOffers(World world) {
        if (world == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null) {
            return;
        }

        WarService warService = world.getWarService();
        for (War war : warService.getWarsForDynasty(playerDynasty.getId())) {
            int offererId = war.getPendingPeaceOfferFromDynastyId();
            if (offererId <= 0) {
                continue;
            }
            if (warService.getStandingForDynasty(war, playerDynasty) != WarStanding.LOSING) {
                continue;
            }
            if (lastPeaceOfferPromptWarId == war.getId()) {
                continue;
            }
            Dynasty offerer = world.findDynastyById(offererId);
            if (offerer == null || offerer.isDefeated()) {
                war.clearPendingPeaceOffer();
                continue;
            }
            UiOptionPane.showMessageDialog(this,
                    LanguageStrings.format(LanguageStrings.WAR_PEACE_OFFERED_MSG_FMT,
                            offerer.getName(), warService.formatWarNameForDisplay(war, playerDynasty)),
                    LanguageStrings.get(LanguageStrings.WAR_PEACE_OFFERED_TITLE),
                    JOptionPane.QUESTION_MESSAGE);
            lastPeaceOfferPromptWarId = war.getId();
            showWarDialog();
            return;
        }
    }

    private void processPendingIntegrationVassalWarAlerts(World world) {
        if (world == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null) {
            return;
        }

        Colony alertColony = playerDynasty.getCapital();
        if (alertColony == null && !playerDynasty.getColonies().isEmpty()) {
            alertColony = playerDynasty.getColonies().get(0);
        }
        if (alertColony == null) {
            return;
        }

        for (Dynasty.PendingIntegrationVassalWarAlert pending : playerDynasty.copyPendingIntegrationVassalWarAlerts()) {
            playerDynasty.removePendingIntegrationVassalWarAlert(pending.attackerId, pending.vassalId);
            Dynasty attacker = world.findDynastyById(pending.attackerId);
            Dynasty vassal = world.findDynastyById(pending.vassalId);
            if (attacker == null || vassal == null || attacker.isDefeated() || vassal.isDefeated()) {
                continue;
            }
            String message = LanguageStrings.format(
                    LanguageStrings.DIPLO_INTEGRATION_VASSAL_WAR_ALERT_FMT,
                    attacker.getName(),
                    vassal.getName());
            alertColony.logEvent(ColonyLogPrefixes.WAR + " " + message);
            UiOptionPane.showMessageDialog(this,
                    message,
                    LanguageStrings.get(LanguageStrings.DIPLO_INTEGRATION_VASSAL_WAR_ALERT_TITLE),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void processPendingWarStageResultAlerts(World world) {
        if (world == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null) {
            return;
        }

        for (Dynasty.PendingWarStageResultAlert pending : playerDynasty.copyPendingWarStageResultAlerts()) {
            playerDynasty.removePendingWarStageResultAlert(pending);
            UiOptionPane.showMessageDialog(this,
                    pending.message,
                    pending.title,
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void processPendingIntegrationCompletedAlerts(World world) {
        if (world == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null) {
            return;
        }

        for (int targetId : playerDynasty.copyPendingIntegrationCompletedTargetIds()) {
            playerDynasty.removePendingIntegrationCompletedAlert(targetId);
            Dynasty integrated = world.findDynastyById(targetId);
            String targetName = integrated != null
                    ? integrated.getName()
                    : LanguageStrings.get(LanguageStrings.STAT_UNKNOWN);
            UiOptionPane.showMessageDialog(this,
                    LanguageStrings.format(LanguageStrings.DIPLO_INTEGRATION_COMPLETED_MSG_FMT, targetName),
                    LanguageStrings.get(LanguageStrings.DIPLO_INTEGRATION_COMPLETED_TITLE),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void processPendingWarDeclarations(World world) {
        if (world == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null) {
            return;
        }

        while (true) {
            List<Integer> pending = playerDynasty.copyPendingWarDeclarationFromIds();
            if (pending.isEmpty()) {
                return;
            }

            int fromId = pending.get(0);
            Dynasty attacker = world.findDynastyById(fromId);
            playerDynasty.removePendingWarDeclarationFrom(fromId);
            if (attacker == null || attacker.isDefeated()) {
                continue;
            }

            UiOptionPane.showMessageDialog(this,
                    LanguageStrings.format(LanguageStrings.DIPLO_WAR_DECLARED_MSG_FMT, attacker.getName()),
                    LanguageStrings.get(LanguageStrings.DIPLO_WAR_DECLARED_TITLE),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void processPendingTradeProposals(World world) {
        if (world == null || !engineStarted) {
            return;
        }
        Dynasty playerDynasty = null;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer() && !dynasty.isDefeated()) {
                playerDynasty = dynasty;
                break;
            }
        }
        if (playerDynasty == null || playerDynasty.getDiplomacyService() == null) {
            return;
        }

        List<CrossDynastyTradeProposal> pending = playerDynasty.copyPendingTradeProposals();
        if (pending.isEmpty()) {
            return;
        }

        CrossDynastyTradeProposal proposal = pending.get(0);
        Dynasty requester = world.findDynastyById(proposal.getFromDynastyId());
        if (requester == null || requester.isDefeated()) {
            playerDynasty.removePendingTradeProposal(proposal);
            return;
        }

        Colony origin = findColonyById(world, proposal.getOriginColonyId());
        Colony destination = findColonyById(world, proposal.getDestinationColonyId());
        if (origin == null || destination == null) {
            playerDynasty.removePendingTradeProposal(proposal);
            return;
        }

        String loadSummary = playerDynasty.getDiplomacyService().formatProposalLoadSummary(proposal);
        String messageKey = proposal.getKind() == CrossDynastyTradeProposal.Kind.OFFER
                ? LanguageStrings.DIPLO_TRADE_OFFER_MSG_FMT
                : LanguageStrings.DIPLO_TRADE_REQUEST_MSG_FMT;
        String message = LanguageStrings.format(messageKey,
                requester.getName(),
                origin.getName(),
                destination.getName(),
                loadSummary);
        String[] options = {
                LanguageStrings.get(LanguageStrings.DIPLO_TRADE_PROPOSAL_ACCEPT),
                LanguageStrings.get(LanguageStrings.DIPLO_TRADE_PROPOSAL_DECLINE)
        };
        int choice = UiOptionPane.showOptionDialog(this, message,
                LanguageStrings.get(LanguageStrings.DIPLO_TRADE_PROPOSAL_TITLE),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == JOptionPane.YES_OPTION || choice == 0) {
            Engine engine = frame.getEngine();
            TradeManager tradeManager = engine != null ? engine.getTradeManager() : null;
            playerDynasty.getDiplomacyService().acceptTradeProposal(requester, proposal, world, tradeManager);
        } else {
            playerDynasty.getDiplomacyService().declineTradeProposal(requester, proposal, world);
        }
    }

    private Colony findColonyById(World world, int colonyId) {
        for (Dynasty dynasty : world.getDynastys()) {
            for (Colony colony : dynasty.getColonies()) {
                if (colony.getId() == colonyId) {
                    return colony;
                }
            }
        }
        return null;
    }
}
