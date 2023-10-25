package com.scouter.cobbleoutbreaks.config;

import com.scouter.cobbleoutbreaks.CobblemonOutbreaks;
import net.minecraftforge.common.ForgeConfigSpec;

public class CobblemonOutbreaksConfig {

    public static final ForgeConfigSpec CONFIG_BUILDER;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        CONFIG_BUILDER = configBuilder.build();
    }

    public static ForgeConfigSpec.ConfigValue<Boolean> OUTBREAK_PORTAL_SPAWN_SOUND;
    public static ForgeConfigSpec.ConfigValue<Double> OUTBREAK_PORTAL_SPAWN_VOLUME;
    public static ForgeConfigSpec.ConfigValue<Double> OUTBREAK_PORTAL_POKEMON_SPAWN_VOLUME;
    public static ForgeConfigSpec.ConfigValue<Integer> OUTBREAK_SPAWN_TIMER;
    public static ForgeConfigSpec.ConfigValue<Integer> OUTBREAK_SPAWN_COUNT;
    public static ForgeConfigSpec.ConfigValue<Boolean> SPAWN_REWARDS;
    public static ForgeConfigSpec.ConfigValue<Integer> OUTBREAKS_MAP_FLUSH_TIMER;
    public static ForgeConfigSpec.ConfigValue<Integer> TEMP_OUTBREAKS_MAP_FLUSH_TIMER;
    public static ForgeConfigSpec.ConfigValue<Boolean> SEND_PORTAL_SPAWN_MESSAGE;
    public static ForgeConfigSpec.ConfigValue<Boolean> SPAWN_PORTAL_PARTICLES;
    public static ForgeConfigSpec.ConfigValue<Boolean> BIOME_SPECIFIC_SPAWNS;
    public static ForgeConfigSpec.ConfigValue<Boolean> BIOME_SPECIFIC_SPAWNS_DEBUG;
    public static ForgeConfigSpec.ConfigValue<Boolean> NOT_SPECIFIC_SPAWN_MESSAGE;
    public static ForgeConfigSpec.ConfigValue<Integer> MIN_SPAWN_RADIUS;
    public static ForgeConfigSpec.ConfigValue<Integer> MAX_SPAWN_RADIUS;
    public static ForgeConfigSpec.ConfigValue<Integer> COMMON_RARITY;
    public static ForgeConfigSpec.ConfigValue<Integer> UNCOMMON_RARITY;
    public static ForgeConfigSpec.ConfigValue<Integer> RARE_RARITY;
    public static ForgeConfigSpec.ConfigValue<Integer> EPIC_RARITY;
    public static ForgeConfigSpec.ConfigValue<Integer> LEGENDARY_RARITY;

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.comment(CobblemonOutbreaks.MODID + " Config");

        OUTBREAK_PORTAL_SPAWN_SOUND = builder.comment("Turns the portal sound on or off when a pokemon is spawned in an outbreak").define("outbreak_portal_spawn_sound", true);
        OUTBREAK_SPAWN_TIMER = builder.comment("Time it takes for an outbreak to spawn around the player (in ticks 36000 being 30 minutes)").defineInRange("outbreak_spawn_timer", 36000,100,1728000);
        OUTBREAK_SPAWN_COUNT = builder.comment("Amount of outbreaks that spawn when the timer runs out, 0 spawns nothing").defineInRange("outbreak_spawn_count", 3, 1, 64);
        SPAWN_REWARDS = builder.comment("If outbreaks should spawn rewards or not").define("outbreak_spawn_rewards", true);

        SEND_PORTAL_SPAWN_MESSAGE = builder.comment("Whether or not a message should be sent when an outbreak spawns, finishes and gets removed").define("send_outbreak_portal_spawn_message", true);
        SPAWN_PORTAL_PARTICLES = builder.comment("Turn particles on or off for the outbreak portal, this will make it easier to find them").define("spawn_portal_particles", false);
        BIOME_SPECIFIC_SPAWNS = builder.comment("Whether or not the outbreaks should spawn in predetermined biomes").define("biome_specific_spawns", false);
        BIOME_SPECIFIC_SPAWNS_DEBUG = builder.comment("A message that tells you what biome an outbreak is currently spawning in with their pokemon and if you expected this").define("biome_specific_spawns_debug", false);
        NOT_SPECIFIC_SPAWN_MESSAGE = builder.comment("A message that tells you what blockposition the outbreak is spawning in with their pokemon").define("not_specific_spawn_message", false);
        OUTBREAK_PORTAL_SPAWN_VOLUME = builder.comment("Volume of the spawning sound of the outbreak portal").defineInRange("outbreak_portal_spawn_volume", 1D, 0D, 10D);
        OUTBREAK_PORTAL_POKEMON_SPAWN_VOLUME = builder.comment("Volume of the pokemon spawning sound of the outbreak portal").defineInRange("outbreak_portal_pokemon_spawn_volume", 0.2D, 0D, 10D);
        OUTBREAKS_MAP_FLUSH_TIMER = builder.comment("Time it takes for the map with outbreaks to flush through, every 6 hours").defineInRange("outbreaks_flush_timer", 432000, 100, 1728000);
        TEMP_OUTBREAKS_MAP_FLUSH_TIMER = builder.comment("Time it takes for the map with temporary outbreaks to flush through, every hour").defineInRange("temp_outbreaks_flush_timer", 72000, 100, 1728000);

        MIN_SPAWN_RADIUS = builder.comment("Minumum spawn radius the portal can spawn around the player, minumum is 16, maximum is 48").defineInRange("min_spawn_radius", 32, 16, 48);
        MAX_SPAWN_RADIUS = builder.comment("Maximum spawn radius the portal can spawn around the player, minumum is 49 maximum is 112").defineInRange("max_spawn_radius", 64, 49, 112);

        COMMON_RARITY  = builder.comment("Rarity for common outbreaks").defineInRange("common_outbreak_rarity", 40, 1, 1000);
        UNCOMMON_RARITY  = builder.comment("Rarity for uncommon outbreaks").defineInRange("uncommon_outbreak_rarity", 30, 1, 1000);
        RARE_RARITY  = builder.comment("Rarity for rare outbreaks").defineInRange("rare_outbreak_rarity", 20, 1, 1000);
        EPIC_RARITY  = builder.comment("Rarity for epic outbreaks").defineInRange("epic_outbreak_rarity", 8, 1,1000);
        LEGENDARY_RARITY  = builder.comment("Rarity for legendary outbreaks").defineInRange("legendary_outbreak_rarity", 2,1,1000);

    }
}
