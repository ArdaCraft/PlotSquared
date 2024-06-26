package com.plotsquared.fabric.generator;

import com.mojang.serialization.Codec;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.ClassicPlotWorld;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.generator.HybridPlotWorld;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.generator.SingleWorldGenerator;
import com.plotsquared.core.location.ChunkWrapper;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.location.UncheckedWorldLocation;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.queue.ZeroedDelegateScopedQueueCoordinator;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.fabric.FabricPlatform;
import com.plotsquared.fabric.queue.GenChunk;
import com.plotsquared.fabric.queue.LimitedRegionWrapperQueue;
import com.plotsquared.fabric.util.FabricUtil;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.biome.Biomes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricPlotGenerator extends ChunkGenerator implements GeneratorWrapper<ChunkGenerator> {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + FabricPlotGenerator.class.getSimpleName());
    private final PlotAreaManager plotAreaManager;
    private final IndependentPlotGenerator plotGenerator;
    private final ChunkGenerator platformGenerator;
    private final boolean full;
    private final String levelName;
    private final BiomeSource biomeSource;
    private final BlockStatePopulator blockStatePopulator;
    private boolean loaded = false;

    private PlotArea lastPlotArea;
    private int lastChunkX = Integer.MIN_VALUE;
    private int lastChunkZ = Integer.MIN_VALUE;

    public FabricPlotGenerator(
            final @NonNull String name,
            final @NonNull IndependentPlotGenerator generator,
            final @NonNull PlotAreaManager plotAreaManager
    ) {
        super(FabricPlatform.SERVER.overworld().getChunkSource().getGenerator().getBiomeSource());
        this.plotAreaManager = plotAreaManager;
        this.levelName = name;
        this.plotGenerator = generator;
        this.platformGenerator = this;
        this.blockStatePopulator = new BlockStatePopulator(this.plotGenerator);
        this.full = true;
        this.biomeSource = FabricPlatform.SERVER.overworld().getChunkSource().getGenerator().getBiomeSource();
    }

    public FabricPlotGenerator(
            final String world,
            final ChunkGenerator cg,
            final @NonNull PlotAreaManager plotAreaManager
    ) {
        super(cg.getBiomeSource());
        if (cg instanceof FabricPlotGenerator) {
            throw new IllegalStateException("ChunkGenerator: " + cg.getClass().getName() + " is already a FabricPlotGenerator");
        }
        this.plotAreaManager = plotAreaManager;
        this.levelName = world;
        this.full = false;
        this.platformGenerator = cg;
        this.plotGenerator = new DelegatePlotGenerator(cg, world);
        this.blockStatePopulator = new BlockStatePopulator(this.plotGenerator);
        this.biomeSource = cg.getBiomeSource();
    }


    @Override
    public IndependentPlotGenerator getPlotGenerator() {
        return this.plotGenerator;
    }

    @Override
    public ChunkGenerator getPlatformGenerator() {
        return this.platformGenerator;
    }

    @Override
    public void augment(final PlotArea area) {
        FabricAugmentedGenerator.get(FabricUtil.getWorld(area.getWorldName()));
    }

    @Override
    public boolean isFull() {
        return this.full;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(
            final WorldGenRegion worldGenRegion,
            final long l,
            final RandomState randomState,
            final BiomeManager biomeManager,
            final StructureManager structureManager,
            final ChunkAccess chunkAccess,
            final GenerationStep.Carving carving
    ) {
        if (platformGenerator != this) {
            platformGenerator.applyCarvers(worldGenRegion, l, randomState, biomeManager, structureManager, chunkAccess, carving);
        }
    }

    @Override
    public void buildSurface(
            final WorldGenRegion worldGenRegion,
            final StructureManager structureManager,
            final RandomState randomState,
            final ChunkAccess chunkAccess
    ) {

    }

    @Override
    public void spawnOriginalMobs(final WorldGenRegion worldGenRegion) {
    }


    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            final Executor executor,
            final Blender blender,
            final RandomState randomState,
            final StructureManager structureManager,
            final ChunkAccess chunkAccess
    ) {
        return CompletableFuture.completedFuture(chunkAccess);
    }

    public @NonNull LevelChunk generateChunkData(
            @NonNull ServerLevel world, @NonNull Random random, int x, int z
    ) {
        int minY = world.getMinBuildHeight();
        int maxY = world.getMaxBuildHeight();
        GenChunk result = new GenChunk(minY, maxY);
        /*
        if (this.getPlotGenerator() instanceof SingleWorldGenerator) {
            if (result.getChunkData() != null) {
                for (int chunkX = 0; chunkX < 16; chunkX++) {
                    for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                        for (int y = minY; y < maxY; y++) {

                        }
                    }
                }
                return result.getChunkData();
            }
        }*/
        // Set the chunk location
        result.setChunk(new ChunkWrapper(world.serverLevelData.getLevelName(), x, z));
        // Set the result data
        result.setChunkData(new LevelChunk(world, new ChunkPos(x, z)));
        result.result = null;

        // Catch any exceptions (as exceptions usually thrown)
        try {
            // Fill the result data if necessary
            if (this.platformGenerator != this) {
                return this.generateChunkData(world, random, x, z);
            } else {
                generate(BlockVector2.at(x, z), world.serverLevelData.getLevelName(), result, true);
            }
        } catch (Throwable e) {
            LOGGER.error("Error attempting to load world into PlotSquared.", e);
        }
        // Return the result data
        return result.getChunkData();
    }

    private void generate(BlockVector2 loc, String world, ZeroedDelegateScopedQueueCoordinator result, boolean biomes) {
        // Load if improperly loaded
        if (!this.loaded) {
            synchronized (this) {
                PlotSquared.get().loadWorld(world, this);
            }
        }
        // Process the chunk
        if (ChunkManager.preProcessChunk(loc, result)) {
            return;
        }
        PlotArea area = getPlotArea(world, loc.getX(), loc.getZ());
        try {
            this.plotGenerator.generateChunk(result, area, biomes);
        } catch (Throwable e) {
            // Recover from generator error
            LOGGER.error("Error attempting to generate chunk.", e);
        }
        ChunkManager.postProcessChunk(loc, result);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(
            final int i,
            final int j,
            final Heightmap.Types types,
            final LevelHeightAccessor levelHeightAccessor,
            final RandomState randomState
    ) {
        PlotArea area = getPlotArea(levelName, i, j);
        if (area instanceof ClassicPlotWorld cpw) {
            // Default to plot height being the heighest point before decoration (i.e. roads, walls etc.)
            return cpw.PLOT_HEIGHT;
        }
        throw new UnsupportedOperationException("getBaseHeight Not Implemented");
    }

    @Override
    public NoiseColumn getBaseColumn(
            final int i,
            final int j,
            final LevelHeightAccessor levelHeightAccessor,
            final RandomState randomState
    ) {
        return null;
    }

    @Override
    public void addDebugScreenInfo(final List<String> list, final RandomState randomState, final BlockPos blockPos) {

    }

    private synchronized PlotArea getPlotArea(String name, int chunkX, int chunkZ) {
        // Load if improperly loaded
        if (!this.loaded) {
            PlotSquared.get().loadWorld(name, this);
            // Do not set loaded to true as we want to ensure spawn limits are set when "loading" is actually able to be
            // completed properly.
        }
        if (lastPlotArea != null && name.equals(this.levelName) && chunkX == lastChunkX && chunkZ == lastChunkZ) {
            return lastPlotArea;
        }
        BlockVector3 loc = BlockVector3.at(chunkX << 4, 0, chunkZ << 4);
        if (lastPlotArea != null && lastPlotArea.getRegion().contains(loc) && lastPlotArea.getRegion().contains(loc)) {
            return lastPlotArea;
        }
        PlotArea area = UncheckedWorldLocation.at(name, loc).getPlotArea();
        if (area == null) {
            throw new IllegalStateException(String.format(
                    "Cannot generate chunk that does not belong to a plot area. World: %s",
                    name
            ));
        }
        this.lastChunkX = chunkX;
        this.lastChunkZ = chunkZ;
        return this.lastPlotArea = area;
    }

}
