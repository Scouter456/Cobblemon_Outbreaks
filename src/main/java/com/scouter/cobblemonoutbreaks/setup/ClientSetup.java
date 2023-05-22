package com.scouter.cobblemonoutbreaks.setup;

import com.scouter.cobblemonoutbreaks.entity.COEntity;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortalRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(COEntity.OUTBREAK_PORTAL, OutbreakPortalRenderer::new);
    }

    public static void init(){

    }
}
