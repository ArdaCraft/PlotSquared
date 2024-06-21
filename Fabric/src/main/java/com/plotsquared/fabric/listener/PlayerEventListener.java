/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.fabric.listener;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.listener.PlayerBlockEventType;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.flag.FlagContainer;
import com.plotsquared.core.plot.flag.implementations.AnimalInteractFlag;
import com.plotsquared.core.plot.flag.implementations.BlockedCmdsFlag;
import com.plotsquared.core.plot.flag.implementations.ChatFlag;
import com.plotsquared.core.plot.flag.implementations.DenyPortalTravelFlag;
import com.plotsquared.core.plot.flag.implementations.DenyPortalsFlag;
import com.plotsquared.core.plot.flag.implementations.DenyTeleportFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.flag.implementations.DropProtectionFlag;
import com.plotsquared.core.plot.flag.implementations.EditSignFlag;
import com.plotsquared.core.plot.flag.implementations.HangingBreakFlag;
import com.plotsquared.core.plot.flag.implementations.HangingPlaceFlag;
import com.plotsquared.core.plot.flag.implementations.HostileInteractFlag;
import com.plotsquared.core.plot.flag.implementations.ItemDropFlag;
import com.plotsquared.core.plot.flag.implementations.KeepInventoryFlag;
import com.plotsquared.core.plot.flag.implementations.LecternReadBookFlag;
import com.plotsquared.core.plot.flag.implementations.MiscInteractFlag;
import com.plotsquared.core.plot.flag.implementations.PlayerInteractFlag;
import com.plotsquared.core.plot.flag.implementations.PreventCreativeCopyFlag;
import com.plotsquared.core.plot.flag.implementations.TamedInteractFlag;
import com.plotsquared.core.plot.flag.implementations.TileDropFlag;
import com.plotsquared.core.plot.flag.implementations.UntrustedVisitFlag;
import com.plotsquared.core.plot.flag.implementations.VehicleBreakFlag;
import com.plotsquared.core.plot.flag.implementations.VehicleUseFlag;
import com.plotsquared.core.plot.flag.implementations.VillagerInteractFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.PlotFlagUtil;
import com.plotsquared.core.util.entity.EntityCategories;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.plotsquared.fabric.listener.event.HandleMoveVehicleCallback;
import com.plotsquared.fabric.listener.event.HandlePlayerMoveCallback;
import com.plotsquared.fabric.listener.event.ServerPlayerTeleportToCallback;
import com.plotsquared.fabric.player.FabricPlayer;
import com.plotsquared.fabric.util.FabricUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.block.BlockDropItemsEvent;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerChatEvent;
import xyz.nucleoid.stimuli.event.player.PlayerCommandEvent;
import xyz.nucleoid.stimuli.event.player.PlayerInventoryActionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static com.plotsquared.fabric.data.PlotSquaredDataAttachments.PLOT_DATA;
import static net.minecraft.world.item.Items.BOOK;
import static net.minecraft.world.item.Items.CHEST_MINECART;
import static net.minecraft.world.item.Items.COMMAND_BLOCK_MINECART;
import static net.minecraft.world.item.Items.FURNACE_MINECART;
import static net.minecraft.world.item.Items.HOPPER_MINECART;
import static net.minecraft.world.item.Items.KNOWLEDGE_BOOK;
import static net.minecraft.world.item.Items.MINECART;
import static net.minecraft.world.item.Items.TNT_MINECART;
import static net.minecraft.world.item.Items.WRITABLE_BOOK;
import static net.minecraft.world.item.Items.WRITTEN_BOOK;

/**
 * Player Events involving plots.
 */
@SuppressWarnings("unused")
public class PlayerEventListener {

    private static final Set<Item> MINECARTS = Set.of(
            MINECART,
            TNT_MINECART,
            CHEST_MINECART,
            COMMAND_BLOCK_MINECART,
            FURNACE_MINECART,
            HOPPER_MINECART
    );
    private static final Set<Item> BOOKS = Set.of(
            BOOK,
            KNOWLEDGE_BOOK,
            WRITABLE_BOOK,
            WRITTEN_BOOK
    );
    private static final Set<String> DYES;

    static {
        Set<String> mutableDyes = new HashSet<>(Set.of(
                "WHITE_DYE",
                "LIGHT_GRAY_DYE",
                "GRAY_DYE",
                "BLACK_DYE",
                "BROWN_DYE",
                "RED_DYE",
                "ORANGE_DYE",
                "YELLOW_DYE",
                "LIME_DYE",
                "GREEN_DYE",
                "CYAN_DYE",
                "LIGHT_BLUE_DYE",
                "BLUE_DYE",
                "PURPLE_DYE",
                "MAGENTA_DYE",
                "PINK_DYE",
                "GLOW_INK_SAC"
        ));
        int[] version = PlotSquared.platform().serverVersion();
        if (version[1] >= 20 && version[2] >= 1) {
            mutableDyes.add("HONEYCOMB");
        }
        DYES = Set.copyOf(mutableDyes);
    }

    private final EventDispatcher eventDispatcher;
    private final WorldEdit worldEdit;
    private final PlotAreaManager plotAreaManager;
    private final PlotListener plotListener;
    // To prevent recursion
    private boolean tmpTeleport = true;
    private String internalVersion;

