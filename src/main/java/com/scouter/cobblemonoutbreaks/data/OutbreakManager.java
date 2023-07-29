package com.scouter.cobblemonoutbreaks.data;

import com.scouter.cobblemonoutbreaks.CobblemonOutbreaks;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortalEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OutbreakManager extends SavedData {

    // Map to store the ownership information of Pokemon
    private static Level level = null;
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblemonoutbreaks");
    private Map<UUID, OutbreakPortalEntity> outbreakPortalEntityMap = new ConcurrentHashMap<>();

    public static OutbreakManager get(Level level){
        if (level.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        // Get the vanilla storage manager from the level
        DimensionDataStorage storage = ((ServerLevel)level).getDataStorage();
        // Get the PokemonOutbreakManager if it already exists. Otherwise, create a new one.
        return storage.computeIfAbsent(OutbreakManager::new, OutbreakManager::new, "outbreakmanager");
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void clearMap(Level level){

        for(Map.Entry<UUID, OutbreakPortalEntity> entry : outbreakPortalEntityMap.entrySet()){
            entry.getValue().kill(level);
        }

        outbreakPortalEntityMap.clear();
        setDirty();
    }

    public Map<UUID, OutbreakPortalEntity> getOutbreakPortalEntityMap() {
        return outbreakPortalEntityMap;
    }

    public boolean containsPortal(UUID pos){
        return outbreakPortalEntityMap.containsKey(pos);
    }

    public OutbreakPortalEntity getOutbreakEntity(UUID pos){
        return outbreakPortalEntityMap.get(pos);
    }

    public void addPortal(UUID uuid, OutbreakPortalEntity outbreakPortalEntity){
        outbreakPortalEntityMap.put(uuid, outbreakPortalEntity);
        setDirty();
    }

    public void removePortal(UUID pokemonUUID){
        outbreakPortalEntityMap.remove(pokemonUUID);
        setDirty();
    }

    public OutbreakManager(){
    }

    public OutbreakManager(CompoundTag nbt) {
        // Load Pokemon ownership data from the provided CompoundTag
        ListTag outbreakList = nbt.getList("outbreakList", 10);
        for (int i = 0; i < outbreakList.size(); i++) {
            CompoundTag outbreakEntry = outbreakList.getCompound(i);
            UUID uuid = outbreakEntry.getUUID("uuid");
            OutbreakPortalEntity outbreakPortal = OutbreakPortalEntity.serialize(level, outbreakEntry.getCompound("outbreak"));
            if(outbreakPortal == null){
                CobblemonOutbreaks.LOGGER.error("Couldn't load outbreak");
                continue;
            }
            outbreakPortalEntityMap.put(uuid, outbreakPortal);
        }
        LOGGER.info("Finished loading {} outbreaks", outbreakPortalEntityMap.keySet().size());

    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        // Save Pokemon ownership data to the provided CompoundTag
        ListTag outbreakList = new ListTag();
        for (Map.Entry<UUID, OutbreakPortalEntity> entry : outbreakPortalEntityMap.entrySet()) {
            CompoundTag outbreakEntry = new CompoundTag();
            outbreakEntry.putUUID("uuid",entry.getKey());
            outbreakEntry.put("outbreak", entry.getValue().deserialize());
            outbreakList.add(outbreakEntry);
        }
        nbt.put("outbreakList", outbreakList);



        return nbt;
    }

}
