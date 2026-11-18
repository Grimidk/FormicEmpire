package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.entities.services.shared.BattleNumericEvent;
import com.grimidk.formicempire.classes.entities.services.shared.BattleNumericFeed;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiResourceLoader;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BattleNumericOverlay {

    static final int COLLAPSED_VISIBLE_ROWS = 2;

    private static final int[] ROLL_SEQUENCE = {2, 6, 3, 5, 4, 1};
    private static final float ROLL_STEP_SEC = 0.06f;

    private static final int ICON_SIZE = 16;
    private static final int DIE_BOX = 20;
    private static final int SLOT_GAP = 6;
    private static final int ROW_H = 22;
    private static final int ROW_GAP = 3;
    private static final int TRAY_TOP_Y = 4;
    private static final int TRAY_BOTTOM_PAD = 4;
    private static final int SIDE_BAR_W = 4;

    static final int COLLAPSED_HEIGHT =
            TRAY_TOP_Y + COLLAPSED_VISIBLE_ROWS * ROW_H + (COLLAPSED_VISIBLE_ROWS - 1) * ROW_GAP + TRAY_BOTTOM_PAD;

    private static final Color ROLL_BORDER = new Color(255, 220, 100);
    private static final Color HIT_BORDER = new Color(95, 210, 130);
    private static final Color HIT_FILL = new Color(40, 110, 65, 160);
    private static final Color DODGE_BORDER = new Color(80, 200, 255);
    private static final Color DODGE_FILL = new Color(30, 95, 135, 160);
    private static final Color MISS_BORDER = new Color(115, 125, 140);
    private static final Color MISS_FILL = new Color(20, 24, 32, 180);
    private static final Color ATK_SIDE_COLOR = new Color(95, 185, 255);
    private static final Color DEF_SIDE_COLOR = new Color(255, 135, 95);

    private static final Image[] DICE_IMAGES = loadDiceImages();

    private static final class SkillSummary {
        final boolean attackerSide;
        final BattleLine battleLine;
        final String skillName;
        final ImageIcon skillIcon;
        final int[] faceCounts = new int[6];
        final int[] hitCounts = new int[6];
        final int[] dodgeCounts = new int[6];
        int totalRolls;

        SkillSummary(boolean attackerSide, BattleLine battleLine, String skillName, ImageIcon skillIcon) {
            this.attackerSide = attackerSide;
            this.battleLine = battleLine;
            this.skillName = skillName;
            this.skillIcon = skillIcon;
        }

        void add(BattleNumericEvent event) {
            int idx = Math.max(0, Math.min(5, event.d6Roll() - 1));
            faceCounts[idx]++;
            if (event.hit()) {
                hitCounts[idx]++;
            } else if (event.dodged()) {
                dodgeCounts[idx]++;
            }
            totalRolls++;
        }
    }

    private record SkillHitbox(Rectangle bounds, String skillName) {
    }

    private final Map<String, SkillSummary> atkSummaries = new LinkedHashMap<>();
    private final Map<String, SkillSummary> defSummaries = new LinkedHashMap<>();
    private final List<SkillHitbox> skillHitboxes = new ArrayList<>();
    private BattleNumericFeed currentFeed;
    private long lastSequence;
    private int currentTickIndex = -1;
    private int revealedTickIndex = -1;
    private boolean pendingRevealAfterRoll;
    private boolean rollingPhase;
    private float rollAnimAccumSec;
    private int rollSequenceOffset;

    private static Image[] loadDiceImages() {
        Image[] images = new Image[6];
        for (int i = 0; i < 6; i++) {
            Icon icon = UiResourceLoader.loadDialogIcon(
                    BattleNumericOverlay.class, "/icons/ui/Dice" + (i + 1) + ".png", ICON_SIZE);
            images[i] = (icon instanceof ImageIcon ii) ? ii.getImage() : null;
        }
        return images;
    }

    void reset() {
        currentFeed = null;
        atkSummaries.clear();
        defSummaries.clear();
        skillHitboxes.clear();
        lastSequence = 0L;
        currentTickIndex = -1;
        revealedTickIndex = -1;
        pendingRevealAfterRoll = false;
        rollingPhase = false;
        rollAnimAccumSec = 0f;
        rollSequenceOffset = 0;
    }

    String getTooltipAt(Point p) {
        if (p == null) {
            return null;
        }
        for (SkillHitbox hb : skillHitboxes) {
            if (hb.bounds().contains(p)) {
                return hb.skillName();
            }
        }
        return null;
    }

    void update(BattleNumericFeed feed, float deltaSec, int worldMinute) {
        update(feed, deltaSec, worldMinute, -1);
    }

    void update(BattleNumericFeed feed, float deltaSec, int worldMinute, int stateTickIndex) {
        if (feed != currentFeed || (feed != null && feed.getLatestSequence() < lastSequence)) {
            reset();
            currentFeed = feed;
        }
        if (feed != null) {
            List<BattleNumericEvent> fresh = feed.pollEventsSince(lastSequence);
            for (BattleNumericEvent event : fresh) {
                lastSequence = Math.max(lastSequence, event.sequence());
                if (event.tickIndex() != currentTickIndex) {
                    currentTickIndex = event.tickIndex();
                    atkSummaries.clear();
                    defSummaries.clear();
                }
                Map<String, SkillSummary> targetMap = event.attackerSide() ? atkSummaries : defSummaries;
                int lineOrd = battleLineOrder(event.battleLine());
                String skillKey = event.skillName() != null ? event.skillName() : "";
                String key = lineOrd + ":" + skillKey;
                SkillSummary summary = targetMap.computeIfAbsent(key,
                        k -> new SkillSummary(event.attackerSide(), event.battleLine(), event.skillName(), event.skillIcon()));
                summary.add(event);
            }
        }
        if (stateTickIndex >= 0 && stateTickIndex != currentTickIndex) {
            currentTickIndex = stateTickIndex;
            atkSummaries.clear();
            defSummaries.clear();
        }

        if (worldMinute >= 30) {
            pendingRevealAfterRoll = true;
            rollingPhase = true;
        } else {
            if (currentTickIndex != revealedTickIndex) {
                pendingRevealAfterRoll = false;
                revealedTickIndex = currentTickIndex;
                rollingPhase = false;
            } else if (pendingRevealAfterRoll && stateTickIndex >= 0) {
                rollingPhase = true;
            } else {
                pendingRevealAfterRoll = false;
                rollingPhase = false;
            }
        }

        if (rollingPhase && deltaSec > 0f) {
            rollAnimAccumSec += deltaSec;
            while (rollAnimAccumSec >= ROLL_STEP_SEC) {
                rollAnimAccumSec -= ROLL_STEP_SEC;
                rollSequenceOffset = (rollSequenceOffset + 1) % ROLL_SEQUENCE.length;
            }
        } else if (!rollingPhase) {
            rollAnimAccumSec = 0f;
        }
    }

    int getRequiredHeight(int panelWidth) {
        if ((atkSummaries.isEmpty() && defSummaries.isEmpty()) || panelWidth < 120) {
            return COLLAPSED_HEIGHT;
        }
        int atkRows = atkSummaries.size();
        int defRows = defSummaries.size();
        int maxRows = Math.max(1, Math.max(atkRows, defRows));
        int h = TRAY_TOP_Y + maxRows * ROW_H + Math.max(0, maxRows - 1) * ROW_GAP + TRAY_BOTTOM_PAD;
        return Math.max(COLLAPSED_HEIGHT, h);
    }

    private static int battleLineOrder(BattleLine line) {
        if (line == null) return 99;
        if (line == GameConstants.BATTLE_LINE_INFANTRY) return 0;
        if (line == GameConstants.BATTLE_LINE_ARTILLERY) return 1;
        if (line == GameConstants.BATTLE_LINE_AIR_SUPPORT) return 2;
        return 50;
    }

    void paint(Graphics2D g2d, int panelWidth, int maxVisibleRows) {
        skillHitboxes.clear();
        if (g2d == null || (atkSummaries.isEmpty() && defSummaries.isEmpty()) || panelWidth < 120 || maxVisibleRows <= 0) {
            return;
        }
        Stroke oldStroke = g2d.getStroke();
        Font oldFont = g2d.getFont();
        g2d.setFont(AssetStyles.FONT_BOLD.deriveFont(11f));

        List<SkillSummary> atkRows = sortedRows(atkSummaries);
        List<SkillSummary> defRows = sortedRows(defSummaries);

        int halfW = panelWidth / 2;
        paintSideRows(g2d, atkRows, 4, halfW - 6, maxVisibleRows);
        paintSideRows(g2d, defRows, halfW + 6, panelWidth - 4, maxVisibleRows);

        g2d.setFont(oldFont);
        g2d.setStroke(oldStroke);
    }

    private static List<SkillSummary> sortedRows(Map<String, SkillSummary> map) {
        List<SkillSummary> rows = new ArrayList<>(map.values());
        rows.sort(Comparator.comparingInt(r -> battleLineOrder(r.battleLine)));
        return rows;
    }

    private void paintSideRows(
            Graphics2D g2d,
            List<SkillSummary> rows,
            int minX,
            int maxX,
            int maxVisibleRows) {
        FontMetrics fm = g2d.getFontMetrics();
        int count = Math.min(maxVisibleRows, rows.size());
        int y = TRAY_TOP_Y;
        int diceStartX = minX + SIDE_BAR_W + 4 + ICON_SIZE + 6;
        for (int r = 0; r < count; r++) {
            SkillSummary row = rows.get(r);
            g2d.setColor(row.attackerSide ? ATK_SIDE_COLOR : DEF_SIDE_COLOR);
            g2d.fillRoundRect(minX, y + 2, SIDE_BAR_W, ROW_H - 4, 2, 2);

            int iconX = minX + SIDE_BAR_W + 4;
            int iconY = y + (ROW_H - ICON_SIZE) / 2;
            if (row.skillIcon != null && row.skillIcon.getImage() != null) {
                g2d.drawImage(row.skillIcon.getImage(), iconX, iconY, ICON_SIZE, ICON_SIZE, null);
                if (row.skillName != null && !row.skillName.isEmpty()) {
                    skillHitboxes.add(new SkillHitbox(new Rectangle(iconX, iconY, ICON_SIZE, ICON_SIZE), row.skillName));
                }
            }

            int cursorX = diceStartX;
            if (rollingPhase) {
                int shown = ROLL_SEQUENCE[(rollSequenceOffset + r) % ROLL_SEQUENCE.length];
                drawCountDieSlot(g2d, fm, cursorX, y, maxX, row.totalRolls, shown, MISS_FILL, ROLL_BORDER);
            } else {
                for (int face = 1; face <= 6; face++) {
                    int n = row.faceCounts[face - 1];
                    if (n <= 0) {
                        continue;
                    }
                    Color fillColor;
                    Color borderColor;
                    if (row.hitCounts[face - 1] > 0) {
                        fillColor = HIT_FILL;
                        borderColor = HIT_BORDER;
                    } else if (row.dodgeCounts[face - 1] > 0) {
                        fillColor = DODGE_FILL;
                        borderColor = DODGE_BORDER;
                    } else {
                        fillColor = MISS_FILL;
                        borderColor = MISS_BORDER;
                    }
                    int usedW = drawCountDieSlot(g2d, fm, cursorX, y, maxX, n, face, fillColor, borderColor);
                    if (usedW <= 0) {
                        break;
                    }
                    cursorX += usedW + SLOT_GAP;
                }
            }
            y += ROW_H + ROW_GAP;
        }
    }

    private static int drawCountDieSlot(
            Graphics2D g2d,
            FontMetrics fm,
            int x,
            int rowY,
            int maxX,
            int count,
            int face,
            Color fillColor,
            Color borderColor) {
        String countText = AssetStyles.formatNumber(count) + "x";
        int textW = fm.stringWidth(countText);
        int totalW = textW + 3 + DIE_BOX;
        if (x + totalW > maxX) {
            return 0;
        }
        int textY = rowY + ((ROW_H - fm.getHeight()) / 2) + fm.getAscent();
        g2d.setColor(Color.WHITE);
        g2d.drawString(countText, x, textY);

        int dieX = x + textW + 3;
        int boxY = rowY + (ROW_H - DIE_BOX) / 2;
        g2d.setColor(fillColor);
        g2d.fillRoundRect(dieX, boxY, DIE_BOX, DIE_BOX, 5, 5);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.4f));
        g2d.drawRoundRect(dieX, boxY, DIE_BOX, DIE_BOX, 5, 5);

        Image dieImg = DICE_IMAGES[Math.max(0, Math.min(5, face - 1))];
        if (dieImg != null) {
            g2d.drawImage(dieImg, dieX + (DIE_BOX - ICON_SIZE) / 2, boxY + (DIE_BOX - ICON_SIZE) / 2,
                    ICON_SIZE, ICON_SIZE, null);
        }
        return totalW;
    }
}
