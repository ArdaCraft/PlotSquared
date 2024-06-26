package com.plotsquared.fabric.generator;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public abstract class BlockPopulator {
    public BlockPopulator() {
    }

    /** @deprecated */
    @Deprecated
    public void populate(@NotNull ServerLevel world, @NotNull Random random, @NotNull LevelChunk source) {
    }

    public void populate(@NotNull ServerLevel worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                         @NotNull WorldGenRegion limitedRegion) {
    }
}
