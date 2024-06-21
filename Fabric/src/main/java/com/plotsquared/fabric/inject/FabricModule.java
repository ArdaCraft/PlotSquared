package com.plotsquared.fabric.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.plotsquared.core.PlotPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.generator.HybridGen;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.inject.annotations.ConsoleActor;
import com.plotsquared.core.inject.annotations.DefaultGenerator;
import com.plotsquared.core.inject.factory.ChunkCoordinatorBuilderFactory;
import com.plotsquared.core.inject.factory.ChunkCoordinatorFactory;
import com.plotsquared.core.inject.factory.HybridPlotWorldFactory;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.DefaultPlotAreaManager;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.queue.ChunkCoordinator;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueProvider;
import com.plotsquared.core.queue.subscriber.DefaultProgressSubscriber;
import com.plotsquared.core.queue.subscriber.ProgressSubscriber;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.fabric.FabricPlatform;
import com.plotsquared.fabric.listener.ServerListener;
import com.plotsquared.fabric.listener.SingleWorldListener;
import com.plotsquared.fabric.player.FabricPlayerManager;
import com.plotsquared.fabric.queue.FabricChunkCoordinator;
import com.plotsquared.fabric.queue.FabricQueueCoordinator;
import com.plotsquared.fabric.schematic.FabricSchematicHandler;
import com.plotsquared.fabric.util.FabricChunkManager;
import com.plotsquared.fabric.util.FabricInventoryUtil;
import com.plotsquared.fabric.util.FabricRegionManager;
import com.plotsquared.fabric.util.FabricSetupUtils;
import com.plotsquared.fabric.util.FabricUtil;
import com.plotsquared.fabric.util.fawe.FaweRegionManager;
import com.plotsquared.fabric.util.fawe.FaweSchematicHandler;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.fabric.FabricWorldEdit;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class FabricModule extends AbstractModule {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + FabricModule.class.getSimpleName());

    private final FabricPlatform fabricPlatform;

    public FabricModule(final @NonNull FabricPlatform fabricPlatform) {
        this.fabricPlatform = fabricPlatform;
    }

    @Override
    protected void configure() {
        bind(PlayerManager.class).to(FabricPlayerManager.class);
        bind(ModInitializer.class).toInstance(fabricPlatform);
        bind(PlotPlatform.class).toInstance(fabricPlatform);
        bind(FabricPlatform.class).toInstance(fabricPlatform);
        bind(IndependentPlotGenerator.class).annotatedWith(DefaultGenerator.class).to(HybridGen.class);
        // Console actor
        @NonNull CommandSourceStack console = FabricPlatform.SERVER.createCommandSourceStack();
        bind(Actor.class).annotatedWith(ConsoleActor.class).toInstance(FabricAdapter.adaptCommandSource(console));
        bind(InventoryUtil.class).to(FabricInventoryUtil.class);
        bind(SetupUtils.class).to(FabricSetupUtils.class);
        bind(WorldUtil.class).to(FabricUtil.class);
        install(new FactoryModuleBuilder()
                .implement(ProgressSubscriber.class, DefaultProgressSubscriber.class)
                .build(ProgressSubscriberFactory.class));
        bind(ChunkManager.class).to(FabricChunkManager.class);
        /*if (PlotSquared.platform().isFaweHooking()) {
            bind(SchematicHandler.class).to(FaweSchematicHandler.class);
            bind(RegionManager.class).to(FaweRegionManager.class);
        } else {*/
            bind(SchematicHandler.class).to(FabricSchematicHandler.class);
            bind(RegionManager.class).to(FabricRegionManager.class);
        //}
        bind(GlobalBlockQueue.class).toInstance(new GlobalBlockQueue(QueueProvider.of(FabricQueueCoordinator.class)));
        if (Settings.Enabled_Components.WORLDS) {
            bind(PlotAreaManager.class).to(SinglePlotAreaManager.class);
            try {
                bind(SingleWorldListener.class).toInstance(new SingleWorldListener());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bind(PlotAreaManager.class).to(DefaultPlotAreaManager.class);
        }
        install(new FactoryModuleBuilder().build(HybridPlotWorldFactory.class));
        install(new FactoryModuleBuilder()
                .implement(ChunkCoordinator.class, FabricChunkCoordinator.class)
                .build(ChunkCoordinatorFactory.class));
        install(new FactoryModuleBuilder().build(ChunkCoordinatorBuilderFactory.class));
    }

    @Provides
    @Singleton
    @NonNull EconHandler provideEconHandler() {
        if (!Settings.Enabled_Components.ECONOMY /*|| !Bukkit.getPluginManager().isPluginEnabled("Vault")*/) {
            return EconHandler.nullEconHandler();
        }
        // Guice eagerly initializes singletons, so we need to bring the laziness ourselves
        return new LazyEconHandler();
    }

    private static final class LazyEconHandler extends EconHandler implements ServerListener.MutableEconHandler {
        private volatile EconHandler implementation;

        public void setImplementation(EconHandler econHandler) {
            this.implementation = econHandler;
        }

        @Override
        public boolean init() {
            return get().init();
        }

        @Override
        public double getBalance(final PlotPlayer<?> player) {
            return get().getBalance(player);
        }

        @Override
        public void withdrawMoney(final PlotPlayer<?> player, final double amount) {
            get().withdrawMoney(player, amount);
        }

        @Override
        public void depositMoney(final PlotPlayer<?> player, final double amount) {
            get().depositMoney(player, amount);
        }

        @Override
        public void depositMoney(final OfflinePlotPlayer player, final double amount) {
            get().depositMoney(player, amount);
        }

        @Override
        public boolean isEnabled(final PlotArea plotArea) {
            return get().isEnabled(plotArea);
        }

        @Override
        public @NonNull String format(final double balance) {
            return get().format(balance);
        }

        @Override
        public boolean isSupported() {
            return get().isSupported();
        }

        private EconHandler get() {
            return Objects.requireNonNull(this.implementation, "EconHandler not ready yet.");
        }

    }

}
