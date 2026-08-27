package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.services.shared.ColonyAntAnimSampleLod;
import com.grimidk.formicempire.classes.entities.services.shared.ViewportPhysicsLod;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.critter.Critter;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.entities.spatial.Dimension;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.entities.spatial.Room;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.swing.ImageIcon;

public class ColonyPhysicsService {

    // --- Main Physics Loop ---
    public void runPhysics(Colony colony, Dimension activeDimension) {
        runPhysics(colony, activeDimension, null, 0L);
    }

    public void runPhysics(Colony colony, Dimension activeDimension, Rectangle viewportBounds, long physicsStepIndex) {
        float locsenseSpeedMultiplier = colony.getStatsService().getLocsenseSpeedMultiplier(colony);
        float creatineSpeedMultiplier = colony.isCreatineDietActive()
                ? GameNumbers.CREATINE_DIET_SPEED_MULTIPLIER
                : 1f;
        float offViewportBaseSpeed = ViewportPhysicsLod.compensatedMoveSpeed(GameNumbers.BASE_SPRITE_SPEED)
                * locsenseSpeedMultiplier;
        // -- Ants --
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            AntType type = entry.getKey();

            ImageIcon spriteIcon = GameConstants.getAntSprite(type, colony.getSpecies());
            int spriteW = spriteIcon != null ? spriteIcon.getIconWidth() : 16;
            int spriteH = spriteIcon != null ? spriteIcon.getIconHeight() : 16;

            List<Ant> antList = entry.getValue();

            synchronized (antList) {
                for (Ant ant : antList) {
                    if (!ant.isAlive()) {
                        continue;
                    }
                    if (ant.isOnTrade()) {
                        continue;
                    }

                    boolean sameDim = ant.getDimension() == activeDimension;
                    boolean lodSameDim = ViewportPhysicsLod.isLodActive(viewportBounds) && sameDim;
                    boolean inView = sameDim && ViewportPhysicsLod.antIntersectsViewport(
                        viewportBounds, ant.getX(), ant.getY(), spriteW, spriteH);

                    boolean shouldRunAI;
                    if (!sameDim) {
                        shouldRunAI = GameRandom.nextDouble() < 0.05;
                    } else if (lodSameDim && !inView) {
                        boolean runOffscreenAi = ViewportPhysicsLod.shouldRunOffViewportAi(physicsStepIndex, ant);
                        boolean runOffscreenMove = ViewportPhysicsLod.shouldRunOffViewportPosition(physicsStepIndex, ant);
                        if (!runOffscreenAi && !runOffscreenMove && !ant.isMoving() && !ant.hasRoute()) {
                            continue;
                        }
                        shouldRunAI = runOffscreenAi;
                    } else {
                        shouldRunAI = true;
                    }

                    if (!ant.isMoving() && ant.hasRoute()) {
                        processNextRoutePoint(ant);
                    }

                    if (!ant.isMoving() && !ant.hasRoute() && shouldRunAI) {
                        updateAntLogic(colony, ant);
                    }

                    float moveSpeed = GameNumbers.BASE_SPRITE_SPEED;
                    moveSpeed *= creatineSpeedMultiplier;
                    moveSpeed *= locsenseSpeedMultiplier;
                    if (lodSameDim && !inView) {
                        if (!ViewportPhysicsLod.shouldRunOffViewportPosition(physicsStepIndex, ant)) {
                            continue;
                        }
                        moveSpeed = offViewportBaseSpeed;
                    }
                    if (type == GameConstants.TYPE_WORKER && colony.hasUpgrade(GameUnlocks.STAT_WORKER_SPEED_2)) {
                        moveSpeed *= 2f;
                    }
                    if (ant.isParasiticMiteInfected()) {
                        moveSpeed *= GameNumbers.PARASITIC_MITE_SPEED_MULTIPLIER;
                    }
                    ant.updatePosition(moveSpeed);
                }
            }
        }

