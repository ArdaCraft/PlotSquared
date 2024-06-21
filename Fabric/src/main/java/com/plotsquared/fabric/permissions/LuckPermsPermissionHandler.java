package com.plotsquared.fabric.permissions;

import com.mojang.authlib.GameProfile;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.permissions.PermissionProfile;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.fabric.player.FabricOfflinePlayer;
import com.plotsquared.fabric.player.FabricPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPermsProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class LuckPermsPermissionHandler implements PermissionHandler {

    @Override
    public void initialize() {
        if(!FabricLoader.getInstance().isModLoaded("luckperms")) {
            throw new IllegalStateException("Luckperms is not present on the server");
        }
    }

    @Override
    public @NonNull Optional<PermissionProfile> getPermissionProfile(@NonNull final PlotPlayer<?> playerPlotPlayer) {
        if(playerPlotPlayer instanceof FabricPlayer fabricPlayer) {
            return Optional.of(new LuckPermsPermissionProfile(fabricPlayer.getPlatformPlayer().getGameProfile()));
        }
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<PermissionProfile> getPermissionProfile(@NonNull final OfflinePlotPlayer offlinePlotPlayer) {
        if(offlinePlotPlayer instanceof FabricOfflinePlayer) {
            return Optional.of(new LuckPermsPermissionProfile(((FabricOfflinePlayer) offlinePlotPlayer).player.getGameProfile()));
        }
        return Optional.empty();
    }

    @Override
    public @NonNull Set<PermissionHandlerCapability> getCapabilities() {
        return EnumSet.of(
                PermissionHandlerCapability.PER_WORLD_PERMISSIONS,
                PermissionHandlerCapability.ONLINE_PERMISSIONS,
                PermissionHandlerCapability.OFFLINE_PERMISSIONS
        );
    }

    @Override
    public boolean hasCapability(final @NonNull PermissionHandlerCapability capability) {
        return PermissionHandler.super.hasCapability(capability);
    }

    private final class LuckPermsPermissionProfile implements PermissionProfile {

        private final GameProfile offlinePlayer;

        private LuckPermsPermissionProfile(final @NonNull GameProfile offlinePlayer) {
            this.offlinePlayer = offlinePlayer;
        }

        @Override
        public boolean hasPermission(
                final @Nullable String world,
                final @NonNull String permission
        ) {
            return LuckPermsProvider.get().getUserManager().getUser(offlinePlayer.getId()).getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            /*if (permissions == null) {
                return false;
            }*/
           /* if (world == null && offlinePlayer instanceof BukkitPlayer) {
                return permissions.playerHas(((BukkitPlayer) offlinePlayer).getPlatformPlayer(), permission);
            }
            return permissions.playerHas(world, offlinePlayer, permission);*/
        }

        @Override
        public boolean hasKeyedPermission(
                final @Nullable String world,
                final @NonNull String stub,
                final @NonNull String key
        ) {
            return LuckPermsProvider.get().getUserManager().getUser(offlinePlayer.getId()).getCachedData().getPermissionData().checkPermission(stub+"."+key).asBoolean();
            /*
            if (permissions == null) {
                return false;
            }
            if (world == null && offlinePlayer instanceof BukkitPlayer) {
                return permissions.playerHas(
                        ((BukkitPlayer) offlinePlayer).getPlatformPlayer(),
                        stub + ".*"
                ) || permissions.playerHas(((BukkitPlayer) offlinePlayer).getPlatformPlayer(), stub + "." + key);
            }
            return permissions.playerHas(world, offlinePlayer, stub + ".*") || permissions.playerHas(world, offlinePlayer,
                    stub + "." + key
            );*/
        }

    }

}
