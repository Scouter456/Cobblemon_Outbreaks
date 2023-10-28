package com.scouter.cobbleoutbreaks.entity;

import com.cobblemon.mod.common.api.entity.Despawner;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scouter.cobbleoutbreaks.config.CobblemonOutbreaksConfig;
import com.scouter.cobbleoutbreaks.data.OutbreakAlgorithms;
import com.scouter.cobbleoutbreaks.data.OutbreakRewards;
import com.scouter.cobbleoutbreaks.data.OutbreakSpecies;
import com.scouter.cobbleoutbreaks.events.CobblemonOutbreaksEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OutbreakPortal {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static Codec<OutbreakPortal> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    OutbreakSpecies.CODEC.fieldOf("species_data").forGetter(s -> s.speciesData),
                    OutbreakRewards.CODEC.optionalFieldOf("rewards", OutbreakRewards.getDefaultRewards()).forGetter(r -> r.outbreakRewards),
                    OutbreakAlgorithms.CODEC.optionalFieldOf("algorithms", OutbreakAlgorithms.getDefaultAlgoritms()).forGetter(a -> a.outbreakAlgorithms),
                    Codec.INT.optionalFieldOf("gate_timer", 36000).forGetter(t -> t.gateTimer),
                    Codec.intRange(-63, 255).optionalFieldOf("outbreak_min_y", -63).forGetter(r -> r.minOutbreakY),
                    Codec.intRange(-63, 255).optionalFieldOf("outbreak_max_y", 255).forGetter(r -> r.maxOutbreakY),
                    ExtraCodecs.TAG_OR_ELEMENT_ID.listOf().optionalFieldOf("biome", Collections.emptyList()).forGetter(t -> t.biomeTags)
            )
            .apply(inst, OutbreakPortal::new)
    );

    public static Codec<OutbreakPortal> EITHER = Codec.either(OutbreakPortalOld.CODEC, OutbreakPortal.CODEC).xmap(either -> {
                if (either.left().isPresent()) {
                    OutbreakPortalOld outbreakPortalOld = either.left().get();
                    OutbreakPortal outbreakPortal = outbreakPortalOld.getOutBreakPortalNew();
                    return outbreakPortal;
                }



                OutbreakPortal outbreakPortal = either.right().get();
                return outbreakPortal;
            },
            outbreakPortal -> Either.right(outbreakPortal)
    );




    private OutbreakSpecies speciesData;
    private OutbreakRewards outbreakRewards;
    protected int gateTimer;
    private OutbreakAlgorithms outbreakAlgorithms;
    protected final List<ResourceLocation> spawnBiomeTags;
    protected final List<ExtraCodecs.TagOrElementLocation> biomeTags;

    protected final List<ResourceLocation> spawnBiome;
    private ResourceLocation jsonLocation;
    private int minOutbreakY;
    private int maxOutbreakY;
    private boolean isOld;
    /**
     * Creates an OutbreakPortal instance.
     *
     * @param rewards            The list of rewards that will be granted at the end of this wave.
     * @param outbreakAlgorithms The algorithms used.
     * @param gateTimer          The time limit for completing this wave.
     * @param spawnBiome         The biomes the entity can spawn in.
     */
    public OutbreakPortal(OutbreakSpecies speciesData, OutbreakRewards rewards,
                          OutbreakAlgorithms outbreakAlgorithms,
                          int gateTimer, int minOutbreakY, int maxOutbreakY, List<ExtraCodecs.TagOrElementLocation> spawnBiome) {
        this.speciesData = speciesData;
        this.outbreakRewards = rewards;
        this.outbreakAlgorithms = outbreakAlgorithms;
        this.gateTimer = gateTimer;
        this.minOutbreakY = minOutbreakY;
        this.maxOutbreakY = maxOutbreakY;
        List<ResourceLocation> spawnBiomeTags = new ArrayList<>();
        List<ResourceLocation> spawnBiomes = new ArrayList<>();
        for (ExtraCodecs.TagOrElementLocation tagOrElementLocation : spawnBiome) {
            if (tagOrElementLocation.tag()) {
                spawnBiomeTags.add(tagOrElementLocation.id());
            } else {
                spawnBiomes.add(tagOrElementLocation.id());
            }
        }

        this.spawnBiome = spawnBiomes;
        this.spawnBiomeTags = spawnBiomeTags;
        this.biomeTags = spawnBiome;

    }


    public void setJsonLocation(ResourceLocation location) {
        this.jsonLocation = location;
    }


    public ResourceLocation getJsonLocation() {
        try {
            return jsonLocation;
        } catch (Exception e) {
            LOGGER.error("Could not find jsonLocation due to {}", e);
        }
        return new ResourceLocation("");
    }


    public OutbreakAlgorithms getOutbreakAlgorithms() {
        return outbreakAlgorithms;
    }


    public double getMaxGateTime() {
        return this.gateTimer;
    }

    public List<ResourceLocation> getSpawnBiomeTags() {
        return this.spawnBiomeTags;
    }

    public List<ResourceLocation> getSpawnBiome() {
        return this.spawnBiome;
    }

    public OutbreakSpecies getSpeciesData() {
        return speciesData;
    }

    public OutbreakRewards getOutbreakRewards() {
        return outbreakRewards;
    }

    public int getMinOutbreakY() {
        return minOutbreakY;
    }

    public int getMaxOutbreakY() {
        return maxOutbreakY;
    }

    public void setOld(boolean old) {
        isOld = old;
    }

    public boolean isOld() {
        return isOld;
    }
    /**
     * Spawns a wave of Pokémon.
     *
     * @param level The server level where the Pokémon will be spawned.
     * @param pos The origin position from where the Pokémon spawns will be determined.
     * @param outbreakPortalEntity The outbreak portal entity associated with the wave.
     * @param species The species of Pokémon to spawn.
     * @return The list of spawned Pokémon.
     */
    protected Despawner despawner = new CustomDespawner();

    public List<PokemonEntity> spawnWave(ServerLevel level, Vec3 pos, OutbreakPortalEntity outbreakPortalEntity, String species) {
        List<PokemonEntity> spawned = new ArrayList<>();
        int spawnCount = outbreakPortalEntity.getOutbreakPortal().getSpeciesData().getSpawnCount();
        for (int i = 0; i < spawnCount; i++) {
            // If the species is something it can't find, it will put out a random Pokémon
            PokemonProperties pokemonProp = PokemonProperties.Companion.parse("species=" + species, " ", "=");
            Player player = level.getPlayerByUUID(outbreakPortalEntity.getOwnerUUID());
            int spawnLevel = outbreakPortalEntity.getOutbreakPortal().getOutbreakAlgorithms().getSpawnLevelAlgo().getLevel(level, player, outbreakPortalEntity);

            pokemonProp.setLevel(spawnLevel);
            // We also create a Pokémon entity to get the bounding boxes since we couldn't find another correct way to do it.


            if (pokemonProp.getSpecies() == null) {
                if (outbreakPortalEntity.getOutbreakPortal().getSpeciesData().getSpecies().equals("default")) {
                    for (int j = 0; j < 4; j++) {
                        LOGGER.error("Species from {} is null, the species: {} is probably spelled incorrectly", outbreakPortalEntity.getResourceLocation(), species);
                        LOGGER.error("This is a default setting, it means that you do not have any correct json files in your datapack and no outbreaks can spawn!");
                        LOGGER.error("If you think this is an error please report it to the developer");
                    }
                } else {
                    for (int j = 0; j < 4; j++) {
                        LOGGER.error("Species from {} is null, the species: {} is probably spelled incorrectly", outbreakPortalEntity.getResourceLocation(), species);
                    }
                }

                outbreakPortalEntity.completeOutBreak(false);
                return Collections.emptyList();
            }

            double shinyChance = 1 / outbreakPortalEntity.getOutbreakPortal().getSpeciesData().getShinyChance();
            if (level.random.nextDouble() < shinyChance) {
                pokemonProp.setShiny(true);
                level.playSound(null, outbreakPortalEntity.getX(), outbreakPortalEntity.getY(), outbreakPortalEntity.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 1.5F, 1);
            }

            PokemonEntity pokemonEntity = pokemonProp.createEntity(level);

            pokemonEntity.setDespawner(despawner);

            // We log that spawning failed either due to the Pokémon being null (unlikely) or the spawn position being null.
            Vec3 spawnPos = outbreakPortalEntity.getOutbreakPortal().getOutbreakAlgorithms().getSpawnAlgo().spawn(level, pos, outbreakPortalEntity, pokemonEntity);
            if (spawnPos == null) {
                //Decided not to add this since it will spawn the logs otherwise.
                //LOGGER.info("Spawning for Pokémon {} failed, due to spawnPos {} or Pokémon {}", pokemon1.getSpecies(), spawnPos, pokemon1.getSpecies());
                continue;
            }
            pokemonEntity.setPos(spawnPos);
            Pokemon pokemon1 = pokemonEntity.getPokemon();

            if (pokemon1 == null) {
                //Decided not to add this since it will spawn the logs otherwise.
                //LOGGER.info("Spawning for Pokémon {} failed, due to spawnPos {} or Pokémon {}", pokemon1.getSpecies(), spawnPos, pokemon1.getSpecies());
                continue;
            }

            CobblemonOutbreaksEvent.PokemonSpawn spawnEvent = new CobblemonOutbreaksEvent.PokemonSpawn(level, pokemonEntity, spawnPos);
            MinecraftForge.EVENT_BUS.post(spawnEvent);
            if(!spawnEvent.isCanceled()) {
                level.addFreshEntity(pokemonEntity);
                if (CobblemonOutbreaksConfig.OUTBREAK_PORTAL_SPAWN_SOUND.get()) {
                    float volume = (float) CobblemonOutbreaksConfig.OUTBREAK_PORTAL_POKEMON_SPAWN_VOLUME.get().doubleValue();
                    level.playSound(null, spawnPos.x(), spawnPos.y(), spawnPos.z(), SoundEvents.PORTAL_TRAVEL, SoundSource.HOSTILE, volume, 1);
                }

                spawned.add(pokemonEntity);
            }
        }

        return spawned;
    }

    public List<ItemStack> spawnRewards(ServerLevel level, OutbreakPortalEntity gate) {
        List<ItemStack> stacks = new ArrayList<>();
        gate.getOutbreakPortal().getOutbreakRewards().getRewards().forEach(r -> stacks.add(new ItemStack(r)));
        CobblemonOutbreaksEvent.SpawnRewards spawnRewards = new CobblemonOutbreaksEvent.SpawnRewards(level , stacks);
        MinecraftForge.EVENT_BUS.post(spawnRewards);
        if(!spawnRewards.isCanceled()) {
            stacks.forEach(s -> level.addFreshEntity(new ItemEntity(level, gate.getX(), gate.getY(), gate.getZ(), s)));
        }
        return stacks;
    }
}
