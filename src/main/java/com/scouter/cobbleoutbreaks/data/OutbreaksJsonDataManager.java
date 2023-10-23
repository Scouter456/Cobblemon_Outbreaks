package com.scouter.cobbleoutbreaks.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.scouter.cobbleoutbreaks.entity.OutbreakPortal;
import com.scouter.cobbleoutbreaks.entity.SpawnAlgorithms;
import com.scouter.cobbleoutbreaks.entity.SpawnLevelAlgorithms;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;

import java.util.*;

import static com.scouter.cobbleoutbreaks.CobblemonOutbreaks.prefix;

public class OutbreaksJsonDataManager extends SimpleJsonResourceReloadListener {


    private static final Gson STANDARD_GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final OutbreakPortal PORTAL = new OutbreakPortal(OutbreakSpecies.getDefaultSpecies(),
            OutbreakRewards.getDefaultRewards(),
            OutbreakAlgorithms.getDefaultAlgoritms(),
            36000, Collections.emptyList());
    protected static Map<ResourceLocation, OutbreakPortal> data = new HashMap<>();
    protected static Map<ResourceKey<Biome>, Map<PokemonRarity, List<Map<ResourceLocation, OutbreakPortal>>>> biomeData = new HashMap<>();
    protected static List<ResourceLocation> resourceLocationList = new ArrayList<>();
    protected static Map<PokemonRarity, List<ResourceLocation>> listWithRarity = new HashMap<>();
    protected static Map<ResourceKey<Biome>, List<ResourceLocation>> resourceLocationMap = new HashMap();
    private final String folderName;
    public OutbreaksJsonDataManager()
    {
        this(prefix("outbreaks").getPath(), STANDARD_GSON);
    }


    public OutbreaksJsonDataManager(String folderName, Gson gson)
    {
        super(gson, folderName);
        this.folderName = folderName;
    }


    public static Map<ResourceKey<Biome>, Map<PokemonRarity, List<Map<ResourceLocation, OutbreakPortal>>>> getBiomeData() {
        return biomeData;
    }

    public static OutbreakPortal getPortalFromRl(ResourceLocation resourceLocation) {
        return data.getOrDefault(resourceLocation, PORTAL);
    }

    public static Map<ResourceLocation, OutbreakPortal> getData() {
        return data;
    }

    public static Map<ResourceLocation, OutbreakPortal> getRandomPortalFromBiome(Level level, ResourceKey<Biome> biome) {
        Map<ResourceLocation, OutbreakPortal> map = new HashMap<>();

        Map<PokemonRarity, List<Map<ResourceLocation, OutbreakPortal>>> outbreakPortalMap = biomeData.getOrDefault(biome, new HashMap<>());
        RandomSource randomSource = level.random;
        PokemonRarity rarity = PokemonRarity.getRandomRarity(randomSource);
        List<Map<ResourceLocation, OutbreakPortal>> rlList = outbreakPortalMap.getOrDefault(rarity, outbreakPortalMap.getOrDefault(PokemonRarity.COMMON, new ArrayList<>()));
        ResourceLocation rl = getRandomResourceLocationFromList(level, rlList);
        OutbreakPortal outbreakPortal = data.getOrDefault(rl, null);

        if(outbreakPortal == null){

            Map<ResourceLocation, OutbreakPortal> outbreakPortalMapRand = getRandomPortal(level);
            rl = outbreakPortalMapRand.keySet().stream().toList().get(0);
            outbreakPortal = outbreakPortalMapRand.values().stream().toList().get(0);
        }


        map.put(rl, outbreakPortal);
        return map;
    }

    public static Map<ResourceLocation, OutbreakPortal> getRandomPortal(Level level) {
        Map<ResourceLocation, OutbreakPortal> map = new HashMap<>();
        ResourceLocation rl = getRandomResourceLocation(level);
        OutbreakPortal outbreakPortal = data.getOrDefault(rl,PORTAL);
        map.put(rl, outbreakPortal);
        return map;
    }

