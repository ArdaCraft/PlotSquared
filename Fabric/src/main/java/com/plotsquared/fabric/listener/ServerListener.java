package com.plotsquared.fabric.listener;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.fabric.FabricPlatform;
import com.plotsquared.fabric.placeholder.MiniPlaceholders;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ServerListener {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + ServerListener.class.getSimpleName());

    private final FabricPlatform plugin;

    @Inject
    public ServerListener(final @NonNull FabricPlatform plugin) {
        this.plugin = plugin;
    }

    public void onServerLoad() {
        if (FabricLoader.getInstance().isModLoaded("miniplaceholders")
               /*&& Settings.Enabled_Components.USE_MINIPLACEHOLDERS*/) {
            new MiniPlaceholders(this.plugin.placeholderRegistry());
            ConsolePlayer.getConsole().sendMessage(TranslatableCaption.of("placeholder.miniplaceholders.hooked"));
        }
        /* TODO SETUP ECON HANDLERS */
        /*
        if (Settings.Enabled_Components.ECONOMY && Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            EconHandler econHandler = new FabricEconHandler();
            try {
                if (!econHandler.init()) {
                    LOGGER.warn("Economy is enabled but no plugin is providing an economy service. Falling back...");
                    econHandler = EconHandler.nullEconHandler();
                }
            } catch (final Exception ignored) {
                econHandler = EconHandler.nullEconHandler();
            }
            if (PlotSquared.platform().econHandler() instanceof MutableEconHandler meh) {
                meh.setImplementation(econHandler);
            }
        }*/
    }

    /**
     * Internal use only. Required to implement lazy econ loading using Guice.
     *
     * @since 7.2.0
     */
    public interface MutableEconHandler {
        void setImplementation(EconHandler econHandler);
    }

}
