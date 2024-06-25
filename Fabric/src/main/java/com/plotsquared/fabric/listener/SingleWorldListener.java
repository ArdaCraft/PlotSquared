package com.plotsquared.fabric.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.util.ReflectionUtils;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.Method;

import static com.plotsquared.core.util.ReflectionUtils.getRefClass;

public class SingleWorldListener {

    private final Method methodSetUnsaved;
    private Method methodGetHandleChunk;
    private Object objChunkStatusFull = null;

    public SingleWorldListener() throws Exception {
        ReflectionUtils.RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        ReflectionUtils.RefClass classChunkAccess = getRefClass("net.minecraft.world.level.chunk.IChunkAccess");
        this.methodSetUnsaved = classChunkAccess.getMethod("a", boolean.class).getRealMethod();
        try {
            this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle").getRealMethod();
        } catch (NoSuchMethodException ignored) {
            try {
                ReflectionUtils.RefClass classChunkStatus = getRefClass("net.minecraft.world.level.chunk.ChunkStatus");
                this.objChunkStatusFull = classChunkStatus.getRealClass().getField("n").get(null);
                this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle", classChunkStatus.getRealClass()).getRealMethod();
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void markChunkAsClean(Chunk chunk) {
        try {
            Object nmsChunk = objChunkStatusFull != null
                    ? this.methodGetHandleChunk.invoke(chunk, objChunkStatusFull)
                    : this.methodGetHandleChunk.invoke(chunk);
            methodSetUnsaved.invoke(nmsChunk, false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void handle(ChunkEvent event) {
        ServerLevel world = event.getWorld();
        String name = world.getName();
        PlotAreaManager man = PlotSquared.get().getPlotAreaManager();
        if (!(man instanceof SinglePlotAreaManager)) {
            return;
        }
        if (!SinglePlotArea.isSinglePlotWorld(name)) {
            return;
        }
        int x = event.getChunk().getX();
        int z = event.getChunk().getZ();
        if (x < 16 && x > -16 && z < 16 && z > -16) {
            // Allow spawn to generate
            return;
        }
        markChunkAsClean(event.getChunk());
    }

    //    @EventHandler
    //    public void onPopulate(ChunkPopulateEvent event) {
    //        handle(event);
    //    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        // disable this for now, should address https://github.com/IntellectualSites/PlotSquared/issues/4413
        // handle(event);
    }

}
