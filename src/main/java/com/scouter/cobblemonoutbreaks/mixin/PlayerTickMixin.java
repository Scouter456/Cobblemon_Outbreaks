package com.scouter.cobblemonoutbreaks.mixin;

import com.scouter.cobblemonoutbreaks.config.CobblemonOutbreaksConfig;
import com.scouter.cobblemonoutbreaks.data.OutbreakPlayerManager;
import com.scouter.cobblemonoutbreaks.entity.OutbreakPortalEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
    private void outbreakPortal$playerTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        Level level = player.level();

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

                BlockPos pos = findSuitableSpawnPoint(player);
                int y = (int) pos.getY();
                if(player.level().dimension() == Level.NETHER){
                    if(y <= 0){
                        sendMessageToPlayer(player, y);
                        continue;
                    }
                }
                if(player.level().dimension() == Level.END){
                    if(y <= 0) {
                        sendMessageToPlayer(player, y);
                        continue;
                    }
                }
                if(player.level().dimension() == Level.OVERWORLD){
                    if(y <= -64){
                        sendMessageToPlayer(player, y);
                        continue;
                    }
                }
                OutbreakPortalEntity outbreakPortal = new OutbreakPortalEntity(level, player, pos);
                //level.addFreshEntity(outbreakPortal);
            }
            outbreakPlayerManager.setTimeLeft(player.getUUID(), outbreakTimer);
        }
    }

    public BlockPos findSuitableSpawnPoint(Player player){
        int maxRange = CobblemonOutbreaksConfig.MAX_SPAWN_RADIUS;
        int minRange = CobblemonOutbreaksConfig.MIN_SPAWN_RADIUS;



        if(maxRange > 112 || maxRange < 49){
            maxRange = 64;
        }
        if(minRange > 48 || minRange < 16){
            minRange = 32;
        }

        int randomX = player.level().random.nextInt(minRange) + (player.level().random.nextBoolean() ? 5 : -5);
        int randomZ = player.level().random.nextInt(maxRange) + (player.level().random.nextBoolean() ? 5 : -5);



        int playerPosX = player.getBlockX();
        int playerPosY = player.getBlockY();
        int playerPosZ = player.getBlockZ();

        boolean changeModX = player.level().random.nextBoolean();
        boolean changeModZ = player.level().random.nextBoolean();

        if(changeModX){
            randomX = -randomX;
        }

        if(changeModZ){
            randomZ = -randomZ;
        }
        int y = (int)player.getY();
        while ((player.level().getBlockState(new BlockPos(playerPosX + randomX, y, playerPosZ + randomZ)).isAir() && player.level().getBlockState(new BlockPos(playerPosX + randomX, y - 1, playerPosZ + randomZ)).isAir()) ||
                (!player.level().getBlockState(new BlockPos(playerPosX + randomX, y, playerPosZ + randomZ)).isAir() && !player.level().getBlockState(new BlockPos(playerPosX + randomX, y - 1, playerPosZ + randomZ)).isAir()) ||
                (!player.level().getBlockState(new BlockPos(playerPosX + randomX, y, playerPosZ + randomZ)).isAir() && player.level().getBlockState(new BlockPos(playerPosX + randomX, y - 1, playerPosZ + randomZ)).isAir())) {
            if(y < -64) break;
            if(!player.level().getBlockState(new BlockPos(playerPosX + randomX, y, playerPosZ + randomZ)).getFluidState().isEmpty()) break;
            y--;
        }

        BlockPos blockPos = new BlockPos(playerPosX + randomX, y , playerPosZ + randomZ);
        return blockPos;
    }

    public void sendMessageToPlayer(Player player, int y){
        if(CobblemonOutbreaksConfig.SEND_PORTAL_SPAWN_MESSAGE) {
            if(CobblemonOutbreaksConfig.BIOME_SPECIFIC_SPAWNS_DEBUG) {
                MutableComponent yLevel = Component.literal(String.valueOf(y)).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC);
                MutableComponent outBreakMessage = Component.translatable("cobblemonoutbreaks.unlucky_spawn_debug", yLevel).withStyle(ChatFormatting.DARK_AQUA);
                player.sendSystemMessage(outBreakMessage);
            } else{
                MutableComponent outBreakMessage = Component.translatable("cobblemonoutbreaks.unlucky_spawn").withStyle(ChatFormatting.DARK_AQUA);
                player.sendSystemMessage(outBreakMessage);
            }
        }
    }

}

