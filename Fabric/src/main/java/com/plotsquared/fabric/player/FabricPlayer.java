package com.plotsquared.fabric.player;

import com.google.common.base.Charsets;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.fabric.listener.event.ServerPlayerTeleportToCallback;
import com.plotsquared.fabric.util.FabricUtil;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Position;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.SpectateCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.SoundType;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.sk89q.worldedit.world.gamemode.GameModes.ADVENTURE;
import static com.sk89q.worldedit.world.gamemode.GameModes.CREATIVE;
import static com.sk89q.worldedit.world.gamemode.GameModes.SPECTATOR;
import static com.sk89q.worldedit.world.gamemode.GameModes.SURVIVAL;

public class FabricPlayer extends PlotPlayer<ServerPlayer> {

    private static boolean CHECK_EFFECTIVE = true;
    public final ServerPlayer player;
    private String name;

    /**
     * @param plotAreaManager   PlotAreaManager instance
     * @param eventDispatcher   EventDispatcher instance
     * @param player            Bukkit player instance
     * @param permissionHandler PermissionHandler instance
     */
    FabricPlayer(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull ServerPlayer player,
            final boolean realPlayer,
            final @NonNull PermissionHandler permissionHandler
    ) {
        super(plotAreaManager, eventDispatcher, permissionHandler);
        this.player = player;
        this.setupPermissionProfile();
        if (realPlayer) {
            super.populatePersistentMetaMap();
        }
    }

    @Override
    public Actor toActor() {
        return FabricAdapter.adaptPlayer(player);
    }

    @Override
    public ServerPlayer getPlatformPlayer() {
        return this.player;
    }

