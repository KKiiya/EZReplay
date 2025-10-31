package me.lagggpixel.replay.listeners.replaysession;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.content.IControls;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SessionListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        IReplaySession replaySession = Replay.getInstance().getReplaySessionManager().getSessionByPlayer(p);
        if (replaySession == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        IReplaySession replaySession = Replay.getInstance().getReplaySessionManager().getSessionByPlayer(p);
        if (replaySession == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        IReplaySession replaySession = Replay.getInstance().getReplaySessionManager().getSessionByPlayer(p);
        if (replaySession == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        IReplaySession replaySession = Replay.getInstance().getReplaySessionManager().getSessionByPlayer(p);
        if (replaySession == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onControlUse(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        IReplaySession replaySession = Replay.getInstance().getReplaySessionManager().getSessionByPlayer(player);

        if (replaySession == null) return;
        if (item == null || item.getType() == Material.AIR) return;
        e.setCancelled(true);
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        IControls controls = replaySession.getPlayerControls().get(player);
        String control = Replay.getInstance().getVersionSupport().getItemTag(item, "Replay-Control");

        if (control == null) return;

        controls.onControl(control);
    }
}
