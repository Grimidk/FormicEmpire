package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.constants.dynasty.WarStagePhase;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.AntModService;
import com.grimidk.formicempire.classes.entities.services.shared.BattleNumericFeed;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleScene;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleSceneBuilder;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleState;
import com.grimidk.formicempire.classes.entities.services.world.WarCreatureCombatService;
import com.grimidk.formicempire.classes.entities.services.world.WarService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RouteViewVisuals;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WarBattleViewPanel extends JPanel {

    private static final int HEADER_HEIGHT = 0;
    private static final int FIELD_MARGIN = 0;
    private static final int ANT_SPRITE_SCALE = 2;
    private static final int ANIMATION_FRAME_MS = 50;
    private static final float WOBBLE_SPEED = 5.5f;
    private static final float WOBBLE_AMPLITUDE_PX = 2f;
    private static final int LINE_JITTER_PX = 10;
    private static final int RESERVE_WALK_PX_PER_SEC = 28;
    private static final float AIR_SUPPORT_FLY_CYCLE_SEC = 14.0f;
    private static final float AIR_SUPPORT_FLY_PORTION = 0.36f;

    private final War war;
    private final Engine engine;
    private final Runnable onBattleEnded;
    private final JPanel headerPanel = new JPanel(new BorderLayout(12, 4));
    private final JPanel attackerHeaderPanel = new JPanel();
    private final JPanel defenderHeaderPanel = new JPanel();
    private final JPanel centerHeaderPanel = new JPanel(new BorderLayout(0, 2));
    private final JPanel phaseIndicatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
    private final JLabel stagePhaseLabel = new JLabel();
    private final JLabel infantryPhaseIcon = new JLabel();
    private final JLabel artilleryPhaseIcon = new JLabel();
    private final JLabel airSupportPhaseIcon = new JLabel();
    private int lastObservedBattleTick = -1;
    private final JPanel diceTrayPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!diceExpanded) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                numericOverlay.paint(g2d, getWidth(), BattleNumericOverlay.COLLAPSED_VISIBLE_ROWS);
                g2d.dispose();
            }
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            return event != null ? numericOverlay.getTooltipAt(event.getPoint()) : null;
        }
    };
    private boolean diceExpanded;
    private final JPanel battleProgressBar = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            float ratio = Math.max(0f, Math.min(1f, targetContactLineRatio));
            int splitX = Math.round(w * ratio);
            g.setColor(new Color(95, 185, 255));
            g.fillRect(0, 0, splitX, h);
            g.setColor(new Color(255, 135, 95));
            g.fillRect(splitX, 0, w - splitX, h);
        }
    };
    private final Map<Integer, Image> biomeTileCache = new HashMap<>();
    private final Image riverSprite = UiResourceLoader.loadImage(WarBattleViewPanel.class, "/sprites/buildings/River.png");
    private final boolean riverFlipX = GameRandom.nextBoolean();
    private final boolean riverFlipY = GameRandom.nextBoolean();
    private final BattleNumericOverlay numericOverlay = new BattleNumericOverlay();
    private WarBattleScene scene;
    private List<BattleAnt> attackerAnts = List.of();
    private List<BattleAnt> defenderAnts = List.of();
    private float contactLineRatio = 0.5f;
    private float targetContactLineRatio = 0.5f;
    private Timer animationTimer;
    private float animationSeconds;
    private float sceneRefreshAccumSec;
    private float stageBannerSecondsRemaining;
    private WarStagePhase lastObservedPhase;
    private WarStagePhase stageBannerPhase;
    private long lastTickNanos;
    private String lastAttackerPoolSig = "";
    private String lastDefenderPoolSig = "";
    private String lastHeaderSig = "";

    public WarBattleViewPanel(War war, Engine engine) {
        this(war, engine, null);
    }

    public WarBattleViewPanel(War war, Engine engine, Runnable onBattleEnded) {
        super(new BorderLayout());
        this.war = war;
        this.engine = engine;
        this.onBattleEnded = onBattleEnded;
        setOpaque(true);
        setBackground(AssetStyles.BACKGROUND_COLOR);

        headerPanel.setOpaque(true);
        headerPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 4, 12));

        attackerHeaderPanel.setLayout(new BoxLayout(attackerHeaderPanel, BoxLayout.Y_AXIS));
        attackerHeaderPanel.setOpaque(false);
        defenderHeaderPanel.setLayout(new BoxLayout(defenderHeaderPanel, BoxLayout.Y_AXIS));
        defenderHeaderPanel.setOpaque(false);

        centerHeaderPanel.setOpaque(false);
        phaseIndicatorPanel.setOpaque(false);
        stagePhaseLabel.setFont(AssetStyles.FONT_BOLD);
        stagePhaseLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        stagePhaseLabel.setIconTextGap(5);
        stagePhaseLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        configurePhaseIconLabel(infantryPhaseIcon, GameConstants.BATTLE_LINE_INFANTRY);
        configurePhaseIconLabel(artilleryPhaseIcon, GameConstants.BATTLE_LINE_ARTILLERY);
        configurePhaseIconLabel(airSupportPhaseIcon, GameConstants.BATTLE_LINE_AIR_SUPPORT);
        phaseIndicatorPanel.add(stagePhaseLabel);
        phaseIndicatorPanel.add(infantryPhaseIcon);
        phaseIndicatorPanel.add(createPhaseArrowLabel());
        phaseIndicatorPanel.add(artilleryPhaseIcon);
        phaseIndicatorPanel.add(createPhaseArrowLabel());
        phaseIndicatorPanel.add(airSupportPhaseIcon);

        diceTrayPanel.setOpaque(false);
        diceTrayPanel.setPreferredSize(new Dimension(320, BattleNumericOverlay.COLLAPSED_HEIGHT));
        ToolTipManager.sharedInstance().registerComponent(diceTrayPanel);
        ToolTipManager.sharedInstance().registerComponent(this);
        installDiceHoverListeners();

        centerHeaderPanel.add(phaseIndicatorPanel, BorderLayout.NORTH);
        centerHeaderPanel.add(diceTrayPanel, BorderLayout.CENTER);

        battleProgressBar.setOpaque(false);
        battleProgressBar.setPreferredSize(new Dimension(100, 6));

        headerPanel.add(attackerHeaderPanel, BorderLayout.WEST);
        headerPanel.add(centerHeaderPanel, BorderLayout.CENTER);
        headerPanel.add(defenderHeaderPanel, BorderLayout.EAST);
        headerPanel.add(battleProgressBar, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        loadBiomeTiles();
        refreshScene();
        startAnimation();
    }

    public void refreshScene() {
        World world = engine != null ? engine.getWorld() : null;
        WarService warService = world != null ? world.getWarService() : null;
        scene = WarBattleSceneBuilder.build(world, war, warService);
        boolean activeBattle = war != null && war.isActive() && WarCreatureCombatService.getState(war) != null;
        if (activeBattle && scene.isAvailable()) {
            WarStagePhase currentPhase = scene.getPhase();
            if (lastObservedPhase != null && currentPhase != null && currentPhase != lastObservedPhase) {
                stageBannerPhase = currentPhase;
                stageBannerSecondsRemaining = 2.8f;
            }
            if (currentPhase != null) {
                lastObservedPhase = currentPhase;
            }
            targetContactLineRatio = scene.getFrontlineRatio();
            String atkSig = sideSignature(scene.getAttacker());
            if (!atkSig.equals(lastAttackerPoolSig)) {
                lastAttackerPoolSig = atkSig;
                attackerAnts = buildAntPool(scene.getAttacker(), true);
            }
            String defSig = sideSignature(scene.getDefender());
            if (!defSig.equals(lastDefenderPoolSig)) {
                lastDefenderPoolSig = defSig;
                defenderAnts = buildAntPool(scene.getDefender(), false);
            }
        } else {
            lastAttackerPoolSig = "";
            lastDefenderPoolSig = "";
            attackerAnts = List.of();
            defenderAnts = List.of();
            updateSideHeaders();
            battleProgressBar.repaint();
            repaint();
            if (!activeBattle && onBattleEnded != null) {
                stopAnimation();
                SwingUtilities.invokeLater(onBattleEnded);
            }
            return;
        }
        updateSideHeaders();
        battleProgressBar.repaint();
        repaint();
    }

    private void updateSideHeaders() {
        WarBattleScene.Side atk = scene != null ? scene.getAttacker() : null;
        WarBattleScene.Side def = scene != null ? scene.getDefender() : null;
        String headerSig = sideSignature(atk) + "||" + sideSignature(def);
        if (!headerSig.equals(lastHeaderSig)) {
            lastHeaderSig = headerSig;
            populateSideHeader(attackerHeaderPanel, atk, true);
            populateSideHeader(defenderHeaderPanel, def, false);
            headerPanel.revalidate();
        }
        updatePhaseIndicator();
        headerPanel.repaint();
    }

    private static String sideSignature(WarBattleScene.Side side) {
        if (side == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(side.dynastyId()).append(':').append(side.dynastyName()).append(':')
                .append(side.livingArmy()).append(':').append(side.livingActive()).append('|');
        for (BattleLine line : GameConstants.getBattleLines()) {
            sb.append(line.getId()).append('=').append(countAntsInLine(side.activeByLine(), line))
                    .append('/').append(countAntsInLine(side.reserveByLine(), line)).append(';');
        }
        return sb.toString();
    }

    private static void configurePhaseIconLabel(JLabel label, BattleLine line) {
        if (line != null) {
            label.setIcon(line.getIcon());
            label.setToolTipText(line.getName());
        }
        label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        label.setOpaque(false);
    }

    private static JLabel createPhaseArrowLabel() {
        JLabel arrow = new JLabel(">");
        arrow.setFont(AssetStyles.FONT_SMALL);
        arrow.setForeground(AssetStyles.FONT_COLOR);
        return arrow;
    }

    private void updatePhaseIndicator() {
        WarStagePhase stagePhase = scene != null ? scene.getPhase() : GameConstants.WAR_STAGE_ACTIVE_CLASH;
        stagePhaseLabel.setText(formatPhase(stagePhase));
        ImageIcon stageIcon = stagePhase != null ? stagePhase.getIcon() : null;
        if (stageIcon != null && stageIcon.getImage() != null) {
            stagePhaseLabel.setIcon(new ImageIcon(stageIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        } else {
            stagePhaseLabel.setIcon(null);
        }

        WarBattleState battleState = WarCreatureCombatService.getState(war);
        if (battleState == null) {
            phaseIndicatorPanel.setVisible(scene != null && scene.isAvailable());
            return;
        }
        BattleLine activeLine = battleState.getCurrentPhaseLine();
        if (activeLine == null) {
            activeLine = GameConstants.BATTLE_LINE_INFANTRY;
        }
        boolean attackerPhase = battleState.isCurrentPhaseAttacker();
        Color turnColor = attackerPhase ? new Color(95, 185, 255) : new Color(255, 135, 95);
        applyPhaseIconHighlight(infantryPhaseIcon, activeLine == GameConstants.BATTLE_LINE_INFANTRY, turnColor);
        applyPhaseIconHighlight(artilleryPhaseIcon, activeLine == GameConstants.BATTLE_LINE_ARTILLERY, turnColor);
        applyPhaseIconHighlight(airSupportPhaseIcon, activeLine == GameConstants.BATTLE_LINE_AIR_SUPPORT, turnColor);
        phaseIndicatorPanel.setVisible(true);
    }

    private static void applyPhaseIconHighlight(JLabel label, boolean active, Color turnColor) {
        if (active) {
            label.setOpaque(true);
            label.setBackground(new Color(turnColor.getRed(), turnColor.getGreen(), turnColor.getBlue(), 55));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(turnColor, 2, true),
                    BorderFactory.createEmptyBorder(1, 3, 1, 3)));
        } else {
            label.setOpaque(false);
            label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        }
    }

    private static void populateSideHeader(JPanel container, WarBattleScene.Side side, boolean leftAligned) {
        container.removeAll();
        if (side == null) {
            return;
        }
        JPanel topRow = new JPanel(new FlowLayout(leftAligned ? FlowLayout.LEFT : FlowLayout.RIGHT, 6, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(leftAligned ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);

        ImageIcon speciesIcon = side.species() != null ? side.species().getIcon() : null;
        JLabel nameLabel = new JLabel(side.dynastyName() != null ? side.dynastyName() : "");
        nameLabel.setFont(AssetStyles.FONT_BOLD);
        nameLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        if (speciesIcon != null) {
            if (leftAligned) {
                nameLabel.setIcon(speciesIcon);
                nameLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            } else {
                nameLabel.setIcon(speciesIcon);
                nameLabel.setHorizontalTextPosition(SwingConstants.LEFT);
            }
            nameLabel.setIconTextGap(6);
        }
        topRow.add(nameLabel);
        container.add(topRow);

        JPanel linesRow = new JPanel(new FlowLayout(leftAligned ? FlowLayout.LEFT : FlowLayout.RIGHT, 8, 2));
        linesRow.setOpaque(false);
        linesRow.setAlignmentX(leftAligned ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);
        for (BattleLine line : GameConstants.getBattleLines()) {
            int count = countAntsInLine(side.activeByLine(), line);
            JLabel lineLabel = new JLabel(AssetStyles.formatNumber(count));
            lineLabel.setFont(AssetStyles.FONT_SMALL);
            lineLabel.setForeground(AssetStyles.FONT_COLOR);
            if (line.getIcon() != null) {
                lineLabel.setIcon(line.getIcon());
                lineLabel.setIconTextGap(3);
            }
            linesRow.add(lineLabel);
        }
        container.add(linesRow);
    }

    private static int countAntsInLine(Map<BattleLine, Map<AntClass, Integer>> byLine, BattleLine line) {
        if (byLine == null || line == null) {
            return 0;
        }
        Map<AntClass, Integer> map = byLine.get(line);
        if (map == null || map.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Integer c : map.values()) {
            if (c != null && c > 0) {
                total += c;
            }
        }
        return total;
    }

    public void startAnimation() {
        if (animationTimer != null) {
            return;
        }
        lastTickNanos = System.nanoTime();
        int intervalMs = engine != null ? engine.getVisualFrameIntervalMs() : ANIMATION_FRAME_MS;
        animationTimer = new Timer(intervalMs, e -> {
            long now = System.nanoTime();
            float deltaSec = (now - lastTickNanos) / 1_000_000_000f;
            lastTickNanos = now;
            if (deltaSec <= 0f || deltaSec > 0.25f) {
                int fallbackMs = animationTimer != null ? animationTimer.getDelay() : intervalMs;
                deltaSec = Math.max(1, fallbackMs) / 1000f;
            }
            animationSeconds += deltaSec;
            if (animationSeconds > 10_000f) {
                animationSeconds = 0f;
            }
            if (stageBannerSecondsRemaining > 0f) {
                stageBannerSecondsRemaining = Math.max(0f, stageBannerSecondsRemaining - deltaSec);
            }
            sceneRefreshAccumSec += deltaSec;
            if (sceneRefreshAccumSec >= 0.35f) {
                sceneRefreshAccumSec = 0f;
                refreshScene();
            }
            float delta = targetContactLineRatio - contactLineRatio;
            if (Math.abs(delta) > 0.0005f) {
                float step = 1f - (float) Math.exp(-1.237f * deltaSec);
                contactLineRatio += delta * step;
            } else {
                contactLineRatio = targetContactLineRatio;
            }
            WarBattleState battleState = WarCreatureCombatService.getState(war);
            BattleNumericFeed feed = battleState != null ? battleState.getNumericFeed() : null;
            int stateTickIndex = battleState != null ? battleState.getTickIndex() : -1;
            if (stateTickIndex != lastObservedBattleTick) {
                lastObservedBattleTick = stateTickIndex;
                updatePhaseIndicator();
            }
            World w = engine != null ? engine.getWorld() : null;
            int minute = w != null ? w.getMinute() : 0;
            numericOverlay.update(feed, deltaSec, minute, stateTickIndex);
            diceTrayPanel.repaint();
            repaint();
        });
        animationTimer.setCoalesce(true);
        animationTimer.start();
    }

    public void applyVisualFrameRate() {
        if (animationTimer != null && engine != null) {
            animationTimer.setDelay(engine.getVisualFrameIntervalMs());
        }
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

    @Override
    public void removeNotify() {
        stopAnimation();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (scene == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int fieldY = headerPanel.getHeight();
        int fieldH = Math.max(120, getHeight() - fieldY);
        int fieldW = getWidth();
        int fieldX = FIELD_MARGIN;

        if (!scene.isAvailable()) {
            g2d.setColor(AssetStyles.FONT_COLOR);
            g2d.setFont(AssetStyles.FONT_NORMAL);
            String msg = LanguageStrings.get(LanguageStrings.BATTLE_NOT_AVAILABLE);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, fieldY + fieldH / 2);
            g2d.dispose();
            return;
        }

        int contactLineX = fieldX + Math.round(fieldW * contactLineRatio);
        if (scene.isDefenseOnlyBiome()) {
            drawFullBiomeField(g2d, fieldX, fieldY, fieldW, fieldH, scene.getDefenderBiome());
        } else {
            int biomeSplitX = fieldX + (fieldW / 2);
            drawBattlefieldBiomes(g2d, fieldX, fieldY, fieldW, fieldH, biomeSplitX,
                    scene.getAttackerBiome(), scene.getDefenderBiome());
        }
        if (scene.isRiverCrossing()) {
            drawCenterRiver(g2d, fieldX, fieldY, fieldW, fieldH);
        }

        Rectangle field = new Rectangle(fieldX, fieldY, fieldW, fieldH);
        drawAntColumn(g2d, attackerAnts, field, contactLineX, true);
        drawAntColumn(g2d, defenderAnts, field, contactLineX, false);
        drawStageTransitionBanner(g2d, fieldX, fieldY, fieldW);

        g2d.dispose();
    }

    private void drawStageTransitionBanner(Graphics2D g2d, int fieldX, int fieldY, int fieldW) {
        if (stageBannerSecondsRemaining <= 0f || stageBannerPhase == null) {
            return;
        }
        float alpha = Math.min(1f, stageBannerSecondsRemaining / 0.6f);
        Composite oldComp = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        String text = formatPhase(stageBannerPhase);
        ImageIcon icon = stageBannerPhase.getIcon();
        Image iconImg = (icon != null) ? icon.getImage() : null;
        int iconSize = (iconImg != null) ? 20 : 0;
        int iconGap = (iconImg != null) ? 8 : 0;

        g2d.setFont(AssetStyles.FONT_BOLD);
        FontMetrics fm = g2d.getFontMetrics();
        int padX = 18;
        int padY = 8;
        int contentW = iconSize + iconGap + fm.stringWidth(text);
        int boxW = contentW + padX * 2;
        int boxH = Math.max(iconSize, fm.getHeight()) + padY * 2;
        int boxX = fieldX + (fieldW - boxW) / 2;
        int boxY = fieldY + 18;

        Color bg = AssetStyles.BACKGROUND_SECONDARY;
        g2d.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 230));
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);
        g2d.setColor(AssetStyles.FONT_COLOR_HEADER);
        g2d.drawRoundRect(boxX, boxY, boxW, boxH, 12, 12);

        int curX = boxX + padX;
        if (iconImg != null) {
            int iconY = boxY + (boxH - iconSize) / 2;
            g2d.drawImage(iconImg, curX, iconY, iconSize, iconSize, this);
            curX += iconSize + iconGap;
        }
        int textY = boxY + ((boxH - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, curX, textY);

        g2d.setComposite(oldComp);
    }

    private void installDiceHoverListeners() {
        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (e.getComponent() == diceTrayPanel) {
                    diceExpanded = true;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateDiceExpandedFromPointer(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateDiceExpandedFromPointer(e);
            }
        };
        diceTrayPanel.addMouseListener(hover);
        diceTrayPanel.addMouseMotionListener(hover);
        addMouseListener(hover);
        addMouseMotionListener(hover);
    }

    private void updateDiceExpandedFromPointer(MouseEvent e) {
        if (e == null || e.getComponent() == null) {
            return;
        }
        Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), this);
        Rectangle expandedRect = expandedDiceBounds();
        boolean inside = expandedRect != null && expandedRect.contains(pt);
        if (inside != diceExpanded) {
            diceExpanded = inside;
            repaint();
        }
    }

    private Rectangle expandedDiceBounds() {
        int w = diceTrayPanel.getWidth();
        if (w <= 0) {
            return null;
        }
        Point origin = SwingUtilities.convertPoint(diceTrayPanel, 0, 0, this);
        int h = Math.max(BattleNumericOverlay.COLLAPSED_HEIGHT, numericOverlay.getRequiredHeight(w));
        return new Rectangle(origin.x, origin.y, w, h);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (!diceExpanded || event == null) {
            return null;
        }
        Point pt = SwingUtilities.convertPoint(this, event.getPoint(), diceTrayPanel);
        return numericOverlay.getTooltipAt(pt);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!diceExpanded) {
            return;
        }
        Rectangle bounds = expandedDiceBounds();
        if (bounds == null) {
            return;
        }
        Graphics2D og = (Graphics2D) g.create();
        og.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg = AssetStyles.BACKGROUND_SECONDARY;
        og.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 235));
        og.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
        og.translate(bounds.x, bounds.y);
        numericOverlay.paint(og, bounds.width, Integer.MAX_VALUE);
        og.dispose();
    }

    private void drawFullBiomeField(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH, Biome biome) {
        Image tile = tileForBiome(biome);
        int tileW = tileWidth(tile);
        int tileH = tileHeight(tile);
        tileRegion(g2d, tile, fieldX, fieldY, fieldW, fieldH, tileW, tileH);
    }

    private void drawBattlefieldBiomes(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH,
            int splitX, Biome leftBiome, Biome rightBiome) {
        Image leftTile = tileForBiome(leftBiome);
        Image rightTile = tileForBiome(rightBiome);
        int tileW = tileWidth(leftTile);
        int tileH = tileHeight(leftTile);

        Shape oldClip = g2d.getClip();
        int leftW = splitX - fieldX;
        int rightW = fieldX + fieldW - splitX;

        if (leftW > 0) {
            g2d.setClip(fieldX, fieldY, leftW, fieldH);
            tileRegion(g2d, leftTile, fieldX, fieldY, fieldW, fieldH, tileW, tileH);
        }
        if (rightW > 0) {
            g2d.setClip(splitX, fieldY, rightW, fieldH);
            tileRegion(g2d, rightTile, fieldX, fieldY, fieldW, fieldH, tileW, tileH);
        }
        g2d.setClip(oldClip);
    }

    private void drawCenterRiver(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH) {
        if (riverSprite == null) {
            return;
        }
        int riverW = tileWidth(riverSprite);
        int riverH = tileHeight(riverSprite);
        if (riverW <= 0 || riverH <= 0) {
            return;
        }
        int riverX = fieldX + (fieldW - riverW) / 2;
        Shape oldClip = g2d.getClip();
        g2d.setClip(fieldX, fieldY, fieldW, fieldH);
        int dx1 = riverFlipX ? riverX + riverW : riverX;
        int dx2 = riverFlipX ? riverX : riverX + riverW;
        for (int y = fieldY; y < fieldY + fieldH; y += riverH) {
            int dy1 = riverFlipY ? y + riverH : y;
            int dy2 = riverFlipY ? y : y + riverH;
            g2d.drawImage(riverSprite, dx1, dy1, dx2, dy2, 0, 0, riverW, riverH, this);
        }
        g2d.setClip(oldClip);
    }

    private void tileRegion(Graphics2D g2d, Image tile, int originX, int originY, int regionW, int regionH,
            int tileW, int tileH) {
        if (tile == null || tileW <= 0 || tileH <= 0) {
            g2d.setColor(AssetStyles.UI_BG_SECONDARY);
            g2d.fillRect(originX, originY, regionW, regionH);
            return;
        }
        for (int y = originY; y < originY + regionH; y += tileH) {
            for (int x = originX; x < originX + regionW; x += tileW) {
                g2d.drawImage(tile, x, y, this);
            }
        }
    }

    private Image tileForBiome(Biome biome) {
        if (biome == null) {
            return biomeTileCache.get(GameConstants.BIOME_PLAINS.getId());
        }
        Image tile = biomeTileCache.get(biome.getId());
        if (tile != null) {
            return tile;
        }
        return biomeTileCache.get(GameConstants.BIOME_PLAINS.getId());
    }

    private int tileWidth(Image tile) {
        if (tile == null) {
            return 64;
        }
        int w = tile.getWidth(this);
        return w > 0 ? w : 64;
    }

    private int tileHeight(Image tile) {
        if (tile == null) {
            return 64;
        }
        int h = tile.getHeight(this);
        return h > 0 ? h : 64;
    }

    private void drawAntColumn(Graphics2D g2d, List<BattleAnt> ants, Rectangle field, int contactLineX,
            boolean attackerSide) {
        if (ants.isEmpty() || field.width <= 4 || field.height <= 4) {
            return;
        }
        int edgePad = 4;
        int fieldRight = field.x + field.width;
        for (BattleAnt ant : ants) {
            boolean winged = RouteViewVisuals.isWinged(ant.type);
            boolean airSupport = !ant.reserve && ant.battleLine == GameConstants.BATTLE_LINE_AIR_SUPPORT;
            int legFrame = RouteViewVisuals.resolveLegFrame(ant.type, airSupport && winged, !airSupport, ant.wobblePhase,
                    animationSeconds, ant.motionRate);
            int jawFrame = RouteViewVisuals.resolveJawFrame(ant.type, ant.reserve, ant.wobblePhase, animationSeconds,
                    ant.motionRate);
            int wingFrame = RouteViewVisuals.resolveWingFrame(ant.type, airSupport && winged, ant.wobblePhase,
                    animationSeconds, ant.motionRate);
            int antennaFrame = RouteViewVisuals.resolveAntennaFrame(ant.type, ant.wobblePhase, animationSeconds,
                    ant.motionRate);

            ImageIcon icon = GameConstants.getAntSprite(
                    ant.type, ant.species, ant.profile, legFrame, jawFrame, wingFrame, antennaFrame, false);
            Image image = icon != null ? icon.getImage() : null;
            int w = icon != null ? Math.max(8, icon.getIconWidth() / ANT_SPRITE_SCALE) : 12;
            int h = icon != null ? Math.max(8, icon.getIconHeight() / ANT_SPRITE_SCALE) : 12;

            float wobble = (float) Math.sin(ant.wobblePhase + animationSeconds * WOBBLE_SPEED * ant.motionRate)
                    * WOBBLE_AMPLITUDE_PX;
            int drawY = field.y + edgePad
                    + Math.round(ant.laneY * (field.height - h - edgePad * 2f))
                    + Math.round(wobble);

            int drawX;
            if (airSupport) {
                float cycle = (animationSeconds / AIR_SUPPORT_FLY_CYCLE_SEC) + ant.flyPhase;
                cycle = cycle - (float) Math.floor(cycle);
                if (cycle > AIR_SUPPORT_FLY_PORTION) {
                    continue;
                }
                float t = cycle / AIR_SUPPORT_FLY_PORTION;
                int travel = field.width + w * 2;
                if (attackerSide) {
                    drawX = field.x - w + Math.round(t * travel);
                } else {
                    drawX = fieldRight - Math.round(t * travel);
                }
            } else {
                int lineOffset = lineOffsetPx(ant.battleLine, ant.reserve);
                if (ant.reserve) {
                    lineOffset = Math.max(WarBattleScene.ARTILLERY_LINE_OFFSET_PX,
                            lineOffset - Math.round(animationSeconds * RESERVE_WALK_PX_PER_SEC * ant.motionRate));
                }
                int jitter = Math.round((ant.laneJitter - 0.5f) * 2f * LINE_JITTER_PX);

                if (attackerSide) {
                    drawX = contactLineX - w - edgePad - lineOffset + jitter;
                    drawX = Math.max(field.x + edgePad, drawX);
                } else {
                    drawX = contactLineX + edgePad + lineOffset + jitter;
                    drawX = Math.min(fieldRight - w - edgePad, drawX);
                }
            }

            AffineTransform old = g2d.getTransform();
            double cx = drawX + w / 2.0;
            double cy = drawY + h / 2.0;
            g2d.translate(cx, cy);
            float faceAngle = RouteViewVisuals.movementFacingDegrees(attackerSide ? 1f : -1f, 0f);
            g2d.rotate(Math.toRadians(faceAngle));
            if (image != null) {
                g2d.drawImage(image, -w / 2, -h / 2, w, h, this);
            } else {
                g2d.setColor(attackerSide ? AssetStyles.FONT_COLOR : AssetStyles.FONT_COLOR_VALUE);
                g2d.fillRect(-w / 2, -h / 2, w, h);
            }
            g2d.setTransform(old);
        }
    }

    private static int lineOffsetPx(BattleLine line, boolean reserve) {
        if (reserve) {
            return WarBattleScene.RESERVE_LINE_OFFSET_PX;
        }
        if (line == GameConstants.BATTLE_LINE_ARTILLERY) {
            return WarBattleScene.ARTILLERY_LINE_OFFSET_PX;
        }
        if (line == GameConstants.BATTLE_LINE_AIR_SUPPORT) {
            return WarBattleScene.AIR_SUPPORT_LINE_OFFSET_PX;
        }
        return 0;
    }

    private List<BattleAnt> buildAntPool(WarBattleScene.Side side, boolean attackerSide) {
        if (side == null) {
            return List.of();
        }
        Dynasty dynasty = resolveDynasty(side.dynastyId());
        Random random = new Random(side.dynastyId() ^ (attackerSide ? 17 : 31));
        List<BattleAnt> ants = new ArrayList<>();

        appendLineAnts(ants, side.activeByLine(), dynasty, side.species(), random, false);
        Map<BattleLine, Map<AntClass, Integer>> reserveVisual =
                allocateReserveVisual(side.reserveByLine(), WarBattleScene.MAX_VISUAL_RESERVE_ANTS_PER_SIDE);
        appendLineAnts(ants, reserveVisual, dynasty, side.species(), random, true);
        return ants;
    }

    private static void appendLineAnts(List<BattleAnt> ants, Map<BattleLine, Map<AntClass, Integer>> byLine,
            Dynasty dynasty, AntSpecies species, Random random, boolean reserve) {
        if (byLine == null || byLine.isEmpty()) {
            return;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            Map<AntClass, Integer> typeCounts = byLine.get(line);
            if (typeCounts == null || typeCounts.isEmpty()) {
                continue;
            }
            int lineTotal = 0;
            for (Integer c : typeCounts.values()) {
                if (c != null) {
                    lineTotal += c;
                }
            }
            if (lineTotal <= 0) {
                continue;
            }
            int indexInLine = 0;
            for (Map.Entry<AntClass, Integer> entry : typeCounts.entrySet()) {
                AntClass type = entry.getKey();
                int count = entry.getValue() != null ? entry.getValue() : 0;
                if (type == null || type == GameConstants.CLASS_DEAD || count <= 0) {
                    continue;
                }
                if (line == GameConstants.BATTLE_LINE_AIR_SUPPORT) {
                    if (!RouteViewVisuals.isWinged(type)) {
                        continue;
                    }
                } else if (type == GameConstants.CLASS_DRONE) {
                    continue;
                }
                for (int i = 0; i < count; i++) {
                    float laneY = lineTotal <= 1 ? 0.5f : indexInLine / (float) (lineTotal - 1);
                    laneY = Math.max(0f, Math.min(1f, laneY + (random.nextFloat() - 0.5f) * 0.02f));
                    AntModProfile profile = dynasty != null
                            ? AntModService.sampleProfileFromDynasty(dynasty, type)
                            : AntModProfile.standard();
                    ants.add(new BattleAnt(type, species, profile, line, laneY, random.nextFloat(),
                            random.nextFloat() * (float) (Math.PI * 2),
                            0.85f + random.nextFloat() * 0.3f,
                            random.nextFloat(),
                            reserve));
                    indexInLine++;
                }
            }
        }
    }

    private static Map<BattleLine, Map<AntClass, Integer>> allocateReserveVisual(
            Map<BattleLine, Map<AntClass, Integer>> reserveByLine, int visualCap) {
        if (reserveByLine == null || reserveByLine.isEmpty() || visualCap <= 0) {
            return Map.of();
        }
        int total = 0;
        for (Map<AntClass, Integer> counts : reserveByLine.values()) {
            if (counts == null) {
                continue;
            }
            for (Integer c : counts.values()) {
                if (c != null) {
                    total += c;
                }
            }
        }
        if (total <= 0) {
            return Map.of();
        }
        if (total <= visualCap) {
            return reserveByLine;
        }
        float scale = visualCap / (float) total;
        Map<BattleLine, Map<AntClass, Integer>> scaled = new HashMap<>();
        int assigned = 0;
        for (Map.Entry<BattleLine, Map<AntClass, Integer>> lineEntry : reserveByLine.entrySet()) {
            Map<AntClass, Integer> out = new HashMap<>();
            if (lineEntry.getValue() != null) {
                for (Map.Entry<AntClass, Integer> e : lineEntry.getValue().entrySet()) {
                    int count = e.getValue() != null ? e.getValue() : 0;
                    int visual = Math.max(0, Math.round(count * scale));
                    if (visual > 0 && e.getKey() != null) {
                        out.put(e.getKey(), visual);
                        assigned += visual;
                    }
                }
            }
            if (!out.isEmpty()) {
                scaled.put(lineEntry.getKey(), out);
            }
        }
        if (assigned == 0) {
            for (Map.Entry<BattleLine, Map<AntClass, Integer>> lineEntry : reserveByLine.entrySet()) {
                if (lineEntry.getValue() == null) {
                    continue;
                }
                for (Map.Entry<AntClass, Integer> e : lineEntry.getValue().entrySet()) {
                    if (e.getKey() != null && e.getValue() != null && e.getValue() > 0) {
                        scaled.put(lineEntry.getKey(), Map.of(e.getKey(), 1));
                        return scaled;
                    }
                }
            }
        }
        return scaled;
    }

    private Dynasty resolveDynasty(int dynastyId) {
        if (dynastyId <= 0 || engine == null || engine.getWorld() == null) {
            return null;
        }
        return engine.getWorld().findDynastyById(dynastyId);
    }

    private String buildHeaderText(WarBattleScene scene) {
        int attackerAnts = sumClassCounts(scene.getAttacker().classCounts());
        int defenderAnts = sumClassCounts(scene.getDefender().classCounts());
        String phase = formatPhase(scene.getPhase());
        String location = LanguageStrings.format(LanguageStrings.BATTLE_LOCATION_FMT, scene.getLocationName());
        String attackerPower = LanguageStrings.format(
                LanguageStrings.BATTLE_POWER_FMT,
                scene.getAttacker().dynastyName(),
                AssetStyles.formatNumber(scene.getAttacker().livingActive()));
        String defenderPower = LanguageStrings.format(
                LanguageStrings.BATTLE_POWER_FMT,
                scene.getDefender().dynastyName(),
                AssetStyles.formatNumber(scene.getDefender().livingActive()));
        String progress = LanguageStrings.format(
                LanguageStrings.WAR_PROGRESS_FMT,
                Math.round(scene.getWarProgressPercent()));
        String forces = LanguageStrings.format(
                LanguageStrings.BATTLE_FORCES_FMT,
                AssetStyles.formatNumber(attackerAnts),
                AssetStyles.formatNumber(defenderAnts));
        if (scene.getPhase() == GameConstants.WAR_STAGE_REDEPLOYING) {
            String redeploy = LanguageStrings.format(
                    LanguageStrings.BATTLE_REDEPLOY_FMT,
                    String.valueOf(scene.getRedeployHoursRemaining()));
            return scene.getWarName() + "  |  " + location + "  |  " + redeploy + "  |  " + forces + "  |  " + progress;
        }
        return scene.getWarName() + "  |  " + location + "  |  " + phase
                + "  |  " + forces + "  |  " + attackerPower + " vs " + defenderPower + "  |  " + progress;
    }

    private static int sumClassCounts(Map<AntClass, Integer> classCounts) {
        if (classCounts == null || classCounts.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (Integer count : classCounts.values()) {
            if (count != null) {
                sum += count;
            }
        }
        return sum;
    }

    private static String formatPhase(WarStagePhase phase) {
        if (phase == null) {
            return GameConstants.WAR_STAGE_ACTIVE_CLASH.getName();
        }
        return phase.getName();
    }

    private void loadBiomeTiles() {
        for (Biome b : GameConstants.getBiomes()) {
            if (b != null && b.getTile() != null) {
                biomeTileCache.put(b.getId(), b.getTile().getImage());
            }
        }
    }

    private void putBiomeTile(Biome biome, String path) {
        Image image = UiResourceLoader.loadImage(WarBattleViewPanel.class, "/" + path);
        if (image == null) {
            image = UiResourceLoader.loadImage(WarBattleViewPanel.class, path);
        }
        if (image != null) {
            biomeTileCache.put(biome.getId(), image);
        }
    }

    private static final class BattleAnt {
        private final AntClass type;
        private final AntSpecies species;
        private final AntModProfile profile;
        private final BattleLine battleLine;
        private final float laneY;
        private final float laneJitter;
        private final float wobblePhase;
        private final float motionRate;
        private final float flyPhase;
        private final boolean reserve;

        private BattleAnt(AntClass type, AntSpecies species, AntModProfile profile, BattleLine battleLine,
                float laneY, float laneJitter, float wobblePhase, float motionRate, float flyPhase, boolean reserve) {
            this.type = type;
            this.species = species;
            this.profile = profile != null ? profile : AntModProfile.standard();
            this.battleLine = battleLine != null ? battleLine : GameConstants.BATTLE_LINE_INFANTRY;
            this.laneY = laneY;
            this.laneJitter = laneJitter;
            this.wobblePhase = wobblePhase;
            this.motionRate = motionRate;
            this.flyPhase = flyPhase;
            this.reserve = reserve;
        }
    }
}
