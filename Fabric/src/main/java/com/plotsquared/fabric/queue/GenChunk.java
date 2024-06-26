/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.fabric.queue;

import com.google.common.base.Preconditions;
import com.intellectualsites.annotations.DoNotUse;
import com.plotsquared.core.location.ChunkWrapper;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.queue.ZeroedDelegateScopedQueueCoordinator;
import com.plotsquared.core.util.ChunkUtil;
import com.plotsquared.core.util.PatternUtil;
import com.plotsquared.fabric.FabricPlatform;
import com.plotsquared.fabric.util.FabricBlockUtil;
import com.plotsquared.fabric.util.FabricUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.fabric.FabricWorldEdit;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Internal use only. Subject to changes at any time.
 */
@DoNotUse
public class GenChunk extends ZeroedDelegateScopedQueueCoordinator {

    public final Biome[] biomes;
    public BlockState[][] result;
    public LevelChunk chunk;
    public String world;
    public int chunkX;
    public int chunkZ;
    private LevelChunk chunkData = null;

    /**
     * @param minY minimum world Y, inclusive
     * @param maxY maximum world Y, inclusive
     * @since 6.6.0
     */
    public GenChunk(int minY, int maxY) {
        super(null, Location.at("", 0, minY, 0), Location.at("", 15, maxY, 15));
        this.biomes = FabricPlatform.SERVER.registryAccess().registry(Registries.BIOME).get().stream().toList().toArray(new Biome[0]);
    }

    public @Nullable LevelChunk getChunkData() {
        return this.chunkData;
    }

    /**
     * Set the internal Bukkit chunk data
     *
     * @param chunkData Bukkit ChunkData
     */
    public void setChunkData(@NonNull LevelChunk chunkData) {
        this.chunkData = chunkData;
    }

    public @NonNull LevelChunk getChunk() {
        if (chunk == null) {
            ServerLevel worldObj = FabricUtil.getWorld(world);
            if (worldObj != null) {
                this.chunk = worldObj.getChunk(chunkX, chunkZ);
            }
        }
        return chunk;
    }

    /**
     * Set the chunk being represented
     *
     * @param chunk Bukkit Chunk
     */
    public void setChunk(@NonNull LevelChunk chunk) {
        this.chunk = chunk;
    }


    /**
     * Set the world and XZ of the chunk being represented via {@link ChunkWrapper}
     *
     * @param wrap PlotSquared ChunkWrapper
     */
    public void setChunk(@NonNull ChunkWrapper wrap) {
        chunk = null;
        world = wrap.world();
        chunkX = wrap.x();
        chunkZ = wrap.z();
    }

