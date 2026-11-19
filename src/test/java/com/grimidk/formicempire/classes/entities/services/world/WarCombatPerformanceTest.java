package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.shared.BattleNumericEvent;
import com.grimidk.formicempire.classes.entities.services.shared.BattleNumericFeed;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarCombatPerformanceTest {

    @AfterEach
    void tearDown() {
        WarCreatureCombatService.clearAll();
    }

    @Test
    void warServiceIndexesTrackDeclaredAndHistoricWarsAcrossLifecycleAndSaveLoad() {
        World world = new World();
        Dynasty a = new Dynasty(1, "A", false, GameConstants.SPECIES_OMNI);
        Dynasty b = new Dynasty(2, "B", false, GameConstants.SPECIES_OMNI);
        Dynasty c = new Dynasty(3, "C", false, GameConstants.SPECIES_OMNI);
        world.registerDynasty(a);
        world.registerDynasty(b);
        world.registerDynasty(c);

        WarService wars = world.getWarService();
        assertEquals(0, wars.countWarsDeclaredBy(1));
        assertFalse(wars.hasWarHistoryForDynasty(2));

        War w1 = wars.beginWar(a, b);
        War w2 = wars.beginWar(a, c);
        assertEquals(2, wars.countWarsDeclaredBy(1));
        assertEquals(0, wars.countWarsDeclaredBy(2));
        assertFalse(wars.hasWarHistoryForDynasty(2));

        wars.concludeWar(w1, 1, LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);
        assertTrue(wars.hasWarHistoryForDynasty(1));
        assertTrue(wars.hasWarHistoryForDynasty(2));
        assertFalse(wars.hasWarHistoryForDynasty(3));
        assertEquals(2, wars.countWarsDeclaredBy(1));

        List<Savefile.SavedWar> saved = wars.toSavedWars();
        World reloaded = new World();
        reloaded.registerDynasty(a);
        reloaded.registerDynasty(b);
        reloaded.registerDynasty(c);
        reloaded.getWarService().loadFromSave(saved);

        assertEquals(2, reloaded.getWarService().countWarsDeclaredBy(1));
        assertTrue(reloaded.getWarService().hasWarHistoryForDynasty(1));
        assertTrue(reloaded.getWarService().hasWarHistoryForDynasty(2));
        assertFalse(reloaded.getWarService().hasWarHistoryForDynasty(3));
        assertNotNull(w2);
    }

    @Test
    void battleNumericFeedRingBufferCapsAtMaxEvents() {
        BattleNumericFeed feed = new BattleNumericFeed();
        for (int i = 0; i < 2100; i++) {
            feed.recordD6Roll(i, true, 4, 3, false, 5f, false, "Bite", null, GameConstants.BATTLE_LINE_INFANTRY);
        }
        assertEquals(2100L, feed.getLatestSequence());
        BattleNumericEvent latest = feed.getLatestEvent();
        assertNotNull(latest);
        assertEquals(2100L, latest.sequence());
        List<BattleNumericEvent> allRetained = feed.pollEventsSince(0L);
        assertEquals(2048, allRetained.size());
        assertEquals(53L, allRetained.get(0).sequence());
    }

    @Test
    void committedAntTrackingUpdatesOnStartClearAndClearAll() {
        World world = new World();
        Dynasty atk = new Dynasty(1, "Atk", true, GameConstants.SPECIES_OMNI);
        Dynasty def = new Dynasty(2, "Def", false, GameConstants.SPECIES_OMNI);
        atk.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        def.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);

        Colony ac = new Colony(1, "AC", true);
        Colony dc = new Colony(2, "DC", false);
        atk.addColony(ac);
        def.addColony(dc);

        List<Ant> atkSoldiers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Ant s = new Ant(ac, GameConstants.CLASS_SOLDIER);
            s.setRole(GameConstants.ROLE_WARRIOR);
            atkSoldiers.add(s);
        }
        ac.getSoldiers().addAll(atkSoldiers);
        ac.setWarAssignedRoleCount(GameConstants.ROLE_WARRIOR, 5);

        for (int i = 0; i < 5; i++) {
            Ant s = new Ant(dc, GameConstants.CLASS_SOLDIER);
            s.setRole(GameConstants.ROLE_WARRIOR);
            dc.getSoldiers().add(s);
        }
        dc.setWarAssignedRoleCount(GameConstants.ROLE_WARRIOR, 5);

        Hex h1 = new Hex();
        h1.setQ(0);
        h1.setR(0);
        h1.setBiome(GameConstants.BIOME_PLAINS);
        h1.setColony(ac);
        Hex h2 = new Hex();
        h2.setQ(1);
        h2.setR(0);
        h2.setBiome(GameConstants.BIOME_PLAINS);
        h2.setColony(dc);
        h1.setSouthEast(h2);
        h2.setNorthWest(h1);
        world.setHexes(new ArrayList<>(List.of(h1, h2)));
        world.registerDynasty(atk);
        world.registerDynasty(def);

        War war = world.getWarService().beginWar(atk, def);
        WarBattleState state = WarCreatureCombatService.startBorderBattle(war, atk, def);
        assertNotNull(state);
        assertTrue(WarCreatureCombatService.isAntCommittedToBattle(atkSoldiers.get(0)));

        WarCreatureCombatService.clear(war);
        assertFalse(WarCreatureCombatService.isAntCommittedToBattle(atkSoldiers.get(0)));

        WarCreatureCombatService.startBorderBattle(war, atk, def);
        assertTrue(WarCreatureCombatService.isAntCommittedToBattle(atkSoldiers.get(0)));
        WarCreatureCombatService.clearAll();
        assertFalse(WarCreatureCombatService.isAntCommittedToBattle(atkSoldiers.get(0)));
    }
}
