package me.starchier.inventorykeeper.storage;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryStorage {
    private final ItemStack[] armor;
    private final ItemStack[] items;

    public PlayerInventoryStorage(Player player) {
        armor = player.getInventory().getArmorContents();
        int size = player.getInventory().getSize();
        items = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            items[i] = player.getInventory().getItem(i);
        }
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public ItemStack[] getItems() {
        return items;
    }
}
