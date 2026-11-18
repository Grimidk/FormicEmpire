package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.entities.services.shared.BattleNumericFeed;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class WarBattleState {
    private final WarBattleSideState attacker;
    private final WarBattleSideState defender;
    private final boolean hexAssault;
    private final int contestedColonyId;
    private final BattleNumericFeed numericFeed = new BattleNumericFeed();
    private int tickIndex;
    private int worldDay;
    private boolean riverBattle;
    private int nextPhaseIndex;
    private boolean currentPhaseAttacker = true;
    private BattleLine currentPhaseLine = GameConstants.BATTLE_LINE_INFANTRY;

    public WarBattleState() {
        this(null, null, false, 0);
    }

    WarBattleState(WarBattleSideState attacker, WarBattleSideState defender, boolean hexAssault,
            int contestedColonyId) {
        this.attacker = attacker;
        this.defender = defender;
        this.hexAssault = hexAssault;
        this.contestedColonyId = contestedColonyId;
        this.tickIndex = 0;
        this.worldDay = 0;
    }

    int getWorldDay() {
        return worldDay;
    }

    void setWorldDay(int worldDay) {
        this.worldDay = worldDay;
    }

    public WarBattleSideState getAttacker() {
        return attacker;
    }

    public WarBattleSideState getDefender() {
        return defender;
    }

    public boolean isHexAssault() {
        return hexAssault;
    }

    public int getContestedColonyId() {
        return contestedColonyId;
    }

    public int getTickIndex() {
        return tickIndex;
    }

    void advanceTick() {
        tickIndex++;
    }

    public boolean isRiverBattle() {
        return riverBattle;
    }

    public void setRiverBattle(boolean riverBattle) {
        this.riverBattle = riverBattle;
    }

    public BattleNumericFeed getNumericFeed() {
        return numericFeed;
    }

    int getNextPhaseIndex() {
        return nextPhaseIndex;
    }

    void setNextPhaseIndex(int nextPhaseIndex) {
        this.nextPhaseIndex = nextPhaseIndex;
    }

    public boolean isCurrentPhaseAttacker() {
        return currentPhaseAttacker;
    }

    void setCurrentPhaseAttacker(boolean currentPhaseAttacker) {
        this.currentPhaseAttacker = currentPhaseAttacker;
    }

    public BattleLine getCurrentPhaseLine() {
        return currentPhaseLine;
    }

    void setCurrentPhaseLine(BattleLine currentPhaseLine) {
        this.currentPhaseLine = currentPhaseLine;
    }
}
