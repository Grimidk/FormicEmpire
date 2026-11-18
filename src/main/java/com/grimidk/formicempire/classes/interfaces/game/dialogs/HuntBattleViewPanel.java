package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
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
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
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

    private static final int HEADER_HEIGHT = 0;
    private static final int ANT_SPRITE_SCALE = 2;
    private static final int CRITTER_SPRITE_SCALE = 1;
    private static final int ANIMATION_FRAME_MS = 50;
    private static final float ORBIT_RADIUS_MIN = 32f;
    private static final float ORBIT_RADIUS_MAX = 68f;

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
        float radius = orbitRadius(fieldH);
        int slotCount = Math.max(1, state.getOrbitSlotCount());
        drawFallenHunters(g2d, centerX, centerY, radius, slotCount);
        List<HuntBattleParticipant> living = state.livingHunters();
        drawOrbitingHunters(g2d, living, centerX, centerY, radius, slotCount);
        drawCenterCritter(g2d, centerX, centerY, radius, slotCount);
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

    private float orbitRadius(int fieldH) {
        return Math.max(ORBIT_RADIUS_MIN, Math.min(fieldH * 0.18f, ORBIT_RADIUS_MAX));
    }

    private void drawFallenHunters(Graphics2D g2d, int centerX, int centerY, float radius, int slotCount) {
        AntSpecies species = colony.getDynasty() != null ? colony.getDynasty().getSpecies() : GameConstants.SPECIES_OMNI;
        for (HuntBattleFallenBody body : state.getFallenBodies()) {
            double angle = slotAngle(body.getOrbitSlot(), slotCount);
            int drawX = centerX + (int) Math.round(Math.cos(angle) * radius);
            int drawY = centerY + (int) Math.round(Math.sin(angle) * radius);
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

    private void drawOrbitingHunters(Graphics2D g2d, List<HuntBattleParticipant> hunters, int centerX, int centerY,
            float radius, int slotCount) {
        if (hunters.isEmpty()) {
            return;
        }
        for (HuntBattleParticipant hunter : hunters) {
            Ant ant = hunter.getAnt();
            if (ant == null) {
                continue;
            }
            double angle = slotAngle(hunter.getOrbitSlot(), slotCount);
            int drawX = centerX + (int) Math.round(Math.cos(angle) * radius);
            int drawY = centerY + (int) Math.round(Math.sin(angle) * radius);
            AntClass type = ant.getAntClass();
            AntSpecies species = colony.getDynasty() != null ? colony.getDynasty().getSpecies() : GameConstants.SPECIES_OMNI;
            int slot = hunter.getOrbitSlot();
            int legFrame = RouteViewVisuals.resolveLegFrame(type, false, true, slot * 0.7f, animationSeconds, 1f);
            int jawFrame = RouteViewVisuals.resolveJawFrame(type, false, slot * 0.7f, animationSeconds, 1f);
            int wingFrame = 1;
            int antennaFrame = RouteViewVisuals.resolveAntennaFrame(type, slot * 0.7f, animationSeconds, 1f);
            ImageIcon icon = GameConstants.getAntSprite(type, species, ant.getModProfile(), legFrame, jawFrame,
                    wingFrame, antennaFrame, false);
            float faceDegrees = (float) Math.toDegrees(Math.atan2(centerY - drawY, centerX - drawX)) + 90f;
            drawScaledSprite(g2d, icon, drawX, drawY, ANT_SPRITE_SCALE, faceDegrees);
        }
    }

    private double slotAngle(int slot, int slotCount) {
        return Math.PI * 2.0 * slot / Math.max(1, slotCount);
    }

    private void drawCenterCritter(Graphics2D g2d, int centerX, int centerY, float radius, int slotCount) {
        Species species = state.getSpecies();
        if (species == null) {
            return;
        }
        float faceAngle = 0f;
        if (slotCount > 0 && !state.livingHunters().isEmpty()) {
            int focusSlot = state.getFocusOrbitIndex();
            double angle = slotAngle(focusSlot, slotCount);
            int targetX = centerX + (int) Math.round(Math.cos(angle) * radius);
            int targetY = centerY + (int) Math.round(Math.sin(angle) * radius);
            faceAngle = (float) Math.toDegrees(Math.atan2(targetY - centerY, targetX - centerX)) + 90f;
        }
        int legFrame = RouteViewVisuals.resolveCritterLegFrame(species, true, animationSeconds, animationSeconds, 1f);
        int antennaFrame = RouteViewVisuals.resolveCritterAntennaFrame(species, animationSeconds, animationSeconds,
                1f);
        ImageIcon icon = GameConstants.getCritterSprite(species, legFrame, antennaFrame);
        drawScaledSprite(g2d, icon, centerX, centerY, CRITTER_SPRITE_SCALE, faceAngle);
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
