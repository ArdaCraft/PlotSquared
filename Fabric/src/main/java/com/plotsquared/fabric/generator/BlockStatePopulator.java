package com.plotsquared.fabric.generator;

import com.plotsquared.core.generator.HybridPlotWorld;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.location.UncheckedWorldLocation;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.queue.ZeroedDelegateScopedQueueCoordinator;
import com.plotsquared.fabric.queue.LimitedRegionWrapperQueue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Random;

public class BlockStatePopulator extends BlockPopulator {
    private final IndependentPlotGenerator plotGenerator;

    public BlockStatePopulator(
            final @NonNull IndependentPlotGenerator plotGenerator
    ) {
        this.plotGenerator = plotGenerator;
    }

    @Override
    public void populate(
            @NonNull final ServerLevel worldInfo,
            @NonNull final Random random,
            final int chunkX,
            final int chunkZ,
            @NonNull final WorldGenRegion limitedRegion
    ) {
        PlotArea area =
                UncheckedWorldLocation.at(worldInfo.dimension().location().getPath().toString(), chunkX << 4, 0, chunkZ << 4).getPlotArea();
        if (area == null || (area instanceof HybridPlotWorld hpw && !hpw.populationNeeded()) || area instanceof SinglePlotArea) {
            return;
        }
        LimitedRegionWrapperQueue wrapped = new LimitedRegionWrapperQueue(limitedRegion);
        // It is possible for the region to be larger than the chunk, but there is no reason for P2 to need to populate
        // outside of the actual chunk area.
        Location min = UncheckedWorldLocation.at(worldInfo.dimension().location().getPath().toString(), chunkX << 4, worldInfo.getMinBuildHeight(),
                chunkZ << 4);
        Location max = UncheckedWorldLocation.at(
                worldInfo.dimension().location().getPath().toString(),
                (chunkX << 4) + 15,
                worldInfo.getMaxBuildHeight(),
                (chunkZ << 4) + 15
        );
        ZeroedDelegateScopedQueueCoordinator offsetChunkQueue = new ZeroedDelegateScopedQueueCoordinator(wrapped, min, max);
        this.plotGenerator.populateChunk(offsetChunkQueue, area);
    }


}
