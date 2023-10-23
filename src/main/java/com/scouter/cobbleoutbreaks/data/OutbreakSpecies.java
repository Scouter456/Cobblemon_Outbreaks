package com.scouter.cobbleoutbreaks.data;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scouter.cobbleoutbreaks.entity.OutbreakPortal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.Collections;
import java.util.List;

public class OutbreakSpecies {

    public static Codec<OutbreakSpecies> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    Codec.STRING.fieldOf("species").forGetter(t -> t.species),
                    Codec.INT.fieldOf("waves").forGetter(w -> w.waves),
                    Codec.intRange(1, 64).fieldOf("spawns_per_wave").forGetter(s -> s.spawnsPerWave),
                    Codec.doubleRange(1,10000000).optionalFieldOf("shiny_chance",1024D).forGetter(r -> r.shinyChance),
                    Codec.STRING.optionalFieldOf("pokemon_rarity", "common").forGetter(r -> r.stringRarity)
            )
            .apply(inst, OutbreakSpecies::new)
    );

    protected final String species;
    protected int waves;
    protected int spawnsPerWave;
    protected double shinyChance;

    protected final PokemonRarity rarity;
    protected final String stringRarity;
    public OutbreakSpecies(String species, int waves, int spawnsPerWave,
                           double shinyChance, String rarity){
        this.species = species;
        this.waves = waves;
        this.spawnsPerWave = spawnsPerWave;
        this.shinyChance = shinyChance;
        this.stringRarity = rarity;
        this.rarity = PokemonRarity.fromName(rarity);
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


    public double getShinyChance() {
        return this.shinyChance;
    }

    public PokemonRarity getRarity() {
        return rarity;
    }

    public static OutbreakSpecies getDefaultSpecies(){
        return new OutbreakSpecies("default",1,5,1024D, "common");
    }
}
