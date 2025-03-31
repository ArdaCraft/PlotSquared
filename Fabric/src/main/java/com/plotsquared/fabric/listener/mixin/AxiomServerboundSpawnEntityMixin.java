package com.plotsquared.fabric.listener.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.axiom.packets.AxiomServerboundSpawnEntity;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxiomServerboundSpawnEntity.class)
public class AxiomServerboundSpawnEntityMixin {


    @WrapMethod(method = "handle")
    public void onHandle(MinecraftServer server, ServerPlayer player, Operation<Void> original){
        if(LuckPermsProvider.get().getUserManager().getUser(player.getUUID()).getCachedData().getPermissionData().checkPermission("axiom.entity.spawn").asBoolean()) {
            original.call(server, player);
        }
    }

}
