package com.plotsquared.fabric.player;

import com.google.common.base.Charsets;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.fabric.util.FabricUtil;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.fabric.FabricAdapter;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class FabricPlayer extends PlotPlayer<ServerPlayer> {

    private static boolean CHECK_EFFECTIVE = true;
    public final ServerPlayer player;
    private String name;

    /**
     * @param plotAreaManager   PlotAreaManager instance
     * @param eventDispatcher   EventDispatcher instance
     * @param player            Fabric ServerPlayer instance
     * @param permissionHandler PermissionHandler instance
     */
    FabricPlayer(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull ServerPlayer player,
            final boolean realPlayer,
            final @NonNull PermissionHandler permissionHandler
    ) {
        super(plotAreaManager, eventDispatcher, permissionHandler);
        this.player = player;
        this.setupPermissionProfile();
        if (realPlayer) {
            super.populatePersistentMetaMap();
        }
    }

    @Override
    public Actor toActor() {
        return FabricAdapter.adaptPlayer(player);
    }

    @Override
    public ServerPlayer getPlatformPlayer() {
        return this.player;
    }

    @NonNull
    @Override
    public UUID getUUID() {
        if (Settings.UUID.OFFLINE) {
            if (Settings.UUID.FORCE_LOWERCASE) {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                        getName().toLowerCase()).getBytes(Charsets.UTF_8));
            } else {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                        getName()).getBytes(Charsets.UTF_8));
            }
        }
        return player.getUUID();
    }

    @Override
    @NonNegative
    public long getLastPlayed() {
        return this.player.getLastActionTime();
    }

    @Override
    public boolean canTeleport(final @NonNull Location location) {
        final GlobalPos to = FabricUtil.adapt(location);
        final org.bukkit.Location from = player.getLocation();
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to);
        callEvent(event);
        if (event.isCancelled() || !event.getTo().equals(to)) {
            return false;
        }
        event = new PlayerTeleportEvent(player, to, from);
        callEvent(event);
        return true;
    }
}
