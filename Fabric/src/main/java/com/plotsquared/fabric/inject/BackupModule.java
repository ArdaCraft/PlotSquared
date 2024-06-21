package com.plotsquared.fabric.inject;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.backup.BackupProfile;
import com.plotsquared.core.backup.NullBackupManager;
import com.plotsquared.core.backup.PlayerBackupProfile;
import com.plotsquared.core.backup.SimpleBackupManager;
import com.plotsquared.core.inject.factory.PlayerBackupProfileFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BackupModule extends AbstractModule {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + BackupModule.class.getSimpleName());

    @Override
    protected void configure() {
        try {
            install(new FactoryModuleBuilder()
                    .implement(BackupProfile.class, PlayerBackupProfile.class).build(PlayerBackupProfileFactory.class));
            bind(BackupManager.class).to(SimpleBackupManager.class);
        } catch (final Exception e) {
            LOGGER.error("Failed to initialize backup manager", e);
            LOGGER.error("Backup features will be disabled");
            bind(BackupManager.class).to(NullBackupManager.class);
        }
    }

}
