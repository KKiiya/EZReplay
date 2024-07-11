package me.lagggpixel.replay.listeners.recordables;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.BlockAction;
import me.lagggpixel.replay.replay.ReplayManager;
import me.lagggpixel.replay.support.nms.recordable.world.block.BlockRecordable;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(player);

        if (a == null) return;

        IRecording recording = ReplayManager.getInstance().getActiveReplay(a);
        if (recording == null) return;

        recording.getLastFrame().addRecordable(new BlockRecordable(recording, e.getBlock(), BlockAction.BREAK, true));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(player);

        if (a == null) return;

        IRecording recording = ReplayManager.getInstance().getActiveReplay(a);
        if (recording == null) return;

        recording.getLastFrame().addRecordable(new BlockRecordable(recording, e.getBlock(), BlockAction.PLACE, true));
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player player = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(player);

        if (a == null) return;
        if (block == null) return;

        IRecording recording = ReplayManager.getInstance().getActiveReplay(a);
        if (recording == null) return;

        recording.getLastFrame().addRecordable(new BlockRecordable(recording, block, BlockAction.UPDATE, true));
    }
}
