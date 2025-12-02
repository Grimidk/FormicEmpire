package com.grimidk.formicempire.classes.infrasctructure;

import java.awt.Point;
import java.util.List;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.BugType;

public class Room {
    private final int id;
    private final String name;
    private final Dimension dimension;
    private final int height;
    private final int width;
    private final boolean isRightAccess;
    private final Point floorPoint;
    private final Point centerPoint;
    private final Point entryPoint; 
    private final Point exitPoint;
    private final List<AntRole> allowedAntRoles;
    private final List<AntType> allowedAntTypes;
    private final List<BugType> allowedBugTypes;

    public Room(int id, String name, Dimension dimension, int height, int width, boolean isRightAccess,
                Point floorPoint, Point centerPoint, Point entryPoint, Point exitPoint,
                List<AntRole> allowedAntRoles, List<AntType> allowedAntTypes, List<BugType> allowedBugTypes) {
        this.id = id;
        this.name = name;
        this.dimension = dimension;
        this.height = height;
        this.width = width;
        this.isRightAccess = isRightAccess;
        this.floorPoint = floorPoint;
        this.centerPoint = centerPoint;
        this.entryPoint = entryPoint;
        this.exitPoint = exitPoint;
        this.allowedAntRoles = allowedAntRoles;
        this.allowedAntTypes = allowedAntTypes;
        this.allowedBugTypes = allowedBugTypes;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Dimension getDimension() { return dimension; }
    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public boolean isRightAccess() { return isRightAccess; }
    public Point getFloorPoint() { return floorPoint; }
    public Point getCenterPoint() { return centerPoint; }
    public Point getEntryPoint() { return entryPoint; }
    public Point getExitPoint() { return exitPoint; }
    public List<AntRole> getAllowedAntRoles() { return allowedAntRoles; }
    public List<AntType> getAllowedAntTypes() { return allowedAntTypes; }
    public List<BugType> getAllowedBugTypes() { return allowedBugTypes; }
}
