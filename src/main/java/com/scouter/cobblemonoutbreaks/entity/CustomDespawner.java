package com.scouter.cobblemonoutbreaks.entity;

import com.cobblemon.mod.common.api.entity.Despawner;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.scouter.cobblemonoutbreaks.data.PokemonOutbreakManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CustomDespawner implements Despawner {
    private int maxAgeTicks = 20 * 60 * 3;

    @Override
    public void beginTracking(@NotNull Entity entity) {
    }

    @Override
    public boolean shouldDespawn(@NotNull Entity entity) {
        Level level = entity.level();
        if(!level.isClientSide && entity instanceof PokemonEntity pokemon){
            PokemonOutbreakManager outbreakManager = PokemonOutbreakManager.get(level);
            UUID uuid = pokemon.getPokemon().getUuid();
            boolean containsPokenon = outbreakManager.containsUUID(uuid) || outbreakManager.containsUUIDTemp(uuid);
            int age = pokemon.getTicksLived();
            boolean isMinAge = age > maxAgeTicks;
            return !containsPokenon && isMinAge;
        }
        return false;
    }
}
