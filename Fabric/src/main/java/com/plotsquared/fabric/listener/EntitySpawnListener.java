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
package com.plotsquared.fabric.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.fabric.data.PlotSquaredDataAttachments;
import com.plotsquared.fabric.listener.event.HandleMoveVehicleCallback;
import com.plotsquared.fabric.util.FabricEntityUtil;
import com.plotsquared.fabric.util.FabricUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.AABB;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.entity.EntitySpawnEvent;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;

import java.util.Set;

public class EntitySpawnListener {
    private static boolean ignoreTP = false;
    private static boolean hasPlotArea = false;
    private static String areaName = null;

    public EntitySpawnListener() {
        Stimuli.global().listen(EntitySpawnEvent.EVENT, entity -> {
            Location location = FabricUtil.adapt(GlobalPos.of(entity.level().dimension(), entity.blockPosition()));
            PlotArea area = location.getPlotArea();
            if (!location.isPlotArea() || area == null) {
                return InteractionResult.PASS;
            }
            /*
            if (PaperLib.isPaper()) {
                //noinspection ConstantValue - getEntitySpawnReason annotated as NotNull, but is not NotNull. lol.
                if (area.isSpawnCustom() && entity.getEntitySpawnReason() != null && "CUSTOM".equals(entity
                        .getEntitySpawnReason()
                        .name())) {
                    return;
                }
            }*/
            Plot plot = location.getOwnedPlotAbs();
            EntityType<?> type = entity.getType();
            if (plot == null) {
                if (type == EntityType.ITEM) {
                    if (Settings.Enabled_Components.KILL_ROAD_ITEMS) {
                        return InteractionResult.FAIL;
                    }
                }
                if (!area.isMobSpawning()) {
                    if (type == EntityType.PLAYER) {
                        return InteractionResult.FAIL;
                    }
                    if (entity.isAlive()) {
                        return InteractionResult.FAIL;
                    }

                }
                if (!area.isMiscSpawnUnowned() && !entity.isAlive()) {
                    return InteractionResult.FAIL;
                }
                return InteractionResult.PASS;
            }
            if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                return InteractionResult.FAIL;
            }
            if (type == EntityType.END_CRYSTAL || type == EntityType.ARMOR_STAND) {
                if (FabricEntityUtil.checkEntity(entity, plot)) {
                    return InteractionResult.FAIL;
                }
                return InteractionResult.PASS;
            }
            if (type == EntityType.SHULKER) {
                if (!entity.hasAttached(PlotSquaredDataAttachments.SHULKER_PLOT)) {
                    entity.setAttached(PlotSquaredDataAttachments.SHULKER_PLOT, plot.getId());
                }
            }
            return InteractionResult.PASS;
        });

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            for (final Entity entity : world.getEntitiesOfClass(
                    Entity.class,
                    new AABB(chunk.getPos().getMinBlockX(), chunk.getMaxBuildHeight(), chunk.getPos().getMinBlockZ(),
                            chunk.getPos().getMaxBlockX(), chunk.getMaxBuildHeight(), chunk.getPos().getMaxBlockZ()
                    ),
                    entity -> true
            )) {
                testCreate(entity);
            }
        });

        Stimuli.global().listen(EntitySpawnEvent.EVENT, entity -> {
            if (entity.getType() == EntityType.ARMOR_STAND) {
                testCreate(entity);
            }
            return InteractionResult.PASS;
        });

        Stimuli.global().listen(EntitySpawnEvent.EVENT, entity -> {
           if(entity.isVehicle()) {
               testCreate(entity);
           }
           return InteractionResult.PASS;
        });


    }

    public static void testNether(final Entity entity) {
        @NonNull ServerLevel world = entity.getServer().getLevel(entity.level().dimension());
        if (!world.dimensionTypeId().equals(BuiltinDimensionTypes.NETHER) && !(world
                .dimensionType()
                .equals(BuiltinDimensionTypes.END))) {
            return;
        }
        test(entity);
    }

    public static void testCreate(final Entity entity) {
        @NonNull ServerLevel world = entity.getServer().getLevel(entity.level().dimension());
        if (!world.serverLevelData.getLevelName().equals(areaName)) {
            areaName = world.serverLevelData.getLevelName();
            hasPlotArea = PlotSquared.get().getPlotAreaManager().hasPlotArea(areaName);
        }
        if (!hasPlotArea) {
            return;
        }
        test(entity);
    }

    public static void test(Entity entity) {
        @NonNull ServerLevel world = entity.getServer().getLevel(entity.level().dimension());
        if (!entity.hasAttached(PlotSquaredDataAttachments.P2)) {
            if (PlotSquared.get().getPlotAreaManager().hasPlotArea(world.serverLevelData.getLevelName())) {
                entity.setAttached(PlotSquaredDataAttachments.P2, GlobalPos.of(world.dimension(), entity.blockPosition()));
            }
        } else {
            GlobalPos origin = entity.getAttached(PlotSquaredDataAttachments.P2);
            ServerLevel originWorld = entity.getServer().getLevel(origin.dimension());
            if (!originWorld.equals(world)) {
                if (!ignoreTP) {
                    if (!world.serverLevelData.getLevelName().equalsIgnoreCase(originWorld + "_the_end")) {
                        if (entity.getType() == EntityType.PLAYER) {
                            return;
                        }
                        try {
                            ignoreTP = true;
                            entity.teleportTo(entity.getServer().getLevel(origin.dimension()), origin.pos().getX(),
                                    origin.pos().getY(), origin.pos().getZ(), Set.of(), 0, 0
                            );
                        } finally {
                            ignoreTP = false;
                        }
                        if (entity.getServer().getLevel(entity.level().dimension()).equals(world)) {
                            entity.remove(Entity.RemovalReason.DISCARDED);
                        }
                    }
                } else {
                    if (entity.getType() == EntityType.PLAYER) {
                        return;
                    }
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    /*
    @EventHandler
    public void onVehicle(VehicleUpdateEvent event) {
        testNether(event.getVehicle());
    }
*/
    @EventHandler
    public void onVehicle(VehicleBlockCollisionEvent event) {
        testNether(event.getVehicle());
    }

    @EventHandler
    public void onTeleport(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        Entity fromLocation = event.getEntity();
        Block toLocation = event.getTo().getBlock();
        final Location fromLocLocation = BukkitUtil.adapt(fromLocation.getLocation());
        final PlotArea fromArea = fromLocLocation.getPlotArea();
        Location toLocLocation = BukkitUtil.adapt(toLocation.getLocation());
        PlotArea toArea = toLocLocation.getPlotArea();

        if (toArea == null) {
            if (fromLocation.getType() == EntityType.SHULKER && fromArea != null) {
                event.setCancelled(true);
            }
            return;
        }
        Plot toPlot = toArea.getOwnedPlot(toLocLocation);
        if (fromLocation.getType() == EntityType.SHULKER && fromArea != null) {
            final Plot fromPlot = fromArea.getOwnedPlot(fromLocLocation);

            if (fromPlot != null || toPlot != null) {
                if ((fromPlot == null || !fromPlot.equals(toPlot)) && (toPlot == null || !toPlot.equals(fromPlot))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (entity instanceof Vehicle || entity instanceof ArmorStand) {
            testNether(event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void vehicleMove(VehicleMoveEvent event) {
        testNether(event.getVehicle());
    }

}
