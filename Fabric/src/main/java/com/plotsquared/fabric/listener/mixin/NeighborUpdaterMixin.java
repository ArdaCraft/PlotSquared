package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.EmptyContentsCallback;
import com.plotsquared.fabric.listener.event.OnExecuteUpdateCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NeighborUpdater.class)
public interface NeighborUpdaterMixin {

    @Inject(method = "executeUpdate", at = @At("HEAD"), cancellable = true)
    private static void onExecuteUpdate(
            Level level,
            BlockState blockState,
            BlockPos blockPos,
            Block block,
            BlockPos blockPos2,
            boolean bl,
            CallbackInfo ci
    ) {
        InteractionResult result =
                OnExecuteUpdateCallback.EVENT.invoker().onExecuteUpdate(
                        level, blockState, blockPos, block, blockPos2, bl
                );
        if (result != InteractionResult.PASS) {
            ci.cancel();
        }
    }

}
