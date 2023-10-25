package com.scouter.cobblemonoutbreaks.data;

import com.scouter.cobblemonoutbreaks.config.CobblemonOutbreaksConfig;
import net.minecraft.util.RandomSource;

public enum PokemonRarity {
    COMMON("common", CobblemonOutbreaksConfig.COMMON_RARITY),
    UNCOMMON("uncommon", CobblemonOutbreaksConfig.UNCOMMON_RARITY),
    RARE("rare", CobblemonOutbreaksConfig.RARE_RARITY),
    EPIC("epic", CobblemonOutbreaksConfig.EPIC_RARITY),
    LEGENDARY("legendary", CobblemonOutbreaksConfig.LEGENDARY_RARITY);

    private final String name;
    private final int probability;

    PokemonRarity(String name, int probability) {
        this.name = name;
        this.probability = probability;
    }

    private static int totalProbability;

    static {
        totalProbability = 0;
        for (PokemonRarity rarity : values()) {
            totalProbability += rarity.probability;
        }
    }
    public static PokemonRarity getRandomRarity(RandomSource randomSource) {
        int randomNumber = randomSource.nextInt(totalProbability);
        int cumulativeProbability = 0;

        for (PokemonRarity rarity : values()) {
            cumulativeProbability += rarity.probability;
            if (randomNumber < cumulativeProbability) {
                return rarity;
            }
        }

        return COMMON;
    }

    public static PokemonRarity fromName(String name) {
        for (PokemonRarity rarity : values()) {
            if (rarity.name.equalsIgnoreCase(name)) {
                return rarity;
            }
        }
        return COMMON; // Default to COMMON if not found
    }
}
