package com.scouter.cobblemonoutbreaks.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortal;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
    protected static List<ResourceLocation> resourceLocationList = new ArrayList<>();


    public static OutbreakPortal getPortalFromRl(ResourceLocation resourceLocation, OutbreakPortal outbreakPortal)
    {
        return data.getOrDefault(resourceLocation, outbreakPortal);
    }

    public static Map<ResourceLocation, OutbreakPortal> getData()
    {
        return data;
    }

    public static Map<ResourceLocation, OutbreakPortal> getRandomPortal(Level level)
    {
        Map<ResourceLocation, OutbreakPortal> map = new HashMap();
        ResourceLocation rl = resourceLocationList.get(level.random.nextInt(0,resourceLocationList.size()));
        OutbreakPortal outbreakPortal = data.get(rl);
        map.put(rl, outbreakPortal);
        return map;
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        LOGGER.info("Beginning loading of data for data loader: {}", this.folderName);

        Map<ResourceLocation, OutbreakPortal> newMap = new HashMap<>();
        List<ResourceLocation> newResourceLocationList = new ArrayList<>();
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
                        newMap.put(key, result.getFirst());
                        resourceLocationList.add(key);
                    })
                    .ifRight(partial -> LOGGER.error("Failed to parse data json for {} due to: {}", key, partial.message()));

        }
        this.resourceLocationList.addAll(newResourceLocationList);
        this.data = newMap;
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
