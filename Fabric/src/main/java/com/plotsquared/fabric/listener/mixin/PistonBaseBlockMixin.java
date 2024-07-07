package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.PistonMoveBlocksEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.nucleoid.stimuli.Stimuli;

import java.util.List;
import java.util.Map;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    @Inject(method = "moveBlocks", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/core/Direction;getOpposite()" +
            "Lnet/minecraft/core/Direction;"), locals =
            LocalCapture.CAPTURE_FAILHARD)
    public void onMoveBlocks(
            Level level,
            BlockPos blockPos,
            Direction direction,
            boolean bl,
            CallbackInfoReturnable<Boolean> cir,
            BlockPos blockPos2,
            PistonStructureResolver pistonStructureResolver,
            Map map,
            List list,
            List list2,
            List list3,
            BlockState[] blockStates
    ) {
        var events = Stimuli.select();

        try (var invokers = events.at(level, blockPos)) {
            var result = invokers.get(PistonMoveBlocksEvent.EVENT).onMoveBlocks(level, blockPos, list, list3, direction,
                    level.getBlockState(blockPos)
            );
            if (result != InteractionResult.PASS) {
                cir.cancel();
            }
        }
    }

}
