package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Bug;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.Room;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.swing.ImageIcon;

public class ColonyPhysicsService {

    private static final int ANCHOR_WIDTH = 550;
    private static final int ANCHOR_HEIGHT = 500;
    private static final int ANCHOR_CENTER_X = ANCHOR_WIDTH / 2;

    // --- Main Physics Loop ---
    public void runPhysics(Colony colony, Dimension activeDimension) {
        runPhysics(colony, activeDimension, null, 0L);
    }

    /**
     * @param viewportBounds panel coordinates of the scroll viewport; {@code null} disables viewport LOD
     * @param physicsStepIndex monotonic step counter for LOD hashing (unused when viewport is null)
     */
    public void runPhysics(Colony colony, Dimension activeDimension, Rectangle viewportBounds, long physicsStepIndex) {
        // -- Ants --
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            AntType type = entry.getKey();

            ImageIcon spriteIcon = GameConstants.getAntSprite(type, colony.getSpecies());
            int spriteW = spriteIcon != null ? spriteIcon.getIconWidth() : 16;
            int spriteH = spriteIcon != null ? spriteIcon.getIconHeight() : 16;

            List<Ant> antList = entry.getValue();

            synchronized (antList) {
                // CopyOnWriteArrayList: indexed loops are unsafe because updateAntLogic may remove ants.
                // Enhanced for uses a snapshot iterator; removals during this step cannot cause get(index) faults.
                for (Ant ant : antList) {
                    if (!ant.isAlive()) {
                        continue;
                    }

                    boolean sameDim = ant.getDimension() == activeDimension;
                    boolean lodSameDim = ViewportPhysicsLod.isLodActive(viewportBounds) && sameDim;
                    boolean inView = ViewportPhysicsLod.antIntersectsViewport(
                        viewportBounds, ant.getX(), ant.getY(), spriteW, spriteH);

                    boolean shouldRunAI;
                    if (!sameDim) {
                        shouldRunAI = Math.random() < 0.05;
                    } else if (lodSameDim && !inView) {
                        shouldRunAI = ViewportPhysicsLod.shouldRunOffViewportAi(physicsStepIndex, ant);
                    } else {
                        shouldRunAI = true;
                    }

                    if (!ant.isMoving() && ant.hasRoute()) {
                        processNextRoutePoint(ant);
                    }

                    if (!ant.isMoving() && !ant.hasRoute() && shouldRunAI) {
                        updateAntLogic(colony, ant);
                    }

                    float moveSpeed = GameConstants.BASE_SPRITE_SPEED;
                    if (lodSameDim && !inView) {
                        if (!ViewportPhysicsLod.shouldRunOffViewportPosition(physicsStepIndex, ant)) {
                            continue;
                        }
                        moveSpeed = ViewportPhysicsLod.compensatedMoveSpeed(GameConstants.BASE_SPRITE_SPEED);
                    }
                    if (type == GameConstants.TYPE_WORKER && colony.hasUpgrade(GameUnlocks.STAT_WORKER_SPEED_2)) {
                        moveSpeed *= 2f;
                    }
                    ant.updatePosition(moveSpeed);
                }
            }
        }

