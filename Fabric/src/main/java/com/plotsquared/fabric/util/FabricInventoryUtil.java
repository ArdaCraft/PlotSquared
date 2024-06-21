package com.plotsquared.fabric.util;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.util.InventoryUtil;
import com.sk89q.worldedit.fabric.FabricAdapter;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
            meta = stack.getTag();
            Component nameComponent = FabricUtil.MINI_MESSAGE.deserialize(item.getName());
            stack.setHoverName(FabricUtil.FABRIC_AUDIENCES.toNative(nameComponent));
        }
        if (item.getLore() != null) {
            if (meta == null) {
                meta = stack.getOrCreateTag();
            }
            List<String> lore = new ArrayList<>();
            for (String entry : item.getLore()) {
                lore.add(BukkitUtil.LEGACY_COMPONENT_SERIALIZER.serialize(BukkitUtil.MINI_MESSAGE.deserialize(entry)));
            }
            ListTag loreTag = new ListTag();

            meta.setLore(lore);
        }
        if (meta != null) {
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @SuppressWarnings("deprecation") // Paper deprecation
    @Override
    public void open(PlotInventory inv) {
        BukkitPlayer bp = (BukkitPlayer) inv.getPlayer();
        Inventory inventory = Bukkit.createInventory(null, inv.getLines() * 9,
                ChatColor.translateAlternateColorCodes('&', inv.getTitle())
        );
        PlotItemStack[] items = inv.getItems();
        for (int i = 0; i < inv.getLines() * 9; i++) {
            PlotItemStack item = items[i];
            if (item != null) {
                inventory.setItem(i, getItem(item));
            }
        }
        bp.player.openInventory(inventory);
    }

    @Override
    public void close(PlotInventory inv) {
        if (!inv.isOpen()) {
            return;
        }
        BukkitPlayer bp = (BukkitPlayer) inv.getPlayer();
        bp.player.closeInventory();
    }

    @Override
    public boolean setItemChecked(PlotInventory inv, int index, PlotItemStack item) {
        BukkitPlayer bp = (BukkitPlayer) inv.getPlayer();
        InventoryView opened = bp.player.getOpenInventory();
        ItemStack stack = getItem(item);
        if (stack == null) {
            return false;
        }
        if (!inv.isOpen()) {
            return true;
        }
        opened.setItem(index, stack);
        bp.player.updateInventory();
        return true;
    }

    @SuppressWarnings("deprecation") // Paper deprecation
    public PlotItemStack getItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        // int id = item.getTypeId();
        Material id = item.getType();
        ItemMeta meta = item.getItemMeta();
        int amount = item.getAmount();
        String name = null;
        String[] lore = null;
        if (item.hasItemMeta()) {
            assert meta != null;
            if (meta.hasDisplayName()) {
                name = meta.getDisplayName();
            }
            if (meta.hasLore()) {
                List<String> itemLore = meta.getLore();
                assert itemLore != null;
                lore = itemLore.toArray(new String[0]);
            }
        }
        return new PlotItemStack(id.name(), amount, name, lore);
    }

    @Override
    public PlotItemStack[] getItems(PlotPlayer<?> player) {
        BukkitPlayer bp = (BukkitPlayer) player;
        PlayerInventory inv = bp.player.getInventory();
        return IntStream.range(0, 36).mapToObj(i -> getItem(inv.getItem(i)))
                .toArray(PlotItemStack[]::new);
    }

    @SuppressWarnings("deprecation") // #getTitle is needed for Spigot compatibility
    @Override
    public boolean isOpen(PlotInventory plotInventory) {
        if (!plotInventory.isOpen()) {
            return false;
        }
        BukkitPlayer bp = (BukkitPlayer) plotInventory.getPlayer();
        InventoryView opened = bp.player.getOpenInventory();
        if (plotInventory.isOpen()) {
            if (opened.getType() == InventoryType.CRAFTING) {
                opened.getTitle();
            }
        }
        return false;
    }

}
