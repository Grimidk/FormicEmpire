package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    void sanitizeRejectsWarEconomyRole() {
        int safe = Engine.sanitizeDefaultRoleId(
                GameConstants.TYPE_SOLDIER,
                GameConstants.ROLE_WARRIOR.getId(),
                GameConstants.ROLE_MILITIA.getId());
        assertEquals(GameConstants.ROLE_MILITIA.getId(), safe);
    }

    @Test
    void eligibleDefaultHatchRolesIncludeEveryImplementedPeacetimeRole() {
        assertEquals(
                List.of(
                        GameConstants.ROLE_FORAGER,
                        GameConstants.ROLE_NURSE,
                        GameConstants.ROLE_BUILDER,
                        GameConstants.ROLE_SCOUT,
                        GameConstants.ROLE_FARMER,
                        GameConstants.ROLE_RANCHER,
                        GameConstants.ROLE_GRAVER,
                        GameConstants.ROLE_COURIER,
                        GameConstants.ROLE_ENGINEER),
                GameConstants.eligibleDefaultHatchRoles(GameConstants.TYPE_WORKER));
        assertEquals(
                List.of(
                        GameConstants.ROLE_MINER,
                        GameConstants.ROLE_POLICE,
                        GameConstants.ROLE_HUNTER,
                        GameConstants.ROLE_CATCHER,
                        GameConstants.ROLE_ESCORT),
                GameConstants.eligibleDefaultHatchRoles(GameConstants.TYPE_SOLDIER));
        assertEquals(
                List.of(
                        GameConstants.ROLE_BORER,
                        GameConstants.ROLE_CRANE,
                        GameConstants.ROLE_TRANSPORT),
                GameConstants.eligibleDefaultHatchRoles(GameConstants.TYPE_MAJOR));
        assertEquals(
                List.of(
                        GameConstants.ROLE_BREEDER,
                        GameConstants.ROLE_DIPLOMAT,
                        GameConstants.ROLE_ASSISTANT,
                        GameConstants.ROLE_SKYTRANS),
                GameConstants.eligibleDefaultHatchRoles(GameConstants.TYPE_PRINCESS));
        assertEquals(
                List.of(
                        GameConstants.ROLE_LAYER,
                        GameConstants.ROLE_RESEARCHER),
                GameConstants.eligibleDefaultHatchRoles(GameConstants.TYPE_QUEEN));
    }

    @Test
    void builtinDefaultsMatchResolvedFreshEngine() {
        Engine engine = new Engine();
        engine.setDefaultRoleWorker(GameConstants.ROLE_FORAGER.getId());
        engine.setDefaultRoleSoldier(GameConstants.ROLE_HUNTER.getId());
        engine.setDefaultRoleMajor(GameConstants.ROLE_CRANE.getId());
        engine.setDefaultRolePrincess(GameConstants.ROLE_BREEDER.getId());
        engine.setDefaultRoleQueen(GameConstants.ROLE_LAYER.getId());
        assertEquals(GameConstants.ROLE_FORAGER,
                Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_WORKER, engine));
        assertEquals(GameConstants.ROLE_HUNTER,
                Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_SOLDIER, engine));
        assertEquals(GameConstants.ROLE_CRANE,
                Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_MAJOR, engine));
        assertEquals(GameConstants.ROLE_BREEDER,
                Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_PRINCESS, engine));
        assertEquals(GameConstants.ROLE_LAYER,
                Engine.resolveDefaultRoleForAntType(GameConstants.TYPE_QUEEN, engine));
        assertEquals(GameConstants.ROLE_FORAGER, Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_WORKER));
        assertEquals(GameConstants.ROLE_HUNTER, Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_SOLDIER));
        assertEquals(GameConstants.ROLE_CRANE, Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_MAJOR));
        assertEquals(GameConstants.ROLE_BREEDER, Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_PRINCESS));
        assertEquals(GameConstants.ROLE_LAYER, Engine.builtinDefaultRoleForAntType(GameConstants.TYPE_QUEEN));
    }
}