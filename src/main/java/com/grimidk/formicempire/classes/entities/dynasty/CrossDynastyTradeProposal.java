package com.grimidk.formicempire.classes.entities.dynasty;

import java.util.HashMap;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class CrossDynastyTradeProposal {

    public enum Kind {
        OFFER,
        REQUEST
    }

    private final int fromDynastyId;
    private final int originColonyId;
    private final int destinationColonyId;
    private final Kind kind;
    private final Map<Integer, Double> loadByResourceId;

    public CrossDynastyTradeProposal(
            int fromDynastyId,
            int originColonyId,
            int destinationColonyId,
            Kind kind,
            Map<ResourceType, Double> load) {
        this.fromDynastyId = fromDynastyId;
        this.originColonyId = originColonyId;
        this.destinationColonyId = destinationColonyId;
        this.kind = kind;
        this.loadByResourceId = new HashMap<>();
        if (load != null) {
            for (Map.Entry<ResourceType, Double> entry : load.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue() >= 1.0) {
                    loadByResourceId.put(entry.getKey().getId(), entry.getValue());
                }
            }
        }
    }

    public CrossDynastyTradeProposal(
            int fromDynastyId,
            int originColonyId,
            int destinationColonyId,
            Kind kind,
            Map<Integer, Double> loadByResourceId,
            boolean keyedById) {
        this.fromDynastyId = fromDynastyId;
        this.originColonyId = originColonyId;
        this.destinationColonyId = destinationColonyId;
        this.kind = kind;
        this.loadByResourceId = loadByResourceId != null ? new HashMap<>(loadByResourceId) : new HashMap<>();
    }

    public int getFromDynastyId() {
        return fromDynastyId;
    }

    public int getOriginColonyId() {
        return originColonyId;
    }

    public int getDestinationColonyId() {
        return destinationColonyId;
    }

    public Kind getKind() {
        return kind;
    }

    public Map<Integer, Double> copyLoadByResourceId() {
        return new HashMap<>(loadByResourceId);
    }

    public Map<ResourceType, Double> toResourceLoad() {
        Map<ResourceType, Double> load = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : loadByResourceId.entrySet()) {
            ResourceType type = GameConstants.getResourceById(entry.getKey());
            if (type != null && entry.getValue() != null && entry.getValue() >= 1.0) {
                load.put(type, entry.getValue());
            }
        }
        return load;
    }

    public boolean matchesRoute(int proposerDynastyId, int originId, int destinationId) {
        return fromDynastyId == proposerDynastyId
                && originColonyId == originId
                && destinationColonyId == destinationId;
    }

    public boolean isEmpty() {
        return loadByResourceId.isEmpty();
    }
}
