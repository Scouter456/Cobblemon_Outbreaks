package com.scouter.cobblemonoutbreaks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scouter.cobblemonoutbreaks.entity.SpawnAlgorithms;
import com.scouter.cobblemonoutbreaks.entity.SpawnLevelAlgorithms;

import static com.scouter.cobblemonoutbreaks.CobblemonOutbreaks.prefix;

public class OutbreakAlgorithms {

    public static Codec<OutbreakAlgorithms> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    SpawnAlgorithms.CODEC.optionalFieldOf("spawn_algorithm", SpawnAlgorithms.NAMED_ALGORITHMS.get(prefix("clustered"))).forGetter(g -> g.spawnAlgorithms),
                    SpawnLevelAlgorithms.CODEC.optionalFieldOf("level_algorithm", SpawnLevelAlgorithms.NAMED_ALGORITHMS.get(prefix("scaled"))).forGetter(g -> g.spawnLevelAlgorithms),
                    Codec.intRange(1, 99).optionalFieldOf("min_pokemon_level", 100).forGetter(s -> s.minPokemonLevel),
                    Codec.intRange(2, 100).optionalFieldOf("max_pokemon_level", 100).forGetter(s -> s.maxPokemonLevel),
                    Codec.doubleRange(5D,40D).optionalFieldOf("spawn_range", 15D).forGetter(r -> r.spawnRange),
                    Codec.doubleRange(5D,40D).optionalFieldOf("leash_range", 32D).forGetter(g -> g.leashRange)
            )
            .apply(inst, OutbreakAlgorithms::new)
    );


    private SpawnLevelAlgorithms.SpawnLevelAlgorithm spawnLevelAlgorithms;
    private SpawnAlgorithms.SpawnAlgorithm spawnAlgorithms;
    protected int maxPokemonLevel;
    protected int minPokemonLevel;
    protected double spawnRange;
    protected double leashRange;


    public OutbreakAlgorithms(SpawnAlgorithms.SpawnAlgorithm spawnAlgorithms, SpawnLevelAlgorithms.SpawnLevelAlgorithm spawnLevelAlgorithms, int maxPokemonLevel, int minPokemonLevel, double spawnRange, double leashRange){
        this.spawnLevelAlgorithms = spawnLevelAlgorithms;
        this.spawnAlgorithms = spawnAlgorithms;
        this.minPokemonLevel = minPokemonLevel;
        this.maxPokemonLevel = maxPokemonLevel;
        this.spawnRange = spawnRange;
        this.leashRange = leashRange;
    }

    public SpawnAlgorithms.SpawnAlgorithm getSpawnAlgo() {
        return spawnAlgorithms;
    }

    public SpawnLevelAlgorithms.SpawnLevelAlgorithm getSpawnLevelAlgo() {
        return spawnLevelAlgorithms;
    }

    public int getMaxPokemonLevel() {
        return this.maxPokemonLevel;
    }

    public int getMinPokemonLevel() {
        return this.minPokemonLevel;
    }

    public double getSpawnRange() {
        return this.spawnRange;
    }

    public double getLeashRangeSq() {
        return this.leashRange * this.leashRange;
    }

    public static OutbreakAlgorithms getDefaultAlgoritms(){
        return new OutbreakAlgorithms(SpawnAlgorithms.NAMED_ALGORITHMS.get(prefix("clustered")), SpawnLevelAlgorithms.NAMED_ALGORITHMS.get(prefix("scaled")),100,100, 15,32);
    }

}
