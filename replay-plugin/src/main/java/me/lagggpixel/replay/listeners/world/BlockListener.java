package me.lagggpixel.replay.listeners.world;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.events.block.BlockChangeEvent;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.api.utils.block.BlockEventType;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.utils.block.BlockAction;
import me.lagggpixel.replay.replay.ReplayManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockEvent(BlockChangeEvent e) {
        Block block = e.getBlock();
        World world = block.getWorld();

        if ((e.getEventType() == BlockEventType.FROM_TO || e.getEventType() == BlockEventType.PHYSICS)
                && (block.getType() == Material.WATER || block.getType() == Material.LAVA)) return;

        IRecording recording = ReplayManager.getInstance().getActiveRecording(world);
        if (recording == null) return;

        IFrame frame = recording.getLastFrame();
        BlockAction action = determineBlockAction(e, block, recording);
        if (action == null) return;

        // Schedule block capture AFTER the event completes
        Bukkit.getScheduler().runTask(Replay.getInstance(), () -> {
            // Now the block state reflects the NEW state
            recording.addBlockUpdate(frame, block);

            BlockCache cache = new BlockCache(block); // Captures NEW state
            Recordable recordable = Replay.getInstance().getVersionSupport()
                    .createBlockRecordable(recording, cache, action, true);
            frame.addRecordable(recordable);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPhysics(BlockPhysicsEvent e) {
        if (e.isCancelled()) return;

        // Only record physics events that actually change the block
        Block block = e.getBlock();
        Material changedType = e.getChangedType();

        // Skip if the block isn't actually changing
        if (block.getType() == changedType) return;

        // Skip water/lava flow physics (too spammy)
        if (changedType == Material.WATER || changedType == Material.LAVA) return;

        e.setCancelled(callBlockChangeEvent(null, block, BlockEventType.PHYSICS));
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent e) {
        callBlockChangeEvent(null, e.getBlock(), BlockEventType.REDSTONE);
    }

    @EventHandler
    public void onCanBuild(BlockCanBuildEvent e) {
        callBlockChangeEvent(null, e.getBlock(), BlockEventType.CAN_BUILD);
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.SPREAD));
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.FORM));
    }

    @EventHandler
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.MULTI_PLACE));
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.FROM_TO));
    }

    @EventHandler
    public void onEntityBlockForm(EntityBlockFormEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getEntity(), e.getBlock(), BlockEventType.ENTITY_FORM));
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.DAMAGE));
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.GROW));
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.BURN));
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.DISPENSE));
    }

    @EventHandler
    public void onBlockExp(BlockExpEvent e) {
        callBlockChangeEvent(null, e.getBlock(), BlockEventType.EXP);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.EXPLODE));
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.FADE));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.PLACE));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.BREAK));
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.PISTON_EXTEND));
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.PISTON_RETRACT));
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.IGNITE));
    }

    private BlockAction determineBlockAction(BlockChangeEvent e, Block block, IRecording recording) {
        BlockAction action;
        if (e.getEventType() == BlockEventType.BREAK) {
            action = BlockAction.BREAK;
            if (e.getEntity() instanceof Player) {
                addSwingAnimation((Player) e.getEntity(), recording);
            }
        } else if (e.getEventType() == BlockEventType.PLACE && e.getEntity() instanceof Player) {
            action = BlockAction.PLACE;
            Player player = (Player) e.getEntity();
            addSwingAnimation(player, recording);
        } else if (e.getEventType() == BlockEventType.PISTON_EXTEND || e.getEventType() == BlockEventType.PISTON_RETRACT) {
            action = BlockAction.INTERACT;
        } else action = BlockAction.UPDATE;
        return action;
    }

    private void addSwingAnimation(Player player, IRecording recording) {
        Recordable animation = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, player, AnimationType.SWING_MAIN_HAND);
        recording.getLastFrame().addRecordable(animation);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        /*
        if (e.getClickedBlock() == null) return;

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        World world = block.getWorld();
        byte data = block.getData();
        Location loc = block.getLocation();

        BlockCache cache = new BlockCache(world, block.getType(), data, loc);

        boolean isInteractable = Replay.getInstance().getVersionSupport().isInteractable(block);

        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(world.getName());
        if (a == null) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
        if (recording == null) return;

        if (!isInteractable) return;

        Recordable recordable = Replay.getInstance().getVersionSupport().createBlockRecordable(recording, cache, BlockAction.INTERACT, true);
        Recordable animation = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, player, AnimationType.SWING_MAIN_HAND);
        recording.getLastFrame().addRecordable(recordable, animation);
         */
    }

    private boolean callBlockChangeEvent(Entity entity, Block block, BlockEventType eventType) {
        BlockChangeEvent event = new BlockChangeEvent(entity, block, eventType);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }
}
