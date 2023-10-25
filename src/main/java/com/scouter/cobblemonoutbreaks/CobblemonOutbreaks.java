package com.scouter.cobblemonoutbreaks;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.scouter.cobblemonoutbreaks.command.OutbreakPortalCommand;
import com.scouter.cobblemonoutbreaks.config.CobblemonOutbreaksConfig;
import com.scouter.cobblemonoutbreaks.data.OutbreakManager;
import com.scouter.cobblemonoutbreaks.data.OutbreaksJsonDataManager;
import com.scouter.cobblemonoutbreaks.data.PokemonOutbreakManager;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortalEntity;
import com.scouter.cobblemonoutbreaks.event.CobblemonOutbreaksEvent;
import com.scouter.cobblemonoutbreaks.setup.ClientSetup;
import com.scouter.cobblemonoutbreaks.setup.Registration;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CobblemonOutbreaks implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MODID = "cobblemonoutbreaks";
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblemonoutbreaks");
    public static ServerLevel serverlevel;
    public static boolean serverStarted = false;
    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                serverStarted = true;
                serverlevel = server.getLevel(Level.OVERWORLD);
                OutbreakManager.get(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)));
                PokemonOutbreakManager.get(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)));
            } catch (Exception e) {
                LOGGER.error("Failed getting the server for cobblemonoutbreaks due to", e);
            }
        });

        CobblemonOutbreaksConfig.registerConfigs();
        Registration.init();
        ClientSetup.init();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new OutbreaksJsonDataManager());


        CobblemonOutbreaks.pokemonCapture();
        CobblemonOutbreaks.pokemonKO();
        CobblemonOutbreaks.entityUnload();
        CobblemonOutbreaks.entityLoad();
        CobblemonOutbreaks.tickOutbreaks();
        CobblemonOutbreaks.flushMap();

        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> OutbreakPortalCommand.register(commandDispatcher));
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
            if (!(event.getPlayer().level() instanceof ServerLevel serverLevel)) return Unit.INSTANCE;
            PokemonOutbreakManager outbreakManager = PokemonOutbreakManager.get(serverLevel);
            UUID pokemonUUID = event.getPokemon().getUuid();
            if (!outbreakManager.containsUUID(pokemonUUID)) return Unit.INSTANCE;
            UUID ownerUUID = outbreakManager.getOwnerUUID(pokemonUUID);
            OutbreakManager outbreakManager1 = OutbreakManager.get(serverLevel);
            OutbreakPortalEntity outbreakPortal = outbreakManager1.getOutbreakEntity(ownerUUID);
            if (outbreakPortal != null) {
                outbreakPortal.removeFromSet(pokemonUUID);
            }
            outbreakManager.removePokemonUUID(pokemonUUID);
            //LOGGER.info("This one was from a portal and captured!");

            return Unit.INSTANCE;
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
            if (event.getPokemon().getOwnerUUID() != null || event.getPokemon() == null || serverlevel == null)
                return Unit.INSTANCE;
            ServerLevel serverLevel = serverlevel;
            if (serverLevel == null) {
                //This will work, however it will sometimes throw a null error, corrupting player data
                //Therefore we want the serverlevel from the server started event, however if that is null,
                ///This will be a fallback
                try {
                    serverLevel = event.getPokemon().getEntity().getServer().getLevel(Level.OVERWORLD);
                } catch (Exception e) {
                    LOGGER.error("Failed getting the serverlevel due to {}", e);
                    return Unit.INSTANCE;
                }

            }

            PokemonOutbreakManager outbreakManager = PokemonOutbreakManager.get(serverLevel);
            UUID pokemonUUID = event.getPokemon().getUuid();
            if (!outbreakManager.containsUUID(pokemonUUID)) return null;
            UUID ownerUUID = outbreakManager.getOwnerUUID(pokemonUUID);
            OutbreakManager outbreakManager1 = OutbreakManager.get(serverLevel);
            OutbreakPortalEntity outbreakPortal = outbreakManager1.getOutbreakEntity(ownerUUID);
            if (outbreakPortal != null) {
                outbreakPortal.removeFromSet(pokemonUUID);
            }
            outbreakManager.removePokemonUUID(pokemonUUID);
            //LOGGER.info("This one fainted!" + event.getPokemon().getSpecies());
            return Unit.INSTANCE;
        });
    }


    public static void entityUnload() {
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, server) -> {
            if (server.getLevel().isClientSide || !(entity instanceof PokemonEntity pokemonEntity)) return;
            ServerLevel serverLevel = (ServerLevel) server.getLevel();
            PokemonOutbreakManager outbreakManager = PokemonOutbreakManager.get(serverLevel);
            UUID pokemonUUID = pokemonEntity.getUUID();
            if (!outbreakManager.containsUUID(pokemonUUID)) return;
            UUID ownerUUID = outbreakManager.getOwnerUUID(pokemonUUID);
            outbreakManager.removePokemonUUID(pokemonUUID);
            outbreakManager.addPokemonWOwnerTemp(pokemonUUID, ownerUUID);
            return;
        });
    }

    public static void entityLoad() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, server) -> {
            if (server.getLevel().isClientSide || !(entity instanceof PokemonEntity pokemonEntity)) return;
            ServerLevel serverLevel = (ServerLevel) server.getLevel();
            PokemonOutbreakManager outbreakManager = PokemonOutbreakManager.get(serverLevel);
            UUID pokemonUUID = pokemonEntity.getUUID();
            if (!outbreakManager.containsUUIDTemp(pokemonUUID)) return;
            UUID ownerUUID = outbreakManager.getOwnerUUIDTemp(pokemonUUID);
            outbreakManager.removePokemonUUIDTemp(pokemonUUID);
            outbreakManager.addPokemonWOwner(pokemonUUID, ownerUUID);
            return;
        });
    }

    public static void tickOutbreaks() {
        ServerTickEvents.END_WORLD_TICK.register(server -> {
            if (server.isClientSide || !CobblemonOutbreaks.serverStarted) return;
            ServerLevel serverLevel = (ServerLevel) server;
            OutbreakManager outbreakManager = OutbreakManager.get(serverLevel);
            outbreakManager.setLevel(serverLevel);
            Map<UUID, OutbreakPortalEntity> outbreaks = outbreakManager.getOutbreakPortalEntityMap();
            for(Map.Entry<UUID, OutbreakPortalEntity> entry : outbreaks.entrySet()){
                BlockPos pos = entry.getValue().getBlockPosition();
                ChunkPos chunkPos = new ChunkPos(pos);
                OutbreakPortalEntity outbreakPortal = entry.getValue();
                if(serverLevel.getChunkSource().hasChunk(chunkPos.x, chunkPos.z)) {
                    if (outbreakPortal.getLevel() == null) outbreakPortal.setLevel(serverLevel);
                    if (outbreakPortal.getOutbreakManager() == null) outbreakPortal.setOutbreakManager(PokemonOutbreakManager.get(serverLevel));
                    outbreakPortal.tick();
                }
            }
        });
    }

    private static int flushTimerTempMap = CobblemonOutbreaksConfig.TEMP_OUTBREAKS_MAP_FLUSH_TIMER;
    private static int flushTimerMap = CobblemonOutbreaksConfig.OUTBREAKS_MAP_FLUSH_TIMER;
    public static void flushMap() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if(flushTimerMap <= 0) flushTimerMap = CobblemonOutbreaksConfig.OUTBREAKS_MAP_FLUSH_TIMER;
            if(flushTimerTempMap <= 0) flushTimerMap = CobblemonOutbreaksConfig.TEMP_OUTBREAKS_MAP_FLUSH_TIMER;

            tickTempFlushTimer(server);
            tickFlushTimer(server);
        });
    }

    public static void tickTempFlushTimer(MinecraftServer server){
        if (flushTimerTempMap-- > 0) return;
        PokemonOutbreakManager outbreakManager = PokemonOutbreakManager.get(server.getLevel(Level.OVERWORLD));
        outbreakManager.clearTempMap();
        flushTimerTempMap =  CobblemonOutbreaksConfig.TEMP_OUTBREAKS_MAP_FLUSH_TIMER;
    }

    public static void tickFlushTimer(MinecraftServer server){
        if (flushTimerMap-- > 0) return;
        server.getPlayerList().broadcastSystemMessage(Component.translatable("cobblemonoutbreaks.clearing_pokemon_outbreaks_map").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC), true);
        PokemonOutbreakManager outbreakManager = PokemonOutbreakManager.get(server.getLevel(Level.OVERWORLD));
        outbreakManager.clearTempMap();
        flushTimerMap =  CobblemonOutbreaksConfig.OUTBREAKS_MAP_FLUSH_TIMER;
    }
}

