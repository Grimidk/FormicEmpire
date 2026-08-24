package com.grimidk.formicempire.classes.entities.critter;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

public class Ant extends Critter {
    private AntType type;
    private AntRole role;
    private AntSubtypeProfile subtypeProfile;
    private float tempRes;    
    private ResourceType carrying;
    private ResourceType carryingSec;
    private AntType carryingAnt;   
    private String causeOfDeath;
    private boolean isOnTrade;
    private boolean isNuptial;
    private boolean parasiticMiteInfected;
    private boolean boostRegenPending;
    private boolean shieldingActive;
    private int jawFrame = 1;
    private int wingFrame = 1;
    private int antennaFrame = 1;
    private int jawOpenMinutesRemaining;
    private int wingOpenMinutesRemaining;
    private int antennaOpenMinutesRemaining;

    public Ant(Colony colony, AntType type) {
        super(GameConstants.TYPE_ANT); 
        
        this.type = type;
        this.role = null;
        this.subtypeProfile = AntSubtypeProfile.standard();
        this.carrying = null;
        this.carryingSec = null;
        this.carryingAnt = null;
        this.causeOfDeath = "Unknown"; 
        this.isOnTrade = false;
        this.isNuptial = false;
        
        this.setStatus(GameConstants.STATUS_ALIVE);
        this.setMaxHealth(Math.max(0, Math.round(colony.getBaseHealth() * type.getHealtMult()))); 
        this.setHealth(this.getMaxHealth());
        this.setAge(0);
        
        this.tempRes = colony.getBaseTempRes();
        
        this.setRegen((int)(colony.getBaseRegen() * type.getRegenMult()));
        this.setConsumption(colony.getBaseConsumption() * type.getConsumptionMult());
        this.setAttack((int)(colony.getBaseAttack() * type.getAttackMult()));
        this.setAttackSpeed((int)(colony.getBaseAttackSpeed() * type.getAttackSpeedMult()));
        this.setDefense(GameNumbers.clampDefensePercent(type.getDefenseMult() + colony.getBaseDefense()));        
        this.setSpeed(colony.getBaseSpeed() * type.getSpeedMult());
        
        this.setDimension(WorldSpaces.OVERWORLD);
        if (AntSubtypeService.isEligibleType(type)) {
            AntSubtypeService.applySubtypeStats(this, colony);
        }
    }
    
    public AntType getAntType() { return type; }
    public void setAntType(AntType type) { this.type = type; }

    public AntRole getRole() { return role; }
    public void setRole(AntRole role) { this.role = role; }

    public AntSubtypeProfile getSubtypeProfile() { return subtypeProfile; }
    public void setSubtypeProfile(AntSubtypeProfile subtypeProfile) {
        this.subtypeProfile = subtypeProfile != null ? subtypeProfile : AntSubtypeProfile.standard();
    }

    public float getTempRes() { return tempRes; }
    public void setTempRes(float tempRes) { this.tempRes = tempRes; }

    public ResourceType getCarrying() { return carrying; }
    public void setCarrying(ResourceType carrying) { this.carrying = carrying; }

    public ResourceType getCarryingSec() { return carryingSec; }
    public void setCarryingSec(ResourceType carryingSec) { this.carryingSec = carryingSec; }

    public AntType getCarryingAnt() { return carryingAnt; }
    public void setCarryingAnt(AntType carryingAnt) { this.carryingAnt = carryingAnt; }
    
    public String getCauseOfDeath() { return causeOfDeath; }

    public boolean isOnTrade() { return isOnTrade; }
    public void setOnTrade(boolean onTrade) { this.isOnTrade = onTrade; }

    public boolean isNuptial() { return isNuptial; }
    public void setNuptial(boolean isNuptial) {
        this.isNuptial = isNuptial;
        if (isNuptial && (type == GameConstants.TYPE_DRONE || type == GameConstants.TYPE_PRINCESS)) {
            setLegFrame(GameNumbers.ANT_LEG_FRAME_FLYING);
        } else if (!isMoving()) {
            setLegFrame(1);
        }
    }

    public boolean isParasiticMiteInfected() { return parasiticMiteInfected; }
    public void setParasiticMiteInfected(boolean parasiticMiteInfected) {
        this.parasiticMiteInfected = parasiticMiteInfected;
    }

    public boolean isBoostRegenPending() {
        return boostRegenPending;
    }

    public void setBoostRegenPending(boolean boostRegenPending) {
        this.boostRegenPending = boostRegenPending;
    }

    public boolean isShieldingActive() {
        return shieldingActive;
    }

    public void setShieldingActive(boolean shieldingActive) {
        this.shieldingActive = shieldingActive;
    }

    // --- Navigation Queue ---
    private Queue<NeoPoint> route = new LinkedList<>();

    public void setRoute(Queue<NeoPoint> route) {
        this.route = route;
    }
    
    public Queue<NeoPoint> getRoute() {
        return this.route;
    }
    
    public boolean hasRoute() {
        return this.route != null && !this.route.isEmpty();
    }
    
    public NeoPoint getNextRoutePoint() {
        if (this.route != null && !this.route.isEmpty()) {
            return this.route.poll();
        }
        return null;
    }
    
    public void clearRoute() {
        if (this.route != null) {
            this.route.clear();
        }
    }

    public void clearLoad() {
        this.carrying = null;
        this.carryingSec = null;
        this.carryingAnt = null;
    }

    @Override
    public void goDie() {
        goDie(null, DeathCause.OTHER);
    }

    public void goDie(Colony colony, String reason) {
        this.type = GameConstants.TYPE_DEAD; 
        this.causeOfDeath = reason;
        
        this.clearLoad();        
        this.clearRoute();
        this.parasiticMiteInfected = false;
        super.goDie();
    }

