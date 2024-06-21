package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.ServerPlayerTeleportToCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {


    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    public void onPlayerTeleport(
            ServerLevel serverLevel,
            double d,
            double e,
            double f,
            Set<RelativeMovement> set,
            float g,
            float h,
            CallbackInfoReturnable<Boolean> cir
    ) {
        InteractionResult result =
                ServerPlayerTeleportToCallback.EVENT.invoker().serverPlayerTeleportToCallback(serverLevel, d, e
                        , f, set, g, h,
                        (ServerPlayer) (Object) this
                );
        if (result != InteractionResult.PASS) {
            cir.cancel();
        }
    }

}
