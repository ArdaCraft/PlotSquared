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

import com.bencrow11.multieconomy.account.AccountManager;
import com.bencrow11.multieconomy.config.ConfigManager;
import com.google.inject.Singleton;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import net.fabricmc.loader.api.FabricLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.NumberFormat;

@Singleton
public class FabricEconHandler extends EconHandler {

    @Override
    public boolean init() {
        setupEconomy();
        return true;
    }

    private void setupEconomy() {
        if (!FabricLoader.getInstance().isModLoaded("multieconomy")) {
            return;
        }
    }

    @Override
    public double getMoney(PlotPlayer<?> player) {
        double bal = super.getMoney(player);
        if (Double.isNaN(bal)) {
            return AccountManager.getAccount(player.getName()).getBalances().values().stream().findFirst().get().doubleValue();
        }
        return bal;
    }

    @Override
    public void withdrawMoney(PlotPlayer<?> player, double amount) {
        AccountManager.getAccount(player.getName()).remove(ConfigManager.getConfig().getCurrencyByName("dollar"), (float) amount);
    }

    @Override
    public void depositMoney(PlotPlayer<?> player, double amount) {
        AccountManager.getAccount(player.getName()).add(ConfigManager.getConfig().getCurrencyByName("dollar"), (float) amount);
    }

    @Override
    public void depositMoney(OfflinePlotPlayer player, double amount) {
        AccountManager.getAccount(player.getName()).remove(ConfigManager.getConfig().getCurrencyByName("dollar"), (float) amount);
    }

    @Override
    public boolean isEnabled(PlotArea plotArea) {
        return plotArea.useEconomy();
    }

    @Override
    public @NonNull String format(double balance) {
        return NumberFormat.getCurrencyInstance().format(balance);
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public double getBalance(PlotPlayer<?> player) {
       return AccountManager.getAccount(player.getName()).getBalance(ConfigManager.getConfig().getCurrencyByName("dollar"));
    }

}
