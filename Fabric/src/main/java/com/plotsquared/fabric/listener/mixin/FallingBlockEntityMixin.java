package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.FallingBlockEntityEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onFall(CallbackInfo ci) {
        InteractionResult result = FallingBlockEntityEvent.EVENT.invoker().onFall((FallingBlockEntity) (Object) this);
        if(result != InteractionResult.PASS) {
            ci.cancel();
        }
    }

}