        // -- Bugs --
        List<Critter> critters = colony.getCritters();
        synchronized (critters) {
            for (Critter bug : critters) {
                if (!bug.isAlive() || bug.getDimension() != activeDimension) {
                    continue;
                }

                ImageIcon bugIcon = bug.getSpecies().getSprite();
                int bw = bugIcon != null ? bugIcon.getIconWidth() : 16;
                int bh = bugIcon != null ? bugIcon.getIconHeight() : 16;

                boolean lod = ViewportPhysicsLod.isLodActive(viewportBounds);
                boolean bugInView = ViewportPhysicsLod.antIntersectsViewport(
                    viewportBounds, bug.getX(), bug.getY(), bw, bh);
                int bugHash = System.identityHashCode(bug);

                boolean runBugAi = !lod || bugInView
                    || ViewportPhysicsLod.shouldRunOffViewportBugAi(physicsStepIndex, bugHash);
                boolean runBugMove = !lod || bugInView
                    || ViewportPhysicsLod.shouldRunOffViewportBugMove(physicsStepIndex, bugHash);
                if (lod && !bugInView && !runBugAi && !runBugMove && !bug.isMoving()) {
                    continue;
                }
                if (!bug.isMoving() && runBugAi) {
                    updateBugLogic(colony, bug);
                }

                float bugMove = GameNumbers.BASE_SPRITE_SPEED;
                if (colony.isCreatineDietActive()) {
                    bugMove *= GameNumbers.CREATINE_DIET_SPEED_MULTIPLIER;
                }
                if (lod && !bugInView) {
                    if (!ViewportPhysicsLod.shouldRunOffViewportBugMove(physicsStepIndex, bugHash)) {
                        continue;
                    }
                    bugMove = ViewportPhysicsLod.compensatedMoveSpeed(GameNumbers.BASE_SPRITE_SPEED);
                }
                bug.updatePosition(bugMove);
            }
        }
    }

    private void processNextRoutePoint(Ant ant) {
        NeoPoint next = ant.getNextRoutePoint();
        if (next != null) {
            if (next.getDimension() != ant.getDimension()) {
                ant.setDimension(next.getDimension());
                ant.setPosition(next); 
            } else {
                ant.moveTo(next);
            }
        }
    }

    public void randomizeAllAntPositions(Colony colony) {
        int virtualWidth = Math.max(colony.getGameAreaWidth(), 2000);

        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            if (entry.getKey() == GameConstants.TYPE_DEAD) continue;
            
            ImageIcon sprite = GameConstants.getAntSprite(entry.getKey(), colony.getSpecies());
            List<Ant> antList = entry.getValue();
            synchronized (antList) {
                for (Ant ant : antList) {
                    ant.clearRoute(); 
                    
                    Rectangle targetRoom = getTargetRoomForAnt(colony, ant, virtualWidth);
                    
                    if (targetRoom != null) {
                        ant.setDimension(WorldSpaces.UNDERWORLD);
                        ant.setPosition(getRandomPointInRoom(colony, targetRoom, virtualWidth));
                    } 
                    else {
                         if (shouldBeInColony(ant)) {
                            ant.setDimension(WorldSpaces.UNDERWORLD);
                            ant.setPosition(getRandomPointInRoom(colony, getRoomBounds(colony, WorldSpaces.STORAGE), virtualWidth));
                        } else {
                            ant.setDimension(WorldSpaces.OVERWORLD);
                            Rectangle yard = getOverworldJobBounds(colony, ant);
                            if (yard != null) {
                                ant.setPosition(getRandomPointInRoom(colony, yard, virtualWidth));
                            } else {
                                ant.setPosition(getRandomOverworldPosition(colony, sprite));
                            }
                        }
                    }
                }
            }
        }
        
        List<Critter> critters = colony.getCritters();
        synchronized (critters) {
            for (Critter bug : critters) {
                if (bug.getSpecies() == GameConstants.TYPE_APHID && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
                    Rectangle yard = getRoomBounds(colony, WorldSpaces.RANCHER_YARD);
                    bug.setPosition(getRandomPointInRoom(colony, yard, virtualWidth));
                } else if (bug.getSpecies() == GameConstants.TYPE_SYMBIOTIC_MITE
                        && colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE)) {
                    Rectangle pen = colony.getInsectPenBounds();
                    if (pen == null) {
                        pen = getRoomBounds(colony, WorldSpaces.INSECT_PEN);
                    }
                    bug.setPosition(getRandomPointInRoom(colony, pen, virtualWidth));
                } else if (bug.getSpecies() == GameConstants.TYPE_DERMESTID
                        && colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_DERMESTID)) {
                    Rectangle yard = colony.getGraverBounds();
                    if (yard == null) {
                        yard = getRoomBounds(colony, WorldSpaces.GRAVEYARD);
                    }
                    bug.setPosition(getRandomPointInRoom(colony, yard, virtualWidth));
                } else if (bug.getSpecies() == GameConstants.TYPE_PARASITE_ANT) {
                    bug.setDimension(WorldSpaces.UNDERWORLD);
                    Rectangle hideout = getRoomBounds(colony, WorldSpaces.STORAGE);
                    bug.setPosition(getRandomPointInRoom(colony, hideout, virtualWidth));
                } else {
                    bug.setPosition(new Point(-1000, -1000));
                }
            }
        }
    }

    // --- AI Logic ---
    private void updateAntLogic(Colony colony, Ant ant) {
        if (ant.isNuptial()) {
            handleNuptialAnt(colony, ant);
            return;
        }

        if (isGatherer(ant)) {
            handleGathererLogic(colony, ant);
            return;
        }

        if (ant.getDimension() == WorldSpaces.OVERWORLD) {
            handleOverworldAnt(colony, ant);
        } else {
            handleUnderworldAnt(colony, ant);
        }
    }
    
    private void handleNuptialAnt(Colony colony, Ant ant) {
        int gameWidth = Math.max(colony.getGameAreaWidth(), 1280);
        int gameHeight = Math.max(colony.getGameAreaHeight(), 720);

        if (ant.getDimension() == WorldSpaces.UNDERWORLD) {
            Point exit = new Point(ColonySpatialLayout.ANCHOR_CENTER_X, 0);
            if (dist(ant.getX(), ant.getY(), exit.x, exit.y) < 20) {
                ant.setDimension(WorldSpaces.OVERWORLD);
                ant.setPosition(colony.getLocationService().getColonyEntrance(colony));
            } else {
                ant.moveTo(exit);
            }
        } else {
            if (ant.getX() < -100 || ant.getX() > gameWidth + 100 || ant.getY() < -100 || ant.getY() > gameHeight + 100) {
                colony.getAntsByType(ant.getAntType()).remove(ant);
                return;
            }

            if (!ant.isMoving()) {
                int side = (int)(GameRandom.nextDouble() * 4);
                int tx = 0, ty = 0;
                int buffer = 200;
                switch(side) {
                    case 0: tx = (int)(GameRandom.nextDouble() * gameWidth); ty = -buffer; break;
                    case 1: tx = (int)(GameRandom.nextDouble() * gameWidth); ty = gameHeight + buffer; break;
                    case 2: tx = -buffer; ty = (int)(GameRandom.nextDouble() * gameHeight); break;
                    case 3: tx = gameWidth + buffer; ty = (int)(GameRandom.nextDouble() * gameHeight); break;
                }
                ant.moveTo(new Point(tx, ty));
            }
        }
    }

    private boolean isGatherer(Ant ant) {
        AntRole r = ant.getRole();
        return r == GameConstants.ROLE_FORAGER || r == GameConstants.ROLE_HUNTER || r == GameConstants.ROLE_MINER;
    }

    private void handleGathererLogic(Colony colony, Ant ant) {
        if (ant.getCarrying() != null || ant.getCarryingAnt() != null) {
            Room storage = WorldSpaces.STORAGE;
            
            if (isAntInRoom(colony, ant, storage)) {
                ant.clearLoad();
            } else {
                Room current = getRoomContainingAnt(colony, ant);
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, storage, ant);
                ant.setRoute(route);
            }
            return;
        }

        ResourceSource target = colony.getLocationService().findNearestRelevantSource(colony, ant);

        if (target == null || !colony.getLocationService().isActiveSource(target)) {
            ant.clearRoute();
            if (ant.getDimension() == WorldSpaces.OVERWORLD) {
                 if (GameRandom.nextDouble() < 0.01) {
                    if (ant.getRole() == GameConstants.ROLE_SCOUT || ant.getRole() == GameConstants.ROLE_CATCHER) {
                         ant.moveTo(getRandomScoutPosition(colony));
                    } else {
                         ant.moveTo(getRandomOverworldPosition(colony, GameConstants.getAntSprite(ant.getAntType(), colony.getSpecies())));
                    }
                }
            } else {
                 handleUnderworldAnt(colony, ant);
            }
            return;
        }

        double d = dist(ant.getX(), ant.getY(), target.getCenterX(), target.getCenterY());

        if (d < 50 && ant.getDimension() == WorldSpaces.OVERWORLD) {
            if (colony.getLocationService().isActiveSource(target)) {
                ant.setCarrying(target.getResourceType());
                if (target.getResourceType() == GameConstants.RESOURCE_PLANT
                        && colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)
                        && GameRandom.nextDouble() < GameNumbers.RESIN_FORAGE_BONUS_CHANCE) {
                    ant.setCarryingSec(GameConstants.RESOURCE_RESIN);
                } else {
                    ant.setCarryingSec(null);
                }
            }
            ant.clearRoute();
        } else {
            if (!ant.hasRoute()) {
                Room current = getRoomContainingAnt(colony, ant);
                Room sourceRoom = colony.getLocationService().createTempRoomAtPoint(
                    new Point(target.getCenterX(), target.getCenterY()),
                    WorldSpaces.OVERWORLD
                );

                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, sourceRoom, ant);
                ant.setRoute(route);
            }
        }
    }

    private void updateBugLogic(Colony colony, Critter bug) {
        if (bug.getDimension() == WorldSpaces.OVERWORLD && bug.getSpecies() == GameConstants.TYPE_APHID) {
            if (colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
                Rectangle yard = colony.getRancherBounds();
                if (yard == null) {
                    yard = getRoomBounds(colony, WorldSpaces.RANCHER_YARD);
                }
                wanderInBoundaries(colony, bug, yard, 0.05);
            } else {
                bug.setPosition(new Point(-1000, -1000));
            }
        } else if (bug.getDimension() == WorldSpaces.OVERWORLD
                && bug.getSpecies() == GameConstants.TYPE_SYMBIOTIC_MITE
                && (colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE) || colony.getSymbioticMites() > 0)) {
            Rectangle pen = colony.getInsectPenBounds();
            if (pen == null) {
                pen = getRoomBounds(colony, WorldSpaces.INSECT_PEN);
            }
            wanderInBoundaries(colony, bug, pen, 0.05);
        } else if (bug.getDimension() == WorldSpaces.OVERWORLD
                && bug.getSpecies() == GameConstants.TYPE_DERMESTID
                && colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_DERMESTID)) {
            Rectangle yard = colony.getGraverBounds();
            if (yard == null) {
                yard = getRoomBounds(colony, WorldSpaces.GRAVEYARD);
            }
            wanderInBoundaries(colony, bug, yard, 0.05);
        }
        else if (bug.getSpecies() == GameConstants.TYPE_PARASITE_ANT) {
            if (bug.getDimension() != WorldSpaces.UNDERWORLD) {
                bug.setDimension(WorldSpaces.UNDERWORLD);
            }

            Rectangle currentRoom = getRoomBounds(colony, WorldSpaces.STORAGE);
            if (GameRandom.nextDouble() < 0.3) currentRoom = getRoomBounds(colony, WorldSpaces.FARM);

            if (currentRoom != null) {
                wanderInBoundaries(colony, bug, currentRoom, 0.05);
            }
        }
    }

    private void handleOverworldAnt(Colony colony, Ant ant) {
        if (shouldBeInColony(ant)) {
            NeoPoint entrance = colony.getLocationService().getColonyEntrance(colony);
            
            if (dist(ant.getX(), ant.getY(), entrance.x, entrance.y) < 30) {
                ant.clearLoad();
                ant.setDimension(WorldSpaces.UNDERWORLD);
                ant.setPosition(new Point(ColonySpatialLayout.ANCHOR_CENTER_X, 50));
            } else {
                Room target = findRoomForAnt(colony, ant);
                if (target == null) target = WorldSpaces.STORAGE;

                Room current = getRoomContainingAnt(colony, ant); 
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, target, ant);
                ant.setRoute(route);
            }
        } else {
            Rectangle yard = getOverworldJobBounds(colony, ant);

            if (yard != null) {
                if (isPointInSafeBounds(colony, yard, ant.getX(), ant.getY())) {
                    if (GameRandom.nextDouble() < 0.05) {
                        ant.moveTo(getRandomPointInRoom(colony, yard, colony.getGameAreaWidth()));
                    }
                } else {
                    Room targetRoom = (ant.getRole() == GameConstants.ROLE_GRAVER) ? WorldSpaces.GRAVEYARD : WorldSpaces.RANCHER_YARD;
                    Room current = getRoomContainingAnt(colony, ant); 
                    Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, targetRoom, ant);
                    ant.setRoute(route);
                }
            } else {
                if (GameRandom.nextDouble() < 0.01) {
                    if (ant.getRole() == GameConstants.ROLE_SCOUT) {
                        ant.moveTo(getRandomScoutPosition(colony));
                    } else {
                        ant.moveTo(getRandomOverworldPosition(colony, GameConstants.getAntSprite(ant.getAntType(), colony.getSpecies())));
                    }
                }
            }
        }
    }

    private void handleUnderworldAnt(Colony colony, Ant ant) {
        int virtualWidth = Math.max(colony.getGameAreaWidth(), 1280);
        Rectangle myRoom = getTargetRoomForAnt(colony, ant, virtualWidth);

        if (myRoom == null) {
             if (ant.getX() == 0 && ant.getY() == 0) {
                 ant.setPosition(new Point(ColonySpatialLayout.ANCHOR_CENTER_X, 0));
             }
             
             Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, getRoomContainingAnt(colony, ant), WorldSpaces.SURFACE, ant);
             ant.setRoute(route);
             return;
        }

        if (ant.getX() == 0 && ant.getY() == 0) {
            ant.setPosition(new Point((int)myRoom.getCenterX(), (int)myRoom.getCenterY()));
            return;
        }

        if (ant.getAntType() == GameConstants.TYPE_EGG || ant.getAntType() == GameConstants.TYPE_PUPA) {
            if (!isPointInSafeBounds(colony, myRoom, ant.getX(), ant.getY())) {
                ant.setPosition(getRandomPointInRoom(colony, myRoom, virtualWidth));
            }
            return;
        }

        if (!shouldBeInColony(ant)) {
            Room targetRoom = null;
            if (ant.getRole() == GameConstants.ROLE_RANCHER) targetRoom = WorldSpaces.RANCHER_YARD;
            else if (ant.getRole() == GameConstants.ROLE_GRAVER) targetRoom = WorldSpaces.GRAVEYARD;
            
            Room current = getRoomContainingAnt(colony, ant); 

            if (targetRoom == null) {
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, WorldSpaces.SURFACE, ant);
                ant.setRoute(route);
            } else {
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, targetRoom, ant);
                ant.setRoute(route);
            }
            return;
        }

        if (isPointInSafeBounds(colony, myRoom, ant.getX(), ant.getY())) {
            boolean isBuilder = (ant.getRole() == GameConstants.ROLE_BUILDER || ant.getRole() == GameConstants.ROLE_CRANE) 
                && colony.getCurrentBuildingProject() != null;

            if (isBuilder && myRoom.equals(getRoomBounds(colony, WorldSpaces.CONSTRUCTION_SITE))) {
                if (GameRandom.nextDouble() < 0.10) {
                    Point center = new Point((int)myRoom.getCenterX(), (int)myRoom.getCenterY());
                    int radius = myRoom.width / 2;
                    ant.moveTo(getCirclePoint(center, radius));
                }
            } else {
                if (GameRandom.nextDouble() < 0.10) {
                    ant.moveTo(getRandomPointInRoom(colony, myRoom, virtualWidth));
                }
            }
        } else {
            Room targetRoom = findRoomForAnt(colony, ant);
            if (targetRoom == null) targetRoom = WorldSpaces.STORAGE; 

            Room current = getRoomContainingAnt(colony, ant); 
            
            Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, targetRoom, ant);
            ant.setRoute(route);
        }
    }
    
    private Point getCirclePoint(Point center, int radius) {
        double angle = GameRandom.nextDouble() * 2 * Math.PI;
        double r = Math.sqrt(GameRandom.nextDouble()) * radius;
        int x = (int)(center.x + r * Math.cos(angle));
        int y = (int)(center.y + r * Math.sin(angle));
        return new Point(x, y);
    }
    
    private Room getRoomContainingAnt(Colony colony, Ant ant) {
        if (ant.getDimension() == WorldSpaces.UNDERWORLD) {
            if (getRoomBounds(colony, WorldSpaces.STORAGE).contains(ant.getX(), ant.getY())) return WorldSpaces.STORAGE;
            if (getRoomBounds(colony, WorldSpaces.NURSERY).contains(ant.getX(), ant.getY())) return WorldSpaces.NURSERY;
            if (getRoomBounds(colony, WorldSpaces.FARM).contains(ant.getX(), ant.getY())) return WorldSpaces.FARM;
            if (getRoomBounds(colony, WorldSpaces.ROYAL_CHAMBER).contains(ant.getX(), ant.getY())) return WorldSpaces.ROYAL_CHAMBER;
            
            if (colony.getBreederBounds() != null && colony.getBreederBounds().contains(ant.getX(), ant.getY())) {
                return WorldSpaces.BREEDER_CHAMBER;
            }
            if (colony.getTransitBounds() != null && colony.getTransitBounds().contains(ant.getX(), ant.getY())) {
                return WorldSpaces.TRANSIT_CHAMBER;
            }
        } 
        else if (ant.getDimension() == WorldSpaces.OVERWORLD) {
            if (getRoomBounds(colony, WorldSpaces.RANCHER_YARD).contains(ant.getX(), ant.getY())) return WorldSpaces.RANCHER_YARD;
            if (getRoomBounds(colony, WorldSpaces.GRAVEYARD).contains(ant.getX(), ant.getY())) return WorldSpaces.GRAVEYARD;
        }
        return null; 
    }
    
    private boolean isAntInRoom(Colony colony, Ant ant, Room room) {
        return ant.getDimension() == room.getDimension() 
            && getRoomBounds(colony, room).contains(ant.getX(), ant.getY());
    }

    // --- Room Logic & Estimates ---
    public Rectangle getTargetRoomForAnt(Colony colony, Ant ant, int gameWidth) {
        if ((ant.getRole() == GameConstants.ROLE_BUILDER || ant.getRole() == GameConstants.ROLE_CRANE) && colony.getCurrentBuildingProject() != null) {
            return getRoomBounds(colony, WorldSpaces.CONSTRUCTION_SITE);
        }
        if (ant.getRole() == GameConstants.ROLE_ASSISTANT) {
            return getRoomBounds(colony, WorldSpaces.ROYAL_CHAMBER);
        }

        Rectangle defined = colony.getTargetRoomForAnt(ant);
        if (defined != null) return defined;
        
        Room room = findRoomForAnt(colony, ant);
        if (room != null) return getRoomBounds(colony, room);
        
        return null;
    }
    
    private Rectangle getOverworldJobBounds(Colony colony, Ant ant) {
        if (ant.getRole() == GameConstants.ROLE_RANCHER) {
            return getRoomBounds(colony, WorldSpaces.RANCHER_YARD);
        }
        if (ant.getRole() == GameConstants.ROLE_CATCHER) {
            return null;
        }
        if (ant.getRole() == GameConstants.ROLE_GRAVER) {
            return getRoomBounds(colony, WorldSpaces.GRAVEYARD);
        }
        return null;
    }

    private Room findRoomForAnt(Colony colony, Ant ant) {
        if ((ant.getRole() == GameConstants.ROLE_BUILDER || ant.getRole() == GameConstants.ROLE_CRANE) && colony.getCurrentBuildingProject() != null) {
            return WorldSpaces.CONSTRUCTION_SITE;
        }
        
        if (ant.getAntType() == GameConstants.TYPE_DRONE || ant.getRole() == GameConstants.ROLE_BREEDER) {
            if (colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
                return WorldSpaces.BREEDER_CHAMBER;
            }
        }
        
        if ((ant.getRole() == GameConstants.ROLE_BORER || ant.getRole() == GameConstants.ROLE_ENGINEER)) {
            boolean hasTunnels = colony.getDynasty() != null && !colony.getDynasty().getTunnels().isEmpty();
            if (hasTunnels) {
                return WorldSpaces.TRANSIT_CHAMBER;
            }
        }
        
        if (ant.getRole() == GameConstants.ROLE_ASSISTANT) return WorldSpaces.ROYAL_CHAMBER;

        if (isAllowedInRoom(WorldSpaces.NURSERY, ant)) return WorldSpaces.NURSERY;
        if (isAllowedInRoom(WorldSpaces.FARM, ant)) return WorldSpaces.FARM;
        if (isAllowedInRoom(WorldSpaces.ROYAL_CHAMBER, ant)) return WorldSpaces.ROYAL_CHAMBER;
        
        return null;
    }
    
    private boolean isAllowedInRoom(Room room, Ant ant) {
        if (room == null || ant == null) return false;

        AntType type = ant.getAntType();
        if (type != null && room.getAllowedAntTypes().contains(type)) {
            return true;
        }
        
        AntRole role = ant.getRole();
        if (role != null && room.getAllowedAntRoles().contains(role)) {
            return true;
        }

        return false;
    }

    public Rectangle getRoomBounds(Colony colony, Room room) {
        if (room == WorldSpaces.RANCHER_YARD) {
            if (colony.getRancherBounds() != null) return colony.getRancherBounds();
            return new Rectangle(10, 10, room.getWidth(), room.getHeight());
        }
        
        if (room == WorldSpaces.GRAVEYARD) {
            if (colony.getGraverBounds() != null) return colony.getGraverBounds();
            int x = Math.max(0, ColonySpatialLayout.ANCHOR_WIDTH - room.getWidth() - 10);
            int y = Math.max(0, ColonySpatialLayout.ANCHOR_HEIGHT - room.getHeight() - 10);
            return new Rectangle(x, y, room.getWidth(), room.getHeight());
        }
        
        if (room == WorldSpaces.BREEDER_CHAMBER) {
            if (colony.getBreederBounds() != null) return colony.getBreederBounds();
            int cx = ColonySpatialLayout.ANCHOR_CENTER_X;
            return new Rectangle(cx - 256, 512, 256, 256);
        }

        if (room == WorldSpaces.TRANSIT_CHAMBER) {
            if (colony.getTransitBounds() != null) return colony.getTransitBounds();
            int cx = ColonySpatialLayout.ANCHOR_CENTER_X;
            return new Rectangle(cx + 64, 512, 512, 256);
        }

        if (room == WorldSpaces.CONSTRUCTION_SITE) {
            int cx = ColonySpatialLayout.ANCHOR_CENTER_X;
            int bottomY = 512; 
            boolean hasTunnels = colony.getDynasty() != null && !colony.getDynasty().getTunnels().isEmpty();
            if (colony.hasUpgrade(GameUnlocks.ROLE_BREEDER) || hasTunnels) {
                bottomY += 512;
            }
            int size = 256;
            return new Rectangle(cx - (size/2), bottomY, size, size);
        }

        int x = room.getFloorPoint().x;
        int y = room.getFloorPoint().y;
        
        if (room.getDimension() == WorldSpaces.UNDERWORLD) {
            int cx = ColonySpatialLayout.ANCHOR_CENTER_X;
            x += cx;
        }
        
        return new Rectangle(x, y, room.getWidth(), room.getHeight());
    }

    private void wanderInBoundaries(Colony colony, Critter entity, Rectangle bounds, double chance) {
        if (bounds == null) {
            return;
        }
        Rectangle walk = getBugWalkBounds(colony, bounds, entity);
        if (isPointInWalkBounds(walk, entity.getX(), entity.getY())) {
            if (GameRandom.nextDouble() < chance) {
                entity.moveTo(getRandomPointInWalkBounds(walk));
            }
        } else {
            entity.moveTo(getRandomPointInWalkBounds(walk));
        }
    }

    private Rectangle getBugWalkBounds(Colony colony, Rectangle bounds, Critter entity) {
        if (bounds.width <= 200 || bounds.height <= 200) {
            ImageIcon icon = entity.getSpecies().getSprite();
            int spriteW = icon != null ? Math.max(1, icon.getIconWidth()) : ColonySpatialLayout.ANT_SIZE;
            int spriteH = icon != null ? Math.max(1, icon.getIconHeight()) : ColonySpatialLayout.ANT_SIZE;
            return getCompactPenWalkBounds(bounds, spriteW, spriteH);
        }
        return getSafeWalkableBounds(colony, bounds, colony.getGameAreaWidth());
    }

    private Rectangle getCompactPenWalkBounds(Rectangle pen, int spriteW, int spriteH) {
        int pad = 4;
        int minX = pen.x + pad;
        int minY = pen.y + pad;
        int maxX = Math.max(minX, pen.x + pen.width - pad - spriteW);
        int maxY = Math.max(minY, pen.y + pen.height - pad - spriteH);
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private boolean isPointInWalkBounds(Rectangle walk, int x, int y) {
        return walk != null && walk.width > 0 && walk.height > 0 && walk.contains(x, y);
    }

    private Point getRandomPointInWalkBounds(Rectangle walk) {
        if (walk == null || walk.width <= 0 || walk.height <= 0) {
            return new Point(0, 0);
        }
        int x = walk.x + (walk.width <= 1 ? 0 : GameRandom.nextInt(walk.width));
        int y = walk.y + (walk.height <= 1 ? 0 : GameRandom.nextInt(walk.height));
        return new Point(x, y);
    }

    private Point getRandomPointInRoom(Colony colony, Rectangle r, int gameWidth) {
        Rectangle safe = getSafeWalkableBounds(colony, r, gameWidth);
        int x = safe.x + (int)(GameRandom.nextDouble() * safe.width);
        int y = safe.y + (int)(GameRandom.nextDouble() * safe.height);
        return new Point(x, y);
    }

    private Rectangle getSafeWalkableBounds(Colony colony, Rectangle r, int gameWidth) {
        int hallCenterX = ColonySpatialLayout.ANCHOR_CENTER_X;
        
        boolean isRightSide = r.getCenterX() > hallCenterX;
        int pLeft = ColonySpatialLayout.PAD_WALL;
        int pRight = ColonySpatialLayout.PAD_WALL;
        
        if (Math.abs(r.getCenterX() - hallCenterX) < (r.getWidth() + 200)) {
             if (isRightSide) pLeft = ColonySpatialLayout.PAD_DOOR; 
             else pRight = ColonySpatialLayout.PAD_DOOR;        
        }

        int minX = r.x + pLeft;
        int maxX = r.x + r.width - pRight - ColonySpatialLayout.ANT_SIZE;
        
        int minY = r.y + ColonySpatialLayout.PAD_TOP;
        int maxY = Math.max(minY, r.y + r.height - ColonySpatialLayout.PAD_BOTTOM - ColonySpatialLayout.ANT_SIZE);
        
        if (maxX < minX) maxX = minX; 
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    private boolean isPointInSafeBounds(Colony colony, Rectangle r, int x, int y) {
        if (r == null) return false;
        return getSafeWalkableBounds(colony, r, colony.getGameAreaWidth()).contains(x, y);
    }

    private Point getRandomOverworldPosition(Colony colony, ImageIcon sprite) {
        int w = (sprite != null) ? sprite.getIconWidth() : 0;
        int h = (sprite != null) ? sprite.getIconHeight() : 0;
        int width = Math.max(colony.getGameAreaWidth(), 2000);
        int height = Math.max(colony.getGameAreaHeight(), 2000);
        
        int maxX = Math.max(1, width - w);
        int maxY = Math.max(1, height - h);
        return new Point((int)(GameRandom.nextDouble() * maxX), (int)(GameRandom.nextDouble() * maxY));
    }

    private Point getRandomScoutPosition(Colony colony) {
        int width = Math.max(colony.getGameAreaWidth(), 2000);
        int height = Math.max(colony.getGameAreaHeight(), 2000);
        
        int minX = -500;
        int maxX = width + 500;
        int minY = -500;
        int maxY = height + 500;
        
        int x = minX + (int)(GameRandom.nextDouble() * (maxX - minX));
        int y = minY + (int)(GameRandom.nextDouble() * (maxY - minY));
        return new Point(x, y);
    }
    
    private boolean shouldBeInColony(Ant ant) {
        if (isAllowedInRoom(WorldSpaces.NURSERY, ant)) return true;
        if (isAllowedInRoom(WorldSpaces.FARM, ant)) return true;
        if (isAllowedInRoom(WorldSpaces.ROYAL_CHAMBER, ant)) return true;
        if (isAllowedInRoom(WorldSpaces.STORAGE, ant)) return true; 
        
        if (ant.getRole() == GameConstants.ROLE_BUILDER || ant.getRole() == GameConstants.ROLE_CRANE) return true;
        if (ant.getRole() == GameConstants.ROLE_BREEDER || ant.getAntType() == GameConstants.TYPE_DRONE) return true;
        if (ant.getRole() == GameConstants.ROLE_ASSISTANT) return true;
        
        return false;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }
    
    public Point getSpecificRoomPoint(Colony c, Rectangle r) { 
        return getRandomPointInRoom(c, r, c.getGameAreaWidth()); 
    }

    public void tickAntSpriteAnimMinutes(Colony colony) {
        tickAntSpriteAnimMinutes(colony, null, null);
    }

    public void tickAntSpriteAnimMinutes(Colony colony, Dimension activeDimension, Rectangle viewportBounds) {
        if (colony == null) {
            return;
        }
        boolean lodActive = ViewportPhysicsLod.isLodActive(viewportBounds);
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            AntType type = entry.getKey();
            if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_LARVA
                    || type == GameConstants.TYPE_PUPA || type == GameConstants.TYPE_DEAD) {
                continue;
            }
            ImageIcon spriteIcon = lodActive
                    ? GameConstants.getAntSprite(type, colony.getSpecies())
                    : null;
            int spriteW = spriteIcon != null ? spriteIcon.getIconWidth() : 16;
            int spriteH = spriteIcon != null ? spriteIcon.getIconHeight() : 16;
            List<Ant> ants = entry.getValue();
            synchronized (ants) {
                for (Ant ant : ants) {
                    if (!ant.isAlive()) {
                        continue;
                    }
                    if (lodActive) {
                        if (activeDimension != null && ant.getDimension() != activeDimension) {
                            continue;
                        }
                        if (!ant.hasOpenSpriteAnim()
                                && !ViewportPhysicsLod.antIntersectsViewport(
                                        viewportBounds, ant.getX(), ant.getY(), spriteW, spriteH)) {
                            continue;
                        }
                    }
                    ant.tickSpriteAnimMinute();
                }
            }
        }
    }

    public void rollAntSpriteAnimHourly(Colony colony, Dimension activeDimension, Rectangle viewportBounds) {
        if (colony == null || activeDimension == null || !ViewportPhysicsLod.isLodActive(viewportBounds)) {
            return;
        }
        boolean sample = ColonyAntAnimSampleLod.isSampleActive(colony.getAntTotal());
        Map<Long, List<Ant>> sampleBuckets = sample ? new HashMap<>() : null;
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            AntType type = entry.getKey();
            if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_LARVA
                    || type == GameConstants.TYPE_PUPA || type == GameConstants.TYPE_DEAD
                    || type == GameConstants.TYPE_ZOMBIE) {
                continue;
            }
            ImageIcon spriteIcon = GameConstants.getAntSprite(type, colony.getSpecies());
            int spriteW = spriteIcon != null ? spriteIcon.getIconWidth() : 16;
            int spriteH = spriteIcon != null ? spriteIcon.getIconHeight() : 16;
            List<Ant> ants = entry.getValue();
            synchronized (ants) {
                for (Ant ant : ants) {
                    if (!ant.isAlive() || ant.getDimension() != activeDimension) {
                        continue;
                    }
                    if (!ViewportPhysicsLod.antIntersectsViewport(
                            viewportBounds, ant.getX(), ant.getY(), spriteW, spriteH)) {
                        continue;
                    }
                    if (sample) {
                        long key = ColonyAntAnimSampleLod.hourlyAnimSampleKey(ant, type);
                        sampleBuckets.computeIfAbsent(key, ignored -> new ArrayList<>()).add(ant);
                    } else {
                        ant.rollHourlySpriteAnim();
                    }
                }
            }
        }
        if (sample && sampleBuckets != null) {
            for (List<Ant> bucket : sampleBuckets.values()) {
                if (bucket.isEmpty()) {
                    continue;
                }
                Ant representative = bucket.get(0);
                representative.rollHourlySpriteAnim();
                for (int i = 1; i < bucket.size(); i++) {
                    bucket.get(i).syncSpriteAnimFrom(representative);
                }
            }
        }
    }
}