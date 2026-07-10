package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AntSubtypeServiceTest {

    @Test
    void standardProfileCodeIs1111() {
        assertEquals(1111, AntSubtypeProfile.standard().getCode());
    }

    @Test
    void trapjawStingerProfileIs2121() {
        AntSubtypeProfile profile = AntSubtypeProfile.fromCode(2121);
        assertEquals(2, profile.getDigit(AntSubtypeSlot.HEAD));
        assertEquals(1, profile.getDigit(AntSubtypeSlot.TORSO));
        assertEquals(2, profile.getDigit(AntSubtypeSlot.ABDOMEN));
        assertEquals(1, profile.getDigit(AntSubtypeSlot.OTHER));
    }

    @Test
    void stingerAbdomenQuadruplesAntAttack() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        Colony colony = new Colony(1, "C", true);
        colony.setDynasty(dynasty);

        Ant standard = new Ant(colony, GameConstants.TYPE_SOLDIER);
        AntSubtypeService.applySubtypeStats(standard, colony);
        Ant ant = new Ant(colony, GameConstants.TYPE_SOLDIER);
        ant.setSubtypeProfile(AntSubtypeProfile.of(1, 1, 2, 1));
        AntSubtypeService.applySubtypeStats(ant, colony);

        assertEquals(standard.getAttack() * 4, ant.getAttack());
    }

    @Test
    void trapjawAttackStacksAdditivelyWithStinger() {
        AntSubtypeProfile profile = AntSubtypeProfile.of(2, 1, 2, 1);
        assertEquals(6f, AntSubtypeService.combinedAttackMult(profile), 0.0001f);
    }

    @Test
    void trapjawAloneTriplesAttack() {
        AntSubtypeProfile profile = AntSubtypeProfile.of(2, 1, 1, 1);
        assertEquals(3f, AntSubtypeService.combinedAttackMult(profile), 0.0001f);
    }

    @Test
    void doorheadQuintuplesDefense() {
        AntSubtypeProfile profile = AntSubtypeProfile.of(3, 1, 1, 1);
        assertEquals(5f, AntSubtypeService.combinedDefenseMult(profile), 0.0001f);
    }

    @Test
    void honeypotQuadruplesForagePower() {
        Colony colony = new Colony(2, "C", true);
        Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
        worker.setSubtypeProfile(AntSubtypeProfile.of(1, 1, 3, 1));
        assertEquals(4f, AntSubtypeService.forageMult(worker), 0.0001f);
        assertEquals(4, AntSubtypeService.forageCarrySlots(worker));
    }

    @Test
    void sumCollectingPowerUsesHoneypotMultiplier() {
        Colony colony = new Colony(3, "C", true);
        colony.setDynasty(new Dynasty(2, "D", true, GameConstants.SPECIES_OMNI));
        colony.getDynasty().unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
        worker.setSubtypeProfile(AntSubtypeProfile.of(1, 1, 3, 1));
        List<Ant> foragers = List.of(worker);
        assertEquals(4, AntSubtypeService.sumCollectingPower(colony, foragers));
    }

    @Test
    void npcNaturalSpeciesSetsFiftyPercentSubtypeRate() {
        Colony colony = new Colony(4, "Trap", false);
        AntSubtypeService.applyNaturalSpeciesSubtypeRates(colony, GameConstants.SPECIES_TRAPJAW);
        assertEquals(50f, colony.getSubtypeHatchRate(AntSubtypeSlot.HEAD, 2));
        assertEquals(50f, colony.getSubtypeHatchRate(AntSubtypeSlot.HEAD, GameConstants.SUBTYPE_DIGIT_NONE));
    }

    @Test
    void aggregateAndFlattenSubtypeData() {
        Colony colony = new Colony(5, "C", true);
        Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
        worker.setSubtypeProfile(AntSubtypeProfile.fromCode(2121));
        colony.getWorkers().add(worker);

        Map<String, Integer> counts = AntSubtypeService.aggregateSubtypeCounts(colony.getWorkers());
        assertEquals(1, counts.get("2121"));

        colony.setSubtypeHatchRate(AntSubtypeSlot.HEAD, 2, 25f);
        Map<String, Double> flat = AntSubtypeService.flattenSubtypeRates(colony.getSubtypeHatchRates());
        assertEquals(25.0, flat.get("HEAD:2"));
    }
}
