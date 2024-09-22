package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.RedstonePowerUpdateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedStoneWireBlock.class)
public class RedStoneWireBlockMixin {

    @Inject(method = "updatePowerStrength", at = @At("HEAD"), cancellable = true)
    public void onUpdatePowerStrength(
            Level level, BlockPos blockPos, BlockState blockState, CallbackInfo ci
    ) {
        InteractionResult result =
                RedstonePowerUpdateEvent.EVENT.invoker().onUpdatePower(
                        level, blockState, blockPos);
        if (result != InteractionResult.PASS) {
            ci.cancel();
        }
    }

}
