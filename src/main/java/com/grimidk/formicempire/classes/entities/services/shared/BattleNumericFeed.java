package com.grimidk.formicempire.classes.entities.services.shared;

import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;

import javax.swing.ImageIcon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class BattleNumericFeed {

    private static final int MAX_EVENTS = 2048;

    private final ArrayDeque<BattleNumericEvent> events = new ArrayDeque<>(MAX_EVENTS + 1);
    private long nextSequence = 1L;

    public synchronized BattleNumericEvent recordD6Roll(
            int tickIndex,
            boolean attackerSide,
            int d6Roll,
            int d6Target,
            boolean dodged,
            float damage,
            boolean kill,
            String skillName,
            ImageIcon skillIcon,
            BattleLine battleLine) {
        int clampedRoll = Math.max(1, Math.min(6, d6Roll));
        int clampedTarget = Math.max(1, Math.min(6, d6Target));
        boolean hit = clampedRoll >= clampedTarget;
        BattleNumericEvent event = new BattleNumericEvent(
                nextSequence++,
                tickIndex,
                attackerSide,
                clampedRoll,
                clampedTarget,
                hit,
                !hit && dodged,
                hit ? Math.max(0f, damage) : 0f,
                hit && kill,
                skillName,
                skillIcon,
                battleLine);
        events.addLast(event);
        if (events.size() > MAX_EVENTS) {
            events.pollFirst();
        }
        return event;
    }

    public synchronized List<BattleNumericEvent> pollEventsSince(long lastSequence) {
        if (events.isEmpty() || lastSequence >= nextSequence - 1L) {
            return List.of();
        }
        List<BattleNumericEvent> result = new ArrayList<>();
        for (BattleNumericEvent event : events) {
            if (event.sequence() > lastSequence) {
                result.add(event);
            }
        }
        return result;
    }

    public synchronized BattleNumericEvent getLatestEvent() {
        return events.peekLast();
    }

    public synchronized long getLatestSequence() {
        return nextSequence - 1L;
    }

    public synchronized void clear() {
        events.clear();
    }
}
