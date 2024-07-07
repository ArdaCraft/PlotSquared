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

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.DisablePhysicsFlag;
import com.plotsquared.core.plot.flag.implementations.RedstoneFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.PlotFlagUtil;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.plotsquared.fabric.listener.event.OnExecuteUpdateCallback;
import com.plotsquared.fabric.player.FabricPlayer;
import com.plotsquared.fabric.util.FabricUtil;
import com.sk89q.worldedit.WorldEdit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class HighFreqBlockEventListener {

    private static final Set<Item> PISTONS = Set.of(
            Items.PISTON,
            Items.STICKY_PISTON
    );
    private static final Set<Item> PHYSICS_BLOCKS = Set.of(
            Items.TURTLE_EGG,
            Items.TURTLE_SPAWN_EGG
    );

    private final PlotAreaManager plotAreaManager;
    private final WorldEdit worldEdit;

    @Inject
    public HighFreqBlockEventListener(final @NonNull PlotAreaManager plotAreaManager, final @NonNull WorldEdit worldEdit) {
        this.plotAreaManager = plotAreaManager;
        this.worldEdit = worldEdit;
        OnExecuteUpdateCallback.EVENT.register(this::onRedstoneEvent);
    }

    public static void sendBlockChange(GlobalPos bloc, final BlockState data) {
        TaskManager.runTaskLater(() -> {
            String world = bloc.dimension().location().getPath();
            int x = bloc.pos().getX();
            int z = bloc.pos().getZ();
            int distance =
                    FabricUtil.getWorld(bloc.dimension().location().getPath()).getServer().getPlayerList().getViewDistance() * 16;

            for (final PlotPlayer<?> player : PlotSquared.platform().playerManager().getPlayers()) {
                Location location = player.getLocation();
                if (location.getWorldName().equals(world)) {
                    if (16 * Math.abs(location.getX() - x) / 16 > distance || 16 * Math.abs(location.getZ() - z) / 16 > distance) {
                        continue;
                    }
                    ((FabricPlayer) player).player.serverLevel().blockUpdated(bloc.pos(), data.getBlock());
                }
            }
        }, TaskTime.ticks(3L));
    }

    public InteractionResult onRedstoneEvent(
            Level level,
            BlockState blockState,
            BlockPos blockPos,
            Block block,
            BlockPos blockPos2,
            boolean bl
    ) {
        if (block instanceof RedStoneWireBlock) {
            Location location = FabricUtil.adapt(GlobalPos.of(level.dimension(), blockPos));
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return InteractionResult.PASS;
            }
            Plot plot = location.getOwnedPlot();
            if (plot == null) {
                if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, RedstoneFlag.class, false)) {
                    blockState.setValue(RedStoneWireBlock.POWER, 0);
                }
                return InteractionResult.PASS;
            }
            if (!plot.getFlag(RedstoneFlag.class)) {
                blockState.setValue(RedStoneWireBlock.POWER, 0);
                plot.debug("Redstone event was cancelled because redstone = false");
                return InteractionResult.FAIL;
            }
            if (Settings.Redstone.DISABLE_OFFLINE) {
                boolean disable = false;
                if (!DBFunc.SERVER.equals(plot.getOwner())) {
                    if (plot.isMerged()) {
                        disable = true;
                        for (UUID owner : plot.getOwners()) {
                            if (PlotSquared.platform().playerManager().getPlayerIfExists(owner) != null) {
                                disable = false;
                                break;
                            }
                        }
                    } else {
                        disable = PlotSquared.platform().playerManager().getPlayerIfExists(plot.getOwnerAbs()) == null;
                    }
                }
                if (disable) {
                    for (UUID trusted : plot.getTrusted()) {
                        if (PlotSquared.platform().playerManager().getPlayerIfExists(trusted) != null) {
                            disable = false;
                            break;
                        }
                    }
                    if (disable) {
                        blockState.setValue(RedStoneWireBlock.POWER, 0);
                        plot.debug("Redstone event was cancelled because no trusted player was in the plot");
                        return InteractionResult.FAIL;
                    }
                }
            }
            if (Settings.Redstone.DISABLE_UNOCCUPIED) {
                for (final PlotPlayer<?> player : PlotSquared.platform().playerManager().getPlayers()) {
                    if (plot.equals(player.getCurrentPlot())) {
                        return InteractionResult.PASS;
                    }
                }
                blockState.setValue(RedStoneWireBlock.POWER, 0);
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }


    public InteractionResult onPhysicsEvent(
            Level level,
            BlockState blockState,
            BlockPos blockPos,
            Block block,
            BlockPos blockPos2,
            boolean bl
    ) {
        Location location = FabricUtil.adapt(GlobalPos.of(level.dimension(), blockPos));
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return InteractionResult.PASS;
        }
        Plot plot = area.getOwnedPlotAbs(location);
        if (plot == null) {
            return InteractionResult.FAIL;
        }
        if (blockState.getBlock() instanceof FallingBlock && plot.getFlag(DisablePhysicsFlag.class)) {
            sendBlockChange(GlobalPos.of(level.dimension(), blockPos), blockState);
            plot.debug("Prevented block physics and resent block change because disable-physics = true");
            return InteractionResult.FAIL;
        }
        if (blockState.getBlock() instanceof ComparatorBlock) {
            if (!plot.getFlag(RedstoneFlag.class)) {
                plot.debug("Prevented comparator update because redstone = false");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
        if (PHYSICS_BLOCKS.contains(blockState.getBlock().asItem())) {
            if (plot.getFlag(DisablePhysicsFlag.class)) {
                plot.debug("Prevented block physics because disable-physics = true");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
        if (Settings.Redstone.DETECT_INVALID_EDGE_PISTONS) {
            if (PISTONS.contains(blockState.getBlock().asItem())) {
                DirectionalBlock piston = (DirectionalBlock) blockState.getBlock();
                final Direction facing = blockState.getValue(PistonBaseBlock.FACING);
                location = location.add(facing.getStepX(), facing.getStepY(), facing.getStepZ());
                Plot newPlot = area.getOwnedPlotAbs(location);
                if (plot.equals(newPlot)) {
                    return InteractionResult.FAIL;
                }
                if (!plot.isMerged() || !plot.getConnectedPlots().contains(newPlot)) {
                    plot.debug("Prevented piston update because of invalid edge piston detection");
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.PASS;
    }

}
