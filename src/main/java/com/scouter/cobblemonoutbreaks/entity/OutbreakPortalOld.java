package com.scouter.cobblemonoutbreaks.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scouter.cobblemonoutbreaks.data.OutbreakAlgorithms;
import com.scouter.cobblemonoutbreaks.data.OutbreakRewards;
import com.scouter.cobblemonoutbreaks.data.OutbreakSpecies;
import com.scouter.cobblemonoutbreaks.data.PokemonRarity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.scouter.cobblemonoutbreaks.CobblemonOutbreaks.prefix;

public class OutbreakPortalOld {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Codec<OutbreakPortalOld> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    Codec.STRING.fieldOf("species").forGetter(t -> t.species),
                    Codec.INT.fieldOf("waves").forGetter(w -> w.waves),
                    Codec.intRange(1, 64).fieldOf("spawns_per_wave").forGetter(s -> s.spawnsPerWave),
                    BuiltInRegistries.ITEM.byNameCodec().listOf().optionalFieldOf("rewards", Collections.emptyList()).forGetter(i -> i.rewards),
                    Codec.doubleRange(1,10000000).optionalFieldOf("shiny_chance",1024D).forGetter(r -> r.shinyChance),
                    Codec.INT.optionalFieldOf("experience_reward", 0).forGetter(e -> e.experience),
                    Codec.doubleRange(5D,40D).optionalFieldOf("spawn_range", 15.0D).forGetter(r -> r.spawnRange),
                    Codec.doubleRange(5D,40D).optionalFieldOf("leash_range", 32D).forGetter(g -> g.leashRange),
                    SpawnAlgorithms.CODEC.optionalFieldOf("spawn_algorithm", SpawnAlgorithms.NAMED_ALGORITHMS.get(prefix("clustered"))).forGetter(g -> g.spawnAlgo),
                    SpawnLevelAlgorithms.CODEC.optionalFieldOf("level_algorithm", SpawnLevelAlgorithms.NAMED_ALGORITHMS.get(prefix("scaled"))).forGetter(g -> g.spawnLevelAlgo),
                    Codec.INT.optionalFieldOf("gate_timer", 36000).forGetter(t -> t.gateTimer),
                    Codec.intRange(1, 99).optionalFieldOf("min_pokemon_level", 100).forGetter(s -> s.minPokemonLevel),
                    Codec.intRange(2, 100).optionalFieldOf("max_pokemon_level", 100).forGetter(s -> s.maxPokemonLevel),
                    ExtraCodecs.TAG_OR_ELEMENT_ID.listOf().optionalFieldOf("biome", Collections.emptyList()).forGetter(t -> t.biomeTags),
                    Codec.STRING.optionalFieldOf("pokemon_rarity", "common").forGetter(r -> r.stringRarity)
            )
            .apply(inst, OutbreakPortalOld::new)
    );

    protected final String species;
    protected final List<Item> rewards;
    protected int experience;
    protected int waves;
    protected int spawnsPerWave;
    protected int gateTimer;
    protected int maxPokemonLevel;
    protected int minPokemonLevel;
    protected double spawnRange;
    protected double leashRange;

    protected double shinyChance;

    protected final SpawnAlgorithms.SpawnAlgorithm spawnAlgo;
    protected final SpawnLevelAlgorithms.SpawnLevelAlgorithm spawnLevelAlgo;
    protected final List<ResourceLocation> spawnBiome;
    protected final List<ResourceLocation> spawnBiomeTags;
    protected final List<ExtraCodecs.TagOrElementLocation> biomeTags;
    protected final PokemonRarity rarity;
    protected final String stringRarity;
    private ResourceLocation jsonLocation;
    private List<ExtraCodecs.TagOrElementLocation> spawnBiomeTag;
    /**
     * Creates an OutbreakPortal instance.
     *
     * @param species        The species of the entities to be spawned in this wave.
     * @param waves          The total number of waves in the gateway.
     * @param spawnsPerWave  The number of entities to be spawned in each wave.
     * @param rewards        The list of rewards that will be granted at the end of this wave.
     * @param shinyChance    The chance of a spawned entity being shiny.
     * @param experience     The experience value associated with the wave.
     * @param spawnRange     The range within which the entities will be spawned.
     * @param leashRange     The maximum range within which the entities can move from their spawn position.
     * @param spawnAlgo      The algorithm used to determine the spawn position for the entities.
     * @param gateTimer      The time limit for completing this wave.
     * @param maxPokemonLevel The maximum level of the spawned entities.
     * @param spawnBiome     The biomes the entity can spawn in.
     */
    public OutbreakPortalOld(String species, int waves, int spawnsPerWave, List<Item> rewards,
                             double shinyChance, int experience, double spawnRange, double leashRange,
                             SpawnAlgorithms.SpawnAlgorithm spawnAlgo, SpawnLevelAlgorithms.SpawnLevelAlgorithm spawnLevelAlgo,
                             int gateTimer, int minPokemonLevel, int maxPokemonLevel, List<ExtraCodecs.TagOrElementLocation> spawnBiome, String rarity) {
        this.species = species;
        this.rewards = rewards;
        this.experience = experience;
        this.waves = waves;
        this.spawnsPerWave = spawnsPerWave;
        this.spawnRange = spawnRange;
        this.leashRange = leashRange;
        this.spawnAlgo = spawnAlgo;
        this.spawnLevelAlgo = spawnLevelAlgo;
        this.shinyChance = shinyChance;
        this.gateTimer = gateTimer;
        this.minPokemonLevel = minPokemonLevel;
        this.maxPokemonLevel = maxPokemonLevel;
        List<ResourceLocation> spawnBiomeTags = new ArrayList<>();
        List<ResourceLocation> spawnBiomes = new ArrayList<>();
        for(ExtraCodecs.TagOrElementLocation tagOrElementLocation : spawnBiome){
            if(tagOrElementLocation.tag()){
                spawnBiomeTags.add(tagOrElementLocation.id());
            } else {
                spawnBiomes.add(tagOrElementLocation.id());
            }
        }

        this.spawnBiome = spawnBiomes;
        this.spawnBiomeTags = spawnBiomeTags;
        this.biomeTags = spawnBiome;
        this.shinyChance = shinyChance;
        this.stringRarity = rarity;
        this.rarity = PokemonRarity.fromName(rarity);
    }


    public void setJsonLocation(ResourceLocation location){
        this.jsonLocation = location;
    }


    public ResourceLocation getJsonLocation() {
        try {
            return jsonLocation;
        } catch (Exception e){
            LOGGER.error("Could not find jsonLocation due to {}", e);
        }
        return new ResourceLocation("");
    }

    public List<Item> getRewards() {
        return this.rewards;
    }

    public String getSpecies() {
        return this.species;
    }

    public int getWaves() {
        return this.waves;
    }

    public int getSpawnCount() {
        return this.spawnsPerWave;
    }

    public double getSpawnRange() {
        return this.spawnRange;
    }

    public double getLeashRangeSq() {
        return this.leashRange * this.leashRange;
    }

    public SpawnAlgorithms.SpawnAlgorithm getSpawnAlgo() {
        return this.spawnAlgo;
    }

    public SpawnLevelAlgorithms.SpawnLevelAlgorithm getSpawnLevelAlgo() {
        return this.spawnLevelAlgo;
    }

    public double getExperience() {
        return this.experience;
    }


    public double getShinyChance() {
        return this.shinyChance;
    }

    public double getMaxGateTime() {
        return this.gateTimer;
    }
    public int getMaxPokemonLevel() {
        return this.maxPokemonLevel;
    }

    public int getMinPokemonLevel() {
        return this.minPokemonLevel;
    }

    public List<ResourceLocation> getSpawnBiomeTags() {
        return this.spawnBiomeTags;
    }

    public List<ResourceLocation> getSpawnBiome() {
        return this.spawnBiome;
    }

    public OutbreakPortal getOutBreakPortalNew() {
        OutbreakSpecies outbreakSpecies = new OutbreakSpecies(species, waves, spawnsPerWave, shinyChance, stringRarity);
        OutbreakRewards outbreakRewards = new OutbreakRewards(rewards, experience);
        OutbreakAlgorithms outbreakAlgorithms = new OutbreakAlgorithms(spawnAlgo, spawnLevelAlgo, maxPokemonLevel, minPokemonLevel, spawnRange, leashRange);
        OutbreakPortal outbreakPortal =  new OutbreakPortal(outbreakSpecies, outbreakRewards,outbreakAlgorithms, gateTimer, -63, 255, biomeTags);
        outbreakPortal.setOld(true);
        return outbreakPortal;
    }
}
