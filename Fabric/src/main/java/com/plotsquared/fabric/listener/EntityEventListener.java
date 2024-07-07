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
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.listener.PlayerBlockEventType;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotHandler;
import com.plotsquared.core.plot.flag.implementations.DisablePhysicsFlag;
import com.plotsquared.core.plot.flag.implementations.EntityChangeBlockFlag;
import com.plotsquared.core.plot.flag.implementations.ExplosionFlag;
import com.plotsquared.core.plot.flag.implementations.InvincibleFlag;
import com.plotsquared.core.plot.flag.implementations.ProjectileChangeBlockFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlotFlagUtil;
import com.plotsquared.fabric.FabricPlatform;
import com.plotsquared.fabric.data.PlotSquaredDataAttachments;
import com.plotsquared.fabric.listener.event.EntityTypeCreateCallback;
import com.plotsquared.fabric.util.FabricEntityUtil;
import com.plotsquared.fabric.util.FabricUtil;
import com.sk89q.worldedit.world.block.BlockType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.entity.EntityDamageEvent;
import xyz.nucleoid.stimuli.event.entity.EntitySpawnEvent;

import java.util.function.Consumer;


@SuppressWarnings("unused")
public class EntityEventListener {

    private final FabricPlatform platform;
    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private float lastRadius;

    @Inject
    public EntityEventListener(
            final @NonNull FabricPlatform platform,
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher
    ) {
        this.platform = platform;
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        Stimuli.global().listen(EntityDamageEvent.EVENT, this::onEntityCombustByEntity);
        EntityTypeCreateCallback.EVENT.register(this::creatureSpawnEvent);
        //Stimuli.global().listen(EntitySpawnEvent.EVENT, this::onEntityFall);
    }

    public InteractionResult onEntityCombustByEntity(LivingEntity entity, DamageSource source, float amount) {
        if (source.is(DamageTypes.ON_FIRE)) {
            return onEntityDamageByEntityCommon(source.getEntity(), entity, source);
        }
        return InteractionResult.PASS;
    }

    public InteractionResult onEntityDamageByEntityEvent(LivingEntity entity, DamageSource source, float amount) {
        return onEntityDamageByEntityCommon(source.getEntity(), entity, source);
    }

