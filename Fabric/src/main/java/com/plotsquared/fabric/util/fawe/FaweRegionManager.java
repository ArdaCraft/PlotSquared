package com.plotsquared.fabric.util.fawe;

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.generator.HybridPlotManager;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.fabric.util.FabricRegionManager;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class FaweRegionManager extends FabricRegionManager {

    private final FaweDelegateRegionManager delegate = new FaweDelegateRegionManager();

    @Inject
    public FaweRegionManager(WorldUtil worldUtil, GlobalBlockQueue blockQueue, ProgressSubscriberFactory subscriberFactory) {
        super(worldUtil, blockQueue, subscriberFactory);
    }

    @Override
    public boolean setCuboids(
            final @NonNull PlotArea area,
            final @NonNull Set<CuboidRegion> regions,
            final @NonNull Pattern blocks,
            int minY,
            int maxY,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
        return delegate.setCuboids(
                area, regions, blocks, minY, maxY,
                Objects.requireNonNullElseGet(queue, area::getQueue).getCompleteTask()
        );
    }

    @Override
    public boolean notifyClear(PlotManager manager) {
        if (!Settings.FAWE_Components.CLEAR || !(manager instanceof HybridPlotManager)) {
            return false;
        }
        return delegate.notifyClear(manager);
    }

    @Override
    public boolean handleClear(
            @NonNull Plot plot,
            @Nullable Runnable whenDone,
            @NonNull PlotManager manager,
            final @Nullable PlotPlayer<?> player
    ) {
        if (!Settings.FAWE_Components.CLEAR || !(manager instanceof HybridPlotManager)) {
            return false;
        }
        return delegate.handleClear(plot, whenDone, manager);
    }

    @Override
    public void swap(
            Location pos1,
            Location pos2,
            Location swapPos,
            final @Nullable PlotPlayer<?> player,
            final Runnable whenDone
    ) {
        delegate.swap(pos1, pos2, swapPos, whenDone);
    }

    @Override
    public void setBiome(CuboidRegion region, int extendBiome, BiomeType biome, PlotArea area, Runnable whenDone) {
        delegate.setBiome(region, extendBiome, biome, area.getWorldName(), whenDone);
    }

    @Override
    public boolean copyRegion(
            final @NonNull Location pos1,
            final @NonNull Location pos2,
            final @NonNull Location pos3,
            final @Nullable PlotPlayer<?> player,
            final @NonNull Runnable whenDone
    ) {
        return delegate.copyRegion(pos1, pos2, pos3, whenDone);
    }

    @Override
    public boolean regenerateRegion(final @NotNull Location pos1, final @NotNull Location pos2, boolean ignore, final Runnable whenDone) {
        return delegate.regenerateRegion(pos1, pos2, ignore, whenDone);
    }

}
