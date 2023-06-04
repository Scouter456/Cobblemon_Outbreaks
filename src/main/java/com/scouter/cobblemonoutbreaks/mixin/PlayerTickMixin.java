package com.scouter.cobblemonoutbreaks.mixin;

import com.scouter.cobblemonoutbreaks.config.CobblemonOutbreaksConfig;
import com.scouter.cobblemonoutbreaks.data.OutbreakPlayerManager;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortalEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerTickMixin {
    /**
     * The outbreak timer, initially set to the value defined in the config (OUTBREAK_SPAWN_TIMER).
     * Represents the time until the next outbreak of Pokémon portals.
     */
    @Unique
    int outbreakTimer = CobblemonOutbreaksConfig.OUTBREAK_SPAWN_TIMER;

    /**
     * The number of outbreak portals to spawn, defined in the config (OUTBREAK_SPAWN_COUNT).
     */
    @Unique
    int outbreakCount = CobblemonOutbreaksConfig.OUTBREAK_SPAWN_COUNT;
    @Inject(method = "tick", at = @At("HEAD"))
    private void battlePass$playerTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        Level level = player.level;

        if (!level.isClientSide) {
            OutbreakPlayerManager outbreakPlayerManager = OutbreakPlayerManager.get((ServerLevel) level);
            if (!outbreakPlayerManager.containsUUID(player.getUUID()))
                outbreakPlayerManager.setTimeLeft(player.getUUID(), outbreakTimer);
            int timeLeft = outbreakPlayerManager.getTimeLeft(player.getUUID());
            if (timeLeft-- > 0) {
                outbreakPlayerManager.setTimeLeft(player.getUUID(), timeLeft--);
                return;
            }
            for (int i = 0; i < outbreakCount; i++) {
                OutbreakPortalEntity outbreakPortal = new OutbreakPortalEntity(level, player);
                level.addFreshEntity(outbreakPortal);
            }
            outbreakPlayerManager.setTimeLeft(player.getUUID(), outbreakTimer);
        }
        }

}

