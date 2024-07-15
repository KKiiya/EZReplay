package me.lagggpixel.replay.listeners;

import me.lagggpixel.replay.menu.IMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        if (inv == null) return;
        if (e.getCurrentItem() == null) return;
        if (inv.getHolder() == null) return;
        if (!(inv.getHolder() instanceof IMenu)) return;
        ((IMenu) (inv.getHolder())).onInventoryClick(e);
    }
}