    public void transform(Colony colony, AntType newType) {
        AntSubtypeProfile preserved = subtypeProfile;
        this.type = newType;
        if (AntSubtypeService.isEligibleType(newType)) {
            this.subtypeProfile = preserved != null ? preserved : AntSubtypeProfile.standard();
            this.setAge(0);
            AntSubtypeService.applySubtypeStats(this, colony);
        } else {
            this.subtypeProfile = AntSubtypeProfile.standard();
            this.setMaxHealth(Math.max(0, Math.round(colony.getBaseHealth() * newType.getHealtMult())));
            this.setHealth(this.getMaxHealth());
            this.setAge(0);
            this.setRegen((int)(colony.getBaseRegen() * newType.getRegenMult()));
            this.setConsumption(colony.getBaseConsumption() * newType.getConsumptionMult());
            this.setAttack((int)(colony.getBaseAttack() * newType.getAttackMult()));
            this.setAttackSpeed((int)(colony.getBaseAttackSpeed() * newType.getAttackSpeedMult()));
            this.setDefense(GameNumbers.clampDefensePercent(newType.getDefenseMult() + colony.getBaseDefense()));
            this.setSpeed(colony.getBaseSpeed() * newType.getSpeedMult());
        }
    }

    public void updateStatsFromColony(Colony colony) {
        if (AntSubtypeService.isEligibleType(type)) {
            AntSubtypeService.applySubtypeStats(this, colony);
            return;
        }
        this.setMaxHealth(Math.max(0, Math.round(colony.getBaseHealth() * type.getHealtMult())));
        
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
        
        this.tempRes = colony.getBaseTempRes();
        this.setRegen((int)(colony.getBaseRegen() * type.getRegenMult()));
        this.setConsumption(colony.getBaseConsumption() * type.getConsumptionMult());
        this.setAttack((int)(colony.getBaseAttack() * type.getAttackMult()));
        this.setAttackSpeed((int)(colony.getBaseAttackSpeed() * type.getAttackSpeedMult()));
        this.setDefense(GameNumbers.clampDefensePercent(type.getDefenseMult() + colony.getBaseDefense()));        
        this.setSpeed(colony.getBaseSpeed() * type.getSpeedMult());
    }

    public int getJawFrame() {
        return jawFrame;
    }

    public int getWingFrame() {
        return wingFrame;
    }

    public int getAntennaFrame() {
        return antennaFrame;
    }

    @Override
    public void updatePosition(float speedMultiplier) {
        boolean wingedFlyer = isNuptial()
                && (type == GameConstants.TYPE_DRONE || type == GameConstants.TYPE_PRINCESS);
        if (wingedFlyer) {
            setLegFrame(GameNumbers.ANT_LEG_FRAME_FLYING);
            super.updatePosition(speedMultiplier);
            return;
        }
        boolean wasMoving = isMoving();
        super.updatePosition(speedMultiplier);
        if (wasMoving) {
            int advances = Math.max(1, Math.round(speedMultiplier / GameNumbers.BASE_SPRITE_SPEED));
            int frame = getLegFrame();
            for (int i = 0; i < advances; i++) {
                frame = frame >= GameNumbers.ANT_LEG_FRAME_COUNT ? 1 : frame + 1;
            }
            setLegFrame(frame);
        } else {
            setLegFrame(1);
        }
    }

    @Override
    public void setPosition(Point p) {
        super.setPosition(p);
        if (isNuptial() && (type == GameConstants.TYPE_DRONE || type == GameConstants.TYPE_PRINCESS)) {
            setLegFrame(GameNumbers.ANT_LEG_FRAME_FLYING);
        } else {
            setLegFrame(1);
        }
    }

    public void tickSpriteAnimMinute() {
        if (jawOpenMinutesRemaining > 0) {
            jawOpenMinutesRemaining--;
            if (jawOpenMinutesRemaining <= 0) {
                jawFrame = 1;
            }
        }
        if (wingOpenMinutesRemaining > 0) {
            wingOpenMinutesRemaining--;
            if (wingOpenMinutesRemaining <= 0) {
                wingFrame = 1;
            }
        }
        if (antennaOpenMinutesRemaining > 0) {
            antennaOpenMinutesRemaining--;
            if (antennaOpenMinutesRemaining <= 0) {
                antennaFrame = 1;
            }
        }
    }

    public void rollHourlySpriteAnim() {
        if (jawOpenMinutesRemaining <= 0
                && GameRandom.nextDouble() < GameNumbers.ANT_JAW_SNAP_CHANCE_PER_HOUR) {
            jawFrame = 2;
            jawOpenMinutesRemaining = GameNumbers.ANT_SPRITE_SNAP_MINUTES;
        }
        if (antennaOpenMinutesRemaining <= 0
                && GameRandom.nextDouble() < GameNumbers.ANT_ANTENNA_TWITCH_CHANCE_PER_HOUR) {
            antennaFrame = 2;
            antennaOpenMinutesRemaining = GameNumbers.ANT_SPRITE_SNAP_MINUTES;
        }

        boolean winged = type == GameConstants.TYPE_DRONE || type == GameConstants.TYPE_PRINCESS;
        if (!winged) {
            wingFrame = 1;
            wingOpenMinutesRemaining = 0;
            return;
        }
        if (wingOpenMinutesRemaining <= 0
                && GameRandom.nextDouble() < GameNumbers.ANT_WING_FLICK_CHANCE_PER_HOUR) {
            wingFrame = 2;
            wingOpenMinutesRemaining = GameNumbers.ANT_SPRITE_SNAP_MINUTES;
        }
    }
}
