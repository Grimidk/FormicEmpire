package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import java.util.ArrayList;
import java.util.List;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class DynastyDeathService {

    private static final ColonyStarterService STARTER = ColonyStarterService.shared();

    public void processDynastyDeaths(World world) {
        for (Dynasty dynasty : world.getDynastys()) {
            markExtinctIfDepopulated(world, dynasty);
            if (dynasty.isDefeated()) continue;

            List<Colony> deadColonies = new ArrayList<>();
            boolean capitalDied = false;

            for (Colony c : dynasty.getColonies()) {
                boolean deadFromZeroAnts = c.getAntTotal() == 0 && c.getAge() >= 7;
                boolean deadFromNoQueens = c.getDaysWithoutQueen() >= 7;

                if (deadFromZeroAnts || deadFromNoQueens) {
                    if (deadFromZeroAnts) {
                        logColonyCollapse(c, dynasty, "zero ants (mature colony)");
                    } else {
                        logColonyCollapse(c, dynasty, "7+ days without a queen");
                    }
                    deadColonies.add(c);
                    if (c.isCapital()) {
                        capitalDied = true;
                    }
                }
            }

            if (deadColonies.isEmpty()) continue;

            if (deadColonies.size() == dynasty.getColonies().size()) {
                Colony remnant = deadColonies.stream().filter(Colony::isCapital).findFirst().orElse(deadColonies.get(0));

                for (Colony dead : deadColonies) {
                    if (dead == remnant) {
                        dead.setActive(false);
                        dead.setAutomationEnabled(false);
                        dead.logEvent(ColonyLogPrefixes.INFO + " "
                            + LanguageStrings.get(LanguageStrings.LOG_LAST_COLONY_FALLEN));
                    } else {
                        Hex hex = findHexForColony(world, dead);
                        if (hex != null) STARTER.dismantleColony(hex);
                    }
                }

                dynasty.setDefeated(true);
                world.getWarService().endWarsInvolving(dynasty);
                logDynastyOutcome(dynasty, "Dynasty defeated (all colonies dead)");

            } else {
                for (Colony dead : deadColonies) {
                    Hex hex = findHexForColony(world, dead);
                    if (hex != null) STARTER.dismantleColony(hex);
                    dynasty.removeColony(dead);
                }

                if (capitalDied) {
                    dynasty.promoteNewCapital();
                    Colony newCapital = dynasty.getCapital();
                    if (newCapital != null) {
                        newCapital.logEvent(ColonyLogPrefixes.PROMOTION + " "
                            + String.format(LanguageStrings.get(LanguageStrings.LOG_PROMOTION_CAPITAL_FMT),
                                dynasty.getName()));
                        System.out.println("[DynastyDeathService] New capital crowned for dynastyId="
                                + dynasty.getId() + " " + dynasty.getName() + ": " + newCapital.getName());
                    }
                }
            }
        }
    }

    private void markExtinctIfDepopulated(World world, Dynasty dynasty) {
        if (dynasty == null || dynasty.isDefeated()) {
            return;
        }
        if (dynasty.hasLivingPopulation()) {
            return;
        }
        dynasty.setDefeated(true);
        world.getWarService().endWarsInvolving(dynasty);
        String causeHint = inferExtinctionCause(dynasty);
        logDynastyOutcome(dynasty, "Dynasty marked extinct (no living ants across all colonies)" + causeHint);
    }

    private static String inferExtinctionCause(Dynasty dynasty) {
        boolean anyWater = false;
        boolean anyFood = false;
        for (Colony colony : dynasty.getColonies()) {
            if (colony.getWater() > 0) {
                anyWater = true;
            }
            if (colony.getMushrooms() > 0) {
                anyFood = true;
            }
        }
        if (!anyWater && anyFood) {
            return " [likely dehydration: water=0 with fungi remaining]";
        }
        if (!anyFood && !anyWater) {
            return " [likely resource exhaustion]";
        }
        return "";
    }

    private void logColonyCollapse(Colony colony, Dynasty dynasty, String cause) {
        System.out.println("[DynastyDeathService] Colony collapse: " + formatColonySnapshot(colony, dynasty)
                + " cause=" + cause);
    }

    private void logDynastyOutcome(Dynasty dynasty, String outcome) {
        System.out.println("[DynastyDeathService] " + outcome + ": dynastyId=" + dynasty.getId()
                + " name=" + dynasty.getName()
                + " player=" + dynasty.isPlayer()
                + " colonyCount=" + dynasty.getColonies().size());
        for (Colony colony : dynasty.getColonies()) {
            System.out.println("[DynastyDeathService]   " + formatColonySnapshot(colony, dynasty));
        }
    }

    private static String formatColonySnapshot(Colony colony, Dynasty dynasty) {
        return "colonyId=" + colony.getId()
                + " name=" + colony.getName()
                + " dynastyId=" + (dynasty != null ? dynasty.getId() : "?")
                + " ants=" + colony.getAntTotal()
                + " queens=" + colony.getQueens().size()
                + " daysWithoutQueen=" + colony.getDaysWithoutQueen()
                + " age=" + colony.getAge()
                + " capital=" + colony.isCapital()
                + " active=" + colony.isActive()
                + " liteSim=" + !colony.runsFullSimulation()
                + " fungi=" + colony.getMushrooms()
                + " water=" + colony.getWater()
                + " automation=" + colony.isAutomationEnabled();
    }

    private Hex findHexForColony(World world, Colony colony) {
        for (Hex h : world.getHexes()) {
            if (h.getColony() == colony) return h;
        }
        return null;
    }
}
