package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyManagementDialogTest {

    @Test
    void orderDiplomacyDynastiesSortsOnlyDiscoveredByMilitaryPower() {
        Dynasty discLow = new Dynasty(10, "Alpha", false, GameConstants.SPECIES_OMNI);
        discLow.setMilitaryPower(50);
        Dynasty discHigh = new Dynasty(11, "Beta", false, GameConstants.SPECIES_OMNI);
        discHigh.setMilitaryPower(500);
        Dynasty discMid = new Dynasty(12, "Gamma", false, GameConstants.SPECIES_OMNI);
        discMid.setMilitaryPower(200);

        Dynasty hiddenStrongest = new Dynasty(2, "Hidden Strong", false, GameConstants.SPECIES_OMNI);
        hiddenStrongest.setMilitaryPower(9999);
        Dynasty hiddenWeakest = new Dynasty(1, "Hidden Weak", false, GameConstants.SPECIES_OMNI);
        hiddenWeakest.setMilitaryPower(1);

        Set<Integer> discoveredIds = Set.of(10, 11, 12);
        List<Dynasty> input = List.of(hiddenStrongest, discLow, discHigh, hiddenWeakest, discMid);

        Comparator<Dynasty> desc = Comparator.comparingInt(Dynasty::getMilitaryPower).reversed()
                .thenComparing(Dynasty::getName, String.CASE_INSENSITIVE_ORDER);
        List<Dynasty> descResult = DynastyManagementDialog.orderDiplomacyDynasties(
                input, d -> discoveredIds.contains(d.getId()), desc);
        assertEquals(List.of(discHigh, discMid, discLow, hiddenWeakest, hiddenStrongest), descResult);

        Comparator<Dynasty> asc = Comparator.comparingInt(Dynasty::getMilitaryPower)
                .thenComparing(Dynasty::getName, String.CASE_INSENSITIVE_ORDER);
        List<Dynasty> ascResult = DynastyManagementDialog.orderDiplomacyDynasties(
                input, d -> discoveredIds.contains(d.getId()), asc);
        assertEquals(List.of(discLow, discMid, discHigh, hiddenWeakest, hiddenStrongest), ascResult);
    }

    @Test
    void excludeDeadDynastiesFiltersDefeatedInDiplomacyAndMapLegend() {
        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        Dynasty alive = new Dynasty(2, "Alive", false, GameConstants.SPECIES_FIRE);
        Colony aliveCap = new Colony(2, "Alive Prime", false);
        aliveCap.setDynasty(alive);
        alive.addColony(aliveCap);
        aliveCap.getQueens().add(new Ant(aliveCap, GameConstants.CLASS_QUEEN));

        Dynasty dead = new Dynasty(3, "Dead", false, GameConstants.SPECIES_LEAFCUTTER);
        dead.setDefeated(true);

        assertFalse(DynastyManagementDialog.shouldIncludeDynastyInDiplomacy(player, player, true));
        assertTrue(DynastyManagementDialog.shouldIncludeDynastyInDiplomacy(player, alive, true));
        assertFalse(DynastyManagementDialog.shouldIncludeDynastyInDiplomacy(player, dead, true));
        assertTrue(DynastyManagementDialog.shouldIncludeDynastyInDiplomacy(player, dead, false));

        assertTrue(MapDialog.shouldIncludeDynastyInLegend(player, true));
        assertTrue(MapDialog.shouldIncludeDynastyInLegend(alive, true));
        assertFalse(MapDialog.shouldIncludeDynastyInLegend(dead, true));
        assertTrue(MapDialog.shouldIncludeDynastyInLegend(dead, false));
    }
}
