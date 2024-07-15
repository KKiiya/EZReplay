package me.lagggpixel.replay.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface IMenu extends InventoryHolder {
    void onInventoryClick(InventoryClickEvent e);
}
