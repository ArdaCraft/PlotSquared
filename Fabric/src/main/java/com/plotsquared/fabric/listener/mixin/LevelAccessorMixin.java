package com.plotsquared.fabric.listener.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelAccessor.class)
public interface LevelAccessorMixin {

    @WrapMethod(method = "blockUpdated")
    default void onSetBlock(BlockPos blockPos, Block block, Operation<Void> original) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                if (Feature.class.isAssignableFrom(clazz)) {
                    return;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        original.call(blockPos, block);
    }


}
