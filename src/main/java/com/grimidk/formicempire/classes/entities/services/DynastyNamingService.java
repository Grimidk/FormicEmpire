package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.misc.Species;

import java.util.*;

public class DynastyNamingService {
    private static final List<String> ALL_THEMES = Arrays.asList(
        "Leaf", "Sand", "Rock", "Iron", "Mushroom", "Meat", "Water", "Fire", "Wind", "Dirt", 
        "Wood", "Gold", "Bug", "Stone", "Clay", "Steel", "Ice", "Flame", "Storm", "Dust",
        "Vine", "Root", "Seed", "Pollen", "Honey", "Silk", "Web", "Shadow", "Night", "Day",
        "Silver", "Copper", "Bronze", "Emerald", "Ruby", "Sapphire", "Quartz", "Granite",
        "Swamp", "Marsh", "Ocean", "River", "Peak", "Valley", "Cave", "Forest", "Jungle"
    );

    private static final Map<Integer, List<String>> SPECIES_PREFERENCES = new HashMap<>();

    static {
        // Omni
        SPECIES_PREFERENCES.put(1, Arrays.asList("Iron", "Gold", "Stone", "Bug", "Silver", "Copper", "Bronze", "Steel"));
        // Leaf-Cutter
        SPECIES_PREFERENCES.put(2, Arrays.asList("Leaf", "Wood", "Vine", "Root", "Seed", "Forest", "Jungle", "Mushroom"));
        // Pharaoh
        SPECIES_PREFERENCES.put(3, Arrays.asList("Sand", "Dust", "Emerald", "Ruby", "Sapphire", "Gold", "Silver", "Silk"));
        // Marauder
        SPECIES_PREFERENCES.put(4, Arrays.asList("Meat", "Fire", "Flame", "Storm", "Shadow", "Night", "Marsh", "Swamp"));
    }

    private final Set<String> usedThemes = new HashSet<>();
    private final Random random = new Random();

    public String generateDynastyName(Species species) {
        String theme = getRandomTheme(species);
        usedThemes.add(theme);
        return theme + " Dynasty";
    }

    public String generateCapitalName(String dynastyName) {
        if (dynastyName == null || !dynastyName.endsWith(" Dynasty")) {
            return "Colony Prime";
        }
        String theme = dynastyName.replace(" Dynasty", "");
        return theme + " Prime";
    }

    private String getRandomTheme(Species species) {
        List<String> pool = new ArrayList<>();
        
        // Try to use species preferences first
        if (species != null && SPECIES_PREFERENCES.containsKey(species.getId())) {
            for (String pref : SPECIES_PREFERENCES.get(species.getId())) {
                if (!usedThemes.contains(pref)) {
                    pool.add(pref);
                }
            }
        }

        if (pool.isEmpty()) {
            for (String theme : ALL_THEMES) {
                if (!usedThemes.contains(theme)) {
                    pool.add(theme);
                }
            }
        }

        if (pool.isEmpty()) {
            return ALL_THEMES.get(random.nextInt(ALL_THEMES.size()));
        }

        return pool.get(random.nextInt(pool.size()));
    }
    
    public void registerUsedName(String dynastyName) {
        if (dynastyName != null && dynastyName.endsWith(" Dynasty")) {
            usedThemes.add(dynastyName.replace(" Dynasty", ""));
        }
    }
}
