package com.plotsquared.fabric.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricPlotGenerator extends ChunkGenerator implements GeneratorWrapper<ChunkGenerator> {

    public static final Codec<FabricPlotGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(FabricPlotGenerator::getBiomeSource),
                    Codec.INT.fieldOf("sea_level").forGetter(FabricPlotGenerator::getSeaLevel),
                    Codec.INT.fieldOf("world_height").forGetter(FabricPlotGenerator::getGenDepth)
            ).apply(instance, FabricPlotGenerator::new));
    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + FabricPlotGenerator.class.getSimpleName());
    private final PlotAreaManager plotAreaManager;
    private final IndependentPlotGenerator plotGenerator;
    private final ChunkGenerator platformGenerator;
    private final boolean full;
    private final String levelName;
    private final boolean useNewGenerationMethods;
    private final BiomeSource biomeSource;
    private List<BlockPopulator> populators;
    private boolean loaded = false;

    private PlotArea lastPlotArea;
    private int lastChunkX = Integer.MIN_VALUE;
    private int lastChunkZ = Integer.MIN_VALUE;

    public FabricPlotGenerator(final @NonNull String name,
                               final @NonNull IndependentPlotGenerator generator,
                               final @NonNull PlotAreaManager plotAreaManager,
                               final @NonNull BiomeSource biomeSource) {
        super(biomeSource);
        this.biomeSource = biomeSource;
        this.plotAreaManager = plotAreaManager;
        this.levelName = name;
        this.plotGenerator = generator;
        this.platformGenerator = this;
        this.full = true;
        this.useNewGenerationMethods = true;
    }

    public FabricPlotGenerator(final String world,
                               final ChunkGenerator cg,
                               final @NonNull PlotAreaManager plotAreaManager,
                               final @NonNull BiomeSource biomeSource) {
        super(biomeSource);
        if(cg instanceof FabricPlotGenerator) {
            throw new IllegalStateException("ChunkGenerator: " + cg.getClass().getName() + " is already a FabricPlotGenerator");
        }
        this.plotAreaManager = plotAreaManager;
        this.levelName = world;
        this.full = false;
        this.platformGenerator = cg;
        this.plotGenerator = new DelegatePlotGenerator(cg, world);
        this.useNewGenerationMethods = true;
        this.biomeSource = biomeSource;
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
        return null;
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
        return 0;
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



}
