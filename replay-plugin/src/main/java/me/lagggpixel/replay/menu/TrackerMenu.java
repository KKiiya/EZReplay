package me.lagggpixel.replay.menu;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.support.IVersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.lagggpixel.replay.utils.Utils.c;

public class TrackerMenu implements IMenu {

    private final IVersionSupport vs;
    private final IReplaySession replaySession;
    private final Player player;
    private Inventory inv;

    public TrackerMenu(IReplaySession replaySession, Player player) {
        this.vs = Replay.getInstance().getVersionSupport();
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
        for (Short uuid : replaySession.getSpawnedEntities().keySet()) {
            Entity entity = replaySession.getSpawnedEntities().get(uuid);
            if (!(entity instanceof Player)) continue;

            Player player = (Player) replaySession.getSpawnedEntities().get(uuid);

            ItemStack stack = new ItemStack(vs.getPlayerHeadMaterial());
            stack.setDurability((short) 3);
            SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
            skullMeta.setOwner(player.getName());
            skullMeta.setDisplayName(player.getDisplayName());
            skullMeta.setLore(Arrays.asList(
                    c("&7Health: " + "&a" + player.getHealth()),
                    "",
                    c("&eLeft Click to teleport!"),
                    c("&eRight Click for first person!")
            ));
            stack.setItemMeta(skullMeta);

            inv.addItem(vs.setItemTag(stack, "player", uuid.toString()));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        String uuid = vs.getItemTag(item, "player");
        short id = -1;
        try {
            id = Short.parseShort(uuid);
        } catch (NumberFormatException ex) {
            return;
        }
        Player target = (Player) replaySession.getSpawnedEntities().get(id);
        switch (e.getClick()) {
            case RIGHT:
            case SHIFT_RIGHT:
                player.setGameMode(GameMode.SPECTATOR);
                player.setSpectatorTarget(target);
                break;
            default:
                player.teleport(target);
        }
        player.closeInventory();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inv;
    }
}