    @Override
    public void fillBiome(@NonNull BiomeType biomeType) {
        for (int y = getMin().getY(); y <= getMax().getY(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    FabricUtil.setBiome(world, new CuboidRegion(getMin().getBlockVector3(), getMax().getBlockVector3()), biomeType);
                }
            }
        }
    }

    @Override
    public void setCuboid(@NonNull Location pos1, @NonNull Location pos2, @NonNull BlockState block) {
        if (result != null && pos1.getX() == 0 && pos1.getZ() == 0 && pos2.getX() == 15 && pos2.getZ() == 15) {
            for (int y = pos1.getY(); y <= pos2.getY(); y++) {
                int layer = getLayerIndex(y);
                BlockState[] data = result[layer];
                if (data == null) {
                    result[layer] = data = new BlockState[4096];
                }
                int start = y << 8;
                int end = start + 256;
                Arrays.fill(data, start, end, block);
            }
        }
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        for(int i = minX; i <= maxX +1; i++) {
            for(int j = minZ; j <= maxZ + 1; j++) {
                for(int k = minY; k <= maxY + 1 ; k++) {
                    chunkData.setBlockState(new BlockPos(i, k, j), FabricAdapter.adapt(block), true);
                }
            }
        }
    }

    @Override
    public boolean setBiome(int x, int z, @NonNull BiomeType biomeType) {
        setBiome(x, z, FabricAdapter.adapt(biomeType));
        return true;
    }

    /**
     * Set the in the whole column of XZ
     *
     * @param x     Relative x location within the chunk (0 - 15)
     * @param z     Relative z location within the chunk (0 - 15)
     * @param biome Bukkit biome to set
     */
    public void setBiome(int x, int z, @NonNull Biome biome) {
            for (int y = getMin().getY(); y <= getMax().getY(); y++) {
                this.setBiome(x, y, z, biome);
            }
    }

    public void setBiome(int x, int y, int z, @NonNull Biome biome) {
       getWorld().setBiome(x,y,z, FabricAdapter.adapt(biome));
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull Pattern pattern) {
        final BaseBlock block = PatternUtil.apply(Preconditions.checkNotNull(
                pattern,
                "Pattern may not be null"
        ), x + (chunkX << 4), y, z + (chunkZ << 4));
        return setBlock(x, y, z, block);
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BlockState id) {
        if (this.result == null) {
            this.chunkData.setBlockState(new BlockPos(x, y, z), FabricAdapter.adapt(id), true);
            return true;
        }
        this.chunkData.setBlockState(new BlockPos(x, y, z), FabricAdapter.adapt(id), true);
        this.storeCache(x, y, z, id);
        return true;
    }

    private void storeCache(final int x, final int y, final int z, final @NonNull BlockState id) {
        int i = getLayerIndex(y);
        BlockState[] v = this.result[i];
        if (v == null) {
            this.result[i] = v = new BlockState[4096];
        }
        int j = ChunkUtil.getJ(x, y, z);
        v[j] = id;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BaseBlock id) {
        if (this.result == null) {
            this.chunkData.setBlockState(new BlockPos(x, y, z), FabricAdapter.adapt(id.toBlockState()), true);
            return true;
        }
        this.chunkData.setBlockState(new BlockPos(x, y, z), FabricAdapter.adapt(id.toBlockState()), true);
        this.storeCache(x, y, z, id.toImmutableState());
        return true;
    }

    @Override
    public @Nullable BlockState getBlock(int x, int y, int z) {
        int i = getLayerIndex(y);
        if (result == null) {
            return FabricBlockUtil.get(chunkData.getBlockState(new BlockPos(x,y,z)).getBlock());
        }
        BlockState[] array = result[i];
        if (array == null) {
            return BlockTypes.AIR.getDefaultState();
        }
        int j = ChunkUtil.getJ(x, y, z);
        return array[j];
    }

    public int getX() {
        return chunk == null ? chunkX : chunk.getPos().x;
    }

    public int getZ() {
        return chunk == null ? chunkZ : chunk.getPos().z;
    }

    @Override
    public com.sk89q.worldedit.world.@NonNull World getWorld() {
        return chunk == null ? FabricAdapter.adapt(FabricUtil.getWorld(world)) :
                FabricAdapter.adapt(chunk.getLevel().getServer().getLevel(chunk.getLevel().dimension()));
    }

    @Override
    public @NonNull Location getMax() {
        return Location.at(getWorld().getName(), 15 + (getX() << 4), super.getMax().getY(), 15 + (getZ() << 4));
    }

    @Override
    public @NonNull Location getMin() {
        return Location.at(getWorld().getName(), getX() << 4, super.getMin().getY(), getZ() << 4);
    }

    public @NonNull GenChunk clone() {
        GenChunk toReturn = new GenChunk(getMin().getY(), getMax().getY());
        if (this.result != null) {
            for (int i = 0; i < this.result.length; i++) {
                BlockState[] matrix = this.result[i];
                if (matrix != null) {
                    toReturn.result[i] = new BlockState[matrix.length];
                    System.arraycopy(matrix, 0, toReturn.result[i], 0, matrix.length);
                }
            }
        }
        toReturn.chunkData = this.chunkData;
        return toReturn;
    }

    private int getLayerIndex(int y) {
        return (y - getMin().getY()) >> 4;
    }

}
