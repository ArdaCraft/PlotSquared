package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.interfaces.BlockSetStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(Feature.class)
public class FeatureMixin implements BlockSetStorage {


    @Unique
    private final Map<BlockPos, Integer> blockNumbers = new HashMap<>();

    @Unique
    private final Map<BlockPos, BlockState> blockSetByFeature = new HashMap<>();

    @Override
    public void plotSquared$addBlockNumber(BlockPos blockPos, int i) {
        blockNumbers.put(blockPos, i);
    }

    @Override
    public void plotSquared$addBlockSet(final BlockPos pos, final BlockState blockState) {
        blockSetByFeature.put(pos, blockState);
    }

    @Override
    public Map<BlockPos, BlockState> plotSquared$getBlocksSet() {
        return this.blockSetByFeature;
    }

    @Override
    public void clearBlocksSet(){
        blockSetByFeature.clear();
    }

}
