package com.plotsquared.fabric.listener.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.plotsquared.fabric.listener.event.SpongeAbsorbEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpongeBlock;
import org.spongepowered.asm.mixin.Mixin;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin {

    @WrapMethod(method = "method_49829")
    private static boolean onRemoveWater(BlockPos blockPos, Level level, BlockPos blockPos2, Operation<Boolean> original) {
        try (var invokers = Stimuli.select().at(level, blockPos)) {
            var result = invokers.get(SpongeAbsorbEvent.EVENT).onRemoveWater(blockPos, level, blockPos2);
            if (result != InteractionResult.PASS) {
                return false;
            }
        }
        return original.call(blockPos, level, blockPos2);
    }

}
