package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.HuntBattleFallenBody;
import com.grimidk.formicempire.classes.entities.services.colony.HuntBattleParticipant;
import com.grimidk.formicempire.classes.entities.services.colony.HuntBattleState;
import com.grimidk.formicempire.classes.entities.services.colony.HuntCreatureCombatService;
import com.grimidk.formicempire.classes.entities.services.shared.BattleNumericFeed;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
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

public class HuntBattleViewPanel extends JPanel {

    public enum BattleKind {
        HUNT,
        INVASION
    }

    record SlotPosition(float x, float y, float radius, double angleRadians) {
    }

    private record BandGeometry(
            float infStart,
            float infMax,
            float infOuter,
            float artStart,
            float artMax,
            float airBaseRadius) {
    }

    private static final int HEADER_HEIGHT = 0;
    private static final int ANT_SPRITE_SCALE = 2;
    private static final int CRITTER_SPRITE_SCALE = 1;
    private static final int ANIMATION_FRAME_MS = 50;
    private static final int MAX_VISUAL_ANTS_PER_LINE = 360;
    private static final float BASE_RING_STEP_PX = 22f;
    private static final float BASE_ARC_SPACING_PX = 22f;
    private static final float CRITTER_TURN_DEG_PER_SEC = 720f;
    private static final float CRITTER_MIN_FOCUS_DELTA_DEG = 45f;
    private static final float CRITTER_HOLD_MIN_SEC = 0.28f;
    private static final float CRITTER_HOLD_RANGE_SEC = 0.42f;

