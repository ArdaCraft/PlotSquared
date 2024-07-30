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
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.CopperOxideFlag;
import com.plotsquared.core.plot.flag.implementations.MiscInteractFlag;
import com.plotsquared.core.plot.flag.implementations.SculkSensorInteractFlag;
import com.plotsquared.core.util.PlotFlagUtil;
import com.plotsquared.fabric.listener.event.GameEventEvent;
import com.plotsquared.fabric.listener.event.LevelSetBlockEvent;
import com.plotsquared.fabric.player.FabricPlayer;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class BlockEventListener117 {

    private static final Set<Block> COPPER_OXIDIZING = Set.of(
            Blocks.COPPER_BLOCK,
            Blocks.EXPOSED_COPPER,
            Blocks.WEATHERED_COPPER,
            Blocks.OXIDIZED_COPPER,
            Blocks.CUT_COPPER,
            Blocks.EXPOSED_CUT_COPPER,
            Blocks.WEATHERED_CUT_COPPER,
            Blocks.OXIDIZED_CUT_COPPER,
            Blocks.CUT_COPPER_STAIRS,
            Blocks.EXPOSED_CUT_COPPER_STAIRS,
            Blocks.WEATHERED_CUT_COPPER_STAIRS,
            Blocks.OXIDIZED_CUT_COPPER_STAIRS,
            Blocks.CUT_COPPER_SLAB,
            Blocks.EXPOSED_CUT_COPPER_SLAB,
            Blocks.WEATHERED_CUT_COPPER_SLAB,
            Blocks.OXIDIZED_CUT_COPPER_SLAB
    );

    @Inject
    public BlockEventListener117() {

        GameEventEvent.EVENT.register(this::onBlockReceiveGame);
        LevelSetBlockEvent.EVENT.register(this::onBlockForm);
    }



    public InteractionResult onBlockReceiveGame(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context, ServerLevel serverLevel) {
        Location location = FabricUtil.adapt(GlobalPos.of(serverLevel.dimension(), new BlockPos((int) vec3.x, (int) vec3.y, (int) vec3.z)));
        Entity entity = context.sourceEntity();

        PlotArea area = location.getPlotArea();
        if (area == null) {
            return InteractionResult.PASS;
        }

        FabricPlayer plotPlayer = null;

        if (entity instanceof ServerPlayer player) {
            plotPlayer = FabricUtil.adapt(player);
            if (area.notifyIfOutsideBuildArea(plotPlayer, location.getY())) {
               return InteractionResult.FAIL;
            }
        }

        Plot plot = location.getOwnedPlot();
        if (plot == null && !PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(
                area,
                MiscInteractFlag.class,
                true
        ) || plot != null && (!plot.getFlag(MiscInteractFlag.class) || !plot.getFlag(SculkSensorInteractFlag.class))) {
            if (plotPlayer != null) {
                if (plot != null) {
                    if (!plot.isAdded(plotPlayer.getUUID())) {
                        plot.debug(plotPlayer.getName() + " couldn't trigger sculk sensors because both " +
                                "sculk-sensor-interact and misc-interact = false");
                        return InteractionResult.FAIL;
                    }
                }
                return InteractionResult.PASS;
            }
            if (entity instanceof Projectile item) {
                UUID itemThrower = item.getOwner().getUUID();
                if (plot != null) {
                    /*
                    if (itemThrower == null && (itemThrower = item.getOwner()) == null) {
                        plot.debug(
                                "A thrown item couldn't trigger sculk sensors because both sculk-sensor-interact and " +
                                        "misc-interact = false and the item's owner could not be resolved.");
                        return InteractionResult.FAIL;
                    }*/
                    if (!plot.isAdded(itemThrower)) {
                        if (!plot.isAdded(itemThrower)) {
                            plot.debug("A thrown item couldn't trigger sculk sensors because both sculk-sensor-interact and " +
                                    "misc-interact = false");
                            return InteractionResult.FAIL;
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
/*
    public void onBlockFertilize() {
        Block block = event.getBlock();
        List<org.bukkit.block.BlockState> blocks = event.getBlocks();
        Location location = BukkitUtil.adapt(blocks.get(0).getLocation());

        PlotArea area = location.getPlotArea();
        if (area == null) {
            for (int i = blocks.size() - 1; i >= 0; i--) {
                Location blockLocation = BukkitUtil.adapt(blocks.get(i).getLocation());
                if (blockLocation.isPlotArea()) {
                    blocks.remove(i);
                }
            }
        } else {
            Plot origin = area.getOwnedPlot(location);
            if (origin == null) {
                event.setCancelled(true);
                return;
            }
            for (int i = blocks.size() - 1; i >= 0; i--) {
                Location blockLocation = BukkitUtil.adapt(blocks.get(i).getLocation());
                if (!area.contains(blockLocation.getX(), blockLocation.getZ())) {
                    blocks.remove(i);
                    continue;
                }
                Plot plot = area.getOwnedPlot(blockLocation);
                if (!Objects.equals(plot, origin)) {
                    event.getBlocks().remove(i);
                    continue;
                }
                if (!area.buildRangeContainsY(location.getY())) {
                    event.getBlocks().remove(i);
                }
            }
        }
    }
*/

    public InteractionResult onBlockForm(
            BlockPos blockPos,
            BlockState blockState,
            int i,
            Level level
    ) {
        Location location = FabricUtil.adapt(GlobalPos.of(level.dimension(), blockPos));
        if (location.isPlotRoad()) {
            return InteractionResult.FAIL;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return InteractionResult.PASS;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            return InteractionResult.PASS;
        }
        if (COPPER_OXIDIZING.contains(blockState.getBlock())) {
            if (!plot.getFlag(CopperOxideFlag.class)) {
                plot.debug("Copper could not oxide because copper-oxide = false");
               return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

}
