package com.grimidk.formicempire.classes.constants.critter.ant;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class SpeciesDiet extends Constant {
    public SpeciesDiet(int id, String name, ImageIcon icon) {
        super(id, name, icon);
    }

    @Override
    public String toString() {
        return getName();
    }
}