    private final Colony colony;
    private final Engine engine;
    private final int battleId;
    private final BattleKind battleKind;
    private final JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
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
            float bugHp = state != null ? Math.max(0f, Math.min(1f, state.getBugHealthRatio())) : 0f;
            int bugW = Math.round(w * bugHp);
            g.setColor(new Color(95, 185, 255));
            g.fillRect(0, 0, w - bugW, h);
            g.setColor(new Color(255, 135, 95));
            g.fillRect(w - bugW, 0, bugW, h);
        }
    };
    private final Map<Integer, Image> biomeTileCache = new HashMap<>();
    private final BattleNumericOverlay numericOverlay = new BattleNumericOverlay();
    private HuntBattleState state;
    private Timer animationTimer;
    private float animationSeconds;
    private long lastTickNanos;
    private float critterFacingDegrees;
    private float critterTargetDegrees;
    private float critterFocusHoldSec;
    private boolean critterFacingInitialized;
    private int lastObservedTick = -1;
    private int visualFocusSlot = -1;

    public HuntBattleViewPanel(Colony colony, Engine engine, int battleId) {
        this(colony, engine, battleId, BattleKind.HUNT);
    }

    public HuntBattleViewPanel(Colony colony, Engine engine, int battleId, BattleKind battleKind) {
        super(new BorderLayout());
        this.colony = colony;
        this.engine = engine;
        this.battleId = battleId;
        this.battleKind = battleKind != null ? battleKind : BattleKind.HUNT;
        setOpaque(true);
        setBackground(AssetStyles.BACKGROUND_COLOR);

        headerPanel.setOpaque(true);
        headerPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        diceTrayPanel.setOpaque(false);
        diceTrayPanel.setPreferredSize(new Dimension(320, BattleNumericOverlay.COLLAPSED_HEIGHT));
        ToolTipManager.sharedInstance().registerComponent(diceTrayPanel);
        ToolTipManager.sharedInstance().registerComponent(this);
        installDiceHoverListeners();

        battleProgressBar.setOpaque(false);
        battleProgressBar.setPreferredSize(new Dimension(100, 6));

        headerPanel.add(diceTrayPanel, BorderLayout.CENTER);
        headerPanel.add(battleProgressBar, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        loadBiomeTiles();
        refreshScene();
        startAnimation();
    }

    public Colony getColony() {
        return colony;
    }

    public int getBattleId() {
        return battleId;
    }

    public BattleKind getBattleKind() {
        return battleKind;
    }

    public int getTargetId() {
        return battleId;
    }

    public void refreshScene() {
        state = resolveBattleState();
        battleProgressBar.repaint();
    }

    public boolean hasActiveState() {
        return resolveBattleState() != null;
    }

    private HuntBattleState resolveBattleState() {
        if (colony == null) {
            return null;
        }
        if (battleKind == BattleKind.INVASION) {
            return HuntCreatureCombatService.getInvasionState(colony, battleId);
        }
        return HuntCreatureCombatService.getState(colony, battleId);
    }

    public void startAnimation() {
        if (animationTimer != null) {
            animationTimer.start();
            return;
        }
        lastTickNanos = System.nanoTime();
        animationTimer = new Timer(ANIMATION_FRAME_MS, e -> {
            long now = System.nanoTime();
            float deltaSec = (now - lastTickNanos) / 1_000_000_000f;
            lastTickNanos = now;
            if (deltaSec <= 0f || deltaSec > 0.25f) {
                deltaSec = ANIMATION_FRAME_MS / 1000f;
            }
            animationSeconds += deltaSec;
            if (state != null) {
                int fieldW = Math.max(240, getWidth());
                int fieldH = Math.max(120, getHeight() - headerPanel.getHeight());
                updateCritterFocus(deltaSec, fieldW, fieldH);
            }
            BattleNumericFeed feed = state != null ? state.getNumericFeed() : null;
            int stateTickIndex = state != null ? state.getTickIndex() : -1;
            World w = engine != null ? engine.getWorld() : null;
            int minute = w != null ? w.getMinute() : 0;
            numericOverlay.update(feed, deltaSec, minute, stateTickIndex);
            diceTrayPanel.repaint();
            repaint();
        });
        animationTimer.start();
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
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
        if (state == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int fieldY = headerPanel.getHeight();
        int fieldH = Math.max(120, getHeight() - fieldY);
        int fieldW = getWidth();
        Biome biome = resolveBiome();
        drawFullBiomeField(g2d, 0, fieldY, fieldW, fieldH, biome);

        int centerX = fieldW / 2;
        int centerY = fieldY + fieldH / 2;
        BandGeometry band = computeBandGeometry(fieldW, fieldH, state);
        if (!critterFacingInitialized) {
            updateCritterFocus(0f, fieldW, fieldH);
        }

        drawFallenHunters(g2d, centerX, centerY, band);
        drawGroundHunters(g2d, state.livingHunters(), centerX, centerY, band);
        drawCenterCritter(g2d, centerX, centerY);
        drawAirSupportHunters(g2d, state.livingHunters(), centerX, centerY, band);
        g2d.dispose();
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

    private static BandGeometry computeBandGeometry(int fieldW, int fieldH, HuntBattleState state) {
        int minDim = Math.max(120, Math.min(fieldW, fieldH));
        boolean hasArtillery = state != null && state.getArtillerySlotCount() > 0;
        int infTotal = state != null ? state.getInfantrySlotCount() : 0;
        float infStart = Math.max(46f, Math.min(minDim * 0.13f, 68f));
        float infMax = hasArtillery
                ? Math.max(infStart + 40f, minDim * 0.31f)
                : Math.max(infStart + 60f, minDim * 0.44f);
        float infOuter = outerRingRadius(infTotal, infStart, infMax, BASE_RING_STEP_PX, BASE_ARC_SPACING_PX);
        float artMax = Math.max(infOuter + 44f, minDim * 0.46f);
        float artStart = Math.min(artMax - 12f, Math.max(infOuter + 38f, infStart + 72f));
        artStart = Math.max(infOuter + 24f, artStart);
        float airBaseRadius = Math.max(infStart + 36f, Math.min(minDim * 0.38f, artMax));
        return new BandGeometry(infStart, infMax, infOuter, artStart, artMax, airBaseRadius);
    }

    private SlotPosition positionForLineSlot(BattleLine line, int lineSlot, BandGeometry band) {
        if (line == GameConstants.BATTLE_LINE_ARTILLERY || line == GameConstants.BATTLE_LINE_AIR_SUPPORT) {
            int artTotal = state != null ? Math.max(1, state.getArtillerySlotCount()) : 1;
            return computeRingPosition(lineSlot, artTotal, band.artStart, band.artMax, BASE_RING_STEP_PX,
                    BASE_ARC_SPACING_PX);
        }
        int infTotal = state != null ? Math.max(1, state.getInfantrySlotCount()) : 1;
        return computeRingPosition(lineSlot, infTotal, band.infStart, band.infMax, BASE_RING_STEP_PX,
                BASE_ARC_SPACING_PX);
    }

    private void drawFallenHunters(Graphics2D g2d, int centerX, int centerY, BandGeometry band) {
        AntSpecies species = colony.getDynasty() != null ? colony.getDynasty().getSpecies() : GameConstants.SPECIES_OMNI;
        for (HuntBattleFallenBody body : state.getFallenBodies()) {
            SlotPosition pos = positionForLineSlot(body.getBattleLine(), body.getLineSlot(), band);
            int drawX = centerX + Math.round(pos.x());
            int drawY = centerY + Math.round(pos.y());
            ImageIcon icon = GameConstants.getAntSprite(
                    GameConstants.CLASS_DEAD,
                    species,
                    body.getModProfile(),
                    1,
                    1,
                    1,
                    1,
                    false);
            drawScaledSprite(g2d, icon, drawX, drawY, ANT_SPRITE_SCALE, body.getBodyRotationDegrees());
        }
    }

    private void drawGroundHunters(Graphics2D g2d, List<HuntBattleParticipant> hunters, int centerX, int centerY,
            BandGeometry band) {
        if (hunters.isEmpty()) {
            return;
        }
        List<HuntBattleParticipant> artillery = new ArrayList<>();
        List<HuntBattleParticipant> infantry = new ArrayList<>();
        for (HuntBattleParticipant hunter : hunters) {
            if (hunter.getAnt() == null || hunter.getBattleLine() == GameConstants.BATTLE_LINE_AIR_SUPPORT) {
                continue;
            }
            if (hunter.getBattleLine() == GameConstants.BATTLE_LINE_ARTILLERY) {
                artillery.add(hunter);
            } else {
                infantry.add(hunter);
            }
        }
        drawGroundLine(g2d, artillery, centerX, centerY, band, true);
        drawGroundLine(g2d, infantry, centerX, centerY, band, false);
    }

    private void drawGroundLine(Graphics2D g2d, List<HuntBattleParticipant> lineHunters, int centerX, int centerY,
            BandGeometry band, boolean artillery) {
        if (lineHunters.isEmpty()) {
            return;
        }
        AntSpecies species = colony.getDynasty() != null ? colony.getDynasty().getSpecies() : GameConstants.SPECIES_OMNI;
        int total = lineHunters.size();
        int step = total > MAX_VISUAL_ANTS_PER_LINE ? Math.max(1, total / MAX_VISUAL_ANTS_PER_LINE) : 1;
        float wobbleAmp = artillery ? 1.1f : 2.2f;
        float rate = artillery ? 0.7f : 1.0f;
        for (int i = 0; i < total; i += step) {
            HuntBattleParticipant hunter = lineHunters.get(i);
            Ant ant = hunter.getAnt();
            SlotPosition pos = positionForLineSlot(hunter.getBattleLine(), hunter.getLineSlot(), band);
            double phase = hunter.getOrbitSlot() * 0.7;
            double radialOffset = Math.sin(animationSeconds * 5.2 * rate + phase) * wobbleAmp;
            int drawX = centerX + Math.round(pos.x() + (float) (Math.cos(pos.angleRadians()) * radialOffset));
            int drawY = centerY + Math.round(pos.y() + (float) (Math.sin(pos.angleRadians()) * radialOffset));
            AntClass type = ant.getAntClass();
            int legFrame = RouteViewVisuals.resolveLegFrame(type, false, true, (float) phase, animationSeconds, rate);
            int jawFrame = RouteViewVisuals.resolveJawFrame(type, false, (float) phase, animationSeconds, rate);
            int antennaFrame = RouteViewVisuals.resolveAntennaFrame(type, (float) phase, animationSeconds, rate);
            ImageIcon icon = GameConstants.getAntSprite(type, species, ant.getModProfile(), legFrame, jawFrame, 1,
                    antennaFrame, false);
            float faceDegrees = (float) Math.toDegrees(Math.atan2(centerY - drawY, centerX - drawX)) + 90f;
            drawScaledSprite(g2d, icon, drawX, drawY, ANT_SPRITE_SCALE, faceDegrees);
        }
    }

    private void drawAirSupportHunters(Graphics2D g2d, List<HuntBattleParticipant> hunters, int centerX, int centerY,
            BandGeometry band) {
        List<HuntBattleParticipant> flyers = new ArrayList<>();
        for (HuntBattleParticipant hunter : hunters) {
            if (hunter.getAnt() != null && hunter.getBattleLine() == GameConstants.BATTLE_LINE_AIR_SUPPORT) {
                flyers.add(hunter);
            }
        }
        if (flyers.isEmpty()) {
            return;
        }
        AntSpecies species = colony.getDynasty() != null ? colony.getDynasty().getSpecies() : GameConstants.SPECIES_OMNI;
        int total = flyers.size();
        int step = total > MAX_VISUAL_ANTS_PER_LINE ? Math.max(1, total / MAX_VISUAL_ANTS_PER_LINE) : 1;
        int airTotal = Math.max(1, state.getAirSupportSlotCount());
        for (int i = 0; i < total; i += step) {
            HuntBattleParticipant hunter = flyers.get(i);
            Ant ant = hunter.getAnt();
            AntClass type = ant.getAntClass();
            int slot = hunter.getLineSlot();
            double basePhase = (2.0 * Math.PI * slot) / airTotal;
            double omega = (slot % 2 == 0 ? 1.0 : -1.0) * (1.15 + (slot % 5) * 0.14);
            double t = basePhase + animationSeconds * omega;
            float rx = band.airBaseRadius * (0.76f + 0.18f * (slot % 3));
            float ry = rx * 0.58f;
            double tilt = basePhase * 0.5;
            double cosTilt = Math.cos(tilt);
            double sinTilt = Math.sin(tilt);
            double lx = Math.cos(t) * rx;
            double ly = Math.sin(t) * ry;
            double wx = lx * cosTilt - ly * sinTilt;
            double wy = lx * sinTilt + ly * cosTilt;
            double lvx = -Math.sin(t) * rx * omega;
            double lvy = Math.cos(t) * ry * omega;
            double vx = lvx * cosTilt - lvy * sinTilt;
            double vy = lvx * sinTilt + lvy * cosTilt;

            int drawX = centerX + (int) Math.round(wx);
            int drawY = centerY + (int) Math.round(wy);
            boolean winged = RouteViewVisuals.isWinged(type);
            float wobble = (float) basePhase;
            int legFrame = RouteViewVisuals.resolveLegFrame(type, winged, !winged, wobble, animationSeconds, 1f);
            int jawFrame = RouteViewVisuals.resolveJawFrame(type, false, wobble, animationSeconds, 1f);
            int wingFrame = RouteViewVisuals.resolveWingFrame(type, winged, wobble, animationSeconds, 1f);
            int antennaFrame = RouteViewVisuals.resolveAntennaFrame(type, wobble, animationSeconds, 1f);
            ImageIcon icon = GameConstants.getAntSprite(type, species, ant.getModProfile(), legFrame, jawFrame,
                    wingFrame, antennaFrame, false);
            float faceDegrees = RouteViewVisuals.movementFacingDegrees((float) vx, (float) vy);
            drawScaledSprite(g2d, icon, drawX, drawY, ANT_SPRITE_SCALE, faceDegrees);
        }
    }

    private void drawCenterCritter(Graphics2D g2d, int centerX, int centerY) {
        Species species = state.getSpecies();
        if (species == null) {
            return;
        }
        int legFrame = RouteViewVisuals.resolveCritterLegFrame(species, true, animationSeconds, animationSeconds, 1f);
        int antennaFrame = RouteViewVisuals.resolveCritterAntennaFrame(species, animationSeconds, animationSeconds,
                1f);
        ImageIcon icon = GameConstants.getCritterSprite(species, legFrame, antennaFrame);
        drawScaledSprite(g2d, icon, centerX, centerY, CRITTER_SPRITE_SCALE, critterFacingDegrees);
    }

    private void updateCritterFocus(float deltaSec, int fieldW, int fieldH) {
        if (state == null) {
            return;
        }
        List<HuntBattleParticipant> focusPool = state.livingFrontlineHunters();
        if (focusPool.isEmpty()) {
            return;
        }
        boolean currentTargetAlive = false;
        for (HuntBattleParticipant candidate : focusPool) {
            if (candidate.getOrbitSlot() == visualFocusSlot) {
                currentTargetAlive = true;
                break;
            }
        }
        int tick = state.getTickIndex();
        boolean needNewFocus = !critterFacingInitialized
                || tick != lastObservedTick
                || critterFocusHoldSec <= 0f
                || !currentTargetAlive;
        if (needNewFocus) {
            BandGeometry band = computeBandGeometry(fieldW, fieldH, state);
            List<HuntBattleParticipant> farCandidates = new ArrayList<>();
            for (HuntBattleParticipant candidate : focusPool) {
                float angle = angleDegreesForParticipant(candidate, band);
                if (!critterFacingInitialized
                        || Math.abs(wrapDegrees180(angle - critterFacingDegrees)) >= CRITTER_MIN_FOCUS_DELTA_DEG) {
                    farCandidates.add(candidate);
                }
            }
            List<HuntBattleParticipant> pickFrom = farCandidates.isEmpty() ? focusPool : farCandidates;
            HuntBattleParticipant chosen = pickFrom.get(GameRandom.nextInt(pickFrom.size()));
            critterTargetDegrees = angleDegreesForParticipant(chosen, band);
            visualFocusSlot = chosen.getOrbitSlot();
            if (!critterFacingInitialized) {
                critterFacingDegrees = critterTargetDegrees;
                critterFacingInitialized = true;
            }
            critterFocusHoldSec = CRITTER_HOLD_MIN_SEC + GameRandom.nextFloat() * CRITTER_HOLD_RANGE_SEC;
            lastObservedTick = tick;
        }
        critterFocusHoldSec -= deltaSec;
        critterFacingDegrees = stepAngleToward(critterFacingDegrees, critterTargetDegrees,
                CRITTER_TURN_DEG_PER_SEC * Math.max(0f, deltaSec));
    }

    private float angleDegreesForParticipant(HuntBattleParticipant participant, BandGeometry band) {
        SlotPosition pos = positionForLineSlot(participant.getBattleLine(), participant.getLineSlot(), band);
        float angle = (float) Math.toDegrees(Math.atan2(pos.y(), pos.x())) + 90f;
        return ((angle % 360f) + 360f) % 360f;
    }

    static SlotPosition computeRingPosition(int lineSlot, int lineTotal, float startRadius, float maxRadius,
            float baseRingStep, float baseArcSpacing) {
        int total = Math.max(1, lineTotal);
        int slot = Math.floorMod(lineSlot, total);
        float start = Math.max(8f, startRadius);
        float max = Math.max(start, maxRadius);
        float[] spacing = resolveRingSpacing(total, start, max, baseRingStep, baseArcSpacing);
        float chosenStep = spacing[0];
        float chosenArc = spacing[1];

        int ringIdx = 0;
        int remainingBefore = 0;
        int slotsOnRing = total;
        float ringRadius = start;
        int remaining = total;
        while (remaining > 0) {
            float candidateR = start + ringIdx * chosenStep;
            int cap = Math.max(8, (int) Math.floor(2.0 * Math.PI * candidateR / Math.max(1f, chosenArc)));
            float nextR = start + (ringIdx + 1) * chosenStep;
            boolean isLastAllowedRing = nextR > max + 0.01f;
            int countHere = isLastAllowedRing ? remaining : Math.min(remaining, cap);
            if (slot < remainingBefore + countHere) {
                ringRadius = Math.min(max, candidateR);
                slotsOnRing = countHere;
                break;
            }
            remainingBefore += countHere;
            remaining -= countHere;
            ringIdx++;
        }
        int posInRing = slot - remainingBefore;
        double stagger = ringIdx * 0.61803399 * Math.PI * 2.0;
        double baseAngle = stagger + (2.0 * Math.PI * posInRing) / Math.max(1, slotsOnRing);
        double angleStep = (2.0 * Math.PI) / Math.max(8, slotsOnRing);
        float radialJitter = hashUnit(slot * 31 + 17) * Math.min(5f, chosenStep * 0.25f);
        double angularJitter = hashUnit(slot * 73 + 41) * angleStep * 0.38;
        float r = Math.max(start * 0.85f, Math.min(max + 4f, ringRadius + radialJitter));
        double angle = baseAngle + angularJitter;
        return new SlotPosition((float) (Math.cos(angle) * r), (float) (Math.sin(angle) * r), r, angle);
    }

    static float outerRingRadius(int lineTotal, float startRadius, float maxRadius, float baseRingStep,
            float baseArcSpacing) {
        if (lineTotal <= 0) {
            return startRadius;
        }
        float start = Math.max(8f, startRadius);
        float max = Math.max(start, maxRadius);
        float[] spacing = resolveRingSpacing(lineTotal, start, max, baseRingStep, baseArcSpacing);
        float chosenStep = spacing[0];
        float chosenArc = spacing[1];
        int ringIdx = 0;
        int remaining = lineTotal;
        float outer = start;
        while (remaining > 0) {
            float candidateR = start + ringIdx * chosenStep;
            outer = Math.min(max, candidateR);
            float nextR = start + (ringIdx + 1) * chosenStep;
            if (nextR > max + 0.01f) {
                break;
            }
            int cap = Math.max(8, (int) Math.floor(2.0 * Math.PI * candidateR / Math.max(1f, chosenArc)));
            remaining -= cap;
            ringIdx++;
        }
        return outer;
    }

    private static float[] resolveRingSpacing(int total, float start, float max, float baseRingStep,
            float baseArcSpacing) {
        float minStep = Math.min(baseRingStep, 8f);
        float minArc = Math.min(baseArcSpacing, 10f);
        float chosenStep = baseRingStep;
        float chosenArc = baseArcSpacing;
        if (max > start && totalRequiredRadius(total, start, chosenStep, chosenArc) > max) {
            float low = 0f;
            float high = 1f;
            for (int iter = 0; iter < 12; iter++) {
                float mid = (low + high) * 0.5f;
                float s = Math.max(minStep, baseRingStep * mid);
                float a = Math.max(minArc, baseArcSpacing * mid);
                if (totalRequiredRadius(total, start, s, a) <= max) {
                    low = mid;
                } else {
                    high = mid;
                }
            }
            chosenStep = Math.max(minStep, baseRingStep * low);
            chosenArc = Math.max(minArc, baseArcSpacing * low);
        }
        return new float[] { chosenStep, chosenArc };
    }

    private static float totalRequiredRadius(int total, float start, float ringStep, float arcSpacing) {
        int remaining = total;
        int ringIdx = 0;
        float radius = start;
        while (remaining > 0) {
            radius = start + ringIdx * ringStep;
            int cap = Math.max(8, (int) Math.floor(2.0 * Math.PI * radius / Math.max(1f, arcSpacing)));
            remaining -= cap;
            ringIdx++;
        }
        return radius;
    }

    private static float hashUnit(int seed) {
        int h = seed * 0x27d4eb2d;
        h ^= (h >>> 15);
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        return ((h & 0xFFFF) / 32767.5f) - 1f;
    }

    static float wrapDegrees180(float deltaDegrees) {
        float wrapped = ((deltaDegrees + 180f) % 360f + 360f) % 360f - 180f;
        return wrapped <= -180f ? 180f : wrapped;
    }

    static float stepAngleToward(float currentDegrees, float targetDegrees, float maxStepDegrees) {
        float step = Math.max(0f, maxStepDegrees);
        float diff = wrapDegrees180(targetDegrees - currentDegrees);
        float next = Math.abs(diff) <= step ? targetDegrees : currentDegrees + Math.copySign(step, diff);
        return ((next % 360f) + 360f) % 360f;
    }

    private void drawScaledSprite(Graphics2D g2d, ImageIcon icon, int centerX, int centerY, int scale,
            float angleDegrees) {
        if (icon == null) {
            return;
        }
        Image image = icon.getImage();
        int w = Math.max(8, icon.getIconWidth() / scale);
        int h = Math.max(8, icon.getIconHeight() / scale);
        AffineTransform old = g2d.getTransform();
        g2d.translate(centerX, centerY);
        g2d.rotate(Math.toRadians(angleDegrees));
        g2d.drawImage(image, -w / 2, -h / 2, w, h, this);
        g2d.setTransform(old);
    }

    private Biome resolveBiome() {
        World world = engine != null ? engine.getWorld() : null;
        if (world == null || colony == null) {
            return GameConstants.BIOME_PLAINS;
        }
        com.grimidk.formicempire.classes.entities.world.Hex hex = world.getHexOfColony(colony);
        return hex != null && hex.getBiome() != null ? hex.getBiome() : GameConstants.BIOME_PLAINS;
    }

    private void drawFullBiomeField(Graphics2D g2d, int fieldX, int fieldY, int fieldW, int fieldH, Biome biome) {
        Image tile = tileForBiome(biome);
        int tileW = tile != null && tile.getWidth(this) > 0 ? tile.getWidth(this) : 64;
        int tileH = tile != null && tile.getHeight(this) > 0 ? tile.getHeight(this) : 64;
        if (tile == null) {
            g2d.setColor(AssetStyles.UI_BG_SECONDARY);
            g2d.fillRect(fieldX, fieldY, fieldW, fieldH);
            return;
        }
        for (int y = fieldY; y < fieldY + fieldH; y += tileH) {
            for (int x = fieldX; x < fieldX + fieldW; x += tileW) {
                g2d.drawImage(tile, x, y, this);
            }
        }
    }

    private void loadBiomeTiles() {
        for (Biome b : GameConstants.getBiomes()) {
            if (b != null && b.getTile() != null) {
                biomeTileCache.put(b.getId(), b.getTile().getImage());
            }
        }
    }

    private void putBiomeTile(Biome biome, String path) {
        Image image = UiResourceLoader.loadImage(HuntBattleViewPanel.class, "/" + path);
        if (image == null) {
            image = UiResourceLoader.loadImage(HuntBattleViewPanel.class, path);
        }
        if (image != null) {
            biomeTileCache.put(biome.getId(), image);
        }
    }

    private Image tileForBiome(Biome biome) {
        if (biome == null) {
            return biomeTileCache.get(GameConstants.BIOME_PLAINS.getId());
        }
        Image tile = biomeTileCache.get(biome.getId());
        return tile != null ? tile : biomeTileCache.get(GameConstants.BIOME_PLAINS.getId());
    }
}
