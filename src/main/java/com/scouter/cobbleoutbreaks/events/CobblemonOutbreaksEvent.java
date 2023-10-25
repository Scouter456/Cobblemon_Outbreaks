package com.scouter.cobbleoutbreaks.events;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.scouter.cobbleoutbreaks.entity.OutbreakPortal;
import com.scouter.cobbleoutbreaks.entity.OutbreakPortalEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class CobblemonOutbreaksEvent extends Event
{

    /**
     * CobblemonOutbreaksEvent#PokemonSpawn is fired whenever a Pokemon is about to be spawned from an outbreak.
     * This event is cancellable, allowing you to prevent the spawning of the Pokemon.
     */
    public static class PokemonSpawn extends CobblemonOutbreaksEvent {
        private PokemonEntity pokemonEntity;
        private ServerLevel serverLevel;
        private Vec3 spawnPos;

        /**
         * Constructs a PokemonSpawn event with the specified parameters.
         *
         * @param level      The server level in which the Pokemon is about to spawn.
         * @param entity     The PokemonEntity that is about to spawn.
         * @param spawnPos   The position where the Pokemon is going to spawn.
         */
        public PokemonSpawn(ServerLevel level, PokemonEntity entity, Vec3 spawnPos) {
            this.pokemonEntity = entity;
            this.serverLevel = level;
            this.spawnPos = spawnPos;
        }

        /**
         * Get the PokemonEntity that is about to spawn.
         *
         * @return The PokemonEntity to be spawned.
         */
        public PokemonEntity getPokemonEntity() {
            return pokemonEntity;
        }

        /**
         * Get the server level in which the Pokemon is about to spawn.
         *
         * @return The server level.
         */
        public ServerLevel getLevel() {
            return serverLevel;
        }

        /**
         * Get the position where the Pokemon is going to spawn.
         *
         * @return The spawn position as a Vec3.
         */
        public Vec3 getSpawnPos() {
            return spawnPos;
        }
    }



    /**
     * CobblemonOutbreaksEvent#PortalSpawn is fired when a portal is spawned during an outbreak.
     * This event is not cancelable.
     */
    public static class PortalSpawn extends CobblemonOutbreaksEvent {
        private OutbreakPortalEntity portal;
        private ServerLevel serverLevel;

        /**
         * Constructs a PortalSpawn event with the specified parameters.
         *
         * @param level  The server level in which the portal is spawned.
         * @param entity The OutbreakPortalEntity that is being spawned.
         */
        public PortalSpawn(ServerLevel level, OutbreakPortalEntity entity) {
            this.portal = entity;
            this.serverLevel = level;
        }

        /**
         * Get the OutbreakPortalEntity that is being spawned.
         *
         * @return The OutbreakPortalEntity being spawned.
         */
        public OutbreakPortalEntity getPortal() {
            return portal;
        }

        /**
         * Get the server level in which the portal is spawned.
         *
         * @return The server level.
         */
        public ServerLevel getLevel() {
            return serverLevel;
        }
    }

    /**
     * CobblemonOutbreaksEvent#PortalClose is fired when a portal is closed during an outbreak.
     * This event is not cancelable.
     */
    public static class PortalClose extends CobblemonOutbreaksEvent {
        private OutbreakPortalEntity portal;
        private ServerLevel serverLevel;

        /**
         * Constructs a PortalClose event with the specified parameters.
         *
         * @param level  The server level in which the portal is closed.
         * @param entity The OutbreakPortalEntity that is being closed.
         */
        public PortalClose(ServerLevel level, OutbreakPortalEntity entity) {
            this.portal = entity;
            this.serverLevel = level;
        }

        /**
         * Get the OutbreakPortalEntity that is being closed.
         *
         * @return The OutbreakPortalEntity being closed.
         */
        public OutbreakPortalEntity getPortal() {
            return portal;
        }

        /**
         * Get the server level in which the portal is closed.
         *
         * @return The server level.
         */
        public ServerLevel getLevel() {
            return serverLevel;
        }
    }

    /**
     * CobblemonOutbreaksEvent#WaveEnd is fired when a wave is ending but before increasing the wave.
     * This event is not cancelable.
     */
    public static class WaveEnd extends CobblemonOutbreaksEvent {
        private OutbreakPortalEntity portal;
        private ServerLevel serverLevel;

        /**
         * Constructs a WaveEnd event with the specified parameters.
         *
         * @param level  The server level in which the wave is ending.
         * @param entity The OutbreakPortalEntity associated with the wave.
         */
        public WaveEnd(ServerLevel level, OutbreakPortalEntity entity) {
            this.portal = entity;
            this.serverLevel = level;
        }

        /**
         * Get the OutbreakPortalEntity associated with the wave that is ending.
         *
         * @return The OutbreakPortalEntity associated with the wave.
         */
        public OutbreakPortalEntity getPortal() {
            return portal;
        }

        /**
         * Get the server level in which the wave is ending.
         *
         * @return The server level.
         */
        public ServerLevel getLevel() {
            return serverLevel;
        }
    }

    /**
     * CobblemonOutbreaksEvent#SpawnRewards is fired when rewards are about to be spawned during an outbreak.
     * This event is cancelable.
     */
    public static class SpawnRewards extends CobblemonOutbreaksEvent {
        private List<ItemStack> itemStack;
        private ServerLevel serverLevel;

        /**
         * Constructs a SpawnRewards event with the specified parameters.
         *
         * @param level The server level in which rewards are about to be spawned.
         * @param items A list of ItemStacks representing the rewards.
         */
        public SpawnRewards(ServerLevel level, List<ItemStack> items) {
            this.itemStack = items;
            this.serverLevel = level;
        }

        /**
         * Get the list of ItemStacks representing the rewards that are about to be spawned.
         *
         * @return The list of rewards.
         */
        public List<ItemStack> getItems() {
            return itemStack;
        }

        /**
         * Set a new list of items to be spawned as rewards. This can be used to change the items that are about to drop.
         *
         * @param items The new list of ItemStacks to be spawned.
         */
        public void setItems(List<ItemStack> items) {
            this.itemStack = items;
        }

        /**
         * Get the server level in which rewards are about to be spawned.
         *
         * @return The server level.
         */
        public ServerLevel getLevel() {
            return serverLevel;
        }
    }

}
