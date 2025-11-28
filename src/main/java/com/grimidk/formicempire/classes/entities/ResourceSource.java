package com.grimidk.formicempire.classes.entities;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;

public class ResourceSource {
    private final ResourceType resourceType;
    private final int initialQuantity;
    private int quantity; 
    private final int x;
    private final int y;

    public ResourceSource(ResourceType resourceType, int quantity, int x, int y) {
        this.resourceType = resourceType;
        this.quantity = quantity;
        this.initialQuantity = quantity; 
        this.x = x;
        this.y = y;
    }
    
    public ResourceSource(ResourceType resourceType, int quantity, int initialQuantity, int x, int y) {
        this.resourceType = resourceType;
        this.quantity = quantity;
        this.initialQuantity = initialQuantity;
        this.x = x;
        this.y = y;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public int getQuantity() {
        return quantity;
    }
    
    public int getInitialQuantity() {
        return initialQuantity;
    }
    
    public void decreaseQuantity(int amount) {
        this.quantity -= amount;
        if (this.quantity < 0) this.quantity = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}