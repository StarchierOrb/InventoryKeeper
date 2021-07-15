package me.starchier.inventorykeeper.items;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBase {
    private String name;
    private ItemStack item;

    public ItemBase(String name, ItemStack item) {
        this.name = name;
        this.item = item;
    }

    public ItemBase(String name) {
        this.name = name;
    }

    public ItemStack getItem() {
        return item;
    }
}
