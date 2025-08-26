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
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.EditSignFlag;
import com.plotsquared.core.util.PlotFlagUtil;
import com.plotsquared.fabric.listener.event.PlayerOpenSignCallback;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.SignBlockEntity;

/**
 * For events since 1.20.1
 *
 * @since 7.2.1
 */
public class PlayerEventListener1201 {

    @Inject
    public PlayerEventListener1201() {
        PlayerOpenSignCallback.EVENT.register(this::onPlayerSignOpenEvent);
    }

    public InteractionResult onPlayerSignOpenEvent(SignBlockEntity signBlockEntity, boolean bl, ServerPlayer serverPlayer) {
        Location location = FabricUtil.adapt(GlobalPos.of(signBlockEntity.getLevel().dimension(), signBlockEntity.getBlockPos()));
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return InteractionResult.PASS;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, EditSignFlag.class, false)
                    && !FabricUtil.adapt(serverPlayer).hasPermission(Permission.PERMISSION_ADMIN_INTERACT_ROAD.toString())) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.FAIL;
        }
        if (plot.isAdded(serverPlayer.getUUID())) {
            return InteractionResult.PASS; // allow for added players
        }
        if (!plot.getFlag(EditSignFlag.class)
                && !FabricUtil.adapt(serverPlayer).hasPermission(Permission.PERMISSION_ADMIN_INTERACT_OTHER.toString())) {
            plot.debug(serverPlayer.getGameProfile().getName() + " could not edit the sign because of edit-sign = false");
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

}
