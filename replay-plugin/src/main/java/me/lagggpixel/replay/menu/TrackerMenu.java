package me.lagggpixel.replay.menu;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.content.RecPlayer;
import me.lagggpixel.replay.utils.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.lagggpixel.replay.utils.Utils.c;

public class TrackerMenu implements IMenu {

    private final IReplaySession replaySession;
    private final Player player;
    private Inventory inv;

    public TrackerMenu(IReplaySession replaySession, Player player) {
        this.replaySession = replaySession;
        this.player = player;
        createInventory();
        addContents();
        player.openInventory(inv);
    }

    private void createInventory() {
        inv = Bukkit.createInventory(this, 54, "Players");
    }

    private void addContents() {
        for (Short uuid : replaySession.getReplayPlayers().keySet()) {
            RecPlayer player = replaySession.getReplayPlayers().get(uuid);

            ItemStack stack = player.getSkinHead();
            SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
            skullMeta.setDisplayName(player.getName());
            skullMeta.setLore(Arrays.asList(
                    c("&7Health: " + "&a" + player.getHealth()),
                    "",
                    c("&eLeft Click to teleport!"),
                    c("&eRight Click for first person!")
            ));
            stack.setItemMeta(skullMeta);

            inv.addItem(PacketUtils.setItemTag(stack, "player", uuid.toString()));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        String uuid = PacketUtils.getItemTag(item, "player");
        short id = -1;
        try {
            id = Short.parseShort(uuid);
        } catch (NumberFormatException ex) {
            return;
        }
        RecPlayer target = replaySession.getReplayPlayers().get(id);
        player.teleport(target.getPosition().toBukkitLocation(player.getWorld()));
        player.closeInventory();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inv;
    }
}
