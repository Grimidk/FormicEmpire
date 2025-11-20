package com.grimidk.formicempire.classes.constants.ant;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class AntRole extends Constant {
    private final AntType antType;

    public AntRole(int id, AntType antType, String name, ImageIcon icon) {
        super(id, name, icon);
        this.antType = antType;
    }

    //(no icon)
    public AntRole(int id, AntType antType, String name) {
        this(id, antType, name, null);
    }

    public AntType getAntType() {
        return antType;
    }
}