    private static ResourceLocation getRandomResourceLocationFromList(Level level, List<Map<ResourceLocation, OutbreakPortal>> list) {
        if (list.isEmpty()) {
            return getRandomResourceLocation(level);
        }

        Map<ResourceLocation, OutbreakPortal> randomMap = list.get(level.random.nextInt(list.size()));
        return randomMap.keySet().iterator().next();
    }

    private static ResourceLocation getRandomResourceLocationFromBiome(Level level, ResourceKey<Biome> biome) {
        List<ResourceLocation> resourceLocations = resourceLocationMap.getOrDefault(biome, Collections.emptyList());
        if (resourceLocations.isEmpty()) {
            return getRandomResourceLocation(level);
        }
        return resourceLocations.get(level.random.nextInt(resourceLocations.size()));
    }

    private static ResourceLocation getRandomResourceLocation(Level level) {
        if (!listWithRarity.isEmpty()) {
            List<ResourceLocation> resourceLocations = listWithRarity.getOrDefault(PokemonRarity.COMMON, null);
            if(resourceLocations == null || resourceLocations.isEmpty()) return null;
            return resourceLocations.get(level.random.nextInt(resourceLocations.size()));
        }
        return null;
    }

    public static void populateMap(ServerLevel level) {
        Map<ResourceKey<Biome>, Map<PokemonRarity, List<Map<ResourceLocation, OutbreakPortal>>>> newBiomeData = new HashMap<>();
        Map<ResourceKey<Biome>, List<ResourceLocation>> resourceLocationBiomeMap = new HashMap<>();
        for (OutbreakPortal portal : data.values()) {
            List<ResourceLocation> tagsRL = portal.getSpawnBiomeTags();
            List<ResourceLocation> biomesRL = portal.getSpawnBiome();
            for (ResourceLocation tag : tagsRL) {
                TagKey<Biome> biomeTagKey = TagKey.create(Registries.BIOME, tag);
                level.registryAccess().registry(Registries.BIOME).ifPresent(reg -> {
                    Iterable<Holder<Biome>> biomeHolder = reg.getTagOrEmpty(biomeTagKey);
                    for(Holder<Biome> biome : biomeHolder){
                        ResourceKey<Biome> biomeResourceKey = biome.unwrapKey().get();
                        Map<PokemonRarity, List<Map<ResourceLocation, OutbreakPortal>>> rarityMapMap = newBiomeData.computeIfAbsent(biomeResourceKey, k -> new HashMap<>());
                        PokemonRarity rarity = portal.getSpeciesData().getRarity();
                        List<Map<ResourceLocation, OutbreakPortal>> listToPut = rarityMapMap.computeIfAbsent(rarity, k -> new ArrayList<>());
                        Map<ResourceLocation, OutbreakPortal> mapToPut = new HashMap<>();
                        mapToPut.put(portal.getJsonLocation(), portal);
                        listToPut.add(mapToPut);
                        rarityMapMap.put(rarity, listToPut);
                        newBiomeData.put(biomeResourceKey, rarityMapMap);

                        List<ResourceLocation> resourceLocations = resourceLocationBiomeMap.getOrDefault(biomeResourceKey, new ArrayList<>());
                        resourceLocations.add(portal.getJsonLocation());
                        resourceLocationBiomeMap.put(biomeResourceKey, resourceLocations);


                    }

                    if (!biomeHolder.iterator().hasNext()) {
                        LOGGER.error("Tag for {} does not have any biomes!", biomeTagKey);
                        LOGGER.error("Outbreak for {} might not have any biomes assigned!", portal.getJsonLocation());
                    }
                });
            }

            for (ResourceLocation biome : biomesRL) {
                ResourceKey<Biome> biomeResourceKey = null;
                try {
                    biomeResourceKey = ResourceKey.create(Registries.BIOME, biome);
                } catch (Exception e) {
                    LOGGER.error("Could not find biome {} in portal for {} due to {}", biome, portal.getJsonLocation(), e);
                }
                if (biomeResourceKey == null) {
                    LOGGER.error("Could not find biome {} in portal for {}, skipping!", biome, portal.getJsonLocation());
                    continue;
                }

                List<ResourceLocation> resourceLocations = resourceLocationBiomeMap.getOrDefault(biomeResourceKey, new ArrayList<>());
                resourceLocations.add(portal.getJsonLocation());
                resourceLocationBiomeMap.put(biomeResourceKey, resourceLocations);

                Map<PokemonRarity, List<Map<ResourceLocation, OutbreakPortal>>> mapToPut = newBiomeData.computeIfAbsent(biomeResourceKey, k -> new HashMap<>());
                PokemonRarity rarity = portal.getSpeciesData().getRarity();
                List<Map<ResourceLocation, OutbreakPortal>> listMap = mapToPut.computeIfAbsent(rarity, k -> new ArrayList<>());
                Map<ResourceLocation, OutbreakPortal> portalMap = new HashMap<>();
                portalMap.put(portal.getJsonLocation(), portal);
                listMap.add(portalMap);
                mapToPut.put(rarity, listMap);

                newBiomeData.put(biomeResourceKey, mapToPut);
            }

            int minLevel = portal.getOutbreakAlgorithms().getMinPokemonLevel();
            int maxLevel = portal.getOutbreakAlgorithms().getMaxPokemonLevel();

            if(minLevel > maxLevel){
                LOGGER.error("Portal with {}, has a bigger min_pokemon_level than max_pokemon_level", portal.getJsonLocation());
            }
        }
        LOGGER.info("Registered {} biomes with pokemon!", newBiomeData.keySet().size());
        biomeData.putAll(newBiomeData);
        resourceLocationMap.putAll(resourceLocationBiomeMap);
        newBiomeData.clear();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        LOGGER.info("Beginning loading of data for data loader: {}", this.folderName);

        Map<ResourceLocation, OutbreakPortal> newMap = new HashMap<>();
        List<ResourceLocation> newResourceLocationList = new ArrayList<>();
        Map<PokemonRarity, List<ResourceLocation>> newResourceLocationMap = new HashMap<>();
        Map<ResourceKey<Biome>, List<ResourceLocation>> resourceLocationBiomeMap = new HashMap<>();
        Map<ResourceKey<Biome>, Map<ResourceLocation, OutbreakPortal>> newBiomeData = new HashMap<>();
        data.clear();
        biomeData.clear();
        resourceLocationMap.clear();
        resourceLocationList.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation key = entry.getKey();
            JsonElement element = entry.getValue();

            // if we fail to parse json, log an error and continue
            OutbreakPortal.EITHER.decode(JsonOps.INSTANCE, element)
                    .get()
                    .ifLeft(result -> {
                        OutbreakPortal portal = result.getFirst();
                        newMap.put(key, portal);
                        portal.setJsonLocation(key);
                        List<ResourceLocation> spawnBiome = portal.getSpawnBiome();
                        spawnBiome.forEach(biome -> {
                            ResourceKey<Biome> biomeResourceKey = null;
                            try {
                                biomeResourceKey = ResourceKey.create(Registries.BIOME, biome);
                            } catch (Exception e) {
                                LOGGER.error("Could not find biome {} in {} due to ", biome, key, e);
                            }
                            if (biomeResourceKey == null) {
                                LOGGER.error("Could not find biome {} in {}", biome, key);
                            }

                            List<ResourceLocation> resourceLocations = resourceLocationBiomeMap.getOrDefault(biomeResourceKey, new ArrayList<>());
                            resourceLocations.add(key);
                            resourceLocationBiomeMap.put(biomeResourceKey, resourceLocations);
                        });
                        newResourceLocationList.add(key);
                        PokemonRarity rarity = portal.getSpeciesData().getRarity();
                        List<ResourceLocation> resourceLocations = newResourceLocationMap.computeIfAbsent(rarity, k -> new ArrayList<>());
                        resourceLocations.add(key);
                        newResourceLocationMap.put(rarity, resourceLocations);
                    })
                    .ifRight(partial -> LOGGER.error("Failed to parse data json for {} due to: {}", key, partial.message()));

        }
        this.listWithRarity = newResourceLocationMap;
        this.resourceLocationList = newResourceLocationList;
        this.data = newMap;
        LOGGER.info("Data loader for {} loaded {} jsons", this.folderName, this.data.size());
    }
}
