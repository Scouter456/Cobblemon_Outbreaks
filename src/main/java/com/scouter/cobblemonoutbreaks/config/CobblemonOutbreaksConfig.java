package com.scouter.cobblemonoutbreaks.config;
import com.mojang.datafixers.util.Pair;

import com.scouter.cobblemonoutbreaks.CobblemonOutbreaks;

public class CobblemonOutbreaksConfig {
    public static SimpleConfig CONFIG;
    private static CobblemonOutbreaksConfigProvider configs;
    public static boolean OUTBREAK_PORTAL_SPAWN_SOUND;
    public static int OUTBREAK_SPAWN_TIMER;
    public static int OUTBREAK_SPAWN_COUNT;
    public static boolean SEND_PORTAL_SPAWN_MESSAGE;
    public static boolean SPAWN_PORTAL_PARTICLES;
    public static boolean BIOME_SPECIFIC_SPAWNS;
    public static boolean BIOME_SPECIFIC_SPAWNS_DEBUG;

    public static int  MIN_SPAWN_RADIUS;
    public static int MAX_SPAWN_RADIUS;
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
        configs.addKeyValuePair(new Pair<>("send_outbreak_portal_spawn_message", true), "boolean");
        configs.addKeyValuePair(new Pair<>("spawn_portal_particles", false), "boolean");
        configs.addKeyValuePair(new Pair<>("biome_specific_spawns", false), "boolean");
        configs.addKeyValuePair(new Pair<>("biome_specific_spawns_debug", false), "boolean");
        configs.addKeyValuePair(new Pair<>("min_spawn_radius", 32), "Minumum spawn radius the portal can spawn around the player, minumum is 16, maximum is 48");
        configs.addKeyValuePair(new Pair<>("max_spaw_radius", 64), "Maximum spawn radius the portal can spawn around the player, minumum is 49 maximum is 112");

    }

    private static void assignConfigs() {
        OUTBREAK_PORTAL_SPAWN_SOUND = CONFIG.getOrDefault("outbreak_portal_spawn_sound", true);
        OUTBREAK_SPAWN_TIMER = CONFIG.getOrDefault("outbreak_spawn_timer", 36000);
        OUTBREAK_SPAWN_COUNT = CONFIG.getOrDefault("outbreak_spawn_count", 3);
        SEND_PORTAL_SPAWN_MESSAGE = CONFIG.getOrDefault("send_outbreak_portal_spawn_message", true);
        SPAWN_PORTAL_PARTICLES = CONFIG.getOrDefault("spawn_portal_particles", false);
        BIOME_SPECIFIC_SPAWNS = CONFIG.getOrDefault("biome_specific_spawns", false);
        BIOME_SPECIFIC_SPAWNS_DEBUG = CONFIG.getOrDefault("biome_specific_spawns_debug", false);
        MIN_SPAWN_RADIUS = CONFIG.getOrDefault("min_spawn_radius", 32);
        MAX_SPAWN_RADIUS = CONFIG.getOrDefault("max_spaw_radius", 64);

        System.out.println("All " + configs.getConfigsList().size() + " have been set properly");
    }
}
