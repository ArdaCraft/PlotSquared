package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.ConcreteHardenFlag;
import com.plotsquared.core.plot.flag.implementations.DisablePhysicsFlag;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.plotsquared.fabric.listener.HighFreqBlockEventListener.sendBlockChange;

@Mixin(ConcretePowderBlock.class)
public class ConcretePowderBlockMixin {

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    public void onUpdateShape(
            BlockState blockState,
            Direction direction,
            BlockState blockState2,
            LevelAccessor levelAccessor,
            BlockPos blockPos,
            BlockPos blockPos2,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Location location = FabricUtil.adapt(GlobalPos.of(((ServerLevel) levelAccessor).dimension(), blockPos));
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if (!plot.getFlag(ConcreteHardenFlag.class)) {
                sendBlockChange(GlobalPos.of(((ServerLevel) levelAccessor).dimension(), blockPos), blockState);
                plot.debug("Prevented block physics and resent block change because disable-physics = true");
                cir.setReturnValue(blockState);
            }
        }
    }

    @Inject(method = "shouldSolidify", at = @At("HEAD"), cancellable = true)
    private static void onShouldSolidify(
            BlockGetter blockGetter,
            BlockPos blockPos,
            BlockState blockState,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Location location = FabricUtil.adapt(GlobalPos.of(((ServerLevel) blockGetter).dimension(), blockPos));
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if (!plot.getFlag(ConcreteHardenFlag.class)) {
                sendBlockChange(GlobalPos.of(((ServerLevel) blockGetter).dimension(), blockPos), blockState);
                plot.debug("Prevented block physics and resent block change because disable-physics = true");
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "touchesLiquid", at = @At("HEAD"), cancellable = true)
    private static void onTouchesLiquid(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        Location location = FabricUtil.adapt(GlobalPos.of(((ServerLevel) blockGetter).dimension(), blockPos));
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if (!plot.getFlag(ConcreteHardenFlag.class)) {
                sendBlockChange(GlobalPos.of(((ServerLevel) blockGetter).dimension(), blockPos), blockGetter.getBlockState(blockPos));
                plot.debug("Prevented block physics and resent block change because disable-physics = true");
                cir.setReturnValue(false);
            }
        }
    }

}

