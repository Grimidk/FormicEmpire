package com.grimidk.formicempire.classes.constants.misc;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

public class ResourceType extends Constant {
    public static final int SOURCE_QTY_SMALL = 100;
    public static final int SOURCE_QTY_MEDIUM = 1_000;
    public static final int SOURCE_QTY_BIG = 10_000;
    public static final int SOURCE_QTY_HUGE = 100_000;

    private final boolean isEdible;
    private final boolean isLiquid;
    private final ImageIcon sourceSpriteSmall;
    private final ImageIcon sourceSpriteMedium;
    private final ImageIcon sourceSpriteBig;
    private final ImageIcon sourceSpriteHuge;

    public ResourceType(int id, String name, boolean isEdible, boolean isLiquid, ImageIcon icon,
            ImageIcon sourceSpriteSmall, ImageIcon sourceSpriteMedium, ImageIcon sourceSpriteBig, ImageIcon sourceSpriteHuge) {
        super(id, name, icon);
        this.isEdible = isEdible;
        this.isLiquid = isLiquid;
        this.sourceSpriteSmall = sourceSpriteSmall;
        this.sourceSpriteMedium = sourceSpriteMedium;
        this.sourceSpriteBig = sourceSpriteBig;
        this.sourceSpriteHuge = sourceSpriteHuge;
    }

    public boolean isIsEdible() {
        return isEdible;
    }

    public boolean isIsLiquid() {
        return isLiquid;
    }

    public ImageIcon getSourceSpriteSmall() {
        return sourceSpriteSmall;
    }

    public ImageIcon getSourceSpriteMedium() {
        return sourceSpriteMedium;
    }

    public ImageIcon getSourceSpriteBig() {
        return sourceSpriteBig;
    }

    public ImageIcon getSourceSpriteHuge() {
        return sourceSpriteHuge;
    }

    public ImageIcon getIconForSourceQuantity(int quantity) {
        if (quantity >= SOURCE_QTY_HUGE && sourceSpriteHuge != null) {
            return sourceSpriteHuge;
        }
        if (quantity >= SOURCE_QTY_BIG && sourceSpriteBig != null) {
            return sourceSpriteBig;
        }
        if (quantity >= SOURCE_QTY_MEDIUM && sourceSpriteMedium != null) {
            return sourceSpriteMedium;
        }
        if (quantity >= SOURCE_QTY_SMALL && sourceSpriteSmall != null) {
            return sourceSpriteSmall;
        }
        return getIcon();
    }

    public int getDisplaySizeForSourceQuantity(int quantity) {
        if (quantity >= SOURCE_QTY_HUGE) {
            return GameConstants.SOURCE_DISPLAY_PX_HUGE;
        }
        if (quantity >= SOURCE_QTY_BIG) {
            return GameConstants.SOURCE_DISPLAY_PX_BIG;
        }
        if (quantity >= SOURCE_QTY_MEDIUM) {
            return GameConstants.SOURCE_DISPLAY_PX_MEDIUM;
        }
        return GameConstants.SOURCE_DISPLAY_PX_SMALL;
    }
}
