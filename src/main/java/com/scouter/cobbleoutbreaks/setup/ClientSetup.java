package com.scouter.cobbleoutbreaks.setup;


import com.scouter.cobbleoutbreaks.CobblemonOutbreaks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CobblemonOutbreaks.MODID, value = Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(FMLClientSetupEvent event){
     //   EntityRenderers.register(COEntity.OUTBREAK_PORTAL.get(), OutbreakPortalRenderer::new);
    }
}

