package com.plotsquared.fabric.listener.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelWriter.class)
public interface LevelWriterMixin {

    @WrapMethod(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    private boolean onSetBlock(BlockPos blockPos, BlockState blockState, int i, Operation<Boolean> original) {
        // Capture the stack trace and filter calls from Feature classes
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                if (Feature.class.isAssignableFrom(clazz)) {
                    try {
                        ((Feature) clazz.cast(null)).plotSquared$addBlockSet(blockPos, blockState);
                        ((Feature) clazz.cast(null)).plotSquared$addBlockNumber(blockPos, i);
                    } catch (Exception ignored) {
                        return original.call(blockPos, blockState, i);
                    }
                    return true;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return original.call(blockPos, blockState, i);
    }

}
