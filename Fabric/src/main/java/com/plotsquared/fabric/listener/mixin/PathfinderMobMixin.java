package com.plotsquared.fabric.listener.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.plotsquared.fabric.listener.event.EntityPathfindEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PathfinderMob.class)
public class PathfinderMobMixin {

    @WrapMethod(method = "getWalkTargetValue(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/LevelReader;)F")
    public float onWalkTarget(BlockPos blockPos, LevelReader levelReader, Operation<Float> original) {

        InteractionResult result = EntityPathfindEvent.EVENT.invoker().onEntityPathfind((Entity) (Object) this, blockPos, levelReader);
        if(result != InteractionResult.PASS) {
            return 0;
        }

        return original.call(blockPos, levelReader);
    }

}
