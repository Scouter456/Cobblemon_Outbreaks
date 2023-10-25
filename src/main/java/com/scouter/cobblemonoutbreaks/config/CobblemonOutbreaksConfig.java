package com.scouter.cobblemonoutbreaks.config;

import com.mojang.datafixers.util.Pair;
import com.scouter.cobblemonoutbreaks.CobblemonOutbreaks;

public class CobblemonOutbreaksConfig {
    public static SimpleConfig CONFIG;
    private static CobblemonOutbreaksConfigProvider configs;
    public static boolean OUTBREAK_PORTAL_SPAWN_SOUND;
    public static float OUTBREAK_PORTAL_SPAWN_VOLUME;
    public static float OUTBREAK_PORTAL_POKEMON_SPAWN_VOLUME;
    public static int OUTBREAK_SPAWN_TIMER;
    public static int OUTBREAK_SPAWN_COUNT;
    public static boolean SPAWN_REWARDS;
    public static int OUTBREAKS_MAP_FLUSH_TIMER;
    public static int TEMP_OUTBREAKS_MAP_FLUSH_TIMER;
    public static boolean SEND_PORTAL_SPAWN_MESSAGE;
    public static boolean SPAWN_PORTAL_PARTICLES;
    public static boolean BIOME_SPECIFIC_SPAWNS;
    public static boolean BIOME_SPECIFIC_SPAWNS_DEBUG;

    public static int  MIN_SPAWN_RADIUS;
    public static int MAX_SPAWN_RADIUS;
    public static int COMMON_RARITY;
    public static int UNCOMMON_RARITY;
    public static int RARE_RARITY;
    public static int EPIC_RARITY;
    public static int LEGENDARY_RARITY;
    public static void registerConfigs() {
        configs = new CobblemonOutbreaksConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(CobblemonOutbreaks.MODID + "config").provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("outbreak_portal_spawn_sound", true), "boolean");
        configs.addKeyValuePair(new Pair<>("outbreak_spawn_timer", 36000), "int");
        configs.addKeyValuePair(new Pair<>("outbreak_spawn_count", 3), "int");
        configs.addKeyValuePair(new Pair<>("outbreak_spawn_rewards", true), "If outbreaks should spawn rewards or not");

        configs.addKeyValuePair(new Pair<>("send_outbreak_portal_spawn_message", true), "boolean");
        configs.addKeyValuePair(new Pair<>("spawn_portal_particles", false), "boolean");
        configs.addKeyValuePair(new Pair<>("biome_specific_spawns", false), "boolean");
        configs.addKeyValuePair(new Pair<>("biome_specific_spawns_debug", false), "boolean");
        configs.addKeyValuePair(new Pair<>("min_spawn_radius", 32), "Minumum spawn radius the portal can spawn around the player, minumum is 16, maximum is 48");
        configs.addKeyValuePair(new Pair<>("max_spawn_radius", 64), "Maximum spawn radius the portal can spawn around the player, minumum is 49 maximum is 112");
        configs.addKeyValuePair(new Pair<>("outbreak_portal_spawn_volume", 1F), "Volume of the spawning sound of the outbreak portal");
        configs.addKeyValuePair(new Pair<>("outbreak_portal_pokemon_spawn_volume", 0.2F), "Volume of the pokemon spawning sound of the outbreak portal");
        configs.addKeyValuePair(new Pair<>("outbreaks_flush_timer", 432000), "Time it takes for the map with outbreaks to flush through, every 6 hours");
        configs.addKeyValuePair(new Pair<>("common_outbreak_rarity", 40), "Rarity for common outbreaks");
        configs.addKeyValuePair(new Pair<>("uncommon_outbreak_rarity", 30), "Rarity for uncommon outbreaks");
        configs.addKeyValuePair(new Pair<>("rare_outbreak_rarity", 20), "Rarity for rare outbreaks");
        configs.addKeyValuePair(new Pair<>("epic_outbreak_rarity", 8), "Rarity for epic outbreaks");
        configs.addKeyValuePair(new Pair<>("legendary_outbreak_rarity", 2), "Rarity for legendary outbreaks");

    }

    private static void assignConfigs() {
        OUTBREAK_PORTAL_SPAWN_SOUND = CONFIG.getOrDefault("outbreak_portal_spawn_sound", true);
        OUTBREAK_SPAWN_TIMER = CONFIG.getOrDefault("outbreak_spawn_timer", 36000);
        OUTBREAK_SPAWN_COUNT = CONFIG.getOrDefault("outbreak_spawn_count", 3);
        SPAWN_REWARDS = CONFIG.getOrDefault("outbreak_spawn_rewards", true);
        OUTBREAK_PORTAL_SPAWN_VOLUME = CONFIG.getOrDefault("outbreak_portal_spawn_volume", 1);
        OUTBREAK_PORTAL_POKEMON_SPAWN_VOLUME = CONFIG.getOrDefault("outbreak_portal_pokemon_spawn_volume", 0.2F);
        OUTBREAKS_MAP_FLUSH_TIMER = CONFIG.getOrDefault("outbreaks_flush_timer", 432000);
        TEMP_OUTBREAKS_MAP_FLUSH_TIMER = CONFIG.getOrDefault("temp_outbreaks_flush_timer", 72000);


        SEND_PORTAL_SPAWN_MESSAGE = CONFIG.getOrDefault("send_outbreak_portal_spawn_message", true);
        SPAWN_PORTAL_PARTICLES = CONFIG.getOrDefault("spawn_portal_particles", false);
        BIOME_SPECIFIC_SPAWNS = CONFIG.getOrDefault("biome_specific_spawns", false);
        BIOME_SPECIFIC_SPAWNS_DEBUG = CONFIG.getOrDefault("biome_specific_spawns_debug", false);
        MIN_SPAWN_RADIUS = CONFIG.getOrDefault("min_spawn_radius", 32);
        MAX_SPAWN_RADIUS = CONFIG.getOrDefault("max_spawn_radius", 64);

        COMMON_RARITY = CONFIG.getOrDefault("common_outbreak_rarity", 40);
        UNCOMMON_RARITY = CONFIG.getOrDefault("uncommon_outbreak_rarity", 30);
        RARE_RARITY = CONFIG.getOrDefault("rare_outbreak_rarity", 20);
        EPIC_RARITY = CONFIG.getOrDefault("epic_outbreak_rarity", 8);
        LEGENDARY_RARITY = CONFIG.getOrDefault("legendary_outbreak_rarity", 2);

        System.out.println("All " + configs.getConfigsList().size() + " have been set properly");
    }
}
