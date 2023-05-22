package com.scouter.cobblemonoutbreaks;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.scouter.cobblemonoutbreaks.command.OutbreakPortalCommand;
import com.scouter.cobblemonoutbreaks.config.CobblemonOutbreaksConfig;
import com.scouter.cobblemonoutbreaks.data.OutbreakPlayerManager;
import com.scouter.cobblemonoutbreaks.data.OutbreaksJsonDataManager;
import com.scouter.cobblemonoutbreaks.data.PokemonOutbreakManager;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortalEntity;
import com.scouter.cobblemonoutbreaks.setup.ClientSetup;
import com.scouter.cobblemonoutbreaks.setup.Registration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.UUID;

public class CobblemonOutbreaks implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MODID = "cobblemonoutbreaks";
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblemonoutbreaks");


    @Override
    public void onInitialize() {

        CobblemonOutbreaksConfig.registerConfigs();

        Registration.init();
        ClientSetup.init();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new OutbreaksJsonDataManager());
        CobblemonOutbreaks.pokemonCapture();
        CobblemonOutbreaks.pokemonKO();
        CobblemonOutbreaks.tickPlayer();
        CommandRegistrationCallback.EVENT.register((commandDispatcher,commandBuildContext,commandSelection) -> OutbreakPortalCommand.register(commandDispatcher));
    }


    public static ResourceLocation prefix(String name) {
        return new ResourceLocation(MODID, name.toLowerCase(Locale.ROOT));
    }

    /**
     * Subscribes to the POKEMON_CAPTURED event and performs actions when a Pokémon is captured.
     * Checks if the captured Pokémon UUID is present in the outbreak manager's map.
     * If present, retrieves the owner UUID and removes the Pokémon from the set in the outbreak portal entity.
     * Finally, removes the Pokémon UUID from the outbreak manager.
     */
    public static void pokemonCapture() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.HIGH, event -> {
            if (!(event.getPlayer().level instanceof ServerLevel serverLevel)) return null;
            PokemonOutbreakManager outbreakManager = PokemonOutbreakManager.get(serverLevel);
            UUID pokemonUUID = event.getPokemon().getUuid();
            if (!outbreakManager.containsUUID(pokemonUUID)) return null;
            UUID ownerUUID = outbreakManager.getOwnerUUID(pokemonUUID);
            OutbreakPortalEntity outbreakPortal = (OutbreakPortalEntity) serverLevel.getEntity(ownerUUID);
            if(outbreakPortal != null) {
                outbreakPortal.removeFromSet(pokemonUUID);
            }
            outbreakManager.removePokemonUUID(pokemonUUID);
            //LOGGER.info("This one was from a portal and captured!");
            return null;
        });
    }

    /**
     * Subscribes to the POKEMON_FAINTED event and performs actions when a Pokémon faints.
     * Checks if the fainted Pokémon UUID is present in the outbreak manager's map.
     * If present, retrieves the owner UUID and removes the Pokémon from the set in the outbreak portal entity.
     * Finally, removes the Pokémon UUID from the outbreak manager.
     */
    public static void pokemonKO() {
        CobblemonEvents.POKEMON_FAINTED.subscribe(Priority.HIGH, event -> {
            ServerLevel serverLevel = event.getPokemon().getEntity().getServer().getLevel(Level.OVERWORLD);
            PokemonOutbreakManager outbreakManager = PokemonOutbreakManager.get(serverLevel);
            UUID pokemonUUID = event.getPokemon().getUuid();
            if (!outbreakManager.containsUUID(pokemonUUID)) return null;
            UUID ownerUUID = outbreakManager.getOwnerUUID(pokemonUUID);
            OutbreakPortalEntity outbreakPortal = (OutbreakPortalEntity) serverLevel.getEntity(ownerUUID);
            if(outbreakPortal != null) {
                outbreakPortal.removeFromSet(pokemonUUID);
            }
            outbreakManager.removePokemonUUID(pokemonUUID);
            //LOGGER.info("This one fainted!" + event.getPokemon().getSpecies());
            return null;
        });
    }



    /**
     * Subscribes to the PlayerTickEvent and creates Pokémon outbreaks based on a timer.
     * Checks if the player is a server player, if the event is on the server side, and if it's in the END phase.
     * If any of these conditions are not met, the method returns.
     * Manages the outbreak timer for each player and spawns outbreak portals when the timer reaches zero.
     */

    public static void tickPlayer() {

        /**
         * The outbreak timer, initially set to the value defined in the config (OUTBREAK_SPAWN_TIMER).
         * Represents the time until the next outbreak of Pokémon portals.
         */
        int outbreakTimer = CobblemonOutbreaksConfig.OUTBREAK_SPAWN_TIMER;

        /**
         * The number of outbreak portals to spawn, defined in the config (OUTBREAK_SPAWN_COUNT).
         */
        int outbreakCount =   CobblemonOutbreaksConfig.OUTBREAK_SPAWN_COUNT;

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            OutbreakPlayerManager outbreakPlayerManager = OutbreakPlayerManager.get((ServerLevel) server.getLevel(Level.OVERWORLD));
            server.getPlayerList().getPlayers().forEach(serverPlayer -> {
                        if (!outbreakPlayerManager.containsUUID(serverPlayer.getUUID()))
                            outbreakPlayerManager.setTimeLeft(serverPlayer.getUUID(), outbreakTimer);
                        int timeLeft = outbreakPlayerManager.getTimeLeft(serverPlayer.getUUID());
                        if (timeLeft-- > 0) {
                            outbreakPlayerManager.setTimeLeft(serverPlayer.getUUID(), timeLeft--);
                            return;
                        }
                        for (int i = 0; i < outbreakCount; i++) {
                            OutbreakPortalEntity outbreakPortal = new OutbreakPortalEntity(serverPlayer.getLevel(), serverPlayer);
                            serverPlayer.level.addFreshEntity(outbreakPortal);
                        }
                        outbreakPlayerManager.setTimeLeft(serverPlayer.getUUID(), outbreakTimer);
                    }
            );
        });
    }
}
