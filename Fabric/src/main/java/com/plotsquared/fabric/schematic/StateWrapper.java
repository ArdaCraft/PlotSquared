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
package com.plotsquared.fabric.schematic;

import com.google.common.base.Preconditions;
import com.plotsquared.fabric.util.FabricUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.fabric.FabricWorldEdit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class StateWrapper {

    public CompoundTag tag;

    private boolean paperErrorTextureSent = false;
    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + StateWrapper.class.getSimpleName());

    public StateWrapper(CompoundTag tag) {
        this.tag = tag;
    }

    public static String jsonToColourCode(String str) {
        str = str.replace("{\"extra\":", "").replace("],\"text\":\"\"}", "]")
                .replace("[{\"color\":\"black\",\"text\":\"", "&0")
                .replace("[{\"color\":\"dark_blue\",\"text\":\"", "&1")
                .replace("[{\"color\":\"dark_green\",\"text\":\"", "&2")
                .replace("[{\"color\":\"dark_aqua\",\"text\":\"", "&3")
                .replace("[{\"color\":\"dark_red\",\"text\":\"", "&4")
                .replace("[{\"color\":\"dark_purple\",\"text\":\"", "&5")
                .replace("[{\"color\":\"gold\",\"text\":\"", "&6")
                .replace("[{\"color\":\"gray\",\"text\":\"", "&7")
                .replace("[{\"color\":\"dark_gray\",\"text\":\"", "&8")
                .replace("[{\"color\":\"blue\",\"text\":\"", "&9")
                .replace("[{\"color\":\"green\",\"text\":\"", "&a")
                .replace("[{\"color\":\"aqua\",\"text\":\"", "&b")
                .replace("[{\"color\":\"red\",\"text\":\"", "&c")
                .replace("[{\"color\":\"light_purple\",\"text\":\"", "&d")
                .replace("[{\"color\":\"yellow\",\"text\":\"", "&e")
                .replace("[{\"color\":\"white\",\"text\":\"", "&f")
                .replace("[{\"obfuscated\":true,\"text\":\"", "&k")
                .replace("[{\"bold\":true,\"text\":\"", "&l")
                .replace("[{\"strikethrough\":true,\"text\":\"", "&m")
                .replace("[{\"underlined\":true,\"text\":\"", "&n")
                .replace("[{\"italic\":true,\"text\":\"", "&o").replace("[{\"color\":\"black\",", "&0")
                .replace("[{\"color\":\"dark_blue\",", "&1")
                .replace("[{\"color\":\"dark_green\",", "&2")
                .replace("[{\"color\":\"dark_aqua\",", "&3").replace("[{\"color\":\"dark_red\",", "&4")
                .replace("[{\"color\":\"dark_purple\",", "&5").replace("[{\"color\":\"gold\",", "&6")
                .replace("[{\"color\":\"gray\",", "&7").replace("[{\"color\":\"dark_gray\",", "&8")
                .replace("[{\"color\":\"blue\",", "&9").replace("[{\"color\":\"green\",", "&a")
                .replace("[{\"color\":\"aqua\",", "&b").replace("[{\"color\":\"red\",", "&c")
                .replace("[{\"color\":\"light_purple\",", "&d").replace("[{\"color\":\"yellow\",", "&e")
                .replace("[{\"color\":\"white\",", "&f").replace("[{\"obfuscated\":true,", "&k")
                .replace("[{\"bold\":true,", "&l").replace("[{\"strikethrough\":true,", "&m")
                .replace("[{\"underlined\":true,", "&n").replace("[{\"italic\":true,", "&o")
                .replace("{\"color\":\"black\",\"text\":\"", "&0")
                .replace("{\"color\":\"dark_blue\",\"text\":\"", "&1")
                .replace("{\"color\":\"dark_green\",\"text\":\"", "&2")
                .replace("{\"color\":\"dark_aqua\",\"text\":\"", "&3")
                .replace("{\"color\":\"dark_red\",\"text\":\"", "&4")
                .replace("{\"color\":\"dark_purple\",\"text\":\"", "&5")
                .replace("{\"color\":\"gold\",\"text\":\"", "&6")
                .replace("{\"color\":\"gray\",\"text\":\"", "&7")
                .replace("{\"color\":\"dark_gray\",\"text\":\"", "&8")
                .replace("{\"color\":\"blue\",\"text\":\"", "&9")
                .replace("{\"color\":\"green\",\"text\":\"", "&a")
                .replace("{\"color\":\"aqua\",\"text\":\"", "&b")
                .replace("{\"color\":\"red\",\"text\":\"", "&c")
                .replace("{\"color\":\"light_purple\",\"text\":\"", "&d")
                .replace("{\"color\":\"yellow\",\"text\":\"", "&e")
                .replace("{\"color\":\"white\",\"text\":\"", "&f")
                .replace("{\"obfuscated\":true,\"text\":\"", "&k")
                .replace("{\"bold\":true,\"text\":\"", "&l")
                .replace("{\"strikethrough\":true,\"text\":\"", "&m")
                .replace("{\"underlined\":true,\"text\":\"", "&n")
                .replace("{\"italic\":true,\"text\":\"", "&o").replace("{\"color\":\"black\",", "&0")
                .replace("{\"color\":\"dark_blue\",", "&1").replace("{\"color\":\"dark_green\",", "&2")
                .replace("{\"color\":\"dark_aqua\",", "&3").replace("{\"color\":\"dark_red\",", "&4")
                .replace("{\"color\":\"dark_purple\",", "&5").replace("{\"color\":\"gold\",", "&6")
                .replace("{\"color\":\"gray\",", "&7").replace("{\"color\":\"dark_gray\",", "&8")
                .replace("{\"color\":\"blue\",", "&9").replace("{\"color\":\"green\",", "&a")
                .replace("{\"color\":\"aqua\",", "&b").replace("{\"color\":\"red\",", "&c")
                .replace("{\"color\":\"light_purple\",", "&d").replace("{\"color\":\"yellow\",", "&e")
                .replace("{\"color\":\"white\",", "&f").replace("{\"obfuscated\":true,", "&k")
                .replace("{\"bold\":true,", "&l").replace("{\"strikethrough\":true,", "&m")
                .replace("{\"underlined\":true,", "&n").replace("{\"italic\":true,", "&o")
                .replace("\"color\":\"black\",\"text\":\"", "&0")
                .replace("\"color\":\"dark_blue\",\"text\":\"", "&1")
                .replace("\"color\":\"dark_green\",\"text\":\"", "&2")
                .replace("\"color\":\"dark_aqua\",\"text\":\"", "&3")
                .replace("\"color\":\"dark_red\",\"text\":\"", "&4")
                .replace("\"color\":\"dark_purple\",\"text\":\"", "&5")
                .replace("\"color\":\"gold\",\"text\":\"", "&6")
                .replace("\"color\":\"gray\",\"text\":\"", "&7")
                .replace("\"color\":\"dark_gray\",\"text\":\"", "&8")
                .replace("\"color\":\"blue\",\"text\":\"", "&9")
                .replace("\"color\":\"green\",\"text\":\"", "&a")
                .replace("\"color\":\"aqua\",\"text\":\"", "&b")
                .replace("\"color\":\"red\",\"text\":\"", "&c")
                .replace("\"color\":\"light_purple\",\"text\":\"", "&d")
                .replace("\"color\":\"yellow\",\"text\":\"", "&e")
                .replace("\"color\":\"white\",\"text\":\"", "&f")
                .replace("\"obfuscated\":true,\"text\":\"", "&k")
                .replace("\"bold\":true,\"text\":\"", "&l")
                .replace("\"strikethrough\":true,\"text\":\"", "&m")
                .replace("\"underlined\":true,\"text\":\"", "&n")
                .replace("\"italic\":true,\"text\":\"", "&o").replace("\"color\":\"black\",", "&0")
                .replace("\"color\":\"dark_blue\",", "&1").replace("\"color\":\"dark_green\",", "&2")
                .replace("\"color\":\"dark_aqua\",", "&3").replace("\"color\":\"dark_red\",", "&4")
                .replace("\"color\":\"dark_purple\",", "&5").replace("\"color\":\"gold\",", "&6")
                .replace("\"color\":\"gray\",", "&7").replace("\"color\":\"dark_gray\",", "&8")
                .replace("\"color\":\"blue\",", "&9").replace("\"color\":\"green\",", "&a")
                .replace("\"color\":\"aqua\",", "&b").replace("\"color\":\"red\",", "&c")
                .replace("\"color\":\"light_purple\",", "&d").replace("\"color\":\"yellow\",", "&e")
                .replace("\"color\":\"white\",", "&f").replace("\"obfuscated\":true,", "&k")
                .replace("\"bold\":true,", "&l").replace("\"strikethrough\":true,", "&m")
                .replace("\"underlined\":true,", "&n").replace("\"italic\":true,", "&o")
                .replace("[{\"text\":\"", "&0").replace("{\"text\":\"", "&0").replace("\"},", "")
                .replace("\"}]", "").replace("\"}", "");
        str = translateAlternateColorCodes('&', str);
        return str;
    }

    public static @NotNull String translateAlternateColorCodes(char altColorChar, @NotNull String textToTranslate) {
        Preconditions.checkArgument(textToTranslate != null, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public boolean restoreTag(String worldName, int x, int y, int z) {
        ServerLevel world = FabricUtil.getWorld(worldName);
        if (world == null) {
            return false;
        }
        Entity spawnedEntity = EntityType.loadEntityRecursive((net.minecraft.nbt.CompoundTag) FabricWorldEdit.inst.getFaweAdapter().fromNative(this.tag), world, entity -> {
            entity.setPos(x, y, z);
            return entity;
        });if(spawnedEntity != null) {
            return world.addFreshEntity(spawnedEntity);
        } else {
            return false;
        }
        //return restoreTag(world, world.getBlockState(new BlockPos(x, y, z)), new BlockPos(x, y, z));
    }
/*
    @SuppressWarnings("deprecation") // #setLine is needed for Spigot compatibility
    public boolean restoreTag(ServerLevel serverLevel, @NonNull BlockState block, BlockPos blockPos) {
        if (this.tag == null) {
            return false;
        }
        BlockState state = block;
        switch (getId()) {
            case "chest", "beacon", "brewingstand", "dispenser", "dropper", "furnace", "hopper", "shulkerbox" -> {
                if (!((serverLevel.getBlockEntity(blockPos)) instanceof Container container)) {
                    return false;
                }
                List<Tag> itemsTag = this.tag.getListTag("Items").getValue();
                for (Tag itemTag : itemsTag) {
                    CompoundTag itemComp = (CompoundTag) itemTag;
                    ItemType type = ItemType.REGISTRY.get(itemComp.getString("id").toLowerCase());
                    if (type == null) {
                        continue;
                    }
                    int count = itemComp.getByte("Count");
                    int slot = itemComp.getByte("Slot");
                    CompoundTag tag = (CompoundTag) itemComp.getValue().get("tag");
                    BaseItemStack baseItemStack = new BaseItemStack(type, tag, count);
                    ItemStack itemStack = FabricAdapter.adapt(baseItemStack);
                    container.setItem(slot, itemStack);
                }
                container.update(true, false);
                return true;
            }
            case "sign" -> {
                if (state.getBlock() instanceof SignBlock sign) {
                    sign.setLine(0, jsonToColourCode(tag.getString("Text1")));
                    sign.setLine(1, jsonToColourCode(tag.getString("Text2")));
                    sign.setLine(2, jsonToColourCode(tag.getString("Text3")));
                    sign.setLine(3, jsonToColourCode(tag.getString("Text4")));
                    state.update(true);
                    return true;
                }
                return false;
            }
            case "skull" -> {
                if (state instanceof Skull skull) {
                    CompoundTag skullOwner = ((CompoundTag) this.tag.getValue().get("SkullOwner"));
                    if (skullOwner == null) {
                        return true;
                    }
                    String player = skullOwner.getString("Name");

                    if (player != null && !player.isEmpty()) {
                        try {
                            skull.setOwningPlayer(Bukkit.getOfflinePlayer(player));
                            skull.update(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }

                    final CompoundTag properties = (CompoundTag) skullOwner.getValue().get("Properties");
                    if (properties == null) {
                        return false;
                    }
                    final ListTag textures = properties.getListTag("textures");
                    if (textures.getValue().isEmpty()) {
                        return false;
                    }
                    final CompoundTag textureCompound = (CompoundTag) textures.getValue().get(0);
                    if (textureCompound == null) {
                        return false;
                    }
                    String textureValue = textureCompound.getString("Value");
                    if (textureValue == null) {
                        return false;
                    }
                    if (!PaperLib.isPaper()) {
                        if (!paperErrorTextureSent) {
                            paperErrorTextureSent = true;
                            LOGGER.error("Failed to populate skull data in your road schematic - This is a Spigot limitation.");
                        }
                        return false;
                    }
                    final PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    profile.setProperty(new ProfileProperty("textures", textureValue));
                    skull.setPlayerProfile(profile);
                    skull.update(true);
                    return true;

                }
                return false;
            }
            case "banner" -> {
                if (state instanceof Banner banner) {
                    List<Tag> patterns = this.tag.getListTag("Patterns").getValue();
                    if (patterns == null || patterns.isEmpty()) {
                        return false;
                    }
                    banner.setPatterns(patterns.stream().map(t -> (CompoundTag) t).map(compoundTag -> {
                        DyeColor color = DyeColor.getByWoolData((byte) compoundTag.getInt("Color"));
                        PatternType patternType = PatternType.getByIdentifier(compoundTag.getString("Pattern"));
                        if (color == null || patternType == null) {
                            return null;
                        }
                        return new Pattern(color, patternType);
                    }).filter(Objects::nonNull).toList());
                    banner.update(true);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public String getId() {
        String tileid = this.tag.getString("id").toLowerCase();
        if (tileid.startsWith("minecraft:")) {
            tileid = tileid.replace("minecraft:", "");
        }
        return tileid;
    }

    public List<CompoundTag> serializeInventory(ItemStack[] items) {
        List<CompoundTag> tags = new ArrayList<>();
        for (int i = 0; i < items.length; ++i) {
            if (items[i] != null) {
                Map<String, Tag> tagData = serializeItem(items[i]);
                tagData.put("Slot", new ByteTag((byte) i));
                tags.add(new CompoundTag(tagData));
            }
        }
        return tags;
    }

    public Map<String, Tag> serializeItem(ItemStack item) {
        Map<String, Tag> data = new HashMap<>();
        data.put("id", new StringTag(item.getType().name()));
        data.put("Damage", new ShortTag(item.getDurability()));
        data.put("Count", new ByteTag((byte) item.getAmount()));
        if (!item.getEnchantments().isEmpty()) {
            List<CompoundTag> enchantmentList = new ArrayList<>();
            for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                Map<String, Tag> enchantment = new HashMap<>();
                enchantment.put("id", new StringTag(entry.getKey().toString()));
                enchantment.put("lvl", new ShortTag(entry.getValue().shortValue()));
                enchantmentList.add(new CompoundTag(enchantment));
            }
            Map<String, Tag> auxData = new HashMap<>();
            auxData.put("ench", new ListTag(CompoundTag.class, enchantmentList));
            data.put("tag", new CompoundTag(auxData));
        }
        return data;
    }*/
}
