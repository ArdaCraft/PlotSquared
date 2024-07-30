package com.plotsquared.fabric.listener.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.axiom.VersionUtils;
import com.moulberry.axiom.hooks.ServerLevelExt;
import com.moulberry.axiom.packets.AxiomServerboundPacket;
import com.moulberry.axiom.packets.AxiomServerboundSetBuffer;
import com.moulberry.axiom.world_modification.BiomeBuffer;
import com.moulberry.axiom.world_modification.BlockBuffer;
import com.moulberry.axiom.world_modification.BlockOrBiomeBuffer;
import com.moulberry.axiom.world_modification.CompressedBlockEntity;
import com.plotsquared.fabric.FabricPlatform;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Mixin(AxiomServerboundSetBuffer.class)
public class AxiomServerboundSetBufferPacketMixin {


    @Shadow
    @Final
    private ResourceKey<Level> world;

    @Shadow
    @Final
    private BlockOrBiomeBuffer buffer;

    /**
     * @author JayemCeekay
     */
    @Overwrite
    public void handle(MinecraftServer server, ServerPlayer player) {
        if (!AxiomServerboundPacket.isMissingPermission(server, player)) {
            ServerLevel level = server.getLevel(this.world);
            if (level != null) {
                BlockOrBiomeBuffer var6 = this.buffer;
                if (var6 instanceof BlockBuffer) {
                    BlockBuffer blockBuffer = (BlockBuffer)var6;
                    applyBlockBufferServer(blockBuffer, level, player);
                } else {
                    var6 = this.buffer;
                    if (!(var6 instanceof BiomeBuffer)) {
                        throw new RuntimeException("Unknown buffer type: " + this.buffer.getClass());
                    }
                    BiomeBuffer biomeBuffer = (BiomeBuffer)var6;
                    applyBiomeBufferServer(biomeBuffer, level, player);
                }

            }
        }
    }

    private static void applyBiomeBufferServer(BiomeBuffer biomeBuffer, ServerLevel world, ServerPlayer player) {
        Set<LevelChunk> changedChunks = new HashSet();
        int minSection = world.getMinSection();
        int maxSection = world.getMaxSection();
        Optional<Registry<Biome>> registryOptional = world.registryAccess().registry(Registries.BIOME);
        if (!registryOptional.isEmpty()) {
            Registry<Biome> registry = (Registry)registryOptional.get();
            biomeBuffer.forEachEntry((x, y, z, biome) -> {
                int cy = y >> 2;
                if (cy >= minSection && cy < maxSection) {
                    LevelChunk chunk = (LevelChunk)world.getChunk(x >> 2, z >> 2, ChunkStatus.FULL, false);
                    if (chunk != null) {
                        LevelChunkSection section = chunk.getSection(cy - minSection);
                        PalettedContainer<Holder<Biome>> container = (PalettedContainer)section.getBiomes();
                        Optional<Holder.Reference<Biome>> holder = registry.getHolder(biome);
                        if (holder.isPresent()) {
                            container.set(x & 3, y & 3, z & 3, (Holder)holder.get());
                            changedChunks.add(chunk);
                        }

                    }
                }
            });
            ChunkMap chunkMap = world.getChunkSource().chunkMap;
            HashMap<ServerPlayer, List<LevelChunk>> map = new HashMap();
            Iterator var9 = changedChunks.iterator();

            while(var9.hasNext()) {
                LevelChunk chunk = (LevelChunk)var9.next();
                chunk.setUnsaved(true);
                ChunkPos chunkPos = chunk.getPos();
                Iterator var12 = chunkMap.getPlayers(chunkPos, false).iterator();

                while(var12.hasNext()) {
                    ServerPlayer serverPlayer2 = (ServerPlayer)var12.next();
                    ((List)map.computeIfAbsent(serverPlayer2, (serverPlayer) -> {
                        return new ArrayList();
                    })).add(chunk);
                }
            }

            map.forEach((serverPlayer, list) -> {
                serverPlayer.connection.send(ClientboundChunksBiomesPacket.forChunks(list));
            });
        }
    }


    private static void applyBlockBufferServer(BlockBuffer buffer, ServerLevel world, ServerPlayer player) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        BlockState emptyState = BlockBuffer.EMPTY_STATE;
        boolean hasStarlight = FabricLoader.getInstance().isModLoaded("starlight");
        ObjectIterator var5 = buffer.entrySet().iterator();

