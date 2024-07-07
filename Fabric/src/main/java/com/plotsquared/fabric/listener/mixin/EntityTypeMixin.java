package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.EntityTypeCreateCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin<T> {


    @Inject(method = "create(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;" +
            "Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)" +
            "Lnet/minecraft/world/entity/Entity;", at = @At("RETURN"), cancellable = true)
    public void createMob(
            ServerLevel serverLevel,
            CompoundTag compoundTag,
            Consumer<?> consumer,
            BlockPos blockPos,
            MobSpawnType mobSpawnType,
            boolean bl,
            boolean bl2,
            CallbackInfoReturnable<?> cir
    ) {
        InteractionResult result =
                EntityTypeCreateCallback.EVENT.invoker().onCreate(
                        serverLevel, compoundTag, consumer, blockPos, mobSpawnType, bl, bl2,
                cir.getReturnValue());
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(null);
        }
    }

}
