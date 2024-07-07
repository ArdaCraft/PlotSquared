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
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.fabric.FabricPlatform;
import com.plotsquared.fabric.generator.FabricPlotGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class WorldEvents {

    private final PlotAreaManager plotAreaManager;

    @Inject
    public WorldEvents(final @NonNull PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
        ServerWorldEvents.LOAD.register(this::onWorldInit);
    }

    public void onWorldInit(MinecraftServer server, ServerLevel serverLevel) {
        ServerLevel world = serverLevel;
        String name = world.dimension().location().getPath();
        if (this.plotAreaManager instanceof final SinglePlotAreaManager single) {
            if (single.isWorld(name)) {
                world.getChunkSource().removeRegionTicket(TicketType.START, new ChunkPos(world.getSharedSpawnPos()), 11,
                        Unit.INSTANCE
                );
                return;
            }
        }
        ChunkGenerator gen = world.getChunkSource().getGenerator();
        if (gen instanceof GeneratorWrapper) {
            PlotSquared.get().loadWorld(name, (GeneratorWrapper<?>) gen);
        } else {
            PlotSquared.get().loadWorld(name, new FabricPlotGenerator(name, gen, this.plotAreaManager));
        }
    }

}
