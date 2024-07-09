package com.plotsquared.fabric.listener.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.plotsquared.fabric.listener.event.ExplosionPrimedEvent;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    @Final
    private float radius;

    @WrapMethod(method = "explode")
    public void onExplosion(Operation<Void> original) {
        ExplosionPrimedEvent.EVENT.invoker().onPrime(this.radius);
        original.call();
    }
}
