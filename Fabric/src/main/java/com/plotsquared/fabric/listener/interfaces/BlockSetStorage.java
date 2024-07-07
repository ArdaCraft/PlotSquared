package com.plotsquared.fabric.listener.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public interface BlockSetStorage {
    boolean settingComplete = false;

    default void plotSquared$addBlockSet(BlockPos pos, BlockState blockState) {}

    default void plotSquared$addBlockNumber(BlockPos pos, int i) {}
    default Map<BlockPos, BlockState> plotSquared$getBlocksSet() {
        return new HashMap<>();
    }

    default Map<BlockPos, Integer> plotSquared$getBlockNumbers() {
        return new HashMap<>();
    }
    default void clearBlocksSet() {
    }
}
