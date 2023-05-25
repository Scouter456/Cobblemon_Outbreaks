package com.scouter.cobblemonoutbreaks.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortal;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.scouter.cobblemonoutbreaks.CobblemonOutbreaks.prefix;

public class OutbreaksJsonDataManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {


    public OutbreaksJsonDataManager(){
        this(STANDARD_GSON, prefix("outbreaks").getPath());
    }

    public OutbreaksJsonDataManager(Gson gson, String folderName) {
        super(gson, folderName);
        this.folderName = folderName;
    }
    private final String folderName;
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblemonoutbreaks");
    public static final Gson STANDARD_GSON = new Gson();
    protected static Map<ResourceLocation, OutbreakPortal> data = new HashMap<>();
    protected static Map<ResourceKey<Biome>, Map<ResourceLocation, OutbreakPortal>> biomeData = new HashMap<>();
    protected static List<ResourceLocation> resourceLocationList = new ArrayList<>();
    protected static Map<ResourceKey<Biome>, List<ResourceLocation>> resourceLocationMap = new HashMap();


    public static Map<ResourceKey<Biome>, Map<ResourceLocation, OutbreakPortal>> getBiomeData() {
        return biomeData;
    }

    public static OutbreakPortal getPortalFromRl(ResourceLocation resourceLocation, OutbreakPortal outbreakPortal) {
        return data.getOrDefault(resourceLocation, outbreakPortal);
    }

    public static Map<ResourceLocation, OutbreakPortal> getData() {
        return data;
    }

    public static Map<ResourceLocation, OutbreakPortal> getRandomPortalFromBiome(Level level, ResourceKey<Biome> biome) {
        Map<ResourceLocation, OutbreakPortal> map = new HashMap<>();
        ResourceLocation rl = getRandomResourceLocationFromBiome(level, biome);
        Map<ResourceLocation, OutbreakPortal> outbreakPortalMap = biomeData.getOrDefault(biome, new HashMap<>());
        OutbreakPortal outbreakPortal = outbreakPortalMap.getOrDefault(rl, null);

        if(outbreakPortal == null){
            outbreakPortalMap =  getRandomPortal(level);
            rl = outbreakPortalMap.keySet().stream().toList().get(0);
            outbreakPortal = outbreakPortalMap.values().stream().toList().get(0);
        }


        map.put(rl, outbreakPortal);
        return map;
    }

    public static Map<ResourceLocation, OutbreakPortal> getRandomPortal(Level level) {
        Map<ResourceLocation, OutbreakPortal> map = new HashMap<>();
        ResourceLocation rl = getRandomResourceLocation(level);
        OutbreakPortal outbreakPortal = data.get(rl);
        map.put(rl, outbreakPortal);
        return map;
    }

    private static ResourceLocation getRandomResourceLocationFromBiome(Level level, ResourceKey<Biome> biome) {
        List<ResourceLocation> resourceLocations = resourceLocationMap.getOrDefault(biome, Collections.emptyList());
        if (resourceLocations.isEmpty()) {
            return getRandomResourceLocation(level);
        }
        return resourceLocations.get(level.random.nextInt(resourceLocations.size()));
    }

    private static ResourceLocation getRandomResourceLocation(Level level) {
        return resourceLocationList.get(level.random.nextInt(resourceLocationList.size()));
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        LOGGER.info("Beginning loading of data for data loader: {}", this.folderName);

        Map<ResourceLocation, OutbreakPortal> newMap = new HashMap<>();
        List<ResourceLocation> newResourceLocationList = new ArrayList<>();
        Map<ResourceKey<Biome>, List<ResourceLocation>> resourceLocationBiomeMap = new HashMap<>();
        Map<ResourceKey<Biome>, Map<ResourceLocation, OutbreakPortal>> newBiomeData = new HashMap<>();
        data.clear();
        resourceLocationList.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation key = entry.getKey();
            JsonElement element = entry.getValue();

            // if we fail to parse json, log an error and continue
            // if we succeeded, add the resulting T to the ma
            OutbreakPortal.CODEC.decode(JsonOps.INSTANCE, element)
                    .get()
                    .ifLeft(result -> {
                        OutbreakPortal portal = result.getFirst();
                        newMap.put(key, portal);
                        List<ResourceLocation> spawnBiome = portal.getSpawnBiome();
                        spawnBiome.forEach(biome -> {
                            ResourceKey<Biome> biomeResourceKey = null;
                            try {
                                biomeResourceKey = ResourceKey.create(Registry.BIOME_REGISTRY, biome);
                            }catch (Exception e){
                                LOGGER.error("Could not find biome {} in {} due to ",biome,key, e);
                            }
                            if(biomeResourceKey == null){
                                LOGGER.error("Could not find biome {} in {}",biome, key);
                            }
                            List<ResourceLocation>  resourceLocations = resourceLocationBiomeMap.getOrDefault(biomeResourceKey, new ArrayList<>());
                            resourceLocations.add(key);
                            resourceLocationBiomeMap.put(biomeResourceKey, resourceLocations);
                            Map<ResourceLocation, OutbreakPortal> mapToPut = newBiomeData.computeIfAbsent(biomeResourceKey, k -> new HashMap<>());
                            mapToPut.put(key, portal);
                            newBiomeData.put(biomeResourceKey, mapToPut);
                        });
                        newResourceLocationList.add(key);
                    })
                    .ifRight(partial -> LOGGER.error("Failed to parse data json for {} due to: {}", key, partial.message()));

        }
        this.resourceLocationList = newResourceLocationList;
        this.data = newMap;
        this.biomeData = newBiomeData;
        this.resourceLocationMap = resourceLocationBiomeMap;
        LOGGER.info("Data loader for {} loaded {} jsons", this.folderName, this.data.size());
    }

    @Override
    public ResourceLocation getFabricId() {
        return prefix("outbreaks");
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return Collections.emptyList();
    }
}
