package com.plotsquared.fabric.listener.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.plotsquared.fabric.listener.event.FarmBlockDryEvent;
import com.plotsquared.fabric.listener.event.FarmBlockMoistureChangeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(value = {FarmBlock.class
})
public class FarmBlockMixin {

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FarmBlock;turnToDirt(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
            )
    )
    public void onScheduledTickSetBlockState(
            Entity entity, BlockState blockState, Level level, BlockPos blockPos, Operation<Void> original, BlockState from
    ) {
        var events = Stimuli.select();

        try (var invokers = events.at(level, blockPos)) {
            var result = invokers.get(FarmBlockDryEvent.EVENT).onSoilDry(entity, blockState, level, blockPos, from);
            if (result == InteractionResult.FAIL) {
                return;
            }
        }
        original.call(entity, blockState, level, blockPos);
    }

    @WrapOperation(
            method = "randomTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    public boolean onScheduledTickSetBlock(
            ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, int i, Operation<Boolean> original, BlockState from
    ) {
        var events = Stimuli.select();

        try (var invokers = events.at(serverLevel, blockPos)) {
            var result = invokers.get(FarmBlockMoistureChangeEvent.EVENT).onMoistureChange(serverLevel, blockPos, blockState, i
                    , from);
            if (result == InteractionResult.FAIL) {
                return false;
            }
        }
        original.call(serverLevel, blockPos, blockState, i);
        return false;
    }


}
