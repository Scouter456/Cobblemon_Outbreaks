package com.scouter.cobblemonoutbreaks.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.scouter.cobblemonoutbreaks.CobblemonOutbreaks;
import com.scouter.cobblemonoutbreaks.data.OutbreakManager;
import com.scouter.cobblemonoutbreaks.data.OutbreakPlayerManager;
import com.scouter.cobblemonoutbreaks.data.OutbreaksJsonDataManager;
import com.scouter.cobblemonoutbreaks.data.PokemonOutbreakManager;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortal;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortalEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class OutbreakPortalCommand {
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblemonoutbreaks");
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_TYPE = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(OutbreaksJsonDataManager.getData().keySet().stream().map(ResourceLocation::toString), builder);
    };


    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("outbreakportal").requires(s -> s.hasPermission(2));

        builder.then(Commands.literal("flush_pokemon_map").executes(c -> {
            return flushPokemonMap(c);
        }));

        builder.then(Commands.literal("clear_outbreaks").executes(c -> {
            return clearOutbreaks(c);
        }));

        builder.then(Commands.literal("clear_player_timer").executes(c -> {
            return clearTimers(c);
        }));

        builder.then(Commands.literal("set_time_to_config_value").executes(c -> {
            return setToConfigValue(c);
        }));

        builder.then(Commands.literal("update_files").executes(c -> {
            return updateFiles(c);
        }));

        builder.then(Commands.argument("pos", Vec3Argument.vec3()).then(Commands.argument("type", ResourceLocationArgument.id()).suggests(SUGGEST_TYPE).executes(c -> {
            return openOutBreakPortal(c, Vec3Argument.getVec3(c, "pos"), ResourceLocationArgument.getId(c, "type"));
        })));

        pDispatcher.register(builder);
    }

    public static int openOutBreakPortal(CommandContext<CommandSourceStack> c, Vec3 pos, ResourceLocation type) {
        try {
            Entity nullableSummoner = c.getSource().getEntity();
            Player summoner = nullableSummoner instanceof Player ? (Player) nullableSummoner : c.getSource().getLevel().getNearestPlayer(pos.x(), pos.y(), pos.z(), 64, false);
            BlockPos blockPos = BlockPos.containing(pos.x(), pos.y(), pos.z());
            OutbreakPortalEntity outbreakPortalEntity = new OutbreakPortalEntity(c.getSource().getLevel(), summoner, type, blockPos);
            outbreakPortalEntity.setBlockPosition(pos);

            //c.getSource().getLevel().addFreshEntity(outbreakPortalEntity);
        } catch (Exception ex) {
            c.getSource().sendFailure(Component.literal("Exception thrown - see log"));
            ex.printStackTrace();
        }
        return 0;
    }

    public static int flushPokemonMap(CommandContext<CommandSourceStack> c) {
        try {
            ServerLevel level = c.getSource().getLevel();
            level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("cobblemonoutbreaks.clearing_pokemon_outbreaks_map").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC), true);
            PokemonOutbreakManager pokemonOutbreakManager = PokemonOutbreakManager.get(level);
            pokemonOutbreakManager.clearMap();
            pokemonOutbreakManager.clearTempMap();
        } catch (Exception ex) {
            c.getSource().sendFailure(Component.literal("Exception thrown - see log"));
            ex.printStackTrace();
        }
        return 0;
    }

    public static int clearOutbreaks(CommandContext<CommandSourceStack> c) {
        try {
            ServerLevel level = c.getSource().getLevel();
            CobblemonOutbreaks.LOGGER.info("command triggered???");
            level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("cobblemonoutbreaks.clearing_outbreaks_map").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC), true);
            OutbreakManager pokemonOutbreakManager = OutbreakManager.get(level);
            pokemonOutbreakManager.clearMap(level);
        } catch (Exception ex) {
            c.getSource().sendFailure(Component.literal("Exception thrown - see log"));
            ex.printStackTrace();
        }
        return 0;
    }

    public static int clearTimers(CommandContext<CommandSourceStack> c) {
        try {
            ServerLevel level = c.getSource().getLevel();
            level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("cobblemonoutbreaks.clear_player_timers").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC), true);
            OutbreakPlayerManager outbreakPlayerManager = OutbreakPlayerManager.get(level);
            outbreakPlayerManager.clearTimeLeft();
        } catch (Exception ex) {
            c.getSource().sendFailure(Component.literal("Exception thrown - see log"));
            ex.printStackTrace();
        }
        return 0;
    }

    public static int setToConfigValue(CommandContext<CommandSourceStack> c) {
        try {
            ServerLevel level = c.getSource().getLevel();
            level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("cobblemonoutbreaks.set_to_config_value").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC), true);
            OutbreakPlayerManager outbreakPlayerManager = OutbreakPlayerManager.get(level);
            outbreakPlayerManager.setTimeLeftToNewConfig();
        } catch (Exception ex) {
            c.getSource().sendFailure(Component.literal("Exception thrown - see log"));
            ex.printStackTrace();
        }
        return 0;
    }

    public static int updateFiles(CommandContext<CommandSourceStack> c) {
        int updatedFile = 0;
        Entity nullableSummoner = c.getSource().getEntity();
        Path PATH = FabricLoader.getInstance().getGameDir().resolve("cobblemon_outbreaks_updated_json_files");
        try {
            Map<ResourceLocation, OutbreakPortal> portals = OutbreaksJsonDataManager.getData();

            try {
                Files.createDirectories(PATH); // Create the directory if it doesn't exist
            } catch (IOException e) {
                LOGGER.error("Error creating directory: ", e);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            for(Map.Entry<ResourceLocation, OutbreakPortal> portalEntry : portals.entrySet()) {
                if(portalEntry.getValue().isOld()){
                DataResult<JsonElement> jsonElement = OutbreakPortal.CODEC.encodeStart(JsonOps.INSTANCE, portalEntry.getValue());

                try (FileWriter writer = new FileWriter(PATH + "/" + portalEntry.getKey().getPath() + ".json")) {
                    gson.toJson(jsonElement.get().left().get(), writer);
                    updatedFile += 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                }
            }
        } catch (Exception ex) {
            c.getSource().sendFailure(Component.literal("Exception thrown - see log"));
            ex.printStackTrace();
        }
        if(nullableSummoner instanceof Player player){
            player.sendSystemMessage(Component.literal("A new directory has been created at: " + PATH).withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.literal("Updated " + updatedFile + " files").withStyle(ChatFormatting.GREEN));

        }

        return 0;
    }
}
