package com.plotsquared.fabric.listener.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ThrownPotion.class)
public class ThrownPotionMixin {

    @Inject(method = "makeAreaOfEffectCloud", at = @At("HEAD"), cancellable = true)
    private void onMakeAreaOfEffectCloud(ItemStack itemStack, Potion potion, CallbackInfo ci) {

    }

    @Inject(method = "applySplash", at = @At("HEAD"), cancellable = true)
    private void onApplySplash(List<MobEffectInstance> list, Entity entity, CallbackInfo ci) {

    }

    @Inject(method = "applyWater", at = @At("HEAD"), cancellable = true)
    private void onApplyWater(CallbackInfo ci) {

    }
}
