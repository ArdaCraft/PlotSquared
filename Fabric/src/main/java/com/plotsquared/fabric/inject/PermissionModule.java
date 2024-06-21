package com.plotsquared.fabric.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.fabric.permissions.FabricPermissionHandler;
import com.plotsquared.fabric.permissions.LuckPermsPermissionHandler;
import net.fabricmc.loader.api.FabricLoader;

public class PermissionModule extends AbstractModule {

    @Provides
    @Singleton
    PermissionHandler providePermissionHandler() {
         try {
             if(FabricLoader.getInstance().isModLoaded("luckperms")) {
                 return new LuckPermsPermissionHandler();
             }
         } catch (final Exception ignored) {
         }
         return new FabricPermissionHandler();
    }
}
