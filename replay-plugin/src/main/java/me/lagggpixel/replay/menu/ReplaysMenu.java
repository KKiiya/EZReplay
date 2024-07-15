package me.lagggpixel.replay.menu;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ReplaysMenu implements IMenu {

    private final Player player;
    private Inventory inv;

    public ReplaysMenu(Player player) {
        this.player = player;
        createInventory();
        addContents();
        player.openInventory(getInventory());
    }

    private void createInventory() {
        inv = Bukkit.createInventory(this, 54, "Replays");
    }

    private void addContents() {
        for (IRecording recording : Replay.getInstance().getReplayManager().getReplays()) {
            ItemStack itemStack = new ItemStack(Material.PAPER);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(recording.getID().toString());
            itemStack.setItemMeta(meta);
            inv.addItem(itemStack);
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        UUID ID = UUID.fromString(meta.getDisplayName());

        IRecording recording = Replay.getInstance().getReplayManager().getReplayByID(ID);
        if (recording == null) return;

        recording.watch(player);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inv;
    }
}
