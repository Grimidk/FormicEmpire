package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.shared.HexWaterCrossing;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.ArrayList;
import java.util.List;

public class WorldBiomeTransformationService {

    public static List<Hex> getNeighbors(Hex h) {
        if (h == null) {
            return List.of();
        }
        List<Hex> list = new ArrayList<>(6);
        if (h.getNorth() != null) list.add(h.getNorth());
        if (h.getNorthEast() != null) list.add(h.getNorthEast());
        if (h.getSouthEast() != null) list.add(h.getSouthEast());
        if (h.getSouth() != null) list.add(h.getSouth());
        if (h.getSouthWest() != null) list.add(h.getSouthWest());
        if (h.getNorthWest() != null) list.add(h.getNorthWest());
        return list;
    }

    public boolean transformBiome(World world, Hex hex, Biome newBiome, String reason) {
        if (world == null || hex == null || newBiome == null) {
            return false;
        }
        Biome oldBiome = hex.getBiome();
        if (oldBiome == newBiome) {
            return false;
        }

        hex.setBiome(newBiome);
        updateCoastalTopology(world, hex, oldBiome, newBiome);

        Colony colony = hex.getColony();
        if (colony != null) {
            if (newBiome == GameConstants.BIOME_OCEAN
                    || newBiome == GameConstants.BIOME_DEEP_OCEAN
                    || newBiome == GameConstants.BIOME_SHALLOW_OCEAN) {
                colony.getLocationService().clearAllSources(colony);
            } else if (newBiome == GameConstants.BIOME_BURNT_FOREST || newBiome == GameConstants.BIOME_DESERT || newBiome == GameConstants.BIOME_BARREN) {
                List<ResourceSource> sources = new ArrayList<>(colony.getLocationService().getDiscoveredSources());
                for (ResourceSource s : sources) {
                    if (s != null && s.getResourceType() != null && s.getResourceType() != GameConstants.RESOURCE_ROCK) {
                        colony.getLocationService().removeSource(colony, s);
                    }
                }
            }
            hex.setNonWaterResourceSourcesGenerated(0);

            String reasonText = reason != null && !reason.isEmpty() ? reason : "";
            String msg = String.format(LanguageStrings.get(LanguageStrings.LOG_BIOME_TRANSFORM_FMT),
                    oldBiome != null ? oldBiome.getName() : "",
                    newBiome.getName(),
                    reasonText);
            colony.logEvent(ColonyLogPrefixes.WARNING + " " + msg.trim());
        }

        String locationArg = colony != null
                ? WorldHistoryEvent.colonyArg(colony.getId())
                : WorldHistoryEvent.plainArg("(" + hex.getQ() + ", " + hex.getR() + ")");
        world.getHistoryService().record(WorldHistoryEventType.DISASTER,
                LanguageStrings.HISTORY_DISASTER_FMT,
                colony != null && colony.getDynasty() != null ? colony.getDynasty().getId() : -1,
                colony != null ? colony.getId() : -1,
                -1,
                locationArg,
                WorldHistoryEvent.plainArg(oldBiome != null ? oldBiome.getName() : "Unknown"),
                WorldHistoryEvent.plainArg(newBiome.getName() + " (" + (reason != null ? reason : "") + ")"));

        return true;
    }

    public boolean applyWildfireTransformation(World world, Hex hex) {
        if (hex == null || hex.getBiome() == null) {
            return false;
        }
        Biome b = hex.getBiome();
        if (b != GameConstants.BIOME_FOREST && b != GameConstants.BIOME_JUNGLE && b != GameConstants.BIOME_TAIGA) {
            return false;
        }
        return transformBiome(world, hex, GameConstants.BIOME_BURNT_FOREST,
                LanguageStrings.get(LanguageStrings.DISASTER_WILDFIRE));
    }

    public boolean applyMeteorTransformation(World world, Hex hex) {
        if (hex == null || hex.getBiome() == null) {
            return false;
        }
        if (HexWaterCrossing.isWaterBiome(hex.getBiome())) {
            return false;
        }
        return transformBiome(world, hex, GameConstants.BIOME_BARREN,
                LanguageStrings.get(LanguageStrings.DISASTER_METEOR));
    }

    public boolean applyIceCollapseTransformation(World world, Hex hex) {
        if (hex == null || hex.getBiome() != GameConstants.BIOME_ICE_FIELD) {
            return false;
        }
        return transformBiome(world, hex, GameConstants.BIOME_OCEAN,
                LanguageStrings.get(LanguageStrings.DISASTER_ICE_COLLAPSE));
    }

    public boolean applyOceanFreezeTransformation(World world, Hex hex) {
        if (hex == null || !HexWaterCrossing.isWaterBiome(hex.getBiome())) {
            return false;
        }
        return transformBiome(world, hex, GameConstants.BIOME_ICE_FIELD,
                LanguageStrings.get(LanguageStrings.DISASTER_OCEAN_FREEZE));
    }

