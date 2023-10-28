package com.scouter.cobblemonoutbreaks.event;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortalEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CobblemonOutbreaksEvent {
    public interface Events {

        /**
         * Pokemon Spawn
         *
         * Fired when a pokemon is spawned during an outbreak.
         * This event is cancelable.
         */
        Event<PokemonSpawn> POKEMON_SPAWN = EventFactory.createArrayBacked(PokemonSpawn.class, callbacks -> (level, entity, spawnPos, spawnOrNot) -> {
            for (PokemonSpawn callback : callbacks) {
                boolean spawn = callback.onPokemonSpawn(level, entity, spawnPos, spawnOrNot);
                return spawn;
            }
            return true;
        });

        /**
         * PortalSpawn Event
         *
         * Fired when a portal is spawned during an outbreak.
         * This event is not cancelable.
         */
        Event<PortalSpawn> PORTAL_SPAWN = EventFactory.createArrayBacked(PortalSpawn.class, callbacks -> (level, portal) -> {
            for (PortalSpawn callback : callbacks) {
                callback.onPortalSpawn(level, portal);
            }
        });

        /**
         * PortalClose Event
         *
         * Fired when a portal is closed during an outbreak.
         * This event is not cancelable.
         */
        Event<PortalClose> PORTAL_CLOSE = EventFactory.createArrayBacked(PortalClose.class, callbacks -> (level, portal) -> {
            for (PortalClose callback : callbacks) {
                callback.onPortalClose(level, portal);
            }
        });

        /**
         * WaveEnd Event
         *
         * Fired when a wave is ending but before increasing the wave.
         * This event is not cancelable.
         */
        Event<WaveEnd> WAVE_END = EventFactory.createArrayBacked(WaveEnd.class, callbacks -> (level, portal) -> {
            for (WaveEnd callback : callbacks) {
                callback.onWaveEnd(level, portal);
            }
        });

        /**
         * SpawnRewards Event
         *
         * Fired when rewards are about to be spawned during an outbreak.
         * This event is cancelable.
         */
        Event<SpawnRewards> SPAWN_REWARDS = EventFactory.createArrayBacked(SpawnRewards.class, callbacks -> (level, items, allowRewards) -> {
            for (SpawnRewards callback : callbacks) {
                boolean spawnRewards = callback.onSpawnRewards(level, items, allowRewards);
                return spawnRewards;
            }
            return true;
        });

        Event<OutbreakPokemonCapture> OUTBREAK_POKEMON_CAPTURE = EventFactory.createArrayBacked(OutbreakPokemonCapture.class, callbacks -> (level, player,outbreakPortal, pokemon) -> {
            for (OutbreakPokemonCapture callback : callbacks) {
                callback.onOutbreakPokemonCapture(level, player,outbreakPortal, pokemon);
            }
        });

        Event<OutbreakPokemonKilled> OUTBREAK_POKEMON_KILLED = EventFactory.createArrayBacked(OutbreakPokemonKilled.class, callbacks -> (level, outbreakPortal, pokemon) -> {
            for (OutbreakPokemonKilled callback : callbacks) {
                callback.onOutbreakPokemonKilled(level, outbreakPortal, pokemon);
            }
        });
    }

    /**
     * PokemonSpawn is fired whenever a Pokemon is about to be spawned from an outbreak.
     * This event is cancellable, allowing you to prevent the spawning of the Pokemon.
     */
    public interface PokemonSpawn {
        /**
         * Called when a Pokemon is about to spawn during an outbreak.
         *
         * @param level      The server level in which the Pokemon is about to spawn.
         * @param entity     The PokemonEntity that is about to spawn.
         * @param spawnPos   The position where the Pokemon is going to spawn.
         * @param allowSpawn Set to true if the event should allow the spawn, or false to cancel the spawn.
         */
        boolean onPokemonSpawn(ServerLevel level, PokemonEntity entity, Vec3 spawnPos, boolean allowSpawn);
    }



    public interface PortalSpawn {
        /**
         * Called when a portal is spawned during an outbreak.
         *
         * @param level  The server level in which the portal is spawned.
         * @param portal The OutbreakPortalEntity that is being spawned.
         */
        void onPortalSpawn(ServerLevel level, OutbreakPortalEntity portal);
    }

    public interface PortalClose {
        /**
         * Called when a portal is closed during an outbreak.
         *
         * @param level  The server level in which the portal is closed.
         * @param portal The OutbreakPortalEntity that is being closed.
         */
        void onPortalClose(ServerLevel level, OutbreakPortalEntity portal);
    }

    public interface WaveEnd {
        /**
         * Called when a wave is ending but before increasing the wave.
         *
         * @param level  The server level in which the wave is ending.
         * @param portal The OutbreakPortalEntity associated with the wave.
         */
        void onWaveEnd(ServerLevel level, OutbreakPortalEntity portal);
    }

    public interface SpawnRewards {
        /**
         * Called when rewards are about to be spawned during an outbreak.
         *
         * @param level The server level in which rewards are about to be spawned.
         * @param items A list of ItemStacks representing the rewards.
         * @param allowRewards A boolean allowing the rewards of not.
         */
        boolean onSpawnRewards(ServerLevel level, List<ItemStack> items, boolean allowRewards);
    }

    /**
     * This interface defines a callback for handling the event of capturing an outbreak Pokémon.
     */
    public interface OutbreakPokemonCapture {

        /**
         * Called when an outbreak Pokémon is captured.
         *
         * @param level The server level where the event occurred.
         * @param player The player that captured the pokemon.
         * @param entity The outbreak portal entity associated with the event.
         * @param pokemon The captured Pokémon.
         */
        void onOutbreakPokemonCapture(ServerLevel level, ServerPlayer player, OutbreakPortalEntity entity, Pokemon pokemon);
    }

    /**
     * This interface defines a callback for handling the event of killing an outbreak Pokémon.
     */
    public interface OutbreakPokemonKilled {

        /**
         * Called when an outbreak Pokémon is killed.
         *
         * @param level The server level where the event occurred.
         * @param entity The outbreak portal entity associated with the event.
         * @param pokemon The killed Pokémon.
         */
        void onOutbreakPokemonKilled(ServerLevel level, OutbreakPortalEntity entity, Pokemon pokemon);
    }

}