        while (true) {
            Long2ObjectMap.Entry entry;
            int cx;
            int cy;
            int cz;
            PalettedContainer container;
            do {
                do {
                    if (!var5.hasNext()) {
                        return;
                    }

                    entry = (Long2ObjectMap.Entry) var5.next();
                    cx = BlockPos.getX(entry.getLongKey());
                    cy = BlockPos.getY(entry.getLongKey());
                    cz = BlockPos.getZ(entry.getLongKey());
                    container = (PalettedContainer) entry.getValue();
                } while (cy < world.getMinSection());
            } while (cy >= world.getMaxSection());

            LevelChunk chunk = world.getChunk(cx, cz);
            LevelChunkSection section = chunk.getSection(world.getSectionIndexFromSectionY(cy));
            PalettedContainer<BlockState> sectionStates = section.getStates();
            boolean hasOnlyAir = section.hasOnlyAir();
            Heightmap worldSurface = null;
            Heightmap oceanFloor = null;
            Heightmap motionBlocking = null;
            Heightmap motionBlockingNoLeaves = null;
            Iterator var19 = chunk.getHeightmaps().iterator();

            while (var19.hasNext()) {
                Map.Entry<Heightmap.Types, Heightmap> heightmap = (Map.Entry) var19.next();
                switch (heightmap.getKey()) {
                    case WORLD_SURFACE:
                        worldSurface = heightmap.getValue();
                        break;
                    case OCEAN_FLOOR:
                        oceanFloor = heightmap.getValue();
                        break;
                    case MOTION_BLOCKING:
                        motionBlocking = (Heightmap) heightmap.getValue();
                        break;
                    case MOTION_BLOCKING_NO_LEAVES:
                        motionBlockingNoLeaves = heightmap.getValue();
                }
            }

            short[] lightUpdates = hasStarlight ? null : ((ServerLevelExt) world).axiom$getPendingLightUpdates(cx, cy, cz);
            boolean sectionChanged = false;
            boolean relightStarlight = false;
            boolean containerMaybeHasPoi = container.maybeHas(o -> PoiTypes.hasPoi((BlockState) o));
            boolean sectionMaybeHasPoi = section.maybeHas(PoiTypes::hasPoi);
            Short2ObjectMap<CompressedBlockEntity> blockEntityChunkMap = buffer.getBlockEntityChunkMap(entry.getLongKey());

            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 16; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        BlockState blockState = (BlockState) container.get(x, y, z);

                        if (blockState != emptyState) {
                            int bx = cx * 16 + x;
                            int by = cy * 16 + y;
                            int bz = cz * 16 + z;
                            InteractionResult result = FabricPlatform.PLATFORM.blockEventListener.blockCreateAxiom(player,
                                    world, new BlockPos(bx, by, bz
                                    ),
                                  blockState, null
                            );
                            if (result != InteractionResult.PASS) {
                                continue;
                            }
                            if (!hasOnlyAir || !blockState.isAir()) {
                                Block block = blockState.getBlock();
                                BlockState old = section.setBlockState(x, y, z, blockState, true);
                                if (blockState != old) {
                                    sectionChanged = true;
                                    blockPos.set(bx, by, bz);
                                    motionBlocking.update(x, by, z, blockState);
                                    motionBlockingNoLeaves.update(x, by, z, blockState);
                                    oceanFloor.update(x, by, z, blockState);
                                    worldSurface.update(x, by, z, blockState);
                                    if (LightEngine.hasDifferentLightProperties(chunk, blockPos, old, blockState)) {
                                        if (hasStarlight) {
                                            relightStarlight = true;
                                        } else {
                                            chunk.getSkyLightSources().update(chunk, x, by, z);
                                            lightUpdates[y + z * 16] = (short) (lightUpdates[y + z * 16] | 1 << x);
                                        }
                                    }

                                    Optional<Holder<PoiType>> newPoi = containerMaybeHasPoi
                                            ? PoiTypes.forState(blockState)
                                            : Optional.empty();
                                    Optional<Holder<PoiType>> oldPoi = sectionMaybeHasPoi
                                            ? PoiTypes.forState(old)
                                            : Optional.empty();
                                    if (!Objects.equals(oldPoi, newPoi)) {
                                        if (oldPoi.isPresent()) {
                                            world.getPoiManager().remove(blockPos);
                                        }

                                        if (newPoi.isPresent()) {
                                            world.getPoiManager().add(blockPos, (Holder) newPoi.get());
                                        }
                                    }
                                }

                                if (blockState.hasBlockEntity()) {
                                    blockPos.set(bx, by, bz);
                                    BlockEntity blockEntity = chunk.getBlockEntity(blockPos, LevelChunk.EntityCreationType.CHECK);
                                    if (blockEntity == null) {
                                        blockEntity = ((EntityBlock) block).newBlockEntity(blockPos, blockState);
                                        if (blockEntity != null) {
                                            chunk.addAndRegisterBlockEntity(blockEntity);
                                        }
                                    } else if (blockEntity.getType().isValid(blockState)) {
                                        blockEntity.setBlockState(blockState);
                                        chunk.updateBlockEntityTicker(blockEntity);
                                    } else {
                                        chunk.removeBlockEntity(blockPos);
                                        blockEntity = ((EntityBlock) block).newBlockEntity(blockPos, blockState);
                                        if (blockEntity != null) {
                                            chunk.addAndRegisterBlockEntity(blockEntity);
                                        }
                                    }

                                    if (blockEntity != null && blockEntityChunkMap != null) {
                                        int key = x | y << 4 | z << 8;
                                        CompressedBlockEntity savedBlockEntity = (CompressedBlockEntity) blockEntityChunkMap.get((short) key);
                                        if (savedBlockEntity != null) {
                                            VersionUtils.helperLoadBlockEntity(
                                                    blockEntity,
                                                    savedBlockEntity.decompress(),
                                                    world.registryAccess()
                                            );
                                            sectionChanged = true;
                                        }
                                    }
                                } else if (old.hasBlockEntity()) {
                                    chunk.removeBlockEntity(blockPos);
                                }
                            }
                        }
                    }
                }
            }

            boolean nowHasOnlyAir = section.hasOnlyAir();
            if (hasOnlyAir != nowHasOnlyAir) {
                world.getChunkSource().getLightEngine().updateSectionStatus(SectionPos.of(cx, cy, cz), nowHasOnlyAir);
            }

            if (sectionChanged) {
                ((ServerLevelExt) world).axiom$markChunkDirty(cx, cz);
                chunk.setUnsaved(true);
            }

            if (relightStarlight) {
                ((ServerLevelExt) world).axiom$relightChunkStarlight(cx, cz);
            }
        }
    }

}
