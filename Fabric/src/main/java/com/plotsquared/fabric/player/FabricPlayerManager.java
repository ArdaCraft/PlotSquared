package com.plotsquared.fabric.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.fabric.FabricPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

@Singleton

public class FabricPlayerManager extends PlayerManager<FabricPlayer, ServerPlayer> {

    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final PermissionHandler permissionHandler;

    @Inject
    public FabricPlayerManager(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull PermissionHandler permissionHandler
    ) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.permissionHandler = permissionHandler;
    }

    @NonNull
    @Override
    public FabricPlayer getPlayer(final @NonNull ServerPlayer object) {
        if (object.getUUID().version() == 2) { // not a real player
            return new FabricPlayer(this.plotAreaManager, this.eventDispatcher, object, false, this.permissionHandler);
        }
        if (object.isLocalPlayer()) {
            throw new NoSuchPlayerException(object.getUUID());
        }
        return getPlayer(object.getUUID());
    }

    @Override
    public @NonNull FabricPlayer createPlayer(final @NonNull UUID uuid) {
        final ServerPlayer player = FabricPlatform.SERVER.getPlayerList().getPlayer(uuid);
        if (player == null) {
            throw new NoSuchPlayerException(uuid);
        }
        return new FabricPlayer(this.plotAreaManager, this.eventDispatcher, player, false, this.permissionHandler);
    }

    @Nullable
    @Override
    public FabricOfflinePlayer getOfflinePlayer(final @Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return new FabricOfflinePlayer(FabricPlatform.SERVER.getPlayerList().getPlayer(uuid), this.permissionHandler);
    }

    @NonNull
    @Override
    public FabricOfflinePlayer getOfflinePlayer(final @NonNull String username) {
        return new FabricOfflinePlayer(FabricPlatform.SERVER.getPlayerList().getPlayerByName(username), this.permissionHandler);
    }

}
