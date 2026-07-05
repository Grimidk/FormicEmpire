package com.grimidk.formicempire.classes.entities.services.world;

/** Sub-phases within a single war stage (one contested colony hex). */
public enum WarStagePhase {
    /** Dynasty-wide active militaries clash at the front. */
    ACTIVE_CLASH,
    /** Surviving active military assaults reserves on the contested hex. */
    RESERVE_ASSAULT,
    /** One-day pause between stages while armies redeploy. */
    REDEPLOYING
}
