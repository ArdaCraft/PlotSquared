package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.ConfiguredFeaturePlaceEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConfiguredFeature.class)
public class ConfiguredFeatureMixin {

    @Inject(method = "place", at = @At("RETURN"), cancellable = true)
    public void onPlace(
            WorldGenLevel worldGenLevel,
            ChunkGenerator chunkGenerator,
            RandomSource randomSource,
            BlockPos blockPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        InteractionResult result = ConfiguredFeaturePlaceEvent.EVENT.invoker().configuredFeaturePlaceEvent(worldGenLevel,
                chunkGenerator, randomSource, blockPos, (ConfiguredFeature) (Object) this
        );
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

}
