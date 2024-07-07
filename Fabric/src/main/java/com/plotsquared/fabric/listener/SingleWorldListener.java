package com.plotsquared.fabric.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.util.ReflectionUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import java.lang.reflect.Method;

import static com.plotsquared.core.util.ReflectionUtils.getRefClass;

public class SingleWorldListener {

    public SingleWorldListener() throws Exception {
        //ServerChunkEvents.CHUNK_LOAD.register(this::handle);
    }

    public void markChunkAsClean(LevelChunk chunk) {
       chunk.setFullStatus(() -> FullChunkStatus.FULL);
    }

    private void handle(ServerLevel serverLevel, LevelChunk chunk) {
        String name = serverLevel.dimension().location().getPath();
        PlotAreaManager man = PlotSquared.get().getPlotAreaManager();
        if (!(man instanceof SinglePlotAreaManager)) {
            return;
        }
        if (!SinglePlotArea.isSinglePlotWorld(name)) {
            return;
        }
        int x = chunk.getPos().x;
        int z = chunk.getPos().z;
        if (x < 16 && x > -16 && z < 16 && z > -16) {
            // Allow spawn to generate
            return;
        }
        markChunkAsClean(chunk);
    }
}
