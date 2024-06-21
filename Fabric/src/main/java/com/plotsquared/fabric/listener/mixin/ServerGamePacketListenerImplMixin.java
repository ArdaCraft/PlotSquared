package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.HandleMoveVehicleCallback;
import com.plotsquared.fabric.listener.event.HandlePlayerMoveCallback;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void onHandleMovePlayer(ServerboundMovePlayerPacket serverboundMovePlayerPacket, CallbackInfo ci) {
        InteractionResult result = HandlePlayerMoveCallback.EVENT.invoker().handlePlayerMoveCallback(
                serverboundMovePlayerPacket,
                player
        );
        if (result != InteractionResult.PASS) {
            ci.cancel();
        }
    }

    @Inject(method = "handleMoveVehicle", at = @At("HEAD"), cancellable = true)
    public void onHandleMoveVehicle(ServerboundMoveVehiclePacket serverboundMoveVehiclePacket, CallbackInfo ci) {
        InteractionResult result = HandleMoveVehicleCallback.EVENT.invoker().handleMoveVehicleCallback(
                serverboundMoveVehiclePacket, player);
        if (result != InteractionResult.PASS) {
            ci.cancel();
        }
    }
}
