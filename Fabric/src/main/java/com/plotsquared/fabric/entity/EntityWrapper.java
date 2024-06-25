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
package com.plotsquared.fabric.entity;


import com.plotsquared.core.location.Location;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class EntityWrapper {

    protected final float yaw;
    protected final float pitch;
    private final Entity entity;
    private final EntityType type;
    public double x;
    public double y;
    public double z;

    EntityWrapper(final @NonNull Entity entity) {
        this.entity = entity;
        this.type = entity.getType();

        final GlobalPos location = GlobalPos.of(entity.level().dimension(), entity.blockPosition());
        this.x = location.pos().getX();
        this.y = location.pos().getY();
        this.z = location.pos().getZ();
        this.yaw = entity.getViewXRot(1.0f);
        this.pitch = entity.getViewYRot(1.0f);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String toString() {
        return String.format("[%s, x=%s, y=%s, z=%s]", type.toString(), x, y, z);
    }

    public abstract Entity spawn(ServerLevel world, int xOffset, int zOffset);

    public abstract void saveEntity();

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EntityType getType() {
        return this.type;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

}
