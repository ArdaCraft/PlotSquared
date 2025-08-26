package com.plotsquared.fabric.player;

import com.plotsquared.core.permissions.NullPermissionProfile;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.permissions.PermissionProfile;
import com.plotsquared.core.player.OfflinePlotPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class FabricOfflinePlayer implements OfflinePlotPlayer {

    public final ServerPlayer player;
    private final PermissionProfile permissionProfile;

    /**
     * Please do not use this method. Instead use FabricUtil.getPlayer(Player),
     * as it caches player objects.
     *
     * @param player            Fabric OfflinePlayer player to convert
     * @param permissionHandler Permission Profile to be used
     */
    public FabricOfflinePlayer(
            final @NonNull ServerPlayer player, final @NonNull
    PermissionHandler permissionHandler
    ) {
        this.player = player;
        this.permissionProfile = permissionHandler.getPermissionProfile(this)
                .orElse(NullPermissionProfile.INSTANCE);
    }

    @NonNull
    @Override
    public UUID getUUID() {
        return this.player.getUUID();
    }

    @Override
    @NonNegative
    public long getLastPlayed() {
        return this.player.getLastActionTime();
    }

    @Override
    public String getName() {
        return this.player.getGameProfile().getName();
    }

    @Override
    public boolean hasPermission(
            final @Nullable String world,
            final @NonNull String permission
    ) {
        return this.permissionProfile.hasPermission(world, permission);
    }

    @Override
    public boolean hasKeyedPermission(
            final @Nullable String world,
            final @NonNull String stub,
            final @NonNull String key
    ) {
        return this.permissionProfile.hasPermission(world, stub + "." + key) || this.permissionProfile.hasPermission(
                world,
                stub + ".*"
        );
    }

    @Override
    public boolean hasPermission(@NonNull final String permission, final boolean notify) {
        return hasPermission(permission);
    }

}
