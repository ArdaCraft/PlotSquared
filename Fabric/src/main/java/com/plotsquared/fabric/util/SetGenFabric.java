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
package com.plotsquared.fabric.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.fabric.FabricPlatform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class SetGenFabric {

    public static void setGenerator(ServerLevel world) {
        PlotSquared.platform().setupUtils().updateGenerators(false);
        String worldName = world.dimension().location().getPath();
        PlotSquared.get().removePlotAreas(worldName);

        // Create a world-specific generator instead of using the shared one from SetupUtils.generators
        ChunkGenerator newGen = FabricPlatform.PLATFORM.getDefaultWorldGenerator(worldName, "");
        if (newGen != null) {
            world.getChunkSource().chunkMap.generator = newGen;
        }

        PlotSquared.get().loadWorld(
                worldName,
                PlotSquared.platform().getGenerator(worldName, null)
        );
    }

}
