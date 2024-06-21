package com.plotsquared.fabric.managers;
/*
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.util.PlatformWorldManager;
import net.minecraft.server.level.ServerLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FabricDimensionManager implements PlatformWorldManager<ServerLevel> {

    @Override
    public void initialize() {
    }

    @Override
    public @Nullable ServerLevel handleWorldCreation(@NonNull String worldName, @Nullable String generator) {
        this.setGenerator(worldName, generator);
        final WorldCreator wc = new WorldCreator(worldName);
        wc.environment(World.Environment.NORMAL);
        if (generator != null) {
            wc.generator(generator);
            wc.type(WorldType.FLAT);
        }
        return Bukkit.createWorld(wc);
    }

    protected void setGenerator(final @Nullable String worldName, final @Nullable String generator) {
        if (generator == null) {
            return;
        }
        File file = new File("bukkit.yml").getAbsoluteFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set(String.format("worlds.%s.generator", worldName), generator);
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "bukkit";
    }

    @Override
    public Collection<String> getWorlds() {
        final List<World> worlds = Bukkit.getWorlds();
        final List<String> worldNames = new ArrayList<>();
        for (final World world : worlds) {
            worldNames.add(world.getName());
        }
        return worldNames;
    }

}
*/
