package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class HexGridMeshTest {

    @Test
    void sharedNeighborEdgeIsCreatedOnce() {
        Hex a = hex(0, 0);
        Hex b = hex(1, 0);
        HexGridMesh mesh = HexGridMesh.build(
                List.of(a, b),
                0,
                0,
                20,
                (q, r) -> {
                    if (q == 0 && r == 0) {
                        return a;
                    }
                    if (q == 1 && r == 0) {
                        return b;
                    }
                    return null;
                });

        assertEquals(2, mesh.faces().size());
        long shared = mesh.edges().stream()
                .filter(edge -> edge.faceA != null && edge.faceB != null)
                .count();
        assertEquals(1, shared);
        assertTrue(mesh.vertices().size() >= 6);
        for (HexGridMesh.Vertex vertex : mesh.vertices()) {
            assertTrue(vertex.edges().size() <= 3);
        }
    }

    @Test
    void ownsSharedEdgeIsExclusiveBetweenNeighbors() {
        assertTrue(HexMapGeometry.ownsSharedEdge(0, 0, 1, 0));
        assertFalse(HexMapGeometry.ownsSharedEdge(1, 0, 0, 0));
        assertTrue(HexMapGeometry.ownsSharedEdge(0, 0, null, null));
    }

    @Test
    void facePolygonUsesSharedCornerVertices() {
        Hex a = hex(0, 0);
        HexGridMesh mesh = HexGridMesh.build(List.of(a), 10, 20, 16, (q, r) -> q == 0 && r == 0 ? a : null);
        HexGridMesh.Face face = mesh.faceAt(0, 0);
        assertNotNull(face);
        assertEquals(6, face.toPolygon().npoints);
    }

    @Test
    void dynastyFrontierIncludesEmptyNeighbor() {
        Hex owned = hex(0, 0);
        attachDynasty(owned, 3, Color.MAGENTA);
        HexGridMesh mesh = HexGridMesh.build(List.of(owned), 0, 0, 10, (q, r) -> q == 0 && r == 0 ? owned : null);
        assertTrue(mesh.edges().stream().anyMatch(HexGridMesh.Edge::isDynastyFrontier));
        assertTrue(mesh.edges().stream()
                .filter(HexGridMesh.Edge::isDynastyFrontier)
                .allMatch(edge -> Color.MAGENTA.equals(edge.dynastyBorderColor(0, 0, Color.BLACK))));
    }

    @Test
    void dynastyFrontierIgnoresSameDynasty() {
        Hex left = hex(0, 0);
        Hex right = hex(1, 0);
        attachDynasty(left, 7, Color.RED);
        attachDynasty(right, 7, Color.RED);
        HexGridMesh.Edge edge = sharedEdge(left, right, 0, 0);
        assertFalse(edge.isDynastyFrontier());
    }

    @Test
    void dynastyBorderPrefersHexCloserToMapCenter() {
        Hex near = hex(0, 0);
        Hex far = hex(1, 0);
        attachDynasty(near, 1, Color.RED);
        attachDynasty(far, 2, Color.BLUE);
        HexGridMesh.Edge edge = sharedEdge(near, far, 0, 0);
        assertTrue(edge.isRivalDynastyFrontier());
        assertEquals(Color.RED, edge.dynastyBorderColor(0, 0, Color.BLACK));
    }

    @Test
    void dynastyBorderPrefersOtherHexWhenItIsCloser() {
        Hex left = hex(0, 0);
        Hex right = hex(1, 0);
        attachDynasty(left, 1, Color.RED);
        attachDynasty(right, 2, Color.BLUE);
        HexGridMesh.Edge edge = sharedEdge(left, right, 0, 0);
        double rightCenterX = HexMapGeometry.flatTopCenterXAbs(1, 0, 0, 10);
        assertEquals(Color.BLUE, edge.dynastyBorderColor(rightCenterX, 0, Color.BLACK));
    }

    @Test
    void dynastyCoastTakesPriorityOverOcean() {
        Hex land = hex(0, 0);
        land.setBiome(GameConstants.BIOME_PLAINS);
        attachDynasty(land, 3, Color.MAGENTA);
        Hex ocean = hex(1, 0);
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        HexGridMesh.Edge edge = sharedEdge(land, ocean, 0, 0);
        assertTrue(edge.isDynastyFrontier());
        assertEquals(Color.MAGENTA, edge.dynastyBorderColor(0, 0, Color.BLACK));
    }

    @Test
    void emptyLandOceanCoastUsesOceanMapColor() {
        Hex land = hex(0, 0);
        land.setBiome(GameConstants.BIOME_PLAINS);
        Hex ocean = hex(1, 0);
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        HexGridMesh.Edge edge = sharedEdge(land, ocean, 0, 0);
        assertFalse(edge.isDynastyFrontier());
        assertEquals(GameConstants.BIOME_OCEAN.getMapColor(),
                HexGridMesh.oceanBorderColor(edge, face -> face.hex.getBiome().getMapColor()));
    }

    private static Hex hex(int q, int r) {
        Hex hex = new Hex();
        hex.setQ(q);
        hex.setR(r);
        return hex;
    }

    private static void attachDynasty(Hex hex, int id, Color color) {
        AntSpecies species = GameConstants.SPECIES_OMNI != null
                ? GameConstants.SPECIES_OMNI
                : GameConstants.getSpeciesById(1);
        Dynasty dynasty = new Dynasty(id, "T" + id, false, species);
        dynasty.setColor(color);
        Colony colony = new Colony(id, "C" + id, false);
        colony.setDynasty(dynasty);
        hex.setColony(colony);
    }

    private static HexGridMesh.Edge sharedEdge(Hex left, Hex right, double originX, double originY) {
        HexGridMesh mesh = HexGridMesh.build(List.of(left, right), originX, originY, 10, (q, r) -> {
            if (q == left.getQ() && r == left.getR()) {
                return left;
            }
            if (q == right.getQ() && r == right.getR()) {
                return right;
            }
            return null;
        });
        return mesh.edges().stream()
                .filter(edge -> edge.faceA != null && edge.faceB != null)
                .findFirst()
                .orElseThrow();
    }
}
