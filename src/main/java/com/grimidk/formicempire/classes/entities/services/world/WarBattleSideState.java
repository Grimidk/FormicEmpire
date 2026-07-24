package com.grimidk.formicempire.classes.entities.services.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class WarBattleSideState {
    private final Dynasty dynasty;
    private final Map<BattleLine, List<WarBattleParticipant>> activeByLine = new HashMap<>();
    private final Map<BattleLine, List<WarBattleParticipant>> reserveByLine = new HashMap<>();
    private int startingArmySize;
    private int deadCount;

    WarBattleSideState(Dynasty dynasty) {
        this.dynasty = dynasty;
        for (BattleLine line : GameConstants.getBattleLines()) {
            activeByLine.put(line, new ArrayList<>());
            reserveByLine.put(line, new ArrayList<>());
        }
    }

    public Dynasty getDynasty() {
        return dynasty;
    }

    List<WarBattleParticipant> getActive(BattleLine line) {
        return activeByLine.computeIfAbsent(line, k -> new ArrayList<>());
    }

    List<WarBattleParticipant> getReserve(BattleLine line) {
        return reserveByLine.computeIfAbsent(line, k -> new ArrayList<>());
    }

    public int getStartingArmySize() {
        return startingArmySize;
    }

    void setStartingArmySize(int startingArmySize) {
        this.startingArmySize = Math.max(0, startingArmySize);
    }

    public int getDeadCount() {
        return deadCount;
    }

    void incrementDead() {
        deadCount++;
    }

    public int livingArmySize() {
        int n = 0;
        for (BattleLine line : GameConstants.getBattleLines()) {
            n += countLiving(getActive(line));
            n += countLiving(getReserve(line));
        }
        return n;
    }

    public int livingActiveSize() {
        int n = 0;
        for (BattleLine line : GameConstants.getBattleLines()) {
            n += countLiving(getActive(line));
        }
        return n;
    }

    boolean hasLivingDefenders() {
        for (BattleLine line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : getActive(line)) {
                if (p.isAlive() && p.isDefenderRole()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasLivingQueen() {
        for (BattleLine line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : getActive(line)) {
                if (p.isAlive() && p.isQueen()) {
                    return true;
                }
            }
            for (WarBattleParticipant p : getReserve(line)) {
                if (p.isAlive() && p.isQueen()) {
                    return true;
                }
            }
        }
        return false;
    }

    List<WarBattleParticipant> allLivingActive() {
        List<WarBattleParticipant> out = new ArrayList<>();
        for (BattleLine line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : getActive(line)) {
                if (p.isAlive()) {
                    out.add(p);
                }
            }
        }
        return out;
    }

    public Map<BattleLine, Integer> activeCountsByLine() {
        Map<BattleLine, Integer> counts = new HashMap<>();
        for (BattleLine line : GameConstants.getBattleLines()) {
            counts.put(line, countLiving(getActive(line)));
        }
        return counts;
    }

    public Map<BattleLine, Integer> reserveCountsByLine() {
        Map<BattleLine, Integer> counts = new HashMap<>();
        for (BattleLine line : GameConstants.getBattleLines()) {
            counts.put(line, countLiving(getReserve(line)));
        }
        return counts;
    }

    private static int countLiving(List<WarBattleParticipant> list) {
        int n = 0;
        for (WarBattleParticipant p : list) {
            if (p.isAlive()) {
                n++;
            }
        }
        return n;
    }

    static BattleLine lineForAnt(Ant ant) {
        if (ant == null) {
            return GameConstants.BATTLE_LINE_INFANTRY;
        }
        if (ant.getAntType() == GameConstants.TYPE_QUEEN) {
            return GameConstants.BATTLE_LINE_INFANTRY;
        }
        BattleLine line = GameConstants.getBattleLineForRole(ant.getRole());
        return line != null ? line : GameConstants.BATTLE_LINE_INFANTRY;
    }
}
