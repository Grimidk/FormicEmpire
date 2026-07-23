package com.grimidk.formicempire.classes.constants.dynasty;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class WarStagePhase extends Constant {
    private final String persistenceKey;

    public WarStagePhase(int id, String nameKey, String persistenceKey, ImageIcon icon) {
        super(id, nameKey, icon);
        this.persistenceKey = persistenceKey;
    }

    /** Save/load key (legacy enum name). Not a display string. */
    public String getPersistenceKey() {
        return persistenceKey;
    }
}