    private InteractionResult onEntityDamageByEntityCommon(
            final Entity damager,
            final Entity victim,
            final DamageSource cause
    ) {
        if (damager != null) {
            Location location = FabricUtil.adapt(GlobalPos.of(damager.level().dimension(), damager.blockPosition()));
            if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
                return InteractionResult.PASS;
            }
            if (!FabricEntityUtil.entityDamage(damager, victim, cause)) {
                if (victim instanceof AgeableMob ageable) {
                    if (ageable.getAge() == -24000) {
                        ageable.setAge(0);
                        ageable.setBaby(false);
                    }
                }
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    public InteractionResult creatureSpawnEvent(
            ServerLevel serverLevel,
            CompoundTag compoundTag,
            Consumer<?> consumer,
            BlockPos blockPos,
            MobSpawnType mobSpawnType,
            boolean bl,
            boolean bl2,
            Object entitySpawned
    ) {
        if (entitySpawned instanceof Entity entity) {
            Location location = FabricUtil.adapt(GlobalPos.of(entity.level().dimension(), entity.blockPosition()));
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return InteractionResult.PASS;
            }
            // Armour-stands are handled elsewhere and should not be handled by area-wide entity-spawn options
            if (entity.getType() == EntityType.ARMOR_STAND) {
                return InteractionResult.FAIL;
            }
            switch (mobSpawnType.name().toUpperCase()) {
                case "DISPENSE_EGG", "EGG", "OCELOT_BABY", "SPAWNER_EGG" -> {
                    if (!area.isSpawnEggs()) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                        return InteractionResult.FAIL;
                    }
                }
                case "REINFORCEMENTS", "NATURAL", "MOUNT", "PATROL", "RAID", "SHEARED", "SILVERFISH_BLOCK", "ENDER_PEARL",
                        "TRAP", "VILLAGE_DEFENSE", "VILLAGE_INVASION", "BEEHIVE", "CHUNK_GEN", "NETHER_PORTAL",
                        "FROZEN", "SPELL", "DEFAULT" -> {
                    if (!area.isMobSpawning()) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                        return InteractionResult.FAIL;
                    }
                }
                case "BREEDING", "DUPLICATION" -> {
                    if (!area.isSpawnBreeding()) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                        return InteractionResult.FAIL;
                    }
                }
                case "CUSTOM" -> {
                    if (!area.isSpawnCustom()) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                        return InteractionResult.FAIL;
                    }
                    // No need to clutter metadata if running paper
                    //if (!PaperLib.isPaper()) {
                    entity.setAttached(PlotSquaredDataAttachments.PS_CUSTOM_SPAWNED, true);
                    // }
                    return InteractionResult.PASS; // Don't cancel if mob spawning is disabled
                }
                case "BUILD_IRONGOLEM", "BUILD_SNOWMAN", "BUILD_WITHER" -> {
                    if (!area.isSpawnCustom()) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                        return InteractionResult.FAIL;
                    }
                }
                case "SPAWNER" -> {
                    if (!area.isMobSpawnerSpawning()) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                        return InteractionResult.FAIL;
                    }
                }
            }
            Plot plot = area.getOwnedPlotAbs(location);
            if (plot == null) {
                if (!area.isMobSpawning()) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return InteractionResult.FAIL;
                }
                return InteractionResult.PASS;
            }
            if (FabricEntityUtil.checkEntity(entity, plot.getBasePlot(false))) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    /*TODO CREATE FALLING BLOCK TICK CALLBACK
    public InteractionResult onEntityFall(Entity entity) {
        if(entity instanceof FallingBlockEntity fallingBlockEntity) {
            BlockState block = fallingBlockEntity.getBlockState();
            ServerLevel world = entity.getServer().getLevel(entity.level().dimension());
            String worldName = world.serverLevelData.getLevelName();
            if (!this.plotAreaManager.hasPlotArea(worldName)) {
                return InteractionResult.PASS;
            }
            Location location = FabricUtil.adapt(GlobalPos.of(world.dimension(), entity.blockPosition()));
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return InteractionResult.PASS;
            }
            Plot plot = area.getOwnedPlotAbs(location);
            if (plot == null || plot.getFlag(DisablePhysicsFlag.class)) {
                if (plot != null) {
                    if (!fallingBlockEntity.isNoGravity()) {
                        BlockEventListener.sendBlockChange(GlobalPos.of(world.dimension(), entity.blockPosition()), block);
                    }
                    plot.debug("Falling block event was cancelled because disable-physics = true");
                }
                return InteractionResult.FAIL;
            }
            if (event.getTo().hasGravity()) {
                Entity entity = event.getEntity();
                List<MetadataValue> meta = entity.getMetadata("plot");
                if (meta.isEmpty()) {
                    return;
                }
                Plot origin = (Plot) meta.get(0).value();
                if (origin != null && !origin.equals(plot)) {
                    event.setCancelled(true);
                    entity.remove();
                }
            } else if (event.getTo() == Material.AIR) {
                event.getEntity().setMetadata("plot", new FixedMetadataValue((Plugin) PlotSquared.platform(), plot));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        Location location = BukkitUtil.adapt(event.getEntity().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, InvincibleFlag.class, true)) {
                event.setCancelled(true);
            }
            return;
        }
        if (plot.getFlag(InvincibleFlag.class)) {
            plot.debug(event.getEntity().getName() + " could not take damage because invincible = true");
            event.setCancelled(true);
        }
    }*/
    /*
    public void onBigBoom(EntityExplodeEvent event) {
        Location location = BukkitUtil.adapt(event.getLocation());
        PlotArea area = location.getPlotArea();
        boolean plotArea = location.isPlotArea();
        if (!plotArea) {
            if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
                return;
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot != null) {
            if (plot.getFlag(ExplosionFlag.class)) {
                List<MetadataValue> meta = event.getEntity().getMetadata("plot");
                Plot origin;
                if (meta.isEmpty()) {
                    origin = plot;
                } else {
                    origin = (Plot) meta.get(0).value();
                }
                if (this.lastRadius != 0) {
                    List<Entity> nearby = event.getEntity().getNearbyEntities(this.lastRadius, this.lastRadius, this.lastRadius);
                    for (Entity near : nearby) {
                        if (near instanceof TNTPrimed || near.getType().equals(EntityType.MINECART_TNT)) {
                            if (!near.hasMetadata("plot")) {
                                near.setMetadata("plot", new FixedMetadataValue((Plugin) PlotSquared.platform(), plot));
                            }
                        }
                    }
                    this.lastRadius = 0;
                }
                Iterator<Block> iterator = event.blockList().iterator();
                while (iterator.hasNext()) {
                    Block block = iterator.next();
                    location = BukkitUtil.adapt(block.getLocation());
                    if (!area.contains(location.getX(), location.getZ()) || !origin.equals(area.getOwnedPlot(location))) {
                        iterator.remove();
                    }
                }
                return;
            } else {
                plot.debug("Explosion was cancelled because explosion = false");
            }
        }
        event.setCancelled(true);
        //Spawn Explosion Particles when enabled in settings
        if (Settings.General.ALWAYS_SHOW_EXPLOSIONS) {
            event.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, event.getLocation(), 0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPeskyMobsChangeTheWorldLikeWTFEvent(EntityChangeBlockEvent event) {
        Entity e = event.getEntity();
        Material type = event.getBlock().getType();
        Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        if (e instanceof FallingBlock) {
            // allow falling blocks converting to blocks and vice versa
            return;
        } else if (e instanceof Boat) {
            // allow boats destroying lily pads
            if (type == Material.LILY_PAD) {
                return;
            }
        } else if (e instanceof Player player) {
            BukkitPlayer pp = BukkitUtil.adapt(player);
            if (type.toString().equals("POWDER_SNOW")) {
                // Burning player evaporating powder snow. Use same checks as
                // trampling farmland
                BlockType blockType = BukkitAdapter.asBlockType(type);
                if (!this.eventDispatcher.checkPlayerBlockEvent(pp,
                        PlayerBlockEventType.TRIGGER_PHYSICAL, location, blockType, true
                )) {
                    event.setCancelled(true);
                }
                return;
            } else {
                // already handled by other flags (mainly the 'use' flag):
                // - player tilting big dripleaf by standing on it
                // - player picking glow berries from cave vine
                // - player trampling farmland
                // - player standing on or clicking redstone ore
                return;
            }
        } else if (e instanceof Projectile entity) {
            // Exact same as the ProjectileHitEvent listener, except that we let
            // the entity-change-block determine what to do with shooters that
            // aren't players and aren't blocks
            Plot plot = area.getPlot(location);
            ProjectileSource shooter = entity.getShooter();
            if (shooter instanceof Player) {
                PlotPlayer<?> pp = BukkitUtil.adapt((Player) shooter);
                if (plot == null) {
                    if (area.isRoadFlags() && !area.getRoadFlag(ProjectileChangeBlockFlag.class) && !pp.hasPermission(Permission.PERMISSION_ADMIN_PROJECTILE_UNOWNED)) {
                        entity.remove();
                        event.setCancelled(true);
                    }
                    return;
                }
                if (plot.isAdded(pp.getUUID()) || plot.getFlag(ProjectileChangeBlockFlag.class) || pp.hasPermission(Permission.PERMISSION_ADMIN_PROJECTILE_OTHER)) {
                    return;
                }
                entity.remove();
                event.setCancelled(true);
                return;
            }
            if (!(shooter instanceof Entity) && shooter != null) {
                if (plot == null) {
                    entity.remove();
                    event.setCancelled(true);
                    return;
                }
                Location sLoc =
                        BukkitUtil.adapt(((BlockProjectileSource) shooter).getBlock().getLocation());
                if (!area.contains(sLoc.getX(), sLoc.getZ())) {
                    entity.remove();
                    event.setCancelled(true);
                    return;
                }
                Plot sPlot = area.getOwnedPlotAbs(sLoc);
                if (sPlot == null || !PlotHandler.sameOwners(plot, sPlot)) {
                    entity.remove();
                    event.setCancelled(true);
                }
                return;
            }
            // fall back to entity-change-block flag
        }

        Plot plot = area.getOwnedPlot(location);
        if (plot != null && !plot.getFlag(EntityChangeBlockFlag.class)) {
            plot.debug(e.getType() + " could not change block because entity-change-block = false");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrime(ExplosionPrimeEvent event) {
        this.lastRadius = event.getRadius() + 1;
    }

    public InteractionResult onVehicleCreate(Entity entity) {
        if(entity.isVehicle()) {
            Location location = FabricUtil.adapt(GlobalPos.of(entity.level().dimension(), entity.blockPosition()));
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return InteractionResult.PASS;
            }
            Plot plot = area.getOwnedPlotAbs(location);
            if (plot == null || FabricEntityUtil.checkEntity(entity, plot)) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                return InteractionResult.FAIL;
            }
            if (Settings.Enabled_Components.KILL_ROAD_VEHICLES) {
                entity.setAttached(PlotSquaredDataAttachments.PLOT_DATA, plot);
            }
        }
        return InteractionResult.PASS;
    }
*/
}
