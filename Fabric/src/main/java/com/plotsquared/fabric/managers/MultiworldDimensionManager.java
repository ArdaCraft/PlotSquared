package com.plotsquared.fabric.managers;

import com.plotsquared.core.util.PlatformWorldManager;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiworldDimensionManager implements PlatformWorldManager<ServerLevel> {

    @Override
    public void initialize() {
    }

    @Override
    public @Nullable ServerLevel handleWorldCreation(final @NonNull String worldName, final @Nullable String generator) {
        return MultiworldMod.create_world("plotsquared:" + worldName, BuiltinDimensionTypes.OVERWORLD.location(),
                MultiworldMod.mc.overworld().getChunkSource().getGenerator(),
                Difficulty.NORMAL, 1234);
    }

    @Override
    public String getName() {
        return "fabric";
    }

    @Override
    public Collection<String> getWorlds() {
        final List<String> worldNames = new ArrayList<>();
        for (final ServerLevel allLevel : MultiworldMod.mc.getAllLevels()) {
            worldNames.add(allLevel.serverLevelData.getLevelName());
        }
        return worldNames;
    }

}
