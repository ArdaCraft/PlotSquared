package com.plotsquared.fabric.listener.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.plotsquared.fabric.listener.event.BaseFireBlockOnPlaceCallback;
import com.plotsquared.fabric.listener.event.EntityHandleInsidePortalCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {

    @WrapOperation(method = "onPlace", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/portal/PortalShape;findEmptyPortalShape" +
                    "(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;" +
                    ")Ljava/util/Optional;"))
    private Optional<PortalShape> InjectToFire(
            LevelAccessor levelAccessor,
            BlockPos blockPos,
            Direction.Axis axis,
            Operation<Optional<PortalShape>> original
    ) {
        InteractionResult result =
                BaseFireBlockOnPlaceCallback.EVENT.invoker().baseFireBlockOnPlaceCallback(
                        levelAccessor, blockPos, axis, original);
        if (result != InteractionResult.PASS) {
            return Optional.empty();
        }
        return original.call(levelAccessor, blockPos, axis);
    }

}
