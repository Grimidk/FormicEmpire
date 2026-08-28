package com.grimidk.formicempire.classes.entities.services.world;

public final class WarBattleState {
    private final WarBattleSideState attacker;
    private final WarBattleSideState defender;
    private final boolean hexAssault;
    private final int contestedColonyId;
    private int tickIndex;
    private int worldDay;

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
}
