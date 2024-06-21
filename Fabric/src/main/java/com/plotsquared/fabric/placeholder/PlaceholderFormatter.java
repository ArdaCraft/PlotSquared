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
package com.plotsquared.fabric.placeholder;

import com.plotsquared.core.configuration.caption.ChatFormatter;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.fabric.player.FabricPlayer;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlaceholderFormatter implements ChatFormatter {

    @Override
    public void format(final @NonNull ChatContext context) {
        final PlotPlayer<?> recipient = context.getRecipient();
        if (recipient instanceof FabricPlayer) {
            if (context.isRawOutput()) {
                context.setMessage(context.getMessage().replace('%', '\u2010'));
            } else {
                final ServerPlayer player = ((FabricPlayer) recipient).player;
                context.setMessage(MiniPlaceholders.);
                context.setMessage(PlaceholderAPI.setPlaceholders(player, context.getMessage()));
            }
        }
    }

}
