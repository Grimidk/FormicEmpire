package com.grimidk.formicempire.classes.entities;

import javax.swing.ImageIcon;

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

    public ImageIcon getIconForDisplay() {
        return resourceType.getIconForSourceQuantity(quantity);
    }

    public int getDisplaySizePx() {
        return resourceType.getDisplaySizeForSourceQuantity(quantity);
    }

    public int getCenterX() {
        return getX() + getDisplaySizePx() / 2;
    }

    public int getCenterY() {
        return getY() + getDisplaySizePx() / 2;
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