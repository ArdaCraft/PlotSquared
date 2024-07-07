package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.util.EntityTracker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        EntityTracker.setCurrentEntity((LivingEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void afterTickMovement(CallbackInfo ci) {
        EntityTracker.clearCurrentEntity();
    }

}
