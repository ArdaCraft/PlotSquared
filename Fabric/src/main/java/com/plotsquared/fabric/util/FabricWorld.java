package com.plotsquared.fabric.util;

import com.google.common.collect.Maps;
import com.plotsquared.core.location.World;
import com.plotsquared.fabric.FabricPlatform;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

public class FabricWorld implements World<ServerLevel> {

    private static final Map<String, FabricWorld> worldMap = Maps.newHashMap();
    private static final boolean HAS_MIN_Y;

    static {
        boolean temp;
        try {
            ServerLevel.class.getMethod("getMinBuildHeight");
            temp = true;
        } catch (NoSuchMethodException e) {
            temp = false;
        }
        HAS_MIN_Y = temp;
    }

    private final ResourceKey<Level> world;

    private FabricWorld(final ServerLevel world) {
        this.world = world.dimension();
    }

    /**
     * Get a new {@link FabricWorld} from a world name
     *
     * @param worldName World name
     * @return World instance
     */
    public static @NonNull FabricWorld of(final @NonNull String worldName) {
        final ServerLevel fabricWorld = FabricUtil.getWorld(worldName);
        if (fabricWorld == null) {
            throw new IllegalArgumentException(String.format("There is no world with the name '%s'", worldName));
        }
        return of(fabricWorld);
    }

    /**
     * Get a new {@link FabricWorld} from a Fabric world
     *
     * @param world Fabric world
     * @return World instance
     */
    public static @NonNull FabricWorld of(final ServerLevel world) {
        FabricWorld fabricWorld = worldMap.get(world.dimension().location().getPath());
        if (fabricWorld != null) {
            if (fabricWorld.getPlatformWorld().equals(world)) {
                return fabricWorld;
            }
        }
        fabricWorld = new FabricWorld(world);
        worldMap.put(world.dimension().location().getPath(), fabricWorld);
        return fabricWorld;
    }

    /**
     * Get the min world height from a Fabric {@link ServerLevel}. Inclusive
     *
     * @since 6.6.0
     */
    public static int getMinWorldHeight(ServerLevel world) {
        return HAS_MIN_Y ? world.getMinBuildHeight() : 0;
    }

    /**
     * Get the max world height from a Fabric {@link ServerLevel}. Exclusive
     *
     * @since 6.6.0
     */
    public static int getMaxWorldHeight(ServerLevel world) {
        return HAS_MIN_Y ? world.getMaxBuildHeight() : 256;
    }

    @Override
    public ServerLevel getPlatformWorld() {
        return FabricPlatform.SERVER.getLevel(this.world);
    }

    @Override
    public @NonNull String getName() {
        return this.world.location().getPath();
    }

    @Override
    public int getMinHeight() {
        return getMinWorldHeight(this.getPlatformWorld());
    }

    @Override
    public int getMaxHeight() {
        return getMaxWorldHeight(this.getPlatformWorld()) - 1;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FabricWorld that = (FabricWorld) o;
        return world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

    public String toString() {
        return "FabricWorld(world=" + this.world.location().getPath() + ")";
    }

}
