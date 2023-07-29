package com.scouter.cobblemonoutbreaks.entity;

import com.scouter.cobblemonoutbreaks.CobblemonOutbreaks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class COEntity {
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblemonoutbreaks");


   //public static final EntityType<OutbreakPortalEntity> OUTBREAK_PORTAL = Registry.register(Registry.ENTITY_TYPE, prefix("outbreak_portal"),
   //        FabricEntityTypeBuilder.<OutbreakPortalEntity>create(MobCategory.MISC, OutbreakPortalEntity::new)
   //                .dimensions(EntityDimensions.fixed(0.01F, 0.01F))
   //                .fireImmune()
   //                .trackRangeChunks(4)
   //                .build());

    public static void ENTITY_TYPES(){
        LOGGER.info("Registering Entity Types for " + CobblemonOutbreaks.MODID);
    }
}


