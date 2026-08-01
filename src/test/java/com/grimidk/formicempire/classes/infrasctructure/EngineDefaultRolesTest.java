package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EngineDefaultRolesTest {

    @Test
    void sanitizeAcceptsRoleMatchingType() {
        int safe = Engine.sanitizeDefaultRoleId(
                GameConstants.TYPE_WORKER,
                GameConstants.ROLE_NURSE.getId(),
                GameConstants.ROLE_FORAGER.getId());
        assertEquals(GameConstants.ROLE_NURSE.getId(), safe);
    }

    @Test
    void sanitizeRejectsWrongTypeAndUsesFallback() {
        int safe = Engine.sanitizeDefaultRoleId(
                GameConstants.TYPE_WORKER,
                GameConstants.ROLE_HUNTER.getId(),
                GameConstants.ROLE_FORAGER.getId());
        assertEquals(GameConstants.ROLE_FORAGER.getId(), safe);
    }

    @Test
    void sanitizeRejectsUnknownId() {
        int safe = Engine.sanitizeDefaultRoleId(
                GameConstants.TYPE_SOLDIER,
                -1,
                GameConstants.ROLE_HUNTER.getId());
        assertEquals(GameConstants.ROLE_HUNTER.getId(), safe);
    }

    @Test
    void resolveUsesLegacyWhenEngineNull() {
        AntRole r = Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_WORKER, null);
        assertEquals(GameConstants.ROLE_FORAGER, r);
    }

    @Test
    void resolveUsesEngineWhenValid() {
        Engine engine = new Engine();
        engine.setDefaultRoleWorker(GameConstants.ROLE_NURSE.getId());
        AntRole r = Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_WORKER, engine);
        assertEquals(GameConstants.ROLE_NURSE, r);
    }

    @Test
    void resolveFallsBackWhenEngineStoresInvalidRoleForType() {
        Engine engine = new Engine();
        engine.setDefaultRoleWorker(GameConstants.ROLE_HUNTER.getId());
        AntRole r = Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_WORKER, engine);
        assertEquals(GameConstants.ROLE_FORAGER, r);
    }

    @Test
    void resolveRejectsWarExclusiveDefaultRoleFromEngine() {
        Engine engine = new Engine();
        engine.setDefaultRoleSoldier(GameConstants.ROLE_WARRIOR.getId());
        AntRole r = Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_SOLDIER, engine);
        assertEquals(GameConstants.ROLE_HUNTER, r);
    }

    @Test
    void sanitizeRejectsWarExclusiveRole() {
        int safe = Engine.sanitizeDefaultRoleId(
                GameConstants.TYPE_SOLDIER,
                GameConstants.ROLE_DEFENDER.getId(),
                GameConstants.ROLE_HUNTER.getId());
        assertEquals(GameConstants.ROLE_HUNTER.getId(), safe);
    }

    @Test
    void legacyMajorDefaultIsPeacetimeRole() {
        AntRole r = Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_MAJOR, null);
        assertEquals(GameConstants.ROLE_CRANE, r);
    }

    @Test
    void defaultRoleIdForAntTypeMatchesResolve() {
        Engine engine = new Engine();
        engine.setDefaultRoleMajor(GameConstants.ROLE_CRANE.getId());
        int id = Engine.defaultRoleIdForAntType(GameConstants.TYPE_MAJOR, engine);
        assertEquals(GameConstants.ROLE_CRANE.getId(), id);
    }

    @Test
    void sanitizeRejectsUnobtainableRole() {
        int safe = Engine.sanitizeDefaultRoleId(
                GameConstants.TYPE_MAJOR,
                GameConstants.ROLE_CARRIER.getId(),
                GameConstants.ROLE_CRANE.getId());
        assertEquals(GameConstants.ROLE_CRANE.getId(), safe);
    }
}
