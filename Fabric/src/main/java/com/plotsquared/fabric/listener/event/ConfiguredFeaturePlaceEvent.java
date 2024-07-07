package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public interface ConfiguredFeaturePlaceEvent {


    Event<ConfiguredFeaturePlaceEvent> EVENT = EventFactory.createArrayBacked(
            ConfiguredFeaturePlaceEvent.class,
            callbacks -> (worldGenLevel, chunkGenerator, randomSource, blockPos, configuredFeature) -> {
                for (ConfiguredFeaturePlaceEvent callback : callbacks) {
                    InteractionResult result = callback.configuredFeaturePlaceEvent(worldGenLevel, chunkGenerator, randomSource
                            , blockPos, configuredFeature);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult configuredFeaturePlaceEvent(
            WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos,
            ConfiguredFeature configuredFeature
    );
}
