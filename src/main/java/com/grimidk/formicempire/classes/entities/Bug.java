package com.grimidk.formicempire.classes.entities;

import com.grimidk.formicempire.classes.constants.ant.AntStatus;
import com.grimidk.formicempire.classes.constants.ant.MoveStatus;
import com.grimidk.formicempire.classes.constants.misc.BugType;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.Room;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

import java.awt.Point;

public class Bug {
    private AntStatus status;
    private BugType type;
    private float health;
    private int maxHealth;
    private int age;
    private float temp;
    private float regen;
    private float consumption;
    private float attack;
    private float attackSpeed;
    private float defense;
    private float speed;
    private float size;
    
    private MoveStatus moveStatus;
    private int x;
    private int y;
    private Dimension dimension; 
    private int r; 
    
    private double preciseX;
    private double preciseY;
    private Point targetPosition;
    private Room currentRoom;

    public Bug(BugType type) {
        this.type = type;
        
        this.status = GameConstants.STATUS_ALIVE;
        this.moveStatus = GameConstants.MOVE_STATIC;
        this.maxHealth = (int)(type.getBaseHealth()); 
        this.health = this.maxHealth;
        this.age = 0;
        this.regen = type.getBaseRegen();
        this.attack = type.getBaseAttack();
        this.attackSpeed = type.getBaseAttackSpeed();
        this.defense = type.getBaseDefense() ;
        this.speed = type.getBaseSpeed();
        this.size = type.getBaseSize();
        
        this.dimension = WorldSpaces.OVERWORLD;

        this.x = 0; 
        this.y = 0;
        this.preciseX = 0.0;
        this.preciseY = 0.0;
    }

    public BugType getBugType() { return type; } 
    public void setBugType(BugType type) { this.type = type; }

    public AntStatus getStatus() { return status; }
    public void setStatus(AntStatus status) { this.status = status; }

    public float getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public float getTemp() { return temp; }
    public void setTemp(float temp) { this.temp = temp; }

    public float getRegen() { return regen; }
    public void setRegen(int regen) { this.regen = regen; }

    public float getConsumption() { return consumption; }
    public void setConsumption(float consumption) { this.consumption = consumption; }

    public float getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public float getAttackSpeed() { return attackSpeed; }
    public void setAttackSpeed(int attackSpeed) { this.attackSpeed = attackSpeed; }

    public float getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public float getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public Dimension getDimension() { return dimension; }
    public void setDimension(Dimension dimension) { this.dimension = dimension; }

    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room currentRoom) { this.currentRoom = currentRoom; }

    public void goDie() {
        this.status = GameConstants.STATUS_ALIVE;
        this.moveStatus = GameConstants.MOVE_STATIC;
        this.maxHealth = 0;
        this.health = 0;
        this.age = 0;
        this.regen = 0;
        this.consumption = 0;
        this.attack = 0;
        this.attackSpeed = 0;
        this.defense = 0;
        this.speed = 0;
    }

    public boolean isAlive() {
        return this.getStatus() == GameConstants.STATUS_ALIVE;
    }

    public MoveStatus getMoveStatus() { return moveStatus; }
    public void setMoveStatus(MoveStatus moveStatus) { this.moveStatus = moveStatus; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getR() { return r; }
    public void setR(int r) { this.r = r; }
    
    public void setPosition(Point p) {
        this.x = p.x;
        this.y = p.y;
        this.preciseX = p.x;
        this.preciseY = p.y;
        this.targetPosition = null;
        this.moveStatus = GameConstants.MOVE_STATIC;
    }

    public void moveTo(Point p) {
        this.targetPosition = p;
        this.moveStatus = GameConstants.MOVE_WANDER; 
        calculateRotation(); 
    }

    public boolean isMoving() {
        return this.targetPosition != null;
    }

    public void updatePosition() {
        if (targetPosition == null || this.moveStatus == GameConstants.MOVE_STATIC) return;

        double dx = targetPosition.x - this.preciseX;
        double dy = targetPosition.y - this.preciseY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        double currentSpeed = this.speed * this.moveStatus.getSpeedMult();
        if (currentSpeed <= 0.01) currentSpeed = 0.01;

        if (distance <= currentSpeed) {
            this.preciseX = targetPosition.x;
            this.preciseY = targetPosition.y;
            this.x = targetPosition.x;
            this.y = targetPosition.y;
            this.targetPosition = null;
            this.moveStatus = GameConstants.MOVE_STATIC;
        } else {
            this.preciseX += (dx / distance) * currentSpeed;
            this.preciseY += (dy / distance) * currentSpeed;
            
            this.x = (int) this.preciseX;
            this.y = (int) this.preciseY;
        }
    }

    private void calculateRotation() {
        if (targetPosition == null) return;

        double dx = targetPosition.x - this.preciseX;
        double dy = targetPosition.y - this.preciseY;
        
        double theta = Math.atan2(dy, dx);
        
        int degrees = (int) Math.toDegrees(theta);
        this.r = (degrees + 90 + 360) % 360; 
    }
}