        // -- Bugs --
        List<Bug> bugs = colony.getBugs();
        synchronized (bugs) {
            for (Bug bug : bugs) {
                if (!bug.isAlive() || bug.getDimension() != activeDimension) {
                    continue;
                }

                ImageIcon bugIcon = bug.getBugType().getSprite();
                int bw = bugIcon != null ? bugIcon.getIconWidth() : 16;
                int bh = bugIcon != null ? bugIcon.getIconHeight() : 16;

                boolean lod = ViewportPhysicsLod.isLodActive(viewportBounds);
                boolean bugInView = ViewportPhysicsLod.antIntersectsViewport(
                    viewportBounds, bug.getX(), bug.getY(), bw, bh);
                int bugHash = System.identityHashCode(bug);

                boolean runBugAi = !lod || bugInView
                    || ViewportPhysicsLod.shouldRunOffViewportBugAi(physicsStepIndex, bugHash);
                if (!bug.isMoving() && runBugAi) {
                    updateBugLogic(colony, bug);
                }

                float bugMove = GameConstants.BASE_SPRITE_SPEED;
                if (lod && !bugInView) {
                    if (!ViewportPhysicsLod.shouldRunOffViewportBugMove(physicsStepIndex, bugHash)) {
                        continue;
                    }
                    bugMove = ViewportPhysicsLod.compensatedMoveSpeed(GameConstants.BASE_SPRITE_SPEED);
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
        
        List<Bug> bugs = colony.getBugs();
        synchronized (bugs) {
            for (Bug bug : bugs) {
                if (bug.getBugType() == GameConstants.TYPE_APHID && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
                    Rectangle yard = getRoomBounds(colony, WorldSpaces.RANCHER_YARD);
                    bug.setPosition(getRandomPointInRoom(colony, yard, virtualWidth));
                } else if (bug.getBugType() == GameConstants.TYPE_PARASITE) {
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
            Point exit = new Point(ANCHOR_CENTER_X, 0);
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
                int side = (int)(Math.random() * 4);
                int tx = 0, ty = 0;
                int buffer = 200;
                switch(side) {
                    case 0: tx = (int)(Math.random() * gameWidth); ty = -buffer; break;
                    case 1: tx = (int)(Math.random() * gameWidth); ty = gameHeight + buffer; break;
                    case 2: tx = -buffer; ty = (int)(Math.random() * gameHeight); break;
                    case 3: tx = gameWidth + buffer; ty = (int)(Math.random() * gameHeight); break;
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
        
        if (target != null) {
            double d = dist(ant.getX(), ant.getY(), target.getX(), target.getY());
            
            if (d < 50 && ant.getDimension() == WorldSpaces.OVERWORLD) {
                ant.setCarrying(target.getResourceType());
            } else {
                ant.clearLoad();
                
                Room current = getRoomContainingAnt(colony, ant);
                Room sourceRoom = colony.getLocationService().createTempRoomAtPoint(
                    new Point(target.getX(), target.getY()), 
                    WorldSpaces.OVERWORLD
                );
                
                Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, sourceRoom, ant);
                ant.setRoute(route);
            }
        } else {
            if (ant.getDimension() == WorldSpaces.OVERWORLD) {
                 if (Math.random() < 0.01) {
                    if (ant.getRole() == GameConstants.ROLE_SCOUT || isGatherer(ant)) {
                         ant.moveTo(getRandomScoutPosition(colony));
                    } else {
                         ant.moveTo(getRandomOverworldPosition(colony, GameConstants.getAntSprite(ant.getAntType(), colony.getSpecies())));
                    }
                }
            } else {
                 handleUnderworldAnt(colony, ant); 
            }
        }
    }

    private void updateBugLogic(Colony colony, Bug bug) {
        if (bug.getDimension() == WorldSpaces.OVERWORLD && bug.getBugType() == GameConstants.TYPE_APHID) {
            if (colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
                Rectangle yard = getRoomBounds(colony, WorldSpaces.RANCHER_YARD);
                wanderInBoundaries(colony, bug, yard, 0.05);
            } else {
                bug.setPosition(new Point(-1000, -1000));
            }
        }
        else if (bug.getBugType() == GameConstants.TYPE_PARASITE) {
            if (bug.getDimension() != WorldSpaces.UNDERWORLD) {
                bug.setDimension(WorldSpaces.UNDERWORLD);
            }

            Rectangle currentRoom = getRoomBounds(colony, WorldSpaces.STORAGE);
            if (Math.random() < 0.3) currentRoom = getRoomBounds(colony, WorldSpaces.FARM);

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
                ant.setPosition(new Point(ANCHOR_CENTER_X, 50));
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
                    if (Math.random() < 0.05) {
                        ant.moveTo(getRandomPointInRoom(colony, yard, colony.getGameAreaWidth()));
                    }
                } else {
                    Room targetRoom = (ant.getRole() == GameConstants.ROLE_GRAVER) ? WorldSpaces.GRAVEYARD : WorldSpaces.RANCHER_YARD;
                    Room current = getRoomContainingAnt(colony, ant); 
                    Queue<NeoPoint> route = colony.getLocationService().calculateRoute(colony, current, targetRoom, ant);
                    ant.setRoute(route);
                }
            } else {
                if (Math.random() < 0.01) {
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
                 ant.setPosition(new Point(ANCHOR_CENTER_X, 0));
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
                if (Math.random() < 0.10) {
                    Point center = new Point((int)myRoom.getCenterX(), (int)myRoom.getCenterY());
                    int radius = myRoom.width / 2;
                    ant.moveTo(getCirclePoint(center, radius));
                }
            } else {
                if (Math.random() < 0.10) {
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
        double angle = Math.random() * 2 * Math.PI;
        double r = Math.sqrt(Math.random()) * radius;
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
            int x = Math.max(0, ANCHOR_WIDTH - room.getWidth() - 10);
            int y = Math.max(0, ANCHOR_HEIGHT - room.getHeight() - 10);
            return new Rectangle(x, y, room.getWidth(), room.getHeight());
        }
        
        if (room == WorldSpaces.BREEDER_CHAMBER) {
            if (colony.getBreederBounds() != null) return colony.getBreederBounds();
            int cx = ANCHOR_CENTER_X;
            return new Rectangle(cx - 256, 512, 256, 256);
        }

        if (room == WorldSpaces.TRANSIT_CHAMBER) {
            if (colony.getTransitBounds() != null) return colony.getTransitBounds();
            int cx = ANCHOR_CENTER_X;
            return new Rectangle(cx + 64, 512, 512, 256);
        }

        if (room == WorldSpaces.CONSTRUCTION_SITE) {
            int cx = ANCHOR_CENTER_X;
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
            int cx = ANCHOR_CENTER_X;
            x += cx;
        }
        
        return new Rectangle(x, y, room.getWidth(), room.getHeight());
    }

    private void wanderInBoundaries(Colony colony, Bug entity, Rectangle bounds, double chance) {
        if (isPointInSafeBounds(colony, bounds, entity.getX(), entity.getY())) {
            if (Math.random() < chance) {
                entity.moveTo(getRandomPointInRoom(colony, bounds, colony.getGameAreaWidth()));
            }
        } else {
            entity.moveTo(new Point((int)bounds.getCenterX(), (int)bounds.getCenterY()));
        }
    }

    private Point getRandomPointInRoom(Colony colony, Rectangle r, int gameWidth) {
        Rectangle safe = getSafeWalkableBounds(colony, r, gameWidth);
        int x = safe.x + (int)(Math.random() * safe.width);
        int y = safe.y + (int)(Math.random() * safe.height);
        return new Point(x, y);
    }

    private Rectangle getSafeWalkableBounds(Colony colony, Rectangle r, int gameWidth) {
        int hallCenterX = ANCHOR_CENTER_X;
        
        boolean isRightSide = r.getCenterX() > hallCenterX;
        int pLeft = ColonyLocationService.PAD_WALL;
        int pRight = ColonyLocationService.PAD_WALL;
        
        if (Math.abs(r.getCenterX() - hallCenterX) < (r.getWidth() + 200)) {
             if (isRightSide) pLeft = ColonyLocationService.PAD_DOOR; 
             else pRight = ColonyLocationService.PAD_DOOR;        
        }

        int minX = r.x + pLeft;
        int maxX = r.x + r.width - pRight - ColonyLocationService.ANT_SIZE;
        
        int minY = r.y + ColonyLocationService.PAD_TOP;
        int maxY = Math.max(minY, r.y + r.height - ColonyLocationService.PAD_BOTTOM - ColonyLocationService.ANT_SIZE);
        
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
        return new Point((int)(Math.random() * maxX), (int)(Math.random() * maxY));
    }

    private Point getRandomScoutPosition(Colony colony) {
        int width = Math.max(colony.getGameAreaWidth(), 2000);
        int height = Math.max(colony.getGameAreaHeight(), 2000);
        
        int minX = -500;
        int maxX = width + 500;
        int minY = -500;
        int maxY = height + 500;
        
        int x = minX + (int)(Math.random() * (maxX - minX));
        int y = minY + (int)(Math.random() * (maxY - minY));
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
}