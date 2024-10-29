package me.lagggpixel.replay.menu;

import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.support.IVersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
        for (String uuid : replaySession.getSpawnedEntities().keySet()) {
            Entity entity = replaySession.getSpawnedEntities().get(uuid);
            if (!(entity instanceof Player)) continue;

            Player player = (Player) replaySession.getSpawnedEntities().get(uuid);
            TeamColor teamColor = replaySession.getTeamColor(uuid);
            ChatColor color = teamColor.chat();

            ItemStack stack = new ItemStack(vs.getPlayerHeadMaterial());
            stack.setDurability((short) 3);
            SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
            skullMeta.setOwner(player.getName());
            skullMeta.setDisplayName(color + player.getDisplayName());
            skullMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Team: " + teamColor,
                    ChatColor.GRAY + "Health: " + ChatColor.GREEN + player.getHealth()
            ));
            stack.setItemMeta(skullMeta);

            inv.addItem(vs.setItemTag(stack, "player", uuid));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        String uuid = vs.getItemTag(item, "player");
        if (uuid == null) return;
        Player teleportingTo = (Player) replaySession.getSpawnedEntities().get(uuid);
        player.teleport(teleportingTo);
        player.closeInventory();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inv;
    }
}
