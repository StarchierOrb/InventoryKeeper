package me.starchier.inventorykeeper.items;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBase {
    private String name;
    private ItemStack item;

    public ItemBase(String name, Material material) {
        this.name = name;
        item = new ItemStack(material, 1);
    }

    public ItemStack getItem() {
        return item;
    }


}
