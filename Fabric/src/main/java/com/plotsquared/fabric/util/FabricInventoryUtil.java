package com.plotsquared.fabric.util;

import com.google.inject.Singleton;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.fabric.player.FabricPlayer;
import com.sk89q.worldedit.fabric.FabricAdapter;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Singleton
public class FabricInventoryUtil extends InventoryUtil {

    @SuppressWarnings("deprecation") // Paper deprecation
    private static @Nullable ItemStack getItem(PlotItemStack item) {
        if (item == null) {
            return null;
        }
        Item material = FabricAdapter.adapt(item.getType());
        if (material == null) {
            return null;
        }
        ItemStack stack = new ItemStack(material, item.getAmount());
        CompoundTag meta = null;
        if (item.getName() != null) {
            Component nameComponent = FabricUtil.MINI_MESSAGE.deserialize(item.getName());
            stack.setHoverName(FabricUtil.FABRIC_AUDIENCES.toNative(nameComponent));
        }
        if (item.getLore() != null) {
            List<String> lore = new ArrayList<>();
            for (String entry : item.getLore()) {
                lore.add(FabricUtil.LEGACY_COMPONENT_SERIALIZER.serialize(FabricUtil.MINI_MESSAGE.deserialize(entry)));
            }
            setLore(stack, lore);
        }
        return stack;
    }

    @SuppressWarnings("deprecation") // Paper deprecation
    @Override
    public void open(PlotInventory inv) {
        FabricPlayer bp = (FabricPlayer) inv.getPlayer();
        Inventory inventory = new Inventory(bp.player);
                /*Bukkit.createInventory(null, inv.getLines() * 9,
                ChatColor.translateAlternateColorCodes('&', inv.getTitle()));*/
        PlotItemStack[] items = inv.getItems();
        for (int i = 0; i < inv.getLines() * 9; i++) {
            PlotItemStack item = items[i];
            if (item != null) {
                inventory.setItem(i, getItem(item));
            }
        }
        bp.player.openMenu(new SimpleMenuProvider((i, inventory1, player) ->
                ChestMenu.sixRows(i, inventory),
                net.minecraft.network.chat.Component.literal(inv.getTitle())));
    }

    @Override
    public void close(PlotInventory inv) {
        if (!inv.isOpen()) {
            return;
        }
        FabricPlayer bp = (FabricPlayer) inv.getPlayer();
        bp.player.closeContainer();
    }

    @Override
    public boolean setItemChecked(PlotInventory inv, int index, PlotItemStack item) {
        FabricPlayer bp = (FabricPlayer) inv.getPlayer();
        InventoryMenu opened = bp.player.inventoryMenu;
        ItemStack stack = getItem(item);
        if (stack == null) {
            return false;
        }
        if (!inv.isOpen()) {
            return true;
        }
        opened.setItem(opened.getSlot(index).x, opened.getSlot(index).y, stack);
        opened.broadcastChanges();
        return true;
    }

    @SuppressWarnings("deprecation") // Paper deprecation
    public PlotItemStack getItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        // int id = item.getTypeId();
        Item id = item.getItem();
        int amount = item.getCount();
        return new PlotItemStack(id.toString(), amount, item.getDisplayName().getString(), getLore(item).toArray(new String[0]));
    }

    @Override
    public PlotItemStack[] getItems(PlotPlayer<?> player) {
        FabricPlayer bp = (FabricPlayer) player;
        Inventory inv = bp.player.getInventory();
        return IntStream.range(0, 36).mapToObj(i -> getItem(inv.getItem(i)))
                .toArray(PlotItemStack[]::new);
    }

    @SuppressWarnings("deprecation") // #getTitle is needed for Spigot compatibility
    @Override
    public boolean isOpen(PlotInventory plotInventory) {
        if (!plotInventory.isOpen()) {
            return false;
        }
        //FabricPlayer bp = (FabricPlayer) plotInventory.getPlayer();
        //Inventory opened = bp.player.containerMenu;
        if (plotInventory.isOpen()) {
            return true;
            /*
            if (opened.getType() == InventoryType.CRAFTING) {
                opened.getTitle();
            }*/
        }
        return false;
    }

    public static List<String> getLore(ItemStack stack) {
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

    public static void setLore(ItemStack stack, List<String> lore) {
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
}
