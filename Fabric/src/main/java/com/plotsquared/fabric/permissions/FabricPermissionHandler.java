package com.plotsquared.fabric.permissions;

import com.plotsquared.core.permissions.ConsolePermissionProfile;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.permissions.PermissionProfile;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.fabric.player.FabricPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class FabricPermissionHandler implements PermissionHandler {

    private Permission permissions;


    @Override
    public void initialize() {
    }

    @Override
    public @NonNull Optional<PermissionProfile> getPermissionProfile(@NonNull final PlotPlayer<?> playerPlotPlayer) {
        if(playerPlotPlayer instanceof final FabricPlayer fabricPlayer) {
            return Optional.of(new FabricPermissionProfile(fabricPlayer.getPlatformPlayer()));
        } else if (playerPlotPlayer instanceof ConsolePlayer) {
            return Optional.of(ConsolePermissionProfile.INSTANCE);
        }
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<PermissionProfile> getPermissionProfile(@NonNull final OfflinePlotPlayer offlinePlotPlayer) {
        return Optional.empty();
    }

    @Override
    public @NonNull Set<PermissionHandlerCapability> getCapabilities() {
        return EnumSet.of(PermissionHandlerCapability.ONLINE_PERMISSIONS);
    }

    private static final class FabricPermissionProfile implements PermissionProfile {

        private final WeakReference<Player> playerReference;

        private FabricPermissionProfile(final @NonNull Player player) {
            this.playerReference = new WeakReference<>(player);
        }

        @Override
        public boolean hasPermission(
                final @Nullable String world,
                final @NonNull String permission
        ) {
            final Player player = this.playerReference.get();
            return player != null /*&& player.hasPermission(permission)*/;
        }

        @Override
        public boolean hasKeyedPermission(
                final @Nullable String world,
                final @NonNull String stub,
                final @NonNull String key
        ) {
            final Player player = this.playerReference.get();
            return player != null /*&& (player.hasPermission(stub + "." + key) || player.hasPermission(stub + ".*"))*/;
        }

    }

}