    @Inject
    public PlayerEventListener(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull WorldEdit worldEdit,
            final @NonNull PlotListener plotListener
    ) {
        this.eventDispatcher = eventDispatcher;
        this.worldEdit = worldEdit;
        this.plotAreaManager = plotAreaManager;
        this.plotListener = plotListener;


        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            Location location = FabricUtil.adapt(GlobalPos.of(world.dimension(), pos));
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return;
            }
            Plot plot = area.getPlot(location);
            if (plot != null) {
                Stimuli.global().listen(BlockDropItemsEvent.EVENT, (entity1, serverLevel, blockPos, blockState, list) -> {
                    if (plot.getFlag(TileDropFlag.class)) {
                        return InteractionResultHolder.pass(list);
                    }
                    return InteractionResultHolder.fail(list);
                });
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            boolean cancelled = false;
            ItemStack itemStack = player.getItemInHand(hand);
            BlockState blockstate = world.getBlockState(hitResult.getBlockPos());
            Block block = blockstate.getBlock();
            if (block instanceof SignBlock) {
                if (/*DYES.contains(itemStack.getItem().toString())*/itemStack.getItem() instanceof DyeItem) {
                    Location location = FabricUtil.adapt(GlobalPos.of(world.dimension(), hitResult.getBlockPos()));
                    PlotArea area = location.getPlotArea();
                    if (area == null) {
                        return InteractionResult.FAIL;
                    }
                    Plot plot = location.getOwnedPlot();
                    ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
                    if (plot == null) {
                        if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, EditSignFlag.class, false)
                                && !FabricUtil
                                .adapt(serverPlayer)
                                .hasPermission(Permission.PERMISSION_ADMIN_INTERACT_ROAD.toString())) {
                            cancelled = true;
                        }
                        return cancelled ? InteractionResult.FAIL : InteractionResult.PASS;
                    }
                    if (plot.isAdded(player.getUUID())) {
                        return InteractionResult.PASS; // allow for added players
                    }
                    if (!plot.getFlag(EditSignFlag.class)
                            && !FabricUtil
                            .adapt(serverPlayer)
                            .hasPermission(Permission.PERMISSION_ADMIN_INTERACT_OTHER.toString())) {
                        plot.debug(player.getName() + " could not color the sign because of edit-sign = false");
                        cancelled = true;
                    }
                }
            }
            return cancelled ? InteractionResult.FAIL : InteractionResult.PASS;
        });

        Stimuli.global().listen(PlayerCommandEvent.EVENT, (serverPlayer, s) -> {
            String msg = s.replace("/", "").toLowerCase(Locale.ROOT).trim();
            if (msg.isEmpty()) {
                return InteractionResult.PASS;
            }
            ServerPlayer player = serverPlayer;
            PlotPlayer<ServerPlayer> plotPlayer = FabricUtil.adapt(player);
            Location location = plotPlayer.getLocation();
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return InteractionResult.FAIL;
            }
            String[] parts = msg.split(" ");
            Plot plot = plotPlayer.getCurrentPlot();
            // Check WorldEdit
            switch (parts[0]) {
                case "up", "worldedit:up" -> {
                    if (plot == null || (!plot.isAdded(plotPlayer.getUUID()) && !plotPlayer.hasPermission(
                            Permission.PERMISSION_ADMIN_BUILD_OTHER,
                            true
                    ))) {
                        return InteractionResult.FAIL;
                    }
                }
            }
            if (plot == null && !area.isRoadFlags()) {
                return InteractionResult.PASS;
            }

            List<String> blockedCommands = plot != null ?
                    plot.getFlag(BlockedCmdsFlag.class) :
                    area.getFlag(BlockedCmdsFlag.class);
            if (blockedCommands.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (plotPlayer.hasPermission(Permission.PERMISSION_ADMIN_INTERACT_BLOCKED_CMDS)) {
                return InteractionResult.PASS;
            }
            // When using namespaced commands, we're not interested in the namespace
            /*
            String part = parts[0];
            if (part.contains(":")) {
                String[] namespaced = part.split(":");
                part = namespaced[1];
                msg = msg.substring(namespaced[0].length() + 1);
            }
            msg = replaceAliases(msg, part);*/
            for (String blocked : blockedCommands) {
                if (blocked.equalsIgnoreCase(msg)) {
                    String perm;
                    if (plot != null && plot.isAdded(plotPlayer.getUUID())) {
                        perm = "plots.admin.command.blocked-cmds.shared";
                    } else {
                        perm = "plots.admin.command.blocked-cmds.road";
                    }
                    if (!plotPlayer.hasPermission(perm)) {
                        plotPlayer.sendMessage(TranslatableCaption.of("blockedcmds.command_blocked"));
                        return InteractionResult.FAIL;
                    }
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.PASS;
        });

        ServerLoginConnectionEvents.INIT.register((handler, server) -> {
            final UUID uuid;
            if (Settings.UUID.OFFLINE) {
                if (Settings.UUID.FORCE_LOWERCASE) {
                    uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + handler
                            .getUserName()
                            .toLowerCase()).getBytes(Charsets.UTF_8));
                } else {
                    uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + handler.getUserName()).getBytes(Charsets.UTF_8));
                }
            } else {
                uuid = server.getProfileCache().get(handler.getUserName()).get().getId();
            }
            PlotSquared.get().getImpromptuUUIDPipeline().storeImmediately(handler.getUserName(), uuid);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final ServerPlayer player = handler.player;
            PlotSquared.platform().playerManager().removePlayer(player.getUUID());
            final PlotPlayer<ServerPlayer> pp = FabricUtil.adapt(player);

            // we're stripping the country code as we don't want to differ between countries
            //pp.setLocale(Locale.forLanguageTag(player.getLocale().substring(0, 2)));

            Location location = pp.getLocation();
            PlotArea area = location.getPlotArea();
            if (area != null) {
                Plot plot = area.getPlot(location);
                if (plot != null) {
                    plotListener.plotEntry(pp, plot);
                }
            }
            // Delayed

            // Async
            TaskManager.runTaskLaterAsync(() -> {
                /*if (!player.hasPlayedBefore() && player.isLocalPlayer()) {
                    player.saveData();
                }*/
                this.eventDispatcher.doJoinTask(pp);
            }, TaskTime.seconds(1L));
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            ServerPlayer player = newPlayer;
            PlotPlayer<ServerPlayer> pp = FabricUtil.adapt(player);
            this.eventDispatcher.doRespawnTask(pp);
        });


        HandlePlayerMoveCallback.EVENT.register((serverboundMovePlayerPacket, serverPlayer) -> {
            BlockPos fromBlockPos = new BlockPos(
                    serverPlayer.getBlockX(),
                    serverPlayer.getBlockY(),
                    serverPlayer.getBlockZ()
            );
            BlockPos toBlockPos = new BlockPos(
                    (int) serverboundMovePlayerPacket.getX(0.0),
                    (int) serverboundMovePlayerPacket.getY(0.0),
                    (int) serverboundMovePlayerPacket.getZ(0.0)
            );


            GlobalPos from = GlobalPos.of(serverPlayer.serverLevel().dimension(), fromBlockPos);
            GlobalPos to = GlobalPos.of(serverPlayer.serverLevel().dimension(), toBlockPos);
            int x2;
            if (MathMan.roundInt(from.pos().getX()) != (x2 = MathMan.roundInt(to.pos().getX()))) {
                ServerPlayer player = serverPlayer;
                FabricPlayer pp = FabricUtil.adapt(player);
                // Cancel teleport
                if (TaskManager.removeFromTeleportQueue(pp.getName())) {
                    pp.sendMessage(TranslatableCaption.of("teleport.teleport_failed"));
                }
                // Set last location
                Location location = FabricUtil.adapt(to);
                try (final MetaDataAccess<Location> lastLocationAccess =
                             pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LOCATION)) {
                    lastLocationAccess.remove();
                }
                PlotArea area = location.getPlotArea();
                if (area == null) {
                    try (final MetaDataAccess<Plot> lastPlotAccess =
                                 pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                        lastPlotAccess.remove();
                    }
                    return InteractionResult.PASS;
                }
                Plot now = area.getPlot(location);
                Plot lastPlot;
                try (final MetaDataAccess<Plot> lastPlotAccess =
                             pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                    lastPlot = lastPlotAccess.get().orElse(null);
                }
                if (now == null) {
                    try (final MetaDataAccess<Boolean> kickAccess =
                                 pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_KICK)) {
                        if (lastPlot != null && !plotListener.plotExit(pp, lastPlot) && this.tmpTeleport && !kickAccess
                                .get()
                                .orElse(
                                        false)) {
                            pp.sendMessage(
                                    TranslatableCaption.of("permission.no_permission_event"),
                                    TagResolver.resolver(
                                            "node",
                                            Tag.inserting(Permission.PERMISSION_ADMIN_EXIT_DENIED)
                                    )
                            );
                            this.tmpTeleport = false;
                            if (lastPlot.equals(FabricUtil.adapt(from).getPlot())) {
                                player.teleportTo(from.pos().getX(), from.pos().getY(), from.pos().getZ());
                            } else {
                                player.teleportTo(
                                        player.serverLevel().getSharedSpawnPos().getX(),
                                        player.serverLevel().getSharedSpawnPos().getY(),
                                        player.serverLevel().getSharedSpawnPos().getZ()
                                );
                            }
                            this.tmpTeleport = true;
                            return InteractionResult.FAIL;
                        }
                    }
                } else if (now.equals(lastPlot)) {
                    ForceFieldListener.handleForcefield(player, pp, now);
                } else if (!plotListener.plotEntry(pp, now) && this.tmpTeleport) {
                    pp.sendMessage(
                            TranslatableCaption.of("deny.no_enter"),
                            TagResolver.resolver("plot", Tag.inserting(Component.text(now.toString())))
                    );
                    this.tmpTeleport = false;
                    to = GlobalPos.of(to.dimension(), from.pos());
                    player.teleportTo(to.pos().getX(), to.pos().getY(), to.pos().getZ());
                    this.tmpTeleport = true;
                    return InteractionResult.PASS;
                }
                int border = area.getBorder(true);
                int x1;
                if (x2 > border && this.tmpTeleport) {
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_BORDER)) {

                        to = GlobalPos.of(to.dimension(), new BlockPos(border - 1, to.pos().getY(), to.pos().getZ()));
                        this.tmpTeleport = false;
                        player.teleportTo(to.pos().getX(), to.pos().getY(), to.pos().getZ());
                        this.tmpTeleport = true;
                        pp.sendMessage(TranslatableCaption.of("border.denied"));
                    } else if (MathMan.roundInt(from.pos().getX()) <= border) { // Only send if they just moved out of the border
                        pp.sendMessage(TranslatableCaption.of("border.bypass.exited"));
                    }
                } else if (x2 < -border && this.tmpTeleport) {
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_BORDER)) {
                        to = GlobalPos.of(to.dimension(), new BlockPos(-border + 1, to.pos().getY(), to.pos().getZ()));
                        this.tmpTeleport = false;
                        player.teleportTo(to.pos().getX(), to.pos().getY(), to.pos().getZ());
                        this.tmpTeleport = true;
                        pp.sendMessage(TranslatableCaption.of("border.denied"));
                    } else if (MathMan.roundInt(from.pos().getX()) >= -border) { // Only send if they just moved out of the border
                        pp.sendMessage(TranslatableCaption.of("border.bypass.exited"));
                    }
                } else if (((x1 = MathMan.roundInt(from
                        .pos()
                        .getX())) >= border && x2 <= border) || (x1 <= -border && x2 >= -border)) {
                    if (pp.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_BORDER)) {
                        pp.sendMessage(TranslatableCaption.of("border.bypass.entered"));
                    }
                }
            }
            int z2;
            if (MathMan.roundInt(from.pos().getZ()) != (z2 = MathMan.roundInt(to.pos().getZ()))) {
                ServerPlayer player = serverPlayer;
                FabricPlayer pp = FabricUtil.adapt(player);
                // Cancel teleport
                if (TaskManager.removeFromTeleportQueue(pp.getName())) {
                    pp.sendMessage(TranslatableCaption.of("teleport.teleport_failed"));
                }
                // Set last location
                Location location = FabricUtil.adapt(to);
                try (final MetaDataAccess<Location> lastLocationAccess =
                             pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LOCATION)) {
                    lastLocationAccess.set(location);
                }
                PlotArea area = location.getPlotArea();
                if (area == null) {
                    try (final MetaDataAccess<Plot> lastPlotAccess =
                                 pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                        lastPlotAccess.remove();
                    }
                    return InteractionResult.PASS;
                }
                Plot plot = area.getPlot(location);
                Plot lastPlot;
                try (final MetaDataAccess<Plot> lastPlotAccess =
                             pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                    lastPlot = lastPlotAccess.get().orElse(null);
                }
                if (plot == null) {
                    try (final MetaDataAccess<Boolean> kickAccess =
                                 pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_KICK)) {
                        if (lastPlot != null && !plotListener.plotExit(pp, lastPlot) && this.tmpTeleport && !kickAccess
                                .get()
                                .orElse(
                                        false)) {
                            pp.sendMessage(
                                    TranslatableCaption.of("permission.no_permission_event"),
                                    TagResolver.resolver(
                                            "node",
                                            Tag.inserting(Permission.PERMISSION_ADMIN_EXIT_DENIED)
                                    )
                            );
                            this.tmpTeleport = false;
                            if (lastPlot.equals(FabricUtil.adapt(from).getPlot())) {
                                player.teleportTo(from.pos().getX(), from.pos().getY(), from.pos().getZ());
                            } else {
                                player.teleportTo(
                                        player.serverLevel().getSharedSpawnPos().getX(),
                                        player.serverLevel().getSharedSpawnPos().getY(),
                                        player.serverLevel().getSharedSpawnPos().getZ()
                                );
                            }
                            this.tmpTeleport = true;
                            return InteractionResult.FAIL;
                        }
                    }
                } else if (plot.equals(lastPlot)) {
                    ForceFieldListener.handleForcefield(player, pp, plot);
                } else if (!plotListener.plotEntry(pp, plot) && this.tmpTeleport) {
                    pp.sendMessage(
                            TranslatableCaption.of("deny.no_enter"),
                            TagResolver.resolver("plot", Tag.inserting(Component.text(plot.toString())))
                    );
                    this.tmpTeleport = false;
                    player.teleportTo(from.pos().getX(), from.pos().getY(), from.pos().getZ());
                    this.tmpTeleport = true;
                    return InteractionResult.PASS;
                }
                int border = area.getBorder(true);
                int z1;
                if (z2 > border && this.tmpTeleport) {
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_BORDER)) {
                        to = GlobalPos.of(to.dimension(), new BlockPos(to.pos().getX(), to.pos().getY(), border - 1));
                        this.tmpTeleport = false;
                        player.teleportTo(to.pos().getX(), to.pos().getY(), to.pos().getZ());
                        this.tmpTeleport = true;
                        pp.sendMessage(TranslatableCaption.of("border.denied"));
                    } else if (MathMan.roundInt(from.pos().getZ()) <= border) { // Only send if they just moved out of the border
                        pp.sendMessage(TranslatableCaption.of("border.bypass.exited"));
                    }
                } else if (z2 < -border && this.tmpTeleport) {
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_BORDER)) {
                        to = GlobalPos.of(to.dimension(), new BlockPos(to.pos().getX(), to.pos().getY(), -border + 1));
                        this.tmpTeleport = false;
                        player.teleportTo(to.pos().getX(), to.pos().getY(), to.pos().getZ());
                        this.tmpTeleport = true;
                        pp.sendMessage(TranslatableCaption.of("border.denied"));
                    } else if (MathMan.roundInt(from.pos().getZ()) >= -border) { // Only send if they just moved out of the border
                        pp.sendMessage(TranslatableCaption.of("border.bypass.exited"));
                    }
                } else if (((z1 = MathMan.roundInt(from
                        .pos()
                        .getZ())) >= border && z2 <= border) || (z1 <= -border && z2 >= -border)) {
                    if (pp.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_BORDER)) {
                        pp.sendMessage(TranslatableCaption.of("border.bypass.entered"));
                    }
                }
            }
            return InteractionResult.PASS;
        });

        ServerPlayerTeleportToCallback.EVENT.register((serverLevel, x, y, z, set, g, h, serverPlayer) -> {
            ServerPlayer player = serverPlayer;
            //We need to account for bad plugins like NoCheatPlus that teleports player on/before login -_-
            if (!player.connection.isAcceptingMessages()) {
                return InteractionResult.FAIL;
            }
            FabricPlayer pp = FabricUtil.adapt(player);
            try (final MetaDataAccess<Plot> lastPlotAccess =
                         pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                Plot lastPlot = lastPlotAccess.get().orElse(null);
                GlobalPos to = GlobalPos.of(serverLevel.dimension(), new BlockPos((int) x, (int) y, (int) z));
                //noinspection ConstantConditions
                if (to != null) {
                    Location location = FabricUtil.adapt(to);
                    PlotArea area = location.getPlotArea();
                    if (area == null) {
                        if (lastPlot != null) {
                            plotListener.plotExit(pp, lastPlot);
                            lastPlotAccess.remove();
                        }
                        try (final MetaDataAccess<Location> lastLocationAccess =
                                     pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LOCATION)) {
                            lastLocationAccess.remove();
                        }
                        return InteractionResult.PASS;
                    }
                    Plot plot = area.getPlot(location);
                    if (plot != null) {
                        final boolean result = DenyTeleportFlag.allowsTeleport(pp, plot);
                        // there is one possibility to still allow teleportation:
                        // to is identical to the plot's home location, and untrusted-visit is true
                        // i.e. untrusted-visit can override deny-teleport
                        // this is acceptable, because otherwise it wouldn't make sense to have both flags set
                        if (!result && !(plot.getFlag(UntrustedVisitFlag.class) && plot
                                .getHomeSynchronous()
                                .equals(FabricUtil.adaptComplete(to, g, h)))) {
                            pp.sendMessage(
                                    TranslatableCaption.of("deny.no_enter"),
                                    TagResolver.resolver("plot", Tag.inserting(Component.text(plot.toString())))
                            );
                            return InteractionResult.FAIL;
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });

        HandleMoveVehicleCallback.EVENT.register((serverboundMoveVehiclePacket, serverPlayer) -> {
            final Vec3 from = serverPlayer.getRootVehicle().position();
            final Vec3 to = new Vec3(serverboundMoveVehiclePacket.getX(), serverboundMoveVehiclePacket.getY(),
                    serverboundMoveVehiclePacket.getZ()
            );

            int toX, toZ;
            if ((toX = MathMan.roundInt(to.x)) != MathMan.roundInt(from.x) | (toZ = MathMan.roundInt(to.z)) != MathMan
                    .roundInt(from.z)) {
                Entity vehicle = serverPlayer.getRootVehicle();

                // Check allowed
                if (!vehicle.getPassengers().isEmpty()) {
                    Entity passenger = vehicle.getPassengers().get(0);

                    if (passenger instanceof final ServerPlayer player) {
                        List<Entity> passengers = vehicle.getPassengers();
                        InteractionResult result =
                                HandlePlayerMoveCallback.EVENT
                                        .invoker()
                                        .handlePlayerMoveCallback(new ServerboundMovePlayerPacket.PosRot(to.x, to.y, to.z,
                                                serverboundMoveVehiclePacket.getXRot(), serverboundMoveVehiclePacket.getYRot(),
                                                serverPlayer.onGround()
                                        ), serverPlayer);
                        Vec3 dest;
                        if (result == InteractionResult.FAIL) {
                            dest = from;
                        } else if (MathMan.roundInt(to.x) != toX || MathMan.roundInt(to.z) != toZ) {
                            dest = to;
                        } else {
                            dest = null;
                        }
                        if (dest != null) {
                            vehicle.ejectPassengers();
                            vehicle.setDeltaMovement(new Vec3(0d, 0d, 0d));
                            vehicle.teleportTo(dest.x, dest.y, dest.z);
                            passengers.forEach(entity -> entity.startRiding(vehicle));
                            return InteractionResult.PASS;
                        }
                    }
                    if (Settings.Enabled_Components.KILL_ROAD_VEHICLES) {
                        final com.sk89q.worldedit.world.entity.EntityType entityType =
                                EntityTypes.get(BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()).toString());
                        // Horses etc are vehicles, but they're also animals
                        // so this filters out all living entities
                        if (EntityCategories.VEHICLE.contains(entityType) && !EntityCategories.ANIMAL.contains(entityType)) {

                            Plot toPlot =
                                    FabricUtil.adapt(GlobalPos.of(serverPlayer.serverLevel().dimension(), new BlockPos(
                                            (int) to.x,
                                            (int) to.y,
                                            (int) to.z
                                    ))).getPlot();
                            if (vehicle.hasAttached(PLOT_DATA)) {
                                Plot origin = vehicle.getAttached(PLOT_DATA);
                                if (origin != null && !origin.getBasePlot(false).equals(toPlot)) {
                                    vehicle.remove(Entity.RemovalReason.DISCARDED);
                                }
                            } else if (toPlot != null) {
                                vehicle.setAttached(PLOT_DATA, toPlot);
                            }
                        }
                    }
                }

            }
            return InteractionResult.PASS;
        });

        Stimuli.global().listen(PlayerChatEvent.EVENT, (serverPlayer, playerChatMessage, bound) -> {
            FabricPlayer plotPlayer = FabricUtil.adapt(serverPlayer);
            Location location = plotPlayer.getLocation();
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return InteractionResult.PASS;
            }
            Plot plot = area.getPlot(location);
            if (plot == null) {
                return InteractionResult.PASS;
            }
            if (!((plot.getFlag(ChatFlag.class) && area.isPlotChat() && plotPlayer.getAttribute("chat"))
                    || area.isForcingPlotChat())) {
                return InteractionResult.FAIL;
            }
            if (plot.isDenied(plotPlayer.getUUID()) && !plotPlayer.hasPermission(Permission.PERMISSION_ADMIN_CHAT_BYPASS)) {
                return InteractionResult.FAIL;
            }
            /*
            event.setCancelled(true);
            Set<Player> recipients = event.getRecipients();
            recipients.clear();*/
            Set<PlotPlayer<?>> spies = new HashSet<>();
            Set<PlotPlayer<?>> plotRecipients = new HashSet<>();
            for (final PlotPlayer<?> pp : PlotSquared.platform().playerManager().getPlayers()) {
                if (pp.getAttribute("chatspy")) {
                    spies.add(pp);
                } else {
                    Plot current = pp.getCurrentPlot();
                    if (current != null && current.getBasePlot(false).equals(plot)) {
                        plotRecipients.add(pp);
                    }
                }
            }
            String message = playerChatMessage.message();
            String sender = serverPlayer.getDisplayName().getString();
            PlotId id = plot.getId();
            String worldName = plot.getWorldName();
            Caption msg = TranslatableCaption.of("chat.plot_chat_format");
            TagResolver.Builder builder = TagResolver.builder();
            builder.tag("world", Tag.inserting(Component.text(worldName)));
            builder.tag("plot_id", Tag.inserting(Component.text(id.toString())));
            builder.tag("sender", Tag.inserting(Component.text(sender)));
            if (plotPlayer.hasPermission("plots.chat.color")) {
                builder.tag("msg", Tag.inserting(MiniMessage.miniMessage().deserialize(
                        message,
                        TagResolver.resolver(StandardTags.color(), StandardTags.gradient(),
                                StandardTags.rainbow(), StandardTags.decorations()
                        )
                )));
            } else {
                builder.tag("msg", Tag.inserting(Component.text(message)));
            }
            for (PlotPlayer<?> receiver : plotRecipients) {
                receiver.sendMessage(msg, builder.build());
            }
            if (!spies.isEmpty()) {
                Caption spymsg = TranslatableCaption.of("chat.plot_chat_spy_format");
                for (PlotPlayer<?> player : spies) {
                    player.sendMessage(spymsg, builder.tag("message", Tag.inserting(Component.text(message))).build());
                }
            }
            if (Settings.Chat.LOG_PLOTCHAT_TO_CONSOLE) {
                Caption spymsg = TranslatableCaption.of("chat.plot_chat_spy_format");
                ConsolePlayer.getConsole().sendMessage(
                        spymsg,
                        builder.tag("message", Tag.inserting(Component.text(message))).build()
                );
            }
            //cancel the original message
            return InteractionResult.FAIL;
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            FabricPlayer pp = FabricUtil.adapt(player);
            // Delete last location
            Plot plot;
            try (final MetaDataAccess<Plot> lastPlotAccess =
                         pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                plot = lastPlotAccess.remove();
            }
            try (final MetaDataAccess<Location> lastLocationAccess =
                         pp.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LOCATION)) {
                lastLocationAccess.remove();
            }
            if (plot != null) {
                plotListener.plotExit(pp, plot);
            }
            if (this.worldEdit != null) {
                if (!pp.hasPermission(Permission.PERMISSION_WORLDEDIT_BYPASS)) {
                    if (pp.getAttribute("worldedit")) {
                        pp.removeAttribute("worldedit");
                    }
                }
            }
            Location location = pp.getLocation();
            PlotArea area = location.getPlotArea();
            if (location.isPlotArea()) {
                plot = location.getPlot();
                if (plot != null) {
                    plotListener.plotEntry(pp, plot);
                }
            }
        });

        Stimuli.global().listen(PlayerInventoryActionEvent.EVENT, (serverPlayer, i, clickType, i1) -> {
        /*if (!event.isLeftClick() || (event.getAction() != InventoryAction.PLACE_ALL) || event
            .isShiftClick()) {
            return;
        }*/
            if (!this.plotAreaManager
                    .hasPlotArea(serverPlayer.serverLevel().serverLevelData.getLevelName())) {
                return InteractionResult.SUCCESS;
            }

            FabricPlayer pp = FabricUtil.adapt(serverPlayer);
            final PlotInventory inventory = PlotInventory.getOpenPlotInventory(pp);
            if (inventory != null) {
                if (!inventory.onClick(i)) {
                    inventory.close();
                    return InteractionResult.FAIL;
                }
            }
            Inventory inv = serverPlayer.getInventory();
            int slot = inv.selected;
            if ((slot > 8) || !serverPlayer.isCreative()) {
                return InteractionResult.PASS;
            }
            ItemStack oldItem = serverPlayer.getMainHandItem();

            List<String> oldMeta = getLore(oldItem);
            ItemStack newItem = inv.getSelected();
            List<String> newMeta = getLore(newItem);

            if (clickType == ClickType.CLONE) {
                final Plot plot = pp.getCurrentPlot();
                if (plot != null) {
                    if (plot.getFlag(PreventCreativeCopyFlag.class) && !plot
                            .isAdded(serverPlayer.getUUID()) && !pp.hasPermission(Permission.PERMISSION_ADMIN_INTERACT_OTHER)) {
                        final ItemStack newStack =
                                new ItemStack(newItem.getItem(), newItem.getCount());
                        inv.setItem(slot, newStack);
                        plot.debug(serverPlayer.getName()
                                + " could not creative-copy an item because prevent-creative-copy = true");
                    }
                } else {
                    PlotArea area = pp.getPlotAreaAbs();
                    if (area != null && PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, PreventCreativeCopyFlag.class, true)) {
                        final ItemStack newStack =
                                new ItemStack(newItem.getItem(), newItem.getCount());
                        inv.setItem(slot, newStack);
                    }
                }
                return InteractionResult.PASS;
            }

            String newLore = "";
            if (newMeta != null) {
                List<String> lore = newMeta;
                if (lore != null) {
                    newLore = lore.toString();
                }
            }
            String oldLore = "";
            if (oldMeta != null) {
                List<String> lore = oldMeta;
                if (lore != null) {
                    oldLore = lore.toString();
                }
            }
            Item itemType = newItem.getItem();
            if (!"[(+NBT)]".equals(newLore) || (oldItem.equals(newItem) && newLore.equals(oldLore))) {
                if (newMeta == null || (itemType != Items.PLAYER_HEAD)) {
                    return InteractionResult.PASS;
                }
            }
            HitResult hit = serverPlayer.pick(7, 1, false);
            BlockState state = serverPlayer.serverLevel().getBlockState(new BlockPos(
                    (int) hit.getLocation().x,
                    (int) hit.getLocation().y,
                    (int) hit.getLocation().z
            ));
            Block stateType = state.getBlock();
            if (stateType.asItem() != itemType) {
                return InteractionResult.FAIL;
            }
            Location location = FabricUtil.adapt(GlobalPos.of(serverPlayer.serverLevel().dimension(), new BlockPos(
                    (int) hit.getLocation().x,
                    (int) hit.getLocation().y,
                    (int) hit.getLocation().z
            )));
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return InteractionResult.PASS;
            }
            Plot plot = area.getPlotAbs(location);
            boolean cancelled = false;
            if (plot == null) {
                if (!pp.hasPermission(Permission.PERMISSION_ADMIN_INTERACT_ROAD)) {
                    pp.sendMessage(
                            TranslatableCaption.of("permission.no_permission_event"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ADMIN_INTERACT_ROAD)
                            )
                    );
                    cancelled = true;
                }
            } else if (!plot.hasOwner()) {
                if (!pp.hasPermission(Permission.PERMISSION_ADMIN_INTERACT_UNOWNED)) {
                    pp.sendMessage(
                            TranslatableCaption.of("permission.no_permission_event"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ADMIN_INTERACT_UNOWNED)
                            )
                    );
                    cancelled = true;
                }
            } else {
                UUID uuid = pp.getUUID();
                if (!plot.isAdded(uuid)) {
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_INTERACT_OTHER)) {
                        pp.sendMessage(
                                TranslatableCaption.of("permission.no_permission_event"),
                                TagResolver.resolver(
                                        "node",
                                        Tag.inserting(Permission.PERMISSION_ADMIN_INTERACT_OTHER)
                                )
                        );
                        cancelled = true;
                    }
                }
            }
            if (cancelled) {
                ItemStack newItemCopy = newItem.copyWithCount(newItem.getCount());
                newItemCopy.setDamageValue(newItem.getDamageValue());
                if ((oldItem.getItem() == newItem.getItem()) && (oldItem.getDamageValue()) == newItem
                        .getDamageValue()) {
                    serverPlayer.inventoryMenu.setCarried(newItemCopy);
                    return InteractionResult.SUCCESS;
                }
                serverPlayer.inventoryMenu.setCarried(
                        newItemCopy);
            }
            return InteractionResult.PASS;
        });

        Stimuli.global().listen(EntityUseEvent.EVENT, (serverPlayer, entity, interactionHand, entityHitResult) -> {
            if (!(entity instanceof ArmorStand) && !(entity instanceof ItemFrame)) {
                return InteractionResult.PASS;
            }
            Location location = FabricUtil.adapt(GlobalPos.of(entity.level().dimension(), entity.blockPosition()));
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return InteractionResult.PASS;
            }
            EntitySpawnListener.testNether(entity);
            Plot plot = location.getPlotAbs();
            BukkitPlayer pp = BukkitUtil.adapt(e.getPlayer());
            if (plot == null) {
                if (!PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, MiscInteractFlag.class, true) && !pp.hasPermission(
                        Permission.PERMISSION_ADMIN_INTERACT_ROAD
                )) {
                    pp.sendMessage(
                            TranslatableCaption.of("permission.no_permission_event"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ADMIN_INTERACT_ROAD)
                            )
                    );
                    e.setCancelled(true);
                }
            } else {
                if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                        pp.sendMessage(TranslatableCaption.of("done.building_restricted"));
                        e.setCancelled(true);
                        return;
                    }
                }
                if (!plot.hasOwner()) {
                    if (!pp.hasPermission("plots.admin.interact.unowned")) {
                        pp.sendMessage(
                                TranslatableCaption.of("permission.no_permission_event"),
                                TagResolver.resolver(
                                        "node",
                                        Tag.inserting(Permission.PERMISSION_ADMIN_INTERACT_UNOWNED)
                                )
                        );
                        e.setCancelled(true);
                    }
                } else {
                    UUID uuid = pp.getUUID();
                    if (plot.isAdded(uuid)) {
                        return;
                    }
                    if (plot.getFlag(MiscInteractFlag.class)) {
                        return;
                    }
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_INTERACT_OTHER)) {
                        pp.sendMessage(
                                TranslatableCaption.of("permission.no_permission_event"),
                                TagResolver.resolver(
                                        "node",
                                        Tag.inserting(Permission.PERMISSION_ADMIN_INTERACT_OTHER)
                                )
                        );
                        e.setCancelled(true);
                        plot.debug(pp.getName() + " could not interact with " + entity.getType()
                                + " because misc-interact = false");
                    }
                }
            }
            return InteractionResult.PASS;
        });

    }

    public List<String> getLore(ItemStack stack) {
        if (stack.hasTag() && stack.getOrCreateTag().contains("display", CompoundTag.TAG_COMPOUND)) {
            CompoundTag displayTag = stack.getTag().getCompound("display");
            if (displayTag.contains("Lore", ListTag.TAG_LIST)) {
                ListTag loreList = displayTag.getList("Lore", StringTag.TAG_STRING);
                List<String> lore = new ArrayList<>();
                for (int i = 0; i < loreList.size(); i++) {
                    lore.add(loreList.getString(i));
                }
                return lore;
            }
        }
        return Collections.emptyList();
    }

    public void setLore(ItemStack stack, List<String> lore) {
        CompoundTag displayTag;
        if (stack.hasTag() && stack.getOrCreateTag().contains("display", CompoundTag.TAG_COMPOUND)) {
            displayTag = stack.getTag().getCompound("display");
        } else {
            displayTag = new CompoundTag();
            stack.getOrCreateTag().put("display", displayTag);
        }

        ListTag loreList = new ListTag();
        for (String line : lore) {
            loreList.add(StringTag.valueOf(line));
        }
        displayTag.put("Lore", loreList);
    }

    @EventHandler(priority = EventPriority.LOW)
    @SuppressWarnings("deprecation") // Paper deprecation
    public void onCancelledInteract(PlayerInteractEvent event) {
        if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = event.getPlayer();
            BukkitPlayer pp = BukkitUtil.adapt(player);
            PlotArea area = pp.getPlotAreaAbs();
            if (area == null) {
                return;
            }
            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                Material item = event.getMaterial();
                if (item.toString().toLowerCase().endsWith("_egg")) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                }
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            Material type = hand.getType();
            Material offType = offHand.getType();
            if (type == Material.AIR) {
                type = offType;
            }
            if (type.toString().toLowerCase().endsWith("_egg")) {
                Block block = player.getTargetBlockExact(5, FluidCollisionMode.SOURCE_ONLY);
                if (block != null && block.getType() != Material.AIR) {
                    Location location = BukkitUtil.adapt(block.getLocation());
                    if (!this.eventDispatcher.checkPlayerBlockEvent(pp, PlayerBlockEventType.SPAWN_MOB, location, null, true)) {
                        event.setCancelled(true);
                        event.setUseItemInHand(Event.Result.DENY);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.adapt(player);
        PlotArea area = pp.getPlotAreaAbs();
        if (area == null) {
            return;
        }
        PlayerBlockEventType eventType;
        BlockType blocktype1;
        Block block = event.getClickedBlock();
        if (block == null) {
            // We do not care in this case, the player is likely interacting with air ("nothing").
            return;
        }
        Location location = BukkitUtil.adapt(block.getLocation());
        Action action = event.getAction();
        switch (action) {
            case PHYSICAL -> {
                eventType = PlayerBlockEventType.TRIGGER_PHYSICAL;
                blocktype1 = BukkitAdapter.asBlockType(block.getType());
            }

            //todo rearrange the right click code. it is all over the place.
            case RIGHT_CLICK_BLOCK -> {
                Material blockType = block.getType();
                eventType = PlayerBlockEventType.INTERACT_BLOCK;
                blocktype1 = BukkitAdapter.asBlockType(block.getType());

                if (blockType.isInteractable()) {
                    if (!player.isSneaking()) {
                        break;
                    }
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    ItemStack offHand = player.getInventory().getItemInOffHand();

                    // sneaking players interact with blocks if both hands are empty
                    if (hand.getType() == Material.AIR && offHand.getType() == Material.AIR) {
                        break;
                    }
                }

                Material type = event.getMaterial();

                // in the following, lb needs to have the material of the item in hand i.e. type
                switch (type.toString()) {
                    case "REDSTONE", "STRING", "PUMPKIN_SEEDS", "MELON_SEEDS", "COCOA_BEANS", "WHEAT_SEEDS", "BEETROOT_SEEDS",
                            "SWEET_BERRIES", "GLOW_BERRIES" -> {
                        return;
                    }
                    default -> {
                        //eventType = PlayerBlockEventType.PLACE_BLOCK;
                        if (type.isBlock()) {
                            return;
                        }
                    }
                }
                if (PaperLib.isPaper()) {
                    if (MaterialTags.SPAWN_EGGS.isTagged(type) || Material.EGG.equals(type)) {
                        eventType = PlayerBlockEventType.SPAWN_MOB;
                        break;
                    }
                } else {
                    if (type.toString().toLowerCase().endsWith("egg")) {
                        eventType = PlayerBlockEventType.SPAWN_MOB;
                        break;
                    }
                }
                if (type.isEdible()) {
                    //Allow all players to eat while also allowing the block place event to be fired
                    return;
                }
                if (type == Material.ARMOR_STAND) {
                    location = BukkitUtil.adapt(block.getRelative(event.getBlockFace()).getLocation());
                    eventType = PlayerBlockEventType.PLACE_MISC;
                }
                if (org.bukkit.Tag.ITEMS_BOATS.isTagged(type) || MINECARTS.contains(type)) {
                    eventType = PlayerBlockEventType.PLACE_VEHICLE;
                    break;
                }
                if (type == Material.FIREWORK_ROCKET || type == Material.FIREWORK_STAR) {
                    eventType = PlayerBlockEventType.SPAWN_MOB;
                    break;
                }
                if (BOOKS.contains(type)) {
                    eventType = PlayerBlockEventType.READ;
                    break;
                }
            }
            case LEFT_CLICK_BLOCK -> {
                Material blockType = block.getType();

                // todo: when the code above is rearranged, it would be great to beautify this as well.
                // will code this as a temporary, specific bug fix (for dragon eggs)
                if (blockType != Material.DRAGON_EGG) {
                    return;
                }

                eventType = PlayerBlockEventType.INTERACT_BLOCK;
                blocktype1 = BukkitAdapter.asBlockType(block.getType());
            }
            default -> {
                return;
            }
        }
        if (this.worldEdit != null && pp.getAttribute("worldedit")) {
            if (event.getMaterial() == Material.getMaterial(this.worldEdit.getConfiguration().wandItem)) {
                return;
            }
        }
        if (!this.eventDispatcher.checkPlayerBlockEvent(pp, eventType, location, blocktype1, true)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }

    // Boats can sometimes be placed on interactable blocks such as levers,
    // see PS-175. Armor stands, minecarts and end crystals (the other entities
    // supported by this event) don't have this issue.
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBoatPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Entity placed = event.getEntity();
        if (!(placed instanceof Boat)) {
            return;
        }
        BukkitPlayer pp = BukkitUtil.adapt(event.getPlayer());
        PlotArea area = pp.getPlotAreaAbs();
        if (area == null) {
            return;
        }
        PlayerBlockEventType eventType = PlayerBlockEventType.PLACE_VEHICLE;
        Block block = event.getBlock();
        BlockType blockType = BukkitAdapter.asBlockType(block.getType());
        Location location = BukkitUtil.adapt(block.getLocation());
        if (!PlotSquared.get().getEventDispatcher()
                .checkPlayerBlockEvent(pp, eventType, location, blockType, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        BlockFace bf = event.getBlockFace();
        // Note: a month after Bukkit 1.14.4 released, they added the API method
        // PlayerBucketEmptyEvent#getBlock(), which returns the block the
        // bucket contents is going to be placed at. Currently we determine this
        // block ourselves to retain compatibility with 1.13.
        final Block block;
        // if the block can be waterlogged, the event might waterlog the block
        // sometimes
        if (event.getBlockClicked().getBlockData() instanceof Waterlogged waterlogged
                && !waterlogged.isWaterlogged() && event.getBucket() != Material.LAVA_BUCKET) {
            block = event.getBlockClicked();
        } else {
            block = event.getBlockClicked().getLocation()
                    .add(bf.getModX(), bf.getModY(), bf.getModZ())
                    .getBlock();
        }
        Location location = BukkitUtil.adapt(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        BukkitPlayer pp = BukkitUtil.adapt(event.getPlayer());
        Plot plot = area.getPlot(location);
        if (plot == null) {
            if (pp.hasPermission(Permission.PERMISSION_ADMIN_BUILD_ROAD)) {
                return;
            }
            pp.sendMessage(
                    TranslatableCaption.of("permission.no_permission_event"),
                    TagResolver.resolver("node", Tag.inserting(Permission.PERMISSION_ADMIN_BUILD_ROAD))
            );
            event.setCancelled(true);
        } else if (!plot.hasOwner()) {
            if (pp.hasPermission(Permission.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                return;
            }
            pp.sendMessage(
                    TranslatableCaption.of("permission.no_permission_event"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ADMIN_BUILD_UNOWNED)
                    )
            );
            event.setCancelled(true);
        } else if (!plot.isAdded(pp.getUUID())) {
            if (pp.hasPermission(Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            }
            pp.sendMessage(
                    TranslatableCaption.of("permission.no_permission_event"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ADMIN_BUILD_OTHER)
                    )
            );
            event.setCancelled(true);
        } else if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
            if (!pp.hasPermission(Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                pp.sendMessage(
                        TranslatableCaption.of("done.building_restricted")
                );
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity closer = event.getPlayer();
        if (!(closer instanceof Player player)) {
            return;
        }
        PlotInventory.removePlotInventoryOpen(BukkitUtil.adapt(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event) {
        TaskManager.removeFromTeleportQueue(event.getPlayer().getName());
        BukkitPlayer pp = BukkitUtil.adapt(event.getPlayer());
        pp.unregister();
        plotListener.logout(pp.getUUID());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Block blockClicked = event.getBlockClicked();
        Location location = BukkitUtil.adapt(blockClicked.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        BukkitPlayer plotPlayer = BukkitUtil.adapt(player);
        Plot plot = area.getPlot(location);
        if (plot == null) {
            if (plotPlayer.hasPermission(Permission.PERMISSION_ADMIN_BUILD_ROAD)) {
                return;
            }
            plotPlayer.sendMessage(
                    TranslatableCaption.of("permission.no_permission_event"),
                    TagResolver.resolver("node", Tag.inserting(Permission.PERMISSION_ADMIN_BUILD_ROAD))
            );
            event.setCancelled(true);
        } else if (!plot.hasOwner()) {
            if (plotPlayer.hasPermission(Permission.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                return;
            }
            plotPlayer.sendMessage(
                    TranslatableCaption.of("permission.no_permission_event"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ADMIN_BUILD_UNOWNED)
                    )
            );
            event.setCancelled(true);
        } else if (!plot.isAdded(plotPlayer.getUUID())) {
            if (plotPlayer.hasPermission(Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            }
            plotPlayer.sendMessage(
                    TranslatableCaption.of("permission.no_permission_event"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ADMIN_BUILD_OTHER)
                    )
            );
            event.setCancelled(true);
        } else if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
            if (!plotPlayer.hasPermission(Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                plotPlayer.sendMessage(
                        TranslatableCaption.of("done.building_restricted")
                );
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Block block = event.getBlock().getRelative(event.getBlockFace());
        Location location = BukkitUtil.adapt(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player p = event.getPlayer();
        if (p == null) {
            event.setCancelled(true);
            return;
        }
        BukkitPlayer pp = BukkitUtil.adapt(p);
        Plot plot = area.getPlot(location);
        if (plot == null) {
            if (!pp.hasPermission(Permission.PERMISSION_ADMIN_BUILD_ROAD)) {
                pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        TagResolver.resolver(
                                "node",
                                Tag.inserting(Permission.PERMISSION_ADMIN_BUILD_ROAD)
                        )
                );
                event.setCancelled(true);
            }
        } else {
            if (!plot.hasOwner()) {
                if (!pp.hasPermission(Permission.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    pp.sendMessage(
                            TranslatableCaption.of("permission.no_permission_event"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ADMIN_BUILD_UNOWNED)
                            )
                    );
                    event.setCancelled(true);
                }
                return;
            }
            if (!plot.isAdded(pp.getUUID())) {
                if (!plot.getFlag(HangingPlaceFlag.class)) {
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                        pp.sendMessage(
                                TranslatableCaption.of("permission.no_permission_event"),
                                TagResolver.resolver(
                                        "node",
                                        Tag.inserting(Permission.PERMISSION_ADMIN_BUILD_OTHER)
                                )
                        );
                        event.setCancelled(true);
                    }
                    return;
                }
            }
            if (BukkitEntityUtil.checkEntity(event.getEntity(), plot)) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (remover instanceof Player p) {
            Location location = BukkitUtil.adapt(event.getEntity().getLocation());
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return;
            }
            BukkitPlayer pp = BukkitUtil.adapt(p);
            Plot plot = area.getPlot(location);
            if (plot == null) {
                if (!pp.hasPermission(Permission.PERMISSION_ADMIN_DESTROY_ROAD)) {
                    pp.sendMessage(
                            TranslatableCaption.of("permission.no_permission_event"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ADMIN_DESTROY_ROAD)
                            )
                    );
                    event.setCancelled(true);
                }
            } else if (!plot.hasOwner()) {
                if (!pp.hasPermission(Permission.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    pp.sendMessage(
                            TranslatableCaption.of("permission.no_permission_event"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ADMIN_DESTROY_UNOWNED)
                            )
                    );
                    event.setCancelled(true);
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                if (plot.getFlag(HangingBreakFlag.class)) {
                    return;
                }
                if (!pp.hasPermission(Permission.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    pp.sendMessage(
                            TranslatableCaption.of("permission.no_permission_event"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ADMIN_DESTROY_OTHER)
                            )
                    );
                    event.setCancelled(true);
                    plot.debug(p.getName()
                            + " could not break hanging entity because hanging-break = false");
                }
            }
        } else if (remover instanceof Projectile p) {
            if (p.getShooter() instanceof Player shooter) {
                Location location = BukkitUtil.adapt(event.getEntity().getLocation());
                PlotArea area = location.getPlotArea();
                if (area == null) {
                    return;
                }
                BukkitPlayer player = BukkitUtil.adapt(shooter);
                Plot plot = area.getPlot(BukkitUtil.adapt(event.getEntity().getLocation()));
                if (plot != null) {
                    if (!plot.hasOwner()) {
                        if (!player.hasPermission(Permission.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                            player.sendMessage(
                                    TranslatableCaption.of("permission.no_permission_event"),
                                    TagResolver.resolver(
                                            "node",
                                            Tag.inserting(Permission.PERMISSION_ADMIN_DESTROY_UNOWNED)
                                    )
                            );
                            event.setCancelled(true);
                        }
                    } else if (!plot.isAdded(player.getUUID())) {
                        if (!plot.getFlag(HangingBreakFlag.class)) {
                            if (!player.hasPermission(Permission.PERMISSION_ADMIN_DESTROY_OTHER)) {
                                player.sendMessage(
                                        TranslatableCaption.of("permission.no_permission_event"),
                                        TagResolver.resolver(
                                                "node",
                                                Tag.inserting(Permission.PERMISSION_ADMIN_DESTROY_OTHER)
                                        )
                                );
                                event.setCancelled(true);
                                plot.debug(player.getName()
                                        + " could not break hanging entity because hanging-break = false");
                            }
                        }
                    }
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.UNKNOWN) {
            return;
        }
        Location location = BukkitUtil.adapt(event.getRightClicked().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player p = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.adapt(p);
        Plot plot = area.getPlot(location);
        if (plot == null && !area.isRoadFlags()) {
            if (!pp.hasPermission(Permission.PERMISSION_ADMIN_INTERACT_ROAD)) {
                pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        TagResolver.resolver(
                                "node",
                                Tag.inserting(Permission.PERMISSION_ADMIN_INTERACT_ROAD)
                        )
                );
                event.setCancelled(true);
            }
        } else if (plot != null && !plot.hasOwner()) {
            if (!pp.hasPermission(Permission.PERMISSION_ADMIN_INTERACT_UNOWNED)) {
                pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        TagResolver.resolver(
                                "node",
                                Tag.inserting(Permission.PERMISSION_ADMIN_INTERACT_UNOWNED)
                        )
                );
                event.setCancelled(true);
            }
        } else if ((plot != null && !plot.isAdded(pp.getUUID())) || (plot == null && area
                .isRoadFlags())) {
            final Entity entity = event.getRightClicked();
            final com.sk89q.worldedit.world.entity.EntityType entityType =
                    BukkitAdapter.adapt(entity.getType());

            FlagContainer flagContainer;
            if (plot == null) {
                flagContainer = area.getRoadFlagContainer();
            } else {
                flagContainer = plot.getFlagContainer();
            }

            if (EntityCategories.HOSTILE.contains(entityType) && flagContainer
                    .getFlag(HostileInteractFlag.class).getValue()) {
                return;
            }

            if (EntityCategories.ANIMAL.contains(entityType) && flagContainer
                    .getFlag(AnimalInteractFlag.class).getValue()) {
                return;
            }

            // This actually makes use of the interface, so we don't use the
            // category
            if (entity instanceof Tameable && ((Tameable) entity).isTamed() && flagContainer
                    .getFlag(TamedInteractFlag.class).getValue()) {
                return;
            }

            if (EntityCategories.VEHICLE.contains(entityType) && flagContainer
                    .getFlag(VehicleUseFlag.class).getValue()) {
                return;
            }

            if (EntityCategories.PLAYER.contains(entityType) && flagContainer
                    .getFlag(PlayerInteractFlag.class).getValue()) {
                return;
            }

            if (EntityCategories.VILLAGER.contains(entityType) && flagContainer
                    .getFlag(VillagerInteractFlag.class).getValue()) {
                return;
            }

            if ((EntityCategories.HANGING.contains(entityType) || EntityCategories.OTHER
                    .contains(entityType)) && flagContainer.getFlag(MiscInteractFlag.class)
                    .getValue()) {
                return;
            }

            if (!pp.hasPermission(Permission.PERMISSION_ADMIN_INTERACT_OTHER)) {
                pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        TagResolver.resolver(
                                "node",
                                Tag.inserting(Permission.PERMISSION_ADMIN_INTERACT_OTHER)
                        )
                );
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Location location = BukkitUtil.adapt(event.getVehicle().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Entity attacker = event.getAttacker();
        if (attacker instanceof Player p) {
            BukkitPlayer pp = BukkitUtil.adapt(p);
            Plot plot = area.getPlot(location);
            if (plot == null) {
                if (!PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, VehicleBreakFlag.class, true) && !pp.hasPermission(
                        Permission.PERMISSION_ADMIN_DESTROY_VEHICLE_ROAD
                )) {
                    pp.sendMessage(
                            TranslatableCaption.of("permission.no_permission_event"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ADMIN_DESTROY_VEHICLE_ROAD)
                            )
                    );
                    event.setCancelled(true);
                }
            } else {
                if (!plot.hasOwner()) {
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_DESTROY_VEHICLE_UNOWNED)) {
                        pp.sendMessage(
                                TranslatableCaption.of("permission.no_permission_event"),
                                TagResolver.resolver(
                                        "node",
                                        Tag.inserting(Permission.PERMISSION_ADMIN_DESTROY_VEHICLE_UNOWNED)
                                )
                        );
                        event.setCancelled(true);
                        return;
                    }
                    return;
                }
                if (!plot.isAdded(pp.getUUID())) {
                    if (plot.getFlag(VehicleBreakFlag.class)) {
                        return;
                    }
                    if (!pp.hasPermission(Permission.PERMISSION_ADMIN_DESTROY_VEHICLE_OTHER)) {
                        pp.sendMessage(
                                TranslatableCaption.of("permission.no_permission_event"),
                                TagResolver.resolver(
                                        "node",
                                        Tag.inserting(Permission.PERMISSION_ADMIN_DESTROY_VEHICLE_OTHER)
                                )
                        );
                        event.setCancelled(true);
                        plot.debug(pp.getName()
                                + " could not break vehicle because vehicle-break = false");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.adapt(player);
        Location location = pp.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, ItemDropFlag.class, false)) {
                event.setCancelled(true);
            }
            return;
        }
        UUID uuid = pp.getUUID();
        if (!plot.isAdded(uuid)) {
            if (!plot.getFlag(ItemDropFlag.class)) {
                plot.debug(player.getName() + " could not drop item because of item-drop = false");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        LivingEntity ent = event.getEntity();
        if (ent instanceof Player player) {
            BukkitPlayer pp = BukkitUtil.adapt(player);
            Location location = pp.getLocation();
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return;
            }
            Plot plot = location.getOwnedPlot();
            if (plot == null) {
                if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, DropProtectionFlag.class, true)) {
                    event.setCancelled(true);
                }
                return;
            }
            UUID uuid = pp.getUUID();
            if (!plot.isAdded(uuid) && plot.getFlag(DropProtectionFlag.class)) {
                plot.debug(player.getName() + " could not pick up item because of drop-protection = true");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        Location location = BukkitUtil.adapt(event.getEntity().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, KeepInventoryFlag.class, true)) {
                event.setCancelled(true);
            }
            return;
        }
        if (plot.getFlag(KeepInventoryFlag.class)) {
            plot.debug(event.getEntity().getName() + " kept their inventory because of keep-inventory = true");
            event.getDrops().clear();
            event.setKeepInventory(true);
        }
    }

    @SuppressWarnings("deprecation") // #getLocate is needed for Spigot compatibility
    @EventHandler
    public void onLocaleChange(final PlayerLocaleChangeEvent event) {
        // The event is fired before the player is deemed online upon login
        if (!event.getPlayer().isOnline()) {
            return;
        }
        BukkitPlayer player = BukkitUtil.adapt(event.getPlayer());
        // we're stripping the country code as we don't want to differ between countries
        player.setLocale(Locale.forLanguageTag(event.getLocale().substring(0, 2)));
    }

    @EventHandler
    public void onPortalEnter(PlayerPortalEvent event) {
        Location location = BukkitUtil.adapt(event.getPlayer().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, DenyPortalTravelFlag.class, true)) {
                event.setCancelled(true);
            }
            return;
        }
        if (plot.getFlag(DenyPortalTravelFlag.class)) {
            plot.debug(event.getPlayer().getName() + " did not travel thru a portal because of deny-portal-travel = true");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortalCreation(PortalCreateEvent event) {
        String world = event.getWorld().getName();
        if (PlotSquared.get().getPlotAreaManager().getPlotAreasSet(world).size() == 0) {
            return;
        }
        BukkitPlayer pp = (event.getEntity() instanceof Player player) ? BukkitUtil.adapt(player) : null;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockState state : event.getBlocks()) {
            minX = Math.min(state.getX(), minX);
            maxX = Math.max(state.getX(), maxX);
            minZ = Math.min(state.getZ(), minZ);
            maxZ = Math.max(state.getZ(), maxZ);
        }
        int y = event.getBlocks().get(0).getY(); // Don't need to worry about this too much
        for (Location location : List.of( // We don't care about duplicate locations
                Location.at(world, minX, y, minZ),
                Location.at(world, minX, y, maxZ),
                Location.at(world, maxX, y, minZ),
                Location.at(world, maxX, y, maxZ)
        )) {
            PlotArea area = location.getPlotArea();
            if (area == null) {
                continue;
            }
            if (area.notifyIfOutsideBuildArea(pp, location.getY())) {
                event.setCancelled(true);
                return;
            }
            Plot plot = location.getOwnedPlot();
            if (plot == null) {
                if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, DenyPortalsFlag.class, true)) {
                    event.setCancelled(true);
                    return;
                }
                continue;
            }
            if (plot.getFlag(DenyPortalsFlag.class)) {
                StringBuilder builder = new StringBuilder();
                if (event.getEntity() != null) {
                    builder.append(event.getEntity().getName()).append(" did not create a portal");
                } else {
                    builder.append("Portal creation cancelled");
                }
                plot.debug(builder.append(" because of deny-portals = true").toString());
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Player player = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.adapt(player);
        Location location = pp.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, LecternReadBookFlag.class, true)) {
                event.setCancelled(true);
            }
            return;
        }
        if (!plot.isAdded(pp.getUUID())) {
            if (plot.getFlag(LecternReadBookFlag.class)) {
                plot.debug(event.getPlayer().getName() + " could not take the book because of lectern-read-book = true");
                event.setCancelled(true);
            }
        }
    }

}
