package com.grimidk.formicempire.classes.entities.services;

import java.util.ArrayList;
import java.util.List;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;

public class ColonyTradeService {

    private final Colony colony;

    public ColonyTradeService(Colony colony) {
        this.colony = colony;
    }

    public List<Colony> getNeighborColonies(World world) {
        List<Colony> neighbors = new ArrayList<>();
        Hex center = world.getHexOfColony(colony);
        if (center == null) return neighbors;

        Hex[] adjacent = {
            center.getNorth(), center.getNorthWest(), center.getNorthEast(),
            center.getSouth(), center.getSouthWest(), center.getSouthEast()
        };

        for (Hex h : adjacent) {
            if (h != null && h.getColony() != null && h.getColony() != colony) {
                neighbors.add(h.getColony());
            }
        }
        return neighbors;
    }
    
    public Hex getNeighborHex(World world, Colony neighbor) {
        Hex center = world.getHexOfColony(colony);
        if (center == null) return null;

        Hex[] adjacent = {
            center.getNorth(), center.getNorthWest(), center.getNorthEast(),
            center.getSouth(), center.getSouthWest(), center.getSouthEast()
        };

        for (Hex h : adjacent) {
            if (h != null && h.getColony() == neighbor) {
                return h;
            }
        }
        return null;
    }
}
