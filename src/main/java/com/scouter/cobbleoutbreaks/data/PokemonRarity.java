package com.scouter.cobbleoutbreaks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scouter.cobbleoutbreaks.config.CobblemonOutbreaksConfig;
import net.minecraft.util.RandomSource;

public enum PokemonRarity {
    COMMON("common", CobblemonOutbreaksConfig.COMMON_RARITY.get()),
    UNCOMMON("uncommon", CobblemonOutbreaksConfig.UNCOMMON_RARITY.get()),
    RARE("rare", CobblemonOutbreaksConfig.RARE_RARITY.get()),
    EPIC("epic", CobblemonOutbreaksConfig.EPIC_RARITY.get()),
    LEGENDARY("legendary", CobblemonOutbreaksConfig.LEGENDARY_RARITY.get());

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
