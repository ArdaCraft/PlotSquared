package com.plotsquared.fabric.util;

import com.plotsquared.core.util.ChunkManager;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FabricChunkManager extends ChunkManager {

    public static boolean isIn(CuboidRegion region, int x, int z) {
        return x >= region.getMinimumPoint().getX() && x <= region.getMaximumPoint().getX() && z >= region
                .getMinimumPoint()
                .getZ() && z <= region
                .getMaximumPoint().getZ();
    }

    @Override
    public CompletableFuture<?> loadChunk(String world, BlockVector2 chunkLoc, boolean force) {
        return FabricUtil.getWorld(world).getChunkSource().getChunkFuture(
                chunkLoc.getX(),
                chunkLoc.getZ(),
                ChunkStatus.FULL,
                force
        );
    }

}
