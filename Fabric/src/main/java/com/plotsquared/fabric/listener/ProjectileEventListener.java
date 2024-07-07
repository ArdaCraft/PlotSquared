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

import com.google.inject.Inject;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotHandler;
import com.plotsquared.core.plot.flag.implementations.FishingFlag;
import com.plotsquared.core.plot.flag.implementations.ProjectilesFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.PlotFlagUtil;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.phys.BlockHitResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.projectile.ProjectileHitEvent;

@SuppressWarnings("unused")
public class ProjectileEventListener {

    private final PlotAreaManager plotAreaManager;

    @Inject
    public ProjectileEventListener(final @NonNull PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
        Stimuli.global().listen(ProjectileHitEvent.BLOCK, this::onProjectileHit);
        //Stimuli.global().listen(ProjectileHitEvent.ENTITY, this::onPotionSplash);
    }
/*
    public InteractionResult onPotionSplash() {
        ThrownPotion damager = event.getPotion();
        Location location = FabricUtil.adapt(damager.getLocation());
        if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
            return;
        }
        int count = 0;
        for (LivingEntity victim : event.getAffectedEntities()) {
            if (!BukkitEntityUtil.entityDamage(damager, victim)) {
                event.setIntensity(victim, 0);
                count++;
            }
        }
        if (count > 0 && count == event.getAffectedEntities().size()) {
            event.setCancelled(true);
        } else {
            // Cancelling projectile hit events still results in potions
            // splashing in the world. We need to cancel the splash events to
            // avoid that.
            onProjectileHit(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile entity = event.getEntity();
        ProjectileSource shooter = entity.getShooter();
        if (!(shooter instanceof Player)) {
            return;
        }
        Location location = BukkitUtil.adapt(entity.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        PlotPlayer<Player> pp = BukkitUtil.adapt((Player) shooter);
        Plot plot = location.getOwnedPlot();

        if (plot == null) {
            if (!PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, ProjectilesFlag.class, true) && !pp.hasPermission(
                    Permission.PERMISSION_ADMIN_PROJECTILE_ROAD
            )) {
                pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        TagResolver.resolver(
                                "node",
                                Tag.inserting(Permission.PERMISSION_ADMIN_PROJECTILE_ROAD)
                        )
                );
                entity.remove();
                event.setCancelled(true);
            }
        } else if (!plot.hasOwner()) {
            if (!pp.hasPermission(Permission.PERMISSION_ADMIN_PROJECTILE_UNOWNED)) {
                pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        TagResolver.resolver(
                                "node",
                                Tag.inserting(Permission.PERMISSION_ADMIN_PROJECTILE_UNOWNED)
                        )
                );
                entity.remove();
                event.setCancelled(true);
            }
        } else if (!plot.isAdded(pp.getUUID())) {
            if (entity.getType().equals(EntityType.FISHING_HOOK)) {
                if (plot.getFlag(FishingFlag.class)) {
                    return;
                }
            }
            if (!plot.getFlag(ProjectilesFlag.class)) {
                if (!pp.hasPermission(Permission.PERMISSION_ADMIN_PROJECTILE_OTHER)) {
                    pp.sendMessage(
                            TranslatableCaption.of("permission.no_permission_event"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ADMIN_PROJECTILE_OTHER)
                            )
                    );
                    entity.remove();
                    event.setCancelled(true);
                }
            }
        }
    }
*/
    public InteractionResult onProjectileHit(Projectile entity, BlockHitResult blockHitResult) {
        Location location = FabricUtil.adapt(GlobalPos.of(entity.level().dimension(), entity.blockPosition()));
        if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
            return InteractionResult.PASS;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return InteractionResult.PASS;
        }
        Plot plot = area.getPlot(location);
        Entity shooter = entity.getOwner();
        if (shooter instanceof ServerPlayer player) {
            if (!(player.connection.isAcceptingMessages())) {
                if (plot != null) {
                    if (plot.isAdded((player.getUUID())) || plot.getFlag(ProjectilesFlag.class)) {
                        return InteractionResult.PASS;
                    }
                } else if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, ProjectilesFlag.class, true)) {
                    return InteractionResult.PASS;
                }

                entity.remove(Entity.RemovalReason.DISCARDED);
                return InteractionResult.FAIL;
            }

            PlotPlayer<?> pp = FabricUtil.adapt((ServerPlayer) shooter);
            if (plot == null) {
                if (!PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, ProjectilesFlag.class, true) && !pp.hasPermission(
                        Permission.PERMISSION_ADMIN_PROJECTILE_UNOWNED
                )) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return InteractionResult.FAIL;
                }
                return InteractionResult.PASS;
            }
            if (plot.isAdded(pp.getUUID()) || pp.hasPermission(Permission.PERMISSION_ADMIN_PROJECTILE_OTHER) || plot.getFlag(
                    ProjectilesFlag.class) || (entity.getType().equals(EntityType.FISHING_BOBBER) && plot.getFlag(
                    FishingFlag.class))) {
                return InteractionResult.PASS;
            }
            entity.remove(Entity.RemovalReason.DISCARDED);
            return InteractionResult.FAIL;
        }
        if (shooter == null) {
            if (plot == null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                return InteractionResult.FAIL;
            }
            Location sLoc =
                    FabricUtil.adapt(GlobalPos.of(shooter.level().dimension(), shooter.blockPosition()));
            if (!area.contains(sLoc.getX(), sLoc.getZ())) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                return InteractionResult.FAIL;
            }
            Plot sPlot = area.getOwnedPlotAbs(sLoc);
            if (sPlot == null || !PlotHandler.sameOwners(plot, sPlot)) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

}
