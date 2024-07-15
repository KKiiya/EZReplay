package me.lagggpixel.replay.listeners.recordables;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.BlockAction;
import me.lagggpixel.replay.replay.ReplayManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(player);
        Block block = e.getBlock();

        World world = block.getWorld();
        Material material = block.getType();
        byte data = block.getData();
        Location location = block.getLocation();

        if (a == null) return;
        if (e.isCancelled()) return;

        IRecording recording = ReplayManager.getInstance().getActiveReplay(a);
        if (recording == null) return;

        Recordable recordable = Replay.getInstance().getVersionSupport().createBlockRecordable(recording, world, material, data, location, BlockAction.PLACE, true);
        recording.getLastFrame().addRecordable(recordable);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(player);
        Block block = e.getBlockPlaced();

        World world = block.getWorld();
        Material material = block.getType();
        byte data = block.getData();
        Location location = block.getLocation();

        if (a == null) return;

        IRecording recording = ReplayManager.getInstance().getActiveReplay(a);
        if (recording == null) return;

        if (e.getItemInHand().getType() == Material.TNT) {
            Recordable tntSpawn = Replay.getInstance().getVersionSupport().createTntSpawnRecordable(recording, e.getBlockPlaced().getLocation());
            Recordable animation = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, player, AnimationType.SWING_MAIN_HAND);
            recording.getLastFrame().addRecordable(tntSpawn, animation);
            return;
        }

        if (e.isCancelled()) return;

        Recordable recordable = Replay.getInstance().getVersionSupport().createBlockRecordable(recording, world, material, data, location, BlockAction.BREAK, true);
        Recordable animation = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, player, AnimationType.SWING_MAIN_HAND);
        recording.getLastFrame().addRecordable(recordable, animation);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(player);
        Block block = e.getClickedBlock();
        if (block == null) return;

        World world = block.getWorld();
        Material material = block.getType();
        byte data = block.getData();
        Location location = block.getLocation();

        if (a == null) return;

        if (e.isCancelled()) return;

        IRecording recording = ReplayManager.getInstance().getActiveReplay(a);
        if (recording == null) return;

        Recordable recordable = Replay.getInstance().getVersionSupport().createBlockRecordable(recording, world, material, data, location, BlockAction.UPDATE, true);
        recording.getLastFrame().addRecordable(recordable);
    }
}
