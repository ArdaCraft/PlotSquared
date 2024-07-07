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
package com.plotsquared.fabric;

import com.mojang.brigadier.Command;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FabricCommand {


    public static boolean onCommand(
            CommandSourceStack commandSender, Command<?> command, String commandLabel,
            String[] args
    ) {

        if (commandSender.isPlayer()) {
            return MainCommand.onCommand(FabricUtil.adapt(commandSender.getPlayer()), args);
        }
        if (!commandSender.isPlayer()) {
            return MainCommand.onCommand(ConsolePlayer.getConsole(), args);
        }
        return false;
    }

    public static List<String> onTabComplete(
            CommandSourceStack commandSender, Command command, String label,
            String[] args
    ) {
        if (!(commandSender.isPlayer())) {
            return null;
        }
        PlotPlayer<?> player = FabricUtil.adapt(commandSender.getPlayer());
        if (!Settings.Enabled_Components.TAB_COMPLETED_ALIASES.contains(label.toLowerCase(Locale.ENGLISH))) {
            return List.of();
        }
        Collection<com.plotsquared.core.command.Command> objects =
                MainCommand.getInstance().tab(player, args, true);
        if (objects == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (com.plotsquared.core.command.Command o : objects) {
            result.add(o.toString());
        }
        return result;
    }

}
