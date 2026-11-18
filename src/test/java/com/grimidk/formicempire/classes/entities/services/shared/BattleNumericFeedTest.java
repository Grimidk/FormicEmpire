package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class BattleNumericFeedTest {

    @Test
    void d6TargetMappingAndModifiersStayClampedBetweenOneAndSix() {
        assertEquals(1, GameConstants.SKILL_BASIC_BITE.getD6Target());
        assertEquals(2, GameConstants.SKILL_STINGING.getD6Target());
        assertEquals(3, GameConstants.SKILL_ACID_SPITTING.getD6Target());
        assertEquals(4, GameConstants.SKILL_ACID_ARTILLERY.getD6Target());
        assertEquals(6, GameNumbers.d6TargetFromAccuracy(0.10f));

        assertEquals(1, GameNumbers.computeD6HitTarget(1, 0.25f, 1.0f, false, 0f));
        assertEquals(3, GameNumbers.computeD6HitTarget(4, 0.15f, 1.0f, false, 0f));
        assertEquals(6, GameNumbers.computeD6HitTarget(4, 0f, 0.50f, true, 20f));
    }

    @Test
    void feedRecordsD6RollsHitsMissesAndSequences() {
        BattleNumericFeed feed = new BattleNumericFeed();

        BattleNumericEvent miss = feed.recordD6Roll(
                1, true, 3, 4, false, 25f, false,
                GameConstants.SKILL_ACID_ARTILLERY.getName(),
                GameConstants.SKILL_ACID_ARTILLERY.getIcon(),
                GameConstants.BATTLE_LINE_ARTILLERY);
        assertFalse(miss.hit());
        assertFalse(miss.dodged());
        assertEquals(0f, miss.damage());
        assertEquals(3, miss.d6Roll());
        assertEquals(4, miss.d6Target());
        assertEquals(GameConstants.BATTLE_LINE_ARTILLERY, miss.battleLine());

        BattleNumericEvent dodged = feed.recordD6Roll(
                1, false, 2, 3, true, 12f, false,
                GameConstants.SKILL_STINGING.getName(),
                GameConstants.SKILL_STINGING.getIcon(),
                GameConstants.BATTLE_LINE_INFANTRY);
        assertFalse(dodged.hit());
        assertTrue(dodged.dodged());
        assertEquals(0f, dodged.damage());

        BattleNumericEvent hit = feed.recordD6Roll(
                1, false, 4, 4, false, 18.5f, true,
                GameConstants.SKILL_BASIC_BITE.getName(),
                GameConstants.SKILL_BASIC_BITE.getIcon(),
                GameConstants.BATTLE_LINE_INFANTRY);
        assertTrue(hit.hit());
        assertTrue(hit.kill());
        assertEquals(18.5f, hit.damage());

        List<BattleNumericEvent> polled = feed.pollEventsSince(miss.sequence());
        assertEquals(2, polled.size());
        assertEquals(dodged.sequence(), polled.get(0).sequence());
        assertEquals(hit.sequence(), polled.get(1).sequence());
    }
}

