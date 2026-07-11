package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarServiceTest {

    private Dynasty player;
    private Dynasty neighbor;
    private World world;

    @BeforeEach
    void setUp() {
        player = new Dynasty(1, "Meat Dynasty", true, GameConstants.SPECIES_OMNI);
        neighbor = new Dynasty(2, "Crystal Dynasty", false, GameConstants.SPECIES_OMNI);
        world = buildBorderWorld();
    }

    @Test
    void beginWarCreatesRecordWithGeneratedName() {
        War war = world.getWarService().beginWar(player, neighbor);

        assertNotNull(war);
        assertEquals(1, world.getWarService().getActiveWars().size());
        assertTrue(war.getDisplayName().contains("Meat"));
        assertTrue(war.getDisplayName().contains("Crystal"));
        assertTrue(war.getDisplayName().toLowerCase().contains("war")
                || war.getDisplayName().toLowerCase().contains("guerra")
                || war.getDisplayName().contains("First")
                || war.getDisplayName().contains("Primera"));
    }

    @Test
    void beginWarIsIdempotent() {
        War first = world.getWarService().beginWar(player, neighbor);
        War second = world.getWarService().beginWar(player, neighbor);
        assertEquals(first, second);
        assertEquals(1, world.getWarService().getActiveWars().size());
    }

    @Test
    void getWarsForDynastyReturnsOpponentWarsOnly() {
        world.getWarService().beginWar(player, neighbor);

        List<War> playerWars = world.getWarService().getWarsForDynasty(player.getId());
        assertEquals(1, playerWars.size());
        assertEquals(neighbor.getId(), playerWars.get(0).getOtherDynastyId(player.getId()));
    }

    @Test
    void endWarClearsDiplomaticWarStateAndMovesToHistory() {
        player.getDiplomacyService().applyWar(neighbor, null, world);
        War war = world.getWarService().findActiveWar(player.getId(), neighbor.getId());
        assertNotNull(war);

        world.getWarService().endWar(war);

        assertFalse(player.getDiplomacyService().isAtWarWith(neighbor));
        assertTrue(world.getWarService().getActiveWars().isEmpty());
        assertEquals(1, world.getWarService().getHistoricWarsForDynasty(player.getId()).size());
        assertFalse(world.getWarService().getHistoricWarsForDynasty(player.getId()).get(0).isActive());
    }

    @Test
    void syncFromDynastiesBackfillsMissingRecords() {
        player.getDiplomacyService().applyWar(neighbor);
        assertTrue(world.getWarService().getActiveWars().isEmpty());

        world.getWarService().syncFromDynasties();

        assertEquals(1, world.getWarService().getActiveWars().size());
    }

    @Test
    void saveRoundTripPreservesWarRecords() {
        War war = world.getWarService().beginWar(player, neighbor);
        assertNotNull(war);

        List<Savefile.SavedWar> saved = world.getWarService().toSavedWars();
        World reloaded = new World();
        reloaded.getDynastys().add(player);
        reloaded.getDynastys().add(neighbor);
        reloaded.getWarService().loadFromSave(saved);

        War loaded = reloaded.getWarService().findActiveWar(player.getId(), neighbor.getId());
        assertNotNull(loaded);
        assertEquals(war.getId(), loaded.getId());
        assertEquals(war.getDisplayName(), loaded.getDisplayName());
        assertEquals(war.getStartedWorldMonth(), loaded.getStartedWorldMonth());
    }

    @Test
    void concludeWarSnapshotsDynastyNames() {
        War war = world.getWarService().beginWar(player, neighbor);
        world.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);

        War historic = world.getWarService().getHistoricWarsForDynasty(player.getId()).get(0);
        assertEquals("Meat Dynasty", historic.getDynastyNameA());
        assertEquals("Crystal Dynasty", historic.getDynastyNameB());
        assertEquals("Meat Dynasty", historic.getWinnerDynastyName());
    }

    @Test
    void resolveWinnerDisplayNameFallsBackToSnapshot() {
        War war = world.getWarService().beginWar(player, neighbor);
        world.getWarService().concludeWar(war, neighbor.getId(), LanguageStrings.WAR_CONCLUSION_DEFEAT);

        War historic = world.getWarService().getHistoricWarsForDynasty(player.getId()).get(0);
        world.getDynastys().remove(neighbor);

        assertEquals("Crystal Dynasty", world.getWarService().resolveWinnerDisplayName(historic, player));
    }

    @Test
    void resolveWinnerDisplayNameUsesDynastyNameForPlayerWinner() {
        War war = world.getWarService().beginWar(player, neighbor);
        world.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);

        War historic = world.getWarService().getHistoricWarsForDynasty(player.getId()).get(0);

        assertEquals("Meat Dynasty", world.getWarService().resolveWinnerDisplayName(historic, player));
    }

    @Test
    void loadFromSaveIgnoresInvalidWarRecords() {
        Savefile.SavedWar invalid = new Savefile.SavedWar();
        invalid.endedWorldMonth = 12;
        invalid.conclusionKey = LanguageStrings.WAR_CONCLUSION_UNKNOWN;

        War war = world.getWarService().beginWar(player, neighbor);
        world.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);

        List<Savefile.SavedWar> saved = new ArrayList<>();
        saved.add(invalid);
        saved.addAll(world.getWarService().toSavedWars());

        World reloaded = new World();
        reloaded.getDynastys().add(player);
        reloaded.getDynastys().add(neighbor);
        reloaded.getWarService().loadFromSave(saved);
        reloaded.getWarService().pruneInvalidWars();

        assertEquals(1, reloaded.getWarService().getHistoricWarsForDynasty(player.getId()).size());
        assertEquals(1, reloaded.getWarService().toSavedWars().size());
    }

    @Test
    void saveRoundTripPreservesHistoricWarRecords() {
        War war = world.getWarService().beginWar(player, neighbor);
        world.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);

        List<Savefile.SavedWar> saved = world.getWarService().toSavedWars();
        assertEquals(1, saved.size());
        assertTrue(saved.get(0).endedWorldMonth >= 0);

        World reloaded = new World();
        reloaded.getDynastys().add(player);
        reloaded.getDynastys().add(neighbor);
        reloaded.getWarService().loadFromSave(saved);

        assertTrue(reloaded.getWarService().getActiveWars().isEmpty());
        assertEquals(1, reloaded.getWarService().getHistoricWarsForDynasty(player.getId()).size());
        assertFalse(reloaded.getWarService().getHistoricWarsForDynasty(player.getId()).get(0).isActive());
    }

    @Test
    void pruneInvalidWarsArchivesOrphanedActiveRecords() {
        War war = world.getWarService().beginWar(player, neighbor);
        player.getDiplomacyService().clearWarWith(neighbor, null);

        world.getWarService().pruneInvalidWars();

        assertTrue(world.getWarService().getActiveWars().isEmpty());
        assertEquals(1, world.getWarService().getHistoricWarsForDynasty(player.getId()).size());
        assertFalse(world.getWarService().getHistoricWarsForDynasty(player.getId()).get(0).isActive());
    }

    @Test
    void offerPeaceRequiresWinningStanding() {
        War war = world.getWarService().beginWar(player, neighbor);
        player.getDiplomacyService().applyWar(neighbor, null, world);

        assertFalse(world.getWarService().canOfferPeace(war, player));

        player.setActiveMilitaryPower(10_000);
        player.setMilitaryPower(10_000);
        neighbor.setActiveMilitaryPower(100);
        neighbor.setMilitaryPower(100);

        assertTrue(world.getWarService().canOfferPeace(war, player));
    }

    @Test
    void secondWarBetweenSamePairUsesNextOrdinal() {
        War first = world.getWarService().beginWar(player, neighbor);
        world.getWarService().concludeWar(first, player.getId(), LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);

        War second = world.getWarService().beginWar(player, neighbor);
        assertNotNull(second);
        assertFalse(second.getDisplayName().equals(first.getDisplayName()));
    }

    @Test
    void absoluteVictoryInheritsLoserAssimilationsWithoutDuplicates() {
        Dynasty leafcutterDynasty = new Dynasty(2, "Leafcutter Dynasty", false, GameConstants.SPECIES_LEAFCUTTER);
        World leafWorld = buildBorderWorld(leafcutterDynasty);
        leafcutterDynasty.absorbSpecies(GameConstants.SPECIES_PHARAOH.getId());
        leafcutterDynasty.completeAssimilation(GameUnlocks.ASSIMILATION_LEAFCUTTER);

        War war = leafWorld.getWarService().beginWar(player, leafcutterDynasty);
        leafWorld.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY);

        assertTrue(player.getDefeatedSpeciesIds().contains(GameConstants.SPECIES_LEAFCUTTER.getId()));
        assertTrue(player.getDefeatedSpeciesIds().contains(GameConstants.SPECIES_PHARAOH.getId()));
        assertTrue(player.isAssimilationCompleted(GameUnlocks.ASSIMILATION_LEAFCUTTER));
        assertEquals(2, player.getDefeatedSpeciesIds().size());
    }

    @Test
    void peaceTreatyDoesNotTransferAssimilations() {
        neighbor.absorbSpecies(GameConstants.SPECIES_MARAUDER.getId());
        neighbor.completeAssimilation(GameUnlocks.ASSIMILATION_MARAUDER);

        War war = world.getWarService().beginWar(player, neighbor);
        world.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);

        assertFalse(player.getDefeatedSpeciesIds().contains(GameConstants.SPECIES_MARAUDER.getId()));
        assertFalse(player.isAssimilationCompleted(GameUnlocks.ASSIMILATION_MARAUDER));
    }

    @Test
    void peaceTreatyAppliesWasAtWarModifier() {
        player.setDiplomaticReputation(neighbor.getId(), 50);
        neighbor.setDiplomaticReputation(player.getId(), 50);
        player.getDiplomacyService().applyWar(neighbor, null, world);

        War war = world.getWarService().findActiveWar(player.getId(), neighbor.getId());
        world.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);

        assertEquals(GameConstants.DIPLO_MODIFIER_WAS_AT_WAR.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
        assertEquals(12, player.getDiplomacyService().getWasAtWarModifierMonthsRemaining(neighbor, world));
        assertEquals(GameConstants.DIPLO_MODIFIER_WAS_AT_WAR.getDurationDays(),
                player.getDiplomaticModifierRemainingDays(
                        neighbor.getId(), GameConstants.DIPLO_MODIFIER_WAS_AT_WAR.getNameKey()));
    }

    @Test
    void defeatConclusionDoesNotApplyWasAtWarModifier() {
        player.getDiplomacyService().applyWar(neighbor, null, world);
        War war = world.getWarService().findActiveWar(player.getId(), neighbor.getId());
        world.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_DEFEAT);

        assertNull(player.getDiplomaticModifierKey(neighbor.getId()));
    }

    @Test
    void wasAtWarModifierExpiresAfterOneYear() {
        world.setYear(0);
        world.setMonth(0);
        player.setDiplomaticReputation(neighbor.getId(), 50);
        neighbor.setDiplomaticReputation(player.getId(), 50);
        player.getDiplomacyService().applyWar(neighbor, null, world);
        War war = world.getWarService().findActiveWar(player.getId(), neighbor.getId());
        world.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);

        assertEquals(GameConstants.DIPLO_MODIFIER_WAS_AT_WAR.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
        player.setDiplomaticReputation(neighbor.getId(), 40);
        neighbor.setDiplomaticReputation(player.getId(), 40);

        advanceWorldDays(GameConstants.DIPLO_MODIFIER_WAS_AT_WAR.getDurationDays());
        world.setYear(1);
        world.setMonth(0);
        player.getDiplomacyService().getEffectiveDiplomaticReputation(neighbor, world);

        assertNull(player.getDiplomaticModifierKey(neighbor.getId()));
        assertEquals(60, player.getDiplomaticReputation(neighbor.getId()));
    }

    @Test
    void absoluteVictoryColonizesCapturedColoniesFromCapitalBreederPairs() {
        player.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        player.unlockUpgrade(GameUnlocks.ROLE_BREEDER);

        Colony capital = player.getCapital();
        capital.getDrones().add(new Ant(capital, GameConstants.TYPE_DRONE));
        Ant breeder = new Ant(capital, GameConstants.TYPE_PRINCESS);
        breeder.setRole(GameConstants.ROLE_BREEDER);
        capital.getPrincesses().add(breeder);

        Colony captured = neighbor.getCapital();
        captured.getQueens().clear();
        captured.setDynasty(player);
        neighbor.removeColony(captured);

        War war = world.getWarService().beginWar(player, neighbor);
        war.recordCapture(player.getId(), captured.getId());

        int dronesBefore = capital.getDrones().size();
        long breedersBefore = capital.getPrincesses().stream()
                .filter(p -> p.getRole() == GameConstants.ROLE_BREEDER).count();

        world.getWarService().concludeWar(war, player.getId(), LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY);

        assertEquals(1, captured.getQueens().size());
        assertEquals(dronesBefore - 1, capital.getDrones().size());
        assertEquals(breedersBefore - 1, capital.getPrincesses().stream()
                .filter(p -> p.getRole() == GameConstants.ROLE_BREEDER).count());
    }

    private World buildBorderWorld() {
        return buildBorderWorld(neighbor);
    }

    private World buildBorderWorld(Dynasty borderingDynasty) {
        World world = new World();
        Hex playerHex = new Hex();
        Hex neighborHex = new Hex();
        playerHex.setQ(0);
        playerHex.setR(0);
        neighborHex.setQ(1);
        neighborHex.setR(0);
        playerHex.setNorthEast(neighborHex);
        neighborHex.setSouthWest(playerHex);

        Colony playerColony = new Colony(10, "Capital", true);
        Colony neighborColony = new Colony(11, "Border", false);
        player.addColony(playerColony);
        borderingDynasty.addColony(neighborColony);
        player.setCapital(playerColony);
        borderingDynasty.setCapital(neighborColony);
        playerColony.setDynasty(player);
        neighborColony.setDynasty(borderingDynasty);
        playerHex.setColony(playerColony);
        neighborHex.setColony(neighborColony);
        ensureWarPopulation(player, playerColony);
        ensureWarPopulation(borderingDynasty, neighborColony);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(playerHex);
        hexes.add(neighborHex);
        world.setHexes(hexes);
        world.getDynastys().add(player);
        world.getDynastys().add(borderingDynasty);
        return world;
    }

    private static void ensureWarPopulation(Dynasty dynasty, Colony colony) {
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        int needed = GameConstants.WAR_DECLARATION_MIN_POPULATION
                - dynasty.getStatService().getTotalPopulation(dynasty);
        for (int i = 0; i < needed; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        for (int i = 0; i < 20; i++) {
            colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        }
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 10);
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 10);
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
    }

    private void advanceWorldDays(int days) {
        TradeManager tradeManager = new TradeManager();
        for (int i = 0; i < days; i++) {
            player.runDailyJobs(world, tradeManager);
            neighbor.runDailyJobs(world, tradeManager);
        }
    }
}
