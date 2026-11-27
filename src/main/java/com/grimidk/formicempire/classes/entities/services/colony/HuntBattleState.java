package com.grimidk.formicempire.classes.entities.services.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.shared.BattleNumericFeed;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
public final class HuntBattleState {
    private final Colony colony;
    private final int contextId;
    private final Species species;
    private final List<HuntBattleParticipant> hunters = new ArrayList<>();
    private final List<HuntBattleFallenBody> fallenBodies = new ArrayList<>();
    private final BattleNumericFeed numericFeed = new BattleNumericFeed();
    private final int orbitSlotCount;
    private final int infantrySlotCount;
    private final int artillerySlotCount;
    private final int airSupportSlotCount;
    private float bugHealth;
    private final float bugMaxHealth;
    private final float bugAttack;
    private final float bugDefense;
    private final int bugAttackSpeed;
    private int tickIndex;
    private int focusTargetIndex;

    public HuntBattleState(Colony colony, int contextId, Species species, List<HuntBattleParticipant> hunters,
            float bugMaxHealth, float bugAttack, float bugDefense, int bugAttackSpeed) {
        this.colony = colony;
        this.contextId = contextId;
        this.species = species;
        this.hunters.addAll(hunters);
        this.orbitSlotCount = hunters.size();
        int infCount = 0;
        int artCount = 0;
        int airCount = 0;
        for (int i = 0; i < hunters.size(); i++) {
            HuntBattleParticipant participant = hunters.get(i);
            participant.setOrbitSlot(i);
            BattleLine line = resolveBattleLine(participant.getAnt());
            participant.setBattleLine(line);
            if (line == GameConstants.BATTLE_LINE_ARTILLERY) {
                participant.setLineSlot(artCount++);
            } else if (line == GameConstants.BATTLE_LINE_AIR_SUPPORT) {
                participant.setLineSlot(airCount++);
            } else {
                participant.setLineSlot(infCount++);
            }
        }
        this.infantrySlotCount = infCount;
        this.artillerySlotCount = artCount;
        this.airSupportSlotCount = airCount;
        this.bugMaxHealth = bugMaxHealth;
        this.bugHealth = bugMaxHealth;
        this.bugAttack = bugAttack;
        this.bugDefense = bugDefense;
        this.bugAttackSpeed = bugAttackSpeed;
    }

    public static BattleLine resolveBattleLine(Ant ant) {
        if (ant == null || ant.getRole() == null) {
            return GameConstants.BATTLE_LINE_INFANTRY;
        }
        BattleLine line = GameConstants.getBattleLineForRole(ant.getRole());
        return line != null ? line : GameConstants.BATTLE_LINE_INFANTRY;
    }

    public static HuntBattleState restore(Colony colony, int contextId, Species species,
            List<HuntBattleParticipant> hunters, float bugHealth, float bugMaxHealth, float bugAttack,
            float bugDefense, int bugAttackSpeed, int tickIndex, int focusTargetIndex) {
        HuntBattleState state = new HuntBattleState(
                colony,
                contextId,
                species,
                hunters,
                bugMaxHealth,
                bugAttack,
                bugDefense,
                bugAttackSpeed);
        state.bugHealth = Math.max(0f, Math.min(bugMaxHealth, bugHealth));
        state.tickIndex = Math.max(0, tickIndex);
        state.focusTargetIndex = focusTargetIndex;
        return state;
    }

    public Colony getColony() {
        return colony;
    }

    public int getContextId() {
        return contextId;
    }

    public Species getSpecies() {
        return species;
    }

    public int getOrbitSlotCount() {
        return orbitSlotCount;
    }

    public int getInfantrySlotCount() {
        return infantrySlotCount;
    }

    public int getArtillerySlotCount() {
        return artillerySlotCount;
    }

    public int getAirSupportSlotCount() {
        return airSupportSlotCount;
    }

    public List<HuntBattleFallenBody> getFallenBodies() {
        return Collections.unmodifiableList(fallenBodies);
    }

    public BattleNumericFeed getNumericFeed() {
        return numericFeed;
    }

    void recordFallenBody(HuntBattleParticipant victim, float bodyRotationDegrees) {
        if (victim == null || victim.getAnt() == null) {
            return;
        }
        Ant ant = victim.getAnt();
        fallenBodies.add(new HuntBattleFallenBody(
                ant.getAntClass(),
                ant.getModProfile(),
                victim.getOrbitSlot(),
                bodyRotationDegrees,
                victim.getBattleLine(),
                victim.getLineSlot()));
    }

    public List<HuntBattleParticipant> getHunters() {
        return Collections.unmodifiableList(hunters);
    }

    public List<HuntBattleParticipant> livingHunters() {
        List<HuntBattleParticipant> living = new ArrayList<>();
        for (HuntBattleParticipant hunter : hunters) {
            if (hunter.isAlive()) {
                living.add(hunter);
            }
        }
        return living;
    }

    public List<HuntBattleParticipant> livingFrontlineHunters() {
        List<HuntBattleParticipant> frontline = new ArrayList<>();
        List<HuntBattleParticipant> allLiving = new ArrayList<>();
        for (HuntBattleParticipant hunter : hunters) {
            if (!hunter.isAlive()) {
                continue;
            }
            allLiving.add(hunter);
            if (hunter.getBattleLine() == GameConstants.BATTLE_LINE_INFANTRY) {
                frontline.add(hunter);
            }
        }
        return frontline.isEmpty() ? allLiving : frontline;
    }

    public float getBugHealth() {
        return bugHealth;
    }

    public float getBugMaxHealth() {
        return bugMaxHealth;
    }

    public float getBugAttack() {
        return bugAttack;
    }

    public float getBugDefense() {
        return bugDefense;
    }

    public int getBugAttackSpeed() {
        return bugAttackSpeed;
    }

    public int getTickIndex() {
        return tickIndex;
    }

    public int getFocusTargetIndex() {
        return focusTargetIndex;
    }

    public void advanceTick() {
        tickIndex++;
    }

    public boolean isBugAlive() {
        return bugHealth > 0f;
    }

    public float applyBugDamage(float damage) {
        if (damage <= 0f) {
            return bugHealth;
        }
        bugHealth = Math.max(0f, bugHealth - damage);
        return bugHealth;
    }

    public HuntBattleParticipant getFocusTarget() {
        List<HuntBattleParticipant> living = livingFrontlineHunters();
        if (living.isEmpty()) {
            return null;
        }
        focusTargetIndex = Math.floorMod(focusTargetIndex, living.size());
        return living.get(focusTargetIndex);
    }

    public void rotateFocusTarget() {
        focusTargetIndex++;
    }

    public float getBugHealthRatio() {
        if (bugMaxHealth <= 0f) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, bugHealth / bugMaxHealth));
    }

    public List<Ant> livingHunterAnts() {
        List<Ant> ants = new ArrayList<>();
        for (HuntBattleParticipant hunter : livingHunters()) {
            if (hunter.getAnt() != null) {
                ants.add(hunter.getAnt());
            }
        }
        return ants;
    }

    public Ant getFocusAnt() {
        HuntBattleParticipant focus = getFocusTarget();
        return focus != null ? focus.getAnt() : null;
    }

    public int getFocusOrbitIndex() {
        HuntBattleParticipant focus = getFocusTarget();
        return focus != null ? focus.getOrbitSlot() : 0;
    }
}
