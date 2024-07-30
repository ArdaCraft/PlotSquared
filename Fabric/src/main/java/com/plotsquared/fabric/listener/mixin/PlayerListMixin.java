package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Shadow
    @Final
    private List<ServerPlayer> players;

    /**
     * @author JayemCeekay
     */
    @Overwrite
    public void broadcastAll(Packet<?> packet, ResourceKey<Level> resourceKey) {

        for (ServerPlayer serverPlayer : this.players) {
            if (serverPlayer.level().dimension() == resourceKey) {

                if (packet instanceof ClientboundGameEventPacket gameEventPacket) {
                    if (gameEventPacket.getEvent() == ClientboundGameEventPacket.START_RAINING
                            || gameEventPacket.getEvent() == ClientboundGameEventPacket.STOP_RAINING
                            || gameEventPacket.getEvent() == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
                        if (FabricUtil.adapt(serverPlayer).weather != PlotWeather.OFF && FabricUtil.adapt(serverPlayer).weather != PlotWeather.WORLD) {
                            continue;
                        }
                    }
                }

                if (packet instanceof ClientboundSetTimePacket) {
                    if (FabricUtil.adapt(serverPlayer).time != Long.MAX_VALUE) {
                        continue;
                    }
                }


                serverPlayer.connection.send(packet);
            }
        }

    }

}