    public static boolean isPlantRichBiome(Biome biome) {
        if (biome == null) {
            return false;
        }
        return biome == GameConstants.BIOME_JUNGLE
                || biome == GameConstants.BIOME_FOREST
                || biome == GameConstants.BIOME_MANGROVE
                || biome == GameConstants.BIOME_WETLANDS
                || biome == GameConstants.BIOME_SWAMP
                || biome == GameConstants.BIOME_PRAIRIE
                || biome == GameConstants.BIOME_PLAINS;
    }

    public static Biome getOverharvestDegradationBiome(Biome current) {
        if (current == GameConstants.BIOME_JUNGLE
                || current == GameConstants.BIOME_FOREST
                || current == GameConstants.BIOME_MANGROVE) {
            return GameConstants.BIOME_PLAINS;
        }
        if (current == GameConstants.BIOME_WETLANDS
                || current == GameConstants.BIOME_SWAMP
                || current == GameConstants.BIOME_PRAIRIE
                || current == GameConstants.BIOME_PLAINS) {
            return GameConstants.BIOME_BARREN;
        }
        return null;
    }

    public boolean checkOverharvestDepletion(World world, Colony colony, Hex hex) {
        if (hex == null || hex.getBiome() == null) {
            return false;
        }
        if (colony != null && colony.hasUpgrade(GameUnlocks.STAT_HEX_SUSTAIN)) {
            return false;
        }
        if (hex.getResourceDepletionPercent() < 100) {
            return false;
        }
        Biome targetBiome = getOverharvestDegradationBiome(hex.getBiome());
        if (targetBiome == null) {
            return false;
        }
        return transformBiome(world, hex, targetBiome,
                LanguageStrings.get(LanguageStrings.REASON_OVERHARVESTING));
    }

    public boolean applyEcologicalRecovery(World world, Hex hex) {
        if (hex == null || (hex.getBiome() != GameConstants.BIOME_BURNT_FOREST && hex.getBiome() != GameConstants.BIOME_BARREN)) {
            return false;
        }
        if (hex.getDisasterType() != null) {
            return false;
        }
        return transformBiome(world, hex, GameConstants.BIOME_PLAINS,
                LanguageStrings.get(LanguageStrings.REASON_RECOVERY));
    }

    public void processDaily(World world) {
        if (world == null || world.getHexes() == null) {
            return;
        }
        for (Hex hex : world.getColonizedHexes()) {
            if (hex != null && hex.getColony() != null) {
                checkOverharvestDepletion(world, hex.getColony(), hex);
            }
        }
        List<Hex> hexes = world.getHexes();
        if (!hexes.isEmpty()) {
            Hex sample = hexes.get(GameRandom.nextInt(hexes.size()));
            if (sample != null && (sample.getBiome() == GameConstants.BIOME_BURNT_FOREST || sample.getBiome() == GameConstants.BIOME_BARREN)
                    && GameRandom.nextInt(120) == 0) {
                applyEcologicalRecovery(world, sample);
            }
        }
    }

    private void updateCoastalTopology(World world, Hex hex, Biome oldBiome, Biome newBiome) {
        boolean wasOcean = oldBiome != null && HexWaterCrossing.isWaterBiome(oldBiome);
        boolean isOcean = newBiome != null && HexWaterCrossing.isWaterBiome(newBiome);
        if (wasOcean == isOcean) {
            return;
        }

        List<Hex> neighbors = getNeighbors(hex);
        if (isOcean) {
            hex.setCoastal(false);
            hex.setElevation(1);
            for (Hex neighbor : neighbors) {
                if (neighbor != null && neighbor.getBiome() != null && !HexWaterCrossing.isWaterBiome(neighbor.getBiome())) {
                    neighbor.setCoastal(true);
                }
            }
        } else {
            hex.setElevation(3);
            boolean touchesOcean = false;
            for (Hex neighbor : neighbors) {
                if (neighbor != null && neighbor.getBiome() != null && HexWaterCrossing.isWaterBiome(neighbor.getBiome())) {
                    touchesOcean = true;
                    break;
                }
            }
            hex.setCoastal(touchesOcean);
            for (Hex neighbor : neighbors) {
                if (neighbor != null && neighbor.getBiome() != null && !HexWaterCrossing.isWaterBiome(neighbor.getBiome())) {
                    boolean nTouchesOcean = false;
                    for (Hex nn : getNeighbors(neighbor)) {
                        if (nn != null && nn.getBiome() != null && HexWaterCrossing.isWaterBiome(nn.getBiome())) {
                            nTouchesOcean = true;
                            break;
                        }
                    }
                    neighbor.setCoastal(nTouchesOcean);
                }
            }
        }
    }
}
