package com.plotsquared.fabric.generator;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.AugmentedUtils;
import com.plotsquared.core.queue.QueueCoordinator;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.util.SideEffectSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Random;

public class FabricAugmentedGenerator extends BlockPopulator {

    private static FabricAugmentedGenerator generator;

    public static FabricAugmentedGenerator get(ServerLevel world) {
        if (generator == null) {
            generator = new FabricAugmentedGenerator();
        }
        return generator;
    }

    @Override
    public void populate(@NonNull ServerLevel world, @NonNull Random random, @NonNull LevelChunk source) {
        QueueCoordinator queue = PlotSquared.platform().globalBlockQueue().getNewQueue(FabricAdapter.adapt(world));
        // The chunk is already loaded and we do not want to load the chunk in "fully" by using any PaperLib methods.
        queue.setForceSync(true);
        queue.setSideEffectSet(SideEffectSet.none());
        queue.setBiomesEnabled(false);
        queue.setChunkObject(source);
        AugmentedUtils.generateChunk(world.serverLevelData.getLevelName(), source.getPos().x, source.getPos().z, queue);
        queue.enqueue();
    }

}
