package com.grimidk.formicempire.classes.infrasctructure;

@FunctionalInterface
public interface WorldGenStepListener {
    void onStepCompleted(int stepNumber, String stepName, World world);
}