    @NonNull
    @Override
    public UUID getUUID() {
        if (Settings.UUID.OFFLINE) {
            if (Settings.UUID.FORCE_LOWERCASE) {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                        getName().toLowerCase()).getBytes(Charsets.UTF_8));
            } else {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                        getName()).getBytes(Charsets.UTF_8));
            }
        }
        return player.getUUID();
    }

    @Override
    @NonNegative
    public long getLastPlayed() {
        return this.player.getLastActionTime();
    }

    @Override
    public boolean canTeleport(final @NonNull Location location) {
        return WorldUtil.isValidLocation(location);
    }

    /*
        private void callEvent(final @NonNull Event event) {
            final RegisteredListener[] listeners = event.getHandlers().getRegisteredListeners();
            for (final RegisteredListener listener : listeners) {
                if (listener.getPlugin().getName().equals(PlotSquared.platform().pluginName())) {
                    continue;
                }
                try {
                    listener.callEvent(event);
                } catch (final EventException e) {
                    e.printStackTrace();
                }
            }
        }
    */
    @SuppressWarnings("StringSplitter")
    @Override
    @NonNegative
    public int hasPermissionRange(
            final @NonNull String stub,
            @NonNegative final int range
    ) {
        if (hasPermission(Permission.PERMISSION_ADMIN.toString())) {
            return Integer.MAX_VALUE;
        }
        final String[] nodes = stub.split("\\.");
        final StringBuilder n = new StringBuilder();
        // Wildcard check from less specific permission to more specific permission
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i]).append(".");
            if (!stub.equals(n + Permission.PERMISSION_STAR.toString())) {
                if (hasPermission(n + Permission.PERMISSION_STAR.toString())) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        // Wildcard check for the full permission
        if (hasPermission(stub + ".*")) {
            return Integer.MAX_VALUE;
        }
        // Permission value cache for iterative check
        int max = 0;
        if (CHECK_EFFECTIVE) {
            boolean hasAny = false;
            String stubPlus = stub + ".";

            final Map<String, Boolean> effective =
                    LuckPermsProvider
                            .get()
                            .getPlayerAdapter(ServerPlayer.class)
                            .getUser(player)
                            .getCachedData()
                            .getPermissionData()
                            .getPermissionMap();
            if (!effective.isEmpty()) {
                for (String attach : effective.keySet()) {
                    // Ignore all "false" permissions
                    if (!effective.get(attach)) {
                        continue;
                    }
                    String permStr = attach;
                    if (permStr.startsWith(stubPlus)) {
                        hasAny = true;
                        String end = permStr.substring(stubPlus.length());
                        if (MathMan.isInteger(end)) {
                            int val = Integer.parseInt(end);
                            if (val > range) {
                                return val;
                            }
                            if (val > max) {
                                max = val;
                            }
                        }
                    }
                }
                if (hasAny) {
                    return max;
                }
                // Workaround
                for (String attach : effective.keySet()) {
                    String permStr = attach;
                    if (permStr.startsWith("plots.") && !permStr.equals("plots.use")) {
                        return max;
                    }
                }
                CHECK_EFFECTIVE = false;
            }
        }
        for (int i = range; i > 0; i--) {
            if (hasPermission(stub + "." + i)) {
                return i;
            }
        }
        return max;
    }

    @Override
    public void teleport(final @NonNull Location location, final @NonNull TeleportCause cause) {
        if (!WorldUtil.isValidLocation(location)) {
            return;
        }
        player.teleportTo(FabricUtil.getWorld(location.getWorldName()), location.getX() + 0.5, location.getY(),
                location.getZ() + 0.5, location.getYaw(), location.getPitch()
        );
    }

    @Override
    public String getName() {
        if (this.name == null) {
            this.name = this.player.getName().getString();
        }
        return this.name;
    }

    @Override
    public void setCompassTarget(Location location) {
        /* TODO FIGURE OUT IMPLEMENTATION */
        /*
        CompassItemPropertyFunction
        this.player.setCompassTarget(
                new org.bukkit.Location(BukkitUtil.getWorld(location.getWorldName()), location.getX(),
                        location.getY(), location.getZ()
                ));*/
    }

    @Override
    public Location getLocationFull() {
        return FabricUtil.adaptComplete(GlobalPos.of(this.player.level().dimension(), this.player.blockPosition()),
                this.player.getXRot(), this.player.getYRot()
        );
    }

    @Override
    public void setWeather(final @NonNull PlotWeather weather) {
        /* TODO COME UP WITH PACKET SENDING IMPLEMENTATION */
        /*
        switch (weather) {
            case CLEAR -> this.player.setPlayerWeather(WeatherType.CLEAR);
            case RAIN -> this.player.setPlayerWeather(WeatherType.DOWNFALL);
            case WORLD -> this.player.resetPlayerWeather();
            default -> {
                //do nothing as this is PlotWeather.OFF
            }
        }*/
    }

    @Override
    public com.sk89q.worldedit.world.gamemode.GameMode getGameMode() {
        return switch (this.player.gameMode.getGameModeForPlayer()) {
            case ADVENTURE -> ADVENTURE;
            case CREATIVE -> CREATIVE;
            case SPECTATOR -> SPECTATOR;
            default -> SURVIVAL;
        };
    }

    @Override
    public void setGameMode(final com.sk89q.worldedit.world.gamemode.GameMode gameMode) {
        if (ADVENTURE.equals(gameMode)) {
            this.player.setGameMode(GameType.ADVENTURE);
        } else if (CREATIVE.equals(gameMode)) {
            this.player.setGameMode(GameType.CREATIVE);
        } else if (SPECTATOR.equals(gameMode)) {
            this.player.setGameMode(GameType.SPECTATOR);
        } else {
            this.player.setGameMode(GameType.SURVIVAL);
        }
    }

    @Override
    public void setTime(final long time) {
        /*
        if (time != Long.MAX_VALUE) {
            this.player.setPlayerTime(time, false);
        } else {
            this.player.resetPlayerTime();
        }
        */
    }

    @Override
    public boolean getFlight() {
        return player.getAbilities().mayfly;
    }

    @Override
    public void setFlight(boolean fly) {
        this.player.getAbilities().mayfly = fly;
    }

    @Override
    public void playMusic(final @NonNull Location location, final @NonNull ItemType id) {
        if (id == ItemTypes.AIR) {
            if (PlotSquared.platform().serverVersion()[1] >= 19) {
                player.stopSound(SoundStop.source(Sound.Source.MUSIC));
                return;
            }
            // 1.18 and downwards require a specific Sound to stop (even tho the packet does not??)
            for (final Sound.Source sound : Sound.Source.values()) {
                if (sound.name().startsWith("MUSIC_DISC")) {
                    this.player.stopSound(SoundStop.source(Sound.Source.MUSIC));
                }
            }
            return;
        }

        try {
            Sound sound = Sound.sound(Key.key(id.getId().replace("music_disc_",
                            "music_disc.")), Sound.Source.MUSIC, 1f, 1f);
            //player.playSound(sound, Sound.Emitter.self());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation") // Needed for Spigot compatibility
    @Override
    public void kick(final String message) {
        this.player.connection.disconnect(Component.literal(message));
    }

    @Override
    public void stopSpectating() {
        if (getGameMode() == SPECTATOR) {
            this.player.setCamera(this.player.getCamera());
        }
    }

    @Override
    public boolean isBanned() {
        return this.player.server.getPlayerList().getBans().isBanned(this.player.getGameProfile());
    }

    @Override
    public @NonNull Audience getAudience() {
        return FabricUtil.FABRIC_AUDIENCES.player(this.player.getUUID());
    }

    @Override
    public void removeEffect(@NonNull String name) {
        MobEffect type = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(name));
        if (type != null) {
            player.removeEffect(type);
        }
    }

    @Override
    public boolean canSee(final PlotPlayer<?> other) {
        if (other instanceof ConsolePlayer) {
            return true;
        } else {
            return (((FabricPlayer) other).getPlatformPlayer().isInvisibleTo(this.player));
        }
    }


    /**
     * Convert from PlotSquared's {@link TeleportCause} to Fabric's
     *
     * @param cause PlotSquared teleport cause to convert
     * @return Bukkit's equivalent teleport cause
     */
   /* public PlayerTeleportEvent.TeleportCause getTeleportCause(final @NonNull TeleportCause cause) {
        if (TeleportCause.CauseSets.COMMAND.contains(cause)) {
            return PlayerTeleportEvent.TeleportCause.COMMAND;
        } else if (cause == TeleportCause.UNKNOWN) {
            return PlayerTeleportEvent.TeleportCause.UNKNOWN;
        }
        return PlayerTeleportEvent.TeleportCause.PLUGIN;
    }*/

}
