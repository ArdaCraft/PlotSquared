package com.plotsquared.fabric.util;

import com.plotsquared.core.util.ChunkManager;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.util.concurrent.CompletableFuture;

public class FabricChunkManager extends ChunkManager {

    public static boolean isIn(CuboidRegion region, int x, int z) {
        return x >= region.getMinimumPoint().getX() && x <= region.getMaximumPoint().getX() && z >= region
                .getMinimumPoint()
                .getZ() && z <= region
                .getMaximumPoint().getZ();
    }

    @Override
    public CompletableFuture<?> loadChunk(String world, BlockVector2 chunkLoc, boolean force) {
        return PaperLib.getChunkAtAsync(BukkitUtil.getWorld(world), chunkLoc.getX(), chunkLoc.getZ(), force);
    }

}
